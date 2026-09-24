# Transfer lifecycle independence — research report

## Header / authority

- **Title:** Is the inter-resource transfer lifecycle independent of spatial layout?
- **Research status:** `ACTIVE`. A completed report exists, but the question is not `CONCLUDED`: independent adversarial review and durable reconciliation have not happened.
- **Research baseline:** live `main` at `07bf95cb14cc446fe02a12c88c9526a7ff205647`. The handoff was written against `397d1e989cb0e9d9a3167887294b410c1836cee3`. The only change between the two is a two-file process-documentation edit (`AGENTS.md`, `.github/CONTRIBUTING.md`). It does not touch any Factory, Engine, planning, research or product surface, so it has no effect on this investigation. The workspace branch was brought up to date with a history-preserving merge; the handoff commit is still reachable.
- **Authority statement:** this is research evidence only. It is not accepted architecture, product direction or implementation commitment. [Factory Model v2](../../../docs/architecture/factory-model-v2.md), [Engine Semantics v1](../../../docs/architecture/engine-semantics-v1.md), the [Factory semantic-evolution contract](../../../docs/architecture/factory-design.md#111-semantic-evolution) and the [Determinism Contract](../../../docs/architecture/overview.md#determinism-contract) remain authoritative until a separate, independently reviewed reconciliation changes them.
- **Risk classification:** **High.** This concerns a complete, result-affecting Engine interpretation. It also bears on whether the unreleased Factory V2 boundary must be reopened, and it touches historical attribution of V1 results.
- **Adversarial-review status:** **required, not yet performed.** The only adversarial pass in this revision is the author's own challenge in §Adversarial analysis.

> **Independent adversarial review is required before architecture/specification reconciliation.** Nothing here is decision-quality evidence for a durable Factory or Engine change until a fresh, independent Researcher session has reviewed this exact report revision.

## Question

Take work that finishes an operation on one configured productive resource and runs its next step on a **different** configured productive resource. Is the transfer (hand-off) an Engine execution phase that exists whether or not the published Factory model includes spatial layout? Or should the transfer lifecycle exist only when the model explicitly represents facts that affect transfer?

**Sharpening.** The brief sets "independent of spatial layout" against "only when transfer-affecting facts are represented". Evidence shows that, **within the current closed V2 grammar**, those two ideas pick out the same set of artifacts. The only transfer-affecting facts V2 can represent (`ticksPerCell`, `handlingTicks`) sit inside the optional spatial record. V2 deliberately has no geometry-only or handling-only form. So the useful question is not "does geometry cause transfer?" It splits into three:

1. Are *no transfer*, *zero-duration transfer* and *refusal* genuinely different semantics?
2. For an artifact that authors no transfer-affecting fact, which of those three is the truthful interpretation?
3. Which owner holds the rule, and how must it be stated so it doesn't get read as "geometry decides"?

## Decision at stake

1. Can Arcogine keep "spatial record absent ⇒ no transfer lifecycle" for V2? Or must binding, reservation, state and events get a non-spatial interpretation before V2 is published and spatial runtime is activated?
2. Must the still-unreleased V2 grammar be corrected before it is attributed?
3. Which exact represented-content meanings can the successor Engine-applicability question take as its input?

## Scope and non-goals

**In scope** (from the brief): distinct-resource and same-resource continuation; destination selection and binding timing; whether in-flight state exists without spatial facts; absent versus zero versus refusal; reservation and arrival only as far as a transfer phase changes them; ownership of the discriminator; result, provenance, identity and compatibility consequences.

**Out of scope, not decided here:** distance metric; paths, conveyors, transport resources, buffers, congestion, routing graphs; how positive non-spatial duration is represented (the separate `CANDIDATE` timing question); transport ontology; dispatch, ranking or sequencing changes; presentation and animation; and **the successor Engine-applicability question itself**. In particular, this report does not decide which Engine identity executes any V2 case.

## Executive conclusion

1. **No transfer, zero-duration transfer and refusal are three different result-affecting semantics.** A zero-duration transfer is *not* equivalent to no transfer, even though completion timing is the same in isolation. Under the fixed Engine v1 rules, a zero-duration transfer creates a separately scheduled same-time turn and a reservation-only window. That window changes (a) whether an external `setMachineAvailability(destination, offline)` command is accepted or rejected, (b) where bounded advancement stops, and (c) the supported event stream and observation (witnesses W1–W3). The choice between them therefore passes the Engine v1 §1.1 membership test and must be in the identified Engine interpretation. It must never be left ambient. *Confidence: high* (checked against specification text and current source).

2. **Surviving rule — Candidate 3a, "authored-transfer-fact-conditioned lifecycle, explicit no-transfer on absence":**
   - For a distinct selected destination, a transfer phase exists **if and only if** the executed artifact authors the transfer-affecting facts from which the identified Engine interpretation derives an inter-resource transfer interval.
   - When no such facts are authored, distinct-resource continuation runs with **no transfer phase**, as an **explicit Engine rule**: no binding separate from processing start, no reservation, no `TRANSFERRING` state, no `TRANSFER_*` events. That is the truthful execution of a design written at production-routing abstraction, not a zero default.
   - Same-resource continuation never transfers.
   - Refusal is reserved for **partially authored** transfer facts (already non-publishable draft state under V2), never for their total absence.
   - *Confidence: medium-high.*

3. **In the current V2 grammar, Candidate 3a has the same extension as the current coupling (Candidate 1).** Under V2 the spatial record is the only carrier of authored transfer-affecting facts. What fails in Candidate 1 is its *formulation*, not the artifacts it covers:
   - it names geometry as the discriminator;
   - it is left implicit in the Engine definition. Engine v1 §8 is written unconditionally for any distinct destination, §7's formula needs positions, and v1 never states the absence rule. So two conforming implementations could disagree about a spatial-absent artifact.

   **Answer to the decision at stake:** Arcogine may keep "no spatial record ⇒ no transfer lifecycle" as the *current extensional outcome*. But the rule must be restated and recorded as an Engine interpretation rule keyed on authored transfer-affecting facts, with absence semantics explicit, before V2 publication or spatial activation.

4. **Candidate 2 ("always a lifecycle, zero-duration fallback") is rejected** under current durable rules:
   - Its absence case works by defaulting the missing transfer inputs to zero, so that the transfer interpretation can apply to content that lacks them. The Determinism Contract rule 3 and the Factory evolution contract (§11.1) forbid exactly that.
   - It can be adopted only through an explicit architecture change to those rules, plus a distinguishable Engine identity. It cannot be applied to V1 artifacts under `engine-semantics:v1` without silently changing historical meaning.
   - Its chief practical appeal — a uniform event vocabulary whatever the authored model — is consumer presentation, which may not drive Engine interpretation.
   - *Rejection confidence: medium.* One reasoning step is contestable; see §Adversarial analysis A2.

5. **Candidate 3b ("explicit applicability required; refuse on absence") is rejected** for production-only designs. It would make every multi-resource V1 or V2-absent design unexecutable unless the designer invents floor, placement and zero handling facts. That is the "production-only model acquires invented authored facts" failure.

6. **No correction to the unreleased V2 grammar is needed for lifecycle reasons.** V2 §1.1 already says absence makes "no spatial, layout or handling assertion". It also already defines `handlingTicks` as "applied once per inter-resource transfer", so a present record presupposes inter-resource transfers. At most, editorial clarification might help (see §Durable consequences).

7. **Ownership.** Whether transfer exists is decided by an **Engine interpretation rule** whose condition is an **authored Factory fact** (whether transfer-affecting facts are present), applied to a **runtime outcome** (whether the selected destination differs from the source). Factory owns the facts; Engine owns the rule and its absence semantics; runtime owns the selection result.

8. **Identity consequences are handed to the successor question, not decided here.** For V1 artifacts under v1, the explicit no-transfer rule is behavior already executed and pinned by conformance fixtures, so writing it down *appears* to be a §1.1(3) correction (recording a previously unwritten rule without changing behavior). Whether any fixed definition admits V2-absent or V2-present artifacts stays with the successor question.

## Repository evidence

### Durable authorities

| # | Repository fact | Source |
|---|---|---|
| F1 | A V2 design has required production records plus one optional spatial record that is either absent or present and complete. Absence means "no spatial, layout or handling assertion: no floor, position, footprint, `ticksPerCell` or `handlingTicks` exists for it, and none is synthesized from a default". A present record with zero magnitudes is a different authored design. The record has "no geometry-only or handling-only form". | [Factory Model v2 §1.1](../../../docs/architecture/factory-model-v2.md) |
| F2 | `handlingTicks` is the "authored fixed overhead applied once per inter-resource transfer"; `ticksPerCell` is the "authored material-handling rate magnitude". | Factory Model v2 §1.1 table |
| F3 | Factory owns authored facts. "Arcogine's choice of distance metric, rounding, zero-distance behavior, destination binding, reservation and transfer lifecycle belongs to Engine semantics." | Factory Model v2 §1.1; [Factory design §11.1](../../../docs/architecture/factory-design.md#111-semantic-evolution) |
| F4 | "A V1 model has no spatial facts and therefore no spatial behavior — the truthful execution of a design that never authored spatial semantics, not a degraded mode." | Factory design §11.1 |
| F5 | "An Engine refuses an artifact outside that domain before runtime mutation rather than ignoring represented content or supplying absent content." | Factory design §11.1 |
| F6 | "Authored facts are never synthesized to make an interpretation applicable. Publication validity never establishes applicability." | [Determinism Contract rule 3](../../../docs/architecture/overview.md#determinism-contract) |
| F7 | A result-affecting change for identical explicit inputs is a new Engine identity; repairing nonconformance is not. Attribution fixes the whole definition, including unexercised rules. | Determinism Contract rule 4; [Semantic evolution rules 1–2](../../../docs/architecture/overview.md#semantic-evolution-and-support) |
| F8 | Engine v1 transfer rules are stated for "distinct source and destination resources" (§7) and in the §8 state machine ("if selected destination != source and can accept → reserve … `TRANSFER_STARTED`"). **Neither is conditioned in text on the spatial record being present.** §5 names the V2 spatial facts as the consumed facts, and the §7 formula needs positions. | [Engine Semantics v1 §5, §7, §8](../../../docs/architecture/engine-semantics-v1.md) |
| F9 | Engine v1 never states which Factory policies or represented content it admits. It also never states that distinct-resource continuation of a V1 (or other spatially silent) artifact has no transfer. §1.1(4): a rule that meets the membership test but is missing from the document is "a defect in this document". §1.1(3): recording a previously unwritten rule with unchanged behavior is a correction. | Engine Semantics v1 §1.1 (searched for `V1`, `factory-model:v1`, "without spatial", "pre-spatial") |
| F10 | Under v1, zero magnitudes still produce transfer start and completion (§7.9). A zero-duration transfer "is still a scheduled completion turn" (§12). A destination holding only inbound reservations "may still be taken offline" (§9.6). Arrival at an offline destination converts the reservation into a queue entry on the bound destination (§9.8). The reservation is neither active processing nor queued work (§9.2, §9.4). | Engine Semantics v1 |
| F11 | Same-resource continuation: "no transfer state, no duration, and no transfer events". | Engine Semantics v1 §7.8 |
| F12 | Session semantics (`advance()` is one event; `advanceUntil` loops over it with an event-count bound; command rejection happens before any mutation) are part of v1's result-affecting interpretation. | Engine Semantics v1 §1.2 |
| F13 | Canonical-model facts include "processing/setup/transfer/dispatch policies when part of the designed system" and "behaviorally relevant … transfer relationships". Transfers in progress are runtime state. | Factory design §4.1, §3.1 |

### Non-normative retained rationale

- The [Factory composition decision record](../../../docs/history/decisions/2026-09-23-factory-model-semantic-composition.md) says: "Engine Semantics v1 already made absence and authored zero result-affecting: a spatial design with zero handling magnitudes still produces transfer events, while a design without spatial facts produces none." This is history, not authority. It shows the coupling was an *inherited premise* of that decision, not something it tested.

### Research and planning state (verified)

| Claim in the handoff | Verified state at baseline |
|---|---|
| `factory-model:v2` unreleased | Yes — V2 §10: no publication path, verifier, decoder or support declaration |
| V2 optional-record shape/validation landed | Yes — `FactoryModelV2`, `SpatialRecord`, `FactoryModelV2Validator` and tests |
| V2 canonical identity/publication blocked | Yes — the spatial-runtime plan's V2 canonical-identity slice is dependency-blocked on this question and then on the successor question |
| Spatial/transfer runtime activation blocked | Yes — the arithmetic, reservation, activation and edge slices are held |
| `engine-semantics:v1` fixed; pre-spatial conformance landed | Yes — `EngineSemanticsVersion.CURRENT`, the conformance tests, the register and the successor brief |
| Previous applicability investigation `SUPERSEDED` | Yes — [retained predecessor](../../../docs/research/investigations/engine-applicability-optional-record-factory-policies.md) |
| Successor applicability `CANDIDATE` | Yes — [successor brief](../../../docs/research/investigations/engine-applicability-after-transfer-boundary.md) |
| Non-spatial timing `CANDIDATE` | Yes — [transfer-semantics brief](../../../docs/research/investigations/transfer-semantics.md#candidate--non-spatial-transfer-timing) |

### Implementation and executable evidence

| # | Repository fact | Source |
|---|---|---|
| I1 | **No transfer runtime exists.** Searching all product Java for `transfer`, `handlingTicks`, `ticksPerCell`, `TRANSFERRING` and `reservation` finds only V2 model/validator code and tests, plus one Challenge Javadoc that disclaims transfers. `RuntimeEventType` has no `TRANSFER_*` value. | `product/**/*.java`; `process/RuntimeEventType.java` |
| I2 | Distinct-resource continuation today: `FactoryHandler.handleTaskEnd` releases the source, selects the next machine, and **starts the next step in the same handler turn** when the machine can accept. Otherwise the job goes to the single-machine queue or the multi-eligible backlog. There is no binding step separate from start, and no reservation. | `process/FactoryHandler.java` `handleTaskEnd` |
| I3 | `FactoryRuntime.advance()` processes exactly one scheduled event. External commands run between `advance()` calls at the current `SimTime`. | `process/FactoryRuntime.java` `advance`, `advanceUntil` |
| I4 | `setMachineAvailability(id, false)` is **rejected before any mutation** when the machine has active jobs. | `FactoryRuntime.setMachineAvailability` |
| I5 | The next-step start is reported as `JOB_DISPATCHED` in the same turn as `JOB_STEP_COMPLETED`, by diffing placement. | `FactoryRuntime.recordSupportedEventsFor`, `emitPlacementChanges` |
| I6 | `FactoryModelV2` shares no supertype with `FactoryModel`. The runtime cannot receive V2 content, and `EngineSemanticsVersion` supports exactly one value, `engine-semantics:v1`. | `model/v2/FactoryModelV2.java`, `types/EngineSemanticsVersion.java` |
| I7 | v1 conformance fixtures pin completion vectors for **V1 models with distinct-resource routings and no transfer phase**. Examples: the downstream unary-bottleneck case (eight jobs M1→M2 complete at 810); the routed recovery regression `[14,29,29]`; the canonical overlap `[106,6,105]`. | `EngineSemanticsV1DispatchConformanceTest` |

**Implementation status:** transfer state, events and reservation are **specification- and planning-only**. The normative text naming `TRANSFER_STARTED`/`TRANSFERRING`/`TRANSFER_COMPLETED` does not establish implementation.

### Inventory of contracts that depend on the present coupling

| Contract | How it depends on "spatial absent ⇒ no transfer" |
|---|---|
| V2 §1.1 optional record and absence semantics | Presence bundles geometry and handling, so presence is today the only way to author transfer-affecting facts |
| V2 maximum-transfer representability predicate | Defined only over a present record — only a present record can yield a transfer interval |
| Engine v1 §2 rule 10, §6–§9, §12, §14 fixtures 7, 9–11, 13 | Presuppose spatial facts; unconditional in text, not in inputs (F8/F9) |
| Engine v1 §7.9 (zero magnitudes still expose transitions) | Makes "authored zero ≠ absence" result-affecting |
| Factory design §11.1 (V1 ⇒ no spatial behavior) | The only durable statement of an absence rule; it names *spatial* behavior, not *transfer* |
| Retained v1 conformance fixtures (I7) | Pin V1 multi-resource outcomes with no transfer phase |
| Spatial-runtime plan (V2 identity; transfer arithmetic, reservation, activation, edge, closure slices) | Held pending this question; the closure scenario item "neither a V1 design nor a V2 design without a spatial record receives synthesized spatial semantics" |
| Composition decision rationale | Cites absent ≠ zero as already result-affecting |
| Successor applicability brief | Waits for this result's case meanings |

## Candidate models / hypotheses

Each candidate is stated with its absence semantics explicit.

- **C1 — current coupling, as currently written.** A transfer phase exists only when the spatial record is present. When it is absent, distinct-resource continuation proceeds with no transfer. The rule is *implicit*: Engine v1 never states it, and Factory states it only as "no spatial behavior" for V1.
- **C2 — lifecycle independent, zero-duration fallback.** Every distinct selected destination gets binding, reservation, `TRANSFERRING` and events. Without authored timing facts the duration is zero. Same-resource never transfers.
- **C3a — lifecycle conditioned on authored transfer-affecting facts, explicit no-transfer on absence.** Geometry as such is not the discriminator. A transfer phase exists for a distinct selected destination iff the artifact authors the facts from which the Engine derives an inter-resource interval (under V2 today: the present spatial record; possibly, in future, a separately admitted non-spatial timing record). When no such fact exists, an **explicit Engine rule** says no transfer. Partial facts are refused (already Factory-invalid or draft under V2).
- **C3b — lifecycle conditioned on authored transfer facts, refusal on absence.** As C3a, but an artifact with distinct-resource routing and no transfer facts is Engine-inapplicable and refused before mutation.
- **C4 (added and tested) — explicit authored "hand-off modeled" flag, separate from timing.** Factory authors a boolean that turns the lifecycle on, independent of any timing fact. It was added because it is the one materially different way to make existence a Factory fact rather than an Engine rule.

C1 and C3a differ only in formulation until the grammar admits geometry without handling, or a non-spatial transfer-timing record. That is itself a finding: see §Proving cases, cases 2 and 9.

## External evidence

External evidence is not decisive here. The question is mainly about Arcogine's own ownership, identity and absence rules, so it was used only to test whether zero-time transfer is conventionally an *authored modeling choice* or an *ambient default*.

| Source | Establishes | Where the analogy breaks | Verification |
|---|---|---|---|
| *Simio and Simulation: Modeling, Analysis, Applications*, 7th ed., ch. 4 "First Model", Table 4.1 ([textbook.simio.com/SASMAA7/ch-first-model.html](https://textbook.simio.com/SASMAA7/ch-first-model.html)) | A mature DES product treats the choice between a *Connector* ("a simple zero-time travel link") and a *Path* (timed, speed-dependent movement) as an **explicitly authored modeling choice**. Entities flow between objects only across links the modeler has explicitly placed. Zero-time transfer is therefore a *stated* property of the model, not an Engine default for unstated handling. | Simio models flow topology explicitly; Arcogine routes through operation eligible-resource sets and has no link concept. In Simio an entity crosses a Connector even at zero time, so Simio has no "no-transfer" mode other than not connecting at all (which prevents flow). It therefore does *not* show that "no transfer phase" is a common option. It supports only the narrower claim that zero-time hand-off is authored content distinct from unstated handling. | Fetched and checked during this investigation (7th-edition online text) |
| Job-shop scheduling literature with transportation times (e.g. Hurink & Knust on job-shops with a transport robot; surveys of job-shop scheduling with transportation resources) | Background claim: the classical job-shop abstraction omits transport entirely, and transport times or resources are an explicit extension, not a default zero phase. That would support "no transfer" as a truthful abstraction level. | Scheduling models have no event, state or command-interleaving semantics, so they cannot tell no-transfer from zero-duration-transfer — the distinction that matters here. | **Unverified background.** A search found only secondary pages and paywalled chapters; the primary text was not checked. **Not load-bearing.** |

External evidence therefore establishes *possibility* — authored zero-time transfer is a recognized, explicit modeling choice. It does not establish that Arcogine *must* adopt C3a.

## Proving cases

### Discriminating witnesses: no transfer versus zero-duration transfer

These rest on the fixed v1 rules (F10–F12) and current implementation behavior (I2–I5). Model: resources M1 and M2, both unary and online. One product routes `M1:5 → M2:3`. Workload of one. Step 1 ends at t=5.

- **W1 — command acceptance.** Advance once, processing the `TaskEnd` at t=5. Then issue `setMachineAvailability(M2, offline)` at t=5.
  - *No transfer:* the job is already active on M2 (I2), so the command is **Rejected** with no mutation (I4). The job completes at t=8.
  - *Zero-duration transfer:* M2 holds only an inbound reservation, so the command is **Accepted** (F10, §9.6). `TRANSFER_COMPLETED` then fires at t=5 with M2 offline, and the job enters M2's own queue with `JOB_WAITING`. Completion depends on when M2 comes back online.
  - Acceptance, terminal state and completion time differ for identical model, workload and ordered commands.
- **W2 — bounded advancement.** `advanceUntil(t=5, maxEvents=1)` right after step 1 ends.
  - *No transfer:* one event leaves the job processing on M2.
  - *Zero-duration transfer:* the job is left `TRANSFERRING`, with a pending same-time completion turn.
  - The runtime state visible at that session boundary differs (F12).
- **W3 — supported stream and observation.**
  - *No transfer:* `JOB_STEP_COMPLETED → JOB_DISPATCHED` in one turn (I5).
  - *Zero-duration transfer:* `JOB_STEP_COMPLETED → TRANSFER_STARTED → TRANSFER_COMPLETED → JOB_DISPATCHED` (F10, §12). Sequence numbers, event counts and in-flight observation facts differ.

Completion time in isolation is equal (t=8 in both). That is why "zero duration" is easy to confuse with "no transfer". These witnesses show the difference passes the §1.1 membership test.

### Candidate-by-case matrix

Notation for each cell: **exists?** / **decided by** / **owner** / **binding** / **reservation** / **absent-fact behavior** / **state/events** / **provenance/identity**.

**Case 1 — `M1 → M1` (the selected destination is the source).**

- **C1, C2, C3a, C3b, C4:** No transfer. Decided by the Engine rule that same-resource continuation never transfers (F11), applied to the runtime selection outcome.
  - Binding: none separate from processing start.
  - Reservation: none.
  - Absent facts are irrelevant.
  - State/events: `TRANSFERRING` and `TRANSFER_*` are forbidden.
  - Identity: unchanged from v1.
- **Discriminating power: none.** This is the control case. Surviving it proves nothing about distinct-resource applicability. Note that with a multi-eligible step, "same resource" is a *runtime selection* outcome, not an authored fact.

**Case 2 — `M1 → M2`, no spatial record, no other authored transfer facts (V2 spatial-absent).**

- **C1:** No transfer. Decided by "spatial record absent", which is an implicit rule.
  - The owner is ambiguous: Factory §11.1 speaks only of V1 "spatial behavior", and Engine v1 is silent (F9).
  - Binding coincides with processing start; there is no reservation.
  - State/events: `TRANSFER_*` forbidden, `JOB_DISPATCHED` in the same turn.
  - **Fails as written:** Engine v1 §8 is unconditional while §7 is undefined without positions. One implementation could read "no transfer" and another "transfer with undefined or zero duration". So two conforming implementations can disagree about whether a transfer state or event exists.
- **C2:** Transfer exists with duration 0. Decided by the rule "distinct destination ⇒ transfer", with an Engine default for the missing timing inputs.
  - Owner: Engine.
  - Binding at admissibility; one reservation unit held for one same-time turn.
  - Absent facts are defaulted to zero duration.
  - State/events: `TRANSFER_STARTED`/`TRANSFER_COMPLETED` required, W1–W3 behavior applies.
  - Identity: needs a distinguishable Engine identity and an architecture change (see §Adversarial analysis A2).
- **C3a:** No transfer. Decided by the explicit Engine rule "no authored transfer-affecting facts ⇒ no transfer phase".
  - Owner: Engine rule, conditioned on Factory's absence.
  - Binding coincides with start (current I2 behavior); no reservation.
  - Absent facts give explicit no-transfer. Nothing is synthesized.
  - State/events: `TRANSFER_*` and `TRANSFERRING` forbidden, `JOB_DISPATCHED` in the same turn.
  - Identity: production outcome equals the V1 production interpretation. The V2 fingerprint stays distinct. Which Engine identity executes it is left to the successor question.
- **C3b:** Artifact refused before mutation (Engine-inapplicable).
  - The absent-fact result is refusal. Nothing runs.
  - **Fails:** executing a production-only design would require authoring floor, placement and zero handling.
- **C4:** Transfer exists iff the flag is set. With the flag unset, as in C3a. With the flag set and no timing, the result is undefined — refuse, or default to zero as in C2.
  - **Fails:** the flag adds no information. "Hand-off modeled with zero time" is already expressible as present zero magnitudes, and flag-set-without-timing reintroduces C2's defaulting.

**Case 3 — `M1 → M2`, present record with authored zero (`ticksPerCell = 0`, `handlingTicks = 0`).**

- **C1, C3a, C3b:** Transfer exists with duration 0. Decided by the presence of authored transfer facts; the value comes from the v1 formula.
  - Owner: Factory authors the facts; Engine applies binding, arithmetic and lifecycle.
  - Binding when the selected destination is admissible (v1 §6); reservation from binding to the same-time arrival (v1 §9.2).
  - State/events: `TRANSFER_STARTED → TRANSFER_COMPLETED` on a separate same-time turn (v1 §12). `TRANSFERRING` is observable between turns.
  - Identity: distinct V2 fingerprint from case 2. The absent/zero distinction is behaviorally meaningful (W1–W3).
- **C2:** Same as C1 — but the outcome is **identical to case 2**. Authored zero and absence become behaviorally indistinguishable; the distinction survives only in the fingerprint.
- **C4:** As C1, if the flag is also set.

**Case 4 — `M1 → M2`, positive timing facts.**

- **All surviving candidates:** Transfer exists with a positive duration. The value is fixed at binding (v1 §6.6, §7).
  - Owner: Factory facts plus the Engine formula.
  - Binding when the destination is admissible; reservation held for the interval.
  - State/events: the full v1 lifecycle; late observation must reconstruct `TRANSFERRING` (v1 §11).
  - Identity: v1 already specifies this behavior. Whether v1 may *admit* V2 artifacts is left to the successor question.
- **Discriminating power: none** between candidates. It is the common ground.

**Case 5 — multi-eligible next step, destination bound before an interval.**

- **With transfer facts (C1, C2, C3a, C3b):** selection and ranking unchanged (v1 §2). The job binds when the selected destination can accept, and the effective eligible set collapses to the bound destination (v1 §6.9). This applies equally to a zero or positive interval. The binding point is the same for zero and positive: both bind at admissibility, before the interval.
- **Without transfer facts:**
  - *C1 / C3a:* no binding step exists. Selection plus immediate start *is* placement. A job that cannot be placed stays unbound in the backlog and may reselect (I2, fixtures in I7).
  - *C2:* the job binds and reserves even in the absent case. The zero-length window becomes observable and command-sensitive (W1 generalizes).
  - *C3b:* refused.
- **Case 5 therefore separates C2 from C1/C3a** in the absent case only.

**Case 6 — destination availability or capacity changes between binding and arrival, non-zero interval.**

- **With positive transfer facts:** v1 §9 applies. The binding is immutable; there is no rerouting; the destination may go offline while holding only a reservation. On arrival at an offline destination, the reservation converts into an entry in the bound destination's own queue with `JOB_WAITING`. Another eligible machine becoming preferable changes nothing.
- **Without transfer facts:**
  - *C1 / C3a:* **not reachable** — there is no interval. The analogous availability change is decided by the ordinary rules: an active job blocks offline (I4); a waiting unbound job reselects.
  - *C2:* reachable only as the zero-length window of W1.
- **Consequence for the reservation recheck the spatial-runtime plan requires before activation:** under C3a that recheck is triggered only for artifacts with transfer facts. Under C2 it would be triggered for every multi-resource run.

**Case 7 — deterministic same-time ordering when duration is zero.**

- **Present zero (C1, C3a, C3b, and C2 for present):** the v1 §12 chain. The completion turn is scheduled at the same `SimTime` and ordered by the existing insertion-order tie-break (v1 §4.2). It therefore runs *after* same-time work already scheduled. Supported ordering follows the supported-event sequence only. Rules are complete and deterministic.
- **Absent, C1 / C3a:** there is no second turn. Placement happens within the `TaskEnd` turn, before the source machine's queue and backlog cascade (I2). This is the v1-pinned ordering (fixtures in I7, "same-machine continuation precedence" and equal-time ordering).
- **Absent, C2:** the extra same-time turn applies to *every* distinct-resource continuation. Whether the reservation stops same-time competing `TaskEnd`s from taking the destination follows from v1 §9.2, so ordering is deterministic — but it differs from the pinned v1 outcomes under interleaved commands and bounded advancement.

**Case 8 — historical V1 and production-only artifacts that never authored spatial facts.**

- **C1 / C3a:** no transfer. This matches the executed and pinned v1 behavior (I2, I7) and Factory §11.1 (F4).
  - State/events: `TRANSFER_*` forbidden.
  - Identity: historical v1 results keep their meaning. Recording the rule in Engine v1 appears to be a correction (F9).
- **C2:**
  - Applied under v1, it changes outcomes for identical inputs (W1–W3), which violates F7. So it requires a new Engine identity.
  - Historical v1 results must remain no-transfer.
  - If the successor also admits V1 artifacts, the same V1 artifact behaves differently under v1 and the successor. If the successor excludes V1, then V1 and V2-absent — which make *the same* (empty) handling assertion — behave differently, and the **Factory policy label, not any authored fact, discriminates**.
- **C3b:** V1 artifacts are refused by any C3b identity. v1 would continue to execute them, so the support partition would be driven by missing geometry.

**Case 9 (added) — a hypothetical future grammar with geometry but no handling magnitudes, or with a non-spatial timing record only.**

- **C1:** its geometry-keyed formulation would switch on transfer for geometry without any timing inputs, and would *not* switch it on for a non-spatial timing record. Both are wrong under the ownership split.
- **C3a:** determined by whether the grammar supplies the facts the Engine needs for an interval. Geometry alone does not; a non-spatial timing record does.
- This case is not reachable under V2 today. It is included because it is the only case that separates the C1 and C3a formulations, and it corresponds to Factory composition reopening trigger 5 and to the `CANDIDATE` timing question.

### Case-by-candidate summary

| Case | C1 (implicit) | C2 | C3a | C3b | C4 |
|---|---|---|---|---|---|
| 1 same resource | ✔ | ✔ | ✔ | ✔ | ✔ |
| 2 distinct, absent | ✘ (implicit; implementations may disagree) | ✘ (defaults absent inputs) | ✔ | ✘ (forces invented facts) | ✘ (redundant; C2 defaulting) |
| 3 authored zero | ✔ | ~ (collapses zero into absent) | ✔ | ✔ | ✔ |
| 4 positive | ✔ | ✔ | ✔ | ✔ | ✔ |
| 5 multi-eligible binding | ✔ | ~ | ✔ | ✘ in absent case | ~ |
| 6 change during interval | ✔ | ✔ with facts; widens recheck | ✔ | ✔ with facts | ✔ |
| 7 zero-duration ordering | ✔ with facts; absent implicit | ✔ but changes pinned absent ordering | ✔ | ✔ with facts | ✔ |
| 8 historical V1 | ✔ in outcome, implicit in text | ✘ under v1; policy-label discriminator | ✔ | ✘ | ~ |
| 9 future grammar | ✘ (geometry-keyed) | n/a | ✔ | ✔ | ~ |

Key: ✔ survives; ~ survives with a material weakness; ✘ fails a falsification rule.

## Adversarial analysis

This is the **author's self-challenge only**. It is not an independent adversarial review and does not carry that weight.

- **A1 — "C3a is just C1 renamed; the brief's worry is unfounded."**
  - Partly true, and the report says so plainly: in the current grammar the two cover the same artifacts.
  - The difference is not cosmetic, though. (i) The current rule is not written in the Engine definition, and its textual rules (F8) point the other way, so the implementation-disagreement falsifier actually fires against C1 as written. (ii) The geometry-keyed formulation would give the wrong answer once case 9 becomes reachable. (iii) The ownership of the absence rule has to be assigned.
  - The brief was right to challenge the premise. The result is that the premise's *extension* survives while its *statement* does not.
- **A2 — "C2's zero default is an Engine rule, not a synthesized fact; C1/C3a equally 'supplies' zero delay."**
  - This is the strongest objection and the one contestable step behind rejecting C2.
  - Reply: C3a applies the *production* interpretation, which needs no transfer inputs; nothing is supplied. C2 applies the *transfer* interpretation, whose defined inputs (F2, v1 §7) are absent, and fills in a value so that interpretation can apply. That matches the "supplying absent content" and "never synthesized to make an interpretation applicable" wording (F5, F6).
  - A reviewer could argue F5/F6 concern only *authored facts*, and an Engine may define any absent-case duration it likes. If that reading prevails, C2 is not falsified. It is still dominated, because it needs a new Engine identity and turns the Factory policy label into a discriminator (case 8). Its main benefit is presentation uniformity, and it makes authored zero behaviorally redundant (case 3). Its absent-case transfer phase has no authored basis and no timing effect, yet it changes command acceptance (W1).
  - *Effect on the conclusion:* C3a's *preference* survives either way. C2's status is "falsified under the current wording, or dominated under the alternative reading". Its rejection confidence is therefore **medium**, not high.
- **A3 — "Real plants always hand off between machines, so C2 is more physically truthful."**
  - Zero-time hand-off is itself a modeling assertion — that hand-off is instantaneous — not an observation. It adds only event structure. A design written at routing abstraction truthfully asserts nothing about hand-off (F1, F4).
  - External precedent (Simio) treats zero-time transfer as authored.
  - This argument does not tell the candidates apart on truthfulness.
- **A4 — "Consumers such as the game want one event vocabulary."**
  - That is presentation, and it cannot drive Engine interpretation. A consumer may still show a visual hand-off derived from `JOB_STEP_COMPLETED`/`JOB_DISPATCHED`, as v1 §11 already allows for interpolation.
- **A5 — hidden assumption: that v1 is fixed (attributed).**
  - The repository treats it as fixed (register, successor brief, F7), but the report did not independently establish that any retained record is attributed to it.
  - If v1 were still correctable, the *mechanics* of recording the absence rule would change (correction versus identity). The *meaning* would not: C3a's semantics for V1 artifacts equals current behavior either way.
- **A6 — possibility treated as necessity?**
  - C3a is shown to be *consistent* with durable rules and current behavior, and C2/C3b/C4 are shown to fail or be dominated. C3a is not shown to be the *only* coherent Engine design. An explicit architecture change could legitimately adopt C2 under a new identity.
- **A7 — stale baseline.** Re-checked: `main` moved only in process documentation, which does not affect the conclusion.

## Surviving invariants

1. **I-A — Three distinct absence semantics.** "No transfer", "transfer with zero duration" and "refusal because required transfer facts are absent" are distinct result-affecting interpretations (W1–W3). Any Engine definition that executes distinct-resource continuation must state which one applies to each represented-content case.
2. **I-B — Lifecycle existence is keyed on authored transfer-affecting facts, not on geometry.** The Engine rule's condition is the presence of the authored facts from which it derives an inter-resource interval. Under V2 today, that is the present spatial record.
3. **I-C — Absence of all transfer-affecting facts means explicit no-transfer, not zero and not refusal.** Refusal applies only to partial or incomplete facts, which are already non-publishable under V2.
4. **I-D — Same-resource continuation never transfers.** "Same resource" is a runtime selection outcome, not an authored fact.
5. **I-E — Lifecycle existence and duration are separate decisions.** Once facts exist, zero is a legal duration with full lifecycle. A later non-spatial timing contract would turn the lifecycle on by being present; its representation is a separate question.

## Transferability and reuse

- **Arcogine-specific dependency:** Engine v1's reservation semantics (offline accepted with reservation-only), one-event `advance()`, and interleaving of external commands between same-time turns.
- **Potentially transferable result:** in a discrete-event engine where external commands can interleave between same-time scheduled turns, or where advancement is bounded by event count, a zero-duration phase is not outcome-equivalent to the absence of that phase. Equal completion time does not show semantic equivalence.
- **Boundary conditions:** this does not hold for an engine that processes all same-time events atomically and exposes no intermediate state, or with no command interleaving and no event-count bound.
- **Evidence level:** established for Arcogine by W1–W3 derived from specification and source. The broader claim is only a hypothesis.
- **Reusable research asset:** witnesses W1–W3 are ready-made deterministic conformance fixtures for whichever interpretation is reconciled.
- **Synthesis-seed candidate (nominated, not admitted):** "a zero-duration phase ≠ absent phase under command interleaving or bounded stepping". *Revisit when* a second Arcogine question (e.g. zero-duration setup, or zero-duration processing steps if ever admitted) or an external engine shows the same distinction.

## What did not survive

- **The framing "geometry decides whether transfer exists".** Under V2 geometry and handling are bundled. The discriminator that survives is the authored transfer-affecting fact.
- **"Zero duration is behaviorally the same as no transfer".** Falsified by W1–W3. This is reusable negative knowledge: a future proposal to "just use zero" as a neutral default should meet these witnesses.
- **C2 as a neutral fallback.** It is not neutral: it changes command acceptance and session semantics for every multi-resource production-only run.
- **C3b refusal on absence.** It forces invented layout.
- **C4 flag.** Redundant with authored zero magnitudes.
- **C1 left implicit.** Its text lets implementations disagree.
- **Note on the predecessor:** the superseded applicability investigation's *inherited premise* (spatial absence ⇒ no transfer) turns out to survive in extension, restated per I-B/I-C. That does **not** revive its conclusion. The successor must still derive Engine identity from the reconciled rule, and the predecessor's evidence remains inputs, not answers.

## Confidence and limitations

- **High:** W1–W3 and invariant I-A; the implementation-status inventory (I1–I7).
- **Medium-high:** C3a as the surviving rule and I-B/I-C/I-D.
- **Medium:** C2's rejection — see A2.
- **Limitations:**
  - No tests were run. W1–W3 are derived from the specification and a reading of the code, not from an executed fixture. Transfer is not implemented, so its side of each witness is necessarily specification-derived.
  - The report did not establish whether retained records are attributed to `engine-semantics:v1` (A5).
  - The scheduling-literature analogue is unverified background and not load-bearing.
  - No inventory was made of consumers outside the repository.
- **What would change the conclusion:**
  - an accepted reading of F5/F6 that permits Engine defaults for absent transfer inputs, plus a concrete Engine-owned need for a uniform lifecycle (would reopen C2);
  - a concrete consumer or plant model in which production-only designs must model hand-off commitment (reservation) without any authored timing;
  - evidence that v1's text was intended universally for V1 artifacts, and that current behavior is a nonconformance rather than the meaning.

## Unresolved unknowns

- Which Engine identity or identities may execute V1, V2-absent, V2-present-zero and V2-present-positive under I-A–I-E, and whether v1 can admit any V2 case without changing its whole definition. This belongs to the **successor question**.
- Whether writing I-C into Engine v1 for V1 artifacts is a §1.1(3) correction or needs another mechanism. A "correction" is the likely classification, but the successor question and reconciliation review own that decision.
- Positive non-spatial duration representation. This belongs to the **`CANDIDATE` timing question**, which may now be promoted under its own trigger if a consumer needs it.

## Durable consequences

Recommended; this report does not perform them.

1. **Engine architecture/specification** (the Engine definition that executes each case, reconciled together with the successor question):
   - state I-A–I-E explicitly;
   - in particular, the transfer lifecycle (current v1 §6–§9, §12) applies only when the executed artifact authors transfer-affecting facts;
   - with none authored, distinct-resource continuation has no transfer phase;
   - same-resource never transfers;
   - refusal applies only to partial facts.
   - Remove the textual unconditionality that F8 identifies.
2. **Factory evolution contract (§11.1)**, editorial: generalize "no spatial behavior" for V1 into "no authored transfer-affecting facts ⇒ no transfer phase under the Engine's explicit rule". Point to the owning Engine rule rather than implying Factory decides.
3. **Factory Model v2:** no grammar correction required. Optionally clarify that the spatial record is currently the only carrier of transfer-affecting facts, and that its absence carries no transfer phase *by the Engine's rule*, not by Factory meaning.
4. **Planning:** once reconciled, the spatial-runtime plan's held slices can be re-resolved against I-A–I-E (after the successor question). The reservation-aware dispatch recheck stays scoped to artifacts with transfer facts.
5. **Research register:** mark this question with its verdict after review and reconciliation. Re-promote the successor question with the handoff below. The timing question stays `CANDIDATE` until its own consumer trigger.
6. **Reusable assets to preserve:** W1–W3 as conformance-fixture specifications; the "zero ≠ absent phase" negative knowledge; the C2 objection record (A2). A historical decision-rationale record is likely warranted at reconciliation: two serious alternatives were live, and C2 is the obvious candidate to be re-proposed later without knowing the trade-offs. No need to retain the exact report.

## Implementation implication

No implementation is admitted by this report. Current code already behaves per C3a for the only artifacts it can execute (V1). Any future transfer activation must follow the reconciled rule and fixtures, not this report.

## Successor-research handoff

If this report survives independent adversarial review and durable reconciliation, the successor Engine-applicability question may take the following as settled:

1. **Distinct-resource transfer without spatial facts:** under the current V2 grammar, *no transfer phase exists* when no transfer-affecting fact is authored (V1 and V2-absent). The reason is an explicit Engine rule keyed on authored transfer facts — not an inference from missing geometry.
2. **Meanings:**
   - *no transfer* — no binding separate from start, no reservation, no `TRANSFERRING`, no `TRANSFER_*`, placement within the completion turn;
   - *zero-duration transfer* — full lifecycle, reservation, a separate same-time completion turn, with W1–W3 consequences, arising only from authored zero magnitudes;
   - *refusal* — only for partial or incomplete transfer facts, which are Factory-invalid or draft under V2 and therefore never published.
3. **Owner:** Engine owns the applicability rule and its absence semantics; Factory owns the facts; runtime owns same versus distinct selection.
4. **V2 grammar:** no correction required for lifecycle reasons.
5. **Represented-content cases the successor must evaluate:**
   - (a) V1 artifact;
   - (b) V2 spatial-absent: production-only interpretation with a distinct fingerprint, equal in production outcome to (a) but not to be re-attributed as (a);
   - (c) V2 spatial-present with authored zero magnitudes: zero-duration lifecycle;
   - (d) V2 spatial-present with positive magnitudes;
   - (e) same-resource continuation under (c) and (d);
   - (f) multi-eligible binding, and offline-at-arrival under (c) and (d);
   - (g) Factory-invalid present content;
   - (h) Factory-valid but Engine-unsupported content, or an unsupported identity.
   - Also: whether a hypothetical future transfer-fact carrier (case 9) is an explicit support question rather than wildcard acceptance.
6. **Not settled for the successor:** which identity executes (b)–(d); whether writing I-C into v1 is a correction; whether one identity or a partition is warranted.

## Follow-up triggers

- Independent adversarial review of this exact revision (required next).
- Reopen if a reading of F5/F6 permitting Engine defaults for absent transfer inputs is adopted; if a grammar admits geometry without handling or a non-spatial timing record (case 9); or if a concrete consumer needs hand-off commitment in production-only designs.
- Track follow-ups in the [research register](../../../docs/research/research-register.md).

## Sources

- Repository (baseline `07bf95cb14cc446fe02a12c88c9526a7ff205647`): the documents and source files cited in §Repository evidence.
- *Simio and Simulation: Modeling, Analysis, Applications*, 7th ed., ch. 4, Table 4.1 — <https://textbook.simio.com/SASMAA7/ch-first-model.html> (verified in session).
- Job-shop-with-transportation literature, e.g. Hurink, J., Knust, S. (2005) — unverified background, not load-bearing.
