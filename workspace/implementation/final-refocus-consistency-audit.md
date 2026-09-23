# Implementation handoff: final refocus consistency and retained-component audit

## Repository and authority

Repository: `alaiba/arcogine`  
Umbrella initiative: #379  
Baseline at handoff creation: live `main` `7e5e6ad2f8a2707d00fff3e2a4e3db96da85852a`

Relevant landed refocus slices:
- #380 — retired the React/Vite web consumer and its dedicated maintenance surface.
- #384 — retired the Spring HTTP/SSE API and Picocli CLI application shell and preserved durable architecture-conformance evidence.
- #388 — removed the orphaned SimRunner/SimResult/ModelProvenanceSource/EventLog/generic-KPI/contentHash compatibility substrate.
- #391 — retired the Scenario/TOML + Economy + SalesAgent experiment-loop stack, removed speculative FinanceObservation, retained Finance's ledger/handler ownership core, and closed #357/#358.

At handoff creation there were no open pull requests.

The repository is the complete and only source of truth for what Arcogine is today. The project owner is the sole source of new future-product direction. Git history is the archive; do not preserve implementation merely because similar functionality could be useful later.

Read and follow `AGENTS.md` before doing anything else. This prompt is transient delivery scaffolding under `workspace/`; it must be removed before independent review and must not appear on the merge candidate.

Before implementation:
1. resolve live `main` again;
2. inspect current open PRs/issues;
3. reconcile this branch if `main` advanced;
4. re-check all current-state claims below against the exact target ref.

## Purpose of this slice

This is the closure pass for #379.

Do **not** start from a predetermined deletion target. Audit the retained repository as it actually exists after #391 and answer two questions:

1. Does every retained production component have a concrete current reason to exist?
2. Does every maintained current-state document/tool/configuration describe only what actually exists?

If the answer is no for a small, coherent leftover, remove or correct it in this PR.

If the audit exposes another **substantial capability-retirement decision** rather than cleanup/consistency work, do not hide that decision inside this final pass. Keep #379 open, create/identify the narrow follow-up owner, and report why closure is not yet justified.

The goal is a repository whose retained surface is deliberate, lean, internally coherent, and aligned with the Product Charter without weakening forward-looking governance or semantic architecture.

## Read first

Read these authorities/current-state surfaces from the exact target ref:

- `AGENTS.md`
- `.github/agents/work-planner.agent.md`
- `.github/CONTRIBUTING.md`
- `.github/SECURITY.md`
- `README.md`
- `docs/README.md`
- `docs/product/charter.md`
- `docs/product/concepts.md`
- `docs/architecture/overview.md`
- `docs/architecture/runtime-contract.md`
- `docs/architecture/external-representations.md`
- `docs/architecture/standards-alignment.md`
- `docs/architecture/isa-95-semantic-mapping.md`
- `docs/architecture/factory-design.md`
- `docs/architecture/factory-model-v1.md`
- `docs/architecture/factory-model-v2.md`
- `docs/architecture/engine-semantics-v1.md`
- `docs/architecture/governance-conformance.md`
- `docs/architecture/governance-evidence.md`
- `docs/development/testing.md`
- `docs/development/researching.md`
- `docs/development/semantic-contract-support.md`
- `docs/research/research-register.md`
- `docs/research/investigations/simulation-analytics-consumer-boundary.md`
- `docs/research/investigations/factory-design-game-vertical-slice.md`
- `docs/planning/factory-simulation-engine-readiness.md`
- `docs/planning/factory-design-capability.md`
- `docs/planning/governance-conformance-capability.md`
- `docs/planning/spatial-runtime-consequences.md`
- `product/settings.gradle.kts`
- `product/build.gradle.kts`
- root `arcogine`
- `.github/workflows/ci.yml`
- `.github/dependabot.yml`
- `.github/codecov.yml`
- `.github/scripts/classify-changes.sh`
- `.github/scripts/classify-changes.test.sh`
- current devcontainer and repository provisioning helpers
- current Gradle build files for every retained module.

Also inspect the full current production tree for every retained Gradle module.

## Current retained module inventory to audit

At handoff creation, `product/settings.gradle.kts` retains:

- `:types`
- `:governance`
- `:simulation`
- `:factory`
- `:finance`
- `:challenge`
- `:challenge-factory-integration-test`
- `:architecture-conformance-test`

Do not assume every module must survive merely because it is on this list. Verify the actual reason.

Conversely, do not delete a module merely because it has no outward application consumer if it provides current semantic capability, conformance evidence, governance, or a deliberately retained domain invariant.

## Retention test

For each retained production class/package/module and for each major repository support surface, ask:

1. Does a current capability require it?
2. Does a current consumer use it?
3. Does an accepted retained contract need it as executable evidence?
4. Does it satisfy an explicit support/custody obligation?
5. Is it governance/tooling required to safely develop and validate retained code?
6. If none of the above, would reimplementing the then-current need later be cheaper and more truthful than maintaining this code now?

If 1–5 are all no, deletion is the default.

Do not invent a justification from hypothetical future usefulness.

### Expected current justifications — verify, do not blindly assume

These are hypotheses to verify against current code/tests/docs, not permission to skip the audit:

- **types** — shared identities/value types/errors/contracts required by current Factory/Engine/Governance/Finance/Challenge capability.
- **simulation** — scheduler/internal event machinery plus the supported Engine runtime contract and deterministic execution semantics used by FactoryRuntime.
- **factory** — canonical Factory model/publication/fingerprint semantics and current deterministic runtime behavior.
- **governance** — controlled history/change/requirements/conformance/evidence/governed-change capability and its support obligations.
- **finance** — minimal balanced-ledger / `FinanceHandler` implementation that provides executable evidence for the durable commercial/operational/financial ownership boundary.
- **challenge** — the currently admitted/landed headless Challenge capability; verify its current product/architecture role rather than treating module existence as proof.
- **challenge-factory-integration-test** — retained only if it still proves a current cross-module contract that cannot be owned more narrowly elsewhere.
- **architecture-conformance-test** — retained only for non-vacuous executable rules protecting current Factory/Finance/domain boundaries.

Where a module survives, make its current reason obvious from its owning architecture/current-state docs without creating a new permanent "retention registry."

## Protected forward-looking assets

Never trade these away for a smaller diff:

- Product Charter direction;
- semantic identity non-rebinding;
- retained attribution and support/custody rules;
- Factory fingerprint/canonicalization contracts;
- controlled revision/history semantics;
- Governance/conformance/evidence architecture and current implementation;
- `FactoryRuntime`, `RuntimeObservation`, `RuntimeEvent`, `RunId`, `ModelFingerprint`, `EngineSemanticsVersion`;
- deterministic Engine conformance and accepted `engine-semantics:v1` behavior;
- research lifecycle/discipline and independent adversarial-review requirements;
- architecture-conformance rules that protect still-existing ownership boundaries;
- Operational architecture/research even though Operational implementation is not currently admitted;
- current Factory V2 / Engine applicability research and its blocking semantics;
- review/governance controls still required by current repository policy.

Do not rewrite the Charter downward to match the current implementation. Current implementation is intentionally smaller than the product destination.

## Repository-wide stale-surface sweep

Search current `main` for the retired implementation vocabulary and classify every hit as one of:

- current and erroneous -> fix/remove;
- historical/research evidence -> retain only if temporally explicit and still useful;
- durable architecture discussing a retired example -> generalize if the concrete name is no longer useful;
- test fixture explicitly proving retirement/history -> retain only if it has a current owner.

Search at least:

- `product/interfaces/web`
- `interfaces/api`
- `interfaces/cli`
- `Spring Boot`
- `Picocli`
- `dist/api`
- `arcogine.jar`
- `SimRunner`
- `SimResult`
- `ModelProvenanceSource`
- `EventLog`
- `com.arcogine.core.kpi`
- `KpiValue`
- `contentHash()`
- `modelContentHash`
- `:economy`
- `:agents`
- `DemandModel`
- `PricingState`
- `SalesAgent`
- `AgentObservation`
- `FinanceObservation`
- `ScenarioConfig`
- `ScenarioLoader`
- `ScenarioFactoryModelAdapter`
- `CompositeHandler`
- `docs/examples`
- `TOML` as a current input format;
- `DemandEvaluation`, `AgentEvaluation`, `PriceChange`, `AgentDecision`, `AgentEnabledChanged`;
- current claims of a price/demand/agent feedback loop;
- current claims of an HTTP/UI/CLI outward surface.

GitHub code-search results may lag deletions. Treat search as discovery only; fetch the matched path at the exact current ref before acting.

## Production/module audit

For every retained module:

1. inspect public production types;
2. identify current production callers/consumers;
3. identify tests/conformance fixtures that establish current capability;
4. identify owning architecture/specification;
5. identify dependencies and build plugins;
6. challenge unused public surfaces, compatibility aliases, deprecated helpers, speculative observations/DTOs, duplicated identity/provenance, unused serialization, and benchmark-only abstractions.

Delete small unjustified leftovers in this PR when doing so is semantically obvious and keeps the repository coherent.

If a production surface is retained only because tests reference it, determine whether the tests prove a current contract or merely preserve old implementation.

Do not preserve code to protect historical coverage percentages.

## Finance audit

Finance was deliberately retained in #391. Re-audit it rather than exempting it.

The current justification should be executable ownership semantics:
- operational completion facts are distinct from financial interpretation;
- Finance alone owns the ledger;
- postings must balance;
- `FinanceHandler` interprets `OrderCompleted` without reading Factory mutable state;
- currency quantization belongs at the Finance boundary.

Check that every remaining Finance type contributes to that current capability.

Delete any speculative helper/projection/compatibility residue if found.

Do **not** expand Finance into pricing, demand, receivables, revenue-recognition frameworks, multi-currency, forecasting, or hypothetical Finance agents.

If the audit demonstrates that the Finance module no longer provides meaningful current executable evidence beyond what architecture/tests elsewhere already own, treat that as a substantive capability-retirement decision: do not silently erase it in this closure PR. Keep #379 open and surface the decision explicitly.

## Challenge audit

Verify that the Challenge module and its Factory integration test still correspond to admitted current capability.

Do not confuse a completed proving consumer with a mandate to grow Challenge.

Remove only clearly obsolete compatibility/scaffolding. Do not start new Challenge/game implementation or move READY game research into delivery.

## Build/tooling/CI/security audit

Reconcile support machinery against the final retained tree.

Audit:

- Gradle project includes and inter-project dependencies;
- dependency declarations and version overrides;
- JMH source sets/benchmarks;
- Jacoco coverage configuration and comments;
- Checkstyle/source exclusions;
- root `./arcogine` commands and help;
- change-classification paths and tests;
- CI job conditions, `needs`, gate expectation map, artifacts and scans;
- Dependabot ecosystems/directories;
- Codecov flags/paths;
- devcontainer features, mounts and forwarded ports;
- provisioning scripts/tests;
- Docker/container remnants;
- security scanning commands and documentation;
- Gitleaks/Trivy/SBOM configuration;
- ignored/generated paths;
- README/CONTRIBUTING/testing command examples.

Remove support for nonexistent modules/surfaces.

Preserve security/governance controls that still protect the retained repository. Do not weaken dependency auditing, secret scanning, provenance/reproducibility, review controls, or exact-main snapshot safeguards merely because the application surface is smaller.

## Documentation consistency audit

Current-state docs must describe the final repository, while durable architecture/research may discuss future concepts without pretending they are implemented.

At minimum reconcile:

- `README.md`
- `docs/README.md`
- `docs/product/concepts.md`
- `docs/architecture/overview.md`
- focused architecture/specification docs affected by the retained/deleted components
- `docs/development/testing.md`
- `.github/CONTRIBUTING.md`
- `.github/SECURITY.md`
- planning status/index entries.

Specific checks:

- no current interactive/application experience is implied;
- no retired scenario/TOML/economy/agent/API/UI/CLI capability is described as current;
- module/dependency diagrams match `product/settings.gradle.kts`;
- testing/security docs match actual commands/jobs;
- outward-consumer migration remains retired as standing backlog;
- completed refocus work is not left as active delivery debt;
- historical statements are clearly historical;
- research is clearly non-authoritative;
- planning only contains genuinely admitted implementation work.

Do not create a second source of truth by adding a permanent audit checklist or module-retention registry.

## Research portfolio reconciliation

#379 explicitly calls for checking research whose motivating consumer/migration debt was removed.

Follow `docs/development/researching.md`. Do not change lifecycle state or priority merely to make the refocus look complete.

At handoff creation:

- Factory-design game vertical-slice research is **READY / High**.
- Simulation Analytics Consumer Boundary is **READY / High** and already states that the legacy EventLog/KPI/API/web estate was historical evidence and that its removal does not prescribe a replacement.
- Engine applicability to optional-record Factory policies is **READY / Critical-path** and is independent of the refocus.

For each research item materially touched by retired consumers:
- verify its current premise is truthful;
- distinguish historical evidence from a current dependency;
- verify priority/status still follows the normative research model;
- change priority/status only if the research rules and current evidence actually support that change;
- never silently conclude research from code deletion.

It is acceptable for the final answer to be "no research portfolio state change is justified."

## Open issue reconciliation

At handoff creation the open issues were:

- #379 — refocus umbrella.
- #376 — AGENTS command/validation guidance consolidation.
- #374 — PR authoring/review-coordinate ergonomics.
- #363 — standards alignment still treats supported runtime contract as being established.
- #362 — docs index still marks Governance evidence implementation blocked.
- #322 — executable guard for commit coordinates in maintained docs.

### #362

Direct inspection of current `docs/README.md` at the handoff baseline already describes Governance as having landed substrate and headless evidence-use capability. The stale "evidence admission remains blocked" wording reported by #362 is no longer present.

Re-verify on current `main`. If still resolved, close #362 as completed with a short evidence note. Do not manufacture a repository edit merely to close an already-resolved issue.

### #363

Direct inspection of current `docs/architecture/standards-alignment.md` still contains stale wording that the supported runtime observation/event contract "is being established" even though `RuntimeObservation`/`RuntimeEvent` core semantics are landed and outward migration is retired.

If still present, correct the current assessment to match the established runtime contract while keeping transport/adapters as future work, then close #363 through the final PR or issue reconciliation.

### #374, #376, #322

These are separate repository/process concerns. Do not absorb them into #379 merely to reduce the open-issue count.

Touch them only if this audit independently changes their exact owned surface and the issue itself becomes resolved. Otherwise leave them open.

## #379 reconciliation and closure rule

The umbrella body is stale at the handoff baseline: it lists only #380/#384/#388 as landed and still leaves the domain audit plus #357/#358 unchecked even though #391 landed and closed those findings.

Update the live issue state/body during this work so it reflects actual landed history.

Expected landed sequence after verification:
- #380 web retirement;
- #384 API/CLI shell retirement;
- #388 orphaned legacy simulation substrate pruning;
- #391 experiment-loop/domain audit retirement with Finance core retained.

Mark only items actually satisfied.

### Close #379 only if all are true

- every retained production component has a concrete current justification;
- no known obsolete compatibility/application estate remains;
- current docs/tooling/CI/security/dependency maintenance match the retained tree;
- refocus-related findings are resolved or deliberately owned elsewhere;
- research premises have been reviewed and are truthful;
- no substantial new retirement decision was discovered and deferred;
- the merge candidate contains no transient `workspace/`;
- retained validation is green.

If all conditions are met, make the final PR close #379 on merge.

If any material refocus work remains, use `Refs #379`, keep it open, and name the exact remaining owner/blocker. Do not declare closure by checklist aesthetics.

## Consistency issues created by search-index lag

GitHub search may still return deleted source paths for Economy/Agents/Scenario immediately after #391. Do not treat indexed search hits as current evidence.

Always fetch the file at the exact target ref or inspect the exact Git tree before claiming it exists.

## Validation

Run the narrowest validation needed for every changed surface, plus a final repository-wide retained gate.

At minimum run the current equivalents of:

```bash
cd product
./gradlew --no-daemon compileJava compileTestJava checkstyleMain checkstyleTest test jacocoTestReport jacocoTestCoverageVerification
```

From repository root:

```bash
./arcogine check
./arcogine check --full
bash .github/scripts/classify-changes.test.sh
python3 .github/scripts/check-transient-workspace.py
```

Also run:
- architecture-conformance tests explicitly if their module/rules/build file change;
- Markdown/link validation if maintained docs change;
- relevant Node/tooling tests if `arcogine`, CI helpers, snapshot tooling, or provisioning scripts change;
- dependency/security checks required by the current full gate.

If `./arcogine check --full` has changed semantics since this prompt was written, follow current `AGENTS.md` rather than preserving an obsolete command contract.

Do not characterize unavailable/partial validation as a full pass.

## Final stale-reference search

Before review, repeat repository searches for all retired surfaces listed above.

For every hit in a maintained file:
- prove it is intentionally historical/research context, or
- remove/generalize/correct it.

Also inspect exact tracked tree state to prove retired production directories have not reappeared.

## PR discipline

Work from this branch after reconciling it with current `main`.

Prefer small deletions/corrections over new abstractions.

Do not create:
- replacement application surfaces;
- compatibility shims;
- a retained-component registry;
- a "future API" framework;
- speculative analytics;
- a generic agent abstraction;
- new scenario machinery;
- new Finance sophistication.

Use the repository owner's human Git identity.

Before independent review:
- inspect every branch-added file;
- delete `workspace/implementation/final-refocus-consistency-audit.md`;
- run the transient-workspace checker;
- confirm no tracked `workspace/` path remains.

Open one PR against `main`.

The PR description should contain a concise **retained-component justification matrix** as review evidence, not as a new durable repository authority. For each retained module, state its current capability/evidence owner and whether the audit changed anything.

Include:
- exact cleanup/deletions performed;
- #362/#363 resolution;
- research reconciliation result;
- CI/tooling/security reconciliation result;
- validation actually run;
- whether #379 can close.

Use `Closes #379` only when the closure rule above is satisfied. Otherwise use `Refs #379`.

Do not put mutable CI status, mergeability, ahead/behind counts, or other live topology facts in the PR body.

Do not self-review as a substitute for the repository PR Reviewer role. Do not merge the PR yourself; final merge remains with the repository owner.

## Acceptance criteria

The final candidate is complete only when:

1. Every retained production module has a concrete current capability, consumer, conformance/evidence role, custody obligation, or governance/tooling reason.
2. No production class survives solely because tests or historical implementation reference it.
3. No retired application/compatibility module or runtime path is present.
4. No dead build dependency/plugin/configuration remains for retired surfaces.
5. CI/classification/security/dependency-maintenance configuration matches the retained tree.
6. Current docs accurately describe the retained implementation.
7. Durable architecture remains stronger than current implementation and is not rewritten downward.
8. Research premises are truthful; lifecycle/priority changes, if any, comply with the research process.
9. #362 is closed if its reported stale state remains already resolved.
10. #363 is corrected/closed if its stale runtime-contract wording remains.
11. #374/#376/#322 remain separate unless independently resolved by exact changes in this PR.
12. The #379 body reflects #391 and the actual completed refocus work.
13. No new substantial refocus work remains hidden or unowned.
14. The retained validation suite passes.
15. No tracked `workspace/` path remains.
16. If and only if 1–15 are true, #379 closes on merge.

## Final report

Report:

- branch and exact candidate head SHA;
- PR number/link;
- retained-module justification summary;
- any final deletions/corrections;
- Finance and Challenge audit conclusions;
- CI/tooling/security cleanup performed;
- research portfolio reconciliation result;
- #362/#363 outcome;
- #379 closure decision and why;
- validation commands/outcomes;
- any deliberately separate open issues that remain;
- confirmation that `workspace/` is absent from the merge candidate.
