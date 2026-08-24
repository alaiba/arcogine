# Factory-Design Game Consumer Initiative

> **Status:** Proposed  
> **Scope:** Arcogine support for a separate factory-design game consumer  
> **Authority:** Planning only; this document does not describe current capability or accepted architecture

## 1. Initiative summary

This initiative explores a separate, single-player factory-design game that uses Arcogine as its authoritative production-simulation engine. The game owns rendering, interaction, progression, scoring, and game content. Arcogine owns the executable production model, deterministic simulation, operational state, events, and observations.

The product hypothesis is deliberately narrow:

```text
Design a factory
        ↓
Submit an executable model
        ↓
Simulate production
        ↓
Inspect queues, transfers, and bottlenecks
        ↓
Revise the design and compare the result
```

The first outcome is a vertical slice proving that machine capacity and physical arrangement create understandable, reproducible trade-offs. It is not a commitment to a campaign, a general-purpose game framework, or a complete factory-management game.

## 2. Charter alignment

The initiative operates under the [Product Charter](../product/charter.md):

- It exercises the **Design**, **Understand**, **Simulate**, and **Improve** modes over one executable production model.
- The model submitted by the game is the model Arcogine validates and simulates; the game must not maintain a second operational truth.
- The session is explicitly hypothetical simulation state, never live production state.
- The game is a consumer of Arcogine, not a redefinition of Arcogine as a game.
- Game-specific concepts must not leak into Arcogine merely because this consumer needs them.

This is consistent with the current [architecture](../architecture/overview.md): the simulation core remains headless and deterministic, while purpose-specific interfaces consume observations and submit controlled actions.

## 3. Minimal vertical slice

The minimum demonstrator is a factory-layout challenge in which the player receives a fixed production contract, places machines on a bounded floor, runs the factory, observes operational behavior, and redesigns the layout until the contract is completed within its constraints.

### 3.1 Game requirements

| ID | Requirement | Acceptance criterion |
|---|---|---|
| G-01 | Bounded challenge | One level defines a floor, starting budget, fixed contract, deadline, and unambiguous success or failure condition. |
| G-02 | Factory editor | The player can place, move, rotate, and remove machine instances while editing a draft. Out-of-bounds and overlapping placement is rejected visibly. |
| G-03 | Small equipment catalogue | At least three machine types support three sequential operations. Multiple instances of a type can be installed. |
| G-04 | Capacity matters | Adding compatible capacity at a bottleneck can reduce queueing or completion time. |
| G-05 | Distance matters | Transfer time depends deterministically on the distance between consecutive production locations. |
| G-06 | Design-run loop | The player can validate a draft, start a run, pause, step, reset, and return to design mode. Structural edits are initially allowed only between runs. |
| G-07 | Legible operation | The player can see machine state, queue depth, active work, transfer progress, contract progress, and core operational KPIs. |
| G-08 | Diagnostic feedback | The most congested machine or process can be identified without reading the raw event log. |
| G-09 | Deterministic retry | The same submitted model, seed, and command sequence produces the same result. |
| G-10 | Actionable validation | Invalid models identify the affected entity or field and provide a stable error code and understandable explanation. |

### 3.2 Minimum content envelope

The first playable slice needs only:

- one bounded factory floor;
- one product;
- three ordered operations;
- three machine types;
- one receiving point and one shipping point;
- one fixed production contract;
- one tutorialized challenge;
- one score derived from completion, deadline performance, and unused game budget.

The level should support at least two credible designs, such as spending limited budget on parallel bottleneck capacity versus reducing transfer distance.

### 3.3 Initial editing rule

The game owns an editable `FactoryDraft`. Pressing Run submits a complete, validated model and starts a new Arcogine simulation context. Changing the draft invalidates the previous run and requires a reset or new run.

Live placement, movement, or removal of machines during an active run is not required for the vertical slice. This keeps model revision, in-flight work migration, and concurrent command semantics out of P0.

## 4. Consumer-engine responsibility boundary

| Concern | Owner |
|---|---|
| Rendering, animation, camera, input, audio, and assets | Game |
| Editable pre-run draft and editor undo history | Game |
| Machine purchase prices, construction budget, score, rewards, and progression | Game |
| Contract presentation, tutorials, narrative, and unlocks | Game |
| Executable factory geometry and production configuration | Arcogine |
| Products, operations, work items, queues, dispatch, and processing time | Arcogine |
| Operational consequences of transfer distance | Arcogine |
| Simulation clock, event ordering, deterministic random behavior, and replay inputs | Arcogine |
| Operational observations, validation results, and KPIs | Arcogine |
| Public consumer contract and compatibility rules | Shared boundary, owned by Arcogine |

Arcogine should not acquire concepts such as `Level`, `StarRating`, `Unlock`, `PlayerCurrency`, decorative furniture, or campaign progression. Conversely, the game should not infer authoritative production state by reproducing Arcogine's scheduling or queueing rules.

## 5. Required Arcogine capabilities

### 5.1 P0: required for the demonstrator

| Capability | Current gap | Expected area |
|---|---|---|
| Structured consumer model and validation | The current public interface loads complete TOML scenario text and reports coarse load errors. A game needs typed model input and structured diagnostics. | `product/types`, `product/interfaces/api` |
| Explicit production workload | Work currently originates through the economy demand loop. The demonstrator needs a fixed, externally supplied contract or order schedule. | factory domain and consumer interface |
| Quantity consumes capacity | One order currently creates one production job while quantity primarily affects commercial value. The demonstrator needs quantity to create proportional production work. | `product/domains/factory` |
| Equivalent-machine dispatch | A process segment currently names one concrete equipment ID. Adding another machine of the same type therefore does not naturally create parallel capacity. | `product/domains/factory`, scenario/model types |
| Minimal spatial semantics | Machines have no engine-semantic position or footprint, and transitions between routing steps have no transfer duration. | factory model and domain |
| Deterministic transfer events | The game needs observable transfer start/completion facts so it can animate movement without owning movement semantics. | simulation events and factory domain |
| Bounded advancement | The current run surface is oriented toward autonomous run-to-completion behavior. An interactive client needs deterministic advancement bounded by tick, event count, or both. | `product/simulation`, `product/interfaces/api` |
| Consumer-oriented observations | Current snapshots expose useful jobs, topology, and KPIs but not positions, transfers, operation timing, order progress, or a resumable event sequence. | `product/interfaces/api` |
| Scenario-level determinism tests | The new dispatch, quantity, and transfer rules need golden acceptance coverage. | domain and integration tests |

A sufficient P0 domain model distinguishes:

```text
Machine definition
    capability, footprint, processing parameters

Machine instance
    stable ID, definition, position, orientation, operational state

Product operation
    required capability, duration

Production order
    stable external ID, product, quantity, release tick

Work item
    order membership, current operation, location, status
```

#### Product, order, and workflow separation

The game makes an existing domain-model ambiguity actionable. Today `Job` carries both order-side facts (`productId`, quantity, agreed unit price, creation time) and execution-side facts (status, current step, assigned machine, completion time), while `ProductId` has no first-class runtime `Product` definition behind it. That is sufficient while an order and its production lifecycle are effectively one object, but it becomes a poor fit once the consumer must present a product definition, submit a fixed production contract, expand quantity into work, and observe multiple execution items independently.

For this initiative, the model should therefore separate three concerns conceptually:

```text
Product
    what is being made and which operations define it

Production order
    demand-side intent: external ID, product, quantity, release tick

Work item
    execution-side state: order membership, operation, location, status
```

The final Java type names are not fixed by this planning document; `SalesOrder`/`WorkOrder` are reasonable candidates but should only be adopted if they fit the implemented semantics. The important requirement is the ownership boundary: immutable order intent must not be conflated with mutable shop-floor execution state, and a product must be addressable as a runtime definition rather than only as a bare identifier. This is also compatible with the ISA-95-oriented vocabulary already used by Arcogine without requiring a speculative full ISA-95 object model.

Dispatch must be deterministic. The precise policy remains an open decision, but every tie must end in a stable ordering such as machine ID.

A deliberately simple initial transfer model is acceptable:

```text
transfer time = fixed handling time
              + Manhattan distance × ticks per cell
```

P0 does not require workers, vehicles, pathfinding, or congestion-aware routing.

### 5.2 P1: required before external distribution

| Capability | Purpose |
|---|---|
| Versioned public consumer contract | Allows the game to detect supported engine, model-schema, command, event, and observation versions. |
| Reliable event recovery | Adds monotonic event sequence IDs and snapshot/reconnect rules so a client can recover without guessing state. |
| Exact checkpoint and restore | Allows a game save to wrap an Arcogine checkpoint and reproduce the same subsequent simulation. |
| Sidecar lifecycle and packaging | Allows the game to start, health-check, communicate with, and stop a bundled local Arcogine runtime without requiring a separate Java installation. |
| Compatibility and contract tests | Prevents changes in Arcogine from silently breaking the external consumer. |

These capabilities are important for a distributable game but need not block the earliest engine/game integration experiment.

## 6. Explicit non-goals

The vertical slice does not require:

- workers, worker skills, fatigue, or individual pathfinding;
- aisles, rooms, doors, collision-aware navigation, or transport congestion;
- raw-material inventory, procurement, suppliers, or bills of material;
- maintenance, machine failures, setup optimization, or shift scheduling;
- dynamic pricing, stochastic market demand, or autonomous sales agents;
- research trees, campaign maps, procedural progression, or multiplayer;
- live structural reconfiguration while work is in flight;
- live-production connectivity or operational execution;
- a generic plugin framework for hypothetical future consumers;
- a renderer, game loop, or game-specific economy inside Arcogine.

These may become later game or platform initiatives, but they are not prerequisites for proving the consumer boundary.

## 7. Open architectural decisions

The planning document intentionally does not settle the following decisions:

| Decision | Question to resolve | Evidence needed |
|---|---|---|
| Consumer surface | Is HTTP/SSE sufficient, or should Arcogine also expose an in-process session facade? | Target game technology, packaging constraints, latency measurements, testability. |
| Model contract | Should the consumer submit an evolved scenario model or a distinct versioned factory-model contract? | Compatibility requirements and overlap with existing scenario use cases. |
| Quantity semantics | Should an order expand into unit work items or remain a capacity-consuming batch? | Required visualization, processing rules, and expected scale. |
| Resource selection | Should operations reference capability pools, explicit eligible-instance sets, or both? | Desired player control and dispatch complexity. |
| Transfer metric | Is Manhattan distance adequate for the first spatial model? | Prototype usability and whether layout choices remain understandable. |
| Advancement contract | Should clients advance by target tick, maximum events, or a combined bound? | Responsiveness and deterministic batching behavior. |
| Command/fact boundary | Which consumer actions require explicit accept/reject commands before facts are emitted? | Domain rules that can fail, conflict, or involve multiple authorities. |
| Compatibility promise | What constitutes a breaking change for model, command, event, and observation schemas? | Expected release cadence and external-consumer ownership. |

Once a decision becomes consequential and hard to reverse, record it as a Proposed or Accepted ADR under [`docs/architecture/decisions/`](../architecture/decisions/README.md). Do not use this planning document as a substitute for durable architectural rationale.

## 8. Delivery slices

These are dependency-oriented slices, not dates or release commitments.

### Slice 1: controlled workload

- Accept an explicit fixed workload without relying on stochastic demand.
- Make requested quantity produce proportional work.
- Expose order and work-item progress.

**Exit:** A headless scenario completes a known contract deterministically.

### Slice 2: flexible capacity

- Separate machine definitions from instances.
- Route operations to compatible installed capacity.
- Apply a deterministic dispatch policy.

**Exit:** Adding a second compatible machine can measurably change queueing and completion time.

### Slice 3: layout consequence

- Add bounded floor geometry, machine footprints, and positions.
- Add deterministic transfer duration and transfer events.

**Exit:** Two layouts containing the same machines produce different, reproducible completion times.

### Slice 4: interactive consumer contract

- Add structured validation, bounded advancement, and the game-oriented observation projection.
- Build a thin external client that can run the complete design-inspect-redesign loop.

**Exit:** The game client can explain the active bottleneck and compare two submitted designs without reading Arcogine internals.

### Slice 5: distribution hardening

- Version the public contract.
- Add resumable events, checkpoint/restore, and local sidecar lifecycle support.

**Exit:** A packaged client can save, restore, and resume a session reproducibly.

## 9. Acceptance criteria

The vertical slice is successful when:

1. A separate client submits a valid factory design without depending on Arcogine's internal Java classes.
2. The same machines arranged differently produce different deterministic completion times.
3. Adding compatible capacity at the actual bottleneck changes throughput, queueing, or completion time.
4. The client can identify the bottleneck from supported observations.
5. Invalid designs return structured, entity-specific diagnostics and do not partially mutate engine state.
6. Resetting the same model with the same seed reproduces the same ordered behavior and final result.
7. The game owns presentation, scoring, progression, and player economy; no game-specific concept enters Arcogine's domain model.
8. The implementation preserves the Events-State-Observations boundaries and authoritative state ownership described in the architecture documentation.

## 10. Documentation and retirement path

While the initiative is exploratory, this file remains under `docs/planning/` and must be read as a proposal rather than current capability.

As work becomes established:

- record durable, hard-to-reverse decisions as ADRs;
- update [`docs/architecture/overview.md`](../architecture/overview.md) for implemented architecture;
- update [`docs/reference/api.md`](../reference/api.md) for implemented public behavior;
- update user-facing concept documentation only for capabilities that actually ship;
- track implementation tasks as issues rather than expanding this file into a detailed backlog;
- keep detailed game UX, content, scoring, and progression specifications in the game consumer's repository.

Once the vertical slice is complete or abandoned, reduce this document to a concise historical outcome or remove it after its durable decisions and implemented behavior have been documented in their authoritative locations.
