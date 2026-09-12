# PLAN-ENG-4 Runtime Observation and Event Delivery

> **Status:** Active implementation companion; PLAN-ENG-4-A/B/C are complete, PLAN-ENG-4-D outward consumer convergence remains outstanding  
> **Owner:** Factory Simulation Engine Readiness / PLAN-ENG-4  
> **Architecture authority:** [ADR-0011](../architecture/decisions/0011-runtime-observation-and-event-contract.md)  
> **Parent plan:** [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md)

## 1. Purpose

Implement ADR-0011 through a consumer-neutral Engine contract, then converge outward consumers on that contract without turning internal scheduler events or transport DTOs into domain semantics.

The core rule remains:

> Internal events drive simulation. Supported observations expose current authoritative state. Supported runtime events expose ordered authoritative change. Transports only project those contracts.

## 2. Completed core

### PLAN-ENG-4-A — Runtime identity and supported observation — COMPLETE

`FactoryRuntime` exposes a consumer-neutral immutable observation containing:

- opaque `RunId` correlation;
- durable source `ModelFingerprint`;
- simulated time and advancement state;
- latest supported event sequence;
- resource state/queues/active work;
- aggregate order progress;
- child work-item state with `JobId` to `OrderId` correlation;
- cross-machine pending work; and
- supported performance facts.

A fresh observation is sufficient to reconstruct current supported consumer state without replaying internal scheduler history.

### PLAN-ENG-4-B — Supported RuntimeEvent contract — COMPLETE

Supported events are distinct from internal scheduler `Event`/`EventType`/`EventPayload` and carry:

```text
runId
sequence
simulationTime
eventType
modelFingerprint
controlledRevisionId [optional only when authoritatively bound]
affected entity references
semantic payload
```

Sequence is allocated only for supported post-authoritative change. Rejected/no-op transitions do not emit successful state-change events. Faults report only authoritative changes that actually occurred.

Work-item events preserve child `JobId` and parent `OrderId`; aggregate order completion preserves both aggregate and completing-child correlation where required by ADR-0010.

### PLAN-ENG-4-C — Headless acceptance closure — COMPLETE

Executable acceptance proves:

- run/sequence/time/model provenance;
- strict per-run sequence ordering, including same-timestamp events;
- observation/event cursor consistency;
- post-authoritative publication;
- correct order/work correlation;
- identical semantic streams for identical explicit inputs, excluding intentionally unique run identity;
- fresh-observation state reconstruction;
- reset creates a new run/sequence epoch without changing semantic outcomes; and
- transport/API DTOs do not re-enter domain decision paths.

The core supported event accessor is draining/non-retained. It is not a durable or cursor-replay journal.

## 3. Current implementation queue — PLAN-ENG-4-D

### PLAN-ENG-4-D1 — Legacy API/SSE migration

Migrate `interfaces/api` so supported observations/events, rather than internal scheduler events, define outward runtime semantics.

The implementation must update together where behavior changes:

- API runtime-event projection/DTO;
- `SseController`;
- frontend SSE client/store;
- SSE integration/contract tests; and
- `docs/reference/api.md`.

Preferred transport shape is conceptually:

```text
id: <supported sequence>
event: runtime-event
data: <supported runtime-event envelope DTO>
```

The exact semantic runtime event type stays inside the envelope. Internal `Event`, `EventPayload`, and `EventLog` remain implementation machinery.

If the legacy integrated API loop cannot consume `FactoryRuntime` directly because it legitimately orchestrates broader economy/finance/agent state, introduce the narrowest adapter/projection that preserves supported Engine semantics. Do not widen `FactoryRuntime` merely for concrete-type reuse.

#### KPI surfaces are ownership-sensitive

Outward convergence must not promote internal `EventLog` KPI computation into the supported contract. `com.arcogine.core.kpi` computes from internal `EventLog`/`SimTime`, and ADR-0011 §8 and ADR-0012 keep `EventLog` as implementation machinery rather than the supported history/analysis contract. Future supported analytical exports should derive from `RuntimeObservation`, `RuntimeEvent`, or another explicitly supported outward contract.

Record as architectural debt, to be resolved rather than extended:

> The existing generic KPI implementation is attached to the wrong observation substrate for future supported consumers and should be replaced/migrated rather than extended.

Consequently, migrating `/api/kpis` and the snapshot KPI list is **ownership-sensitive** and must consume the outcome of [Simulation analytics consumer boundary](../research/simulation-analytics-consumer-boundary.md) rather than becoming another formula set defined at the transport layer.

This does not block the rest of this work. Settled `RuntimeObservation`/`RuntimeEvent` transport migration may continue. Only pause where a concrete DTO or field would freeze disputed derived-performance semantics into the outward contract before that research resolves it.

### PLAN-ENG-4-D2 — CLI/reference consumer convergence

For each current headless/CLI path:

- use the supported runtime/session boundary directly where semantics match;
- retain broader scenario orchestration where it owns real extra concerns;
- make any adapter boundary explicit;
- provide a consumer-neutral reference path that can consume `RuntimeObservation` and ordered `RuntimeEvent` semantics without Spring/frontend/internal stores/raw `EventLog` replay; and
- remove duplicated outward observation/event semantics where doing so preserves ownership.

### PLAN-ENG-4-D acceptance

PLAN-ENG-4-D closes when:

1. current API/SSE projects supported runtime semantics, not internal scheduler taxonomy;
2. the frontend consumes the supported outward projection;
3. CLI/reference execution can consume or deliberately adapt the supported runtime contract;
4. transport/CLI DTOs remain one-way projections;
5. broader orchestration is not forced through `FactoryRuntime` solely for reuse;
6. behavior-changing slices update current-state docs in the same PR; and
7. no protocol/broker/event-bus/interchange framework is introduced to accomplish convergence.

PLAN-ENG-4-D1 and PLAN-ENG-4-D2 should normally be separate reviewable PRs when their code surfaces differ.

## 4. Provenance boundary

`ModelFingerprint` is required supported provenance and comes from the published model's durable fingerprint, never legacy content hash.

`ControlledRevisionId` is optional and appears only when the runtime has an authoritative upstream revision binding. Governance completion does not imply that every runtime has such a binding, and Engine must not generate/infer one.

The completed Governance identity/history substrate is summarized by [Governance Identity/History Downstream Compatibility Guard](governance-continuity.md).

ADR-0015/PLAN-ENG-5 adds mandatory `EngineSemanticsVersion` provenance. Outward convergence should consume the settled runtime provenance shape rather than publish an immediately obsolete envelope.

## 5. Recovery/resynchronization hardening

Recovery hardening is not a PLAN-ENG-4 core blocker and must not block PLAN-ENG-5. Admit it after outward contract requirements are concrete.

Any implementation must preserve:

```text
resume cursor retained
    -> return ordered retained deltas

cursor older than retained history
    -> explicit resync-required result
    -> fetch fresh observation
    -> continue after observation.latestEventSequence

cursor belongs to another run
    -> reject cursor for this run
    -> fetch fresh observation
```

Silent truncation is never successful recovery.

Retained supported-event history, durability across restart, exact checkpoint/restore, public contract versioning, and sidecar packaging are separate implementation responsibilities. Unselected policy/technology choices remain in [Engine Evolution Research](../research/engine-evolution.md) until promoted.

## 6. Cross-track ownership

- Factory Design owns source model semantics/publication.
- Engine owns run identity, supported observations/events, sequencing, and simulation recovery semantics.
- Governance owns controlled revision identity/history, semantic change/conformance/evidence/governed change.
- Challenge/Game owns challenge evaluation, attempts, rendering, and game state.
- Operational research/architecture owns future real-world trust, correspondence, external command/result, observation, and reconciliation semantics.

Runtime events are simulation facts. They do not become production telemetry merely because an adapter serializes them.

## 7. Non-goals

This plan does not require event sourcing, Kafka/NATS/MQTT, CloudEvents as an Engine-domain type, a generic event bus, revision persistence in Engine, production telemetry trust/reconciliation, game scoring/attempt history, or forcing broader orchestration through `FactoryRuntime`.

## 8. Landing policy

Land remaining work from fresh `main` in small dependency-aware PRs. Reconcile PLAN-ENG-4-D status here and in the parent Engine plan as each outward consumer converges. Do not combine outward transport migration with unrelated spatial/Operational/research changes.
