//! Server-Sent Events (SSE) endpoint for real-time simulation event delivery.
//!
//! Subscribes to the `tokio::sync::broadcast` channel fed by the simulation
//! thread. Disconnection and reconnection are handled gracefully — each new
//! connection gets a fresh subscription from the broadcast channel.

use axum::extract::State;
use axum::http::StatusCode;
use axum::response::sse::{Event as SseEvent, KeepAlive, Sse};
use futures_core::Stream;
use std::convert::Infallible;
use std::sync::Arc;
use tokio_stream::wrappers::BroadcastStream;
use tokio_stream::StreamExt;

use crate::state::AppState;

pub async fn event_stream(
    State(state): State<Arc<AppState>>,
) -> Result<Sse<impl Stream<Item = Result<SseEvent, Infallible>>>, StatusCode> {
    let permit = state.sse_semaphore.clone().try_acquire_owned()
        .map_err(|_| StatusCode::SERVICE_UNAVAILABLE)?;

    let rx = state.event_tx.subscribe();
    let stream = BroadcastStream::new(rx).filter_map(move |result| {
        let _permit = &permit;
        match result {
            Ok(event) => {
                let json = serde_json::to_string(&event).unwrap_or_default();
                Some(Ok(SseEvent::default()
                    .event(format!("{:?}", event.event_type))
                    .data(json)))
            }
            Err(_) => None,
        }
    });

    Ok(Sse::new(stream).keep_alive(KeepAlive::default()))
}

#[cfg(test)]
mod tests {
    use sim_core::event::{Event, EventPayload, EventType};
    use sim_types::SimTime;
    use tokio::sync::broadcast;

    #[test]
    fn event_serializes_with_expected_type_name() {
        let event = Event::new(
            SimTime(10),
            EventPayload::TaskEnd {
                job_id: sim_types::JobId(1),
                machine_id: sim_types::MachineId(1),
                step_index: 0,
            },
        );
        let json = serde_json::to_string(&event).unwrap();
        assert!(json.contains("\"event_type\":\"TaskEnd\""));
        assert_eq!(event.event_type, EventType::TaskEnd);
    }

    #[test]
    fn broadcast_receive_error_is_handled() {
        let (tx, _rx) = broadcast::channel::<Event>(1);
        let mut rx2 = tx.subscribe();

        tx.send(Event::new(SimTime(1), EventPayload::DemandEvaluation))
            .unwrap();
        tx.send(Event::new(SimTime(2), EventPayload::DemandEvaluation))
            .unwrap();

        match rx2.try_recv() {
            Err(broadcast::error::TryRecvError::Lagged(_)) => {}
            other => panic!("expected Lagged, got {:?}", other),
        }
    }
}
