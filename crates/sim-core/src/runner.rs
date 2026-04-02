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
