# Governance and Conformance Capability Implementation Plan

> **Status:** Landed substrate; no further slice admitted. PLAN-GOV-1 and PLAN-GOV-3 are complete; PLAN-GOV-2, PLAN-GOV-4 and PLAN-GOV-5 are complete as initial/headless slices. Governed change, exceptions, framework mappings and audit projections are **not admitted**  
> **Scope:** The landed Governance identity/change/conformance/evidence substrate, the constraints later Governance work must preserve, and the conditions for admitting it  
> **Authority:** Planning only; durable semantics remain owned by Governance architecture and its adopted contracts  
> **Related:** [Governance Architecture](../architecture/governance-conformance.md), [Identity/History Compatibility Guard](governance-continuity.md), [Governance evidence contract](../architecture/governance-evidence.md), [Determinism Contract](../architecture/overview.md#determinism-contract), [Operational continuity contract](../architecture/operational-continuity.md), [Factory publication identity contract](../architecture/factory-design.md#11-publication-identity-and-provenance), [Factory model specification](../architecture/factory-model.md), [controlled revision contract](../architecture/controlled-revisions.md)

## 1. Boundary

Governance derives decisions from explicit semantic state, change, requirements, assertions, evidence, and accountable workflow records. It does not become telemetry ingestion, production command execution, or a framework-specific checklist engine.

```text
semantic artifact + controlled revision
        |
        v
semantic ChangeSet / impact
        |
        v
requirement + assertion
        |
        v
conformance evaluation / finding
        |
        v
evidence / evidence use
        |
        v
governed change / authorization / exception
        |
        v
optional framework mappings / audit projection
```

## 2. Landed substrate

### PLAN-GOV-1 — Durable fingerprint and controlled revision history — COMPLETE

Provides the semantic-fingerprint seam, opaque historical revision identity, append-only history, lineage, recording provenance, and exact semantic-state resolution. The Governance authority port is realized by [built-in Storage](../architecture/storage.md) within one Factory definition binding. Stronger retained historical support requires an explicit scoped declaration under the [semantic evolution rules](../architecture/overview.md#semantic-evolution-and-support).

Downstream work must preserve the [Identity/History Compatibility Guard](governance-continuity.md).

### PLAN-GOV-2 — Semantic ChangeSet and impact — COMPLETE (initial slice)

Provides domain-neutral semantic add/remove/modify changes, stable affected-entity references/impact scope, change provenance, and optional external change reference association. Factory semantic comparison proves the first domain use.

Do not replace this with generic text/JSON diff or source-control semantics.

### PLAN-GOV-3 — Requirement/assertion contract — COMPLETE

Provides versioned requirements/assertions, scopes, catalogue selection against real impact scope, and exact external requirement source/provenance where evaluation depends on it.

### PLAN-GOV-4 — Conformance evaluation and findings — COMPLETE (initial slice)

Provides deterministic pre-change evaluation over explicit subject/revision/requirement/assertion inputs with attributable evaluation result and findings. `ConformanceEvaluation` remains a deterministic value without occurrence identity; occurrence identity and its acceptance boundary come from the evidence slice below.

### PLAN-GOV-5 — Evidence and EvidenceUse — COMPLETE (headless slice)

Implements the [Governance evidence contract](../architecture/governance-evidence.md) headlessly: source-qualified, never-rebound evidence references; contextual evidence uses with explicit target, role, temporal frame and applicability; and accepted, immutable evaluation occurrences with a fixed basis, composed additively with the landed requirement, assertion and evaluation values. Structural facts are proved with a real producer binding (an accepted controlled revision plus `ModelFingerprint`); Arcogine-derived analytical results and external observations are proved only through explicitly attributed fixtures, with no exact Engine-definition identifier invented.

It does not claim a production Engine, analytics or Operational integration, durable evidence or requirement-definition storage, or durable evaluation-history persistence. Executable evidence: `GovernanceEvidenceTest` and the Governance conformance suites.

## 3. Further Governance capability — not admitted

[Governance architecture](../architecture/governance-conformance.md) describes the capabilities beyond the landed substrate: governed change with authorization and external workflow association (§6, §11), exceptions and risk acceptance (§10), framework/control mappings (§8), and reproducible audit projections (§12). None is an admitted implementation slice.

They are not admitted because each still depends on something unsettled or unlanded. Authorization and exceptions attribute accountable decisions, and the [Attribution and decision boundaries](../architecture/overview.md#attribution-and-decision-boundaries) settle roles and provenance but not a shared actor identity, which remains a research candidate. A reproducible audit projection needs durable evaluation history that does not exist. The architecture lists the exception and risk-acceptance lifecycle, and authority protocols with an external governance system, as reconciliation triggers that are still open (§16).

### Constraints any promoted slice must preserve

- An authorization decision references the exact controlled revision or candidate state, never redefines revision identity, and never mutates the immutable revision core.
- Semantic impact, conformance results and evidence remain traceable into a governance decision, which stays historically attributable after later workflow or model changes.
- An external workflow reference is an association, never identity. A later deployment references the authorization/revision without Governance owning application mechanics.
- An exception or risk acceptance never rewrites the underlying finding. Its rationale, accountable approver, and effective and expiration times are explicit, and expiry becomes visible without rewriting history.
- Framework/control mappings are versioned independently, never become compliance booleans on business or domain objects, and never silently change historical evaluations. Imported framework text respects source and licensing constraints.
- An audit projection is reconstructed from exact versioned inputs, explains its result and evidence, and is never silently reinterpreted by later requirement, source or mapping changes.
- `GovernanceModuleBoundaryTest` keeps authorization, deployment, severity and risk-acceptance declarations out of production Governance until a slice admits them, and production Governance depends only on `:types`.

### Promotion criteria

The next Governance delivery coordinate may be created only after:

1. the exact semantic referent of the proposed slice is settled, including how it attributes accountable decisions;
2. any hard-to-reverse choice it commits to among the architecture's reconciliation triggers is reconciled into the owning architecture or specification;
3. its prerequisites are explicit and landed or deliberately fixture-backed;
4. a concrete requirement names the use, under the Charter's [core-capability principle](../product/charter.md#6-enduring-product-principles); and
5. executable acceptance evidence is defined.

When those conditions are met, add the narrowest concrete implementation slice here. Do not restore a speculative multi-step Governance sequence merely as a roadmap placeholder.

## 4. Cross-track rules

- Factory/other domains own their semantic models and supply typed comparison/evidence where required.
- Engine runtime observations/events are not Governance evidence records by default.
- Future Operational external observations may be referenced through EvidenceUse without Governance ingesting telemetry or inferring subject correspondence.
- Implemented Governance contracts must be consumed by downstream tracks; fixtures may stand in only for genuinely unimplemented sibling-owned inputs.

## 5. Non-goals

Do not build broad framework libraries, auditor marketplaces, questionnaire automation, generic policy generation, trust-center surfaces, a monolithic business-object graph, a replacement for Jira/change-management systems, generic Git branch/merge semantics, telemetry ingestion, production commands, or digital-twin reconciliation merely to advance this plan.

## 6. Documentation reconciliation rule

Update architecture/current-state docs only when capabilities land. Reconcile the owning architecture or specification when implementation commits to a durable hard-to-reverse semantic/protocol/persistence contract that the current contracts do not already fix.

Untriggered lineage/source-control extensions and Operational ontology questions remain outside this implementation plan until concrete research/architecture work promotes them.
