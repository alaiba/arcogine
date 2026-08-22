# ADR-0002: Rewrite the simulation platform from Rust to Java

Status: Accepted
Date: 2026-08-22

## Context

Arcogine's simulation platform (the discrete-event simulation engine, factory
and economy domain logic, sales agents, and the HTTP/SSE API) was originally
implemented in Rust as a set of crates (`sim-types`, `sim-core`,
`sim-factory`, `sim-economy`, `sim-agents`, `sim-api`, `sim-cli`). The
React/TypeScript UI was, and remains, a separate concern from this decision.

Continuing to maintain and extend the simulation platform in Rust was
weighed against rewriting it on a different stack, primarily to widen the
pool of contributors (including coding agents) who could work on it
confidently, and to align the platform with the JVM ecosystem used
elsewhere. This was treated as a full rewrite rather than an incremental
migration: no backwards compatibility with the Rust codebase was required,
and the Rust crates were removed once the rewrite was complete.

## Decision

The simulation platform is rewritten in Java 25 (LTS), organized as a
multi-module Gradle build under `java/`, using Spring Boot for the API layer
and Picocli for the CLI. The Rust crates and `Cargo.toml`/`Cargo.lock` have
been removed; Java is now the sole implementation language for the
simulation platform. The React/TypeScript UI is unchanged by this decision.

Java 25 was chosen specifically for its LTS status (giving the platform a
long support horizon without forced upgrades) and because its default
strict floating-point semantics (JEP 306) were sufficient to meet the
platform's determinism requirements without additional configuration.

The Java module boundaries mirror the original Rust crate DAG
(`sim-types → sim-core → sim-factory / sim-economy / sim-agents → sim-api →
sim-cli`), preserving the same dependency direction and separation of
concerns that the Rust crate structure had already validated. This mirroring
was a deliberate starting point for the rewrite, not a claim that the
module boundaries must remain fixed forever — module structure is
current-architecture detail, described authoritatively in
[`architecture.md`](../architecture.md), and may evolve independently of
this decision (for example, `sim-finance` was added as a module after the
initial rewrite).

The rewrite deliberately preserved, rather than renegotiated, several
properties of the Rust implementation:

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

- **Continue investing in the Rust implementation.** Kept the existing,
  already-validated implementation and avoided rewrite risk entirely, but
  did not address the motivations above.
- **Incremental migration (interop layer, module-by-module cutover with both
  languages live).** Would have reduced point-in-time risk but added
  sustained cross-language interop complexity for a codebase of this size,
  for longer than a clean rewrite justified.
- **Rewrite in a different JVM language (e.g. Kotlin).** Considered and
  rejected in favor of plain Java to keep the toolchain and hiring/agent
  familiarity as broad as possible; not pursued further.

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
