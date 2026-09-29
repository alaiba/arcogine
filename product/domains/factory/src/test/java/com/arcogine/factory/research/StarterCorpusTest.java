package com.arcogine.factory.research;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.change.FactoryModelSemanticComparator;
import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelArtifact;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeEventPayload;
import com.arcogine.factory.process.RuntimeEventType;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.factory.process.RuntimePerformanceObservation;
import com.arcogine.factory.process.RuntimeRunState;
import com.arcogine.factory.research.ExperimentFixture.WindowIntent;
import com.arcogine.governance.SemanticArtifact;
import com.arcogine.governance.change.SemanticChange;
import com.arcogine.governance.change.SemanticChangeKind;
import com.arcogine.types.MachineId;
import com.arcogine.types.ModelFingerprint;
import com.arcogine.types.RunId;
import com.arcogine.types.SimTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Invariants every fixture in the corpus must satisfy, so a new fixture inherits them by joining
 * the corpus, plus the controlled-pair and determinism proofs that need more than one fixture.
 */
class StarterCorpusTest {

    static Stream<Arguments> corpus() {
        return StarterCorpus.all().stream().map(fixture -> Arguments.of(fixture.id(), fixture));
    }

    private static long completionTick(ExperimentEvidence evidence) {
        List<RuntimeEventEnvelope> completions = evidence.retainedEvents().stream()
                .filter(event -> event.eventType() == RuntimeEventType.ORDER_COMPLETED)
                .toList();
        assertEquals(1, completions.size(), "the fixture submits one order, so exactly one completion is expected");
        return completions.getFirst().simulationTime().value();
    }

    private static SemanticArtifact artifact(ExperimentFixture fixture) {
        return new SemanticArtifact(
                fixture.publishedModel().fingerprint(), FactoryModelArtifact.encode(fixture.publishedModel()));
    }

    @Test
    void theCorpusHoldsTheFourStarterCasesUnderDistinctIdsAndDistinctModels() {
        List<ExperimentFixture> fixtures = StarterCorpus.all();

        assertEquals(
                List.of(
                        StarterCorpus.BASELINE_ID,
                        StarterCorpus.ASSEMBLE_VARIANT_ID,
                        StarterCorpus.MULTI_ELIGIBLE_ID,
                        StarterCorpus.LONG_STEP_PARALLEL_ID),
                fixtures.stream().map(ExperimentFixture::id).toList());
        Set<ModelFingerprint> fingerprints = fixtures.stream()
                .map(fixture -> fixture.publishedModel().fingerprint())
                .collect(Collectors.toSet());
        assertEquals(fixtures.size(), fingerprints.size(), "each fixture authors a different design");
        assertTrue(fixtures.stream().allMatch(fixture -> fixture.authoredModel().spatial().isEmpty()),
                "the starter corpus is non-spatial");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("corpus")
    void everyFixtureRunsToQuiescenceWithAcceptedCommandsAndItsDeclaredWindow(String id, ExperimentFixture fixture) {
        ExperimentEvidence evidence = ExperimentRunner.run(fixture);

        assertEquals(id, evidence.fixtureId());
        assertTrue(evidence.allCommandsAccepted());
        assertEquals(RuntimeRunState.QUIESCENT, evidence.observation(ExperimentEvidence.CLOSING_LABEL).metadata().runState());
        assertEquals(fixture.windowIntent() == WindowIntent.COMPLETE_RUN, evidence.window().isComplete());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("corpus")
    void everyFixtureReplaysDeterministicallyAfterNormalizingOnlyRunIdentity(String id, ExperimentFixture fixture) {
        ExperimentEvidence first = ExperimentRunner.run(fixture);
        ExperimentEvidence second = ExperimentRunner.run(fixture);

        // The identity being normalized really is run-specific: without normalization the two
        // independent runs of one fixture are not equal.
        assertNotEquals(first.window().runId(), second.window().runId());
        assertNotEquals(first, second);
        // With only that identity normalized, the supported evidence is equivalent.
        assertEquals(first.withNormalizedRunIdentity(), second.withNormalizedRunIdentity());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("corpus")
    void everyExpectedClaimHoldsOnFreshEvidence(String id, ExperimentFixture fixture) {
        ExperimentEvidence evidence = ExperimentRunner.run(fixture);

        assertFalse(fixture.expectedClaims().isEmpty(), "a corpus fixture states the ground truth it is for");
        for (ExpectedClaim<?> claim : fixture.expectedClaims()) {
            ExpectedClaim.ClaimCheck<?> check = claim.check(evidence);
            assertTrue(check.holds(), () -> "claim '" + check.name() + "' expected " + check.expected() + " but was "
                    + check.actual());
        }
    }

    @Test
    void normalizationReplacesOnlyTheRunIdentity() {
        ExperimentEvidence raw = ExperimentRunner.run(StarterCorpus.capacityConstrainedBaseline());
        ExperimentEvidence normalized = raw.withNormalizedRunIdentity();

        // Fixture identity, model, script and command outcomes (including the OrderId a command
        // returned) are semantic and untouched.
        assertEquals(raw.fixtureId(), normalized.fixtureId());
        assertEquals(raw.publishedModel(), normalized.publishedModel());
        assertEquals(raw.modelFingerprint(), normalized.modelFingerprint());
        assertEquals(raw.script(), normalized.script());
        assertEquals(raw.commands(), normalized.commands());

        EvidenceWindow before = raw.window();
        EvidenceWindow after = normalized.window();
        assertEquals(ExperimentEvidence.NORMALIZED_RUN_ID, after.runId());
        assertEquals(before.declaredIntent(), after.declaredIntent());
        assertEquals(before.startSequence(), after.startSequence());
        assertEquals(before.endSequence(), after.endSequence());
        assertEquals(before.runFinalSequence(), after.runFinalSequence());
        assertEquals(before.missingSequences(), after.missingSequences());
        assertEquals(before.collectionPoints(), after.collectionPoints());

        assertEquals(raw.retainedEvents().size(), normalized.retainedEvents().size());
        for (int i = 0; i < raw.retainedEvents().size(); i++) {
            RuntimeEventEnvelope a = raw.retainedEvents().get(i);
            RuntimeEventEnvelope b = normalized.retainedEvents().get(i);
            assertEquals(ExperimentEvidence.NORMALIZED_RUN_ID, b.runId());
            assertEquals(a.sequence(), b.sequence());
            assertEquals(a.simulationTime(), b.simulationTime());
            assertEquals(a.eventType(), b.eventType());
            assertEquals(a.modelFingerprint(), b.modelFingerprint());
            assertEquals(a.controlledRevisionId(), b.controlledRevisionId());
            assertEquals(a.affectedEntityRefs(), b.affectedEntityRefs());
            assertEquals(a.payload(), b.payload());
        }

        assertEquals(new ArrayList<>(raw.observations().keySet()), new ArrayList<>(normalized.observations().keySet()));
        raw.observations().forEach((label, a) -> {
            RuntimeObservation b = normalized.observation(label);
            assertEquals(ExperimentEvidence.NORMALIZED_RUN_ID, b.metadata().runId());
            assertEquals(a.metadata().modelFingerprint(), b.metadata().modelFingerprint());
            assertEquals(a.metadata().currentTime(), b.metadata().currentTime());
            assertEquals(a.metadata().runState(), b.metadata().runState());
            assertEquals(a.metadata().latestEventSequence(), b.metadata().latestEventSequence());
            assertEquals(a.resources(), b.resources());
            assertEquals(a.orders(), b.orders());
            assertEquals(a.jobs(), b.jobs());
            assertEquals(a.pendingWork(), b.pendingWork());
            assertEquals(a.performance(), b.performance());
        });
    }

    @Test
    void normalizedEvidenceStillDistinguishesEverySemanticDifference() {
        ExperimentEvidence base =
                ExperimentRunner.run(StarterCorpus.capacityConstrainedBaseline()).withNormalizedRunIdentity();
        List<RuntimeEventEnvelope> events = base.retainedEvents();
        int dispatchIndex = 0;
        while (events.get(dispatchIndex).eventType() != RuntimeEventType.JOB_DISPATCHED) {
            dispatchIndex++;
        }
        RuntimeEventEnvelope dispatch = events.get(dispatchIndex);
        RuntimeEventPayload.JobDispatched payload = (RuntimeEventPayload.JobDispatched) dispatch.payload();

        // A different authored design and outcome.
        assertNotEquals(base, ExperimentRunner.run(StarterCorpus.assembleCapacityVariant()).withNormalizedRunIdentity());

        // Event order.
        List<RuntimeEventEnvelope> reordered = new ArrayList<>(events);
        Collections.swap(reordered, 5, 6);
        assertNotEquals(base, TamperedEvidence.withEvents(base, reordered));

        // Event sequence, event time, the model fingerprint an event carries, and a machine identity in a payload.
        assertNotEquals(base, withEvent(base, dispatchIndex, rebuilt(dispatch, dispatch.sequence() + 1,
                dispatch.simulationTime(), dispatch.modelFingerprint(), dispatch.payload())));
        assertNotEquals(base, withEvent(base, dispatchIndex, rebuilt(dispatch, dispatch.sequence(),
                dispatch.simulationTime().plus(1), dispatch.modelFingerprint(), dispatch.payload())));
        assertNotEquals(base, withEvent(base, dispatchIndex, rebuilt(dispatch, dispatch.sequence(),
                dispatch.simulationTime(), new ModelFingerprint("factory-model", "sha256", "0".repeat(64)),
                dispatch.payload())));
        assertNotEquals(base, withEvent(base, dispatchIndex, rebuilt(dispatch, dispatch.sequence(),
                dispatch.simulationTime(), dispatch.modelFingerprint(),
                new RuntimeEventPayload.JobDispatched(
                        payload.jobId(), payload.orderId(), new MachineId(99), payload.stepIndex()))));

        // An outcome value in an observation.
        RuntimeObservation closing = base.observation(ExperimentEvidence.CLOSING_LABEL);
        RuntimePerformanceObservation performance = closing.performance();
        RuntimeObservation altered = new RuntimeObservation(
                closing.metadata(),
                closing.resources(),
                closing.orders(),
                closing.jobs(),
                closing.pendingWork(),
                new RuntimePerformanceObservation(
                        performance.backlog(),
                        performance.completedOrders() + 1,
                        performance.completedSalesValue(),
                        performance.averageLeadTime(),
                        performance.throughputPerTick()));
        assertNotEquals(base, TamperedEvidence.withObservation(base, ExperimentEvidence.CLOSING_LABEL, altered));
    }

    private static ExperimentEvidence withEvent(ExperimentEvidence evidence, int index, RuntimeEventEnvelope event) {
        List<RuntimeEventEnvelope> events = new ArrayList<>(evidence.retainedEvents());
        events.set(index, event);
        return TamperedEvidence.withEvents(evidence, events);
    }

    private static RuntimeEventEnvelope rebuilt(
            RuntimeEventEnvelope event,
            long sequence,
            SimTime time,
            ModelFingerprint fingerprint,
            RuntimeEventPayload payload) {
        return new RuntimeEventEnvelope(
                event.runId(),
                sequence,
                time,
                event.eventType(),
                fingerprint,
                event.controlledRevisionId(),
                event.affectedEntityRefs(),
                payload);
    }

    /**
     * The constraint is the step with the most work per unit of capacity: its duration divided by the
     * summed concurrency of its eligible resources.
     */
    private static double workPerCapacity(FactoryModel model, OperationStepDefinition step) {
        Map<MachineId, Integer> concurrency = model.resources().stream()
                .collect(Collectors.toMap(ConfiguredResource::id, ConfiguredResource::concurrency));
        int capacity = step.eligibleResources().stream().mapToInt(concurrency::get).sum();
        return (double) step.duration() / capacity;
    }

    @Test
    void theBaselineFinishesAtTheClosedFormMakespanOfAnUnstarvedConstraint() {
        ExperimentFixture fixture = StarterCorpus.capacityConstrainedBaseline();
        FactoryModel model = fixture.authoredModel();
        ThreeStepRoutingFamily family = StarterCorpus.BASELINE_FAMILY;
        long units = StarterCorpus.BASELINE_QUANTITY;

        // ASSEMBLE is the authored constraint, and the stage before it is no slower, so it is never starved.
        OperationDefinition operation = model.operations().getFirst();
        OperationStepDefinition constraint = operation.steps().stream()
                .max(Comparator.comparingDouble(step -> workPerCapacity(model, step)))
                .orElseThrow();
        assertEquals(ThreeStepRoutingFamily.ASSEMBLE, constraint.name());
        assertTrue(workPerCapacity(model, operation.steps().getFirst()) <= workPerCapacity(model, constraint));

        // An unstarved constraint serves every unit back to back: the first unit's upstream time, then all
        // units through the constraint, then the last unit's downstream time.
        long makespan = family.cut().duration() + units * family.assemble().duration() + family.inspect().duration();
        assertEquals(29, makespan);

        ExperimentEvidence evidence = ExperimentRunner.run(fixture);
        assertEquals(makespan, completionTick(evidence));
        assertEquals(
                makespan,
                evidence.observation(ExperimentEvidence.CLOSING_LABEL).metadata().currentTime().value());
    }

    @Test
    void theVariantDiffersFromTheBaselineInExactlyOneAuthoredFactAndNothingIsHidden() {
        ExperimentFixture baseline = StarterCorpus.capacityConstrainedBaseline();
        ExperimentFixture variant = StarterCorpus.assembleCapacityVariant();
        MachineId assembler = StarterCorpus.BASELINE_FAMILY.assembleResources().getFirst();

        // The workload/command script and the evidence protocol are identical: nothing changed in the inputs.
        assertEquals(baseline.script(), variant.script());
        assertEquals(baseline.windowIntent(), variant.windowIntent());

        // Factory's own semantic comparison attributes exactly one authored change: the assembler's concurrency.
        List<SemanticChange> changes = new FactoryModelSemanticComparator().compare(artifact(baseline), artifact(variant));
        assertEquals(1, changes.size(), changes.toString());
        SemanticChange change = changes.getFirst();
        assertEquals(SemanticChangeKind.ENTITY_MODIFIED, change.kind());
        assertEquals("factory.resource", change.entity().entityType());
        assertEquals(Long.toString(assembler.value()), change.entity().entityId());
        assertEquals("concurrency: 1 -> 2", change.detail());

        // The authored change is visible in the model identity, and the outcome fact that changed is completion.
        assertNotEquals(baseline.publishedModel().fingerprint(), variant.publishedModel().fingerprint());
        assertEquals(29, completionTick(ExperimentRunner.run(baseline)));
        assertEquals(20, completionTick(ExperimentRunner.run(variant)));
    }

    @Test
    void everyRunOfAFixtureStartsFromAFreshRuntimeSoFixturesNeverShareState() {
        ExperimentFixture fixture = StarterCorpus.multiEligibleWaiting();

        Set<RunId> runIds = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            ExperimentEvidence evidence = ExperimentRunner.run(fixture);
            runIds.add(evidence.window().runId());
            assertEquals(1, evidence.retainedEvents().getFirst().sequence(), "a fresh run starts a new sequence epoch");
        }
        assertEquals(3, runIds.size());
    }
}
