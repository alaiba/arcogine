# Pre-registered protocol — non-spatial factory-design strategy space

> **Artifact kind:** transient research checkpoint (pre-registration), not a report and not decision-quality evidence  
> **Question:** [`docs/research/investigations/factory-design-game-strategy-space.md`](../../../docs/research/investigations/factory-design-game-strategy-space.md)  
> **Research baseline (live `main`):** `3479053f304e43ec57d7a5fd69799ae09b84b7fd`  
> **Pass:** 1 (original). A later revision, if any, is committed as a separate pass and this file is left unchanged.

This protocol is committed **before** any strategy-space enumeration, frontier, or intervention run has
been executed or inspected. The only runtime results known when it was written are the already-landed
`StarterCorpus` fixtures on `main`. Everything below is a game-owned research parameter or a
research-local rule unless labeled as a repository fact.

## 1. Repository facts the protocol depends on

Established at the research baseline from source and normative docs:

- **F1 — step-level duration.** `OperationStepDefinition.duration` belongs to the routing step; the
  Engine schedules completion at `currentTime + step.duration()` whatever resource is selected
  (`FactoryHandler`; `docs/architecture/factory-resource-semantics.md` “Current step duration also
  belongs to the step rather than the selected resource”). No supported fact expresses per-resource
  speed.
- **F2 — inert resource fields.** `ConfiguredResource.setupTime` and `capacityLiters` are canonical,
  fingerprinted and observed, but no Engine decision path reads them. Varying them would change
  model identity without changing execution.
- **F3 — the executable capacity levers** are the number of configured resources, each resource's
  `concurrency` (`canAcceptJob` = online and active jobs `< concurrency`), and each step's authored
  eligible-resource set, which may contain the same resource for several steps.
- **F4 — decomposition and dispatch.** One accepted workload of quantity `N` becomes `N` child jobs
  released atomically; selection, FIFO queues, the multi-eligible backlog and the recovery cascade are
  fixed by `docs/architecture/engine-semantics.md` §2–§4. Spatial content is refused.
- **F5 — landed game-owned Challenge facts** are a starting budget and a deadline
  (`ChallengeDefinition`), purchase costs and quantity limits (`EquipmentOffer`), and a reference
  evaluation policy that is pass/fail on completion, deadline and budget with an additive score. The
  catalogue does not currently project to Factory facts; this experiment defines a research-local
  projection.

## 2. Reference routing and workload

One product, one operation, three steps in order `CUT -> ASSEMBLE -> INSPECT`. Durations are
challenge-authored and fixed; they are not a player choice.

| Profile | CUT | ASSEMBLE | INSPECT | Role |
|---|---:|---:|---:|---|
| `P1` | 2 | 6 | 3 | primary; reuses the landed `StarterCorpus` baseline durations rather than new tuned values |
| `P2` | 3 | 4 | 5 | secondary sensitivity; inspection-heavy ordering |

Production quantity `N ∈ {6, 12, 24}`; reference `N = 12`. The workload is submitted once as one
`submitWorkload(product, N, 10.0)` command (unit price is irrelevant to execution). No other command
is issued: no availability changes, no second order.

## 3. Game-owned equipment catalogue and projection

Five offers. Each occurrence projects to exactly one `ConfiguredResource` with the stated concurrency,
`capacityLiters = null`, `setupTime = 0` (F2), and to membership in the listed steps' eligible sets.

| Offer | Eligible steps | Concurrency | Quantity limit |
|---|---|---:|---:|
| `CUTTER` | CUT | 1 | 4 |
| `ASSEMBLER` | ASSEMBLE | 1 | 6 |
| `TWIN_ASSEMBLER` | ASSEMBLE | 2 | 3 |
| `INSPECTOR` | INSPECT | 1 | 4 |
| `FLEX_CELL` | ASSEMBLE, INSPECT | 1 | 3 |

There is deliberately **no faster offer**: F1 makes per-resource speed inexpressible, and F2 forbids
presenting an inert field as equipment quality. That absence is a finding, not an omission.

Catalogue families:

- **Family D (dedicated):** `FLEX_CELL` count fixed at 0.
- **Family F (flexible):** all five offers.

Projection order (fixes `MachineId`, which is the Engine's final selection tie-break): offers in the
table order, occurrences consecutively, `MachineId` from 1. A design is **projectable** iff CUT,
ASSEMBLE and INSPECT each have at least one eligible resource. Projection publishes through
`FactoryModelPublisher`; nothing bypasses Factory publication.

## 4. Game-owned cost rules

Construction cost is the sum of occurrence prices. Two base rules:

- **`UNIFORM`:** `CUTTER = ASSEMBLER = INSPECTOR = 100`.
- **`WORK`:** a dedicated single-slot machine costs `50 × (duration of the step it serves)`, so one
  tick of processing capacity costs the same at every step. `P1`: 100 / 300 / 150; `P2`: 150 / 200 / 250.

Derived prices, under either rule:

- `TWIN_ASSEMBLER = round(δ × 2 × ASSEMBLER)`, `δ ∈ {0.75, 0.9, 1.0}`;
- `FLEX_CELL = round(φ × max(ASSEMBLER, INSPECTOR))`, `φ ∈ {1.0, 1.25, 1.5, 2.0}` (Family F only).

Cost parameterizations: Family D `2 × 3 = 6`; Family F `2 × 3 × 4 = 24`.

Cost values do not affect execution, so every projectable design is simulated once per
`(profile, N)` and every cost rule is applied to the same runs.

## 5. Declared dimensions and dominance

Exactly two challenge dimensions, mirroring the landed budget/deadline facts (F5):

1. construction cost in credits — lower is better;
2. completion tick of the single order — lower is better.

No weighted score, exchange rate or third dimension is used for feasibility, dominance or frontier
analysis. Design `A` dominates `B` iff `cost(A) ≤ cost(B)` and `T(A) ≤ T(B)` with at least one strict.
Distinct designs equal in both dimensions form a **tie class** and are reported as such.

The **global frontier** is the non-dominated set over all projectable designs of a family. Because
feasibility is the rectangle `cost ≤ B ∧ T ≤ τ`, and any design dominating a feasible design is
itself feasible, the feasible non-dominated set equals the global frontier intersected with the
rectangle. It is a contiguous segment of the frontier sorted by cost.

## 6. Parameter window

For every `(profile, N, family, cost parameterization)`:

- `T_ref` = completion of the starter design `M0 = {1 CUTTER, 1 ASSEMBLER, 1 INSPECTOR}`;
- `T_floor` = the minimum completion over all projectable designs in the family;
- completion targets `τ_f = floor(T_floor + f × (T_ref − T_floor))`, `f ∈ {0.2, 0.4, 0.6, 0.8}`;
- `C*(τ)` = the minimum cost of any design with `T ≤ τ`;
- budgets `B = floor(C*(τ) × (1 + β))`, `β ∈ {0, 0.1, 0.25, 0.5}`.

That is 16 `(τ, B)` cells per parameterization. The window is not enlarged after results are seen.

**Pre-selected reference case:** `P1`, `N = 12`, `WORK`, `δ = 0.9`, `φ = 1.25`, `f = 0.6`,
`β = 0.25`, reported for both families. Its complete global frontier and feasible segment are
reported in full, not only selected designs.

## 7. Enumeration

Full Cartesian enumeration of occurrence counts within the quantity limits
(`CUTTER 1..4`, `ASSEMBLER 0..6`, `TWIN_ASSEMBLER 0..3`, `INSPECTOR 0..4`, `FLEX_CELL 0..3`),
discarding unprojectable designs. Each design is run through the landed research substrate
(`ExperimentFixture` / `ExperimentRunner`) in a fresh `FactoryRuntime` with the script
`submit(N) → observe → advanceToQuiescence(10000) → captureEvents`, window intent `COMPLETE_RUN`.

Completion tick = the closing observation's `OrderObservation.completedAt`, which must equal the
single `ORDER_COMPLETED` event's simulated time; a mismatch or incomplete order is recorded as an
experiment failure, never silently repaired.

## 8. Research-local derivations

All derivations read only declared supported evidence (published model, supported observations,
complete supported-event window). None is an Engine fact, public API or game-owned analytic.

- **Provision vector** (published model only): `v = (cut, asmOnly, inspOnly, flex)` slots, where
  `asmOnly = ASSEMBLER + 2 × TWIN_ASSEMBLER`. Two designs are **provision-incomparable** when neither
  vector is component-wise `≤` the other.
- **Step-pool occupancy** (`PUBLISHED_MODEL`, `OBSERVATIONS`, `SUPPORTED_EVENTS`): pools are the
  connected components of the step–resource eligibility graph. For each pool,
  `occupied = Σ (JOB_STEP_COMPLETED time − JOB_DISPATCHED time)` over dispatches of the pool's steps,
  `capacity = T × Σ concurrency of the pool's resources`. The **active constraint** is the pool with
  the maximum `occupied / capacity` (exact rational comparison); equal maxima are reported as a
  co-constraint set. With a `FLEX_CELL`, ASSEMBLE and INSPECT form one pool and are not attributed
  separately, because the supported evidence does not license per-step attribution of shared
  capacity.
- **Flex dual use** (`PUBLISHED_MODEL`, `SUPPORTED_EVENTS`): a flex resource was dispatched for both
  ASSEMBLE and INSPECT during the run.
- **Waiting-work corroboration** (landed `WaitingWorkByStepOracle`): for load-bearing designs only, a
  second run with an observation at `floor(T / 2)`.

## 9. Proving-case operationalization

Interventions are one-variable: exactly one offer count changes; nothing else authored changes.
They start from (i) `M0` and (ii) each feasible frontier design of the reference cell.

- **Constraint capacity (CC).** Add one single-slot dedicated offer serving a step of the active
  pool (for the merged ASSEMBLE–INSPECT pool, test `ASSEMBLER`, `INSPECTOR` and `FLEX_CELL`
  separately). Material iff `ΔT_at ≥ max(1, ceil(0.05 × T_base))`.
- **Irrelevant capacity (IC).** For each step outside the active pool add `k` units of its dedicated
  offer, `k = max(1, round(price_at / price_away))` (cost-matched), and also `k = 1`. Materially smaller
  iff `ΔT_away ≤ 0.25 × ΔT_at`.
- **Constraint migration (CM).** Add the CC offer one unit at a time (up to 4), re-deriving the
  active constraint after each; migration iff the identified active pool changes while completion
  has strictly decreased and nothing else was changed.
- **Capital pressure (CP).** In a cell, the feasible frontier has at least two designs (the faster
  one costs more); **binding** capital pressure additionally requires that the family's fastest design
  (`T_floor`) be infeasible under `B`.
- **Multiple viable structures.**
  - *weak (MVS-W):* at least two feasible frontier designs;
  - *strong (MVS-S):* at least two feasible frontier designs that are provision-incomparable, with
    strict inequality in both dimensions (no tie), and a mechanical explanation from supported
    evidence: the faster design provides more slots to the cheaper design's active pool, and where
    the incomparability involves `FLEX_CELL`, flex dual use is observed.
  - *price-induced tie:* distinct designs, or distinct provision vectors, equal in both dimensions.
- **Monotone frontier (null signature).** Sorted by cost, each successive feasible frontier design's
  provision vector is component-wise `≥` its predecessor's, and each step adds provision to the
  predecessor's active pool.

**Why MVS-S is required for the alternative.** A frontier whose designs are nested by provision is
exactly “buy more capacity at the bottleneck until the target is met”: CC, IC, CM and CP can all pass on
such a chain, so those cases alone cannot separate the brief's null from its alternative. Both MVS-W
and MVS-S are reported so a reader applying the looser reading can see it.

**Positive cell:** CC, IC, CM, CP and MVS-S all hold for the same cell, with deterministic replay.

## 10. Selection, robustness and stopping

If the reference cell is not positive, the full window is searched for positive cells and the complete
map is reported. A positive candidate is the positive cell with the most positive immediate
neighbours (adjacent `f`, `β`, `N`, `δ`, `φ`, and the other cost rule), ties broken by the
lexicographic order `(profile, N, rule, δ, φ, f, β)`.

**Robust** iff MVS-S holds in at least half of the immediate neighbours along each perturbation axis,
no neighbour collapses the feasible frontier to a single design, the active-constraint and migration
evidence remain derivable, and no load-bearing pair depends on an exact equality. A positive cell that
fails this is reported as brittle.

Stopping: the declared window is exhaustive; it is not enlarged to rescue or to strengthen either
hypothesis (`docs/development/researching.md` §8).

## 11. Controls and replay

- **Granularity control:** every pair of designs differing only by one `TWIN_ASSEMBLER` versus two
  `ASSEMBLER`s (equal provision) is compared on completion.
- **Projection-order control:** reference frontier designs containing `FLEX_CELL` are re-projected
  with offers in reverse order and their completion compared, to expose dependence on `MachineId`
  assignment rather than on the design.
- **Replay:** the whole reference `(P1, N = 12)` enumeration and every intervention design are run
  twice; the two evidence bundles must be equal after `withNormalizedRunIdentity()`.

## 12. A-priori expectations

Recorded from the semantics in §1 before any run, so the report can show which were confirmed or
falsified:

- **E1:** the granularity control shows no completion difference, so twin versus single assemblers is
  a price choice only.
- **E2:** Family D's frontier is provision-monotone in every cell (approximately separable
  bottleneck-driven completion), so MVS-S fails for Family D while CC, IC, CM and CP pass.
- **E3:** Family F is uncertain. Pooling ASSEMBLE and INSPECT capacity in a flex cell could make a
  flex design and a dedicated design provision-incomparable frontier neighbours for some `φ`, but the
  current greedy/FIFO dispatch may also let flex cells absorb the wrong work; the direction is not
  predicted.

## 13. Execution custody

The experiment source and raw outputs are committed under
`workspace/research/investigations/factory-design-game-strategy-space-experiment/`. The source is
copied into the factory module's test source tree only for the duration of a run and is never
committed there. No production source is modified.
