# Documentation

## Start here

| Document | Who it's for | What it covers |
|----------|-------------|----------------|
| [Product Charter](product/charter.md) | Everyone | Arcogine's enduring product vision, system thesis, and principles — read this first |
| [Root README](../README.md) | Everyone | What Arcogine is today, canonical setup/local-run guide, first session |
| [Concepts](product/concepts.md) | New users | How the current simulation works, KPIs, agents, scenarios |
| [API Reference](reference/api.md) | Developers | Every HTTP endpoint with curl examples |

## Documentation hierarchy

Arcogine's documentation is layered, and each layer answers a different question:

- **[`product/charter.md`](product/charter.md)** — normative. What Arcogine is ultimately intended to become, and the principles future work is evaluated against. Not a roadmap, not a feature list.
- **[`architecture/overview.md`](architecture/overview.md)** — current architecture plus the enduring architectural principles that follow from the Charter. Explains how the system is built today and which parts of that are expected to persist regardless of implementation.
- **[`architecture/decisions/`](architecture/decisions/README.md)** (ADRs) — historical rationale for significant, hard-to-reverse decisions: *why* the system ended up the way it did, operating under the Charter and current architecture rather than setting product direction themselves.
- **[`product/concepts.md`](product/concepts.md)**, **[`reference/api.md`](reference/api.md)**, [`../product/interfaces/web/README.md`](../product/interfaces/web/README.md) — current capability/reference documentation. Describe what exists now, honestly, without projecting future capability.
- **[`planning/`](planning/)** — temporary analysis, proposals, and assessments. Useful for context; not authoritative for current or future direction.

When documents disagree, the higher layer governs product direction; the lower layer remains authoritative for current implementation detail.

## Development and contributing

The root [README](../README.md#quick-start) owns environment setup and local-run instructions. Contributor and testing documents reference that setup rather than maintaining parallel startup procedures.

| Document | What it covers |
|----------|-----------------|
| [CONTRIBUTING.md](../.github/CONTRIBUTING.md) | Contribution workflow, code style, architecture constraints, required validation |
| [testing.md](development/testing.md) | Full test category reference, CI pipeline, quality gates, native test commands |
| [CODE_OF_CONDUCT.md](../.github/CODE_OF_CONDUCT.md) | Community standards |

## Architecture and design

| Document | What it covers |
|----------|-----------------|
| [Architecture](architecture/overview.md) | Design philosophy (including the Events–State–Observations model), module structure, determinism contract, event dispatch, technology stack |
| [Standards alignment](architecture/standards-alignment.md) | ISA-95, ISO 22400, DES, RAMI 4.0 mapping |
| [Vision (superseded)](product/vision.md) | Pointer to the Product Charter; retains naming/etymology history only |
| [Decision records](architecture/decisions/README.md) | Architecture/design decision history — why significant choices were made |
| [SECURITY.md](../.github/SECURITY.md) | Security policy, hardening posture, deployment constraints |

## Examples

| Document | What it covers |
|----------|-----------------|
| [Examples README](examples/README.md) | Executable TOML scenario fixtures (educational, not runtime assets — never shipped in `dist/` or Docker images) |

## Internal planning (maintainers)

These documents are internal planning artifacts, not user-facing guides:

| Document | What it covers |
|----------|-----------------|
| [Factory-design game consumer initiative](planning/factory-design-game-consumer.md) | Proposed external game consumer, vertical-slice boundary, Arcogine capability gaps, and acceptance criteria |
| [Product/workflow entity separation plan](planning/product-and-workflow-entity-separation-plan.md) | Domain-model planning notes |
| [Events–State–Observations architecture assessment](planning/architecture-assessment-events-state-observations.md) | Source-level review against the Events–State–Observations philosophy and staged refactoring backlog |
