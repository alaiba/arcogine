# Governance change control beyond the landed substrate

Status: CANDIDATE brief; no conclusion — promote only when a concrete governed-change use names its requirement

## Question and decision

What must be settled before Arcogine implements Governance capability beyond its landed identity, change, requirement, conformance and evidence substrate: governed change with authorization and external workflow association, exceptions and risk acceptance, framework/control mappings, and reproducible audit projections?

The [Governance architecture](../../architecture/governance-conformance.md) describes these capabilities (§6, §8, §10, §11, §12). This question decides which of them a concrete use actually needs first, and what still blocks an implementation-ready conclusion for it.

## Landed baseline

The landed substrate is described in [Governance architecture §13](../../architecture/governance-conformance.md#13-relationship-to-current-factory-model-and-operational-work) and the [Governance evidence contract](../../architecture/governance-evidence.md): durable fingerprint and controlled-revision history realized by built-in [Storage](../../architecture/storage.md), semantic change and impact, versioned requirements and assertions, deterministic pre-change evaluation and findings, and headless evidence use with accepted evaluation occurrences. It has no durable evaluation history, durable requirement-definition storage, or production producer integration.

## Known blockers

- **Accountable decisions.** Authorization and exceptions attribute decisions to an accountable approver. The [Attribution and decision boundaries](../../architecture/overview.md#attribution-and-decision-boundaries) settle roles and provenance, but not a shared actor identity, which is a separate candidate question in the [research register](../research-register.md).
- **Historical reconstruction.** A reproducible audit projection needs durable evaluation history and definition resolution that do not exist yet; stronger retained history is the [historical storage support](storage-historical-support.md) question.
- **Open reconciliation triggers.** The architecture lists the exception and risk-acceptance lifecycle, and authority protocols with an external governance system, among the hard-to-reverse choices still to be reconciled (§16).

## Constraints any conclusion must preserve

- An authorization decision references the exact controlled revision or candidate state, never redefines revision identity, and never mutates the immutable revision core.
- Semantic impact, conformance results and evidence remain traceable into a governance decision, which stays historically attributable after later workflow or model changes.
- An external workflow reference is an association, never identity. A later deployment references the authorization or revision without Governance owning application mechanics.
- An exception or risk acceptance never rewrites the underlying finding. Its rationale, accountable approver, and effective and expiration times are explicit, and expiry becomes visible without rewriting history.
- Framework/control mappings are versioned independently, never become compliance booleans on business or domain objects, and never silently change historical evaluations. Imported framework text respects source and licensing constraints, and a first proving set stays small and legally usable.
- An audit projection is reconstructed from exact versioned inputs, explains its result and evidence, and is never silently reinterpreted by later requirement, source or mapping changes.
- Production Governance keeps depending only on `:types`; `GovernanceModuleBoundaryTest` keeps authorization, deployment, severity and risk-acceptance declarations out of it until an implementation includes them.

## Promotion trigger and exit

Promote this brief when a concrete use — under the Charter's [core-capability principle](../../product/charter.md#6-enduring-product-principles), or a selected product direction — names the governance decision it needs recorded and later explained. Then narrow the question to that capability, settle its accountable-decision attribution and any §16 choice it commits to, and conclude with an [implementation-ready](../../development/researching.md#implementation-handoff) handoff or a narrower question. Authority semantics are high risk: independent adversarial review precedes any architecture reconciliation.
