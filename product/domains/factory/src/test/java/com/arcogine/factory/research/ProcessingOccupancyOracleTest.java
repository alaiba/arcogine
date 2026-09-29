package com.arcogine.factory.research;

import static com.arcogine.factory.research.OutcomeAssertions.assertDerived;
import static com.arcogine.factory.research.OutcomeAssertions.assertUnderdetermined;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.process.ResourceObservation;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.factory.research.ExperimentFixture.WindowIntent;
import com.arcogine.factory.research.ProcessingOccupancyOracle.ResourceOccupancy;
import com.arcogine.types.MachineId;
import com.arcogine.types.MachineState;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * The temporal/performance counterexample: cumulative, completion-credited {@code busyTicks} is
 * neither an instantaneous occupancy nor, divided by elapsed time, an interval utilization, and the
 * derivation that measures occupancy from supported events refuses where the evidence does not
 * license a claim.
 */
class ProcessingOccupancyOracleTest {

    private static final ThreeStepRoutingFamily FAMILY = StarterCorpus.LONG_STEP_PARALLEL_FAMILY;
    private static final MachineId ASSEMBLER = FAMILY.assembleResources().getFirst();

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

    @Test
    void completionCreditedBusyTicksReadZeroWhileTheAssemblerIsFullyOccupied() {
        ResourceObservation assembler = resource(evidence.observation("mid-run"), ASSEMBLER);

        // The supported observation: both concurrency slots hold a running step, and nothing has been credited.
        assertEquals(MachineState.Busy, assembler.state());
        assertEquals(assembler.concurrency(), assembler.activeJobIds().size());
        assertEquals(0, assembler.busyTicks(), "busyTicks is credited only when a step completes");

        // Read as a utilization, busyTicks would say a fully occupied resource is idle. The derivation over
        // dispatch and completion events sees the two unfinished steps: (3-1) + (3-2) job-ticks so far.
        ResourceOccupancy occupancy = occupancyOf(occupancyAt("mid-run"), ASSEMBLER);
        assertEquals(3, occupancy.occupiedJobTicks());
        assertEquals(6, occupancy.capacityJobTicks());
        assertTrue(occupancy.occupiedJobTicks() > assembler.busyTicks());
    }

    @Test
    void cumulativeBusyTicksCanExceedElapsedTimeSoDividingThemByItIsNotAUtilization() {
        RuntimeObservation closing = evidence.observation(ExperimentEvidence.CLOSING_LABEL);
        ResourceObservation assembler = resource(closing, ASSEMBLER);
        long elapsed = closing.metadata().currentTime().value();

        // Two jobs overlap on one resource, so cumulative processing ticks pass the elapsed ticks:
        // busyTicks / elapsed would be about 1.38, more than the resource can possibly be occupied.
        assertEquals(2, assembler.concurrency());
        assertTrue(assembler.busyTicks() > elapsed, assembler.busyTicks() + " vs elapsed " + elapsed);

        // Occupancy measured against capacity (elapsed time x concurrency) stays within what is possible.
        ResourceOccupancy occupancy = occupancyOf(occupancyAt(ExperimentEvidence.CLOSING_LABEL), ASSEMBLER);
        assertEquals(52, occupancy.capacityJobTicks());
        assertEquals(36, occupancy.occupiedJobTicks());
        assertTrue(occupancy.occupiedJobTicks() <= occupancy.capacityJobTicks());
    }

    @Test
    void busyTicksEqualsDerivedOccupancyOnlyWhenNoStepIsUnfinished() {
        // At completion every started step has finished, so the credited total is exactly the occupied total.
        RuntimeObservation closing = evidence.observation(ExperimentEvidence.CLOSING_LABEL);
        for (ResourceOccupancy occupancy : occupancyAt(ExperimentEvidence.CLOSING_LABEL)) {
            assertEquals(
                    resource(closing, occupancy.machineId()).busyTicks(),
                    occupancy.occupiedJobTicks(),
                    "resource " + occupancy.machineId());
        }

        // Mid-run the assembler has two unfinished steps: the difference between the two is exactly them.
        RuntimeObservation midRun = evidence.observation("mid-run");
        ResourceOccupancy assembler = occupancyOf(occupancyAt("mid-run"), ASSEMBLER);
        assertNotEquals(resource(midRun, ASSEMBLER).busyTicks(), assembler.occupiedJobTicks());
        // The cutter has no unfinished step at that boundary, so for it the two agree.
        MachineId cutter = FAMILY.cutResources().getFirst();
        assertEquals(resource(midRun, cutter).busyTicks(), occupancyOf(occupancyAt("mid-run"), cutter).occupiedJobTicks());
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
