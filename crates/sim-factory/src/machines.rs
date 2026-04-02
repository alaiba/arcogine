//! Machine state management (ISA-95: Equipment state tracking).
//!
//! Design-for Phase 7: Machine definitions include optional `capacity_liters`
//! and `setup_time` fields for batch/process manufacturing.

use serde::Serialize;
use sim_types::{JobId, MachineId, MachineState, SimError};
use std::collections::VecDeque;

/// Runtime state of a single machine.
#[derive(Debug, Clone, PartialEq, Serialize)]
pub struct Machine {
    pub id: MachineId,
    pub name: String,
    pub state: MachineState,
    /// Max concurrent jobs (default 1 for discrete).
    pub concurrency: u32,
    /// Currently processing job IDs.
    pub active_jobs: Vec<JobId>,
    /// Jobs waiting to be processed on this machine.
    pub queue: VecDeque<JobId>,
    /// Volume capacity in liters (Phase 7).
    pub capacity_liters: Option<f64>,
    /// Setup/cleaning time between jobs (ticks).
    pub setup_time: u64,
    /// Total ticks this machine has been busy (for utilization calculation).
    pub busy_ticks: u64,
}

impl Machine {
    pub fn new(
        id: MachineId,
        name: String,
        concurrency: u32,
        capacity_liters: Option<f64>,
        setup_time: u64,
    ) -> Self {
        Machine {
            id,
            name,
            state: MachineState::Idle,
            concurrency,
            active_jobs: Vec::new(),
            queue: VecDeque::new(),
            capacity_liters,
            setup_time,
            busy_ticks: 0,
        }
    }

    /// Check if this machine can accept a new job.
    pub fn can_accept_job(&self) -> bool {
        self.state != MachineState::Offline && (self.active_jobs.len() as u32) < self.concurrency
    }

    /// Start processing a job on this machine.
    pub fn start_job(&mut self, job_id: JobId) -> Result<(), SimError> {
        if self.state == MachineState::Offline {
            return Err(SimError::InvalidStateTransition {
                context: format!("cannot start job on offline machine {}", self.id),
            });
        }
        if (self.active_jobs.len() as u32) >= self.concurrency {
            return Err(SimError::InvalidStateTransition {
                context: format!(
                    "machine {} already at max concurrency ({})",
                    self.id, self.concurrency
                ),
            });
        }
        self.active_jobs.push(job_id);
        self.state = MachineState::Busy;
        Ok(())
    }

    /// Complete a job on this machine.
    pub fn complete_job(&mut self, job_id: JobId) -> Result<(), SimError> {
        let pos = self
            .active_jobs
            .iter()
            .position(|&j| j == job_id)
            .ok_or_else(|| SimError::InvalidStateTransition {
                context: format!("job {} not active on machine {}", job_id, self.id),
            })?;
        self.active_jobs.swap_remove(pos);
        if self.active_jobs.is_empty() {
            self.state = MachineState::Idle;
        }
        Ok(())
    }

    /// Enqueue a job to wait for this machine.
    pub fn enqueue_job(&mut self, job_id: JobId) {
        self.queue.push_back(job_id);
    }

    /// Dequeue the next waiting job.
    pub fn dequeue_job(&mut self) -> Option<JobId> {
        self.queue.pop_front()
    }

    /// Queue depth (number of waiting jobs).
    pub fn queue_depth(&self) -> usize {
        self.queue.len()
    }

    /// Set machine online/offline.
    pub fn set_availability(&mut self, online: bool) -> Result<(), SimError> {
        if online {
            if self.state == MachineState::Offline {
                self.state = MachineState::Idle;
            }
        } else {
            if !self.active_jobs.is_empty() {
                return Err(SimError::InvalidStateTransition {
                    context: format!(
                        "cannot take machine {} offline while {} jobs are active",
                        self.id,
                        self.active_jobs.len()
                    ),
                });
            }
            self.state = MachineState::Offline;
        }
        Ok(())
    }
}

/// Store for all machines, keyed by `MachineId`.
#[derive(Debug, Clone, PartialEq, Serialize)]
pub struct MachineStore {
    machines: Vec<Machine>,
}

impl MachineStore {
    pub fn new() -> Self {
        MachineStore {
            machines: Vec::new(),
        }
    }

    pub fn add(&mut self, machine: Machine) {
        self.machines.push(machine);
    }

    pub fn get(&self, id: MachineId) -> Result<&Machine, SimError> {
        self.machines
            .iter()
            .find(|m| m.id == id)
            .ok_or_else(|| SimError::UnknownId {
                kind: "machine".to_string(),
                id: id.0,
            })
    }

    pub fn get_mut(&mut self, id: MachineId) -> Result<&mut Machine, SimError> {
        self.machines
            .iter_mut()
            .find(|m| m.id == id)
            .ok_or_else(|| SimError::UnknownId {
                kind: "machine".to_string(),
                id: id.0,
            })
    }

    pub fn iter(&self) -> impl Iterator<Item = &Machine> {
        self.machines.iter()
    }
}

impl Default for MachineStore {
    fn default() -> Self {
        Self::new()
    }
}
