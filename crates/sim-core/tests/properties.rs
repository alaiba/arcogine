//! Property tests using proptest: verify invariants hold across
//! randomized inputs.

use proptest::prelude::*;
use sim_core::event::{Event, EventPayload};
use sim_core::queue::Scheduler;
use sim_types::SimTime;

proptest! {
    /// Events are always dequeued in non-decreasing time order.
    #[test]
    fn monotonic_time_progression(
        times in prop::collection::vec(0u64..10000, 1..100)
    ) {
        let mut scheduler = Scheduler::new();
        for t in &times {
            scheduler.schedule(Event::new(
                SimTime(*t),
                EventPayload::DemandEvaluation,
            )).unwrap();
        }

        let mut last_time = 0u64;
        while let Some(event) = scheduler.next_event() {
            prop_assert!(event.time.ticks() >= last_time,
                "time went backwards: {} < {}", event.time.ticks(), last_time);
            last_time = event.time.ticks();
        }
    }

    /// All scheduled events are eventually dequeued (no event loss).
    #[test]
    fn no_event_loss(
        times in prop::collection::vec(0u64..10000, 1..100)
    ) {
        let count = times.len();
        let mut scheduler = Scheduler::new();
        for t in &times {
            scheduler.schedule(Event::new(
                SimTime(*t),
                EventPayload::DemandEvaluation,
            )).unwrap();
        }

        let mut dequeued = 0;
        while scheduler.next_event().is_some() {
            dequeued += 1;
        }
        prop_assert_eq!(dequeued, count, "lost events: scheduled {} but dequeued {}", count, dequeued);
    }

    /// Scheduling an event at or after current time never fails.
    #[test]
    fn scheduling_at_current_time_succeeds(
        base_time in 0u64..1000,
        offset in 0u64..1000,
    ) {
        let mut scheduler = Scheduler::new();
        // Advance to base_time
        if base_time > 0 {
            scheduler.schedule(Event::new(
                SimTime(base_time),
                EventPayload::DemandEvaluation,
            )).unwrap();
            let _ = scheduler.next_event();
        }

        // Schedule at base_time + offset (always >= current_time)
        let result = scheduler.schedule(Event::new(
            SimTime(base_time + offset),
            EventPayload::DemandEvaluation,
        ));
        prop_assert!(result.is_ok());
    }
}
