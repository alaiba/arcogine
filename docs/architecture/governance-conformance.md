# Governance and Conformance Architecture

> **Status:** Proposed architectural reference  
> **Scope:** Cross-domain model history, semantic change, requirements, conformance, evidence, and governance over Arcogine's canonical business semantics  
> **Authority:** Proposed architecture; this document does not claim current compliance, audit, or certification capability  
> **Related:** [Product Charter](../product/charter.md), [Architecture Overview](overview.md), [Factory Design Architecture](factory-design.md), [ADR-0003](decisions/0003-canonical-factory-model-boundary.md), [Standards Alignment](standards-alignment.md), [Governance and Conformance Capability Plan](../planning/governance-conformance-capability.md)

## 1. Architectural position

Arcogine's Product Charter makes verification, provenance, governed change, and continuity between design and reality first-class concerns. Once Arcogine owns authoritative semantic models of a business and their history, governance and compliance can be derived from those models rather than maintained as disconnected checklists.

The architectural direction is therefore broader than a compliance product:

> **Arcogine should provide a generic conformance capability over authoritative business semantics, model history, and observations. Regulatory compliance, standards conformance, architecture governance, internal policy, contractual requirements, and operational assurance are projections over that capability.**

This avoids turning the core model into a collection of framework-specific flags such as `soc2Compliant`. Business objects describe the business. Requirements, controls, assertions, evidence, and findings interpret those objects for a particular governance purpose.

## 2. Authority: modeled truth and observed truth are different

Arcogine should be authoritative for the business semantics it owns: intended structure, policy, responsibilities, constraints, model lineage, and governed changes. External systems may remain authoritative for operational facts that Arcogine observes.

Conceptually:

```text
Arcogine model
    "this account SHALL use MFA"

External identity provider
    "this account DID use MFA"

Intended state + observed state
              |
              v
          Conformance
```

A model assertion is therefore not automatically operational evidence. A requirement can depend on one or more evidence classes:

```text
Structural evidence
    derived from authoritative Arcogine model state

Operational evidence
    observations from runtime systems, equipment, cloud providers, identity systems, etc.

Human/process evidence
    approvals, attestations, reviews, artifacts, and completed governed workflows
```

Arcogine should preserve the provenance and authority of each source rather than flattening them into one undifferentiated truth store.

## 3. Compliance is a downstream projection

The primitive dependency direction should be:

```text
Authoritative business semantics
            |
            v
   Model history / lineage
            |
            v
      Semantic changes
            |
            v
       Requirements
            |
            v
    Conformance assertions
            |
            v
    Findings and evidence
            |
            v
 Governance / risk / controls
            |
            v
 Framework requirement mappings
            |
            +--> SOC 2
            +--> ISO 27001
            +--> ISO 9001
            +--> internal policy
            +--> customer commitments
            +--> architecture standards
```

Framework content should not drive the shape of the business model. One underlying business fact or control may satisfy multiple framework requirements.

## 4. Durable identity and lineage precede audit claims

The current factory-model publication boundary proves that immutable semantic content can be validated, published, identified, and attributed to runtime behavior. A compliance- or audit-grade system requires a stronger durable lifecycle.

Conceptually:

```text
ModelVersion
    model identity
    immutable semantic content
    content hash
    parent version(s)
    schema/model version
    created/published at
    author/decision source
    change set
    external change request
    approvals
    provenance
```

A content hash answers whether semantic content is equal under a defined canonicalization policy. It does not by itself answer why a version exists, what changed, who authorized it, or which version preceded it. Those are lineage and governance concerns.

The system should eventually be able to answer:

```text
What was true at time T?
Which model version represented it?
What changed from the previous version?
Why was that change proposed?
Who or what approved it?
Which requirements were affected?
Which observations or evidence supported the resulting state?
```

## 5. Semantic change is a first-class governance primitive

Text, JSON, or serialized-object diffs are insufficient for business governance. Arcogine should represent consequential changes in domain terms when a concrete workflow requires shared change semantics.

For example:

```text
DataStoreMoved
    datastore: CustomerRecords
    fromRegion: EU
    toRegion: US
```

is more useful to governance than:

```diff
- "region": "EU"
+ "region": "US"
```

because the semantic change can drive impact analysis:

```text
ChangeSet
   |
   +--> affected business objects
   +--> affected requirements
   +--> affected controls
   +--> required reviewers
   +--> required evidence
   +--> invalidated prior evidence
```

This extends the factory-design distinction between model-revision history and runtime event history. A design/change event is not a simulated or operational event merely because both describe changes.

## 6. External change-management systems remain workflow authorities where appropriate

Arcogine does not need to replace Jira or another enterprise change-management system. A change request may be initiated, assigned, discussed, approved, and tracked in Jira while Arcogine owns the semantic meaning and impact of the proposed business change.

A target integration can look like:

```text
Jira Change Request
        |
        v
Arcogine ChangeSet
        |
        +--> affected model entities
        +--> affected requirements / controls
        +--> required validation and reviewers
        |
        v
Approval / authorization
        |
        v
Model version N --> model version N+1
        |
        v
Generated provenance / evidence
```

The external ticket identity should be retained as provenance. Arcogine should not duplicate ticket comments, workflow metadata, or project-management state unless those facts become necessary to a cross-system governance contract.

## 7. Generic conformance model

The core verification capability should answer whether a defined scope satisfies explicit requirements.

Conceptually:

```text
Requirement
    id
    description
    source/version

Assertion
    scope
    expression/evaluator
    evidence requirements

Evaluation
    requirement/assertion version
    model version
    observed-at / applicable period
    result
    evidence set

Finding
    affected entities
    severity
    explanation
    remediation state
```

A simple structural example:

```text
Requirement:
    Production resources require accountable ownership.

Scope:
    Resource where environment == Production

Assertion:
    owner != null

Evidence:
    authoritative Arcogine model version
```

An operational example may require external observation:

```text
Requirement:
    Production databases are encrypted at rest.

Intended state:
    Arcogine policy/model

Observed state:
    cloud-provider configuration evidence

Result:
    PASS / FAIL / UNKNOWN
```

`UNKNOWN` is important: absence of evidence must not silently become either success or failure when the underlying fact is genuinely unobserved.

## 8. Controls and frameworks are mappings, not business truth

Controls should describe how requirements are implemented and verified without becoming properties of the underlying business objects.

Conceptually:

```text
Framework
    |
    v
Requirement
    |
 satisfied by
    v
Control
    |
 implemented by
    +--> BusinessObject / Process / Policy
    |
 verified by
    +--> Assertion(s)
    |
 evidenced by
    +--> Evidence
```

One control can map to multiple requirements across multiple frameworks. Framework upgrades should therefore change mappings and requirement versions without forcing unrelated business objects to acquire new schema fields.

## 9. Evidence must be attributable and temporal

Evidence should be modeled with enough provenance to support historical reconstruction and audit review.

An external observation does not intrinsically belong to one Arcogine model fingerprint or revision. An AWS configuration snapshot, an IdP login log, or a Jira approval artifact has its own authority, its own observation time, and often its own applicable period, independent of which Arcogine model version happens to exist when it is captured or used. Binding the observation itself to one model version either forces duplicating identical evidence across every subsequent version or misrepresents the observation's actual provenance. Arcogine should therefore separate the evidence itself from any particular evaluation's use of it:

```text
Evidence
    evidenceId
    source
    provenance
    observedAt
    applicableFrom / applicableUntil
    external identity or artifact reference
    integrity metadata where required

EvidenceUse (a.k.a. EvaluationEvidence)
    evidenceId
    evaluation / assertion reference
    modelVersion (or controlled revision, once G1 exists)
    scope at time of use
    applicability determination
```

`Evidence` is the source-level fact: what was observed, by what authority, when, and over what period it applies. It does not carry a model version. `EvidenceUse` is the binding: which evaluation consumed that evidence, against which model version/revision, and why it was judged applicable to that scope at that time. One `Evidence` record may be referenced by many `EvidenceUse` records across multiple model versions, as long as each use's scope/applicability determination independently holds.

Structural evidence derived directly from Arcogine's own authoritative model state is the one case where binding to a model version at the source is natural — the model version *is* the evidence's provenance, so `Evidence` and `EvidenceUse` may collapse into one record for that case. External evidence should generally be bound to model/revision identity at evaluation/use time (`EvidenceUse`), not at source-observation time (`Evidence`).

Arcogine should distinguish evidence generated from its own authoritative state from evidence observed externally. Evidence reuse across requirements is valid only when scope, applicable period, provenance, and semantic meaning remain compatible.

## 10. Findings, exceptions, and risk acceptance are explicit governance state

Conformance is not always binary operational enforcement. A failed assertion may produce a finding that is remediated, accepted temporarily, or explicitly excepted.

Conceptually:

```text
Finding
    assertion
    affected objects
    severity
    detected at
    remediation

Exception / RiskAcceptance
    finding or control
    rationale
    owner/approver
    effective period
    expiration
    compensating controls where applicable
```

An exception does not rewrite the underlying assertion into success. It records an authorized governance decision about a known non-conformance.

## 11. Pre-change conformance is strategically important

Continuous monitoring after a change is useful, but Arcogine's stronger opportunity is to evaluate a proposed semantic change before it becomes authoritative.

```text
Current model version
        |
 Proposed ChangeSet
        |
        v
Impact analysis
        |
        +--> affected entities
        +--> affected requirements
        +--> evidence invalidation
        +--> required approvals
        |
        v
Pre-change conformance evaluation
        |
   approve / reject / revise
```

This connects design, verification, change management, and compliance through one semantic representation. It is also directly useful outside regulatory compliance: architecture standards, safety rules, production constraints, internal policy, and customer commitments can use the same mechanism.

## 12. Audit snapshots are reproducible interpretations

An audit view should be reconstructible from versioned inputs rather than stored only as an opaque dashboard state.

Conceptually:

```text
AuditSnapshot
    model version
    framework / requirement version
    control mappings
    evaluation results
    evidence set
    exceptions / risk acceptances
    generated at
```

The desired invariant is:

> Given the relevant model version, requirement/control versions, observations, evidence, and governance decisions, Arcogine can explain how a historical conformance result was derived.

## 13. Relationship to current factory-model work

The factory model is the first implemented proving ground for this broader architecture, not yet a complete business model.

Current factory work establishes useful primitives:

```text
canonical semantic model
immutable publication
structural validation
content-derived identity
runtime instantiation from a published model
runtime provenance
```

The next cross-cutting needs are durable lineage, semantic changes, and generic conformance. These should be designed so other authoritative domain models can participate without being forced into one monolithic `BusinessModel` aggregate.

Each domain should retain clear ownership of its facts while cross-domain identity, lineage, change impact, requirements, and evidence provide the governance graph over them.

## 14. What this architecture does not claim

This proposal does not mean that Arcogine currently:

- implements SOC 2, ISO 27001, ISO 9001, GDPR, or another compliance framework;
- provides auditor workflows or certification services;
- continuously observes cloud, identity, HR, source-control, or ticketing systems;
- owns all operational truth in connected systems;
- replaces Jira or enterprise GRC workflow;
- has durable model persistence or lineage today;
- has a generic conformance engine today.

Standards/reference alignment and semantic mappings remain distinct from tested conformance claims.

## 15. Architectural review checklist

When governance or compliance work is proposed, ask:

1. Is the underlying fact authoritative Arcogine model state, external observed state, or a governance decision?
2. Are we adding framework-specific fields to business objects instead of deriving compliance through requirements and controls?
3. Can the result identify the exact model, requirement, assertion, and evidence versions that produced it?
4. Is the change represented semantically enough to perform impact analysis?
5. Does an external workflow system already own the ticket/change-management lifecycle?
6. Are failures, exceptions, and risk acceptances distinguishable rather than collapsed into one status?
7. Can one control/evidence fact support multiple frameworks without duplication?
8. Are historical results reproducible rather than dependent on today's mutable mappings?
9. Are modeled intent and observed reality explicit and independently attributable?
10. Would the capability remain useful for internal policy or architecture governance even if no regulatory framework were mapped to it?

## 16. ADR triggers

Create or revise ADRs when implementation commits to hard-to-reverse choices about:

- durable model identity and lineage semantics;
- parent/branch/merge relationships between model versions;
- canonical semantic change representation;
- temporal semantics for modeled facts and observations;
- requirement/assertion evaluation contracts;
- control and framework versioning;
- evidence integrity/retention semantics;
- exception and risk-acceptance lifecycle;
- authority boundaries with Jira or other external governance systems;
- deployment of approved model changes into real operations.

Do not create framework-specific ADRs merely to add content mappings when the generic governance architecture is unchanged.
