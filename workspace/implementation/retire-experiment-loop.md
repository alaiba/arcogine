# Implementation handoff: retire the legacy experiment-loop stack

## Repository and authority

Repository: `alaiba/arcogine`  
Umbrella initiative: #379  
Baseline at handoff creation: live `main` `cf67df8f28c5bf466960215e3f4552d509db58a4`

Relevant landed predecessors:
- #380 retired the React/Vite web consumer.
- #384 retired the Spring HTTP/SSE API and Picocli CLI application shell.
- #388 removed the orphaned SimRunner/SimResult/ModelProvenanceSource/EventLog/generic-KPI/contentHash compatibility substrate.

At handoff creation, PR #389 (`chore: retire independent Code Owner approval`) was open. It is an independent repository-governance lane. Do not depend on it, absorb it, or treat it as landed until it actually merges. If it advances `main`, reconcile this branch mechanically/semantically as required before final review.

The repository is the complete source of truth for current Arcogine. The project owner is the sole source of new future-product direction. Do not preserve implementation solely because a similar idea may become useful later. Git history is the archive.

Read and follow `AGENTS.md` before doing anything else. This prompt is transient delivery scaffolding under `workspace/`. It must be deleted before independent PR review and must not appear on the merge candidate.

Before implementation, resolve live `main` again. If `main` advanced, reconcile this branch with current `main` and re-check every current-state assumption below.

## Read first

Read these files from the exact target ref before editing:

- `AGENTS.md`
- `.github/agents/work-planner.agent.md`
- `.github/CONTRIBUTING.md`
- `docs/product/charter.md`
- `docs/product/concepts.md`
- `docs/architecture/overview.md`
- `docs/architecture/factory-design.md`
- `docs/architecture/runtime-contract.md`
- `docs/development/testing.md`
- `docs/research/research-register.md`
- `product/settings.gradle.kts`
- `product/types/build.gradle.kts`
- `product/simulation/build.gradle.kts`
- `product/simulation/src/main/java/com/arcogine/core/event/Event.java`
- `product/simulation/src/main/java/com/arcogine/core/event/EventPayload.java`
- `product/simulation/src/main/java/com/arcogine/core/event/EventType.java`
- `product/simulation/src/main/java/com/arcogine/core/handler/EventHandler.java`
- `product/simulation/src/main/java/com/arcogine/core/handler/CompositeHandler.java`
- `product/simulation/src/main/java/com/arcogine/core/scenario/ScenarioLoader.java`
- `product/domains/factory/src/main/java/com/arcogine/factory/model/scenario/ScenarioFactoryModelAdapter.java`
- `product/domains/economy/build.gradle.kts`
- `product/domains/economy/src/main/java/com/arcogine/economy/demand/DemandModel.java`
- `product/domains/economy/src/main/java/com/arcogine/economy/pricing/PricingState.java`
- `product/agents/build.gradle.kts`
- `product/agents/src/main/java/com/arcogine/agents/SalesAgent.java`
- `product/agents/src/main/java/com/arcogine/agents/SalesAgentConfig.java`
- `product/agents/src/main/java/com/arcogine/agents/AgentObservation.java`
- `product/domains/finance/build.gradle.kts`
- `product/domains/finance/src/main/java/com/arcogine/finance/process/FinanceHandler.java`
- `product/domains/finance/src/main/java/com/arcogine/finance/ledger/FinanceObservation.java`
- `product/architecture-conformance-test/build.gradle.kts`
- `product/architecture-conformance-test/src/test/java/com/arcogine/architecture/ArchitectureTest.java`
- `docs/examples/README.md`

Also inspect:
- all files under `product/types/src/main/java/com/arcogine/types/scenario/`;
- all files under `docs/examples/`;
- the current root `arcogine` helper;
- `.github/workflows/ci.yml`;
- `.github/scripts/classify-changes.sh` and its tests;
- README/current-state docs that mention scenarios, TOML, Economy, pricing, demand, SalesAgent, or the old experiment loop.

Do a quick repository/docs search for at least:

- `ScenarioConfig`
- `ScenarioLoader`
- `ScenarioFactoryModelAdapter`
- `docs/examples`
- `TOML`
- `EconomyConfig`
- `AgentConfig`
- `SimulationParams`
- `DemandModel`
- `PricingState`
- `SalesAgent`
- `SalesAgentConfig`
- `AgentObservation`
- `FinanceObservation`
- `CompositeHandler`
- `DemandEvaluation`
- `AgentEvaluation`
- `PriceChange`
- `AgentDecision`
- `AgentEnabledChanged`
- `OfferPrice`
- `demand loop`
- `economy loop`

Search results are discovery only. Fetch relevant files at the exact target ref before making current-state claims.

## Objective

Retire the old application-era experiment loop:

```text
scenario/TOML
    -> pricing/economy
    -> demand generation
    -> Factory event flow
    -> SalesAgent observation/price intervention
    -> generic multi-handler composition
```

That stack was useful to the retired interactive application, but after #380/#384/#388 no retained production code assembles or executes it as a current capability.

The target after this slice is a leaner repository centered on the justified semantic core:

- Factory Design / FactoryRuntime / Engine semantics;
- Governance/conformance;
- Challenge consumer;
- retained Finance ownership semantics;
- architecture/research/planning/tooling required for those capabilities.

Do not replace the removed experiment loop with another scenario runner, orchestration service, policy framework, pricing abstraction, agent framework, or generic composition layer.

This is a capability-retirement slice, not a migration.

## Product/architecture decision for this slice

### Retire Economy and SalesAgent

The current `:economy` and `:agents` production modules have no retained production assembler or consumer after the application shell was removed.

Their present behavior is application-era experiment policy:
- mutable offer-price state;
- a simple demand formula driven by price/lead-time;
- scheduled demand evaluation;
- random order generation;
- one backlog-threshold SalesAgent that changes price;
- orchestration events that existed to run/toggle that policy.

Remove them rather than preserving them as hypothetical future business semantics.

This does **not** mean Arcogine can never model pricing, demand, autonomous decision-making, or commercial policy again. Future capability should enter from then-current product requirements and semantic boundaries, not inherit this unused implementation by default.

### Retain Finance's actual ownership core

Finance is different. The current architecture deliberately distinguishes:

- commercial truth;
- operational truth;
- financial truth.

The minimal ledger and `FinanceHandler` provide executable ownership/invariant evidence for the rule that operational domains emit facts while Finance owns their financial interpretation.

Retain the Finance core unless exact current evidence proves a component is speculative rather than part of that ownership capability.

Expected retained Finance surface includes, subject to exact dependency verification:
- `FinanceHandler`;
- `Ledger` / `LedgerView`;
- `Account`;
- `Posting`;
- `JournalEntry`;
- `Side`;
- `CurrencyPolicy`;
- tests for balanced postings, financial interpretation, and Finance ownership.

Do not broaden Finance in this PR.

### Remove speculative Finance observation if still unused

At handoff creation `FinanceObservation` has no production consumer; it exists only as a future-consumer projection with its own test.

If current `main` still has no production consumer, delete `FinanceObservation` and its dedicated test rather than preserving a hypothetical future FinanceAgent/API shape.

Keep `LedgerView` where it is the actual read-only surface used by current Finance capability/tests.

## Required deletion scope

### 1. Remove the Economy module

Delete `product/domains/economy/` and remove `:economy` from Gradle settings/dependencies.

This includes:
- `DemandModel`;
- `PricingState`;
- economy tests;
- economy-only build configuration.

Do not move their formulas elsewhere.

### 2. Remove the Agents module

Delete `product/agents/` and remove `:agents` from Gradle settings/dependencies.

This includes:
- `SalesAgent`;
- `SalesAgentConfig`;
- `AgentObservation`;
- their tests.

Do not create a replacement generic `Agent`, policy engine, decision framework, actor type, or shared agent abstraction. The concluded agency research explicitly does not justify such a platform abstraction.

Durable governance/architecture principles about humans/agents acting through explicit capability/authority boundaries remain; implementation of one obsolete SalesAgent does not own those principles.

### 3. Remove the application-era Scenario/TOML envelope

Delete the current scenario-loading/configuration estate if exact dependency search confirms it has no retained production consumer outside the old experiment loop:

- `ScenarioLoader`;
- its tests;
- `ScenarioLoaderBenchmark`;
- `ScenarioFactoryModelAdapter` and its tests;
- scenario-only records under `com.arcogine.types.scenario`, including the current `ScenarioConfig`, simulation/economy/agent config records, equipment/material/process/routing config DTOs;
- `docs/examples/` TOML fixtures and scenario README;
- Jackson TOML/serialization dependencies whose only retained reason was scenario parsing;
- Jackson annotation dependencies whose only retained use was scenario config DTOs.

Verify before deleting the entire scenario package. If one of those records has gained a retained non-scenario semantic consumer on current `main`, do not bulldoze through it; isolate and retain only the justified semantic type.

Do not delete canonical Factory model types, Factory model fingerprint policies, Factory publication, FactoryRuntime, or Engine workload APIs merely because the old scenario adapter disappears.

The durable architecture already says scenario/run context and canonical Factory model are different concepts. Deleting today's TOML envelope removes one implementation choice; it does not prohibit future scenario/experiment inputs.

### 4. Remove CompositeHandler if still orphaned

At handoff creation `CompositeHandler` is referenced only by its own production class/test and documentation. The full economy+factory+finance+agents composition has no retained production assembler.

If this remains true, delete `CompositeHandler` and its dedicated test.

Do not create a new orchestrator.

Retain `EventHandler` if Factory and/or Finance still use it as their current event-handling contract.

If Finance remains an isolated event consumer with no production assembler, that is acceptable: this slice preserves its semantic ownership capability, not a currently running application topology.

### 5. Prune experiment-loop event vocabulary

After Economy/Agents/Scenario removal, re-search `EventPayload`, `EventType`, `Event.of(...)`, scheduler tests, Factory tests, Finance tests, and Engine conformance.

Delete event variants whose only current meaning belonged to the retired experiment loop, expected to include if no retained consumer remains:

- `PriceChange`;
- `AgentEnabledChanged`;
- `AgentDecision`;
- `DemandEvaluation`;
- `AgentEvaluation`.

Update `EventType`, `Event.of(...)` exhaustiveness, event/scheduler/property tests, and docs accordingly.

Do not delete event vocabulary still required by retained Factory/Finance semantics, including as applicable:
- `OrderCreation`;
- `TaskStart`;
- `TaskEnd`;
- `OrderCompleted`;
- `MachineAvailabilityChange`.

Do not change the semantic meaning of retained events simply to make deletion convenient.

### 6. Reconcile architecture-conformance evidence

The test-only architecture-conformance module currently depends on `:agents`, `:economy`, `:factory`, and `:finance`.

After Agents/Economy deletion:
- remove obsolete test dependencies on deleted modules;
- remove ArchUnit rules whose subject/object package no longer exists and whose only purpose was policing the retired modules;
- do **not** preserve vacuous rules solely because they once existed.

Retain durable executable rules that still protect current capability, especially:
- Finance must not depend on Factory mutable internals;
- only Finance may post to the ledger;
- only Factory may drive Job lifecycle mutation;
- only Factory may mutate Machine state.

The current `sim_finance_must_not_depend_on_sim_economy` rule becomes meaningless if Economy is deleted; remove it rather than inventing a replacement package.

Durable future-facing principles about decision-makers observing rather than mutating can remain in architecture/contributor guidance without an executable rule targeting a nonexistent SalesAgent package.

## Documentation reconciliation

The current documentation still substantially describes the old price-demand-agent experiment loop as Arcogine's present factory-simulation experience. This PR must make current-state docs truthful.

Inspect and reconcile at least:

- `README.md`
- `AGENTS.md`
- `.github/CONTRIBUTING.md`
- `.github/SECURITY.md` if affected
- `docs/README.md`
- `docs/product/concepts.md`
- `docs/architecture/overview.md`
- `docs/architecture/factory-design.md`
- `docs/architecture/runtime-contract.md`
- `docs/development/testing.md`
- `docs/research/investigations/agency-decision-boundary.md`
- `docs/research/investigations/factory-design-game-vertical-slice.md`
- any standards/research/planning docs found by exact search.

### Product concepts

`docs/product/concepts.md` currently presents a live feedback loop of:
price -> demand -> factory -> KPIs -> SalesAgent/manual price decisions.

That is no longer truthful after this PR.

Rewrite/condense the current concepts page around retained current capability:
- canonical Factory model;
- FactoryRuntime/workload;
- deterministic Engine execution;
- orders/jobs/resources/routings;
- supported observations/events;
- commercial facts retained on orders where relevant;
- Finance's retained financial-interpretation boundary, if useful to current concepts;
- clear statement that there is no current outward application/interactive experiment loop.

Do not invent a new user experience to fill the deleted sections.

### Architecture overview

Remove current-state claims such as:
- "MVP ties factory flow to the economy loop";
- state ownership rows for PricingState/SalesAgent;
- current DemandModel/offer-price feedback behavior;
- current multi-handler dispatch topology containing Economy/Agents;
- module/dependency-graph entries for Economy/Agents;
- current scenario/TOML implementation claims if the implementation is removed;
- current EventPayload taxonomy entries for retired events;
- claims that `AgentObservation` / `FinanceObservation` are current canonical examples if those types are gone.

Preserve durable rules, expressed without dead implementation names where necessary:
- one authoritative owner per mutable fact;
- events/state/observations/decisions as the current architectural heuristic/invariant where still applicable;
- operational facts and financial interpretation are distinct;
- decision-makers must not bypass domain ownership;
- outward projections do not become domain truth;
- commercial terms already accepted by Factory remain immutable historical facts;
- future agent/decision capabilities must use explicit observations/capabilities rather than mutable reach-through.

Do not delete the Charter's future-facing human/agent governance direction merely because `SalesAgent` is removed.

### Factory-design architecture

`docs/architecture/factory-design.md` currently distinguishes Scenario/TOML from canonical Factory model. Preserve that durable distinction, but remove wording that falsely calls the current `ScenarioConfig` envelope an implemented/current input when it has been retired.

A future scenario/run context remains conceptually possible; no replacement schema is selected in this PR.

### Finance architecture

Keep the "Commercial, Operational, and Financial Truth" ownership boundary if Finance remains.

Update examples that rely on deleted Economy/SalesAgent types:
- `OfferPrice` may no longer be current mutable Economy state;
- do not imply `SalesAgent` currently exists;
- retain the core rule that immutable commercial facts carried by an accepted order/event are distinct from Finance's later financial interpretation.

Do not expand Finance into pricing, demand, or accounting sophistication to compensate for Economy removal.

### Research artifacts

Research is not automatically invalidated by implementation deletion.

For agency research:
- historical references to `SalesAgent` may remain when clearly historical evidence;
- current-state claims must not say the implementation still exists;
- do not reopen or alter the concluded no-platform-Agent result unless the research process requires it.

For game research:
- preserve historical evidence where it matters;
- update only factual premises that become false;
- do not conclude or reprioritize game/analytics research merely because the old experiment loop is gone.

## Issue reconciliation

This slice is under #379.

At handoff creation:
- #357 remains open: built-in scenario documentation drift.
- #358 remains open: Sales Agent documentation describes policy inputs it does not implement.

The preferred resolution is deletion, not repair.

If the PR removes the remaining owning/current-state surfaces:
- include `Closes #357`;
- include `Closes #358`;
- include `Refs #379`.

Do not fix the old built-in scenario comparison or SalesAgent policy just to close those issues.

After landing, #379's domain-audit item can be reconciled based on what actually remains. Do not close #379 in this slice.

## Explicit non-goals / defer

Do not remove or redesign in this PR:

- Factory canonical model/publication/fingerprint contracts;
- FactoryRuntime;
- RuntimeObservation / RuntimeEvent;
- EngineSemanticsVersion or engine-semantics:v1;
- current Factory/Engine deterministic conformance;
- Governance/conformance/evidence capability;
- Challenge consumer;
- retained Finance ledger/FinanceHandler ownership core;
- Operational architecture/research;
- current Factory V2/Engine applicability research;
- Simulation Analytics research;
- game product research;
- any future replacement scenario format;
- any new pricing/demand model;
- any generic agent framework;
- any new outward application consumer.

Do not treat "no current consumer" as permission to weaken governance, semantic identity, deterministic evidence, or retained domain ownership invariants.

## Acceptance criteria

The candidate is complete only when:

1. `:economy` is absent from production and Gradle settings.
2. `:agents` is absent from production and Gradle settings.
3. `DemandModel`, `PricingState`, `SalesAgent`, `SalesAgentConfig`, and `AgentObservation` are absent.
4. The current Scenario/TOML implementation is absent if exact dependency verification confirms it has no retained consumer:
   - `ScenarioLoader`;
   - scenario DTO package;
   - `ScenarioFactoryModelAdapter`;
   - example TOML fixtures;
   - scenario-loader benchmark;
   - scenario-only Jackson dependencies.
5. `CompositeHandler` is absent if it remains orphaned.
6. Experiment-loop-only event variants/types are absent.
7. Retained Factory/Finance events keep their established semantics.
8. `FinanceHandler`, ledger invariants, and Finance ownership remain executable and tested.
9. `FinanceObservation` is removed if it still has no production consumer.
10. Architecture-conformance tests no longer depend on deleted modules and still enforce retained Factory/Finance ownership rules.
11. No generic replacement Economy, Agent, Scenario, composition, or policy abstraction is introduced.
12. FactoryRuntime, RuntimeObservation, RuntimeEvent, ModelFingerprint, Engine semantics, Governance, Challenge, and their conformance evidence remain semantically intact.
13. Current product/architecture docs no longer advertise a live price-demand-SalesAgent experiment loop or TOML scenario-loading capability.
14. Historical/research references are clearly historical where retained.
15. #357/#358 are closed by deletion if their full owning surfaces disappear.
16. The change remains deletion-dominant.
17. No tracked `workspace/` path remains in the final PR candidate.

## Validation

Follow current `AGENTS.md` after editing.

At minimum run the current equivalents of:

```bash
cd product
./gradlew --no-daemon compileJava compileTestJava checkstyleMain checkstyleTest test jacocoTestReport jacocoTestCoverageVerification
```

Also run as applicable:

```bash
./arcogine check
bash .github/scripts/classify-changes.test.sh
python3 .github/scripts/check-transient-workspace.py
```

If build files/dependencies change, verify dependency resolution and the full Gradle project graph.

If `architecture-conformance-test` changes, run its tests explicitly and confirm the retained rules still execute.

Search the final candidate for stale current references to:

- `:economy`
- `:agents`
- `DemandModel`
- `PricingState`
- `SalesAgent`
- `SalesAgentConfig`
- `AgentObservation`
- `FinanceObservation`
- `ScenarioConfig`
- `ScenarioLoader`
- `ScenarioFactoryModelAdapter`
- `docs/examples`
- `TOML` as a current Arcogine input format
- `CompositeHandler`
- `DemandEvaluation`
- `AgentEvaluation`
- `PriceChange`
- `AgentDecision`
- `AgentEnabledChanged`
- phrases claiming a current economy loop or current price-demand-agent experience.

Historical/research references are acceptable only when clearly temporal and still useful.

If a validation capability is unavailable, report exactly what was not run; do not characterize partial validation as a full pass.

## Implementation and PR discipline

Work from this branch after reconciling it with current `main`.

Prefer deletion over adaptation.

Keep the PR focused on:
- Economy retirement;
- SalesAgent/Agents retirement;
- Scenario/TOML retirement;
- orphaned CompositeHandler/event-vocabulary cleanup;
- removal of speculative FinanceObservation if still unused;
- architecture-conformance adjustment;
- exact build/tooling/docs/research reconciliation required by those changes.

Do not expand into unrelated Finance redesign, Engine work, Governance work, Factory V2 work, or research execution.

Use the repository owner's human Git identity for commits.

Before independent review:
- inspect every branch-added file;
- delete this `workspace/implementation/retire-experiment-loop.md` prompt;
- run the transient-workspace checker;
- confirm no tracked `workspace/` path remains.

Open one PR against `main` with a stable description containing:
- scope and rationale;
- why Finance was retained while Economy/Agents/Scenario were retired;
- protected semantic/governance invariants;
- explicit non-goals;
- reproducible validation actually performed;
- `Refs #379`;
- `Closes #357` and `Closes #358` if the final tree fully removes their owning surfaces.

Do not put mutable CI status, mergeability, ahead/behind counts, or other live topology facts in the PR body.

Do not self-review as a substitute for the repository PR Reviewer role. Do not merge the PR yourself; final merge remains with the repository owner.

## Final report

At completion report:

- branch and exact candidate head SHA;
- PR number/link;
- deleted modules/surfaces;
- retained Finance surface and its current justification;
- whether `FinanceObservation` was removed;
- which event variants and composition helpers were removed;
- architecture-conformance rules retained/removed;
- docs/research reconciled;
- issue closure for #357/#358;
- validation commands and outcomes;
- any remaining production component that now looks unjustified and should be considered in the final #379 consistency pass;
- confirmation that `workspace/` is absent from the merge candidate.
