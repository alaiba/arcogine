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

Use `infra/dev/checkpoint-comment.mjs` from a repository checkout with authenticated `gh` when available, or an equivalent GitHub issue-comment API operation preserving its marker and deduplication semantics. The helper accepts `--checkpoint <number>`, `--practice assessment|consistency|retrospective`, `--run-id <stable-key>`, and `--body-file <path>` (plus optional `--repo`). Complete, nonempty results are posted once per checkpoint/practice/run; a repeat is unchanged or updates the same author's comment. The publisher resolves the authenticated GitHub viewer (including the GitHub Actions bot) and verifies both the author's account ID and login before accepting or updating a matching comment. A matching marker owned by another author, or missing author metadata, is an ownership collision: publication fails without writing or accepting that result. Resolve the collision explicitly before retrying; never overwrite the foreign comment. Failure to verify the publisher also prevents publication. A publication failure must be disclosed, never reported as success. Use the reviewed `main` SHA for assessment and Consistency keys, and the report PR number plus distinct proposed/merged phase for retrospectives. Follow the GitHub attribution rules in `AGENTS.md`.

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
- **Purpose:** deep repository semantic review across implementation, architecture/specifications, research state, docs, examples, config, tests, CI, and prior findings.
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

Run the formal retrospective in a coding-agent repository checkout using the repository-owned Node tooling and authenticated GitHub access. Keep the versioned retrospective baseline, acquisition adapter, evidence validator, and analyzer on the same exact current `main` revision. Authenticate through `GH_TOKEN`, `GITHUB_TOKEN`, or an authenticated `gh` CLI, as supported by the adapter. Do not substitute hand-reconstructed PR/review counts or a chat/connector-side acquisition run if this environment is unavailable.

Before interpretation:

1. choose one exact main-target `throughPr` that bounds the run;
2. acquire and save a complete Retrospective Evidence bundle:

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

4. require acquisition, evidence-contract validation, and analysis to complete; then use the analyzer's exact PR window, merged-PR count, trusted-author CHANGES REQUIRED disposition count, review-checkpoint distribution, structured-finding dataset, and research-to-delivery lead time without manually reconstructing or retyping alternative totals.

The **supported Retrospective Evidence acquisition path** is `infra/dev/delivery-retrospective-github.mjs`, which fetches evidence through authenticated GitHub GraphQL and validates the complete Retrospective Evidence bundle before writing it. The adapter proves merged `main` baseline/through endpoints, complete candidate search pagination, exact merge-time window membership, complete submitted-review payloads preserving GitHub-provided author association and reviewed-head fields, each named handoff's commit, file and commit date as GitHub records them, and the issues open during the window, each with the pull request GitHub records as its closer, taken from one complete listing of the repository's issues regardless of state, so an issue that closes or reopens during acquisition is not lost. It fails closed on GitHub search limits, truncated review connections, a malformed, repeated or unconfirmed handoff line, an issue listing whose size changes during pagination, or unverifiable evidence. Do not replace it with manual issue/PR enumeration, connector-only acquisition outside the repository scripts, inferred review associations, or hand-built evidence JSON.

The **evidence contract remains source-neutral** in `infra/dev/delivery-retrospective-evidence.mjs`, and the analyzer in `infra/dev/delivery-retrospective.mjs` remains pure and source-independent. This does not authorize an alternative acquisition path: any new adapter must be introduced by a separately reviewed repository change that preserves the same completeness, provenance, and validation guarantees. The existing local GraphQL adapter is the only supported Retrospective Evidence acquisition entrypoint; research branch inventory is a separate supporting evidence pass over the same saved bundle.

The adapter fails closed when GitHub search/review retrieval cannot prove completeness. The analyzer fails closed on unsupported or internally incomplete evidence. If either fails, the retrospective is `INCOMPLETE`; fix the retrieval/tooling problem rather than estimating the sample. Structurally complete evidence can still contain malformed or noncanonical historical reviewer findings. In that case the analyzer preserves the core window/review metrics, marks finding analytics incomplete, and reports the specific coverage diagnostics; do not treat any incomplete distribution as a complete sample.

Manual analysis begins only after the mechanical window is established. It should focus on:

- whether the previous retrospective's retained controls actually prevented recurrence;
- high-confidence post-merge process/lifecycle escapes;
- repeated waste that can be tied to a concrete mechanism;
- whether an existing experiment/change should be retained, retired, or superseded.

**Control-effectiveness exposure.** For each prior control marked `verify next time`, identify its landing PR and merge time, the failure mechanism it was intended to prevent, and the later changes actually exposed to that mechanism after the control became applicable. Use those eligible changes and any attributable escapes or avoided rework to assess effectiveness; earlier failures explain why the control was adopted, not whether it worked. If eligible work, follow-up time, or trustworthy evidence is insufficient, retain `verify next time` and label the control **not yet verified** rather than claiming success or failure. Compare windows only where the sample and definitions permit it, and do not infer causation from lower review-churn counts alone. No exposure quota or success target is imposed.

**Post-merge escape mechanisms.** For every material attributable escape, record the origin PR and violated contract, whether the contradiction was in the origin diff or an omitted semantic neighbor, the detection source and date, the delay from merge to detection where available, and the existing corrective owner. Distinguish preventable semantic propagation misses, premature implementation premises, and branch-integration/stacking failures from valid research falsification or deliberate replacement. Keep **origin window**, **discovery date**, and the evidence observation cutoff distinct: a later-discovered escape from a window PR may inform that origin window but its detection is subsequent context; an escape whose origin predates this window is not a new in-window-origin escape. State whether detection lag and differing observation periods limit trend comparisons. These classifications are evidence-backed interpretation, not new canonical analyzer metrics.

Do not optimize for finding count. Healthy adversarial review findings are not waste merely because they are numerous. Hand-classified finding totals or category percentages remain noncanonical. The analyzer may report mechanically parsed PR Reviewer fields — including category, confidence, distinct findings, and per-PR incidence — when its structured-finding coverage is complete. Historical review bodies may still contain a legacy `Severity:` field; the analyzer preserves that value on the parsed historical finding when present, but severity is not part of current finding identity or aggregate analytics. When structured-finding coverage is incomplete, use the diagnostics and do not present the observed values as complete distributions. Interpretation and improvement decisions remain retrospective reasoning, not analyzer output.

### Research delivery and temporary-evidence value

Every formal retrospective includes a **research-delivery analysis** within the same review, not a separate practice, delivery ledger, or second canonical metric dataset. The pure analyzer still covers merged `main` PR metadata, submitted reviews, and issue opening and closing times, **not source diffs or historical branch content**. Research evidence therefore needs a separate, explicit branch pass. Both passes share **one analytical window**: from the baseline PR's merge timestamp (exclusive) through the selected through PR's merge timestamp (inclusive), as recorded in the saved evidence bundle. The retrospective's mechanically exact PR sample in that window remains authoritative for delivery metrics; the branch pass is qualitative evidence, not another denominator. The associated `Continuous improvement checkpoint` only receives the retrospective's result (see [Retrospective checkpoint publication](#retrospective-checkpoint-publication)); its opening time bounds no evidence.

**Inventory every relevant branch before selecting research cases.** Run the repository's authenticated helper against the same saved bundle the analyzer consumes:

```bash
node infra/dev/delivery-retrospective-branches.mjs \
  --evidence logs/retrospective-evidence.json \
  --output logs/retrospective-branches.json
```

The helper validates the bundle against the evidence contract, requires its repository and baseline PR to match the versioned retrospective state (pass the same `--baseline-pr` used for acquisition if that was overridden), and records the window it took from the bundle. It enumerates **all live repository branches**, whatever their age or PR association, and every discoverable same-repository PR-head branch identity whose PR lifecycle—creation to closure, or still open—overlaps the window, including PRs opened before the baseline merge that were still open after it. This includes open, closed, and merged PRs, plus branches whose remote ref has since disappeared; GitHub's current branch list alone is insufficient. Each linked PR records its target branch, is labeled `before`, `overlaps`, or `after` the window, and states whether it merged inside the window into any target (`mergedInWindow`) and whether it belongs to the window's merged-`main` PR sample (`inMainPrSample`). The helper fails closed on invalid or mismatched evidence, malformed PR facts, duplicate records, incomplete pagination, or an all-state PR listing that disagrees with the bundle's merged PRs, including a `main` merge inside the window that the bundle lacks; a failed inventory leaves the branch pass incomplete, not estimated. Its `scannedAt` records when refs were observed and is not an analysis cutoff. Inspect the complete inventory, not only branches named `workspace/`, `research/`, or associated with merged PRs. A branch deleted without any associated PR may leave no reliably enumerable GitHub history, and an overlapping PR lifecycle makes a branch a candidate for inspection without proving when its ref existed or that its commits fall inside the window; do not infer when a branch was first opened from its PR's creation time. Record these limitations rather than claiming a perfect historical inventory.

For **every** inventory entry, use the branch name, associated PR title/body/files, live `main` and research-register status, and—where a ref or historical commit can actually be fetched—the branch's relevant commit history to classify it as **research-related**, **non-research**, or **uncertain**. A research-related branch is one that carries research investigation, experiment, independent/adversarial review, research evidence custody, or durable reconciliation of a research conclusion; names alone do not establish this. **Use every identified research-related branch as an input** to the research-delivery examination, including research work with no merged PR. For missing/deleted or uncertain candidates, explicitly record whether evidence was obtainable; never silently drop the entry. Assess work not yet merged as *in flight or historical research evidence*, not as delivered capability. Non-research branches need only enough inspection to classify them and flag material provenance or custody uncertainty, not a full code review or routine deletion assessment.

**Attribute to the window only what happened in it.** Draw period-specific research conclusions only from work materially relevant to the shared window: delivery established by a PR in the window's merged-`main` PR sample, or in-flight investigation established by verifiable evidence occurring inside it (for example a commit, review, or report revision), even without a merged PR. A PR merged inside the window into another branch is in-window activity and provenance, not delivery; its work counts as delivered only once it reaches `main`. Earlier report revisions, experiments, and adversarial reviews may be read from reachable branch history to explain how a decision delivered in the window was reached; label them **context**, not newly completed work. Commits, PR outcomes, or events after the through PR's merge are **subsequent context**, never period outcomes. Missing or unverifiable research history is a documented qualitative coverage limit; it does not change the mechanically established PR/review sample.

Within that exhaustive **research-branch input set**, choose a bounded, diverse set of consequential questions or coupled packets for deeper qualitative reconstruction, including complicated, blocked, superseded, no-action, or reworked work where observable. Reconstruct the decision-to-delivery path: brief and alternatives, proving/falsification cases, risk-proportionate evidence, independent review and surviving qualifications, decision/reconciliation or no-action, knowledge-transfer audit, and any implementation handoff/delivery. Look for missing distinctions, rework, lost experiments, avoidable wait, and failed knowledge transfer, but distinguish such waste from valid falsification, sound caution, and deliberate non-implementation. Compare what the research branches reveal with the PR reviews and durable results; say what **incremental, trustworthy insight** was actually gained from retained branch history, or say no additional value was demonstrated. After the complete classification and evidence-availability pass, deepen only cases where the additional history can change a control verdict, establish consequential rework or loss of custody, or resolve a material contradiction. Stop when further historical reconstruction only repeats accepted PR or maintained evidence; do not treat unresolvable or insufficiently evidenced branches as resolved by that stopping rule.

Research evidence requires **no special retention request, branch label, or hold status**: when the branch ref is still present, prior committed `workspace/research/` files remain accessible through its history even though its final merge tree is clean; a clean current tree is not evidence that nothing was persisted. To inspect a retained research workspace, verify the *current* ref and the handed-off report/review commit and path before trusting the content. Use `git log <verified-ref> -- workspace/research/` and read earlier report revisions, experiments, checkpoints, and adversarial challenges at their exact commits where they discriminate a process claim. A deleted branch or a copied SHA does not guarantee the artifact remains available; drafts/rejected alternatives are not accepted conclusions. Label each research branch **inspected**, **not resolvable**, or **insufficient evidence**, and explain the consequences for interpretation. Missing branch evidence limits that case, **not** the separately validated PR/review metrics. Retained branches are selected non-randomly; do not generalize from them to every investigation.

**End-of-run inventory accounting — chat only.** Report the exact window and inventory time, counts of live/absent and research/non-research/uncertain branch identities, coverage of inspected/not-resolvable/insufficient-evidence cases, and named material custody or coverage exceptions with their consequences. Preserve the complete local branch inventory and use every identified research-related branch as evidence input, but do **not** produce a per-branch cleanup table by default. The dated Git report keeps only decision-relevant research-delivery findings, and the checkpoint receives only the usual summary/link.

**Optional branch-cleanup advice — chat only.** When the owner separately requests branch-cleanup advice, present **every** inventory entry in a table: branch identity, current/ref-absent status, related PRs and window relation, research classification, evidence inspection and window relevance, and `DELETE CANDIDATE` | `KEEP` | `VERIFY FIRST` | `ALREADY ABSENT`, with reasons. A `DELETE CANDIDATE` requires proof that the ref exists, is not default/protected or an active PR head, has no unique unmerged work or custody obligation, and, if research-related, has terminal questions, completed knowledge-transfer and independent reconciliation, no needed unpromoted artifacts, and inspected history or explicit evidence limits. A squash merge does not itself prove safe deletion; reconcile the exact PR head, tip and surviving work. Unknown state means `VERIFY FIRST`. Neither the optional table nor the exhaustive inventory belongs in Git, a checkpoint comment, or the research register. This practice never deletes branches, force-pushes, closes PRs, or mutates research status; deletion requires a separate explicit owner instruction under `AGENTS.md`. The research operating model continues to own knowledge-transfer requirements.

Research-to-delivery elapsed-time and issue-aging statistics are canonical only as the analyzer computes them under [Research-to-delivery lead time](#research-to-delivery-lead-time). Do **not** hand-calculate and label them canonical, equate longer investigation with waste, use finding counts as research-quality scores, or silently classify unobservable cases as successes. Report clearly which observations are direct artifacts, delivery history, interpretation, or unavailable evidence.

### Research-to-delivery lead time

Every retrospective also reports how long decided work waits before it lands. The analyzer computes it over the exact PR window as `researchToDeliveryLeadTime`, listing each pull request or issue used with its elapsed days, plus the count and median of each measure:

- **handoff to merge** — for each implementation pull request merged in the window that implements a research [implementation handoff](researching.md#implementation-handoff), the time from the handoff's commit date to the merge;
- **issue opening to merge** — for each issue closed in the window by a merged pull request, the time from the issue's opening to that pull request's merge;
- **open issue age** — for each issue still open at the end of the window, its age at the through PR's merge. An issue a window pull request closes is measured as closed, not open, even when GitHub records the close seconds after the window ends.

Each implementation pull request names its input: the issue it closes, or the handoff it implements on the standalone `Implementation handoff:` line the [Implementer](agent-roles/implementer.md#pull-request-and-ci) defines. Acquisition confirms each named handoff's commit, file and commit date against GitHub. An issue counts as closed by a pull request only when GitHub records that pull request as the closer of the issue's current closure; a link saying a pull request may close an issue is not closure. An issue closed in the window any other way appears in neither measure. Whether an issue was open at the end of the window comes from its current opening and closing times, not its close and reopen history. These are measures to compare between retrospectives, not targets or thresholds.

### Action ownership

There is no continuous-improvement intervention register.

Every retrospective conclusion must resolve to one of these forms:

- **retain** — existing control is working; no work item;
- **retire/supersede** — stop carrying the old experiment/control as active work;
- **action** — concrete work is required and must be owned by an existing or newly created GitHub issue before the retrospective is complete;
- **verify next time** — an already-landed change needs later measurement only; record the measure in the dated report, not as an unowned intervention.

If nobody is expected to change anything before the next measurement, it is not an active intervention. For `verify next time`, name the specific failure mechanism and future eligible evidence to examine; insufficient exposure is **not yet verified** within that existing verdict, not a fifth action form. A `retain` or `retire/supersede` verdict needs evidence for its intended effect or obsolescence, not merely fewer review checkpoints.

### Dated report

Dated retrospective evidence lives under:

`docs/history/continuous-improvement/`

The normative method lives here; dated reports do not restate it. Keep each report concise (normally no more than about 80 lines) and use this structure:

```text
# Delivery-process retrospective — <date>

Scope: <baseline PR exclusive> -> <through PR inclusive>

## Result
<3-5 decision-oriented bullets, including decisions confirmed/changed, remaining uncertainty and the incremental value of this retrospective>

## Mechanical metrics
<small comparison table using helper output, plus research-to-delivery lead time>

## Prior changes
| Change | Verdict | Evidence | Action |

## Research delivery
<bounded cases within the Scope window, with context and subsequent context labeled; evidence of
review/reconciliation or avoidable rework; incremental value or
unavailability of retained branch artifacts; retain/retire/process action, if justified>

## Actions
- #<issue> — <required outcome>
or
- none

## Next baseline
- baseline PR
- specific measures to verify next time
```

Detailed review bodies, PR histories, issue evidence, and calculations remain in their owning GitHub/repository sources; do not duplicate them into the report unless necessary to support a decision. Keep the complete branch inventory in transient local evidence; any separately requested per-branch cleanup advice stays in chat, never in the Git report or checkpoint comment.

**Decision value and conclusion audit.** In `Result` or `Actions`, briefly state what decisions this run changed or confirmed, what meaningful uncertainty remains, and what the retrospective added beyond existing PR and Consistency evidence; `none` is valid when no decision changed. When credible execution-effort evidence exists, give a modest cost indication; otherwise state that cost was not measured. Before publishing, check each decision-bearing claim against its cited GitHub or repository evidence, including temporal order, population/denominators, window vs later discovery, qualification and plausible counterexamples. Distinguish observed facts, interpretation and unverified hypotheses; correct or narrow unsupported claims. This is a reasoning check, not a new prose-based CI gate, metric target or persistence layer.

### Completion

A retrospective is complete only when one reviewed PR:

1. adds the dated report under `docs/history/continuous-improvement/`;
2. advances `.github/continuous-improvement/retrospective.json` to the exact `throughPr`;
3. names every concrete follow-up issue in the report;
4. carries no unowned intervention/trial state.

The versioned state file records only the retrospective baseline and latest report path. It is not a trigger, due-state, reminder, or intervention register.

### Retrospective checkpoint publication

Carry the explicit checkpoint issue number from the Continuous Improvement handoff when one exists; otherwise resolve the single open checkpoint before opening the retrospective PR. Do not create a checkpoint for a standalone retrospective that has no destination. The retrospective PR body must contain a standalone stable `Retrospective checkpoint: #<number>` line **when an associated checkpoint exists**. This is a durable reference, not a claim about mutable PR or CI state.

After creating the report PR, publish a short **proposed** result comment to that issue: the exact baseline and through-PR window, principal provisional decisions, follow-up issues, and report PR link. First reconcile its claims against the conclusion-audited report's `Result` and `Actions`: preserve temporal qualifiers, completeness limits, and unresolved ownership; do not strengthen a report claim in the checkpoint summary. If review later corrects the report, update the existing same-author proposed comment using its stable marker and the checkpoint publisher's ownership/deduplication rules rather than leaving conflicting guidance. Use practice `retrospective`, run ID `pr-<report-PR-number>-proposed`. The PR and report are not yet authoritative; do not call the retrospective complete.

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
| Retrospective mechanical analysis | `infra/dev/delivery-retrospective.mjs` |
| Retrospective branch inventory | `infra/dev/delivery-retrospective-branches.mjs` |
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
