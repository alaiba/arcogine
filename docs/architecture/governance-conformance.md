# Governance and Conformance Architecture

> **Status:** Proposed architectural reference  
> **Scope:** Cross-domain model history, semantic change, requirements, conformance, evidence, and governance over Arcogine's canonical business semantics  
> **Authority:** Proposed architecture; this document does not claim current compliance, audit, or certification capability  
> **Related:** [Product Charter](../product/charter.md), [Architecture Overview](overview.md), [Factory Design Architecture](factory-design.md), [Operational Execution and Digital Twin Architecture](operational-execution-digital-twin.md), [ADR-0003](decisions/0003-canonical-factory-model-boundary.md), [ADR-0004](decisions/0004-model-identity-revision-lineage-and-external-change-control.md), [ADR-0006](decisions/0006-durable-semantic-fingerprint-contract.md), [ADR-0008](decisions/0008-controlled-revision-identity-and-lineage.md), [ADR-0013](decisions/0013-durable-operational-identity.md), [ADR-0015](decisions/0015-engine-semantics-identity-and-reproducibility.md), [ADR-0016](decisions/0016-governance-evidence-provenance.md), [Standards Alignment](standards-alignment.md), [Governance and Conformance Capability Plan](../planning/governance-conformance-capability.md)

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

`recorder` is **recording provenance, not attribution and not authorization**: it identifies what caused Arcogine to record the revision, and a recorder is not thereby an approver, reviewer, owner, or deployer. It is recorded as a whole. Its internal `source` / `subject` decomposition is deliberately **underspecified** — ADR-0008 permits a small source/subject value without fixing which slot carries a mechanism and which carries a party, so neither slot may be read as a canonical channel or a canonical actor identity. Because the recorder is persisted and participates in idempotency equality in immutable revision history, it must not be renamed, reinterpreted, or mechanically migrated into any future attribution capability; such a capability must be **additive**. The cross-cutting separation between attributable actor, recording provenance, decision provenance, and external data source is recorded in [Architecture Overview — Attribution and decision boundaries](overview.md#attribution-and-decision-boundaries).

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

A controlled revision becomes an authoritative historical fact only when its immutable record is accepted by Arcogine's authoritative revision store. ADR-0008 defines the identity, immutability, lineage, and provenance semantics; Governance authoritative controlled-revision persistence and historical resolution now implements the corresponding authority boundary and exact historical resolution. `ControlledRevisionAuthority` accepts candidate revision content, the authority establishes the accepted record's `recordedAt` at its commit boundary, and the current filesystem adapter durably binds the accepted revision to its immutable semantic artifact. Historical resolution re-verifies the artifact against the recorded `ModelFingerprint`; missing or corrupt history fails explicitly rather than falling back to current state. The filesystem record layout and locking mechanics remain replaceable adapter details rather than a permanent production persistence contract.

**Custody is declared by the authority, and retained is the default** ([ADR-0017](decisions/0017-semantic-contract-maturity-and-support-promotion.md) §4). Whether an accepted artifact is disposable or attributable and retained is decided by the declared custody of the authority holding it, not by the fact that `accept(...)` was called: an authority is retained custody unless it is explicitly declared ephemeral (a test-owned temporary authority, for example), and everything accepted into retained custody is kept for the horizon of the promise governing it. A retained authority may accept an artifact produced under a **proving** semantic-contract revision only when its record preserves the exact definition revision the artifact was produced under together with the applicable custody declaration and support horizon; an authority that cannot record both must reject the artifact explicitly rather than accept it and later resolve it under a different definition. Today the authority verifies artifacts only through registered policy verifiers, and only the promoted `factory-model:v1` policy is registered, so every accepted artifact is already bound to a promoted, permanently resolvable definition and no proving-policy artifact can be accepted anywhere; recording a custody declaration becomes an executable obligation of the first change that registers a proving-policy verifier.

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
    scope

Assertion
    stable identity
    version
    associated requirement identity/version
    expression/evaluator
    evidence requirements

Evaluation occurrence
    occurrence identity, once accepted
    exact requirement definition used
    exact assertion definition used
    model fingerprint
    controlled revision ID, when available
    temporal frame / knowledge boundary
    evidence basis: uses relied on, material considered but excluded, known gaps
    applicability and interpretation rules applied
    result and explanation

Finding
    affected entities
    severity
    explanation
    remediation state
```

A requirement's provenance must identify the exact normative or governing source when its meaning depends on an external standard, regulation, contract, or policy. Family-level labels such as `ISA-95 / IEC 62264` are insufficient for an auditable requirement because closely aligned standards, editions, and national adoptions can differ. The requirement must retain enough source identity to determine what text and obligations governed a historical evaluation.

External source identity is separate from Arcogine's own requirement and assertion identities and versions. The same external clause or policy source may support multiple Arcogine requirement versions as scope, interpretation, or executable semantics evolve; source provenance therefore augments rather than replaces Arcogine versioning.

A structural requirement can be evaluated solely from authoritative model state; an operational requirement may need external observation. `UNKNOWN` is important: absence of evidence must not silently become success or failure when the underlying fact is genuinely unobserved.

[ADR-0016](decisions/0016-governance-evidence-provenance.md) is **Accepted** and fixes what a completed evaluation is once evidence is involved: an identifiable, immutable **evaluation occurrence** whose basis — the exact requirement/assertion definitions used, the subject fingerprint and optional verified revision, the evidence uses relied on, material excluded or missing, the temporal frame, the applicability rules applied, and the outcome — is fixed when the occurrence is accepted. Two evaluations with equal inputs and equal outcomes remain distinct occurrences. Identity plus version labels are not by themselves proof that the historical definitions remain resolvable: the landed `Requirement`/`Assertion` equality deliberately excludes wording, source, and rule, so an evidence-capable evaluation must preserve or authoritatively resolve the exact definitions it used. The landed `ConformanceEvaluation` is a deterministic value without occurrence identity or an acceptance boundary; occurrence identity is an additive obligation on the first evidence-capable slice, not a change to the existing value contract.

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

The evidence contract is fixed by [ADR-0016](decisions/0016-governance-evidence-provenance.md), which is **Accepted**. This section summarizes it and the rest of this document does not extend it.

An external observation does not intrinsically belong to one Arcogine model fingerprint or controlled revision. An AWS configuration snapshot, an IdP login log, a PLC measurement, or a Jira approval artifact has its own authority, its own observation time, and often its own applicable period, independent of which Arcogine model version or revision happens to exist when it is captured or used. Binding the observation itself to one fingerprint/revision either forces duplicating identical evidence across every subsequent version or misrepresents the observation's actual provenance. Arcogine therefore separates the evidence itself from any particular evaluation's use of it:

```text
Evidence
    evidence reference: one attributable recorded source/result revision
    source / producer
    intrinsic provenance
    described subject, fact/result, units
    source or production time, or explicit uncertainty
    applicable period, where the source establishes one
    integrity metadata where required

EvidenceUse (a.k.a. EvaluationEvidence)
    evidence reference
    evaluation occurrence / assertion / control relationship
    role: relied on, considered but not relied on, comparator, ...
    target model fingerprint
    target controlled revision ID, when applicable
    scope and temporal frame at time of use
    applicability / reliance determination
```

**Reference and equality.** An evidence reference identifies one particular attributable recorded assertion, observation, artifact revision, or analytical result revision — not the external subject, the value, an artifact name, the evaluation, or the truth of the claim. Equality means the same referenced source/result revision, never byte equality, logical equivalence, common subject, corroboration, trust, or applicability. A producer-owned immutable or versioned handle can be the reference; Governance allocates no second global identifier where the producer identity already suffices. A bare digest is content identity, not source-occurrence identity, and a source's stable logical ID participates only while the exact relied-on revision or capture stays identifiable. The same complete reference never later resolves to different identity-bearing content; correction, retraction, supersession, or reinterpretation creates a distinguishable attributable relationship rather than rebinding the reference or rewriting an earlier evaluation's basis. Redelivery of one source record is the same evidence, not a second corroborating observation.

**Intrinsic provenance versus use target.** `Evidence` carries whatever provenance is intrinsic to how it was produced; `EvidenceUse` carries the target it was later used against. A raw external observation is independent of Arcogine model/revision identity at ingestion unless its source intrinsically owns such provenance, and correspondence to an Arcogine subject is a later use or reconciliation determination that is never fabricated at ingestion. An Arcogine-derived analytical or verification result may, and where its producer's contract establishes it must, retain producer-intrinsic `ModelFingerprint`, controlled revision, `EngineSemanticsVersion`, run/result identity, explicit result-affecting inputs, and analytical definition/version. The use target may differ from the source model — a `baseline` result used as a comparator for `changed` is an explicit use role, not proof about `changed`. Missing producer provenance is recorded as missing, never inferred or stamped on. One `Evidence` item may be referenced by many `EvidenceUse` records across multiple model versions and evaluations, as long as each use's scope/applicability determination independently holds; equal fingerprints or a rollback never copy one use's applicability to another target.

**Roles, not necessarily records.** Evidence and use are distinct semantic roles. A representation may embed them together, reference one from the other, or copy source material into an evaluation-local record, provided the independent source identity, intrinsic provenance, and use context remain distinguishable and copies never masquerade as independent corroboration. For a fact intrinsic to immutable Arcogine semantic state, the model version is the evidence's own provenance and the two may collapse into one record; that collapse cannot erase the evaluation occurrence, the exact definitions used, or later distinct uses.

**Applicability is three determinations.** Evidence applicability/reliance (source and subject correspondence, period/freshness, provenance and trust adequacy, meaning and units, compatibility) is a use-owned, attributable, recoverable determination. Requirement applicability is scope, and `NOT_APPLICABLE` arises only from scope. Outcome is the assertion's own judgement over an adequate basis. Stale, out-of-period, wrong-subject, untrusted, incompatible, or incomplete evidence never silently produces `PASS` and never alone makes an applicable requirement `NOT_APPLICABLE`; missing evidence ordinarily supports `UNKNOWN` unless the assertion's own semantics establish a violation from adequate evidence of absence. A single unusable item does not poison an otherwise sufficient basis; considered-but-excluded material, unresolved conflicts, and known gaps stay part of the evaluation basis rather than being manufactured as evidence. Cross-version compatibility is explicit, claim-specific, and owned by the consuming use per [ADR-0015](decisions/0015-engine-semantics-identity-and-reproducibility.md); equal or different Engine semantics versions alone never establish comparability. Consumer-specific freshness, admissibility, coverage, conflict, and compatibility policies remain open.

**Point targets only.** A use binds evidence to exact point-in-time subjects — fingerprint, verified revision, semantic scope, evaluation occurrence. Evidence about an accountable operational continuation over time is the accumulating-identity limit [ADR-0013](decisions/0013-durable-operational-identity.md) §11 records; it is outside this contract and must not be obtained by reinterpreting the point-identity rule.

Operational observations retain the identity and provenance assigned by the Operational Execution capability. Governance must reference them; it must not rewrite them into revision-bound telemetry records in order to use them as evidence, and it does not take ownership of Operational acquisition, correspondence, or trust semantics, or of Engine/analytics calculation semantics, by consuming their attributable results.

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

The desired invariant is:

> Given the relevant semantic fingerprint, controlled revision, the exact Arcogine requirement and assertion definitions actually used, exact external requirement source identity/version when applicable, framework/mapping versions, the accepted evaluation occurrences with their fixed evidence basis, and governance decisions, Arcogine can explain how a historical conformance result was derived.

An audit view is a projection over accepted occurrences and their fixed basis, not a fresh evaluation: it never substitutes current definitions, a current evidence query, a current source revision, or re-execution under current semantics for the historical basis. Where necessary historical material is missing or corrupt, the projection discloses the gap rather than reconstructing around it. A historical `PASS` is the recorded outcome of that occurrence, not a current assurance that the claim remains true.

## 13. Relationship to current factory-model and operational work

The factory model is the first implemented proving ground for this broader architecture, not yet a complete business model.

Current factory/Governance work establishes:

```text
canonical semantic model
immutable publication
structural validation
durable factory-model:v1 semantic fingerprint
authoritative durable controlled-revision history
exact historical factory semantic-artifact reconstruction
runtime instantiation from a published model
runtime/result provenance work in progress
```

ADR-0006 and its implementation establish the durable factory-model fingerprint contract. ADR-0008 establishes controlled revision identity/lineage, and Governance Governance identity/history capability is complete: controlled-revision identity and lineage supplies the controlled-revision identity/value contracts in `:types` and `:governance`, while authoritative controlled-revision persistence and historical resolution supplies the authoritative acceptance/repository boundary, repository-level parent integrity, restart-durable storage, and exact revision-to-semantic-artifact resolution. The current proving adapter is filesystem-backed and factory artifacts reuse canonical `factory-model:v1` bytes; neither choice changes the durable identity semantics. Governance semantic ChangeSet/impact capability is now implemented for its initial slice: the generic `ChangeSet`/`ImpactScope`/`SemanticChange` value contracts in `:governance`, the domain-owned `SemanticChangeExtractor` seam, and the factory-domain `FactoryModelSemanticComparator` (Factory semantic-comparison capability) that compares `factory-model:v1` artifacts by stable domain identity while still honoring the ADR-0006 order-significance of `resources`, `operations`, and `products`. Governance requirements/assertions capability is now implemented: the generic, domain-neutral `Requirement`/`RequirementScope`/`RequirementSource` and `Assertion`/`EvidenceRequirement`/`AssertionRule` value contracts in `:governance`, plus an immutable `RequirementCatalogue` that resolves requirements by identity/version and selects those whose `RequirementScope` intersects a real semantic ChangeSet/impact capability `ImpactScope`. Governance conformance evaluation/findings capability is now implemented for its initial slice: the generic `ConformanceResult` (`PASS`/`FAIL`/`UNKNOWN`/`NOT_APPLICABLE`) taxonomy, the deterministic `ConformanceEvaluator` that turns one requirements/assertions capability `Requirement`/`Assertion` pair and a model fingerprint (with an optional, never-synthesized `ControlledRevisionId`) into a `ConformanceEvaluation`, and the immutable `Finding` type produced only for `FAIL`. Arcogine still does **not** have the evidence/evidence-use capability evidence, authorization, deployment, or framework-mapping capabilities described later in this architecture. The evidence/evidence-use semantic contract is now Accepted in [ADR-0016](decisions/0016-governance-evidence-provenance.md) — reference/equality/non-rebinding, evidence versus use roles, producer-intrinsic versus use-target provenance, the three applicability/outcome determinations, evaluation-occurrence identity, exact definition resolution, and the point-identity limit — while its representation, storage, identifier scheme, and first-implementation admission remain planning and implementation responsibilities. ADR-0016 also bounds what a first implementation may claim: structural facts have a landed producer identity, but Engine result identity and observation/event provenance propagation, Operational observation/correspondence/trust identity, analytical-definition ownership, and durable requirement-definition/evaluation-history storage do not yet exist, so a fixture that carries explicitly attributed material proves the seam and not the integration.

The Governance dependency is now:

```text
durable semantic fingerprint contract durable semantic fingerprint        complete
    ↓
controlled-revision identity and lineage controlled revision value contract  complete
    ↓
authoritative controlled-revision persistence and historical resolution authoritative persistence +
     historical semantic-state resolution  complete
    ↓
semantic ChangeSet/impact capability ChangeSet (initial slice)             complete
    ↓
requirements/assertions capability requirement/assertion contract        complete
    ↓
conformance evaluation/findings capability conformance evaluation/findings
     (initial slice)                     complete
    ↓
evidence/evidence-use capability evidence
    ↓
governed-change and external-workflow integration governed-change/authorization integration
```

The Operational Execution and Digital Twin track is a sibling consumer/proving ground for Governance identity/history capability/semantic ChangeSet/impact capability/conformance evaluation/findings capability/evidence/evidence-use capability, not an alternate owner. It may use clearly scoped synthetic fixtures while the specific Governance capabilities it depends on are incomplete, but such fixtures do not satisfy Governance gates and must be replaced by Governance-owned contracts when those gates land. Governance identity/history capability consumers may now depend on the durable controlled-revision boundary itself; that does not imply a runtime or deployment has already been bound to a particular revision.

Other authoritative domain models should participate without being forced into one monolithic `BusinessModel` aggregate. Each domain retains ownership of its facts while cross-domain identity references, lineage, semantic changes, requirements, and evidence form the governance graph over them.

## 14. What this architecture does not claim

This proposal does not mean that Arcogine currently:

- implements SOC 2, ISO 27001, ISO 9001, GDPR, or another compliance framework;
- provides auditor workflows or certification services;
- continuously observes cloud, identity, HR, source-control, ticketing, or industrial systems;
- owns all operational truth in connected systems;
- replaces Jira or enterprise GRC workflow;
- has a complete generic conformance engine today (only the minimal conformance evaluation/findings capability evaluation/findings slice exists, with no evidence, authorization, deployment, or framework-mapping backing it);
- has production actuation or digital-twin reconciliation today.

Standards/reference alignment and semantic mappings remain distinct from tested conformance claims. Controlled revision identity/lineage and durable history are enabling configuration-management primitives, not evidence that any external standard or control has been satisfied.

## 15. Architectural review checklist

When governance or compliance work is proposed, ask:

1. Is the underlying fact authoritative Arcogine model state, external observed state, or a governance decision?
2. Are framework-specific fields being added to business objects instead of deriving compliance through requirements and controls?
3. Can the result identify the exact semantic fingerprint, controlled revision, the exact requirement/assertion definitions actually used, the evaluation occurrence, and the evidence basis (relied on, excluded, missing) that produced it?
4. If a requirement comes from an external source, can it identify the exact authority, designation, edition/version, locator, and applicable adoption/profile rather than only a standards-family name?
5. Is semantic identity being confused with historical revision identity?
6. Is the change represented semantically enough to perform impact analysis?
7. Does an external workflow system already own the ticket/change-management lifecycle?
8. Are approval and operational deployment modeled as separate records referencing a revision rather than as revision identity itself?
9. If a revision is deployed through a transformation/adapter, can the operational deployment record identify the effective applied artifact/profile rather than only the source revision?
10. Are failures, exceptions, and risk acceptances distinguishable rather than collapsed into one status?
11. Are modeled intent and observed reality explicit and independently attributable?
12. Is an external observation kept revision-independent until an `EvidenceUse`/interpretation binds it when appropriate, and is producer-intrinsic provenance on an Arcogine-derived result kept distinct from the target it is later used against?
13. Are historical results reproducible rather than dependent on today's mutable mappings, definitions, or evidence lookups, and does evidence unusable for a use stay explainable rather than becoming `PASS` or `NOT_APPLICABLE`?
14. Is a workflow/change reference being treated as an association rather than an immutable identity field of the revision?
15. Does a proposed lineage extension preserve the distinction between semantic identity and historical occurrence identity?

## 16. ADR triggers

[ADR-0004](decisions/0004-model-identity-revision-lineage-and-external-change-control.md) fixes the semantic-identity versus controlled-revision distinction and the external change-control boundary. [ADR-0006](decisions/0006-durable-semantic-fingerprint-contract.md) fixes the first durable fingerprint contract. [ADR-0008](decisions/0008-controlled-revision-identity-and-lineage.md) fixes controlled revision identity, current lineage cardinality, rollback semantics, immutable recording provenance, and the persistence boundary. [ADR-0016](decisions/0016-governance-evidence-provenance.md) fixes evidence reference identity, equality and non-rebinding, evidence versus use, producer-intrinsic versus use-target provenance, applicability versus requirement scope versus outcome, evaluation-occurrence identity, exact definition resolution, and the point-identity limit, while deferring representation, storage, and policy mechanisms.

Create or revise ADRs when implementation commits to hard-to-reverse choices about:

- replacement/production controlled-revision persistence, artifact retention/resolution, migration, or integrity semantics beyond the current replaceable Governance identity/history capability adapter;
- extending current `0..1` lineage to multi-parent merge semantics;
- branch/ref/tag semantics over controlled revisions;
- cryptographic revision-record integrity/signature semantics;
- canonical semantic `ChangeSet` representation;
- temporal semantics for modeled facts and observations;
- requirement/assertion evaluation contracts;
- evidence-reference representation, evaluation-occurrence acceptance/persistence, or durable requirement-definition resolution mechanisms beyond the semantic obligations ADR-0016 fixes;
- requirement source-identity and versioning semantics;
- control and framework versioning;
- evidence integrity/retention semantics;
- exception and risk-acceptance lifecycle;
- authority boundaries/protocols with Jira or another external governance system;
- the governance-to-operational deployment record boundary for approved revisions.

Do not create framework-specific ADRs merely to add content mappings when the generic governance architecture is unchanged.
