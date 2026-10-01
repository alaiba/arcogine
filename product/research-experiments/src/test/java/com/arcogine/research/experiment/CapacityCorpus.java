package com.arcogine.research.experiment;

import com.arcogine.research.experiment.DispatchProfileOracle.DispatchProfile;
import com.arcogine.research.experiment.DispatchProfileOracle.ResourceStepDispatches;
import com.arcogine.research.experiment.DispatchProfileOracle.StepWait;
import com.arcogine.research.experiment.EligibilityPoolOccupancyOracle.EligibilityPool;
import com.arcogine.research.experiment.EligibilityPoolOccupancyOracle.PoolOccupancies;
import com.arcogine.research.experiment.EligibilityPoolOccupancyOracle.PoolOccupancy;
import com.arcogine.research.experiment.ExperimentFixture.WindowIntent;
import com.arcogine.research.experiment.LinearRoutingFamily.Resource;
import com.arcogine.research.experiment.LinearRoutingFamily.Step;
import com.arcogine.research.experiment.ThreeStepRoutingFamily.Stage;
import com.arcogine.research.experiment.WaitingWorkByStepOracle.Attribution;
import com.arcogine.research.experiment.WaitingWorkByStepOracle.WaitingAtStep;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Deterministic, non-spatial fixtures about how capacity is authored, shared and measured: resource
 * order as an experiment input, resources that serve more than one step, and measurements that
 * answer different questions. Like {@link StarterCorpus}, it states ground truth, not what any
 * diagnostic method should conclude, and no fixture here selects a dispatch policy.
 *
 * <p>Expected values are hand-derived from {@code docs/architecture/engine-semantics.md}, not copied
 * from a run, and each derivation is stated next to its fixture. The rules they use are section 2
 * (selection prefers a resource that can accept now, then the smaller {@code combinedQueueDepth},
 * then the lower {@code MachineId}; a single-eligible step waits in its resource's own FIFO queue and
 * a multi-eligible step in the shared backlog) and section 4 (equal-time work in insertion order; a
 * released resource first serves its own queue, then the backlog). Like the Engine's conformance
 * fixtures, they change together with any change to the Engine interpretation.
 *
 * <p>Occupancy below is in job-ticks, written {@code occupied/capacity} with capacity the run's
 * elapsed ticks times the pool's summed concurrency; waits are a step's summed ticks between a unit
 * becoming ready for the step and its dispatch.
 */
public final class CapacityCorpus {

    public static final String DEDICATED_AUTHORED_ID = "resource-order/dedicated-assemblers-authored";
    public static final String DEDICATED_REVERSED_ID = "resource-order/dedicated-assemblers-reversed";
    public static final String SHARED_AUTHORED_ID = "resource-order/shared-resource-authored";
    public static final String SHARED_REVERSED_ID = "resource-order/shared-resource-reversed";
    public static final String WAITING_VERSUS_OCCUPANCY_ID = "diagnostics/waiting-versus-occupancy";
    public static final String TWO_INSPECTORS_ID = "pooling/two-dedicated-inspectors";
    public static final String SHARED_ASSEMBLE_INSPECT_ID = "pooling/shared-assemble-inspect-resource";

    private static final String CUT = ThreeStepRoutingFamily.CUT;
    private static final String ASSEMBLE = ThreeStepRoutingFamily.ASSEMBLE;
    private static final String INSPECT = ThreeStepRoutingFamily.INSPECT;

    /**
     * {@code CUT} 2, {@code ASSEMBLE} 3, {@code INSPECT} 2, with {@code ASSEMBLE} eligible on two
     * single-capacity assemblers that serve nothing else.
     */
    public static final LinearRoutingFamily DEDICATED_ASSEMBLERS =
            new ThreeStepRoutingFamily(Stage.of(2, 1), Stage.of(3, 1, 1), Stage.of(2, 1)).asLinearFamily();

    /**
     * The same routing with one dedicated assembler and one single-capacity {@code Shared} resource
     * eligible for both {@code ASSEMBLE} and {@code INSPECT}, authored last.
     */
    public static final LinearRoutingFamily SHARED_RESOURCE = new LinearRoutingFamily(
            List.of(new Step(CUT, 2), new Step(ASSEMBLE, 3), new Step(INSPECT, 2)),
            List.of(
                    Resource.of("Cutter", 1, CUT),
                    Resource.of("Assembler", 1, ASSEMBLE),
                    Resource.of("Shared", 1, ASSEMBLE, INSPECT)));

    /** {@code CUT} 3, {@code ASSEMBLE} 4, {@code INSPECT} 5, one single-capacity resource per step. */
    public static final LinearRoutingFamily DEDICATED_LINE =
            new ThreeStepRoutingFamily(Stage.of(3, 1), Stage.of(4, 1), Stage.of(5, 1)).asLinearFamily();

    /** The dedicated line with a second single-capacity inspector. */
    public static final LinearRoutingFamily TWO_INSPECTORS =
            new ThreeStepRoutingFamily(Stage.of(3, 1), Stage.of(4, 1), Stage.of(5, 1, 1)).asLinearFamily();

    /**
     * The dedicated line plus one single-capacity {@code Shared} resource eligible for both {@code
     * ASSEMBLE} and {@code INSPECT}, authored last.
     */
    public static final LinearRoutingFamily SHARED_ASSEMBLE_INSPECT = new LinearRoutingFamily(
            DEDICATED_LINE.steps(),
            List.of(
                    Resource.of("Cutter", 1, CUT),
                    Resource.of("Assembler", 1, ASSEMBLE),
                    Resource.of("Inspector", 1, INSPECT),
                    Resource.of("Shared", 1, ASSEMBLE, INSPECT)));

    public static final long PAIR_QUANTITY = 2;
    public static final long LINE_QUANTITY = 12;

    /** Generous: every fixture is quiescent long before it. */
    private static final long DEADLINE_TICKS = 1_000;

    private CapacityCorpus() {}

    public static List<ExperimentFixture> all() {
        return List.of(
                dedicatedAssemblersAuthored(),
                dedicatedAssemblersReversed(),
                sharedResourceAuthored(),
                sharedResourceReversed(),
                waitingVersusOccupancy(),
                twoDedicatedInspectors(),
                sharedAssembleInspectResource());
    }

    /**
     * Two interchangeable assemblers in authored order: the final tie-break is reached, and it does
     * not change completion.
     *
     * <p>Unit 1 is cut over 0-2 while unit 2 waits in the cutter's queue. At 2 both assemblers can
     * accept and neither has queued work, so the lower {@code MachineId}, {@code Assembler 1}, takes
     * unit 1 (2-5) while unit 2 is cut (2-4); at 4 only {@code Assembler 2} can accept unit 2 (4-7).
     * Unit 1 is inspected over 5-7; at 7 unit 2 finishes assembly and is inspected over 7-9, whichever
     * of the two tick-7 completions is handled first. The order completes at 9.
     */
    public static ExperimentFixture dedicatedAssemblersAuthored() {
        return new ExperimentFixture(
                DEDICATED_AUTHORED_ID,
                DEDICATED_ASSEMBLERS.model(),
                wholeRun(PAIR_QUANTITY),
                WindowIntent.COMPLETE_RUN,
                List.of(completesAt(9)));
    }

    /**
     * The same design with its resource order reversed. Now {@code Assembler 2} has the lower {@code
     * MachineId}, so it takes unit 1 at 2 and {@code Assembler 1} takes unit 2 at 4. The two assemblers
     * are configured identically and {@code ASSEMBLE}'s duration belongs to the step, so every other
     * time is unchanged and the order still completes at 9.
     */
    public static ExperimentFixture dedicatedAssemblersReversed() {
        return new ExperimentFixture(
                DEDICATED_REVERSED_ID,
                DEDICATED_ASSEMBLERS.withReversedResourceOrder().model(),
                wholeRun(PAIR_QUANTITY),
                WindowIntent.COMPLETE_RUN,
                List.of(completesAt(9)));
    }

    /**
     * A shared resource in authored order (last): the final tie-break gives the first unit's assembly
     * to the dedicated assembler.
     *
     * <p>Unit 1 is cut over 0-2, unit 2 over 2-4. At 2 both {@code Assembler} and {@code Shared} can
     * accept with no queued work, so the lower {@code MachineId}, {@code Assembler}, takes unit 1
     * (2-5); at 4 only {@code Shared} can accept unit 2 (4-7). Unit 1 finishes assembly at 5 and waits
     * in {@code Shared}'s own queue, because {@code Shared} is the only resource eligible for {@code
     * INSPECT}. From 7 {@code Shared} inspects both units back to back, so the order completes at 11.
     *
     * <p>Pools: {@code CUT} with {@code Cutter}, 2 x 2 = 4 of 11 x 1; {@code ASSEMBLE} and {@code
     * INSPECT} merged by {@code Shared}, with {@code Assembler} and {@code Shared}, 2 x 3 + 2 x 2 = 10
     * of 11 x 2. Each assembler assembles one unit and {@code Shared} inspects both. Unit 2 waits 2 for
     * {@code CUT}; nothing waits for {@code ASSEMBLE}; the two inspections start at 7 and 9 for units
     * ready at 5 and 7, so {@code INSPECT} waits 4 in total.
     */
    public static ExperimentFixture sharedResourceAuthored() {
        LinearRoutingFamily family = SHARED_RESOURCE;
        return new ExperimentFixture(
                SHARED_AUTHORED_ID,
                family.model(),
                wholeRun(PAIR_QUANTITY),
                WindowIntent.COMPLETE_RUN,
                List.of(
                        completesAt(11),
                        poolOccupancy(
                                occupancy(pool(family, List.of(CUT), List.of("Cutter")), 4, 11),
                                occupancy(pool(family, List.of(ASSEMBLE, INSPECT), List.of("Assembler", "Shared")), 10, 22)),
                        dispatchProfile(
                                List.of(
                                        dispatches(family, "Cutter", CUT, 2),
                                        dispatches(family, "Assembler", ASSEMBLE, 1),
                                        dispatches(family, "Shared", ASSEMBLE, 1),
                                        dispatches(family, "Shared", INSPECT, 2)),
                                List.of(
                                        waits(family, CUT, 2, 2),
                                        waits(family, ASSEMBLE, 2, 0),
                                        waits(family, INSPECT, 2, 4)))));
    }

    /**
     * The same design with its resource order reversed, so {@code Shared} is first and wins the tie.
     *
     * <p>At 2 the lower {@code MachineId} is now {@code Shared}, which takes unit 1's assembly (2-5);
     * at 4 only {@code Assembler} can accept unit 2 (4-7). At 5 {@code Shared} is released with nothing
     * waiting for it and inspects unit 1 (5-7). At 7 unit 2 finishes assembly and {@code Shared}
     * finishes inspecting unit 1, so unit 2 is inspected over 7-9 whichever completion is handled
     * first. The order completes at 9, two ticks earlier than in authored order: in this design, which
     * resource takes the first assembly decides whether inspection overlaps assembly.
     *
     * <p>Pools: {@code CUT}, 4 of 9 x 1; the merged pool, 10 of 9 x 2. The dispatch counts per resource
     * and step are those of authored order, but no inspection waits.
     */
    public static ExperimentFixture sharedResourceReversed() {
        LinearRoutingFamily family = SHARED_RESOURCE.withReversedResourceOrder();
        return new ExperimentFixture(
                SHARED_REVERSED_ID,
                family.model(),
                wholeRun(PAIR_QUANTITY),
                WindowIntent.COMPLETE_RUN,
                List.of(
                        completesAt(9),
                        poolOccupancy(
                                occupancy(pool(family, List.of(CUT), List.of("Cutter")), 4, 9),
                                occupancy(pool(family, List.of(ASSEMBLE, INSPECT), List.of("Assembler", "Shared")), 10, 18)),
                        dispatchProfile(
                                List.of(
                                        dispatches(family, "Cutter", CUT, 2),
                                        dispatches(family, "Shared", ASSEMBLE, 1),
                                        dispatches(family, "Assembler", ASSEMBLE, 1),
                                        dispatches(family, "Shared", INSPECT, 2)),
                                List.of(
                                        waits(family, CUT, 2, 2),
                                        waits(family, ASSEMBLE, 2, 0),
                                        waits(family, INSPECT, 2, 0)))));
    }

    /**
     * Waiting work and occupancy rank the same steps differently.
     *
     * <p>Every step has its own resource and the cutter's queue holds all twelve units, so unit {@code
     * k} is cut over {@code 3(k-1)}-{@code 3k}. The assembler is slower: unit {@code k} is ready at
     * {@code 3k}, its predecessor is assembled at {@code 4k-1}, and {@code 3k <= 4k-1}, so assembly
     * runs back to back and ends at {@code 3+4k}. The inspector is slower still: unit {@code k} is ready
     * at {@code 3+4k}, its predecessor is inspected at {@code 2+5k}, and {@code 3+4k <= 2+5k}, so
     * inspection ends at {@code 7+5k}. The order completes at {@code 7+60 = 67}.
     *
     * <p>At tick 33 units 1-11 are cut and unit 12 is being cut; units 1-7 are assembled ({@code
     * 3+4k <= 33}), unit 8 is being assembled and units 9-11 wait for the assembler; units 1-5 are
     * inspected ({@code 7+5k <= 33}), unit 6 is being inspected and unit 7 waits. So three units wait
     * at {@code ASSEMBLE} and one at {@code INSPECT}, while over the run the inspector is occupied for
     * 12 x 5 = 60 of 67 ticks and the assembler for 12 x 4 = 48 (the cutter 36).
     */
    public static ExperimentFixture waitingVersusOccupancy() {
        LinearRoutingFamily family = DEDICATED_LINE;
        return new ExperimentFixture(
                WAITING_VERSUS_OCCUPANCY_ID,
                family.model(),
                withMidRunObservation(LINE_QUANTITY, 33),
                WindowIntent.COMPLETE_RUN,
                List.of(
                        ExpectedClaim.derives(
                                "three units wait at ASSEMBLE and one at INSPECT at tick 33",
                                new WaitingWorkByStepOracle("mid-run"),
                                List.of(
                                        waiting(family, ASSEMBLE, 3, "Assembler"),
                                        waiting(family, INSPECT, 1, "Inspector"))),
                        completesAt(67),
                        poolOccupancy(
                                occupancy(pool(family, List.of(CUT), List.of("Cutter")), 36, 67),
                                occupancy(pool(family, List.of(ASSEMBLE), List.of("Assembler")), 48, 67),
                                occupancy(pool(family, List.of(INSPECT), List.of("Inspector")), 60, 67))));
    }

    /**
     * Two dedicated inspectors: assembly paces the line and the inspectors share the inspections.
     *
     * <p>Cutting and assembly are as in {@link #waitingVersusOccupancy()}: unit {@code k} is assembled
     * at {@code 3+4k}, so assembly waits {@code k-1} for units 2-12, 66 in total. Unit 1 reaches
     * {@code INSPECT} at 7 with both inspectors free, and the lower {@code MachineId}, {@code Inspector
     * 1}, takes it. From then on a unit arrives every 4 ticks and each inspection takes 5, so exactly
     * one inspector is free at every arrival: odd units go to {@code Inspector 1} and even units to
     * {@code Inspector 2}, none waits, and unit 12 is inspected over 51-56. The order completes at 56.
     *
     * <p>Pools: {@code CUT} 36 of 56; {@code ASSEMBLE} 48 of 56; {@code INSPECT}, with both
     * inspectors, 60 of 56 x 2. Each inspector inspects six units.
     */
    public static ExperimentFixture twoDedicatedInspectors() {
        LinearRoutingFamily family = TWO_INSPECTORS;
        return new ExperimentFixture(
                TWO_INSPECTORS_ID,
                family.model(),
                wholeRun(LINE_QUANTITY),
                WindowIntent.COMPLETE_RUN,
                List.of(
                        completesAt(56),
                        poolOccupancy(
                                occupancy(pool(family, List.of(CUT), List.of("Cutter")), 36, 56),
                                occupancy(pool(family, List.of(ASSEMBLE), List.of("Assembler")), 48, 56),
                                occupancy(pool(family, List.of(INSPECT), List.of("Inspector 1", "Inspector 2")), 60, 112)),
                        dispatchProfile(
                                List.of(
                                        dispatches(family, "Cutter", CUT, 12),
                                        dispatches(family, "Assembler", ASSEMBLE, 12),
                                        dispatches(family, "Inspector 1", INSPECT, 6),
                                        dispatches(family, "Inspector 2", INSPECT, 6)),
                                List.of(
                                        waits(family, CUT, 12, 198),
                                        waits(family, ASSEMBLE, 12, 66),
                                        waits(family, INSPECT, 12, 0)))));
    }

    /**
     * A shared resource pools assembly and inspection capacity.
     *
     * <p>The cutter cuts unit {@code k} over {@code 3(k-1)}-{@code 3k}. Both later steps are
     * multi-eligible, so their waiting work is in the shared backlog; selection among two busy
     * resources with nothing queued falls to the lower {@code MachineId}, which only decides where an
     * entry first waits, since the backlog reselects. Same-tick completions are handled in the order
     * they were scheduled. Working forward from those rules gives, per unit, the assembler and
     * interval of {@code ASSEMBLE} and then of {@code INSPECT}:
     *
     * <pre>
     * unit  ready  ASSEMBLE              INSPECT
     *   1     3    Assembler  3-7        Inspector  7-12
     *   2     6    Shared     6-10       Shared    10-15
     *   3     9    Assembler  9-13       Inspector 13-18
     *   4    12    Assembler 13-17       Inspector 18-23
     *   5    15    Shared    15-19       Shared    19-24
     *   6    18    Assembler 18-22       Inspector 23-28
     *   7    21    Assembler 22-26       Inspector 28-33
     *   8    24    Shared    24-28       Shared    28-33
     *   9    27    Assembler 27-31       Inspector 33-38
     *  10    30    Assembler 31-35       Inspector 38-43
     *  11    33    Shared    33-37       Shared    37-42
     *  12    36    Assembler 36-40       Shared    42-47
     * </pre>
     *
     * The decisive points: at 3 both assemble-capable resources are free and {@code Assembler} wins
     * the tie; whenever {@code Shared} finishes an assembly with the inspector busy, the unit's own
     * inspection takes {@code Shared} next; at 33 three completions coincide and are handled in the
     * order they were scheduled -- the inspector's (scheduled at 28, ahead of {@code Shared}'s, also at
     * 28) first -- so waiting unit 9 goes to the inspector and {@code Shared}, released next, assembles
     * unit 11 as soon as it is cut; and at 37 {@code Shared} inspects the unit it has just assembled,
     * 11, while unit 10 waits for the inspector at 38. Handing {@code Shared} to unit 10 instead would
     * occupy the same two intervals, so nothing measured below depends on that choice. The order
     * completes at 47.
     *
     * <p>{@code Shared} serves both steps: it assembles units 2, 5, 8 and 11 and inspects units 2, 5,
     * 8, 11 and 12. Waits: {@code CUT} {@code 3(k-1)} per unit, 198 in total; {@code ASSEMBLE} 1 each
     * for units 4, 7 and 10, 3 in total; {@code INSPECT} 1+1+2+2+3+2 = 11 for units 4, 6, 7, 9, 10 and
     * 12. Pools: {@code CUT} 36 of 47; {@code ASSEMBLE} and {@code INSPECT} merged, with {@code
     * Assembler}, {@code Inspector} and {@code Shared}, 12 x 4 + 12 x 5 = 108 of 47 x 3 = 141. The two
     * ratios are exactly equal, 36/47 = 108/141, so the two pools tie for the maximum occupancy.
     */
    public static ExperimentFixture sharedAssembleInspectResource() {
        LinearRoutingFamily family = SHARED_ASSEMBLE_INSPECT;
        return new ExperimentFixture(
                SHARED_ASSEMBLE_INSPECT_ID,
                family.model(),
                wholeRun(LINE_QUANTITY),
                WindowIntent.COMPLETE_RUN,
                List.of(
                        completesAt(47),
                        poolOccupancy(
                                occupancy(pool(family, List.of(CUT), List.of("Cutter")), 36, 47),
                                occupancy(
                                        pool(family, List.of(ASSEMBLE, INSPECT), List.of("Assembler", "Inspector", "Shared")),
                                        108,
                                        141)),
                        dispatchProfile(
                                List.of(
                                        dispatches(family, "Cutter", CUT, 12),
                                        dispatches(family, "Assembler", ASSEMBLE, 8),
                                        dispatches(family, "Shared", ASSEMBLE, 4),
                                        dispatches(family, "Inspector", INSPECT, 7),
                                        dispatches(family, "Shared", INSPECT, 5)),
                                List.of(
                                        waits(family, CUT, 12, 198),
                                        waits(family, ASSEMBLE, 12, 3),
                                        waits(family, INSPECT, 12, 11)))));
    }

    private static List<ExperimentStep> wholeRun(long quantity) {
        return List.of(
                ExperimentStep.submit(LinearRoutingFamily.PRODUCT, quantity, LinearRoutingFamily.UNIT_PRICE),
                ExperimentStep.advanceToQuiescence(DEADLINE_TICKS),
                ExperimentStep.captureEvents("complete-run"));
    }

    private static List<ExperimentStep> withMidRunObservation(long quantity, long midRunTicks) {
        return List.of(
                ExperimentStep.submit(LinearRoutingFamily.PRODUCT, quantity, LinearRoutingFamily.UNIT_PRICE),
                ExperimentStep.advanceUntil(midRunTicks),
                ExperimentStep.observe("mid-run"),
                ExperimentStep.captureEvents("through-mid-run"),
                ExperimentStep.advanceToQuiescence(DEADLINE_TICKS),
                ExperimentStep.captureEvents("through-completion"));
    }

    private static ExpectedClaim<Long> completesAt(long tick) {
        return ExpectedClaim.derives("the order completes at tick " + tick, new CompletionTickOracle(), tick);
    }

    private static ExpectedClaim<PoolOccupancies> poolOccupancy(PoolOccupancy... pools) {
        return ExpectedClaim.derives(
                "eligibility-pool occupancy through completion",
                new EligibilityPoolOccupancyOracle(ExperimentEvidence.CLOSING_LABEL),
                new PoolOccupancies(List.of(pools)));
    }

    private static ExpectedClaim<DispatchProfile> dispatchProfile(
            List<ResourceStepDispatches> dispatches, List<StepWait> waits) {
        return ExpectedClaim.derives(
                "dispatches per resource and step, and waits per step, through completion",
                new DispatchProfileOracle(ExperimentEvidence.CLOSING_LABEL),
                new DispatchProfile(dispatches, waits));
    }

    static OperationStep step(LinearRoutingFamily family, String name) {
        return new OperationStep(LinearRoutingFamily.OPERATION_ID, family.stepId(name), name);
    }

    static EligibilityPool pool(LinearRoutingFamily family, List<String> steps, List<String> resources) {
        return new EligibilityPool(
                steps.stream().map(name -> step(family, name)).toList(),
                resources.stream().map(family::resourceId).collect(Collectors.toSet()));
    }

    private static PoolOccupancy occupancy(EligibilityPool pool, long occupiedJobTicks, long capacityJobTicks) {
        return new PoolOccupancy(pool, occupiedJobTicks, capacityJobTicks);
    }

    private static ResourceStepDispatches dispatches(
            LinearRoutingFamily family, String resource, String step, long count) {
        return new ResourceStepDispatches(family.resourceId(resource), step(family, step), count);
    }

    private static StepWait waits(LinearRoutingFamily family, String step, long dispatches, long totalWaitTicks) {
        return new StepWait(step(family, step), dispatches, totalWaitTicks);
    }

    private static WaitingAtStep waiting(LinearRoutingFamily family, String step, long jobs, String resource) {
        return new WaitingAtStep(
                LinearRoutingFamily.OPERATION_ID,
                family.stepId(step),
                step,
                jobs,
                new Attribution.SingleResource(family.resourceId(resource)));
    }
}
