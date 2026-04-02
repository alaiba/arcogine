//! API smoke tests using tower::ServiceExt to test routes without starting
//! an HTTP server. Includes error-path cases per F29.

use axum::body::Body;
use axum::http::{self, Request, StatusCode};
use http_body_util::BodyExt;
use sim_api::server::{build_router, create_app_state};
use tower::ServiceExt;

fn basic_scenario_toml() -> &'static str {
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
base_demand = 3.0
price_elasticity = 0.3
lead_time_sensitivity = 0.0
"#
}

async fn load_scenario(app: &axum::Router, toml: &str) -> StatusCode {
    let body = serde_json::json!({ "toml": toml });
    let req = Request::builder()
        .method(http::Method::POST)
        .uri("/api/scenario")
        .header("content-type", "application/json")
        .body(Body::from(serde_json::to_string(&body).unwrap()))
        .unwrap();
    let resp = app.clone().oneshot(req).await.unwrap();
    resp.status()
}

// ─── Happy-path tests ───────────────────────────────────────────────

#[tokio::test]
async fn health_endpoint_returns_ok() {
    let state = create_app_state();
    let app = build_router(state);

    let req = Request::builder()
        .uri("/api/health")
        .body(Body::empty())
        .unwrap();

    let resp = app.oneshot(req).await.unwrap();
    assert_eq!(resp.status(), StatusCode::OK);

    let body = resp.into_body().collect().await.unwrap().to_bytes();
    let json: serde_json::Value = serde_json::from_slice(&body).unwrap();
    assert_eq!(json["status"], "ok");
}

#[tokio::test]
async fn load_scenario_succeeds() {
    let state = create_app_state();
    let app = build_router(state);

    let status = load_scenario(&app, basic_scenario_toml()).await;
    assert_eq!(status, StatusCode::OK);
}

#[tokio::test]
async fn step_after_load_returns_updated_state() {
    let state = create_app_state();
    let app = build_router(state);

    load_scenario(&app, basic_scenario_toml()).await;
    tokio::time::sleep(std::time::Duration::from_millis(100)).await;

    let req = Request::builder()
        .method(http::Method::POST)
        .uri("/api/sim/step")
        .body(Body::empty())
        .unwrap();

    let resp = app.clone().oneshot(req).await.unwrap();
    assert_eq!(resp.status(), StatusCode::OK);

    let body = resp.into_body().collect().await.unwrap().to_bytes();
    let json: serde_json::Value = serde_json::from_slice(&body).unwrap();
    assert!(json["events_processed"].as_u64().unwrap() > 0);
}

#[tokio::test]
async fn run_and_query_kpis() {
    let state = create_app_state();
    let app = build_router(state);

    load_scenario(&app, basic_scenario_toml()).await;
    tokio::time::sleep(std::time::Duration::from_millis(100)).await;

    let req = Request::builder()
        .method(http::Method::POST)
        .uri("/api/sim/run")
        .body(Body::empty())
        .unwrap();
    let resp = app.clone().oneshot(req).await.unwrap();
    assert_eq!(resp.status(), StatusCode::OK);

    tokio::time::sleep(std::time::Duration::from_millis(100)).await;

    let req = Request::builder()
        .uri("/api/kpis")
        .body(Body::empty())
        .unwrap();
    let resp = app.clone().oneshot(req).await.unwrap();
    assert_eq!(resp.status(), StatusCode::OK);

    let body = resp.into_body().collect().await.unwrap().to_bytes();
    let kpis: Vec<serde_json::Value> = serde_json::from_slice(&body).unwrap();
    assert!(!kpis.is_empty(), "KPIs should be returned");
}

#[tokio::test]
async fn change_price_returns_updated_snapshot() {
    let state = create_app_state();
    let app = build_router(state);

    load_scenario(&app, basic_scenario_toml()).await;
    tokio::time::sleep(std::time::Duration::from_millis(100)).await;

    let body = serde_json::json!({ "price": 15.0 });
    let req = Request::builder()
        .method(http::Method::POST)
        .uri("/api/price")
        .header("content-type", "application/json")
        .body(Body::from(serde_json::to_string(&body).unwrap()))
        .unwrap();

    let resp = app.clone().oneshot(req).await.unwrap();
    assert_eq!(resp.status(), StatusCode::OK);

    let body = resp.into_body().collect().await.unwrap().to_bytes();
    let json: serde_json::Value = serde_json::from_slice(&body).unwrap();
    assert_eq!(json["current_price"], 15.0);
}

#[tokio::test]
async fn topology_returns_machines() {
    let state = create_app_state();
    let app = build_router(state);

    load_scenario(&app, basic_scenario_toml()).await;
    tokio::time::sleep(std::time::Duration::from_millis(100)).await;

    let req = Request::builder()
        .uri("/api/factory/topology")
        .body(Body::empty())
        .unwrap();

    let resp = app.clone().oneshot(req).await.unwrap();
    assert_eq!(resp.status(), StatusCode::OK);

    let body = resp.into_body().collect().await.unwrap().to_bytes();
    let json: serde_json::Value = serde_json::from_slice(&body).unwrap();
    let machines = json["machines"].as_array().unwrap();
    assert!(!machines.is_empty(), "topology should contain machines");
}

#[tokio::test]
async fn export_events_returns_log() {
    let state = create_app_state();
    let app = build_router(state);

    load_scenario(&app, basic_scenario_toml()).await;
    tokio::time::sleep(std::time::Duration::from_millis(100)).await;

    // Step a few times to generate events
    for _ in 0..3 {
        let req = Request::builder()
            .method(http::Method::POST)
            .uri("/api/sim/step")
            .body(Body::empty())
            .unwrap();
        app.clone().oneshot(req).await.unwrap();
        tokio::time::sleep(std::time::Duration::from_millis(50)).await;
    }

    let req = Request::builder()
        .uri("/api/export/events")
        .body(Body::empty())
        .unwrap();
    let resp = app.clone().oneshot(req).await.unwrap();
    assert_eq!(resp.status(), StatusCode::OK);
}

// ─── Error-path tests (F29) ─────────────────────────────────────────

#[tokio::test]
async fn run_without_scenario_returns_conflict() {
    let state = create_app_state();
    let app = build_router(state);

    let req = Request::builder()
        .method(http::Method::POST)
        .uri("/api/sim/run")
        .body(Body::empty())
        .unwrap();

    let resp = app.oneshot(req).await.unwrap();
    assert_eq!(resp.status(), StatusCode::CONFLICT);
}

#[tokio::test]
async fn step_without_scenario_returns_conflict() {
    let state = create_app_state();
    let app = build_router(state);

    let req = Request::builder()
        .method(http::Method::POST)
        .uri("/api/sim/step")
        .body(Body::empty())
        .unwrap();

    let resp = app.oneshot(req).await.unwrap();
    assert_eq!(resp.status(), StatusCode::CONFLICT);
}

#[tokio::test]
async fn negative_price_returns_bad_request() {
    let state = create_app_state();
    let app = build_router(state);

    load_scenario(&app, basic_scenario_toml()).await;
    tokio::time::sleep(std::time::Duration::from_millis(100)).await;

    let body = serde_json::json!({ "price": -5.0 });
    let req = Request::builder()
        .method(http::Method::POST)
        .uri("/api/price")
        .header("content-type", "application/json")
        .body(Body::from(serde_json::to_string(&body).unwrap()))
        .unwrap();

    let resp = app.clone().oneshot(req).await.unwrap();
    assert_eq!(resp.status(), StatusCode::BAD_REQUEST);
}

#[tokio::test]
async fn malformed_json_returns_error() {
    let state = create_app_state();
    let app = build_router(state);

    let req = Request::builder()
        .method(http::Method::POST)
        .uri("/api/scenario")
        .header("content-type", "application/json")
        .body(Body::from("not valid json"))
        .unwrap();

    let resp = app.oneshot(req).await.unwrap();
    assert!(
        resp.status().is_client_error(),
        "malformed JSON should return 4xx, got {}",
        resp.status()
    );
}

#[tokio::test]
async fn price_change_without_scenario_returns_conflict() {
    let state = create_app_state();
    let app = build_router(state);

    let body = serde_json::json!({ "price": 10.0 });
    let req = Request::builder()
        .method(http::Method::POST)
        .uri("/api/price")
        .header("content-type", "application/json")
        .body(Body::from(serde_json::to_string(&body).unwrap()))
        .unwrap();

    let resp = app.oneshot(req).await.unwrap();
    assert_eq!(resp.status(), StatusCode::CONFLICT);
}

#[tokio::test]
async fn reset_without_scenario_returns_conflict() {
    let state = create_app_state();
    let app = build_router(state);

    let req = Request::builder()
        .method(http::Method::POST)
        .uri("/api/sim/reset")
        .body(Body::empty())
        .unwrap();

    let resp = app.oneshot(req).await.unwrap();
    assert_eq!(resp.status(), StatusCode::CONFLICT);
}
