# Factory-Design Game Consumer Initiative

> **Status:** Proposed  
> **Scope:** A separate, single-player factory-design game consuming Arcogine as its production engine  
> **Authority:** Planning only; this document does not describe current capability or accepted architecture  
> **Dependency:** Game implementation begins only after Gates 1–5 in [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md) are satisfied  
> **Related:** [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md), [ISA-95 Semantic Mapping](../architecture/isa-95-semantic-mapping.md)

## 1. Initiative summary

This initiative explores a separate factory-design game that uses Arcogine as its authoritative deterministic production engine. The game owns rendering, interaction, progression, scoring, content, and player experience. Arcogine owns the executable production model, simulation clock, operational state, scheduling, events, observations, and deterministic consequences of factory design.

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

## 2. Upstream engine dependency

This consumer must not define Arcogine's missing factory semantics under UI pressure.

The upstream [engine-readiness plan](factory-simulation-engine-readiness.md) owns:

- product, production-order, work-item, and performance boundaries;
- resource definitions, installed instances, capabilities, and deterministic dispatch;
- consumer-neutral session and bounded advancement semantics;
- supported observations and externally visible event envelopes;
- spatial floor, footprint, and transfer-time semantics;
- headless capacity and layout acceptance scenarios;
- later versioning, recovery, checkpoint, and sidecar hardening.

This game document owns only the downstream consumer hypothesis, user-facing vertical slice, responsibility boundary, client-specific requirements, and game acceptance criteria.

A CLI command, JUnit harness, or thin reference client used to prove engine gates is not game implementation. The game should not begin until the engine can prove the relevant behavior headlessly.

### 2.1 Entry gates

Before game implementation starts, Arcogine must have satisfied:

1. a canonical factory domain model with explicit workload and proportional quantity semantics;
2. resource definitions, installed instances, capability-based eligibility, and deterministic dispatch;
3. a consumer-neutral simulation session with atomic validation and bounded advancement;
4. stable observations and ordered external events sufficient to explain bottlenecks;
5. deterministic spatial and transfer semantics.

The capacity and layout benchmark scenarios in the readiness plan must pass before a game UI is used as evidence for those capabilities.

## 3. Charter and semantic alignment

The initiative operates under the [Product Charter](../product/charter.md):

- it exercises the **Design**, **Understand**, **Simulate**, and **Improve** modes over one executable production model;
- the model submitted by the game is the model Arcogine validates and simulates;
- the game does not maintain a competing operational truth;
- the session is explicitly hypothetical simulation state, never live production state;
- the game is a consumer of Arcogine, not a redefinition of Arcogine as a game;
- game-specific concepts do not enter Arcogine merely because this consumer needs them.

The initiative also follows the repository's [ISA-95 Semantic Mapping](../architecture/isa-95-semantic-mapping.md) where it improves domain separation. It does not become an ISA-95 implementation project.

In particular:

- product or material definitions remain distinct from production orders and mutable work execution;
- resource definitions remain distinct from installed instances;
- requested, assigned, started, completed, and reported state remain distinguishable;
- equipment/resource hierarchy remains distinct from spatial floor geometry;
- approachable Arcogine aliases may be retained where they are clearer;
- the game makes no ISA-95 interchange or conformance claim.

## 4. Minimal vertical slice

The demonstrator is a factory-layout challenge in which the player receives a fixed production contract, places machines on a bounded floor, runs the factory, observes operational behavior, and redesigns the layout until the contract is completed within its constraints.

### 4.1 Game requirements

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
| G-10 | Actionable validation | Invalid drafts identify the affected entity or field and present the engine's stable diagnostic clearly. |

### 4.2 Minimum content envelope

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

### 4.3 Initial editing rule

The game owns an editable `FactoryDraft`. Pressing Run converts or submits that draft through Arcogine's supported model contract and starts a fresh simulation context.

Changing the draft invalidates the previous run and requires a reset or new run. Live placement, movement, or removal of machines during active work is not required for the vertical slice.

## 5. Consumer-engine responsibility boundary

| Concern | Owner |
|---|---|
| Rendering, animation, camera, input, audio, and assets | Game |
| Editable pre-run draft and editor undo history | Game |
| Machine purchase prices, construction budget, score, rewards, and progression | Game |
| Contract presentation, tutorials, narrative, and unlocks | Game |
| Visual interpolation between authoritative event times | Game |
| Executable factory geometry and production configuration | Arcogine |
| Products, orders, work items, queues, dispatch, processing, and transfer time | Arcogine |
| Simulation clock, event ordering, deterministic random behavior, and replay inputs | Arcogine |
| Operational observations, validation results, and KPIs | Arcogine |
| Model, command, event, observation, and compatibility contracts | Arcogine |
| Combined save file | Game wrapper around an Arcogine checkpoint plus game-owned state |

Arcogine should not acquire concepts such as `Level`, `StarRating`, `Unlock`, `PlayerCurrency`, decorative furniture, campaign progress, or tutorial steps. Conversely, the game should not reproduce Arcogine's scheduling, queueing, transfer, or KPI rules.

## 6. Client-specific requirements after engine readiness

### 6.1 Draft authoring

The game may maintain a user-friendly draft with temporary IDs, editor selection, invalid intermediate states, undo/redo history, and presentation metadata.

At validation or run time it must translate the draft into Arcogine's supported executable-model contract. The translation must be explicit and testable; the game must not silently invent production semantics that the engine does not represent.

### 6.2 Simulation control and pacing

The game consumes Arcogine's bounded advancement contract to provide:

- pause;
- single-event step;
- normal presentation speed;
- accelerated presentation speed;
- reset and deterministic retry.

Presentation speed controls how frequently and how far the client asks the engine to advance. It does not alter processing durations or make the simulation depend on wall-clock timing.

### 6.3 Visualization

The game renders supported observations and events rather than internal engine classes.

It may interpolate:

- resource operation progress;
- work-item transfers;
- queue movement;
- contract completion feedback.

Interpolation is visual only. Arcogine remains authoritative for operation start, completion, assignment, transfer, and simulated time.

### 6.4 Diagnostics

The client must make supported engine evidence understandable:

- queue depth by resource;
- utilization;
- current and projected completion;
- work in process;
- average lead time;
- transfer contribution;
- active bottleneck.

The game may summarize or highlight these observations, but it must not calculate a competing authoritative result from hidden assumptions.

### 6.5 Game economy

The initial game economy consists only of:

- starting cash;
- machine purchase prices;
- machine resale values;
- contract payout;
- deadline bonus or penalty;
- scenario score.

These are game rules. They do not extend Arcogine's Finance domain unless a separate product-level use case establishes a genuine engine requirement.

### 6.6 Persistence and local runtime

For a prototype, the game may save its editable draft and rerun from the beginning.

Before external distribution, it should:

- wrap an exact Arcogine checkpoint with game-owned progress and settings;
- verify engine/model/protocol compatibility;
- start and stop a bundled local Arcogine runtime if the chosen integration uses a sidecar;
- recover from event-stream interruption through the engine's supported resynchronization contract.

These engine capabilities are defined upstream; the game only orchestrates them.

## 7. Explicit non-goals

The vertical slice does not require:

- workers, worker skills, fatigue, or individual pathfinding;
- aisles, rooms, doors, collision-aware navigation, or transport congestion;
- raw-material inventory, procurement, suppliers, or bills of material;
- maintenance, machine failures, setup optimization, or shift scheduling;
- dynamic pricing, stochastic market demand, or autonomous sales agents;
- research trees, campaign maps, procedural progression, or multiplayer;
- live structural reconfiguration while work is in flight;
- live-production connectivity or operational execution;
- a generic plugin framework;
- a complete ISA-95 hierarchy, transaction model, interchange profile, or conformance claim;
- engine-domain design justified only by game presentation convenience.

These may become later game or platform initiatives, but they are not prerequisites for proving the consumer boundary.

## 8. Client-specific open decisions

These decisions belong to the game initiative after the engine contracts are established:

| Decision | Evidence needed |
|---|---|
| Rendering and input technology | Target platforms, team capability, packaging, and performance prototype |
| Sidecar versus in-process use of an available engine adapter | Supported Arcogine consumer surfaces and target game runtime |
| Draft-to-model translation boundary | Final executable model contract and editor usability prototype |
| Visual interpolation policy | Event/observation timing and desired presentation style |
| Game-save wrapper format | Final checkpoint contract and game progression needs |
| Contract scoring formula | Playtest evidence that it rewards meaningful factory trade-offs |
| Tutorial sequence | Observation of first-time users completing the vertical slice |

Engine-domain choices such as quantity semantics, resource capabilities, dispatch policy, session semantics, event envelopes, and transfer rules do not belong here; they are upstream decisions governed by the readiness plan and ADRs.

## 9. Game implementation entry criteria

Game implementation may begin when all of the following are true:

1. Gates 1–5 in [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md) are satisfied.
2. The headless capacity benchmark proves equivalent-resource dispatch and exposes the resulting bottleneck.
3. The headless layout benchmark proves deterministic transfer consequences.
4. A reference consumer can validate, load, advance, observe, and reset a session without using Arcogine internals.
5. The executable-model and observation contracts are stable enough for a prototype consumer, even if their external compatibility policy is not yet final.
6. No unresolved game requirement requires the client to reproduce authoritative engine logic.

## 10. Vertical-slice acceptance criteria

The game vertical slice is successful when:

1. The player can create and submit a valid factory design without using Arcogine tooling directly.
2. The same resources arranged differently produce different deterministic completion times.
3. Adding compatible capacity at the actual bottleneck changes throughput, queueing, utilization, or completion time.
4. The player can identify the bottleneck from supported observations presented by the game.
5. Invalid drafts display structured, entity-specific diagnostics and do not partially mutate engine state.
6. Resetting the same model with the same seed reproduces the same ordered behavior and final result.
7. The game owns presentation, scoring, progression, and player economy; no game-specific concept enters Arcogine's domain model.
8. The client depends only on supported model, command, event, observation, checkpoint, and lifecycle contracts.
9. The implementation follows the ISA-95 mapping policy where relevant without claiming or requiring conformance.

## 11. Documentation lifecycle

While the initiative is exploratory, this file remains under `docs/planning/`.

As work becomes established:

- keep engine work and readiness evidence in [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md) until it becomes current architecture;
- record hard-to-reverse engine decisions as ADRs;
- update [`docs/architecture/overview.md`](../architecture/overview.md), the [ISA-95 semantic mapping](../architecture/isa-95-semantic-mapping.md), and [`docs/reference/api.md`](../reference/api.md) only for implemented behavior;
- track client implementation as issues or in the game consumer's repository;
- keep detailed game UX, content, scoring, art, and progression specifications outside Arcogine.

Once the vertical slice is complete or abandoned, reduce this document to a concise historical outcome or retire it after its durable decisions and implemented behavior are represented in authoritative documentation.
