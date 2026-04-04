//! Axum HTTP server setup with routing, middleware, CORS, and OpenAPI spec.

use axum::extract::DefaultBodyLimit;
use axum::http::{HeaderValue, Method};
use axum::routing::{get, post};
use axum::Router;
use std::sync::Arc;
use tower_http::cors::{Any, CorsLayer};
use tower_http::trace::TraceLayer;

use crate::routes;
use crate::sse;
use crate::state::{spawn_sim_thread, AppState};

/// Build the application router with all middleware configured.
pub fn build_router(state: Arc<AppState>) -> Router {
    let cors = build_cors_layer();
    build_router_with_cors(state, cors)
}

/// Build CORS layer from `CORS_ALLOWED_ORIGIN` env var; falls back to permissive.
pub fn build_cors_layer() -> CorsLayer {
    let methods = [
        Method::GET,
        Method::POST,
        Method::PUT,
        Method::DELETE,
        Method::OPTIONS,
    ];

    match std::env::var("CORS_ALLOWED_ORIGIN") {
        Ok(origin) => CorsLayer::new()
            .allow_origin(origin.parse::<HeaderValue>().expect("invalid CORS origin"))
            .allow_methods(methods)
            .allow_headers(Any),
        Err(_) => CorsLayer::new()
            .allow_origin(Any)
            .allow_methods(methods)
            .allow_headers(Any),
    }
}

/// Build the router with a specific CORS layer (useful for testing).
pub fn build_router_with_cors(state: Arc<AppState>, cors: CorsLayer) -> Router {
    Router::new()
        .route("/api/health", get(routes::health))
        .route("/api/scenario", post(routes::load_scenario))
        .route("/api/sim/run", post(routes::run_sim))
        .route("/api/sim/pause", post(routes::pause_sim))
        .route("/api/sim/step", post(routes::step_sim))
        .route("/api/sim/reset", post(routes::reset_sim))
        .route("/api/price", post(routes::change_price))
        .route("/api/machines", post(routes::change_machine))
        .route("/api/agent", post(routes::toggle_agent))
        .route("/api/kpis", get(routes::query_kpis))
        .route("/api/snapshot", get(routes::query_snapshot))
        .route("/api/factory/topology", get(routes::query_topology))
        .route("/api/jobs", get(routes::query_jobs))
        .route("/api/export/events", get(routes::export_events))
        .route("/api/events/stream", get(sse::event_stream))
        .with_state(state)
        .layer(TraceLayer::new_for_http())
        .layer(cors)
        .layer(DefaultBodyLimit::max(1024 * 1024))
}

/// Create shared application state by spawning the simulation thread.
pub fn create_app_state() -> Arc<AppState> {
    let (cmd_tx, snapshot_rx, event_tx, event_log_rx) = spawn_sim_thread();
    Arc::new(AppState {
        cmd_tx,
        snapshot_rx,
        event_tx,
        event_log_rx,
        sse_semaphore: Arc::new(tokio::sync::Semaphore::new(64)),
    })
}

/// Start the HTTP server on the given address.
pub async fn start_server(addr: &str) -> std::io::Result<()> {
    let state = create_app_state();
    let app = build_router(state);

    tracing::info!("Starting Arcogine API server on {}", addr);
    let listener = tokio::net::TcpListener::bind(addr).await?;
    axum::serve(listener, app).await?;
    Ok(())
}
