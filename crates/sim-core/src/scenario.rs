//! Scenario loader: reads a TOML file, parses it into configuration structs,
//! validates references and value ranges, and constructs the initial simulation state.

use sim_types::scenario::ScenarioConfig;
use sim_types::SimError;
use std::collections::HashSet;

/// Load and parse a scenario from a TOML string.
pub fn load_scenario(toml_str: &str) -> Result<ScenarioConfig, SimError> {
    let config: ScenarioConfig =
        toml::from_str(toml_str).map_err(|e| SimError::ScenarioLoadError {
            message: format!("TOML parse error: {e}"),
        })?;

    validate_scenario(&config)?;
    Ok(config)
}

/// Load a scenario from a file path.
pub fn load_scenario_file(path: &str) -> Result<ScenarioConfig, SimError> {
    let contents = std::fs::read_to_string(path).map_err(|e| SimError::ScenarioLoadError {
        message: format!("cannot read file '{path}': {e}"),
    })?;
    load_scenario(&contents)
}

/// Validate a parsed scenario configuration.
fn validate_scenario(config: &ScenarioConfig) -> Result<(), SimError> {
    if config.simulation.max_ticks == 0 {
        return Err(SimError::OutOfRange {
            field: "simulation.max_ticks".to_string(),
            message: "must be > 0".to_string(),
        });
    }

    if config.equipment.is_empty() {
        return Err(SimError::ScenarioLoadError {
            message: "at least one [[equipment]] entry is required".to_string(),
        });
    }

    if config.material.is_empty() {
        return Err(SimError::ScenarioLoadError {
            message: "at least one [[material]] entry is required".to_string(),
        });
    }

    // Validate unique equipment IDs
    let mut equip_ids = HashSet::new();
    for eq in &config.equipment {
        if !equip_ids.insert(eq.id) {
            return Err(SimError::InvalidReference {
                message: format!("duplicate equipment id: {}", eq.id),
            });
        }
        if eq.concurrency == 0 {
            return Err(SimError::OutOfRange {
                field: format!("equipment[{}].concurrency", eq.id),
                message: "must be > 0".to_string(),
            });
        }
    }

    // Validate unique material IDs and routing references
    let mut material_ids = HashSet::new();
    let ops_ids: HashSet<u64> = config
        .operations_definition
        .iter()
        .map(|od| od.id)
        .collect();

    for mat in &config.material {
        if !material_ids.insert(mat.id) {
            return Err(SimError::InvalidReference {
                message: format!("duplicate material id: {}", mat.id),
            });
        }
        if !ops_ids.contains(&mat.routing_id) {
            return Err(SimError::InvalidReference {
                message: format!(
                    "material '{}' references nonexistent routing id: {}",
                    mat.name, mat.routing_id
                ),
            });
        }
    }

    // Validate process segments: equipment references
    let mut seg_ids = HashSet::new();
    for seg in &config.process_segment {
        if !seg_ids.insert(seg.id) {
            return Err(SimError::InvalidReference {
                message: format!("duplicate process_segment id: {}", seg.id),
            });
        }
        if !equip_ids.contains(&seg.equipment_id) {
            return Err(SimError::InvalidReference {
                message: format!(
                    "process_segment '{}' references nonexistent equipment id: {}",
                    seg.name, seg.equipment_id
                ),
            });
        }
        if seg.duration == 0 {
            return Err(SimError::OutOfRange {
                field: format!("process_segment[{}].duration", seg.id),
                message: "must be > 0".to_string(),
            });
        }
    }

    // Validate operations definitions: step references
    for od in &config.operations_definition {
        if od.steps.is_empty() {
            return Err(SimError::ScenarioLoadError {
                message: format!("operations_definition '{}' has no steps", od.name),
            });
        }
        for step_id in &od.steps {
            if !seg_ids.contains(step_id) {
                return Err(SimError::InvalidReference {
                    message: format!(
                        "operations_definition '{}' references nonexistent process_segment id: {}",
                        od.name, step_id
                    ),
                });
            }
        }
    }

    if let Some(econ) = &config.economy {
        if !econ.initial_price.is_finite()
            || econ.initial_price <= 0.0
            || econ.initial_price > 1_000_000.0
        {
            return Err(SimError::OutOfRange {
                field: "economy.initial_price".to_string(),
                message: "must be a finite number > 0 and <= 1,000,000".to_string(),
            });
        }
        if !econ.base_demand.is_finite() || econ.base_demand < 0.0 {
            return Err(SimError::OutOfRange {
                field: "economy.base_demand".to_string(),
                message: "must be a finite number >= 0".to_string(),
            });
        }
        if !econ.price_elasticity.is_finite() || econ.price_elasticity < 0.0 {
            return Err(SimError::OutOfRange {
                field: "economy.price_elasticity".to_string(),
                message: "must be a finite number >= 0".to_string(),
            });
        }
    }

    Ok(())
}
