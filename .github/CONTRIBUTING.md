# Contributing to Arcogine

Thank you for considering a contribution to Arcogine. This guide covers the contribution workflow, code standards, architecture constraints, and required validation. This page is its entry point: read each routed topic whose trigger matches your work. A routed topic is mandatory whenever its trigger applies; it is not optional reference.

For environment setup and running Arcogine locally, use the canonical [Quick start](../README.md#quick-start) in the root README. `./arcogine` is the canonical developer entry point for common cross-project workflows.

Before proposing a significant product, domain, or architecture change, read [`docs/product/charter.md`](../docs/product/charter.md). It's the normative source for Arcogine's product direction — significant proposals should be evaluated against it as well as the current architecture constraints in [`docs/architecture/overview.md`](../docs/architecture/overview.md). It's strategic context for judgment calls, not a rulebook for blocking routine local implementation work.

## Routed topics

| Read | When you are |
|---|---|
| [Development workflow](../docs/development/contributing/workflow.md) | Making any change: the issue branch, the owner-identity pre-commit check, local checks, opening a pull request and writing its description, and temporary artifacts. |
| [Change slicing and branch hygiene](../docs/development/contributing/change-slicing.md) | Deciding what a pull request contains: slice size and non-goals, compatibility, production code reachable from a supported entry point, documentation and research-state updates, delivery coordinates and durable names, reconciling with `main`, and branches after merge. |
| [Semantic closure before final review](../docs/development/contributing/change-slicing.md#semantic-closure-before-final-review) | Preparing any change for final review; the topic says which changes need the closure search and which do not. |
| [Architecture guardrails](../docs/development/contributing/architecture-guardrails.md) | Adding a domain, or changing cross-module boundaries, state ownership, observations, event dispatch or anything determinism depends on. |
| [Repository layout](../docs/development/contributing/repository-layout.md) | Finding which module owns something, or deciding where a new root, CI, test-support or security-support file belongs. |
| [Review method](../docs/development/reviewing.md) | Reviewing or re-reviewing a pull request. |

## Code style

- Checkstyle enforces Java style; `./arcogine check` (or `cd product && ./gradlew checkstyleMain checkstyleTest`) runs it, and warnings are treated as errors at compile time (`-Werror`).
- Prefer explicit types over inference in public APIs.

## Testing

The contribution gate is `./arcogine check`. It covers Java compilation, Checkstyle, tests and Jacoco coverage gates.

For the Java dependency audit and secret scan, run `./arcogine check --full`. The testing guide documents the separate npm lockfile scan.

See [`docs/development/testing.md`](../docs/development/testing.md) for the test taxonomy, CI pipeline, native subsystem commands, and testing rationale.

## Commit messages

Use concise, descriptive commit messages. Reference a delivery phase or task number when it helps active coordination; do not promote that coordinate into durable semantic documentation.

## License

By contributing, you agree that your contributions will be licensed under the Apache-2.0 license (see [`LICENSE`](../LICENSE)).
