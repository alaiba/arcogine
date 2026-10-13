# Factory-Design Game Product Requirements

> **Status:** Maintained product requirements for the factory-design game consumer
> **Scope:** The owner-selected direction for the first playable slice, and the player-facing diagnostic and analytical-method requirements every playable slice must meet
> **Authority:** Product direction for a separate consumer of Arcogine under the [Product Charter](charter.md). It defines no Factory, Engine, Governance or analytics semantics. The game/Arcogine responsibility boundary lives in the [Game consumer plan](../planning/factory-design-game-consumer.md), and implementation admission in the [playable implementation gate](../planning/factory-design-game-vertical-slice.md)

The game is a consumer of Arcogine, not Arcogine's product identity ([Charter §9](charter.md#9-what-arcogine-is-not)). It owns presentation, player-facing authoring, challenge rules and attempt history; Arcogine remains the authority for published production-system semantics and deterministic runtime consequences.

## 1. First playable slice

**Owner direction (2026-10-13):** the first non-spatial playable slice is a small factory-optimization game in which the player designs a production system, tests it, learns why it behaves as it does, and improves it.

The loop exercises four of the Charter's [modes of engagement](charter.md#5-modes-of-engagement-not-personas) over one published model: Design, Simulate, Understand and Improve. This direction selects:

- a **non-spatial** slice: layout is not behaviorally consequential, so the slice does not depend on spatial runtime execution;
- the **design → test → understand → improve** loop as the slice's purpose, rather than a scoring, campaign or presentation showcase.

It does not yet select the challenge content, how an improvement is compared or rewarded, which diagnostics the slice shows, whether any named analytical method is needed, client technology, or save/recovery behavior. Those are settled when the slice's requirement set is written, under the [playable implementation gate](../planning/factory-design-game-vertical-slice.md). Two concluded inputs already bound that work: the [strategy-space reference challenge](../research/investigations/factory-design-game-strategy-space.md) and the diagnostic requirements below. The reference challenge carries its qualifications into any use: shared eligibility is a strong constructive mechanism, not a proven necessity, and mechanical ground truth is not player comprehension.

## 2. Diagnostic claims and evidence

These requirements promote the concluded
[diagnostic-evidence research](../research/investigations/factory-design-game-diagnostic-evidence.md).
They bind every player-facing diagnostic of an admitted slice. They select no UI, client technology,
mission set, curriculum or analytical method. They were established only for the studied scope:
non-spatial, one product, linear routing, the whole order released at once, every machine always
online, no setup or transfer, and static recorded runs with pre-computed variants. Outside that scope
a claim needs its own validation or is refused. Their human evidence is one co-designing owner's
single-person smoke test of a text prototype: usability, comprehension, engagement and learning by
target players are **not** established. An internal playable slice needs no participant pool, but a
release claim about broad player comprehension must first satisfy the separate external-player
validation question.

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
[Challenge evaluation](../planning/factory-design-game-consumer.md#challenge-evaluation) rule may
decide that. Changes keep result-affecting resource identity and order visible, as the
[consumer boundary](../planning/factory-design-game-consumer.md#1-consumer-boundary) requires of the
projector.

**Two-layer idle classification.** Slot state (working or idle) is observed. *Why* a slot is idle
is a named, Game-owned interpretation. Within the studied scope, a snapshot may classify an idle slot
from published eligibility and all **currently known** unfinished work. It is either starved (known
work still needs a step this machine can serve), or it has no currently known eligible work. The
second is not a promise that no further work will arrive. After an availability change, [Engine
semantics §4](../architecture/engine-semantics.md#4-scheduler-and-dispatch-cascade-ordering) rule 7
can leave a slot idle while work waits in that machine's own queue. That state needs a third
classification or an explicit refusal; it is not impossible. Broader scope also needs a third
classification or refusal until a method is validated for it. Snapshot, period and future-horizon idle
claims follow the evidence rules in [Game-owned analytical methods](#3-game-owned-analytical-methods).

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
edits is the projector's identity/order rule, not a new Factory primitive. Design differences
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

## 3. Game-owned analytical methods

A **named Game-specific method** may be Game-owned when it fixes the diagnostic question and its
population, observation basis, interval, denominator/capacity, availability, attribution and
interpretation; this reverses the former default against Game ownership. The concluded
[diagnostic-evidence research](../research/investigations/factory-design-game-diagnostic-evidence.md)
admits no method: an actual Game utilization or starvation method stays unadmitted until it passes
the method acceptance gate in [Diagnostic claims and evidence](#2-diagnostic-claims-and-evidence),
and playable implementation until the [playable implementation gate](../planning/factory-design-game-vertical-slice.md)
admits it. Research-local formulas do not become production definitions. A Game method describes
supported outcomes, never reconstructs Engine scheduling or unseen processing starts, declares a
causal bottleneck from occupancy alone, or treats Engine's observed-time `throughputPerTick` as a
rate over a user-selected period.

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
