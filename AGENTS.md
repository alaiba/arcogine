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
- `.!` = run the on-demand Continuous Improvement assessment;
- `./` = review or re-review the current applicable pull request using the dedicated PR Reviewer contract;
- `..` = read the current implementation pull request's live GitHub state and perform the next implementation-owned transition, if one is available;

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
- **Continuous improvement:** when asked to assess the health of Arcogine's engineering practices, decide which improvement practice is worth running next, or identify opportunities to simplify or strengthen repository-owned standard work, read and follow [`.github/agents/continuous-improvement.agent.md`](.github/agents/continuous-improvement.agent.md) in addition to this file.
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

For `.?`, classify and durably capture anything from the current session that should
survive deletion, then give the deletion verdict. A Consistency review is **not** a
standing `.?` subroutine; recommend one only when the session itself provides a
concrete reason that a repository-wide consistency sweep would be useful, such as a
major cross-cutting architecture/status transition or evidence of broader semantic
drift.

Delivery-process retrospectives run only when explicitly requested. Do not assess or
recommend one during `.?` or normal repository grounding. An explicit Continuous
Improvement assessment may recommend a retrospective when current evidence makes that
formal measurement useful, but it does not run the retrospective or create due state.

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

Do not redirect canonical tool-managed outputs: Gradle (`product/**/build/`) continues to write to its configured location per the canonical build commands.

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

## PR merge gates

A PR is merge-ready only when all of these hold for its current head, each owned by its own authority: the trusted `disposition` check is green, required CI (`CI / gate`) is green, the head contains its live base, and GitHub reports it mergeable. Read these facts live from GitHub; do not infer authorization from comments or CI alone, and do not collapse them into a derived lifecycle state.

Base freshness is a merge-readiness condition, not a review finding: a current-head disposition stays bound to that head when `main` advances. Semantic remediation and conflict resolution belong to the implementation/author side. A reviewer who finds a stale branch at review start may perform only the mechanical base-normalization protocol below before substantive review; if it needs a semantic choice, the reviewer returns the PR to the implementation owner without mutating the branch. Pending CI does not delay review or disposition.

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
continuation may synchronize again if the PR remains behind. A successful merge creates a new
head that requires review as the current candidate; CI, trusted `disposition`, and final
mergeability remain independent gates. Base-head churn is separate from
current-head review integrity, and GitHub owns the final merge into `main` through the owner's
manual **Squash and merge** action.

For a stale Dependabot PR that currently qualifies for trusted provenance and otherwise
needs no maintainer-authored change, preserve the trusted provenance rules enforced by the
base-side workflow. A maintainer-authored synchronization commit changes the PR's provenance and
the resulting current head follows the ordinary review path.

Reviewer disposition is a review-only vocabulary with exactly two values, `READY TO MERGE` and `CHANGES REQUIRED` (see [`.github/agents/pr-reviewer.agent.md`](.github/agents/pr-reviewer.agent.md)). Arcogine reviewers publish both as `COMMENT` reviews; they do not use native GitHub `REQUEST_CHANGES` as a second blocking state machine. An accidental or human-created native `CHANGES_REQUESTED` review still physically blocks GitHub merge and must be cleared through GitHub before the PR can merge, but it is not part of Arcogine's intended reviewer protocol. CI is not a reviewer disposition and is enforced independently by GitHub branch protection. The required `disposition` check is the repository's review-authorization gate: ordinary PRs require a current-head `READY TO MERGE`; trusted Dependabot provenance removes only that positive-review requirement; and a latest applicable current-head canonical `CHANGES REQUIRED` blocks either path. Dependabot provenance is trusted only while the base-side workflow verifies the exact GitHub Dependabot account as both PR opener and actor of the `CI` pull-request workflow run for the exact current head; that Actions metadata is provenance only, and CI outcome stays independent.

## Implementation continuation

After creating an implementation PR or updating its head, report the current transition and stop. Standard implementation work does not start autonomous PR activity handling or schedule a delayed recheck.

When the user sends `..`, identify the current implementation PR and read its live GitHub state for the current head once: submitted reviews and the trusted `disposition` check, required checks, base freshness, mergeability/conflicts, and unresolved findings. If a coherent implementation-owned transition is available — such as remediating a valid current-head `CHANGES REQUIRED` finding, fixing failed required CI, resolving a conflict, or base normalization — perform exactly that transition; after any head change, stop and let the next `..` re-read live state. Otherwise report the blocking or waiting fact (for example pending review, pending CI, or an owner-only action) and stop.

Agents never merge pull requests. When every merge gate holds, report that and stop; the repository owner merges manually.

## Layout

- `product/` — all executable product source.
  - Gradle multi-module Java backend (Java 21 compatibility baseline; preferred devcontainer JDK 25) rooted here: `types`, `governance`, `simulation`, `domains/{factory,economy,finance}`, `agents`, `consumer/challenge`, `consumer/challenge-factory-integration-test`, `architecture-conformance-test`. There is currently no application server, HTTP API, or CLI product surface — retained executable evidence is tests, conformance checks, and benchmarks; a future outward consumer is introduced from the supported runtime contract (`docs/architecture/runtime-contract.md`) when a concrete product need exists.
- `docs/` — architecture, product, development, reference, planning docs, and executable example scenarios (`docs/examples/`). Read `docs/architecture/overview.md` before touching cross-module boundaries.
- `infra/` — dev-environment infrastructure: `infra/dev/claude-cloud.sh` (Claude Cloud environment provisioning) and related repository tooling.

## Canonical commands

Run everything from the repo root via `./arcogine`, a thin wrapper that composes the project's own tools (Gradle wrapper, npm/npx):

```bash
./arcogine setup        # optional full-development dependency bootstrap, safe to re-run
./arcogine test         # Java unit tests
./arcogine check        # Java compile, style, tests, and coverage
./arcogine check --full # + dependency audit, secret scan
./arcogine snapshot     # generate logs/arcogine-main-<sha>.xml, a whole-repo Repomix snapshot from a clean main checkout
```

See [`docs/development/repository-snapshot.md`](docs/development/repository-snapshot.md) for the snapshot command's preconditions, canonical-provenance checks, and authority boundary.

For anything more specific, use the subsystem's native tool directly: `cd product && ./gradlew <task>` (coverage, Checkstyle, JMH, dependency audit), `trivy`/`gitleaks` (security scans). See `docs/development/testing.md` for the full command reference.

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

`./arcogine setup` is an optional convenience for developers who want resolved Gradle dependencies; it is not a prerequisite for inspecting the repository or doing a narrow task. Agents must use the existing environment where practical and install only the tooling or dependencies the current task requires. Do not run setup automatically or turn it into a general-purpose toolchain manager. Environment-specific capabilities such as security scanners must not gate unrelated work.

## Validating changes

Before considering a change complete, run the narrowest validation that actually exercises what changed. A change touching Java (`product/{types,governance,simulation,domains,agents,consumer,architecture-conformance-test}`) needs the Java gates (`cd product && ./gradlew compileJava compileTestJava checkstyleMain checkstyleTest test jacocoTestReport jacocoTestCoverageVerification`); a documentation-only change needs neither. Use `./arcogine check` when the repository-wide Java gate is appropriate, and `./arcogine check --full` when dependency-audit or secret-scan behavior is in scope.

When finishing an implementation task, report the validation commands and tools used, the outcome of each, and any validation that was unavailable, skipped, or only partially completed. Do not summarize a partially completed validation as a full pass.

## Do not edit

- `product/**/build/` — generated output.
- `product/gradle/wrapper/gradle-wrapper.jar` and `.properties` — regenerate via `./gradlew wrapper`, don't hand-edit.
- `.devcontainer/devcontainer-lock.json` — feature version lockfile, regenerated by the Dev Containers CLI.

## Conventions worth knowing

- **Gradle** has one true source: `product/gradle/wrapper/gradle-wrapper.properties`. Both `gradlew` and `gradlew.bat` read it, and no Gradle is installed via the devcontainer feature — don't add one back.
- **Java compatibility and preferred environments are separate.** JDK 21 is a fully supported development runtime: Java sources compile with `--release 21` and CI runs on JDK 21, while the preferred devcontainer currently uses JDK 25. Node may be present for repository tooling, but there is no product/frontend Node support contract.
- **Trivy and Gitleaks** are environment/security tools pinned independently in the devcontainer and CI. When intentionally changing either tool version, grep the repository for the old version and keep the relevant devcontainer/CI install sites aligned.
- Architecture guardrails (module dependency direction, event/state/observation boundaries) are documented in [.github/CONTRIBUTING.md](.github/CONTRIBUTING.md#architecture-guardrails-events-state-observations) and enforced by `architecture-conformance-test`'s ArchUnit `ArchitectureTest`. Read that section before adding a new domain.
- The simulation must stay deterministic (seeded RNG only) — see `docs/architecture/overview.md`.
- Example scenarios under `docs/examples/` are educational/executable documentation, not runtime assets — there is currently no distributable artifact for them to be bundled into.
