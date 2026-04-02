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
