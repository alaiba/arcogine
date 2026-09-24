# Arcogine — Concepts

This page describes the retained Factory and Engine capabilities. See the [Product Charter](charter.md) for Arcogine's enduring product direction.

## Current capability

Arcogine currently provides a headless, deterministic factory model and runtime. It has no outward application or interactive experiment loop; executable evidence lives in tests, conformance checks, and benchmarks.

The canonical **Factory model** describes products, operations, configured resources, and eligible-resource relationships. A validated model can be published as an immutable, fingerprinted version. A **FactoryRuntime** instantiates that version and executes explicit production workload under the identified Engine semantics.

## Factory concepts

| Term | Meaning |
|------|---------|
| **Resource** | A configured productive resource that performs eligible operation steps. |
| **Product** | A product definition associated with an operation routing. |
| **Operation** | An ordered set of processing steps and each step's eligible resources and duration. |
| **Order** | Immutable accepted production intent: product, quantity, creation time, and agreed unit price. |
| **Job** | A unit-quantity work item released from an order and processed through its routing. |
| **Runtime** | Mutable execution state instantiated from one published Factory model version. |
| **Observation** | A read-only projection of the current runtime's orders, jobs, resources, pending work, and performance facts. |
| **Runtime event** | An ordered description of an authoritative runtime change, published after that change succeeds. |

An accepted order remains the commercial record for its requested quantity and unit price. Runtime execution releases one unit-quantity job per unit, each with a stable ordinal and shared order identity. Order completion, backlog, completed sales value, and lead time remain order-level facts.

The runtime accepts workload explicitly through its consumer-neutral command surface. It does not generate orders from a pricing or demand model. Repeated runs with the same published model, Engine semantics, and explicit commands produce the same ordered supported runtime events and terminal observations.

## Commercial and financial facts

An order's unit price is fixed when the order is accepted. Factory retains that commercial fact and derives its operational completed-sales value from completed orders. Finance separately interprets `OrderCompleted` as a financial fact and records balanced postings in its minimal ledger. Operational completion and financial interpretation have distinct owners even when the current immediate-settlement policy yields matching totals.

## What is not currently provided

There is no scenario input format, pricing or demand model, experiment loop, agent framework, application server, HTTP API, or CLI. A future input format or outward consumer must follow then-current product requirements and the [runtime contract](../architecture/runtime-contract.md); no replacement scenario format or experiment experience is selected here.

## Further reading

- [Architecture overview](../architecture/overview.md)
- [Factory design architecture](../architecture/factory-design.md)
- [Runtime contract](../architecture/runtime-contract.md)
- [Quick start](../../README.md#quick-start)
- [Contributing](../../.github/CONTRIBUTING.md)
