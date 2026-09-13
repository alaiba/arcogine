# Simulation Analytics Consumer Boundary

> **Status:** READY
> **Risk:** **High** — touches major ownership, supported/public semantics, reproducibility, compatibility, and several existing consumers. Independent adversarial review is required before any architecture promotion (`docs/development/researching.md` §7, §9).
> **Scope:** Where the boundary sits between authoritative simulation/runtime facts and reusable derived analytics, and how that boundary replaces the legacy `EventLog` KPI substrate
> **Authority:** Research only. This brief decides nothing. Current Engine semantics, ADR-0011, ADR-0015, and `engine-semantics:v1` remain exactly as accepted until a separate, independently reviewed reconciliation says otherwise.

## Research question

> What is the supported ownership boundary between authoritative simulation/runtime facts and reusable derived analytics, and how should that boundary replace the legacy `EventLog` KPI substrate without creating a second simulation engine or transport-specific KPI semantics?

## Decision at stake

Whether Arcogine's supported runtime contract keeps publishing substantial derived performance results, or whether reusable measurement moves to a consumer-neutral analytics layer computed over supported facts — and what that implies for the legacy KPI implementation, the outward KPI surface, and the reproducibility guarantees already fixed by `engine-semantics:v1`.

The investigation must decide or explicitly classify:

- which facts/projections must remain Engine/runtime-owned;
- which longitudinal, statistical, or diagnostic computations should be consumer-neutral analytics;
- the ownership of current `RuntimePerformanceObservation`, `busyTicks`, and the `FactoryHandler` aggregates;
- analytics provenance and versioning obligations;
- event/observation retention versus runtime responsibility;
- how embedded-Java and remote adapters get equivalent semantics without duplicated formulas;
- the eventual migration target for `com.arcogine.core.kpi` and `/api/kpis`.

## Why this question exists

This question was extracted from a superseded mixed investigation into player-facing diagnostic evidence (see `docs/research/README.md` § Superseded: player-facing diagnostic evidence). That investigation concluded, provisionally, that reusable diagnostic derivations were game-owned, on the trigger that a *future* second consumer would be needed to justify shared ownership.

That trigger was already satisfied. Arcogine currently has:

- generic KPI computation in `com.arcogine.core.kpi` (`Kpi`, `KpiValue`, `ThroughputRate`, `OrderCount`, `EventCount`, `TotalSimulatedTime`);
- an outward KPI endpoint (`GET /api/kpis`) and a KPI list inside the API snapshot projection (`SnapshotBuilder`, `SimSnapshot`);
- a web consumer retaining KPI history and computing baseline-to-baseline metric deltas (`stores/baselines.ts`, `MetricDelta`), plus KPI cards, time-series charting, and export;
- headless/CLI and reference consumers of the same runtime;
- an Accepted transport-neutral runtime contract (ADR-0011) explicitly intended for HTTP/SSE, CLI, embedded Java, and future adapters.

Reusable measurement, longitudinal retention, and comparison are therefore already multi-consumer concerns, not a hypothetical future need.

## Non-goals

This investigation does not implement an analytics module, migrate or delete `com.arcogine.core.kpi`, change KPI formulas, alter `RuntimePerformanceObservation` in code, implement event retention/journaling, build a Java SDK, force consumers through HTTP/SSE, or reopen transport architecture. It does not edit Accepted ADRs or `engine-semantics:v1`.

It also does not decide game presentation. Which visualization produces correct player understanding remains product research in [Factory-design game vertical-slice research](factory-design-game-vertical-slice.md).

## Candidate models

Compare at least these. **Do not predetermine model 3.**

1. **Status quo / Engine-rich performance boundary.** The Engine continues publishing substantial derived performance results as part of the supported observation. Reusable analytics may exist outside it; the legacy KPI substrate migrates separately as ordinary debt.
2. **Facts-only extreme.** The Engine publishes almost exclusively authoritative execution facts and current state. Every recomputable performance measure lives in analytics.
3. **Mixed / minimal authoritative boundary.** The Engine exposes authoritative state and change plus only the minimum justified direct projections; reusable longitudinal, statistical, and diagnostic analysis lives in transport-neutral analytics over supported facts, events, and model facts.

## The ownership rule under test

Do **not** adopt the rule "if a value can be derived, it is not Engine-native." That is too broad and would misclassify result-affecting semantics.

The principle to test is:

> The Engine owns authoritative execution state, authoritative runtime changes, result-affecting interpretation, and the minimum supported current-state projections needed to understand or reconstruct execution. Recomputable interpretation, longitudinal aggregation, statistical analysis, diagnosis, and comparison over those facts belong outside the Engine — unless the derived value itself participates in authoritative Engine behavior.

Worked examples that any surviving rule must classify correctly:

- `combinedQueueDepth` is derived **and** result-affecting, because it is a dispatch ranking key whose exact arithmetic changes assignment (`engine-semantics-v1.md` §2 rule 3). It stays Engine semantics.
- Interval utilization is measurement over outcomes. It must not become scheduling semantics merely because the Engine could compute it.
- A fresh current-state projection may expose a direct summary such as queue depth without thereby becoming historical analytics.

## Invariants that must survive

**Analytics must not become a second simulation engine.**

> Analytics may measure outcomes; it must not reconstruct or re-decide Engine choices.

Deriving occupancy duration from supported events is safe. Reimplementing candidate ranking to explain *why* a machine was selected is not.

**Current state and ordered events answer different questions.** Current observation answers what/where/how much now; ordered supported runtime events answer what changed, in what order, and when. Duration and interval analysis requires temporal evidence or an equivalent analytical accumulator. The runtime need not become event sourced or own unbounded history; analytics may retain supported events or maintain incremental state without becoming authoritative runtime history.

**Waiting attribution is operation-step-first.** Waiting belongs semantically to the operation step. Resource attribution is truthful only when the effective eligible set is singular or bound. Multi-eligible waiting must never be rendered as though one physical queue owns it.

**Truthful naming is contractual.** `busyTicks` is not instantaneous utilization. `combinedQueueDepth` is a ranking quantity, not physical units waiting at one machine. Authored `setupTime` is not a runtime performance fact while the landed runtime does not use it.

**Deterministic comparison is useful but bounded.** Deterministic re-execution supports strong controlled single-variable comparison; comparable explicit inputs plus one changed input can support causal explanation. Multi-variable changes are not uniquely attributable merely because execution is deterministic.

**Embedded and remote consumption are sibling adapters.** ADR-0007 and ADR-0011 already establish transport-neutral semantics with multiple possible adapters. Do not reopen "HTTP/SSE versus embedded Java" as an either/or. The narrower open question — what stable public Java client/package boundary should exist if one is supported — may be noted but is not this investigation's to settle.

## The high-risk conflict

Do not reduce this to relocating the legacy KPI package. Current *supported* runtime semantics already embed derived performance results:

- `RuntimeObservation` carries a mandatory `RuntimePerformanceObservation`;
- that record currently exposes `backlog`, `completedOrders`, `completedSalesValue`, `averageLeadTime`, and `throughputPerTick`;
- `FactoryRuntime`/`FactoryHandler` compute several of those values;
- `engine-semantics:v1` §10.1–§10.2 treat supported derived-result arithmetic **and accumulation** as result-affecting, because a formula or accumulator change alters supported results for identical explicit inputs;
- planned v1 conformance work intends to pin exactly that derived-result behavior (`busyTicks` saturation, throughput and mean-lead-time edge cases, accumulator exactness).

So any reclassification of those fields is a change to a reproducibility contract, not a refactor. This investigation must resolve that conflict; it must not pre-empt it.

## Required classifications

At minimum, classify each of:

- `RuntimeObservation` state projections;
- each `RuntimePerformanceObservation` field individually;
- `busyTicks` and its accumulator semantics;
- `FactoryHandler.avgLeadTime()`, `throughput(...)`, and completed-value/count aggregates;
- `combinedQueueDepth` as Engine ranking semantics versus presentable measurement;
- legacy `com.arcogine.core.kpi.*`;
- KPI/metric history and baseline comparison;
- occupancy intervals;
- waiting-by-step attribution;
- processing/waiting/transfer decomposition;
- utilization;
- starved/surplus classification;
- active-period and other bottleneck inference;
- run/attempt comparison;
- retained supported-event history and analytics accumulator state;
- `ModelFingerprint`, `EngineSemanticsVersion`, run/event-range, and analytical-definition provenance.

## Proving and failure cases

At minimum:

1. Multi-eligible waiting while every per-machine `queueDepth` is zero.
2. A long unfinished step while completion-credited `busyTicks` is still zero.
3. `concurrency > 1`, where raw cumulative processing ticks ÷ elapsed can exceed 1.
4. A single-order challenge where backlog, completion count, mean lead time, and throughput are all diagnostically degenerate.
5. `combinedQueueDepth`: exact as Engine ranking semantics, unsafe as physical queue visualization.
6. The same run consumed through embedded and HTTP/SSE adapters yields semantically equivalent facts and analytics **without duplicated formulas**.
7. Draining, non-retained supported-event access still permits defined duration analytics through an explicit retention/accumulator owner.
8. Analytical results produced under different `EngineSemanticsVersion`s are not silently treated as directly comparable.
9. One-variable deterministic rerun versus a multi-variable change.
10. Once transfer lands, destination reservation/admission load is distinguished from processing occupancy.

## Exit criteria

Conclude only with a report that:

- classifies current facts and derived outputs with rationale;
- defines the supported analytics input set;
- defines retention and accumulation ownership;
- defines provenance obligations;
- prevents analytics from reimplementing scheduling decisions;
- defines compatibility expectations across embedded and remote adapters;
- determines the disposition and migration path for the legacy KPI substrate and the current performance fields;
- states explicitly whether ADR-0011, ADR-0015, or `engine-semantics:v1` require supersession, a new ADR, a semantics-version change, or only implementation reorganization;
- receives genuinely independent adversarial review before any architecture promotion.

## Inherited evidence

Reusable evidence transferred from the superseded diagnostic-evidence investigation, to be re-verified rather than assumed:

- the landed non-transfer diagnostic questions were derivable from supported observation plus supported events plus published model facts, with no proven new Engine fact gap — evidence about landed behavior, not a permanent theorem;
- transfer-dependent diagnostics remain conditional and must be re-evaluated when transfer semantics land;
- the legacy `EventLog` KPI substrate is not the future supported analytics substrate (ADR-0011 §8, ADR-0012);
- current legacy HTTP/SSE internal-event surfaces are not the semantic compatibility boundary;
- external bottleneck-detection evidence that remains load-bearing: Skoogh, Thürer, Subramaniyan, Matta & Roser (2023), *Throughput bottleneck detection in manufacturing: a systematic review of the literature on methods and operationalization modes*, Production & Manufacturing Research 11(1) 2283031, DOI 10.1080/21693277.2023.2283031 — utilization is not sufficient for bottleneck identification and, being period-averaged, detects only an average constraint; queue-state methods suffer transient fluctuation; shifting bottlenecks are a first-class contingency; no single method is universally recommended. Roser, Nakano & Tanaka's active period method (WSC 2001/2002, ISS 2002, ESM 2004) defines the bottleneck as the process with the longest uninterrupted non-waiting period, which is computable from supported dispatch/completion events without re-deciding any assignment.
