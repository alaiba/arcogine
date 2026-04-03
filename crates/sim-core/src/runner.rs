//! Headless simulation runner: drives the event loop by dequeuing events
//! from the scheduler and dispatching them to an `EventHandler`.

use crate::event::{Event, EventPayload};
use crate::handler::EventHandler;
use crate::log::EventLog;
use crate::queue::Scheduler;
use sim_types::scenario::ScenarioConfig;
use sim_types::{SimError, SimTime};

/// Result of a completed simulation run.
#[derive(Debug, Clone, PartialEq)]
pub struct SimResult {
    pub final_time: SimTime,
    pub event_log: EventLog,
    pub events_processed: u64,
}

/// Run a scenario to completion with the given event handler.
///
/// The runner:
/// 1. Seeds the scheduler with initial periodic events (DemandEvaluation, AgentEvaluation).
/// 2. Dequeues events in time order and dispatches each to the handler.
/// 3. Logs every event to the event log.
/// 4. Stops when the scheduler is empty or `max_ticks` is exceeded.
pub fn run_scenario(
    config: &ScenarioConfig,
    handler: &mut dyn EventHandler,
) -> Result<SimResult, SimError> {
    let mut scheduler = Scheduler::new();
    let mut event_log = EventLog::new();
    let max_time = SimTime(config.simulation.max_ticks);

    // Seed initial DemandEvaluation events
    let demand_interval = config.simulation.demand_eval_interval;
    if demand_interval > 0 {
        scheduler.schedule(Event::new(
            SimTime(demand_interval),
            EventPayload::DemandEvaluation,
        ))?;
    }

    // Seed initial AgentEvaluation events (if agent is configured and enabled)
    let agent_interval = config.simulation.agent_eval_interval;
    if agent_interval > 0 {
        if let Some(agent_cfg) = &config.agent {
            if agent_cfg.enabled {
                scheduler.schedule(Event::new(
                    SimTime(agent_interval),
                    EventPayload::AgentEvaluation,
                ))?;
            }
        }
    }

    let mut events_processed: u64 = 0;

    while let Some(event) = scheduler.next_event() {
        if event.time > max_time {
            break;
        }

        event_log.append(event.clone());
        handler.handle_event(&event, &mut scheduler)?;
        events_processed += 1;

        // Reschedule periodic events
        match &event.payload {
            EventPayload::DemandEvaluation => {
                let next_time = event.time + demand_interval;
                if next_time <= max_time {
                    scheduler.schedule(Event::new(next_time, EventPayload::DemandEvaluation))?;
                }
            }
            EventPayload::AgentEvaluation => {
                let next_time = event.time + agent_interval;
                if next_time <= max_time {
                    scheduler.schedule(Event::new(next_time, EventPayload::AgentEvaluation))?;
                }
            }
            _ => {}
        }
    }

    Ok(SimResult {
        final_time: scheduler.current_time(),
        event_log,
        events_processed,
    })
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::event::{Event, EventType};
    use crate::handler::EventHandler;
    use crate::queue::Scheduler;
    use sim_types::scenario::{AgentConfig, SimulationParams};

    struct NoopHandler;

    impl EventHandler for NoopHandler {
        fn handle_event(
            &mut self,
            _event: &Event,
            _scheduler: &mut Scheduler,
        ) -> Result<(), SimError> {
            Ok(())
        }
    }

    struct FailingHandler;

    impl EventHandler for FailingHandler {
        fn handle_event(
            &mut self,
            _event: &Event,
            _scheduler: &mut Scheduler,
        ) -> Result<(), SimError> {
            Err(SimError::Other {
                message: "handler failure".into(),
            })
        }
    }

    fn minimal_config(max_ticks: u64) -> ScenarioConfig {
        ScenarioConfig {
            simulation: SimulationParams {
                rng_seed: 1,
                max_ticks,
                demand_eval_interval: 10,
                agent_eval_interval: 50,
            },
            equipment: vec![],
            material: vec![],
            process_segment: vec![],
            operations_definition: vec![],
            economy: None,
            agent: None,
        }
    }

    #[test]
    fn zero_max_ticks_returns_immediately() {
        let config = minimal_config(0);
        let result = run_scenario(&config, &mut NoopHandler).unwrap();
        assert_eq!(result.events_processed, 0);
    }

    #[test]
    fn seeds_demand_evaluation_at_interval() {
        let config = minimal_config(100);
        let result = run_scenario(&config, &mut NoopHandler).unwrap();
        let demand_count = result
            .event_log
            .filter_by_type(EventType::DemandEvaluation)
            .count();
        assert_eq!(demand_count, 10);
    }

    #[test]
    fn seeds_agent_evaluation_when_enabled() {
        let mut config = minimal_config(100);
        config.agent = Some(AgentConfig {
            enabled: true,
            agent_type: "sales".into(),
        });
        let result = run_scenario(&config, &mut NoopHandler).unwrap();
        let agent_count = result
            .event_log
            .filter_by_type(EventType::AgentEvaluation)
            .count();
        assert_eq!(agent_count, 2);
    }

    #[test]
    fn no_agent_evaluation_without_agent_config() {
        let config = minimal_config(100);
        assert!(config.agent.is_none());
        let result = run_scenario(&config, &mut NoopHandler).unwrap();
        let agent_count = result
            .event_log
            .filter_by_type(EventType::AgentEvaluation)
            .count();
        assert_eq!(agent_count, 0);
    }

    #[test]
    fn no_agent_evaluation_when_disabled() {
        let mut config = minimal_config(100);
        config.agent = Some(AgentConfig {
            enabled: false,
            agent_type: "sales".into(),
        });
        let result = run_scenario(&config, &mut NoopHandler).unwrap();
        let agent_count = result
            .event_log
            .filter_by_type(EventType::AgentEvaluation)
            .count();
        assert_eq!(agent_count, 0);
    }

    #[test]
    fn handler_error_propagates() {
        let config = minimal_config(100);
        let result = run_scenario(&config, &mut FailingHandler);
        assert!(result.is_err());
    }
}
