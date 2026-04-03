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

#[cfg(test)]
mod tests {
    use super::*;
    use crate::event::{Event, EventPayload};
    use sim_types::{JobId, MachineId, ProductId};

    fn empty_log() -> EventLog {
        EventLog::new()
    }

    fn populated_log() -> EventLog {
        let mut log = EventLog::new();
        log.append(Event::new(
            SimTime(1),
            EventPayload::OrderCreation {
                product_id: ProductId(1),
                quantity: 5,
            },
        ));
        log.append(Event::new(
            SimTime(2),
            EventPayload::TaskStart {
                job_id: JobId(1),
                machine_id: MachineId(1),
                step_index: 0,
            },
        ));
        log.append(Event::new(
            SimTime(5),
            EventPayload::TaskEnd {
                job_id: JobId(1),
                machine_id: MachineId(1),
                step_index: 0,
            },
        ));
        log.append(Event::new(
            SimTime(6),
            EventPayload::OrderCreation {
                product_id: ProductId(2),
                quantity: 3,
            },
        ));
        log
    }

    #[test]
    fn total_simulated_time_on_empty_log() {
        let v = TotalSimulatedTime.compute(&empty_log(), SimTime::ZERO);
        assert_eq!(v.value, 0.0);
    }

    #[test]
    fn total_simulated_time_equals_ticks() {
        let v = TotalSimulatedTime.compute(&empty_log(), SimTime(100));
        assert_eq!(v.value, 100.0);
    }

    #[test]
    fn event_count_on_empty_log() {
        let v = EventCount.compute(&empty_log(), SimTime::ZERO);
        assert_eq!(v.value, 0.0);
    }

    #[test]
    fn event_count_counts_all_events() {
        let log = populated_log();
        let v = EventCount.compute(&log, SimTime(10));
        assert_eq!(v.value, 4.0);
    }

    #[test]
    fn throughput_rate_on_empty_log() {
        let v = ThroughputRate.compute(&empty_log(), SimTime::ZERO);
        assert_eq!(v.value, 0.0);
    }

    #[test]
    fn throughput_rate_computes_correctly() {
        let log = populated_log();
        let v = ThroughputRate.compute(&log, SimTime(10));
        assert_eq!(v.value, 1.0 / 10.0);
    }

    #[test]
    fn order_count_on_empty_log() {
        let v = OrderCount.compute(&empty_log(), SimTime::ZERO);
        assert_eq!(v.value, 0.0);
    }

    #[test]
    fn order_count_counts_order_creation_events() {
        let log = populated_log();
        let v = OrderCount.compute(&log, SimTime(10));
        assert_eq!(v.value, 2.0);
    }
}
