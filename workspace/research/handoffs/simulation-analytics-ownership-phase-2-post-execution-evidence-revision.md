# Research handoff — revise analytics ownership Phase 2 against reconciled execution evidence

## Role and precise question

Work as an Arcogine **Researcher in standard-investigation / report-revision mode**, in a fresh session,
under the live repository's `AGENTS.md`, `.github/agents/researcher.agent.md`, and
`docs/development/researching.md`. This is the **already-admitted Phase 2 continuation**
of the single `READY` simulation analytics ownership question, **not** a new investigation,
a reconciliation PR, an implementation task, or an independent adversarial review.

**Question to settle in this revision:**

> Given the already-reconciled consumer-neutral execution-evidence contract and the corrected
> Engine session/marker semantics, which parts of the earlier staged, kind-specific
> analytics-ownership recommendation still survive? What must change about evidence
> custody, measurement sufficiency, current-field classification, and Game-derived demand?
> Does any reusable consumer-neutral analytics responsibility become justified **now**,
> and what minimum Engine/definition/consumer boundary should be recommended for later
> independent review and durable reconciliation?

Start with the current Phase 2 report as the candidate being revised. Its proposed
staged-admission rules are **not adopted architecture** and have **not yet been independently
reviewed at their exact revised SHA**. Explicitly distinguish the previous adversarial
`REOPEN` on the older report from the unreviewed subsequent revision.

**Desired output:** a complete, decision-quality revised Phase 2 report committed at a
**new exact SHA**, followed by a concise submission of that SHA and path for a **separate,
genuinely independent adversarial Researcher**. Do not issue an adversarial-review
disposition on your own revision or start a new broad research cycle.

## Repository, ownership and immutable inputs

- Repository: `alaiba/arcogine`.
- **Existing evidence workspace:** `workspace/simulation-analytics-ownership-phase-1`.
  Reuse it. Do not create a parallel analytics branch or a separate research question.
- This handoff: `workspace/research/handoffs/simulation-analytics-ownership-phase-2-post-execution-evidence-revision.md`.
- Intended **new report file**:
  `workspace/research/investigations/simulation-analytics-ownership-phase-2-post-execution-evidence-revision.md`.
  It must be a **full standalone conclusion** with changes traceable to predecessor evidence,
  not merely a delta or a silent overwrite of an immutable reviewed/handed-off revision.
- At handoff preparation, live `main`:
  `a80cd6bf83d478dedb7a782143f93674cb5019e0`
  (Engine R-remove PR #465, `4c4bcdbaa4f3f990b6e54b95a03373c48cd5dac8`;
  execution-evidence reconciliation PR #466,
  `a80cd6bf83d478dedb7a782143f93674cb5019e0`).
  Re-resolve `main` at investigation start and again before completing the report.
  Treat this as historical grounding, not a promise about the future base.
- At handoff preparation, analytics workspace head:
  `75736dddfa3fe1ae89c5089720c702f4ca595ab5`.
  Never substitute branch tip for an evidence artifact's exact commit.
- The **Phase 1 qualified evidence checkpoint**:
  `7e72a46506bf63febcb57d7b7dcc9ce0cd484d64`,
  `workspace/research/investigations/simulation-analytics-ownership-phase-1-current-boundary.md`.
- **Original Phase 2 report**, immutable:
  `6a5c043563e3bd89fae6826f830b377b43643d46`,
  `workspace/research/investigations/simulation-analytics-ownership-phase-2-admission-boundary.md`.
- **Independent review of that original Phase 2 report**, disposition **REOPEN**:
  `2c85fe7c03c6256c9e8a2ce73fde5091b3357e3c`,
  `workspace/research/investigations/simulation-analytics-ownership-phase-2-adversarial-review.md`.
  Its material findings must be tracked and demonstrably resolved or qualified.
  It did **not** review the later revision.
- **Subsequent revised Phase 2 report, not independently reviewed**:
  `75736dddfa3fe1ae89c5089720c702f4ca595ab5`,
  `workspace/research/investigations/simulation-analytics-ownership-phase-2-admission-boundary-revision.md`.
  This is the primary candidate to stress-test and update against the new authorities.
  It proposes rules 1–8 and a staged D-plus-E owner choice.
- **Landed execution-account conclusion**:
  `docs/research/investigations/simulation-execution-account.md`,
  `docs/architecture/runtime-contract.md#captured-execution-evidence-and-interval-determinacy`.
  For provenance only, the underlying report is
  `a53778145f3ee9941a4b46c7f04f1ec17e6bff86`
  and its first independent review is
  `57d87fc4c8ba19927bf50efa4097321208811d5a`,
  on `workspace/simulation-execution-account`.
  The Engine-adaptation follow-up
  `9551c0cee42bfdeab07f4a55f931eb10ec5fba4e`
  and its independent review
  `e3a4f9a223b982b341a71c2acf7b74146620e9f2`
  are supplementary evidence; **live canonical contracts now own the semantics**.
  Do not reopen E1–E8, R-remove or closure API design without a new concrete falsifier.
- **Game diagnostic use and qualifications**, consumed as **research evidence**:
  report `4ad00136441fa29e6b462ac5431b60cf2de1e148`,
  `workspace/research/investigations/factory-design-game-diagnostic-evidence-report.md`;
  independent review `6484795ff4c4ad971e06c70751b03805e54a55f5`,
  `workspace/research/investigations/factory-design-game-diagnostic-evidence-adversarial-review.md`,
  on `workspace/factory-design-game-diagnostic-evidence`.
  Their qualifying evidence supports concrete Game questions, not a landed playable
  implementation or an already-established shared analytics owner.

**Immutable custody:** keep all handed-off reports, pre-registrations, earlier adversarial
reviews and experiment commits reachable. Append commits; do not amend/rebase/force-push
past evidence SHAs. Preserve the same finite analytics workspace through any later
review and reconciliation. If safe history-preserving synchronization is unavailable,
record `EVIDENCE PERSISTENCE BLOCKED` rather than rewriting evidence history.

## Mandatory live-grounding and quick docs search

At the new live-main SHA, read:

1. `AGENTS.md`, `.github/agents/researcher.agent.md`,
   `docs/development/researching.md` (especially implementability versus suitability,
   evidence categories, proving cases, stopping rule, high-risk review and custody),
   `docs/development/testing.md` (research-experiments procedure).
2. `docs/research/research-register.md`,
   `docs/research/investigations/simulation-analytics-consumer-boundary.md`,
   `docs/research/investigations/simulation-analytics-evidence-provenance.md`,
   `docs/research/investigations/simulation-execution-account.md`.
3. `docs/architecture/runtime-contract.md` — **captured execution evidence,
   claim-specific coverage, half-open interval finality and responsibility placement**;
   `docs/architecture/engine-semantics.md` §§1.1–1.2, 4 and 10–10.2 — **R-remove,
   event-count/command-time behavior, performance result arithmetic and accumulators**;
   `docs/architecture/overview.md` (Determinism Contract, attribution/domain
   authority and headless bottleneck claim);
   `docs/architecture/factory-design.md` (verification objective, prospective
   utilization use) and `docs/architecture/governance-evidence.md` (producer and
   evidence-use provenance).
4. `docs/product/charter.md` (core capability admission by actual need);
   `docs/planning/factory-design-game-consumer.md`,
   `docs/planning/factory-design-game-vertical-slice.md`,
   `docs/research/investigations/factory-design-game-diagnostic-evidence.md`,
   and applicable Engine readiness/spatial planning where the current-field analysis
   already requires them.
5. Current source and tests for `FactoryRuntime`, `FactoryHandler`,
   `RuntimePerformanceObservation`, `ResourceObservation`, `MachineView`,
   `busyTicks`, `completedSalesValue`, `averageLeadTime`,
   `throughputPerTick`, `combinedQueueDepth`, supported events,
   `latestEventSequence`, authoritative state/command outcomes and advancement.

Do a **quick search in `docs/`** for analytics owner, utilization,
starvation/idle-flow, occupancy, KPI, evidence completeness, capture/retention,
basis/frontier, E5/E6, R-remove, `busyTicks`, Game diagnostics and analytics
provenance. Then inspect material source/test matches. Use exact live files,
not an older report's line numbers, PR description or memory, for every
statement about **current** code and current accepted semantics.

Only after grounding in the live owners, read the historical Phase 1, original
Phase 2, its `REOPEN` review, the unreviewed later Phase 2 revision and the
Game report/review at their immutable coordinates. Identify which live
changes since the revised report's baseline `306205c33ef93f0433a26c92f85cbd82eadd27f8`
are material; in particular PRs #465 and #466, not just unrelated test updates.
Carry a baseline-drift table into the report.

## Essential semantic correction: definition ownership is NOT evidence custody

This is the most important newly landed constraint, and it must be tested rather
than patched with an editorial sentence.

The unreviewed Phase 2 revision §10 **Rule 7** says:

> Evidence custody follows the definition owner.

The now-canonical `runtime-contract.md` §Responsibilities and limits says:

> The evidence holder owns capture, retention and coverage qualification.
> An analytical-definition owner specifies method-specific sufficiency and
> computation; definition ownership does not imply physical custody.

Consequently, **do not repeat Rule 7 as a universal owner rule** or assume
that transferring a method's owner automatically transfers its raw captured
history. Reconstruct the roles explicitly:

- **Engine/Factory producer:** authoritative run, state, ordered changes,
  current observations, producing-model binding and supported event cursor;
  result-affecting interpretation and any supported declared result.
- **Session controller:** admission, commands, advancement interleavings,
  command/fault outcomes, any attributed no-future-input closure claim.
- **Evidence holder/capture delegate:** retained observation basis,
  supported-event ranges, independent proof frontier, sequence gap checks,
  relevant retention horizon and handover evidence.
- **Analytical-definition owner:** named method, basis and denominator,
  applicable supported inputs, completeness requirements, refusal conditions,
  computation and interpretation, plus analytical result provenance when needed.
- **Consumer/presenter or Governance verifier:** use, applicability,
  decision/evidence evaluation; neither automatically owns the above roles.

These can be implemented together, but they are **not identical authorities**.
A method owner may **obtain** sufficient evidence from an exclusive driver,
a capture holder or an accepted future delivery contract. A stateless shared
method need not drain a live runtime or retain the history. Conversely, a game
driver may capture once and supply more than one definition. Ownership of
physical custody must be justified separately by requirements, not inferred
from the method's destination.

Check the resulting implications for old Rules 3–5, 7 and 8:
- Can consumer-local measurement stay the default without the game becoming
  a second Engine or mandatory universal evidence owner?
- Can method meaning remain unchanged on promotion when method custodianship
  changes but the controller/holder remains the same? Name the portability
  preconditions: declared inputs, basis, proof sequence, provenance and trust.
- Do the previously claimed "one-way/no-rework" and future shared-owner
  admission benefits still follow, or were they predicated on conflating
  definition ownership with capture?
- Does a method-specific result require command-outcome completeness, a
  no-future-input controller attestation, just supported state-change
  completeness, or no historical claim at all? Different cases may differ.
- Does "fresh observation" contain enough input for the specific result,
  or is a complete interval/window and valid finality proof needed?

Retain the conclusion that Engine does **not** become event sourced and has
no new central retention obligation merely because analytics consumes
execution evidence. Do not add an account identifier, holder class, universal
report record, analytics database, shared broker or extra module by default.

## E5/E6 proving cases that change analytical sufficiency

Use the actual accepted `[a,b)` rules, not an earlier heuristic:

- **Basis:** the supported observation at `(S0,T0)` must precede or be at
  the interval's start, with the relevant initial state.
- **Coverage:** supplied supported changes must be gap-free through the
  **claim's proof sequence**, not necessarily all subsequent changes.
  A locally contiguous capture without an authoritative frontier can
  have an undetectable missing tail.
- **Producer-supported finality:** a genuinely held first supported change
  at time `>= b` witnesses `[a,b)` with the prefix through `K-1`,
  or an observation at time `>= b` with adequate cursor coverage.
- **Controller-attested finality for idle tails:** successful exhaustion of
  relevant work; a post-advancement `(RunId,S*,T*)` bound to complete
  capture; attributed, scoped controller trust against future effective
  input inside the interval. Quiescence, the requested deadline and a spent
  count budget are not sufficient.
- **Boundary behavior:** an event exactly at `b` does not automatically
  invalidate `[a,b)`; same-time supported sequence still orders state.
- **Claim-specific preservation:** a later missing range does not invalidate
  an earlier proved interval. A new method may nevertheless require a
  larger interval and refuse.
- **Different completeness classes:** supported state changes, full
  command/fault history, fault-free completion, producer definition,
  analytical applicability and deterministic reproduction remain distinct.
- **Driver versus passive claimant:** a late join or non-driving verifier
  must not infer a full interval from a current observation or an
  unauthenticated closure statement.

**Mandatory discriminators** for the revised conclusion:

1. **Stale frontier (five versus 100):** a truthful controller finishes a
   five-tick job before `b=100`, but the holder missed the completion;
   a 100-tick activity integral must **refuse**, not report 100.
2. **Valid subinterval / later gap:** an established `[0,5)` result survives
   evidence loss after 5, but a larger interval requiring the missing
   history is refused or qualified.
3. **Late-join utilization/occupancy:** with only a fresh observation, show
   which execution-fixed aggregates remain reconstructable and which
   histories cannot be inferred without capture, producer projection or
   further declared contract. Reuse prior B/C/C′ evidence only in its
   original *unchanged-input* scope.
4. **Game-defined period and flow:** per-slot utilization and starvation
   characterization must declare basis, period, availability treatment,
   idle with eligible work queued, known-work horizon, cap, pool/eligibility
   grouping and any inference refusal — without attributing an unobserved
   Engine decision to a resource.
5. **Unfinished work crossing the upper boundary:** distinguish completed-
   step counters from running occupancy; prove the scope/coverage needed
   to count time resident in an interval correctly.
6. **Method promotion/custody separation:** one driver supplies a proved
   interval to consumer-local and shared-method candidates. Does the
   named definition yield the same result under the same inputs *without*
   transferring capture ownership or widening its trust claim?
7. **Command/clock sensitivity:** use the corrected R-remove Engine
   contract: no inert marker-only budget steps are scheduled in the
   current Factory session; every processed scheduled step publishes at
   its own time, and after a session call current command time matches
   supported observed time *under this current scheduling rule*. A target
   time is not a clock-advance or a finality witness. Check any proposed
   throughput period/window inference and mid-run Game interleaving
   against this actual contract, not the former hidden marker artifact.

Hand-derive expected outcomes or refusal for each new/materially affected
case before running a probe. Rerun **only** existing research experiments,
selected Game fixtures and accepted conformance cases needed to verify
semantic drift; do not blindly repeat the whole original investigation.
Add a small research-local experiment only when an outcome can genuinely
distinguish a surviving ownership alternative. Distinguish current-code
execution from specification-derived proposals, and report command/test
environment, counts, failures and limitations exactly. Follow the
documented `research-experiments` workflow; no production modifications.

## Reevaluate the full candidate set, including a real Engine alternative

Do not merely carry forward the old "D refined by E" winner and adjust Rule 7.
Re-test all serious alternatives from §9 of the existing revised report against
the current runtime/evidence guarantees and the concrete product demand:

- **A: Engine-rich standardized measurement** when an admitted contract
  needs one Engine-wide definition. Can it truthfully provide the method
  with no hidden retention, or does it choose a contested denominator?
- **A′: Engine-produced execution-fixed aggregate** (for example cumulative
  occupied slot-ticks) supporting fresh-observation/late-join uses. Test
  the exact need, semantics, cost and alternative holder capture.
- **B: outside-Engine measurement as a universal rule.** Challenge the
  universal ban against bona fide Engine-meaning/admitted support needs,
  while preserving its current limited successes.
- **C: shared analytics owner now** (not necessarily a module), including
  an actual *single* concrete core verification/governance requirement if
  one exists; do not require two shipped consumers as a precondition.
- **D: staged, concrete-demand admission**, with explicit triggers.
- **E: kind-specific hybrid** separating authoritative facts and
  execution-fixed aggregates from named measurements/characterizations,
  causal inferences and comparisons.
- **Any smaller viable decoupled design** where one capture holder supplies
  evidence to independent, stateless analytical definitions. Check whether
  it changes owner placement or merely implementation custody.

The "implementability versus architectural suitability" rule is now
normative. Evidence that a consumer can calculate a number from present
events establishes feasibility, **not** that the consumer is the right
semantic guarantor. Conversely, a possible Engine-provided measurement is
not a requirement. Compare producer versus consumer ownership based on
which invariant each can guarantee, interface/support burden, duplication
across real consumers, opportunity for common methods, and
definition-change compatibility.

For each candidate, say **falsified**, **viable but not currently admitted**,
**viable with a concrete compatibility/custody cost**, or **selected**,
and why. Include an alternative-by-case matrix with at least one
counterexample to simplistic Engine-rich and "always consumer-local"
arguments. Avoid treating test execution as proof of an untested
architecture-placement preference.

## Recheck current-field and current-demand truth

**The previous revised report §12 treats a user-selected removal of
`busyTicks` as if its semantic consequences were already resolved.**
That removal was **not landed** at handoff preparation: current
`FactoryHandler` still accumulates it, and `FactoryRuntime` still
projects it on resource views. The R-remove scheduler correction does
not itself remove `busyTicks`.

Separate three states:
1. **Current landed fields and methods**, with exact owner, arithmetic,
   support/projection and provenance;
2. **Owner-selected but separately unlanded change** to `busyTicks`
   (if still applicable on live main), with its own exhaustive dependency
   and compatibility work;
3. **Prospective analytics reclassifications**, requiring the later
   independently reviewed ownership conclusion and deliberate authority
   transition.

Do not mark a current contract ambiguity or field as "removed" before
that code, tests and specification actually change. Check the latest
live branch/PR/merged state instead of assuming the handoff snapshot.
Report a **current state matrix** for `combinedQueueDepth`, queue/state/work
projections, `backlog`, `completedOrders`, `completedSalesValue`,
`averageLeadTime`, `throughputPerTick`,
`FactoryRuntime.throughput(long)`, `busyTicks`, the observation's
time/cursor and Engine bottleneck wording. Preserve any remaining
compatibility exceptions with their *concrete* arithmetic/support and
triggers; do not silently remove existing declared Engine results.

Re-test the Game evidence **without upgrading it to product admission**:
- The validated static recorded prototype is a single concrete consumer
  with two forcing questions: utilization over a selected period and
  idle-flow/starvation characterization.
- The first Game diagnostic report/review constrains those meanings;
  its research-local oracle definitions are not automatically production
  contracts. Reuse remains anticipated unless a second concrete
  requirement names the **same definition**.
- Factory Design's illustrative "maximum utilization ≤ threshold"
  objective is not an implemented/evaluated shared requirement merely
  because a heading mentions it.
- Game diagnostics currently `READY`, playable slice `BLOCKED`;
  the Game's later live-control requirements may select mid-run commands,
  time-targeted input or passive finality. Those are separate Engine
  T1–T3 checkpoints, not automatic grounds to grow analytics.
- Compare a Game-owned named method, a reusable stateless method,
  and a justified Engine-wide defined result. **Do not** force a shared
  owner to appear, but do not turn present single-consumer status into
  a permanent prohibition of sharing.
- Maintain the anti-second-Engine rule: analysis measures actual
  recorded outcomes and can request Engine re-execution for controlled
  counterfactuals; it must not recompute unseen dispatch/cascade choices,
  simulate forward from a snapshot, or claim unobserved causal mechanisms.

## Deliverable, bounded stopping rule and next actor

Create a **new self-contained Phase 2 report revision** at:

`workspace/research/investigations/simulation-analytics-ownership-phase-2-post-execution-evidence-revision.md`

Include all of:

1. Live-main research baseline, final recheck, full workspace evidence
   custody, old/new report and review exact coordinates, and material
   baseline drift (especially #465/#466 and `busyTicks` status).
2. Concise question, risk, candidate set and the previous `REOPEN`
   challenges, with a per-challenge disposition and no inherited
   acceptance for the later unreviewed report.
3. An **old-rule → current contract → revised-rule** table for rules
   1–8, especially the **Rule 7 custody/definition-owner conflict**
   and the effect on Rules 3–5 and method promotion.
4. A rigorously separated producer/controller/holder/definition/consumer
   authority table with how each statement can be demonstrated, and
   explicit provenance/trust boundaries.
5. Candidate-by-proving-case matrix, truthful Engine alternative,
   consumer-local and delayed-shared-owner alternatives, and evidence
   supporting or undermining the recommended current choice.
6. Claim-specific temporal completeness and measurement-sufficiency
   checks, including the stale-frontier failure, valid earlier prefix,
   idle-tail finality, late join, cross-boundary running work and
   a separate controller command/fault provenance column.
7. Updated Phase 1 Q1–Q6 and **current-field matrix**, keeping landed
   state, owner-directed but not yet landed changes, and prospective
   policy separate.
8. Classification of the two **actual** Game diagnostic requirements,
   their qualification limits, shared-owner test and anti-second-Engine
   implications. No new Game implementation or premature game-method
   promotion.
9. Concise downstream decision: whether consumer-neutral analytics exists
   now; whether and on which **concrete** trigger it may later be
   admitted; whether any Engine/runtime semantic change is justified
   by the ownership result; and exact conditions for the
   evidence/provenance, adapter, comparison and Game questions.
10. A bounded "what changed, what survived, what is still open"
    comparison against the revision at `75736dddfa3fe1ae89c5089720c702f4ca595ab5`,
    with confidence per conclusion and explicit falsifiers/reopening
    conditions. Do not rewrite the earlier report.
11. Reusable experiment sources and tests run with exact outputs or
    explicit no-rerun reason. Identify suggested durable contract,
    planning and conformance destinations **without editing them**.
12. One clear answer: `RECOMMEND ADMISSION NOW`,
    `RECOMMEND STAGED/NO SHARED OWNER NOW`, or `MORE EVIDENCE REQUIRED`
    with a bounded, material missing fact. These are **research
    recommendations**, not lifecycle/adversarial-review dispositions.

Remain in standard-investigation mode. **No** production/Engine
code change, canonical `docs/architecture/` or `docs/planning/`
mutation, maintained register edit, analytics module, Game implementation,
new research question, or PR merge. The research register remains
`READY`; the shared analytics evidence/provenance question remains
`CANDIDATE` unless some separate admitted process actually promotes it.

Do not repeat broad literature review or all test suites for its own sake.
Stop when the revision addresses the newly landed constraints and its
candidate selection is decision-quality **for independent challenge**.
If a materially new, unsupported guarantee is necessary, name it as
an unknown rather than fabricate test evidence or expand into another track.

**Commit the completed report and any necessary new research-only
pre-registration/probes** to this same analytics workspace. Register
expected oracle values before executing any **new** discriminating probe.
Never overwrite the reviewed original report or the previous unreviewed
revision at its handed-off SHA. Do not treat a draft commit as final.
Perform a final live-main drift recheck and verify previous SHAs remain
fetchable.

### Submission for separate independent adversarial review

After the report commit is completed, submit it by returning the **exact
branch + report commit SHA + path**, the live-main baseline/final
recheck, the new experiment/pre-registration exact coordinates if any,
and a short summary of what changed and what most needs falsification.
The separate reviewer must be a fresh independent actor/session under
`docs/development/researching.md` §9: independently reconstruct
authorities/candidates before deeply reading the exact new report, then
issue one of `ACCEPT`, `ACCEPT WITH QUALIFICATIONS`,
`MORE EVIDENCE REQUIRED`, or `REOPEN`.

**Mandatory reviewer attack surfaces to identify in the handoff:** whether
the new report truly separates evidence custody and method ownership,
whether E5/E6 sufficiency is claimed without a valid proof frontier,
whether current `busyTicks`/Engine-clock results are misclassified,
whether the Game concrete demand justifies the selected owner and
promotion triggers, and whether the Engine alternative was seriously
compared. The independent reviewer, not this author, must verify
the exact report revision and test any load-bearing ownership claim.

Do not perform the review yourself, assign yourself a review
disposition, edit any already-handed-off review, or preemptively
reconcile architecture. A later accepted/qualified revision will
need its **separate** reviewed architectural/planning reconciliation
and knowledge-transfer audit before the umbrella question can
be marked `CONCLUDED`.

**Return to the user:** existing analytics workspace branch, **new report
SHA and path**, old candidate revision SHA, live-main grounding/final
recheck, any new probe coordinates/results, concise recommendation,
changed dependency/ownership effects, and that **independent review
is now the next action**. Do not paste the full report or this handoff
in chat. Preserve evidence custody and do not merge.
