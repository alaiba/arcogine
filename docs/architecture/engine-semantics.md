# Engine semantics

Status: Normative current development interpretation. No stability/support promotion has been declared.
Rationale: [Determinism Contract](overview.md#determinism-contract)
Evolution rule: [Semantic evolution and support](overview.md#semantic-evolution-and-support)
Model-side counterpart: [Factory model](factory-model.md)

## 1. Purpose

This document defines the complete result-affecting Engine interpretation a simulation run uses. It
records semantic rules, not Java class shape, DTO serialization, build identity, or replaceable
implementation algorithms.

The current implementation has no dedicated identifier for this interpretation. That absence is
deliberate: a human development label would not identify the exact definition, while a build identity
would distinguish many semantics-preserving changes. `ModelFingerprint` identifies the authored
Factory content and `RunId` identifies one runtime epoch for correlation; neither substitutes for
the Engine definition. If a concrete consumer later requires an exact resolvable Engine-definition
reference, that reference is introduced for that boundary rather than synthesized from development
status.

A fact describing the production system the designer authored belongs to the canonical model and its
fingerprint; a rule describing how Arcogine interprets any such design belongs here when changing it
can change semantic outcome for identical explicit inputs; a replaceable algorithm that preserves
observable semantics is an implementation detail.

The result inputs are:

```text
current Engine definition
+ ModelFingerprint
+ explicit workload
+ seed/random inputs
+ ordered external commands
+ any other explicitly identified result-affecting input
```

A run fixes the current interpretation at establishment; it never changes mid-run, and a new runtime
always gets a fresh `RunId`. One interpretation covers dispatch, decomposition, scheduling and
derived-result rules together, because those rules interact to produce one outcome;
independently identified sub-definitions would need a concrete independent-evolution requirement.

The interpretation is under active development. That status is human-facing repository state and is
not emitted through runtime APIs or provenance:

- for the same model, explicit inputs and current definition, supported outcomes are deterministic;
- between development revisions the result-affecting rules may be corrected or extended. An
  intentional change that can alter outcome for identical explicit inputs — a bug fix that observably
  changes interpretation included — is a definition change: it updates this specification and the
  conformance fixtures of §6 in the same change. Repairing an implementation to conform to this
  specification is not a definition change, and performance work, dependency upgrades, refactors,
  logging and projection-only changes must preserve the normative behavior;
- a result produced under an earlier development revision is not attributable to today's definition
  merely from its model fingerprint, run ID, or a human status label; comparing results across
  revisions is a claim its consumer must establish;
- implementation, fixtures and this normative description do not establish a stability/support
  commitment. If an owner later declares one for a concrete need, the declaration records an
  unambiguous stable reference to the exact definition plus the applicable non-rebinding and support
  obligations of the [semantic evolution rules](overview.md#semantic-evolution-and-support). Engine
  and Factory commitments need not be made together.

Complete conformance requires the result-affecting rules and the behavioral fixtures, not a reported
interpretation name.

### 1.1 Completeness rule

This specification claims to identify the **complete** result-affecting Engine interpretation. That
claim is operational, not aspirational. The membership test is:

> A rule belongs to the Engine interpretation if changing it can change the outcome — acceptance or
> rejection, assignment, ordering, timing, or derived result — for an identical `ModelFingerprint`,
> explicit workload, seed/random inputs, and ordered external commands.

These consequences follow.

1. **No result-affecting limit may remain ambient implementation policy.** A hard-coded threshold,
   envelope, ceiling, or bound that deterministically decides acceptance, rejection, assignment,
   ordering, or timing is a semantic rule regardless of where it currently sits in the
   implementation. Every such limit is either
   - part of the interpretation and recorded in this specification, or
   - an explicitly identified reproducibility input recorded alongside `ModelFingerprint`, explicit
     workload, seed, and ordered commands.

   There is no third category. "It is only a guard", "it is an implementation detail", and "it is
   only reachable at extreme inputs" do not exempt a limit that two conforming implementations could
   choose differently and thereby disagree on a run's outcome. This covers derived-result arithmetic
   as much as acceptance limits: a saturation, clamp, or zero-denominator rule decides a supported
   observation's value and is therefore in scope, and so does the **accumulation** feeding that
   value, not only its edge cases. The currently known rules of this kind are recorded in the section
   that owns the behavior they bound — the child materialization envelope in section 3, the exact
   ranking arithmetic in section 2 rule 3, the derived-result edge cases in section 5.1, and the
   accumulator register in section 5.2.
2. **A rule is in scope even when the capability that motivated it did not introduce it.**
   Behavior that satisfies the membership test is captured here even when it predates the change
   that wrote it down, rather than left implicit. Sections 1.2, 2, 3 and 4 exist for exactly that
   reason.
3. **Recording a previously unwritten rule is a correction, not a behavior change**, provided the
   rule's behavior is unchanged. Changing the behavior is a definition change (§1).
4. **A rule that satisfies the membership test but is absent here is a defect in this document**,
   not a licence to treat the behavior as unspecified. The correct response is to record it, or to
   make it an explicitly identified reproducibility input — not to leave it ambient.
5. **Incidental implementation ordering is never a semantic tie-breaker.** Hash iteration order,
   set or map traversal order, thread scheduling, and comparable artefacts of the runtime are not
   rules two conforming implementations could agree on, so they may not decide a result. Where
   ordering can change an outcome, an explicit rule recorded in this specification decides it —
   sections 2, 3 and 4 own the current ones.
6. **Which internal events a session schedules is in scope.** Bounded advancement counts processed
   scheduled events, and a command applies at the time of the last one processed (section 1.2), so
   an internal event can change timing and derived results for identical explicit inputs even when
   processing it publishes nothing. An event is not exempt from the membership test because it is
   internal; section 4 rule 3 records the current scheduling rule.

### 1.2 Session and control semantics

The consumer-neutral session/control behavior satisfies the section 1.1 membership test: it decides
how far a session advances and whether an externally initiated change is applied, for an identical
ordered command sequence. It is therefore part of the interpretation, and two implementations may not
claim the same interpretation while differing on it.

This section owns those rules; `SessionControlAcceptanceTest`, `BoundedAdvancementConformanceTest`
and `RecordingSchedulerTest` prove them. In scope:

- `advance()` as the unchanged one-event primitive, and `advanceUntil(targetTime, maxEvents)`
  defined in terms of it — processing events one at a time in `advance()` order and stopping as soon
  as either bound is reached (the next event's time exceeds `targetTime`, or the call has processed
  `maxEvents` events), returning every event actually processed, in order. Because `advanceUntil` is
  defined as a loop over `advance()`, the two cannot diverge in event ordering or dispatch behavior.
  The unit both count is one processed scheduled event. Under section 4 rule 3 every such event is
  an authoritative step completion published at its own time, so a budget is never spent on an
  event that changes nothing.
- A command applies at the session's current simulated time: the time of the most recently
  processed scheduled event, or zero before any. A time guard or an event budget does not move it,
  so a call that stops before a later event leaves commands applying at the last processed event's
  time, never at the requested target. Because every processed event publishes at its own time,
  after every session call — including one that faults — this is also the supported observation's
  current time: advancement budget alone cannot make the next command apply later than the state a
  caller observed. That equality follows from the current scheduling rule of section 4 rule 3; it is
  not a separate guarantee for event kinds a session does not schedule.
- `reset()` as a fresh session over the same retained model version: replaying an identical command
  sequence reproduces an identical ordered event stream and identical terminal state, and the
  original session is left untouched.
- The `CommandResult` outcome contract for the two externally initiated commands, `submitWorkload`
  and `setMachineAvailability` — `Accepted`; `Rejected` with its zero-mutation guarantee and
  structurally empty scheduled-event list; and `Faulted` for a command whose requested change was
  genuinely applied before the runtime failed while carrying out the resulting work. Acceptance and
  execution outcome are independent facts, so `Faulted` is not a variant of `Rejected`.
- Sequence exhaustion. Supported-event sequences and the scheduler's insertion ordinal are checked
  and fail closed at `Long.MAX_VALUE` with the structured `SimError.SequenceExhausted`; sequence
  `Long.MAX_VALUE` itself is a legal, publishable event. Once `latestEventSequence` is
  `Long.MAX_VALUE` the session is exhausted: `submitWorkload` and a transitioning
  `setMachineAvailability` return `Rejected` before any mutation, `advance` and `advanceUntil` throw
  the error without processing an event, a redundant availability request stays an `Accepted` no-op
  because it emits nothing, and `observe`, `drainSupportedEvents` and `reset` keep working (`reset`
  starts a fresh epoch at 0). A command that starts below the limit but needs a later sequence
  partway through reports a fault, never a rejection: `submitWorkload` and `setMachineAvailability`
  return `Faulted` carrying the applied request, and `advance` and `advanceUntil` throw the error
  after processing. Events through `Long.MAX_VALUE` are published, and the transition whose event
  could not be numbered may be visible in `observe()` without one. An exhausted scheduler ordinal
  raises the same error and takes the same command-level fault path.

Further session rules in scope:

- `FactoryRuntime` retains the exact `FactoryModelVersion` it was instantiated from for the
  session lifetime and exposes it through `modelVersion()`; `reset()` reuses that retained value.
- Every externally initiated command returns a definite result rather than throwing: `Accepted`
  and `Faulted` both carry the accepted value/affected entity, the model provenance and the events
  scheduled as a direct effect of the command; `Rejected` carries the structured `SimError` and
  model provenance with no scheduled events. Rejection is verified before any mutation; a fault
  that surfaces after an applied change must be reported as `Faulted`, never disguised as
  rejection and never allowed to escape as an exception. Full two-phase preflight of the
  machine-availability dispatch cascade is deliberately not required; `Faulted` exists for
  that residual post-mutation case.
- Command-effect capture is command-scoped: events scheduled by ordinary advancement are never
  retained for a session's lifetime merely to serve a later command result.
- Waiting work that is not queued on any single machine (`pendingWorkView()`) is observable
  separately from per-machine queue depth, so a consumer never sees every queue empty while
  real work is still waiting.

A change to any rule in this section is a change to the interpretation (§1).

## 2. Resource-selection and dispatch semantics

The following rules are part of the interpretation:

1. Start from the routing step's authored eligible-machine set. Prefer online machines by filtering
   out `Offline` resources when at least one eligible machine is online. If every eligible machine
   is offline, retain the full eligible set so selection remains deterministic and the job enters
   the existing waiting/recovery path rather than becoming unselectable. An eligible-resource set
   is unordered; every engine-owned eligible set iterates in ascending resource ID, so no consumer
   observes an order that depends on authoring or the JVM.
2. Within the resulting candidate set, `canAcceptJob()` is the primary **ranking key**, not an
   eligibility filter: a machine that can accept immediately ranks ahead of one that cannot.
3. Among candidates tied on immediate acceptance, rank by `combinedQueueDepth` as currently defined
   by the Factory Engine, including pending multi-eligible work that could land on that resource.
   `combinedQueueDepth` is an **exact** sum of that machine's own queue depth and the count of
   compatible multi-eligible entries. Unlike the derived-result accumulators of section 5.2 it may
   not saturate, narrow, or approximate: it is a ranking key, so a wrapped or truncated value would
   change which machine is selected and therefore change assignment, not merely a reported number.

   Exactness here is **total, and requires no session-wide envelope**. Each term is structurally
   bounded by the size of the collection holding it, so their true sum cannot exceed twice that
   bound. The obligation is therefore only that the sum be accumulated and compared at a width that
   holds it — never that a run be restricted to keep it representable. The concrete failures this
   rule forbids are a narrowing conversion of either term and a same-width addition of two
   independently bounded terms. Section 3's per-submission materialization envelope is unrelated: it
   bounds one submission's children, not a session's cumulative waiting set, and must not be cited
   as the reason this sum is representable.
4. Break a remaining tie deterministically by `MachineId`.
5. Selection runs at the established semantic points even when the selected machine cannot yet
   accept the job. The post-selection branch determines immediate dispatch versus
   `pendingMultiEligible` versus the selected single-machine queue.
6. Recovery after a machine/step completion preserves the current cascade order: attempt that
   machine's own queue first, then reconsider pending multi-eligible work. The per-machine attempt
   starts at most one job (see section 4); it does not drain the queue to capacity. Within the
   multi-eligible backlog, the earliest queued entry that can actually be placed is dispatched
   first, with repeated re-selection after each placement.
7. **Which waiting path a job enters is determined by its routing step's authored eligible-machine
   set.** When the selected machine cannot currently accept the job, a step with more than one
   eligible machine enters the shared multi-eligible backlog, and a step with exactly one eligible
   machine enters that single machine's own queue. A multi-eligible step therefore never occupies a
   per-machine queue, and a single-eligible step never occupies the shared backlog.
8. **Each machine's own queue is strict FIFO in arrival order.** Entries are appended at the tail on
   enqueue and taken from the head on dispatch. Queue position is not re-derived from job identity,
   ordinal, order identity, step duration, remaining steps, waiting time, or priority; there is no
   priority, aging, or reordering rule for per-machine queues.
9. **The shared multi-eligible backlog retains arrival order.** An entry's eligible set is the one
   captured when it was enqueued. Reconsideration re-runs selection over that captured set against
   current machine state, so an entry may be placed on a machine other than the one selected when it
   first waited; see section 4 for the scan order.

Changing any of those result-affecting rules is a change to the interpretation (§1).

## 3. Unit-work decomposition semantics

Work decomposition (which independently dispatchable execution units exist) is a different
question from resource dispatch (which eligible resource executes one such unit). One accepted
`Order` is immutable production intent identified by `OrderId`; the same `OrderId` identifies the
order-level execution aggregate (requested, released and completed quantity, completion time).
`JobId` identifies one independently dispatchable work item within an order and is the identity
machine queues, active-machine state, pending multi-eligible work and step completions (`TaskEnd`)
use; no separate `ExecutionUnitId`, `LotId` or `BatchId` exists. A consumer must never have to infer
aggregate completion by counting child states itself, and the game/challenge layer never splits
one production requirement into several orders merely to obtain parallelism — Arcogine owns that
decomposition.

The unit-work decomposition rules are part of the interpretation:

1. Workload quantity `N` decomposes into `N` independently dispatchable `JobId` children.
2. Child creation/release and initial dispatch use deterministic ordinal ordering.
3. Aggregate completion continues to correlate explicit `OrderId` with the completing child
   `JobId`.
4. Each child has execution quantity one and traverses its routing exactly once; its immutable
   zero-based `ordinalWithinOrder` orders creation, `JobId` allocation and initial dispatch
   attempts and is ordering metadata, not identity. A job cannot start step `k + 1` before its own
   step `k` completes; sibling jobs have no additional precedence. All children are released
   atomically with order acceptance, so `releasedQuantity == requestedQuantity` immediately after
   acceptance. `completedQuantity` increments exactly once per child final completion, and only
   the transition to `requestedQuantity` completes the order. That transition is part of the
   completing child's final step completion; it is published once, as the supported
   `ORDER_COMPLETED` event carrying the `OrderId` and the completing child `JobId`, and schedules no
   further event (section 4 rule 3). Backlog, completed sales and value, lead time
   and order-throughput remain order-level facts; child count never multiplies a sale. Material
   lots, arbitrary batch sizes and split/merge semantics are separate future contracts.
5. **The supported child-materialization envelope is part of the interpretation.** Workload
   submission accepts `1 <= N <= 100000` and rejects anything outside that closed interval as an
   out-of-range explicit input. The bound is a flat count of children; it does not vary with routing
   step count, resource count, or any other model content.
6. Rejection under rule 5 is total and occurs **before any runtime mutation**: no `Order`, no child
   `Job`, no queue entry, and no scheduled event exists after a rejected submission. A rejected
   submission is therefore not a partial run with a smaller workload.

Rule 5 is recorded here rather than left as an implementation guard because it satisfies the section
1.1 membership test directly: for an identical model, seed, and command sequence, a workload of
`100001` is deterministically rejected and a workload of `100000` is deterministically accepted and
executed. Two implementations choosing different envelopes would disagree about whether a run
happens at all, which is the strongest possible outcome difference. The envelope is consequently
neither an ambient policy nor a free implementation choice; changing the accepted interval is a
change to the interpretation, and a deployment that needs a different interval must expose it as an
explicitly identified reproducibility input rather than silently widening or narrowing it.

A change that can alter assignment, ordering, or completion outcome for identical explicit inputs is a
change to the interpretation (§1).

## 4. Scheduler and dispatch-cascade ordering

1. Authoritative scheduled work is ordered first by `SimTime`.
2. Equal-time scheduled work uses the existing deterministic insertion-order tie-break.
3. **A Factory session schedules only authoritative work.** Every event it schedules is a step
   completion: processing it authoritatively completes that step and publishes `JOB_STEP_COMPLETED`
   at the event's own time, together with the order completion and placement changes it causes.
   The session schedules no marker that processing would ignore — no start marker beside a
   dispatched step completion, and no order-completion event behind a final child completion. An
   order's completion is part of the step-completion transition that causes it (section 3 rule 4)
   and is published from that transition. Because section 1.2 bounded advancement counts processed
   scheduled events and commands apply at the time of the last one processed, a queued marker would
   be result-affecting: it would consume advancement budget and move the time at which the next
   command applies without any supported change. Scheduling a new event kind is therefore a
   definition change that must state the authoritative transition the event performs and the
   supported events it publishes at its own time.

   This corrects an earlier development interpretation that scheduled both markers. A script that
   stopped a bounded call on a positive event budget can now reach authoritative work sooner, so a
   later command can apply at a different time and derived results can differ. Runs whose inputs are
   all given before advancement, and calls bounded only by time, keep the same supported events and
   outcomes; the internal `Event` lists that `advance()`, `advanceUntil` and
   `CommandResult.scheduledEvents()` return no longer contain markers.
4. Supported runtime-event ordering at the same `SimTime` is represented only by the monotonic
   supported-event sequence established by the [runtime contract](runtime-contract.md).
5. The intra-handler recovery cascade is also semantic ordering where it changes assignment:
   after a completed step releases a machine, the Engine first attempts to dispatch that machine's
   own queued work and only then reconsiders `pendingMultiEligible` work. A change to that ordering
   can change assignments and is therefore a change to the interpretation.
6. The cascade runs at the same two trigger points and in the same shape for both of them: a step
   completion that releases a machine, and a machine coming back online. Taking a machine offline
   runs no cascade.
7. **The per-machine stage of the cascade dispatches at most one queued job per trigger.** If the
   released machine can accept work, exactly one head-of-queue entry is started; the machine is not
   drained to capacity in one pass. Remaining queued work waits for the next trigger.
8. **The multi-eligible stage runs to fixpoint, scanning from the head each pass.** One pass walks
   the backlog in arrival order, re-running selection over each entry's captured eligible set, and
   dispatches the first entry whose selected machine can currently accept it. After a placement the
   scan restarts from the head, because that placement changes queue and active state and can change
   subsequent selections. The stage ends when a complete pass places nothing.
9. **An unplaceable entry does not head-of-line block the backlog.** A pass skips entries whose
   selected machine cannot accept and continues to later entries, so an entry waiting on a disjoint
   eligible set is still reachable. Arrival order is therefore decisive only among entries that are
   simultaneously placeable, which is exactly the case where it can change assignment.

Rules 7, 8 and 9 satisfy the section 1.1 membership test — each of them can change which job lands on
which machine at which time for identical explicit inputs — and are recorded here for that reason.

## 5. KPI and resource-observation interpretation

The Engine produces no resource utilization, cumulative busy-time, occupancy or bottleneck measure.
Resource observation reports current operational facts — status, concurrency, active jobs and queue
depth — and the supported dispatch and step-completion events record when processing began and
ended. A consumer may derive processing occupancy for a completely recorded period from those events
only under an explicitly named analytical method that owns its own population, denominator and
evidence-completeness rules ([runtime contract](runtime-contract.md#responsibilities-and-limits));
such a derivation is not an Engine result. Neither current resource load (active plus queued work)
nor any occupancy derivation identifies a design's bottleneck or constraint by itself.

### 5.1 Derived-result arithmetic

The arithmetic that produces supported derived results is itself result-affecting under section 1.1:
two implementations computing these differently would report different supported observations for an
identical model, workload, seed and ordered command sequence. These rules are part of the interpretation.

1. **Elapsed-time subtraction saturates at zero.** Subtracting one simulated time from another
   yields zero when the subtrahend is the greater, never a negative interval. Lead time and every
   other interval-derived measure therefore has a floor of zero by construction.
2. **Throughput over a zero-tick window is zero.** Throughput is completed orders per observed tick;
   an observation window of zero ticks yields `0`, not a division by zero, an infinity, or a NaN.
3. **Mean lead time over an empty completed set is zero.** With no completed orders the mean is `0`,
   not a NaN from a zero denominator.
4. Simulated-time **addition is not saturating.** It is unchecked, and the pre-existing runtime
   time-addition guard rejects an overflowing schedule at the command boundary instead of silently
   producing a wrapped time — see the zero-mutation rejection contract of section 1.2.

Rules 1 to 3 are deliberate total-function choices at domain edges, not incidental defensive coding:
each replaces an undefined, wrapped, or non-finite value with a defined one a consumer can interpret.
Rule 4 is the deliberate exception, and it is a rejection rule rather than a value rule. Changing any
of them changes supported derived results for identical explicit inputs and is therefore a change to
the interpretation.

### 5.2 Derived-result accumulator register

Section 5.1 fixes edge-case *values*. This register fixes the **accumulation** behind each supported
derived result, because an accumulator's overflow policy changes the reported value for identical
explicit inputs exactly as a zero-denominator rule does. Sentinel cases and accumulators are two
halves of one contract; specifying only the first leaves the second ambient.

| Supported derived result | Accumulator | Accumulation rule |
|---|---|---|
| mean lead time | sum of completed-order lead times ÷ completed count | saturating (rule 1 below) |
| throughput | completed-order counter ÷ elapsed ticks | exact counting (rule 2 below) |
| backlog | count of incomplete accepted orders | current-state count, not a running accumulator |
| completed sales value | running total of completed order value | deterministic by completion order (rule 3 below) |

1. **Every tick-valued accumulator saturates; none wraps.** Mean-lead-time accumulation is the
   tick-valued accumulator: summing completed-order lead times saturates at the maximum
   representable tick value and never wraps to a negative or small positive total. A wrapping
   accumulator would not merely lose precision — it would report a qualitatively wrong mean, and two
   implementations differing only in accumulator width would disagree on a supported observation.
2. **Counting accumulators are exact.** The completed-order counter increments once per completing
   order and is exact for every run that can exist: each increment corresponds to a distinct
   materialized child job, so the count stays far below the 64-bit range however many
   `submitWorkload` commands a session issues. Its exactness rests on the counter's width, not on
   section 3's per-submission envelope — that envelope bounds one submission's children and says
   nothing about a session's cumulative completions, so it must not be cited as the justification.
3. **Value accumulation is deterministic by completion order.** Completed order value accumulates
   once per completing order, in the deterministic completion order this specification already fixes.
   Because floating-point addition is not associative, that fixed order is what makes the running
   total reproducible; an implementation may not reorder, batch, or parallelise the accumulation. It
   saturates toward infinity rather than wrapping.
4. **Conversion to the reported representation is IEEE 754 round-to-nearest.** Converting an integral
   accumulator to the reported floating-point value is exact below the mantissa limit and
   round-to-nearest above it — deterministic across conforming implementations either way, so no
   further rounding rule is required.

The mean-lead-time accumulator saturates in the current implementation, pinned by
`EngineDerivedResultConformanceTest`. Specifying wrap-around instead would turn an arithmetic defect
into a reproducibility rule, which is why a wrapping accumulator is a conformance failure rather than
an alternative behavior.

## 6. Conformance fixtures

Pinned behavioral fixtures prove the normative semantics above using representative explicit inputs.
They change together with any definition change (§1). They are pinned by
`EngineDispatchConformanceTest`, `SharedBacklogEquivalenceTest` (a mixed local/shared waiting
workload whose full resource-assignment trace must stay identical however the shared backlog is
indexed), `EngineDerivedResultConformanceTest`, `SessionControlAcceptanceTest`, `BoundedAdvancementConformanceTest`, the runtime event/observation
acceptance suites and the child-materialization acceptance and benchmark tests. Complete fixtures are
a precondition of promoting the interpretation, never a promotion by themselves. The fixtures must
cover at least:

1. current deterministic resource-selection behavior, including offline filtering/all-offline
   fallback, `canAcceptJob` ranking, `combinedQueueDepth`, `MachineId` tie-breaking, and the
   queue-before-`pendingMultiEligible` recovery cascade;
2. current unit-work decomposition/release ordering, plus the child-materialization envelope: `1`
   and `100000` accepted, `0` and `100001` rejected, and a rejected submission leaving no `Order`,
   child `Job`, queue entry, or scheduled event behind;
3. current scheduler same-time ordering;
4. per-machine queue FIFO order, proved by a dispatch sequence whose FIFO result differs from every
   plausible alternative ordering — arrival order must be distinguishable from `JobId` order,
   ordinal order, and step-duration order — plus the one-dispatch-per-trigger rule for a machine
   that has free capacity and more than one queued job;
5. waiting-path selection by eligible-set size: a multi-eligible step waiting in the
   shared backlog and never in a per-machine queue, and a single-eligible step waiting in that
   machine's queue and never in the backlog;
6. multi-eligible backlog ordering: two simultaneously placeable entries competing for one machine
   resolving in arrival order; an unplaceable head entry not blocking a later placeable entry with a
   disjoint eligible set; an entry placed on a machine other than the one selected when it first
   waited; the fixpoint rescan placing more than one entry per trigger; and the
   queue-before-backlog cascade order at both trigger points, step completion and machine coming
   back online;
7. session/control rules of section 1.2: `advanceUntil` converging with looping `advance()`
   under both bounds, reset-session reproduction, and the `Accepted`/`Rejected`/`Faulted` outcome
   shapes with `Rejected`'s zero-mutation guarantee — satisfied by the existing
   `SessionControlAcceptanceTest` rather than duplicated here;
8. deterministic event-type/entity-reference ordering and simulated times;
9. the section 5.1 derived-result arithmetic: elapsed-time subtraction flooring at zero, zero-tick
   throughput and empty-set mean lead time each yielding `0` rather than a non-finite value, and
   simulated-time addition overflow being rejected at the command boundary with no partial
   mutation rather than saturating;
10. the section 5.2 accumulator rules: **non-empty** mean-lead-time accumulation saturating rather
    than wrapping when the summed lead times exceed the representable tick range, exact
    completed-order counting across many submissions rather than one large one, and completed-value
    accumulation reproducing bit-identically under the fixed completion order;
11. section 2 rule 3 ranking exactness: a `combinedQueueDepth` whose two terms sum beyond the 32-bit
    range selects the same machine an exact sum would, proving neither the narrowing conversion nor
    a same-width addition survives — and proving it without appealing to section 3's per-submission
    envelope, which does not bound the cumulative waiting set;
12. terminal state / derived result agreement for repeated identical explicit inputs;
13. the section 4 rule 3 scheduling rule and its section 1.2 consequences: every processed
    scheduled event a step completion that publishes at its own time, and nothing queued behind an
    order's completion; a one-event budget spent on the next step completion however the same
    supported state was reached; the next command applying at the observed time after bounded calls
    across queue dispatch, the shared backlog, equal-time completions, availability recovery,
    interleaved commands, time guards before an event, exhausted and zero event budgets, reset, and a
    step completion whose cascade faults; and a command's outcome following the supported state it is
    issued at, whatever presentation frame budget reached that state.

Fixtures pin semantic outcomes, not DTO/JSON bytes, transport representation, or unrelated
non-behavioral observation fields.