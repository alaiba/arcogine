# Follow-up handoff: reconcile the factory-design diagnostic-evidence investigation

You previously produced the research report **Player-Facing Diagnostic Evidence for the First Factory-Design Game Slice**. This follow-up asks you to perform the durable reconciliation that the report itself did not perform.

This prompt is self-contained. Do not assume access to the conversation that produced it. Re-ground repository facts before editing, but treat the decisions and problem decomposition below as the required reconciliation intent unless live repository authority materially contradicts them.

## Mission

Complete the repository reconciliation of the original diagnostic-evidence investigation so Arcogine no longer carries a mixed game/analytics question whose ownership conclusion is too narrow.

The required outcome is documentation/research/planning reconciliation, **not** analytics implementation, game UI, Engine changes, KPI migration, or a Java SDK.

You must:

1. mark the original player-facing diagnostic-evidence research question **SUPERSEDED**, not CONCLUDED;
2. preserve the report's reusable technical evidence, proving cases, qualifications, and truthfulness constraints in appropriate durable/follow-up surfaces;
3. explicitly supersede the provisional conclusion that reusable diagnostic derivations are game-owned;
4. create a bounded, high-risk research question/brief for Arcogine's **consumer-neutral simulation analytics / KPI boundary**;
5. narrow the remaining game research to the actual product question: which presentation helps players understand causal performance differences;
6. reconcile affected planning so the game does not implement generic analytics locally, the legacy `EventLog` KPI path is not treated as the target contract, and Engine work does not unnecessarily cement ownership-sensitive reported-derived-result semantics before the new research resolves them;
7. preserve the already-settled transport-neutrality decision: HTTP/SSE and embedded Java are sibling adapters over one supported semantic contract, not competing architectures;
8. perform the complete knowledge-transfer audit required by `docs/development/researching.md`;
9. drive the final reconciliation PR through normal validation and independent review, stopping at the repository-defined merge boundary rather than self-merging.

Expect meaningful edits across research and planning plus at least one new research brief. Avoid churn: every changed file must carry a surviving consequence.

---

## Evidence coordinate and workspace lifecycle

The completed original report is identified by this immutable artifact coordinate:

- workspace: `claude/arcogine-factory-diagnostics-px2bvb`
- report commit: `dea3fe3b58ba319d9c11c2a527f2d601ccbff566`
- report path: `docs/research/factory-design-game-diagnostic-evidence.md`
- report research baseline: `56a876208a98dd570d384ed9d8e3535dd86f1335`
- later verified live-main baseline: `24b29418672eaeb74658e04994684bb9fe2e5db4`

The exact `commit SHA + path` is sufficient evidence identity. The workspace branch is expected to advance after that commit.

### One-workspace rule

**Do not create a fresh reconciliation branch. Continue using `claude/arcogine-factory-diagnostics-px2bvb` through reconciliation.**

"Separate durable reconciliation" means a separate phase/authority transition, not separate Git topology.

Required branch handling:

1. re-ground against current live `main`;
2. preserve the handed-off report commit exactly — do not amend, rebase, force-push, or otherwise rewrite it;
3. if the workspace must catch up to `main`, use a history-preserving merge-style update or equivalent operation that leaves handed-off evidence SHAs intact;
4. perform all reconciliation edits on this same workspace branch;
5. before the final reconciliation PR is merged, remove temporary report/review/handoff files from the branch's final tree unless a file is deliberately being promoted as durable repository content;
6. open the reconciliation PR from this same workspace branch to `main`;
7. after the reconciliation lands and the knowledge-transfer audit says the workspace is retirement-eligible, delete this one workspace branch as immediate cleanup.

Do not create a second branch merely to protect evidence. Artifact immutability is already provided by the exact commit coordinate.

This workspace now also contains a narrow process clarification in:

- `docs/development/researching.md`
- `docs/research/README.md`
- `.github/agents/researcher.agent.md`

Those edits make the one-workspace-through-reconciliation rule explicit. Compare them with live `main`. If live `main` still lacks equivalent wording, keep the narrow clarification in the final reconciliation; if equivalent wording has since landed, avoid duplicate edits.

This handoff file is temporary workspace material and should not remain in the final tree unless there is a deliberate reason to promote it.

---

## Mandatory grounding

Before changing the substantive reconciliation:

1. Read live `main`'s `AGENTS.md`.
2. Read live `main`'s `docs/development/researching.md`, `docs/research/README.md`, and `.github/agents/researcher.agent.md`.
3. Compare those process files with their workspace versions so you understand the one-workspace clarification carried here.
4. Read the exact report at `dea3fe3b58ba319d9c11c2a527f2d601ccbff566:docs/research/factory-design-game-diagnostic-evidence.md`.
5. Re-resolve live `main` and record its exact SHA.
6. Inspect open PRs and recent merged PRs. Do not assume the state recorded in this handoff is still current.
7. Search docs/code for at least:
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
8. Read the directly affected architecture, planning, code, and tests listed below.

### Research / method

- `docs/research/README.md`
- `docs/research/factory-design-game-vertical-slice.md`
- exact report revision above
- `docs/development/researching.md`
- `.github/agents/researcher.agent.md`

### Architecture / accepted decisions

- `docs/architecture/overview.md`
- `docs/architecture/decisions/0007-consumer-neutral-session-control-primitives.md`
- `docs/architecture/decisions/0011-runtime-observation-and-event-contract.md`
- `docs/architecture/decisions/0012-external-interchange-and-serialization-boundaries.md`
- `docs/architecture/decisions/0015-engine-semantics-identity-and-reproducibility.md`
- `docs/architecture/engine-semantics-v1.md`
- `docs/architecture/standards-alignment.md`

### Planning

- `docs/planning/factory-simulation-engine-readiness.md`
- `docs/planning/runtime-observation-event-delivery.md`
- `docs/planning/spatial-runtime-consequences.md`
- `docs/planning/factory-design-game-consumer.md`
- `docs/planning/factory-design-game-vertical-slice.md`

### Implementation / outward surfaces

- `product/simulation/src/main/java/com/arcogine/core/kpi/`
- `product/domains/factory/src/main/java/com/arcogine/factory/process/RuntimeObservation.java`
- `product/domains/factory/src/main/java/com/arcogine/factory/process/RuntimePerformanceObservation.java`
- `product/domains/factory/src/main/java/com/arcogine/factory/process/FactoryRuntime.java`
- `product/domains/factory/src/main/java/com/arcogine/factory/process/FactoryHandler.java`
- `product/interfaces/api/src/main/java/com/arcogine/api/state/SnapshotBuilder.java`
- `product/interfaces/api/src/main/java/com/arcogine/api/controller/SimController.java`
- current web KPI consumer/store surfaces
- `docs/reference/api.md`

Read tests proving relevant runtime/consumer invariants, especially headless observation/event acceptance and KPI behavior. Do not infer current behavior from docs alone.

---

## Why the original question is superseded

The original report asked one mixed question:

> What player-facing evidence best exposes bottlenecks and causal performance differences in the first factory-design game slice, such that a player can correctly explain why one design performs better or worse without reading Engine internals or inventing competing production semantics?

It combined two different decisions:

1. **technical contract/ownership** — what facts and temporal evidence are required, which derivations are safe, and where reusable derivations belong;
2. **product cognition/presentation** — what a player must see to form the correct causal model.

The report made substantial progress on the first and explicitly did not validate the second through participant testing.

The first question is not game-local. Arcogine already has:

- generic KPI code under `com.arcogine.core.kpi`;
- an HTTP KPI endpoint and API snapshot projection;
- a web consumer retaining KPI history;
- headless/reference consumers;
- architecture language allowing computed values to live in a KPI/projection layer rather than a new domain;
- planned analytical exports from supported outward contracts;
- an Accepted transport-neutral runtime contract intended for HTTP/SSE, CLI, embedded Java, and future adapters.

That is already sufficient cross-consumer demand. The report's provisional trigger that Arcogine should wait for a future second product consumer before considering shared analytics ownership no longer survives.

Therefore the mixed research question is **SUPERSEDED** by better-bounded work. Do not spend another review cycle trying to preserve the "game-owned derivations" conclusion. Transfer what survives, explicitly supersede what does not, and move the high-risk ownership question into a focused investigation.

---

## Findings that must survive reconciliation

Re-verify each against live repository authority, but do not lose them accidentally.

### Separate runtime truth, analytics, presentation, and transport

Use this conceptual decomposition:

```text
Authoritative Engine/runtime facts and result-affecting semantics
                         |
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

This is an ownership model, not an instruction to create a module with a predetermined name.

### Use a nuanced ownership rule

Do **not** encode the rule "if a value can be derived, it is not Engine-native." That is too broad.

The principle to investigate is:

> The Engine owns authoritative execution state, authoritative runtime changes, result-affecting interpretation, and the minimum supported current-state projections needed to understand or reconstruct execution. Recomputable interpretation, longitudinal aggregation, statistical analysis, diagnosis, and comparison over those facts belong outside the Engine unless the derived value itself participates in authoritative Engine behavior.

Examples:

- `combinedQueueDepth` is derived but result-affecting because it is a dispatch ranking key;
- interval utilization is measurement over outcomes and should not become scheduling semantics merely because the Engine could compute it;
- a fresh current-state projection may expose a direct summary such as queue depth without becoming historical analytics.

### Analytics must not become a second simulation engine

Preserve this invariant:

> Analytics may measure outcomes; it must not reconstruct or re-decide Engine choices.

Deriving occupancy duration from supported events is safe. Reimplementing candidate ranking to explain why a machine was selected is not.

### Current state and ordered events answer different questions

Preserve:

- current observation answers what/where/how much now;
- ordered supported runtime events answer what changed, in what order, and when;
- duration/interval analysis requires temporal evidence or equivalent analytical accumulator state.

The runtime need not become event sourced or own unbounded history. Analytics may retain supported events or maintain incremental analytical state without becoming authoritative runtime history.

### Waiting attribution is operation-step-first

Preserve:

- waiting belongs semantically to the operation step;
- resource attribution is truthful only when the effective eligible set is singular/bound;
- multi-eligible waiting must not be rendered as if one physical queue owns it.

### Truthful naming is contractual

Preserve the report's concrete failure cases:

- `busyTicks` is not instantaneous utilization;
- `combinedQueueDepth` is a ranking quantity, not physical units waiting "at" one machine;
- authored `setupTime` is not a runtime performance fact while the landed runtime does not use it.

### Deterministic comparison is useful but bounded

Preserve:

- deterministic re-execution supports strong controlled single-variable comparison;
- comparable explicit inputs/provenance plus one relevant changed input can support causal explanation;
- multi-variable changes are not uniquely attributable merely because execution is deterministic.

### No proven new Engine fact gap for landed non-transfer diagnostics

Preserve the report's finding that the landed non-transfer diagnostic questions were derivable from supported observation + supported events + published model facts. Treat that as evidence about landed behavior, not a permanent theorem.

### Transfer-dependent diagnostics remain conditional

Do not invent missing transfer semantics. When transfer behavior lands, re-evaluate processing/transfer/waiting decomposition, destination admission load versus processing occupancy, transfer intervals, and constraint/utilization naming.

### Embedded and remote consumption are sibling adapters

Do not reopen "HTTP/SSE versus embedded Java" as an either/or architecture question. ADR-0011/ADR-0007 already establish transport-neutral semantics with multiple possible adapters. A future Java SDK/public-package boundary may still need design, but JVM consumers should not be forced through HTTP merely for uniformity.

---

## Legacy KPI finding to record

Current `com.arcogine.core.kpi.Kpi` computes from internal `EventLog`/`SimTime`, and current API snapshot paths expose those KPI values outward.

ADR-0011/0012 make internal `EventLog` implementation machinery rather than the supported history/analysis contract. Future supported analytical exports should derive from `RuntimeObservation`, `RuntimeEvent`, or another explicitly supported outward contract.

Record this as architectural debt/follow-up constraint:

> The existing generic KPI implementation is attached to the wrong observation substrate for future supported consumers and should be replaced/migrated rather than extended.

Do not implement or delete it in this reconciliation.

---

## High-risk conflict the new research must resolve

Do not reduce the problem to moving the old KPI package.

Current supported runtime semantics also embed derived performance results:

- `RuntimeObservation` contains mandatory `RuntimePerformanceObservation`;
- it currently includes `backlog`, `completedOrders`, `completedSalesValue`, `averageLeadTime`, and `throughputPerTick`;
- `FactoryRuntime`/`FactoryHandler` compute several of those values;
- `engine-semantics:v1` treats supported derived-result arithmetic/accumulation as result-affecting when formula changes alter supported results for identical inputs;
- current `PLAN-ENG-5-0` intends to pin derived-result behavior including `busyTicks`, throughput/mean-lead-time edge cases, and related accumulators.

This is a real ownership/public-contract question. Do **not** silently move those values out of Engine, edit Accepted ADR-0015, or rewrite `engine-semantics:v1` as though the answer is already known.

Create focused high-risk research and adjust planning only enough to avoid needless entrenchment while keeping unrelated authoritative Engine/spatial work unblocked where possible.

---

# Required repository reconciliation

## A. Research register

In `docs/research/README.md`:

### Supersede the original diagnostic question

For:

> What player-facing evidence best exposes bottlenecks and causal performance differences?

set lifecycle to **SUPERSEDED**.

Record that:

- the mixed question split because technical ownership is cross-consumer while player comprehension remains product research;
- reusable technical evidence moved to the new simulation-analytics research question;
- unresolved player-understanding work remains under the factory-design game vertical-slice research;
- the exact report coordinate above remains historical evidence until workspace retirement.

Do not merge the temporary report itself merely to archive it.

### Add the focused analytics question

Add a High-priority question, `READY` only if the new brief is genuinely executable after re-grounding; otherwise leave it `CANDIDATE` and say what is missing.

Recommended wording:

> What is Arcogine's supported boundary between authoritative simulation facts and consumer-neutral simulation analytics, including which currently exposed derived results belong to Engine semantics, which should instead be computed from supported observations/events, and what provenance/compatibility obligations analytics must preserve?

Expected destination:

- architecture/ADR reconciliation if a shared boundary survives;
- then implementation planning for analytics/KPI migration and supported consumer projection;
- no implementation before the high-risk conclusion receives independent adversarial review and durable reconciliation.

## B. Create a focused research brief

Create a semantic brief such as:

`docs/research/simulation-analytics-consumer-boundary.md`

It must be executable, not a placeholder.

### Bounded question

> What is the supported ownership boundary between authoritative simulation/runtime facts and reusable derived analytics, and how should that boundary replace the legacy EventLog KPI substrate without creating a second simulation engine or transport-specific KPI semantics?

### Decision at stake

The investigation must decide/classify:

- facts/projections that must remain Engine/runtime-owned;
- longitudinal/statistical/diagnostic computations that should be consumer-neutral analytics;
- current `RuntimePerformanceObservation`, `busyTicks`, and FactoryHandler aggregate ownership;
- analytics provenance/versioning;
- event/observation retention versus runtime responsibility;
- equivalent semantics across embedded Java and remote adapters without duplicated formulas;
- the eventual migration target for `com.arcogine.core.kpi` and `/api/kpis`.

### Risk

Treat as **High risk** unless live evidence materially changes the classification. It touches major ownership, supported/public semantics, reproducibility, compatibility, and multiple consumers. Require genuinely independent adversarial research review before architecture promotion.

### Candidate models

At minimum compare:

1. **Status quo / Engine-rich performance boundary** — Engine continues publishing substantial derived performance results; reusable analytics may exist outside; legacy KPI substrate migrates separately.
2. **Facts-only extreme** — Engine publishes almost exclusively authoritative execution facts/current state; recomputable performance measures live in analytics.
3. **Mixed/minimal authoritative boundary** — Engine exposes authoritative state/change plus only the minimum justified direct projections; reusable longitudinal/statistical/diagnostic analysis lives in transport-neutral analytics over supported facts/events/model facts.

Do not predetermine model 3.

### Required classifications

At minimum classify:

- `RuntimeObservation` state projections;
- each `RuntimePerformanceObservation` field;
- `busyTicks` and accumulator semantics;
- `FactoryHandler.avgLeadTime()`, throughput, completed-value/count aggregates;
- `combinedQueueDepth` as Engine ranking semantics versus presentable measurement;
- legacy `com.arcogine.core.kpi.*`;
- KPI/metric history;
- occupancy intervals;
- waiting-by-step attribution;
- processing/waiting/transfer decomposition;
- utilization;
- starved/surplus classification;
- active-period/bottleneck inference;
- run/attempt comparisons;
- retained supported-event history / analytics accumulator state;
- `ModelFingerprint`, `EngineSemanticsVersion`, run/event-range and analytical-definition provenance.

### Required proving/failure cases

At minimum:

1. multi-eligible waiting while all per-machine `queueDepth`s are zero;
2. long unfinished step while completion-credited `busyTicks` is zero;
3. concurrency > 1 where raw cumulative processing ticks / elapsed can exceed 1;
4. single-order challenge where backlog/completion/lead/throughput are diagnostically degenerate;
5. `combinedQueueDepth` exactness is Engine ranking semantics but unsafe as physical queue visualization;
6. same run through embedded and HTTP/SSE consumption yields semantically equivalent facts/analytics without duplicated formulas;
7. draining/non-retained event access still permits defined duration analytics through an explicit retention/accumulator owner;
8. analytical results under different `EngineSemanticsVersion`s are not silently treated as directly comparable;
9. one-variable deterministic rerun versus multi-variable change;
10. future transfer distinguishes destination reservation/admission from processing occupancy.

### Exit criteria

Require a report that:

- classifies current facts/derived outputs with rationale;
- defines supported analytics inputs;
- defines retention/accumulation ownership;
- defines provenance obligations;
- prevents analytics from reimplementing scheduling decisions;
- defines compatibility expectations across embedded/remote adapters;
- determines disposition/migration of legacy KPI and current performance fields;
- states whether ADR-0011/0015/`engine-semantics:v1` require supersession, a new ADR, a semantics-version change, or only implementation reorganization;
- receives independent adversarial review before architecture promotion.

## C. Narrow the game research

Update `docs/research/factory-design-game-vertical-slice.md` so it no longer decides shared analytics ownership.

The remaining product question should be approximately:

> Given supported Arcogine simulation facts/analytics, which presentation lets players correctly identify bottlenecks, major delay sources, and the causal effect of a design change?

Keep prototype/playtest evidence requirements. Clarify that:

- overlays, timelines, callouts, wording, tutorial/progressive disclosure remain game research;
- the game must not invent shared KPI/diagnostic formulas while analytics ownership is unresolved;
- transfer-dependent presentation remains conditional on landed transfer semantics;
- sidecar versus in-process is a packaging/integration question, not competing runtime semantics.

## D. Reconcile game planning

### `docs/planning/factory-design-game-consumer.md`

Ensure:

- authoritative runtime facts/events remain Engine/runtime-owned;
- reusable analytics/KPI semantics are not game-owned while focused research is unresolved;
- game presentation/explanation remains game-owned;
- game scoring/Challenge evaluation remains separate from generic simulation analytics;
- the game may eventually consume analytics through embedded or remote adapters.

Do not invent the analytics public API/module here.

### `docs/planning/factory-design-game-vertical-slice.md`

Keep the game blocked on unresolved product research. If promoted playable requirements need generic diagnostics, those diagnostics must consume a resolved shared analytics boundary rather than be implemented locally by the game. Do not turn analytics research into a blanket blocker for unrelated headless Challenge work.

## E. Reconcile outward-consumer planning

### `docs/planning/runtime-observation-event-delivery.md`

Preserve ADR-0011's observation/event transport migration.

Clarify:

- outward convergence must not promote internal `EventLog` KPI computation into the supported contract;
- `/api/kpis` migration is ownership-sensitive and must consume the future analytics decision rather than become another formula set;
- settled RuntimeObservation/RuntimeEvent transport work may continue;
- do not block unrelated convergence unless a concrete DTO/field would freeze disputed performance semantics.

## F. Reconcile Engine/spatial planning carefully

Review `docs/planning/factory-simulation-engine-readiness.md` and `docs/planning/spatial-runtime-consequences.md`.

Current `PLAN-ENG-5-0` mixes clearly authoritative rules with ownership-sensitive reported-derived-result rules.

Keep authoritative/result-affecting dispatch, acceptance, scheduler, ranking, and spatial semantics admitted according to their existing prerequisites. Preserve exact `combinedQueueDepth` ranking arithmetic as Engine semantics.

Identify the subset whose purpose is to pin disputed reported-derived-result semantics (`busyTicks`, throughput/mean-lead-time reporting/accumulation, completed aggregates where ownership is genuinely in question) and place only that subset behind the analytics research/architecture reconciliation where semantically honest.

Do not make all Engine semantics identity or Factory V2/spatial work hostage to that subset unless the actual dependency requires it.

If Accepted architecture prevents a clean planning split, state the conflict instead of inventing an exception. The new research must then resolve it.

Use normal `PLAN-<TRACK>-<LOCAL-ID>` coordinates if a planning item genuinely needs splitting.

## G. Architecture restraint

Do not:

- semantically edit Accepted ADRs as if analytics ownership were settled;
- remove `RuntimePerformanceObservation` by documentation fiat;
- rewrite `engine-semantics:v1` before research/review/reconciliation;
- create an Accepted analytics ADR without the focused high-risk research and independent adversarial review;
- declare a package/module name authoritative prematurely.

Factual coordination clarifications are fine; semantic adoption is not.

## H. Reference docs remain current-state

Do not rewrite `docs/reference/api.md` into a desired future analytics contract. This reconciliation does not ship runtime/API behavior.

---

# Knowledge-transfer audit

The final PR description must name workspace `claude/arcogine-factory-diagnostics-px2bvb` and report commit `dea3fe3b58ba319d9c11c2a527f2d601ccbff566`, then classify every material item as **Transferred**, **Superseded**, **Deferred/open**, or **Discarded**, with destination.

### Transfer

- observation-now versus event-history duration/order distinction;
- operation-step-first waiting attribution;
- analytics measures outcomes rather than reconstructing Engine decisions;
- multi-eligible waiting with zero machine-local queue depths;
- long unfinished-step `busyTicks` failure mode;
- concurrency > 1 utilization failure mode;
- single-order `RuntimePerformanceObservation` diagnostic degeneracy;
- `combinedQueueDepth` ranking-versus-physical-queue distinction;
- deterministic one-variable comparison and its multi-variable limit;
- no proven new Engine fact gap for landed non-spatial diagnostics;
- transfer-dependent diagnostics remain conditional;
- legacy `EventLog` KPI substrate is not the future supported analytics substrate;
- current legacy HTTP/SSE internal-event surfaces are not the semantic compatibility boundary;
- verified external bottleneck evidence that remains load-bearing.

### Explicitly supersede

- generic diagnostics are game-owned derivations;
- a future second product consumer is required before shared analytics is justified;
- game-retained history is the architectural retention owner rather than one possible consumer-local realization;
- the next global step is only a game playtest;
- direct `FactoryRuntime` versus HTTP/SSE is an unresolved semantic architecture choice.

Preserve only the narrower open Java question: what stable public Java client/SDK/package boundary should exist if/when supported.

### Keep open in game research

- which visualization/explanation produces correct player understanding;
- scoring/tutorial/presentation decisions;
- product playtest evidence;
- transfer/layout pedagogy once transfer is landed.

### Keep open in analytics research

- exact fact-versus-analytics ownership;
- `RuntimePerformanceObservation` field disposition;
- `busyTicks` ownership/meaning;
- retained history / accumulator boundary;
- analytics result provenance/versioning;
- compatibility across embedded and remote consumption;
- legacy KPI migration target;
- whether current Engine-semantics/ADR text must be superseded/versioned.

### Retirement disposition

State one of:

- **RETIREMENT-ELIGIBLE AFTER MERGE** — every material result is transferred/superseded/deferred/discarded; or
- **KEEP WORKSPACE** — name the specific untransferred material.

Do not delete the workspace before the reconciliation lands.

---

# Validation and delivery

This should remain docs/research/planning work. Do not smuggle production-code changes into the reconciliation.

Run the narrowest required repository validation. At minimum for expected docs-only changes:

```bash
git diff --check
python3 .github/scripts/check-markdown-links.py .
python3 .github/scripts/check-delivery-labels.py
```

Run any additional checks required by the files actually changed.

## Final PR requirements

Use **this same workspace branch** for the reconciliation PR.

Before requesting review:

- bring the workspace current with live `main` without rewriting handed-off evidence commits;
- ensure temporary report/handoff artifacts not intended for main are absent from the final tree;
- ensure the net PR diff contains the durable reconciliation and the narrow research-process clarification if live `main` still needs it;
- link the exact report commit/path;
- explain why the old mixed question is SUPERSEDED;
- summarize the new high-risk analytics research question;
- explain planning consequences for outward convergence and `PLAN-ENG-5-0` without claiming implementation changed;
- include the knowledge-transfer audit and retirement disposition;
- request normal independent PR review;
- follow normal CI/review lifecycle and stop at `READY TO MERGE` if merge authority belongs to the owner.

If review finds that unresolved analytics hypotheses have been promoted into accepted architecture, correct that.

---

# Non-goals

Do **not**:

- create another reconciliation branch;
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
- create an ADR claiming analytics ownership is settled before focused research and independent adversarial review;
- keep temporary evidence/handoff files in the final main tree merely as an archive;
- delete the workspace before reviewed reconciliation lands.

---

# Expected final state

When the reconciliation PR is ready for owner merge:

1. the old mixed diagnostic question is `SUPERSEDED`;
2. game research remains open for player comprehension/product evidence;
3. a focused executable high-risk analytics/KPI-boundary research brief exists;
4. game planning explicitly avoids owning generic analytics;
5. outward-consumer planning preserves RuntimeObservation/RuntimeEvent convergence without blessing legacy EventLog KPI computation;
6. Engine/spatial planning isolates disputed reported-derived-result ownership without blocking unrelated authoritative semantics more than necessary;
7. transport-neutral embedded/remote architecture remains unchanged;
8. no runtime behavior is falsely claimed to have changed;
9. the one-workspace research process is explicit and no unnecessary reconciliation branch was created;
10. the PR contains a complete knowledge-transfer audit and workspace-retirement disposition.

---

# Final report back to the user

Report:

- live-main SHA used as reconciliation baseline;
- confirmation that `claude/arcogine-factory-diagnostics-px2bvb` was reused rather than creating another branch;
- commits created;
- PR number/URL and lifecycle state;
- files added/edited and why;
- exact lifecycle disposition of the original diagnostic question;
- exact new research question/status/brief path;
- planning statuses/gates changed, especially outward convergence and `PLAN-ENG-5-0` consequences;
- architecture/ADR text intentionally left unchanged pending research;
- validation results;
- knowledge-transfer audit disposition;
- whether the workspace is `RETIREMENT-ELIGIBLE AFTER MERGE` or must remain, and why;
- the single next action after reconciliation, expected to be execution of the focused simulation-analytics research followed by independent adversarial review if it remains high risk.

Do not claim the analytics architecture is decided merely because this reconciliation creates the research brief.
