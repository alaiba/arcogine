# Operational Execution and Digital Twin Architecture

> **Status:** Proposed architectural reference  
> **Scope:** Operational execution, external observations, digital-twin reconciliation, and design-to-reality continuity  
> **Authority:** Proposed architecture; this document does not describe current production capability  
> **Related:** [Product Charter](../product/charter.md), [Architecture Overview](overview.md), [Factory Design Architecture](factory-design.md), [Governance and Conformance Architecture](governance-conformance.md), [Standards Alignment](standards-alignment.md), [ADR-0004](decisions/0004-model-identity-revision-lineage-and-external-change-control.md), [ADR-0011](decisions/0011-runtime-observation-and-event-contract.md), [ADR-0012](decisions/0012-external-interchange-and-serialization-boundaries.md), [ADR-0013](decisions/0013-execution-context-identity.md), [Operational Execution and Digital Twin Readiness](../planning/operational-execution-digital-twin-readiness.md)

## 1. Architectural position

Arcogine's product thesis requires continuity from designed intent to operational reality without collapsing simulation, production control, external observations, and governance into one mutable runtime.

The proposed architectural rule is:

> **Arcogine reuses production semantics across lifecycle contexts. Consequential Arcogine interpretations, commands, deployments, and reconciliation results must identify their execution context, authority, provenance, and the semantic model/revision relationship that informed them when one exists. Raw external observations retain independent source and time provenance and must not be forced to identify an Arcogine execution context or controlled revision at ingestion.**

This capability is a sibling of Factory Design / Engine Readiness and Governance / Conformance:

- **Factory Design / Engine Readiness** owns production semantics and deterministic simulation runtime truth.
- **Operational Execution / Digital Twin** owns operational execution-context identity, command/result lifecycle, deployment application, external observations, operational trust boundaries, and modeled-versus-observed reconciliation.
- **Governance / Conformance** owns durable semantic identity, controlled revision history, requirements/assertions, evidence use, findings, exceptions, and governed-change interpretation.

The current implementation is simulation-first and does not execute real-world commands. Nothing in this document changes that current-state claim.

Governance G1 is now implemented and authoritative. Operational work that needs durable model/revision identity must consume `ModelFingerprint`, `ControlledRevisionId`, `ControlledRevision`, and `ControlledRevisionAuthority` rather than inventing synthetic production substitutes. Governance G2's initial slice (`ChangeSet`/`ImpactScope`/`SemanticChange` and the factory-domain semantic comparator) is also now implemented; operational work that needs semantic change attribution should consume it rather than inventing a substitute. Governance G3 (`Requirement`/`Assertion`/`RequirementCatalogue`) is also now implemented; operational work that needs a registered, versioned requirement/assertion contract should consume it rather than inventing a substitute. G4+ Governance capabilities (conformance evaluation, evidence use, findings, exceptions, authorization) remain future dependencies where applicable.

Engine Gate 4 core/headless closure is now complete: `RunId` and consumer-neutral `RuntimeObservation` landed through G4-A, the supported `RuntimeEvent` contract landed through G4-B, and G4-C closed the headless acceptance contract (fresh-observation reconstruction without replay, observation/event closure, bottleneck identification from the supported observation, and structural enforcement that API/frontend DTOs never re-enter domain decision paths). ADR-0011 defines the event contract that G4-B/G4-C implement at the `FactoryRuntime` boundary. Gate 4 distribution hardening beyond the headless boundary (G4-D transport/SSE/CLI migration, DH-E retained history/replay/reconnect) remains outstanding and out of scope for Operational Execution to assume.

## 2. Execution context has classification and concrete identity

Operational consequence must be explicit, but classification is not identity.

The architecture distinguishes concepts equivalent to:

```text
ExecutionContextKind
    what consequence/environment semantics apply?

ExecutionContextId
    which concrete Arcogine execution context is this?

ExecutionContext
    immutable binding of kind + concrete identity
```

`PRODUCTION` is therefore a classification, not one global production context. Arcogine must be able to represent several production, staging, or simulation-linked contexts at the same time without conflating them.

The original conceptual list `SIMULATION / REPLAY / TEST / STAGING / PRODUCTION` was exploratory, not a frozen enum. The O1 decision must use consequence-oriented semantics:

- production consequence is a distinct classification;
- staging-like production integration without production consequence is a justified classification;
- simulation may be classified or linked when operational artifacts need that distinction, but simulation runtime identity remains `RunId`;
- replay is primarily a processing/history-interpretation mode and should not become a context kind unless a concrete authority/consequence invariant requires it;
- generic software test execution is not itself an operational environment ontology.

This is a narrower taxonomy than the Product Charter's conceptual use of **execution context**. The Charter requires simulation, replay, staging, and production to remain unambiguously distinguishable; O1 does not relax that invariant. `ExecutionContextKind` is intended only to classify operational consequence/environment semantics, while replay may be represented by a separate explicit processing/execution-mode dimension correlated with the concrete context. A replay must therefore remain visibly distinct from an ordinary simulation, staging, or production interpretation even if `REPLAY` is not an `ExecutionContextKind` member. If later design cannot preserve that distinction cleanly without making replay a context kind, the O1 ADR must revise the taxonomy rather than weaken the Charter requirement.

[ADR-0013](decisions/0013-execution-context-identity.md) now records the concrete O1 proposal: initial kinds `PRODUCTION`, `STAGING`, and `SIMULATION`; opaque UUIDv4 `ExecutionContextId`; immutable ID-plus-kind binding; permanent ID-to-kind association; checked semantic comparison that treats same-ID/different-kind as a binding conflict rather than another context; explicit establishment through an Operational boundary; and no O1 context registry. Because ADR-0013 is still **Proposed**, these are not yet accepted architecture and O1 remains unimplemented.

### 2.1 `RunId` is not execution-context identity

ADR-0011 defines `RunId` as opaque correlation identity for one fresh simulation runtime epoch. A reset creates a new `RunId` even when the same semantic model and deterministic workload are reused.

Operational execution context answers a different question: under what consequence/environment semantics is an Arcogine-owned operational artifact interpreted, and which concrete operational context does it belong to?

Therefore:

```text
RunId != ExecutionContextId
```

A later operational record may correlate both. The existence of a `RunId` must never be used to infer an operational context.

### 2.2 Context identity is not target, actor, revision, or model identity

The architecture preserves these distinct identities:

```text
ModelFingerprint
    which semantic content?

ControlledRevisionId
    which governed historical occurrence?

RunId
    which simulation runtime epoch?

ExecutionContextKind
    what consequence/environment semantics apply?

ExecutionContextId
    which concrete operational context?

Target identity
    which external system/resource receives an action?

Actor identity
    who/what requested or performed the consequential action?
```

Required non-equivalences are:

```text
ControlledRevisionId != ExecutionContextId
RunId               != ExecutionContextId
target identity      != ExecutionContextId
actor identity       != ExecutionContextId
ModelFingerprint     != ExecutionContextId
```

Correlation among these values belongs in the operational artifact that needs it. No convenient neighboring identity becomes a substitute for context identity.

### 2.3 Authoritative context boundary

Execution context becomes trustworthy only when explicitly supplied or resolved through an Operational Execution-owned semantic boundary. Downstream operational artifacts propagate that value as data.

Context must not be inferred from:

- Spring profile;
- process hostname;
- environment variable alone;
- API URL;
- deployment namespace;
- caller convention;
- existence of a `RunId`;
- existence of a `ControlledRevisionId`;
- target identity;
- actor identity.

Configuration mechanisms may later carry an already-defined context identifier, but configuration syntax/location is not the semantic authority.

This boundary intentionally precedes O2 authorization. O2 can later evaluate policy against explicit context kind and concrete identity rather than duplicating environment heuristics or forking factory production semantics.

## 3. Modeled state, observed reality, and reconciled twin state are different things

Arcogine must preserve three distinct concepts:

```text
Modeled intent/state
    what the selected semantic model and, when applicable,
    controlled revision say should exist

Observed external reality
    immutable facts reported by external systems or measurements
    with independent source/time provenance

Reconciled twin interpretation
    Arcogine's current interpretation of reality after considering model,
    observations, authority, freshness, confidence, and discrepancies
```

An observation does not automatically overwrite modeled state. A model does not automatically override an external authoritative fact. Reconciliation is an explicit domain responsibility.

A reconciled twin may represent agreement, stale or missing observations, conflicting external authorities, known divergence, inferred state with confidence, or a pending commanded transition that is not yet observed as complete.

The model/revision relationship belongs naturally to the reconciliation result or another interpretation/evidence-use record because that is where Arcogine decides which model semantics were applied to independent external facts.

## 4. Identity, trust, authority, and capability

Operational execution requires a common actor and peer boundary broader than interactive login.

Conceptually, an actor may be human, agent, service, or external system. Every consequential request must be attributable to an actor identity and evaluated against explicit capability/scope in the target execution context.

For a production-consequential boundary, identity is not sufficient by itself. Arcogine must distinguish claimed identity from verified identity and establish an explicit trust basis for consequential peers, sources, and targets.

Before production actuation or authoritative production observation is considered mature, the boundary must provide semantics for peer/source/target verification, integrity/authenticity, credential/secret lifecycle sufficient for the integration, least privilege, trust assumptions, revocation/expiry or equivalent loss of trust, and fail-safe treatment of unverifiable or integrity-failed inputs.

Authentication mechanism, identity provider, certificate scheme, protocol security profile, and policy engine remain implementation choices.

## 5. Commands are not facts

A request to change reality must remain distinguishable from the resulting operational facts.

A conceptual lifecycle is:

```text
CommandRequested
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
Reconciled with observed reality
```

The exact state machine may vary by adapter, but production actuation requires stable command identity/correlation, target identity and verified trust context, actor and authority provenance, explicit execution context, requested/effective values, source semantic fingerprint / controlled revision when derived from one, submission/acknowledgement facts, timeout/retry rules, partial outcome handling, cancellation/compensation where meaningful, and resulting observation/reconciliation links.

An accepted command is not proof that reality changed. A successful adapter call is not automatically proof that the physical system reached the requested state.

## 6. Deployment is distinct from publication and approval

Factory Design publishes semantic model versions. Governance owns controlled revision identity/history. Operational Execution applies an appropriately authorized semantic state to a target execution context.

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

Deployment records remain separate from models, revision records, and authorization decisions. Different targets or execution contexts may legitimately be at different revisions.

A deployment record must be able to answer not only which source revision was intended, but what effective representation was applied. Provenance should bind source semantic fingerprint and authoritative controlled revision when applicable, target identity and execution context, mapping/profile/transformation identity/version, material tool version, rendered/applied artifact fingerprint or authoritative external applied-version/reference, authorization, application acknowledgement, verification result, and rollback reference.

Governance G1 now provides the durable revision/history prerequisite for this work. That availability does not implement deployment semantics.

## 7. External observations have independent provenance

An external observation is an operational fact, not merely a governance evidence attachment and not intrinsically a fact about one Arcogine context or revision.

A durable observation contract should be able to identify observation identity, source system/identity, source trust/authenticity provenance where required, observed subject, observed value/fact, unit/dimension where applicable, source event/measurement time, ingestion/receipt time, quality/confidence metadata, correlation to command/deployment/run when genuinely known, and raw-source reference when retention policy permits.

A raw observation does **not** require:

- an Arcogine `ExecutionContextId`;
- a `ModelFingerprint`;
- a `ControlledRevisionId`.

If the external source genuinely supplies its own environment/context, that fact may be retained as source provenance. If Arcogine later interprets the observation against an operational context or model, that binding belongs to the interpretation, reconciliation result, deployment correlation, or Governance `EvidenceUse` relationship.

Transport protocols such as OPC UA or MQTT are adapters over this semantic boundary; they are not the domain model themselves.

## 8. Reconciliation owns modeled-versus-observed divergence

Reconciliation compares what Arcogine expects with what authoritative observations indicate.

It should distinguish states equivalent to match, pending, stale, missing, conflict, diverged, and unknown without freezing those illustrative names prematurely.

A reconciliation result should be historically attributable to the exact semantic fingerprint / controlled revision interpreted when applicable, the observations considered, source authority/trust decisions, reconciliation policy/version, relevant pending commands/deployments, execution context, and temporal frame.

Governance G1 now allows revision-bound reconciliation to resolve an exact authoritative historical semantic state through `ControlledRevisionAuthority`; it must not substitute the mutable current model or a synthetic revision fixture when the historical revision matters.

Reconciliation must account for temporal semantics including simulation time, source/event time, observed-at time, received/ingested-at time, effective intervals, recorded-at time, deployment time, and processing wall-clock time.

## 9. Calibration and drift close the improvement loop

The Product Charter's "reality improves the model" principle requires a governed feedback path rather than direct mutation of published semantics.

```text
Expected behavior from model
        +
Observed operational behavior
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

Calibration proposals must not mutate a published model or production target outside normal publication, governance, and deployment boundaries.

Operational drift analysis may produce a candidate semantic change, but Governance owns the durable `ChangeSet`, conformance, finding, and controlled-revision semantics used to govern that candidate. Governance G2's initial `ChangeSet`/`ImpactScope` slice and Governance G4's initial `ConformanceEvaluator`/`Finding` slice are now available for that; Governance G5 evidence-use semantics remain outstanding and Operational Execution must not introduce substitutes.

## 10. Boundary with Governance and Conformance

Operational Execution owns acquisition and provenance of operational facts. Governance consumes those facts as evidence when evaluating requirements or governed change.

The invariant is:

> **An external observation is not created as evidence for one context or revision. It is an operational fact with independent provenance; later Arcogine interpretations may bind it to an execution context/model, and Governance may reference it through an evidence-use relationship.**

Governance G1 is complete and authoritative for durable semantic fingerprint policy, controlled revision identity/lineage, acceptance/persistence, and exact historical resolution. Governance G2's initial slice is complete and authoritative for semantic `ChangeSet`/`ImpactScope`/`SemanticChange` attribution. Governance G3 is complete and authoritative for the generic `Requirement`/`Assertion`/`RequirementCatalogue` contract. Later Governance gates own conformance evaluation, evidence use, findings, exceptions, governed-change interpretation, and audit projections.

Operational Execution references those contracts when they exist rather than introducing duplicate revision, ChangeSet, evidence-use, or finding types.

## 11. Boundary with Factory Design and Engine Readiness

Factory Design / Engine Readiness remains authoritative for executable production semantics and deterministic simulated execution.

Operational Execution does not turn `FactoryRuntime` into a production-control runtime by default.

Engine concepts such as workload, dispatch, queues, operations, observations, and spatial consequences may inform shared semantic contracts. Production actuation additionally requires execution context, verified identity/trust, authorization, command acknowledgement, external-system failures, deployment targeting, operational observation provenance, and reconciliation.

Current Engine Gate 4 status matters at this boundary:

- `RunId` and consumer-neutral `RuntimeObservation` are implemented through G4-A;
- the supported `RuntimeEvent` contract is implemented through G4-B;
- Gate 4 core/headless closure is complete through G4-C: fresh observation reconstruction without replay, observation/event closure, and consumer-neutral bottleneck identification are all proven at the `FactoryRuntime` boundary;
- Operational Execution must not claim or depend on Gate 4 distribution hardening (G4-D transport/SSE/CLI migration, DH-E retained history/replay/reconnect) merely because headless closure is complete — those remain outstanding.

Shared semantics do not imply shared mutable runtime state or identical lifecycle machinery.

## 12. Integration adapter boundary

Industrial adapters should sit behind a common semantic integration contract that identifies external identity mapping/version, direction, authority/trust expectations, transport/profile/security profile where relevant, lossiness/transformation rules, mapping/tool version, retry/idempotency behavior, provenance, and compatibility expectations.

Candidate protocols and standards include OPC UA, MQTT, Asset Administration Shell profiles, ERP/MES interfaces, FMI/co-simulation boundaries, and ISA-95/B2MML-style interchange where justified.

No protocol should become the canonical Arcogine domain model.

## 13. Persistence and history

Operational execution will eventually create durable artifacts whose historical identity matters: commands/results, observations, deployment records and effective applied-artifact provenance, reconciliation records, and drift/calibration proposals.

Execution-context identity has a narrower first requirement:

- a concrete context identity must be stable enough to appear in persisted later records and be re-established after restart;
- O1 does **not** require a context registry, context-history database, issuance service, alias store, or generic persistence framework;
- later registration/lifecycle requirements such as retirement, aliases, metadata history, or global uniqueness are separate operational persistence decisions.

The Governance `FileControlledRevisionAuthority` is a revision-specific adapter and must not be generalized into operational storage by reuse.

Storage technology remains deliberately unspecified. Future durable operational records require explicit retention, compatibility, and migration semantics appropriate to their own responsibilities.

## 14. Safety and failure principles

For production contexts:

1. Absence of authority is denial, not implicit permission.
2. An unverifiable actor/source/target is not silently treated as trusted.
3. Integrity/authenticity failure on a consequential path fails safe according to documented policy.
4. Ambiguous command outcome must remain representable as ambiguous.
5. Retry must be governed by explicit idempotency semantics.
6. Loss of observation must not be interpreted as successful state convergence.
7. External system rejection or partial failure must remain visible.
8. Rollback/compensation semantics must distinguish logical model rollback from physical-world reversibility.
9. Credential or trust loss must have explicit operational consequences rather than falling through to permissive behavior.
10. Execution context must never be silently defaulted from process/deployment location when consequence semantics are required.

These are architecture requirements, not claims that current Arcogine implements production-grade safety controls.

## 15. Cross-track dependency rule

This track may develop operational-owned contracts headlessly and in parallel, but it must not complete sibling-owned semantics by inventing substitutes.

Current dependency state:

- Governance G1 is **complete** and must be consumed for durable semantic fingerprint / controlled revision identity and historical resolution where applicable;
- Governance G2 semantic `ChangeSet`/impact (initial slice) is **complete** and must be consumed for semantic change attribution where applicable;
- Governance G3 generic requirement/assertion contract (`Requirement`, `Assertion`, `RequirementCatalogue`) is **complete**;
- Governance G4 conformance/finding semantics (`ConformanceEvaluator`, `ConformanceEvaluation`, `Finding`, initial slice) are **complete**; evidence/authorization capabilities remain outstanding;
- Governance G5 evidence-use semantics remain outstanding;
- Engine Gate 4 core/headless closure is complete — G4-A observations, G4-B supported runtime events, and G4-C headless acceptance evidence are all implemented — while Gate 4 distribution hardening (G4-D, DH-E) remains outstanding.

Synthetic fixtures remain appropriate only for still-missing sibling contracts. New synthetic G1 revision identity is no longer justified for operational deployment or revision-bound reconciliation work.

The detailed gate policy lives in the [Operational Execution and Digital Twin Readiness](../planning/operational-execution-digital-twin-readiness.md) plan.

## 16. Non-goals for the first readiness track

The first track does not require choosing an identity provider, implementing generic enterprise RBAC, choosing one universal industrial transport, building a full MES/SCADA platform, replacing PLC/device safety logic, implementing every ISA-95 object, building a universal workflow engine, making the simulation scheduler a wall-clock production scheduler, or collapsing Governance, runtime observations, and twin reconciliation into one generic evaluation framework.

O1 specifically does not implement actor authorization, target identity, deployment, commands, telemetry, reconciliation, a context registry, or public context-management APIs.

## 17. ADR triggers

[ADR-0013](decisions/0013-execution-context-identity.md) is the concrete **Proposed** O1 identity decision required before implementation. It settles the proposed separation of kind and concrete identity, initial consequence-oriented taxonomy, UUIDv4 representation and establishment semantics, permanent ID-to-kind binding, checked same-ID comparison/conflict handling, explicit authority/resolution boundary, projection compatibility rules, and the no-registry requirement.

O1 implementation must not proceed as though those decisions were Accepted while ADR-0013 remains Proposed. If ADR-0013 is Accepted, the implementation slice should follow it; if review changes the proposal, the implementation must follow the accepted decision rather than stale planning prose.

Later ADRs remain appropriate when implementation commits Arcogine to hard-to-reverse choices such as actor/capability/trust model, command correlation/idempotency, operational observation envelope/timestamps, deployment target/application provenance, reconciliation conflict authority, or production persistence/retention contracts.

Creating planning prose alone is not an ADR trigger; ADR-0013 is the required O1 decision record.
