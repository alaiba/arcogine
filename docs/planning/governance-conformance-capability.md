# Governance and Conformance Capability Implementation Plan

> **Status:** Active; PLAN-GOV-1 complete, PLAN-GOV-2 initial slice complete, PLAN-GOV-3 complete, PLAN-GOV-4 initial slice complete; PLAN-GOV-5 is `READY_NEXT`
> **Scope:** Implementation admission and sequencing for evidence, governed change, exceptions, mappings, and audit projections over the landed identity/change/conformance substrate
> **Authority:** Planning only; durable semantics remain owned by Governance architecture and its adopted contracts  
> **Related:** [Governance Architecture](../architecture/governance-conformance.md), [Identity/History Compatibility Guard](governance-continuity.md), [Governance evidence contract](../architecture/governance-evidence.md), [deterministic simulation decision](../architecture/decisions/deterministic-simulation.md), [Operational continuity contract](../architecture/operational-continuity.md), [Factory publication identity contract](../architecture/factory-design.md#11-publication-identity-and-provenance), [Factory Model v1 specification](../architecture/factory-model-v1.md), [controlled revision contract](../architecture/controlled-revisions.md)

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

Provides durable semantic identity, opaque historical revision identity, authoritative history, lineage, recording provenance, and exact historical semantic-state resolution.

Downstream work must preserve the [Identity/History Compatibility Guard](governance-continuity.md).

### PLAN-GOV-2 — Semantic ChangeSet and impact — COMPLETE (initial slice)

Provides domain-neutral semantic add/remove/modify changes, stable affected-entity references/impact scope, change provenance, and optional external change reference association. Factory semantic comparison proves the first domain use.

Do not replace this with generic text/JSON diff or source-control semantics.

### PLAN-GOV-3 — Requirement/assertion contract — COMPLETE

Provides versioned requirements/assertions, scopes, catalogue selection against real impact scope, and exact external requirement source/provenance where evaluation depends on it.

### PLAN-GOV-4 — Conformance evaluation and findings — COMPLETE (initial slice)

Provides deterministic pre-change evaluation over explicit subject/revision/requirement/assertion inputs with attributable evaluation result and findings.

The initial slice does not claim durable historical persistence of every evaluation/evidence record, and its `ConformanceEvaluation` is a deterministic value without occurrence identity; evaluation-occurrence identity and its acceptance boundary are PLAN-GOV-5 work under the Governance evidence contract.

## 3. Current implementation queue

```text
PLAN-GOV-5  Evidence and EvidenceUse                         READY_NEXT
    |
    v
PLAN-GOV-6  Governed change / authorization / external workflow association  downstream
    |
    v
PLAN-GOV-7  Exceptions and risk acceptance
    |
    v
PLAN-GOV-8  Framework/control mappings
    |
    v
PLAN-GOV-9  Audit snapshots / compliance projections
```

## 4. PLAN-GOV-5 — Evidence and EvidenceUse — `READY_NEXT`

PLAN-GOV-5 is admitted for implementation. Its semantic contract is fixed by
the [Governance evidence contract](../architecture/governance-evidence.md)
after decision-quality research and an independent adversarial review, and is summarized in
[Governance architecture](../architecture/governance-conformance.md) §7, §9, and §12. Every one of
the earlier promotion criteria has landed on `main`: the research question is concluded, the
high-risk conclusion was independently reviewed, the surviving contract is Accepted durable
authority, and this plan and the current-state architecture are reconciled to it. This section
derives the implementation responsibility and acceptance evidence from that decision; it does not
restate or extend the decision, and the Governance evidence contract remains the authority wherever the two could be read
differently.

### Responsibility

Implement the first **headless** Governance evidence capability: the generic evidence
reference/use contract and the evaluation occurrence that anchors it, composed additively with the
landed `Requirement`/`Assertion`/`ConformanceEvaluation` values.

The slice must provide, in whatever representation it chooses:

- an **evidence reference** whose equality is same-source/result-revision identity and which is
  never rebound (the Governance evidence contract §2–§3), able to be satisfied by a producer-owned handle without a second
  Governance-allocated identifier;
- an **evidence use** relationship carrying the target subject (fingerprint, optional verified
  revision, scope), the exact requirement/assertion definitions, the role (relied on, considered but
  not relied on, comparator), the temporal frame where material, and an attributable
  applicability/reliance determination (the Governance evidence contract §4, §6);
- an **evaluation occurrence** with its own identity and a fixed basis — exact definitions, subject,
  relied-on uses, material exclusions and known gaps, temporal frame, applicability rules, outcome
  — that becomes authoritative at an acceptance boundary and is never mutated afterwards
  (the Governance evidence contract §8–§9);
- the conformance composition: an applicable assertion with an adequate, applicable basis may
  `PASS`; unusable or missing evidence never yields `PASS` and never alone yields `NOT_APPLICABLE`;
  absence supports `FAIL` only where the assertion's own semantics establish a violation from
  adequate evidence of absence (the Governance evidence contract §6).

The `Evidence`/`EvidenceUse` entries in `GovernanceModuleBoundaryTest`'s forbidden-declaration guard
protected the blocked state and are retired by this slice; its authorization, deployment, severity,
and risk-acceptance entries stay in force for their own later slices, and production Governance must
still depend only on `:types`.

### Scope of the first slice

Bounded by the Governance evidence contract §12: prove the contract with producer identities and provenance that actually
exist, or with explicit fixtures at the owning seam, and state which provenance class is proved.

- **Structural facts** are the production-backed class: authoritative controlled revision plus
  `ModelFingerprint` supply a real producer identity, and the model version is the evidence's own
  provenance.
- **Arcogine-derived analytical results** may be proved through an explicitly attributed fixture
  carrying producer-owned `ModelFingerprint`, `EngineSemanticsVersion`, run/result identity, and
  explicit inputs. `FactoryRuntime` exposes a fixed `EngineSemanticsVersion`, but durable Engine
  result identity and observation/event provenance propagation are not yet established and
  analytical-definition ownership is an open research question, so this slice must not claim a
  production Engine or analytics integration and must never infer or stamp a missing semantics
  version.
- **External observations** may be proved only through a fixture that retains source
  identity/subject/time/trust provenance and an explicit correspondence assertion supplied at the
  seam. No Operational observation type, ingestion, correspondence authority, or trust semantics is
  implemented or implied.
- Durable requirement-definition storage and durable evaluation-history persistence are not yet
  implemented. The slice must satisfy the definition-resolution and occurrence-basis obligations for
  the material it actually accepts (an in-memory or fixture-backed authority is acceptable), must
  disclose rather than substitute when material is missing, and must not claim durable historical
  reconstruction beyond what its acceptance authority actually retains.

### Acceptance evidence

Executable acceptance must demonstrate at least the discriminating cases the Governance evidence contract was tested
against, distinguishing fixture-proved seams from production integrations:

1. **Revision reuse / rollback** — one evidence item used against two controlled revisions with equal
   fingerprints (and against a different fingerprint) yields distinct uses with independent
   applicability; nothing is copied from one target to the other.
2. **Duplicate delivery versus independent corroboration** — redelivery of the same source record
   resolves to the same evidence and does not inflate independent support; equal payloads from
   distinct producers/occurrences remain distinct evidence.
3. **Late correction** — a correction or retraction is a new attributable relationship; the earlier
   occurrence's basis is unchanged and a later occurrence can reach a different result while naming
   its relationship to the earlier one.
4. **Unusable evidence** — stale, out-of-period, wrong-subject, or incompatible material is recorded
   as considered-but-not-relied-on with its reason, cannot produce `PASS`, and cannot make an
   applicable requirement `NOT_APPLICABLE`; a sufficient alternative set may still `PASS`.
5. **Missing evidence versus record existence** — absence yields `UNKNOWN` with an explainable gap,
   except for an assertion whose semantics establish a violation from adequate evidence of absence.
6. **External observation before correspondence** — a fixture observation retains source provenance
   with no Arcogine subject at ingestion; the use carries the explicit correspondence decision.
7. **Analytical result** — a fixture result retains producer-owned provenance unchanged; a result
   with no `EngineSemanticsVersion` remains explicitly unresolved rather than being stamped.
8. **Source model versus use target** — a result produced for one model is used as a comparator
   for another only through an explicit comparator role.
9. **Definition rebinding** — a later requirement/assertion wording or rule change under the same
   identity plus version cannot change the definition a historical occurrence resolves to.
10. **Retired producer** — attribution survives loss of executability; missing required material is
    disclosed rather than substituted.
11. **Context-bound packaging** — if evidence and use are stored together or copied per evaluation,
    the independent source identity and use context remain distinguishable.
12. **Accumulating continuation** — a use target that is an operational continuation rather than a
    point identity is rejected or unrepresentable, not silently accepted.
13. **Evaluation occurrence** — two accepted occurrences with equal inputs and outcomes are distinct;
    an unaccepted `ConformanceEvaluation` value is not an occurrence; a rebind of an occurrence's
    basis is rejected.
14. **Boundary rejection** — malformed or falsely bound input is rejected explicitly rather than
    converted into a conformance result.

### Readiness non-goals

No telemetry ingestion, connector trust/authentication, document-management system, vector search,
generic evidence lake, production adapter, signature/PKI infrastructure, database or event-store
selection, generic verification framework, Engine analytics redesign, universal source taxonomy or
subject reference, or invented producer provenance belongs in this slice. Storage, identifier
scheme, serialization, and API field shapes are implementation choices for this slice only insofar
as they do not become persisted or public contracts; a hard-to-reverse choice among them needs its
own ADR per §12.

## 5. PLAN-GOV-6 — Governed change and external workflow integration

### Responsibility

Represent the Governance decision that a candidate controlled revision is authorized/rejected/otherwise dispositioned, while allowing an external workflow system to remain authoritative for organizational process state.

Keep separate:

- semantic `ChangeSet` / impact;
- conformance/evidence result;
- authorization/governance decision;
- optional external workflow reference; and
- any future Operational deployment/application record.

### Acceptance criteria

1. authorization references the exact controlled revision/candidate state;
2. authorization does not redefine revision identity or mutate the immutable revision core;
3. impact/conformance/evidence can be traced into the decision;
4. external workflow references are associations, not identity;
5. a later deployment can reference the authorization/revision without Governance owning target adapter/application mechanics; and
6. the decision remains historically attributable after later workflow/model changes.

## 6. PLAN-GOV-7 — Exceptions and risk acceptance

### Responsibility

Represent explicit governance disposition of known non-conformance without rewriting the underlying factual result.

### Acceptance criteria

- finding/conformance result remains immutable factual input;
- exception/risk acceptance records rationale and accountable actor/approver;
- effective/expiration time is explicit;
- expired exceptions become visible without history rewrite; and
- compensating controls/evidence may be linked explicitly.

## 7. PLAN-GOV-8 — Framework/control mappings

### Responsibility

Add conventional framework/control projection only after generic requirements/conformance/evidence/governed change work.

### Acceptance criteria

- requirements/controls/mappings are independently versioned;
- one control may map to several framework requirements;
- business/domain objects do not acquire framework-specific compliance booleans;
- historical evaluations do not silently change when mappings/framework versions change; and
- imported source text/content respects source/licensing constraints.

Start with one small legally usable proving set; broad framework coverage is not part of the first slice.

## 8. PLAN-GOV-9 — Audit snapshots and compliance projections

### Responsibility

Produce a reproducible historical projection over exact semantic state, controlled revision, requirement/assertion versions, evidence/evidence use, findings, decisions/exceptions, and mappings where applicable.

### Acceptance criteria

1. a historical result can be reconstructed from exact versioned inputs;
2. the system can explain pass/fail/unknown and supporting evidence;
3. evidence/exception applicability periods remain attributable;
4. later requirement/assertion/source/mapping changes do not silently reinterpret old snapshots; and
5. a reviewer can traverse from requirement/assertion through source, affected semantic state, evidence, finding, decision, and revision history.

## 9. End-to-end milestone

The next meaningful Governance milestone is:

> Take one proposed semantic change with an authoritative controlled revision and ChangeSet, select a real versioned requirement/assertion, evaluate it before authorization, attach explicit evidence/evidence use, record a separate authorization decision and external workflow reference where applicable, and later reconstruct why that revision was accepted or rejected.

Deployment is not required to prove this milestone; future Operational application remains a sibling responsibility.

## 10. Cross-track rules

- Factory/other domains own their semantic models and supply typed comparison/evidence where required.
- Engine runtime observations/events are not Governance evidence records by default.
- Future Operational external observations may be referenced through EvidenceUse without Governance ingesting telemetry or inferring subject correspondence.
- Implemented Governance contracts must be consumed by downstream tracks; fixtures may stand in only for genuinely unimplemented sibling-owned inputs.

## 11. Non-goals

Do not build broad framework libraries, auditor marketplaces, questionnaire automation, generic policy generation, trust-center surfaces, a monolithic business-object graph, a replacement for Jira/change-management systems, generic Git branch/merge semantics, telemetry ingestion, production commands, or digital-twin reconciliation merely to advance this plan.

## 12. Documentation/ADR rule

Update architecture/current-state docs only when capabilities land. Add an ADR only when implementation commits to a durable hard-to-reverse semantic/protocol/persistence contract not already governed by existing ADRs.

Untriggered lineage/source-control extensions and Operational ontology questions remain outside this implementation plan until concrete research/architecture work promotes them.
