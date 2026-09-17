# Arcogine consistency review

This contract defines Arcogine's repository-wide semantic consistency review. It runs in a ChatGPT chat session with a mandatory current-main Repomix attachment for repository content and the GitHub connector for live/mutable repository state.

A formal review is diagnostic plus the narrow finding-ledger/register accounting described below. It does not authorize source/doc remediation, planning changes, ADR changes, pull-request creation, or merging. Ad-hoc consistency questions are read-only analyses and do not record completion.

## Goal

Deeply inspect the repository for semantic inconsistency across implementation, architecture, ADRs, planning, public/reference documentation, examples, configuration, tests, CI, and prior findings.

A previous clean review is not evidence that older content is correct. New material gets first attention when a previous reviewed head exists, but recency never bounds scope. Follow suspicious evidence wherever it leads.

## Required review corpus

A formal review requires an Arcogine Repomix attachment generated from the exact current canonical `main` commit.

At review start:

1. Resolve live `main` through GitHub.
2. Read the Repomix provenance header and require:
   - `Repository: alaiba/arcogine`;
   - `Branch: main`;
   - a full `Commit` SHA exactly equal to live `main`.
3. If the attachment is missing or malformed, stop `INCOMPLETE`: generate/upload a current-main Repomix and retry.
4. If its commit differs from live `main`, stop `INCOMPLETE`: report both SHAs, say the Repomix is stale, and tell the user to update it from current `main` and retry.
5. After equality is established, use Repomix as the primary repository-content corpus. Read `AGENTS.md`, this contract, docs, source, tests, configuration, workflows, and other tracked repository content from it rather than refetching files through GitHub.

Do not compensate for a stale/missing corpus by reconstructing repository content through GitHub file/search calls. The prerequisite exists so the deep scan is fast, local, and complete at one known head.

GitHub remains authoritative for mutable state and history: live `main`, issue #295, finding issues, pull requests, reviews, CI/checks, commit/compare history, and all mutations.

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
| Persisted finding identity | GitHub issue number |

Consistency issues preserve finding identity/lifecycle continuity; they are not product, architecture, planning, or implementation authority.

Classify each material claim using this complete generic review taxonomy: `CURRENT`, `NORMATIVE_DECISION`, `PROPOSED`, `PLANNED`, `IMPLEMENTED_STATUS`, `PARTIAL`, `DEFERRED`, `BLOCKED`, `NON_GOAL`, `HISTORICAL`, or `COMPATIBILITY_DEBT`. Domain-owned lifecycles remain their own vocabulary. If this taxonomy proves insufficient, change this contract explicitly rather than inventing another generic state during a run.

Proposed/planned behavior differing from current source is not drift by itself; a current-state artifact presenting planned behavior as implemented is. Accepted ADRs preserve decision history and are superseded rather than rewritten to match later implementation.

## Live grounding

After Repomix freshness is proven:

1. Read GitHub issue `#295`, titled exactly `Continuous improvement register`.
2. Load currently open consistency findings whose titles begin `CONS:`.
3. If #295 records a resolvable previous reviewed head, compare it with current `main` and use changed/new material as the first attention priority.

Issue #295 is mandatory. If it is missing, inaccessible, has the wrong title, or its weekly Consistency section is malformed, stop `INCOMPLETE`; do not recreate, replace, or guess it.

Closed findings are not preloaded. Search closed `CONS:` issues only when a candidate finding needs duplicate/regression matching.

## Review strategy

Search and slice the Repomix corpus aggressively. For each material concept investigated:

1. Identify the claim and lifecycle state.
2. Search for the concept, symbols, terminology, and nearby assumptions across the corpus.
3. Read the authoritative current/planning/ADR/test/interface surfaces that encode the same semantics.
4. Inspect source/config/tests as executable evidence; inspect live GitHub CI/check evidence only when it materially proves or contradicts a claim.
5. Use PR/commit history only when needed to explain a transition, attribute evidence, or determine whether a finding is in flight.
6. Compare semantic neighbors and decide which authority, if any, is wrong.
7. If something appears even mildly inconsistent, follow the thread far enough to classify it regardless of file age or the previous reviewed head.

Prefer evidence-driven repository search over a duplicated architecture matrix. Newness is a search-order heuristic, not a stopping rule.

Carry every open issue-backed finding forward on every review. A merged PR, closed issue, review comment, or green CI result is not proof of resolution; only authoritative evidence on the reviewed `main` head establishes that a finding is fixed.

## Findings

A material finding requires both the artifact making the claim and contradictory authoritative/executable evidence, or a clear demonstration that required evidence is absent.

Use exact paths, symbols, criteria, ADRs, tests, PRs, commits, and issue numbers where available. Use confidence `HIGH`, `MEDIUM`, or `LOW`; do not inflate confidence because CI is green. Use the current P0/P1/P2/P3/Nit severity definitions from `docs/development/reviewing.md`.

Useful categories are `PUBLIC_DOC_DRIFT`, `ARCHITECTURE_DRIFT`, `ARCHITECTURE_STALENESS`, `PLANNING_STATUS_DRIFT`, `ASPIRATIONAL_LEAKAGE`, `ADR_CONFLICT`, `EXECUTABLE_EVIDENCE_DRIFT`, `INTERFACE_DRIFT`, `DEPENDENCY_BOUNDARY_DRIFT`, `TERMINOLOGY_IDENTITY_DRIFT`, `TOOLCHAIN_CI_DRIFT`, `LINK_PATH_DRIFT`, `STANDARD_PROVENANCE_DRIFT`, `DUPLICATED_AUTHORITY`, and `PR_INCOMPLETE_RECONCILIATION`.

Do not report inconsistency solely because wording differs, a proposal differs from implementation, an intentional compatibility alias exists, an internal symbol is undocumented, or an open PR contains behavior not yet on `main`.

## GitHub finding ledger

The GitHub issue number is the sole durable finding identity. All findings use:

```text
CONS: <concise semantic title>
```

A new finding body needs durable diagnostic evidence only:

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

Do not duplicate mutable lifecycle state in the body. Unresolved findings stay open; plausible corrective PRs remain open and are reported `IN_FLIGHT`; verified fixes on reviewed `main` close completed; false positives/duplicates/superseded findings close with explanation; regressions reopen the same issue.

Invoking a formal review authorizes only the issue operations required to account for that review's findings and the final weekly-register update. It does not authorize remediation or unrelated issue changes.

## Completion

Do not mutate findings while analyzing.

1. Immediately before finding-accounting mutations, resolve live `main` again. It must still equal the Repomix commit; otherwise stop `INCOMPLETE` with no review-accounting mutations and require a fresh Repomix/retry.
2. Reconcile finding issues.
3. Resolve live `main` again. It must still equal the Repomix commit.
4. Re-fetch issue #295 immediately before writing, require the exact title, and replace only its `### Weekly Consistency review` subsection in the latest body while preserving all other content.

Write factual review state only:

```text
### Weekly Consistency review

- last verified: <UTC YYYY-MM-DD>
- reviewed head: <Repomix/current-main full SHA>
- accounted result: CLEAN | FINDINGS
- finding issues: none | #<number>, #<number>, ...
- interval: every 7 days
```

`CLEAN` requires `finding issues: none`; `FINDINGS` lists every unresolved finding applicable to the reviewed head. The body edit is the complete recording operation. Do not create a completion comment, trigger a workflow, invoke `gh`, or maintain a second completion ledger.

The register stores facts, not derived due-state cache. Weekly `CURRENT`/`DUE`/`OVERDUE` is derived from `last verified` when an agent grounds; see `AGENTS.md` and `docs/development/continuous-improvement.md`.

## Report

```text
Consistency review
Head: <sha>
Previous reviewed head: <sha | NONE>
Findings: none | #<number>, #<number>, ...
Coverage: <short description>
Limitations: none | <specific limitation>
Overall: CLEAN | FINDINGS | INCOMPLETE
```

Present each material finding with issue number, severity, category, evidence, authority analysis, and smallest coherent corrective action. `CLEAN` means no evidence-backed inconsistency was found during this review; it never claims exhaustive proof of consistency.
