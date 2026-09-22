# AGENTS.md

Operational notes for coding agents working in this repository. See [README.md](README.md) for what Arcogine is, and [.github/CONTRIBUTING.md](.github/CONTRIBUTING.md) for shared contributor workflow/style guidance. Don't duplicate either here.

## Repository identity and task shorthand

This repository is the canonical Arcogine repository:

`alaiba/arcogine`

Agents operating from this repository context must treat that identity as already known. For GitHub operations involving issues, pull requests, branches, commits, workflows, files, or repository state:

- default to `alaiba/arcogine` unless the user explicitly identifies another repository;
- use repository-scoped operations first;
- do not search for or rediscover the repository before acting;
- use global repository discovery only for explicitly cross-repository tasks or when the requested operation genuinely cannot be resolved from this repository.

Repository context is sufficient authority to perform read-only repository operations without asking the user to restate the repository name or URL.

Repository search results are discovery aids, not revision authority. Search indexes may lag a mutable target ref. When a search hit informs a current-state or target-revision claim, fetch the matched path at the exact target ref or commit before relying on its content. Do not treat a snippet from another indexed commit as evidence about the target revision.

Common shorthand should be interpreted in repository context:

- “read an issue” means select and read an applicable open issue in this repository;
- “review PR” means select and review an applicable pull request in this repository, following the dedicated PR Reviewer contract;
- “check repo state” means inspect the state of this repository;
- references such as “the issue”, “the PR”, “main”, or a bare issue/PR number refer to this repository unless context explicitly establishes otherwise.

Repository workflow shorthand has distinct meanings. Match these as exact tokens,
using the longest matching token when symbols overlap; do not decompose a token
into a shorter shorthand:

- `.?` = perform the Session-close Kaizen review before ending or deleting the current session;
- `./` = review or re-review the current applicable pull request using the dedicated PR Reviewer contract;
- `..` = re-resolve the current implementation pull request's lifecycle state and perform the next implementation-owned transition, if one is available;

### Session-close Kaizen

When the user's entire message is `.?`, inspect the current session and live repository for anything learned, decided, repeated, or encountered that should survive deletion of the conversation by changing executable safeguards, standard work, or maintained repository knowledge.

Classify each material candidate as one of:

- **Already encoded** — the repository already captures the lesson or invariant adequately; make no duplicate change.
- **Bake in** — the lesson is durable and generally reusable; identify the narrowest authoritative repository surface that should encode it.
- **Follow-up** — the improvement is worthwhile but belongs in separate work rather than being smuggled into the current PR or slice.
- **Discard** — the observation is situational, transient, or otherwise not worth preserving.

Prefer stronger forms of durable capture in this order when they fit the lesson: executable guard/test, canonical helper/tooling, agent/contributor standard work, maintained documentation, then canonical architecture or specification for genuinely architectural or hard-to-reverse constraints. Generalize incidents into semantic rules rather than preserving session or PR coordinates as durable concepts. Prefer improving an existing authoritative artifact over creating a new one.

Do not manufacture a lesson merely to produce an output. Finish every Session-close Kaizen review with an explicit deletion verdict: either the session is safe to delete because nothing unique remains, or name exactly what still needs to be captured first.

Do not replace known repository context with generic GitHub discovery.

## Specialized agent roles

Some repository tasks have additional repository-owned operating contracts.

- **Work planning:** when asked to re-ground initiative progress, decide what to work on next, prioritize open work, identify blocked versus ready slices, identify safe parallel lanes, or generate a handoff prompt for a recommended next slice, read and follow [`.github/agents/work-planner.agent.md`](.github/agents/work-planner.agent.md) in addition to this file.
- **Dependency maintenance:** when asked to process, apply, remediate, or sweep dependency updates or Dependabot pull requests, read and follow [`.github/agents/dependency-maintainer.agent.md`](.github/agents/dependency-maintainer.agent.md) in addition to this file.
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
- **Research:** when asked to investigate a bounded Arcogine research question,
  execute a research brief from `docs/research/`, produce a decision-quality
  research report, or perform an independent adversarial review of an existing
  research report, read and follow
  [`.github/agents/researcher.agent.md`](.github/agents/researcher.agent.md)
  in addition to this file. That contract defers to
  [`docs/development/researching.md`](docs/development/researching.md) for the
  normative research method. Research investigation and adversarial research
  review belong to this role, not to Work Planner or Consistency.

Specialized agent contracts supplement `AGENTS.md`; they do not override
repository architecture, contribution, documentation, or executable
authorities.

## Continuous improvement

Arcogine's continuous-improvement operating model — Session-close Kaizen, the
Consistency review, and the evidence-based delivery-process retrospective — is defined
in [`docs/development/continuous-improvement.md`](docs/development/continuous-improvement.md).

For `.?`, the standing responsibilities are only:

1. classify and durably capture anything from the current session that should survive
   deletion; and
2. evaluate the versioned delivery-retrospective trigger before giving the deletion
   verdict.

A Consistency review is **not** a standing `.?` subroutine. Recommend one from Kaizen
only when the session itself provides a concrete reason that a repository-wide
consistency sweep would be useful, such as a major cross-cutting architecture/status
transition or evidence of broader semantic drift.

Normal repository grounding does **not** evaluate the delivery-retrospective threshold.
Outside `.?`, evaluate it only when the current task explicitly concerns continuous
improvement, delivery-process health, or repository-wide planning/next-work.

At those boundaries, follow `docs/development/continuous-improvement.md` and derive the
retrospective threshold from its versioned factual authority. If action is warranted,
tell the user plainly what is recommended and include the minimal prompt for a fresh
session.

If no action is warranted, say nothing. If the versioned retrospective state cannot be
verified, say so once without assuming everything is current. Never repeat the same
reminder more than once per session, and never derail the user's requested task merely
because an improvement obligation is due.

## Temporary delivery coordinates and durable documentation

Arcogine planning coordinates use the reserved `PLAN-<TRACK>-<LOCAL-ID>` namespace (for example, a
hierarchical item might read `PLAN-<TRACK>-4-B`). `<TRACK>` is a stable, repository-wide track code
(currently `FD` Factory Design, `ENG` Factory Simulation Engine, `GOV` Governance/Conformance,
`CHAL` Factory-Design Challenge, `OPS` Operational Execution/Digital Twin); `<LOCAL-ID>` is one or
more hyphen-separated segments, extended for hierarchical items rather than inventing another
namespace. Compact, ad-hoc, or track-local coordinate syntax (a bare letter+number, a dotted or
space-separated variant, etc.) must not be introduced — the whole point of one reserved namespace
is that a temporary coordinate is always unmistakable on sight. PR-local review/finding
identifiers use the separate `REV-<NNN>` namespace.

Both namespaces are temporary delivery coordinates. They may be used in `docs/planning/`, issues,
pull requests, PR descriptions/comments, reviews, branch names, commit messages, handoff prompts,
and other active/delivery-history context where the coordinate helps sequence or track work — see
[`.github/CONTRIBUTING.md`](.github/CONTRIBUTING.md)'s commit message guidance, which this section
does not change.

Do not carry those identifiers into durable semantic naming — content whose meaning is expected to
outlive the delivery context that produced it. This includes architecture, product, reference, or
development documents; code comments; workflow definitions; and test/class/file names introduced
alongside the change. It does not include commit messages or other delivery-history records, which
may keep the coordinate that was actually used to track the work. When a planned result, a review
finding's resolution, or other delivery-context outcome is recorded as durable semantic naming,
translate it into the semantic capability, contract, identity, invariant, or behavior it actually
represents rather than naming it after the coordinate that tracked it. Working/process material
may mention a temporary delivery coordinate when the coordinate itself is the subject, but durable
semantic claims must remain understandable without reconstructing that coordinate after the
originating plan, PR, or review is completed, condensed, renamed, or removed.

Planning filenames are semantic, not coordinate-derived: the delivery label belongs in a planning
document's content, not its path, so the filename keeps describing the subject if sequencing
changes later.

The mechanical checker (`.github/scripts/check-delivery-labels.py`) enforces this deterministically
by scanning every tracked repository file (`git ls-files`, so generated/untracked/build output is
never in scope): a `PLAN-*` or `REV-<NNN>` token outside `docs/planning/` is a durable-naming leak;
inside `docs/planning/`, the old ambiguous label forms it replaced (a bare `Gate` plus number, a
bare letter-plus-number optionally dotted/hyphenated, `W1`, `DH-` plus a letter) may not be
reintroduced. Those old forms are not banned outside `docs/planning/` — they can be ordinary,
unrelated identifiers elsewhere in the codebase — which is exactly why the reserved `PLAN-`/`REV-`
namespaces exist: catching identifier leakage no syntax pattern can safely recognize (prose like
"the next stage" with no literal coordinate) remains a human review responsibility.

`docs/architecture/` holds Arcogine's current architecture. The
[Architecture Overview](docs/architecture/overview.md) owns cross-cutting principles and domain
boundaries; focused architecture and specification documents own exact contracts, identities,
semantics, algorithms and supported boundaries. Together they must be sufficient to answer what
Arcogine's architecture is now, without reading history. Arcogine keeps no separate
decision-record layer: a significant architectural change is reconciled into the architecture or
specification that owns the affected semantics, with code, tests, and dependent planning updated
in the same reviewed change. Git and pull-request history preserve what changed and why.

## Artifact lifetime and transient workspace

Classify temporary material by its intended lifetime:

- **Local ephemeral material:** use `logs/` for ad hoc diagnostics, local captures, and session scratch that should never be committed. The `logs/` directory is gitignored as a whole. Keep the root and working directory clean; use `logs/coverage.txt`, `logs/test-output.log`, etc. instead of root-level files.
- **Transient committed material:** use the reserved `workspace/` root for branch-local artifacts that must survive the current session or be handed to another actor, but are not intended to survive on `main`. This includes implementation and fresh-session prompts, research reports/revisions, adversarial reviews, checkpoints, diagnostic notes, and intentionally branch-transient review packets. A useful semantic structure is `workspace/implementation/`, `workspace/research/`, and `workspace/review/`.
- **Durable material:** keep maintained repository state in its existing canonical locations (`docs/`, product code/tests, scripts, workflows, and related maintained surfaces). A transient artifact can inform durable reconciliation without becoming durable itself.

`workspace/` is not an archive. It must not be gitignored, must not contain a permanent marker file such as `.gitkeep` or a README, and must be absent from `main` and every merge candidate as tracked content. The repository-owned check `.github/scripts/check-transient-workspace.py` enforces this final-tree invariant. Git therefore normally shows no `workspace/` directory at all on `main`.

When repository persistence is available, any agent producing a complete prompt for a fresh session, coding agent, reviewer, researcher, or other execution context must write it to a semantic path under `workspace/`, commit it with the repository owner's human Git identity, and hand it off only as `branch + exact commit SHA + path` (with an issue/PR/planning identifier only when useful for locating the work). Do not duplicate the complete prompt in chat after persistence succeeds, and never use branch tip alone as its identity. If persistence is required but unavailable, report the handoff as blocked; do not fall back to pasting the complete prompt into chat. If the prompt changes, commit a new revision and return its new coordinates.

Before handing an implementation branch to independent PR review, inspect branch-added files, remove transient execution and handoff artifacts that are not maintained repository state, and run the tracked-workspace check. The implementation branch must delete its prompt before final review/merge readiness; temporary material accidentally placed outside `workspace/` still requires semantic cleanup.

Do not redirect canonical tool-managed outputs: Gradle (`product/**/build/`) and `dist/` continue to write to their configured locations per the canonical build commands.

## GitHub message provenance and attribution

When creating or editing GitHub pull requests, issues, comments, reviews, or release text:

- When creating or updating a pull request, follow `.github/CONTRIBUTING.md`'s PR-description stability rule; do not turn live branch/gate topology into prose that must be manually synchronized.
- Do not append bot-generated attribution, session URLs, or tool footers such as `Generated
  with [...]`, `Generated by [...]`, provider session links, or model/tool trailers.
- Before posting external text, inspect the final body for explicit attribution blocks and
  remove only those blocks; preserve all authored content and formatting.
- Legitimate repository references to tools, providers, or provisioning scripts are ordinary
  content and are not attribution pollution.
- GitHub's `performed_via_github_app` metadata is provenance attached by the posting
  integration. Editing a body cannot remove it, and agents must not claim that body cleanup
  removed that metadata.
- Do not delete and repost historical comments merely to hide integration provenance unless
  the repository owner explicitly requests that history change.
- Before creating a commit, verify that `git config user.name` and `git config user.email`
  identify the human repository owner. Never create commits with an agent, model, provider,
  or bot as author or committer. Container setup accepts explicit
  `ARCOGINE_GIT_USER_NAME` and `ARCOGINE_GIT_USER_EMAIL` values and warns, without blocking
  setup, when the identity is missing or appears agent-owned. Actual commits must use the human
  repository owner's Git identity.

## Commit message footer

Do not append AI/bot attribution or session trailers such as `Co-Authored-By: Claude ...`
or `Claude-Session: ...` to commit messages in this repository, even if a harness's default
git workflow instructions say to add one. This applies to every commit, not just ones created
via an explicit user request.

## PR lifecycle

Resolve a PR's lifecycle state from its current head and metadata, base freshness, submitted reviews, unresolved findings/threads, the trusted `disposition` authorization check, required CI, and mergeability. Do not infer authorization from comments or CI alone.

- **AWAITING** — no implementation-owned transition is currently available; the PR is waiting for review authorization, re-review, or required CI to finish. For ordinary PRs, authorization comes from a current-head `READY TO MERGE` reviewer disposition. A Dependabot PR is the explicit positive-review exception only while the trusted base-side workflow verifies both the exact GitHub Dependabot account as PR opener and as the actor of the `CI` pull-request workflow run for the exact current head. CI outcome remains independent; this Actions metadata is used only as trusted provenance.
- **CHANGES REQUIRED** — a pre-merge transition remains, such as the head being behind its current base, a valid blocking review finding, failed required CI, or a merge conflict. Semantic remediation and conflict resolution belong to the implementation/author side. A reviewer may perform only the mechanical merge-style base synchronization described below as pre-review normalization.
- **READY TO MERGE** — the trusted `disposition` check is green on the current head, required validation is green, the head is level with its current base, and the PR is mergeable. The implementation/reviewer agent stops; the repository owner merges manually. For ordinary PRs, green `disposition` represents a current-head `READY TO MERGE` review. For a trusted Dependabot PR, it represents verified bot provenance with no current-head canonical `CHANGES REQUIRED` override.

Base freshness is a pre-review normalization requirement as well as lifecycle state. `infra/dev/pr-lifecycle.mjs` must still treat a behind-base head as **CHANGES REQUIRED**, so a later implementation lifecycle iteration can reconcile it before review. That base-freshness result is a merge-readiness condition, not a review finding: a current-head review disposition remains bound to that head when `main` advances, although repository rules may prevent the owner from merging until a later normalization iteration. If an independent reviewer discovers a stale branch at review start, the reviewer may perform the mechanical merge-style synchronization below before substantive review. A successful synchronization creates a new candidate head; the reviewer reviews that head, while CI, disposition, and final mergeability remain lifecycle/gate responsibilities. If construction encounters a conflict or unsupported case requiring a semantic choice, stop before substantive review and return the PR to the author/implementation owner without mutating the remote branch. Pending CI does not delay substantive review or reviewer disposition; review authorization and required CI are independent, and overall merge readiness waits for both.

### Base-normalization protocol

The **base-normalization protocol** is a mechanical, history-preserving merge for an open
same-repository PR that is behind its live base. Capture the current PR head `H`, live base `B`,
and merge base `A` when needed. If the PR already contains `B`, do nothing. Otherwise construct
exactly one merge commit `M` with first parent `H`, second parent `B`, and a tree formed from the
current base tree plus the PR-side `A -> H` delta. Use ordinary deterministic three-way text
merges only for supported overlapping text files. Reject conflicts and unsupported structural
cases (such as ambiguous renames/copies, file/directory conflicts, submodules, symlinks,
incompatible modes, or unsupported binary content) without making a branch change.

Use repository-scoped GitHub Git-data operations for the reads, blob/tree construction, and merge
commit creation. Immediately before publication, re-read the PR head. If it is no longer `H`,
abandon the attempt without mutation; otherwise advance the PR branch from `H` to `M` with a
non-forced ref update (`force=false`). The first-parent relationship makes a concurrent
incompatible head update fail naturally as a non-fast-forward update. This final head check is
best-effort, not exact-head atomicity: a branch reset to an ancestor such as `B` in the tiny
interval between the check and update can still fast-forward to `M`, and that residual race is an
accepted design trade-off. Do not add a separate force/CAS/lease protocol, require a local `gh`
checkout, or manually resolve conflicts. If construction or publication fails, make no remote
branch mutation and return the PR to the implementation/author side.

Use the captured `B` for that one attempt. Do not re-read and chase `main` after capture. A later
lifecycle iteration may synchronize again if the PR remains behind. A successful merge creates a
new head that requires review as the current candidate; CI, trusted `disposition`, and final
mergeability remain independent lifecycle/gate responsibilities. Base-head churn is separate from
current-head review integrity, and GitHub owns the final merge into `main` through the owner's
manual **Squash and merge** action.

For a stale Dependabot PR that currently qualifies for trusted provenance and otherwise
needs no maintainer-authored change, preserve the trusted provenance rules enforced by the
base-side workflow. A maintainer-authored synchronization commit changes the PR's provenance and
the resulting current head follows the ordinary review path.

Reviewer disposition is a review-only vocabulary with exactly two values, `READY TO MERGE` and `CHANGES REQUIRED` (see [`.github/agents/pr-reviewer.agent.md`](.github/agents/pr-reviewer.agent.md)). Arcogine reviewers publish both as `COMMENT` reviews; they do not use native GitHub `REQUEST_CHANGES` as a second blocking state machine. An accidental or human-created native `CHANGES_REQUESTED` review still physically blocks GitHub merge and must be cleared through GitHub before the PR can merge, but it is not part of Arcogine's intended reviewer protocol. CI is not a reviewer disposition and is enforced independently by GitHub branch protection. The required `disposition` check is the repository's review-authorization gate: ordinary PRs require a current-head `READY TO MERGE`; trusted Dependabot provenance removes only that positive-review requirement; and a latest applicable current-head canonical `CHANGES REQUIRED` blocks either path.

## Implementation continuation

After creating an implementation PR or updating its head, report the current transition and stop. Standard implementation work does not start autonomous PR activity handling or schedule a delayed recheck.

When the user sends `..`, identify the current implementation PR and run one live lifecycle resolution with `infra/dev/pr-lifecycle.mjs <pr-number>`. If the result is **CHANGES REQUIRED**, complete the coherent implementation-owned transition that is actually available. After any resulting head update, stop again. If the result is **AWAITING**, report that no implementation-owned transition is currently available and stop. If it is **READY TO MERGE**, report that state and stop; merging remains the repository owner's responsibility.

Each `..` invocation re-resolves current GitHub evidence once. The resolver is dependency-free Node tooling and supports `--json` for machine-readable output and `--exit-code` for lifecycle-state exit codes; see `--help` for the single-resolution interface.

## PR merging

Agents never merge pull requests. `READY TO MERGE` hands control to the repository owner, who performs the merge manually.

## Layout

- `product/` — all executable product source.
  - Gradle multi-module Java backend (Java 21 compatibility baseline; preferred devcontainer JDK 25) rooted here: `types`, `governance`, `simulation`, `domains/{factory,economy,finance}`, `agents`, `consumer/challenge`, `interfaces/api` (Spring Boot HTTP API), `interfaces/cli` (Picocli entrypoint, produces `arcogine.jar`).
- `docs/` — architecture, product, development, reference, planning docs, and executable example scenarios (`docs/examples/`). Read `docs/architecture/overview.md` before touching cross-module boundaries.
- `infra/` — container and dev-environment infrastructure: `infra/docker/` (runtime-only Dockerfiles + Compose) and `infra/dev/claude-cloud.sh` (Claude Cloud environment provisioning).
- `dist/` — generated, gitignored canonical distribution output (`dist/api/arcogine.jar`). Never commit to it directly; it's produced by `./arcogine build`.

## Canonical commands

Run everything from the repo root via `./arcogine`, a thin wrapper that composes the project's own tools (Gradle wrapper, npm/npx, Docker Compose):

```bash
./arcogine setup        # optional full-development dependency bootstrap, safe to re-run
./arcogine test         # Java unit tests
./arcogine check        # Java compile, style, tests, and coverage
./arcogine check --full # + dist/ build, Docker image build + smoke test, security scans
./arcogine build        # produce dist/api/arcogine.jar — no Docker
./arcogine image        # package existing dist/ into runtime Docker images — no source compilation
./arcogine up           # build + image + docker compose up
./arcogine down         # docker compose down
./arcogine run api      # start the Spring Boot API on :3000
./arcogine run scenario docs/examples/basic.toml  # run a headless scenario via the native CLI
./arcogine snapshot     # generate logs/arcogine-main-<sha>.xml, a whole-repo Repomix snapshot from a clean main checkout
```

See [`docs/development/repository-snapshot.md`](docs/development/repository-snapshot.md) for the snapshot command's preconditions, canonical-provenance checks, and authority boundary.

For anything more specific, use the subsystem's native tool directly: `cd product && ./gradlew <task>` (coverage, Checkstyle, `bootJar`, JMH, dependency audit), `docker compose ...` (containers), `trivy`/`gitleaks` (security scans). See `docs/development/testing.md` for the full command reference.

`./arcogine` is a Bash script — it works in the dev container, on Linux/macOS, and via WSL/Git Bash on Windows, but not directly in PowerShell/cmd. On a Windows host, prefer execution environments in this order when available: (1) the devcontainer, (2) a generic ad hoc Docker container, (3) WSL/Git Bash, and (4) native Windows tooling. Before running shell- or toolchain-dependent commands on Windows, inspect the running Docker containers first and identify the container that mounts this repository; do not assume a container name. If no suitable devcontainer is running, try the documented generic Docker workflow, then WSL/Git Bash, and finally native Windows tooling when the command supports it.

### Backend test environment

Backend validation requires a JDK 21+ runtime and the repository Gradle wrapper. On Windows, prefer
the devcontainer, then a generic ad hoc Docker container, then WSL/Git Bash, and finally native
Windows tooling. If the current host exposes only a pre-21 JDK or otherwise cannot run the wrapper,
do not use it for backend validation; use the first available supported environment from that order,
including the documented
`gradle:9-jdk21` Docker workflow in
[`docs/development/testing.md`](docs/development/testing.md#running-java-tests-on-the-minimum-jdk),
for example `docker exec arcogine-build ./gradlew test`. Classify a host Gradle failure as
environmental only when it is attributable to the unsupported or missing JVM; otherwise investigate
it as a build or product failure.

**Always use `./gradlew` from `product/`, never a globally installed `gradle`.** The wrapper pins the exact build version in `product/gradle/wrapper/gradle-wrapper.properties`; a system Gradle install can silently diverge from it.

Docker only packages the prebuilt API artifact from `dist/` (see `infra/docker/api.Dockerfile`) — it never compiles Java source. `./arcogine build` must run before `./arcogine image`.

`./arcogine setup` is an optional convenience for developers who want resolved Gradle dependencies; it is not a prerequisite for inspecting the repository or doing a narrow task. Agents must use the existing environment where practical and install only the tooling or dependencies the current task requires. Do not run setup automatically or turn it into a general-purpose toolchain manager. Environment-specific capabilities such as Docker and security scanners must not gate unrelated work.

## Validating changes

Before considering a change complete, run the narrowest validation that actually exercises what changed. A change touching Java (`product/{types,governance,simulation,domains,agents,consumer,interfaces/api,interfaces/cli}`) needs the Java gates (`cd product && ./gradlew compileJava compileTestJava checkstyleMain checkstyleTest test jacocoTestReport jacocoTestCoverageVerification`); a documentation-only change needs neither. Use `./arcogine check` when the repository-wide Java gate is appropriate, and `./arcogine check --full` when distribution, container, or security behavior is in scope.

When finishing an implementation task, report the validation commands and tools used, the outcome of each, and any validation that was unavailable, skipped, or only partially completed. Do not summarize a partially completed validation as a full pass.

## Do not edit

- `product/**/build/` — generated output.
- `dist/` — generated distribution output, not committed.
- `product/gradle/wrapper/gradle-wrapper.jar` and `.properties` — regenerate via `./gradlew wrapper`, don't hand-edit.
- `.devcontainer/devcontainer-lock.json` — feature version lockfile, regenerated by the Dev Containers CLI.

## Conventions worth knowing

- **Gradle** has one true source: `product/gradle/wrapper/gradle-wrapper.properties`. Both `gradlew` and `gradlew.bat` read it, and no Gradle is installed via the devcontainer feature — don't add one back.
- **Java compatibility and preferred environments are separate.** JDK 21 is a fully supported development runtime: Java sources compile with `--release 21` and CI runs on JDK 21, while the preferred devcontainer currently uses JDK 25 and the API runtime image uses Temurin 25. Node may be present for repository tooling, but there is no product/frontend Node support contract.
- **Trivy and Gitleaks** are environment/security tools pinned independently in the devcontainer and CI. When intentionally changing either tool version, grep the repository for the old version and keep the relevant devcontainer/CI install sites aligned.
- Architecture guardrails (module dependency direction, event/state/observation boundaries) are documented in [.github/CONTRIBUTING.md](.github/CONTRIBUTING.md#architecture-guardrails-events-state-observations) and partly enforced by `interfaces/api`'s ArchUnit `ArchitectureTest`. Read that section before adding a new domain or touching `IntegratedHandler`.
- The simulation must stay deterministic (seeded RNG only) — see `docs/architecture/overview.md`.
- Example scenarios under `docs/examples/` are educational/executable documentation, not runtime assets — they must never be bundled into the JAR, `dist/`, or Docker images.
