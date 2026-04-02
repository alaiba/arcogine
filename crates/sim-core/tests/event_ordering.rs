//! Tests for event ordering: monotonic time progression, priority queue
//! correctness, and rejection of out-of-order events.

use sim_core::event::{Event, EventPayload};
use sim_core::queue::Scheduler;
use sim_types::{ProductId, SimTime};

#[test]
fn events_dequeued_in_time_order() {
    let mut scheduler = Scheduler::new();

    scheduler
        .schedule(Event::new(SimTime(30), EventPayload::DemandEvaluation))
        .unwrap();
    scheduler
        .schedule(Event::new(SimTime(10), EventPayload::DemandEvaluation))
        .unwrap();
    scheduler
        .schedule(Event::new(SimTime(20), EventPayload::DemandEvaluation))
        .unwrap();

    let e1 = scheduler.next_event().unwrap();
    assert_eq!(e1.time, SimTime(10));

    let e2 = scheduler.next_event().unwrap();
    assert_eq!(e2.time, SimTime(20));

    let e3 = scheduler.next_event().unwrap();
    assert_eq!(e3.time, SimTime(30));

    assert!(scheduler.next_event().is_none());
}

#[test]
fn monotonic_time_progression() {
    let mut scheduler = Scheduler::new();

    scheduler
        .schedule(Event::new(
            SimTime(5),
            EventPayload::OrderCreation {
                product_id: ProductId(1),
                quantity: 1,
            },
        ))
        .unwrap();
    scheduler
        .schedule(Event::new(SimTime(10), EventPayload::DemandEvaluation))
        .unwrap();

    let e1 = scheduler.next_event().unwrap();
    assert_eq!(e1.time, SimTime(5));
    assert_eq!(scheduler.current_time(), SimTime(5));

    let e2 = scheduler.next_event().unwrap();
    assert_eq!(e2.time, SimTime(10));
    assert!(scheduler.current_time() >= SimTime(5));
}

#[test]
fn reject_past_time_events() {
    let mut scheduler = Scheduler::new();

    // Advance time by dequeuing an event
    scheduler
        .schedule(Event::new(SimTime(10), EventPayload::DemandEvaluation))
        .unwrap();
    let _ = scheduler.next_event().unwrap();

    // Now try to schedule an event in the past
    let result = scheduler.schedule(Event::new(SimTime(5), EventPayload::DemandEvaluation));

    assert!(result.is_err());
    match result.unwrap_err() {
        sim_types::SimError::EventOrderingViolation {
            expected_min,
            actual,
        } => {
            assert_eq!(expected_min, SimTime(10));
            assert_eq!(actual, SimTime(5));
        }
        other => panic!("expected EventOrderingViolation, got: {:?}", other),
    }
}

#[test]
fn same_time_events_are_accepted() {
    let mut scheduler = Scheduler::new();

    scheduler
        .schedule(Event::new(SimTime(10), EventPayload::DemandEvaluation))
        .unwrap();
    scheduler
        .schedule(Event::new(SimTime(10), EventPayload::AgentEvaluation))
        .unwrap();

    let e1 = scheduler.next_event().unwrap();
    assert_eq!(e1.time, SimTime(10));

    // After dequeuing time=10, scheduling another time=10 should work
    scheduler
        .schedule(Event::new(SimTime(10), EventPayload::DemandEvaluation))
        .unwrap();

    let e2 = scheduler.next_event().unwrap();
    assert_eq!(e2.time, SimTime(10));

    let e3 = scheduler.next_event().unwrap();
    assert_eq!(e3.time, SimTime(10));
}

#[test]
fn empty_scheduler_returns_none() {
    let mut scheduler = Scheduler::new();
    assert!(scheduler.next_event().is_none());
    assert!(scheduler.is_empty());
    assert_eq!(scheduler.len(), 0);
}
