# Operational Execution and Digital Twin Readiness

> **Status:** Proposed; O1 is the next operational implementation target  
> **Scope:** Establish the semantic and safety boundaries required before Arcogine can connect designed production semantics to real operational systems  
> **Authority:** Planning only; this document defines readiness gates, dependencies, and implementation sequencing, not current production capability  
> **Related:** [Operational Execution and Digital Twin Architecture](../architecture/operational-execution-digital-twin.md), [ADR-0013: Execution context identity](../architecture/decisions/0013-execution-context-identity.md), [Product Charter](../product/charter.md), [Architecture Overview](../architecture/overview.md), [Factory Design Architecture](../architecture/factory-design.md), [Governance and Conformance Architecture](../architecture/governance-conformance.md), [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md), [Gate 4 Runtime Observation and Event Delivery](gate-4-runtime-observation-event-delivery.md), [Governance and Conformance Capability](governance-conformance-capability.md)

## 1. Purpose

Arcogine's current executable core is simulation-first. This track prepares the platform for a later transition from deterministic simulated execution to controlled interaction with real operational systems without allowing production concerns to leak accidentally into simulation runtime semantics.

The track owns the missing bridge:

```text
Published production semantics
        ↓
Governed semantic identity / revision when applicable
        ↓
Execution context
        ↓
Deployment / command
        ↓
External system
        ↓
Operational observations
        ↓
Reconciliation
        ↓
Drift / calibration feedback
```

This work is intentionally separate from Factory Simulation Engine Readiness. Engine Readiness owns production semantics and deterministic simulation runtime truth. This track owns the additional semantics introduced when actions have external consequence and external systems become independent authorities.

## 2. Current repository grounding

This plan was originally written while several sibling contracts were still prospective. That is no longer true.

| Dependency / capability | Current status | Operational consequence |
|---|---|---|
| Governance G1 durable fingerprint + controlled revision history | **Complete** | O4 and revision-bound O6 must consume `ModelFingerprint`, `ControlledRevisionId`, `ControlledRevision`, and `ControlledRevisionAuthority`; synthetic G1 revision fixtures are no longer appropriate for new operational work |
| Governance G2 semantic `ChangeSet` / impact (initial slice) | **Complete** | O7 must consume the Governance-owned `ChangeSet`/`ImpactScope`/`SemanticChange` contracts for semantic change attribution rather than inventing a substitute |
| Governance G3 requirement/assertion contract | **Complete** | O7 should consume the Governance-owned `Requirement`/`Assertion`/`RequirementCatalogue` contracts rather than inventing a substitute once conformance-evaluation integration begins; G4+ conformance-evaluation/evidence/authorization capabilities remain outstanding |
| Governance G4 conformance / findings | **Outstanding** | O7 conformance/finding integration remains dependent on Governance |
| Governance G5 evidence / `EvidenceUse` | **Outstanding** | O5 ingestion remains independent; O9 evidence-use integration cannot close yet |
| Engine Gate 4 G4-A runtime observation slice | **Complete** | `RunId` and consumer-neutral `RuntimeObservation` exist for one factory simulation runtime epoch |
| Engine Gate 4 G4-B supported runtime events | **Complete** | The supported `RuntimeEventEnvelope`/`RuntimeEventType`/`RuntimeEventPayload` contract per ADR-0011 is implemented at the `FactoryRuntime` boundary |
| Engine Gate 4 G4-C headless closure | **Complete** | Gate 4 core/headless acceptance closure across the runtime/observation boundary is proven: fresh-observation reconstruction without replay, observation/event closure, and consumer-neutral bottleneck identification; distribution hardening (G4-D, DH-E) remains outstanding |
| Operational O1 execution-context identity | **Proposed ADR recorded; implementation outstanding** | [ADR-0013](../architecture/decisions/0013-execution-context-identity.md) records the concrete proposed identity contract. O1 remains the next Operational implementation target and must follow ADR-0013 if/when it becomes Accepted. |

The important Engine/Operational boundary is:

```text
RunId
    identity/correlation for one fresh simulation runtime epoch

ExecutionContext
    Arcogine-owned semantics describing the consequence/environment context
    under which operationally relevant activity occurs
```

These concepts may later be correlated, but they are not synonyms. `RunId` must not be reused as execution-context identity.

## 3. Ownership boundaries

### Factory Design / Engine Readiness owns

- canonical production-system semantics;
- deterministic model validation/publication/instantiation;
- workload and execution semantics;
- dispatch, queues, assignments, operations, transfers;
- simulation session control;
- simulation runtime observations and supported runtime events;
- spatial runtime consequences.

### Operational Execution / Digital Twin owns

- execution-context classification and identity;
- actor/service/external-system identity at the operational boundary;
- operational trust/authenticity semantics;
- authority/capability checks for consequential actions;
- command/acknowledgement/result lifecycle;
- deployment target/application semantics;
- effective applied-artifact provenance;
- external observation ingestion and provenance;
- modeled-versus-observed reconciliation;
- divergence/drift classification;
- calibration feedback;
- adapter-level resilience, idempotency, and recovery semantics.

### Governance / Conformance owns

- durable semantic fingerprint policy;
- controlled revision identity, lineage, authoritative persistence, and exact historical semantic-state resolution;
- semantic ChangeSets;
- requirement/assertion versions;
- conformance evaluation;
- evidence-use relationships;
- findings;
- governed change and exceptions;
- audit/compliance projections.

Operational facts may become Governance evidence, but Governance does not own telemetry acquisition, command execution, deployment application, or twin reconciliation. Conversely, Operational Execution must not invent replacement controlled-revision, ChangeSet, finding, or evidence-use types because it needs them as inputs.

## 4. Readiness sequence and cross-track dependency status

The local operational sequence remains:

```text
O1  Execution-context identity
 ↓
O2  Actor, trust, authority, and capability boundary
 ↓
O3  External command / actuation lifecycle
 ↓
O4  Deployment target and deployment-record semantics
 ↓
O5  External observation ingestion and provenance
 ↓
O6  Modeled-versus-observed reconciliation
 ↓
O7  Divergence, drift, and calibration feedback
 ↓
O8  Operational resilience / recovery semantics
 ↓
O9  First live-system adapter proving ground
```

This is not a self-contained linear program. Sibling dependencies and local prerequisites must be kept separate.

| Operational gate | Sibling dependency status | Current consequence |
|---|---|---|
| O1 execution context | **No hard sibling prerequisite.** Engine G4-A is available but is not the identity source. | ADR-0013 records the concrete Proposed O1 identity decision. O1 implementation remains outstanding and should proceed only against the Accepted form of that decision. |
| O2 identity/trust/authority | **No Governance gate prerequisite.** | O2 remains operational work after O1. Production trust requirements remain entirely outstanding. |
| O3 command lifecycle | **Partially satisfied.** Stable factory production semantics exist, but the applicable real target-operation contract is not yet an operational capability. | Headless command lifecycle work may proceed after O1/O2; live command integration must map to owned production semantics rather than protocol-driven inventions. |
| O4 deployment | **Governance G1 satisfied.** | O4 is not implemented, but it is no longer blocked on durable revision identity. It must consume Governance G1 authoritative identities/history instead of synthetic revision fixtures. |
| O5 external observations | **No G5 prerequisite for ingestion.** Engine runtime observations are a sibling simulation concept, not an O5 prerequisite. | O5 may define independent external-observation provenance. G5 is required only when observations are used through Governance `EvidenceUse`. |
| O6 reconciliation | **Governance G1 satisfied; modeled-side Engine maturity partial.** | Historical/revision-bound reconciliation can use authoritative G1 resolution when implemented. O6 still depends locally on O5 and on the modeled semantics relevant to the reconciliation. |
| O7 drift/calibration | **Governance G2 (initial slice) and G3 satisfied; G4 outstanding.** | Operational-local drift analysis should consume Governance-owned `ChangeSet`/`ImpactScope` and registered `Requirement`/`Assertion` contracts where applicable, but conformance/finding semantics remain Governance-owned and outstanding. |
| O8 resilience | **No separate Governance prerequisite.** | Depends primarily on the command/observation identity and persistence contracts selected by O3/O5; those are not implemented yet. |
| O9 live adapter | **Governance G1 satisfied; G5 outstanding; Engine Gate 4 core/headless closure complete.** | A protocol test server may prove local adapter behavior, but O9 cannot close until its local O2-O8 requirements, Governance evidence-use integration where required, and applicable Engine distribution hardening are actually available. |

### 4.1 Fixture rules after Governance G1

Synthetic fixtures remain allowed where a sibling-owned contract genuinely has not landed, under these constraints:

1. A fixture stands in for a sibling-owned input; it does not define that sibling contract.
2. A fixture must be visibly test/fixture scoped and must not escape as a shared production type merely because it was convenient.
3. Completion of an operational-local behavior criterion may be demonstrated with fixtures, but a criterion explicitly requiring an outstanding sibling capability remains incomplete.
4. Synthetic operational adapters do not satisfy Engine Readiness gates.
5. **Do not create new synthetic revision/fingerprint identity fixtures for O4/O6. Governance G1 is complete and its authoritative contracts are available.**
6. **Do not create new synthetic ChangeSet/impact fixtures for O7. Governance G2's initial slice is complete and its authoritative `ChangeSet`/`ImpactScope`/`SemanticChange` contracts are available.**
7. **Do not create synthetic requirement/assertion contracts for O7. Governance G3 is complete and its authoritative `Requirement`/`Assertion`/`RequirementCatalogue` contracts are available.** Synthetic conformance/finding or evidence-use fixtures still do not satisfy Governance G4/G5.
8. When a later sibling contract lands, replace fixture mappings with the owned contract rather than preserving a parallel identity system.

## 5. O1 — Execution-context identity

### 5.1 Goal

Make operational consequence explicit before real-system behavior is introduced.

O1 establishes an Arcogine semantic contract that answers two different questions:

```text
ExecutionContextKind
    what consequence/environment semantics apply?

ExecutionContextId
    which concrete Arcogine execution context is this?
```

An immutable `ExecutionContext` value binds those two responsibilities for downstream operational artifacts.

This distinction is required because `PRODUCTION` or `STAGING` is a classification, not the identity of one concrete environment. Arcogine must be able to represent multiple simultaneous production, staging, or non-production contexts without treating all contexts of one kind as the same context.

### 5.2 Context classification

The original planning vocabulary was:

```text
SIMULATION
REPLAY
TEST
STAGING
PRODUCTION
```

That list is not an implementation enum merely because it appeared in the first proposal.

[ADR-0013](../architecture/decisions/0013-execution-context-identity.md) now proposes the concrete consequence-oriented initial taxonomy:

```text
PRODUCTION
STAGING
SIMULATION
```

It proposes `PRODUCTION` for real production-consequential activity, `STAGING` for production-like integration without production consequence, and `SIMULATION` where an Operational artifact needs to classify/correlate simulated activity while remaining distinct from Engine `RunId`.

`REPLAY` remains a processing/history-interpretation mode rather than an O1 context kind unless a later concrete consequence/authority invariant proves otherwise. Generic software `TEST` remains a process/build/test concern, not an operational context kind. The implementation must follow these semantics only if ADR-0013 becomes Accepted; a Proposed ADR does not make the taxonomy established architecture.

### 5.3 Concrete context identity

O1 requires a stable concrete context identity in addition to classification.

ADR-0013 proposes an opaque RFC 9562 UUID version 4 `ExecutionContextId`, with canonical lowercase hyphenated textual form and strict semantic-boundary parsing. It also proposes:

- one `ExecutionContextId` identifies one concrete Arcogine execution context;
- semantic context identity compares by ID, not by label, target, deployment, model, revision, actor, URL, namespace, or process location;
- different IDs are distinct even when their kinds are equal;
- the same ID with the same kind is the same context;
- the same ID with a different kind is a binding conflict that must fail when the values meet, not a second unequal context;
- the identifier is safe to persist in later operational records and safe to expose through versioned external projections;
- the same concrete context can be recognized after process restart when the same identity and permanently bound kind are supplied/resolved;
- changing a context's human-facing name does not change identity;
- context identity does not imply external target, deployed revision/model, actor identity, authorization, or simulation `RunId`.

Identity semantics are distinct from issuance infrastructure: Arcogine or an operator/deployment system may establish a UUIDv4 once, and configuration may later carry it, but O1 does not require a centralized issuance service or derive identity from deployment/configuration metadata.

These representation and lifecycle rules remain proposed until ADR-0013 is Accepted.

### 5.4 Identity boundaries

The operational track must preserve these separate identities:

```text
ModelFingerprint
    which semantic content?

ControlledRevisionId
    which governed historical occurrence?

RunId
    which simulation runtime epoch?

ExecutionContextKind
    what operational consequence/environment semantics apply?

ExecutionContextId
    which concrete Arcogine operational context?

Target identity
    which external system/resource receives an action?

Actor identity
    who/what requested or performed the consequential action?
```

Required non-equivalences:

```text
ControlledRevisionId != ExecutionContextId
RunId               != ExecutionContextId
target identity      != ExecutionContextId
actor identity       != ExecutionContextId
ModelFingerprint     != ExecutionContextId
```

A later command, deployment, reconciliation result, or interpretation may correlate several of these identities. Correlation never collapses them into one identifier.

### 5.5 Authoritative boundary

Execution context must be established explicitly at an Operational Execution boundary and then propagated as data.

Downstream operational code may rely on an `ExecutionContext` only after it has been supplied or resolved through the O1-owned boundary. It must not infer context from:

- Spring profile;
- process hostname;
- environment variable alone;
- API URL;
- deployment namespace;
- caller convention;
- the presence or absence of a `RunId`;
- the presence or absence of a `ControlledRevisionId`;
- target identity;
- actor identity.

Environment variables or deployment tooling may later **carry** an already-defined context identifier as configuration input, but they are projections/configuration mechanisms, not the semantic authority that decides what the identifier means.

The first O1 implementation does not need authorization. It only needs a boundary strong enough that O2 can later evaluate rules such as "allowed in staging" or "production-consequential in production" against an explicit context kind and concrete context identity.

### 5.6 Persistence and restart semantics

O1 requires durable **identity semantics**, not durable **context-registry persistence**.

ADR-0013 proposes that the same durable ID plus its permanently bound kind re-established after restart identifies the same context. O1 does not require Arcogine to implement a repository of contexts, lifecycle administration, discovery, renaming, issuance history, aliases, or retirement.

If a later capability requires authoritative durable context registration, uniqueness across independent Arcogine installations, aliases, retirement, migration, or context metadata history, that is a separate operational persistence concern and may require another ADR.

`FileControlledRevisionAuthority` is Governance-specific and must not be reused as a generic operational persistence mechanism.

### 5.7 External-observation independence

Raw external observations remain independent operational facts.

They:

- retain their own source identity and source/receipt time provenance;
- may report a source environment/context when that information genuinely comes from the source;
- must not be forced to invent an Arcogine `ExecutionContextId`;
- must not be forced to invent a `ControlledRevisionId`;
- must not be forced to invent a `ModelFingerprint`.

Arcogine-owned interpretations, reconciliation results, commands, deployments, or later evidence-use associations may establish those relationships when appropriate.

### 5.8 Public compatibility boundary

ADR-0013 now proposes the O1 representation and evolution rules that were previously open: UUIDv4 identity with strict canonical parsing, `PRODUCTION`/`STAGING`/`SIMULATION` taxonomy, explicit unknown-kind failure at the semantic boundary, decentralized establishment through the O1 boundary, permanent ID-to-kind binding with checked conflict handling, and explicit versioned migration handling for public/persisted changes.

ADR-0012 remains authoritative:

```text
Arcogine semantic contract
        ↓
versioned projection / adapter
        ↓
external representation
```

JSON, OpenAPI, OPC UA, MQTT, CloudEvents, environment variables, or deployment tooling must not become the semantic authority for execution-context identity. Any stable public projection still requires its own versioning, mapping, validation, compatibility evidence, and migration behavior.

### 5.9 O1 acceptance criteria

O1 is complete when:

1. context classification and concrete context identity are explicit and distinct;
2. one immutable operational context value can carry both responsibilities;
3. multiple contexts of the same kind are representable and compare by concrete identity;
4. context is supplied/resolved explicitly at an Operational Execution boundary rather than inferred from process location or transport configuration;
5. simulation `RunId`, Governance `ControlledRevisionId`, target identity, actor identity, and model fingerprint remain non-conflated;
6. the contract can survive process restart when the same concrete context identity is re-established, without requiring a context registry in O1;
7. O2 can make consequence-sensitive decisions from context kind/identity without forking factory production semantics;
8. raw external observations are not forced to invent Arcogine context/model/revision relationships;
9. compatibility and migration rules are fixed before any stable public/persisted representation is introduced;
10. tests prove checked identity comparison/conflict handling, validation, explicit-boundary use, non-inference, and identity separation from `RunId`, `ControlledRevisionId`, and `ModelFingerprint`.

ADR-0013 addresses the architectural compatibility questions behind these criteria, but **O1 is not complete** until the implementation and tests land after the ADR is Accepted.

### 5.10 O1 non-goals

- actor identity, authentication, authorization, or policy evaluation;
- OAuth/OIDC, certificates/PKI, or secrets management;
- target identity or target registration;
- commands, deployments, telemetry ingestion, reconciliation, calibration, resilience, or live adapters;
- a generic environment-management system;
- a context registry or generic persistence framework;
- public HTTP/OpenAPI/UI configuration for contexts;
- making simulation runtime identity an operational identity;
- choosing a protocol-specific representation.

## 6. First O1 implementation slice

The first implementation slice should fit in one PR **after the O1 ADR decision is accepted**.

### Owning module

No Operational Execution module exists in the current Gradle graph. O1 is the first concrete durable semantic responsibility of this track and is sufficient justification to introduce a dedicated `:operational` module rather than placing these concepts in `:simulation`, `:governance`, `:factory`, or API DTOs.

Proposed dependency direction:

```text
:types
   ↑
:operational
   ↑
future operational adapters / API projections
```

The first slice should not require `:operational -> :simulation`, `:governance`, or `:factory`. Correlation with `RunId` or `ControlledRevisionId` belongs in later operational records that actually need those references; O1 must not create coupling merely to prove non-equivalence.

ADR-0013 proposes this module/dependency ownership as part of the O1 decision. Implementation must follow the Accepted form of that ADR rather than treating planning prose as authority.

### Values/contracts to introduce

The minimum semantic shape is:

```text
ExecutionContextKind
ExecutionContextId
ExecutionContext
```

If ADR-0013 is Accepted as proposed, `ExecutionContextKind` contains `PRODUCTION`, `STAGING`, and `SIMULATION`; `ExecutionContextId` is opaque UUIDv4; and `ExecutionContext` is an immutable ID-plus-kind binding with permanent ID-to-kind semantics.

`ExecutionContext` should not accumulate target, actor, model, revision, deployment, command, hostname, namespace, URL, authentication, permissions, or presentation metadata.

### Invariants

- non-null/valid kind;
- non-null/valid concrete identity;
- different IDs are distinct regardless of kind;
- same ID plus same kind is the same context;
- same ID plus different kind is an explicit binding conflict, not ordinary inequality;
- context kind is explicit, never inferred;
- construction/resolution failure is explicit rather than falling back to a default production/non-production context;
- no `RunId`/`ControlledRevisionId`/`ModelFingerprint` conversion constructors or derived-ID helpers;
- no process/environment inspection inside the semantic types.

### Persistence

No persistence adapter is required for the first O1 PR. The UUIDv4 representation proposed by ADR-0013 is stable enough for later persistence and public projections, while context registration/history remains deferred.

### Expected tests

At minimum:

- distinct concrete IDs of the same kind remain distinct contexts;
- the same concrete ID and kind re-established after reconstruction compare as the same context;
- same ID plus different kind produces a checked binding conflict wherever both values are observable, rather than a second distinct context;
- malformed/non-canonical UUIDs and invalid/unknown kinds fail explicitly;
- context is not derivable from `RunId`, `ControlledRevisionId`, or `ModelFingerprint`;
- no dependency on Spring, API DTOs, factory runtime, Governance authority, hostname, environment profile, or deployment namespace;
- module-dependency checks preserve the intended direction.

### Explicit non-goals for the PR

All O2-O9 behavior, persistence, registration, adapters, API projections, commands, deployments, telemetry, reconciliation, live-system integration, and production actuation remain out of scope.

## 7. O1 ADR decision

[ADR-0013: Execution context identity](../architecture/decisions/0013-execution-context-identity.md) is now the concrete **Proposed** O1 identity decision.

It proposes:

1. separate first-class `ExecutionContextKind` and `ExecutionContextId` concepts;
2. initial consequence-oriented kinds `PRODUCTION`, `STAGING`, and `SIMULATION`, with `REPLAY` and generic `TEST` excluded;
3. opaque RFC 9562 UUIDv4 `ExecutionContextId`, canonical lowercase textual form, strict parsing, and identity independent of labels/target/model/revision/actor/process metadata;
4. decentralized establishment through an explicit O1 parse/validation/resolution boundary, with configuration allowed to carry but not define identity;
5. permanent one-ID-to-one-kind binding, so a staging-to-production consequence change receives a new ID;
6. checked semantic context comparison: different ID = distinct, same ID/same kind = same, same ID/different kind = explicit binding conflict;
7. an immutable `ExecutionContext` containing only ID and kind;
8. the dedicated `:operational` ownership/dependency direction for the first implementation;
9. versioned projection/migration rules under ADR-0012 and explicit failure for unsupported semantic kinds;
10. durable restart identity semantics without a context registry;
11. preservation of raw external-observation independence and all neighboring identity boundaries.

Because ADR-0013 is **Proposed**, none of those decisions should be described as implemented or accepted yet. O1 implementation remains outstanding and must follow ADR-0013 if/when it becomes Accepted. If review changes the ADR before acceptance, this plan should follow the Accepted decision rather than preserve stale proposal wording.

The ADR deliberately does not choose O2 authorization, O3 command identity, O4 deployment records, O5 observation envelopes, or protocol/infrastructure details.

## 8. O2 — Actor, trust, authority, and capability boundary

O2 remains proposed and unimplemented. It will consume O1 so consequential requests can be evaluated against explicit context kind and identity. It owns actor identity/provenance, claimed-versus-verified identity, authority/capability checks, denial, least privilege, and production trust semantics. It does not change the O1 identity contract.

## 9. O3 — External command / actuation lifecycle

O3 remains proposed and unimplemented. It will keep requested action, authorization, submission, acknowledgement, execution, success/failure/unknown outcome, and later reconciliation distinct. Commands must correlate actor, execution context, and target without collapsing those identities. Real command integration must map to stable production semantics owned by the relevant domain/Engine surface.

## 10. O4 — Deployment target and deployment-record semantics

O4 remains proposed and unimplemented, but its Governance G1 prerequisite is now satisfied.

When implemented, deployment records must reference the exact source `ModelFingerprint` and, when the deployment is revision-bound, the authoritative `ControlledRevisionId` supplied/resolved through Governance G1. New O4 work must not use synthetic revision identity as a substitute for the available Governance contract.

Deployment still owns target identity, transformation/application provenance, verification outcome, and rollback reference. Governance revision identity does not make deployment complete and does not identify the target or execution context.

## 11. O5 — External observation ingestion and provenance

O5 remains proposed and unimplemented. It owns durable operational facts with independent source/subject/time/quality/trust provenance. Governance G5 is not a prerequisite for ingestion; it is required only for shared evidence-use integration. Engine `RuntimeObservation` is a simulation runtime concept and must not be reused as the raw external-observation identity/envelope merely because both are observations.

O1 and ADR-0013 do not change that boundary: a raw observation is not required to carry an Arcogine `ExecutionContextId`, `ControlledRevisionId`, or `ModelFingerprint`.

## 12. O6 — Modeled-versus-observed reconciliation

O6 remains proposed and unimplemented. Governance G1 now makes authoritative historical revision resolution available where a controlled revision applies. O6 may therefore consume `ControlledRevisionAuthority` to resolve the exact historical semantic state rather than using a synthetic revision fixture or the mutable current model.

That satisfied dependency does not implement reconciliation. O6 still must define independent observation inputs, source authority/trust/freshness, temporal alignment, conflict/divergence/unknown semantics, and reproducibility.

## 13. O7 — Divergence, drift, and calibration feedback

O7 remains proposed and unimplemented. Operational-local drift analysis may eventually produce candidate changes and should consume the now-complete Governance G2 `ChangeSet`/impact and G3 requirement/assertion contracts where applicable. Final cross-track integration still depends on Governance G4 conformance/finding semantics, which is not currently complete. Operational Execution must not invent substitutes for G4.

## 14. O8 — Operational resilience and recovery semantics

O8 remains proposed and unimplemented. It depends primarily on the concrete command/observation identity, persistence, idempotency, retry, ambiguity, and resynchronization contracts selected by O3/O5. No sibling Governance gate independently closes O8.

## 15. O9 — First live-system adapter proving ground

O9 remains proposed and unimplemented. Governance G1 is available for real deployment revision identity, but G5 evidence-use integration is still outstanding. Engine Gate 4 core/headless closure (G4-A/G4-B/G4-C) is complete, but the distribution hardening a live-system adapter would need (G4-D transport/SSE migration, DH-E retained history/replay/reconnect) is not yet implemented. O9 also depends on the local O1-O8 safety and operational semantics; a protocol connection alone is never completion evidence.

## 16. Cross-cutting persistence dependency

Later operational artifacts such as commands, acknowledgements/results, observations, deployment records, reconciliation outputs, and drift/calibration proposals require stable identity, history, retention, and compatibility. Storage technology is not selected here.

O1 is deliberately narrower: it requires stable context identity semantics that later records can carry, not a generic operational database or context registry.

## 17. Temporal and measurement dependencies

Before O5/O6 become public contracts, Arcogine must define temporal vocabulary sufficient to distinguish simulation time, source/event time, observed-at time, received/ingested-at time, effective intervals, recorded-at time, deployment time, and processing wall-clock time.

Before measured values become durable O5 contracts, Arcogine also needs explicit units/dimensions, precision, and conversion semantics. Neither dependency belongs in O1.

## 18. Standards and adapter policy

Potential later adapters include OPC UA / IEC 62541, MQTT, AAS profiles, ERP/MES APIs, ISA-95/B2MML interchange, and FMI/co-simulation where appropriate.

The policy remains:

> **Define Arcogine identity, trust, authority, command, observation, provenance, transformation, and reconciliation semantics first; implement protocol adapters over those contracts second.**

No readiness gate is satisfied merely by choosing or connecting a protocol.

## 19. Relationship to current Engine Readiness

Engine Readiness continues independently. Gate 4 core/headless closure is complete: the G4-A observation slice (`RunId` and consumer-neutral `RuntimeObservation`), the G4-B supported `RuntimeEvent` contract, and G4-C headless acceptance closure are all implemented. Gate 4 distribution hardening (G4-D transport/SSE/CLI migration, DH-E retained history/replay/reconnect) remains outstanding.

Operational O1 must therefore preserve both truths:

- simulation runtime identity is already explicit and useful;
- operational execution-context identity is a different semantic responsibility and must not be derived from `RunId`.

## 20. Relationship to Governance and Conformance

Governance G1 is complete and now supplies the authoritative durable fingerprint/revision history needed by later deployment and revision-bound reconciliation. Governance G2's initial slice is also complete and now supplies the authoritative semantic `ChangeSet`/`ImpactScope` needed by candidate-change attribution. Governance G3 is complete and supplies the generic registered `Requirement`/`Assertion`/`RequirementCatalogue` contract for later conformance integration.

Governance G4/G5 remain future dependencies for the operational capabilities that need conformance/findings and evidence use. There should be no generic evaluation or persistence framework introduced merely because these tracks have analogous needs.

## 21. Next action

1. Review and accept, revise, or reject [ADR-0013](../architecture/decisions/0013-execution-context-identity.md). Its current status is Proposed.
2. If accepted, implement one headless `:operational` PR containing only the minimum execution-context values/boundary and tests described in section 6, following the Accepted ADR exactly.
3. Update `docs/architecture/overview.md` only when that code is actually implemented; until then O1 remains unimplemented and the overview must not claim executable Operational behavior.

## 22. Exit condition

This readiness track has served its purpose when Arcogine can demonstrate, headlessly and reproducibly, that one Governance-identified controlled semantic revision can be authorized for a concrete execution context, transformed/applied through an attributable adapter/profile, verified against the effective applied representation, observed through independently provenanced operational facts over a verified trust boundary, reconciled against modeled intent, recovered safely across realistic failures, and fed back into governed model improvement without violating the ownership boundaries above.
