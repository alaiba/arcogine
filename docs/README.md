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

## Cross-track ownership map

The active architecture/readiness tracks are siblings with explicit ownership boundaries. They may progress in parallel using clearly scoped fixtures, but one track must not invent durable substitutes for another track's owned semantics.

| Concern | Primary owner | Boundary |
|---|---|---|
| Canonical production-system semantics, validation, publication, deterministic instantiation | [Factory design architecture](architecture/factory-design.md) / [Factory design capability](planning/factory-design-capability.md) | Published semantic model is the shared source for downstream contexts |
| Deterministic workload, dispatch, simulation session, runtime events/observations, spatial consequences | [Factory simulation engine readiness](planning/factory-simulation-engine-readiness.md) | Owns simulation/runtime truth; does not become a production-control runtime by default |
| Durable semantic fingerprint policy, controlled revision lineage, ChangeSets, requirements/assertions, conformance, evidence use, findings/exceptions | [Governance and conformance](architecture/governance-conformance.md) / [capability plan](planning/governance-conformance-capability.md) | Governance G1 identity/history, G2 semantic ChangeSet (initial slice), and G3 requirement/assertion contract are implemented; operational facts may be consumed as evidence, but Governance does not ingest telemetry or reconcile the twin |
| Execution contexts, verified operational identity/trust, command/result lifecycle, deployment application, external observations, reconciliation, drift/calibration feedback, adapter resilience | [Operational execution and digital twin](architecture/operational-execution-digital-twin.md) / [readiness plan](planning/operational-execution-digital-twin-readiness.md) | Raw external observations keep independent provenance; operational work references Governance-owned revision/ChangeSet/evidence-use contracts when those exist |
| Game challenge identity, admissibility, scoring, attempt provenance/comparison | [Factory-design game challenge readiness](planning/factory-design-game-challenge-readiness.md) | Sibling proving ground; no generic evaluation framework or domain-type unification |

Two dependency rules are especially important:

1. **Fixtures are not sibling completion.** Governance G1 is complete, so new operational deployment/revision-bound reconciliation work should consume its authoritative revision identity/history rather than synthetic G1 revision fixtures. Governance G2's initial slice (semantic `ChangeSet`/`ImpactScope` and the factory-domain semantic comparator) and Governance G3 (generic `Requirement`/`Assertion`/`RequirementCatalogue`) are also complete and available for consumption rather than synthetic substitution. Clearly scoped synthetic fixtures remain appropriate for the still-outstanding G4+ conformance-evaluation/evidence/authorization capabilities; likewise synthetic operational adapters do not satisfy Engine Readiness gates.
2. **External observations are not revision- or Arcogine-context-bound at ingestion.** Their source/time/trust provenance is independent. Arcogine context/model/revision binding belongs to reconciliation, interpretation, deployment correlation, or Governance `EvidenceUse` when applicable.

## Development and contributing

The root [README](../README.md#quick-start) owns environment setup and local-run instructions. Contributor and testing documents reference that setup rather than maintaining parallel startup procedures.

| Document | What it covers |
|----------|-----------------|
| [CONTRIBUTING.md](../.github/CONTRIBUTING.md) | Contribution workflow, code style, architecture constraints, required validation |
| [reviewing.md](development/reviewing.md) | Independent PR review and re-review workflow, severity/disposition, AI-assisted session boundaries, CI language, and durable-knowledge rules |
| [consistency-review.md](development/consistency-review.md) | Human operating model for recurring repository consistency reviews, finding persistence, baseline discipline, and the trigger for durable review-state storage |
| [testing.md](development/testing.md) | Full test category reference, CI pipeline, quality gates, native test commands |
| [codex-cloud.md](development/codex-cloud.md) | Observed Codex Cloud environment model, validated workflow, limitations, and recommended bounded-task usage |
| [CODE_OF_CONDUCT.md](../.github/CODE_OF_CONDUCT.md) | Community standards |

## Architecture and design

| Document | What it covers |
|----------|-----------------|
| [Architecture](architecture/overview.md) | Current design philosophy (including Events–State–Observations), module structure, determinism contract, event dispatch, technology stack |
| [Factory design architecture](architecture/factory-design.md) | Proposed cross-consumer factory-design semantics, scenario/model/runtime separation, publication boundary, spatial ownership, and design-lifecycle principles |
| [Governance and conformance architecture](architecture/governance-conformance.md) | Proposed cross-domain model lineage, semantic change, requirements, conformance, evidence, governed change, and compliance-as-projection architecture |
| [Operational execution and digital twin architecture](architecture/operational-execution-digital-twin.md) | Proposed execution-context, verified identity/trust, command/result, deployment, external-observation, reconciliation, drift/calibration, and industrial-adapter boundaries |
| [ADR-0003: Canonical factory model boundary](architecture/decisions/0003-canonical-factory-model-boundary.md) | Accepted decision that `ScenarioConfig` remains a run envelope while immutable published factory-model versions bridge design and runtime |
| [ADR-0004: Model identity, revision lineage, and external change control](architecture/decisions/0004-model-identity-revision-lineage-and-external-change-control.md) | Accepted decision separating semantic model fingerprint from controlled revision/change-management identity, with external change-management systems referenced, not depended on |
| [ADR-0011: Runtime observation and event contract](architecture/decisions/0011-runtime-observation-and-event-contract.md) | Accepted Gate 4 semantics separating internal scheduler events, authoritative observations, ordered runtime events, transport adapters, and later recovery |
| [ADR-0012: External interchange and serialization boundaries](architecture/decisions/0012-external-interchange-and-serialization-boundaries.md) | Accepted policy keeping Arcogine semantic contracts authoritative while JSON/OpenAPI, CloudEvents, Parquet, industrial standards, and other representations remain explicit projections/adapters |
| [Standards alignment](architecture/standards-alignment.md) | Standards, format-selection, interchange, and conformance boundaries across manufacturing, runtime, analytics, governance, and operational integrations |
| [ISA-95 semantic mapping](architecture/isa-95-semantic-mapping.md) | Maintained Arcogine-to-ISA-95 concept mapping, deliberate divergences, structural gaps, and design-review policy |
| [Vision (superseded)](product/vision.md) | Pointer to the Product Charter; retains naming/etymology history only |
| [Decision records](architecture/decisions/README.md) | Architecture/design decision history — why significant choices were made |
| [SECURITY.md](../.github/SECURITY.md) | Security policy, hardening posture, deployment constraints, and mature operational trust boundary |

## Examples

| Document | What it covers |
|----------|-----------------|
| [Examples README](examples/README.md) | Executable TOML scenario fixtures (educational, not runtime assets — never shipped in `dist/` or Docker images) |

## Internal planning (maintainers)

These documents are internal planning artifacts, not user-facing guides:

| Document | What it covers |
|----------|-----------------|
| [Factory design capability plan](planning/factory-design-capability.md) | Immediate upstream work: canonical model seam, validation, publication/provenance, and behavior-preserving runtime instantiation |
| [Governance and conformance capability plan](planning/governance-conformance-capability.md) | Cross-cutting sequence after the model seam: durable lineage, semantic ChangeSets, generic conformance, evidence, external-workflow governed change, framework mappings, and audit projections |
| [Factory simulation engine readiness](planning/factory-simulation-engine-readiness.md) | Runtime gates after the model seam: explicit workload/work execution, deterministic dispatch, session control, observations/events, and spatial consequences |
| [Gate 4 runtime observation/event delivery](planning/gate-4-runtime-observation-event-delivery.md) | Implementation companion for ADR-0011: W1 benchmark prerequisite, headless observation/event slices, provenance, API/SSE migration, recovery boundary, and PR landing sequence |
| [Operational execution and digital twin readiness](planning/operational-execution-digital-twin-readiness.md) | Sibling readiness track for execution-context identity, verified trust/authority, command/deployment lifecycle, external observations, reconciliation, drift/calibration, resilience, and a first live-system adapter proving ground, with explicit Governance/Engine prerequisites |
| [Factory-design game challenge readiness](planning/factory-design-game-challenge-readiness.md) | Game-owned parallel track for challenge identity/validation, candidate admissibility, catalogue and budget rules, deterministic evaluation, attempt provenance/comparison, and cross-track learning with governance |
| [Factory-design game consumer initiative](planning/factory-design-game-consumer.md) | Downstream consumer boundary between the game and Arcogine, including readiness entry criteria and ownership constraints |
| [Factory-design game vertical slice](planning/factory-design-game-vertical-slice.md) | Product hypothesis for the first playable slice: fixed contract, capacity/layout/cost trade-offs, diagnosis, and deterministic redesign |
