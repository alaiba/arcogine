# ADR-0003: Canonical factory model and runtime boundary

Status: Accepted
Date: 2026-08-25

## Context

Arcogine currently accepts a `ScenarioConfig` that combines several concerns in one runnable input envelope: simulation parameters, equipment, material, process segments, operations definitions, economy configuration, and agent configuration.

That shape is useful for describing an experiment, but the product direction now requires a cross-consumer factory-design boundary. A game editor, industrial design UI, optimizer, CLI, importer, or future integration should be able to describe the same production system without treating simulation settings, economy behavior, or agent configuration as properties of the factory itself.

The Product Charter also requires semantic continuity between design, simulation, verification, and future execution. If each lifecycle context translates a consumer-specific design into a separate runtime representation by hand, Arcogine would recreate the model fragmentation the Charter explicitly seeks to avoid.

A second distinction is equally important: mutable runtime state is not the designed production system. Production orders, work items, queues, assignments, active transfers, simulated time, and performance measurements belong to an execution context. They must not become mutable fields of the published factory design.

The immediate implementation goal is intentionally conservative: introduce this boundary without simultaneously redesigning order execution, dispatch, spatial behavior, or the public API. Existing scenarios should be adaptable into the canonical model and should continue to produce the same deterministic behavior while the seam is established.

Related analysis and plans:

- [`../factory-design.md`](../factory-design.md)
- [`../../planning/factory-design-capability.md`](../../planning/factory-design-capability.md)
- [`../../planning/factory-simulation-engine-readiness.md`](../../planning/factory-simulation-engine-readiness.md)
- [`../isa-95-semantic-mapping.md`](../isa-95-semantic-mapping.md)

## Decision

Arcogine establishes a canonical factory-model boundary with the following invariants.

### Scenario input is not the canonical factory model

`ScenarioConfig` remains a scenario/run input envelope. It may contain or reference factory design information, but simulation parameters, workload, economy configuration, agent configuration, and other execution-context concerns do not become properties of the canonical factory model merely because they are currently serialized together.

Existing scenario/TOML input is treated as one adapter or authoring source for the canonical model rather than as the canonical model itself.

Conceptually:

```text
Scenario/TOML ---------+
Game design -----------|
Industrial design UI --|
Optimizer -------------+--> FactoryModel
Importer/adapter ------|
CLI/test builder ------+
```

### The canonical model represents the designed production system

A `FactoryModel` or equivalent semantic representation owns behaviorally relevant production-system definitions and installed structure, such as products, operations, resource definitions, resource instances, capability or eligibility requirements, policies/constraints, and semantic layout where those facts affect execution.

Exact Java type names and package/module placement remain implementation decisions until the model spike validates them.

### Published model versions are immutable

A validated design is published as an immutable `FactoryModelVersion` or equivalent identified artifact. A semantic change creates another version rather than mutating the design underneath an existing simulation or verification context.

Published identity must be sufficient to attribute downstream results to the exact semantic design. The eventual identity/version/hash policy is a separate implementation decision, but consumer presentation metadata must not determine semantic model identity.

### Runtime state is instantiated from one model version

Simulation and future execution contexts instantiate mutable runtime state from one published model version.

Runtime state includes concerns such as production orders, work items, queues, assignments, active operations, transfers in progress, simulated time, event position, and performance observations. Those concerns do not mutate the published factory model.

Every runtime context must retain provenance identifying the model version from which it was instantiated.

### Derived runtime structures are not a second authored model

Arcogine may compile or resolve a published model into an `ExecutableFactoryModel` or equivalent internal representation for efficient execution. Such structures may include resolved references, routing indexes, capability/eligibility indexes, spatial indexes, or derived scheduling metadata.

These structures are derived from one published semantic model. They are not independently authored sources of truth and must not require manual semantic translation by consumers.

### Consumer drafts are authoring representations

A consumer may own a mutable `FactoryDraft`, editor state, undo history, temporary invalid states, camera data, game rules, or other authoring metadata. Those representations are not authoritative executable models.

Arcogine remains authoritative for shared executability validation and publication of the semantic model. Shared draft persistence, branching, collaboration, merge, and review workflows are deferred until a concrete cross-consumer requirement justifies them.

## Alternatives considered

### Evolve `ScenarioConfig` into the canonical factory model

This would minimize the number of model types initially and preserve the current TOML-centric shape.

It was not selected because `ScenarioConfig` mixes the designed production system with simulation parameters, economy configuration, agents, and other run concerns. Promoting it to canonical status would preserve model/run coupling and make non-scenario design consumers inherit concepts they do not own.

### Let each consumer translate directly into runtime classes

This would avoid a new canonical model layer and could be expedient for one client.

It was rejected because it creates multiple independently maintained interpretations of products, operations, resources, and layout. It also exposes mutable runtime structure as a de facto public design contract and weakens provenance.

### Make mutable runtime state the canonical factory representation

This would treat the current in-memory machine/job/routing structures as both design and execution state.

It was rejected because design revision and simulated time have different lifecycles and validity rules. Drafts may be incomplete; runtime state may not. Published designs must remain immutable while queues, work, assignments, and transfers change continuously.

### Build a complete shared design workspace before introducing the model boundary

This would centralize drafts, history, branching, collaboration, and publication immediately.

It is deferred because the current requirement is semantic continuity, validation, publication, and runtime instantiation. Shared editing infrastructure is not justified by a single concrete authoring consumer.

## Consequences

As a result of this decision:

- the first factory-design implementation work becomes a behavior-preserving model seam rather than a broad runtime rewrite;
- existing scenarios require an adapter/extraction path into the canonical factory model;
- validation can become a shared cross-consumer capability instead of parser/controller-specific behavior;
- simulations and exported results can carry explicit model provenance;
- product/order/work-item execution refactoring can proceed after the model boundary exists, reducing the number of semantic changes made at once;
- capability-based dispatch and spatial consequences can consume canonical model semantics without making the runtime an editor model;
- consumers may keep local draft UX and persistence while sharing the same published semantic model;
- `FactoryModel`, `FactoryModelVersion`, and `ExecutableFactoryModel` remain conceptual names until implementation proves the appropriate concrete types;
- current-state architecture and product-reference documentation must not describe this boundary as implemented until the code exists.

The main cost is an additional model boundary and adapter path between current scenario input and runtime construction. That cost is intentional: it prevents a run configuration or mutable runtime object graph from becoming the long-term cross-consumer production-system contract by accident.

## Charter alignment

This decision directly supports the Product Charter's **one model, many views**, **lifecycle continuity**, **semantics survive deployment**, and **causality and provenance** principles.

It keeps authoring experience, simulation runtime, and future execution contexts operationally distinct while preserving one semantic production-system model across them. It also keeps hypothetical runtime state explicit: a simulation is an instantiation of a particular published model version, not a mutation of the design itself.
