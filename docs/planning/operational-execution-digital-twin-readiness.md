# Operational Execution and Digital Twin Readiness

> **Status:** Proposed  
> **Scope:** Establish the semantic and safety boundaries required before Arcogine can connect designed production semantics to real operational systems  
> **Authority:** Planning only; this document defines readiness gates, not current capability or accepted production architecture  
> **Related:** [Operational Execution and Digital Twin Architecture](../architecture/operational-execution-digital-twin.md), [Product Charter](../product/charter.md), [Architecture Overview](../architecture/overview.md), [Factory Design Architecture](../architecture/factory-design.md), [Governance and Conformance Architecture](../architecture/governance-conformance.md), [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md), [Governance and Conformance Capability](governance-conformance-capability.md)

## 1. Purpose

Arcogine's current executable core is simulation-first. This track prepares the platform for a later transition from deterministic simulated execution to controlled interaction with real operational systems without allowing production concerns to leak accidentally into simulation runtime semantics.

The track owns the missing bridge:

```text
Published production semantics
        ↓
Controlled revision / authorization
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

This work is intentionally separate from Factory Simulation Engine Readiness. Engine Readiness owns production semantics and deterministic runtime truth. This track owns the additional semantics introduced when actions have real-world consequence and external systems become independent authorities.

## 2. Ownership boundaries

### Factory Design / Engine Readiness owns

- canonical production-system semantics;
- deterministic model validation/publication/instantiation;
- workload and execution semantics;
- dispatch, queues, assignments, operations, transfers;
- simulation session control;
- simulation/runtime observations and events;
- spatial runtime consequences.

### Operational Execution / Digital Twin owns

- execution-context identity;
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
- controlled revision lineage;
- semantic ChangeSets;
- requirement/assertion versions;
- conformance evaluation;
- evidence-use relationships;
- findings;
- governed change and exceptions;
- audit/compliance projections.

Operational facts may become Governance evidence, but Governance does not own telemetry acquisition, command execution, deployment application, or twin reconciliation. Conversely, Operational Execution must not invent replacement controlled-revision, ChangeSet, finding, or evidence-use types because it needs them as inputs.

## 3. Readiness sequence and dependency policy

The local operational sequence is:

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

This is **not** a self-contained linear program. Several gates can be developed headlessly using synthetic fixtures before sibling tracks are complete, but those fixtures do not satisfy sibling-owned gates and must not become duplicate shared abstractions.

### 3.1 Cross-track dependency and fixture matrix

| Operational gate | May proceed with operational-owned synthetic fixtures? | Sibling capability required for final completion/integration |
|---|---|---|
| O1 execution context | Yes | No hard Governance dependency; public contract must remain compatible with Engine/runtime contexts |
| O2 identity/trust/authority | Yes | No specific G-gate, but production integration also requires the security/trust acceptance criteria in this plan |
| O3 command lifecycle | Yes | Requires stable production semantics/target operations from the relevant Engine readiness surface before a real command contract is considered integrated |
| O4 deployment | Yes, with synthetic revision/fingerprint fixtures | **Governance G1** for durable fingerprint + controlled revision identity; synthetic revision fixtures do not close O4's durable-identity acceptance criteria |
| O5 external observations | Yes | No G5 dependency for ingestion itself; **Governance G5** is required only for evidence-use integration |
| O6 reconciliation | Yes, with synthetic model/revision fixtures | Durable model identity from **G1** is required for historical/reproducible revision-bound reconciliation where a controlled revision applies; Engine semantics provide the modeled side |
| O7 drift/calibration | Yes, for operational analysis/proposal generation | **G2** for durable semantic ChangeSet/impact semantics and **G4** for conformance/finding integration; Operational Execution must not define substitutes |
| O8 resilience | Yes | Depends on whatever command/observation persistence contracts O3/O5 have selected; no separate G-gate closes it |
| O9 live adapter | Protocol test server may be used before physical integration | Requires the applicable Engine semantics, **G1** for real deployment identity, and **G5** for the Governance evidence-use integration criterion; if O9 exercises governed calibration/change, G2/G4 apply as well |

### 3.2 Fixture rules

Synthetic fixtures are allowed to keep this track parallel and headless, under these constraints:

1. A fixture stands in for a sibling-owned input; it does not define that sibling contract.
2. A fixture must be visibly test/fixture scoped and must not escape as a shared production type merely because it was convenient.
3. Completion of an operational-local behavior criterion may be demonstrated with fixtures, but any criterion explicitly requiring durable sibling identity/evidence/change semantics remains **blocked** until the owning sibling gate exists.
4. Synthetic operational adapters do not satisfy Engine Readiness gates.
5. Synthetic revision, ChangeSet, finding, or evidence-use fixtures do not satisfy Governance G1/G2/G4/G5.
6. When the sibling contract lands, the fixture must be replaced by an adapter/mapping to that contract rather than preserved as a parallel identity system.

## 4. O1 — Execution-context identity

### Goal

Make consequence explicit before real-system behavior is introduced.

Every command, deployment, reconciliation result, and externally relevant run must identify its Arcogine execution context. External observations retain the source context/environment they actually provide; Arcogine must not fabricate one when absent.

The initial conceptual Arcogine contexts are:

```text
SIMULATION
REPLAY
TEST
STAGING
PRODUCTION
```

The exact names and Java/API representation remain open.

### Acceptance criteria

O1 is complete when:

1. execution context is represented explicitly rather than inferred from environment/process location;
2. context identity can be attached to externally relevant Arcogine operational artifacts;
3. code can distinguish simulated/test behavior from production-consequential behavior at an authoritative boundary;
4. production-only restrictions can be expressed without forking the production-domain ontology;
5. compatibility/migration rules for the context identifier are documented before it becomes a public contract;
6. source observations are not forced to invent an Arcogine context/revision relationship they do not intrinsically have.

### Non-goals

- choosing deployment namespaces;
- defining all environment-management infrastructure;
- selecting an authentication provider.

## 5. O2 — Actor, trust, authority, and capability boundary

### Goal

Represent who or what is requesting a consequential action, whether that identity is sufficiently verified for the operation, and what it is allowed to do in the target execution context.

Conceptual actor categories include:

```text
Human
Agent
Service
ExternalSystem
```

### Acceptance criteria

O2 is complete when:

1. a consequential request carries stable actor identity/provenance;
2. the boundary can distinguish claimed identity from sufficiently verified identity;
3. authority is checked explicitly before command submission or deployment application;
4. capabilities/scopes can be constrained by target and execution context;
5. denial and trust-verification failure are representable and auditable;
6. autonomous agents use the same authority boundary as human/service callers rather than a privileged bypass;
7. production policy can fail safe when identity/trust cannot be established.

### Production trust requirements

Before any live production-consequential adapter can be considered ready, Arcogine must also have explicit semantics for:

- peer/source/target identity verification;
- integrity/authenticity of consequential command and observation paths;
- credential/secret lifecycle appropriate to the selected integration;
- least privilege;
- trust-boundary/threat assumptions;
- revocation/expiry or equivalent loss-of-trust behavior;
- fail-safe treatment of unverifiable or integrity-failed inputs.

These requirements deliberately do **not** choose OAuth/OIDC, certificates, an OPC UA security profile, a secrets manager, or another concrete mechanism.

### Non-goals

- selecting an identity provider;
- enterprise directory synchronization;
- generic organization/user-management UI.

## 6. O3 — External command / actuation lifecycle

### Goal

Separate requested action from external acceptance, execution, success, failure, and observed physical result.

Conceptually:

```text
Requested
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
Reconciled
```

### Required semantics

- stable command ID;
- correlation to actor/context/target;
- source model fingerprint/revision where the command is derived from controlled semantics;
- verified trust context for consequential production commands;
- idempotency policy;
- requested value/action;
- validation and authorization outcome;
- submission/acknowledgement facts;
- timeout/retry semantics;
- explicit unknown/ambiguous outcome;
- partial failure where the target supports multi-part operations;
- resulting observation/reconciliation links.

### Acceptance criteria

O3 local behavior is demonstrated when a synthetic adapter proves that:

1. identical retry of an idempotent command does not create duplicate effect;
2. rejection remains distinct from transport failure;
3. accepted-but-not-yet-complete remains distinct from success;
4. timeout can result in `UNKNOWN` rather than false failure/success;
5. external result facts remain inspectable independently of the original request;
6. an unverifiable production peer cannot reach the submission boundary under production policy.

A real command contract is not considered operationally integrated until the target action maps to stable production semantics owned by the Engine/Factory domain.

## 7. O4 — Deployment target and deployment-record semantics

### Goal

Represent applying an authorized semantic revision to one or more real execution targets without treating publication, approval, rendering/transformation, and deployment as the same lifecycle.

Conceptually:

```text
ControlledRevision
       ↓
DeploymentPlan
       ↓
Target(s)
       ↓
Authorization
       ↓
Render / map / transform
       ↓
Apply
       ↓
Verify
       ↓
Promote / Rollback / Fail
```

### Required provenance

A deployment record must be able to bind:

```text
source semantic fingerprint
controlled revision ID (when available)
target identity + execution context
adapter/profile/mapping/transformation identity + version
rendering/tool version when material
rendered/applied artifact fingerprint, when Arcogine materializes one
  OR authoritative external applied-version/reference
authorization
apply acknowledgement
verification outcome
prior target state / rollback reference
```

### Acceptance criteria

O4 is complete when:

1. deployment target identity is durable;
2. deployment record references the exact source semantic fingerprint and controlled revision using **Governance G1** identities;
3. deployment provenance records the adapter/profile/mapping/transformation version that produced the target representation;
4. when a rendered/applied artifact exists, its fingerprint or authoritative external applied-version/reference is retained;
5. approval and deployment are separate artifacts;
6. staging and production targets can hold different revisions legitimately;
7. verification outcome is separate from apply acknowledgement;
8. rollback can reference the prior known target state without implying physical reversibility where none exists.

O4 implementation may be prototyped with synthetic revision fixtures before G1, but criteria 2 and the durable historical interpretation of criteria 3-4 remain blocked until the Governance-owned identity contract exists.

## 8. O5 — External observation ingestion and provenance

### Goal

Introduce a durable operational-fact boundary independent of simulation observations and Governance evidence use.

A durable observation should support concepts equivalent to:

```text
ObservationId
SourceSystemId
ObservedSubjectId
ObservedFact / Value
Unit / Dimension
ObservedAt
ReceivedAt
Quality / Confidence
Trust / authenticity provenance where required
Correlation
RawSourceReference (optional)
```

A raw external observation does **not** carry a model fingerprint or controlled revision merely because Arcogine later interprets it.

### Acceptance criteria

O5 is complete when:

1. observations have stable source and subject identity;
2. source time and ingestion time are distinguishable;
3. quality/confidence can be represented when supplied by the source;
4. trust/authenticity status can be represented where the integration requires it;
5. duplicate/replayed source messages can be handled deterministically under a documented policy;
6. operational observations can later be referenced by Governance as evidence without changing their operational identity;
7. transport-specific payloads are adapted into a transport-neutral semantic form;
8. ingestion does not require an Arcogine model fingerprint/revision binding.

Governance G5 is **not** a prerequisite for O5 ingestion. It becomes a prerequisite only when an observation is consumed through the shared `EvidenceUse` boundary.

## 9. O6 — Modeled-versus-observed reconciliation

### Goal

Determine what Arcogine currently believes about operational reality without mutating the published model or blindly trusting the latest message.

Reconciliation must consider:

- expected/model state;
- latest relevant observations;
- source authority and trust status;
- freshness/staleness;
- pending command/deployment transitions;
- conflicts between sources;
- confidence/inference where needed;
- temporal alignment.

### Acceptance criteria

O6 is complete when:

1. agreement, missing data, stale data, conflict, divergence, and unknown state are distinguishable;
2. reconciliation result identifies which model fingerprint/revision (when applicable) and independent observations were considered;
3. a command being accepted does not cause reconciled state to change until the reconciliation policy permits it;
4. contradictory authoritative observations remain visible rather than last-write-wins silently;
5. reconciliation is reproducible from retained inputs under the same policy/version;
6. unverifiable inputs can be excluded, downgraded, or marked unknown according to explicit policy rather than silently treated as authoritative.

O6 may be developed against synthetic model/revision fixtures. Historical revision-bound reconciliation that claims durable controlled identity depends on G1.

## 10. O7 — Divergence, drift, and calibration feedback

### Goal

Close the loop from operational reality back to model improvement without allowing live telemetry to mutate canonical semantics directly.

Conceptually:

```text
Model expectation
     +
Operational history
     ↓
Drift analysis
     ↓
Candidate calibration/change
     ↓
Governance ChangeSet / impact
     ↓
Validation / simulation / conformance
     ↓
Controlled revision
```

### Acceptance criteria

Operational-local analysis is demonstrated when:

1. persistent deviation can be distinguished from a single operational anomaly;
2. calibration proposals identify the source observations and model parameters they affect;
3. a proposal is a candidate change, not an in-place edit of a published model;
4. accepted calibration remains attributable to the operational facts that motivated it.

Final cross-track O7 integration additionally requires:

5. candidate changes flow through **Governance G2** `ChangeSet`/impact semantics rather than an operational duplicate;
6. conformance/finding integration uses **Governance G4** when policy requires it.

Synthetic ChangeSet/conformance fixtures may prove orchestration locally, but they do not close criteria 5-6 or satisfy G2/G4.

## 11. O8 — Operational resilience and recovery semantics

### Goal

Define failure behavior before a production adapter is trusted with consequential work.

The gate covers:

- adapter disconnect/reconnect;
- duplicate/reordered observations;
- command retry and deduplication;
- acknowledgement loss;
- partial target availability;
- restart/recovery of command correlation state;
- observation resynchronization;
- persistent unknown outcomes;
- trust/credential expiry or revocation while work is in flight;
- safe degradation when authority or telemetry is unavailable.

### Acceptance criteria

O8 is complete when failure-injection tests prove that:

1. restart does not lose the ability to correlate in-flight commands;
2. duplicate source messages do not silently duplicate operational facts under the selected identity policy;
3. recovery can resynchronize observations without pretending continuity that cannot be proven;
4. ambiguous external command outcomes survive restart as ambiguous;
5. production behavior fails according to documented safety policy rather than simulation-oriented convenience;
6. trust/credential loss cannot silently downgrade a consequential path into permissive behavior.

## 12. O9 — First live-system adapter proving ground

### Goal

Prove the architecture against one real integration profile without allowing that protocol to become the domain model.

The first adapter should be chosen for learning value, not market coverage. It must exercise at least:

- external identity mapping;
- verified peer/source/target trust appropriate to the consequentiality of the integration;
- observation ingestion;
- one controlled command or deployment-like action if safe and appropriate;
- effective applied-artifact/deployment provenance where transformation occurs;
- correlation and idempotency;
- reconnect/recovery;
- reconciliation;
- provenance into a Governance evidence use through **G5**.

A simulator or protocol test server may be used before any physical system is connected. That can prove protocol and operational-local behavior, but **O9 is not complete as a live-system proving ground** until:

1. the applicable Engine/Factory semantics are stable enough to define the command/observation meaning;
2. G1 supplies durable deployment revision identity when deployment is exercised;
3. G5 supplies the shared evidence-use integration;
4. all O2 production trust requirements relevant to the adapter are satisfied;
5. reconnect/recovery and ambiguous-outcome behavior have been failure-tested.

O9 is not complete merely because a client library can connect to a broker/server.

## 13. Cross-cutting persistence dependency

This track depends on a coherent persistence architecture for durable operational artifacts. Storage technology is not selected here, but the following require stable identities, history, retention, and compatibility:

- commands;
- acknowledgements/results;
- observations;
- deployment records and applied-artifact provenance;
- reconciliation outputs;
- drift/calibration proposals.

This should align with persistence for controlled revisions, runs, evaluations, findings, and evidence rather than introducing unrelated per-capability databases with incompatible identity rules.

## 14. Temporal semantics dependency

Before O5/O6 become public contracts, Arcogine must define cross-domain temporal vocabulary sufficient to distinguish:

- simulation time;
- external event/measurement time;
- observed-at time;
- received/ingested-at time;
- effective interval;
- recorded-at time;
- deployment time;
- processing wall-clock time.

Protocol timestamp fields should map into this vocabulary rather than define it.

## 15. Units and measurements dependency

External observations make implicit units unsafe. Before measured values become durable operational contracts, Arcogine should establish explicit units/dimensions, precision, and conversion semantics for industrial quantities.

This does not require a universal scientific-units framework in O1-O4, but O5 cannot treat a primitive `double` as a sufficient durable measurement contract.

## 16. Standards and adapter policy

This track gives concrete ownership to integrations already anticipated in the standards-alignment material.

Potential adapters include:

- OPC UA / IEC 62541;
- MQTT;
- Asset Administration Shell profiles;
- ERP/MES APIs;
- ISA-95/B2MML interchange;
- FMI/co-simulation where the boundary is genuinely model/execution exchange.

The policy is:

> **Define identity, trust, authority, command, observation, provenance, transformation, and reconciliation semantics first; implement protocol adapters over those contracts second.**

No readiness gate is satisfied merely by choosing or connecting a protocol.

## 17. Relationship to current Engine Readiness

This track must not absorb or block the existing Factory Simulation Engine Readiness gates.

Engine Readiness can continue independently because its job is to establish deterministic production semantics and a consumer-neutral simulation/runtime contract. Operational Execution consumes that semantic maturity later.

Operational prototypes may use fixture production semantics to test operational-local behavior, but:

- synthetic operational adapters must not be used as evidence that Engine Readiness gates are complete;
- operational-owned fixture types must not become a second production ontology;
- live O3/O9 integration waits on the relevant stable Engine/Factory semantics rather than freezing protocol-driven semantics first.

## 18. Relationship to Governance and Conformance

Governance G1-G9 remains a sibling track.

Expected integration points include:

- G1 durable fingerprint/controlled revision identity used by deployment and historical reconciliation;
- G2 ChangeSets used by calibration/change proposals;
- G4 conformance/findings used when drift/change policy requires them;
- G5 evidence-use relationships referencing operational observations without changing those observations' source identity;
- deployment and command results referenced during conformance/governed change;
- exceptions/risk acceptance constraining otherwise prohibited deployment or command paths.

There should be no generic "evaluation framework" introduced merely because reconciliation and conformance both classify results.

## 19. Initial implementation policy

Early slices should prefer:

- headless contracts;
- synthetic adapters;
- deterministic fixtures clearly scoped as fixtures;
- failure-injection tests;
- protocol-independent domain tests;
- explicit trust-boundary tests;
- explicit ADRs only when a hard-to-reverse contract is selected.

They should avoid:

- selecting infrastructure before semantics;
- coupling `FactoryRuntime` directly to PLC/MQTT/OPC UA clients;
- treating API authentication as the whole authority/trust model;
- authorizing a merely claimed identity as though it were verified;
- modeling a complete MES ontology preemptively;
- using Governance `Evidence` as the primary telemetry record;
- binding raw external observations to a model revision at ingestion;
- inventing operational-owned revision/ChangeSet/finding/evidence-use abstractions;
- treating latest observation as authoritative without source/freshness/trust policy.

## 20. Exit condition

This readiness track has served its purpose when Arcogine can demonstrate, headlessly and reproducibly, that one **Governance-identified controlled semantic revision** can be authorized for a real execution context, transformed/applied through an attributable adapter/profile, verified against the effective applied representation, observed through independently provenanced operational facts over a verified trust boundary, reconciled against modeled intent, recovered safely across realistic failures, and fed back into governed model improvement without violating the ownership boundaries above.
