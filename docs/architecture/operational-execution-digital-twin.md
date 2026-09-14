# Operational Execution and Digital Twin Architecture

> **Status:** Proposed architectural reference  
> **Scope:** Execution/reality relationships, external observations, digital-twin reconciliation, and design-to-reality continuity  
> **Authority:** Proposed architecture; this document does not describe current production capability  
> **Related:** [Product Charter](../product/charter.md), [Architecture Overview](overview.md), [Factory Design Architecture](factory-design.md), [Governance and Conformance Architecture](governance-conformance.md), [Standards Alignment](standards-alignment.md), [ADR-0004](decisions/0004-model-identity-revision-lineage-and-external-change-control.md), [ADR-0011](decisions/0011-runtime-observation-and-event-contract.md), [ADR-0012](decisions/0012-external-interchange-and-serialization-boundaries.md), [ADR-0013](decisions/0013-durable-operational-identity.md), [ADR-0015](decisions/0015-engine-semantics-identity-and-reproducibility.md), [Operational Execution and Digital Twin Readiness](../planning/operational-execution-digital-twin-readiness.md)

## 1. Architectural position

Arcogine's product thesis requires continuity from designed intent through simulation and verification to independently existing operational reality without forking the business semantics into separate simulation and production ontologies.

The architectural rule is:

> **Arcogine reuses the same semantic model across synthetic and externally grounded execution. What differs is expressed through relationships: how state transitions are realized, what provenance and authority they carry, what evidence is available, what trust applies, and what consequence can escape Arcogine. Raw external observations retain independent source and time provenance and must not be forced to invent Arcogine model, revision, or operational-history identity at ingestion.**

This capability is a sibling of Factory Design / Engine Readiness and Governance / Conformance:

- **Factory Design / Engine Readiness** owns canonical production semantics and deterministic simulation runtime truth.
- **Operational Execution / Digital Twin** owns the future semantic integration boundary where independently existing systems, external observations, trusted correspondences, command/result facts, deployment application, and modeled-versus-observed reconciliation enter the picture.
- **Governance / Conformance** owns durable semantic identity, controlled revision history, requirements/assertions, evidence use, findings, exceptions, and governed-change interpretation.

The current implementation is simulation-first and does not execute real-world commands. Nothing in this document changes that current-state claim.

Governance durable fingerprint/revision history, semantic change/impact, requirement/assertion, and initial conformance/finding contracts are implemented and authoritative. Operational work that needs those responsibilities must consume them rather than inventing substitutes. Governance evidence-use/authorization capabilities remain future dependencies where applicable.

Engine runtime observation/event core/headless closure is also implemented. `RunId`, supported runtime observations/events, and `EngineSemanticsVersion` remain Engine-owned concepts. Distribution hardening and durable replay/reconnect remain separate future Engine work; Operational Execution must not assume they already exist.

## 2. Synthetic versus operational is not a global execution kind

A single Arcogine execution may legitimately combine, for example:

```text
Machine A        externally observed
Machine B        externally observed and physically controllable
Machine C        synthetic
Demand           historical trace
Operator         verified human
Supplier         synthetic autonomous agent
Controller       physical hardware
Plant dynamics   simulated
```

No single `SIMULATION`, `STAGING`, `PRODUCTION`, or `OPERATIONAL` label adequately describes that whole graph.

The architecture therefore does **not** treat `PRODUCTION / STAGING / SIMULATION` as a durable global execution taxonomy. ADR-0013 withdrew the earlier consequence-oriented `ExecutionContextKind` proposal, and that withdrawal stands in the Accepted decision: no replacement enum, simulation/operational binary, or equivalent whole-execution consequence classification may be reintroduced under another name.

Several concerns that were previously candidates for one context kind are orthogonal:

- whether a subject's authoritative state is synthetic or independently external;
- how an operation is realized;
- whether an external target can suffer consequence;
- what actor/capability policy applies;
- what trust and authentication basis applies;
- installation lifecycle such as commissioning, qualification, production, maintenance, or decommissioning;
- historical processing such as seek, replay, or fork.

Hybrid configurations are expected rather than exceptional. These concerns must remain independently representable instead of being compressed into one permanent enum value.

### 2.1 Digital twin is relational

A digital twin is not a global execution kind and no top-level `DigitalTwinId` is justified merely by the concept.

A twin exists where Arcogine-modeled subjects are related to independently existing external subjects through sufficiently authoritative correspondence, observation, provenance, and reconciliation relationships. Some subjects in one model may be externally grounded while others remain synthetic.

A twin can also use simulation internally for prediction or hypothetical continuation. A synthetic fork from a reconciled historical state does not make the continuing physical world synthetic; it creates a separate hypothetical continuation from that point.

### 2.2 Durable operational identity is an accountable operational continuation

[ADR-0013](decisions/0013-durable-operational-identity.md) is **Accepted** and is the authority for this boundary. Its decision is summarized here; it is not restated in full, and this document does not extend it.

The durable operational identity identifies **one accountable operational continuation**: one independently continuing body of Arcogine's own operational conduct and conclusions, maintained as one account for which Arcogine is answerable. It is a reference to the record that established that account, not a new category of thing.

It is not:

```text
RunId
ModelFingerprint
ControlledRevisionId
EngineSemanticsVersion
target identity
actor identity
deployment identity
external subject identity
a physical installation
a digital-twin object
```

The accepted rules this document depends on are:

- **Continuity is representation-independent.** Identity is preserved for as long as the continuation remains able to answer for accountable facts it has already accepted. Storage migration, encoding changes, declared and bounded compaction, runtime replacement, deployment changes, actor changes, target changes, and lifecycle changes do not themselves create a new identity. Silent loss is a fork; declared loss is a fork with a recorded gap.
- **Divergence establishes a distinct identity** under one rule for deliberate fork, stale restore, and accidental divergence, with mandatory immutable lineage naming the parent and a determinate, referenceable divergence boundary. A declared fork is additionally permitted at any time. Later convergence of state is never identity equality.
- **Exclusive extension is an obligation, not a truth-condition.** Divergence is guaranteed representable and detectable, not prevented; a violation is recorded and remediated forward, and already-written identity bindings are never relabelled.
- **The identity attaches to Arcogine's conduct and conclusions**, never to what Arcogine was told. Raw external observations do not carry it at ingestion.
- **Possession of the identifier confers no authority.** Continuity evidence is not authorization.

The final type name, identifier representation, persistence and acceptance mechanics, divergence-evidence representation, registry question, coordination mechanism, closure/retirement semantics, and module ownership remain deliberately deferred by ADR-0013 until a concrete consumer proves them. `ExecutionContextId` is not the name of this identity; the withdrawn `ExecutionContext` concept is not revived.

Other boundaries in this document — subject correspondence, operation realization, actor/trust/authority, reconciliation, and temporal semantics — remain **Proposed** and unresolved. Accepting the identity decision resolves one contained question, not this document's status.

### 2.3 Identity must not be inferred from deployment location

The accepted durable operational identity must not be inferred from:

- Spring profile;
- process hostname;
- environment variable naming convention;
- API URL;
- Kubernetes/deployment namespace;
- target identity;
- actor identity;
- `RunId`;
- `ControlledRevisionId`;
- `ModelFingerprint`;
- build, test, replay, or process mode.

Configuration may carry an already-established identity. Configuration syntax/location is not semantic identity or authority.

## 3. Modeled, external, observed, reconciled, and predicted state are distinct

Arcogine must preserve at least these concepts:

```text
Modeled intent/state
    what the selected semantic model and, when applicable,
    controlled revision say should exist

External reality
    independently existing state that Arcogine does not own as a database value

Observation
    immutable/provenanced evidence reported about external reality

Reconciled twin interpretation
    Arcogine's current evidence-based interpretation after considering
    model, observations, authority, freshness, confidence, and discrepancy

Simulated/predicted state
    hypothetical or forecast continuation generated from selected inputs
```

An observation does not automatically overwrite modeled state. A model does not override an independently authoritative external fact. A prediction does not silently become reconciled reality. Reconciliation is an explicit domain responsibility.

A reconciled twin may represent agreement, stale or missing evidence, conflicting authorities, known divergence, inferred state with confidence, or a pending requested transition that has not yet been observed as complete.

## 4. Subjects, operations, transitions, and realization

The same semantic operation should remain meaningful whether its realization is synthetic, physical, hardware-in-the-loop, software-in-the-loop, or another hybrid.

Conceptually:

```text
Operation
    semantic request/decision intended to affect state

Transition
    state change that actually occurs

Realization
    mechanism through which an operation can produce or request a transition

Observation
    evidence about state or a transition
```

These names are conceptual; this document does not require corresponding Java types yet.

For an Arcogine-owned synthetic state machine, an authorized operation may directly and deterministically produce the authoritative next state.

For an independently existing system, Arcogine can request an operation through an external realization mechanism, but request, acceptance, adapter success, physical transition, observation, and reconciled interpretation remain separate facts.

Therefore:

```text
requested operation
!= accepted command
!= actual transition
!= observation of transition
!= reconciled interpretation
```

This distinction is substrate-independent and is more fundamental than an `observe / actuate` direction flag on an identity correspondence.

## 5. Actor, authority, trust, and capability

Actor/action/capability semantics are not intrinsically operational. Synthetic execution may need humans, agents, NPCs, adversaries, delegated authority, protected resources, approvals, or forbidden actions.

The cross-cutting role, attribution, and provenance rules this section depends on are recorded in [Architecture Overview — Attribution and decision boundaries](overview.md#attribution-and-decision-boundaries), which is their durable authority. In particular: actor, decision source, and subject are distinct roles whose identities need not differ; no shared actor type, actor kind, or actor equality/lifecycle contract exists; and **actor identity is a different question from the accountable operational continuation identity** [ADR-0013](decisions/0013-durable-operational-identity.md) accepts. Actor identity answers *who is the attributable party*; ADR-0013 answers *which independently continuing body of accountable conduct a fact belongs to*. They may correlate, and neither inherits the other's equality or lifecycle rules.

The reusable semantic question is approximately:

```text
May actor A perform operation X on subject S under the applicable policy?
```

That is a **question shape** for locating where policy belongs, not a persisted or public input schema, and Arcogine asserts no rule that authorization consults only the current actor.

Real external consequence adds further requirements rather than a second authorization ontology:

- claimed identity versus verified identity;
- trust roots and peer/source/target authenticity;
- credential/secret lifecycle;
- least privilege;
- revocation/expiry or equivalent loss of trust;
- physical safety enforcement;
- fail-safe behavior when identity, integrity, authority, or target state is uncertain.

Authentication mechanism, identity provider, certificate scheme, protocol security profile, and policy engine remain implementation choices.

The exact module ownership of reusable actor/capability semantics is still open and must not be forced into Operational merely because the first real-world consumer needs it. Consequential external execution adds verification, trust, safety, and fail-safe requirements to that ownership question; it does not make Operational the owner of generic actor semantics.

## 6. External commands are not facts about reality

A conceptual external command lifecycle is:

```text
Operation requested
      ↓
Validated
      ↓
Authorized
      ↓
Submitted
      ↓
Accepted / Rejected
      ↓
Executing
      ↓
Succeeded / Failed / Unknown
      ↓
Observed and reconciled
```

The exact state machine may vary by adapter. Consequential external realization requires stable correlation, target identity and trust, actor/authority provenance, requested/effective values, semantic model/revision provenance when derived from one, timeout/retry rules, partial outcome handling, and cancellation/compensation where meaningful.

An accepted command is not proof that reality changed. A successful adapter call is not proof that the physical system reached the requested state.

## 7. Deployment is distinct from publication and approval

Factory Design publishes semantic model versions. Governance owns controlled revision identity/history. Operational Execution applies an appropriately authorized semantic state to external targets.

```text
Factory model version
        ↓
Controlled revision when applicable
        ↓
Technical assessment / conformance
        ↓
Authorization
        ↓
Deployment plan
        ↓
Render / map / transform
        ↓
Target application
        ↓
Verification
        ↓
Promote or rollback
```

Deployment records remain separate from models, revisions, authorization decisions, and target identities. A deployment record carries the accepted durable operational identity as provenance (it is Arcogine's own act) but the identity is never inferred from the deployment: many deployments may extend one continuation, and one deployment may host many.

A deployment record must be able to answer not only which source revision was intended, but what effective representation was applied. Provenance should bind source semantic fingerprint and authoritative controlled revision when applicable, target identity, mapping/profile/transformation identity/version, material tool version, rendered/applied artifact fingerprint or authoritative external applied-version/reference, authorization, application acknowledgement, verification result, and rollback reference.

Governance durable revision/history capability supplies the identity/history prerequisite; it does not implement deployment semantics.

## 8. External observations and subject correspondence

An external observation is an operational fact, not merely a Governance evidence attachment and not intrinsically a fact about one Arcogine model/revision or operational history.

A durable observation contract should be able to identify observation identity, source system/identity, source trust/authenticity provenance where required, observed external subject, observed value/fact, unit/dimension where applicable, source event/measurement time, ingestion/receipt time, quality/confidence metadata, genuine correlations, and raw-source reference when retention policy permits.

A raw observation does **not** require:

- an Arcogine operational-history identity;
- a `ModelFingerprint`;
- a `ControlledRevisionId`.

If an external source supplies its own environment/context, that fact may be retained as source provenance. Arcogine-owned interpretation establishes later relationships only when it has authority to do so.

### 8.1 External-subject correspondence is first-class future work

Before reconciliation can claim that an external subject is evidence about an Arcogine semantic subject, Arcogine needs an explicit authoritative correspondence assertion conceptually equivalent to:

```text
external identity namespace + external subject
                    ↕
        authoritative correspondence
                    ↕
          Arcogine semantic subject
```

Names, endpoints, connector configuration, namespace placement, or coincident identifiers must never establish this correspondence implicitly.

A mature correspondence contract must eventually address at least:

- namespace-qualified external subject identity;
- Arcogine semantic subject reference;
- asserting/mapping authority;
- mapping/profile version;
- effective interval;
- historical preservation;
- explicit unknown/unmapped state;
- conflict/replacement/alias semantics where required.

Subject correspondence answers **which subjects correspond**. It does not itself say how evidence arrives or how an operation is realized.

Observation-side mapping and operation-realization mapping are therefore separate semantic responsibilities even when one adapter/profile supports both.

Transport protocols such as OPC UA or MQTT remain adapters over this boundary; they do not define Arcogine identity.

## 9. Reconciliation owns modeled-versus-observed divergence

Reconciliation compares modeled/reconciled expectations with authoritative external evidence.

It should distinguish states equivalent to match, pending, stale, missing, conflict, diverged, and unknown without freezing those illustrative names prematurely.

A reconciliation result should be historically attributable to the exact semantic fingerprint / controlled revision interpreted when applicable, the observations considered, the correspondence assertions used, source authority/trust decisions, reconciliation policy/version, relevant pending commands/deployments, and temporal frame.

Governance controlled-revision history allows revision-bound reconciliation to resolve the exact authoritative historical semantic state; mutable current model state must not substitute when historical attribution matters.

Reconciliation must eventually account for temporal semantics including source/event time, observed-at time, received/ingested-at time, effective intervals, recorded-at time, deployment time, simulation time where relevant, and processing wall-clock time.

## 10. History, seek, replay, checkpoint, and fork are distinct capabilities

`REPLAY` is not an execution/world/context kind.

The architecture distinguishes at least:

```text
seek / reconstitution
    recover or view state at a historical point

replay
    derive/reconstruct state or results by consuming retained historical inputs/trace

checkpoint / restore
    resume runtime state from a retained checkpoint

fork
    create an independently evolving continuation from a selected historical state
```

These capabilities have different retention and reproducibility requirements. Forking from a snapshot does not require replaying every event that produced the snapshot.

The physical world does not replay. Arcogine may replay or reinterpret records about it, and may fork synthetic futures from historical reconciled state.

ADR-0011 does not currently promise an unbounded cursor-addressable durable event history, and ADR-0015 does not promise permanent exact executability of every historical Engine version. Those remain separate design concerns.

None of these capabilities is an execution kind, and none of them is the durable operational identity. ADR-0013 settles how they interact with it: historical inspection and seek/reconstitution accept no record and establish nothing; replay by itself establishes nothing, and derived results belong to whatever account retains them; a checkpoint restore that loses no accepted material preserves the identity, while a stale restore establishes a new identity with lineage and an abandoned-tail annotation; and a fork from a selected historical state always establishes a new identity with mandatory lineage, at the child's first accepted record.

No *additional* durable identity above `RunId` should be introduced beyond the accepted accountable-continuation identity until a concrete capability proves a distinct lifecycle/equality contract.

## 11. Calibration and drift close the improvement loop

The Product Charter's "reality improves the model" principle requires a governed feedback path rather than direct mutation of published semantics.

```text
Expected behavior from model
        +
Observed / reconciled operational behavior
        ↓
Drift / discrepancy analysis
        ↓
Candidate calibration or semantic change
        ↓
Validation / simulation / conformance
        ↓
Controlled revision
        ↓
Optional deployment
```

Calibration proposals must not mutate a published model or external target outside normal publication, governance, and deployment boundaries.

Operational drift analysis may propose a semantic change, but Governance owns durable `ChangeSet`, conformance/finding, evidence-use, and controlled-revision semantics.

## 12. Boundary with Governance and Conformance

Operational Execution owns acquisition and provenance of operational facts and the interpretation/reconciliation relationships that connect them to modeled semantics. Governance consumes those facts through explicit evidence-use relationships when evaluating requirements or governed change.

The invariant is:

> **An external observation is not created as evidence for one Arcogine model, revision, or operational history. It is an operational fact with independent provenance; later authoritative relationships may interpret it, and Governance may reference it through evidence use.**

Operational Execution must consume Governance-owned fingerprint, revision, semantic change, requirement/assertion, conformance/finding, and evidence-use contracts when they exist rather than introducing duplicates.

## 13. Boundary with Factory Design and Engine Readiness

Factory Design / Engine Readiness remains authoritative for executable production semantics and deterministic simulated execution.

Operational Execution does not turn `FactoryRuntime` into a production-control runtime by default.

The architectural pressure from Operational work is broader than simply adding a second command vocabulary. Domain-level operations and state-transition meaning should remain reusable across synthetic and externally grounded realization where the semantics genuinely match. Exactly where that reusable contract belongs — Factory, Engine, a lower shared domain, or only structural analogy — remains an open design question and must not be generalized prematurely.

`EngineSemanticsVersion` remains Engine-owned. It identifies result-affecting Engine interpretation for simulation, including future Engine-driven prediction or virtual-commissioning use where applicable. It is not a generic Arcogine execution-semantics version.

Current Engine runtime observation/event core/headless closure is complete. Operational Execution must not assume outstanding transport, retained-history, replay, reconnect, or distribution hardening merely because the headless contract exists.

## 14. Integration adapter boundary

Industrial adapters sit behind Arcogine semantic contracts rather than defining them.

An adapter/profile may eventually carry several independent relationships, including:

```text
subject correspondence
    external subject ↔ Arcogine semantic subject

observation mapping
    external evidence → Arcogine observation facts

operation realization mapping
    Arcogine semantic operation → protocol/target-specific request
```

Do not collapse these responsibilities into a generic `direction = observe | actuate` property.

Adapter/profile contracts also need attributable transformation/mapping version, authority/trust expectations, transport/security profile where relevant, lossiness rules, retry/idempotency behavior, provenance, and compatibility expectations.

Candidate protocols and standards include OPC UA, MQTT, Asset Administration Shell profiles, ERP/MES interfaces, FMI/co-simulation boundaries, and ISA-95/B2MML-style interchange where justified. No protocol becomes the canonical Arcogine domain model.

## 15. Persistence, history, and time

Operational execution will eventually create durable artifacts whose historical identity matters: commands/results, observations, subject correspondences, deployment records and effective applied-artifact provenance, reconciliation records, and drift/calibration proposals.

The durable operational identity these artifacts carry is settled by ADR-0013 and summarized in §2.2. What remains unspecified here is representation, not meaning:

- Arcogine's own acts and conclusions carry the accepted accountable-continuation identity; raw observations do not (§2.2, ADR-0013 §11);
- the acceptance semantics and the divergence-evidence obligation are design inputs any durable operational record capability must satisfy, not later additions;
- the architecture does **not** define a registry, alias service, lifecycle administration model, identifier representation, or storage layout, and ADR-0013 deliberately defers all of them.

Storage technology remains unspecified. Future durable operational records require explicit retention, compatibility, migration, and temporal semantics appropriate to their own responsibilities.

Late/corrected evidence also pressures the future twin state model toward explicit valid/effective time versus knowledge/recorded time. This document does not select a bitemporal storage representation or query API yet.

## 16. Safety and failure principles

Where an operation can have external consequence:

1. Absence of authority is denial, not implicit permission.
2. An unverifiable actor/source/target is not silently treated as trusted.
3. Integrity/authenticity failure on a consequential path fails safe according to documented policy.
4. Ambiguous command outcome remains representable as ambiguous.
5. Retry is governed by explicit idempotency semantics.
6. Loss of observation is not interpreted as successful convergence.
7. External rejection or partial failure remains visible.
8. Rollback/compensation distinguishes logical model rollback from physical-world reversibility.
9. Credential or trust loss has explicit operational consequences.
10. Consequence must not be inferred or downgraded from a caller-supplied global environment label.

These are architecture requirements, not claims that current Arcogine implements production-grade safety controls.

## 17. Current open questions, non-goals, and ADR triggers

The next architecture work is deliberately bounded to these unresolved questions:

1. **Universal execution primitives:** what minimum semantics are shared among subject, state, operation/action/actuation, transition, realization, observation/evidence, reconciliation, provenance, and authority without creating unnecessary generic types?
2. **Factory/Engine/Operational ownership:** where does reusable operation/transition meaning belong when the same semantic operation can have synthetic and external realizations?
3. **Correspondence/realization contracts:** what cardinality, effective-time, authority, conflict, and mapping rules are actually required?
4. **Temporal twin history:** what temporal/history semantics are required before durable reconciliation and historical reinterpretation become public contracts?

Do not spend this architecture round selecting OPC UA/MQTT schemas, authentication providers, database technology, a central context registry, exact checkpoint/replay storage, distributed execution, autonomous physical control, or a generic cross-domain policy framework. Those are proving cases or future implementation choices, not current prerequisites.

[ADR-0013](decisions/0013-durable-operational-identity.md) is **Accepted** and resolves the durable operational identity referent, continuity, divergence, and record-attachment rules summarized in §2.2. Its identity-separation, non-inference, and raw-observation constraints are now accepted architecture. Its withdrawal of the kind-bound `ExecutionContextKind` model stands, and the representation, persistence, coordination, registry, closure, and ownership questions it defers must not be settled by this document.

Later ADRs are appropriate when implementation commits Arcogine to hard-to-reverse choices such as shared operation/transition ownership, actor/capability/trust semantics, command correlation/idempotency, external observation/correspondence contracts, reconciliation authority/temporal semantics, or production persistence/retention.
