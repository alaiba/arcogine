# Operational Execution and Digital Twin Architecture

> **Status:** Proposed architectural reference  
> **Scope:** Operational execution, external observations, digital-twin reconciliation, and design-to-reality continuity  
> **Authority:** Proposed architecture; this document does not describe current production capability  
> **Related:** [Product Charter](../product/charter.md), [Architecture Overview](overview.md), [Factory Design Architecture](factory-design.md), [Governance and Conformance Architecture](governance-conformance.md), [Standards Alignment](standards-alignment.md), [ADR-0004](decisions/0004-model-identity-revision-lineage-and-external-change-control.md), [Operational Execution and Digital Twin Readiness](../planning/operational-execution-digital-twin-readiness.md)

## 1. Architectural position

Arcogine's product thesis requires continuity from designed intent to operational reality without collapsing simulation, production control, external observations, and governance into one mutable runtime.

The proposed architectural rule is:

> **Arcogine reuses production semantics across lifecycle contexts. Consequential Arcogine interpretations, commands, deployments, and reconciliation results must identify their execution context, authority, provenance, and the semantic model/revision relationship that informed them when one exists. Raw external observations retain independent source and time provenance and must not be forced to identify an Arcogine controlled revision at ingestion.**

This capability is a sibling of Factory Design / Engine Readiness and Governance / Conformance:

- **Factory Design / Engine Readiness** owns production semantics and deterministic runtime truth.
- **Operational Execution / Digital Twin** owns real execution context, command/result lifecycle, deployment application, external observations, operational trust boundaries, and modeled-versus-observed reconciliation.
- **Governance / Conformance** owns controlled revision lineage, requirements/assertions, evidence use, findings, exceptions, and governed-change interpretation.

The current implementation is simulation-first and does not execute real-world commands. Nothing in this document changes that current-state claim.

## 2. Execution contexts are first-class

A run, command, deployment, reconciliation result, and externally relevant interpretation must identify the context in which it exists. Raw external observations identify their source context or environment when known, but that context must not be fabricated when the source does not provide one.

The initial conceptual Arcogine vocabulary is:

```text
SIMULATION
REPLAY
TEST
STAGING
PRODUCTION
```

The exact type names are not fixed here, but context identity must not be inferred only from process location, URL, deployment namespace, or caller convention.

Execution context affects:

- authorization requirements;
- allowed commands;
- audit expectations;
- failure handling;
- external-system authority;
- retention and provenance;
- safety controls.

A command that is harmless in `SIMULATION` may be prohibited, approval-gated, or fail-safe in `PRODUCTION`.

## 3. Modeled state, observed reality, and reconciled twin state are different things

Arcogine must preserve three distinct concepts:

```text
Modeled intent/state
    what the selected semantic model and, when available,
    controlled revision say should exist

Observed external reality
    immutable facts reported by external systems or measurements
    with independent source/time provenance

Reconciled twin interpretation
    Arcogine's current interpretation of reality after considering model,
    observations, authority, freshness, confidence, and discrepancies
```

An observation does not automatically overwrite modeled state. A model does not automatically override an external authoritative fact. Reconciliation is an explicit domain responsibility.

A reconciled twin may therefore represent:

- agreement between model and observation;
- stale or missing observations;
- conflicting external authorities;
- known divergence;
- inferred state with confidence;
- a pending commanded transition that is not yet observed as complete.

The model/revision relationship belongs naturally to the **reconciliation result** or another interpretation/evidence-use record because that is where Arcogine decides which model semantics were applied to independent external facts.

## 4. Identity, trust, authority, and capability

Operational execution requires a common actor and peer boundary broader than interactive login.

Conceptually, an actor may be:

```text
Human
Agent
Service
ExternalSystem
```

Every consequential request must be attributable to an actor identity and evaluated against explicit capability/scope in the target execution context.

For a production-consequential boundary, identity is not sufficient by itself. Arcogine must distinguish **claimed identity** from **verified identity** and must establish an explicit trust basis for consequential peers, sources, and targets.

Before production actuation or authoritative production observation is considered mature, the boundary must provide semantics for:

- peer/source/target identity verification;
- integrity and authenticity of consequential command and observation paths;
- credential/secret lifecycle sufficient for the selected deployment model;
- least-privilege capability/scope;
- trust-boundary and threat assumptions;
- revocation/expiry or equivalent loss-of-trust behavior;
- fail-safe treatment of unverifiable or integrity-failed inputs.

Authentication mechanism, identity provider, certificate scheme, protocol security profile, and policy engine remain implementation choices. The architectural requirement is that authorization is performed over a sufficiently verified identity/trust context rather than being implied by network reachability or possession of an endpoint.

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

The exact state machine may vary by adapter, but the following concerns are mandatory before production actuation is considered mature:

- stable command identity and correlation;
- idempotency semantics;
- target identity and verified trust context;
- actor and authority provenance;
- requested versus effective values;
- source semantic fingerprint / controlled revision when the command is derived from one;
- submission and acknowledgement timestamps;
- timeout and retry rules;
- partial success/failure representation;
- cancellation or compensation where meaningful;
- resulting external facts and reconciliation status.

An accepted command is not proof that reality changed. A successful adapter call is not automatically proof that the physical system reached the requested state.

## 6. Deployment is distinct from publication and approval

Factory Design publishes semantic model versions. Governance owns controlled revision lineage and may associate revisions with authorization decisions. Operational Execution applies an authorized semantic state to a target execution context.

```text
Factory model version
        ↓
Controlled revision
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

Deployment records must remain separate from the model and from approval records. A revision may exist without being approved; it may be approved without being deployed; it may be deployed to staging but not production; different targets may be at different revisions.

A deployment record must be able to answer not only **which source revision was intended**, but also **what effective representation was applied**. Where transformation or rendering occurs, deployment provenance must bind:

- source semantic fingerprint and controlled revision ID when available;
- target identity and execution context;
- adapter/profile/mapping/transformation identity and version;
- transformation/tool version when materially relevant;
- rendered/applied artifact fingerprint when Arcogine materializes one; or
- authoritative external applied-version/reference when the target owns the rendered form;
- authorization, application acknowledgement, verification result, and rollback reference.

The same source revision passed through different mapping/profile versions can yield materially different target configuration. Source revision alone is therefore insufficient deployment provenance.

## 7. External observations have independent provenance

An external observation is an operational fact, not merely a governance evidence attachment and not intrinsically a fact about one Arcogine revision.

A durable observation contract should be able to identify:

- observation identity;
- source system and source identity;
- source trust/authenticity status or verification provenance where required;
- observed entity or semantic subject;
- observed value/fact;
- unit/dimension where applicable;
- source event/measurement time;
- ingestion/receipt time;
- quality, confidence, or validity metadata where available;
- correlation to command/deployment/run when known;
- raw-source reference when retention policy permits.

A raw observation **does not require** an Arcogine model fingerprint or controlled revision. If Arcogine later interprets that observation against a model, the binding belongs to the interpretation, reconciliation result, or Governance `EvidenceUse` relationship.

Transport protocols such as OPC UA or MQTT are adapters over this semantic boundary; they are not the domain model themselves.

## 8. Reconciliation owns modeled-versus-observed divergence

Reconciliation compares what Arcogine expects with what authoritative observations indicate.

It should classify discrepancies such as:

```text
MATCH
PENDING
STALE
MISSING
CONFLICT
DIVERGED
UNKNOWN
```

These names are illustrative, not accepted enums.

A reconciliation result should be historically attributable to:

- the exact semantic fingerprint / controlled revision interpreted, when applicable;
- the observations considered;
- source authority/trust decisions;
- the reconciliation policy/version;
- relevant pending commands/deployments;
- the temporal frame used.

Reconciliation must account for temporal semantics. At minimum Arcogine must distinguish concepts equivalent to:

- simulation time;
- source/event time;
- observed-at time;
- received/ingested-at time;
- effective-from/effective-until time;
- recorded-at time;
- deployment time;
- wall-clock processing time.

The twin must not silently compare facts from incompatible temporal frames.

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

Calibration may propose parameter changes, but it must not mutate a published model or production target outside the normal publication, governance, and deployment boundaries.

Operational drift analysis may produce a candidate semantic change, but Governance owns the durable `ChangeSet`, conformance, finding, and controlled-revision semantics used to govern that candidate.

## 10. Boundary with Governance and Conformance

Operational Execution owns acquisition and provenance of operational facts. Governance consumes those facts as evidence when evaluating requirements or governed change.

The invariant is:

> **An external observation is not created as evidence for one revision. It is an operational fact with independent provenance; Governance may later reference it through an evidence-use relationship that binds the observation to a particular evaluation, scope, fingerprint, or controlled revision when applicable.**

Operational Execution owns:

- external observations;
- command lifecycle facts;
- deployment application/result facts;
- reconciliation state and divergence;
- trust/authenticity facts at operational integration boundaries.

Governance / Conformance owns:

- durable semantic fingerprint policy and controlled revision lineage;
- semantic ChangeSets;
- requirements/assertions;
- conformance evaluation;
- evidence use;
- findings;
- exceptions and risk acceptance;
- governed-change interpretation and audit projections.

Operational Execution must reference Governance-owned identities/contracts when they exist rather than introducing duplicate revision, ChangeSet, evidence-use, or finding types.

## 11. Boundary with Factory Design and Engine Readiness

Factory Design / Engine Readiness remains authoritative for executable production semantics and deterministic simulated execution.

Operational Execution does not turn `FactoryRuntime` into a production-control runtime by default.

Engine concepts such as workload, dispatch, queues, operations, observations, and spatial consequences may inform shared semantic contracts. Production actuation additionally requires verified identity/trust, authorization, command acknowledgement, external-system failures, deployment targeting, operational observation provenance, and reconciliation.

The architectural dependency is therefore:

```text
Canonical factory semantics
        |
        +--> deterministic simulation / verification
        |
        +--> operational deployment / execution adapters
                         |
                         v
                 external observations
                         |
                         v
                    reconciliation
```

Shared semantics do not imply shared mutable runtime state or identical lifecycle machinery.

## 12. Integration adapter boundary

Industrial adapters should sit behind a common semantic integration contract that identifies:

- external identity mapping and mapping version;
- direction: observation, command, or bidirectional;
- authority and trust expectations for the external source/target;
- transport/profile and security profile where relevant;
- lossiness or transformation rules;
- mapping/transformation/tool version;
- retry/idempotency behavior;
- provenance;
- compatibility/version expectations.

Candidate protocols and standards include OPC UA, MQTT, Asset Administration Shell profiles, ERP/MES interfaces, FMI/co-simulation boundaries, and ISA-95/B2MML-style interchange where justified.

No protocol should become the canonical Arcogine domain model.

## 13. Persistence and history

Operational execution creates durable artifacts whose historical identity matters:

- execution contexts;
- verified actor/peer identities and authority decisions where retention is appropriate;
- commands and acknowledgements;
- external observations;
- deployment plans/results and effective applied-artifact provenance;
- reconciliation records;
- drift/calibration proposals.

The exact storage technology is deliberately unspecified, but these artifacts require stable IDs, retention semantics, compatibility rules, and queryable provenance. They should align with the broader cross-cutting persistence architecture used for model revisions, runs, evaluations, findings, and evidence.

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

These are architecture requirements, not claims that current Arcogine implements production-grade safety controls.

## 15. Cross-track dependency rule

This track may develop operational-owned contracts headlessly and in parallel, but it must not complete sibling-owned semantics by inventing substitutes.

In particular:

- durable fingerprint and controlled revision identity come from Governance G1;
- semantic `ChangeSet` and impact semantics come from Governance G2;
- conformance/finding semantics come from Governance G4;
- evidence-use semantics come from Governance G5;
- production semantics and consumer-neutral runtime contracts come from the relevant Engine Readiness gates.

Operational-owned synthetic fixtures may stand in for these inputs to prove local behavior, but such fixtures do not satisfy or redefine sibling-track gates.

The detailed gate/fixture policy lives in the [Operational Execution and Digital Twin Readiness](../planning/operational-execution-digital-twin-readiness.md) plan.

## 16. Non-goals for the first readiness track

The first track does not require:

- choosing an identity provider or authentication mechanism;
- implementing generic enterprise RBAC;
- choosing OPC UA versus MQTT as the universal transport;
- building a full MES or SCADA platform;
- replacing PLC/device safety logic;
- implementing every ISA-95 object;
- building a universal workflow engine;
- making the simulation scheduler a wall-clock production scheduler;
- collapsing Governance, runtime observations, and twin reconciliation into one generic evaluation framework.

## 17. ADR triggers

Create ADRs when implementation commits Arcogine to hard-to-reverse choices such as:

- execution-context identity semantics;
- common actor/capability/trust model;
- command correlation/idempotency contract;
- operational observation envelope and timestamp semantics;
- deployment target/deployment-record and applied-artifact provenance model;
- reconciliation authority and conflict-resolution rules;
- persistence/compatibility contract for operational facts.

Creating this readiness track by itself does not require an ADR.
