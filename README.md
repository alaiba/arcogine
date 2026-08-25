# Arcogine

[![CI](https://github.com/alaiba/arcogine/actions/workflows/ci.yml/badge.svg)](https://github.com/alaiba/arcogine/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/alaiba/arcogine/graph/badge.svg)](https://codecov.io/gh/alaiba/arcogine)

Arcogine is building toward purpose-built ways to design, understand, simulate, verify, operate, and improve a production system — all grounded in one executable model of the business. See [`docs/product/charter.md`](docs/product/charter.md) for the full product vision and enduring principles.

**The current implementation is an early, deterministic, simulation-focused slice of that vision.** It does not yet include digital-twin connectivity, live operational execution, or multi-user/production deployment — see [Security](.github/SECURITY.md) and [Architecture](docs/architecture/overview.md) for exactly what exists today.

## What is Arcogine today?

Today, Arcogine is a simulation platform where you experiment with how pricing, capacity, and automated agents interact in a factory environment. Three systems feed back into each other:

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

**New to Arcogine?** Read [Concepts](docs/product/concepts.md) to understand what you're looking at before running your first session.

## Quick start

This section is the canonical setup and local-run guide for Arcogine. `./arcogine` is the canonical developer entry point for common cross-project workflows.

### Dev container (recommended)

```bash
git clone https://github.com/alaiba/arcogine.git
cd arcogine
```

Open the folder in VS Code with the [Dev Containers](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote-containers) extension. The preferred development container currently provides **JDK 25 and Node 24**; dependency caches (`~/.gradle`, `product/interfaces/web/node_modules`) live in named Docker volumes for speed.

After the container is ready, start the web app and API in two terminals:

```bash
# Terminal 1: web dev server
./arcogine run web

# Terminal 2: API server
./arcogine run api
```

Then open **http://127.0.0.1:5173**.

### Other setup paths

- **Docker Compose:** `./arcogine build && ./arcogine up`
- **Native (Linux/macOS, or Windows via WSL/Git Bash):** `./arcogine setup`, then `./arcogine run api` / `./arcogine run web` as above.

`./arcogine` is a Bash script. On Windows it runs in the Dev Container, WSL, or Git Bash — not directly in PowerShell/cmd. The Dev Container is the recommended path on Windows.

### Development toolchain policy

Arcogine deliberately separates **supported compatibility** from the versions used by its preferred development and runtime environments:

- **Java compatibility baseline:** Java 21. Java compilation uses `--release 21`, so a supported newer JDK may compile the project without allowing post-21 language features, APIs, or bytecode. CI runs on an actual JDK 21; the preferred devcontainer currently uses JDK 25.
- **Node.js support:** `^22.22.2 || ^24.15.0 || ^26.0.0`, declared in `product/interfaces/web/package.json`. CI exercises the lowest supported Node 22 release, 22.22.2; the preferred devcontainer currently uses Node 24.
- **Runtime Java:** the API runtime image currently uses Eclipse Temurin 25 JRE. The runtime-image JDK is independent of the Java 21 build-compatibility floor.

Raising a supported minimum is a deliberate repository change: update the compatibility declaration, CI floor, provisioning validation, and current documentation together. Preferred devcontainer/runtime versions may move independently as long as they remain compatible.

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
java -jar dist/api/arcogine.jar run docs/examples/basic.toml
```

(or, without building `dist/` first: `./arcogine run scenario docs/examples/basic.toml`)

## Technology stack

| Layer | Technology |
|-------|-----------|
| Simulation engine | Java (Java 21 compatibility baseline; preferred devcontainer JDK 25) |
| HTTP API | Spring Boot 3.4 + Spring MVC |
| CLI | Picocli |
| Build | Gradle (Kotlin DSL), via the `product/gradlew` wrapper |
| Frontend | React 19 + TypeScript + Vite |
| Container | Eclipse Temurin 25 JRE |

## Documentation

| Document | What it covers |
|----------|----------------|
| [Product Charter](docs/product/charter.md) | Enduring product vision and principles — start here to understand what Arcogine is ultimately becoming |
| [Concepts](docs/product/concepts.md) | How the current simulation works, KPIs, agents, scenarios |
| [API Reference](docs/reference/api.md) | Every HTTP endpoint with curl examples |
| [Architecture](docs/architecture/overview.md) | Design philosophy, module structure, determinism contract |
| [Full docs index](docs/README.md) | Everything else: testing, standards, vision, security |

## Quality gates

```bash
./arcogine setup         # install/bootstrap dependencies
./arcogine test          # Java + frontend unit tests
./arcogine check         # fast gates: compile, tests, coverage, lint, build
./arcogine check --full  # everything: check + Playwright E2E + Docker smoke + security scans
```

See [testing.md](docs/development/testing.md) for the full test category reference.

## Contributing

See [CONTRIBUTING.md](.github/CONTRIBUTING.md) for the contribution workflow, code style, architecture constraints, and required validation. Setup and local-run instructions live here in the README.

## License

Apache-2.0 — see [LICENSE](LICENSE).
