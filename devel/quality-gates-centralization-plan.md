# Analysis → Structured Plan

> **Date:** 2026-04-08
> **Scope:** Centralize all quality-gate commands in Make as the single developer and CI source-of-truth using a clean `<domain>-<action>` naming convention, add discoverable defaults (`help`/`list`), and align documentation.
> **Primary sources:** Makefile:1-45, .github/workflows/ci.yml:17-225, TESTING.md:126-154, CONTRIBUTING.md:90-126, README.md:18-75, ui/README.md:34-49, SECURITY.md:49-51, docs/testing-strategy.md:96-124

---

## 1. Goal

- Create one canonical quality-gate surface so developers and CI can run the same checks from Make.
- Eliminate duplicated gate command bodies between local guidance and CI workflows.
- Make check discoverability frictionless via a default `make` target and a `list` alias.
- Synchronize documentation in contributor-facing and user-facing docs to point to Make targets.

---

## 2. Non-Negotiable Constraints

1. `Makefile` remains the command surface of record for quality gates; CI must orchestrate calls to Make targets instead of re-listing full command chains (`Makefile:1-45`, `.github/workflows/ci.yml:39-153`).
2. The entire existing Makefile target surface (`test`, `lint`, `coverage`, `test-rust`, `test-frontend`, `coverage-rust`, `coverage-frontend`, `coverage-summary`, `clean`) is replaced by the new `<domain>-<action>` leaf targets, composites, and `quality`/`quality-full` entrypoints. No backward-compatible aliases are retained.
3. There must be self-documenting discovery when users run `make` with no target (`Makefile:1-45`).
4. `make list` must be present and intentionally kept as an alias for discoverability familiarity (`Makefile:1-45`).
5. This plan captures the target architecture first; code execution changes are not applied as part of this plan rewrite.

---

## 2.1. Terminology note: `frontend` vs `ui`

- `frontend-*` targets in this plan refer to logical checks on the UI codebase (linting, type checking, tests, build, and frontend coverage).
- `ui` in scan targets (for example, `trivy-scan-ui`) refers to the concrete image/service artifact context used by CI and container workflows.
- Mapping used throughout this plan: `frontend` = quality domain, `ui` = deployable artifact owner.

---

## 3. Verified Current State

### 3.1 Makefile quality coverage is partial and duplicated at call sites
The current Makefile already groups Rust and frontend checks under `test`, `lint`, `coverage`, and `clean`, but it does not provide a default help target, a `list` alias, or CI-oriented wrapper targets for Playwright and docker/image scans (`Makefile:1-45`). This means discoverability and CI orchestration are not centralized yet.

### 3.2 CI currently executes explicit command chains that duplicate local intent
The Rust and frontend workflow jobs each install dependencies, then run shell command sequences for checks directly in YAML (`.github/workflows/ci.yml:39-63`, `.github/workflows/ci.yml:65-114`). Playwright, docker, docker image scan, and secret scan jobs similarly encode command specifics inline (`.github/workflows/ci.yml:115-225`).

### 3.3 Project docs currently describe checks as direct shell snippets
`TESTING.md` lists inline commands for rust checks, frontend checks, Playwright, and coverage (`TESTING.md:21-154`), and `CONTRIBUTING.md` mirrors this with manual check commands (`CONTRIBUTING.md:94-105`). `README.md` and `ui/README.md` describe start/dev/test commands independently from Make (`README.md:18-75`, `ui/README.md:20-49`).

### 3.4 Security and hardening checks are currently workflow-owned
Dependency and security scans are present in CI jobs with dedicated steps and uploads, but they are not grouped under a Make-based command API (`.github/workflows/ci.yml:48-63`, `.github/workflows/ci.yml:104-114`, `.github/workflows/ci.yml:186-224`, `.github/workflows/ci.yml:211-224`, `SECURITY.md:49-51`).

---

## 4. Recommended Approach

(Recommended) Introduce a strict "Make-first, CI-orchestrated" quality-gate model where Make owns every command body and CI consumes Make targets, while wrappers and docs absorb the existing command set without changing outcomes.

Rationale:
- This prevents drift between local and CI gate execution.
- It gives immediate discoverability through `make`/`make help`/`make list` for all contributors.
- It keeps environment and tool provisioning in CI while moving reusable gating logic into versioned Make targets (`Makefile:1-45`, `.github/workflows/ci.yml:17-225`).

---

## 5. Phased Plan

### Phase 1. Establish Make discoverability and command contract [Done]

Objective: Replace the existing Makefile with a clean `<domain>-<action>` target surface and make `make` the entrypoint for understanding and running quality gates.

Planned work:
1. Replace the entire existing Makefile content with the new target surface. Remove all legacy targets (`test`, `lint`, `coverage`, `test-rust`, `test-frontend`, `coverage-rust`, `coverage-frontend`, `coverage-summary`, `clean`).
2. Add `.DEFAULT_GOAL := help` near the top of `Makefile` so `make` invokes help output by default.
3. Add a `help` target that prints grouped `<domain>-<action>` leaf targets, composites, and `quality`/`quality-full` entrypoints.
4. Add a `list` target that delegates to `help`.
5. Add a `clean` target (this is a universal build convention, not a quality gate alias).

Files expected:
- `Makefile`

Acceptance criteria:
- Running `make` prints a categorized help screen and exits successfully.
- Running `make list` prints identical output to `make help`.
- No legacy target names (`test`, `lint`, `coverage`, `test-rust`, `test-frontend`, `coverage-rust`, `coverage-frontend`, `coverage-summary`) exist in the new Makefile.

### Phase 2. Consolidate gate primitives and composites in Make [Done]

Objective: Define explicit leaf and composite targets for each Rust/frontend gate so CI and developers can call the same command units.

Planned work:
1. Add explicit leaf targets for existing operations using the single naming convention `<domain>-<action>`:
   (`fmt`, `clippy`, `rust-test`, `rust-audit`, `rust-coverage`, `frontend-lint`, `frontend-typecheck`, `frontend-test`, `frontend-coverage`, `frontend-build`, `frontend-audit`, `playwright`, `docker-build`, `docker-smoke`, `trivy-scan-api`, `trivy-scan-ui`, `gitleaks`) and map each to the command body currently in CI/docs (`Makefile:3-45`, `.github/workflows/ci.yml:39-224`, `TESTING.md:21-154`, `SECURITY.md:49-51`, `CONTRIBUTING.md:94-105`).
2. Define composite targets (`ci-rust`, `ci-frontend`, `ci-playwright`, `ci-docker`, `ci-security`) that orchestrate the leaf targets without encoding command bodies.
   - Keep `ci-security` scoped to Make-owned scan targets (`rust-audit`, `frontend-audit`, `trivy-scan-api`, `trivy-scan-ui`, `gitleaks`).
3. Add `quality` and `quality-full` as the developer-facing entrypoints:
   - `quality` runs all fast gates: `fmt`, `clippy`, `rust-test`, `rust-coverage`, `frontend-lint`, `frontend-typecheck`, `frontend-test`, `frontend-coverage`, `frontend-build`.
   - `quality-full` runs `quality` plus `playwright`, `docker-build`, `docker-smoke`, and `ci-security`.
4. No legacy target names are retained. The complete developer surface is: leaf targets, composites, `quality`, `quality-full`, `help`, `list`, and `clean`.

Files expected:
- `Makefile` (will grow significantly beyond current 46 lines)

Acceptance criteria:
- Every CI gate body line in docs and workflow has an equivalent Make target by name.
- `make quality` and `make quality-full` are the primary developer entrypoints.
- Composed targets are deterministic and call only Make-defined prerequisites.
- `.PHONY` declaration covers all targets.
- No legacy names (`test`, `lint`, `coverage`, `test-rust`, `test-frontend`, `coverage-rust`, `coverage-frontend`, `coverage-summary`) exist.

### Phase 3. Refactor CI to consume Make targets [Done]

Objective: Remove duplicated shell command chains from workflow jobs and make CI orchestration explicit.

Planned work:
1. Replace Rust job check commands with Make-based steps:
   - Replace Rust checks in CI with a single `make ci-rust` step after toolchain setup (`.github/workflows/ci.yml:39-63`) so coverage is included in the blocking Rust quality gate.
   - The `rust-coverage` leaf target writes all outputs to `target/coverage/` (`cobertura.xml` + HTML). CI's Codecov upload reads `target/coverage/cobertura.xml`.
2. Replace frontend job commands with `make ci-frontend` after `npm ci` (`.github/workflows/ci.yml:65-114`).
   - Remove the `defaults: run: working-directory: ui` block from the frontend job so that `make ci-frontend` executes from the repo root where `Makefile` lives. The Make target handles `cd ui &&` internally.
3. Replace Playwright commands with `make ci-playwright` while keeping server setup/build steps in workflow (`.github/workflows/ci.yml:115-154`).
   - `make playwright` (the leaf) runs only `cd ui && npx playwright test`. Binary build (`cargo build -p sim-cli`), Node/browser install, and `npm ci` remain as workflow steps before `make playwright` is invoked.
4. Replace docker build/start commands with `make ci-docker` and keep environment bootstrap lines in workflow (`.github/workflows/ci.yml:156-184`).
   - `make docker-build` wraps `docker compose build`. `make docker-smoke` wraps `cp .env.example .env && docker compose up -d --wait --wait-timeout 120`, health-check curls, and `docker compose down`. Failure-log printing remains a CI-only conditional step.
5. Keep artifact upload/reporting steps in CI and point report paths to existing outputs to avoid report-break drift (`.github/workflows/ci.yml:57-63`, `.github/workflows/ci.yml:93-113`, `.github/workflows/ci.yml:172-184`).
6. Refactor security-orchestration jobs to call Make wrappers for scan execution (`make rust-audit`, `make trivy-scan-api`, `make trivy-scan-ui`, and `make gitleaks`) while workflow-owned bootstrap and policy handling (install steps, fail-fast/exit policy) remain in CI (`.github/workflows/ci.yml:39-225`, `SECURITY.md:39-51`).
   - Replace `rustsec/audit-check@v2` with `make rust-audit`, ensuring `rust-audit` handles `cargo install cargo-audit` and `cargo audit` execution inside the Make target.

Files expected:
- `.github/workflows/ci.yml:17-225`

Acceptance criteria:
- CI logs show Make targets invoked in each relevant job instead of repeated manual command bodies for linting, tests, type checks, coverage, Playwright, docker, and scans.
- Existing uploads remain intact and continue to write/consume the same report locations where already consumed by CI.
- The frontend CI job no longer sets `defaults: run: working-directory: ui`; all path handling is internal to Make targets.

### Phase 4. Update docs and contributor surfaces [Done]

Objective: Make Make the canonical quality interface in docs and remove ambiguity between docs and command execution.

Planned work:
1. Replace long command lists in `TESTING.md` with explicit "canonical target" mappings, while retaining examples as convenience (`TESTING.md:21-154`).
2. Update `CONTRIBUTING.md` checklist to use `make help`, `make quality`, `make quality-full`, and gate-specific leaf targets (`CONTRIBUTING.md:90-105`).
3. Add a short quality-gates section in `README.md` for discoverability (`README.md:18-45`).
4. Update `ui/README.md` with Make-oriented frontend validation commands (`ui/README.md:34-49`).
5. Align `docs/testing-strategy.md` and `SECURITY.md` with Make-first gate ownership (`docs/testing-strategy.md:96-124`, `SECURITY.md:39-51`).

Files expected:
- `TESTING.md:1-154`
- `CONTRIBUTING.md:90-126`
- `README.md:18-75`
- `ui/README.md:34-49`
- `docs/testing-strategy.md:96-124`
- `SECURITY.md:39-51`

Acceptance criteria:
- All referenced docs explicitly state Make as the primary quality-gate entrypoint.
- At least one doc command matrix maps every check to its canonical Make target.

### Phase 5. Stabilize rollout and governance [Done]

Objective: Ensure no command drift over time and reduce regression risk for future quality-gate changes.

Planned work:
1. Add a short governance note to this plan file and, if needed later, a lightweight rollout checklist file.
2. Require PR updates for both Makefile and relevant docs whenever gate commands change.
3. Add periodic quick review of `make help` output against workflow target usage during release prep.

Files expected:
- `devel/quality-gates-centralization-plan.md`

Acceptance criteria:
- Command execution and documentation remain synchronized in periodic review.
- New checks are added to both `Makefile` and workflow/docs in the same change set.

#### Governance note

> **Quality-gate change protocol (2026-04-08)**
>
> Any change to a quality gate command (adding, renaming, removing a Make target or altering its body) must update **all three** of:
> 1. `Makefile` — the target definition.
> 2. `.github/workflows/ci.yml` — the CI step that invokes it.
> 3. Docs (`TESTING.md`, `CONTRIBUTING.md`, `README.md`, `ui/README.md`, `docs/testing-strategy.md`, `SECURITY.md`) — whichever reference the affected target.
>
> The `make-contract` CI job (`make help && make list && make -n ci-rust ci-frontend ci-playwright ci-docker ci-security rust-audit`) catches target regressions automatically.
>
> During release prep, run `make help` and diff its output against the CI workflow to verify parity.

---

## 6. Validation Plan

1. Review dry-run command surface:
   - Run `make` and `make list` and confirm target groups are visible and aligned with phase artifacts.
2. Validate local target matrix:
   - Run `make quality` and spot-check individual leaf targets (`make rust-test`, `make frontend-lint`, etc.).
3. Validate CI parity:
   - Confirm each CI job log begins from corresponding Make targets (`ci-rust`, `ci-frontend`, `ci-playwright`, `ci-docker`, `ci-security`, `rust-audit`) and that outputs are generated at expected paths.
4. Validate docs:
   - Verify docs in `README.md`, `TESTING.md`, `CONTRIBUTING.md`, `ui/README.md`, and `SECURITY.md` describe identical target names for identical checks.
5. Governance check:
   - Verify every gate command introduced in this plan appears in `devel/quality-gates-centralization-plan.md` and Makefile target definitions.
6. CI/CD guard:
   - Add a lightweight make contract smoke that executes `make help`, `make list`, and `make -n ci-rust ci-frontend ci-playwright ci-docker ci-security rust-audit` to catch syntax/target regressions during implementation and gate reviews.

---

## 7. Implementation Order

1. Phase 1, then 2: establishes the Make command contract before changing workflow behavior.
2. Phase 3: shifts CI orchestration only after command contract and naming are stable.
3. Phase 4: avoids documentation mismatch immediately after CI changes.
4. Phase 5: applied as process debt prevention after technical and doc changes land.

---

## 8. Out of Scope

- Changing the actual quality thresholds (for example, adding stricter audit policies or new linters).
- Changing CI runners, base images, or non-gate infrastructure.
- Reworking test content or adding new functional coverage unrelated to gate orchestration.
- Introducing a separate task runner (for example, Nx/Turbo) to replace Make.

## Findings

### F1 [Applied]: Scope for `make quality` versus `make quality-full`
<!-- severity: minor -->
<!-- dimension: best-practices -->

**Context:** The plan proposes multiple quality groupings (`quality`, `quality-full`) and the existing repository has both fast gates and slower gates like Playwright/docker scans (`Makefile:3-45`, `.github/workflows/ci.yml:115-224`).

**Issue:** If the boundary is not fixed, contributors and CI operators may unintentionally skip heavy checks or run too much locally, creating inconsistency.

**Recommendation:** Define `make quality` to run all fast gates (`fmt`, `clippy`, `rust-test`, `rust-coverage`, `frontend-lint`, `frontend-typecheck`, `frontend-test`, `frontend-coverage`, `frontend-build`), and `make quality-full` to add `playwright`, `docker-build`, `docker-smoke`, and `ci-security`.

**Choices:**
- [x] `quality` (fast gates) and `quality-full` (full stack) as the two developer entrypoints.
- [ ] Single `quality` target that always runs everything including Playwright/docker/security.

### F2 [Applied]: Whether security scans stay in CI workflows or move to Make wrappers
<!-- severity: minor -->
<!-- dimension: plan-hygiene -->

**Context:** Security scans are currently explicit workflow steps with custom install/exit handling (`.github/workflows/ci.yml:186-224`, `SECURITY.md:49-51`).

**Issue:** If Make takes full control of scan installation behavior, workflow portability can become harder; if CI owns too much, duplication remains.

**Recommendation:** Keep installation and policy steps in workflow, but move scan invocations to Make targets (`trivy-scan-*`, `gitleaks`) for a consistent command API.

**Choices:**
- [x] Make-based command wrappers for scan execution, workflow-owned bootstrap and policy handling.
- [ ] Fully migrate scan bootstrap and policy enforcement into Make.

### F3 [Applied]: Invalid Make target name in leaf target list
<!-- severity: minor -->
<!-- dimension: correctness -->

**Context:** Phase 2 currently calls out a coverage leaf target with a colon-style name in its naming guidance (`devel/quality-gates-centralization-plan.md:83-87`) and no matching Make identifier currently exists.

**Issue:** Using a colon inside a Make target name without escaping is parsed as a rule separator, so a literal `frontend-test:coverage`-style name would fail parsing when executed.

**Recommendation:** Use a hyphenated Make target name (`frontend-coverage`) and reuse that name consistently across composites, `help` output, and `quality-full` composition.

**Choices:**
- [x] Standardize on `frontend-coverage` and update all references accordingly.
- [ ] Keep a colon-in-name target by escaping it (`frontend-test\:coverage`) wherever required.

### F4 [Applied]: Require coverage in `make ci-rust` and enforce blocking failures
<!-- severity: minor -->
<!-- dimension: plan-hygiene -->

**Context:** The current implementation plan and existing CI workflow previously modeled coverage as a soft-fail step (`continue-on-error: true`) separate from other Rust checks (`.github/workflows/ci.yml:53-55`).

**Issue:** Keeping a separate soft-fail coverage path reintroduces policy divergence from the requested blocking quality gate direction and weakens consistency for CI and local Make-driven quality workflows.

**Recommendation:** Collapse coverage back into `make ci-rust` and remove `continue-on-error` so Rust quality failures, including coverage failures, are blocking and consistently reported through one target.

**Choices:**
- [x] Require coverage in `make ci-rust` and remove `continue-on-error` so all coverage failures become blocking.
- [ ] Split Rust checks into `make ci-rust` (blocking) plus a separate `make rust-coverage` step with `continue-on-error: true`.

### F5 [Applied]: `ci-security` scope for Rust dependency audit mismatch
<!-- severity: minor -->
<!-- dimension: correctness -->

**Context:** The plan’s `ci-security` composite target now includes dependency checks (`.github/workflows/ci.yml:39-225`) and Rust audit coverage must be represented in Make-compatible form.

**Issue:** Without a dedicated Make target, either the audit check remains workflow-owned or parity is lost between CI and the canonical `ci-security` contract.

**Recommendation:** Add a command-based `rust-audit` target that installs `cargo-audit` and runs `cargo audit`, then include it in `ci-security` and invoke it from CI so Rust dependency auditing follows the same Make orchestration model.

**Choices:**
- [ ] Scope `ci-security` to Make-owned scan targets and keep Rust audit action in the workflow until a command wrapper is intentionally added.
- [x] Add a new `rust-audit` Make target now that installs `cargo-audit` and executes `cargo audit`, then include it in `ci-security`.

### F6 [Applied]: Add automated contract check for Makefile target updates
<!-- severity: minor -->
<!-- dimension: testing -->

**Context:** The validation plan is mostly review-based (`devel/quality-gates-centralization-plan.md:158-168`) and does not currently require a machine-checked target contract check beyond manual review.

**Issue:** Without an automated Makefile contract check, a malformed or missing target can be introduced by an MR and not be caught until a later gate job fails, increasing review latency.

**Recommendation:** Add a scripted parity/smoke check that runs `make help`, `make list`, and representative composite targets in dry-run mode, so target presence and spelling are validated continuously.

**Choices:**
- [x] Add an automated make-contract smoke check (e.g., `make help && make list && make -n ci-rust ci-frontend ci-playwright ci-docker ci-security rust-audit`) to CI validation.
- [ ] Keep purely manual verification in the implementation PR checklist.

### F7 [Applied]: Tarpaulin output-path divergence between local and CI
<!-- severity: major -->
<!-- dimension: correctness -->

**Context:** The existing Makefile `coverage-rust` target runs `cargo tarpaulin --workspace --out xml --out html --output-dir target/coverage --skip-clean` (`Makefile:24`), producing `target/coverage/cobertura.xml`. CI line 54 runs `cargo tarpaulin --workspace --out xml --skip-clean`, producing `cobertura.xml` at the repo root. The Codecov upload step reads `cobertura.xml` (`.github/workflows/ci.yml:61`).

**Issue:** If `ci-rust` reuses the local `coverage-rust` target as-is, the XML output lands in `target/coverage/` and the Codecov upload step finds nothing at `cobertura.xml`. Coverage upload silently fails.

**Recommendation:** ~~Define `rust-coverage` with CI-compatible flags producing `cobertura.xml` at root.~~ (2026-04-08) Resolved by pointing CI's Codecov upload at `target/coverage/cobertura.xml` instead of copying to root. Simpler: one canonical output directory, no post-processing.

**Choices:**
- [ ] Single `rust-coverage` target producing XML at root (plus optional local HTML).
- [x] Change the CI Codecov upload to read from `target/coverage/cobertura.xml` instead.

### F8 [Applied]: Frontend CI job `defaults: working-directory` must be removed
<!-- severity: major -->
<!-- dimension: correctness -->

**Context:** The frontend CI job sets `defaults: run: working-directory: ui` (`.github/workflows/ci.yml:68-70`). Phase 3 replaces inline commands with `make ci-frontend`.

**Issue:** If `working-directory: ui` remains, `make` is invoked from `ui/` where no Makefile exists, causing immediate failure. The plan did not mention this required removal.

**Recommendation:** Phase 3 step 2 must explicitly require removing the `defaults: run: working-directory: ui` block from the frontend job when switching to `make ci-frontend`.

**Choices:**
- [x] Remove `defaults: run: working-directory: ui` from the frontend job and let Make handle `cd ui &&` internally.
- [ ] Add a symlink or secondary Makefile in `ui/` to forward targets to the root Makefile.

### F9 [Applied]: Legacy `<action>-<domain>` names removed
<!-- severity: major -->
<!-- dimension: best-practices -->

**Context:** The current Makefile includes legacy names (`test-rust`, `test-frontend`) while this plan is standardizing on domain-first target names (`rust-test`, `frontend-test`).

**Issue:** Coexisting legacy and new-style names increases implementation and review risk, and creates ambiguity for contributors when choosing the canonical Make entrypoint.

**Recommendation:** Adopt `<domain>-<action>` as the sole naming rule. Remove all legacy names (`test`, `lint`, `coverage`, `test-rust`, `test-frontend`, `coverage-rust`, `coverage-frontend`, `coverage-summary`). Use `quality`/`quality-full` as developer entrypoints.

**Choices:**
- [x] Clean-slate `<domain>-<action>` naming with `quality`/`quality-full` entrypoints; no legacy names retained.
- [ ] Keep `test`/`lint`/`coverage` as compatibility wrappers alongside the new surface.

### F10 [Applied]: Plan lacks precise scope for `docker-build` and `docker-smoke` leaf targets
<!-- severity: major -->
<!-- dimension: gaps -->

**Context:** Phase 2 lists `docker-build` and `docker-smoke` as leaf targets. The CI docker job (`.github/workflows/ci.yml:156-184`) performs: Buildx setup, `docker compose build`, env copy, `docker compose up -d --wait`, health checks, failure-log dump, and teardown.

**Issue:** Without specifying which commands each leaf wraps, a coding agent cannot implement them unambiguously. `docker-smoke` could mean just the health curls, or the full up/check/down cycle.

**Recommendation:** Define `docker-build` as `docker compose build` and `docker-smoke` as the full cycle: env setup, compose up, health checks, and compose down. Failure-log printing stays as a CI-only conditional step. Document this in Phase 2 and Phase 3.

**Choices:**
- [x] `docker-build` = `docker compose build`; `docker-smoke` = env copy + compose up + health checks + compose down.
- [ ] `docker-smoke` only runs health-check curls against an already-running stack, requiring manual up/down.

### F11: `make playwright` scope vs CI Playwright job bootstrap
<!-- severity: minor -->
<!-- dimension: plan-hygiene -->

**Context:** Phase 3 step 3 says "Replace Playwright commands with `make ci-playwright` while keeping server setup/build steps in workflow" (`.github/workflows/ci.yml:115-154`). The CI job does: Rust toolchain install, cargo cache, `cargo build -p sim-cli`, Node setup, `npm ci`, Playwright browser install, then `npx playwright test`.

**Issue:** The plan doesn't specify whether `make playwright` (leaf) runs only `npx playwright test`, or also handles binary build and browser install. A coding agent needs this boundary to be unambiguous.

**Recommendation:** Define `make playwright` as running only `cd ui && npx playwright test`. All bootstrap (Rust build, Node setup, npm ci, browser install) stays in the CI workflow or is the developer's responsibility locally.

**Choices:**
- [x] `make playwright` = `cd ui && npx playwright test` only; bootstrap stays in workflow/developer hands.
- [ ] `make playwright` includes `cargo build -p sim-cli` to ensure the API binary is fresh.

### F12: Stale line references in Section 3 (Verified Current State)
<!-- severity: minor -->
<!-- dimension: plan-hygiene -->

**Context:** Section 3.2 originally cited `.github/workflows/ci.yml:101-155` for frontend workflow jobs. The actual frontend job spans lines 65-114.

**Issue:** A reviewer or coding agent cross-referencing the plan against CI would find the wrong lines.

**Recommendation:** Update all Section 3 line references to match actual file content.

**Choices:**
- [x] Correct line references in Section 3 to match actual CI file structure.
- [ ] Remove line references from Section 3 entirely.

### Summary

| # | Title | Severity | Dimension | Depends on |
|---|-------|----------|-----------|------------|
| F1 | Scope for `make quality` versus `make quality-full` | minor | best-practices | — |
| F2 | Security scan ownership split between workflow and Make | minor | plan-hygiene | — |
| F3 | Invalid Make target name in leaf target list | minor | correctness | — |
| F4 | Require coverage in `make ci-rust` and enforce blocking failures | minor | plan-hygiene | — |
| F5 | `ci-security` scope for Rust dependency audit mismatch | minor | correctness | — |
| F6 | Add automated contract check for Makefile target updates | minor | testing | — |
| F7 | Tarpaulin output-path divergence between local and CI | major | correctness | — |
| F8 | Frontend CI job `defaults: working-directory` must be removed | major | correctness | — |
| F9 | Legacy `<action>-<domain>` names removed from Make surface | major | best-practices | — |
| F10 | Plan lacks precise scope for `docker-build` and `docker-smoke` leaf targets | major | gaps | — |
| F11 | `make playwright` scope vs CI Playwright job bootstrap | minor | plan-hygiene | — |
| F12 | Stale line references in Section 3 (Verified Current State) | minor | plan-hygiene | — |

---

## Implementation Status

> **Date:** 2026-04-08

### Completed tasks

| Phase | Status | Notes |
|-------|--------|-------|
| Phase 1 — Establish Make discoverability and command contract | [Done] | All acceptance criteria verified: `make` prints help, `make list` = `make help`, no legacy targets. |
| Phase 2 — Consolidate gate primitives and composites | [Done] | 17 leaf targets, 5 composites, `quality`/`quality-full` entrypoints, full `.PHONY` coverage, no legacy names. |
| Phase 3 — Refactor CI to consume Make targets | [Done] | Removed `defaults: working-directory: ui`, replaced `rustsec/audit-check@v2` with `make rust-audit`, removed `continue-on-error` from coverage, added `make-contract` CI job. |
| Phase 4 — Update docs and contributor surfaces | [Done] | All 6 docs updated: TESTING.md (quality gates matrix), CONTRIBUTING.md, README.md, ui/README.md, docs/testing-strategy.md, SECURITY.md. |
| Phase 5 — Stabilize rollout and governance | [Done] | Governance note added to plan. |

### Deviations from plan

- **Phase 3, docker-scan job (2026-04-08):** The CI docker-scan job now calls `make trivy-scan-${{ matrix.image }}` directly instead of the `aquasecurity/trivy-action` GitHub Action. The Make target uses `trivy` CLI directly, requiring `trivy` to be installed on the runner. This is intentional to keep scan invocation in Make per F2.
- **rust-coverage output path (2026-04-08):** Resolved F7 by pointing CI's Codecov upload at `target/coverage/cobertura.xml` instead of copying the file to repo root. The `rust-coverage` target writes all outputs to `target/coverage/` with no post-processing step.

### Build/runtime fixes applied

- **crates/sim-cli/src/main.rs:** Added missing closing brace for `mod tests` block (pre-existing unclosed delimiter).
- **crates/sim-core/tests/scenario_loading.rs:** Removed 3 duplicate test functions (`scenario_with_nan_price_rejected`, `scenario_with_inf_demand_rejected`, `scenario_with_extreme_price_rejected`) that were superseded by refactored versions using the `scenario_with_economy` helper.

### Validations executed

1. **Validation 1 (discovery):** `make` and `make list` both print categorized help screen — PASS.
2. **Validation 2 (local target matrix):** `make quality` executed end-to-end — PASS. All 9 leaf targets green: fmt, clippy, rust-test (234 tests), rust-coverage (91.81%), frontend-lint, frontend-typecheck, frontend-test (51 tests), frontend-coverage, frontend-build.
3. **Validation 5 (governance):** Governance note present in plan — PASS.
4. **Validation 6 (CI/CD guard):** Dry-run verified locally via `make help && make list && make -n ci-rust ci-frontend ci-playwright ci-docker ci-security rust-audit` — PASS.
5. **Validation 4 (docs parity):** Full cross-reference audit completed. Fixed: stale `rustsec/audit-check` ref in testing-strategy.md, overstated `make ci-security` CI claim in SECURITY.md, raw `cargo fmt`/`cargo clippy` refs in CONTRIBUTING.md Code Style section, missing repo-root note in ui/README.md — PASS.

### Validations not yet executed

1. **Validation 3 (CI parity):** Requires an actual CI run to confirm Make targets are invoked and produce expected outputs.
