# Factory Simulation Engine Readiness

> **Status:** Proposed  
> **Scope:** Prepare Arcogine's factory runtime for external consumers after the canonical factory-model boundary is established  
> **Authority:** Planning only; this document defines runtime-readiness gates, not current capability or accepted architecture  
> **Prerequisite:** D1-D4 in [Factory Design Capability](factory-design-capability.md)  
> **Related:** [Factory Design Architecture](../architecture/factory-design.md), [ADR-0003](../architecture/decisions/0003-canonical-factory-model-boundary.md), [ADR-0004](../architecture/decisions/0004-model-identity-revision-lineage-and-external-change-control.md), [Factory-Design Game Consumer Initiative](factory-design-game-consumer.md), [ISA-95 Semantic Mapping](../architecture/isa-95-semantic-mapping.md), [Architecture Overview](../architecture/overview.md)

## 1. Purpose

Arcogine should become a consumer-ready deterministic production runtime before a factory-design game is used as an integration client.

This plan begins **after** Arcogine can validate, publish, and instantiate an immutable canonical factory model. It does not own the factory-design schema, publication lifecycle, or consumer draft model.

The intended dependency is:

```text
Factory Design Capability D1-D4
        |
        v
Published FactoryModelVersion
        |
        v
Factory runtime readiness
        |
        v
Headless capacity/layout acceptance scenarios
        |
        v
Stable external-consumer contract
        |
        v
Factory-design game implementation
```

The game is a downstream proof that Arcogine is usable as an engine. It must not be the place where missing workload, execution, scheduling, session, observation, or transfer semantics are invented under UI pressure.

A CLI command, JUnit harness, or thin reference consumer may prove a readiness gate. That is engine verification, not game implementation.

## 2. Boundary with factory design

The [Factory Design Capability](factory-design-capability.md) owns model-side concerns:

```text
Product/operation definitions
Resource definitions and installed instances
Capability or explicit eligibility requirements
Semantic layout
Executability validation
Publication/model identity/provenance
Deterministic instantiation boundary
```

This plan owns runtime concerns:

```text
Production orders
Work items
Quantity execution semantics
Resource dispatch
Queues and assignments
Active operations
Transfers in progress
Runtime events/observations
Performance
Session control
```

Where a concern crosses the boundary, the published model owns stable input semantics and runtime owns changing consequences. For example:

```text
Model:   resource position and footprint
Runtime: transfer start/progress/completion caused by that layout
```

Runtime may derive indexes or compiled structures from the published model, but it does not author another factory model and never mutates the published version.

## 3. Charter and architecture alignment

This initiative supports the [Product Charter](../product/charter.md) by strengthening one executable model across design, simulation, verification, and future execution contexts.

It follows these constraints:

- Arcogine owns executable production semantics; consumers receive controlled commands, events, and purpose-specific observations.
- Mutable runtime state has one authoritative owner.
- Request, execution, and performance concerns remain distinguishable.
- Simulation behavior remains deterministic for identical published models, seeds, workloads, and ordered commands.
- Runtime contracts are established before transport-specific API shapes become public compatibility obligations.
- The [ISA-95 semantic mapping](../architecture/isa-95-semantic-mapping.md) is a modeling reference, not a requirement for full ISA-95 implementation or conformance.
- Game-specific scoring, rendering, progression, and player-economy concepts do not enter runtime semantics.

## 4. Readiness policy

Game implementation begins only after the design prerequisite and Gates 1-5 below are satisfied by headless evidence.

```text
Prerequisite  Published canonical factory model (Design D1-D4)
        ↓
Gate 1        Explicit workload and execution model
        ↓
Gate 2        Capability-based deterministic dispatch
        ↓
Gate 3        Consumer-neutral simulation session
        ↓
Gate 4        Stable observations and event envelopes
        ↓
Gate 5        Spatial runtime consequences
        ↓
Game consumer may begin
```

Distribution hardening follows the core gates. A first UI experiment may begin after these gates; a distributable client additionally requires the contract, recovery, persistence, and packaging work in Section 11.

## 5. Gate 1 — Explicit production workload and execution model

### 5.1 Goal

Separate immutable production intent from mutable execution and make factory workload independent of the economy demand loop.

The published factory model supplies product and operation definitions. Runtime introduces workload and execution concepts equivalent to:

```text
Production order
    immutable workload intent
    product, quantity, release time

Work item
    mutable execution state
    parent order
    current operation
    assignment
    timing/status

Performance observation
    what actually happened
```

These are conceptual responsibilities, not accepted Java type names.

### 5.2 Current problem

The current runtime compresses several concerns into `Job`:

- immutable order-side facts and mutable execution state live in one object;
- one order effectively corresponds to one execution object;
- quantity primarily affects commercial value rather than proportional production work;
- work originates through the economy demand loop rather than an explicit factory workload contract.

The canonical-model spike should be completed before this refactor so definition-model changes and execution-model changes do not happen at the same time.

### 5.3 Required behavior

Arcogine must support explicit production workload independently of pricing, stochastic demand, and sales agents.

Order intent remains immutable after acceptance. Execution progress belongs to work items or an equivalent execution aggregate. Completion/performance is derived from executed work rather than by mutating the original request into a result record.

The economy subsystem may remain one source of production orders, but the factory runtime must not depend on it as the only source.

### 5.4 Acceptance criteria

Gate 1 is satisfied when:

1. Runtime instantiates from a published factory model containing the relevant product/operation definitions.
2. A caller can submit or preload an explicit production order without enabling pricing, stochastic demand, or agents.
3. An order for 10 units consumes more production work than an otherwise identical order for 1 unit.
4. Immutable order intent is distinct from mutable work execution state.
5. Multiple work items can report progress against one production order when the selected quantity model requires it.
6. Order completion is derived from executed work and exposed through a supported observation.
7. The same model version, seed, and workload produce the same ordered result.
8. Existing economy-driven scenario behavior remains covered or is migrated deliberately.

## 6. Gate 2 — Capability-based deterministic dispatch

### 6.1 Goal

Use the published model's resource definitions, installed instances, and operation eligibility/capability semantics to select runtime capacity deterministically.

The model side owns facts equivalent to:

```text
Resource definition
    capabilities / nominal processing properties

Resource instance
    installed identity and definition

Operation requirement
    capability or explicit eligible-resource semantics
```

Runtime owns:

```text
resource availability
queue state
active work
assignment
dispatch decision
```

### 6.2 Current problem

A current routing step points to one concrete `MachineId`, so a second equivalent machine does not naturally participate in the route. The model migration spike may preserve that behavior initially through an explicit single-resource eligibility mapping; Gate 2 is where runtime behavior becomes flexible.

### 6.3 Deterministic dispatch

Resource selection must have a documented deterministic policy. A possible initial order is:

1. online and capable/eligible;
2. inside the operation's allowed resource pool if one exists;
3. lowest projected completion time;
4. lowest queue depth;
5. lowest stable resource ID as final tie-breaker.

This ordering is illustrative, not accepted architecture. The eventual policy should be recorded when it becomes hard to reverse.

### 6.4 Acceptance criteria

Gate 2 is satisfied when:

1. Runtime consumes model-side resource definitions and installed instances rather than redefining them.
2. Two equivalent eligible resource instances can both execute the same operation.
3. Both resources are used when workload justifies parallel capacity.
4. Equal candidates resolve reproducibly through a stable tie-break rule.
5. Adding a second compatible resource changes queueing, utilization, throughput, or completion time in an appropriate workload.
6. Removing/disabling one instance does not require changing the product definition.
7. Resource capability, operational status, and queue state remain distinct concepts.

## 7. Gate 3 — Consumer-neutral simulation session

### 7.1 Goal

Provide transport-independent runtime-control semantics before treating HTTP, SSE, CLI, or an embedded Java adapter as a stable external contract.

Conceptually, a session needs operations equivalent to:

```text
instantiate/load published model atomically
submit workload/command and receive accepted/rejected result
advance one event
advance until target simulation time
advance with maximum event bound
observe current runtime state
read produced events
reset deterministically
```

Model validation/publication belongs upstream. The session may verify that the supplied published model/version is usable, but it should not become a second model-authoring API.

### 7.2 Required command semantics

Every externally initiated runtime change returns a definite result containing at least:

- accepted/rejected status;
- stable result/rejection code;
- understandable diagnostic;
- affected entity identifiers when applicable;
- session/model provenance;
- events produced by the accepted command, or a cursor identifying them.

A rejected runtime command must not leave partial mutation.

### 7.3 Bounded advancement

Interactive consumers control presentation speed by requesting bounded simulated progress. Wall-clock sleeping and rendering cadence remain outside the simulation core.

The session should support enough bounded advancement for:

- event stepping;
- pause/resume presentation semantics;
- normal and accelerated presentation speeds;
- deterministic headless runs;
- protection against unbounded work monopolizing a caller.

### 7.4 Acceptance criteria

Gate 3 is satisfied when a non-graphical reference consumer can:

1. instantiate a published factory model version;
2. submit explicit production workload and receive structured results;
3. advance exactly one event;
4. advance to a selected simulated time with a maximum event bound;
5. inspect order, work-item, queue, and resource state;
6. reset and reproduce the same result;
7. identify the source model version throughout the session;
8. operate without Spring controllers, frontend DTOs, or mutable domain internals.

## 8. Gate 4 — Stable observations and event envelopes

### 8.1 Goal

Expose enough supported runtime information for a consumer to understand the simulation without reaching into internal stores or reconstructing authoritative state from undocumented events.

### 8.2 Minimum observation

The consumer-neutral observation should contain, directly or through purpose-specific projections:

```text
Session
    session/run ID
    model fingerprint
    model revision ID [optional/future]
    current simulated time
    run state
    latest event sequence

Resources
    instance ID
    definition ID
    operational status
    queue depth
    active work
    current operation
    expected completion time where available

Orders
    requested quantity
    released quantity
    completed quantity
    status

Work items
    parent order
    current operation
    assignment
    execution state
    start/end timing

Performance
    throughput
    lead time
    work in process/backlog
    utilization
```

Not every consumer must receive one universal state dump. Purpose-specific observations remain preferable where they preserve capability boundaries.

### 8.3 Event envelope

Every externally visible runtime event should have an envelope equivalent to:

```text
sequence
simulation time
event type
session ID
model fingerprint
model revision ID [optional/future]
affected entity IDs
payload
```

The sequence is monotonic within one session and makes order explicit independently of event timestamps.

### 8.4 Acceptance criteria

Gate 4 is satisfied when:

1. Every externally visible runtime entity has a stable identifier.
2. Observation state and emitted events agree about the same authoritative transition history.
3. Event ordering is explicit and reproducible.
4. A consumer can identify the active bottleneck using supported observations rather than internal stores.
5. Requested, assigned, started, completed, and reported states are distinguishable.
6. Runtime observations include source-model provenance.
7. API/UI DTOs remain outward projections and are not reused as domain decision inputs.
8. A fresh observation can reconstruct current view state without replaying the entire history.

## 9. Gate 5 — Spatial runtime consequences

### 9.1 Goal

Apply deterministic production consequences to semantic layout supplied by the published factory model.

The design/model side owns facts equivalent to:

```text
Factory floor dimensions
Resource position
Resource orientation
Resource footprint
Semantic transfer/connection inputs
```

Runtime owns consequences such as:

```text
transfer duration
transfer start/completion
work-item location/progress
layout-dependent completion time
```

A sufficient first transfer policy may be:

```text
transfer time = fixed handling time
              + Manhattan distance * ticks per cell
```

The exact metric remains an open runtime/model-policy decision until validated by a prototype.

### 9.2 Hierarchy is not layout

Resource scope and physical placement remain separate dimensions:

```text
Resource scope
    Factory -> Work Center / Resource Pool -> Resource Instance

Spatial layout
    Floor -> Position -> Footprint -> Transfer distance
```

Moving a resource changes spatial consequences without changing its identity or hierarchy membership. Changing pool/work-center membership does not implicitly move it.

### 9.3 Transfer state

Transfers should be represented through explicit runtime state and/or events such as:

```text
TransferStarted
TransferCompleted
```

The runtime owns transfer timing. A consumer may interpolate visual movement between authoritative times.

### 9.4 Acceptance criteria

Gate 5 is satisfied when:

1. Runtime consumes validated semantic layout from a published model rather than accepting editor-specific geometry directly.
2. Two published models with identical resources/workload/processing durations but different semantic positions produce different completion times when transfer distance differs.
3. Both results are deterministic.
4. Transfer time is observable and attributable through supported events/observations.
5. Moving a resource creates a new model version and changes transfer behavior without changing resource identity.
6. Resource-pool/hierarchy changes do not implicitly change spatial placement.
7. No pathfinding, worker, vehicle, aisle, or congestion model is required to satisfy the gate.

## 10. Headless engine acceptance scenarios

The gates are proven before a game client exists. Model variants are published through the design capability and instantiated as separate runtimes.

### 10.1 Capacity benchmark

```text
Product
    CUT -> ASSEMBLE -> INSPECT

Production order
    20 units

Published model A
    one cutter
    one assembler
    one inspector

Published model B
    two equivalent cutters
    one assembler
    one inspector
```

Expected evidence:

- both runs are deterministic;
- model B dispatches work to both cutters;
- cutter queueing, throughput, utilization, or total completion time changes;
- if assembly becomes the new bottleneck, supported observations expose it;
- no product definition is rewritten to name the second cutter.

### 10.2 Layout benchmark

```text
Same product definition
Same production order
Same resource definitions/instances
Same processing durations

Published model A
    resources close together

Published model B
    resources far apart
```

Expected evidence:

- processing work is identical;
- transfer duration differs;
- total completion time differs;
- transfer events/observations explain the difference;
- repeated runs reproduce each model's results;
- each run identifies the model version used.

## 11. Distribution hardening after the core gates

These capabilities are required before treating an external client as distributable, but they do not block the first headless readiness proof.

| Capability | Requirement |
|---|---|
| Public contract versioning | Version model, command, event, observation, and protocol schemas |
| Reliable event recovery | Support monotonic event IDs and a defined reconnect/resynchronization strategy |
| Exact checkpoint and restore | Persist source model identity, simulated time, runtime state, queues, active work, scheduler contents, random state, and event position |
| Sidecar lifecycle and packaging | Start, health-check, version-check, communicate with, and stop a bundled local runtime without requiring a separate Java installation |
| Compatibility tests | Keep consumer-contract fixtures that fail on accidental breaking changes |

A game save may wrap an Arcogine checkpoint with game-owned state. Arcogine does not own campaign progress, score, camera state, or user preferences.

## 12. Explicit non-goals

Engine readiness does not require:

- defining or persisting consumer drafts;
- a complete ISA-95 object model/hierarchy/transaction set/B2MML profile/conformance claim;
- Enterprise or Site entities without a concrete use case;
- personnel, worker skills, fatigue, or pathfinding;
- raw-material procurement/suppliers/BOM merely for this initiative;
- maintenance, failure, quality, or shift-management domains;
- live production connectivity or operational execution;
- dynamic mutation of published factory structure while work is in flight;
- a generic plugin framework;
- multiplayer/distributed simulation/remote hosting;
- game rendering, scoring, progression, narrative, tutorials, or player economy.

The objective is a consumer-ready deterministic production runtime over a published factory model, not a universal MES/digital-twin/game framework.

## 13. Delivery order and first runtime milestone

### 13.1 Prerequisite

Complete the behavior-preserving canonical-model seam from [Factory Design Capability](factory-design-capability.md):

```text
Scenario factory semantics
      -> FactoryModel
      -> validate/publish
      -> instantiate existing runtime
      -> preserve deterministic results
```

### 13.2 Runtime delivery order

1. Production-order/work-item separation and explicit workload.
2. Quantity consumes proportional production work.
3. Capability/eligibility-driven deterministic dispatch.
4. Consumer-neutral session and bounded advancement.
5. Stable runtime observations and event envelopes.
6. Spatial transfer consequences from published layout.
7. Public-contract, recovery, persistence, and packaging hardening.

### 13.3 First runtime milestone

> **Run a deterministic explicit production order through separate order intent and work execution without using the economy demand loop.**

Definition of done:

```text
Published model supplies product/operation definitions
Production order exists as runtime workload
Quantity creates proportional work
Work execution is distinct from order intent
Order progress is observable
Completion is deterministic
Economy and sales agent are optional workload sources
Existing scenario behavior remains covered
```

This milestone deliberately excludes changing the canonical-model boundary, layout consequences, public HTTP versioning, checkpointing, sidecar packaging, and game UI.

## 14. Open decisions and ADR triggers

| Decision | Trigger for resolution |
|---|---|
| Final aggregate/type boundaries for order and work execution | Gate 1 implementation |
| Unit work items versus capacity-consuming batches | Quantity prototype and expected scale |
| Capability pools versus explicit eligible-instance sets | Gate 2 scheduling/control requirements |
| Deterministic dispatch policy | First equivalent-resource benchmark |
| Session interface/module ownership | Gate 3 implementation |
| Tick/event-count advancement semantics | Interactive/headless responsiveness tests |
| Observation decomposition | First reference consumer and capability-boundary review |
| Spatial metric/transfer policy | Layout benchmark prototype |
| Public compatibility policy | Before external consumer contract publication |

The canonical model/run/runtime boundary is tracked by [ADR-0003](../architecture/decisions/0003-canonical-factory-model-boundary.md). Record additional accepted hard-to-reverse decisions as ADRs rather than expanding this plan into a decision log.

## 15. Documentation lifecycle

While this work is proposed, this file remains under `docs/planning/`.

As gates become implemented:

- update [`../architecture/overview.md`](../architecture/overview.md) with established runtime architecture;
- update [Factory Design Architecture](../architecture/factory-design.md) only as the model boundary becomes accepted/implemented;
- update the [ISA-95 semantic mapping](../architecture/isa-95-semantic-mapping.md) when manufacturing concepts change;
- update [`../reference/api.md`](../reference/api.md) only for implemented public behavior;
- add ADRs for durable execution, scheduling, session, persistence, and compatibility decisions;
- keep headless acceptance scenarios executable and version-controlled.

Once the readiness initiative is complete or abandoned, reduce this file to a concise historical outcome or retire it after durable decisions and current behavior are represented in authoritative locations.
