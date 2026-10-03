# Research handoff — Factory-design game non-spatial strategy space

## Handoff identity

This is a transient research handoff for the bounded READY investigation in:

`docs/research/investigations/factory-design-game-strategy-space.md`

Research-evidence workspace branch:

`research/factory-design-game-strategy-space`

The branch was created from live `main` at:

`3479053f304e43ec57d7a5fd69799ae09b84b7fd`

Do not treat that SHA as permanently current. Resolve live `main` again at the start of the research run and record the exact research baseline required by the Researcher contract. This handoff commit is itself an immutable transient coordinate once handed off: if the workspace later needs a newer `main`, preserve this commit in history rather than rebasing or force-updating it away.

## Role and operating contracts

Operate as the Arcogine **Researcher**.

Before substantive work, read and follow, in this order:

1. `AGENTS.md`
2. `.github/agents/researcher.agent.md`
3. `docs/development/researching.md` in full
4. `docs/research/research-register.md`
5. `docs/research/investigations/factory-design-game-strategy-space.md`
6. `docs/research/report-template.md`

The research operating model is normative. This is a research investigation, not implementation and not architecture adoption. Do not modify production/runtime code, silently settle architecture, or admit implementation planning from the research run.

Use this existing branch as the bounded question's temporary research-evidence workspace. Do not create a second research branch merely because the prompt, protocol, report, adversarial review, and later reconciliation are different artifacts/phases.

## Start-of-run grounding

Resolve live `main` and record its exact SHA as the research baseline.

If live `main` has advanced since the handoff baseline above, determine whether the change is material to this question. Bring the workspace current only by a history-preserving method that leaves handed-off artifact SHAs reachable. Do not rebase or force-push away handed-off evidence.

Read the current versions of the directly relevant repository authorities, including at minimum:

- `docs/product/charter.md` where product-direction context is needed;
- `docs/architecture/overview.md`;
- `docs/architecture/factory-model.md`;
- `docs/architecture/engine-semantics.md`;
- `docs/planning/factory-design-game-consumer.md`;
- `docs/planning/factory-design-game-vertical-slice.md`;
- `docs/planning/factory-design-game-challenge-readiness.md`;
- the current Challenge catalogue/economics/admissibility/evaluation implementation and tests;
- `product/domains/factory/src/test/java/com/arcogine/factory/research/`;
- the current Factory model/runtime implementation and executable tests needed to establish what the supported non-spatial surface actually does.

Perform a quick repository/docs search for semantic neighbors using terms such as:

- `non-spatial`
- `strategy space`
- `constraint migration`
- `capacity`
- `equipment catalogue`
- `construction cost`
- `budget`
- `completion target`
- `challenge evaluation`
- `runtime observation`
- `supported events`
- `analytics`
- `occupancy`
- `waiting work`

Do not rely on search snippets as revision authority; fetch relevant paths at the exact research baseline before relying on them.

Inspect open PRs that materially affect this question. At handoff time, PR #435 (`docs: split simulation analytics research`) is open against the handoff baseline. It changes research framing around simulation analytics but explicitly does not change current Engine semantics or supported behavior. Do not treat it as landed. Re-check its live state at research start. The strategy-space investigation should not be blocked merely because generic analytics ownership is unresolved: research-local derivations over supported evidence are permitted where needed, provided they are clearly labeled and do not become game-owned or Engine/public analytics semantics by implication.

Record any important repository or GitHub surface that cannot be inspected.

## Exact bounded question

Answer the READY brief's question, not a broader game-design question:

> Can a deliberately small, fixed non-spatial production challenge produce several materially different, explainable viable designs using only current Arcogine production/routing/resource-capacity semantics, one fixed quantity-bearing production requirement, a bounded game-owned equipment catalogue and construction cost, and a game-owned budget and completion target?

The decision at stake is whether the first factory-design product kernel already contains a meaningful capacity/capital design space before spatial layout is introduced, or whether the proposed product would depend on unresolved semantics to become interesting.

A negative answer is a valid research result. Do not optimize the experiment to rescue the product hypothesis.

## Scope boundary

Keep the investigation deliberately non-spatial.

In scope:

- a small routing such as `CUT -> ASSEMBLE -> INSPECT`;
- one fixed quantity-bearing production requirement submitted once;
- current supported Factory publication/model semantics;
- current supported Engine non-spatial execution, dispatch, routing, resource capacity and deterministic session semantics;
- a bounded game-owned equipment catalogue;
- game-owned construction cost;
- a game-owned budget;
- a game-owned completion target;
- deterministic headless evaluation;
- controlled one-variable capacity interventions;
- constraint migration;
- complete feasible-frontier analysis within the declared search window.

Out of scope:

- spatial layout;
- transfer timing, paths, conveyors, buffers, congestion or transport resources;
- player-facing presentation or UI;
- population-level player comprehension;
- score/rating formulas, progression, level sequencing or tutorials;
- renderer/input technology;
- changing Engine dispatch/scheduling semantics to make the challenge interesting;
- creating multiple production orders merely to manufacture parallelism;
- selecting or implementing a generic simulation-analytics architecture.

Do not smuggle an out-of-scope capability back into the experiment under a research helper or game-owned abstraction.

## Hypotheses

Test both the null and alternative stated by the brief.

### Null

Under current non-spatial semantics, the small challenge collapses to an obvious monotone solution — effectively buy faster/more capacity until the completion target is met — or any apparent multiplicity depends on fragile/arbitrary economic tuning.

### Alternative

There is a bounded catalogue/budget/target region where all of these hold:

- adding capacity at the active constraint materially improves completion;
- adding similar-cost capacity away from the active constraint does not, or has a materially smaller effect;
- relieving one constraint exposes another;
- at least two structurally different feasible designs survive;
- neither surviving design strictly dominates the other across every declared game-owned cost/resource/performance dimension.

The investigation is explicitly allowed to falsify the alternative.

## Pre-register the experimental protocol before inspecting result patterns

The brief already requires a pre-declared parameter window. Make that concrete before searching for a pleasing result.

Persist a protocol/checkpoint under `workspace/research/investigations/` before examining the frontier. It should state at least:

- the chosen reference routing/family;
- production quantity;
- the equipment offers and the exact supported Factory facts they project to;
- allowed capacity/configuration variants;
- construction-cost values or bounded cost ranges;
- budget values/range;
- completion-target values/range;
- the full parameter window that will be searched;
- the systematic enumeration/generation algorithm;
- the explicit dimensions used for feasibility, dominance and frontier analysis;
- the operational rule for what counts as a materially smaller intervention effect;
- the rule for distinguishing an explainable capital/performance trade-off from an arbitrary price-induced tie;
- the supported evidence used to identify the active constraint;
- the supported evidence used to establish constraint migration;
- deterministic replay checks for load-bearing cases.

Do not inspect outputs and then silently revise the window until a desirable frontier appears.

If the protocol genuinely needs revision after seeing evidence, preserve the original protocol, explain why it was inadequate, commit the revised protocol as a new experimental pass, and keep the two passes distinguishable in the report.

## Existing experimental substrate

Prefer the landed deterministic research/test substrate rather than inventing a parallel simulator or reading hidden Engine state.

Inspect and reuse where appropriate:

- `ExperimentFixture`
- `ExperimentRunner`
- `ExperimentEvidence`
- `ExperimentStep`
- `EvidenceWindow`
- `DeclaredEvidence`
- `ResearchDefinition`
- `Oracle` / `OracleOutcome`
- `ThreeStepRoutingFamily`
- `StarterCorpus`
- `WaitingWorkByStepOracle`
- `ProcessingOccupancyOracle`
- the related contract/boundary/corpus tests.

At the handoff baseline, that substrate already demonstrates deterministic non-spatial fixtures, an obvious capacity constraint, a controlled one-variable assembly-capacity intervention, and a case where relieving assembly capacity causes inspection to become limiting. Treat these as useful experimental infrastructure/evidence, not as proof that the full strategy-space hypothesis is already satisfied.

Maintain these distinctions:

- authored model/workload facts;
- supported Factory/Engine observations and events;
- research-local derived measures;
- game-owned catalogue/economic parameters;
- inference about the strategy space.

A research-local oracle does not become an Engine fact, public API or game-owned reusable analytics semantic merely because it is useful to this investigation.

## Required experiment

For every parameterization admitted by the pre-registered protocol:

1. Enumerate or systematically generate every feasible design within the bounded catalogue/budget.
2. Project each candidate through supported canonical Factory semantics.
3. Execute the same explicit workload under the same Engine semantics and explicit inputs.
4. Record the candidate design/configuration and game-owned construction cost.
5. Record completion time using supported runtime outcomes.
6. Retain enough supported observations/events to explain the load-bearing constraint behavior without scheduler internals or hidden mutable state.
7. Compute any research-local derivation only from a declared supported evidence set, and preserve its method/evidence dependency.
8. Identify dominated designs only on the declared challenge dimensions; do not introduce a hidden weighted score.
9. Produce the complete feasible frontier for the selected reference case, not hand-picked "winners".
10. Run one-variable interventions that add capacity:
    - at the identified active constraint; and
    - away from the identified active constraint.
11. Test whether relieving the first constraint exposes a second limiting operation/resource without changing unrelated authored variables.
12. Re-run load-bearing cases to verify deterministic replay.

Where executable evidence can be encoded as research-local test/fixture changes, keep those changes within the research evidence workspace and clearly distinguish them from production implementation. Do not mutate maintained production code to make the research easier.

## Proving cases

A positive result must demonstrate all of the brief's proving cases:

### Constraint capacity

Adding compatible capacity at the active constraint materially improves completion.

### Irrelevant capacity

A similar-cost capacity addition away from the active constraint has a null or materially smaller effect according to the pre-registered rule.

### Constraint migration

After relieving the original active constraint, another operation/resource becomes limiting.

### Capital pressure

The faster/more-capacity design costs enough that it is not automatically preferable on every declared challenge dimension.

### Multiple viable structures

At least two non-identical structures meet the production requirement and target while exposing a genuine, mechanically explainable trade-off rather than an arbitrary tie.

Neither qualifying design may strictly dominate the other across every declared game-owned cost/resource/performance dimension.

If the last properties can only be produced by relying on unresolved spatial/transfer semantics, record that as evidence against the non-spatial kernel rather than assuming those semantics.

## Falsification discipline

Treat the intended non-spatial product kernel as unsupported when a reasonable bounded systematic search shows one or more of these persist across the declared window:

- one design strictly dominates the alternatives on cost and completion;
- meaningful improvements reduce to monotone capacity purchase;
- no meaningful constraint migration/trade-off appears;
- viable multiplicity appears only through arbitrary economic tuning with no production-system explanation;
- explaining the difference requires facts or semantics Arcogine does not support.

Also test whether an apparent positive case is numerically brittle. A frontier that exists only at a single accidental equality or disappears under trivial reasonable perturbations should not be presented as a robust product-relevant strategy space without qualification.

Do not enlarge the search indefinitely merely to avoid falsification. Apply the stopping rule in `docs/development/researching.md`.

## Robustness check for a positive candidate

If a positive reference family is found, test nearby values inside the pre-registered window sufficiently to establish whether the qualitative trade-off is robust rather than a single lucky point.

Check at least:

- nearby reasonable cost values;
- modest budget changes;
- modest completion-target changes;
- whether more than one structural design remains viable;
- whether the active-constraint explanation remains traceable;
- whether constraint migration remains visible;
- whether one design becomes globally dominant after a trivial perturbation;
- whether the result depends on an accidental equality/tie.

This is a robustness test for the bounded reference challenge, not permission to expand into general game balancing.

## Evidence discipline

Keep the report's evidence categories explicit:

- **Repository fact**
- **Experimental observation**
- **Research-local derivation / inference**
- **Game-owned research parameter**
- **Recommendation / proposed durable consequence**

Do not present cost choices as Arcogine production semantics.

Do not present a research oracle's result as an Engine fact unless the supported Engine contract actually establishes that fact.

External evidence is optional. Use it only if it materially discriminates between live hypotheses, reveals a failure mode relevant to this bounded experiment, or changes confidence/durable consequence. Do not pad this repository-local investigation with a generic literature review.

Classify research risk independently under `docs/development/researching.md` §7. Do not confuse the register's **High portfolio priority** with the research-method risk classification. If the resulting conclusion is high-risk under the normative definition, arrange the required independent adversarial review before treating it as decision-quality evidence for a durable architecture/specification change.

## Exit criteria

Conclude with one of the brief's two real outcomes, or explicitly identify a concrete evidence blocker if neither can be reached.

### Positive result

Provide:

- one bounded reference challenge family;
- explicit catalogue;
- explicit cost parameters or bounded parameter rules;
- production quantity;
- budget;
- completion target;
- complete feasible frontier;
- evidence for constraint capacity;
- evidence for irrelevant capacity;
- evidence for constraint migration;
- evidence for capital pressure;
- at least two qualifying non-identical viable structures;
- robustness results;
- assumptions, limitations and confidence.

State only the bounded product/challenge requirements this evidence supports.

Do not infer a score formula, spatial mechanic, UI technology or Engine semantic change.

### Falsification result

Show why current non-spatial semantics fail to produce the intended design problem within the declared reasonable bounded search.

Identify exactly which proving cases fail and whether the failure is due to:

- monotone capacity dominance;
- absence of meaningful constraint migration/trade-off;
- arbitrary economics being required;
- dependence on unresolved spatial/transfer semantics;
- unsupported diagnostic facts;
- another concrete bounded cause.

Do not propose new Engine semantics merely to rescue the hypothesis.

### Evidence blocked / more evidence required

Use this only when a specific material evidence gap genuinely prevents either a positive or falsification conclusion. Name the exact missing evidence, why it is decision-relevant, and why it cannot be obtained from the current repository/runtime.

Do not use "more evidence required" merely because the result is negative.

## Report and custody

Use `docs/research/report-template.md` for the completed report and omit inapplicable sections rather than padding them.

Persist the completed report under a semantic path in:

`workspace/research/investigations/`

Before presenting the investigation as complete:

1. re-resolve live `main`;
2. record whether it moved materially from the research baseline;
3. reconcile any material repository changes into the conclusion without rewriting handed-off evidence history;
4. commit the completed report;
5. return the required active-custody coordinates:
   - workspace branch;
   - exact report commit SHA;
   - report path;
   - research baseline SHA;
   - final live-main SHA checked;
6. state the concise conclusion, confidence, risk classification, unresolved unknowns, durable destination, and adversarial-review requirement/status.

A written report does not make the research question `CONCLUDED`. Durable reconciliation or an explicit recorded no-action result is still required under the normative research lifecycle.

Do not merge transient `workspace/` material to `main` merely because the investigation exists. The same finite workspace may continue into adversarial review and durable reconciliation, preserving exact handed-off artifact revisions.
