# Logging Improvement Plan

> **Date:** 2026-04-04
> **Scope:** Audit existing logging practices across the Arcogine codebase and provide a prioritized, actionable plan to improve observability, diagnostic quality, and logging hygiene across Rust crates, the TypeScript UI, CI/CD, and container deployment.
> **Primary sources:** All crates under `crates/`, `ui/src/`, `.github/workflows/ci.yml`, `compose.yaml`, `Dockerfile`, `ui/Dockerfile`, `.env.example`, `docs/architecture.md`, `CONTRIBUTING.md`, `docs/TESTING.md`
> **Relationship to other plans:** Complements `devel/security-improvement-plan.md` (which references `RUST_LOG=warn` for production) and `devel/testability-improvement-plan.md` (which covers test infrastructure). Logging-related testing items are defined here; cross-references are noted.

---

## 1. Current State Assessment

### 1.1 Rust Logging Inventory

The project uses the `tracing` ecosystem for application-level logging and `tower-http`'s `TraceLayer` for HTTP request instrumentation.

| Location | Pattern | Purpose |
|----------|---------|---------|
| `crates/sim-cli/src/main.rs:136-141` | `tracing_subscriber::fmt().with_env_filter(EnvFilter::try_from_default_env().unwrap_or_else(\|_\| EnvFilter::new("info"))).init()` | Subscriber initialization; only logging setup in the binary |
| `crates/sim-cli/src/main.rs:149` | `tracing::error!("Server error: {}", e)` | Fatal server error |
| `crates/sim-api/src/server.rs:64` | `tracing::info!("Starting Arcogine API server on {}", addr)` | Startup banner |
| `crates/sim-api/src/server.rs:44` | `TraceLayer::new_for_http()` | Tower HTTP request/response tracing middleware |
| `crates/sim-api/src/state.rs:420` | `tracing::error!("Failed to load scenario")` | Scenario parse failure (error details discarded) |
| `crates/sim-cli/src/main.rs:165-170` | `println!(...)` | Headless run summary output (stdout, not tracing) |

**Dependency anomalies:**
- `crates/sim-core/Cargo.toml` lists `tracing = "0.1"` and `tracing-subscriber = "0.3"` as dependencies, but **no source file** in `crates/sim-core/src/` references `tracing` or `tracing_subscriber`. Both are unused.
- `crates/sim-api/Cargo.toml` lists `tracing-subscriber` as a runtime `[dependencies]` entry, but `sim-api` only uses `tracing` macros — subscriber initialization occurs in `sim-cli`. The `tracing-subscriber` dep should be a `[dev-dependencies]` entry (for test use only).

### 1.2 Frontend Logging Inventory

| Location | Pattern | Purpose |
|----------|---------|---------|
| `ui/src/components/shared/ErrorBoundary.tsx:19` | `console.error(error, info.componentStack)` | React error boundary — the only `console.*` call in application source |

No logging library (e.g. `pino`, `winston`, `loglevel`) is installed. The API client (`ui/src/api/client.ts`) and SSE client (`ui/src/api/sse.ts`) silently swallow errors: `catch { /* ignore */ }` in both files.

### 1.3 Log Configuration and Environment

| Aspect | Status |
|--------|--------|
| `RUST_LOG` env var | Honored by `EnvFilter::try_from_default_env()` in `sim-cli/main.rs`; **not documented** in `.env.example`, `README.md`, `CONTRIBUTING.md`, or `compose.yaml` |
| Default log level | `info` (hardcoded fallback in `sim-cli/main.rs:139`) |
| Log output format | `tracing_subscriber::fmt()` — human-readable text to stderr |
| Structured (JSON) logging | Not available; no `fmt::format::Json` layer configured |
| Log rotation / file output | None — all output goes to stderr |
| Docker log configuration | No `logging:` section in `compose.yaml`; relies on Docker's default `json-file` driver |
| Log level override in containers | `RUST_LOG` not passed in `compose.yaml` environment block |
| Nginx access/error logs | Default nginx logging in `ui/Dockerfile`; no customization |

### 1.4 Tracing Instrumentation Gaps

| Area | Gap |
|------|-----|
| **Simulation thread** (`state.rs`) | No logging for: scenario load success, sim start/complete/pause/reset, step count milestones, price changes, machine toggles, agent toggles. The `tracing::error!` on load failure discards the error value (`Err(_e)`). |
| **Event handler errors** (`state.rs:432,481`) | `let _ = h.handle_event(...)` — handler errors are silently ignored. No log emission on failure. |
| **Route handlers** (`routes.rs`) | Zero `tracing` calls in any route handler. Errors are returned as HTTP responses but never logged server-side. |
| **SSE endpoint** (`sse.rs`) | `Err(_) => None` silently drops broadcast lag errors. No logging of client connections or disconnections. |
| **Domain crates** (`sim-factory`, `sim-economy`, `sim-agents`) | Zero `tracing` usage. No instrumentation for significant domain events (e.g. job creation, machine state change, agent decisions). |
| **`sim-core`** | Zero `tracing` usage despite having `tracing` as a dependency. The event log (`log.rs`) is an in-memory simulation artifact, not application logging. |
| **Scenario loader** (`scenario.rs`) | Validation errors return `SimError` but are not logged. |

### 1.5 Key Problems

1. **Near-zero observability** — Only 3 `tracing` calls exist in the entire Rust codebase. In a running server, the only application-level log lines are the startup banner and catastrophic failures.
2. **Silent error swallowing** — Handler errors in the simulation thread (`let _ = h.handle_event(...)`) and scenario load errors (`Err(_e)`) lose diagnostic information.
3. **No structured logging option** — Production deployments cannot emit JSON logs for log aggregation pipelines.
4. **Undocumented RUST_LOG** — Operators have no guidance on available log targets or recommended verbosity.
5. **Unused tracing dependencies** — `sim-core` declares `tracing` and `tracing-subscriber` but never uses them. `sim-api` declares `tracing-subscriber` as a runtime dependency but only uses `tracing` macros (subscriber initialization happens in `sim-cli`).
6. **Frontend logging is ad-hoc** — A single `console.error` in `ErrorBoundary`; API and SSE errors are silently swallowed.
7. **No request ID / correlation** — HTTP requests lack trace IDs for correlating request logs with downstream behavior.

---

## 2. Improvement Plan

Items are ordered by priority and grouped into phases. Each item is self-contained with file paths, code changes, and verification criteria.

### Phase 1: Foundation — Fix Logging Hygiene (Priority: High)

#### 2.1 Remove Unused Tracing Dependencies from `sim-core` and `sim-api`

**Problem:** Two crates have unnecessary `tracing-subscriber` dependencies:

1. `crates/sim-core/Cargo.toml:12-13` lists both `tracing = "0.1"` and `tracing-subscriber = "0.3"` as dependencies, but no source file in `crates/sim-core/src/` references either crate.
2. `crates/sim-api/Cargo.toml:21` lists `tracing-subscriber = { version = "0.3", features = ["env-filter"] }` as a runtime dependency. Since `sim-api` is a library crate, subscriber initialization never happens here — it happens in `sim-cli`. The crate only uses `tracing` macros and `tower-http::TraceLayer`.

**Files:**
- `crates/sim-core/Cargo.toml:12-13`
- `crates/sim-api/Cargo.toml:21`

**Changes:**

For `sim-core`, remove both lines from `[dependencies]`:
```toml
# Remove:
tracing = "0.1"
tracing-subscriber = "0.3"
```

For `sim-api`, remove `tracing-subscriber` from `[dependencies]` and move it to `[dev-dependencies]` (needed for `with_test_writer()` in §3.3):
```toml
[dependencies]
# Keep:
tracing = "0.1"
# Remove tracing-subscriber from here

[dev-dependencies]
http-body-util = "0.1"
tracing-subscriber = { version = "0.3", features = ["env-filter"] }
```

**Verification:**
```bash
cargo build -p sim-core
cargo test -p sim-core
cargo build -p sim-api
cargo test -p sim-api
```

If `sim-core` needs `tracing` instrumentation in Phase 2, re-add `tracing` (not `tracing-subscriber`) as a dependency at that time.

#### 2.2 Document `RUST_LOG` in `.env.example` and `README.md`

**Problem:** The `RUST_LOG` environment variable controls log verbosity but is undocumented in `.env.example`, `README.md`, `CONTRIBUTING.md`, and `compose.yaml`.

**Files to change:**

1. **`.env.example`** — Add:
   ```
   # Log verbosity filter (default: info)
   # Examples: RUST_LOG=debug, RUST_LOG=warn, RUST_LOG=sim_api=debug,tower_http=debug
   # RUST_LOG=info
   ```

2. **`compose.yaml`** — Add `RUST_LOG` passthrough in the `api` service:
   ```yaml
   services:
     api:
       environment:
         - RUST_LOG=${RUST_LOG:-info}
   ```

3. **`README.md`** — Add a "Logging" subsection under the quick-start section explaining `RUST_LOG`.

**Verification:** `docker compose config` shows the environment variable. Manual test: `RUST_LOG=debug cargo run --bin arcogine -- serve` produces debug-level output.

#### 2.3 Log Scenario Load Errors with Full Context

**Problem:** `crates/sim-api/src/state.rs:419-420` discards the error value:
```rust
Err(_e) => {
    tracing::error!("Failed to load scenario");
}
```

**File:** `crates/sim-api/src/state.rs:419-420`

**Change:**
```rust
Err(e) => {
    tracing::error!(error = %e, "Failed to load scenario");
}
```

This uses `tracing`'s structured field syntax to include the error in both human-readable and structured output.

**Verification:** Load an invalid scenario via `POST /api/scenario` and confirm the error message appears in server logs with the parse error details.

#### 2.4 Log Silenced Handler Errors

**Problem:** `crates/sim-api/src/state.rs` uses `let _ = h.handle_event(&event, &mut scheduler);` in both the `Step` (line 432) and `Run` (line 481) command handlers, silently discarding handler errors.

**Files:** `crates/sim-api/src/state.rs:432` and `crates/sim-api/src/state.rs:481`

**Change:** Replace both occurrences:
```rust
if let Err(e) = h.handle_event(&event, &mut scheduler) {
    tracing::warn!(error = %e, time = event.time.ticks(), "Handler error during event processing");
}
```

Use `warn!` rather than `error!` because handler failures in the current design are non-fatal (the simulation continues).

**Verification:** Write a test that triggers a handler error and verify the log output includes the error and event time. See §3.2 for test details.

### Phase 2: API and Simulation Lifecycle Logging (Priority: High)

#### 2.5 Add Lifecycle Tracing to the Simulation Thread

**Problem:** The simulation thread in `state.rs` performs significant state transitions (load, run, pause, step, reset, complete) with zero logging. Operators cannot tell what the server is doing.

**File:** `crates/sim-api/src/state.rs` — inside the `match cmd { ... }` block

**Changes:** Add `tracing` calls at key transition points. Precise insertion locations within `crates/sim-api/src/state.rs`:

1. **`SimCommand::LoadScenario`** — Insert `tracing::info!("Loading scenario");` as the first statement inside the arm, between lines 380 and 381 (after `SimCommand::LoadScenario(toml_str) => {`, before `match sim_core::scenario::load_scenario`). Insert the success log before line 406 (`run_state = SimRunState::Paused;`):
   ```rust
   SimCommand::LoadScenario(toml_str) => {  // line 380
       tracing::info!("Loading scenario");  // NEW
       match sim_core::scenario::load_scenario(&toml_str) {
           Ok(cfg) => {
               // ... existing handler build code (lines 382-404) ...
               tracing::info!(                           // NEW
                   equipment = cfg.equipment.len(),
                   materials = cfg.material.len(),
                   max_ticks = cfg.simulation.max_ticks,
                   "Scenario loaded successfully"
               );
               run_state = SimRunState::Paused;          // existing line 406
               // ... existing snapshot code ...
           }
           Err(e) => {
               tracing::error!(error = %e, "Failed to load scenario");
           }
       }
   }
   ```

2. **`SimCommand::Run`** — Insert `tracing::info!("Simulation run started");` after line 471 (`if let (Some(h), Some(cfg))`) and before line 472 (`run_state = SimRunState::Running`). Insert the completion log after line 502 (`run_state = SimRunState::Completed;`):
   ```rust
   SimCommand::Run => {                                     // line 470
       if let (Some(h), Some(cfg)) = (&mut handler, &config) {  // line 471
           tracing::info!("Simulation run started");        // NEW
           run_state = SimRunState::Running;                // existing line 472
           // ... existing while-loop (lines 474-498) ...
           if run_state == SimRunState::Running {           // existing line 501
               run_state = SimRunState::Completed;          // existing line 502
               tracing::info!(events_processed, "Simulation completed");  // NEW
           }
           // ... existing snapshot code ...
       }
   }
   ```

3. **`SimCommand::Pause`** — Insert after line 520 (`run_state = SimRunState::Paused;`):
   ```rust
   SimCommand::Pause => {                              // line 518
       if run_state == SimRunState::Running {          // line 519
           run_state = SimRunState::Paused;            // existing line 520
           tracing::debug!("Simulation paused");       // NEW
       }
   }
   ```

4. **`SimCommand::Step`** — Insert after line 468 (`let _ = log_tx.send(event_log.clone());`), before the closing brace of the `if let` block at line 469:
   ```rust
   let _ = log_tx.send(event_log.clone());         // existing line 468
   tracing::trace!(events_processed, "Simulation step");  // NEW
   ```

5. **`SimCommand::Reset`** — Insert as first statement inside the `if let Some(cfg) = &config` block, after line 525:
   ```rust
   SimCommand::Reset => {                          // line 524
       if let Some(cfg) = &config {                // line 525
           tracing::info!("Simulation reset");     // NEW
           // ... existing handler rebuild code ...
       }
   }
   ```

6. **`SimCommand::ChangePrice(new_price)`** — Insert as first statement inside the `if let` block, after line 561:
   ```rust
   SimCommand::ChangePrice(new_price) => {                     // line 560
       if let (Some(h), Some(cfg)) = (&mut handler, &config) { // line 561
           tracing::debug!(new_price, "Price changed");         // NEW
           // ... existing code ...
       }
   }
   ```

7. **`SimCommand::ChangeMachineCount { machine_id, online }`** — Insert as first statement inside the `if let` block, after line 584:
   ```rust
   SimCommand::ChangeMachineCount { machine_id, online } => {   // line 583
       if let (Some(h), Some(cfg)) = (&mut handler, &config) {  // line 584
           tracing::debug!(machine_id, online, "Machine availability changed");  // NEW
           // ... existing code ...
       }
   }
   ```

8. **`SimCommand::ToggleAgent(enabled)`** — Insert as first statement in the arm, after line 611:
   ```rust
   SimCommand::ToggleAgent(enabled) => {           // line 611
       tracing::debug!(enabled, "Agent toggled");  // NEW
       agent_enabled = enabled;                    // existing line 612
       // ... existing code ...
   }
   ```

Level rationale:
- `info` for lifecycle transitions visible at default verbosity (load, run start/complete, reset)
- `debug` for user-initiated mutations (price, machine, agent, pause)
- `trace` for high-frequency operations (step)

**Verification:** Run the server with `RUST_LOG=debug`, load a scenario, run it, and confirm lifecycle events appear in logs.

#### 2.6 Add Debug-Level Logging for Dropped SSE Broadcast Messages

**Problem:** `crates/sim-api/src/sse.rs:28` silently drops `BroadcastStream` errors (which indicate client lag):
```rust
Err(_) => None,
```

**File:** `crates/sim-api/src/sse.rs:21-29`

**Change:**
```rust
let stream = BroadcastStream::new(rx).filter_map(|result| match result {
    Ok(event) => {
        let json = serde_json::to_string(&event).unwrap_or_default();
        Some(Ok(SseEvent::default()
            .event(format!("{:?}", event.event_type))
            .data(json)))
    }
    Err(e) => {
        tracing::debug!(error = %e, "SSE broadcast lag — dropped events for a client");
        None
    }
});
```

Use `debug` level because lag is expected under normal operation (the broadcast channel has a fixed buffer of 4096) and logging at `warn` would be noisy.

**Verification:** Add `tracing` to `sim-api/Cargo.toml` if not already present (it is). Run with `RUST_LOG=sim_api::sse=debug` and a slow SSE client to confirm lag messages appear.

#### 2.7 Add Request-Level Logging for Mutating API Routes

**Problem:** Route handlers in `routes.rs` have zero `tracing` calls. Errors are returned as HTTP responses but never logged server-side. While `TraceLayer` logs the HTTP request/response cycle, it does not capture application-level context like which command was sent or why it failed.

**File:** `crates/sim-api/src/routes.rs`

**Changes:** Add `tracing::warn!` to error paths and `tracing::debug!` to successful mutations:

```rust
pub async fn load_scenario(...) -> ... {
    tracing::debug!("Received load_scenario request");
    // ... on error:
    // tracing::warn!(error = msg, "load_scenario failed");
}
```

Apply to: `load_scenario`, `run_sim`, `pause_sim`, `step_sim`, `reset_sim`, `change_price`, `change_machine`, `toggle_agent`.

Level rationale:
- `debug` for successful command dispatch (visible with `RUST_LOG=sim_api=debug`)
- `warn` for application-level errors (visible at default `info` level)

**Verification:** Send invalid requests and confirm warnings appear in server logs.

### Phase 3: Structured Logging and Configuration (Priority: Medium)

#### 2.8 Add Optional JSON Log Format

**Problem:** The current `tracing_subscriber::fmt()` emits human-readable text. Production deployments behind log aggregation pipelines (ELK, Datadog, CloudWatch) benefit from structured JSON output.

**File:** `crates/sim-cli/src/main.rs:136-141`

**Change:** Add a `--log-format` CLI flag:
```rust
/// Start the HTTP API server.
Serve {
    /// Address to bind the server to.
    #[arg(long, default_value = "0.0.0.0:3000")]
    addr: String,

    /// Log output format: "text" or "json".
    #[arg(long, default_value = "text")]
    log_format: String,
},
```

Then in `main()`:
```rust
let env_filter = tracing_subscriber::EnvFilter::try_from_default_env()
    .unwrap_or_else(|_| tracing_subscriber::EnvFilter::new("info"));

match log_format.as_str() {
    "json" => {
        tracing_subscriber::fmt()
            .json()
            .with_env_filter(env_filter)
            .init();
    }
    _ => {
        tracing_subscriber::fmt()
            .with_env_filter(env_filter)
            .init();
    }
}
```

Update the `Run` subcommand to accept the same flag.

**Dependency change required:** The `json` feature is **not** included in the default feature set of `tracing-subscriber` 0.3. Update `crates/sim-cli/Cargo.toml`:
```toml
tracing-subscriber = { version = "0.3", features = ["env-filter", "json"] }
```
This pulls in `tracing-serde`, `serde`, and `serde_json` as transitive dependencies. `serde` and `serde_json` are already workspace-wide dependencies, so only `tracing-serde` is net-new.

**Verification:** `cargo run --bin arcogine -- serve --log-format json` emits JSON lines. `cargo run --bin arcogine -- serve` emits human-readable text (backward-compatible default).

#### 2.9 Add Request ID Middleware for Correlation

**Problem:** HTTP requests lack a unique identifier for correlating log lines across a single request's lifecycle (route handler → simulation thread → response).

**File:** `crates/sim-api/src/server.rs`

**Dependency change required:** The `request-id` feature must be enabled in `crates/sim-api/Cargo.toml`:
```toml
tower-http = { version = "0.6", features = ["trace", "cors", "request-id"] }
```

**Change:** Use `tower-http`'s `SetRequestIdLayer` and `PropagateRequestIdLayer` to inject and propagate request IDs. Customize `TraceLayer`'s span to include them:

```rust
use tower_http::request_id::{SetRequestIdLayer, PropagateRequestIdLayer, MakeRequestUuid};
use tower_http::trace::{TraceLayer, DefaultMakeSpan, DefaultOnResponse};

Router::new()
    // ... routes ...
    .layer(PropagateRequestIdLayer::x_request_id())
    .layer(
        TraceLayer::new_for_http()
            .make_span_with(DefaultMakeSpan::new().level(tracing::Level::INFO))
            .on_response(DefaultOnResponse::new().level(tracing::Level::INFO))
    )
    .layer(SetRequestIdLayer::x_request_id(MakeRequestUuid))
    .layer(cors)
```

Note: `MakeRequestUuid` is provided by `tower-http` when the `request-id` feature is enabled; `uuid` is pulled in as a transitive dependency — no explicit `uuid` entry is needed in `Cargo.toml`. If the transitive `uuid` dependency is undesirable, implement a simple counter-based `MakeRequestId` instead.

**Verification:** Inspect log output for HTTP requests and confirm each request/response pair shares a span with an `x-request-id` header value.

### Phase 4: Frontend Logging Improvements (Priority: Medium)

#### 2.10 Add Error Logging to API Client

**Problem:** `ui/src/api/client.ts` throws errors on non-OK responses but never logs them. Callers (Zustand stores) may catch and handle these errors, but there is no centralized record of API failures for debugging.

**File:** `ui/src/api/client.ts:24-25` — inside the `if (!res.ok)` block

**Change:** Insert a `console.warn` call between the `if` guard (line 24) and the `throw` (line 25):
```typescript
if (!res.ok) {
    const message = await readErrorMessage(res);
    console.warn(`[api] ${init?.method ?? 'GET'} ${path} → ${res.status}: ${message}`);
    throw new Error(message);
}
```

This preserves the existing throw behavior while adding a diagnostic breadcrumb.

**Verification:** Open browser DevTools, trigger an API error (e.g. run without scenario loaded), and confirm the warning appears in the console.

#### 2.11 Add Error Logging to SSE Client

**Problem:** `ui/src/api/sse.ts:82-84` silently ignores malformed SSE payloads:
```typescript
catch {
    /* ignore malformed payloads */
}
```

And `onerror` (line 95-103) silently reconnects without logging.

**File:** `ui/src/api/sse.ts`

**Changes:**
```typescript
// Line 82-84: Add logging for parse failures
catch (err) {
    console.debug('[sse] Failed to parse event data:', ev.data);
}

// Line 95: Add logging for connection errors
es.onerror = () => {
    console.debug('[sse] Connection error, scheduling reconnect');
    // ... existing reconnect logic ...
};
```

Use `console.debug` to avoid noise at default browser console verbosity.

**Verification:** Trigger an SSE disconnect (stop the API server) and confirm debug messages appear in browser DevTools when verbose logging is enabled.

### Phase 5: Container and Operations Logging (Priority: Low)

#### 2.12 Configure Docker Compose Log Limits

**Problem:** `compose.yaml` has no `logging:` configuration. The default Docker `json-file` driver stores logs indefinitely, which can exhaust disk space in long-running deployments.

**File:** `compose.yaml`

**Change:** Add log rotation configuration to both services:
```yaml
services:
  api:
    logging:
      driver: json-file
      options:
        max-size: "10m"
        max-file: "3"
  ui:
    logging:
      driver: json-file
      options:
        max-size: "5m"
        max-file: "3"
```

**Verification:** `docker compose config` shows the logging configuration.

#### 2.13 Add Nginx Access Log Format Customization

**Problem:** The nginx configuration in `ui/Dockerfile` uses default log formatting. For consistency with structured logging in the API, a JSON access log format would be beneficial.

**File:** `ui/Dockerfile` — nginx config block

**Change:** Add a JSON log format directive to the nginx configuration:
```nginx
log_format json_combined escape=json
    '{"time":"$time_iso8601","remote_addr":"$remote_addr",'
    '"request":"$request","status":$status,'
    '"body_bytes_sent":$body_bytes_sent,'
    '"upstream_response_time":"$upstream_response_time"}';

access_log /var/log/nginx/access.log json_combined;
```

**Verification:** `docker compose up` and inspect `docker compose logs ui` for JSON-formatted access logs.

---

## 3. Testing Strategy

Each improvement item should include appropriate test coverage. Items are referenced by their section number.

### 3.1 Unit Test: Subscriber Initialization Does Not Panic

**File:** `crates/sim-cli/src/main.rs` (inline `#[cfg(test)]` module)

Test that the tracing subscriber setup logic does not panic when `RUST_LOG` is unset and when it contains an invalid filter string. This validates the `unwrap_or_else` fallback in §2.8.

### 3.2 Unit Test: Handler Errors Are Logged (§2.4)

**File:** `crates/sim-api/src/state.rs` (inline `#[cfg(test)]` module)

Create a handler implementation that returns `Err(SimError::Other {...})` and verify that the simulation thread continues processing after the error (non-fatal behavior). The test validates that the `if let Err(e) = ...` pattern does not break the event loop.

### 3.3 Integration Test: Lifecycle Log Messages Appear (§2.5)

**File:** `crates/sim-api/tests/` or inline tests

**Prerequisite:** `tracing-subscriber` must be available as a dev-dependency in `sim-api` (see §2.1 — it is moved to `[dev-dependencies]`, not removed entirely).

Use `tracing_subscriber::fmt().with_test_writer()` in a test to capture log output, then execute a load→run→complete cycle and assert that expected log messages are present.

### 3.4 Frontend Test: API Client Logs Warnings (§2.10)

**File:** `ui/src/api/client.test.ts` (existing file — extend, do not overwrite)

The file already contains tests for error extraction and non-OK responses. Add a new test case inside the existing `describe('jsonRequest', ...)` block that spies on `console.warn` and asserts it is called with the expected `[api]` prefix when a request fails:

```typescript
it('logs a warning on non-OK response', async () => {
  const warnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});
  fetchMock.mockResolvedValue(jsonResponse({ error: 'conflict' }, 409));
  await expect(postSimRun()).rejects.toThrow('conflict');
  expect(warnSpy).toHaveBeenCalledWith(expect.stringContaining('[api]'));
  warnSpy.mockRestore();
});
```

### 3.5 Frontend Test: SSE Client Logs Parse Failures (§2.11)

**File:** `ui/src/api/sse.test.ts` (existing file — extend, do not overwrite)

The file already tests that malformed JSON is silently ignored (line 100-107). Add a companion test that spies on `console.debug` and asserts the `[sse]` prefix message:

```typescript
it('logs debug message for malformed JSON', async () => {
  const debugSpy = vi.spyOn(console, 'debug').mockImplementation(() => {});
  client.connect();
  await vi.advanceTimersByTimeAsync(0);

  const es = (client as unknown as { es: MockEventSource }).es;
  es.emit('TaskEnd', 'not-json');
  expect(debugSpy).toHaveBeenCalledWith(expect.stringContaining('[sse]'), expect.anything());
  debugSpy.mockRestore();
});
```

### 3.6 CI: Ensure No Unintended `println!` in Library Crates

Add a CI step or clippy configuration to flag `println!` usage in library crates (all crates except `sim-cli`). `println!` is acceptable in the CLI binary for user-facing output but should not appear in library code.

**Note:** No current violations exist in any library crate. This is a preventive CI gate to catch future regressions.

```bash
# In CI, after clippy:
! grep -rn 'println!' crates/sim-types/src crates/sim-core/src crates/sim-factory/src \
  crates/sim-economy/src crates/sim-agents/src crates/sim-api/src
```

---

## 4. Priority Matrix

| Item | Phase | Priority | Effort | Risk if Skipped |
|------|-------|----------|--------|----------------|
| 2.1 Remove unused deps | 1 | High | Low | Misleading dependency graph; `cargo audit` noise |
| 2.2 Document `RUST_LOG` | 1 | High | Low | Operators cannot configure log verbosity |
| 2.3 Log scenario errors | 1 | High | Low | Silent failures make debugging impossible |
| 2.4 Log handler errors | 1 | High | Low | Silent data loss in simulation thread |
| 2.5 Lifecycle tracing | 2 | High | Medium | No visibility into server behavior |
| 2.6 SSE lag logging | 2 | Medium | Low | Cannot diagnose SSE client issues |
| 2.7 Route handler logging | 2 | Medium | Medium | Server-side error diagnosis relies solely on HTTP responses |
| 2.8 JSON log format | 3 | Medium | Medium | Cannot integrate with log aggregation |
| 2.9 Request ID correlation | 3 | Medium | Medium | Cannot trace requests across components |
| 2.10 API client logging | 4 | Medium | Low | Frontend debugging harder |
| 2.11 SSE client logging | 4 | Low | Low | SSE issues invisible in browser |
| 2.12 Docker log limits | 5 | Low | Low | Disk exhaustion in long runs |
| 2.13 Nginx JSON logs | 5 | Low | Low | Inconsistent log format across stack |

---

## 5. Execution Guidance

### Recommended Implementation Order

1. **Phase 1 (items 2.1–2.4):** Quick wins that fix hygiene issues. Can be done in a single PR.
2. **Phase 2 (items 2.5–2.7):** Core observability improvements. Should be a separate PR for review clarity.
3. **Phase 3 (items 2.8–2.9):** Production-readiness features. Implement when deployment plans firm up.
4. **Phase 4 (items 2.10–2.11):** Frontend improvements. Can be merged with any Phase 2/3 PR.
5. **Phase 5 (items 2.12–2.13):** Operational polish. Low urgency.

### Conventions to Adopt

Based on the existing codebase patterns in `CONTRIBUTING.md` and `docs/architecture.md`:

1. **Use `tracing` macros, not `log` macros** — The project already standardizes on `tracing`. Do not introduce the `log` crate.
2. **Use structured fields** — Prefer `tracing::info!(key = value, "message")` over `tracing::info!("message: {}", value)` for machine-parseable output.
3. **Level discipline:**
   - `error!` — Unrecoverable failures (process should exit or feature is broken)
   - `warn!` — Unexpected conditions that are handled (non-fatal errors, degraded behavior)
   - `info!` — Significant lifecycle events (startup, shutdown, scenario load, run complete)
   - `debug!` — Detailed operational events (route calls, user mutations, SSE events)
   - `trace!` — High-frequency events (individual simulation steps, scheduler operations)
4. **Target-aware filtering** — Document that `RUST_LOG=sim_api=debug,tower_http=debug` enables verbose API logging without flooding from other crates.
5. **No `println!` in library crates** — Use `tracing` for all diagnostic output. `println!` is reserved for the CLI binary's user-facing output.
6. **Frontend: prefix console calls** — Use `[api]`, `[sse]`, `[store]` prefixes for `console.*` calls to aid filtering in browser DevTools.

### Out of Scope

- **External log aggregation setup** (ELK, Datadog, etc.) — infrastructure concern
- **Distributed tracing** (Jaeger, Zipkin) — unnecessary for single-process architecture
- **Application Performance Monitoring (APM)** — premature for MVP
- **Log-based alerting** — requires production monitoring stack
- **`sim-core` domain-level `tracing` instrumentation** — the core engine's `EventLog` serves the observability role for simulation events; adding `tracing` to hot-path simulation code could impact determinism benchmarks

---

## 6. Appendix

### A. Full `tracing` Call Inventory (as of 2026-04-04)

| File | Line | Level | Message |
|------|------|-------|---------|
| `crates/sim-cli/src/main.rs` | 149 | `error` | `"Server error: {}"` |
| `crates/sim-api/src/server.rs` | 64 | `info` | `"Starting Arcogine API server on {}"` |
| `crates/sim-api/src/state.rs` | 420 | `error` | `"Failed to load scenario"` |

### B. `console.*` Inventory (UI, as of 2026-04-04)

| File | Line | Level | Context |
|------|------|-------|---------|
| `ui/src/components/shared/ErrorBoundary.tsx` | 19 | `error` | React error boundary catch |

### C. `println!` Inventory (Rust, as of 2026-04-04)

| File | Lines | Purpose |
|------|-------|---------|
| `crates/sim-cli/src/main.rs` | 165-170 | Headless run summary (user-facing CLI output) |

---

## Findings

### F1: JSON feature not in `tracing-subscriber` default features [Applied]
<!-- severity: critical -->
<!-- dimension: correctness -->

**Context:** §2.8 states: "**Dependency:** `tracing-subscriber` already has the `json` feature available (it's included in the default feature set of `tracing-subscriber` 0.3)."

The actual `Cargo.toml` for `sim-cli` (line 21) reads:
```toml
tracing-subscriber = { version = "0.3", features = ["env-filter"] }
```

**Issue:** The `json` feature is **not** part of the default feature set of `tracing-subscriber` 0.3. It must be explicitly enabled and pulls in `tracing-serde`, `serde`, and `serde_json` as transitive dependencies. A coding agent following the plan as written would produce code that fails to compile.

**Recommendation:** Correct the dependency claim and add the required feature flag to the implementation instructions.

**Choices:**
- [x] Fix the dependency statement to require `features = ["env-filter", "json"]` and note the transitive deps
- [ ] Remove the JSON log format item entirely (defer to a future plan)

### F2: §2.6 title/body log-level mismatch [Applied]
<!-- severity: major -->
<!-- dimension: plan-hygiene -->

**Context:** §2.6 is titled "Add Warn-Level Logging for Dropped SSE Broadcast Messages" but the code sample and rationale both specify `tracing::debug!`. The rationale text explicitly says "Use `debug` level because lag is expected under normal operation."

**Issue:** A coding agent reading only the title would use `warn!`, contradicting the body. This inconsistency creates ambiguity about the intended log level.

**Recommendation:** Rename the section to match the body.

**Choices:**
- [x] Rename to "Add Debug-Level Logging for Dropped SSE Broadcast Messages"
- [ ] Change the body to use `warn!` to match the title

### F3: §3.4 and §3.5 suggest creating test files that already exist [Applied]
<!-- severity: major -->
<!-- dimension: correctness -->

**Context:** §3.4 says "**File:** `ui/src/api/client.test.ts`" and §3.5 says "**File:** `ui/src/api/sse.test.ts`" as if these files need to be created. Both files already exist with substantial test coverage (93 lines and 149 lines respectively).

`ui/src/api/client.test.ts` already tests error handling paths (extracting error fields, falling back to status text).
`ui/src/api/sse.test.ts` already tests malformed JSON handling (line 100-107) and reconnection logic.

**Issue:** A coding agent may overwrite the existing test files or create duplicate test suites. The plan should instruct extending, not creating.

**Recommendation:** Rewrite §3.4 and §3.5 to instruct *adding test cases* to the existing files, specifically for the new `console.warn` / `console.debug` calls.

**Choices:**
- [x] Rewrite to "Extend existing test file with a new test case for console.warn/debug spy"
- [ ] Remove §3.4/§3.5 and rely on manual verification

### F4: §2.9 missing `tower-http` feature flag for `request-id` [Applied]
<!-- severity: major -->
<!-- dimension: correctness -->

**Context:** §2.9 proposes using `tower-http`'s `RequestIdLayer` but does not mention the required feature flag. The current `sim-api/Cargo.toml:18` reads:
```toml
tower-http = { version = "0.6", features = ["trace", "cors"] }
```

**Issue:** The `request-id` feature is not enabled. The `propagate-header` and `set-header` features mentioned in the text also require explicit activation. A coding agent following the plan would get compilation errors.

**Recommendation:** Add the `request-id` feature flag to the change instructions and update `sim-api/Cargo.toml`.

**Choices:**
- [x] Add `features = ["trace", "cors", "request-id"]` to the §2.9 instructions
- [ ] Simplify §2.9 to only use `DefaultMakeSpan` customization (no new feature needed)

### F5: §2.10 line reference off-by-one [Applied]
<!-- severity: minor -->
<!-- dimension: correctness -->

**Context:** §2.10 references `ui/src/api/client.ts:24-26` for the error-throw code. The actual code at those lines:
- Line 24: `if (!res.ok) {`
- Line 25: `    throw new Error(await readErrorMessage(res));`
- Line 26: `  }`

**Issue:** The change inserts a `console.warn` between the `if` and the `throw`, which means lines 24-26 is the correct range of the `if`-block (including the closing brace). The reference is technically valid but should note the new line is inserted between lines 24 and 25.

**Recommendation:** Clarify to "Insert `console.warn` between lines 24 and 25, inside the `if (!res.ok)` block."

**Choices:**
- [x] Clarify insertion point in §2.10
- [ ] Leave as-is (the code sample is unambiguous)

### F6: §3.6 `println!` guard has no current violations to fix [Applied]
<!-- severity: minor -->
<!-- dimension: testing -->

**Context:** §3.6 proposes a CI check to catch `println!` in library crates. A search of all library crate source directories (`sim-types`, `sim-core`, `sim-factory`, `sim-economy`, `sim-agents`, `sim-api`) finds zero `println!` occurrences.

**Issue:** The guard is purely preventive (good), but the plan does not acknowledge that there are no current violations. A coding agent might spend time investigating non-existent issues.

**Recommendation:** Add a note that this is a preventive measure with no current violations.

**Choices:**
- [x] Add clarifying note "No current violations exist; this is a preventive CI gate"
- [ ] Remove §3.6 as unnecessary

### F7: §2.5 code samples lack precise insertion-point guidance [Applied]
<!-- severity: major -->
<!-- dimension: correctness -->

**Context:** §2.5 shows code samples like:
```rust
SimCommand::Run => {
    tracing::info!("Simulation run started");
    // ... existing code ...
    if run_state == SimRunState::Completed {
        tracing::info!(events_processed, "Simulation completed");
    }
}
```

In `state.rs`, the `SimCommand::Run` arm spans lines 469-514 with a complex while-loop, pause-check, and state transition. The `SimCommand::Step` arm spans lines 425-467. Each arm has multiple plausible insertion points.

**Issue:** The code samples show correct `tracing` syntax but don't specify where within each arm to insert the calls. For example, should the "Simulation completed" log go at line 501 (after `run_state = SimRunState::Completed`) or at line 512 (after the snapshot is sent)? A coding agent may place logs before state is finalized, producing misleading output.

**Recommendation:** Add explicit "Insert after line X" guidance for each tracing call.

**Choices:**
- [x] Add precise line-number insertion points for each tracing call in §2.5
- [ ] Restructure code samples to show 3+ lines of surrounding context

### F8: `sim-api` also has unused `tracing-subscriber` dependency [Applied]
<!-- severity: minor -->
<!-- dimension: gaps -->

**Context:** §2.1 identifies `sim-core/Cargo.toml` as having unused `tracing` and `tracing-subscriber` deps. However, `sim-api/Cargo.toml:21` also lists:
```toml
tracing-subscriber = { version = "0.3", features = ["env-filter"] }
```

Since `sim-api` is a library crate (not a binary), subscriber initialization never happens in `sim-api` — it happens in `sim-cli`. The `sim-api` crate only uses `tracing` macros (`tracing::info!`, `tracing::error!`) and `TraceLayer` from `tower-http`, none of which require `tracing-subscriber`.

**Issue:** The plan misses this second unused `tracing-subscriber` dependency.

**Recommendation:** Extend §2.1 to also address `sim-api/Cargo.toml`: remove `tracing-subscriber` from `[dependencies]`, move to `[dev-dependencies]` for test use (§3.3).

**Choices:**
- [x] Extend §2.1 to also handle `sim-api/Cargo.toml` (remove from deps, move to dev-deps)
- [ ] Leave it — sim-api might use subscriber setup in tests later
<!-- Depends on: F9 choice "Move tracing-subscriber to dev-deps" -->

### F9: §3.3 proposes `with_test_writer()` but does not note dev-dependency [Applied]
<!-- severity: minor -->
<!-- dimension: testing -->

**Context:** §3.3 proposes using `tracing_subscriber::fmt().with_test_writer()` in tests to capture log output. If §2.1/F8 remove `tracing-subscriber` from `sim-api`'s `[dependencies]`, it would need to be re-added as a `[dev-dependencies]` entry for this test strategy to work.

**Issue:** The test strategy creates a hidden dependency on the very crate being removed from runtime deps.

**Recommendation:** Note that `tracing-subscriber` should be moved to `[dev-dependencies]` in `sim-api/Cargo.toml`, not deleted entirely.

**Choices:**
- [x] Move `tracing-subscriber` to `[dev-dependencies]` in `sim-api/Cargo.toml` instead of removing it
- [ ] Use a different test capture approach that doesn't need `tracing-subscriber`

### F10: §2.5 line numbers inaccurate after F7 application [Applied]
<!-- severity: major -->
<!-- dimension: correctness -->

**Context:** After applying F7 (adding insertion-point guidance), the line numbers in §2.5 were approximate and contained errors:
- Item 1: "after line 379" should be after line 380 (`SimCommand::LoadScenario(toml_str) => {`). "After line 416, before line 405" was self-contradictory (416 > 405).
- Item 2: "after line 470" should be after line 471 (the `if let` guard). Line 501 is the `if run_state == SimRunState::Running` check, not the assignment.
- Item 3: "after line 518" is the arm header, not the if-guard. The insert should be after line 520 (`run_state = SimRunState::Paused;`).
- Items 6-8: Line numbers for ChangePrice (557→561), ChangeMachine (580→584), ToggleAgent (607→611) were off by several lines.

**Issue:** Incorrect line numbers would cause a coding agent to insert logging calls at the wrong positions, potentially before state transitions are complete or in the wrong scope.

**Recommendation:** Correct all line numbers to match actual `state.rs` content, with surrounding context lines shown.

**Choices:**
- [x] Rewrite §2.5 with corrected line numbers and surrounding context (applied)
- [ ] Remove line numbers and use only contextual code anchors

### F11: §1.1 and §1.5 inconsistent with updated §2.1 scope [Applied]
<!-- severity: minor -->
<!-- dimension: plan-hygiene -->

**Context:** After applying F8 (extending §2.1 to cover `sim-api`), the §1.1 "Dependency anomaly" and §1.5 problem #5 still only mentioned `sim-core`.

**Issue:** An agent reading §1.1 would not know about the `sim-api` unused dependency until reaching §2.1.

**Recommendation:** Update §1.1 and §1.5 to mention both `sim-core` and `sim-api`.

**Choices:**
- [x] Update both assessment sections (applied)
- [ ] Leave as-is (§2.1 is self-sufficient)

### F12: §2.9 incorrectly warns about adding `uuid` manually [Applied]
<!-- severity: minor -->
<!-- dimension: correctness -->

**Context:** §2.9 stated "Note: `MakeRequestUuid` requires the `uuid` crate." This implied a manual `uuid` dependency was needed.

**Issue:** The `uuid` crate is a transitive dependency of `tower-http`'s `request-id` feature. No manual `Cargo.toml` entry is needed. The warning could cause an agent to add a redundant dependency.

**Recommendation:** Clarify that `uuid` comes transitively.

**Choices:**
- [x] Clarify that `uuid` is transitive (applied)
- [ ] Remove the note entirely

### Summary

| # | Title | Severity | Dimension | Depends on |
|---|-------|----------|-----------|------------|
| F1 | JSON feature not in default features | critical | correctness | — |
| F2 | §2.6 title/body log-level mismatch | major | plan-hygiene | — |
| F3 | Test files already exist | major | correctness | — |
| F4 | Missing `request-id` feature flag | major | correctness | — |
| F5 | §2.10 line reference off-by-one | minor | correctness | — |
| F6 | §3.6 no current violations | minor | testing | — |
| F7 | §2.5 missing insertion-point guidance | major | correctness | — |
| F8 | `sim-api` unused `tracing-subscriber` dep | minor | gaps | F9 |
| F9 | `with_test_writer()` needs dev-dependency | minor | testing | F8 |
| F10 | §2.5 line numbers inaccurate | major | correctness | F7 |
| F11 | §1.1 / §1.5 inconsistent with §2.1 | minor | plan-hygiene | F8 |
| F12 | §2.9 uuid dependency misleading | minor | correctness | F4 |
