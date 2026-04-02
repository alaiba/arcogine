//! Shared types, typed IDs, error definitions, and scenario configuration for Arcogine.
//!
//! This crate is the foundation of the dependency graph — all other crates depend on it.
//! It contains no business logic, only data definitions.

pub mod scenario;

use serde::{Deserialize, Serialize};
use std::fmt;

// ---------------------------------------------------------------------------
// Typed IDs
// ---------------------------------------------------------------------------

/// Unique identifier for a machine (ISA-95: Equipment / Work Unit).
#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize, PartialOrd, Ord)]
pub struct MachineId(pub u64);

/// Unique identifier for a product (ISA-95: Material Definition).
#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize, PartialOrd, Ord)]
pub struct ProductId(pub u64);

/// Unique identifier for a job (a production order being processed).
#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize, PartialOrd, Ord)]
pub struct JobId(pub u64);

/// Reserved for Phase 7: unique identifier for a batch in process manufacturing.
#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize, PartialOrd, Ord)]
pub struct BatchId(pub u64);

impl fmt::Display for MachineId {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "Machine({})", self.0)
    }
}

impl fmt::Display for ProductId {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "Product({})", self.0)
    }
}

impl fmt::Display for JobId {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "Job({})", self.0)
    }
}

impl fmt::Display for BatchId {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "Batch({})", self.0)
    }
}

// ---------------------------------------------------------------------------
// Simulation Time
// ---------------------------------------------------------------------------

/// Simulation time in discrete ticks. Monotonically increasing.
#[derive(Debug, Clone, Copy, PartialEq, Eq, PartialOrd, Ord, Hash, Serialize, Deserialize)]
pub struct SimTime(pub u64);

impl SimTime {
    pub const ZERO: SimTime = SimTime(0);

    pub fn ticks(self) -> u64 {
        self.0
    }
}

impl fmt::Display for SimTime {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "t={}", self.0)
    }
}

impl std::ops::Add<u64> for SimTime {
    type Output = SimTime;
    fn add(self, rhs: u64) -> SimTime {
        SimTime(self.0 + rhs)
    }
}

impl std::ops::Sub for SimTime {
    type Output = u64;
    fn sub(self, rhs: SimTime) -> u64 {
        self.0.saturating_sub(rhs.0)
    }
}

// ---------------------------------------------------------------------------
// Quantity — supports both discrete units and volumes (Phase 7 extensibility)
// ---------------------------------------------------------------------------

/// Quantity type supporting discrete units and volume-based measurements.
/// Phase 7 batch/process manufacturing uses `Volume`; MVP uses `Units`.
#[derive(Debug, Clone, Copy, PartialEq, Serialize, Deserialize)]
pub enum Quantity {
    Units(u64),
    Volume { liters: f64 },
}

impl Quantity {
    pub fn units(n: u64) -> Self {
        Quantity::Units(n)
    }

    pub fn as_units(&self) -> Option<u64> {
        match self {
            Quantity::Units(n) => Some(*n),
            Quantity::Volume { .. } => None,
        }
    }
}

impl Default for Quantity {
    fn default() -> Self {
        Quantity::Units(0)
    }
}

// ---------------------------------------------------------------------------
// Machine State
// ---------------------------------------------------------------------------

/// Operational state of a machine (ISA-95: Equipment State).
#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub enum MachineState {
    Idle,
    Busy,
    Offline,
}

// ---------------------------------------------------------------------------
// Job Status
// ---------------------------------------------------------------------------

/// Lifecycle status of a production job.
#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub enum JobStatus {
    Queued,
    InProgress,
    Completed,
    Cancelled,
}

// ---------------------------------------------------------------------------
// Error Types
// ---------------------------------------------------------------------------

/// Shared error type for the simulation.
#[derive(Debug, Clone, PartialEq, Eq, Serialize)]
pub enum SimError {
    /// Attempted an invalid state transition (e.g., starting a job on a busy machine).
    InvalidStateTransition { context: String },
    /// Referenced an ID that does not exist.
    UnknownId { kind: String, id: u64 },
    /// Events were submitted out of temporal order.
    EventOrderingViolation {
        expected_min: SimTime,
        actual: SimTime,
    },
    /// Scenario file is malformed or missing required fields.
    ScenarioLoadError { message: String },
    /// A scenario references a nonexistent machine, product, or routing.
    InvalidReference { message: String },
    /// A value in the scenario is outside its valid range.
    OutOfRange { field: String, message: String },
    /// Generic simulation error.
    Other { message: String },
}

impl fmt::Display for SimError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            SimError::InvalidStateTransition { context } => {
                write!(f, "invalid state transition: {context}")
            }
            SimError::UnknownId { kind, id } => write!(f, "unknown {kind} id: {id}"),
            SimError::EventOrderingViolation {
                expected_min,
                actual,
            } => {
                write!(
                    f,
                    "event ordering violation: expected time >= {expected_min}, got {actual}"
                )
            }
            SimError::ScenarioLoadError { message } => {
                write!(f, "scenario load error: {message}")
            }
            SimError::InvalidReference { message } => write!(f, "invalid reference: {message}"),
            SimError::OutOfRange { field, message } => {
                write!(f, "out of range ({field}): {message}")
            }
            SimError::Other { message } => write!(f, "{message}"),
        }
    }
}

impl std::error::Error for SimError {}
