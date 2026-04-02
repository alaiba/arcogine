# Contributing to Arcogine

Thank you for considering a contribution to Arcogine. This guide covers the conventions and workflow for getting involved.

## Prerequisites

- **Rust** (stable channel, floating policy in `rust-toolchain.toml`)
- **Node.js** 20+ and npm (for the `ui/` experiment console)
- **Docker** and Docker Compose (optional, for containerized runs)

## Host vs container prerequisites

Native development requires Rust and Node installed on the host.

Container development requires Docker plus VS Code Dev Containers.

If your host does not have Rust/Node installed, use the dev container path first.

## Choose a start path

### 1) Dev container

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

### 3) Docker Compose runtime

```bash
cp .env.example .env
docker compose up --build
```

## Repository Layout

See `docs/architecture-overview.md` for the full crate structure and design rationale.

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

## Development Workflow

1. **Branch** from `main` with a descriptive name (`feature/xyz`, `fix/abc`).
2. **Make your changes.** Follow the code style enforced by `cargo fmt` and `cargo clippy`.
3. **Write tests** for new functionality. All crates with logic have unit tests; integration tests live in `crates/sim-api/tests/`.
4. **Run the checks:**
   ```bash
   cargo fmt --check
   cargo clippy -- -D warnings
   cargo test
   cd ui
   npm ci
   npx tsc --noEmit
   npm run build
   ```
5. **Open a pull request** against `main` with a clear description of what changed and why.

## Code Style

- Run `cargo fmt` before committing.
- All `cargo clippy` warnings are treated as errors in CI.
- Prefer explicit types over inference in public APIs.
- All public types and functions must have doc-comments.
- State structs derive `PartialEq`, `Eq`, `Clone`, `Debug`, and `serde::Serialize`.

## Testing

- **Unit tests** live alongside the code they test (in `tests/` directories within each crate).
- **Integration tests** that require multiple domain crates live in `crates/sim-api/tests/`.
- **Property tests** use `proptest` in `crates/sim-core/` and `crates/sim-factory/`.
- **Benchmarks** use Criterion in `crates/sim-core/benches/`.
- **E2E tests** for the UI use Playwright in `ui/e2e/`.

## Determinism Contract

Arcogine's simulation must produce identical results given identical inputs. All stochastic behavior uses `ChaCha8Rng` seeded from the scenario configuration. See the Determinism Contract section in `docs/architecture-overview.md` for details.

## Commit Messages

Use concise, descriptive commit messages. Reference the phase and task number when applicable (e.g., "Phase 2: implement event scheduler").

## License

By contributing, you agree that your contributions will be licensed under the Apache-2.0 license (see `LICENSE`).
