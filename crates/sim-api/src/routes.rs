//! API route handlers for the Arcogine simulation.

use axum::extract::State;
use axum::http::StatusCode;
use axum::response::Json;
use serde::{Deserialize, Serialize};
use std::sync::Arc;

use crate::state::{AppState, JobInfo, SimCommand, SimRunState, SimSnapshot, TopologySnapshot};

#[derive(Serialize)]
pub struct HealthResponse {
    pub status: &'static str,
}

pub async fn health() -> Json<HealthResponse> {
    Json(HealthResponse { status: "ok" })
}

#[derive(Deserialize)]
pub struct LoadScenarioRequest {
    pub toml: String,
}

#[derive(Serialize)]
pub struct LoadScenarioResponse {
    pub success: bool,
    pub message: String,
}

pub async fn load_scenario(
    State(state): State<Arc<AppState>>,
    Json(body): Json<LoadScenarioRequest>,
) -> Result<Json<LoadScenarioResponse>, (StatusCode, Json<ErrorResponse>)> {
    let (tx, rx) = std::sync::mpsc::sync_channel(1);
    state
        .cmd_tx
        .send(SimCommand::LoadScenario { toml: body.toml, reply: tx })
        .map_err(|_| sim_error("Failed to send command to simulation thread"))?;

    match rx.recv_timeout(std::time::Duration::from_secs(5)) {
        Ok(Ok(())) => Ok(Json(LoadScenarioResponse {
            success: true,
            message: "Scenario loaded".to_string(),
        })),
        Ok(Err(msg)) => Err(bad_request(&msg)),
        Err(_) => Err(sim_error("Scenario load timed out")),
    }
}

#[derive(Serialize)]
pub struct ErrorResponse {
    pub error: String,
}

fn sim_error(msg: &str) -> (StatusCode, Json<ErrorResponse>) {
    (
        StatusCode::INTERNAL_SERVER_ERROR,
        Json(ErrorResponse {
            error: msg.to_string(),
        }),
    )
}

fn bad_request(msg: &str) -> (StatusCode, Json<ErrorResponse>) {
    (
        StatusCode::BAD_REQUEST,
        Json(ErrorResponse {
            error: msg.to_string(),
        }),
    )
}

fn conflict(msg: &str) -> (StatusCode, Json<ErrorResponse>) {
    (
        StatusCode::CONFLICT,
        Json(ErrorResponse {
            error: msg.to_string(),
        }),
    )
}

pub async fn run_sim(
    State(state): State<Arc<AppState>>,
) -> Result<Json<SimSnapshot>, (StatusCode, Json<ErrorResponse>)> {
    {
        let snap = state.snapshot_rx.borrow();
        if !snap.scenario_loaded {
            return Err(conflict("No scenario loaded"));
        }
        if snap.run_state == SimRunState::Completed {
            return Err(conflict("Simulation already completed; reset first"));
        }
    }

    state
        .cmd_tx
        .send(SimCommand::Run)
        .map_err(|_| sim_error("Failed to send command"))?;

    tokio::time::sleep(std::time::Duration::from_millis(100)).await;
    let snap = state.snapshot_rx.borrow().clone();
    Ok(Json(snap))
}

pub async fn pause_sim(
    State(state): State<Arc<AppState>>,
) -> Result<Json<SimSnapshot>, (StatusCode, Json<ErrorResponse>)> {
    state
        .cmd_tx
        .send(SimCommand::Pause)
        .map_err(|_| sim_error("Failed to send command"))?;

    tokio::time::sleep(std::time::Duration::from_millis(50)).await;
    let snap = state.snapshot_rx.borrow().clone();
    Ok(Json(snap))
}

pub async fn step_sim(
    State(state): State<Arc<AppState>>,
) -> Result<Json<SimSnapshot>, (StatusCode, Json<ErrorResponse>)> {
    {
        let snap = state.snapshot_rx.borrow();
        if !snap.scenario_loaded {
            return Err(conflict("No scenario loaded"));
        }
        if snap.run_state == SimRunState::Completed {
            return Err(conflict("Simulation already completed; reset first"));
        }
    }

    state
        .cmd_tx
        .send(SimCommand::Step)
        .map_err(|_| sim_error("Failed to send command"))?;

    tokio::time::sleep(std::time::Duration::from_millis(50)).await;
    let snap = state.snapshot_rx.borrow().clone();
    Ok(Json(snap))
}

pub async fn reset_sim(
    State(state): State<Arc<AppState>>,
) -> Result<Json<SimSnapshot>, (StatusCode, Json<ErrorResponse>)> {
    {
        let snap = state.snapshot_rx.borrow();
        if !snap.scenario_loaded {
            return Err(conflict("No scenario loaded; load a scenario first"));
        }
    }

    state
        .cmd_tx
        .send(SimCommand::Reset)
        .map_err(|_| sim_error("Failed to send command"))?;

    tokio::time::sleep(std::time::Duration::from_millis(50)).await;
    let snap = state.snapshot_rx.borrow().clone();
    Ok(Json(snap))
}

#[derive(Deserialize)]
pub struct ChangePriceRequest {
    pub price: f64,
}

pub async fn change_price(
    State(state): State<Arc<AppState>>,
    Json(body): Json<ChangePriceRequest>,
) -> Result<Json<SimSnapshot>, (StatusCode, Json<ErrorResponse>)> {
    const MAX_PRICE: f64 = 1_000_000.0;
    if body.price < 0.0 || body.price > MAX_PRICE {
        return Err(bad_request("Price must be between 0 and 1,000,000"));
    }

    {
        let snap = state.snapshot_rx.borrow();
        if !snap.scenario_loaded {
            return Err(conflict("No scenario loaded"));
        }
    }

    state
        .cmd_tx
        .send(SimCommand::ChangePrice(body.price))
        .map_err(|_| sim_error("Failed to send command"))?;

    tokio::time::sleep(std::time::Duration::from_millis(50)).await;
    let snap = state.snapshot_rx.borrow().clone();
    Ok(Json(snap))
}

#[derive(Deserialize)]
pub struct ChangeMachineRequest {
    pub machine_id: u64,
    pub online: bool,
}

pub async fn change_machine(
    State(state): State<Arc<AppState>>,
    Json(body): Json<ChangeMachineRequest>,
) -> Result<Json<SimSnapshot>, (StatusCode, Json<ErrorResponse>)> {
    {
        let snap = state.snapshot_rx.borrow();
        if !snap.scenario_loaded {
            return Err(conflict("No scenario loaded"));
        }
    }

    state
        .cmd_tx
        .send(SimCommand::ChangeMachineCount {
            machine_id: body.machine_id,
            online: body.online,
        })
        .map_err(|_| sim_error("Failed to send command"))?;

    tokio::time::sleep(std::time::Duration::from_millis(50)).await;
    let snap = state.snapshot_rx.borrow().clone();
    Ok(Json(snap))
}

#[derive(Deserialize)]
pub struct ToggleAgentRequest {
    pub enabled: bool,
}

pub async fn toggle_agent(
    State(state): State<Arc<AppState>>,
    Json(body): Json<ToggleAgentRequest>,
) -> Result<Json<SimSnapshot>, (StatusCode, Json<ErrorResponse>)> {
    state
        .cmd_tx
        .send(SimCommand::ToggleAgent(body.enabled))
        .map_err(|_| sim_error("Failed to send command"))?;

    tokio::time::sleep(std::time::Duration::from_millis(50)).await;
    let snap = state.snapshot_rx.borrow().clone();
    Ok(Json(snap))
}

pub async fn query_kpis(State(state): State<Arc<AppState>>) -> Json<Vec<sim_core::kpi::KpiValue>> {
    let snap = state.snapshot_rx.borrow().clone();
    Json(snap.kpis)
}

pub async fn query_snapshot(State(state): State<Arc<AppState>>) -> Json<SimSnapshot> {
    let snap = state.snapshot_rx.borrow().clone();
    Json(snap)
}

pub async fn query_topology(State(state): State<Arc<AppState>>) -> Json<TopologySnapshot> {
    let snap = state.snapshot_rx.borrow().clone();
    Json(snap.topology)
}

pub async fn query_jobs(State(state): State<Arc<AppState>>) -> Json<Vec<JobInfo>> {
    let snap = state.snapshot_rx.borrow().clone();
    Json(snap.jobs)
}

pub async fn export_events(State(state): State<Arc<AppState>>) -> Json<sim_core::log::EventLog> {
    let log = state.event_log_rx.borrow().clone();
    Json(log)
}
