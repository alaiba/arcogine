# Factory Design Capability Plan

> **Status:** Proposed  
> **Scope:** Establish a cross-consumer factory-design capability over Arcogine's canonical production-system model  
> **Authority:** Planning only; this document defines delivery slices and readiness criteria, not current capability or accepted architecture  
> **Related:** [Factory Design Architecture](../architecture/factory-design.md), [ADR-0003](../architecture/decisions/0003-canonical-factory-model-boundary.md), [ADR-0004](../architecture/decisions/0004-model-identity-revisions-and-change-management.md), [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md), [Factory-Design Game Consumer Initiative](factory-design-game-consumer.md), [ISA-95 Semantic Mapping](../architecture/isa-95-semantic-mapping.md)

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

This plan establishes the minimal shared design substrate before a game, industrial UI, optimizer, CLI, or integration adapter treats Arcogine as the authority for a published factory design.

## 2. Relationship to engine readiness

This capability is orthogonal to runtime execution, but both share one canonical production-system model.

This plan owns model-side semantics and lifecycle:

- canonical factory model definition;
- structured validation;
- immutable publication/version boundary;
- model identity and provenance;
- deterministic runtime instantiation from a published model;
- eventual semantic compare/diff and broader design lifecycle when justified.

The [engine-readiness plan](factory-simulation-engine-readiness.md) starts after this boundary exists. It owns runtime execution semantics such as production orders, work items, resource dispatch, bounded advancement, observations, events, and transfer progression.

```text
Factory Design Capability D1-D4
        |
        v
Published FactoryModelVersion
        |
        v
Factory Simulation Engine Readiness
```

The runtime must not mutate the published model in place. The design capability must not reproduce queues, assignments, transfers in progress, or other runtime state.

## 3. Delivery policy

The first implementation target is deliberately narrow and behavior-preserving.

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

D1-D4 form the immediate implementation sequence. D5 and D6 are explicitly deferred and are not prerequisites for engine runtime work or a first game consumer.

The initial spike should establish the model seam without simultaneously redesigning order execution, dispatch policy, spatial behavior, or the public HTTP contract.

## 4. Current-model migration strategy

The first implementation should adapt today's scenario factory semantics into the canonical model and then instantiate the existing runtime from that model with no intended behavior change.

```text
Current ScenarioConfig / TOML
            |
            | extract/adapt factory semantics
            v
       FactoryModel
            |
         validate
            v
   FactoryModelVersion
            |
      resolve/compile
            v
 existing runtime construction
```

`ScenarioConfig` remains a scenario/run input envelope rather than becoming the canonical factory model. Simulation parameters, economy configuration, agent configuration, and workload concerns remain outside the factory design.

For current features that are narrower than the intended future model, the adapter may preserve existing semantics explicitly. For example, a process step that currently targets one concrete resource may be represented initially as an eligible-resource set containing that one instance. The seam should not manufacture new runtime behavior merely to appear more general.

### 4.1 Migration-spike acceptance criteria

Before broad runtime refactoring begins:

1. An existing scenario can produce a canonical `FactoryModel` or equivalent.
2. Simulation, economy, and agent configuration do not enter that model.
3. The model validates independently of mutable runtime construction.
4. A valid model can be published with stable identity/provenance under a defined initial policy.
5. The existing runtime can be instantiated from the published model or its derived executable representation.
6. Existing deterministic scenario results remain unchanged for representative regression fixtures.
7. Runtime observations/results can identify the source model version.
8. No `ProductionOrder`/`WorkItem` redesign, capability-dispatch redesign, spatial-transfer behavior, or game UI is required to prove this spike.

## 5. D1 — Canonical factory model contract

### 5.1 Goal

Define one semantic representation of a designed production system that can be authored by multiple consumers and instantiated by multiple lifecycle contexts.

The initial model should cover only behaviorally relevant facts needed by current and near-term factory work:

```text
FactoryModel
    model/schema version
    product definitions
    operation definitions
    resource definitions
    installed resource instances
    capability or explicit eligibility requirements
    relevant policies and constraints
    semantic spatial layout where behavior depends on it
```

The exact Java and wire types remain an implementation decision. The model must not be shaped around one editor state tree, TOML layout, transport format, or mutable runtime object graph.

### 5.2 Required separations

```text
MODEL SIDE
    definitions
    installed structure
    validated published model

RUNTIME SIDE
    production orders
    work items
    queues
    assignments
    transfers in progress
    performance
```

Runtime workload/state does not belong in a published factory design.

### 5.3 Acceptance criteria

D1 is satisfied when:

1. A complete factory can be represented without frontend DTOs or mutable runtime classes.
2. Product/operation/resource concepts have stable semantic identities.
3. Resource definitions and installed instances are distinguishable, even if the migration adapter initially creates a one-to-one pair from current equipment configuration.
4. Operation requirements can express capability-based or explicit eligible-resource semantics without making the product definition permanently depend on mutable runtime objects.
5. Semantic layout facts are present only where they affect execution or shared validation.
6. Consumer-only state such as selection, camera, undo, score, and artwork is absent.
7. Canonical content can be serialized deterministically or otherwise normalized for identity/provenance purposes.

## 6. D2 — Structured validation

### 6.1 Goal

Make Arcogine authoritative for whether a factory design can be published and instantiated.

Consumers may perform optimistic local checks, but publication/runtime instantiation relies on Arcogine validation.

Validation should distinguish at least:

```text
ERROR
    model cannot publish/instantiate

WARNING
    model is executable but has a noteworthy condition
```

### 6.2 Validation result shape

A finding should contain enough structure for any consumer to present or automate against it:

```text
code
severity
message
entity/type identifier where applicable
field/path where applicable
related identifiers where applicable
```

Codes become stable contract elements once exposed publicly. Human-readable wording may evolve more freely.

### 6.3 Initial executability checks

Examples include:

- duplicate/missing identifiers;
- dangling product/operation/resource references;
- invalid operation order/graph;
- operation with no resolvable eligible resource under the represented semantics;
- invalid resource definition/instance relationship;
- unsupported policy/configuration values;
- floor/footprint/transfer-input violations when spatial semantics are introduced.

Game budget, unlocks, scores, and tutorials are not Arcogine executability checks.

### 6.4 Acceptance criteria

D2 is satisfied when:

1. Invalid models return deterministic structured findings.
2. Findings identify the affected entity/path when possible.
3. Validation does not mutate runtime state.
4. Publication/instantiation is atomic: an invalid model cannot become partially active.
5. A headless harness and a UI consumer can use the same validation contract.

## 7. D3 — Publication, identity, and provenance

### 7.1 Goal

Create an explicit boundary between mutable authoring state and an immutable semantic snapshot that downstream contexts can instantiate.

```text
FactoryModel
    ↓ validate/publish
immutable semantic snapshot
    ↓
FactoryModelFingerprint
```

Arcogine does not need to own the first consumer's draft persistence to provide this boundary.

### 7.2 Minimum identity

Per [ADR-0004](../architecture/decisions/0004-model-identity-revisions-and-change-management.md), the initial publication boundary requires only a deterministic content-derived **fingerprint** and enough provenance to attribute a downstream run to the model:

```text
fingerprint (deterministic, content-derived)
publication provenance
```

No model UUID, revision counter, Jira key, or approval state is required at this stage — those belong to the deferred controlled-revision capability below.

The exact fingerprint scheme remains an implementation decision. It must derive from semantic model content rather than consumer presentation metadata.

Persistent model repositories, branch lineage, and collaborative authoring are not required for the initial implementation; an in-memory publication boundary is sufficient to prove semantics.

### 7.3 Immutability

Once published, the model snapshot is immutable. A semantic change creates another snapshot rather than editing the model underneath an existing runtime or verification context.

### 7.4 Acceptance criteria

D3 is satisfied when:

1. A validated factory can be published as an immutable semantic snapshot.
2. Different semantic content cannot mutate an existing snapshot.
3. Equivalent canonical content produces a stable deterministic fingerprint under the selected policy.
4. Consumer draft/editor metadata does not affect the fingerprint.
5. Publication records enough provenance to attribute a downstream run to the model.

### 7.5 Deferred: controlled-revision capability

A controlled-revision lifecycle — persistent repository, lineage, approval state, deployment tracking, and an external change reference (e.g. a Jira issue key) — is explicitly out of scope for D3. Build it only when a concrete trigger makes it necessary:

- a persistent model repository is needed;
- a Jira (or equivalent) change-management integration is needed;
- an approval/deployment workflow is needed;
- audit requirements demand recorded change history;
- branching/lineage across concurrent design efforts is needed.

Until one of these triggers a concrete implementation, Arcogine does not need a revision entity, only the fingerprint.

## 8. D4 — Deterministic runtime instantiation

### 8.1 Goal

Make the published model the only semantic bridge from design into runtime contexts.

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

The exact implementation types are not fixed by this plan.

### 8.2 Derived structures

Runtime preparation may derive:

- resolved product/operation references;
- capability or explicit eligibility indexes;
- immutable operation/routing indexes;
- geometry/spatial indexes when supported;
- validated transfer relationships when supported;
- derived scheduling metadata.

These structures are derived from one published semantic model and are not independently authored sources of truth.

### 8.3 Runtime provenance

Every run/session must identify the model version it instantiated.

At minimum, supported observations/results should make the following attributable:

```text
session/run ID
model fingerprint
simulation seed/context
```

### 8.4 Acceptance criteria

D4 is satisfied when:

1. A published model version can instantiate a fresh deterministic runtime.
2. Runtime cannot mutate the published model.
3. Two fresh runtimes instantiated from the same model version and same simulation inputs preserve deterministic behavior.
4. Runtime observations/results identify the source model version.
5. No consumer-specific draft representation is required by runtime.
6. Representative existing scenarios still produce the same results through the new model seam.

## 9. D5 — Semantic comparison and design alternatives

Implement shared semantic comparison only when more than one concrete consumer/workflow needs a common domain-level diff.

Potential changes include resource added/removed/moved, resource definition changed, operation requirement changed, product definition changed, policy changed, or constraint changed.

A change-management workflow (for example, a Jira-backed review process) is one concrete future consumer of this capability: reviewers need a domain-level semantic diff between a candidate revision and its predecessor, not a generic text/JSON diff, to assess a proposed change.

Do not implement arbitrary text/JSON diff, generic patch/merge, or collaborative editing merely to satisfy this stage.

## 10. D6 — Shared draft lifecycle and collaboration

Promote drafts into an Arcogine-owned shared lifecycle only when a second concrete workflow requires common persistence, branching, collaboration, review, or approval.

Possible triggers include industrial design plus optimizer/game authoring, human/agent co-design, branching real production changes, or multi-user design sessions.

Arcogine is not required to own approval or organizational change-management workflow as part of this capability. Shared drafts/collaboration and organizational change management (per [ADR-0004](../architecture/decisions/0004-model-identity-revisions-and-change-management.md)) are separate concerns; the latter may remain owned by an external system such as Jira.

Until a trigger applies, Arcogine does not need generic undo/redo, draft branching, merge, collaboration cursors, edit locks, comments, workspace permissions, or autosave semantics.

## 11. Constraint classification

Every design rule must be classified before implementation.

| Class | Meaning | Owner |
|---|---|---|
| Executability constraint | Required for the model to publish/instantiate coherently | Arcogine model/design boundary |
| Verification objective/constraint | Tests whether an executable design meets a target | Shared verification capability when supported |
| Consumer rule | Applies only to one experience/workflow | Consumer |

Examples:

```text
Resource outside floor               -> executability
Operation has no eligible resource   -> executability
Throughput must exceed target        -> verification
Player construction budget           -> game consumer
Machine unlock level                 -> game consumer
```

## 12. Interaction with engine readiness

The engine-readiness plan consumes published model versions rather than treating runtime state as the design model.

The design capability owns:

```text
Product/operation definitions
Resource definitions and instances
Capability/eligibility requirements
Semantic layout
Validation
Publication/model provenance
```

Engine readiness owns:

```text
Production orders and work items
Quantity execution semantics
Dispatch and queues
Active operations/transfers
Runtime events and observations
Performance
```

Where a concern crosses the boundary, the model owns the input semantics and runtime owns their changing consequences. For example, model-side position/footprint belong to factory design; transfer-in-progress state and transfer events belong to runtime.

## 13. Factory-design game integration

The game may own an editor-specific `FactoryDraft`, undo history, camera, palettes, previews, and game rules.

```text
Game-owned draft
      |
 project canonical semantics
      v
Arcogine validate/publish
      |
      v
FactoryModelVersion
      |
      v
Arcogine runtime
```

The game must not implement a parallel scheduler or treat its draft as the authoritative executable model.

## 14. Headless acceptance path

### 14.1 Behavior-preserving publication test

1. Load an existing representative scenario.
2. Adapt only its factory semantics into a canonical model.
3. Validate the model.
4. Publish a model version.
5. Instantiate runtime through the new boundary.
6. Run the same inputs/seed as the existing path.
7. Assert deterministic behavior/results are unchanged.
8. Assert results identify the source model version.

### 14.2 Variant test after richer semantics exist

1. Publish model A.
2. Publish model B with one semantic design change.
3. Instantiate independent runtimes.
4. Apply the same seed/workload.
5. Verify deterministic but appropriately different outcomes.

Examples later include adding a capable resource, moving one resource, changing an operation requirement, or changing an engine-owned processing/transfer policy.

## 15. First implementation milestone

> **Take an existing Arcogine scenario, derive a validated immutable canonical factory model from it, instantiate the existing simulation from that model, and prove the simulation result has not changed.**

Definition of done:

```text
Canonical FactoryModel exists
ScenarioConfig adapts into it
Simulation/economy/agent concerns remain outside it
Structured validation exists
Invalid model cannot publish/instantiate
Published version has stable identity/provenance
Existing runtime instantiates from that version
Runtime reports its model version
Representative deterministic behavior is unchanged
Published model remains immutable
No ProductionOrder/WorkItem rewrite is required yet
No shared editor service is required
```

This milestone deliberately excludes semantic diff, collaboration, generalized design workspaces, quantity/work-item redesign, capability-based dispatch behavior changes, spatial transfer behavior, public HTTP versioning, and a game UI.

## 16. ADR triggers

[ADR-0003](../architecture/decisions/0003-canonical-factory-model-boundary.md) establishes the accepted model/run/runtime boundary. [ADR-0004](../architecture/decisions/0004-model-identity-revisions-and-change-management.md) establishes the accepted separation between semantic fingerprint and controlled-revision/change-management identity.

Additional ADRs are warranted when implementation commits to hard-to-reverse choices about:

- concrete canonical-model aggregate/type boundaries;
- fingerprint computation/versioning semantics;
- compilation representation and caching rules;
- a concrete controlled-revision repository, persistent model storage, or lineage scheme;
- an external change-management integration (e.g. Jira) whose protocol becomes hard to reverse once adopted;
- work-center/resource-pool semantics;
- shared draft lifecycle/collaboration;
- deployment of a design into real operations.

Do not create ADRs for consumer-local editor gestures or temporary UI structure.

## 17. Documentation lifecycle

While this work is proposed, this file remains under `docs/planning/`.

As D1-D4 are implemented and decisions accepted:

- reconcile established behavior into [`../architecture/overview.md`](../architecture/overview.md);
- update [`../product/concepts.md`](../product/concepts.md) only for capabilities that actually ship;
- update [`../reference/api.md`](../reference/api.md) only when a public contract exists;
- update the [ISA-95 semantic mapping](../architecture/isa-95-semantic-mapping.md) when implemented manufacturing concepts change;
- keep migration/regression fixtures executable and version-controlled.

Once the initiative is complete or abandoned, reduce this file to a concise historical outcome or retire it after durable decisions and current behavior are represented in authoritative locations.
