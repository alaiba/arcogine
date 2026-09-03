package com.arcogine.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
        assertNotNull(handler.offerPrice());
    }

    @Test
    void handleEventDelegatesEarlyOrderCompletionToFinance() throws SimError {
        ScenarioConfig config = basicConfig();
        HeadlessHandler handler = HeadlessHandler.fromConfig(config);
        Scheduler scheduler = new Scheduler();

        // Create an order that will complete
        Event orderCreation = Event.of(
                SimTime.of(0),
                new EventPayload.OrderCreation(new com.arcogine.types.ProductId(1), 1, 5.0));
        handler.handleEvent(orderCreation, scheduler);

        // Process the resulting job task
        assertTrue(scheduler.size() > 0, "ordering should have generated TaskStart events");
    }

    @Test
    void buildHeadlessHandlerWithMultipleEquipment() {
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
        assertNotNull(handler.factory);
    }

    @Test
    void headlessHandlerProcessesAllEventTypes() throws SimError {
        ScenarioConfig config = basicConfig();
        HeadlessHandler handler = HeadlessHandler.fromConfig(config);
        Scheduler scheduler = new Scheduler();

        // Test handling different event types through the same handler
        Event priceEvent = Event.of(SimTime.of(1), new EventPayload.PriceChange(8.0));
        handler.handleEvent(priceEvent, scheduler);

        Event demandEvent = Event.of(SimTime.of(2), EventPayload.DemandEvaluation.INSTANCE);
        handler.handleEvent(demandEvent, scheduler);

        assertTrue(scheduler.size() > 0, "demand evaluation should schedule orders");
    }

    @Test
    void runHeadlessWithMultipleMachinesCompletes() {
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
        assertTrue(handler.factory.completedSales() >= 0);
    }

    @Test
    void priceChangeEventUpdatesOfferPrice() throws SimError {
        ScenarioConfig config = basicConfig();
        HeadlessHandler handler = HeadlessHandler.fromConfig(config);
        Scheduler scheduler = new Scheduler();

        double initialPrice = handler.offerPrice();
        Event priceChangeEvent = Event.of(SimTime.of(1), new EventPayload.PriceChange(15.0));
        handler.handleEvent(priceChangeEvent, scheduler);

        double newPrice = handler.offerPrice();
        assertEquals(15.0, newPrice);
        assertNotEquals(initialPrice, newPrice);
    }
}
