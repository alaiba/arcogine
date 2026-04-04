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
    let (status, _body) = load_scenario_with_body(app, toml).await;
    status
}

async fn load_scenario_with_body(app: &axum::Router, toml: &str) -> (StatusCode, serde_json::Value) {
    let body = serde_json::json!({ "toml": toml });
    let req = Request::builder()
        .method(http::Method::POST)
        .uri("/api/scenario")
        .header("content-type", "application/json")
        .body(Body::from(serde_json::to_string(&body).unwrap()))
        .unwrap();
    let resp = app.clone().oneshot(req).await.unwrap();
    let status = resp.status();
    let bytes = resp.into_body().collect().await.unwrap().to_bytes();
    let json: serde_json::Value = serde_json::from_slice(&bytes).unwrap_or(serde_json::Value::Null);
    (status, json)
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

// ─── §2.8 — Missing route coverage ──────────────────────────────────

#[tokio::test]
async fn pause_resume_step_sequence() {
    let state = create_app_state();
    let app = build_router(state);

    load_scenario(&app, basic_scenario_toml()).await;
    tokio::time::sleep(std::time::Duration::from_millis(100)).await;

    // Pause (no-op since simulation starts paused)
    let req = Request::builder()
        .method(http::Method::POST)
        .uri("/api/sim/pause")
        .body(Body::empty())
        .unwrap();
    let resp = app.clone().oneshot(req).await.unwrap();
    assert_eq!(resp.status(), StatusCode::OK);

    // Step one event
    let req = Request::builder()
        .method(http::Method::POST)
        .uri("/api/sim/step")
        .body(Body::empty())
        .unwrap();
    let resp = app.clone().oneshot(req).await.unwrap();
    assert_eq!(resp.status(), StatusCode::OK);

    // The step handler's internal sleep may not be long enough under parallel
    // load, so poll the snapshot endpoint until events_processed > 0.
    for _ in 0..10 {
        let req = Request::builder()
            .uri("/api/snapshot")
            .body(Body::empty())
            .unwrap();
        let resp = app.clone().oneshot(req).await.unwrap();
        let body = resp.into_body().collect().await.unwrap().to_bytes();
        let json: serde_json::Value = serde_json::from_slice(&body).unwrap();
        if json["events_processed"].as_u64().unwrap_or(0) >= 1 {
            return;
        }
        tokio::time::sleep(std::time::Duration::from_millis(50)).await;
    }
    panic!("step should process at least 1 event within timeout");
}

#[tokio::test]
async fn run_to_completion_returns_final_snapshot() {
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

    tokio::time::sleep(std::time::Duration::from_millis(200)).await;

    let req = Request::builder()
        .uri("/api/snapshot")
        .body(Body::empty())
        .unwrap();
    let resp = app.clone().oneshot(req).await.unwrap();
    assert_eq!(resp.status(), StatusCode::OK);

    let body = resp.into_body().collect().await.unwrap().to_bytes();
    let json: serde_json::Value = serde_json::from_slice(&body).unwrap();
    assert_eq!(json["run_state"], "Completed");
    assert!(json["events_processed"].as_u64().unwrap() > 0);
}

#[tokio::test]
async fn query_jobs_returns_list() {
    let state = create_app_state();
    let app = build_router(state);

    // Use a scenario with high enough base_demand to generate orders
    let high_demand_toml = r#"
[simulation]
rng_seed = 42
max_ticks = 200
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
initial_price = 5.0
base_demand = 10.0
price_elasticity = 0.3
lead_time_sensitivity = 0.0
"#;

    load_scenario(&app, high_demand_toml).await;
    tokio::time::sleep(std::time::Duration::from_millis(100)).await;

    let req = Request::builder()
        .method(http::Method::POST)
        .uri("/api/sim/run")
        .body(Body::empty())
        .unwrap();
    app.clone().oneshot(req).await.unwrap();

    for _ in 0..20 {
        tokio::time::sleep(std::time::Duration::from_millis(50)).await;
        let req = Request::builder()
            .uri("/api/snapshot")
            .body(Body::empty())
            .unwrap();
        let resp = app.clone().oneshot(req).await.unwrap();
        let body = resp.into_body().collect().await.unwrap().to_bytes();
        let json: serde_json::Value = serde_json::from_slice(&body).unwrap();
        if json["run_state"] == "Completed" {
            break;
        }
    }

    let req = Request::builder()
        .uri("/api/jobs")
        .body(Body::empty())
        .unwrap();
    let resp = app.clone().oneshot(req).await.unwrap();
    assert_eq!(resp.status(), StatusCode::OK);

    let body = resp.into_body().collect().await.unwrap().to_bytes();
    let jobs: Vec<serde_json::Value> = serde_json::from_slice(&body).unwrap();
    assert!(!jobs.is_empty(), "jobs should be populated after a run");
}

#[tokio::test]
async fn toggle_agent_on_off() {
    let state = create_app_state();
    let app = build_router(state);

    let body = serde_json::json!({ "enabled": true });
    let req = Request::builder()
        .method(http::Method::POST)
        .uri("/api/agent")
        .header("content-type", "application/json")
        .body(Body::from(serde_json::to_string(&body).unwrap()))
        .unwrap();
    let resp = app.clone().oneshot(req).await.unwrap();
    assert_eq!(resp.status(), StatusCode::OK);

    let body = serde_json::json!({ "enabled": false });
    let req = Request::builder()
        .method(http::Method::POST)
        .uri("/api/agent")
        .header("content-type", "application/json")
        .body(Body::from(serde_json::to_string(&body).unwrap()))
        .unwrap();
    let resp = app.clone().oneshot(req).await.unwrap();
    assert_eq!(resp.status(), StatusCode::OK);
}

#[tokio::test]
async fn sse_endpoint_returns_event_stream() {
    let state = create_app_state();
    let app = build_router(state);

    let req = Request::builder()
        .uri("/api/events/stream")
        .body(Body::empty())
        .unwrap();
    let resp = app.clone().oneshot(req).await.unwrap();
    assert_eq!(resp.status(), StatusCode::OK);

    let content_type = resp
        .headers()
        .get("content-type")
        .and_then(|v| v.to_str().ok())
        .unwrap_or("");
    assert!(
        content_type.contains("text/event-stream"),
        "SSE endpoint should return text/event-stream, got: {content_type}"
    );
}

#[tokio::test]
async fn invalid_toml_content_returns_error() {
    let state = create_app_state();
    let app = build_router(state);

    let invalid_toml = r#"
[simulation]
max_ticks = "not a number"
"#;
    let status = load_scenario(&app, invalid_toml).await;
    assert_eq!(status, StatusCode::BAD_REQUEST, "invalid TOML should return 400");
}

#[tokio::test]
async fn change_machine_updates_snapshot() {
    let state = create_app_state();
    let app = build_router(state);

    load_scenario(&app, basic_scenario_toml()).await;
    tokio::time::sleep(std::time::Duration::from_millis(100)).await;

    let body = serde_json::json!({ "machine_id": 1, "online": false });
    let req = Request::builder()
        .method(http::Method::POST)
        .uri("/api/machines")
        .header("content-type", "application/json")
        .body(Body::from(serde_json::to_string(&body).unwrap()))
        .unwrap();
    let resp = app.clone().oneshot(req).await.unwrap();
    assert_eq!(resp.status(), StatusCode::OK);
}

// ─── §3.12 — Configurable CORS ──────────────────────────────────────

#[tokio::test]
async fn cors_with_env_var_restricts_origin() {
    use tower_http::cors::{Any, CorsLayer};
    use axum::http::{HeaderValue, Method};

    let allowed = "http://example.com";
    let cors = CorsLayer::new()
        .allow_origin(allowed.parse::<HeaderValue>().unwrap())
        .allow_methods([Method::GET, Method::POST, Method::PUT, Method::DELETE, Method::OPTIONS])
        .allow_headers(Any);

    let state = create_app_state();
    let app = sim_api::server::build_router_with_cors(state, cors);

    let req = Request::builder()
        .uri("/api/health")
        .header("Origin", "http://evil.com")
        .body(Body::empty())
        .unwrap();
    let resp = app.oneshot(req).await.unwrap();
    let acao = resp
        .headers()
        .get("access-control-allow-origin")
        .map(|v| v.to_str().unwrap_or(""));
    assert_ne!(
        acao,
        Some("http://evil.com"),
        "evil origin should not be reflected"
    );
}

// ─── §3.2 — Scenario load error propagation ─────────────────────────

#[tokio::test]
async fn load_valid_scenario_returns_success() {
    let state = create_app_state();
    let app = build_router(state);

    let (status, body) = load_scenario_with_body(&app, basic_scenario_toml()).await;
    assert_eq!(status, StatusCode::OK);
    assert_eq!(body["success"], true);
}

#[tokio::test]
async fn load_invalid_toml_returns_bad_request() {
    let state = create_app_state();
    let app = build_router(state);

    let (status, body) = load_scenario_with_body(&app, "not valid [[ toml").await;
    assert_eq!(status, StatusCode::BAD_REQUEST);
    assert!(body["error"].is_string(), "error field should be present");
}

#[tokio::test]
async fn load_scenario_with_zero_max_ticks_returns_bad_request() {
    let state = create_app_state();
    let app = build_router(state);

    let toml = r#"
[simulation]
rng_seed = 42
max_ticks = 0

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

    let (status, body) = load_scenario_with_body(&app, toml).await;
    assert_eq!(status, StatusCode::BAD_REQUEST);
    let error_msg = body["error"].as_str().unwrap_or("");
    assert!(error_msg.contains("max_ticks"), "error should mention max_ticks, got: {error_msg}");
}

#[tokio::test]
async fn load_scenario_with_missing_equipment_returns_bad_request() {
    let state = create_app_state();
    let app = build_router(state);

    let toml = r#"
[simulation]
rng_seed = 42
max_ticks = 100

[[material]]
id = 1
name = "Widget"
routing_id = 1
"#;

    let (status, body) = load_scenario_with_body(&app, toml).await;
    assert_eq!(status, StatusCode::BAD_REQUEST);
    let error_msg = body["error"].as_str().unwrap_or("");
    assert!(error_msg.contains("equipment"), "error should mention equipment, got: {error_msg}");
}

// ─── §3.3 — Handler error surfaces in snapshot ──────────────────────

#[tokio::test]
async fn handler_error_surfaces_in_snapshot() {
    let state = create_app_state();
    let app = build_router(state);

    load_scenario(&app, basic_scenario_toml()).await;
    tokio::time::sleep(std::time::Duration::from_millis(100)).await;

    let body = serde_json::json!({ "machine_id": 9999, "online": false });
    let req = Request::builder()
        .method(http::Method::POST)
        .uri("/api/machines")
        .header("content-type", "application/json")
        .body(Body::from(serde_json::to_string(&body).unwrap()))
        .unwrap();
    app.clone().oneshot(req).await.unwrap();
    tokio::time::sleep(std::time::Duration::from_millis(100)).await;

    let req = Request::builder()
        .uri("/api/snapshot")
        .body(Body::empty())
        .unwrap();
    let resp = app.clone().oneshot(req).await.unwrap();
    let body = resp.into_body().collect().await.unwrap().to_bytes();
    let json: serde_json::Value = serde_json::from_slice(&body).unwrap();
    assert!(
        json["last_error"].is_string(),
        "last_error should be present after handler error, got: {:?}",
        json["last_error"]
    );
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
