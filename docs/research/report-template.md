# Research report template

> This is a reusable structure, not a form to fill in mechanically. Omit any section that does not apply to the question rather than padding it — see [`docs/development/researching.md`](../development/researching.md)'s stopping rule and risk-proportionate depth guidance. A lower-risk local question may need only a fraction of these sections; a high-risk cross-domain question may need all of them plus an independent adversarial review before it counts as decision-quality evidence.
>
> Copy the structure below into the new report file under `docs/research/`; delete this note and any section headers that do not apply to the question at hand.

---

## Header / authority

State at minimum:

- **Title** — name the question, not the delivery coordinate that tracked it.
- **Research status** — one of `docs/research/README.md`'s lifecycle values. `READY` means the question is bounded and ready to start, not that work is in progress: once evidence gathering or synthesis has begun (including drafting this report), the investigation is `ACTIVE`, and stays `ACTIVE` until the existing `CONCLUDED` conditions in `docs/research/README.md` are actually satisfied.
- **Research baseline** — the exact live `main` SHA the investigation was grounded against.
- **Later reconciliation baseline**, if this write-up is revisited against a newer `main` before durable reconciliation — state both explicitly rather than silently updating the original baseline.
- **Authority statement** — this document is research evidence only; it is not accepted architecture, product direction, or implementation commitment until a separate reconciliation change promotes it.
- **Adversarial-review status**, if applicable — `not yet required` (low/medium risk), `required, not yet performed`, `self-administered only`, or a link to the independent adversarial review artifact and its disposition.

## Question

The exact bounded question this write-up answers. If the question needed sharpening from its brief, say what changed and why.

## Decision at stake

What Arcogine decision this question's answer would actually change. If nothing concrete would change, say so — that is itself a finding.

## Scope and non-goals

What is in scope, and what is explicitly excluded so the write-up does not silently expand into an adjacent question.

## Executive conclusion

The answer, stated plainly, before the supporting analysis. State confidence here too, not only in a buried "Confidence" section.

## Repository evidence

What the current repository actually establishes, labeled `Repository fact` where useful for clarity. Distinguish Accepted ADRs from Proposed ones, landed capability from planned/deferred, and current architecture from aspirational direction.

## Candidate models / hypotheses

The alternative answers under test, including a current/simple/no-new-abstraction candidate where one plausibly applies. State each candidate's shape before evaluating it against proving cases — do not present only the winning candidate.

## External evidence

Only evidence that materially changes or tests an Arcogine-specific hypothesis, proving case, boundary, inference, confidence, or decision — see `docs/development/researching.md` §5. For each source, capture:

- what it establishes and where the analogy breaks, if it is an analogy;
- provenance precise enough to re-check (issuing body/designation/part/edition/year/locator for a standard; version/date for product documentation);
- verification status — fetched and checked during this investigation, or background knowledge not yet verified (label explicitly; do not let an unverified memory carry a load-bearing conclusion).

## Proving cases

The concrete scenarios used to discriminate between the candidate models, derived from this question's own candidates and failure modes rather than reused wholesale from another investigation. For each case, state which candidates survive and which fail, and why.

## Adversarial analysis

For a write-up still awaiting independent review, distinguish the author's own self-challenge from a later independent pass — do not present a self-administered check with the weight of independent review. Once an independent adversarial review exists, link it here and state its disposition (`ACCEPT`, `ACCEPT WITH QUALIFICATIONS`, `MORE EVIDENCE REQUIRED`, or `REOPEN` — see `docs/development/researching.md` §9) rather than duplicating its content.

## Surviving invariants

The smallest cross-case semantic rules the investigation actually established — not a restatement of every proving case's outcome, and not generalized beyond what the cases actually cover.

## What did not survive

Candidates, assumptions, or prior framings that the proving cases or evidence ruled out, and why. This is useful negative evidence for future researchers; do not omit it merely because the write-up already states a positive conclusion.

## Confidence and limitations

How confident the conclusion is, and what evidence would change it. State any repository or external surface that could not be inspected.

## Unresolved unknowns

What remains genuinely open after this investigation, distinct from what the investigation deliberately excluded as non-goal.

## Durable consequences

The smallest consequences that may deserve reconciliation into: no action; product; architecture; ADR; reference; implementation responsibility. State them — do not perform that promotion by writing this section; the actual ADR/architecture/planning change is a separate, independently reviewed change.

## Implementation implication

What this means for implementation, including explicitly stating "no implementation" when that is the honest answer. Do not admit implementation planning from within a research write-up.

## Follow-up triggers

What future evidence, consumer, or event should reopen or extend this question, and where that follow-up should be tracked (normally a new or updated entry in `docs/research/README.md`).

## Sources

Enough precision to re-check every load-bearing piece of external evidence — not a bibliography padded with sources that did not actually change the conclusion.
