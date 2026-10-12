package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.factory.machines.Machine;
import com.arcogine.factory.machines.MachineStore;
import com.arcogine.factory.routing.Routing;
import com.arcogine.factory.routing.RoutingStep;
import com.arcogine.factory.routing.RoutingStore;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class FactoryHandlerTest {

    private static FactoryHandler oneMachineOneProduct() {
        MachineStore machines = new MachineStore();
        machines.add(new Machine(new MachineId(1), "Mill", 1, null, 0));

        RoutingStore routings = new RoutingStore();
        routings.addRouting(
                new Routing(
                        1,
                        "Widget Route",
                        List.of(new RoutingStep(1, "Milling", new MachineId(1), 5))));
        routings.addProductRouting(new ProductId(1), 1);

        return new FactoryHandler(machines, routings, List.of(new ProductId(1)));
    }

    private static FactoryHandler twoStepHandler() {
        MachineStore machines = new MachineStore();
        machines.add(new Machine(new MachineId(1), "Mill", 1, null, 0));
        machines.add(new Machine(new MachineId(2), "Drill", 1, null, 0));

        RoutingStore routings = new RoutingStore();
        routings.addRouting(
                new Routing(
                        1,
                        "Widget Route",
                        List.of(
                                new RoutingStep(1, "Milling", new MachineId(1), 5),
                                new RoutingStep(2, "Drilling", new MachineId(2), 3))));
        routings.addProductRouting(new ProductId(1), 1);

        return new FactoryHandler(machines, routings, List.of(new ProductId(1)));
    }

    private static final double DEFAULT_UNIT_PRICE = 10.0;

    private static Event orderEvent(long time, long quantity) {
        return orderEvent(time, quantity, DEFAULT_UNIT_PRICE);
    }

    private static Event orderEvent(long time, long quantity, double unitPrice) {
        return Event.of(
                new SimTime(time),
                new EventPayload.OrderCreation(new ProductId(1), quantity, unitPrice));
    }

    /**
     * Child jobs each traverse once, so completing an order requires driving one TaskEnd per unit
     * (for a single-step routing). Drains exactly {@code quantity} events -- each one a TaskEnd,
     * the only event kind the handler schedules -- returning the final one, which completes the
     * order and schedules nothing further.
     */
    private static Event driveToCompletion(FactoryHandler h, Scheduler sched, long quantity) {
        Event taskEnd = null;
        for (long i = 0; i < quantity; i++) {
            taskEnd = sched.nextEvent().orElseThrow();
            assertInstanceOf(EventPayload.TaskEnd.class, taskEnd.payload());
            h.handleEvent(taskEnd, sched);
        }
        return taskEnd;
    }

    @Test
    void newInitializesCorrectly() {
        FactoryHandler h = oneMachineOneProduct();
        assertEquals(1, h.machines.iter().count());
        assertEquals(1, h.productIds.size());
        assertEquals(0.0, h.completedSalesValue());
        assertEquals(0, h.completedSales());
    }

    @Test
    void emptyRoutingIsRefusedBeforeCreatingOrderOrJob() {
        RoutingStore routings = new RoutingStore();
        routings.addRouting(new Routing(1, "Empty", List.of()));
        routings.addProductRouting(new ProductId(1), 1);
        FactoryHandler handler = new FactoryHandler(new MachineStore(), routings, List.of(new ProductId(1)));
        Scheduler scheduler = new Scheduler();

        assertThrows(com.arcogine.types.SimError.InvalidStateTransition.class,
                () -> handler.handleEvent(orderEvent(0, 1), scheduler));
        assertEquals(0, handler.ordersView().count());
        assertEquals(0, handler.jobsView().count());
        assertTrue(scheduler.isEmpty());
    }

    @Test
    void machineLookupRejectsAnUnknownIdentity() {
        FactoryHandler handler = oneMachineOneProduct();
        assertThrows(com.arcogine.types.SimError.UnknownId.class,
                () -> handler.machines.get(new MachineId(99)));
    }

    @Test
    void backlogCountsActiveJobs() {
        FactoryHandler h = oneMachineOneProduct();
        Scheduler sched = new Scheduler();

        Event order = orderEvent(1, 1);
        sched.schedule(order);
        sched.nextEvent();
        h.handleEvent(order, sched);
        assertEquals(1, h.backlog());
    }

    @Test
    void avgLeadTimeZeroWhenNoCompleted() {
        FactoryHandler h = oneMachineOneProduct();
        assertEquals(0.0, h.avgLeadTime());
    }

    @Test
    void avgLeadTimeCorrectForCompletedJobs() {
        FactoryHandler h = oneMachineOneProduct();
        Scheduler sched = new Scheduler();

        Event order = orderEvent(1, 1);
        sched.schedule(order);
        sched.nextEvent();
        h.handleEvent(order, sched);

        Event taskEnd = sched.nextEvent().orElseThrow();
        h.handleEvent(taskEnd, sched);

        assertEquals(1, h.completedSales());
        assertTrue(h.avgLeadTime() > 0.0);
    }

    @Test
    void throughputRateDivision() {
        FactoryHandler h = oneMachineOneProduct();
        Scheduler sched = new Scheduler();

        for (int i = 0; i < 10; i++) {
            Event order = orderEvent(sched.currentTime().ticks(), 1);
            sched.schedule(order);
            sched.nextEvent();
            h.handleEvent(order, sched);
            h.handleEvent(sched.nextEvent().orElseThrow(), sched); // TaskEnd completes the order
            assertTrue(sched.isEmpty(), "order completion must leave nothing queued");
        }

        assertEquals(10, h.completedSales());
        assertEquals(0.1, h.throughput(100));
    }

    @Test
    void throughputZeroWhenZeroTicks() {
        FactoryHandler h = oneMachineOneProduct();
        assertEquals(0.0, h.throughput(0));
    }

    @Test
    void orderCreationCreatesAndDispatchesJob() {
        FactoryHandler h = oneMachineOneProduct();
        Scheduler sched = new Scheduler();

        Event order = orderEvent(1, 2);
        sched.schedule(order);
        sched.nextEvent();
        h.handleEvent(order, sched);

        assertEquals(2, h.jobs.allJobs().count());
        assertFalse(sched.isEmpty(), "should have scheduled TaskEnd");
    }

    @Test
    void orderCreationEnqueuesWhenMachineFull() {
        FactoryHandler h = oneMachineOneProduct();
        Scheduler sched = new Scheduler();

        Event o1 = orderEvent(1, 1);
        sched.schedule(o1);
        sched.nextEvent();
        h.handleEvent(o1, sched);

        Event o2 = orderEvent(1, 1);
        h.handleEvent(o2, sched);

        assertEquals(1, h.machines.get(new MachineId(1)).queueDepth());
    }

    @Test
    void taskEndCompletesJobAndDequeuesNext() {
        FactoryHandler h = oneMachineOneProduct();
        Scheduler sched = new Scheduler();

        Event o1 = orderEvent(1, 1);
        sched.schedule(o1);
        sched.nextEvent();
        h.handleEvent(o1, sched);

        Event o2 = orderEvent(1, 1);
        h.handleEvent(o2, sched);
        assertEquals(1, h.machines.get(new MachineId(1)).queueDepth());

        Event taskEnd = sched.nextEvent().orElseThrow();
        h.handleEvent(taskEnd, sched);
        assertEquals(1, h.completedSales());
        assertEquals(0, h.machines.get(new MachineId(1)).queueDepth());
    }

    @Test
    void nextStepEnqueuesWhenItsOnlyEligibleMachineIsBusy() {
        FactoryHandler h = twoStepHandler();
        Scheduler sched = new Scheduler();

        // Saturate Drilling's only eligible machine before the job ever reaches that step.
        h.machines.getMut(new MachineId(2)).startJob(new com.arcogine.types.JobId(999));

        Event order = orderEvent(1, 1);
        sched.schedule(order);
        sched.nextEvent();
        h.handleEvent(order, sched);

        Event te1 = sched.nextEvent().orElseThrow();
        h.handleEvent(te1, sched);

        assertEquals(
                1,
                h.machines.get(new MachineId(2)).queueDepth(),
                "a job whose next step's only eligible machine is busy must be enqueued on it");
    }

    @Test
    void multiStepRoutingAdvancesToNextStep() {
        FactoryHandler h = twoStepHandler();
        Scheduler sched = new Scheduler();

        Event order = orderEvent(1, 1);
        sched.schedule(order);
        sched.nextEvent();
        h.handleEvent(order, sched);

        Event te1 = sched.nextEvent().orElseThrow();
        h.handleEvent(te1, sched);
        assertEquals(0, h.completedSales(), "should not be complete after step 1");

        Event te2 = sched.nextEvent().orElseThrow();
        h.handleEvent(te2, sched);
        assertEquals(1, h.completedSales(), "should be complete after step 2");
        assertTrue(
                sched.isEmpty(),
                "completing the final step completes the order without scheduling any follow-up event");
    }

    @Test
    void intermediateStepSchedulesOnlyTheNextStepCompletion() {
        FactoryHandler h = twoStepHandler();
        Scheduler sched = new Scheduler();

        Event order = orderEvent(1, 1);
        sched.schedule(order);
        sched.nextEvent();
        h.handleEvent(order, sched);

        Event te1 = sched.nextEvent().orElseThrow();
        h.handleEvent(te1, sched);

        Event next = sched.nextEvent().orElseThrow();
        assertEquals(
                new EventPayload.TaskEnd(h.jobsView().findFirst().orElseThrow().id(), new MachineId(2), 1),
                next.payload(),
                "completing step 1 of 2 should schedule exactly the next step's completion");
        assertTrue(sched.isEmpty(), "nothing else may be queued before the order is fully complete");
        assertFalse(h.orderExecution(h.jobsView().findFirst().orElseThrow().orderId()).complete());
    }

    @Test
    void finalChildCompletionCompletesTheOrderWithoutSchedulingAMarker() {
        FactoryHandler h = oneMachineOneProduct();
        Scheduler sched = new Scheduler();

        Event order = orderEvent(1, 3, 12.0);
        sched.schedule(order);
        sched.nextEvent();
        h.handleEvent(order, sched);

        var orderId = h.jobs.allJobs().findFirst().orElseThrow().orderId();

        // Three unit children on one concurrency-1 machine: completions at 6, 11 and 16.
        Event last = driveToCompletion(h, sched, 2);
        assertFalse(h.orderExecution(orderId).complete(), "two of three children is a partial completion");
        assertEquals(1, sched.size(), "only the third child's step completion is queued");

        last = driveToCompletion(h, sched, 1);
        assertEquals(SimTime.of(16), last.time());
        var execution = h.orderExecution(orderId);
        assertTrue(execution.complete());
        assertEquals(3L, execution.completedQuantity());
        assertEquals(SimTime.of(16), execution.completedAt());
        assertEquals(1, h.completedSales(), "the final child completion completes the order exactly once");
        assertEquals(36.0, h.completedSalesValue());
        assertTrue(sched.isEmpty(), "order completion is a transition, not a further scheduled event");
    }

    @Test
    void completedSalesValueAndCountEqualTheSumAndCountOfCompletedJobs() {
        FactoryHandler h = oneMachineOneProduct();
        Scheduler sched = new Scheduler();

        Event order1 = orderEvent(1, 2, 10.0);
        sched.schedule(order1);
        sched.nextEvent();
        h.handleEvent(order1, sched);
        driveToCompletion(h, sched, 2);
        assertTrue(sched.isEmpty(), "order1's completion leaves nothing queued ahead of order2");

        Event order2 = orderEvent(sched.currentTime().ticks(), 3, 20.0);
        sched.schedule(order2);
        sched.nextEvent();
        h.handleEvent(order2, sched);
        driveToCompletion(h, sched, 3);

        double expectedValue = h.ordersView().mapToDouble(com.arcogine.factory.orders.Order::orderValue).sum();
        long expectedCount = h.ordersView().count();

        assertEquals(
                expectedValue,
                h.completedSalesValue(),
                "completedSalesValue is a cached aggregate; it must equal Sum(orderValue) over completed jobs");
        assertEquals(expectedCount, h.completedSales());
    }

    @Test
    void machineAvailabilityDispatchesQueuedOnOnline() {
        FactoryHandler h = oneMachineOneProduct();
        Scheduler sched = new Scheduler();

        h.machines.getMut(new MachineId(1)).setAvailability(false);

        Event order = orderEvent(1, 1);
        sched.schedule(order);
        sched.nextEvent();
        h.handleEvent(order, sched);
        assertEquals(1, h.machines.get(new MachineId(1)).queueDepth());

        Event online =
                Event.of(
                        new SimTime(2),
                        new EventPayload.MachineAvailabilityChange(new MachineId(1), true));
        sched.schedule(online);
        sched.nextEvent();
        h.handleEvent(online, sched);
        assertEquals(
                0,
                h.machines.get(new MachineId(1)).queueDepth(),
                "queued job should be dispatched");
    }

    @Test
    void completedSalesValueUsesOrderCreationPrice() {
        FactoryHandler h = oneMachineOneProduct();
        Scheduler sched = new Scheduler();

        Event order = orderEvent(1, 3, 10.0);
        sched.schedule(order);
        sched.nextEvent();
        h.handleEvent(order, sched);

        driveToCompletion(h, sched, 3);

        assertEquals(30.0, h.completedSalesValue());
    }

    @Test
    void completedSalesValueSumsEachOrdersOwnCreationTimePrice() {
        FactoryHandler h = oneMachineOneProduct();
        Scheduler sched = new Scheduler();

        // Order A created at $10, completes before order B is even created.
        Event orderA = orderEvent(1, 2, 10.0);
        sched.schedule(orderA);
        sched.nextEvent();
        h.handleEvent(orderA, sched);
        driveToCompletion(h, sched, 2);
        assertEquals(20.0, h.completedSalesValue());
        assertTrue(sched.isEmpty(), "order A's completion leaves nothing queued");

        // Order B is accepted later at its own, different agreed unit price.
        Event orderB = orderEvent(sched.currentTime().ticks(), 2, 50.0);
        sched.schedule(orderB);
        sched.nextEvent();
        h.handleEvent(orderB, sched);
        driveToCompletion(h, sched, 2);

        assertEquals(20.0 + 100.0, h.completedSalesValue(), "each order contributes its own creation-time price");
    }
}
