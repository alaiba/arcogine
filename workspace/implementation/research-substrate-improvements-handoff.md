# Implementation handoff — research experiment substrate improvements

## Identity

Transient implementation handoff on branch `alaiba/research-substrate-improvements`. The branch was
originally created from historical `main` `cac5a305a1934cee003c0965418af934acd823f8`. At this handoff
revision, live `main` is `335908a154170655d4a6f694db8826980dee4547`, which includes the configured-
resource current-truth clarification and the expanded resource/game research portfolio from PRs #436
and #437. Re-resolve live `main` before starting and bring this branch current with a history-preserving
merge if needed. Preserve prior handoff commits in history rather than rebasing or force-pushing them
away.

The implementer edits this branch; it is the implementation PR's branch. Before handing off to review,
delete this file. `workspace/` must be absent from the final tree, as
`.github/scripts/check-transient-workspace.mjs` enforces.

## Role and contracts

Operate as an ordinary implementation agent. Before substantive work, read and follow:

1. `AGENTS.md`, in particular product-source lifetime rules, validation, PR-body preflight, and
   implementation continuation;
2. `.github/CONTRIBUTING.md`;
3. `docs/development/testing.md`;
4. the package documentation in
   `product/domains/factory/src/test/java/com/arcogine/factory/research/package-info.java`, and every
   class in that package;
5. `docs/architecture/overview.md`, its Layout and module-dependency material, and the
   `architecture-conformance-test` module, before changing module structure;
6. `docs/architecture/factory-model.md` and `docs/architecture/engine-semantics.md`, especially the
   current configured-resource execution facts and final resource-selection tie-break; and
7. the current research boundaries in
   `docs/research/investigations/simulation-analytics-consumer-boundary.md` and
   `docs/research/investigations/engine-shared-resource-final-tiebreak.md` so reusable experiment
   measurements are not promoted into analytics or Engine policy by implementation naming.

Use the devcontainer for Gradle, per `AGENTS.md` on Windows. Commit with the repository owner's human
identity and add no tool or model attribution.

## Background (why this work exists)

A recent research run used the test-only research experiment package
(`com.arcogine.factory.research`) for roughly 30,000 deterministic runs over a non-spatial
`CUT -> ASSEMBLE -> INSPECT` design space. The substrate's evidence discipline held. The run also
exposed concrete gaps, listed below. All are research/test-infrastructure improvements; none is a
production semantic change.

The source strategy-space investigation remains active and unreconciled. Its branch-local measurements,
operational definitions and conclusions are evidence to learn from, not accepted product requirements or
architecture. This implementation must improve the reusable experiment substrate without turning that
investigation's interpretations into repository-wide semantics.

## Hard boundaries

- **Research infrastructure only.** After step 0, all substrate code lives in the dedicated research
  module. Changes elsewhere are limited to Gradle settings and build files, an architecture-conformance
  rule, moved or updated tests, and the layout and documentation updates step 0 requires. No production
  source, Engine rule, Factory model, or public API changes. The guarantee that
  `ResearchPackageBoundaryTest` gives — the substrate reads only the supported runtime contract and is
  never a production dependency — must hold at least as strongly after the move.
- **No transient references.** Per `AGENTS.md`, product source and tests must not reference
  `docs/planning/`, `docs/research/`, `workspace/`, or temporary coordinates (`PLAN-*`, `REV-*`,
  workspace SHAs). Describe behavior semantically and cite only durable authorities such as
  `docs/architecture/engine-semantics.md`.
- **Research-local stays research-local.** New derivations are `Oracle` implementations with a
  `ResearchDefinition`. Their Javadoc must say they are not Engine facts, game-owned analytics, or
  public API, matching existing oracles. Do not read scheduler, handler, or store internals. A reusable
  oracle may calculate a measurement such as occupancy, waiting work or dispatch counts; it must not
  silently name that measurement the active constraint, bottleneck, optimum or other investigation-
  specific interpretation while the owning diagnostic/analytics questions remain open.
- **No new Engine interpretation.** Expected values in new fixtures are hand-derived from the
  specification, as `StarterCorpus` does, and change only with an Engine definition change.
- **Characterize `MachineId` sensitivity; do not select policy.** Current `MachineId` ordering is the
  final deterministic resource-selection tie-break. A fixture may prove that an explicit ordering can
  affect one current-semantics shared-resource case, but that does not make every shared-resource design
  order-sensitive, does not establish the tie-break as desirable, and does not satisfy the promotion
  trigger for the separate shared-resource final-tie-break research CANDIDATE.
- Keep Checkstyle, `-Xlint:all -Werror` and every module's coverage gate green. `:factory` coverage
  must not depend on the moved research tests; if it drops below its floor, report it rather than
  lowering the floor silently.

## Work items

Each item states the problem and the acceptance evidence. Exact class shape is the implementer's call;
prefer extending existing types over parallel ones. Do step 0 first; items 1–9 then land directly in
the new module.

### 0. Extract the substrate into a dedicated research module

**Problem.** The substrate is test code inside `:factory`
(`product/domains/factory/src/test/java/com/arcogine/factory/research/`). Its supported-contract-only
rule is enforced by a source-text scan; it cannot be reused by other modules' experiments; experiments
have no home; and no durable document names it.

**Do.**
- Create a **non-shipped internal research-support Gradle module** under `product/` (suggested
  `product/research-experiments`, project `:research-experiments`; final name is the implementer's call
  if a clearer one fits repository conventions) and register it in `product/settings.gradle.kts`.
- Put the reusable substrate classes in that module's `src/main/java` and the module's own corpus,
  contract and proving tests in `src/test/java`. This explicit shape makes reuse by branch-local
  experiments possible and makes a module coverage gate meaningful; "non-shipped" means the module is
  repository research infrastructure, not a product/runtime capability or outward artifact.
- The reusable main substrate depends only on `:factory` and `:types` through ordinary dependencies,
  so the compiler limits it to `:factory`'s public API. Existing corpus tests currently exercise
  Factory semantic comparison through Governance API types; a narrowly scoped `testImplementation`
  dependency on `:governance` is acceptable for those tests if still needed after the move. Do not let
  that test dependency leak into the reusable substrate. If main substrate code needs Governance or a
  non-public `:factory` type, stop and report it rather than widening production visibility.
- Move the substrate classes and their tests (including `StarterCorpus`, oracles and the corpus/contract
  tests) into the module, keeping one package (a rename such as `com.arcogine.research.experiment` is
  acceptable if it reads better). Preserve history with `git mv` where practical.
- No production or consumer module may depend on the research module. The test-only
  `architecture-conformance-test` may depend on/inspect it solely to enforce that direction. Add a
  conformance rule asserting that production/consumer modules do not depend on or reference the research
  package.
- Rework `ResearchPackageBoundaryTest` so its guarantees survive the move: not on the production
  classpath, no reference from production sources, and no use of scheduler, handler, or store internals.
  Prefer the module dependency boundary plus an ArchUnit rule over source-text scanning; keep a text
  check only for what the compiler cannot prove.
- Make the module part of the CI Java gate (compile, Checkstyle, tests, coverage verification with a
  sensible floor) per `docs/development/testing.md`. Confirm `./arcogine` and CI pick it up.
- Update the durable descriptions in the same change: the Layout sections of `AGENTS.md`,
  `.github/CONTRIBUTING.md`, and `docs/architecture/overview.md` (module list and dependency graph),
  plus `docs/development/testing.md`. Add a short "executable research substrate" paragraph to
  `docs/development/researching.md` naming the module as the standard place for deterministic
  experiments and stating its boundary: supported runtime contract only, research-local derivations,
  never a production dependency. Keep the wording durable and semantic.
- Update `.github/scripts/classify-changes.sh` and its focused tests so the new durable
  `product/research-experiments/` surface is intentionally classified as backend rather than remaining
  permanently dependent on the fail-safe "unknown path" fallback.

**Accept.**
- `:factory` no longer contains the research package.
- The new module builds and tests green in the CI gate.
- The ArchUnit rule fails if a product module is made to depend on the research module (demonstrate it
  once locally, then revert).
- Every moved corpus and contract test passes with unchanged expectations.
- The layout, dependency graph and research/testing documentation name the module.
- The change classifier recognizes the module path explicitly and its focused regression tests pass.


### 1. Shared-eligibility routing family

**Problem.** `ThreeStepRoutingFamily` gives every resource exactly one stage, so it cannot express a
resource eligible for several steps, which current Factory semantics allow and which proved load-bearing.

**Do.** Provide a way to author a three-step (or general linear) family from resources, each with a
concurrency and the set of steps it may serve. The authored model must remain exactly projectable onto
the current Factory model: no per-resource speed, no spatial record, and `setupTime`/`capacityLiters`
not used to imply behavior. Keep `ThreeStepRoutingFamily`'s existing API working, or migrate its
callers in the same change.

**Accept.** Tests show a shared resource appears in every listed step's eligible set and publishes
through `FactoryModelPublisher`, and that existing `StarterCorpus` fixtures are unchanged in model and
fingerprint.

### 2. Explicit resource ordering as an experiment input

**Problem.** `MachineId` is assigned from resource construction/list order, and it is the current
Engine interpretation's final resource-selection tie-break (`engine-semantics.md` §2 rule 4). The
strategy-space run found current-semantics shared-resource cases where reversing that order changed
completion by up to 6 ticks, while its tested dedicated-only family was order-invariant. That is a
characterization of those cases, not a universal statement about all dedicated or shared resources.

**Do.** Make resource order an explicit authored input of the family or builder from item 1. Document
precisely that ordering **can** become result-affecting when otherwise tied selection reaches the final
`MachineId` rule; do not describe `MachineId` ordering as desirable policy or imply that every
shared-resource model is order-sensitive. The fixture API exposes an experiment input; it does not
promote or reopen Engine semantics.

**Accept.** Two pinned current-semantics fixtures with hand-derived expectations:
- one dedicated-only design whose completion is invariant under reversed resource order;
- one design with a shared resource whose completion differs under reversed order.

The test names/Javadoc describe only those proven cases and current interpretation, not a universal
resource-order law.

### 3. Pool occupancy measurement — not constraint semantics

**Problem.** `ProcessingOccupancyOracle` is per resource and cannot truthfully attribute a shared
resource's capacity to one operation step. The strategy-space investigation used eligibility-connected
pool occupancy as one research-local way to interpret constraint behavior, but that interpretation is
not an accepted reusable diagnostic or analytics semantic.

**Do.** Add an oracle that groups steps into pools — connected components of the step–resource
eligibility graph — and reports **measurements** per pool: occupied job-ticks
(`JOB_DISPATCHED` to `JOB_STEP_COMPLETED`) and capacity job-ticks. It must never attribute shared
capacity to an individual step. If a convenience result identifies the pool(s) with the maximum exact
occupancy ratio, name it in measurement terms (for example maximum-occupancy pools), compare ratios
exactly and return ties as a set; do not expose `activeConstraint`, `bottleneck`, or equivalent
semantic labels.

Capacity arithmetic must be truthful when resource availability changes. The smallest acceptable first
implementation is to define occupancy over continuously-online resources and **refuse** when a relevant
resource has an availability transition in the measured interval. A more complete implementation may
derive effective available-capacity intervals from supported observations/events, but must not inspect
handler/scheduler internals. Apply the same availability rule to the existing
`ProcessingOccupancyOracle`; do not leave two reusable occupancy measures with inconsistent
denominators.

Also refuse on an incomplete event window, zero elapsed time, or a completion without a dispatch.

**Accept.** Contract tests in the style of `OracleContractTest` cover:
- declared inputs only;
- refusal on partial event windows;
- refusal (or, if fully implemented, correct interval accounting) for a relevant availability change;
- hand-derived measurements on a dedicated fixture (one pool per step); and
- hand-derived measurements on a shared fixture (merged pool).

No reusable API or test claims that maximum occupancy is the Factory/Engine "constraint" or
"bottleneck"; an individual investigation may make that operational interpretation in its own
research artifact.

### 4. Waiting work and occupancy are different diagnostics

**Problem.** A faster upstream step can build the largest queue ahead of a different step/resource
that has the higher measured occupancy. Observed case: routing CUT 3 / ASSEMBLE 4 / INSPECT 5 ticks,
one continuously-online single-concurrency resource per step, quantity 12. At tick 33, three jobs wait
at ASSEMBLE and one at INSPECT, while INSPECT has the higher occupancy over the run (60/67 vs 48/67
job-ticks).

**Do.** Add this as a pinned corpus fixture (re-derive the numbers by hand from the specification; do
not copy them as unexplained constants). Add Javadoc to `WaitingWorkByStepOracle` stating that waiting
work and interval occupancy answer different questions and are not interchangeable. Do **not** replace
the old overclaim with a new one: neither measurement alone is a repository-wide bottleneck or
constraint definition.

**Accept.** The fixture's claims for waiting work and occupancy (item 3, or the existing oracle) both
hold and rank the steps differently, exactly as described. The test demonstrates diagnostic
non-equivalence, not which measure is universally "right."

### 5. Avoid double publication

**Problem.** `ExperimentFixture` publishes in its constructor for validation, and `ExperimentRunner`
publishes again.

**Do.** Remove the redundant publication only if it can be done without distorting the fixture's value
semantics. The invariant is that one validated/published `FactoryModelVersion` is established for a
fixture execution lifecycle and reused to instantiate its runtime; caching is one possible mechanism,
not a requirement. Keep fail-at-construction validation. If satisfying both properties would require
materially redesigning `ExperimentFixture` solely to avoid a second pure validation/publication call,
leave the current behavior in place and record the cleanup as deliberately skipped rather than forcing
an abstraction.

**Accept.** Existing tests pass. If implemented, a test or assertion shows the runner instantiates the
runtime from the already established published version without changing fixture equality semantics. If
skipped under the bounded rule above, state that explicitly in the implementation handoff.

### 6. Replay helper

**Problem.** Every caller re-implements "run twice and compare evidence after
`withNormalizedRunIdentity()`".

**Do.** Add a helper (for example `ExperimentRunner.runAndReplay(fixture)`) that runs twice, fails with
a clear message on any difference, and returns the evidence. Use it in `StarterCorpusTest`'s replay test.

**Accept.** The helper is used by existing replay tests, plus a negative test showing it detects a
difference, using `TamperedEvidence` or an equivalent existing mechanism.

### 7. Shared small utilities

**Problem.** Completion tick and per-resource/per-step dispatch counts are re-derived ad hoc. A private
`completionTick` already exists in `StarterCorpusTest`.

**Do.**
- Provide a completion-tick accessor that requires an accepted single order and cross-checks the
  closing observation's `completedAt` against the single `ORDER_COMPLETED` event, refusing or failing
  on disagreement.
- Provide a dispatch-profile oracle: `JOB_DISPATCHED` counts per resource and step, and mean wait per
  step as an exact sum/count, measured from the previous step's completion or from order acceptance.
- Replace the private duplicate.

**Accept.** Unit tests for both, including the disagreement or incomplete-window refusal paths.

### 8. A documented way to run an ephemeral experiment

**Problem.** Running a workspace-held experiment required copying a test file into the tracked test
tree and deleting it afterwards, which is fragile.

**Do.** Choose the smallest mechanism, such as an opt-in Gradle property that adds an extra source
directory to the research module's tests, ignored when unset. Document it in `docs/development/testing.md` in
durable terms: no `workspace/` path is baked into source; the directory is supplied by the caller. If no
safe mechanism exists without weakening the CI gate or the coverage verification, document the copy
procedure instead and record that decision in the PR description.

**Accept.** With the property unset, CI behavior is unchanged. With it set, a class in an external
directory compiles against the research module's classpath and runs under `--tests`.

### 9. Pooling proving case (optional, if items 1–3 land cleanly)

Add a corpus fixture demonstrating capacity pooling with hand-derived expectations: routing CUT 3 /
ASSEMBLE 4 / INSPECT 5, quantity 12, comparing
- (a) one cutter, one assembler, two inspectors — completes at 56, ASSEMBLE-limited, the second
  inspector half idle;
- (b) one cutter, one assembler, one inspector, and one resource eligible for both ASSEMBLE and INSPECT
  placed last in resource order — completes at 47, with the shared resource serving both steps.

Keep costs and any game framing out of the fixture: it states current Factory/Engine production facts
only. Its presence does not accept, reconcile, conclude, or create a product requirement from the still-
active strategy-space investigation. Skip this item and say so in the handoff if hand-deriving (b) proves
impractical. Do not paste runtime output as truth.

## Validation and handoff

- Run the narrowest Java gates that exercise the change in the devcontainer, then the full CI Java gate
  from `docs/development/testing.md` before handoff.
- Open a PR to `main` using the repository template. The body records stable intent only, with no
  validation results.
- Own exact-head `CI / gate` convergence per `AGENTS.md`.
- Report validation commands, outcomes, and anything skipped in the implementation handoff, not in the
  PR body.
- Delete this handoff file before handing the PR to review.
