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

#[cfg(test)]
mod tests {
    use super::*;
    use crate::machines::{Machine, MachineStore};
    use crate::routing::{Routing, RoutingStep, RoutingStore};
    use sim_core::event::{Event, EventPayload};
    use sim_core::handler::EventHandler;
    use sim_core::queue::Scheduler;

    fn one_machine_one_product() -> FactoryHandler {
        let mut machines = MachineStore::new();
        machines.add(Machine::new(MachineId(1), "Mill".into(), 1, None, 0));

        let mut routings = RoutingStore::new();
        routings.add_routing(Routing {
            id: 1,
            name: "Widget Route".into(),
            steps: vec![RoutingStep {
                step_id: 1,
                name: "Milling".into(),
                machine_id: MachineId(1),
                duration: 5,
            }],
        });
        routings.add_product_routing(ProductId(1), 1);

        FactoryHandler::new(machines, routings, vec![ProductId(1)])
    }

    fn two_step_handler() -> FactoryHandler {
        let mut machines = MachineStore::new();
        machines.add(Machine::new(MachineId(1), "Mill".into(), 1, None, 0));
        machines.add(Machine::new(MachineId(2), "Drill".into(), 1, None, 0));

        let mut routings = RoutingStore::new();
        routings.add_routing(Routing {
            id: 1,
            name: "Widget Route".into(),
            steps: vec![
                RoutingStep {
                    step_id: 1,
                    name: "Milling".into(),
                    machine_id: MachineId(1),
                    duration: 5,
                },
                RoutingStep {
                    step_id: 2,
                    name: "Drilling".into(),
                    machine_id: MachineId(2),
                    duration: 3,
                },
            ],
        });
        routings.add_product_routing(ProductId(1), 1);

        FactoryHandler::new(machines, routings, vec![ProductId(1)])
    }

    #[test]
    fn new_initializes_correctly() {
        let h = one_machine_one_product();
        assert_eq!(h.machines.iter().count(), 1);
        assert_eq!(h.product_ids.len(), 1);
        assert_eq!(h.total_revenue, 0.0);
        assert_eq!(h.completed_sales, 0);
    }

    #[test]
    fn backlog_counts_active_jobs() {
        let mut h = one_machine_one_product();
        let mut sched = Scheduler::new();

        let order = Event::new(
            SimTime(1),
            EventPayload::OrderCreation {
                product_id: ProductId(1),
                quantity: 1,
            },
        );
        sched.schedule(order.clone()).unwrap();
        sched.next_event();
        h.handle_event(&order, &mut sched).unwrap();
        assert_eq!(h.backlog(), 1);
    }

    #[test]
    fn avg_lead_time_zero_when_no_completed() {
        let h = one_machine_one_product();
        assert_eq!(h.avg_lead_time(), 0.0);
    }

    #[test]
    fn avg_lead_time_correct_for_completed_jobs() {
        let mut h = one_machine_one_product();
        let mut sched = Scheduler::new();

        let order = Event::new(
            SimTime(1),
            EventPayload::OrderCreation {
                product_id: ProductId(1),
                quantity: 1,
            },
        );
        sched.schedule(order.clone()).unwrap();
        sched.next_event();
        h.handle_event(&order, &mut sched).unwrap();

        let task_end = sched.next_event().unwrap();
        h.handle_event(&task_end, &mut sched).unwrap();

        assert_eq!(h.completed_sales, 1);
        assert!(h.avg_lead_time() > 0.0);
    }

    #[test]
    fn throughput_rate_division() {
        let mut h = one_machine_one_product();
        h.completed_sales = 10;
        assert_eq!(h.throughput(100), 0.1);
    }

    #[test]
    fn throughput_zero_when_zero_ticks() {
        let h = one_machine_one_product();
        assert_eq!(h.throughput(0), 0.0);
    }

    #[test]
    fn order_creation_creates_and_dispatches_job() {
        let mut h = one_machine_one_product();
        let mut sched = Scheduler::new();

        let order = Event::new(
            SimTime(1),
            EventPayload::OrderCreation {
                product_id: ProductId(1),
                quantity: 2,
            },
        );
        sched.schedule(order.clone()).unwrap();
        sched.next_event();
        h.handle_event(&order, &mut sched).unwrap();

        assert_eq!(h.jobs.iter().count(), 1);
        assert!(!sched.is_empty(), "should have scheduled TaskEnd");
    }

    #[test]
    fn order_creation_enqueues_when_machine_full() {
        let mut h = one_machine_one_product();
        let mut sched = Scheduler::new();

        let o1 = Event::new(
            SimTime(1),
            EventPayload::OrderCreation {
                product_id: ProductId(1),
                quantity: 1,
            },
        );
        sched.schedule(o1.clone()).unwrap();
        sched.next_event();
        h.handle_event(&o1, &mut sched).unwrap();

        let o2 = Event::new(
            SimTime(1),
            EventPayload::OrderCreation {
                product_id: ProductId(1),
                quantity: 1,
            },
        );
        h.handle_event(&o2, &mut sched).unwrap();

        assert_eq!(h.machines.get(MachineId(1)).unwrap().queue_depth(), 1);
    }

    #[test]
    fn task_end_completes_job_and_dequeues_next() {
        let mut h = one_machine_one_product();
        let mut sched = Scheduler::new();

        let o1 = Event::new(
            SimTime(1),
            EventPayload::OrderCreation {
                product_id: ProductId(1),
                quantity: 1,
            },
        );
        sched.schedule(o1.clone()).unwrap();
        sched.next_event();
        h.handle_event(&o1, &mut sched).unwrap();

        let o2 = Event::new(
            SimTime(1),
            EventPayload::OrderCreation {
                product_id: ProductId(1),
                quantity: 1,
            },
        );
        h.handle_event(&o2, &mut sched).unwrap();
        assert_eq!(h.machines.get(MachineId(1)).unwrap().queue_depth(), 1);

        let task_end = sched.next_event().unwrap();
        h.handle_event(&task_end, &mut sched).unwrap();
        assert_eq!(h.completed_sales, 1);
        assert_eq!(h.machines.get(MachineId(1)).unwrap().queue_depth(), 0);
    }

    #[test]
    fn multi_step_routing_advances_to_next_step() {
        let mut h = two_step_handler();
        let mut sched = Scheduler::new();

        let order = Event::new(
            SimTime(1),
            EventPayload::OrderCreation {
                product_id: ProductId(1),
                quantity: 1,
            },
        );
        sched.schedule(order.clone()).unwrap();
        sched.next_event();
        h.handle_event(&order, &mut sched).unwrap();

        let te1 = sched.next_event().unwrap();
        h.handle_event(&te1, &mut sched).unwrap();
        assert_eq!(h.completed_sales, 0, "should not be complete after step 1");

        let te2 = sched.next_event().unwrap();
        h.handle_event(&te2, &mut sched).unwrap();
        assert_eq!(h.completed_sales, 1, "should be complete after step 2");
    }

    #[test]
    fn machine_availability_dispatches_queued_on_online() {
        let mut h = one_machine_one_product();
        let mut sched = Scheduler::new();

        h.machines
            .get_mut(MachineId(1))
            .unwrap()
            .set_availability(false)
            .unwrap();

        let order = Event::new(
            SimTime(1),
            EventPayload::OrderCreation {
                product_id: ProductId(1),
                quantity: 1,
            },
        );
        sched.schedule(order.clone()).unwrap();
        sched.next_event();
        h.handle_event(&order, &mut sched).unwrap();
        assert_eq!(h.machines.get(MachineId(1)).unwrap().queue_depth(), 1);

        let online = Event::new(
            SimTime(2),
            EventPayload::MachineAvailabilityChange {
                machine_id: MachineId(1),
                online: true,
            },
        );
        sched.schedule(online.clone()).unwrap();
        sched.next_event();
        h.handle_event(&online, &mut sched).unwrap();
        assert_eq!(
            h.machines.get(MachineId(1)).unwrap().queue_depth(),
            0,
            "queued job should be dispatched"
        );
    }

    #[test]
    fn revenue_tracked_with_current_price() {
        let mut h = one_machine_one_product();
        let mut sched = Scheduler::new();
        h.set_current_price(10.0);

        let order = Event::new(
            SimTime(1),
            EventPayload::OrderCreation {
                product_id: ProductId(1),
                quantity: 3,
            },
        );
        sched.schedule(order.clone()).unwrap();
        sched.next_event();
        h.handle_event(&order, &mut sched).unwrap();

        let task_end = sched.next_event().unwrap();
        h.handle_event(&task_end, &mut sched).unwrap();

        assert_eq!(h.total_revenue, 30.0);
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
