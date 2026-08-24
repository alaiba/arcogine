# Contributing to Arcogine

Thank you for considering a contribution to Arcogine. This guide covers the contribution workflow, code standards, architecture constraints, and required validation.

For environment setup and running Arcogine locally, use the canonical [Quick start](../README.md#quick-start) in the root README. `./arcogine` is the canonical developer entry point for common cross-project workflows.

Before proposing a significant product, domain, or architecture change, read [`docs/product/charter.md`](../docs/product/charter.md). It's the normative source for Arcogine's product direction — significant proposals should be evaluated against it as well as the current architecture constraints in [`docs/architecture/overview.md`](../docs/architecture/overview.md). It's strategic context for judgment calls, not a rulebook for blocking routine local implementation work.

## Repository layout

| Directory | Purpose |
|-----------|---------|
| `product/types/` | Shared types, typed IDs, error definitions |
| `product/simulation/` | Event engine, scheduler, logging, KPIs, scenario loader |
| `product/domains/factory/` | Machines, jobs, routing, queues |
| `product/domains/economy/` | Pricing, demand, revenue |
| `product/domains/finance/` | Ledger, financial interpretation of operational events |
| `product/agents/` | Agent interface and implementations |
| `product/interfaces/api/` | HTTP API (Spring Boot MVC), SSE |
| `product/interfaces/cli/` | CLI entrypoint (Picocli, produces `arcogine.jar`) |
| `product/interfaces/web/` | React/TypeScript experiment console |
| `docs/examples/` | TOML scenario fixture files |
| `docs/` | Project documentation |
| `infra/` | Container and dev-environment infrastructure |

See [`docs/architecture/overview.md`](../docs/architecture/overview.md) for the full module dependency graph and design rationale.

## Development workflow

1. **Branch** from `main` with a descriptive name (`feature/xyz`, `fix/abc`).
2. **Make your changes.** Follow the code style enforced by Checkstyle and the frontend lint/format tooling.
3. **Write tests** for new functionality. Java modules use JUnit 5; frontend stores and components use Vitest and Testing Library.
4. **Run the checks:**

```bash
./arcogine check         # fast gates: compile, lint, tests, coverage, typecheck, build
./arcogine check --full  # everything: check + Playwright + Docker + security
```

Use `./arcogine check` before pushing. Use `./arcogine check --full` when the change warrants the complete local validation surface. For individual test categories and native subsystem commands, see [`docs/development/testing.md`](../docs/development/testing.md).

5. **Open a pull request** against `main` with a clear description of what changed and why.

## Code style

- Checkstyle enforces Java style; `./arcogine check` (or `cd product && ./gradlew checkstyleMain checkstyleTest`) runs it, and warnings are treated as errors at compile time (`-Werror`).
- ESLint + Prettier enforce frontend style; `cd product/interfaces/web && npm run lint` runs it.
- Prefer explicit types over inference in public APIs.

## Architecture guardrails

Arcogine follows an Events–State–Observations model:

```text
Events mutate State.
State produces Observations.
Observations inform Decisions.
Decisions produce Events.
```

The authoritative architecture description and detailed domain semantics live in [`docs/architecture/overview.md`](../docs/architecture/overview.md). Read it before adding a domain, changing cross-module boundaries, changing event dispatch, or touching `IntegratedHandler`.

During implementation and review, preserve these non-negotiable constraints:

1. Every mutable piece of domain state has exactly one authoritative owner; cross-domain consumers receive read-only observations or explicit purpose-specific context, never another subsystem's mutable state.
2. Agents observe and emit decisions/events; they do not mutate simulation domains directly. Decisions that change simulation state become deterministic simulation events.
3. Observation objects remain immutable and purpose-specific. API DTOs and UI snapshots are not automatically valid domain observations.
4. Handler execution order stays explicit wherever order affects semantics. Do not introduce asynchronous/event-bus dispatch that weakens deterministic, explicitly ordered execution.
5. Do not introduce synchronized copies of authoritative state or pairwise setter wiring between domains as a coupling mechanism.
6. Keep domain concepts distinct and owned by the appropriate domain; in particular, operational facts and Finance's financial interpretation of those facts must remain separate.
7. The simulation must remain deterministic: identical inputs and seeds produce identical results.

A subset of these constraints is CI-enforced by `interfaces/api`'s ArchUnit `ArchitectureTest`; the remainder are review constraints. Detailed examples — including pricing/order terminology and Commercial, Operational, and Financial Truth — belong in the architecture documentation rather than this contributor guide.

## Testing

The contribution gate is `./arcogine check`. It covers Java compilation, Checkstyle, tests and Jacoco coverage gates, plus frontend linting, type-checking, tests, coverage, and production build.

For Playwright E2E, the canonical distribution build, Docker image/smoke validation, and security scans, run `./arcogine check --full`.

See [`docs/development/testing.md`](../docs/development/testing.md) for the test taxonomy, CI pipeline, native subsystem commands, and testing rationale.

## Commit messages

Use concise, descriptive commit messages. Reference the phase and task number when applicable (e.g., "Phase 2: implement event scheduler").

## License

By contributing, you agree that your contributions will be licensed under the Apache-2.0 license (see [`LICENSE`](../LICENSE)).
