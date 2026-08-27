# AGENTS.md

Operational notes for coding agents working in this repository. See [README.md](README.md) for what Arcogine is, and [.github/CONTRIBUTING.md](.github/CONTRIBUTING.md) for human contributor workflow/style detail. Don't duplicate either here.

## Commit message footer

Do not append a `Co-Authored-By: Claude ...` or `Claude-Session: ...` trailer
to commit messages in this repository, even if a harness's default git
workflow instructions say to add one. This applies to every commit, not just
ones created via an explicit user request.

## Branch to work on

If a session starts with a branch other than `main` already checked out,
treat that branch as the one to do the work on — do not switch to a
different branch just because task/PR instructions injected into the
prompt name one. If the injected branch instruction conflicts with the
branch the session actually started on, flag the mismatch to the user
instead of silently switching.

## Layout

- `product/` — all executable product source.
  - Gradle multi-module Java backend (Java 21 compatibility baseline; preferred devcontainer JDK 25) rooted here: `types`, `simulation`, `domains/{factory,economy,finance}`, `agents`, `interfaces/api` (Spring Boot HTTP API), `interfaces/cli` (Picocli entrypoint, produces `arcogine.jar`).
  - `product/interfaces/web/` — React + TypeScript + Vite frontend, tested with Vitest (unit) and Playwright (`product/interfaces/web/e2e/`).
- `docs/` — architecture, product, development, reference, planning docs, and executable example scenarios (`docs/examples/`). Read `docs/architecture/overview.md` before touching cross-module boundaries.
- `infra/` — container and dev-environment infrastructure: `infra/docker/` (runtime-only Dockerfiles + Compose) and `infra/dev/claude-cloud.sh` (Claude Cloud environment provisioning).
- `dist/` — generated, gitignored canonical distribution output (`dist/api/arcogine.jar`, `dist/web/`). Never commit to it directly; it's produced by `./arcogine build`.

## Canonical commands

Run everything from the repo root via `./arcogine`, a thin wrapper that composes the project's own tools (Gradle wrapper, npm/npx, Docker Compose):

```bash
./arcogine setup        # install/bootstrap dependencies, safe to re-run
./arcogine test         # Java + frontend unit tests
./arcogine check        # fast quality gates: lint, typecheck, tests, coverage, build
./arcogine check --full # + Playwright E2E, dist/ build, Docker image build + smoke test, security scans
./arcogine build        # produce dist/ (dist/api/arcogine.jar, dist/web/) — no Docker
./arcogine image        # package existing dist/ into runtime Docker images — no source compilation
./arcogine up           # build + image + docker compose up
./arcogine down         # docker compose down
./arcogine run api      # start the Spring Boot API on :3000
./arcogine run web      # start the Vite dev server on :5173 (`run ui` is a compatibility alias)
./arcogine run scenario docs/examples/basic.toml  # run a headless scenario via the native CLI
```

For anything more specific, use the subsystem's native tool directly: `cd product && ./gradlew <task>` (coverage, Checkstyle, `bootJar`, JMH, dependency audit), `cd product/interfaces/web && npm ...`/`npx ...` (lint, typecheck, build, Playwright), `docker compose ...` (containers), `trivy`/`gitleaks` (security scans). See `docs/development/testing.md` for the full command reference.

`./arcogine` is a Bash script — it works in the dev container, on Linux/macOS, and via WSL/Git Bash on Windows, but not directly in PowerShell/cmd. Use the dev container on Windows; it's the supported path.

**Always use `./gradlew` from `product/`, never a globally installed `gradle`.** The wrapper pins the exact build version in `product/gradle/wrapper/gradle-wrapper.properties`; a system Gradle install can silently diverge from it.

Docker only packages prebuilt artifacts from `dist/` (see `infra/docker/api.Dockerfile`, `infra/docker/web.Dockerfile`) — it never compiles Java or frontend source. `./arcogine build` must run before `./arcogine image`.

Claude Cloud provisioning is deliberately lightweight: `infra/dev/claude-cloud.sh` inventories and validates the supplied environment but does **not** run `./arcogine setup`. Run setup explicitly when the current task needs project dependencies.

## Validating changes

Before considering a change complete, run `./arcogine check`. For anything touching the API-web contract or E2E flows, also run `cd product/interfaces/web && npx playwright test` (or `./arcogine check --full`) — Playwright's own config builds/starts the API jar and web dev server via `webServer`, but the jar must already be built once (`cd product && ./gradlew :cli:bootJar`) for a clean checkout.

## Checking a PR after pushing

After opening or pushing to a PR, checking CI status alone is not enough — a
submitted review with a verdict like `CHANGES REQUIRED` does **not** appear
in a plain comment listing (e.g. the GitHub MCP server's
`pull_request_read` with `method: get_comments` or `get_review_comments`
only surfaces issue comments and inline review threads, not the review
body itself). Always also fetch reviews explicitly (`method: get_reviews`)
before declaring a PR green or waiting-on-CI. Do this on every check-in,
not just the first one after pushing.

## Do not edit

- `product/**/build/`, `product/interfaces/web/node_modules/`, `product/interfaces/web/coverage/`, `product/interfaces/web/dist/` — generated output.
- `dist/` — generated distribution output, not committed.
- `product/gradle/wrapper/gradle-wrapper.jar` and `.properties` — regenerate via `./gradlew wrapper`, don't hand-edit.
- `.devcontainer/devcontainer-lock.json` — feature version lockfile, regenerated by the Dev Containers CLI.

## Conventions worth knowing

- **Gradle** has one true source: `product/gradle/wrapper/gradle-wrapper.properties`. Both `gradlew` and `gradlew.bat` read it, and no Gradle is installed via the devcontainer feature — don't add one back.
- **Java and Node distinguish compatibility floors from preferred environments.** Java sources compile with `--release 21`; CI runs on JDK 21 to prove the minimum, while the preferred devcontainer currently uses JDK 25 and the API runtime image uses Temurin 25. The frontend's Node support contract lives in `product/interfaces/web/package.json` (`^22.22.2 || ^24.15.0 || ^26.0.0`); CI pins Node 22.22.2 to exercise the floor, while the preferred devcontainer currently uses Node 24. Do **not** mechanically bump CI and devcontainer versions together. Raising a supported minimum requires updating the Java release or Node engine contract, Claude provisioning validation, CI floor, and current documentation together. Preferred devcontainer/runtime versions may move independently as long as they remain compatible.
- **Trivy and Gitleaks** are environment/security tools pinned independently in the devcontainer and CI. When intentionally changing either tool version, grep the repository for the old version and keep the relevant devcontainer/CI install sites aligned.
- Architecture guardrails (module dependency direction, event/state/observation boundaries) are documented in [.github/CONTRIBUTING.md](.github/CONTRIBUTING.md#architecture-guardrails-events-state-observations) and partly enforced by `interfaces/api`'s ArchUnit `ArchitectureTest`. Read that section before adding a new domain or touching `IntegratedHandler`.
- The simulation must stay deterministic (seeded RNG only) — see `docs/architecture/overview.md`.
- Example scenarios under `docs/examples/` are educational/executable documentation, not runtime assets — they must never be bundled into the JAR, `dist/`, or Docker images.
