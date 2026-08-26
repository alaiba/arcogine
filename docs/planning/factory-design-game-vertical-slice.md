# Factory-Design Game Vertical Slice

> **Status:** Proposed  
> **Scope:** Product hypothesis for the first playable factory-design game slice  
> **Authority:** Planning only; this document does not describe current Arcogine capability or accepted architecture  
> **Related:** [Factory-Design Game Consumer Initiative](factory-design-game-consumer.md), [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md), [Factory Design Capability](factory-design-capability.md), [Product Charter](../product/charter.md)

## 1. Product thesis

The first Arcogine game is a deterministic factory-design puzzle: given a fixed production contract, floor, budget, and deadline, the player chooses capacity and spatial arrangement, simulates the published design, diagnoses bottlenecks and transfer losses from supported observations, and iterates toward a better factory.

The game is not initially a general factory-management simulation. Its first purpose is to prove that Arcogine can make production-system design trade-offs understandable, reproducible, and engaging.

The player's primary loop is:

```text
Understand contract
        |
        v
Design factory
        |
        v
Publish design
        |
        v
Run production
        |
        v
Diagnose queues, utilization, transfers, and bottlenecks
        |
        v
Revise design
```

The central optimization tension is:

```text
processing capacity
        versus
spatial efficiency
        versus
capital efficiency
```

A strong design places enough eligible capacity at the actual bottleneck while avoiding unnecessary transfer time and equipment cost.

## 2. Player fantasy

The player acts as a production-system designer or industrial engineer rather than as a live shift operator.

The core question is:

> Can this factory design execute a known production requirement within its time and budget constraints?

The player should form hypotheses about the production system, test them through deterministic simulation, inspect supported evidence, and revise the design. The game should reward understanding why a design works, not merely discovering a hidden scoring formula.

## 3. Core player decisions

### 3.1 Capacity

The player decides where additional compatible resources are worth their capital cost.

Adding capacity at a true bottleneck should be capable of reducing queueing or completion time. Adding capacity away from the bottleneck may produce little or no benefit. Solving one bottleneck may expose another.

### 3.2 Resource eligibility and dispatch

The player chooses installed capability. The player does not author a separate product routing for each individual equivalent machine.

Arcogine owns deterministic assignment of work among eligible resources. The game presents the consequences of the installed resource pool without reproducing dispatch logic.

### 3.3 Spatial arrangement

Resource placement affects deterministic transfer time. Shorter production flows may outperform a more expensive design with additional capacity under some constraints.

The first spatial model is intentionally not a logistics simulation. Workers, vehicles, pathfinding, aisles, congestion, and traffic are not needed to make distance meaningful.

### 3.4 Capital efficiency

More equipment may improve capacity but consumes the game-owned construction budget. The strongest design is not necessarily the largest factory.

The initial score should reward successful completion, deadline performance, and unused construction budget.

## 4. Reference challenge

The first head-to-head product challenge should be concrete enough to anchor engine acceptance work and later playtesting.

```text
Contract
    Produce 20 units of Product A

Routing
    CUT -> ASSEMBLE -> INSPECT

Available equipment
    Cutter
    Assembly station
    Inspector

Constraints
    Factory floor: 12 x 10 cells
    Starting budget: 40,000 credits
    Deadline: 400 simulation ticks

Scoring
    Contract completion
    Deadline margin
    Unused construction budget
```

Exact numbers are level-content parameters, not engine contracts, and should be tuned through playtesting.

The level should admit at least two credible approaches, for example:

```text
Strategy A
    install a second cutter
    spend more capital
    reduce CUT queueing

Strategy B
    keep one cutter
    shorten resource-to-resource transfers
    preserve more capital
```

Neither approach should dominate under every constraint. The useful product question is whether the player can understand why one design performs better in a particular run.

## 5. What the player sees

The game should make Arcogine's supported runtime evidence legible without requiring the raw event log.

At minimum the player should be able to answer:

- Where is work waiting?
- Which operation or resource is the current bottleneck?
- Which resources are saturated or underused?
- How much production time is attributable to processing versus transfer?
- How far is the contract from completion?
- What changed relative to the previous published design and run?

A run should retain source-model provenance so the game can compare the result of one published design with another without treating mutable editor state as authoritative history.

The game may summarize, highlight, animate, and interpolate supported observations. It must not reconstruct a competing production model from presentation assumptions.

## 6. Relationship to Arcogine's existing economy simulation

Arcogine currently has pricing, stochastic demand, and autonomous sales behavior. Those capabilities are useful but are not part of this first game loop.

The initial factory-design game uses explicit production workload so that the design problem remains controlled and comparable across retries. Pricing, stochastic demand, and sales-agent behavior would confound the first capacity/layout experiments and are therefore non-goals for the vertical slice.

They may become later game modes if a concrete product hypothesis justifies them. Their current existence should not cause the first game to become an economy-management wrapper around the existing UI.

## 7. Engine dependencies and gameplay significance

The game remains downstream of the readiness gates. Each gate unlocks a specific player-facing mechanic while remaining an Arcogine capability rather than a game-specific implementation.

| Readiness capability | Gameplay unlocked |
|---|---|
| Canonical published model seam | Design -> publish -> run loop |
| Gate 1: explicit workload and execution semantics | Fixed production contracts with meaningful quantity |
| Gate 2: capability-based deterministic dispatch | Duplicate machines and capacity decisions |
| Gate 3: consumer-neutral simulation session | Pause, step, bounded acceleration, reset, deterministic retry |
| Gate 4: stable observations and event envelopes | Bottleneck diagnosis and legible production behavior |
| Gate 5: spatial runtime consequences | Layout optimization and transfer-cost trade-offs |

The game UI must not be used as the evidence that a gate is correct. Headless acceptance scenarios establish the engine semantics first.

## 8. Gate-oriented product acceptance examples

### 8.1 Workload

Given the same published factory, a production order for 20 units must require materially more production work than an otherwise identical order for one unit. Progress and completion must be observable without enabling pricing, stochastic demand, or agents.

### 8.2 Capacity

Compare two published designs under the same 20-unit workload:

```text
Model A
    1 cutter
    1 assembler
    1 inspector

Model B
    2 equivalent cutters
    1 assembler
    1 inspector
```

Both cutters in Model B must be eligible to execute CUT work when workload justifies it. The result should expose the resulting change in queueing, utilization, throughput, completion time, or bottleneck location.

### 8.3 Layout

Compare two published designs with the same resources, operation durations, and workload but different semantic positions. Where transfer distances differ, completion time must differ deterministically and the transfer contribution must be observable.

## 9. Explicit non-goals

The vertical slice does not require:

- live machine micromanagement as the primary game loop;
- dynamic pricing, stochastic demand, or autonomous sales agents;
- workers, skills, fatigue, or pathfinding;
- vehicles, conveyors, aisle routing, or congestion-aware transport;
- raw-material procurement, suppliers, BOM, or inventory optimization;
- maintenance, failures, or shift scheduling;
- research trees or broad campaign progression;
- multiplayer;
- live structural reconfiguration while production work is in flight;
- live-production connectivity or execution;
- a generic game framework or plugin system.

These omissions are intentional. They keep the first product hypothesis focused on capacity, flow, layout, and diagnosis.

## 10. Ownership boundary

The game owns:

- level definitions and contract presentation;
- editable draft UX and local undo/history;
- rendering, animation, input, audio, and presentation pacing;
- construction prices, budget, payout, score, rewards, and progression;
- tutorials, narrative, unlocks, and game-only persistence.

Arcogine owns:

- canonical production-system semantics;
- validation and publication of executable designs;
- model identity and provenance;
- production workload and execution semantics;
- queues, assignments, dispatch, processing, and transfers;
- simulation time and event ordering;
- supported runtime observations and KPIs;
- deterministic consequences of capacity and semantic layout.

Game-specific concepts must not enter Arcogine merely because they make the vertical slice easier to implement.

## 11. Vertical-slice success criteria

The product hypothesis is proven when:

1. A player can design and publish a valid factory without using Arcogine tooling directly.
2. A fixed production contract can run independently of pricing, stochastic demand, and sales agents.
3. Quantity represents real production workload rather than only commercial value.
4. Adding compatible capacity at the actual bottleneck can materially change the result.
5. Different valid spatial arrangements can materially change deterministic transfer and completion behavior.
6. The player can identify the bottleneck and major delay sources from supported observations.
7. Retrying the same published model with the same seed, workload, and ordered commands reproduces the result.
8. A new published design can be compared with the previous design and its run using retained model provenance.
9. At least two credible solutions exist for the reference challenge, creating an understandable capacity/layout/cost trade-off.
10. Game-specific scoring and presentation remain outside Arcogine's authoritative production semantics.

## 12. Documentation boundary

This document owns the product hypothesis for the first playable slice. It does not define canonical model fields, runtime APIs, dispatch policy, event envelopes, transfer formulas, or compatibility contracts.

Those remain in the upstream architecture and capability/readiness documents. Detailed art direction, content expansion, campaign design, and distribution planning should remain outside Arcogine until the vertical slice provides evidence that those investments are justified.
