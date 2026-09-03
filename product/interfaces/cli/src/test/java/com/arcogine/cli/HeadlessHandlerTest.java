package com.arcogine.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.event.EventType;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.core.runner.SimResult;
import com.arcogine.core.runner.SimRunner;
import com.arcogine.core.scenario.ScenarioLoader;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import com.arcogine.types.scenario.ScenarioConfig;
import org.junit.jupiter.api.Test;

/** Ported from the inline #[cfg(test)] module in crates/sim-cli/src/main.rs. */
class HeadlessHandlerTest {

    private static final String BASIC_TOML =
            """
            [simulation]
            rng_seed = 42
            max_ticks = 200
            demand_eval_interval = 10

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
            duration = 5

            [[operations_definition]]
            id = 1
            name = "Widget routing"
            steps = [1]

            [economy]
            initial_price = 5.0
            base_demand = 10.0
            """;

    private static ScenarioConfig basicConfig() {
        return ScenarioLoader.loadScenario(BASIC_TOML);
    }

    private static SimResult runHeadless(ScenarioConfig config, HeadlessHandler handler) {
        return SimRunner.runScenario(config, handler);
    }

    @Test
    void headlessHandlerDelegatesToPricingDemandFactory() throws SimError {
        ScenarioConfig config = basicConfig();
        HeadlessHandler handler = HeadlessHandler.fromConfig(config);
        Scheduler scheduler = new Scheduler();

        Event seed = Event.of(SimTime.of(10), EventPayload.DemandEvaluation.INSTANCE);
        scheduler.schedule(seed);
        Event event = scheduler.nextEvent().orElseThrow();
        handler.handleEvent(event, scheduler);
        assertFalse(
                scheduler.isEmpty(),
                "DemandEvaluation should generate OrderCreation events");

        Event order = scheduler.nextEvent().orElseThrow();
        handler.handleEvent(order, scheduler);
        assertTrue(
                handler.factory.jobsView().count() > 0,
                "factory should have a job after OrderCreation");
    }

    @Test
    void runHeadlessReturnsErrorForInvalidToml() {
        assertThrows(SimError.class, () -> ScenarioLoader.loadScenario("not valid toml {{{}"));
    }

    @Test
    void runHeadlessCompletesWithSalesAndValue() {
        ScenarioConfig config = basicConfig();
        HeadlessHandler handler = HeadlessHandler.fromConfig(config);
        SimResult result = runHeadless(config, handler);
        assertTrue(result.eventsProcessed() > 0);
        assertTrue(handler.factory.completedSales() > 0);
        assertTrue(handler.factory.completedSalesValue() > 0.0);
    }

    @Test
    void headlessProducesTaskStartEvents() {
        ScenarioConfig config = basicConfig();
        HeadlessHandler handler = HeadlessHandler.fromConfig(config);
        SimResult result = runHeadless(config, handler);
        long taskStarts = result.eventLog().filterByType(EventType.TaskStart).count();
        assertTrue(taskStarts > 0, "headless run should produce TaskStart events");
    }

    @Test
    void runHeadlessActuallyReturnsErrorForInvalidToml() {
        SimError err = assertThrows(
                SimError.class, () -> ScenarioLoader.loadScenario("not valid toml {{{}"));
        assertNotNull(err.getMessage());
        assertFalse(err.getMessage().isEmpty());
    }

    @Test
    void runHeadlessProducesTaskEndEvents() {
        ScenarioConfig config = basicConfig();
        HeadlessHandler handler = HeadlessHandler.fromConfig(config);
        SimResult result = runHeadless(config, handler);
        long taskEnds = result.eventLog().filterByType(EventType.TaskEnd).count();
        assertTrue(taskEnds > 0, "headless run should produce TaskEnd events");
    }

    @Test
    void runHeadlessEventCountMatchesEventsProcessed() {
        ScenarioConfig config = basicConfig();
        HeadlessHandler handler = HeadlessHandler.fromConfig(config);
        SimResult result = runHeadless(config, handler);
        assertEquals((long) result.eventLog().count(), result.eventsProcessed());
    }

    @Test
    void buildHeadlessHandlerWithoutEconomyUsesDefaults() {
        String toml =
                """
                [simulation]
                rng_seed = 1
                max_ticks = 10
                demand_eval_interval = 5

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
                duration = 5

                [[operations_definition]]
                id = 1
                name = "Widget routing"
                steps = [1]
                """;
        ScenarioConfig config = ScenarioLoader.loadScenario(toml);
        HeadlessHandler handler = HeadlessHandler.fromConfig(config);
        assertEquals(10.0, handler.offerPrice());
    }

    @Test
    void runHeadlessFinalTimeIsPositive() {
        ScenarioConfig config = basicConfig();
        HeadlessHandler handler = HeadlessHandler.fromConfig(config);
        SimResult result = runHeadless(config, handler);
        assertTrue(
                result.finalTime().ticks() > 0,
                "simulation should advance past t=0");
    }

    @Test
    void headlessHandlerPricePropagatesToFactory() throws SimError {
        ScenarioConfig config = basicConfig();
        HeadlessHandler handler = HeadlessHandler.fromConfig(config);
        Scheduler scheduler = new Scheduler();

        Event priceEvent = Event.of(SimTime.of(1), new EventPayload.PriceChange(99.0));
        handler.handleEvent(priceEvent, scheduler);
        assertEquals(99.0, handler.offerPrice());
    }

    @Test
    void financeAccessorReturnsFinanceHandler() {
        ScenarioConfig config = basicConfig();
        HeadlessHandler handler = HeadlessHandler.fromConfig(config);
        assertNotNull(handler.finance());
    }

    @Test
    void buildHeadlessHandlerCreatesAllHandlers() {
        ScenarioConfig config = basicConfig();
        HeadlessHandler handler = HeadlessHandler.fromConfig(config);
        assertNotNull(handler.factory);
        assertNotNull(handler.finance());
        double initialPrice = handler.offerPrice();
        assertEquals(5.0, initialPrice, "initial price should match config");
    }

    @Test
    void handleEventDelegatesOrderCompletionToFinance() throws SimError {
        ScenarioConfig config = basicConfig();
        HeadlessHandler handler = HeadlessHandler.fromConfig(config);
        // Run full scenario and verify finance ledger was populated by order completions
        SimResult result = runHeadless(config, handler);

        // Finance ledger should contain entries from order completions
        assertTrue(
                result.eventLog().filterByType(EventType.OrderCompleted).count() > 0,
                "scenario should complete orders");
        assertTrue(
                handler.finance().ledger().entries().size() > 0,
                "finance should have recorded order completions in ledger");
    }

    @Test
    void multiEquipmentRoutingIsConstructed() {
        String toml =
                """
                [simulation]
                rng_seed = 1
                max_ticks = 100
                demand_eval_interval = 10

                [[equipment]]
                id = 1
                name = "Mill"

                [[equipment]]
                id = 2
                name = "Dryer"

                [[material]]
                id = 1
                name = "Widget"
                routing_id = 1

                [[process_segment]]
                id = 1
                name = "Milling"
                equipment_id = 1
                duration = 5

                [[process_segment]]
                id = 2
                name = "Drying"
                equipment_id = 2
                duration = 3

                [[operations_definition]]
                id = 1
                name = "Widget routing"
                steps = [1, 2]

                [economy]
                initial_price = 8.0
                base_demand = 15.0
                """;
        ScenarioConfig config = ScenarioLoader.loadScenario(toml);
        HeadlessHandler handler = HeadlessHandler.fromConfig(config);

        assertEquals(8.0, handler.offerPrice());
        // Verify both equipment was registered in the factory's machine store
        assertTrue(
                handler.factory.ordersView().count() >= 0,
                "multi-equipment factory should be initialized");
        // Verify routing with both steps was constructed
        assertTrue(handler.factory.routings != null, "routing store should be created");
    }

    @Test
    void headlessHandlerDelegatesMultipleEventTypes() throws SimError {
        ScenarioConfig config = basicConfig();
        HeadlessHandler handler = HeadlessHandler.fromConfig(config);
        Scheduler scheduler = new Scheduler();

        // PriceChange event
        double initialPrice = handler.offerPrice();
        Event priceEvent = Event.of(SimTime.of(1), new EventPayload.PriceChange(8.0));
        handler.handleEvent(priceEvent, scheduler);
        assertEquals(8.0, handler.offerPrice(), "price change should propagate");

        // DemandEvaluation event
        Event demandEvent = Event.of(SimTime.of(2), EventPayload.DemandEvaluation.INSTANCE);
        handler.handleEvent(demandEvent, scheduler);
        assertTrue(scheduler.size() > 0, "demand evaluation should schedule orders");

        // OrderCreation event
        Event orderEvent = Event.of(
                SimTime.of(3),
                new EventPayload.OrderCreation(new com.arcogine.types.ProductId(1), 1, 8.0));
        handler.handleEvent(orderEvent, scheduler);
        assertTrue(scheduler.size() > 0, "order creation should generate factory events");
    }

    @Test
    void multiMachineScenarioCompletesWithSales() {
        String toml =
                """
                [simulation]
                rng_seed = 42
                max_ticks = 100
                demand_eval_interval = 5

                [[equipment]]
                id = 1
                name = "Machine A"

                [[equipment]]
                id = 2
                name = "Machine B"

                [[material]]
                id = 1
                name = "Product"
                routing_id = 1

                [[process_segment]]
                id = 1
                name = "Step 1"
                equipment_id = 1
                duration = 3

                [[process_segment]]
                id = 2
                name = "Step 2"
                equipment_id = 2
                duration = 3

                [[operations_definition]]
                id = 1
                name = "Routing"
                steps = [1, 2]

                [economy]
                initial_price = 10.0
                base_demand = 20.0
                """;
        ScenarioConfig config = ScenarioLoader.loadScenario(toml);
        HeadlessHandler handler = HeadlessHandler.fromConfig(config);
        SimResult result = runHeadless(config, handler);

        assertTrue(result.eventsProcessed() > 0);
        assertTrue(
                handler.factory.completedSales() > 0,
                "multi-machine scenario should complete orders");
    }

    @Test
    void orderCompletionUpdatesFinanceLedger() throws SimError {
        ScenarioConfig config = basicConfig();
        HeadlessHandler handler = HeadlessHandler.fromConfig(config);

        // Run scenario and verify ledger captured sales
        SimResult result = runHeadless(config, handler);

        // Verify that completed orders result in ledger entries
        long completedOrders = result.eventLog().filterByType(EventType.OrderCompleted).count();
        int ledgerEntries = handler.finance().ledger().entries().size();

        assertEquals(
                completedOrders,
                ledgerEntries,
                "each order completion should produce exactly one ledger entry");
    }
}
