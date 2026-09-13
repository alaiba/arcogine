# Factory-Design Game Consumer Implementation Plan

> **Status:** BLOCKED for playable/runtime-integrated implementation; headless Challenge capability is already available independently  
> **Scope:** Settled Arcogine/game ownership, upstream prerequisites, implementation admission, and integration acceptance for a separate factory-design game consumer  
> **Authority:** Planning only; product hypotheses and unresolved consumer choices live in research

## 1. Consumer boundary

The game consumes Arcogine as the authority for published production-system semantics and deterministic runtime consequences.

The game owns presentation and player-facing authoring. Arcogine owns production truth.

```text
Game-owned draft
      |
      v
challenge admissibility
      |
      v
project supported canonical semantics
      |
      v
Arcogine validate + publish
      |
      v
FactoryModelVersion
      |
      v
Arcogine runtime
      |
      v
supported observations / runtime events
      |
      v
game presentation and attempt evaluation
```

Challenge admissibility and Arcogine executability remain separate decisions. Passing one never implies passing the other.

## 2. Product-research dependency

The playable requirement set is not yet an implementation input. It is being tested in [Factory-Design Game Vertical-Slice Research](../research/factory-design-game-vertical-slice.md).

Rendering/input technology, scoring, tutorial sequence, save-wrapper shape, interpolation policy, and other evidence-dependent consumer choices must not be selected in this plan before that research concludes.

The implementation gate is tracked in [Factory-Design Game Vertical-Slice Implementation Gate](factory-design-game-vertical-slice.md).

## 3. Upstream implementation prerequisites

Playable/runtime-integrated work must not define missing Arcogine semantics under UI pressure.

### Factory Design

Consume the supported canonical model, executability validation, immutable publication, model identity/provenance, and deterministic runtime-instantiation boundary.

The game may keep an editor-specific mutable draft, but it must project only supported canonical facts at validation/publication time.

### Engine

Before playable integration is admitted, the required Engine capabilities for the promoted product requirements must be landed. For the currently researched capacity/layout loop this includes:

- explicit production workload and immutable order intent;
- deterministic independently dispatchable work and resource selection;
- consumer-neutral bounded advancement;
- stable supported observations and ordered runtime events;
- deterministic spatial transfer consequences;
- the accepted order/work-item decomposition contract from ADR-0010.

Use the current [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md) and its implementation companions as the authority for which of those gates are actually complete.

### Challenge

[Factory-Design Game Challenge Readiness](factory-design-game-challenge-readiness.md) provides the game-owned headless challenge substrate: challenge identity/version, catalogue/economics, admissibility, deterministic evaluation, attempt provenance/comparison, and data-driven fixtures.

That substrate may be consumed by the eventual game, but its completion is not evidence that the playable product loop has been validated.

## 4. Settled responsibility boundary

| Concern | Owner |
|---|---|
| Rendering, animation, camera, input, audio, assets | Game |
| Editable draft, local undo/history, editor persistence | Game |
| Challenge identity/rules, catalogue availability/prices, construction budget | Challenge layer |
| Candidate admissibility | Challenge layer |
| Evaluation policy, score/rating semantics, attempt history/comparison | Challenge layer |
| Canonical production-system semantics and executability | Arcogine Factory Design |
| Published model identity/version/provenance | Arcogine Factory Design / Governance where revision-bound |
| Workload, work items, queues, dispatch, processing, transfers | Arcogine Engine/runtime |
| Simulation clock and deterministic event ordering | Arcogine Engine/runtime |
| Supported runtime observations/events and performance facts | Arcogine Engine/runtime |
| Reusable derived measurement (longitudinal aggregation, utilization/occupancy intervals, diagnosis, run comparison) | **Unresolved — not game-owned by default.** Open High-risk research: [Simulation analytics consumer boundary](../research/simulation-analytics-consumer-boundary.md) |
| Player-facing presentation, explanation, wording, and visualization of supported facts | Game |
| Game save wrapper around any supported Arcogine checkpoint | Game |

Do not put `Level`, score/rating, unlocks, player currency, tutorial state, decorative assets, or campaign progression into canonical Arcogine semantics.

Do not reproduce Arcogine validation, workload decomposition, scheduling, queueing, dispatch, transfer, or KPI semantics inside the game.

## 5. Integration rules after admission

### Draft projection

The game draft may be incomplete or invalid. Projection into Arcogine must be explicit and testable and must not invent unsupported production semantics.

### Simulation control

The game consumes bounded advancement/reset semantics. Presentation speed changes how the client asks the runtime to advance; it must not change production durations or make outcomes depend on wall-clock timing.

### Visualization and diagnostics

Render supported observations/events rather than internal Engine classes. Consumer summaries may make evidence easier to understand but must not become a competing authoritative computation.

Reusable *diagnostic* computation — utilization or occupancy intervals, longitudinal aggregation, bottleneck inference, run-to-run comparison — is **not settled as game-owned**. Arcogine already has several consumers of reusable derived measurement (generic KPI computation, an outward KPI surface, and a web consumer retaining KPI history and computing baseline deltas), so that ownership is an open cross-consumer question under [Simulation analytics consumer boundary](../research/simulation-analytics-consumer-boundary.md).

Until that question resolves, the game must not implement generic analytics locally as though it owned the semantics. Game-owned presentation of supported facts, and game/Challenge-owned scoring and evaluation, remain unaffected — scoring and challenge evaluation are deliberately separate from generic simulation analytics. Once the boundary is settled, the game may consume analytics through either an embedded or a remote adapter; that is a packaging choice, not a semantics choice.

### Challenge evaluation

Challenge evaluation interprets authoritative Arcogine outcome facts plus game-owned economics/draft facts. It must not reconstruct production truth from hidden assumptions.

### Persistence

Any combined save is a consumer wrapper around the supported Arcogine recovery/checkpoint contract plus game-owned state. Do not invent an unsupported Engine persistence contract merely to satisfy the game.

## 6. Playable implementation admission

Playable/runtime-integrated work may begin only when:

1. the vertical-slice product research has concluded and promoted a concrete requirement set;
2. the required Factory/Engine gates for that requirement set are landed;
3. the integration surface is explicit;
4. any selected save/recovery requirement has a supported Arcogine contract or is explicitly excluded from the first slice;
5. game-only semantics remain outside Arcogine; and
6. acceptance tests can be written against supported Arcogine contracts rather than internal implementation state.

## 7. Integration acceptance criteria

Once admitted, the first playable slice is complete only when:

1. a game-owned candidate is admitted by challenge rules, projected, validated, and published through Arcogine;
2. the runtime is created only from the published model;
3. the fixed production requirement is submitted without the game manufacturing work decomposition;
4. the client controls the run through supported session/advancement primitives;
5. player-visible runtime state derives from supported observations/events;
6. identical explicit inputs produce identical semantic outcomes;
7. a design change produces a new candidate/publication rather than mutating an active run's source model;
8. challenge scoring/evaluation remains game-owned and attributable to its policy version; and
9. no game DTO, editor object, or presentation state re-enters Arcogine domain decision paths.

## 8. Non-goals

Unless separately promoted from research, the first implementation does not include live structural reconfiguration, live-production connectivity, a generic plugin framework, campaign/multiplayer architecture, a shared Arcogine editor, or new Factory/Engine abstractions justified only by presentation convenience.
