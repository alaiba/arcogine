# Governance and Conformance Capability Plan

> **Status:** Proposed  
> **Scope:** Establish the cross-domain substrate for durable semantic identity, controlled revision history, semantic change, requirements, conformance, evidence, and governed change  
> **Authority:** Planning only; this document defines delivery dependencies and readiness criteria, not current product capability  
> **Related:** [Governance and Conformance Architecture](../architecture/governance-conformance.md), [ADR-0004](../architecture/decisions/0004-model-identity-revision-lineage-and-external-change-control.md), [Product Charter](../product/charter.md), [Factory Design Capability Plan](factory-design-capability.md), [Factory Design Architecture](../architecture/factory-design.md), [Standards Alignment](../architecture/standards-alignment.md)

## 1. Purpose

Arcogine should not respond to the opportunity for compliance automation by building framework checklists first. Governance should be derived from authoritative semantic models, their controlled revision history, observed facts, and explicit governance decisions.

The generic sequence is:

```text
Canonical domain models
        |
        v
Durable semantic fingerprint + controlled revision lineage
        |
        v
Semantic ChangeSets
        |
        v
Requirements and assertions
        |
        v
Conformance evaluation
        |
        v
Evidence and findings
        |
        v
Governed change / exceptions / risk
        |
        v
Framework mappings and compliance projections
```

The same primitives support design review, architecture governance, deployment approval, internal policy, safety constraints, customer commitments, auditability, digital-twin reconciliation, and agent governance.

## 2. Relationship to current factory-model work

The canonical factory-model work is the first implementation proving ground.

The implemented seam currently provides:

```text
FactoryModel
    -> structural validation
    -> immutable publication
    -> provisional content-derived identity
    -> runtime instantiation
    -> handler-level provenance
```

That seam must not be blocked by the generic governance initiative. In particular, the current `FactoryModelVersion.contentHash()` is not yet a durable cross-process fingerprint contract, and the current factory model has no controlled revision repository.

The governance use case now provides a concrete cross-consumer reason to pursue work previously deferred in the factory plan. The dependency order matters here, and matches the G1-G9 sequence in §4 below, not the order these concerns happen to be listed elsewhere:

```text
durable semantic fingerprint + controlled revision lineage (G1)
          ↓
semantic ChangeSet (G2)
          ↓
requirement-based conformance/evidence (G3-G5)
          ↓
review/approval/governed-change integration (G6)
```

Evaluating a proposed change's conformance before it is authorized is the strategic point (see [architecture §11](../architecture/governance-conformance.md#11-pre-change-conformance-is-strategically-important)); an approval/deployment integration that isn't preceded by conformance evaluation would authorize changes Arcogine hasn't yet assessed.

> **D5 semantic comparison is no longer only an editor convenience. It is an enabling primitive for governed change and impact analysis once the model seam is stable.**

This does not imply generic patch/merge infrastructure. The need is semantic change attribution.

## 3. Delivery principles

1. Framework-specific content remains downstream of generic conformance. Do not add SOC 2, ISO 27001, GDPR, or similar fields to core business objects.
2. Do not create a monolithic `BusinessModel`. Each domain retains authoritative ownership of its facts.
3. Distinguish modeled intent from observed reality. Structural facts may be provable from Arcogine state; operational assertions may require external evidence.
4. Reuse external workflow systems where they already own organizational process state. Jira may remain authoritative for issue workflow while Arcogine owns semantic impact, evidence, and controlled revision lineage.
5. Keep semantic identity and controlled revision identity separate as required by [ADR-0004](../architecture/decisions/0004-model-identity-revision-lineage-and-external-change-control.md).
6. Treat external requirement provenance as versioned input. A standards-family label is not sufficient when an evaluation depends on a specific issuing authority, designation, edition/version, clause/locator, or adoption/profile.

## 4. Delivery sequence

```text
G1  Durable fingerprint and controlled revision lineage
    ↓
G2  Semantic ChangeSet and impact model
    ↓
G3  Generic requirement/assertion contract
    ↓
G4  Conformance evaluation and findings
    ↓
G5  Evidence and observation provenance
    ↓
G6  Governed change and external workflow integration
    ↓
G7  Exceptions and risk acceptance
    ↓
G8  Framework/control mappings
    ↓
G9  Audit snapshots and compliance projections
```

G1-G5 are architectural substrate. G6-G7 establish governance workflow integration. G8-G9 make conventional compliance automation possible without turning compliance into Arcogine's core ontology.

## 5. G1 — Durable fingerprint and controlled revision lineage

### Goal

Promote the current provisional content-derived identity into an explicit durable semantic fingerprint contract, and introduce a separate controlled revision lifecycle for historical lineage.

The conceptual split is:

```text
ModelFingerprint
    deterministic identity of canonical semantic content

ControlledRevisionId
    identity of one persisted historical configuration artifact

ControlledRevision
    revision ID
    model fingerprint
    parent revision(s)
    schema/model version where applicable
    publication/creation provenance
    author or decision source
    external change reference, when applicable
```

Approval and deployment are separate records referencing a controlled revision. They are not components of semantic identity and are not prerequisites for a revision to exist.

### Required properties

- Equal semantic content can have the same model fingerprint across distinct controlled revisions.
- A later rollback may therefore have the same fingerprint as an earlier revision while remaining historically distinct.
- The durable fingerprint policy must specify canonicalization, ordering semantics, algorithm/format versioning, and compatibility guarantees.
- The durable fingerprint policy must specify which fields are semantic versus non-semantic, including whether names and allocated IDs participate in semantic identity, and how semantic fields are normalized, and must define the relationship between Java equality and fingerprint equality.
- Controlled revision identity must not be inferred from the fingerprint or a human label such as `v7`.
- Human version/revision labels may exist for presentation but are not fundamental identity.

### Acceptance criteria

G1 is ready when:

1. A durable semantic fingerprint contract is explicitly specified and testable across supported process/version boundaries.
2. Controlled revisions have durable identities independent of process memory and semantic fingerprint equality.
3. Revision lineage identifies predecessor/parent relationships under the chosen policy.
4. Semantic content remains immutable for a published/referenced revision.
5. Provenance records who or what created/published the revision and when.
6. Approval and deployment records can independently reference a revision.
7. A downstream result can retain the semantic fingerprint and, when applicable, the controlled revision ID.

### ADR trigger

[ADR-0004](../architecture/decisions/0004-model-identity-revision-lineage-and-external-change-control.md) already fixes the semantic separation. A follow-up ADR is warranted before implementation commits to the concrete durable fingerprint format, revision identifier scheme, persistence model, or lineage rules.

## 6. G2 — Semantic ChangeSet and impact model

### Goal

Represent a meaningful transition between controlled revisions or between a base revision and a candidate semantic snapshot in domain terms sufficient for review, impact analysis, and governance.

A `ChangeSet` or equivalent should identify:

```text
base controlled revision / fingerprint
candidate fingerprint
resulting controlled revision, when persisted
semantic changes
changed entities
change source/reason
external change-request reference
```

### Non-goal

Do not build generic JSON patching, arbitrary text merge, collaborative cursors, or a distributed source-control system for models merely to satisfy G2.

### Impact analysis

Useful first questions include:

```text
Which business objects are affected?
Which requirements evaluate over those objects?
Which existing evidence becomes stale or invalid?
Which owners/reviewers have authority over the affected scope?
Which runtime/deployment contexts would consume the changed model?
```

### Acceptance criteria

G2 is ready when:

1. Two relevant semantic states can be compared in domain terms rather than only byte-for-byte.
2. Changed entities can be identified with stable domain identity.
3. At least one domain change has a meaningful typed/classified representation.
4. Impact analysis can determine which registered requirements are potentially affected.
5. ChangeSet provenance can retain an external change-request identifier.

## 7. G3 — Generic requirement and assertion contract

### Goal

Define explicit requirements independently of any particular compliance framework while preserving enough source identity to reproduce the requirement that actually governed an evaluation.

```text
Requirement
    stable identity
    source authority, when external
    source designation, when external
    source edition/version
    source locator
    adoption/profile, when applicable
    human meaning

Scope
    business objects to which it applies

Assertion
    deterministic evaluation rule or evaluator
    required evidence class
```

For an Arcogine-native requirement, the source may be an internal policy or architecture authority rather than an external standard. For an external requirement, a family-level reference such as `ISA-95 / IEC 62264` is insufficient if the obligation depends on a specific publication. The contract must be able to distinguish, for example, an IEC publication from a modified ANSI/ISA adoption or a national adoption without assuming that aligned editions are textually or normatively identical.

The first requirement should be Arcogine-native and structurally evaluable from authoritative model state rather than imported from an external framework.

### Acceptance criteria

G3 is ready when:

1. A requirement can be versioned separately from the model it evaluates.
2. Scope selection is explicit and deterministic.
3. An assertion can declare whether model state alone is sufficient or external evidence is required.
4. Requirement wording/source and executable assertion semantics are distinguishable.
5. An external requirement can identify its exact source authority, designation, edition/version, locator, and applicable adoption/profile where those facts affect meaning.
6. The contract is generic enough to represent internal policy and architecture rules.

## 8. G4 — Conformance evaluation and findings

### Goal

Evaluate requirements against an exact semantic state and produce explainable results.

A useful result model supports at least:

```text
PASS
FAIL
UNKNOWN
NOT_APPLICABLE
```

An evaluation retains:

```text
model fingerprint
controlled revision ID, when available
requirement/assertion version
scope
result
explanation
affected entities
evaluation time / applicable period
```

A failed result produces a finding rather than mutating the underlying business model.

### Acceptance criteria

G4 is ready when:

1. The same semantic fingerprint/revision and requirement/assertion versions produce deterministic structural results.
2. Findings identify affected entities and the failed assertion.
3. Results distinguish missing evidence from proven non-conformance.
4. Evaluation output is immutable or historically attributable.
5. A proposed `ChangeSet` can be evaluated before authorization/deployment for at least one requirement.

The last criterion is the first major strategic milestone: pre-change conformance rather than only post-change monitoring.

## 9. G5 — Evidence and observation provenance

### Goal

Support assertions whose truth depends on observations outside Arcogine's authoritative semantic model.

Per [Evidence must be attributable and temporal](../architecture/governance-conformance.md#9-evidence-must-be-attributable-and-temporal), the evidence source record and its use in one evaluation are distinct:

```text
Evidence (source-level, no model fingerprint/revision)
    source
    provenance
    observedAt
    applicable period
    external identity/reference
    integrity metadata where required

EvidenceUse (binds evidence to one evaluation)
    related assertion/control
    model fingerprint
    controlled revision ID, when applicable
    scope/applicability at time of use
```

External evidence is generally reusable across model versions as long as each `EvidenceUse` independently re-establishes scope and applicability; only structural evidence derived directly from Arcogine's own model state naturally collapses the two into one record. Initial adapters should be driven by a concrete requirement, not a desire to match a vendor's integration count.

### Acceptance criteria

G5 is ready when:

1. External evidence is distinguishable from Arcogine-derived structural evidence.
2. Evidence is attributable to source and observation time, independent of any model fingerprint or revision.
3. An assertion can combine intended model state with observed external state.
4. An `EvidenceUse` can become stale/invalid when its scope, applicable period, or affected semantics change, without invalidating the underlying `Evidence` record for other uses.
5. A historical evaluation can identify the evidence set (and the `EvidenceUse` bindings) it relied on.

## 10. G6 — Governed change and external workflow integration

### Goal

Connect semantic `ChangeSet`s, candidate controlled revisions, and technical evidence to enterprise change-management workflows without recreating Jira inside Arcogine.

Target relationship:

```text
External issue / change request
        |
        v
Arcogine ChangeSet
        |
        v
Candidate controlled revision
        |
        +--> semantic impact
        +--> validation/simulation/conformance evidence
        +--> required approvals/owners
        |
        v
Authorization decision/evidence
required by applicable change-control policy
(e.g. ApprovalRecord, standing authorization,
pre-approved standard change, emergency
justification, automated policy)
        |
        v
Deployment record, when deployed
```

### Authority boundary

Jira or another workflow system may remain authoritative for issue workflow, assignments, discussions, and transitions. Arcogine retains the stable external reference and the technical/governance facts required to explain the semantic change and its resulting revision.

If Arcogine later owns an approval decision itself, that decision must be modeled explicitly with actor, authority, and provenance rather than inferred from mutable UI state.

### Acceptance criteria

G6 is ready when:

1. A `ChangeSet`/revision can reference an external change request.
2. Impact and conformance information can be surfaced into the change workflow.
3. Approval/authorization records reference the relevant controlled revision rather than defining revision identity.
4. Deployment, when it occurs, is separately attributable to the deployed revision.
5. External project-management metadata is not duplicated without semantic need.
6. A reviewer can trace a governed model transition back to the external record that tracked/governed the change.

## 11. G7 — Exceptions and risk acceptance

### Goal

Represent explicit governance decisions when a known non-conformance is tolerated temporarily or conditionally.

```text
Finding
Remediation
Exception
RiskAcceptance
CompensatingControl
Expiration
```

An approved exception does not change a failed assertion into `PASS`; it changes the governance disposition of the finding.

### Acceptance criteria

G7 is ready when:

1. Findings retain their factual conformance result independently of disposition.
2. Exceptions have rationale, accountable owner/approver, and effective/expiration periods.
3. Expired exceptions become visible without rewriting history.
4. Compensating controls/evidence can be linked when used.

## 12. G8 — Controls and framework mappings

### Goal

Introduce conventional compliance abstractions only after generic conformance and evidence are working.

```text
Framework
  -> Requirement
      -> satisfied by Control
          -> implemented by business semantics/process/policy
          -> verified by Assertions
          -> supported by Evidence
```

One control may map to multiple frameworks. Framework versions and mappings must be historically attributable so an old audit is not silently reinterpreted through today's mapping.

### First framework policy

Do not attempt broad framework coverage. Select one small, legally permissible set of requirements sufficient to prove cross-framework mapping and evidence reuse. Review licensing and source terms before importing copyrighted or proprietary control text.

### Acceptance criteria

G8 is ready when:

1. Requirements and controls are versioned independently of business model revisions/fingerprints.
2. One control can map to more than one requirement/framework.
3. Business objects do not acquire framework-specific compliance booleans.
4. Framework/mapping changes do not mutate historical evaluations.
5. Evidence reuse respects scope, time, and semantic compatibility.

## 13. G9 — Audit snapshots and compliance projections

### Goal

Produce a reproducible audit/compliance view over exact semantic state, controlled revision history, requirements, controls, evidence, findings, and exceptions.

An audit snapshot should identify:

```text
model fingerprint
controlled revision ID
requirement source identity and version
framework/mapping version, when applicable
control mappings
assertion results
supporting evidence
findings
exceptions/risk acceptances
generation time
```

The first useful UX can be headless/export-oriented. Do not build a large GRC dashboard before the historical semantics work.

### Acceptance criteria

G9 is ready when:

1. A historical compliance result can be reconstructed from explicit versioned inputs, including the exact requirement source identity that governed the evaluation.
2. The system can explain why a control passed, failed, or was unknown.
3. Evidence and exceptions are traceable to sources and applicable periods.
4. A change in today's framework mapping or source standard edition does not silently alter a previous audit snapshot.
5. A reviewer can traverse from a requirement to its governing source, control, assertion, affected business objects, semantic fingerprint, controlled revision, evidence, and change history.

## 14. First end-to-end milestone

The first milestone should deliberately avoid a full external compliance framework:

> **Take a proposed semantic change to an Arcogine model, derive its candidate fingerprint, persist a controlled revision linked to an external change request, evaluate one Arcogine-native requirement before authorization/deployment, record the approval decision separately, and reconstruct why the resulting semantic state was considered conformant.**

A concrete example could be an ownership requirement once the relevant owner/authority semantics exist.

Definition of done:

```text
Durable semantic fingerprint contract exists
Controlled revision identity exists
Base revision -> ChangeSet -> candidate revision is attributable
Affected entity is identified
One versioned requirement applies
Pre-change assertion evaluates deterministically
Finding is produced if violated
External change-request provenance can be linked
Approval is a separate record referencing the revision
Deployment is not required to prove the milestone
Historical evaluation remains attributable after later changes
No framework-specific field exists on the business object
```

## 15. What not to build yet

Do not prioritize:

- dozens of compliance frameworks;
- hundreds of SaaS integrations;
- auditor marketplaces or certification workflow;
- questionnaire automation;
- generic policy-document generation;
- trust-center marketing surfaces;
- broad vendor-risk management;
- a monolithic cross-domain business object graph;
- generic Git branch/merge/rebase semantics for models without a concrete workflow;
- a replacement for Jira or another organizational change-management system.

Those may become valid product capabilities later, but they should not distract from the semantic substrate that differentiates Arcogine.

## 16. Documentation and ADR updates as work lands

As implementation progresses:

- update [`../architecture/overview.md`](../architecture/overview.md) only with established current-state behavior;
- update [`../architecture/governance-conformance.md`](../architecture/governance-conformance.md) when this proposed direction changes materially;
- keep [ADR-0004](../architecture/decisions/0004-model-identity-revision-lineage-and-external-change-control.md) as the authority for semantic identity vs. revision identity and the external change-control boundary;
- update factory/domain architecture docs when identity/change requirements alter those models;
- update [`../architecture/standards-alignment.md`](../architecture/standards-alignment.md) when Arcogine moves from reference/mapping toward an actual tested conformance profile;
- create ADRs for the concrete durable fingerprint contract, controlled revision persistence/lineage scheme, semantic `ChangeSet` contracts, temporal evidence semantics, and hard-to-reverse external protocols when implementation commits to them;
- update product/reference docs only for capabilities that actually ship.

Once this initiative is complete or superseded, retain durable decisions in ADR/current architecture and retire or reduce this planning artifact.
