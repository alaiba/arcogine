# Continuous-improvement operating model

> **Status:** canonical operating model for when Arcogine's improvement loops run and how their active state is tracked.

Arcogine has three distinct improvement loops. None substitutes for another.

## Session-close Kaizen

- **Trigger:** `.?` at the close of a meaningful coding-agent session; see `AGENTS.md`.
- **Purpose:** preserve durable lessons as executable safeguards, standard work, or maintained knowledge.
- **Owner:** the coding agent.
- **Cadence:** event-driven only; no recurring due state.

## Weekly Consistency review

- **Trigger/cadence:** weekly, plus additional review after major architecture/status transitions when useful.
- **Purpose:** repository semantic coherence across implementation, architecture, ADRs, planning, docs, examples, config, tests, CI, and prior findings.
- **Owner/runtime:** the Consistency reviewer in a ChatGPT chat session using the GitHub connector. The detailed algorithm is `.github/agents/consistency.agent.md`.
- **Execution:** manual/user-invoked. The scheduled workflow tracks derived state but never performs the judgment-bearing review.
- **Scope:** selected automatically. With no valid baseline the review is `FULL`; otherwise it is `INCREMENTAL` from the recorded head to current `main`.
- **Finding accounting:** invoking a formal review authorizes only the GitHub issue operations needed to account for that review's findings and the final update of the weekly register section. It does not authorize remediation or merging.

## Delivery-process retrospective

- **Trigger:** evidence-based, not calendar-driven — about 25 additional substantive merges since the recorded baseline, or 2 high-confidence post-merge process escapes, or 1 P1 lifecycle/process escape. See the latest dated `delivery-process-retrospective-YYYY-MM-DD.md` for the full method.
- **Purpose:** empirical evidence about delivery-process performance and waste.
- **Owner:** a human/agent who judges whether the substantive trigger actually fired.
- **Mechanical guard:** the workflow may count raw merged PRs and surface `CHECK_TRIGGER`; it never turns that raw count into an automatic retrospective decision.

## Authority boundaries

| Concern | Owning authority |
| --- | --- |
| Session-close Kaizen | `AGENTS.md` |
| Consistency review algorithm | `.github/agents/consistency.agent.md` |
| Consistency operating guidance | `docs/development/consistency-review.md` |
| Retrospective method/evidence | dated retrospective documents |
| Current recurring-obligation state | GitHub issue #295 |
| Raw delivery evidence | GitHub PR/review/CI/issue history |

PR review remains governed by `docs/development/reviewing.md`; it is evidence for retrospectives, not a fourth improvement ceremony.

## Continuous improvement register

GitHub issue **#295**, titled `Continuous improvement register`, is the fixed operational register for this repository. It already exists; automation does not discover, bootstrap, or replace it. If issue #295 is unavailable or no longer has that title, automation fails rather than writing elsewhere.

The register body has two regions separated by HTML markers:

- **Recurring obligations** inside `<!-- continuous-improvement:obligations:start -->` / `...:end -->`.
- **Agent/human-managed active interventions** outside those markers.

Inside the recurring-obligations region, ownership is split by subsection:

- the Consistency reviewer writes the accounting fields in `### Weekly Consistency review` when a review completes;
- the scheduled workflow may update the weekly derived `state` as time passes;
- the scheduled workflow writes the mechanically derived `### Delivery-process retrospective` count/state;
- the workflow preserves the reviewer's weekly date/head/result/finding identities.

The register is active state, not an append-only process database. Historical evidence belongs in issues, PRs, commits, and dated retrospective documents.

### Recording a Consistency review

A completed formal review directly edits the weekly section of issue #295:

```text
### Weekly Consistency review

- last verified: <UTC YYYY-MM-DD>
- reviewed head: <full main SHA>
- accounted result: CLEAN | FINDINGS
- finding issues: none | #<number>, #<number>, ...
- next due / interval: every 7 days
- state: **CURRENT**
```

`CLEAN` requires `finding issues: none`; `FINDINGS` cites every unresolved consistency issue applicable to the reviewed head. The GitHub issue number is the finding identity.

This body edit is the whole completion protocol. There is no completion-comment ledger, comment parser, `issue_comment` trigger, `repository_dispatch`, `workflow_dispatch`, `gh api` call, or synchronous refresh requirement.

### Scheduled derived-state refresh

`.github/workflows/continuous-improvement.yml` is maintenance, not completion plumbing. On its schedule it:

1. reads the current weekly review accounting directly from issue #295's body;
2. derives `CURRENT`, `DUE`, or `OVERDUE` from `last verified`;
3. recomputes the retrospective raw merged-PR guard and `CHECK_TRIGGER` state;
4. updates the recurring-obligations region while preserving weekly review accounting and all intervention content.

The weekly states are:

- `CURRENT` when the last verified date is at most 7 days old;
- `DUE` when there is no verified review or it is more than 7 but at most 14 days old;
- `OVERDUE` when it is more than 14 days old.

The workflow runs daily so the derived display state does not lag the recorded review date by several days. A workflow run is never evidence that a review occurred; only the weekly accounting fields written by the reviewer establish that.

## Consistency finding identities

All consistency findings use the title prefix:

```text
CONS: <semantic title>
```

The GitHub issue number is the sole durable identity. Earlier numeric `CONS-*` issue titles were normalized instead of being carried as a compatibility scheme.

A formal review loads open `CONS:` findings up front because unresolved findings must be carried forward. Closed findings are searched only when a candidate new finding is identified, using the candidate's semantic subject/terminology/evidence to detect duplicates or regressions. The cost of historical lookup therefore grows with new candidates, not with the total lifetime ledger size.

## Retrospective baseline

The retrospective baseline and explicit escape evidence live in `.github/scripts/continuous-improvement-data.json`. A later verified retrospective advances that data as an ordinary repository change; the helper's state-derivation logic does not need to change.

Raw merged-PR count reaching the configured guard threshold produces `CHECK_TRIGGER`, not an automatic `DUE`. Judgment about whether the substantive retrospective trigger fired remains outside the workflow.

## Every-agent reminder

On the first normal repository grounding of a session, Arcogine agents inspect issue #295's recurring-obligations state as described in `AGENTS.md`. `CURRENT` is silent; `DUE`/`OVERDUE` and `CHECK_TRIGGER` are mentioned at most once and never derail the requested task.

## Non-goals

This system does not automatically execute a Consistency review or retrospective, require local coding-agent compatibility for the Consistency reviewer, become a general process database, or preserve redundant compatibility machinery for old finding-title formats. Its job is to preserve current recurring-obligation state and durable finding identities while leaving semantic judgment in the ChatGPT review session.
