//! Append-only event log for simulation observability and replay.
//!
//! Provides: `append`, `iter`, `filter_by_type`, `count`, and `snapshot`.
//! Phase 4 exposes this API over HTTP; Phases 2–3 tests use it directly.

use crate::event::{Event, EventType};
use serde::Serialize;

/// Append-only log of simulation events with a configurable size cap.
///
/// `PartialEq` is implemented manually so that two logs with different
/// `max_capacity` values but identical events compare as equal. This
/// preserves determinism-test assertions (`assert_eq!` on event logs)
/// when capacity configuration differs.
#[derive(Debug, Clone, Serialize)]
pub struct EventLog {
    events: Vec<Event>,
    #[serde(skip)]
    max_capacity: usize,
}

impl Default for EventLog {
    fn default() -> Self {
        Self::new()
    }
}

impl PartialEq for EventLog {
    fn eq(&self, other: &Self) -> bool {
        self.events == other.events
    }
}

impl EventLog {
    pub fn new() -> Self {
        EventLog {
            events: Vec::new(),
            max_capacity: 1_000_000,
        }
    }

    pub fn with_capacity(max_capacity: usize) -> Self {
        EventLog {
            events: Vec::new(),
            max_capacity,
        }
    }

    /// Append an event to the log. Events beyond `max_capacity` are dropped.
    pub fn append(&mut self, event: Event) {
        if self.events.len() < self.max_capacity {
            self.events.push(event);
        }
    }

    /// Returns `true` if the log reached its capacity and is dropping events.
    pub fn is_truncated(&self) -> bool {
        self.events.len() >= self.max_capacity
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

    #[test]
    fn event_log_caps_at_max_capacity() {
        let mut log = EventLog::with_capacity(5);
        for i in 0..10 {
            log.append(make_order(i));
        }
        assert_eq!(log.count(), 5);
    }

    #[test]
    fn event_log_equality_ignores_capacity() {
        let mut log_a = EventLog::with_capacity(100);
        let mut log_b = EventLog::with_capacity(200);
        for i in 0..5 {
            log_a.append(make_order(i));
            log_b.append(make_order(i));
        }
        assert_eq!(log_a, log_b);
    }

    #[test]
    fn event_log_is_truncated() {
        let mut log = EventLog::with_capacity(3);
        assert!(!log.is_truncated());
        for i in 0..3 {
            log.append(make_order(i));
        }
        assert!(log.is_truncated());
        log.append(make_order(99));
        assert_eq!(log.count(), 3);
    }

    #[test]
    fn default_log_has_large_capacity() {
        let log = EventLog::default();
        assert_eq!(log.count(), 0);
        assert!(!log.is_truncated());
    }
}
