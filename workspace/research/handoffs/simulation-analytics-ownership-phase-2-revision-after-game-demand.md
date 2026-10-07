# Handoff — Reopened Phase 2: analytics ownership boundary, concrete-demand anchored and horizon-tested

Continue the existing **Simulation analytics ownership boundary** research after the independent
adversarial review returned `REOPEN`.

This is a bounded revision of **Phase 2 — Consumer-neutral analytics admission boundary**. Do not
repeat Phase 1 from scratch, do not reopen the completed game diagnostic-evidence question, and do
not reconcile architecture or implement an analytics capability in this run.

The purpose of this revision is to use the now-accepted game diagnostic evidence to resolve the
prospective ownership rule without overfitting the answer to the first two concrete measurements.

## Active workspace

Use the existing finite research workspace:

- branch: `workspace/simulation-analytics-ownership-phase-1`

Persist the revised completed Phase 2 report under a new semantic path, for example:

`workspace/research/investigations/simulation-analytics-ownership-phase-2-admission-boundary-revision.md`

Do **not** overwrite the previously reviewed report or adversarial review. They are immutable evidence
coordinates for this reopened question.

## Required exact prior evidence

### Original Phase 2 report and REOPEN review

- original Phase 2 report commit:
  `6a5c043563e3bd89fae6826f830b377b43643d46`
- path:
  `workspace/research/investigations/simulation-analytics-ownership-phase-2-admission-boundary.md`
- independent adversarial-review commit:
  `2c85fe7c03c6256c9e8a2ce73fde5091b3357e3c`
- path:
  `workspace/research/investigations/simulation-analytics-ownership-phase-2-adversarial-review.md`
- disposition: **REOPEN**

The reopened review is mandatory evidence. Preserve all conclusions that survived it unless new
evidence falsifies them, and directly address every load-bearing reopening reason.

### Game diagnostic-evidence report and accepted review

The separate game investigation has now supplied the concrete demand evidence Phase 2 previously
lacked.

- game workspace:
  `workspace/factory-design-game-diagnostic-evidence`
- game report commit:
  `4ad00136441fa29e6b462ac5431b60cf2de1e148`
- report path:
  `workspace/research/investigations/factory-design-game-diagnostic-evidence-report.md`
- independent game-review commit:
  `6484795ff4c4ad971e06c70751b03805e54a55f5`
- review path:
  `workspace/research/investigations/factory-design-game-diagnostic-evidence-adversarial-review.md`
- disposition: **ACCEPT WITH QUALIFICATIONS**

The exact game review qualifications are binding input. Do not consume the game report without its
review.

## Important interpretation of the game stopping point

The game diagnostic investigation stopped because it had made enough progress to answer its own
bounded question and further iteration would have turned the spike into a broader game curriculum
programme.

It **did not** stop because utilization and starvation are the only desired diagnostic concepts.

Treat that stopping point as a **research-management boundary**, not as a product-scope verdict.

The accepted game evidence distinguishes:

1. **Concrete forcing use cases now**
   - utilization over a stated period;
   - flow characterization of idle capacity / starvation.

2. **Near-term diagnostic horizon already exposed by the same exploration**
   - waiting-by-step attribution;
   - machine/slot occupancy intervals and timelines;
   - bottleneck / constraint inference under a named method;
   - shifting bottleneck / migrating constraint;
   - co-binding constraints;
   - excess-capacity / "needed" analysis through interventions;
   - controlled run/attempt comparison;
   - refusal of unique attribution for confounded changes;
   - active-period or equivalent temporal constraint analysis where a method warrants it.

3. **Future semantic horizon that must not be designed here**
   - availability/downtime-aware diagnostics;
   - arrival/release policies and rolling workloads;
   - multiple products;
   - finite buffers / blocking;
   - setup/changeover;
   - transfer/layout decomposition;
   - stochastic failures, durations, arrivals or yield.

The two concrete forcing use cases prove that the ownership question is **not speculative**. The
near-term horizon is design pressure used to test whether the ownership rule will survive the next
few diagnostic concepts without restructuring. The future horizon is only a compatibility/reopening
check; do not invent formulas or unsupported facts for semantics that do not exist yet.

## Start-of-run grounding

Resolve live `main` anew and record its exact SHA as the revised research baseline.

This handoff was prepared when live `main` was:

`306205c33ef93f0433a26c92f85cbd82eadd27f8`

but the run must not assume that remains current.

Read current live-main versions of:

- `AGENTS.md`
- `.github/agents/researcher.agent.md`
- `docs/development/researching.md`
- `docs/research/research-register.md`
- `docs/research/investigations/simulation-analytics-consumer-boundary.md`
- `docs/research/investigations/simulation-analytics-evidence-provenance.md`
- `docs/research/investigations/factory-design-game-diagnostic-evidence.md`
- `docs/research/investigations/factory-design-game-strategy-space.md`
- `docs/planning/factory-design-game-consumer.md`
- `docs/planning/factory-design-game-vertical-slice.md`
- `docs/architecture/overview.md`
- `docs/architecture/engine-semantics.md`
- `docs/architecture/runtime-contract.md`
- `docs/architecture/factory-design.md`
- `docs/architecture/governance-evidence.md`
- `docs/architecture/governance-conformance.md`
- the relevant current Factory runtime implementation/tests;
- the relevant current Challenge comparison/evaluation implementation/tests;
- the current `product/research-experiments/` measurement/oracle substrate.

Then resolve and read the exact prior evidence coordinates above.

Perform the normal semantic-neighbor search under `docs/` and `product/`. At minimum search for:

- `RuntimeObservation`
- `RuntimePerformanceObservation`
- `busyTicks`
- occupancy / processing intervals
- utilization
- starvation / starved / surplus
- waiting by step
- bottleneck / constraint
- active period
- co-binding
- excess capacity / needed
- comparison / `AttemptComparison`
- producer provenance
- verification objective
- analytical definition
- availability / downtime
- transfer / blocking / setup

Search results are discovery only. Fetch load-bearing current files at the exact revised research
baseline before relying on them.

## Question for this revision

> Given the established current Engine/runtime ownership boundary, the concrete game measurement
> demands, and the already-visible near-term diagnostic horizon, what prospective ownership/admission
> rule should Arcogine use for new simulation measurement and diagnostic semantics so that it is
> justified by current demand but does not require ownership restructuring as the next diagnostic
> concepts are admitted?

This is still an **ownership-boundary** question, not an analytics API/module design exercise.

## Two distinct evidence roles

Keep these separate throughout the report.

### Admission evidence

The accepted game research provides real demand:

- utilization over a period;
- idle-flow / starvation characterization.

These prove that Arcogine has a concrete consumer asking for analytical measurement beyond the
existing Engine performance observation.

They **do not by themselves prove**:

- that the definitions must be shared;
- that a standalone analytics module must exist;
- that Engine ownership is wrong;
- that the game cannot own a definition;
- that every future diagnostic belongs to one common abstraction.

### Design evidence

The broader diagnostic concept map is evidence about whether an ownership model is likely to survive
near-term growth.

Use the near-term horizon to test:

> Could the next two or three diagnostic concepts be admitted under the same ownership principle
> without moving semantic responsibility back and forth among Engine, shared analytics, and the game?

A model that works only for two scalar metrics but structurally breaks for named inference,
attribution, comparison, temporal aggregation or refusal is not a satisfactory prospective ownership
rule.

This is an **anti-rework test**, not permission to design all future concepts now.

## Candidate ownership models

Reconstruct the candidate set independently before choosing.

At minimum include materially distinct variants exposed by the previous REOPEN review:

### A. Engine-rich / explicit standardized measurement

Current Engine-owned performance results remain. A new standardized measurement may also become
Engine-owned when a concrete cross-consumer or runtime-contract requirement justifies one fixed
definition.

This candidate must not be rejected merely because Engine ownership creates coupling. Show why that
coupling is or is not justified for each proving class.

### B. Universal outside-Engine measurement

Engine owns authoritative execution state/change, result-affecting interpretation and justified
current projections; report-only measurement policy lives outside Engine.

This is close to the prior report's R2 direction. Re-test it rather than assuming it.

### C. Shared consumer-neutral analytics

A reusable analytics semantic owner defines measurements/analytical methods that multiple consumers
must interpret identically, while Engine remains authoritative for execution facts and the game owns
presentation and product-specific interpretation.

Do not equate this semantic owner with a mandatory standalone module or service.

### D. Staged / demand-driven admission

Keep a new measurement consumer-local unless/until evidence shows either:

- it must become part of the supported Engine contract; or
- multiple consumers require one shared definition.

Under this model, shared analytics is an available ownership destination, not an abstraction created
in advance.

### E. Hybrid if evidence demands it

A surviving rule may intentionally assign different classes differently. For example:

- direct execution/result facts → Engine;
- one consumer's explanatory characterization → consumer;
- reusable named measurement → shared analytics;
- standardized contractual result with Engine-wide meaning → Engine;
- cross-run experimental interpretation → consumer or named analytical method depending on reuse.

Do not force all things called "analytics" behind one owner if the evidence supports meaningful
semantic distinctions.

Add another candidate only if it changes an ownership decision rather than renaming one of these.

## Mandatory classification dimensions

For each representative concept, distinguish at least:

1. **authoritative fact/state/change**;
2. **current supported projection/result**;
3. **measurement definition** — interval, denominator, grouping, aggregation;
4. **state characterization** — e.g. starvation under an explicit evidence rule;
5. **inference method** — e.g. a named bottleneck method;
6. **comparison/experimental method**;
7. **consumer interpretation/presentation**;
8. **evidence retention/completeness requirement**.

Do not collapse these merely because one UI presents them together.

Implementation placement is not semantic ownership.

## Forcing use case 1 — utilization over a period

Use the game requirement as a real forcing case, but do not blindly promote the research-local
formula as canonical.

Required player question:

> How much of a machine's available capacity was working over a stated period?

The game research proposed, as feasibility evidence:

- working slot-ticks over a half-open period;
- concurrency-aware denominator;
- work in progress counted through the interval boundary;
- per-machine basis by default;
- potentially separate eligible-pool measurement;
- offline time excluded from available time, specified but not exercised;
- complete occupancy evidence required;
- refusal for incomplete period coverage;
- never use utilization as a bottleneck verdict.

The adversarial review adds:

- the research-local characterization was not corpus-wide audited as a final production definition;
- availability semantics expose additional edge cases;
- reuse outside the game is anticipated, not demonstrated;
- Factory Design's "maximum utilization" verification objective is a plausible second use, not an
  implemented consumer.

Decide the ownership/admission consequence, not necessarily the final utilization formula.

## Forcing use case 2 — idle-flow / starvation characterization

Required player question:

> When capacity is idle, is it idle because no relevant work can currently flow to it, because known
> work remains elsewhere in the process, or because the available evidence cannot license a stronger
> claim?

The game feasibility definition must be corrected by its adversarial qualifications:

- the always-online two-layer characterization is bounded, not a universal Engine property;
- after an availability change, an idle slot may coexist with eligible queued work;
- decide whether a third state or refusal is needed;
- decide whether "starved slot" accounting is capped by remaining eligible work;
- state the pool basis for machines eligible for multiple steps;
- flow characterization has not yet been exercised over multi-step-eligible machines or the whole
  13-design corpus;
- future arrivals need an explicit "known work" rule before claiming "no work left".

Again, decide what ownership rule should govern such a characterization; do not silently promote the
research-local definition to production semantics.

## Near-term architecture proving horizon

These concepts are **not all concrete requirements now**. Use them to falsify brittle ownership rules.

For each class below, answer:

- Does the proposed ownership rule have a natural destination for it?
- Would adding it require changing the ownership principle itself?
- What evidence would trigger Engine vs shared vs consumer-local ownership?
- Does it require new authoritative Engine facts, or only supported evidence/history?
- Does it risk becoming a second simulation engine?
- Is the concept a measurement, an inference, a comparison, a product interpretation, or a mixture?

### Waiting-by-step attribution

Preserve operation-step-first waiting. Multi-eligible waiting must not be assigned to one physical
machine before binding.

Test whether reusable waiting aggregation is naturally handled under the proposed rule.

### Occupancy intervals / timelines

These are temporal measurements over supported dispatch/completion evidence.

Test whether the rule handles complete-window/refusal semantics without making Engine retain history
by default.

### Bottleneck / constraint under a named method

The game research deliberately did **not** finish a canonical single-run method.

Use the existing falsification evidence:

- highest utilization is not automatically the constraint;
- highest carried load is not automatically the constraint;
- controlled interventions can establish bounded ground truth;
- shifting/migrating constraints exist;
- co-binding exists.

The ownership rule must have a place for a later named method without predetermining that one is
currently required.

Do not invent a universal bottleneck algorithm in this run.

### Shifting bottleneck / active-period analysis

Use only as a proving class: temporal inference can depend on intervals and method identity.

Ask whether the proposed rule can support multiple named methods and refusal without freezing one
Engine-wide definition unnecessarily.

### Co-binding

Test whether a diagnostic may legitimately return several simultaneous constraints, and whether this
is analytical interpretation rather than Engine execution semantics.

No final algorithm required.

### Excess capacity / "needed"

The game evidence shows that one-change intervention verdicts do not compose.

Test the distinction between:

- direct outcome comparison;
- experiment definition;
- causal/decision interpretation.

Do not turn "needed" into a single-run metric.

### Run / attempt comparison

Preserve:

- controlled one-variable changes;
- multi-variable/confounded change sets;
- explicit refusal of unique causal attribution;
- stable identity/order when identifying design differences.

Evaluate how Factory's landed semantic comparator and Governance `ChangeSet` constrain or reduce the
analytics responsibility.

## Future semantic horizon — compatibility only

Do not solve these. For each, state only whether the ownership principle has an obvious extension or
what would trigger reopening:

- machine downtime / availability-aware capacity;
- orders arriving over time / release policies;
- multiple products;
- finite buffers and blocking;
- setup/changeover;
- spatial transfer/layout decomposition;
- stochastic behavior.

A satisfactory ownership rule should not depend on the permanent truth of today's always-online,
single-product, release-at-once world, but this investigation must not invent semantics that are not
yet supported.

## Anti-second-engine invariant

This remains binding:

> Analytics may measure outcomes and apply declared analytical methods; it must not reconstruct or
> re-decide Engine choices.

Use the broader horizon to sharpen this.

Examples:

- reconstructing occupancy intervals from actual dispatch/completion evidence is permitted;
- aggregating waiting by authored operation step is permitted;
- comparing actual controlled reruns is permitted;
- replaying candidate ranking to infer which machine "should" have received work is not;
- inventing hidden mid-cascade state is not;
- counterfactual execution requires Engine re-execution, not analytical simulation.

For any proposed inference method, state the boundary between interpreting recorded outcomes and
recreating authoritative execution logic.

## Required anti-rework output

Include an explicit section:

### Near-term extension stress test

For the surviving ownership rule, classify at minimum:

| Concept | Demand status | Semantic kind | Natural owner under rule | New owner/architecture required? | Trigger/qualification |
|---|---|---|---|---|
| Utilization over period | concrete | measurement | ... | ... | ... |
| Idle-flow/starvation | concrete | characterization/measurement | ... | ... | ... |
| Waiting by step | evidenced | attribution/aggregation | ... | ... | ... |
| Occupancy/timeline | evidenced | temporal measurement | ... | ... | ... |
| Bottleneck named method | near-term possible | inference | ... | ... | ... |
| Shifting/active-period | near-term possible | temporal inference | ... | ... | ... |
| Co-binding | near-term possible | inference | ... | ... | ... |
| Excess capacity / needed | evidenced | intervention/comparison | ... | ... | ... |
| Run comparison | evidenced | comparison | ... | ... | ... |

Then answer directly:

> Why would admitting the next two or three game diagnostic concepts under this rule not require
> restructuring semantic ownership?

If the honest answer is that it would, the model fails the anti-rework test.

This does **not** require a common API or common implementation abstraction for every row.

## Shared-owner admission test

Because the prior review found shared analytics premature, define the minimum evidence needed to
admit consumer-neutral ownership.

Consider candidate triggers such as:

- two concrete consumers require one identical named definition;
- one durable governance/verification contract and one product consumer must share semantics;
- duplicated formulas would create correctness/compatibility risk;
- analytical provenance requires one definition identity independent of presentation;
- a public/outward contract must promise the same metric across adapters.

Also test the null:

> one concrete consumer alone may be insufficient; keep the definition consumer-local until another
> trigger appears.

Do not assume the same threshold is correct for every measurement/inference class.

## Engine-owned admission test

Likewise define when a new report-only measurement should legitimately become Engine-owned.

At minimum test:

- Does the value participate in authoritative execution? If yes, Engine ownership is strongly
  indicated.
- Is it required as a supported current-state/result contract for all Engine consumers?
- Is one fixed definition integral to the Engine interpretation rather than merely convenient?
- Would alternate legitimate definitions be suppressed by placing it in Engine?
- Are its completeness/refusal semantics compatible with the supported Engine observation contract?
- Is the coupling cost justified by a concrete semantic requirement?

Do not categorically ban new Engine measurements merely because they are derived.

## Current supported fields and busy counter

Preserve the prior descriptive Phase 1 classification unless changed current-main evidence or the
accepted owner decision requires a specific revision.

Re-evaluate only the consequences needed for this Phase 2 revision.

In particular:

- current supported performance fields do not move merely to make the new boundary aesthetically
  pure;
- `combinedQueueDepth` remains Engine decision semantics;
- `busyTicks` must not be called utilization;
- the game research contains an owner decision to remove the busy counter, but its independent review
  correctly says:
  - it is misnamed/misused as utilization;
  - at quiescence it is exact total processing job-ticks;
  - removal is an owner decision, not a mathematical necessity;
  - the durable removal scope must be exhaustive and is separate reconciliation/implementation work.

Determine how that owner decision affects the prospective rule, but do not perform the removal here.

## Evidence/provenance downstream trigger

Revisit the prior recommendation to promote
`simulation-analytics-evidence-provenance.md`.

Do not promote it merely because analytics is a plausible category.

Decide separately for each surviving shared analytical responsibility whether truthfulness requires a
reusable contract for:

- complete event/observation windows;
- retained history;
- running work at interval boundaries;
- availability history;
- model/Engine interpretation identity;
- analytical-definition identity/provenance.

A concrete game requirement may justify evidence/provenance work even when the final semantic owner is
not a standalone analytics module. State the exact trigger.

## What this revision must resolve from the prior REOPEN

Explicitly close the prior adversarial review's load-bearing issues:

1. **Omitted candidate:** test the demand-driven/staged model and explicit Engine-owned standardized
   measurement option.
2. **Strict facts-only overclaim:** distinguish failure under the **current supported evidence
   contract** from impossibility under every conceivable evidence contract.
3. **Engine-rich characterization:** distinguish semantic falsification from coupling/cost trade-off.
4. **Shared analytics prematurity:** use the game evidence as concrete demand, but still prove or
   withhold the shared-owner conclusion.
5. **R1(c) attribution wording:** distinguish authoritative attribution already fixed by execution
   from analytical attribution chosen by a method.
6. **Legacy current fields:** decide whether retaining `averageLeadTime` and
   `throughputPerTick` is a deliberate compatibility exception, staged migration, or evidence that
   the prospective rule needs refinement.
7. **Anti-second-engine rule:** show that the broader diagnostic horizon stays on the measurement /
   interpretation side.
8. **Q1–Q6 closure:** update only where the new game evidence or review qualifications change the
   previous answer.
9. **Contract-change accounting:** keep current semantic changes, future admission policy and
   implementation factoring distinct.
10. **Downstream triggers:** only promote what a concrete surviving responsibility actually needs.

## Experiments

Do not create a large new experiment programme by default.

Reuse/reverify existing executable evidence from:

- Phase 1 and original Phase 2;
- the game diagnostic evidence corpus;
- the independent game-review probes;
- tracked `product/research-experiments/` oracles.

Add a new research-custody experiment only when it materially discriminates two ownership models or
tests the anti-rework rule.

Potentially useful discriminators include:

- the same underlying occupancy evidence feeding two legitimate different analytical definitions;
- one direct measurement plus one named inference over it, showing why measurement and inference need
  not share an owner;
- availability-change evidence demonstrating why completeness/refusal belongs to the analytical
  contract rather than being hidden;
- a multi-eligible waiting case demonstrating step-first attribution;
- comparison/intervention cases showing that analysis can consume actual executions without becoming
  a simulator.

Do not implement a production analytics API as an experiment.

## External evidence

Use external sources only if they materially discriminate ownership or method identity.

Possibility precedent is not necessity.

In particular, external simulation/manufacturing tooling that places metrics in the simulation engine,
post-processing, or a dashboard may show viable placements; it does not decide Arcogine's owner.

Bottleneck literature may be useful to show that multiple named methods exist and that utilization
alone is insufficient. Do not turn this revision into a broad operations-research survey.

## Expected report conclusion

The report must state one prospective ownership/admission rule that:

- is justified by the concrete current game demand;
- distinguishes Engine, shared analytical semantics and consumer-local interpretation;
- does not create a second simulation engine;
- handles the near-term diagnostic horizon without ownership restructuring;
- does not prematurely design unsupported future semantics;
- states when a new Engine-owned measurement is legitimate;
- states when shared consumer-neutral analytics becomes justified;
- states when consumer-local ownership remains correct;
- preserves current supported results unless a separately justified change exists.

A valid outcome may be a **staged ownership rule** rather than immediate creation of a shared analytics
capability.

A valid outcome may also conclude that one of the concrete game measurements already crosses the
shared-owner threshold — but that must be shown from evidence, not inferred from the word
"reusable".

## Required report sections

In addition to the normal research report structure, include:

- exact prior evidence coordinates consumed;
- exact current research baseline and final live-main recheck;
- surviving Phase 1 facts;
- prior REOPEN findings and how each was resolved;
- concrete-demand analysis;
- candidate ownership models;
- admission tests for Engine/shared/consumer-local ownership;
- near-term extension stress test;
- anti-second-engine analysis;
- future-horizon reopening conditions;
- current-field consequence matrix;
- downstream evidence/provenance trigger decision;
- confidence by conclusion class;
- unresolved unknowns;
- durable consequences if accepted.

## Review and stop boundary

This remains **High risk**.

After the revised report is persisted, stop.

Do not:

- edit architecture/specifications;
- update the research register to `CONCLUDED`;
- update game planning;
- remove `busyTicks`;
- implement analytics;
- promote evidence/provenance automatically;
- continue into durable reconciliation;
- perform the independent adversarial review in the same run.

The **new exact Phase 2 report revision must receive a new genuinely independent adversarial review**
before any architecture/planning promotion.

## Expected handoff

Return only:

- revised research baseline SHA;
- final live-main recheck SHA;
- workspace branch;
- exact revised report commit SHA;
- revised report path;
- any new experiment paths/commit, if created;
- concise surviving ownership rule;
- whether shared consumer-neutral ownership is admitted now, staged, or rejected;
- which concrete game requirements it classifies;
- whether the near-term extension stress test passed;
- exact downstream questions newly triggered, if any;
- confirmation that a new independent adversarial review is required.

Do not paste the completed report into chat after it is persisted.
