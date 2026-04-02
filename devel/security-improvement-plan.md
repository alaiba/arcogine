# Security Improvement Plan

> **Date:** 2026-04-02
> **Scope:** Identify security weaknesses in the Arcogine codebase and provide a prioritized, actionable improvement plan covering the Rust API, frontend, CI/CD, container deployment, and supply-chain hygiene.
> **Primary sources:** All crates under `crates/`, `ui/`, `.github/workflows/ci.yml`, `compose.yaml`, `Dockerfile`, `ui/Dockerfile`, `SECURITY.md`, `docs/architecture-overview.md`, `docs/standards-alignment.md`
> **Relationship to other plans:** This plan complements `devel/testability-improvement-plan.md` (testing focus). Security-relevant testing items are referenced but not duplicated.

---

## 1. Current Security Posture

### 1.1 Documented Policy

`SECURITY.md` explicitly scopes the MVP as a **local-only, single-user experimentation tool** and acknowledges three known limitations:

1. The REST API does not require authentication.
2. CORS is configured permissively for development.
3. Scenario files and simulation state are not encrypted.

This plan accepts the local-only scope for Phase 1 items and introduces a hardening path for items needed before any network-exposed or multi-user deployment.

### 1.2 Attack Surface Summary

| Surface | Exposure | Current Controls |
|---------|----------|-----------------|
| REST API (`sim-api`) | `0.0.0.0:3000` — all interfaces | No auth, no rate limiting; Axum 0.8 applies a 2 MB default body limit |
| SSE stream (`/api/events/stream`) | Same as REST | No auth, no connection limits |
| Nginx reverse proxy (`ui/Dockerfile`) | Port 5173 in Docker | No security headers, no TLS |
| Vite dev proxy (`vite.config.ts`) | Port 5173 local | Dev-only, proxies `/api` |
| TOML scenario input (`POST /api/scenario`) | JSON body with embedded TOML string | Parsed by `toml` crate; validated by `validate_scenario`; Axum 2 MB default body limit |
| CLI file read (`arcogine run --scenario`) | Local filesystem | `std::fs::read_to_string` — no path validation |
| Docker images | `rust:1.94-slim` / `debian:bookworm-slim` / `node:20-slim` / `nginx:alpine` | No image scanning in CI |

### 1.3 Existing Security Controls

| Control | Location | Status |
|---------|----------|--------|
| CORS middleware | `crates/sim-api/src/server.rs:16-25` | Present; permissive (`Any` origin/headers) |
| HTTP tracing | `crates/sim-api/src/server.rs:44` | `TraceLayer::new_for_http()` |
| Input validation (scenario) | `crates/sim-core/src/scenario.rs:28-142` | IDs, references, ranges validated |
| Price validation | `crates/sim-api/src/routes.rs:168-170` | Rejects negative prices |
| State guards | `crates/sim-api/src/routes.rs:84-91,119-127,144-147` | Commands rejected when no scenario loaded / sim completed |
| Error responses | `crates/sim-api/src/routes.rs:53-78` | Generic fixed strings; no stack traces in JSON |
| XSS prevention | `ui/src/` | React default escaping; no `dangerouslySetInnerHTML` |
| CSV injection mitigation | `ui/src/components/experiment/ExportMenu.tsx:18-23` | Escapes `"`, `,`, `\n`, `\r` in CSV cells |
| `.env` gitignored | `.gitignore:18-19` | `.env` and `.env.local` excluded from VCS |

---

## 2. Risk Assessment

### 2.1 Risk Categories

Each item is rated by **likelihood** (if the API is reachable from an untrusted network) and **impact** (consequence of exploitation).

| # | Risk | Likelihood | Impact | Current Mitigation |
|---|------|-----------|--------|-------------------|
| R1 | Unauthenticated API access | High | High | None — any reachable client can drive the sim |
| R2 | Unrestricted CORS | High | Medium | `CorsLayer::allow_origin(Any)` |
| R3 | Default body-size limit too generous | Low | Low | Axum 0.8 applies a 2 MB default; scenarios are ~1 KB |
| R4 | Unbounded event log growth | Medium | Medium | `EventLog` Vec grows without limit in memory |
| R5 | Unbounded SSE connections | Medium | Medium | No connection cap on broadcast channel |
| R6 | Scenario load reports false success | High | Low | `POST /api/scenario` always returns `success: true` |
| R7 | Silenced handler errors | Medium | Medium | `let _ = h.handle_event(...)` in `state.rs` |
| R8 | Default bind to all interfaces | Medium | Medium | `--addr 0.0.0.0:3000` default |
| R9 | No security headers (nginx) | Low | Low | No CSP, X-Frame-Options, etc. |
| R10 | `Cargo.lock` not committed | Medium | Low | `.gitignore` includes `Cargo.lock` |
| R11 | No dependency vulnerability scanning | Medium | Medium | No `cargo audit`, `npm audit`, or Dependabot |
| R12 | Docker images not scanned | Low | Medium | No Trivy/Snyk in CI |
| R13 | No TLS termination | Low | Medium | HTTP only in API and nginx |
| R14 | `f64` price/economy values not bounded above | Low | Low | No upper-bound check on price or economy params |
| R15 | Event log export has no pagination | Low | Low | `/api/export/events` returns entire log as JSON |

---

## 3. Improvement Plan

### 3.1 Tighten Request Body-Size Limit

**Priority:** Medium
**Risk addressed:** R3
**Effort:** Low

Axum 0.8 already applies a **2 MB default body limit** via `axum_core::extract::DefaultBodyLimit` on all extractors (`Json`, `String`, `Bytes`, `Form`). Scenarios are TOML text (the largest example `capacity_expansion_scenario.toml` is ~1 KB). Lowering the limit to 1 MB provides defense-in-depth with generous headroom for complex scenarios.

File: `crates/sim-api/src/server.rs`

Add a `DefaultBodyLimit` layer to the router (no `Cargo.toml` change needed — `DefaultBodyLimit` is re-exported from `axum`):

```rust
use axum::extract::DefaultBodyLimit;

// Inside build_router():
Router::new()
    // ... routes ...
    .with_state(state)
    .layer(TraceLayer::new_for_http())
    .layer(cors)
    .layer(DefaultBodyLimit::max(1024 * 1024)) // 1 MB (down from 2 MB default)
```

**Verification:** Add a test in `crates/sim-api/tests/api_smoke.rs` that sends a `POST /api/scenario` body larger than 1 MB and asserts a `413 Payload Too Large` response. Also add a test confirming that a body just under 1 MB is accepted normally.

### 3.2 Fix Scenario Load Error Propagation

**Priority:** High
**Risk addressed:** R6, R7
**Effort:** Medium

The `POST /api/scenario` endpoint enqueues the TOML string and unconditionally returns `{ "success": true }`. If parsing or validation fails, the sim thread silently logs a generic error and the client never learns the scenario was rejected.

File: `crates/sim-api/src/state.rs` — `SimCommand::LoadScenario` handler (lines 480-523)

**Approach:** Add a synchronous reply channel to `SimCommand::LoadScenario` so the sim thread can report success or failure back to the API handler.

1. Change `SimCommand::LoadScenario` to carry a `std::sync::mpsc::SyncSender<Result<(), String>>`. Use `SyncSender` (not `tokio::sync::oneshot`) because the sim thread is a plain OS thread, not a Tokio task. `SyncSender` implements both `Clone` and `Debug`, preserving `SimCommand`'s existing derives. The API handler creates a `std::sync::mpsc::sync_channel(1)` before sending the command:

```rust
pub enum SimCommand {
    LoadScenario {
        toml: String,
        reply: std::sync::mpsc::SyncSender<Result<(), String>>,
    },
    // ... other variants unchanged
}
```

2. In the sim thread, send the parse/validate result back through `reply`:

```rust
SimCommand::LoadScenario { toml, reply } => {
    match sim_core::scenario::load_scenario(&toml) {
        Ok(cfg) => {
            // ... existing setup ...
            let _ = reply.send(Ok(()));
        }
        Err(e) => {
            tracing::error!("Failed to load scenario: {e}");
            let _ = reply.send(Err(e.to_string()));
        }
    }
}
```

3. In `routes::load_scenario`, wait on the reply channel with a timeout:

```rust
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
```

This eliminates the misleading `success: true`, removes the arbitrary `sleep(50ms)`, and surfaces validation errors to the client.

**Verification:** Update the `load_scenario` test helper in `api_smoke.rs` to return `(StatusCode, serde_json::Value)` so tests can inspect the response body. Add these named tests:

- `load_valid_scenario_returns_success` — existing `basic_scenario_toml()` → 200, body `success == true`
- `load_invalid_toml_returns_bad_request` — send `{ "toml": "not valid [[ toml" }` → 400, body `error` field contains "TOML parse error" or similar
- `load_scenario_with_zero_max_ticks_returns_bad_request` — send valid TOML with `max_ticks = 0` → 400, body `error` field contains "max_ticks"
- `load_scenario_with_missing_equipment_returns_bad_request` — send TOML with `[simulation]` but no `[[equipment]]` → 400, body `error` contains "equipment"

### 3.3 Propagate Handler and Scheduler Errors in Simulation Thread

**Priority:** High
**Risk addressed:** R7
**Effort:** Low

File: `crates/sim-api/src/state.rs`

Multiple locations suppress errors with `let _ = ...`:

**Handler errors:**
- Line 533: `let _ = h.handle_event(&event, &mut scheduler);` (Step command)
- Line 582: `let _ = h.handle_event(&event, &mut scheduler);` (Run command)
- Line 664: `let _ = h.handle_event(&event, &mut scheduler);` (ChangePrice)
- Line 694: `let _ = h.handle_event(&event, &mut scheduler);` (ChangeMachineCount)

**Scheduler errors:**
- Lines 492, 500: `let _ = scheduler.schedule(...)` (LoadScenario — initial periodic events)
- Lines 634, 642: `let _ = scheduler.schedule(...)` (Reset — initial periodic events)
- Lines 759, 765: `let _ = scheduler.schedule(...)` (reschedule_periodic)

This is inconsistent with `crates/sim-core/src/runner.rs:37-53` which correctly uses `scheduler.schedule(...)?` to propagate errors.

**Approach:**

1. Add a `last_error: Option<String>` field to `SimSnapshot`:

```rust
pub struct SimSnapshot {
    // ... existing fields ...
    pub last_error: Option<String>,
}
```

2. Replace all `let _ = h.handle_event(...)` and `let _ = scheduler.schedule(...)` with error capture:

```rust
if let Err(e) = h.handle_event(&event, &mut scheduler) {
    tracing::warn!(error = %e, event_time = event.time.ticks(), "event handler error");
    last_error = Some(e.to_string());
}
```

3. Include `last_error` in the snapshot built by `build_snapshot`.

**Verification:** Add a test in `api_smoke.rs`:
- `handler_error_surfaces_in_snapshot` — Load a scenario, then send `POST /api/machines` with an invalid `machine_id` (one that doesn't exist in the loaded scenario). Assert that the response snapshot's `last_error` field is `Some(...)` containing an error message about the unknown machine ID.

### 3.4 Restrict Default Bind Address

**Priority:** Medium
**Risk addressed:** R8
**Effort:** Low

File: `crates/sim-cli/src/main.rs:10`

Change the CLI default from `0.0.0.0:3000` to `127.0.0.1:3000`:

```rust
#[arg(long, default_value = "127.0.0.1:3000")]
addr: String,
```

File: `Dockerfile:20`

The Docker CMD must continue to bind to `0.0.0.0` for container networking:

```dockerfile
CMD ["serve", "--addr", "0.0.0.0:3000"]
```

This keeps Docker working while protecting native/dev-container users from unintentional LAN exposure. Users who need LAN access pass `--addr 0.0.0.0:3000` explicitly.

**Verification:** Add a CLI integration test (or document a manual check) that parses `Cli::Serve` with no `--addr` argument and asserts the default is `"127.0.0.1:3000"`. This can use `clap`'s `try_parse_from` in a `#[test]` in `sim-cli`.

### 3.5 Add Nginx Security Headers

**Priority:** Medium
**Risk addressed:** R9
**Effort:** Low

File: `ui/Dockerfile` — inline nginx config (lines 18-36)

Add security headers inside the `server` block:

```nginx
server {
    listen 5173;
    root /usr/share/nginx/html;
    index index.html;

    # Security headers
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;
    add_header Permissions-Policy "camera=(), microphone=(), geolocation=()" always;
    add_header Content-Security-Policy "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; connect-src 'self'; img-src 'self' data:; font-src 'self';" always;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://api:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_buffering off;
        proxy_cache off;
    }
}
```

**CSP note:** `'unsafe-inline'` for `style-src` is needed because Tailwind CSS injects inline styles. If the project migrates to extracted CSS, this can be tightened. The `connect-src 'self'` directive covers both REST and SSE connections through the nginx proxy.

**Scope:** This CSP applies only to the Docker nginx container (`ui/Dockerfile`). The Vite dev server (`vite.config.ts`) does not serve these headers. CSP for Vite dev mode is not required for a local-only tool and is out of scope for this item.

**Verification:** Build the Docker UI image and verify headers with `curl -I http://localhost:5173/`. Assert that `Content-Security-Policy`, `X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy`, and `Permissions-Policy` headers are present.

### 3.6 Commit `Cargo.lock` to Version Control

**Priority:** High
**Risk addressed:** R10
**Effort:** Low

Arcogine is an application (binary crates `sim-cli` and `sim-api`), not a library. The Rust community convention is to commit `Cargo.lock` for applications to ensure reproducible builds.

File: `.gitignore:4`

Remove the `Cargo.lock` line:

```gitignore
# Rust
/target/
**/*.rs.bk
```

Then run `cargo generate-lockfile` (or `cargo build`) and commit the resulting `Cargo.lock`.

**Impact on CI:** The CI job already references `hashFiles('**/Cargo.lock')` for cache keys (`ci.yml:31`). With the lockfile committed, CI caching becomes deterministic. Without it, the cache key is always empty on a fresh clone, which defeats the purpose of caching.

**Impact on Docker:** `Dockerfile:4` already copies `Cargo.lock` into the build context. This currently fails on a fresh clone without a local build; committing the lockfile fixes this.

**Impact on dev workflow:** After committing `Cargo.lock`, dependency updates require `cargo update` followed by committing the updated lockfile. Dev container users (`.devcontainer/post-create.sh` runs `cargo build`) should be aware that local `cargo update` changes are not reflected in CI until committed.

**Verification:** After removing `Cargo.lock` from `.gitignore` and committing it, verify: (a) `docker build .` succeeds on a fresh clone, (b) CI cache key `hashFiles('**/Cargo.lock')` produces a non-empty hash.

### 3.7 Add Dependency Vulnerability Scanning to CI

**Priority:** High
**Risk addressed:** R11
**Effort:** Low

File: `.github/workflows/ci.yml`

#### 3.7.1 Rust: `cargo audit`

Add after the test step in the `rust` job. Use the `rustsec/audit-check` GitHub Action (pre-built binary, avoids slow `cargo install` compilation):

```yaml
- name: Audit Rust dependencies
  uses: rustsec/audit-check@v2
  with:
    token: ${{ secrets.GITHUB_TOKEN }}
```

#### 3.7.2 Frontend: `npm audit`

Add after the build step in the `frontend` job:

```yaml
- name: Audit npm dependencies
  run: npm audit --audit-level=high
```

`--audit-level=high` avoids failing on low/moderate advisories that may not have fixes yet. Adjust as the project matures.

#### 3.7.3 GitHub Dependabot

Create `.github/dependabot.yml`:

```yaml
version: 2
updates:
  - package-ecosystem: "cargo"
    directory: "/"
    schedule:
      interval: "weekly"
    open-pull-requests-limit: 10

  - package-ecosystem: "npm"
    directory: "/ui"
    schedule:
      interval: "weekly"
    open-pull-requests-limit: 10

  - package-ecosystem: "github-actions"
    directory: "/"
    schedule:
      interval: "weekly"
    open-pull-requests-limit: 5
```

### 3.8 Add Docker Image Scanning to CI

**Priority:** Medium
**Risk addressed:** R12
**Effort:** Low

Add a new job to `.github/workflows/ci.yml`:

```yaml
docker-scan:
  name: Docker image scan
  runs-on: ubuntu-latest
  steps:
    - uses: actions/checkout@v4

    - name: Build API image
      run: docker build -t arcogine-api:ci .

    - name: Scan API image
      uses: aquasecurity/trivy-action@master
      with:
        image-ref: 'arcogine-api:ci'
        severity: 'CRITICAL,HIGH'
        exit-code: '1'
```

This only builds and scans the API image. The UI image scan can be added similarly if container publishing is planned.

**Verification:** The CI job itself is the verification — it fails with exit code 1 if critical or high vulnerabilities are found.

### 3.9 Bound SSE Connections

**Priority:** Medium
**Risk addressed:** R5
**Effort:** Low

File: `crates/sim-api/src/state.rs:461`

The broadcast channel is already bounded to 4096 messages. However, there is no limit on concurrent SSE subscribers.

File: `crates/sim-api/src/server.rs`

Add a connection-counting semaphore to the SSE route. `tokio::sync::Semaphore::try_acquire_owned()` requires `Arc<Semaphore>`, so the field must be wrapped in `Arc`:

Add an `sse_semaphore` field to `AppState`:

```rust
pub struct AppState {
    pub cmd_tx: mpsc::Sender<SimCommand>,
    pub snapshot_rx: watch::Receiver<SimSnapshot>,
    pub event_tx: broadcast::Sender<Event>,
    pub event_log_rx: watch::Receiver<EventLog>,
    pub sse_semaphore: Arc<tokio::sync::Semaphore>,
}
```

Initialize with a reasonable limit (e.g., 64 concurrent connections):

```rust
Arc::new(AppState {
    // ... existing fields ...
    sse_semaphore: Arc::new(tokio::sync::Semaphore::new(64)),
})
```

In `sse.rs`, acquire an owned permit before streaming. The permit is moved into the stream's `filter_map` closure so it is held for the stream's lifetime and released on disconnect:

```rust
pub async fn event_stream(
    State(state): State<Arc<AppState>>,
) -> Result<Sse<impl Stream<Item = Result<SseEvent, Infallible>>>, StatusCode> {
    let permit = state.sse_semaphore.clone().try_acquire_owned()
        .map_err(|_| StatusCode::SERVICE_UNAVAILABLE)?;

    let rx = state.event_tx.subscribe();
    let stream = BroadcastStream::new(rx)
        .filter_map(move |result| {
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
```

**Verification:** Add a test that opens 64 SSE connections (using `tower::ServiceExt::oneshot` with `GET /api/events/stream`), then verifies that a 65th connection receives `503 Service Unavailable`.

### 3.10 Cap Event Log Size

**Priority:** Medium
**Risk addressed:** R4
**Effort:** Low

File: `crates/sim-core/src/log.rs`

The `EventLog` grows without bound. For long-running simulations this can exhaust memory.

Add a configurable maximum capacity. **Important:** `EventLog` derives `PartialEq` (`crates/sim-core/src/log.rs:10`), and determinism tests compare event logs with `assert_eq!` (`crates/sim-core/tests/determinism.rs:57`). The `max_capacity` field must be excluded from equality comparison to avoid breaking these tests.

Replace the auto-derived `PartialEq` with a manual implementation that compares only the `events` field:

```rust
// Remove PartialEq and Default from the derive list
#[derive(Debug, Clone, Serialize)]
pub struct EventLog {
    events: Vec<Event>,
    #[serde(skip)]
    max_capacity: usize,
}

impl Default for EventLog {
    fn default() -> Self {
        Self::new()
    }
}

// Equality compares only the event data, not the capacity configuration
impl PartialEq for EventLog {
    fn eq(&self, other: &Self) -> bool {
        self.events == other.events
    }
}

impl EventLog {
    pub fn new() -> Self {
        EventLog {
            events: Vec::new(),
            max_capacity: 1_000_000,
        }
    }

    pub fn with_capacity(max_capacity: usize) -> Self {
        EventLog {
            events: Vec::new(),
            max_capacity,
        }
    }

    pub fn append(&mut self, event: Event) {
        if self.events.len() < self.max_capacity {
            self.events.push(event);
        }
    }

    pub fn is_truncated(&self) -> bool {
        self.events.len() >= self.max_capacity
    }
}
```

**Trade-off:** Dropping events silently is acceptable for an MVP simulation tool where the event stream (SSE) provides real-time delivery. The `is_truncated()` method allows the export endpoint (`/api/export/events`) to include a `truncated` flag in the response.

**Verification:** Add unit tests in `crates/sim-core/src/log.rs`:
- `event_log_caps_at_max_capacity` — create log with `with_capacity(5)`, append 10 events, assert count is 5
- `event_log_equality_ignores_capacity` — create two logs with different capacities, append same events, assert they are equal
- `event_log_is_truncated` — verify `is_truncated()` returns `true` when cap is reached

### 3.11 Add Input Validation for Economy and Price Parameters

**Priority:** Low
**Risk addressed:** R14
**Effort:** Low

**Two contexts require different treatment:**

1. **JSON API (`routes.rs`):** `serde_json` rejects NaN and Infinity during deserialization (they are not valid JSON numbers), so explicit NaN/Infinity checks in the handler are defensive-only. Add upper-bound validation:

File: `crates/sim-api/src/routes.rs` — `change_price` handler (line 164-187)

```rust
const MAX_PRICE: f64 = 1_000_000.0;

if body.price < 0.0 || body.price > MAX_PRICE {
    return Err(bad_request("Price must be between 0 and 1,000,000"));
}
```

2. **TOML scenario (`scenario.rs`):** The `toml` crate **does** parse `nan` and `inf` as valid TOML floats, so NaN/Infinity checks are necessary here:

File: `crates/sim-core/src/scenario.rs` — `validate_scenario` (lines 131-139)

Extend economy validation with finiteness and range checks:

```rust
if let Some(econ) = &config.economy {
    if !econ.initial_price.is_finite() || econ.initial_price <= 0.0 || econ.initial_price > 1_000_000.0 {
        return Err(SimError::OutOfRange {
            field: "economy.initial_price".to_string(),
            message: "must be a finite number > 0 and <= 1,000,000".to_string(),
        });
    }
    if !econ.base_demand.is_finite() || econ.base_demand < 0.0 {
        return Err(SimError::OutOfRange {
            field: "economy.base_demand".to_string(),
            message: "must be a finite number >= 0".to_string(),
        });
    }
    if !econ.price_elasticity.is_finite() || econ.price_elasticity < 0.0 {
        return Err(SimError::OutOfRange {
            field: "economy.price_elasticity".to_string(),
            message: "must be a finite number >= 0".to_string(),
        });
    }
}
```

**Verification:** Add tests in `crates/sim-core/tests/scenario_loading.rs`:
- `scenario_with_nan_price_rejected` — TOML with `initial_price = nan` → error
- `scenario_with_inf_demand_rejected` — TOML with `base_demand = inf` → error
- `scenario_with_extreme_price_rejected` — TOML with `initial_price = 999999999.0` → error
Add a test in `api_smoke.rs`:
- `extreme_price_returns_bad_request` — `POST /api/price` with `price: 2000000.0` → 400

### 3.12 Add Configurable CORS for Non-Development Deployments

**Priority:** Low (no immediate risk for local-only MVP)
**Risk addressed:** R2
**Effort:** Low

File: `crates/sim-api/src/server.rs`

Replace hardcoded `Any` CORS with an environment-driven allowed origin:

```rust
use std::env;

let cors = match env::var("CORS_ALLOWED_ORIGIN") {
    Ok(origin) => CorsLayer::new()
        .allow_origin(origin.parse::<HeaderValue>().expect("invalid CORS origin"))
        .allow_methods([Method::GET, Method::POST, Method::PUT, Method::DELETE, Method::OPTIONS])
        .allow_headers(Any),
    Err(_) => CorsLayer::new()
        .allow_origin(Any)
        .allow_methods([Method::GET, Method::POST, Method::PUT, Method::DELETE, Method::OPTIONS])
        .allow_headers(Any),
};
```

Update `.env.example`:

```
# CORS allowed origin (optional; defaults to permissive if unset)
# CORS_ALLOWED_ORIGIN=http://localhost:5173
```

This maintains the permissive default for development while providing a path to restrict CORS in deployments.

**Verification:** Add a test in `api_smoke.rs`:
- `cors_with_env_var_restricts_origin` — set `CORS_ALLOWED_ORIGIN=http://example.com` before building the router, send a request with `Origin: http://evil.com`, verify the response lacks `Access-Control-Allow-Origin` or returns a CORS error. (Note: this test needs to set the env var before `build_router` is called; use a dedicated test function with cleanup.)

### 3.13 Update `SECURITY.md` with Hardening Guidance

**Priority:** Low
**Effort:** Low

Extend `SECURITY.md` with a "Hardening for Network Deployment" section that documents:

1. Change `--addr` to `127.0.0.1:3000` for non-Docker use
2. Set `CORS_ALLOWED_ORIGIN` to the UI's origin
3. Place behind a reverse proxy with TLS termination
4. Run `cargo audit` and `npm audit` before deployment
5. Set `RUST_LOG=warn` in production to reduce log verbosity

### 3.14 CI Security Gates (No Deployment)

**Priority:** High
**Risk addressed:** R11, R12
**Effort:** Medium

This item is explicitly CI-only and does not implement any deployment or image publishing step.

File: `.github/workflows/ci.yml`

#### 3.14.1 Secret scanning

Add a dedicated CI job that scans repository content for secrets:

```yaml
  security-secrets:
    name: Secret scan
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run Gitleaks
        uses: gitleaks/gitleaks-action@v2
        with:
          args: detect --no-git -v --redact --source .
```

If the repo produces noisy findings, start with a checked-in `.gitleaks.toml` allowlist and keep the step enabled in alert mode before turning on hard failure.

#### 3.14.2 Dependency audit evidence

Keep dependency checks in CI and persist machine-readable outputs:

```yaml
  - name: Audit Rust dependencies
    uses: rustsec/audit-check@v2
    with:
      token: ${{ secrets.GITHUB_TOKEN }}

  - name: Audit npm dependencies
    run: npm audit --audit-level=high --json > npm-audit.json
```

Upload reports when failures occur so review can be done from workflow artifacts:

```yaml
  - name: Upload audit reports
    if: failure()
    uses: actions/upload-artifact@v4
    with:
      name: security-audit-reports
      path: |
        npm-audit.json
```

#### 3.14.3 Scan both API and UI images

Expand image scanning to `arcogine-api:ci` and `arcogine-ui:ci` in a matrix strategy:

```yaml
  - name: Scan image with Trivy
    uses: aquasecurity/trivy-action@master
    with:
      image-ref: arcogine-${{ matrix.image }}:ci
      severity: 'CRITICAL,HIGH'
      format: 'table'
      exit-code: '1'
```

Use `matrix.image` values of `api` and `ui` so each image is gated independently. Keep the matrix `fail-fast: false` so both scans run even if one fails.

**Verification:** CI fails on any CRITICAL/HIGH issue from Rust/npm scans or either image scan, and artifacts include `npm-audit.json`.

---

## 4. Priority Matrix

| # | Item | Priority | Effort | Risk(s) | Depends on |
|---|------|----------|--------|---------|------------|
| 3.1 | Tighten body-size limit | Medium | Low | R3 | — |
| 3.2 | Scenario load error propagation | High | Medium | R6, R7 | — |
| 3.3 | Handler and scheduler error propagation | High | Low | R7 | — |
| 3.4 | Restrict default bind address | Medium | Low | R8 | — |
| 3.5 | Nginx security headers | Medium | Low | R9 | — |
| 3.6 | Commit `Cargo.lock` | High | Low | R10 | — |
| 3.7 | Dependency vulnerability scanning | High | Low | R11 | 3.6 (for cargo audit cache) |
| 3.8 | Docker image scanning | Medium | Low | R12 | — |
| 3.9 | Bound SSE connections | Medium | Low | R5 | — |
| 3.10 | Cap event log size | Medium | Low | R4 | — |
| 3.11 | Economy/price input validation | Low | Low | R14 | — |
| 3.12 | Configurable CORS | Low | Low | R2 | — |
| 3.13 | Update SECURITY.md | Low | Low | — | 3.4, 3.12 |
| 3.14 | CI security gates (no deployment) | High | Medium | R11, R12 | — |

---

## 5. Recommended Execution Order

1. **3.6** — Commit `Cargo.lock` (unblocks CI caching and Docker builds)
2. **3.3** — Handler and scheduler error propagation (quick win, improves observability)
3. **3.2** — Scenario load error propagation (medium effort, high value)
4. **3.7** — Dependency scanning in CI (automated protection)
5. **3.14** — CI security gates (no deployment)
6. **3.4** — Restrict default bind address
7. **3.5** — Nginx security headers
8. **3.1** — Tighten body-size limit (Axum already provides 2 MB default)
9. **3.9** — Bound SSE connections
10. **3.10** — Cap event log size
11. **3.8** — Docker image scanning
12. **3.11** — Economy/price input validation
13. **3.12** — Configurable CORS
14. **3.13** — Update SECURITY.md (after other items are implemented)

---

## 6. Out of Scope (Future Work)

The following are documented for completeness but deferred beyond the MVP hardening scope:

- **Authentication / authorization** — required for multi-user or network-exposed deployments; see `SECURITY.md` known limitations
- **TLS termination** — handle via reverse proxy (nginx, Caddy, cloud LB) rather than in-application
- **Rate limiting** — consider `tower_governor` or similar when auth is added
- **Encryption at rest** — scenario state is ephemeral in-memory; not applicable for MVP
- **GDPR compliance** — only relevant when processing personal data (`docs/standards-alignment.md` Tier 3)
- **Signed container images** — relevant for production CI/CD pipelines
- **API versioning** — relevant when external consumers depend on API stability

---

## Findings

### F1: §3.1 mischaracterizes Axum's existing body-size default [Applied]
<!-- severity: major -->
<!-- dimension: correctness -->

**Context:** §3.1 states "Add `tower_http::limit::RequestBodyLimitLayer` to the Axum router" and risk table R3 says "no body-size limits" / "Axum defaults only (~2MB for JSON)". The attack surface table (§1.2) also claims "no body-size limits" for the TOML scenario input.

**Issue:** Axum 0.8 already applies a **2 MB default body limit** via `axum_core::extract::DefaultBodyLimit` on all extractors (`Json`, `String`, `Bytes`, `Form`). The plan correctly parenthetically mentions "~2MB" in R3 but contradicts this in §1.2 and §3.1 by framing the situation as if no limit exists. Adding `RequestBodyLimitLayer` on top of the existing `DefaultBodyLimit` creates two independent limits — the lower of the two applies. The plan should use Axum's built-in `DefaultBodyLimit::max()` instead of adding a tower-http layer, or explicitly explain the layering. Additionally, lowering to 1 MB while Axum's default is 2 MB means the tower-http layer is redundant unless it's set below 2 MB.

**Recommendation:** Rewrite §3.1 to use `axum::extract::DefaultBodyLimit::max(1024 * 1024)` as a router layer (no Cargo.toml change needed). Update §1.2 and R3 to acknowledge the existing 2 MB default and explain why lowering it to 1 MB is desirable.

**Choices:**
- [x] Use `DefaultBodyLimit::max()` from Axum; remove `tower-http` `"limit"` feature addition; fix §1.2 and R3 wording
- [ ] Keep `RequestBodyLimitLayer` but document that it stacks with the Axum default
- [ ] Remove §3.1 entirely (the 2 MB default is sufficient for this MVP)

### F2: §3.2 `SyncSender` prose contradicts code sample [Applied]
<!-- severity: critical -->
<!-- dimension: correctness -->

**Context:** §3.2 proposes adding `reply: std::sync::mpsc::SyncSender<Result<(), String>>` to `SimCommand::LoadScenario`. `SimCommand` derives `Clone` (`crates/sim-api/src/state.rs:29`).

**Issue:** `std::sync::mpsc::SyncSender` implements `Clone`, so that specific trait is not broken. However, the code snippet in step 1 says to use `tokio::sync::oneshot::Sender` in the prose ("Change `SimCommand::LoadScenario` to carry a `tokio::sync::oneshot::Sender`") but then shows `std::sync::mpsc::SyncSender` in the code block. `tokio::sync::oneshot::Sender` does **not** implement `Clone` and would break the derive. The plan text contradicts the code sample.

**Recommendation:** Remove the `tokio::sync::oneshot` mention from the prose. The code sample using `std::sync::mpsc::SyncSender` is the correct approach since the sim thread is a plain OS thread. Verify that `SyncSender<Result<(), String>>` satisfies `Debug` (it does — `SyncSender` implements `Debug`).

**Choices:**
- [x] Fix prose to consistently say `std::sync::mpsc::SyncSender`; remove `tokio::sync::oneshot` reference
- [ ] Use `tokio::sync::oneshot` and remove `Clone` derive from `SimCommand`

### F3: §3.9 SSE semaphore code uses wrong ownership pattern [Applied]
<!-- severity: major -->
<!-- dimension: correctness -->

**Context:** §3.9 proposes `sse_semaphore: tokio::sync::Semaphore` as a field on `AppState` and then calls `state.sse_semaphore.clone().try_acquire_owned()`.

**Issue:** `tokio::sync::Semaphore` does not implement `Clone`. The `try_acquire_owned()` method requires `Arc<Semaphore>`. Since `AppState` is wrapped in `Arc<AppState>`, the semaphore field needs to be `Arc<Semaphore>`, or the code must use `try_acquire()` with a lifetime-bounded permit instead.

**Recommendation:** Change the field type to `Arc<tokio::sync::Semaphore>` and update the initialization to `sse_semaphore: Arc::new(tokio::sync::Semaphore::new(64))`. Update the `event_stream` code to `state.sse_semaphore.clone().try_acquire_owned()`.

**Choices:**
- [x] Change field to `Arc<Semaphore>`, update initialization and acquisition code
- [ ] Use `try_acquire()` with lifetime-bounded permit (more complex stream typing)

### F4: §3.10 event log cap breaks `PartialEq` for determinism tests [Applied]
<!-- severity: major -->
<!-- dimension: testing -->

**Context:** §3.10 adds a `max_capacity: usize` field to `EventLog`. `EventLog` derives `PartialEq` (`crates/sim-core/src/log.rs:10`). Determinism tests use `assert_eq!(result1.event_log, result2.event_log)` (`crates/sim-core/tests/determinism.rs:57`).

**Issue:** If `max_capacity` participates in `PartialEq`, two logs with identical events but different capacity values would compare as unequal, breaking determinism tests. If `max_capacity` is excluded (manual `PartialEq` impl), it adds maintenance burden and diverges from the derive pattern used across all Arcogine types (`CONTRIBUTING.md:56`).

**Recommendation:** Exclude `max_capacity` from equality by implementing `PartialEq` manually (compare only `events`), or annotate the field with `#[serde(skip)]` and derive `PartialEq` only on the `events` vec. Document the design choice.

**Choices:**
- [x] Implement `PartialEq` manually, comparing only the `events` field; add a doc-comment explaining why
- [ ] Use `#[derive(PartialEq)]` and accept that different capacities make logs unequal (update determinism test to use same capacity)

### F5: §3.2 and §3.3 lack test specifications for the new behavior [Applied]
<!-- severity: major -->
<!-- dimension: testing -->

**Context:** §3.2 says "Add tests in `api_smoke.rs`" with three bullet points (valid TOML → 200, invalid TOML → 400, validation failure → 400). §3.3 has no test specification at all.

**Issue:** The test bullets in §3.2 are acceptance-level descriptions, not implementable test specifications. They omit: (a) how to construct the invalid TOML body in the test, (b) what the error response body should contain, (c) whether the existing `load_scenario` helper function in `api_smoke.rs` needs updating (it currently only returns `StatusCode`, not the body). §3.3 proposes changing silent error suppression to logging but provides no way to verify the fix — there is no test that checks log output or an error field in the snapshot.

**Recommendation:** For §3.2, expand test specs: update `load_scenario` helper to return `(StatusCode, serde_json::Value)`, add three named test functions with specific assertions on the error `message` field. For §3.3, add a `last_error: Option<String>` field to `SimSnapshot` (currently described as "optional future enhancement") and promote it to the primary verification mechanism, with a test that triggers a handler error (e.g., `ChangeMachineCount` with an invalid machine ID) and asserts the snapshot contains the error.

**Choices:**
- [x] Expand test specs for §3.2; promote `last_error` field in §3.3 from "optional" to "required" with test spec
- [ ] Keep §3.3 as log-only and verify via integration test with a tracing subscriber capture

### F6: No security-focused tests exist or are planned [Applied]
<!-- severity: major -->
<!-- dimension: testing -->

**Context:** The plan proposes 13 improvements but only §3.1 and §3.2 mention verification tests. The remaining 11 items have no test specifications.

**Issue:** Without test coverage for security controls, regressions can silently reintroduce vulnerabilities. Items particularly needing tests: §3.4 (bind address — verify CLI parses `127.0.0.1` default), §3.9 (SSE connection limit — verify 503 when limit exceeded), §3.11 (NaN/Infinity/upper-bound rejection), §3.12 (CORS with env var set).

**Recommendation:** Add a "Verification" subsection to each actionable plan item (§3.1–§3.12) specifying at least one test.

**Choices:**
- [x] Add verification test specs to all §3.x items
- [ ] Add a single "§3.14 Security Test Suite" section consolidating all security tests

### F7: §3.5 CSP scope not clarified for Vite dev mode [Applied]
<!-- severity: major -->
<!-- dimension: gaps -->

**Context:** §3.5 proposes `Content-Security-Policy "... connect-src 'self'; ..."` for the nginx config in `ui/Dockerfile`.

**Issue:** This CSP is applied only in the Docker nginx container, not during Vite dev mode (which uses a different proxy at `vite.config.ts:9-13`). This is fine for Docker-only deployment. However, the `connect-src 'self'` directive would block SSE connections if the browser connects to the UI on a different host/port than the API. In the Docker setup, nginx proxies `/api/` so `'self'` works. The plan correctly notes this but should explicitly state that the CSP does **not** apply to Vite dev mode and that adding a CSP meta tag to `ui/index.html` is out of scope for this item.

**Recommendation:** Add a note clarifying the scope: "This CSP applies only to the Docker nginx container. The Vite dev server does not serve these headers; CSP for dev mode is not required for a local-only tool."

**Choices:**
- [x] Add scope clarification note; no changes to Vite config
- [ ] Also add a CSP meta tag to `ui/index.html` for dev-mode coverage

### F8: §3.7 `cargo install cargo-audit` is slow in CI [Applied]
<!-- severity: minor -->
<!-- dimension: best-practices -->

**Context:** §3.7.1 proposes `cargo install cargo-audit` in CI.

**Issue:** `cargo install` compiles from source, adding 1–3 minutes to every CI run. The `rustsec/audit-check` GitHub Action provides a pre-built binary and is the idiomatic approach for CI.

**Recommendation:** Replace the `cargo install` approach with the `rustsec/audit-check` action:

```yaml
- name: Audit Rust dependencies
  uses: rustsec/audit-check@v2
  with:
    token: ${{ secrets.GITHUB_TOKEN }}
```

**Choices:**
- [x] Use `rustsec/audit-check@v2` action instead of `cargo install`
- [ ] Keep `cargo install` and cache `~/.cargo/bin` between runs

### F9: §3.8 Docker scan job has unnecessary `needs` dependency [Applied]
<!-- severity: minor -->
<!-- dimension: best-practices -->

**Context:** §3.8 specifies `needs: [rust]` for the `docker-scan` job.

**Issue:** The Docker build is self-contained (multi-stage Dockerfile). It does not use CI-built artifacts. The `needs` dependency creates an unnecessary serial dependency that slows CI. The only reason to depend on `rust` would be to avoid building the Docker image if Rust checks fail, but this is a workflow-design preference, not a correctness requirement.

**Recommendation:** Remove `needs: [rust]` to allow parallel execution, or clarify the intent (fail-fast optimization).

**Choices:**
- [x] Remove `needs: [rust]`; let Docker scan run in parallel
- [ ] Keep `needs: [rust]` and document it as a fail-fast optimization

### F10: Plan omits `let _ = scheduler.schedule(...)` error suppression [Applied]
<!-- severity: major -->
<!-- dimension: gaps -->

**Context:** §3.3 identifies `let _ = h.handle_event(...)` error suppression at four locations. However, `state.rs` also has `let _ = scheduler.schedule(...)` at lines 492, 500, 634, 642, 759, 765.

**Issue:** Scheduler errors (`EventOrderingViolation`) are silently discarded. While these should not occur in a well-behaved simulation (events are always scheduled in the future), suppressing them hides bugs. The `run_scenario` runner in `crates/sim-core/src/runner.rs:37-53` correctly uses `scheduler.schedule(...)?` to propagate these errors. The `state.rs` sim thread is inconsistent with this pattern.

**Recommendation:** Extend §3.3 to also cover `let _ = scheduler.schedule(...)` lines. Apply the same `tracing::warn!` pattern.

**Choices:**
- [x] Extend §3.3 to cover all `let _ = ...` suppressions in `state.rs`, including scheduler errors
- [ ] Address scheduler errors in a separate finding

### F11: NaN/Infinity check misplaced for JSON vs TOML [Applied]
<!-- severity: minor -->
<!-- dimension: gaps -->

**Context:** §3.11 adds `is_nan()` / `is_infinite()` checks to `change_price`. However, `serde_json` by default rejects NaN and Infinity when deserializing `f64` (they are not valid JSON numbers).

**Issue:** The proposed NaN/Infinity check in `routes.rs` is dead code — `serde_json` will reject the request with a deserialization error before the handler runs. The check is harmless but misleading. The scenario-level TOML validation in `scenario.rs` is different: `toml` crate **does** parse `nan` and `inf` as valid TOML floats, so the NaN/Infinity check matters there but not in the JSON API handler.

**Recommendation:** Move the NaN/Infinity guard to `validate_scenario` in `crates/sim-core/src/scenario.rs` where TOML parsing could produce these values. Remove or note as defensive in the API handler section.

**Choices:**
- [x] Add NaN/Infinity checks to `validate_scenario`; note the JSON handler check as defensive-only
- [ ] Keep both checks (defense in depth)

### F12: §3.6 doesn't mention dev container lockfile workflow [Applied]
<!-- severity: minor -->
<!-- dimension: plan-hygiene -->

**Context:** §3.6 proposes committing `Cargo.lock`. The dev container's `post-create.sh` runs `cargo build` which generates/updates `Cargo.lock`.

**Issue:** Once `Cargo.lock` is committed, `cargo build` in `post-create.sh` will use the committed lockfile (correct behavior). However, if a developer runs `cargo update` inside the dev container and doesn't commit the updated lockfile, subsequent CI runs will use the stale lockfile. This is standard Rust workflow, but the plan should mention it in the "Impact" section for completeness.

**Recommendation:** Add a brief note to §3.6: "After committing `Cargo.lock`, dependency updates require `cargo update` followed by committing the updated lockfile. Dev container users should be aware that local `cargo update` changes are not reflected in CI until committed."

**Choices:**
- [x] Add the note to §3.6
- [ ] No change needed (standard Rust knowledge)

### F13: Priority matrix inconsistency after F1 correction [Applied]
<!-- severity: minor -->
<!-- dimension: plan-hygiene -->

**Context:** §4 Priority Matrix shows §3.1 as "High" priority and first in the execution order (§5 step 2). §3.6 is also "High" and is step 1 in execution order.

**Issue:** §3.1's effective priority should be "Medium" now that we know Axum already provides a 2 MB default. The existing default is sufficient for the MVP threat model (local-only). Lowering to 1 MB is a hardening preference, not a critical fix. With F1 applied, the matrix should reflect the reduced urgency.

**Recommendation:** After applying F1, downgrade §3.1 from "High" to "Medium" in the priority matrix and move it after §3.7 in the execution order.

**Choices:**
- [x] Downgrade §3.1 to Medium priority; adjust execution order
- [ ] Keep as High (defense in depth justifies it)

### F14: §3.10 `Default` derive produces zero-capacity EventLog [Applied]
<!-- severity: major -->
<!-- dimension: correctness -->

**Context:** §3.10 proposes `#[derive(Debug, Clone, Default, Serialize)]` for the updated `EventLog` with a `max_capacity: usize` field. `Default` for `usize` is `0`.

**Issue:** `EventLog::default()` would create a log with `max_capacity = 0`, meaning `append()` would never accept any event. While `EventLog::default()` is not currently called directly (all usages are `EventLog::new()`), the derive `Default` is part of the existing API and could be used in tests or future code. Silently producing a broken instance violates the principle of least surprise.

**Recommendation:** Remove `Default` from the derive list and implement it manually to delegate to `EventLog::new()`.

**Choices:**
- [x] Implement `Default` manually, delegating to `new()` which sets `max_capacity` to 1,000,000
- [ ] Keep `Default` derive and document that default capacity is 0

### Summary

| # | Title | Severity | Dimension | Depends on |
|---|-------|----------|-----------|------------|
| F1 | §3.1 mischaracterizes Axum's existing body-size default | major | correctness | — |
| F2 | §3.2 `SyncSender` prose contradicts code sample | critical | correctness | — |
| F3 | §3.9 SSE semaphore code uses wrong ownership pattern | major | correctness | — |
| F4 | §3.10 event log cap breaks `PartialEq` for determinism tests | major | testing | — |
| F5 | §3.2 and §3.3 lack test specifications | major | testing | F2 |
| F6 | No security-focused tests for most plan items | major | testing | — |
| F7 | §3.5 CSP scope not clarified for Vite dev mode | major | gaps | — |
| F8 | §3.7 `cargo install cargo-audit` is slow in CI | minor | best-practices | — |
| F9 | §3.8 Docker scan job has unnecessary `needs` dependency | minor | best-practices | — |
| F10 | Plan omits scheduler error suppression in `state.rs` | major | gaps | — |
| F11 | NaN/Infinity check misplaced for JSON vs TOML | minor | gaps | — |
| F12 | §3.6 doesn't mention dev container lockfile workflow | minor | plan-hygiene | — |
| F13 | Priority matrix inconsistency after F1 correction | minor | plan-hygiene | F1 |
| F14 | §3.10 `Default` derive produces zero-capacity EventLog | major | correctness | F4 |
