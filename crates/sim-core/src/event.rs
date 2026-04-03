//! Event types for the discrete-event simulation.

use serde::{Deserialize, Serialize};
use sim_types::{JobId, MachineId, ProductId, SimTime};

/// The type discriminant for an event, used for filtering.
#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub enum EventType {
    OrderCreation,
    TaskStart,
    TaskEnd,
    MachineAvailabilityChange,
    PriceChange,
    AgentDecision,
    DemandEvaluation,
    AgentEvaluation,
}

/// Payload for each event variant.
#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
pub enum EventPayload {
    OrderCreation {
        product_id: ProductId,
        quantity: u64,
    },
    TaskStart {
        job_id: JobId,
        machine_id: MachineId,
        step_index: usize,
    },
    TaskEnd {
        job_id: JobId,
        machine_id: MachineId,
        step_index: usize,
    },
    MachineAvailabilityChange {
        machine_id: MachineId,
        online: bool,
    },
    PriceChange {
        new_price: f64,
    },
    AgentDecision {
        description: String,
    },
    DemandEvaluation,
    AgentEvaluation,
}

/// A simulation event with a scheduled time and payload.
#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
pub struct Event {
    pub time: SimTime,
    pub event_type: EventType,
    pub payload: EventPayload,
}

impl Event {
    pub fn new(time: SimTime, payload: EventPayload) -> Self {
        let event_type = match &payload {
            EventPayload::OrderCreation { .. } => EventType::OrderCreation,
            EventPayload::TaskStart { .. } => EventType::TaskStart,
            EventPayload::TaskEnd { .. } => EventType::TaskEnd,
            EventPayload::MachineAvailabilityChange { .. } => EventType::MachineAvailabilityChange,
            EventPayload::PriceChange { .. } => EventType::PriceChange,
            EventPayload::AgentDecision { .. } => EventType::AgentDecision,
            EventPayload::DemandEvaluation => EventType::DemandEvaluation,
            EventPayload::AgentEvaluation => EventType::AgentEvaluation,
        };
        Event {
            time,
            event_type,
            payload,
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn event_new_sets_time_and_payload() {
        let e = Event::new(
            SimTime(42),
            EventPayload::OrderCreation {
                product_id: ProductId(1),
                quantity: 5,
            },
        );
        assert_eq!(e.time, SimTime(42));
        assert!(matches!(
            e.payload,
            EventPayload::OrderCreation {
                product_id: ProductId(1),
                quantity: 5
            }
        ));
    }

    #[test]
    fn event_type_derived_from_each_payload_variant() {
        let cases: Vec<(EventPayload, EventType)> = vec![
            (
                EventPayload::OrderCreation {
                    product_id: ProductId(1),
                    quantity: 1,
                },
                EventType::OrderCreation,
            ),
            (
                EventPayload::TaskStart {
                    job_id: JobId(1),
                    machine_id: MachineId(1),
                    step_index: 0,
                },
                EventType::TaskStart,
            ),
            (
                EventPayload::TaskEnd {
                    job_id: JobId(1),
                    machine_id: MachineId(1),
                    step_index: 0,
                },
                EventType::TaskEnd,
            ),
            (
                EventPayload::MachineAvailabilityChange {
                    machine_id: MachineId(1),
                    online: true,
                },
                EventType::MachineAvailabilityChange,
            ),
            (
                EventPayload::PriceChange { new_price: 1.0 },
                EventType::PriceChange,
            ),
            (
                EventPayload::AgentDecision {
                    description: "test".into(),
                },
                EventType::AgentDecision,
            ),
            (EventPayload::DemandEvaluation, EventType::DemandEvaluation),
            (EventPayload::AgentEvaluation, EventType::AgentEvaluation),
        ];

        for (payload, expected_type) in cases {
            let event = Event::new(SimTime::ZERO, payload);
            assert_eq!(event.event_type, expected_type);
        }
    }
}
