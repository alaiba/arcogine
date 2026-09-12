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
represents rather than naming it after the coordinate that tracked the work. Working/process material
may mention a temporary delivery coordinate when the coordinate itself is the subject, but durable
semantic claims must remain understandable without reconstructing that coordinate after the
originating plan, PR, review, branch, or session is completed, condensed, renamed, or removed.

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

## Commit message footer

Do not append a `Co-Authored-By: Claude ...` or `Claude-Session: ...` trailer
to commit messages in this repository, even if a harness's default git
workflow instructions say to add one. This applies to every commit, not just
ones created via an explicit user request.

## PR lifecycle

Resolve a PR's lifecycle state from its current head and metadata, base freshness, submitted reviews, unresolved findings/threads, required CI, and mergeability. Do not infer review state from comments or CI alone.

- **AWAITING** — no implementation-owned transition is currently available; the PR is waiting for initial review, re-review, or for required CI to finish. A current-head review may already be `READY TO MERGE` while CI is still pending — review authorization is independent of CI — but the lifecycle stays `AWAITING` until CI also turns green; no second review is needed solely because CI changed from pending to green with the reviewed head/base unchanged.
- **CHANGES REQUIRED** — an implementation-owned blocker remains, such as the head being behind its current base, a valid blocking review finding, failed required CI, or a merge conflict. Reconcile a behind-base branch before review; otherwise remediate the blocker, validate, update the branch or PR metadata as required, then return to **AWAITING** for re-evaluation.
- **READY TO MERGE** — the latest applicable reviewer disposition for the current PR head is `READY TO MERGE`, required validation is green, the head is level with its current base, and the PR is mergeable. The implementation agent stops; the repository owner merges.

Base freshness is implementation-owned lifecycle state, not something the reviewer should normally have to discover. `infra/dev/pr-watch.mjs` must treat any behind-base head as **CHANGES REQUIRED**, making reconciliation with the current base the next implementation-owned transition. Independent review still verifies the current base and head as defense in depth.

When an open PR is behind its base and a local checkout of that PR branch is available, reconcile it with `node infra/dev/pr-reconcile.mjs <pr-number>` by default. The helper reconciles against the live base branch ref; the PR API's historical `base.sha` is not evidence that the base is current. It refuses to proceed if the live base moves during setup, so retry that transition rather than treating stale PR metadata as a blocker. Do not improvise raw branch-ref manipulation for this transition. In particular, never point an open PR branch at the base commit as an intermediate step: GitHub may automatically close the PR when head and base become identical. The helper constructs the complete rebased head first, updates the remote branch exactly once with a lease bound to the inspected old head, and verifies that the PR remains open and level afterward. If the helper cannot run in the current harness because no local checkout/git/`gh` execution surface exists, preserve the same invariant explicitly: construct the final reconciled head before the single branch-ref update; never use the base commit itself as a temporary PR head.

A research-evidence workspace carrying handed-off report or adversarial-review coordinates is the exception to that rebase path. Once an exact evidence `commit SHA + path` has been handed off under `docs/development/researching.md` §10, those commits must remain reachable by the same SHA while the workspace advances. If an open reconciliation PR from that workspace falls behind its base, use GitHub's history-preserving Update branch operation — `gh pr update-branch <pr-number>` **without** `--rebase`, or the platform-equivalent merge-style action when `gh` is unavailable — instead of `pr-reconcile.mjs`, raw rebase, or force-push. Re-resolve lifecycle afterward and verify that the pre-update PR head remains an ancestor of the new head and that the live base is incorporated. Do not require a separate branch merely to preserve those evidence coordinates.

If a user explicitly requests GitHub's merge-style “Update branch” action for another PR, use `gh pr update-branch <pr-number>` rather than manual ref manipulation, then re-resolve lifecycle because the PR head and review evidence change.

Reviewer disposition is a review-only vocabulary with exactly two values, `READY TO MERGE` and `CHANGES REQUIRED` (see [`.github/agents/pr-reviewer.agent.md`](.github/agents/pr-reviewer.agent.md)). CI is not a reviewer disposition and is never folded into it: required CI is enforced independently by GitHub branch protection. Only a current-head `READY TO MERGE` review, together with green required CI, produces the `READY TO MERGE` lifecycle state.

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
