# Continuous-improvement operating model

> **Status:** canonical operating model for when Arcogine's improvement loops run, what each loop records, and where its state lives.

Arcogine has three distinct improvement loops. None substitutes for another.

## Routed topics

Read the topic whose trigger matches your task. A routed topic is mandatory whenever its trigger applies; only the authority-boundaries table below is reference material, consulted to find the owner of a concern.

| Read | When you are |
|---|---|
| [Continuous Improvement assessment](continuous-improvement/assessment.md) | Running `Assess continuous improvement.`, or interpreting the weekly `Continuous improvement checkpoint` reminder. |
| [Checkpoint outcome publication](continuous-improvement/checkpoint-publication.md) | Publishing a completed assessment, formal Consistency review or delivery-process retrospective result to the checkpoint issue. |
| [Session-close Kaizen](#session-close-kaizen) | Handling an entire message `.?`. |
| [Consistency review](#consistency-review) | Deciding when a formal Consistency review is useful, or what it records. |
| [Delivery-process retrospective](continuous-improvement/retrospective.md) | Running an explicitly requested retrospective: invocation, purpose and the mechanical evidence pass come first. |
| [Research delivery and temporary-evidence value](continuous-improvement/retrospective-research-branches.md) | Every formal retrospective: the shared window, the branch inventory, branch classification and window attribution. |
| [Research case reconstruction and branch accounting](continuous-improvement/retrospective-research-cases.md) | Every formal retrospective after the inventory: case selection and reconstruction, retained research evidence, end-of-run accounting, and owner-requested branch-cleanup advice. |
| [Research-to-delivery lead time](continuous-improvement/retrospective-lead-time.md) | Every retrospective's lead-time report, and the `Implementation handoff:` line an implementation pull request carries. |
| [Retrospective conclusions, report and completion](continuous-improvement/retrospective-report.md) | Concluding a retrospective: action ownership, the dated report, completion and checkpoint publication. |

A formal retrospective reads all five retrospective topics: they are one method, split only for size.

## Session-close Kaizen

For `.?`, follow the [session-close rules](agent-rules/session-close.md), including classification, capture preferences and the deletion verdict.

## Consistency review

- **Recommended cadence:** roughly weekly, and after significant architecture/status transitions when useful.
- **Purpose:** deep repository semantic review across implementation, architecture/specifications, research state, docs, examples, config, tests, CI, and prior findings.
- **Owner/runtime:** the Consistency reviewer using the canonical repository-snapshot protocol plus live GitHub state.
- **Actionable durable output:** only evidence-backed `CONS:` finding issues that require resolution; the optional checkpoint result comment is informational.

The cadence is guidance, not persisted scheduler state. Arcogine does not maintain a last-reviewed timestamp, previous-reviewed-head ledger, or `CURRENT`/`DUE`/`OVERDUE` state for Consistency.

A clean review creates no finding issue, but a completed formal review posts its `CLEAN` or `FINDINGS` summary to the associated checkpoint as [Checkpoint outcome publication](continuous-improvement/checkpoint-publication.md) describes. A review with findings creates, updates, reopens, or closes the applicable `CONS:` issues. Only the findings affect future work; the comment records the review outcome without creating completion state.

Recent Git history may be used as a search-order heuristic, but no previous-review coordinate bounds review scope or is required for correctness.

## Authority boundaries

| Concern | Owning authority |
| --- | --- |
| Session-close Kaizen | [Session-close rules](agent-rules/session-close.md#session-close-kaizen) |
| Continuous Improvement assessment | `docs/development/agent-roles/continuous-improvement.md` |
| Assessment reminder delivery | `.github/workflows/continuous-improvement-reminder.yml` |
| Consistency review algorithm | `docs/development/agent-roles/consistency.md` |
| Consistency corpus generation | `infra/dev/repo-snapshot.mjs` + `infra/dev/repomix.config.json` |
| Consistency operating guidance | `docs/development/consistency-review.md` |
| Consistency finding identity/state | GitHub `CONS:` issues |
| Retrospective method | the retrospective topics routed above, starting at [`continuous-improvement/retrospective.md`](continuous-improvement/retrospective.md) |
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

Ordinary repository grounding and `.?` do not assess global improvement health. The Continuous Improvement agent is the explicit on-demand boundary for that question and may recommend a specialized practice without performing it. Delivery-process retrospective execution remains explicit and uses the versioned baseline plus the [mechanical method](continuous-improvement/retrospective.md#mechanical-evidence-first).

The scheduled checkpoint workflow is only an alarm clock for invoking that assessment. It does not establish that any practice is due.

## Non-goals

This system does not automatically execute a review/retrospective, infer practice due state, maintain retrospective due state, maintain a Consistency completion ledger, maintain an intervention database, turn historical reports into normative guidance, create work without issue ownership, or use GitHub as a slow substitute for repository content already established through the snapshot protocol.
