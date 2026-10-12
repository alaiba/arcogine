package com.arcogine.research.experiment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.process.ResourceObservation;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.research.experiment.ExperimentFixture.WindowIntent;
import com.arcogine.research.experiment.LinearRoutingFamily.Resource;
import com.arcogine.research.experiment.LinearRoutingFamily.Step;
import com.arcogine.types.MachineState;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Current-main ports of the two independent-review probes behind the reconciled diagnostic
 * guardrails: the offline-and-queued state that needs a third classification or refusal, and the
 * non-composition of single-removal "needed" results. Expectations were derived by hand from Engine
 * semantics before running, and are written beside each assertion.
 */
class GameDiagnosticCurrentMainProbes {

    private static final double PRICE = LinearRoutingFamily.UNIT_PRICE;

    /**
     * One step BAKE 4; one Oven with two slots; four loaves submitted while the Oven is offline, then
     * the Oven is brought online before any advancement.
     *
     * <p>Hand derivation. Every eligible machine is offline, so selection keeps the full eligible set
     * and each loaf waits in the Oven's own queue (single-eligible step): four queued. Coming online is
     * one cascade trigger, and the per-machine stage starts at most one queued job per trigger: one
     * active, three queued, one slot idle. Each later completion is one trigger that frees one slot and
     * starts one loaf, so the second slot is never used again: completions at 4, 8, 12 and 16. At tick
     * 6 one loaf is active and two are queued. Always online, two loaves start at once and the two
     * completions at 4 each start one more: completion at 8.
     */
    @Test
    void anAvailabilityChangeLeavesASlotIdleWhileWorkQueuesForThatMachine() {
        LinearRoutingFamily design = new LinearRoutingFamily(
                List.of(new Step("BAKE", 4)), List.of(Resource.of("Oven", 2, "BAKE")));
        List<ExperimentStep> script = new ArrayList<>();
        script.add(ExperimentStep.setAvailability(design.resourceId("Oven"), false));
        script.add(ExperimentStep.submit(LinearRoutingFamily.PRODUCT, 4, PRICE));
        script.add(ExperimentStep.setAvailability(design.resourceId("Oven"), true));
        script.add(ExperimentStep.observe("online"));
        script.add(ExperimentStep.advanceUntil(6));
        script.add(ExperimentStep.observe("t6"));
        script.add(ExperimentStep.advanceToQuiescence(100));
        script.add(ExperimentStep.captureEvents("whole-run"));
        ExperimentEvidence evidence = ExperimentRunner.run(new ExperimentFixture(
                "probe-offline-and-queued", design.model(), script, WindowIntent.COMPLETE_RUN, List.of()));

        RuntimeObservation online = evidence.observation("online");
        ResourceObservation oven = online.resources().getFirst();
        assertEquals(MachineState.Busy, oven.state(), "machine-level Busy with one of two slots idle");
        assertEquals(2, oven.concurrency());
        assertEquals(1, oven.activeJobIds().size());
        assertEquals(3, oven.queueDepth(), "work waits in this machine's own queue while a slot is idle");
        assertTrue(online.pendingWork().isEmpty());

        ResourceObservation ovenAtSix = evidence.observation("t6").resources().getFirst();
        assertEquals(1, ovenAtSix.activeJobIds().size());
        assertEquals(2, ovenAtSix.queueDepth());

        assertEquals(16, completion(evidence));
        assertEquals(8, completion(alwaysOnline(design, 4)));
    }

    /**
     * MIX 2, BAKE 3, PACK 4; Mixer, Oven and Packers 1 to 3, each with one slot; eight loaves
     * released at once.
     *
     * <p>Hand derivation. The Mixer finishes loaves at 2, 4, ..., 16; the Oven, slower at 3 ticks,
     * finishes them at 5, 8, ..., 26. One packer packs back to back from 5: 5 + 8 x 4 = 37. With two
     * packers every 3-tick arrival finds a packer free (each is free again 4 ticks after it starts,
     * and alternate arrivals are 6 apart), so the last loaf packs from 26 and completes at 30; three
     * packers cannot do better. So removing Packer 2 alone or Packer 3 alone changes nothing (30),
     * while removing both gives 37.
     */
    @Test
    void singleRemovalResultsDoNotComposeIntoAJointRemoval() {
        Step mix = new Step("MIX", 2);
        Step bake = new Step("BAKE", 3);
        Step pack = new Step("PACK", 4);
        Resource mixer = Resource.of("Mixer", 1, "MIX");
        Resource oven = Resource.of("Oven", 1, "BAKE");
        Resource packer1 = Resource.of("Packer 1", 1, "PACK");
        Resource packer2 = Resource.of("Packer 2", 1, "PACK");
        Resource packer3 = Resource.of("Packer 3", 1, "PACK");
        List<Step> steps = List.of(mix, bake, pack);

        assertEquals(30, completion(alwaysOnline("three-packers",
                new LinearRoutingFamily(steps, List.of(mixer, oven, packer1, packer2, packer3)), 8)));
        assertEquals(30, completion(alwaysOnline("without-packer-3",
                new LinearRoutingFamily(steps, List.of(mixer, oven, packer1, packer2)), 8)));
        assertEquals(30, completion(alwaysOnline("without-packer-2",
                new LinearRoutingFamily(steps, List.of(mixer, oven, packer1, packer3)), 8)));
        assertEquals(37, completion(alwaysOnline("without-both",
                new LinearRoutingFamily(steps, List.of(mixer, oven, packer1)), 8)));
    }

    private static ExperimentEvidence alwaysOnline(LinearRoutingFamily design, long quantity) {
        return alwaysOnline("always-online", design, quantity);
    }

    private static ExperimentEvidence alwaysOnline(String id, LinearRoutingFamily design, long quantity) {
        List<ExperimentStep> script = List.of(
                ExperimentStep.submit(LinearRoutingFamily.PRODUCT, quantity, PRICE),
                ExperimentStep.advanceToQuiescence(1_000),
                ExperimentStep.captureEvents("whole-run"));
        return ExperimentRunner.run(new ExperimentFixture(
                "probe-" + id, design.model(), script, WindowIntent.COMPLETE_RUN, List.of()));
    }

    private static long completion(ExperimentEvidence evidence) {
        return evidence.observation(ExperimentEvidence.CLOSING_LABEL).orders().getFirst().completedAt().value();
    }
}
