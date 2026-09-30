# Simulation analytics ownership boundary

> **Status:** READY
> **Risk:** **High** — touches major ownership, supported observable semantics, and deterministic Engine behavior. Independent adversarial review is required before any architecture promotion (`docs/development/researching.md` §7, §9).
> **Scope:** Which current simulation/runtime facts and derived results are Engine-owned, and which computations, if any, belong to consumer-neutral analytics
> **Authority:** Research only. This brief decides nothing. Current Engine semantics, the runtime observation/event contract, and the Determinism Contract remain exactly as accepted until a separate, independently reviewed reconciliation says otherwise.

This is the first question in a split simulation-analytics research series. Later questions are
[evidence/completeness/provenance](simulation-analytics-evidence-provenance.md),
[cross-adapter compatibility](simulation-analytics-adapter-compatibility.md), and
[cross-revision analytical comparability](simulation-analytics-cross-revision-comparability.md).
Their presence does not predetermine that a reusable analytics capability survives this question.

## Research question

> Which current supported runtime facts and derived results are Engine-owned because they are authoritative state, authoritative change, result-affecting interpretation, or required current-state projection; and which computations, if any, belong to consumer-neutral analytics?

## Decision at stake

Determine the supported ownership boundary without confusing source-code placement with semantic
ownership.

Moving implementation code while retaining a value as an Engine-owned supported result with the same
formula, arithmetic, accumulation, and conformance contract is not an ownership reclassification.
Removing or redefining a value from the Engine's supported observable boundary, or moving ownership of
its defining arithmetic to another capability, is a semantic/support-boundary change even when a
particular run still produces the same number.

The investigation must decide or explicitly classify:

- which facts/projections must remain Engine/runtime-owned;
- which longitudinal, statistical, diagnostic, or comparative computations, if any, should be
  consumer-neutral analytics;
- the ownership of each current `RuntimePerformanceObservation` field, `busyTicks`, and the
  relevant `FactoryHandler` aggregates;
- which proposed changes would require an Engine-semantics change, a runtime-contract change, both,
  or only implementation reorganization; and
- whether any consumer-neutral analytics responsibility remains after that classification.

Later reproduction of analytical results, retention/accumulation, adapter compatibility, and
cross-revision comparison are separate questions. Do not require this investigation to design those
contracts before it can answer ownership.

## Why this question exists

This question was extracted from a superseded mixed investigation into player-facing diagnostic
evidence. The supersession narrative is maintained in the
[Factory-design game product research programme](factory-design-game-vertical-slice.md#superseded-predecessor),
and its lifecycle state in the [research register](../research-register.md).

Historical Arcogine implementations already demonstrated more than one consumer of reusable derived
measurement: generic KPI computation, an outward KPI projection, a web consumer retaining KPI
history, and headless/reference consumers. Those retired implementations are evidence that reusable
measurement is not inherently game-owned; they are not a current dependency or a substrate to
restore.

The current supported runtime also already exposes derived results. That makes the ownership
question concrete today even though Arcogine currently has no outward HTTP API, CLI, or other remote
adapter.

## Non-goals

This investigation does not:

- implement an analytics module or restore the removed generic KPI package;
- change KPI formulas or `RuntimePerformanceObservation` in code;
- define event retention, analytics accumulator state, or provenance/versioning rules — those belong
  to the [evidence/provenance question](simulation-analytics-evidence-provenance.md) if promoted;
- define embedded-versus-remote compatibility — that belongs to the
  [adapter-compatibility question](simulation-analytics-adapter-compatibility.md) when a second
  concrete supported consumption boundary exists;
- decide whether results from different Engine interpretations/revisions are comparable — that
  belongs to [cross-revision analytical comparability](simulation-analytics-cross-revision-comparability.md);
- decide game presentation or population-level player comprehension; or
- change adopted architecture or Engine semantics merely by recording a research classification.

## Candidate models

Compare at least these. **Do not predetermine model 3.**

1. **Status quo / Engine-rich performance boundary.** The Engine continues publishing substantial
   derived performance results as part of the supported observation. Any reusable analytics
   responsibility is narrower and additive.
2. **Facts-only extreme.** The Engine publishes almost exclusively authoritative execution facts and
   current state. Recomputable performance measurement lives outside Engine except where a derived
   value participates in authoritative Engine behavior.
3. **Mixed / minimal authoritative boundary.** Engine exposes authoritative state and change,
   result-affecting derivations, and only the minimum justified current projections; reusable
   longitudinal, statistical, diagnostic, and comparison logic lives outside Engine.

## Ownership rule under test

Do **not** adopt the rule "if a value can be derived, it is not Engine-native." That would
misclassify result-affecting semantics.

The principle to test is:

> Engine owns authoritative execution state, authoritative runtime changes, result-affecting interpretation, and the minimum supported current-state projections needed to understand execution. Recomputable interpretation, longitudinal aggregation, statistical analysis, diagnosis, and comparison over those facts may belong outside Engine when they do not participate in authoritative Engine behavior.

Worked examples any surviving rule must classify correctly:

- `combinedQueueDepth` is derived **and** result-affecting because it is a dispatch ranking key whose
  exact arithmetic changes assignment. Its ranking meaning remains Engine semantics.
- Interval utilization is measurement over outcomes; it does not become scheduling semantics merely
  because Engine could compute it.
- A fresh current-state projection may expose a direct summary such as queue depth without thereby
  becoming historical analytics.
- A field can remain Engine-owned even if its implementation is factored into reusable code.

## Invariants that must survive

**Analytics must not become a second simulation engine.**

> Analytics may measure outcomes; it must not reconstruct or re-decide Engine choices.

Deriving occupancy duration from supported evidence is compatible with this rule. Reimplementing
candidate ranking to decide which machine should have been selected is not.

**Current state and ordered events answer different questions.** A current observation answers
what/where/how much now; ordered supported runtime events answer what changed, in what order, and
when. This distinction helps classify ownership but does not require Engine to retain unbounded
history.

**Waiting attribution is operation-step-first.** Resource attribution is truthful only when the
effective eligible set is singular or bound. Multi-eligible waiting must not be reclassified as
though one physical queue owns it.

**Truthful naming is contractual.** `busyTicks` is not instantaneous utilization.
`combinedQueueDepth` is a ranking quantity, not physical units waiting at one machine. Authored
`setupTime` is not a runtime performance fact while the runtime does not use it.

**Deterministic comparison is bounded.** Deterministic re-execution supports controlled comparison
under one fixed definition and complete inputs. It does not establish equivalence between different
definitions, and multi-variable changes are not uniquely attributable merely because execution is
deterministic.

## High-risk current boundary

Current supported runtime semantics already embed derived performance results:

- `RuntimeObservation` carries a mandatory `RuntimePerformanceObservation`;
- that record exposes `backlog`, `completedOrders`, `completedSalesValue`,
  `averageLeadTime`, and `throughputPerTick`;
- `FactoryRuntime`/`FactoryHandler` compute several of those values;
- Engine semantics §10.1–§10.2 define supported derived-result arithmetic and accumulation as
  result-affecting because changing them changes supported results for identical explicit inputs; and
- Engine conformance tests pin those derived-result rules.

Therefore, any reclassification that changes which derived results belong to Engine's supported
observable boundary or who owns their defining arithmetic is a semantic/support-boundary change, not
merely a refactor. Whether later reproduction of those results is required is a separate provenance
and support question.

## Required classifications

At minimum classify:

- `RuntimeObservation` state projections;
- every `RuntimePerformanceObservation` field individually;
- `busyTicks` and its accumulator semantics;
- `FactoryHandler.avgLeadTime()`, `throughput(...)`, and completed-value/count aggregates;
- `combinedQueueDepth` as Engine ranking semantics versus presentable measurement;
- the former `com.arcogine.core.kpi.*` implementation as historical evidence, not a component to
  restore;
- KPI/metric history and baseline comparison as a responsibility class;
- occupancy intervals;
- waiting-by-step attribution;
- processing/waiting/transfer decomposition;
- utilization;
- starved/surplus classification;
- active-period and other bottleneck inference; and
- run/attempt comparison.

The last items are proving classes for ownership, not requests to select their final formulas.

## Proving and failure cases

At minimum:

1. Multi-eligible waiting while every per-machine `queueDepth` is zero.
2. A long unfinished step while completion-credited `busyTicks` is still zero.
3. `concurrency > 1`, where raw cumulative processing ticks divided by elapsed time can exceed 1.
4. A single-order challenge where backlog, completion count, mean lead time, and throughput are
   diagnostically degenerate.
5. `combinedQueueDepth`: exact as Engine ranking semantics, unsafe as physical queue visualization.
6. A controlled one-variable rerun versus a multi-variable change, proving that "comparison" is not
   automatically an authoritative Engine fact.

Transfer-dependent classification is a recheck when executable transfer evidence lands; it is not a
prerequisite for concluding the current non-spatial ownership boundary.

## Exit criteria

Conclude only with a report that:

- classifies current facts and derived outputs with rationale;
- determines whether any consumer-neutral analytics responsibility remains;
- states, for every current supported performance field proposed for reclassification, whether the
  consequence is an Engine-semantics change, runtime-contract change, both, or implementation-only
  reorganization;
- states the minimum boundary rule that prevents analytics from becoming a second simulation engine;
- identifies which downstream candidate questions become promotable because of the result;
- states explicitly whether the runtime observation/event contract, the Determinism Contract, or
  Engine semantics require revision; and
- receives genuinely independent adversarial review before any architecture promotion.

## Downstream questions

If this investigation establishes a reusable consumer-neutral analytics responsibility, promote the
[evidence/completeness/provenance](simulation-analytics-evidence-provenance.md) question only when
its concrete analytics use/input is identified.

The [cross-adapter compatibility](simulation-analytics-adapter-compatibility.md) question remains
candidate until both an analytics contract and a second concrete supported consumption boundary
exist.

The [cross-revision comparability](simulation-analytics-cross-revision-comparability.md) question is
a sibling concern. It may be promoted for a concrete cross-revision comparison use whether the
compared result is Engine-owned or consumer-neutral; if consumer-neutral analytics is involved, it
must consume the applicable evidence/provenance contract.

## Inherited evidence

Reusable evidence transferred from the superseded diagnostic-evidence investigation must be
re-verified rather than assumed:

- landed non-transfer diagnostic questions were derivable from supported observation plus supported
  events plus published model facts, with no proven new Engine fact gap;
- the former generic `EventLog`-derived KPI implementation was not a supported analytics substrate
  and has been removed;
- retired legacy HTTP/SSE internal-event surfaces were never the semantic compatibility boundary; and
- external bottleneck-detection literature remains relevant to proving that utilization alone does
  not establish bottleneck identity and that shifting constraints require temporal evidence.
