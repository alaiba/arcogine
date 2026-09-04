# Factory-Design Game Consumer Initiative

> **Status:** Proposed  
> **Scope:** A separate, single-player factory-design game consuming Arcogine as its production engine  
> **Authority:** Planning only; this document does not describe current capability or accepted architecture  
> **Dependency:** Headless [Factory-Design Game Challenge Readiness](factory-design-game-challenge-readiness.md) may progress independently; playable/runtime-integrated game implementation begins only after the model-seam entry gate (§1.1 of [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md)), Engine Gates 1-5, and the accepted W1 decomposition contract in [ADR-0010](../architecture/decisions/0010-intra-order-execution-decomposition-and-work-item-identity.md) are implemented and proven  
> **Related:** [Factory-Design Game Challenge Readiness](factory-design-game-challenge-readiness.md), [Factory-Design Game Vertical Slice](factory-design-game-vertical-slice.md), [Factory Design Architecture](../architecture/factory-design.md), [Factory Design Capability](factory-design-capability.md), [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md), [ADR-0009](../architecture/decisions/0009-deterministic-dispatch-closure-and-work-decomposition-boundary.md), [ADR-0010](../architecture/decisions/0010-intra-order-execution-decomposition-and-work-item-identity.md), [ISA-95 Semantic Mapping](../architecture/isa-95-semantic-mapping.md)

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

The first outcome is a vertical slice proving that capacity and physical arrangement create understandable, reproducible trade-offs. It is not a commitment to a campaign, generic game framework, or complete factory-management game. The concrete product thesis, player loop, reference challenge, and product-level success criteria live in [Factory-Design Game Vertical Slice](factory-design-game-vertical-slice.md); this document owns the consumer boundary, readiness dependencies, and ownership constraints.

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
- deterministic decomposition of one accepted quantity-`N` order into independently dispatchable work items under the accepted W1 contract;
- capability/eligibility-based deterministic dispatch;
- consumer-neutral session and bounded advancement;
- supported observations and externally visible runtime events;
- deterministic transfer/runtime consequences of semantic layout;
- later recovery, checkpoint, compatibility, and sidecar hardening.

ADR-0009 records why decomposition is separate from Gate 2 dispatch. ADR-0010 resolves the W1 architecture: `Order` remains immutable production intent and the aggregate correlation identity; `JobId` identifies independently dispatchable work items; W1 creates one unit-quantity `Job` per requested quantity unit; aggregate progress/completion remains order-level; and exactly one `OrderCompleted` event is produced for the order. This design is accepted but is not runtime evidence until implemented and proven by the Engine track.

A CLI command, JUnit harness, or thin reference consumer used to prove either upstream plan is not game implementation.

### 2.3 Challenge readiness may progress in parallel

[Factory-Design Game Challenge Readiness](factory-design-game-challenge-readiness.md) owns game-side challenge semantics that can be proven headlessly without a playable or runtime-integrated game consumer:

- challenge identity and content/rules versioning;
- game-owned equipment catalogue, prices, availability, and construction-budget rules;
- candidate admissibility under challenge rules;
- evaluation-policy identity/version and deterministic challenge evaluation;
- evaluation provenance;
- attempt history and game-facing comparison;
- data-driven challenge fixtures and synthetic outcome fixtures.

C1-C5 in that plan may progress before Engine Readiness Gates 1-5 because they consume game-owned drafts and synthetic outcome facts rather than standing in for missing Arcogine production semantics. Headless challenge work must not simulate production, reconstruct queues or dispatch, infer missing runtime observations, or be used as evidence that an Engine Readiness gate is satisfied.

This exception applies only to the independent challenge/evaluation track. The playable/runtime-integrated game consumer still waits for the entry gates below.

### 2.4 Playable/runtime-integrated game entry gates

Before playable or runtime-integrated game implementation starts, Arcogine must have satisfied:

1. The model-seam entry gate (§1.1 of Factory Simulation Engine Readiness): the behavior-preserving canonical-model seam, narrower than the full D1-D4 acceptance criteria.
2. Gate 1: explicit production workload and separate execution semantics.
3. Gate 2: capability/eligibility-based deterministic resource dispatch for independently dispatchable work.
4. Gate 3: consumer-neutral simulation session with bounded advancement.
5. W1 as defined by ADR-0010: one fixed accepted quantity-`N` production requirement deterministically creates `N` unit-quantity sibling `Job`s under one `Order`, so equivalent bottleneck capacity can affect that same contract while aggregate progress/completion remains order-level.
6. Gate 4: stable observations and ordered external runtime events, including the `OrderId`/`JobId` execution correlation established by W1.
7. Gate 5: deterministic spatial runtime consequences.

The headless dispatch-capacity, fixed-contract work-decomposition, and layout benchmarks must pass before a game UI is used as evidence for those capabilities. Gate 2 remains complete independently of work decomposition; the extra prerequisite exists because the current reference challenge specifically requires both semantics together.

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
| G-04 | Capacity matters | Adding compatible capacity at a bottleneck can reduce queueing or completion time for the fixed production contract. |
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

The product-level reference challenge is described in [Factory-Design Game Vertical Slice](factory-design-game-vertical-slice.md). Its exact quantities, dimensions, prices, and deadlines are game-content parameters rather than Arcogine contracts.

### 4.3 Initial editing and publication rule

The game owns an editable `FactoryDraft` or equivalent authoring representation. It may contain temporary IDs, invalid intermediate states, selection, undo history, camera state, previews, and game-only metadata.

Pressing Run follows this semantic boundary:

```text
Game-owned draft
      |
 challenge admissibility
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

Challenge admissibility and Arcogine executability answer different questions. A draft may be canonically executable yet inadmissible under the selected challenge's catalogue, budget, fixed workload, or other game-owned constraints. Conversely, passing challenge admissibility does not prove Arcogine executability after projection.

Changing the draft creates another candidate design and therefore another published model version when accepted. It does not mutate the model underneath an active run.

Live placement/movement/removal during active work is not required for the vertical slice.

## 5. Consumer-Arcogine responsibility boundary

| Concern | Owner |
|---|---|
| Rendering, animation, camera, input, audio, assets | Game |
| Editable draft, local undo history, editor persistence | Game |
| Challenge identity and content/rules version | Game challenge layer |
| Equipment catalogue identity, availability, purchase/resale rules, construction budget | Game challenge layer |
| Candidate admissibility under challenge-owned rules | Game challenge layer |
| Evaluation-policy identity/version, success/failure, score/rating semantics | Game challenge layer |
| Evaluation provenance, attempt history, game-facing attempt comparison | Game challenge layer |
| Contract presentation, tutorials, narrative, unlocks | Game |
| Visual interpolation between authoritative event times | Game |
| Canonical production-system semantics | Arcogine design/model boundary |
| Shared executability validation | Arcogine design/model boundary |
| Published model identity/version/provenance | Arcogine design/model boundary |
| Products, operations, resource definitions/instances, semantic layout | Arcogine canonical model |
| Orders, order-level execution progress, child Jobs/work items, queues, dispatch, processing, transfers in progress | Arcogine runtime |
| Simulation clock, event ordering, deterministic random behavior | Arcogine runtime |
| Runtime observations and KPIs | Arcogine runtime |
| Model/command/event/observation compatibility contracts | Arcogine |
| Combined save file | Game wrapper around Arcogine checkpoint plus game-owned state |

Arcogine should not acquire `Level`, `StarRating`, `Unlock`, `PlayerCurrency`, challenge-evaluation policy, game-attempt history, decorative furniture, campaign progress, or tutorial steps. The game should not reproduce validation, workload decomposition, scheduling, queueing, dispatch, transfer, or KPI semantics that Arcogine owns.

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

### 6.5 Game economy and challenge evaluation

The initial game economy may include starting cash, purchase/resale prices, contract payout, deadline bonus/penalty, and scenario score.

These are game rules. They do not extend Arcogine Finance unless a separate product-level use case creates a genuine engine requirement.

Challenge identity/version, admissibility, evaluation-policy identity/version, score/success semantics, and attempt provenance/history are specified in [Factory-Design Game Challenge Readiness](factory-design-game-challenge-readiness.md). Challenge evaluation may interpret authoritative Arcogine outcome facts but must not recreate production semantics to derive facts Arcogine did not report.

The fixed challenge workload remains one game-owned production requirement. The game does not split it into multiple Arcogine Orders merely to obtain parallel capacity. ADR-0010 assigns decomposition to Arcogine: quantity `N` becomes `N` deterministic unit-quantity sibling `Job`s under the accepted `Order`; the game may visualize those work items and aggregate progress, but it does not choose their count, identity, release order, or dispatch.

### 6.6 Persistence and local runtime

For a prototype, the game may save its draft and rerun from the beginning.

Before external distribution it should wrap an exact Arcogine checkpoint with game-owned progress/settings, verify contract compatibility, manage a bundled local runtime if using a sidecar, and recover through Arcogine's supported resynchronization contract.

## 7. Explicit non-goals

The vertical slice does not require:

- live machine micromanagement as the primary game loop;
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

Canonical model, order/work semantics, W1 decomposition/work-item identity, dispatch, session behavior, event envelopes, and transfer rules are upstream decisions and do not belong here.

## 9. Playable/runtime-integrated game implementation entry criteria

Headless Challenge Readiness C1-C5 is explicitly outside this entry gate and may proceed earlier under the constraints in §2.3. Playable or runtime-integrated game implementation may begin when all of the following are true:

1. The model-seam entry gate (§1.1 of [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md)) is satisfied.
2. Gates 1-3 in [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md) are satisfied, including Gate 2 at its deterministic-dispatch boundary.
3. ADR-0010 is implemented: a single quantity-20 accepted `Order` creates exactly 20 independently dispatchable unit-quantity sibling `Job`s with deterministic identities/order, explicit aggregate progress, and exactly one aggregate order completion.
4. Gates 4-5 in [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md) are satisfied after accounting for the `OrderId`/`JobId` execution correlation introduced by W1.
5. A game-like draft can project into the canonical model and publish a `FactoryModelVersion` without using mutable runtime classes.
6. The headless capacity benchmark proves equivalent-resource dispatch, and the fixed-contract decomposition benchmark proves the current reference workload can actually use that capacity without game-authored order splitting.
7. The headless layout benchmark proves deterministic transfer consequences across distinct published model versions.
8. A reference consumer can instantiate, submit workload, advance, observe, and reset without Arcogine internals.
9. Model and runtime observation contracts are stable enough for a prototype consumer.
10. No unresolved game requirement requires the client to reproduce authoritative Arcogine logic.

## 10. Vertical-slice acceptance criteria

The game vertical slice is successful when:

1. The player can author a draft and publish a valid factory design without using Arcogine tooling directly.
2. The resulting run identifies the exact published model version.
3. A fixed production contract runs independently of pricing, stochastic demand, and autonomous sales behavior.
4. Quantity represents real production workload rather than primarily commercial value.
5. The same resources arranged differently produce different deterministic completion times where transfer distance differs.
6. Adding compatible capacity at the actual bottleneck changes throughput, queueing, utilization, or completion time for the same fixed production contract; the game does not achieve this by rewriting the contract as artificial independent Orders.
7. The player can identify the bottleneck and major delay sources from supported observations presented by the game.
8. Invalid drafts display structured diagnostics and do not publish or partially mutate runtime state.
9. Retrying the same published model with the same seed/workload/commands reproduces behavior and result.
10. The game owns presentation, scoring, progression, and player economy; no game-specific concept enters Arcogine's canonical/runtime model.
11. The client depends only on supported model, command, event, observation, checkpoint, and lifecycle contracts.
12. ISA-95 guidance is used where relevant without claiming conformance.

## 11. Documentation lifecycle

While exploratory, this file remains under `docs/planning/`.

As work becomes established:

- keep canonical-model work in [Factory Design Capability](factory-design-capability.md) until it becomes accepted/current architecture;
- keep runtime work/evidence in [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md);
- keep headless challenge/evaluation work in [Factory-Design Game Challenge Readiness](factory-design-game-challenge-readiness.md);
- keep the concrete first-playable product hypothesis in [Factory-Design Game Vertical Slice](factory-design-game-vertical-slice.md);
- record hard-to-reverse decisions as ADRs;
- update [`../architecture/overview.md`](../architecture/overview.md), [Factory Design Architecture](../architecture/factory-design.md), the [ISA-95 mapping](../architecture/isa-95-semantic-mapping.md), and [`../reference/api.md`](../reference/api.md) only as behavior becomes accepted/implemented;
- track game implementation in issues or the game consumer repository;
- keep detailed game UX/content/scoring/art/progression outside Arcogine.

Once the vertical slice is complete or abandoned, reduce this document to a concise historical outcome or retire it after durable decisions and implemented behavior are represented in authoritative documentation.
