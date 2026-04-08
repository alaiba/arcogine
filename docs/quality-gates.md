# Quality Gate Centralization (archived)

This document captures the decisions from the completed quality-gate centralization
work and replaces the retired plan document as the canonical history for the contract
that drives local and CI checks.

## Scope

- Centralize quality-gate ownership in `Makefile` as the developer and CI command
  source of truth.
- Define gate contracts with a consistent `<domain>-<action>` naming convention.
- Add discoverability through default help behavior and a `list` alias.
- Keep documentation and CI in sync with Make targets.
- Make command-surface conventions explicit for future contributors and reviewers.

## Design goals

- One canonical quality-gate interface for local and CI execution.
- Eliminate duplicated command chains between docs and CI workflows.
- Provide clear discoverability from `make` and `make list`.
- Make security and runtime quality checks explicit, named, and easy to audit.

## Verified starting point (before implementation)

- Makefile previously exposed only `test`, `lint`, `coverage`, and `clean`.
- CI jobs ran full shell command chains directly in workflow files.
- Playwright, Docker, image scan, and secret scan commands were also encoded in workflow
  logic.
- `docs/TESTING.md` and `docs/CONTRIBUTING.md` were documenting direct shell snippets
  rather than canonical Make targets.
- Security scans were workflow-owned without a uniform Make command surface.

## Non-negotiable constraints

1. `Makefile` is the command surface of record for all quality gates.
2. CI jobs consume Make targets instead of duplicating full command bodies.
3. `make` without a target prints a grouped help screen.
4. `make list` exists as an intentionally kept alias for discoverability.
5. `clean` remains the single legacy utility target; all other command surfaces are
   domain-action leaves, composites, and `quality`/`quality-full`.
6. `help` and `list` are discovery targets outside the quality-gate namespace; they
   are part of the command contract but not quality gates themselves.

## Terminology

- `frontend-*` targets are logical checks on the UI codebase (lint, typecheck, tests,
  coverage, build, etc.).
- `ui` in scan targets refers to the concrete container artifact context used by
  deployment and CI workflows (`frontend` vs `ui` mapping).
- Mapping: `frontend` = quality domain, `ui` = deployable artifact owner.

## Target model

### Discovery targets

- `help` (`.DEFAULT_GOAL`), `list` (alias for `help`)

### Leaf targets

- `fmt`, `clippy`, `rust-test`, `rust-audit`, `rust-coverage`
- `frontend-lint`, `frontend-typecheck`, `frontend-test`, `frontend-coverage`,
  `frontend-build`, `frontend-audit`
- `playwright`
- `docker-build`, `docker-smoke`
- `trivy-scan-api`, `trivy-scan-ui`, `gitleaks`

### Composite targets

- `ci-rust`: `fmt` `clippy` `rust-test` `rust-coverage`
- `ci-frontend`: `frontend-lint` `frontend-typecheck` `frontend-coverage`
  `frontend-build` `frontend-audit`
- `ci-playwright`: `playwright`
- `ci-docker`: `docker-build` `docker-smoke`
- `ci-security`: `rust-audit` `frontend-audit` `trivy-scan-api`
  `trivy-scan-ui` `gitleaks`

### Developer entrypoints

- `quality` runs all fast gates (`fmt`, `clippy`, `rust-test`, `rust-coverage`,
  `frontend-lint`, `frontend-typecheck`, `frontend-test`, `frontend-coverage`,
  `frontend-build`).
- `quality-full` runs `quality` plus `playwright`, `docker-build`, `docker-smoke`,
  and `ci-security`.

## Implementation summary

1. Replaced legacy target names (`test`, `lint`, `coverage`, `test-rust`,
   `test-frontend`, `coverage-rust`, `coverage-frontend`, `coverage-summary`)
   with the domain-action naming model.
2. Added `.DEFAULT_GOAL := help`, grouped discovery (`help`/`list`), and `clean`.
3. Refactored CI jobs to consume Make targets:
   - `make ci-rust`
   - `make ci-frontend`
   - `make playwright`
   - `make ci-docker`
   - `make trivy-scan-*`
   - `make gitleaks`
   - `make rust-audit` (via `ci-security` composite; dry-run validated in CI, executed locally via `make quality-full`)
4. Added `make-contract` CI guard:  
   `make help && make list && make -n ci-rust ci-frontend ci-playwright ci-docker ci-security rust-audit`.
5. Updated command documentation so `make` targets are the canonical interface.

## Policy and rationale highlights

- Security installation and policy handling remain in workflow steps; command
  invocation lives in Make targets for consistency.
- Coverage output is canonical in `target/coverage/cobertura.xml`; CI uploads from that
  path (coverage in target/coverage is the canonical artifact location for this contract).
- Playwright target scope is intentionally narrow (`cd ui && npx playwright test`).
  API/browser bootstrap and dependency setup stays in CI or explicit developer setup.
- `make ci-rust` is blocking and includes coverage in the same composite gate.

## Trade-offs and findings (archived)

trade-offs and findings are archived here for historical review.

### F1 — `quality` vs `quality-full` boundary

Keep `quality` fast and lightweight, with heavy checks (`playwright`, docker checks, and
security composite) in `quality-full`.

### F2 — Make vs CI responsibility for security scans

Keep install/policy flow in CI where needed, but make scan execution itself a Make
target (`trivy-scan-*`, `gitleaks`) for a stable API.

### F3 — Target naming correctness

Use hyphenated target names only (`frontend-coverage`) to avoid Make parser pitfalls.

### F4 — Coverage blocking behavior

Coverage runs inside `ci-rust` as part of blocking quality gates; no separate
soft-fail coverage branch.

### F5 — Rust dependency audit parity

`rust-audit` exists as a Make target and is part of `ci-security`.

### F6 — Contract drift control

The `make-contract` CI job checks target presence and dry-runability.

### F7 — Coverage artifact drift

CI reads `target/coverage/cobertura.xml` to match Make output location.

### F8 — Frontend working directory alignment

Frontend CI runs `make` from repository root; no job-level `working-directory: ui` remains.

### F9 — Legacy naming retirement

Legacy names were removed to avoid ambiguity with new domain-action names.

### F10 — `docker-build` vs `docker-smoke` scope

`docker-build` is build-only; `docker-smoke` includes env copy, compose up/wait,
health checks, and teardown.

### F11 — Playwright target scope

`playwright` does not install dependencies or build the backend binary; those are CI
bootstrap concerns.

### F12 — Documentation cross-reference hygiene

Line references and command claims were corrected where they were stale.

## Governance

### Quality-gate change protocol

Any quality-gate change must be updated in:

1. `Makefile`
2. `.github/workflows/ci.yml`
3. Referenced docs (`docs/TESTING.md`, `docs/testing-strategy.md`, `docs/SECURITY.md`,
   and contributor-facing docs where affected).

Governance checks are currently enforced by the contract job (dry-run validation) and
manual parity review of `make help` output against CI target usage.

## Validation and notes

Validation is split into documented steps and recorded outcomes:

1. Discovery: `make` and `make list` output are grouped and identical where required.
2. Local matrix: `make quality` and focused leaf-target execution for fast local verification.
3. CI parity: workflows now call Make composites and report target names in the gate matrix.
4. Docs parity: target matrix and command references aligned to Make targets.
5. Governance: presence of the contract check and documented protocol.
6. CI guard: `make help && make list && make -n ci-rust ci-frontend ci-playwright ci-docker ci-security rust-audit`.

Executed/recorded in the implementation history:

- Discovery, local matrix, governance, and make-contract checks were completed.
- CI parity still requires an actual CI run for runtime confirmation of all jobs.
- Full policy scope is represented by `make quality-full`.
- Historical implementation evidence recorded that `make quality` executed end-to-end with passing
  Rust test counts (e.g., 234 `rust-test` cases) and frontend tests (51+ `frontend-test` cases).

## Out of scope

- Changing thresholds (e.g., stricter lint/linting policies)
- Replacing Make with a different task runner
- CI runner/base image changes unrelated to command orchestration.

## Future enhancements

- **`make contract-smoke`**: Add a lightweight runtime contract check that executes
  fast, deterministic targets (e.g., `make fmt`, `make clippy`, `make frontend-lint`)
  to prove command bodies are executable beyond dry-run validation. This would
  complement the existing `make-contract` dry-run guard and catch broken command bodies
  that `make -n` cannot detect. See F2 and F4 in Findings for rationale.

## Implementation order

1. Phase 1, then phase 2: establish the command contract before changing workflow behavior.
2. Phase 3: shift orchestration to Make only after the contract stabilized.
3. Phase 4: immediately align documentation with the new surface.
4. Phase 5: apply governance and contract checks for future drift.

## Deviations and implementation notes

- The docker scan job now calls `make trivy-scan-${{ matrix.image }}` directly and installs
  `trivy` in CI for execution control.
- Coverage upload was adjusted to `target/coverage/cobertura.xml` to match Make output without
  duplicating path-shifting logic.
- Build/runtime fixes that accompanied this migration:
  - `crates/sim-cli/src/main.rs`: fixed closing brace for the `mod tests` block.
  - `crates/sim-core/tests/scenario_loading.rs`: consolidated duplicate scenario tests into
    a shared helper.

## Findings

### F1: `testing-strategy.md` overstates `rust-audit` as a CI job [Applied]
<!-- severity: minor -->
<!-- dimension: correctness -->

**Context:** `docs/testing-strategy.md` lists `make rust-audit` as part of CI security jobs (`docs/testing-strategy.md:104`), while `.github/workflows/ci.yml` runs only `make ci-rust`, `make ci-frontend`, `make playwright`, `make ci-docker`, `make trivy-scan-*`, `make gitleaks`, and a contract dry-run (`.github/workflows/ci.yml:43`, `.github/workflows/ci.yml:71`, `.github/workflows/ci.yml:127`, `.github/workflows/ci.yml:138`, `.github/workflows/ci.yml:160`, `.github/workflows/ci.yml:176`, `.github/workflows/ci.yml:184`).

**Issue:** The current documentation implies Rust dependency auditing is a CI-enforced quality gate, but the workflow does not execute `make rust-audit` in any job. That mismatch weakens confidence in CI parity and can mislead contributors.

**Recommendation:** Align docs and implementation by either adding a CI job for `make rust-audit` or changing the security job list to explicitly call it local-only (as part of `make quality-full`) in the strategy docs.

**Choices:**
- [x] Update `docs/testing-strategy.md` to keep Rust audit out of the CI job list and clarify that it is part of `quality-full` local execution.
- [ ] Add a dedicated CI job for `make rust-audit` and keep `docs/testing-strategy.md` unchanged.

### F2: Contract validation checks only target syntax [Applied]
<!-- severity: minor -->
<!-- dimension: testing -->

**Context:** The contract job and plan both validate with `make -n` only (`.github/workflows/ci.yml:184`; `docs/quality-gates.md:94`, `docs/quality-gates.md:187`).

**Issue:** `make -n` confirms target existence and dependency resolution, but does not execute command bodies. A broken command body (for example a typo in a flag or a missing tool expectation) can pass the contract check and only fail later in manual verification.

**Recommendation:** Add a lightweight runtime smoke contract in CI that executes a small, bounded set of representative targets in addition to the current dry-run, while keeping expensive/slow targets out of the contract gate.

**Choices:**
- [x] Keep the current dry-run and add a separate `make contract-smoke` target/job that runs fast, deterministic checks (e.g., `make fmt`, `make clippy`, `make frontend-lint`) to prove command bodies are executable.
- [ ] Keep dry-run-only to preserve strict runtime budget in the contract gate.

### F3: Non-negotiable constraints omit discovery targets [Applied]
<!-- severity: minor -->
<!-- dimension: best-practices -->

**Context:** `docs/quality-gates.md` defines non-negotiables as "`clean` remains the single legacy utility target; all other command surfaces are domain-action leaves, composites, and `quality`/`quality-full`" (`docs/quality-gates.md:35-40`), while `help` and `list` are also explicitly introduced and are part of the user-facing contract (`docs/quality-gates.md:10-12`, `Makefile:10-20`).

**Issue:** The wording creates ambiguity about whether discovery targets are within scope of the command-contract model, which can complicate PR review and implementation checks.

**Recommendation:** Clarify the constraint by explicitly naming discovery/intent surfaces (`help`, `list`) as an exception category separate from quality gate surfaces.

**Choices:**
- [x] Add a brief exception in the non-negotiable list and target-model section to keep discoverability targets explicit.
- [ ] Keep the current wording and document the exception verbally during reviews.

### F4: `contract-smoke` presented as completed work but does not exist [Applied]
<!-- severity: major -->
<!-- dimension: correctness -->

**Context:** The F2 application added `contract-smoke` to the Implementation summary (step 5, `docs/quality-gates.md:101-103`), Governance section (`docs/quality-gates.md:185`), and Validation section (`docs/quality-gates.md:198`) using past tense ("Added `make contract-smoke`"). However, the document header says "archived" and describes "completed quality-gate centralization work" (`docs/quality-gates.md:1-5`). Neither `Makefile` nor `.github/workflows/ci.yml` contain a `contract-smoke` target.

**Issue:** An unimplemented recommendation is presented as a past-tense accomplishment in an archived record. A coding agent or contributor reading this document would expect `contract-smoke` to already exist, creating confusion and potentially blocking work that depends on it.

**Recommendation:** Reframe the `contract-smoke` references as a planned future enhancement rather than a completed step. Move them out of the Implementation summary and Validation sections into a new "Future enhancements" section, or add them to the existing "Out of scope" section with a note that they are recommended additions.

**Choices:**
- [x] Move `contract-smoke` references out of past-tense sections into a new "Future enhancements" section below "Out of scope".
- [ ] Remove `contract-smoke` entirely and revert the F2 application to the archived document.
- [ ] Keep the current wording and add `contract-smoke` to the Makefile and CI now.
<!-- Depends on: F2 choice "Keep the current dry-run and add a separate `make contract-smoke`" -->

### F5: Hardening test count claims 19 but lists 18 [Applied]
<!-- severity: minor -->
<!-- dimension: correctness -->

**Context:** `docs/testing-strategy.md:169` states "This hardening layer added 19 new tests" but the named list at lines 151-168 contains exactly 18 test names.

**Issue:** The off-by-one count could cause a contributor verifying test coverage to waste time searching for a missing 19th test.

**Recommendation:** Correct the count to match the list, or identify and add the missing 19th test name.

**Choices:**
- [x] Update the count from 19 to 18 to match the listed test names.
- [ ] Identify the missing 19th test and add it to the list.

### Summary

| # | Title | Severity | Dimension | Depends on |
|---|-------|----------|-----------|------------|
| F1 | `testing-strategy.md` overstates `rust-audit` as a CI job | minor | correctness | — |
| F2 | Contract validation checks only target syntax | minor | testing | — |
| F3 | Non-negotiable constraints omit discovery targets | minor | best-practices | — |
| F4 | `contract-smoke` presented as completed work but does not exist | major | correctness | F2 |
| F5 | Hardening test count claims 19 but lists 18 | minor | correctness | — |
