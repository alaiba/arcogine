//! Factory event handler: processes simulation events that affect
//! machines, jobs, and routing. Implements the EventHandler trait
//! from sim-core.

use sim_core::event::{Event, EventPayload};
use sim_core::handler::EventHandler;
use sim_core::queue::Scheduler;
use sim_types::{MachineId, ProductId, SimError, SimTime};

use crate::jobs::JobStore;
use crate::machines::MachineStore;
use crate::routing::RoutingStore;

/// Factory-layer event handler. Manages the flow of jobs through machines
/// according to their product routing.
pub struct FactoryHandler {
    pub machines: MachineStore,
    pub jobs: JobStore,
    pub routings: RoutingStore,
    /// Available product IDs for order creation.
    pub product_ids: Vec<ProductId>,
    /// Revenue accumulator: price at completion * quantity.
    pub total_revenue: f64,
    /// Number of completed sales.
    pub completed_sales: u64,
    /// Current price used for revenue calculations. Updated by the economy layer.
    current_price: f64,
}

impl FactoryHandler {
    pub fn new(
        machines: MachineStore,
        routings: RoutingStore,
        product_ids: Vec<ProductId>,
    ) -> Self {
        FactoryHandler {
            machines,
            jobs: JobStore::new(),
            routings,
            product_ids,
            total_revenue: 0.0,
            completed_sales: 0,
            current_price: 0.0,
        }
    }

    /// Try to dispatch the next queued job on a machine.
    fn try_dispatch_from_queue(
        &mut self,
        machine_id: MachineId,
        scheduler: &mut Scheduler,
        current_time: SimTime,
    ) -> Result<(), SimError> {
        let machine = self.machines.get_mut(machine_id)?;
        if !machine.can_accept_job() {
            return Ok(());
        }
        if let Some(job_id) = machine.dequeue_job() {
            let job = self.jobs.get(job_id)?;
            let step_index = job.current_step;
            let product_id = job.product_id;
            let routing = self.routings.get_routing_for_product(product_id)?;
            let step = routing
                .get_step(step_index)
                .ok_or_else(|| SimError::Other {
                    message: format!("step index {step_index} out of range for job {job_id}"),
                })?;
            let duration = step.duration;

            let machine = self.machines.get_mut(machine_id)?;
            machine.start_job(job_id)?;

            let job = self.jobs.get_mut(job_id)?;
            job.start(machine_id)?;

            scheduler.schedule(Event::new(
                current_time + duration,
                EventPayload::TaskStart {
                    job_id,
                    machine_id,
                    step_index,
                },
            ))?;

            scheduler.schedule(Event::new(
                current_time + duration,
                EventPayload::TaskEnd {
                    job_id,
                    machine_id,
                    step_index,
                },
            ))?;
        }
        Ok(())
    }

    fn handle_order_creation(
        &mut self,
        product_id: ProductId,
        quantity: u64,
        scheduler: &mut Scheduler,
        current_time: SimTime,
    ) -> Result<(), SimError> {
        let routing = self.routings.get_routing_for_product(product_id)?;
        let total_steps = routing.step_count();

        let job_id = self
            .jobs
            .create_job(product_id, quantity, total_steps, current_time);

        if let Some(first_step) = routing.get_step(0) {
            let machine_id = first_step.machine_id;
            let machine = self.machines.get_mut(machine_id)?;

            if machine.can_accept_job() {
                let duration = first_step.duration;
                machine.start_job(job_id)?;

                let job = self.jobs.get_mut(job_id)?;
                job.start(machine_id)?;

                scheduler.schedule(Event::new(
                    current_time + duration,
                    EventPayload::TaskEnd {
                        job_id,
                        machine_id,
                        step_index: 0,
                    },
                ))?;
            } else {
                machine.enqueue_job(job_id);
            }
        }

        Ok(())
    }

    fn handle_task_end(
        &mut self,
        job_id: sim_types::JobId,
        machine_id: MachineId,
        _step_index: usize,
        scheduler: &mut Scheduler,
        current_time: SimTime,
        current_price: f64,
    ) -> Result<(), SimError> {
        // Complete the job step
        let machine = self.machines.get_mut(machine_id)?;
        machine.complete_job(job_id)?;

        let job = self.jobs.get_mut(job_id)?;
        job.complete_step(current_time)?;

        if job.is_complete() {
            self.total_revenue += current_price * job.quantity as f64;
            self.completed_sales += 1;
        } else {
            // Advance to next routing step
            let next_step_index = job.current_step;
            let product_id = job.product_id;
            let routing = self.routings.get_routing_for_product(product_id)?;

            if let Some(next_step) = routing.get_step(next_step_index) {
                let next_machine_id = next_step.machine_id;
                let next_machine = self.machines.get_mut(next_machine_id)?;

                if next_machine.can_accept_job() {
                    let duration = next_step.duration;
                    next_machine.start_job(job_id)?;

                    let job = self.jobs.get_mut(job_id)?;
                    job.start(next_machine_id)?;

                    scheduler.schedule(Event::new(
                        current_time + duration,
                        EventPayload::TaskEnd {
                            job_id,
                            machine_id: next_machine_id,
                            step_index: next_step_index,
                        },
                    ))?;
                } else {
                    next_machine.enqueue_job(job_id);
                }
            }
        }

        // Try to dispatch queued jobs on the freed machine
        self.try_dispatch_from_queue(machine_id, scheduler, current_time)?;

        Ok(())
    }

    fn handle_machine_availability(
        &mut self,
        machine_id: MachineId,
        online: bool,
        scheduler: &mut Scheduler,
        current_time: SimTime,
    ) -> Result<(), SimError> {
        let machine = self.machines.get_mut(machine_id)?;
        machine.set_availability(online)?;

        if online {
            self.try_dispatch_from_queue(machine_id, scheduler, current_time)?;
        }

        Ok(())
    }

    /// Set the current price used for revenue calculations on job completion.
    pub fn set_current_price(&mut self, price: f64) {
        self.current_price = price;
    }

    /// Current backlog: number of active (queued + in-progress) jobs.
    pub fn backlog(&self) -> usize {
        self.jobs.active_jobs().count()
    }

    /// Average lead time for completed jobs.
    pub fn avg_lead_time(&self) -> f64 {
        let completed: Vec<_> = self.jobs.completed_jobs().collect();
        if completed.is_empty() {
            return 0.0;
        }
        let total: u64 = completed.iter().filter_map(|j| j.lead_time()).sum();
        total as f64 / completed.len() as f64
    }

    /// Throughput: completed jobs per tick elapsed.
    pub fn throughput(&self, elapsed_ticks: u64) -> f64 {
        if elapsed_ticks == 0 {
            return 0.0;
        }
        self.completed_sales as f64 / elapsed_ticks as f64
    }
}

impl EventHandler for FactoryHandler {
    fn handle_event(&mut self, event: &Event, scheduler: &mut Scheduler) -> Result<(), SimError> {
        match &event.payload {
            EventPayload::OrderCreation {
                product_id,
                quantity,
            } => {
                self.handle_order_creation(*product_id, *quantity, scheduler, event.time)?;
            }
            EventPayload::TaskEnd {
                job_id,
                machine_id,
                step_index,
            } => {
                self.handle_task_end(
                    *job_id,
                    *machine_id,
                    *step_index,
                    scheduler,
                    event.time,
                    self.current_price,
                )?;
            }
            EventPayload::MachineAvailabilityChange { machine_id, online } => {
                self.handle_machine_availability(*machine_id, *online, scheduler, event.time)?;
            }
            _ => {}
        }
        Ok(())
    }
}
