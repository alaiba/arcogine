# Factory-Design Game Vertical-Slice Implementation Gate

> **Status:** BLOCKED — playable implementation is not admitted until the focused product questions required by the selected slice conclude and upstream runtime gates are satisfied  
> **Scope:** Preserve the implementation-admission boundary for the first playable game slice without turning the product-research programme into one pseudo-question  
> **Authority:** Planning only

## Current blocker

The former READY “vertical-slice research question” has been superseded by the focused portfolio in [Factory-Design Game Product Research Programme](../research/investigations/factory-design-game-vertical-slice.md).

For the smallest **non-spatial** playable slice, the currently required product evidence is:

- [Factory-design game strategy-space research](../research/investigations/factory-design-game-strategy-space.md) — whether current capacity semantics plus game-owned capital constraints produce a useful design problem; and
- [Factory-design game diagnostic-evidence research](../research/investigations/factory-design-game-diagnostic-evidence.md) — whether the product can expose a mechanically truthful, inspectable diagnostic evidence contract without unsupported inference.

Neither question authorizes implementation merely by being `READY`; each must first reach a decision-quality conclusion and promote a bounded requirement set.

Controlled retry explanation, scoring/level structure, external-player validation, and other candidate product questions are additional gates only if the selected first slice actually depends on them. External-player validation is not required for an internal playable slice unless product/release direction explicitly requires a population-level comprehension claim.

A slice that makes **layout** behaviorally consequential has an additional upstream semantic dependency: the READY [transfer-lifecycle research](../research/investigations/transfer-semantics.md#ready--transfer-lifecycle-independence), any resulting Factory/Engine reconciliation, the subsequently re-bounded Engine-applicability question, and the spatial runtime work selected after those results.

If promoted playable requirements need generic diagnostics — reusable utilization/occupancy measurement, longitudinal aggregation, bottleneck inference, or run-to-run comparison — they must consume a resolved shared analytics boundary ([Simulation analytics consumer boundary](../research/investigations/simulation-analytics-consumer-boundary.md)) rather than implement those semantics locally in the game. This constrains only requirements that actually depend on reusable derived measurement; unrelated headless Challenge capability remains independently usable.

## Upstream prerequisites

Playable/runtime-integrated implementation additionally requires the game-consumer entry gates in [Factory-Design Game Consumer Initiative](factory-design-game-consumer.md): the settled canonical model seam, deterministic workload/dispatch/session/event contracts, and every Factory/Engine capability selected by the promoted requirements.

Spatial runtime consequences are **not** a universal first-slice prerequisite anymore. They are required only if the promoted product slice includes spatial layout as a behaviorally consequential mechanic.

Headless Challenge capability remains independently usable and does not by itself satisfy playable entry criteria.

## Admission criteria

Replace this gate with a concrete implementation slice only when all of the following are true:

1. every focused research question required by the selected slice is **CONCLUDED** and has promoted an explicit bounded requirement or an explicit no-action/falsification result;
2. the promoted requirements form a coherent first slice without depending on another unresolved product question;
3. upstream Engine/Factory prerequisites required by those exact requirements are landed;
4. any reusable analytics requirement has a settled ownership/input contract;
5. the exact consumer integration boundary is known;
6. implementation ownership is clear; and
7. executable acceptance tests can be written before coding starts.

Until then this file intentionally contains no reference-level parameters, scoring formula, UI technology choice, tutorial sequence, or other product hypothesis.
