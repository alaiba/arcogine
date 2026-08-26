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

When documents disagree, the higher layer governs product direction; the lower layer remains authoritative for current implementation detail. Proposed architecture references and Proposed ADRs describe a target or decision under consideration; they do not override current-state documentation. Accepted ADRs constrain intended architecture, but they likewise do not make unimplemented behavior current capability.

## Development and contributing

The root [README](../README.md#quick-start) owns environment setup and local-run instructions. Contributor and testing documents reference that setup rather than maintaining parallel startup procedures.

| Document | What it covers |
|----------|-----------------|
| [CONTRIBUTING.md](../.github/CONTRIBUTING.md) | Contribution workflow, code style, architecture constraints, required validation |
| [reviewing.md](development/reviewing.md) | Independent PR review and re-review workflow, severity/disposition, AI-assisted session boundaries, CI language, and durable-knowledge rules |
| [testing.md](development/testing.md) | Full test category reference, CI pipeline, quality gates, native test commands |
| [CODE_OF_CONDUCT.md](../.github/CODE_OF_CONDUCT.md) | Community standards |

## Architecture and design

| Document | What it covers |
|----------|-----------------|
| [Architecture](architecture/overview.md) | Current design philosophy (including Events–State–Observations), module structure, determinism contract, event dispatch, technology stack |
| [Factory design architecture](architecture/factory-design.md) | Proposed cross-consumer factory-design semantics, scenario/model/runtime separation, publication boundary, spatial ownership, and design-lifecycle principles |
| [Governance and conformance architecture](architecture/governance-conformance.md) | Proposed cross-domain model lineage, semantic change, requirements, conformance, evidence, governed change, and compliance-as-projection architecture |
| [ADR-0003: Canonical factory model boundary](architecture/decisions/0003-canonical-factory-model-boundary.md) | Accepted decision that `ScenarioConfig` remains a run envelope while immutable published factory-model versions bridge design and runtime |
| [ADR-0004: Model identity, revision lineage, and external change control](architecture/decisions/0004-model-identity-revision-lineage-and-external-change-control.md) | Accepted decision separating semantic model fingerprint from controlled revision/change-management identity, with external change-management systems referenced, not depended on |
| [Standards alignment](architecture/standards-alignment.md) | Standards influence and claim boundaries across ISA-95, ISO 22400, DES, RAMI 4.0, and future integrations |
| [ISA-95 semantic mapping](architecture/isa-95-semantic-mapping.md) | Maintained Arcogine-to-ISA-95 concept mapping, deliberate divergences, structural gaps, and design-review policy |
| [Vision (superseded)](product/vision.md) | Pointer to the Product Charter; retains naming/etymology history only |
| [Decision records](architecture/decisions/README.md) | Architecture/design decision history — why significant choices were made |
| [SECURITY.md](../.github/SECURITY.md) | Security policy, hardening posture, deployment constraints |

## Examples

| Document | What it covers |
|----------|-----------------|
| [Examples README](examples/README.md) | Executable TOML scenario fixture files (educational, not runtime assets — never shipped in `dist/` or Docker images) |

## Internal planning (maintainers)

These documents are internal planning artifacts, not user-facing guides:

| Document | What it covers |
|----------|-----------------|
| [Factory design capability plan](planning/factory-design-capability.md) | Immediate upstream work: canonical model seam, validation, publication/provenance, and behavior-preserving runtime instantiation |
| [Governance and conformance capability plan](planning/governance-conformance-capability.md) | Cross-cutting sequence after the model seam: durable lineage, semantic ChangeSets, generic conformance, evidence, external-workflow governed change (Jira anticipated), framework mappings, and audit projections |
| [Factory simulation engine readiness](planning/factory-simulation-engine-readiness.md) | Runtime gates after the model seam: explicit workload/work execution, deterministic dispatch, session control, observations/events, and spatial consequences |
| [Factory-design game challenge readiness](planning/factory-design-game-challenge-readiness.md) | Game-owned parallel track for challenge identity/validation, candidate admissibility, catalogue and budget rules, deterministic evaluation, attempt provenance/comparison, and cross-track learning with governance |
| [Factory-design game consumer initiative](planning/factory-design-game-consumer.md) | Downstream consumer boundary between the game and Arcogine, including readiness entry criteria and ownership constraints |
| [Factory-design game vertical slice](planning/factory-design-game-vertical-slice.md) | Product hypothesis for the first playable slice: fixed contract, capacity/layout/cost trade-offs, diagnosis, and deterministic redesign |
