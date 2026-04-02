//! Tests for job lifecycle and routing: creation, step advancement,
//! completion, and error handling.

use sim_factory::jobs::{Job, JobStore};
use sim_factory::routing::{Routing, RoutingStep, RoutingStore};
use sim_types::{JobId, JobStatus, MachineId, ProductId, SimError, SimTime};

fn sample_routing() -> Routing {
    Routing {
        id: 1,
        name: "Widget Routing".to_string(),
        steps: vec![
            RoutingStep {
                step_id: 1,
                name: "Milling".to_string(),
                machine_id: MachineId(1),
                duration: 5,
            },
            RoutingStep {
                step_id: 2,
                name: "Turning".to_string(),
                machine_id: MachineId(2),
                duration: 3,
            },
        ],
    }
}

#[test]
fn new_job_is_queued() {
    let job = Job::new(JobId(1), ProductId(1), 10, 2, SimTime(0));
    assert_eq!(job.status, JobStatus::Queued);
    assert_eq!(job.current_step, 0);
    assert!(job.current_machine.is_none());
    assert!(job.completed_at.is_none());
}

#[test]
fn job_advances_through_steps() {
    let mut job = Job::new(JobId(1), ProductId(1), 10, 2, SimTime(0));

    // Start first step
    job.start(MachineId(1)).unwrap();
    assert_eq!(job.status, JobStatus::InProgress);
    assert_eq!(job.current_machine, Some(MachineId(1)));

    // Complete first step — not final, so returns to Queued
    job.complete_step(SimTime(5)).unwrap();
    assert_eq!(job.status, JobStatus::Queued);
    assert_eq!(job.current_step, 1);
    assert!(job.current_machine.is_none());

    // Start second step
    job.start(MachineId(2)).unwrap();
    assert_eq!(job.status, JobStatus::InProgress);

    // Complete second step — final step
    job.complete_step(SimTime(8)).unwrap();
    assert_eq!(job.status, JobStatus::Completed);
    assert_eq!(job.current_step, 2);
    assert!(job.is_complete());
}

#[test]
fn completed_job_has_lead_time() {
    let mut job = Job::new(JobId(1), ProductId(1), 10, 1, SimTime(10));
    job.start(MachineId(1)).unwrap();
    job.complete_step(SimTime(25)).unwrap();

    assert_eq!(job.lead_time(), Some(15));
}

#[test]
fn cannot_start_completed_job() {
    let mut job = Job::new(JobId(1), ProductId(1), 10, 1, SimTime(0));
    job.start(MachineId(1)).unwrap();
    job.complete_step(SimTime(5)).unwrap();
    assert_eq!(job.status, JobStatus::Completed);

    let result = job.start(MachineId(2));
    assert!(result.is_err());
}

#[test]
fn cannot_complete_step_when_queued() {
    let job = Job::new(JobId(1), ProductId(1), 10, 2, SimTime(0));
    assert_eq!(job.status, JobStatus::Queued);

    // Completing a step without starting should fail
    let mut job2 = job.clone();
    let result = job2.complete_step(SimTime(5));
    assert!(result.is_err());
}

#[test]
fn job_store_creates_unique_ids() {
    let mut store = JobStore::new();
    let id1 = store.create_job(ProductId(1), 10, 2, SimTime(0));
    let id2 = store.create_job(ProductId(1), 5, 2, SimTime(1));
    assert_ne!(id1, id2);
}

#[test]
fn job_store_unknown_id_returns_error() {
    let store = JobStore::new();
    let result = store.get(JobId(999));
    assert!(result.is_err());
    match result.unwrap_err() {
        SimError::UnknownId { kind, id } => {
            assert_eq!(kind, "job");
            assert_eq!(id, 999);
        }
        other => panic!("expected UnknownId, got: {:?}", other),
    }
}

#[test]
fn routing_store_lookup() {
    let mut store = RoutingStore::new();
    store.add_routing(sample_routing());
    store.add_product_routing(ProductId(1), 1);

    let routing = store.get_routing_for_product(ProductId(1)).unwrap();
    assert_eq!(routing.step_count(), 2);
    assert_eq!(routing.get_step(0).unwrap().name, "Milling");
    assert_eq!(routing.get_step(1).unwrap().name, "Turning");
}

#[test]
fn routing_store_unknown_product_returns_error() {
    let store = RoutingStore::new();
    let result = store.get_routing_for_product(ProductId(999));
    assert!(result.is_err());
}

#[test]
fn routing_steps_in_correct_order() {
    let routing = sample_routing();
    let step0 = routing.get_step(0).unwrap();
    let step1 = routing.get_step(1).unwrap();

    assert_eq!(step0.machine_id, MachineId(1));
    assert_eq!(step1.machine_id, MachineId(2));
    assert!(routing.get_step(2).is_none());
}
