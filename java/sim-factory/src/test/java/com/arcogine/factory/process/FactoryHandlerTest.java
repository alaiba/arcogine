package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

/** Ported from the inline #[cfg(test)] module in crates/sim-factory/src/process.rs. */
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

    @Test
    void newInitializesCorrectly() {
        FactoryHandler h = oneMachineOneProduct();
        assertEquals(1, h.machines.iter().count());
        assertEquals(1, h.productIds.size());
        assertEquals(0.0, h.completedSalesValue);
        assertEquals(0, h.completedSales);
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

        assertEquals(1, h.completedSales);
        assertTrue(h.avgLeadTime() > 0.0);
    }

    @Test
    void throughputRateDivision() {
        FactoryHandler h = oneMachineOneProduct();
        h.completedSales = 10;
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

        assertEquals(1, h.jobs.allJobs().count());
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
        assertEquals(1, h.completedSales);
        assertEquals(0, h.machines.get(new MachineId(1)).queueDepth());
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
        assertEquals(0, h.completedSales, "should not be complete after step 1");

        Event te2 = sched.nextEvent().orElseThrow();
        h.handleEvent(te2, sched);
        assertEquals(1, h.completedSales, "should be complete after step 2");
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

        Event taskEnd = sched.nextEvent().orElseThrow();
        h.handleEvent(taskEnd, sched);

        assertEquals(30.0, h.completedSalesValue);
    }

    @Test
    void jobOrderValueIsFixedAtOrderCreationPriceRegardlessOfLaterOrders() {
        FactoryHandler h = oneMachineOneProduct();
        Scheduler sched = new Scheduler();

        // Order A is created while the market price is $10.
        Event orderA = orderEvent(1, 3, 10.0);
        sched.schedule(orderA);
        sched.nextEvent();
        h.handleEvent(orderA, sched);

        var jobA = h.jobs.allJobs().findFirst().orElseThrow();
        assertEquals(10.0, jobA.unitPrice());
        assertEquals(30.0, jobA.orderValue());

        // The market price changes to $999 before job A completes. Job A's own price must not move.
        Event taskEndA = sched.nextEvent().orElseThrow();
        h.handleEvent(taskEndA, sched);

        assertEquals(10.0, jobA.unitPrice());
        assertEquals(
                30.0, jobA.orderValue(), "completed order's value must not track later market price changes");
        assertEquals(30.0, h.completedSalesValue);
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
        h.handleEvent(sched.nextEvent().orElseThrow(), sched);
        assertEquals(20.0, h.completedSalesValue);

        // Market price rises to $50 before order B is created.
        Event orderB = orderEvent(sched.currentTime().ticks(), 2, 50.0);
        sched.schedule(orderB);
        sched.nextEvent();
        h.handleEvent(orderB, sched);
        h.handleEvent(sched.nextEvent().orElseThrow(), sched);

        assertEquals(20.0 + 100.0, h.completedSalesValue, "each order contributes its own creation-time price");
    }
}
