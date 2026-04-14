//! Demand model: generates orders based on price and delivery performance.
//!
//! The demand model is triggered by DemandEvaluation events. It reads the
//! current price and average lead time, samples the demand function, and
//! schedules OrderCreation events.

use rand::rngs::ChaCha8Rng;
use rand::RngExt;
use sim_core::event::{Event, EventPayload};
use sim_core::handler::EventHandler;
use sim_core::queue::Scheduler;
use sim_types::{ProductId, SimError};

/// Demand model parameters.
#[derive(Debug)]
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
        let extra = if self.rng.random::<f64>() < fractional {
            1
        } else {
            0
        };
        let order_count = base_orders + extra;

        for _ in 0..order_count {
            if self.product_ids.is_empty() {
                break;
            }
            let product_idx = self.rng.random_range(0..self.product_ids.len());
            let product_id = self.product_ids[product_idx];
            let quantity = self.rng.random_range(1..=10);

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

#[cfg(test)]
mod tests {
    use super::*;
    use rand::SeedableRng;
    use sim_core::event::EventType;
    use sim_types::SimTime;

    fn make_model(base_demand: f64, price: f64) -> DemandModel {
        DemandModel::new(
            base_demand,
            0.5,
            0.0,
            price,
            vec![ProductId(1)],
            ChaCha8Rng::seed_from_u64(42),
        )
    }

    #[test]
    fn generate_orders_with_zero_demand_produces_none() {
        let mut model = make_model(0.0, 0.0);
        let mut sched = Scheduler::new();
        let count = model.generate_orders(&mut sched).unwrap();
        assert_eq!(count, 0);
    }

    #[test]
    fn generate_orders_schedules_order_creation_events() {
        let mut model = make_model(5.0, 1.0);
        let mut sched = Scheduler::new();
        let count = model.generate_orders(&mut sched).unwrap();
        assert!(count > 0);
        for _ in 0..count {
            let evt = sched.next_event().unwrap();
            assert_eq!(evt.event_type, EventType::OrderCreation);
        }
    }

    #[test]
    fn handle_event_ignores_non_relevant_events() {
        let mut model = make_model(5.0, 1.0);
        let mut sched = Scheduler::new();
        let event = Event::new(SimTime(1), EventPayload::AgentEvaluation);
        sched.schedule(event.clone()).unwrap();
        sched.next_event();
        model.handle_event(&event, &mut sched).unwrap();
        assert!(sched.is_empty());
    }

    #[test]
    fn handle_event_for_demand_evaluation_generates_orders() {
        let mut model = make_model(5.0, 1.0);
        let mut sched = Scheduler::new();
        let event = Event::new(SimTime(10), EventPayload::DemandEvaluation);
        sched.schedule(event.clone()).unwrap();
        sched.next_event();
        model.handle_event(&event, &mut sched).unwrap();
        assert!(!sched.is_empty());
    }

    #[test]
    fn handle_event_for_price_change_updates_price() {
        let mut model = make_model(5.0, 1.0);
        let mut sched = Scheduler::new();
        let event = Event::new(SimTime(1), EventPayload::PriceChange { new_price: 5.0 });
        sched.schedule(event.clone()).unwrap();
        sched.next_event();
        model.handle_event(&event, &mut sched).unwrap();
        assert_eq!(model.current_price, 5.0);
    }
}
