# Runtime observation and event contract

Status: Adopted semantic contract; headless `FactoryRuntime` implementation complete. No current outward adapter exists; a future one is introduced from this contract when a concrete product need exists.
Owning architecture: [Architecture Overview](overview.md#core-architecture-philosophy-events-state-observations)
Engine interpretation: [Engine Semantics v1](engine-semantics-v1.md)
Evolution rule: [Semantic evolution and support](overview.md#semantic-evolution-and-support)

## Purpose

The repository contains several things called or treated as events that do not mean the same
thing: `com.arcogine.core.event.Event`/`EventPayload` are deterministic scheduler and transition
machinery; `FactoryRuntime.advance()` returns internal processed events and
`CommandResult.scheduledEvents()` reports internal events scheduled as a direct command effect.
Promoting any of those directly into the long-term consumer contract would couple external
compatibility to scheduler internals and blur "transition attempted" from "authoritatively applied".

This contract therefore defines a durable semantic boundary for current authoritative state and
ordered authoritative runtime change without making Arcogine event sourced, without making SSE or
another transport part of the engine domain, and without creating a second authored model. It is
implemented headlessly by `FactoryRuntime.observe()`, `RuntimeObservation`, `RuntimeEventEnvelope`
and the `RuntimeEventType`/`RuntimeEventPayload`/`AffectedEntityRef` taxonomy; the
[Overview](overview.md#state-ownership-table) records the current implementation state.

## Separate internal transition events, supported runtime observations, and supported runtime events

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

The supported runtime contract introduces separate runtime-observation and runtime-event types. A first implementation may map many internal events closely, but it must not define the supported envelope as a wrapper around `Event` or expose `EventPayload` as its payload type.

The conceptual contract is:

> Internal events drive deterministic simulation. Supported observations expose current authoritative state. Supported runtime events expose ordered authoritative change. Transports only project those contracts.

Not every internal `Event` must produce a supported `RuntimeEvent`, and a supported runtime event need not remain permanently one-to-one with one internal scheduler event.

## Arcogine is not event sourced

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

Deterministic rerun, exact checkpoint/restore, durable history, and transport recovery are related but separate capabilities. Distribution/recovery/checkpoint hardening remains a later responsibility.

## Runtime events are published only after authoritative processing

A supported runtime event describes an authoritative transition that has actually been applied to supported runtime state.

Sequence allocation and supported publication occur after the relevant authoritative state transition succeeds and the event payload can be derived from that resulting state.

A rejected command or rejected/failed transition must not emit a successful state-change `RuntimeEvent` for a change that did not become authoritative.

If a command is accepted and a later execution cascade faults after partial authoritative mutation, the consumer-neutral session-control distinction between acceptance and execution outcome is preserved. The supported runtime-event contract reports only the authoritative changes that actually occurred; it does not pretend an all-or-nothing transition happened when it did not. Fault/result reporting remains distinct from state-change event publication.

The retired legacy API's `SimThread` SSE path used to log and notify internal events before `handleEvent(...)` completed. That was migration debt owed to this contract, not the semantic model defined here; a future outward adapter must not repeat it.

## Every runtime has explicit run identity and a per-run sequence epoch

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

## The minimum supported runtime-event envelope is transport neutral

Every supported runtime event carries semantics equivalent to:

```text
runId
sequence
simulationTime
eventType
modelFingerprint
engineSemanticsVersion
controlledRevisionId [optional when authoritatively bound]
affectedEntityRefs[]
payload
```

The Java type names and payload decomposition may vary by implementation, but these responsibilities are stable.

`eventType` is a supported semantic event taxonomy distinct from `EventType`.

`affectedEntityRefs` provide stable correlation without forcing consumers to parse domain-specific payloads merely to identify affected runtime entities. Entity references must preserve domain identity rather than introducing stringly typed replacement identities.

The [unit-work decomposition](engine-semantics-v1.md#3-unit-work-decomposition-semantics) correlation is part of the supported contract. Events concerning a child work item must preserve its `JobId` and enough parent correlation to identify the owning `OrderId`. The aggregate order-completion event preserves both explicit `OrderId` and the completing child `JobId`.

## Supported observations expose authoritative current state and the event cursor they include

Supported runtime observations are consumer-neutral outward runtime projections, distinct from both internal domain observations used for deterministic decisions and API/UI DTOs used for a specific wire representation.

A supported runtime observation carries at least:

```text
Run
    runId
    modelFingerprint
    engineSemanticsVersion
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

## Model fingerprint and Engine semantics are mandatory provenance; controlled revision is conditional provenance

Every supported runtime observation and runtime event carries the durable `ModelFingerprint` of the published `FactoryModelVersion` that instantiated the runtime and the `EngineSemanticsVersion` fixed for the run (see the [Determinism Contract](overview.md#determinism-contract)). `FactoryRuntime` also exposes its fixed semantics version directly so headless callers can read it without first observing or draining events. Propagating `EngineSemanticsVersion` into the observation/event metadata types is a known implementation gap; the runtime already fixes and reports one version.

A `ControlledRevisionId` is carried only when the runtime was actually instantiated with an authoritative controlled-revision binding supplied by the owning revision/repository boundary.

The runtime must not generate, infer, or synthesize a controlled revision merely to fill the field, and the simulation Engine does not take ownership of authoritative revision persistence or resolution.

Thus:

```text
ModelFingerprint
    mandatory semantic source identity

EngineSemanticsVersion
    mandatory result-affecting interpretation identity

ControlledRevisionId
    optional historical occurrence identity
    present only when authoritatively supplied
```

This permits Governance provenance integration without making the runtime contract duplicate authoritative revision persistence.

## Internal scheduler machinery is not supported runtime history

Internal scheduler events remain execution machinery; their availability during processing does not
create a retained history or replay contract. Supported observations and ordered runtime events
remain the outward semantic boundary.

If a future consumer requires retained supported events, its ownership, retention, and recovery
semantics must be defined explicitly. A bounded history is not a durable audit ledger, and recovery
must detect dropped events rather than silently treating an incomplete sequence as complete.

## Transport mechanisms are adapters, not the event contract

HTTP/SSE, WebSocket, Kafka, NATS, MQTT, an embedded Java API, or later operational adapters are projections of the same transport-neutral runtime contract. None is a dependency of the simulation core.

The retired legacy API's SSE design, where each internal `EventType` became an SSE `event:` name, is not the target compatibility boundary. A future SSE-based adapter should use one stable transport event name such as `runtime-event`, use the supported `sequence` as the SSE message ID, and carry the semantic `eventType` inside the envelope.

This avoids requiring transport-listener registration changes whenever a supported semantic event type is added and prevents the transport taxonomy from becoming the domain taxonomy.

CloudEvents or another integration envelope may later be an adapter representation, but it is not the Arcogine domain type and is not required by this runtime contract.

## Recovery uses snapshot/resynchronization semantics, not mandatory full replay

The supported runtime contract establishes the primitives needed for later recovery:

- run identity;
- monotonic supported-event sequence;
- `latestEventSequence` on observations;
- transport-neutral supported events.

Distribution hardening defines retention, resume cursors, reconnect behavior, gap detection, and exact checkpoint/restore separately.

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

## Challenge/Game and Operational Execution remain consumers/siblings, not owners

Challenge/Game code may consume supported Engine observations/outcome facts but does not define runtime event semantics, reconstruct authoritative queues/dispatch from event replay, or make challenge scoring part of the runtime event contract. The playable consumer continues to depend on a complete supported runtime observation/event contract and deterministic spatial runtime consequences as specified by its own planning initiative.

Operational Execution / Digital Twin remains a sibling track. These events are simulation-runtime events. They do not define production telemetry envelopes, source authenticity, operational actor/target identity, actuation acknowledgements, deployment provenance, external-observation ingestion, or modeled-versus-observed reconciliation.

An Operational adapter may later translate relevant Arcogine semantics, but production trust and consequence cannot be inferred from simulation event-stream maturity.
