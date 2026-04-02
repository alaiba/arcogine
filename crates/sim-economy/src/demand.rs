//! Demand model: generates orders based on price and delivery performance.
//!
//! The demand model is triggered by DemandEvaluation events. It reads the
//! current price and average lead time, samples the demand function, and
//! schedules OrderCreation events.

use rand::Rng;
use rand_chacha::ChaCha8Rng;
use sim_core::event::{Event, EventPayload};
use sim_core::handler::EventHandler;
use sim_core::queue::Scheduler;
use sim_types::{ProductId, SimError};

/// Demand model parameters.
#[derive(Debug, Clone)]
pub struct DemandModel {
    pub base_demand: f64,
    pub price_elasticity: f64,
    pub lead_time_sensitivity: f64,
    pub current_price: f64,
    pub avg_lead_time: f64,
    pub product_ids: Vec<ProductId>,
    pub rng: ChaCha8Rng,
}

impl DemandModel {
    pub fn new(
        base_demand: f64,
        price_elasticity: f64,
        lead_time_sensitivity: f64,
        initial_price: f64,
        product_ids: Vec<ProductId>,
        rng: ChaCha8Rng,
    ) -> Self {
        DemandModel {
            base_demand,
            price_elasticity,
            lead_time_sensitivity,
            current_price: initial_price,
            avg_lead_time: 0.0,
            product_ids,
            rng,
        }
    }

    /// Compute the expected number of orders given current conditions.
    /// demand = base_demand - price_elasticity * price - lead_time_sensitivity * lead_time
    pub fn compute_demand(&self) -> f64 {
        let demand = self.base_demand
            - self.price_elasticity * self.current_price
            - self.lead_time_sensitivity * self.avg_lead_time;
        demand.max(0.0)
    }

    /// Sample the demand function and generate order creation events.
    pub fn generate_orders(&mut self, scheduler: &mut Scheduler) -> Result<u64, SimError> {
        let expected = self.compute_demand();
        let current_time = scheduler.current_time();

        // Round to integer with stochastic rounding
        let base_orders = expected.floor() as u64;
        let fractional = expected - expected.floor();
        let extra = if self.rng.gen::<f64>() < fractional {
            1
        } else {
            0
        };
        let order_count = base_orders + extra;

        for _ in 0..order_count {
            if self.product_ids.is_empty() {
                break;
            }
            let product_idx = self.rng.gen_range(0..self.product_ids.len());
            let product_id = self.product_ids[product_idx];
            let quantity = self.rng.gen_range(1..=10);

            scheduler.schedule(Event::new(
                current_time,
                EventPayload::OrderCreation {
                    product_id,
                    quantity,
                },
            ))?;
        }

        Ok(order_count)
    }

    pub fn set_price(&mut self, price: f64) {
        self.current_price = price;
    }

    pub fn set_avg_lead_time(&mut self, lead_time: f64) {
        self.avg_lead_time = lead_time;
    }
}

impl EventHandler for DemandModel {
    fn handle_event(&mut self, event: &Event, scheduler: &mut Scheduler) -> Result<(), SimError> {
        if let EventPayload::DemandEvaluation = &event.payload {
            self.generate_orders(scheduler)?;
        }
        if let EventPayload::PriceChange { new_price } = &event.payload {
            self.set_price(*new_price);
        }
        Ok(())
    }
}
