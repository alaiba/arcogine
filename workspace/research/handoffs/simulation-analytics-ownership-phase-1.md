# Handoff — Simulation analytics ownership boundary, Phase 1

Execute **Phase 1 — Current Engine/runtime ownership classification** of the coupled
[Simulation analytics ownership boundary](../../../docs/research/investigations/simulation-analytics-consumer-boundary.md)
research packet.

## Task authority and coordinates

- Repository: `alaiba/arcogine`
- Phased research-definition commit: `22188d4bf5fedfa44795b4e2890b03d11266edb0`
- Phased research-definition branch: `docs/simulation-analytics-phased-research`
- Research brief:
  `docs/research/investigations/simulation-analytics-consumer-boundary.md`
- Research register:
  `docs/research/research-register.md`

The phased definition above is the task definition for this handoff. At investigation start, still
resolve live `main` and record its exact SHA as the **research baseline**. Treat live `main` as
repository truth for current architecture, implementation, tests, and other landed state. Treat the
phased-definition commit separately if it has not yet landed on `main`; do not misstate it as landed
repository truth merely because this workspace descends from it.

Follow `AGENTS.md`, `.github/agents/researcher.agent.md`, and
`docs/development/researching.md` in full. This is a **high-risk** ownership/determinism research
question. Persist the completed Phase 1 report in this same temporary research-evidence workspace
under `workspace/research/investigations/` and hand it off by exact branch + commit SHA + path.
Do not mark the registered umbrella question `CONCLUDED`; Phase 1 is an evidence checkpoint for
Phase 2.

## Bounded Phase 1 question

> Which currently supported runtime facts and derived results are semantically Engine-owned under
> the accepted contracts, and why?

This phase is descriptive before prescriptive. Reconstruct and classify the current supported
Engine/runtime boundary. Do **not** decide whether Arcogine should ultimately use an Engine-rich,
facts-only, or mixed/minimal analytics boundary.

## Required current classifications

At minimum classify, individually where applicable:

- `RuntimeObservation` state projections;
- every `RuntimePerformanceObservation` field:
  - `backlog`
  - `completedOrders`
  - `completedSalesValue`
  - `averageLeadTime`
  - `throughputPerTick`;
- `busyTicks` and its accumulator semantics;
- `FactoryHandler.avgLeadTime()`, `throughput(...)`, and completed-value/count aggregates;
- `combinedQueueDepth`, explicitly separating its Engine ranking semantics from any presentational
  or physical-queue interpretation;
- the former `com.arcogine.core.kpi.*` implementation as historical evidence only, not a component
  to restore.

For each current item, report:

1. current semantic owner;
2. ownership reason — authoritative state, authoritative change, result-affecting interpretation,
   required current-state projection, or another currently supported derived-result responsibility;
3. authoritative textual contract/specification evidence;
4. implementation location and whether it matches semantic ownership;
5. executable/conformance evidence that pins the meaning;
6. whether the ownership is clear under current accepted contracts or remains genuinely contestable;
7. if Phase 2 were later to reclassify it, whether that would require:
   - Engine-semantics change,
   - runtime-contract change,
   - both, or
   - implementation-only reorganization.

Do not equate "derived" with "analytics", and do not infer semantic ownership from package/module
placement alone.

## Phase 1 proving/failure cases

Use at least these current-boundary cases, refining them from the live repository evidence rather
than treating the wording as a fixed checklist:

1. Multi-eligible waiting while every per-machine `queueDepth` is zero — distinguish current
   projection/ranking meaning from physical-queue inference.
2. A long unfinished step while completion-credited `busyTicks` is still zero — establish what
   the current accumulator means without relabeling it as utilization.
3. `concurrency > 1`, where raw cumulative processing ticks divided by elapsed time can exceed 1 —
   establish what current Engine-owned accumulation does and does not mean.
4. `combinedQueueDepth` — exact as current Engine ranking semantics, unsafe if presented as physical
   units waiting at one machine.

If current code/tests reveal a better discriminator for an in-scope ownership classification, include
it. Do not expand into prospective metric design merely to increase coverage.

## Explicit non-goals

Do not in Phase 1:

- choose among the Engine-rich, facts-only, or mixed/minimal candidate models;
- decide that a reusable consumer-neutral analytics capability should exist;
- define the final ownership rule for prospective analytics;
- select formulas for utilization, occupancy, bottleneck detection, starvation/surplus, or run
  comparison;
- design event retention, analytical provenance, replay/reconstruction, adapter compatibility, or
  cross-revision comparability;
- design a target module/API migration;
- modify production/runtime code;
- edit accepted architecture/specification as though the research were already adopted.

Potential reclassification candidates may be identified, but their **future owner must remain an
explicit Phase 2 question**.

## Repository grounding

After resolving live `main`, inspect at least the current versions of:

- `AGENTS.md`;
- `.github/agents/researcher.agent.md`;
- `docs/development/researching.md`;
- `docs/research/research-register.md`;
- the phased simulation-analytics ownership brief at the coordinate above;
- `docs/architecture/overview.md`;
- `docs/architecture/engine-semantics.md`;
- `docs/architecture/runtime-contract.md`;
- relevant Factory/runtime source and tests for every classification;
- relevant planning/reference documents surfaced by semantic search.

Search the repository for semantic neighbors using terms including at least:
`RuntimePerformanceObservation`, `RuntimeObservation`, `busyTicks`, `combinedQueueDepth`,
`averageLeadTime`, `throughputPerTick`, `completedSalesValue`, and historical `kpi` references.

Search results are discovery aids only. Fetch/load-bearing files at the exact research baseline or
explicitly identified historical revision before relying on them.

## Evidence posture

Prefer current repository authority and executable evidence for this phase. External evidence is
needed only if it materially discriminates a current ownership classification; do not turn Phase 1
into a general analytics literature review.

Keep repository facts, historical evidence, inference, and recommendations visibly distinct.
Historical removed KPI code may inform what Arcogine previously tried, but it cannot establish the
current supported boundary.

## Phase 1 exit criteria

The Phase 1 report is complete only when it:

- provides an evidence-backed classification matrix for every current item in scope;
- distinguishes semantic ownership from implementation placement;
- explains why each Engine-owned derived value is Engine-owned without using "Engine computes it" as
  the argument;
- identifies genuinely contestable current ownership without resolving the future owner;
- states the semantic/support change category for every plausible reclassification candidate;
- hands Phase 2 a bounded unresolved set and does not expand into prospective metric design;
- records the live-main research baseline;
- states that the umbrella question remains open for Phase 2;
- states that independent adversarial review is required before any final high-risk ownership
  recommendation can support architecture promotion; and
- is persisted in this workspace with an exact report commit + path handoff.

Use `docs/research/report-template.md` as the report structure, omitting irrelevant sections rather
than padding them.

## Expected handoff

Return:

- research baseline SHA;
- workspace branch;
- exact completed Phase 1 report commit SHA;
- report path;
- concise Phase 1 classification result;
- the bounded set of ownership questions Phase 2 must decide;
- any required surface that could not be inspected.

Do not perform Phase 2 in the same run.
