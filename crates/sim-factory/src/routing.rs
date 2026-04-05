//! Product routing definitions (ISA-95: Operations Definition / Process Segment).
//!
//! A routing defines the ordered sequence of steps a product goes through.
//! Each step runs on a specific machine for a specified duration.
//!
//! Design-for Phase 7: Steps accept generic durations and optional setup/cleaning
//! times, enabling time-based process steps without restructuring.

use serde::Serialize;
use sim_types::{MachineId, ProductId, SimError};

/// A single step in a routing (ISA-95: Process Segment).
#[derive(Debug, Clone, PartialEq, Eq, Serialize)]
pub struct RoutingStep {
    pub step_id: u64,
    pub name: String,
    pub machine_id: MachineId,
    pub duration: u64,
}

/// A product routing: an ordered list of steps (ISA-95: Operations Definition).
#[derive(Debug, Clone, PartialEq, Eq, Serialize)]
pub struct Routing {
    pub id: u64,
    pub name: String,
    pub steps: Vec<RoutingStep>,
}

impl Routing {
    /// Get the step at the given index.
    pub fn get_step(&self, index: usize) -> Option<&RoutingStep> {
        self.steps.get(index)
    }

    /// Number of steps in this routing.
    pub fn step_count(&self) -> usize {
        self.steps.len()
    }
}

/// Store of routings, indexed for lookup by routing ID.
#[derive(Debug, Clone, PartialEq, Eq, Serialize)]
pub struct RoutingStore {
    routings: Vec<Routing>,
    /// Maps ProductId -> routing index for fast lookup.
    product_routing: Vec<(ProductId, u64)>,
}

impl RoutingStore {
    pub fn new() -> Self {
        RoutingStore {
            routings: Vec::new(),
            product_routing: Vec::new(),
        }
    }

    pub fn add_routing(&mut self, routing: Routing) {
        self.routings.push(routing);
    }

    pub fn add_product_routing(&mut self, product_id: ProductId, routing_id: u64) {
        self.product_routing.push((product_id, routing_id));
    }

    /// Get the routing for a product.
    pub fn get_routing_for_product(&self, product_id: ProductId) -> Result<&Routing, SimError> {
        let routing_id = self
            .product_routing
            .iter()
            .find(|(pid, _)| *pid == product_id)
            .map(|(_, rid)| *rid)
            .ok_or_else(|| SimError::UnknownId {
                kind: "product routing".to_string(),
                id: product_id.0,
            })?;

        self.routings
            .iter()
            .find(|r| r.id == routing_id)
            .ok_or_else(|| SimError::UnknownId {
                kind: "routing".to_string(),
                id: routing_id,
            })
    }

    /// Get a routing by its ID.
    pub fn get_routing(&self, routing_id: u64) -> Result<&Routing, SimError> {
        self.routings
            .iter()
            .find(|r| r.id == routing_id)
            .ok_or_else(|| SimError::UnknownId {
                kind: "routing".to_string(),
                id: routing_id,
            })
    }
}

impl Default for RoutingStore {
    fn default() -> Self {
        Self::new()
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    fn sample_routing() -> Routing {
        Routing {
            id: 1,
            name: "Widget Route".into(),
            steps: vec![
                RoutingStep {
                    step_id: 1,
                    name: "Step A".into(),
                    machine_id: MachineId(1),
                    duration: 5,
                },
                RoutingStep {
                    step_id: 2,
                    name: "Step B".into(),
                    machine_id: MachineId(2),
                    duration: 3,
                },
            ],
        }
    }

    #[test]
    fn step_count_correct() {
        let r = sample_routing();
        assert_eq!(r.step_count(), 2);
    }

    #[test]
    fn get_step_out_of_bounds_is_none() {
        let r = sample_routing();
        assert!(r.get_step(5).is_none());
    }

    #[test]
    fn get_step_valid_index() {
        let r = sample_routing();
        let step = r.get_step(0).unwrap();
        assert_eq!(step.machine_id, MachineId(1));
    }

    #[test]
    fn routing_store_product_roundtrip() {
        let mut store = RoutingStore::new();
        store.add_routing(sample_routing());
        store.add_product_routing(ProductId(10), 1);
        let r = store.get_routing_for_product(ProductId(10)).unwrap();
        assert_eq!(r.name, "Widget Route");
    }

    #[test]
    fn routing_store_unknown_product_errors() {
        let store = RoutingStore::new();
        assert!(store.get_routing_for_product(ProductId(99)).is_err());
    }

    #[test]
    fn get_routing_by_id() {
        let mut store = RoutingStore::new();
        store.add_routing(sample_routing());
        let r = store.get_routing(1).unwrap();
        assert_eq!(r.name, "Widget Route");
    }

    #[test]
    fn get_routing_unknown_id_errors() {
        let store = RoutingStore::new();
        assert!(store.get_routing(42).is_err());
    }

    #[test]
    fn routing_store_default_is_empty() {
        let store = RoutingStore::default();
        assert!(store.get_routing(1).is_err());
    }

    #[test]
    fn product_routing_with_missing_routing_id_errors() {
        let mut store = RoutingStore::new();
        store.add_product_routing(ProductId(1), 999);
        assert!(store.get_routing_for_product(ProductId(1)).is_err());
    }
}
