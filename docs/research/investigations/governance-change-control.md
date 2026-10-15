# Governance change control beyond the landed substrate

Status: CANDIDATE brief; no conclusion — promote only when a concrete governed-change use names its requirement

## Question and decision

What must be settled before Arcogine implements Governance capability beyond its landed identity, change, requirement, conformance and evidence substrate: governed change with authorization and external workflow association, exceptions and risk acceptance, framework/control mappings, and reproducible audit projections?

None of them is implemented. Their design, which the [Governance architecture](../../architecture/governance-conformance.md) carried until it was limited to implemented behavior, is kept [below](#design-kept-from-the-architecture--not-implemented). This question decides which of them a concrete use actually needs first, and what still blocks an implementation-ready conclusion for it.

## Landed baseline

The landed substrate is described in [Governance architecture §13](../../architecture/governance-conformance.md#13-relationship-to-current-factory-model-and-operational-work) and the [Governance evidence contract](../../architecture/governance-evidence.md): durable fingerprint and controlled-revision history realized by built-in [Storage](../../architecture/storage.md), semantic change and impact, versioned requirements and assertions, deterministic pre-change evaluation and findings, and headless evidence use with accepted evaluation occurrences. It has no durable evaluation history, durable requirement-definition storage, or production producer integration.

## Known blockers

- **Accountable decisions.** Authorization and exceptions attribute decisions to an accountable approver. The [Attribution and decision boundaries](../../architecture/overview.md#attribution-and-decision-boundaries) settle roles and provenance, but not a shared actor identity, which is a separate candidate question in the [research register](../research-register.md).
- **Historical reconstruction.** A reproducible audit projection needs durable evaluation history and definition resolution that do not exist yet; stronger retained history is the [historical storage support](storage-historical-support.md) question.
- **Open reconciliation triggers.** The [Governance architecture](../../architecture/governance-conformance.md#16-architecture-reconciliation-triggers) lists the exception and risk-acceptance lifecycle, and authority protocols with an external governance system, among the hard-to-reverse choices still to be reconciled.

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

## Design kept from the architecture — not implemented

This is the direction the Governance and Factory design architecture described before they were limited to implemented behavior. It is input to the question above, not a decided specification: a conclusion may keep, narrow or replace any part of it, and the constraints above still apply.

### Compliance as a downstream projection

The intended dependency direction continues the implemented chain — semantics, fingerprint and controlled revision lineage, semantic changes, requirements, assertions, findings and evidence — upward into governance, risk and controls, and from there into framework requirement mappings:

```text
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

### Controls and frameworks are mappings, not business truth

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

### Richer semantic-change impact

The implemented `ChangeSet` carries base and candidate identity, semantic changes and the affected entities. The intended impact goes further:

```text
ChangeSet
   |
   +--> affected requirements
   +--> affected controls
   +--> required reviewers
   +--> required evidence
   +--> invalidated prior evidence
```

### Governed change, authorization and deployment

The intended relationship between an external change request, a `ChangeSet`, a candidate revision, its authorization and its deployment is:

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

The Factory design described the same bridge from a candidate change through a persisted controlled revision, technical assessment (validation, simulation, verification), authorization and an operational deployment plan to the observed result and reconciliation.

- The revision is persisted before authorization, not after: an unauthorized revision, or one authorized but never deployed, must remain representable. Authorization and deployment are separate records that reference the revision, not steps a revision passes through to come into existence.
- Governance owns the revision/change/authorization interpretation. Operational Execution owns applying an authorized revision to an execution target and recording the effective adapter/profile/transformation/applied-artifact provenance and operational result; a Governance deployment reference points to that record rather than redefining deployment mechanics.
- Authorization may be owned externally: Arcogine can produce the technical assessment evidence a candidate needs without hosting the request/review/approval workflow. What authorization a deployment requires is a matter of the applicable change-control policy, which Arcogine does not own; the invariant is narrower — a deployment must be attributable to the authorization that policy required for it.
- Pre-change evaluation closes the loop: impact analysis adds evidence invalidation and required authorization or review, and the pre-change conformance result feeds an authorize / reject / revise decision.

### Finding severity, remediation, exceptions and risk acceptance

A failed assertion may be remediated, accepted temporarily, or explicitly excepted.

```text
Finding
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

### Historical questions and reproducible audit snapshots

The substrate was meant eventually to answer, beyond the fingerprint, revision and semantic change it answers today: why a change was proposed, which external change record governed it, who or what approved it, whether and where it was deployed, which effective applied artifact governed each deployment target, which requirements were affected, and which evidence supported the resulting conformance state.

An audit view should be reconstructible from versioned inputs rather than stored only as an opaque dashboard state:

```text
AuditSnapshot
    model fingerprint
    controlled revision ID
    exact requirement definition used (identity / version resolved)
    exact assertion definition used (identity / version resolved)
    requirement source identity / version
    framework / mapping version, when applicable
    control mappings
    evaluation occurrences and results
    evidence basis: uses relied on, material excluded, known gaps
    exceptions / risk acceptances
    generated at
```

The intended invariant: given the relevant semantic fingerprint, controlled revision, the exact requirement and assertion definitions actually used, the exact external requirement source identity/version when applicable, framework/mapping versions, the accepted evaluation occurrences with their fixed evidence basis, and governance decisions, Arcogine can explain how a historical conformance result was derived. An audit view is a projection over accepted occurrences and their fixed basis, not a fresh evaluation: it never substitutes current definitions, a current evidence query, a current source revision, or re-execution under current semantics. Where necessary historical material is missing or corrupt, the projection discloses the gap. A historical `PASS` is the recorded outcome of that occurrence, not a current assurance that the claim remains true.
