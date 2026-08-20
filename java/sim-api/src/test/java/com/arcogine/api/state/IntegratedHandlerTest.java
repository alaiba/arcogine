package com.arcogine.api.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.agents.SalesAgent;
import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.economy.demand.DemandModel;
import com.arcogine.economy.pricing.PricingState;
import com.arcogine.factory.machines.Machine;
import com.arcogine.factory.machines.MachineStore;
import com.arcogine.factory.process.FactoryHandler;
import com.arcogine.factory.routing.Routing;
import com.arcogine.factory.routing.RoutingStep;
import com.arcogine.factory.routing.RoutingStore;
import com.arcogine.finance.process.FinanceHandler;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimTime;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * Covers the ToggleAgent consistency fix: agentEnabled is now mutated only in response to an
 * {@link EventPayload.AgentEnabledChanged} event, the same way ChangePrice/ChangeMachine
 * commands become PriceChange/MachineAvailabilityChange events, rather than via a direct setter
 * bypassing the event system.
 */
class IntegratedHandlerTest {

    private static IntegratedHandler buildHandler(boolean agentEnabled) {
        MachineStore machines = new MachineStore();
        machines.add(new Machine(new MachineId(1), "Mill", 1, null, 0));

        RoutingStore routings = new RoutingStore();
        routings.addRouting(
                new Routing(1, "Widget Route", List.of(new RoutingStep(1, "Milling", new MachineId(1), 5))));
        routings.addProductRouting(new ProductId(1), 1);

        FactoryHandler factory = new FactoryHandler(machines, routings, List.of(new ProductId(1)));
        PricingState pricing = new PricingState(10.0);
        DemandModel demand = new DemandModel(
                5.0, 0.5, 0.0, pricing::offerPrice, factory::avgLeadTime, List.of(new ProductId(1)), new Random(1));
        SalesAgent agent = SalesAgent.withDefaultConfig();

        return new IntegratedHandler(factory, demand, pricing, new FinanceHandler(), agent, agentEnabled);
    }

    @Test
    void agentEnabledChangedEventTogglesAgentEnabled() {
        IntegratedHandler handler = buildHandler(false);
        Scheduler sched = new Scheduler();
        assertFalse(handler.agentEnabled());

        Event enable = Event.of(SimTime.ZERO, new EventPayload.AgentEnabledChanged(true));
        handler.handleEvent(enable, sched);
        assertTrue(handler.agentEnabled());

        Event disable = Event.of(SimTime.ZERO, new EventPayload.AgentEnabledChanged(false));
        handler.handleEvent(disable, sched);
        assertFalse(handler.agentEnabled());
    }

    @Test
    void agentOnlyDispatchedOnAgentEvaluationWhenEnabled() {
        IntegratedHandler handler = buildHandler(false);
        Scheduler sched = new Scheduler();

        Event eval = Event.of(SimTime.of(10), EventPayload.AgentEvaluation.INSTANCE);
        sched.schedule(eval);
        sched.nextEvent();
        handler.handleEvent(eval, sched);
        assertEquals(0L, handler.agent().interventions(), "disabled agent must not be dispatched");

        Event enable = Event.of(SimTime.of(10), new EventPayload.AgentEnabledChanged(true));
        handler.handleEvent(enable, sched);
        assertTrue(handler.agentEnabled());
        // Enabling alone doesn't force an intervention (that depends on backlog), but confirms
        // the flag flip took effect via the event rather than any direct setter.
    }
}
