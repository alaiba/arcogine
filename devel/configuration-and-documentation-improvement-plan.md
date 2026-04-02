# Configuration and Documentation Improvement Plan

> **Date:** 2026-04-02
> **Scope:** Address the configuration, structure, and documentation issues identified in the Arcogine repository review, including dev container usability, local/container runtime consistency, and a clearer "play it as a game" onboarding path.
> **Primary sources:** `README.md`, `CONTRIBUTING.md`, `TESTING.md`, `.devcontainer/`, `compose.yaml`, `Dockerfile`, `ui/Dockerfile`, `ui/README.md`, `examples/README.md`, `docs/README.md`, `docs/architecture-overview.md`, `docs/vision.md`, `ui/src/`

---

## 1. Goals

1. Make the documented startup paths match the real runtime behavior.
2. Make the dev container a first-class, documented development path.
3. Remove stale or template documentation that misrepresents the current project.
4. Align architecture and feature docs with the code that actually exists.
5. Add a lightweight onboarding path for using Arcogine as an experiment game, not only as a serious simulation platform.

---

## 2. Current Issues

### 2.1 Runtime and Container Configuration

1. `compose.yaml` healthcheck relies on `curl`, and the final API image currently installs it; keep this contract explicit instead of replacing it by default.
2. `ui/Dockerfile` and `compose.yaml` define `VITE_API_URL`, but the frontend client uses a hardcoded relative `/api` base path.
3. Native startup docs assume Rust is installed locally, but the repository also includes a complete dev container path that is undocumented.
4. Rust versioning is described as pinned, but `rust-toolchain.toml` uses floating `stable` while Docker and dev container images use different concrete bases.

### 2.2 Documentation Drift

1. `ui/README.md` is still the stock Vite template.
2. `examples/README.md` still describes the shipped scenarios as "planned".
3. `README.md` and `CONTRIBUTING.md` still use placeholder clone URLs.
4. `docs/README.md` is incomplete and does not list all active docs.
5. `docs/architecture-overview.md` mixes current implementation with planned features.
6. `docs/vision.md` leans heavily toward "not a game" language, while the UI already includes game-like onboarding elements.

### 2.3 UI and Feature Communication Gaps

1. The UI contains a welcome overlay and scenario cards, but the docs do not explain how to use them.
2. The docs do not explain scenario goals, challenge framing, or how to "play" the simulation.
3. The architecture doc overstates several implemented UI capabilities.
4. Some UI components exist but are not wired into the active layout, creating documentation ambiguity about what is real.

### 2.4 Validation and CI Gaps

1. CI does not verify Docker or compose flows.
2. CI does not run Playwright, despite docs implying stronger integration.
3. No automated check exists for the documented containerized getting-started path.

---

## 3. Workstreams

### Workstream A: Fix Runtime and Container Mismatches

Objective: Ensure the documented native, container, and dev container flows all work as described.

#### A1. Fix the compose healthcheck path

Files:
- `compose.yaml`
- `Dockerfile`

Actions:
- Replace the API healthcheck with an approach that works in the final runtime image.
- Prefer one of:
  - install `curl` in the final image, or
  - switch to a healthcheck command that uses a tool already present.
- Reconfirm that `depends_on: condition: service_healthy` works as intended for the UI service.

Acceptance criteria:
- `docker compose up --build` starts both `api` and `ui` successfully.
- The `api` service reaches healthy state.
- The `ui` service starts without waiting indefinitely on a broken healthcheck.

#### A2. Resolve API base URL configuration drift

Files:
- `compose.yaml`
- `ui/Dockerfile`
- `ui/src/api/client.ts`
- `ui/vite.config.ts`

Actions:
- Choose one clear strategy:
  - same-origin `/api` everywhere, or
  - env-driven API base URL.
- Remove unused configuration if same-origin remains the intended deployment model.
- Update documentation to describe the chosen model clearly.

Acceptance criteria:
- There is exactly one documented API routing model for dev and container runs.
- No build args or env vars remain unused.
- Frontend networking behavior is easy to explain from the code and docs.

#### A3. Normalize the toolchain story

Files:
- `rust-toolchain.toml`
- `Dockerfile`
- `.devcontainer/Dockerfile`
- `README.md`
- `CONTRIBUTING.md`
- `TESTING.md`

Actions:
- Decide whether Arcogine wants:
  - floating stable everywhere, or
  - a concrete pinned Rust version everywhere practical.
- Make the wording in docs match the actual implementation.
- If version pinning is desired, align the dev container and release image story.

Acceptance criteria:
- The repository no longer claims stronger pinning than it actually provides.
- A contributor can tell which Rust version policy the project follows.

---

### Workstream B: Make the Dev Container a First-Class Path

Objective: Document the existing dev container as the easiest supported way to start contributing.

#### B1. Add a dev container quick start to the root docs

Files:
- `README.md`
- `CONTRIBUTING.md`

Actions:
- Add a "Dev Container" quick-start path ahead of or alongside native development.
- Explain what the post-create script already does:
  - builds the Rust workspace
  - installs UI dependencies
  - creates `.env` when missing
- Document the expected commands after container startup.

Acceptance criteria:
- A user with VS Code + Dev Containers can get started without reading source files.
- The docs explicitly distinguish:
  - dev container
  - native development
  - docker compose runtime

#### B2. Clarify host prerequisites versus in-container prerequisites

Files:
- `README.md`
- `CONTRIBUTING.md`
- `TESTING.md`

Actions:
- Separate "host machine requirements" from "inside dev container requirements".
- Note that native development requires Rust and Node installed on the host.
- Avoid implying native commands will work everywhere by default.

Acceptance criteria:
- The docs no longer assume `cargo` exists on every host environment.
- Users can choose the right path without trial and error.

---

### Workstream C: Rewrite Stale Documentation

Objective: Replace generic or outdated docs with project-specific guidance.

#### C1. Rewrite `ui/README.md`

Files:
- `ui/README.md`

Actions:
- Replace the stock Vite template with Arcogine-specific content:
  - what the UI does
  - how it talks to the API
  - local dev commands
  - build/test commands
  - where the main source directories are

Acceptance criteria:
- `ui/README.md` describes Arcogine, not Vite boilerplate.

#### C2. Rewrite `examples/README.md`

Files:
- `examples/README.md`

Actions:
- Change scenario descriptions from "planned" to current shipped scenarios.
- Document each scenario’s purpose:
  - `basic`
  - `overload`
  - `capacity_expansion`
- Add a short "what to look for" section for each.

Acceptance criteria:
- The examples doc accurately reflects the files that exist today.
- A new user can pick a scenario intentionally.

#### C3. Repair root and contributing docs

Files:
- `README.md`
- `CONTRIBUTING.md`
- `TESTING.md`

Actions:
- Replace placeholder repository URLs.
- Standardize commands on `npm ci` vs `npm install` where appropriate.
- Clarify which commands are guaranteed, optional, or environment-specific.
- Ensure the testing doc only claims automation that actually exists.

Acceptance criteria:
- No placeholder clone URLs remain.
- Command examples are internally consistent.
- Testing guidance aligns with CI and actual tooling.

#### C4. Update the docs index

Files:
- `docs/README.md`

Actions:
- Add any active docs missing from the index, including deployment analysis.
- Distinguish between:
  - architecture/vision docs
  - operational docs
  - roadmap/planning docs in `devel/`

Acceptance criteria:
- `docs/README.md` can be used as a complete navigation point for `/docs`.

---

### Workstream D: Bring Architecture Docs Back in Sync with the Code

Objective: Ensure `docs/architecture-overview.md` describes current implementation truthfully.

#### D1. Separate implemented behavior from roadmap behavior

Files:
- `docs/architecture-overview.md`

Actions:
- Review and correct claims that are currently overstated, especially around:
  - OpenAPI generation availability
  - typed client generation
  - Playwright integration into CI
  - UI control surface details
  - shadcn/ui usage
  - PNG export capability
  - repository structure entries for crates not yet present
- Use explicit labels such as:
  - "Current"
  - "Planned"
  - "Phase 7"
when needed.

Acceptance criteria:
- The architecture doc can be read as a trustworthy description of the current codebase.
- Planned features are clearly marked as planned.

#### D2. Align UI architecture claims with the actual mounted UI

Files:
- `docs/architecture-overview.md`
- `ui/src/App.tsx`
- `ui/src/components/layout/Sidebar.tsx`
- `ui/src/components/experiment/`

Actions:
- Review documented UI regions and controls against the current mounted component tree.
- Remove or qualify claims about capabilities not currently wired into the app.
- Either:
  - wire the intended components into the active UI, or
  - document them as in-progress and keep them out of current-state descriptions.

Acceptance criteria:
- The docs no longer describe unmounted or partially implemented UI as fully shipped.

---

### Workstream E: Add a "Play Arcogine" Onboarding Path

Objective: Make it easy to use Arcogine as a lightweight simulation game without diluting the serious simulation identity.

#### E1. Add a "Play Arcogine" section to the root README

Files:
- `README.md`
- `docs/vision.md`

Actions:
- Add a short section explaining the project can be used in two complementary ways:
  - serious experiment console
  - lightweight factory/economy challenge sandbox
- Keep the project identity rigorous, but stop forcing a false binary between "serious" and "fun".

Acceptance criteria:
- New users immediately understand that Arcogine can be explored casually.
- The copy does not undermine the simulation-first architecture.

#### E2. Turn the built-in scenarios into explicit challenge modes

Files:
- `README.md`
- `examples/README.md`
- optionally `ui/src/components/onboarding/WelcomeOverlay.tsx` if copy adjustments are desired later

Actions:
- Document each scenario as a challenge:
  - `Basic`: learn the controls
  - `Overload`: rescue a stressed line
  - `Capacity Expansion`: compare structural improvements against reactive fixes
- Add suggested player goals:
  - maximize revenue
  - reduce backlog
  - improve throughput
  - beat a saved baseline

Acceptance criteria:
- Someone opening the repo can understand how to "play" without reading source code.
- Scenarios feel intentional rather than just technical fixtures.

#### E3. Add a short controls-and-loop guide

Files:
- `README.md`
- `examples/README.md`

Actions:
- Explain the interaction loop in plain language:
  - load scenario
  - run or step
  - adjust price
  - take machines offline/online
  - toggle the agent
  - save a baseline
  - compare outcomes
- Include one short recommended first session.

Acceptance criteria:
- A first-time user can launch the UI and meaningfully interact with it in under five minutes.

---

### Workstream F: Tighten Validation Through CI and Smoke Checks

Objective: Reduce drift between docs and reality by validating the actual supported startup paths.

#### F1. Add Docker and compose validation to CI

Files:
- `.github/workflows/ci.yml`

Actions:
- Add at least one job that builds the API and UI container images.
- Prefer adding a compose smoke job once the healthcheck is fixed.

Acceptance criteria:
- Container breakage is caught before merge.

#### F2. Align CI claims with actual coverage

Files:
- `.github/workflows/ci.yml`
- `TESTING.md`
- `docs/architecture-overview.md`

Actions:
- Either add Playwright to CI or stop claiming it is already integrated.
- Keep the documentation conservative until the automation exists.

Acceptance criteria:
- Docs and CI describe the same quality gates.

---

## 4. Proposed Execution Order

1. Workstream A
2. Workstream B
3. Workstream C
4. Workstream D
5. Workstream E
6. Workstream F

Rationale:
- Runtime truth comes first.
- The dev container path should be documented before broader doc rewrites.
- Documentation should only be rewritten after the runtime/config story is stable.
- "Play Arcogine" guidance should be layered on top of accurate operational docs.

---

## 5. Deliverables

Expected outputs:

1. Updated startup and contributor docs that accurately reflect supported paths.
2. A working compose stack with a valid healthcheck.
3. A clarified API routing/configuration model for the UI.
4. Rewritten `ui/README.md` and `examples/README.md`.
5. A corrected `docs/architecture-overview.md` and expanded `docs/README.md`.
6. A lightweight "Play Arcogine" onboarding flow in the documentation.
7. CI checks that validate the most important documented runtime paths.

---

## 6. Definition of Done

This plan is complete when:

1. A contributor can choose between dev container, native dev, and compose using only the docs.
2. `docker compose up --build` works without manual patching.
3. No primary doc still contains template copy, placeholder URLs, or clearly stale feature claims.
4. The architecture doc distinguishes current behavior from planned behavior.
5. The repo includes an explicit, documented "play it like a game" path.
6. CI validates enough of the runtime story to keep the docs honest.

---

## 7. Notes

- This plan is intentionally focused on configuration and documentation alignment, not on feature expansion.
- Security and testability improvements are already covered separately in:
  - `devel/security-improvement-plan.md`
  - `devel/testability-improvement-plan.md`
- If implementation work starts, this plan should be updated with status markers per workstream so it remains useful as an execution document rather than a one-time review artifact.

## Findings

### F1: Add a deterministic frontend test gate
<!-- severity: minor -->
<!-- dimension: testing -->

**Context:** `ui/package.json` currently defines `dev`, `build`, `lint`, and `preview` scripts only (`ui/package.json:6-11`), and there are no `*.test.*` files under `ui/src` in the current tree.

**Issue:** The plan targets runtime and docs alignment, but existing frontend-facing behavior (`SSE` wiring, API wrappers, and onboarding controls) has no unit-level guard. Documentation work can drift without a fast JS contract test gate.

**Recommendation:** Add an explicit unit-test slice (for example, API client and simulation store helpers) and gate it in CI so documentation/code behavior changes are caught quickly.

**Choices:**
- [x] Add a `test`/`test:unit` script in `ui/package.json` (e.g., `vitest run`) and run it in the frontend CI job.
- [ ] Keep only `tsc`/`build` in frontend CI and document frontend test coverage as out of scope.
- [ ] Remove Playwright smoke tests from CI instead and rely on manual browser checks.

### F2: CI Playwright execution needs explicit bootstrap
<!-- severity: minor -->
<!-- dimension: testing -->

**Context:** Playwright is present in `ui/package.json` dependencies (`ui/package.json:20-23`) and tests exist in `ui/e2e/smoke.spec.ts`, but `.github/workflows/ci.yml` does not install browsers or run any Playwright command.

**Issue:** Workstream F2 targets CI parity, but without explicit browser setup and execution, the documented path will still be unverifiable in automated CI.

**Recommendation:** Add a CI step that installs Playwright browsers and runs `npx playwright test` using the existing `ui/playwright.config.ts`.

**Choices:**
- [x] Add Playwright bootstrap and `npx playwright test` in CI.
- [ ] Skip Playwright in CI and only document it as a manual QA lane.
- [ ] Remove Playwright tests and replace with additional build-only checks.

### F3: Healthcheck mismatch claim is stale
<!-- severity: minor -->
<!-- dimension: correctness -->

**Context:** `compose.yaml` uses `curl` for `/api/health` (`compose.yaml:9`), and the final runtime image installs `curl` (`Dockerfile:11`), while the API exposes `/api/health` (`crates/sim-api/src/server.rs:28`).

**Issue:** `Current Issues` still presents a missing-dependency mismatch that no longer exists, which can misdirect implementation effort and acceptance criteria.

**Recommendation:** Reframe Workstream A1 to validate the existing healthcheck contract and pin behavior, rather than treating it as a mismatch.

**Choices:**
- [x] Keep the existing curl-based healthcheck and add an explicit validation check in the stack verification plan.
- [ ] Replace the check with a non-curl probe without updating the image.
- [ ] Remove compose healthchecks from the documented startup paths.

### F4: Architecture doc overstates generated tooling and CI integration
<!-- severity: minor -->
<!-- dimension: correctness -->

**Context:** `docs/architecture-overview.md:187-198` claims OpenAPI-generated typed clients and Playwright-in-CI, but `crates/sim-api/Cargo.toml:22` only lists dependencies and `crates/sim-api/src` has no `utoipa` generation usage.

**Issue:** These statements exceed shipped implementation and can mislead contributors into expecting unavailable tooling and guarantees.

**Recommendation:** In D1, rewrite these architecture sections to clearly separate shipped behavior from planned/in-progress work.

**Choices:**
- [x] Update the architecture document to reflect current shipped API tooling and CI reality.
- [ ] Add OpenAPI generation + Playwright-in-CI in this same pass before documenting them as shipped.
- [ ] Leave claims unchanged and rely on future implementation to catch up.

### F5: Planned material crate appears as active in architecture structure
<!-- severity: minor -->
<!-- dimension: correctness -->

**Context:** `docs/architecture-overview.md:88-97` lists `sim-material` in the structure, while workspace members are only six crates in `Cargo.toml:4-10` and there is no `crates/sim-material`.

**Issue:** Users can infer a crate exists and attempt to inspect or use it even though it is not part of the current workspace.

**Recommendation:** Distinguish active crates from planned crates in the structure section.

**Choices:**
- [x] Mark `sim-material` as phase-7 planned and keep structure lists aligned to workspace members.
- [ ] Add placeholder scaffold for `sim-material` now to match documentation.
- [ ] Remove all phase-7 roadmap references from architecture documentation.

### F6: PNG export capability is documented ahead of implementation
<!-- severity: minor -->
<!-- dimension: correctness -->

**Context:** `docs/architecture-overview.md:196-198` describes PNG export support, while `ui/src/components/experiment/ExportMenu.tsx:52-54` shows PNG export as not yet available.

**Issue:** This mismatch weakens reliability claims and undermines the promised onboarding flow.

**Recommendation:** Document PNG export as planned-only until implemented, while keeping current CSV/JSON export paths truthful.

**Choices:**
- [x] Mark PNG export as planned-only until code is added.
- [ ] Implement PNG export in this plan before declaring it available.
- [ ] Remove export features from UI and docs until parity is complete.

### F7: Unmounted experiment components create capability ambiguity
<!-- severity: minor -->
<!-- dimension: gaps -->

**Context:** `ui/src/components/experiment/BaselineCompare.tsx` and `ExportMenu.tsx` exist, but `ui/src/App.tsx` currently renders only `Toolbar`, `WelcomeOverlay`, `KpiCards`, `TimeSeriesChart`, `MachineTable`, `JobTracker`, `Sidebar`, and `BottomDrawer`.

**Issue:** D2 focuses on mapping docs to mounted UI, but without deciding mount status these components create silent drift.

**Recommendation:** Add a D2 acceptance gate that explicitly requires either wiring these components into layout or marking them as unmounted with status.

**Choices:**
- [x] Add explicit "wired vs planned" status for every file in `ui/src/components/experiment/*` and align docs.
- [ ] Remove the components to eliminate ambiguity.
- [ ] Keep components in place as-is and continue documenting them as shipped.

### F8: Placeholder clone URLs weaken onboarding reliability
<!-- severity: minor -->
<!-- dimension: best-practices -->

**Context:** `README.md:21` and `CONTRIBUTING.md:14` still use `https://github.com/your-username/arcogine.git`.

**Issue:** This contradicts the plan’s B1 and C3 goals for frictionless first-run setup.

**Recommendation:** Replace placeholders with a canonical repository URL or a documented token convention used consistently across docs.

**Choices:**
- [x] Replace placeholders with the real repository URL across all onboarding docs.
- [ ] Keep placeholders and document that users must substitute their fork URL manually.
- [ ] Remove direct clone examples from onboarding docs.

### F9: Findings section must be added for traceability
<!-- severity: minor -->
<!-- dimension: plan-hygiene -->

**Context:** The plan currently has implementation tasks but no dedicated findings capture point for pass-by-pass verification.

**Issue:** Without a findings section, each pass can lose applied fixes and decision rationale, weakening implementation readiness and handoff quality.

**Recommendation:** Keep this findings section and summary table as a first-class part of the plan lifecycle.

**Choices:**
- [x] Maintain this structured findings section in the same plan document and update after each pass.
- [ ] Maintain findings in ad-hoc notes outside the plan.
- [ ] Drop findings capture and rely on PR descriptions only.

### Summary
| # | Title | Severity | Dimension | Depends on |
|---|-------|----------|-----------|-----------|
| F1 | Add a deterministic frontend test gate | minor | testing | - |
| F2 | CI Playwright execution needs explicit bootstrap | minor | testing | F1 |
| F3 | Healthcheck mismatch claim is stale | minor | correctness | - |
| F4 | Architecture doc overstates generated tooling and CI integration | minor | correctness | F3 |
| F5 | Planned material crate appears as active in architecture structure | minor | correctness | F4 |
| F6 | PNG export capability is documented ahead of implementation | minor | correctness | F4 |
| F7 | Unmounted experiment components create capability ambiguity | minor | gaps | F5 |
| F8 | Placeholder clone URLs weaken onboarding reliability | minor | best-practices | - |
| F9 | Findings section must be added for traceability | minor | plan-hygiene | - |

