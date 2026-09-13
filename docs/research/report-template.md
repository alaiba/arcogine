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
- **Adversarial-review status**, if applicable — state the status that is true for this exact report revision when it is committed: `not yet required` (low/medium risk), `required, not yet performed`, or `self-administered only`. Do not amend an already reviewed report merely to add the later independent-review link or disposition; the adversarial-review artifact and any reconciliation PR must instead identify this report's exact commit SHA. A later report commit is a distinct evidence revision and does not inherit the prior disposition automatically.

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

For a write-up still awaiting independent review, distinguish the author's own self-challenge from a later independent pass — do not present a self-administered check with the weight of independent review. Once an independent adversarial review exists, keep this reviewed report revision unchanged: the review artifact records this report's exact commit SHA and its disposition (`ACCEPT`, `ACCEPT WITH QUALIFICATIONS`, `MORE EVIDENCE REQUIRED`, or `REOPEN` — see `docs/development/researching.md` §9), and the later reconciliation references both exact evidence coordinates. If the report itself is revised after review, that new commit is a distinct report revision and, where independent review is required for promotion, must receive its own applicable adversarial review before its conclusions are promoted.

Where the report proposes that a result may transfer beyond its immediate Arcogine context, explicitly attack that generalization too: look for an Arcogine-specific assumption that actually causes the result, a plausible counter-context where the broader claim fails, evidence of a useful design choice being presented as a necessary property, or a claim whose scope is stronger than the evidence. Prefer narrowing a transferability claim to the smallest version that survives over defending a broader formulation.

## Surviving invariants

The smallest cross-case semantic rules the investigation actually established — not a restatement of every proving case's outcome, and not generalized beyond what the cases actually cover.

## Transferability and reuse

Optional. Use this section only when the investigation produced a result or research asset that may be useful outside the immediate Arcogine question. Do not add it merely to make a report look broader or more publishable.

For each material candidate for reuse, distinguish:

- **Arcogine-specific dependency** — which repository architecture, assumptions, consumers, or constraints the result depends on;
- **potentially transferable result** — the narrowest claim that might remain useful when those implementation details are removed;
- **boundary conditions / counter-contexts** — where the broader claim should not be expected to hold, including contrary examples when known;
- **evidence level** — whether the broader claim is established by this investigation, supported but not established by external evidence, or only a hypothesis suggested by the result;
- **reusable research asset** — any proving case, counterexample, failure mode, benchmark/scenario, trace, experimental or playtest protocol, measurement method, falsification criterion, source map, or implementation know-how worth carrying forward;
- **strengthening evidence** — what independent evidence, replication, second consumer/domain, or contrary-case testing would be needed before making a broader claim confidently.

Do not silently promote `works for Arcogine` into `general principle`. A potentially transferable result remains research evidence until independently supported at the broader scope. This section does not create a publication lifecycle, publication candidate status, or obligation to preserve the whole report.

## What did not survive

Candidates, assumptions, or prior framings that the proving cases or evidence ruled out, and why. This is useful negative evidence for future researchers; do not omit it merely because the write-up already states a positive conclusion. When forgetting a failed hypothesis, broken analogy, misleading metric, or counterexample would plausibly cause future research to repeat the same mistake, identify the reusable negative knowledge explicitly so reconciliation can preserve it in the narrowest appropriate surface.

## Confidence and limitations

How confident the conclusion is, and what evidence would change it. State any repository or external surface that could not be inspected.

## Unresolved unknowns

What remains genuinely open after this investigation, distinct from what the investigation deliberately excluded as non-goal.

## Durable consequences

The smallest consequences that may deserve reconciliation into: no action; product; architecture; ADR; reference; implementation responsibility. State them — do not perform that promotion by writing this section; the actual ADR/architecture/planning change is a separate, independently reviewed change.

Also identify any reusable research assets or negative knowledge from `Transferability and reuse` / `What did not survive` that would change future reasoning or validation if lost. The later knowledge-transfer audit should preserve the asset in the durable surface that will need it, or make an explicit discard decision; it should not retain the whole report merely because the report contains potentially reusable material.

## Implementation implication

What this means for implementation, including explicitly stating "no implementation" when that is the honest answer. Do not admit implementation planning from within a research write-up.

## Follow-up triggers

What future evidence, consumer, or event should reopen or extend this question, and where that follow-up should be tracked (normally a new or updated entry in `docs/research/README.md`).

## Sources

Enough precision to re-check every load-bearing piece of external evidence — not a bibliography padded with sources that did not actually change the conclusion.
