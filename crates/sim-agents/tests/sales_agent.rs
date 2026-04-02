//! Unit tests for the SalesAgent decision logic.

use sim_agents::sales_agent::{AgentObservation, SalesAgent, SalesAgentConfig};
use sim_core::event::{Event, EventPayload, EventType};
use sim_core::handler::EventHandler;
use sim_core::queue::Scheduler;
use sim_types::SimTime;

fn make_agent(backlog_high: usize, backlog_low: usize) -> SalesAgent {
    SalesAgent::new(SalesAgentConfig {
        backlog_high,
        backlog_low,
        adjustment_pct: 0.10,
        min_price: 1.0,
        max_price: 50.0,
    })
}

#[test]
fn raises_price_when_backlog_high() {
    let mut agent = make_agent(10, 3);
    agent.observe(AgentObservation {
        backlog: 15,
        current_price: 10.0,
        ..Default::default()
    });
    let decision = agent.decide();
    assert!(decision.is_some());
    let new_price = decision.unwrap();
    assert!(new_price > 10.0, "price should increase: got {}", new_price);
}

#[test]
fn lowers_price_when_backlog_low() {
    let mut agent = make_agent(10, 3);
    agent.observe(AgentObservation {
        backlog: 1,
        current_price: 10.0,
        ..Default::default()
    });
    let decision = agent.decide();
    assert!(decision.is_some());
    let new_price = decision.unwrap();
    assert!(new_price < 10.0, "price should decrease: got {}", new_price);
}

#[test]
fn holds_price_when_backlog_normal() {
    let mut agent = make_agent(10, 3);
    agent.observe(AgentObservation {
        backlog: 5,
        current_price: 10.0,
        ..Default::default()
    });
    let decision = agent.decide();
    assert!(
        decision.is_none(),
        "agent should hold price in normal range"
    );
}

#[test]
fn respects_max_price() {
    let mut agent = make_agent(10, 3);
    agent.observe(AgentObservation {
        backlog: 100,
        current_price: 49.5,
        ..Default::default()
    });
    let decision = agent.decide();
    assert!(decision.is_some());
    let new_price = decision.unwrap();
    assert!(
        new_price <= 50.0,
        "price should not exceed max: got {}",
        new_price
    );
}

#[test]
fn respects_min_price() {
    let mut agent = make_agent(10, 3);
    agent.observe(AgentObservation {
        backlog: 0,
        current_price: 1.05,
        ..Default::default()
    });
    let decision = agent.decide();
    assert!(decision.is_some());
    let new_price = decision.unwrap();
    assert!(
        new_price >= 1.0,
        "price should not go below min: got {}",
        new_price
    );
}

#[test]
fn handle_event_schedules_price_change() {
    let mut agent = make_agent(10, 3);
    agent.observe(AgentObservation {
        backlog: 20,
        current_price: 10.0,
        ..Default::default()
    });

    let mut scheduler = Scheduler::new();
    scheduler
        .schedule(Event::new(SimTime(100), EventPayload::AgentEvaluation))
        .unwrap();
    let event = scheduler.next_event().unwrap();

    agent.handle_event(&event, &mut scheduler).unwrap();

    assert_eq!(agent.interventions, 1);
    assert!(
        !scheduler.is_empty(),
        "should have scheduled PriceChange + AgentDecision"
    );

    let ev1 = scheduler.next_event().unwrap();
    let ev2 = scheduler.next_event().unwrap();

    let types: Vec<EventType> = vec![ev1.event_type, ev2.event_type];
    assert!(types.contains(&EventType::PriceChange));
    assert!(types.contains(&EventType::AgentDecision));
}

#[test]
fn handle_event_ignores_non_agent_events() {
    let mut agent = make_agent(10, 3);
    agent.observe(AgentObservation {
        backlog: 20,
        current_price: 10.0,
        ..Default::default()
    });

    let mut scheduler = Scheduler::new();
    scheduler
        .schedule(Event::new(SimTime(100), EventPayload::DemandEvaluation))
        .unwrap();
    let event = scheduler.next_event().unwrap();

    agent.handle_event(&event, &mut scheduler).unwrap();
    assert_eq!(agent.interventions, 0);
    assert!(scheduler.is_empty());
}

#[test]
fn tracks_intervention_count() {
    let mut agent = make_agent(5, 2);
    let mut scheduler = Scheduler::new();

    for tick in [100, 200, 300] {
        agent.observe(AgentObservation {
            backlog: 10,
            current_price: 10.0,
            ..Default::default()
        });
        scheduler
            .schedule(Event::new(SimTime(tick), EventPayload::AgentEvaluation))
            .unwrap();
        let event = scheduler.next_event().unwrap();
        agent.handle_event(&event, &mut scheduler).unwrap();
        // Drain scheduled events
        while scheduler.next_event().is_some() {}
    }

    assert_eq!(agent.interventions, 3);
}

#[test]
fn default_config_is_sensible() {
    let agent = SalesAgent::with_default_config();
    assert!(agent.config.backlog_high > agent.config.backlog_low);
    assert!(agent.config.min_price > 0.0);
    assert!(agent.config.max_price > agent.config.min_price);
    assert!(agent.config.adjustment_pct > 0.0 && agent.config.adjustment_pct < 1.0);
}
