# Remove implementer-side PR monitoring

## Objective

Simplify the implementation-side pull-request lifecycle so that progression after a PR is created or updated is explicitly user-driven.

Remove the standard requirement for implementation sessions to start, maintain, or schedule autonomous PR monitoring. The only standard implementation continuation mechanism after PR creation is the repository shorthand `..`.

Each `..` invocation must re-resolve the live state of the current implementation PR once and perform the next implementation-owned transition, if one is available. If no implementation-owned transition is available, stop.

This change removes monitoring scaffolding; it does **not** remove or weaken lifecycle resolution, review authorization, CI enforcement, mergeability checks, base-freshness handling, or the owner-only merge boundary.

## Repository grounding

Before editing anything:

1. Read `AGENTS.md` from the current branch.
2. Read `.github/agents/pr-reviewer.agent.md` and `docs/development/reviewing.md` far enough to preserve the implementation/reviewer boundary.
3. Read `docs/development/testing.md` for the repository-owned validation contract.
4. Inspect `infra/dev/pr-watch.mjs`, `infra/dev/pr-watch.test.mjs`, and all maintained references to `pr-watch`, `--watch`, PR monitoring, background watchers, subscriptions, scheduled rechecks, and the `..` shorthand.
5. Search `docs/`, `.github/`, and `infra/` for semantic neighbors before deciding the exact edit set. At minimum search for:
   - `PR monitoring`
   - `implementation monitoring`
   - `pr-watch`
   - `--watch`
   - `scheduled`
   - `background watcher`
   - `session-scoped`
   - `` `..` ``
   - `AWAITING`
   - `CHANGES REQUIRED`
   - `READY TO MERGE`
6. Re-resolve live `main` before implementation. The prompt was authored from `main` at `b47cdb822c9df3b8c614351efb67f69bca23c86d`; do not assume that SHA is still current.

Repository authority overrides this prompt if the relevant contracts changed after it was written.

## Current contract to preserve

The implementation lifecycle currently distinguishes:

- `AWAITING` — no implementation-owned transition is available; the PR is waiting on review authorization/re-review or required CI.
- `CHANGES REQUIRED` — an implementation-owned pre-merge transition remains, such as base reconciliation, remediation of a valid blocking finding, failed required CI, or a merge conflict.
- `READY TO MERGE` — review authorization, required validation, base freshness, and mergeability are satisfied; agents stop and the repository owner merges manually.

Preserve these semantics unless current repository authority has explicitly changed them.

Also preserve the existing shorthand split:

- `..` — implementation continuation: re-resolve the current implementation PR and perform the next implementation-owned transition, if one exists.
- `./` — independent PR review/re-review under the PR Reviewer contract.

The desired result is that nothing automatically bridges those invocations.

## Required behavioral change

After an implementation PR is opened, or after the implementation side pushes a new PR head:

1. Do **not** start a watcher.
2. Do **not** subscribe to PR activity as part of standard implementation procedure.
3. Do **not** schedule a delayed recheck.
4. Do **not** run a background polling loop.
5. Do **not** treat monitoring startup or baseline confirmation as a delivery gate.
6. Do **not** claim that the implementation session will autonomously react to future review/CI events.
7. Stop after reporting the current transition and wait for explicit continuation.

When the user later sends `..`:

1. identify the current implementation PR from repository/session context;
2. re-resolve its live lifecycle state from current GitHub evidence;
3. if `CHANGES REQUIRED`, perform the next implementation-owned transition that is actually available;
4. after any resulting head update, stop again rather than beginning autonomous monitoring;
5. if `AWAITING`, report that no implementation-owned transition is currently available and stop;
6. if `READY TO MERGE`, report that state and stop; merging remains owner-only.

A single `..` invocation may complete the coherent implementation-owned transition it discovers; it is not intended to artificially stop halfway through a necessary remediation. It must not turn into indefinite monitoring after that transition is complete.

## `AGENTS.md` changes

Remove the current `## PR monitoring` policy and all standard-work requirements whose sole purpose is autonomous monitoring, including as applicable:

- automatic monitoring of every open PR associated with the current branch;
- native subscription preference;
- Claude `Monitor` guidance;
- background/streaming facility guidance;
- scheduled rechecks while `AWAITING`;
- session-scoped watcher expectations;
- monitoring-startup-as-delivery-gate language;
- restart-after-head-push instructions;
- monitor baseline/initial-state confirmation requirements;
- fail-loud watcher rules that exist only for a persistent monitor.

Replace that material with a concise implementation-continuation contract centered on `..` and single-shot lifecycle resolution.

Update nearby prose that currently says a stale branch is surfaced so “implementation monitoring” can reconcile it. Express the semantic behavior instead: a later implementation lifecycle iteration / `..` invocation can detect and reconcile the stale base.

Do not duplicate the full lifecycle state definitions unnecessarily; link or refer to the existing PR lifecycle section where possible.

## Lifecycle resolver tooling

`infra/dev/pr-watch.mjs` currently mixes two concerns:

1. authoritative single-shot PR lifecycle resolution;
2. continuous polling/watch behavior.

Retain the first and remove the second.

At minimum remove watcher-specific behavior such as:

- `--watch`;
- `--interval`;
- polling loops/timers;
- state-change streaming used only by watch mode;
- watcher-specific retry/failure-alert thresholds and messages;
- help text/examples describing continuous monitoring;
- comments whose rationale is specifically about keeping a watcher alive or polling repeatedly.

Preserve single-resolution capabilities needed by implementation continuation, including the existing lifecycle-state calculation and useful machine-readable/exit-code modes unless current code evidence shows they are unnecessary.

### Naming

Once continuous watch behavior is gone, `pr-watch.mjs` becomes misleading durable naming. Prefer renaming the helper and its test to a semantic lifecycle name such as:

- `infra/dev/pr-lifecycle.mjs`
- `infra/dev/pr-lifecycle.test.mjs`

Use the exact name that best matches the resulting responsibility after inspecting all current references. Do not keep “watch” merely to minimize the diff if the code no longer watches anything.

If a rename creates avoidable compatibility risk for an actual maintained caller, document that evidence and choose the narrowest coherent migration. Do not retain obsolete watch behavior as a compatibility shim unless repository evidence proves it is required.

## Tests

Preserve executable evidence for lifecycle resolution. Existing tests that prove correctness of the following remain valuable and should survive the cleanup:

- required-check identity and success;
- review/disposition parsing;
- native blocking-review handling where applicable;
- unresolved review-thread handling where applicable;
- base freshness / behind-base classification;
- mergeability and PR-state handling;
- current-head binding;
- pagination/truncation safety where the resolver still depends on it;
- `AWAITING`, `CHANGES REQUIRED`, and `READY TO MERGE` classification.

Delete or rewrite tests that exist only to prove continuous watch behavior, event/change emission, polling cadence, watcher retry behavior, or watcher-specific state diffs.

Tests should prove the post-change contract: one invocation resolves current lifecycle truth correctly; repeated resolution happens because `..` is invoked again, not because the helper keeps running.

## Semantic neighbors to reconcile

Inspect and update every maintained surface whose wording would otherwise continue to prescribe or imply autonomous implementation monitoring. Likely surfaces include, but are not limited to:

- `AGENTS.md`;
- `docs/development/testing.md`;
- `.github/workflows/ci.yml` comments/step names for the lifecycle resolver tests;
- `.github/workflows/pr-disposition.yml` comments;
- `.github/scripts/check-pr-disposition.sh` comments;
- `.github/scripts/continuous-improvement.mjs` or related continuous-improvement wording if it describes `pr-watch.mjs` specifically as monitoring rather than lifecycle enforcement;
- `docs/development/continuous-improvement.md` or the dated delivery retrospective if maintained present-tense process claims need clarification;
- any agent contracts or planning material that instruct implementation sessions to monitor after creating a PR.

Historical evidence should remain historical. Do not rewrite a dated retrospective merely because it records that monitoring existed at the time. Change only present-tense or normative material that would become false or misleading.

## Explicit non-goals

Do not use this cleanup to weaken or redesign unrelated PR controls.

In particular, do not:

- remove `AWAITING`, `CHANGES REQUIRED`, or `READY TO MERGE` lifecycle semantics;
- remove the trusted `disposition` check or its current-head authorization semantics;
- merge CI and reviewer disposition into one state machine;
- remove required CI enforcement;
- remove base-freshness detection or the base-normalization protocol;
- remove `pr-reconcile` functionality merely because monitoring is removed;
- change the independent PR Reviewer contract beyond wording required to keep the role boundary accurate;
- make reviewers responsible for implementation remediation;
- allow agents to merge PRs;
- redesign Dependabot provenance rules;
- add a replacement webhook daemon, scheduled workflow, bot, queue processor, or other autonomous monitoring infrastructure;
- introduce a new lifecycle abstraction unless removal of watcher code reveals a concrete need that cannot be handled by the existing resolver structure.

## Architecture / ADR rule

This is delivery-process/tooling simplification, not a product/domain architecture decision. Do not create an ADR unless implementation reveals a genuinely hard-to-reverse architectural decision that current repository authority does not already settle.

Do not edit Accepted/Superseded ADR history to describe this process cleanup.

## Documentation terminology

Use semantic durable terminology such as “PR lifecycle resolution”, “implementation continuation”, “current PR state”, and “implementation-owned transition”. Avoid retaining “watch”, “monitor”, or “poll” in durable names when the associated behavior no longer exists.

The `..` token itself may be documented in `AGENTS.md` because repository workflow shorthand is the subject there. Do not unnecessarily propagate shorthand into unrelated durable documentation when the semantic phrase is clearer.

## Validation

Run the narrowest repository-owned validation that covers the changed surfaces.

At minimum, if the lifecycle helper/test remains Node-based, run its direct Node test suite under the final filename.

Also run the repository checks that exercise maintained workflow/tooling integration affected by the change, including the always-running classification/tooling tests when their referenced paths or commands change. Use `docs/development/testing.md` and current CI as authority for exact commands rather than copying stale commands from this prompt.

Run the delivery-label and Markdown-link checks if documentation paths or references change and they are not already covered by the chosen repository-owned suite.

Do not claim validation passed unless it actually ran successfully in the current environment. Report unavailable checks precisely.

## Completion criteria

The change is complete when all of the following are true:

1. `AGENTS.md` no longer requires or recommends autonomous implementation-side PR monitoring as standard work.
2. `..` is clearly the standard post-PR implementation continuation mechanism.
3. Creating or updating a PR does not require starting any watcher, subscription, scheduled recheck, or background process.
4. Single-shot lifecycle resolution remains available and correctly classifies the PR lifecycle states.
5. The lifecycle helper contains no continuous-watch/polling mode.
6. Durable helper/test naming no longer falsely advertises watch behavior, unless a concrete maintained compatibility constraint justifies retaining the name.
7. Lifecycle resolver tests retain the safety-critical state-resolution coverage while watcher-only tests are removed.
8. CI/tooling/docs references use the resulting lifecycle helper name and responsibility consistently.
9. Review authorization, required CI, base normalization, mergeability, Dependabot provenance, and owner-only merge semantics are unchanged except for wording necessary to remove monitoring assumptions.
10. Repository-owned validation for the changed surfaces passes, or any environment limitation is explicitly reported.

## PR and handoff

Keep this as one narrow process/tooling cleanup PR.

The PR description should explain:

- that autonomous implementer monitoring was removed;
- that `..` is now the sole standard continuation mechanism after PR creation/update;
- that lifecycle resolution and enforcement remain intact;
- whether `pr-watch.mjs` was renamed and why;
- validation performed;
- any intentionally retained historical references.

After creating the PR, follow the **new behavior introduced by this change**: do not start a watcher or scheduled recheck. Stop and hand control back to the user. A later `..` invocation should re-resolve the PR and continue implementation-side work if necessary.

Independent review remains the responsibility of the PR Reviewer workflow (`./`). Agents never merge the PR.

## Final report

Report:

- branch and PR number;
- files changed;
- watcher/monitoring behavior removed;
- lifecycle-resolution behavior retained;
- any helper/test rename;
- semantic neighbors reconciled;
- validation commands and results;
- current PR lifecycle state at the time of the report;
- explicit confirmation that no persistent monitor or scheduled recheck was started.
