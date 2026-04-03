//! Property tests for sim-factory invariants using proptest.

use proptest::prelude::*;
use sim_factory::jobs::{Job, JobStore};
use sim_factory::machines::Machine;
use sim_types::{JobId, MachineId, ProductId, SimTime};

proptest! {
    #[test]
    fn job_current_step_never_exceeds_total(
        total_steps in 1usize..=10,
        completions in 0usize..=15,
    ) {
        let mut job = Job::new(JobId(1), ProductId(1), 1, total_steps, SimTime::ZERO);
        for i in 0..completions {
            if job.is_complete() {
                break;
            }
            let _ = job.start(MachineId(1));
            let _ = job.complete_step(SimTime(i as u64 + 1));
        }
        prop_assert!(job.current_step <= total_steps);
    }

    #[test]
    fn machine_active_jobs_never_exceeds_concurrency(
        concurrency in 1u32..=5,
        job_count in 0u32..=20,
    ) {
        let mut machine = Machine::new(MachineId(1), "M1".into(), concurrency, None, 0);
        let mut started = 0u32;
        for i in 0..job_count {
            let jid = JobId(i as u64 + 1);
            if machine.can_accept_job() {
                machine.start_job(jid).unwrap();
                started += 1;
            }
        }
        prop_assert!(machine.active_jobs.len() as u32 <= concurrency);
        prop_assert_eq!(started, concurrency.min(job_count));
    }

    #[test]
    fn queue_fifo_order(count in 0usize..=20) {
        let mut machine = Machine::new(MachineId(1), "M1".into(), 1, None, 0);
        machine.start_job(JobId(0)).unwrap();

        let ids: Vec<JobId> = (1..=count as u64).map(JobId).collect();
        for &id in &ids {
            machine.enqueue_job(id);
        }
        let mut dequeued = Vec::new();
        while let Some(id) = machine.dequeue_job() {
            dequeued.push(id);
        }
        prop_assert_eq!(dequeued, ids);
    }

    #[test]
    fn no_lost_jobs(
        created in 1usize..=20,
    ) {
        let mut store = JobStore::new();
        for _ in 0..created {
            store.create_job(ProductId(1), 1, 2, SimTime::ZERO);
        }

        let active = store.active_jobs().count();
        let completed = store.completed_jobs().count();
        let total = store.iter().count();

        prop_assert_eq!(total, created);
        prop_assert_eq!(active + completed, total);
    }
}
