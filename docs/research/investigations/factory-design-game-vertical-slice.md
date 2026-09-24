# Factory-Design Game Product Research Programme

> **Status:** Maintained product-research programme; lifecycle belongs to the bounded questions in the research register  
> **Scope:** Coordinate the focused product questions that must be answered before a first playable factory-design consumer is admitted  
> **Authority:** Research/product framing only; this document does not authorize game implementation or change Arcogine semantics

## Product thesis under test

The candidate product loop remains:

```text
Understand a fixed production requirement
        |
        v
Design a factory
        |
        v
Publish the design
        |
        v
Run production
        |
        v
Diagnose the result
        |
        v
Revise and compare
```

The intended player role is a production-system designer or industrial engineer, not a live shift operator. The product is valuable only if the player can form and test a causal model of the production system rather than discover an opaque score formula.

That thesis is deliberately **not one research question**. Capacity, challenge structure, player diagnosis, controlled comparison, spatial consequences, scoring, and implementation technology require different evidence and have different owners. Their lifecycle state is therefore tracked separately.

## Focused questions

### Diagnostic evidence contract — READY

[Factory-design game diagnostic evidence contract](factory-design-game-diagnostic-evidence.md) asks what minimum player-facing evidence contract can be built from supported simulation facts so that every diagnosis is mechanically traceable, its method is explicit, and unsupported causal attribution is refused.

This question is deterministic/fixture-driven. It does not claim to establish population-level player comprehension. A product-owner walkthrough is a qualitative smoke test only: failure can reject a candidate, while success cannot establish what independent players will understand.

### Non-spatial challenge strategy space — READY

[Factory-design game strategy space](factory-design-game-strategy-space.md) asks whether a deliberately small fixed production challenge, using current production/capacity semantics plus game-owned capital costs, can produce several materially different and explainable viable interventions without relying on spatial layout.

This question is headless and product-system focused. It is intentionally separated from presentation and from spatial transfer so that Arcogine can first determine whether capacity plus capital already creates a useful design problem.

### Controlled retry explanation — CANDIDATE

Question:

> Can a controlled one-variable retry be presented as a mechanically traceable before/change/after evidence chain without asserting an unobserved causal mechanism?

Promote this only after the diagnostic-evidence and strategy-space investigations establish an evidence contract and a challenge family worth comparing. This remains a product-explanation question; reusable run-comparison derivations still belong to the analytics-boundary investigation.

### Spatial design trade-off — CANDIDATE

Question:

> Once transfer semantics and spatial runtime consequences are settled, does changing spatial arrangement create a legible performance trade-off against capacity and game-owned capital cost?

This is not ready while Arcogine is still deciding whether transfer lifecycle/timing is semantically independent of spatial layout. It must consume the result of [Transfer semantics boundary research](transfer-semantics.md) and any reconciled spatial-runtime contract rather than using the game to decide them.

### External player validation — CANDIDATE

Question:

> When independent target users become available, does the promoted diagnostic presentation let them correctly diagnose the reference challenge without repository/runtime knowledge or expert guidance?

This is the place for actual population-level comprehension evidence. It is intentionally not `READY` because no independent participant pool is currently available and the first internal playable slice does not need to make a population-level usability claim.

Promote it when either independent target users are available or product/release direction explicitly requires a claim about new-player comprehension, accessibility, onboarding, or diagnostic success. At promotion time, define the target population, tasks, falsification thresholds, and decision at stake. Reuse the deterministic fixture corpus from the diagnostic-evidence study where appropriate, but do not treat the product owner, an AI model, or repository reviewers as substitutes for the target population.

### Scoring, challenge and level structure — CANDIDATE

The maintained register separately tracks:

> What scoring, challenge, and level structures create several understandable viable strategies without an opaque dominant meta?

Do not promote it until the strategy-space investigation provides actual viable solution families and the product has evidence about what should be rewarded.

## Boundary with reusable analytics

Which *facts and reusable derivations* exist, and who owns them, is [Simulation analytics consumer boundary](simulation-analytics-consumer-boundary.md), an independent High-risk investigation.

Accordingly:

- overlays, timelines, callouts, wording, tutorial sequencing, and progressive disclosure remain game presentation choices;
- the game must not invent shared KPI or diagnostic formulas while analytics ownership is unresolved;
- a research-local derivation may be used to establish ground truth for a study, but selecting it for product use does not make it game-owned or part of Engine semantics;
- transfer-dependent diagnostics remain outside the READY diagnostic-comprehension study until transfer semantics are reconciled and executable evidence exists.

## Boundary with transfer and spatial semantics

Transfer is a production/runtime concern, not a game mechanic that the consumer may define. The current normative contracts couple transfer timing to the optional spatial record; [Transfer semantics boundary research](transfer-semantics.md) now tests whether that coupling is semantically justified before spatial activation proceeds.

The game therefore must not assume that:

- absence of spatial facts necessarily means absence of transfer lifecycle;
- placement is behaviorally consequential before a landed Engine contract makes it so; or
- a game-local movement formula can stand in for missing Factory/Engine semantics.

## Product/implementation choices that are not research by default

Rendering/input technology, sidecar versus in-process packaging, save-wrapper shape, and similar implementation choices do not belong in one standing research brief merely because they are undecided. They become research only when a bounded material uncertainty can change a product or implementation decision and has explicit evidence and exit criteria. Otherwise they remain deferred consumer implementation choices until a promoted requirement makes the decision concrete.

Draft-to-canonical projection UX, visual interpolation, scoring presentation, and tutorial sequencing may become focused product studies when the selected product requirement makes them material. They are not prerequisites merely because they are imaginable.

## Shared Arcogine boundary

Research must not use game pressure to invent shared semantics. In particular:

- game catalogue prices, availability, score, progression, tutorial state, and presentation remain game-owned;
- canonical executability remains Arcogine-owned;
- production workload, dispatch, queues, processing, transfers, time, and authoritative performance facts remain Arcogine-owned;
- reusable analytics ownership remains with the analytics-boundary investigation;
- game attempt snapshots may explain player-authored differences without requiring a new canonical model-diff abstraction;
- unresolved equipment ontology, transfer semantics, spatial evolution, and richer design comparison remain separate research questions.

## Superseded predecessor

The register question *"What player-facing evidence best exposes bottlenecks and causal performance differences?"* is **SUPERSEDED** rather than `CONCLUDED`: it combined two decisions with different owners.

1. **technical contract/ownership** — which facts and temporal evidence are required, which derivations are safe, and where reusable derivations belong;
2. **product cognition** — what a player must see to form the correct causal model.

The investigation made substantial progress on the first and explicitly did not validate the second, which requires participant evidence.

The first half is **not game-local**. At that investigation's baseline, Arcogine carried generic KPI computation, an outward KPI endpoint and snapshot projection, and a web consumer that retained KPI history and computed baseline-to-baseline metric deltas. That historical evidence established multiple consumers of reusable derived measurement at the time; later consumer retirement does not make those implementations current dependencies, but it does invalidate the old premise that a future second consumer was needed before asking the ownership question.

Consequently:

- reusable technical evidence, proving cases, and truthfulness constraints transferred to [Simulation analytics consumer boundary](simulation-analytics-consumer-boundary.md);
- the provisional conclusion that reusable diagnostic derivations are **game-owned** is explicitly superseded;
- the immediately actionable product question is now the focused [diagnostic-evidence contract](factory-design-game-diagnostic-evidence.md), which can establish truthfulness/traceability without a participant pool; population-level comprehension remains the separate external-player-validation candidate above; and
- the broad vertical-slice question itself is superseded by the focused portfolio recorded here and in the research register.

## Promotion to playable implementation

This programme does not have one `CONCLUDED` state. Playable implementation may be admitted only when the implementation gate can name the exact focused research conclusions required by the chosen first slice, the required Arcogine semantics are landed, and executable acceptance tests can be written.

A minimal non-spatial **internal playable** slice would currently require, at least, a concluded challenge-strategy question and a concluded diagnostic-evidence question. It does not require independent human participants merely to exist as an internal playable slice. A later release goal that claims broad player comprehension must separately promote and satisfy external-player validation. A slice that makes layout consequential additionally requires the transfer/spatial questions that own that behavior. Other candidate questions are promoted only if the selected slice actually depends on them.
