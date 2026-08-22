# Arcogine

[![CI](https://github.com/alaiba/arcogine/actions/workflows/ci.yml/badge.svg)](https://github.com/alaiba/arcogine/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/alaiba/arcogine/graph/badge.svg)](https://codecov.io/gh/alaiba/arcogine)

Arcogine is a deterministic simulation engine for factory systems, economic dynamics, and agent-driven decision making.

## What is Arcogine?

Arcogine is a **simulation-first platform** where you experiment with how pricing, capacity, and automated agents interact in a factory environment. Three systems feed back into each other:

```text
     You set a price
           │
           ▼
    Demand responds        (lower price → more orders)
           │
           ▼
    Factory produces        (machines process jobs through routing steps)
           │
           ▼
    KPIs update             (throughput, lead time, backlog, revenue)
           │
           ▼
    You (or the agent)      (observe KPIs, decide what to change)
    make decisions
           │
           └───────────────► loop repeats
```

The simulation is fully deterministic: same inputs produce identical outputs every time. This makes it useful for comparing strategies, testing hypotheses, and understanding cause-and-effect in complex operational systems.

**New to Arcogine?** Read [Concepts](docs/concepts.md) to understand what you're looking at before running your first session.

## Quick start

### Dev container (recommended)

```bash
git clone https://github.com/alaiba/arcogine.git
cd arcogine
```

Open the folder in VS Code with the [Dev Containers](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers) extension. The container provides Java 25 and Node; dependency caches (`~/.gradle`, `ui/node_modules`) live in named Docker volumes for speed.

After the container is ready, start the UI and API in two terminals:

```bash
# Terminal 1: UI dev server
./arcogine run ui

# Terminal 2: API server
./arcogine run api
```

Then open **http://127.0.0.1:5173**.

### Other setup paths

- **Docker Compose:** `cp .env.example .env && docker compose up --build`

## Your first session

You can go from clone to meaningful results in under five minutes:

1. **Load a scenario** — the welcome overlay offers three built-in options.
2. **Run the simulation** — click Run and watch KPIs update in real time.
3. **Try an intervention** — change the price or toggle a machine offline.
4. **Save a baseline** — snapshot the current state before a big change.
5. **Compare** — make the change, then compare against your baseline.
6. **Toggle the agent** — enable the Sales Agent and see how it manages pricing.

### Built-in scenarios

| Scenario | Challenge | What you'll learn |
|----------|-----------|-------------------|
| **Basic** | None — balanced factory | How the controls work and what the KPIs mean |
| **Overload** | Demand exceeds capacity | How to stabilize backlog and lead times with pricing |
| **Capacity Expansion** | Same pressure, more machines | Whether structural upgrades beat tactical tuning |

### Headless mode

Run a scenario without the UI:

```bash
java -jar java/sim-cli/build/libs/arcogine.jar run examples/basic_scenario.toml
```

## Technology stack

| Layer | Technology |
|-------|-----------|
| Simulation engine | Java 25 (records, sealed interfaces, pattern matching) |
| HTTP API | Spring Boot 3.4 + Spring MVC |
| CLI | Picocli |
| Build | Gradle (Kotlin DSL), via the `java/gradlew` wrapper |
| Frontend | React 19 + TypeScript + Vite |
| Container | Eclipse Temurin 25 JRE |

## Documentation

| Document | What it covers |
|----------|----------------|
| [Concepts](docs/concepts.md) | How the simulation works, KPIs, agents, scenarios |
| [API Reference](docs/api.md) | Every HTTP endpoint with curl examples |
| [Architecture](docs/architecture.md) | Design philosophy, module structure, determinism contract |
| [Full docs index](docs/README.md) | Everything else: testing, standards, vision, security |

## Quality gates

```bash
./arcogine setup   # install/bootstrap dependencies
./arcogine test    # Java + frontend unit tests
./arcogine check   # fast gates: compile, tests, coverage, lint, build
make quality-full  # everything: quality + Playwright E2E + Docker smoke + security scans
```

See [TESTING.md](docs/TESTING.md) for the full test category reference.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for setup paths, development workflow, code style, and testing conventions.

## License

Apache-2.0 — see [LICENSE](LICENSE).
