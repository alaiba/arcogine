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
- **Owner/runtime:** the Consistency reviewer in a ChatGPT chat session using a canonical-`main` Repomix baseline reconciled to one exact current `main` target through `docs/development/repository-snapshot.md`, plus the GitHub connector for mutable state.
- **Execution:** manual/user-invoked. A snapshot may be behind current `main`; that is acceptable when exact target `T`, ancestry, and a complete usable `S..T` delta are established. Missing/malformed provenance or unsafe/incomplete reconciliation makes the review `INCOMPLETE` and requires a refreshed snapshot.
- **Review strategy:** the previous reviewed head is a recency anchor only. New and changed content gets first attention, but scope is not bounded to that change range and later reviews may uncover older inconsistencies.
- **Finding accounting:** invoking a formal review authorizes only the GitHub issue operations needed to account for that review's findings and the final weekly-register update. It does not authorize remediation or merging.

## Delivery-process retrospective

- **Trigger:** evidence-based, not calendar-driven — about 25 additional substantive merges since the recorded baseline, or 2 high-confidence post-merge process escapes, or 1 P1 lifecycle/process escape. See the latest dated `delivery-process-retrospective-YYYY-MM-DD.md` for the full method.
- **Purpose:** empirical evidence about delivery-process performance and waste.
- **Owner:** a human/agent who judges whether the substantive trigger actually fired.
- **Mechanical guard:** agents can count merged PRs since the baseline to detect that the retrospective threshold should be evaluated; that raw count never turns itself into an automatic retrospective decision.

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

Immediately before finding mutations, the reviewer rechecks that live `main` still equals the exact reviewed target `T`. After finding accounting is complete, it rechecks `main` against the same `T` again, then re-fetches #295 immediately before the final body edit.

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

Weekly state is derived when reminder state is evaluated from the factual `last verified` date; it is not persisted separately:

- `CURRENT` when the last verified date is at most 7 days old;
- `DUE` when no review is recorded, or it is more than 7 but at most 14 days old;
- `OVERDUE` when it is more than 14 days old.

A malformed or future `last verified` value is not `CURRENT`; treat the weekly state as unverifiable and surface that once during reminder evaluation.

## Consistency finding identities

All findings use `CONS: <semantic title>`. GitHub issue number is the sole durable identity. Open findings load at review start; closed findings are searched only when a candidate needs duplicate/regression matching. Reconciliation is idempotent: the same semantic finding reuses the same issue, and repeated observation alone does not create duplicate issues or duplicate evidence.

## Retrospective baseline and trigger check

The retrospective baseline and explicit escape evidence live in `.github/scripts/continuous-improvement-data.json`. A later verified retrospective advances that file as an ordinary repository change.

When evaluating the trigger, count merged PRs newer than `baselinePr` from newest to oldest and stop as soon as the baseline is reached. Do not scan older PR history. The retrospective threshold needs evaluation when any mechanical condition holds:

- raw merged PRs since baseline >= 25;
- `escapeEvidenceCount >= 2`;
- `p1LifecycleEscape == true`.

Crossing that threshold means evaluate the retrospective method; it is not itself a conclusion that the retrospective is due.

## Reminder boundaries and wording

Do not evaluate recurring continuous-improvement obligations during ordinary repository grounding. Evaluate them only at the natural process boundaries defined in `AGENTS.md`: Session-close Kaizen and tasks that explicitly concern continuous improvement, Consistency cadence, delivery-process health, or repository-wide planning/next-work.

User-facing reminders state the action plainly and include the minimal fresh-session prompt:

- weekly Consistency review due or overdue → `The weekly Consistency review is due. Start a fresh session with: "Run the consistency review."`
- delivery-process retrospective threshold reached → `The delivery-process retrospective threshold has been reached. Start a fresh session with: "Run the delivery-process retrospective."`

These sentences are interaction guidance, not persisted state. Internal derivation labels such as `CURRENT`, `DUE`, or `OVERDUE` may remain useful while computing the reminder, but they are not the reminder itself. If no action is warranted, say nothing. If state cannot be verified, say so once without inventing a status. A reminder is mentioned at most once per session and never derails the requested task.

## Removed automation

There is no scheduled continuous-improvement register workflow or register-body helper. Derived state changes with time and repository history, so recomputing it when an agent actually needs the reminder is simpler and avoids concurrent writers racing over issue #295.

## Non-goals

This system does not automatically execute a Consistency review or retrospective, maintain a second completion ledger, require local coding-agent compatibility for the reviewer, become a general process database, preserve compatibility machinery for old finding-title formats, or use GitHub as a slow substitute for unaffected content already established by the Repomix baseline plus revision-bound delta.
