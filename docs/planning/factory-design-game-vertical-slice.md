# Factory-Design Game Vertical Slice

> **Status:** Proposed  
> **Scope:** Product hypothesis for the first playable factory-design game slice  
> **Authority:** Planning only; this document does not describe current Arcogine capability or accepted architecture  
> **Related:** [Factory-Design Game Consumer Initiative](factory-design-game-consumer.md), [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md), [Factory Design Capability](factory-design-capability.md), [Product Charter](../product/charter.md)

## 1. Product thesis

The first game built on Arcogine is a deterministic factory-design puzzle: given a fixed production contract, floor, budget, and deadline, the player chooses capacity and spatial arrangement, simulates the published design, diagnoses bottlenecks and transfer losses from supported observations, and iterates toward a better factory.

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

### 3.4 Capital efficiency

More equipment may improve capacity but consumes the game-owned construction budget. The strongest design is not necessarily the largest factory.

The initial score should reward successful completion, deadline performance, and unused construction budget.

## 4. Reference challenge

The first product challenge should be concrete enough to anchor later playtesting.

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

## 5. Player evidence and attempt comparison

The game should make Arcogine's supported runtime evidence legible without requiring the raw event log.

At minimum the player should be able to answer:

- Where is work waiting?
- Which operation or resource is the current bottleneck?
- Which resources are saturated or underused?
- How much production time is attributable to processing versus transfer?
- How far is the contract from completion?
- How did this attempt's outcome compare with the previous attempt?

For the vertical slice, design-to-design comparison does **not** require Arcogine's deferred D5 semantic-comparison capability. The game may retain immutable game-owned draft snapshots for each attempt and pair them with the published model provenance and supported run outcomes. It can therefore show player-authored design differences from its own snapshots and compare authoritative run results without claiming a canonical semantic diff between two published Arcogine models.

If a future product requirement needs Arcogine itself to explain semantic differences between published model versions, that requirement becomes a concrete trigger to revisit D5 in [Factory Design Capability](factory-design-capability.md).

## 6. Product success criteria

The product hypothesis is proven when:

1. The player understands the fixed production objective, budget, deadline, and available equipment without needing Arcogine-specific tooling knowledge.
2. Capacity investment creates an understandable trade-off rather than making "buy more machines" universally optimal.
3. Spatial arrangement creates an understandable performance trade-off rather than acting as decoration.
4. Solving one production bottleneck can expose another, encouraging diagnosis and redesign.
5. The player can identify the bottleneck and major delay sources from presented evidence rather than the raw event log.
6. A retry is useful as an experiment: the player can relate a changed game-owned draft to changed authoritative run outcomes.
7. At least two credible solutions exist for the reference challenge, creating a meaningful capacity/layout/cost trade-off.
8. Scoring rewards contract success, deadline performance, and capital efficiency without obscuring why one design performed better.

The consumer-readiness gates, ownership rules, integration acceptance criteria, and explicit non-goals for implementing this slice are defined in [Factory-Design Game Consumer Initiative](factory-design-game-consumer.md), not duplicated here.

## 7. Documentation boundary

This document owns only the concrete product hypothesis: player fantasy, loop, decisions, reference challenge, player-facing evidence, and product success criteria.

[Factory-Design Game Consumer Initiative](factory-design-game-consumer.md) owns the Arcogine/game responsibility boundary, upstream readiness dependencies, implementation entry criteria, integration acceptance criteria, and non-goals. [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md) owns the runtime gates themselves.

Detailed art direction, content expansion, campaign design, and distribution planning should remain outside Arcogine until the vertical slice provides evidence that those investments are justified.
