package com.arcogine.api.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.arcogine.agents.AgentObservation;
import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.economy.pricing.PricingState;
import com.arcogine.factory.machines.Machine;
import com.arcogine.factory.machines.MachineStore;
import com.arcogine.factory.process.FactoryHandler;
import com.arcogine.factory.routing.Routing;
import com.arcogine.factory.routing.RoutingStep;
import com.arcogine.factory.routing.RoutingStore;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link AgentObservationProjector}, independent of the full event pipeline. */
class AgentObservationProjectorTest {

    private static FactoryHandler oneMachineOneProduct() {
        MachineStore machines = new MachineStore();
        machines.add(new Machine(new MachineId(1), "Mill", 1, null, 0));

        RoutingStore routings = new RoutingStore();
        routings.addRouting(
                new Routing(1, "Widget Route", List.of(new RoutingStep(1, "Milling", new MachineId(1), 5))));
        routings.addProductRouting(new ProductId(1), 1);

        return new FactoryHandler(machines, routings, List.of(new ProductId(1)));
    }

    @Test
    void projectsAllFieldsFromFactoryAndPricingState() {
        FactoryHandler factory = oneMachineOneProduct();
        PricingState pricing = new PricingState(12.0);
        Scheduler sched = new Scheduler();

        Event order = Event.of(new SimTime(1), new EventPayload.OrderCreation(new ProductId(1), 3, 12.0));
        sched.schedule(order);
        sched.nextEvent();
        factory.handleEvent(order, sched);

        // The job's routing repeats once per unit of quantity (3), so three TaskEnd events are
        // needed to complete it.
        for (int i = 0; i < 3; i++) {
            Event taskEnd = sched.nextEvent().orElseThrow();
            factory.handleEvent(taskEnd, sched);
        }

        AgentObservation observation = AgentObservationProjector.project(factory, pricing, 10L);

        assertEquals((int) factory.backlog(), observation.backlog());
        assertEquals(factory.avgLeadTime(), observation.avgLeadTime());
        assertEquals(factory.completedSalesValue(), observation.completedSalesValue());
        assertEquals(factory.completedSales(), observation.completedSales());
        assertEquals(pricing.offerPrice(), observation.offerPrice());
        assertEquals(factory.throughput(10L), observation.throughput());

        // Concrete values, not just self-consistency with the source objects.
        assertEquals(0, observation.backlog(), "job completed, so backlog should be empty");
        assertEquals(1, observation.completedSales());
        assertEquals(36.0, observation.completedSalesValue(), "completedSalesValue: 3 units at $12");
        assertEquals(12.0, observation.offerPrice());
    }

    @Test
    void elapsedTicksIsFlooredAtOneToAvoidDivisionByZero() {
        FactoryHandler factory = oneMachineOneProduct();
        PricingState pricing = new PricingState(10.0);

        AgentObservation zeroElapsed = AgentObservationProjector.project(factory, pricing, 0L);
        AgentObservation oneElapsed = AgentObservationProjector.project(factory, pricing, 1L);

        assertEquals(oneElapsed.throughput(), zeroElapsed.throughput(), "elapsedTicks=0 must be treated as 1");
    }
}
