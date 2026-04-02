//! Job lifecycle management — tracking production orders through their routing.

use serde::Serialize;
use sim_types::{JobId, JobStatus, MachineId, ProductId, SimError, SimTime};

/// A production job: an instance of a product order being processed.
#[derive(Debug, Clone, PartialEq, Eq, Serialize)]
pub struct Job {
    pub id: JobId,
    pub product_id: ProductId,
    pub quantity: u64,
    pub status: JobStatus,
    /// Index of the current routing step (0-based).
    pub current_step: usize,
    /// Total number of routing steps for this job.
    pub total_steps: usize,
    /// Machine currently processing this job (if any).
    pub current_machine: Option<MachineId>,
    /// Time this job entered the system.
    pub created_at: SimTime,
    /// Time this job completed (if completed).
    pub completed_at: Option<SimTime>,
}

impl Job {
    pub fn new(
        id: JobId,
        product_id: ProductId,
        quantity: u64,
        total_steps: usize,
        created_at: SimTime,
    ) -> Self {
        Job {
            id,
            product_id,
            quantity,
            status: JobStatus::Queued,
            current_step: 0,
            total_steps,
            current_machine: None,
            created_at,
            completed_at: None,
        }
    }

    /// Start processing this job on a machine.
    pub fn start(&mut self, machine_id: MachineId) -> Result<(), SimError> {
        if self.status != JobStatus::Queued && self.status != JobStatus::InProgress {
            return Err(SimError::InvalidStateTransition {
                context: format!("cannot start job {} in state {:?}", self.id, self.status),
            });
        }
        self.status = JobStatus::InProgress;
        self.current_machine = Some(machine_id);
        Ok(())
    }

    /// Complete the current routing step.
    pub fn complete_step(&mut self, time: SimTime) -> Result<(), SimError> {
        if self.status != JobStatus::InProgress {
            return Err(SimError::InvalidStateTransition {
                context: format!(
                    "cannot complete step for job {} in state {:?}",
                    self.id, self.status
                ),
            });
        }
        self.current_machine = None;
        self.current_step += 1;

        if self.current_step >= self.total_steps {
            self.status = JobStatus::Completed;
            self.completed_at = Some(time);
        } else {
            self.status = JobStatus::Queued;
        }
        Ok(())
    }

    /// Lead time for completed jobs (ticks from creation to completion).
    pub fn lead_time(&self) -> Option<u64> {
        self.completed_at.map(|ct| ct - self.created_at)
    }

    /// Whether this job has completed all routing steps.
    pub fn is_complete(&self) -> bool {
        self.status == JobStatus::Completed
    }
}

/// Store for all jobs, keyed by `JobId`.
#[derive(Debug, Clone, PartialEq, Eq, Serialize)]
pub struct JobStore {
    jobs: Vec<Job>,
    next_id: u64,
}

impl JobStore {
    pub fn new() -> Self {
        JobStore {
            jobs: Vec::new(),
            next_id: 1,
        }
    }

    /// Create a new job and return its ID.
    pub fn create_job(
        &mut self,
        product_id: ProductId,
        quantity: u64,
        total_steps: usize,
        created_at: SimTime,
    ) -> JobId {
        let id = JobId(self.next_id);
        self.next_id += 1;
        let job = Job::new(id, product_id, quantity, total_steps, created_at);
        self.jobs.push(job);
        id
    }

    pub fn get(&self, id: JobId) -> Result<&Job, SimError> {
        self.jobs
            .iter()
            .find(|j| j.id == id)
            .ok_or_else(|| SimError::UnknownId {
                kind: "job".to_string(),
                id: id.0,
            })
    }

    pub fn get_mut(&mut self, id: JobId) -> Result<&mut Job, SimError> {
        self.jobs
            .iter_mut()
            .find(|j| j.id == id)
            .ok_or_else(|| SimError::UnknownId {
                kind: "job".to_string(),
                id: id.0,
            })
    }

    pub fn active_jobs(&self) -> impl Iterator<Item = &Job> {
        self.jobs
            .iter()
            .filter(|j| j.status == JobStatus::InProgress || j.status == JobStatus::Queued)
    }

    pub fn completed_jobs(&self) -> impl Iterator<Item = &Job> {
        self.jobs
            .iter()
            .filter(|j| j.status == JobStatus::Completed)
    }

    pub fn iter(&self) -> impl Iterator<Item = &Job> {
        self.jobs.iter()
    }
}

impl Default for JobStore {
    fn default() -> Self {
        Self::new()
    }
}
