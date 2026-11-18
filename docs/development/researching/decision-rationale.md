# Historical decision rationale

Part of the [research operating model](../researching.md); it is mandatory whenever the entry point routes your task here.


A **historical decision-rationale record** is a concise, dated, non-normative account of why a significant reconciled choice was made: the serious alternatives, the trade-offs that actually decided it, and the changed conditions that would justify revisiting it. It lets that reasoning be retrieved deliberately after the temporary evidence that produced it retires, without making canonical documents carry alternative-analysis narrative and without restoring a decision-record authority. Research reconciliation is the normal producer; a significant architectural change reconciled without a research investigation applies the same rules. Records live under `docs/history/decisions/` (see [Historical evidence](../../history/README.md)). Git and pull-request history remain delivery-history evidence of what changed and how it was reviewed; a record adds deliberate retrieval for selected high-value rationale and may cite the reconciliation pull request as its provenance.

**Authority.** A historical decision-rationale record cannot introduce, extend, override, or repair a current requirement. Any constraint, qualification, behavior, identity rule, or support obligation that still governs Arcogine must be present in its owning canonical document or executable contract, and current architecture and specifications must remain sufficient without reading any record. Therefore:

- a record is never implementation, review, or reconciliation authority — "the newest record says so" establishes nothing;
- editing a record changes no current meaning, and a record that differs from current architecture is historical evidence, not documentation drift by itself;
- a canonical document keeps any explanation whose removal would make a current rule ambiguous, unsafe to implement, or easy to misapply, while a record holds explanation whose primary purpose is why one option was selected over another, which trade-off was knowingly accepted, which assumptions were contingent at the time, and what evidence would justify choosing differently later.

**Retention test.** Retain a record only when losing the rationale would materially increase the chance that a future maintainer would:

- repeat substantial investigation;
- re-open a deliberately rejected alternative without knowing the decisive trade-off;
- mistake a contingent choice for a universal truth;
- accidentally undo a constraint whose motivation is not apparent from the current contract; or
- misunderstand why a deliberately narrower solution was chosen over a more general one.

A record is most clearly warranted when several of these hold: the decision followed substantial research or independent adversarial review; two or more serious alternatives remained viable; the selected option knowingly accepted a non-obvious trade-off; the decision is foundational or hard to reverse; future evidence can reasonably trigger reconsideration; the canonical result is much shorter than the reasoning needed to understand the choice. A record is usually not warranted for ordinary local implementation choices, straightforward refactors, rationale that is obvious and fully captured by current code, tests, and documents, temporary delivery sequencing, research material with no surviving decision relevance, interesting but non-decisive analysis, or content already adequately preserved as a durable proving case, synthesis seed, or retained artifact. "No rationale record" is a valid and normal audit outcome; a record is never a prerequisite for reconciliation, review, or merge.

**Boundaries with neighboring surfaces.** Keep each piece of reasoning in the one surface whose purpose it serves rather than repeating the same narrative across several:

| Surface | Holds | Does not hold |
|---|---|---|
| Canonical architecture or specification | Current requirements and semantics, with the concise rationale needed to interpret and apply them | A chronological decision log or alternative-analysis narrative |
| Historical decision-rationale record | Project-specific reasoning behind one significant reconciled choice | Current requirements, lifecycle state, or a copied report |
| Retained research artifact under `docs/research/investigations/` | Analysis or evidence whose exact or substantial content stays independently valuable beyond a decision summary | A substitute for the canonical result |
| Synthesis seed | A compact, potentially transferable signal for recognizing recurrence across investigations | An account of why Arcogine chose one option |
| Research register | Current portfolio state, verdicts, and reopening triggers | A rationale archive |

**Record content.** Name the file `YYYY-MM-DD-<semantic-slug>.md`, dated by when the choice was reconciled; there is no global sequence number, index file, or registry. Keep the record short — it is not an archived research report. Use this shape, omitting any section that does not apply:

```markdown
# <Decision title>

> **Date:** YYYY-MM-DD
>
> **Authority:** Historical, non-normative evidence; current meaning lives only in the documents it was reconciled into
>
> **Reconciled into:** <canonical document(s) that received the decision>

## Decision
## Context
## Serious alternatives
## Decisive rationale
## Consequences and accepted trade-offs
## Reconsider when
## Provenance
```

- **Decision** — a short historical statement of what was chosen at the time.
- **Context** — only the constraints that materially shaped the choice.
- **Serious alternatives** — the credible alternatives and concisely why each was not selected, saying plainly whether it was falsified or merely not justified by the evidence then available.
- **Decisive rationale** — the small set of facts or trade-offs that actually discriminated the choice.
- **Consequences and accepted trade-offs** — what the choice made easier or harder and what it deliberately left unresolved.
- **Reconsider when** — concrete evidence or changed conditions that would materially weaken the original rationale. This explains the record; it neither reopens research nor creates work. A trigger that should actively reopen an Arcogine question also belongs in the research register.
- **Provenance** — the merged reconciliation pull request or another delivery-history provenance reference, plus links to any deliberately retained evidence.

A record must be understandable without reconstructing the cited pull request's discussion. It must not carry a status or acceptance field, approval lifecycle, sequence number, copied report or source dump, restated current specification text, temporary delivery coordinates, or workspace `commit SHA + path` coordinates presented as preserved evidence; a research artifact that must itself stay readable is promoted under the artifact-promotion rule in [workspace retirement](workspace-retirement.md) instead.

**Later change.** When a later decision changes an earlier one, normally add a new dated record and cross-link the two rather than rewriting the earlier rationale to match the present. Small factual or link corrections are fine when they do not falsify the historical account. Retention is prospective: do not reconstruct records for past decisions wholesale; capture an older decision only when doing so has concrete current value.
