# Arcogine — Architectural Overview

This document sits under the [Product Charter](/docs/product/charter.md), which defines Arcogine's enduring product direction and principles. This document describes the design philosophy and architectural principles that guide Arcogine's implementation *today*, and distinguishes principles expected to persist regardless of implementation from constraints specific to the current MVP. This document and the focused architecture/specification documents it links are the current architectural authority; Git and pull-request history hold the historical rationale for how they reached their present form, and selected non-normative [decision-rationale records](/docs/history/README.md#decision-rationale) may preserve why a significant choice was made without ever defining current architecture.

## Enduring architectural principles

These are expected to hold regardless of how the implementation evolves, because they follow directly from the Product Charter:

1. Repository must be reproducible, modular, testable, and collaboration-ready.
2. Deterministic acceptance tests and scenario-level validation are mandatory for simulation, replay, and verification contexts (see the [Determinism Contract](#determinism-contract) below for scope).
3. Agents only use approved command interfaces and never mutate simulation state directly — this is the same governance boundary the Charter asks of human and autonomous decision-makers alike.

### Domain authority

Each kind of fact has exactly one authoritative owner, and exposing a fact never transfers its
ownership:

- **Factory** owns authored production/design facts: the canonical model, its validation,
  publication and content identity ([Factory Design](factory-design.md)).
- **Engine** owns deterministic simulation interpretation and authoritative runtime state: dispatch,
  decomposition, scheduling, transfer and derived-result rules under one identified interpretation
  ([Engine semantics](engine-semantics.md), [Determinism Contract](#determinism-contract)).
- **Governance** owns controlled history, requirements/assertions, evidence use, conformance
  findings and governed change ([Governance and Conformance](governance-conformance.md)).
- **Operational** owns real-operation integration, accountable continuation and the relationship
  to external authority ([Operational Execution](operational-execution-digital-twin.md)).
- Observations, events, projections, DTOs, serializations and outward views expose these facts
  without becoming domain truth, and consumer domains (Challenge/Game, analytics, UI) never
  redefine authoritative production semantics.

### Determinism, equivalence, identity, and durability

These are canonical cross-cutting definitions, not a universal data model, lifecycle, or identity
service. Owning domain specifications select their concrete equality, input, output, and support
contracts. A current normative specification states what must hold; being normative does not by
itself create a separately versioned semantic entity or a stability promise.

#### Determinism

**Determinism** is a property of a defined computation: with the same definition and the same
complete result-affecting inputs, repeated evaluation produces the same specified observable
result. A claim must identify the admitted input domain, initial-state assumptions, and the output
boundary and equality being compared. Formally, for a fixed definition `D` and declared observation
boundary `P`, `O = P(evaluate(D, I))` is single-valued for each admitted complete input `I`.

Relevant inputs include ordered commands and any result-affecting random, time, numerical, or
external context. A seed alone is insufficient when the random algorithm or its consumption order
can differ. Incidental metadata may be excluded only by the owning contract, not after a comparison
fails; for example, the simulation contract excludes per-run correlation identity, not production
ordering or timing. Determinism neither requires identical implementation binaries nor establishes
equivalence between different definitions. It does not prove correctness against a specification,
accuracy about physical reality, persistence, or later reproducibility. The [Determinism
Contract](#determinism-contract) specializes this definition for the current simulation boundary.

#### Durability and related properties

Qualify a durability claim with the property, subject, scope, and horizon it actually concerns.
Do not infer one row of this table from another:

| Term | Meaning |
|---|---|
| **Persistence / storage durability** | Persistence stores information beyond its producing operation or process. A storage-durability guarantee specifies which accepted information survives which failures, recovery conditions, and retention horizon. Writing a file alone proves no broader guarantee. |
| **Referential stability / non-rebinding** | A reference declared stable continues to denote the same referent under its owning equality rule. This does not ensure that the referent remains available. A mutable lookup alias is not such a reference. |
| **Traceability** | Evidence-backed links identify the relevant inputs, definitions, implementations, occurrences, or decisions behind a recorded fact. A link or stated rationale is not by itself proof of causation. |
| **Historical interpretability** | Sufficient original meaning and context remain resolvable to understand a historical fact without silently substituting today's definition. This need not include an executable implementation. |
| **Reproducibility** | The necessary basis and capabilities can be recovered and used to repeat a computation against a stated result boundary. Required inputs, definitions, implementation/environment constraints, and dependencies must be available; a deterministic computation whose basis was lost is not thereby reproducible. |
| **Compatibility** | A scoped, potentially directional relationship: a particular consumer can read, execute, exchange, or convert particular input under stated conditions. It is not semantic equivalence and need not be symmetric or transitive. |
| **Stability / support commitment** | An authorized promise to specified consumers or uses about preservation, interpretation, compatibility, execution, migration, or support over a stated scope and horizon. The promise and evidence that it is fulfilled are different facts. |

A stored result can survive a restart while its interpretation has been lost. An old definition can
remain interpretable after its execution support ends. A development experiment can be traceable and
reproducible without an external stability promise. Conversely, a promise may exist without adequate
fulfilment evidence. These distinctions do not waive current admission or retention policies.

Use **durable commitment** only for a stated stability/support promise, not as a synonym for all
these properties. Promotion concerns that commitment; it does not make a computation deterministic,
create semantic equivalence, or cause previously recorded facts to acquire identity.

#### Equivalence, canonicalization, and fingerprints

The owning domain first states what objects are being compared and which distinctions matter for
the declared purpose. For a domain `X`, a semantic equivalence relation `~` partitions `X` into
equivalence classes. It must be reflexive, symmetric, and transitive. A tolerance, similarity score,
one-way compatibility test, or equality of one experiment's outcomes is not automatically that
relation. Different concerns can legitimately use different equivalence relations.

**Canonicalization** selects a deterministic representation of the distinctions the owning contract
retains. When `C` claims to represent the classes of a declared relation, its correctness condition
is `x ~ y` if and only if `C(x) = C(y)`: it neither distinguishes equivalent members nor collapses a
required distinction. This is a criterion for that claim, not a claim that every behavioral
equivalence has a computable complete canonicalizer. A concrete contract must state its scope and
justify any narrower representation rather than infer universal equivalence from a byte comparison.

A **hash function** maps representations to digests. A **fingerprint** is the resulting compact,
content-derived value together with whatever interpretation context its contract requires. For a
canonical-content fingerprint, the construction is `F(x) = H(C(x))`. The equivalence decision comes
before `C`, and `C` comes before `H`; hashing does not decide which differences are consequential.
Equal canonical bytes yield equal digests. Treating equal digests as equal canonical bytes relies on
the chosen hash's collision-resistance assumption, not mathematical injectivity. A collision is not
semantic equivalence, and hashing cannot recover information the canonicalizer discarded.

Always ask **a fingerprint of what, under which rules?** A model-content fingerprint does not thereby
identify its complete validation definition, the Engine interpretation, the executable build, or a
historical occurrence. A build digest can distinguish semantics-preserving refactors without proving
behavioral difference. A definition or schema reference helps only when its referent and resolution
are known. Reference spelling, equality, availability, and support are separate properties.

The [Factory model](factory-model.md) continues to own the concrete content distinctions and
canonical bytes used by `ModelFingerprint`; this vocabulary changes none of its field membership,
ordering, normalization, or golden vectors. The [Engine specification](engine-semantics.md) owns its
result-affecting rules. Equal results for one workload do not establish equal Factory designs or
behavioral equivalence for every admitted Engine input. Neither definition is a version of physical
reality, and an external observation does not become a simulation result by being compared with one.

#### Separate semantic responsibilities

| Responsibility | Question and owning surface |
|---|---|
| Semantic definition | What do authored facts or interpretation rules mean, and which distinctions matter? Factory and Engine specifications own their respective answers. |
| Canonical representation / codec | Which representation encodes those distinctions, and which grammar does a decoder accept? The owning representation contract defines framing, normalization, and validation. |
| Runtime executability | Does this implementation execute the behavior required by the represented content and requested interpretation? Engine owns this test; model validity alone is insufficient. |
| Exact reference / provenance | Which content, definition, implementation, inputs, or occurrence does the claim concern, and can that basis be resolved? The relevant domain or accepting authority owns the binding. |
| Compatibility / migration / support | Which consumers, conversions, preserved distinctions, losses, and obligations are supported? These are declared relationships, not consequences of a matching name. |
| Human naming / attestation | How does a party refer to or make a claim about identified things? The label's binding and any declaration supply its meaning; its spelling supplies no semantics. |

These responsibilities may share an artifact where a concrete requirement justifies that choice;
they must not be collapsed merely because a string is reused. A format discriminator does not define
an architectural generation, a specification title does not prove runtime support, and a support
label does not determine equivalence. Planning must name the represented-content or behavioral
prerequisite, rather than manufacture a dependency from a version label alone.

**Strict decoding** means enforcing the declared decoding contract: grammar/framing, canonicality
where required, and the applicable validity predicates. It does not establish historical provenance
that the input does not contain, or prohibit a separate importer or migration operation. Such an
operation must identify its source/target meanings and any preserved distinctions, assumptions, or
losses instead of silently pretending source data was authored under the target definition. No
legacy decoder or migration is introduced by this distinction; the current development reset policy
below and the [Factory artifact contract](factory-model.md#5-canonicality-and-decoding) still apply.

#### Human-readable labels and attestations

A human-friendly label is optional and orthogonal to content equality, determinism, encoding,
execution, and support. It may alias an exact reference or name a later declaration grouping several
references, such as a Factory definition, Engine definition, and build. A declaration or attestation
records what a party claims about that basis; a convenient name alone proves neither the claim nor
its fulfilment. No shared label, release-bundle, or attestation entity is introduced here.

Where a label selects a configuration or a declaration participates in an admission policy, resolve
and record the relevant basis as required by that use. The selected facts and explicit policy, not
the label's intrinsic spelling, supply its effect. Stable declaration bindings must not be silently
rebound; mutable aliases must not be passed off as exact historical references.

Development status is human-facing repository state only. It does not appear in canonical bytes,
fingerprints, decoder dispatch, runtime APIs, or provenance. The current Factory fingerprint contains
only its domain namespace, digest algorithm, and digest; the current Engine exposes no dedicated
definition identifier. Neither absence implies that an exact definition reference can never be
needed: the unresolved concrete boundary question remains in the [research
register](../research/research-register.md), with [question-specific
framing](../research/investigations/semantic-equivalence-and-reference-boundaries.md).

### Semantic evolution and support

The [definitions above](#determinism-equivalence-identity-and-durability) separate semantic meaning,
exact reference, representation, execution, persistence, and support. The rules here apply that
vocabulary to the current Factory and Engine development policy. Their specifications remain
normative and correctable without a successor-per-correction identity ladder. Promotion establishes
a scoped stability/support commitment, not determinism, content equality, or the first possible
record of an exact basis:

1. **Development status is not semantic data.** The [Factory model](factory-model.md) and [Engine
   interpretation](engine-semantics.md) are current development definitions. Between repository
   revisions their records, grammar, canonical bytes, fingerprints, validation and interpretation
   may be corrected or extended; each change updates the owning specification, fixtures and
   dependents together. No maturity/status token is encoded in their technical contracts.
2. **Determinism holds within a definition.** For the same current definition and explicit inputs,
   canonical bytes, fingerprints and supported simulation outcomes are deterministic. A running
   session executes one fixed interpretation over one immutable published model snapshot; neither
   changes under it.
3. **Current technical values are not exact cross-revision definition references.** A Factory
   content fingerprint does not identify the exact definition revision that produced it, and the
   current Engine has no dedicated definition identifier. Current proving stores bind their contents
   separately to the definition build that wrote them, refuse a mismatching binding, and are reset
   rather than migrated. A raw decoder enforces its current grammar and predicates. No reader may
   claim historical meaning that its available basis does not establish.
4. **Nothing creates a support commitment by accident.** Tests, golden vectors, in-process
   publication, implementation landing, persistence in a proving store, an internal file write, and
   a normative description of current behavior do not create a retained stability/support promise.
   Proving authorities declare disposable custody; retained, commitment-bearing admission requires
   an explicit owner declaration.
5. **Promotion is an explicit owner decision tied to a concrete stability/support need.** It
   identifies the exact definition through an unambiguous stable reference and states the supported
   uses and consumers, retained basis, and obligations. Any human-readable label is optional and gets
   its meaning from that binding or declaration. The commitment is recorded in the owning contract,
   not a repository-wide maturity state, registry, or version ladder. Factory and Engine commitments
   need not be made together.
6. **The exact basis bound by a promotion never rebinds.** The stable reference recorded by the
   promotion continues to denote the same definition, including rules no fixture has exercised and
   rejection behavior no consumer has observed. A materially changed definition — field membership,
   canonical bytes, validation, result-affecting interpretation — must not replace the definition
   behind that reference; later development continues without a commitment or a later commitment
   records a distinct stable reference. Promotion does not require a new shared semantic entity or a
   human-readable name.
7. **Support is scoped separately from meaning.** For a promoted definition, retaining its exact
   definition, retaining content, decoding, executing, migrating and interoperating are separate
   obligations the owning contract declares for the uses it accepts; recording or naming a reference
   creates none of them. Withdrawing support removes a capability; it never permits the stable
   reference of an accepted commitment to be rebound to changed meaning and never silently
   discharges obligations accepted records created.
8. **Misuse is handled explicitly.** Retention of, or reliance on, material outside a declared custody
   is a defect to diagnose and account for: further admission stops, the accepted facts and the
   definition they used are preserved as far as the evidence allows, and the use is neither silently
   rewritten nor treated as having promoted the contracts it touched.

Historical *occurrence* identities are a different kind of identity: a `ControlledRevisionId`, an
evidence reference, an evaluation occurrence or an operational continuation never rebinds within the
authority that accepted it, under the equality rule its owning contract defines, whatever the
maturity of the content it refers to. The owning contracts apply these rules to the [Factory
model](factory-model.md), the [Engine interpretation](engine-semantics.md), [controlled
revisions](controlled-revisions.md), [evidence](governance-evidence.md) and [operational
continuity](operational-continuity.md); review guidance lives in [Semantic contract
support](../development/semantic-contract-support.md). Why this boundary replaced an earlier
attribution-driven freeze is recorded, non-normatively, in the [provisional semantic-contract reset
rationale](/docs/history/decisions/2026-09-25-provisional-semantic-contract-reset.md).

## Current implementation constraints (MVP)

These describe today's implementation choices. They are not claims about Arcogine's permanent identity — see the Product Charter's [product boundaries](/docs/product/charter.md#9-what-arcogine-is-not) for why Java, current interfaces, and the current deployment model are implementation choices rather than product identity, subject to change as the product grows toward the full lifecycle described there.

1. Core simulation is written in Java with a **Java 21 language/API/bytecode compatibility baseline**. The preferred devcontainer currently uses JDK 25, and CI runs on JDK 21 to prove the supported floor; the compiler JDK and compatibility baseline are deliberately separate concerns.
2. The headless simulation core is the entire current implementation; there is presently no UI, HTTP API, or CLI product surface consuming it. Retained executable evidence is tests, conformance checks, and benchmarks. This describes today's layering, not a permanent claim that Arcogine's mature product surface has no outward consumer — a future one is introduced from the then-current supported runtime contract when a concrete product need exists (see [runtime contract](runtime-contract.md)).
3. Security-sensitive defaults remain local-first by default; non-local exposure requires explicit hardening controls (see [SECURITY.md](/.github/SECURITY.md)).

## Architectural implications of the Product Charter

Consequences of the Charter's thesis, stated at the conceptual level only — none of this is a module design, schema, or implementation commitment; see the Charter's [Architectural implications](/docs/product/charter.md#7-architectural-implications) section for the full list:

- Arcogine should not evolve separate simulation-only and production-only domain semantics.
- Model, version, and provenance concepts become fundamental once changes can move from design to reality — today's deterministic Engine execution and explicit run/model identity are simulation-scoped realizations of this, not the final answer.
- Purpose-specific observations and capabilities are preferable to exposing unrestricted mutable state, and are expected to remain so as new consumers (human roles, external systems, execution surfaces) are added.
- Real execution, when it exists, introduces safety, authorization, auditability, failure, and operational consequence as architectural concerns — the current implementation does not yet need to solve these because it does not yet execute anything real (see [SECURITY.md](/.github/SECURITY.md)).

## Simulation-First (current implementation)

Today's system is built around a **headless simulation core**, not a game engine. This describes the current implementation's architecture — simulation is a major Arcogine capability (per the Product Charter), not the entirety of its identity.

- No rendering dependency in the core
- Deterministic execution — same inputs always produce the same outputs
- Reproducible outcomes for testing, comparison, and analysis
- Explicit workload execution independent of any UI or network layer

## Core Architecture Philosophy: Events, State, Observations

This is the first-class architectural principle for Arcogine. It is a design heuristic *and* an architectural invariant: new features and refactors should be evaluated against it, and deviations should be a deliberate, documented decision rather than an accident of implementation order.

```text
Events mutate State.
State produces Observations.
Observations inform Decisions.
Decisions produce Events.
```

### Events

Internal events are immutable facts or scheduled facts in simulated time: order creation, task start/end, order completion, and machine availability changes. They carry the facts needed for deterministic Factory transitions and are processed by the scheduler.

The separate supported runtime event contract exposes ordered authoritative changes only after the corresponding state transition succeeds. Internal scheduler events remain implementation contracts rather than public compatibility types.
### State

Each subsystem exclusively owns its mutable domain state. Factory owns accepted orders, machines, jobs, queues, completion state, and production metrics. Finance owns financial state. A future inventory subsystem would own stock; workforce would own labor state.

Commercial/order intent and mutable production execution are represented separately. `Order` is immutable accepted intent; its same `OrderId` identifies an authoritative order-execution aggregate. Acceptance deterministically materializes one unit-quantity `Job` per requested unit, in zero-based ordinal order. Each child has its own `JobId`, traverses the routing once, and can be dispatched independently under the existing selector. The aggregate records release and completion quantities and is the sole source for order completion, backlog, sales KPIs, and lead time; only its final child completion emits `OrderCompleted` with both identities and full order commercial facts.

State should:

- have exactly one authoritative owner;
- avoid synchronized duplicate representations of the same fact across subsystems;
- be mutated only by its owning subsystem, in response to an event it handles;
- never be mutated directly by agents or by unrelated domains.

#### State ownership table

Concrete, source-level version of the rule above — checkable in review, not just implicit:

| Fact | Owning class | Mutated by |
|---|---|---|
| Machines, machine availability | `MachineStore` (owned by `FactoryHandler`) | `MachineAvailabilityChange` |
| Accepted order intent (`OrderId`, product, quantity, creation time, `OrderPrice`) | immutable `Order` in `OrderStore` (owned by `FactoryHandler`) | Created at `OrderCreation`; immutable thereafter |
| Jobs, job status (production lifecycle) | `JobStore` (owned by `FactoryHandler`) | `OrderCreation` (creates), `TaskEnd` (advances/completes) |
| `OrderValue` | Derived by immutable `Order` from quantity × `OrderPrice` | Derived, not separately mutated |
| `CompletedSalesValue`, `completedSales` | `FactoryHandler` | `TaskEnd` (once when the order-level execution aggregate completes) |
| Ledger, `Cash`/`Sales` balances | `Ledger` (owned by `FinanceHandler`) | `OrderCompleted` |

The [runtime contract](runtime-contract.md) requires supported `RuntimeEvent` state-change facts to be derived/published only after authoritative processing succeeds.


Order/job creation has both an internal event path and a supported runtime command: `FactoryRuntime.submitWorkload(productId, quantity, unitPrice)` is the consumer-neutral entry point for callers, without any need to own a `Scheduler` or choose a simulation time. `FactoryRuntime` is only built via `FactoryRuntime.forModel(FactoryModelVersion)`, which assembles and owns its own exclusive `FactoryHandler`/`Scheduler` pair — it is never wrapped around an already-live `FactoryHandler` another scheduler might also be driving, and it does not expose that `FactoryHandler` directly (callers observe state through its own read-only projections instead). Both paths use the same package-private `FactoryHandler.submitOrder(...)` acceptance operation and create the same immutable `Order` and deterministic quantity-`N` set of unit-quantity sibling `Job`s under the same `OrderId` aggregate, with identical routing/dispatch semantics; `submitOrder` itself is not public, so scheduler/time plumbing never leaks past `FactoryHandler`.

`FactoryRuntime` also implements the consumer-neutral session-control semantics of [Engine semantics §1.2](engine-semantics.md#12-session-and-control-semantics), additive to the shape above: `modelVersion()` retains and exposes the exact `FactoryModelVersion` the session was instantiated from, for the session's full lifetime; `advanceUntil(SimTime targetTime, long maxEvents)` sits alongside the unchanged single-event `advance()`, processing pending events one at a time until either the next event's time would exceed `targetTime` or `maxEvents` events have been processed, implemented directly in terms of `advance()` so the two can never diverge in ordering or dispatch behavior; `reset()` returns a fresh `FactoryRuntime.forModel(modelVersion())` rather than mutating the existing session in place, since `FactoryHandler`'s stores have no partial-reset subsystem to mutate safely. `submitWorkload` and `setMachineAvailability` — the two externally initiated runtime changes `FactoryRuntime` exposes — always return a definite `CommandResult<T>` (a stable code/diagnostic, `modelVersion()` provenance, and every `Event` scheduled as a direct effect of the command, captured by a command-scoped `RecordingScheduler` window rather than a permanently growing history) instead of ever throwing or returning `void`. `CommandResult` is a three-way sealed type: `Accepted`, `Rejected` (wraps the original, already-structured, sealed `SimError`; verified pre-mutation — `FactoryHandler.submitOrder` preflights its scheduling check before mutating any store, `setMachineAvailability` verifies its own two rejectable conditions from `machinesView()` before calling into `FactoryHandler` at all — so a `Rejected` result never follows partial mutation), and `Faulted` (a genuine engine fault surfacing from deep in `setMachineAvailability`'s online-machine dispatch cascade, after mutation may already have started; making that whole cascade provably preflight-safe was judged disproportionate, so `Faulted` reports it as a definite result instead of letting it throw past the command boundary, while making clear — unlike `Rejected` — that it does not promise zero mutation). Acceptance and execution outcome are independent facts, not one axis, so `Faulted` carries the same accepted value `Accepted` would have alongside the fault — the requested change genuinely was applied before the later failure, and a caller must not lose which entity was affected just because execution subsequently failed. `pendingWorkView()` exposes `FactoryHandler`'s cross-machine `pendingMultiEligible` backlog (see [Engine semantics §2](engine-semantics.md#2-resource-selection-and-dispatch-semantics)) as read-only `PendingWorkView` entries — necessary because that waiting work is not associated with any single machine and so is invisible to `MachineView.queueDepth()`.

The supported runtime observation/event contract adds `FactoryRuntime.observe()` as the separate supported current-state boundary required by the [runtime contract](runtime-contract.md). It returns immutable, deterministically ordered resource, aggregate-order, unit-work decomposition child-job, and multi-eligible-pending-work projections plus the factory's authoritative backlog, completed-order/value, lead-time, and throughput calculations. Metadata carries an opaque per-runtime `RunId`, `FactoryModelVersion.fingerprint()` source-model provenance, current simulated time, explicit active/quiescent advancement state, and a `latestEventSequence`. This projection never exposes `FactoryHandler`, mutable stores, or internal scheduler events.

The supported runtime observation/event contract also implements the supported `RuntimeEventEnvelope` contract on top of that boundary: `RuntimeEventType`/`RuntimeEventPayload`/`AffectedEntityRef` (`product/domains/factory/.../process/`) are a taxonomy distinct from the internal scheduler's `EventPayload`, and `FactoryRuntime` only ever constructs an envelope, via its single package-private `emit(...)` point, after the authoritative transition it describes has already succeeded. `submitWorkload` emits `ORDER_ACCEPTED` (carrying every created child `JobId`) followed by one `JOB_DISPATCHED`/`JOB_WAITING` per created job describing its resulting placement; `setMachineAvailability` emits `MACHINE_AVAILABILITY_CHANGED` only for a genuine online/offline transition (a no-op request emits nothing) plus any `JOB_DISPATCHED` a resulting dispatch cascade produced, including on the `Faulted` path where only the mutation that actually occurred is reported; `advance()` emits, for each processed `TaskEnd`, `JOB_STEP_COMPLETED`, then `ORDER_COMPLETED` when that step completed the order, then `JOB_DISPATCHED`/`JOB_WAITING` for every placement change the same `TaskEnd` authoritatively caused — freed capacity re-places both the completing job onto its next routing step and whatever queued or multi-eligible backlog work that machine can now accept, derived by diffing authoritative placement rather than by re-exposing internal scheduler events. Internal scheduler markers `FactoryHandler` ignores (`TaskStart`; the `OrderCompleted` a terminal `TaskEnd` schedules for other internal handlers) emit nothing and, by construction, change nothing `observe()` reports: observed time advances only with emission, and `RuntimeRunState` reflects pending *authoritative* work rather than a non-empty queue, so every observation fact stays coherent with one `latestEventSequence` boundary. `RuntimeObservationMetadata.latestEventSequence()` is a live cursor advanced in lockstep with emission, independent of when a caller retrieves the events themselves: `FactoryRuntime.drainSupportedEvents()` returns and clears everything accumulated since it was last called, rather than retaining an unbounded, cursor-replayable history. That retained/replayable-by-cursor responsibility is deliberately not part of this boundary (see the [runtime contract](runtime-contract.md); recovery/resynchronization is later distribution hardening); a caller needing durable replay retains the drained events itself.

The supported runtime observation/event contract closes the headless contract without adding new abstractions to it. `HeadlessClosureAcceptanceTest` proves that a consumer joining an already-progressed runtime reconstructs the complete supported view from one `observe()` result alone — no retained or replayed runtime events, no `FactoryHandler`/mutable store, no scheduler-event replay, no outward-projection DTO — and that `latestEventSequence` survives draining so such a consumer knows where to continue; that supported events and observations close over the same authoritative transitions (the delta after an observation at sequence `S` is exactly `S+1..S'`, is consistent with the later observation, and explains what changed, without events carrying redundant full-state payloads); and that the active production bottleneck is identifiable from `ResourceObservation` facts alone, independently by carried load (active plus queued work) and by busy-tick utilization, deterministically and reproducibly. Those facts rest on authoritative runtime behavior: `FactoryHandler.handleTaskEnd` credits each finished step's duration to the resource that performed it, so `ResourceObservation.busyTicks()` reports cumulative utilization; `FactoryRuntime.advance()` reports the whole `TaskEnd` placement cascade as `JOB_DISPATCHED`/`JOB_WAITING`, pinned by `taskEndDispatchCascadeIsReportedByTheSupportedEventStream`; and `observe()` derives time, throughput, and `RuntimeRunState` from the supported boundary rather than the raw scheduler cursor, pinned by `processingANoOpInternalMarkerLeavesTheSupportedObservationUnchanged`. The complementary structural fact — outward-projection DTOs never re-entering domain decision paths — has no executable rule today because no outward adapter exists to enforce it against; a future outward adapter should add an architecture rule scoped to its own package. The core/headless runtime contract is therefore complete. Outward-consumer convergence is not an Engine objective (see [Factory Simulation Engine Readiness](../planning/factory-simulation-engine-readiness.md)): a future consumer is introduced from this supported contract when a concrete product need exists, not migrated toward as standing backlog. Runtime-event recovery/resynchronization hardening (retained runtime-event history, replay-by-cursor, reconnect/resume, checkpoint/restore) remains a separate, independently-triggered concern.

### Observations

Observations are immutable, read-only projections of authoritative state. The retained FactoryRuntime exposes purpose-specific resource, order, job, pending-work, and performance facts without exposing FactoryHandler, mutable stores, or internal scheduler state. Finance exposes its read-only ledger through LedgerView.

Observations should be derived from their owner's authoritative state, expose only what a consumer needs, and never become a second source of truth. Any future decision-maker or outward consumer must receive an explicit purpose-specific view rather than unrestricted mutable state.

### Domain observations vs. outward projections/DTOs

A domain observation supports an internal decision; an outward projection supports an external consumer and may have a separately versioned wire or display shape. They may report related facts but should not share a type merely because their fields look alike. Arcogine currently has no outward adapter or internal Finance observation type.
### Decisions

A decision is a choice made from an observation; a change to authoritative state must still pass through the owning capability's explicit command or event boundary. Humans, agents, and other future decision sources must not bypass that ownership boundary. This is a durable constraint, not a claim that a decision-making consumer exists today.

### Commercial, operational, and financial facts

The accepted `Order` is the immutable commercial record: its product, quantity, creation time, and agreed unit price are fixed at acceptance. Factory owns production execution and derives completed sales value from completed orders. Finance separately interprets `OrderCompleted` under its current immediate-settlement policy and records balanced postings in its ledger.

| Fact | Owner | Meaning |
|---|---|---|
| Accepted order intent and agreed unit price | Immutable `Order` in Factory | Historical commercial terms |
| Jobs, resources, routing progress, and completion | Factory runtime | Operational state and performance |
| Completed sales value | Factory runtime | Operational value of completed orders |
| Cash and sales ledger balances | Finance ledger | Financial interpretation of completed orders |

Changing a future pricing or demand policy, if one is later required, must not rewrite already accepted order terms. No current mutable offer-price state, pricing policy, or demand-generation model is selected by this architecture.
### What should trigger architectural review

Treat any of the following as a signal to stop and reconsider the design, not just implement around it:

- one subsystem mutating another subsystem's state;
- a mutable "observation" (anything handed to an agent/consumer that they could write through);
- duplicated authoritative state (the same fact represented as separate mutable fields in two subsystems);
- synchronization setters proliferating between domains (`setX`/`syncX`-style cross-domain pushes);
- agents or policies reaching directly into mutable subsystem internals instead of going through an observation;
- adding a subsystem requiring pairwise wiring changes to every existing subsystem;
- event ordering becoming implicit, or dependent on registration/construction order rather than an explicit, documented contract;
- a monetary accumulator (cash, profit, receivables, or similar) appearing outside Finance;
- Finance inspecting another domain's mutable state to infer what happened, instead of reacting to an event that domain emitted;
- an unbalanced journal entry able to enter financial state;
- attribution or provenance metadata being read by domain, dispatch, pricing, or simulation logic instead of the requester appearing as a modelled payload field;
- a shared actor, subject, decision-source, or capability type appearing before multiple consumers have demonstrated the same equality/namespace/lifecycle contract (see [Attribution and decision boundaries](#attribution-and-decision-boundaries)).

These guardrails are part of the current architecture and are reinforced by executable architecture tests where the invariant can be checked mechanically. Remaining runtime-readiness work is tracked separately in [Factory Simulation Engine Readiness](../planning/factory-simulation-engine-readiness.md); that document is planning guidance, not architectural authority.

## Attribution and decision boundaries

The [Events–State–Observations](#core-architecture-philosophy-events-state-observations) model says how a choice becomes a state change. It does not say **who is answerable** for that change, **what produced** the choice, or **what was acted on**. Those are separate questions, and they are easy to conflate because the implementation currently answers all of them with free-form strings.

This section records the durable semantic result of the [Agency and decision boundary investigation](../research/investigations/agency-decision-boundary.md). Everything here is a **rule, not a type**. No Java type, module, persisted field, identity contract, or delivery track is introduced by it, and nothing here describes implemented capability: `Event` and `RuntimeEventEnvelope` carry no attribution today. Read these as constraints on future work.

The headline result: **no platform-level `Agent` abstraction is currently justified.** Designs that collapse or hard-bind the actor, decision-source, and subject roles below fail the investigation's proving cases, and no additional cross-case invariant was found that warrants a shared platform `Agent` concept now. This is a claim about current evidence and current consumers, not an impossibility claim — a future design that preserves the roles compositionally is not foreclosed. `agent` remains a useful application/domain label; it must not become a platform ontology, superclass, shared module, or durable identity category merely because that label already exists.

### Actor, decision source, and subject are distinct roles

```text
Actor            who or what bears responsibility for a participation
Decision source  the mechanism that produced the choice
Subject          what is acted on or represented
```

- **Role is a property of a participation, not an intrinsic permanent kind of an identity.** The roles must stay distinguishable; the identities occupying them need not be different. A human acting directly is both actor and decision source, and that is not a modelling failure.
- **One decision source may serve many actors**, and **one actor may change decision sources without changing its attributable identity**. Attribution that dissolves when a controller is replaced is not attribution.
- **A responsibility-bearing actor concept survives** across humans, organizations, software, planners, simulated participants, and external systems — as a concept, and not always present. An external data source is not automatically an actor.
- **The actor concept does not justify a shared actor type.** Arcogine has no `ActorId`, actor reference, actor-kind enum, actor namespace, or actor equality/lifecycle rule, and should not acquire one until multiple concrete consumers demonstrate the *same* equality, namespace, lifecycle, and interoperability contract strongly enough that domain-local representations would duplicate one semantic invariant. A committed implementation consumer is strong evidence for that; a consumer *count* is not a threshold. A closed actor taxonomy is specifically not justified — W3C PROV, the usual source cited for one, states its agent types do not cover all kinds of agent.
- **An acting party and a represented principal may differ**, and where they do, both must remain recoverable. Whether the represented principal retains responsibility is a **domain policy question**: delegation, impersonation, and transfer of responsibility are different relations, and Arcogine takes no general position that a delegator stays accountable.

### Four kinds of provenance must stay distinct

```text
attributable actor      who is answerable
recording provenance    what caused Arcogine to record something
decision provenance     which mechanism(s) produced the choice
external data source    where an observation came from
```

These must not collapse into one `source` or `actor` field merely because several current records happen to be strings. On current `main`, attribution-shaped names already mean different things, two of them are not pinned down by durable authority at all, and not all of them are even strings. **No field below may be renamed, reinterpreted, or mechanically migrated** into a future attribution capability:

- `RevisionRecorder` is, **as a whole**, persisted recording provenance identifying what caused Arcogine to record a controlled revision. Its internal `source` / `subject` decomposition is **underspecified by durable authority** — the [controlled revision contract](controlled-revisions.md) permits representing the recorder with a small source/subject value without fixing which slot means what, and the implementation says only that the pair identifies "the source and subject that caused a revision to be recorded". Do not read `source` as a canonical mechanism/channel or `subject` as a canonical actor/principal. It is persisted and participates in idempotency equality in immutable governance history, so any future attribution must be **additive**.
- `ChangeProvenance.source` is *producer* provenance for a change set: its contract states plainly that **none of its fields are identity**. What the slot carries beyond that is **not established** — current call sites use both role-like and mechanism-like values — so it is neither a party identity nor, on current authority, a role label. It must not be migrated into a shared actor identity.
- `RequirementSource` is a requirement's *governing publication* provenance, and is a sealed domain type rather than a free-form string. Its sense of "authority" is **publishing body**, not authorization authority — a direct collision with the authorization sense of the word used elsewhere in this section.
- Historical decision-event descriptions were not attribution; that event vocabulary has been retired.

**Subject is distinct from actor, and there is no universal subject reference.** `AffectedEntityRef` (Factory-owned, sealed, transition-scoped) and `ChangedEntityRef` (Governance-owned) coexist deliberately, because their equality, namespace, ownership, and lifecycle contracts differ — shared appearance is not shared semantic identity. Neither is Arcogine's universal subject reference, and the word `subject` in `RevisionRecorder` does not establish Arcogine's meaning of "the subject of an operation": when the operation sense is meant, say so explicitly.

### Decision-source internals are not world semantics

Goals, beliefs, memory, plans, prompts, behaviour-tree state, planner search state, model weights, and hidden reasoning traces stay **private to the mechanism** that uses them. They do not become shared Arcogine semantics because a decision-maker happens to have them.

Recording a hidden reasoning trace as authoritative causal provenance would manufacture **false provenance** — a stated rationale need not be the operative cause. A voluntarily recorded public rationale, declared policy identifier, commitment, or audit explanation is a legitimate durable record of what a party *asserted*, and must never be presented as evidence of the hidden cause.

Where it is materially relevant, Arcogine must be able to explain **which mechanism(s) were relied on to produce a decision**. That requirement is deliberately weaker than a schema:

- an exact immutable source version is required **only** where a consumer's own reproducibility contract requires it, and must **never be fabricated** for an opaque external participant that cannot supply one;
- where a version does exist, it must be a resolved immutable identifier, never a mutable alias;
- there is **no single `DecisionSourceRef`** and no commitment to a singular source — multi-source, ensemble, recommender-plus-human, and policy-check provenance must remain expressible later;
- a **stateful or online-learning source**, whose material behaviour changes without any version changing, is an explicitly **unresolved** case. Identity plus version may not identify it. Reopen this when a concrete consumer needs such a source to be explainable or replayable; do not force it into an identity+version shape that does not describe it.

### Requests, replay, and the attribution/outcome boundary

**An attributed operation request is the preferred first and common durable boundary** when a decision results in a requested semantic change, and Arcogine does not need a universal shared `Decision` type. This is the narrow form and the only form: a decision and the operation it requests are *not* universally one durable fact. A domain remains free to own a distinct durable recommendation, approval, denial, standing authorization, or exception/risk-acceptance record when a real consumer requires it — [Governance and Conformance](governance-conformance.md) already names several — and those must not be recast as operation requests for vocabulary symmetry.

Where a decision is durably recorded, it should identify the **observation boundary that bounded it** — what the decision source could actually know at the time — so the decision stays explainable from that input plus the source's own private state. This is what keeps a simulated participant from acting on information it could not have had. The reference itself stays domain-owned; there is no shared cross-domain observation reference, and an Engine observation cursor is not one.

Four replay operations stay distinct:

```text
re-execute the decision source
!= replay the recorded decision
!= replay the requested operation
!= replay the resulting transition
```

The general move for nondeterministic behaviour is to **convert the relevant nondeterministic boundary into durable recorded input** where a consumer's contract requires replayability — the [Determinism Contract](#determinism-contract)'s ordered external commands already work this way. Hidden source internals never become replay state.

Attribution and outcome have a two-part rule:

1. **Incidental attribution metadata must never affect outcome.** Attribution attached to a request for provenance must not reach any domain, dispatch, pricing, or simulation computation. If a requester genuinely should influence dispatch, it must appear as a **modelled field in the operation payload**, never be smuggled in through attribution metadata.
2. **Explicitly modelled authority is a legitimate exception.** Actor identity may affect behaviour only through an authority determination that is explicitly modelled, whose outcome is recorded, and which is itself a reproducible input on replay — never through an implicit read of attribution by unrelated domain logic.

This preserves the boundary between provenance and explicit policy input; it does not settle an authorization model. The cross-cutting authority question — *may this actor perform this operation on this subject under the applicable policy?* — is a useful **question shape** for locating where policy belongs. It is not a persisted or public input schema, Arcogine asserts no rule that authorization consults only the current actor, and ownership of reusable actor/capability semantics remains open (see [Operational Execution and Digital Twin](operational-execution-digital-twin.md) §5, which must not become that owner by default).

### What is deliberately not introduced, and what would reopen it

| Not introduced | Why | Reopen when |
|---|---|---|
| Platform `Agent` type, actor/subject/decision-source value types, actor kind enum | No current consumer proves a shared equality/namespace/lifecycle contract | Multiple concrete consumers demonstrate the *same* such contract (see above) |
| Shared temporally extended `Capability`, procedure, or skill type | The [unit-work decomposition](engine-semantics.md#3-unit-work-decomposition-semantics) aggregate/child pattern already supplies aggregate intent, child identity, correlation, and a completion rule | A concrete consumer shows that pattern cannot express a real temporally extended capability |
| Agent message bus or conversation ontology | Typed operations, events, observations, results, and explicit public commitments suffice; an accepted order is already such a commitment | A proving case shows ordinary domain interaction cannot represent a required interaction cleanly |
| Agency module, subsystem, or delivery track | The result is cross-cutting semantic distinctions, not a coherent implementation responsibility | A surviving shared contract acquires an owner that no existing module can hold |
| A universal `Decision` record, subject reference, or observation reference | Each is domain-owned today for reasons that differ per domain | A cross-domain consumer needs one contract, not merely one shape |

Each row is a refusal justified by *current* evidence and current consumers, not a permanent prohibition, which is why each states what would reopen it. None of them commits Arcogine to persisted or public identity equality, a shared namespace or lifecycle contract, a permanent closed taxonomy, or cross-module equality semantics, which is why they belong in this section at all. A future change that would introduce any of those, or another hard-to-reverse public or persisted semantic commitment, is a change to Arcogine's architecture: it must be reconciled into the architecture or specification document that would own the new semantics, with its consumers and executable invariants updated in the same change, rather than absorbed into this section as another refusal.

W3C PROV and comparable external models remain **vocabulary donors and outward projection targets**, consistent with the [external representation policy](external-representations.md) and [Standards Alignment](standards-alignment.md). They are not Arcogine's domain model, and adopting their vocabulary never imports their ontology.

## Commercial, Operational, and Financial Truth: the Finance Domain

Factory and Finance own different interpretations of completed work:

- **Commercial truth** is the immutable accepted `Order`, including its product, quantity, creation time, and agreed unit price.
- **Operational truth** is Factory's execution state and derived facts such as completed sales value, backlog, throughput, and lead time.
- **Financial truth** is Finance's interpretation of completed-order facts, recorded in its balanced ledger.

The core rule is: **operational domains emit facts; Finance owns the financial interpretation of those facts.** Finance reacts to `OrderCompleted` and does not inspect Factory's mutable state to infer transactions.

### Why a Finance domain, deliberately minimal

Finance remains because its ledger and `FinanceHandler` provide executable ownership evidence: balanced postings are enforced by `JournalEntry`, and only Finance may post to its ledger. The current immediate-settlement policy records each completed order as a debit to Cash and a credit to Sales. This establishes a domain boundary; it is not an accounting framework.

### Event and money boundary

`OrderCompleted` carries the order identity, completing job identity, product, quantity, and immutable unit price. `FinanceHandler` derives the order value and converts it at the ledger boundary to a quantized `BigDecimal`. `CurrencyPolicy` applies the current two-decimal `HALF_UP` rule, and `JournalEntry` rejects unbalanced postings. This ledger representation enforces Finance's exact debit-equals-credit invariant without changing the commercial price or Factory's operational value.

### Ownership table

| Concept | Meaning | Owner |
|---|---|---|
| Accepted order and agreed unit price | Immutable commercial terms | Factory `Order` |
| Production state and order completion | Operational facts | Factory |
| Completed sales value, backlog, throughput, lead time | Operational measures | Factory |
| Financial postings, Cash, Sales balance | Financial interpretation | Finance ledger |

Further accounting capability requires concrete product requirements and a reconciled architecture change. Pricing, demand generation, and commercial policy are not part of the retained Finance capability.
## Discrete-Event Simulation (DES)

The simulation advances in deterministic event-time order. The scheduler processes only meaningful events, skips idle time, and preserves FIFO ordering among events at the same tick.
### Event taxonomy

The current internal event vocabulary is limited to Factory execution facts and commands: `OrderCreation`, `TaskStart`, `TaskEnd`, `OrderCompleted`, and `MachineAvailabilityChange`. Supported `RuntimeEvent` types form a separate consumer-facing taxonomy and report only authoritative changes.
### When a new domain deserves its own module

Not every new metric or piece of derived behavior warrants a new `XHandler`/module — Finance is justified by more than "it computes a number Factory doesn't." The admission rule: **a new domain deserves its own handler/module when it owns mutable state with its own invariants and lifecycle, not merely because it has a new metric or helper function.** Concretely, ask:

- Does it own state that nothing else should be able to mutate directly (the way `Ledger` owns postings, or `FactoryHandler` owns job/machine lifecycle)?
- Does that state have its own invariants worth protecting at construction/mutation time (the way `JournalEntry` rejects unbalanced entries)?
- Does it react to events from other domains and produce its own facts, rather than just recomputing a view over another domain's existing state?

If the answer is genuinely yes to state-with-invariants, it's a domain with an explicit owner, its own invariants, and only the event/runtime integration needed by a real consumer. If the answer is no — it's a computed value over state another domain already owns — it belongs as a method/projection on the existing owner (like `FactoryHandler.completedSalesValue()`) or in a separately supported consumer projection, not a new module. This keeps the module count matched to genuine ownership boundaries instead of granularity of features.

## Module Structure

The Java codebase uses a Gradle multi-module layout rooted at `product/`:

```text
product/
├── types/                Shared typed IDs, time, quantities, and errors
├── governance/           Controlled revisions, semantic change, requirements,
│                         conformance, and evidence-use capabilities
├── simulation/           Deterministic event scheduler and EventHandler contract
├── domains/
│   ├── factory/          Canonical model, runtime, machines, orders, jobs, routing
│   └── finance/          FinanceHandler and balanced double-entry ledger
├── consumer/
│   ├── challenge/        Game-owned challenge definitions, validation, evaluation,
│   │                     catalogue/economics, and attempt comparison
│   └── challenge-factory-integration-test/  Test-only proof of independent validation axes
└── architecture-conformance-test/          Test-only cross-domain ownership rules
```
### Dependency graph

```text
types ← simulation ← factory
                    ← finance

types ← governance ← factory

challenge (independent game-owned boundary)

challenge-factory-integration-test ← types, factory, challenge (test-only)
architecture-conformance-test ← types, factory, finance (test-only)
```

Governance depends on `types`; Factory depends on the narrow Governance ports it implements. Challenge remains independent of the production simulation and domain modules; the test-only Challenge–Factory integration module proves that canonical Factory executability and Challenge admissibility are independent validation axes without either module depending on the other. The architecture-conformance module scans current production sources to protect retained Factory and Finance ownership rules.

## Event Dispatch Architecture

`EventHandler` is the current contract for Factory and Finance event consumers. No retained production code composes the domains into an application-wide handler chain. `FactoryRuntime` owns its FactoryHandler and Scheduler for explicit workload execution; Finance remains an isolated event consumer for the financial ownership capability. This architecture does not prescribe a replacement orchestrator or a future application topology.
## Type System

Java features available within the **Java 21 compatibility baseline** map cleanly to the domain:

| Concept | Java feature |
|---------|-------------|
| Typed IDs | `record MachineId(long value)` |
| Value objects | `record SimTime(long value)` |
| Sum types | `sealed interface EventPayload` with record permits |
| Error hierarchy | `sealed class SimError extends RuntimeException` |
| Pattern matching | `switch (event.payload())` with exhaustive pattern matching |

## Determinism Contract

Deterministic simulation is architectural, not an implementation convenience: acceptance tests,
comparison of design candidates, historical explanation of a run, and challenge evaluation are all
meaningless if executions under the same definition and complete inputs can legitimately disagree. At the same
time Arcogine must be able to change how it interprets a design — dispatch ranking, work
decomposition, scheduling, transfer timing — without pretending the designer authored a different
production system, and without claiming that historical results were produced under rules they were
not. The [canonical definition of determinism](#determinism) supplies the computation and comparison
scope; the following rules specialize it for simulation. Five rules hold that line:

1. **A simulation outcome is a function of explicit inputs and one Engine interpretation.** The
   reproducibility inputs are the authored model content, the Engine interpretation, the explicit
   workload, the seed and other random inputs, the ordered external commands, and any other
   explicitly identified result-affecting input. Nothing else may influence acceptance, rejection,
   assignment, ordering, simulated time, terminal state or derived results; run identity is
   correlation metadata and never affects an outcome.
2. **No result-affecting rule may remain ambient.** Any limit, ordering rule, tie-break, rounding or
   accumulation rule that two implementations could choose differently is part of the
   interpretation or is an explicitly identified input. One interpretation covers a run's complete
   result-affecting behavior and is fixed when the run is established.
   [Engine semantics](engine-semantics.md) owns the exact rules, their membership test and the
   conformance fixtures that pin them.
3. **Authored facts and interpretation have different owners.** Facts describing the production
   system the designer authored belong to the canonical model and its fingerprint; rules describing
   how Arcogine interprets any such design belong to the Engine interpretation. Changing
   interpretation alone never changes the authored model's fingerprint, and authored facts are never
   synthesized to make an interpretation applicable. Publication validity never establishes
   executability: an interpretation executes only the represented content its own definition
   supports and refuses the rest before runtime mutation
   ([transfer applicability](transfer-applicability.md)).
4. **Interpretation changes are explicit.** An intentional change to result-affecting behavior — a
   bug fix that observably changes outcomes included — is a definition change recorded in the
   specification and its fixtures in the same change; it is never unrecorded implementation drift.
   Repairing an implementation so that it conforms to the specification is not such a change. The
   current implementation executes the repository's current Engine definition; no placeholder
   identifier is emitted to imply stronger provenance.
5. **Historical basis and support commitments are separate.** Development results may have
   traceability and reproducibility when their exact basis and capabilities are retained. Neither a
   model fingerprint, a run ID, nor a human development label supplies the exact Engine definition
   basis after it changes. Commitment-bearing admission requires an explicit promotion. A promoted
   commitment states which exact references, definitions, fixtures, and content remain resolvable
   for its accepted uses; historical interpretability does not automatically promise permanent
   re-execution.
   Comparison across interpretations or development revisions is explicit and owned by the
   consumer making the claim. Neither a shared name nor a matching result from one run proves
   behavioral equivalence.

Nondeterministic boundaries a consumer needs to replay — clocks, external inputs, human or agent
decisions — are converted into recorded explicit inputs rather than admitted into the
interpretation. Runtime provenance carries the source model's fingerprint and run correlation. No
dedicated exact Engine-definition reference is currently emitted; a consumer that needs one must use
a separately established exact basis rather than infer it from development status
([runtime contract](runtime-contract.md)).

The current implementation realizes this contract with:

- Priority queue orders events by time, with FIFO tie-breaking
- Java strict floating-point semantics; compilation targets the Java 21 compatibility baseline
- No concurrent mutation of simulation state

Given the same published factory model, current Engine definition, and explicit workload/commands,
fresh `FactoryRuntime` sessions produce identical ordered supported `RuntimeEvent` streams and
terminal `RuntimeObservation` state. Tests comparing semantic outcomes account for the per-run
`RunId`; a test that depends on that correlation identity is wrong.

This determinism contract is scoped to simulation, replay, and verification contexts, where it is a critical property. It is not a claim that real-world execution itself must be, or will be made, deterministic — production operates in a non-deterministic world of real machines, people, and failures. See the Product Charter's [continuity with current architecture](/docs/product/charter.md#8-continuity-with-current-architecture) section for this distinction.

## Factory model and runtime provenance (current state)

The runtime retains the source `ModelFingerprint` and opaque per-runtime `RunId`. The fingerprint
identifies canonical Factory content in the current producing context; `RunId` is correlation only.
The runtime executes the current result-affecting [Engine definition](engine-semantics.md), but no
dedicated Engine-definition identifier is exposed today. That omission is intentional until a
concrete boundary requires an exact resolvable reference.

Factory runtime semantics are instantiated through the canonical-model seam: `FactoryModel` (validated) → `FactoryModelVersion` (immutable, published) → `FactoryRuntimeAssembler` (deterministic runtime instantiation). See [Factory Design](factory-design.md#4-canonical-model-boundary) for the boundary this implements. `FactoryModel` holds the required production records and an explicit optional spatial record; the current Engine executes production records only, so `FactoryRuntimeAssembler` refuses a published model whose spatial record is present before any runtime state exists.

`FactoryModelVersion.fingerprint()` implements the current canonical form specified by the [Factory model](factory-model.md): one aggregate `ModelFingerprint` rendered as `factory-model:sha256:<digest>` over a language-independent binary encoding of the production records and the optional spatial record, pinned by golden vectors for the current definition. Equal canonical content has an equal fingerprint independent of process memory and implementation language under the same definition; the fingerprint does not identify the exact definition revision that produced it.

The supported runtime observation/event contract supplies opaque per-runtime `RunId` and the
source-model `ModelFingerprint` on `RuntimeObservation`. It does not currently invent an Engine
definition identifier; exact Engine-definition provenance remains a separately triggered concern.

`:types` provides the opaque UUIDv4 `ControlledRevisionId` value model, and `:governance` provides the immutable `ControlledRevision`, lineage, and recording-provenance values fixed by the [controlled revision contract](controlled-revisions.md). `ControlledRevisionAuthority` defines the acceptance/lookup/resolution boundary, and `accept(...)` returns the immutable accepted record after the authority establishes its `recordedAt` at the commit boundary rather than trusting the candidate's timestamp. The current `FileControlledRevisionAuthority` is a disposable development **proving store**: it declares that scope and the exact definition binding at its root, refuses to reopen under a changed definition build, never adopts or modifies a location it did not create, persists append-only revision records and semantic artifacts across process/reopen boundaries, rejects duplicate/rebound IDs, requires an already-accepted parent under the current `0..1` lineage policy, verifies the supplied canonical artifact reproduces the revision's `ModelFingerprint`, and atomically installs the revision record under process/filesystem locking. Resolution returns the accepted revision together with its exact semantic artifact; missing/corrupt metadata or artifacts, fingerprint mismatches and artifacts of unsupported definitions fail explicitly rather than falling back to current model state. No retained, commitment-bearing revision authority exists without an explicit stability/support declaration.

Here, *disposable* describes the declared retention/support scope, not volatile storage: the
proving store persists data to exercise revision acceptance, lineage, integrity, and reopen
behavior. Its definition-build binding is a conservative compatibility check, not a proof of
semantic equivalence; the current reset policy does not establish that all future migrations must
be forbidden.

The proving store reuses the current current Factory canonical bytes as its semantic artifact. `FactoryModelArtifact` strictly decodes and canonical-reencodes those bytes to reconstruct the exact `FactoryModelVersion`, while the Governance store remains artifact-policy-agnostic through `SemanticArtifactVerifier`. Distinct revisions may therefore share one `ModelFingerprint` and one artifact — including the `F1 -> F2 -> F1` rollback case — without becoming the same historical occurrence. The filesystem record layout and locking mechanics are replaceable adapter details, not a selected production persistence architecture. The Governance semantic change/impact capability provides the generic `ChangeSet`/`SemanticChange`/`ImpactScope` contract in `:governance`, and the factory-domain `FactoryModelSemanticComparator` implements `SemanticChangeExtractor` for current Factory artifacts, keyed on stable domain identity while still attributing a semantically significant top-level list reorder (semantic under the [Factory model](factory-model.md)) as a real change, and reporting any spatial-record addition, removal or change coarsely against the model's spatial-record entity. The requirements/assertions capability adds the generic `Requirement`/`Assertion`/`RequirementCatalogue` contract in `:governance`, whose `RequirementScope` matches directly against the `ImpactScope` seam. The conformance evaluation/findings capability adds the generic `ConformanceResult`/`ConformanceEvaluation`/`Finding` contract and the deterministic `ConformanceEvaluator` in `com.arcogine.governance.conformance`, which evaluates a `Requirement`/`Assertion` pair against a model fingerprint (and an optional, never-synthesized `ControlledRevisionId`) without introducing authorization or deployment concepts; evidence references, evidence use, and evidence-backed conformance follow the [Governance evidence contract](governance-evidence.md). Approval/authorization, deployment, external change-management relationships, labels/tags/branches, and multi-parent merge semantics remain later Governance concerns, separate from revision identity.

## Outward Adapters

Arcogine currently has no outward adapter: no HTTP API, no CLI, no UI. See [Event Dispatch Architecture](#event-dispatch-architecture) for the current internal handler boundary and [Domain observations vs. outward projections/DTOs](#domain-observations-vs-outward-projectionsdtos) for the boundary it must maintain. A future consumer is introduced from the supported [runtime contract](runtime-contract.md) when a concrete product need exists, not spun up preemptively.

## Technology Stack

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Language | Java (release 21 compatibility baseline) | Records, sealed types, pattern matching |
| Build | Gradle (Kotlin DSL, repository wrapper) | Multi-module build; exact version pinned by `product/gradle/wrapper/gradle-wrapper.properties` |
| Testing | JUnit 6 | Unit and acceptance tests |
| Coverage | JaCoCo | Code coverage reporting |
