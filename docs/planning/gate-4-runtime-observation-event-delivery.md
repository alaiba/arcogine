# Gate 4 Runtime Observation and Event Delivery

> **Status:** Active implementation plan  
> **Owner:** Factory Simulation Engine Readiness / Gate 4  
> **Architecture authority:** [ADR-0011](../architecture/decisions/0011-runtime-observation-and-event-contract.md)  
> **Parent plan:** [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md)  
> **Upstream execution identity:** [ADR-0010](../architecture/decisions/0010-intra-order-execution-decomposition-and-work-item-identity.md) / W1  
> **Session boundary:** [ADR-0007](../architecture/decisions/0007-gate-3-session-control-primitives.md) / Gate 3

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
- Gate 4 is the active Engine gate and builds stable observation/event contracts around the correct `OrderId` aggregate and child `JobId` work-item identities.
- Gate 5 remains next after Gate 4 core closure.
- public-contract versioning, recovery, checkpoint/restore, persistence, and packaging remain later distribution hardening.

The current legacy API is not the Gate 3 session implementation. `interfaces/api` still has its own `SimThread`/`IntegratedHandler` loop and streams internal scheduler `Event` objects directly over SSE. That path is maintained current behavior, not the architectural owner of Gate 4.

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
`FactoryModelVersion.fingerprint()` (never the legacy content hash), the scheduler's current
`SimTime`, and `latestEventSequence = 0`. The cursor is intentionally reserved for G4-B supported
runtime events; it is not derived from internal scheduler events. The projection contains resources
(operational state, active child work, per-resource queue depth, and busy ticks), aggregate orders,
W1 child jobs with `JobId -> OrderId` correlation and ordinal, cross-machine pending work, and the
authoritative backlog/completed-order/completed-sales/lead-time/throughput aggregates already owned
by the factory runtime. Collections are immutable and ordered by stable identities. `reset()` uses
fresh construction and therefore receives a fresh `RunId` while preserving the same semantic
outcome for the same model and commands. `Gate4RuntimeObservationAcceptanceTest` proves these
facts without the API, Spring, frontend, internal-store access, or scheduler-event replay.

G4-B (supported runtime-event taxonomy and post-authoritative publication) and G4-C (headless
event/observation closure) remain outstanding.

### Slice G4-B — Supported RuntimeEvent contract and post-authoritative publication

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

### Slice G4-C — Gate 4 headless acceptance closure

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

### Slice G4-D — Migrate legacy API/SSE as a consumer

Only after G4-C establishes the headless contract should `interfaces/api` migrate.

Target direction:

```text
FactoryRuntime / supported session boundary
        |
        +--> supported RuntimeObservation
        |
        +--> supported RuntimeEvent source
                    |
                    v
              API projection
                    |
                    v
               HTTP / SSE
```

The exact migration may need an adapter because today's `SimThread` runs a broader scenario/economy/finance/agent integrated loop rather than `FactoryRuntime`. The important constraint is dependency direction: API orchestration may adapt the supported engine semantics; it must not redefine them.

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

The semantic runtime event type stays inside the data envelope.

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

G1.2 and ADR-0008 now provide `ControlledRevisionId` and immutable revision/lineage value objects. G1.3 authoritative revision persistence and exact historical semantic-state resolution are not complete.

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
- authoritative revision persistence/resolution (G1.3);
- ChangeSets, requirements/assertions, conformance evaluation, evidence use, findings, governed changes/exceptions in later gates.

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

separate follow-up:
PR 5  legacy API/SSE migration
  ↓
Distribution hardening PRs for recovery/versioning/checkpoint
```

If implementation naturally makes G4-A and G4-B one reviewable atomic change, combining them is acceptable, but transport migration must remain separate so transport neutrality is demonstrable.

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
4. the legacy API either projects the supported contract or is explicitly documented as a legacy/current compatibility path awaiting migration;
5. recovery/versioning/checkpoint work is clearly owned by distribution hardening rather than hidden inside Gate 4;
6. no knowledge required to continue the work depends on a conversational session.
