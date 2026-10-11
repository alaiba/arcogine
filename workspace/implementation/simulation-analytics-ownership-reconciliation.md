# Implementation handoff — reconcile simulation analytical-definition ownership

## Mission and delivery authority

Act as an Arcogine **implementation/contribution agent performing a bounded research-backed
architectural and planning reconciliation**, not as the Researcher, report author, adversarial
research reviewer, or independent PR Reviewer.

**Goal:** transfer the accepted-with-qualifications simulation analytics ownership result into the
smallest durable authorities, reconcile the Game consumer's now-stale ownership gate, preserve the
downstream evidence/provenance triggers, and close the **one** registered analytics ownership
question. Do not reopen broad analytics research or add a production analytics capability.

- Repository: `alaiba/arcogine`.
- **Continue on the existing evidence workspace branch:**
  `workspace/simulation-analytics-ownership-phase-1`.
  A new reconciliation branch is not required or preferred. Preserve the workspace's finite
  historical custody until reconciliation lands.
- This transient handoff:
  `workspace/implementation/simulation-analytics-ownership-reconciliation.md`.
  This file, all research reports and review artifacts, and other tracked `workspace/` files
  **must not appear in the final PR merge tree**.
- Live `main` at preparation:
  `a7f9856617d99e796d528a44aa8c15677c485f55` (includes #465, #466 and #469).
  Re-resolve `main` and relevant open PRs at execution start, after major edits and
  before final handoff. Never treat this historical baseline as immutable live truth.
- Latest independently reviewed research report:
  `d2125f39482de3306f2f56e9c7261a3dde160b33`,
  `workspace/research/investigations/simulation-analytics-ownership-phase-2-post-execution-evidence-revision.md`.
- **Independent research review — authoritative qualification set for this slice:**
  `1bb2a12fed66e893e31aa85b6db30c3bcecd6a0f`,
  `workspace/research/investigations/simulation-analytics-ownership-phase-2-post-execution-evidence-adversarial-review.md`.
  **Disposition: ACCEPT WITH QUALIFICATIONS.** Reconcile all Q1–Q6 (§8).
- Original Phase 1 checkpoint:
  `7e72a46506bf63febcb57d7b7dcc9ce0cd484d64`,
  `workspace/research/investigations/simulation-analytics-ownership-phase-1-current-boundary.md`.
  Earlier Phase 2 report `6a5c043563e3bd89fae6826f830b377b43643d46`
  was **REOPEN** at `2c85fe7c03c6256c9e8a2ce73fde5091b3357e3c`;
  interim revision `75736dddfa3fe1ae89c5089720c702f4ca595ab5`
  was **never independently reviewed**. Neither provides an acceptance of the new report.
- The new reviewer reconstructed before reviewing the report
  (`d43251eedac326e4a5ac9572fd5d8e214ec2016d`), pre-registered review probes
  (`f8c4ba3a4975b539b3bf60eda4fe2d9c4bcb173b`) and committed them
  (`cb926538c353bb4c760183cc83846f9b1fc04ed5`).
  These and the report's earlier pre-registration/probes remain research evidence,
  **not implementation dependencies or production helper types**.

Before changing authority, follow the live `AGENTS.md`, `.github/CONTRIBUTING.md`,
`docs/development/researching.md` §10, `docs/development/reviewing.md`,
`docs/development/testing.md`, and the architecture/document ownership rules.
Read the exact report and **entire independent review** at their committed SHAs rather
than copying its proposed wording blindly.

No extra independent *research* review is required to reconcile the accepted result;
the resulting **PR does require independent PR review**. The agent writing this
reconciliation never issues its own PR-review disposition and never merges.

## Repository grounding and history preservation

1. Resolve live `main`, current workspace head, any relevant active PR and the exact reviewed
   report/review coordinates. Treat main as canonical and the workspace as evidence/draft custody.
2. Read `AGENTS.md` and perform a **quick search in `docs/`** for
   `analytics ownership`, `domain authority`, `named analytical method`,
   `utilization`, `occupancy`, `Game diagnostics`, `evidence custody`,
   `definition identity`, `basis/frontier`, `E5`, `E6`,
   `busyTicks`, `evidence/provenance`, `CONCLUDED`, `READY`,
   `shared definition`, `cross-revision`, `Governance analytical evidence`.
   Inspect concrete semantic neighbors, not just grep snippets.
3. At live-main SHA, read:
   - `docs/architecture/overview.md` **Domain authority** and Determinism Contract;
   - `docs/architecture/runtime-contract.md` **Captured execution evidence and interval
     determinacy** / **Responsibilities and limits**;
   - `docs/architecture/engine-semantics.md` §§1.1–1.2, 4 and 10–10.2;
   - `docs/architecture/governance-evidence.md` §§5–7 and 11–13;
   - `docs/product/charter.md` §6; `docs/architecture/factory-design.md` §10;
   - `docs/planning/factory-design-game-consumer.md` §§4–7;
     `docs/planning/factory-design-game-vertical-slice.md`;
   - `docs/research/investigations/simulation-analytics-consumer-boundary.md`,
     `simulation-analytics-evidence-provenance.md`,
     `simulation-analytics-adapter-compatibility.md`,
     `simulation-analytics-cross-revision-comparability.md`,
     `factory-design-game-diagnostic-evidence.md`,
     `simulation-execution-account.md`;
     `docs/research/research-register.md`;
   - `docs/history/README.md`, existing
     `docs/history/decisions/2026-10-09-captured-simulation-evidence.md`.
4. Where a current-state claim needs source, inspect current `FactoryRuntime`,
   `FactoryHandler`, `RuntimeObservation`, `RuntimePerformanceObservation`,
   `ResourceObservation`, supported event/command outcomes, and applicable conformance.
   Source existence alone does not establish supported method-level status.
5. The workspace may **diverge** from current main. Preserve every handed-off research/review
   commit using the documented **history-preserving base normalization** and supported
   semantic conflict resolution. No amend, rebase, force push, hard reset of evidence
   history, or copying the workspace archive onto main. Re-check exact SHA reachability,
   current-base ancestry and the final net diff against **live main**. If no safe
   normalization can preserve custody, stop with **`EVIDENCE PERSISTENCE BLOCKED`**.

## Accepted decision to reconcile — no new analytics architecture

The supported ownership verdict is **staged, kind-specific admission with no shared
consumer-neutral analytics owner now**. Do not turn this into a standing prohibition
on reusable analytics, Engine-provided measurements, or a future implementation.

**Single durable analytical-definition rule** (place in
`docs/architecture/overview.md#domain-authority`, near the existing domain owners):

- Engine/Factory remain authoritative for state, result-affecting interpretation,
  supported observations/events and the exact arithmetic of **existing Engine-declared**
  derived results. Changing one of those values' definition is an Engine
  specification/support change, not a consumer calculation refactor.
- **A named analytical method's meaning belongs to the capability that fixes
  choices execution does not fix**, such as population, selected period,
  denominator/capacity, availability treatment, grouping, attribution,
  interpretation and any method-specific causal/diagnostic judgment.
  A single consumer's method is **consumer-local by default**.
- A **shared analytical-definition owner** becomes justified by a concrete
  admission trigger, not a speculative module:
  (a) two actual consumers require **the same named meaning**;
  (b) one consumer plus a concrete **core verification/Governance requirement
      with acceptance evidence**, even with no second shipped consumer;
  (c) an admitted outward contract requires shared semantics; or
  (d) divergent duplicated computations need **one agreed definition**.
  These are alternative routes, not a two-shipped-consumer minimum.
  Co-located capture, two call sites, fixture-only Governance evaluation,
  or an illustrative objective do not satisfy a trigger by themselves.
- A measurement enters Engine only on a **separate demonstrated producer
  responsibility**: it participates in execution/result-affecting behavior,
  a concrete contract requires Engine-wide meaning, or a justified
  execution-fixed run-to-date aggregate is needed where the current
  holder cannot capture from establishment. Compare the latter fairly
  with bounded retained/shared delivery and its compatibility cost.
  Neither an attractive A′ accumulator nor recent `busyTicks` removal
  preselects the answer. Such an aggregate can reduce a **retention** need,
  not guarantee arbitrary interval **finality**.
- Keep the anti-second-Engine boundary: analytical methods interpret
  published outcomes; they do **not** reconstruct scheduler selection,
  dispatch cascades, unseen historical step starts, future input, fault-free
  execution, authoritative bottlenecks or causal truth from raw occupancy.
  Controlled counterfactual comparisons use Engine re-execution with
  explicit inputs, not a second simulator.

**One home (Q6):** this is a short rule under overview Domain authority,
not a duplicated analytics section in `runtime-contract.md` or a report
copied into the architecture. The current runtime contract already owns
RunId, basis, sequence frontier, claim-specific coverage, half-open
finality, controller attestation, holder retention and role separation.
At most add a small relevant cross-reference there if essential.
Do **not** edit the Engine interpretation or Determinism Contract,
reintroduce `busyTicks`, change current declared KPI arithmetic,
or add an Engine analytics API, central ledger, analytics module or service.

## Qualifications that must survive (review Q1–Q6)

### Q1 — Evidence/provenance activation is broader than custody crossing

Keep `docs/research/investigations/simulation-analytics-evidence-provenance.md`
at **CANDIDATE**. Its maintained activation options must include a **concrete**
admitted analytical use needing any of:

- additional supported producer inputs;
- definition-specific input sufficiency or refusal, including a shared
  method or custody-crossing use;
- definition-shaped bounded accumulation **instead of** raw retention;
- definition identity and historical meaning;
- result attribution/provenance; or
- retention/handover for the needed horizon.

Include cross-boundary reliance by a passive verifier, Governance use,
a party other than the driver/holder, and cross-session reproduction as
**examples/additional triggers**, not a replacement for the above.
Rule-4 shared-definition admission must cause **evaluation** of these
dimensions, not automatic promotion. A stateless named method supplied with
already-sufficient raw evidence by one co-located holder, needing no new
inputs, accumulation, provenance or retention, is not a trigger **by
definition sharing alone**. Do not route every missing producer fact to an
A′ accumulator; producer support and analytics evidence/provenance are
related but different questions.

Leave adapter compatibility **CANDIDATE** until there is an admitted shared
analytics contract and another concrete supported consumption boundary.
Leave cross-revision comparability **CANDIDATE** until there is a concrete
comparison of results made under different definitions/revisions.
Preserve the precise #465/#469 risks (below) without declaring every
historical result incompatible.

### Q2 — Promotion-ready method and evidence refusal discipline

For any **retained or promoted interval analytical result**, the method/use
must require the result's definition identity, RunId and ModelFingerprint
(ControlledRevisionId only when authoritatively bound), interval and
convention, observation basis `(S0,T0)`, proof sequence/frontier and
proof class. When needed by the **specific claim**, carry the controller
attestor, input-exclusion scope, trust and command/fault qualifications;
exact producing Engine-definition reference for cross-revision use
or reproduction **only when independently supported**; and ordered
commands plus advancement interleavings/budgets for reproduction.

Method-specific sufficiency/refusal must require **same-run** binding
of basis, observations, changes and closure and reject a closure
contradicted by captured changes after its purported binding but before
the claimed upper bound. Preserve distinct producer-witness,
producer-observation and attributable-controller-closure proofs. A
gap-free list from one holder is not its own proof of an authoritative
frontier. Complete supported changes are not complete command/fault
provenance, proof of fault-free execution, or a replay engine.

The reviewer demonstrated two **false acceptances** by the report's
research-only `prove()` helper: a mixed-RunId basis produced 10 where
truth was 5, and a stale-bound controller closure produced 100 where
truth was 5 despite a held completion. **Do not copy that helper** into
product, require an unadmitted production verifier, or call those
research-only defects a falsifier of the landed runtime contract.
Preserve these as named **rejection/acceptance cases** for future
method admission (plus the existing stale-frontier five-vs-100
coverage case), in the smallest durable destination that will own
them. Do not add a large test framework now merely to archive the probes.

When definition custodianship is promoted to shared ownership,
physical evidence custody need not move and no-rework is **conditional**
on preserving the proof and trust provenance from the outset.
Do not claim that naming a definition automatically creates its
versioned identity, storage or implementation.

### Q3 — Game snapshot, interval and horizon have distinct meanings

- A **fresh observed snapshot** can establish "no currently known work",
  or classify current idle/starved work based on the published routing
  and present supported state. That statement does **not** require
  a future-input promise.
- A **period aggregate** of those labels requires valid captured
  interval evidence and finality. In the idle tail after the last
  supported change, the controller's attributed successful exhaustion
  and no-future-effective-input commitment may be required.
- "No work left **for the rest of the period**" is a **future-horizon**
  statement needing scope of future input exclusion; do not silently
  equate it with a snapshot's "no currently known work".
- Research-local flow experiments prove only their **declared scope**:
  they may assume a job-free observation basis, always-online resources,
  one product and constructive closed scripts. Do not make those
  fixtures the universal Game diagnostic contract.

### Q4 — The Game plan changes ownership default, not implementation status

The current Game plan says **"Unresolved — not game-owned by default"**
for reusable derived measurement and forbids implementing generic analytics
locally pending the ownership question. Reconcile this *explicitly* in
`docs/planning/factory-design-game-consumer.md` §§4–5:

- The Game **may own its own named consumer-specific diagnostic
  definitions** when it chooses the analytical questions and bases,
  using supported Arcogine evidence under Q2/Q3. A named Game-owned
  measurement is **not** an Engine fact, generic shared definition,
  causal bottleneck verdict or second simulator.
- A genuinely common named definition is promoted on the **architecture's
  concrete shared-owner trigger**, not game preference or a repository
  folder. Game capture as its own run's exclusive driver does **not**
  make it the owner of independent consumers' captured evidence.
- Game method/proof provenance should be declaration-ready from the
  beginning so later sharing, handover or retained-result interpretation
  cannot silently widen the underlying trust. No analytics module,
  universal evidence container or remote/embedded adapter is selected.

Update `docs/planning/factory-design-game-vertical-slice.md` criterion 4
and adjacent prose as needed so Game-local methods may proceed **only
after their own Game diagnostic requirements are concluded and admitted**,
while shared/reusable analytics still respects Rule 4 and triggered
evidence/provenance dependencies. **Do not unblock the playable slice.**
The independently reviewed **Game diagnostic evidence** question
remains **READY** and requires its **separate** product reconciliation.
That future reconciliation, not this PR, owns the actual utilization
and flow definitions and its prior review's third-state/refusal, capped
starvation, pool basis and availability obligations.
The selected live-loop T1/T2/T3 requirements remain an Engine/Game
admission checkpoint; this PR chooses none.

### Q5 — Current semantics, alternatives and historical compatibility

- #465 removed inert scheduler markers. For fully start-fixed inputs
  with time-bounded-only advancement calls, accepted supported events
  remain equal under the stated #465 scope. Specific positive-event
  budget-stopped scripts **can** have changed results.
- #466 established the captured-evidence/interval-finality contract.
  That contract remains unchanged.
- #469 removed `busyTicks`, Engine utilization/occupancy/bottleneck
  claims and associated historical accumulator. Results needing that
  removed field cannot simply be recomputed from a fresh current
  observation; supported captured event history may still answer a
  different named analytical question.
- The current Engine-declared results — `backlog`, `completedOrders`,
  `completedSalesValue`, `averageLeadTime` and
  `throughputPerTick` — retain their existing exact meanings and
  arithmetic. `throughputPerTick` uses **last supported observed time**,
  not an analyst's period ending at an `advanceUntil` target.
  Public `FactoryRuntime.throughput(long)` method-level support
  remains a separately open classification, not implicitly settled.
  `combinedQueueDepth` is Engine result-affecting ranking, not a
  display-ready physical queue depth.
- "All analytical measurement must be outside Engine" is **rejected as
  a universal policy**; it conflicts with actual Engine-owned
  result-affecting interpretation and preempts suitable future
  producer adaptation. The hypothetical late-joining consumer is a
  **future trigger, not an empirical current falsifier**.
- Existing current contracts are not revised as an incidental
  consequence of assigning new analytical-definition ownership.
  Preserve open transfer/spatial rechecks until executable consumer
  evidence exists.

### Q6 — One current owner per rule

Keep the short semantic-ownership/admission rule in overview. Keep
existing runtime capture/finality semantics in the runtime contract.
Keep Game product-ownership consequences in its consumer plan.
Keep Governance's **attribution/provenance use** language in
`governance-evidence.md`. Keep unresolved admission triggers in
research briefs/register. Do not paraphrase the same Q1–Q5 obligations
into redundant standing contracts or leave a governing qualification
only in a historical rationale record.

## Minimal durable change set and acceptance

Expect a **documentation-only** PR, with changes limited to actual
semantic neighbors. Proposed files:

1. `docs/architecture/overview.md` — one concise authoritative
   analytical-definition ownership/admission rule under Domain authority,
   plus at most one existing-runtime-contract cross-reference.
2. `docs/planning/factory-design-game-consumer.md` — replace the
   unresolved/default ban with the new explicit Game-local named method
   boundary and preservation of future shared-owner conditions.
3. `docs/planning/factory-design-game-vertical-slice.md` —
   change criterion 4 and stale analytics ownership dependency
   **without** claiming product research or playable admission is done.
4. `docs/architecture/governance-evidence.md` §§12–13 — resolve the
   *ownership* part of analytical-definition provenance: definition
   owner supplies definition identity/meaning; holder and attestor
   supply their respective captured-evidence/closure provenance.
   **Representation, retained reference identity, producer exact
   Engine-definition provenance and storage remain open.**
   Do not claim Governance now produces or calculates analytics.
5. `docs/research/investigations/simulation-analytics-consumer-boundary.md` —
   reconcile the **full** Phase 1/current-field and Phase 2 verdict.
   Update the status, decision, conditions/reopening triggers and
   stale text about prospective investigation/review. Keep remaining
   historical evidence sections only when useful, clearly marked as
   such; do not copy the long report.
6. `docs/research/research-register.md` — change **only this analytics
   ownership question** to `CONCLUDED` in the merge candidate,
   identifying the owning normative decision and reopening triggers.
   The conclusion becomes actual repository state **only on merge**.
7. `docs/research/investigations/simulation-analytics-evidence-provenance.md`
   — preserve **CANDIDATE** and Q1's whole trigger set; remove stale
   "once ownership is known" dependency language when misleading.
   Update its register row only to the extent needed to preserve Q1.
8. `docs/research/investigations/simulation-analytics-adapter-compatibility.md`
   and `simulation-analytics-cross-revision-comparability.md` —
   no change unless a specific stale/contradictory statement is found;
   their register rows remain `CANDIDATE`.
9. `docs/history/decisions/YYYY-MM-DD-analytics-definition-ownership.md`
   — **assess** the research method's retention test. A short
   non-normative rationale record is plausible because the research
   went through a REOPEN and two viable alternatives remain:
   Engine-provided standardized measurement and an execution-fixed
   late-join aggregate. If warranted, record why the selected
   staged rule won now, rejected definition/custody coupling (both
   directions), "retention not finality", the R1/R2 validator lesson,
   snapshot versus horizon, and specific reopen triggers. Otherwise
   explicitly justify why no standalone historical record adds value.
   Never put current rules **only** in this historical record.
10. `docs/architecture/runtime-contract.md` — **no rewrite**.
    A cross-reference only if actually needed for coherence.
    Inspect other semantic neighbors and fix **specific** stale
    statements, but do not expand into a repository-wide sweep.

**Hard exclusions:** no changes to `product/` APIs or logic, no
analytics implementation/module/database, no definition/version
registry, no new Engine KPI or clock/closure primitive, no factory
dispatcher/transfer changes, no Game UI/prototype code, no new
Game measurement formulas, no causal bottleneck metric, no second
research report/review, no decision about adapters or
cross-revision compatibility, no premature Gameplay admission.

### Required cross-file truth checks

Before final PR submission, verify by inspecting actual text that:

- overview names the one current rule and all four shared-admission
  alternatives; it neither requires two shipped consumers nor
  labels a possible future Engine aggregate forbidden;
- Game planning's previous **"not game-owned by default"** rule and
  generic local prohibition no longer conflict with the new
  *consumer-specific named method* permission;
- snapshot, interval and future-horizon claims remain distinct;
  no planner describes `QUIESCENT` or `advanceUntil(target)`
  as a producer closure witness;
- a promoted/retained interval result's listed provenance
  contains run/model, basis, claim convention, proof frontier
  and proof class with claim-specific controller/definition
  qualifications; the false-accept R1/R2 conditions are refused,
  not reproduced as a helper API;
- Governance describes **who supplies** definition and capture
  provenance without promising missing implementation or exact
  Engine-definition references;
- the execution-account conclusion and runtime evidence
  semantics remain unchanged and authoritative;
- the analytics question's brief and register agree on
  `CONCLUDED` after merge, evidence/provenance stays
  `CANDIDATE` with the full Q1 trigger, and Game diagnostics
  stay `READY` with the playable gate **BLOCKED**;
- no maintained current-state file calls `busyTicks` a live
  supported field, misnames `throughputPerTick` a caller-window
  rate, or assumes all #465 run comparisons are incomparable;
- original high-risk qualifications Q1–Q6 are all mapped to
  at least one **appropriate durable destination**.

## Evidence-transfer audit, branch retirement and PR review

Conduct the knowledge-transfer audit required by
`docs/development/researching.md` §10 **before final review**:

- Map every load-bearing Phase 1 conclusion, revised Rules 1–8
  and independent-review qualifications Q1–Q6 to its new
  canonical home or justify a deliberate non-transfer.
- Preserve in durable wording or a small maintained proving case
  the **reusable** failures: mixed-run basis; stale closure with
  held contradictory completion; stale-holder false 100 ticks;
  earlier valid prefix despite later gap; snapshot vs interval
  and the `busyTicks` information loss. Explicitly classify
  author/reviewer experiments and fixtures as **promoted,
  retained for active custody, or discarded on workspace
  retirement**. Avoid an archival 1000-line test copy.
- Judge whether a historical decision-rationale record adds
  durable information that would otherwise be repeatedly
  rediscovered; it is optional, not a parallel authority.
- Check for a qualifying cross-investigation synthesis seed
  under the normal method. Do not add one merely to preserve
  an interesting observation already owned by the runtime
  contract and architecture.
- Preserve all handed-off commits during reconciliation. Once
  content has transferred and the reviewer can compare the
  accepted evidence, **remove every tracked `workspace/` path
  from the final PR merge tree** without rewriting history.
  Never copy exact full SHA + `workspace/` path pairs into
  durable docs as a purported archive.
- Run the tracked transient-workspace and transient-coordinate
  checks. The workspace may be retired only after this
  **reconciled** question is `CONCLUDED`, all other questions
  carried on the workspace have a recorded terminal outcome,
  and the audit is complete. Merely opening a PR does not
  satisfy retirement or `CONCLUDED`.

**One coherent reconciliation PR** into current `main` from the
same workspace branch, once base-normalized and cleaned. Use
`.github/pull_request_template.md` with only stable
**Summary**, **Compatibility / migration** and **Non-goals**.
Do not include CI results, validation logs, SHA/head/base
state, mergeability, reviewer status or other transient
lifecycle data in the PR body. Do not put agent/model attribution
trailers in commit messages or PR text.

Run the narrowest check for the actual changed files
(`AGENTS.md`, `docs/development/testing.md` and the
repository's markdown-link, transient-workspace,
transient-coordinate, source-authority and delivery-label
checkers). A documentation-only reconciliation does **not**
require unrelated Java test reruns. If any product/source
assertion is added or changed, validate only the applicable
maintained conformance. Report executed versus skipped checks
truthfully.

After creating/updating the PR, inspect its **exact current
head** and carry required `CI / gate` to a terminal green
result, repairing implementation-owned failures and repeating
if the head changes. Ensure the head includes current live
`main` and GitHub reports it mergeable or report the blocker.
Then request the dedicated **independent PR Reviewer**.
The implementation agent does not issue `READY TO MERGE` or
`CHANGES REQUIRED` on itself, does not simulate an
independent review, and **never merges the PR**.

## Final handoff to repository owner

Return a compact factual status, not this full prompt or a new
research report:

- PR number/link, unchanged workspace branch and exact final
  PR-head SHA; resolved live-main baseline and base freshness;
- durable files changed and the short adopted ownership/admission
  rule, including all four shared-owner triggers;
- evidence that review qualifications Q1–Q6 reached their
  respective durable homes, including Game's explicit reversal,
  the same-run/contradicted-closure refusals and Q1's complete
  downstream activation set;
- status of analytics ownership `CONCLUDED` **on merge**,
  evidence/provenance `CANDIDATE`, Game diagnostic `READY`
  and playable gate `BLOCKED`;
- whether historical rationale was retained, how the remaining
  valuable evidence was transferred/discarded and proof that
  no `workspace/` files remain in the PR tree;
- tests/checkers run, skipped or unavailable; terminal
  exact-head CI result; mergeability/freshness and independent
  PR-review disposition or the specific unresolved blocker.

If a proposed rule proves stronger than the exact qualified
research evidence, **narrow the rule**. If independent PR
review discovers a materially new load-bearing falsifier,
escalate only that bounded issue; do not silently commission
another broad analytics research track.

**No additional analytics research is a prerequisite for this
reconciliation. No implementation or product gate is
unblocked merely by this handoff. Do not merge.**
