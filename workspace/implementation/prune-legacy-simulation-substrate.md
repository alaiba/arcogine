# Implementation handoff: prune orphaned legacy simulation substrate

## Repository and authority

Repository: `alaiba/arcogine`  
Umbrella initiative: #379  
Baseline at handoff creation: live `main` `2e13eb5fcf71823740453b1c80d0e792fa38a746`  
Relevant landed predecessors:
- #380 retired the React/Vite web consumer.
- #384 retired the Spring HTTP/SSE API and Picocli CLI application shell and moved durable architecture-conformance rules into the retained test-only conformance module.
- #387 reconciled Factory semantic composition and introduced a separate READY Engine-applicability research question. That research lane is independent of this cleanup and must not be modified or pre-empted here.

At handoff creation there were no open pull requests.

The repository is the complete source of truth for current Arcogine. The project owner is the sole source of new future-product direction. Do not retain implementation solely because similar functionality might be useful later. Git history is the archive.

Read and follow `AGENTS.md` before doing anything else. This prompt is transient delivery scaffolding. It must be deleted before independent PR review and must not appear on the merge candidate.

Before implementation, resolve live `main` again. If `main` advanced, reconcile this branch with current `main` and re-check all current-state assumptions below.

## Read first

Read these files from the exact target ref before editing:

- `AGENTS.md`
- `.github/agents/work-planner.agent.md`
- `.github/CONTRIBUTING.md`
- `docs/product/charter.md`
- `docs/architecture/overview.md`
- `docs/architecture/runtime-contract.md`
- `docs/architecture/external-representations.md`
- `docs/architecture/factory-model-v1.md`
- `docs/development/testing.md`
- `docs/planning/factory-simulation-engine-readiness.md`
- `docs/research/research-register.md`
- `docs/research/investigations/simulation-analytics-consumer-boundary.md`
- `docs/research/investigations/factory-design-game-vertical-slice.md`
- `product/simulation/build.gradle.kts`
- `product/simulation/src/main/java/com/arcogine/core/runner/SimRunner.java`
- `product/simulation/src/main/java/com/arcogine/core/runner/SimResult.java`
- `product/simulation/src/main/java/com/arcogine/core/handler/ModelProvenanceSource.java`
- `product/simulation/src/main/java/com/arcogine/core/log/EventLog.java`
- `product/simulation/src/jmh/java/com/arcogine/core/bench/ScenarioRuntimeBenchmark.java`
- `product/simulation/src/test/java/com/arcogine/core/runner/SimRunnerTest.java`
- `product/simulation/src/test/java/com/arcogine/core/runner/DeterminismTest.java`
- `product/simulation/src/test/java/com/arcogine/core/log/EventLogTest.java`
- `product/simulation/src/test/java/com/arcogine/core/kpi/KpiTest.java`
- `product/domains/factory/src/main/java/com/arcogine/factory/model/FactoryModelVersion.java`
- `product/domains/factory/src/test/java/com/arcogine/factory/process/HeadlessClosureAcceptanceTest.java`

Also inspect the current `com.arcogine.core.kpi` production package and current FactoryRuntime/Engine conformance tests before removing anything.

Do a quick repository/docs search for at least:

- `SimRunner`
- `SimResult`
- `ModelProvenanceSource`
- `EventLog`
- `com.arcogine.core.kpi`
- `KpiValue`
- `contentHash()`
- `modelContentHash`
- `ScenarioRuntimeBenchmark`
- `scenario TOML`
- `determinism`
- `RuntimeObservation`
- `RuntimeEvent`
- `FactoryRuntime`
- `analytics`
- `KPI`

Search results are discovery only. Fetch the matched files at the exact target ref before relying on them.

## Objective

Remove the legacy simulation compatibility/analysis substrate that became mechanically orphaned after the web and API/CLI application surfaces were retired.

This slice is deletion-first. It should remove production abstractions that now have no retained production consumer rather than preserving them as speculative compatibility machinery.

The expected deletion chain is:

```text
legacy result provenance:
FactoryModelVersion.contentHash()
    -> ModelProvenanceSource
    -> SimRunner
    -> SimResult

legacy analysis/history substrate:
internal EventLog
    -> com.arcogine.core.kpi.*
    -> legacy runner/tests/old outward projections
```

The retained modern boundary is:

```text
FactoryModelVersion.fingerprint() -> ModelFingerprint
FactoryRuntime
    -> RuntimeObservation
    -> RuntimeEvent
    -> EngineSemanticsVersion / RunId / ModelFingerprint provenance
```

Do not replace the deleted substrate with a new generic runner, event journal, KPI framework, analytics layer, or compatibility adapter.

## Why this slice is now justified

After #384, repository search shows no retained production caller of `SimRunner`; `SimResult` and `ModelProvenanceSource` exist only for that runner/provenance chain.

`EventLog` production imports are confined to the legacy runner and generic KPI implementation. Retained Finance code only mentions EventLog in explanatory Javadoc and does not depend on it.

The generic `com.arcogine.core.kpi` package has no retained production consumer.

`FactoryModelVersion.contentHash()` remains explicitly documented as legacy compatibility provenance while the supported durable identity is already `ModelFingerprint`.

Verify these facts on current `main` before deletion. If any new retained production consumer has appeared since this handoff was written, stop and re-scope rather than deleting through an active contract.

## Protected capability and invariants

Do not weaken or reopen current semantic/governance capability merely because old compatibility code is being removed.

Preserve:

- Product Charter direction;
- Governance/conformance and evidence capability;
- semantic identity non-rebinding and support/custody rules;
- `FactoryModelVersion.fingerprint()` and the named Factory fingerprint policies;
- `ModelFingerprint`;
- `FactoryRuntime`;
- `RuntimeObservation`;
- `RuntimeEvent`;
- `RunId`;
- `EngineSemanticsVersion`;
- `engine-semantics:v1` conformance evidence;
- deterministic scheduler/runtime behavior required by retained Engine semantics;
- `HeadlessClosureAcceptanceTest` and other current FactoryRuntime acceptance/conformance evidence;
- architecture-conformance-test;
- research lifecycle/governance;
- scenario/TOML infrastructure unless a narrow change is required to keep compilation coherent;
- Economy, Finance, Agents, Challenge, and their current domain tests in this slice.

Do not touch the READY Engine-applicability research created by #387 except for an unavoidable factual reference correction. This cleanup does not answer which Engine identity executes optional-record Factory policies.

## Required production deletion scope

### 1. Remove legacy runner/result/provenance

Delete:

- `com.arcogine.core.runner.SimRunner`
- `com.arcogine.core.runner.SimResult`
- `com.arcogine.core.handler.ModelProvenanceSource`

Delete their tests and any production/test-only helpers whose sole purpose is supporting those types.

Do not introduce a replacement runner.

### 2. Remove EventLog

Delete `com.arcogine.core.log.EventLog` and its dedicated tests after verifying no retained production capability depends on it.

Do not replace it with a differently named internal event-history collection merely to preserve the old shape.

Internal `Event`, `EventPayload`, `EventType`, `Scheduler`, and other event-driving machinery remain if required by retained simulation/domain execution. The removal target is the retained-history/log abstraction, not the event-driven engine itself.

Where durable architecture currently uses the deleted concrete type as an example for a broader rule, keep the rule and remove or generalize the obsolete implementation name. In particular, "internal scheduler history is not the supported outward/runtime history contract" remains a valid boundary even after `EventLog` is gone.

### 3. Remove generic core KPI implementation

Delete the production package `com.arcogine.core.kpi` and its dedicated tests when current dependency search confirms no retained production consumer.

This includes the generic log-derived KPI abstractions such as:

- `Kpi`
- `KpiValue`
- `EventCount`
- `OrderCount`
- `ThroughputRate`
- `TotalSimulatedTime`

Do not replace them with formulas over `RuntimeObservation`/`RuntimeEvent` in this PR.

The READY Simulation Analytics Consumer Boundary research owns the unresolved question of what, if anything, should become supported consumer-neutral analytics. Deleting an obsolete implementation is not an implementation conclusion for that research.

### 4. Remove legacy FactoryModelVersion contentHash compatibility surface

Remove `FactoryModelVersion.contentHash()`.

Also remove the private Java-derived canonical representation machinery and imports that exist only to implement `contentHash()`, if they have no other retained use.

Do not change `FactoryModelVersion.fingerprint()`, `FactoryModelFingerprintV1`, V1/V2 fingerprint policies, or current canonical semantic identity.

Remove or update tests that assert the legacy content-hash implementation while preserving all durable fingerprint/canonicalization tests.

The result must make the distinction simpler, not create a replacement untyped hash.

### 5. Reconcile JMH benchmarks

`ScenarioRuntimeBenchmark.runBasicScenario1000Ticks()` currently benchmarks `SimRunner` with a no-op handler. Remove that benchmark and runner-only helper code.

Retain `scenarioLoadAndValidate` only if `ScenarioLoader` remains a current justified capability after the edit. Do not delete ScenarioLoader merely to simplify this PR.

If the benchmark file becomes misleadingly named or empty after runner removal, rename/simplify it semantically while preserving any still-useful scenario-loader benchmark.

## Determinism evidence: do not preserve obsolete tests mechanically

The legacy `DeterminismTest` proves repeatability through `SimRunner`, `EventLog`, and generic KPIs. Those concrete surfaces are being deleted.

Before removing or rewriting that test, inventory the same semantic claims in retained modern evidence, especially:

- FactoryRuntime tests;
- Engine-semantics:v1 conformance fixtures;
- `HeadlessClosureAcceptanceTest`;
- supported RuntimeObservation/RuntimeEvent equivalence tests.

If the old runner test only duplicates already-retained modern determinism evidence, delete it rather than porting EventLog/KPI-era assertions.

If it contains a still-required determinism invariant not proven anywhere else, move only that invariant onto the current supported Engine boundary. Do not reconstruct a legacy runner or log merely to keep the test shape.

The final repository should prove determinism through supported Engine semantics, not through an orphaned application-era harness.

## Build/coverage reconciliation

Update `product/simulation/build.gradle.kts` comments/configuration that specifically justify coverage using deleted EventLog/KPI tests.

Do not retain dead source or dead tests to protect a historical coverage percentage.

Keep the module's coverage gate meaningful for the retained simulation code. Prefer keeping the existing numeric floor if the retained test suite still satisfies it. If the deletion materially changes the denominator and a threshold change is necessary, make the smallest evidence-based adjustment and explain it in the PR; do not silently lower quality expectations to make the deletion pass.

Keep JMH only for benchmarks that still measure retained capability.

## Documentation reconciliation

Current-state documentation must stop describing deleted compatibility machinery as current capability or outstanding migration debt.

Inspect and reconcile at least:

- `docs/architecture/overview.md`
  - module structure that currently lists EventLog/KPIs/SimRunner;
  - state ownership table entries for EventLog;
  - event/history/replay wording that names EventLog as current inspection/replay machinery;
  - determinism wording that claims identical scenario TOML produces identical event logs/KPIs where those outputs no longer exist;
  - Factory Model Identity text that calls `contentHash()` / `SimResult.modelContentHash` bounded later cleanup;
  - any current claim that the economy loop is executed through SimRunner.
- `docs/architecture/runtime-contract.md`
- `docs/architecture/external-representations.md`
- `docs/architecture/factory-model-v1.md`
- `docs/architecture/standards-alignment.md`
- `docs/architecture/isa-95-semantic-mapping.md`
- `docs/development/testing.md`
- `docs/planning/factory-simulation-engine-readiness.md`
- `docs/research/investigations/simulation-analytics-consumer-boundary.md`
- `docs/research/investigations/factory-design-game-vertical-slice.md`
- any other current docs found by search.

### Durable architecture rule

Do not delete a durable semantic rule solely because its illustrative implementation disappeared.

Examples:

- Internal scheduler machinery is not a supported outward contract.
- Supported observations expose current authoritative state.
- Supported RuntimeEvents expose ordered authoritative changes.
- Durable Factory identity is `ModelFingerprint` under a named fingerprint policy, not an untyped implementation hash.
- Historical support/compatibility obligations are scoped and must not be invented.

Rewrite these in implementation-neutral terms where necessary.

### Analytics research

The Simulation Analytics Consumer Boundary item is currently **READY**, High priority, and explicitly says no implementation before research/review.

Update its factual current-state premise if necessary to say that the old generic EventLog-derived KPI implementation has been removed.

Do **not**:
- mark the research CONCLUDED;
- lower/raise its priority merely because old KPI code is gone;
- define a replacement analytics contract;
- implement new analytics;
- change its promotion/review requirements.

The question may remain relevant for future Game or other consumers even though the obsolete implementation has been deleted.

### Historical research evidence

Where an investigation records the old KPI/web/API estate as historical evidence, preserve the historical fact if it still supports the investigation, but make the temporal framing unambiguous. Do not rewrite history as though those consumers never existed.

## Explicitly deferred: next capability audit

Do not turn this PR into the domain/capability audit.

Unless required for compilation after deleting the target substrate, retain for the next slice:

- `ScenarioLoader`
- scenario/TOML records and `docs/examples`
- Economy / `DemandModel` / `PricingState`
- Finance / `FinanceHandler`
- Agents / `SalesAgent`
- `CompositeHandler`
- the internal Event/Scheduler machinery used by retained domain/runtime code
- Challenge/Game capability
- Factory spatial/V2 work
- Engine applicability research

After this PR, the next refocus question is whether scenarios, economy, finance, and agents are current Arcogine capabilities or remnants of the retired application experience. That decision must be made deliberately against the Charter and current architecture, not smuggled into this mechanical orphan deletion.

## Issue and initiative reconciliation

This work belongs under umbrella issue #379.

Inspect current open issues before opening the PR. If an issue is entirely owned by the deleted legacy runner/KPI/EventLog/content-hash surface, reconcile/close it according to repository practice only when the landed change truly removes its owner.

Do not create child issues for every deleted class.

Do not close #379.

## Acceptance criteria

The candidate is complete only when:

1. `SimRunner` is absent.
2. `SimResult` is absent.
3. `ModelProvenanceSource` is absent.
4. `EventLog` is absent.
5. The production package `com.arcogine.core.kpi` is absent.
6. `FactoryModelVersion.contentHash()` and its Java-only canonical-hash machinery are absent.
7. No retained production code references any of those removed surfaces.
8. No replacement generic runner, event history, KPI framework, analytics layer, or compatibility adapter was introduced.
9. Retained `FactoryModelVersion.fingerprint()` and named Factory fingerprint policies are unchanged semantically.
10. FactoryRuntime/RuntimeObservation/RuntimeEvent and Engine-semantics:v1 conformance remain intact.
11. Determinism remains executably proven through current supported runtime semantics rather than the deleted runner/log/KPI harness.
12. ScenarioLoader/TOML, Economy, Finance, and Agents are not opportunistically removed in this slice.
13. Simulation Analytics research remains a research question, with current-state wording corrected only as necessary.
14. Current architecture/planning/testing docs contain no false current-state claims about EventLog, generic KPIs, SimRunner/SimResult, ModelProvenanceSource, or contentHash compatibility debt.
15. Retained CI/coverage/benchmark configuration is truthful for retained code.
16. No tracked `workspace/` path remains in the final PR candidate.
17. The change is deletion-dominant and simplifies the supported repository surface.

## Validation

Follow the current `AGENTS.md` validation rules after the edits.

At minimum run the current equivalents of:

```bash
cd product
./gradlew --no-daemon compileJava compileTestJava checkstyleMain checkstyleTest test jacocoTestReport jacocoTestCoverageVerification
```

Also run:

```bash
./arcogine check
bash .github/scripts/classify-changes.test.sh
python3 .github/scripts/check-transient-workspace.py
```

Run relevant JMH compilation/benchmark checks if benchmark sources change; at minimum ensure the JMH source set still compiles if it is retained.

Search the final candidate for stale references to:

- `SimRunner`
- `SimResult`
- `ModelProvenanceSource`
- `EventLog`
- `com.arcogine.core.kpi`
- `KpiValue`
- `contentHash()`
- `modelContentHash`
- `ScenarioRuntimeBenchmark.runBasicScenario1000Ticks`
- phrases asserting current event-log replay/history or generic KPI capability

Historical references are acceptable only when clearly historical and still useful. Current-state docs, build/tooling, and production code must be clean.

If a validation capability is unavailable, report exactly what was not run; do not convert partial validation into a pass.

## Implementation and PR discipline

Work from this branch after reconciling it with current `main`.

Prefer deletion over compatibility preservation.

Keep the PR focused on:
- orphaned runner/result/provenance deletion;
- EventLog deletion;
- generic KPI deletion;
- legacy contentHash deletion;
- directly associated obsolete tests/benchmark code;
- exact documentation/build/test reconciliation required to make those deletions truthful.

Do not perform the later scenario/economy/finance/agents capability audit in this PR.

Use the repository owner's human Git identity for commits.

Before independent review:
- inspect every branch-added file;
- delete this `workspace/implementation/prune-legacy-simulation-substrate.md` prompt;
- run the transient-workspace checker;
- confirm no tracked `workspace/` path remains.

Open one PR against `main` with a stable description containing:
- scope and rationale;
- protected semantic/governance invariants;
- explicit deferred domain audit;
- reproducible validation actually performed;
- reference to #379.

Do not put mutable CI status, mergeability, ahead/behind counts, or other live topology facts in the PR body.

Do not self-review as a substitute for the repository PR Reviewer role. Do not merge the PR yourself; final merge remains with the repository owner.

## Final report

At completion report:

- branch and exact candidate head SHA;
- PR number/link;
- deleted production surfaces;
- any old determinism assertion that was migrated to a modern retained boundary, and why;
- retained benchmark/scenario-loader behavior;
- docs/planning/research reconciled;
- validation commands and outcomes;
- any newly exposed orphan candidates for the subsequent capability audit;
- confirmation that `workspace/` is absent from the merge candidate.
