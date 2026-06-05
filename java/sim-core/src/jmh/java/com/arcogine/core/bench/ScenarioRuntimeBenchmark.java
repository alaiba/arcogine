package com.arcogine.core.bench;

import com.arcogine.core.event.Event;
import com.arcogine.core.handler.EventHandler;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.core.runner.SimResult;
import com.arcogine.core.runner.SimRunner;
import com.arcogine.core.scenario.ScenarioLoader;
import com.arcogine.types.SimError;
import com.arcogine.types.scenario.ScenarioConfig;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

/** Ported from crates/sim-core/benches/scenario_runtime.rs. */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
public class ScenarioRuntimeBenchmark {

    private static final String BASIC_SCENARIO_TOML =
            """
            [simulation]
            rng_seed = 42
            max_ticks = 1000
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
            initial_price = 10.0
            base_demand = 3.0
            price_elasticity = 0.3
            lead_time_sensitivity = 0.0
            """;

    /** A handler that ignores every event — isolates the runtime/scheduler cost. */
    private static final class NullHandler implements EventHandler {
        @Override
        public void handleEvent(Event event, Scheduler scheduler) {
            // no-op
        }
    }

    private ScenarioConfig config;

    @Setup
    public void loadConfig() {
        config = ScenarioLoader.loadScenario(BASIC_SCENARIO_TOML);
    }

    @Benchmark
    public void runBasicScenario1000Ticks(Blackhole bh) throws SimError {
        SimResult result = SimRunner.runScenario(config, new NullHandler());
        bh.consume(result);
    }

    @Benchmark
    public void scenarioLoadAndValidate(Blackhole bh) {
        bh.consume(ScenarioLoader.loadScenario(BASIC_SCENARIO_TOML));
    }
}
