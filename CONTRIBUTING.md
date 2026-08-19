# Contributing to Arcogine

Thank you for considering a contribution to Arcogine. This guide covers everything you need to get started.

## Prerequisites

- **Rust** (stable channel, floating policy in `rust-toolchain.toml`)
- **Node.js** 20+ and npm (for the `ui/` experiment console)
- **Docker** and Docker Compose (optional, for containerized runs)

Native development requires Rust and Node installed on the host. If your host does not have Rust/Node installed, use the dev container path.

## Choose a start path

### 1) Dev container (recommended)

```bash
git clone https://github.com/alaiba/arcogine.git
cd arcogine
```

Open the repository in VS Code and reopen in the dev container. The post-create script:

- runs `cargo build`,
- installs UI dependencies with `npm ci`,
- copies `.env.example` to `.env` if missing.

After startup:

```bash
cd ui
npm run dev
```

In a second terminal:

```bash
cargo run --bin arcogine -- serve --addr 0.0.0.0:3000
```

### 2) Native (host Rust + host Node)

```bash
git clone https://github.com/alaiba/arcogine.git
cd arcogine
cargo build
cargo test
```

Then run:

```bash
cargo run --bin arcogine -- serve --addr 127.0.0.1:3000
cd ui
npm ci
npm run dev
```

### 3) Docker Compose

```bash
cp .env.example .env
docker compose up --build
```

## Repository layout

| Directory | Purpose |
|-----------|---------|
| `crates/sim-types/` | Shared types, typed IDs, error definitions |
| `crates/sim-core/` | Event engine, scheduler, logging, KPIs, scenario loader |
| `crates/sim-factory/` | Machines, jobs, routing, queues |
| `crates/sim-economy/` | Pricing, demand, revenue |
| `crates/sim-agents/` | Agent trait and implementations |
| `crates/sim-api/` | HTTP API (Axum), SSE |
| `crates/sim-cli/` | CLI entrypoint (`arcogine` binary) |
| `ui/` | React/TypeScript experiment console |
| `examples/` | TOML scenario fixture files |
| `docs/` | Project documentation |

See `docs/architecture.md` for the full crate dependency graph and design rationale.

## Development workflow

1. **Branch** from `main` with a descriptive name (`feature/xyz`, `fix/abc`).
2. **Make your changes.** Follow the code style enforced by `cargo fmt` and `cargo clippy`.
3. **Write tests** for new functionality. Each crate has inline `#[cfg(test)]` unit test modules; integration tests live in `crates/sim-api/tests/`. Frontend stores and components are tested with Vitest and Testing Library.
4. **Run the checks:**

```bash
make quality        # fast gates: fmt, clippy, tests, coverage, lint, typecheck, build
make quality-full   # everything: quality + playwright + docker + security
```

Run `make help` to see all available targets.

5. **Open a pull request** against `main` with a clear description of what changed and why.

## Code style

- Run `make fmt` before committing (`cargo fmt --check` under the hood).
- All Clippy warnings are treated as errors — `make clippy` runs `cargo clippy -- -D warnings`.
- Prefer explicit types over inference in public APIs.
- All public types and functions must have doc-comments.
- State structs derive `PartialEq`, `Eq`, `Clone`, `Debug`, and `serde::Serialize`.

## Architecture guardrails (Events, State, Observations)

Arcogine follows an Events–State–Observations philosophy (see `docs/architecture.md`):

```text
Events mutate State.
State produces Observations.
Observations inform Decisions.
Decisions produce Events.
```

Use these rules during code review, especially when adding a new domain (inventory, procurement, finance, workforce, maintenance, another agent) or touching `IntegratedHandler`:

1. Every mutable piece of domain state must have exactly one authoritative owner.
2. Cross-domain consumers receive read-only observations or explicit, purpose-specific context — never a reference to another subsystem's mutable state.
3. Agents observe and emit decisions/events; they never mutate simulation domains directly.
4. Decisions that affect simulation state become deterministic simulation events, not direct method calls into another handler.
5. Observation objects are immutable and purpose-specific — don't widen one into a general-purpose state dump.
6. Avoid synchronized copies of authoritative state (no new `setX`/`syncX` cross-domain pushes without a documented reason).
7. Handler execution order must stay explicit wherever order affects semantics — don't let it fall out of construction or registration order.
8. New domains should not require pairwise setter wiring to every existing domain.
9. Don't introduce an event bus or async dispatch to solve coupling — it must not weaken Arcogine's deterministic, explicitly-ordered execution.
10. API DTOs and UI snapshots are not automatically valid domain observations; treat `SnapshotBuilder`'s projections and `AgentObservation` as separate concerns with separate capability boundaries.
11. Don't collapse distinct concepts into one field because they're both prices/money — e.g. current market state (`MarketPrice`) and an already-created order's agreed terms (`OrderPrice`/`OrderValue`) are different things with different mutability, and a historical transaction fact should be captured on the event that created it rather than re-derived later from current mutable state. See `docs/architecture.md`'s "Pricing, orders, and money" section for the worked example. Also: prefer precise operational terms like `CompletedSalesValue` over "revenue" outside Finance — a formally-recorded financial figure belongs in the Finance domain (see next rule), not scattered across operational handlers.
12. Financial concepts (cash, a recorded sales/revenue balance, receivables, payables) belong in the Finance domain, not in Factory, Economy, or any other operational domain — see `docs/architecture.md`'s "Commercial, Operational, and Financial Truth" section. Operational domains emit facts (e.g. `OrderCompleted`); Finance owns the financial interpretation of those facts, reacting to events rather than inspecting another domain's mutable state to infer what happened. A journal entry that doesn't balance (`sum(debits) != sum(credits)`) must never be able to enter financial state.

See `devel/architecture-assessment-events-state-observations.md` for the current-state review and backlog.

## Testing

Run `make quality` before pushing. That covers:

- Rust formatting, linting, workspace tests, and coverage
- Frontend linting, type-checking, unit tests, coverage, and production build

For the full test surface including Playwright E2E, Docker, and security scans, run `make quality-full`.

See `docs/TESTING.md` for the complete test category reference.

### Test layers at a glance

| Layer | Location | Tool |
|-------|----------|------|
| Rust unit tests | `#[cfg(test)]` in each crate | `cargo test` |
| Rust integration tests | `crates/sim-api/tests/` | `cargo test` |
| Property tests | `crates/sim-core/tests/`, `crates/sim-factory/tests/` | `proptest` |
| Frontend unit tests | `ui/src/**/*.test.{ts,tsx}` | Vitest |
| E2E tests | `ui/e2e/` | Playwright |
| Benchmarks | `crates/sim-core/benches/` | Criterion |

## Determinism contract

Arcogine's simulation must produce identical results given identical inputs. All stochastic behavior uses `ChaCha8Rng` seeded from the scenario configuration. See the determinism contract section in `docs/architecture.md` for details.

## Commit messages

Use concise, descriptive commit messages. Reference the phase and task number when applicable (e.g., "Phase 2: implement event scheduler").

## License

By contributing, you agree that your contributions will be licensed under the Apache-2.0 license (see `LICENSE`).
