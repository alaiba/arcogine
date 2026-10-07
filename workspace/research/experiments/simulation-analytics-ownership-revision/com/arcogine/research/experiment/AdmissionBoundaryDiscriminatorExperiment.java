package com.arcogine.research.experiment;

import static com.arcogine.research.experiment.OutcomeAssertions.assertUnderdetermined;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.process.JobObservation;
import com.arcogine.factory.process.ResourceObservation;
import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeEventPayload;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.research.experiment.ExperimentFixture.WindowIntent;
import com.arcogine.research.experiment.LinearRoutingFamily.Resource;
import com.arcogine.research.experiment.LinearRoutingFamily.Step;
import com.arcogine.types.JobId;
import com.arcogine.types.JobStatus;
import com.arcogine.types.MachineId;
import com.arcogine.types.MachineState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Research-custody experiment for the reopened analytics ownership admission boundary. It tests
 * three discriminators between ownership models; it defines no production measurement.
 *
 * <ol>
 *   <li>One set of supported evidence feeds several legitimate utilization definitions that
 *       disagree, including on which machine is "most utilized". Two evidence routes (per-tick
 *       observation, dispatch/completion intervals) agree on the execution-fixed occupied
 *       slot-ticks; the definitions differ only in choices execution does not fix.
 *   <li>After an availability change an idle slot coexists with work queued for that very machine,
 *       which a two-way starved/no-work-left characterization cannot classify. A third state is
 *       computable from the observation and model alone, without consulting any dispatch rule.
 *   <li>Starved slot counting depends on a declared cap, not on execution.
 * </ol>
 *
 * <p>Every expected value was derived by hand from Engine semantics sections 2 and 4 before the
 * first run. Every derivation here is research-local: it reads supported observations, supported
 * events and the published model, and never ranks candidates or re-decides an assignment.
 */
class AdmissionBoundaryDiscriminatorExperiment {

    private static final String LOG = "[analytics-admission] ";

    // ---- 1: several legitimate utilization definitions over one evidence set ------------------

    /**
     * MIX 2 (Mixer, 1 slot) -> BAKE 4 (Oven, 2 slots) -> PACK 1 (Packer, 1 slot); six units released
     * at once. Hand derivation: the Mixer works [0,12); the Oven bakes units over [2,6), [4,8),
     * [6,10), [8,12), [10,14), [12,16); the Packer packs over [6,7), [8,9), ..., [16,17); the order
     * completes at 17. Oven working slots per tick 0..16: 0 0 1 1 2 2 2 2 2 2 2 2 2 2 1 1 0.
     */
    @Test
    void oneEvidenceSetFeedsSeveralLegitimateUtilizationDefinitions() {
        LinearRoutingFamily bakery = new LinearRoutingFamily(
                List.of(new Step("MIX", 2), new Step("BAKE", 4), new Step("PACK", 1)),
                List.of(Resource.of("Mixer", 1, "MIX"), Resource.of("Oven", 2, "BAKE"),
                        Resource.of("Packer", 1, "PACK")));
        ExperimentEvidence evidence = ExperimentRunner.runAndReplay(perTick("bakery-big-oven", bakery, 6, 17, List.of()));
        MachineId mixer = bakery.resourceId("Mixer");
        MachineId oven = bakery.resourceId("Oven");

        assertEquals(17, closing(evidence).orders().getFirst().completedAt().value(), "hand-derived finish");
        long[] ovenWorking = workingPerTick(evidence, oven, 17);
        assertEquals(List.of(0L, 0L, 1L, 1L, 2L, 2L, 2L, 2L, 2L, 2L, 2L, 2L, 2L, 2L, 1L, 1L, 0L), toList(ovenWorking));

        // Two evidence routes agree on the execution-fixed quantity: occupied slots per tick.
        long[] ovenFromEvents = workingPerTickFromEvents(evidence, oven, 17);
        assertEquals(toList(ovenWorking), toList(ovenFromEvents), "per-tick observation and event intervals agree");

        // The definitions differ in choices execution does not fix.
        long halfOpen = sum(ovenWorking, 4, 14);
        long inclusive = sum(ovenWorking, 4, 15);
        assertEquals(20, halfOpen, "per-slot, half-open [4,14): 20 of 20 slot-ticks");
        assertEquals(21, inclusive, "per-slot, inclusive [4,14]: 21 of 22 slot-ticks");
        long wholeRunOven = sum(ovenWorking, 0, 17);
        assertEquals(24, wholeRunOven, "per-slot whole run: 24 of 34 slot-ticks");
        long ovenBusyTicks = busyStateTicks(evidence, oven, 17);
        assertEquals(14, ovenBusyTicks, "machine-state Busy whole run: 14 of 17 ticks");

        // Which machine is "most utilized" depends on the definition, not on execution.
        long mixerWorking = sum(workingPerTick(evidence, mixer, 17), 0, 17);
        long mixerBusyTicks = busyStateTicks(evidence, mixer, 17);
        assertEquals(12, mixerWorking);
        assertEquals(12, mixerBusyTicks);
        assertEquals(mixerWorking * (2L * 17), wholeRunOven * (1L * 17),
                "per-slot whole-run utilization ties: Mixer 12/17 equals Oven 24/34");
        assertTrue(ovenBusyTicks > mixerBusyTicks, "machine-state Busy ranks the Oven above the Mixer");

        // The completion-credited counter equals occupied job-ticks only once nothing is running.
        assertEquals(24, resource(closing(evidence), oven).busyTicks(), "busyTicks at quiescence");
        assertEquals(8, resource(evidence.observation("t9"), oven).busyTicks(), "busyTicks at tick 9");
        assertEquals(12, sum(ovenWorking, 0, 9), "occupied slot-ticks over [0,9)");

        System.out.println(LOG + "1: Oven occupied slot-ticks agree by observation and by events " + toList(ovenWorking)
                + "; [4,14) half-open " + halfOpen + "/20, inclusive " + inclusive + "/22; whole run per-slot "
                + wholeRunOven + "/34, machine-state Busy " + ovenBusyTicks + "/17; Mixer per-slot " + mixerWorking
                + "/17 ties the Oven per-slot, but Busy " + mixerBusyTicks + "/17 ranks below it; busyTicks at t9 = "
                + resource(evidence.observation("t9"), oven).busyTicks() + " against " + sum(ovenWorking, 0, 9)
                + " occupied");
    }

    // ---- 2: after an availability change an idle slot coexists with work queued for it ---------

    /**
     * BAKE 4 on one 2-slot Oven; the Oven is taken offline, four units are submitted (all enter the
     * Oven's own queue, section 2 rules 1 and 7), and the Oven comes back online at tick 0. The
     * online trigger starts at most one queued job (section 4 rules 6-7), as does every later step
     * completion, so one slot stays idle while work waits for it: units bake over [0,4), [4,8),
     * [8,12), [12,16) and the order completes at 16. Always online, the same order completes at 8.
     */
    @Test
    void afterAnAvailabilityChangeAnIdleSlotCoexistsWithWorkQueuedForIt() {
        LinearRoutingFamily oven = new LinearRoutingFamily(List.of(new Step("BAKE", 4)), List.of(Resource.of("Oven", 2, "BAKE")));
        MachineId id = oven.resourceId("Oven");
        List<ExperimentStep> script = new ArrayList<>();
        script.add(ExperimentStep.setAvailability(id, false));
        script.add(ExperimentStep.submit(LinearRoutingFamily.PRODUCT, 4, LinearRoutingFamily.UNIT_PRICE));
        script.add(ExperimentStep.setAvailability(id, true));
        ExperimentEvidence toggled = ExperimentRunner.runAndReplay(perTick("oven-back-online", oven, 0, 16, script));
        ExperimentEvidence alwaysOnline = ExperimentRunner.runAndReplay(perTick("oven-always-online", oven, 4, 8, List.of()));
        assertTrue(toggled.allCommandsAccepted());

        RuntimeObservation afterOnline = toggled.observation("t0");
        ResourceObservation atZero = resource(afterOnline, id);
        assertEquals(MachineState.Busy, atZero.state());
        assertEquals(1, atZero.activeJobIds().size(), "one of two slots works");
        assertEquals(3, atZero.queueDepth(), "three units wait in this machine's own queue");
        assertTrue(afterOnline.pendingWork().isEmpty());
        assertEquals(16, closing(toggled).orders().getFirst().completedAt().value());
        assertEquals(8, closing(alwaysOnline).orders().getFirst().completedAt().value());

        // A two-way starved/no-work-left characterization must reject this state; a third state
        // is read from the observation and the model alone.
        Map<String, Long> slotTicks = new HashMap<>();
        for (long tick = 0; tick < 16; tick++) {
            RuntimeObservation observation = toggled.observation("t" + tick);
            ResourceObservation machine = resource(observation, id);
            long idle = machine.concurrency() - machine.activeJobIds().size();
            slotTicks.merge("working", (long) machine.activeJobIds().size(), Long::sum);
            if (idle > 0) {
                slotTicks.merge(idleCharacterization(toggled.publishedModel(), observation, id), idle, Long::sum);
            }
        }
        assertEquals(Map.of("working", 16L, "idle-with-eligible-work-queued", 12L, "no-work-left", 4L), slotTicks);

        // Completeness/refusal is a property of the analytical definition: the research-local
        // continuously-online occupancy refuses, while a per-slot basis that excludes offline time
        // (offline here for zero ticks) reports 16 of 32 slot-ticks although work waited throughout.
        String reason = String.join(" ", assertUnderdetermined(new ProcessingOccupancyOracle(ExperimentEvidence.CLOSING_LABEL)
                .evaluateOn(toggled)).reasons());
        assertTrue(reason.contains("changed availability"), reason);
        assertEquals(16, slotTicks.get("working"));

        System.out.println(LOG + "2: after coming back online the Oven has " + atZero.activeJobIds().size()
                + " active of 2 slots and queueDepth " + atZero.queueDepth() + "; completes at 16 against 8 always"
                + " online; slot-ticks " + slotTicks + "; continuously-online occupancy refuses (" + reason + ")");
    }

    // ---- 3: starved-slot counting depends on a declared cap -----------------------------------

    /**
     * MIX 4 (Mixer, 1 slot) -> PACK 1 (Packer, 2 slots); one unit. Hand derivation: MIX over [0,4),
     * PACK over [4,5). Over [0,4) both Packer slots are idle while one unit upstream still needs
     * packing: uncapped, 8 starved slot-ticks; capped by the units that still need the machine, 4.
     */
    @Test
    void starvedSlotCountingDependsOnADeclaredCap() {
        LinearRoutingFamily line = new LinearRoutingFamily(List.of(new Step("MIX", 4), new Step("PACK", 1)),
                List.of(Resource.of("Mixer", 1, "MIX"), Resource.of("Packer", 2, "PACK")));
        ExperimentEvidence evidence = ExperimentRunner.runAndReplay(perTick("wide-packer", line, 1, 5, List.of()));
        MachineId packer = line.resourceId("Packer");
        assertEquals(5, closing(evidence).orders().getFirst().completedAt().value());

        long uncapped = 0;
        long capped = 0;
        long noWorkLeft = 0;
        for (long tick = 0; tick < 5; tick++) {
            RuntimeObservation observation = evidence.observation("t" + tick);
            ResourceObservation machine = resource(observation, packer);
            long idle = machine.concurrency() - machine.activeJobIds().size();
            if (idle == 0) {
                continue;
            }
            String state = idleCharacterization(evidence.publishedModel(), observation, packer);
            if (state.equals("starved")) {
                uncapped += idle;
                capped += Math.min(idle, unitsStillNeeding(evidence.publishedModel(), observation, packer));
            } else if (state.equals("no-work-left")) {
                noWorkLeft += idle;
            }
        }
        assertEquals(8, uncapped, "uncapped starved slot-ticks over [0,4)");
        assertEquals(4, capped, "starved slot-ticks capped by the units that still need the Packer");
        assertEquals(1, noWorkLeft, "the second slot has no work left while the unit is packed");

        System.out.println(LOG + "3: Packer starved slot-ticks uncapped " + uncapped + ", capped " + capped
                + "; no-work-left " + noWorkLeft);
    }

    // ---- research-local helpers over supported evidence only ---------------------------------

    /**
     * One machine's idle-slot state at one observation, from the observation and the published model
     * only: "idle-with-eligible-work-queued" when a Queued unit's current step lists this machine;
     * otherwise "starved" when an unfinished unit still has a remaining step this machine may serve;
     * otherwise "no-work-left". It names a state; it does not say why the Engine left the slot idle.
     */
    private static String idleCharacterization(FactoryModel model, RuntimeObservation observation, MachineId machine) {
        List<OperationStepDefinition> routing = model.operations().getFirst().steps();
        for (JobObservation job : observation.jobs()) {
            if (job.status() == JobStatus.Queued && routing.get(job.currentStep()).eligibleResources().contains(machine)) {
                return "idle-with-eligible-work-queued";
            }
        }
        return unitsStillNeeding(model, observation, machine) > 0 ? "starved" : "no-work-left";
    }

    /** Unfinished units with a remaining step (a Queued unit's current step onward, a running unit's later steps) the machine may serve. */
    private static long unitsStillNeeding(FactoryModel model, RuntimeObservation observation, MachineId machine) {
        List<OperationStepDefinition> routing = model.operations().getFirst().steps();
        return observation.jobs().stream()
                .filter(job -> job.status() != JobStatus.Completed)
                .filter(job -> {
                    int from = job.status() == JobStatus.Queued ? job.currentStep() : job.currentStep() + 1;
                    for (int step = from; step < routing.size(); step++) {
                        if (routing.get(step).eligibleResources().contains(machine)) {
                            return true;
                        }
                    }
                    return false;
                })
                .count();
    }

    /** A fixture that runs {@code prefix}, submits {@code quantity} units when positive, observes ticks 0..horizon-1, and captures every event. */
    private static ExperimentFixture perTick(String id, LinearRoutingFamily family, long quantity, long horizon,
            List<ExperimentStep> prefix) {
        List<ExperimentStep> script = new ArrayList<>(prefix);
        if (quantity > 0) {
            script.add(ExperimentStep.submit(LinearRoutingFamily.PRODUCT, quantity, LinearRoutingFamily.UNIT_PRICE));
        }
        script.add(ExperimentStep.observe("t0"));
        for (long tick = 1; tick < horizon; tick++) {
            script.add(ExperimentStep.advanceUntil(tick));
            script.add(ExperimentStep.observe("t" + tick));
        }
        script.add(ExperimentStep.advanceToQuiescence(1_000));
        script.add(ExperimentStep.captureEvents("whole-run"));
        return new ExperimentFixture(id, family.model(), script, WindowIntent.COMPLETE_RUN, List.of());
    }

    private static long[] workingPerTick(ExperimentEvidence evidence, MachineId machine, long horizon) {
        long[] working = new long[(int) horizon];
        for (int tick = 0; tick < horizon; tick++) {
            working[tick] = resource(evidence.observation("t" + tick), machine).activeJobIds().size();
        }
        return working;
    }

    /** Occupied slots per tick from complete dispatch/completion events: a step occupies [dispatch, completion). */
    private static long[] workingPerTickFromEvents(ExperimentEvidence evidence, MachineId machine, long horizon) {
        assertTrue(evidence.window().isComplete(), "the event route needs the complete window");
        record Key(JobId job, int step) {}
        Map<Key, Long> started = new HashMap<>();
        long[] working = new long[(int) horizon];
        for (RuntimeEventEnvelope event : evidence.retainedEvents()) {
            long time = event.simulationTime().value();
            switch (event.payload()) {
                case RuntimeEventPayload.JobDispatched dispatched when dispatched.machineId().equals(machine) ->
                    started.put(new Key(dispatched.jobId(), dispatched.stepIndex()), time);
                case RuntimeEventPayload.JobStepCompleted completed when completed.machineId().equals(machine) -> {
                    long from = started.remove(new Key(completed.jobId(), completed.stepIndex()));
                    for (long tick = from; tick < Math.min(time, horizon); tick++) {
                        working[(int) tick]++;
                    }
                }
                default -> { }
            }
        }
        assertTrue(started.isEmpty(), "every dispatch completed by quiescence");
        return working;
    }

    private static long busyStateTicks(ExperimentEvidence evidence, MachineId machine, long horizon) {
        long ticks = 0;
        for (int tick = 0; tick < horizon; tick++) {
            if (resource(evidence.observation("t" + tick), machine).state() == MachineState.Busy) {
                ticks++;
            }
        }
        return ticks;
    }

    private static long sum(long[] values, int from, int to) {
        long total = 0;
        for (int index = from; index < to; index++) {
            total += values[index];
        }
        return total;
    }

    private static List<Long> toList(long[] values) {
        List<Long> list = new ArrayList<>();
        for (long value : values) {
            list.add(value);
        }
        return list;
    }

    private static ResourceObservation resource(RuntimeObservation observation, MachineId machine) {
        return observation.resources().stream().filter(r -> r.machineId().equals(machine)).findFirst().orElseThrow();
    }

    private static RuntimeObservation closing(ExperimentEvidence evidence) {
        return evidence.observation(ExperimentEvidence.CLOSING_LABEL);
    }
}
