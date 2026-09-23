package com.arcogine.core.bench;

import com.arcogine.core.scenario.ScenarioLoader;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.infra.Blackhole;

/** Ported from crates/sim-core/benches/scenario_runtime.rs. */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class ScenarioLoaderBenchmark {

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

    @Benchmark
    public void scenarioLoadAndValidate(Blackhole bh) {
        bh.consume(ScenarioLoader.loadScenario(BASIC_SCENARIO_TOML));
    }
}
