# Continuous-improvement operating model

> **Status:** canonical operating model for when and why Arcogine's improvement loops run and how their active state is tracked. This document does not restate the detailed procedure owned elsewhere — see the ownership boundaries below.

Arcogine runs three distinct continuous-improvement loops. They differ in trigger, purpose, and who executes them. Conflating them was itself a repeated failure mode this document exists to prevent: none of the loops below may substitute for another.

## The three loops

### Session-close Kaizen

- **Trigger:** event-driven only, via `.?` at the close of a meaningful coding-agent session. See `AGENTS.md`.
- **Purpose:** capture durable lessons — a safeguard, standard-work change, or documentation update — while session context is fresh, before it is lost with the session.
- **Owner:** the coding agent, per `AGENTS.md`.
- **Not scheduled.** No workflow triggers this loop. A missed session close simply means that session's lessons were not captured; there is no recurring due state to track.

### Weekly Consistency review

- **Trigger/cadence:** a real, repository-owned weekly obligation, plus additional high-scrutiny review when a major architecture/status transition justifies one (see `docs/development/consistency-review.md`).
- **Purpose:** repository semantic coherence — the diagnostic sweep across implementation, architecture, ADRs, planning, docs, examples, config, tests, CI, and prior findings.
- **Owner:** the Consistency agent (`.github/agents/consistency.agent.md`), invoked explicitly by a user/agent. **Execution is manual.** `.github/workflows/continuous-improvement.yml` never invokes the Consistency agent — it only makes the obligation's due state visible in the register (see below) so a missed review does not silently disappear.
- **Recording completion:** after a user has actually requested and the agent has completed a valid Consistency review, `.github/agents/consistency.agent.md` requires recording that completion as structured evidence in the continuous-improvement register issue (see "Completion evidence" below). That is the sole additional authority this document grants the Consistency agent; it does not extend to synchronizing `CONS-*` findings, remediating content, or any other issue mutation, which remain governed entirely by the existing authorization rules in `consistency-review.md` and `consistency.agent.md`.

### Delivery-process retrospective

- **Trigger:** evidence-based, not calendar-driven — about 25 additional substantive merges since the last retrospective's baseline, or 2 high-confidence post-merge process escapes, or 1 P1 lifecycle/process escape. See the dated retrospective documents (for example `delivery-process-retrospective-2026-09-05.md`) for the method and the exact rerun-trigger language.
- **Purpose:** empirical, dated evidence about the delivery process itself — remediation-round distribution, finding taxonomy, waste classification.
- **Owner:** a human/agent who judges whether the substantive trigger has actually fired. **"Substantive" is not mechanically reducible to an exact count.** The workflow may compute a *raw* merged-PR count since the recorded baseline as a mechanical guard; reaching that raw threshold produces `CHECK_TRIGGER` in the register, not an automatic `DUE` — a human/agent must still determine whether the substantive threshold in the linked retrospective actually fired. The same applies to escape-based triggers: they are judgment-bearing unless explicit, repository-recorded evidence (see `.github/scripts/continuous-improvement-data.json`) identifies a qualifying escape.

## Authority boundaries

Each surface below owns exactly what it says and nothing else. This document is the index; it does not duplicate any of their content.

| Concern | Owning authority |
| --- | --- |
| Session-close Kaizen behavior | `AGENTS.md` |
| How a Consistency review is performed | `.github/agents/consistency.agent.md` |
| Consistency-review operating procedure (human/maintainer side) | `docs/development/consistency-review.md` |
| Empirical, dated process evidence | dated `delivery-process-retrospective-YYYY-MM-DD.md` documents |
| Raw delivery evidence | GitHub PR/review/CI/issue history |
| Current recurring-obligation state and still-unverified interventions | the continuous-improvement register issue (below) |

PR review is a continuous evidence/input loop — every review is a data point the retrospective can later draw on — but it is **not** a fourth scheduled continuous-improvement ceremony. It remains governed entirely by `docs/development/reviewing.md`.

## The continuous-improvement register

A single long-lived GitHub issue titled exactly **`Continuous improvement register`** is the active operational state for these obligations. It is discovered by exact title match (never by a hardcoded issue number, and never confused with a `CONS-*` consistency-finding issue), bootstrapped automatically the first time `.github/workflows/continuous-improvement.yml` runs if it does not already exist, and updated idempotently on every run after that. Finding more than one issue with that exact title is treated as ledger corruption: the workflow fails loudly rather than picking one or creating a duplicate.

The register has two ownership regions, separated by explicit HTML marker comments (`<!-- continuous-improvement:obligations:start -->` / `...:end -->`) so automation can prove it only ever touches its own region:

- **Workflow-managed recurring obligations** (inside the markers) — mechanically derived: the weekly Consistency review's last-verified evidence and `CURRENT`/`DUE`/`OVERDUE` state, and the delivery-process retrospective's raw merged-PR count since baseline and `CURRENT`/`CHECK_TRIGGER` state. Only `.github/workflows/continuous-improvement.yml` (via `.github/scripts/continuous-improvement.mjs`) writes here.
- **Active improvement interventions** (everything outside the markers) — agent/human-owned. Judgment-bearing improvement interventions — proposing one, closing it out, or declaring it verified effective — are never invented, closed, or dispositioned by the scheduled workflow. It preserves this region byte-for-byte (or semantically equivalently) on every update.

The register is **work-in-progress state, not historical storage**. Once an intervention is dispositioned `RETAIN`, `ADJUST`, or `REMOVE`, the detailed evidence belongs in the corresponding retrospective/PR/issue history, not in an ever-growing table on this issue. On initial bootstrap the register is seeded only with the controls/experiments from the 2026-09-05 retrospective that are genuinely still awaiting later verification, with provenance preserved back to that document — not a full history of every past process change.

### Completion evidence

Because the scheduled workflow and agent/human edits must never race over the same state, recurring-obligation *completion* is recorded as append-only structured evidence — a comment on the register issue — rather than a body edit:

```text
Consistency review completed
reviewed head: <full main SHA>
completed at: <UTC timestamp>
mode: <incremental/full/etc.>
```

The workflow derives the managed "last verified" summary from the latest comment matching this structure, but structured syntax alone never confers completion authority: only a comment from a trusted GitHub author association (owner/member/collaborator) counts, and a future-dated `completed at` is rejected even from a trusted author. A workflow run is never itself proof that a review happened — only a valid, authorized, non-future completion comment is. Malformed, unauthorized, or missing evidence is ignored (treated as "never verified"), never fabricated; the workflow never manufactures a completion. Repeated runs with unchanged semantic state update nothing (the rendered "last updated" timestamp is excluded from that comparison), so recurring scheduled runs do not spam the issue with unchanged summaries.

### Retrospective baseline

The retrospective's baseline (currently PR #260, dated 2026-09-05) is operational data in `.github/scripts/continuous-improvement-data.json`, not control flow hardcoded into the workflow or helper. A future verified retrospective advances this file's `baselinePr`/`baselineDate` (and resets `escapeEvidenceCount`/`p1LifecycleEscape`) as an ordinary repository change; the state-derivation logic itself never changes.

## Every-agent reminder

Every Arcogine agent inspects the register once per session and mentions due/overdue work at most once — see the rule in `AGENTS.md`. This is defense in depth: a recurring obligation must not disappear simply because a scheduled notification was missed. It never derails the user's requested task, and it never grants any agent additional authority to act on the register beyond what its own governing contract already allows.

## Non-goals

This model deliberately does not: automatically run the Consistency agent or a formal retrospective; introduce a separate Kaizen agent; become a general process database; track one issue per improvement; require a retrospective after every PR; or depend on any scheduler/service outside `alaiba/arcogine`. Everything required to remember these obligations lives in this repository.
