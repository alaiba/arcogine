# Architecture guardrails

Part of the [contributor guide](../../../.github/CONTRIBUTING.md).

Arcogine follows an Events–State–Observations model:

```text
Events mutate State.
State produces Observations.
Observations inform Decisions.
Decisions produce Events.
```

The authoritative architecture description and detailed domain semantics live in [`docs/architecture/overview.md`](../../architecture/overview.md). Read it before adding a domain, changing cross-module boundaries, or changing event dispatch.

During implementation and review, preserve these non-negotiable constraints:

1. Every mutable piece of domain state has exactly one authoritative owner; cross-domain consumers receive read-only observations or explicit purpose-specific context, never another subsystem's mutable state.
2. Agents observe and emit decisions/events; they do not mutate simulation domains directly. Decisions that change simulation state become deterministic simulation events.
3. Observation objects remain immutable and purpose-specific. Outward-projection DTOs are not automatically valid domain observations.
4. Handler execution order stays explicit wherever order affects semantics. Do not introduce asynchronous/event-bus dispatch that weakens deterministic, explicitly ordered execution.
5. Do not introduce synchronized copies of authoritative state or pairwise setter wiring between domains as a coupling mechanism.
6. Keep domain concepts distinct and owned by the appropriate domain; in particular, operational facts and any financial interpretation of those facts must remain separate.
7. The simulation must remain deterministic: identical inputs and seeds produce identical results.

A subset of these constraints is CI-enforced by `architecture-conformance-test`'s ArchUnit `ArchitectureTest`; the remainder are review constraints. Detailed examples — including order terminology and Commercial, Operational, and Financial Truth — belong in the architecture documentation rather than this contributor guide.
