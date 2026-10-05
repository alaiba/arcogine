# Handoff — Factory-design game diagnostic-evidence research

Execute the existing **READY** research question:

> What minimum player-facing diagnostic evidence contract can the game build from supported
> non-spatial simulation facts so every displayed diagnosis is mechanically traceable, explicit
> about its method, and able to refuse unsupported causal attribution?

This is a bounded **Game / Challenge consumer research investigation**. It is not an implementation
slice, not a simulation-analytics ownership decision, and not architecture reconciliation.

## Workspace and custody

Use this temporary research-evidence workspace:

- branch: `workspace/factory-design-game-diagnostic-evidence`

Persist the completed report under a semantic path such as:

`workspace/research/investigations/factory-design-game-diagnostic-evidence-report.md`

Put any new research-custody experiments, generated fixtures, blinded walkthrough material, or
supporting notes under the same `workspace/research/` tree.

The workspace is temporary custody and must not be merged to `main`.

## Start-of-run grounding

Resolve **live `main` anew** and record its exact SHA as the research baseline. This handoff was
prepared when `main` was:

`4f11dbcfac183223ed3974525c4c28007888394d`

but that is context, not authority for the run.

Read and follow current live-main versions of:

- `AGENTS.md`
- `.github/agents/researcher.agent.md`
- `docs/development/researching.md`
- `docs/development/testing.md`
- `docs/research/research-register.md`
- `docs/research/investigations/factory-design-game-diagnostic-evidence.md`
- `docs/research/investigations/factory-design-game-strategy-space.md`
- `docs/planning/factory-design-game-vertical-slice.md`
- `docs/planning/factory-design-game-consumer.md`
- `docs/architecture/overview.md`
- `docs/architecture/engine-semantics.md`
- `docs/architecture/runtime-contract.md`
- current Factory runtime observations/events and their conformance tests;
- current Challenge attempt/evaluation/comparison surfaces;
- the current `product/research-experiments/` substrate and relevant fixtures/oracles.

Perform the normal docs/product semantic-neighbor search. At minimum search for:

- diagnostic evidence
- waiting / waiting-by-step
- occupancy
- utilization
- bottleneck / constraint
- starved / surplus
- controlled comparison / attempt comparison
- `busyTicks`
- `combinedQueueDepth`
- `RuntimeObservation`
- `RuntimeEventPayload`
- `WaitingWorkByStepOracle`
- `ProcessingOccupancyOracle`
- `DispatchProfileOracle`
- `CompletionTickOracle`

Search results are discovery only. Fetch every load-bearing source at the exact research baseline
before relying on it.

## Relationship to the reopened analytics-ownership research

The simulation-analytics ownership investigation is **not settled**. Its first Phase 2 report was
independently reviewed with disposition `REOPEN`.

Exact review evidence, for context after the game requirement has been reconstructed from its own
brief and evidence:

- workspace branch: `workspace/simulation-analytics-ownership-phase-1`
- adversarial review commit: `2c85fe7c03c6256c9e8a2ce73fde5091b3357e3c`
- review path:
  `workspace/research/investigations/simulation-analytics-ownership-phase-2-adversarial-review.md`
- reviewed Phase 2 report:
  `6a5c043563e3bd89fae6826f830b377b43643d46`

Do **not** let the reopened analytics report prescribe what the game must need.

Derive the game's diagnostic requirements independently from the diagnostic brief, current supported
runtime evidence, the concluded strategy-space reference, and the product decision at stake.

Only after that derivation, use the analytics review as dependency context:

- it established that hypothetical reusability is insufficient to create a shared analytics owner;
- it asks for a **concrete bounded measurement/use requirement** before prospective ownership is
  finalized;
- therefore this game research should clearly identify any concrete named measurement requirement it
  actually needs, but **must not choose that measurement's reusable semantic owner**.

A valid result is that the first playable diagnostic contract does **not** need a generic utilization,
occupancy, bottleneck, or other reusable measurement. Do not manufacture one merely to unblock the
analytics investigation.

## Decision to make

Determine whether the smallest non-spatial playable path can expose a mechanically truthful,
inspectable diagnostic surface from current supported evidence, and identify the minimum
player-facing evidence requirements that should later be promoted into the game consumer plan.

The result must answer:

1. What information must the player be shown to diagnose the bounded reference challenge truthfully?
2. Which of those items are direct supported Engine/Factory facts versus research-local derivations
   versus named interpretations?
3. For every derived/interpretive claim, what exact supported evidence licenses it?
4. Which requested diagnoses must explicitly refuse an answer?
5. Does the first playable slice actually require any **concrete named measurement definition** whose
   semantic owner cannot be left local/undefined?
6. If yes, state the measurement/use requirement precisely enough for the reopened analytics
   ownership research to decide its owner later:
   - purpose / player decision it supports;
   - exact semantic question the measurement answers;
   - required evidence inputs;
   - interval/population/grouping/normalization choices that are material;
   - refusal conditions;
   - whether the game needs the value itself or only a weaker factual statement.
7. If no such reusable measurement is necessary, say so explicitly and state the weaker evidence
   contract that suffices.

Do not answer question 5 by assuming that utilization or occupancy is desirable. Make the evidence
contract earn every derived quantity it requires.

## Candidate evidence contracts

Evaluate the candidate information contracts already defined by the brief:

1. **Facts only**
   - queue/waiting state;
   - resource activity;
   - completion progress;
   - attempt outcome facts;
   - no synthesized diagnostic verdict.

2. **Facts plus named interpretation**
   - the same factual basis;
   - a named diagnostic method;
   - exact supporting quantities/facts visible or inspectable.

3. **Claim-evidence bundle**
   - concise player-facing diagnosis;
   - mechanically traceable evidence chain;
   - explicit refusal when available evidence does not license the requested conclusion.

You may refine these candidates if executable evidence exposes a materially distinct contract, but do
not turn the investigation into UI design.

The selected result may mix presentation forms by diagnostic question if one uniform contract is
artificial. If so, explain the minimum invariant rather than forcing one display pattern everywhere.

## Mandatory fixture basis

Use the concluded strategy-space reference challenge as the primary deterministic mechanical basis
unless a current-main change requires explicit revalidation.

Preserve all accepted qualifications from
`docs/research/investigations/factory-design-game-strategy-space.md`, especially:

- shared eligibility is constructive, not necessary;
- occupancy is descriptive measurement, not a causal constraint definition;
- the most-occupied pool can be **not** the effective constraint;
- result-affecting resource identity/order must be explicit;
- mechanical ground truth is not player comprehension;
- the selected reference proves a bounded case, not general factory-design realism.

At minimum the corpus must cover the diagnostic brief's required cases:

- a true capacity-constraint case;
- capacity added at the effective constraint;
- capacity added away from it;
- constraint migration after relief;
- starvation versus surplus;
- multi-eligible waiting where per-machine queue depth is misleading;
- a long unfinished step where completion-credited `busyTicks` is misleading;
- `concurrency > 1` defeating naïve utilization arithmetic;
- a deliberately confounded multi-variable attempt comparison.

Prefer reusing/revalidating existing tracked research-experiment fixtures where they truthfully cover
the case. Add research-custody fixtures only where the current corpus lacks the required discriminator.

## Fixed diagnostic questions

For every fixture, derive ground truth **before** evaluating the player-facing candidate:

1. What work is waiting, and for which operation step?
2. Which resource/operation is the constraint under the candidate's **named** diagnostic method, if
   the candidate uses one?
3. Is a named idle resource starved, surplus, or not decidable from the selected evidence?
4. Which supported interval/category accounts for the largest measured delay that the selected
   evidence can actually distinguish?
5. For a controlled one-variable attempt pair, what authored fact changed and what authoritative
   outcome fact changed?
6. For a confounded pair, which causal attribution must be refused?

Do not tune the oracle or interpretation after seeing which presentation candidate looks best.

## Mandatory evidence audits

For every candidate contract, perform:

### Traceability audit

Map every player-visible statement to:

- exact supported fact(s);
- supported event interval(s); or
- a named, explicit derivation.

If a claim depends on a research-local oracle, state that the oracle proves feasibility/ground truth
only. It does not silently become production semantics.

### Reconstruction test

Show that every derived claim can be recomputed from the declared supported input set without:

- scheduler internals;
- handler/store internals;
- hidden mutable state;
- replaying/re-deciding Engine ranking or dispatch choices.

### Counterexample test

Run the misleading cases and prove the candidate does not overclaim.

Mandatory examples include:

- multi-eligible waiting is not assigned to one physical machine before binding;
- `combinedQueueDepth` is not presented as a physical queue count;
- `busyTicks` is not labeled instantaneous utilization;
- highest occupancy is not automatically labeled the bottleneck/constraint;
- resource identity/order effects are not silently attributed to another cause.

### Refusal test

For confounded or underdetermined cases, require an explicit result such as:

- not attributable from this evidence;
- not decidable from this evidence;
- measurement unavailable because the evidence window/basis is incomplete.

A guessed diagnosis is a failure.

### Controlled-mutation test

Vary one authored input at a time and confirm that the claim/evidence bundle changes only where the
authoritative outcome/evidence changes.

### Terminology audit

Reject terminology whose ordinary meaning overstates the actual semantics even when the number is
correct.

## Special ownership-demand output

Because this research now feeds the reopened analytics ownership investigation, include a separate
section in the report:

### Concrete reusable-measurement demand

Classify each player-facing diagnostic requirement as one of:

- **Direct supported fact** — no new reusable measurement definition required.
- **Game-local presentation/interpretation** — the game can own the named explanatory method without
  a generic cross-consumer definition.
- **Concrete reusable measurement candidate** — the game genuinely needs a named measurement whose
  exact definition/evidence contract is not merely presentation and could plausibly require Engine
  or consumer-neutral ownership.
- **Unsupported / must refuse** — current evidence cannot truthfully supply it.

For every **concrete reusable measurement candidate**, provide:

| Item | Required content |
|---|---|
| Player/product purpose | What decision or understanding it enables |
| Measurement question | The exact quantity/relationship requested |
| Inputs | Supported facts/events/model facts required |
| Basis choices | Interval, grouping, population, normalization, availability/concurrency treatment, etc. |
| Completeness | What evidence window/provenance is required |
| Refusal | When no truthful value may be emitted |
| Reuse need | Why this must mean the same outside this one presentation, if that is actually required |
| Ownership | **Leave unresolved here**; hand to simulation-analytics ownership research |

If no requirement meets that bar, explicitly record:

> The first non-spatial playable diagnostic contract does not currently establish a concrete reusable
> analytics requirement.

That is a successful research outcome, not a failure.

## Utilization and occupancy discipline

Do not start from the assumption that the game needs a metric named `utilization`.

If a weaker statement suffices, prefer it. Examples of potentially sufficient evidence include:

- “Resource A processed work for N supported machine-ticks over this interval.”
- “Work for operation STEP waited for N total ticks.”
- “Adding capacity at STEP changed completion from X to Y while a comparable addition elsewhere did
  not.”

If a normalized utilization-like metric is truly required, the report must state which choices are
material, such as:

- elapsed interval;
- numerator semantics (completion-credited processing versus actual occupancy interval);
- denominator / capacity basis;
- concurrency;
- offline/unavailable time;
- running work at the boundary;
- grouping by resource versus eligible pool;
- treatment of incomplete evidence.

Do not select a universal formula merely because one is conventional.

## Constraint / bottleneck discipline

Do not infer a causal constraint from occupancy alone.

A named constraint/bottleneck claim must:

- state the method;
- expose its supporting evidence;
- survive the known misleading-occupancy counterexample;
- distinguish measured state from intervention-based or comparative evidence;
- refuse stronger causal wording when the evidence only supports correlation/descriptive load.

Controlled marginal interventions in the strategy-space reference are ground truth for that bounded
reference, not a universal production diagnostic algorithm.

## Comparison discipline

For controlled one-variable retries:

- identify the authored change;
- identify the authoritative outcome delta;
- do not assert an unobserved mechanism merely because deterministic execution reproduces the
  association.

For multi-variable/confounded comparisons:

- present the change set and outcome delta;
- refuse unique causal attribution unless an explicit method and evidence genuinely support it.

Game/Challenge scoring remains separate from generic simulation diagnostics.

## Product-owner walkthrough

The brief permits a blinded product-owner walkthrough after the mechanical evidence audit.

Do not fabricate this evidence.

If the repository owner is available in the research session, prepare a blinded packet:

- shuffle fixture identities/order;
- hide oracle/result notes;
- present only the candidate player-facing evidence;
- ask the fixed diagnostic questions;
- reveal oracle afterward;
- record ambiguities/misleading wording.

Treat:

- owner failure as useful falsification;
- owner success only as a qualitative smoke test, never population-level validation.

If no real product-owner interaction is available in the research session, persist a compact blinded
walkthrough packet in the research workspace and state that the mechanical investigation is complete
but the brief's owner-smoke-test exit criterion remains outstanding. Do **not** substitute an AI
answer for the owner.

## External evidence

Use external evidence only when it materially discriminates:

- truthful diagnostic method;
- misuse of utilization/occupancy as bottleneck identity;
- refusal/causal-attribution discipline;
- presentation of diagnostic evidence.

Do not turn this into a generic HCI or simulation-diagnostics literature review.

Population-level player comprehension is explicitly out of scope.

## Expected report content

The report must include at least:

- exact research baseline and final current-main recheck;
- authority/non-goal statement;
- exact fixture/model/input provenance;
- candidate evidence-contract definitions;
- fixed diagnostic-question oracle derivations;
- claim-to-evidence matrix;
- reconstruction results;
- counterexample results;
- refusal results;
- controlled-mutation results;
- terminology audit;
- product-owner walkthrough result or exact outstanding walkthrough packet;
- **Concrete reusable-measurement demand** section described above;
- result for the playable diagnostic requirement set;
- exact missing fact/contract if no candidate succeeds;
- explicit separation of:
  - Engine/runtime facts;
  - game-owned presentation;
  - game-owned named interpretation;
  - reusable measurement candidate(s) whose ownership remains unresolved;
- downstream consequence for the reopened simulation-analytics ownership research.

## Decision-quality outcomes

A positive result may conclude:

> A bounded player-facing diagnostic evidence contract is mechanically truthful from current supported
> evidence.

That conclusion must be accompanied by the exact bounded requirement set and qualifications.

A negative result may conclude:

> Current supported evidence cannot support the required diagnostic contract.

Then identify the precise missing supported fact, evidence range, semantic definition, or product
constraint. Do not invent new Engine facts merely to force a positive result.

The report must separately state whether it found a concrete reusable measurement requirement.

## High-risk / review boundary

This question is marked **High** in the research register.

Follow the current risk/adversarial-review requirement from `docs/development/researching.md`.
If current policy requires an independent adversarial review before promotion, do not perform that
review in the same run and do not claim the research is promotable without it.

Persist the investigation first. A later independent reviewer must bind to the exact report revision.

## Non-goals

Do not:

- implement the game;
- implement or create an analytics module;
- decide reusable analytics ownership;
- reopen Engine dispatch/ranking semantics;
- change `combinedQueueDepth`;
- define a universal utilization formula unless the concrete game requirement itself demands one;
- define a universal bottleneck algorithm;
- define transfer/spatial diagnostics before executable transfer evidence exists;
- create scoring/level/tutorial design;
- claim player comprehension from mechanical evidence or one owner walkthrough;
- promote controlled-retry research automatically;
- update architecture, planning, or the research register in the same run;
- perform durable reconciliation;
- merge the research workspace.

## Stop boundary

Stop after:

1. the investigation report and any necessary research-custody evidence are persisted;
2. all mechanically executable evidence required by the report has been run;
3. the product-owner walkthrough has either been truthfully completed or an exact blinded packet has
   been persisted and the remaining exit condition is clearly stated;
4. the report states whether a concrete reusable-measurement demand exists;
5. the report identifies what the reopened analytics ownership research should consume next.

Do not continue into analytics ownership revision or game implementation in the same run.

## Expected handoff

Return:

- research baseline SHA;
- final live-main recheck SHA;
- workspace branch;
- exact report commit SHA;
- report path;
- any experiment/fixture/walkthrough artifact paths and the exact containing commit;
- whether the product-owner walkthrough is complete;
- research result (positive / negative / incomplete only because owner walkthrough remains);
- whether a concrete reusable-measurement demand was established;
- concise list of such measurement candidates, if any, **without assigning reusable ownership**;
- whether independent adversarial review is required next.

Do not paste the full report into chat after persistence.
