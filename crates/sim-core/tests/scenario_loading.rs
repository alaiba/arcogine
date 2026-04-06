//! Tests for scenario loading: valid TOML, malformed TOML, missing fields,
//! invalid references, and out-of-range values.

use sim_core::scenario::load_scenario;
use sim_types::SimError;

fn valid_scenario() -> &'static str {
    r#"
[simulation]
rng_seed = 42
max_ticks = 100

[[equipment]]
id = 1
name = "Mill"

[[equipment]]
id = 2
name = "Lathe"

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
name = "Turning"
equipment_id = 2
duration = 3

[[operations_definition]]
id = 1
name = "Widget Routing"
steps = [1, 2]
"#
}

#[test]
fn load_valid_scenario() {
    let config = load_scenario(valid_scenario()).unwrap();
    assert_eq!(config.simulation.rng_seed, 42);
    assert_eq!(config.simulation.max_ticks, 100);
    assert_eq!(config.equipment.len(), 2);
    assert_eq!(config.material.len(), 1);
    assert_eq!(config.process_segment.len(), 2);
    assert_eq!(config.operations_definition.len(), 1);
}

#[test]
fn malformed_toml_returns_error() {
    let result = load_scenario("this is not valid toml {{{{");
    assert!(result.is_err());
    match result.unwrap_err() {
        SimError::ScenarioLoadError { message } => {
            assert!(message.contains("TOML parse error"));
        }
        other => panic!("expected ScenarioLoadError, got: {:?}", other),
    }
}

#[test]
fn missing_simulation_section_returns_error() {
    let toml = r#"
[[equipment]]
id = 1
name = "Mill"
"#;
    let result = load_scenario(toml);
    assert!(result.is_err());
}

#[test]
fn missing_rng_seed_returns_error() {
    let toml = r#"
[simulation]
max_ticks = 100

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
    let result = load_scenario(toml);
    assert!(result.is_err());
}

#[test]
fn no_equipment_returns_error() {
    let toml = r#"
[simulation]
rng_seed = 42
max_ticks = 100

[[material]]
id = 1
name = "Widget"
routing_id = 1
"#;
    let result = load_scenario(toml);
    assert!(result.is_err());
    match result.unwrap_err() {
        SimError::ScenarioLoadError { message } => {
            assert!(message.contains("equipment"));
        }
        other => panic!("expected ScenarioLoadError, got: {:?}", other),
    }
}

#[test]
fn no_material_returns_error() {
    let toml = r#"
[simulation]
rng_seed = 42
max_ticks = 100

[[equipment]]
id = 1
name = "Mill"
"#;
    let result = load_scenario(toml);
    assert!(result.is_err());
    match result.unwrap_err() {
        SimError::ScenarioLoadError { message } => {
            assert!(message.contains("material"));
        }
        other => panic!("expected ScenarioLoadError, got: {:?}", other),
    }
}

#[test]
fn nonexistent_routing_reference_returns_error() {
    let toml = r#"
[simulation]
rng_seed = 42
max_ticks = 100

[[equipment]]
id = 1
name = "Mill"

[[material]]
id = 1
name = "Widget"
routing_id = 999

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
    let result = load_scenario(toml);
    assert!(result.is_err());
    match result.unwrap_err() {
        SimError::InvalidReference { message } => {
            assert!(message.contains("999"));
        }
        other => panic!("expected InvalidReference, got: {:?}", other),
    }
}

#[test]
fn nonexistent_equipment_in_process_segment_returns_error() {
    let toml = r#"
[simulation]
rng_seed = 42
max_ticks = 100

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
equipment_id = 999
duration = 5

[[operations_definition]]
id = 1
name = "Widget Routing"
steps = [1]
"#;
    let result = load_scenario(toml);
    assert!(result.is_err());
    match result.unwrap_err() {
        SimError::InvalidReference { message } => {
            assert!(message.contains("999"));
        }
        other => panic!("expected InvalidReference, got: {:?}", other),
    }
}

#[test]
fn zero_max_ticks_returns_error() {
    let toml = r#"
[simulation]
rng_seed = 42
max_ticks = 0

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
    let result = load_scenario(toml);
    assert!(result.is_err());
    match result.unwrap_err() {
        SimError::OutOfRange { field, .. } => {
            assert!(field.contains("max_ticks"));
        }
        other => panic!("expected OutOfRange, got: {:?}", other),
    }
}

#[test]
fn zero_duration_process_segment_returns_error() {
    let toml = r#"
[simulation]
rng_seed = 42
max_ticks = 100

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
duration = 0

[[operations_definition]]
id = 1
name = "Widget Routing"
steps = [1]
"#;
    let result = load_scenario(toml);
    assert!(result.is_err());
    match result.unwrap_err() {
        SimError::OutOfRange { field, .. } => {
            assert!(field.contains("duration"));
        }
        other => panic!("expected OutOfRange, got: {:?}", other),
    }
}

#[test]
fn duplicate_equipment_ids_returns_error() {
    let toml = r#"
[simulation]
rng_seed = 42
max_ticks = 100

[[equipment]]
id = 1
name = "Mill A"

[[equipment]]
id = 1
name = "Mill B"

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
    let result = load_scenario(toml);
    assert!(result.is_err());
    match result.unwrap_err() {
        SimError::InvalidReference { message } => {
            assert!(message.contains("duplicate"));
        }
        other => panic!("expected InvalidReference, got: {:?}", other),
    }
}

#[test]
fn scenario_with_nan_price_rejected() {
    let toml = r#"
[simulation]
rng_seed = 42
max_ticks = 100

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

[economy]
initial_price = nan
"#;
    let result = load_scenario(toml);
    assert!(result.is_err());
    match result.unwrap_err() {
        SimError::OutOfRange { field, .. } => {
            assert!(field.contains("initial_price"));
        }
        other => panic!("expected OutOfRange, got: {:?}", other),
    }
}

#[test]
fn scenario_with_inf_demand_rejected() {
    let toml = r#"
[simulation]
rng_seed = 42
max_ticks = 100

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

[economy]
initial_price = 10.0
base_demand = inf
"#;
    let result = load_scenario(toml);
    assert!(result.is_err());
    match result.unwrap_err() {
        SimError::OutOfRange { field, .. } => {
            assert!(field.contains("base_demand"));
        }
        other => panic!("expected OutOfRange, got: {:?}", other),
    }
}

#[test]
fn scenario_with_extreme_price_rejected() {
    let toml = r#"
[simulation]
rng_seed = 42
max_ticks = 100

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

[economy]
initial_price = 2000000.0
"#;
    let result = load_scenario(toml);
    assert!(result.is_err());
    match result.unwrap_err() {
        SimError::OutOfRange { field, .. } => {
            assert!(field.contains("initial_price"));
        }
        other => panic!("expected OutOfRange, got: {:?}", other),
    }
}

#[test]
fn default_values_applied_correctly() {
    let config = load_scenario(valid_scenario()).unwrap();
    assert_eq!(config.simulation.demand_eval_interval, 10);
    assert_eq!(config.simulation.agent_eval_interval, 50);
    assert_eq!(config.equipment[0].concurrency, 1);
    assert_eq!(config.equipment[0].setup_time, 0);
    assert!(config.equipment[0].capacity_liters.is_none());
}

fn scenario_with_economy(economy_block: &str) -> String {
    format!(
        r#"
[simulation]
rng_seed = 42
max_ticks = 100

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

{economy_block}
"#
    )
}

#[test]
fn scenario_with_nan_price_rejected() {
    let toml = scenario_with_economy("[economy]\ninitial_price = nan\n");
    let result = load_scenario(&toml);
    assert!(result.is_err());
    match result.unwrap_err() {
        SimError::OutOfRange { field, .. } => {
            assert!(
                field.contains("initial_price"),
                "expected initial_price, got: {field}"
            );
        }
        other => panic!("expected OutOfRange, got: {:?}", other),
    }
}

#[test]
fn scenario_with_inf_demand_rejected() {
    let toml = scenario_with_economy("[economy]\ninitial_price = 10.0\nbase_demand = inf\n");
    let result = load_scenario(&toml);
    assert!(result.is_err());
    match result.unwrap_err() {
        SimError::OutOfRange { field, .. } => {
            assert!(
                field.contains("base_demand"),
                "expected base_demand, got: {field}"
            );
        }
        other => panic!("expected OutOfRange, got: {:?}", other),
    }
}

#[test]
fn scenario_with_extreme_price_rejected() {
    let toml = scenario_with_economy("[economy]\ninitial_price = 999999999.0\n");
    let result = load_scenario(&toml);
    assert!(result.is_err());
    match result.unwrap_err() {
        SimError::OutOfRange { field, .. } => {
            assert!(
                field.contains("initial_price"),
                "expected initial_price, got: {field}"
            );
        }
        other => panic!("expected OutOfRange, got: {:?}", other),
    }
}

#[test]
fn scenario_with_extreme_base_demand_rejected() {
    let toml = scenario_with_economy("[economy]\ninitial_price = 10.0\nbase_demand = 1500000.0\n");
    let result = load_scenario(&toml);
    assert!(result.is_err());
    match result.unwrap_err() {
        SimError::OutOfRange { field, .. } => {
            assert!(
                field.contains("base_demand"),
                "expected base_demand, got: {field}"
            );
        }
        other => panic!("expected OutOfRange, got: {:?}", other),
    }
}
