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

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn simtime_ticks_returns_inner() {
        assert_eq!(SimTime(42).ticks(), 42);
    }

    #[test]
    fn simtime_add_u64() {
        assert_eq!(SimTime(10) + 5, SimTime(15));
    }

    #[test]
    fn simtime_sub_produces_delta() {
        assert_eq!(SimTime(30) - SimTime(10), 20);
    }

    #[test]
    fn simtime_sub_saturates_at_zero() {
        assert_eq!(SimTime(5) - SimTime(10), 0);
    }

    #[test]
    fn simtime_zero_constant() {
        assert_eq!(SimTime::ZERO, SimTime(0));
    }

    #[test]
    fn quantity_units_roundtrip() {
        let q = Quantity::units(7);
        assert_eq!(q.as_units(), Some(7));
    }

    #[test]
    fn quantity_volume_not_units() {
        let q = Quantity::Volume { liters: 3.5 };
        assert_eq!(q.as_units(), None);
    }

    #[test]
    fn quantity_default_is_zero_units() {
        assert_eq!(Quantity::default(), Quantity::Units(0));
    }

    #[test]
    fn simerror_display_invalid_state_transition() {
        let e = SimError::InvalidStateTransition {
            context: "test".into(),
        };
        assert_eq!(e.to_string(), "invalid state transition: test");
    }

    #[test]
    fn simerror_display_unknown_id() {
        let e = SimError::UnknownId {
            kind: "machine".into(),
            id: 5,
        };
        assert_eq!(e.to_string(), "unknown machine id: 5");
    }

    #[test]
    fn simerror_display_event_ordering() {
        let e = SimError::EventOrderingViolation {
            expected_min: SimTime(10),
            actual: SimTime(5),
        };
        assert_eq!(
            e.to_string(),
            "event ordering violation: expected time >= t=10, got t=5"
        );
    }

    #[test]
    fn simerror_display_scenario_load() {
        let e = SimError::ScenarioLoadError {
            message: "bad toml".into(),
        };
        assert_eq!(e.to_string(), "scenario load error: bad toml");
    }

    #[test]
    fn simerror_display_invalid_reference() {
        let e = SimError::InvalidReference {
            message: "no such machine".into(),
        };
        assert_eq!(e.to_string(), "invalid reference: no such machine");
    }

    #[test]
    fn simerror_display_out_of_range() {
        let e = SimError::OutOfRange {
            field: "price".into(),
            message: "must be positive".into(),
        };
        assert_eq!(e.to_string(), "out of range (price): must be positive");
    }

    #[test]
    fn simerror_display_other() {
        let e = SimError::Other {
            message: "oops".into(),
        };
        assert_eq!(e.to_string(), "oops");
    }

    #[test]
    fn machine_state_serde_roundtrip() {
        for state in [MachineState::Idle, MachineState::Busy, MachineState::Offline] {
            let json = serde_json::to_string(&state).unwrap();
            let back: MachineState = serde_json::from_str(&json).unwrap();
            assert_eq!(state, back);
        }
    }

    #[test]
    fn job_status_serde_roundtrip() {
        for status in [
            JobStatus::Queued,
            JobStatus::InProgress,
            JobStatus::Completed,
            JobStatus::Cancelled,
        ] {
            let json = serde_json::to_string(&status).unwrap();
            let back: JobStatus = serde_json::from_str(&json).unwrap();
            assert_eq!(status, back);
        }
    }
}
