# Continuous-improvement operating model

> **Status:** canonical operating model for when Arcogine's improvement loops run, what each loop records, and where its state lives.

Arcogine has three distinct improvement loops. None substitutes for another.

## Session-close Kaizen

- **Trigger:** `.?` at the close of a meaningful coding-agent session; see `AGENTS.md`.
- **Purpose:** preserve durable lessons as executable safeguards, standard work, or maintained knowledge.
- **Owner:** the coding agent.
- **Cadence:** event-driven only; no recurring due state.

## Weekly Consistency review

- **Trigger/cadence:** weekly, plus additional review after major architecture/status transitions when useful.
- **Purpose:** deep repository semantic review across implementation, architecture/specifications, planning, docs, examples, config, tests, CI, and prior findings.
- **Owner/runtime:** the Consistency reviewer using the canonical repository-snapshot protocol plus live GitHub state.
- **Finding identity:** open `CONS:` issues are the unresolved finding ledger.
- **Completion ledger:** GitHub issue **#295**, titled exactly `Consistency review ledger`, is append-only operational history for completed reviews. Its body is static instructions, not mutable state.

A completed formal review appends one comment to #295:

```text
### Consistency review completion

- verified at: <UTC YYYY-MM-DD>
- reviewed head: <full main SHA>
- result: CLEAN | FINDINGS
- finding issues: none | #<number>, #<number>, ...
```

Only comments authored by a repository OWNER, MEMBER, or COLLABORATOR and matching that complete shape count as completion records. The latest valid completion comment is the previous-review fact source. Corrections are new comments; do not rewrite old completion evidence.

`CLEAN` requires `finding issues: none`. `FINDINGS` lists every unresolved consistency issue applicable to that reviewed head.

Weekly due state is derived from the latest valid `verified at` date:

- **CURRENT** — at most 7 days old;
- **DUE** — more than 7 but at most 14 days old, or no valid completion exists;
- **OVERDUE** — more than 14 days old.

A malformed or future completion date is not current.

## Delivery-process retrospective

### Trigger

The retrospective is evidence-driven, not calendar-driven. Versioned trigger state lives in:

`.github/continuous-improvement/retrospective.json`

Evaluate the retrospective method when any mechanical condition holds:

- at least 25 merged PRs since `baselinePr`;
- `escapeEvidenceCount >= 2`;
- `p1LifecycleEscape == true`.

Crossing a threshold means evaluate whether a retrospective is warranted; it is not itself the retrospective conclusion.

### Purpose

Determine whether Arcogine's delivery controls are reducing recurring waste and escapes, decide which prior changes should be retained/retired/superseded, and create owned follow-up work only where the evidence justifies it.

The retrospective is **not** a process-history dump, an issue ledger, or a second planning system.

### Mechanical evidence first

Before interpretation:

1. choose one exact main-target `throughPr` that bounds the run;
2. run:

   ```bash
   node infra/dev/delivery-retrospective.mjs --through-pr <number> --json
   ```

3. require the helper to complete successfully;
4. use its exact PR window, merged-PR count, CHANGES REQUIRED submission count, and 0/1/2/3+ review-checkpoint distribution without manually reconstructing or retyping alternative totals.

The helper fails closed when GitHub search/review retrieval cannot prove completeness. If it fails, the retrospective is `INCOMPLETE`; fix the retrieval/tooling problem rather than estimating the sample.

Manual analysis begins only after the mechanical window is established. It should focus on:

- whether the previous retrospective's retained controls actually prevented recurrence;
- high-confidence post-merge process/lifecycle escapes;
- repeated waste that can be tied to a concrete mechanism;
- whether an existing experiment/change should be retained, retired, or superseded.

Do not optimize for finding count. Healthy adversarial review findings are not waste merely because they are numerous.

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
- baseline PR/date
- specific measures to verify next time
- early-trigger conditions
```

Detailed review bodies, PR histories, issue evidence, and calculations remain in their owning GitHub/repository sources; do not duplicate them into the report unless necessary to support a decision.

### Completion

A retrospective is complete only when one reviewed PR:

1. adds the dated report under `docs/history/continuous-improvement/`;
2. advances `.github/continuous-improvement/retrospective.json` to the exact `throughPr`;
3. names every concrete follow-up issue in the report;
4. carries no unowned intervention/trial state.

The versioned state file records only trigger/baseline facts and the latest report path. It is not an intervention register.

## Authority boundaries

| Concern | Owning authority |
| --- | --- |
| Session-close Kaizen | `AGENTS.md` |
| Consistency review algorithm | `.github/agents/consistency.agent.md` |
| Consistency corpus generation | `infra/dev/repo-snapshot.mjs` + `infra/dev/repomix.config.json` |
| Consistency operating guidance | `docs/development/consistency-review.md` |
| Consistency finding identity | GitHub `CONS:` issues |
| Consistency completion history | issue #295 append-only completion comments |
| Retrospective method | this document |
| Retrospective trigger/baseline state | `.github/continuous-improvement/retrospective.json` |
| Retrospective mechanical window | `infra/dev/delivery-retrospective.mjs` |
| Dated retrospective evidence | `docs/history/continuous-improvement/` |
| Retrospective actions | their owning GitHub issues |
| Raw delivery evidence | GitHub PR/review/CI/issue history |

PR review remains governed by `docs/development/reviewing.md`; it is evidence for retrospectives, not a fourth improvement ceremony.

## Reminder boundaries

Do not evaluate recurring continuous-improvement obligations during ordinary repository grounding. Evaluate them only at the natural process boundaries defined in `AGENTS.md`: Session-close Kaizen and tasks explicitly concerning continuous improvement, Consistency cadence, delivery-process health, or repository-wide planning/next-work.

User-facing reminders state the action plainly and include the minimal fresh-session prompt:

- weekly Consistency review due/overdue -> `The weekly Consistency review is due. Start a fresh session with: "Run the consistency review."`
- retrospective threshold reached -> `The delivery-process retrospective threshold has been reached. Start a fresh session with: "Run the delivery-process retrospective."`

If no action is warranted, say nothing. If the factual source cannot be verified, say so once without inventing a status.

## Non-goals

This system does not automatically execute a review/retrospective, maintain an intervention database, turn historical reports into normative guidance, create work without issue ownership, or use GitHub as a slow substitute for repository content already established through the snapshot protocol.
