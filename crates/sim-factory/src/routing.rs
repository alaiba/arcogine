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
