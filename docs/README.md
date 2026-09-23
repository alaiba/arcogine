# Documentation

## Start here

| Document | Who it's for | What it covers |
|---|---|---|
| [Product Charter](product/charter.md) | Everyone | Enduring product thesis and principles |
| [Root README](../README.md) | Everyone | What Arcogine is today, setup, and local run |
| [Concepts](product/concepts.md) | New users | Current simulation concepts and behavior |

## Documentation hierarchy

Arcogine separates durable direction, current truth, research, and executable planning:

- **[`product/charter.md`](product/charter.md)** — normative product direction and enduring principles; not a roadmap.
- **[`architecture/overview.md`](architecture/overview.md)** — current architecture plus enduring architectural principles, and the cross-cutting constraints the focused specifications apply.
- **[`architecture/`](architecture/overview.md)** — focused architecture and specification documents owning exact domain contracts, identities, semantics and supported boundaries.
- **Current capability/reference docs** — [`product/concepts.md`](product/concepts.md) and consumer/component references describe what exists now.
- **[`research/`](research/README.md)** — research-area index linking the normative operating model, current research portfolio state, synthesis signals, investigation artifacts, and reusable brief/report structures. Research is not accepted architecture or implementation commitment.
- **[`planning/`](planning/README.md)** — implementation-ready delivery planning only: admitted slices, dependencies, blockers on concrete prerequisites, acceptance evidence, and implementation status.
- **[`history/`](history/README.md)** — dated, non-normative evidence retained for later comparison, including selected decision rationale; never current authority.

Research discovers. Product direction and canonical architecture/specifications establish durable meaning. Planning sequences concrete implementation. Landed code/tests and current-state docs establish what actually exists. Arcogine keeps no separate decision-record authority: the durable result of an architectural change is the changed canonical document, and Git and pull-request history preserve why it changed. Where the reasoning behind a significant choice is worth deliberate later retrieval, a concise [historical decision-rationale record](history/README.md#decision-rationale) may also preserve it; such a record explains a past choice and never defines current meaning.

A research conclusion does not become authoritative merely because it is recorded. An unresolved question that still determines implementation meaning must not be hidden inside a delivery plan.

### Delivery-coordinate boundary

Temporary delivery coordinates belong to executable planning/delivery context, not research or durable semantic naming. When research produces an implementable responsibility, promote the semantic conclusion first and then assign the delivery coordinate in `docs/planning/`.

## Cross-track ownership

| Concern | Primary authority / delivery surface | Boundary |
|---|---|---|
| Canonical production-system semantics, validation, publication, deterministic instantiation | [Factory design architecture](architecture/factory-design.md) / [Factory design plan](planning/factory-design-capability.md) | One published semantic model is the downstream source of truth |
| Deterministic workload, work items, dispatch, session, supported observations/events, spatial consequences | [Engine readiness](planning/factory-simulation-engine-readiness.md) | Simulation runtime truth; not production-control semantics |
| Durable semantic fingerprint/revision history, semantic change, requirements, conformance, evidence/governed change | [Governance architecture](architecture/governance-conformance.md) / [Governance plan](planning/governance-conformance-capability.md) | Governance does not ingest telemetry or perform external actuation/reconciliation |
| Operational identity/trust, external realization, subject correspondence, external observations, reconciliation, drift/resilience | [Operational architecture](architecture/operational-execution-digital-twin.md) / [Operational research](research/investigations/operational-execution-digital-twin-boundaries.md) | No implementation is currently admitted until the required semantic boundaries are resolved |
| Game challenge identity, catalogue/economics, admissibility, evaluation, attempt comparison | [Challenge plan](planning/factory-design-game-challenge-readiness.md) | Headless game-owned rules; not production simulation |
| Playable factory-design product hypothesis | [Game vertical-slice research](research/investigations/factory-design-game-vertical-slice.md) | Product evidence first; implementation only after promotion into the consumer plan |

## Development and contributing

| Document | What it covers |
|---|---|
| [CONTRIBUTING.md](../.github/CONTRIBUTING.md) | Contribution workflow, style, validation |
| [reviewing.md](development/reviewing.md) | Independent PR review/re-review workflow |
| [semantic-contract-support.md](development/semantic-contract-support.md) | Owning support declarations, retained admission and promise-specific review evidence |
| [researching.md](development/researching.md) | Normative Arcogine research operating model: lifecycle/priority semantics, promotion/reconciliation, investigation and adversarial review, evidence custody, synthesis-seed handling, historical decision-rationale retention, and register maintenance |
| [continuous-improvement.md](development/continuous-improvement.md) | Continuous-improvement operating model: Session-close Kaizen, Consistency review, and delivery-process retrospective method |
| [consistency-review.md](development/consistency-review.md) | Recurring repository consistency-review operating model |
| [testing.md](development/testing.md) | Test categories, CI, quality gates, native commands |
| [repository-snapshot.md](development/repository-snapshot.md) | Canonical whole-repository retrieval snapshot |
| [codex-cloud.md](development/codex-cloud.md) | Codex Cloud environment/workflow notes |
| [coding-agent-evaluation.md](development/coding-agent-evaluation.md) | Non-normative coding-agent/model evaluation |
| [SECURITY.md](../.github/SECURITY.md) | Security policy and mature operational security boundary |

## Architecture and design

| Document | What it covers |
|---|---|
| [Architecture overview](architecture/overview.md) | Current design, modules, determinism and Events-State-Observations principles |
| [Factory design](architecture/factory-design.md) | Cross-consumer factory-model/design lifecycle semantics |
| [Governance and conformance](architecture/governance-conformance.md) | Revision/change/requirements/conformance/evidence/governed-change architecture |
| [Operational execution and digital twin](architecture/operational-execution-digital-twin.md) | Proposed relationship-based execution/reality architecture |
| [Engine Semantics v1](architecture/engine-semantics-v1.md) | Normative current result-affecting Engine interpretation |
| [Factory Model v2](architecture/factory-model-v2.md) | Normative, unreleased v2 canonicalization/fingerprint byte grammar: V1 production records plus an optional spatial record |
| [Standards alignment](architecture/standards-alignment.md) | Standards/interchange/conformance boundaries |
| [ISA-95 semantic mapping](architecture/isa-95-semantic-mapping.md) | Maintained manufacturing semantic mapping and deliberate divergences |
| [Factory Model v1](architecture/factory-model-v1.md) | Normative v1 canonicalization/fingerprint byte grammar (implemented) |
| [Runtime contract](architecture/runtime-contract.md) | Supported observations, ordered authoritative runtime events, provenance and transport boundary |
| [Controlled revisions](architecture/controlled-revisions.md) | Historical occurrence identity, lineage, provenance and acceptance |
| [Governance evidence](architecture/governance-evidence.md) | Evidence reference/use, applicability and immutable evaluation-basis contract |
| [Operational continuity](architecture/operational-continuity.md) | Accountable operational continuation identity and continuity rules |
| [External representations](architecture/external-representations.md) | Serialization, transport and industrial-interchange boundaries |

## Internal research

| Document | What it covers |
|---|---|
| [Research area index](research/README.md) | Navigation and authority map for research surfaces |
| [Research operating model](development/researching.md) | Normative research rules: lifecycle, priority, promotion/reconciliation, investigation/review, evidence custody, synthesis seeds, decision-rationale retention, and register maintenance |
| [Research register](research/research-register.md) | Current admitted research questions, priority, lifecycle state, evidence artifact, expected destination, and review date |
| [Synthesis seeds](research/synthesis-seeds.md) | Current retained non-authoritative cross-investigation synthesis signals |
| [Research brief template](research/brief-template.md) | Reusable advisory failure-oriented planning structure for bounding research questions; it does not add `READY` criteria |
| [Research report template](research/report-template.md) | Reusable structure for decision-quality research reports |
| [Agency and decision boundary](research/investigations/agency-decision-boundary.md) | Actor/controller/subject/capability/operation/replay investigation — **concluded**; durable rules live in [Attribution and decision boundaries](architecture/overview.md#attribution-and-decision-boundaries) |
| [Factory Design evolution](research/investigations/factory-design-evolution.md) | Equipment ontology, diagnostics, comparison, shared drafts, resource groups, later Factory evolution |
| [Factory-design game vertical slice](research/investigations/factory-design-game-vertical-slice.md) | Product loop, diagnostics, reference challenge, scoring/tutorial/technology evidence |
| [Engine evolution](research/investigations/engine-evolution.md) | Lot/batch, capability/pools, dispatch-policy, Engine applicability to optional-record Factory policies, and unselected recovery/session extensions |
| [Operational/Digital Twin boundaries](research/investigations/operational-execution-digital-twin-boundaries.md) | Durable operational identity, trust/authority, external realization, correspondence, reconciliation and proving-case research |

## Internal implementation planning

See [planning/README.md](planning/README.md) for the admission rule.

| Document | What it covers |
|---|---|
| [Factory Design capability](planning/factory-design-capability.md) | Current canonical-model baseline and admitted v2 implementation work |
| [Factory Simulation Engine readiness](planning/factory-simulation-engine-readiness.md) | Completed runtime core (outward consumer convergence retired as an objective) plus current spatial implementation queue |
| [Spatial runtime consequences](planning/spatial-runtime-consequences.md) | Detailed accepted spatial/Engine-semantics implementation sequence |
| [Governance/conformance capability](planning/governance-conformance-capability.md) | Landed Governance substrate and headless evidence-use capability; durable producer integrations and later authorization remain future work |
| [Governance identity/history compatibility guard](planning/governance-continuity.md) | Downstream implementation invariants over completed revision identity/history |
| [Challenge delivery](planning/factory-design-game-challenge-readiness.md) | Closed headless challenge sequence and downstream invariants |
| [Game consumer](planning/factory-design-game-consumer.md) | Settled ownership/integration boundary and playable implementation admission criteria |
| [Game vertical-slice implementation gate](planning/factory-design-game-vertical-slice.md) | Explicit gate from product research into playable implementation |
| [Operational implementation admission](planning/operational-execution-digital-twin-readiness.md) | Concrete conditions that must be met before an Operational delivery slice is created |

## Examples

[Examples](examples/README.md) are executable TOML scenario fixtures for education/testing; they are not runtime distribution assets.
