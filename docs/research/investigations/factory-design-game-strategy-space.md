# Factory-Design Game Strategy-Space Research

> **Status:** READY — see the maintained [research register](../research-register.md)  
> **Scope:** Whether current non-spatial production/capacity semantics plus game-owned capital constraints can produce a useful first design problem  
> **Authority:** Product research only; this brief does not define Factory/Engine semantics or scoring

## Question

Can a deliberately small, fixed **non-spatial** production challenge produce several materially different, explainable viable designs using only:

- current Arcogine production/routing/resource-capacity semantics;
- one fixed quantity-bearing production requirement;
- a bounded game-owned equipment catalogue and construction cost; and
- a game-owned budget and completion target?

## Decision at stake

Whether the first factory-design product kernel already has a meaningful capacity/capital design space before spatial layout is introduced, or whether the proposed product depends on unresolved semantics to become interesting.

A positive result promotes a bounded non-spatial challenge requirement set. A negative result is evidence against implementing a playable shell around the current mechanics merely in anticipation that spatial features will rescue it.

## Scope and non-goals

In scope:

- a simple routing such as `CUT -> ASSEMBLE -> INSPECT`;
- a fixed production quantity submitted once as one Arcogine requirement;
- game-owned catalogue entries that project to supported configured-resource records;
- game-owned construction costs, budget and completion target;
- deterministic headless evaluation of candidate designs;
- controlled capacity interventions and constraint migration.

Out of scope:

- spatial layout, transfer timing, paths, conveyors, buffers, congestion, or transport resources;
- player comprehension/presentation — [Diagnostic comprehension](factory-design-game-diagnostic-comprehension.md) owns that;
- score/rating formulas, progression, level sequencing, tutorial design, renderer/input technology;
- changing Engine dispatch/scheduling semantics to improve the challenge;
- manufacturing several production orders merely to create parallelism. Arcogine owns quantity decomposition.

## Null and alternative hypotheses

**Null:** under current non-spatial semantics, a small challenge collapses to an obvious monotone solution — buy the fastest/more capacity until the target is met — or produces distinctions that are too fragile/arbitrary to support an explainable design problem.

**Alternative:** a bounded catalogue/budget/target region exists where:

- adding capacity at the active constraint materially improves completion;
- adding similar-cost capacity away from the active constraint does not;
- relieving one constraint can expose another;
- at least two structurally different feasible designs survive; and
- neither surviving design strictly dominates the other on every game-owned resource/cost/performance dimension used by the challenge.

The research is allowed to falsify the alternative.

## Reference family

Start with one three-step routing and a deliberately small catalogue. Candidate numeric values are research parameters, not contracts.

The catalogue should be small enough that feasible configurations can be enumerated or systematically searched. Prefer 4–6 equipment offers over a broad content set.

The production requirement, budget and completion target may be tuned **within a pre-declared parameter window** to locate or falsify a useful region. Do not tune one hidden score formula until a desired answer appears.

## Evidence method

For every candidate parameterization:

1. enumerate or systematically generate every feasible design within the bounded catalogue/budget;
2. project it through supported canonical Factory semantics and execute the same explicit workload under the same Engine semantics and inputs;
3. record construction cost, completion time and the supported state/event evidence needed to explain constraint behavior;
4. identify dominated designs only using the explicit challenge dimensions, never a hidden weighted score;
5. run one-variable interventions that add capacity at and away from the observed constraint; and
6. check whether relieving the first constraint exposes another without changing unrelated authored variables.

Report the full feasible frontier for the selected reference case, not only hand-picked winning designs.

## Proving cases

A supported reference challenge must contain at least:

- **constraint capacity:** adding compatible capacity at the active constraint improves the target outcome;
- **irrelevant capacity:** a similar-cost addition away from the active constraint has a null or materially smaller effect;
- **constraint migration:** after relieving the first constraint, another operation/resource becomes limiting;
- **capital pressure:** a faster/more-capacity design costs enough that it is not automatically preferable under every challenge dimension;
- **multiple viable structures:** at least two non-identical designs meet the production requirement and target while exposing a real trade-off rather than an arbitrary tie.

If the only way to create the final two properties is to rely on unresolved layout/transfer semantics, record that result rather than treating spatial behavior as implicitly available.

## Falsification conditions

Treat the non-spatial kernel as unsupported when a reasonable bounded search shows any of these persist across the declared parameter window:

- one design strictly dominates all others on cost and completion;
- every meaningful improvement is monotone capacity purchase with no constraint migration or trade-off;
- viable multiplicity exists only through arbitrary price tuning with no production-system explanation; or
- explaining the difference requires facts/semantics Arcogine does not support.

## Exit criteria

Conclude with either:

- one bounded reference challenge family, explicit catalogue/budget/target parameters or parameter rules, its complete feasible frontier, and the proving cases above; or
- a falsification result showing why current non-spatial semantics do not produce the intended design problem.

A positive conclusion promotes only product/challenge requirements. It does not authorize a scoring formula, spatial mechanics, UI technology, or Engine semantics change.
