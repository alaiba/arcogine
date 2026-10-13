# Implementation handoff: machine inbound admission reservation

Repository: `alaiba/arcogine`
Starting branch (carries this prompt; implement here): `feature/machine-inbound-reservation`
Starting live-main baseline: `28dce205bc6d06ddad5bb4086b9a525219b92c0c`

## Objective

Give `Machine` the minimum internal bookkeeping needed to reserve inbound admission capacity for a job that is in transit to it, so that a later activation slice can bind a destination without that job being active, queued, or making the machine `Busy`. Concretely:

1. a reservation counts against the machine's admission capacity: `canAcceptJob()` and `startJob` treat a reserved slot as taken;
2. a reserved job is neither active nor queued work, and it changes nothing the machine reports about itself (`activeJobs()`, `queuedJobs()`, `queueDepth()`, `state()`);
3. a reservation ends in exactly one of two deterministic ways: converted into active processing (the destination is online at arrival) or into the tail of the machine's own FIFO queue (the destination is offline at arrival).

Worked example: a concurrency-1 machine M2 with job J1 in transit to it holds one reservation. M2 reports `Idle`, no active jobs and queue depth 0, yet `canAcceptJob()` is false, so J2 selecting M2 must wait rather than take J1's seat. M2 may be taken offline while it holds only the reservation. When J1 arrives: if M2 is online, the reservation becomes J1's active processing slot (M2 is `Busy`); if M2 is offline, J1 joins the tail of M2's own queue, to be started by the existing recovery logic when M2 returns.

Why it exists, for context only: the Factory model can already carry an optional spatial record, and the Engine specification already defines how a present record produces transfers between operations, including this reservation. The Engine still refuses such a model before any runtime state exists. This slice supplies the capacity bookkeeping that a later activation slice will use. It changes nothing observable: nothing calls the new code, a present spatial record is still refused, and every existing test stays green unmodified. Landing it does not by itself unblock activation, which has other prerequisites outside this handoff.

Deliver one PR for this slice only.

## Concurrent work

A sibling slice, exact transfer duration arithmetic, is being implemented in another session on the local branch `feature/transfer-duration-arithmetic`. It adds a new class and test in `com.arcogine.factory.process` and updates the same two planning documents you will. You share no source files with it.

- Do not read, modify or depend on its branch or worktree.
- Expect textual conflicts only on the lines both slices update in the planning docs: the top status paragraph of `docs/planning/spatial-runtime-consequences.md` and the admitted-sequence step covering arithmetic and reservation in `docs/planning/factory-simulation-engine-readiness.md`. If the sibling has landed when you open your PR or merge `main`, state both as complete. If you land first, mark only reservation complete and leave arithmetic remaining. Resolve your own hunks only.
- Both sessions may run Gradle on this machine at the same time. Use your own worktree's build directories; builds will be slower, not broken.

## Grounding before implementation

Re-resolve live `main` and open PRs before making repository-state claims. The baseline above is an evidence coordinate, not a promise that `main` stays there; at handoff creation no PR was open. Everything this prompt says about code is a point-in-time observation at the baseline, so verify it against the code you edit, reading from the target revision rather than a possibly stale checkout. If `main` has moved, reconcile per `AGENTS.md` without rewriting handed-off history.

The shared local repository holds other sessions' branches and worktrees. Preserve them and work only on the branch named here. An earlier handoff that combined this slice with the arithmetic slice sits on the local branch `feature/inbound-admission-reservation`; it is stale (it predates the removal of the busy-time counter and misstates the exact-build binding list) and must not be used. Fetch this branch and inspect the worktree and branch state before editing, and preserve any unrelated work you find.

Read first:

1. `AGENTS.md`
2. `.github/CONTRIBUTING.md`
3. `docs/development/testing.md`
4. `docs/architecture/overview.md` (Determinism Contract)
5. `docs/architecture/engine-semantics.md`: sections 1 and 1.1 (what counts as result-affecting), 2 (rule 10 especially), 6 (rules 4, 5 and 9), 8 (state machine), 9 (rules 2, 4, 6 and 8; the governing text), 10 and 11 (what observations will later have to show), and 13 and 14
6. `docs/architecture/transfer-applicability.md`
7. `docs/planning/spatial-runtime-consequences.md`: sections 1 to 4, the slice titled "Destination admission-reservation substrate", the slice titled "Vertical transfer activation" (including its pre-activation recheck, which is not yours), and the dependency map. The plan gives these slices delivery labels; do not carry any plan label or coordinate into code, tests, comments or durable docs.
8. `docs/planning/factory-simulation-engine-readiness.md`: the spatial runtime section

Inspect in code (paths under `product/`):

- `domains/factory/src/main/java/com/arcogine/factory/machines/`: `Machine`, `MachineView`, `MachineStore`
- `domains/factory/src/main/java/com/arcogine/factory/process/FactoryHandler.java` (read-only: every call to `canAcceptJob`, `startJob`, `enqueueJob`, `dequeueJob`, `completeJob`, `setAvailability`, and the `selectMachine` ranking comparator)
- `domains/factory/src/main/java/com/arcogine/factory/process/FactoryRuntime.java` (read-only: `setMachineAvailability` and its command-level checks)
- `domains/factory/src/main/java/com/arcogine/factory/process/ResourceObservation.java` (read-only)
- `domains/factory/src/main/java/com/arcogine/factory/model/FactoryRuntimeAssembler.java` (the refusal of a present spatial record)
- `types/src/main/java/com/arcogine/types/`: `SimError`, `MachineState`
- tests: `domains/factory/src/test/java/com/arcogine/factory/machines/MachineStateTest.java` (the style to follow), `.../process/EngineDispatchConformanceTest.java`, `.../process/EngineDerivedResultConformanceTest.java`, `.../process/FactoryRuntimeExecutabilityAcceptanceTest.java`, `.../process/SessionControlAcceptanceTest.java`
- `architecture-conformance-test/src/test/java/com/arcogine/architecture/ArchitectureTest.java`

Quick repository search terms: `canAcceptJob`, `activeJobs`, `queueDepth`, `startJob`, `enqueueJob`, `setAvailability`, `admission`, `only_factory_may_mutate_machine_state`, `UnsupportedModelContentException`, `DEFINITION_CLASSES`.

## Current repository facts to preserve

- `Machine` is Factory-side runtime state, mutated only by `FactoryHandler`. Reservation is runtime bookkeeping, not a new public resource abstraction (Engine semantics section 9 rule 4).
- `Machine` today: `canAcceptJob()` is "online and active jobs below concurrency"; `startJob` refuses an offline machine and a machine at concurrency, with messages containing "offline" and "concurrency" that existing tests assert; `completeJob` removes an active job and returns the machine to `Idle` when none remain; the machine's own queue is strict FIFO in arrival order; `setAvailability(false)` is refused only while active jobs exist; going online restores `Idle` from `Offline`. Capacity is expressed through `activeJobs` and `concurrency`.
- Every acceptance decision in the runtime goes through `Machine.canAcceptJob()` and `startJob`: the `selectMachine` ranking key, immediate placement, queue dispatch, shared-backlog dispatch and the next-step path. Counting reservations inside `Machine` therefore makes all of them reservation-aware at once, so you do not touch `FactoryHandler`.
- `FactoryRuntime.setMachineAvailability` rejects "offline" only when `MachineView.activeJobs()` is non-empty. Leave it alone. Because a reservation is not an active job, a machine holding only reservations can be taken offline at both layers.
- `MachineView` is read-only and excludes every mutator. `ResourceObservation` (machine id, name, state, concurrency, active job ids, queue depth, capacity liters, setup time) is derived from it. Both stay exactly as they are: the observation field for admission load is a later slice's decision.
- The current Engine refuses a published model whose spatial record is present, before any runtime state exists (`FactoryRuntimeAssembler`), and a model without a spatial record has no transfer lifecycle. Both stay exactly true. There is no way to exercise reservation through the runtime, and you must not add one; tests call `Machine` directly.
- `Machine` is not part of the Factory exact-build definition binding (the `DEFINITION_CLASSES` constant in `FactoryModelCanonicalForm`; read it in the source). Do not modify any Factory model class either.
- Determinism Contract: identical explicit inputs give identical results; no wall-clock time, no unseeded randomness, no hash-iteration order that can decide a result.
- Engine semantics section 1.1: an intentional change that can alter an outcome for identical explicit inputs is a definition change requiring the specification and its fixtures to change together. This slice is behavior-neutral for every input the Engine executes today. If you find yourself believing otherwise, stop and report.
- Product source and tests must not reference `docs/planning/`, `docs/research/` or `workspace/research/` (enforced by `.github/scripts/check-source-authority-links.mjs`). Comments may cite a durable authority, for example `docs/architecture/engine-semantics.md` with a section number. No plan coordinate or review label appears in code, comments, test names or durable docs.
- Source comments and Javadoc describe current behavior, invariants and limitations, not delivery sequencing. Say what the new code does and that nothing in the runtime calls it yet; do not say which slice will call it or what comes next. A recent sweep removed that kind of language from product source, so do not reintroduce it.

## Required slice

### Invariants (each proven by a direct test)

1. **Admission load is bounded.** Admission load is active jobs plus inbound reservations, and never exceeds `concurrency`. `canAcceptJob()` is true only when the machine is online and its admission load is below concurrency. `startJob` refuses when the load is at concurrency, so an ordinary start can never take a reserved slot even if a caller skipped `canAcceptJob()`. With no reservations, behavior and messages are identical to today; keep the existing messages byte-for-byte on every path the existing tests exercise.
2. **A reservation is not work.** A reserved job is not in `activeJobs()`, not in `queuedJobs()`, not counted by `queueDepth()`, and does not change `state()`: a machine holding only reservations stays `Idle`, and `Busy` still arises only from real active jobs.
3. **Reserve only where admissible.** A reservation can be made only when `canAcceptJob()` is true. Binding is defined only for a currently admissible destination (Engine semantics section 2 rule 10 and section 6 rule 4). Reserving on an offline or full machine is refused.
4. **Offline behavior.** `setAvailability(false)` still refuses only for active jobs, so a machine holding only reservations can go offline. Its reservations survive going offline and coming back online unchanged. While offline it cannot accept and cannot take a new reservation.
5. **Conversion to processing (arrival at an online destination).** One atomic operation releases the job's reservation and starts it. It must not be refused because of the job's own reservation, it is capacity-neutral, and afterwards the machine looks exactly as if the job had been started normally (the job is active, `state()` is `Busy`). It is refused if the machine is offline.
6. **Conversion to the destination's own queue (arrival at an offline destination).** One atomic operation releases the reservation and appends the job at the tail of the machine's own FIFO queue. The job is then counted once: queue depth plus one, reservation minus one, never both. Order is arrival order, not `JobId` order. It is refused when the machine is online: the specification sends only the offline-arrival case to a queue.
7. **Misuse fails loudly and atomically.** A duplicate reservation, conversion of a job that holds no reservation, conversion using another job's identity, conversion in the wrong availability state, and reserving beyond capacity or while offline each fail with `SimError.InvalidStateTransition` (the existing convention; give the context text the same informative style, naming the machine and the problem) and leave the machine exactly as it was.
8. **Deterministic.** No hash-iteration order can be observable. Use an insertion-ordered structure if you keep per-job state.
9. **Invisible.** `MachineView`, `ResourceObservation`, runtime events and DTOs are unchanged, and no new public type or aggregate is introduced.

### Design latitude

- Representation is yours. A job-identified, insertion-ordered collection is recommended because it makes invariant 7 checkable: a bare count cannot detect converting the wrong job's reservation. If you choose a count, justify it and show how invariant 7 is still met.
- Method names and the read accessor used by tests are yours. Expose reads on `Machine` only, not on `MachineView`.
- Provide exactly the two exits the specification defines. Do not add a bare "release" unless a concrete path in the specification needs one.
- Keep the `Machine` constructor signature unchanged; the runtime assembler and tests use it.

### Known traps

- **Self-refusal.** `startJob` compares load with concurrency. Once reservations count, converting a reserved job must not be refused by that job's own reservation, and invariant 1 must hold at every intermediate step and on every exception path. Do the conversion as one operation, not as "release, then call `startJob`".
- **Online queue conversion.** Allowing the queue conversion on an online machine would manufacture "online machine with free processing capacity plus residual local work", the very state the later activation slice must prove it never creates. Keep it offline-only.
- **The architecture guardrail has a fixed list.** `only_factory_may_mutate_machine_state` names `Machine`'s mutators one by one (currently `startJob`, `completeJob`, `enqueueJob`, `dequeueJob`, `setAvailability`), so every new public mutator escapes it until added. Extend the rule with exact signatures for each new mutator. A mistyped name or parameter type could leave the guard silently ineffective, so verify each new entry by observation rather than assuming it works. No production module outside Factory can see `Machine` today, but the non-shipped `research-experiments` module depends on the factory module and is inside the rule's scan: for each new mutator, temporarily add a call to it from a main class there, confirm the rule fails, then remove the call and do not commit it. Say how you verified in the final report. Keep the extension minimal; generalizing the rule is optional and must not weaken it.
- **`MachineView` documentation.** Its Javadoc enumerates the mutators it excludes. Keep it truthful for the new ones, in terms of current behavior.
- **Do not expose reservations through observation.** Adding an admission-load field now hardens a supported contract prematurely.
- **Do not change `state()` derivation, `completeJob`, or the queue.**
- **Do not make it reachable.** No production caller, no flag, no configuration. Only tests call the new mutators.
- **Do not perform the pre-activation recheck.** The later activation slice must prove that reservation-aware admission creates no new healthy state that violates the retained dispatch-conformance assumptions. That is its obligation. Record any structural observation relevant to it in your final report; do not write that analysis into durable docs.

### Evidence

Add a new test class in `domains/factory/src/test/java/com/arcogine/factory/machines/` (follow `MachineStateTest`: JUnit Jupiter, same package, `SimError.InvalidStateTransition` assertions with `context()` checks), named for behavior, not for a slice. Cover at least:

1. **Capacity admission.** Concurrency 1: one reservation makes `canAcceptJob()` false, refuses an ordinary `startJob`, refuses a second reservation. Concurrency 2: one active plus one reserved is full; reserved only still accepts exactly one more; two reserved is full. A reservation never lets total load exceed concurrency.
2. **Active and queue separation.** With reservations present, `activeJobs()`, `queuedJobs()`, `queueDepth()` and `state()` are exactly what they would be without them. A reservations-only machine is `Idle`; a machine with one real active job and a reservation is `Busy` only because of the active job.
3. **Offline.** A reservations-only machine goes offline (contrast: a machine with an active job still refuses). Reservations persist across offline and online. While offline it cannot accept and cannot take a new reservation.
4. **Conversion to processing.** On a fully reserved concurrency-1 machine, converting succeeds (not refused as "at max concurrency"), leaves the job active and no reservation, load unchanged, state `Busy`; completing the job returns the machine to `Idle` and capacity frees normally. Also a concurrency-2 case with one active job and one reservation. Refused on an offline machine with the reservation intact.
5. **Conversion to queue.** On an offline machine with existing queued work, converting appends at the tail: queue depth plus one, reservation minus one, job present exactly once. Use an arrival order that differs from `JobId` order (for example an existing entry with a higher id) so FIFO is actually discriminated. Refused on an online machine with state unchanged.
6. **Misuse.** Duplicate reservation; conversion for a job with no reservation; conversion using another job's identity; each fails as specified and leaves the machine exactly as it was.
7. **Determinism.** A scripted sequence of reservations, conversions, offline and online toggles produces identical observable machine state across repeated fresh machines.
8. **No regression.** `MachineStateTest` and the dispatch-conformance, derived-result, session-control and runtime acceptance suites pass unmodified. If an existing test fails, fix your change, not the test.
9. **Guardrail.** The extended architecture rule covers every new mutator (the existing rule's shape is the model).

The new code must be fully exercised: the module's coverage gate requires at least 90% line coverage across the module, and branch coverage is reported, so every refusal branch needs a test.

## Scope discipline and non-goals

This slice must not:

- change Engine scheduling, dispatch, ranking, timing, workload decomposition, supported events or supported observations, or `FactoryHandler`, `FactoryRuntime` and the assembler in any way;
- change Factory canonicalization, fingerprints, publication validity, or any Factory model class;
- make a present spatial record executable, add `TRANSFERRING` or any transfer event, state or observation field, compute a transfer duration, or choose a destination;
- add transport resources, paths, congestion, rerouting, or orientation;
- add a public reservation aggregate, a runtime-provenance field, an outward adapter, or an analytics or KPI change;
- touch Governance, Storage, Challenge, Finance or the research experiment module.

If implementation evidence conflicts with the Factory model, the Determinism Contract or the Engine specification, stop and surface the contradiction. Do not invent semantics in product code.

## Documentation and semantic closure

- Reconcile `docs/planning/spatial-runtime-consequences.md`: the reservation slice's status becomes implemented, with a short factual description and the names of its evidence test class and the extended architecture rule, in the style of the neighboring implemented slices; keep the statement that nothing reaches it until activation; update the top-of-file status paragraph (it currently says the transfer slices are unimplemented) and annotate the slice as implemented in the dependency map.
- Reconcile the admitted-sequence step in `docs/planning/factory-simulation-engine-readiness.md` that covers "deterministic transfer arithmetic and inbound admission reservation", in the style of the neighboring "complete; ... remains" step, according to which slice has landed (see Concurrent work).
- Do not edit the Engine specification or the Factory model unless an implementation contradiction forces a correction, and then only after stopping to report it.
- Plan coordinates may appear only inside `docs/planning/`. Durable docs, code, comments and test names use semantic vocabulary.
- Semantic closure: search maintained docs and planning for stale claims about "not started", "unimplemented", "admission reservation" and "reserved", and reconcile them in the same change. Keep it to that neighborhood. A substrate that nothing calls needs no README or concepts change unless a current statement becomes false.
- This prompt is transient delivery scaffolding. Remove it from this branch's final tree before independent review.

## Validation

Narrow loop while working (from `product/`):

```bash
./gradlew :factory:test --tests 'com.arcogine.factory.machines.*'
./gradlew :factory:test --tests 'com.arcogine.factory.process.*'
./gradlew :architecture-conformance-test:test
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
- Reconcile a materially moved `main` (including a landed sibling slice) before final review using the repository's history-preserving approach, then review the net diff against current `main`.

## Final implementation report

Report to the developer:

1. exact implementation branch, head SHA, and PR link;
2. what changed and where (files and the representation you chose), and why;
3. the tests added and the focused commands run, with outcomes;
4. the extended architecture rule, how you verified each new entry is effective, and the result of a search showing no production caller of the new mutators;
5. `./arcogine check` result, other validation run, and anything unavailable or skipped;
6. the terminal exact-head `CI / gate` result;
7. the planning and documentation reconciliation made, and how the sibling slice's state was handled;
8. any contradiction found in the Engine specification, Factory model or plan, and any structural observation relevant to the later pre-activation recheck;
9. confirmation that this prompt and every other tracked `workspace/` file were removed before review, and that `check-transient-workspace.mjs` passes on this branch;
10. anything deliberately deferred, and confirmation that unrelated local work and other sessions' branches were left untouched.
