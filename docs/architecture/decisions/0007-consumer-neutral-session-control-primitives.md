# ADR-0007: Consumer-neutral session-control primitives

Status: Accepted
Date: 2026-08-27
Amendment: 2026-09-03 — replaced transient Engine Readiness coordinates with semantic terminology; no semantic change

## Context

The Engine Readiness plan's consumer-neutral session-control capability ([`../../planning/factory-simulation-engine-readiness.md`](../../planning/factory-simulation-engine-readiness.md), §7) asks `FactoryRuntime` to expose source-model identification, bounded advancement, structured command results, reset, and full state inspection before HTTP, SSE, CLI, or an embedded Java adapter can be treated as a stable external contract.

A repository-grounded audit preceding this ADR found `FactoryRuntime` already satisfied instantiate-from-published-model (`forModel`), single-event advancement (`advance()`), and state inspection (the existing read-only projections). Four criteria were open: retaining/identifying the source model version throughout the session, a bounded-advancement primitive, a structured submit-workload result, and reset-and-reproduce. The first revision of this ADR closed `modelVersion()`, `advanceUntil`, and `reset()` additively, and argued the structured-result criterion was already satisfied by `submitWorkload`'s existing throw-on-reject contract together with the sealed `SimError` hierarchy, without a new result type.

Independent review of the resulting PR (`alaiba/arcogine#177`) identified two gaps this revision closes:

- **The structured-result criterion is not actually satisfied by an exception-based contract.** Every externally initiated runtime change must *return* a definite result -- accepted/rejected status, a stable code, a diagnostic, affected-entity identifiers, session/model provenance, and events produced (or a cursor to them) -- not throw on rejection while returning a bare value on acceptance. `submitWorkload`'s prior shape, and the already-public `setMachineAvailability` (`void` return, throws on rejection), did not meet that literal contract regardless of how well-structured `SimError` itself is.
- **Queue-state inspection was claimed satisfied but was not**, for the deterministic multi-resource dispatch model. `FactoryHandler.pendingMultiEligible` (introduced by [ADR-0005](0005-explicit-eligibility-deterministic-dispatch-policy.md)) is a second authoritative waiting-work structure alongside each `Machine`'s own queue; a job waiting there is invisible to `MachineView.queueDepth()`, so a consumer could observe every machine's queue depth at zero while real work is still waiting.

A second review, of the resulting `CommandResult<T>`/`pendingWorkView()` head, identified two further correctness/resource issues this revision also closes:

- **A `Rejected` result could still follow partial mutation.** `FactoryHandler.submitOrder` created the `Order`/`Job` and started the selected `Machine`/`Job` *before* calling `Scheduler.schedule(...)` for the immediate-dispatch case; `Scheduler.schedule` can itself throw `SimError.EventOrderingViolation`, reachable with a validly published model because `FactoryModelValidator` only requires `step.duration() > 0` while `SimTime.plus(long)` uses unchecked `long` addition and can silently overflow for a large enough duration once simulated time itself is also large. `FactoryRuntime`'s broad `catch (SimError e)` then reported a clean `Rejected` even though the `Order`/`Job`/`Machine` had already mutated -- violating the command contract's "a rejected runtime command must not leave partial mutation" requirement. The same class of risk exists for `setMachineAvailability`, whose `handleMachineAvailability` mutates `Machine.setAvailability` before the dispatch cascade that can trigger the identical scheduling failure.
- **`RecordingScheduler` retained the entire session's event history, not just a command's.** The first version of `RecordingScheduler` appended every scheduled event to one permanent list for the scheduler's whole lifetime, including everything scheduled during ordinary `advance()`/`advanceUntil()` processing (dispatch, queue drains, order completion, ...), not only during a `submitWorkload`/`setMachineAvailability` call. That changes a `FactoryRuntime` session's space behavior from bounded pending-queue state to unbounded, monotonically growing history purely to serve the occasional command's `scheduledEvents()` field.

A third review, of the resulting atomicity fixes, identified one further gap this revision closes:

- **`setMachineAvailability` could still throw past its own boundary, which is not a "definite result" either.** The second revision's fix pre-verified its two rejectable conditions and then let any fault surfacing later, from deep in the online-machine dispatch cascade, propagate as an unchecked `SimError` -- correctly no longer *misreporting* mutation as "rejected, nothing changed," but still not satisfying the command contract: an escaped exception is not a returned result at all, and by the time it escapes, machine/job state may already have mutated, so the caller also loses the command-local event/result evidence `CommandResult` exists to provide. This is reachable through the public `FactoryRuntime` boundary: two independent single-machine routes, one machine held offline with a job waiting in its own queue, simulated time driven to `Long.MAX_VALUE` by an unrelated job completing on the other machine, then bringing the offline machine back online -- the resulting dispatch dequeues and starts the waiting job before its own scheduling call discovers the same `SimTime` overflow `submitOrder` had.

A fourth review, of the resulting `CommandResult.Faulted`, identified one further gap this revision closes:

- **`Faulted` dropped the accepted value, collapsing "accepted" and "still failed" onto one axis.** The third revision's `Faulted<T>` carried only `error`/`modelVersion`/`scheduledEvents` -- no `T value`. In the exact repro `Faulted` exists for, the availability change's own preconditions passed and `Machine.setAvailability` genuinely ran before the *subsequent* dispatch cascade faulted. Reporting that as a type with no accepted value meant a caller could not recover which `MachineId`/`online` request had actually been applied -- `SimError.EventOrderingViolation` itself carries only `SimTime` values, not the affected entity -- even though the request demonstrably was accepted and acted on. `Faulted` was accidentally modeling "accepted-ness" and "later failure" as mutually exclusive outcomes, when they are independent facts: a command can be accepted and still fail while being carried out.

This ADR records nine decisions, all additive to `FactoryRuntime` (plus two small package-private/public helper types in `com.arcogine.factory.process`).

## Decision

### Classification: evolve `FactoryRuntime` additively, not a new session type or framework

As with [ADR-0005](0005-explicit-eligibility-deterministic-dispatch-policy.md)'s classification discipline, this change adds fields/methods to the existing `FactoryRuntime` rather than introducing a new session-identity type or a generic simulation-session framework. `FactoryRuntime` already owns the exclusive `FactoryHandler`/`Scheduler` pair a session needs; nothing in the open criteria requires a new abstraction.

### Source model version is a retained field, not re-derived

`FactoryRuntime` stores the exact `FactoryModelVersion` passed to `forModel` and exposes it via `modelVersion()`. This is a plain retained reference (compared and returned by identity), not a new fingerprint or provenance type -- `FactoryModelVersion.contentHash()` (or, once implemented, the durable `factory-model:v1` fingerprint from [ADR-0006](0006-durable-semantic-fingerprint-contract.md)) remains the identity a caller derives from it if needed. `reset()` reuses this same retained reference rather than requiring a caller to track it separately.

### Bounded advancement: `advanceUntil(SimTime targetTime, long maxEvents)` alongside the unchanged `advance()`

`FactoryRuntime` gains `advanceUntil(SimTime targetTime, long maxEvents)`, processing pending events one at a time in the same order `advance()` would, stopping as soon as either bound is reached: the next pending event's time would exceed `targetTime`, or `maxEvents` events have already been processed by this call. It returns every event actually processed, in order. `advance()` is unchanged and remains the one-event primitive; `advanceUntil` is implemented in terms of it, so the two can never diverge in event ordering or dispatch behavior by construction, not merely by test coverage.

This directly generalizes the two bespoke max-time loops already duplicated in `SimThread.Run`/`SimThread.Step` (`product/interfaces/api/src/main/java/com/arcogine/api/state/SimThread.java`) and `SimRunner.runScenario` (`product/simulation/src/main/java/com/arcogine/core/runner/SimRunner.java`), both of which stop at `event.time() > maxTime` without ever comparing to a caller-supplied max event count. Migrating those call sites onto `FactoryRuntime`/`advanceUntil` is explicitly out of scope for this decision; this ADR only establishes the primitive's shape.

`Gate3SessionControlAcceptanceTest` proves `advanceUntil` converges with looping `advance()` for identical workloads, both bounded to one event per call and unbounded in a single call, and proves the time bound is respected independently of the event-count bound.

### Reset is fresh construction over the retained model version, not in-place mutation

`FactoryRuntime.reset()` returns `FactoryRuntime.forModel(this.modelVersion)` -- a brand-new instance with none of the original session's submitted workload or dispatch state, leaving the original session itself untouched. `FactoryRuntime` only owns a `FactoryHandler`+`Scheduler` pair with no partial/selective-reset subsystem; a general in-place reset would require adding mutation paths to types (`OrderStore`, `JobStore`, `MachineStore`, `RoutingStore`) that are otherwise never mutated outside event handling, purely to serve a reset case. Fresh construction reuses the same construction path every other session already goes through, so reset-session behavior is provably identical to any other fresh session over the same model version.

`Gate3SessionControlAcceptanceTest.resetSessionReproducesIdenticalResultToTheOriginalSessionWithoutMutatingIt` proves the invariant: a reset session replaying the identical workload/command sequence reproduces an identical ordered event stream and identical terminal state, mirroring the established two-fresh-runtimes determinism evidence.

### Externally initiated commands return a `CommandResult<T>` instead of throwing or returning `void`

`submitWorkload` and `setMachineAvailability` -- the two externally initiated runtime changes `FactoryRuntime` exposes -- return `CommandResult<T>` (`com.arcogine.factory.process.CommandResult`), a sealed interface with three records:

```text
CommandResult.Accepted<T>(T value, FactoryModelVersion modelVersion, List<Event> scheduledEvents)
CommandResult.Rejected<T>(SimError error, FactoryModelVersion modelVersion)
CommandResult.Faulted<T>(T value, SimError error, FactoryModelVersion modelVersion, List<Event> scheduledEvents)
```

This satisfies the command-result contract directly: `code()`/`diagnostic()` are `"ACCEPTED"`/`"accepted"` on `Accepted`, or the `SimError` subtype's simple name and `getMessage()` on `Rejected`/`Faulted`; `modelVersion()` is session/model provenance on all three; `scheduledEvents()` is every `Event` scheduled as a direct, synchronous effect of the command -- structurally empty on `Rejected`, possibly non-empty on `Faulted`; `T` (`OrderId` for `submitWorkload`, the existing `EventPayload.MachineAvailabilityChange` record reused for `setMachineAvailability`) is the accepted value/affected entity, present on both `Accepted` and `Faulted`. `orElseThrow()` returns the accepted value or rethrows the original `SimError`, so existing happy-path call sites needed only `.orElseThrow()` appended; `Faulted.value()` remains reachable separately for a caller that specifically needs to know what was applied despite the fault.

`Faulted` exists as a third outcome, distinct from `Rejected`, precisely because it does not carry `Rejected`'s zero-mutation guarantee: it means the command's own preconditions passed and its requested change was genuinely applied, but the runtime then failed while carrying out the resulting work, after some further mutation and/or event scheduling may already have happened. Acceptance and execution outcome are independent facts, not one axis, so `Faulted` carries the same accepted `value` `Accepted` would have carried for the same outcome, not just the fault.

Rejection carries the original, already-structured, sealed `SimError` (`Rejected.error()`) rather than re-deriving a parallel code/diagnostic/entity-id shape. The `SimError` subtype and typed accessors remain the actual rejection detail; `CommandResult` wraps that existing structure into a returned value instead of an unchecked throw.

`scheduledEvents()` is populated by `RecordingScheduler` (`com.arcogine.factory.process`, package-private), a `Scheduler` subclass. `FactoryRuntime` opens a capture window with `startCapturing(sink)` immediately before calling into `FactoryHandler`, and closes it with `stopCapturing()` in a `finally` block; only events scheduled while a window is open are appended anywhere. This is entirely local to the `factory` module -- the shared `com.arcogine.core.queue.Scheduler` type used by every other domain is unchanged.

`Gate3SessionControlAcceptanceTest` proves all three shapes directly (`acceptedSubmissionReturnsAStructuredResultWithProvenanceAndScheduledEvents`, `rejectedSubmissionReturnsAStructuredResultAndLeavesNoPartialMutation`, `machineAvailabilityCommandReturnsAStructuredResultForAcceptanceAndRejection`, `setMachineAvailabilityReportsFaultedRatherThanThrowingWhenTheDispatchCascadeFailsAfterMutation`).

### Cross-machine pending work gets its own read-only projection

`FactoryRuntime` gains `pendingWorkView(): List<PendingWorkView>`, delegating to a new `FactoryHandler.pendingWorkView()` that snapshots `pendingMultiEligible` into the new public record `PendingWorkView(JobId jobId, Set<MachineId> eligibleMachines)`. This is a second, necessary queue/pending-dispatch projection alongside `machinesView()`'s per-machine `queueDepth()` -- not a replacement for it, and not folded into `machinesView()` itself, because a `PendingWorkView` entry is by definition not queued on any single machine. A consumer resolves `jobId` through the existing `FactoryRuntime.job(JobId)` for the waiting job's order/execution state, rather than `PendingWorkView` duplicating those fields.

`Gate3SessionControlAcceptanceTest.pendingWorkViewExposesAMultiEligibleJobWaitingWhileBothEligibleMachinesAreOccupied` proves the gap directly: with both eligible machines occupied and a third order waiting, every machine's `queueDepth()` reads zero while `pendingWorkView()` reports the waiting job and its eligible set -- and that freeing a machine dispatches the job and clears it from `pendingWorkView()`.

### `FactoryHandler.submitOrder` preflights immediate-dispatch scheduling before any mutation

`submitOrder`'s immediate-dispatch branch now computes and validates the resulting `TaskEnd`'s end time -- the same `SimTime` ordering check `Scheduler.schedule` itself performs -- *before* `orders.createOrder`/`jobs.createJob`/`Machine.startJob`/`Job.start` run. `routing.getStep(0)`, `selectMachine`, and `Machine.canAcceptJob()` are all pure reads, so hoisting them and the end-time check ahead of mutation changes nothing about dispatch selection; it only moves the one remaining throw-capable step earlier. If the check fails, `submitOrder` throws `SimError.EventOrderingViolation` having created nothing; `FactoryRuntime.submitWorkload`'s broad `catch (SimError e)` is therefore safe to keep because every remaining throw path is pre-mutation.

`Gate3SessionControlAcceptanceTest.rejectedSubmissionFromAPostValidationSchedulingFailureStillLeavesNoPartialMutation` proves the rejected result leaves no order, no job, and the machine still idle for the overflow repro.

### `setMachineAvailability` rejections are verified before calling into `FactoryHandler`; deeper cascade faults are reported as `Faulted`, never thrown

Unlike `submitOrder`, `FactoryHandler.handleMachineAvailability` cannot be made fully preflight-safe without a disproportionate rewrite: bringing a machine online can trigger queue and multi-eligible dispatch loops that may dispatch several pending jobs, each with its own scheduling call.

`FactoryRuntime.setMachineAvailability` verifies, from its read-only `machinesView()`, exactly the two conditions that can make this command legitimately rejectable, and returns `CommandResult.Rejected` for either without calling `FactoryHandler` at all:

- the `machineId` is unknown;
- the caller is taking a machine with active jobs offline.

Both are pure reads verified before any call into `FactoryHandler`, so a `Rejected` result from this method is pre-mutation by construction. Past that point, `setMachineAvailability` calls `handleMachineAvailability` inside a `try`/`catch (SimError e)`: a fault surfacing from deep in the dispatch cascade is a genuine engine fault, not a rejectable input this method could have pre-verified, and is returned as `CommandResult.Faulted<>(requested, e, modelVersion, scheduled)` -- carrying the same `EventPayload.MachineAvailabilityChange requested` value `Accepted` would have carried, plus whatever events the capture window saw before the failure. Because the pre-checks guarantee `Machine.setAvailability` itself cannot fail, by the time any `SimError` reaches this catch the availability change was genuinely applied.

`Gate3SessionControlAcceptanceTest.takingABusyMachineOfflineIsRejectedBeforeAnyMutation` proves the pre-check path. `Gate3SessionControlAcceptanceTest.setMachineAvailabilityReportsFaultedRatherThanThrowingWhenTheDispatchCascadeFailsAfterMutation` proves a post-mutation cascade fault returns `Faulted` rather than escaping or being misreported as rejection.

### `RecordingScheduler` captures a command-scoped window, not the session's lifetime

`RecordingScheduler` exposes `startCapturing(List<Event> sink)`/`stopCapturing()`: while a window is open, every scheduled event is appended to the caller-supplied `sink`; once closed, nothing is retained by `RecordingScheduler` itself. `FactoryRuntime` opens a window immediately before calling into `FactoryHandler` and closes it in a `finally` block, so a window is always closed even when the command throws. Events scheduled by ordinary `advance()`/`advanceUntil()` processing -- outside any command's window -- are never captured or retained anywhere, so a long-lived `FactoryRuntime` session's space behavior remains bounded by pending queue state rather than total lifetime event count.

`RecordingSchedulerTest` proves events scheduled before, during, and after a window are exactly partitioned, a long run of ordinary scheduling before a window opens does not leak into it, and successive command windows do not accumulate into each other.

## Alternatives considered

### Keep the exception-based contract and rely on `SimError`'s own structure

This was the first revision's decision: rely on `SimError`'s existing sealed-hierarchy structure, plus `modelVersion()`, as already adequate. Superseded by this revision: the contract asks for a *returned, definite result*, and an unchecked exception on rejection while returning a bare value on acceptance is not that shape no matter how well-typed the exception is. `SimError` remains the actual rejection detail; it is now wrapped into a returned `CommandResult.Rejected` instead of thrown past the caller.

### A per-command bespoke result type

Rejected in favor of one generic `CommandResult<T>`: `FactoryRuntime` has exactly two externally initiated commands today, both needing the identical envelope around a different accepted-value type. A shared generic shape avoids duplicating that envelope twice and keeps one place to extend if a third command is added.

### Re-derive rejection code/diagnostic/entity IDs as new `CommandResult` fields

Rejected: it would duplicate detail `SimError` already carries and create two representations that could drift. Wrapping the original `SimError` keeps exactly one rejection-detail representation; `code()`/`diagnostic()` are thin derived views over it.

### Add a global scheduling-observer hook to the shared `Scheduler`

Rejected: `Scheduler` is used by every domain and interface path. Adding an observer hook there would widen a cross-cutting shared type for a need specific to `FactoryRuntime`. A package-private `RecordingScheduler` subclass gets the same capability with zero blast radius outside the factory domain.

### Fold `pendingWorkView()` into `machinesView()`/`MachineView`

Rejected: a `pendingMultiEligible` entry is specifically *not* associated with one machine. Attaching it to `MachineView` would either duplicate one waiting job across every eligible machine's view or arbitrarily attribute it to one machine. A separate `pendingWorkView()` keeps per-machine queue and cross-machine backlog distinct, matching the deterministic dispatch contract's distinction among capability, operational status, and queue state.

### `advanceUntil` returning only the final `SimTime`/event count

Rejected: a bounded-advancement caller may need the events themselves, and `advance()` already returns the one event it processed. Returning nothing would be a strictly weaker contract than looping `advance()`.

### In-place `reset(...)` mutating the existing runtime

Rejected: the stores have no reset method, and adding one would create a second "clear everything" path that could drift from normal construction. Returning a fresh instance reuses the already-proven `forModel` path.

### Migrate `SimThread`/`SimRunner`/CLI `HeadlessHandler` onto `advanceUntil` in the same change

Rejected: the planning document and audit identify that as separate, larger follow-up wiring across interfaces. This ADR establishes the primitive; adopting it elsewhere is a later migration.

### Fix atomicity by making `SimTime.plus` arithmetic checked platform-wide

Rejected: `SimTime` is used by every domain. Changing its arithmetic semantics is a materially bigger cross-cutting decision than session control requires. `submitOrder`'s preflight fix reuses the same comparison `Scheduler.schedule` already performs to predict the failure before mutating.

### True two-phase dispatch for `setMachineAvailability`'s cascade

Rejected: simulating the whole dispatch cascade once to validate it and again to apply it would make every cascade failure representable as a clean `Rejected`, but is a materially larger restructuring than the pathological overflow case justifies. `Faulted` closes the definite-result requirement without claiming zero mutation for this residual case.

### Let a cascade fault propagate as an unchecked `SimError`

Rejected after review: it stopped a fault from being disguised as rejection but still violated the "definite result" contract. `CommandResult.Faulted` closes this without reopening the misreporting problem.

### Catch `SimError.InvalidStateTransition` by type after calling `handleMachineAvailability`

Rejected: that would rely on the exception subtype being reachable only from the pre-mutation guard. Pre-checking from `FactoryRuntime`'s read-only view is structurally guaranteed pre-mutation regardless of future internal changes.

### Keep `RecordingScheduler` as an always-append history

Rejected: retaining O(all events ever scheduled) for a session lifetime is an unbounded resource cost for a capability that only needs one command's immediate window. Scoping capture to the command boundary removes that cost.

### `Faulted<T>` without a `value`

Rejected after review: that shape collapsed "accepted" and "failed during execution" and discarded the affected entity. Adding `T value` to `Faulted` closes this without requiring a fourth outcome type.

### Represent "accepted then faulted" as a flag/optional field inside `Accepted<T>`

Rejected: it would make `Accepted`'s semantics conditional and blur its existing meaning. Keeping `Faulted` a distinct sealed variant that also carries `value` preserves exhaustive pattern matching and the distinction between full success and execution fault.

## Consequences

- `FactoryRuntime` gains `modelVersion()`, `advanceUntil(SimTime, long)`, `reset()`, and `pendingWorkView()`, all additive; `advance()` and existing read-only projections remain unchanged apart from the new pending-work projection.
- `submitWorkload(...)` changes from `OrderId` to `CommandResult<OrderId>`, and `setMachineAvailability(...)` changes from `void` to `CommandResult<EventPayload.MachineAvailabilityChange>` and no longer declares `throws SimError`. Both are breaking signature changes. Existing test call sites were updated, mechanically in the common case by appending `.orElseThrow()`.
- `FactoryHandler.submitOrder`'s immediate-dispatch branch is reordered to preflight end-time validation before mutation. `handleMachineAvailability` itself is unchanged; `FactoryRuntime.setMachineAvailability` pre-verifies rejectable conditions and returns `Faulted` for deeper cascade errors.
- `CommandResult<T>` is a three-way sealed type (`Accepted`/`Rejected`/`Faulted`), with `Faulted` carrying both an accepted `value` and the fault `error`. `PendingWorkView` is public and `RecordingScheduler` is package-private with scoped capture windows.
- A caller can identify the source model for a session's full lifetime, bound progress by time and event count, reset through fresh construction, receive a definite result from both externally initiated commands under every outcome, and observe cross-machine pending work invisible to per-machine queue depth.
- A `CommandResult.Rejected` is provably pre-mutation for every condition each command can determine as rejectable. A genuine post-mutation engine fault is returned as `CommandResult.Faulted`, explicitly without a zero-mutation guarantee.
- `RecordingScheduler` no longer grows unboundedly over a session lifetime.
- `SimThread`'s and `SimRunner`'s bespoke max-time loops remain unmigrated and duplicate logic `FactoryRuntime.advanceUntil` also expresses; that duplication is accepted as later migration work.
- No new session-identity type, supported event envelope, or generic simulation-session framework is introduced. The supported runtime observation/event contract remains a separate decision.

## Charter alignment

This decision keeps determinism provable by construction: `advanceUntil` is implemented directly in terms of the unchanged `advance()`, and `reset()` reuses the unchanged `forModel` construction path, so neither primitive can diverge from the event-ordering and instantiation guarantees already established by explicit workload and deterministic dispatch semantics.
