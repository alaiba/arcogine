# Research handoff — Engine adaptation versus controller-managed interval closure

## Assignment: a bounded, decision-quality Engine-alternative comparison

Operate as an Arcogine **Researcher in standard-investigation mode**, in a fresh session, under
`AGENTS.md`, `.github/agents/researcher.agent.md`, and `docs/development/researching.md`.
This is a **high-risk, explicitly coupled follow-up** to the existing simulation execution-account
investigation. It is *not* an implementation request, a PR review, or a new generic Engine-roadmap
exercise. Use the existing execution-account evidence workspace; do not create a separate
research-register question by default.

**Research question**

> For trustworthy completion of supported execution evidence over a simulated-time interval,
> should Arcogine retain the independently reviewed controller-managed closure protocol, or
> change the Engine's supported session-control/observation boundary to offer a simpler or
> stronger guarantee? If an Engine adaptation is justified, what is the minimum semantic
> operation, observable evidence, ownership and compatibility consequence?

The user-originating concern is not merely whether the current API *can* express correct
closure. It is whether the research has over-optimized consumers around today's Engine code
rather than seriously examining whether a modest Engine change would make the system safer,
simpler, and better suited to future uses.

**Do not equate "possible with the current API" with "the current API is appropriate."**
Conversely, do not equate "an Engine API could encapsulate steps" with "the Engine can
truthfully attest that no future inputs will affect an interval."

The outcome must be one of:

1. retain controller-managed closure **for defensible comparative reasons**, with exact
   invariants and triggers that would change the choice;
2. recommend a **bounded Engine-side advancement/evidence primitive** without claiming
   it alone establishes finality;
3. recommend an **Engine-enforced interval closure/input-watermark/seal guarantee**, with
   its actual admission and time semantics specified;
4. recommend another smaller hybrid or explicitly supported alternative;
5. state **MORE EVIDENCE REQUIRED** for a named discriminating fact if the evidence cannot
   decide the boundary honestly.

These are possible conclusions, not preselected outcomes. No new module, durable history,
event sourcing, generalized session framework, Engine clock redesign, or central custody is
assumed.

## Exact repository and evidence coordinates

- Repository: `alaiba/arcogine`.
- Existing workspace: `workspace/simulation-execution-account`.
- Live `main` at **handoff preparation**, not a substitute for fresh grounding:
  `912b2ac85cefab3907fc562a3aaa6507e51975a7`.
- Original execution-account report, immutable commit:
  `a53778145f3ee9941a4b46c7f04f1ec17e6bff86`,
  `workspace/research/investigations/simulation-execution-account-report.md`.
  Original report baseline: `79b3399149499f2f337206c909a277f66a5befd5`.
- Independent adversarial review, immutable commit:
  `57d87fc4c8ba19927bf50efa4097321208811d5a`,
  `workspace/research/investigations/simulation-execution-account-adversarial-review.md`.
  Disposition: **ACCEPT WITH QUALIFICATIONS**. Review live-main baseline:
  `912b2ac85cefab3907fc562a3aaa6507e51975a7`.
- Independent review's pre-review reconstruction:
  `ba6db2e35f25150624a53ae900c48d15757b78fb`,
  `workspace/research/investigations/simulation-execution-account-review-reconstruction.md`.
- Independent new probes:
  `928fb86665016bc94d5979e18f1ce5b2ebef3b57`,
  `workspace/research/experiments/simulation-execution-account-review/com/arcogine/research/executionaccount/ExecutionAccountAdversarialTest.java`.
- The earlier Engine session/advancement ownership note is **already landed** in
  `docs/research/investigations/engine-evolution.md#sessionadvancement-evolution` by PR #462.
  It distinguishes driver batching from Engine's pre-execution time guard but **does not**
  resolve historical interval closure or admit an API redesign.

Do not let a branch tip replace an exact evidence coordinate. Resolve live main and the active
workspace head independently at the start, compare their material differences against the
report/review baselines, and perform a final live-main recheck before reaching conclusions.
Preserve all handed-off original report, review, pre-registration and probe commits. No rebasing,
amending, force-updating, or overwriting immutable handed-off evidence.

If a necessary original artifact is unavailable, disclose the blocked coordinate. Do not
reconstruct a lost review/report from this prompt and present it as checked evidence.

## Why this follow-up is justified — and what is already known

The independent review is **not being reopened just because an alternative seems attractive**.
It establishes useful facts that this comparison must preserve or directly falsify:

- The existing controller can, with exclusive admission control, process relevant internal
  events through an upper time bound, capture/drain supported changes, obtain an authoritative
  final observation, verify the frontier, and make an attributable closure declaration.
- `advanceUntil(b, maxEvents)` is inclusive in event time; a count-budget stop does **not**
  establish successful exhaustion. It does not move the simulation clock to `b`.
- The supported observed time may lag internal scheduler time after internal markers that do
  not generate supported state changes.
- The original research-local `CapturedAccount` helper is **insufficient as a guarantee**:
  a truthful completed-advancement declaration with a **stale captured frontier** licensed
  an incorrect 100-tick reading for a job that ran for 5 ticks.
- Rejected/accepted-no-op command outcomes may be invisible in supported events; detecting
  contradictory *delivered effects* is not verifying every controller promise.
- A later capture gap must not retroactively erase a previously established correct
  historical subinterval, even though blanket helper-level refusal is conservative.
- Half-open `[a,b)` interval claims and inclusive `closedThrough(b)` promises differ.
  Changes at exactly `b` may be permissible for the former.
- Supported state-change evidence completeness is distinct from command/outcome history,
  analytical sufficiency, successful/fault-free processing, and deterministic reproduction.
- The review found no existing consumer requiring central retained history or a new
  execution-account identity; shared coverage meaning and custody/attestation/analysis
  remain separable.

The reviewer demonstrated a sufficient existing-API controller procedure. **That is proof of
feasibility, not a comparative design decision.** The independent review's
`ACCEPT WITH QUALIFICATIONS` applies to the old report revision; it is not independent
acceptance of any new Engine operation, lifecycle guarantee or changed command semantics.

This follow-up deliberately tests the *additional* claim of architectural suitability before
E6 is reconciled. Do not silently reinterpret the review as requiring no further design
comparison or as already approving an Engine change.

## Start-of-run grounding and source map

Read at live `main`:

1. `AGENTS.md`, `.github/agents/researcher.agent.md`,
   `docs/development/researching.md`, `docs/development/testing.md` §10.
2. `docs/research/research-register.md`,
   `docs/research/investigations/simulation-execution-account.md`.
3. `docs/product/charter.md`,
   `docs/architecture/overview.md`,
   `docs/architecture/engine-semantics.md` (especially §1.2),
   `docs/architecture/runtime-contract.md`,
   `docs/research/investigations/engine-evolution.md` (session/advancement).
4. Relevant Governance evidence and Operational continuity contracts, only insofar as
   an Engine alternative could wrongly acquire evidence/operational authority.
5. `docs/planning/factory-simulation-engine-readiness.md` and the current
   analytics-ownership/evidence research briefs, to distinguish admitted current needs,
   planned work, and future hypotheses.
6. Actual `FactoryRuntime`, `Scheduler`, `RecordingScheduler`, supported event/observation
   types, command-outcome types and tests. Trace `advance()`, `advanceUntil()`,
   `observe()`, `drainSupportedEvents()`, `submitWorkload()`,
   `setMachineAvailability()`, and `reset()`.

Quick-search `docs/`, then code and tests, for `advanceUntil`, event-count budget, next event,
internal simulated time, supported observed time, `QUIESCENT`, frontier, closure, `RunId`,
rejected/no-op/faulted command, single drainer, history retention, reset and replay.
Inspect concrete call sites rather than infer broad application demand from JavaDoc. PR #462's
session-advancement note is a distinct boundary observation, not the decision under test.

After grounding in the live normative contracts, read the original report, independent review,
the reviewer reconstruction and executed probe sources at their exact commits. The source
materials are inputs to test critically, not architecture already adopted on main.

## Pre-analysis: reconstruct the problem before selecting an API

Write a short research-local decision framing **before evaluating the preferred candidate**:

- What exact correctness guarantee must `[a,b)` evidence satisfy? Distinguish producer
  events/observations, controller admission and advancement, physical capture, and analytical
  definition sufficiency.
- Which facts can the Engine *know* at a given instant? Which remain promises about future
  controller behavior, not runtime observations?
- Which safety properties cannot be guaranteed by two separate API calls if mutation or
  evidence capture can interleave between them?
- Who needs the guarantee today? Check research-runner, Challenge/Game, Governance, diagnostic
  and future passive-view use cases individually and label them implemented, admitted,
  hypothetical or separately triggered.
- What errors are caused by missing semantics versus a merely cumbersome API?
- Does "final" mean **no pending effect currently at or before `b`**, **no future accepted
  change with time `< b`**, **no future accepted command at all**, **complete captured supported
  changes**, or **a globally terminated runtime**? Do not collapse these into one bool.
- What burden will be imposed on two or three different consumers if closure remains
  caller-managed? Count semantic invariants, order-sensitive calls, trust assumptions,
  repeat code and failure paths rather than merely implementation line count.

Commit the candidate definitions and hand-derived expected discriminators as a distinct
research-local checkpoint if needed for reproducibility. Avoid front-loading a favored API.

## Serious alternatives — test each, improve or replace this set where evidence warrants

**A. Qualified existing-API/controller protocol (no Engine change).**
One exclusive driver stops relevant admission, demonstrates bounded advancement exhaustion,
drains the supported delivery, binds a post-advancement cursor/observation, checks claim-scoped
coverage and records a trusted closure attestation. A shared library or adapter may encapsulate
that policy without Engine API changes. Investigate real complexity: it should be possible to
reuse a protocol without every consumer inventing it, but moving code into a wrapper does not
automatically remove the correctness obligations or establish passive verifiability. Preserve
this as a serious potential winner, not a strawman.

**B. Engine-produced bounded-advancement *evidence* or result.**
For example, a result could expose what was attempted and processed, why it stopped
(event-count exhausted, next event beyond target, no pending work, fault), and a supported
RunId/cursor/time observation boundary. Compare possible atomicity guarantees and scope.
Could it remove the caller's ambiguity around exhaustion and stale frontier without promising
that no later command will arrive? Would it be enough to make the capture holder's evidence
claim sound? Specify which facts are source-authored versus caller attestations. Explore
whether simply enriching `advanceUntil()` is enough versus introducing a new operation.

**C. Engine-owned protected batch/observation/delivery boundary.**
Consider a bounded method or operation that performs advancement and exposes an associated
supported frontier and relevant changes in one coherent operation, without event sourcing or
unbounded retention. Evaluate whether this solves the stale-frontier counterexample by
construction, or merely moves the single-drainer/fan-out and capture problems elsewhere.
Discuss the effect on current `drainSupportedEvents()` semantics, consumer coexistence,
faults, ownership and retention. Do not require an atomic bundle or shared store just
because it sounds safe.

**D. Engine-enforced temporal closure / input watermark / sealing.**
Examine a carefully scoped Engine operation that makes later effective changes inside
`[a,b)` impossible, rather than trusting a driver's future-command promise. This alternative
must explicitly decide:
- whether the Engine rejects further commands that would be accepted at its internal current
  time `< b`, delays/re-times them to `b`, advances an input-time floor, or truly ends a run;
- whether any option changes the deterministic Engine interpretation, command acceptance,
  scheduling behavior or model outcome;
- whether `[a,b)` allows commands/events at exactly `b` and how equal-time ordering works;
- whether an Engine-authored closure token or observation is sufficient evidence without full
  event capture, and how capture completeness remains separately checked;
- whether closure is irrevocable, per interval or per run, and what `reset()` means for it;
- how to handle concurrent calls, old references and command outcomes, including no-ops/faults;
- what capability would be gained by passive verifiers (if any) and whether a current consumer
  needs it.

Compare a bounded interval seal with a global terminal state and with deliberately advancing
the logical command clock to `b`; they are not automatically equivalent. Do not select
clock advancement or terminal state without proving its semantic consequences.

**E. Minimal Engine guard with driver-owned batching.**
The landed Engine Evolution note observes that count budgeting is a driver loop whereas a
time guard needs an Engine-side pre-execution decision. Include a guarded single-event
primitive or similar smallest-operation boundary *only if it actually improves interval
closure* or is an informative negative comparator. A clean advancement API may simplify
pacing while leaving E6's future-input and capture problems unsolved.

**F. Any smaller coherent hybrid identified from independent reconstruction.**
An Engine-produced stopping/cursor fact plus controller-issued closure provenance, or a
lightweight first-class controller-owned evidence-capture facility, may outperform B–D.
Compare based on explicit guarantees, not method naming or preconceived package location.

Evaluate all serious options against the same evidence. A design may be **safe but overbuilt**,
**useful but insufficient for finality**, **appropriately deferred**, or **justified now**.

## Mandatory discriminating cases

Derive expected outcomes from current Engine semantics and hand-check them before executing or
simulating candidate behavior. Reuse previous probes as baseline facts, but add at least one
**new** discriminator designed specifically to separate Engine-adaptation alternatives.

At minimum cover:

1. **Idle tail / later input:** work finishes at tick 13; `advanceUntil(100)` returns
   quiescent at 13; another unit is submitted afterward. What does each alternative
   guarantee, refuse, re-time or label provisional?
2. **Count exhaustion:** stop after one internal event while a due authoritative completion
   remains. Include a budget-zero call and the distinction between an internal no-op marker
   and an authoritative scheduled event. Does any reported stop condition lie?
3. **Stale capture frontier:** the Engine finished a five-tick job before 100 but the account
   retains only the dispatch and stale cursor; its time integral falsely reads 100 without a
   bound final frontier. Which alternative prevents *issuing a false claim*, not merely
   detecting it later?
4. **Historical subinterval preservation:** `[0,5)` was complete; subsequent capture loses
   changes at 10. Can a claim over the earlier interval retain its proven coverage?
5. **Half-open edge and equal-time order:** accepted/effective events at exactly `b`;
   several ordered changes at the same tick; no accidental inclusive promise where
   `[a,b)` does not require it.
6. **Observed-time/internal-time split:** internal no-op marker advances scheduler time
   without a supported event; an external command is then accepted at internal time.
   Is an advertised supported frontier or Engine time floor truthful?
7. **Controller promise versus Engine guarantee:** a no-op or rejected request after a
   "no more commands" declaration, an accepted command that changes state, and a
   `Faulted` post-mutation outcome. What contradictions are observable versus
   only attributable to the controller?
8. **Delivery and shared consumers:** a single destructive drainer, two competing drainers,
   a capture/fan-out holder, and a late observer that requires history from before joining.
   Do not infer a central store is needed unless a present proof requires it.
9. **Reset and identity:** new `RunId`, original runtime still usable, provenance/closure
   applying to the correct run. Never turn `RunId` into an execution-account ID.
10. **Fault during bounded advancement:** partial authoritative mutation, surviving supported
    events, failure/stop reason, command admission, and no accidental declaration of completion.
11. **Temporal provenance / replay:** compare two command/advancement interleavings whose
    supported changes or acceptance times hide the controlling difference; test whether an
    Engine evidence boundary changes reproducibility needs or just interval completeness.
12. **A concrete two-consumer cost case:** model how research experiments and a Game/Challenge
    or passive verification consumer would each implement the required closure obligations.
    Compare complexity and semantics rather than asserting "future consumers" generically.

For a candidate Engine modification, supply at least one **specific** API/contract sketch:
inputs, outputs, state transitions, invariants, rejection/fault behavior, ordering, cursor
guarantees and compatibility changes. Illustrative pseudocode is welcome but **must not be
mistaken for a landed implementation or tested Engine behavior**.

## Comparative analysis and decision criteria

For each alternative state:

- **Truthful guarantees:** precisely which parts of completeness/finality are established
  by Engine facts, which are controller/capture-holder attestations, and which remain
  unknowable without more custody or external trust.
- **Correctness by construction:** which reviewer-discovered failures become impossible,
  detectable, unchanged, or newly introduced? Especially stale frontiers, count exhaustion
  and temporal promises.
- **Consumer burden:** number and ordering of operations, exclusive-control requirements,
  repeated protocol logic, race/partial-failure handling, trust and knowledge every consumer
  must carry, and ease of a common library.
- **Engine burden:** public API and state surface, immutable result types, scheduler exposure,
  admission authority, lifecycle/clock changes, memory/retention costs, determinism and
  conformance obligations.
- **Compatibility:** current `advance()`/`advanceUntil()`, `observe()`,
  `drainSupportedEvents()`, `CommandResult`, reset and existing research callers;
  whether changes are additive meaning-preserving or materially result-affecting.
- **Cross-consumer utility:** supported and likely near-term uses versus speculative
  distributed/passive-verification/future remote use.
- **Reversibility and evolution:** whether adopting a small producer-side guarantee now
  avoids multiplying fragile wrappers, or commits to premature abstractions.
- **Negative knowledge:** scenarios in which the option seems useful but cannot genuinely
  guarantee finality; name its precise failure rather than rejecting it by preference.

Treat no Engine change as a genuine candidate that must **earn** selection through evidence.
Treat an Engine adaptation as permissible while the definition is in development, but it must
earn selection through material simplification or stronger guarantees and accurately account
for semantic changes. Do not silently require backward compatibility at the cost of the
architecture, nor dismiss real compatibility/conformance costs.

Avoid tautological arguments like "the Engine should own it because the Engine can know it" or
"the controller should own it because a controller can program it." Identify the specific
invariant and producer/consumer authority that decides placement. Information insufficiency
does not prove a unique module or owner.

## Evidence / proving method

Use the current runtime implementation and supported contracts as empirical baselines.
The original report's 14 cases and the independent review's nine probes are prior evidence,
not conclusive comparisons of the new candidate designs.

- Verify exact source/test claims and existing results when environment permits. JDK21 and the
  documented Gradle/Docker research-experiment workflow are preferred.
- Define new proving cases with independently hand-derived expectations; implement
  research-local `workspace/research/experiments/` probes if they materially discriminate.
- Do not alter production code, tracked runtime tests, or canonical architecture/specification
  within this Researcher run. A research-local surrogate/prototype may illustrate alternative
  semantics, but label it **proposed behavior**, not executed evidence of a changed Engine.
- If a proposed operation cannot be tested without modifying production source, give a
  precise acceptance-test contract and mark the result specification-derived or unknown.
  Do not penalize an alternative for lacking a production implementation during research.
- Run and report available tests honestly. If a suite was not rerun, state so. Distinguish
  a passing negative probe (demonstrated failure) from positive conformance.
- External references are optional. Use primary/versioned sources only if they materially
  discriminate a design choice; they cannot establish Arcogine ownership by analogy.

Avoid a universal performance optimization, general concurrency framework, live production
telemetry, scheduling-policy redesign, analytical KPI definitions, retained log, security
signing scheme or new execution entity. Such work requires its own concrete trigger.

## Expected decision-quality result

Persist the completed follow-up report, ideally at:

`workspace/research/investigations/simulation-execution-account-engine-adaptation-report.md`

Its structure must contain:

1. Exact research baseline, live-main final recheck and custody coordinates.
2. Bounded question and why it matters despite the prior ACCEPT WITH QUALIFICATIONS.
3. Independently reconstructed invariants and owner/control/holder distinctions.
4. Serious alternatives, including the qualified no-change design.
5. At least one concrete contract sketch for each surviving Engine alternative.
6. Hand-derived proving cases, evidence/test commands and actual results or explicit limitations.
7. A candidate × case matrix identifying falsifiers, not just successful illustrations.
8. Explicit separation of `advancedThrough`, `capturedThrough`, `noFutureInputBefore`,
   `finalityOf[a,b)`, and command/reproduction completeness; use other names if clearer.
9. Comparative caller complexity and Engine semantic/API complexity; state assumptions.
10. A **recommendation on whether to change the Engine now**, leave it unchanged with a
    defined controller protocol, or commit a bounded future revisit trigger — with
    evidence-based reasons rather than mere current implementability.
11. If an Engine change is recommended: the smallest truthful primitive/behavior and
    semantics, compatibility/provenance impact, and precise conformance/negative tests.
12. If no change: which genuine Engine simplification alternatives were rejected, why,
    what costs remain on consumers, and the concrete signal that would reverse that choice.
13. Implications for **E5/E6 and the pending execution-account reconciliation**.
    Clearly distinguish qualifications already accepted in the independent review from
    any new conclusions that need further review.
14. Explicit impact (or non-impact) on the paused analytics Phase 2, delivery custody,
    Governance, Operational continuity and future Engine Evolution.
15. Remaining unknowns, confidence by conclusion, and conditions under which the
    recommendation must be reopened.
16. Which artifacts/fixtures are reusable in a later knowledge-transfer audit, and which
    research-local probes must not be promoted.

If the recommended Engine operation changes the high-risk E6/Engine session-control meaning,
or supplies evidence materially beyond the reviewed conclusion, **require a genuinely separate
independent adversarial review of this new exact report revision before architectural
reconciliation**. The prior review does not confer approval on new semantics. If the result
is demonstrably the qualified existing controller protocol, still explain whether it
introduces any new load-bearing inference that requires independent checking.

The execution-account research stays `READY` until its eventual durable, independently
reviewed reconciliation. Do **not** mark it `CONCLUDED` because this follow-up report exists.
Do **not** resume or reconcile paused analytics Phase 2 as part of this run.

## Persistence, stop boundary and handoff

Write and commit the finished report and any needed research-only probes to
**`workspace/simulation-execution-account`**, preserving handed-off original report/review
revisions and history. Keep the original accepted-with-qualifications review immutable;
do not overwrite it or retroactively change its disposition.

This is a research-only task. No production implementation, no public API mutation,
no architecture or planning reconciliation, no maintained research-register edit,
no PR merge, and no review disposition on your own new report.

On completion return:

- workspace branch;
- **exact report commit SHA and path**;
- any new experiment/pre-registration commit SHAs and paths;
- research baseline and final live-main recheck;
- decisive candidate comparison and recommended current action;
- changed-versus-unchanged Engine contract responsibilities;
- impact on original E6 and the pending reconciliation;
- whether a new independent adversarial review is required, and the exact report revision
  it would need to review.

Do not paste the full report into chat. The exact committed coordinates are the evidence handoff.
