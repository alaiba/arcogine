# ADR-0013: Durable operational identity

Status: Accepted
Date: 2026-09-14

## Context

Operational Execution needs durable attribution that is not reducible to an Engine runtime epoch, semantic model fingerprint, governed revision, actor identity, or external target identity.

The original proposal attempted to solve that problem with three concepts:

```text
ExecutionContextKind = PRODUCTION | STAGING | SIMULATION
ExecutionContextId
ExecutionContext = { id, kind }
```

and permanently bound one ID to one consequence/environment kind.

Architecture review tested that model against mixed synthetic/physical execution, digital twins, virtual commissioning, software-in-the-loop, hardware-in-the-loop, predictive forks, agentic/multi-user simulations, commissioning, and externally observed or controlled subjects.

Those scenarios invalidate the premise that one global consequence/environment kind is a durable property of an execution. A single Arcogine execution may contain synthetic subjects, externally observed subjects, physically controllable subjects, historical inputs, real actors, and synthetic agents at the same time. Lifecycle state, trust, authority, external consequence, historical processing mode, and relationship to reality vary independently.

That review withdrew the kind-bound model but could not yet say what the surviving identity actually refers to. It left one blocking question:

> **What exactly is the independently continuing operational history/partition that needs durable identity, and what makes two records belong to the same one versus different ones?**

Bounded investigation and an independent adversarial review have since answered that question. This ADR records the answer and lifts the hold.

The broader operational reasoning is recorded in [Operational Execution and Digital Twin Architecture](../operational-execution-digital-twin.md), which remains **Proposed** as a whole; this ADR is the Accepted authority for the identity decision it contains.

## Decision

### 1. The referent is an accountable operational continuation

The durable operational identity identifies **one accountable operational continuation**: one independently continuing body of Arcogine's own operational conduct and conclusions, maintained as one account for which Arcogine is answerable and which may be extended over time.

It is a reference to the durable record that established the account. It is not a new category of thing, and it introduces no classification of how real anything is.

It is **not**:

- the physical installation;
- the modelled production system;
- the external subject;
- the runtime or process;
- the deployment;
- a digital-twin object or category;
- a global simulation/staging/production classification.

The withdrawn `ExecutionContextKind` and the `PRODUCTION / STAGING / SIMULATION` taxonomy stay withdrawn. No replacement enum, simulation/operational binary, or equivalent whole-execution consequence classification is introduced by this decision, under this or any other name.

### 2. Neighbouring identities remain distinct

The durable operational identity must not be derived from, inferred from, or collapsed into:

```text
RunId
ModelFingerprint
ControlledRevisionId
EngineSemanticsVersion
actor identity
deployment identity or location
external target identity
external subject identity
a physical installation
```

Those values may be correlated by operational records without becoming substitutes for one another. In particular, a run is a runtime incarnation: one continuation may span many runs, and many runs belong to no continuation at all.

### 3. Identity is established explicitly, never inferred from infrastructure

Identity is established by one explicit authoritative act, of exactly one of two forms:

- **genesis** — an account begins with no predecessor;
- **fork** — an account begins by naming a parent identity and a divergence boundary in it, adopting the parent's state as of that boundary and becoming separately answerable thereafter.

The identity becomes authoritative when the establishing record is accepted into durable operational history, mirroring [ADR-0008](0008-controlled-revision-identity-and-lineage.md)'s rule that authoritative historical identity begins at persistence acceptance.

It must never be inferred from:

- Spring profile;
- hostname;
- environment-variable naming convention;
- deployment/Kubernetes namespace;
- URL or endpoint;
- target identity;
- actor identity;
- presence or absence of `RunId`;
- presence or absence of `ControlledRevisionId`;
- `ModelFingerprint`;
- build, test, replay, or process mode.

Configuration may carry an already-established identity. Configuration location or syntax is not semantic identity or authority.

### 4. Continuity is stated representation-independently

> **Identity is preserved for as long as the continuation remains able to answer for accountable facts it has already accepted.**

Continuity is broken when the account can no longer answer for an already-accepted accountable fact — because that fact was semantically disowned, or because it was lost in a way the account cannot declare and bound. Continuity is **not** broken by a change of representation, storage, encoding, or physical location that preserves attributability, nor by a declared, recorded, bounded retention or compaction under which what is no longer retrievable remains explicitly known to have existed.

Three consequences follow:

- **silent loss is a fork**;
- **declared loss is a fork carrying a recorded gap**, consistent with [ADR-0011](0011-runtime-observation-and-event-contract.md)'s rule that recovery must detect a gap rather than pretend completeness;
- **a lossy summarisation is not a fork only when its lossiness is itself an accepted record.**

This grounds continuity in attributability rather than in permanent byte retention, consistent with [ADR-0015](0015-engine-semantics-identity-and-reproducibility.md)'s guarantee of attribution plus a verifiable definition rather than permanent exact re-execution.

The condition is **set-based, not sequence-based**: it asks whether anything accepted became unanswerable, not whether a log prefix was preserved. Operational records are heterogeneous and concurrently produced, so the rule must not presuppose a total order.

Nothing else bears on continuity. Storage, encoding, location, deployment, runtime, process, actor, target, subject, model, revision, engine semantics, telemetry, control capability, consequence, authority, organisation, and installation lifecycle stage may all change — repeatedly — while the identity holds. That list is what makes the rule usable.

Where continuity cannot be established, it is **not proven**, and the safe resolution is a new identity with lineage and a recorded discontinuity. Absence of demonstrated continuity is not evidence of continuity.

### 5. Minimum semantics of acceptance

The continuity rule turns on what has been *accepted*, so the smallest semantics of acceptance are decided here even though the concrete acceptance mechanics are not:

1. Acceptance is an act of **an authority for the account, at that authority's commit boundary**. A record-shaped value that has not been accepted is not part of the accepted set.
2. Acceptance is **monotonic**. An accepted record is never un-accepted. Correction, retraction, supersession, and reinterpretation are **new accepted records about** an earlier one, never removals of it.
3. Received-but-not-accepted, provisional, and rejected material is outside the accepted set, so losing it never breaks continuity.
4. A derived projection is not independently accepted material; it inherits the status of what it derives from.
5. The accepted set, state, or frontier must remain **determinable by the account**, because the continuity and divergence rules are otherwise unevaluable.

This is a semantic contract on any future durable operational record capability. It selects no storage model, arrival model, or transaction semantics.

### 6. Divergence, fork, and lineage

One rule covers deliberate fork, stale restore, and accidental divergence:

- A continuation that begins from a **selected earlier state**, or from a state that cannot establish the required accepted continuity, **establishes a distinct continuation identity**.
- An **explicit declared fork is additionally permitted at any time**, including at the head, even where continuity could otherwise have been maintained.
- The child identity is established at the child's **first accepted record**, not when a historical state is selected, loaded, or inspected. Historical inspection therefore creates nothing.

Deliberate fork and stale restore share one identity rule; only the annotation differs — typically whether the parent continues, and whether an authoritative tail was abandoned. Keeping one rule is protective: it removes any incentive to reclassify an inconvenient stale restore as "really a continuation."

**Lineage is mandatory and immutable on every fork.** It records:

- the parent continuation identity;
- a **determinate, referenceable divergence boundary** — precise enough that "what the child adopted" is answerable later;
- whether the parent continues, where that distinction is meaningful.

Ancestry is carried by these links alone: no branch objects and no second super-identity, following ADR-0008's choice for revisions. Parent cardinality is `0..1` in this contract, structurally extensible; multi-parent lineage is reserved as a later extension rather than importing merge semantics now.

A fork whose parent was never recorded is indistinguishable from a genesis, which is exactly the distinction this rule exists to preserve.

### 7. Divergence must remain provable, in whatever representation

> **Every accepted fact must remain durably relatable to the accepted continuation state or frontier it extended, with sufficient surviving evidence to establish common ancestry and incompatible extension later, including the divergence boundary or material the chosen semantic model requires.**

This obligation is semantic, not structural. The evidence may be carried per record, per batch, per segment, per checkpoint, or by an authoritative correlated lineage/frontier structure. This decision constrains **what must remain provable, not where a field is physically stored**, and it deliberately does not copy any external system's storage layout into Arcogine's semantic contract.

The obligation is load-bearing and cannot be deferred merely because its representation can. Ancestry links written only when a fork is *declared* cannot detect an **undeclared** divergence; a record capability that discards every durable relation between accepted facts and the continuation state they extended makes later divergence permanently unprovable, and no later decision can repair records already written without it.

### 8. Exclusive extension is an obligation, not a truth-condition

Sole legitimate extension of a continuation is a **normative obligation**, not a metaphysical condition that would retroactively invalidate records written before an accidental split was discovered.

The obligation is claimable and detectable, never provable at write time, and stating it requires no coordination mechanism. Accordingly:

- a set of records bearing one identity is **not guaranteed** free of divergence;
- what is guaranteed is that divergence is **representable and detectable**, and that anyone reading a continuation can learn that a divergence finding exists against it;
- a violation is recorded as a divergence finding naming both continuations and the divergence boundary, and remediated **forward**: entitlement to extend the identity from that decision onward is assigned to at most one continuation, and the other establishes a new identity going forward.

### 9. Convergence is never identity equality

Two continuations that separately recorded conduct never become one account. Independently arrived-at identical state, content equivalence, a shared deployment, selection of one branch as authoritative, and a shared external subject are all insufficient.

What is available instead is a correlation between them, or a new account whose lineage names its parent.

### 10. Historical bindings are immutable and identities are not reused

- Already-written records keep the identity they were written under. Late-discovered divergence is represented by additional findings and lineage, **never** by retroactively relabelling recorded identity.
- The record-to-identity binding never changes.
- A retired or superseded identity is never reissued.

Records may already have been exported as external projections under [ADR-0012](0012-external-interchange-and-serialization-boundaries.md); relabelling them would be identity mutation, which every other Arcogine identity forbids. History is added to, never rewritten.

Non-reuse does **not** imply an endless lifecycle. Whether an accountable operational continuation may close, retire, or become non-extendable is deliberately **left open** — see §14.

### 11. The identity attaches to conduct and conclusions, not to what Arcogine was told

> The identity attaches to what Arcogine did or concluded — never to what Arcogine was told.

Walking an external operation end to end, the boundary falls at this granularity:

| Fact | Owner | Carries the identity |
|---|---|---|
| Arcogine's decision/request to command | Arcogine conduct | **Yes** |
| Arcogine's submission act — what it sent, when, under what authority | Arcogine conduct | **Yes** |
| Target-produced acknowledgement of receipt | Externally sourced fact | No |
| External controller's acceptance or rejection | Externally sourced fact | No |
| Actual physical transition | Reality; not an Arcogine record at all | No |
| Telemetry reporting the outcome | Raw observation | No |
| Arcogine's interpretation or reconciliation of the above | Arcogine conclusion | **Yes** |

Deployment records, subject-correspondence assertions, reconciliation results, and drift/calibration proposals are Arcogine's own acts or conclusions and carry the identity. Target acknowledgements, external accept/reject facts, physical transitions, and telemetry do not — they are independently provenanced facts *linked to* the identity-bearing records.

This preserves the distinction the architecture already requires between a requested semantic operation, an external command or result, an actual transition, an observation, and a reconciled interpretation. Collapsing "a command and its lifecycle" into one identity-bearing unit would smuggle externally sourced facts onto the identity-bearing side.

**Raw external observations are never made to carry the identity at ingestion.** They retain their own source identity, external subject identity, source/event time, receipt time, quality, trust and authenticity provenance, and raw-source reference. Association with a continuation is made later, at interpretation time, by a separate identity-bearing relating record; the observation's original provenance is never rewritten, annotated, or duplicated per continuation.

Two proving cases motivate this and should survive with the decision:

- Two independent interpretations of one physical installation consume the same observation feed and reach different conclusions without conflict, because each records its own interpretation in its own account.
- An observation arriving *after* a fork but describing a period *before* it belongs to no continuation, so parent and child may each legitimately interpret it. Any model that bound identity at ingestion would have had to choose, and would have been wrong either way.

The Governance `Evidence`/`EvidenceUse` separation is the recommended shape for that later binding. Its known limit is recorded here rather than glossed: `EvidenceUse` binds evidence to a **point** identity, whereas an accountable operational continuation is an **accumulating** identity, so the precedent transfers for *where the binding lives* but not for *what a binding to an accumulating identity means over time*.

### 12. Possession of the identifier confers no authority

Holding the identifier is not entitlement to extend the continuation, and is not authorization to act.

Evidence that a continuation retains the required accepted history is **continuity evidence**, and evidence for a later decision. It is not by itself authority. Actor identity, trust, capability, and authorization remain a separate semantic boundary that this decision does not settle and must not be used to settle.

### 13. One identity level now; a second is not foreclosed

This decision defines only the **accountable-continuation identity**, because no current proving case or concrete consumer requires a second grouping or account identity above it.

That is a scope-minimisation decision for this ADR, not a durable claim that operational identity is permanently one-level. ADR-0008 is cited here as a **useful lineage precedent** — it shows that divergent historical lineage can be represented without a separate branch object — and not as authority over operational identity cardinality, which it does not decide.

A future stable higher-level grouping or account identity remains permissible if a concrete consumer later proves a distinct equality, lifecycle, lookup, policy, or aggregation contract that accountable-continuation identity cannot satisfy. Two-level identity was **not falsified**; mature external systems commonly use it. It is simply not justified by current evidence.

### 14. What this decision deliberately defers

The following remain open on the record, and this ADR must not be read as having settled them:

- the final production type name, and any identifier representation, encoding, textual canonicalization, or parsing rule — external representations remain projections under ADR-0012;
- storage schema, persistence mechanics, and the concrete acceptance boundary: arrival ordering, late arrival, and multi-writer commit semantics;
- whether divergence evidence is carried per record, per batch, per segment, per checkpoint, or by a correlated lineage/frontier structure;
- the representation of the divergence boundary;
- any registry, alias service, discovery service, lifecycle-administration database, or centralized uniqueness authority. The semantics require none; whether a later capability ever needs one is a separate decision;
- any distributed coordination, quorum, fencing, lease, or consensus mechanism. Choosing one would violate the infrastructure-independence constraint in §3;
- the concrete multi-writer acceptance protocol, and whether federated extension of one continuation needs more than a mutual non-loss obligation;
- **closure and retirement semantics**, left explicitly open in both directions pending a concrete consumer;
- module ownership of an implementation type;
- whether multi-parent lineage is ever required;
- whether an inspection or analysis session needs its own durable identity;
- a second grouping/account identity (§13);
- Operational implementation sequencing.

The semantic core above is settled now because a future durable operational record capability needs the acceptance and divergence-evidence obligations as design inputs. The value type waits for its first real durable-record consumer: until such a capability exists, an identifier would have nothing to label, no acceptance boundary, and no way to test the continuity rule.

## Alternatives considered

**A global execution kind (`ExecutionContextKind`, the original proposal).** Rejected and withdrawn. Lifecycle state, trust, authority, external consequence, historical processing mode, and relationship to reality vary independently and per subject within one execution, so no single label durably describes the whole graph. Reviving it under another name is explicitly out of bounds.

**No new identity — a per-continuation `continues:` relation only.** The strongest challenger, and semantically equivalent: it declines to name the equivalence class rather than denying it. Rejected on three practical grounds. Entitlement must be decidable at write time, which under a relation-only model requires traversing the graph rather than holding a single reference. Concurrent heterogeneous records have no single predecessor chain to belong to, because succession relates *continuations*, not records. And ADR-0012 makes exported records projections of semantic contracts, so a traversal-only model would force an exported record to carry its whole ancestry graph to state which account it belongs to.

**A two-level model: a stable account identity plus a per-incarnation continuation identity.** Survives the proving cases and is **not falsified**. Set aside because no current consumer proves a distinct equality, lifecycle, lookup, policy, or aggregation contract that the single accountable-continuation identity cannot satisfy. Explicitly preserved as a permissible future extension (§13).

**Deriving identity from a deployment, run, installation, or external subject.** Rejected. Every one of these is many-to-many with a continuation in at least one direction: many deployments may extend one continuation and one deployment may host many; a continuation spans many runs and most runs belong to none; several independent continuations may model one physical installation. Derivation in either direction would be false.

**Defining continuity as preservation of an accepted log prefix.** Rejected. It presupposes a total order that heterogeneous, concurrently produced operational records do not have, and it would classify legitimate compaction, archival, and storage migration as identity forks (§4).

**Requiring every accepted record to physically embed its divergence evidence.** Rejected as a semantic requirement. It is a valid implementation shape, and external recovery systems demonstrate it works, but promoting one storage layout into the semantic contract would over-constrain representation while adding nothing to what must remain provable (§7).

## Consequences

- ADR-0013's architecture-review hold is lifted. The withdrawals it recorded remain in force; the unresolved equality/lifecycle question it named is answered by §4–§10.
- Future durable operational record contracts must satisfy the acceptance semantics (§5) and the divergence-evidence obligation (§7) as design inputs, not as later additions.
- Recovery, checkpoint, and restore capabilities must implement the loss test and the fail-safe-to-fork rule, must not silently truncate, and must be able to record an abandoned branch. Discarding the records of what the system actually did is falsification, not repair.
- External command, deployment, correspondence, and reconciliation contracts must carry the identity on Arcogine's own request, submission, and interpretation records, and must keep target acknowledgements, external accept/reject facts, physical transitions, and telemetry independently provenanced.
- Raw external observation ingestion is unaffected and must stay so.
- Actor, trust, authority, and capability semantics are not settled here and must not be placed in Operational Execution on the strength of this decision.
- No production type, module, persistence format, registry, coordination mechanism, or implementation slice follows from accepting this ADR. In particular, no `:operational` module or public/persisted schema should be introduced merely to materialise an identity type.
- Operational implementation admission remains governed independently by [Operational Execution and Digital-Twin Implementation Admission](../../planning/operational-execution-digital-twin-readiness.md); closing this semantic blocker does not admit a slice.

## Charter alignment

This decision serves the Charter's **Reality is explicit** principle without reviving a global taxonomy. The Charter's architectural-implications parenthetical "(simulation, replay, staging, production)" states a conceptual consequence: that execution contexts must remain **distinguishable**. Distinguishability is satisfied relationally — per subject, per realization, per authority, per correspondence — and is not a mandate for the withdrawn global enum. That parenthetical is the most likely route by which the withdrawn taxonomy would return, so it is read here explicitly and once.

The fork rule actively serves the Charter's requirement that hypothetical branches never be ambiguously presented: a divergent continuation is a distinct identity with mandatory lineage, so a user or agent can always tell which continuation they are looking at.

The decision also serves **Causality and provenance** — what happened, under which model, based on which observations, by whose decision — by making Arcogine answerable for its own conduct as one account while keeping externally sourced facts independently provenanced.

## Related decisions

- [ADR-0004: Model identity, revision lineage, and external change control](0004-model-identity-revision-lineage-and-external-change-control.md)
- [ADR-0008: Controlled revision identity and lineage](0008-controlled-revision-identity-and-lineage.md)
- [ADR-0011: Runtime observation and event contract](0011-runtime-observation-and-event-contract.md)
- [ADR-0012: External interchange and serialization boundaries](0012-external-interchange-and-serialization-boundaries.md)
- [ADR-0015: Engine semantics identity and reproducibility](0015-engine-semantics-identity-and-reproducibility.md)
