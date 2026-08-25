# Factory-Design Game Consumer Initiative

> **Status:** Proposed  
> **Scope:** A separate, single-player factory-design game consuming Arcogine as its production engine  
> **Authority:** Planning only; this document does not describe current capability or accepted architecture  
> **Dependency:** Game implementation begins only after the model-seam entry gate (§1.1 of [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md)) and Gates 1-5 in that same document are satisfied  
> **Related:** [Factory Design Architecture](../architecture/factory-design.md), [Factory Design Capability](factory-design-capability.md), [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md), [ISA-95 Semantic Mapping](../architecture/isa-95-semantic-mapping.md)

## 1. Initiative summary

This initiative explores a separate factory-design game that uses Arcogine as its authoritative production-system model and deterministic simulation runtime.

The game owns rendering, interaction, progression, scoring, content, and player experience. Arcogine owns the published executable production-system semantics, validation, simulation clock, operational state, scheduling, events, observations, and deterministic consequences of factory design.

```text
Design a factory
        ↓
Project the draft into Arcogine's canonical model
        ↓
Validate and publish a model version
        ↓
Instantiate and simulate production
        ↓
Inspect queues, transfers, and bottlenecks
        ↓
Revise the draft and publish another design
```

The first outcome is a vertical slice proving that capacity and physical arrangement create understandable, reproducible trade-offs. It is not a commitment to a campaign, generic game framework, or complete factory-management game.

## 2. Upstream dependencies

This consumer must not define Arcogine's missing factory semantics under UI pressure.

### 2.1 Factory design capability

[Factory Design Capability](factory-design-capability.md) owns the model-side boundary:

- canonical factory model semantics;
- structured executability validation;
- immutable publication/model versioning boundary;
- model identity and provenance;
- deterministic runtime instantiation from a published model.

The game may own an editor-specific draft, but that draft is not the authoritative executable model.

### 2.2 Simulation engine readiness

[Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md) owns runtime concerns:

- production orders, quantity semantics, and work execution;
- capability/eligibility-based deterministic dispatch;
- consumer-neutral session and bounded advancement;
- supported observations and externally visible runtime events;
- deterministic transfer/runtime consequences of semantic layout;
- later recovery, checkpoint, compatibility, and sidecar hardening.

A CLI command, JUnit harness, or thin reference consumer used to prove either upstream plan is not game implementation.

### 2.3 Entry gates

Before game implementation starts, Arcogine must have satisfied:

1. The model-seam entry gate (§1.1 of Factory Simulation Engine Readiness): the behavior-preserving canonical-model seam, narrower than the full D1-D4 acceptance criteria.
2. Gate 1: explicit production workload and separate execution semantics.
3. Gate 2: capability/eligibility-based deterministic resource dispatch.
4. Gate 3: consumer-neutral simulation session with bounded advancement.
5. Gate 4: stable observations and ordered external runtime events.
6. Gate 5: deterministic spatial runtime consequences.

The headless capacity and layout benchmarks must pass before a game UI is used as evidence for those capabilities.

## 3. Charter and semantic alignment

The initiative operates under the [Product Charter](../product/charter.md):

- it exercises the **Design**, **Understand**, **Simulate**, and **Improve** modes over one executable production-system model;
- the design published by Arcogine is the design Arcogine instantiates and simulates;
- the game does not maintain a competing operational truth;
- simulation state is explicitly hypothetical;
- the game is a consumer of Arcogine, not a redefinition of Arcogine as a game;
- game-specific concepts do not enter Arcogine merely because this consumer needs them.

The initiative also follows [Factory Design Architecture](../architecture/factory-design.md) and the [ISA-95 Semantic Mapping](../architecture/isa-95-semantic-mapping.md):

- definitions remain distinct from runtime orders/work execution;
- resource definitions remain distinct from installed instances;
- requested, assigned, started, completed, and reported state remain distinguishable;
- resource hierarchy remains distinct from spatial layout;
- approachable Arcogine aliases may be retained where clearer;
- no ISA-95 interchange or conformance claim is made.

## 4. Minimal vertical slice

The demonstrator is a factory-layout challenge in which the player receives a fixed production contract, places machines on a bounded floor, publishes the design, runs the factory, observes operational behavior, and redesigns until the contract is completed within its constraints.

### 4.1 Game requirements

| ID | Requirement | Acceptance criterion |
|---|---|---|
| G-01 | Bounded challenge | One level defines a floor, starting budget, fixed contract, deadline, and unambiguous success/failure condition. |
| G-02 | Factory editor | The player can place, move, rotate, and remove resource instances in a game-owned draft. Out-of-bounds/overlap problems are shown clearly. |
| G-03 | Small equipment catalogue | At least three resource types support three sequential operations. Multiple instances of a type can be installed. |
| G-04 | Capacity matters | Adding compatible capacity at a bottleneck can reduce queueing or completion time. |
| G-05 | Distance matters | Transfer time depends deterministically on semantic layout. |
| G-06 | Design-run loop | The player can validate/publish a draft, start a run, pause, step, reset, and return to design mode. Structural edits are between runs initially. |
| G-07 | Legible operation | The player can see resource state, queues, active work, transfers, contract progress, and core KPIs. |
| G-08 | Diagnostic feedback | The bottleneck can be identified without reading the raw event log. |
| G-09 | Deterministic retry | Same published model, seed, workload, and commands produce the same result. |
| G-10 | Actionable validation | Invalid drafts display Arcogine's structured entity/path-specific diagnostics clearly. |

### 4.2 Minimum content envelope

The first playable slice needs only:

- one bounded factory floor;
- one product;
- three ordered operations;
- three resource/machine types;
- one receiving and one shipping point;
- one fixed production contract;
- one tutorialized challenge;
- one score derived from completion, deadline performance, and unused game budget.

The level should support at least two credible designs, such as parallel bottleneck capacity versus shorter transfer distance.

### 4.3 Initial editing and publication rule

The game owns an editable `FactoryDraft` or equivalent authoring representation. It may contain temporary IDs, invalid intermediate states, selection, undo history, camera state, previews, and game-only metadata.

Pressing Run follows this semantic boundary:

```text
Game-owned draft
      |
 project canonical semantics
      v
Arcogine validate + publish
      |
      v
FactoryModelVersion
      |
 instantiate
      v
Arcogine runtime
```

Changing the draft creates another candidate design and therefore another published model version when accepted. It does not mutate the model underneath an active run.

Live placement/movement/removal during active work is not required for the vertical slice.

## 5. Consumer-Arcogine responsibility boundary

| Concern | Owner |
|---|---|
| Rendering, animation, camera, input, audio, assets | Game |
| Editable draft, local undo history, editor persistence | Game |
| Machine purchase prices, construction budget, score, rewards, progression | Game |
| Contract presentation, tutorials, narrative, unlocks | Game |
| Visual interpolation between authoritative event times | Game |
| Canonical production-system semantics | Arcogine design/model boundary |
| Shared executability validation | Arcogine design/model boundary |
| Published model identity/version/provenance | Arcogine design/model boundary |
| Products, operations, resource definitions/instances, semantic layout | Arcogine canonical model |
| Orders, work items, queues, dispatch, processing, transfers in progress | Arcogine runtime |
| Simulation clock, event ordering, deterministic random behavior | Arcogine runtime |
| Runtime observations and KPIs | Arcogine runtime |
| Model/command/event/observation compatibility contracts | Arcogine |
| Combined save file | Game wrapper around Arcogine checkpoint plus game-owned state |

Arcogine should not acquire `Level`, `StarRating`, `Unlock`, `PlayerCurrency`, decorative furniture, campaign progress, or tutorial steps. The game should not reproduce validation, scheduling, queueing, transfer, or KPI semantics that Arcogine owns.

## 6. Client-specific requirements after upstream readiness

### 6.1 Draft authoring

The game may maintain a user-friendly draft representation. At validation/run time it projects only canonical semantic facts into Arcogine's supported model contract.

This projection must be explicit and testable. The game must not invent production semantics absent from the canonical model.

### 6.2 Simulation control and pacing

The game consumes Arcogine's bounded advancement contract to provide pause, single-event step, normal/accelerated presentation, reset, and deterministic retry.

Presentation speed controls how frequently/how far the client asks Arcogine to advance. It does not alter processing durations or make simulation depend on wall-clock timing.

### 6.3 Visualization

The game renders supported observations/events rather than internal engine classes. It may interpolate operation progress, transfers, queue movement, and completion feedback visually while Arcogine remains authoritative for state and simulated time.

### 6.4 Diagnostics

The client should make supported evidence understandable, including queue depth, utilization, completion progress, WIP, lead time, transfer contribution, and active bottleneck.

It may summarize/highlight observations but must not calculate a competing authoritative result from hidden assumptions.

### 6.5 Game economy

The initial game economy may include starting cash, purchase/resale prices, contract payout, deadline bonus/penalty, and scenario score.

These are game rules. They do not extend Arcogine Finance unless a separate product-level use case creates a genuine engine requirement.

### 6.6 Persistence and local runtime

For a prototype, the game may save its draft and rerun from the beginning.

Before external distribution it should wrap an exact Arcogine checkpoint with game-owned progress/settings, verify contract compatibility, manage a bundled local runtime if using a sidecar, and recover through Arcogine's supported resynchronization contract.

## 7. Explicit non-goals

The vertical slice does not require:

- workers, skills, fatigue, or pathfinding;
- aisles/doors/congestion-aware transport;
- raw-material procurement/suppliers/BOM;
- maintenance/failures/shift scheduling;
- dynamic pricing/stochastic demand/autonomous sales agents;
- research trees/campaign maps/multiplayer;
- live structural reconfiguration while work is in flight;
- live-production connectivity/execution;
- generic plugin framework;
- complete ISA-95 hierarchy/transactions/interchange/conformance;
- shared Arcogine editor/collaboration infrastructure;
- engine-domain design justified only by game presentation convenience.

## 8. Client-specific open decisions

| Decision | Evidence needed |
|---|---|
| Rendering/input technology | Target platforms, team capability, packaging, performance prototype |
| Sidecar versus supported in-process adapter | Available Arcogine consumer surfaces and target game runtime |
| Draft-to-canonical-model projection | Final model contract and editor usability prototype |
| Visual interpolation policy | Event/observation timing and desired presentation style |
| Game-save wrapper format | Final checkpoint contract and game progression needs |
| Scoring formula | Playtests showing meaningful factory trade-offs |
| Tutorial sequence | First-time-user observation |

Canonical model, order/work semantics, dispatch, session behavior, event envelopes, and transfer rules are upstream decisions and do not belong here.

## 9. Game implementation entry criteria

Game implementation may begin when all of the following are true:

1. The model-seam entry gate (§1.1 of [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md)) is satisfied.
2. Gates 1-5 in [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md) are satisfied.
3. A game-like draft can project into the canonical model and publish a `FactoryModelVersion` without using mutable runtime classes.
4. The headless capacity benchmark proves equivalent-resource dispatch and exposes the resulting bottleneck.
5. The headless layout benchmark proves deterministic transfer consequences across distinct published model versions.
6. A reference consumer can instantiate, submit workload, advance, observe, and reset without Arcogine internals.
7. Model and runtime observation contracts are stable enough for a prototype consumer.
8. No unresolved game requirement requires the client to reproduce authoritative Arcogine logic.

## 10. Vertical-slice acceptance criteria

The game vertical slice is successful when:

1. The player can author a draft and publish a valid factory design without using Arcogine tooling directly.
2. The resulting run identifies the exact published model version.
3. The same resources arranged differently produce different deterministic completion times where transfer distance differs.
4. Adding compatible capacity at the actual bottleneck changes throughput, queueing, utilization, or completion time.
5. The player can identify the bottleneck from supported observations presented by the game.
6. Invalid drafts display structured diagnostics and do not publish or partially mutate runtime state.
7. Retrying the same published model with the same seed/workload/commands reproduces behavior and result.
8. The game owns presentation, scoring, progression, and player economy; no game-specific concept enters Arcogine's canonical/runtime model.
9. The client depends only on supported model, command, event, observation, checkpoint, and lifecycle contracts.
10. ISA-95 guidance is used where relevant without claiming conformance.

## 11. Documentation lifecycle

While exploratory, this file remains under `docs/planning/`.

As work becomes established:

- keep canonical-model work in [Factory Design Capability](factory-design-capability.md) until it becomes accepted/current architecture;
- keep runtime work/evidence in [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md);
- record hard-to-reverse decisions as ADRs;
- update [`../architecture/overview.md`](../architecture/overview.md), [Factory Design Architecture](../architecture/factory-design.md), the [ISA-95 mapping](../architecture/isa-95-semantic-mapping.md), and [`../reference/api.md`](../reference/api.md) only as behavior becomes accepted/implemented;
- track game implementation in issues or the game consumer repository;
- keep detailed game UX/content/scoring/art/progression outside Arcogine.

Once the vertical slice is complete or abandoned, reduce this document to a concise historical outcome or retire it after durable decisions and implemented behavior are represented in authoritative documentation.
