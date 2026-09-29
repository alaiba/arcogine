package com.arcogine.factory.research;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.model.validation.FactoryModelValidationException;
import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeEventType;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.factory.research.EvidenceWindow.CollectionPoint;
import com.arcogine.factory.research.EvidenceWindow.CollectionPoint.Kind;
import com.arcogine.factory.research.ExperimentEvidence.CommandRecord;
import com.arcogine.factory.research.ExperimentEvidence.CommandRecord.Outcome;
import com.arcogine.factory.research.ExperimentFixture.WindowIntent;
import com.arcogine.types.MachineId;
import com.arcogine.types.OrderId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.LongStream;
import org.junit.jupiter.api.Test;

/**
 * The embedded runner and the evidence-window semantics, exercised through real runs of the
 * supported runtime contract.
 */
class ExperimentRunnerTest {

    private static final ThreeStepRoutingFamily FAMILY = StarterCorpus.BASELINE_FAMILY;
    private static final long DEADLINE_TICKS = 1_000;

    private static ExperimentFixture fixture(String id, WindowIntent intent, ExperimentStep... script) {
        return new ExperimentFixture(id, FAMILY.model(), List.of(script), intent, List.of());
    }

    private static ExperimentStep submit(long quantity) {
        return ExperimentStep.submit(ThreeStepRoutingFamily.PRODUCT, quantity, ThreeStepRoutingFamily.UNIT_PRICE);
    }

    private static CollectionPoint point(ExperimentEvidence evidence, String label) {
        return evidence.window().collectionPoints().stream()
                .filter(candidate -> candidate.label().equals(label))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void aCompleteRunRecordsRunIdentityBoundsAndEveryCollectionPoint() {
        ExperimentFixture fixture = StarterCorpus.capacityConstrainedBaseline();
        ExperimentEvidence evidence = ExperimentRunner.run(fixture);
        EvidenceWindow window = evidence.window();
        RuntimeObservation closing = evidence.observation(ExperimentEvidence.CLOSING_LABEL);

        // The run identity is kept exactly as the runtime issued it: on the window and on every
        // observation and event, never the normalization placeholder.
        assertNotEquals(ExperimentEvidence.NORMALIZED_RUN_ID, window.runId());
        assertTrue(evidence.observations().values().stream()
                .allMatch(observation -> observation.metadata().runId().equals(window.runId())));
        assertTrue(evidence.retainedEvents().stream().allMatch(event -> event.runId().equals(window.runId())));

        // Every supported event of the run was retained, from the first through the closing cursor.
        assertEquals(WindowIntent.COMPLETE_RUN, window.declaredIntent());
        assertTrue(window.isComplete());
        assertEquals(List.of(), window.missingSequences());
        assertEquals(1, window.startSequence());
        assertEquals(closing.metadata().latestEventSequence(), window.endSequence());
        assertEquals(closing.metadata().latestEventSequence(), window.runFinalSequence());
        assertEquals(
                LongStream.rangeClosed(1, window.runFinalSequence()).boxed().toList(),
                evidence.retainedEvents().stream().map(RuntimeEventEnvelope::sequence).toList());

        // Collection points appear in script order, each with the run's event cursor at that moment.
        List<CollectionPoint> points = window.collectionPoints();
        assertEquals(
                List.of("after-submission", "mid-run", "through-mid-run", "through-completion", "closing"),
                points.stream().map(CollectionPoint::label).toList());
        assertEquals(
                List.of(Kind.OBSERVATION, Kind.OBSERVATION, Kind.EVENTS_RETAINED, Kind.EVENTS_RETAINED,
                        Kind.CLOSING_OBSERVATION),
                points.stream().map(CollectionPoint::kind).toList());
        assertEquals(List.of(1, 3, 4, 6, 7), points.stream().map(CollectionPoint::stepIndex).toList());
        // ORDER_ACCEPTED, the first unit's dispatch, and one JOB_WAITING for each of the three queued units.
        assertEquals(5, point(evidence, "after-submission").runCursor());
        assertEquals(
                evidence.observation("mid-run").metadata().latestEventSequence(),
                point(evidence, "mid-run").runCursor());
        // Draining moves events out of the runtime; it does not move the cursor.
        assertEquals(point(evidence, "mid-run").runCursor(), point(evidence, "through-mid-run").runCursor());
        assertEquals(window.runFinalSequence(), point(evidence, "through-completion").runCursor());
    }

    @Test
    void evidenceRecordsTheSemanticInputsAndProvenanceOfTheRun() {
        ExperimentFixture fixture = StarterCorpus.capacityConstrainedBaseline();
        ExperimentEvidence evidence = ExperimentRunner.run(fixture);

        assertEquals(fixture.id(), evidence.fixtureId());
        assertEquals(fixture.authoredModel(), evidence.publishedModel());
        assertEquals(fixture.script(), evidence.script());
        // The fingerprint comes from the actual published model, and the runtime reports the same one.
        assertEquals(fixture.publishedModel().fingerprint(), evidence.modelFingerprint());
        assertTrue(evidence.retainedEvents().stream()
                .allMatch(event -> event.modelFingerprint().equals(evidence.modelFingerprint())));
        assertTrue(evidence.observations().values().stream()
                .allMatch(observation -> observation.metadata().modelFingerprint().equals(evidence.modelFingerprint())));
        assertTrue(evidence.allCommandsAccepted());
        assertEquals(
                List.of(Optional.of(new OrderId(1))),
                evidence.commands().stream().map(CommandRecord::acceptedOrder).toList());
    }

    @Test
    void eventsThatWereNeverDrainedAreReportedAsMissingNotSilentlyDropped() {
        ExperimentEvidence evidence = ExperimentRunner.run(fixture(
                "partial/tail-never-captured",
                WindowIntent.PARTIAL,
                submit(4),
                ExperimentStep.captureEvents("submission-only"),
                ExperimentStep.advanceToQuiescence(DEADLINE_TICKS)));
        EvidenceWindow window = evidence.window();

        assertFalse(window.isComplete());
        assertEquals(1, window.startSequence());
        assertEquals(5, window.endSequence());
        assertTrue(window.runFinalSequence() > window.endSequence());
        assertEquals(List.of(new SequenceRange(6, window.runFinalSequence())), window.missingSequences());
        assertEquals(5, evidence.retainedEvents().size());
    }

    @Test
    void eventsDiscardedBeforeTheCaptureAreReportedAsMissingAtTheStart() {
        ExperimentEvidence evidence = ExperimentRunner.run(fixture(
                "partial/joined-late",
                WindowIntent.PARTIAL,
                submit(4),
                ExperimentStep.discardEvents("joined-late"),
                ExperimentStep.advanceToQuiescence(DEADLINE_TICKS),
                ExperimentStep.captureEvents("after-join")));
        EvidenceWindow window = evidence.window();

        assertFalse(window.isComplete());
        assertEquals(6, window.startSequence());
        assertEquals(window.runFinalSequence(), window.endSequence());
        assertEquals(List.of(new SequenceRange(1, 5)), window.missingSequences());
        assertEquals(
                List.of(Kind.EVENTS_DISCARDED, Kind.EVENTS_RETAINED, Kind.CLOSING_OBSERVATION),
                window.collectionPoints().stream().map(CollectionPoint::kind).toList());
        // The discard is a collection point that knew the cursor it discarded up to.
        assertEquals(5, point(evidence, "joined-late").runCursor());
    }

    @Test
    void aCompleteRunDeclarationThatCapturedAPartialTraceFailsLoudly() {
        ExperimentFixture mistaken = fixture(
                "mistaken/complete-but-discarded",
                WindowIntent.COMPLETE_RUN,
                submit(4),
                ExperimentStep.discardEvents("oops"),
                ExperimentStep.advanceToQuiescence(DEADLINE_TICKS),
                ExperimentStep.captureEvents("rest"));

        IllegalStateException failure = assertThrows(IllegalStateException.class, () -> ExperimentRunner.run(mistaken));

        assertTrue(failure.getMessage().contains("COMPLETE_RUN"), failure.getMessage());
        assertTrue(failure.getMessage().contains("from=1, through=5"), failure.getMessage());
    }

    @Test
    void aPartialDeclarationThatCapturedEverythingFailsLoudly() {
        ExperimentFixture mistaken = fixture(
                "mistaken/partial-but-complete",
                WindowIntent.PARTIAL,
                submit(4),
                ExperimentStep.advanceToQuiescence(DEADLINE_TICKS),
                ExperimentStep.captureEvents("everything"));

        IllegalStateException failure = assertThrows(IllegalStateException.class, () -> ExperimentRunner.run(mistaken));

        assertTrue(failure.getMessage().contains("PARTIAL"), failure.getMessage());
        assertTrue(failure.getMessage().contains("is complete"), failure.getMessage());
    }

    @Test
    void aRunStillActiveAtItsQuiescenceDeadlineIsAMalformedExperimentNotAPartialResult() {
        ExperimentFixture tooShort = fixture(
                "malformed/deadline-before-quiescence",
                WindowIntent.COMPLETE_RUN,
                submit(4),
                ExperimentStep.advanceToQuiescence(5),
                ExperimentStep.captureEvents("never-reached"));

        IllegalStateException failure = assertThrows(IllegalStateException.class, () -> ExperimentRunner.run(tooShort));

        assertTrue(failure.getMessage().contains("still active at deadline"), failure.getMessage());
    }

    @Test
    void commandsTheSupportedBoundaryRejectsAreEvidenceNotExceptions() {
        MachineId cutter = FAMILY.cutResources().getFirst();
        MachineId inspector = FAMILY.inspectResources().getFirst();
        ExperimentEvidence evidence = ExperimentRunner.run(fixture(
                "commands/accepted-and-rejected",
                WindowIntent.COMPLETE_RUN,
                ExperimentStep.setAvailability(inspector, false),
                ExperimentStep.setAvailability(inspector, true),
                submit(0),
                ExperimentStep.setAvailability(new MachineId(99), false),
                submit(4),
                ExperimentStep.setAvailability(cutter, false),
                ExperimentStep.advanceToQuiescence(DEADLINE_TICKS),
                ExperimentStep.captureEvents("everything")));

        List<CommandRecord> commands = evidence.commands();
        assertEquals(
                List.of(Outcome.ACCEPTED, Outcome.ACCEPTED, Outcome.REJECTED, Outcome.REJECTED, Outcome.ACCEPTED,
                        Outcome.REJECTED),
                commands.stream().map(CommandRecord::outcome).toList());
        assertEquals(
                List.of("ACCEPTED", "ACCEPTED", "OutOfRange", "UnknownId", "ACCEPTED", "InvalidStateTransition"),
                commands.stream().map(CommandRecord::code).toList());
        assertEquals(List.of(0, 1, 2, 3, 4, 5), commands.stream().map(CommandRecord::stepIndex).toList());
        assertFalse(evidence.allCommandsAccepted());
        // Only the accepted workload submission yields an order.
        assertEquals(
                List.of(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                        Optional.of(new OrderId(1)), Optional.empty()),
                commands.stream().map(CommandRecord::acceptedOrder).toList());

        // The supported stream shows the two applied availability changes and nothing for the three
        // rejected commands: ORDER_ACCEPTED is sequence 3 even though a rejected submission came first.
        List<RuntimeEventEnvelope> events = evidence.retainedEvents();
        assertEquals(RuntimeEventType.MACHINE_AVAILABILITY_CHANGED, events.get(0).eventType());
        assertEquals(RuntimeEventType.MACHINE_AVAILABILITY_CHANGED, events.get(1).eventType());
        assertEquals(RuntimeEventType.ORDER_ACCEPTED, events.get(2).eventType());
        assertTrue(evidence.window().isComplete());
    }

    @Test
    void evidenceIsReadOnly() {
        ExperimentEvidence evidence = ExperimentRunner.run(StarterCorpus.capacityConstrainedBaseline());

        assertThrows(UnsupportedOperationException.class, () -> evidence.retainedEvents().clear());
        assertThrows(UnsupportedOperationException.class, () -> evidence.commands().clear());
        assertThrows(UnsupportedOperationException.class, () -> evidence.script().clear());
        assertThrows(UnsupportedOperationException.class, () -> evidence.observations().clear());
        assertThrows(UnsupportedOperationException.class, () -> evidence.window().collectionPoints().clear());
        assertThrows(UnsupportedOperationException.class, () -> evidence.window().missingSequences().clear());
    }

    @Test
    void fixturesRejectAmbiguousOrReservedCollectionLabels() {
        FactoryModel model = FAMILY.model();
        List<ExperimentStep> duplicated =
                List.of(submit(1), ExperimentStep.observe("point"), ExperimentStep.captureEvents("point"));
        List<ExperimentStep> reserved = List.of(submit(1), ExperimentStep.observe(ExperimentEvidence.CLOSING_LABEL));

        assertThrows(
                IllegalArgumentException.class,
                () -> new ExperimentFixture("f", model, duplicated, WindowIntent.PARTIAL, List.of()));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExperimentFixture("f", model, reserved, WindowIntent.PARTIAL, List.of()));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExperimentFixture("f", model, List.of(), WindowIntent.PARTIAL, List.of()));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExperimentFixture(" ", model, List.of(submit(1)), WindowIntent.PARTIAL, List.of()));
        assertThrows(IllegalArgumentException.class, () -> ExperimentStep.observe(" "));
    }

    @Test
    void aFixtureWhoseModelCannotBePublishedFailsWhereItIsBuilt() {
        FactoryModel unpublishable = new FactoryModel(
                List.of(new ConfiguredResource(new MachineId(1), "Only", 1, null, 0)),
                List.of(new OperationDefinition(
                        1,
                        "Route",
                        List.of(new OperationStepDefinition(1, "STEP", Set.of(new MachineId(2)), 5)))),
                List.of(new ProductDefinition(ThreeStepRoutingFamily.PRODUCT, "Widget", 1)));

        assertThrows(
                FactoryModelValidationException.class,
                () -> new ExperimentFixture("f", unpublishable, List.of(submit(1)), WindowIntent.PARTIAL, List.of()));
    }
}
