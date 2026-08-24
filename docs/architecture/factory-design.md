# Factory Design Architecture

> **Status:** Maintained architectural reference  
> **Scope:** Cross-consumer factory-design semantics and their boundary with simulation/runtime behavior  
> **Authority:** Describes the intended architectural separation between design artifacts, published executable models, runtime state, and consumer-specific authoring experiences  
> **Related:** [Product Charter](../product/charter.md), [Architecture Overview](overview.md), [ISA-95 Semantic Mapping](isa-95-semantic-mapping.md), [Factory Design Capability Plan](../planning/factory-design-capability.md), [Factory Simulation Engine Readiness](../planning/factory-simulation-engine-readiness.md)

## 1. Architectural position

Factory design is a cross-consumer Arcogine concern, but the phrase "factory design" covers three different responsibilities that must remain separate:

1. **The designed production system** — the semantic model of products, operations, resources, policies, constraints, and behaviorally relevant geometry.
2. **The design lifecycle** — drafting, validation, versioning, publication, comparison, provenance, and eventual deployment of model changes.
3. **The design experience** — the UI, interaction model, visualization, undo gestures, camera, palettes, game tutorials, forms, import tools, or agent workflows through which a consumer authors a design.

The architectural rule is:

> **Arcogine owns the semantics and publication of production-system designs. Consumers may own how drafts are authored. The simulation runtime instantiates immutable published model versions and never doubles as the design workspace.**

This follows the Product Charter's central thesis: the system that is designed should remain semantically continuous with the system that is simulated, verified, deployed, executed, and monitored. It does not imply one UI, one runtime, or one mutable state store for every lifecycle stage.

## 2. Orthogonal lifecycle, shared ontology

Factory design is **operationally orthogonal** to the simulation runtime but **semantically inseparable** from it.

It is orthogonal because:

- a design can be authored without running a simulation;
- a simulation can instantiate a published model without a design editor being present;
- design changes are ordered by model revision and provenance, not by simulated time;
- drafts may be incomplete or temporarily invalid while executable runtime models may not;
- design collaboration, review, approval, and publication have different authority and lifecycle rules from simulated production events.

It is semantically inseparable because:

- design and runtime must share the same product, operation, resource, capability, policy, constraint, and behaviorally relevant layout semantics;
- simulation validation must reflect what the runtime can actually execute;
- consumers must not translate an editor-specific schema manually into an unrelated runtime representation;
- every simulation, verification, or future execution context must identify the exact model version it instantiated.

The intended shape is:

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

## 3. Canonical model boundary

The semantic output of design is a canonical production-system model. The exact Java types are not fixed by this document, but the lifecycle distinctions are.

Conceptually:

```text
FactoryDraft
    mutable authoring state
    may be incomplete or invalid

FactoryModel
    complete semantic definition

FactoryModelVersion
    immutable published model
    model identity, version, provenance, content hash

ExecutableFactoryModel
    validated/resolved runtime-ready representation

FactoryRuntime
    mutable state instantiated from one published model version
```

These need not all be separate classes or persistence entities initially. They represent different responsibilities and invariants that implementation must not collapse accidentally.

### 3.1 What belongs in the canonical model

A fact belongs in the canonical design model when it changes the executable meaning of the production system across consumers or lifecycle modes. Examples include:

- product/material definitions;
- operations or work definitions;
- resource definitions and capabilities;
- installed resource instances;
- resource-pool or work-center membership when it affects scheduling, capacity, responsibility, or reporting;
- processing, setup, transfer, or dispatch policies;
- production constraints;
- objectives intended for shared verification;
- behaviorally relevant floor geometry, position, orientation, footprint, connection points, zones, or transfer relationships.

### 3.2 What does not belong in the canonical model

Consumer presentation and transient authoring state remain consumer-owned unless a concrete cross-consumer need justifies promotion. Examples include:

- selection and hover state;
- editor camera position;
- drag preview state;
- UI snapping guides;
- sprites, 3D meshes, animation offsets, sound, colors, visual themes;
- game tutorial state, unlocks, scores, stars, and player currency;
- local undo/redo stacks;
- decorative objects with no production semantics.

## 4. Design revision is not simulated time

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

The metadata differs:

| Design lifecycle | Simulation runtime |
|---|---|
| model/draft revision | event sequence |
| author or decision source | event source |
| wall-clock provenance | simulated time |
| validation/publication state | runtime fact |
| branch/review context | run/session context |
| published model version | instantiated model version |

A future deployment workflow may connect the two lifecycles explicitly:

```text
Design change set
        ↓
Validation / verification
        ↓
Approval
        ↓
Deployment request
        ↓
Accepted operational change
```

That bridge must preserve provenance and authority; it does not make editor operations into simulation events.

## 5. Cross-consumer ownership

The following ownership rules apply across consumer types such as a game, industrial design UI, optimizer, CLI, import adapter, or autonomous design agent.

| Concern | Ownership |
|---|---|
| Product and operation definitions | Arcogine canonical model |
| Resource definitions and capabilities | Arcogine canonical model |
| Installed resource instances | Arcogine canonical model |
| Semantic position/footprint when behavior depends on them | Arcogine canonical model |
| Transfer and dispatch policies | Arcogine canonical model/runtime policy |
| Structured executability validation | Shared Arcogine model/design capability |
| Model identity, version, hash, and lineage | Shared Arcogine model infrastructure |
| Model publication and runtime instantiation | Shared Arcogine boundary |
| Semantic model comparison/diff | Cross-consumer candidate; shared when justified |
| Draft persistence, branching, merge, collaboration | Cross-consumer candidate; defer until justified |
| Editor selection, hover, camera, drag/drop, palette | Consumer |
| Visual assets and presentation | Consumer |
| Game construction budget, unlocks, score, progression | Game consumer |
| Industrial safety/engineering constraints | Arcogine model or verification capability when semantically supported |
| Throughput/lead-time verification objectives | Shared verification capability when supported |

## 6. Spatial layout and resource hierarchy are independent dimensions

Factory design introduces spatial semantics, but spatial layout must not be confused with ISA-95-style equipment/resource hierarchy.

Resource and organizational scope may eventually resemble:

```text
Factory
  Work Center
    Resource Pool
      Resource Instance
```

Spatial layout is a different dimension:

```text
Factory Floor
  Position
  Orientation
  Footprint
  Connection points
  Transfer relationships
```

Hierarchy supports concepts such as containment, responsibility, capacity aggregation, scheduling scope, reporting, or authorization. Spatial layout supports physical placement and transport consequences.

A resource may move without changing identity or hierarchy membership. A resource may change pool/work-center membership without moving physically.

The [ISA-95 semantic mapping](isa-95-semantic-mapping.md) remains the reference for whether a hierarchy concept should be adopted, aliased, extended, or deferred.

## 7. Semantic geometry versus presentation geometry

Geometry is Arcogine-owned only when it changes executable behavior or shared validation.

### Semantic geometry

Potential model facts include:

- floor dimensions;
- resource position and orientation;
- resource footprint;
- semantic connection/hand-off points;
- zones or restrictions;
- adjacency or reachability constraints;
- transfer distance or routing inputs.

The runtime may derive consequences such as:

- transfer duration;
- material-flow feasibility;
- capacity restrictions;
- adjacency-dependent behavior.

### Presentation geometry

Consumers own:

- meshes/sprites;
- animation anchors and offsets;
- camera framing;
- selection outlines;
- label placement;
- decorative composition;
- visual interpolation between authoritative engine states.

## 8. Constraint ownership

Not every design rule has the same architectural status.

### 8.1 Executability constraints — Arcogine model/runtime boundary

These answer whether the production system can be instantiated and executed:

```text
Referenced definitions exist
Identifiers are unique
Operation graph is valid
Each operation can resolve eligible capacity
Semantic footprint lies inside the floor
Semantic footprints do not overlap where overlap is forbidden
Required transfers are representable
```

### 8.2 Verification/business objectives — shared verification concern

These answer whether an executable design satisfies a target:

```text
Throughput >= target
Lead time <= limit
Maximum utilization <= threshold
Safety separation >= required distance
```

An objective is not necessarily an executability invariant.

### 8.3 Consumer rules — consumer concern

These answer whether a design is acceptable within one experience:

```text
Player construction budget
Unlock level
Star-rating threshold
Tutorial restriction
Aesthetic preference
```

Consumers may pre-check these rules, but they must not be promoted into Arcogine's executable semantics unless they represent a cross-consumer business constraint.

## 9. Publication and provenance

A published model version is the bridge between design and downstream contexts.

At minimum, a published version should be attributable by:

```text
model ID
model version or revision
content hash
schema/model version
creation/publication provenance
```

Every simulation or verification run must retain the identity of the model version it instantiated.

The desired invariant is:

> Given a published model version, execution context, seed, and ordered commands, Arcogine can identify exactly which semantic design produced the resulting events and observations.

The design lifecycle may later add authorship, approvals, change sets, parent revisions, and branch lineage. Those should be added when collaboration or deployment creates a concrete need rather than speculatively.

## 10. Model validation and compilation

Validation belongs at the shared model/design boundary, not independently in each consumer.

A consumer may perform optimistic local checks for responsiveness, but Arcogine remains authoritative for executable semantics.

Validation should be:

- deterministic;
- structured rather than prose-only;
- attributable to a field, path, or entity when possible;
- explicit about severity (error/warning/advisory when supported);
- atomic with respect to publication/instantiation.

A compilation or resolution step may derive runtime-ready structures such as:

- resolved definition references;
- capability/resource indexes;
- routing/operation indexes;
- precomputed immutable geometry structures;
- validated transfer relationships;
- derived scheduling metadata.

These derived structures are runtime preparation, not a second independently maintained business model.

## 11. Initial implementation boundary

Arcogine should provide a deliberately small shared design substrate before building a general-purpose editor service.

The initial shared substrate should include:

1. a canonical factory model schema;
2. structured model validation;
3. an explicit publication boundary producing an immutable model version;
4. deterministic runtime instantiation from that version;
5. runtime provenance linking runs to model versions.

The first consumer may still own its mutable draft, undo history, and editor persistence:

```text
Consumer-owned FactoryDraft
          |
          | project/submit
          v
Arcogine validation + publication
          |
          v
FactoryModelVersion
          |
          v
Simulation / verification
```

Shared draft branching, merge, collaborative editing, generalized undo/redo, and long-lived design workspaces are deferred until a second concrete consumer or workflow requires them.

## 12. Architectural dependency direction

Conceptually, the dependency direction is:

```text
                   Canonical factory model
                 definitions + validation
                          |
             +------------+------------+
             |                         |
             v                         v
      Design capability          Factory runtime
    drafts/publication          work/events/state
             |                         |
             +------------+------------+
                          v
                 Interfaces/adapters
```

The canonical model must not depend on a specific UI or simulation transport. The runtime must not mutate the published design model in place. Consumers must not depend on mutable runtime internals to author a design.

This separation may initially be implemented inside existing modules/packages. A new Gradle module is warranted only when dependency direction, independent lifecycle, or ownership invariants justify one; package proliferation is not the goal.

## 13. Review checklist

When a change introduces or modifies factory-design semantics, ask:

1. Is this fact part of the production-system definition, the design lifecycle, runtime state, verification state, or one consumer's experience?
2. Does changing it alter executable behavior across consumers?
3. Can a draft be temporarily invalid while the published model must reject the same state?
4. Is the fact ordered by model revision or by simulated time?
5. Does the runtime need it, or only the editor presentation?
6. Is this resource hierarchy or spatial layout?
7. Is a constraint about executability, verification, or a consumer rule?
8. Can a simulation result identify the exact published model version that produced it?
9. Are we creating a second representation that must be manually translated into runtime semantics?
10. Is a new shared design abstraction justified by more than one concrete consumer/workflow?

## 14. Triggers for revisiting this architecture

Revisit this document when Arcogine introduces or materially changes:

- a canonical `FactoryModel` or equivalent published model;
- model versioning, hashes, or lineage;
- a shared draft/design service;
- semantic model diff or compare;
- design branching/merge/collaboration;
- deployment of model changes to real operations;
- spatial geometry that affects execution;
- work-center/resource-pool hierarchy;
- cross-consumer validation rules;
- a second independent design consumer;
- ISA-95/AAS/other model import or export.

Durable, hard-to-reverse decisions that emerge from these triggers should be recorded as ADRs. This document should remain a current architectural reference rather than a chronological change log.
