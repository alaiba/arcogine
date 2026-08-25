# Governance and Conformance Architecture

> **Status:** Proposed architectural reference  
> **Scope:** Cross-domain model history, semantic change, requirements, conformance, evidence, and governance over Arcogine's canonical business semantics  
> **Authority:** Proposed architecture; this document does not claim current compliance, audit, or certification capability  
> **Related:** [Product Charter](../product/charter.md), [Architecture Overview](overview.md), [Factory Design Architecture](factory-design.md), [ADR-0003](decisions/0003-canonical-factory-model-boundary.md), [ADR-0004](decisions/0004-model-identity-revision-lineage-and-external-change-control.md), [Standards Alignment](standards-alignment.md), [Governance and Conformance Capability Plan](../planning/governance-conformance-capability.md)

## 1. Architectural position

Arcogine's Product Charter makes verification, provenance, governed change, and continuity between design and reality first-class concerns. Once Arcogine owns authoritative semantic models of a business and their history, governance and compliance can be derived from those models rather than maintained as disconnected checklists.

> **Arcogine should provide a generic conformance capability over authoritative business semantics, controlled revision history, and observations. Regulatory compliance, standards conformance, architecture governance, internal policy, contractual requirements, and operational assurance are projections over that capability.**

Framework content must not drive the shape of business objects. Requirements, controls, assertions, evidence, and findings interpret authoritative business semantics for a particular governance purpose.

## 2. Authority: modeled truth and observed truth are different

Arcogine can be authoritative for intended business semantics it owns: structure, policy, responsibilities, constraints, semantic identity, controlled revision lineage, and technical evidence. External systems may remain authoritative for operational facts and organizational workflow state.

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

Evidence therefore has distinct authority classes:

```text
Structural evidence
    derived from authoritative Arcogine model state

Operational evidence
    observations from runtime systems, equipment, cloud providers,
    identity systems, and other external authorities

Human/process evidence
    approvals, attestations, reviews, artifacts, and governed workflows
```

Arcogine must preserve provenance and authority rather than flattening these into one undifferentiated truth store.

## 3. Compliance is a downstream projection

The primitive dependency direction is:

```text
Authoritative business semantics
            |
            v
Semantic fingerprint + controlled revision lineage
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

One underlying business fact or control may satisfy multiple framework requirements.

## 4. Semantic identity and controlled lineage precede audit claims

[ADR-0004](decisions/0004-model-identity-revision-lineage-and-external-change-control.md) separates two identities that governance must not collapse:

```text
ModelFingerprint
    deterministic identity of canonical semantic content
    answers: "what exact design/state?"

ControlledRevision
    durable historical configuration artifact
    revision ID
    model fingerprint
    parent revision(s)
    schema/model version where applicable
    creation/publication provenance
    author or decision source
    ChangeSet reference
    external change reference
    answers: "which controlled historical state?"
```

Approval, deployment, conformance evaluation, and simulation execution are separate artifacts that may reference a controlled revision; they are not properties required for the revision to exist.

Equal semantic content may therefore occur in distinct controlled revisions. For example, an intentional rollback can create a later revision with the same model fingerprint as an earlier revision while remaining a distinct governed event in history.

The current `FactoryModelVersion.contentHash()` is only the implemented proving ground for content-derived identity. It remains an internal, provisional policy until canonicalization, ordering semantics, fingerprint format/versioning, and cross-process compatibility are specified. It must not be described as an audit-grade durable fingerprint merely because it uses SHA-256.

The system should eventually answer:

```text
What semantic state was true at time T?
What fingerprint identifies that state?
Which controlled revision represented it?
What changed from the prior revision?
Why was that change proposed?
Which external change record governed it?
Who or what approved it, if approval was required?
Was it deployed, and where/when?
Which requirements were affected?
Which evidence supported the resulting conformance state?
```

## 5. Semantic change is a first-class governance primitive

Text, JSON, or serialized-object diffs are insufficient for business governance. When a concrete workflow requires shared change semantics, Arcogine should represent consequential changes in domain terms.

```text
DataStoreMoved
    datastore: CustomerRecords
    fromRegion: EU
    toRegion: US
```

is more useful than a raw field diff because a semantic `ChangeSet` can drive impact analysis:

```text
ChangeSet
   |
   +--> base controlled revision / fingerprint
   +--> candidate fingerprint
   +--> affected business objects
   +--> affected requirements
   +--> affected controls
   +--> required reviewers
   +--> required evidence
   +--> invalidated prior evidence
```

Design/change history remains distinct from simulated or operational event history.

## 6. External change-management systems remain workflow authorities where appropriate

Arcogine does not need to replace Jira or another enterprise change-management system. Organizational workflow may remain external while Arcogine owns semantic meaning, impact, technical evidence, and controlled revision lineage.

A target relationship is:

```text
External change request (e.g. Jira)
        |
        v
Arcogine ChangeSet
        |
        v
Candidate controlled revision
        |
        +--> semantic diff
        +--> affected requirements / controls
        +--> validation / simulation / conformance evidence
        |
        v
External or Arcogine approval record
        |
        v
Deployment record, when deployed
```

The external reference tracks or governs the change. Arcogine should not duplicate ticket comments, project-management state, or vendor-specific workflow terminology unless those facts become necessary to a cross-system governance contract.

## 7. Generic conformance model

The core verification capability should answer whether a defined scope satisfies explicit requirements.

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
    model fingerprint
    controlled revision ID, when available
    observed-at / applicable period
    result
    evidence set

Finding
    affected entities
    severity
    explanation
    remediation state
```

A structural requirement can be evaluated solely from authoritative model state; an operational requirement may need external observation. `UNKNOWN` is important: absence of evidence must not silently become success or failure when the underlying fact is genuinely unobserved.

## 8. Controls and frameworks are mappings, not business truth

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

One control may map to multiple requirements across multiple frameworks. Framework upgrades should change mappings and requirement versions without forcing unrelated business objects to acquire new schema fields.

## 9. Evidence must be attributable and temporal

An external observation does not intrinsically belong to one Arcogine model fingerprint or controlled revision. An AWS configuration snapshot, an IdP login log, or a Jira approval artifact has its own authority, its own observation time, and often its own applicable period, independent of which Arcogine model version or revision happens to exist when it is captured or used. Binding the observation itself to one fingerprint/revision either forces duplicating identical evidence across every subsequent version or misrepresents the observation's actual provenance. Arcogine must therefore separate the evidence itself from any particular evaluation's use of it:

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
    evaluation / assertion / control relationship
    model fingerprint
    controlled revision ID, when applicable
    scope at time of use
    applicability determination
```

`Evidence` is the source-level fact: what was observed, by what authority, when, and over what period it applies. It does not carry a model fingerprint or revision. `EvidenceUse` is the binding: which evaluation consumed that evidence, against which fingerprint/revision, and why it was judged applicable to that scope at that time. One `Evidence` record may be referenced by many `EvidenceUse` records across multiple model versions, as long as each use's scope/applicability determination independently holds.

Structural evidence derived directly from Arcogine's own authoritative model state is the one case where binding to a fingerprint/revision at the source is natural — the model version *is* the evidence's provenance, so `Evidence` and `EvidenceUse` may collapse into one record for that case. External evidence should generally be bound to fingerprint/revision identity at evaluation/use time (`EvidenceUse`), not at source-observation time (`Evidence`).

Evidence generated from Arcogine's authoritative state must remain distinguishable from evidence observed externally. Reuse is valid only when scope, applicable period, provenance, and semantic meaning remain compatible.

## 10. Findings, exceptions, and risk acceptance are explicit governance state

A failed assertion may be remediated, accepted temporarily, or explicitly excepted.

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

Arcogine's stronger opportunity is to evaluate a proposed semantic change before deployment or operational activation.

```text
Current controlled revision
        |
 Proposed ChangeSet
        |
        v
Candidate fingerprint / controlled revision
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

This connects design, verification, change management, and compliance through one semantic representation without requiring Arcogine to own the organizational workflow.

## 12. Audit snapshots are reproducible interpretations

An audit view should be reconstructible from versioned inputs rather than stored only as an opaque dashboard state.

```text
AuditSnapshot
    model fingerprint
    controlled revision ID
    framework / requirement version
    control mappings
    evaluation results
    evidence set
    exceptions / risk acceptances
    generated at
```

The desired invariant is:

> Given the relevant semantic fingerprint, controlled revision, requirement/control versions, observations, evidence, and governance decisions, Arcogine can explain how a historical conformance result was derived.

## 13. Relationship to current factory-model work

The factory model is the first implemented proving ground for this broader architecture, not yet a complete business model.

Current factory work establishes:

```text
canonical semantic model
immutable publication
structural validation
provisional content-derived identity
runtime instantiation from a published model
handler-level runtime provenance
```

It does **not** yet provide a durable fingerprint contract, controlled revision repository, approval/deployment lifecycle, or generic conformance engine. Those capabilities should be added independently and in that order when concrete requirements justify them.

Other authoritative domain models should participate without being forced into one monolithic `BusinessModel` aggregate. Each domain retains ownership of its facts while cross-domain identity references, lineage, semantic changes, requirements, and evidence form the governance graph over them.

## 14. What this architecture does not claim

This proposal does not mean that Arcogine currently:

- implements SOC 2, ISO 27001, ISO 9001, GDPR, or another compliance framework;
- provides auditor workflows or certification services;
- continuously observes cloud, identity, HR, source-control, or ticketing systems;
- owns all operational truth in connected systems;
- replaces Jira or enterprise GRC workflow;
- has durable model persistence or controlled revision lineage today;
- has a durable cross-process model fingerprint contract today;
- has a generic conformance engine today.

Standards/reference alignment and semantic mappings remain distinct from tested conformance claims.

## 15. Architectural review checklist

When governance or compliance work is proposed, ask:

1. Is the underlying fact authoritative Arcogine model state, external observed state, or a governance decision?
2. Are framework-specific fields being added to business objects instead of deriving compliance through requirements and controls?
3. Can the result identify the exact semantic fingerprint, controlled revision, requirement/assertion versions, and evidence that produced it?
4. Is semantic identity being confused with historical revision identity?
5. Is the change represented semantically enough to perform impact analysis?
6. Does an external workflow system already own the ticket/change-management lifecycle?
7. Are approval and deployment modeled as records referencing a revision rather than as revision identity itself?
8. Are failures, exceptions, and risk acceptances distinguishable rather than collapsed into one status?
9. Are modeled intent and observed reality explicit and independently attributable?
10. Are historical results reproducible rather than dependent on today's mutable mappings?

## 16. ADR triggers

[ADR-0004](decisions/0004-model-identity-revision-lineage-and-external-change-control.md) already fixes the semantic-identity versus controlled-revision distinction and the external change-control boundary.

Create or revise ADRs when implementation commits to hard-to-reverse choices about:

- the durable fingerprint canonicalization/format/compatibility contract;
- concrete controlled revision identifiers and persistence/lineage semantics;
- parent/branch/merge relationships between controlled revisions;
- canonical semantic `ChangeSet` representation;
- temporal semantics for modeled facts and observations;
- requirement/assertion evaluation contracts;
- control and framework versioning;
- evidence integrity/retention semantics;
- exception and risk-acceptance lifecycle;
- authority boundaries/protocols with Jira or another external governance system;
- deployment of approved revisions into real operations.

Do not create framework-specific ADRs merely to add content mappings when the generic governance architecture is unchanged.
