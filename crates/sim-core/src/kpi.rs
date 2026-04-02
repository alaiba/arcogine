//! KPI (Key Performance Indicator) trait and implementations.
//!
//! Phase 2: `TotalSimulatedTime`, `EventCount` for deterministic validation.
//! Phase 3: domain KPIs aligned with ISO 22400 (SR EN ISO 22400).

use crate::event::EventType;
use crate::log::EventLog;
use serde::Serialize;
use sim_types::SimTime;

/// Trait for computing a KPI from the event log and simulation state.
pub trait Kpi {
    fn name(&self) -> &str;
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

/// Throughput rate (ISO 22400 KPI 1200): completed orders per unit time.
pub struct ThroughputRate;

impl Kpi for ThroughputRate {
    fn name(&self) -> &str {
        "throughput_rate"
    }

    fn compute(&self, log: &EventLog, current_time: SimTime) -> KpiValue {
        let completed = log.filter_by_type(EventType::TaskEnd).count();
        let elapsed = current_time.ticks().max(1) as f64;
        KpiValue {
            name: self.name().to_string(),
            value: completed as f64 / elapsed,
            unit: "task_completions/tick".to_string(),
        }
    }
}

/// Work-in-process: count of OrderCreation events minus TaskEnd events
/// that represent final routing steps (approximation from event log).
pub struct OrderCount;

impl Kpi for OrderCount {
    fn name(&self) -> &str {
        "order_count"
    }

    fn compute(&self, log: &EventLog, _current_time: SimTime) -> KpiValue {
        let orders = log.filter_by_type(EventType::OrderCreation).count();
        KpiValue {
            name: self.name().to_string(),
            value: orders as f64,
            unit: "orders".to_string(),
        }
    }
}
