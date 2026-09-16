# Arcogine consistency review

This contract defines Arcogine's repository consistency review. It is built for a ChatGPT chat session using the GitHub connector against `alaiba/arcogine`.

Do not require a local checkout, `git`, `gh`, shell commands, or local execution of repository scripts. Read repository state, files, issues, pull requests, commits, reviews, and CI evidence through the GitHub connector.

A formal Consistency review is diagnostic plus the narrow finding-ledger and register accounting described below. It does not authorize source/doc remediation, planning changes, ADR changes, pull-request creation, or merging. Ad-hoc consistency questions in chat are read-only analyses; they are not formal recurring reviews and do not record completion.

## Goal

Deeply inspect the repository for semantic inconsistency. Determine whether implementation, architecture, ADRs, planning, public/reference documentation, examples, configuration, tests, CI, and prior findings tell a coherent, temporally honest, evidence-backed story.

A previous clean review is not evidence that older content is correct. Every review remains free to uncover an older inconsistency. Recency guides attention; it never bounds scope.

Do not force artifacts to use identical wording. Compare semantic claims about the same subject, lifecycle state, authority, and point in time.

## Authority and time

Use the authority that owns the question:

| Question | Primary authority |
| --- | --- |
| Product destination | `docs/product/charter.md` |
| Implemented architecture today | `docs/architecture/overview.md`, corroborated by source/tests/config |
| Architectural rationale | accepted ADRs in `docs/architecture/decisions/` |
| Research state | `docs/development/researching.md` and `docs/research/research-register.md` |
| Planned/gated/partial/deferred work | applicable `docs/planning/` documents |
| Public interface | implementation/tests reconciled with `docs/reference/` and consumers |
| Commands, versions, modules, CI behavior | executable configuration and workflow definitions |
| Development/review process | `.github/CONTRIBUTING.md` and `docs/development/` |
| Consistency-review procedure | this file |
| Persisted finding identity | the GitHub issue number |

GitHub consistency issues preserve finding identity and lifecycle continuity; they are not product, architecture, planning, or implementation authority.

Classify each material claim using this complete generic review taxonomy: `CURRENT`, `NORMATIVE_DECISION`, `PROPOSED`, `PLANNED`, `IMPLEMENTED_STATUS`, `PARTIAL`, `DEFERRED`, `BLOCKED`, `NON_GOAL`, `HISTORICAL`, or `COMPATIBILITY_DEBT`. Domain-owned lifecycles such as research statuses remain their own vocabulary and are not additional generic review states. If this taxonomy later proves insufficient, change this contract explicitly rather than inventing a new state during a run.

Proposed/planned behavior differing from current source is not drift by itself; a current-state artifact presenting planned behavior as implemented is.

Accepted ADRs preserve decision history. Do not call historical wording/path context stale merely because implementation later moved. A changed architectural decision requires the repository's ADR supersession process, not retrospective rewriting of history.

## Start of every formal review

Re-ground from live repository state rather than conversation memory or uploaded snapshots:

1. Resolve the current `main` SHA and read this file from that head.
2. Read `AGENTS.md`.
3. Read GitHub issue `#295`, titled `Continuous improvement register`.
4. Load the currently open consistency findings whose titles begin `CONS:`.
5. If #295 records a resolvable previous reviewed head, compare that head to current `main` and use new/changed material as the first attention priority.

Issue #295 is mandatory repository state. If it is missing, inaccessible, has the wrong title, or its weekly Consistency section is malformed, stop with `INCOMPLETE`. Do not recreate, replace, or guess the register.

If no previous reviewed head is recorded, perform the same deep review without a recency anchor.

## Review strategy

The review is repository-wide in intent. Start with newness when a previous reviewed head exists, then follow semantic evidence wherever it leads. Do not stop at the comparison range, and do not treat content predating the previous review as cleared.

For each material concept investigated:

1. Identify the claim and its lifecycle state.
2. Search the repository for the concept, important symbols, and terminology.
3. Read the authoritative current/planning/ADR/test/interface surfaces returned by that search.
4. Inspect source/config/tests or existing GitHub CI/check evidence when they materially prove or contradict the claim.
5. Inspect recent/open pull requests only when history is needed to understand a transition or determine whether an existing finding is in flight.
6. Compare semantic neighbors and decide which authority, if any, is wrong.
7. If something appears even mildly inconsistent, follow the thread far enough to classify it rather than dismissing it because it is old or outside the recent-change set.

Prefer repository search over a permanently duplicated neighbor matrix. Examples: a `FactoryModel` semantic change should lead to factory architecture/ADRs/planning and Engine assumptions; an API/DTO change should lead to reference docs and consumers; a CI/toolchain change should lead to testing/contribution policy. These examples are not exhaustive.

Carry every open issue-backed finding forward on every review.

When a new candidate finding is identified, search closed `CONS:` issues using its semantic subject, terminology, and evidence before creating anything. Reopen the matching issue for a regression; otherwise create a new finding. Closed history is queried on demand rather than preloaded.

## Findings

A material finding requires both:

1. the artifact making the claim; and
2. contradictory authoritative/executable evidence, or a clear demonstration that required evidence is absent.

Use exact paths, symbols, criteria, ADRs, tests, PRs, commits, and issue numbers where available. Use confidence `HIGH`, `MEDIUM`, or `LOW`; do not inflate confidence because CI is green.

Useful categories are:

`PUBLIC_DOC_DRIFT`, `ARCHITECTURE_DRIFT`, `ARCHITECTURE_STALENESS`, `PLANNING_STATUS_DRIFT`, `ASPIRATIONAL_LEAKAGE`, `ADR_CONFLICT`, `EXECUTABLE_EVIDENCE_DRIFT`, `INTERFACE_DRIFT`, `DEPENDENCY_BOUNDARY_DRIFT`, `TERMINOLOGY_IDENTITY_DRIFT`, `TOOLCHAIN_CI_DRIFT`, `LINK_PATH_DRIFT`, `STANDARD_PROVENANCE_DRIFT`, `DUPLICATED_AUTHORITY`, and `PR_INCOMPLETE_RECONCILIATION`.

Use the current P0/P1/P2/P3/Nit severity definitions from `docs/development/reviewing.md`; do not invent another severity model.

Do not report an inconsistency solely because two artifacts use different wording, a proposal differs from current implementation, a compatibility alias is intentionally retained, an internal symbol is undocumented, or an open PR contains behavior not yet on `main`.

## GitHub finding ledger

The GitHub issue number is the sole canonical durable identity for a finding. All consistency findings use:

```text
CONS: <concise semantic title>
```

A new finding issue body needs only durable diagnostic evidence, for example:

```text
Severity: P0 | P1 | P2 | P3 | Nit
Category: <category>
Confidence: HIGH | MEDIUM | LOW

Claim:
<path/symbol/criterion and claim>

Contradictory evidence:
<path/symbol/test/config and evidence>

Why inconsistent:
<semantic conflict>

Authority:
<owning authority and why>
```

Do not duplicate mutable lifecycle state in the body.

Lifecycle uses GitHub state plus current review evidence:

- unresolved -> issue open;
- plausible corrective PR -> issue remains open and is reported `IN_FLIGHT`;
- verified fixed on reviewed `main` -> close the issue as completed;
- false positive/duplicate/superseded -> close with the appropriate reason/explanation;
- regression of a closed finding -> reopen the same issue.

A merged PR, closed issue, review comment, or green CI result is not proof of resolution. Only authoritative evidence on the reviewed `main` head establishes that a finding is fixed.

Invoking a formal Consistency review authorizes only the issue creation/update/reopen/close operations required to account for that review's findings and the final update of the weekly Consistency section in register issue #295. It does not authorize remediation or unrelated issue changes.

## Completion

After the review is complete and every unresolved finding has a durable issue identity, update only the `### Weekly Consistency review` section of register issue `#295`, preserving the rest of the issue body:

```text
### Weekly Consistency review

- last verified: <UTC YYYY-MM-DD>
- reviewed head: <full main SHA actually reviewed>
- accounted result: CLEAN | FINDINGS
- finding issues: none | #<number>, #<number>, ...
- next due / interval: every 7 days
- state: **CURRENT**
```

`CLEAN` requires `finding issues: none`. `FINDINGS` lists every unresolved finding applicable to the reviewed head.

That body update is the complete review-recording operation. Do not create a completion comment, trigger another workflow, invoke `gh`, or wait for a derived refresh. The scheduled continuous-improvement workflow may later age `CURRENT` to `DUE`/`OVERDUE` and refresh retrospective counters independently; that maintenance is not part of review completion.

Do not update the register if finding accounting is incomplete or required repository evidence was unavailable.

## Report to the user

Use a compact summary:

```text
Consistency review
Head: <sha>
Previous reviewed head: <sha | NONE>
Findings: none | #<number>, #<number>, ...
Coverage: <short description>
Limitations: none | <specific limitation>
Overall: CLEAN | FINDINGS | INCOMPLETE
```

Then present each material finding with its issue number, severity, category, evidence, authority analysis, and smallest coherent corrective action. `CLEAN` means no evidence-backed inconsistency was found during this review; it is never a claim that the repository has been exhaustively proven consistent.
