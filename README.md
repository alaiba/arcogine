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

This section is the canonical setup and local-run guide. `./arcogine` is the repository entry point for common workflows.

### Dev container (recommended)

```bash
git clone https://github.com/alaiba/arcogine.git
cd arcogine
```

Open the folder in VS Code with Dev Containers. The preferred development container provides JDK 25 and keeps the Gradle cache plus GitHub CLI configuration in named Docker volumes.

On a brand-new machine, authenticate GitHub once inside the container:

```bash
gh auth login
gh auth setup-git
```

The container reuses that credential state across rebuilds.

To start the current HTTP API:

```bash
./arcogine run api
```

For headless execution, use `./arcogine run scenario PATH`.

### Other execution environments

- **Docker Compose:** `./arcogine build && ./arcogine up`
- **Native development:** optionally run `./arcogine setup`, then use the API or headless commands above.

The devcontainer is one supported environment, not the development contract. `./arcogine setup` resolves the Java dependency/toolchain surface; it is not required for repository inspection or narrow documentation work.

### Development toolchain policy

- **Java compatibility baseline:** JDK 21 is a first-class development runtime. Java compilation uses `--release 21`; CI runs on JDK 21 while the preferred devcontainer currently uses JDK 25.
- **Runtime Java:** the API runtime image currently uses Eclipse Temurin 25 JRE. Runtime-image JDK and Java compilation compatibility are deliberately separate concerns.
- **Node.js:** Node remains repository tooling for scripts such as snapshot/retrospective utilities, but Arcogine no longer has a product/frontend Node compatibility contract.

Raising a supported Java minimum remains a deliberate repository change with coordinated CI and documentation updates.

## Running the current simulation

The retained application surfaces are headless scenario execution and the local HTTP API.

### Headless mode

```bash
java -jar dist/api/arcogine.jar run docs/examples/basic.toml
```

Without building `dist/` first:

```bash
./arcogine run scenario docs/examples/basic.toml
```

### HTTP API

Start the local API with:

```bash
./arcogine run api
```

The API remains a current local simulation interface while Arcogine's consumer-neutral Engine contracts evolve independently. See [API Reference](docs/reference/api.md) for the current endpoints.

## Technology stack

| Layer | Technology |
|-------|-----------|
| Simulation engine | Java (Java 21 compatibility baseline; preferred devcontainer JDK 25) |
| HTTP API | Spring Boot 4 + Spring MVC |
| CLI | Picocli |
| Build | Gradle (Kotlin DSL), via the `product/gradlew` wrapper |
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
./arcogine setup         # optional full-development dependency bootstrap
./arcogine test          # Java unit tests
./arcogine check         # Java compile, style, tests, and coverage
./arcogine check --full  # check + dist build + Docker smoke + security scans
```

See [testing.md](docs/development/testing.md) for the full test category reference.

## Contributing

See [CONTRIBUTING.md](.github/CONTRIBUTING.md) for the contribution workflow, code style, architecture constraints, and required validation. Setup and local-run instructions live here in the README.

## License

Apache-2.0 — see [LICENSE](LICENSE).
