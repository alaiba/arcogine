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

### Diagnostic evidence contract — CONCLUDED

[Factory-design game diagnostic evidence contract](factory-design-game-diagnostic-evidence.md) asked what minimum player-facing evidence contract can be built from supported simulation facts so that every diagnosis is mechanically traceable, its method is explicit, and unsupported causal attribution is refused.

It concluded as a bounded positive within the studied non-spatial scope: a mechanically truthful, inspectable diagnostic surface is achievable. Its requirements are in the consumer plan's [Diagnostic claims and evidence](../../planning/factory-design-game-consumer.md#diagnostic-claims-and-evidence). The human evidence is a single-person smoke test by a co-designing owner with reduced blinding. That can falsify a presentation but cannot establish what independent players will understand, so usability, comprehension, learning and bottleneck teaching remain unestablished. Utilization and starvation are concrete Game measurement demands; an accepted named method must fix their choices before any slice shows them. No method or implementation is admitted.

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

This is not ready until spatial runtime consequences are executable. It must consume the [current transfer-applicability boundary](../../architecture/transfer-applicability.md), the current [Engine semantics](../../architecture/engine-semantics.md), and the executable spatial-runtime work in [Spatial Runtime Consequences](../../planning/spatial-runtime-consequences.md) rather than using the game to decide them.

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

## Boundary with reusable analytics

The [concluded ownership boundary](simulation-analytics-consumer-boundary.md) separates
Engine-owned execution/results from named methods whose selected meaning belongs to their
consumer by default. This does not select a Game method. The concluded
[diagnostic evidence question](factory-design-game-diagnostic-evidence.md) promoted the product
requirements, sufficient-evidence rules and refusal guardrails into the consumer plan, but admitted no
method.

Accordingly:

- overlays, timelines, callouts, wording, tutorial sequencing, and progressive disclosure remain Game presentation choices;
- the Game may define its **own** named diagnostic only through the consumer plan's method acceptance gate; a genuinely common method follows concrete [shared-owner triggers](../../architecture/overview.md#domain-authority), not a duplicated local KPI formula;
- a research-local derivation may establish study ground truth but does not become a supported Game/Engine definition automatically;
- snapshot, period-aggregate and future-horizon claims require distinct proof; a Game driver does not thereby own every holder's evidence;
- transfer-dependent diagnostics were outside the concluded study and stay unvalidated until spatial runtime consequences are executable and evidenced; population-level comprehension remains solely with the separate CANDIDATE external-player-validation question.

## Boundary with transfer and spatial semantics

Transfer is a production/runtime concern, not a game mechanic that the consumer may define. The [current transfer-applicability boundary](../../architecture/transfer-applicability.md) selects no transfer for spatial-absent content and preserves a full lifecycle for present content with zero or positive timing. The current Engine refuses present spatial content until spatial execution lands.

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

This programme does not have one `CONCLUDED` state. Playable implementation may be admitted only when the implementation gate can name the exact focused research conclusions required by the chosen first slice, the required Arcogine semantics are landed, and executable acceptance tests can be written.

A minimal non-spatial **internal playable** slice requires, at least, a concluded challenge-strategy question and a concluded diagnostic-evidence question. Both are now concluded. Neither admits implementation: the [implementation gate](../../planning/factory-design-game-vertical-slice.md) still needs a selected slice, accepted methods for any utilization or starvation measure, the integration boundary, the selected prerequisites and executable acceptance tests. A slice does not require independent human participants merely to exist as an internal playable slice.

Controlled retry, a richer concept curriculum (including bottleneck teaching), scoring and level structure, live interaction, and external-player evidence each have their own admission criteria. They are not an automatic required research programme. Admit or promote one only when a selected slice or product decision actually depends on it. A later release goal that claims broad player comprehension must separately promote and satisfy external-player validation. A slice that makes layout consequential additionally requires the transfer/spatial questions that own that behavior. Other candidate questions are promoted only if the selected slice actually depends on them.
