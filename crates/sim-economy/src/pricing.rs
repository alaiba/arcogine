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
