# Discrete-event simulation, modules, dispatch, and types

Part of the [architecture overview](../overview.md#routed-topics); it is mandatory whenever the overview routes your change here.

## Discrete-Event Simulation (DES)

The simulation advances in deterministic event-time order. The scheduler processes only meaningful events, skips idle time, and preserves FIFO ordering among events at the same tick.
### Event taxonomy

The current internal event vocabulary is limited to Factory execution facts and commands: `OrderCreation`, `TaskEnd`, and `MachineAvailabilityChange`. A `FactoryRuntime` session schedules only `TaskEnd` step completions ([Engine semantics §4](../engine-semantics.md#4-scheduler-and-dispatch-cascade-ordering) rule 3): its commands apply order creation and availability changes directly. Supported `RuntimeEvent` types form a separate consumer-facing taxonomy and report only authoritative changes.
### When a new domain deserves its own module

Not every new metric or piece of derived behavior warrants a new `XHandler`/module — a domain is justified by more than "it computes a number Factory doesn't." The admission rule: **a new domain deserves its own handler/module when it owns mutable state with its own invariants and lifecycle, not merely because it has a new metric or helper function.** Concretely, ask:

- Does it own state that nothing else should be able to mutate directly (the way `FactoryHandler` owns job/machine lifecycle)?
- Does that state have its own invariants worth protecting at construction/mutation time (the way an accepted `Order` keeps its commercial terms immutable)?
- Does it react to events from other domains and produce its own facts, rather than just recomputing a view over another domain's existing state?

If the answer is genuinely yes to state-with-invariants, it's a domain with an explicit owner, its own invariants, and only the event/runtime integration needed by a real consumer. If the answer is no — it's a computed value over state another domain already owns — it belongs as a method/projection on the existing owner (like `FactoryHandler.completedSalesValue()`) or in a separately supported consumer projection, not a new module. This keeps the module count matched to genuine ownership boundaries instead of granularity of features.

## Module Structure

The Java codebase uses a Gradle multi-module layout rooted at `product/`:

```text
product/
├── types/                Shared typed IDs, time, quantities, and errors
├── governance/           Controlled revisions, semantic change, requirements,
│                         conformance, and evidence-use capabilities
├── storage/              Built-in persistence of controlled-revision history
├── simulation/           Deterministic event scheduler and EventHandler contract
├── domains/
│   └── factory/          Canonical model, runtime, machines, orders, jobs, routing
├── consumer/
│   ├── challenge/        Game-owned challenge definitions, validation, evaluation,
│   │                     catalogue/economics, and attempt comparison
│   ├── challenge-factory-integration-test/  Test-only proof of independent validation axes
│   └── last-light-foundry/  Outward game consumer (HTTP adapter and browser client)
├── research-experiments/ Non-shipped deterministic research-experiment substrate
└── architecture-conformance-test/          Test-only cross-domain ownership rules
```
### Dependency graph

```text
types ← simulation ← factory

types ← governance ← factory
types, governance ← storage

challenge (independent game-owned boundary)

challenge-factory-integration-test ← types, factory, challenge (test-only)
last-light-foundry ← types, factory, challenge (outward consumer; nothing depends on it)
research-experiments ← types, factory (non-shipped research infrastructure)
architecture-conformance-test ← types, factory, storage, challenge, last-light-foundry,
                                research-experiments (test-only)
```

Governance depends on `types`; Factory depends on the narrow Governance ports it implements. Storage depends on `governance` and `types` to realize the revision authority without either semantic owner depending on its implementation. Challenge remains independent of the production simulation and domain modules, and its game-owned contract lives in [Challenge](../challenge.md); the test-only Challenge–Factory integration module demonstrates that Factory publication validity and Challenge admissibility are independent validation axes without either module depending on the other. The research-experiments module is an embedded consumer of the supported runtime contract for deterministic research experiments; it adds no product capability, its derivations are research-local rather than Engine facts or analytics, and no production or consumer source set depends on it ([researching.md](../../development/researching.md#executable-research-substrate)). The architecture-conformance module scans current production and consumer sources to protect retained ownership rules and that research boundary.

## Event Dispatch Architecture

`EventHandler` is the current contract for the Factory event consumer. No retained production code composes domains into an application-wide handler chain. `FactoryRuntime` owns its FactoryHandler and Scheduler for explicit workload execution, and publishes order completion as its supported `ORDER_COMPLETED` event. This architecture does not prescribe a replacement orchestrator or a future application topology.
## Type System

Java features available within the **Java 21 compatibility baseline** map cleanly to the domain:

| Concept | Java feature |
|---------|-------------|
| Typed IDs | `record MachineId(long value)` |
| Value objects | `record SimTime(long value)`, whose compact constructor refuses a negative tick count ([Engine semantics §5.1](../engine-semantics.md#51-derived-result-arithmetic)) |
| Sum types | `sealed interface EventPayload` with record permits |
| Error hierarchy | `sealed class SimError extends RuntimeException` |
| Pattern matching | `switch (event.payload())` with exhaustive pattern matching |
