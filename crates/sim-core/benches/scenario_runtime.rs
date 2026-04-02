use criterion::{black_box, criterion_group, criterion_main, Criterion};
use sim_core::event::Event;
use sim_core::handler::EventHandler;
use sim_core::queue::Scheduler;
use sim_core::runner::run_scenario;
use sim_core::scenario::load_scenario;
use sim_types::SimError;

struct NullHandler;

impl EventHandler for NullHandler {
    fn handle_event(&mut self, _event: &Event, _scheduler: &mut Scheduler) -> Result<(), SimError> {
        Ok(())
    }
}

fn basic_scenario_toml() -> &'static str {
    r#"
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
"#
}

fn bench_scenario_execution(c: &mut Criterion) {
    c.bench_function("run_basic_scenario_1000_ticks", |b| {
        let config = load_scenario(basic_scenario_toml()).unwrap();
        b.iter(|| {
            let mut handler = NullHandler;
            let result = run_scenario(&config, &mut handler).unwrap();
            black_box(&result);
        });
    });

    c.bench_function("scenario_load_and_validate", |b| {
        b.iter(|| {
            let config = load_scenario(black_box(basic_scenario_toml())).unwrap();
            black_box(&config);
        });
    });
}

criterion_group!(benches, bench_scenario_execution);
criterion_main!(benches);
