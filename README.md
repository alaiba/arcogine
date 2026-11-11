# Arcogine

[![CI](https://github.com/alaiba/arcogine/actions/workflows/ci.yml/badge.svg)](https://github.com/alaiba/arcogine/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/alaiba/arcogine/graph/badge.svg)](https://codecov.io/gh/alaiba/arcogine)

Arcogine is building toward purpose-built ways to design, understand, simulate, verify, operate, and improve a production system — all grounded in one executable model of the business. See [`docs/product/charter.md`](docs/product/charter.md) for the full product vision and enduring principles.

**The current implementation is an early, deterministic, simulation-focused slice of that vision.** Its one outward consumer is the local single-player [Last Light Foundry](product/consumer/last-light-foundry/README.md) browser game (a Java session server plus a bundled web client, started with `./arcogine play`); there is no other application server, HTTP API, or CLI product surface. Retained executable evidence is otherwise tests, conformance checks, and benchmarks. It does not yet include digital-twin connectivity, live operational execution, or multi-user/production deployment — see [Security](.github/SECURITY.md) and [Architecture](docs/architecture/overview.md) for exactly what exists today.

## What is Arcogine today?

Today, the retained implementation centers on Factory Design, a deterministic FactoryRuntime and Engine, Governance/conformance, built-in Storage for controlled revisions, and the Challenge consumer. FactoryRuntime executes explicit production workload from a published model version and exposes supported observations and runtime events. The only interactive application is the Last Light Foundry game consumer; there is no experiment loop.

Repeated runs with the same model version, Engine semantics, and explicit commands produce identical outputs. **New to Arcogine?** Read [Concepts](docs/product/concepts.md) for the current retained capabilities.

## Quick start

This section is the canonical setup and local-run guide. `./arcogine` is the repository entry point for common workflows.

### Dev container (recommended)

```bash
git clone https://github.com/alaiba/arcogine.git
cd arcogine
```

Open the folder in VS Code with Dev Containers. The preferred development container provides the JDK (defined in [`.devcontainer/devcontainer.json`](.devcontainer/devcontainer.json)) and keeps the Gradle cache plus GitHub CLI configuration in a named Docker volume.

On a brand-new machine, authenticate GitHub once inside the container:

```bash
gh auth login
gh auth setup-git
```

The container reuses that credential state across rebuilds.

### Native development

Optionally run `./arcogine setup`, then use the quality gates below.

The devcontainer is one supported environment, not the development contract. `./arcogine setup` resolves the Java dependency/toolchain surface; it is not required for repository inspection or narrow documentation work.

### Development toolchain policy

- **Java compatibility baseline:** see [`docs/development/testing.md`](docs/development/testing.md#prerequisites-and-version-policy).
- **Node.js:** Node is repository tooling for scripts such as snapshot/retrospective utilities; Arcogine has no product Node compatibility contract.

Raising a supported Java minimum remains a deliberate repository change with coordinated CI and documentation updates.

## Running the current simulation

To play the engine-backed game, start the local server and its bundled browser client with one command (JDK 21 required; no account, API key or network access beyond the first Gradle dependency download):

```bash
./arcogine play
```

Then open <http://127.0.0.1:8551/>. See the [Last Light Foundry guide](product/consumer/last-light-foundry/README.md) for how to play, configuration, and troubleshooting. Apart from that consumer, Arcogine has no application server, HTTP API, or CLI product surface: retained executable evidence is Java tests, architecture conformance checks, and benchmarks, run directly through the Java build (see [Quality gates](#quality-gates) below). Further outward consumers are introduced from the supported [runtime contract](docs/architecture/runtime-contract.md) when a concrete product need exists.

## Technology stack

| Layer | Technology |
|-------|-----------|
| Simulation engine | Java ([compatibility baseline](docs/development/testing.md#prerequisites-and-version-policy)) |
| Build | Gradle (Kotlin DSL), via the `product/gradlew` wrapper |
| Game consumer | JDK built-in HTTP server and a build-free browser client (ES modules, SVG); Node and Playwright only for its tests |

## Documentation

| Document | What it covers |
|----------|----------------|
| [Product Charter](docs/product/charter.md) | Enduring product vision and principles — start here to understand what Arcogine is ultimately becoming |
| [Concepts](docs/product/concepts.md) | Current Factory and Engine capabilities |
| [Architecture](docs/architecture/overview.md) | Design philosophy, module structure, determinism contract |
| [Last Light Foundry](product/consumer/last-light-foundry/README.md) | How to launch and play the browser game |
| [Full docs index](docs/README.md) | Everything else: testing, standards, vision, security |

## Quality gates

```bash
./arcogine setup         # optional full-development dependency bootstrap
./arcogine test          # Java unit tests
./arcogine check         # Java compile, style, tests, and coverage
./arcogine check --full  # check + Java dependency audit + secret scan
./arcogine play          # start the Last Light Foundry game (not a quality gate)
```

The game's browser tests (frontend unit tests and real-browser end-to-end tests against the Java server) run in CI; see [testing.md](docs/development/testing.md#12-game-browser-tests).

See [testing.md](docs/development/testing.md) for the full test category reference.

## Contributing

See [CONTRIBUTING.md](.github/CONTRIBUTING.md) for the contribution workflow, code style, architecture constraints, and required validation. Setup and local-run instructions live here in the README.

## License

Apache-2.0 — see [LICENSE](LICENSE).
