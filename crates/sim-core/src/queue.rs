//! Priority-queue-based event scheduler for deterministic event dispatch.

use crate::event::Event;
use sim_types::{SimError, SimTime};
use std::cmp::Ordering;
use std::collections::BinaryHeap;

/// Wrapper for reverse-ordering events by time (earliest first).
#[derive(Debug, Clone)]
struct TimedEvent(Event);

impl PartialEq for TimedEvent {
    fn eq(&self, other: &Self) -> bool {
        self.0.time == other.0.time
    }
}

impl Eq for TimedEvent {}

impl PartialOrd for TimedEvent {
    fn partial_cmp(&self, other: &Self) -> Option<Ordering> {
        Some(self.cmp(other))
    }
}

impl Ord for TimedEvent {
    fn cmp(&self, other: &Self) -> Ordering {
        // Reverse order: smallest time has highest priority.
        other.0.time.cmp(&self.0.time)
    }
}

/// The event scheduler — a min-heap priority queue ordered by simulation time.
#[derive(Debug, Clone)]
pub struct Scheduler {
    queue: BinaryHeap<TimedEvent>,
    current_time: SimTime,
}

impl Scheduler {
    pub fn new() -> Self {
        Scheduler {
            queue: BinaryHeap::new(),
            current_time: SimTime::ZERO,
        }
    }

    /// Schedule an event. The event time must be >= the current simulation time.
    pub fn schedule(&mut self, event: Event) -> Result<(), SimError> {
        if event.time < self.current_time {
            return Err(SimError::EventOrderingViolation {
                expected_min: self.current_time,
                actual: event.time,
            });
        }
        self.queue.push(TimedEvent(event));
        Ok(())
    }

    /// Dequeue the next event (earliest time). Returns `None` if the queue is empty.
    pub fn next_event(&mut self) -> Option<Event> {
        self.queue.pop().map(|te| {
            self.current_time = te.0.time;
            te.0
        })
    }

    /// Peek at the next event without dequeuing.
    pub fn peek_time(&self) -> Option<SimTime> {
        self.queue.peek().map(|te| te.0.time)
    }

    /// Current simulation time (time of the last dequeued event).
    pub fn current_time(&self) -> SimTime {
        self.current_time
    }

    /// Number of pending events.
    pub fn len(&self) -> usize {
        self.queue.len()
    }

    /// Whether the queue is empty.
    pub fn is_empty(&self) -> bool {
        self.queue.is_empty()
    }
}

impl Default for Scheduler {
    fn default() -> Self {
        Self::new()
    }
}
