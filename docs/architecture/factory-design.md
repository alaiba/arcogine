# Factory Design Architecture

> **Status:** Proposed architectural reference  
> **Scope:** Cross-consumer factory-design semantics and their boundary with scenario configuration and runtime behavior  
> **Authority:** Proposed architecture; current implementation remains documented by the architecture overview and product/reference docs until this direction is accepted and implemented  
> **Related:** [Product Charter](../product/charter.md), [Architecture Overview](overview.md), [ADR-0003](decisions/0003-canonical-factory-model-boundary.md), [ADR-0004](decisions/0004-model-identity-revision-lineage-and-external-change-control.md), [Governance and Conformance Architecture](governance-conformance.md), [Operational Execution and Digital Twin Architecture](operational-execution-digital-twin.md), [ISA-95 Semantic Mapping](isa-95-semantic-mapping.md), [Factory Design Capability Plan](../planning/factory-design-capability.md), [Factory Simulation Engine Readiness](../planning/factory-simulation-engine-readiness.md), [Operational Execution and Digital Twin Readiness](../planning/operational-execution-digital-twin-readiness.md)

## 1. Architectural position

Factory design is a cross-consumer Arcogine concern, but "factory design" covers three responsibilities that must remain separate:

1. **Designed production system** — products, operations, resources, policies, constraints, and behaviorally relevant geometry.
2. **Design lifecycle** — drafting, validation, publication, versioning, comparison, provenance, and eventual deployment of changes.
3. **Design experience** — UI, interaction model, visualization, undo gestures, camera, palettes, game tutorials, forms, import tools, or agent workflows.

The proposed architectural rule is:

> **Arcogine owns the semantics and publication of production-system designs. Consumers may own how drafts are authored. Runtime contexts instantiate immutable published model versions and never double as the design workspace.**

This follows the Product Charter's lifecycle-continuity thesis without implying one UI, one runtime, or one mutable state store for every mode.

## 2. Orthogonal lifecycle, shared ontology

Factory design is **operationally orthogonal** to simulation/runtime behavior but **semantically inseparable** from it.

It is orthogonal because:

- design can occur without running a simulation;
- runtime can instantiate a published model without an editor;
- design changes are ordered by model revision and provenance, not simulated time;
- drafts may be incomplete or temporarily invalid while executable runtime models may not;
- design review/publication and runtime event processing have different authority and lifecycle rules.

It is semantically inseparable because:

- design and runtime must share product, operation, resource, capability, policy, constraint, and behaviorally relevant layout semantics;
- validation must reflect what runtime can actually execute;
- consumers must not manually translate an editor-specific ontology into unrelated runtime semantics;
- every runtime or verification context must identify the exact model version it instantiated.

```text
Consumer-specific design experience
                 |
                 v
            Design draft
      mutable, possibly invalid
                 |
          validate / publish
                 v
       Factory model version
      immutable and identified
                 |
             instantiate
                 v
        Factory runtime state
        mutable in its context
```

## 3. Scenario, model, and runtime are different things

The proposed model boundary distinguishes an experiment from the production system being experimented on and from mutable execution state.

```text
Scenario
    describes an experiment or execution context

FactoryModel
    describes the production system

FactoryModelVersion
    immutable published identity of that production system

ExecutableFactoryModel
    validated/resolved representation derived from one model version

FactoryRuntime
    mutable state instantiated from that executable model
```

The current `ScenarioConfig` combines simulation parameters, equipment/material/process definitions, economy configuration, and agent configuration. That remains useful as an input envelope, but it should not become the canonical factory model merely because those concerns are serialized together today.

Conceptually:

```text
Scenario / TOML -------+
Game design -----------|
Industrial design UI --|
Optimizer -------------+--> FactoryModel --> publish --> FactoryModelVersion
Importer / adapter ----|
CLI / test builder ----+
```

A scenario may contain or reference a published model together with runtime inputs such as workload and simulation settings.

### 3.1 Concern classification

| Concern | Proposed ownership |
|---|---|
| Product definitions | `FactoryModel` |
| Operation definitions | `FactoryModel` |
| Resource definitions and capabilities | `FactoryModel` |
| Installed resource instances | `FactoryModel` |
| Capability/eligibility requirements | `FactoryModel` |
| Semantic layout | `FactoryModel` |
| Simulation seed/limits | Scenario/runtime context |
| Production workload | Scenario/runtime input |
| Economy configuration | Scenario/context |
| Agent configuration | Scenario/context |
| Production orders | Runtime |
| Work items | Runtime |
| Queues and assignments | Runtime |
| Transfers in progress | Runtime |
| Simulated time/events | Runtime |
| KPIs/performance observations | Runtime observation |

## 4. Canonical model boundary

The exact Java types are intentionally not fixed yet, but the lifecycle responsibilities are.

```text
FactoryDraft
    mutable consumer authoring state
    may be incomplete or invalid

FactoryModel
    complete semantic production-system definition

FactoryModelVersion
    immutable published semantic snapshot
    deterministic content-derived fingerprint

ExecutableFactoryModel
    validated/resolved runtime-ready derivative

FactoryRuntime
    mutable state instantiated from one model version
```

These need not all become separate persistence entities or modules initially.

Today's `FactoryModelVersion` is an immutable *validated semantic snapshot*: publishing it proves the design is executable and gives it a deterministic content hash under the current provisional identity policy. It is not yet a **controlled revision** entity — there is no persistent revision repository, lineage, approval state, or external change reference. [ADR-0004](decisions/0004-model-identity-revision-lineage-and-external-change-control.md) draws that distinction; see section 11 below for what the fingerprint does and does not carry.

### 4.1 What belongs in the canonical model

A fact belongs in the canonical model when changing it changes the executable meaning of the production system across consumers or lifecycle modes. Examples include:

- product/material definitions;
- operations/work definitions;
- resource definitions and capabilities;
- installed resource instances;
- resource-pool/work-center membership when it has real scheduling, capacity, responsibility, or reporting semantics;
- processing/setup/transfer/dispatch policies when part of the designed system;
- executable constraints;
- shared verification objectives when modeled explicitly;
- behaviorally relevant floor geometry, position, orientation, footprint, connection points, zones, or transfer relationships.

### 4.2 What does not belong in the canonical model

Consumer presentation and transient authoring state remain consumer-owned unless a concrete cross-consumer requirement justifies promotion. Examples include:

- selection/hover state;
- editor camera;
- drag preview and snapping guides;
- sprites, meshes, animation offsets, sound, colors, themes;
- game tutorial state, unlocks, score, stars, and player currency;
- local undo/redo stacks;
- decorative objects with no production semantics.

## 5. Derived executable representation is not a second model

A runtime may compile or resolve immutable structures for efficiency:

- resolved definition references;
- capability/eligible-resource indexes;
- operation/routing indexes;
- geometry/spatial indexes;
- validated transfer relationships;
- derived scheduling metadata.

These structures are derived from exactly one published semantic model. They are not independently authored sources of truth.

The invariant is:

> **There is no independently authored runtime factory model. Runtime indexes and compiled structures are derivations of one published `FactoryModelVersion`, not another representation that consumers must maintain manually.**

## 6. Design revision is not simulated time

Design changes and simulation events must not share one event stream merely because both are changes.

Simulation events are facts in simulated time:

```text
OrderReleased at tick 10
OperationStarted at tick 14
TransferCompleted at tick 19
```

Design changes belong to a model-revision context:

```text
ResourcePlaced in draft revision 12
OperationRequirementChanged in revision 13
FactoryModelPublished as version 4
```

| Design lifecycle | Runtime lifecycle |
|---|---|
| model/draft revision | event sequence |
| author or decision source | event source |
| wall-clock provenance | simulated time |
| validation/publication state | runtime fact |
| branch/review context | run/session context |
| published model version | instantiated source model version |

A future real-operations workflow connects design to governance and deployment explicitly:

```text
Candidate change
        ↓
Controlled revision (Governance; persisted, with lineage)
        ↓
Technical assessment (validation / simulation / verification)
        ↓
Authorization
        ↓
Operational deployment plan / target application
        ↓
Observed result / reconciliation
```

The revision is persisted before authorization, not after: an unauthorized revision, or one authorized but never deployed, must remain representable. Authorization and deployment are separate records that reference the revision, not steps a revision passes through to come into existence.

Factory Design owns the semantic design and publication boundary. Governance owns controlled revision/change-control and authorization interpretation. [Operational Execution and Digital Twin](operational-execution-digital-twin.md) owns deployment targeting/application, effective transformed/applied-artifact provenance, resulting operational facts, and reconciliation. That bridge preserves provenance and authority; it does not turn editor operations into simulation or production-control events.

Authorization may be owned externally: Arcogine can produce the technical assessment evidence a candidate needs (validation results, semantic diff, simulation/verification outcomes) without itself hosting the request/review/approval workflow. See [ADR-0004](decisions/0004-model-identity-revision-lineage-and-external-change-control.md), section 11.1 below, and the operational architecture.

## 7. Cross-consumer ownership

| Concern | Ownership |
|---|---|
| Product and operation definitions | Arcogine canonical model |
| Resource definitions and capabilities | Arcogine canonical model |
| Installed resource instances | Arcogine canonical model |
| Semantic position/footprint when behavior depends on them | Arcogine canonical model |
| Structured executability validation | Shared Arcogine model/design capability |
| Semantic model identity (fingerprint) | Shared Arcogine model infrastructure; durable fingerprint policy completed by Governance G1 |
| Controlled revision lifecycle and lineage | Cross-domain Governance and Conformance capability (G1) — see [ADR-0004](decisions/0004-model-identity-revision-lineage-and-external-change-control.md) and the [Governance and Conformance Capability Plan](../planning/governance-conformance-capability.md) |
| Change request/review/authorization workflow | Cross-domain Governance and Conformance capability (G6), or an external change-management system referenced not depended on |
| Operational deployment target/application and effective applied-artifact provenance | [Operational Execution and Digital Twin](operational-execution-digital-twin.md) |
| External operational observations and modeled-versus-observed reconciliation | [Operational Execution and Digital Twin](operational-execution-digital-twin.md) |
| Model publication and runtime instantiation | Shared Arcogine boundary |
| Runtime dispatch/queues/work/transfers | Arcogine runtime |
| Semantic model comparison/diff | Cross-consumer candidate; share when justified |
| Draft persistence/branching/merge/collaboration | Cross-consumer candidate; defer until justified |
| Editor selection/camera/drag/drop/palette | Consumer |
| Visual assets and presentation | Consumer |
| Game budget/unlocks/score/progression | Game consumer |
| Industrial engineering constraints | Arcogine model or verification capability when semantically supported |
| Throughput/lead-time objectives | Shared verification capability when supported |

## 8. Spatial layout and resource hierarchy are independent

Resource/organizational scope may eventually resemble:

```text
Factory
  Work Center
    Resource Pool
      Resource Instance
```

Spatial layout is a separate dimension:

```text
Factory Floor
  Position
  Orientation
  Footprint
  Connection points
  Transfer relationships
```

Hierarchy supports containment, responsibility, capacity aggregation, scheduling scope, reporting, or authorization. Spatial layout supports physical placement and transport consequences.

A resource may move without changing identity or hierarchy membership. A resource may change resource-pool membership without moving physically.

The [ISA-95 semantic mapping](isa-95-semantic-mapping.md) remains the reference for whether hierarchy concepts should be adopted, aliased, extended, or deferred.

## 9. Semantic geometry versus presentation geometry

Arcogine owns geometry only when it changes executable behavior or shared validation.

Semantic geometry may include floor dimensions, resource position/orientation/footprint, connection points, zones/restrictions, adjacency/reachability constraints, and transfer-distance inputs.

Consumers own meshes/sprites, animation anchors, camera framing, selection outlines, label placement, decorative composition, and visual interpolation between authoritative engine states.

## 10. Constraint ownership

Every design rule should be classified before implementation.

### 10.1 Executability constraints — Arcogine

These answer whether the production system can be published/instantiated coherently:

```text
Referenced definitions exist
Identifiers are unique
Operation graph is valid
Each operation can resolve eligible capacity
Semantic footprint lies inside the floor
Forbidden semantic footprints do not overlap
Required transfers are representable
```

### 10.2 Verification objectives — shared verification concern

These test whether an executable design meets a target:

```text
Throughput >= target
Lead time <= limit
Maximum utilization <= threshold
Safety separation >= required distance
```

An objective is not automatically an executability invariant.

### 10.3 Consumer rules — consumer

These apply only to one experience:

```text
Player construction budget
Unlock level
Star-rating threshold
Tutorial restriction
Aesthetic preference
```

## 11. Publication, identity, and provenance

A published model version is the bridge between design and downstream contexts.

[ADR-0004](decisions/0004-model-identity-revision-lineage-and-external-change-control.md) separates two concepts that publication identity must not bundle together:

- **Semantic fingerprint** — a deterministic identity derived from canonical model content. Equivalent canonical facts produce equivalent fingerprints, independent of consumer presentation metadata, authorship, or timing. This is the minimum publication identity required today.
- **Controlled revision** — a persisted, controlled historical configuration state carrying change-control provenance and lineage back to a prior revision, and optionally a stable external change reference (for example, an issue-tracker key). Authorization and deployment are separate lifecycle/evidence records that may reference a revision; a revision need not be authorized or deployed to exist. This is deferred capability, not part of today's publication boundary.

Every runtime or verification result must retain the identity of the model version it instantiated, expressed as the fingerprint.

The desired invariant is:

> Given a published model version, execution context, seed, and ordered commands, Arcogine can identify exactly which semantic design produced the resulting events and observations.

Persistent lineage, authorship, approvals, branches, and change sets should be added only when concrete collaboration or deployment workflows require them.

### 11.1 External change-management and deployment integration

Arcogine does not require organizational change-management workflow to live inside the factory domain.

A controlled model revision may reference an external change record, such as a Jira issue. Arcogine remains authoritative for the model, semantic diff, technical assessments, and resulting revision; the external system remains authoritative for request/review/authorization workflow unless that responsibility is explicitly brought into Arcogine.

Conformance/verification assessments, authorization decisions, simulation runs, and operational deployments remain separate artifacts from the model and from each other. They may reference a fingerprint and, once it exists, a controlled revision, but none of them is the model. The operational deployment record additionally owns target, adapter/profile/transformation, effective applied-artifact/version, apply/verification result, and reconciliation provenance; Factory Design must not duplicate those mechanics.

## 12. Shared validation and publication boundary

Validation belongs at the shared model/design boundary, not independently in each consumer.

A consumer may perform optimistic local checks for responsiveness, but Arcogine remains authoritative for shared executability semantics.

Validation should be deterministic, structured, attributable to fields/entities when possible, explicit about severity, and atomic with respect to publication/instantiation.

The initial shared substrate should include:

1. a canonical factory model contract;
2. structured validation;
3. an explicit publication boundary producing an immutable model version;
4. deterministic runtime instantiation from that version;
5. runtime provenance linking runs to model versions.

The first consumer may still own mutable drafts, undo history, and editor persistence.

## 13. Architectural dependency direction

```text
Consumer authoring sources
          |
          v
Canonical factory model
 definitions + validation
          |
     publish version
          |
     +------------+-------------+
     |            |             |
     v            v             v
Simulation    Verification   Governance
runtime       contexts       revision/change
                                |
                                v
                         Operational deployment
                                |
                                v
                         External observations
                                |
                                v
                           Reconciliation
```

The canonical model must not depend on a specific UI, simulation transport, industrial protocol, or deployment target. Runtime must not mutate the published model. Operational deployment references an authorized governed revision rather than becoming part of model publication. Consumers must not depend on mutable runtime internals to author a design.

This may initially be implemented inside existing modules/packages. A new Gradle module is warranted only when dependency direction or ownership invariants justify one.

## 14. Review checklist

When factory-design semantics change, ask:

1. Is this production-system definition, design lifecycle, runtime state, verification state, operational deployment/reconciliation state, or one consumer's experience?
2. Does changing it alter executable behavior across consumers?
3. Is it part of the scenario/run context rather than the factory itself?
4. Is the fact ordered by model revision or simulated time?
5. Can a draft be invalid while a published model must reject that state?
6. Is this resource hierarchy or spatial layout?
7. Is the rule executability, verification, or consumer-specific?
8. Can a runtime result identify the exact published model version that produced it?
9. Are runtime structures derived from that model, or are we creating a second authored representation?
10. Is a new shared design abstraction justified by a concrete cross-consumer workflow?
11. Are deployment targeting/application, external observation ingestion, or reconciliation semantics being placed in Factory Design even though they belong to Operational Execution?

## 15. Triggers for revisiting this proposal

Revisit this document when Arcogine introduces or materially changes:

- a canonical `FactoryModel` or equivalent;
- the scenario-to-model adapter boundary;
- model versioning/hashes/lineage;
- shared draft/design services;
- semantic model diff/compare;
- design branching/merge/collaboration;
- deployment of model changes to real operations;
- behaviorally relevant spatial geometry;
- work-center/resource-pool hierarchy;
- cross-consumer validation rules;
- another independent design consumer;
- ISA-95/AAS/other model import/export.

Hard-to-reverse decisions should be recorded as ADRs. Once this architecture is implemented and accepted, authoritative current-state portions should move into or be reconciled with [`overview.md`](overview.md) rather than leaving proposed behavior presented as current fact.