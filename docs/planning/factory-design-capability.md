# Factory Design Capability Plan

> **Status:** Proposed  
> **Scope:** Establish a cross-consumer factory-design capability over Arcogine's canonical production-system model  
> **Authority:** Planning only; this document defines delivery slices and readiness criteria, not current capability or accepted architecture  
> **Related:** [Factory Design Architecture](../architecture/factory-design.md), [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md), [Factory-Design Game Consumer Initiative](factory-design-game-consumer.md), [ISA-95 Semantic Mapping](../architecture/isa-95-semantic-mapping.md)

## 1. Purpose

Arcogine should treat factory design as a cross-consumer capability rather than a game-specific editor concern.

The semantic output of design belongs to Arcogine because the same production-system model must remain continuous across design, simulation, verification, and future execution. The authoring experience remains consumer-specific unless multiple concrete consumers justify a shared design workspace.

The delivery shape is:

```text
Consumer-specific draft authoring
              |
              v
     Canonical model validation
              |
              v
       Publish model version
              |
       +------+------+
       |             |
       v             v
   Simulate        Verify
```

This plan establishes the minimal shared design substrate required before a game, industrial UI, optimizer, CLI, or integration adapter publishes factory designs into Arcogine.

## 2. Relationship to engine readiness

This capability is orthogonal to the simulation runtime, but the two share the same canonical model.

The [engine-readiness plan](factory-simulation-engine-readiness.md) is responsible for runtime execution semantics: production orders, work items, resource dispatch, bounded advancement, observations, events, and transfer behavior.

This plan is responsible for model-side semantics and lifecycle:

- canonical factory model definition;
- structured validation;
- immutable publication/version boundary;
- model identity and provenance;
- deterministic runtime instantiation from a published model;
- eventual semantic compare/diff and broader design lifecycle when justified.

The runtime must not mutate the published model in place. The design capability must not reproduce queueing, dispatch, or simulation state.

## 3. Delivery policy

The first implementation target is deliberately small.

```text
D1  Canonical factory model contract
    ↓
D2  Structured validation
    ↓
D3  Publication, identity, and provenance
    ↓
D4  Deterministic runtime instantiation
    ↓
D5  Semantic comparison (after concrete need)
    ↓
D6  Shared draft lifecycle (deferred until justified)
```

D1–D4 are required before a consumer treats Arcogine as the authority for a published factory design. D5 and D6 are not prerequisites for the first game or headless engine readiness work.

## 4. D1 — Canonical factory model contract

### 4.1 Goal

Define one semantic representation of a designed production system that can be authored by multiple consumers and instantiated by multiple lifecycle contexts.

The initial model should cover only behaviorally relevant facts needed by current factory-readiness work:

```text
FactoryModel
    model/schema version
    product definitions
    operation definitions
    resource definitions
    installed resource instances
    capability/resource requirements
    relevant policies and constraints
    semantic spatial layout where behavior depends on it
```

The exact Java and wire types remain an implementation decision. The model must not be shaped around one editor's state tree or one transport format.

### 4.2 Required separations

The model must distinguish:

```text
Definition
    product, operation, resource capability

Installed structure
    resource instances, pool/work-center membership, semantic placement

Runtime workload/state
    orders, work items, queues, assignments, transfers
```

Runtime workload and state do not belong in a published factory design.

### 4.3 Acceptance criteria

D1 is satisfied when:

1. A complete factory can be represented without using frontend DTOs or mutable runtime classes.
2. Product/operation/resource definitions have stable identities.
3. Resource definitions and installed instances are distinct.
4. Operation requirements can refer to capabilities or explicit eligible resources without binding the product definition permanently to one concrete machine.
5. Semantic layout facts are present only where they affect execution or shared validation.
6. Consumer-only state such as selection, camera, undo, score, and artwork is absent.
7. The model can be serialized deterministically or otherwise hashed canonically for identity/provenance purposes.

## 5. D2 — Structured validation

### 5.1 Goal

Make Arcogine authoritative for whether a factory design is executable.

Consumers may perform local optimistic checks, but publication and runtime instantiation rely on Arcogine validation.

Validation should distinguish at least:

```text
ERROR
    model cannot be published/instantiated

WARNING
    model is executable but has a noteworthy condition
```

Advisories may be added later if useful.

### 5.2 Validation result shape

A validation finding should contain enough structure for any consumer to present or automate against it:

```text
code
severity
message
entity/type identifier where applicable
field/path where applicable
related identifiers where applicable
```

Codes are stable contract elements once exposed publicly. Human-readable wording may evolve more freely.

### 5.3 Initial executability checks

Examples include:

- duplicate or missing identifiers;
- dangling product/operation/resource references;
- invalid operation graph/order;
- operation with no resolvable eligible capacity;
- invalid resource definition/instance relationship;
- floor or footprint violations when spatial semantics are enabled;
- unsupported policy/configuration values;
- inconsistent transfer inputs.

Game budget, unlocks, scores, and tutorials are not Arcogine executability checks.

### 5.4 Acceptance criteria

D2 is satisfied when:

1. Invalid models return deterministic structured findings.
2. Findings identify the affected entity/path when possible.
3. Validation does not mutate runtime state.
4. Publication/instantiation is atomic: an invalid model cannot become partially active.
5. A headless test harness and a UI consumer can use the same validation result contract.

## 6. D3 — Publication, identity, and provenance

### 6.1 Goal

Create an explicit boundary between mutable authoring state and an immutable model version that downstream contexts can instantiate.

Conceptually:

```text
FactoryDraft
     |
 validate
     |
 publish
     v
FactoryModelVersion
```

Arcogine does not need to own the first consumer's draft persistence to provide this boundary.

### 6.2 Minimum published-version identity

A published version should expose or persist enough information to identify it unambiguously:

```text
model ID
model revision/version
model/schema version
content hash
publication provenance
```

The exact versioning scheme remains an implementation decision. The content hash must be derived from semantic model content rather than consumer presentation metadata.

### 6.3 Immutability

Once published, the model version is immutable. A change creates another version rather than editing the model underneath an existing simulation or verification run.

### 6.4 Acceptance criteria

D3 is satisfied when:

1. A validated factory can be published as an immutable model version.
2. Re-publishing different semantic content does not mutate the old version.
3. Equivalent canonical content produces a stable deterministic identity/hash under the defined policy.
4. A consumer can retain its own draft/editor metadata without affecting published model identity.
5. Publication records sufficient provenance to attribute a downstream run to the published model.

## 7. D4 — Deterministic runtime instantiation

### 7.1 Goal

Make the published model the only semantic bridge from design into simulation/runtime contexts.

The runtime may compile/resolve immutable structures for efficiency, but it must not require manual translation into a second business model.

Conceptually:

```text
FactoryModelVersion
       |
 validate/resolve
       v
ExecutableFactoryModel
       |
 instantiate
       v
FactoryRuntime
```

### 7.2 Compilation/resolution responsibilities

Possible derived runtime structures include:

- resolved product/operation references;
- capability and eligible-resource indexes;
- immutable routing/operation indexes;
- geometry/spatial indexes;
- validated transfer relationships;
- derived scheduling metadata.

These are derived from one published semantic model and are not independently authored sources of truth.

### 7.3 Runtime provenance

Every run/session must identify the model version it instantiated.

At minimum, supported observations and exported results should make the following attributable:

```text
session/run ID
model ID/version/hash
simulation seed/context
```

### 7.4 Acceptance criteria

D4 is satisfied when:

1. A published model version can instantiate a fresh deterministic runtime.
2. The runtime cannot mutate the published model.
3. Two fresh runtimes instantiated from the same model version and same simulation inputs produce the same deterministic behavior.
4. Runtime observations identify the source model version.
5. No consumer-specific draft representation is required by the runtime.
6. The headless capacity and layout benchmarks operate by publishing/instantiating model variants rather than editing one running simulation in place.

## 8. D5 — Semantic comparison and design alternatives

### 8.1 Trigger

Implement shared semantic comparison when Arcogine has a concrete need to compare two published designs across more than one consumer or workflow.

The factory-design game and improvement workflows are likely candidates, but the first game may perform simple consumer-side comparison using model versions and simulation outcomes.

### 8.2 Potential scope

A shared semantic diff should report domain changes such as:

```text
resource instance added/removed/moved
resource definition changed
operation requirement changed
product definition changed
policy changed
constraint changed
```

It should ignore presentation-only metadata.

### 8.3 Non-goal

Do not implement generic text diff, arbitrary JSON patch, merge conflict resolution, or collaborative editing merely to satisfy D5.

## 9. D6 — Shared draft lifecycle and collaboration

### 9.1 Trigger

Promote drafts into an Arcogine-owned shared lifecycle only when a second concrete consumer or workflow requires common draft persistence, branching, collaboration, review, or approval.

Possible triggers include:

- industrial design UI and game/optimizer both editing the same model workspace;
- human and autonomous design agents collaborating on revisions;
- approval workflows before deployment;
- branching and comparison of real production changes;
- multi-user design sessions.

### 9.2 Deferred capabilities

Until triggered, Arcogine does not need generic:

- undo/redo;
- draft branching;
- merge;
- collaboration cursors;
- edit locks;
- comments/review threads;
- workspace permissions;
- autosave semantics.

The first consumer may own these locally while publishing canonical model versions through Arcogine.

## 10. Constraint classification

Every design rule introduced by a consumer or engine initiative must be classified before implementation.

| Class | Meaning | Owner |
|---|---|---|
| Executability constraint | Required for the model to instantiate/execute coherently | Arcogine model/design boundary |
| Verification objective/constraint | Tests whether an executable design meets a target | Shared verification capability when supported |
| Consumer rule | Applies only to one experience or game/business workflow | Consumer |

Examples:

```text
Resource outside floor                  -> executability
Operation has no eligible capability    -> executability
Throughput must exceed target           -> verification
Safety separation must exceed limit     -> verification/model rule depending on semantics
Player construction budget              -> game consumer
Machine unlock level                    -> game consumer
```

## 11. Dependencies and interaction with other plans

### 11.1 Engine readiness

The engine-readiness plan should consume published model versions rather than treat runtime state as the design model.

Its Gate 1 domain work must therefore distinguish:

```text
MODEL SIDE
    definitions
    installed structure
    validated published model

RUNTIME SIDE
    orders
    work items
    queues
    assignments
    transfers
    performance
```

Resource capabilities and spatial semantics are shared model semantics; dispatch, queues, transfer progression, and performance are runtime semantics.

### 11.2 Factory-design game

The game remains free to own an editor-specific `FactoryDraft`, undo history, camera, palettes, construction previews, and game rules.

Its integration shape is:

```text
Game-owned draft
      |
 project semantic model
      v
Arcogine validate/publish
      |
      v
FactoryModelVersion
      |
      v
Arcogine runtime
```

The game must not implement a parallel production scheduler or treat its draft representation as the authoritative executable model.

### 11.3 ISA-95 semantic mapping

The ISA-95 mapping guides definitions, resource semantics, hierarchy, schedule/execution/performance separation, and terminology. Factory design may extend beyond ISA-95, especially for spatial layout. Standards semantics should inform the model without forcing unnecessary hierarchy or compliance scope.

## 12. Headless acceptance path

Before using a visual design consumer as evidence, prove the design boundary headlessly.

### 12.1 Publication test

1. Construct a complete canonical factory model.
2. Validate it.
3. Publish it as a model version.
4. Instantiate a simulation runtime.
5. Run a fixed workload.
6. Assert that results identify the source model version.

### 12.2 Variant comparison test

1. Publish model A.
2. Publish model B with one semantic design change.
3. Instantiate independent runtimes from each.
4. Apply the same seed/workload.
5. Verify deterministic but meaningfully different outcomes where the changed design should matter.

Examples:

- add a second capable machine;
- move one resource farther away;
- change an operation capability requirement;
- alter an engine-owned processing/transfer policy.

## 13. First implementation milestone

The first design-capability milestone is:

> **Publish and instantiate one validated immutable factory model without relying on frontend/editor state or mutable runtime classes.**

Definition of done:

```text
Canonical factory model exists
Structured validation exists
Invalid model cannot publish/instantiate
Published version has stable identity/provenance
Runtime instantiates from that version
Runtime reports its model version
Published model remains immutable
No shared editor service is required
```

This milestone deliberately excludes semantic diff, collaboration, branching, merge, generalized design workspaces, and a game UI.

## 14. ADR triggers

Create an ADR when implementation commits to a hard-to-reverse decision about:

- canonical model versus scenario representation;
- model ID/version/hash semantics;
- publication and immutability boundary;
- whether compilation produces a separate runtime representation and what may be cached there;
- persistent model storage or lineage;
- work-center/resource-pool semantics;
- shared draft lifecycle or collaboration model;
- deployment of a design into real operations.

Do not create ADRs for consumer-local editor gestures or temporary UI structure.

## 15. Explicit non-goals

This initiative does not require:

- a general-purpose CAD system;
- rendering or scene-graph infrastructure;
- a shared game editor;
- a complete ISA-95 hierarchy or interchange implementation;
- collaborative editing before a concrete need exists;
- generic undo/redo infrastructure;
- arbitrary JSON patch/merge protocols;
- runtime mutation of published models;
- game construction budget, scoring, progression, unlocks, or narrative;
- pathfinding, workers, vehicles, or congestion merely because layout is represented;
- real-world deployment approval workflows in the initial milestone.

The goal is a small, reusable design boundary that preserves one executable production-system model across consumers and lifecycle contexts.
