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

Prefer stronger forms of durable capture in this order when they fit the lesson: executable guard/test, canonical helper/tooling, agent/contributor standard work, maintained documentation, then an ADR only for genuinely architectural or hard-to-reverse decisions. Generalize incidents into semantic rules rather than preserving session or PR coordinates as durable concepts. Prefer improving an existing authoritative artifact over creating a new one.

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
repository architecture, ADR, contribution, documentation, or executable
authorities.

## Continuous improvement

Arcogine's continuous-improvement operating model — Session-close Kaizen, the weekly
Consistency review, and the evidence-based delivery-process retrospective — is defined
in [`docs/development/continuous-improvement.md`](docs/development/continuous-improvement.md).
Recurring-obligation due state lives in the GitHub issue titled exactly
`Continuous improvement register`, maintained by
[`.github/workflows/continuous-improvement.yml`](.github/workflows/continuous-improvement.yml).

At the first normal repository grounding of a session, inspect that register's
workflow-managed obligations section:

- `CURRENT` only → say nothing.
- `DUE` or `OVERDUE` → mention the obligation once during the session.
- `CHECK_TRIGGER` → mention once that the retrospective trigger needs human/agent
  evaluation; do not claim the retrospective is automatically due.
- register state cannot be read/verified → say once that continuous-improvement
  obligation state could not be verified; do not silently assume everything is
  current.

Never repeat the same reminder more than once per session, and never derail the
user's requested task merely because an improvement obligation is due — this is
defense in depth so scheduled work does not disappear unnoticed, not a gate on
other work.

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
outlive the delivery context that produced it. This includes ADR, architecture, product, reference, or
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

When editing an Accepted or Superseded ADR only to improve durable terminology or legibility, follow
`docs/architecture/decisions/README.md`: the amendment must be semantics-preserving, explicitly
recorded as an editorial amendment, and independently reviewed for semantic equivalence. A semantic
decision change still requires supersession.

## Temporary artifacts

Ad hoc diagnostic reports, one-off log captures, and transient session artifacts that would otherwise be written at repository root should go to the `logs/` directory at the repository root. The `logs/` directory is gitignored as a whole. Keep the root and working directory clean; use `logs/coverage.txt`, `logs/test-output.log`, etc. instead of root-level files.

Do not redirect canonical tool-managed outputs: Gradle (`product/**/build/`), npm/Vitest (`product/interfaces/web/coverage/`, `test-results/`), Playwright (`playwright-report/`), and `dist/` continue to write to their configured locations per the canonical build commands.

## GitHub message provenance and attribution

When creating or editing GitHub pull requests, issues, comments, reviews, or release text:

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
  setup, when the identity is missing or appears agent-owned.

## Commit message footer

Do not append AI/bot attribution or session trailers such as `Co-Authored-By: Claude ...`
or `Claude-Session: ...` to commit messages in this repository, even if a harness's default
git workflow instructions say to add one. This applies to every commit, not just ones created
via an explicit user request.

## PR lifecycle

Resolve a PR's lifecycle state from its current head and metadata, base freshness, submitted reviews, unresolved findings/threads, the trusted `disposition` authorization check, required CI, and mergeability. Do not infer authorization from comments or CI alone.

- **AWAITING** — no implementation-owned transition is currently available; the PR is waiting for review authorization, re-review, or required CI to finish. For ordinary PRs, authorization comes from a current-head `READY TO MERGE` reviewer disposition. A Dependabot PR is the explicit positive-review exception only while the trusted base-side workflow verifies both the exact GitHub Dependabot account as PR opener and as the actor of the `CI` pull-request workflow run for the exact current head. CI outcome remains independent; this Actions metadata is used only as trusted provenance.
- **CHANGES REQUIRED** — a pre-merge transition remains, such as the head being behind its current base, a valid blocking review finding, failed required CI, or a merge conflict. Semantic remediation and conflict resolution belong to the implementation/author side. A reviewer may perform only the conflict-free base synchronization described below as pre-review normalization.
- **READY TO MERGE** — the trusted `disposition` check is green on the current head, required validation is green, the head is level with its current base, and the PR is mergeable. The implementation/reviewer agent stops; the repository owner merges manually. For ordinary PRs, green `disposition` represents a current-head `READY TO MERGE` review. For a trusted Dependabot PR, it represents verified bot provenance with no current-head canonical `CHANGES REQUIRED` override.

Base freshness is a pre-review normalization requirement as well as lifecycle state. `infra/dev/pr-watch.mjs` must still treat any behind-base head as **CHANGES REQUIRED**, so implementation monitoring can reconcile it before review. If an independent reviewer instead discovers a stale branch at review start, the reviewer should attempt the repository-approved conflict-free synchronization before doing substantive review. A successful mechanical synchronization is reviewer-side normalization, not implementation remediation: re-resolve live `main`, the new head, CI, and the net diff, then review only that reconciled head. Pending CI does not delay substantive review or reviewer disposition; review authorization and required CI are independent, and overall merge readiness waits for both. If synchronization produces a merge conflict, requires a semantic choice, lacks permission, or otherwise cannot complete mechanically, stop before substantive review and return the PR to the author/implementation owner; do not spend review effort or file a reconciliation finding against the stale head.

### Base-normalization protocol

The **base-normalization protocol** is canonical; `infra/dev/pr-reconcile.mjs` is the
preferred native GitHub adapter for ordinary PRs, not the protocol itself. Normal freshness
is history-preserving: incorporate the live base into the PR branch without replacing the
existing PR history. Route by capability and keep the invariants below independent of the
adapter or harness:

| Route | When available | Branch operation | Required safety boundary |
| --- | --- | --- | --- |
| Native Update branch | GitHub's merge-style Update branch operation is exposed | Ask GitHub to merge the live base into the PR branch; use `expected_head_sha` or equivalent atomic expected-head protection when supported | GitHub owns merge computation and conflict detection; verify the normalized head afterward |
| Mechanical Git-data fallback | Native Update branch is unavailable, but repository-scoped commit/tree/blob reads, commit/tree creation, and ref movement with `force=false` are available | Construct merge commit `M`, then advance `H -> M` with a non-forced fast-forward ref update | Use only the exact-tree protocol below; never turn a rejected fast-forward into a forced update |
| Research-evidence workspace | Handed-off evidence SHA+path coordinates must remain reachable | Use the native route or the mechanical fallback, whichever is available and safe | Preserve every evidence SHA and the pre-update head as ancestors; do not rebase or force-push evidence history |
| Trusted Dependabot | The base-side workflow verifies the exact Dependabot opener and exact-head CI actor | Prefer Dependabot's supported rebase/recreate mechanism | Do not silently add a maintainer-authored connector merge while the provenance exception applies |
| Explicit history rewrite | A user or documented special workflow explicitly requires replacement history | Construct the complete replacement before mutation and update through force-with-lease/CAS/atomic expected-head protection | This is exceptional; the exact inspected old head remains mandatory |

For the native and mechanical history-preserving routes, first re-resolve the live base ref,
current PR head, PR state, and head repository. Require an open same-repository PR; the PR
API's historical `base.sha` is not evidence that the base is current. Immediately before
mutation, re-read the PR head and live base and stop if either moved. Afterward, verify the
PR remains open, the old head and the live base are ancestors of the resulting head,
`behind_by == 0`, `ahead_by > 0`, and the net live-base→new-head diff remains non-empty.
Re-resolve CI, reviews, trusted `disposition`, and mergeability because the resulting head
is new. Pending CI does not delay substantive review or reviewer disposition: review
authorization and required CI are independent, and overall merge readiness waits for both.
Explicit rewrite and Dependabot routes retain their own identity/provenance checks below
rather than claiming old-head ancestry.

The native route is preferred. `node infra/dev/pr-reconcile.mjs <pr-number>` and
`gh pr update-branch <pr-number>` without `--rebase` are native merge-style adapters where
available. They must bind the update to the inspected head with `expected_head_sha` or an
equivalent atomic expected-head precondition. If the native operation conflicts, requires a
semantic choice, lacks permission, or otherwise cannot complete safely, return the PR to
the implementation/author unless the mechanical fallback below is available.

The mechanical fallback uses these symbols: `H` is the inspected PR head, `B` is the live
base head, `A` is their resolved merge base, `T` is the mechanically proven merged tree,
and `M` is a merge commit with first parent `H`, second parent `B`, and tree `T`. It is
available only when all of the following are proven from repository-scoped Git data:

1. `A -> H` and `A -> B` can be represented as exact tree-entry changes.
2. Cross-path changes have no file/directory ancestor collision or case-folded/path-normalization
   ambiguity. Exact same-path regular-file modify/modify is not automatically a conflict: fetch
   the exact blob bytes from `A`, `B`, and `H` and run Git's deterministic three-way text merge
   (`git merge-file -p <H-file> <A-file> <B-file>` or an equivalent invocation of the same Git
   merge algorithm). Accept that path only when the merge reports cleanly with no conflict
   markers; create the result blob from those exact output bytes and bind the planner input to
   the exact `A`/`B`/`H` blob SHAs plus the resulting blob SHA. A real text conflict stops
   normalization rather than inviting reviewer judgment.
3. No rename/copy interpretation, add/delete or delete/modify resolution, binary-content merge,
   file-mode choice, symlink, submodule, unsupported mode, or other semantic merge choice is
   required. The current pure planner (`infra/dev/pr-merge-plan.mjs`) accepts only complete,
   non-truncated recursive tree snapshots whose regular blob leaves have exact `100644` or
   `100755` modes. Same-path text merging is limited to ordinary regular files whose mode is
   unchanged across `A`, `B`, and `H`; unsupported shapes fail closed. A recursive Git tree
   response with `truncated: true` — or without an explicit `truncated: false` proof — is
   incomplete; recursively expand its subtrees through the connector or return the PR to the
   implementation/author.
4. `T` is constructed completely before any ref mutation by starting from `B`, applying disjoint
   PR-side blob states exactly, and substituting only verified clean text-merge result blobs for
   supported same-path modify/modify cases. Preserve exact modes and mechanically unambiguous
   deletions. The resulting tree must retain a non-empty net PR diff.
5. `M` is created with repository-compliant human author/committer identity, no bot/session
   attribution, and the exact parents/tree above. If identity cannot be established and
   verified, the fallback is unavailable.

Immediately before the one ref mutation, require the PR head still equals `H` and live base
   still equals `B`; otherwise discard `M` and recompute. Move the branch exactly once with
   `force=false`. The server's non-forced update is the concurrency guard for this
   history-preserving route: a concurrent forward or divergent/rebased head that is not an
   ancestor of `M` must be rejected, and that rejection means retry from the new state,
   never `force=true`. This is not identical to exact-head CAS: a deliberate backward reset
   to an ancestor of `M` can remain fast-forwardable during the narrow race after the final
   read. That residual race is accepted only for this non-forced fallback; native Update
   branch with `expected_head_sha` remains stronger and preferred. The CAS/lease requirement
   for true history rewrites is unchanged.

Post-update verification must additionally confirm that the resulting head is `M`, its
parents are `[H, B]`, its complete non-truncated tree is exactly `T`, the current live base is still an ancestor,
and the live-base diff is exactly the intended non-empty PR change. If `main` advances to
`B2`, do not roll back the successful merge; report the branch stale and repeat the same
normalization against `B2`. Research-evidence workspaces must also verify every handed-off
evidence SHA remains reachable. A conflict, unsupported tree shape, identity failure,
failed verification, or unavailable safe route returns the stale PR to the
implementation/author before substantive review.

For a stale Dependabot PR that currently qualifies for trusted provenance and otherwise
needs no maintainer-authored change, prefer Dependabot's own supported rebase/recreate
mechanism. GitHub permits maintainers to add commits to Dependabot branches, so a
maintainer-authored merge/rebase commit intentionally revokes the no-positive-review
exception. If such a commit is necessary or deliberately added, the resulting PR follows
the ordinary review path.

Reviewer disposition is a review-only vocabulary with exactly two values, `READY TO MERGE` and `CHANGES REQUIRED` (see [`.github/agents/pr-reviewer.agent.md`](.github/agents/pr-reviewer.agent.md)). Arcogine reviewers publish both as `COMMENT` reviews; they do not use native GitHub `REQUEST_CHANGES` as a second blocking state machine. An accidental or human-created native `CHANGES_REQUESTED` review still physically blocks GitHub merge and must be cleared through GitHub before the PR can merge, but it is not part of Arcogine's intended reviewer protocol. CI is not a reviewer disposition and is enforced independently by GitHub branch protection. The required `disposition` check is the repository's review-authorization gate: ordinary PRs require a current-head `READY TO MERGE`; trusted Dependabot provenance removes only that positive-review requirement; and a latest applicable current-head canonical `CHANGES REQUIRED` blocks either path.

## PR monitoring

For any open PR associated with the current branch, start monitoring it without asking for confirmation. On any signal, re-resolve the PR lifecycle state and perform any available implementation-owned transition.

Use `infra/dev/pr-watch.mjs` rather than rediscovering how to query GitHub:

```bash
node infra/dev/pr-watch.mjs <pr-number>            # resolve lifecycle state once, then exit
node infra/dev/pr-watch.mjs <pr-number> --watch    # emit one line per change, for a background watcher
```

It is dependency-free Node (builtins only, no install step) and reads `GH_TOKEN`/`GITHUB_TOKEN`, falling back to `gh auth token` once at startup. `--json` gives machine-readable output and `--exit-code` maps the lifecycle state onto the exit status; see `--help`.

### Keeping a watcher running

The script is harness-neutral. How you keep it running is not — each agent harness has different primitives, so use whichever of these applies.

**Prefer a native PR-activity subscription when the current session actually exposes one** — webhook-driven wake beats polling and costs no API traffic. Otherwise use `pr-watch.mjs`, which depends on nothing but Node and the GitHub API and is therefore always available.

This section deliberately names no subscription tool. It previously named one that did not resolve, and agents improvised a poller per session instead; naming a replacement would pin repository guidance to an external detail this file cannot keep accurate. Check the tools the session actually exposes rather than expecting this file to tell you what exists.

**Claude Code** — run `--watch` under the `Monitor` tool with `persistent: true`, so each emitted line arrives as a notification:

```bash
export PATH="/c/Program Files/nodejs:/c/Program Files/Git/cmd:/c/Program Files/GitHub CLI:$PATH"
cd <repo-root>
exec node infra/dev/pr-watch.mjs <pr-number> --watch --interval 60
```

Two things that are easy to get wrong:

- The harness shell may not share your interactive shell's `PATH`. On Windows/Git Bash, `node`, `git` and `gh` are all commonly missing from it, and `gh` fails without `git`. Set `PATH` explicitly, as above, rather than assuming. Verify the invocation once directly before trusting a background watcher.
- A running watcher holds the script it loaded at startup. Editing `pr-watch.mjs` does **not** affect it — stop and restart the watcher after changing the script, or it will keep running the old logic.

**Other harnesses** (Codex and others) have their own primitives and generally no equivalent of `Monitor`. Use whatever background or streaming facility exists; if there is none, run the single-resolution form at each decision point, and if scheduled tasks are supported keep at most one recheck scheduled about 10 minutes out while the PR is **AWAITING**.

A session-scoped watcher is expected and sufficient: its purpose is to let the session react to review and CI feedback on its own rather than the repository owner relaying state changes. It ends with the session, and that is fine — it is not intended as durable infrastructure.

Treat monitoring startup as a delivery gate: immediately after opening a PR or pushing a new PR head, establish at most one session-scoped monitoring mechanism when the harness supports persistent monitoring and verify its initial-state evidence before reporting the transition complete. The selected mechanism must surface lifecycle-relevant changes and fail visibly if monitoring stops working. If persistent monitoring is unavailable, perform the single-resolution form at each lifecycle decision point and say that no persistent monitor is active.

The repository-owned `pr-watch.mjs` is the default fallback when no native or harness-provided monitor exists. Stop and restart this fallback after every head push because it holds the script loaded at startup; for a devcontainer checkout, use the equivalent of:

```bash
cd /workspaces/arcogine
exec node infra/dev/pr-watch.mjs <pr-number> --watch --interval 60
```

Do not claim that a PR is being monitored unless the selected mechanism has provided its startup/initial-state confirmation; for the `pr-watch` fallback, that means its emitted baseline line.

### Rules for any monitoring mechanism

A monitor must fail loudly: if it cannot reach GitHub it must say so, because a silent watcher is indistinguishable from a quiet PR. Do not report a PR as unchanged on the strength of a monitor that has not actually confirmed it.

`..` remains the immediate manual continuation mechanism, and works regardless of whether a watcher is running.

## PR merging

Agents never merge pull requests. `READY TO MERGE` hands control to the repository owner, who performs the merge manually.

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
./arcogine snapshot     # generate logs/arcogine-main-<sha>.xml, a whole-repo Repomix snapshot from a clean main checkout
```

See [`docs/development/repository-snapshot.md`](docs/development/repository-snapshot.md) for the snapshot command's preconditions, canonical-provenance checks, and authority boundary.

For anything more specific, use the subsystem's native tool directly: `cd product && ./gradlew <task>` (coverage, Checkstyle, `bootJar`, JMH, dependency audit), `cd product/interfaces/web && npm ...`/`npx ...` (lint, typecheck, build, Playwright), `docker compose ...` (containers), `trivy`/`gitleaks` (security scans). See `docs/development/testing.md` for the full command reference.

`./arcogine` is a Bash script — it works in the dev container, on Linux/macOS, and via WSL/Git Bash on Windows, but not directly in PowerShell/cmd. Use the dev container on Windows; it's the supported path. Before running shell- or toolchain-dependent commands on Windows, inspect the running Docker containers first, identify the container that mounts this repository, and execute there rather than assuming a container name. If no suitable dev container is running, use the documented WSL/Git Bash fallback or report the missing environment.

### Backend test environment

Backend validation requires a JDK 21+ runtime and the repository Gradle wrapper. On Windows, use
the dev container when practical. If the current host exposes only a pre-21 JDK or otherwise cannot
run the wrapper, do not use it for backend validation; use the dev container or the documented
`gradle:9-jdk21` Docker workflow in
[`docs/development/testing.md`](docs/development/testing.md#running-java-tests-on-the-minimum-jdk),
for example `docker exec arcogine-build ./gradlew test`. Classify a host Gradle failure as
environmental only when it is attributable to the unsupported or missing JVM; otherwise investigate
it as a build or product failure.

**Always use `./gradlew` from `product/`, never a globally installed `gradle`.** The wrapper pins the exact build version in `product/gradle/wrapper/gradle-wrapper.properties`; a system Gradle install can silently diverge from it.

Docker only packages prebuilt artifacts from `dist/` (see `infra/docker/api.Dockerfile`, `infra/docker/web.Dockerfile`) — it never compiles Java or frontend source. `./arcogine build` must run before `./arcogine image`.

`./arcogine setup` is an optional convenience for developers who want the full local dependency set (frontend packages, Playwright Chromium, and resolved Gradle dependencies); it is not a prerequisite for inspecting the repository or doing a narrow task. Agents must use the existing environment where practical and install only the tooling or dependencies the current task requires. Do not run setup automatically or turn it into a general-purpose toolchain manager. Environment-specific capabilities such as Docker and security scanners must not gate unrelated work.

## Validating changes

Before considering a change complete, run the narrowest validation that actually exercises what changed. A change touching only Java (`product/{types,simulation,domains,agents,consumer,interfaces/api,interfaces/cli}`) needs only the Java gates (`cd product && ./gradlew compileJava compileTestJava checkstyleMain checkstyleTest test jacocoTestReport jacocoTestCoverageVerification`); a change touching only the frontend (`product/interfaces/web/`) needs only its gates (`cd product/interfaces/web && npm run lint && npx tsc --noEmit && npm run test:coverage && npm run build`); a documentation-only change needs neither. When a change spans both, or you can't tell whether it's narrow, run `./arcogine check`, which runs both unconditionally. For anything touching the API-web contract or E2E flows, also run `cd product/interfaces/web && npx playwright test` (or `./arcogine check --full`) — Playwright's own config builds/starts the API jar and web dev server via `webServer`, but the jar must already be built once (`cd product && ./gradlew :cli:bootJar`) for a clean checkout.

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