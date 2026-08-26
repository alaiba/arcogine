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
}
