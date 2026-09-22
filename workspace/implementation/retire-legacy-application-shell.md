# Implementation handoff: retire the legacy API/CLI application shell

## Repository and authority

Repository: alaiba/arcogine
Umbrella initiative: #379
Baseline at handoff creation: live main 1bc86df0fd51230aac9e8285695582ddea03dcd8
Predecessor: #380 merged; the React/Vite web consumer and its maintenance estate are already removed.

The repository is the complete source of truth for current Arcogine. The project owner is the sole source of new future-product direction. Do not preserve implementation solely for hypothetical compatibility or possible future reuse. Git history is the archive.

Read and follow AGENTS.md before doing anything else. This prompt is transient delivery scaffolding under workspace/. It must be deleted from the implementation branch before independent PR review and must not appear on the merge candidate.

Before implementation, resolve live main again. If main advanced, reconcile the branch with the latest main before relying on this handoff's baseline claims. At handoff creation, PR #383 was open and is an independent agents/continuous-improvement lane; do not depend on it or absorb it into this work.

## Read first

Read these current files from the target branch / latest main before editing:

- AGENTS.md
- .github/agents/work-planner.agent.md
- .github/CONTRIBUTING.md
- docs/product/charter.md
- docs/architecture/overview.md
- docs/architecture/runtime-contract.md
- docs/architecture/external-representations.md
- docs/development/testing.md
- docs/planning/runtime-observation-event-delivery.md
- docs/planning/factory-simulation-engine-readiness.md
- product/settings.gradle.kts
- product/build.gradle.kts
- product/interfaces/api/build.gradle.kts
- product/interfaces/cli/build.gradle.kts
- product/interfaces/api/src/test/java/com/arcogine/api/architecture/ArchitectureTest.java
- product/interfaces/cli/src/main/java/com/arcogine/cli/ArcogineCommand.java
- product/interfaces/cli/src/main/java/com/arcogine/cli/HeadlessHandler.java

Also inspect the current trees under product/interfaces/api and product/interfaces/cli, the root arcogine script, .github/workflows/ci.yml, devcontainer configuration, infra/docker, README.md, docs/reference/api.md, and any current documentation that describes API/CLI/headless execution as a current application surface.

Do a quick repository/docs search for at least:

- interfaces/api
- interfaces/cli
- Spring
- Picocli
- SimThread
- SseController
- IntegratedHandler
- HeadlessHandler
- SimRunner
- SimResult
- EventLog
- API
- SSE
- arcogine.jar
- dist/api
- Docker
- PLAN-ENG-4-D
- outward consumer convergence
- ArchitectureTest

Search results are discovery only. Fetch and inspect relevant files at the exact target ref before making current-state claims.

## Objective

Retire Arcogine's legacy Spring HTTP/SSE application shell and the coupled CLI/executable packaging surface.

The target repository after this slice should intentionally have no current end-user application server or CLI product surface. Arcogine may temporarily be a governed semantic/core platform whose retained executable evidence is tests, conformance, benchmarks, and repository tooling.

Do not create a replacement API, CLI, launcher, generic transport abstraction, server wrapper, or compatibility shim.

The purpose of this slice is deletion and truthful reconciliation, not migration.

## Required protected capability

Do not compromise the forward-looking semantic/governance substrate.

Preserve, unless a concrete compile-time relocation is required without semantic change:

- Product Charter direction;
- Governance/conformance capability and its evidence;
- controlled identity/history and provenance/non-rebinding rules;
- FactoryRuntime;
- RuntimeObservation;
- RuntimeEvent;
- RunId, ModelFingerprint, EngineSemanticsVersion semantics;
- engine-semantics:v1 deterministic/conformance evidence;
- HeadlessClosureAcceptanceTest and other retained Engine acceptance evidence;
- research discipline and research register;
- review/governance repository controls;
- durable runtime observation/event architecture;
- internal scheduler machinery still required by retained Engine/domain implementation.

Do not reinterpret the removal of current consumers as permission to weaken semantic contracts.

## Critical prerequisite inside this PR: preserve architecture conformance evidence

The current API test module hosts ArchitectureTest. Several rules in that class are not API tests; they are durable executable architecture guardrails. They must survive API deletion.

Before removing interfaces/api, extract the still-valid cross-domain ArchUnit rules into the narrowest retained test-only module that can see the relevant production modules.

A semantic name such as architecture-conformance-test is appropriate, but do not turn this into a generalized policy framework. Follow the existing test-only-module pattern where useful.

Preserve executable rules equivalent to the current invariants that:

- agents do not depend on Factory internals;
- agents do not depend on Economy internals;
- Finance does not depend on Factory internals;
- Finance does not depend on Economy internals;
- only Finance posts to the Ledger;
- only Factory drives Job production-lifecycle mutation;
- only Factory mutates Machine state.

The API-specific DTO/Spring dependency rule exists to prove a boundary for an adapter that this PR removes. Do not preserve a fake API-specific rule after the API no longer exists. Preserve the durable outward-projection principle in architecture documentation where it already belongs, and retain any consumer-neutral executable evidence that remains meaningful.

Update CONTRIBUTING/AGENTS references so executable architecture enforcement points at the retained conformance-test location, not interfaces/api.

## Required deletion scope

### 1. Remove interfaces/api

Delete the full product/interfaces/api module, including:

- Spring Boot configuration;
- controllers;
- SimThread;
- SseController;
- SimController;
- SnapshotBuilder;
- IntegratedHandler and API-owned orchestration helpers;
- API DTOs;
- API integration/smoke tests;
- API application resources;
- API-only build dependencies/plugins/security overrides.

Remove the :api project from settings and all dependencies on it.

### 2. Remove interfaces/cli

Delete the full product/interfaces/cli module, including:

- ArcogineCommand;
- HeadlessHandler;
- Picocli/Spring launcher behavior;
- executable bootJar packaging;
- stageDist;
- CLI-specific tests and build configuration.

Remove the :cli project from settings and all dependencies on it.

The CLI is part of this slice because it currently packages the Spring server and separately exposes the older SimRunner/scenario orchestration path. Do not preserve it as a nominally independent shell merely to avoid deletion.

### 3. Remove obsolete application packaging and runtime operations

After API/CLI deletion, remove application-only repository machinery whose reason to exist disappears, including as applicable:

- infra/docker/api.Dockerfile;
- application Docker Compose service/configuration;
- API-specific .env template content or the template itself if no retained owner remains;
- API port forwarding from devcontainer configuration;
- Docker-in-Docker devcontainer capability if no retained repository workflow requires it;
- canonical dist/api/arcogine.jar packaging;
- root ./arcogine commands for build/image/up/down/run api/run scenario where their implementation has no retained target;
- shell tests that exist only to enforce those removed commands;
- CI build-dist and Docker image/smoke jobs;
- image scanning of the deleted runtime image.

Keep repository security/governance checks that still protect retained code. In particular, do not delete Java dependency/SBOM auditing or secret scanning merely because runtime image scanning disappears.

Simplify ./arcogine to the smallest truthful retained developer surface. setup, test, check, snapshot and other repository-owned tooling may remain when they still have current value. If check --full remains, redefine it only around retained validation; do not keep dead Docker/application semantics behind the name.

Do not introduce a new distributable artifact to replace arcogine.jar in this slice.

## Documentation and planning reconciliation

Current main must remain the sole truthful current-state description after this PR.

Delete docs/reference/api.md if the API no longer exists.

Reconcile, as applicable:

- README.md
- AGENTS.md
- .github/CONTRIBUTING.md
- .github/SECURITY.md
- docs/product/concepts.md
- docs/product/charter.md only where it contains current-state pointers, without weakening its durable product direction
- docs/architecture/overview.md
- docs/architecture/runtime-contract.md only where it names the removed legacy path as current migration debt
- docs/architecture/external-representations.md
- docs/development/testing.md
- docs/examples/README.md or scenario guidance if it falsely claims a retained CLI execution surface
- other current docs discovered by search.

Do not delete durable architecture merely because a concrete adapter disappeared. Rewrite current-state claims so the durable rule remains comprehensible without the removed implementation.

### Retire PLAN-ENG-4-D as an implementation objective

The product decision is no longer to migrate legacy outward consumers. Therefore:

- remove PLAN-ENG-4-D outward consumer convergence as outstanding Engine work;
- make clear that PLAN-ENG-4-A/B/C are the completed supported runtime observation/event capability;
- remove dependency graphs/status text that treats API/SSE/CLI convergence as a required next Engine slice;
- future consumers should be introduced from the then-current supported contracts when a concrete product need exists;
- do not create a generic future HTTP/CLI migration backlog.

The detailed runtime-observation/event delivery planning artifact should be condensed, completed, or removed according to the repository's planning-lifecycle conventions once it no longer owns active implementation work. Preserve durable semantic meaning in docs/architecture, not by keeping a dead delivery plan alive.

Do not turn PLAN-* coordinates into durable terminology outside planning.

## Existing issues / umbrella reconciliation

This work is under #379.

Inspect currently open consistency issues before opening the PR. If an issue's complete owning surface is deleted by this PR, arrange truthful closure through the PR or issue state according to repository practice. Do not close an issue if retained code/docs still own part of the defect.

Do not create a proliferation of child issues for follow-on orphan cleanup unless a concrete blocker independently justifies one.

Do not mark the umbrella initiative complete.

## Explicitly deferred: orphan/compatibility pruning

Do not expand this PR into the subsequent orphan-pruning slice merely because deletion reveals unused code.

Unless needed to make the repository compile after the requested shell removal, leave the following for the next dependency-guided audit:

- SimRunner;
- SimResult;
- ModelProvenanceSource;
- FactoryModelVersion.contentHash();
- com.arcogine.core.kpi;
- EventLog;
- scenario/TOML infrastructure;
- economy;
- finance;
- agents.

The next slice will re-evaluate those from the post-shell dependency graph.

If this PR exposes an unavoidable dependency that makes one of those impossible to retain without keeping a deleted application shell, prefer the narrowest compile-coherent resolution and document exactly why it crossed the slice boundary. Do not use that exception to perform broad opportunistic cleanup.

## Research boundary

Do not execute or silently conclude research in this implementation slice.

If removal of API/KPI consumers makes a research artifact's current-state premise false, update only the factual current-state wording required for repository truth. Do not change research lifecycle/priority or settle an unresolved semantic question unless the normative research process authorizes that conclusion.

In particular, do not invent a new analytics contract while deleting the old API KPI projection.

## Acceptance criteria

The candidate implementation is complete only when all of the following are true:

1. product/interfaces/api is absent.
2. product/interfaces/cli is absent.
3. product/settings.gradle.kts no longer includes :api or :cli.
4. No retained production module depends on Spring Web/Spring Boot application infrastructure or Picocli solely because of the deleted shell.
5. No retained canonical build creates dist/api/arcogine.jar or an application Docker image.
6. No retained developer/CI command claims to launch the deleted API or CLI.
7. CI no longer has application dist/Docker smoke jobs, while retained Java quality, dependency-audit, secret-scan, workspace, review/governance, and other applicable controls remain enforced.
8. The durable cross-domain ArchUnit guardrails formerly hosted in interfaces/api still execute from a retained test-only location.
9. The API-specific conformance rule is removed rather than fossilized after the API disappears.
10. FactoryRuntime, RuntimeObservation, RuntimeEvent, Engine semantics, Governance, Challenge, and their retained acceptance/conformance evidence are not semantically weakened.
11. docs/reference/api.md and all current-state docs no longer advertise API/CLI/application packaging as current capability.
12. PLAN-ENG-4-D is no longer an outstanding migration objective; future consumer work is demand-triggered.
13. The repository still passes its retained validation surface.
14. No tracked workspace/ path remains in the final PR candidate.
15. The change remains deletion-dominant and does not introduce replacement application infrastructure.

## Validation

Run the narrowest executable validation that exercises every changed surface, following current AGENTS.md after your edits.

At minimum expect to run, or update and then run the current equivalents of:

- cd product && ./gradlew --no-daemon compileJava compileTestJava checkstyleMain checkstyleTest test jacocoTestReport jacocoTestCoverageVerification
- ./arcogine check
- ./arcogine check --full if that command remains part of the retained repository contract
- bash .github/scripts/classify-changes.test.sh
- bash .github/scripts/arcogine-cli.test.sh if retained; otherwise validate the replacement/narrowed wrapper contract
- relevant devcontainer/provisioning helper tests if those files change
- python3 .github/scripts/check-transient-workspace.py after deleting this prompt before review

Also grep/search the final candidate for stale current references to:

- product/interfaces/api
- product/interfaces/cli
- Spring Boot application server
- Picocli
- /api/
- SSE as a current external surface
- arcogine.jar
- dist/api
- api.Dockerfile
- port 3000 where used only for the deleted service
- PLAN-ENG-4-D
- outward consumer convergence

Historical or research evidence may still mention removed implementation when clearly historical. Current-state and executable guidance must not.

If a validation capability is unavailable, report it precisely; do not characterize partial validation as a pass.

## Implementation and PR discipline

Work from this branch after reconciling it with current main.

Prefer deletion to compatibility adapters.

Keep the PR focused on:
- architecture-conformance extraction;
- API/CLI/application-shell retirement;
- exact tooling/CI/docs/planning reconciliation required by that retirement.

Do not mix the later orphan-domain audit into this PR.

Use the repository owner's human Git identity for commits.

Before independent review:
- inspect all branch-added files;
- delete this workspace/implementation/retire-legacy-application-shell.md prompt;
- run the transient-workspace check;
- ensure no tracked workspace/ path remains.

Open one PR against main with a stable description containing:
- scope and rationale;
- protected invariants;
- explicit non-goals/deferred orphan pruning;
- reproducible validation actually performed;
- reference to #379.

Do not put mutable CI status, mergeability, ahead/behind counts, or other live topology facts into the PR body.

Do not self-review as a substitute for the repository PR Reviewer role. Do not merge the PR yourself; final merge remains with the repository owner.

## Final report

At completion, report:

- branch and exact candidate head SHA;
- PR number/link;
- major deleted surfaces;
- where the architecture-conformance rules now live and which rules remain;
- any shell-adjacent artifact intentionally retained and its current justification;
- planning/docs reconciled;
- validation commands and outcomes;
- explicitly deferred orphan candidates exposed by the new dependency graph;
- confirmation that workspace/ is absent from the merge candidate.
