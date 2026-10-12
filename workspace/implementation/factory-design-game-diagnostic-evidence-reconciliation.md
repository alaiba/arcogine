# Implementation handoff — reconcile Factory-design Game diagnostic evidence

## Mission, authority, and exact inputs

Act as an Arcogine **implementation agent carrying out a bounded, research-backed product/planning reconciliation**, not as a Researcher, independent research reviewer, product tester, or independent PR Reviewer. Deliver the minimum durable player-facing diagnostic **requirements and truthfulness guardrails** supported by the existing Game research. Preserve its independent review's qualifications, account for the intervening Engine/evidence/analytics changes, close this one research question when the durable reconciliation lands, and leave playable implementation behind its own admission gate.

**Existing evidence branch:** `workspace/factory-design-game-diagnostic-evidence`. Continue on this branch; do not create another research workspace or overwrite earlier artifacts. This prompt is transient:
`workspace/implementation/factory-design-game-diagnostic-evidence-reconciliation.md`.
Do not merge the prompt or any other tracked `workspace/` material into `main`.

**Exact, immutable sources:**

- Research report **revision 3, consolidated**:
  `4ad00136441fa29e6b462ac5431b60cf2de1e148`,
  `workspace/research/investigations/factory-design-game-diagnostic-evidence-report.md`.
  Prior revisions are historical evidence, not the reviewed decision.
- **Independent adversarial review**, disposition **ACCEPT WITH QUALIFICATIONS**:
  `6484795ff4c4ad971e06c70751b03805e54a55f5`,
  `workspace/research/investigations/factory-design-game-diagnostic-evidence-adversarial-review.md`.
  Its **section 6, qualifications 1–8**, and disposition section 7 bind this transfer.
  It identifies the exact report revision it reviewed; do not apply this disposition to another revision.
- Last report/review-era `main`:
  `306205c33ef93f0433a26c92f85cbd82eadd27f8`.
- At handoff preparation (2026-10-12), current `main`:
  `5775057aac2bd61dd45afdefed0bd49e1f93a98c`.
  Current evidence branch head: `6484795ff4c4ad971e06c70751b03805e54a55f5`.
  The branch is **diverged** relative to current `main` (22 branch-side commits and
  10 `main`-side commits at preparation), **not** an up-to-date implementation branch.
  Re-resolve both exact heads at execution start and before PR handoff; these values
  are preparation coordinates only.
- Four relevant merged changes:
  - **#465 R-remove** `4c4bcdbaa4f3f990b6e54b95a03373c48cd5dac8`;
  - **#466 captured execution-evidence reconciliation**
    `a80cd6bf83d478dedb7a782143f93674cb5019e0`;
  - **#469 removal of cumulative `busyTicks`**
    `a7f9856617d99e796d528a44aa8c15677c485f55`;
  - **#471 analytical-definition ownership**
    `5775057aac2bd61dd45afdefed0bd49e1f93a98c`.

Use the live repository as authority. Read `AGENTS.md`,
`.github/CONTRIBUTING.md`, `docs/development/researching.md` §10,
`docs/development/reviewing.md`, and `docs/development/testing.md`.
This is a **separate reconciliation and PR**, not a reopening of the accepted
research or a fresh report. A genuine independent **PR review** is still required
before merging; the implementation agent neither self-approves nor merges.

## Grounding before changes

Read the *complete* immutable report/review and then the current
authoritative semantic neighbors at the resolved `main` SHA:

- `docs/research/research-register.md`;
  `docs/research/investigations/factory-design-game-diagnostic-evidence.md`;
  `docs/research/investigations/factory-design-game-vertical-slice.md`;
  `docs/research/investigations/factory-design-game-strategy-space.md`;
- `docs/planning/factory-design-game-consumer.md`, especially §§2, 4–7;
  `docs/planning/factory-design-game-vertical-slice.md`;
  `docs/planning/factory-design-game-challenge-readiness.md`;
- `docs/architecture/overview.md#domain-authority`,
  `docs/architecture/runtime-contract.md#captured-execution-evidence-and-interval-determinacy`,
  `docs/architecture/engine-semantics.md` §§1.2, 4, 5, 10–10.2,
  `docs/architecture/factory-design.md`,
  `docs/architecture/governance-evidence.md`;
- `docs/research/investigations/simulation-analytics-consumer-boundary.md`
  (**CONCLUDED**),
  `docs/research/investigations/simulation-analytics-evidence-provenance.md`
  (**CANDIDATE**), `docs/research/investigations/simulation-execution-account.md`
  (**CONCLUDED**), and the relevant deferred Game product-research candidates;
- Source/accepted tests needed to verify assertions about:
  `FactoryRuntime`, `FactoryHandler`, `RecordingScheduler`,
  `ResourceObservation`, `RuntimeObservation`,
  `RuntimePerformanceObservation`, `RuntimeEventEnvelope`,
  `ExperimentRunner`, `ProcessingOccupancyOracle`,
  `FactoryModelSemanticComparator`, Governance `ChangeSet`,
  `SessionControlAcceptanceTest`, `BoundedAdvancementConformanceTest`,
  `HeadlessClosureAcceptanceTest`, and the affected research experiments.

Perform a **quick docs search** for:
`game diagnostic`, `slot`, `starv`, `idle`, `utilization`,
`eligibility`, `waiting`, `bottleneck`, `intervention`,
`busyTicks`, `event-count`, `R-remove`, `half-open`,
`basis`, `coverage`, `frontier`, `controller closure`,
`analytic definition`, `Game-owned`, `CONCLUDED`, `READY`,
and `playable gate`. Inspect relevant matches, not only search snippets.

Use exact historical report/review commits for research claims, exact live
`main` for current-code/contract claims, and explicit `planned` wording for
anything not yet implemented. Do not let a stale research summary override
the subsequent accepted Engine and ownership decisions.

## Reconcile the baseline drift, narrowly

Produce a short internal drift/knowledge-transfer matrix before editing.
The required reconciliations are:

**1. R-remove (#465):** Factory now schedules only supported-authoritative
step-completion turns; it no longer schedules inert start/order-completion
markers. This changes outcomes for some **positive event-budget-stopped,
interleaved-command** scripts. Under the explicitly stated narrower
start-fixed, time-bounded-only scope, supported results and events are preserved.
Existing Game corpus scripts primarily use `advanceUntil(t)` with an
effectively unbounded event count plus start-fixed inputs, so the evidence
should survive *subject to targeted verification*. Do not claim every Game
fixture still passes before running it. A request for tick `t` is **not**
itself an observed-clock advance; check actual `currentTime` on event-free
ticks. Never convert internal scheduler events into diagnostic evidence.

**2. Execution evidence (#466):** Supported observations and ordered
runtime changes have claim-specific proof obligations. Distinguish:
- snapshot fact (fresh same-run supported observation);
- a derived **interval** claim over `[a,b)`, with basis observation,
  RunId/model binding, gap-free changes through its proof sequence, an
  independently known cursor/frontier and valid finality; and
- a **future-horizon** input-exclusion assertion by an attributable
  controller, not a producer fact.

For an idle tail lacking a producer witness at or beyond `b`, closure
may require a controller's scoped no-future-effective-input commitment,
post-advancement bound, complete supported changes through that bound,
successful exhaustion and relevant command/fault provenance.
`QUIESCENT`, `advanceUntil(b)`, a spent budget or an event list alone
cannot seal a period. A change at exactly `b` is outside `[a,b)`.
Capture can remain Game-driver-owned or held by a delegate and does not
by itself give the holder analytical-definition authority. Do **not**
implement a new Engine evidence ledger, validator, closure endpoint or
history service for this reconciliation.

**3. `busyTicks` removal (#469):** The old
`ResourceObservation.busyTicks()` API, the Engine counter accumulation,
the related misleading utilization/bottleneck claim and maintained
conformance references have **already been removed**.
The historical research workspace still references the removed API in
`GameContracts.NaiveDashboard` and in
`GameDiagnosticEvidenceExperiment.handDerivedBusyTicksDivergenceHolds`;
its complete historical source set therefore cannot compile unchanged
against present `main`. Those historical 4/48 and other counter values
remain baseline-specific negative evidence; do not rewrite or amend
the reviewed report/experiments, reintroduce the field or pretend those
assertions still exercise a current API. Port **only** surviving
discriminators into a *separate, transient* research-local compatibility
test/fixture if that will materially verify a promoted requirement.
Compare current maintained counter-removal call sites, the Engine
semantics, `ProcessingOccupancyOracle` and established tests before
claiming an unresolved removal dependency. The old owner directive
`remove busyTicks` is now **fulfilled**, not work for this PR.
Its removal alone is not evidence that generic analytics reuse is needed.

**4. Analytics-definition ownership (#471):** Architecture now permits
**Game-owned named, consumer-specific diagnostic methods**, conditional on
their separate product admission and sufficiency/refusal rules.
A genuinely shared named analytical definition requires a concrete
cross-consumer, verification/Governance, outward-contract or duplicate-drift
admission trigger, **not** two code call sites or one captured stream.
Engine owns its existing declared performance facts and production
interpretation; a Game method must not replay, reinterpret dispatch or
assert unobserved causal bottlenecks.
Physical evidence custody is separate from method meaning.
This decision settles the former analytics ownership blocker but
**does not** admit the research-local `utilization` or
`flow` formulas as production definitions, select a new analytics
module or settle the separate analytics evidence/provenance `CANDIDATE`.

Recheck any main movement during execution and account for material
additional changes; do not generalize this historical four-change list
if the live tree has moved.

## Accepted product verdict and mandatory Q1–Q8 qualifications

Transfer only the **bounded positive** result supported by the
independent review: a mechanically truthful, inspectable diagnostic
surface is **achievable within the studied non-spatial scope**.
One co-designing owner found a text prototype explorable in a
**single-person smoke test**. Do **not** promote population-level
`usable`, `understandable`, `satisfactory`, `engaging` or
`learning observed` conclusions. The third walkthrough had reduced
blinding: the owner co-designed scenarios/missions and labeled
displays answered many missions. Independently demonstrated concept
coverage comprises slot state, starvation, utilization, experiment-based
`needed`, and one co-binding experiment. Bottleneck teaching and
external player comprehension **were not established**. (Q1)

**Guardrails and their demonstrated scope (Q2).** The reviewed
C3b/C3c candidate surfaces and 13-design/24-pair challenge corpus
falsify a naive contract's specific claims. The six `audits`
are keyword/self-declared-facet regression checks, not a universal
truthfulness verifier. Revision 3's flow/utilization characterizations
were checked on **four bakery scenarios** plus a slot-state
consistency check, **not** on the entire 13-design corpus.
Future implementation must derive independent acceptance tests from
durable guardrails, not copy the self-declared audit as authority.

Write the **smallest actionable diagnostic claim-to-evidence contract**
into Game planning, including:

- **Allowed inspection:** published design, observed finish/progress,
  active slot/resource state at a labeled observed time, supported
  waiting by **operation step** (per-machine only when binding is
  unambiguous), per-unit journeys/wait-versus-process intervals and
  machine timelines from supported events or sufficient observations.
  Include present pending multi-eligible work without double-counting or
  silently naming a receiving machine.
- **Exact source and qualification:** every claim is either a direct
  supported fact, a snapshot count, a supported event interval, a
  controlled between-run outcome comparison, or a **named analytical
  definition with declared method, population, period, availability,
  grouping and sufficient evidence/refusal**. A claim stronger than the
  available supported proof is omitted or weakened.
- **Forbidden shortcuts:** busiest/highest-occupancy machine = causal
  bottleneck, most-waiting step = constraint, an idle resource =
  expendable/surplus, shared pending work = multiple machine queues,
  `combinedQueueDepth` = a physical queue, Engine `Busy`
  = every slot occupied, observed throughput over supported time =
  arbitrary caller-window throughput, and a deterministic association =
  an observed cause.
- **Comparison truth:** single-variable variants can show the exact
  controlled change and completion/outcome delta but not an unseen
  mechanism. Two or more changes with confounding/co-binding require
  their **whole change set**, attribution withheld and an explicit
  comparison basis. Single-change `needed/not needed` results
  **do not compose** into a joint-change verdict. `needed` is a
  **time-only** conclusion within the offered variants; finishing
  sooner is not automatically `worth it`. Value-for-money requires
  an admitted named Challenge scoring/evaluation rule. Keep modeled
  resource identity and order visible in changes.
- **Presentation as requirements, not chosen UI:** pull-based
  inspection, opportunities to explore variants, learning goal plus
  explicit answer format, official answer/explanation/how-to-find-it
  and gentle period conventions; do not copy one-off scripts,
  answer keys, fixed mission count or client technology into durable
  requirements. Explicit refusal remains an **internal correctness
  guardrail**, not a mandatory player-facing "cannot infer" panel.
  Prefer presenting an available experiment or omitting an unsupported
  statement.

**Two-layer idle/flow classification (Q3).** Distinguish observed
slot state (working versus idle) from a **named Game-owned**
interpretation of *why a slot is idle*. For the studied **always-online,
one-product, linear-routing, start-fixed** scope, a snapshot can classify
based on published eligibility and all **currently known** unfinished
work. Do not interpret `no currently known eligible work` as a promise
that no further work will arrive. After availability changes, an idle
slot can coexist with a queue for that machine under current Engine
§4 rule 7; never encode an always-online contradiction as a universal
Engine invariant. For broader scope use a **third classification or
explicit refusal** until a named method is validated. Future-horizon
`no work left for the rest of [a,b)` needs an appropriate controller
input-exclusion statement. A period aggregate of idle labels needs
complete time coverage and finality; a fresh snapshot does not prove
that integral.

**Utilization and starvation method boundaries (Q4/Q5).** Treat these
as two **concrete Game measurement demands**, not automatically
existing generic analytics. The report's proposed working-slot-ticks
over available slot-ticks for a half-open period is a *research
candidate*, not a production API/definition. Likewise, starved
slot-ticks and `starved/no work left` are candidate Game meanings.
The product requirement must record the choices that an eventual
**admitted named method** must fix and test:
- interval bounds and whether to count running work through the
  upper boundary;
- slots/concurrency and denominator/available-time policy;
- whether offline time is excluded and what is refused when
  availability changes;
- definition of known unfinished/eligible work, including
  whether starved slots are capped by remaining eligible work;
- pool/step attribution when a machine can serve several steps,
  and whether a step-level pool versus machine-level view differs;
- evidence source: a complete retained supported-event stream,
  or adequately sampled observations, with valid basis, frontier
  and finality; and
- deterministic refusal for inputs/populations/intervals outside
  a method's demonstrated domain.

The previous flow method was **not** validated on multi-eligible
machines or the entire 13-design corpus. Choose a narrower **admitted
research outcome** ("requirements and candidates only") rather than
invent missing proof or silently adopting the specific formulas.
If the immediate non-spatial Game slice needs these exact calculations,
its later implementation admission must specify, independently
validate and accept the chosen scoped method(s) and refusal policy.
In particular, removing `busyTicks` does not automatically
transfer the old Engine semantics to the Game or make future
cross-consumer reuse established. A single-run `bottleneck` method
is optional and **not demanded** by the reviewed product evidence.
Do not create a new general analytics investigation to settle a
method not selected by the product.

**Additional falsification guards (Q6):** For `concurrency > 1`,
a machine-level `Busy` observation need not mean all its slots
are working; use independent slot occupancy evidence or refuse
slot-level claims. Preserve the non-composition of controlled
single-change outcomes and the distinction between production
time improvement, cost and Challenge evaluation. Do not claim
that deterministic replay proves unique causation.

**No imaginary platform gaps (Q7):**
- Per-step timing is available via **supported captured/drained
  runtime events**, or observation sampling adequate to the selected
  claim; do not demand a new Engine timing event/accumulator.
  The controller/holder owns its actual recording horizon; a
  consumer cannot recover pre-join history from a fresh
  observation. Shared delivery/retention/provenance remains
  conditional under the existing `CANDIDATE` evidence question.
- Stable machine identity under Game draft edits belongs to
  the **Game-to-Factory projector's result-affecting
  identity/order rule**. This requires no new Factory
  identity primitive simply because an earlier draft list
  renumbered a resource.
- Confirm current `FactoryModelSemanticComparator` and
  Governance `ChangeSet` semantics (including order changes
  by stable identity) before declaring design difference
  detection an unimplemented platform requirement.

**Counter-removal scope (Q8):** It is already implemented
and reviewed in #469. Document its historical bad-utilization
counterexample as rationale/negative knowledge only, not
current product state; keep the Governance illustrative
verification objective intact. Confirm that the old misleading
carried-load/bottleneck claims have been repaired on `main`.
Do not rerun a now-unbuildable old test as if unchanged, or
re-add `busyTicks` to support a demo.

## Minimal durable destinations and status discipline

Prefer a **documentation-only, small-scope reconciliation**.
Select the smallest set of authority-bearing files and explain
any deliberate no-change decision. Expected destinations:

1. **`docs/planning/factory-design-game-consumer.md`:**
   own the bounded Game diagnostic **product requirement set**,
   classification and evidence/refusal gates, controlled
   comparison and presentation requirements, method-owner
   separation, scope restrictions, and acceptance criteria
   before an admitted Game diagnostic implementation. Keep
   the integration and Challenge/Engine responsibility boundary.
   Do not copy the research report into the plan.
2. **`docs/planning/factory-design-game-vertical-slice.md`:**
   recognize that the **diagnostic evidence research gate
   becomes satisfied only once this reconciliation merges**,
   while the **playable implementation gate remains BLOCKED**
   pending an actual selected coherent slice, method choices,
   integration/projector, Engine prerequisites actually selected,
   executable tests and any other *necessary* product decisions.
   Strategy-space research is already CONCLUDED. Spatial
   behavior is not a universal first-slice prerequisite;
   do not select a live-loop, T1–T3 Engine change or UI
   technology in this PR.
3. **`docs/research/investigations/factory-design-game-diagnostic-evidence.md`:**
   reconcile the maintained research brief to the accepted
   bounded-positive decision, the Q1–Q8 qualifications,
   exclusions, limits, actual product destination and
   specific reopening triggers. Make the statement and
   status `CONCLUDED` only in the merge candidate as the
   durable consequences are transferred. Do not promise
   population-level comprehension, universal flow formulas
   or a production implementation.
4. **`docs/research/research-register.md`:**
   change **only this Game diagnostic question** from
   `READY` to `CONCLUDED`, with a compact truthful
   summary, accepted scope and planned method gate. That
   state becomes live repository truth only after merge.
   Preserve the separate Game product candidates, analytics
   evidence/provenance `CANDIDATE`, analytics ownership
   `CONCLUDED`, execution account `CONCLUDED`, and
   the still-BLOCKED playable gate.
5. **`docs/research/investigations/factory-design-game-vertical-slice.md`:**
   update *specific* stale references/dependencies on the
   diagnostic study and clarify that controlled retry,
   richer curriculum, scoring, live interaction and
   external-player evidence have separate admission criteria,
   not an automatic required research programme.
6. **`docs/history/decisions/YYYY-MM-DD-game-diagnostic-evidence.md`
   (only if §10's retention test genuinely warrants it):**
   a short **non-normative** rationale about the chosen bounded
   contract versus naive dashboard/statement-list and the
   single-person/guardrail trade-offs. A rationale record
   is optional, not a second specification or copied report.
   Alternatively state in the audit why existing enduring
   requirements/proving cases make one unnecessary.

Inspect and adjust other **direct semantic neighbors** only when
the changed conclusion demonstrably makes them stale.
Do **not** modify `docs/architecture/engine-semantics.md`,
`docs/architecture/runtime-contract.md` or
`docs/architecture/overview.md` simply to restate the
already accepted Engine/evidence/analytics contracts.
Do not alter Factory/Engine/Challenge code, add a Game
runtime/UI, introduce a generalized evidence service, implement
analytical methods, add an Engine KPI or reopen settled
research as incidental work.

**Do not claim that `CONCLUDED` Game diagnostic research
authorizes playable code.** Only the minimum bounded,
independently reviewed product findings are promoted;
the selected Game-local methods, future live-loop options,
and exact player experience may still need narrow
acceptance/evidence before an implementation slice is admitted.

## Validation and selective historical-experiment checks

Before editing, build a concise requirement-to-authority map.
After editing, perform a cross-file truth check:

- **Q1–Q8**: each reviewer qualification has a durable
  destination or a justified explicit non-transfer;
  the reviewer's limitations travel with the positive
  claim rather than living only in the temporary review.
- No document represents the owner walkthrough as blinded
  target-player comprehension, the four-scenario measurement
  exercise as corpus-wide validation, or six lexical audits
  as a universal proof of correctness.
- Snapshot, historical **interval**, and future-horizon
  statements cannot silently stand in for each other.
  Resource state, slot occupancy and attributed starvation
  are separate; offline-and-queued is not universally
  contradictory. Cap/pool/offline choices are either
  explicitly admitted with evidence **or** remain method
  acceptance gates with refusal; never implied defaults.
- No Game claim infers causation, single-run excess
  capacity, a bottleneck from a ranking, a unique
  responsible change in a confounded pair, or composition
  of two separate single-change results.
- No current maintained source still claims a supported
  `busyTicks` API; no future Engine event/clock/closure
  service is declared necessary for old Game fixtures.
- Names and ordering for draft resource projection
  remain deterministic; existing comparator/change-set
  semantics are not reimplemented in the game.
- The Game research question's brief/register and the
  Game plan's blocker language agree. The **product
  research** gate can close on merge while the **playable**
  gate explicitly remains blocked; other CANDIDATE
  questions do not become admitted by implication.

Do a **focused semantic-drift verification** against
current `main`: compare the surviving start-fixed,
time-bounded corpus fixtures and supported event
interpretations with accepted Engine/Research conformance.
Old tests that call removed `ResourceObservation.busyTicks()`
are historically valuable but **not runnable unmodified**.
If a targeted rerun is materially useful, port only the
necessary test/fixture into a new **temporary workspace**
experiment, independently hand-derive expectations and run
the documented research-experiment workflow. Preserve
original report/review sources byte-for-byte and record
new checks' exact commands, outcomes and limitations.
Do **not** refit historical expectations to make tests
pass, copy a naive counter to product code, or expand
into a new Game research cycle.

For documentation-only changes, run the narrowest
repository-owned markdown, authority-link, research
register/lifecycle, transient-coordinate and transient
workspace checks as applicable under
`docs/development/testing.md` and `AGENTS.md`.
Do not rerun unrelated Java suites merely to check
documentation. If you make any executable change
despite this scope, identify why it is essential,
test that exact behavior and obtain review of the
expanded scope rather than treating it as docs-only.

## Knowledge-transfer audit, same-branch reconciliation and PR gates

Execute `docs/development/researching.md` §10's
**knowledge-transfer audit** before final PR review:

- Map each promoted report §7 guardrail, §9 measurement
  demand, §12 requirement and the review's **Q1–Q8**
  to one maintained product, planning, research
  register or deliberate no-transfer destination.
- Preserve reusable negative cases: shared multi-eligible
  waiting not one physical queue, unfinished-work
  occupancy at a half-open bound, concurrency
  denominator, same-snapshot/different-intervention
  `surplus` counterexample, added-capacity non-bottleneck
  case, order-only change, confounded co-binding,
  single-change non-composition, offline-and-queued third
  state/refusal, and the historical completion-credited
  `busyTicks` error. Avoid archiving every original
  scenario or a long now-unbuildable source tree.
- Preserve owner-smoke-test limits, test/audit coverage
  limits, and deferred external-player criteria in
  maintained product/research language. Record any
  important unknowns/triggers (multi-product, arrival
  over time, availability, transfer, pools, named
  methods, live interaction) under their real
  owners; do **not** mint parallel candidate questions
  without a concrete admission need.
- Classify each workspace experiment, report/review,
  walkthrough and fixture as promoted in condensed
  form, deliberately retained under a named durable
  acceptance source, or discarded at retirement.
  Apply the independent historical-rationale retention
  test, and assess a synthesis seed only if a genuinely
  cross-investigation, reusable signal warrants it.
- No accepted requirement may depend on post-retirement
  readability of a full SHA + `workspace/` path.
  Deliberately promote any exact artifact whose future
  readability is indispensable; do not assume PR/Git
  metadata preserves the content.

The existing workspace branch has **diverged** from
`main`. Preserve every handed-off report/review SHA
and any preregistration/probe commits through the
repository's documented **history-preserving,
non-forced merge-style base normalization**.
Do not rebase, amend, hard reset or force-push away
research history, and do not replace the branch with
a new history. If an actual semantic conflict requires
an owner decision, resolve it in the implementation
role, recheck changed meaning and preserve history;
if safe persistence is impossible, report
**`EVIDENCE PERSISTENCE BLOCKED`** rather than
destroying evidence. Verify report/review SHAs remain
reachable after normalization.

Before creating the final reconciliation PR, ensure
**no tracked `workspace/` path** (including this
handoff, old reports, tests, prompts or internal notes)
remains in the candidate's final tree. This removal
is deliberate workspace retirement **after** the
knowledge-transfer audit and only if every question
carried by the workspace has reached a terminal
recorded outcome. Do not accidentally delete valuable
evidence before transfer or equate branch deletion
with a completed audit.

Create/continue **one bounded reconciliation PR**
into live `main`, on the same workspace branch by
default. The PR body records stable **Summary**,
**Compatibility / migration**, and **Non-goals**
per the repository's template; it must not include
transient CI logs, exact head/base status, review/merge
claims or workspace evidence coordinates as a
purported archive.

Run and truthfully report narrow validation, ensure
exact-current-head `CI / gate` reaches terminal
green, the PR head contains live `main`, and GitHub
reports it mergeable; repair implementation-owned
failures and repeat for changed heads. If a blocker
prevents that state, name it precisely. Hand the
**current exact PR head** to a separate **independent
PR Reviewer** under `.github/agents/pr-reviewer.agent.md`.
The author does not self-approve, issue `READY TO MERGE`
on its own change, or merge the PR.

## Final response / stop boundary

Return compact delivery coordinates and decisions:

- unchanged workspace branch; PR link/number; exact
  PR-head SHA; resolved live-main SHA/base freshness;
- durable files changed and the bounded diagnostic
  product requirement set actually reconciled;
- a compact **Q1–Q8 transfer matrix** or its verified
  artifact location, covering the limitations, third
  state/refusal, cap/pool and controlled-comparison guards;
- explicit drift treatment for #465, #466, #469, #471,
  plus targeted historical fixture rerun/port outcome
  or exact reason none was needed;
- knowledge-transfer/discard decisions, including
  evidence history reachability before cleanup and
  zero tracked `workspace/` files in final PR tree;
- validation/CI status on the exact head,
  base freshness/mergeability, independent-review
  status and any concrete blocker;
- precise lifecycle distinction: Game diagnostic
  research `CONCLUDED` **only when merged**;
  analytics ownership and execution account already
  `CONCLUDED`; analytics evidence/provenance
  `CANDIDATE`; and first playable implementation
  still `BLOCKED` until independently admitted.

**Stop when one reviewable durable reconciliation is
ready for independent PR review.** Do not reopen the
Game research, claim a production Game method already
exists, implement the playable loop, require a new
Engine abstraction, or merge.
