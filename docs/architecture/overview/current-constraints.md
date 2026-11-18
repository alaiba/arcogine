# Current constraints, Charter implications, and simulation-first

Part of the [architecture overview](../overview.md#routed-topics); it is mandatory whenever the overview routes your change here.

## Current implementation constraints (MVP)

These describe today's implementation choices. They are not claims about Arcogine's permanent identity — see the Product Charter's [product boundaries](/docs/product/charter.md#9-what-arcogine-is-not) for why Java, current interfaces, and the current deployment model are implementation choices rather than product identity, subject to change as the product grows toward the full lifecycle described there.

1. Core simulation is written in Java with a fixed **language/API/bytecode compatibility baseline** (see [`docs/development/testing.md`](../../development/testing.md#prerequisites-and-version-policy)); the compiler JDK and compatibility baseline are deliberately separate concerns.
2. Factory, Engine, Governance, Storage and Challenge are headless: none of them has a UI, HTTP API, or CLI. Their one outward consumer is the local single-player Last Light Foundry game, which sits entirely outside them (see [Last Light Foundry integration](../last-light-foundry.md)); the architecture tests forbid any inward dependency on it. Other retained executable evidence is tests, conformance checks, and benchmarks. This describes today's layering, not a permanent claim about Arcogine's mature product surface — further outward consumers are introduced from the then-current supported runtime contract when a concrete product need exists (see [runtime contract](../runtime-contract.md)).
3. Security-sensitive defaults remain local-first by default; non-local exposure requires explicit hardening controls (see [SECURITY.md](/.github/SECURITY.md)).

## Architectural implications of the Product Charter

Consequences of the Charter's thesis, stated at the conceptual level only — none of this is a module design, schema, or implementation commitment; see the Charter's [Architectural implications](/docs/product/charter.md#7-architectural-implications) section for the full list:

- Arcogine should not evolve separate simulation-only and production-only domain semantics.
- Model, version, and provenance concepts become fundamental once changes can move from design to reality — today's deterministic Engine execution and explicit run/model identity are simulation-scoped realizations of this, not the final answer.
- Purpose-specific observations and capabilities are preferable to exposing unrestricted mutable state, and are expected to remain so as new consumers (human roles, external systems, execution surfaces) are added.
- Real execution, when it exists, introduces safety, authorization, auditability, failure, and operational consequence as architectural concerns — the current implementation does not yet need to solve these because it does not yet execute anything real (see [SECURITY.md](/.github/SECURITY.md)).

## Simulation-First (current implementation)

Today's system is built around a **headless simulation core**, not a game engine. This describes the current implementation's architecture — simulation is a major Arcogine capability (per the Product Charter), not the entirety of its identity.

- No rendering or wall-clock dependency in the core; `ArchitectureTest` guards wall-clock access
- Deterministic execution — same inputs always produce the same outputs
- Reproducible outcomes for testing, comparison, and analysis
- Explicit workload execution independent of any UI or network layer
