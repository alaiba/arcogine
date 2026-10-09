# Runtime observation and event contract

Status: Adopted semantic contract; headless `FactoryRuntime` implementation complete. No current outward adapter exists; a future one is introduced from this contract when a concrete product need exists.
Owning architecture: [Architecture Overview](overview.md#core-architecture-philosophy-events-state-observations)
Engine interpretation: [Engine semantics](engine-semantics.md)
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

`Event` and `EventPayload` remain internal simulation-engine contracts. They are not automatically public compatibility types.

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

A future outward adapter must not log, notify, or forward an internal event as a runtime change before the handler processing it (`handleEvent(...)`) has completed the authoritative transition.

## Every runtime has explicit run identity and a per-run sequence epoch

A simulation execution is one runtime epoch identified by the existing opaque `RunId`, from
runtime establishment onward. An evidence account is evidence about that run, not a separate
runtime entity, identity, persisted record or authoritative state. Several holders may capture
evidence about the same run without creating another execution.

A reset that creates a fresh `FactoryRuntime` creates a new run identity and a new sequence epoch, even when it uses the same published model and the same deterministic workload/commands.

Reset leaves the original runtime usable; it does not terminate that run. `QUIESCENT` means no
currently pending authoritative work, not a terminal state or a prohibition on later commands.
Runtime establishment and observations of bounded progress are distinct from a controller's
decision to stop driving or capturing a run.

Run identity is metadata. It must never participate in simulation decisions, scheduler ordering, random behavior, dispatch policy, or any other deterministic outcome. Tests comparing deterministic semantic event streams normalize or inject run identity rather than requiring independently created runs to have equal IDs.

Within one run:

- supported runtime-event `sequence` is strictly monotonic;
- `sequence` is independent of simulated timestamp;
- simulated time is non-decreasing along supported sequence;
- several events may share the same `SimTime` and remain ordered by sequence;
- the no-event initial observation reports `latestEventSequence = 0`;
- the first supported event uses sequence `1` and subsequent supported events increment by one.

A sequence is a supported-event position, not an internal scheduler-event count. Internal events that produce no supported runtime event do not consume public sequence values.

The pair `(RunId, sequence)` identifies one supported state-change fact. Neither timestamps nor
equal model fingerprints permit merging facts across runs or collapsing equal-time ordering.

## The minimum supported runtime-event envelope is transport neutral

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

`eventType` is a supported semantic event taxonomy distinct from the internal scheduler's `EventPayload` variants.

`affectedEntityRefs` provide stable correlation without forcing consumers to parse domain-specific payloads merely to identify affected runtime entities. Entity references must preserve domain identity rather than introducing stringly typed replacement identities.

The [unit-work decomposition](engine-semantics.md#3-unit-work-decomposition-semantics) correlation is part of the supported contract. Events concerning a child work item must preserve its `JobId` and enough parent correlation to identify the owning `OrderId`. The aggregate order-completion event preserves both explicit `OrderId` and the completing child `JobId`.

## Supported observations expose authoritative current state and the event cursor they include

Supported runtime observations are consumer-neutral outward runtime projections, distinct from both internal domain observations used for deterministic decisions and API/UI DTOs used for a specific wire representation.

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

## Model fingerprint is mandatory source-model provenance; controlled revision is conditional provenance

Every supported runtime observation and runtime event carries the `ModelFingerprint` of the published
`FactoryModelVersion` that instantiated the runtime (see the [Determinism
Contract](overview.md#determinism-contract)). The runtime executes the repository's current Engine
definition for its lifetime but does not expose a placeholder Engine-definition identifier. A model
fingerprint, run ID, or human development label must not be used as a substitute for an exact Engine
definition reference. If a concrete supported consumer requires that reference, the owning boundary
must define and carry it explicitly ([semantic evolution
rules](overview.md#semantic-evolution-and-support)).

A `ControlledRevisionId` is carried only when the runtime was actually instantiated with an authoritative controlled-revision binding supplied by the owning revision/repository boundary.

The runtime must not generate, infer, or synthesize a controlled revision merely to fill the field, and the simulation Engine does not take ownership of authoritative revision persistence or resolution.

Thus:

```text
ModelFingerprint
    mandatory source-model content fingerprint

ControlledRevisionId
    optional historical occurrence identity
    present only when authoritatively supplied
```

This permits Governance provenance integration without making the runtime contract duplicate authoritative revision persistence.

## Internal scheduler machinery is not supported runtime history

Internal scheduler events remain execution machinery; their availability during processing does not
create a retained history or replay contract. Supported observations and ordered runtime events
remain the outward semantic boundary.

Current delivery through `drainSupportedEvents()` returns and clears the changes accumulated since
the last drain. Two independent destructive drainers receive partial deliveries and cannot each
claim full history. One capture holder may fan out evidence to several consumers. Retention belongs
to that holder or its delegate for the horizon its use needs; this implies no central service,
physical persistence obligation or unbounded history owner.

Retained, shared or cursor-addressable delivery requires an explicit contract when a concrete
independent live consumer, late join needing earlier history, or long-horizon third-party
verification makes it necessary. A bounded history is not a durable audit ledger, and recovery
must detect dropped events rather than silently treating an incomplete sequence as complete.

## Captured execution evidence and interval determinacy

These rules define shared evidence meaning, not a production account type, verifier, interval-seal
operation or history service. They apply to claims based on captured supported state and changes;
they do not require every analytical definition to consume a full event history. A definition may
instead justify a sufficient observation or aggregate for its particular claim.

### Basis, coverage and frontier

A holder's coverage statement identifies:

- the run and its published `ModelFingerprint`, plus `ControlledRevisionId` only when
  authoritatively supplied;
- a **basis observation** at supported sequence/time `(S0,T0)`, the state from which the claim starts;
- the supported changes held after that basis;
- an independently known authoritative **frontier** `(Sf,Tf)`, from a supported observation or
  genuinely captured supported change;
- the claimed time/sequence range, its relevant proof boundary, and any known missing ranges.

A basis at sequence zero supports capture from execution establishment. A later basis supports
bounded reasoning from that state; it cannot silently supply pre-join history or an unknown
original start time. A fresh observation remains authoritative for current state even when earlier
history is missing. Folding retained changes produces a derived view, not mutable runtime truth,
model replay or an Engine ledger.

Supported-change completeness requires sequence continuity from the basis through the **relevant
proof sequence**. A locally contiguous list alone cannot rule out a lost tail: it needs a separately
known authoritative cursor at the claimed boundary. State missing middle and tail ranges against
that cursor; without an adequate basis/frontier, report completeness as unknown. Completeness is
per claim or prefix. A gap beyond an already established proof boundary does not revoke that
earlier interval, although it may prevent a broader claim.

Complete supported state-change coverage does not imply complete command/outcome history,
fault-free success, analytical sufficiency or deterministic re-execution. Rejected requests,
accepted no-ops and faults need controller-held provenance where the claim requires it; absence
from the change stream does not prove their absence. Reproduction requires the ordered command
script **including advancement interleavings and budgets**, the model and the actual producing
definition, not just supported changes. Run identity and model fingerprint do not identify that
definition; exact producing-definition provenance remains a separate custody/support obligation
when required by a concrete use.

### Finality of a half-open interval

For a claim about one run over `[a,b)`, require a valid state basis at or before `a`, gap-free held
changes through the exact proof sequence, and a truthful basis that no later supported change with
time `< b` can alter that history. An open interval is provisional; unknown or insufficient evidence
requires qualification or refusal. There are two classes of finality proof:

1. **Producer-supported temporal evidence.** The first genuinely captured supported change at
   time `>= b` witnesses finality by monotone supported time/order. The required history prefix ends
   immediately before that change: if its sequence is `K`, coverage is through `K-1`, with the
   witness itself retained. Alternatively, a supported observation at time `>= b` supplies a proof
   cursor through which the required prefix is captured without gaps. Changes exactly at `b` are
   excluded from `[a,b)`; other changes at `b` may remain pending, and sequence still orders state
   at `b`. Requested deadlines and unobserved internal scheduler time are not supported witnesses.
2. **Attributable controller closure.** An idle tail may leave supported time below `b`. The
   exclusive driver can then provide a scoped commitment against later effective input inside
   the interval, together with demonstrated successful exhaustion of relevant pending work,
   a post-advancement observation bound to `(RunId,S*,T*)`, and complete held changes through
   `S*`. The driver must know command and advancement outcomes since the basis; a successor's clean
   call does not erase an earlier fault. Confining the runtime to a closed script can support the
   commitment. The attestor, excluded input/effect scope, outcome qualifications and trust assumption
   must accompany the evidence and survive handover.

`advanceUntil(b,maxEvents)` limits which events may be processed; it does not move supported time
to the requested deadline or seal the interval. Consuming a count budget does not prove successful
exhaustion. Under exclusive control, repeated calls with a positive budget until a successful call
returns fewer events than that budget can demonstrate exhaustion through the inclusive target;
an exactly consumed budget requires another call, and a faulting call is not clean completion.
Only supported changes advance supported observation time. `QUIESCENT` alone does not exclude
future input, and an internal marker/no-op is not a closure witness. Command-time and stepping
rules remain owned by [Engine session semantics](engine-semantics.md#12-session-and-control-semantics).

A controller commitment is an attributable assertion, not a producer-verifiable fact. A frozen
claim relying on it remains final under recorded trust. Delivered contradictory changes can expose
a broken commitment. A violated no-further-requests promise can leave no supported evidence at all:
rejected requests and accepted no-ops are absent from that stream, and a frozen capture receives
no later effects. An inclusive promise through `b` is stronger than needed
for `[a,b)`; effective changes exactly at `b` need not invalidate that earlier interval.

Handover preserves run/model binding, the basis, required changes, proof frontier and any controller
attestation with its provenance and trust scope. A later observation alone cannot recover earlier
history; a raw event list stripped of its basis or closure cannot inherit the original guarantee.

For example, after a five-tick job completes, an honest driver may exhaust work through 100 and
stop issuing input while a holder still has only the time-zero dispatch and its stale cursor.
That holder cannot claim 100 active-job-ticks: it must capture the completion through the driver's
post-advancement cursor, yielding five ticks. Conversely, a proved `[0,5)` remains valid after a
later capture gap at time 10. Advancing an idle run toward 100 with its last supported change at
13 supplies no producer witness for `[0,100)` by itself.

### Responsibilities and limits

The runtime contract owns shared run, sequence, basis, coverage and interval-finality meaning.
The controller owns external input admission/issuance and advancement protocol under Engine command
semantics, plus its attributable promises and outcome provenance. The evidence holder owns capture,
retention and coverage qualification. An analytical-definition owner specifies method-specific
sufficiency and computation; definition ownership does not imply physical custody. These roles may
share a component and do not require one class or module each.

Supported waiting-state residence, processing residence and future transfer residence remain
distinct. Residence between waiting entry and dispatch is not automatically availability-conditioned
readiness, starvation or causal attribution. Utilization, occupancy populations/denominators and
other interpretations require an explicit named analytical method. Governance owns evidence use,
historical attribution and conformance, not scheduling or capture. [Operational
continuation](operational-continuity.md) has its own identity, loss and closure boundaries.

No new closure API is justified for present confined/static consumers. Reconsider stronger producer
evidence, command-time support, shared delivery or handover guarantees when concrete requirements
need them; the [execution-evidence research state](../research/investigations/simulation-execution-account.md#reopening-triggers)
records those triggers. This is a bounded present choice, not a universal exclusion of such APIs.

## Transport mechanisms are adapters, not the event contract

HTTP/SSE, WebSocket, Kafka, NATS, MQTT, an embedded Java API, or later operational adapters are projections of the same transport-neutral runtime contract. None is a dependency of the simulation core.

Transport event names must not be derived from internal scheduler event kinds. A future SSE-based adapter should use one stable transport event name such as `runtime-event`, use the supported `sequence` as the SSE message ID, and carry the semantic `eventType` inside the envelope.

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

Challenge/Game code may consume supported Engine observations/outcome facts but does not define runtime event semantics, reconstruct authoritative queues/dispatch from event replay, or make challenge scoring part of the runtime event contract. Every playable consumer depends on the complete supported runtime observation/event contract. Deterministic spatial/transfer runtime consequences are additionally required only when the promoted consumer requirements make spatial layout behaviorally consequential; a non-spatial playable slice does not acquire that dependency merely by being playable.

Operational Execution / Digital Twin remains a sibling track. These events are simulation-runtime events. They do not define production telemetry envelopes, source authenticity, operational actor/target identity, actuation acknowledgements, deployment provenance, external-observation ingestion, or modeled-versus-observed reconciliation.

An Operational adapter may later translate relevant Arcogine semantics, but production trust and consequence cannot be inferred from simulation event-stream maturity.
