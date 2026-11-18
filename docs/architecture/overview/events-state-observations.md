# Core Architecture Philosophy: Events, State, Observations

Part of the [architecture overview](../overview.md#routed-topics); it is mandatory whenever the overview routes your change here.


This is the first-class architectural principle for Arcogine. It is a design heuristic *and* an architectural invariant: new features and refactors should be evaluated against it, and deviations should be a deliberate, documented decision rather than an accident of implementation order.

```text
Events mutate State.
State produces Observations.
Observations inform Decisions.
Decisions produce Events.
```

## Events

Internal events are immutable facts in simulated time that carry what deterministic Factory transitions need. The internal vocabulary names order creation, task start/end, order completion and machine availability changes, but a Factory session schedules only step completions: its commands apply order creation and availability changes directly ([Event taxonomy](simulation-modules-and-types.md#event-taxonomy)).

The separate supported runtime event contract exposes ordered authoritative changes only after the corresponding state transition succeeds. Internal scheduler events remain implementation contracts rather than public compatibility types.
## State

Each subsystem exclusively owns its mutable domain state. Factory owns accepted orders, machines, jobs, queues, completion state, and production metrics. A future financial, inventory or workforce subsystem would own its own ledger, stock or labor state.

Commercial/order intent and mutable production execution are represented separately. `Order` is immutable accepted intent; its same `OrderId` identifies an authoritative order-execution aggregate. Acceptance deterministically materializes one unit-quantity `Job` per requested unit, in zero-based ordinal order. Each child has its own `JobId`, traverses the routing once, and can be dispatched independently under the existing selector. The aggregate records release and completion quantities and is the sole source for order completion, backlog, sales KPIs, and lead time; only its final child completion completes the order, published once as the supported `ORDER_COMPLETED` event with both identities and full order commercial facts.

State should:

- have exactly one authoritative owner;
- avoid synchronized duplicate representations of the same fact across subsystems;
- be mutated only by its owning subsystem, in response to an event it handles;
- never be mutated directly by agents or by unrelated domains.

## Observations

Observations are immutable, read-only projections of authoritative state. The retained FactoryRuntime exposes purpose-specific resource, order, job, pending-work, and performance facts without exposing FactoryHandler, mutable stores, or internal scheduler state.

Observations should be derived from their owner's authoritative state, expose only what a consumer needs, and never become a second source of truth. Any future decision-maker or outward consumer must receive an explicit purpose-specific view rather than unrestricted mutable state.

## Domain observations vs. outward projections/DTOs

A domain observation supports an internal decision; an outward projection supports an external consumer and may have a separately versioned wire or display shape. They may report related facts but should not share a type merely because their fields look alike. Arcogine's one outward adapter is the Last Light Foundry game consumer; its projections live in the game's own package.
## Decisions

A decision is a choice made from an observation; a change to authoritative state must still pass through the owning capability's explicit command or event boundary. Humans, agents, and other future decision sources must not bypass that ownership boundary. This is a durable constraint, not a claim that a decision-making consumer exists today.

## Commercial, operational, and financial facts

The accepted `Order` is the immutable commercial record: its product, quantity, creation time, and agreed unit price are fixed at acceptance. Factory owns production execution and derives completed sales value from completed orders. Arcogine has no financial interpretation of those facts — cash, receivables, profit or a ledger. When a consumer needs one, a dedicated owner interprets facts Factory emits; it never inspects or alters Factory state, and Factory decisions never depend on it.

| Fact | Owner | Meaning |
|---|---|---|
| Accepted order intent and agreed unit price | Immutable `Order` in Factory | Historical commercial terms |
| Jobs, resources, routing progress, and completion | Factory runtime | Operational state and performance |
| Completed sales value | Factory runtime | Operational value of completed orders |

Changing a future pricing or demand policy, if one is later required, must not rewrite already accepted order terms. No current mutable offer-price state, pricing policy, or demand-generation model is selected by this architecture.
## What should trigger architectural review

Treat any of the following as a signal to stop and reconsider the design, not just implement around it:

- one subsystem mutating another subsystem's state;
- a mutable "observation" (anything handed to an agent/consumer that they could write through);
- duplicated authoritative state (the same fact represented as separate mutable fields in two subsystems);
- synchronization setters proliferating between domains (`setX`/`syncX`-style cross-domain pushes);
- agents or policies reaching directly into mutable subsystem internals instead of going through an observation;
- adding a subsystem requiring pairwise wiring changes to every existing subsystem;
- event ordering becoming implicit, or dependent on registration/construction order rather than an explicit, documented contract;
- financial interpretation (cash, profit, receivables, or similar) accumulating inside an operational domain, or inferring transactions from another domain's mutable state instead of the facts that domain emitted;
- attribution or provenance metadata being read by domain, dispatch, pricing, or simulation logic instead of the requester appearing as a modelled payload field;
- a shared actor, subject, decision-source, or capability type appearing before multiple consumers have demonstrated the same equality/namespace/lifecycle contract (see [Attribution and decision boundaries](attribution-and-provenance.md#attribution-and-decision-boundaries)).

These guardrails are part of the current architecture and are reinforced by executable architecture tests where the invariant can be checked mechanically. Known remaining runtime work is tracked as GitHub issues, which are delivery context, not architectural authority.
