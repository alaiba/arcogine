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

### Prompt handoff preflight

When asked to write, draft, generate, or prepare a complete prompt or handoff for another session,
agent, reviewer, researcher, or execution context, persist it before composing the complete body —
see [Artifact lifetime and transient workspace](#artifact-lifetime-and-transient-workspace) for where
and how. Do not draft the complete prompt in chat and persist a copy afterward: compose it directly
into the persisted `workspace/` artifact, commit it, and hand it off only as `branch + exact commit
SHA + path` (plus an issue/PR/planning identifier only when useful). After persistence succeeds, do
not duplicate the prompt body in chat. If repository persistence is required but unavailable, report
the handoff as blocked rather than falling back to chat-only prompt custody.

### Session-close Kaizen

When the user's entire message is `.?`, inspect the current session and live repository for anything
that should survive deletion of the conversation, following the classification and capture-preference
rules in [`docs/development/continuous-improvement.md`](docs/development/continuous-improvement.md#session-close-kaizen).
Prefer improving an existing authoritative artifact over creating a new one, and never replace known
repository context with generic GitHub discovery.

Finish every review with an explicit deletion verdict: either the session is safe to delete because
nothing unique remains, or name exactly what still needs to be captured first. Default output reports
material findings/actions plus that verdict; do not narrate adjacent practices — a Consistency review
or a delivery-process retrospective — that the session gives no concrete reason to recommend.

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

## Temporary delivery coordinates and durable documentation

Arcogine planning coordinates use the reserved `PLAN-<TRACK>-<LOCAL-ID>` namespace (for example, a
hierarchical item might read `PLAN-<TRACK>-4-B`). `<TRACK>` is a stable, repository-wide track code
(currently `FD` Factory Design, `ENG` Factory Simulation Engine, `GOV` Governance/Conformance,
`CHAL` Factory-Design Challenge, `OPS` Operational Execution/Digital Twin); `<LOCAL-ID>` is one or
more hyphen-separated segments, extended for hierarchical items rather than inventing another
namespace. Compact, ad-hoc, or track-local coordinate syntax (a bare letter+number, a dotted or
space-separated variant, etc.) must not be introduced — the whole point of one reserved namespace
is that a temporary coordinate is always unmistakable on sight. PR-local review/finding
identifiers use the separate `REV-<N>` namespace: ordinary variable-width decimal numbering, with
no fixed-width zero-padding required or preferred.

Both namespaces are temporary delivery coordinates. They may be used in `docs/planning/`, issues,
pull requests, PR descriptions/comments, reviews, branch names, commit messages, handoff prompts,
and other active/delivery-history context where the coordinate helps sequence or track work — see
[`.github/CONTRIBUTING.md`](.github/CONTRIBUTING.md)'s commit message guidance, which this section
does not change.

Durable repository assets must not depend on transient coordinates. Durable assets include
architecture, product, reference, and development documents; code comments; workflow definitions;
and test/class/file names introduced alongside a change. A transient coordinate may remain in
active planning/delivery context or durable delivery-history provenance (such as a commit message
or merged PR reference), but maintained semantic state must survive independently through the
durable capability, contract, identity, invariant, behavior, deliberately promoted artifact, or
provenance it represents. Exact `commit SHA + workspace/... path` pairs identify artifacts only
while those artifacts are in active workspace custody; copying the pair into durable state does
not preserve the artifact. Working/process material may discuss coordinate syntax when the syntax
itself is the subject. When delivery outcomes move into durable semantic naming, translate them
into what they represent rather than naming them after the coordinate that tracked them.

Planning filenames are semantic, not coordinate-derived: the delivery label belongs in a planning
document's content, not its path, so the filename keeps describing the subject if sequencing
changes later.

The mechanical checkers enforce recognizable cases deterministically by scanning tracked
repository text (`git ls-files`, so generated/untracked/build output is never in scope):
`.github/scripts/check-delivery-labels.mjs` rejects a `PLAN-*` or `REV-<N>` token outside
`docs/planning/`, while `.github/scripts/check-transient-coordinates.mjs` rejects an exact full
commit SHA paired with a concrete `workspace/...` artifact path in durable files. Neither checker
attempts to infer semantic dependence from prose without a safe syntax signal. A `PLAN-*` or
`REV-<N>` token outside `docs/planning/` is a durable-naming leak; inside `docs/planning/`, the
old ambiguous label forms it replaced (a bare `Gate` plus number, a bare letter-plus-number
optionally dotted/hyphenated, `W1`, `DH-` plus a letter) may not be reintroduced. Those old forms
are not banned outside `docs/planning/` — they can be ordinary, unrelated identifiers elsewhere
in the codebase — which is exactly why the reserved `PLAN-`/`REV-` namespaces exist. Catching
semantic dependencies without a safe literal signal (for example, prose like "the next stage")
remains a human-review responsibility.

`docs/architecture/` holds Arcogine's current architecture — see
[`docs/architecture/overview.md`](docs/architecture/overview.md) for cross-cutting principles and
domain boundaries, and [`.github/CONTRIBUTING.md`](.github/CONTRIBUTING.md) for the rule that a
significant architectural change is reconciled into the owning architecture/specification document
rather than left in delivery history, plus the historical decision-rationale exception.

## Artifact lifetime and transient workspace

Classify temporary material by its intended lifetime:

- **Local ephemeral material:** use `logs/` for ad hoc diagnostics, local captures, and session scratch that should never be committed. The `logs/` directory is gitignored as a whole. Keep the root and working directory clean; use `logs/coverage.txt`, `logs/test-output.log`, etc. instead of root-level files.
- **Transient committed material:** use the reserved `workspace/` root for branch-local artifacts that must survive the current session or be handed to another actor, but are not intended to survive on `main`. This includes implementation and fresh-session prompts, research reports/revisions, adversarial reviews, checkpoints, diagnostic notes, and intentionally branch-transient review packets. A useful semantic structure is `workspace/implementation/`, `workspace/research/`, and `workspace/review/`.
- **Durable material:** keep maintained repository state in its existing canonical locations (`docs/`, product code/tests, scripts, workflows, and related maintained surfaces). A transient artifact can inform durable reconciliation without becoming durable itself.

`workspace/` is not an archive. It must not be gitignored, must not contain a permanent marker file such as `.gitkeep` or a README, and must be absent from `main` and every merge candidate as tracked content. The repository-owned check `.github/scripts/check-transient-workspace.mjs` enforces this final-tree invariant. Git therefore normally shows no `workspace/` directory at all on `main`.

A complete prompt/handoff (see [Prompt handoff preflight](#prompt-handoff-preflight)) must be committed with the repository owner's human Git identity under a semantic `workspace/` path before it is handed off; branch tip alone is not an immutable identity, and a changed prompt requires a new commit and new coordinates.

Before handing an implementation branch to independent PR review, ensure the contributor workflow's bounded semantic-closure obligation (`.github/CONTRIBUTING.md`) has been satisfied whenever its concrete trigger applies, then inspect branch-added files, remove transient execution and handoff artifacts that are not maintained repository state, and run the tracked-workspace check. Temporary material accidentally placed outside `workspace/` still requires semantic cleanup.

Do not redirect canonical tool-managed outputs: Gradle (`product/**/build/`) continues to write to its configured location per the canonical build commands.

## GitHub message provenance and attribution

When creating or editing GitHub pull requests, issues, comments, reviews, or release text:

- When creating or updating a pull request, follow `.github/CONTRIBUTING.md`'s PR-description stability rule; do not turn live branch/gate topology into prose that must be manually synchronized.
- Do not append bot-generated attribution, session URLs, or tool footers such as `Generated
  with [...]`, `Generated by [...]`, provider session links, or model/tool trailers — this applies
  to commit messages too (for example `Co-Authored-By: Claude ...` or `Claude-Session: ...`), even
  if a harness's default workflow instructions suggest adding one.
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

## PR merge gates

A PR is merge-ready only when all of these hold for its current head, each owned by its own authority: the trusted `disposition` check is green, required CI (`CI / gate`) is green, the head contains its live base, and GitHub reports it mergeable. Read these facts live from GitHub; do not infer authorization from comments or CI alone, and do not collapse them into a derived lifecycle state.

Base freshness is a merge-readiness condition, not a review finding, and pending CI never delays review or disposition: a current-head disposition stays bound to that head when `main` advances. A reviewer who finds a stale branch at review start may perform only the mechanical **base-normalization protocol** defined in [`docs/development/reviewing.md`](docs/development/reviewing.md#1-resolve-and-normalize-the-live-revision) before substantive review; if it needs a semantic choice, the reviewer returns the PR to the implementation owner without mutating the branch. See that document and [`.github/agents/pr-reviewer.agent.md`](.github/agents/pr-reviewer.agent.md) for the exact mechanical algorithm, and for the `disposition` gate's authorization rules (trusted Dependabot provenance, ordinary reviewer authorization, and the `CHANGES REQUIRED` override). Reviewer disposition itself is a review-only vocabulary with exactly two values, `READY TO MERGE` and `CHANGES REQUIRED`; Arcogine reviewers publish it as a `COMMENT` review and never use native GitHub `REQUEST_CHANGES` or `APPROVE` as a substitute.

## Implementation continuation

After creating an implementation PR or updating its head, report the current transition and stop. Standard implementation work does not start autonomous PR activity handling or schedule a delayed recheck.

When the user sends `..`, identify the current implementation PR and read its live GitHub state for the current head once: submitted reviews and the trusted `disposition` check, required checks, base freshness, mergeability/conflicts, and unresolved findings. If a coherent implementation-owned transition is available — such as remediating a valid current-head `CHANGES REQUIRED` finding, fixing failed required CI, resolving a conflict, or base normalization — perform exactly that transition; after any head change, stop and let the next `..` re-read live state. Otherwise report the blocking or waiting fact (for example pending review, pending CI, or an owner-only action) and stop.

Agents never merge pull requests. When every merge gate holds, report that and stop; the repository owner merges manually.

## Layout

- `product/` — all executable product source.
  - Gradle multi-module Java backend (Java 21 compatibility baseline; preferred devcontainer JDK 25) rooted here: `types`, `governance`, `simulation`, `domains/{factory,finance}`, `consumer/challenge`, `consumer/challenge-factory-integration-test`, `architecture-conformance-test`. There is currently no application server, HTTP API, or CLI product surface — retained executable evidence is tests, conformance checks, and benchmarks; a future outward consumer is introduced from the supported runtime contract (`docs/architecture/runtime-contract.md`) when a concrete product need exists.
- `docs/` — architecture, product, development, reference, and planning docs. Read `docs/architecture/overview.md` before touching cross-module boundaries.
- `infra/` — dev-environment infrastructure: `infra/dev/claude-cloud.sh` (Claude Cloud environment provisioning) and related repository tooling.

## Canonical commands

`./arcogine` (run from the repo root) is the canonical developer entry point; run `./arcogine --help`
or see [`docs/development/testing.md`](docs/development/testing.md) for the exact command list,
native subsystem commands (`cd product && ./gradlew <task>`, `trivy`, `gitleaks`), and the JDK-21
Docker workflow that backend validation needs when the host JDK is older.

`./arcogine` is a Bash script — it works in the dev container, on Linux/macOS, and via WSL/Git Bash
on Windows, but not directly in PowerShell/cmd. On a Windows host, or when the current JDK is below
21, prefer execution environments in this order when available: (1) the devcontainer, (2) a generic
ad hoc Docker container, (3) WSL/Git Bash, and (4) native Windows tooling. Before running shell- or
toolchain-dependent commands on Windows, inspect the running Docker containers first and identify the
container that mounts this repository; do not assume a container name. If no suitable devcontainer is
running, try the documented generic Docker workflow, then WSL/Git Bash, and finally native Windows
tooling when the command supports it. Classify a host Gradle failure as environmental only when it is
attributable to the unsupported or missing JVM; otherwise investigate it as a build or product
failure.

## Validating changes

Before considering a change complete, run the narrowest validation that actually exercises what
changed — see [`docs/development/testing.md`](docs/development/testing.md) for the Java gates and
native commands; a documentation-only change needs neither.

When finishing an implementation task, report the validation commands and tools used, the outcome of
each, and any validation that was unavailable, skipped, or only partially completed. Do not summarize
a partially completed validation as a full pass.

**PR-body preflight:** base reconciliation, ahead/behind state, mergeability, current CI/check
results, and current head/base coordinates are live lifecycle facts to verify when required, not
PR-description validation to persist. Follow [`.github/CONTRIBUTING.md`](.github/CONTRIBUTING.md)'s
PR-description stability rule: `## Validation` states reproducible commands/checks/review actually
performed, not the resulting branch topology.

## Do not edit

- `product/**/build/` — generated output.
- `product/gradle/wrapper/gradle-wrapper.jar` and `.properties` — regenerate via `./gradlew wrapper`, don't hand-edit.
- `.devcontainer/devcontainer-lock.json` — feature version lockfile, regenerated by the Dev Containers CLI.

## Conventions worth knowing

- **Gradle** has one true source: `product/gradle/wrapper/gradle-wrapper.properties`. Both `gradlew` and `gradlew.bat` read it, and no Gradle is installed via the devcontainer feature — don't add one back.
- **Trivy and Gitleaks** are environment/security tools pinned independently in the devcontainer and CI. When intentionally changing either tool version, grep the repository for the old version and keep the relevant devcontainer/CI install sites aligned.
- Architecture guardrails (module dependency direction, event/state/observation boundaries) are documented in [.github/CONTRIBUTING.md](.github/CONTRIBUTING.md#architecture-guardrails-events-state-observations) and enforced by `architecture-conformance-test`'s ArchUnit `ArchitectureTest`. Read that section before adding a new domain.
- The simulation must stay deterministic (seeded RNG only) — see `docs/architecture/overview.md`.
