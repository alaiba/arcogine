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

No production game/Challenge-to-Factory projector exists today. The current
`challenge-factory-integration-test` module is test-only and proves independent validation axes; it
does not provide a production adapter. When playable integration is eventually admitted, the
consumer-side integration responsibility must explicitly translate game-authored candidate/content
facts into then-supported canonical Factory facts before validation/publication. Challenge remains
independent of Factory and must not become a second production ontology.

Under current Engine semantics a remaining selection tie is broken by `MachineId`, so for a
resource eligible for more than one operation step the order in which resources are authored can
change completion. The concluded [strategy-space reference](../research/investigations/factory-design-game-strategy-space.md)
shows it: its shared-capacity design completes in 46 or 47 ticks depending on resource-identity
order, so a deadline between them would make eligibility identity-dependent. The projector must
therefore make result-affecting configured-resource identity and order explicit and deterministic;
incidental draft, edit or build order must not decide a result silently. This is a requirement on
the integration boundary, not an assignment of ownership. Where identity is allocated, how edits
preserve it, and whether `MachineId` tie-breaking is an acceptable player-visible policy remain
unresolved, and the separate [final tie-break question](../research/investigations/engine-shared-resource-final-tiebreak.md)
stays a candidate that this result does not promote.

## 2. Product-research dependency

The playable requirement set is not yet an implementation input. The former umbrella vertical-slice question is superseded; current product state is coordinated by the [Factory-Design Game Product Research Programme](../research/investigations/factory-design-game-vertical-slice.md) and its focused register questions.

For a minimal non-spatial slice, [strategy space](../research/investigations/factory-design-game-strategy-space.md) is concluded: it promoted bounded reference-challenge requirements and no implementation. The [diagnostic evidence contract](../research/investigations/factory-design-game-diagnostic-evidence.md) remains `READY` and is the product-evidence blocker. Controlled retry explanation, spatial trade-off, scoring/level structure, external-player validation, and other product questions remain separate candidates and become implementation prerequisites only if the selected slice actually depends on them. A first internal playable slice does not require a participant pool; any later release claim about broad player comprehension must promote and satisfy the separate external-player validation question.

Rendering/input technology, save-wrapper shape, packaging, interpolation policy, and similar implementation choices are deferred here until promoted requirements make them concrete; undecided does not by itself make them standing research questions.

The implementation gate is tracked in [Factory-Design Game Vertical-Slice Implementation Gate](factory-design-game-vertical-slice.md).

## 3. Upstream implementation prerequisites

Playable/runtime-integrated work must not define missing Arcogine semantics under UI pressure.

### Factory Design

Consume the supported canonical model, Factory model validation for publication, immutable publication, model identity/provenance, and deterministic runtime-instantiation boundary. Engine admission remains the authority for runtime executability.

The game may keep an editor-specific mutable draft, but it must project only supported canonical facts at validation/publication time.

### Engine

Before playable integration is admitted, the required Engine capabilities for the promoted product requirements must be landed. Every playable slice requires:

- explicit production workload and immutable order intent;
- deterministic independently dispatchable work and resource selection;
- consumer-neutral bounded advancement;
- stable supported observations and ordered runtime events; and
- the accepted order/work-item decomposition contract from [Engine semantics §3](../architecture/engine-semantics.md#3-unit-work-decomposition-semantics).

Deterministic spatial transfer consequences are required only if the promoted slice makes layout behaviorally consequential. That path depends on the executable spatial-runtime work in [Spatial Runtime Consequences](spatial-runtime-consequences.md); the [current transfer boundary](../architecture/transfer-applicability.md) is already reconciled.

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
| Canonical production-system semantics and publication validity | Arcogine Factory Design |
| Engine executability of a published model | Arcogine Engine/runtime |
| Published model identity/version/provenance | Arcogine Factory Design / Governance where revision-bound |
| Workload, work items, queues, dispatch, processing, transfers | Arcogine Engine/runtime |
| Simulation clock and deterministic event ordering | Arcogine Engine/runtime |
| Supported runtime observations/events and performance facts | Arcogine Engine/runtime |
| Named Game-specific analytical diagnostics, if requirements are separately admitted | Game defines its own chosen method and evidence/refusal rules; never an Engine fact or a generic shared result |
| Named analytical definitions common to multiple users or a verification/outward contract | Shared definition owner only after a concrete [architecture admission trigger](../architecture/overview.md#domain-authority); evidence custody stays with the actual holder |
| Player-facing presentation, explanation, wording, and visualization of supported facts | Game |
| Game save wrapper around any supported Arcogine checkpoint | Game |

Do not put `Level`, score/rating, unlocks, player currency, tutorial state, decorative assets, or campaign progression into canonical Arcogine semantics.

Do not reproduce Arcogine validation, workload decomposition, scheduling, queueing, dispatch, transfer, or KPI semantics inside the game.

## 5. Integration rules after admission

### Draft projection

The game draft may be incomplete or invalid. Projection into Arcogine must be explicit and testable and must not invent unsupported production semantics. It must also be deterministic with respect to every result-affecting input it chooses, including configured-resource identity and order (see §1).

### Simulation control

The game consumes bounded advancement/reset semantics. Presentation speed changes how the client asks the runtime to advance; it must not change production durations or make outcomes depend on wall-clock timing.

### Visualization and diagnostics

Render supported observations/events rather than internal Engine classes. Consumer summaries may make evidence easier to understand but must not become a competing authoritative computation.

A **named Game-specific method** may be Game-owned when it fixes the diagnostic question and its
population, observation basis, interval, denominator/capacity, availability, attribution and
interpretation; this reverses the former default against Game ownership, **not** the separate
[diagnostic-evidence research](../research/investigations/factory-design-game-diagnostic-evidence.md)
gate. Actual Game utilization/flow methods and playable implementation remain unadmitted until
that research concludes and its product requirements are reconciled. Research-local formulas do
not become production definitions. A Game method describes supported outcomes, never reconstructs
Engine scheduling or unseen processing starts, declares a causal bottleneck from occupancy alone,
or treats Engine's observed-time `throughputPerTick` as a rate over a user-selected period.

For a retained or promoted **interval** result, a Game method must declare definition identity,
`RunId`, `ModelFingerprint` (and `ControlledRevisionId` only when authoritatively bound),
the half-open interval and convention, observation basis `(S0,T0)`, proof sequence/frontier and
proof class. Claim-specific controller attestor, excluded-input scope, trust and command/fault
qualifications, exact producing Engine-definition reference or complete ordered command script
(including advancement interleavings/budgets) are required **only when that claim needs them**.
Bind basis, changes, observations and closure to the same run and refuse a closure contradicted
by captured changes after its binding but before the upper bound. A held sequence without an
independent authoritative frontier is insufficient. Producer witnesses, producer observations
and attributable controller closure are different proof classes; complete changes alone prove
neither fault-free execution nor replayability (see [runtime evidence
rules](../architecture/runtime-contract.md#captured-execution-evidence-and-interval-determinacy)).
Evidence custody can stay with an exclusive Game driver or another holder; neither capture nor
promotion by itself grants ownership or upgrades trust, and portability without rework requires
preserved proof provenance.

A fresh observation can show **no currently known work** or classify currently known
idle/starved work using published routing; it is not a period aggregate. Aggregating such labels
over an interval requires claim-scoped coverage and finality, including qualified controller
closure for an idle tail when needed. **No work left for the rest of a period** is a future-horizon
claim requiring an explicit no-future-effective-input scope. Game diagnostic experiments with a
job-free initial basis, always-online resources, one product and closed constructive scripts
establish only their bounded case, not a universal method.

A genuinely shared named definition is reconsidered under the four [Domain authority
triggers](../architecture/overview.md#domain-authority), not by Game preference, shared file
placement or common capture. [Analytics evidence/provenance](../research/investigations/simulation-analytics-evidence-provenance.md)
remains conditional on a concrete input, method-proof, accumulation, identity, provenance or
retention use; [adapter compatibility](../research/investigations/simulation-analytics-adapter-compatibility.md)
and [cross-revision comparison](../research/investigations/simulation-analytics-cross-revision-comparability.md)
retain their separate triggers. No analytics module, universal evidence container or embedded/remote
adapter is chosen.

### Challenge evaluation

Challenge evaluation interprets authoritative Arcogine outcome facts plus game-owned economics/draft facts. It must not reconstruct production truth from hidden assumptions.

### Persistence

Any combined save is a consumer wrapper around the supported Arcogine recovery/checkpoint contract plus game-owned state. Do not invent an unsupported Engine persistence contract merely to satisfy the game.

## 6. Playable implementation admission

Playable/runtime-integrated work may begin only when:

1. every focused product-research question required by the selected first slice has concluded and promoted a concrete requirement set;
2. the required Factory/Engine gates for that exact requirement set are landed;
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
