# ADR-0007: Gate 3 session-control primitives

Status: Accepted
Date: 2026-08-27

## Context

Gate 3 of Engine Readiness ([`../../planning/factory-simulation-engine-readiness.md`](../../planning/factory-simulation-engine-readiness.md), §7) asks `FactoryRuntime` to expose consumer-neutral session/control semantics -- source-model identification, bounded advancement, structured submit results, and reset -- before HTTP, SSE, CLI, or an embedded Java adapter can be treated as a stable external contract.

A repository-grounded audit (the session preceding this ADR) found `FactoryRuntime` already satisfied instantiate-from-published-model (`forModel`), single-event advancement (`advance()`), and state inspection (the existing read-only projections). Four criteria were open: retaining/identifying the source model version throughout the session (§7.4 criterion 7), a bounded-advancement primitive (criterion 4), a structured submit-workload result (criterion 2), and reset-and-reproduce (criterion 6). This ADR records the four decisions closing them, all additive to `FactoryRuntime` alone.

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

### Submit-workload rejection stays exception-based; no new result type

A repository-grounded check confirmed `FactoryHandler.submitOrder` can concretely reject today -- `SimError.OutOfRange` for an invalid quantity, `SimError.UnknownId` for a product with no published routing -- both checked before any `Order`/`Job` mutation. No other command result in this codebase is represented as an explicit accepted/rejected value type (sealed interface, status+code+record); every other command (`submitOrder`, `advance()`, `handleMachineAvailability`, and every caller across `interfaces/api`/`interfaces/cli`) signals rejection by throwing the sealed, typed `SimError` hierarchy and lets the caller catch it at whatever boundary needs to translate it (HTTP response, CLI exit code, etc.).

`submitWorkload` therefore keeps its existing signature (`OrderId`, or an unchecked `SimError`) rather than gaining a new wrapper type. This already satisfies §7.2's structured-result shape without inventing one: a normal return is acceptance; the thrown `SimError` subtype is itself the stable rejection code; its typed accessors (`field()`, `id()`, etc.) are the affected-entity/diagnostic detail; `getMessage()` is the human-readable diagnostic; `modelVersion()` (above) is the session/model provenance available for either outcome. `Gate3SessionControlAcceptanceTest.rejectedSubmissionThrowsAStableStructuredErrorAndLeavesNoPartialMutation` proves both existing rejection paths throw the expected typed `SimError` and leave no partial `Order`/`Job` behind.

## Alternatives considered

### A new sealed `WorkloadSubmissionResult` (or similar) wrapping accept/reject

Rejected for this slice: no comparable structured-result type exists anywhere else in `product/domains` or `product/simulation` to extend consistently, and `SimError` already is a structured, sealed hierarchy rather than a bare string -- introducing a second, competing way to express "this command was rejected" would fragment error handling rather than complete it, and the Gate 3 audit explicitly cautions against inventing a rejection path that doesn't already exist. If a future consumer genuinely needs a uniform result envelope across multiple command types, that is a bigger, deliberate decision warranting its own ADR, not a Gate 3 side effect.

### `advanceUntil` returning only the final `SimTime`/event count, not the processed events

Rejected: a bounded-advancement caller (e.g. an interactive consumer building an event log or UI update from what just happened) needs the events themselves, and `advance()` already returns the one event it processed -- returning nothing from the bounded primitive would be a strictly weaker contract than looping `advance()` for no benefit.

### In-place `reset(...)` mutating the existing `FactoryHandler`/`Scheduler` in a `FactoryRuntime`

Rejected: `FactoryHandler`'s stores have no reset method today, and adding one would mean maintaining a second "clear everything" code path alongside normal construction -- one more place determinism guarantees could silently drift from `forModel`. Returning a fresh instance instead reuses the already-proven construction path.

### Migrating `SimThread`/`SimRunner`/CLI `HeadlessHandler` onto `advanceUntil` in this same slice

Rejected for this slice: the planning document and prior audit explicitly flag that migration as separate, larger follow-up work (it touches `interfaces/api` and `interfaces/cli` production wiring, not just `FactoryRuntime`). This ADR only establishes the primitive; adopting it elsewhere is a deliberate later decision.

## Consequences

- `FactoryRuntime` gains `modelVersion()`, `advanceUntil(SimTime, long)`, and `reset()`, all additive; `advance()`, `submitWorkload(...)`, and every existing read-only projection are unchanged.
- A caller can now identify the published model a session came from for the session's full lifetime, bound simulated progress by both time and event count in one call, and obtain a fresh session over the same model without hand-rebuilding the `FactoryModelVersion` plumbing itself.
- `SimThread`'s and `SimRunner`'s bespoke max-time loops remain unmigrated and now duplicate logic `FactoryRuntime.advanceUntil` also expresses; that duplication is accepted for this slice and tracked as later migration work, not silently left unrecorded.
- No new session-identity type, event envelope, or generic simulation-session framework was introduced; Gate 4 (event envelopes/cursors) and any future need for a uniform command-result type remain open, separate decisions.

## Charter alignment

This decision keeps determinism provable by construction: `advanceUntil` is implemented directly in terms of the unchanged `advance()`, and `reset()` reuses the unchanged `forModel` construction path, so neither primitive can diverge from the event-ordering and instantiation guarantees Gate 1/Gate 2 already established.
