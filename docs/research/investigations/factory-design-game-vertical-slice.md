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

## Owner direction for the first playable slice

On 2026-10-13 the owner selected the direction of the first slice:

> The first non-spatial playable slice should be a small factory-optimization game in which the player designs a production system, tests it, learns why it behaves as it does, and improves it.

It bounds the [first-slice requirement set](#first-slice-requirement-set--candidate) question below.

## Focused questions

### First-slice requirement set — CANDIDATE

Question:

> What minimum requirement set makes the owner-selected direction a coherent first non-spatial playable slice, and which open product questions, if any, does that requirement set depend on?

Two concluded inputs bound it: the [strategy-space reference challenge](factory-design-game-strategy-space.md) and the diagnostic [promoted requirements](factory-design-game-diagnostic-evidence.md#promoted-requirements), together with the [consumer integration boundary](#consumer-integration-boundary) below. It needs a bounded brief before it is `READY`. A conclusion that passes the research method's [implementation-ready test](../../development/researching.md#implementation-handoff) leaves research as an implementation handoff; anything it cannot settle becomes a narrower question.

### Diagnostic evidence contract — CONCLUDED

[Factory-design game diagnostic evidence contract](factory-design-game-diagnostic-evidence.md) asked what minimum player-facing evidence contract can be built from supported simulation facts so that every diagnosis is mechanically traceable, its method is explicit, and unsupported causal attribution is refused.

It concluded as a bounded positive within the studied non-spatial scope: a mechanically truthful, inspectable diagnostic surface is achievable. Its requirements are its [promoted requirements](factory-design-game-diagnostic-evidence.md#promoted-requirements). The human evidence is a single-person smoke test by a co-designing owner with reduced blinding. That can falsify a presentation but cannot establish what independent players will understand, so usability, comprehension, learning and bottleneck teaching remain unestablished. Utilization and starvation are concrete Game measurement demands; an accepted named method must fix their choices before any slice shows them. No method or implementation is admitted.

### Non-spatial challenge strategy space — CONCLUDED

[Factory-design game strategy space](factory-design-game-strategy-space.md) asked whether a deliberately small fixed production challenge, using current production/capacity semantics plus game-owned capital costs, can produce several materially different and explainable viable interventions without relying on spatial layout.

It concluded as a bounded positive for a constructive reference, not a general design-space claim. Current deterministic non-spatial semantics support a mechanically non-trivial capacity/capital reference challenge: a bounded serial fixed-workload family with shared multi-step-eligible capacity has two materially separated, nondominated structures that survive the tested resource-identity orderings and bounded repricing. Shared eligibility is a strong constructive mechanism but is not established as necessary, and excluding it is not an automatic negative result. Occupancy is a descriptive measurement, not a constraint definition, and a future projector must make result-affecting resource identity/order explicit and deterministic.

This is **mechanical ground truth** only, and it was deliberately separated from presentation and from spatial transfer. It establishes no player comprehension, discoverability, engagement or fairness of presentation; those stay with the diagnostic-evidence, equipment-value-discovery, scoring/level and external-player questions. It promoted bounded reference-challenge requirements, not an implementation slice, a scoring formula, a spatial mechanic or an Engine/Factory change.

### Controlled retry explanation — CANDIDATE

Question:

> Can a controlled one-variable retry be presented as a mechanically traceable before/change/after evidence chain without asserting an unobserved causal mechanism?

Its evidence-contract precondition is now met. The concluded diagnostic-evidence requirements already fix what a controlled comparison may claim: a single change with its outcome delta, a whole change set with attribution withheld, single-change results that do not compose, and "needed" as time-only. Promote this question only when a selected slice or product decision needs retry-explanation evidence beyond those requirements. The concluded strategy-space reference already supplies a challenge family worth comparing, though most single additions from a given base change nothing, so the base of a controlled retry must be chosen deliberately. This remains a product-explanation question; common named run-comparison meanings follow the [concluded analytical-definition admission rule](../../architecture/overview.md#domain-authority), not an assumed shared module.

### Spatial design trade-off — CANDIDATE

Question:

> Once transfer semantics and spatial runtime consequences are settled, does changing spatial arrangement create a legible performance trade-off against capacity and game-owned capital cost?

This is not ready until spatial runtime consequences are executable, which they are not: the spatial record and transfer lifecycle are [decided but not implemented](transfer-semantics.md#decided-design--not-implemented). It must consume that design once it is implemented rather than using the game to decide it.

### External player validation — CANDIDATE

Question:

> When independent target users become available, does the promoted diagnostic presentation let them correctly diagnose the reference challenge without repository/runtime knowledge or expert guidance?

This is the place for actual population-level comprehension evidence. It is intentionally not `READY` because no independent participant pool is currently available and the first internal playable slice does not need to make a population-level usability claim.

Promote it when either independent target users are available or product/release direction explicitly requires a claim about new-player comprehension, accessibility, onboarding, or diagnostic success. At promotion time, define the target population, tasks, falsification thresholds, and decision at stake. Reuse the diagnostic-evidence study's [preserved proving cases](factory-design-game-diagnostic-evidence.md#reusable-proving-cases) where appropriate, but do not treat the product owner, an AI model, or repository reviewers as substitutes for the target population.

### Equipment-value discovery and misleading cues — CANDIDATE

Question:

> Can a factory-design challenge deliberately include mechanically equivalent, dominated, or
> presentation-attractive equipment offers so players must distinguish genuine production leverage
> from price, branding, cosmetic or other non-mechanical cues without making the problem opaque or
> unfair?

This is distinct from mechanical strategy-space research. An optimizer may discard a dominated
offer immediately, while a human player may infer nonexistent capability from price, appearance,
naming or presentation. That discovery process can itself be a legitimate challenge and learning
objective if the underlying mechanical truth remains inspectable through supported evidence.

Keep these distinctions explicit:

- **mechanical value** — consequences under the declared production/economic objective;
- **choice-set structure** — which mechanically viable, dominated or equivalent offers are available;
- **presentation/perceived value** — what the UI or content makes salient before the player has
  tested the choice; and
- **player preference** — aesthetic or other utility the player may genuinely value even when it is
  outside the declared mechanical objective.

Promote when a playable presentation and independent target users are available, or when product
direction makes this learning claim material enough to test. Define fairness/disclosure boundaries,
target population, tasks and falsification thresholds at promotion time. Do not treat a product
owner, repository reviewer or AI model as evidence of population behavior.

### Scoring, challenge and level structure — CANDIDATE

The maintained register separately tracks:

> What scoring, challenge, level, catalogue-choice and information structures create a legible
> optimization experience without collapsing into an opaque dominant meta?

The mechanically viable solution set and the player's offered choice set need not be identical.
Challenge design may intentionally include dominated, equivalent, bargain or presentation-only
alternatives when discovering which distinctions matter is part of the intended reasoning loop.
Accordingly, the mere presence of a dominated catalogue option is not by itself evidence of a bad
challenge.

The concluded strategy-space reference now supplies one bounded mechanical solution family. Its
frontier is ground truth, not a prescription: it does not require every offered item to be
nondominated, and any fixed scalar scoring rule selects a single frontier design. Do not promote
this question until the product has evidence about what should be rewarded, disclosed, hidden or
learned. Coordinate empirical claims about misleading cues with the separate
equipment-value-discovery question rather than inferring human behavior from headless enumeration.

## Consumer integration boundary

These are the current boundary assumptions for any playable slice. They come from earlier delivery planning, carry no implementation admission, and are inputs to the first-slice requirement set.

```text
game-owned draft
      |
      v
challenge admissibility            (Challenge)
      |
      v
project supported canonical facts  (consumer projector)
      |
      v
Arcogine validate + publish -> FactoryModelVersion -> Arcogine runtime
      |
      v
supported observations / runtime events
      |
      v
game presentation, evaluation and attempt comparison
```

Challenge admissibility, Factory publication validity and Engine executability stay separate decisions ([Challenge](../../architecture/challenge.md)). No production projector from a game candidate to a Factory model exists; a consumer-side projector must translate game-authored candidate facts into then-supported canonical Factory facts before validation and publication, and must not invent unsupported production semantics.

**Projector identity and order.** Under current Engine semantics a remaining selection tie is broken by `MachineId`, so the order in which resources eligible for more than one step are authored can change completion: the [strategy-space reference](factory-design-game-strategy-space.md) completes in 46 or 47 ticks depending on it. The projector must therefore make result-affecting configured-resource identity and order explicit and deterministic; incidental draft, edit or build order must not decide a result. Where identity is allocated, how edits preserve it, and whether `MachineId` tie-breaking is an acceptable player-visible policy remain open, and the [final tie-break question](engine-shared-resource-final-tiebreak.md) stays a candidate.

**Upstream capability.** Every playable slice consumes the landed canonical Factory model, publication validity, immutable publication and model provenance; explicit workload and immutable order intent; deterministic work decomposition and resource selection; consumer-neutral bounded advancement; and the supported observation and event contract. Engine admission remains the authority for executability. Spatial transfer consequences are needed only by a slice that makes layout behaviorally consequential.

**Responsibilities.**

| Concern | Owner |
|---|---|
| Rendering, animation, camera, input, audio, assets | Game |
| Editable draft, local undo/history, editor persistence | Game |
| Challenge identity/rules, catalogue availability/prices, construction budget, admissibility | Challenge |
| Evaluation policy, score/rating, attempt history and comparison | Challenge |
| Canonical production-system semantics and publication validity | Arcogine Factory |
| Executability, workload, work items, queues, dispatch, processing, transfers, clock and event order | Arcogine Engine/runtime |
| Supported observations, runtime events and performance facts | Arcogine Engine/runtime |
| Named Game diagnostics, once a slice includes them | Game, under the [diagnostic requirements](factory-design-game-diagnostic-evidence.md#promoted-requirements); never an Engine fact |
| Named analytical definitions common to several users | A shared owner only after a concrete [Domain authority](../../architecture/overview.md#domain-authority) trigger |
| Player-facing presentation, explanation and wording | Game |
| Game save wrapper around any supported Arcogine checkpoint | Game |

`Level`, score, unlocks, player currency, tutorial state, decorative assets and campaign progression never enter canonical Arcogine semantics, and the game never reproduces Arcogine validation, decomposition, scheduling, dispatch, transfer or KPI semantics.

**Integration rules.** Projection is explicit, testable and deterministic for every result-affecting input. Presentation speed changes how the client asks the runtime to advance, never production durations, and outcomes never depend on wall-clock time. The game renders supported observations and events, not internal Engine classes, and every player-facing diagnostic meets the diagnostic requirements. Challenge evaluation interprets authoritative outcome facts plus game-owned economics and never reconstructs production truth. A combined save wraps a supported Arcogine recovery contract plus game-owned state; it never invents an Engine persistence contract.

**Acceptance evidence for a first slice.** A first playable slice is complete only when:

1. a game-owned candidate is admitted by challenge rules, projected, validated and published through Arcogine;
2. the runtime is created only from the published model;
3. the fixed production requirement is submitted without the game manufacturing work decomposition;
4. the client controls the run through supported session and advancement primitives;
5. player-visible runtime state derives from supported observations and events, and every diagnostic claim passes the slice's acceptance tests for the diagnostic requirements;
6. identical explicit inputs produce identical semantic outcomes;
7. a design change produces a new candidate and publication rather than mutating an active run's source model;
8. challenge scoring and evaluation stay game-owned and attributable to their policy version; and
9. no game DTO, editor object or presentation state re-enters Arcogine domain decision paths.

**Non-goals for a first slice.** Live structural reconfiguration, live-production connectivity, a generic plugin framework, campaign or multiplayer architecture, a shared Arcogine editor, and new Factory/Engine abstractions justified only by presentation convenience.

## Boundary with reusable analytics

The [concluded ownership boundary](simulation-analytics-consumer-boundary.md) separates
Engine-owned execution/results from named methods whose selected meaning belongs to their
consumer by default. This does not select a Game method. The concluded
[diagnostic evidence question](factory-design-game-diagnostic-evidence.md) promoted the product
requirements, sufficient-evidence rules and refusal guardrails, but admitted no method.

Accordingly:

- overlays, timelines, callouts, wording, tutorial sequencing, and progressive disclosure remain Game presentation choices;
- the Game may define its **own** named diagnostic only through the diagnostic requirements' [method acceptance gate](factory-design-game-diagnostic-evidence.md#diagnostic-claims-and-evidence); a genuinely common method follows concrete [shared-owner triggers](../../architecture/overview.md#domain-authority), not a duplicated local KPI formula;
- a research-local derivation may establish study ground truth but does not become a supported Game/Engine definition automatically;
- snapshot, period-aggregate and future-horizon claims require distinct proof; a Game driver does not thereby own every holder's evidence;
- transfer-dependent diagnostics were outside the concluded study and stay unvalidated until spatial runtime consequences are executable and evidenced; population-level comprehension remains solely with the separate CANDIDATE external-player-validation question.

## Boundary with transfer and spatial semantics

Transfer is a production/runtime concern, not a game mechanic that the consumer may define. The decided [transfer-applicability boundary](transfer-semantics.md#applicability-boundary) selects no transfer for spatial-absent content and preserves a full lifecycle for present content with zero or positive timing. Neither the spatial record nor the lifecycle is implemented, so every current model is a no-transfer model.

The game therefore must not assume that:

- Factory publication validity alone makes a design executable by the current Engine;
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
- named Game analytical definitions are consumer-local by default, while shared meanings follow [Domain authority](../../architecture/overview.md#domain-authority); implementation awaits admitted evidence requirements;
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
- the predecessor's unqualified assertion that **all** reusable diagnostic derivations are Game-owned was superseded; the later admitted rule permits bounded Game-specific named methods without transferring genuinely shared meanings;
- the immediately actionable product question became the focused [diagnostic-evidence contract](factory-design-game-diagnostic-evidence.md), which established truthfulness/traceability requirements without a participant pool; population-level comprehension remains the separate external-player-validation candidate above; and
- the broad vertical-slice question itself is superseded by the focused portfolio recorded here and in the research register.

## Promotion to playable implementation

This programme does not have one `CONCLUDED` state. Playable implementation starts only when the [first-slice requirement set](#first-slice-requirement-set--candidate) question reaches an [implementation-ready](../../development/researching.md#implementation-handoff) conclusion and hands it to the Implementer. That conclusion must settle, for its slice:

- the requirement set, following the owner direction, with no dependency on another unresolved product question;
- an accepted named Game method for any utilization or starvation measure the slice shows, under the [method acceptance gate](factory-design-game-diagnostic-evidence.md#diagnostic-claims-and-evidence);
- the projector's identity and order rule and the rest of the integration boundary;
- the Factory/Engine capability it needs, all landed;
- any save/recovery requirement, backed by a supported Arcogine contract or explicitly excluded; and
- executable acceptance tests against supported contracts.

The strategy-space and diagnostic-evidence questions, both concluded, are its minimum inputs. A slice does not require independent human participants merely to exist as an internal playable slice.

The diagnostic research studied static recorded runs with pre-computed variants. A slice whose live interaction issues mid-run commands must first meet the [execution-evidence reopening trigger](simulation-execution-account.md#reopening-triggers) for live-loop control; static or start-fixed playback at any speed does not trigger it.

Controlled retry, a richer concept curriculum (including bottleneck teaching), scoring and level structure, live interaction, and external-player evidence each have their own admission criteria. They are not an automatic required research programme. Admit or promote one only when a selected slice or product decision actually depends on it. A later release goal that claims broad player comprehension must separately promote and satisfy external-player validation. A slice that makes layout consequential additionally requires the transfer/spatial questions that own that behavior. Other candidate questions are promoted only if the selected slice actually depends on them.
