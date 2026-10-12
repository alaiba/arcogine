# Factory-Design Game Vertical-Slice Implementation Gate

> **Status:** BLOCKED — playable implementation is not admitted until the focused product questions required by the selected slice conclude and upstream runtime gates are satisfied  
> **Scope:** Preserve the implementation-admission boundary for the first playable game slice without turning the product-research programme into one pseudo-question  
> **Authority:** Planning only

## Current blocker

The former READY “vertical-slice research question” has been superseded by the focused portfolio in [Factory-Design Game Product Research Programme](../research/investigations/factory-design-game-vertical-slice.md).

For the smallest **non-spatial** playable slice, the product evidence is:

- [Factory-design game strategy-space research](../research/investigations/factory-design-game-strategy-space.md) — **CONCLUDED; this gate is satisfied.** Current capacity semantics plus game-owned capital constraints support a bounded, mechanically non-trivial reference challenge. It promoted bounded reference-challenge requirements (including that a future projector must make result-affecting resource identity/order explicit and deterministic), not an implementation slice. Its qualifications bound how it may be used: shared eligibility is a strong constructive mechanism and not a proven necessity, and mechanical ground truth is not player comprehension; and
- [Factory-design game diagnostic-evidence research](../research/investigations/factory-design-game-diagnostic-evidence.md) — **READY, and the remaining product blocker.** It asks whether the product can expose a mechanically truthful, inspectable diagnostic evidence contract without unsupported inference.

The concluded result supplies challenge content, not a diagnostic contract, an integration boundary or acceptance tests, so it does not admit implementation. The diagnostic-evidence question must first reach a decision-quality conclusion and promote a bounded requirement set. No playable implementation is admitted, and this file does not create a concrete slice.

Controlled retry explanation, scoring/level structure, external-player validation, and other candidate product questions are additional gates only if the selected first slice actually depends on them. External-player validation is not required for an internal playable slice unless product/release direction explicitly requires a population-level comprehension claim.

A slice that makes **layout** behaviorally consequential has an additional upstream dependency on the executable spatial-runtime prerequisite selected in [Spatial Runtime Consequences](spatial-runtime-consequences.md): the current Engine refuses present spatial content until that plan makes it executable. It consumes the already-reconciled [transfer-applicability contract](../architecture/transfer-applicability.md), the current [Factory model](../architecture/factory-model.md) and [Engine semantics](../architecture/engine-semantics.md); no separate Engine-identity or applicability research gate remains. New research is added only if a concrete new semantic uncertainty is admitted.

A named **Game-local** diagnostic method is permitted by the concluded
[analytics-definition ownership boundary](../architecture/overview.md#domain-authority), but can
enter a playable requirement set only after the separately **READY** Game diagnostic-evidence
question concludes and its actual methods, proof/refusal and product requirements are admitted.
For a shared named definition, apply the concrete architecture admission triggers instead of
reimplementing it locally; any additional input, method-proof, provenance, accumulation or
retention need evaluates the still-[CANDIDATE evidence/provenance
question](../research/investigations/simulation-analytics-evidence-provenance.md). Adapter and
cross-revision questions have their own independent triggers. A fresh snapshot's `no currently
known work` is not an interval aggregate or `no work left for the rest of the period`; the latter
claims require distinct finality and future-input evidence. The separate product gate, Engine
session/control requirements, and unrelated headless Challenge use are unaffected.

## Upstream prerequisites

Playable/runtime-integrated implementation additionally requires the game-consumer entry gates in [Factory-Design Game Consumer Initiative](factory-design-game-consumer.md): the settled canonical model seam, deterministic workload/dispatch/session/event contracts, and every Factory/Engine capability selected by the promoted requirements.

Spatial runtime consequences are **not** a universal first-slice prerequisite anymore. They are required only if the promoted product slice includes spatial layout as a behaviorally consequential mechanic.

Headless Challenge capability remains independently usable and does not by itself satisfy playable entry criteria.

## Admission criteria

Replace this gate with a concrete implementation slice only when all of the following are true:

1. every focused research question required by the selected slice is **CONCLUDED** and has promoted an explicit bounded requirement or an explicit no-action/falsification result;
2. the promoted requirements form a coherent first slice without depending on another unresolved product question;
3. upstream Engine/Factory prerequisites required by those exact requirements are landed;
4. any selected named Game diagnostic has admitted requirements and sufficient claim-specific input/proof/refusal rules, and any truly shared method meets the architecture's concrete definition-owner trigger;
5. the exact consumer integration boundary is known, including how a projector fixes result-affecting configured-resource identity and order;
6. implementation ownership is clear; and
7. executable acceptance tests can be written before coding starts.

Until then this file intentionally contains no reference-level parameters, scoring formula, UI technology choice, tutorial sequence, or other product hypothesis.
