//! Scenario acceptance tests validating behavioral outcomes.
//! Placed in sim-api because these integration tests require all domain crates.

use rand::SeedableRng;
use rand_chacha::ChaCha8Rng;
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

/// Build factory and economy handlers from a loaded scenario config.
fn build_handlers_from_scenario(
    toml: &str,
) -> (
    sim_types::scenario::ScenarioConfig,
    FactoryHandler,
    DemandModel,
    PricingState,
) {
    let config = load_scenario(toml).unwrap();

    // Build machine store
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

    // Build routing store
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

    let econ = config.economy.as_ref().expect("economy config required");
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

/// Wrapper handler that coordinates factory + demand + pricing.
struct IntegratedHandler {
    factory: FactoryHandler,
    demand: DemandModel,
    pricing: PricingState,
}

impl EventHandler for IntegratedHandler {
    fn handle_event(&mut self, event: &Event, scheduler: &mut Scheduler) -> Result<(), SimError> {
        // Update pricing first
        self.pricing.handle_event(event, scheduler)?;

        // Sync price to demand model
        self.demand.current_price = self.pricing.current_price;

        // Sync lead time from factory to demand model
        self.demand.avg_lead_time = self.factory.avg_lead_time();

        // Handle demand evaluation (generates orders)
        self.demand.handle_event(event, scheduler)?;

        // Handle factory events (processes orders, advances jobs)
        // Pass current price for revenue calculation
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
                    // Advance to next step
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

                // Dispatch queued jobs on the freed machine
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
            _ => {}
        }

        Ok(())
    }
}

#[test]
fn basic_scenario_runs_to_completion() {
    let toml = include_str!("../../../examples/basic_scenario.toml");
    let (config, factory, demand, pricing) = build_handlers_from_scenario(toml);
    let mut handler = IntegratedHandler {
        factory,
        demand,
        pricing,
    };
    let result = run_scenario(&config, &mut handler).unwrap();
    assert!(result.events_processed > 0, "no events processed");
    assert!(handler.factory.completed_sales > 0, "no sales completed");
    assert!(handler.factory.total_revenue > 0.0, "no revenue generated");
}

#[test]
fn overload_scenario_builds_backlog() {
    let toml = include_str!("../../../examples/overload_scenario.toml");
    let (config, factory, demand, pricing) = build_handlers_from_scenario(toml);
    let mut handler = IntegratedHandler {
        factory,
        demand,
        pricing,
    };
    let _result = run_scenario(&config, &mut handler).unwrap();

    // Overload: low price + high demand should create backlog
    let backlog = handler.factory.backlog();
    assert!(
        backlog > 0,
        "overload scenario should have nonzero backlog, got {}",
        backlog
    );
}

#[test]
fn lowering_price_increases_demand() {
    // Run twice: once at high price, once at low price.
    let base_toml = r#"
[simulation]
rng_seed = 42
max_ticks = 200
demand_eval_interval = 10

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
duration = 5

[[operations_definition]]
id = 1
name = "Widget routing"
steps = [1]

[economy]
initial_price = 10.0
base_demand = 5.0
price_elasticity = 0.5
lead_time_sensitivity = 0.0
"#;

    let (config_high, factory_h, demand_h, pricing_h) = build_handlers_from_scenario(base_toml);
    let mut handler_high = IntegratedHandler {
        factory: factory_h,
        demand: demand_h,
        pricing: pricing_h,
    };
    let result_high = run_scenario(&config_high, &mut handler_high).unwrap();
    let orders_high = result_high
        .event_log
        .filter_by_type(EventType::OrderCreation)
        .count();

    // Low price scenario
    let low_toml = base_toml.replace("initial_price = 10.0", "initial_price = 1.0");
    let (config_low, factory_l, demand_l, pricing_l) = build_handlers_from_scenario(&low_toml);
    let mut handler_low = IntegratedHandler {
        factory: factory_l,
        demand: demand_l,
        pricing: pricing_l,
    };
    let result_low = run_scenario(&config_low, &mut handler_low).unwrap();
    let orders_low = result_low
        .event_log
        .filter_by_type(EventType::OrderCreation)
        .count();

    assert!(
        orders_low > orders_high,
        "lower price should generate more orders: low={}, high={}",
        orders_low,
        orders_high
    );
}

#[test]
fn raising_price_reduces_load() {
    let base_toml = r#"
[simulation]
rng_seed = 42
max_ticks = 200
demand_eval_interval = 10

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
duration = 5

[[operations_definition]]
id = 1
name = "Widget routing"
steps = [1]

[economy]
initial_price = 1.0
base_demand = 5.0
price_elasticity = 0.5
lead_time_sensitivity = 0.0
"#;

    let (config_low, factory_l, demand_l, pricing_l) = build_handlers_from_scenario(base_toml);
    let mut handler_low = IntegratedHandler {
        factory: factory_l,
        demand: demand_l,
        pricing: pricing_l,
    };
    let _result_low = run_scenario(&config_low, &mut handler_low).unwrap();
    let backlog_low = handler_low.factory.backlog();

    // High price scenario
    let high_toml = base_toml.replace("initial_price = 1.0", "initial_price = 9.0");
    let (config_high, factory_h, demand_h, pricing_h) = build_handlers_from_scenario(&high_toml);
    let mut handler_high = IntegratedHandler {
        factory: factory_h,
        demand: demand_h,
        pricing: pricing_h,
    };
    let _result_high = run_scenario(&config_high, &mut handler_high).unwrap();
    let backlog_high = handler_high.factory.backlog();

    assert!(
        backlog_high <= backlog_low,
        "higher price should reduce backlog: high={}, low={}",
        backlog_high,
        backlog_low
    );
}

#[test]
fn revenue_generated_from_completed_jobs() {
    let toml = include_str!("../../../examples/basic_scenario.toml");
    let (config, factory, demand, pricing) = build_handlers_from_scenario(toml);
    let mut handler = IntegratedHandler {
        factory,
        demand,
        pricing,
    };
    let _result = run_scenario(&config, &mut handler).unwrap();

    // Each completed sale should contribute exactly price * quantity
    assert!(handler.factory.total_revenue > 0.0);
    assert!(handler.factory.completed_sales > 0);
}
