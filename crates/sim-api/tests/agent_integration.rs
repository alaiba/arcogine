//! Agent integration tests: verifies that the SalesAgent produces at least one
//! intervention and measurably reduces backlog growth under an overload scenario.
//! Placed in sim-api because the test requires all domain crates (F53).

use rand::SeedableRng;
use rand_chacha::ChaCha8Rng;
use sim_agents::sales_agent::{AgentObservation, SalesAgent, SalesAgentConfig};
use sim_core::event::{Event, EventPayload, EventType};
use sim_core::handler::EventHandler;
use sim_core::queue::Scheduler;
use sim_core::runner::run_scenario;
use sim_core::scenario::load_scenario;
use sim_economy::demand::DemandModel;
use sim_economy::pricing::PricingState;
use sim_factory::machines::{Machine, MachineStore};
use sim_factory::process::FactoryHandler;
use sim_factory::routing::{Routing, RoutingStep, RoutingStore};
use sim_types::{MachineId, ProductId, SimError};

fn overload_toml() -> &'static str {
    r#"
[simulation]
rng_seed = 42
max_ticks = 500
demand_eval_interval = 10
agent_eval_interval = 25

[[equipment]]
id = 1
name = "Mill"

[[material]]
id = 1
name = "Widget"
routing_id = 1

[[process_segment]]
id = 1
name = "Milling"
equipment_id = 1
duration = 10

[[operations_definition]]
id = 1
name = "Widget routing"
steps = [1]

[economy]
initial_price = 2.0
base_demand = 8.0
price_elasticity = 0.5
lead_time_sensitivity = 0.0

[agent]
enabled = true
agent_type = "sales"
"#
}

fn build_handlers(
    toml_str: &str,
) -> (
    sim_types::scenario::ScenarioConfig,
    FactoryHandler,
    DemandModel,
    PricingState,
) {
    let config = load_scenario(toml_str).unwrap();

    let mut machines = MachineStore::new();
    for eq in &config.equipment {
        machines.add(Machine::new(
            MachineId(eq.id),
            eq.name.clone(),
            eq.concurrency,
            eq.capacity_liters,
            eq.setup_time,
        ));
    }

    let mut routings = RoutingStore::new();
    for od in &config.operations_definition {
        let steps: Vec<RoutingStep> = od
            .steps
            .iter()
            .filter_map(|seg_id| {
                config
                    .process_segment
                    .iter()
                    .find(|s| s.id == *seg_id)
                    .map(|s| RoutingStep {
                        step_id: s.id,
                        name: s.name.clone(),
                        machine_id: MachineId(s.equipment_id),
                        duration: s.duration,
                    })
            })
            .collect();
        routings.add_routing(Routing {
            id: od.id,
            name: od.name.clone(),
            steps,
        });
    }

    let product_ids: Vec<ProductId> = config.material.iter().map(|m| ProductId(m.id)).collect();
    for mat in &config.material {
        routings.add_product_routing(ProductId(mat.id), mat.routing_id);
    }

    let factory = FactoryHandler::new(machines, routings, product_ids.clone());

    let econ = config.economy.as_ref().unwrap();
    let rng = ChaCha8Rng::seed_from_u64(config.simulation.rng_seed);
    let demand = DemandModel::new(
        econ.base_demand,
        econ.price_elasticity,
        econ.lead_time_sensitivity,
        econ.initial_price,
        product_ids,
        rng,
    );
    let pricing = PricingState::new(econ.initial_price);

    (config, factory, demand, pricing)
}

/// Integrated handler with agent for testing.
struct AgentIntegratedHandler {
    factory: FactoryHandler,
    demand: DemandModel,
    pricing: PricingState,
    agent: SalesAgent,
    agent_enabled: bool,
}

impl EventHandler for AgentIntegratedHandler {
    fn handle_event(&mut self, event: &Event, scheduler: &mut Scheduler) -> Result<(), SimError> {
        self.pricing.handle_event(event, scheduler)?;
        self.demand.current_price = self.pricing.current_price;
        self.demand.avg_lead_time = self.factory.avg_lead_time();
        self.demand.handle_event(event, scheduler)?;

        match &event.payload {
            EventPayload::TaskEnd {
                job_id,
                machine_id,
                step_index: _,
            } => {
                let machine = self.factory.machines.get_mut(*machine_id)?;
                machine.complete_job(*job_id)?;

                let job = self.factory.jobs.get_mut(*job_id)?;
                job.complete_step(event.time)?;

                if job.is_complete() {
                    self.factory.total_revenue += self.pricing.current_price * job.quantity as f64;
                    self.factory.completed_sales += 1;
                } else {
                    let next_step = job.current_step;
                    let product_id = job.product_id;
                    let routing = self.factory.routings.get_routing_for_product(product_id)?;
                    if let Some(step) = routing.get_step(next_step) {
                        let next_machine_id = step.machine_id;
                        let duration = step.duration;
                        let next_machine = self.factory.machines.get_mut(next_machine_id)?;
                        if next_machine.can_accept_job() {
                            next_machine.start_job(*job_id)?;
                            let job = self.factory.jobs.get_mut(*job_id)?;
                            job.start(next_machine_id)?;
                            scheduler.schedule(Event::new(
                                event.time + duration,
                                EventPayload::TaskEnd {
                                    job_id: *job_id,
                                    machine_id: next_machine_id,
                                    step_index: next_step,
                                },
                            ))?;
                        } else {
                            next_machine.enqueue_job(*job_id);
                        }
                    }
                }

                let machine = self.factory.machines.get_mut(*machine_id)?;
                if let Some(queued_job_id) = machine.dequeue_job() {
                    let qjob = self.factory.jobs.get(queued_job_id)?;
                    let qstep = qjob.current_step;
                    let qpid = qjob.product_id;
                    let routing = self.factory.routings.get_routing_for_product(qpid)?;
                    if let Some(step) = routing.get_step(qstep) {
                        let duration = step.duration;
                        let machine = self.factory.machines.get_mut(*machine_id)?;
                        machine.start_job(queued_job_id)?;
                        let qjob = self.factory.jobs.get_mut(queued_job_id)?;
                        qjob.start(*machine_id)?;
                        scheduler.schedule(Event::new(
                            event.time + duration,
                            EventPayload::TaskEnd {
                                job_id: queued_job_id,
                                machine_id: *machine_id,
                                step_index: qstep,
                            },
                        ))?;
                    }
                }
            }
            EventPayload::OrderCreation {
                product_id,
                quantity,
            } => {
                let routing = self.factory.routings.get_routing_for_product(*product_id)?;
                let total_steps = routing.step_count();
                let job_id =
                    self.factory
                        .jobs
                        .create_job(*product_id, *quantity, total_steps, event.time);

                if let Some(first_step) = routing.get_step(0) {
                    let machine_id = first_step.machine_id;
                    let duration = first_step.duration;
                    let machine = self.factory.machines.get_mut(machine_id)?;
                    if machine.can_accept_job() {
                        machine.start_job(job_id)?;
                        let job = self.factory.jobs.get_mut(job_id)?;
                        job.start(machine_id)?;
                        scheduler.schedule(Event::new(
                            event.time + duration,
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
            }
            EventPayload::MachineAvailabilityChange { machine_id, online } => {
                self.factory
                    .machines
                    .get_mut(*machine_id)?
                    .set_availability(*online)?;
            }
            EventPayload::AgentEvaluation => {
                if self.agent_enabled {
                    let elapsed = scheduler.current_time().ticks().max(1);
                    self.agent.observe(AgentObservation {
                        backlog: self.factory.backlog(),
                        avg_lead_time: self.factory.avg_lead_time(),
                        total_revenue: self.factory.total_revenue,
                        completed_sales: self.factory.completed_sales,
                        current_price: self.pricing.current_price,
                        throughput: self.factory.throughput(elapsed),
                    });
                    self.agent.handle_event(event, scheduler)?;
                }
            }
            _ => {}
        }

        Ok(())
    }
}

#[test]
fn agent_produces_at_least_one_intervention() {
    let (config, factory, demand, pricing) = build_handlers(overload_toml());

    let agent = SalesAgent::new(SalesAgentConfig {
        backlog_high: 5,
        backlog_low: 2,
        adjustment_pct: 0.15,
        min_price: 0.5,
        max_price: 50.0,
    });

    let mut handler = AgentIntegratedHandler {
        factory,
        demand,
        pricing,
        agent,
        agent_enabled: true,
    };

    let result = run_scenario(&config, &mut handler).unwrap();

    assert!(
        handler.agent.interventions > 0,
        "agent should have intervened at least once, got {} interventions",
        handler.agent.interventions
    );

    let agent_decisions = result
        .event_log
        .filter_by_type(EventType::AgentDecision)
        .count();
    assert!(
        agent_decisions > 0,
        "should have at least one AgentDecision event logged"
    );
}

#[test]
fn agent_reduces_backlog_vs_fixed_price_baseline() {
    let toml = overload_toml();

    // Run without agent (fixed price)
    let (config_no_agent, factory_na, demand_na, pricing_na) = build_handlers(toml);
    struct NoAgentHandler {
        factory: FactoryHandler,
        demand: DemandModel,
        pricing: PricingState,
    }
    impl EventHandler for NoAgentHandler {
        fn handle_event(
            &mut self,
            event: &Event,
            scheduler: &mut Scheduler,
        ) -> Result<(), SimError> {
            self.pricing.handle_event(event, scheduler)?;
            self.demand.current_price = self.pricing.current_price;
            self.demand.avg_lead_time = self.factory.avg_lead_time();
            self.demand.handle_event(event, scheduler)?;

            match &event.payload {
                EventPayload::TaskEnd {
                    job_id,
                    machine_id,
                    step_index: _,
                } => {
                    let machine = self.factory.machines.get_mut(*machine_id)?;
                    machine.complete_job(*job_id)?;
                    let job = self.factory.jobs.get_mut(*job_id)?;
                    job.complete_step(event.time)?;
                    if job.is_complete() {
                        self.factory.total_revenue +=
                            self.pricing.current_price * job.quantity as f64;
                        self.factory.completed_sales += 1;
                    } else {
                        let next_step = job.current_step;
                        let product_id = job.product_id;
                        let routing = self.factory.routings.get_routing_for_product(product_id)?;
                        if let Some(step) = routing.get_step(next_step) {
                            let next_machine_id = step.machine_id;
                            let duration = step.duration;
                            let next_machine = self.factory.machines.get_mut(next_machine_id)?;
                            if next_machine.can_accept_job() {
                                next_machine.start_job(*job_id)?;
                                let job = self.factory.jobs.get_mut(*job_id)?;
                                job.start(next_machine_id)?;
                                scheduler.schedule(Event::new(
                                    event.time + duration,
                                    EventPayload::TaskEnd {
                                        job_id: *job_id,
                                        machine_id: next_machine_id,
                                        step_index: next_step,
                                    },
                                ))?;
                            } else {
                                next_machine.enqueue_job(*job_id);
                            }
                        }
                    }
                    let machine = self.factory.machines.get_mut(*machine_id)?;
                    if let Some(queued_job_id) = machine.dequeue_job() {
                        let qjob = self.factory.jobs.get(queued_job_id)?;
                        let qstep = qjob.current_step;
                        let qpid = qjob.product_id;
                        let routing = self.factory.routings.get_routing_for_product(qpid)?;
                        if let Some(step) = routing.get_step(qstep) {
                            let duration = step.duration;
                            let machine = self.factory.machines.get_mut(*machine_id)?;
                            machine.start_job(queued_job_id)?;
                            let qjob = self.factory.jobs.get_mut(queued_job_id)?;
                            qjob.start(*machine_id)?;
                            scheduler.schedule(Event::new(
                                event.time + duration,
                                EventPayload::TaskEnd {
                                    job_id: queued_job_id,
                                    machine_id: *machine_id,
                                    step_index: qstep,
                                },
                            ))?;
                        }
                    }
                }
                EventPayload::OrderCreation {
                    product_id,
                    quantity,
                } => {
                    let routing = self.factory.routings.get_routing_for_product(*product_id)?;
                    let total_steps = routing.step_count();
                    let job_id = self.factory.jobs.create_job(
                        *product_id,
                        *quantity,
                        total_steps,
                        event.time,
                    );
                    if let Some(first_step) = routing.get_step(0) {
                        let machine_id = first_step.machine_id;
                        let duration = first_step.duration;
                        let machine = self.factory.machines.get_mut(machine_id)?;
                        if machine.can_accept_job() {
                            machine.start_job(job_id)?;
                            let job = self.factory.jobs.get_mut(job_id)?;
                            job.start(machine_id)?;
                            scheduler.schedule(Event::new(
                                event.time + duration,
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
                }
                EventPayload::MachineAvailabilityChange { machine_id, online } => {
                    self.factory
                        .machines
                        .get_mut(*machine_id)?
                        .set_availability(*online)?;
                }
                _ => {}
            }
            Ok(())
        }
    }

    let mut handler_no_agent = NoAgentHandler {
        factory: factory_na,
        demand: demand_na,
        pricing: pricing_na,
    };
    let _ = run_scenario(&config_no_agent, &mut handler_no_agent).unwrap();
    let backlog_no_agent = handler_no_agent.factory.backlog();

    // Run with agent
    let (config_agent, factory_a, demand_a, pricing_a) = build_handlers(toml);
    let agent = SalesAgent::new(SalesAgentConfig {
        backlog_high: 5,
        backlog_low: 2,
        adjustment_pct: 0.15,
        min_price: 0.5,
        max_price: 50.0,
    });

    let mut handler_agent = AgentIntegratedHandler {
        factory: factory_a,
        demand: demand_a,
        pricing: pricing_a,
        agent,
        agent_enabled: true,
    };
    let _ = run_scenario(&config_agent, &mut handler_agent).unwrap();
    let backlog_agent = handler_agent.factory.backlog();

    assert!(
        backlog_agent <= backlog_no_agent,
        "agent should reduce backlog: agent={}, no_agent={}",
        backlog_agent,
        backlog_no_agent
    );
}

#[test]
fn agent_does_not_intervene_when_disabled() {
    let (config, factory, demand, pricing) = build_handlers(overload_toml());

    let agent = SalesAgent::new(SalesAgentConfig {
        backlog_high: 5,
        backlog_low: 2,
        adjustment_pct: 0.15,
        min_price: 0.5,
        max_price: 50.0,
    });

    let mut handler = AgentIntegratedHandler {
        factory,
        demand,
        pricing,
        agent,
        agent_enabled: false,
    };

    let result = run_scenario(&config, &mut handler).unwrap();

    assert_eq!(
        handler.agent.interventions, 0,
        "disabled agent should not intervene"
    );

    let agent_decisions = result
        .event_log
        .filter_by_type(EventType::AgentDecision)
        .count();
    assert_eq!(agent_decisions, 0, "no AgentDecision events when disabled");
}
