# PR merge gates

A PR is merge-ready only when all of these hold for its current head, each owned by its own authority: the trusted `disposition` check is green, required CI (`CI / gate`) is green, the head contains its live base, and GitHub reports it mergeable. Read these facts live from GitHub; do not infer authorization from comments or CI alone, and do not collapse them into a derived lifecycle state.

Base freshness is a merge-readiness condition, not a review finding, and pending CI never delays review or disposition: a current-head disposition stays bound to that head when `main` advances. A reviewer who finds a stale branch at review start may perform only the mechanical **base-normalization protocol** defined in [`docs/development/reviewing.md`](../reviewing.md#1-resolve-and-normalize-the-live-revision) before substantive review; if it needs a semantic choice, the reviewer returns the PR to the implementation owner without mutating the branch. See that document and [`docs/development/agent-roles/pr-reviewer.md`](../agent-roles/pr-reviewer.md) for the exact mechanical algorithm, and for the `disposition` gate's authorization rules (trusted Dependabot provenance, ordinary reviewer authorization, and the `CHANGES REQUIRED` override). Reviewer disposition itself is a review-only vocabulary with exactly two values, `READY TO MERGE` and `CHANGES REQUIRED`; Arcogine reviewers publish it as a `COMMENT` review and never use native GitHub `REQUEST_CHANGES` or `APPROVE` as a substitute.

## Implementation continuation

After creating an implementation PR or updating its head, implementation work owns exact-head CI convergence before the completion report. Resolve the `CI` workflow for that exact head and wait synchronously for `CI / gate` to reach a terminal result. If it fails for an implementation-owned reason, remediate the failure, update the head, and repeat exact-head CI. If `main` advances or GitHub reports a conflict before the completion report, normalize or resolve the implementation-owned branch state and repeat CI for the resulting head. Do not wait for or manufacture independent reviewer disposition. For an ordinary implementation completion report, the expected state is: exact-head `CI / gate` green, the head contains live `main`, GitHub reports it mergeable, and the only unresolved merge gate is `disposition` because independent review has not yet authorized that exact head. If CI cannot start or a non-implementation-owned blocker prevents that state, report the blocker instead of claiming completion. Arcogine's review-readiness policy treats a PR stacked on another PR's branch as not ready for independent review until its dependency lands. On the next `..`, verify whether GitHub has already retargeted the PR to `main`, retarget it if necessary, reconcile its head with live `main`, and validate exact-head CI. Reviewer base normalization cannot retarget a PR.

When the user sends `..`, identify the current implementation PR and read its live GitHub state for the current head once: submitted reviews and the trusted `disposition` check, required checks, base freshness, mergeability/conflicts, and unresolved findings. If a coherent implementation-owned transition is available — such as remediating a valid current-head `CHANGES REQUIRED` finding, fixing failed required CI, resolving a conflict, or base normalization — perform that transition and carry its exact-head CI validation through to a terminal `CI / gate` result before reporting. A direct CI failure caused by that transition is part of the same implementation validation closure and should be repaired and revalidated; do not re-read reviewer disposition for a newly changed head in the same turn. Otherwise report the blocking or waiting fact (for example pending review or an owner-only action) and stop.

Agents never merge pull requests. When every merge gate holds, report that and stop; the repository owner merges manually.

## Validating changes

Before considering a change complete, run the narrowest validation that actually exercises what
changed — see [`docs/development/testing.md`](../testing.md) for the Java gates and
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
