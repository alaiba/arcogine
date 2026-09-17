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
- **Execution:** manual/user-invoked. A stale/missing Repomix makes the review `INCOMPLETE`; update it and retry rather than reconstructing repository content through GitHub file reads.
- **Review strategy:** the previous reviewed head is a recency anchor only. New and changed content gets first attention, but scope is not bounded to that change range and later reviews may uncover older inconsistencies.
- **Finding accounting:** invoking a formal review authorizes only the GitHub issue operations needed to account for that review's findings and the final weekly-register update. It does not authorize remediation or merging.

## Delivery-process retrospective

- **Trigger:** evidence-based, not calendar-driven — about 25 additional substantive merges since the recorded baseline, or 2 high-confidence post-merge process escapes, or 1 P1 lifecycle/process escape. See the latest dated `delivery-process-retrospective-YYYY-MM-DD.md` for the full method.
- **Purpose:** empirical evidence about delivery-process performance and waste.
- **Owner:** a human/agent who judges whether the substantive trigger actually fired.
- **Mechanical guard:** agents can count merged PRs since the baseline to surface `CHECK_TRIGGER`; that raw count never turns itself into an automatic retrospective decision.

## Authority boundaries

| Concern | Owning authority |
| --- | --- |
| Session-close Kaizen | `AGENTS.md` |
| Consistency review algorithm | `.github/agents/consistency.agent.md` |
| Consistency corpus generation | `infra/dev/repo-snapshot.mjs` + `infra/dev/repomix.config.json` |
| Consistency operating guidance | `docs/development/consistency-review.md` |
| Retrospective method/evidence | dated retrospective documents |
| Weekly review facts and active interventions | GitHub issue #295 |
| Retrospective baseline/recorded escape evidence | `.github/scripts/continuous-improvement-data.json` |
| Raw delivery evidence | GitHub PR/review/CI/issue history |

PR review remains governed by `docs/development/reviewing.md`; it is evidence for retrospectives, not a fourth improvement ceremony.

## Continuous improvement register

GitHub issue **#295**, titled `Continuous improvement register`, is mandatory operational state. It already exists and is never discovered, bootstrapped, recreated, or substituted. If #295 is unavailable or no longer has that title, operations that depend on it fail rather than writing elsewhere.

The register stores facts and active interventions. It is not an append-only process database, and it does not need a scheduled writer. Historical evidence belongs in issues, PRs, commits, and dated retrospective documents.

### Recording a Consistency review

Immediately before finding mutations, the reviewer rechecks that live `main` still equals the Repomix commit. After finding accounting is complete, it rechecks `main` again, then re-fetches #295 immediately before the final body edit.

A completed review replaces only `### Weekly Consistency review` in that latest body:

```text
### Weekly Consistency review

- last verified: <UTC YYYY-MM-DD>
- reviewed head: <full main SHA>
- accounted result: CLEAN | FINDINGS
- finding issues: none | #<number>, #<number>, ...
- interval: every 7 days
```

`CLEAN` requires `finding issues: none`; `FINDINGS` cites every unresolved consistency issue applicable to the reviewed head. The GitHub issue number is the finding identity.

The recorded head biases the next review toward newer material; it never narrows that review's scope or certifies older content as consistent. This body edit is the whole completion protocol: no completion-comment ledger, scheduled refresh, event trigger, manual dispatch, `gh api`, or second derived-state store.

### Weekly due-state derivation

Weekly state is derived when an agent grounds from the factual `last verified` date; it is not persisted separately:

- `CURRENT` when the last verified date is at most 7 days old;
- `DUE` when no review is recorded, or it is more than 7 but at most 14 days old;
- `OVERDUE` when it is more than 14 days old.

A malformed or future `last verified` value is not `CURRENT`; treat the weekly state as unverifiable and surface that once during grounding.

## Consistency finding identities

All findings use `CONS: <semantic title>`. GitHub issue number is the sole durable identity. Open findings load at review start; closed findings are searched only when a candidate needs duplicate/regression matching.

## Retrospective baseline and trigger check

The retrospective baseline and explicit escape evidence live in `.github/scripts/continuous-improvement-data.json`. A later verified retrospective advances that file as an ordinary repository change.

When evaluating the trigger, count merged PRs newer than `baselinePr` from newest to oldest and stop as soon as the baseline is reached. Do not scan older PR history. Surface `CHECK_TRIGGER` when any mechanical condition holds:

- raw merged PRs since baseline >= 25;
- `escapeEvidenceCount >= 2`;
- `p1LifecycleEscape == true`.

`CHECK_TRIGGER` means evaluate the retrospective method; it is not itself a conclusion that the retrospective is due.

## Every-agent reminder

On the first normal repository grounding of a session, Arcogine agents derive weekly state from #295 and evaluate the retrospective mechanical guard from the baseline data plus bounded live GitHub history, as described in `AGENTS.md`.

`CURRENT` alone is silent. `DUE`/`OVERDUE`, `CHECK_TRIGGER`, or unverifiable state is mentioned at most once and never derails the requested task.

## Removed automation

There is no scheduled continuous-improvement register workflow or register-body helper. Derived state changes with time and repository history, so recomputing it when an agent actually needs the reminder is simpler and avoids concurrent writers racing over issue #295.

## Non-goals

This system does not automatically execute a Consistency review or retrospective, maintain a second completion ledger, require local coding-agent compatibility for the reviewer, become a general process database, preserve compatibility machinery for old finding-title formats, or use GitHub as a slow substitute for the required Repomix content corpus.
