# Documentation

## Start here

| Document | Who it's for | What it covers |
|----------|-------------|----------------|
| [Product Charter](../PRODUCT_CHARTER.md) | Everyone | Arcogine's enduring product vision, system thesis, and principles — read this first |
| [Root README](../README.md) | Everyone | What Arcogine is today, quick start, first session |
| [Concepts](concepts.md) | New users | How the current simulation works, KPIs, agents, scenarios |
| [API Reference](api.md) | Developers | Every HTTP endpoint with curl examples |

## Documentation hierarchy

Arcogine's documentation is layered, and each layer answers a different question:

- **[`PRODUCT_CHARTER.md`](../PRODUCT_CHARTER.md)** — normative. What Arcogine is ultimately intended to become, and the principles future work is evaluated against. Not a roadmap, not a feature list.
- **[`architecture.md`](architecture.md)** — current architecture plus the enduring architectural principles that follow from the Charter. Explains how the system is built today and which parts of that are expected to persist regardless of implementation.
- **[`decisions/`](decisions/README.md)** (ADRs) — historical rationale for significant, hard-to-reverse decisions: *why* the system ended up the way it did, operating under the Charter and current architecture rather than setting product direction themselves.
- **[`concepts.md`](concepts.md)**, **[`api.md`](api.md)**, [`../ui/README.md`](../ui/README.md) — current capability/reference documentation. Describe what exists now, honestly, without projecting future capability.
- **[`../devel/`](../devel/)** — temporary analysis, proposals, and assessments. Useful for context; not authoritative for current or future direction.

When documents disagree, the higher layer governs product direction; the lower layer remains authoritative for current implementation detail.

## Contributing

| Document | What it covers |
|----------|----------------|
| [CONTRIBUTING.md](../CONTRIBUTING.md) | Setup paths, workflow, code style, testing quick reference |
| [TESTING.md](TESTING.md) | Full test category reference, CI pipeline, quality gates |
| [CODE_OF_CONDUCT.md](../CODE_OF_CONDUCT.md) | Community standards |

## Architecture and design

| Document | What it covers |
|----------|----------------|
| [Architecture](architecture.md) | Design philosophy (including the Events–State–Observations model), module structure, determinism contract, event dispatch, technology stack |
| [Standards alignment](standards-alignment.md) | ISA-95, ISO 22400, DES, RAMI 4.0 mapping |
| [Vision (superseded)](vision.md) | Pointer to the Product Charter; retains naming/etymology history only |
| [Decision records](decisions/README.md) | Architecture/design decision history — why significant choices were made |
| [SECURITY.md](../SECURITY.md) | Security policy, hardening posture, deployment constraints |

## Internal planning (maintainers)

These documents are internal planning artifacts, not user-facing guides:

| Document | What it covers |
|----------|----------------|
| [Deployment options](../devel/deployment-options.md) | Runtime path analysis |
| [Logging improvement plan](../devel/logging-improvement-plan.md) | Observability audit and improvement plan |
| [Events–State–Observations architecture assessment](../devel/architecture-assessment-events-state-observations.md) | Source-level review against the Events–State–Observations philosophy and staged refactoring backlog |
