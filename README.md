# Arcogine

Arcogine is a deterministic simulation engine for factory systems, economic dynamics, and agent-driven decision making.

## What is Arcogine?

Arcogine is a **simulation-first platform** for experimentation with industrial operations and decision policies. It bridges three domains:

- **Operations** — production, capacity, bottlenecks
- **Economics** — pricing, demand, revenue
- **Decision systems** — agents, policies, incentives

The core simulation loop is deterministic and event-driven: decisions affect operations, and operations feed back into decisions.

## Quick start

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

## Contributing

See `CONTRIBUTING.md` for development workflow, alternative setup paths (native, Docker Compose), toolchain policy, and testing conventions. For architecture and design details see `docs/architecture-overview.md`; for project identity and long-term directions see `docs/vision.md`.

## License

Apache-2.0 — see `LICENSE`.
