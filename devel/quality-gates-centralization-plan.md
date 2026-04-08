Quality Gates Centralization Plan
================================

Date: 2026-04-08
Status: Planned
Scope: Consolidate all quality gates into Make targets for local dev and CI, remove duplicated logic, add self-documenting default behavior, and update all relevant docs.

Goals
1. Single source of truth
   - Keep check/build/test commands in Makefile as the canonical definitions.
2. No duplicated gate logic in CI
   - GitHub Actions should call Make targets, not re-encode shell command sequences.
3. Discoverable local entrypoint
   - Make should show available checks by default with no target selected.
4. Full documentation alignment
   - Update user-facing and contributor-facing docs to direct users to Make targets first.

Current baseline summary
- Makefile currently has useful Rust/frontend aggregates:
  - `test`, `test-rust`, `test-frontend`
  - `lint`, `coverage`, `coverage-rust`, `coverage-frontend`
  - `clean`
- `.github/workflows/ci.yml` currently hardcodes command sequences for each job.
- Docs currently list command snippets directly and are not fully consistent with a canonical Make surface.

Target state
1. Makefile owns all gate command semantics.
2. CI jobs are orchestration only (install toolchains/dependencies, run Make targets, upload reports/artifacts).
3. Default behavior of `make` is to print categorized gate help.
4. Both local dev and CI use the same target names and behavior.
5. Every check remains runnable independently and with clear composition.

Proposed Makefile model
1. Expand Makefile command surface (without renaming existing targets)
   - Keep existing behavior of:
     - `make test`
     - `make lint`
     - `make coverage`
     - `make clean`
   - Add granular leaf targets:
     - `make fmt`
     - `make clippy`
     - `make rust-test`
     - `make rust-coverage`
     - `make rust-audit`
     - `make frontend-lint`
     - `make frontend-typecheck`
     - `make frontend-test`
     - `make frontend-test:coverage`
     - `make frontend-build`
     - `make frontend-audit`
     - `make playwright`
     - `make docker-build`
     - `make docker-smoke`
     - `make trivy-scan-api`
     - `make trivy-scan-ui`
     - `make gitleaks`
   - Do not keep short aliases for back-compat where useful (`ui-` vs `frontend-`). This is a clean cut transition.

2. Add self-documenting entrypoint behavior
   - Set `.DEFAULT_GOAL := help` in Makefile.
   - Add:
     - `help` target that lists:
       - all available targets
       - category (core, frontend, ci, security)
       - short one-line description
    - `list` alias target as a compatibility short-form that delegates to `help` for teams that expect `make list`.
     - A `quality` or `quality-local` target that runs standard PR-ready checks.

3. Compose composite targets by dependency graph
   - `test = test-rust + test-frontend`
   - `lint = lint-rust + lint-frontend`
   - `coverage = coverage-rust + coverage-frontend + coverage-summary`
   - Add `quality-local = quality-prereqs + test + coverage` if that is intended.
   - Add `quality-ci-rust`, `quality-ci-frontend`, `quality-ci-playwright`, `quality-ci-docker` wrapper targets that CI can call.
   - Keep leaf targets idempotent and environment-agnostic where possible.

Implementation for CI-to-Make unification
1. Create dedicated CI targets used only by workflow jobs
   - `ci-rust`: `fmt`, `clippy`, `rust-test`, `rust-coverage` (if no external tarpaulin installation concerns).
   - `ci-frontend`: `frontend-lint`, `frontend-typecheck`, `frontend-test`, `frontend-build`, `frontend-audit`.
   - `ci-playwright`: `playwright` wrapper that expects API/UI deps already available.
   - `ci-docker`: `docker-build` + `docker-smoke`.
   - `ci-security`: `rust-audit`, `frontend-audit`, `gitleaks`, `trivy-scan-*`.
2. Update `.github/workflows/ci.yml` to call make targets:
   - Rust job:
     - install toolchain, then `make ci-rust` (or existing explicit plus `make` leaf calls)
   - Frontend job:
     - install `npm ci`, then `make ci-frontend`
   - Playwright job:
     - build API binary, install node, then `make ci-playwright`
   - Docker job:
     - create `.env`, then `make ci-docker`
   - Docker scan job:
     - run `make trivy-scan-api` and `make trivy-scan-ui`
   - Secret scan job:
     - run `make gitleaks` (install step can stay in workflow or be moved to helper target)
3. Keep non-gate responsibilities in workflow
   - Upload coverage/audit artifacts can stay in workflow.
   - Node/Rust tool installation and runner-specific setup can remain in workflow.
4. Ensure command outputs still match existing expectations
   - file paths used by existing artifacts (coverage and audit reports) remain unchanged where possible.

Doc update plan (all user and contributor facing files)
1. README.md
   - Add a short "Quality gates" section with `make` defaults and common targets.
   - Link CI to Make targets and mention quick local discovery command (`make`).
2. CONTRIBUTING.md
   - Replace manual command block under "Run the checks" with `make` command-first guidance.
   - Add a section "Choosing gate depth" with examples:
     - fast check: `make lint`
     - quick functional check: `make test`
     - full local gate: `make quality` (or chosen target)
3. TESTING.md
   - Add "Canonical commands live in Makefile."
   - Keep explicit command examples but map each to Make target names in parallel.
   - Update CI section to reflect that jobs execute Make targets.
4. docs/testing-strategy.md
   - Update CI quality gates section to state Makefile as gate source of truth.
5. ui/README.md
   - Add Make-driven validation commands for frontend checks.
   - Keep local `npm` steps as optional quick-start fallback.
6. SECURITY.md
   - Point dependency/security checks to Make targets where available.
   - Keep note that CI uploads and policy thresholds remain workflow-owned.
7. devel/ notes
   - Add this plan entry under `devel/quality-gates-centralization-plan.md` for traceability.
   - Optional follow-up: add `devel/quality-gates-rollout-checklist.md` when implementation starts.

Rollout order (safe and incremental)
1. Makefile scaffold
   - Add new helper targets and help/default behavior first.
   - Verify all existing `make test`, `make lint`, `make coverage` targets still behave unchanged.
2. Introduce CI-target-compatible composites
   - Add `ci-*` and `playwright/docker/trivy` wrappers.
   - Keep CI unchanged and validate targets run directly from shell.
3. Point workflow jobs to make
   - Swap duplicated command blocks to Make invocations one job at a time.
   - Update artifact paths only if outputs/locations changed.
4. Documentation updates
   - Apply changes across README, CONTRIBUTING, TESTING, docs/testing-strategy, ui/README, SECURITY.
5. Final quality validation
   - Run:
     - `make` (help output)
     - `make test`
     - `make lint`
     - `make coverage`
     - `make ci-rust`
     - `make ci-frontend`
     - `make ci-playwright`
   - Confirm docs remain consistent with actual target names.

Acceptance criteria
1. `make` with no target prints discoverable help grouped by area.
2. Existing local checks can be invoked using only Make for each gate.
3. GitHub workflow no longer duplicates gate command bodies for Rust, frontend, and Playwright checks.
4. At least one CI job still uploads all existing artifacts (coverage/security reports) as before.
5. Documentation references are consistent and point to Make as first-class interface.

Risks and mitigation
1. CI environment differences (tool installation, binary paths)
   - Keep environment bootstrapping in workflow, reserve gating logic in Make.
2. Target naming churn
   - Preserve existing target compatibility and add new aliases.
3. Report path changes
   - Keep legacy output filenames for minimal CI changes.
4. Over-eager refactor of command order
   - Roll out job-by-job and keep behavior-equivalent command ordering.

Open questions
1. Should `make quality` include `playwright` and `docker` smoke checks, or remain developer-light?
2. Should security jobs (`gitleaks`, `trivy`, audits) remain explicit under dedicated CI jobs or go through `make ci-security` + upload logic in workflow?
3. Do we want a `make pre-commit` target that wraps the minimum local gate for contributors (e.g. `fmt`, `clippy`, `frontend-lint`)?
