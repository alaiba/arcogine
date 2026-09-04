# Gate 4 Runtime Observation and Event Delivery

> **Status:** Active implementation plan  
> **Owner:** Factory Simulation Engine Readiness / Gate 4  
> **Architecture authority:** [ADR-0011](../architecture/decisions/0011-runtime-observation-and-event-contract.md)  
> **Parent plan:** [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md)  
> **Upstream execution identity:** [ADR-0010](../architecture/decisions/0010-intra-order-execution-decomposition-and-work-item-identity.md) / W1  
> **Session boundary:** [ADR-0007](../architecture/decisions/0007-consumer-neutral-session-control-primitives.md) / Gate 3

## 1. Purpose

This file is the implementation/delivery companion for Engine Readiness Gate 4. It records the execution order, code boundaries, acceptance evidence, transport migration, and cross-track responsibilities needed to implement ADR-0011 without turning event streaming into a separate Arcogine initiative.

Event streaming is not a new top-level track. Its semantic owner is Engine Readiness Gate 4; transport recovery/versioning maturity remains Engine distribution hardening. Governance, Challenge/Game, and Operational Execution remain sibling or downstream tracks with their existing ownership boundaries.

The core rule is:

> Internal events drive simulation. Supported observations expose current authoritative state. Supported runtime events expose ordered authoritative change. Transports only project those contracts.

## 2. Current repository position

As of 2026-09-01:

- Gate 1 is complete.
- Gate 2 is complete.
- Gate 3 is complete through `FactoryRuntime` and ADR-0007.
- W1 is complete through ADR-0010, the implemented `Order -> Job*` child-job model, and the executable 100,000-child large-order benchmark.
- Gate 4 core/headless closure is complete through G4-A, G4-B, and G4-C: the consumer-neutral engine boundary carries stable observation/event contracts around the correct `OrderId` aggregate and child `JobId` work-item identities, with executable acceptance evidence. G4-D (outward consumer convergence) remains outstanding.
- Gate 5 may proceed; Gate 4 core closure no longer blocks it.
- public-contract versioning, recovery, checkpoint/restore, persistence, and packaging remain later distribution hardening.

The current legacy API is not the Gate 3 session implementation. `interfaces/api` still has its own `SimThread`/`IntegratedHandler` loop and streams internal scheduler `Event` objects directly over SSE. That path is maintained current behavior, not the architectural owner of Gate 4.

The current CLI headless `run` path also remains on `ScenarioLoader -> HeadlessHandler -> SimRunner.runScenario(...)` rather than the newer `FactoryRuntime` seam. That broader orchestration path is a downstream consumer-convergence decision after the Gate 4 headless contract is stable, not a reason to widen `FactoryRuntime` prematurely.

## 3. Current implementation debt Gate 4 must not fossilize

### 3.1 Internal Event is still scheduler machinery

`product/simulation/.../Event.java` currently contains only:

```text
simulation time
event type
payload
```

It has no run identity, supported sequence, durable model provenance, or external compatibility contract. This is correct for scheduler machinery and should remain possible after Gate 4.

### 3.2 `FactoryRuntime` still returns internal events

Gate 3's `FactoryRuntime.advance()` / `advanceUntil(...)` return processed internal `Event`s. `CommandResult.scheduledEvents()` captures the internal events scheduled as a direct command effect.

Those are useful headless control/evidence surfaces but are not the supported runtime-event contract. Gate 4 must add separate supported types rather than silently rebranding those internal values.

### 3.3 Legacy `SimThread` publication is pre-authoritative in several paths

The current API loop can perform:

```text
EventLog.append(event)
notifyListeners(event)
handler.handleEvent(event, scheduler)
```

That means the current SSE stream can expose a transition attempt before authoritative processing succeeds. Gate 4's supported publication rule is the opposite: derive/publish a successful state-change event only after authoritative processing has occurred.

This is a migration target, not a reason to weaken ADR-0011.

### 3.4 Current SSE is coupled to internal event taxonomy

`SseController` currently:

```java
.name(event.eventType().name())
.data(event, MediaType.APPLICATION_JSON)
```

The current API reference documents that exact behavior. It remains current-state truth until the API migration lands, but it is not the target domain boundary.

When migrated, SSE should be a thin adapter over supported runtime events. Prefer one stable SSE transport event name (for example `runtime-event`), the supported event sequence as the SSE message ID, and semantic `eventType` inside the envelope.

### 3.5 `EventLog` is not the supported recovery journal

The current `EventLog` is a bounded in-memory trace of internal events. It is suitable for simulation trace/debug/export behavior. It does not supply a durable authoritative external history or gap-aware recovery contract.

Do not rename or repurpose it into the Gate 4 history. If retained supported events are needed, introduce a separately named responsibility such as `RuntimeEventJournal` or `RuntimeEventHistory`.

## 4. Delivery sequence

### Slice W1-B — Complete: close W1 with large-order benchmark evidence

This prerequisite is complete. `LargeOrderDecompositionBenchmarkTest` executes the supported 100,000-child materialization ceiling, proves deterministic decomposition and terminal semantics, and records diagnostic memory/execution measurements. The existing W1 acceptance suite preserves the fixed-contract and pre-mutation over-limit rejection evidence.

Required work:

- benchmark the implemented unit-child representation at a large accepted quantity (the parent plan currently uses 100,000 as the motivating scale);
- record resident-memory and execution/event-volume behavior;
- verify that the current provisional child-materialization limit is justified or adjust it with evidence;
- determine the evidence-backed supported quantity envelope;
- keep the benchmark non-semantic: it must not introduce arbitrary batching/chunking merely to improve performance, because configurable batch semantics are explicitly outside W1;
- update W1 status only when the benchmark evidence is committed and reproducible. **Complete:** the parent Engine Readiness plan records W1 complete and Gate 4 active.

This slice must not redesign Gate 4.

### Slice G4-A — Complete: headless runtime identity and supported observation contract

Add the minimum runtime metadata and observation seam at the `FactoryRuntime` boundary.

Responsibilities:

```text
RunId (or equivalently named opaque run identity)
FactoryRuntime owns one RunId
reset() -> fresh runtime -> fresh RunId
model fingerprint from FactoryModelVersion.fingerprint()
latest supported event sequence
consumer-neutral supported observation projection(s)
```

Minimum observation responsibilities are defined by ADR-0011 and the Engine plan: run/source provenance, simulated time/run state, resources, aggregate order progress, child work-item state/correlation, and supported performance facts.

`latestEventSequence` starts at `0` before any supported runtime event.

Do not put Spring DTOs or frontend DTOs in this module. Do not create a generic "world state" type if purpose-specific projections preserve a better capability boundary.

**Implemented evidence (2026-09-02):** `FactoryRuntime.observe()` now returns one immutable,
consumer-neutral current-state projection. Its metadata carries a fresh opaque `RunId`, the durable
`FactoryModelVersion.fingerprint()` (never the legacy content hash), the `SimTime` as of the latest
supported boundary, an explicit runtime advancement state (`ACTIVE` when authoritative work is
pending, otherwise `QUIESCENT` — see the REV-003 correction under G4-C), and `latestEventSequence` (`0` before any supported runtime event; G4-B below
wires this cursor to real supported-event sequencing). It is not derived from internal scheduler
events. The projection contains resources
(operational state, active child work, per-resource queue depth, and busy ticks), aggregate orders,
W1 child jobs with `JobId -> OrderId` correlation and ordinal, cross-machine pending work, and the
authoritative backlog/completed-order/completed-sales/lead-time/throughput aggregates already owned
by the factory runtime. Collections are immutable and ordered by stable identities. `reset()` uses
fresh construction and therefore receives a fresh `RunId` while preserving the same semantic
outcome for the same model and commands. `Gate4RuntimeObservationAcceptanceTest` proves these
facts without the API, Spring, frontend, internal-store access, or scheduler-event replay.

G4-B (supported runtime-event taxonomy and post-authoritative publication) and G4-C (headless
event/observation closure) are now implemented (see below); G4-D (outward consumer convergence)
remains outstanding.

### Slice G4-B — Complete: Supported RuntimeEvent contract and post-authoritative publication

Introduce separate types equivalent to:

```text
RuntimeEventEnvelope
RuntimeEventType
RuntimeEventPayload
AffectedEntityRef / typed entity correlation
```

The envelope carries:

```text
runId
sequence
simulationTime
eventType
modelFingerprint
controlledRevisionId [optional when authoritatively bound]
affectedEntityRefs[]
payload
```

Implementation rules:

- `RuntimeEventType != EventType`;
- `RuntimeEventPayload != EventPayload`;
- do not define `RuntimeEventEnvelope(..., Event event)`;
- allocate public sequence only for supported runtime events;
- allocate sequence after the authoritative transition succeeds;
- multiple supported events may share one simulated timestamp;
- rejected commands/transitions do not emit a successful state-change runtime event;
- a fault after partial mutation reports only authoritative changes that actually happened;
- the event mapper may inspect resulting authoritative state to enrich a supported payload/correlation;
- W1 child events preserve `JobId` and parent `OrderId`; aggregate order completion preserves both parent and completing child identities.

A run ID is correlation metadata only. It must not participate in simulation decisions or make deterministic outcomes differ.

**Implemented evidence (2026-09-02):** `RuntimeEventEnvelope`, `RuntimeEventType`, `RuntimeEventPayload`,
and `AffectedEntityRef` (`product/domains/factory/.../process/`) introduce the supported,
consumer-neutral event contract described above; none of them wrap or expose the internal scheduler
`Event`/`EventType`/`EventPayload`. `FactoryRuntime` now derives and appends a `RuntimeEventEnvelope`
only after the corresponding authoritative transition has already succeeded:

- `submitWorkload` emits `ORDER_ACCEPTED` (carrying the created child `jobIds`, in ordinal order)
  after `FactoryHandler.submitOrder` returns successfully, then emits one `JOB_DISPATCHED` or
  `JOB_WAITING` per created job describing the resulting placement (dispatched to a machine, waiting
  in that machine's own single-eligible queue, or waiting in the cross-machine multi-eligible
  backlog) — so a consumer can reconstruct the job creation/assignment/pending-work deltas a
  quantity>1 submission produces, not just the order-level acceptance fact;
- `setMachineAvailability` emits `MACHINE_AVAILABILITY_CHANGED` only when `Machine#setAvailability`
  actually transitioned the machine's online/offline state — a request that is a no-op (e.g. bringing
  an already-online machine online again) emits nothing and does not advance the sequence — including
  on the `Faulted` path, since that mutation genuinely happened before a later dispatch-cascade fault,
  and only that mutation is reported. When a genuine transition to online triggers a dispatch cascade,
  every job that moved from waiting to dispatched as a result is additionally reported via
  `JOB_DISPATCHED`, derived by diffing authoritative job state before/after the call;
- `advance()` derives `JOB_STEP_COMPLETED` from a successfully processed internal `TaskEnd` event
  (inspecting the resulting `JobView`, never copying the internal payload) and, when that same call's
  internal scheduler activity also produced an internal `OrderCompleted` event (proving the order's
  full execution aggregate already completed), additionally emits `ORDER_COMPLETED` carrying both the
  order and completing child `JobId` for W1 correlation (ADR-0010). Both derivations run in a
  `finally`, so a step completion that occurred before a later cascade fault is still reported
  (`Gate4BRuntimeEventAcceptanceTest.faultReportsOnlyAuthoritativeChangesThatActuallyOccurred`).

Sequence is allocated only at emission (`FactoryRuntime.emit`), strictly increasing per run,
independent of how many internal scheduler events were involved; `RuntimeObservationMetadata
.latestEventSequence()` is now the live cursor (no longer hardcoded to `0`) and always equals the
last emitted envelope's sequence, independent of when a caller drains the event list (see below).
`controlledRevisionId` is always `Optional.empty()` in this slice — G4-B does not bind to or
synthesize one, since no established authoritative binding contract exists yet. This is unrelated to
Governance G1 completion status: G1 is complete (`docs/planning/governance-g1-continuity.md`), but
Gate 4 still treats revision provenance as an optional binding because G1 completion does not by
itself mean any given runtime was instantiated from an authoritative controlled revision.
`modelFingerprint` on every envelope is `FactoryModelVersion.fingerprint()`, never the legacy content
hash. `FactoryRuntime.drainSupportedEvents()` is the read-only accessor a caller uses instead of
internal `Event` values: it returns and clears everything accumulated since it was last called.
It is deliberately draining, not retained/replayable-by-cursor — an unbounded, cursor-addressable
supported-event history is a separately-named responsibility for later distribution hardening
(ADR-0011 §8, DH-E), not part of this contract; a caller that needs durable replay retains the
drained events itself. `advance()`/`advanceUntil` keep returning internal `Event`s unchanged for
existing Gate 3 callers. `Gate4BRuntimeEventAcceptanceTest` proves: run/sequence/time/model provenance
on the envelope; the enriched `ORDER_ACCEPTED` job-id list and the `JOB_DISPATCHED`/`JOB_WAITING`
events it implies; strict monotonic sequencing and same-timestamp sequence ordering within one run;
observation cursor/state consistency with the applied event log; that a rejected command emits
nothing; that a no-op availability request emits nothing; that a faulted command reports only the
mutation that actually occurred; W1 `OrderId`/`JobId` correlation on both `JOB_STEP_COMPLETED` and
`ORDER_COMPLETED`; identical semantic event streams for identical inputs (run ID excluded from the
comparison); a fresh sequence epoch on `reset()`; and that two distinct run IDs replaying the same
commands converge to the same event-type sequence and terminal state (run identity does not influence
simulation outcome).

G4-C (headless event/observation closure across the full acceptance list, including
`freshObservationReconstructsCurrentConsumerViewWithoutReplay` and
`apiDtosDoNotReenterDomainDecisionPaths`) landed separately and is recorded under its own slice
below; G4-D (outward consumer convergence: SSE/API DTO migration, frontend, CLI) remains
outstanding and is not claimed here. No persistence, recovery,
checkpoint, or replay semantics were introduced; `FactoryRuntime.drainSupportedEvents()` is a
draining, non-retained accessor only — it is not a cursor-addressable or replayable journal (that
remains Slice DH-E).

### Slice G4-C — Complete: Gate 4 headless acceptance closure

Gate 4 is closed through the consumer-neutral engine boundary before any legacy transport is used as architectural evidence.

Acceptance evidence should include tests equivalent to:

```text
runtimeEventCarriesRunSequenceTimeAndModelProvenance
sequenceIsStrictlyMonotonicWithinOneRun
sameTimeEventsRemainOrderedBySequence
observationLatestSequenceMatchesAppliedRuntimeEvents
observationReflectsStateReportedByLastRuntimeEvent
rejectedTransitionDoesNotEmitSuccessfulStateChange
faultReportsOnlyAuthoritativeChangesThatActuallyOccurred
w1EventsPreserveOrderIdAndJobIdCorrelation
identicalInputsProduceIdenticalSemanticEventStreams
freshObservationReconstructsCurrentConsumerViewWithoutReplay
resetCreatesNewRunAndSequenceEpoch
runIdentityDoesNotInfluenceSimulationOutcome
apiDtosDoNotReenterDomainDecisionPaths
```

Deterministic stream comparisons ignore or inject intentionally unique run IDs while comparing all semantic event content/order.

Gate 4 also requires a supported observation that is sufficient for a consumer to identify the active bottleneck without reaching into internal stores or replaying raw events.

**Implemented evidence (2026-09-02):** the acceptance list above is executable and passing. Most of
it was already proved by the G4-A/G4-B suites, which G4-C deliberately reuses rather than
duplicating: `Gate4BRuntimeEventAcceptanceTest` owns
`runtimeEventCarriesRunSequenceTimeAndModelProvenance`, `sequenceIsStrictlyMonotonicWithinOneRun`,
`sameTimeEventsRemainOrderedBySequence`, `observationLatestSequenceMatchesAppliedRuntimeEvents`,
`observationReflectsStateReportedByLastRuntimeEvent`,
`rejectedTransitionDoesNotEmitSuccessfulStateChange`,
`faultReportsOnlyAuthoritativeChangesThatActuallyOccurred`,
`w1EventsPreserveOrderIdAndJobIdCorrelation`, `identicalInputsProduceIdenticalSemanticEventStreams`,
`resetCreatesNewRunAndSequenceEpoch`, and `runIdentityDoesNotInfluenceSimulationOutcome`;
`Gate4RuntimeObservationAcceptanceTest` owns the observation projection, immutability, and
fresh-`RunId`-without-changed-outcome facts.

G4-C adds the three remaining closure facts, plus two review-driven regression facts (REV-002,
REV-003) described after them, in
`product/domains/factory/src/test/java/com/arcogine/factory/process/Gate4CHeadlessClosureAcceptanceTest.java`:

- `freshObservationReconstructsCurrentConsumerViewWithoutReplay` — drives a runtime to a
  non-trivial mixed state (one order complete, a second order's child jobs simultaneously
  completed, in progress, and queued), drains and **discards** every supported event emitted so
  far, then rebuilds the whole supported consumer view through a pure function of one
  `FactoryRuntime.observe()` result. No `FactoryHandler`/mutable store, no scheduler `Event` or
  `EventLog` replay, no previously drained `RuntimeEvent`, and no API/Spring/frontend DTO is
  available to that reconstruction. It also asserts that `latestEventSequence` survives draining, so
  a late-joining consumer knows exactly where to continue the stream from;
- `observationAndSupportedEventsCloseOverTheSameAuthoritativeTransitions` — proves semantic closure
  in both directions: the supported events emitted after an observation at sequence `S` are exactly
  `S+1..S'` with no gap or overlap against the later observation's cursor; every transition those
  events report is consistent with the later observation's authoritative state; and the state the
  later observation reports that the earlier one did not is explained by the delta. Events are not
  required to carry redundant full-state payloads;
- `supportedObservationIdentifiesTheActiveBottleneckWithoutInternalAccess` — runs an unbalanced
  two-stage routing (fast prep, slow finish) and identifies the finishing resource as the active
  bottleneck from supported `ResourceObservation` facts alone, independently by carried load
  (active plus queued work) and by busy-tick utilization, with deterministic tie-breaking by
  machine id and a reproducible result across an equivalent fresh run.

Review of the slice surfaced two genuine closure gaps, each fixed in production code and pinned by
its own regression fact in the same test:

- `taskEndDispatchCascadeIsReportedByTheSupportedEventStream` (REV-002) — a `TaskEnd` frees machine
  capacity, and `FactoryHandler` re-places not only the completing job onto its next routing step
  but also whatever queued or multi-eligible backlog work that machine can now accept. Emitting
  only `JOB_STEP_COMPLETED` left a consumer unable to derive those jobs' status or machine
  assignment from the supported stream, so the closure claim did not actually hold on the `TaskEnd`
  path;
- `processingANoOpInternalMarkerLeavesTheSupportedObservationUnchanged` (REV-003) — the internal
  scheduler also carries markers `FactoryHandler.handleEvent` ignores (the `TaskStart` paired with
  every dispatched `TaskEnd`; the `OrderCompleted` a terminal `TaskEnd` schedules purely so other
  internal handlers can observe completion). Processing one is authoritatively a no-op and emits no
  supported event, yet it moved the scheduler cursor, which `observe()` read directly — so two
  observations at the same `latestEventSequence` could report different `currentTime`, throughput,
  or `runState`, contradicting the supported model.

`apiDtosDoNotReenterDomainDecisionPaths` is a structural rather than behavioural fact and is
enforced as `ArchitectureTest.api_dtos_must_not_reenter_domain_decision_paths` in
`interfaces/api` — the only module whose test classpath can see both sides of the boundary — which
fails the build if anything in `com.arcogine.factory..` ever depends on `com.arcogine.api..`,
`org.springframework..`, or `jakarta.servlet..`.

Three production changes were required, all of them corrections to facts the existing contract
already claimed rather than new surface:

- **Busy-tick accumulation.** `Machine.busyTicks()` was never accumulated anywhere in the product,
  so `ResourceObservation.busyTicks()` advertised a utilization fact that was permanently zero.
  `FactoryHandler.handleTaskEnd` now credits the just-finished step's duration to the resource that
  performed it, which is the only point where a machine is known to have occupied itself for
  exactly that long.
- **`TaskEnd` placement cascade (REV-002).** `FactoryRuntime.advance()` snapshots the placement
  (status, machine, step) of every job currently occupying machine capacity before processing a
  `TaskEnd`, and diffs it against authoritative state afterwards, emitting `JOB_DISPATCHED` for
  everything newly placed and `JOB_WAITING` for a completing job that fell back to waiting. The
  snapshot is deliberately scoped to jobs occupying capacity — bounded by published machine
  concurrency, not by backlog size — so a large order's completions stay linear rather than
  quadratic (`LargeOrderDecompositionBenchmarkTest`).
- **Supported-boundary coherence (REV-003).** `FactoryRuntime.observe()` no longer reads the
  internal scheduler cursor. Metadata time (and the throughput derived from it) come from an
  `observedTime` that advances only when a supported event is emitted, and `RuntimeRunState` comes
  from whether any *authoritative* event is still queued (`RecordingScheduler
  .hasPendingAuthoritativeWork()`, counting exactly the payloads `FactoryHandler.handleEvent` acts
  on) rather than from `Scheduler.isEmpty()`. Every observation-visible metadata and performance
  fact therefore moves in lockstep with `latestEventSequence`.

No new observation field, event type, scoring concept, or analytics surface was introduced.

No retained runtime-event journal, replay-by-cursor, `Last-Event-ID`, gap detection, or
checkpoint/restore behaviour was added — that remains Slice DH-E — and no transport, DTO, frontend,
or CLI behaviour changed, which remains Slice G4-D.

### Slice G4-D — Converge outward consumers on supported runtime semantics

Only after G4-C establishes the headless contract should existing outward consumers migrate or explicitly adapt to it. This is downstream migration/integration work, not a prerequisite for Gate 4 core closure or Gate 5.

The objective is **semantic convergence**, not forcing every execution path to reuse one concrete runtime type. `FactoryRuntime` remains factory-simulation scoped unless a later architecture decision broadens it; economy/finance/agent orchestration must not be distorted merely to obtain concrete-type reuse.

Current consumer inventory:

- `interfaces/api` uses `SimThread` / `IntegratedHandler` and exposes internal scheduler `Event` values over SSE;
- `arcogine run` uses `ScenarioLoader -> HeadlessHandler -> SimRunner.runScenario(...)` and may legitimately remain broader than the factory-only session boundary;
- the Gate 4 acceptance/reference consumer must prove supported observations/events without UI-specific DTOs or undocumented internal stores;
- the frontend is a consumer of the API projection and must not become a semantic authority.

Target dependency direction:

```text
       supported Engine semantics
     RuntimeObservation / RuntimeEvent
          |                 |
          |                 +--> API adapter --> HTTP / SSE --> frontend
          |
          +--> headless/reference consumer
          |
          +--> CLI adapter where semantically applicable
```

Where a broader execution path cannot directly reuse `FactoryRuntime` without changing its legitimate scope, keep the broader orchestration and introduce or preserve an explicit adapter/projection boundary. The adapter may translate supported Engine semantics; it must not redefine them.

#### G4-D1 — Legacy API/SSE migration

Migrate `interfaces/api` so supported observations/events, not internal scheduler events, define outward runtime semantics. The exact implementation may require an adapter because today's `SimThread` runs a broader scenario/economy/finance/agent integrated loop rather than `FactoryRuntime`.

When migrated, update in the same slice:

- `SseController`;
- outward API event DTO;
- frontend SSE client/store;
- SSE integration/contract tests;
- `docs/reference/api.md`.

Do not update the API reference before behavior changes: that document is a current-state reference.

Preferred SSE shape:

```text
id: <supported sequence>
event: runtime-event
data: <supported envelope DTO>
```

The semantic runtime event type stays inside the data envelope. Internal `Event`, `EventPayload`, and `EventLog` remain implementation machinery and must not become outward compatibility types.

#### G4-D2 — CLI and reference/headless consumer convergence

Evaluate the current CLI/headless path against the supported runtime contract after G4-C:

- where factory-only execution can consume the supported runtime/session boundary directly without changing semantics, migrate to that boundary;
- where `arcogine run` legitimately owns broader scenario/economy/finance/agent orchestration, retain that orchestration and document the deliberate adapter/projection boundary rather than widening `FactoryRuntime` for convenience;
- provide or preserve a consumer-neutral headless/reference path that can observe `RuntimeObservation` and ordered `RuntimeEvent` semantics without depending on Spring, frontend DTOs, internal scheduler stores, or raw `EventLog` replay;
- remove duplicated outward observation/event semantics where practical; where reuse would be semantically wrong, make the separate responsibility explicit in code/tests/current planning.

G4-D acceptance requires:

- the CLI/API consumer inventory and direct-consumption versus adapter decisions are documented in current planning or implementation docs;
- legacy HTTP/SSE projects the supported runtime semantics rather than internal scheduler event taxonomy;
- headless/reference execution can prove and consume the supported runtime contract without UI-specific code or undocumented internal stores;
- transport/CLI DTOs remain projections and never re-enter domain decision paths;
- broader economy/finance/agent orchestration is not forced through `FactoryRuntime` solely for type reuse;
- behavior-changing slices update current-state docs in the same PR, while unchanged current-state references remain untouched;
- no new integration framework, protocol selection, generic event bus, or cross-domain interchange ontology is introduced to accomplish convergence.

### Slice DH-E — Recovery and resynchronization hardening

This is not a Gate 4 core blocker and must not hold up Gate 5.

Distribution hardening later owns:

- retained supported-event journal/history;
- oldest/latest retained cursor;
- reconnect/resume after sequence;
- explicit history-gap detection;
- run-ID mismatch handling;
- fresh-observation resynchronization;
- SSE `Last-Event-ID` or equivalent adapter behavior;
- public contract fixtures/versioning;
- exact checkpoint event position;
- durability decisions if/when retained history must survive process restart.

Recovery contract:

```text
resume cursor available
    -> replay retained deltas

cursor older than retained history
    -> RESYNC_REQUIRED (or equivalent explicit result)
    -> fetch fresh observation
    -> continue after observation.latestEventSequence

cursor belongs to old run
    -> invalid for new run
    -> fetch fresh observation
```

Silent truncation is never considered successful recovery.

## 5. Provenance integration

### Model fingerprint

`ModelFingerprint` is mandatory Gate 4 provenance. G1.1 and ADR-0006 have already established `factory-model:v1`; Gate 4 should use `FactoryModelVersion.fingerprint()` rather than legacy `contentHash()` for its supported provenance contract.

### Controlled revision

G1 is complete: G1.2 and ADR-0008 provide `ControlledRevisionId` and immutable revision/lineage values, while G1.3 provides authoritative durable revision persistence and exact historical semantic-state resolution. Gate 4 still treats revision provenance as an optional binding because G1 completion does not imply that a particular runtime was instantiated from an authoritative controlled revision.

Therefore Gate 4 uses:

```text
modelFingerprint      required
controlledRevisionId optional
```

`ControlledRevisionId` is present only when an authoritative upstream revision binding actually exists. Engine must not generate one or infer one from the fingerprint.

No Gate 4 dependency may turn Governance persistence into an Engine responsibility.

## 6. Track ownership and cross-track impact

### Factory Design

Factory Design continues to own published executable model semantics, validation/publication, model identity, and semantic layout input. Gate 4 consumes the published model and its durable fingerprint; it does not author another model/version system.

### Engine Readiness

Engine owns:

- run/session runtime identity;
- runtime observations;
- runtime-event semantic taxonomy/envelope;
- runtime-event ordering;
- W1 order/work correlation;
- later simulation event recovery/versioning/checkpoint integration.

This is the only semantic owner of the Gate 4 runtime-event contract.

### Governance / Conformance

Governance owns:

- controlled historical revision identity/lineage;
- authoritative revision persistence/resolution (G1 complete);
- ChangeSets and impact scoping (G2 complete), requirements/assertions (G3 complete), conformance evaluation and findings (G4 complete);
- evidence use, governed changes/exceptions in later gates (G5+, not yet implemented).

Runtime events are not Governance evidence records by default. A later evidence-use boundary may reference selected runtime observations/events with provenance, but RuntimeEvent, ControlledRevision, EvidenceUse, Finding, and ChangeSet remain distinct concepts.

### Challenge / Game

Challenge/Game owns challenge admissibility, scoring/evaluation policy, attempt provenance/history/comparison, rendering, and game state.

The playable game consumes supported Engine observations/outcome facts/events. It must not:

- define Gate 4 event semantics;
- reconstruct authoritative queue/dispatch state from the raw event stream;
- simulate missing Engine behavior;
- treat challenge attempt history as runtime event history;
- make scoring/evaluation outcomes part of the Arcogine runtime event taxonomy.

Challenge evaluators should prefer final supported outcome facts/observations for evaluation rather than replaying the runtime stream as the source of truth.

### Operational Execution / Digital Twin

Operational Execution remains the owner of real-world consequence:

- production execution-context identity;
- verified actor/source/target trust and authorization;
- command/acknowledgement/result lifecycle;
- deployment target/application/effective-artifact provenance;
- external telemetry/observation ingestion;
- reconciliation and drift/calibration;
- operational resilience and live-adapter recovery.

Gate 4 runtime events are simulation events. They do not become production telemetry merely because an adapter can serialize them. An operational adapter may translate shared production semantics, but it must preserve the trust/provenance/reconciliation boundary of the Operational track.

## 7. Important history distinctions

Keep these histories separate:

```text
model revision history
    ControlledRevision / Governance lineage

simulation runtime history
    RuntimeEvent sequence within one run

internal deterministic scheduler trace
    Event / EventLog

governance evidence/decision history
    EvidenceUse / Finding / ChangeSet / exception records

challenge attempt history
    game-owned attempt/evaluation records

production observation/command history
    Operational Execution-owned records
```

They can correlate through typed identities and provenance. They are not one generic event/evidence ledger.

## 8. Explicit non-goals

Gate 4 and this delivery plan do not require:

- event sourcing;
- Kafka, NATS, MQTT, WebSocket, or another broker/protocol;
- CloudEvents as an engine-domain type;
- durable external event history;
- exact checkpoint/restore before Gate 4 closure;
- a generic event bus abstraction;
- a generic rules/evaluation/evidence framework shared with Governance or Challenge;
- revision persistence inside Engine;
- production telemetry authenticity/trust/reconciliation;
- game scoring or attempt-history semantics;
- forcing broader CLI/economy/finance/agent orchestration through `FactoryRuntime` merely for concrete-type reuse;
- rewriting accepted ADRs 0005/0007/0009/0010.

## 9. PR landing strategy

Land from fresh `main` in small dependency-ordered PRs rather than maintaining a long stacked branch:

```text
PR 1  W1 large-order benchmark / close W1
  ↓
PR 2  Gate 4 headless run identity + observation seam
  ↓
PR 3  supported RuntimeEvent + post-authoritative publication
  ↓
PR 4  Gate 4 acceptance closure / plan-current-architecture reconciliation
  ↓
Gate 5 may proceed

separate downstream convergence after G4-C:
PR 5a  legacy API/SSE migration
  ↓
PR 5b  CLI/reference consumer convergence where needed
  ↓
Distribution hardening PRs for recovery/versioning/checkpoint
```

If implementation naturally makes G4-A and G4-B one reviewable atomic change, combining them is acceptable, but transport migration must remain separate so transport neutrality is demonstrable.

G4-D does not have to be one large PR. API/SSE migration and CLI/reference convergence should land as separate reviewable slices when they have different code surfaces or semantic risks; combine them only if the resulting change remains narrow and coherent.

Do not combine Gate 4 with:

- a Kafka/NATS/MQTT selection;
- Governance persistence;
- Challenge evaluation;
- Operational telemetry/reconciliation;
- Gate 5 transfer-policy decisions.

## 10. Documentation propagation rules

As implementation lands:

- `docs/planning/factory-simulation-engine-readiness.md` remains the parent status/gate authority and should be updated for completed slices/evidence;
- `docs/architecture/overview.md` receives only implemented current-state behavior, while ADR-0011 remains the accepted design authority for not-yet-landed Gate 4 details;
- `docs/reference/api.md` changes only when the HTTP/SSE behavior actually changes;
- the game-consumer planning remains downstream and should only update when entry-gate status changes or its consumption contract materially changes;
- Governance planning updates only when revision/evidence responsibilities change, not merely because Gate 4 carries existing provenance values;
- Operational Execution planning updates only if the operational adapter boundary itself changes;
- accepted ADR history is immutable; later changes to ADR-0011 require a new Accepted superseding ADR rather than editing this decision in place.

## 11. Completion condition

This delivery plan has done its job when:

1. W1 benchmark evidence is recorded and W1 status is resolved;
2. Gate 4 has headless supported observations and ordered authoritative runtime events through the consumer-neutral runtime boundary;
3. Gate 4 acceptance evidence is executable and current architecture reflects what is actually implemented;
4. outward consumer convergence is explicit: legacy API/SSE projects the supported contract, and CLI/reference execution either consumes the supported runtime boundary where semantically appropriate or documents a deliberate broader orchestration adapter boundary;
5. a consumer-neutral headless/reference path can prove and consume supported observations/events without UI DTOs, undocumented internal stores, or raw internal-event replay;
6. recovery/versioning/checkpoint work is clearly owned by distribution hardening rather than hidden inside Gate 4;
7. no knowledge required to continue the work depends on a conversational session.
