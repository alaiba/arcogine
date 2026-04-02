//! Sales agent: observes backlog, lead time, and revenue, then adjusts price.
//!
//! The agent is invoked synchronously on the simulation thread via
//! `AgentEvaluation` events. It reads state from a snapshot pushed by the
//! integrated handler before each invocation, and submits decisions by
//! scheduling `PriceChange` events on the `Scheduler`.

use sim_core::event::{Event, EventPayload};
use sim_core::handler::EventHandler;
use sim_core::queue::Scheduler;
use sim_types::SimError;

/// Read-only snapshot of simulation state for agent decision-making.
/// The integrated handler populates this before invoking the agent.
#[derive(Debug, Clone, Default)]
pub struct AgentObservation {
    pub backlog: usize,
    pub avg_lead_time: f64,
    pub total_revenue: f64,
    pub completed_sales: u64,
    pub current_price: f64,
    pub throughput: f64,
}

/// Configuration for the SalesAgent's decision policy.
#[derive(Debug, Clone)]
pub struct SalesAgentConfig {
    /// Backlog threshold above which the agent raises price.
    pub backlog_high: usize,
    /// Backlog threshold below which the agent lowers price.
    pub backlog_low: usize,
    /// Percentage to adjust price by (0.0 to 1.0).
    pub adjustment_pct: f64,
    /// Minimum allowed price.
    pub min_price: f64,
    /// Maximum allowed price.
    pub max_price: f64,
}

impl Default for SalesAgentConfig {
    fn default() -> Self {
        SalesAgentConfig {
            backlog_high: 10,
            backlog_low: 3,
            adjustment_pct: 0.10,
            min_price: 0.5,
            max_price: 100.0,
        }
    }
}

/// A sales agent that adjusts price based on backlog levels.
///
/// Decision policy:
/// - If backlog > backlog_high: raise price to reduce demand
/// - If backlog < backlog_low: lower price to increase demand
/// - Otherwise: hold price steady
///
/// The interface is trait-based and agent-type-agnostic, supporting future
/// agent types (Planning, Procurement, Maintenance) and LLM-based strategies.
#[derive(Debug, Clone)]
pub struct SalesAgent {
    pub config: SalesAgentConfig,
    pub observation: AgentObservation,
    pub interventions: u64,
}

impl SalesAgent {
    pub fn new(config: SalesAgentConfig) -> Self {
        SalesAgent {
            config,
            observation: AgentObservation::default(),
            interventions: 0,
        }
    }

    pub fn with_default_config() -> Self {
        Self::new(SalesAgentConfig::default())
    }

    /// Update the agent's observation of the simulation state.
    pub fn observe(&mut self, obs: AgentObservation) {
        self.observation = obs;
    }

    /// Compute the agent's price decision. Returns `Some(new_price)` if the
    /// agent decides to change price, or `None` to hold.
    pub fn decide(&self) -> Option<f64> {
        let obs = &self.observation;
        let current = obs.current_price;

        if obs.backlog > self.config.backlog_high {
            let new_price =
                (current * (1.0 + self.config.adjustment_pct)).min(self.config.max_price);
            if (new_price - current).abs() > f64::EPSILON {
                return Some(new_price);
            }
        } else if obs.backlog < self.config.backlog_low {
            let new_price =
                (current * (1.0 - self.config.adjustment_pct)).max(self.config.min_price);
            if (new_price - current).abs() > f64::EPSILON {
                return Some(new_price);
            }
        }
        None
    }
}

impl EventHandler for SalesAgent {
    fn handle_event(&mut self, event: &Event, scheduler: &mut Scheduler) -> Result<(), SimError> {
        if let EventPayload::AgentEvaluation = &event.payload {
            if let Some(new_price) = self.decide() {
                self.interventions += 1;
                scheduler.schedule(Event::new(
                    event.time,
                    EventPayload::PriceChange { new_price },
                ))?;
                scheduler.schedule(Event::new(
                    event.time,
                    EventPayload::AgentDecision {
                        description: format!(
                            "SalesAgent: backlog={}, price {:.2} -> {:.2}",
                            self.observation.backlog, self.observation.current_price, new_price
                        ),
                    },
                ))?;
            }
        }
        Ok(())
    }
}
