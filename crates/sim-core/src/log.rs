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
