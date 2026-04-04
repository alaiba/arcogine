# Security Improvement Plan

> **Date:** 2026-04-02
> **Last updated:** 2026-04-04 (all tasks implemented; implementation status block added)
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
| Price validation | `crates/sim-api/src/routes.rs:168-170` | Rejects negative prices; no upper bound |
| State guards | `crates/sim-api/src/routes.rs:83-91,119-127,142-147` | Commands rejected when no scenario loaded / sim completed |
| Error responses | `crates/sim-api/src/routes.rs:53-78` | Generic fixed strings; no stack traces in JSON |
| XSS prevention | `ui/src/` | React default escaping; no `dangerouslySetInnerHTML` |
| CSV injection mitigation | `ui/src/components/experiment/ExportMenu.tsx:18-23` | Escapes `"`, `,`, `\n`, `\r` in CSV cells |
| `.env` gitignored | `.gitignore:17-18` | `.env` and `.env.local` excluded from VCS |

---

## 2. Risk Assessment

Each item is rated by **likelihood** (if the API is reachable from an untrusted network) and **impact** (consequence of exploitation).

| # | Risk | Likelihood | Impact | Current Mitigation |
|---|------|-----------|--------|-------------------|
| R1 | Unauthenticated API access | High | High | None — any reachable client can drive the sim |
| R2 | Unrestricted CORS | High | Medium | `CorsLayer::allow_origin(Any)` in `server.rs:17` |
| R3 | Default body-size limit too generous | Low | Low | Axum 0.8 applies a 2 MB default; scenarios are ~1 KB |
| R4 | Unbounded event log growth | Medium | Medium | `EventLog` Vec grows without limit in memory |
| R5 | Unbounded SSE connections | Medium | Medium | No connection cap on broadcast channel |
| R6 | Scenario load reports false success | High | Low | `POST /api/scenario` always returns `success: true` |
| R7 | Silenced handler errors | Medium | Medium | `let _ = h.handle_event(...)` in `state.rs` |
| R8 | Default bind to all interfaces | Medium | Medium | `--addr 0.0.0.0:3000` default in `main.rs:23` |
| R9 | No security headers (nginx) | Low | Low | No CSP, X-Frame-Options, etc. |
| R10 | ~~`Cargo.lock` not committed~~ | ~~Medium~~ | ~~Low~~ | **Resolved** — `Cargo.lock` is committed and not gitignored |
| R11 | No dependency vulnerability scanning | Medium | Medium | No `cargo audit`, `npm audit`, or Dependabot |
| R12 | Docker images not scanned | Low | Medium | No Trivy/Snyk in CI |
| R13 | No TLS termination | Low | Medium | HTTP only in API and nginx |
| R14 | `f64` price/economy values not bounded above | Low | Low | No upper-bound or finiteness check on price or economy params |
| R15 | Event log export has no pagination | Low | Low | `/api/export/events` returns entire log as JSON |

---

## 3. Improvement Plan

Each item below is self-contained: it states the problem, the exact files and lines to change, the replacement code, and the verification tests. A coding agent can implement each item independently unless a dependency is noted.

### 3.1 Tighten Request Body-Size Limit

**Priority:** Medium  |  **Risk:** R3  |  **Effort:** Low  |  **Status:** [Done] 2026-04-04  |  **Depends on:** —

Axum 0.8 already applies a **2 MB default body limit** via `DefaultBodyLimit` on all extractors. Scenarios are ~1 KB TOML. Lowering the limit to 1 MB provides defense-in-depth.

**File:** `crates/sim-api/src/server.rs`

**Change:** Add a `DefaultBodyLimit` layer after the existing `cors` layer in `build_router()` (currently at line 15). No `Cargo.toml` change needed — `DefaultBodyLimit` is re-exported from `axum`.

```rust
use axum::extract::DefaultBodyLimit;

// Inside build_router(), append after .layer(cors):
    .layer(DefaultBodyLimit::max(1024 * 1024)) // 1 MB
```

The full `build_router` return should become:

```rust
Router::new()
    // ... all .route() calls unchanged ...
    .with_state(state)
    .layer(TraceLayer::new_for_http())
    .layer(cors)
    .layer(DefaultBodyLimit::max(1024 * 1024))
```

**Verification:** Add tests in `crates/sim-api/tests/api_smoke.rs`:
- `oversized_body_returns_413` — send `POST /api/scenario` with a JSON body larger than 1 MB → assert `413 Payload Too Large`
- `normal_body_accepted` — send `POST /api/scenario` with `basic_scenario_toml()` (< 1 KB) → assert `200 OK`

---

### 3.2 Fix Scenario Load Error Propagation

**Priority:** High  |  **Risk:** R6, R7  |  **Effort:** Medium  |  **Status:** [Done] 2026-04-04  |  **Depends on:** —

The `POST /api/scenario` endpoint sends `SimCommand::LoadScenario(body.toml)` and unconditionally returns `success: true` after a 50ms sleep. If parsing or validation fails, the sim thread logs a generic error and the client never learns the scenario was rejected.

**Files to change:**

1. **`crates/sim-api/src/state.rs`** — `SimCommand` enum (line 30) and `LoadScenario` match arm (line 378)

   Change `SimCommand::LoadScenario` from a tuple variant to a struct variant carrying a reply channel:

   ```rust
   #[derive(Debug, Clone)]
   pub enum SimCommand {
       LoadScenario {
           toml: String,
           reply: std::sync::mpsc::SyncSender<Result<(), String>>,
       },
       // Run, Pause, Step, Reset, ChangePrice, ChangeMachineCount, ToggleAgent, QuerySnapshot — unchanged
   }
   ```

   Use `std::sync::mpsc::SyncSender` (not `tokio::sync::oneshot`) because the sim thread is a plain OS thread. `SyncSender` implements both `Clone` and `Debug`, preserving `SimCommand`'s existing derives.

   In the sim thread match arm (currently line 378), send the result back:

   ```rust
   SimCommand::LoadScenario { toml: toml_str, reply } => {
       match sim_core::scenario::load_scenario(&toml_str) {
           Ok(cfg) => {
               // ... existing setup (build handler, scheduler, event_log, etc.) ...
               let _ = reply.send(Ok(()));
               // ... existing snapshot_tx.send, log_tx.send, handler/config assignment ...
           }
           Err(e) => {
               tracing::error!("Failed to load scenario: {e}");
               let _ = reply.send(Err(e.to_string()));
           }
       }
   }
   ```

2. **`crates/sim-api/src/routes.rs`** — `load_scenario` handler (lines 31-46)

   Replace the fire-and-forget send + sleep with a reply-channel wait:

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

3. **`crates/sim-api/src/state.rs`** — all other `SimCommand::LoadScenario` references

   Update `spawn_sim_thread` inline tests and any other `SimCommand::LoadScenario(...)` call sites (search for `SimCommand::LoadScenario`) to use the new struct-variant syntax with a reply channel.

**Verification:** Update the `load_scenario` test helper in `crates/sim-api/tests/api_smoke.rs` to return `(StatusCode, serde_json::Value)`. Add these tests:
- `load_valid_scenario_returns_success` — `basic_scenario_toml()` → 200, body `success == true`
- `load_invalid_toml_returns_bad_request` — `{ "toml": "not valid [[ toml" }` → 400, body `error` field present
- `load_scenario_with_zero_max_ticks_returns_bad_request` — valid TOML with `max_ticks = 0` → 400, body `error` contains "max_ticks"
- `load_scenario_with_missing_equipment_returns_bad_request` — TOML with `[simulation]` but no `[[equipment]]` → 400, body `error` contains "equipment"

---

### 3.3 Propagate Handler and Scheduler Errors in Simulation Thread

**Priority:** High  |  **Risk:** R7  |  **Effort:** Low  |  **Status:** [Done] 2026-04-04  |  **Depends on:** —

**File:** `crates/sim-api/src/state.rs`

Multiple locations suppress errors with `let _ = ...`:

**Handler errors** (`let _ = h.handle_event(&event, &mut scheduler)`):
- Line 432 (Step command)
- Line 481 (Run command)
- Line 563 (ChangePrice)
- Line 591 (ChangeMachineCount)

**Scheduler errors** (`let _ = scheduler.schedule(...)`):
- Lines 391, 399 (LoadScenario — initial periodic events)
- Lines 533, 541 (Reset — initial periodic events)
- Lines 658, 664 (`reschedule_periodic` function)

This is inconsistent with `crates/sim-core/src/runner.rs:37-53` which correctly uses `scheduler.schedule(...)?`.

**Changes:**

1. Add `last_error: Option<String>` to `SimSnapshot` (currently lines 92-105 in `state.rs`):

   ```rust
   pub struct SimSnapshot {
       // ... all existing fields ...
       pub last_error: Option<String>,
   }
   ```

   Update `Default for SimSnapshot` (lines 107-127) to include `last_error: None`.

2. Add a mutable `last_error: Option<String>` local in the sim thread (after line 372). Replace every `let _ = h.handle_event(...)` with:

   ```rust
   if let Err(e) = h.handle_event(&event, &mut scheduler) {
       tracing::warn!(error = %e, event_time = event.time.ticks(), "event handler error");
       last_error = Some(e.to_string());
   }
   ```

   Replace every `let _ = scheduler.schedule(...)` with:

   ```rust
   if let Err(e) = scheduler.schedule(Event::new(...)) {
       tracing::warn!(error = %e, "scheduler error");
       last_error = Some(e.to_string());
   }
   ```

3. Pass `last_error` into `build_snapshot` (line 260) and include it in the returned `SimSnapshot`. Clear `last_error` to `None` after each snapshot send so stale errors do not persist.

**Verification:** Add a test in `crates/sim-api/tests/api_smoke.rs`:
- `handler_error_surfaces_in_snapshot` — load a scenario, send `POST /api/machines` with `machine_id: 9999` (nonexistent), assert the response snapshot's `last_error` is `Some(...)` containing an error message

---

### 3.4 Restrict Default Bind Address

**Priority:** Medium  |  **Risk:** R8  |  **Effort:** Low  |  **Status:** [Done] 2026-04-04  |  **Depends on:** —

**File:** `crates/sim-cli/src/main.rs` — line 23

Change:
```rust
#[arg(long, default_value = "0.0.0.0:3000")]
```
To:
```rust
#[arg(long, default_value = "127.0.0.1:3000")]
```

**File:** `Dockerfile` — line 23

Keep as-is (Docker must bind to `0.0.0.0` for container networking):
```dockerfile
CMD ["serve", "--addr", "0.0.0.0:3000"]
```

**Verification:** Add a `#[test]` in `crates/sim-cli/src/main.rs` (in the existing `mod tests`):
- `default_bind_address_is_localhost` — use `Cli::try_parse_from(["arcogine", "serve"])` and assert `addr == "127.0.0.1:3000"`

---

### 3.5 Add Nginx Security Headers

**Priority:** Medium  |  **Risk:** R9  |  **Effort:** Low  |  **Status:** [Done] 2026-04-04  |  **Depends on:** —

**File:** `ui/Dockerfile` — inline nginx config heredoc (lines 15-40)

Replace the `server { ... }` block with:

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

    location = /health {
        add_header Content-Type text/plain;
        return 200 "ok";
    }

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

**CSP note:** `'unsafe-inline'` for `style-src` is needed because Tailwind CSS injects inline styles. `connect-src 'self'` covers both REST and SSE through the nginx proxy.

**Scope:** This CSP applies only to the Docker nginx container. The Vite dev server does not serve these headers. CSP for Vite dev mode is not required for a local-only tool and is out of scope.

**Verification:** Build the Docker UI image and verify headers with `curl -I http://localhost:5173/`. Assert that `Content-Security-Policy`, `X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy`, and `Permissions-Policy` headers are present.

---

### 3.6 Commit `Cargo.lock` to Version Control

**Priority:** High  |  **Risk:** R10  |  **Effort:** Low  |  **Status:** DONE

`Cargo.lock` exists at the repo root and is not listed in `.gitignore`. The CI cache key at `.github/workflows/ci.yml:31` already uses `hashFiles('**/Cargo.lock')`. `Dockerfile:5` copies `Cargo.lock` into the build context.

No further action required. Dependency updates should use `cargo update` followed by committing the updated lockfile.

---

### 3.7 Add Dependency Vulnerability Scanning to CI

**Priority:** High  |  **Risk:** R11  |  **Effort:** Low  |  **Status:** [Done] 2026-04-04  |  **Depends on:** 3.6 (done)

**File:** `.github/workflows/ci.yml`

#### 3.7.1 Rust: `cargo audit`

Add after the "Run tests" step in the `rust` job (after line 41). Use the `rustsec/audit-check` GitHub Action (pre-built binary, avoids slow `cargo install`):

```yaml
      - name: Audit Rust dependencies
        uses: rustsec/audit-check@v2
        with:
          token: ${{ secrets.GITHUB_TOKEN }}
```

#### 3.7.2 Frontend: `npm audit`

Add after the "Build" step in the `frontend` job (after line 76):

```yaml
      - name: Audit npm dependencies
        run: npm audit --audit-level=high
```

`--audit-level=high` avoids failing on low/moderate advisories that may not have fixes yet.

#### 3.7.3 GitHub Dependabot

Create new file `.github/dependabot.yml`:

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

**Verification:** CI fails on any known CRITICAL/HIGH vulnerability in Rust or npm dependencies.

---

### 3.8 Add Docker Image Scanning to CI

**Priority:** Medium  |  **Risk:** R12  |  **Effort:** Low  |  **Status:** [Done] 2026-04-04  |  **Depends on:** —

**File:** `.github/workflows/ci.yml`

Add a new job (not nested under any existing job):

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

**Verification:** The CI job fails with exit code 1 if critical or high vulnerabilities are found.

---

### 3.9 Bound SSE Connections

**Priority:** Medium  |  **Risk:** R5  |  **Effort:** Low  |  **Status:** [Done] 2026-04-04  |  **Depends on:** —

**Files to change:**

1. **`crates/sim-api/src/state.rs`** — `AppState` struct (line 129)

   Add a semaphore field:

   ```rust
   pub struct AppState {
       pub cmd_tx: mpsc::Sender<SimCommand>,
       pub snapshot_rx: watch::Receiver<SimSnapshot>,
       pub event_tx: broadcast::Sender<Event>,
       pub event_log_rx: watch::Receiver<EventLog>,
       pub sse_semaphore: Arc<tokio::sync::Semaphore>,
   }
   ```

2. **`crates/sim-api/src/server.rs`** — `create_app_state()` (line 49)

   Initialize the semaphore:

   ```rust
   Arc::new(AppState {
       cmd_tx,
       snapshot_rx,
       event_tx,
       event_log_rx,
       sse_semaphore: Arc::new(tokio::sync::Semaphore::new(64)),
   })
   ```

3. **`crates/sim-api/src/sse.rs`** — `event_stream` function (lines 17-32)

   Acquire an owned permit before streaming. The permit is held for the stream's lifetime and released on disconnect:

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

**Verification:** Add a test in `crates/sim-api/tests/api_smoke.rs`:
- `sse_connection_limit_returns_503` — open 64 SSE connections (via `tower::ServiceExt::oneshot` with `GET /api/events/stream`), then verify a 65th returns `503 Service Unavailable`

---

### 3.10 Cap Event Log Size

**Priority:** Medium  |  **Risk:** R4  |  **Effort:** Low  |  **Status:** [Done] 2026-04-04  |  **Depends on:** —

**File:** `crates/sim-core/src/log.rs`

The `EventLog` (line 10) currently derives `Default` and `PartialEq` and uses an unbounded `Vec<Event>`.

**Replace** the current struct and its `impl` block (lines 10-51) with:

```rust
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

/// Equality compares only the event data, not the capacity configuration.
/// This preserves determinism test compatibility where two runs may use
/// different capacity settings but should still compare equal.
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

    pub fn iter(&self) -> impl Iterator<Item = &Event> {
        self.events.iter()
    }

    pub fn filter_by_type(&self, event_type: EventType) -> impl Iterator<Item = &Event> {
        self.events
            .iter()
            .filter(move |e| e.event_type == event_type)
    }

    pub fn count(&self) -> usize {
        self.events.len()
    }

    pub fn snapshot(&self) -> EventLog {
        self.clone()
    }

    pub fn events(&self) -> &[Event] {
        &self.events
    }
}
```

**Verification:** Add unit tests in the existing `#[cfg(test)] mod tests` in `crates/sim-core/src/log.rs`:
- `event_log_caps_at_max_capacity` — `with_capacity(5)`, append 10 events, assert `count() == 5`
- `event_log_equality_ignores_capacity` — two logs with different capacities, same events, assert `==`
- `event_log_is_truncated` — `with_capacity(3)`, append 3 events, assert `is_truncated() == true`

---

### 3.11 Add Input Validation for Economy and Price Parameters

**Priority:** Low  |  **Risk:** R14  |  **Effort:** Low  |  **Status:** [Done] 2026-04-04  |  **Depends on:** —

**Two contexts require different treatment:**

1. **JSON API (`routes.rs`):** `serde_json` rejects NaN/Infinity during deserialization (not valid JSON), so NaN/Infinity checks in the handler are defensive-only. Add an upper-bound:

   **File:** `crates/sim-api/src/routes.rs` — `change_price` handler (line 164)

   Replace the existing check (line 168-170) with:

   ```rust
   const MAX_PRICE: f64 = 1_000_000.0;

   if body.price < 0.0 || body.price > MAX_PRICE {
       return Err(bad_request("Price must be between 0 and 1,000,000"));
   }
   ```

2. **TOML scenario (`scenario.rs`):** The `toml` crate **does** parse `nan` and `inf` as valid TOML floats, so NaN/Infinity checks are necessary here.

   **File:** `crates/sim-core/src/scenario.rs` — `validate_scenario` (replace lines 131-139)

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

**Verification:**
- In `crates/sim-core/tests/scenario_loading.rs`: `scenario_with_nan_price_rejected`, `scenario_with_inf_demand_rejected`, `scenario_with_extreme_price_rejected`
- In `crates/sim-api/tests/api_smoke.rs`: `extreme_price_returns_bad_request` — `POST /api/price` with `price: 2000000.0` → 400

---

### 3.12 Add Configurable CORS for Non-Development Deployments

**Priority:** Low  |  **Risk:** R2  |  **Effort:** Low  |  **Status:** [Done] 2026-04-04  |  **Depends on:** —

**File:** `crates/sim-api/src/server.rs` — replace lines 16-25

```rust
use std::env;
use axum::http::HeaderValue;

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

**File:** `.env.example` — append:

```
# CORS allowed origin (optional; defaults to permissive if unset)
# CORS_ALLOWED_ORIGIN=http://localhost:5173
```

**Verification:** Add a test in `crates/sim-api/tests/api_smoke.rs`:
- `cors_with_env_var_restricts_origin` — set `CORS_ALLOWED_ORIGIN=http://example.com` before calling `build_router`, send a request with `Origin: http://evil.com`, verify response lacks `Access-Control-Allow-Origin: http://evil.com`

---

### 3.13 Update `SECURITY.md` with Hardening Guidance

**Priority:** Low  |  **Risk:** —  |  **Effort:** Low  |  **Status:** [Done] 2026-04-04  |  **Depends on:** 3.4, 3.12

**File:** `SECURITY.md`

Append a new section after "Known Limitations":

```markdown
## Hardening for Network Deployment

If you deploy Arcogine outside a trusted local environment:

1. Use `--addr 127.0.0.1:3000` (the default) and place behind a reverse proxy with TLS termination.
2. Set the `CORS_ALLOWED_ORIGIN` environment variable to your UI's origin (e.g., `http://yourdomain.com`).
3. Run `cargo audit` and `npm audit` before each deployment.
4. Set `RUST_LOG=warn` in production to reduce log verbosity.
5. Consider adding authentication before exposing the API to untrusted networks.
```

**Verification:** Manual review — the section exists and references the correct env var and CLI flag.

---

### 3.14 CI Security Gates (No Deployment)

**Priority:** High  |  **Risk:** R11, R12  |  **Effort:** Medium  |  **Status:** [Done] 2026-04-04  |  **Depends on:** —

This item is CI-only and does not implement any deployment or image publishing.

**File:** `.github/workflows/ci.yml`

#### 3.14.1 Secret scanning

Add a new job:

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

If the repo produces noisy findings, create a `.gitleaks.toml` allowlist.

#### 3.14.2 Dependency audit evidence

In the `rust` job, after the audit step from 3.7.1, and in the `frontend` job:

```yaml
      - name: Audit npm dependencies
        run: npm audit --audit-level=high --json > npm-audit.json

      - name: Upload audit reports
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: security-audit-reports
          path: |
            npm-audit.json
```

#### 3.14.3 Scan both API and UI images

Expand `docker-scan` (from 3.8) with a matrix:

```yaml
  docker-scan:
    name: Docker image scan (${{ matrix.image }})
    runs-on: ubuntu-latest
    strategy:
      fail-fast: false
      matrix:
        image: [api, ui]
        include:
          - image: api
            context: .
          - image: ui
            context: ui
    steps:
      - uses: actions/checkout@v4
      - name: Build image
        run: docker build -t arcogine-${{ matrix.image }}:ci ${{ matrix.context }}
      - name: Scan image with Trivy
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: arcogine-${{ matrix.image }}:ci
          severity: 'CRITICAL,HIGH'
          format: 'table'
          exit-code: '1'
```

**Verification:** CI fails on any CRITICAL/HIGH issue from Rust/npm scans or either image scan, and artifacts include `npm-audit.json`.

---

## 4. Priority Matrix

| # | Item | Priority | Effort | Risk(s) | Depends on | Status |
|---|------|----------|--------|---------|------------|--------|
| 3.6 | Commit `Cargo.lock` | High | Low | R10 | — | **DONE** |
| 3.2 | Scenario load error propagation | High | Medium | R6, R7 | — | **[Done] 2026-04-04** |
| 3.3 | Handler and scheduler error propagation | High | Low | R7 | — | **[Done] 2026-04-04** |
| 3.7 | Dependency vulnerability scanning | High | Low | R11 | 3.6 ✓ | **[Done] 2026-04-04** |
| 3.14 | CI security gates | High | Medium | R11, R12 | — | **[Done] 2026-04-04** |
| 3.4 | Restrict default bind address | Medium | Low | R8 | — | **[Done] 2026-04-04** |
| 3.5 | Nginx security headers | Medium | Low | R9 | — | **[Done] 2026-04-04** |
| 3.1 | Tighten body-size limit | Medium | Low | R3 | — | **[Done] 2026-04-04** |
| 3.9 | Bound SSE connections | Medium | Low | R5 | — | **[Done] 2026-04-04** |
| 3.10 | Cap event log size | Medium | Low | R4 | — | **[Done] 2026-04-04** |
| 3.8 | Docker image scanning | Medium | Low | R12 | — | **[Done] 2026-04-04** |
| 3.11 | Economy/price input validation | Low | Low | R14 | — | **[Done] 2026-04-04** |
| 3.12 | Configurable CORS | Low | Low | R2 | — | **[Done] 2026-04-04** |
| 3.13 | Update SECURITY.md | Low | Low | — | 3.4, 3.12 | **[Done] 2026-04-04** |

---

## 5. Recommended Execution Order

Items are grouped into waves. Items within a wave can be done in parallel. 3.6 is already complete and omitted.

**Wave 1 — Functional correctness (High priority, unblocks observability)**
1. **3.3** — Handler and scheduler error propagation (quick win)
2. **3.2** — Scenario load error propagation (medium effort, high value)

**Wave 2 — Deployment defaults (Medium priority, low effort)**
3. **3.4** — Restrict default bind address
4. **3.12** — Configurable CORS

**Wave 3 — Resource exhaustion controls (Medium priority)**
5. **3.9** — Bound SSE connections
6. **3.10** — Cap event log size

**Wave 4 — Input hardening (Low-Medium priority)**
7. **3.1** — Tighten body-size limit
8. **3.11** — Economy/price input validation

**Wave 5 — Container and docs (Medium-Low priority)**
9. **3.5** — Nginx security headers
10. **3.13** — Update SECURITY.md

**Wave 6 — Supply-chain and CI gates (High priority, can run anytime)**
11. **3.7** — Dependency vulnerability scanning in CI
12. **3.8** — Docker image scanning in CI
13. **3.14** — CI security gates (Gitleaks, matrix Trivy, audit artifacts)

> Wave 6 is listed last because it only touches CI config and has no code dependencies on waves 1-5. It can be implemented at any point.

---

## Implementation Status (2026-04-04)

All 13 actionable tasks (3.1–3.5, 3.7–3.14) plus the pre-existing 3.6 are now **complete**.

### Completed tasks

| Task | Summary | Commit wave |
|------|---------|-------------|
| 3.3 | Handler/scheduler error propagation; `last_error` in `SimSnapshot` | Wave 1 |
| 3.2 | Scenario load error propagation via `SyncSender` reply channel | Wave 1 |
| 3.4 | Default bind address changed to `127.0.0.1:3000` | Wave 2 |
| 3.12 | CORS reads `CORS_ALLOWED_ORIGIN` env var; falls back to permissive | Wave 2 |
| 3.9 | SSE semaphore (64 permits); 503 when exhausted | Wave 3 |
| 3.10 | EventLog capacity cap (default 1M); manual PartialEq | Wave 3 |
| 3.1 | DefaultBodyLimit::max(1 MB) on Axum router | Wave 4 |
| 3.11 | Price upper bound (1M) in API; finiteness checks in TOML validation | Wave 4 |
| 3.5 | Nginx security headers: CSP, X-Frame-Options, etc. | Wave 5 |
| 3.13 | SECURITY.md "Hardening for Network Deployment" section | Wave 5 |
| 3.7 | rustsec/audit-check + npm audit + Dependabot config | Wave 6 |
| 3.8 | Trivy matrix scan for API + UI Docker images | Wave 6 |
| 3.14 | Gitleaks secret scan; npm-audit.json artifact on failure | Wave 6 |

### Test summary

- **Rust workspace:** 203 tests, 0 failures
- **Clippy:** 0 warnings (with `-D warnings`)
- **rustfmt:** clean

### Build/runtime fixes applied

- `SimCommand` derive changed from `#[derive(Debug, Clone)]` to `#[derive(Debug)]` because `SyncSender` does not implement `Clone` when used as a field. No call sites clone `SimCommand`, so this is safe.
- Added `reschedule_periodic_checked()` as a new function rather than modifying the existing `reschedule_periodic()`, to preserve backward compatibility for any external callers (the old function is still used in tests via `spawn_sim_thread`).
- `cargo fmt` applied as a separate commit after all implementation changes.

### Validations remaining

- **Task 3.5 (Nginx headers):** Verification requires building the Docker UI image and checking response headers with `curl -I`. This was not performed in the dev environment. Must be verified in CI or locally with Docker.
- **Task 3.7/3.8/3.14 (CI config):** CI YAML changes cannot be tested locally; verification requires a GitHub Actions run on push.

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

## Appendix A: Findings History

The following findings (F1–F14) were identified during internal review and have been applied to the plan body above. They are retained here as an audit trail. All items marked `[Applied]` — the plan text already reflects the chosen resolution.

| # | Title | Severity | Dimension | Resolution |
|---|-------|----------|-----------|------------|
| F1 | §3.1 mischaracterized Axum's body-size default | major | correctness | Rewritten to use `DefaultBodyLimit::max()` from Axum; priority downgraded to Medium |
| F2 | §3.2 `SyncSender` prose contradicted code sample | critical | correctness | Prose fixed to consistently say `std::sync::mpsc::SyncSender` |
| F3 | §3.9 SSE semaphore used wrong ownership pattern | major | correctness | Field type changed to `Arc<Semaphore>` |
| F4 | §3.10 event log cap would break `PartialEq` for determinism tests | major | testing | Manual `PartialEq` impl comparing only `events` |
| F5 | §3.2 and §3.3 lacked test specifications | major | testing | Verification blocks added with named tests |
| F6 | No security-focused tests for most plan items | major | testing | Verification blocks added to all §3.x items |
| F7 | §3.5 CSP scope not clarified for Vite dev mode | major | gaps | Scope note added |
| F8 | §3.7 proposed slow `cargo install cargo-audit` | minor | best-practices | Replaced with `rustsec/audit-check@v2` action |
| F9 | §3.8 Docker scan had unnecessary `needs` dependency | minor | best-practices | `needs: [rust]` removed |
| F10 | §3.3 omitted `let _ = scheduler.schedule(...)` suppression | major | gaps | Extended to cover all suppressions |
| F11 | NaN/Infinity check misplaced for JSON vs TOML | minor | gaps | Finiteness checks moved to `validate_scenario`; API check noted as defensive |
| F12 | §3.6 didn't mention dev container lockfile workflow | minor | plan-hygiene | Impact note added |
| F13 | Priority matrix inconsistency after F1 correction | minor | plan-hygiene | §3.1 downgraded to Medium |
| F14 | §3.10 `Default` derive would produce zero-capacity EventLog | major | correctness | Manual `Default` impl delegating to `new()` |
