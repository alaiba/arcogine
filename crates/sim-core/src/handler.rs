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

#[cfg(test)]
mod tests {
    use super::*;
    use crate::event::{Event, EventPayload};
    use sim_types::SimTime;
    use std::sync::{Arc, Mutex};

    struct TrackingHandler {
        calls: Arc<Mutex<Vec<SimTime>>>,
    }

    impl EventHandler for TrackingHandler {
        fn handle_event(
            &mut self,
            event: &Event,
            _scheduler: &mut Scheduler,
        ) -> Result<(), SimError> {
            self.calls.lock().unwrap().push(event.time);
            Ok(())
        }
    }

    struct FailingHandler;

    impl EventHandler for FailingHandler {
        fn handle_event(
            &mut self,
            _event: &Event,
            _scheduler: &mut Scheduler,
        ) -> Result<(), SimError> {
            Err(SimError::Other {
                message: "fail".into(),
            })
        }
    }

    fn make_event() -> Event {
        Event::new(SimTime(1), EventPayload::DemandEvaluation)
    }

    #[test]
    fn composite_dispatches_to_all_handlers() {
        let calls_a = Arc::new(Mutex::new(Vec::new()));
        let calls_b = Arc::new(Mutex::new(Vec::new()));
        let mut composite = CompositeHandler::new(vec![
            Box::new(TrackingHandler {
                calls: calls_a.clone(),
            }),
            Box::new(TrackingHandler {
                calls: calls_b.clone(),
            }),
        ]);
        let mut scheduler = Scheduler::new();
        composite
            .handle_event(&make_event(), &mut scheduler)
            .unwrap();
        assert_eq!(calls_a.lock().unwrap().len(), 1);
        assert_eq!(calls_b.lock().unwrap().len(), 1);
    }

    #[test]
    fn composite_propagates_first_err_and_short_circuits() {
        let calls = Arc::new(Mutex::new(Vec::new()));
        let mut composite = CompositeHandler::new(vec![
            Box::new(FailingHandler),
            Box::new(TrackingHandler {
                calls: calls.clone(),
            }),
        ]);
        let mut scheduler = Scheduler::new();
        let result = composite.handle_event(&make_event(), &mut scheduler);
        assert!(result.is_err());
        assert!(
            calls.lock().unwrap().is_empty(),
            "second handler should not be called"
        );
    }
}
