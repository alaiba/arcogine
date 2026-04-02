# Arcogine — Archived Implementation Roadmap (Completed)

> **Status:** Completed and archived from the original plan document.
> **Scope:** Single-user, locally runnable factory + economy simulation platform in Rust.
> **Date:** 2026-04-02

This file preserves the complete planning and execution intent from the original MVP plan after implementation and validation.

## 1. Project goal and constraints

### 1.1 Core goal

- Prove that a closed-loop factory and economy simulation can run deterministically in Rust with reproducible outcomes.
- Define a minimal but durable architecture, testing strategy, local deployment path, and experiment UI.
- Keep the implementation publishable and contributor-friendly on GitHub.
- Preserve long-term directions (digital-twin, serious-game, multi-agent, MMO adjacency) without overbuilding the MVP.

### 1.2 Non-negotiable constraints

1. Core simulation is written in Rust.
2. Headless simulation core is primary; UI/API are additive.
3. MVP must tie factory flow to economy loop.
4. Repository must be reproducible, modular, testable, and collaboration ready.
5. UI is a single-user experiment console, not a game client.
6. Support native and containerized local execution.
7. Deterministic acceptance tests and scenario-level validation are mandatory.
8. Agents only use approved command interfaces and never mutate simulation state directly.

### 1.3 Implementation rule

Each phase must leave prior phase acceptance checks conceptually true before proceeding.

## 2. Starting state and baseline assumptions

- `LICENSE` and a first-draft roadmap existed in `devel/`.
- Authoritative project identity and architecture references already existed in `docs/vision.md` and `docs/architecture-overview.md`.
- No workspace, source code, tests, CI, Docker assets, `.gitignore`, or scenario files existed in the codebase before Phase 1.

## 3. Architectural decisions and conventions

### 3.1 Core simulation model

- Discrete-event simulation (DES) with explicit event types and monotonic scheduling.
- Determinism contract with fixed scenario seed and deterministic PRNG.
- Append-only event log with query operations (`append`, `iter`, `filter_by_type`, `count`, `snapshot`) for replay and inspection.
- State transitions use typed IDs (`MachineId`, `ProductId`, `JobId`, `BatchId`) and structured state types in shared crate.

### 3.2 Determinism contract

- PRNG: `rand` + `rand_chacha` (`ChaCha8Rng`).
- Seed stored in scenario config and propagated through shared root RNG state.
- Reproducible results are validated by identical final state, identical KPI summaries, and identical event streams for repeated runs.
- `Eq`/`PartialEq`, `Debug`, and serialization derives on comparable state types where meaningful.

### 3.3 Event dispatch and crate layering

- `sim-core` owns scheduler, runner, event log, and shared types only.
- Domain crates implement `EventHandler` and expose `handle_event`.
- Runner composes domain handlers and invokes them for each event.
- This preserves acyclic dependencies:
  - `sim-types` has no upstream dependencies.
  - `sim-core` depends on `sim-types`.
  - `sim-factory`, `sim-economy`, `sim-agents` depend on `sim-core` + `sim-types`.
  - `sim-api` depends on all domain crates and shared modules.
  - `sim-cli` depends on `sim-api`.

### 3.4 IO contracts and runtime boundaries

- Scenario files are TOML.
- Scenario schema structs are in `sim-types`.
- Loader/validation logic is in `sim-core` and returns structured `SimError`.
- Simulation command and query path is synchronous and deterministic inside `sim-cli`/`sim-api` runners.
- HTTP API and UI run in separate process layers and interact via commands/events, not direct state mutation.

### 3.5 Concurrency model

- API layer runs on Tokio async runtime.
- Simulation engine runs on a deterministic synchronous execution path.
- API uses bounded command channels and broadcast event channels to communicate without sharing mutable simulation state across threads.

### 3.6 Stack convention

- Rust crates: `axum`, `tokio`, `serde`, `toml`, `rand_chacha`, `clap`.
- UI stack: React + TypeScript (latest stable), Vite, Tailwind CSS v4, Zustand, Recharts.
- Testing: Rust unit/integration/property tests and Playwright UI smoke tests.
- Deployment: Docker + Docker Compose + benchmark scaffolding.

## 4. Repository structure (as built)

- `crates/sim-types`
- `crates/sim-core`
- `crates/sim-factory`
- `crates/sim-economy`
- `crates/sim-agents`
- `crates/sim-api`
- `crates/sim-cli`
- `ui/`
- `examples/`
- `docs/`
- `tests`, `benches`, `.github/workflows`, `TESTING.md`, Docker artifacts

## 5. Phased execution record

### Phase 1 — Repository foundation (completed)

**Objective:** establish a reproducible workspace, baseline docs, and contributor workflow.

**Completed core outcomes**

- Created Cargo workspace with `resolver = "2"` and crate-level layout.
- Added stable toolchain pinning via `rust-toolchain.toml`.
- Added core crate manifests and source entry points.
- Added repository health docs: `README.md`, `CONTRIBUTING.md`, `SECURITY.md`, `CODE_OF_CONDUCT.md`, `.gitignore`.
- Added baseline CI with `cargo fmt --check`, `cargo clippy`, `cargo test`.
- Added placeholder docs and examples assets (`docs/README.md`, `examples/README.md`).

### Phase 2 — Deterministic kernel (completed)

**Objective:** produce a minimal, typed, deterministic simulation kernel with deterministic replay support.

**Completed core outcomes**

- Added typed IDs, simulation time, shared error model (`SimError`), and scenario config structs.
- Added event type system with demand and agent evaluation signals.
- Added priority-queue scheduler and runner API (`run_scenario`).
- Added event log internals and base KPI trait.
- Added TOML schema + loader with validation.
- Added SoA-style storage for machine/product/job/routing state.
- Added unit tests for determinism, event ordering, scenario loading error-paths, and invariants.

### Phase 3 — Factory flow and economy loop (completed)

**Objective:** validate demand-capacity-price-feedback behavior across scenario runs.

**Completed core outcomes**

- Implemented demand model triggered by `DemandEvaluation`.
- Implemented pricing state and command model.
- Extended KPI set with throughput and order accounting.
- Added scenario fixtures and scenario-level integration checks.
- Added unit tests for demand and pricing behavior.

### Phase 4 — Command surface and simple agent (completed)

**Objective:** expose explicit control surface and run closed-loop behavior from outside the core.

**Completed core outcomes**

- Added Axum/Tokio API with command/query routes, simulation state routes, and event stream endpoint.
- Added CLI support for `run` and `serve` modes with common binary `arcogine`.
- Implemented `SalesAgent` under the same command interface pattern as API commands.
- Added topology/jobs/export endpoints, health check endpoint, OpenAPI generation dependencies.
- Added integration tests for API smoke cases and agent behavior.

### Phase 5 — Single-user experimentation UI (completed)

**Objective:** provide an opinionated experiment console for one operator.

**Completed core outcomes**

- Built minimal Vite + React + TypeScript dashboard in `ui/`.
- Added typed API and SSE clients.
- Added Zustand stores and UI components for control, topology, jobs, KPI cards, charts, and exports.
- Added scenario-first run flow with quick-start and baseline comparison.
- Added Playwright smoke coverage and frontend CI lint/type-check/build wiring.

### Phase 6 — Deployment and performance (completed)

**Objective:** make local reproducibility easy and measurable.

**Completed core outcomes**

- Added multi-stage `Dockerfile` and `ui/Dockerfile`.
- Added `compose.yaml` with API/UI services and health checks.
- Added `.dockerignore`, `.env.example`, benchmark scaffolding.
- Added `TESTING.md` with test execution guidance.

### Phase 7 — Batch and process manufacturing (planned extension)

**Objective:** preserve forward evolution while keeping MVP stable.

**Current status:** preserved in design documents as post-MVP, but not required for current MVP completion.

- Reference scenario: gin distillery.
- Extends state model to batch/volume, material recipes, cleaning times, and multi-component costs.
- Adds `sim-material`, multi-stage cost and supply handling, and phase-specific scenario set.

## 6. Acceptance and validation inventory

### 6.1 Core checks already completed

- Determinism validation from repeated seeded runs.
- Factory/economy correctness checks under normal and constrained scenarios.
- Agent intervention behavior and command validation.
- API control path and UI command path parity.
- CI for Rust formatting/lint/tests and frontend build/type-check/lint.

### 6.2 Remaining manual checks after implementation

- Full Playwright CI execution in local CI-capable environment.
- Docker `compose up --build` verification in an environment with Docker available.
- Benchmark number baseline capture and trend tracking.
- Keyboard interaction completeness checks for all controls.
- Event log click-through filtering in UI by job id.

## 7. Out of scope and future work

### 7.1 MVP out-of-scope

- Multiplayer/MMO, distributed shards, advanced auth, full ERP/MES integration.
- Advanced scheduling optimization and planning algorithms.
- Full ISA-95/B2MML exchange and FMI/OPC-UA/FIPA production use in MVP.
- LLM-native agent autonomy, complex protocol interop, and enterprise observability stacks.

### 7.2 Future directions

- Digital twin and OPC-UA integration.
- Serious-game workflows and training scenario DSLs.
- Multi-agent ecosystems and negotiation protocols.
- Multi-site/supply-chain expansion and batch/process ecosystems beyond current discrete scope.

## 8. Design decisions and trade-off ledger

### 8.1 Why plan was executed in this order

- Early correctness-first foundation reduced rework risk for crates, tests, and CI.
- Determinism and scenario IO were defined before API/UI features to avoid mismatched interfaces.
- API and UI were intentionally layered above a stable runner and command contract.

### 8.2 Key architecture choices retained in the archived implementation

- **Event-based command model** over direct shared-state mutation: preserves reproducibility and testability.
- **`EventHandler` + composite runner** instead of inverse dependencies from `sim-core` to domain crates.
- **`Arcogine` binary name and mode split** in one CLI for local and service workflows.
- **Separate API command channel model** rather than shared mutable state between async API and deterministic runner.
- **TOML scenario schema** with explicit naming alignment to ISA-95 vocabulary where useful.
- **UI via API only** and event streaming for reactive updates.

### 8.3 Documented trade-offs

- Postponed full OpenAPI endpoint annotation for complete spec while keeping command and schema compatibility stable.
- Used `std::sync::mpsc` in final runtime path because simulation executes on a synchronous OS thread.
- Kept Material Layer (`sim-material`) as Phase 7 to avoid changing discrete-MVP scope.
- Left Playwright execution in repo for smoke intent with a local/manual CI step due environment constraints.

### 8.4 Standards and interoperability posture

- ISA-95 naming alignment for core concepts.
- ISO 22400 KPI mapping for production reporting definitions.
- DES methodology as foundational runtime model.
- OpenAPI 3.x generated from API surface.

## 9. Canonical references

This roadmap is now the canonical archive for the original plan content.

- `docs/vision.md` for identity, mission, and target loop.
- `docs/architecture-overview.md` for architecture and architecture-level conventions.
- `docs/standards-alignment.md` for standards mapping and future interoperability.
- `TESTING.md` for test execution details.
- `README.md` for quick usage and onboarding.
