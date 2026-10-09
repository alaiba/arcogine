# Simulation execution account semantics: independent adversarial review

Date: 2026-10-09. Research evidence only; no architecture or implementation adoption.

## Exact target and custody

- Workspace: `workspace/simulation-execution-account`.
- Reviewed report: `a53778145f3ee9941a4b46c7f04f1ec17e6bff86`,
  `workspace/research/investigations/simulation-execution-account-report.md`.
- Report baseline and report final recheck: `79b3399149499f2f337206c909a277f66a5befd5`.
- Review assignment: `ab0332f4f753f8d6471a6b4fc82d2f197d7c1508`,
  `workspace/research/handoffs/simulation-execution-account-adversarial-review.md`.
- Live-main review baseline and final remote recheck:
  `912b2ac85cefab3907fc562a3aaa6507e51975a7`.
- This completed review: the commit adding this file, at
  `workspace/research/investigations/simulation-execution-account-adversarial-review.md`.
  Its exact commit is returned in the handoff; no self-referential SHA is invented here.

The full 67,265-byte report blob resolved before substantive report-specific review. The
original report, original reconstruction, and original experiment sources remain unchanged.
The existing workspace was fast-forwarded to its published handoff, then extended with review
commits. No handed-off history was rewritten. This workspace is not a merge candidate.

The only live-main change after the report baseline is the session/advancement note in
`docs/research/investigations/engine-evolution.md` (merged #462). It distinguishes driver batching
from the Engine's pre-execution time guard and explicitly denies that an advancement target
establishes interval closure. It changes no runtime behavior or normative contract and does not
defer the closure question. `product/`, architecture, research policy, and agent contracts in the
executed workspace are identical to the exact review baseline. No semantic stale-baseline defect.

## Independence and chronology

**Independent review achieved through a fresh isolated session.** This reviewer had no role in
authoring the report or original experiments and no responsibility to preserve their conclusions.
No different-person or different-model-family independence is claimed. High-risk depth applies.

The sequence was: resolve report identity using only its first 16 metadata lines; read the
registered brief, register, research policy and repository authorities; inspect runtime/control
code and tests; reconstruct candidates and failure cases; commit that reconstruction; only then
read the full report, its experiment and the inherited research. The assignment itself already
identified possible closure attacks. This was blind to report reasoning, not to handoff framing.
Synthesis seeds were not consulted.

The inspectable pre-review reconstruction is:
`ba6db2e35f25150624a53ae900c48d15757b78fb`,
`workspace/research/investigations/simulation-execution-account-review-reconstruction.md`.

Its independently stated candidates were: the existing producer plus a shared caller-capture
specification; a distinct semantic account authority; a retained record service; an analytics
evidence session; and a hybrid separating producer meaning, controller attestation, capture
warranty, and analytical sufficiency. Expected discriminators included idle tails, count-budget
exhaustion, exact interval boundaries, competing drainers, invisible no-op commands, alternative
waiting definitions, and reset/interleaving distinctions.

## What the report claims and what this review accepts

The report's central recommendation is to specify common execution-evidence coverage in the
existing Engine-owned runtime contract, keep physical capture with the delivery holder, and leave
analytical definitions responsible for their evidence requirements. It introduces no new entity,
RunId replacement, terminal runtime state, central record service, or event-sourcing dependency.

That ownership result survives. The evidence does **not**, however, establish that the report's
E1–E8 text and `CapturedAccount` helper can be promoted unchanged. In particular, a closure
declaration is a trusted controller attestation with capture-binding preconditions. It is not a
supported observation fact, and the helper does not establish those preconditions. Several
report claims about automatic detection and monotone determinacy are too strong.

The necessary qualifications below are bounded to this question. They neither require a new
closure investigation nor show that a public advancement API or retained-history service is
necessary. A new positive probe demonstrates a sufficient controller/capture procedure using
the existing supported API.

## Evidence and execution

### Repository authorities and semantic neighbors

Repository facts were checked against the exact live-main baseline: `AGENTS.md`, Researcher
contract, research operating model, testing section 10, Product Charter, architecture overview
(ownership, determinism, provenance), runtime contract, Engine session/dispatch and transfer
specification, Operational continuity and Operational architecture, Governance evidence and
conformance, the execution-account and analytics briefs, and Engine/spatial planning boundaries.

Implementation inspection included `FactoryRuntime`, `Scheduler`, `RecordingScheduler`,
`FactoryHandler` scheduling paths, command outcomes, supported payloads/metadata, and the
research substrate's `EvidenceWindow`, `DeclaredEvidence`, and routing fixtures. Session-control,
headless closure and supported-event evidence was inspected; the original experiment's reset,
fault, same-time, late-join, drain and reproduction assertions were rerun. Searches across docs
and product located run/reset, event and internal time, frontier, late join, retained history,
interval and provenance neighbors. Current Challenge's outcome/reference boundary and Governance
evidence requirements do not establish a present shared history service requirement.

After reconstruction, inherited material was retrieved at the report's exact section 5
coordinates: analytics Phase 1 (`7e72a465...`), original Phase 2 (`6a5c0435...`), its reopened
review (`2c85fe7c...`), paused revision (`75736ddd...`), Game report (`4ad00136...`) and Game
review (`6484795f...`). Relevant scope, custody, completeness and qualification sections were
inspected. This is not a review or resumption of paused Phase 2. The investigator's committed
pre-registration was compared with its experiment expectations; its disclosed oven-tail
imprecision does not change the executed assertions.

### Reproducible runs

No suitable devcontainer was running. Used the documented generic Docker route, image
`gradle:9-jdk21`, Temurin `21.0.12+8-LTS`, repository wrapper Gradle `9.8.0`. The checkout was
mounted at `/repo`; Gradle wrote only its conventional ignored build output. No runtime or
tracked build configuration changed.

Original experiment and substrate:

```text
docker exec arcogine-execution-account-review sh ./gradlew :research-experiments:test -PresearchExperimentSources=/repo/workspace/research/experiments/simulation-execution-account --no-daemon
```

**151 tests, zero failures/errors/skips**, including all **14 original proving cases**. Counts
were independently read from JUnit XML, not inferred from the report.

New probes and supported-boundary checks:

```text
docker exec arcogine-execution-account-review sh ./gradlew :research-experiments:test -PresearchExperimentSources=/repo/workspace/research/experiments --tests '*ExecutionAccountAdversarialTest' --tests '*ResearchPackageBoundaryTest' --no-daemon
```

**9 independent probes and 4 package-boundary tests, zero failures/errors/skips.** Passing a
negative probe means it reproduced the specified limitation; it does not mean the original
helper is correct. The full 151-test run preceded these additions; no combined full-suite pass
is claimed. This was a research-evidence run, not a full Java quality, coverage, security or CI gate.
No execution capability was unavailable. Spatial execution remains unsupported, not tested.

Final probe coordinate:
`928fb86665016bc94d5979e18f1ce5b2ebef3b57`,
`workspace/research/experiments/simulation-execution-account-review/com/arcogine/research/executionaccount/ExecutionAccountAdversarialTest.java`.
Its first draft was committed at `4a9361de977e8a46da9b54b0b216cd50f2343a72` at the same path.
Before successful execution, compile errors were corrected: a command's internal generic return
type required wildcard capture, and `JobWaiting` has no step-index accessor. Static inspection
also corrected two fixture assumptions: initial submission schedules only a completion, whereas
recovery schedules a start marker too. The budget case now stops on the second job's marker;
the pending-marker case stops after one completion. These were reviewer fixture corrections,
not changes to the investigator's tests or post-failure changes to the semantic verdict.

## Closure: the six propositions must remain separate

Report locations challenged: executive E6 and implementability claim; cases 6/6b; sections 11–12,
17, 20 and surviving invariant 5. Helper locations: `observeFrontier` (line 105),
`declareClosedThrough` (line 120), `missing` (line 141), `intervalDeterminacy` (line 165).

| Proposition | What establishes it | What it does not establish |
|---|---|---|
| Every currently known event through b was processed | An exclusive controller's successful bounded-advancement exhaustion procedure | No later input; complete delivery; observed time b |
| Supported observation time reached b | A genuine observation at that supported frontier | All events at b processed; all supported events captured; no fault |
| No more inputs will affect [a,b) | Controller commitment with scope and provenance, or monotone runtime time already beyond that interval | Already-scheduled work processed or captured |
| Pending internal work through b is exhausted | Advancement stopping-condition evidence; count exhaustion alone is insufficient | Full supported delivery or observed clock reaching b |
| Required supported changes are held | Basis plus gap-free evidence through a frontier bound to the relevant execution boundary | No future changes inside an otherwise open interval |
| The interval claim is historically final | Sufficient state/change coverage and a valid temporal finality basis | Reproduction, analytical applicability, fault-free success or completeness of command history |

### Closure preconditions and stale frontier — QUALIFY; unqualified helper sufficiency falsified

The smallest decisive counterexample is the new
`honestAdvancementDeclarationWithStaleCaptureFrontierLicensesWrongIntegral` probe:

1. One machine, one five-tick processing step, one unit. Capture the initial observation and the
   two supported submission/dispatch events, with frontier cursor 2 at time 0.
2. The controller successfully calls `advanceUntil(100, Long.MAX_VALUE)`. All internal work
   is finished at 5 and it will issue no further command. Do not yet deliver the final drain or
   final observation to the account.
3. `declareClosedThrough(100)` is truthful under its documented controller preconditions, but
   is not tied to a final supported cursor. `missing()` sees no gap through the stale frontier.
4. The helper licenses `[0,100)` and reports **100 active-job-ticks**, although the completed
   job performed **5**. A fresh frontier reveals the missing tail; attempting to deliver the
   old, already-emitted completion after declaration is rejected as a closure violation.

This is more than a dishonest controller: processing and input promises can both hold while
capture coverage is insufficient. E5's gap arithmetic is correct relative to its given frontier,
but E5+E6 omit the relation between the closure boundary and that frontier. A declaration must
bind to the relevant final supported cursor/basis and declare the covered channel. Delayed
receipt of an already-covered event must not be confused with a newly occurring transition.

The helper is an assumption consumer, not a closure verifier. Its declaration method merely
assigns a `Long`; it cannot establish exclusive control, exhausted advancement, complete capture,
or truthful future-input promises from observations/events. The report discloses controller
ownership, but overstates executable implementation of all E-rules.

### Count exhaustion — SURVIVES as a known distinction; explicit qualification required

`budgetExhaustionAndNoFutureCommandsDoNotEstablishClosure` uses two five-tick jobs. Advance to
9 (supported time 5), then call `advanceUntil(100,1)`. This processes the internal marker at 10,
leaving the second job's authoritative completion at 10 pending. Declaring closure makes the
helper report **100** processing ticks; actual total processing is **10**, without any new
command. The declaration violates its documented processed-work precondition, so this is not
a new falsification of that precondition. It disproves reading “controller promises no more
commands” as sufficient closure and demonstrates that the helper cannot check the promise's
other required part. Promotion must carry both obligations explicitly.

### A sufficient existing-API procedure — SURVIVES

`serializedControllerCanCloseWithExhaustionEvidenceAndFinalCaptureWithoutNewApi` stops command
admission, repeatedly calls `advanceUntil(100,1)` until a call returns fewer than its budget,
then drains and takes a final observation while exclusive control prevents intervening mutation.
It verifies complete coverage through that cursor and only then declares closure. Two five-tick
jobs yield **10** ticks over `[0,100)` although supported time remains 10.

This is a sufficient bounded realization, not a new mandatory API or universal protocol. A call
that faults cannot be counted as successful exhaustion; fault outcomes remain separately
qualified. If the last batch exactly consumes its budget, repeat rather than infer the reason
for stopping. No private next-event query or internal-event interpretation is needed.

### Inclusive promises and half-open claims — SURVIVES WITH QUALIFICATION

`eventExactlyAtUpperBoundaryDoesNotChangeHalfOpenIntegral` reaches time 5, computes five ticks
over `[0,5)`, then submits another job at exactly 5. The new equal-time events leave that integral
unchanged. The helper's optional inclusive `closedThrough(5)` rejects those events.

There is no off-by-one error in `IntervalReadings`: it excludes events at b correctly. The
inclusive declaration is deliberately stronger than required by a half-open state-duration
claim. Say so; do not promote it as a necessary prohibition on changes at b. An event at b can
close earlier times even when other events at b remain pending. Same-time sequence order still
matters for state at b and later intervals.

`markerAdvancesInternalTimeButSupportedFrontierConservativelyRefuses` confirms that supported
time may stay at 5 after an internal marker at 10. Refusing `[5,10)` at that point is conservative.
A subsequent accepted command is timestamped 10 and advances the supported frontier; the earlier
half-open interval then has five processing ticks. The report's warning about internal versus
supported time survives; the conservative supported-time rule does not falsely finalize it.

`pendingNoOpMarkerDoesNotPreventSupportedIntervalFinality` leaves a non-authoritative marker
pending at 5 after authoritative completion. The supported observation is already quiescent
and `[0,5)` is final. Processing the marker changes neither observation nor supported stream.
Exhausting all internal events is a sufficient controller discipline, not a necessary fact for
every supported interval claim. Do not equate QUIESCENT with internal queue empty.

### Promises and contradiction detection — QUALIFY

Report case 6b and section 12 say premature closure is detectable. Original case 6b establishes
only detection when a later supported event is actually delivered to this helper. The new
`acceptedNoOpContradictsNoFurtherCommandsWithoutDetectableEvent` declares closure on an idle
runtime, then successfully repeats “machine online.” No event, sequence or observation changes.
The helper cannot detect the broken no-further-command promise.

That no-op does **not** make the state-duration value wrong: distinguish a false input-history
attestation from a false state-history claim. Rejected requests are similarly invisible. Even
an effective contradictory command remains invisible to a frozen account until further evidence
is delivered. Narrow detection to observed contradictory effects; do not promise continuous
truth monitoring or self-validating finality. A consumer relying on controller closure must
preserve the attestation's provenance and trust assumption. A frozen account is final only under
that assumption, unlike a supported-time boundary established by the producer's monotonicity.

## Coverage, delivery and the meaning of “minimum”

Report locations: E5/E8, cases 2–5/13, sections 11–13, invariant 4.

**SURVIVES:** genuine basis observation, run/model binding, increasing supported sequences and
an authoritative frontier detect omitted middle events and missing tails. The original cases
independently reproduced the gap-blind 13-versus-6 occupancy error, incomplete late joins,
cross-run rejection, and split drainers. Neither RunId nor a locally contiguous event list alone
proves whole-run history. Observation at S remains sufficient for the current view even when
earlier history is unavailable. Equal-time order is semantically significant.

Two independent drainers get disjoint deliveries. E5 identifies loss only relative to an adequate
frontier; it cannot infer unknown future tails or validate an unbound closure. One capture holder
can fan out to several consumers today. This is an available mechanism, not proof that every
future use needs one central retained store. Any replay/retention promise remains separately
triggered and scoped.

**QUALIFY:** the helper's global gap predicate is conservative, not a minimum necessary predicate
for every historical subinterval. New probe
`gapAfterRequestedIntervalDoesNotEraseEarlierIntervalKnowledge` first establishes `[0,5)` with
five processing ticks, then loses only events at 10 while advancing the account's frontier.
The helper now refuses the previously established interval. A frozen copy with the original
basis/events/frontier still proves the same five ticks. This falsifies the claim that this helper
implements monotone determinacy under arbitrary later capture loss, not the mathematical fact
that valid earlier evidence remains evidence.

Reconcile coverage per stated evidence boundary/claim; keep a prior validated boundary, segment
coverage, or use an explicit conservative refusal. Never claim all later gaps erase earlier
knowledge. Likewise a claim-specific sufficient observation/aggregate can avoid reconstructing
every past change: the common history-completeness vocabulary must not become a mandatory input
shape for every analytic. The current research substrate already distinguishes missing events
within a selected sequence range.

Handover must retain basis, required events, frontier and any closure attestation with provenance.
The probe's frozen copy keeps these and its guarantee. Keeping only a later observation refuses
earlier history; a raw list stripped of its basis/closure cannot inherit the original guarantee.
The report correctly defers a handover format, but that does not defer the semantic preservation
obligation. No transport or cryptographic mechanism is selected here.

## Identity, provenance and derivation boundaries

### E1–E4 and E7 — SURVIVE with scoped completeness terminology

RunId suffices for this bounded runtime-epoch referent; reset starts another and leaves the old
runtime usable. `(RunId, sequence)` identifies a supported change, not every possible source
record, controller outcome, snapshot or analytical result. No second execution identity is
required by the inspected uses. Capture loss does not mutate or fork the underlying run.

The report correctly separates requests, accepted effects, faults, rejected/no-op commands and
supported changes. Its fault case reproduced through the supported API: the controller sees a
fault while supported state has applied changes and can be quiescent with in-progress work.
This is an already disclosed limitation, not a new reason to reject the report. “Complete
execution account” must therefore be qualified as **complete supported state-change evidence
through a named boundary**. It cannot imply complete conduct, commands, successful processing,
fault-free execution, or evidence sufficient for every Governance use. A passive consumer must
refuse or qualify claims that require the missing outcomes.

The same supported history is not a reproduction script. Original case 12 reproduced different
histories from commands interleaved before versus after an equal-time completion, even when their
recorded simulated acceptance times agree. The controller's session-control script and complete
producing basis remain necessary for that reproduction claim. Reading supported changes never
authorizes replay to become mutable runtime truth.

No new exact Engine-definition ID is required by these same-revision probes. Preserve the actual
repository/build/definition basis in research custody; model fingerprint and RunId are not
substitutes. Cross-revision attribution or later reproducibility must establish the exact basis
and availability they need. Report section 14.3's “still-missing exact Engine definition” must
not be read as a mandatory new field for every current evidence use; section 14.5 and the owning
runtime/Governance contracts deliberately make that requirement claim-specific.

### Waiting and analytical definitions — QUALIFY section 15.1

The report classifies “waiting intervals (readiness to dispatch, per job and step)” as an
execution-fixed reading. That is safe only with a named primitive meaning, such as residence
between supported waiting entry and dispatch, including the observed state basis for a late join.
The word “readiness” is otherwise ambiguous.

The new waiting probe uses CUT 3 then BAKE 4, two units, with a two-slot Oven offline until 6.
The first unit finishes cutting and enters waiting at 3, then dispatches at 6. Its waiting-state
residence is **3 ticks**; waiting while an eligible resource is online is **0 ticks**. After
recovery one idle slot coexists with queued eligible work. Both interval definitions can be
truthful over the same facts; neither fixes a starvation or causal verdict. This is not merely
a grouping choice. Name primitive Engine state/event meanings and keep availability-conditioned
readiness, eligibility conventions and attribution with named analytical definitions.

Processing residence between supported dispatch and completion remains an unambiguous current
primitive. In-progress work from a late basis has a known bounded residence but not necessarily
its original start time. Do not upgrade a complete bounded interval into an exact whole-step
duration without its start basis. Original case 6 correctly demonstrates that distinction.

Utilization, occupancy denominators, population, starvation caps, idle characterization,
bottleneck inference and intervention claims remain definition-dependent. Original cases 7–8
reproduced the differing slot-utilization, machine-busy and refusal outcomes over the same
execution evidence. This supports the separation, not any formula selection.

## Ownership, alternatives and downstream effect

Report locations: candidate sections 7–8, sections 13–17 and self-challenge 1–2.

**Owner placement SURVIVES as a bounded architectural recommendation.** Engine defines supported
facts, sequence, time and epoch meaning. A common coverage vocabulary should preserve those
meanings. Capture holders attest what they possess; controllers attest admission/advancement;
analytical definitions specify sufficiency and interpretation. Describing these obligations in
the existing runtime contract does not turn the Engine into their attesting actor.

**Strongest surviving counterargument:** a rigorously specified caller-capture contract can obey
all producer invariants without the runtime document owning every rule. The brief's bounded
semantic alternative also need not allocate an entity, identity or terminal state. Therefore
the report's literal C1 counterexamples falsify bad local completeness rules, not every caller
contract; C2/C3/C4 are not universally impossible. “No legitimate alternate truth” constrains
semantics but does not prove a unique organizational owner or document location.

This counterargument does not materially change the recommended boundary: that disciplined
caller contract is the same producer/capture split the surviving hybrid needs. Existing Engine
ownership, the already common runtime contract and no demonstrated new responsibility favor
putting its cross-consumer invariants there. State the placement as recommendation with that
reasoning, not as an ownership theorem proven by occupancy errors. Treat a retained service as
unjustified now, not falsified for future requirements. Several accounts can share a run without
being one entity or requiring one database.

The paused Phase 2 Rule 7 text does assign capture to the definition owner. This report usefully
exposes that definition ownership does not imply physical custody: a stateless shared definition
can consume a supplied, appropriately qualified evidence bundle. Its contract may require the
caller to capture evidence without the definition implementation driving or draining the runtime.
Shared definition ownership can also coexist with capture in one component when useful; it is
not categorically forbidden. The required downstream change is to separate those responsibilities,
not to transfer all evidence to another service.

Analytics must consume the qualified coverage vocabulary while continuing to own method-specific
sufficiency, accumulation, definition identity and result provenance. Controller outcomes and
closure attribution remain producer/control provenance. A downstream definition can refuse a
fully covered state-change account because it lacks a fault outcome or a required start point.
This review neither adopts the paused admission rules nor resolves its shared-owner triggers.

Governance owns evidence identity/use/applicability and conformance; a closure declaration is an
attributable assertion, not truth established by its recording. Operational continuation remains
a durable accountable accepted set, with different equality, loss and lineage rules. No reset,
simulation capture loss or sequence cursor should acquire Operational fork semantics. These
boundaries survive unchanged.

Spatial compatibility is a **reasoned specification check only**: the current runtime refuses
spatial content, as the original test confirmed. Specified transfer start/completion and
mid-transfer observation fields fit the same ordering/basis distinction. Zero-duration transfer
still needs same-time order; transfer, waiting and processing residence must stay distinct.
Recheck with executable evidence when that behavior lands. No executed transfer proof is claimed.

## External-source verification and scope

Rechecked primary sources on 2026-10-09:

- [Apache Kafka 4.3.X consumer configuration](https://kafka.apache.org/43/configuration/consumer-configs/#auto.offset.reset):
  missing/expired offsets have several configured responses, including automatic reset and an
  exception option. This supports an explicit policy boundary. It does not support an unqualified
  claim that every truncation is surfaced as an application-visible failure; the report's wording
  should be narrowed. Kafka's retained distributed log is a contrary custody context, not proof
  that Arcogine must own one.
- [OpenTelemetry Trace SDK](https://opentelemetry.io/docs/specs/otel/trace/sdk/), readable-span
  dropped-item counts: exporters can receive counts for collection-limit losses. This corroborates
  explicit loss accounting. It does not prove completeness of an entire sampled trace or account
  for every transport/export failure.
- [OpenTelemetry Trace API](https://opentelemetry.io/docs/specs/otel/trace/api/#end): ending a
  parent span does not end its children, and ended spans can still parent later work. This is a
  useful counterexample to inferred global completion, not an Arcogine closure or ownership rule.

IEEE 1849/XES normative clauses were not independently accessed. The original report already labels
them non-load-bearing unverified background; this review assigns them no evidentiary weight.
No external analogy chooses the owner. The transferable claim must remain scoped to a producer
with trustworthy total order, monotone time and known delivery/control assumptions. Sampled
telemetry, set-ordered Operational records and shared durable logs do not inherit E1–E8 unchanged.

## Minimum consequences before durable reconciliation

1. **Rewrite E6's closure branch with explicit obligations.** Bind the controller's scoped
   attestation to RunId and a post-advancement supported frontier; establish successful exhaustion
   of relevant pending work rather than count-budget exhaustion; retain every required change
   through that boundary; state which later inputs/effects are excluded. Distinguish controller
   assertions from runtime facts and include provenance/trust on handover. No new public API is
   required by this evidence.
2. **Narrow detection and finality claims.** Contradictory delivered effects can be detected;
   absent delivery, rejected requests and accepted no-ops cannot. An inclusive no-further-events
   promise is stronger than half-open finality. A valid immutable earlier boundary survives later
   capture loss; the helper's blanket refusal is conservative, not minimum coverage semantics.
3. **Scope “complete.”** Name supported state-change coverage, basis, range and frontier. Keep
   command/outcome completeness, successful execution, analytical sufficiency and reproducibility
   separate. Do not promote the original helper or its unqualified implementability claim.
4. **Qualify the derivation and ownership language.** Define waiting-state residence precisely;
   retain alternative readiness/availability/attribution meanings in named definitions. Admit the
   disciplined caller-contract alternative as semantically equivalent, and describe Engine
   placement as the smallest justified recommendation rather than a theorem. Narrow the Kafka
   analogy; preserve the fault and provenance limitations already disclosed.
5. **Carry these into downstream work.** Split coverage semantics, controller/holder warranties,
   method sufficiency and custody in the resumed analytics discussion. Preserve Governance and
   Operational boundaries and future retention/spatial/exact-reference triggers. Do not create a
   separate closure research question or infer that analytics owns all history.

The review supplies both falsifiers and a positive sufficient closure procedure, so no additional
research cycle is required **if these exact qualifications are incorporated**. A materially
different closure guarantee, independent passive finality, new retained owner or stronger
cross-revision claim needs new evidence and applicable independent review. A changed report
remains a different revision; this disposition never silently transfers to it. Durable
reconciliation still requires its normal independent PR review and knowledge-transfer audit.

What survives is the minimum owner/identity/custody separation and the executed runtime facts.
What is falsified is unconditional helper sufficiency, universal detection of closure violations,
and helper-level monotone determinacy under later gaps. What needs qualification is precisely
specified above. No unresolved evidence requirement blocks that bounded qualified result.

**Independence:** fresh isolated reviewer session; no responsibility for the original conclusion.

**Disposition: ACCEPT WITH QUALIFICATIONS.**
