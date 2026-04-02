//! Deterministic discrete-event simulation engine: event scheduling, dispatch,
//! logging, KPI computation, and scenario loading.

pub mod event;
pub mod handler;
pub mod kpi;
pub mod log;
pub mod queue;
pub mod runner;
pub mod scenario;
