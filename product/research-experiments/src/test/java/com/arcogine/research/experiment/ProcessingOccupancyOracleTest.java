package com.arcogine.research.experiment;

import static com.arcogine.research.experiment.OutcomeAssertions.assertDerived;
import static com.arcogine.research.experiment.OutcomeAssertions.assertUnderdetermined;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.process.ResourceObservation;
import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeEventPayload;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.research.experiment.ExperimentFixture.WindowIntent;
import com.arcogine.research.experiment.ProcessingOccupancyOracle.ResourceOccupancy;
import com.arcogine.types.MachineId;
import com.arcogine.types.MachineState;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * The temporal/performance counterexample: processing time credited only when a step completes is
 * neither an instantaneous occupancy nor, divided by elapsed time, an interval utilization, and the
 * derivation that measures occupancy from supported events refuses where the evidence does not
 * license a claim.
 */
class ProcessingOccupancyOracleTest {

    private static final ThreeStepRoutingFamily FAMILY = StarterCorpus.LONG_STEP_PARALLEL_FAMILY;
    private static final MachineId ASSEMBLER = FAMILY.assembleResources().getFirst();
    private static final List<ThreeStepRoutingFamily.Stage> STAGES =
            List.of(FAMILY.cut(), FAMILY.assemble(), FAMILY.inspect());

    private final ExperimentEvidence evidence = ExperimentRunner.run(StarterCorpus.longStepAndParallelCapacity());

    private static ResourceObservation resource(RuntimeObservation observation, MachineId machineId) {
        return observation.resources().stream()
                .filter(candidate -> candidate.machineId().equals(machineId))
                .findFirst()
                .orElseThrow();
    }

    private static ResourceOccupancy occupancyOf(List<ResourceOccupancy> occupancy, MachineId machineId) {
        return occupancy.stream()
                .filter(candidate -> candidate.machineId().equals(machineId))
                .findFirst()
                .orElseThrow();
    }

    private List<ResourceOccupancy> occupancyAt(String boundaryLabel) {
        return assertDerived(new ProcessingOccupancyOracle(boundaryLabel).evaluateOn(evidence)).value();
    }

    /**
     * The comparison the derivation must be distinguishable from: processing time credited only when
     * a step completes, each {@code JOB_STEP_COMPLETED} through the boundary contributing its
     * published step duration.
     */
    private long completedStepJobTicks(String boundaryLabel, MachineId machineId) {
        long cursor = evidence.observation(boundaryLabel).metadata().latestEventSequence();
        long total = 0;
        for (RuntimeEventEnvelope event : evidence.retainedEvents()) {
            if (event.sequence() <= cursor
                    && event.payload() instanceof RuntimeEventPayload.JobStepCompleted completed
                    && completed.machineId().equals(machineId)) {
                total += STAGES.get(completed.stepIndex()).duration();
            }
        }
        return total;
    }

    @Test
    void stepsStillRunningAtTheBoundaryCountWhileTheAssemblerIsFullyOccupied() {
        ResourceObservation assembler = resource(evidence.observation("mid-run"), ASSEMBLER);

        // The supported observation: both concurrency slots hold a running step, and none has completed.
        assertEquals(MachineState.Busy, assembler.state());
        assertEquals(assembler.concurrency(), assembler.activeJobIds().size());
        assertEquals(0, completedStepJobTicks("mid-run", ASSEMBLER), "no assembler step has completed yet");

        // Credited only at completion, processing time would say a fully occupied resource is idle. The
        // derivation over dispatch and completion events sees the two unfinished steps: (3-1) + (3-2)
        // job-ticks so far.
        ResourceOccupancy occupancy = occupancyOf(occupancyAt("mid-run"), ASSEMBLER);
        assertEquals(3, occupancy.occupiedJobTicks());
        assertEquals(6, occupancy.capacityJobTicks());
    }

    @Test
    void occupiedJobTicksCanExceedElapsedTimeSoDividingThemByItIsNotAUtilization() {
        RuntimeObservation closing = evidence.observation(ExperimentEvidence.CLOSING_LABEL);
        ResourceObservation assembler = resource(closing, ASSEMBLER);
        long elapsed = closing.metadata().currentTime().value();
        ResourceOccupancy occupancy = occupancyOf(occupancyAt(ExperimentEvidence.CLOSING_LABEL), ASSEMBLER);

        // Two jobs overlap on one resource, so occupied job-ticks pass the elapsed ticks: divided by
        // elapsed time alone they would be about 1.38, more than the resource can possibly be occupied.
        assertEquals(2, assembler.concurrency());
        assertEquals(36, occupancy.occupiedJobTicks());
        assertTrue(occupancy.occupiedJobTicks() > elapsed, occupancy.occupiedJobTicks() + " vs elapsed " + elapsed);

        // Occupancy measured against capacity (elapsed time x concurrency) stays within what is possible.
        assertEquals(52, occupancy.capacityJobTicks());
        assertTrue(occupancy.occupiedJobTicks() <= occupancy.capacityJobTicks());
    }

    @Test
    void completedStepTimeEqualsDerivedOccupancyOnlyWhenNoStepIsUnfinished() {
        // At completion every started step has finished, so the completion-credited total is exactly
        // the occupied total.
        for (ResourceOccupancy occupancy : occupancyAt(ExperimentEvidence.CLOSING_LABEL)) {
            assertEquals(
                    completedStepJobTicks(ExperimentEvidence.CLOSING_LABEL, occupancy.machineId()),
                    occupancy.occupiedJobTicks(),
                    "resource " + occupancy.machineId());
        }

        // Mid-run the assembler has two unfinished steps: the difference between the two is exactly them.
        ResourceOccupancy assembler = occupancyOf(occupancyAt("mid-run"), ASSEMBLER);
        assertNotEquals(completedStepJobTicks("mid-run", ASSEMBLER), assembler.occupiedJobTicks());
        // The cutter has no unfinished step at that boundary, so for it the two agree.
        MachineId cutter = FAMILY.cutResources().getFirst();
        assertEquals(
                completedStepJobTicks("mid-run", cutter),
                occupancyOf(occupancyAt("mid-run"), cutter).occupiedJobTicks());
    }

    @Test
    void anObservationBoundaryIsTheLastSupportedEventNotTheTimeTheRunWasAdvancedTo() {
        // The script advanced to tick 10, but nothing supported happened after tick 3, so that is the
        // boundary: observed time moves only with supported events, and no elapsed duration is licensed
        // beyond it.
        RuntimeObservation midRun = evidence.observation("mid-run");

        assertEquals(3, midRun.metadata().currentTime().value());
        assertEquals(
                ThreeStepRoutingFamily.CUT_STEP_ID * 3,
                midRun.metadata().currentTime().value(),
                "three one-tick CUT steps precede the last supported event");
    }

    @Test
    void occupancyIsRefusedWhenNoSupportedTimeHasElapsed() {
        OracleOutcome.Underdetermined<List<ResourceOccupancy>> refused =
                assertUnderdetermined(new ProcessingOccupancyOracle("after-submission").evaluateOn(evidence));

        assertTrue(refused.reasons().getFirst().contains("no supported time has elapsed"), refused.toString());
    }

    @Test
    void occupancyIsRefusedWhenTheEventsItNeedsWereNotAllRetained() {
        ExperimentFixture joinedLate = new ExperimentFixture(
                "partial/long-step-joined-late",
                FAMILY.model(),
                List.of(
                        ExperimentStep.submit(
                                ThreeStepRoutingFamily.PRODUCT,
                                StarterCorpus.LONG_STEP_PARALLEL_QUANTITY,
                                ThreeStepRoutingFamily.UNIT_PRICE),
                        ExperimentStep.advanceUntil(10),
                        ExperimentStep.observe("mid-run"),
                        ExperimentStep.discardEvents("joined-late"),
                        ExperimentStep.advanceToQuiescence(1_000),
                        ExperimentStep.captureEvents("through-completion")),
                WindowIntent.PARTIAL,
                List.of());
        ExperimentEvidence partial = ExperimentRunner.run(joinedLate);
        long midRunCursor = partial.observation("mid-run").metadata().latestEventSequence();

        for (String boundary : List.of("mid-run", ExperimentEvidence.CLOSING_LABEL)) {
            OracleOutcome.Underdetermined<List<ResourceOccupancy>> refused =
                    assertUnderdetermined(new ProcessingOccupancyOracle(boundary).evaluateOn(partial));
            assertTrue(refused.reasons().getFirst().contains("not all retained"), refused.toString());
            assertTrue(refused.reasons().getFirst().contains("from=1, through=" + midRunCursor), refused.toString());
        }

        // The same derivation over the complete evidence for the same boundary does derive a value: the
        // refusal is about the window, not about the derivation or the run.
        assertEquals(3, occupancyOf(occupancyAt("mid-run"), ASSEMBLER).occupiedJobTicks());
    }

    @Test
    void occupancyIsRefusedWhenAResourceChangedAvailabilityDuringTheInterval() {
        // Capacity over the interval is elapsed time times concurrency only for a resource that was
        // online throughout. One that went offline and came back refuses the measurement instead of
        // being credited with capacity it did not have.
        MachineId inspector = FAMILY.inspectResources().getFirst();
        ExperimentEvidence toggled = ExperimentRunner.run(new ExperimentFixture(
                "availability/inspector-offline-then-online",
                FAMILY.model(),
                List.of(
                        ExperimentStep.setAvailability(inspector, false),
                        ExperimentStep.submit(
                                ThreeStepRoutingFamily.PRODUCT,
                                StarterCorpus.LONG_STEP_PARALLEL_QUANTITY,
                                ThreeStepRoutingFamily.UNIT_PRICE),
                        ExperimentStep.advanceUntil(10),
                        ExperimentStep.setAvailability(inspector, true),
                        ExperimentStep.advanceToQuiescence(1_000),
                        ExperimentStep.captureEvents("everything")),
                WindowIntent.COMPLETE_RUN,
                List.of()));
        assertTrue(toggled.allCommandsAccepted());

        OracleOutcome.Underdetermined<List<ResourceOccupancy>> refused = assertUnderdetermined(
                new ProcessingOccupancyOracle(ExperimentEvidence.CLOSING_LABEL).evaluateOn(toggled));

        assertTrue(refused.reasons().getFirst().contains("resource " + inspector + " changed availability"), refused.toString());
    }

    @Test
    void occupancyIsRefusedForAResourceThatIsOfflineAtTheBoundary() {
        MachineId inspector = FAMILY.inspectResources().getFirst();
        ExperimentEvidence offlineAtTheEnd = ExperimentRunner.run(new ExperimentFixture(
                "availability/inspector-offline-at-close",
                FAMILY.model(),
                List.of(
                        ExperimentStep.submit(
                                ThreeStepRoutingFamily.PRODUCT,
                                StarterCorpus.LONG_STEP_PARALLEL_QUANTITY,
                                ThreeStepRoutingFamily.UNIT_PRICE),
                        ExperimentStep.advanceToQuiescence(1_000),
                        ExperimentStep.setAvailability(inspector, false),
                        ExperimentStep.captureEvents("everything")),
                WindowIntent.COMPLETE_RUN,
                List.of()));

        OracleOutcome.Underdetermined<List<ResourceOccupancy>> refused = assertUnderdetermined(
                new ProcessingOccupancyOracle(ExperimentEvidence.CLOSING_LABEL).evaluateOn(offlineAtTheEnd));

        assertTrue(refused.reasons().getFirst().contains("is offline at boundary 'closing'"), refused.toString());
    }

    @Test
    void aCompletionWithoutItsDispatchIsRefused() {
        List<RuntimeEventEnvelope> withoutFirstDispatch = evidence.retainedEvents().stream()
                .filter(event -> !(event.payload() instanceof RuntimeEventPayload.JobDispatched dispatched
                        && dispatched.jobId().value() == 1 && dispatched.stepIndex() == 0))
                .toList();

        OracleOutcome.Underdetermined<List<ResourceOccupancy>> refused = assertUnderdetermined(
                new ProcessingOccupancyOracle(ExperimentEvidence.CLOSING_LABEL)
                        .evaluateOn(TamperedEvidence.withEvents(evidence, withoutFirstDispatch)));

        assertTrue(refused.reasons().getFirst().contains("has no matching dispatch"), refused.toString());
    }

    @Test
    void aDerivedOutcomeNamesTheExactObservationAndEventIntervalThatSupportIt() {
        OracleOutcome.Derived<List<ResourceOccupancy>> derived =
                assertDerived(new ProcessingOccupancyOracle("mid-run").evaluateOn(evidence));
        long cursor = evidence.observation("mid-run").metadata().latestEventSequence();

        assertEquals(ProcessingOccupancyOracle.DEFINITION, derived.definition());
        assertEquals(
                new EvidenceSupport(List.of("mid-run"), Optional.of(new SequenceRange(1, cursor))), derived.support());
        assertTrue(cursor < evidence.window().runFinalSequence(), "the interval stops at the boundary, not at the end of the run");
    }
}
