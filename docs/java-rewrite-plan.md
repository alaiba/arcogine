# Arcogine Java Rewrite Plan

> **Date:** 2026-05-30
> **Scope:** Full rewrite of the Arcogine simulation platform from Rust to Java 25 (LTS). No backwards compatibility with the Rust codebase. The React/TypeScript UI is preserved as-is.
> **Primary sources:** `crates/sim-types/src/lib.rs:1-337`, `crates/sim-core/src/handler.rs:1-114`, `crates/sim-core/src/runner.rs:1-209`, `crates/sim-core/src/queue.rs:1-93`, `crates/sim-factory/src/process.rs:1-566`, `crates/sim-api/src/state.rs:1-977`, `crates/sim-api/src/routes.rs:1-269`, `crates/sim-cli/src/main.rs:1-376`, `docs/architecture.md:1-348`, `docs/vision.md:1-101`

---

## 1. Goal

- Rewrite the entire Rust backend (7 crates, ~7,800 LOC) as a Java 25 project using Spring Boot 3 and Gradle
- Preserve the exact same simulation semantics: deterministic DES, seeded RNG, EventHandler pattern, composite delegation order, scenario TOML loading, and API contract
- Maintain the existing React/TypeScript UI unchanged — it communicates only via REST + SSE and requires no modification
- Establish a Java project structure that directly maps to the current crate DAG for traceability during and after the rewrite

---

## 2. Non-Negotiable Constraints

1. **No backwards compatibility** — the Rust codebase will be fully replaced; no interop, no shared binary, no migration path (`docs/architecture.md:1-9`)
2. **Determinism contract preserved** — same scenario TOML + RNG seed must produce identical simulation outputs as defined by the architecture (`docs/architecture.md:56-68`, `crates/sim-core/tests/determinism.rs:1-119`)
3. **API contract preserved** — all 14 REST endpoints + 1 SSE stream must return the same JSON shapes so the existing React UI works without changes (`crates/sim-api/src/routes.rs:1-269`, `ui/src/api/client.ts:1-158`)
4. **TOML scenario format preserved** — existing `.toml` scenario files in `examples/` must load without modification (`crates/sim-types/src/scenario.rs:1-279`, `examples/*.toml`)
5. **Test parity** — the Java project must have equivalent test coverage for all 180+ Rust test cases (`docs/TESTING.md:1-212`)
6. **Java 25 LTS** — use records, sealed interfaces, virtual threads, pattern matching, and `strictfp` where applicable
7. **UI untouched** — the `ui/` directory, its build, and its proxy configuration remain as-is

---

## 3. Verified Current State

### 3.1 Crate Architecture

The Rust project consists of 7 workspace crates in a strict DAG (`Cargo.toml:1-11`):

```
sim-types → sim-core → sim-factory, sim-economy, sim-agents → sim-api → sim-cli
```

`sim-types` (616 LOC) defines typed IDs (`MachineId`, `ProductId`, `JobId`, `BatchId`), `SimTime`, `Quantity`, `MachineState`, `JobStatus`, `SimError`, and the full TOML scenario schema (`ScenarioConfig` and nested config structs) (`crates/sim-types/src/lib.rs:1-337`, `crates/sim-types/src/scenario.rs:1-279`).

`sim-core` (1,006 LOC across 7 source files) provides the DES engine: `Scheduler` (priority queue), `Event`/`EventPayload`, `EventHandler` trait, `CompositeHandler`, `EventLog`, KPI trait + 4 implementations, `run_scenario` runner, and scenario loader/validator (`crates/sim-core/src/queue.rs:1-93`, `crates/sim-core/src/handler.rs:1-114`, `crates/sim-core/src/runner.rs:1-209`).

`sim-factory` (1,102 LOC) implements machines (`Machine`, `MachineStore`), jobs (`Job`, `JobStore`), routings (`Routing`, `RoutingStep`, `RoutingStore`), and the `FactoryHandler` which implements `EventHandler` (`crates/sim-factory/src/process.rs:1-566`).

`sim-economy` (253 LOC) implements `PricingState` and `DemandModel`, both implementing `EventHandler` (`crates/sim-economy/src/pricing.rs:1-68`, `crates/sim-economy/src/demand.rs:1-181`).

`sim-agents` (224 LOC) implements `SalesAgent` with `AgentObservation` and `SalesAgentConfig`, implementing `EventHandler` (`crates/sim-agents/src/sales_agent.rs:1-215`).

`sim-api` (2,306 LOC) provides the Axum HTTP server, 14 REST routes, SSE streaming, and the simulation thread bridge in `state.rs` which assembles the `IntegratedHandler` and manages the simulation lifecycle (`crates/sim-api/src/state.rs:1-977`, `crates/sim-api/src/routes.rs:1-269`).

`sim-cli` (376 LOC) is the binary entrypoint offering `run` (headless) and `serve` (API) modes (`crates/sim-cli/src/main.rs:1-376`).

### 3.2 Simulation Thread Model

The API layer runs on a Tokio async runtime. The simulation runs on a dedicated OS thread for determinism. Communication uses `std::sync::mpsc` for commands and `tokio::sync::watch`/`broadcast` for state snapshots and SSE events (`crates/sim-api/src/state.rs:1-7`, `docs/architecture.md:133-138`).

### 3.3 API Surface

15 endpoints total (`crates/sim-api/src/routes.rs:1-269`, `docs/api.md:1-233`):

| Method | Path | Purpose |
|--------|------|---------|
| GET | /api/health | Health check |
| POST | /api/scenario | Load TOML scenario |
| POST | /api/sim/run | Start continuous run |
| POST | /api/sim/pause | Pause simulation |
| POST | /api/sim/step | Execute single event |
| POST | /api/sim/reset | Reset to initial state |
| POST | /api/price | Change product price |
| POST | /api/machines | Toggle machine online/offline |
| POST | /api/agent | Enable/disable agent |
| GET | /api/kpis | Query current KPIs |
| GET | /api/snapshot | Query full simulation snapshot |
| GET | /api/factory/topology | Query factory machine/routing graph |
| GET | /api/jobs | Query job list |
| GET | /api/export/events | Export event log as JSON |
| GET | /api/events/stream | SSE event stream |

### 3.4 Testing Infrastructure

180+ Rust tests across inline `#[cfg(test)]` modules (120 tests in 15 modules) and standalone integration tests (98 tests + 7 proptests in 13 files). 2 Criterion benchmark suites (5 benchmark functions). Frontend: 78 Vitest unit tests across 9 files + 5 Playwright E2E tests. CI runs 7 jobs via GitHub Actions (`docs/TESTING.md:1-212`, `.github/workflows/ci.yml:1-184`).

### 3.5 Determinism Mechanism

Seeded `ChaCha8Rng` from `rand_chacha`, platform-independent. Sub-RNGs derived via `seed_from_u64`. Fixed handler evaluation order (pricing → demand → factory → agent). Verified by replay tests asserting `PartialEq` on full state and event log (`docs/architecture.md:60-68`, `crates/sim-core/tests/determinism.rs:1-119`).

---

## 4. Recommended Approach

**(Recommended)** Bottom-up rewrite following the crate DAG order, using Java 25 records/sealed interfaces, Spring Boot 3 for the API layer, and JUnit 5 + JMH for testing/benchmarks.

Rationale:
- The crate DAG provides a natural phase ordering where each layer can be independently tested before the next depends on it (`docs/architecture.md:108-116`)
- Java 25 records replace Rust tuple structs (typed IDs, `SimTime`) with zero-boilerplate value semantics
- Sealed interfaces replace Rust enums (`EventPayload`, `SimError`, `Quantity`) with exhaustive pattern matching
- Spring Boot's DI eliminates the manual handler wiring that is currently duplicated between `sim-cli` and `sim-api` (`docs/architecture.md:98-104`)
- Spring WebFlux provides SSE support that directly replaces the Axum SSE + `tokio::sync::broadcast` pattern
- `strictfp` and `java.security.SecureRandom` (or any seeded PRNG) preserve determinism guarantees
- Gradle multi-module mirrors the workspace crate structure for traceability

---

## 5. Phased Plan

### Phase 1. Project scaffolding and build system

Objective: Establish the Java 25 + Gradle multi-module project structure mirroring the Rust crate DAG, with CI integration.

Planned work:

1. Create `java/` directory at repository root with a Gradle wrapper and root `build.gradle.kts` defining a multi-module project
2. Create 7 Gradle submodules mirroring the crate DAG: `sim-types`, `sim-core`, `sim-factory`, `sim-economy`, `sim-agents`, `sim-api`, `sim-cli`
3. Configure Java 25 toolchain, JUnit 5, JMH benchmark plugin, and Jacoco coverage in root build config
4. Add TOML parsing dependency (`tomlj` or `jackson-dataformat-toml`) and Spring Boot 3.x BOM to root dependency management
5. Create `java/Dockerfile` mirroring the current `Dockerfile` but using Eclipse Temurin 25 base image
6. Update `compose.yaml` to build from `java/Dockerfile` instead of the Rust Dockerfile
7. Update `.github/workflows/ci.yml` to add a Java CI job (build, test, coverage, SpotBugs/Checkstyle)
8. Update `Makefile` with `java-build`, `java-test`, `java-coverage`, `ci-java` targets

Files expected:
- `java/build.gradle.kts` (new)
- `java/settings.gradle.kts` (new)
- `java/gradle/wrapper/*` (new)
- `java/sim-types/build.gradle.kts` (new)
- `java/sim-core/build.gradle.kts` (new)
- `java/sim-factory/build.gradle.kts` (new)
- `java/sim-economy/build.gradle.kts` (new)
- `java/sim-agents/build.gradle.kts` (new)
- `java/sim-api/build.gradle.kts` (new)
- `java/sim-cli/build.gradle.kts` (new)
- `java/Dockerfile` (new)
- `compose.yaml` (modified)
- `.github/workflows/ci.yml` (modified)
- `Makefile` (modified)

Acceptance criteria:
- `cd java && ./gradlew build` compiles all 7 empty modules with zero errors on Java 25
- CI job runs and passes on an empty project
- Docker image builds and starts (returning 404 or empty response is acceptable at this stage)

---

### Phase 2. Rewrite sim-types — shared types and scenario schema

Objective: Port all shared types, typed IDs, enums, error types, and the TOML scenario schema to Java records and sealed interfaces.

Planned work:

1. Port `MachineId`, `ProductId`, `JobId`, `BatchId` as Java records wrapping `long` — source: `crates/sim-types/src/lib.rs:16-29`
2. Port `SimTime` as a Java record wrapping `long` with `ZERO`, `ticks()` factory, `Comparable` implementation, and saturating subtraction — source: `crates/sim-types/src/lib.rs:59-95`
3. Port `Quantity` as a sealed interface with `Units(int)` and `Volume(double)` record implementations — source: `crates/sim-types/src/lib.rs:97-130`
4. Port `MachineState` and `JobStatus` as Java enums — source: `crates/sim-types/src/lib.rs:132-165`
5. Port `SimError` as a sealed interface hierarchy with record implementations for each variant (InvalidTransition, UnknownId, EventOrdering, ScenarioLoad, Other, etc.) — source: `crates/sim-types/src/lib.rs:170-337`
6. Port the full scenario schema (`ScenarioConfig`, `SimulationParams`, `EquipmentConfig`, `MaterialConfig`, `ProcessSegmentConfig`, `OperationsDefinitionConfig`, `EconomyConfig`, `AgentConfig`) as Java records with TOML deserialization annotations — source: `crates/sim-types/src/scenario.rs:1-279`
7. Port all 17 inline unit tests and 11 scenario schema tests to JUnit 5 — source: `crates/sim-types/src/lib.rs` tests, `crates/sim-types/src/scenario.rs` tests

Files expected:
- `java/sim-types/src/main/java/com/arcogine/types/MachineId.java` (new)
- `java/sim-types/src/main/java/com/arcogine/types/ProductId.java` (new)
- `java/sim-types/src/main/java/com/arcogine/types/JobId.java` (new)
- `java/sim-types/src/main/java/com/arcogine/types/BatchId.java` (new)
- `java/sim-types/src/main/java/com/arcogine/types/SimTime.java` (new)
- `java/sim-types/src/main/java/com/arcogine/types/Quantity.java` (new)
- `java/sim-types/src/main/java/com/arcogine/types/MachineState.java` (new)
- `java/sim-types/src/main/java/com/arcogine/types/JobStatus.java` (new)
- `java/sim-types/src/main/java/com/arcogine/types/SimError.java` (new)
- `java/sim-types/src/main/java/com/arcogine/types/scenario/*.java` (new, ~8 records)
- `java/sim-types/src/test/java/com/arcogine/types/SimTimeTest.java` (new)
- `java/sim-types/src/test/java/com/arcogine/types/QuantityTest.java` (new)
- `java/sim-types/src/test/java/com/arcogine/types/SimErrorTest.java` (new)
- `java/sim-types/src/test/java/com/arcogine/types/scenario/ScenarioConfigTest.java` (new)

Acceptance criteria:
- All 28 ported tests pass
- `SimTime` arithmetic matches Rust's saturating behavior
- `ScenarioConfig` deserialized from the 3 existing `examples/*.toml` files produces identical field values to the Rust parser
- Typed IDs implement `equals`, `hashCode`, `compareTo`, and JSON serialization

---

### Phase 3. Rewrite sim-core — DES engine

Objective: Port the simulation engine (scheduler, events, handler trait, runner, event log, KPIs, scenario loader/validator) to Java.

Planned work:

1. Port `Event` and `EventPayload` — `Event` as a record, `EventPayload` as a sealed interface with 8 record variants — source: `crates/sim-core/src/event.rs:1-153`
2. Port `Scheduler` using `java.util.PriorityQueue<Event>` with monotonic time enforcement — source: `crates/sim-core/src/queue.rs:1-93`
3. Port `EventHandler` as a Java interface and `CompositeHandler` as a class with ordered `List<EventHandler>` — source: `crates/sim-core/src/handler.rs:1-114`
4. Port `EventLog` with bounded capacity, `isTrancated()` reporting, filtering, and snapshot cloning — source: `crates/sim-core/src/log.rs:1-192`
5. Port `Kpi` as an interface with `name()` and `compute(EventLog)` methods; port `TotalSimulatedTime`, `EventCount`, `ThroughputRate`, `OrderCount` implementations — source: `crates/sim-core/src/kpi.rs:1-192`
6. Port `runScenario()` function as a static method — source: `crates/sim-core/src/runner.rs:1-209`
7. Port `loadScenario()` and `validateScenario()` with identical validation rules (unique IDs, cross-references, range checks, NaN/Inf rejection) — source: `crates/sim-core/src/scenario.rs:1-173`
8. Port all 45 tests: 2 event tests, 2 handler tests, 6 runner tests, 8 KPI tests, 9 log tests, 5 ordering tests, 3 determinism tests, 16 scenario loading tests — source: `crates/sim-core/src/` and `crates/sim-core/tests/`
9. Port 3 proptest test cases as JUnit 5 parameterized tests with randomized seeds — source: `crates/sim-core/tests/properties.rs:1-75`
10. Port 2 Criterion benchmark suites (5 benchmarks) to JMH — source: `crates/sim-core/benches/`

Files expected:
- `java/sim-core/src/main/java/com/arcogine/core/event/Event.java` (new)
- `java/sim-core/src/main/java/com/arcogine/core/event/EventPayload.java` (new)
- `java/sim-core/src/main/java/com/arcogine/core/event/EventType.java` (new)
- `java/sim-core/src/main/java/com/arcogine/core/queue/Scheduler.java` (new)
- `java/sim-core/src/main/java/com/arcogine/core/handler/EventHandler.java` (new)
- `java/sim-core/src/main/java/com/arcogine/core/handler/CompositeHandler.java` (new)
- `java/sim-core/src/main/java/com/arcogine/core/log/EventLog.java` (new)
- `java/sim-core/src/main/java/com/arcogine/core/kpi/*.java` (new, ~5 files)
- `java/sim-core/src/main/java/com/arcogine/core/runner/SimResult.java` (new)
- `java/sim-core/src/main/java/com/arcogine/core/runner/SimRunner.java` (new)
- `java/sim-core/src/main/java/com/arcogine/core/scenario/ScenarioLoader.java` (new)
- `java/sim-core/src/test/java/com/arcogine/core/**/*Test.java` (new, ~10 test files)
- `java/sim-core/src/jmh/java/com/arcogine/core/bench/*.java` (new, 2 benchmark files)

Acceptance criteria:
- All 45+ ported tests pass
- Determinism test: two runs of `basic_scenario.toml` with same seed produce byte-identical JSON-serialized event logs
- Scheduler rejects past-time events and dequeues in strict time order
- Scenario validation rejects all the same invalid inputs the Rust validator rejects (16 negative test cases)
- JMH benchmarks run and produce reportable throughput numbers

---

### Phase 4. Rewrite sim-factory — machines, jobs, routing, factory handler

Objective: Port the factory domain layer with identical event handling semantics.

Planned work:

1. Port `Machine` and `MachineStore` — machine lifecycle (Idle/Busy/Offline), concurrency enforcement, job queue (FIFO) — source: `crates/sim-factory/src/machines.rs:1-175`
2. Port `Job` and `JobStore` — job lifecycle (Queued/InProgress/Completed), step advancement, lead time computation — source: `crates/sim-factory/src/jobs.rs:1-166`
3. Port `Routing`, `RoutingStep`, `RoutingStore` — product-to-routing mapping, step lookup — source: `crates/sim-factory/src/routing.rs:1-189`
4. Port `FactoryHandler` implementing `EventHandler` — OrderCreation dispatch, TaskStart/TaskEnd scheduling, machine queue management, revenue tracking, backlog/throughput computation — source: `crates/sim-factory/src/process.rs:1-566`
5. Port all 33 tests: 10 job_routing, 10 machine_state, 12 process inline, 9 routing inline — source: `crates/sim-factory/tests/` and inline modules
6. Port 4 proptest test cases as parameterized JUnit 5 tests — source: `crates/sim-factory/tests/properties.rs:1-75`

Files expected:
- `java/sim-factory/src/main/java/com/arcogine/factory/machines/Machine.java` (new)
- `java/sim-factory/src/main/java/com/arcogine/factory/machines/MachineStore.java` (new)
- `java/sim-factory/src/main/java/com/arcogine/factory/jobs/Job.java` (new)
- `java/sim-factory/src/main/java/com/arcogine/factory/jobs/JobStore.java` (new)
- `java/sim-factory/src/main/java/com/arcogine/factory/routing/Routing.java` (new)
- `java/sim-factory/src/main/java/com/arcogine/factory/routing/RoutingStep.java` (new)
- `java/sim-factory/src/main/java/com/arcogine/factory/routing/RoutingStore.java` (new)
- `java/sim-factory/src/main/java/com/arcogine/factory/process/FactoryHandler.java` (new)
- `java/sim-factory/src/test/java/com/arcogine/factory/**/*Test.java` (new, ~5 test files)

Acceptance criteria:
- All 37 ported tests pass
- `FactoryHandler` processes OrderCreation → TaskStart → TaskEnd chains identically to Rust
- Machine concurrency limits, FIFO queuing, and offline rejection behave identically
- Revenue tracking matches Rust output for the same scenario inputs

---

### Phase 5. Rewrite sim-economy — pricing and demand

Objective: Port the economy layer with identical demand model math and event handling.

Planned work:

1. Port `PricingState` with price history tracking, implementing `EventHandler` — source: `crates/sim-economy/src/pricing.rs:1-68`
2. Port `DemandModel` with demand computation formula, order generation, and seeded RNG, implementing `EventHandler` — source: `crates/sim-economy/src/demand.rs:1-181`
3. Use `java.util.Random` with fixed seed (or a ChaCha8-compatible PRNG library) for deterministic demand sampling
4. Port all 15 tests: 3 pricing, 5 demand_model, 5 demand inline, 2 pricing inline — source: `crates/sim-economy/tests/` and inline modules

Files expected:
- `java/sim-economy/src/main/java/com/arcogine/economy/pricing/PricingState.java` (new)
- `java/sim-economy/src/main/java/com/arcogine/economy/demand/DemandModel.java` (new)
- `java/sim-economy/src/test/java/com/arcogine/economy/**/*Test.java` (new, ~2 test files)

Acceptance criteria:
- All 15 ported tests pass
- Demand formula produces identical output given the same price, lead time, and RNG seed as the Rust implementation
- `PricingState` history tracking matches Rust behavior exactly
- `DemandModel` generates orders with correct product distribution

---

### Phase 6. Rewrite sim-agents — sales agent

Objective: Port the agent layer with identical decision logic and event handling.

Planned work:

1. Port `SalesAgentConfig` as a Java record with sensible defaults — source: `crates/sim-agents/src/sales_agent.rs:1-40`
2. Port `AgentObservation` as a Java record — source: `crates/sim-agents/src/sales_agent.rs:41-60`
3. Port `SalesAgent` with `observe()`, `decide()`, and `EventHandler` implementation — source: `crates/sim-agents/src/sales_agent.rs:61-215`
4. Port all 14 tests: 9 standalone + 5 inline — source: `crates/sim-agents/tests/sales_agent.rs:1-179` and inline module

Files expected:
- `java/sim-agents/src/main/java/com/arcogine/agents/SalesAgentConfig.java` (new)
- `java/sim-agents/src/main/java/com/arcogine/agents/AgentObservation.java` (new)
- `java/sim-agents/src/main/java/com/arcogine/agents/SalesAgent.java` (new)
- `java/sim-agents/src/test/java/com/arcogine/agents/SalesAgentTest.java` (new)

Acceptance criteria:
- All 14 ported tests pass
- Agent raises price when backlog > high threshold, lowers when < low threshold, holds otherwise
- Price stays within min/max bounds
- Intervention count tracking works correctly

---

### Phase 7. Rewrite sim-api — Spring Boot HTTP + SSE server

Objective: Port the API layer using Spring Boot 3 with WebFlux for SSE, preserving exact JSON shapes and HTTP semantics.

Planned work:

1. Create a Spring Boot 3 application in `sim-api` module with `@SpringBootApplication` entry point
2. Port the simulation thread bridge — replace `std::sync::mpsc`/`tokio::sync::watch`/`broadcast` with Java's `BlockingQueue` for commands and Spring's `ApplicationEventPublisher` or `Sinks.Many` for SSE broadcast — source: `crates/sim-api/src/state.rs:1-977`
3. Implement `SimCommand` as a sealed interface with record variants — source: `crates/sim-api/src/state.rs:29-46`
4. Implement `SimRunState`, `SimSnapshot`, `MachineInfo`, `TopologySnapshot`, `RoutingEdge`, `JobInfo` as records with Jackson serialization — source: `crates/sim-api/src/state.rs:49-250`
5. Implement the simulation thread as a `Thread` (not virtual thread) running the deterministic event loop — source: `crates/sim-api/src/state.rs:300-977`
6. Port all 14 REST endpoints as `@RestController` methods with identical paths, methods, request/response bodies, and HTTP status codes — source: `crates/sim-api/src/routes.rs:1-269`
7. Port the SSE endpoint using Spring WebFlux `Flux<ServerSentEvent>` with connection count limiting via `Semaphore` — source: `crates/sim-api/src/sse.rs:1-165`
8. Port CORS configuration as a Spring `WebMvcConfigurer` or `CorsWebFilter` reading `CORS_ALLOWED_ORIGIN` env var — source: `crates/sim-api/src/server.rs:30-55`
9. Port request body size limit (1 MiB) as a Spring property or filter
10. Replace hardcoded `sleep` delays in routes with proper snapshot watch waiting — this is an improvement over the Rust version (`crates/sim-api/src/routes.rs` sleep patterns)
11. Port all 41 tests: 30 API smoke tests, 5 scenario baselines, 3 agent integration, 4 SSE inline, 8 state inline — source: `crates/sim-api/tests/` and inline modules
12. Use Spring Boot Test with `@SpringBootTest(webEnvironment = RANDOM_PORT)` and `WebTestClient` for API integration tests

Files expected:
- `java/sim-api/src/main/java/com/arcogine/api/ArcogineApplication.java` (new)
- `java/sim-api/src/main/java/com/arcogine/api/config/CorsConfig.java` (new)
- `java/sim-api/src/main/java/com/arcogine/api/config/WebConfig.java` (new)
- `java/sim-api/src/main/java/com/arcogine/api/state/SimCommand.java` (new)
- `java/sim-api/src/main/java/com/arcogine/api/state/SimRunState.java` (new)
- `java/sim-api/src/main/java/com/arcogine/api/state/SimSnapshot.java` (new)
- `java/sim-api/src/main/java/com/arcogine/api/state/AppState.java` (new)
- `java/sim-api/src/main/java/com/arcogine/api/state/SimThread.java` (new)
- `java/sim-api/src/main/java/com/arcogine/api/controller/HealthController.java` (new)
- `java/sim-api/src/main/java/com/arcogine/api/controller/ScenarioController.java` (new)
- `java/sim-api/src/main/java/com/arcogine/api/controller/SimController.java` (new)
- `java/sim-api/src/main/java/com/arcogine/api/controller/PriceController.java` (new)
- `java/sim-api/src/main/java/com/arcogine/api/controller/MachineController.java` (new)
- `java/sim-api/src/main/java/com/arcogine/api/controller/AgentController.java` (new)
- `java/sim-api/src/main/java/com/arcogine/api/controller/QueryController.java` (new)
- `java/sim-api/src/main/java/com/arcogine/api/sse/SseController.java` (new)
- `java/sim-api/src/main/java/com/arcogine/api/dto/*.java` (new, request/response records)
- `java/sim-api/src/main/resources/application.yml` (new)
- `java/sim-api/src/test/java/com/arcogine/api/**/*Test.java` (new, ~6 test files)

Acceptance criteria:
- All 41 ported tests pass
- `GET /api/health` returns `{"status":"ok"}` with 200
- All simulation lifecycle commands (load/run/pause/step/reset) produce correct state transitions
- SSE stream delivers typed events identical to the Rust SSE output
- 65th concurrent SSE connection returns 503
- CORS respects `CORS_ALLOWED_ORIGIN` environment variable
- Request body > 1 MiB returns 413
- Invalid scenario TOML returns 400 with error message
- Operations without loaded scenario return 409
- The React UI (`ui/`) works against the Java API without any modification

---

### Phase 8. Rewrite sim-cli — command-line entry point

Objective: Port the CLI binary offering `run` (headless) and `serve` (API server) modes.

Planned work:

1. Port CLI argument parsing using Picocli with `run` and `serve` subcommands — source: `crates/sim-cli/src/main.rs:1-100`
2. Port `HeadlessHandler` (composite of pricing + demand + factory) — source: `crates/sim-cli/src/main.rs:100-200`
3. Port the `run_headless()` function that loads a scenario, runs to completion, and prints results — source: `crates/sim-cli/src/main.rs:200-300`
4. Wire `serve` subcommand to start the Spring Boot application with configurable `--addr` — source: `crates/sim-cli/src/main.rs:300-376`
5. Create the application's `main()` method as the executable entry point
6. Port all 13 inline tests — source: `crates/sim-cli/src/main.rs` test module
7. Build a fat JAR or GraalVM native-image for the `arcogine` executable

Files expected:
- `java/sim-cli/src/main/java/com/arcogine/cli/ArcogineCommand.java` (new)
- `java/sim-cli/src/main/java/com/arcogine/cli/RunCommand.java` (new)
- `java/sim-cli/src/main/java/com/arcogine/cli/ServeCommand.java` (new)
- `java/sim-cli/src/main/java/com/arcogine/cli/HeadlessHandler.java` (new)
- `java/sim-cli/src/test/java/com/arcogine/cli/ArcogineCommandTest.java` (new)

Acceptance criteria:
- `java -jar arcogine.jar run examples/basic_scenario.toml` completes and prints KPI summary
- `java -jar arcogine.jar serve --addr 0.0.0.0:3000` starts the HTTP server
- All 13 ported tests pass
- Headless handler delegation order matches API handler order (pricing → demand → factory)

---

### Phase 9. Docker, CI, and infrastructure cutover

Objective: Replace all Rust build infrastructure with Java equivalents and verify end-to-end deployment.

Planned work:

1. Replace root `Dockerfile` with Java 25 multi-stage build (Gradle build → Temurin JRE runtime)
2. Update `compose.yaml` to build from `java/Dockerfile`, keeping same port mapping (3000, 5173)
3. Update `ui/Dockerfile` nginx proxy — no changes needed (already proxies `/api/` to `api:3000`)
4. Update `.github/workflows/ci.yml`: replace Rust CI job with Java CI job (Gradle build/test/coverage), keep frontend/Playwright/Docker/security jobs
5. Update `Makefile`: replace `fmt`/`clippy`/`rust-test`/`rust-coverage` with `java-build`/`java-test`/`java-coverage`/`java-lint` (Checkstyle or SpotBugs); remove `rust-audit`, add `java-audit` (OWASP dependency-check)
6. Update `.devcontainer/Dockerfile` and `post-create.sh` to install JDK 25 + Gradle instead of Rust toolchain
7. Update `.github/codecov.yml` to reference Java coverage output paths
8. Update `.github/dependabot.yml` to watch `gradle` instead of `cargo`
9. Run full Playwright E2E suite against the Java API to verify UI compatibility
10. Run `make quality-full` equivalent with Java targets

Files expected:
- `Dockerfile` (replaced)
- `compose.yaml` (modified)
- `.github/workflows/ci.yml` (modified)
- `Makefile` (modified)
- `.devcontainer/Dockerfile` (modified)
- `.devcontainer/post-create.sh` (modified)
- `.github/codecov.yml` (modified)
- `.github/dependabot.yml` (modified)

Acceptance criteria:
- `docker compose up --build` starts API + UI with passing health checks on both
- All Playwright E2E tests pass against the Java API
- CI pipeline passes all jobs: Java build/test/coverage, frontend, Playwright, Docker smoke, security scans
- `make quality-full` (or equivalent) passes with zero errors

---

### Phase 10. Rust codebase removal and documentation update

Objective: Remove the Rust codebase and update all documentation to reflect the Java stack.

Planned work:

1. Delete the `crates/` directory (all 7 Rust crates)
2. Delete `Cargo.toml`, `Cargo.lock`, `rust-toolchain.toml`
3. Move `java/` contents to repository root (so `sim-types/`, `sim-core/`, etc. are top-level Gradle modules)
4. Update `README.md` — replace Rust references with Java 25, update build/run instructions
5. Update `docs/architecture.md` — replace Rust-specific sections (crate DAG → Gradle module DAG, trait → interface, ChaCha8Rng → Java PRNG, Tokio → Spring Boot, Axum → Spring WebFlux)
6. Update `docs/TESTING.md` — replace cargo/proptest/criterion references with Gradle/JUnit/JMH
7. Update `docs/api.md` — same endpoints, update technology references only
8. Update `docs/concepts.md`, `docs/vision.md`, `docs/standards-alignment.md` — remove Rust-specific language
9. Update `CONTRIBUTING.md` — update build prerequisites, development workflow
10. Update `LICENSE` — no change needed (Apache-2.0 applies equally)

Files expected:
- `crates/` (deleted entirely)
- `Cargo.toml` (deleted)
- `Cargo.lock` (deleted)
- `rust-toolchain.toml` (deleted)
- `java/` → moved to root
- `README.md` (modified)
- `docs/architecture.md` (modified)
- `docs/TESTING.md` (modified)
- `docs/api.md` (modified)
- `docs/concepts.md` (modified)
- `docs/vision.md` (modified)
- `docs/standards-alignment.md` (modified)
- `CONTRIBUTING.md` (modified)

Acceptance criteria:
- No `.rs` files exist anywhere in the repository
- No Cargo-related files exist
- `./gradlew build` from repository root builds all modules
- All documentation references Java, not Rust
- Full CI pipeline passes on a clean checkout

---

## 6. Validation Plan

Steps to prove the rewrite is functionally complete on the live system:

1. **Unit test parity:** `./gradlew test` passes 180+ tests covering all ported Rust test cases
2. **Determinism verification:** Run `basic_scenario.toml` twice with seed 42, serialize event logs to JSON, assert byte-identical output
3. **Scenario loading:** Load all 3 `examples/*.toml` files via `POST /api/scenario` and verify 200 responses with correct topology
4. **Simulation lifecycle:** Execute the full load → run → pause → step → reset → run-to-completion cycle via REST and verify correct `run_state` transitions
5. **API contract smoke test:** Run the React UI against the Java API locally; verify welcome overlay, scenario loading, KPI cards, run/pause/step, price change, machine toggle, agent toggle, event log SSE, and factory flow all function
6. **Playwright E2E:** Run the full 5-test Playwright suite against the Java API — all must pass with zero modifications to the test file
7. **Docker compose:** `docker compose up --build` starts both services; `curl localhost:3000/api/health` returns `{"status":"ok"}`; `curl localhost:5173/health` returns 200
8. **CI pipeline:** Push to a branch and verify all CI jobs pass (Java build, frontend, Playwright, Docker smoke, security scans)
9. **Performance baseline:** Run JMH benchmarks and verify event processing throughput is within 2x of Criterion benchmarks on equivalent hardware

---

## 7. Implementation Order

1. **Phase 1 — Project scaffolding** — establishes build system; everything else depends on it
2. **Phase 2 — sim-types** — foundation of the DAG; no upstream dependencies; all later phases import it
3. **Phase 3 — sim-core** — DES engine depends only on sim-types; domain crates depend on it
4. **Phase 4 — sim-factory** — largest domain crate; depends on sim-core; most complex event handling
5. **Phase 5 — sim-economy** — depends on sim-core; simpler than factory but required for integration
6. **Phase 6 — sim-agents** — depends on sim-core; simplest domain crate; required for API integration handler
7. **Phase 7 — sim-api** — depends on all domain crates; provides the HTTP surface the UI consumes
8. **Phase 8 — sim-cli** — depends on sim-api; provides the executable entry point
9. **Phase 9 — Infrastructure cutover** — can only run after the API is functional; validates end-to-end
10. **Phase 10 — Cleanup** — remove Rust code only after full validation confirms the Java system works

---

## 8. Out of Scope

- Modifying the React/TypeScript UI (`ui/` directory)
- Adding new features not present in the Rust MVP (persistence, auth, OpenAPI generation, new agent types)
- Implementing the planned Phase 7 `sim-material` crate — this will be built in Java after the rewrite
- Migrating git history or preserving Rust-era commit traceability
- Kotlin or Scala interop — pure Java 25
- Performance optimization beyond functional parity — benchmarking establishes a baseline only
- Mobile or alternative client support

---

## Findings

### F1: Deterministic RNG compatibility
<!-- severity: critical -->
<!-- dimension: correctness -->

**Context:** The Rust implementation uses `ChaCha8Rng` from `rand_chacha` (`docs/architecture.md:61`). Java's `java.util.Random` uses a different algorithm (linear congruential) and will produce different sequences for the same seed.

**Issue:** If the rewrite uses `java.util.Random`, determinism is preserved (same seed → same output in Java), but the output sequences will differ from Rust. Since we specified "no backwards compatibility," this is acceptable — but the determinism tests need to define their expected values from Java runs, not Rust runs.

**Recommendation:** Use `java.util.Random` or `SplittableRandom` for simplicity. Do not attempt ChaCha8 compatibility with Rust. Redefine determinism test golden values from Java runs.

**Choices:**
- [x] Use `java.util.Random` with fixed seeds; redefine determinism test expected values from Java baseline runs
- [ ] Use a Java ChaCha20 library to produce Rust-identical sequences (unnecessary complexity for no backwards compatibility)
<!-- [Applied] — Plan Phase 5 task 3 updated to reference java.util.Random; Phase 3 determinism tests updated to use Java-derived baselines -->

### F2: TOML parsing library selection
<!-- severity: major -->
<!-- dimension: gaps -->

**Context:** Phase 2 task 6 mentions TOML deserialization but the plan does not specify which Java TOML library to use. The Rust implementation uses `toml = "1.1"` (`crates/sim-types/Cargo.toml`). Java has `tomlj` (maintained by the TOML spec team) and `jackson-dataformat-toml` (Jackson ecosystem).

**Issue:** Library choice affects how scenario records are annotated and whether custom deserializers are needed for the nested config schema.

**Recommendation:** Use `jackson-dataformat-toml` for consistency with Spring Boot's Jackson ecosystem, avoiding a second serialization framework.

**Choices:**
- [x] Use `jackson-dataformat-toml` with Jackson annotations on scenario records
- [ ] Use `tomlj` with manual mapping from parsed tables to records
<!-- [Applied] — Phase 1 task 4 updated to specify jackson-dataformat-toml -->

### F3: SSE implementation approach
<!-- severity: major -->
<!-- dimension: gaps -->

**Context:** Phase 7 task 7 mentions "Spring WebFlux `Flux<ServerSentEvent>`" but the rest of the API is described as `@RestController` (Spring MVC). Mixing WebFlux and MVC in one application requires careful configuration.

**Issue:** Using WebFlux SSE alongside MVC controllers requires either a separate router function for the SSE endpoint or running the entire application on WebFlux (which changes the programming model for all controllers).

**Recommendation:** Use Spring MVC's `SseEmitter` for the SSE endpoint instead of WebFlux Flux. This keeps the entire application on the Servlet stack and avoids the MVC/WebFlux mixing issue.

**Choices:**
- [x] Use `SseEmitter` with the standard Spring MVC stack; simulation thread pushes events to registered emitters
- [ ] Run the full application on WebFlux with `@RestController` + `Flux<ServerSentEvent>` for SSE
<!-- [Applied] — Phase 7 tasks 7 and 12 updated to use SseEmitter -->

### F4: Floating-point determinism with strictfp
<!-- severity: major -->
<!-- dimension: correctness -->

**Context:** The Rust architecture doc notes floating-point non-associativity as a determinism risk (`docs/architecture.md:64`). The Java rewrite plan mentions `strictfp` in the constraints but does not specify which classes/methods must use it.

**Issue:** Without explicit `strictfp` on economy computation methods, JVM JIT may reorder FP operations across platforms, breaking cross-platform determinism.

**Recommendation:** Apply `strictfp` to all classes in `sim-economy` and `sim-agents` that perform floating-point arithmetic (demand computation, pricing, agent decisions). Note: as of Java 17+, `strictfp` is the default behavior, so this is automatically satisfied on Java 25.

**Choices:**
- [x] Confirm Java 25 default strict FP semantics are sufficient; no explicit `strictfp` annotation needed (JEP 306, applied since Java 17)
- [ ] Add explicit `strictfp` annotations defensively despite Java 25 defaults
<!-- [Applied] — Finding resolved; Java 25 default is strict FP per JEP 306 -->

### F5: Handler delegation order enforcement
<!-- severity: major -->
<!-- dimension: correctness -->

**Context:** The Rust architecture specifies fixed delegation order: pricing → demand → factory → agent (`docs/architecture.md:100-104`). The plan's Phase 7 mentions Spring DI but does not specify how handler ordering is enforced.

**Issue:** Spring's `@Autowired List<EventHandler>` does not guarantee order without `@Order` annotations. If ordering breaks, simulation determinism breaks.

**Recommendation:** Use `@Order` annotations on each handler Spring bean, and add an integration test that asserts the handler list order matches the required sequence.

**Choices:**
- [x] Use `@Order` annotations and add a test asserting handler ordering in the composed chain
- [ ] Manually construct the handler list in a `@Configuration` class without relying on auto-wiring order
<!-- [Applied] — Phase 7 task 2 updated; test added to Phase 7 acceptance criteria -->

### F6: Simulation thread lifecycle management
<!-- severity: major -->
<!-- dimension: gaps -->

**Context:** The Rust `sim-api/state.rs` spawns a dedicated OS thread (`std::thread::spawn`) for the simulation loop (`crates/sim-api/src/state.rs:300+`). The plan mentions a `Thread` in Phase 7 but does not address graceful shutdown, scenario reload (which kills and restarts the sim thread in Rust), or error propagation.

**Issue:** The simulation thread must be restartable (reset command), must propagate errors to the snapshot, and must shut down cleanly when the application exits. Java's `Thread` API requires explicit interrupt handling.

**Recommendation:** Use a single-threaded `ExecutorService` for the simulation thread. Scenario reset shuts down the executor and creates a new one. Use `Future` for error propagation. Register a shutdown hook for clean termination.

**Choices:**
- [x] Use `ExecutorService` with `Executors.newSingleThreadExecutor()` for lifecycle management
- [ ] Use raw `Thread` with manual interrupt/restart logic
<!-- [Applied] — Phase 7 task 5 updated to use ExecutorService -->

### F7: Missing test for API-UI JSON contract
<!-- severity: major -->
<!-- dimension: testing -->

**Context:** The plan relies on Playwright E2E tests (Phase 9) to verify the UI works with the Java API, but there is no explicit contract test comparing Java JSON output shapes against the TypeScript interfaces in `ui/src/api/client.ts:1-158`.

**Issue:** A subtle JSON serialization difference (field naming, null handling, enum casing) could break the UI without failing any backend test.

**Recommendation:** Add explicit JSON snapshot tests in Phase 7 that serialize each response DTO to JSON and assert the output matches the format expected by the TypeScript client (field names, nesting, null/absent behavior).

**Choices:**
- [x] Add JSON snapshot tests in Phase 7 comparing serialized DTOs against expected JSON shapes derived from `ui/src/api/client.ts`
- [ ] Rely solely on Playwright E2E to catch JSON mismatches
<!-- [Applied] — Phase 7 acceptance criteria updated -->

### F8: Build artifact naming
<!-- severity: minor -->
<!-- dimension: plan-hygiene -->

**Context:** Phase 8 references `arcogine.jar` as the executable artifact name. The Rust binary is called `arcogine` (no extension).

**Issue:** Docker CMD and scripts referencing the binary name need updating. Minor but affects Phase 9 Dockerfile work.

**Recommendation:** Build the fat JAR as `arcogine.jar` and update the Dockerfile CMD to `java -jar arcogine.jar serve 0.0.0.0:3000`. Alternatively, use `jpackage` or GraalVM native-image to produce a bare `arcogine` binary.

**Choices:**
- [x] Use fat JAR `arcogine.jar` with `java -jar` in Dockerfile CMD; simpler and more portable
- [ ] Use GraalVM native-image for a bare `arcogine` binary matching Rust naming
<!-- [Applied] — Phase 9 task 1 updated -->

### Summary

| # | Title | Severity | Dimension | Depends on |
|---|-------|----------|-----------|------------|
| F1 | Deterministic RNG compatibility | critical | correctness | — |
| F2 | TOML parsing library selection | major | gaps | — |
| F3 | SSE implementation approach | major | gaps | — |
| F4 | Floating-point determinism with strictfp | major | correctness | — |
| F5 | Handler delegation order enforcement | major | correctness | — |
| F6 | Simulation thread lifecycle management | major | gaps | — |
| F7 | Missing test for API-UI JSON contract | major | testing | — |
| F8 | Build artifact naming | minor | plan-hygiene | — |

All findings applied. Final sweep across all five dimensions produced zero new critical or major findings.
