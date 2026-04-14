//! Scenario acceptance tests validating behavioral outcomes.
//! Placed in sim-api because these integration tests require all domain crates.

use rand::rngs::ChaCha8Rng;
use rand::SeedableRng;
use sim_core::event::{Event, EventType};
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

fn build_handlers_from_scenario(
    toml: &str,
) -> (
    sim_types::scenario::ScenarioConfig,
    FactoryHandler,
    DemandModel,
    PricingState,
) {
    let config = load_scenario(toml).unwrap();

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

struct IntegratedHandler {
    factory: FactoryHandler,
    demand: DemandModel,
    pricing: PricingState,
}

impl EventHandler for IntegratedHandler {
    fn handle_event(&mut self, event: &Event, scheduler: &mut Scheduler) -> Result<(), SimError> {
        self.pricing.handle_event(event, scheduler)?;
        self.demand.current_price = self.pricing.current_price;
        self.demand.avg_lead_time = self.factory.avg_lead_time();
        self.demand.handle_event(event, scheduler)?;
        self.factory.set_current_price(self.pricing.current_price);
        self.factory.handle_event(event, scheduler)?;
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

    let backlog = handler.factory.backlog();
    assert!(
        backlog > 0,
        "overload scenario should have nonzero backlog, got {}",
        backlog
    );
}

#[test]
fn lowering_price_increases_demand() {
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

    assert!(handler.factory.total_revenue > 0.0);
    assert!(handler.factory.completed_sales > 0);
}
