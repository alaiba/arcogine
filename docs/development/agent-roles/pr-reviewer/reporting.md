# GitHub feedback, final report and disposition

Part of the [PR Reviewer contract](../pr-reviewer.md#review-procedure); every complete review, re-review and `./` continuation follows all of its routed topics.

## GitHub feedback

For a complete live-PR review or re-review, post actionable findings and the disposition to the PR using a formal **COMMENT** review whenever formal review submission is available. Arcogine reviewer verdicts never use native GitHub `REQUEST_CHANGES`; the canonical disposition is the repository's review state machine. Do not use native `APPROVE` as a substitute for the canonical disposition either.

If an existing native `CHANGES_REQUESTED` review is active, report it as an anomalous GitHub blocker. If you authored it and it no longer represents an unresolved blocker, clear/dismiss it where the available GitHub operation and permissions allow; otherwise identify the required platform cleanup. Do not create new native change-request state.

Fall back to a PR conversation comment only if formal review submission itself is unavailable. Do not leave the only copy of actionable findings in a chat/session.

If the user explicitly requests a read-only or targeted report without posting, honor that request and state that durable PR feedback was not written.

Architectural knowledge that must outlive the PR belongs in maintained architecture, specifications, or the research register, not only in review comments.

## CI and validation

Use the precise validation language defined in `docs/development/reviewing.md`. Never equate author-reported local checks, absence of failing checks, or absence of a workflow run with visible current-head green CI.

When repository-owned checks can be run safely and are relevant, prefer them over invented substitutes. Do not run commands that mutate tracked files or dependency state merely to produce a review result.

## Final report

Every complete review/re-review that is actually performed must identify:

- PR number/title;
- reviewed `main` SHA;
- reviewed PR head SHA;
- review mode;
- material semantic-impact classification;
- actionable findings in `REV-<N>` order, with `OPEN` findings clearly visible;
- prior-finding lifecycle on re-review;
- validation/CI state;
- semantic neighbors inspected for medium/high-risk reviews, including important checked surfaces that required no change;
- any inspection limitations;
- explicit final disposition using `docs/development/reviewing.md`: `READY TO MERGE` or `CHANGES REQUIRED`.

The semantic-neighbor coverage note is evidence of review breadth, not proof of repository-wide consistency. Keep it compact and material; do not dump every search hit.

A trusted Dependabot PR may satisfy the required `disposition` check without any reviewer-authored positive disposition. If you are explicitly asked to review one, the review report still uses the normal format; do not claim that such a review was required merely to make the gate pass. A current-head `CHANGES REQUIRED` from that requested review still blocks the trusted path until superseded by a later current-head `READY TO MERGE` or by a new head on which the blocker is no longer applicable.

## Canonical disposition format

Every complete ordinary review/re-review must end with a machine-readable canonical disposition block. This block is parsed by the repository's PR disposition merge gate and must appear exactly once per review, at the end of the review body, in this format:

```
Reviewed head: <full-PR-head-SHA>
Disposition: **READY TO MERGE**
```

There are exactly two disposition values:
- `**READY TO MERGE**` — independent review of the code/docs is complete and no `OPEN` `REV-<N>` finding remains on this exact PR head
- `**CHANGES REQUIRED**` — at least one implementation-owned `REV-<N>` finding remains `OPEN`

There is no third disposition for "review is clean but CI is still pending." CI is not a reviewer disposition and review authorization is genuinely orthogonal to CI status, never coupled to it in review vocabulary: you may issue `READY TO MERGE` based solely on the code/docs review, regardless of whether required CI has finished for this head. For ordinary PRs, that reviewer verdict is necessary but not sufficient for merge — required CI, strict base freshness, mergeability, and other branch protections are enforced independently. Do not withhold a `READY TO MERGE` disposition merely because CI is still running, and do not treat CI transitioning from pending to green as by itself requiring a fresh review when the reviewed head remains unchanged.

**Important semantics:**

- The `Reviewed head:` must be the exact current PR head SHA inspected in this review. When a new commit is pushed or pre-review/final normalization creates a new PR head, the prior review's disposition becomes stale and does not authorize the new head.
- The head SHA may be written bare or wrapped in backticks as inline code; both are accepted, and the SHA itself must still match the current head exactly.
- The disposition block must be the final block in the review body. Prose elsewhere (examples, quoted prior reviews, discussion) mentioning disposition names is not authoritative.
- A targeted/incomplete review must not emit a merge-authorizing disposition unless you completed the full review procedure.
- A new head or a new finding requires a fresh disposition; CI alone changing state on an otherwise-unchanged reviewed head does not.

When re-reviewing or editing an existing review, update the canonical block to reflect the current state: new head SHA if normalization/remediation pushed a commit, and potentially a new disposition value.

For a targeted review, do not issue a full merge disposition unless you actually completed the full review procedure. A complete review with no findings should say so directly. Do not leave merge readiness implicit.
