# Adversarial review handoff — Factory-design game non-spatial strategy space

## Review identity

Perform a genuinely independent adversarial review of this exact completed research report:

- **Workspace branch:** `research/factory-design-game-strategy-space`
- **Reviewed report commit:** `df23807640d68512d3ac9bee1b189d462c9efe08`
- **Reviewed report path:** `workspace/research/investigations/factory-design-game-strategy-space-report.md`
- **Report research baseline:** `3479053f304e43ec57d7a5fd69799ae09b84b7fd`

This handoff is persisted on the same finite research-evidence workspace. The reviewed report revision above is immutable evidence and must remain reachable. Do not amend, rewrite, rebase away, or force-push that report commit.

At handoff creation, live `main` is:

`335908a154170655d4a6f694db8826980dee4547`

Resolve live `main` again at review start and record the exact review baseline. Do not assume the handoff-time SHA is still current.

## Role and authority

Operate as the Arcogine **Researcher in adversarial-review mode**.

Read and follow:

1. `AGENTS.md`
2. `.github/agents/researcher.agent.md`
3. `docs/development/researching.md` in full
4. `docs/research/research-register.md`
5. `docs/research/investigations/factory-design-game-strategy-space.md`
6. the current architecture, planning, implementation and tests materially relevant to the question

Research is evidence, not architecture adoption. The review determines whether the report's load-bearing conclusion survives an independent falsification attempt. It does not reconcile product requirements, change Engine/Factory semantics, or admit implementation.

Use the repository's four adversarial-review dispositions exactly:

- `ACCEPT`
- `ACCEPT WITH QUALIFICATIONS`
- `MORE EVIDENCE REQUIRED`
- `REOPEN`

## Independence and anchoring control

Do **not** begin by deeply reading the report's conclusion, recommendation, adversarial-analysis section, or durable-consequence section.

First verify only that the complete report exists at the coordinate above. Then, before substantive report consumption:

1. re-ground on current live `main`;
2. read the bounded research brief;
3. independently reconstruct the decision at stake and current supported Factory/Engine/Challenge constraints;
4. identify plausible candidate answers, including a null/negative answer;
5. identify likely failure/adversarial cases;
6. identify what evidence would discriminate those cases.

Only after that reconstruction should you read the report in full and compare its reasoning, evidence, operational definitions and conclusions against your independent model.

If you cannot achieve genuine reviewer independence, do not claim an independent adversarial review. Persist the work as a non-independent critique instead.

## Current-main semantic neighbors to inspect

At minimum inspect current versions of:

- `docs/architecture/factory-model.md`
- `docs/architecture/factory-resource-semantics.md`
- `docs/architecture/engine-semantics.md`
- `docs/architecture/overview.md`
- `docs/planning/factory-design-game-consumer.md`
- `docs/planning/factory-design-game-vertical-slice.md`
- `docs/planning/factory-design-game-challenge-readiness.md`
- `docs/research/investigations/factory-design-game-diagnostic-evidence.md`
- `docs/research/investigations/simulation-analytics-consumer-boundary.md`
- `docs/research/investigations/factory-design-evolution.md`
- `docs/research/investigations/factory-setup-changeover-semantics.md`
- `docs/research/investigations/factory-resource-characteristics-requirements.md`
- `docs/research/investigations/engine-evolution.md`
- `docs/research/investigations/engine-shared-resource-final-tiebreak.md`
- the current Factory research substrate and the current Factory/Engine implementation/tests needed to verify supported semantics

Search current repository/docs for semantic neighbors including:

- `shared eligibility`
- `MachineId`
- `combinedQueueDepth`
- `constraint`
- `bottleneck`
- `occupancy`
- `waiting work`
- `resource-dependent performance`
- `setupTime`
- `capacityLiters`
- `equipment-value discovery`
- `dominated`
- `projection`
- `determinism`
- `stochastic`

Treat search as discovery only; fetch any relied-on path at the exact review baseline.

## Core question to review

The original bounded question is:

> Can a deliberately small, fixed non-spatial production challenge produce several materially different, explainable viable designs using only current Arcogine production/routing/resource-capacity semantics, one fixed quantity-bearing production requirement, a bounded game-owned equipment catalogue and construction cost, and a game-owned budget and completion target?

The report's load-bearing conclusion is a **qualified positive**: dedicated-only capacity largely collapses into monotone bottleneck purchase, while a bounded catalogue containing a multi-step-eligible resource can produce non-nested, explainable capacity/capital alternatives under current semantics.

Your job is not to defend or reject that conclusion. Try to falsify it.

## Required independent challenges

After the anchoring-control reconstruction above, explicitly challenge at least the following. These are **attack surfaces**, not expected findings or predetermined qualifications.

### 1. Occupancy measurement versus "constraint" interpretation

The report operationalizes an active constraint through eligibility-pool occupancy and uses constraint migration as a proving case.

Challenge whether:

- maximum pool occupancy is merely a useful research-local measurement or actually warranted as "the active constraint";
- the capacity-intervention proving cases establish the same conclusion without assuming the occupancy definition;
- adding capacity at the identified pool materially improves completion;
- similar-cost capacity elsewhere has null or materially smaller effect;
- relieving the first limiting area makes a different intervention effective or exposes a new limitation;
- any claimed migration can be stated causally from supported evidence without turning occupancy into a repository-wide bottleneck definition.

Check the occupancy denominator against the actual fixtures. If all relevant resources remain continuously online, say so and bound the conclusion accordingly. If any availability transition exists, verify that the calculation accounts for effective available capacity rather than blindly using elapsed time × concurrency.

A report conclusion should not survive merely because its own research-local definition names the winner.

### 2. Resource identity / projection-order sensitivity

The report found flex/shared-resource designs sensitive to the Engine's final `MachineId` tie-break and tested two projection orders.

Challenge whether the positive pooling result survives a stronger identity-order attack:

- Are the two tested orders genuinely discriminating extremes, or can other reasonable permutations materially change frontier structure or qualifying designs?
- Does the **existence** of a non-trivial shared-capacity strategy space survive even when individual completion times move?
- Could a semantically irrelevant renaming/reordering make a qualifying design cease to qualify?
- Is the conclusion really about pooling/shared eligibility, or partly about the arbitrary way canonical resource identity is allocated?
- Does fixing a deterministic order solve only reproducibility, while leaving a player-relevant but semantically arbitrary policy effect?

Do not infer that this observation itself promotes the separate shared-resource final-tie-break research question. On current `main`, that question is a **CANDIDATE** whose promotion remains gated by the retained dispatch reopening triggers.

### 3. Projection-order ownership

The report recommends that a challenge fix deterministic projection order as challenge-owned content.

Challenge that ownership claim against current repository authority:

- no production game/Challenge-to-Factory projector exists today;
- Challenge remains independent of Factory;
- current planning treats canonical Factory projection as a future consumer-side integration responsibility;
- current Engine tie-breaking remains authoritative current semantics.

A narrower conclusion may survive:

> any future projector operating under current Engine semantics must make result-affecting resource ordering explicit and deterministic.

Do not accept "challenge-owned" as established merely because the research-local experiment chose an ordering.

### 4. Mechanical strategy space versus human choice/discovery space

Challenge any inference that mechanically dominated or equivalent catalogue choices are necessarily poor game content.

Current product research now separately recognizes that a human challenge may include:

- genuine mechanical trade-offs;
- dominated alternatives;
- mechanically equivalent alternatives at different prices;
- bargains;
- cosmetically/presentation-attractive but mechanically irrelevant differences;
- player inference from price, branding or appearance.

The strategy-space investigation should establish the underlying **mechanical ground truth**, not whether every offered item must survive an optimizer's dominance filter.

Conversely, do not use possible human confusion, aesthetics or perceived value to rescue a mechanically trivial kernel inside this question. Those are separate empirical/product questions.

Challenge whether the report cleanly keeps:

- mechanical value;
- offered choice-set structure;
- perceived/presentational value; and
- player preference

separate.

### 5. Economics and robustness

Attack whether the flex-positive result is an artifact of the chosen economic construction.

Independently inspect:

- the pass-1 and pass-2 preregistration sequence;
- the reason the first discriminator was revised;
- the 5% materiality rule;
- price bands;
- budget slack;
- completion-target construction;
- nearby perturbations;
- whether the qualifying frontier relies on accidental equalities or very narrow numerical tuning.

The report observed that the flex effect is useful in a bounded price region and weakens greatly when priced around 2× the dearer dedicated substitute. Treat any such numerical band as evidence for this bounded experimental family, not a universal equipment-pricing law.

Ask whether a reasonable alternative materiality criterion changes the qualitative conclusion, and report sensitivity rather than silently substituting a preferred threshold.

### 6. Fixture vocabulary versus transferable mechanism

`CUT`, `ASSEMBLE` and `INSPECT` are research-fixture labels, not Arcogine production ontology.

Challenge whether the report's transferable claim can be stated more abstractly:

- serial operation routing;
- heterogeneous step durations;
- dedicated capacity;
- one or more resources eligible for multiple steps;
- fixed workload;
- bounded capital constraints.

Do not generalize from the tested three-step serial topology to branching routings, longer routings, multiple products, multiple orders, spatial transfer, or other unsupported structures.

If the conclusion only survives for the tested family, narrow it rather than rejecting a correctly bounded finding.

### 7. Dedicated-only negative under current semantics

The report's dedicated-only negative is specifically under current Engine/Factory semantics.

Current `main` now makes several adjacent uncertainties explicit:

- processing duration belongs to the operation step, not selected resource;
- `setupTime` is canonical/observable but not currently executed;
- `capacityLiters` is canonical/observable but not currently executed;
- setup/changeover semantics are a separate READY question;
- resource-characteristic/operation-requirement semantics are a separate READY question;
- operation-resource-dependent performance is a separate CANDIDATE.

Challenge whether the report correctly limits its negative conclusion to **current** dedicated-capacity semantics.

Do not treat the inability to express faster/slower dedicated equipment today as evidence that such semantics should or should not be added. Future resource-dependent performance, setup/changeover or qualification semantics are reopening conditions for the product-space conclusion, not implied defects in this investigation.

### 8. Twin-versus-singles control

The report found that one twin-concurrency assembler and two otherwise equivalent single-concurrency assemblers have the same execution consequence for equal total concurrency in the tested semantics.

Challenge any stronger interpretation.

The supported conclusion may be:

> under current semantics and this workload family, machine granularity adds no mechanical execution distinction beyond total concurrency.

It does **not** imply that two game catalogue offers with identical execution behavior have no product value. Different price, availability, presentation, aesthetics or player inference can still matter at the Challenge/product layer.

Keep this control as mechanical evidence only.

### 9. Determinism versus realism

Do not require stochastic simulation as a precondition for reviewing a report about current deterministic Engine semantics.

Arcogine's determinism contract already allows future explicit random context while preserving reproducibility. Processing-time variability, failures, repairs, quality/yield uncertainty and stochastic arrivals are separate semantic questions.

Challenge only overclaiming:

- deterministic execution does not by itself establish industrial realism;
- this report establishes behavior under current fixed semantics;
- future stochastic semantics could change strategy robustness and would be a legitimate reopening trigger for a broader product claim.

Do not propose random resource selection as a substitute for resolving deterministic dispatch semantics.

### 10. Meaning of "meaningful" and "explainable"

Whenever the report says the design space is "meaningful", "interesting", "explainable", or similar, separate what the evidence actually establishes.

The headless experiment can establish, for example:

- multiple mechanically non-identical designs;
- non-dominance under declared dimensions;
- material performance differences;
- controlled intervention effects;
- shared-capacity usage visible in supported evidence;
- bounded economic trade-offs.

It does **not** establish:

- player comprehension;
- player engagement;
- population-level discoverability;
- fair presentation;
- learning effectiveness.

Those remain with the diagnostic-evidence, equipment-value-discovery, scoring/level and external-player-validation questions as applicable.

If necessary, qualify language rather than rejecting the mechanical result.

## Additional falsification questions

Also attempt to discover independently:

- an omitted candidate explanation for the observed flex advantage;
- a reasonable design family inside the brief's scope where the purported pooling mechanism disappears;
- a frontier-classification bug caused by dominance dimensions, target feasibility or materiality arithmetic;
- a workload/order construction that manufactures multiplicity rather than exposing a persistent structural trade-off;
- any use of hidden Engine state or unsupported facts;
- any mismatch between raw result files and summarized counts;
- any non-deterministic or unreproducible load-bearing case;
- any stale current-main semantic change that invalidates the report's assumptions;
- any conclusion stronger than the evidence, especially a claim of necessity where the experiments only show possibility.

Do not manufacture objections merely to produce findings.

## What is deliberately not required

Do **not** fail the report merely because it lacks:

- stochastic processing/failure/arrival semantics;
- human participant evidence;
- spatial mechanics;
- resource-dependent speed;
- setup/changeover execution;
- generalized resource characteristics;
- a production game-to-Factory projector;
- a resolved reusable analytics architecture.

Those are separate questions unless the report itself relies on them.

Likewise, do not treat branch-local or still-unreconciled work as current authority.

## Evidence execution

You may inspect and re-run the persisted experiment assets from the reviewed report revision where needed. Preserve the distinction between:

- repository fact;
- experimental observation;
- research-local derivation;
- game-owned research parameter;
- inference/recommendation.

When reproducing or extending a test, use supported Factory/Engine control and evidence surfaces only. Do not inspect scheduler, handler or store internals to rescue or falsify a result.

If a new experiment is needed for adversarial discrimination, persist it under this same research workspace with clear custody and distinguish it from the original preregistered experiment. Do not rewrite the original protocols or raw outputs.

## Review result

The completed adversarial review must state:

- exact reviewed report coordinate;
- exact live-main review baseline;
- independence method;
- the independently reconstructed candidate set and failure cases;
- each material challenge actually exercised;
- evidence considered or newly generated;
- whether each challenge falsified, qualified or left the report unchanged;
- one of the four review dispositions;
- the surviving load-bearing conclusion, if any, in its narrowest justified form;
- qualifications that must survive durable reconciliation;
- unresolved unknowns;
- whether more evidence is required before reconciliation.

If the pooling result survives but occupancy interpretation, projection ownership, transferability or human-facing language are too strong, prefer `ACCEPT WITH QUALIFICATIONS` over rewriting the research question. If a viable omitted model or identity-order artifact destroys the load-bearing positive, use `REOPEN`. If no falsification is established but the current evidence cannot discriminate a material challenge, use `MORE EVIDENCE REQUIRED`.

Do not choose a disposition in advance.

## Review custody

Persist the completed review under a semantic path in:

`workspace/research/investigations/`

on this same research workspace when practical.

Do not alter the original report revision. A completed review gets its own exact commit SHA + path identity.

Before handing the review off:

1. re-resolve live `main`;
2. record whether it changed materially during review;
3. commit the completed review;
4. return:
   - workspace branch;
   - exact review commit SHA;
   - review path;
   - reviewed report commit SHA;
   - live-main review baseline;
   - final live-main SHA checked;
   - adversarial-review disposition.

Research remains unreconciled after the review. Do not mark the strategy-space question `CONCLUDED`, modify canonical product/architecture/specification authority, or admit implementation as part of the adversarial pass.
