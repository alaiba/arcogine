# Decision-source internals, requests and replay, and deliberate non-introductions

Part of the [architecture overview](../overview.md#routed-topics); it is mandatory whenever the overview routes your change here.

## Decision-source internals are not world semantics

Goals, beliefs, memory, plans, prompts, behaviour-tree state, planner search state, model weights, and hidden reasoning traces stay **private to the mechanism** that uses them. They do not become shared Arcogine semantics because a decision-maker happens to have them.

Recording a hidden reasoning trace as authoritative causal provenance would manufacture **false provenance** — a stated rationale need not be the operative cause. A voluntarily recorded public rationale, declared policy identifier, commitment, or audit explanation is a legitimate durable record of what a party *asserted*, and must never be presented as evidence of the hidden cause.

Where it is materially relevant, Arcogine must be able to explain **which mechanism(s) were relied on to produce a decision**. That requirement is deliberately weaker than a schema:

- an exact immutable source version is required **only** where a consumer's own reproducibility contract requires it, and must **never be fabricated** for an opaque external participant that cannot supply one;
- where a version does exist, it must be a resolved immutable identifier, never a mutable alias;
- there is **no single `DecisionSourceRef`** and no commitment to a singular source — multi-source, ensemble, recommender-plus-human, and policy-check provenance must remain expressible later;
- a **stateful or online-learning source**, whose material behaviour changes without any version changing, is an explicitly **unresolved** case. Identity plus version may not identify it. Reopen this when a concrete consumer needs such a source to be explainable or replayable; do not force it into an identity+version shape that does not describe it.

## Requests, replay, and the attribution/outcome boundary

**An attributed operation request is the preferred first and common durable boundary** when a decision results in a requested semantic change, and Arcogine does not need a universal shared `Decision` type. This is the narrow form and the only form: a decision and the operation it requests are *not* universally one durable fact. A domain remains free to own a distinct durable recommendation, approval, denial, standing authorization, or exception/risk-acceptance record when a real consumer requires it — [Governance and Conformance](../governance-conformance.md) already names several — and those must not be recast as operation requests for vocabulary symmetry.

Where a decision is durably recorded, it should identify the **observation boundary that bounded it** — what the decision source could actually know at the time — so the decision stays explainable from that input plus the source's own private state. This is what keeps a simulated participant from acting on information it could not have had. The reference itself stays domain-owned; there is no shared cross-domain observation reference, and an Engine observation cursor is not one.

Four replay operations stay distinct:

```text
re-execute the decision source
!= replay the recorded decision
!= replay the requested operation
!= replay the resulting transition
```

The general move for nondeterministic behaviour is to **convert the relevant nondeterministic boundary into durable recorded input** where a consumer's contract requires replayability — the [Determinism Contract](determinism-contract.md#determinism-contract)'s ordered external commands already work this way. Hidden source internals never become replay state.

Attribution and outcome have a two-part rule:

1. **Incidental attribution metadata must never affect outcome.** Attribution attached to a request for provenance must not reach any domain, dispatch, pricing, or simulation computation. If a requester genuinely should influence dispatch, it must appear as a **modelled field in the operation payload**, never be smuggled in through attribution metadata.
2. **Explicitly modelled authority is a legitimate exception.** Actor identity may affect behaviour only through an authority determination that is explicitly modelled, whose outcome is recorded, and which is itself a reproducible input on replay — never through an implicit read of attribution by unrelated domain logic.

This preserves the boundary between provenance and explicit policy input; it does not settle an authorization model. The cross-cutting authority question — *may this actor perform this operation on this subject under the applicable policy?* — is a useful **question shape** for locating where policy belongs. It is not a persisted or public input schema, Arcogine asserts no rule that authorization consults only the current actor, and ownership of reusable actor/capability semantics remains open (see the [Operational execution design](../../research/investigations/operational-execution-design.md) §5, which must not become that owner by default).

## What is deliberately not introduced, and what would reopen it

| Not introduced | Why | Reopen when |
|---|---|---|
| Platform `Agent` type, actor/subject/decision-source value types, actor kind enum | No current consumer proves a shared equality/namespace/lifecycle contract | Multiple concrete consumers demonstrate the *same* such contract (see above) |
| Shared temporally extended `Capability`, procedure, or skill type | The [unit-work decomposition](../engine-semantics.md#3-unit-work-decomposition-semantics) aggregate/child pattern already supplies aggregate intent, child identity, correlation, and a completion rule | A concrete consumer shows that pattern cannot express a real temporally extended capability |
| Agent message bus or conversation ontology | Typed operations, events, observations, results, and explicit public commitments suffice; an accepted order is already such a commitment | A proving case shows ordinary domain interaction cannot represent a required interaction cleanly |
| Agency module, subsystem, or delivery track | The result is cross-cutting semantic distinctions, not a coherent implementation responsibility | A surviving shared contract acquires an owner that no existing module can hold |
| A universal `Decision` record, subject reference, or observation reference | Each is domain-owned today for reasons that differ per domain | A cross-domain consumer needs one contract, not merely one shape |

Each row is a refusal justified by *current* evidence and current consumers, not a permanent prohibition, which is why each states what would reopen it. None of them commits Arcogine to persisted or public identity equality, a shared namespace or lifecycle contract, a permanent closed taxonomy, or cross-module equality semantics, which is why they belong in these attribution and decision boundaries at all. A future change that would introduce any of those, or another hard-to-reverse public or persisted semantic commitment, is a change to Arcogine's architecture: it must be reconciled into the architecture or specification document that would own the new semantics, with its consumers and executable invariants updated in the same change, rather than absorbed into this section as another refusal.

W3C PROV and comparable external models remain **vocabulary donors and outward projection targets**, consistent with the [external representation policy](../external-representations.md) and [Standards Alignment](../standards-alignment.md). They are not Arcogine's domain model, and adopting their vocabulary never imports their ontology.
