# Simulation analytics ownership boundary

> **Status:** READY
> **Risk:** **High** — touches major ownership, supported observable semantics, and deterministic Engine behavior. Independent adversarial review is required before any architecture promotion (`docs/development/researching.md` §7, §9).
> **Scope:** A coupled two-phase investigation: first classify current Engine/runtime ownership from accepted contracts; then determine whether any reusable consumer-neutral analytics responsibility exists and its minimum semantic boundary
> **Authority:** Research only. This brief decides nothing. Current Engine semantics, the runtime observation/event contract, and the Determinism Contract remain exactly as accepted until a separate, independently reviewed reconciliation says otherwise.

This is the first question in a split simulation-analytics research series. Later questions are
[evidence/completeness/provenance](simulation-analytics-evidence-provenance.md),
[cross-adapter compatibility](simulation-analytics-adapter-compatibility.md), and
[cross-revision analytical comparability](simulation-analytics-cross-revision-comparability.md).
Their presence does not predetermine that a reusable analytics capability survives this question.

## Upstream execution-account dependency

Phase 1's classification of the current Engine/runtime boundary remains independently useful.

Before the prospective Phase 2 ownership conclusion is finalized, independently reviewed, or
reconciled, Arcogine must resolve the [simulation execution account](simulation-execution-account.md)
question: whether one simulated execution already has a sufficient consumer-neutral semantic boundary
through the current observation/event contract plus caller capture, or whether identity, ordered
execution evidence, completeness, live/completed status, and related execution-window semantics need
a separate cross-consumer owner.

This dependency does **not** predetermine that an execution-account capability exists, that history
must be retained centrally, or that analytics becomes shared. It prevents Phase 2 from treating
consumer-local evidence custody as settled before the more fundamental execution substrate has been
tested.

Existing Phase 2 workspace reports remain research evidence and candidate analysis. They do not
receive final adversarial review or durable promotion until this dependency is resolved and Phase 2
has consumed the result.

This remains one registered research question, executed as an explicitly coupled two-phase packet.
Phase 1 is an independently stoppable evidence checkpoint over the current supported boundary. Phase 2
consumes that classification and makes the prospective ownership decision. A Phase 1 report does not
by itself conclude this registered question or authorize architecture change.

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

## Coupled phase structure

### Phase 1 — Current Engine/runtime ownership classification

> Which currently supported runtime facts and derived results are semantically Engine-owned under the accepted contracts, and why?

Phase 1 is deliberately descriptive before it is prescriptive. It reconstructs the current supported
boundary from architecture/specification, runtime contracts, implementation, and conformance evidence.
It must distinguish semantic ownership from source-code placement and classify the current
`RuntimeObservation` surface, `RuntimePerformanceObservation` fields, `busyTicks`, relevant
`FactoryHandler` aggregates, and result-affecting derived quantities such as
`combinedQueueDepth`.

Phase 1 may identify tensions, misleading names, or plausible reclassification candidates, but it
must not choose the future Engine-rich/facts-only/mixed model, invent the target analytics contract,
or design a code migration. Its output is an evidence-backed classification matrix plus the precise
questions that Phase 2 still has to decide.

### Phase 2 — Consumer-neutral analytics admission boundary

> Given the Phase 1 classification, should any reusable consumer-neutral analytics responsibility exist, and what minimum ownership rule separates it from Engine semantics?

Phase 2 compares the candidate ownership models, tests prospective analytical responsibility classes,
and decides whether longitudinal, statistical, diagnostic, or comparative computation has a reusable
consumer-neutral owner at all. Any recommended reclassification must state its semantic/support
consequence; detailed API/module migration remains later reconciliation/planning, not research.

Across the two phases, the investigation must decide or explicitly classify:

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

**Phase 2 only.** Phase 1 must not choose among these models. Phase 2 compares at least these and must **not predetermine model 3.**

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

**Phase 2 tests the prospective rule after Phase 1 establishes the current boundary.** Do **not** adopt the rule "if a value can be derived, it is not Engine-native." That would
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

### Phase 1 — current supported boundary

At minimum classify:

- `RuntimeObservation` state projections;
- every `RuntimePerformanceObservation` field individually;
- `busyTicks` and its accumulator semantics;
- `FactoryHandler.avgLeadTime()`, `throughput(...)`, and completed-value/count aggregates;
- `combinedQueueDepth` as Engine ranking semantics versus presentable measurement; and
- the former `com.arcogine.core.kpi.*` implementation as historical evidence, not a component to
  restore.

For each current item, state the semantic owner and ownership reason, the authoritative contract/test
evidence, whether implementation placement matches semantic ownership, and what kind of change would
be required if Phase 2 later recommended reclassification.

### Phase 2 — prospective analytics proving classes

Use these to test the ownership rule rather than to select their final formulas:

- KPI/metric history and baseline comparison;
- occupancy intervals;
- waiting-by-step attribution;
- processing/waiting/transfer decomposition;
- utilization;
- starved/surplus classification;
- active-period and other bottleneck inference; and
- run/attempt comparison.

## Proving and failure cases

### Phase 1

At minimum:

1. Multi-eligible waiting while every per-machine `queueDepth` is zero, to distinguish current
   projection meaning from physical-queue inference.
2. A long unfinished step while completion-credited `busyTicks` is still zero, to pin the current
   accumulator semantics without renaming it as utilization.
3. `concurrency > 1`, where raw cumulative processing ticks divided by elapsed time can exceed 1,
   to expose what current Engine-owned accumulation does and does not mean.
4. `combinedQueueDepth`: exact as Engine ranking semantics, unsafe as physical queue visualization.

### Phase 2

At minimum:

1. A single-order challenge where backlog, completion count, mean lead time, and throughput are
   diagnostically degenerate, testing whether supported Engine results exhaust useful analysis.
2. A temporal measurement such as occupancy/utilization that can be derived from supported evidence
   without reconstructing dispatch decisions.
3. A controlled one-variable rerun versus a multi-variable change, proving that "comparison" is not
   automatically an authoritative Engine fact.

These cases discriminate ownership classes; they do not require this investigation to settle a
canonical utilization, bottleneck, or comparison formula.

Transfer-dependent classification is a recheck when executable transfer evidence lands; it is not a
prerequisite for concluding the current non-spatial ownership boundary.

## Exit criteria

### Phase 1 checkpoint

Phase 1 is complete when its persisted report:

- classifies each current supported fact/result in scope with an explicit semantic owner and rationale;
- distinguishes authoritative state/change, result-affecting interpretation, required current-state
  projection, and other supported derived-result responsibility without assuming that "derived"
  means "analytics";
- cites the current architecture/specification, runtime contract, implementation, and executable
  evidence that carry each load-bearing classification;
- distinguishes semantic ownership from implementation placement;
- identifies any current field whose ownership is genuinely contestable, but does not choose its
  future owner;
- states, for each plausible reclassification candidate, whether moving ownership would require an
  Engine-semantics change, runtime-contract change, both, or only implementation reorganization; and
- hands Phase 2 a bounded unresolved set rather than expanding into prospective metric design.

This checkpoint is evidence for the coupled packet, not a conclusion of the registered question.
Because the umbrella question is high risk, any final ownership recommendation that depends on these
classifications remains subject to genuinely independent adversarial review before architecture
promotion.

### Final packet / Phase 2

Conclude the registered question only when the combined evidence:

- determines whether any consumer-neutral analytics responsibility remains;
- states the minimum boundary rule that prevents analytics from becoming a second simulation engine;
- classifies the prospective analytical responsibility classes needed to prove that rule, without
  selecting unnecessary final formulas;
- states the semantic/support consequence for every current supported performance field recommended
  for reclassification;
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
