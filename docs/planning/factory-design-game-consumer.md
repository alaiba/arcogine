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

For a minimal non-spatial slice, both focused product questions are concluded and neither admits implementation. [Strategy space](../research/investigations/factory-design-game-strategy-space.md) promoted bounded reference-challenge requirements. [Diagnostic evidence](../research/investigations/factory-design-game-diagnostic-evidence.md) promoted the bounded diagnostic requirements in [Diagnostic claims and evidence](#diagnostic-claims-and-evidence). Its positive result is narrow: a mechanically truthful, inspectable diagnostic surface is achievable within the studied scope. One co-designing owner, in a single-person smoke test, found a text prototype explorable. Usability, comprehension, engagement and learning by target players are **not** established. Controlled retry explanation, spatial trade-off, scoring/level structure, external-player validation, and other product questions remain separate candidates and become implementation prerequisites only if the selected slice actually depends on them. A first internal playable slice does not require a participant pool; any later release claim about broad player comprehension must promote and satisfy the separate external-player validation question.

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
interpretation; this reverses the former default against Game ownership. The concluded
[diagnostic-evidence research](../research/investigations/factory-design-game-diagnostic-evidence.md)
admits no method: an actual Game utilization or starvation method stays unadmitted until it passes
the method acceptance gate in [Diagnostic claims and evidence](#diagnostic-claims-and-evidence),
and playable implementation until §6. Research-local formulas do
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

### Diagnostic claims and evidence

These requirements promote the concluded
[diagnostic-evidence research](../research/investigations/factory-design-game-diagnostic-evidence.md).
They bind every player-facing diagnostic of an admitted slice. They select no UI, client technology,
mission set, curriculum or analytical method. They were established only for the studied scope:
non-spatial, one product, linear routing, the whole order released at once, every machine always
online, no setup or transfer, and static recorded runs with pre-computed variants. Outside that scope
a claim needs its own validation or is refused.

**Claim classes.** Every diagnostic claim shown to the player is exactly one of:

1. a direct supported fact (observation or runtime event), or a Game-owned design fact;
2. a count from one observation, labeled with that observation's own simulated time;
3. an interval between supported events;
4. a controlled comparison of outcomes between runs; or
5. a value of a **named analytical method** that declares its method, population, period,
   availability treatment, grouping, and sufficient evidence or refusal.

A claim stronger than its available proof is omitted or weakened, never shown as fact. A requested
advancement target is not an observed time. Commands apply at the last processed event's time, so
present a snapshot as the state at a later requested tick only when the driver exhausted pending
work through that tick and no later input takes effect at or before it
([runtime evidence rules](../architecture/runtime-contract.md#captured-execution-evidence-and-interval-determinacy)).
Internal scheduler events are never diagnostic evidence.

**Pull-based inspection.** The player can inspect on demand, rather than being handed answers:

- the published design, and the run's progress and finish;
- slot state at a labeled observed time: active jobs against concurrency, never the machine-level
  `Busy` state alone;
- waiting by **operation step**. A machine is named only where supported evidence binds the work to
  it, as in its own queue. Pending multi-eligible work appears once, under its step and eligible set:
  never as several machine queues, and never with a guessed receiving machine;
- each unit's journey, separating waiting from processing at each step, and each machine's
  timeline, from captured supported events or observations sufficient for the claim; and
- comparison with pre-computed variants.

**Forbidden shortcuts.** None of these may be presented as fact; the research falsified each on a
concrete case:

- the busiest or most-occupied machine or step is the bottleneck or constraint;
- the step with the most or longest waiting is the constraint;
- an idle resource seen in one run is expendable or surplus;
- shared pending work is several machine queues, or `combinedQueueDepth` is a physical queue;
- machine-level `Busy` means every slot is working;
- Engine `throughputPerTick`, defined over observed supported time, is a rate over a period the
  player chooses; and
- a deterministic, reproducible association is an observed cause.

**Comparisons.** A single-change variant may show the exact authored change and the outcome
delta, but not an unseen mechanism. When several things change, show the whole change set and an
explicit comparison basis, and withhold attribution. Under confounding or co-binding no single change
is responsible, and how a joint effect splits between changes depends on the order in which they
are considered. Single-change "needed / not needed" results do **not** compose into a verdict on a
joint change; offer the joint change as its own experiment. "Needed" is a time-only conclusion within
the offered variants. Finishing sooner is not "worth it": only an admitted, named
[Challenge evaluation](#challenge-evaluation) rule may decide that. Changes keep result-affecting
resource identity and order visible (§1).

**Two-layer idle classification.** Slot state (working or idle) is observed. *Why* a slot is idle
is a named, Game-owned interpretation. Within the studied scope, a snapshot may classify an idle slot
from published eligibility and all **currently known** unfinished work. It is either starved (known
work still needs a step this machine can serve), or it has no currently known eligible work. The
second is not a promise that no further work will arrive. After an availability change, [Engine
semantics §4](../architecture/engine-semantics.md#4-scheduler-and-dispatch-cascade-ordering) rule 7
can leave a slot idle while work waits in that machine's own queue. That state needs a third
classification or an explicit refusal; it is not impossible. Broader scope also needs a third
classification or refusal until a method is validated for it. Snapshot, period and future-horizon idle
claims follow the evidence rules above.

**Method acceptance gate.** Utilization over a period, and starvation as an idle-time
characterization, are concrete **Game measurement demands**. They are neither admitted methods nor
shared analytics. The research formulas are candidates only: working slot-ticks over available
slot-ticks for a half-open period, starved slot-ticks, and "starved / no work left". Before a slice
shows either measure, its admission must specify, independently validate and accept a named method.
That method must fix and test:

- the interval bounds, and whether work still running at the upper bound counts up to it;
- slots and concurrency, and the denominator or available-time policy;
- whether offline time is excluded, and what is refused when availability changes;
- what counts as known unfinished and eligible work, and whether starved slots are capped by the
  remaining eligible work;
- pool or step attribution for a machine serving several steps, since step-pool and machine views
  can rank differently;
- its evidence source: a complete retained supported-event stream, or adequately sampled
  observations, with a valid basis, frontier and finality; and
- deterministic refusal for any input, population or interval outside its demonstrated domain.

The research flow method was never validated on machines eligible for several steps, nor across the
whole falsification corpus. No default for any of these choices is implied. A single-run bottleneck
method is optional: no product evidence demands one, and variant experiments answer bottleneck
questions.

**No new platform facts.** These requirements need no new Engine fact, event, accumulator or
closure service. Per-step timing comes from captured supported runtime events, or from observations
sampled adequately for the claim. The controller or holder owns its recording horizon, and a fresh
observation cannot recover history from before capture began. Stable machine identity under draft
edits is the projector's identity/order rule (§1), not a new Factory primitive. Design differences
are not a platform gap either. Factory's semantic comparator, under Governance `ChangeSet`
orchestration, already compares designs by stable `MachineId` and reports list-position changes.
Presentation must not show a renumbering, or a position shift caused by another change, as a
separate authored change.

**Presentation requirements.** These are requirements, not a chosen design:

- inspection is pulled by the player, and nothing is pre-answered;
- counterfactual questions are answered by variant experiments, never by a single-run label;
- a learning task states its goal and an explicit answer format, and after the answer shows the
  official answer, why, how to find it and what it teaches;
- industry vocabulary is introduced in context, with an on-demand reference; and
- period conventions such as half-open tick ranges are taught gently before the first period
  question, not annotated on every range.

Refusal is an internal correctness guardrail, not a mandatory player-facing "cannot infer" panel:
prefer offering the experiment that answers the question, or omit the unsupported statement. A mission
count, scripts, answer keys and client technology are not requirements.

**Acceptance tests.** Before admission, a slice that shows diagnostics derives its own executable
acceptance tests from these guardrails. It does not reuse the research's keyword and
self-declared-facet audits, which are regression checks rather than truthfulness verifiers. The
tests cover at least the [preserved negative
cases](../research/investigations/factory-design-game-diagnostic-evidence.md#reusable-proving-cases):

- shared multi-eligible waiting;
- work still running at a half-open bound;
- the concurrency denominator;
- one idle snapshot with different removal outcomes;
- capacity added at the most-occupied pool to no effect;
- an order-only change;
- confounded co-binding;
- single-change non-composition; and
- the offline-and-queued state.

Each admitted named method also needs its own validation and refusal tests on its admitted domain.

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
5. game-only semantics remain outside Arcogine;
6. every player-facing diagnostic the slice selects meets [Diagnostic claims and evidence](#diagnostic-claims-and-evidence), including an accepted named method for any utilization or starvation measure; and
7. acceptance tests can be written against supported Arcogine contracts rather than internal implementation state.

## 7. Integration acceptance criteria

Once admitted, the first playable slice is complete only when:

1. a game-owned candidate is admitted by challenge rules, projected, validated, and published through Arcogine;
2. the runtime is created only from the published model;
3. the fixed production requirement is submitted without the game manufacturing work decomposition;
4. the client controls the run through supported session/advancement primitives;
5. player-visible runtime state derives from supported observations/events, and every diagnostic claim passes the slice's acceptance tests for [Diagnostic claims and evidence](#diagnostic-claims-and-evidence);
6. identical explicit inputs produce identical semantic outcomes;
7. a design change produces a new candidate/publication rather than mutating an active run's source model;
8. challenge scoring/evaluation remains game-owned and attributable to its policy version; and
9. no game DTO, editor object, or presentation state re-enters Arcogine domain decision paths.

## 8. Non-goals

Unless separately promoted from research, the first implementation does not include live structural reconfiguration, live-production connectivity, a generic plugin framework, campaign/multiplayer architecture, a shared Arcogine editor, or new Factory/Engine abstractions justified only by presentation convenience.
