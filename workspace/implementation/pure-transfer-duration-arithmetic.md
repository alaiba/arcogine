# Implementation handoff: pure transfer duration arithmetic

Repository: `alaiba/arcogine`
Starting branch (carries this prompt; implement here): `feature/transfer-duration-arithmetic`
Starting live-main baseline: `28dce205bc6d06ddad5bb4086b9a525219b92c0c`

## Objective

Add one small, deterministic calculation seam on the Engine interpretation side. From an already-published spatial record it computes the Manhattan distance between two distinct resources' reference cells and the transfer duration between them:

```text
manhattanDistance = abs(xDestination - xSource) + abs(yDestination - ySource)   (reference cells)
transferDuration  = handlingTicks + ticksPerCell * manhattanDistance            (distinct resources)
```

Worked example: a 3x3 footprint at (0,0), a 1x1 footprint at (3,2), `ticksPerCell` 2, `handlingTicks` 10. The reference-cell distance is 5, so the duration is 10 + 2*5 = 20 ticks.

Why it exists, for context only: the Factory model can already carry an optional spatial record, and the Engine specification already defines how a present record turns into transfer delays between operations, but the Engine refuses such a model before any runtime state exists. This slice supplies the arithmetic that a later activation slice will call. It changes nothing observable: nothing calls the seam, a present spatial record is still refused, and every existing test stays green unmodified. Landing it does not by itself unblock activation, which has other prerequisites outside this handoff.

Deliver one PR for this slice only.

## Grounding before implementation

Re-resolve live `main` and open PRs before making repository-state claims. The baseline above is an evidence coordinate, not a promise that `main` stays there; at handoff creation no PR was open. Everything this prompt says about code is a point-in-time observation at the baseline, so verify it against the code you edit, reading from the target revision rather than a possibly stale checkout. If `main` has moved, reconcile per `AGENTS.md` without rewriting handed-off history.

The shared local repository holds other sessions' branches and worktrees. Preserve them and work only on the branch named here. A stale earlier handoff, which combined this slice with a different one, sits on the local branch `feature/inbound-admission-reservation`; it is superseded and must not be used. Fetch this branch and inspect the worktree and branch state before editing, and preserve any unrelated work you find.

Read first:

1. `AGENTS.md`
2. `.github/CONTRIBUTING.md`
3. `docs/development/testing.md`
4. `docs/architecture/overview.md` (Determinism Contract)
5. `docs/architecture/engine-semantics.md`: sections 1 and 1.1 (what counts as result-affecting), 5 (model/Engine ownership boundary), 7 (transfer timing; the governing text), and 13 and 14 (what is deferred and what the conformance items cover)
6. `docs/architecture/factory-model.md`: the optional spatial record and its maximum-transfer-duration predicate
7. `docs/architecture/transfer-applicability.md`
8. `docs/planning/spatial-runtime-consequences.md`: sections 1 to 4, the slice titled "Pure transfer arithmetic", the slice titled "Vertical transfer activation" (to know the boundary you must not cross), and the dependency map. The plan gives these slices delivery labels; do not carry any plan label or coordinate into code, tests, comments or durable docs.
9. `docs/planning/factory-simulation-engine-readiness.md`: the spatial runtime section

Inspect in code (paths under `product/domains/factory/src/`):

- `main/java/com/arcogine/factory/model/spatial/`: `SpatialRecord`, `ResourceLayout`, `ResourcePlacement`, `FactoryFloor`, `ResourceFootprint`
- `main/java/com/arcogine/factory/model/validation/FactoryModelValidator.java` (the maximum-transfer-duration predicate and the error field it reports)
- `main/java/com/arcogine/factory/model/FactoryRuntimeAssembler.java` (the refusal of a present spatial record)
- `main/java/com/arcogine/factory/model/FactoryModelCanonicalForm.java` (the exact-build definition binding; see below)
- `main/java/com/arcogine/factory/process/FactoryHandler.java` (read-only: `handleTaskEnd` is where a later slice will call your code)
- tests: `test/java/com/arcogine/factory/model/spatial/SpatialRecordTest.java`, `test/java/com/arcogine/factory/model/validation/SpatialRecordValidationTest.java` (its private helpers show how a minimal valid model with a spatial record is built, and it already has maximum-duration boundary cases), and the existing tests under `test/java/com/arcogine/factory/process/` for style

Quick repository search terms: `Manhattan`, `ticksPerCell`, `handlingTicks`, `maxTransferDuration`, `ResourcePlacement`, `resourceLayouts`, `UnsupportedModelContentException`, `DEFINITION_CLASSES`.

## Current repository facts to preserve

- Factory owns the authored facts: the optional spatial record (floor, per-resource reference-cell position and footprint, `ticksPerCell`, `handlingTicks`). The Engine owns how they are interpreted. Section 7 is Engine interpretation, so the seam belongs on the Engine side, beside `FactoryHandler`, not on the model records.
- The records are `SpatialRecord(floor, ticksPerCell, handlingTicks, resourceLayouts)`, `ResourceLayout(resourceId, placement, footprint)`, `ResourcePlacement(x, y)`. Every magnitude is a `long`, and tick durations are `long`. The value constructors deliberately do not validate ranges; `FactoryModelValidator` does. A published record has exactly one layout per configured resource, non-negative coordinates, footprints inside the floor, and no overlapping footprints.
- The maximum-transfer-duration predicate: `maxManhattanDistance = (W - 1) + (H - 1)` and `maxTransferDuration = handlingTicks + ticksPerCell * maxManhattanDistance`, every step with exact arithmetic, reported as an error on the field `spatial.maxTransferDuration`. Passing it proves any derived duration for that record is representable. It does not prove that adding a duration to the current simulated time is, which stays the runtime's existing time-addition guard's job (section 7 rule 6).
- The current Engine refuses a published model whose spatial record is present, before any runtime state exists, and a model without a spatial record has no transfer lifecycle. Both stay exactly true. You cannot run a spatial model through the runtime to test the seam, and you must not try to bypass the refusal; call the seam directly with `SpatialRecord` values.
- The Factory exact-build definition binding hashes the compiled classes listed in the `DEFINITION_CLASSES` constant in `FactoryModelCanonicalForm`. Read that list in the source rather than trusting any summary; at the baseline it includes the model records (the spatial record types among them), the validator, the validation result, error and exception types, `MachineId`, `ProductId`, `ModelFingerprint`, and the version, canonical-form and artifact classes plus the artifact verifier's class. Changing any listed class changes the binding and therefore which Storage roots are accepted. Do not modify any of them.
- Determinism Contract: identical explicit inputs give identical results; no wall-clock time, no unseeded randomness, no hash-iteration order that can decide a result.
- Engine semantics section 1.1: an intentional change that can alter an outcome for identical explicit inputs is a definition change requiring the specification and its fixtures to change together. This slice is behavior-neutral for every input the Engine executes today. If you find yourself believing otherwise, stop and report.
- Product source and tests must not reference `docs/planning/`, `docs/research/` or `workspace/research/` (enforced by `.github/scripts/check-source-authority-links.mjs`). Comments may cite a durable authority, for example `docs/architecture/engine-semantics.md` with a section number. No plan coordinate or review label appears in code, comments, test names or durable docs.
- Source comments and Javadoc describe current behavior, invariants and limitations, not delivery sequencing. Say what the seam does and that nothing in the runtime calls it yet; do not say which slice will call it or what comes next. A recent sweep removed that kind of language from product source, so do not reintroduce it.

## Required slice

### Invariants (each proven by a direct test)

1. **Reference cells only.** Distance uses each resource's minimum-coordinate reference cell. Footprint extent never enters it, however large the footprints or close their edges.
2. **Handling once per transfer.** Not once per endpoint and not once per cell.
3. **Distinct resources only.** Consecutive operations on the same resource involve no transfer: no duration, no state, no events (section 7 rule 8). The seam must not return `handlingTicks`, or any value, for a same-resource request. Reject it so a caller that forgets to branch fails visibly.
4. **Zero magnitudes are legal.** `ticksPerCell = 0` and/or `handlingTicks = 0` give the arithmetic result, possibly 0, for distinct resources. That is a valid transfer duration, not "no transfer"; an absent spatial record is the only "no transfer" case.
5. **Exact and loud.** Use `Math`'s exact operations throughout: `subtractExact`, `absExact`, `addExact`, `multiplyExact`. Plain `Math.abs(Long.MIN_VALUE)` is still negative, so it is not acceptable. No saturation (only derived-result accumulators saturate; this does not), no wrapping, no floating point, no rounding. A record that violates the representability predicate is not publishable; if the seam is handed one anyway it throws `ArithmeticException` rather than producing a wrapped value.
6. **Published-record contract.** Coordinate range, footprint containment, non-overlap and layout coverage are publication predicates. Do not re-implement validation here.
7. **Unknown resources are rejected.** A `MachineId` that the record does not place is a caller error, never a silent zero. This applies to the source and to the destination.
8. **Pure.** No scheduler, handler, store, clock, randomness, or observable caching. Thread-safe by construction.
9. **Not a time operation.** Do not add the duration to a `SimTime` or guard time addition; that is out of scope.

### Placement and shape

- Put it on the Engine interpretation side (`com.arcogine.factory.process`), package-private, since the later consumer is `FactoryHandler` in the same package. Widen visibility only if a concrete need appears.
- Smallest shape: pure functions over placements plus a per-resource-pair function over `SpatialRecord`; or one small immutable value. For example, a package-private final class `TransferTiming` with `manhattanDistance(ResourcePlacement, ResourcePlacement)` and `transferDuration(SpatialRecord, MachineId, MachineId)`. Names and shape are yours. A linear scan of `resourceLayouts()` is fine; do not add caches or indexes a later slice has not asked for.
- Failure types: use explicit unchecked exceptions with informative messages. Value-contract violations in this codebase use `IllegalArgumentException` or `NullPointerException`; runtime state errors use `SimError` subclasses. A caller-contract violation in a pure function fits the former. Exact-arithmetic overflow surfaces as `ArithmeticException`.
- Do **not** add methods to `SpatialRecord`, `ResourceLayout`, or any other class in `DEFINITION_CLASSES`. That would change Storage's binding and blur the model/Engine ownership line. If you conclude the seam cannot be built without touching one, stop and report.

### Known traps

- **Same-resource handling.** Distance 0 would yield `handlingTicks` from the bare formula. That is wrong for same-resource continuation (invariant 3).
- **Edges and centres.** With a large footprint, distance between reference cells differs from nearest-cell distance and from centre-to-centre distance. Make the tests separate all three, so a wrong reading cannot pass by coincidence.
- **Distinct resources never have distance 0.** Footprints cannot overlap, so reference cells differ and the minimum distance is 1. Do not special-case 0.
- **Tautological tests.** Do not restate the production formula as the oracle. Use literal expected values and an independent derivation (for example `BigInteger`, or counting grid steps).
- **Randomness.** Any generated cases use fixed seeds or exhaustive enumeration. There is no property-testing library in this build; plain JUnit loops are fine.
- **Testing through the runtime.** The runtime refuses spatial models. Test the seam directly.
- **Conformance items.** Engine semantics section 14 items 7 to 11 and 13 accompany spatial execution. Your unit tests are not those fixtures; do not edit the Engine conformance suites and do not claim to satisfy those items.

### Evidence

Add a new test class beside the seam (same package, JUnit Jupiter, plain assertions as in the neighboring tests), named for behavior, not for a slice. Cover at least:

1. **Reference-cell distance.** Same row, same column, diagonal; both directions give the same distance and duration; a case with a large footprint that separates the three plausible readings, for example a 3x3 footprint at (0,0) and a 1x1 at (3,2), where the reference-cell distance is 5, nearest-cell distance would be 1 and centre-to-centre would be 3; changing only a footprint leaves distance and duration unchanged.
2. **Handling exactly once.** For example handling 10, `ticksPerCell` 1, distance 4 gives 14 (not 24, which would apply handling at both endpoints, and not 44, which would apply it per cell).
3. **Zero magnitudes.** `ticksPerCell` 0 gives handling regardless of distance; handling 0 gives `ticksPerCell` times distance; both zero gives 0 for distinct resources.
4. **Adjacent resources.** Adjacent 1x1 footprints give distance 1.
5. **Rejections.** A same-resource request, an unknown source and an unknown destination are each rejected, with no value returned.
6. **Overflow consistency with the publication predicate.** On a 2x2 floor with two 1x1 resources at opposite corners, (0,0) and (1,1), take handling 1 and `ticksPerCell` equal to `(Long.MAX_VALUE - 1) / 2`: the predicate's maximum equals `Long.MAX_VALUE`, the validator accepts the model, and the seam returns exactly `Long.MAX_VALUE`. With handling 2 (built directly, bypassing publication) the seam throws `ArithmeticException` and the validator reports `spatial.maxTransferDuration` for the same record. Also show the seam fails loudly, never wrapping or going negative, when distance itself would overflow on an unvalidated record (for example coordinates at opposite ends of the `long` range).
7. **Enumerated oracle.** Check every ordered pair of distinct placements on a small floor (for example 4x4 with 1x1 footprints) against an independent derivation, for several magnitude combinations including zeros, and assert each result never exceeds that record's predicate maximum.

The new class must be fully exercised by these tests: the module's coverage gate requires at least 90% line coverage across the module, and branch coverage is reported, so every refusal branch needs a test.

## Scope discipline and non-goals

This slice must not:

- change Engine scheduling, dispatch, ranking, timing, workload decomposition, supported events or supported observations;
- change Factory canonicalization, fingerprints, publication validity, or any class in `DEFINITION_CLASSES`;
- make a present spatial record executable, add `TRANSFERRING` or any transfer event, state or observation field, or choose a destination;
- add destination reservation or any `Machine` change (a separate slice), transport resources, paths, congestion, rerouting, or orientation;
- add a runtime-provenance field, an outward adapter, or an analytics or KPI change;
- touch Governance, Storage, Challenge, Finance or the research experiment module.

If implementation evidence conflicts with the Factory model, the Determinism Contract or the Engine specification, stop and surface the contradiction. Do not invent semantics in product code.

## Documentation and semantic closure

- Reconcile `docs/planning/spatial-runtime-consequences.md`: the arithmetic slice's status becomes implemented, with a short factual description and the name of its evidence test class, in the style of the neighboring implemented slices; update the top-of-file status paragraph (it currently says the transfer slices are unimplemented) and annotate the slice as implemented in the dependency map.
- Reconcile the admitted-sequence list in `docs/planning/factory-simulation-engine-readiness.md`: the step covering "deterministic transfer arithmetic and inbound admission reservation" becomes arithmetic complete with admission reservation remaining, in the style of the neighboring "complete; ... remains" step.
- Do not edit the Engine specification or the Factory model unless an implementation contradiction forces a correction, and then only after stopping to report it.
- Plan coordinates may appear only inside `docs/planning/`. Durable docs, code, comments and test names use semantic vocabulary.
- Semantic closure: search maintained docs and planning for stale claims about "not started", "unimplemented" and "transfer arithmetic", and reconcile them in the same change. Keep it to that neighborhood. A seam that nothing calls needs no README or concepts change unless a current statement becomes false.
- This prompt is transient delivery scaffolding. Remove it from this branch's final tree before independent review.

## Validation

Narrow loop while working (from `product/`):

```bash
./gradlew :factory:test --tests 'com.arcogine.factory.process.*'
./gradlew :factory:test --tests 'com.arcogine.factory.model.*'
```

Then the module and gates: `./gradlew :factory:test checkstyleMain checkstyleTest jacocoTestCoverageVerification` (the coverage verification runs every Java test suite, per `docs/development/testing.md`; `node .github/scripts/report-java-coverage.mjs` prints the line and branch table afterwards). Compilation uses `-Xlint:all -Werror`, so fix warnings rather than suppressing them.

Before implementation handoff run `./arcogine check`. Run `./arcogine check --full` when the tools are available; do not claim a skipped or unavailable check passed. When docs changed, also run `node .github/scripts/check-markdown-links.mjs`, `check-delivery-labels.mjs`, `check-transient-coordinates.mjs` and `check-source-authority-links.mjs`. After removing this prompt and any other workspace artifact, run `node .github/scripts/check-transient-workspace.mjs`.

Environment (see `AGENTS.md` "Canonical commands" and `docs/development/testing.md`): `./arcogine` is a Bash script. On a Windows host prefer, in order, the devcontainer, a generic Docker container, WSL/Git Bash, then native tooling. Inspect running Docker containers first and find the one that mounts this repository; do not assume a name. JDK 21 is the supported floor. Cautions learned on this host:

- The owner's host `JAVA_HOME` has been seen pointing at an old JDK; verify `java -version` or use Docker.
- In some agent shells on this host (seen with Claude Code's Bash tool) `git`, `gh`, `docker` and `node` are not on `PATH`; run them through PowerShell.
- For the JDK 21 floor in Docker, export the commit with `git archive` into a scratch directory and mount that copy, with `--no-daemon --no-build-cache`. Do not pipe the tar through PowerShell, and delete the scratch copy afterwards. Bind-mounting this worktree's real `product/` into a root container leaves root-owned `build/` files that break the devcontainer's Gradle.
- Do not alternate host Gradle and container Gradle against the same `product/**/build/` directories. Delete the gitignored build directories (confirm with `git check-ignore`) and rerun in one environment.
- Do not set `GIT_DIR` or `GIT_WORK_TREE` overrides to run repository self-tests inside a container against a Windows-host worktree; use a scratch clone instead.
- Confirm tests really executed (per-test PASSED lines), not just `BUILD SUCCESSFUL` or `UP-TO-DATE`.
- A shell whose working directory is inside a `build/` directory locks it on Windows.

## PR and lifecycle requirements

- Verify the human Git identity before committing; the repository's pre-commit check enforces it. Do not bypass hooks. Commit messages are concise, with no bot or session attribution trailers.
- Open the PR against `main` with the repository template when the slice is coherent. Do not rewrite the history of this prompt's commit or force-push it away; remove the prompt in a normal commit before independent review.
- The PR body is stable semantic intent only: summary and rationale, compatibility or migration impact (none expected), and material non-goals. No validation section, local results, CI state, SHAs, ahead/behind counts, base freshness or mergeability. No generated-by footer.
- You own exact-head CI convergence: wait synchronously for `CI / gate` on the exact head and fix implementation-owned failures. Ensure the head contains live `main` and GitHub reports it mergeable. The expected state at handoff is gate green with the independent `disposition` as the only unresolved merge gate; do not wait for or manufacture it. Never merge.
- Reconcile a materially moved `main` before final review using the repository's history-preserving approach, then review the net diff against current `main`.

## Final implementation report

Report to the developer:

1. exact implementation branch, head SHA, and PR link;
2. what changed and where (files and the shape you chose), and why;
3. the tests added and the focused commands run, with outcomes;
4. `./arcogine check` result, other validation run, and anything unavailable or skipped;
5. the terminal exact-head `CI / gate` result;
6. the planning and documentation reconciliation made;
7. any contradiction found in the Engine specification, Factory model or plan;
8. confirmation that this prompt and every other tracked `workspace/` file were removed before review, and that `check-transient-workspace.mjs` passes on this branch;
9. anything deliberately deferred, and confirmation that unrelated local work and other sessions' branches were left untouched.
