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

Repository workflow shorthand must match the entire token:

- `.?` = perform the Session-close Kaizen review before ending or deleting the current session;
- `./` = review or re-review the current applicable pull request using the dedicated PR Reviewer contract;
- `..` = read the current implementation pull request's live GitHub state and perform the next implementation-owned transition, if one is available;

### Prompt handoff preflight

When asked to write, draft, generate, or prepare a complete prompt or handoff for another session,
agent, reviewer, researcher, or execution context, persist it before composing the complete body —
see [Artifact lifetime and transient workspace](#artifact-lifetime-and-transient-workspace) for where
and how. Do not draft the complete prompt in chat and persist a copy afterward: compose it directly
into the persisted `workspace/` artifact, commit it, and hand it off only as `branch + exact commit
SHA + path` (plus an issue/PR identifier only when useful). After persistence succeeds, do
not duplicate the prompt body in chat. If repository persistence is required but unavailable, report
the handoff as blocked rather than falling back to chat-only prompt custody.

A complete prompt or handoff is a persisted instruction/evidence package. An ordinary short
issue-based continuation notice (for example "Continue issue #N") is not one: it carries no
operational instructions beyond naming the issue and is not persisted.

Before committing a prompt, run a bounded content preflight:

1. verify the intended issue, its existing `issue/<N>-*` branch (see
   [Work admission, branch custody and issue coordination](#work-admission-branch-custody-and-issue-coordination)),
   and the output destination;
2. reconcile every operational instruction in the prompt with current repository and role authority;
   in particular, a prompt that asks for another branch for an issue that already has one, a
   workspace exchange file, or admission of work without an issue conflicts with those authorities
   and is corrected before commit, not carried forward.

Persistence establishes artifact identity; it does not confer authority on conflicting instructions.

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

Some repository tasks have additional repository-owned operating contracts. Their canonical
procedures live in [`docs/development/agent-roles/`](docs/development/agent-roles/README.md)
and apply regardless of the model or execution provider. Runtime-specific profiles,
including `.github/agents/` GitHub Copilot adapters, only load those procedures; they
do not replace or override them.

- **Dependency maintenance:** when asked to process, apply, remediate, or sweep dependency updates or Dependabot pull requests, read and follow [`docs/development/agent-roles/dependency-maintainer.md`](docs/development/agent-roles/dependency-maintainer.md) in addition to this file.
- **Continuous improvement:** when the user says `Assess continuous improvement.`, read and follow [`docs/development/agent-roles/continuous-improvement.md`](docs/development/agent-roles/continuous-improvement.md) in addition to this file.
- **Consistency review:** when asked to perform a repository consistency
  review, documentation/architecture reconciliation, periodic consistency
  sweep, or to operate as the consistency agent, read and follow
  [`docs/development/agent-roles/consistency.md`](docs/development/agent-roles/consistency.md)
  in addition to this file.
- **PR review:** when asked to independently review, re-review, or assess merge
  readiness of a pull request, read and follow
  [`docs/development/agent-roles/pr-reviewer.md`](docs/development/agent-roles/pr-reviewer.md)
  in addition to this file. This is the repository's dedicated **PR Reviewer**
  role; do not substitute a generic coding-agent review when the contract is
  available.
- **Research:** when asked to investigate a bounded Arcogine research question,
  execute a research brief from `docs/research/`, produce a decision-quality
  research report, or perform an independent adversarial review of an existing
  research report, or write the supporting implementation handoff for a concluded question,
  read and follow
  [`docs/development/agent-roles/researcher.md`](docs/development/agent-roles/researcher.md)
  in addition to this file. That contract defers to
  [`docs/development/researching.md`](docs/development/researching.md) for the
  normative research method. Research investigation and adversarial research
  review belong to this role, not to Consistency. Research never
  changes production code.
- **Implementation:** when asked to implement an issue, or to continue an
  implementation pull request with `..`, read and follow
  [`docs/development/agent-roles/implementer.md`](docs/development/agent-roles/implementer.md)
  in addition to this file.

There is no separate planning role. Known work waits in GitHub issues and open questions in the
research register; the repository owner chooses what to start. A request to recommend next work is
answered from open issues, `READY` research questions, open pull requests and their live
review/CI state.

Specialized agent contracts supplement `AGENTS.md`; they do not override
repository architecture, contribution, documentation, or executable
authorities.

## Temporary delivery coordinates and durable documentation

Arcogine tracks open questions in the [research register](docs/research/research-register.md) and known work in GitHub issues. An issue exists only for work defined well enough to implement; it waits until someone starts it, and in the rare case something blocks it, its body says what. Issues carry no workflow-state labels. Issue and pull-request numbers are the delivery coordinates. PR-local review/finding identifiers use the reserved `REV-<N>` namespace: ordinary variable-width decimal numbering, with no fixed-width zero-padding required or preferred. The former `PLAN-<TRACK>-<LOCAL-ID>` planning namespace is retired together with the planning area, and no tracked file may contain one.

These are temporary delivery coordinates. They may be used in issues, pull requests, PR descriptions/comments, reviews, branch names, commit messages, handoff prompts, and other active/delivery-history context where the coordinate helps track work — see [`.github/CONTRIBUTING.md`](.github/CONTRIBUTING.md)'s commit message guidance, which this section does not change.

Durable repository assets must not depend on transient coordinates. Durable assets include
architecture, product, reference, and development documents; code comments; workflow definitions;
and test/class/file names introduced alongside a change. A transient coordinate may remain in
active delivery context or durable delivery-history provenance (such as a commit message
or merged PR reference), but maintained semantic state must survive independently through the
durable capability, contract, identity, invariant, behavior, deliberately promoted artifact, or
provenance it represents. Exact `commit SHA + workspace/... path` pairs identify artifacts only
while those artifacts are in active workspace custody; copying the pair into durable state does
not preserve the artifact. Working/process material may discuss coordinate syntax when the syntax
itself is the subject. When delivery outcomes move into durable semantic naming, translate them
into what they represent rather than naming them after the coordinate that tracked them.

Product source and tests have a stricter lifetime boundary: maintained files under `product/`
must not reference `docs/research/` or transient research-workspace artifacts. Research may cite
implementation as evidence, but product source must describe current behavior, invariants, and
limitations in durable semantic terms and, when a textual authority is useful, point to the current
architecture, specification, reference, or development contract that owns them. The dependency
direction is one-way: transient research material may point to durable implementation; durable
product source must not point back to it.

The mechanical checkers enforce recognizable cases deterministically by scanning tracked
repository text (`git ls-files`, so generated/untracked/build output is never in scope):
`.github/scripts/check-delivery-labels.mjs` rejects any `PLAN-*` or `REV-<N>` token in a tracked
file or file path; `.github/scripts/check-transient-coordinates.mjs` rejects an exact full commit
SHA paired with a concrete `workspace/...` artifact path in durable files; and
`.github/scripts/check-source-authority-links.mjs` rejects product-source references to
`docs/research/` or `workspace/research/`. These checkers do not attempt to infer semantic
dependence from prose without a safe syntax signal. Catching semantic dependencies without a safe
literal signal (for example, prose like "the next stage") remains a human-review responsibility.

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

`workspace/` is not an archive. It must not be gitignored, must not contain a permanent marker file such as `.gitkeep` or a README, and must be absent from `main` and every merge candidate as tracked content. The repository-owned check `.github/scripts/check-transient-workspace.mjs` enforces this final-tree invariant. Git therefore normally shows no `workspace/` directory at all on `main`. Removing the temporary files from a branch's final tree does **not** remove their earlier commits or require deleting the branch; those commits remain reachable while its ref still retains the history.

**Branch deletion is owner-invoked only.** No agent or repository practice may delete a local or remote Git branch as implicit session, PR-merge, research-reconciliation, retrospective, or other post-task cleanup. Branch deletion requires a separate explicit user instruction authorizing that deletion. Before executing an authorized deletion, verify the intended ref, protection/default status, active work and PRs, unique unmerged commits, and any research-evidence/knowledge-transfer obligations; stop if any safety condition is uncertain. A branch-cleanup recommendation is informational and never authorizes a deletion.

A complete prompt/handoff (see [Prompt handoff preflight](#prompt-handoff-preflight)) belongs on the issue's single branch (or, for research evidence, the question's workspace branch) and must be committed with the repository owner's human Git identity under a semantic `workspace/` path before it is handed off; branch tip alone is not an immutable identity, and a changed prompt requires a new commit and new coordinates.

Before handing an implementation branch to independent PR review, ensure the contributor workflow's bounded semantic-closure obligation (`.github/CONTRIBUTING.md`) has been satisfied whenever its concrete trigger applies, then inspect branch-added files, remove transient execution and handoff artifacts that are not maintained repository state, and run the tracked-workspace check. Temporary material accidentally placed outside `workspace/` still requires semantic cleanup.

Do not redirect canonical tool-managed outputs: Gradle (`product/**/build/`) continues to write to its configured location per the canonical build commands.

## Work admission, branch custody and issue coordination

Development follows one path: **issue → one issue branch → pull request → independent review → owner merge.**

### Work admission

A GitHub issue is the sole entry point for any change to `main`: production code, tests, documentation and process changes, and the reconciliation of a research conclusion. "Implement issue #N" is sufficient: the Implementer fetches the live issue and its comments and grounds them against this file, the owning specifications, code and tests. An issue that does not yet decide the responsibility, meaning, acceptance evidence or non-goals stops the affected work for clarification; the Implementer does not invent semantics.

A research report, adversarial review or implementation handoff is supporting evidence, never an alternative admission route. A research conclusion that requires a maintained change is first expressed as a bounded issue, which may cite the exact reports, reviews or handoff (branch, commit SHA and path) it relies on. A request in conversation, an issue comment or an owner-authored handoff does not admit new scope; an owner who wants a fix, refactor or reconciliation opens an issue. Continuing an already-admitted pull request (review findings, CI failures, base freshness) is not a new origin, and an existing Dependabot pull request stays governed by the Dependency Maintainer contract and its provenance and merge rules without an issue prerequisite.

### Branch custody

An issue has **one branch**, published as `issue/<N>-<slug>`, and it carries every pull request-bound change for that issue, with no exception for research, review, handoff, reconciliation or operational isolation. Continuation reuses that branch; a scratch tree or detached checkout may isolate execution but is never published as a second branch for the issue. Responsibilities that must be delivered separately are separate bounded issues, each with its own branch. Previously published artifact commits stay immutable, and branch deletion remains owner-invoked (see [Artifact lifetime and transient workspace](#artifact-lifetime-and-transient-workspace)).

Investigation that changes nothing on `main` — a research question's evidence workspace — keeps its own temporary workspace branch as evidence custody under the [research operating model](docs/development/researching.md); that branch never carries a change to `main`.

### Issue coordination

The issue body records the agreed scope, contracts, acceptance evidence and non-goals. Issue comments carry substantive questions, decisions, blockers and results; keep them concise and omit execution diaries. There is no exchange file, dispatcher, registry, poller or status database.

- **Continuation.** Given only "issue #N", read the live issue body and comments, the associated `issue/<N>-*` branch and pull request, and every exact artifact the issue names. With no matching branch, none has been started; with several matching remote heads, stop and ask the owner which — never choose. Do not require the owner to relay coordinates.
- **Evidence locator.** When a committed report, reproducing evidence or complete prompt is the evidence for a comment, link its exact commit and path from the issue. Such artifacts may stay transient `workspace/` files on the issue branch (or, for research evidence, on the question's workspace branch), identified by exact commit and path; they are absent from `main` and from the final merge candidate.
- **Decisions.** A recorded decision answers a question and is not new scope. A material change of scope or meaning is made in the issue body (or a revised research conclusion); a new independent responsibility is a new issue.
- **Authority.** A comment, locator, commit, handoff or role label carries information, not authorization, and does not start or authorize another actor. The repository owner's live delegation ("implement issue #N", "continue issue #N") starts work. Independent review remains a distinct actor and session responsibility; branch separation does not establish independence.
- **Reviews stay on the pull request.** Formal review findings, disposition, CI and merge state live on the pull request. An issue comment must not become a second review ledger.
- **Identity.** Commits that publish evidence carry the repository owner's Git identity like every commit. A path that cannot comply is reported as a blocker; do not weaken the identity rule or fall back to chat-only custody of a prompt.

## GitHub message provenance and attribution

When creating or editing GitHub pull requests, issues, comments, reviews, or release text:

- When creating or updating a pull request, follow `.github/CONTRIBUTING.md` and the repository PR template: the body records stable change intent only. Do not put local validation results, unavailable checks, CI state, head/base coordinates, ahead/behind state, mergeability, or other live lifecycle facts in the PR body; report them in the implementation completion report instead.
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

Base freshness is a merge-readiness condition, not a review finding, and pending CI never delays review or disposition: a current-head disposition stays bound to that head when `main` advances. A reviewer who finds a stale branch at review start may perform only the mechanical **base-normalization protocol** defined in [`docs/development/reviewing.md`](docs/development/reviewing.md#1-resolve-and-normalize-the-live-revision) before substantive review; if it needs a semantic choice, the reviewer returns the PR to the implementation owner without mutating the branch. See that document and [`docs/development/agent-roles/pr-reviewer.md`](docs/development/agent-roles/pr-reviewer.md) for the exact mechanical algorithm, and for the `disposition` gate's authorization rules (trusted Dependabot provenance, ordinary reviewer authorization, and the `CHANGES REQUIRED` override). Reviewer disposition itself is a review-only vocabulary with exactly two values, `READY TO MERGE` and `CHANGES REQUIRED`; Arcogine reviewers publish it as a `COMMENT` review and never use native GitHub `REQUEST_CHANGES` or `APPROVE` as a substitute.

## Implementation continuation

After creating an implementation PR or updating its head, implementation work owns exact-head CI convergence before the completion report. Resolve the `CI` workflow for that exact head and wait synchronously for `CI / gate` to reach a terminal result. If it fails for an implementation-owned reason, remediate the failure, update the head, and repeat exact-head CI. If `main` advances or GitHub reports a conflict before the completion report, normalize or resolve the implementation-owned branch state and repeat CI for the resulting head. Do not wait for or manufacture independent reviewer disposition. For an ordinary implementation completion report, the expected state is: exact-head `CI / gate` green, the head contains live `main`, GitHub reports it mergeable, and the only unresolved merge gate is `disposition` because independent review has not yet authorized that exact head. If CI cannot start or a non-implementation-owned blocker prevents that state, report the blocker instead of claiming completion. Arcogine's review-readiness policy treats a PR stacked on another PR's branch as not ready for independent review until its dependency lands. On the next `..`, verify whether GitHub has already retargeted the PR to `main`, retarget it if necessary, reconcile its head with live `main`, and validate exact-head CI. Reviewer base normalization cannot retarget a PR.

When the user sends `..`, identify the current implementation PR and read its live GitHub state for the current head once: submitted reviews and the trusted `disposition` check, required checks, base freshness, mergeability/conflicts, and unresolved findings. If a coherent implementation-owned transition is available — such as remediating a valid current-head `CHANGES REQUIRED` finding, fixing failed required CI, resolving a conflict, or base normalization — perform that transition and carry its exact-head CI validation through to a terminal `CI / gate` result before reporting. A direct CI failure caused by that transition is part of the same implementation validation closure and should be repaired and revalidated; do not re-read reviewer disposition for a newly changed head in the same turn. Otherwise report the blocking or waiting fact (for example pending review or an owner-only action) and stop.

Agents never merge pull requests. When every merge gate holds, report that and stop; the repository owner merges manually.

## Layout

- `product/` — all executable product source.
  - Gradle multi-module Java backend (Java 21 compatibility baseline; preferred devcontainer uses a newer supported JDK configured in [`.devcontainer/devcontainer.json`](.devcontainer/devcontainer.json)) rooted here: `types`, `governance`, `storage`, `simulation`, `domains/factory`, `consumer/challenge`, `consumer/challenge-factory-integration-test`, `research-experiments`, `architecture-conformance-test`. `research-experiments` is non-shipped research infrastructure: the substrate for deterministic research experiments over the supported runtime contract, which no production or consumer source set depends on (see `docs/development/researching.md`). There is currently no application server, HTTP API, or CLI product surface — retained executable evidence is tests, conformance checks, and benchmarks; a future outward consumer is introduced from the supported runtime contract (`docs/architecture/runtime-contract.md`) when a concrete product need exists.
- `docs/` — architecture, product, development, research, and history docs. Read `docs/architecture/overview.md` before touching cross-module boundaries.
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
each, any validation that was unavailable or skipped, and the terminal exact-head `CI / gate` result
in the implementation completion report to the developer. This is transient execution knowledge: do not copy it
into the pull-request description, and do not summarize partial validation as a full pass.

**PR-body preflight:** use the repository pull-request template and keep only stable change intent:
summary/rationale, compatibility or migration impact, and material non-goals. A PR body has no
`## Validation` section. Local checks, unavailable checks, current CI/check results, head/base
coordinates, ahead/behind state, base freshness, and mergeability are live lifecycle facts to report
in the implementation completion report or resolve directly from GitHub, not prose to persist in the PR body.

## Do not edit

- `product/**/build/` — generated output.
- `product/gradle/wrapper/gradle-wrapper.jar` and `.properties` — regenerate via `./gradlew wrapper`, don't hand-edit.
- `.devcontainer/devcontainer-lock.json` — feature version lockfile, regenerated by the Dev Containers CLI.

## Conventions worth knowing

- **Gradle** has one true source: `product/gradle/wrapper/gradle-wrapper.properties`. Both `gradlew` and `gradlew.bat` read it, and no Gradle is installed via the devcontainer feature — don't add one back.
- **Trivy and Gitleaks** are environment/security tools pinned independently in the devcontainer and CI. When intentionally changing either tool version, grep the repository for the old version and keep the relevant devcontainer/CI install sites aligned.
- Architecture guardrails (module dependency direction, event/state/observation boundaries) are documented in [.github/CONTRIBUTING.md](.github/CONTRIBUTING.md#architecture-guardrails) and enforced by `architecture-conformance-test`'s ArchUnit `ArchitectureTest`. Read that section before adding a new domain.
- The simulation must stay deterministic (seeded RNG only) — see `docs/architecture/overview.md`.
