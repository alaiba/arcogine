# Governance and Conformance Capability Implementation Plan

> **Status:** Active; PLAN-GOV-1 complete, PLAN-GOV-2 initial slice complete, PLAN-GOV-3 complete, PLAN-GOV-4 initial slice complete; PLAN-GOV-5 is the next admitted Governance slice  
> **Scope:** Implementation-ready sequence for evidence, governed change, exceptions, mappings, and audit projections over the landed identity/change/conformance substrate  
> **Authority:** Planning only; durable semantics remain owned by Governance architecture and accepted ADRs  
> **Related:** [Governance Architecture](../architecture/governance-conformance.md), [Identity/History Compatibility Guard](governance-continuity.md), [ADR-0004](../architecture/decisions/0004-model-identity-revision-lineage-and-external-change-control.md), [ADR-0006](../architecture/decisions/0006-durable-semantic-fingerprint-contract.md), [ADR-0008](../architecture/decisions/0008-controlled-revision-identity-and-lineage.md)

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

The initial slice does not claim durable historical persistence of every evaluation/evidence record; that is downstream work.

## 3. Current implementation queue

```text
PLAN-GOV-5  Evidence and EvidenceUse
    |
    v
PLAN-GOV-6  Governed change / authorization / external workflow association
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

## 4. PLAN-GOV-5 — Evidence and EvidenceUse

### Responsibility

Represent independently attributable evidence and the explicit act of using that evidence for a particular requirement/assertion/evaluation context.

The implementation must distinguish:

```text
Evidence
    independent source fact/artifact/provenance

EvidenceUse
    interpretation/application of that evidence
    to a subject/revision/requirement/assertion/evaluation
```

Raw Operational observations, when they later exist, remain Operational facts with their own source/subject/time/trust provenance. Governance may reference them as evidence without rebinding them at ingestion to a model/revision.

### Acceptance criteria

PLAN-GOV-5 is ready to close when:

1. evidence has stable identity and source/provenance appropriate to its type;
2. one evidence item can be reused in several explicit EvidenceUse records without rewriting the evidence;
3. EvidenceUse identifies the exact semantic subject/revision and requirement/assertion/evaluation context it supports;
4. time/applicability and source version are explicit when material;
5. missing/stale/inapplicable evidence produces explicit unknown/failure semantics rather than implicit pass;
6. external observations can be fixture-backed without inventing duplicate Operational telemetry types; and
7. historical evaluation remains attributable after later semantic or evidence changes.

### Non-goals

No telemetry ingestion, connector trust/authentication, document-management system, vector search, generic evidence lake, or production adapter belongs in this slice.

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
