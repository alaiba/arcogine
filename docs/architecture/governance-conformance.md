# Governance and Conformance Architecture

> **Status:** Proposed architectural reference  
> **Scope:** Cross-domain model history, semantic change, requirements, conformance, evidence, and governance over Arcogine's canonical business semantics  
> **Authority:** Proposed architecture; this document does not claim current compliance, audit, or certification capability  
> **Related:** [Product Charter](../product/charter.md), [Architecture Overview](overview.md), [Factory Design Architecture](factory-design.md), [Operational Execution and Digital Twin Architecture](operational-execution-digital-twin.md), [ADR-0003](decisions/0003-canonical-factory-model-boundary.md), [ADR-0004](decisions/0004-model-identity-revision-lineage-and-external-change-control.md), [ADR-0006](decisions/0006-durable-semantic-fingerprint-contract.md), [ADR-0008](decisions/0008-controlled-revision-identity-and-lineage.md), [Standards Alignment](standards-alignment.md), [Governance and Conformance Capability Plan](../planning/governance-conformance-capability.md)

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

For operational systems specifically, the [Operational Execution and Digital Twin Architecture](operational-execution-digital-twin.md) owns acquisition, trust/authenticity provenance, command/deployment facts, and reconciliation of operational observations. Governance consumes those independent facts through evidence-use relationships; it does not own telemetry ingestion or twin reconciliation.

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

[ADR-0004](decisions/0004-model-identity-revision-lineage-and-external-change-control.md), [ADR-0006](decisions/0006-durable-semantic-fingerprint-contract.md), and [ADR-0008](decisions/0008-controlled-revision-identity-and-lineage.md) establish two identities that governance must not collapse:

```text
ModelFingerprint
    deterministic identity of canonical semantic content
    answers: "what exact design/state?"

ControlledRevision
    immutable historical configuration occurrence
    revision ID
    exactly one model fingerprint
    zero or one parent in the current capability
    recording provenance: recordedAt + recorder
    answers: "which controlled historical occurrence?"
```

The current `0..1` parent rule is a capability constraint, not a permanent assertion that revision history is intrinsically single-parent. Divergence is already representable because multiple revisions may share the same parent. Branch refs, tags, multi-parent merge revisions, merge/conflict semantics, and stronger cryptographic revision-record integrity may be added later without changing the distinction between semantic identity and historical revision identity.

The minimum revision core deliberately does **not** contain a `ChangeSet`, external change reference, approval/authorization state, deployment state, human version label, framework/compliance state, or serialized model artifact. Those are separate relationships or follow-on capabilities.

Equal semantic content may therefore occur in distinct controlled revisions. A rollback illustrates the invariant:

```text
R1 -> F1
 |
 v
R2 -> F2
 |
 v
R3 -> F1
```

`R1` and `R3` have equal semantic fingerprints but distinct controlled revision IDs. The later occurrence does not inherit the historical governance meaning of the earlier one merely because the semantic content is equal.

Controlled revision identity and lineage form the **configuration-history and evidence-addressability substrate**. They let later records point at an exact historical occurrence, but they do not themselves mean that the revision was approved, authorized, conformant, certified, deployed, or compliant with an external framework.

This creates two complementary dimensions:

```text
Configuration history

R40 --------> R41 --------> R42 --------> R43
                              |
                              +--> ChangeSet / rationale
                              +--> conformance evaluation / finding
                              +--> authorization decision
                              +--> deployment record
                              +--> evidence use
                              +--> external workflow reference
```

The horizontal dimension is immutable revision lineage. The attached records are governance, evidence, and operational facts about that history.

A controlled revision becomes an authoritative historical fact only when its immutable record is accepted by Arcogine's authoritative revision store. ADR-0008 defines the required identity, immutability, lineage, and provenance semantics, but does not choose the persistence technology. G1.3 must establish that authoritative persistence boundary and ensure an accepted revision can resolve to the exact semantic state/artifact needed for historical reconstruction.

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
Which effective transformed/applied artifact governed each deployment target?
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

Operational drift or calibration may motivate a candidate semantic change, but the durable `ChangeSet` remains Governance-owned rather than being duplicated by the digital-twin capability.

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
Authorization decision/evidence
required by applicable change-control policy
(e.g. ApprovalRecord, standing authorization,
pre-approved standard change, emergency
justification, automated policy)
        |
        v
Operational deployment record, when deployed
```

External workflow references are associations to revisions/changes, not fields that define controlled revision identity. This allows a revision to be recorded before an external ticket is linked and prevents workflow metadata changes from mutating immutable configuration history.

The external reference tracks or governs the change. Arcogine should not duplicate ticket comments, project-management state, or vendor-specific workflow terminology unless those facts become necessary to a cross-system governance contract.

Governance owns the revision/change/authorization interpretation. The Operational Execution capability owns applying an authorized revision to an execution target and recording the effective adapter/profile/transformation/applied-artifact provenance and operational result. A Governance deployment reference therefore points to an Operational Execution deployment record rather than redefining deployment mechanics inside Governance.

## 7. Generic conformance model

The core verification capability should answer whether a defined scope satisfies explicit requirements.

```text
Requirement
    stable identity
    version
    description
    source authority
    source designation
    source edition/version
    source locator
    adoption/profile, when applicable

Assertion
    stable identity
    version
    scope
    expression/evaluator
    evidence requirements

Evaluation
    requirement identity/version
    assertion identity/version
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

A requirement's provenance must identify the exact normative or governing source when its meaning depends on an external standard, regulation, contract, or policy. Family-level labels such as `ISA-95 / IEC 62264` are insufficient for an auditable requirement because closely aligned standards, editions, and national adoptions can differ. The requirement must retain enough source identity to determine what text and obligations governed a historical evaluation.

External source identity is separate from Arcogine's own requirement and assertion identities and versions. The same external clause or policy source may support multiple Arcogine requirement versions as scope, interpretation, or executable semantics evolve; source provenance therefore augments rather than replaces Arcogine versioning.

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

An external observation does not intrinsically belong to one Arcogine model fingerprint or controlled revision. An AWS configuration snapshot, an IdP login log, a PLC measurement, or a Jira approval artifact has its own authority, its own observation time, and often its own applicable period, independent of which Arcogine model version or revision happens to exist when it is captured or used. Binding the observation itself to one fingerprint/revision either forces duplicating identical evidence across every subsequent version or misrepresents the observation's actual provenance. Arcogine must therefore separate the evidence itself from any particular evaluation's use of it:

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

Operational observations retain the identity and provenance assigned by the Operational Execution capability. Governance must reference them; it must not rewrite them into revision-bound telemetry records in order to use them as evidence.

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
        +--> required authorization/review
        |
        v
Pre-change conformance evaluation
        |
   authorize / reject / revise
```

This connects design, verification, change management, and compliance through one semantic representation without requiring Arcogine to own the organizational workflow.

## 12. Audit snapshots are reproducible interpretations

An audit view should be reconstructible from versioned inputs rather than stored only as an opaque dashboard state.

```text
AuditSnapshot
    model fingerprint
    controlled revision ID
    requirement identity / version
    assertion identity / version
    requirement source identity / version
    framework / mapping version, when applicable
    control mappings
    evaluation results
    evidence set
    exceptions / risk acceptances
    generated at
```

The desired invariant is:

> Given the relevant semantic fingerprint, controlled revision, Arcogine requirement and assertion identities/versions, exact external requirement source identity/version when applicable, framework/mapping versions, observations, evidence, and governance decisions, Arcogine can explain how a historical conformance result was derived.

## 13. Relationship to current factory-model and operational work

The factory model is the first implemented proving ground for this broader architecture, not yet a complete business model.

Current factory work establishes:

```text
canonical semantic model
immutable publication
structural validation
durable factory-model:v1 semantic fingerprint
runtime instantiation from a published model
runtime/result provenance work in progress
```

ADR-0006 and its implementation establish the durable factory-model fingerprint contract. ADR-0008 establishes the controlled revision identity/lineage decision, and G1.2 now implements the controlled-revision identity/value contracts in `:types` and `:governance`. Authoritative revision persistence and exact historical semantic-state/artifact resolution remain outstanding in G1.3. Arcogine therefore still does **not** have an authoritative controlled revision repository or generic conformance engine.

The Governance dependency remains:

```text
G1.1 durable semantic fingerprint        complete
    ↓
G1.2 controlled revision value contract  complete
    ↓
G1.3 authoritative persistence +
     historical semantic-state resolution  remaining G1 substrate slice
    ↓
G2 ChangeSet
    ↓
G3-G5 conformance/evidence
    ↓
G6 governed-change/authorization integration
```

The Operational Execution and Digital Twin track is a sibling consumer/proving ground for G1/G2/G4/G5, not an alternate owner. It may use clearly scoped synthetic fixtures while Governance capabilities are incomplete, but such fixtures do not satisfy Governance gates and must be replaced by the Governance-owned contracts when those gates land.

Other authoritative domain models should participate without being forced into one monolithic `BusinessModel` aggregate. Each domain retains ownership of its facts while cross-domain identity references, lineage, semantic changes, requirements, and evidence form the governance graph over them.

## 14. What this architecture does not claim

This proposal does not mean that Arcogine currently:

- implements SOC 2, ISO 27001, ISO 9001, GDPR, or another compliance framework;
- provides auditor workflows or certification services;
- continuously observes cloud, identity, HR, source-control, ticketing, or industrial systems;
- owns all operational truth in connected systems;
- replaces Jira or enterprise GRC workflow;
- has an authoritative controlled revision repository;
- has durable exact historical semantic-state/artifact resolution for controlled revisions;
- has a generic conformance engine today;
- has production actuation or digital-twin reconciliation today.

Standards/reference alignment and semantic mappings remain distinct from tested conformance claims. Controlled revision identity/lineage is an enabling configuration-management primitive, not evidence that any external standard or control has been satisfied.

## 15. Architectural review checklist

When governance or compliance work is proposed, ask:

1. Is the underlying fact authoritative Arcogine model state, external observed state, or a governance decision?
2. Are framework-specific fields being added to business objects instead of deriving compliance through requirements and controls?
3. Can the result identify the exact semantic fingerprint, controlled revision, requirement/assertion versions, and evidence that produced it?
4. If a requirement comes from an external source, can it identify the exact authority, designation, edition/version, locator, and applicable adoption/profile rather than only a standards-family name?
5. Is semantic identity being confused with historical revision identity?
6. Is the change represented semantically enough to perform impact analysis?
7. Does an external workflow system already own the ticket/change-management lifecycle?
8. Are approval and operational deployment modeled as separate records referencing a revision rather than as revision identity itself?
9. If a revision is deployed through a transformation/adapter, can the operational deployment record identify the effective applied artifact/profile rather than only the source revision?
10. Are failures, exceptions, and risk acceptances distinguishable rather than collapsed into one status?
11. Are modeled intent and observed reality explicit and independently attributable?
12. Is an external observation kept revision-independent until an `EvidenceUse`/interpretation binds it when appropriate?
13. Are historical results reproducible rather than dependent on today's mutable mappings?
14. Is a workflow/change reference being treated as an association rather than an immutable identity field of the revision?
15. Does a proposed lineage extension preserve the distinction between semantic identity and historical occurrence identity?

## 16. ADR triggers

[ADR-0004](decisions/0004-model-identity-revision-lineage-and-external-change-control.md) fixes the semantic-identity versus controlled-revision distinction and the external change-control boundary. [ADR-0006](decisions/0006-durable-semantic-fingerprint-contract.md) fixes the first durable fingerprint contract. [ADR-0008](decisions/0008-controlled-revision-identity-and-lineage.md) fixes controlled revision identity, current lineage cardinality, rollback semantics, immutable recording provenance, and the persistence boundary.

Create or revise ADRs when implementation commits to hard-to-reverse choices about:

- authoritative controlled revision persistence and exact historical semantic-state/artifact resolution;
- extending current `0..1` lineage to multi-parent merge semantics;
- branch/ref/tag semantics over controlled revisions;
- cryptographic revision-record integrity/signature semantics;
- canonical semantic `ChangeSet` representation;
- temporal semantics for modeled facts and observations;
- requirement/assertion evaluation contracts;
- requirement source-identity and versioning semantics;
- control and framework versioning;
- evidence integrity/retention semantics;
- exception and risk-acceptance lifecycle;
- authority boundaries/protocols with Jira or another external governance system;
- the governance-to-operational deployment record boundary for approved revisions.

Do not create framework-specific ADRs merely to add content mappings when the generic governance architecture is unchanged.
