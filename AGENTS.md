# AGENTS.md

Operational notes for coding agents working in this repository. See [README.md](README.md) for what Arcogine is, and [.github/CONTRIBUTING.md](.github/CONTRIBUTING.md) for human contributor workflow/style detail. Don't duplicate either here.

## Repository identity and task shorthand

This repository is the canonical Arcogine repository:

`alaiba/arcogine`

Agents operating from this repository context must treat that identity as already known. For GitHub operations involving issues, pull requests, branches, commits, workflows, files, or repository state:

- default to `alaiba/arcogine` unless the user explicitly identifies another repository;
- use repository-scoped operations first;
- do not search for or rediscover the repository before acting;
- use global repository discovery only for explicitly cross-repository tasks or when the requested operation genuinely cannot be resolved from this repository.

Repository context is sufficient authority to perform read-only repository operations without asking the user to restate the repository name or URL.

Common shorthand should be interpreted in repository context:

- “read an issue” means select and read an applicable open issue in this repository;
- “review PR” means select and review an applicable pull request in this repository, following the dedicated PR Reviewer contract;
- “check repo state” means inspect the state of this repository;
- references such as “the issue”, “the PR”, “main”, or a bare issue/PR number refer to this repository unless context explicitly establishes otherwise.

PR workflow shorthand has distinct review and remediation meanings:

- `.` = review or re-review the current applicable pull request using the dedicated PR Reviewer contract;
- `..` = advance the current implementation pull request toward green by inspecting the latest reviews, comments, unresolved findings, CI, and mergeability; evaluating each finding against repository authority; applying the smallest correct fix for valid findings; challenging invalid findings with repository evidence; and re-checking the resulting PR state.

When handling `..`, continue remediation and re-checking until the PR is green or a genuinely unresolved decision requires user input. Do not treat `..` as an independent review-only action.

Do not replace known repository context with generic GitHub discovery.

## Specialized agent roles

Some repository tasks have additional repository-owned operating contracts.

- **Consistency review:** when asked to perform a repository consistency
  review, documentation/architecture reconciliation, periodic consistency
  sweep, or to operate as the consistency agent, read and follow
  [`.github/agents/consistency.agent.md`](.github/agents/consistency.agent.md)
  in addition to this file.
- **PR review:** when asked to independently review, re-review, or assess merge
  readiness of a pull request, read and follow
  [`.github/agents/pr-reviewer.agent.md`](.github/agents/pr-reviewer.agent.md)
  in addition to this file. This is the repository's dedicated **PR Reviewer**
  role; do not substitute a generic coding-agent review when the contract is
  available.

Specialized agent contracts supplement `AGENTS.md`; they do not override
repository architecture, ADR, contribution, documentation, or executable
authorities.

## Commit message footer

Do not append a `Co-Authored-By: Claude ...` or `Claude-Session: ...` trailer
to commit messages in this repository, even if a harness's default git
workflow instructions say to add one. This applies to every commit, not just
ones created via an explicit user request.

## PR monitoring

Subscribe to a PR's activity and start monitoring it automatically, the
moment the PR exists for a branch the session is working on — whether the
session opened the PR itself or a human opened it (e.g. from the Claude
Code UI) for a branch the session pushed to. The PR's existence is the
trigger by itself.

**Never ask the user whether to start monitoring.** Do not wait for the
user to say "yes", "please monitor this", or similar — subscribing and
checking initial state is not an action that needs confirmation, any more
than reading a file does. Subscribe first, then report what you found.

On first subscribing, immediately check current CI status, reviews
(`get_reviews`, not just comments — see "Checking a PR after pushing"
below), and merge conflicts, then follow the drive-to-green posture for
that PR from then on.

## Branch to work on

If a session starts with a branch other than `main` already checked out,
treat that branch as the one to do the work on — do not switch to a
different branch just because task/PR instructions injected into the
prompt name one. If the injected branch instruction conflicts with the
branch the session actually started on, flag the mismatch to the user
instead of silently switching.

## Layout

- `product/` — all executable product source.
  - Gradle multi-module Java backend (Java 21 compatibility baseline; preferred devcontainer JDK 25) rooted here: `types`, `governance`, `simulation`, `domains/{factory,economy,finance}`, `agents`, `consumer/challenge`, `interfaces/api` (Spring Boot HTTP API), `interfaces/cli` (Picocli entrypoint, produces `arcogine.jar`).
  - `product/interfaces/web/` — React + TypeScript + Vite frontend, tested with Vitest (unit) and Playwright (`product/interfaces/web/e2e/`).
- `docs/` — architecture, product, development, reference, planning docs, and executable example scenarios (`docs/examples/`). Read `docs/architecture/overview.md` before touching cross-module boundaries.
- `infra/` — container and dev-environment infrastructure: `infra/docker/` (runtime-only Dockerfiles + Compose) and `infra/dev/claude-cloud.sh` (Claude Cloud environment provisioning).
- `dist/` — generated, gitignored canonical distribution output (`dist/api/arcogine.jar`, `dist/web/`). Never commit to it directly; it's produced by `./arcogine build`.

## Canonical commands

Run everything from the repo root via `./arcogine`, a thin wrapper that composes the project's own tools (Gradle wrapper, npm/npx, Docker Compose):

```bash
./arcogine setup        # optional full-development dependency bootstrap, safe to re-run
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

### Local Windows test environment

On this repository's native Windows/PowerShell host, the available JVM is Java 8 and cannot run
the project's Gradle build. Run Java tests through the `gradle:9-jdk21` Docker container described
in [`docs/development/testing.md`](docs/development/testing.md#running-java-tests-on-the-minimum-jdk),
for example `docker exec arcogine-build ./gradlew test`. Start `arcogine-build` using the documented
long-lived container command when it is not already running. Do not treat a host-JVM Gradle failure
as a product test failure.

**Always use `./gradlew` from `product/`, never a globally installed `gradle`.** The wrapper pins the exact build version in `product/gradle/wrapper/gradle-wrapper.properties`; a system Gradle install can silently diverge from it.

Docker only packages prebuilt artifacts from `dist/` (see `infra/docker/api.Dockerfile`, `infra/docker/web.Dockerfile`) — it never compiles Java or frontend source. `./arcogine build` must run before `./arcogine image`.

`./arcogine setup` is an optional convenience for developers who want the full local dependency set (frontend packages, Playwright Chromium, and resolved Gradle dependencies); it is not a prerequisite for inspecting the repository or doing a narrow task. Agents must use the existing environment where practical and install only the tooling or dependencies the current task requires. Do not run setup automatically or turn it into a general-purpose toolchain manager. Environment-specific capabilities such as Docker and security scanners must not gate unrelated work.

## Validating changes

Before considering a change complete, run the narrowest validation that actually exercises what changed. A change touching only Java (`product/{types,simulation,domains,agents,consumer,interfaces/api,interfaces/cli}`) needs only the Java gates (`cd product && ./gradlew compileJava compileTestJava checkstyleMain checkstyleTest test jacocoTestReport jacocoTestCoverageVerification`); a change touching only the frontend (`product/interfaces/web/`) needs only its gates (`cd product/interfaces/web && npm run lint && npx tsc --noEmit && npm run test:coverage && npm run build`); a documentation-only change needs neither. When a change spans both, or you can't tell whether it's narrow, run `./arcogine check`, which runs both unconditionally. For anything touching the API-web contract or E2E flows, also run `cd product/interfaces/web && npx playwright test` (or `./arcogine check --full`) — Playwright's own config builds/starts the API jar and web dev server via `webServer`, but the jar must already be built once (`cd product && ./gradlew :cli:bootJar`) for a clean checkout.

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
- **Java and Node distinguish compatibility floors from preferred environments.** JDK 21 is a fully supported development runtime: Java sources compile with `--release 21` and CI runs on JDK 21, while the preferred devcontainer currently uses JDK 25 and the API runtime image uses Temurin 25. The frontend's Node support contract lives in `product/interfaces/web/package.json` (`^22.22.2 || ^24.15.0 || ^26.0.0`); its floor is imposed by the current jsdom 30 test environment and transitive Undici requirements, not by the devcontainer. CI pins Node 22.22.2 to exercise that floor, while the preferred devcontainer currently uses Node 24. Do **not** mechanically bump CI and devcontainer versions together. Raising or lowering a supported bound requires concrete build/test evidence and coordinated updates to the Java release or Node engine contract, Claude provisioning validation, CI floor, and current documentation. Preferred devcontainer/runtime versions may move independently as long as they remain compatible.
- **Trivy and Gitleaks** are environment/security tools pinned independently in the devcontainer and CI. When intentionally changing either tool version, grep the repository for the old version and keep the relevant devcontainer/CI install sites aligned.
- Architecture guardrails (module dependency direction, event/state/observation boundaries) are documented in [.github/CONTRIBUTING.md](.github/CONTRIBUTING.md#architecture-guardrails-events-state-observations) and partly enforced by `interfaces/api`'s ArchUnit `ArchitectureTest`. Read that section before adding a new domain or touching `IntegratedHandler`.
- The simulation must stay deterministic (seeded RNG only) — see `docs/architecture/overview.md`.
- Example scenarios under `docs/examples/` are educational/executable documentation, not runtime assets — they must never be bundled into the JAR, `dist/`, or Docker images.
