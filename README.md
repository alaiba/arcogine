# Arcogine

Arcogine is a deterministic simulation engine for factory systems, economic dynamics, and agent-driven decision making.

## What is Arcogine?

Arcogine is a **simulation-first platform** for experimentation with industrial operations and decision policies. It bridges three domains:

- **Operations** — production, capacity, bottlenecks
- **Economics** — pricing, demand, revenue
- **Decision systems** — agents, policies, incentives

The core simulation loop is deterministic and event-driven: decisions affect operations, and operations feed back into decisions.

## Rust toolchain policy

The repository follows a floating Stable policy:

- `rust-toolchain.toml` is set to `channel = "stable"` (floating).
- The dev container image and native workflow both rely on stable channels.

## Quick start paths

### 1) Dev container (recommended)

```bash
git clone https://github.com/alaiba/arcogine.git
cd arcogine
```

Open the folder in VS Code with the Dev Containers extension. The container post-create command:

- builds the workspace,
- installs UI dependencies with `npm ci`,
- copies `.env.example` to `.env` when missing.

After the container is ready:

```bash
cd ui
npm run dev
```

In another terminal:

```bash
cd arcogine
cargo run --bin arcogine -- serve --addr 0.0.0.0:3000
```

Then open `http://127.0.0.1:5173`.

### 2) Native development (host tools)

```bash
git clone https://github.com/alaiba/arcogine.git
cd arcogine
cargo build
cargo test
```

Terminal 1:

```bash
cargo run --bin arcogine -- serve --addr 127.0.0.1:3000
```

Terminal 2:

```bash
cd ui
npm ci
npm run dev
```

Open `http://localhost:5173`.

### 3) Docker Compose

```bash
cp .env.example .env
docker compose up --build
```

Open `http://localhost:3000` (health) and `http://localhost:5173` (UI).

## Network model for frontend API calls

Arcogine uses one explicit networking model:

- Same-origin browser paths with a `/api` base (`/api/health`, `/api/scenario`, ...).
- Native/dev mode: Vite proxy sends `/api` to `http://localhost:3000`.
- Container mode: Nginx proxies `/api` to the `api:3000` service.

There are no additional API URL environment variables used by the shipped container build.

## Play Arcogine

Arcogine supports both:

- **serious simulation** for deterministic experimentation,
- **lightweight challenge play** for quick scenario-oriented sessions.

The built-in scenarios are intentionally shaped as challenge modes:

- **Basic** — learn controls and baseline behavior.
- **Overload** — stabilize backlog, lead times, and throughput when demand is excessive.
- **Capacity Expansion** — compare infrastructure upgrades versus reactive controls.

## First interaction loop

You can launch and operate a meaningful session in under five minutes:

1. Start the UI and load a scenario.
2. Run or step through a few ticks.
3. Adjust price and machine availability to influence flow.
4. Toggle the agent for assistance.
5. Save a baseline before major changes.
6. Compare current results with your saved baseline.

Headless mode (no UI, no server) is also available:

```bash
cargo run --bin arcogine -- run --headless --scenario examples/basic_scenario.toml
```

## Running Tests

See `TESTING.md` for the full testing guide. Quick summary:

```bash
cargo fmt --check && cargo clippy -- -D warnings && cargo test
cd ui && npm ci && npx tsc --noEmit && npm run build
```

## Architecture

Arcogine uses a Cargo workspace with modular crates:

| Crate | Purpose |
|-------|---------|
| `sim-types` | Shared types, typed IDs, error definitions |
| `sim-core` | Event engine, scheduler, logging, KPIs, scenario loader |
| `sim-factory` | Machines, jobs, routing, queues |
| `sim-economy` | Pricing, demand, revenue |
| `sim-agents` | Agent trait and implementations |
| `sim-api` | HTTP API (Axum), SSE stream |
| `sim-cli` | CLI entrypoint (headless and server modes) |

The simulation core is headless and deterministic. A thin REST API and web-based experiment console provide observability and control. See `docs/architecture-overview.md` for the full design philosophy and `docs/vision.md` for project identity and long-term directions.

## Standards Alignment

Arcogine's data model aligns with industry standards where practical:

- **ISA-95 / IEC 62264** — domain concept naming
- **ISO 22400** — KPI definitions
- **DES methodology** — core simulation approach
See `docs/standards-alignment.md` for the full mapping.

## Contributing

See `CONTRIBUTING.md` for development workflow, code style, and testing conventions.

## License

Apache-2.0 — see `LICENSE`.
