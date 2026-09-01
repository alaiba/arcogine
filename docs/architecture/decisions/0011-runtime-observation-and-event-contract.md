# ADR-0011: Runtime Observation and Event Contract

Status: Accepted
Date: 2026-09-01

## Context

Arcogine's Engine Readiness sequence has now established the prerequisites needed to stabilize externally supported simulation observations and runtime events:

- Gate 1 established explicit workload and separate order/execution semantics;
- Gate 2 established deterministic dispatch for independently dispatchable work;
- Gate 3 established `FactoryRuntime` as the consumer-neutral simulation-session/control boundary;
- ADR-0010 and W1 established the current execution identity model: one accepted quantity-`N` `Order` keeps aggregate identity/progress under `OrderId` and deterministically materializes `N` independently dispatchable unit-quantity child `Job`s identified by `JobId`.

W1's functional implementation is in place. Its required large-order performance/memory benchmark remains the final completion item before Gate 4 implementation is declared complete.

The repository also already contains several things called or treated as events, but they do not all mean the same thing:

1. `com.arcogine.core.event.Event` / `EventPayload` are deterministic scheduler and transition machinery. They may represent a scheduled transition, evaluation trigger, or orchestration/control signal.
2. `EventLog` is a bounded in-memory simulation trace used by the current API/export path. It is not a durable supported recovery journal.
3. `SimThread` currently publishes internal `Event` instances directly to SSE listeners. In several paths it logs/notifies before `handleEvent(...)` completes, so the stream can describe a dispatched transition attempt before authoritative processing has succeeded.
4. `/api/events/stream` currently uses the internal `EventType` name as the SSE transport event name and serializes the internal `Event` shape directly.
5. `FactoryRuntime.advance()` and `advanceUntil(...)` currently return internal processed `Event` instances. `CommandResult.scheduledEvents()` reports internal events scheduled as a direct command effect. Gate 3 deliberately did not claim these as stable external event-envelope contracts.

Those implementation surfaces are useful current behavior, but promoting them directly into the long-term consumer contract would couple external compatibility to scheduler internals and would make it difficult to distinguish "transition attempted" from "authoritative state changed".

The project also now has stronger provenance primitives than it did when Gate 4 was first sketched:

- ADR-0006 and G1.1 establish `FactoryModelVersion.fingerprint()` / typed `ModelFingerprint` as the durable `factory-model:v1` semantic identity contract;
- ADR-0008 and G1.2 establish `ControlledRevisionId` and immutable controlled-revision value/lineage contracts;
- G1.3 authoritative revision persistence and exact revision-to-semantic-state resolution are still outstanding.

Gate 4 therefore needs a durable semantic boundary for current authoritative state and ordered authoritative runtime change without making Arcogine event sourced, without making SSE or another transport part of the engine domain, and without creating a second Governance or Operational Execution ontology.

## Decision

### 1. Separate internal transition events, supported runtime observations, and supported runtime events

Arcogine distinguishes three concepts:

```text
Internal Event
    deterministic scheduler / transition machinery
    may be scheduled before it is processed
    may be an evaluation or orchestration trigger
             |
             v
      authoritative processing
             |
       +-----+-----+
       |           |
    rejected/    success
     fault          |
                    v
           authoritative State
              /          \
             v            v
   RuntimeObservation   RuntimeEvent
   "what is true now"  "what authoritatively changed"
```

`Event`, `EventType`, and `EventPayload` remain internal simulation-engine contracts. They are not automatically public compatibility types.

Gate 4 introduces separate supported runtime-observation and runtime-event types. A first implementation may map many internal events closely, but it must not define the supported envelope as a wrapper around `Event` or expose `EventPayload` as its payload type.

The conceptual contract is:

> Internal events drive deterministic simulation. Supported observations expose current authoritative state. Supported runtime events expose ordered authoritative change. Transports only project those contracts.

Not every internal `Event` must produce a supported `RuntimeEvent`, and a supported runtime event need not remain permanently one-to-one with one internal scheduler event.

### 2. Arcogine is not event sourced

Authoritative simulation state remains owned by the runtime domains and is not reconstructed by replaying the supported runtime-event stream.

A fresh supported observation must be sufficient to reconstruct a consumer's current view without replaying the full runtime-event history.

The supported external model is therefore snapshot/current-observation plus ordered deltas, not event sourcing:

```text
fresh observation at sequence S
        +
RuntimeEvents after S
        =
current consumer view
```

Deterministic rerun, exact checkpoint/restore, durable history, and transport recovery are related but separate capabilities. Section 11 of Engine Readiness remains the owner of distribution/recovery/checkpoint hardening.

### 3. Runtime events are published only after authoritative processing

A supported runtime event describes an authoritative transition that has actually been applied to supported runtime state.

Sequence allocation and supported publication occur after the relevant authoritative state transition succeeds and the event payload can be derived from that resulting state.

A rejected command or rejected/failed transition must not emit a successful state-change `RuntimeEvent` for a change that did not become authoritative.

If a command is accepted and a later execution cascade faults after partial authoritative mutation, Gate 3's distinction between acceptance and execution outcome is preserved. Gate 4 must report only the supported authoritative changes that actually occurred; it must not pretend an all-or-nothing transition happened when it did not. Fault/result reporting remains distinct from state-change event publication.

This deliberately differs from the current `SimThread` SSE path, which can log and notify internal events before `handleEvent(...)` completes. That legacy behavior is compatibility/current-implementation debt to migrate after the headless Gate 4 contract exists; it is not the semantic model for Gate 4.

### 4. Every runtime has explicit run identity and a per-run sequence epoch

A consumer-neutral runtime instance has an opaque `runId` (or equivalently named session-run identity) used for correlation.

A reset that creates a fresh `FactoryRuntime` creates a new run identity and a new sequence epoch, even when it uses the same published model and the same deterministic workload/commands.

Run identity is metadata. It must never participate in simulation decisions, scheduler ordering, random behavior, dispatch policy, or any other deterministic outcome. Tests comparing deterministic semantic event streams normalize or inject run identity rather than requiring independently created runs to have equal IDs.

Within one run:

- supported runtime-event `sequence` is strictly monotonic;
- `sequence` is independent of simulated timestamp;
- several events may share the same `SimTime` and remain ordered by sequence;
- the no-event initial observation reports `latestEventSequence = 0`;
- the first supported event uses sequence `1` and subsequent supported events increment by one.

A sequence is a supported-event position, not an internal scheduler-event count. Internal events that produce no supported runtime event do not consume public sequence values.

### 5. The minimum supported runtime-event envelope is transport neutral

Every supported runtime event carries semantics equivalent to:

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

The Java type names and payload decomposition may vary by implementation, but these responsibilities are stable.

`eventType` is a supported semantic event taxonomy distinct from `EventType`.

`affectedEntityRefs` provide stable correlation without forcing consumers to parse domain-specific payloads merely to identify affected runtime entities. Entity references must preserve domain identity rather than introducing stringly typed replacement identities.

W1 correlation is part of the supported contract. Events concerning a child work item must preserve its `JobId` and enough parent correlation to identify the owning `OrderId`. The aggregate order-completion event preserves both explicit `OrderId` and the completing child `JobId`, as required by ADR-0010.

### 6. Supported observations expose authoritative current state and the event cursor they include

Gate 4 observations are consumer-neutral outward runtime projections, distinct from both internal domain observations used for deterministic decisions and API/UI DTOs used for a specific wire representation.

A supported runtime observation carries at least:

```text
Run
    runId
    modelFingerprint
    controlledRevisionId [optional when authoritatively bound]
    current simulated time
    run state
    latestEventSequence

Resources
    stable resource-instance identity
    definition identity
    operational status
    queue depth
    active work/current operation
    expected completion when supported

Orders
    OrderId
    requested quantity
    released quantity
    completed quantity
    status

Work items
    JobId
    parent OrderId
    ordinalWithinOrder
    execution quantity
    current operation
    assignment
    execution state
    timing

Performance
    supported throughput/lead-time/backlog/utilization facts
```

Purpose-specific observation types are preferred over one unrestricted universal state dump where they preserve a cleaner capability boundary. Regardless of decomposition, all facts are derived from authoritative runtime state and must agree on one `latestEventSequence` boundary.

API/UI DTOs may project these supported observations, but DTO types never become domain decision inputs.

### 7. Model fingerprint is mandatory provenance; controlled revision is conditional provenance

Every supported Gate 4 observation and runtime event carries the durable `ModelFingerprint` of the published `FactoryModelVersion` that instantiated the runtime.

A `ControlledRevisionId` is carried only when the runtime was actually instantiated with an authoritative controlled-revision binding supplied by the owning revision/repository boundary.

Gate 4 must not generate, infer, or synthesize a controlled revision merely to fill the field, and Engine Readiness does not take ownership of G1.3 persistence or revision resolution.

Thus:

```text
ModelFingerprint
    mandatory semantic source identity

ControlledRevisionId
    optional historical occurrence identity
    present only when authoritatively supplied
```

This permits later Governance provenance integration without making Gate 4 wait for or duplicate G1.3.

### 8. `EventLog` and a supported runtime-event history have different responsibilities

The current `EventLog` remains an internal simulation trace of scheduler events. It is not renamed or silently repurposed into the supported external runtime-event history.

If Gate 4 or later distribution hardening needs retained supported events, use a separately named responsibility such as `RuntimeEventJournal`/`RuntimeEventHistory` behind the runtime-event source contract.

A bounded retained history is not a durable audit ledger. If retention drops events, recovery must detect the gap rather than silently pretending replay is complete.

### 9. Transport mechanisms are adapters, not the event contract

HTTP/SSE, WebSocket, Kafka, NATS, MQTT, an embedded Java API, or later operational adapters are projections of the same transport-neutral runtime contract. None is a dependency of the simulation core.

The current SSE design, where each internal `EventType` becomes an SSE `event:` name, is not the target compatibility boundary. When the legacy API migrates, SSE should use one stable transport event name such as `runtime-event`, use the supported `sequence` as the SSE message ID, and carry the semantic `eventType` inside the envelope.

This avoids requiring transport-listener registration changes whenever a supported semantic event type is added and prevents the transport taxonomy from becoming the domain taxonomy.

`docs/reference/api.md` remains a current-state reference and is updated only when that API migration is implemented; this ADR does not claim the current SSE endpoint already has the new behavior.

CloudEvents or another integration envelope may later be an adapter representation, but it is not the Arcogine domain type and is not required by Gate 4.

### 10. Recovery uses snapshot/resynchronization semantics, not mandatory full replay

Gate 4 establishes the primitives needed for later recovery:

- run identity;
- monotonic supported-event sequence;
- `latestEventSequence` on observations;
- transport-neutral supported events.

Distribution hardening later defines retention, resume cursors, reconnect behavior, gap detection, and exact checkpoint/restore.

The intended recovery shape is:

```text
client has run R / sequence S
        |
        +--> events after S still retained
        |       resume with S+1 ...
        |
        +--> S is outside retained history
        |       explicit resync required
        |       fetch fresh observation at S2
        |       continue after S2
        |
        +--> run ID changed
                old cursor is invalid
                fetch fresh observation for new run
```

A bounded journal must expose enough information to detect that a cursor has fallen behind retained history. Silent truncation is not acceptable recovery behavior.

### 11. Challenge/Game and Operational Execution remain consumers/siblings, not owners

Challenge/Game code may consume supported Engine observations/outcome facts but does not define runtime event semantics, reconstruct authoritative queues/dispatch from event replay, or make challenge scoring part of the runtime event contract. The playable consumer continues to wait for Gate 4 and Gate 5 as already specified by the game-consumer initiative.

Operational Execution / Digital Twin remains a sibling track. Gate 4 events are simulation-runtime events. They do not define production telemetry envelopes, source authenticity, operational actor/target identity, actuation acknowledgements, deployment provenance, external-observation ingestion, or modeled-versus-observed reconciliation.

An Operational adapter may later translate relevant Arcogine semantics, but production trust and consequence cannot be inferred from simulation event-stream maturity.

### 12. Gate 4 closes headlessly before legacy transport migration is allowed to define the contract

Gate 4 is proven first through `FactoryRuntime` and a headless reference consumer/acceptance tests.

The existing `SimThread`/SSE path is then migrated as a consumer of the established contract. The legacy API is not allowed to dictate the headless type shape merely because it already has an event stream.

The implementation order is:

```text
W1 large-order benchmark / W1 closure
        ↓
Gate 4 headless observation + RuntimeEvent contract
        ↓
Gate 4 deterministic acceptance closure
        ↓
legacy API/SSE projection migration

Gate 5 may proceed after Gate 4 core closure.
Recovery/resynchronization remains later distribution hardening.
```

## Alternatives considered

### Expose internal `Event` directly as the stable external event type

Rejected. It would freeze scheduler machinery, evaluation/control triggers, and `EventPayload` representation into the public compatibility contract. It also cannot cleanly express the post-authoritative-processing rule while the current legacy API can emit before handling succeeds.

### Make Arcogine event sourced

Rejected. Domain state already has clear authoritative owners, and Gate 4 explicitly requires a fresh observation to reconstruct current consumer state without full replay. Event sourcing would add a different persistence/state-ownership model without a demonstrated requirement.

### Treat `EventLog` as the supported journal

Rejected. `EventLog` records internal scheduler events, is bounded, and currently serves debugging/export concerns. Supported authoritative change and recovery semantics require a distinct contract and explicit gap behavior.

### Let SSE event names define the semantic event taxonomy

Rejected. The current transport-specific naming has already demonstrated drift risk and couples clients to every event subtype. Semantic `eventType` belongs inside the stable envelope; SSE is an adapter.

### Require `ControlledRevisionId` on every event now

Rejected. G1.2 provides the value contract, but G1.3 authoritative persistence/resolution is not complete. A revision ID is meaningful only when an authoritative revision binding exists. `ModelFingerprint` is already durable and mandatory.

### Block Gate 4 on complete recovery, persistence, or checkpointing

Rejected. Run identity, sequence, provenance, observation cursor, and supported event semantics are Gate 4 primitives. Retention/reconnect/gap handling/checkpoint durability are distribution-hardening concerns already sequenced after the core gates.

### Introduce Kafka, NATS, MQTT, WebSocket, or CloudEvents as the core implementation

Rejected. These are possible adapters/integration representations. Choosing one as the domain contract would reverse the desired dependency direction and prematurely constrain deployment.

### Create a generic cross-domain event/evidence framework shared with Governance or Challenge

Rejected. Runtime change, Governance evidence/use, controlled revision history, challenge attempt history, and operational telemetry have different ownership and lifecycle semantics. Similarity may justify later adapters or proven shared value types, not premature unification.

## Consequences

Positive consequences:

- external consumers gain a stable distinction between current truth and ordered authoritative change;
- deterministic scheduler internals remain evolvable without automatically breaking public consumers;
- W1's `OrderId`/`JobId` model is stabilized at the correct point in the Engine sequence;
- event ordering no longer depends on simulated timestamps alone;
- model provenance uses the already-established durable fingerprint contract;
- later controlled-revision provenance can be added without inventing revision identity in Engine;
- SSE and future transports become thin adapters rather than architectural owners;
- reconnect/resynchronization has explicit primitives without forcing event sourcing or durable replay into Gate 4;
- Challenge/Game and Operational Execution can consume the contract without taking ownership of it.

Costs and constraints:

- Gate 4 requires new types rather than simply reusing `Event`, `EventType`, and `EventPayload`;
- command-result scheduled events and supported runtime events must remain conceptually distinct;
- post-processing publication may require explicit mapping/enrichment from authoritative state;
- deterministic tests must normalize/inject run identity while still asserting semantic event equality;
- the current `SimThread` and SSE implementation remain migration debt until they project the supported contract;
- a later retained runtime-event journal must handle history gaps explicitly rather than inheriting `EventLog` truncation behavior.

Required Gate 4 evidence includes tests equivalent to:

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

## Charter alignment

This decision supports the Product Charter's emphasis on one executable model, explicit provenance, deterministic simulation, controlled consumer capabilities, and continuity between design/simulation/verification without conflating simulation with real-world execution.

It deliberately keeps the simulation event contract consumer-neutral and provenance-aware while preserving the separate safety, trust, deployment, telemetry, and reconciliation responsibilities that arise only when Arcogine interacts with real production systems.
