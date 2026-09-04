# ADR-0009: Deterministic dispatch closure and work-decomposition boundary

Status: Accepted
Date: 2026-08-28
Supersedes: ADR-0005
Amendment: 2026-09-03 — replaced transient Engine Readiness coordinates with semantic terminology; no semantic change

## Context

ADR-0005 established Arcogine's deterministic explicit-eligibility dispatch policy and correctly implemented the model/runtime boundary needed for equivalent eligible resources. Its first-slice scope also left two acceptance questions open for a different concern: whether one quantity-scaled production order must itself expose several concurrently dispatchable execution units.

That wording conflated two distinct production concerns:

```text
work decomposition
    what independently dispatchable execution units exist?

resource dispatch
    which eligible resource executes one such unit?
```

The implemented deterministic selector can only choose among resources for work that already exists. It cannot decide that one accepted order should become several lots, batches, jobs, or execution units without introducing additional production semantics around divisibility, identity, precedence, progress, and aggregate completion.

The repository now also has a concrete consumer that makes the decomposition question non-hypothetical. The factory-design game reference challenge defines a fixed contract to produce 20 units of Product A and expects a second cutter to be capable of reducing CUT queueing/completion time. Challenge workload identity is game-owned, but the game is explicitly prohibited from recreating Arcogine workload decomposition, queueing, or dispatch semantics. With the runtime shape at the time of this decision, one `submitWorkload(..., 20, ...)` created one accepted `Order` and one sequential `Job`, so the reference challenge could not obtain the intended parallel-capacity trade-off from a single fixed production contract.

ADR-0005 is therefore preserved as historical record and superseded by this decision. Its actual deterministic selector and pending-work behavior remain valid; this ADR changes the scope/closure interpretation and records the newly activated work-decomposition capability.

## Decision

### 1. Deterministic resource dispatch is complete at the dispatch boundary

Resource dispatch answers:

> Given an independently dispatchable unit of work and a published set of eligible resource instances, which resource executes it deterministically?

The existing implementation satisfies that contract:

- published multi-resource eligibility survives model validation and runtime assembly;
- multiple equivalent eligible resources can execute the same operation;
- independently dispatchable jobs can use equivalent resources concurrently;
- equal candidates resolve through the accepted deterministic ranking;
- offline/recovered resources are handled without rewriting product/operation definitions;
- eligibility, operational status, and queue state remain distinct.

Accordingly, the parallel-capacity acceptance requirements are satisfied by appropriate workloads containing sufficient independently dispatchable work. A requirement to split one accepted production order into several concurrent units is not part of resource selection itself.

The dispatch policy from ADR-0005 remains unchanged:

```text
eligible
    -> online
    -> able to accept work immediately
    -> shallowest queue
    -> lowest MachineId
```

The `pendingMultiEligible` cross-machine waiting-work semantics from ADR-0005 also remain unchanged.

### 2. Intra-order parallelism is a separate Engine work-decomposition capability

One accepted order may only use several equivalent resources concurrently if Arcogine first defines independently dispatchable execution units within that order.

That capability must explicitly decide at least:

- whether and where order quantity is divisible;
- execution-unit / lot / batch identity and lifecycle;
- whether one Order still maps to one Job or to a higher-level execution aggregate;
- operation precedence when units of one order progress independently;
- partial progress and aggregate completion semantics;
- event/observation correlation across Order, Job, and any new execution-unit identity;
- deterministic interaction with setup, material, transfer, or other constraints when those capabilities exist.

The implementation must not assume that one quantity unit automatically equals one execution object.

### 3. The current reference challenge activates this capability

The factory-design game is already a concrete supported-consumer requirement: its fixed 20-unit contract and second-cutter strategy require the engine to demonstrate that additional compatible bottleneck capacity can improve execution of that same fixed contract.

Therefore work decomposition is no longer merely a dormant future option. It is a **required pre-playable Engine capability for the current reference challenge**.

The game/challenge layer remains responsible only for saying what workload must be produced. It must not translate one challenge workload into several Arcogine Orders merely to manufacture parallelism unless a future, separately accepted contract explicitly assigns that mapping to the game. Arcogine owns the production execution decomposition needed to run one accepted production requirement correctly.

### 4. Work decomposition precedes stabilization of the supported observation/event contract for this consumer

For the currently activated reference challenge, the dependency is:

```text
explicit workload/execution baseline        complete
deterministic resource dispatch             complete
consumer-neutral session control            complete
        ↓
intra-order work decomposition
        ↓
stable observations and event envelopes
        ↓
spatial runtime consequences
        ↓
playable/runtime-integrated game
```

Work decomposition is placed before supported observation/event stabilization because it is expected to introduce or refine runtime execution identities and correlation semantics. The externally visible identifier, observation, and event contract should be stabilized after those production entities are known rather than frozen around a representation that immediately needs structural revision.

This ordering is specific to the current reference consumer. Deterministic resource dispatch itself remains complete independently of intra-order decomposition.

## Alternatives considered

### Keep single-order parallelism inside resource dispatch

Rejected. It makes a resource-selector capability responsible for inventing work units and obscures the fact that decomposition is a larger production-semantic decision.

### Represent the fixed game workload as multiple independent Arcogine Orders

Rejected for the current reference challenge. The challenge defines one fixed production requirement, and the game is not allowed to recreate Arcogine production execution semantics. Splitting that requirement into several Orders solely to make capacity scale would move an authoritative production decision into the consumer layer and change the meaning of `Order` without a separate accepted contract.

A future consumer may legitimately submit multiple independent Orders when its source workload genuinely contains multiple independent orders; that remains fully supported and is the correct proving workload for deterministic resource dispatch.

### Stabilize the supported observation/event contract before work decomposition

Rejected for the current vertical slice. Because decomposition is now a known pre-playable requirement and is likely to introduce new externally observable execution identities, stabilizing the observation/event contract first would create avoidable contract churn.

### Introduce a generalized lot/batch/scheduling framework now

Rejected. The work-decomposition capability should implement only the minimum semantics proven necessary by the reference challenge. Material lots, transfer batches, setup optimization, inventory allocation, and generalized scheduling remain separate unless implementation evidence proves they are inseparable.

## Consequences

- ADR-0005 remains historical evidence of the original first-slice decision and implementation scope.
- Deterministic resource dispatch can be considered complete without pretending that intra-order parallelism already exists.
- The current factory-design reference challenge has an explicit upstream Engine dependency instead of silently assuming one quantity-scaled Job can use parallel capacity.
- Work decomposition becomes the next semantic Engine prerequisite before stabilizing the supported observation/event contract for the playable vertical slice.
- The supported observation/event contract must account for whatever stable execution-unit identities/correlation the decomposition capability establishes.
- No runtime behavior changes merely by accepting this ADR; work decomposition still requires separate repository-grounded design/implementation.
- The accepted resource-selection ranking and queue-recovery behavior do not change.

## Charter alignment

This decision preserves Arcogine's ownership of executable production semantics. The game states the fixed production requirement and evaluates outcomes; Arcogine defines how accepted production intent becomes executable work and how that work is dispatched. The consumer does not invent a parallel workload representation merely to obtain a desired game result.
