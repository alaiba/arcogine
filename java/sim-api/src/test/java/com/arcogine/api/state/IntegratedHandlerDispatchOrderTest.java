package com.arcogine.api.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.arcogine.agents.SalesAgent;
import com.arcogine.agents.SalesAgentConfig;
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
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.DoubleSupplier;
import org.junit.jupiter.api.Test;

/**
 * Locks down IntegratedHandler's dispatch order (Pricing -> Demand -> Factory -> Finance ->
 * Agent) as an explicit, executable fact rather than something only visible by reading
 * IntegratedHandler.handleEvent's method-call sequence. Today the order has no observable effect
 * on any single event (each event type is handled by exactly one sub-handler; the others no-op),
 * but as more domains are added and cross-handler effects become possible within a single
 * dispatch, an accidental reorder should fail this test immediately rather than surface later as
 * a subtle behavioral change. Each recording subclass below overrides handleEvent to append its
 * name to a shared, ordered list before delegating to the real implementation -- this observes
 * the actual call sequence IntegratedHandler makes, not a hand-maintained assertion about it.
 */
class IntegratedHandlerDispatchOrderTest {

    private static final class RecordingPricingState extends PricingState {
        private final List<String> order;

        RecordingPricingState(double initialPrice, List<String> order) {
            super(initialPrice);
            this.order = order;
        }

        @Override
        public void handleEvent(Event event, Scheduler scheduler) throws SimError {
            order.add("pricing");
            super.handleEvent(event, scheduler);
        }
    }

    private static final class RecordingDemandModel extends DemandModel {
        private final List<String> order;

        RecordingDemandModel(
                double baseDemand,
                double priceElasticity,
                double leadTimeSensitivity,
                DoubleSupplier offerPrice,
                DoubleSupplier avgLeadTime,
                List<ProductId> productIds,
                Random rng,
                List<String> order) {
            super(baseDemand, priceElasticity, leadTimeSensitivity, offerPrice, avgLeadTime, productIds, rng);
            this.order = order;
        }

        @Override
        public void handleEvent(Event event, Scheduler scheduler) throws SimError {
            order.add("demand");
            super.handleEvent(event, scheduler);
        }
    }

    private static final class RecordingFactoryHandler extends FactoryHandler {
        private final List<String> order;

        RecordingFactoryHandler(
                MachineStore machines, RoutingStore routings, List<ProductId> productIds, List<String> order) {
            super(machines, routings, productIds);
            this.order = order;
        }

        @Override
        public void handleEvent(Event event, Scheduler scheduler) throws SimError {
            order.add("factory");
            super.handleEvent(event, scheduler);
        }
    }

    private static final class RecordingFinanceHandler extends FinanceHandler {
        private final List<String> order;

        RecordingFinanceHandler(List<String> order) {
            this.order = order;
        }

        @Override
        public void handleEvent(Event event, Scheduler scheduler) throws SimError {
            order.add("finance");
            super.handleEvent(event, scheduler);
        }
    }

    private static final class RecordingSalesAgent extends SalesAgent {
        private final List<String> order;

        RecordingSalesAgent(List<String> order) {
            super(SalesAgentConfig.DEFAULT);
            this.order = order;
        }

        @Override
        public void handleEvent(Event event, Scheduler scheduler) throws SimError {
            order.add("agent");
            super.handleEvent(event, scheduler);
        }
    }

    @Test
    void dispatchOrderIsPricingThenDemandThenFactoryThenFinanceThenAgent() {
        List<String> order = new ArrayList<>();

        MachineStore machines = new MachineStore();
        machines.add(new Machine(new MachineId(1), "Mill", 1, null, 0));
        RoutingStore routings = new RoutingStore();
        routings.addRouting(
                new Routing(1, "Widget Route", List.of(new RoutingStep(1, "Milling", new MachineId(1), 5))));
        routings.addProductRouting(new ProductId(1), 1);
        List<ProductId> productIds = List.of(new ProductId(1));

        RecordingFactoryHandler factory = new RecordingFactoryHandler(machines, routings, productIds, order);
        RecordingPricingState pricing = new RecordingPricingState(10.0, order);
        RecordingDemandModel demand = new RecordingDemandModel(
                5.0, 0.5, 0.0, pricing::offerPrice, factory::avgLeadTime, productIds, new Random(1), order);
        RecordingFinanceHandler finance = new RecordingFinanceHandler(order);
        RecordingSalesAgent agent = new RecordingSalesAgent(order);

        IntegratedHandler handler = new IntegratedHandler(factory, demand, pricing, finance, agent, true);

        Scheduler scheduler = new Scheduler();
        Event event = Event.of(SimTime.of(10), EventPayload.AgentEvaluation.INSTANCE);
        scheduler.schedule(event);
        scheduler.nextEvent();
        handler.handleEvent(event, scheduler);

        assertEquals(List.of("pricing", "demand", "factory", "finance", "agent"), order);
    }
}
