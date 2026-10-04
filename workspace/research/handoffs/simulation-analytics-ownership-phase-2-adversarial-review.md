# Handoff — Independent adversarial review of simulation analytics ownership Phase 2

Perform the **independent adversarial research review** required for the high-risk
Simulation analytics ownership boundary investigation.

This is a review of one exact report revision. Do not execute a fresh Phase 2 investigation, do not
reconcile architecture/specifications, and do not implement the recommendation.

## Exact report identity

Repository: `alaiba/arcogine`

Temporary research-evidence workspace:

- branch: `workspace/simulation-analytics-ownership-phase-1`
- reviewed report commit: `6a5c043563e3bd89fae6826f830b377b43643d46`
- reviewed report path:
  `workspace/research/investigations/simulation-analytics-ownership-phase-2-admission-boundary.md`
- report-stated research baseline:
  `8c2ed14dccde0ca1dbb51e58c94ff19d822856a1`

The same report commit also contains the research-custody experiment:

`workspace/research/experiments/simulation-analytics-ownership/com/arcogine/research/experiment/PerformanceEvidenceSufficiencyExperiment.java`

Earlier Phase 1 / handoff evidence remains reachable from the same workspace history, including:

- Phase 1 report commit `7e72a46506bf63febcb57d7b7dcc9ce0cd484d64`
- Phase 2 handoff commit `c792041388772da23846e2881c4644db69668eb6`

Resolve and verify the complete exact report before substantive report-specific work. If the exact
report cannot be resolved, stop with:

`INPUT BLOCKED — ORIGINAL REPORT NOT AVAILABLE`

Do not infer the report from this handoff.

## Independence requirement

This is a **high-risk** ownership/determinism question. The review counts as independent only if this
run is genuinely separate from the run that authored the Phase 2 report and has no responsibility for
defending its conclusion.

State explicitly in the review whether the independence condition is satisfied.

If it is not satisfied, do not claim an independent adversarial disposition with promotion weight.
Persist the pass truthfully as self-administered/non-independent and say that a genuinely independent
review remains required.

Sharing the same research-evidence workspace is expected and does not weaken independence.

## Required operating authorities

Before substantive work, read and follow the current versions on live `main` of:

- `AGENTS.md`
- `.github/agents/researcher.agent.md`
- `docs/development/researching.md`
- `docs/research/research-register.md`
- `docs/research/investigations/simulation-analytics-consumer-boundary.md`

Then ground in the current repository authorities actually touched by the question, including at
least:

- `docs/architecture/overview.md`
- `docs/architecture/engine-semantics.md`
- `docs/architecture/runtime-contract.md`
- `docs/architecture/factory-design.md`
- `docs/architecture/governance-evidence.md`
- `docs/architecture/governance-conformance.md`
- relevant current Factory runtime implementation and conformance tests
- relevant current Challenge comparison/evaluation surfaces
- relevant current `product/research-experiments/` substrate and tests
- the Phase 1 report only after the independent reconstruction below when needed as supporting
  evidence.

Perform the normal semantic-neighbor search under `docs/` and `product/`. At minimum search for:

- `RuntimePerformanceObservation`
- `RuntimeObservation`
- `busyTicks`
- `combinedQueueDepth`
- `averageLeadTime`
- `throughputPerTick`
- `completedSalesValue`
- `utilization`
- `occupancy`
- `bottleneck`
- `waiting`
- `AttemptComparison`
- `AuthoritativeOutcomeFacts`
- analytics ownership / producer provenance / verification objectives

Search results are discovery only. Fetch every load-bearing file at the exact live-main review
baseline before relying on it.

## Anchoring-control sequence

Follow this order strictly.

### 1. Verify identity, but do not deeply read the report recommendation yet

Confirm:

- workspace branch;
- exact report commit;
- complete report path/content;
- report-stated research baseline;
- presence of the experiment at the exact reviewed commit.

Recording identity/completeness is allowed before the independent reconstruction.

### 2. Independently reconstruct the question from current authority

Before deeply consuming the report's own recommendation:

1. read the landed research brief and current register entry;
2. resolve live `main` and record its exact SHA as the **review baseline**;
3. reconstruct the material current Engine/runtime facts and ownership constraints;
4. identify the plausible ownership models independently;
5. derive the proving/failure cases you think must discriminate those models;
6. identify the strongest likely failure modes for any mixed Engine/analytics boundary.

Do not use the report's R1–R5 rule, candidate verdict, or self-adversarial analysis as the starting
ontology for this reconstruction.

Only after writing down/reaching that independent reconstruction should you deeply evaluate the
report.

## Current-main process note

At report completion, the report's final repository recheck was
`4d83f143143bf778881ba842bd4949734ce400c9`.

Live `main` has since advanced. In particular, PR #456 landed and removed `ACTIVE` as a maintained
research lifecycle state; admitted unresolved research now remains `READY` while investigation,
review, or reconciliation is in progress.

The reviewed report itself still records `Research status: ACTIVE` because it predates that process
change. Treat this as stale process metadata unless the current lifecycle change materially affects a
load-bearing research claim. Do not silently rewrite the reviewed report. The review must remain
bound to exact commit `6a5c043563e3bd89fae6826f830b377b43643d46`.

Recheck all live-main changes since the report baseline and final recheck. Distinguish:

- process-only changes with no research consequence;
- any changes to research policy that affect review procedure;
- any architecture/specification/runtime/test change that could affect the conclusion.

## Report conclusion to challenge — only after independent reconstruction

After completing the anchoring-control sequence, evaluate the report's actual conclusion at the exact
reviewed revision.

The report proposes, in summary, a refined minimal-authoritative/mixed boundary in which:

- current supported performance results remain Engine-owned;
- strict facts-only is rejected because current run-to-date accumulators cannot generally be recovered
  from a fresh observation without retained evidence or replacement projections;
- Engine-rich is rejected as a **growth model** for future measurement policy;
- a consumer-neutral analytics responsibility exists for reusable named measurements over supported
  evidence plus comparison qualification;
- diagnostic interpretation remains consumer-owned unless a concrete shared named method later
  justifies shared ownership;
- analytics may measure outcomes but must never reconstruct or re-decide Engine choices;
- no current field is reclassified now;
- `averageLeadTime` and `throughputPerTick` remain Engine-owned as legacy declared results although
  they would not satisfy the proposed admission rule for new Engine measurements;
- evidence/completeness/provenance becomes promotable only after this conclusion survives independent
  review and durable reconciliation.

Do not accept any of these statements because this handoff summarizes them. Evaluate them against the
exact report and independent evidence.

## Mandatory adversarial targets

Attempt to falsify at least the following load-bearing claims.

### A. Is the candidate set complete?

Challenge whether the three-model framing omits a materially distinct viable model, especially:

- Engine-owned measurement definitions with a separate non-Engine presentation/diagnostic layer;
- consumer-local measurement with no reusable analytics owner at all;
- a contract that exposes evidence primitives but retains selected standardized measurements outside
  the Engine without creating a standalone shared analytics capability;
- any other model your independent reconstruction reveals.

Do not manufacture a fourth model if it changes no decision.

### B. Does strict facts-only actually fail?

Scrutinize experiments B, C, and C′ and the underlying current contracts.

Ask:

- Do they prove that `busyTicks` and `completedSalesValue` are not determined by the rest of a
  fresh supported observation and published model?
- Does the result actually falsify a facts-only semantic model, or merely show that a facts-only
  model would need a different evidence contract?
- Is the report justified in calling that hidden retention/replay requirement enough to reject Model
  2 under the current Arcogine contract?
- Are the paired runs genuinely indistinguishable in all inputs/evidence that a plausible facts-only
  consumer would be allowed to use?
- Does the experiment accidentally rely on research-local or unsupported evidence?

### C. Does Engine-rich really fail as a growth model?

This is not an impossibility proof; the report calls it a coupling argument.

Challenge whether:

- placing standardized utilization/occupancy in Engine semantics is actually harmful enough to reject
  the model;
- measurement choices truly do not interact with supported execution interpretation;
- the indivisible Engine-definition argument is correctly applied to report-only measurements;
- refusal semantics genuinely conflict with Engine's supported total-function/sentinel conventions,
  or whether multiple named Engine measurements could coexist without semantic damage;
- the report presents discretionary architectural cost as though it were semantic falsification.

If Model 1 remains viable but less attractive, say so precisely.

### D. Is a consumer-neutral analytics responsibility actually justified now?

This is one of the report's lowest-confidence conclusions.

Challenge whether current repository authority establishes a **shared semantic owner** rather than
only showing that:

- the Engine should not own future measurement policy; and
- each consumer may define its own measurements when needed.

Inspect the claimed evidence:

- Factory shared verification objectives;
- Governance analytical-producer ownership;
- game/Challenge restrictions against local generic analytics;
- any concrete implemented or admitted second consumer.

Test the narrower alternative:

> Future measurement is outside Engine; shared analytics is admitted only when two concrete consumers
> need the same named definition. Until then measurement remains consumer-local.

Determine whether the report's current "consumer-neutral responsibility exists" claim is evidence
supported, merely a useful category, or premature.

### E. Are R1(c) and R2 internally coherent?

Challenge the distinction between Engine-owned run-to-date aggregates and non-Engine measurement
policy.

In particular inspect whether R1(c)'s wording around counts/sums and "no ... attribution" is coherent
with `busyTicks`, which is credited/grouped per resource.

Distinguish:

- authoritative attribution already fixed by Engine execution/change history;
- analytical attribution chosen by a measurement method.

If this is only wording, identify the qualification precisely. If it breaks the rule, say so.

### F. Are `averageLeadTime` and `throughputPerTick` an unjustified exception?

This is explicitly medium-confidence in the report.

Challenge:

- whether R1(d) makes the prospective rule non-predictive by grandfathering policy-laden results;
- whether the stated migration/compatibility cost actually justifies semantic retention;
- whether "no analytics owner exists to receive them" is circular after the report simultaneously
  concludes that a consumer-neutral analytics responsibility exists;
- whether Model 3-strict is cleaner and sufficiently justified now;
- conversely, whether removing them would be churn with no decision benefit.

A qualification here may be sufficient; do not reopen the entire boundary unless this exception is
load-bearing to the model itself.

### G. Does the anti-second-engine rule survive the proving cases?

Verify that proposed analytics can derive:

- waiting attribution;
- occupancy/utilization;
- comparison qualification;

without reconstructing candidate ranking, dispatch choice, timing/admission decisions, or
counterfactual execution.

Check whether any claimed input set actually forces analytics to reproduce hidden Engine semantics.

### H. Are Q1–Q6 genuinely closed?

Review each Phase 1 question independently:

- Q1 utilization wording and responsibility;
- Q2 fresh-observation-recomputable fields;
- Q3 history-dependent accumulators;
- Q4 throughput/observation-clock coherence;
- Q5 `FactoryRuntime` accessors;
- Q6 boundary proving cases.

Look for any question the report labels "closed" but actually defers to later reconciliation or a
downstream research question in a way that leaves the ownership decision unresolved.

### I. Are the contract-change categories truthful?

For every recommended current-field consequence, verify whether the report correctly classifies it as:

- Engine-semantics change;
- runtime-contract change;
- both;
- implementation/public-operation only;
- textual clarification with no supported semantic change.

Pay particular attention to:

- the interpretation of "utilization facts";
- the claim that no Determinism Contract revision is required;
- `FactoryRuntime.throughput(long)`;
- whether retaining `averageLeadTime` / `throughputPerTick` while changing the prospective ownership
  rule creates an implicit semantic exception that deserves durable documentation.

### J. Are downstream triggers premature?

Challenge the recommendation to promote simulation analytics
evidence/completeness/provenance after acceptance/reconciliation.

Ask whether the Phase 2 result establishes:

1. a concrete reusable analytics responsibility/use; and
2. a truthfulness dependency on an explicit evidence/retention contract,

or only establishes a possible future responsibility.

If the trigger is only partially met, qualify it rather than promoting it automatically.

## Experiment review

Inspect the exact custody experiment source and, where useful, re-run or independently reproduce
the load-bearing cases using the repository's documented research-experiment mechanism.

Do not accept printed values as evidence by themselves. Verify:

- expected values are specification-derived;
- inputs distinguish the intended hypothesis;
- assertions are not tautological;
- helpers do not accidentally call the implementation logic they claim to reproduce independently;
- event completeness assumptions are explicit;
- research-local oracles are not smuggled in as production semantics;
- saturation/non-finite limitations are correctly scoped.

You do not need to rerun every baseline test merely because the report did. Re-run what materially
tests a challenged conclusion.

## External evidence

The report uses external material only as supporting context, not as its primary ownership proof.

Verify any external source you rely on in the review. Specifically challenge:

- whether bottleneck/constraint literature actually supports the limited claim attributed to it;
- whether any possibility precedent has been upgraded into necessity;
- whether unverified background sources are carrying any hidden load-bearing weight.

Do not expand this into a general literature review.

## Required disposition

Reach exactly one:

- **ACCEPT**
- **ACCEPT WITH QUALIFICATIONS**
- **MORE EVIDENCE REQUIRED**
- **REOPEN**

Use the definitions in current `docs/development/researching.md`.

For every material challenge, state:

- what was challenged;
- evidence considered;
- result;
- effect on the report's conclusion;
- any qualification that must survive reconciliation.

Do not manufacture findings merely to avoid a clean `ACCEPT`.

## Review artifact requirements

Persist the completed adversarial-review artifact in this same workspace under a semantic path such
as:

`workspace/research/investigations/simulation-analytics-ownership-phase-2-adversarial-review.md`

The artifact must identify:

- independence status;
- live-main review baseline SHA;
- reviewed report branch;
- exact reviewed report commit:
  `6a5c043563e3bd89fae6826f830b377b43643d46`;
- reviewed report path;
- disposition;
- challenges/evidence/results;
- qualifications that must survive reconciliation;
- any additional evidence required;
- any required repository or external surface that could not be inspected.

Do not edit the reviewed report in place. A report revision would be a new evidence revision and,
if promotion still depends on it, must receive its own applicable independent review.

## Stop boundary

Stop after the adversarial review artifact is persisted.

Do **not**:

- edit canonical architecture/specifications;
- update the research register to `CONCLUDED`;
- promote downstream research;
- implement analytics;
- move current runtime fields;
- perform reconciliation;
- modify issue #453;
- retire the workspace.

Those actions depend on the review disposition and belong to a later reconciliation slice.

## Expected handoff

Return only the review result and evidence coordinates needed for the next actor:

- independence status;
- review baseline SHA;
- workspace branch;
- exact adversarial-review commit SHA;
- adversarial-review path;
- reviewed report commit SHA;
- disposition;
- concise list of qualifications or blockers;
- whether durable reconciliation may proceed.

Do not paste the full review artifact into chat after it has been persisted.
