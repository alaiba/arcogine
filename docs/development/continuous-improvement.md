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
- **Purpose:** deep repository semantic review across implementation, architecture, ADRs, planning, docs, examples, config, tests, CI, and prior findings.
- **Owner/runtime:** the Consistency reviewer in a ChatGPT chat session using a mandatory exact-current-main Repomix corpus plus the GitHub connector for mutable state. See `.github/agents/consistency.agent.md` and `docs/development/repository-snapshot.md`.
- **Execution:** manual/user-invoked. A stale/missing Repomix makes the review `INCOMPLETE`; it is updated and retried rather than reconstructed through GitHub file reads.
- **Review strategy:** the previous reviewed head is a recency anchor only. New and changed content gets first attention, but scope is not bounded to that change range and later reviews may uncover older inconsistencies.
- **Finding accounting:** invoking a formal review authorizes only the GitHub issue operations needed to account for that review's findings and the final weekly-register update. It does not authorize remediation or merging.

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
| Consistency corpus generation | `infra/dev/repo-snapshot.mjs` + `infra/dev/repomix.config.json` |
| Consistency operating guidance | `docs/development/consistency-review.md` |
| Retrospective method/evidence | dated retrospective documents |
| Current recurring-obligation state | GitHub issue #295 |
| Raw delivery evidence | GitHub PR/review/CI/issue history |

PR review remains governed by `docs/development/reviewing.md`; it is evidence for retrospectives, not a fourth improvement ceremony.

## Continuous improvement register

GitHub issue **#295**, titled `Continuous improvement register`, is mandatory operational state. Automation and reviewers do not discover, bootstrap, recreate, or replace it. If #295 is unavailable or no longer has that title, the operation fails rather than writing elsewhere.

The body contains recurring obligations inside `<!-- continuous-improvement:obligations:start -->` / `...:end -->` and agent/human-managed active interventions outside those markers.

Within recurring obligations:

- the Consistency reviewer writes accounting fields in `### Weekly Consistency review` after a fresh-current-main review;
- the scheduled workflow may update the weekly derived `state` as time passes;
- the scheduled workflow writes the mechanically derived delivery-retrospective count/state;
- the workflow preserves reviewer-owned weekly date/head/result/finding identities.

The register is active state, not an append-only process database. Historical evidence belongs in issues, PRs, commits, and dated retrospective documents.

### Recording a Consistency review

Immediately before finding/register mutations, the reviewer rechecks that live `main` still equals the Repomix commit. A mismatch aborts accounting and requires a fresh snapshot/retry.

A completed review directly edits the weekly section:

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

The recorded head is used to bias the next review toward newer material; it never narrows that review's scope or certifies older content as consistent. This body edit is the whole completion protocol: no completion-comment ledger, parser, event-driven completion trigger, manual dispatch, `gh api`, or synchronous refresh.

### Scheduled derived-state refresh

`.github/workflows/continuous-improvement.yml` is maintenance, not review completion plumbing. On schedule it reads #295, derives `CURRENT`/`DUE`/`OVERDUE` from `last verified`, recomputes the retrospective raw-merge guard/`CHECK_TRIGGER`, and preserves reviewer-owned weekly accounting plus intervention content.

The weekly states are:

- `CURRENT` when the last verified date is at most 7 days old;
- `DUE` when there is no verified review or it is more than 7 but at most 14 days old;
- `OVERDUE` when it is more than 14 days old.

A workflow run is never evidence that a Consistency review occurred.

## Consistency finding identities

All findings use `CONS: <semantic title>`. GitHub issue number is the sole durable identity. Open findings load at review start; closed findings are searched only when a candidate needs duplicate/regression matching.

## Retrospective baseline

The retrospective baseline and explicit escape evidence live in `.github/scripts/continuous-improvement-data.json`. A later verified retrospective advances that data as an ordinary repository change. Raw merge count reaching the guard threshold produces `CHECK_TRIGGER`, not an automatic retrospective decision.

## Every-agent reminder

On the first normal repository grounding of a session, Arcogine agents inspect issue #295 as described in `AGENTS.md`. `CURRENT` is silent; `DUE`/`OVERDUE` and `CHECK_TRIGGER` are mentioned at most once and never derail the requested task.

## Non-goals

This system does not automatically execute a Consistency review or retrospective, require local coding-agent compatibility for the reviewer, become a general process database, preserve compatibility machinery for old finding-title formats, or use GitHub as a slow substitute for the required Repomix content corpus.
