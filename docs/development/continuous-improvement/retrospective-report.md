# Retrospective conclusions, report and completion

Part of the [delivery-process retrospective](retrospective.md) method in the [continuous-improvement operating model](../continuous-improvement.md#routed-topics); a formal retrospective reads every retrospective topic.

## Action ownership

There is no continuous-improvement intervention register.

Every retrospective conclusion must resolve to one of these forms:

- **retain** — existing control is working; no work item;
- **retire/supersede** — stop carrying the old experiment/control as active work;
- **action** — concrete work is required and must be owned by an existing or newly created GitHub issue before the retrospective is complete;
- **verify next time** — an already-landed change needs later measurement only; record the measure in the dated report, not as an unowned intervention.

If nobody is expected to change anything before the next measurement, it is not an active intervention. For `verify next time`, name the specific failure mechanism and future eligible evidence to examine; insufficient exposure is **not yet verified** within that existing verdict, not a fifth action form. A `retain` or `retire/supersede` verdict needs evidence for its intended effect or obsolescence, not merely fewer review checkpoints.

## Dated report

Dated retrospective evidence lives under:

`docs/history/continuous-improvement/`

The normative method lives in these retrospective topics; dated reports do not restate it. Keep each report concise (normally no more than about 80 lines) and use this structure:

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

## Completion

A retrospective is complete only when one reviewed PR:

1. adds the dated report under `docs/history/continuous-improvement/`;
2. advances `.github/continuous-improvement/retrospective.json` to the exact `throughPr`;
3. names every concrete follow-up issue in the report;
4. carries no unowned intervention/trial state.

The versioned state file records only the retrospective baseline and latest report path. It is not a trigger, due-state, reminder, or intervention register.

## Retrospective checkpoint publication

Carry the explicit checkpoint issue number from the Continuous Improvement handoff when one exists; otherwise resolve the single open checkpoint before opening the retrospective PR. Do not create a checkpoint for a standalone retrospective that has no destination. The retrospective PR body must contain a standalone stable `Retrospective checkpoint: #<number>` line **when an associated checkpoint exists**. This is a durable reference, not a claim about mutable PR or CI state. A retrospective PR is a change to `main` and follows the same issue-only admission as any other: the associated checkpoint issue admits it, and without a checkpoint the owner opens an issue first. The PR uses that issue's single branch.

After creating the report PR, publish a short **proposed** result comment to that issue: the exact baseline and through-PR window, principal provisional decisions, follow-up issues, and report PR link. First reconcile its claims against the conclusion-audited report's `Result` and `Actions`: preserve temporal qualifiers, completeness limits, and unresolved ownership; do not strengthen a report claim in the checkpoint summary. If review later corrects the report, update the existing same-author proposed comment using its stable marker and the checkpoint publisher's ownership/deduplication rules rather than leaving conflicting guidance. Use practice `retrospective`, run ID `pr-<report-PR-number>-proposed`. The PR and report are not yet authoritative; do not call the retrospective complete.

After the reviewed report PR merges, `.github/workflows/retrospective-checkpoint.yml` checks that the merged PR changed the baseline and the report, reads the **exact merged commit**, and publishes a separate **complete** comment with the report's Result and Actions sections and an immutable report link. Its run ID is `pr-<report-PR-number>-merged`. The separate comments are intentional: GitHub Actions cannot ordinarily edit a human-authored provisional comment. Both phases are idempotent; a missing checkpoint reference means no destination and does not create one. Workflow failures must be investigated or rerun, not silently treated as publication.

Git remains the authoritative retrospective record. A checkpoint comment never advances `retrospective.json` or replaces the reviewed report.
