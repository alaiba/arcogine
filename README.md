# Arcogine

Arcogine is a deterministic simulation engine for factory systems, economic dynamics, and agent-driven decision making.

## What is Arcogine?

Arcogine is a **simulation-first platform** that models and experiments with factory operations, economic feedback loops, and autonomous decision-making processes. It bridges three domains:

- **Operations** — production, capacity, bottlenecks
- **Economics** — pricing, demand, revenue
- **Decision systems** — agents, policies, incentives

The core simulation loop is deterministic and event-driven: decisions affect operations, and operations feed back into decisions.

## Quick Start

### Native (development)

```bash
# Clone and build
git clone https://github.com/your-username/arcogine.git
cd arcogine
cargo build

# Run tests
cargo test

# Start the API server
cargo run --bin arcogine -- serve

# Run a scenario headlessly
cargo run --bin arcogine -- run --headless
```

### Containerized

```bash
docker compose up --build
```

The API server will be available at `http://localhost:3000` and the experiment console at `http://localhost:5173`.

## Architecture

Arcogine uses a Cargo workspace with modular crates:

| Crate | Purpose |
|-------|---------|
| `sim-types` | Shared types, typed IDs, error definitions |
| `sim-core` | Event engine, scheduler, logging, KPIs, scenario loader |
| `sim-factory` | Machines, jobs, routing, queues |
| `sim-economy` | Pricing, demand, revenue |
| `sim-agents` | Agent trait and implementations |
| `sim-api` | HTTP API (Axum), SSE stream, OpenAPI spec |
| `sim-cli` | CLI entrypoint (headless and server modes) |

The simulation core is headless and deterministic. A thin REST API and web-based experiment console provide observability and control. See `docs/architecture-overview.md` for the full design philosophy and `docs/vision.md` for project identity and long-term directions.

## Standards Alignment

Arcogine's data model aligns with industry standards where practical:

- **ISA-95 / IEC 62264** — domain concept naming
- **ISO 22400** — KPI definitions
- **DES methodology** — core simulation approach
- **OpenAPI 3.x** — REST API specification

See `docs/standards-alignment.md` for the full mapping.

## Contributing

See `CONTRIBUTING.md` for development workflow, code style, and testing conventions.

## License

Apache-2.0 — see `LICENSE`.
