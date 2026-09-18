# Arcogine consistency review

This contract defines Arcogine's repository-wide semantic consistency review. It runs in a ChatGPT chat session with an Arcogine Repomix attachment as the repository-content baseline and the GitHub connector for revision reconciliation plus live/mutable repository state.

A formal review is diagnostic plus the narrow finding-ledger/register accounting described below. It does not authorize source/doc remediation, planning changes, ADR changes, pull-request creation, or merging. Ad-hoc consistency questions are read-only analyses and do not record completion.

## Goal

Deeply inspect the repository for semantic inconsistency across implementation, architecture, ADRs, planning, public/reference documentation, examples, configuration, tests, CI, and prior findings.

A previous clean review is not evidence that older content is correct. New material gets first attention when a previous reviewed head exists, but recency never bounds scope. Follow suspicious evidence wherever it leads.

## Required review corpus

A formal review requires an Arcogine Repomix attachment with valid provenance for canonical `alaiba/arcogine` `main`. The attachment need not be generated from the latest `main` if its recorded revision can be reconciled safely to one exact current target revision.

At review start:

1. Read the Repomix provenance header as baseline `S` and require:
   - `Repository: alaiba/arcogine`;
   - `Branch: main`;
   - a valid full `Commit` SHA.
   Missing or malformed provenance makes the review `INCOMPLETE`.
2. Resolve current live `main` through one GitHub compare from `S` to `main` using a compare surface that exposes the exact resolved target SHA `T` and the changed-path delta together.
3. Prove the changed-path set is complete before using delta mode. GitHub's Compare API exposes at most 300 changed files for one comparison, so a returned file list with **300 or more entries is ambiguous and must be treated as incomplete**. Also reject a missing file list or any explicit too-large/truncation signal. Only a present list with fewer than 300 entries and no incompleteness signal may establish a complete usable delta.
4. If the compare shows no repository-content difference, use the Repomix directly as the repository-content corpus for `T`.
5. If `S` is an ancestor of `T` and the compare provides a complete usable delta under that predicate:
   - keep the Repomix as the primary corpus for unaffected paths;
   - read every added, modified, renamed, copied, or otherwise affected target path from immutable `ref=T`;
   - treat deleted/replaced snapshot paths as unavailable at `T`;
   - never use snapshot content from an affected path as evidence about `T`;
   - for repository-wide or semantic searches, search the Repomix baseline and reconcile results with the affected-path set, inspecting affected content at `T` so additions/modifications are not missed and removed/replaced text cannot create false conclusions.
6. If ancestry, exact `T`, or a complete usable delta cannot be established — including an ambiguous 300-file Compare result — stop `INCOMPLETE` and require a refreshed snapshot. Do not attest a repository-wide review from a partial or ambiguous target corpus.
7. The resulting exact target view — snapshot content for unaffected paths plus revision-bound live content for affected paths — is the review corpus. If `AGENTS.md` or this contract is affected, read its `T` version before continuing and follow the target-revision instructions.
8. Use `T`, not the snapshot baseline `S`, as the reviewed head for all evidence, finding reconciliation, and completion recording.

Do not redundantly refetch unaffected static content through GitHub. The snapshot is a cache, not authority for changed target paths.

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

After the exact target corpus at `T` is established:

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

After that concept-driven work, every formal repository-wide review must run these independent breadth passes over the complete target corpus. These passes are candidate-discovery mechanisms, not automatic findings:

1. **Lifecycle/status prose sweep.** Search claim-bearing maintained prose — including architecture, planning, product/reference/development docs, README material, and durable source/test comments or Javadocs — for assertions about lifecycle or delivery state such as current/implemented/complete/partial, future/later/not-yet, ready-to, being-established, remaining, deferred, blocked, temporary, legacy, or equivalent wording. Reconcile suspicious matches with the authority that owns current status. Obvious lexical anomalies such as accidentally repeated adjacent words are candidate selectors too, but wording defects alone are not semantic findings.
2. **Volatile duplicated-fact sweep.** Search maintained prose for copied exact facts whose executable owner can move independently: dependency/tool/runtime versions, module/test/component counts, commands, configuration keys or assignments, workflow/job names, ports, limits, paths, and similarly change-prone literals. Locate the executable/configuration authority and verify the copied claim instead of assuming an exact value in prose is still current. Do not turn ordinary domain numbers into noise; focus on facts presented as current operational/configuration truth.
3. **Cross-authority current-state sweep.** For capabilities described as current, implemented, complete, partial, deferred, or blocked in architecture/planning authorities, search semantic neighbors across architecture, planning, product/reference/development docs, examples, and claim-bearing source comments for incompatible lifecycle state or ownership claims. This sweep must include older unchanged text; the changed-file range is not evidence that neighboring claims are current.
4. **Candidate closure check.** When a candidate exposes drift in a maintained current-state surface, inspect the smallest neighboring closure set governed by the same authority before finalizing it. Examples include sibling API examples/schema claims for the same surface, neighboring status claims for the same capability, or sibling comments carrying the same delivery assumption. Keep unrelated subjects separate, but do not stop at the first contradictory line when adjacent claims share the same authority.

A formal review may not record completion unless all four breadth passes were performed. If a required pass cannot be completed, stop `INCOMPLETE` before finding/register mutations and state which pass was not completed.

Prefer evidence-driven repository search over a duplicated architecture matrix. The required breadth passes define minimum discovery coverage; they do not require maintaining a static architecture matrix or treating every search hit as a finding. Newness is a search-order heuristic, not a stopping rule.

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

Do not duplicate mutable lifecycle state in the body. Finding reconciliation is idempotent: before creating a new issue, match the candidate against loaded open findings and, when needed, closed `CONS:` findings. A semantic match reuses the existing issue identity. Re-running a review must not create a duplicate issue or append duplicate evidence merely because the same inconsistency was observed again. If materially stronger or newly relevant diagnostic evidence clarifies the same unresolved finding, update that issue narrowly while preserving its identity. Unresolved findings stay open; plausible corrective PRs remain open and are reported `IN_FLIGHT`; verified fixes on reviewed `main` close completed; false positives/duplicates/superseded findings close with explanation; regressions reopen the same issue.

Invoking a formal review authorizes only the issue operations required to account for that review's findings and the final weekly-register update. It does not authorize remediation or unrelated issue changes.

## Completion

Do not mutate findings while analyzing.

1. Immediately before finding-accounting mutations, resolve live `main` again. It must still equal reviewed target `T`; otherwise stop `INCOMPLETE` with no review-accounting mutations and restart against the new exact target.
2. Reconcile finding issues idempotently.
3. Resolve live `main` again. It must still equal reviewed target `T`.
4. Re-fetch issue #295 immediately before writing, require the exact title, and replace only its `### Weekly Consistency review` subsection in the latest body while preserving all other content.

Write factual review state only:

```text
### Weekly Consistency review

- last verified: <UTC YYYY-MM-DD>
- reviewed head: <reviewed target T full SHA>
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
Coverage:
- recency/concept pass: COMPLETE | NOT_APPLICABLE | INCOMPLETE
- lifecycle/status prose sweep: COMPLETE | INCOMPLETE
- volatile duplicated-fact sweep: COMPLETE | INCOMPLETE
- cross-authority current-state sweep: COMPLETE | INCOMPLETE
- candidate closure checks: COMPLETE | INCOMPLETE
- open finding carry-forward: COMPLETE | NOT_APPLICABLE | INCOMPLETE
Limitations: none | <specific incomplete pass or other limitation>
Overall: CLEAN | FINDINGS | INCOMPLETE
```

Present each material finding with issue number, severity, category, evidence, authority analysis, and smallest coherent corrective action. `CLEAN` means no evidence-backed inconsistency was found during this review; it never claims exhaustive proof of consistency.
