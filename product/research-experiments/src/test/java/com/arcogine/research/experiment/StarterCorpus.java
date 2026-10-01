package com.arcogine.research.experiment;

import com.arcogine.research.experiment.ExperimentFixture.WindowIntent;
import com.arcogine.research.experiment.ProcessingOccupancyOracle.ResourceOccupancy;
import com.arcogine.research.experiment.ThreeStepRoutingFamily.Stage;
import com.arcogine.research.experiment.WaitingWorkByStepOracle.Attribution;
import com.arcogine.research.experiment.WaitingWorkByStepOracle.WaitingAtStep;
import com.arcogine.types.MachineId;
import java.util.List;
import java.util.Set;

/**
 * A deliberately small corpus of deterministic, non-spatial fixtures with ground truth, not a
 * statement of what any diagnostic method should conclude.
 *
 * <p>Every fixture uses the same capture protocol (an observation right after submission, one
 * mid-run observation, events retained through the mid-run boundary and again through completion),
 * so a fixture differs from another only in the design it authors and the workload it submits.
 * Expected values are hand-derived from {@code docs/architecture/engine-semantics.md}, not copied
 * from a run, and each derivation is stated next to its fixture. Like the Engine's conformance
 * fixtures, they change together with any change to the Engine interpretation.
 *
 * <p>A new case joins the corpus by building a model, a script and expected claims; the corpus-wide
 * tests then hold it to deterministic replay, its declared window and its expected claims.
 */
public final class StarterCorpus {

    public static final String BASELINE_ID = "three-step/capacity-constrained-baseline";
    public static final String ASSEMBLE_VARIANT_ID = "three-step/assemble-capacity-variant";
    public static final String MULTI_ELIGIBLE_ID = "three-step/multi-eligible-waiting";
    public static final String LONG_STEP_PARALLEL_ID = "three-step/long-step-and-parallel-capacity";

    /**
     * {@code CUT} 2 ticks, {@code ASSEMBLE} 6, {@code INSPECT} 3, one single-capacity resource each,
     * so {@code ASSEMBLE} has the least capacity per tick of work and the stage before it is faster
     * and never starves it.
     */
    public static final ThreeStepRoutingFamily BASELINE_FAMILY =
            new ThreeStepRoutingFamily(Stage.of(2, 1), Stage.of(6, 1), Stage.of(3, 1));

    /** The baseline with exactly one authored fact changed: the assembler's concurrency, 1 to 2. */
    public static final ThreeStepRoutingFamily ASSEMBLE_VARIANT_FAMILY = BASELINE_FAMILY.withAssemble(Stage.of(6, 2));

    /** {@code ASSEMBLE} is eligible on two single-capacity resources, so its waiting work is shared. */
    public static final ThreeStepRoutingFamily MULTI_ELIGIBLE_FAMILY =
            new ThreeStepRoutingFamily(Stage.of(1, 1), Stage.of(8, 1, 1), Stage.of(1, 1));

    /** A long {@code ASSEMBLE} step on one resource with concurrency 2. */
    public static final ThreeStepRoutingFamily LONG_STEP_PARALLEL_FAMILY =
            new ThreeStepRoutingFamily(Stage.of(1, 1), Stage.of(12, 2), Stage.of(1, 1));

    public static final long BASELINE_QUANTITY = 4;
    public static final long MULTI_ELIGIBLE_QUANTITY = 5;
    public static final long LONG_STEP_PARALLEL_QUANTITY = 3;

    /** Generous: every fixture is quiescent long before it. */
    private static final long DEADLINE_TICKS = 1_000;

    private StarterCorpus() {}

    public static List<ExperimentFixture> all() {
        return List.of(
                capacityConstrainedBaseline(),
                assembleCapacityVariant(),
                multiEligibleWaiting(),
                longStepAndParallelCapacity());
    }

    /**
     * An obvious active capacity constraint.
     *
     * <p>The single order's four units finish at {@code 29}: with {@code ASSEMBLE} never starved,
     * the run is the first unit's {@code CUT} (2), then the constraint serving all four units back
     * to back (4 x 6), then the last unit's {@code INSPECT} (3). At tick 8 two units wait for the
     * one assembler, which is the only eligible resource for the step. Occupancy through
     * completion is, per resource, its units times its step duration (8, 24 and 12 job-ticks)
     * against the run's 29 ticks times its concurrency.
     */
    public static ExperimentFixture capacityConstrainedBaseline() {
        ThreeStepRoutingFamily family = BASELINE_FAMILY;
        MachineId cutter = family.cutResources().getFirst();
        MachineId assembler = family.assembleResources().getFirst();
        MachineId inspector = family.inspectResources().getFirst();
        return new ExperimentFixture(
                BASELINE_ID,
                family.model(),
                script(BASELINE_QUANTITY, 8),
                WindowIntent.COMPLETE_RUN,
                List.of(
                        ExpectedClaim.derives(
                                "two units wait for the only assembler at tick 8",
                                new WaitingWorkByStepOracle("mid-run"),
                                List.of(assembleWaiting(2, new Attribution.SingleResource(assembler)))),
                        ExpectedClaim.derives(
                                "processing occupancy through completion",
                                new ProcessingOccupancyOracle(ExperimentEvidence.CLOSING_LABEL),
                                List.of(
                                        occupancy(cutter, 8, 29),
                                        occupancy(assembler, 24, 29),
                                        occupancy(inspector, 12, 29)))));
    }

    /**
     * The baseline with the assembler's concurrency raised to 2 and nothing else changed, so the two
     * runs are a controlled one-variable pair. The order now finishes at {@code 20}: the second
     * assemble slot lets two units be assembled at once, and {@code INSPECT} (4 x 3 from the first
     * unit's arrival at tick 8) becomes the limiting stage. At tick 8 one unit waits for the assembler.
     */
    public static ExperimentFixture assembleCapacityVariant() {
        ThreeStepRoutingFamily family = ASSEMBLE_VARIANT_FAMILY;
        MachineId cutter = family.cutResources().getFirst();
        MachineId assembler = family.assembleResources().getFirst();
        MachineId inspector = family.inspectResources().getFirst();
        return new ExperimentFixture(
                ASSEMBLE_VARIANT_ID,
                family.model(),
                script(BASELINE_QUANTITY, 8),
                WindowIntent.COMPLETE_RUN,
                List.of(
                        ExpectedClaim.derives(
                                "one unit waits for the assembler at tick 8",
                                new WaitingWorkByStepOracle("mid-run"),
                                List.of(assembleWaiting(1, new Attribution.SingleResource(assembler)))),
                        ExpectedClaim.derives(
                                "processing occupancy through completion",
                                new ProcessingOccupancyOracle(ExperimentEvidence.CLOSING_LABEL),
                                List.of(
                                        occupancy(cutter, 8, 20),
                                        occupancy(assembler, 24, 40),
                                        occupancy(inspector, 12, 20)))));
    }

    /**
     * Work eligible for two resources, where per-resource queue depth alone is misleading.
     *
     * <p>Five units are cut one tick apart. The first two are assembled at ticks 1 and 2, one on each
     * assembler; by tick 5 the other three wait for either assembler. They wait in the shared
     * backlog, so every resource's own queue depth is zero while three units wait.
     */
    public static ExperimentFixture multiEligibleWaiting() {
        ThreeStepRoutingFamily family = MULTI_ELIGIBLE_FAMILY;
        return new ExperimentFixture(
                MULTI_ELIGIBLE_ID,
                family.model(),
                script(MULTI_ELIGIBLE_QUANTITY, 5),
                WindowIntent.COMPLETE_RUN,
                List.of(ExpectedClaim.derives(
                        "three units wait for either assembler at tick 5",
                        new WaitingWorkByStepOracle("mid-run"),
                        List.of(assembleWaiting(
                                3, new Attribution.SharedEligibleSet(Set.copyOf(family.assembleResources())))))));
    }

    /**
     * Long unfinished steps, and concurrency above 1, where completion-credited {@code busyTicks}
     * misleads.
     *
     * <p>Three units, {@code CUT} 1 tick, {@code ASSEMBLE} 12 ticks on one resource with concurrency
     * 2. Units 1 and 2 start assembling at ticks 1 and 2 and are still running at the last supported
     * event before tick 10 (tick 3, when unit 3 begins waiting), so at that boundary the assembler's
     * {@code busyTicks} is 0 although both of its slots are occupied. Occupancy over the interval is
     * (3-1) + (3-2) = 3 job-ticks of 3 x 2 = 6. The run finishes at tick 26, when the assembler's
     * {@code busyTicks} is 3 x 12 = 36: more than the 26 elapsed ticks, because two jobs overlap, while
     * its occupancy is 36 job-ticks of 26 x 2 = 52. At submission no supported time has elapsed, so
     * occupancy over an interval is undefined and must be refused.
     */
    public static ExperimentFixture longStepAndParallelCapacity() {
        ThreeStepRoutingFamily family = LONG_STEP_PARALLEL_FAMILY;
        MachineId cutter = family.cutResources().getFirst();
        MachineId assembler = family.assembleResources().getFirst();
        MachineId inspector = family.inspectResources().getFirst();
        return new ExperimentFixture(
                LONG_STEP_PARALLEL_ID,
                family.model(),
                script(LONG_STEP_PARALLEL_QUANTITY, 10),
                WindowIntent.COMPLETE_RUN,
                List.of(
                        ExpectedClaim.refuses(
                                "no supported time has elapsed at submission",
                                new ProcessingOccupancyOracle("after-submission")),
                        ExpectedClaim.derives(
                                "processing occupancy while two long steps are unfinished",
                                new ProcessingOccupancyOracle("mid-run"),
                                List.of(occupancy(cutter, 3, 3), occupancy(assembler, 3, 6), occupancy(inspector, 0, 3))),
                        ExpectedClaim.derives(
                                "processing occupancy through completion",
                                new ProcessingOccupancyOracle(ExperimentEvidence.CLOSING_LABEL),
                                List.of(occupancy(cutter, 3, 26), occupancy(assembler, 36, 52), occupancy(inspector, 3, 26)))));
    }

    private static List<ExperimentStep> script(long quantity, long midRunTicks) {
        return List.of(
                ExperimentStep.submit(ThreeStepRoutingFamily.PRODUCT, quantity, ThreeStepRoutingFamily.UNIT_PRICE),
                ExperimentStep.observe("after-submission"),
                ExperimentStep.advanceUntil(midRunTicks),
                ExperimentStep.observe("mid-run"),
                ExperimentStep.captureEvents("through-mid-run"),
                ExperimentStep.advanceToQuiescence(DEADLINE_TICKS),
                ExperimentStep.captureEvents("through-completion"));
    }

    private static WaitingAtStep assembleWaiting(long waitingJobs, Attribution attribution) {
        return new WaitingAtStep(
                ThreeStepRoutingFamily.OPERATION_ID,
                ThreeStepRoutingFamily.ASSEMBLE_STEP_ID,
                ThreeStepRoutingFamily.ASSEMBLE,
                waitingJobs,
                attribution);
    }

    private static ResourceOccupancy occupancy(MachineId machineId, long occupiedJobTicks, long capacityJobTicks) {
        return new ResourceOccupancy(machineId, occupiedJobTicks, capacityJobTicks);
    }
}
