# ADR-0010: Intra-order execution decomposition and work-item identity

Status: Accepted
Date: 2026-08-28
Amendment: 2026-09-03 — replaced transient Engine Readiness coordinates with semantic terminology; no semantic change

## Context

ADR-0009 separated two production concerns that had previously been conflated:

```text
work decomposition
    what independently dispatchable execution units exist?

resource dispatch
    which eligible resource executes one such unit?
```

Deterministic resource dispatch is complete at the dispatch boundary. The remaining decomposition question is therefore not how to rank eligible resources, but how one accepted production `Order` with quantity greater than one becomes independently dispatchable runtime work without moving authoritative production semantics into the game or another consumer.

The implementation at the time of this decision had one immutable `Order` and one mutable `Job` per accepted workload. Quantity was represented by repeating the product routing inside that one `Job`: a quantity-20 order for a three-step routing executed 60 task steps sequentially. This made quantity consume proportional production work, but it also meant one order could not use two equivalent resources concurrently because only one `JobId` existed to dispatch at a time.

The factory-design reference challenge makes this limitation concrete. The game owns one fixed requirement to produce 20 units of Product A through `CUT -> ASSEMBLE -> INSPECT`, and one credible player strategy is to install a second cutter. The game must not fabricate multiple Arcogine `Order`s merely to make that strategy effective. Arcogine therefore needs an accepted intra-order decomposition contract before the supported runtime observation/event contract stabilizes externally visible execution identities and event correlation.

The existing runtime already provides an important architectural seam:

- `Order` is immutable accepted production intent;
- `Job` is mutable executable work and already carries the identity used by machine queues, active-machine state, pending multi-eligible work, and task events;
- `Order` was deliberately modeled so that multiple jobs may later reference the same order;
- deterministic resource dispatch already operates correctly once several independently dispatchable `Job`s exist.

The design therefore needs to choose the smallest execution shape that enables the reference workload while preserving determinism, aggregate order semantics, and future room for real lot/batch concepts.

## Decision

### 1. One accepted Order owns one order-level execution aggregate and one or more Jobs

The accepted runtime shape is:

```text
Order
    immutable production intent
    OrderId
    product
    requested quantity
    created/released time
    commercial facts
        |
        v
Order execution aggregate
    identity: the same OrderId
    released quantity
    completed quantity
    completion time
        |
        +---- Job 1
        +---- Job 2
        +---- ...
        +---- Job N
```

The order-level execution aggregate does **not** receive a second independent identifier. `OrderId` already identifies the accepted production requirement and is sufficient to correlate aggregate progress and completion.

The aggregate may be represented internally by dedicated mutable state or by equivalent authoritative state owned by the factory runtime. The architectural requirement is the responsibility boundary, not a mandatory Java class name.

### 2. JobId is the independently dispatchable work-item identity

`JobId` becomes the identity of one independently dispatchable work item within an accepted order.

A `Job` owns mutable execution state for that work item:

- parent `OrderId` / referenced `Order`;
- deterministic ordinal within the order;
- execution quantity represented by the work item;
- current routing step;
- assignment / current machine;
- status and timing;
- completion state.

No new `ExecutionUnitId`, `LotId`, or `BatchId` is introduced for this unit-work decomposition contract. Machine queues, active-machine state, `pendingMultiEligible`, and `TaskStart` / `TaskEnd` continue to use `JobId` as their concrete execution identity.

### 3. Quantity N decomposes into N unit-quantity Jobs

For the first supported intra-order decomposition contract, accepting an `Order` with quantity `N` deterministically creates exactly `N` child `Job`s, each representing execution quantity `1`.

Each child `Job` traverses the product routing exactly once and independently:

```text
Order quantity = N
routing = step 0 -> step 1 -> ... -> step R-1

creates

Job ordinal 0: step 0 -> step 1 -> ... -> step R-1
Job ordinal 1: step 0 -> step 1 -> ... -> step R-1
...
Job ordinal N-1: step 0 -> step 1 -> ... -> step R-1
```

This reifies the `N` routing passes already implicit in the prior quantity-scaled single-Job implementation. It does not introduce an arbitrary batch size or invent material-lot semantics.

Every accepted child is released for dispatch as part of the atomic order-acceptance operation. `releasedQuantity == requestedQuantity` immediately after successful acceptance.

### 4. ordinalWithinOrder is deterministic ordering metadata, not identity

Each child work item has a zero-based immutable ordinal within its parent order, assigned in ascending order at acceptance.

The ordinal exists to make decomposition and replay ordering explicit and inspectable. It is not globally unique, is not a substitute for `JobId`, and must not be treated as a durable cross-session identifier.

Child creation, `JobId` allocation, and initial dispatch attempts occur in ascending `ordinalWithinOrder` order.

### 5. Aggregate order progress is quantity-based

The order-level execution aggregate tracks at least:

```text
requestedQuantity
releasedQuantity
completedQuantity
completedAt
```

For this unit-work decomposition contract:

- `requestedQuantity` comes from immutable `Order.quantity`;
- `releasedQuantity` is the number of child work items released for execution and equals requested quantity immediately after acceptance;
- `completedQuantity` increments exactly once when a child `Job` completes its final routing step;
- the order is complete exactly when `completedQuantity == requestedQuantity`;
- `completedAt` is the simulation time of that aggregate transition.

A child `Job` completing one intermediate routing step does not change completed quantity.

### 6. Each Job progresses through one routing independently

The prior quantity-scaled `Job.totalSteps = routing.stepCount() * order.quantity` representation is superseded for unit-work execution.

Each child `Job` instead has one routing traversal:

```text
Job.totalSteps = routing.stepCount()
Job.currentStep = routing-local execution progress
```

A job cannot begin routing step `k + 1` until its own step `k` completes. Sibling jobs of the same order have no additional precedence relationship in this contract and may therefore occupy different eligible resources concurrently.

No cross-job batching, synchronization barrier, split/merge rule, or transfer-batch constraint is introduced by this decision.

### 7. Existing deterministic dispatch policy is reused unchanged

Unit-work decomposition creates independently dispatchable work; it does not redefine resource selection.

Every released child `Job` is dispatched using the existing deterministic policy:

```text
eligible
    -> online
    -> able to accept work immediately
    -> shallowest queue
    -> lowest MachineId
```

`pendingMultiEligible` retains its existing semantics and continues to hold `JobId`-identified waiting work.

A second equivalent machine can therefore improve execution of one order only because decomposition supplies multiple dispatchable sibling jobs; the selector itself remains unchanged.

### 8. Order completion is emitted exactly once

Completing the final routing step of a child `Job` performs one aggregate progress transition.

Only the transition from:

```text
completedQuantity = requestedQuantity - 1
```

to:

```text
completedQuantity = requestedQuantity
```

may emit the order-completion event.

Exactly one `OrderCompleted` event is emitted for one accepted `Order`, regardless of child count.

To remove the ambiguity created by several `JobId`s belonging to one order, the completion payload carries explicit order identity while retaining the completing child correlation:

```text
OrderCompleted(
    OrderId orderId,
    JobId jobId,
    ProductId productId,
    long quantity,
    double unitPrice
)
```

`jobId` identifies the child whose final completion caused the aggregate order to become complete. `orderId` identifies the completed production requirement.

`TaskStart` and `TaskEnd` may retain their current `JobId`, `MachineId`, and routing-local `stepIndex` fields. The supported runtime-event envelope is a separate contract; this ADR does not require adding `OrderId` to every internal task event.

### 9. Order-level business and performance semantics remain order-level

Introducing several `Job`s underneath one `Order` must not silently redefine existing business/performance measures.

For this decomposition contract:

- backlog remains the count of incomplete accepted orders, not the count of child jobs;
- completed sales remains the count of completed orders;
- completed sales value is posted exactly once per completed order at the full order value;
- average lead time remains order lead time from order acceptance/release to aggregate order completion;
- throughput metrics that currently count completed orders remain order-count metrics unless a later explicit contract introduces unit-throughput measures.

Compatibility adapters that currently project full parent order quantity or value from every `Job` must be changed deliberately. In particular, no child job may independently cause the full order value to be posted or reported as if it were a separate sale.

### 10. FactoryRuntime keeps one workload-submission result and gains aggregate execution observation

`FactoryRuntime.submitWorkload(...)` continues to accept one production requirement and return one accepted `OrderId`.

A successful submission may now schedule initial events for multiple child jobs. A rejected submission remains atomic and must leave no partial accepted state:

- no `Order`;
- no order-execution aggregate;
- no child `Job`s;
- no machine assignment/queue mutation;
- no `pendingMultiEligible` entries;
- no command-produced runtime events.

The supported runtime observation surface must expose order-level execution progress in an equivalent shape to:

```text
OrderExecutionView(
    OrderId orderId,
    long requestedQuantity,
    long releasedQuantity,
    long completedQuantity,
    SimTime completedAt,
    boolean complete
)
```

The exact Java representation may differ, but a consumer must not need to infer aggregate completion by counting arbitrary child-job states itself.

### 11. Deterministic decomposition and replay are required

For the same published model, runtime state, seed, workload, and ordered commands, the decomposition contract must reproduce the same:

- child count;
- child ordinals;
- `JobId` allocation sequence;
- initial dispatch order;
- machine assignments;
- queue/pending ordering;
- task-event ordering;
- aggregate progress transitions;
- order-completion event.

The required ordering rules are:

1. create and allocate child jobs in ascending `ordinalWithinOrder` order;
2. attempt initial dispatch in that same order;
3. preserve existing per-machine FIFO queue semantics;
4. preserve existing deterministic `pendingMultiEligible` reconsideration semantics;
5. preserve `MachineId` as the final resource-selection tie-breaker;
6. preserve scheduler insertion order as the tie-breaker for events at the same simulation time.

Hash/set iteration order must not become a semantic tie-breaker.

### 12. Unit decomposition is the minimum supported policy, not a generalized batch model

Creating one lightweight `Job` per quantity unit changes resident execution-object scaling from approximately O(orders) to O(quantity), while event work was already O(quantity * routing steps) in the existing quantity-scaled implementation.

That cost is accepted because any coarser chunk size would be arbitrary without a domain input that actually defines lot or batch size. Decomposing according to available resource count would also be incorrect because it would couple work decomposition to resource dispatch and make work identity change when the factory design changes.

Implementation acceptance must therefore include a large-order non-functional benchmark (for example quantity 100,000) to measure memory and execution impact. That benchmark informs the supported materialization envelope; it is not itself the admission contract.

Before mutating runtime state, workload acceptance must deterministically verify that the requested quantity is within a supported child-materialization envelope. A quantity above that envelope must be rejected through the existing structured command-error semantics before creating the `Order`, order-execution aggregate, child `Job`s, assignments, pending work, or command-produced events. The concrete bound is an implementation/configuration/model decision and is intentionally not frozen by this ADR, but it must be deterministic for the same accepted model/runtime configuration and must be established from evidence rather than ambient heap availability or allocation failure.

This admission rule preserves the consumer-neutral session command contract's zero-partial-mutation rejection guarantee and prevents environment-dependent allocation failure from becoming a de facto workload limit. If benchmarking shows that the desired supported envelope is impractical with unit `Job` materialization, the follow-up design must address representation efficiency without silently inventing batch semantics.

A future accepted lot/batch capability may allow one `Job` to represent `executionQuantity > 1` when the production domain provides an explicit lot/batch rule. This decision deliberately leaves that seam open but does not define such a rule now.

## Alternatives considered

### Keep one parent Job and introduce a new ExecutionUnit identity below it

Rejected. The current runtime already treats `JobId` as the identity that machines, queues, pending work, and task events execute. Preserving `Job` as a parent aggregate would require threading a new identity through all of those structures while retaining two mutable execution layers with overlapping responsibilities. The concrete requirement does not justify that complexity.

### Use one Job per arbitrary chunk of quantity

Rejected. No accepted domain fact currently supplies a batch size. Choosing a fixed chunk size would invent production semantics; choosing a chunk size from resource count would incorrectly make decomposition depend on dispatch capacity.

### Keep one Job and allow one Job to execute on several machines concurrently

Rejected. The current `Job` lifecycle and task correlation model represent one mutable work item with one current routing progression. Making one `JobId` simultaneously own several active operations would blur work identity, precedence, machine assignment, event correlation, and completion accounting.

### Have the game submit several Orders

Rejected by ADR-0009 for the current reference challenge. The game owns the fixed production requirement, not Arcogine's internal decomposition of that requirement.

### Introduce generalized lots, batches, material genealogy, and split/merge now

Rejected. Those are legitimate future manufacturing concepts but are not required to prove the current 20-unit fixed-contract capacity trade-off.

## Consequences

- The former one-`Order`-to-one-`Job` runtime invariant becomes historical implementation shape rather than intended architecture once unit-work decomposition is implemented.
- `JobId` has a clear semantic meaning: identity of one independently dispatchable execution work item.
- One order can use several equivalent resources concurrently without changing deterministic dispatch policy.
- Aggregate order progress and completion become explicit instead of being synonymous with one job's progress.
- Existing job-oriented API/DTO compatibility projections require review because parent quantity/value can no longer safely be interpreted as child-job quantity/value.
- `OrderCompleted` gains explicit `OrderId` correlation and remains exactly once per order.
- The supported observation/event contract can stabilize against a known execution-identity model rather than the obsolete 1:1 assumption.
- Resident `Job` count becomes quantity-proportional and must be measured with a large-order benchmark.
- Workload acceptance must establish and enforce a deterministic supported child-materialization envelope before mutation; ambient memory exhaustion is not an admissible workload-limit mechanism.
- Real lot/batch semantics remain open and require a separate accepted decision if introduced.

## Acceptance evidence required before the decomposition contract is complete

The implementation must prove at least:

1. accepting quantity 20 creates exactly one `Order` and exactly 20 sibling `Job`s under that order;
2. the child ordinals and `JobId` allocation are deterministic;
3. each child traverses `CUT -> ASSEMBLE -> INSPECT` in order and exactly once;
4. two eligible cutters can simultaneously execute different children of that same order;
5. aggregate progress advances from 0 through 20 completed units and completion occurs only at 20;
6. exactly one `OrderCompleted` is emitted with explicit `OrderId` and the completing child `JobId`;
7. completed-sales count/value, backlog, and lead-time semantics remain order-level and are not multiplied by child count;
8. the one-cutter and two-cutter reference models produce observably different queue/utilization/completion behavior for the same single order;
9. two fresh identical runtimes produce identical child identities, assignments, event streams, and terminal aggregate state;
10. quantity 1 still creates one child job and behaves as the degenerate case of the same model;
11. independent-order dispatch, offline/recovery, reset/replay, bounded advancement, and rejected-submission atomicity remain covered;
12. a large-order benchmark records the practical cost of unit decomposition and justifies the chosen supported child-materialization envelope;
13. quantities above that supported envelope are deterministically rejected before mutation through the structured command-result/error contract, with zero partial `Order`, aggregate, `Job`, queue, pending-work, assignment, or event state.

## Explicit non-goals

This decision does not define:

- material-lot identity or genealogy;
- configurable production batch sizes;
- transfer batches;
- split/merge processing;
- inventory allocation or BOM consumption;
- setup-family optimization or campaign scheduling;
- resource pools beyond current explicit eligibility;
- priority, due-date, or generalized scheduling policy;
- durable cross-session `JobId` identity;
- the final supported runtime event-envelope contract;
- game scoring, challenge evaluation, or player-owned workload decomposition.

## Charter alignment

The decision keeps executable production semantics inside Arcogine while preserving the consumer boundary established by the product and planning documents. A consumer states one production requirement; Arcogine deterministically turns that requirement into executable work, dispatches it, and reports aggregate outcome. The model is intentionally minimal: it enables the proven reference requirement without prematurely introducing a generalized manufacturing-execution ontology.