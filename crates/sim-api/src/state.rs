//! Shared application state and the simulation thread bridge.
//!
//! The simulation runs on a dedicated OS thread for determinism.
//! The API layer communicates via `tokio::sync::mpsc` (commands in)
//! and `tokio::sync::watch` (state snapshots out). SSE clients
//! receive events via `tokio::sync::broadcast`.

use rand::SeedableRng;
use rand_chacha::ChaCha8Rng;
use serde::Serialize;
use sim_core::event::{Event, EventPayload};
use sim_core::handler::EventHandler;
use sim_core::kpi::{EventCount, Kpi, KpiValue, OrderCount, ThroughputRate, TotalSimulatedTime};
use sim_core::log::EventLog;
use sim_core::queue::Scheduler;
use sim_types::scenario::ScenarioConfig;
use sim_types::{JobStatus, MachineId, MachineState, ProductId, SimError, SimTime};
use std::sync::{mpsc, Arc};
use tokio::sync::{broadcast, watch};

use sim_agents::sales_agent::{AgentObservation, SalesAgent, SalesAgentConfig};
use sim_economy::demand::DemandModel;
use sim_economy::pricing::PricingState;
use sim_factory::machines::{Machine, MachineStore};
use sim_factory::process::FactoryHandler;
use sim_factory::routing::{Routing, RoutingStep, RoutingStore};

/// Commands sent from the API layer to the simulation thread.
#[derive(Debug)]
pub enum SimCommand {
    LoadScenario {
        toml: String,
        reply: std::sync::mpsc::SyncSender<Result<(), String>>,
    },
    Run,
    Pause,
    Step,
    Reset,
    ChangePrice(f64),
    ChangeMachineCount {
        machine_id: u64,
        online: bool,
    },
    ToggleAgent(bool),
    QuerySnapshot,
}

/// The running state of the simulation.
#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize)]
pub enum SimRunState {
    Idle,
    Running,
    Paused,
    Completed,
}

/// Machine info for the topology endpoint.
#[derive(Debug, Clone, Serialize)]
pub struct MachineInfo {
    pub id: u64,
    pub name: String,
    pub state: MachineState,
    pub queue_depth: usize,
    pub active_jobs: usize,
}

/// Routing edge for the topology endpoint.
#[derive(Debug, Clone, Serialize)]
pub struct RoutingEdge {
    pub from_machine_id: u64,
    pub to_machine_id: u64,
    pub routing_name: String,
}

/// Topology response (machines + edges).
#[derive(Debug, Clone, Serialize)]
pub struct TopologySnapshot {
    pub machines: Vec<MachineInfo>,
    pub edges: Vec<RoutingEdge>,
}

/// Job info for the jobs endpoint.
#[derive(Debug, Clone, Serialize)]
pub struct JobInfo {
    pub job_id: u64,
    pub product_id: u64,
    pub quantity: u64,
    pub status: JobStatus,
    pub current_step: usize,
    pub total_steps: usize,
    pub created_at: u64,
    pub completed_at: Option<u64>,
    pub revenue: Option<f64>,
}

/// A complete snapshot of the simulation state for the API layer.
#[derive(Debug, Clone, Serialize)]
pub struct SimSnapshot {
    pub run_state: SimRunState,
    pub current_time: u64,
    pub events_processed: u64,
    pub kpis: Vec<KpiValue>,
    pub topology: TopologySnapshot,
    pub jobs: Vec<JobInfo>,
    pub total_revenue: f64,
    pub completed_sales: u64,
    pub backlog: usize,
    pub current_price: f64,
    pub agent_enabled: bool,
    pub scenario_loaded: bool,
    pub last_error: Option<String>,
}

impl Default for SimSnapshot {
    fn default() -> Self {
        SimSnapshot {
            run_state: SimRunState::Idle,
            current_time: 0,
            events_processed: 0,
            kpis: Vec::new(),
            topology: TopologySnapshot {
                machines: Vec::new(),
                edges: Vec::new(),
            },
            jobs: Vec::new(),
            total_revenue: 0.0,
            completed_sales: 0,
            backlog: 0,
            current_price: 0.0,
            agent_enabled: false,
            scenario_loaded: false,
            last_error: None,
        }
    }
}

/// Shared state accessible by all API handlers.
pub struct AppState {
    pub cmd_tx: mpsc::Sender<SimCommand>,
    pub snapshot_rx: watch::Receiver<SimSnapshot>,
    pub event_tx: broadcast::Sender<Event>,
    pub event_log_rx: watch::Receiver<EventLog>,
    pub sse_semaphore: Arc<tokio::sync::Semaphore>,
}

impl std::fmt::Debug for AppState {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        f.debug_struct("AppState").finish_non_exhaustive()
    }
}

/// The integrated handler that coordinates all domain handlers.
struct IntegratedHandler {
    factory: FactoryHandler,
    demand: DemandModel,
    pricing: PricingState,
    agent: SalesAgent,
    agent_enabled: bool,
}

impl EventHandler for IntegratedHandler {
    fn handle_event(&mut self, event: &Event, scheduler: &mut Scheduler) -> Result<(), SimError> {
        self.pricing.handle_event(event, scheduler)?;
        self.demand.current_price = self.pricing.current_price;
        self.demand.avg_lead_time = self.factory.avg_lead_time();
        self.demand.handle_event(event, scheduler)?;

        self.factory.set_current_price(self.pricing.current_price);
        self.factory.handle_event(event, scheduler)?;

        if let EventPayload::AgentEvaluation = &event.payload {
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

        Ok(())
    }
}

fn build_handler_from_config(config: &ScenarioConfig) -> IntegratedHandler {
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

    let agent_enabled = config.agent.as_ref().is_some_and(|a| a.enabled);

    let agent = SalesAgent::new(SalesAgentConfig::default());

    IntegratedHandler {
        factory,
        demand,
        pricing,
        agent,
        agent_enabled,
    }
}

fn build_snapshot(
    handler: &IntegratedHandler,
    event_log: &EventLog,
    run_state: SimRunState,
    current_time: SimTime,
    events_processed: u64,
    config: Option<&ScenarioConfig>,
    last_error: Option<String>,
) -> SimSnapshot {
    let kpis = vec![
        TotalSimulatedTime.compute(event_log, current_time),
        EventCount.compute(event_log, current_time),
        ThroughputRate.compute(event_log, current_time),
        OrderCount.compute(event_log, current_time),
    ];

    let machines: Vec<MachineInfo> = handler
        .factory
        .machines
        .iter()
        .map(|m| MachineInfo {
            id: m.id.0,
            name: m.name.clone(),
            state: m.state,
            queue_depth: m.queue_depth(),
            active_jobs: m.active_jobs.len(),
        })
        .collect();

    let mut edges = Vec::new();
    if let Some(cfg) = config {
        for od in &cfg.operations_definition {
            let step_machine_ids: Vec<u64> = od
                .steps
                .iter()
                .filter_map(|seg_id| {
                    cfg.process_segment
                        .iter()
                        .find(|s| s.id == *seg_id)
                        .map(|s| s.equipment_id)
                })
                .collect();
            for pair in step_machine_ids.windows(2) {
                edges.push(RoutingEdge {
                    from_machine_id: pair[0],
                    to_machine_id: pair[1],
                    routing_name: od.name.clone(),
                });
            }
        }
    }

    let jobs: Vec<JobInfo> = handler
        .factory
        .jobs
        .all_jobs()
        .map(|j| {
            let revenue = if j.status == JobStatus::Completed {
                Some(handler.pricing.current_price * j.quantity as f64)
            } else {
                None
            };
            JobInfo {
                job_id: j.id.0,
                product_id: j.product_id.0,
                quantity: j.quantity,
                status: j.status,
                current_step: j.current_step,
                total_steps: j.total_steps,
                created_at: j.created_at.ticks(),
                completed_at: j.completed_at.map(|t| t.ticks()),
                revenue,
            }
        })
        .collect();

    SimSnapshot {
        run_state,
        current_time: current_time.ticks(),
        events_processed,
        kpis,
        topology: TopologySnapshot { machines, edges },
        jobs,
        total_revenue: handler.factory.total_revenue,
        completed_sales: handler.factory.completed_sales,
        backlog: handler.factory.backlog(),
        current_price: handler.pricing.current_price,
        agent_enabled: handler.agent_enabled,
        scenario_loaded: config.is_some(),
        last_error,
    }
}

/// Spawn the simulation thread. Returns handles for communication.
pub fn spawn_sim_thread() -> (
    mpsc::Sender<SimCommand>,
    watch::Receiver<SimSnapshot>,
    broadcast::Sender<Event>,
    watch::Receiver<EventLog>,
) {
    let (cmd_tx, cmd_rx) = mpsc::channel::<SimCommand>();
    let (snapshot_tx, snapshot_rx) = watch::channel(SimSnapshot::default());
    let (event_tx, _) = broadcast::channel::<Event>(4096);
    let (log_tx, log_rx) = watch::channel(EventLog::new());

    let event_tx_clone = event_tx.clone();

    std::thread::spawn(move || {
        let mut config: Option<ScenarioConfig> = None;
        let mut handler: Option<IntegratedHandler> = None;
        let mut scheduler = Scheduler::new();
        let mut event_log = EventLog::new();
        let mut run_state = SimRunState::Idle;
        let mut events_processed: u64 = 0;
        let mut agent_enabled = false;
        let mut last_error: Option<String> = None;

        loop {
            let cmd = cmd_rx.recv();
            let Ok(cmd) = cmd else { break };

            match cmd {
                SimCommand::LoadScenario {
                    toml: toml_str,
                    reply,
                } => match sim_core::scenario::load_scenario(&toml_str) {
                    Ok(cfg) => {
                        let h = build_handler_from_config(&cfg);
                        agent_enabled = h.agent_enabled;

                        scheduler = Scheduler::new();
                        event_log = EventLog::new();
                        events_processed = 0;
                        last_error = None;

                        let demand_interval = cfg.simulation.demand_eval_interval;
                        if demand_interval > 0 {
                            if let Err(e) = scheduler.schedule(Event::new(
                                SimTime(demand_interval),
                                EventPayload::DemandEvaluation,
                            )) {
                                tracing::warn!(error = %e, "scheduler error");
                                last_error = Some(e.to_string());
                            }
                        }

                        let agent_interval = cfg.simulation.agent_eval_interval;
                        if agent_interval > 0 && agent_enabled {
                            if let Err(e) = scheduler.schedule(Event::new(
                                SimTime(agent_interval),
                                EventPayload::AgentEvaluation,
                            )) {
                                tracing::warn!(error = %e, "scheduler error");
                                last_error = Some(e.to_string());
                            }
                        }

                        run_state = SimRunState::Paused;
                        let snap = build_snapshot(
                            &h,
                            &event_log,
                            run_state,
                            SimTime::ZERO,
                            0,
                            Some(&cfg),
                            last_error.take(),
                        );
                        let _ = snapshot_tx.send(snap);
                        let _ = log_tx.send(event_log.clone());
                        let _ = reply.send(Ok(()));
                        handler = Some(h);
                        config = Some(cfg);
                    }
                    Err(e) => {
                        tracing::error!("Failed to load scenario: {e}");
                        let _ = reply.send(Err(e.to_string()));
                    }
                },

                SimCommand::Step => {
                    if let (Some(h), Some(cfg)) = (&mut handler, &config) {
                        let max_time = SimTime(cfg.simulation.max_ticks);
                        if let Some(event) = scheduler.next_event() {
                            if event.time <= max_time {
                                event_log.append(event.clone());
                                let _ = event_tx_clone.send(event.clone());
                                if let Err(e) = h.handle_event(&event, &mut scheduler) {
                                    tracing::warn!(error = %e, event_time = event.time.ticks(), "event handler error");
                                    last_error = Some(e.to_string());
                                }
                                events_processed += 1;

                                reschedule_periodic(
                                    &event,
                                    &mut scheduler,
                                    max_time,
                                    cfg.simulation.demand_eval_interval,
                                    cfg.simulation.agent_eval_interval,
                                    agent_enabled,
                                );
                            }

                            if scheduler.is_empty()
                                || scheduler.peek_time().is_some_and(|t| t > max_time)
                            {
                                run_state = SimRunState::Completed;
                            } else {
                                run_state = SimRunState::Paused;
                            }
                        } else {
                            run_state = SimRunState::Completed;
                        }

                        let snap = build_snapshot(
                            h,
                            &event_log,
                            run_state,
                            scheduler.current_time(),
                            events_processed,
                            Some(cfg),
                            last_error.take(),
                        );
                        let _ = snapshot_tx.send(snap);
                        let _ = log_tx.send(event_log.clone());
                    }
                }

                SimCommand::Run => {
                    if let (Some(h), Some(cfg)) = (&mut handler, &config) {
                        run_state = SimRunState::Running;
                        let max_time = SimTime(cfg.simulation.max_ticks);

                        while let Some(event) = scheduler.next_event() {
                            if event.time > max_time {
                                break;
                            }

                            event_log.append(event.clone());
                            let _ = event_tx_clone.send(event.clone());
                            if let Err(e) = h.handle_event(&event, &mut scheduler) {
                                tracing::warn!(error = %e, event_time = event.time.ticks(), "event handler error");
                                last_error = Some(e.to_string());
                            }
                            events_processed += 1;

                            reschedule_periodic(
                                &event,
                                &mut scheduler,
                                max_time,
                                cfg.simulation.demand_eval_interval,
                                cfg.simulation.agent_eval_interval,
                                agent_enabled,
                            );

                            if let Ok(SimCommand::Pause) = cmd_rx.try_recv() {
                                run_state = SimRunState::Paused;
                                break;
                            }
                        }

                        if run_state == SimRunState::Running {
                            run_state = SimRunState::Completed;
                        }

                        let snap = build_snapshot(
                            h,
                            &event_log,
                            run_state,
                            scheduler.current_time(),
                            events_processed,
                            Some(cfg),
                            last_error.take(),
                        );
                        let _ = snapshot_tx.send(snap);
                        let _ = log_tx.send(event_log.clone());
                    }
                }

                SimCommand::Pause => {
                    if run_state == SimRunState::Running {
                        run_state = SimRunState::Paused;
                    }
                }

                SimCommand::Reset => {
                    if let Some(cfg) = &config {
                        let h = build_handler_from_config(cfg);
                        agent_enabled = h.agent_enabled;
                        scheduler = Scheduler::new();
                        event_log = EventLog::new();
                        events_processed = 0;
                        last_error = None;

                        let demand_interval = cfg.simulation.demand_eval_interval;
                        if demand_interval > 0 {
                            if let Err(e) = scheduler.schedule(Event::new(
                                SimTime(demand_interval),
                                EventPayload::DemandEvaluation,
                            )) {
                                tracing::warn!(error = %e, "scheduler error");
                                last_error = Some(e.to_string());
                            }
                        }

                        let agent_interval = cfg.simulation.agent_eval_interval;
                        if agent_interval > 0 && agent_enabled {
                            if let Err(e) = scheduler.schedule(Event::new(
                                SimTime(agent_interval),
                                EventPayload::AgentEvaluation,
                            )) {
                                tracing::warn!(error = %e, "scheduler error");
                                last_error = Some(e.to_string());
                            }
                        }

                        run_state = SimRunState::Paused;
                        let snap = build_snapshot(
                            &h,
                            &event_log,
                            run_state,
                            SimTime::ZERO,
                            0,
                            Some(cfg),
                            last_error.take(),
                        );
                        let _ = snapshot_tx.send(snap);
                        let _ = log_tx.send(event_log.clone());
                        handler = Some(h);
                    }
                }

                SimCommand::ChangePrice(new_price) => {
                    if let (Some(h), Some(cfg)) = (&mut handler, &config) {
                        let current_time = scheduler.current_time();
                        let event =
                            Event::new(current_time, EventPayload::PriceChange { new_price });
                        event_log.append(event.clone());
                        let _ = event_tx_clone.send(event.clone());
                        if let Err(e) = h.handle_event(&event, &mut scheduler) {
                            tracing::warn!(error = %e, event_time = event.time.ticks(), "event handler error");
                            last_error = Some(e.to_string());
                        }
                        events_processed += 1;

                        let snap = build_snapshot(
                            h,
                            &event_log,
                            run_state,
                            scheduler.current_time(),
                            events_processed,
                            Some(cfg),
                            last_error.take(),
                        );
                        let _ = snapshot_tx.send(snap);
                        let _ = log_tx.send(event_log.clone());
                    }
                }

                SimCommand::ChangeMachineCount { machine_id, online } => {
                    if let (Some(h), Some(cfg)) = (&mut handler, &config) {
                        let current_time = scheduler.current_time();
                        let event = Event::new(
                            current_time,
                            EventPayload::MachineAvailabilityChange {
                                machine_id: MachineId(machine_id),
                                online,
                            },
                        );
                        event_log.append(event.clone());
                        let _ = event_tx_clone.send(event.clone());
                        if let Err(e) = h.handle_event(&event, &mut scheduler) {
                            tracing::warn!(error = %e, event_time = event.time.ticks(), "event handler error");
                            last_error = Some(e.to_string());
                        }
                        events_processed += 1;

                        let snap = build_snapshot(
                            h,
                            &event_log,
                            run_state,
                            scheduler.current_time(),
                            events_processed,
                            Some(cfg),
                            last_error.take(),
                        );
                        let _ = snapshot_tx.send(snap);
                        let _ = log_tx.send(event_log.clone());
                    }
                }

                SimCommand::ToggleAgent(enabled) => {
                    agent_enabled = enabled;
                    if let Some(h) = &mut handler {
                        h.agent_enabled = enabled;
                    }
                    if let (Some(h), Some(cfg)) = (&handler, &config) {
                        let snap = build_snapshot(
                            h,
                            &event_log,
                            run_state,
                            scheduler.current_time(),
                            events_processed,
                            Some(cfg),
                            last_error.clone(),
                        );
                        let _ = snapshot_tx.send(snap);
                    }
                }

                SimCommand::QuerySnapshot => {
                    if let (Some(h), Some(cfg)) = (&handler, &config) {
                        let snap = build_snapshot(
                            h,
                            &event_log,
                            run_state,
                            scheduler.current_time(),
                            events_processed,
                            Some(cfg),
                            last_error.clone(),
                        );
                        let _ = snapshot_tx.send(snap);
                        let _ = log_tx.send(event_log.clone());
                    }
                }
            }
        }
    });

    (cmd_tx, snapshot_rx, event_tx, log_rx)
}

fn reschedule_periodic(
    event: &Event,
    scheduler: &mut Scheduler,
    max_time: SimTime,
    demand_interval: u64,
    agent_interval: u64,
    agent_enabled: bool,
) {
    match &event.payload {
        EventPayload::DemandEvaluation => {
            let next_time = event.time + demand_interval;
            if next_time <= max_time {
                let _ = scheduler.schedule(Event::new(next_time, EventPayload::DemandEvaluation));
            }
        }
        EventPayload::AgentEvaluation if agent_enabled => {
            let next_time = event.time + agent_interval;
            if next_time <= max_time {
                let _ = scheduler.schedule(Event::new(next_time, EventPayload::AgentEvaluation));
            }
        }
        _ => {}
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::thread;
    use std::time::Duration;

    fn basic_toml() -> String {
        r#"
[simulation]
rng_seed = 42
max_ticks = 100
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
"#
        .to_string()
    }

    fn send_load_scenario(cmd_tx: &mpsc::Sender<SimCommand>, toml: String) {
        let (tx, _rx) = std::sync::mpsc::sync_channel(1);
        cmd_tx
            .send(SimCommand::LoadScenario { toml, reply: tx })
            .unwrap();
    }

    fn wait_for_snapshot(
        rx: &mut watch::Receiver<SimSnapshot>,
        pred: impl Fn(&SimSnapshot) -> bool,
    ) -> SimSnapshot {
        for _ in 0..100 {
            let snap = rx.borrow().clone();
            if pred(&snap) {
                return snap;
            }
            thread::sleep(Duration::from_millis(20));
        }
        rx.borrow().clone()
    }

    #[test]
    fn default_snapshot_state() {
        let snap = SimSnapshot::default();
        assert_eq!(snap.run_state, SimRunState::Idle);
        assert!(!snap.scenario_loaded);
        assert_eq!(snap.events_processed, 0);
    }

    #[test]
    fn integrated_handler_delegates_to_factory_handler() {
        let toml = basic_toml();
        let config = sim_core::scenario::load_scenario(&toml).unwrap();
        let mut h = build_handler_from_config(&config);
        let mut scheduler = Scheduler::new();

        let seed_event = Event::new(SimTime(10), EventPayload::DemandEvaluation);
        scheduler.schedule(seed_event).unwrap();
        let event = scheduler.next_event().unwrap();

        h.handle_event(&event, &mut scheduler).unwrap();

        let order_event = Event::new(
            SimTime(10),
            EventPayload::OrderCreation {
                product_id: ProductId(1),
                quantity: 1,
            },
        );
        h.handle_event(&order_event, &mut scheduler).unwrap();
        assert!(
            h.factory.jobs.iter().count() > 0,
            "factory handler should process OrderCreation"
        );
    }

    #[test]
    fn build_snapshot_produces_edges_for_multi_step_routing() {
        let toml = r#"
[simulation]
rng_seed = 1
max_ticks = 10

[[equipment]]
id = 1
name = "Mill"
[[equipment]]
id = 2
name = "Drill"

[[material]]
id = 1
name = "Widget"
routing_id = 1

[[process_segment]]
id = 1
name = "Milling"
equipment_id = 1
duration = 5
[[process_segment]]
id = 2
name = "Drilling"
equipment_id = 2
duration = 3

[[operations_definition]]
id = 1
name = "Widget routing"
steps = [1, 2]

[economy]
initial_price = 10.0
"#;
        let config = sim_core::scenario::load_scenario(toml).unwrap();
        let h = build_handler_from_config(&config);
        let log = EventLog::new();
        let snap = build_snapshot(
            &h,
            &log,
            SimRunState::Paused,
            SimTime::ZERO,
            0,
            Some(&config),
            None,
        );
        assert_eq!(snap.topology.edges.len(), 1);
        assert_eq!(snap.topology.edges[0].from_machine_id, 1);
        assert_eq!(snap.topology.edges[0].to_machine_id, 2);
    }

    #[test]
    fn build_snapshot_handles_empty_routings() {
        let toml = r#"
[simulation]
rng_seed = 1
max_ticks = 10

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
"#;
        let config = sim_core::scenario::load_scenario(toml).unwrap();
        let h = build_handler_from_config(&config);
        let log = EventLog::new();
        let snap = build_snapshot(
            &h,
            &log,
            SimRunState::Paused,
            SimTime::ZERO,
            0,
            Some(&config),
            None,
        );
        assert!(snap.topology.edges.is_empty());
    }

    #[test]
    fn spawn_load_scenario_transitions_to_loaded() {
        let (cmd_tx, mut snap_rx, _event_tx, _log_rx) = spawn_sim_thread();
        send_load_scenario(&cmd_tx, basic_toml());

        let snap = wait_for_snapshot(&mut snap_rx, |s| s.scenario_loaded);
        assert!(snap.scenario_loaded);
        assert_eq!(snap.run_state, SimRunState::Paused);
    }

    #[test]
    fn spawn_run_after_load_completes() {
        let (cmd_tx, mut snap_rx, _event_tx, _log_rx) = spawn_sim_thread();
        send_load_scenario(&cmd_tx, basic_toml());
        wait_for_snapshot(&mut snap_rx, |s| s.scenario_loaded);

        cmd_tx.send(SimCommand::Run).unwrap();
        let snap = wait_for_snapshot(&mut snap_rx, |s| s.run_state == SimRunState::Completed);
        assert!(snap.events_processed > 0);
        assert_eq!(snap.run_state, SimRunState::Completed);
    }

    #[test]
    fn spawn_step_advances_one_event() {
        let (cmd_tx, mut snap_rx, _event_tx, _log_rx) = spawn_sim_thread();
        send_load_scenario(&cmd_tx, basic_toml());
        wait_for_snapshot(&mut snap_rx, |s| s.scenario_loaded);

        cmd_tx.send(SimCommand::Step).unwrap();
        let snap = wait_for_snapshot(&mut snap_rx, |s| s.events_processed > 0);
        assert_eq!(snap.events_processed, 1);
    }

    #[test]
    fn spawn_change_price_updates_snapshot() {
        let (cmd_tx, mut snap_rx, _event_tx, _log_rx) = spawn_sim_thread();
        send_load_scenario(&cmd_tx, basic_toml());
        wait_for_snapshot(&mut snap_rx, |s| s.scenario_loaded);

        cmd_tx.send(SimCommand::ChangePrice(25.0)).unwrap();
        let snap = wait_for_snapshot(&mut snap_rx, |s| s.current_price == 25.0);
        assert_eq!(snap.current_price, 25.0);
    }

    #[test]
    fn spawn_change_machine_count_updates_snapshot() {
        let (cmd_tx, mut snap_rx, _event_tx, _log_rx) = spawn_sim_thread();
        send_load_scenario(&cmd_tx, basic_toml());
        wait_for_snapshot(&mut snap_rx, |s| s.scenario_loaded);

        cmd_tx
            .send(SimCommand::ChangeMachineCount {
                machine_id: 1,
                online: false,
            })
            .unwrap();
        let snap = wait_for_snapshot(&mut snap_rx, |s| s.events_processed > 0);
        assert!(snap.events_processed >= 1);
    }

    #[test]
    fn spawn_toggle_agent_updates_snapshot() {
        let (cmd_tx, mut snap_rx, _event_tx, _log_rx) = spawn_sim_thread();
        send_load_scenario(&cmd_tx, basic_toml());
        wait_for_snapshot(&mut snap_rx, |s| s.scenario_loaded);

        cmd_tx.send(SimCommand::ToggleAgent(true)).unwrap();
        let snap = wait_for_snapshot(&mut snap_rx, |s| s.agent_enabled);
        assert!(snap.agent_enabled);

        cmd_tx.send(SimCommand::ToggleAgent(false)).unwrap();
        let snap = wait_for_snapshot(&mut snap_rx, |s| !s.agent_enabled);
        assert!(!snap.agent_enabled);
    }

    #[test]
    fn spawn_reset_restores_initial_state() {
        let (cmd_tx, mut snap_rx, _event_tx, _log_rx) = spawn_sim_thread();
        send_load_scenario(&cmd_tx, basic_toml());
        wait_for_snapshot(&mut snap_rx, |s| s.scenario_loaded);

        cmd_tx.send(SimCommand::Run).unwrap();
        wait_for_snapshot(&mut snap_rx, |s| s.run_state == SimRunState::Completed);

        cmd_tx.send(SimCommand::Reset).unwrap();
        let snap = wait_for_snapshot(&mut snap_rx, |s| s.events_processed == 0);
        assert_eq!(snap.events_processed, 0);
        assert_eq!(snap.run_state, SimRunState::Paused);
        assert!(snap.scenario_loaded);
    }

    #[test]
    fn spawn_query_snapshot_returns_current_state() {
        let (cmd_tx, mut snap_rx, _event_tx, _log_rx) = spawn_sim_thread();
        send_load_scenario(&cmd_tx, basic_toml());
        wait_for_snapshot(&mut snap_rx, |s| s.scenario_loaded);

        cmd_tx.send(SimCommand::QuerySnapshot).unwrap();
        let snap = wait_for_snapshot(&mut snap_rx, |s| s.scenario_loaded);
        assert_eq!(snap.run_state, SimRunState::Paused);
    }

    #[test]
    fn spawn_pause_during_idle_is_no_op() {
        let (cmd_tx, mut snap_rx, _event_tx, _log_rx) = spawn_sim_thread();
        send_load_scenario(&cmd_tx, basic_toml());
        wait_for_snapshot(&mut snap_rx, |s| s.scenario_loaded);

        cmd_tx.send(SimCommand::Pause).unwrap();
        cmd_tx.send(SimCommand::QuerySnapshot).unwrap();
        let snap = wait_for_snapshot(&mut snap_rx, |s| s.scenario_loaded);
        assert_eq!(snap.run_state, SimRunState::Paused);
    }

    #[test]
    fn step_past_max_ticks_completes() {
        let toml = r#"
[simulation]
rng_seed = 42
max_ticks = 5
demand_eval_interval = 2

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
"#
        .to_string();

        let (cmd_tx, mut snap_rx, _event_tx, _log_rx) = spawn_sim_thread();
        send_load_scenario(&cmd_tx, toml);
        wait_for_snapshot(&mut snap_rx, |s| s.scenario_loaded);

        for _ in 0..50 {
            cmd_tx.send(SimCommand::Step).unwrap();
        }
        let snap = wait_for_snapshot(&mut snap_rx, |s| s.run_state == SimRunState::Completed);
        assert_eq!(snap.run_state, SimRunState::Completed);
    }

    #[test]
    fn build_snapshot_without_config_has_no_edges() {
        let toml = basic_toml();
        let config = sim_core::scenario::load_scenario(&toml).unwrap();
        let h = build_handler_from_config(&config);
        let log = EventLog::new();
        let snap = build_snapshot(
            &h,
            &log,
            SimRunState::Idle,
            SimTime::ZERO,
            0,
            None,
            None,
        );
        assert!(snap.topology.edges.is_empty());
        assert!(!snap.scenario_loaded);
    }

    #[test]
    fn build_snapshot_includes_last_error() {
        let toml = basic_toml();
        let config = sim_core::scenario::load_scenario(&toml).unwrap();
        let h = build_handler_from_config(&config);
        let log = EventLog::new();
        let snap = build_snapshot(
            &h,
            &log,
            SimRunState::Paused,
            SimTime::ZERO,
            0,
            Some(&config),
            Some("test error".to_string()),
        );
        assert_eq!(snap.last_error.as_deref(), Some("test error"));
    }

    #[test]
    fn build_handler_from_config_uses_economy_defaults_when_absent() {
        let toml = r#"
[simulation]
rng_seed = 1
max_ticks = 10

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
"#;
        let config = sim_core::scenario::load_scenario(toml).unwrap();
        let h = build_handler_from_config(&config);
        assert_eq!(h.pricing.current_price, 10.0);
        assert!(!h.agent_enabled);
    }

    #[test]
    fn reschedule_periodic_demand_within_max() {
        let event = Event::new(SimTime(10), EventPayload::DemandEvaluation);
        let mut scheduler = Scheduler::new();
        scheduler.schedule(Event::new(SimTime(10), EventPayload::DemandEvaluation)).unwrap();
        scheduler.next_event().unwrap();

        reschedule_periodic(&event, &mut scheduler, SimTime(100), 10, 0, false);
        assert!(!scheduler.is_empty());
        assert_eq!(scheduler.peek_time(), Some(SimTime(20)));
    }

    #[test]
    fn reschedule_periodic_demand_past_max_does_not_schedule() {
        let event = Event::new(SimTime(95), EventPayload::DemandEvaluation);
        let mut scheduler = Scheduler::new();
        scheduler.schedule(Event::new(SimTime(95), EventPayload::DemandEvaluation)).unwrap();
        scheduler.next_event().unwrap();

        reschedule_periodic(&event, &mut scheduler, SimTime(100), 10, 0, false);
        assert!(scheduler.is_empty());
    }

    #[test]
    fn reschedule_periodic_agent_when_enabled() {
        let event = Event::new(SimTime(10), EventPayload::AgentEvaluation);
        let mut scheduler = Scheduler::new();
        scheduler.schedule(Event::new(SimTime(10), EventPayload::AgentEvaluation)).unwrap();
        scheduler.next_event().unwrap();

        reschedule_periodic(&event, &mut scheduler, SimTime(100), 10, 20, true);
        assert_eq!(scheduler.peek_time(), Some(SimTime(30)));
    }

    #[test]
    fn reschedule_periodic_agent_when_disabled_does_not_schedule() {
        let event = Event::new(SimTime(10), EventPayload::AgentEvaluation);
        let mut scheduler = Scheduler::new();
        scheduler.schedule(Event::new(SimTime(10), EventPayload::AgentEvaluation)).unwrap();
        scheduler.next_event().unwrap();

        reschedule_periodic(&event, &mut scheduler, SimTime(100), 10, 20, false);
        assert!(scheduler.is_empty());
    }

    #[test]
    fn reschedule_periodic_ignores_other_events() {
        let event = Event::new(
            SimTime(10),
            EventPayload::OrderCreation {
                product_id: ProductId(1),
                quantity: 1,
            },
        );
        let mut scheduler = Scheduler::new();
        reschedule_periodic(&event, &mut scheduler, SimTime(100), 10, 20, true);
        assert!(scheduler.is_empty());
    }
}
