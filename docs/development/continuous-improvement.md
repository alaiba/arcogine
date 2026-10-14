# Continuous-improvement operating model

> **Status:** canonical operating model for when Arcogine's improvement loops run, what each loop records, and where its state lives.

Arcogine has three distinct improvement loops. None substitutes for another.

## Continuous Improvement assessment

The repository's user-invocable [Continuous Improvement agent](agent-roles/continuous-improvement.md) is a light advisory checkpoint over recent delivery activity. It is not a fourth improvement loop.

Invoke it with exactly:

`Assess continuous improvement.`

The assessment is intentionally light and broad. It re-grounds against current repository and live GitHub evidence, scans activity since the preceding checkpoint opened, and recommends the smallest worthwhile next practice or diagnostic instead of performing deep analysis itself. The scan centers on pull requests merged into `main`, non-PR issues closed in that window, and current open work needed only to avoid duplicating ownership.

A repository workflow, [`continuous-improvement-reminder.yml`](../../.github/workflows/continuous-improvement-reminder.yml), provides the only standing reminder. Once per week it creates an issue titled exactly `Continuous improvement checkpoint` only when no open issue with that title exists. The workflow performs no health analysis and derives no due state; the issue simply prompts an explicit Continuous Improvement assessment. When an open checkpoint triggers the assessment, the previous checkpoint's opening time is the normal lower bound; otherwise the latest checkpoint's opening time is used. Closing the reminder acknowledges it, after which a later scheduled run may create another.

The assessment may recommend Consistency, a delivery-process retrospective, a focused test-evidence diagnostic, or a narrow process-tightening action. It does not substitute for those deeper practices, and every actionable recommendation includes an exact copy/paste fresh-session invocation. It persists no due state, completion timestamps, thresholds, intervention register, or diagnostic score.

### Checkpoint outcome publication

The existing GitHub issue titled exactly `Continuous improvement checkpoint` is the **informational index** for completed Continuous Improvement assessments, formal Consistency reviews, and delivery-process retrospectives. Each practice publishes its own result, not another practice's result. These comments are not a due-state ledger, a substitute for `CONS:` findings or retrospective Git history, or evidence that another practice must run.

Resolve the reporting destination before publication: prefer an explicitly associated checkpoint issue number carried in the invocation/handoff, validate its exact title, and allow it to be closed. Without an explicit number, use exactly one open issue with that title. If no unambiguous checkpoint exists, report that publication had no destination and **do not create or reopen an issue**. An assessment handing off a specialist invocation must include its checkpoint number so that the specialist cannot accidentally post on a later cycle.

Use `infra/dev/checkpoint-comment.mjs` from a repository checkout with authenticated `gh` when available, or an equivalent GitHub issue-comment API operation preserving its marker and deduplication semantics. The helper accepts `--checkpoint <number>`, `--practice assessment|consistency|retrospective`, `--run-id <stable-key>`, and `--body-file <path>` (plus optional `--repo`). Complete, nonempty results are posted once per checkpoint/practice/run; a repeat is unchanged or updates the same author's comment. A publication failure must be disclosed, never reported as success. Use the reviewed `main` SHA for assessment and Consistency keys, and the report PR number plus distinct proposed/merged phase for retrospectives. Follow the GitHub attribution rules in `AGENTS.md`.

Session-close Kaizen remains session-local. PR review and research investigations do not create checkpoint comments merely by completing their normal work. Only an explicitly invoked formal practice above reports here; the reminder workflow still only creates checkpoint issues.

## Session-close Kaizen

- **Trigger:** `.?` at the close of a meaningful coding-agent session; see `AGENTS.md`.
- **Purpose:** preserve durable lessons as executable safeguards, standard work, or maintained knowledge.
- **Owner:** the coding agent.
- **Cadence:** event-driven only; no recurring due state.

At `.?`, inspect the current session and live repository for anything learned, decided, repeated, or
encountered that should survive deletion of the conversation. Classify each material candidate as one
of:

- **Already encoded** — the repository already captures the lesson or invariant adequately; make no
  duplicate change.
- **Bake in** — the lesson is durable and generally reusable; identify the narrowest authoritative
  repository surface that should encode it.
- **Follow-up** — the improvement is worthwhile but belongs in separate work rather than being
  smuggled into the current PR or slice.
- **Discard** — the observation is situational, transient, or otherwise not worth preserving.

Prefer the strongest durable capture that actually fits the lesson. Use an executable guard/test only
when the invariant is mechanically observable and the guard exercises behavior or repository state
rather than merely asserting that instruction prose still contains particular wording. Otherwise
prefer canonical helper/tooling, agent/contributor standard work, maintained documentation, then
canonical architecture or specification for genuinely architectural or hard-to-reverse constraints.
Generalize incidents into semantic rules rather than preserving session or PR coordinates as durable
concepts. Prefer improving an existing authoritative artifact over creating a new one.

Do not manufacture a lesson merely to produce an output. Default output reports material findings and
actions plus the deletion verdict; it does not narrate adjacent practices that the session gives no
concrete reason to invoke — for example "Consistency review not warranted" or "retrospective not
assessed" are not useful by default (see [Invocation boundaries](#invocation-boundaries)). Finish
every review with an explicit deletion verdict: either the session is safe to delete because nothing
unique remains, or name exactly what still needs to be captured first.

## Consistency review

- **Recommended cadence:** roughly weekly, and after significant architecture/status transitions when useful.
- **Purpose:** deep repository semantic review across implementation, architecture/specifications, planning, docs, examples, config, tests, CI, and prior findings.
- **Owner/runtime:** the Consistency reviewer using the canonical repository-snapshot protocol plus live GitHub state.
- **Actionable durable output:** only evidence-backed `CONS:` finding issues that require resolution; the optional checkpoint result comment is informational.

The cadence is guidance, not persisted scheduler state. Arcogine does not maintain a last-reviewed timestamp, previous-reviewed-head ledger, or `CURRENT`/`DUE`/`OVERDUE` state for Consistency.

A clean review creates no finding issue, but a completed formal review posts its `CLEAN` or `FINDINGS` summary to the associated checkpoint as described above. A review with findings creates, updates, reopens, or closes the applicable `CONS:` issues. Only the findings affect future work; the comment records the review outcome without creating completion state.

Recent Git history may be used as a search-order heuristic, but no previous-review coordinate bounds review scope or is required for correctness.

## Delivery-process retrospective

### Invocation

The retrospective runs only when explicitly requested. Arcogine keeps no standing retrospective cadence, threshold, due state, or automatic reminder, and Session-close Kaizen does not evaluate whether a retrospective is due.

Versioned baseline state for an explicitly invoked retrospective lives in:

`.github/continuous-improvement/retrospective.json`

### Purpose

Determine whether Arcogine's delivery controls are reducing recurring waste and escapes, decide which prior changes should be retained/retired/superseded, and create owned follow-up work only where the evidence justifies it.

The retrospective is **not** a process-history dump, an issue ledger, or a second planning system.

### Mechanical evidence first

Before interpretation:

1. choose one exact main-target `throughPr` that bounds the run;
2. acquire and save a complete Retrospective Evidence v1 bundle:

   ```bash
   node infra/dev/delivery-retrospective-github.mjs \
     --through-pr <number> \
     --output logs/retrospective-evidence.json
   ```

3. run the source-neutral analyzer against that saved bundle:

   ```bash
   node infra/dev/delivery-retrospective.mjs \
     --input logs/retrospective-evidence.json \
     --json
   ```

4. require acquisition, evidence-contract validation, and analysis to complete; then use the analyzer's exact PR window, merged-PR count, trusted-author CHANGES REQUIRED disposition count, review-checkpoint distribution, and structured-finding dataset without manually reconstructing or retyping alternative totals.

The GitHub GraphQL adapter is the current local acquisition path. Another approved acquisition path must emit the same source-neutral contract and can then use the same analyzer unchanged. The executable contract and completeness rules live in `infra/dev/delivery-retrospective-evidence.mjs`; the local GraphQL retrieval rules live in `infra/dev/delivery-retrospective-github.mjs`; the ChatGPT connector procedure below owns connector-specific retrieval; all retrospective counting and finding analysis live in the pure `infra/dev/delivery-retrospective.mjs` analyzer.

#### ChatGPT GitHub connector acquisition

A formal retrospective running in ChatGPT may use the GitHub connector as an approved acquisition path only by producing the same Retrospective Evidence v1 contract and then invoking the exact-current pure analyzer. The connector path does not hand-count dispositions or findings.

Use this fail-closed procedure:

1. Read `.github/continuous-improvement/retrospective.json`, `infra/dev/delivery-retrospective-evidence.mjs`, and `infra/dev/delivery-retrospective.mjs` from one exact current `main` SHA. Resolve the requested `throughPr` from live GitHub.
2. Fetch baseline and through PR metadata and require both to be merged into `main` with valid merge timestamps.
3. Enumerate merged `main` PR candidates one UTC merge day at a time from the baseline merge day through the through-PR merge day, using a repository-scoped PR search capped at 100 results per day. If any daily query returns 100 results, the connector cannot prove that day complete and the retrospective is `INCOMPLETE`. De-duplicate PR numbers, fetch authoritative PR metadata for every candidate, then retain only the exact `(baseline mergedAt, through mergedAt]` interval.
4. For every exact-window PR, retrieve submitted reviews from GitHub's raw REST review subresource through the connector's approved raw-fetch surface: `/repos/<owner>/<repo>/pulls/<number>/reviews?per_page=100&page=<n>`. Fetch pages in order until one returns fewer than 100 reviews. Reject a repeated page, duplicate review identity, fetch failure, malformed payload, or any other condition that prevents proving complete enumeration; do not substitute the connector's normalized review-list projection when the raw subresource is available.
5. Populate Evidence v1 review fields from the authoritative raw review payload: use `node_id` as the review identity when present, `body` as the body, `author_association` as `authorAssociation`, `submitted_at` as `submittedAt`, and `commit_id` as `reviewedHead`. Preserve these values rather than replacing them with weaker normalized projections; in particular, the analyzer's finding-head consistency check depends on `reviewedHead`.
6. Only when the active connector surface genuinely cannot expose authoritative review association or reviewed-commit provenance may acquisition fall back to weaker fields, and review enumeration must still be proven complete independently. In that fallback only, Arcogine may synthesize `OWNER` when the authenticated GitHub login equals the repository owner login and the review author login equals that same verified login, and may set `reviewedHead` to `null` when the reviewed commit is genuinely unavailable. Do not infer `MEMBER` or `COLLABORATOR` from repository permission levels. Any other review author without a direct GitHub association makes acquisition `INCOMPLETE`.
7. Set source provenance to identify the connector acquisition path (for example, `adapter: "github-connector"`) and include enough non-semantic retrieval metadata to diagnose its completeness proof. Validate the completed bundle against the exact-current Evidence v1 validator.
8. Materialize the exact-current `delivery-retrospective-evidence.mjs` and `delivery-retrospective.mjs` into the ChatGPT execution environment together with the validated evidence JSON, preserving their relative import relationship, and run the analyzer with Node. If the runtime cannot execute the analyzer, the retrospective is `INCOMPLETE`; do not reproduce its aggregation rules in model reasoning.

The connector acquisition path may make different GitHub calls from the local GraphQL adapter, but after Evidence v1 validation the downstream analyzer and interpretation boundary are identical.

The adapter fails closed when GitHub search/review retrieval cannot prove completeness. The analyzer fails closed on unsupported or internally incomplete evidence. If either fails, the retrospective is `INCOMPLETE`; fix the retrieval/tooling problem rather than estimating the sample. Structurally complete evidence can still contain malformed or noncanonical historical reviewer findings. In that case the analyzer preserves the core window/review metrics, marks finding analytics incomplete, and reports the specific coverage diagnostics; do not treat any incomplete distribution as a complete sample.

Manual analysis begins only after the mechanical window is established. It should focus on:

- whether the previous retrospective's retained controls actually prevented recurrence;
- high-confidence post-merge process/lifecycle escapes;
- repeated waste that can be tied to a concrete mechanism;
- whether an existing experiment/change should be retained, retired, or superseded.

Do not optimize for finding count. Healthy adversarial review findings are not waste merely because they are numerous. Hand-classified finding totals or category percentages remain noncanonical. The analyzer may report mechanically parsed PR Reviewer fields — including category, confidence, distinct findings, and per-PR incidence — when its structured-finding coverage is complete. Historical review bodies may still contain a legacy `Severity:` field; the analyzer preserves that value on the parsed historical finding when present, but severity is not part of current finding identity or aggregate analytics. When structured-finding coverage is incomplete, use the diagnostics and do not present the observed values as complete distributions. Interpretation and improvement decisions remain retrospective reasoning, not analyzer output.

### Action ownership

There is no continuous-improvement intervention register.

Every retrospective conclusion must resolve to one of these forms:

- **retain** — existing control is working; no work item;
- **retire/supersede** — stop carrying the old experiment/control as active work;
- **action** — concrete work is required and must be owned by an existing or newly created GitHub issue before the retrospective is complete;
- **verify next time** — an already-landed change needs later measurement only; record the measure in the dated report, not as an unowned intervention.

If nobody is expected to change anything before the next measurement, it is not an active intervention.

### Dated report

Dated retrospective evidence lives under:

`docs/history/continuous-improvement/`

The normative method lives here; dated reports do not restate it. Keep each report concise (normally no more than about 80 lines) and use this structure:

```text
# Delivery-process retrospective — <date>

Scope: <baseline PR exclusive> -> <through PR inclusive>

## Result
<3-5 decision-oriented bullets>

## Mechanical metrics
<small comparison table using helper output>

## Prior changes
| Change | Verdict | Evidence | Action |

## Actions
- #<issue> — <required outcome>
or
- none

## Next baseline
- baseline PR
- specific measures to verify next time
```

Detailed review bodies, PR histories, issue evidence, and calculations remain in their owning GitHub/repository sources; do not duplicate them into the report unless necessary to support a decision.

### Completion

A retrospective is complete only when one reviewed PR:

1. adds the dated report under `docs/history/continuous-improvement/`;
2. advances `.github/continuous-improvement/retrospective.json` to the exact `throughPr`;
3. names every concrete follow-up issue in the report;
4. carries no unowned intervention/trial state.

The versioned state file records only the retrospective baseline and latest report path. It is not a trigger, due-state, reminder, or intervention register.

### Retrospective checkpoint publication

Carry the explicit checkpoint issue number from the Continuous Improvement handoff when one exists; otherwise resolve the single open checkpoint before opening the retrospective PR. Do not create a checkpoint for a standalone retrospective that has no destination. The retrospective PR body must contain a standalone stable `Retrospective checkpoint: #<number>` line **when an associated checkpoint exists**. This is a durable reference, not a claim about mutable PR or CI state.

After creating the report PR, publish a short **proposed** result comment to that issue: the exact baseline and through-PR window, principal provisional decisions, follow-up issues, and report PR link. Use practice `retrospective`, run ID `pr-<report-PR-number>-proposed`. The PR and report are not yet authoritative; do not call the retrospective complete.

After the reviewed report PR merges, `.github/workflows/retrospective-checkpoint.yml` checks that the merged PR changed the baseline and the report, reads the **exact merged commit**, and publishes a separate **complete** comment with the report's Result and Actions sections and an immutable report link. Its run ID is `pr-<report-PR-number>-merged`. The separate comments are intentional: GitHub Actions cannot ordinarily edit a human-authored provisional comment. Both phases are idempotent; a missing checkpoint reference means no destination and does not create one. Workflow failures must be investigated or rerun, not silently treated as publication.

Git remains the authoritative retrospective record. A checkpoint comment never advances `retrospective.json` or replaces the reviewed report.

## Authority boundaries

| Concern | Owning authority |
| --- | --- |
| Session-close Kaizen | `AGENTS.md` |
| Continuous Improvement assessment | `docs/development/agent-roles/continuous-improvement.md` |
| Assessment reminder delivery | `.github/workflows/continuous-improvement-reminder.yml` |
| Consistency review algorithm | `docs/development/agent-roles/consistency.md` |
| Consistency corpus generation | `infra/dev/repo-snapshot.mjs` + `infra/dev/repomix.config.json` |
| Consistency operating guidance | `docs/development/consistency-review.md` |
| Consistency finding identity/state | GitHub `CONS:` issues |
| Retrospective method | this document |
| Retrospective baseline state | `.github/continuous-improvement/retrospective.json` |
| Retrospective evidence contract | `infra/dev/delivery-retrospective-evidence.mjs` |
| Retrospective local GitHub acquisition | `infra/dev/delivery-retrospective-github.mjs` |
| Retrospective ChatGPT connector acquisition | this document + `infra/dev/delivery-retrospective-evidence.mjs` |
| Retrospective mechanical analysis | `infra/dev/delivery-retrospective.mjs` |
| Dated retrospective evidence | `docs/history/continuous-improvement/` |
| Retrospective actions | their owning GitHub issues |
| Raw delivery evidence | GitHub PR/review/CI/issue history |

PR review remains governed by `docs/development/reviewing.md`; it is evidence for retrospectives, not a fourth improvement ceremony.

## Invocation boundaries

Session-close Kaizen has no standing retrospective reminder. Consistency likewise has no standing due-state mechanism; recommend a fresh Consistency review from Kaizen only when evidence from the current session itself makes a repository-wide sweep materially useful.

Ordinary repository grounding and `.?` do not assess global improvement health. The Continuous Improvement agent is the explicit on-demand boundary for that question and may recommend a specialized practice without performing it. Delivery-process retrospective execution remains explicit and uses the versioned baseline plus the mechanical method above.

The scheduled checkpoint workflow is only an alarm clock for invoking that assessment. It does not establish that any practice is due.

## Non-goals

This system does not automatically execute a review/retrospective, infer practice due state, maintain retrospective due state, maintain a Consistency completion ledger, maintain an intervention database, turn historical reports into normative guidance, create work without issue ownership, or use GitHub as a slow substitute for repository content already established through the snapshot protocol.
