package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

class OrderIntentSeparationTest {

    private static FactoryHandler handler() {
        MachineStore machines = new MachineStore();
        machines.add(new Machine(new MachineId(1), "Mill", 1, null, 0));

        RoutingStore routings = new RoutingStore();
        routings.addRouting(new Routing(
                1,
                "Widget Route",
                List.of(new RoutingStep(1, "Milling", new MachineId(1), 5))));
        routings.addProductRouting(new ProductId(1), 1);

        return new FactoryHandler(machines, routings, List.of(new ProductId(1)));
    }

    @Test
    void orderCreationPersistsIntentBeforeCreatingUnitExecutionJobs() {
        FactoryHandler handler = handler();
        Scheduler scheduler = new Scheduler();
        Event event = Event.of(
                new SimTime(4),
                new EventPayload.OrderCreation(new ProductId(1), 3, 12.0));

        handler.handleEvent(event, scheduler);

        var order = handler.ordersView().findFirst().orElseThrow();
        var job = handler.jobsView().findFirst().orElseThrow();

        assertEquals(1L, handler.ordersView().count());
        assertEquals(3L, handler.jobsView().count());
        assertEquals(order.id(), job.orderId());
        assertEquals(List.of(0L, 1L, 2L), handler.jobsView().map(j -> j.ordinalWithinOrder()).toList());
        assertEquals(new ProductId(1), order.productId());
        assertEquals(3L, order.quantity());
        assertEquals(12.0, order.unitPrice());
        assertEquals(new SimTime(4), order.createdAt());
    }

    @Test
    void completedJobUsesReferencedOrderForCommercialFacts() {
        FactoryHandler handler = handler();
        Scheduler scheduler = new Scheduler();
        Event event = Event.of(
                new SimTime(1),
                new EventPayload.OrderCreation(new ProductId(1), 3, 12.0));

        scheduler.schedule(event);
        scheduler.nextEvent();
        handler.handleEvent(event, scheduler);

        var job = handler.jobsView().findFirst().orElseThrow();
        var order = handler.order(job.orderId());
        EventPayload.OrderCompleted completed = null;
        java.util.Optional<Event> pending;
        while ((pending = scheduler.nextEvent()).isPresent()) {
            Event next = pending.orElseThrow();
            handler.handleEvent(next, scheduler);
            if (next.payload() instanceof EventPayload.OrderCompleted payload) completed = payload;
        }

        assertEquals(order.orderValue(), handler.completedSalesValue());
        var payload = completed;
        assertEquals(order.productId(), payload.productId());
        assertEquals(order.quantity(), payload.quantity());
        assertEquals(order.unitPrice(), payload.unitPrice());
    }
}
