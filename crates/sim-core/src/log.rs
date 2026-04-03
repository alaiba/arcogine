//! Append-only event log for simulation observability and replay.
//!
//! Provides: `append`, `iter`, `filter_by_type`, `count`, and `snapshot`.
//! Phase 4 exposes this API over HTTP; Phases 2–3 tests use it directly.

use crate::event::{Event, EventType};
use serde::Serialize;

/// Append-only log of simulation events.
#[derive(Debug, Clone, Default, PartialEq, Serialize)]
pub struct EventLog {
    events: Vec<Event>,
}

impl EventLog {
    pub fn new() -> Self {
        EventLog { events: Vec::new() }
    }

    /// Append an event to the log.
    pub fn append(&mut self, event: Event) {
        self.events.push(event);
    }

    /// Iterate over all logged events.
    pub fn iter(&self) -> impl Iterator<Item = &Event> {
        self.events.iter()
    }

    /// Filter events by type.
    pub fn filter_by_type(&self, event_type: EventType) -> impl Iterator<Item = &Event> {
        self.events
            .iter()
            .filter(move |e| e.event_type == event_type)
    }

    /// Total number of logged events.
    pub fn count(&self) -> usize {
        self.events.len()
    }

    /// Clone the log for determinism comparison.
    pub fn snapshot(&self) -> EventLog {
        self.clone()
    }

    /// Get events as a slice.
    pub fn events(&self) -> &[Event] {
        &self.events
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::event::{Event, EventPayload};
    use sim_types::{ProductId, SimTime};

    fn make_order(t: u64) -> Event {
        Event::new(
            SimTime(t),
            EventPayload::OrderCreation {
                product_id: ProductId(1),
                quantity: 1,
            },
        )
    }

    #[test]
    fn new_log_is_empty() {
        let log = EventLog::new();
        assert_eq!(log.count(), 0);
    }

    #[test]
    fn append_increases_count() {
        let mut log = EventLog::new();
        log.append(make_order(1));
        assert_eq!(log.count(), 1);
        log.append(make_order(2));
        assert_eq!(log.count(), 2);
    }

    #[test]
    fn filter_by_type_returns_matching() {
        let mut log = EventLog::new();
        log.append(make_order(1));
        log.append(Event::new(SimTime(2), EventPayload::DemandEvaluation));
        log.append(make_order(3));

        let orders: Vec<_> = log.filter_by_type(EventType::OrderCreation).collect();
        assert_eq!(orders.len(), 2);

        let demands: Vec<_> = log.filter_by_type(EventType::DemandEvaluation).collect();
        assert_eq!(demands.len(), 1);
    }

    #[test]
    fn snapshot_returns_clone() {
        let mut log = EventLog::new();
        log.append(make_order(1));
        let snap = log.snapshot();
        assert_eq!(snap, log);
        assert_eq!(snap.count(), 1);
    }

    #[test]
    fn iter_yields_insertion_order() {
        let mut log = EventLog::new();
        log.append(make_order(10));
        log.append(make_order(20));
        log.append(make_order(5));

        let times: Vec<u64> = log.iter().map(|e| e.time.ticks()).collect();
        assert_eq!(times, vec![10, 20, 5]);
    }
}
