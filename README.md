# Arcogine

[![CI](https://github.com/alaiba/arcogine/actions/workflows/ci.yml/badge.svg)](https://github.com/alaiba/arcogine/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/alaiba/arcogine/graph/badge.svg)](https://codecov.io/gh/alaiba/arcogine)

Arcogine is building toward purpose-built ways to design, understand, simulate, verify, operate, and improve a production system — all grounded in one executable model of the business. See [`docs/product/charter.md`](docs/product/charter.md) for the full product vision and enduring principles.

**The current implementation is an early, deterministic, simulation-focused slice of that vision.** It has no application server, HTTP API, or CLI product surface today — retained executable evidence is tests, conformance checks, and benchmarks; a future outward consumer is introduced from the supported runtime contract when a concrete product need exists. It does not yet include digital-twin connectivity, live operational execution, or multi-user/production deployment — see [Security](.github/SECURITY.md) and [Architecture](docs/architecture/overview.md) for exactly what exists today.

## What is Arcogine today?

Today, the retained implementation centers on Factory Design, a deterministic FactoryRuntime and Engine, Governance/conformance, the Challenge consumer, and Finance's financial interpretation of completed orders. FactoryRuntime executes explicit production workload from a published model version and exposes supported observations and runtime events. It does not currently provide an interactive application or experiment loop.

Repeated runs with the same model version, Engine semantics, and explicit commands produce identical outputs. **New to Arcogine?** Read [Concepts](docs/product/concepts.md) for the current retained capabilities.

## Quick start

This section is the canonical setup and local-run guide. `./arcogine` is the repository entry point for common workflows.

### Dev container (recommended)

```bash
git clone https://github.com/alaiba/arcogine.git
cd arcogine
```

Open the folder in VS Code with Dev Containers. The preferred development container provides JDK 25 and keeps the Gradle cache plus GitHub CLI configuration in a named Docker volume.

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

- **Java compatibility baseline:** JDK 21 is a first-class development runtime. Java compilation uses `--release 21`; CI runs on JDK 21 while the preferred devcontainer currently uses JDK 25.
- **Node.js:** Node remains repository tooling for scripts such as snapshot/retrospective utilities, but Arcogine no longer has a product/frontend Node compatibility contract.

Raising a supported Java minimum remains a deliberate repository change with coordinated CI and documentation updates.

## Running the current simulation

Arcogine currently has no application server, HTTP API, or CLI product surface — retained executable evidence is Java tests, architecture conformance checks, and benchmarks, run directly through the Java build (see [Quality gates](#quality-gates) below). A future outward consumer will be introduced from the supported [runtime contract](docs/architecture/runtime-contract.md) when a concrete product need exists.

## Technology stack

| Layer | Technology |
|-------|-----------|
| Simulation engine | Java (Java 21 compatibility baseline; preferred devcontainer JDK 25) |
| Build | Gradle (Kotlin DSL), via the `product/gradlew` wrapper |

## Documentation

| Document | What it covers |
|----------|----------------|
| [Product Charter](docs/product/charter.md) | Enduring product vision and principles — start here to understand what Arcogine is ultimately becoming |
| [Concepts](docs/product/concepts.md) | Current Factory and Engine capabilities |
| [Architecture](docs/architecture/overview.md) | Design philosophy, module structure, determinism contract |
| [Full docs index](docs/README.md) | Everything else: testing, standards, vision, security |

## Quality gates

```bash
./arcogine setup         # optional full-development dependency bootstrap
./arcogine test          # Java unit tests
./arcogine check         # Java compile, style, tests, and coverage
./arcogine check --full  # check + dependency audit + secret scan
```

See [testing.md](docs/development/testing.md) for the full test category reference.

## Contributing

See [CONTRIBUTING.md](.github/CONTRIBUTING.md) for the contribution workflow, code style, architecture constraints, and required validation. Setup and local-run instructions live here in the README.

## License

Apache-2.0 — see [LICENSE](LICENSE).
