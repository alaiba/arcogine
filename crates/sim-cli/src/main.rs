use clap::Parser;
use rand::SeedableRng;
use rand_chacha::ChaCha8Rng;
use sim_core::event::Event;
use sim_core::handler::EventHandler;
use sim_core::queue::Scheduler;
use sim_core::runner::{run_scenario, SimResult};
use sim_economy::demand::DemandModel;
use sim_economy::pricing::PricingState;
use sim_factory::machines::{Machine, MachineStore};
use sim_factory::process::FactoryHandler;
use sim_factory::routing::{Routing, RoutingStep, RoutingStore};
use sim_types::scenario::ScenarioConfig;
use sim_types::{MachineId, ProductId, SimError};

/// Arcogine — deterministic factory & economy simulation engine.
#[derive(Parser)]
#[command(name = "arcogine", version, about)]
enum Cli {
    /// Start the HTTP API server.
    Serve {
        /// Address to bind the server to.
        #[arg(long, default_value = "0.0.0.0:3000")]
        addr: String,
    },
    /// Run a scenario headlessly and exit.
    Run {
        /// Path to a scenario TOML file.
        #[arg(long)]
        scenario: String,

        /// Run without the HTTP server.
        #[arg(long)]
        headless: bool,
    },
}

pub struct HeadlessHandler {
    pub factory: FactoryHandler,
    pub demand: DemandModel,
    pub pricing: PricingState,
}

impl EventHandler for HeadlessHandler {
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

pub fn build_headless_handler(config: &ScenarioConfig) -> HeadlessHandler {
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

    let econ = config.economy.as_ref();
    let rng = ChaCha8Rng::seed_from_u64(config.simulation.rng_seed);
    let (base_demand, price_elasticity, lt_sensitivity, initial_price) = match econ {
        Some(e) => (
            e.base_demand,
            e.price_elasticity,
            e.lead_time_sensitivity,
            e.initial_price,
        ),
        None => (5.0, 0.5, 0.1, 10.0),
    };

    let demand = DemandModel::new(
        base_demand,
        price_elasticity,
        lt_sensitivity,
        initial_price,
        product_ids,
        rng,
    );
    let pricing = PricingState::new(initial_price);

    HeadlessHandler {
        factory,
        demand,
        pricing,
    }
}

pub fn run_headless(config: &ScenarioConfig) -> Result<(SimResult, HeadlessHandler), SimError> {
    let mut handler = build_headless_handler(config);
    let result = run_scenario(config, &mut handler)?;
    Ok((result, handler))
}

fn main() {
    tracing_subscriber::fmt()
        .with_env_filter(
            tracing_subscriber::EnvFilter::try_from_default_env()
                .unwrap_or_else(|_| tracing_subscriber::EnvFilter::new("info")),
        )
        .init();

    let cli = Cli::parse();
    match cli {
        Cli::Serve { addr } => {
            let rt = tokio::runtime::Runtime::new().expect("Failed to create Tokio runtime");
            rt.block_on(async {
                if let Err(e) = sim_api::server::start_server(&addr).await {
                    tracing::error!("Server error: {}", e);
                    std::process::exit(1);
                }
            });
        }
        Cli::Run {
            scenario,
            headless: _,
        } => {
            let toml_str =
                std::fs::read_to_string(&scenario).expect("Failed to read scenario file");
            let config =
                sim_core::scenario::load_scenario(&toml_str).expect("Failed to parse scenario");

            let (result, handler) = run_headless(&config).expect("Simulation failed");

            println!("Simulation completed:");
            println!("  Final time:       t={}", result.final_time.ticks());
            println!("  Events processed: {}", result.events_processed);
            println!("  Completed sales:  {}", handler.factory.completed_sales);
            println!("  Total revenue:    {:.2}", handler.factory.total_revenue);
            println!("  Backlog:          {}", handler.factory.backlog());
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use sim_core::event::EventType;
    use sim_types::SimTime;

    fn basic_toml() -> &'static str {
        r#"
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
initial_price = 5.0
base_demand = 10.0
"#
    }

    #[test]
    fn headless_handler_delegates_to_pricing_demand_factory() {
        let config = sim_core::scenario::load_scenario(basic_toml()).unwrap();
        let mut handler = build_headless_handler(&config);
        let mut scheduler = sim_core::queue::Scheduler::new();

        let seed = Event::new(SimTime(10), sim_core::event::EventPayload::DemandEvaluation);
        scheduler.schedule(seed).unwrap();
        let event = scheduler.next_event().unwrap();
        handler.handle_event(&event, &mut scheduler).unwrap();
        assert!(!scheduler.is_empty(), "DemandEvaluation should generate OrderCreation events");

        let order = scheduler.next_event().unwrap();
        handler.handle_event(&order, &mut scheduler).unwrap();
        assert!(handler.factory.jobs.iter().count() > 0, "factory should have a job after OrderCreation");
    }

    #[test]
    fn run_headless_returns_error_for_invalid_toml() {
        let result = sim_core::scenario::load_scenario("not valid toml {{{}");
        assert!(result.is_err());
    }

    #[test]
    fn run_headless_completes_with_sales_and_revenue() {
        let config = sim_core::scenario::load_scenario(basic_toml()).unwrap();
        let (result, handler) = run_headless(&config).unwrap();
        assert!(result.events_processed > 0);
        assert!(handler.factory.completed_sales > 0);
        assert!(handler.factory.total_revenue > 0.0);
    }

    #[test]
    fn headless_produces_task_start_events() {
        let config = sim_core::scenario::load_scenario(basic_toml()).unwrap();
        let (result, _handler) = run_headless(&config).unwrap();
        let task_starts = result
            .event_log
            .filter_by_type(EventType::TaskStart)
            .count();
        assert!(task_starts > 0, "headless run should produce TaskStart events");
    }
}
