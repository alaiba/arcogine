//! Agent layer: decision-making actors that observe simulation state and
//! submit commands through approved interfaces.
//!
//! All agents implement the `EventHandler` trait from `sim-core`. They run
//! synchronously on the simulation thread, invoked via `AgentEvaluation`
//! events. The trait-based architecture supports future agent types
//! (Planning, Procurement, Maintenance) and LLM-based strategy agents.

pub mod sales_agent;
