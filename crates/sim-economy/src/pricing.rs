//! Pricing module: holds the current price and exposes it to the demand model.

use serde::Serialize;
use sim_core::event::{Event, EventPayload};
use sim_core::handler::EventHandler;
use sim_core::queue::Scheduler;
use sim_types::SimError;

/// Tracks the current price and price history.
#[derive(Debug, Clone, Serialize)]
pub struct PricingState {
    pub current_price: f64,
    pub price_history: Vec<(u64, f64)>,
}

impl PricingState {
    pub fn new(initial_price: f64) -> Self {
        PricingState {
            current_price: initial_price,
            price_history: vec![(0, initial_price)],
        }
    }

    pub fn set_price(&mut self, price: f64, tick: u64) {
        self.current_price = price;
        self.price_history.push((tick, price));
    }
}

impl EventHandler for PricingState {
    fn handle_event(&mut self, event: &Event, _scheduler: &mut Scheduler) -> Result<(), SimError> {
        if let EventPayload::PriceChange { new_price } = &event.payload {
            self.set_price(*new_price, event.time.ticks());
        }
        Ok(())
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use sim_types::SimTime;

    #[test]
    fn price_change_updates_price_and_history() {
        let mut ps = PricingState::new(10.0);
        let mut sched = Scheduler::new();
        let event = Event::new(SimTime(5), EventPayload::PriceChange { new_price: 15.0 });
        sched.schedule(event.clone()).unwrap();
        sched.next_event();
        ps.handle_event(&event, &mut sched).unwrap();
        assert_eq!(ps.current_price, 15.0);
        assert_eq!(ps.price_history.len(), 2);
        assert_eq!(ps.price_history[1], (5, 15.0));
    }

    #[test]
    fn ignores_non_price_change_events() {
        let mut ps = PricingState::new(10.0);
        let mut sched = Scheduler::new();
        let event = Event::new(SimTime(1), EventPayload::DemandEvaluation);
        sched.schedule(event.clone()).unwrap();
        sched.next_event();
        ps.handle_event(&event, &mut sched).unwrap();
        assert_eq!(ps.current_price, 10.0);
        assert_eq!(ps.price_history.len(), 1);
    }
}
