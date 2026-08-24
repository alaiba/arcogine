package com.arcogine.core.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.arcogine.core.event.Event;
import com.arcogine.core.handler.EventHandler;
import com.arcogine.core.kpi.EventCount;
import com.arcogine.core.kpi.KpiValue;
import com.arcogine.core.kpi.TotalSimulatedTime;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.core.scenario.ScenarioLoader;
import com.arcogine.types.scenario.ScenarioConfig;
import org.junit.jupiter.api.Test;

/**
 * Determinism tests ported from crates/sim-core/tests/determinism.rs.
 *
 * <p>The Java rewrite switched RNG from ChaCha8 to {@code java.util.Random},
 * so we assert reproducibility (same scenario run twice yields identical event
 * logs, final time, KPIs) rather than copying Rust numeric golden values. The
 * noop handler used here is RNG-free, so the runner output is fully
 * deterministic regardless of seed.
 */
class DeterminismTest {

    /** Does nothing. */
    private static final class NoopHandler implements EventHandler {
        @Override
        public void handleEvent(Event event, Scheduler scheduler) {
            // no-op
        }
    }

    private static final String MINIMAL_SCENARIO_TOML = """
            [simulation]
            rng_seed = 42
            max_ticks = 100
            demand_eval_interval = 10
            agent_eval_interval = 50

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
            name = "Widget Routing"
            steps = [1]
            """;

    @Test
    void identicalRunsProduceIdenticalEventLogs() {
        ScenarioConfig config = ScenarioLoader.loadScenario(MINIMAL_SCENARIO_TOML);

        SimResult result1 = SimRunner.runScenario(config, new NoopHandler());
        SimResult result2 = SimRunner.runScenario(config, new NoopHandler());

        assertEquals(result1.eventLog(), result2.eventLog());
        assertEquals(result1.finalTime(), result2.finalTime());
        assertEquals(result1.eventsProcessed(), result2.eventsProcessed());
    }

    @Test
    void identicalRunsProduceIdenticalKpis() {
        ScenarioConfig config = ScenarioLoader.loadScenario(MINIMAL_SCENARIO_TOML);

        SimResult result1 = SimRunner.runScenario(config, new NoopHandler());
        SimResult result2 = SimRunner.runScenario(config, new NoopHandler());

        TotalSimulatedTime timeKpi = new TotalSimulatedTime();
        EventCount countKpi = new EventCount();

        KpiValue t1 = timeKpi.compute(result1.eventLog(), result1.finalTime());
        KpiValue t2 = timeKpi.compute(result2.eventLog(), result2.finalTime());
        assertEquals(t1, t2);

        KpiValue c1 = countKpi.compute(result1.eventLog(), result1.finalTime());
        KpiValue c2 = countKpi.compute(result2.eventLog(), result2.finalTime());
        assertEquals(c1, c2);
    }

    @Test
    void runnerIsDeterministicWithNoopHandler() {
        String toml = """
                [simulation]
                rng_seed = 42
                max_ticks = 100
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
                name = "Widget Routing"
                steps = [1]
                """;

        ScenarioConfig config = ScenarioLoader.loadScenario(toml);
        SimResult result = SimRunner.runScenario(config, new NoopHandler());

        // DemandEvaluation events fire at 10, 20, ..., 100 = 10 events
        assertEquals(10L, result.eventsProcessed());
    }
}
