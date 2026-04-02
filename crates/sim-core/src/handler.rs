//! The `EventHandler` trait — the core integration point between `sim-core`
//! and domain crates. Domain crates implement this trait; the simulation
//! runner dispatches events to the handler.

use crate::event::Event;
use crate::queue::Scheduler;
use sim_types::SimError;

/// Trait for handling simulation events. Domain crates (`sim-factory`,
/// `sim-economy`, `sim-agents`) implement this to process events relevant
/// to their domain.
pub trait EventHandler {
    /// Process a single event. May schedule follow-up events via the scheduler.
    fn handle_event(&mut self, event: &Event, scheduler: &mut Scheduler) -> Result<(), SimError>;
}

/// A composite handler that delegates to multiple sub-handlers in order.
pub struct CompositeHandler {
    handlers: Vec<Box<dyn EventHandler>>,
}

impl CompositeHandler {
    pub fn new(handlers: Vec<Box<dyn EventHandler>>) -> Self {
        CompositeHandler { handlers }
    }
}

impl EventHandler for CompositeHandler {
    fn handle_event(&mut self, event: &Event, scheduler: &mut Scheduler) -> Result<(), SimError> {
        for handler in &mut self.handlers {
            handler.handle_event(event, scheduler)?;
        }
        Ok(())
    }
}
