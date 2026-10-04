# Handoff — Simulation analytics ownership boundary, Phase 2

Execute **Phase 2 — Consumer-neutral analytics admission boundary** of the coupled
[Simulation analytics ownership boundary](../../../docs/research/investigations/simulation-analytics-consumer-boundary.md)
research packet.

## Task authority and immutable evidence

Repository: `alaiba/arcogine`.

Use live `main` as repository truth at investigation start and record its exact SHA as the new
research baseline. Do not treat this workspace branch as landed repository truth.

The following exact Phase 1 evidence revision is mandatory input:

- workspace branch: `workspace/simulation-analytics-ownership-phase-1`
- Phase 1 report commit: `7e72a46506bf63febcb57d7b7dcc9ce0cd484d64`
- Phase 1 report path:
  `workspace/research/investigations/simulation-analytics-ownership-phase-1-current-boundary.md`

The Phase 1 report is evidence, not architecture. Re-verify any load-bearing current-state claim
against the new live-main baseline before relying on it.

The landed phased research definition is:

- brief: `docs/research/investigations/simulation-analytics-consumer-boundary.md`
- research register: `docs/research/research-register.md`

A narrow lifecycle correction is currently proposed in PR #454 to move the umbrella item from
`READY` to `ACTIVE` because Phase 1 is complete and Phase 2 remains open. At investigation start,
resolve whether that PR has landed. If it is still open, treat it as an in-flight portfolio-state
correction only; do not describe `ACTIVE` as landed register truth, but do not block Phase 2 solely
on that bookkeeping state.

Issue #453 ("Add conformance evidence for combinedQueueDepth exactness above 32-bit range") owns a
separate executable-evidence gap discovered by Phase 1. It is not a Phase 2 prerequisite and must not
be absorbed into this investigation.

Follow `AGENTS.md`, `.github/agents/researcher.agent.md`, and
`docs/development/researching.md` in full. This remains a **high-risk** ownership/determinism
research question.

Persist the completed Phase 2 report in this same temporary research-evidence workspace under
`workspace/research/investigations/`, preserving the Phase 1 report commit in history. Hand off the
completed report by exact branch + commit SHA + path.

Do not perform durable architecture reconciliation, implementation, or a purportedly independent
adversarial review in the same run. The final Phase 2 report must later receive genuinely independent
adversarial review before any architecture promotion.

## Bounded Phase 2 question

> Given the Phase 1 classification, should any reusable consumer-neutral analytics responsibility
> exist, and what minimum ownership rule separates it from Engine semantics?

The decision is semantic ownership, not source-code placement.

Phase 2 must determine whether longitudinal, statistical, diagnostic, or comparative computation
has a reusable consumer-neutral owner at all. If it does, define the minimum boundary that prevents
analytics from becoming a second simulation engine. If it does not, say so explicitly and explain
why the status quo remains the better ownership model.

## Phase 1 facts to start from — and challenge

Phase 1 concluded, at high confidence, that the current accepted boundary has these properties:

- current supported execution facts and performance results are Engine / Factory-runtime owned,
  except echoed authored model facts whose semantic ownership remains Factory;
- `combinedQueueDepth` is a derived **decision-affecting** Engine quantity and is not an analytics
  candidate;
- `backlog`, `completedOrders`, `averageLeadTime`, and `throughputPerTick` are current
  report-only supported results that Phase 1 classified as fresh-observation-recomputable
  (`R-obs`);
- `busyTicks` and `completedSalesValue` are current report-only supported accumulators whose
  general reconstruction is history-dependent with current projection shapes (`R-hist`);
- current ownership of those report-only values follows from accepted support status, not because
  they participate in Engine decisions;
- `busyTicks` is a saturating, completion-credited cumulative of authored processing durations
  across concurrent slots; it is not instantaneous occupancy and is not a normalized utilization
  ratio;
- current contract wording around "utilization facts" remains genuinely ambiguous;
- implementation placement is not semantic ownership.

Treat these as Phase 1 evidence to test, not as a prospective ownership rule. If Phase 2 discovers a
load-bearing error in them, state it and explain whether Phase 1 must be reopened.

In particular, do **not** silently turn the Phase 1 `R-obs` / `R-hist` distinction into the future
boundary. It is a consequence axis, not an adopted ontology.

## Candidate ownership models

Compare at least these three models from the brief. **Do not predetermine model 3.**

1. **Status quo / Engine-rich performance boundary.**
   Engine continues publishing substantial derived performance results as supported observations;
   any reusable analytics responsibility is narrower and additive.

2. **Facts-only extreme.**
   Engine publishes almost exclusively authoritative execution facts/current state; recomputable
   performance measurement moves outside Engine except where a derivation participates in
   authoritative Engine behavior.

3. **Mixed / minimal authoritative boundary.**
   Engine retains authoritative state/change, decision/result-affecting interpretation, and only
   the minimum justified current projections; reusable longitudinal, statistical, diagnostic, and
   comparison logic belongs outside Engine.

You may refine or add a candidate if current evidence exposes a materially distinct ownership model,
but do not multiply variants that do not change a decision.

For each candidate, state:

- what Engine is semantically responsible for;
- what a consumer-neutral analytics capability, if any, is semantically responsible for;
- which current supported performance values remain Engine-owned, move, or cease to be supported as
  Engine results;
- what evidence a late-joining consumer needs to obtain equivalent analytical information;
- whether the model creates duplicated formulas, hidden replay requirements, or a second-engine risk;
- what current contracts would have to change.

## Ownership rule under test

Test, rather than assume, the brief's prospective principle:

> Engine owns authoritative execution state, authoritative runtime changes, result-affecting
> interpretation, and the minimum supported current-state projections needed to understand
> execution. Recomputable interpretation, longitudinal aggregation, statistical analysis, diagnosis,
> and comparison over those facts may belong outside Engine when they do not participate in
> authoritative Engine behavior.

Any surviving rule must correctly classify at least these cases:

- `combinedQueueDepth` remains Engine-owned because its exact arithmetic participates in assignment;
- a current-state summary can remain Engine-owned even if derived;
- interval occupancy/utilization can be measurement over outcomes rather than scheduling semantics;
- code factoring does not itself transfer semantic ownership;
- analytics may measure outcomes but must not reconstruct or re-decide which Engine choice should
  have occurred.

If the proposed rule requires exceptions so numerous or ad hoc that it does not actually constrain
ownership, reject or narrow it.

## Phase 1 unresolved set that Phase 2 must close

### Q1 — Utilization wording and responsibility

Determine whether current "utilization facts" means the supplied processing-time facts, whether a
separate normalized result is actually part of the intended supported minimum, or whether the
current text needs clarification.

Do not select a canonical utilization formula unless the ownership decision genuinely requires one.
Distinguish:

- clarification preserving existing supported facts;
- addition of a new supported result;
- removal/narrowing of an existing obligation;
- future consumer-neutral derived measurement.

### Q2 — Fresh-observation-recomputable performance fields

Decide the future ownership of:

- `backlog`
- `completedOrders`
- `averageLeadTime`
- `throughputPerTick`

Phase 1 says these are recomputable from one fresh supported observation, subject to the current
formula/clock semantics. Determine whether that is a reason to move them, merely a reason they could
move, or not an ownership reason at all.

### Q3 — History-dependent accumulators

Decide the future ownership of:

- `busyTicks`
- `completedSalesValue`

Do not hand-wave away evidence completeness. If moving either result requires retained ordered
events, published model facts, additional current-state projection, or another input contract, state
that dependency explicitly without designing the full evidence/provenance subsystem.

If a proposed Phase 2 conclusion materially depends on the Phase 1 fresh-observation/history
distinction, strengthen that distinction with dedicated executable evidence where practical. If the
ownership conclusion does not depend on it, do not create test work solely to embellish Phase 1.

### Q4 — `throughputPerTick` and observation-clock coherence

Determine whether any ownership change can preserve the current relationship between throughput and
the supported observation clock, or whether the definition itself must change. Do not split formula
ownership from clock/boundary semantics accidentally.

### Q5 — `FactoryRuntime` aggregate accessors

Determine the consequence of any proposed reclassification for the public runtime operations outside
`observe()`, including caller-windowed `throughput(long)`.

Do not infer "unsupported" from documentation silence. If method-level support must be clarified,
state that as a reconciliation consequence.

### Q6 — Boundary-rule proving cases

The final rule must keep `combinedQueueDepth` in Engine and must permit consumer-side measurement
over supported evidence without allowing analytics to reconstruct authoritative Engine decisions.

Use Phase 1's multi-eligible waiting and occupancy evidence as inputs, but do not merely restate the
Phase 1 classification.

## Prospective analytics responsibility classes

Use these as **proving classes**, not a request to finalize every metric or algorithm:

- KPI/metric history and baseline comparison;
- occupancy intervals;
- waiting-by-operation-step attribution;
- processing/waiting/transfer decomposition;
- utilization;
- starved/surplus classification;
- active-period and other bottleneck inference;
- run/attempt comparison.

For each class, determine whether it is:

- necessarily Engine-owned;
- plausibly consumer-neutral analytics;
- necessarily consumer-local;
- currently impossible to classify because a prerequisite contract/capability does not exist; or
- unnecessary as a shared responsibility.

Avoid creating a metric catalogue. The point is to falsify or validate an ownership boundary.

## Minimum proving/failure cases

At minimum evaluate:

1. **Single-order diagnostic degeneracy.**
   A challenge where backlog, completion count, mean lead time, and throughput are not sufficient
   to distinguish the useful diagnosis. Test whether supported Engine results exhaust useful
   analysis or merely provide inputs to it.

2. **Temporal occupancy/utilization derivation.**
   A measurement derivable from supported observation/event/model evidence without reconstructing
   dispatch decisions. Test whether such measurement can truthfully live outside Engine and what
   completeness assumptions it needs.

3. **Controlled comparison versus multi-variable comparison.**
   A one-variable rerun can support a bounded before/change/after statement under one fixed
   definition; a multi-variable change does not create unique causal attribution merely because
   execution is deterministic. Test whether comparison is an Engine fact, consumer-neutral
   analytics, or consumer-local interpretation.

4. **Decision-affecting counterexample.**
   `combinedQueueDepth` must remain Engine-owned even though it is derived. Any candidate rule that
   moves it merely because it is recomputable fails.

Add proving cases only when they discriminate candidates or a load-bearing ownership rule.

## Repository grounding

At investigation start:

1. resolve live `main` and record its exact SHA;
2. read current `AGENTS.md`;
3. read current `.github/agents/researcher.agent.md`;
4. read `docs/development/researching.md` in full;
5. read current `docs/research/research-register.md`;
6. read the current simulation-analytics ownership brief;
7. resolve and read the exact Phase 1 report at
   `7e72a46506bf63febcb57d7b7dcc9ce0cd484d64`;
8. read the current owning architecture/specifications, including at least:
   - `docs/architecture/overview.md`
   - `docs/architecture/engine-semantics.md`
   - `docs/architecture/runtime-contract.md`;
9. inspect the relevant current Factory runtime implementation and executable evidence for every
   current field whose future ownership is evaluated;
10. inspect the research-experiment substrate/oracles where they supply supported-evidence
    counterexamples, while preserving their explicit status as research-local derivations;
11. inspect current Challenge comparison/evaluation consumers where they materially discriminate
    Engine fact from consumer interpretation;
12. search semantic neighbors under `docs/` and `product/`.

At minimum search for:

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
- historical `kpi`

Search results are discovery only. Fetch every load-bearing current file at the exact research
baseline before relying on it.

## External evidence

Phase 2 is the point where external evidence may become useful, but only when it materially
discriminates an ownership candidate or failure case.

Useful questions may include:

- whether utilization alone is sufficient evidence of a bottleneck;
- why temporal evidence is needed for shifting constraints;
- the distinction between authoritative simulation semantics and post-simulation measurement or
  statistical analysis.

Do not conduct a general simulation-analytics survey. Industry precedent may show possibility and
tradeoffs; it does not establish that Arcogine needs a shared analytics capability or that a
particular ownership model is necessary.

Verify provenance for every external source actually used and state analogy limits.

## Anti-second-engine invariant

Treat this as a hard failure condition:

> Analytics may measure outcomes; it must not reconstruct or re-decide Engine choices.

Consumer-neutral analytics may derive measurements from supported evidence. It must not reimplement
dispatch candidate ranking, scheduling/admission logic, transfer choices, or other authoritative
Engine interpretation in order to decide what the Engine "should" have done.

If a candidate analytics responsibility requires such reconstruction because the supported evidence
is insufficient, classify that as evidence against the candidate or as a separate missing-evidence
question — not permission to clone Engine semantics.

## Contract-change accounting

For every current supported performance field or runtime operation that the surviving model would
change, state separately whether the consequence is:

- Engine-semantics change;
- runtime-contract change;
- both;
- implementation-only reorganization;
- textual clarification with no supported semantic change.

Also state whether the surviving conclusion requires changes to:

- the runtime observation/event contract;
- the Determinism Contract / determinism architecture;
- Engine semantics;
- Architecture Overview ownership tables;
- Factory/runtime public operations.

Do not design the concrete migration/API in this research report.

## Downstream research triggers

At the end, determine whether each existing downstream candidate is now promotable, remains
candidate, or is falsified/unnecessary:

- simulation analytics evidence/completeness/provenance;
- simulation analytics cross-adapter compatibility;
- simulation analytics cross-revision comparability.

Apply their existing trigger conditions rather than promoting them automatically.

In particular, evidence/completeness/provenance should be promoted only if the result establishes a
concrete reusable analytics responsibility/use whose truthfulness depends on an explicit evidence or
retention contract.

## Explicit non-goals

Do not:

- implement an analytics module;
- restore the removed generic KPI package;
- change current runtime/Engine behavior;
- modify `RuntimePerformanceObservation` or `FactoryRuntime`;
- fix the `busyTicks`/"utilization" wording before the ownership conclusion;
- resolve issue #453 inside this investigation;
- select final formulas for utilization, bottleneck detection, starvation/surplus, or every
  prospective metric;
- design event retention/replay, analytical provenance, adapter compatibility, or cross-revision
  comparison contracts beyond identifying dependencies/triggers;
- design target module/package/API migration;
- mark the umbrella research question `CONCLUDED`;
- edit accepted architecture/specifications as if the research conclusion were already adopted;
- perform implementation planning for a still-unreconciled ownership decision.

## Exit criteria

The Phase 2 report is complete only when the combined Phase 1 + Phase 2 evidence:

- selects or rejects the candidate ownership models with explicit reasons and proving-case results;
- determines whether a useful consumer-neutral analytics responsibility exists at all;
- states a minimum ownership rule that prevents analytics from becoming a second simulation engine;
- classifies the prospective analytical responsibility classes needed to prove that rule without
  selecting unnecessary final formulas;
- explicitly closes Phase 1 Q1–Q6, or states why any one cannot yet be closed;
- states the future ownership consequence for every current
  `RuntimePerformanceObservation` field, `busyTicks`, and relevant `FactoryRuntime` /
  `FactoryHandler` aggregate surface;
- provides the semantic/support change category for every recommended current-field
  reclassification;
- states whether the runtime observation/event contract, Determinism Contract, Engine semantics,
  Architecture Overview ownership text, or public runtime operations require revision;
- identifies which downstream research candidates are now promotable and why;
- states confidence, limitations, and what evidence would change the conclusion;
- records all required surfaces that could not be inspected;
- states plainly that independent adversarial review is still required for this exact final report
  revision before architecture promotion;
- persists the report in this workspace and returns its exact commit SHA + path.

A decision-quality Phase 2 report answers this ownership question. It is not a catalogue of analytics
features.

## Expected handoff

Return:

- research baseline SHA;
- workspace branch;
- exact completed Phase 2 report commit SHA;
- report path;
- selected/surviving ownership model, or explicit no-model conclusion;
- minimum ownership rule;
- current performance-field ownership consequences;
- downstream research trigger decisions;
- required durable reconciliation surfaces;
- unresolved unknowns/limitations;
- confirmation that independent adversarial review remains outstanding for the exact report revision.

Do not perform the independent adversarial review in the same run.
