# Transfer lifecycle independence — research report (revision 2)

## Header / authority

- **Title:** Is the inter-resource transfer lifecycle independent of spatial layout?
- **Revision:** 2. This revision replaces revision 1 (report commit `f36f7f535d42245b38b3db0ca8ad0333963edf79`, same path). It responds to the independent adversarial review at commit `b40c9a4ccdbaf8de813f0c16b467ccefd26df1d7`, path `workspace/research/investigations/transfer-lifecycle-independence-adversarial-review.md`, disposition **REOPEN**. That review binds only to revision 1. Nothing in it applies to this revision.
- **Research status:** `ACTIVE`. The question is not `CONCLUDED`: this revision has not been reviewed, and nothing has been reconciled.
- **Research baseline:** live `main` at `07bf95cb14cc446fe02a12c88c9526a7ff205647`. This is the same baseline as revision 1 and the review. It was re-resolved at the start of this revision; `main` had not moved. **Final recheck before persistence:** `main` had advanced to `812eff79ba017c6770633aeab74249cbe7a504d3`. The only new commit separates retrospective evidence acquisition and analysis, touching `infra/dev/delivery-retrospective*`, `docs/development/{continuous-improvement,reviewing,testing}.md`, `.github/agents/pr-reviewer.agent.md` and a tooling check script. It touches no Factory, Engine, research or planning surface and does not affect this report. The workspace branch was deliberately not re-synchronized for this revision.
- **Authority statement:** this is research evidence only. It is not accepted architecture, product direction or implementation commitment. [Factory Model v2](../../../docs/architecture/factory-model-v2.md), [Engine Semantics v1](../../../docs/architecture/engine-semantics-v1.md), the [Factory semantic-evolution contract](../../../docs/architecture/factory-design.md#111-semantic-evolution) and the [Determinism Contract](../../../docs/architecture/overview.md#determinism-contract) stay authoritative until a separate, independently reviewed reconciliation changes them.
- **Risk classification:** **High.** The question concerns a complete, result-affecting Engine interpretation. It bears on whether the unreleased Factory V2 grammar needs correcting, and it touches how V1 results are attributed.
- **Adversarial-review status:** **a new independent adversarial review is required.** The recommended *meaning* for the absent case is the same as in revision 1, but the reasons for it have changed. Revision 1 claimed to falsify the alternatives. This revision chooses by a stated trade-off criterion among alternatives that remain viable. Refusal has moved from the meaning layer to the support layer. The grammar conclusion changed from "no correction needed" to "no correction justified by current evidence". These are load-bearing changes, so the review of revision 1 does not carry over.

> **Independent adversarial review is required before architecture/specification reconciliation.** Nothing in this revision is decision-quality evidence for a durable Factory or Engine change until a fresh, independent Researcher session has reviewed this exact revision.

## Response to the adversarial review of revision 1

| Review item | Accepted? | Where this revision addresses it |
|---|---|---|
| Finding 1 — Candidate 2 is not falsified by the anti-synthesis rule | **Accepted.** The review's reading of F5/F6 is correct. The rules forbid synthesizing *authored* content. They do not forbid an Engine rule over the represented state "no transfer-timing facts authored". The same reading applies to Candidate 3a, which is also an Engine rule over represented absence. So the anti-synthesis rule does not separate any of the absence meanings. | §Candidate models (C2 restated); §Viability screen; §Decision criterion |
| Finding 2 — refusal is rejected by reasoning that contradicts the applicability boundary | **Accepted.** Refusing a valid published spatial-absent artifact is an Engine support-domain choice. It does not force a designer to invent facts. Revision 1's invariant "refusal applies only to partial facts" was vacuous, because partial V2 content is Factory-invalid and never reaches an Engine. | §Candidate models (C3b restated); §Support-requirement search; case 5 (refusal granularity); invariant I-C restated |
| Finding 3 — explicit applicability separate from timing was dismissed too narrowly | **Accepted.** Tested again in its strongest form, as C4-z and C4-t. It is not redundant with a present zero spatial record. The new evaluation finds that C4 adds a represented-content case but does not answer the absence question: an artifact without the C4 fact still needs a meaning from C2, C3a or C3b. It is deferred because nothing currently needs it and it would cost a grammar change, not because it is redundant. | §Candidate models (C4); cases 2, 3 and 8; §Decision criterion |
| Finding 4 — successor handoff overstates what is settled | **Accepted.** The handoff is narrowed. Recommended meanings are marked as trade-off choices. Refusal and support scope are handed on as open. | §Successor-research handoff |
| Required pass, item 4 — use a clear decision criterion | Done. | §Decision criterion among viable candidates |
| Required pass, item 5 — keep W1–W3 | Kept unchanged. | §Discriminating witnesses |
| Required pass, item 6 — no settled handoff until re-reviewed | Accepted. | Header; §Successor-research handoff |

The review's list of what survived is kept: the three semantics are distinct (W1–W3), transfer lifecycle is Engine-owned, same-resource continuation is the control case, and the implementation inventory is accurate. Each was re-checked against the unchanged baseline.

## Question

Take work that finishes an operation on one configured productive resource and runs its next step on a **different** configured productive resource. Is transfer (hand-off) an Engine execution phase that exists whether or not the published Factory model includes spatial layout? Or should the transfer lifecycle exist only when the model explicitly represents facts that affect transfer?

**Sharpening.** Under the current closed V2 grammar, the only transfer-affecting facts that can be represented (`ticksPerCell`, `handlingTicks`) sit inside the optional spatial record. V2 deliberately has no geometry-only or handling-only form (F1). So "independent of spatial layout" and "only when transfer facts are represented" pick out the same artifacts today. The decision therefore turns on three narrower questions:

1. Are *no transfer*, *zero-duration transfer* and *refusal* really different semantics? (Yes: W1–W3.)
2. For a valid artifact that authors no transfer-affecting fact, which meaning should an Engine that executes it adopt? And is not executing it a legitimate alternative?
3. Who owns that rule, and should the Factory grammar gain a way to author that a hand-off exists without authoring geometry or timing?

## Decision at stake

1. Can Arcogine keep "spatial record absent ⇒ no transfer lifecycle" for V2? Or must binding, reservation, state and events get a non-spatial interpretation before V2 is published and spatial runtime is activated?
2. Must the unreleased V2 grammar be corrected before it is attributed?
3. Which represented-content meanings, and which open support choices, does the successor Engine-applicability question receive?

## Scope and non-goals

**In scope** (from the brief): distinct-resource and same-resource continuation; when a destination is selected and bound; whether in-flight state exists without spatial facts; absent versus zero versus refusal; reservation and arrival only where a transfer phase changes them; who owns the discriminator; result, provenance, identity and compatibility consequences.

**Out of scope:** the distance metric; paths, conveyors, transport resources, buffers, congestion and routing graphs; how a positive non-spatial duration would be represented (the separate `CANDIDATE` timing question); transport ontology; changes to dispatch, ranking or sequencing; presentation; and **the successor Engine-applicability question itself**, including which Engine identity executes or refuses any V2 case.

## Executive conclusion

1. **No transfer, zero-duration transfer and refusal are three different result-affecting semantics** (W1–W3, unchanged from revision 1). Under Engine v1's fixed rules, a zero-duration transfer creates a separate turn at the same simulated time and a window in which the destination holds only a reservation. That changes whether an external availability command is accepted, where bounded advancement stops, and what the supported event stream and observations show — even though completion time in isolation is equal. So the choice passes the Engine v1 §1.1 membership test. It must be stated in whichever Engine interpretation executes the case, and must never be left ambient. *Confidence: high.*

2. **The anti-synthesis rule does not discriminate among the absence meanings.** An Engine rule that reads "no transfer-affecting fact authored" as no transfer (C3a), as a zero-duration lifecycle (C2), or as out of its support domain (C3b) interprets represented absence. None of them claims that `ticksPerCell = 0`, `handlingTicks = 0` or a placement was authored. Each is permitted by the Determinism Contract rule 3 and Factory §11.1, provided the executing Engine definition states it explicitly. *Confidence: high* (this adopts the review's reading, which the text supports).

3. **Viable candidates after the hard screen:** C2, C3a, C3b as a support-domain choice, and C4 (an authored non-spatial hand-off applicability fact) in two variants. **C1 as currently written fails** one falsifier: Engine v1 never states the absence rule, and its §8 text is unconditional, so two conforming implementations could disagree about whether a transfer state or event exists. What it produces for the artifacts it covers is the same as C3a.

4. **Recommended meaning, by trade-off rather than elimination — C3a, explicit no-transfer on absence.** For a distinct selected destination, a transfer phase exists iff the executed artifact authors the facts from which the executing Engine derives a transfer interval. When none are authored, the executing Engine states, as its own explicit rule, that distinct-resource continuation has no transfer phase: no binding separate from processing start, no reservation, no `TRANSFERRING`, no `TRANSFER_*`. Same-resource continuation never transfers. It is chosen because (§Decision criterion):
   - no current consumer or support requirement needs what C2 or C4 add;
   - it adds the least semantic and support surface;
   - it gives V1 and V2-absent artifacts the same behavior, and they make the same (empty) handling assertion;
   - it adds no command-sensitive runtime state that has no authored basis;
   - it matches the reconciled Factory composition rationale.

   This is a **deliberate conservative trade-off, not a proof that the alternatives are forbidden.** *Confidence: medium.* The criterion is sound and fits the evidence, but its weights are a judgment.

5. **C2 remains a legitimate future Engine design.** An Engine identity that deliberately adopts "every distinct-resource continuation has a lifecycle, zero duration when no timing is authored" is coherent. The price: authored zero and absence become behaviorally identical for lifecycle existence, and every multi-resource production-only run gains W1-type windows that react to commands. Adopting it needs a distinguishable Engine identity (the normal evolution mechanism, not an objection) and a concrete consumer need to justify that price. None exists today.

6. **Refusal (C3b) is a legitimate support-domain choice, not a meaning to reject.** An Engine identity may treat valid V2-absent artifacts as outside its support domain and refuse them before mutation. **No current product or support requirement says that a V2-absent artifact must be executable** (§Support-requirement search). Whether new production-only designs keep being published under `factory-model:v1` after V2 ships is still an open support decision. Two cautions:
   - As an Engine's *permanent and only* answer for V2-absent, refusal would make the V2-absent form publishable but executable nowhere. That fires the Factory composition decision's reopening trigger "the Engine applicability answer changes the relative cost of this Factory boundary".
   - The refusal predicate must be decidable before mutation, so it cannot key on the runtime same-or-distinct selection outcome (case 5).

   Whether to refuse or execute V2-absent is handed to the successor question as open.

7. **C4 is not adopted for now, because nothing currently needs it — not because it is redundant.** An authored fact that says "a hand-off exists" without authoring floor, placement or handling magnitudes carries information a present zero spatial record does not. But C4:
   - does not answer the absence question — artifacts without the fact still need a C2, C3a or C3b meaning;
   - needs a grammar addition, which is cheap before V2 attribution and needs a new Factory policy afterwards;
   - in its zero-default variant (C4-z), must be squared with the current Factory rule that partly authored handling facts are draft state;
   - has no consumer.

   Its natural home is the `CANDIDATE` non-spatial-timing question, whose candidate families it extends.

8. **V2 grammar: no correction is justified by current evidence.** The deferral has a cost: once V2 is attributed, adding C4 needs a distinguishable Factory policy. This matches the reconciled composition decision, which accepted per-transition policy cost because transitions were not shown to be frequent.

9. **Ownership.** Transfer existence is decided by an **Engine interpretation rule** (owned by the executing Engine definition). The rule is conditioned on **authored Factory content** (whether transfer-affecting facts are present), and applied to a **runtime outcome** (whether the selected destination differs from the source). Whether an Engine executes a given represented-content case at all is that Engine's **support-domain** decision. Factory owns the facts; it does not own the lifecycle.

## Repository evidence

### Durable authorities

Re-verified at the baseline. Rows unchanged from revision 1 are kept because the review found them accurate.

| # | Repository fact | Source |
|---|---|---|
| F1 | A V2 design has required production records plus one optional spatial record, which is either absent or present and complete. Absence means "no spatial, layout or handling assertion", with nothing synthesized from a default. A present record with zero magnitudes is a different authored design. Unknown or partly authored facts, "such as geometry whose handling magnitudes are unknown", are draft or adapter state and not publishable. The record has "no geometry-only or handling-only form". | [Factory Model v2 §1.1](../../../docs/architecture/factory-model-v2.md) |
| F2 | `handlingTicks` is the "authored fixed overhead applied once per inter-resource transfer"; `ticksPerCell` is the "authored material-handling rate magnitude". | Factory Model v2 §1.1 table |
| F3 | Factory owns authored facts. "Distance metric, rounding, zero-distance behavior, destination binding, reservation and transfer lifecycle belongs to Engine semantics." | Factory Model v2 §1.1; [Factory design §11.1](../../../docs/architecture/factory-design.md#111-semantic-evolution) |
| F4 | "A V1 model has no spatial facts and therefore no spatial behavior — the truthful execution of a design that never authored spatial semantics, not a degraded mode." | Factory design §11.1 |
| F5 | "An absent record means the design makes no assertion in that semantic dimension; nothing is synthesized for it from a default." "An Engine refuses an artifact outside that domain before runtime mutation rather than ignoring represented content or supplying absent content." | Factory design §11.1 |
| F6 | "Authored facts are never synthesized to make an interpretation applicable. Publication validity never establishes applicability: an interpretation applies only to the Factory policies and represented content its own definition supports." | [Determinism Contract rule 3](../../../docs/architecture/overview.md#determinism-contract) |
| F7 | An intentional result-affecting change is a new Engine identity; repairing nonconformance is not. Attribution fixes the whole definition. | Determinism Contract rule 4; [semantic evolution rules](../../../docs/architecture/overview.md#semantic-evolution-and-support) |
| F8 | Engine v1 transfer rules are stated for "distinct source and destination resources" (§7) and in the unconditional §8 state machine. §5 names the V2 spatial facts as the ones consumed; the §7 formula needs positions. | [Engine Semantics v1 §5, §7, §8](../../../docs/architecture/engine-semantics-v1.md) |
| F9 | Engine v1 never states which Factory policies or represented content it supports. It never states that distinct-resource continuation of a V1 artifact has no transfer. §1.1(3): recording a previously unwritten rule without changing behavior is a correction. §1.1(4): an unrecorded rule that meets the membership test is a document defect. | Engine Semantics v1 §1.1 |
| F10 | Under v1, zero magnitudes still produce transfer start and completion (§7.9). A zero-duration transfer is still a scheduled completion turn (§12). A destination holding only inbound reservations "may still be taken offline" (§9.6). Arrival at an offline destination converts the reservation into a queue entry on the bound destination (§9.8). | Engine Semantics v1 |
| F11 | Same-resource continuation: "no transfer state, no duration, and no transfer events". | Engine Semantics v1 §7.8 |
| F12 | Session semantics — one-event `advance()`, `advanceUntil` with an event-count bound, rejection before mutation — are part of v1's result-affecting interpretation. | Engine Semantics v1 §1.2 |
| F13 | Whether `engine-semantics:v1` applies to V2 artifacts, with the spatial record present or absent, "is not established"; it is the open applicability question. | Factory Model v2 §1.2 |
| F14 | **(new)** "Whether new production-only designs continue to be published under `factory-model:v1` once this policy is released is a support decision owned by the Factory semantic-evolution contract, not part of this grammar." The V2 release slice must carry "a support declaration that states whether `factory-model:v1` publication continues for production-only designs". | Factory Model v2 §2; [spatial-runtime plan](../../../docs/planning/spatial-runtime-consequences.md), V2 canonical-identity slice |
| F15 | **(new)** The successor brief says: "A still-supported V1-only state is a valid interim refusal boundary, not a full answer to V2 applicability." It also names "present-only versus both-form support, if their meanings justify partitioning" as a proving dimension. | [Successor brief](../../../docs/research/investigations/engine-applicability-after-transfer-boundary.md) |

### Non-normative retained rationale (history, not authority)

The [Factory composition decision record](../../../docs/history/decisions/2026-09-23-factory-model-semantic-composition.md):

- selected the optional-record V2 because "one closed policy represents both sets truthfully without making spatial facts the price of the newer policy";
- left V1-publication continuation as "a scoped support decision";
- accepted that "geometry without handling magnitudes, or unknown values, cannot be published under this policy";
- lists as reopening triggers "geometry must be authored without handling magnitudes" and "the Engine applicability answer changes the relative cost of this Factory boundary".

It also records that the coupling of absence to no transfer was an *inherited premise* of that decision.

### Research and planning state (verified at baseline)

Unchanged from revision 1: `factory-model:v2` is unreleased; V2 shape and validation are landed; V2 identity and publication are blocked on this question and then the successor; spatial and transfer runtime activation is held; `engine-semantics:v1` is fixed with pre-spatial conformance landed; the predecessor applicability question is `SUPERSEDED`; the successor is `CANDIDATE`; non-spatial timing is `CANDIDATE`.

### Implementation and executable evidence

Re-verified. A search of `product/**/src/main/**/*.java` for `TRANSFER`, `reservation` and `handlingTicks` finds only `SpatialRecord` and `FactoryModelV2Validator`.

| # | Repository fact | Source |
|---|---|---|
| I1 | No transfer runtime exists. `RuntimeEventType` has no `TRANSFER_*` value. | `product/**/*.java` |
| I2 | For distinct-resource continuation, `FactoryHandler.handleTaskEnd` releases the source, selects the next machine, and **starts the next step in the same handler turn** when the machine can accept. Otherwise the job goes to the machine queue or the multi-eligible backlog. There is no separate binding step and no reservation. | `process/FactoryHandler.java` |
| I3 | `FactoryRuntime.advance()` processes exactly one scheduled event. Commands run between calls at the current `SimTime`. | `process/FactoryRuntime.java` |
| I4 | `setMachineAvailability(id, false)` is rejected before any mutation when the machine has active jobs. | `FactoryRuntime.setMachineAvailability` |
| I5 | The next-step start is reported as `JOB_DISPATCHED` in the same turn as `JOB_STEP_COMPLETED`. | `FactoryRuntime` event recording |
| I6 | `FactoryModelV2` shares no supertype with `FactoryModel`; the runtime cannot receive V2 content. `EngineSemanticsVersion` supports exactly `engine-semantics:v1`. | `model/v2/FactoryModelV2.java`, `types/EngineSemanticsVersion.java` |
| I7 | v1 conformance fixtures pin completion vectors for V1 models with distinct-resource routings and no transfer phase. | `EngineSemanticsV1DispatchConformanceTest` |

### Support-requirement search (new; review item 2)

**Question searched:** does any current product, planning, support or consumer requirement say that a valid V2 spatial-absent artifact must be executable? Or does any consumer need hand-off commitment (binding, reservation, `TRANSFER_*`) for designs that author no transfer facts?

**Scope:**

- `docs/product/` (charter included; no match for transfer or hand-off);
- `docs/architecture/` (Factory design, Factory Model v2, Engine v1, overview, runtime contract, operational continuity, ISA-95 mapping, standards alignment);
- `docs/planning/` (Factory-design capability, engine readiness, spatial-runtime consequences, operational-execution readiness — no transfer match in the last);
- `docs/research/` (register; transfer, successor, predecessor, composition and game-programme briefs);
- `docs/development/semantic-contract-support.md`;
- `docs/history/decisions/`.

Search terms: `spatial absent`, `without a spatial record`, `production-only`, `no spatial record`, `transfer`, `hand-off`, `reservation`.

**Result:**

- **No requirement that V2-absent must execute.** The V2 release slice must *declare* whether V1 publication continues (F14). The composition record left that open. The successor brief treats a V1-only interim state as a valid refusal boundary (F15). Planning's closure scenario requires only that "neither a V1 design nor a V2 design without a spatial record receives synthesized spatial semantics". That constrains the meaning, not whether the case is executed.
- **No consumer need for lifecycle without transfer facts.** The game programme says transfer is Arcogine-owned. It forbids the game from assuming that absence means no lifecycle, and it makes spatial trade-offs wait for transfer reconciliation. It states no need for hand-off events in production-only designs. The operational-execution plan has no transfer content. No external consumer inventory exists in the repository; consumers outside the repository were not inspected.
- **No consumer need to author hand-off existence without geometry or timing** (C4). The only related trigger on record is the composition reopening trigger for "geometry … without handling magnitudes", which is the opposite shape.

This is a bounded absence claim over the listed scope, not proof that no such need can arise.

## Candidate models

Each candidate states its absence semantics explicitly and names the layer it acts on. The *meaning* layer is what an executing Engine does. The *support* layer is whether an Engine executes the case at all. The *grammar* layer is what Factory can represent.

- **C1 — current coupling, implicit (meaning).** A transfer phase exists only when the spatial record is present; absent means no transfer. Engine v1 never states this rule. Factory states it only as "no spatial behavior" for V1.
- **C2 — zero-duration lifecycle on absence (meaning).** The executing Engine states an explicit rule over represented absence: a distinct selected destination gets binding, reservation, `TRANSFERRING` and `TRANSFER_*` events, with duration 0 when no transfer-timing fact is authored. It does not claim that zero magnitudes or placement were authored. Same-resource never transfers. *Restated per review Finding 1.*
- **C3a — explicit no-transfer on absence (meaning).** A transfer phase exists for a distinct selected destination iff the artifact authors the facts from which the executing Engine derives a transfer interval (under V2 today, the present spatial record; in future, possibly a separately admitted non-spatial timing record). When none are authored, the executing Engine states an explicit rule: no transfer phase. This is also an Engine rule over represented absence. It is no more and no less "synthetic" than C2.
- **C3b — support-domain refusal (support).** An Engine identity declares valid V2 artifacts with no authored transfer facts, or a static subclass of them, outside its support domain, and refuses them before mutation. V1 artifacts may still be executed under their own identity. *Restated per review Finding 2.* C3b says nothing about meaning. If the same artifact is later executed under some identity, that identity still needs C2 or C3a.
- **C4 — authored non-spatial hand-off applicability fact (grammar plus meaning).** Factory gains an optional authored fact meaning, roughly, "in this design, work moving between distinct resources passes through a hand-off". It authors no floor, placement or handling magnitude. *Strongest form per review Finding 3.* Two variants:
  - **C4-z:** fact present, no timing contract → the executing Engine gives a zero-duration lifecycle (a C2-style Engine rule, but gated on an authored assertion rather than applied to every distinct continuation).
  - **C4-t:** fact present → a lifecycle exists, and execution needs a separately present timing contract. The fact without timing is either not publishable or refused by the Engine.

  In either variant, an artifact **without** the fact still needs an absence meaning (C2, C3a) or refusal (C3b). C4 therefore **adds** a represented-content case. It does not **replace** the absence decision.

## External evidence

Unchanged from revision 1, and still not decisive. The question is mainly about Arcogine's own ownership, identity and absence rules.

| Source | Establishes | Where the analogy breaks | Verification |
|---|---|---|---|
| *Simio and Simulation: Modeling, Analysis, Applications*, 7th ed., ch. 4, Table 4.1 (<https://textbook.simio.com/SASMAA7/ch-first-model.html>) | A mature discrete-event simulation product treats zero-time transfer (a *Connector*) and timed movement (a *Path*) as **explicitly authored modeling choices** between explicitly connected objects. | Simio models flow topology explicitly and has no "no-transfer" mode other than not connecting objects. It supports only the narrower claim that zero-time hand-off is authored content. It does not settle what an engine should do when the model is silent. It is weak support for C4's premise (authoring hand-off existence is a recognized modeling act), and gives no evidence of any Arcogine consumer need. | Checked in the revision-1 session (7th-edition online text); not re-fetched for this revision |
| Job-shop scheduling literature with transportation times (e.g. Hurink & Knust, 2005) | Background: the classical job-shop abstraction leaves transport out, and transport is added as an explicit extension. | No event, state or command semantics, so it cannot distinguish no-transfer from zero-duration transfer. | **Unverified background. Not load-bearing.** |

External evidence shows that each candidate is *possible*. It does not show that any of them is *necessary*.

## Proving cases

### Discriminating witnesses: no transfer versus zero-duration transfer (unchanged)

These rest on the fixed v1 rules (F10–F12) and current behavior (I2–I5). Model: M1 and M2, both unary and online. One product routes `M1:5 → M2:3`. Workload of one. Step 1 ends at t=5.

- **W1 — command acceptance.** Advance once (the `TaskEnd` at t=5), then issue `setMachineAvailability(M2, offline)` at t=5.
  - *No transfer:* the job is already active on M2, so the command is **Rejected** with no mutation. Completion is at t=8.
  - *Zero-duration transfer:* M2 holds only an inbound reservation, so the command is **Accepted** (§9.6). `TRANSFER_COMPLETED` fires at t=5 with M2 offline, and the job waits in M2's queue. Completion depends on when M2 recovers.
- **W2 — bounded advancement.** `advanceUntil(t=5, maxEvents=1)` right after step 1 ends.
  - *No transfer:* the job is left processing on M2.
  - *Zero-duration transfer:* the job is left `TRANSFERRING`, with a completion turn pending at the same time.
- **W3 — supported stream.**
  - *No transfer:* `JOB_STEP_COMPLETED → JOB_DISPATCHED` in one turn.
  - *Zero-duration transfer:* `JOB_STEP_COMPLETED → TRANSFER_STARTED → TRANSFER_COMPLETED → JOB_DISPATCHED`, with different sequence numbers, event counts and in-flight observation.
- **Refusal** (added for completeness): no runtime is created. The attempt ends with a refusal before any mutation, and there is no event stream to compare.

Completion time in isolation is equal (t=8), which is why "zero duration" is easy to mistake for "no transfer".

### Candidate-by-case matrix

For each candidate, a cell gives: **exists?** / **decided by, and owner** / **binding** / **reservation** / **absent-fact behavior** / **state and events** / **provenance and identity**. "Executing Engine" means whichever identity the successor question admits for the case.

**Case 1 — `M1 → M1` (the selected destination is the source).**

- **All candidates:** No transfer. Decided by the Engine rule that same-resource continuation never transfers (F11), applied to the runtime selection outcome.
  - Binding: none separate from processing start. Reservation: none.
  - Absent facts are irrelevant.
  - `TRANSFERRING` and `TRANSFER_*` are forbidden.
  - Identity unchanged.
- **Discriminating power: none.** This is the control case. With a multi-eligible step, "same resource" is a runtime outcome, not an authored fact.

**Case 2 — `M1 → M2`, V2 spatial-absent, no other authored transfer fact.**

- **C1:** No transfer, by an implicit rule. The owner is ambiguous. **Fails:** v1 §8 is unconditional and §7 is undefined without positions, so conforming implementations could disagree about whether a transfer state or event exists.
- **C2:** Transfer with duration 0, by an explicit Engine rule over represented absence.
  - Owner: the executing Engine.
  - Binding at admissibility; one reservation unit held for one turn at the same time.
  - `TRANSFER_STARTED` and `TRANSFER_COMPLETED` are required, and W1–W3 zero-duration behavior applies.
  - Provenance: V2 fingerprint (absent form) plus an Engine identity that states C2.
  - **Not falsified.** Costs are listed under §Decision criterion.
- **C3a:** No transfer, by an explicit Engine rule over represented absence.
  - Owner: the executing Engine.
  - Binding coincides with processing start; no reservation.
  - `TRANSFER_*` and `TRANSFERRING` are forbidden; `JOB_DISPATCHED` in the same turn.
  - Provenance: V2 fingerprint (absent form) plus an Engine identity that states C3a. The production outcome equals the V1 production interpretation.
- **C3b:** The Engine refuses the artifact before mutation (valid but outside its support domain). No runtime, no events. Provenance: a structured refusal naming the policy and represented content. **Not falsified.** It forces no invented facts, because the designer can still publish and run the design as V1 while V1 publication continues. See case 5 for how the predicate must be shaped, and §Decision criterion for its conditional status.
- **C4-z / C4-t:** the C4 fact is absent here, so C4 **defers to C2, C3a or C3b** for this case. C4 does not answer it.

**Case 2′ (new, C4 only) — `M1 → M2`, V2 spatial-absent, C4 hand-off fact present, no timing.**

- **C4-z:** Transfer with duration 0.
  - Decided by the authored hand-off fact (Factory) and the Engine's zero rule for unstated magnitude (Engine).
  - Binding at admissibility; reservation for one turn; full zero-duration lifecycle (W1–W3).
  - Provenance: a fingerprint that differs from both case 2 and case 3.
  - **Tension:** Factory Model v2 §1.1 makes partly authored handling facts "draft or adapter state". C4-z must define the hand-off fact as a *complete* assertion in its own right ("a hand-off exists, and this design deliberately asserts no magnitude") and not as unknown handling data. That distinction has to be written into the grammar and reviewed. It is a cost, not a falsification.
- **C4-t:** No publishable or executable meaning without timing. The fact without timing is either Factory-invalid or refused by the Engine. In effect C4-t is a non-spatial timing record with a separate presence flag, which is the `CANDIDATE` timing question's territory.
- Under C1, C2, C3a and C3b the case is **not representable**; the current grammar has no such fact.

**Case 3 — `M1 → M2`, present spatial record, authored zero (`ticksPerCell = 0`, `handlingTicks = 0`).**

- **C1, C3a, C4 (with the spatial record as the timing carrier):** Transfer with duration 0.
  - Factory authors the facts; the Engine applies binding, arithmetic and lifecycle.
  - Binding at admissibility (§6); reservation until the arrival at the same time (§9.2).
  - `TRANSFER_STARTED → TRANSFER_COMPLETED` on a separate turn at the same time (§12).
  - The fingerprint differs from case 2, and the behavior differs too (W1–W3).
- **C2:** The same lifecycle, but **behaviorally identical to case 2**. Absence and authored zero differ only in fingerprint and provenance, not in lifecycle existence or timing. Case 2 does not claim authored zero, so the representation stays truthful. Behaviorally, though, V2's absent/zero distinction does nothing for transfer under C2.
- **C3b:** as C3a for this case. Refusal targets only the absent form.
- **C4-z vs case 3:** "hand-off asserted, magnitude unasserted" (case 2′) and "spatial record with authored zero" (case 3) behave the same but are different content. The present zero record also authors floor, placement and footprint. This is why C4 is not redundant (Finding 3).

**Case 4 — `M1 → M2`, positive timing facts.**

- **All candidates:** Transfer with positive duration, fixed at binding (v1 §6.6, §7).
  - Factory facts plus the Engine formula.
  - Binding at admissibility; reservation for the interval.
  - Full lifecycle; a late observation must be able to reconstruct `TRANSFERRING` (§11).
- **Discriminating power: none.**

**Case 5 — multi-eligible next step; destination bound before an interval.**

- **With transfer facts (zero or positive), all candidates:** ranking unchanged. Binding happens when the selected destination can accept, and the eligible set collapses to the bound destination (§6.9). Zero and positive bind at the same point.
- **Without transfer facts:**
  - *C1 / C3a:* there is no binding step. Selection plus immediate start is the placement. A job that cannot be placed stays unbound and may reselect (I2, I7).
  - *C2:* the job binds and reserves on selection, and the one-turn window reacts to commands (W1 generalizes).
  - *C3b (new sharpening):* refusal happens **before mutation**. For a multi-eligible step, whether the next destination differs from the source is a *runtime* outcome. So a C3b predicate cannot be "refuse when a distinct-resource continuation occurs". It must be a static predicate over the artifact, for example:
    - (i) refuse every V2-absent artifact ("present-only support"); or
    - (ii) refuse V2-absent artifacts in which some product routing can statically reach a distinct-resource continuation — any consecutive step pair whose eligible sets are not both the same single resource.

    Option (ii) refuses nearly every multi-step design, so in practice C3b is close to present-only support. Refusing only when a distinct continuation actually happens would require refusal after mutation, which F5 forbids.
  - *C4:* with the fact, as C2 (C4-z). Without it, as whichever absence meaning is chosen.
- Case 5 separates C2 from C1/C3a in the absent case, and fixes the shape of C3b's predicate.

**Case 6 — destination availability or capacity changes between binding and arrival, non-zero interval.**

- **With positive transfer facts, all executing candidates:** v1 §9 applies. Binding is immutable; there is no rerouting; the destination may go offline while holding only a reservation; on arrival at an offline destination the reservation becomes a bound queue entry with `JOB_WAITING`.
- **Without transfer facts:**
  - *C1 / C3a:* unreachable (there is no interval). Ordinary rules apply: an active job blocks the offline command; a waiting unbound job reselects.
  - *C2 / C4-z:* reachable only as the zero-length window of W1.
  - *C3b:* no run.
- **Consequence for the reservation recheck the spatial-runtime plan requires before activation:** under C3a or C3b it covers only artifacts with transfer facts. Under C2 it covers every multi-resource run of an identity that executes V2-absent.

**Case 7 — deterministic ordering at the same time when duration is zero.**

- **Present zero (every executing candidate), and C2 / C4-z absent:** the v1 §12 chain. The completion turn is scheduled at the same `SimTime` and ordered by the existing insertion-order tie-break (§4.2), after same-time work already scheduled. The rules are complete and deterministic.
- **Absent, C1 / C3a:** there is no second turn. Placement happens inside the `TaskEnd` turn, before the source machine's queue and backlog cascade. This is the ordering pinned by the v1 fixtures (I7).
- **Absent, C2:** deterministic, but for every distinct-resource continuation it differs from the pinned no-transfer ordering whenever commands are interleaved or advancement is bounded.
- **C3b:** not applicable (no run).

**Case 8 — historical V1 and production-only artifacts that never authored spatial facts.**

- **V1 under `engine-semantics:v1`, all candidates:** **no transfer.** Every candidate must keep this. It is the executed, pinned behavior (I2, I7), matches F4, and v1 results are attributed to it (F7). Writing it into v1 appears to be a §1.1(3) correction (F9); the successor and reconciliation review own that classification. *This sub-case does not discriminate between candidates.*
- **Where the case does discriminate: a V1 or V2-absent artifact under any Engine identity other than v1.**
  - *C3a:* V1 and V2-absent behave the same (no transfer) under any identity that executes both. Behavior follows represented content: both make no handling assertion.
  - *C2:*
    - If the identity also executes V1 artifacts with C2, the same V1 artifact behaves differently under v1 and under the new identity. That is allowed, because attribution is explicit and cross-identity comparison is the consumer's job, but it is a real cost.
    - If the identity refuses V1, then V1 and V2-absent — whose production content is identical and which make the same empty handling assertion — run under different identities with different lifecycle behavior. The **policy label** decides that. Engine applicability may legitimately depend on exact policy (F6), so this is not a falsification. It does mean identical production content gets different results depending on which policy it was published under.
  - *C3b:* V2-absent is refused; V1 keeps running under v1 (or any identity that supports it). Production-only designs stay executable **only while V1 publication continues** (F14). If that support decision ends V1 publication for new production-only designs, then under C3b those designs are executable nowhere until some identity executes V2-absent.
  - *C4:* V1 has no C4 fact, so it falls to the chosen absence meaning. C4 changes nothing for V1.

**Case 9 — a hypothetical future grammar with geometry but no handling, or with a non-spatial timing record.**

- **C1:** keyed on geometry, so it would switch on transfer for geometry-only content and miss a non-spatial timing record. Wrong under the ownership split.
- **C3a:** determined by whether the grammar supplies interval inputs. Geometry alone does not; a timing record does.
- **C2:** unaffected. The lifecycle always exists and timing only changes the duration.
- **C4-t:** realized by such a timing record, plus or minus a separate flag.
- Not reachable under V2. It maps to the composition reopening trigger and the timing question.

### Case-by-candidate summary

| Case | C1 (implicit) | C2 | C3a | C3b (support) | C4-z | C4-t |
|---|---|---|---|---|---|---|
| 1 same resource | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ |
| 2 distinct, absent | ✘ implicit | ✔ (zero lifecycle) | ✔ (no transfer) | ✔ (refuse) | defers | defers |
| 2′ hand-off fact, no timing | n/r | n/r | n/r | n/r | ✔ with grammar tension | ✘/refuse by design |
| 3 authored zero | ✔ | ✔ but same behavior as absent | ✔ | ✔ | ✔ | ✔ |
| 4 positive | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ |
| 5 multi-eligible binding | ✔ | ✔, adds windows | ✔ | ✔ with a static predicate only | ✔ | ✔ |
| 6 change during interval | ✔ | ✔, widens recheck | ✔ | ✔ | ✔ | ✔ |
| 7 zero ordering | ✔ with facts | ✔, changes absent ordering | ✔ | n/a absent | ✔ | ✔ |
| 8 V1 under v1 | ✔ | ✔ (must keep no-transfer) | ✔ | ✔ | ✔ | ✔ |
| 8 V1/V2-absent under another identity | implicit | ~ policy label or cross-identity divergence | ✔ uniform | ~ executable only while V1 publication continues | follows absence meaning | follows absence meaning |
| 9 future grammar | ✘ | ✔ | ✔ | ✔ | ~ | ✔ |

Key: ✔ viable; ~ viable, with a cost weighed in §Decision criterion; ✘ fails a hard falsifier; n/r not representable; defers = the candidate gives no answer, and the absence meaning applies.

## Viability screen (hard falsifiers)

These apply the brief's and the handoff's falsification rules, under the review's corrected reading of F5/F6.

| Falsifier | C1 | C2 | C3a | C3b | C4-z | C4-t |
|---|---|---|---|---|---|---|
| Cannot truthfully distinguish absence from authored zero | pass | pass (fingerprint and provenance distinguish; behavior does not — a cost) | pass | pass | pass | pass |
| Makes a production-only model acquire invented authored facts | pass | pass (Engine rule, not authored claim) | pass | pass (refusal invents nothing; V1 route stays) | pass | pass |
| Engine interpretation depends on consumer presentation | pass | pass if justified by an Engine-owned need; **fails if uniform vocabulary is its only justification** | pass | pass | pass | pass |
| Two conforming implementations may disagree on transfer existence | **fail** | pass if stated | pass if stated | pass | pass if stated | pass if stated |
| Contradicts durable rules without naming the required change | fail (v1 text unconditional) | pass (needs a distinguishable identity for any changed behavior; says so) | pass | pass | pass (names a grammar change) | pass (names a grammar change) |
| Non-deterministic binding, reservation or zero-duration ordering | pass with facts; implicit without | pass | pass | pass | pass | pass |
| Silently changes V1 meaning | pass in outcome | pass only if not applied under v1 | pass | pass | pass | pass |

**Survivors:** C2, C3a, C3b (support layer), C4-z and C4-t (grammar layer). C1 fails only in its current *wording*. What it produces is carried forward by C3a.

## Decision criterion among viable candidates

With the hard falsifiers applied, the choice is a **trade-off**. The criteria, fixed before the candidates were weighed:

- **T1 — Demonstrated need.** Does a current consumer, product or support requirement need what the candidate adds? (Evidence: §Support-requirement search.)
- **T2 — Semantic and support surface.** How many new Factory grammar elements, Engine rules, runtime states and support declarations does the candidate add?
- **T3 — Behavior follows represented content.** Do artifacts that assert the same thing in the transfer dimension (V1 and V2-absent: nothing) behave the same? Or does the policy label alone change behavior? Policy-dependent applicability is permitted (F6). T3 is a preference, because divergence pushes explicit cross-policy or cross-identity comparison onto consumers.
- **T4 — No result-affecting consequence without an authored basis.** Does the candidate add command-sensitive runtime states (W1–W3) that no authored fact motivates?
- **T5 — Reversibility.** What would it cost to change the choice later?
- **T6 — Fit with the reconciled Factory composition.** Does it keep V2-absent as a truthful production-only design, as the composition rationale intended, without firing a composition reopening trigger?

| | T1 need | T2 surface | T3 content-uniform | T4 no unmotivated states | T5 later change | T6 composition fit |
|---|---|---|---|---|---|---|
| **C3a** | nothing new needed (records existing V1 behavior) | lowest: one explicit Engine rule | ✔ | ✔ | switching to C2 later needs a new Engine identity (the normal route) | ✔ |
| **C2** | none found | adds a lifecycle to every production-only multi-resource run; makes the absent/zero lifecycle distinction behaviorally inert | ✘ unless V1 is also re-interpreted under the new identity | ✘ (W1–W3 windows with no authored basis) | switching to C3a later needs a new Engine identity | ✔ neutral |
| **C3b** | none requires V2-absent to execute, but nothing requires refusing it either | smallest execution domain; needs a static predicate and a refusal contract | n/a (no run) | ✔ | widening support later changes acceptance → new identity | **~** using it as the permanent only answer fires the composition trigger "the Engine applicability answer changes the relative cost of this Factory boundary"; tied to the open V1-publication decision |
| **C4-z** | none found | grammar addition, validation interactions with the spatial record, reconciling with "partly authored handling ⇒ draft", plus an Engine zero rule | absence still needs a meaning | ✔ (gated on an authored assertion) | adding later needs a new Factory policy after V2 attribution | ~ reopens the grammar just reconciled |
| **C4-t** | none found; overlaps the timing question | a grammar addition that duplicates the timing question's future carrier | absence still needs a meaning | ✔ | as C4-z | ~ |

**Result.**

- **Meaning for an executed absent case: C3a.** It does best on T1–T4 and T6, and ties with C2 on T5. The choice is conservative: it keeps current behavior, adds no abstraction, and turns the existing implicit rule into an explicit one owned by the Engine.
- **Support: C3b stays open for the successor.** It is a legitimate scope for any identity that chooses not to execute V2-absent — for example an interim V1-only or present-only boundary (F15). It must not become the permanent *only* V2-absent answer without revisiting the composition boundary and the V1-publication support decision together.
- **Grammar: C4 deferred to the non-spatial-timing question**, with a recorded trigger. The deferral is consistent with the composition decision's accepted per-transition policy cost.

**What would change the result:**

- a concrete consumer or plant model needing hand-off commitment (binding, reservation, `TRANSFER_*`) in designs that author no transfer facts → C2 under a new identity, or C4-z;
- a consumer needing to author "a hand-off exists" without geometry or timing → C4 (V2 grammar correction if still unattributed, otherwise a new policy);
- a support decision that production-only designs stop publishing under V1 → C3b cannot be the only V2-absent answer; some identity must execute V2-absent with a stated meaning;
- an admitted non-spatial timing record → C4-t is realized through the timing question, and C3a's condition extends to that carrier.

## Adversarial analysis (author's self-challenge only)

This is **not** an independent adversarial review.

- **A1 — "C3a is just C1 renamed."** The two cover the same artifacts under V2 today. They still differ in three ways: C3a is explicit where C1 is implicit (a falsifier C1 actually fails), C3a keys on authored transfer facts rather than on geometry (this matters in case 9), and C3a assigns ownership of the absence rule.
- **A2 (resolved) — "C2's zero is an Engine rule, not synthesis."** Accepted, following the review. The same holds for C3a. The anti-synthesis rule does not discriminate among the meanings, and nothing in this revision relies on it to do so.
- **A3 (new) — "The criterion is rigged toward the status quo."** T1 and T2 do favor minimal change. That bias is intended and is stated openly: the repository's research method asks for a no-new-abstraction candidate, and the composition decision rejected frameworks with no current requirement. The bias would be illegitimate if it hid a real need. The support-requirement search found none within its stated scope. T3, T4 and T6 are not status-quo arguments; each names a concrete cost of C2 or C3b.
- **A4 (new) — "T3 contradicts the review's point that applicability may depend on policy."** It does not. T3 accepts that policy-dependent behavior is permitted and treats it as a cost: consumers must make explicit cross-policy comparisons (Determinism Contract rule 5). It is weighed, not used as a falsifier.
- **A5 (new) — "C3b is the more conservative choice: it commits no meaning at all."** For V2-absent specifically, yes, and that is why it stays open as a support choice. But C3a commits no *new* meaning: it extends to V2-absent the meaning v1 already gives identical production content. C3b's conservatism therefore buys little, and it costs executability once V1 publication ends.
- **A6 — "Real plants always hand off between machines, so C2 is more truthful."** A zero-time hand-off is itself a modeling assertion. A design written at routing abstraction truthfully asserts nothing about hand-off (F1, F4). If the assertion matters, C4 is the truthful way to author it, and even then only with a consumer.
- **A7 — "Consumers want one event vocabulary."** That is presentation. Consumers may derive a visual hand-off from `JOB_STEP_COMPLETED`/`JOB_DISPATCHED`.
- **A8 — "Does C3a for V2-absent itself need a new Engine identity?"** Possibly. That is the successor's question. It is treated as neutral here because every candidate that executes V2 content faces the same identity question.
- **A9 — hidden assumption that v1 is fixed.** The repository treats it as fixed (register, successor brief, F7). The meaning for V1 is the same either way; only the mechanism for recording it changes.
- **A10 — stale baseline.** Re-checked. `main` moved to `812eff79` only in retrospective tooling and process documentation, so the conclusion is unaffected.

## Surviving invariants

1. **I-A — Three distinct semantics.** "No transfer", "transfer with zero duration" and "refusal before mutation" are distinct result-affecting outcomes (W1–W3). Every Engine definition that touches distinct-resource continuation must state, for each represented-content case it supports, which one applies. It must also state which cases it refuses.
2. **I-B — Existence is keyed on authored transfer-affecting content, not on geometry.** When an Engine gives a case a transfer lifecycle *because of authored content*, the condition is the presence of the facts from which it derives the interval (under V2 today, the present spatial record), not geometry as such. C2 is the exception by design: it gives a lifecycle to every distinct continuation, so the Engine rule is its condition, not authored content.
3. **I-C — Absence meaning is an explicit Engine rule, and refusal is a support choice.** Absence of every transfer-affecting fact never *implies* any meaning. The executing Engine states its meaning (recommended: no transfer) or declares the case outside its support domain. Refusal of Factory-invalid content is not an Engine concern: partial V2 content is never published. *(Restated. Revision 1's "refusal only for partial facts" is withdrawn.)*
4. **I-D — Same-resource continuation never transfers.** "Same resource" is a runtime outcome. So any refusal predicate keyed on distinct continuation must be static over the artifact (case 5).
5. **I-E — Lifecycle existence and duration are separate decisions.** Once transfer facts exist, zero is a legal duration with the full lifecycle. A later non-spatial timing record would condition existence by being present; how it is represented is a separate question.
6. **I-F (new) — V1 under `engine-semantics:v1` is no-transfer.** That behavior is attributed and must stay. No candidate may change it under v1.

## Transferability and reuse

- **Arcogine-specific dependency:** Engine v1's reservation semantics (an offline command is accepted while the destination holds only a reservation), one-event `advance()`, and commands interleaved between turns at the same time.
- **Potentially transferable result:** in a discrete-event engine that lets external commands interleave between same-time turns, or that bounds advancement by event count, a zero-duration phase is not outcome-equivalent to having no phase at all.
- **Boundary conditions:** it does not hold for an engine that processes all same-time events atomically, exposes no intermediate state, and has no command interleaving and no event-count bound.
- **Reusable research assets:** W1–W3 as conformance-fixture specifications; the static-predicate constraint on pre-mutation refusal (case 5); the layering of meaning, support and grammar.
- **Synthesis-seed candidate (nominated, not admitted):** "a zero-duration phase ≠ an absent phase under command interleaving or bounded stepping". *Revisit when* a second Arcogine question, or an external engine, shows the same distinction.

## What did not survive

- **"Geometry decides whether transfer exists."** Geometry and handling are bundled in V2; the discriminator is authored transfer-affecting content.
- **"Zero duration is behaviorally the same as no transfer."** Falsified by W1–W3.
- **Revision 1's claims that C2 is falsified by the anti-synthesis rule, that C3b forces invented facts, and that C4 is redundant.** All three are withdrawn. The candidates are viable, and they are now weighed by an explicit criterion.
- **Revision 1's invariant "refusal only for partial facts".** Vacuous; replaced by I-C.
- **C1 left implicit.**

## Confidence and limitations

- **High:** W1–W3 and I-A; the implementation inventory; the anti-synthesis rule not discriminating among the meanings; I-F; the static-predicate constraint on C3b.
- **Medium:** C3a as the recommended meaning. The criterion is explicit and fits the evidence, but weighting T1–T6 is a judgment an independent reviewer may reasonably weigh differently.
- **Medium:** deferring C4. It rests on a negative search with the scope stated above.
- **Limitations:**
  - No tests were run. W1–W3 come from the specification and a reading of the code; the transfer side is necessarily specification-only.
  - Consumers outside the repository were not inventoried.
  - The support-requirement search is a bounded absence claim.
  - The Simio source was checked in the revision-1 session and not re-fetched.

## Unresolved unknowns

- Which Engine identity or identities execute or refuse V1, V2-absent, V2-present-zero and V2-present-positive, and whether any existing fixed definition can admit V2 cases without changing its whole definition. **Successor question.**
- Whether any identity will treat V2-absent as outside its support domain (C3b), and how that interacts with the V1-publication support decision (F14). **Successor question, jointly with the V2 release support declaration.**
- Whether writing I-F into v1 is a §1.1(3) correction. **Successor question and reconciliation review.**
- Representation of positive non-spatial duration, and whether an explicit hand-off applicability fact (C4) is ever warranted. **`CANDIDATE` timing question**, whose candidate families this report proposes to extend.

## Durable consequences

Recommended; this report does not perform them.

1. **Engine architecture/specification** (reconciled with the successor question):
   - state I-A–I-F;
   - for each identity, state the absence meaning of each represented-content case it supports (recommended: C3a) and the cases it refuses;
   - remove the textual unconditionality in F8 by conditioning the transfer lifecycle on authored transfer-affecting content.
2. **Factory evolution contract §11.1** (editorial): generalize "no spatial behavior" for V1 to "no authored transfer-affecting content; transfer meaning is the executing Engine's explicit rule". Point to the Engine as owner.
3. **Factory Model v2:** no grammar correction justified by current evidence. Optionally note that the spatial record is currently the only carrier of transfer-affecting facts. Record that adding a non-spatial hand-off applicability fact after attribution needs a new policy.
4. **Research register and transfer brief:**
   - record the verdict after review and reconciliation;
   - re-promote the successor question with the handoff below;
   - extend the `CANDIDATE` timing question's candidate families with "explicit non-spatial hand-off applicability, with or without timing (C4-z / C4-t)" and its trigger.
5. **Planning:** once reconciled, re-resolve the held spatial-runtime slices against I-A–I-F and the successor result. Scope the reservation recheck to artifacts with transfer facts under C3a or C3b.
6. **Historical decision-rationale record:** likely warranted at reconciliation. Three serious alternatives (C2, C3b, C4) stayed viable and were set aside on a trade-off, not falsified. Each is an obvious candidate to be proposed again later, and the decisive trade-offs are not visible from the resulting contract alone. This is the retention test's core case. The exact report does not need to be kept.

## Implementation implication

This report admits no implementation. Current code already behaves as C3a for the only artifacts it can execute (V1).

## Successor-research handoff

This handoff is **conditional**. Nothing in it is settled until this revision survives a new independent adversarial review **and** durable reconciliation.

**If that happens, the successor may take as settled:**

1. No transfer, zero-duration transfer and refusal are distinct result-affecting semantics (I-A, W1–W3).
2. Transfer lifecycle, including absence meaning, is owned by the executing Engine definition. Factory owns the transfer-affecting facts. Runtime owns the same-or-distinct selection outcome.
3. Authored absence stays distinguishable from authored zero in fingerprint and provenance.
4. V1 under `engine-semantics:v1` is no-transfer (I-F). Changing that under an attributed identity would need the semantic-evolution treatment.
5. The anti-synthesis rule neither requires nor forbids any particular absence meaning; each identity must state its meaning explicitly.
6. Any pre-mutation refusal of V2-absent content must be a static artifact predicate (I-D, case 5).
7. There is no V2 grammar correction for lifecycle reasons on current evidence (C4 deferred, with a trigger).

**Recommended, not settled — the successor weighs these as inputs:**

- The meaning for any identity that executes V2-absent should be C3a (explicit no-transfer), on the §Decision criterion. C2 is a legitimate alternative design for a distinguishable identity if an Engine-owned need appears.

**Open, and handed to the successor:**

- Whether each identity executes or refuses V2-absent (C3b), with the V1-publication support decision as a coupled input.
- Present-only versus both-form support.
- Which identity executes each case, and whether v1 can admit any V2 case.

**Represented-content cases the successor must evaluate:**

- (a) V1 artifact;
- (b) V2 spatial-absent: under C3a meaning, under C2 meaning, and under refusal;
- (c) V2 spatial-present with authored zero magnitudes;
- (d) V2 spatial-present with positive magnitudes;
- (e) same-resource continuation under (b)–(d);
- (f) multi-eligible binding, and an offline destination at arrival, under (c) and (d), and under (b) with C2;
- (g) Factory-invalid present content;
- (h) valid content outside an identity's support domain, including a static predicate for (b) under refusal;
- (i) an unsupported Engine identity;
- (j) a hypothetical future carrier of transfer facts (case 9, C4) as an explicit support question, never wildcard acceptance.

## Follow-up triggers

- Independent adversarial review of this exact revision (required next).
- Reopen the recommended meaning if a consumer needs hand-off commitment without authored transfer facts, or needs to author hand-off existence without geometry or timing.
- Revisit the support stance on C3b if the V2 release support declaration ends V1 publication for production-only designs.
- Track follow-ups in the [research register](../../../docs/research/research-register.md).

## Sources

- Repository (baseline `07bf95cb14cc446fe02a12c88c9526a7ff205647`): the documents and source files cited in §Repository evidence and §Support-requirement search.
- *Simio and Simulation: Modeling, Analysis, Applications*, 7th ed., ch. 4, Table 4.1 — <https://textbook.simio.com/SASMAA7/ch-first-model.html> (checked in the revision-1 session).
- Job-shop-with-transportation literature, e.g. Hurink, J., Knust, S. (2005) — unverified background, not load-bearing.
