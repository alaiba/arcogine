# Documentation

## Start here

| Document | Who it's for | What it covers |
|---|---|---|
| [Product Charter](product/charter.md) | Everyone | Enduring product thesis and principles |
| [Root README](../README.md) | Everyone | What Arcogine is today, setup, and local run |
| [Concepts](product/concepts.md) | New users | Current Factory and Engine capabilities |

## Documentation hierarchy

Arcogine separates durable direction, current truth, and research; known work lives outside the documentation:

- **[`product/charter.md`](product/charter.md)** — normative product direction and enduring principles; not a roadmap.
- **[`architecture/overview.md`](architecture/overview.md)** — current architecture plus enduring architectural principles, and the cross-cutting constraints the focused specifications apply.
- **[`architecture/`](architecture/overview.md)** — focused architecture and specification documents owning exact domain contracts, identities, semantics and supported boundaries.
- **Current capability/reference docs** — [`product/concepts.md`](product/concepts.md) and consumer/component references describe what exists now.
- **[`research/`](research/README.md)** — research-area index linking the normative operating model, current research portfolio state, synthesis signals, investigation artifacts, and reusable brief/report structures. Research is not accepted architecture or implementation commitment.
- **GitHub issues** — known work defined well enough to implement but not yet started. Issues are delivery context, not documentation authority.
- **[`history/`](history/README.md)** — dated, non-normative evidence retained for later comparison, including selected decision rationale; never current authority.

Research discovers, and hands implementation-ready conclusions to the [Implementer](development/agent-roles/implementer.md). Product direction and canonical architecture/specifications establish durable meaning. Landed code/tests and current-state docs establish what actually exists. Arcogine keeps no separate decision-record authority: the durable result of an architectural change is the changed canonical document, and Git and pull-request history preserve why it changed. Where the reasoning behind a significant choice is worth deliberate later retrieval, a concise [historical decision-rationale record](history/README.md#decision-rationale) may also preserve it; such a record explains a past choice and never defines current meaning.

A research conclusion does not become authoritative merely because it is recorded. An unresolved question that still determines implementation meaning must not be hidden inside an issue or a handoff.

### Delivery-coordinate boundary

Temporary delivery coordinates — issue and pull-request numbers, and PR-local `REV-<N>` review identifiers — belong to delivery context, not research or durable semantic naming ([`AGENTS.md`](../AGENTS.md#temporary-delivery-coordinates-and-durable-documentation)).

## Cross-track ownership

| Concern | Primary authority / delivery surface | Boundary |
|---|---|---|
| Canonical production-system semantics, validation, publication, deterministic instantiation | [Factory design architecture](architecture/factory-design.md) | One published semantic model is the downstream source of truth |
| Deterministic workload, work items, dispatch, session, supported observations/events | [Engine semantics](architecture/engine-semantics.md) / [runtime contract](architecture/runtime-contract.md) | Simulation runtime truth; not production-control semantics |
| Semantic fingerprint/revision history, semantic change, requirements, conformance, evidence/governed change | [Governance architecture](architecture/governance-conformance.md) | Governance does not ingest telemetry or perform external actuation/reconciliation |
| Built-in persistent realization of controlled revisions | [Storage architecture](architecture/storage.md) | Governance owns history meaning; Storage owns current local persistence and its support scope |
| Operational identity/trust, external realization, subject correspondence, external observations, reconciliation, drift/resilience | [Operational architecture](architecture/operational-execution-digital-twin.md) / [Operational research](research/investigations/operational-execution-digital-twin-boundaries.md) | No implementation exists until the required semantic boundaries are resolved |
| Game challenge identity, catalogue/economics, admissibility, evaluation, attempt comparison | [Challenge](architecture/challenge.md) | Headless game-owned rules; not production simulation |
| Playable factory-design product research | [Game product-research programme](research/investigations/factory-design-game-vertical-slice.md) | Umbrella only; lifecycle lives on focused questions such as strategy space and diagnostic evidence |
| Inter-resource transfer and spatial layout | [Transfer research](research/investigations/transfer-semantics.md) | Decided spatial record and transfer lifecycle; not implemented, so no current model has transfer |

## Development and contributing

| Document | What it covers |
|---|---|
| [CONTRIBUTING.md](../.github/CONTRIBUTING.md) | Contribution workflow, style, validation |
| [reviewing.md](development/reviewing.md) | Independent PR review/re-review workflow |
| [semantic-contract-support.md](development/semantic-contract-support.md) | Work-in-progress status, durability-claim audits, explicit promotion declarations and promise-specific review evidence |
| [researching.md](development/researching.md) | Normative Arcogine research operating model: lifecycle/priority semantics, promotion/reconciliation, investigation and adversarial review, evidence custody, synthesis-seed handling, historical decision-rationale retention, and register maintenance |
| [continuous-improvement.md](development/continuous-improvement.md) | Continuous-improvement operating model: Session-close Kaizen, Consistency review, and delivery-process retrospective method |
| [consistency-review.md](development/consistency-review.md) | Recurring repository consistency-review operating model |
| [testing.md](development/testing.md) | Test categories, CI, quality gates, native commands |
| [repository-snapshot.md](development/repository-snapshot.md) | Canonical whole-repository retrieval snapshot |
| [codex-cloud.md](development/codex-cloud.md) | Codex Cloud environment/workflow notes |
| [Agent role contracts](development/agent-roles/README.md) | Canonical provider-neutral specialized procedures and optional runtime adapters |
| [coding-agent-evaluation.md](development/coding-agent-evaluation.md) | Non-normative coding-agent/model evaluation |
| [SECURITY.md](../.github/SECURITY.md) | Security policy and mature operational security boundary |

## Architecture and design

| Document | What it covers |
|---|---|
| [Architecture overview](architecture/overview.md) | Current design, modules, determinism and Events-State-Observations principles |
| [Factory design](architecture/factory-design.md) | Cross-consumer factory-model/design lifecycle semantics |
| [Governance and conformance](architecture/governance-conformance.md) | Revision/change/requirements/conformance/evidence/governed-change architecture |
| [Storage](architecture/storage.md) | Public contract, built-in provider, local persistence guarantees and limits |
| [Operational execution and digital twin](architecture/operational-execution-digital-twin.md) | Proposed relationship-based execution/reality architecture |
| [Engine semantics](architecture/engine-semantics.md) | Normative work-in-progress result-affecting Engine interpretation |
| [Factory model](architecture/factory-model.md) | Normative work-in-progress Factory records, predicates and canonical/fingerprint byte grammar |
| [Standards alignment](architecture/standards-alignment.md) | Standards/interchange/conformance boundaries |
| [ISA-95 semantic mapping](architecture/isa-95-semantic-mapping.md) | Maintained manufacturing semantic mapping and deliberate divergences |
| [Runtime contract](architecture/runtime-contract.md) | Supported observations, ordered authoritative runtime events, provenance and transport boundary |
| [Controlled revisions](architecture/controlled-revisions.md) | Historical occurrence identity, lineage, provenance and authority acceptance |
| [Governance evidence](architecture/governance-evidence.md) | Evidence reference/use, applicability and immutable evaluation-basis contract |
| [Challenge](architecture/challenge.md) | Game-owned challenge identity, admissibility, evaluation, attempt and comparison contract, independent of production semantics |
| [Operational continuity](architecture/operational-continuity.md) | Accountable operational continuation identity and continuity rules |
| [External representations](architecture/external-representations.md) | Serialization, transport and industrial-interchange boundaries |

## Internal research

| Document | What it covers |
|---|---|
| [Research area index](research/README.md) | Navigation and authority map for research surfaces |
| [Research operating model](development/researching.md) | Normative research rules: lifecycle, priority, promotion/reconciliation, investigation/review, evidence custody, synthesis seeds, decision-rationale retention, and register maintenance |
| [Research register](research/research-register.md) | Current admitted research questions, priority, lifecycle state, evidence artifact, expected destination, and review date |
| [Storage failure model](research/investigations/storage-failure-model.md) | READY question on stronger local durability, recovery and concurrency guarantees |
| [Storage historical support](research/investigations/storage-historical-support.md) | CANDIDATE question; promote only when a concrete retained use and support horizon bound the cross-definition resolution need |
| [Synthesis seeds](research/synthesis-seeds.md) | Current retained non-authoritative cross-investigation synthesis signals |
| [Research brief template](research/brief-template.md) | Reusable advisory failure-oriented planning structure for bounding research questions; it does not add `READY` criteria |
| [Research report template](research/report-template.md) | Reusable structure for decision-quality research reports |
| [Agency and decision boundary](research/investigations/agency-decision-boundary.md) | Actor/controller/subject/capability/operation/replay investigation — **concluded**; durable rules live in [Attribution and decision boundaries](architecture/overview.md#attribution-and-decision-boundaries) |
| [Factory Design evolution](research/investigations/factory-design-evolution.md) | Equipment ontology, diagnostics, comparison, shared drafts, resource groups, later Factory evolution |
| [Factory-design game product programme](research/investigations/factory-design-game-vertical-slice.md) | Umbrella framing for focused game research; no programme-level lifecycle |
| [Game diagnostic evidence contract](research/investigations/factory-design-game-diagnostic-evidence.md) | CONCLUDED bounded positive: truthful, inspectable non-spatial diagnostic claims are achievable; promoted requirements held in the investigation, owner smoke test only, no method admitted |
| [Game strategy space](research/investigations/factory-design-game-strategy-space.md) | CONCLUDED bounded positive for a constructive non-spatial capacity/capital reference challenge; shared eligibility is constructive, not proven necessary |
| [Transfer semantics](research/investigations/transfer-semantics.md) | READY decided spatial record and transfer lifecycle, not implemented, plus CANDIDATE non-spatial hand-off/timing follow-up |
| [Engine evolution](research/investigations/engine-evolution.md) | Lot/batch, capability/pools, dispatch-policy, and unselected recovery/session extensions |
| [Operational/Digital Twin boundaries](research/investigations/operational-execution-digital-twin-boundaries.md) | Durable operational identity, trust/authority, external realization, correspondence, reconciliation and proving-case research |
| [Governance change control](research/investigations/governance-change-control.md) | CANDIDATE: what must be settled before governed change, exceptions, mappings or audit projections are implemented |
