//! KPI (Key Performance Indicator) trait and initial implementations.
//!
//! Phase 2 defines the trait and implements `TotalSimulatedTime` and `EventCount`
//! sufficient for deterministic replay validation. Phase 3 adds domain KPIs
//! (revenue, backlog, throughput, lead time, utilization) aligned with ISO 22400.

use crate::log::EventLog;
use serde::Serialize;
use sim_types::SimTime;

/// Trait for computing a KPI from the event log and simulation state.
pub trait Kpi {
    /// Human-readable name of the KPI.
    fn name(&self) -> &str;

    /// Compute the KPI value.
    fn compute(&self, log: &EventLog, current_time: SimTime) -> KpiValue;
}

/// A computed KPI value with metadata.
#[derive(Debug, Clone, PartialEq, Serialize)]
pub struct KpiValue {
    pub name: String,
    pub value: f64,
    pub unit: String,
}

/// Total simulated time elapsed (in ticks).
pub struct TotalSimulatedTime;

impl Kpi for TotalSimulatedTime {
    fn name(&self) -> &str {
        "total_simulated_time"
    }

    fn compute(&self, _log: &EventLog, current_time: SimTime) -> KpiValue {
        KpiValue {
            name: self.name().to_string(),
            value: current_time.ticks() as f64,
            unit: "ticks".to_string(),
        }
    }
}

/// Total number of events processed.
pub struct EventCount;

impl Kpi for EventCount {
    fn name(&self) -> &str {
        "event_count"
    }

    fn compute(&self, log: &EventLog, _current_time: SimTime) -> KpiValue {
        KpiValue {
            name: self.name().to_string(),
            value: log.count() as f64,
            unit: "events".to_string(),
        }
    }
}
