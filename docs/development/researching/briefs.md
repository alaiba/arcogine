# 1. What research is for

Part of the [research operating model](../researching.md); it is mandatory whenever the entry point routes your task here.


Arcogine research exists to answer a **material bounded uncertainty that can change an Arcogine decision** — a question whose answer would plausibly change product direction, architecture, a specification, or an implementation contract, and whose current uncertainty is real enough that guessing would be worse than investigating.

A question that cannot change any decision is not a research question; it is either already answered by current architecture/product docs, or it is genuine curiosity that does not belong in `docs/research/`.

A researcher must not turn an insufficiently bounded question into an encyclopedia. If a brief is too broad to falsify or too vague to exit, the correct action is to narrow the brief (or return it to `CANDIDATE` under the [lifecycle](lifecycle.md)), not to produce a sprawling survey.

## What a bounded research brief normally identifies

A `READY` brief should normally state:

- the exact question;
- the decision at stake if the question is answered;
- scope, and explicit non-goals;
- a current/simple/no-new-abstraction candidate, where one plausibly exists;
- alternative hypotheses or candidate models to be tested against each other;
- proving cases the candidates must survive;
- evidence expectations (what would make the answer decision-quality);
- falsification conditions, where the question is the kind that can be falsified;
- exit criteria;
- the expected durable destination if the question is settled (no action, product, architecture/specification, process policy, or implementation responsibility).

A brief missing one of these because it genuinely does not apply to that question is fine. A brief missing several of these is not yet `READY`: an independent researcher should be able to execute it from the brief's stated evidence and exit criteria without first re-deriving the question.

External-domain material belongs in a report only when it changes or tests an Arcogine-specific hypothesis, proving case, boundary, inference, confidence assessment, or decision. A summary of an outside field that does not do one of those things does not belong in the report, however interesting.

## Implementability versus architectural suitability

Demonstrating that desired behavior can be assembled from existing APIs establishes
**implementability**, not that the current responsibility boundary is **architecturally suitable**.
This matters when consumers must coordinate multiple calls to reproduce producer-owned invariants,
make completeness or safety assertions, or independently implement the same protocol.

For a material ownership or cross-boundary decision, compare a feasible current-API/consumer
solution with a proportionate change to the producer or owning contract **when that alternative
could change the answer**. Test what each can actually guarantee, what failures become impossible
rather than merely detectable, consumer coordination and duplication, producer complexity,
compatibility, and future reuse. Neither a working wrapper nor an attractive new API settles
responsibility placement on its own.

Retaining the existing boundary is a valid conclusion, but give an affirmative comparative
reason and any material reopening trigger rather than citing feasibility alone. When the question
has no meaningful shared-semantic or ownership consequence, briefly explain why a producer-side
alternative is unnecessary; do not launch speculative redesign for every local convenience.
Apply the comparison proportionately to the risk (§7).
