# Implementation handoff: reusable research experiment package

Repository: `alaiba/arcogine`
Starting branch: `feature/reusable-research-experiment-package`
Starting live-main baseline: `92b59e99ab4249ec8c514b12f7ca2d1ccb0386e1`

## Objective

Establish the smallest reusable, deterministic, **test-only research experiment package** needed to support Arcogine's READY investigations around:

- non-spatial factory-design strategy space;
- simulation analytics ownership/boundary; and
- player-facing diagnostic evidence.

The package must standardize how a research fixture records its input model, workload/commands, supported runtime evidence window, research-local analytical definitions, and oracle/ground-truth expectations so that the same deterministic case can be reused without each investigation inventing a different harness.

This is research/test infrastructure, not a new Arcogine production subsystem. Do not create a production `ResearchExperiment` API, a generic analytics framework, a new outward adapter, or new Factory/Engine semantics.

## Grounding before implementation

Re-resolve live `main` before making repository-state claims. This branch was created from the baseline above; if `main` has moved materially, reconcile according to `AGENTS.md` before final review.

Read these first:

1. `AGENTS.md`
2. `.github/CONTRIBUTING.md`
3. `docs/development/testing.md`
4. `docs/development/researching.md`
5. `docs/research/research-register.md`
6. `docs/research/investigations/factory-design-game-strategy-space.md`
7. `docs/research/investigations/factory-design-game-diagnostic-evidence.md`
8. `docs/research/investigations/simulation-analytics-consumer-boundary.md`
9. `docs/architecture/runtime-contract.md`
10. `docs/architecture/engine-semantics.md`
11. `docs/architecture/factory-model.md`
12. `docs/planning/factory-design-game-challenge-readiness.md`

Inspect the current implementation and tests for:

- `FactoryRuntime`
- `FactoryRuntimeAssembler`
- `RuntimeObservation`
- `RuntimePerformanceObservation`
- `RuntimeEventEnvelope`
- `PendingWorkObservation` / `PendingWorkView`
- `FactoryModelCanonicalForm` / model fingerprinting
- current determinism and runtime-event acceptance tests
- `ChallengeAttempt` / comparison only to understand existing provenance patterns; do not make the research package depend on Challenge unless a concrete fixture requires it.

Perform a quick repository/docs search for at least:

- `deterministic fixture corpus`
- `research-local`
- `run/event-range`
- `analytical-definition`
- `multi-eligible waiting`
- `busyTicks`
- `concurrency > 1`
- `constraint migration`
- `supported events`
- `drainSupportedEvents`
- `normalized runtime identity`

## Current repository facts to preserve

- Factory owns canonical authored production-system semantics and publication validity.
- Engine/runtime owns deterministic interpretation and mutable simulation truth.
- Challenge owns game admissibility/economics/evaluation, not Factory/Engine execution truth.
- Reusable analytics ownership is intentionally unresolved by the READY analytics investigation.
- Research-local derivations may establish fixture ground truth; their existence in this package must not silently make them Engine facts, game-owned analytics, or public APIs.
- The supported evidence boundary is the published model plus supported runtime observations/events. Do not inspect hidden scheduler state to make an experiment easier.
- Current runtime event delivery is a draining stream, not retained replay. Any experiment evidence window must therefore be explicit about what was collected and when.
- Current Arcogine has no outward HTTP/SSE/API product surface. Do not add one for this work.
- Present spatial content is not part of this package's initial proving corpus. Keep the first package non-spatial.

## Placement rule

Prefer the smallest existing **test source** that can own the first package.

The default direction should be Factory test sources because the initial experiment substrate is about Factory/Engine execution evidence and must not create new production dependencies. Use a dedicated test package such as `com.arcogine.factory.research` or another semantically precise test-only namespace if that fits the existing layout.

Do not:

- add production sources merely to share research helpers;
- create a new Gradle module unless concrete implementation evidence shows existing test sources cannot support the bounded slice cleanly;
- expand `:challenge` to depend on Factory/runtime;
- move generic research infrastructure into the existing `challenge-factory-integration-test` module merely because it can see both domains.

If a concrete cross-domain fixture later proves that Challenge types are necessary, keep that dependency in a test-only boundary and justify it from the actual fixture, not anticipated reuse.

## Required implementation slice

### 1. A small deterministic experiment descriptor

Create the minimum test-only shape needed to describe one fixture. It should make the following explicit without turning them into production concepts:

- stable fixture name/id meaningful inside the test corpus;
- exact Factory model/model version or enough information to reconstruct it;
- model fingerprint from the actual published/canonical model where available;
- ordered workload/command script;
- evidence collection boundaries, including the relevant supported-event sequence range;
- whether the captured window is complete for the claim being tested;
- named research-local analytical definition(s), when an oracle uses one;
- expected/oracle claims kept separate from raw runtime evidence.

Do **not** hard-code the Git commit SHA into durable Java fixtures. The exact repository baseline belongs in the research report/workspace evidence coordinate for a run. The test package should preserve semantic inputs and runtime evidence; research custody preserves the source revision that produced them.

Keep this shape concrete. Do not build a plugin system, serializer framework, registry, or generic experiment DSL unless the first fixtures actually require it.

### 2. An embedded runner over supported contracts

Provide a reusable test helper that can:

1. construct/publish the fixture's Factory model using existing Factory APIs;
2. assemble a fresh `FactoryRuntime`;
3. execute an explicit ordered script of supported workload/command operations;
4. advance deterministically using supported runtime control;
5. drain/capture supported `RuntimeEventEnvelope` evidence at declared points;
6. capture the supported observations needed by the fixture;
7. return a read-only experiment result/evidence bundle for assertions and research-local oracle code.

Do not consume `RecordingScheduler`, internal scheduler queues, private Factory handler state, or other implementation internals as research truth.

If current command results expose implementation-level scheduled events, do not use those as a substitute for the supported runtime-event stream unless the owning Engine contract explicitly makes them part of the research question.

### 3. Explicit evidence-window/completeness semantics

The package must make it difficult to accidentally treat a partial trace as a complete history.

At minimum, captured evidence should retain enough information to state:

- run/session identity before normalization;
- starting supported-event sequence;
- ending supported-event sequence;
- collection point(s);
- whether the fixture intentionally captured the complete event stream for the execution interval under test.

Do not invent event sourcing or retained replay. This is research metadata around the current draining event contract.

### 4. Separate raw evidence from oracle/derivation

A fixture result should distinguish:

```text
authoritative/supported evidence
        |
        +--> observations
        +--> supported runtime events
        +--> published model facts

research-local oracle/derivation
        |
        +--> named method/formula
        +--> derived values
        +--> claims/refusals
```

An oracle calculation must be reconstructable from its declared supported inputs. It must not modify runtime truth or re-decide scheduling/dispatch.

The package should permit an oracle to return an explicit refusal/underdetermined outcome rather than forcing a diagnosis.

### 5. Deterministic replay proof

Add an executable test that runs the same fixture twice in fresh runtimes and proves equivalent supported evidence after normalizing only identities that are intentionally run/session-specific.

Do not normalize semantic differences such as machine IDs, operation IDs, authored ordering, event sequence/order, model fingerprint, or outcome values.

Reuse the repository's existing determinism conventions rather than inventing a second comparison policy.

### 6. Seed a bounded starter corpus

Seed only enough fixtures to prove the package is useful. Do **not** attempt to complete every READY research brief in this implementation slice.

Include representative fixtures covering at least:

1. **capacity-constrained baseline** — a small deterministic non-spatial production model with an obvious active capacity constraint;
2. **controlled capacity variant** — the same family with one authored capacity/resource change so A/B execution can be compared without hidden changes;
3. **multi-eligible waiting counterexample** — work eligible for multiple resources where per-machine queue depth alone would be misleading;
4. **temporal/performance counterexample** — a long unfinished step and/or `concurrency > 1` case that demonstrates why a cumulative completion-credited field cannot be naïvely interpreted as instantaneous utilization.

Prefer a shared, parameterized three-step routing family (for example CUT -> ASSEMBLE -> INSPECT) over many bespoke models if that stays readable.

The full diagnostic corpus named in the research brief — capacity-away, constraint migration, starvation/surplus, confounded pair, etc. — remains research work to add when that investigation runs. This package must make those additions straightforward, not pre-claim their research conclusions.

## Strategy-space compatibility

The fixture family should be capable of supporting the strategy-space investigation later without silently changing Factory semantics.

In particular:

- costs/budget remain research/game-side parameters;
- the projected Factory model must use actual current `ConfiguredResource`, operation eligibility, routing, duration and workload semantics;
- do not invent per-machine speed if the current Factory model does not represent it;
- do not project game floor/placement facts into a present spatial record for this non-spatial corpus;
- keep Challenge admissibility, Factory publication validity, Engine executability, and Challenge success as separate checks.

Do not implement the strategy-space search, scoring, Pareto analysis, or Challenge catalogue projection as part of this infrastructure slice unless a tiny example is strictly necessary to prove the fixture boundary.

## Analytics/diagnostic compatibility

The package should enable later investigations to ask:

- can this value be derived from supported evidence?
- which exact event/observation interval supports it?
- was the evidence window complete?
- does a named derivation survive known counterexamples?
- should the correct result be a refusal rather than a causal claim?

Do not choose durable analytics ownership, KPI names, public compatibility policy, or presentation wording in this change.

## Tests as acceptance evidence

Add focused tests proving at least:

- same fixture + same commands => equivalent normalized supported evidence;
- evidence-window metadata is correct for a complete deterministic run;
- a multi-eligible waiting fixture cannot be reconstructed correctly from per-machine queue depth alone but is represented correctly by supported pending/waiting evidence;
- the temporal/performance counterexample prevents the package/oracle from equating cumulative completion-credited `busyTicks` with instantaneous occupancy/utilization;
- research-local oracle output is computed only from declared evidence and can represent an unsupported/underdetermined claim;
- fixture helpers remain in test-only code and do not create a production dependency/API.

Use ordinary JUnit assertions and concrete fixtures. Avoid snapshot/golden files unless they add discriminating value beyond typed assertions.

## Scope discipline / non-goals

This slice must not:

- change Engine scheduling, dispatch, timing, workload decomposition or event semantics;
- change Factory canonicalization, fingerprints or publication validity;
- add spatial/transfer behavior;
- add HTTP/SSE/CLI/UI adapters;
- define a production analytics domain/module;
- promote a research-local formula into Engine or Challenge;
- implement the full strategy-space, diagnostics, analytics-boundary, retry, scoring, or player-validation research;
- add persistence for experiment results;
- create a permanent research evidence database/format;
- modify research statuses to `CONCLUDED`;
- treat this test package itself as architectural reconciliation.

If implementation exposes a genuine missing supported fact or a hard-to-reverse contract decision, stop expanding the helper and report the gap. Put the semantic decision through the owning research/architecture process rather than resolving it opportunistically in test infrastructure.

## Documentation and semantic closure

A purely test-only helper may require no architecture change.

Update maintained documentation only when this change makes an existing current-state statement false or establishes a durable testing/research practice that the owning development documentation genuinely needs to state. Do not duplicate the fixture catalogue into maintained prose just because it exists in tests.

If you make a significant ownership/public-contract/identity/persistence decision, reconcile it into the owning architecture/specification in the same reviewed change; do not leave such a decision only in code comments or this handoff.

Remember that this prompt under `workspace/implementation/` is transient delivery scaffolding. Remove it from the branch's final tree before independent PR review.

## Validation

During implementation, run the narrowest focused Gradle tests for the affected module(s).

Before implementation handoff, run:

```bash
./arcogine check
```

Run `./arcogine check --full` when the final change warrants the complete local security/audit surface and the required tools are available; do not claim skipped/unavailable checks passed.

After removing the transient prompt and any other branch-local workspace artifacts, run:

```bash
node .github/scripts/check-transient-workspace.mjs
```

Use `docs/development/testing.md` for exact-current native test commands and CI semantics.

## PR/lifecycle requirements

- Keep the branch focused on this bounded infrastructure slice.
- Use concise commits with no bot/session attribution trailers.
- Open a PR against `main` using the repository template only after the implementation slice is coherent.
- PR body = stable semantic intent/rationale, compatibility impact, and material non-goals only.
- Keep local validation output, CI state, SHAs, ahead/behind state, mergeability and other transient lifecycle facts out of the PR body.
- Reconcile materially moved `main` before final review according to repository policy.
- Converge exact-head `CI / gate` to green before implementation handoff; independent `disposition` remains reviewer-owned.
- Do not merge the PR.
- Before independent review, remove every tracked `workspace/` file.

## Final implementation report

Report to the developer:

1. exact implementation branch/head;
2. where the test-only experiment package landed;
3. fixture/evidence/oracle shapes introduced;
4. starter fixtures added;
5. how supported evidence and partial/full windows are represented;
6. how run-specific identity is normalized for deterministic comparison;
7. focused tests run and results;
8. `./arcogine check` result;
9. current visible exact-head CI/gate state;
10. any deliberately deferred diagnostic/analytics/strategy fixtures;
11. any newly exposed semantic gap that should return to research rather than being solved in infrastructure;
12. confirmation that the transient prompt/workspace files were removed before review.
