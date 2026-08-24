package com.arcogine.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.agents.SalesAgent;
import com.arcogine.agents.SalesAgentConfig;
import com.arcogine.api.state.HandlerFactory;
import com.arcogine.api.state.IntegratedHandler;
import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.core.runner.SimRunner;
import com.arcogine.core.scenario.ScenarioLoader;
import com.arcogine.economy.demand.DemandModel;
import com.arcogine.economy.pricing.PricingState;
import com.arcogine.factory.machines.Machine;
import com.arcogine.factory.machines.MachineStore;
import com.arcogine.factory.process.FactoryHandler;
import com.arcogine.factory.routing.Routing;
import com.arcogine.factory.routing.RoutingStep;
import com.arcogine.factory.routing.RoutingStore;
import com.arcogine.finance.ledger.Account;
import com.arcogine.finance.process.FinanceHandler;
import com.arcogine.types.JobStatus;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import com.arcogine.types.scenario.ScenarioConfig;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * Scenario-level proof that Finance's output stays correct under the conditions that actually
 * stress the OfferPrice/OrderPrice invariant: a price change while orders sit in backlog, a
 * factory that can't keep up with demand, and an agent actively changing OfferPrice mid-run.
 * Unlike ScenarioBaselinesTest's Finance tests (which check aggregate agreement with Factory)
 * or OrderLifecycleIntegrationTest (which proves the causal chain mechanics), these tests
 * independently recompute the expected ledger total from each completed job's own captured
 * OrderPrice and assert the Ledger matches it exactly -- proving Finance used the right price,
 * not just that its total happens to agree with Factory's own (same-formula) total.
 */
class FinanceScenarioIntegrationTest {

    /**
     * Mirrors FinanceHandler's own computation exactly: quantity x unitPrice computed in
     * BigDecimal (not Job.orderValue()'s double multiplication, which can differ in the last
     * digit) and quantized to the canonical currency scale per job, then summed -- matching
     * FinanceHandler's per-posting rounding rather than rounding the total once, which can
     * disagree by a cent (rounding a sum of already-rounded values is not the same as rounding an
     * unrounded sum).
     */
    private static BigDecimal expectedCashFromCompletedJobs(FactoryHandler factory) {
        return factory.jobsView()
                .filter(j -> j.status() == JobStatus.Completed)
                .map(j -> BigDecimal.valueOf(j.unitPrice())
                        .multiply(BigDecimal.valueOf(j.quantity()))
                        .setScale(2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Test
    void priceChangeWhileOrdersAreInBacklogDoesNotAlterTheirLedgerPosting() {
        ScenarioConfig config = ScenarioLoader.loadScenario(
                """
                [simulation]
                rng_seed = 1
                max_ticks = 500
                demand_eval_interval = 1000
                agent_eval_interval = 1000

                [[equipment]]
                id = 1
                name = "Mill"

                [[material]]
                id = 1
                name = "Widget"
                routing_id = 1

                [[process_segment]]
                id = 1
                name = "Milling"
                equipment_id = 1
                duration = 10

                [[operations_definition]]
                id = 1
                name = "Widget routing"
                steps = [1]

                [economy]
                initial_price = 10.0
                base_demand = 0.0
                price_elasticity = 0.0
                lead_time_sensitivity = 0.0
                """);
        IntegratedHandler handler = HandlerFactory.buildFromConfig(config);
        Scheduler scheduler = new Scheduler();

        // Orders A and B are created back to back at t=0, both at the $10 offer price. Only A
        // starts immediately (single machine); B sits in backlog.
        Event orderA = Event.of(SimTime.ZERO, new EventPayload.OrderCreation(new ProductId(1), 2, 10.0));
        scheduler.schedule(orderA);
        scheduler.nextEvent();
        handler.handleEvent(orderA, scheduler);

        Event orderB = Event.of(SimTime.ZERO, new EventPayload.OrderCreation(new ProductId(1), 3, 10.0));
        handler.handleEvent(orderB, scheduler);

        // The offer price changes to $50 while both A (in progress) and B (queued/in backlog)
        // still exist. Neither has completed yet.
        Event priceChange = Event.of(SimTime.of(1), new EventPayload.PriceChange(50.0));
        scheduler.schedule(priceChange);
        scheduler.nextEvent();
        handler.handleEvent(priceChange, scheduler);

        // Order C is created after the price change, so it correctly captures $50.
        Event orderC = Event.of(SimTime.of(1), new EventPayload.OrderCreation(new ProductId(1), 1, 50.0));
        handler.handleEvent(orderC, scheduler);

        // Drain everything to completion.
        Event next;
        while ((next = scheduler.nextEvent().orElse(null)) != null) {
            handler.handleEvent(next, scheduler);
        }

        assertEquals(3, handler.factory().completedSales());
        assertEquals(3, handler.finance().ledger().entries().size());

        // A: 2 x $10 = $20, B: 3 x $10 = $30, C: 1 x $50 = $50 -> $100 total, NOT recomputed at $50.
        BigDecimal expected = new BigDecimal("100.00");
        assertEquals(0, expected.compareTo(expectedCashFromCompletedJobs(handler.factory())));
        assertEquals(0, expected.compareTo(handler.finance().ledger().balance(Account.CASH)));
    }

    @Test
    void overloadedFactoryStillAccountsExactlyForCompletedOrders() throws SimError {
        ScenarioConfig config = ScenarioLoader.loadScenario(
                """
                [simulation]
                rng_seed = 42
                max_ticks = 500
                demand_eval_interval = 10
                agent_eval_interval = 50

                [[equipment]]
                id = 1
                name = "Mill"
                [[equipment]]
                id = 2
                name = "Lathe"

                [[material]]
                id = 1
                name = "Widget A"
                routing_id = 1

                [[process_segment]]
                id = 1
                name = "Milling"
                equipment_id = 1
                duration = 8
                [[process_segment]]
                id = 2
                name = "Turning"
                equipment_id = 2
                duration = 5

                [[operations_definition]]
                id = 1
                name = "Widget A routing"
                steps = [1, 2]

                [economy]
                initial_price = 2.0
                base_demand = 5.0
                price_elasticity = 0.2
                lead_time_sensitivity = 0.02
                """);
        IntegratedHandler handler = HandlerFactory.buildFromConfig(config);
        SimRunner.runScenario(config, handler);

        long backlog = handler.factory().backlog();
        assertTrue(backlog > 0, "scenario should genuinely overload the factory, got backlog=" + backlog);

        assertEquals(handler.factory().completedSales(), handler.finance().ledger().entries().size());
        assertEquals(
                0,
                expectedCashFromCompletedJobs(handler.factory())
                        .compareTo(handler.finance().ledger().balance(Account.CASH)),
                "even under overload, the ledger accounts exactly for what actually completed - "
                        + "no more, no less, regardless of the large uncompleted backlog");
    }

    @Test
    void agentChangingOfferPriceLedgerReflectsEachOrdersOwnPrice() throws SimError {
        ScenarioConfig config = ScenarioLoader.loadScenario(
                """
                [simulation]
                rng_seed = 42
                max_ticks = 500
                demand_eval_interval = 10
                agent_eval_interval = 25

                [[equipment]]
                id = 1
                name = "Mill"

                [[material]]
                id = 1
                name = "Widget"
                routing_id = 1

                [[process_segment]]
                id = 1
                name = "Milling"
                equipment_id = 1
                duration = 10

                [[operations_definition]]
                id = 1
                name = "Widget routing"
                steps = [1]

                [economy]
                initial_price = 2.0
                base_demand = 8.0
                price_elasticity = 0.5
                lead_time_sensitivity = 0.0

                [agent]
                enabled = true
                agent_type = "sales"
                """);

        MachineStore machines = new MachineStore();
        machines.add(new Machine(new MachineId(1), "Mill", 1, null, 0));
        RoutingStore routings = new RoutingStore();
        routings.addRouting(
                new Routing(1, "Widget routing", List.of(new RoutingStep(1, "Milling", new MachineId(1), 10))));
        routings.addProductRouting(new ProductId(1), 1);
        List<ProductId> productIds = List.of(new ProductId(1));

        FactoryHandler factory = new FactoryHandler(machines, routings, productIds);
        PricingState pricing = new PricingState(2.0);
        DemandModel demand = new DemandModel(
                8.0, 0.5, 0.0, pricing::offerPrice, factory::avgLeadTime, productIds, new Random(42));
        SalesAgent agent = new SalesAgent(new SalesAgentConfig(5, 2, 0.15, 0.5, 50.0));
        IntegratedHandler handler = new IntegratedHandler(factory, demand, pricing, new FinanceHandler(), agent, true);

        SimRunner.runScenario(config, handler);

        assertTrue(
                handler.agent().interventions() > 0,
                "agent must actually change OfferPrice during the run for this test to be meaningful");
        assertTrue(handler.factory().completedSales() > 1, "need multiple completed orders to be meaningful");

        assertEquals(handler.factory().completedSales(), handler.finance().ledger().entries().size());
        assertEquals(
                0,
                expectedCashFromCompletedJobs(handler.factory())
                        .compareTo(handler.finance().ledger().balance(Account.CASH)),
                "Ledger must total each completed order's own creation-time price, "
                        + "not whatever OfferPrice the agent last set");
    }
}
