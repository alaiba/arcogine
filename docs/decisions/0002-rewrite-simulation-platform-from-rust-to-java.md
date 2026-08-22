# ADR-0002: Rewrite the simulation platform from Rust to Java

Status: Accepted
Date: 2026-05-30
Recorded: 2026-08-22

<!--
This is a retrospective ADR. The decision itself was already established
by the time `docs/java-rewrite-plan.md` was written (dated 2026-05-30);
this record was added later, once the rewrite was complete, to preserve
the rationale in a durable form. Date reflects the decision as documented
in that plan; Recorded reflects when this ADR was written.
-->

## Context

Arcogine's simulation platform (the discrete-event simulation engine, factory
and economy domain logic, sales agents, and the HTTP/SSE API) was originally
implemented in Rust as a set of crates (`sim-types`, `sim-core`,
`sim-factory`, `sim-economy`, `sim-agents`, `sim-api`, `sim-cli`). The
React/TypeScript UI was, and remains, a separate concern from this decision.

By 2026-05-30, the decision to replace this Rust implementation with Java 25
had already been made and was documented as the scope of a rewrite plan
(`docs/java-rewrite-plan.md`, since removed once the rewrite completed). That
plan recorded the target stack and a set of non-negotiable constraints, but
the underlying motivation for choosing Java over continuing to invest in the
Rust implementation is not preserved in any source available at the time of
writing this ADR. This ADR does not reconstruct that motivation; it records
what was actually decided and what was documented as required to hold
constant across the rewrite.

## Decision

The simulation platform was rewritten in Java 25 (LTS), organized as a
multi-module Gradle build under `java/`, using Spring Boot for the API layer
and Picocli for the CLI. This was a full rewrite, not an incremental
migration: no backwards compatibility with the Rust codebase was required,
and the Rust crates and `Cargo.toml`/`Cargo.lock` were removed once the
rewrite was complete. The React/TypeScript UI was unchanged by this decision.

The rewrite plan required Java's default strict floating-point semantics
(JEP 306) to be verified sufficient for the platform's determinism
requirements before proceeding on Java 25; that verification succeeded and
no additional floating-point configuration was needed.

The Java module boundaries mirror the original Rust crate DAG
(`sim-types → sim-core → sim-factory / sim-economy / sim-agents → sim-api →
sim-cli`): the rewrite plan's recommended approach was a bottom-up rewrite
in crate-DAG order, and the resulting Gradle modules preserve that same
dependency direction and separation of concerns. This mirroring was the
documented starting point for the rewrite, not a claim that the module
boundaries must remain fixed forever — module structure is current-
architecture detail, described authoritatively in
[`architecture.md`](../architecture.md), and has already evolved since
(for example, `sim-finance` was added as a module after the initial
rewrite).

The rewrite plan's non-negotiable constraints required the rewrite to
preserve, rather than renegotiate, several properties of the Rust
implementation:

- **Determinism**: a fixed seed still yields reproducible simulation runs.
  The specific mechanism changed — from Rust's ChaCha8 RNG to Java's
  `java.util.Random`/`SplittableRandom` — so bit-for-bit cross-language
  reproduction was never a goal; reproducibility within the Java
  implementation is the guarantee that matters and is what the test suite
  asserts.
- **Public API compatibility**: the REST and SSE contract the UI depends on
  was preserved so the UI required no changes.
- **Scenario format compatibility**: TOML scenario files remain the input
  format.
- **Simulation semantics and test parity**: the discrete-event simulation
  behavior and the existing test suite's coverage intent were carried over
  rather than redesigned as part of the rewrite.

## Alternatives considered

The rewrite plan documents alternatives it evaluated for specific technical
choices within the rewrite, not for the decision to rewrite in Java itself
(no record of that broader alternatives analysis is available):

- **RNG compatibility approach**: using a Java RNG bit-identical to Rust's
  ChaCha8 was considered and rejected in favor of `java.util.Random`, with
  Java-derived golden values and reproducibility (not cross-language
  equivalence) as the determinism guarantee.
- **SSE implementation**: Spring WebFlux `Flux`-based streaming was
  considered and rejected in favor of Spring MVC's `SseEmitter`.
- **TOML parsing library**: the plan evaluated TOML libraries and selected
  `jackson-dataformat-toml`.

## Consequences

- The simulation platform now has a single implementation language and
  toolchain (Java 25 + Gradle), removing the Rust toolchain as a dependency
  for building, testing, or deploying Arcogine.
- Determinism is now asserted as intra-Java reproducibility, not
  cross-language bit-for-bit equivalence with the retired Rust
  implementation; anyone comparing historical Rust-era simulation output
  against current output should expect numeric divergence even for
  identical scenarios and seeds.
- The module DAG mirrors the original crate DAG as a starting point, but is
  current architecture and may diverge over time (see
  [`architecture.md`](../architecture.md) for the authoritative current
  structure) — this ADR records why the rewrite started that way, not a
  constraint that it must stay that way.
- The Rust codebase is fully removed; Git history is the record of the
  prior implementation, not a retained `crates/` tree or archived copy.
