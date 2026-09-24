# Implementation handoff — consolidate repository tooling on Node and remove incidental Python

You are implementing a repository-tooling consolidation in the canonical repository:

`alaiba/arcogine`

This is implementation work. Follow current `AGENTS.md` and all current contributor/testing authorities.

The objective is to make Arcogine's repository-tooling stack simpler and more intentional:

- remove incidental Python from repository tooling;
- consolidate non-shell repository logic and tests on Node.js using built-in Node facilities;
- keep Bash only where the boundary is genuinely shell-native;
- make every test file test only its named subject;
- introduce one explicit repository-tooling suite runner with no hidden aggregation;
- standardize lightweight tooling-test conventions;
- preserve all current observable repository/CI behavior unless this prompt explicitly calls for simplification.

Do not turn this into a product-code migration. Product code remains Java/Gradle/JUnit.

## Handoff baseline

At prompt creation time, live `main` was:

`d8f862724e0a190acafc452566c29fbe44cc397e`

There were no open pull requests.

This branch was created from that baseline.

At execution start:

1. resolve live `main` again;
2. read current `AGENTS.md`;
3. inspect current open PRs for overlap;
4. if `main` advanced materially, reconcile this branch with current `main` using the repository's normal history-preserving process before final review;
5. use current files as authority if any path or implementation changed after this prompt was committed.

Do not treat this prompt's file list as stronger than current repository state.

## Read first

Read:

- `AGENTS.md`
- `.github/CONTRIBUTING.md`
- `.github/agents/work-planner.agent.md`
- `docs/development/testing.md`
- `.github/workflows/ci.yml`
- `.github/workflows/pr-disposition.yml`
- `.github/workflows/continuous-improvement-reminder.yml`
- root `arcogine`
- all tracked files under `.github/scripts/`
- relevant repository tooling under `infra/dev/`, especially:
  - `repo-snapshot.mjs` and its test;
  - `delivery-retrospective.mjs` and its test;
  - `github-attribution-hygiene.mjs` and its test;
  - `git-identity.sh` and its test;
  - `claude-cloud.sh` and its test.

Issue #376 is adjacent work about reducing hot-path duplication in `AGENTS.md`. Do not absorb that issue wholesale. Only update `AGENTS.md` where this migration mechanically changes script names, technology references, or validation ownership.

## Quick repository search

Before editing, search the current repository for:

- `python`
- `python3`
- `*.py`
- `node --test`
- `.mjs`
- `bash`
- `.test.sh`
- `.test.py`
- `check-markdown-links`
- `check-delivery-labels`
- `check-transient-workspace`
- `check-transient-coordinates`
- `check-pr-disposition`
- `check-dependabot-provenance`
- `check-actions-workflows`
- `check-continuous-improvement-reminder`
- `classify-changes.test.sh`
- `repository-tooling`

Fetch any search hit used for a current-state decision at the exact target revision.

## Current technology finding to preserve

At the handoff baseline, Python was not part of Arcogine product code or product tests.

The only tracked `.py` files were these eight repository-tooling files:

- `.github/scripts/check-markdown-links.py`
- `.github/scripts/check-markdown-links.test.py`
- `.github/scripts/check-delivery-labels.py`
- `.github/scripts/check-delivery-labels.test.py`
- `.github/scripts/check-transient-workspace.py`
- `.github/scripts/check-transient-workspace.test.py`
- `.github/scripts/check-transient-coordinates.py`
- `.github/scripts/check-transient-coordinates.test.py`

Python was also used inline/invoked from current CI/tooling, notably:

- Python invocations in `.github/scripts/classify-changes.test.sh`;
- Python invocations and an inline Python gate evaluator in `.github/workflows/ci.yml`;
- command examples/references in `docs/development/testing.md`.

Do not assume this list remains exhaustive. Re-search current `main`.

## Desired technology policy

Converge toward this repository-tooling model:

- **Java + Gradle/JUnit/ArchUnit/JaCoCo** for product and product tests.
- **Node.js + built-in `node:test` + built-in Node modules** for repository logic, parsing, text/repository checks, and their tests.
- **Bash** only for genuinely shell-native boundaries:
  - public shell entry points;
  - environment/provisioning scripts whose behavior is shell behavior;
  - very thin orchestration of external commands;
  - Git/shell pipelines where retaining shell materially reduces complexity.
- Specialized external validators such as actionlint, Trivy, Gitleaks, Gradle, etc. remain their own tools.

Do not add Jest, Vitest, Mocha, pytest, Bats, or another test framework just to standardize syntax.

Prefer zero new npm dependencies. Use Node built-ins unless a dependency is clearly necessary and justified.

## Primary migration: eliminate incidental Python

Migrate all repository-owned Python tooling and tests to Node `.mjs` equivalents.

The migrated implementations must preserve their current CLI behavior, exit status semantics, diagnostics, repository-root overrides/testability, tracked-file semantics, and false-positive protections.

Expected migrations include:

- `check-markdown-links.py` -> `check-markdown-links.mjs`
- `check-markdown-links.test.py` -> `check-markdown-links.test.mjs`
- `check-delivery-labels.py` -> `check-delivery-labels.mjs`
- `check-delivery-labels.test.py` -> `check-delivery-labels.test.mjs`
- `check-transient-workspace.py` -> `check-transient-workspace.mjs`
- `check-transient-workspace.test.py` -> `check-transient-workspace.test.mjs`
- `check-transient-coordinates.py` -> `check-transient-coordinates.mjs`
- `check-transient-coordinates.test.py` -> `check-transient-coordinates.test.mjs`

Use `node:test` and `node:assert/strict` for tests.

Use built-in modules such as `node:fs`, `node:path`, `node:os`, `node:child_process`, and `node:url` as appropriate.

Preserve the useful existing test pattern of exercising the real checker against disposable temporary repositories rather than replacing behavioral tests with implementation-detail unit tests.

After migration, remove the superseded `.py` files rather than leaving wrappers or duplicate implementations.

## Remove Python as a repository-tooling runtime dependency

The end state should not require Python for Arcogine's maintained repository tooling.

Audit and migrate all current Python invocations, including inline scripting in GitHub Actions.

In particular, replace the inline Python logic in the CI aggregate `gate` job with Node logic that preserves the exact fail-closed semantics:

- required jobs failing/cancelled fail the gate;
- expected-to-run jobs unexpectedly skipped fail the gate;
- the `needs:` set and expectation-map set must remain exactly synchronized;
- intentionally skipped jobs remain acceptable only when the current classifier/event model says they should skip.

If the gate job needs a pinned Node runtime, set it up explicitly rather than relying on incidental runner state.

Final repository searches should show:

- no tracked `.py` repository tooling;
- no `python3` invocation in current Arcogine CI/tooling/docs;
- no maintained Python prerequisite for repository validation.

Do not treat actionlint's `-pyflakes=` option name as a Python runtime dependency.

## Bash audit: retain Bash only when it genuinely fits

Do not mechanically convert every `.sh` file.

For every maintained Bash helper/test under `.github/scripts/`, `infra/dev/`, and the root `arcogine` entry point, classify it as either:

1. **retain in Bash** because the behavior is fundamentally shell/environment/orchestration oriented; or
2. **migrate to Node** because the file primarily implements parsing, state evaluation, text processing, structured-data logic, or reusable repository policy.

Do the classification based on current implementation, not filename history.

### Strong Bash-retention candidates

These are expected to remain Bash unless current inspection shows otherwise:

- root `arcogine` — public shell developer entry point and thin external-tool orchestration;
- `.github/scripts/arcogine-cli.test.sh` — black-box test of the shell entry point using fake executables;
- `.github/scripts/arcogine-preflight.test.sh` — tests shell command-availability/preflight behavior;
- `infra/dev/claude-cloud.sh` and its test — shell/environment provisioning behavior;
- `infra/dev/git-identity.sh` and its test — shell/Git environment configuration;
- `.github/scripts/check-actions-workflows.sh` — thin download/verification/execution wrapper around the pinned external `actionlint` binary;
- a new top-level repository-tooling suite runner, if it remains only a thin sequential command orchestrator.

### Bash files that require an explicit migration decision

Inspect these rather than automatically retaining them:

- `.github/scripts/check-pr-disposition.sh`
- `.github/scripts/check-pr-disposition.test.sh`
- `.github/scripts/check-dependabot-provenance.sh`
- `.github/scripts/check-dependabot-provenance.test.sh`
- `.github/scripts/check-continuous-improvement-reminder.sh`
- `.github/scripts/classify-changes.sh`
- `.github/scripts/classify-changes.test.sh`
- `.github/scripts/discover-changed-files.sh`
- `.github/scripts/discover-changed-files.test.sh`

Guidance:

- parsing reviewer disposition text and base64/structured inputs is a strong candidate for Node;
- structured GitHub provenance validation currently implemented with Bash + `jq` is a strong candidate for Node and can potentially remove the `jq` dependency from that path;
- literal/text workflow-contract validation is a reasonable Node candidate;
- stdin/path classification and direct Git-range discovery may remain Bash if they are still simpler, clearer, and directly coupled to shell/Git pipeline semantics.

Do not migrate a Bash file merely to improve language-count optics. Do migrate it when Node makes the logic materially easier to test, reason about, or maintain.

Document the retained-Bash rationale concisely in code comments or the PR description only where it is not self-evident; do not create a new policy document solely for this matrix.

## Single-subject test ownership

Remove hidden aggregation from individual test files.

A file named for one subject must test only that subject.

At minimum:

- `classify-changes.test.sh` must test classification only;
- the PR-disposition test must test disposition semantics only;
- Dependabot provenance tests must test provenance semantics only;
- the continuous-improvement reminder contract check/test must own only that workflow contract;
- `check-actions-workflows.sh` must own workflow-definition syntax validation only.

Remove current tail calls in `classify-changes.test.sh` that execute unrelated Markdown-link, delivery-label, attribution-hygiene, or Git-identity suites.

Remove current tail calls in `check-pr-disposition.test.sh` that execute Dependabot provenance tests, actionlint, and continuous-improvement reminder checks.

Do not replace those hidden calls with another test file that secretly aggregates unrelated tests.

## Add one explicit repository-tooling suite runner

Introduce a semantically named canonical runner, preferably:

`.github/scripts/check-repository-tooling.sh`

A Bash implementation is appropriate **only if it remains thin orchestration**: `set -euo pipefail`, clear named sections, direct calls to independently runnable checks/tests, no domain parsing/policy logic.

The runner should be the explicit owner of "run the repository-tooling validation suite".

It should invoke the independent suites/checkers in a clear deterministic order, including as applicable:

- shell-native tests retained in Bash;
- migrated Node checker tests;
- each migrated checker against the actual candidate repository where that current behavior exists today;
- existing Node tooling tests under `infra/dev/`;
- attribution-hygiene checks/tests;
- Git identity tests;
- transient workspace/coordinate candidate-tree checks;
- PR disposition/provenance tests in their final language;
- continuous-improvement reminder contract validation;
- workflow-definition validation through the pinned actionlint helper;
- other currently always-required repository-tooling checks discovered during grounding.

Do not duplicate the same suite both inside the runner and as unrelated explicit CI steps unless there is a concrete reason.

Every component must remain independently runnable for focused debugging.

## CI simplification

Update `.github/workflows/ci.yml` so the always-running repository-tooling validation is obvious from the workflow.

Prefer one clearly named step such as:

`Validate repository tooling`

that invokes the canonical repository-tooling runner after setting up its required runtime(s).

Do not bury repository-tooling validation inside the changed-file classifier test.

Keep changed-file classification itself separate:

1. validate repository tooling;
2. determine changed-file surfaces;
3. conditionally run product/security jobs according to the existing classifier contract.

Preserve all current fail-safe classifier behavior unless a migration requires a behaviorally equivalent refactor.

Preserve the stable `CI / gate` semantics.

## Standard tooling-test conventions

Apply these conventions consistently to lightweight repository-tooling tests:

- deterministic exit code: 0 pass, non-zero fail;
- concise, descriptive test/case names;
- failures explain expected versus actual behavior;
- temporary repositories/directories are automatically cleaned up;
- tests do not mutate the real repository;
- tests use fake executables or isolated fixtures when exercising external command dispatch;
- no network by default;
- network is allowed only when the subject being validated inherently requires it, e.g. the pinned actionlint download helper;
- stdout is normal progress/success information;
- stderr is for checker/test failure diagnostics where the underlying CLI contract already follows that convention;
- no dependence on host-global packages when Node built-ins or repository-pinned tools suffice;
- preserve deterministic ordering where output is compared.

Do not create an abstraction framework merely to eliminate a few repeated fixture lines. Extract helpers only where duplication is substantial and the shared semantics are stable.

## Workflow-contract checks

Review literal workflow-contract checks individually.

Keep a mechanical workflow-contract check only when:

- the invariant is machine-observable;
- actionlint/GitHub syntax validation does not already prove it;
- violating it would materially weaken lifecycle/security behavior;
- the check can be expressed with low false-positive risk.

The continuous-improvement reminder's trigger/concurrency/permission/creation invariants are currently a defensible example.

Do not grow a generic collection of grep-based tests for agent/process prose or workflow wording.

If migrating a literal workflow check to Node, preserve the semantic invariant rather than necessarily preserving the exact implementation strategy.

## Node implementation conventions

For repository tooling migrated to Node:

- use ESM `.mjs` consistently with existing Arcogine tooling;
- use `node:test` and `node:assert/strict`;
- avoid npm dependencies unless current behavior cannot reasonably be implemented with built-ins;
- keep pure parsing/evaluation logic separable from process/filesystem adapters where that materially improves tests;
- use `execFileSync`, `spawnSync`, or equivalent argument-array APIs rather than shell-string execution when invoking Git or external tools;
- preserve exact exit-status boundaries expected by CI;
- preserve current environment-variable contracts where workflows depend on them unless changing the contract clearly simplifies the system and all callers/tests are updated atomically.

## Documentation reconciliation

Update `docs/development/testing.md` so it becomes the clear current authority for the repository-tooling stack and commands.

The final documentation should no longer describe Python suites.

Describe the simplified technology model:

- Java ecosystem for product;
- Node/`node:test` for repository logic/checkers;
- Bash for shell-native orchestration/environment boundaries;
- specialized external validators for their own formats/security domains.

Document the canonical repository-tooling runner and focused individual commands.

Update narrow references in `AGENTS.md` and other maintained docs only where filenames/tool names changed.

Do not duplicate a full command matrix into `AGENTS.md`; issue #376 separately owns hot-path command/authority consolidation.

## Compatibility and non-goals

Preserve:

- all existing product behavior;
- product test framework and Gradle build structure;
- current repository checker semantics unless a discovered bug is separately justified and called out;
- current workflow authorization/security semantics;
- current change-classification fail-safe behavior;
- current transient-workspace and transient-coordinate protections;
- current delivery-label namespace rules;
- current Markdown-link behavior;
- current repository snapshot/retrospective behavior;
- current actionlint version pinning and checksum verification unless there is an independently justified dependency update.

Non-goals:

- rewriting Java tests;
- adopting a new JavaScript test framework;
- adding TypeScript or a build/transpile step for repository tooling;
- converting shell-native provisioning/public entry points to Node merely for language uniformity;
- folding issue #376's broader `AGENTS.md` cleanup into this PR;
- changing PR disposition policy or Dependabot trust policy;
- changing CI job-selection semantics;
- introducing generalized plugin/test-framework architecture.

## Expected resulting shape

The exact file list may vary after live grounding, but a successful change should approximately produce:

### Node repository checks/tests

- `.github/scripts/check-markdown-links.mjs`
- `.github/scripts/check-markdown-links.test.mjs`
- `.github/scripts/check-delivery-labels.mjs`
- `.github/scripts/check-delivery-labels.test.mjs`
- `.github/scripts/check-transient-workspace.mjs`
- `.github/scripts/check-transient-workspace.test.mjs`
- `.github/scripts/check-transient-coordinates.mjs`
- `.github/scripts/check-transient-coordinates.test.mjs`

plus Node migrations of non-shell-native Bash policy/evaluator helpers where the audit supports them.

### Explicit orchestration

- `.github/scripts/check-repository-tooling.sh` as thin orchestration, unless current grounding demonstrates that a Node runner is simpler without adding complexity.

### Removed

- the eight current Python checker/test files;
- Python invocations and inline Python scripting from repository tooling/CI;
- hidden unrelated-suite invocations inside subject-specific test files.

## Acceptance evidence

At minimum prove:

### Python elimination

- no tracked `.py` files remain in maintained repository tooling;
- repo search finds no active `python3` invocation in CI/scripts/docs;
- CI no longer relies on inline Python;
- repository-tooling validation does not require Python.

### Checker behavioral parity

For each migrated checker, port and pass the existing behavioral cases, including positive, negative, malformed, false-positive, tracked/untracked, and path/content cases currently covered.

### Test ownership

Demonstrate that:

- classifier tests run and pass without invoking unrelated suites;
- disposition tests run and pass without invoking provenance/actionlint/reminder checks;
- each independent suite can be invoked directly;
- the canonical repository-tooling runner invokes all intended suites once.

### Bash-retention audit

The final PR description should summarize which Bash files remain and the category of reason:

- shell/public CLI;
- environment/provisioning;
- thin external-tool orchestration;
- direct Git/shell pipeline.

Do not serialize mutable branch/CI state into the PR body.

### Full tooling suite

Run the canonical repository-tooling runner successfully.

Also run any focused commands needed while migrating individual components.

### Java/product safety

Because this is repository tooling rather than product behavior, do not run expensive Java gates merely by reflex if current `AGENTS.md`/testing guidance does not require them for the changed surfaces. Run the narrowest applicable validation.

### Static repository checks

Run:

- `git diff --check`;
- Markdown link validation in its final Node form;
- delivery-label validation in its final Node form;
- transient-coordinate validation in its final Node form;
- transient-workspace validation after removing this prompt from the branch;
- actionlint/workflow validation;
- any other current merge-required repository-tooling validation identified by live grounding.

## Prompt cleanup

This prompt is transient committed delivery scaffolding.

Before independent PR review:

1. remove this file from `workspace/implementation/`;
2. verify no tracked `workspace/` path remains in the final candidate;
3. run the repository's tracked-workspace validation in its final migrated form;
4. inspect the branch for any other temporary files.

## Pull request

Create a focused implementation PR against current `main`.

The PR description should contain stable information only:

- why Python was removed;
- the final repository-tooling technology boundary;
- what Bash remains and why;
- how single-subject ownership and explicit suite orchestration changed;
- validation commands actually run.

Do not include mutable facts such as "branch is current with main", ahead/behind counts, current CI state, or mergeability.

Use normal independent PR review.

Do not merge autonomously.

## Final report

Return:

- live-main baseline actually used;
- branch;
- final implementation commit SHA;
- PR number/URL;
- Python files/invocations removed;
- Node files introduced/migrated;
- Bash files retained with concise rationale categories;
- Bash files migrated to Node, if any;
- repository-tooling runner path;
- CI orchestration changes;
- documentation changes;
- validation commands and results;
- confirmation that no tracked `workspace/` files remain in the PR candidate;
- any intentionally retained non-Node repository-tooling technology and why.
