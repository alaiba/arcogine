# Governance and Conformance Capability Plan

> **Status:** Proposed  
> **Scope:** Establish the cross-domain substrate for durable model history, semantic change, requirements, conformance, evidence, and governed change  
> **Authority:** Planning only; this document defines delivery dependencies and readiness criteria, not current product capability  
> **Related:** [Governance and Conformance Architecture](../architecture/governance-conformance.md), [Product Charter](../product/charter.md), [Factory Design Capability Plan](factory-design-capability.md), [Factory Design Architecture](../architecture/factory-design.md), [Standards Alignment](../architecture/standards-alignment.md)

## 1. Purpose

Arcogine should not respond to the opportunity for compliance automation by building framework checklists first. The immediate product and architecture opportunity is to make governance a derived property of Arcogine's authoritative semantic models and their history.

This plan therefore establishes a generic sequence:

```text
Canonical domain models
        |
        v
Durable identity and lineage
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

The work remains valuable even if Arcogine never competes directly with a compliance-automation vendor. The same primitives support design review, architecture governance, deployment approval, internal policy, safety constraints, customer commitments, auditability, digital-twin reconciliation, and agent governance.

## 2. Relationship to current factory-model work

The canonical factory-model work is the first implementation proving ground.

D1-D4 of the [Factory Design Capability Plan](factory-design-capability.md) establish the initial seam:

```text
FactoryModel
    -> structural validation
    -> immutable publication
    -> content-derived identity
    -> runtime instantiation
    -> provenance
```

That seam should complete without being blocked by a generic governance framework.

However, the governance/compliance use case now provides a concrete cross-consumer reason to revisit work previously deferred in the factory plan. The dependency order matters here, and matches the G1-G9 sequence in §4 below, not the order these concerns happen to be listed elsewhere:

```text
durable model identity and lineage (G1)
          ↓
semantic ChangeSet (G2)
          ↓
requirement-based conformance/evidence (G3-G5)
          ↓
review/approval/governed-change integration (G6)
```

Evaluating a proposed change's conformance before it is authorized is the strategic point (see [architecture §11](../architecture/governance-conformance.md#11-pre-change-conformance-is-strategically-important)); an approval/deployment integration that isn't preceded by conformance evaluation would authorize changes Arcogine hasn't yet assessed.

The key refinement is:

> **D5 semantic comparison is no longer only an editor convenience. It is an enabling primitive for governed change and impact analysis once the initial D1-D4 seam is stable.**

This does not mean implementing arbitrary patch/merge infrastructure. The need is semantic change attribution, not generic collaborative text editing.

## 3. Delivery principles

The delivery sequence should preserve several constraints.

First, framework-specific content must remain downstream of generic conformance. Do not add SOC 2, ISO 27001, GDPR, or other framework fields to core business objects.

Second, do not create a monolithic `BusinessModel` merely to support cross-domain governance. Each domain keeps authoritative ownership of its facts. Cross-domain lineage, identity references, changes, requirements, and evidence provide the governance layer.

Third, distinguish modeled intent from observed reality. Arcogine model state can prove structural facts it owns, but operational assertions may require external observations.

Fourth, reuse external workflow systems where they already own process state. Jira can remain authoritative for ticket workflow while Arcogine owns the semantic meaning, affected entities, conformance impact, and resulting model lineage.

## 4. Delivery sequence

```text
G1  Durable model identity and lineage
    ↓
G2  Semantic ChangeSet and impact model
    ↓
G3  Generic requirement/assertion contract
    ↓
G4  Conformance evaluation and findings
    ↓
G5  Evidence and observation provenance
    ↓
G6  Governed change and Jira integration
    ↓
G7  Exceptions and risk acceptance
    ↓
G8  Framework/control mappings
    ↓
G9  Audit snapshots and compliance projections
```

G1-G5 are architectural substrate. G6-G7 establish governance workflow. G8-G9 make conventional compliance automation possible without turning compliance into Arcogine's core ontology.

## 5. G1 — Durable model identity and lineage

### Goal

Extend the initial in-memory publication identity into a durable lifecycle that can answer how one authoritative model version relates to another.

The design should support concepts equivalent to:

```text
ModelIdentity
ModelVersion
ParentVersion / lineage relationship
ContentHash
SchemaVersion
Publication provenance
Created/published timestamp
Author or decision source
```

The exact persistence technology and public identifiers are implementation decisions.

### Required properties

A content hash and a durable version identity must not be conflated. Equivalent semantic content may intentionally have the same content hash while appearing in different provenance contexts; conversely a durable version identifier represents a historical artifact and its lineage.

The system should eventually support reconstructing the authoritative model state used by an evaluation, simulation, deployment, or audit.

### Acceptance criteria

G1 is ready when:

1. Published versions have durable identities independent of process memory.
2. Version lineage can identify predecessor/parent relationships under the chosen policy.
3. Semantic content remains immutable after publication.
4. Content equality/hash semantics are explicitly documented separately from version identity.
5. Provenance records who or what caused publication and when.
6. A downstream result can refer to a durable model version rather than only a transient runtime object/hash.

### ADR trigger

The identity/version/lineage scheme is hard to reverse and should be recorded in an ADR before it becomes a public persistence contract.

## 6. G2 — Semantic ChangeSet and impact model

### Goal

Represent the meaningful transition between authoritative model versions in domain terms sufficient for review, impact analysis, and governance.

A `ChangeSet` or equivalent should be able to identify:

```text
base model version
proposed/resulting model version or draft
semantic changes
changed entities
change source/reason
external change-request reference
```

Changes should be typed or otherwise semantically classified where the domain has enough meaning to do so.

### Non-goal

Do not build generic JSON patching, arbitrary text merge, collaborative cursors, or a distributed source-control system for models merely to satisfy G2.

### Impact analysis

The first useful impact queries are:

```text
Which business objects are affected?
Which requirements evaluate over those objects?
Which existing evidence becomes stale or invalid?
Which owners/reviewers have authority over the affected scope?
Which runtime/deployment contexts would consume the changed model?
```

### Acceptance criteria

G2 is ready when:

1. Two relevant model versions can be compared semantically rather than only byte-for-byte.
2. Changed entities can be identified with stable identity.
3. At least one domain change has a meaningful typed/classified representation.
4. Impact analysis can determine which registered requirements are potentially affected.
5. ChangeSet provenance can retain an external ticket/change-request identifier.

## 7. G3 — Generic requirement and assertion contract

### Goal

Define explicit requirements independently of any particular compliance framework.

A minimal contract should distinguish:

```text
Requirement
    stable identity
    version/source
    human meaning

Scope
    business objects to which it applies

Assertion
    deterministic evaluation rule or evaluator
    required evidence class
```

The first requirement should be Arcogine-native and structurally evaluable from authoritative model state.

Example:

```text
Production resources require accountable ownership.
```

This is preferable to beginning with a SOC 2 control because it tests the architecture without importing an external framework ontology prematurely.

### Acceptance criteria

G3 is ready when:

1. A requirement can be versioned separately from the model it evaluates.
2. Scope selection is explicit and deterministic.
3. An assertion can declare whether model state alone is sufficient or external evidence is required.
4. Requirement wording/source and executable assertion semantics are distinguishable.
5. The contract is generic enough to represent internal policy and architecture rules.

## 8. G4 — Conformance evaluation and findings

### Goal

Evaluate requirements against a specific authoritative model version and produce explainable results.

A useful result model should support at least:

```text
PASS
FAIL
UNKNOWN
NOT_APPLICABLE
```

where `UNKNOWN` represents insufficient authoritative evidence rather than silently passing or failing.

An evaluation should retain:

```text
model version
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

1. The same model/version and requirement/assertion versions produce deterministic structural results.
2. Findings identify affected entities and the failed assertion.
3. Results distinguish missing evidence from proven non-conformance.
4. Evaluation output is immutable or historically attributable.
5. A proposed ChangeSet can be evaluated before publication for at least one requirement.

The last criterion is the first major strategic milestone: pre-change conformance rather than only post-change monitoring.

## 9. G5 — Evidence and observation provenance

### Goal

Support assertions whose truth depends on observations outside Arcogine's authoritative semantic model.

Per [Evidence must be attributable and temporal](../architecture/governance-conformance.md#9-evidence-must-be-attributable-and-temporal), the evidence source record and its use in one evaluation are distinct:

```text
Evidence (source-level, no model version)
    source
    provenance
    observedAt
    applicable period
    external identity/reference
    integrity metadata where required

EvidenceUse (binds evidence to one evaluation)
    related assertion/control
    related model version (or controlled revision, once G1 exists)
    scope/applicability at time of use
```

External evidence is generally reusable across model versions as long as each `EvidenceUse` independently re-establishes scope and applicability; only structural evidence derived directly from Arcogine's own model state naturally collapses the two into one record. Initial adapters should be driven by a concrete requirement, not a desire to match a vendor's integration count.

### First adapter selection

Choose an external source only after one assertion requires it. Good candidates will be systems where the authority boundary is clear, such as source control, identity, cloud configuration, or Jira change records.

### Acceptance criteria

G5 is ready when:

1. External evidence is distinguishable from Arcogine-derived structural evidence.
2. Evidence is attributable to source and observation time, independent of any model version.
3. An assertion can combine intended model state with observed external state.
4. An `EvidenceUse` can become stale/invalid when its scope, applicable period, or the affected model semantics change, without invalidating the underlying `Evidence` record for other uses.
5. A historical evaluation can identify the evidence set (and the `EvidenceUse` bindings) it relied on.

## 10. G6 — Governed change and Jira integration

### Goal

Connect semantic ChangeSets and conformance results to enterprise change-management workflows without recreating Jira inside Arcogine.

Target relationship:

```text
Jira issue / change request
        |
        v
Arcogine ChangeSet
        |
        +--> semantic impact
        +--> pre-change conformance
        +--> required approvals/owners
        |
        v
external workflow decision
        |
        v
authorized publication/deployment
        |
        v
model lineage + evidence
```

### Authority boundary

Jira may remain authoritative for issue workflow, assignments, discussions, and workflow transitions. Arcogine should retain only the ticket identity and governance facts required to explain why a semantic model transition was authorized.

If Arcogine later owns an approval decision itself, that decision must be modeled explicitly with actor/authority/provenance rather than inferred from mutable UI state.

### Acceptance criteria

G6 is ready when:

1. A ChangeSet can reference a Jira change request.
2. Impact and conformance information can be surfaced into the change workflow.
3. Publication/authorization provenance records the relevant external workflow identity/status or explicit Arcogine decision.
4. Jira project-management metadata is not duplicated without semantic need.
5. A reviewer can trace an authorized model transition back to the governing change request.

## 11. G7 — Exceptions and risk acceptance

### Goal

Represent explicit governance decisions when a known non-conformance is tolerated temporarily or conditionally.

The model should distinguish:

```text
Finding
Remediation
Exception
RiskAcceptance
CompensatingControl
Expiration
```

An approved exception does not change a failed assertion into `PASS`. It changes the governance disposition of the finding.

### Acceptance criteria

G7 is ready when:

1. Findings retain their factual conformance result independently of disposition.
2. Exceptions have rationale, accountable owner/approver, and effective/expiration periods.
3. Expired exceptions become visible without rewriting history.
4. Compensating controls/evidence can be linked when used.

## 12. G8 — Controls and framework mappings

### Goal

Introduce conventional compliance abstractions only after generic conformance and evidence are working.

Conceptually:

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

Do not attempt broad framework coverage. Select one small, legally permissible set of requirements sufficient to prove cross-framework mapping and evidence reuse. Framework content licensing and authoritative source terms must be reviewed before importing copyrighted or proprietary control text.

### Acceptance criteria

G8 is ready when:

1. Requirements and controls are versioned independently of business model versions.
2. One control can map to more than one requirement/framework.
3. Business objects do not acquire framework-specific compliance booleans.
4. Framework/mapping changes do not mutate historical evaluations.
5. Evidence reuse respects scope, time, and semantic compatibility.

## 13. G9 — Audit snapshots and compliance projections

### Goal

Produce a reproducible audit/compliance view over versioned model state, requirements, controls, evidence, findings, and exceptions.

An audit snapshot should identify:

```text
model version
framework/requirement versions
control mappings
assertion results
supporting evidence
findings
exceptions/risk acceptances
generation time
```

The first useful UX can be headless/export-oriented. Do not build a large GRC dashboard before the underlying historical semantics work.

### Acceptance criteria

G9 is ready when:

1. A historical compliance result can be reconstructed from explicit versioned inputs.
2. The system can explain why a control passed, failed, or was unknown.
3. Evidence and exceptions are traceable to their sources and applicable periods.
4. A change in today's framework mapping does not silently alter a previous audit snapshot.
5. An auditor/reviewer can traverse from a requirement to control, assertion, affected business objects, evidence, and change history.

## 14. First end-to-end milestone

The first milestone should deliberately avoid a full external compliance framework:

> **Take a proposed semantic change to a versioned Arcogine model, determine which explicit Arcogine-native requirement it affects, evaluate the requirement before publication, link the change to a Jira change request or equivalent provenance, record the authorized transition to the next model version, and reconstruct why the resulting state was considered conformant.**

A concrete example could be an ownership requirement once the relevant owner/authority semantics exist.

Definition of done:

```text
Durable model version identity exists
Base -> proposed ChangeSet is attributable
Affected entity is identified
One versioned requirement applies
Pre-change assertion evaluates deterministically
Finding is produced if violated
External change-request provenance can be linked
Authorized publication creates a new immutable version
Historical evaluation remains attributable after later changes
No framework-specific field exists on the business object
```

This milestone proves the architecture that later compliance automation depends on.

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
- generic model Git semantics (branch/merge/rebase) without a concrete workflow;
- a replacement for Jira.

Those may become valid product capabilities later, but they should not distract from the semantic substrate that differentiates Arcogine.

## 16. Documentation and ADR updates as work lands

As implementation progresses:

- update [`../architecture/overview.md`](../architecture/overview.md) only with established current-state behavior;
- update [`../architecture/governance-conformance.md`](../architecture/governance-conformance.md) when the proposed architectural direction changes materially;
- update factory/domain architecture docs when semantic identity/change requirements alter those models;
- update [`../architecture/standards-alignment.md`](../architecture/standards-alignment.md) when Arcogine moves from reference/mapping toward an actual tested conformance profile;
- create ADRs for durable identity/lineage, semantic ChangeSet contracts, temporal evidence semantics, and external authority boundaries when implementation commits to them;
- update product/reference docs only for capabilities that actually ship.

Once this initiative is complete or superseded, retain durable decisions in ADR/current architecture and retire or reduce this planning artifact.
