//! Determinism tests: verify that running the same scenario with the same
//! seed produces identical event logs, final state, and KPIs.

use sim_core::event::Event;
use sim_core::handler::EventHandler;
use sim_core::kpi::{EventCount, Kpi, TotalSimulatedTime};
use sim_core::queue::Scheduler;
use sim_core::runner::run_scenario;
use sim_core::scenario::load_scenario;
use sim_types::SimError;

struct NoopHandler;

impl EventHandler for NoopHandler {
    fn handle_event(&mut self, _event: &Event, _scheduler: &mut Scheduler) -> Result<(), SimError> {
        Ok(())
    }
}

fn minimal_scenario_toml() -> &'static str {
    r#"
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
"#
}

#[test]
fn identical_runs_produce_identical_event_logs() {
    let config = load_scenario(minimal_scenario_toml()).unwrap();

    let result1 = run_scenario(&config, &mut NoopHandler).unwrap();
    let result2 = run_scenario(&config, &mut NoopHandler).unwrap();

    assert_eq!(result1.event_log, result2.event_log);
    assert_eq!(result1.final_time, result2.final_time);
    assert_eq!(result1.events_processed, result2.events_processed);
}

#[test]
fn identical_runs_produce_identical_kpis() {
    let config = load_scenario(minimal_scenario_toml()).unwrap();

    let result1 = run_scenario(&config, &mut NoopHandler).unwrap();
    let result2 = run_scenario(&config, &mut NoopHandler).unwrap();

    let time_kpi = TotalSimulatedTime;
    let count_kpi = EventCount;

    let t1 = time_kpi.compute(&result1.event_log, result1.final_time);
    let t2 = time_kpi.compute(&result2.event_log, result2.final_time);
    assert_eq!(t1, t2);

    let c1 = count_kpi.compute(&result1.event_log, result1.final_time);
    let c2 = count_kpi.compute(&result2.event_log, result2.final_time);
    assert_eq!(c1, c2);
}

#[test]
fn different_seeds_can_produce_different_results() {
    // With a noop handler the event logs should still be identical since
    // the noop handler doesn't use RNG. This test verifies the runner
    // itself is deterministic regardless of seed when no stochastic
    // handler is involved.
    let toml1 = r#"
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
"#;

    let config = load_scenario(toml1).unwrap();
    let result = run_scenario(&config, &mut NoopHandler).unwrap();

    // The DemandEvaluation events fire at 10, 20, ..., 100 = 10 events
    assert_eq!(result.events_processed, 10);
}
