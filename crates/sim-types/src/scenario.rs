//! Scenario configuration structs — the `#[derive(Deserialize)]` types
//! representing the TOML schema. Multiple crates consume these types.
//!
//! TOML section names align with ISA-95 concepts where practical:
//! `[[equipment]]` for machines, `[[material]]` for products,
//! `[[process_segment]]` for routing steps.

use serde::{Deserialize, Serialize};

/// Top-level scenario configuration, deserialized from a TOML file.
#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
pub struct ScenarioConfig {
    pub simulation: SimulationParams,
    #[serde(default)]
    pub equipment: Vec<EquipmentConfig>,
    #[serde(default)]
    pub material: Vec<MaterialConfig>,
    #[serde(default)]
    pub process_segment: Vec<ProcessSegmentConfig>,
    #[serde(default)]
    pub operations_definition: Vec<OperationsDefinitionConfig>,
    #[serde(default)]
    pub economy: Option<EconomyConfig>,
    #[serde(default)]
    pub agent: Option<AgentConfig>,
}

/// Global simulation run parameters.
#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
pub struct SimulationParams {
    pub rng_seed: u64,
    pub max_ticks: u64,
    /// Interval (in ticks) between demand evaluation events.
    #[serde(default = "default_demand_interval")]
    pub demand_eval_interval: u64,
    /// Interval (in ticks) between agent evaluation events.
    #[serde(default = "default_agent_interval")]
    pub agent_eval_interval: u64,
}

fn default_demand_interval() -> u64 {
    10
}

fn default_agent_interval() -> u64 {
    50
}

/// Machine definition (ISA-95: Equipment / Work Unit).
#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
pub struct EquipmentConfig {
    pub id: u64,
    pub name: String,
    /// Number of concurrent jobs (default 1 for discrete manufacturing).
    #[serde(default = "default_concurrency")]
    pub concurrency: u32,
    /// Volume-based capacity in liters (Phase 7: batch/process manufacturing).
    #[serde(default)]
    pub capacity_liters: Option<f64>,
    /// Setup/cleaning time between jobs in ticks (default 0).
    #[serde(default)]
    pub setup_time: u64,
}

fn default_concurrency() -> u32 {
    1
}

/// Product definition (ISA-95: Material Definition).
#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
pub struct MaterialConfig {
    pub id: u64,
    pub name: String,
    /// Routing reference: which operations_definition this product uses.
    pub routing_id: u64,
}

/// A single step in a production routing (ISA-95: Process Segment).
#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
pub struct ProcessSegmentConfig {
    pub id: u64,
    pub name: String,
    /// Which equipment (machine) performs this step.
    pub equipment_id: u64,
    /// Processing duration in ticks.
    pub duration: u64,
}

/// Product routing — an ordered list of process segments (ISA-95: Operations Definition).
#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
pub struct OperationsDefinitionConfig {
    pub id: u64,
    pub name: String,
    /// Ordered list of process segment IDs that form this routing.
    pub steps: Vec<u64>,
}

/// Economy configuration: pricing and demand parameters.
#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
pub struct EconomyConfig {
    /// Initial price per unit.
    pub initial_price: f64,
    /// Base demand rate (orders per evaluation) at the reference price.
    #[serde(default = "default_base_demand")]
    pub base_demand: f64,
    /// Price elasticity: how much demand changes per unit price change.
    #[serde(default = "default_price_elasticity")]
    pub price_elasticity: f64,
    /// Lead-time sensitivity: how much demand falls as lead time increases.
    #[serde(default = "default_lead_time_sensitivity")]
    pub lead_time_sensitivity: f64,
}

fn default_base_demand() -> f64 {
    5.0
}

fn default_price_elasticity() -> f64 {
    0.5
}

fn default_lead_time_sensitivity() -> f64 {
    0.1
}

/// Agent configuration.
#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
pub struct AgentConfig {
    pub enabled: bool,
    /// Agent type identifier.
    #[serde(default = "default_agent_type")]
    pub agent_type: String,
}

fn default_agent_type() -> String {
    "sales".to_string()
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn default_demand_interval_value() {
        assert_eq!(default_demand_interval(), 10);
    }

    #[test]
    fn default_agent_interval_value() {
        assert_eq!(default_agent_interval(), 50);
    }

    #[test]
    fn default_base_demand_value() {
        assert_eq!(default_base_demand(), 5.0);
    }

    #[test]
    fn default_price_elasticity_value() {
        assert_eq!(default_price_elasticity(), 0.5);
    }

    #[test]
    fn default_lead_time_sensitivity_value() {
        assert_eq!(default_lead_time_sensitivity(), 0.1);
    }

    #[test]
    fn default_concurrency_value() {
        assert_eq!(default_concurrency(), 1);
    }

    #[test]
    fn default_agent_type_value() {
        assert_eq!(default_agent_type(), "sales");
    }

    #[test]
    fn scenario_config_serde_roundtrip() {
        let config = ScenarioConfig {
            simulation: SimulationParams {
                rng_seed: 42,
                max_ticks: 1000,
                demand_eval_interval: 10,
                agent_eval_interval: 50,
            },
            equipment: vec![EquipmentConfig {
                id: 1,
                name: "Lathe".into(),
                concurrency: 2,
                capacity_liters: None,
                setup_time: 0,
            }],
            material: vec![MaterialConfig {
                id: 1,
                name: "Widget".into(),
                routing_id: 1,
            }],
            process_segment: vec![ProcessSegmentConfig {
                id: 1,
                name: "Turn".into(),
                equipment_id: 1,
                duration: 5,
            }],
            operations_definition: vec![OperationsDefinitionConfig {
                id: 1,
                name: "Widget Route".into(),
                steps: vec![1],
            }],
            economy: Some(EconomyConfig {
                initial_price: 10.0,
                base_demand: 5.0,
                price_elasticity: 0.5,
                lead_time_sensitivity: 0.1,
            }),
            agent: Some(AgentConfig {
                enabled: true,
                agent_type: "sales".into(),
            }),
        };

        let toml_str = toml::to_string(&config).unwrap();
        let back: ScenarioConfig = toml::from_str(&toml_str).unwrap();
        assert_eq!(config, back);
    }

    #[test]
    fn partial_toml_fills_defaults() {
        let toml_str = r#"
[simulation]
rng_seed = 1
max_ticks = 100
"#;
        let config: ScenarioConfig = toml::from_str(toml_str).unwrap();
        assert_eq!(config.simulation.demand_eval_interval, 10);
        assert_eq!(config.simulation.agent_eval_interval, 50);
        assert!(config.equipment.is_empty());
        assert!(config.material.is_empty());
        assert!(config.process_segment.is_empty());
        assert!(config.operations_definition.is_empty());
        assert!(config.economy.is_none());
        assert!(config.agent.is_none());
    }

    #[test]
    fn equipment_defaults() {
        let toml_str = r#"
[simulation]
rng_seed = 1
max_ticks = 100

[[equipment]]
id = 1
name = "Mill"
"#;
        let config: ScenarioConfig = toml::from_str(toml_str).unwrap();
        let eq = &config.equipment[0];
        assert_eq!(eq.concurrency, 1);
        assert_eq!(eq.capacity_liters, None);
        assert_eq!(eq.setup_time, 0);
    }

    #[test]
    fn economy_defaults() {
        let toml_str = r#"
[simulation]
rng_seed = 1
max_ticks = 100

[economy]
initial_price = 20.0
"#;
        let config: ScenarioConfig = toml::from_str(toml_str).unwrap();
        let econ = config.economy.unwrap();
        assert_eq!(econ.base_demand, 5.0);
        assert_eq!(econ.price_elasticity, 0.5);
        assert_eq!(econ.lead_time_sensitivity, 0.1);
    }
}
