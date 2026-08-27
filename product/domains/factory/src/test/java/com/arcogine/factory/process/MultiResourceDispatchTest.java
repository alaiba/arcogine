package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.factory.jobs.JobView;
import com.arcogine.factory.machines.Machine;
import com.arcogine.factory.machines.MachineStore;
import com.arcogine.factory.routing.Routing;
import com.arcogine.factory.routing.RoutingStep;
import com.arcogine.factory.routing.RoutingStore;
import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Gate 2 acceptance evidence: one operation step naming two equivalent eligible machines, with
 * runtime dispatch selecting between them deterministically. See {@code
 * FactoryHandler#selectMachine}.
 */
class MultiResourceDispatchTest {

    private static FactoryHandler twoEligibleMachinesHandler() {
        MachineStore machines = new MachineStore();
        machines.add(new Machine(new MachineId(1), "Mill A", 1, null, 0));
        machines.add(new Machine(new MachineId(2), "Mill B", 1, null, 0));

        RoutingStore routings = new RoutingStore();
        routings.addRouting(new Routing(
                1,
                "Widget Route",
                List.of(new RoutingStep(
                        1, "Milling", Set.of(new MachineId(1), new MachineId(2)), 5))));
        routings.addProductRouting(new ProductId(1), 1);

        return new FactoryHandler(machines, routings, List.of(new ProductId(1)));
    }

    private static Event orderEvent(long time) {
        return Event.of(
                new SimTime(time), new EventPayload.OrderCreation(new ProductId(1), 1, 10.0));
    }

    /** A two-step routing whose second step is the one with two eligible machines. */
    private static FactoryHandler secondStepMultiEligibleHandler() {
        MachineStore machines = new MachineStore();
        machines.add(new Machine(new MachineId(0), "Prep", 1, null, 0));
        machines.add(new Machine(new MachineId(1), "Mill A", 1, null, 0));
        machines.add(new Machine(new MachineId(2), "Mill B", 1, null, 0));

        RoutingStore routings = new RoutingStore();
        routings.addRouting(new Routing(
                1,
                "Widget Route",
                List.of(
                        new RoutingStep(1, "Prep", new MachineId(0), 5),
                        new RoutingStep(2, "Milling", Set.of(new MachineId(1), new MachineId(2)), 5))));
        routings.addProductRouting(new ProductId(1), 1);

        return new FactoryHandler(machines, routings, List.of(new ProductId(1)));
    }

    @Test
    void twoIndependentOrdersDispatchToBothEligibleMachinesConcurrently() {
        FactoryHandler h = twoEligibleMachinesHandler();
        Scheduler sched = new Scheduler();

        Event order1 = orderEvent(1);
        sched.schedule(order1);
        sched.nextEvent();
        h.handleEvent(order1, sched);

        Event order2 = orderEvent(1);
        h.handleEvent(order2, sched);

        List<JobView> jobs = h.jobsView().toList();
        assertEquals(2, jobs.size());
        Set<MachineId> assignedMachines = jobs.stream().map(JobView::currentMachine).collect(
                java.util.stream.Collectors.toSet());
        assertEquals(
                Set.of(new MachineId(1), new MachineId(2)),
                assignedMachines,
                "both eligible machines should be running work at once, not queued behind one");

        // Neither machine has a queued job: both jobs are actively running in parallel.
        assertEquals(0, h.machines.get(new MachineId(1)).queueDepth());
        assertEquals(0, h.machines.get(new MachineId(2)).queueDepth());
    }

    @Test
    void equalCandidatesResolveDeterministicallyToTheLowestMachineId() {
        FactoryHandler h1 = twoEligibleMachinesHandler();
        Scheduler sched1 = new Scheduler();
        Event order1a = orderEvent(1);
        sched1.schedule(order1a);
        sched1.nextEvent();
        h1.handleEvent(order1a, sched1);

        MachineId firstRun = h1.jobsView().findFirst().orElseThrow().currentMachine();

        FactoryHandler h2 = twoEligibleMachinesHandler();
        Scheduler sched2 = new Scheduler();
        Event order1b = orderEvent(1);
        sched2.schedule(order1b);
        sched2.nextEvent();
        h2.handleEvent(order1b, sched2);

        MachineId secondRun = h2.jobsView().findFirst().orElseThrow().currentMachine();

        assertEquals(new MachineId(1), firstRun, "a single job among equal idle candidates ties to the lowest MachineId");
        assertEquals(firstRun, secondRun, "identical inputs must resolve to the same machine on every run");
    }

    @Test
    void offlineEligibleMachineIsExcludedFromSelection() {
        FactoryHandler h = twoEligibleMachinesHandler();
        Scheduler sched = new Scheduler();

        h.machines.getMut(new MachineId(1)).setAvailability(false);

        Event order = orderEvent(1);
        sched.schedule(order);
        sched.nextEvent();
        h.handleEvent(order, sched);

        JobView job = h.jobsView().findFirst().orElseThrow();
        assertEquals(
                new MachineId(2),
                job.currentMachine(),
                "an offline eligible machine must not be selected for new dispatch");
        assertNotEquals(new MachineId(1), job.currentMachine());
    }

    /**
     * Reproduces the stranding bug a machine-local queue would have: a job that started waiting
     * because M1 was offline and M2 was busy must not stay pinned to M2's queue forever -- once
     * M1 comes back online, it must pick up that waiting job immediately, even though M1 had
     * nothing to do with the job when it first started waiting. See {@code
     * FactoryHandler#pendingMultiEligible}/{@code tryDispatchPendingMultiEligible}.
     */
    @Test
    void machineComingBackOnlineDispatchesWorkThatWasStrandedWaitingOnAnotherMachine() {
        FactoryHandler h = twoEligibleMachinesHandler();
        Scheduler sched = new Scheduler();

        h.machines.getMut(new MachineId(1)).setAvailability(false);

        // Order A: only M2 is online, so it starts immediately on M2, leaving M2 busy.
        Event orderA = orderEvent(1);
        sched.schedule(orderA);
        sched.nextEvent();
        h.handleEvent(orderA, sched);
        assertEquals(new MachineId(2), h.jobsView().findFirst().orElseThrow().currentMachine());

        // Order B: M1 is offline and M2 is busy, so neither eligible machine can accept it --
        // it must wait, not be forced onto one specific machine's local queue.
        Event orderB = orderEvent(1);
        h.handleEvent(orderB, sched);
        JobView jobB = h.jobsView()
                .filter(j -> j.currentMachine() == null)
                .findFirst()
                .orElseThrow(() -> new AssertionError("job B must be waiting, not yet assigned a machine"));

        // M1 recovers. Even though job B never touched M1 before, M1 coming online must dispatch
        // it -- proving the wait is against the whole eligible set, not pinned to M2.
        Event online = Event.of(
                new SimTime(2), new EventPayload.MachineAvailabilityChange(new MachineId(1), true));
        h.handleEvent(online, sched);

        JobView jobBAfter = h.job(jobB.id());
        assertEquals(
                new MachineId(1),
                jobBAfter.currentMachine(),
                "a job stranded waiting for either eligible machine must dispatch to whichever "
                        + "one actually comes available, not remain stuck");
    }

    /**
     * The multi-eligible pending path is reached from {@code handleTaskEnd} advancing a job to
     * its *next* routing step too, not only from a job's very first step in {@code submitOrder}.
     */
    @Test
    void nextRoutingStepPendingOnBothEligibleMachinesDispatchesOnceOneFreesUp() {
        FactoryHandler h = secondStepMultiEligibleHandler();
        Scheduler sched = new Scheduler();

        // Saturate both of step two's eligible machines before the job ever reaches that step.
        h.machines.getMut(new MachineId(1)).startJob(new JobId(901));
        h.machines.getMut(new MachineId(2)).startJob(new JobId(902));

        Event order = orderEvent(1);
        sched.schedule(order);
        sched.nextEvent();
        h.handleEvent(order, sched);
        assertEquals(new MachineId(0), h.jobsView().findFirst().orElseThrow().currentMachine());

        // Step one (Prep) completes; step two's eligible machines are both busy, so the job must
        // wait rather than being pinned to one of them.
        Event stepOneEnd = sched.nextEvent().orElseThrow();
        h.handleEvent(stepOneEnd, sched);
        JobId jobId = h.jobsView().findFirst().orElseThrow().id();
        assertEquals(null, h.job(jobId).currentMachine(), "step two must be waiting, not yet dispatched");

        // Mill B frees up; re-announcing it online must dispatch the waiting job onto it.
        h.machines.getMut(new MachineId(2)).completeJob(new JobId(902));
        Event millBOnline = Event.of(
                new SimTime(20), new EventPayload.MachineAvailabilityChange(new MachineId(2), true));
        h.handleEvent(millBOnline, sched);

        assertEquals(
                new MachineId(2),
                h.job(jobId).currentMachine(),
                "the pending step-two job must dispatch once one of its eligible machines frees up");
    }

    /**
     * An unrelated availability event must not force-dispatch multi-eligible pending work while
     * none of its eligible machines can actually accept it yet.
     */
    @Test
    void pendingWorkStaysWaitingWhileNoEligibleMachineCanAcceptItYet() {
        FactoryHandler h = twoEligibleMachinesHandler();
        Scheduler sched = new Scheduler();

        h.machines.getMut(new MachineId(1)).setAvailability(false);

        Event orderA = orderEvent(1);
        sched.schedule(orderA);
        sched.nextEvent();
        h.handleEvent(orderA, sched);
        assertEquals(new MachineId(2), h.jobsView().findFirst().orElseThrow().currentMachine());

        Event orderB = orderEvent(1);
        h.handleEvent(orderB, sched);
        JobId jobBId = h.jobsView()
                .filter(j -> j.currentMachine() == null)
                .findFirst()
                .orElseThrow()
                .id();

        // Re-announcing Mill B (M2) as online -- it is already online and still busy -- must not
        // dispatch job B: neither eligible machine can accept it yet, so it stays pending.
        Event redundantOnline = Event.of(
                new SimTime(2), new EventPayload.MachineAvailabilityChange(new MachineId(2), true));
        h.handleEvent(redundantOnline, sched);

        assertEquals(
                null,
                h.job(jobBId).currentMachine(),
                "pending work must not be dispatched while every eligible machine is still unavailable");
    }
}
