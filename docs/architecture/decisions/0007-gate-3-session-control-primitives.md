# ADR-0007: Gate 3 session-control primitives

Status: Accepted
Date: 2026-08-27

## Context

Gate 3 of Engine Readiness ([`../../planning/factory-simulation-engine-readiness.md`](../../planning/factory-simulation-engine-readiness.md), §7) asks `FactoryRuntime` to expose consumer-neutral session/control semantics -- source-model identification, bounded advancement, structured submit results, reset, and full state inspection -- before HTTP, SSE, CLI, or an embedded Java adapter can be treated as a stable external contract.

A repository-grounded audit (the session preceding this ADR's first revision) found `FactoryRuntime` already satisfied instantiate-from-published-model (`forModel`), single-event advancement (`advance()`), and state inspection (the existing read-only projections). Four criteria were open: retaining/identifying the source model version throughout the session (§7.4 criterion 7), a bounded-advancement primitive (criterion 4), a structured submit-workload result (criterion 2), and reset-and-reproduce (criterion 6). The first revision of this ADR closed `modelVersion()`, `advanceUntil`, and `reset()` additively, and argued criterion 2 was already satisfied by `submitWorkload`'s existing throw-on-reject contract together with the sealed `SimError` hierarchy, without a new result type.

Independent review of the resulting PR (`alaiba/arcogine#177`) identified two gaps this revision closes:

- **Criterion 2 is not actually satisfied by an exception-based contract.** §7.2 requires every externally initiated runtime change to *return* a definite result -- accepted/rejected status, a stable code, a diagnostic, affected-entity identifiers, session/model provenance, and events produced (or a cursor to them) -- not to throw on rejection while returning a bare value on acceptance. `submitWorkload`'s prior shape, and the already-public `setMachineAvailability` (`void` return, throws on rejection), did not meet that literal contract regardless of how well-structured `SimError` itself is.
- **Criterion 5 (inspect queue state) was claimed satisfied but is not**, for the current Gate 2 dispatch model. `FactoryHandler.pendingMultiEligible` (introduced by [ADR-0005](0005-gate-2-explicit-eligibility-dispatch-policy.md)) is a second authoritative waiting-work structure alongside each `Machine`'s own queue; a job waiting there is invisible to `MachineView.queueDepth()`, so a consumer could observe every machine's queue depth at zero while real work is still waiting.

A second review, of the resulting `CommandResult<T>`/`pendingWorkView()` head, identified two further correctness/resource issues this revision also closes:

- **A `Rejected` result could still follow partial mutation.** `FactoryHandler.submitOrder` created the `Order`/`Job` and started the selected `Machine`/`Job` *before* calling `Scheduler.schedule(...)` for the immediate-dispatch case; `Scheduler.schedule` can itself throw `SimError.EventOrderingViolation`, reachable with a validly published model because `FactoryModelValidator` only requires `step.duration() > 0` while `SimTime.plus(long)` uses unchecked `long` addition and can silently overflow for a large enough duration once simulated time itself is also large. `FactoryRuntime`'s broad `catch (SimError e)` then reported a clean `Rejected` even though the `Order`/`Job`/`Machine` had already mutated -- violating §7.2's "a rejected runtime command must not leave partial mutation" requirement. The same class of risk exists for `setMachineAvailability`, whose `handleMachineAvailability` mutates `Machine.setAvailability` before the dispatch cascade that can trigger the identical scheduling failure.
- **`RecordingScheduler` retained the entire session's event history, not just a command's.** The first version of `RecordingScheduler` appended every scheduled event to one permanent list for the scheduler's whole lifetime, including everything scheduled during ordinary `advance()`/`advanceUntil()` processing (dispatch, queue drains, order completion, ...), not only during a `submitWorkload`/`setMachineAvailability` call. That changes a `FactoryRuntime` session's space behavior from bounded pending-queue state to unbounded, monotonically growing history purely to serve the occasional command's `scheduledEvents()` field.

This ADR now records eight decisions, all still additive to `FactoryRuntime` (plus two small new package-private/public helper types in `com.arcogine.factory.process`).

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

`scheduledEvents()` is populated by `RecordingScheduler` (`com.arcogine.factory.process`, package-private), a `Scheduler` subclass. `FactoryRuntime` opens a capture window with `startCapturing(sink)` immediately before calling into `FactoryHandler`, and closes it with `stopCapturing()` in a `finally` block; only events scheduled while a window is open are appended anywhere. This is entirely local to the `factory` module -- the shared `com.arcogine.core.queue.Scheduler` type used by every other domain (economy, finance, agents, `SimThread`, `SimRunner`) is completely unchanged, so this instrumentation carries zero risk or behavior change for any other consumer of `Scheduler`. See "`RecordingScheduler` captures a command-scoped window, not the session's lifetime" below for why this replaced an earlier always-append design.

`Gate3SessionControlAcceptanceTest` proves both the accepted and rejected shapes directly (`acceptedSubmissionReturnsAStructuredResultWithProvenanceAndScheduledEvents`, `rejectedSubmissionReturnsAStructuredResultAndLeavesNoPartialMutation`, `machineAvailabilityCommandReturnsAStructuredResultForAcceptanceAndRejection`) -- asserting the actual `code()`/`diagnostic()`/`modelVersion()`/`scheduledEvents()`/wrapped-`error()` fields, not only that a rejection throws the right exception subtype and leaves no partial mutation.

### Cross-machine pending work gets its own read-only projection

`FactoryRuntime` gains `pendingWorkView(): List<PendingWorkView>`, delegating to a new `FactoryHandler.pendingWorkView()` that snapshots `pendingMultiEligible` into the new public record `PendingWorkView(JobId jobId, Set<MachineId> eligibleMachines)`. This is a second, necessary queue/pending-dispatch projection alongside `machinesView()`'s per-machine `queueDepth()` -- not a replacement for it, and not folded into `machinesView()` itself, because a `PendingWorkView` entry is by definition not queued on any single machine (that is exactly why `FactoryHandler` keeps it in a separate cross-machine backlog rather than one `Machine`'s own `ArrayDeque`). A consumer resolves `jobId` through the existing `FactoryRuntime.job(JobId)` for the waiting job's order/execution state, rather than `PendingWorkView` duplicating those fields.

`Gate3SessionControlAcceptanceTest.pendingWorkViewExposesAMultiEligibleJobWaitingWhileBothEligibleMachinesAreOccupied` proves the gap directly: with both eligible machines occupied and a third order waiting, every machine's `queueDepth()` reads zero while `pendingWorkView()` reports the waiting job and its eligible set -- and that freeing a machine dispatches the job and clears it from `pendingWorkView()`.

### `FactoryHandler.submitOrder` preflights immediate-dispatch scheduling before any mutation

`submitOrder`'s immediate-dispatch branch (step 0's eligible machine is free) now computes and validates the resulting `TaskEnd`'s end time -- the same `SimTime` ordering check `Scheduler.schedule` itself performs -- *before* `orders.createOrder`/`jobs.createJob`/`Machine.startJob`/`Job.start` run, not after. `routing.getStep(0)`, `selectMachine`, and `Machine.canAcceptJob()` are all pure reads, so hoisting them (and the new end-time check) ahead of any store mutation changes nothing about dispatch selection -- it only moves the one remaining throw-capable step earlier. If the check fails, `submitOrder` throws `SimError.EventOrderingViolation` having created nothing; `FactoryRuntime.submitWorkload`'s existing broad `catch (SimError e)` is therefore now provably safe to keep, because every throw path `submitOrder` has left is pre-mutation. The two pre-existing rejection checks (invalid quantity, unknown product/routing) were already positioned before any mutation and are unchanged.

`Gate3SessionControlAcceptanceTest.rejectedSubmissionFromAPostValidationSchedulingFailureStillLeavesNoPartialMutation` reproduces the reviewer's exact repro (a one-step model with `duration = Long.MAX_VALUE`, completed once so simulated time is itself `Long.MAX_VALUE`, then a second submission whose end-time computation overflows) and proves the rejected result leaves no order, no job, and the machine still idle.

### `setMachineAvailability`'s own rejections are verified before calling into `FactoryHandler`; deeper cascade faults are not caught

Unlike `submitOrder`, `FactoryHandler.handleMachineAvailability` cannot be made fully preflight-safe without a disproportionate rewrite: bringing a machine online can trigger `tryDispatchFromQueue` and a `tryDispatchPendingMultiEligible` loop that may dispatch several pending jobs in one call, each an independent scheduling call with the same overflow risk as `submitOrder`'s -- proving all of them safe before mutating any of them would mean simulating the entire cascade twice (a dry-run/commit split), which is out of proportion to a `Long.MAX_VALUE`-duration edge case.

Instead, `FactoryRuntime.setMachineAvailability` verifies, from its own read-only `machinesView()` (no `FactoryHandler` internals reached), exactly the two conditions that can make this command legitimately rejectable, and returns `CommandResult.Rejected` for either without calling `FactoryHandler` at all:

- the `machineId` is unknown (mirrors `MachineStore.get`'s `SimError.UnknownId`);
- the caller is taking a machine with active jobs offline (mirrors `Machine.setAvailability`'s own `SimError.InvalidStateTransition` guard, duplicated deliberately here as a pure pre-check rather than relying on catching that exception type after the fact).

Both are pure reads verified before any call into `FactoryHandler`, so a `Rejected` result from this method is always genuinely pre-mutation by construction, not by exception-type inference. Past that point, `setMachineAvailability` calls `handleMachineAvailability` with no `try`/`catch` around it: a fault surfacing from deep in the dispatch cascade (the same `SimError.EventOrderingViolation` risk `submitOrder` has) is a genuine engine fault, not a rejectable input, and propagates as an unchecked `SimError` exactly as `advance()` already does -- rather than being caught and misreported as "rejected, nothing changed" once the cascade may already have mutated state. `setMachineAvailability`'s signature now declares `throws SimError` to document this.

`Gate3SessionControlAcceptanceTest.takingABusyMachineOfflineIsRejectedBeforeAnyMutation` proves the new pre-check path: rejecting the command leaves the machine `Busy` with its active job still running, not silently transitioned.

### `RecordingScheduler` captures a command-scoped window, not the session's lifetime

`RecordingScheduler` no longer appends to one list for its entire lifetime. It instead exposes `startCapturing(List<Event> sink)`/`stopCapturing()`: while a window is open, every scheduled event is appended to the caller-supplied `sink`; once closed, nothing is retained by `RecordingScheduler` itself. `FactoryRuntime` opens a window immediately before calling into `FactoryHandler` and closes it in a `finally` block, so a window is always closed even when the command throws. Events scheduled by ordinary `advance()`/`advanceUntil()` processing -- outside any command's window -- are never captured or retained anywhere, so a long-lived `FactoryRuntime` session's space behavior returns to bounded pending-queue state, matching every other consumer of `Scheduler`, rather than growing with total lifetime event count.

`RecordingSchedulerTest` (new, package-local unit test) proves the capture window directly: events scheduled before, during, and after a window are exactly partitioned as expected, a long run of ordinary scheduling before a window opens does not leak into it, and successive command windows do not accumulate into each other.

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

### Fix atomicity by making `SimTime.plus`/arithmetic checked platform-wide

Rejected: `SimTime` is used by every domain (economy, finance, agents), not just factory; changing its arithmetic semantics is a materially bigger, cross-cutting decision than this Gate 3 slice, and nothing about Gate 3's own acceptance criteria requires it. `submitOrder`'s preflight fix reuses the exact comparison `Scheduler.schedule` already performs (`endTime.compareTo(currentTime) < 0`) to predict the identical failure before mutating, without needing `SimTime` itself to change.

### True two-phase (dry-run, then commit) dispatch for `setMachineAvailability`'s cascade

Rejected for this slice: simulating the whole `tryDispatchFromQueue`/`tryDispatchPendingMultiEligible` cascade once to validate it and again to apply it would fully close the remaining atomicity gap, but is a materially larger restructuring of dispatch internals than a `Long.MAX_VALUE`-duration edge case justifies. The chosen fix (pre-verify this command's own two rejectable conditions; let a genuine cascade fault propagate rather than be misreported as a clean rejection) closes the misreporting bug without that cost. If a concrete need for full atomicity here ever arises, it should be a deliberate follow-up decision, not folded into this fix.

### Catch `SimError.InvalidStateTransition` by type after calling `handleMachineAvailability`, instead of pre-checking `activeJobs()`

Rejected: this would rely on `InvalidStateTransition` being reachable, in this call path, only from `Machine.setAvailability`'s guard and never from anywhere later in the dispatch cascade -- true today by inspection, but a future change to `FactoryHandler` could silently break that assumption and cause a genuine post-mutation fault to be misreported as a clean rejection again. Verifying `activeJobs().isEmpty()` from `FactoryRuntime`'s own read-only view before calling `FactoryHandler` at all is structurally guaranteed pre-mutation regardless of what `FactoryHandler`'s internals do later, matching how `submitOrder`'s own fix works.

### Keep `RecordingScheduler` as an always-append history, exposed as "internal, not a public concern"

Rejected: retaining O(all events ever scheduled) for a session's lifetime is a real, unbounded resource cost regardless of whether the type is public -- a long headless run (Gate 1 explicitly allows quantity-proportional event volume) would grow memory monotonically for a capability (reporting one command's own scheduled events) that only ever needs a small, recent window. Scoping capture to the command boundary removes the cost at its source instead of documenting it as an accepted limitation.

## Consequences

- `FactoryRuntime` gains `modelVersion()`, `advanceUntil(SimTime, long)`, `reset()`, and `pendingWorkView()`, all additive; `advance()` and every existing read-only projection other than `pendingWorkView()`'s own addition are unchanged.
- `submitWorkload(...)`'s return type changes from `OrderId` to `CommandResult<OrderId>`, and `setMachineAvailability(...)`'s changes from `void` to `CommandResult<EventPayload.MachineAvailabilityChange>` (and now declares `throws SimError`, since a post-mutation cascade fault can still propagate). Both are **breaking signature changes** -- every prior call site (all in `product/domains/factory/src/test/java/com/arcogine/factory/process/`; no production `interfaces/api`/`interfaces/cli` code called either method) was updated, mechanically in the common case by appending `.orElseThrow()` to preserve prior throw-on-reject/return-value-on-accept test behavior. No production code outside this module referenced either method, so no other module was touched.
- `FactoryHandler.submitOrder`'s immediate-dispatch branch is reordered (preflight the end-time check, then mutate), a behavior-preserving change for every currently-passing scenario -- it changes nothing about which machine is selected or when dispatch happens, only the position of one already-existing validation relative to mutation. `handleMachineAvailability` itself is unchanged; `FactoryRuntime.setMachineAvailability` instead pre-verifies its own two rejectable conditions before calling it.
- Two new public types and one new package-private type in `com.arcogine.factory.process`: the sealed `CommandResult<T>`, the `PendingWorkView` record, and `RecordingScheduler` (a `Scheduler` subclass local to this package, now with a scoped `startCapturing`/`stopCapturing` window rather than an always-append history).
- A caller can now identify the published model a session came from for the session's full lifetime, bound simulated progress by both time and event count in one call, obtain a fresh session over the same model without hand-rebuilding the `FactoryModelVersion` plumbing itself, receive a definite accept/reject result (with provenance and scheduled-events) from both externally initiated commands instead of relying on an unchecked throw, and observe cross-machine pending work that no machine's own queue depth reflects.
- A `CommandResult.Rejected` from either command is now provably pre-mutation for every condition each command can itself determine is rejectable; a genuine engine fault surfacing after that point (still theoretically possible for `setMachineAvailability`'s dispatch cascade, given a pathological model) propagates as an unchecked `SimError` rather than being misreported as a clean rejection -- see "True two-phase... dispatch" above for why closing that residual gap is deliberately out of scope for this slice.
- `RecordingScheduler` no longer grows unboundedly over a session's lifetime; its capture window is scoped to exactly one command call.
- `SimThread`'s and `SimRunner`'s bespoke max-time loops remain unmigrated and now duplicate logic `FactoryRuntime.advanceUntil` also expresses; that duplication is accepted for this slice and tracked as later migration work, not silently left unrecorded. Neither currently calls `submitWorkload`/`setMachineAvailability`, so the signature change does not touch them.
- No new session-identity type, event envelope, or generic simulation-session framework was introduced; Gate 4 (event envelopes/cursors) remains open, separate decisions. `CommandResult<T>` is deliberately narrow (two current commands) rather than a claim that every future Arcogine command must use this exact shape.

## Charter alignment

This decision keeps determinism provable by construction: `advanceUntil` is implemented directly in terms of the unchanged `advance()`, and `reset()` reuses the unchanged `forModel` construction path, so neither primitive can diverge from the event-ordering and instantiation guarantees Gate 1/Gate 2 already established.
