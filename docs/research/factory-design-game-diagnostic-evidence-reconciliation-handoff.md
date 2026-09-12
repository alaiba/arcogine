# Follow-up handoff: reconcile the factory-design diagnostic-evidence investigation

You previously produced the research report **Player-Facing Diagnostic Evidence for the First Factory-Design Game Slice**. This follow-up asks you to perform the **separate durable reconciliation work** that the report itself did not perform.

This prompt is intentionally self-contained. Do not assume access to the conversation that led to it. Re-ground every repository fact before editing, but use the decisions and problem decomposition below as the required reconciliation intent unless current repository authority materially contradicts them.

## Mission

Perform the complete repository reconciliation of the original diagnostic-evidence investigation so that Arcogine no longer carries a mixed game/analytics question whose ownership conclusion we now know is too narrow.

The required outcome is **not** implementation of analytics, game UI, Engine changes, KPI migration, or a Java SDK. The outcome is a reviewed documentation/research/planning reconciliation that:

1. classifies the original player-facing diagnostic-evidence research question as **SUPERSEDED**, not CONCLUDED;
2. preserves the report's reusable technical evidence, proving cases, qualifications, and truthfulness constraints in the right durable/follow-up surfaces;
3. explicitly supersedes the report's provisional conclusion that reusable diagnostic derivations are game-owned;
4. creates a new, bounded, high-risk research question/brief for Arcogine's **consumer-neutral simulation analytics / KPI boundary**;
5. narrows the remaining game research back to the actual product question: which presentation helps players understand causal performance differences;
6. reconciles affected planning so the game does not implement generic analytics locally, the legacy EventLog KPI path is not treated as the target contract, and current Engine work does not unnecessarily cement ownership-sensitive derived-result semantics before the new research resolves them;
7. preserves already-settled transport neutrality: HTTP/SSE and embedded Java are sibling adapters over the same supported semantic contract, not competing architectures;
8. performs the knowledge-transfer audit required by `docs/development/researching.md`, names this evidence workspace and exact report revision, and determines whether the workspace is retirement-eligible **after the reconciliation PR lands**;
9. opens/drives the reconciliation PR through normal validation and independent review according to repository workflow, stopping at the repository-defined merge boundary rather than self-merging.

The reconciliation should be substantial enough to make the repository self-consistent. Expect edits across research and planning plus at least one new research brief. Do not create churn for its own sake; every changed file must carry a real surviving consequence.

---

## Critical evidence coordinate — preserve it exactly

The original completed report is immutable evidence:

- evidence workspace: `claude/arcogine-factory-diagnostics-px2bvb`
- exact report commit: `dea3fe3b58ba319d9c11c2a527f2d601ccbff566`
- exact report path: `docs/research/factory-design-game-diagnostic-evidence.md`
- original report baseline: `56a876208a98dd570d384ed9d8e3535dd86f1335`
- report's later verified baseline: `24b29418672eaeb74658e04994684bb9fe2e5db4`

**Do not amend, rewrite, rebase away, squash away, or force-push away that report commit.** `docs/development/researching.md` makes exact commit SHA + path the evidence identity.

At the time this handoff was written, the workspace branch tip was exactly the report commit above and live `main` was `24b29418672eaeb74658e04994684bb9fe2e5db4`. The workspace is therefore behind `main`; that is expected and must not be "fixed" by rewriting the evidence revision.

### Reconciliation branch rule

This task is **not a Researcher-mode continuation on the evidence workspace**. The Researcher contract explicitly separates research from durable reconciliation.

After grounding:

1. leave the evidence workspace and the report commit intact;
2. create a **fresh reconciliation branch from current live `main`** with a semantic branch name;
3. perform all durable repository edits on that reconciliation branch;
4. use the exact report coordinate above as evidence input;
5. open a normal reconciliation PR to `main` and follow normal independent PR review/lifecycle rules;
6. do not delete the evidence workspace before the reconciliation is merged and the knowledge-transfer audit says it is retirement-eligible.

This handoff file itself is temporary workspace material. It does not need to be merged to `main`.

---

## Start-of-run grounding — mandatory

Before changing anything:

1. Read `AGENTS.md` from live `main`.
2. Read `.github/agents/researcher.agent.md` to understand why the original report is evidence rather than authority, especially the prohibition on folding reconciliation into Researcher mode.
3. Read `docs/development/researching.md` in full, especially:
   - repository grounding;
   - risk classification;
   - research/reconciliation separation;
   - evidence workspace identity;
   - knowledge-transfer audit and retirement rules.
4. Read `docs/research/README.md` and the current register row for:
   - `What player-facing evidence best exposes bottlenecks and causal performance differences?`
   - the broader factory-design game product question.
5. Read the exact original report revision at commit `dea3fe3b58ba319d9c11c2a527f2d601ccbff566`, not a conversational summary.
6. Re-resolve live `main` and record its exact SHA.
7. Inspect open PRs and recent merged PRs. At handoff-writing time there were no open PRs, but do not assume that remains true.
8. Search the docs/code for at least these terms before editing:
   - `KPI`
   - `RuntimePerformanceObservation`
   - `RuntimeObservation`
   - `RuntimeEvent`
   - `EventLog`
   - `busyTicks`
   - `combinedQueueDepth`
   - `analytics`
   - `embedded Java`
   - `PLAN-ENG-4-D`
   - `PLAN-ENG-5-0`
   - `player-facing evidence`
9. Read the current versions of the directly affected authorities listed below.

### Files that must be read before editing

Research / method:

- `docs/research/README.md`
- `docs/research/factory-design-game-vertical-slice.md`
- exact report revision: `dea3fe3...:docs/research/factory-design-game-diagnostic-evidence.md`
- `docs/development/researching.md`
- `.github/agents/researcher.agent.md`

Architecture / accepted decisions:

- `docs/architecture/overview.md`
- `docs/architecture/decisions/0007-consumer-neutral-session-control-primitives.md`
- `docs/architecture/decisions/0011-runtime-observation-and-event-contract.md`
- `docs/architecture/decisions/0012-external-interchange-and-serialization-boundaries.md`
- `docs/architecture/decisions/0015-engine-semantics-identity-and-reproducibility.md`
- `docs/architecture/engine-semantics-v1.md`
- `docs/architecture/standards-alignment.md`

Planning:

- `docs/planning/factory-simulation-engine-readiness.md`
- `docs/planning/runtime-observation-event-delivery.md`
- `docs/planning/spatial-runtime-consequences.md`
- `docs/planning/factory-design-game-consumer.md`
- `docs/planning/factory-design-game-vertical-slice.md`

Implementation / current outward surfaces:

- `product/simulation/src/main/java/com/arcogine/core/kpi/`
- `product/domains/factory/src/main/java/com/arcogine/factory/process/RuntimeObservation.java`
- `product/domains/factory/src/main/java/com/arcogine/factory/process/RuntimePerformanceObservation.java`
- `product/domains/factory/src/main/java/com/arcogine/factory/process/FactoryRuntime.java`
- `product/domains/factory/src/main/java/com/arcogine/factory/process/FactoryHandler.java`
- `product/interfaces/api/src/main/java/com/arcogine/api/state/SnapshotBuilder.java`
- `product/interfaces/api/src/main/java/com/arcogine/api/controller/SimController.java`
- current web KPI consumer/store surfaces
- `docs/reference/api.md`

Read tests that prove the relevant runtime/consumer invariants, especially headless observation/event acceptance and any KPI tests. Do not infer current behavior from docs alone.

---

## Why the original question is being superseded

The original report asked one mixed question:

> What player-facing evidence best exposes bottlenecks and causal performance differences in the first factory-design game slice, such that a player can correctly explain why one design performs better or worse without reading Engine internals or inventing competing production semantics?

The report produced valuable evidence, but it mixed two different decisions:

1. **technical contract/ownership** — what facts and temporal evidence are required, which derivations are safe, where those derivations should live;
2. **product cognition/presentation** — what a player must actually see to form the correct causal model.

The report made substantial progress on the first and explicitly did **not** validate the second through participant testing.

Subsequent architecture review exposed that the first is not game-local. Arcogine already has:

- a generic KPI subsystem under `com.arcogine.core.kpi`;
- an HTTP KPI endpoint and API snapshot KPI projection;
- a web consumer that retains KPI history;
- headless/reference consumers;
- architecture language that computed values may belong in a KPI/projection layer rather than a new domain;
- planned analytical exports from supported outward contracts;
- an Accepted transport-neutral runtime contract intended for HTTP/SSE, CLI, embedded Java, and future adapters.

That is enough generic demand to reject the report's provisional trigger that a *future second product consumer* is needed before Arcogine considers shared analytics ownership.

Therefore the mixed research question should be **SUPERSEDED** by better-bounded work rather than marked CONCLUDED.

Do not spend an independent adversarial-review cycle trying to rescue the old "game-owned derivations" recommendation. Preserve the report as evidence, transfer what survives, explicitly supersede what no longer survives, and move the high-risk ownership question into a new focused investigation where it can receive the required adversarial review.

---

## Findings and principles that must survive reconciliation

These are the conclusions to preserve or turn into explicit hypotheses/requirements in the new focused research. Re-verify them against live repository authority, but do not lose them accidentally.

### 1. Separate runtime truth, analytics, presentation, and transport

Use this decomposition as the core framing:

```text
Authoritative Engine/runtime facts and result-affecting semantics
                         |
                         | supported semantic contract
                         v
             RuntimeObservation + RuntimeEvent
                         |
                         v
          consumer-neutral simulation analytics
                         |
       +-----------------+-----------------+
       |                 |                 |
 embedded/headless    HTTP/SSE/API      game/other consumers
       |                                   |
       +---------------- presentation ------+
```

This is conceptual ownership, not an instruction to create a module with a particular name.

### 2. Use the nuanced ownership rule, not "anything derivable is outside Engine"

Do **not** encode this over-broad rule:

> If a value can be derived, it is not Engine-native.

That would misclassify legitimate current-state projections and result-affecting derived rules.

The stronger principle to test and likely promote later is:

> The Engine owns authoritative execution state, authoritative runtime changes, result-affecting interpretation, and the minimum supported current-state projections needed to understand or reconstruct that execution. Recomputable interpretation, longitudinal aggregation, statistical analysis, diagnosis, and comparison over those facts belong outside the Engine unless the derived value itself participates in authoritative Engine behavior.

Examples that make the distinction concrete:

- `combinedQueueDepth` is derived but is a dispatch ranking key, so its exact arithmetic is Engine semantics;
- "resource utilization over a retained interval" is a measurement over outcomes and should not become scheduling semantics merely because the Engine could compute it;
- a fresh current-state projection may expose a direct summary such as queue depth without becoming a historical analytics subsystem.

### 3. Safe analytics measures outcomes; it must not become a second simulation engine

Preserve this invariant from the report:

> A consumer/analytics derivation is safe when it measures outcomes and unsafe when it reconstructs or re-decides why the Engine selected a resource or ordered work.

For example:

- deriving occupancy duration from supported dispatch/completion events is safe;
- reimplementing the Engine's candidate ranking to explain "why M2 was chosen" is not an analytics responsibility and risks semantic drift.

### 4. Current state and ordered events answer different classes of questions

Preserve:

- current observation answers "what/where/how much now";
- ordered supported runtime events answer "what changed, in what order, and when";
- duration/interval questions require temporal evidence or an equivalent analytics accumulator built from that evidence.

The supported runtime itself need not become event sourced and need not own unbounded history. Analytics may retain supported events or maintain incremental analytical state without changing authoritative runtime history semantics.

### 5. Waiting attribution is operation-step-first

Preserve:

- waiting work belongs semantically to the operation step;
- resource attribution is only truthful when the effective eligible set is singular/bound;
- multi-eligible waiting must not be rendered as though one physical queue owns it.

### 6. Truthful naming is part of the contract

Preserve the report's concrete failure cases:

- `busyTicks` is not instantaneous utilization;
- `combinedQueueDepth` is a ranking quantity, not a count of physical units waiting "at" one machine;
- authored `setupTime` is not a runtime performance fact when the landed runtime does not use it.

Mislabeling these is a truthfulness defect, not merely a UI preference.

### 7. Deterministic comparison is a useful causal instrument, but only within its limits

Preserve:

- deterministic re-execution gives Arcogine unusually strong controlled single-variable comparison;
- if a player changes exactly one relevant input and explicit inputs/provenance are comparable, outcome differences can support a causal explanation;
- multi-variable changes do not magically become uniquely attributable merely because execution is deterministic.

### 8. Existing landed non-spatial diagnostics did not prove a new Engine fact gap

The original report found that the **landed** non-transfer diagnostic questions were derivable from supported observation + supported events + published model facts.

Preserve that as evidence, not as a permanent theorem. The new analytics research must still ask whether the *placement of derived results* in Engine versus analytics is correct and whether future transfer semantics expose new minimum facts.

### 9. Transfer-dependent diagnostics remain conditional on landed transfer runtime behavior

The original report correctly separated processing-vs-waiting from processing-vs-transfer because transfer state/events are not yet landed.

Do not turn absent transfer behavior into invented consumer semantics. When transfer lands, the analytics question must re-evaluate:

- processing vs transfer vs waiting decomposition;
- destination admission load versus processing occupancy;
- transfer intervals and resource attribution;
- truthfulness of utilization/constraint labels in the presence of reserved inbound capacity.

### 10. Transport and embedding are not competing semantic architectures

Accepted ADR-0011 already says HTTP/SSE, WebSocket, brokers, an embedded Java API, and later adapters are projections of the same transport-neutral runtime contract. ADR-0007 likewise anticipated an embedded Java adapter.

Therefore do **not** create a new research question framed as:

> Should all consumers use HTTP/SSE, or should Arcogine support embedded Java?

The durable answer in principle already exists: both may be supported sibling adapters over one semantic contract.

A later public-Java-client/SDK design may still need a compatibility/package boundary, but that is narrower. The present reconciliation should ensure planning does not force JVM consumers through HTTP solely for architecture uniformity.

---

## Legacy KPI finding that must be made explicit

Current `com.arcogine.core.kpi.Kpi` computes from internal `EventLog`/`SimTime`. The API snapshot path imports these KPI types and exposes them outward.

Accepted ADR-0011/0012 make internal `EventLog` implementation machinery rather than the supported runtime-history/public-analysis contract. Future supported analytical exports are supposed to derive from supported `RuntimeObservation`, supported `RuntimeEvent`, or another explicitly supported outward contract.

Therefore the reconciliation should record this as an architectural debt/follow-up constraint:

> The existing generic KPI implementation is attached to the wrong observation substrate for future supported consumers and should be replaced/migrated rather than extended.

Do **not** implement the migration in this PR. Do **not** delete the old KPI subsystem yet. Do **not** invent the replacement API before the focused research concludes.

---

## The important architecture conflict the new research must resolve

Do not reduce this to "move the old KPI package."

Current Arcogine architecture/implementation also puts derived performance results inside the supported Engine boundary:

- `RuntimeObservation` contains mandatory `RuntimePerformanceObservation`;
- `RuntimePerformanceObservation` currently includes `backlog`, `completedOrders`, `completedSalesValue`, `averageLeadTime`, and `throughputPerTick` and calls them authoritative factory performance aggregates;
- `FactoryRuntime`/`FactoryHandler` compute several of those values;
- `engine-semantics:v1` currently treats supported derived-result arithmetic/accumulation as result-affecting semantics when changing those formulas would change supported results for identical inputs;
- current `PLAN-ENG-5-0` intends to pin derived-result behavior, including `busyTicks` arithmetic and throughput/mean-lead-time edge cases/accumulators.

This creates a genuine ownership/public-contract question. Do **not** silently move those values out of Engine in reconciliation. Do **not** edit Accepted ADR-0015 or the normative semantics spec as though the new answer is already known.

Instead, create a focused high-risk research brief and adjust planning only enough to prevent premature implementation from locking the disputed ownership deeper while leaving unrelated Engine/spatial work unblocked where possible.

---

# Required repository changes

The exact wording and smallest coherent file set must follow live repository evidence, but the reconciliation should achieve all outcomes below.

## A. `docs/research/README.md`

Update the research register deliberately.

### Original diagnostic question

For the row:

> What player-facing evidence best exposes bottlenecks and causal performance differences?

Change lifecycle to **SUPERSEDED**, not CONCLUDED.

The row/destination should make clear that:

- the mixed question was split because the technical ownership problem is cross-consumer while player comprehension remains product research;
- the exact evidence report remains in the temporary workspace at `dea3fe3.../docs/research/factory-design-game-diagnostic-evidence.md` until reconciliation lands and the workspace is retired;
- reusable technical evidence transfers into the new simulation-analytics research question;
- the unresolved player-understanding half remains under the broader factory-design game vertical-slice research.

Do not merge the temporary report itself to `main` merely to keep a historical copy. The register + reconciliation history + exact commit coordinate are enough unless repository policy now says otherwise.

### Add the new focused question

Add a new High-priority research question, expected to be `READY` if the brief you create is sufficiently bounded after live re-grounding. If it is not genuinely executable from the brief, leave it `CANDIDATE` and explain what is missing rather than falsely marking it READY.

Recommended wording:

> What is Arcogine's supported boundary between authoritative simulation facts and consumer-neutral simulation analytics, including which currently exposed derived results belong to Engine semantics, which should instead be computed from supported observations/events, and what provenance/compatibility obligations analytics must preserve?

Area should reflect Engine / cross-consumer analytics rather than Game.

Expected destination should say approximately:

- architecture/ADR reconciliation if a shared boundary survives;
- then implementation planning for analytics/KPI migration and supported consumer projection;
- no implementation until the high-risk conclusion has independent adversarial review and durable reconciliation.

## B. Create a dedicated research brief

Create a semantically named brief such as:

`docs/research/simulation-analytics-consumer-boundary.md`

Use the repository's research-method expectations. This should be a real executable brief, not a paragraph placeholder.

### Required bounded question

The brief should investigate:

> What is the supported ownership boundary between authoritative simulation/runtime facts and reusable derived analytics, and how should that boundary replace the legacy EventLog KPI substrate without creating a second simulation engine or transport-specific KPI semantics?

### Decision at stake

The decision is **not** "what module name should we create?"

It is:

- which facts/projections must stay Engine/runtime-owned;
- which longitudinal/statistical/diagnostic computations should live in a consumer-neutral analytics capability;
- which current `RuntimePerformanceObservation` / `busyTicks` / FactoryHandler aggregate semantics are legitimate Engine contract versus legacy placement;
- what provenance/version information analytical results need;
- what event/observation retention expectations belong to analytics versus runtime;
- how embedded Java and remote adapters consume the same semantic/analytics contract without duplicating formulas;
- what migration path replaces `com.arcogine.core.kpi` and `/api/kpis`' current substrate after the decision is reconciled.

### Risk classification

Classify this as **High risk** unless live evidence materially changes the assessment. It touches:

- major ownership;
- public/supported consumer semantics;
- reproducibility / `EngineSemanticsVersion`;
- compatibility and multiple consumers.

The brief must require a genuinely independent adversarial research review before the result can promote into an ADR or comparable durable architecture.

### Candidate models to compare

At minimum include three real candidates:

1. **Status quo / Engine-rich performance boundary**
   - Engine continues to publish significant derived performance results;
   - reusable analytics may exist outside it;
   - legacy KPI substrate is separately migrated.

2. **Facts-only extreme**
   - Engine publishes almost exclusively authoritative execution facts/current state;
   - all recomputable performance measures move to analytics.

3. **Mixed/minimal authoritative boundary**
   - Engine exposes authoritative state/change plus only the minimum direct current-state/result projections justified by runtime semantics;
   - reusable longitudinal/statistical/diagnostic analysis sits in a transport-neutral analytics capability over supported observations/events/model facts.

Do not predetermine candidate 3 as the answer just because this handoff expects it to be strong. The research must be able to falsify it.

### Required classification set

The investigation must explicitly classify at least:

- `RuntimeObservation` state projections;
- `RuntimePerformanceObservation` fields individually;
- `busyTicks` and its accumulator semantics;
- `FactoryHandler.avgLeadTime()` / throughput / completed-value aggregates;
- `combinedQueueDepth` as a result-affecting ranking key versus presentable analytics;
- legacy `com.arcogine.core.kpi.*`;
- KPI/metric history;
- occupancy intervals;
- waiting-by-step attribution;
- processing/waiting/transfer decomposition;
- utilization measures;
- starved/surplus classification;
- active-period/bottleneck inference;
- run/attempt comparisons;
- retained supported-event history / analytics accumulator state;
- model fingerprint / `EngineSemanticsVersion` / controlled-revision provenance requirements on analytical results.

### Required proving/failure cases

Carry forward the report's strongest discriminating cases and add cross-consumer ones. At minimum:

1. **Multi-eligible waiting:** real waiting exists while all per-machine `queueDepth`s can be zero.
2. **Long unfinished step:** a resource can be occupied for the whole observed interval while completion-credited `busyTicks` remains zero.
3. **Concurrency > 1:** raw cumulative processing ticks divided by elapsed time can exceed 1; define what a utilization metric would actually mean.
4. **Single-order challenge:** order-scoped backlog/completion/mean lead-time/throughput can be diagnostically degenerate.
5. **Ranking-vs-measurement:** `combinedQueueDepth` exactness affects dispatch and is Engine semantics even though the value is unsafe as a physical queue visualization.
6. **Same run, different adapter:** an embedded Java consumer and an HTTP/SSE consumer must be capable of obtaining semantically equivalent supported facts/analytics without independent formula implementations.
7. **Retention:** duration analytics must still work when the runtime accessor is draining/non-retained; decide who retains/accumulates and what gap semantics are required.
8. **Reproducibility/provenance:** analytical results derived from runs under different `EngineSemanticsVersion`s must not be silently compared as if interpretation were identical.
9. **Controlled comparison:** one-variable deterministic rerun supports stronger causal attribution than a multi-variable change.
10. **Future transfer:** when transfer semantics land, destination admission reservation versus processing occupancy must not be conflated.

### External evidence

Reuse the original report's verified bottleneck-detection literature only where it is actually load-bearing. Do not require a generic literature survey.

External evidence should mainly help test:

- whether bottleneck/utilization/waiting metrics have known semantic distinctions Arcogine would otherwise blur;
- provenance/versioning practice for analytical interpretation if relevant;
- whether a proposed shared abstraction is actually reusable versus game-specific.

The new investigation is primarily an Arcogine ownership/public-contract question; repository evidence is load-bearing.

### Exit criteria

The brief should require a decision-quality report that:

- classifies each current derived result/fact into an ownership category with rationale;
- states the supported input contract for analytics;
- states what temporal retention/accumulation analytics needs and who owns it;
- states how analytics preserves runtime/model/semantics provenance;
- defines the boundary that prevents analytics from reimplementing scheduling decisions;
- explains compatibility expectations across embedded and remote adapters;
- determines the disposition/migration target of legacy `com.arcogine.core.kpi` and `RuntimePerformanceObservation` fields;
- identifies whether current ADR-0011/0015/`engine-semantics:v1` require supersession, a new ADR, a semantics version change, or only implementation reorganization;
- has a genuinely independent adversarial review if the conclusion is to support architecture promotion.

## C. Narrow `docs/research/factory-design-game-vertical-slice.md`

Preserve the game research, but remove the implication that it should decide shared analytics ownership.

The remaining product question should be approximately:

> Given supported Arcogine simulation facts/analytics, which presentation lets players correctly identify bottlenecks, major delay sources, and the causal effect of a design change?

Keep the need for playtest/prototype evidence. The original report explicitly did not validate player comprehension.

Make clear that:

- overlays, timelines, callouts, wording, tutorial/progressive disclosure remain game research;
- the game must not invent shared KPI/diagnostic formulas while the analytics boundary is under research;
- transfer-dependent presentation remains conditional on landed transfer semantics;
- the product loop can consume a future supported analytics contract without defining it.

Also revisit the current "sidecar versus supported in-process integration" wording. Accepted architecture already allows transport-neutral sibling adapters, including embedded Java. Do not leave the research brief implying that sidecar versus in-process decides runtime semantics. It may remain a packaging/product integration choice if target platform constraints still need evidence.

## D. Reconcile game planning

### `docs/planning/factory-design-game-consumer.md`

The current responsibility table says "Supported runtime observations/events and performance facts" are Engine/runtime-owned and says player-facing summaries are Game-owned. The current diagnostics section allows consumer summaries but warns against competing authoritative computation.

Reconcile this language so it does not pre-decide the new analytics question and does not invite the game to own reusable diagnostics.

Required intent:

- authoritative runtime facts/events remain Engine/runtime-owned;
- generic reusable analytics/KPI semantics are **not** to be implemented in the game while the focused research is unresolved;
- game presentation/explanation remains game-owned;
- the game may consume the eventual supported analytics surface through an embedded or remote adapter;
- game scoring/Challenge evaluation remains separate from generic simulation analytics.

Do not invent an analytics module or public API in this planning edit.

### `docs/planning/factory-design-game-vertical-slice.md`

Update the blocker/admission wording only as necessary so the implementation gate remains truthful after the research split.

The game remains blocked on product research. If generic diagnostics are required by the promoted playable requirement, the relevant analytics boundary must also be resolved/landed before the game implements those generic diagnostics itself.

Do not turn the new analytics investigation into a blanket blocker for unrelated headless Challenge work.

## E. Reconcile Engine/outward-convergence planning

### `docs/planning/runtime-observation-event-delivery.md`

Preserve ADR-0011's settled observation/event transport migration.

Add the narrow planning consequence that:

- outward convergence must not promote internal `EventLog` KPI computation into the new supported contract;
- `/api/kpis` / legacy KPI migration is ownership-sensitive and must consume the eventual analytics decision rather than becoming a transport-specific second formula set;
- PLAN-ENG-4-D can continue migrating supported runtime observations/events where semantics are already settled;
- do not force unrelated runtime-event/SSE convergence to wait on the analytics research unless a concrete shared DTO/field would freeze the disputed performance semantics.

### `docs/planning/factory-simulation-engine-readiness.md` and `docs/planning/spatial-runtime-consequences.md`

This is the most delicate part of the reconciliation.

Current planning says PLAN-ENG-5-0 is ready to pin all pre-existing result-affecting Engine semantics, and that list currently includes:

- dispatch/ranking/recovery/scheduler behavior;
- child-materialization constraints;
- `busyTicks` arithmetic;
- throughput / mean-lead-time derived-result edge cases and accumulators;
- completed-value/count accumulators.

The new analytics question challenges the ownership/public-contract status of some **reported derived results**, but it does not challenge dispatch selection, queue ordering, acceptance, transfer semantics, scheduler ordering, or exact ranking keys such as `combinedQueueDepth`.

Reconcile planning so we do **not** unnecessarily block the whole spatial/Engine-semantic track while also avoiding needless entrenchment of the ownership-sensitive reported metrics.

Desired result:

- keep clearly authoritative/result-affecting dispatch, acceptance, scheduler, ranking, and spatial semantics admitted/ready according to their existing prerequisites;
- identify the subset of PLAN-ENG-5-0 whose purpose is to pin disputed reported derived-result semantics;
- place only that ownership-sensitive subset behind the new analytics research/architecture reconciliation where doing so is semantically honest;
- ensure downstream prerequisites do not accidentally make all Engine-semantics identity or Factory V2/spatial work hostage to that subset if the subset is not actually a hard prerequisite;
- preserve exact `combinedQueueDepth` ranking arithmetic as Engine semantics even though the same value is not a user-facing queue metric;
- do not change Accepted ADR-0015 or `engine-semantics:v1` semantics in this docs reconciliation merely to make the plan easier.

If the current accepted architecture makes it impossible to split the planning safely without contradicting an Accepted ADR, state that explicitly in the plan/research blocker rather than improvising a semantic exception. The focused research is then responsible for deciding the architecture change.

Use the repository's normal `PLAN-<TRACK>-<LOCAL-ID>` coordinate rules if a planning item must be split. Do not invent ad-hoc labels.

## F. Architecture documents / ADRs

Be conservative.

### Already-settled principles you may rely on

- ADR-0011: runtime observations/events are transport-neutral supported semantics; transports are adapters; embedded Java is allowed.
- ADR-0012: supported analytical exports derive from supported outward contracts, not internal `EventLog` serialization.
- architecture overview: computed values over another owner's state may belong in a KPI/projection layer rather than a new domain.

### What this reconciliation must NOT do

Do not:

- edit an Accepted ADR to change semantics as though this session settled the high-risk analytics boundary;
- remove `RuntimePerformanceObservation` from the architecture contract by documentation fiat;
- rewrite `engine-semantics:v1` to exclude derived results before research/adversarial review/architecture reconciliation;
- introduce a new Accepted analytics ADR without the required focused research and independent adversarial review;
- declare a particular package/module name authoritative.

If a current-state architecture doc contains an outright contradiction created by the research split, make only a factual/coordination clarification that does not pre-decide the high-risk answer. Otherwise leave the architecture change for the later post-research reconciliation.

## G. Do not edit reference docs as if behavior shipped

`docs/reference/api.md` describes current behavior. This reconciliation changes no runtime/API implementation.

Do not rewrite the reference API into the desired future analytics contract. Planning/research may state that the current KPI endpoint is legacy/debt; reference remains current-state until code changes.

---

# Knowledge-transfer audit — required before declaring the workspace retirement-eligible

The reconciliation PR must contain a clear knowledge-transfer audit in its PR description (and in durable files where the item itself belongs durably). Name the workspace and exact report commit.

At minimum classify every item below as **Transferred**, **Superseded**, **Deferred/open**, or **Discarded**, with destination.

## Transfer these technical findings/proving cases

- observation-now versus event-history duration/order distinction;
- operation-step-first waiting attribution;
- safe analytics measures outcomes and does not reconstruct Engine decisions;
- multi-eligible backlog can make all machine queue depths misleadingly zero;
- long unfinished step makes completion-credited `busyTicks` misleading for instantaneous utilization;
- concurrency makes raw `busyTicks / elapsed` potentially exceed one;
- single-order `RuntimePerformanceObservation` degeneracy for diagnosis;
- `combinedQueueDepth` is a dispatch ranking key, not a physical queue count;
- deterministic one-variable comparison as a causal instrument, with multi-variable attribution limit;
- no proven new Engine fact gap for landed non-spatial diagnostics;
- transfer-dependent diagnostics remain pending transfer runtime behavior;
- legacy `EventLog` KPI substrate is unsuitable as the future supported analytics substrate;
- current HTTP/SSE legacy internal-event surface is not the supported runtime compatibility boundary;
- any verified external bottleneck-method evidence that remains useful to the new analytics question.

## Explicitly supersede these report conclusions

- generic diagnostics are "game-owned derivations";
- a second future product consumer must appear before cross-consumer analytics is justified;
- "game-retained event history" is the architectural retention owner rather than one possible consumer-local implementation of a more general analytics responsibility;
- the next global step is only a game playtest;
- direct `FactoryRuntime` versus HTTP/SSE is an unresolved semantic architecture choice.

For the last item, preserve the actual unresolved implementation question: what stable Java client/SDK surface/package boundary should exist, if/when Arcogine supports it publicly. Do not reopen transport neutrality.

## Keep open in game research

- which visualization/explanation actually makes a player understand the bottleneck and causal performance difference;
- scoring/tutorial/presentation decisions;
- product playtest evidence;
- transfer/layout pedagogy once transfer is actually landed.

## Keep open in the new analytics research

- exact fact-vs-analytics ownership boundary;
- current `RuntimePerformanceObservation` field disposition;
- `busyTicks` ownership/meaning;
- retained history / accumulator API boundary;
- analytics result provenance/versioning;
- compatibility across embedded and remote consumption;
- legacy KPI migration destination;
- whether accepted Engine-semantics/ADR text must be superseded or versioned.

## Workspace retirement decision

The reconciliation must explicitly state one of:

- **RETIREMENT-ELIGIBLE AFTER MERGE** — every material report result is transferred/superseded/deferred/discarded and the original register question is SUPERSEDED; or
- **KEEP WORKSPACE** — name the specific untransferred material that still requires custody.

Do not delete `claude/arcogine-factory-diagnostics-px2bvb` before the reconciliation PR lands. Branch deletion is post-merge operational cleanup, not part of repository authority.

---

# Validation and delivery

This is expected to be primarily documentation/research/planning work. Do not modify production code unless re-grounding uncovers a small factual correctness issue that is absolutely necessary for the reconciliation; if that happens, stop and separate it rather than smuggling code into this docs PR.

Run the narrowest repository validation required by `AGENTS.md` and contribution docs. At minimum for the expected docs-only change, run:

```bash
git diff --check
python3 .github/scripts/check-markdown-links.py .
python3 .github/scripts/check-delivery-labels.py
```

Run any additional repository-prescribed checks that apply to files you actually change.

### PR requirements

The reconciliation PR should:

- start from fresh live `main`;
- link/name the exact evidence report commit and path;
- explain that the original research question is SUPERSEDED because it mixed a generic analytics ownership question with a still-unvalidated game product question;
- summarize the new focused research question and why it is high risk;
- explain the planning consequence for PLAN-ENG-4-D / PLAN-ENG-5-0 without claiming implementation has changed;
- include the knowledge-transfer audit;
- explicitly state the evidence workspace retirement disposition;
- request normal independent PR review;
- follow repository PR lifecycle rules through CI/review/reconciliation;
- stop at `READY TO MERGE` if repository policy reserves merge for the owner.

If review finds that the reconciliation accidentally promotes the unresolved analytics hypothesis into accepted architecture, correct that. The new research brief should carry the uncertainty; durable architecture should wait for that research and independent adversarial review.

---

# Non-goals for this reconciliation

Do **not**:

- implement a simulation analytics module/library;
- delete or migrate `com.arcogine.core.kpi` yet;
- remove/change `RuntimePerformanceObservation` in code;
- change KPI formulas;
- implement event retention/journaling;
- implement a Java SDK/client module;
- force all consumers through HTTP/SSE;
- implement game diagnostics/UI/playtests;
- implement spatial transfer;
- alter dispatch policy;
- change `engine-semantics:v1` semantics by documentation shortcut;
- create an ADR that claims the new analytics boundary is settled before the new high-risk research and independent adversarial review;
- merge the temporary evidence workspace into `main` merely to archive the report;
- delete the evidence workspace before reviewed reconciliation lands.

---

# Expected final state after this task

When you are done and the reconciliation PR is ready for owner merge, the repository should tell a coherent story:

1. The old mixed player-facing diagnostic question is `SUPERSEDED`.
2. The broader game research remains open for player comprehension/product evidence.
3. A new focused, executable, high-risk research brief owns simulation analytics/KPI boundary questions.
4. The game implementation plan explicitly avoids owning generic analytics.
5. Outward-consumer planning preserves runtime observation/event convergence but does not bless legacy EventLog KPI computation as the future contract.
6. Engine/spatial planning isolates the disputed reported-derived-result ownership question without blocking unrelated authoritative semantics more than necessary.
7. Accepted transport-neutral architecture remains unchanged: embedded and remote adapters can coexist over one semantic contract.
8. No runtime/code behavior is falsely claimed to have changed.
9. The reconciliation PR contains a complete knowledge-transfer audit for `claude/arcogine-factory-diagnostics-px2bvb` / `dea3fe3...` and states whether the workspace may be deleted after merge.
10. The new analytics question is ready for a separate Researcher run and, because it is high risk, a genuinely independent adversarial review before any architecture promotion.

---

# Final report back to the user

At the end of your run, report concisely but completely:

- live-main SHA used as reconciliation baseline;
- reconciliation branch name;
- commit(s) created;
- PR number/URL and lifecycle state;
- files added/edited and why;
- exact lifecycle disposition of the original diagnostic question;
- exact new research question/status/brief path;
- planning gates or statuses changed, especially PLAN-ENG-4-D and PLAN-ENG-5-0 consequences;
- whether any architecture/ADR text was intentionally left unchanged pending research;
- validation commands/results;
- knowledge-transfer audit disposition;
- whether `claude/arcogine-factory-diagnostics-px2bvb` is `RETIREMENT-ELIGIBLE AFTER MERGE` or must remain, with the reason;
- the single next recommended action after this reconciliation (expected: execute the focused simulation-analytics research, followed by independent adversarial review if the brief remains high risk).

Do not claim the analytics architecture is decided merely because the reconciliation creates the research brief. The purpose of this task is to leave Arcogine with the right questions, ownership boundaries, planning gates, and evidence custody before new implementation begins.
