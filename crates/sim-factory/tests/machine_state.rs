//! Tests for machine state management: job start/completion, queuing,
//! concurrency limits, and availability transitions.

use sim_factory::machines::Machine;
use sim_types::{JobId, MachineId, MachineState, SimError};

fn test_machine() -> Machine {
    Machine::new(MachineId(1), "TestMill".to_string(), 1, None, 0)
}

#[test]
fn new_machine_is_idle() {
    let m = test_machine();
    assert_eq!(m.state, MachineState::Idle);
    assert!(m.active_jobs.is_empty());
    assert_eq!(m.queue_depth(), 0);
}

#[test]
fn start_job_transitions_to_busy() {
    let mut m = test_machine();
    m.start_job(JobId(1)).unwrap();
    assert_eq!(m.state, MachineState::Busy);
    assert_eq!(m.active_jobs.len(), 1);
}

#[test]
fn complete_job_transitions_to_idle() {
    let mut m = test_machine();
    m.start_job(JobId(1)).unwrap();
    m.complete_job(JobId(1)).unwrap();
    assert_eq!(m.state, MachineState::Idle);
    assert!(m.active_jobs.is_empty());
}

#[test]
fn cannot_start_on_offline_machine() {
    let mut m = test_machine();
    m.set_availability(false).unwrap();
    assert_eq!(m.state, MachineState::Offline);

    let result = m.start_job(JobId(1));
    assert!(result.is_err());
    match result.unwrap_err() {
        SimError::InvalidStateTransition { context } => {
            assert!(context.contains("offline"));
        }
        other => panic!("expected InvalidStateTransition, got: {:?}", other),
    }
}

#[test]
fn cannot_exceed_concurrency() {
    let mut m = test_machine(); // concurrency = 1
    m.start_job(JobId(1)).unwrap();

    let result = m.start_job(JobId(2));
    assert!(result.is_err());
    match result.unwrap_err() {
        SimError::InvalidStateTransition { context } => {
            assert!(context.contains("concurrency"));
        }
        other => panic!("expected InvalidStateTransition, got: {:?}", other),
    }
}

#[test]
fn concurrent_machine_accepts_multiple_jobs() {
    let mut m = Machine::new(MachineId(1), "ParallelMill".to_string(), 3, None, 0);
    m.start_job(JobId(1)).unwrap();
    m.start_job(JobId(2)).unwrap();
    m.start_job(JobId(3)).unwrap();
    assert_eq!(m.active_jobs.len(), 3);
    assert_eq!(m.state, MachineState::Busy);

    let result = m.start_job(JobId(4));
    assert!(result.is_err());
}

#[test]
fn complete_nonexistent_job_returns_error() {
    let mut m = test_machine();
    m.start_job(JobId(1)).unwrap();

    let result = m.complete_job(JobId(999));
    assert!(result.is_err());
    match result.unwrap_err() {
        SimError::InvalidStateTransition { context } => {
            assert!(context.contains("999"));
        }
        other => panic!("expected InvalidStateTransition, got: {:?}", other),
    }
}

#[test]
fn cannot_go_offline_with_active_jobs() {
    let mut m = test_machine();
    m.start_job(JobId(1)).unwrap();

    let result = m.set_availability(false);
    assert!(result.is_err());
}

#[test]
fn queue_management() {
    let mut m = test_machine();
    m.enqueue_job(JobId(1));
    m.enqueue_job(JobId(2));
    assert_eq!(m.queue_depth(), 2);

    let j = m.dequeue_job().unwrap();
    assert_eq!(j, JobId(1));
    assert_eq!(m.queue_depth(), 1);

    let j = m.dequeue_job().unwrap();
    assert_eq!(j, JobId(2));
    assert!(m.dequeue_job().is_none());
}

#[test]
fn online_offline_toggle() {
    let mut m = test_machine();
    assert_eq!(m.state, MachineState::Idle);

    m.set_availability(false).unwrap();
    assert_eq!(m.state, MachineState::Offline);

    m.set_availability(true).unwrap();
    assert_eq!(m.state, MachineState::Idle);

    // Going online when already online is a no-op
    m.set_availability(true).unwrap();
    assert_eq!(m.state, MachineState::Idle);
}
