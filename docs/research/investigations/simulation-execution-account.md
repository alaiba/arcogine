# Simulation execution account semantics

> **Status:** READY — see the maintained [research register](../research-register.md)
> **Risk:** **High** — may establish a new cross-consumer semantic responsibility between Engine execution and downstream reasoning
> **Scope:** The minimum consumer-neutral meaning, if any, of one simulated execution as a coherent basis for understanding, analysis, verification, comparison, and later outward consumption
> **Authority:** Research framing only. This brief does not create an execution module, retained-history service, event-sourcing model, storage contract, analytics owner, or public API.

## Research question

> What minimum consumer-neutral semantic account, if any, of one simulated execution must Arcogine define so independent capabilities can reason about the same execution without reconstructing Engine behavior, making the Engine event sourced, or independently assembling incompatible notions of execution history?

The term **execution account** is a hypothesis under test, not an adopted architecture or required type name. A valid result may conclude that the current supported observation/event contract plus caller-owned capture is already the correct boundary.

## Decision at stake

Arcogine already distinguishes:

- Factory-authored executable model semantics;
- Engine-owned deterministic interpretation and authoritative runtime state;
- supported current observations;
- supported ordered runtime events;
- downstream consumer interpretation.

What remains unresolved is whether the semantic meaning of **one execution over time** is sufficiently owned by those contracts, or whether Arcogine needs a bounded consumer-neutral responsibility that says what belongs to the same execution, what happened in what order, which basis it ran under, and whether a retained or supplied interval is complete enough for later reasoning.

The decision must determine:

1. whether a cross-consumer execution-account responsibility exists at all;
2. if it exists, its minimum semantic boundary and owner;
3. which concerns remain Engine-owned;
4. which concerns belong only to analytical definitions or consumer interpretation;
5. which current analytics evidence/provenance concerns are actually upstream execution-account concerns;
6. whether the result changes the prospective simulation-analytics ownership question.

## Why this question exists now

The [Product Charter](../../product/charter.md) defines Arcogine as supporting design, understanding, simulation, verification, operation, monitoring, and improvement over the same executable business model. Its enduring principles require lifecycle continuity, purpose-built views over one model, causality, and provenance. Multiple consumption modes are therefore product direction, even though their final UI, adapter, deployment, and implementation shapes are not selected.

The current [runtime observation and event contract](../../architecture/runtime-contract.md) already supplies a strong consumer-neutral streaming boundary:

- opaque run identity;
- model fingerprint;
- monotonic supported-event sequence;
- simulation time;
- current observations;
- ordered authoritative runtime events;
- observation/event cursor agreement.

It also deliberately says:

- Arcogine is not event sourced;
- authoritative simulation state remains runtime-owned;
- supported events are drained rather than durable replay history;
- retained supported-event history needs an explicit owner and retention/recovery semantics when a concrete consumer requires it.

The accepted game diagnostic research then demonstrated concrete reasoning over an execution:

- machine/slot timelines;
- waiting-by-step;
- utilization over an interval;
- idle-flow/starvation characterization;
- controlled comparisons;
- future bottleneck/shifting/co-binding methods.

The reopened simulation-analytics work showed that these can often be derived without re-deciding Engine behavior, but its latest revision defaults evidence capture and execution-window custody to the first consumer. That may be correct, or it may place a more fundamental cross-consumer execution responsibility too low in the architecture.

The question is timely because deciding analytics ownership before resolving this substrate risks optimizing around an accidental evidence-custody boundary.

## Relationship to adjacent authorities

### Engine

Engine remains authoritative for deterministic simulation interpretation, mutable runtime state during a run, result-affecting decisions, supported current projections, and supported runtime-event semantics.

This investigation must not make retained history a second authoritative state or require replay to reconstruct the live runtime.

### Governance

Governance owns controlled semantic history, evidence use, conformance findings, and governed change. A simulation execution account may later be used as Governance evidence, but this investigation must not transfer Governance authority or create a second controlled-revision model.

### Operational continuity

The [Operational continuity contract](../../architecture/operational-continuity.md) defines an **accountable operational continuation**: a durable, independently continuing account of Arcogine's own operational conduct and conclusions.

That is a useful comparison, not a template to copy.

A bounded deterministic simulation execution may differ materially in lifetime, reset semantics, authority, fork/continuation meaning, relationship to external observations, durability, and reproducibility.

Do not unify simulation run identity with operational continuation identity unless evidence unexpectedly proves the same equality/lifecycle contract.

### Simulation analytics

The [simulation analytics ownership boundary](simulation-analytics-consumer-boundary.md) remains unresolved.

Phase 1's current Engine/runtime classification is not reopened merely because this question exists.

The prospective Phase 2 conclusion should consume this investigation before final independent review and reconciliation, because execution-account semantics may change whether evidence-window assembly is consumer-local, consumer-neutral, or another bounded responsibility.

### Analytics evidence/provenance

The [analytics evidence/completeness/provenance question](simulation-analytics-evidence-provenance.md) remains CANDIDATE.

This investigation may move some of its current concerns upstream — especially run/reset basis, ordered execution evidence, completeness of retained intervals, and late-join semantics. The analytics question should remain responsible for analytical-definition/result provenance and any additional evidence obligations that genuinely belong to analysis after this boundary is known.

## Candidate models

Compare at least these materially different models.

### 1. Existing boundary is sufficient

The supported observation/event contract plus caller-owned capture is the consumer-neutral execution boundary.

Each consumer that needs history retains the evidence it needs and declares its own completeness. No additional execution-account semantic owner is justified.

This model must explain how several independent consumers avoid incompatible meanings of execution boundaries, complete versus partial capture, accepted-input provenance, reset, terminal state, and late join.

### 2. Bounded execution-account semantics, representation deferred

Arcogine defines a consumer-neutral semantic account of one execution — identity, basis, ordered accepted inputs/changes, observation boundaries, completeness, and terminal/live status — without selecting storage, transport, event sourcing, or a production module.

Implementations may realize the account through caller capture, retained events, snapshots, accumulators, or another mechanism while preserving one semantic contract.

### 3. Retained execution-record capability

A concrete consumer-neutral retained record owns the relevant supported execution evidence for a run.

This model must justify why semantic correctness requires retained custody rather than only a semantic contract, and why that custody does not make Engine event sourced or duplicate Governance/Storage authority.

### 4. Analysis-owned evidence session

Execution evidence remains outside Engine and is captured by an analysis/inspection capability whose session provides completeness and provenance.

This model must explain whether non-analytical consumers such as verification or monitoring would then depend on an incorrectly narrow owner.

A hybrid may survive if live and completed executions genuinely require different mechanisms while sharing one semantic referent.

## Required semantic dimensions

For every surviving candidate, classify at least:

### Execution identity and boundary

- Is current RunId sufficient for the bounded execution referent?
- What starts an execution?
- What does reset mean?
- What, if anything, marks terminal completion?
- Can a live execution and its completed account be the same semantic execution?

### Basis and provenance

- exact Factory model/content basis;
- any controlled revision provenance when present;
- exact Engine interpretation/reference only if a concrete claim requires it;
- explicit accepted workload/commands;
- other result-affecting context that must be attributable.

Do not invent an Engine-definition identifier merely because historical reasoning sounds easier with one.

### Accepted inputs versus resulting changes

Keep distinct a caller request, accepted or rejected command result, authoritative state transition, supported runtime event, observation, and downstream interpretation.

Do not collapse the command lifecycle into one ambiguous record.

### Ordering

State which ordering is semantic: supported event sequence, simulation time, command acceptance order, and equal-time event ordering.

Do not substitute scheduler-private ordering for the supported outward contract.

### Current state versus historical account

A fresh observation remains authoritative for current runtime state.

Historical evidence may describe what happened without becoming mutable runtime truth or the state-reconstruction mechanism.

### Completeness and partial evidence

Define whether and how an account can say:

- complete from execution start through sequence N;
- complete over bounded interval [a,b);
- partial / late-joined;
- gap detected;
- terminally complete;
- unknown completeness.

Silent treatment of a partial interval as a complete execution must fail.

### Live versus completed reasoning

Determine whether consumers can incrementally reason over a live execution and later continue over the completed account without changing semantic identity or silently changing evidence guarantees.

### Retention and representation

Separate semantic necessity from mechanism.

The investigation may conclude that retained events, snapshots, accumulators, or checkpoints are useful mechanisms, but must not select database/storage technology, remote transport, event sourcing, unbounded history, public wire format, or package/module topology.

### Replay, seek, checkpoint, restore, and fork

Preserve the current distinctions among these capabilities.

A retained execution account must not become replay, checkpoint state, or fork semantics merely because those capabilities could consume it.

### Cross-consumer use

Test whether Game/Challenge, verification, future monitoring/improvement, and outward projections can consume one execution meaning without each becoming an execution-history authority.

## Proving and failure cases

At minimum, use executable/current evidence for these cases.

### 1. Two presentations over one live run

A CLI-style consumer and a web-style consumer observe the same live execution.

They may have different presentation state, but must not disagree about execution identity, event ordering, or whether their evidence is complete.

This proves representation plurality, not necessarily multiple semantic consumers.

### 2. Completed game analysis

After one run completes, derive machine/slot timelines, waiting-by-step, occupancy/utilization over a stated interval, and a named characterization.

Determine what common execution basis those computations require before their analytical definitions diverge.

### 3. Late join

A consumer starts at a non-zero event sequence.

It must not silently claim complete whole-run history.

Test whether a fresh observation plus later events is sufficient for current view while still being insufficient for historical interval claims.

### 4. Reset

Run the same model and commands, reset, and run again.

The second run has a fresh run identity and sequence epoch. No retained analysis/execution state may silently flow across the reset unless an explicit cross-run consumer owns that relation.

### 5. Equal-time events

Several supported runtime events share one simulation time.

Sequence ordering, not timestamp equality, must preserve the supported order.

### 6. Work crossing an analytical interval boundary

A processing step begins before or inside an interval and finishes after it.

Test what observation/event basis is required to account for running work without inventing hidden Engine state.

### 7. Availability change

Use the reviewed case where a multi-slot machine returns online with eligible queued work and one slot remains idle.

This tests temporal completeness and prevents a historical account from encoding a false two-state characterization.

### 8. Two analytical methods over one execution

Apply two legitimate named measurements or inference methods to one execution.

They may produce different interpretations while agreeing on the same execution evidence.

This separates execution semantics from analytics semantics.

### 9. Compare two executions

A controlled retry compares two independent runs.

The comparison must reference two execution accounts rather than silently merging their event histories or identities.

Factory/Governance change-set ownership remains distinct.

### 10. Spatial extension compatibility

Do not require spatial execution to finish this question.

Instead state what would need to extend when transfer execution adds authoritative transfer/admission facts. A correct execution-account principle should accept new Engine-owned facts without redefining what an execution account fundamentally is.

## Failure conditions

A candidate fails if it:

- reconstructs or re-decides Engine dispatch/scheduling;
- makes supported event replay authoritative runtime state;
- requires unbounded retention without a bounded use;
- conflates partial capture with complete execution evidence;
- collapses simulation execution identity into Operational continuation identity without matching lifecycle/equality evidence;
- makes Game, analytics, or a UI redefine supported runtime-event semantics;
- duplicates Governance controlled-revision authority;
- requires an exact Engine-definition identity without a concrete exact-reference use;
- chooses storage/transport/API/module shape as if it were semantic evidence; or
- cannot support the accepted game diagnostic proving cases without each consumer inventing incompatible execution-window semantics.

## Evidence expectations

A decision-quality report must include:

- current runtime observation/event contract reconstruction;
- exact run/reset/event-sequence semantics;
- current ownership of live runtime state;
- candidate comparison;
- proving-case results;
- completeness/late-join model;
- accepted-input versus resulting-change treatment;
- live versus completed execution analysis;
- relationship to Operational continuity;
- relationship to Governance evidence/history;
- explicit classification of concerns that remain analytics-owned;
- architecture/support consequences for any surviving execution-account responsibility.

Prefer executable discriminators over conceptual analogy where current code can prove the difference.

## Non-goals

Do not:

- design an analytics API;
- choose utilization or bottleneck formulas;
- implement a new module;
- introduce event sourcing;
- choose a database or storage format;
- promise unbounded event retention;
- design reconnect/SSE/WebSocket/HTTP behavior;
- implement replay/checkpoint/fork;
- redefine the Engine Determinism Contract;
- merge simulation and Operational identity;
- create a universal execution-context registry;
- change runtime code;
- reconcile architecture before independent review.

## Exit criteria

Conclude when the evidence determines one of:

1. **No additional execution-account responsibility is justified.** The current runtime contract plus explicitly defined caller-capture/completeness rules is sufficient; state those rules precisely and identify which downstream owner carries them.

2. **A bounded consumer-neutral execution-account responsibility exists.** State its minimum semantics, owner, relationship to Engine/Governance/Operational, and why several consumers need it independently of any one analytical method.

3. **A retained execution-record capability is required now.** State the concrete requirement that makes custody/retention semantically necessary, not merely convenient, and the exact support boundary.

For every outcome:

- state what changes, if anything, in the simulation-analytics ownership investigation;
- state how the analytics evidence/provenance question should be narrowed or promoted;
- state whether any current architecture/runtime contract would need revision;
- identify future reopening triggers;
- receive genuinely independent adversarial review before any architecture promotion.

## Downstream relationship

The immediate downstream sequence is:

1. this execution-account investigation;
2. resume the prospective Phase 2 simulation-analytics ownership decision using its conclusion;
3. independently review the revised analytics conclusion;
4. reconcile durable architecture/planning only after both high-risk questions have accepted evidence.

The existing Phase 2 workspace revisions remain useful evidence and candidate analysis. This brief does not accept or reject their current ownership recommendation.
