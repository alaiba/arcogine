# Arcogine consistency review

This contract defines Arcogine's repository consistency review. It is intentionally built for a ChatGPT chat session using the GitHub connector against `alaiba/arcogine`.

Do not require a local checkout, `git`, `gh`, shell commands, or local execution of repository scripts. Read repository state, files, issues, pull requests, commits, reviews, and CI evidence through the GitHub connector. A missing local command is never by itself a review limitation.

A formal Consistency review is diagnostic plus the narrow finding-ledger accounting described below. It does not authorize source/doc remediation, planning changes, ADR changes, pull-request creation, or merging. Ad-hoc consistency questions in chat are read-only analyses; they are not formal recurring reviews and do not record completion.

## Goal

Determine whether the repository tells a coherent, temporally honest, evidence-backed story across implementation, architecture, ADRs, planning, public/reference documentation, examples, configuration, tests, CI, and prior consistency findings.

Do not force artifacts to use identical wording. Compare semantic claims about the same subject, scope, lifecycle state, and point in time.

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

Classify claims before comparing them. Distinguish at least `CURRENT`, `NORMATIVE_DECISION`, `PROPOSED`, `PLANNED`, `IMPLEMENTED_STATUS`, `PARTIAL`, `DEFERRED`, `BLOCKED`, `NON_GOAL`, `HISTORICAL`, and `COMPATIBILITY_DEBT`. Proposed/planned behavior differing from current source is not drift by itself; a current-state artifact presenting planned behavior as implemented is.

Accepted ADRs preserve decision history. Do not call historical wording/path context stale merely because implementation later moved. A changed architectural decision requires the repository's ADR supersession process, not retrospective rewriting of history.

## Start of every formal review

Re-ground from live repository state rather than conversation memory or uploaded snapshots:

1. Resolve the current `main` SHA and read this file from that head.
2. Read `AGENTS.md`.
3. Read the continuous-improvement register, fixed at GitHub issue `#295`.
4. Load open and closed consistency findings: issues whose titles begin `CONS:` (current form) or `CONS-` (historical form).
5. Determine scope automatically from the register:
   - no accounted reviewed head -> `FULL`;
   - accounted reviewed head present -> `INCREMENTAL` from that head to current `main`.
6. For `INCREMENTAL`, compare baseline to head and reconstruct the material semantic changes in that interval. For `FULL`, inspect the maintained documentation/architecture/planning families plus representative executable evidence broadly enough to establish a repository-wide baseline.

If the registered baseline cannot be resolved, fall back to `FULL`. The user does not need to choose a mode.

If required repository state or the finding ledger cannot be read, report `INCOMPLETE` and do not record completion.

## Review method

For each material changed or reviewed concept:

1. Identify the claim and its lifecycle state.
2. Search the repository for the concept, important symbols, and terminology.
3. Read the authoritative current/planning/ADR/test/interface surfaces returned by that search.
4. Inspect source/config/tests or existing CI evidence when they materially prove or contradict the claim.
5. Inspect recent/open pull requests only when history is needed to understand the transition or determine whether an existing finding is in flight.
6. Compare semantic neighbors and decide which authority, if any, is wrong.

Prefer repository search over a permanently duplicated neighbor matrix. Examples: a `FactoryModel` semantic change should lead to factory architecture/ADRs/planning and Engine assumptions; an API/DTO change should lead to reference docs and consumers; a CI/toolchain change should lead to testing/contribution policy. Expand from evidence rather than treating these examples as an exhaustive graph.

Do not execute repository checks locally. Existing workflow/check results may be inspected through GitHub, but green CI or the existence of a test/class is not by itself proof that an acceptance criterion is satisfied. Read the actual evidence.

Carry every unresolved issue-backed finding forward even if it predates the incremental baseline. Search relevant closed findings when current evidence resembles their semantic subject so regressions reuse the same GitHub issue rather than creating duplicates.

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

The GitHub issue number is the sole canonical durable identity for a finding.

Historical `CONS-001` through `CONS-006` and later numeric `CONS-*` titles remain valid history and must not be renamed merely to adopt this contract. New findings use the simpler title form:

```text
CONS: <concise semantic title>
```

Before creating a finding, search all open and closed current/historical consistency issues and match by semantic subject and evidence. Reuse the existing issue for the same inconsistency, including a regression.

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

Invoking a formal `FULL` or `INCREMENTAL` review authorizes only the issue creation/update/reopen/close/comment operations required to account for that review's consistency findings. It does not authorize remediation or unrelated issue changes.

## Completion

After the review is complete and every unresolved finding has a durable issue identity, post exactly one completion comment to register issue `#295`:

```text
Consistency review completed
head: <full main SHA actually reviewed>
scope: FULL | INCREMENTAL
findings: none | #<number>, #<number>, ...
```

`findings: none` means the reviewed scope is clean. Otherwise list every unresolved finding applicable to the reviewed head. GitHub supplies the trusted commenter identity and comment timestamp; do not duplicate either in the body.

Posting this comment is the end of the review's completion protocol. The register workflow reacts to the comment and refreshes its derived state. Do not require `repository_dispatch`, `workflow_dispatch`, `gh`, another command, or synchronous verification that the derived register body has already refreshed.

Do not post completion if finding accounting is incomplete or required repository evidence was unavailable.

## Report to the user

Use a compact summary:

```text
Consistency review
Head: <sha>
Baseline: <sha | NONE>
Scope: FULL | INCREMENTAL
Findings: none | #<number>, #<number>, ...
Coverage: <short description>
Limitations: none | <specific limitation>
Overall: CLEAN | FINDINGS | INCOMPLETE
```

Then present each material finding with its issue number, severity, category, evidence, authority analysis, and smallest coherent corrective action. `CLEAN` means the inspected scope produced no evidence-backed inconsistency; it is not a mathematical proof that no inconsistency exists anywhere.