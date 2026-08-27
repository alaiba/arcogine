# ADR-0007: Gate 3 session-control primitives

Status: Accepted
Date: 2026-08-27

## Context

Gate 3 of Engine Readiness ([`../../planning/factory-simulation-engine-readiness.md`](../../planning/factory-simulation-engine-readiness.md), §7) asks `FactoryRuntime` to expose consumer-neutral session/control semantics -- source-model identification, bounded advancement, structured submit results, reset, and full state inspection -- before HTTP, SSE, CLI, or an embedded Java adapter can be treated as a stable external contract.

A repository-grounded audit (the session preceding this ADR's first revision) found `FactoryRuntime` already satisfied instantiate-from-published-model (`forModel`), single-event advancement (`advance()`), and state inspection (the existing read-only projections). Four criteria were open: retaining/identifying the source model version throughout the session (§7.4 criterion 7), a bounded-advancement primitive (criterion 4), a structured submit-workload result (criterion 2), and reset-and-reproduce (criterion 6). The first revision of this ADR closed `modelVersion()`, `advanceUntil`, and `reset()` additively, and argued criterion 2 was already satisfied by `submitWorkload`'s existing throw-on-reject contract together with the sealed `SimError` hierarchy, without a new result type.

Independent review of the resulting PR (`alaiba/arcogine#177`) identified two gaps this revision closes:

- **Criterion 2 is not actually satisfied by an exception-based contract.** §7.2 requires every externally initiated runtime change to *return* a definite result -- accepted/rejected status, a stable code, a diagnostic, affected-entity identifiers, session/model provenance, and events produced (or a cursor to them) -- not to throw on rejection while returning a bare value on acceptance. `submitWorkload`'s prior shape, and the already-public `setMachineAvailability` (`void` return, throws on rejection), did not meet that literal contract regardless of how well-structured `SimError` itself is.
- **Criterion 5 (inspect queue state) was claimed satisfied but is not**, for the current Gate 2 dispatch model. `FactoryHandler.pendingMultiEligible` (introduced by [ADR-0005](0005-gate-2-explicit-eligibility-dispatch-policy.md)) is a second authoritative waiting-work structure alongside each `Machine`'s own queue; a job waiting there is invisible to `MachineView.queueDepth()`, so a consumer could observe every machine's queue depth at zero while real work is still waiting.

This ADR now records six decisions, all still additive to `FactoryRuntime` (plus two small new package-private/public helper types in `com.arcogine.factory.process`).

## Decision

### Classification: evolve `FactoryRuntime` additively, not a new session type or framework

As with [ADR-0005](0005-gate-2-explicit-eligibility-dispatch-policy.md)'s classification discipline, this slice adds fields/methods to the existing `FactoryRuntime` rather than introducing a new session-identity type or a generic simulation-session framework. `FactoryRuntime` already owns the exclusive `FactoryHandler`/`Scheduler` pair a session needs; nothing in the four open criteria requires a new abstraction.

### Source model version is a retained field, not re-derived

`FactoryRuntime` now stores the exact `FactoryModelVersion` passed to `forModel` and exposes it via `modelVersion()`. This is a plain retained reference (compared and returned by identity), not a new fingerprint or provenance type -- `FactoryModelVersion.contentHash()` (or, once implemented, the durable `factory-model:v1` fingerprint from [ADR-0006](0006-durable-semantic-fingerprint-contract.md)) remains the identity a caller derives from it if needed. `reset()` (below) reuses this same retained reference rather than requiring a caller to track it separately.

### Bounded advancement: `advanceUntil(SimTime targetTime, long maxEvents)` alongside the unchanged `advance()`

`FactoryRuntime` gains `advanceUntil(SimTime targetTime, long maxEvents)`, processing pending events one at a time in the same order `advance()` would, stopping as soon as either bound is reached: the next pending event's time would exceed `targetTime`, or `maxEvents` events have already been processed by this call. It returns every event actually processed, in order. `advance()` is unchanged and remains the one-event primitive; `advanceUntil` is implemented in terms of it (a loop calling `advance()` under the two stopping conditions), so the two can never diverge in event ordering or dispatch behavior by construction, not merely by test coverage.

This directly generalizes the two bespoke max-time loops already duplicated in `SimThread.Run`/`SimThread.Step` (`product/interfaces/api/src/main/java/com/arcogine/api/state/SimThread.java`) and `SimRunner.runScenario` (`product/simulation/src/main/java/com/arcogine/core/runner/SimRunner.java`), both of which stop at `event.time() > maxTime` without ever comparing to a caller-supplied max event count. Migrating those call sites onto `FactoryRuntime`/`advanceUntil` is explicitly out of scope for this slice (see Non-goals) -- this ADR only establishes the primitive's shape.

`Gate3SessionControlAcceptanceTest` proves `advanceUntil` converges with looping `advance()` for identical workloads, both bounded to one event per call and unbounded in a single call, and proves the time bound is respected independently of the event-count bound.

### Reset is fresh construction over the retained model version, not in-place mutation

`FactoryRuntime.reset()` returns `FactoryRuntime.forModel(this.modelVersion)` -- a brand-new instance with none of the original session's submitted workload or dispatch state, leaving the original session itself untouched. `FactoryRuntime` only owns a `FactoryHandler`+`Scheduler` pair with no partial/selective-reset subsystem; a general in-place reset would require adding mutation paths to types (`OrderStore`, `JobStore`, `MachineStore`, `RoutingStore`) that are otherwise never mutated outside event handling, purely to serve a reset case. Fresh construction reuses the same construction path every other session already goes through, so reset-session behavior is provably identical to any other fresh session over the same model version -- it is not a second code path that could drift from `forModel`'s guarantees.

`Gate3SessionControlAcceptanceTest.resetSessionReproducesIdenticalResultToTheOriginalSessionWithoutMutatingIt` proves the invariant: a reset session replaying the identical workload/command sequence reproduces an identical ordered event stream and identical terminal state, mirroring `Gate1EngineReadinessAcceptanceTest`'s two-fresh-runtimes determinism pattern.

### Externally initiated commands return a `CommandResult<T>` instead of throwing or returning `void`

`submitWorkload` and `setMachineAvailability` -- the two externally initiated runtime changes `FactoryRuntime` exposes -- now return `CommandResult<T>` (`com.arcogine.factory.process.CommandResult`), a sealed interface with two records:

```text
CommandResult.Accepted<T>(T value, FactoryModelVersion modelVersion, List<Event> scheduledEvents)
CommandResult.Rejected<T>(SimError error, FactoryModelVersion modelVersion)
```

This satisfies §7.2's field list directly rather than by assertion: `code()`/`diagnostic()` are `"ACCEPTED"`/`"accepted"` on `Accepted`, or the rejecting `SimError` subtype's simple name and `getMessage()` on `Rejected`; `modelVersion()` is session/model provenance on both; `scheduledEvents()` is every `Event` scheduled as a direct, synchronous effect of the command (always empty on `Rejected`, since a rejected command must never leave partial mutation and therefore never schedules anything); `T` (`OrderId` for `submitWorkload`, the existing `EventPayload.MachineAvailabilityChange` record reused for `setMachineAvailability`) is the accepted value/affected entity. `orElseThrow()` returns the accepted value or rethrows the original `SimError`, so every existing call site that only cared about the happy-path value (all of Gate 1/Gate 2's acceptance tests) needed only `.orElseThrow()` appended, not a rewrite.

Rejection still carries the original, already-structured, sealed `SimError` (`Rejected.error()`) rather than re-deriving a parallel code/diagnostic/entity-id shape: the `SimError` subtype (e.g. `SimError.OutOfRange`, `SimError.UnknownId`) and its own typed accessors (`field()`, `id()`) remain the actual rejection detail: `CommandResult` wraps that existing structure into a returned value instead of an unchecked throw, it does not duplicate it.

`scheduledEvents()` is populated by `RecordingScheduler` (`com.arcogine.factory.process`, package-private), a `Scheduler` subclass that remembers every event it has ever scheduled, in order. `FactoryRuntime` records the count before a command and takes everything scheduled since that mark after it returns. This is entirely local to the `factory` module -- the shared `com.arcogine.core.queue.Scheduler` type used by every other domain (economy, finance, agents, `SimThread`, `SimRunner`) is completely unchanged, so this instrumentation carries zero risk or behavior change for any other consumer of `Scheduler`.

`Gate3SessionControlAcceptanceTest` proves both the accepted and rejected shapes directly (`acceptedSubmissionReturnsAStructuredResultWithProvenanceAndScheduledEvents`, `rejectedSubmissionReturnsAStructuredResultAndLeavesNoPartialMutation`, `machineAvailabilityCommandReturnsAStructuredResultForAcceptanceAndRejection`) -- asserting the actual `code()`/`diagnostic()`/`modelVersion()`/`scheduledEvents()`/wrapped-`error()` fields, not only that a rejection throws the right exception subtype and leaves no partial mutation.

### Cross-machine pending work gets its own read-only projection

`FactoryRuntime` gains `pendingWorkView(): List<PendingWorkView>`, delegating to a new `FactoryHandler.pendingWorkView()` that snapshots `pendingMultiEligible` into the new public record `PendingWorkView(JobId jobId, Set<MachineId> eligibleMachines)`. This is a second, necessary queue/pending-dispatch projection alongside `machinesView()`'s per-machine `queueDepth()` -- not a replacement for it, and not folded into `machinesView()` itself, because a `PendingWorkView` entry is by definition not queued on any single machine (that is exactly why `FactoryHandler` keeps it in a separate cross-machine backlog rather than one `Machine`'s own `ArrayDeque`). A consumer resolves `jobId` through the existing `FactoryRuntime.job(JobId)` for the waiting job's order/execution state, rather than `PendingWorkView` duplicating those fields.

`Gate3SessionControlAcceptanceTest.pendingWorkViewExposesAMultiEligibleJobWaitingWhileBothEligibleMachinesAreOccupied` proves the gap directly: with both eligible machines occupied and a third order waiting, every machine's `queueDepth()` reads zero while `pendingWorkView()` reports the waiting job and its eligible set -- and that freeing a machine dispatches the job and clears it from `pendingWorkView()`.

## Alternatives considered

### Keep the exception-based contract and rely on `SimError`'s own structure (this ADR's original decision)

This was the first revision's decision: rely on `SimError`'s existing sealed-hierarchy structure, plus `modelVersion()`, as an already-adequate answer to §7.2. Superseded by this revision: §7.2 asks for a *returned, definite result*, and an unchecked exception on rejection while returning a bare value on acceptance is not that shape no matter how well-typed the exception is -- review of PR #177 correctly identified this as over-claiming completion rather than satisfying the criterion. `SimError` remains the actual rejection detail (see above); what changed is that it is now wrapped into a returned `CommandResult.Rejected` instead of thrown past the caller.

### A per-command bespoke result type (e.g. separate `WorkloadSubmissionResult` and `MachineAvailabilityResult`)

Rejected in favor of one generic `CommandResult<T>`: `FactoryRuntime` has exactly two externally initiated commands today, both needing the identical envelope (status/code/diagnostic/provenance/scheduled-events) around a different accepted-value type. A shared generic shape avoids duplicating that envelope twice for no difference in behavior, matches how `T` already varies command-to-command (`OrderId` vs `EventPayload.MachineAvailabilityChange`), and keeps exactly one place to extend if a third command is ever added.

### Re-deriving rejection code/diagnostic/entity-ids as new `CommandResult` fields instead of wrapping `SimError`

Rejected: it would duplicate detail `SimError`'s own sealed subtypes and accessors (`field()`, `id()`, etc.) already carry, creating two representations of the same rejection that could drift. Wrapping the original `SimError` in `Rejected.error()` keeps exactly one rejection-detail representation; `code()`/`diagnostic()` are thin derived views (`getClass().getSimpleName()` / `getMessage()`) over it, not a second parallel model.

### Add a global scheduling-observer hook to the shared `com.arcogine.core.queue.Scheduler`

Rejected: `Scheduler` is used by every domain (`economy`, `finance`, `agents`) and by `interfaces/api`'s `SimThread` and `SimRunner` directly; adding an observer hook there, even opt-in and no-op by default, widens a cross-cutting shared type's contract for a need specific to `FactoryRuntime`. Subclassing it as a package-private `RecordingScheduler` confined to `com.arcogine.factory.process` gets the same capability with zero blast radius outside the factory domain, using ordinary virtual dispatch (`FactoryHandler` still receives a plain `Scheduler`-typed parameter and calls `schedule(...)` on it as before; `RecordingScheduler.schedule` overrides transparently).

### Fold `pendingWorkView()` into `machinesView()`/`MachineView`

Rejected: a `pendingMultiEligible` entry is specifically *not* associated with one machine -- that is the reason `FactoryHandler` keeps it in a separate cross-machine backlog rather than any single `Machine`'s queue. Attaching it to `MachineView` would either duplicate one waiting job across every eligible machine's view (double-counting) or arbitrarily pick one machine to attribute it to (misleading). A separate `pendingWorkView()` keeps the two "waiting work" shapes -- per-machine queue and cross-machine backlog -- distinct and both real, matching Gate 2 acceptance criterion 7's requirement that capability, operational status, and queue state remain distinct concepts.

### `advanceUntil` returning only the final `SimTime`/event count, not the processed events

Rejected: a bounded-advancement caller (e.g. an interactive consumer building an event log or UI update from what just happened) needs the events themselves, and `advance()` already returns the one event it processed -- returning nothing from the bounded primitive would be a strictly weaker contract than looping `advance()` for no benefit.

### In-place `reset(...)` mutating the existing `FactoryHandler`/`Scheduler` in a `FactoryRuntime`

Rejected: `FactoryHandler`'s stores have no reset method today, and adding one would mean maintaining a second "clear everything" code path alongside normal construction -- one more place determinism guarantees could silently drift from `forModel`. Returning a fresh instance instead reuses the already-proven construction path.

### Migrating `SimThread`/`SimRunner`/CLI `HeadlessHandler` onto `advanceUntil` in this same slice

Rejected for this slice: the planning document and prior audit explicitly flag that migration as separate, larger follow-up work (it touches `interfaces/api` and `interfaces/cli` production wiring, not just `FactoryRuntime`). This ADR only establishes the primitive; adopting it elsewhere is a deliberate later decision.

## Consequences

- `FactoryRuntime` gains `modelVersion()`, `advanceUntil(SimTime, long)`, `reset()`, and `pendingWorkView()`, all additive; `advance()` and every existing read-only projection other than `pendingWorkView()`'s own addition are unchanged.
- `submitWorkload(...)`'s return type changes from `OrderId` to `CommandResult<OrderId>`, and `setMachineAvailability(...)`'s changes from `void` to `CommandResult<EventPayload.MachineAvailabilityChange>`. Both are **breaking signature changes** -- every prior call site (all in `product/domains/factory/src/test/java/com/arcogine/factory/process/`; no production `interfaces/api`/`interfaces/cli` code called either method) was updated, mechanically in the common case by appending `.orElseThrow()` to preserve prior throw-on-reject/return-value-on-accept test behavior. No production code outside this module referenced either method, so no other module was touched.
- Two new types in `com.arcogine.factory.process`: the public sealed `CommandResult<T>` and public `PendingWorkView` record, plus one new package-private `RecordingScheduler` (a `Scheduler` subclass local to this package).
- A caller can now identify the published model a session came from for the session's full lifetime, bound simulated progress by both time and event count in one call, obtain a fresh session over the same model without hand-rebuilding the `FactoryModelVersion` plumbing itself, receive a definite accept/reject result (with provenance and scheduled-events) from both externally initiated commands instead of relying on an unchecked throw, and observe cross-machine pending work that no machine's own queue depth reflects.
- `SimThread`'s and `SimRunner`'s bespoke max-time loops remain unmigrated and now duplicate logic `FactoryRuntime.advanceUntil` also expresses; that duplication is accepted for this slice and tracked as later migration work, not silently left unrecorded. Neither currently calls `submitWorkload`/`setMachineAvailability`, so the signature change does not touch them.
- No new session-identity type, event envelope, or generic simulation-session framework was introduced; Gate 4 (event envelopes/cursors) remains open, separate decisions. `CommandResult<T>` is deliberately narrow (two current commands) rather than a claim that every future Arcogine command must use this exact shape.

## Charter alignment

This decision keeps determinism provable by construction: `advanceUntil` is implemented directly in terms of the unchanged `advance()`, and `reset()` reuses the unchanged `forModel` construction path, so neither primitive can diverge from the event-ordering and instantiation guarantees Gate 1/Gate 2 already established.
