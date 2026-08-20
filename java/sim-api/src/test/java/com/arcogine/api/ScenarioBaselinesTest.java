package com.arcogine.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.api.state.HandlerFactory;
import com.arcogine.api.state.IntegratedHandler;
import com.arcogine.core.event.EventType;
import com.arcogine.core.runner.SimResult;
import com.arcogine.core.runner.SimRunner;
import com.arcogine.core.scenario.ScenarioLoader;
import com.arcogine.finance.ledger.Account;
import com.arcogine.types.JobId;
import com.arcogine.types.SimError;
import com.arcogine.types.scenario.ScenarioConfig;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.junit.jupiter.api.Test;

/**
 * Ported from crates/sim-api/tests/scenario_baselines.rs. Behavioural acceptance
 * tests that drive the integrated handler (factory + demand + pricing) through
 * the simulation runner. None of these scenarios enable an agent, so
 * {@link HandlerFactory} produces a handler whose agent stays dormant — matching
 * the Rust {@code IntegratedHandler} which had no agent.
 */
class ScenarioBaselinesTest {

    private static final String BASIC_SCENARIO =
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
            [[equipment]]
            id = 3
            name = "QC Station"

            [[material]]
            id = 1
            name = "Widget A"
            routing_id = 1
            [[material]]
            id = 2
            name = "Widget B"
            routing_id = 2

            [[process_segment]]
            id = 1
            name = "Rough milling"
            equipment_id = 1
            duration = 5
            [[process_segment]]
            id = 2
            name = "Turning"
            equipment_id = 2
            duration = 3
            [[process_segment]]
            id = 3
            name = "Quality check"
            equipment_id = 3
            duration = 2
            [[process_segment]]
            id = 4
            name = "Fine milling"
            equipment_id = 1
            duration = 4

            [[operations_definition]]
            id = 1
            name = "Widget A routing"
            steps = [1, 2, 3]
            [[operations_definition]]
            id = 2
            name = "Widget B routing"
            steps = [4, 3]

            [economy]
            initial_price = 5.0
            base_demand = 3.0
            price_elasticity = 0.3
            lead_time_sensitivity = 0.05
            """;

    private static final String OVERLOAD_SCENARIO =
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
            """;

    private static final String PRICE_SENSITIVE_BASE =
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
            initial_price = %s
            base_demand = 5.0
            price_elasticity = 0.5
            lead_time_sensitivity = 0.0
            """;

    private record RunOutcome(SimResult result, IntegratedHandler handler) {}

    private static RunOutcome run(String toml) throws SimError {
        ScenarioConfig config = ScenarioLoader.loadScenario(toml);
        IntegratedHandler handler = HandlerFactory.buildFromConfig(config);
        SimResult result = SimRunner.runScenario(config, handler);
        return new RunOutcome(result, handler);
    }

    @Test
    void basicScenarioRunsToCompletion() throws SimError {
        RunOutcome run = run(BASIC_SCENARIO);
        assertTrue(run.result().eventsProcessed() > 0, "no events processed");
        assertTrue(run.handler().factory().completedSales() > 0, "no sales completed");
        assertTrue(run.handler().factory().completedSalesValue() > 0.0, "no revenue generated");
    }

    @Test
    void overloadScenarioBuildsBacklog() throws SimError {
        RunOutcome run = run(OVERLOAD_SCENARIO);
        long backlog = run.handler().factory().backlog();
        assertTrue(backlog > 0, "overload scenario should have nonzero backlog, got " + backlog);
    }

    @Test
    void loweringPriceIncreasesDemand() throws SimError {
        RunOutcome high = run(PRICE_SENSITIVE_BASE.formatted("10.0"));
        long ordersHigh = high.result().eventLog().filterByType(EventType.OrderCreation).count();

        RunOutcome low = run(PRICE_SENSITIVE_BASE.formatted("1.0"));
        long ordersLow = low.result().eventLog().filterByType(EventType.OrderCreation).count();

        assertTrue(ordersLow > ordersHigh,
                "lower price should generate more orders: low=" + ordersLow + ", high=" + ordersHigh);
    }

    @Test
    void raisingPriceReducesLoad() throws SimError {
        RunOutcome low = run(PRICE_SENSITIVE_BASE.formatted("1.0"));
        long backlogLow = low.handler().factory().backlog();

        RunOutcome high = run(PRICE_SENSITIVE_BASE.formatted("9.0"));
        long backlogHigh = high.handler().factory().backlog();

        assertTrue(backlogHigh <= backlogLow,
                "higher price should reduce backlog: high=" + backlogHigh + ", low=" + backlogLow);
    }

    @Test
    void revenueGeneratedFromCompletedJobs() throws SimError {
        RunOutcome run = run(BASIC_SCENARIO);
        assertTrue(run.handler().factory().completedSalesValue() > 0.0);
        assertTrue(run.handler().factory().completedSales() > 0);
    }

    @Test
    void sameScenarioAndSeedProducesIdenticalOrdersAndCompletedSalesValue() throws SimError {
        RunOutcome first = run(BASIC_SCENARIO);
        RunOutcome second = run(BASIC_SCENARIO);

        assertEquals(
                first.handler().factory().completedSalesValue(),
                second.handler().factory().completedSalesValue(),
                "same scenario + seed must produce identical completedSalesValue");
        assertEquals(first.handler().factory().completedSales(), second.handler().factory().completedSales());
        assertEquals(first.result().eventsProcessed(), second.result().eventsProcessed());

        long ordersA = first.result().eventLog().filterByType(EventType.OrderCreation).count();
        long ordersB = second.result().eventLog().filterByType(EventType.OrderCreation).count();
        assertEquals(ordersA, ordersB, "identical number of orders must be generated");

        long jobCount = first.handler().factory().jobsView().count();
        assertEquals(jobCount, second.handler().factory().jobsView().count());
        for (long id = 1; id <= jobCount; id++) {
            var jobA = first.handler().factory().job(new JobId(id));
            var jobB = second.handler().factory().job(new JobId(id));
            assertEquals(jobA.unitPrice(), jobB.unitPrice(), "job " + id + " unitPrice must be reproducible");
            assertEquals(jobA.orderValue(), jobB.orderValue(), "job " + id + " orderValue must be reproducible");
            assertEquals(jobA.status(), jobB.status(), "job " + id + " status must be reproducible");
        }
    }

    @Test
    void financeLedgerAgreesWithFactoryCompletedSalesValue() throws SimError {
        RunOutcome run = run(BASIC_SCENARIO);

        assertEquals(run.handler().factory().completedSales(), run.handler().finance().ledger().entries().size(),
                "one journal entry per completed order under the immediate-settlement policy");

        BigDecimal cash = run.handler().finance().ledger().balance(Account.CASH);
        assertEquals(
                0,
                cash.compareTo(BigDecimal.valueOf(run.handler().factory().completedSalesValue())
                        .setScale(2, RoundingMode.HALF_UP)),
                "Finance's Cash balance must agree with Factory's completedSalesValue "
                        + "under the current immediate-settlement policy");
    }

    @Test
    void financeLedgerIsReproducibleForSameScenarioAndSeed() throws SimError {
        RunOutcome first = run(BASIC_SCENARIO);
        RunOutcome second = run(BASIC_SCENARIO);

        assertEquals(
                first.handler().finance().ledger().entries(),
                second.handler().finance().ledger().entries(),
                "same scenario + seed must produce an identical sequence of journal entries");
    }
}
