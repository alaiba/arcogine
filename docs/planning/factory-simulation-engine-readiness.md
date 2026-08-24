# Factory Simulation Engine Readiness

> **Status:** Proposed  
> **Scope:** Prepare Arcogine's factory simulation for external consumers before game-specific implementation begins  
> **Authority:** Planning only; this document defines readiness gates, not current capability or accepted architecture  
> **Related:** [Factory-Design Game Consumer Initiative](factory-design-game-consumer.md), [ISA-95 Semantic Mapping](../architecture/isa-95-semantic-mapping.md), [Architecture Overview](../architecture/overview.md)

## 1. Purpose

Arcogine should become a consumer-ready deterministic production engine before a factory-design game is used to drive its domain model and runtime contracts.

The intended dependency is:

```text
Arcogine engine and runtime readiness
                ↓
Headless factory-design acceptance scenarios
                ↓
Stable external-consumer contract
                ↓
Factory-design game implementation
```

The game is a downstream proof that Arcogine is usable as an engine. It must not be the place where missing product, order, resource, execution, scheduling, observation, or spatial boundaries are invented under user-interface pressure.

A thin reference consumer, CLI command, or integration-test harness may be used to prove a readiness gate. That is engine verification, not game implementation.

## 2. Charter and architecture alignment

This initiative supports the [Product Charter](../product/charter.md) by strengthening one executable model across design, simulation, verification, and future execution contexts.

It follows these constraints:

- Arcogine owns the executable production semantics; consumers receive controlled commands, events, and purpose-specific observations.
- Mutable state has one authoritative owner.
- Definition, request, execution, and performance concerns remain distinguishable.
- Simulation behavior remains deterministic for identical models, seeds, and ordered commands.
- Domain semantics are established before transport-specific API shapes become public compatibility obligations.
- The [ISA-95 semantic mapping](../architecture/isa-95-semantic-mapping.md) is a modeling reference, not a requirement to implement the complete ISA-95 object model or claim conformance.
- Game-specific scoring, progression, rendering, and player-economy concepts do not enter the engine.

## 3. Readiness policy

Game implementation begins only after Gates 1–5 below are satisfied by headless acceptance evidence.

These gates are ordered by dependency, but implementation may overlap when doing so does not publish unstable contracts or collapse domain ownership.

```text
Gate 1  Canonical factory domain model
    ↓
Gate 2  Resource capabilities and deterministic dispatch
    ↓
Gate 3  Consumer-neutral simulation session
    ↓
Gate 4  Stable observations and event envelopes
    ↓
Gate 5  Spatial production semantics
    ↓
Game consumer may begin
```

Distribution hardening is a later requirement. A first UI experiment may begin after the five core gates; a distributable external client additionally requires the contract, recovery, persistence, and packaging work in Section 10.

## 4. Gate 1 — Canonical factory domain model

### 4.1 Goal

Establish an executable factory model that is not shaped by the current web interface, TOML serialization, stochastic demand loop, or game presentation.

The minimum conceptual model distinguishes:

```text
Product definition
    identity
    operations definition or revision

Operation definition
    required capability
    processing parameters

Resource definition
    capabilities
    nominal processing properties

Resource instance
    stable identity
    resource definition
    operational state

Production order
    immutable workload intent
    product, quantity, release time

Work item
    mutable execution state
    parent order, current operation, assignment, timing

Performance observation
    what actually happened
```

These are conceptual responsibilities, not accepted Java type names. The implementation may choose different names if their semantics and ownership remain explicit.

### 4.2 Current problem

The current model compresses several concerns into `Routing` and `Job`:

- `ProductId` has no first-class runtime product definition behind it;
- `Job` carries immutable order-side facts and mutable execution-side state;
- one order effectively corresponds to one execution object;
- quantity primarily affects commercial value rather than proportional production work;
- work originates through the economy demand loop rather than an explicit factory workload contract.

That model is adequate for the current experiment console but should not become the foundation of an external consumer contract.

### 4.3 Required behavior

Arcogine must support an explicit production workload independently of pricing, stochastic demand, and the sales agent.

Order intent remains immutable after acceptance. Execution progress belongs to work items or an equivalent execution aggregate. Completion and performance are derived from executed work rather than by mutating the original request into a result record.

The demand/economy subsystem may remain as one source of production orders, but the factory domain must no longer require it as the only source.

### 4.4 Acceptance criteria

Gate 1 is satisfied when:

1. A first-class product definition exists at runtime.
2. A caller can submit or preload an explicit production order without enabling pricing, demand generation, or agents.
3. Order quantity consumes proportional production capacity: an order for 10 units requires more work than an otherwise identical order for 1 unit.
4. Immutable order intent is distinct from mutable work execution state.
5. Multiple work items can report progress against one production order.
6. Order completion is derived from its work items and is observable through a supported projection.
7. The same model, seed, and workload produce the same ordered result.
8. Existing scenario behavior remains covered or is migrated deliberately rather than silently changed.

## 5. Gate 2 — Resource definitions, capabilities, and deterministic dispatch

### 5.1 Goal

Allow multiple installed resources to satisfy the same operation requirement without redefining the product route for every machine instance.

The engine must distinguish:

```text
Resource definition
    "This resource type can perform CUT"

Resource instance
    "Cutter #7 is installed and currently idle"

Operation requirement
    "This operation requires CUT"
```

A resource pool or work-center concept may be introduced if it owns real capacity, dispatch, scheduling, or reporting semantics. It should not be added merely to imitate an ISA-95 hierarchy.

### 5.2 Current problem

A current `RoutingStep` points to one concrete `MachineId`. This prevents a newly installed equivalent machine from participating naturally in the route and couples product definition to one resource instance.

### 5.3 Deterministic dispatch

Resource selection must have a documented, deterministic policy. A possible initial order is:

1. online and capable;
2. eligible for the operation or selected resource pool;
3. lowest projected completion time;
4. lowest queue depth;
5. lowest stable resource ID as the final tie-breaker.

This ordering is illustrative, not an accepted decision. The eventual policy must be recorded when it becomes hard to reverse.

### 5.4 Acceptance criteria

Gate 2 is satisfied when:

1. Resource definitions and installed instances are distinct.
2. Operation definitions express required capabilities or explicit eligible-resource sets rather than one mandatory machine instance.
3. Two equivalent resource instances can both execute the same operation.
4. Both resources are used when the workload justifies parallel capacity.
5. Equal candidates resolve reproducibly through a stable tie-break rule.
6. Adding a second compatible resource changes queueing, utilization, throughput, or completion time in an appropriate workload.
7. Removing or disabling one instance does not require redefining the product's operations.
8. Resource capability, operational status, and queue state remain distinct concepts.

## 6. Gate 3 — Consumer-neutral simulation session

### 6.1 Goal

Provide transport-independent session semantics before treating HTTP, SSE, CLI, or an embedded Java API as a stable external contract.

Conceptually, a session needs operations equivalent to:

```text
validate executable model
load model atomically
submit command and receive an accepted/rejected result
advance one event
advance until a target simulation time
advance with a maximum event bound
observe current state
read produced events
reset deterministically
```

The exact interface is an implementation decision. It must not require consumers to enqueue work, sleep for a wall-clock interval, and guess whether a snapshot reflects the requested command.

### 6.2 Required command semantics

Every externally initiated state change returns a definite result containing at least:

- accepted or rejected status;
- a stable result or rejection code;
- a human-readable diagnostic;
- affected entity identifiers when applicable;
- resulting model/session revision;
- events produced by the accepted command, or a cursor identifying them.

Validation and model loading must be atomic: an invalid model does not leave a partially mutated session.

### 6.3 Bounded advancement

Interactive consumers control presentation speed by requesting bounded simulated progress. Wall-clock sleeping and rendering cadence remain outside the simulation core.

The session should support enough bounded advancement to implement:

- event stepping;
- pause/resume semantics;
- normal and accelerated presentation speeds;
- deterministic headless runs;
- protection against an unbounded request monopolizing the caller.

### 6.4 Acceptance criteria

Gate 3 is satisfied when a non-graphical reference consumer can:

1. validate and load an executable model;
2. receive structured, entity-specific validation diagnostics;
3. submit an explicit production order or equivalent workload command;
4. advance exactly one event;
5. advance to a selected simulated time with a maximum event bound;
6. inspect order, work-item, queue, and resource state;
7. reset and reproduce the same result;
8. use the session without depending on Spring controllers, frontend DTOs, or mutable domain internals.

## 7. Gate 4 — Stable observations and event envelopes

### 7.1 Goal

Expose enough supported information for a consumer to understand the simulation without reaching into domain stores or rebuilding authoritative state from undocumented events.

### 7.2 Minimum observation

The consumer-neutral observation should contain, directly or through purpose-specific projections:

```text
Session
    session/run ID
    model revision
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
    expected completion time

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

Not every consumer must receive one universal state dump. Purpose-specific observations and projections remain preferable where they preserve capability boundaries.

### 7.3 Event envelope

Every externally visible event should have an envelope equivalent to:

```text
sequence
simulation time
event type
session ID
model revision
affected entity IDs
payload
```

The sequence is monotonic within one session and makes event order explicit independently of event timestamps.

### 7.4 Acceptance criteria

Gate 4 is satisfied when:

1. Every externally visible entity has a stable identifier.
2. Observation state and emitted events agree about the same authoritative transition history.
3. Event ordering is explicit and reproducible.
4. A consumer can identify the active bottleneck using supported observations rather than internal stores.
5. Requested, assigned, started, completed, and reported states are distinguishable.
6. API and UI DTOs remain outward projections and are not reused as domain decision inputs.
7. A reference consumer can recover its current view from a fresh observation without replaying the entire history.

## 8. Gate 5 — Spatial production semantics

### 8.1 Goal

Make physical arrangement an engine-owned production consequence after the resource and execution models are stable.

The minimal model is:

```text
Factory floor
    dimensions

Resource instance
    position
    orientation
    footprint

Transfer policy
    deterministic duration from source to destination
```

A sufficient first policy may be:

```text
transfer time = fixed handling time
              + Manhattan distance × ticks per cell
```

The exact metric remains an open decision until validated by an engine prototype.

### 8.2 Hierarchy is not layout

Resource scope and physical placement are separate dimensions:

```text
Resource scope
    Factory → Work Center / Resource Pool → Resource Instance

Spatial layout
    Floor → Position → Footprint → Transfer distance
```

A work center may aggregate capacity, scheduling, or reporting. It does not determine coordinates. Moving a resource must not change its identity or hierarchy membership implicitly, and changing resource-pool membership must not move it.

### 8.3 Transfer state

Transfers should be represented through explicit state and/or events such as:

```text
TransferStarted
TransferCompleted
```

The engine owns transfer timing. A consumer may interpolate visual movement between the authoritative start and completion times.

### 8.4 Acceptance criteria

Gate 5 is satisfied when:

1. A bounded floor, resource positions, orientations, and footprints are validated by the engine model.
2. Two models with identical resources, workloads, and processing durations but different positions produce different completion times when transfer distance differs.
3. Both results are deterministic.
4. Transfer time is observable and attributable through events or observations.
5. Moving a resource changes transfer behavior without changing its stable identity.
6. Changing hierarchy/resource-pool membership does not implicitly change spatial placement.
7. No pathfinding, worker, vehicle, aisle, or congestion model is required to satisfy the gate.

## 9. Headless engine acceptance scenarios

The gates are proven by engine-level scenarios before a game client exists.

### 9.1 Capacity benchmark

```text
Product
    CUT → ASSEMBLE → INSPECT

Production order
    20 units

Factory A
    one cutter
    one assembler
    one inspector

Factory B
    two equivalent cutters
    one assembler
    one inspector
```

Expected evidence:

- both runs are deterministic;
- Factory B dispatches work to both cutters;
- cutter queueing, throughput, utilization, or total completion time changes;
- if assembly becomes the new bottleneck, supported observations make that visible;
- no product route is rewritten to name the second cutter.

### 9.2 Layout benchmark

```text
Same product
Same production order
Same resources
Same processing durations

Layout A
    resources close together

Layout B
    resources far apart
```

Expected evidence:

- processing work is identical;
- transfer duration differs;
- total completion time differs;
- transfer events or observations explain the difference;
- repeated runs produce identical results for each layout.

## 10. Distribution hardening after the core gates

These capabilities are required before treating an external client as distributable, but they do not block the first headless readiness proof.

| Capability | Requirement |
|---|---|
| Public contract versioning | Version model, command, event, observation, and protocol schemas |
| Reliable event recovery | Support monotonic event IDs and a defined reconnect/resynchronization strategy |
| Exact checkpoint and restore | Persist model revision, simulated time, domain state, queues, active work, scheduler contents, random state, and event position |
| Sidecar lifecycle and packaging | Start, health-check, version-check, communicate with, and stop a bundled local runtime without requiring a separate Java installation |
| Compatibility tests | Keep consumer-contract fixtures that fail on accidental breaking changes |

A game save may wrap an Arcogine checkpoint with game-owned state. Arcogine does not own campaign progress, score, camera state, or user preferences.

## 11. Explicit non-goals

Engine readiness does not require:

- a complete ISA-95 object model, hierarchy, transaction set, B2MML profile, or conformance claim;
- Enterprise or Site entities without a concrete multi-site or integration use case;
- personnel, worker skills, fatigue, or individual pathfinding;
- raw-material inventory, procurement, suppliers, or bills of material;
- maintenance, failure, quality, or shift-management domains;
- live production connectivity or operational execution;
- dynamic mutation of factory structure while work is in flight;
- a generic plugin framework for hypothetical domains or consumers;
- multiplayer, distributed simulation, or remote hosting;
- game rendering, scoring, progression, narrative, tutorials, or player economy.

Engine-first work must remain bounded. The objective is a consumer-ready deterministic production engine, not a universal MES, digital-twin platform, or game framework in one initiative.

## 12. Delivery order and first milestone

### 12.1 Delivery order

1. Canonical product, production-order, and work-item model.
2. Resource definition/instance separation and capability-based deterministic dispatch.
3. Consumer-neutral session and bounded advancement.
4. Stable observations and event envelopes.
5. Spatial floor and transfer semantics.
6. Public-contract, recovery, persistence, and packaging hardening.

### 12.2 First implementation milestone

The first milestone is:

> Run a deterministic explicit production order through first-class product, production-order, and work-item concepts without using the economy demand loop.

Definition of done:

```text
Product definition exists
Production order exists
Quantity creates proportional work
Work items execute independently
Order progress is observable
Completion is deterministic
Economy and sales agent are optional
Existing scenario behavior remains covered
```

This milestone deliberately excludes layout, public HTTP versioning, checkpointing, sidecar packaging, and a game UI.

## 13. Open decisions and ADR triggers

| Decision | Trigger for resolution |
|---|---|
| Final names and aggregate boundaries for product, order, and work execution | Gate 1 implementation design |
| Unit work items versus capacity-consuming batches | Quantity semantics prototype and expected workload scale |
| Capability pools versus explicit eligible-instance sets | Gate 2 scheduling and player-control requirements |
| Deterministic dispatch policy | First equivalent-resource benchmark |
| Session interface shape and module ownership | Gate 3 implementation |
| Tick and event-count advancement semantics | Interactive and headless responsiveness tests |
| Observation decomposition | First reference consumer and capability-boundary review |
| Spatial metric and transfer policy | Layout benchmark prototype |
| Public compatibility policy | Before publishing a consumer contract outside the repository |

Record accepted, hard-to-reverse decisions as ADRs under [`docs/architecture/decisions/`](../architecture/decisions/README.md). Track implementation units as issues rather than expanding this document into a task-by-task backlog.

## 14. Documentation lifecycle

While this work is proposed, this file remains under `docs/planning/`.

As gates become implemented:

- update [`docs/architecture/overview.md`](../architecture/overview.md) with established architecture;
- update the [ISA-95 semantic mapping](../architecture/isa-95-semantic-mapping.md) when manufacturing concepts change;
- update [`docs/reference/api.md`](../reference/api.md) only for implemented public behavior;
- add ADRs for durable domain, scheduling, session, persistence, and compatibility decisions;
- keep headless acceptance scenarios executable and version-controlled.

Once the readiness initiative is complete or abandoned, reduce this file to a concise historical outcome or retire it after durable decisions and current behavior are represented in their authoritative locations.
