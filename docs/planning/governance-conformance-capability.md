# Governance and Conformance Capability Plan

> **Status:** Proposed  
> **Scope:** Establish the cross-domain substrate for durable semantic identity, controlled revision history, semantic change, requirements, conformance, evidence, and governed change  
> **Authority:** Planning only; this document defines delivery dependencies and readiness criteria, not current product capability  
> **Related:** [Governance and Conformance Architecture](../architecture/governance-conformance.md), [Governance G1 Continuity Notes](governance-g1-continuity.md), [ADR-0004](../architecture/decisions/0004-model-identity-revision-lineage-and-external-change-control.md), [ADR-0006](../architecture/decisions/0006-durable-semantic-fingerprint-contract.md), [ADR-0008](../architecture/decisions/0008-controlled-revision-identity-and-lineage.md), [Product Charter](../product/charter.md), [Factory Design Capability Plan](factory-design-capability.md), [Factory Design Architecture](../architecture/factory-design.md), [Operational Execution and Digital Twin Readiness](operational-execution-digital-twin-readiness.md), [Standards Alignment](../architecture/standards-alignment.md)

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

The same primitives support design review, architecture governance, deployment authorization, internal policy, safety constraints, customer commitments, auditability, digital-twin interpretation, and agent governance. Operational execution and reconciliation consume or produce some of the same facts, but remain a sibling capability rather than part of the Governance runtime.

## 2. Relationship to current factory-model and operational work

The canonical factory-model work is the first implementation proving ground.

The implemented seam currently provides:

```text
FactoryModel
    -> structural validation
    -> immutable publication
    -> durable factory-model:v1 semantic fingerprint
    -> runtime instantiation
    -> runtime/result provenance work in progress
```

ADR-0006 and its implementation establish the durable semantic fingerprint contract. ADR-0008 establishes the controlled revision identity and lineage decision; G1.2 implements its value contracts, while authoritative revision persistence remains outstanding.

The governance use case provides the concrete cross-consumer reason to complete the remaining G1 work. The dependency order matters here, and matches the G1-G9 sequence in §4 below:

```text
G1.1 durable semantic fingerprint                 complete
          ↓
G1.2 controlled revision identity/value contract  complete
          ↓
G1.3 authoritative revision persistence +
     exact historical semantic-state resolution
          ↓
G2 semantic ChangeSet
          ↓
G3-G5 requirement-based conformance/evidence
          ↓
G6 review/authorization/governed-change integration
```

Evaluating a proposed change's conformance before it is authorized is the strategic point (see [architecture §11](../architecture/governance-conformance.md#11-pre-change-conformance-is-strategically-important)); an authorization/deployment integration that isn't preceded by conformance evaluation would authorize changes Arcogine hasn't yet assessed.

> **D5 semantic comparison is no longer only an editor convenience. It is an enabling primitive for governed change and impact analysis once the model seam is stable.**

This does not imply generic patch/merge infrastructure. The need is semantic change attribution.

### 2.1 Boundary with Operational Execution and Digital Twin

The sibling [Operational Execution and Digital Twin Readiness](operational-execution-digital-twin-readiness.md) track is an early consumer/proving ground for Governance-owned identity, change, conformance, and evidence-use contracts.

The dependency is explicit:

```text
Governance G1
    durable semantic fingerprint + controlled revision identity
        |
        +--> Operational deployment / historical reconciliation

Governance G2
    semantic ChangeSet / impact
        |
        +--> Operational drift/calibration candidate change

Governance G4
    conformance / findings
        |
        +--> governed operational-change assessment when policy requires it

Governance G5
    Evidence + EvidenceUse
        |
        +--> independently provenanced operational observations used as evidence
```

Operational Execution owns telemetry/external-observation acquisition, operational source trust/authenticity provenance, command/result facts, deployment target application/effective artifact provenance, and modeled-versus-observed reconciliation. Governance owns the durable revision/change/evaluation/evidence-use/finding semantics that may reference those facts.

Operational work may proceed headlessly with clearly scoped synthetic revision, ChangeSet, conformance, or evidence-use fixtures while G1/G2/G4/G5 are incomplete. Those fixtures **do not satisfy Governance gates** and must not escape as duplicate shared production abstractions. When the Governance-owned contract lands, the operational fixture is replaced by an adapter/mapping to it.

## 3. Delivery principles

1. Framework-specific content remains downstream of generic conformance. Do not add SOC 2, ISO 27001, GDPR, or similar fields to core business objects.
2. Do not create a monolithic `BusinessModel`. Each domain retains authoritative ownership of its facts.
3. Distinguish modeled intent from observed reality. Structural facts may be provable from Arcogine state; operational assertions may require external evidence.
4. Reuse external workflow systems where they already own organizational process state. Jira may remain authoritative for issue workflow while Arcogine owns semantic impact, evidence use, and controlled revision lineage.
5. Keep semantic identity and controlled revision identity separate as required by [ADR-0004](../architecture/decisions/0004-model-identity-revision-lineage-and-external-change-control.md) and concretized by [ADR-0008](../architecture/decisions/0008-controlled-revision-identity-and-lineage.md).
6. Treat controlled revision lineage as configuration history and evidence addressability, not as approval, deployment, certification, or compliance state.
7. Keep `ChangeSet`, external workflow references, authorization decisions, deployments, evidence uses, labels, and framework mappings outside the minimum immutable controlled-revision identity core.
8. Treat external requirement provenance as versioned input in addition to Arcogine's own requirement and assertion versioning. A standards-family label is not sufficient when an evaluation depends on a specific issuing authority, designation, edition/version, clause/locator, or adoption/profile.
9. Do not bind raw external operational observations to a model fingerprint/revision at source. The revision relationship belongs to `EvidenceUse`, reconciliation, deployment correlation, or another interpretation record when applicable.
10. Governance authorization and Operational deployment application are separate concerns. Governance may reference the deployment record but does not define adapter/application mechanics.
11. Preserve future lineage extensions without implementing generic source-control semantics prematurely. G1.2 supports `0..1` parent today; branch refs, tags, multi-parent merges, and cryptographic revision-record integrity are deferred rather than forbidden.

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

### Current status

**Partial.** G1.1 is complete: ADR-0006 and the `factory-model:v1` implementation establish the durable semantic fingerprint contract.

**G1.2 is complete.** `:types` provides `ControlledRevisionId`; `:governance` provides the immutable controlled-revision, lineage, and recording-provenance value contracts fixed by ADR-0008. This slice proves local value invariants only; it does not provide authoritative persistence or historical semantic-state resolution.

**G1.3 remains outstanding.** Arcogine still needs authoritative durable controlled-revision persistence, repository-level lineage integrity, and exact revision-to-semantic-state/artifact resolution before the G1 identity/history substrate can be considered complete. Downstream authorization, deployment, and result-provenance integrations validate their own use of G1 identities in their owning capabilities; those integrations do not gate G1 closure.

### Goal

Provide two durable, non-conflated identities and the historical substrate required by later governance work:

```text
ModelFingerprint
    deterministic identity of canonical semantic content

ControlledRevisionId
    opaque identity of one controlled historical occurrence

ControlledRevision
    revision ID
    exactly one model fingerprint
    parent revision IDs: 0..1 in the current capability
    recording provenance:
        recordedAt
        recorder
```

The invariant is:

> **Durable semantic identity is not historical revision identity.**

Equal semantic content may appear in multiple controlled revisions. A revision's existence does not imply approval, authorization, deployment, conformance, certification, or compliance.

### G1.1 — Durable semantic fingerprint

**Status: Complete.**

ADR-0006 defines the versioned, cross-process `ModelFingerprint` contract and the first `factory-model:v1` policy. The implementation supplies the typed fingerprint and canonical binary encoding while retaining legacy `contentHash()` compatibility where needed.

G1.1 remains the semantic-content identity layer only. It does not identify historical occurrences.

### G1.2 — Controlled revision identity and value contract

**Status: Complete.**

ADR-0008 fixes the following contract:

- `ControlledRevisionId` uses UUIDv4 as opaque durable historical identity;
- revision identity is not derived from fingerprint, parent, actor, timestamp, human label, or external workflow ID;
- every controlled revision references exactly one `ModelFingerprint`;
- root revisions have zero parents and current descendants have one parent;
- current `0..1` parent cardinality is a capability constraint, not a permanent architectural limit;
- multiple children may share a parent, so divergence is representable;
- multi-parent merge semantics, branch refs, tags, rebase/cherry-pick semantics, and cryptographic record integrity are deferred but not precluded;
- rollback/reversion creates a new revision and may legitimately reuse an earlier fingerprint;
- minimum provenance records `recordedAt` and the human/service/agent/import source that recorded the revision;
- ID, fingerprint, lineage, and required recording provenance are immutable once accepted by the authoritative revision store;
- `ChangeSet`, external workflow references, approval/authorization, deployment, conformance/evidence, labels, and model artifact storage are not fields in the minimum immutable revision core.

The implemented slice is deliberately narrow:

```text
:types
    ControlledRevisionId

:governance
    ControlledRevision
    RevisionProvenance
    RevisionRecorder (or equivalently narrow recorder value)
```

The implementation proves local value invariants and the `F1 -> F2 -> F1` historical case. It does not create a fake in-memory repository or claim durable persistence.

### G1.3 — Authoritative persistence and historical semantic-state resolution

**Status: Outstanding.**

A controlled revision becomes an authoritative historical fact only when its immutable record is accepted by Arcogine's authoritative revision store. G1.3 must establish the durable persistence boundary without reopening the identity semantics fixed by ADR-0008.

G1.3 must provide or prove:

- durable revision-ID uniqueness and stable ID-to-record binding;
- immutable revision-to-fingerprint and revision-to-provenance relationships;
- repository-level parent existence and lineage integrity;
- exact controlled-revision resolution to the semantic state/artifact required for historical reconstruction;
- persistence semantics sufficient for later ChangeSet reconstruction, conformance attribution, and downstream revision-ID provenance.

G1.3 may require a follow-up ADR when the implementation commits to hard-to-reverse repository, artifact-retention/resolution, integrity, or migration semantics. ADR-0008 intentionally does not choose a database, event store, repository API, blob/artifact format, or physical indexing strategy.

### Required properties

- Equal semantic content can have the same model fingerprint across distinct controlled revisions.
- A later rollback may therefore have the same fingerprint as an earlier revision while remaining historically distinct.
- Controlled revision identity must not be inferred from the fingerprint or a human label such as `v7`.
- The same controlled revision ID must always identify the same immutable revision record.
- Revision lineage must be explicit and independent of fingerprint equality.
- A revision must not name itself as parent; the authoritative store must preserve parent existence/integrity under the accepted lineage policy.
- Recording provenance identifies when Arcogine accepted the historical record and who/what recorded it, without implying authorization.
- Human version/revision labels may exist for presentation but are not fundamental identity.
- External workflow references are associations, not revision identity.
- Authorization, deployment, conformance, evidence, and framework/compliance state remain separate records/projections.

### Acceptance criteria

Criteria 1-5 define completion of the G1 identity/history substrate. Criteria 6-7 are cross-capability compatibility boundaries: G1 must make those references possible without absorbing downstream state into revision identity, but the concrete downstream integrations are accepted in their owning capability slices and are not prerequisites for G1 closure.

G1 is ready when:

1. A durable semantic fingerprint contract is explicitly specified and testable across supported process/version boundaries. **Satisfied by G1.1.**
2. Controlled revision identity/value semantics are implemented according to ADR-0008, including UUIDv4 identity, exactly one fingerprint, current `0..1` parent lineage, rollback-as-new-revision, and immutable recording provenance. **Satisfied by G1.2.**
3. Controlled revisions have authoritative durable identities independent of process memory and semantic fingerprint equality. **G1.3.**
4. The authoritative store enforces revision-ID uniqueness, immutable ID-to-record binding, and parent existence/integrity under the chosen lineage policy. **G1.3.**
5. An authoritative controlled revision can resolve to the exact semantic state/artifact needed for historical reconstruction. **G1.3.**
6. Authorization and deployment records can independently reference a revision without becoming revision identity. **Compatibility boundary fixed by ADR-0008; concrete authorization/deployment integration is accepted downstream.**
7. A downstream result can retain the semantic fingerprint and, when applicable, the controlled revision ID. **Compatibility boundary; concrete result-provenance integration is accepted by the owning producer/consumer capability.**

### ADR status

The identity decisions are no longer open:

- ADR-0004 fixes semantic identity vs. controlled revision identity and the external workflow authority boundary;
- ADR-0006 fixes the durable semantic fingerprint contract;
- ADR-0008 fixes controlled revision identity, current lineage semantics, rollback, minimum provenance, immutability, and the persistence boundary.

A new ADR is needed only if G1.3 commits to a hard-to-reverse persistence/artifact-resolution/integrity choice or if future work extends lineage into multi-parent merge/ref semantics.

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

Define explicit requirements independently of any particular compliance framework while preserving both Arcogine's own requirement/assertion versions and enough external source identity to reproduce the requirement that actually governed an evaluation.

```text
Requirement
    stable identity
    version
    source authority, when external
    source designation, when external
    source edition/version
    source locator
    adoption/profile, when applicable
    human meaning

Scope
    business objects to which it applies

Assertion
    stable identity
    version
    deterministic evaluation rule or evaluator
    required evidence class
```

For an Arcogine-native requirement, the source may be an internal policy or architecture authority rather than an external standard. For an external requirement, a family-level reference such as `ISA-95 / IEC 62264` is insufficient if the obligation depends on a specific publication. The contract must be able to distinguish, for example, an IEC publication from a modified ANSI/ISA adoption or a national adoption without assuming that aligned editions are textually or normatively identical.

External source identity does not replace Arcogine's requirement or assertion version. The same external clause may support multiple Arcogine requirement versions as scope, interpretation, or executable semantics evolve.

The first requirement should be Arcogine-native and structurally evaluable from authoritative model state rather than imported from an external framework.

### Acceptance criteria

G3 is ready when:

1. A requirement has stable identity and can be versioned separately from both the model it evaluates and the edition/version of any external source it cites.
2. An assertion has stable identity and version independently of the requirement and model revision it evaluates.
3. Scope selection is explicit and deterministic.
4. An assertion can declare whether model state alone is sufficient or external evidence is required.
5. Requirement wording/source and executable assertion semantics are distinguishable.
6. An external requirement can identify its exact source authority, designation, edition/version, locator, and applicable adoption/profile where those facts affect meaning.
7. The contract is generic enough to represent internal policy and architecture rules.

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
requirement identity/version
assertion identity/version
scope
result
explanation
affected entities
evaluation time / applicable period
```

A failed result produces a finding rather than mutating the underlying business model.

### Acceptance criteria

G4 is ready when:

1. The same semantic fingerprint/revision and requirement/assertion identities and versions produce deterministic structural results.
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

Operational observations sourced through the sibling Operational Execution capability keep their operational observation ID, source/time/trust provenance, and lifecycle. G5 references those facts as `Evidence`; it does not re-ingest them, add a source-level revision binding, or create a second telemetry identity.

### Acceptance criteria

G5 is ready when:

1. External evidence is distinguishable from Arcogine-derived structural evidence.
2. Evidence is attributable to source and observation time, independent of any model fingerprint or revision.
3. An assertion can combine intended model state with observed external state.
4. An `EvidenceUse` can become stale/invalid when its scope, applicable period, or affected semantics change, without invalidating the underlying `Evidence` record for other uses.
5. A historical evaluation can identify the evidence set (and the `EvidenceUse` bindings) it relied on.
6. A pre-existing operational observation can be referenced as evidence without changing its operational identity or provenance.

## 10. G6 — Governed change and external workflow integration

### Goal

Connect semantic `ChangeSet`s, candidate controlled revisions, and technical evidence to enterprise change-management workflows without recreating Jira inside Arcogine or absorbing operational deployment mechanics.

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
Operational deployment record, when deployed
```

### Authority boundary

Jira or another workflow system may remain authoritative for issue workflow, assignments, discussions, and transitions. Arcogine retains the stable external reference and the technical/governance facts required to explain the semantic change and its resulting revision.

External change references are separate associations/provenance records, not immutable identity fields of `ControlledRevision`.

If Arcogine later owns an authorization decision itself, that decision must be modeled explicitly with actor, authority, and provenance rather than inferred from mutable UI state.

The Operational Execution capability owns applying the authorized revision to a target, adapter/profile/transformation provenance, effective applied-artifact/external-version identity, operational verification/result facts, and reconciliation. G6 owns the governed-change/authorization interpretation and references that operational deployment record when it exists.

### Acceptance criteria

G6 is ready when:

1. A `ChangeSet`/revision can reference an external change request through a separate association/provenance relationship.
2. Impact and conformance information can be surfaced into the change workflow.
3. Approval/authorization records reference the relevant controlled revision rather than defining revision identity.
4. Operational deployment, when it occurs, is separately attributable to the deployed revision through a referenced Operational Execution deployment record.
5. Governance does not duplicate target adapter/application/effective-artifact mechanics owned by Operational Execution.
6. External project-management metadata is not duplicated without semantic need.
7. A reviewer can trace a governed model transition back to the external record that tracked/governed the change.

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
requirement identity/version
assertion identity/version
requirement source identity/version
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

1. A historical compliance result can be reconstructed from explicit versioned inputs, including the Arcogine requirement/assertion identities and versions and the exact external requirement source identity that governed the evaluation, when applicable.
2. The system can explain why a control passed, failed, or was unknown.
3. Evidence and exceptions are traceable to sources and applicable periods.
4. A change in today's Arcogine requirement/assertion versions, framework mapping, or source standard edition does not silently alter a previous audit snapshot.
5. A reviewer can traverse from a versioned requirement and assertion to the governing source, control, affected business objects, semantic fingerprint, controlled revision, evidence, and change history.

## 14. First end-to-end milestone

The first milestone should deliberately avoid a full external compliance framework:

> **Take a proposed semantic change to an Arcogine model, derive its candidate fingerprint, persist a controlled revision linked through a separate association to an external change request, evaluate one Arcogine-native requirement before authorization/deployment, record the authorization decision separately, and reconstruct why the resulting semantic state was considered conformant.**

A concrete example could be an ownership requirement once the relevant owner/authority semantics exist.

Definition of done:

```text
Durable semantic fingerprint contract exists
Controlled revision identity exists
Authoritative controlled revision persistence exists
Exact historical semantic state is resolvable
Base revision -> ChangeSet -> candidate revision is attributable
Affected entity is identified
One versioned requirement applies
Pre-change assertion evaluates deterministically
Finding is produced if violated
External change-request provenance can be linked separately
Authorization is a separate record referencing the revision
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
- a replacement for Jira or another organizational change-management system;
- telemetry ingestion, production command execution, target adapter/application logic, or digital-twin reconciliation inside Governance.

Those may become valid product capabilities later, but they should not distract from the semantic substrate that differentiates Arcogine.

## 16. Documentation and ADR updates as work lands

As implementation progresses:

- update [`../architecture/overview.md`](../architecture/overview.md) only with established current-state behavior;
- update [`../architecture/governance-conformance.md`](../architecture/governance-conformance.md) when this proposed direction changes materially;
- keep [ADR-0004](../architecture/decisions/0004-model-identity-revision-lineage-and-external-change-control.md) as the authority for semantic identity vs. revision identity and the external change-control boundary;
- keep [ADR-0006](../architecture/decisions/0006-durable-semantic-fingerprint-contract.md) as the authority for the durable semantic fingerprint contract;
- keep [ADR-0008](../architecture/decisions/0008-controlled-revision-identity-and-lineage.md) as the authority for controlled revision identity, current lineage cardinality, rollback, recording provenance, immutability, and the persistence boundary;
- update factory/domain architecture docs when identity/change requirements alter those models;
- update the sibling [Operational Execution and Digital Twin Readiness](operational-execution-digital-twin-readiness.md) when Governance G1/G2/G4/G5 contract availability changes its blocked/fixture-backed criteria;
- update [`../architecture/standards-alignment.md`](../architecture/standards-alignment.md) when Arcogine moves from reference/mapping toward an actual tested conformance profile;
- create a follow-up ADR for G1.3 only when implementation commits to hard-to-reverse persistence, artifact-resolution, migration, retention, or revision-record-integrity semantics;
- create later lineage ADRs only when concrete branch/ref/multi-parent merge semantics are required;
- create ADRs for semantic `ChangeSet` contracts, temporal evidence semantics, and hard-to-reverse external protocols when implementation commits to them;
- update product/reference docs only for capabilities that actually ship.

Once this initiative is complete or superseded, retain durable decisions in ADR/current architecture and retire or reduce this planning artifact.
