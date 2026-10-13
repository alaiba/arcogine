# Factory-Design Game Vertical-Slice Implementation Gate

> **Status:** BLOCKED — the product direction for the first non-spatial playable slice is selected, but no slice is admitted until its requirement set, integration and projector boundary, any named diagnostic method, prerequisites and executable acceptance tests are settled  
> **Scope:** The single implementation-admission boundary for the first playable game slice  
> **Authority:** Planning only

## Current blocker

The owner has selected the direction of the first slice: a small, **non-spatial** factory-optimization game in which the player designs a production system, tests it, learns why it behaves as it does, and improves it. [Game product requirements](../product/factory-design-game.md) record that direction and the player-facing diagnostic requirements every slice must meet.

The non-spatial product-research gates are satisfied: [strategy space](../research/investigations/factory-design-game-strategy-space.md) and [diagnostic evidence](../research/investigations/factory-design-game-diagnostic-evidence.md) are both **CONCLUDED**, with bounded results and qualifications recorded in the [research register](../research/research-register.md). Neither result admits implementation.

What remains before a concrete slice can be admitted:

- the slice's **requirement set**: which challenge it offers, which diagnostics it shows, and how it compares one attempt with an improved one, drawn from the selected direction and the promoted requirements;
- an accepted named Game method for any utilization or starvation measure the slice shows;
- the consumer integration and projector boundary;
- the Engine/Factory prerequisites the requirement set actually selects;
- executable acceptance tests; and
- any other product decision the requirement set genuinely depends on.

This file creates no concrete slice.

Controlled retry explanation, scoring/level structure, external-player validation, and other candidate product questions are additional gates only if the requirement set actually depends on them. External-player validation is not required for an internal playable slice unless product/release direction explicitly requires a population-level comprehension claim.

The diagnostic research studied static recorded runs with pre-computed variants. A slice whose live interaction issues mid-run commands must first meet the [execution-evidence reopening trigger](../research/investigations/simulation-execution-account.md#reopening-triggers) for live-loop control. Static or start-fixed playback at any speed does not trigger it. This gate selects no live loop, Engine change or UI technology.

Because the selected slice is non-spatial, spatial runtime consequences are **not** among its prerequisites. A later slice that makes **layout** behaviorally consequential depends on the executable spatial-runtime work in [Spatial Runtime Consequences](spatial-runtime-consequences.md), which consumes the already-reconciled [transfer-applicability contract](../architecture/transfer-applicability.md); no separate Engine-identity or applicability research gate remains.

A named **Game-local** diagnostic method enters a requirement set only through the [method acceptance gate](../product/factory-design-game.md#2-diagnostic-claims-and-evidence), under the [Game-owned analytical method](../product/factory-design-game.md#3-game-owned-analytical-methods) rules; a genuinely shared definition follows the [Domain authority](../architecture/overview.md#domain-authority) triggers instead.

## Upstream prerequisites

Playable/runtime-integrated implementation requires the upstream prerequisites in the [Game consumer plan](factory-design-game-consumer.md#3-upstream-implementation-prerequisites): the settled canonical model seam, deterministic workload/dispatch/session/event contracts, and every Factory/Engine capability selected by the requirement set.

Headless Challenge capability remains independently usable and does not by itself satisfy playable entry criteria.

## Admission criteria

Replace this gate with a concrete implementation slice only when all of the following are true:

1. the slice's requirement set is written down, follows the selected product direction, and does not depend on another unresolved product question;
2. every focused research question that requirement set depends on is **CONCLUDED** and has promoted an explicit bounded requirement or an explicit no-action/falsification result;
3. the Factory/Engine prerequisites required by those exact requirements are landed;
4. the consumer integration boundary is explicit, including how the projector fixes result-affecting configured-resource identity and order;
5. every player-facing diagnostic the slice shows meets [Diagnostic claims and evidence](../product/factory-design-game.md#2-diagnostic-claims-and-evidence), including an accepted named Game method for any utilization or starvation measure, and any truly shared method meets the architecture's concrete definition-owner trigger;
6. any selected save/recovery requirement has a supported Arcogine contract or is explicitly excluded from the slice;
7. game-only semantics stay outside Arcogine, and implementation ownership is clear; and
8. executable acceptance tests can be written against supported Arcogine contracts, rather than internal implementation state, before coding starts.

Until then this file intentionally contains no reference-level parameters, scoring formula, UI technology choice, tutorial sequence, or other product hypothesis.
