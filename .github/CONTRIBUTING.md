# Contributing to Arcogine

Thank you for considering a contribution to Arcogine. This guide covers the contribution workflow, code standards, architecture constraints, and required validation.

For environment setup and running Arcogine locally, use the canonical [Quick start](../README.md#quick-start) in the root README. `./arcogine` is the canonical developer entry point for common cross-project workflows.

Before proposing a significant product, domain, or architecture change, read [`docs/product/charter.md`](../docs/product/charter.md). It's the normative source for Arcogine's product direction — significant proposals should be evaluated against it as well as the current architecture constraints in [`docs/architecture/overview.md`](../docs/architecture/overview.md). It's strategic context for judgment calls, not a rulebook for blocking routine local implementation work.

## Repository layout

| Directory | Purpose |
|-----------|---------|
| `product/types/` | Shared types, typed IDs, error definitions |
| `product/governance/` | Controlled revisions, semantic change, requirements, conformance, and evidence use |
| `product/simulation/` | Event engine and scheduler |
| `product/domains/factory/` | Canonical factory model and publication, `FactoryRuntime`, machines, jobs, routing, queues |
| `product/domains/finance/` | Ledger, financial interpretation of operational events |
| `product/consumer/challenge/` | Headless, game-owned challenge definitions, catalogue/economics, admissibility, evaluation, and attempt comparison — no dependency on any module above |
| `product/consumer/challenge-factory-integration-test/` | Test-only proof module: Factory-executability and challenge admissibility are independent axes |
| `product/architecture-conformance-test/` | Test-only module: durable cross-domain ArchUnit guardrails |
| `docs/` | Project documentation |
| `infra/` | Dev-environment infrastructure |

Keep the repository root limited to primary entry points, standard discovery/configuration files, and files whose tools conventionally expect them there. CI, test-support, and security-support files should prefer `.github/` when their paths are explicitly controlled. Do not move conventionally discovered files merely to reduce visual clutter. For example, `.trivyignore` stays at the root because Trivy conventionally discovers it there, while the Gitleaks config can live under `.github/security/` because Arcogine invokes it with an explicit `--config` path.

See [`docs/architecture/overview.md`](../docs/architecture/overview.md) for the full module dependency graph and design rationale.

## Development workflow

The managed devcontainer and Claude Cloud setup install a pre-commit check that confirms `gh` is authenticated as the repository's human owner and that the configured, author, and committer emails belong to that account. It accepts verified account emails, the public profile email, and the account's GitHub noreply addresses; private secondary emails require `gh`'s `user` scope. In other environments, install it once with `bash infra/dev/install-git-hooks.sh`.

1. **Branch** from `main` with a descriptive name (`feature/xyz`, `fix/abc`).
2. **Make your changes.** Follow the code style enforced by Checkstyle.
3. **Write tests** for new functionality. Java modules use JUnit 6.
4. **Run the checks:**

```bash
./arcogine check         # Java compile, style, tests, and coverage
./arcogine check --full  # check + dependency audit + secret scan
```

Use `./arcogine check` before pushing. Use `./arcogine check --full` when the change warrants the complete local validation surface. For individual test categories and native subsystem commands, see [`docs/development/testing.md`](../docs/development/testing.md).

5. **Open a pull request** against `main` with a clear description of what changed and why.

Keep PR descriptions stable under normal branch evolution. Describe semantic scope, rationale, non-goals, and validation actually performed. Do not present mutable Git/GitHub topology or gate state — such as the current `main`/head SHA, ahead/behind or commit counts, base freshness, mergeability, or current CI/check state — as validation facts that the body must stay synchronized with. GitHub resolves those facts live. Exact SHAs may still appear when they intentionally identify immutable evidence/artifacts or are clearly labeled as historical provenance. Validation text should name reproducible commands, checks, or review performed rather than temporary branch shape.

**Temporary artifacts:** use `logs/` for local diagnostics, captures, and session scratch that should never be committed; it is gitignored as a whole. Branch-local material that must be committed for continuity or handoff but must not land on `main` belongs under the unignored `workspace/` root. `workspace/` is transient storage, not an archive: remove its files before final review and do not add a marker file. The repository check rejects any tracked `workspace/` path. Do not redirect canonical tool outputs — Gradle continues to use its configured locations.

For independent PR review, re-review, severity/disposition, CI-language, and AI-assisted session-boundary guidance, follow [`docs/development/reviewing.md`](../docs/development/reviewing.md).

## Change slicing and branch hygiene

Arcogine's larger initiatives are intentionally delivered as small, dependency-ordered pull requests rather than as one broad implementation branch. This keeps architectural decisions reviewable and makes behavior changes attributable.

When a roadmap item spans several capabilities:

- prefer the smallest coherent slice that establishes one required concept, boundary, or behavior;
- state the PR's **non-goals** explicitly, especially when later roadmap work is adjacent and tempting to pull forward;
- keep a behavior-preserving refactor behavior-preserving: do not combine a representation/boundary change with new scheduling, workload, persistence, or external-contract semantics unless the change genuinely requires them together;
- preserve deterministic behavior and existing API/wire compatibility by default; intentional compatibility breaks must be explicit in the PR and supported by migration/contract tests as appropriate;
- do not introduce abstractions only because a later roadmap step might need them; add the abstraction when the current slice gives it a concrete responsibility;
- update authoritative current-state documentation when implementation changes established behavior or ownership, and update planning documents when the remaining sequence changes; do not make planning prose claim that deferred capability already exists;
- when a change removes or replaces automation, schema, or tooling, treat live GitHub state or configuration whose meaning, lifecycle, or operation depends on that component as part of the change's closure; inspect and reconcile affected state before treating the change as complete, and if the current task or role is not authorized to make the required live mutation, report that reconciliation as a blocker rather than treating the repository diff as sufficient;
- keep initiative-local stage/gate/slice identifiers, and PR-local review/finding identifiers (see `AGENTS.md`), in planning, issues, PRs, reviews, branch names, commits, and implementation handoffs where they help coordinate or track delivery; when knowledge moves into durable semantic naming — current-state documentation, code comments, workflow definitions, test names — translate the coordinate into the capability, contract, identity, invariant, or behavior it represents;
- reconcile a significant architectural change into the architecture or specification document that owns the affected semantics, with code, tests, and dependent planning updated in the same reviewed change, rather than leaving the constraint in a PR discussion, branch name, issue comment, or chat transcript; where rationale is needed to understand or not accidentally undo a constraint, keep it concise and next to the rule, and leave the chronology of how the document got there to Git and pull-request history; do not grow the canonical document into an analysis of rejected alternatives — when that reasoning meets the retention test in [`docs/development/researching.md`](../docs/development/researching.md#historical-decision-rationale), preserve it as a non-normative historical decision-rationale record instead, which is never required and never carries a current requirement;
- reconcile the feature branch with the latest `main` before final review when `main` has moved materially, then review the **net diff against current `main`**, not merely the original branch commit;
- after a PR is merged, delete feature/review branches once they contain no unique work that still needs to be preserved.

Before final review of any medium- or high-semantic-risk change, close the semantic consequences as part of completing the implementation: identify the small set of concepts whose meaning, ownership, lifecycle, status, supported behavior, or authority changed; search maintained current-state documentation, planning, tests, examples, interfaces, and executable configuration for current vocabulary and plausible prior-state assumptions; and reconcile stale claims in the same change. Keep this bounded to the affected semantic neighborhood. This applies the existing obligation to keep authoritative documentation and planning truthful; it does not create a closure-set handoff artifact, a new review gate, or a repository-wide Consistency sweep.

A useful review question is: **if this PR were merged by itself, would the repository tell the truth about what exists now, while leaving later roadmap decisions genuinely open?**

Near-term sequencing may live in issues, PRs, or planning documents; durable product and architecture knowledge must live in the repository's charter, architecture and specifications, tests, and maintained documentation rather than depending on conversational history or an obsolete planning coordinate.

## Code style

- Checkstyle enforces Java style; `./arcogine check` (or `cd product && ./gradlew checkstyleMain checkstyleTest`) runs it, and warnings are treated as errors at compile time (`-Werror`).
- Prefer explicit types over inference in public APIs.

## Architecture guardrails

Arcogine follows an Events–State–Observations model:

```text
Events mutate State.
State produces Observations.
Observations inform Decisions.
Decisions produce Events.
```

The authoritative architecture description and detailed domain semantics live in [`docs/architecture/overview.md`](../docs/architecture/overview.md). Read it before adding a domain, changing cross-module boundaries, or changing event dispatch.

During implementation and review, preserve these non-negotiable constraints:

1. Every mutable piece of domain state has exactly one authoritative owner; cross-domain consumers receive read-only observations or explicit purpose-specific context, never another subsystem's mutable state.
2. Agents observe and emit decisions/events; they do not mutate simulation domains directly. Decisions that change simulation state become deterministic simulation events.
3. Observation objects remain immutable and purpose-specific. Outward-projection DTOs are not automatically valid domain observations.
4. Handler execution order stays explicit wherever order affects semantics. Do not introduce asynchronous/event-bus dispatch that weakens deterministic, explicitly ordered execution.
5. Do not introduce synchronized copies of authoritative state or pairwise setter wiring between domains as a coupling mechanism.
6. Keep domain concepts distinct and owned by the appropriate domain; in particular, operational facts and Finance's financial interpretation of those facts must remain separate.
7. The simulation must remain deterministic: identical inputs and seeds produce identical results.

A subset of these constraints is CI-enforced by `architecture-conformance-test`'s ArchUnit `ArchitectureTest`; the remainder are review constraints. Detailed examples — including order terminology and Commercial, Operational, and Financial Truth — belong in the architecture documentation rather than this contributor guide.

## Testing

The contribution gate is `./arcogine check`. It covers Java compilation, Checkstyle, tests and Jacoco coverage gates.

For the dependency audit and secret scan, run `./arcogine check --full`.

See [`docs/development/testing.md`](../docs/development/testing.md) for the test taxonomy, CI pipeline, native subsystem commands, and testing rationale.

## Commit messages

Use concise, descriptive commit messages. Reference a delivery phase or task number when it helps active coordination; do not promote that coordinate into durable semantic documentation.

## License

By contributing, you agree that your contributions will be licensed under the Apache-2.0 license (see [`LICENSE`](../LICENSE)).
