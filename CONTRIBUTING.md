# Contributing to Arcogine

Thank you for considering a contribution to Arcogine. This guide covers everything you need to get started.

## Prerequisites

- **JDK 25** and **Node.js 24+** (for native, non-container development)
- **Docker** and Docker Compose (optional, for containerized runs)

If your host doesn't have JDK 25 / Node 24 installed, use the dev container path — it provides both.

## Choose a start path

### 1) Dev container (recommended)

```bash
git clone https://github.com/alaiba/arcogine.git
cd arcogine
```

Open the repository in VS Code and reopen in the dev container. `postCreateCommand` installs UI dependencies (`npm ci`), installs the Playwright browser, and copies `.env.example` to `.env` if missing. Gradle and npm caches live in named Docker volumes, so subsequent rebuilds are fast.

After startup, in two terminals:

```bash
./arcogine run ui
./arcogine run api
```

### 2) Native (host JDK + host Node)

```bash
git clone https://github.com/alaiba/arcogine.git
cd arcogine
./arcogine setup
./arcogine test
```

Then run:

```bash
./arcogine run api
./arcogine run ui
```

### 3) Docker Compose

```bash
cp .env.example .env
docker compose up --build
```

## Repository layout

| Directory | Purpose |
|-----------|---------|
| `java/sim-types/` | Shared types, typed IDs, error definitions |
| `java/sim-core/` | Event engine, scheduler, logging, KPIs, scenario loader |
| `java/sim-factory/` | Machines, jobs, routing, queues |
| `java/sim-economy/` | Pricing, demand, revenue |
| `java/sim-finance/` | Ledger, financial interpretation of operational events |
| `java/sim-agents/` | Agent interface and implementations |
| `java/sim-api/` | HTTP API (Spring Boot MVC), SSE |
| `java/sim-cli/` | CLI entrypoint (Picocli, produces `arcogine.jar`) |
| `ui/` | React/TypeScript experiment console |
| `examples/` | TOML scenario fixture files |
| `docs/` | Project documentation |

See `docs/architecture.md` for the full module dependency graph and design rationale.

## Development workflow

1. **Branch** from `main` with a descriptive name (`feature/xyz`, `fix/abc`).
2. **Make your changes.** Follow the code style enforced by Checkstyle.
3. **Write tests** for new functionality. Each module has JUnit 5 unit tests; frontend stores and components are tested with Vitest and Testing Library.
4. **Run the checks:**

```bash
./arcogine check     # fast gates: compile, lint, tests, coverage, typecheck, build
make quality-full    # everything: check + playwright + docker + security
```

Run `make help` to see every available target.

5. **Open a pull request** against `main` with a clear description of what changed and why.

## Code style

- Checkstyle enforces Java style; `./arcogine check` (or `make java-lint`) runs it, and warnings are treated as errors at compile time (`-Werror`).
- ESLint + Prettier enforce frontend style; `make frontend-lint` runs it.
- Prefer explicit types over inference in public APIs.

## Architecture guardrails (Events, State, Observations)

Arcogine follows an Events–State–Observations philosophy (see `docs/architecture.md`):

```text
Events mutate State.
State produces Observations.
Observations inform Decisions.
Decisions produce Events.
```

Use these rules during code review, especially when adding a new domain (inventory, procurement, finance, workforce, maintenance, another agent) or touching `IntegratedHandler`. A subset is CI-enforced, not just review discipline — `sim-api`'s `ArchitectureTest` (ArchUnit) checks: `sim-agents`/`sim-finance` never depend on `sim-factory`/`sim-economy`; `Ledger.post` and `Job`/`Machine`'s lifecycle mutators are never called from outside their owning module. Everything else below is still enforced by review.

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
11. Don't collapse distinct concepts into one field because they're both prices/money — e.g. the firm's own current asking price (`OfferPrice`) and an already-created order's agreed terms (`OrderPrice`/`OrderValue`) are different things with different mutability, and a historical transaction fact should be captured on the event that created it rather than re-derived later from current mutable state. Don't call `OfferPrice` a "market price" either — that name is reserved for a future external-market signal (`ObservedMarketPrice`) that Arcogine doesn't model yet; `OfferPrice` is what the firm sets, not what an outside market observes. See `docs/architecture.md`'s "Pricing, orders, and money" section for the worked example. Also: prefer precise operational terms like `CompletedSalesValue` over "revenue" outside Finance — a formally-recorded financial figure belongs in the Finance domain (see next rule), not scattered across operational handlers.
12. Financial concepts (cash, a recorded sales/revenue balance, receivables, payables) belong in the Finance domain, not in Factory, Economy, or any other operational domain — see `docs/architecture.md`'s "Commercial, Operational, and Financial Truth" section. Operational domains emit facts (e.g. `OrderCompleted`); Finance owns the financial interpretation of those facts, reacting to events rather than inspecting another domain's mutable state to infer what happened. A journal entry that doesn't balance (`sum(debits) != sum(credits)`) must never be able to enter financial state.

See `devel/architecture-assessment-events-state-observations.md` for the current-state review and backlog.

## Testing

Run `./arcogine check` before pushing. That covers:

- Java compilation, Checkstyle, unit tests, and Jacoco coverage gates
- Frontend linting, type-checking, unit tests, coverage, and production build

For the full test surface including Playwright E2E, Docker, and security scans, run `make quality-full`.

See `docs/TESTING.md` for the complete test category reference.

### Test layers at a glance

| Layer | Location | Tool |
|-------|----------|------|
| Java unit/integration tests | `java/*/src/test/` | JUnit 5 |
| Architecture tests | `java/sim-api/src/test/` | ArchUnit |
| Frontend unit tests | `ui/src/**/*.test.{ts,tsx}` | Vitest |
| E2E tests | `ui/e2e/` | Playwright |
| Benchmarks | `java/sim-core` (`jmh` source set) | JMH |

## Determinism contract

Arcogine's simulation must produce identical results given identical inputs. All stochastic behavior uses a `Random` seeded from the scenario configuration. See the determinism contract section in `docs/architecture.md` for details.

## Commit messages

Use concise, descriptive commit messages. Reference the phase and task number when applicable (e.g., "Phase 2: implement event scheduler").

## License

By contributing, you agree that your contributions will be licensed under the Apache-2.0 license (see `LICENSE`).
