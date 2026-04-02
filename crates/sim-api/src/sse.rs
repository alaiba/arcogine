//! Server-Sent Events (SSE) endpoint for real-time simulation event delivery.
//!
//! Subscribes to the `tokio::sync::broadcast` channel fed by the simulation
//! thread. Disconnection and reconnection are handled gracefully — each new
//! connection gets a fresh subscription from the broadcast channel.

use axum::extract::State;
use axum::response::sse::{Event as SseEvent, KeepAlive, Sse};
use futures_core::Stream;
use std::convert::Infallible;
use std::sync::Arc;
use tokio_stream::wrappers::BroadcastStream;
use tokio_stream::StreamExt;

use crate::state::AppState;

pub async fn event_stream(
    State(state): State<Arc<AppState>>,
) -> Sse<impl Stream<Item = Result<SseEvent, Infallible>>> {
    let rx = state.event_tx.subscribe();
    let stream = BroadcastStream::new(rx).filter_map(|result| match result {
        Ok(event) => {
            let json = serde_json::to_string(&event).unwrap_or_default();
            Some(Ok(SseEvent::default()
                .event(format!("{:?}", event.event_type))
                .data(json)))
        }
        Err(_) => None,
    });

    Sse::new(stream).keep_alive(KeepAlive::default())
}
