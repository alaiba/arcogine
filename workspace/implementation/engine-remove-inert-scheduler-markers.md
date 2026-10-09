# Implementation handoff — remove inert Factory scheduler markers (R-remove)

## Role, repository, and delivery vehicle

Act as a **coding/implementation agent**, not as a Researcher or the independent PR Reviewer.
Follow the live repository's `AGENTS.md`, `.github/CONTRIBUTING.md`,
`docs/development/reviewing.md`, and `docs/development/testing.md`.

- Repository: `alaiba/arcogine`
- **Implementation branch:** `fix/engine-remove-inert-markers`
- This branch was created from live `main` at
  `b9902d407e048f731e31e62e85726ed3b9ddbbff` when the handoff was prepared.
  Resolve `main` again at implementation start; treat that SHA as historical grounding,
  not an assumption that the base is still current.
- Handoff path: `workspace/implementation/engine-remove-inert-scheduler-markers.md`.
  This file is **transient**. Remove it, and any other tracked `workspace/` material,
  from the **final PR head** before independent review/merge.
- **Do not work on, merge, rewrite, rebase, delete or force-update**
  `workspace/simulation-execution-account`. It holds immutable research evidence,
  not implementation source. Read exact research revisions by commit SHA and path,
  without merging them into this branch.
- One change, one implementation PR against `main`, from this branch only.
  Do not open a PR until a coherent implementation, normative reconciliation and
  executable acceptance evidence exist. Do **not** merge the PR yourself.

This is an **explicit Engine-interpretation owner decision**:
**choose R-remove and correct the Factory runtime behavior now** as a bounded
specification-completeness defect correction. It is not a new research spike and not
permission to implement E-guard, D-clock, interval sealing or a live Game loop.

## Immutable research evidence to consult, not merge

Read the *full* relevant findings and specific proving cases from these exact
revisions before modifying Engine semantics:

1. Follow-up adaptation report, commit
   `9551c0cee42bfdeab07f4a55f931eb10ec5fba4e`:
   `workspace/research/investigations/simulation-execution-account-engine-adaptation-report.md`.
   In particular §8 (D2), §12, alternative `E-guard` and `R-remove` discussion.
2. **Independent adaptation adversarial review**, commit
   `e3a4f9a223b982b341a71c2acf7b74146620e9f2`:
   `workspace/research/investigations/simulation-execution-account-engine-adaptation-adversarial-review.md`.
   In particular §3 findings A, §4 findings B, §8 qualifications and conformance requirements.
   Disposition: `ACCEPT WITH QUALIFICATIONS`. Preserve its qualifications rather than
   treating the original report's preferred E-guard as adopted architecture.
3. The review's executed adversarial probes, commit
   `fe3848ed3cc6c8a9aee3d60248a02b680cef6878`:
   `workspace/research/experiments/simulation-execution-account-engine-adaptation-review/com/arcogine/research/executionaccount/adaptationreview/EngineAdaptationReviewProbeTest.java`
   (R1 and R2 in particular, plus R4 for consumer consequence).
4. Original adaptation discriminators, commit
   `f74c4ad7d9e0aa54743a17bf575699a0a5eeab92`:
   `workspace/research/experiments/simulation-execution-account-engine-adaptation/com/arcogine/research/executionaccount/adaptation/EngineAdaptationDiscriminatorTest.java`
   (D2; inspect its precise expectations and setup).
5. Earlier execution-account report
   `a53778145f3ee9941a4b46c7f04f1ec17e6bff86`
   and its independent review
   `57d87fc4c8ba19927bf50efa4097321208811d5a`,
   both at their corresponding
   `workspace/research/investigations/simulation-execution-account-*.md` paths.
   Consult only for boundaries touched by this change. Do **not** reopen E1–E8 or
   implement their broader reconciliation here.

These commits are evidence inputs, not current architecture. The *landed* authority is
the live `docs/architecture/engine-semantics.md`, `docs/architecture/runtime-contract.md`,
and actual code/tests at the freshly resolved `main`.

## Grounding and quick semantic-neighbor search

Before editing, read live `AGENTS.md` and inspect:

- `docs/architecture/overview.md` — present Engine/Factory owner split and
  semantic-evolution/support policy;
- `docs/architecture/engine-semantics.md` §§1.1, 1.2, 4, 14;
- `docs/architecture/runtime-contract.md` — internal `Event` versus supported
  `RuntimeEvent`, post-authoritative publication, event ordering and observation cursor;
- `docs/planning/factory-simulation-engine-readiness.md` — scope of current Engine
  conformance, the separate performance slice, and the explicit research/implementation
  boundary for **new** session capabilities;
- `docs/research/investigations/engine-evolution.md` session/advancement note
  as adjacent context, not as license to widen this PR;
- `product/domains/factory/src/main/java/com/arcogine/factory/process/FactoryHandler.java`
  (all scheduling paths and terminal order completion);
- `.../FactoryRuntime.java` (`advance`, `advanceUntil`,
  `recordSupportedEventsFor`, `submitWorkload`,
  `setMachineAvailability`, `drainSupportedEvents`, `observe`, reset);
- `.../RecordingScheduler.java` and `product/simulation/src/main/java/com/arcogine/core/queue/Scheduler.java`;
- `product/simulation/src/main/java/com/arcogine/core/event/EventPayload.java`;
- `product/domains/finance/src/main/java/com/arcogine/finance/process/FinanceHandler.java`
  (uses the internal `OrderCompleted` payload, even though today's
  `FactoryRuntime` does not wire this handler);
- `SessionControlAcceptanceTest`, `HeadlessClosureAcceptanceTest`,
  `RuntimeEventDeliveryAcceptanceTest`, `RuntimeObservationAcceptanceTest`,
  `EngineDispatchConformanceTest`, `FactoryHandlerTest`, scheduler tests
  and any relevant Finance tests.

Run a focused search across `docs/`, implementation and tests for
`TaskStart`, `TaskEnd`, `OrderCompleted`, `maxEvents`, `advanceUntil`,
`advance()`, `scheduledEvents`, `hasPendingAuthoritativeWork`,
`observedTime`, `currentTime`, `QUIESCENT`, and marker-presence assumptions.
Search findings must be confirmed in exact current files, not inferred from snippets.
Check actual internal `Event` consumers before removing scheduling.

## Decision and acceptance contract

**Fix both independently demonstrated effects of semantically inert queued markers:**

- **Hidden command-time movement (D2):** a budgeted call processing only a
  `TaskStart` marker must not advance the clock at which the next command applies
  without any corresponding authoritative supported progress.
- **Hidden advancement-budget consumption (R1):** a queued trailing
  `OrderCompleted` marker must not consume a step so that two otherwise equivalent
  supported states advance differently solely because of this inert event.

**Selected remedy: R-remove.** For the currently supported `FactoryRuntime`
session, stop scheduling these inert markers on the reachable dispatch/completion
paths; derive supported `ORDER_COMPLETED` from the **authoritative order-completion
transition** instead of from an additional scheduled marker.

The intended post-change semantics:

1. `FactoryRuntime.advance()` continues to process and return **one pending
   scheduler event**, not a batch or newly defined authoritative turn.
   `advanceUntil(targetTime, maxEvents)` remains a loop over that operation,
   respecting the inclusive event-time guard, `maxEvents` count, processing order,
   zero-budget behavior and its actual-processed-event return list.
   Do not silently turn `maxEvents` into authoritative-turn counting.
2. The Factory-owned session's scheduler should not carry the two inert markers
   merely to serve bookkeeping or supported-event derivation. Every remaining
   scheduled event in this current Factory session should have a valid semantic
   reason to be scheduled; prevent unnoticed reintroduction of no-op budget
   consumers.
3. A `TaskEnd` still authoritatively completes exactly the appropriate step(s),
   updates resource placement/busy ticks, may complete an order, and may trigger
   deterministic same-time recovery/dispatch. Supported
   `JOB_STEP_COMPLETED`, `ORDER_COMPLETED`, and placement deltas must still
   be emitted **after the authoritative state transition**, with the correct
   run-scoped monotonic sequence and time; preserve their relative order and
   payloads (job/order identifiers, product, quantity, price, affected refs).
4. An order-completion event is emitted **once per genuine final order
   completion**, never from a mere no-op, partial child completion, rejected
   command, or marker-shaped stand-in. Preserve multi-job / multi-step orders,
   terminal states, and deterministic equal-time semantics.
5. Fault-after-partial-mutation behavior must remain truthful:
   `FactoryRuntime.advance()` currently publishes in a `finally` path.
   If the `TaskEnd` completed a job/order before a later dispatch cascade
   fails, supported events for mutations that **did** occur still appear;
   no success event describes a transition that did **not** occur.
   Derive the completion fact from the authoritative transition (for example
   a precise before/after condition or a narrowly scoped internal outcome),
   not from a scheduled event whose existence is about to be removed.
   Choose the smallest coherent design that preserves fault semantics and
   cannot emit duplicates; avoid an unbounded event-history facility.
6. Preserve `RunId`, model provenance, accepted/rejected/faulted
   `CommandResult` meanings, empty scheduled-event lists on rejection,
   supported observation/cursor consistency and reset reproducibility.
   `CommandResult.scheduledEvents()` and internal
   `advance()/advanceUntil()` returned `Event` streams may **intentionally
   change** because redundant events disappear. Audit/update dependent assertions
   and compatibility prose rather than representing that as a no-op refactor.
7. `EventPayload.TaskStart` and `EventPayload.OrderCompleted` may still
   exist in the generic simulation vocabulary and standalone Finance tests:
   **do not delete payload types or break `FinanceHandler` silently.**
   Determine whether its internal-event path is a currently required composition.
   If it is needed, preserve its intended domain fact using a narrow supported
   producer mechanism or explicitly surface a blocker for owner resolution;
   do not sneak the no-op markers back into the supported Factory stepping path.
   No new Finance subsystem, domain-wide event bus or broad handler API rewrite.
8. For reachable current Factory scenarios where formerly inert markers caused
   the discrepancy, after successful bounded progression the next command's
   effective time must not silently exceed the supported observation time solely
   because a marker was processed. Derive the invariant from the adopted
   Factory scheduling semantics; avoid declaring a universal future guarantee
   for arbitrary unimplemented internal event types.

This is **deliberately result-affecting** for some budget-stopped interleavings.
Same complete runs under the same start-fixed inputs should retain supported
production outcomes, but scripts that previously stopped on markers can now
reach authoritative work earlier and may legitimately change command timing.
Document this interpretation change clearly.

## Mandatory retained conformance evidence (not research-only)

Translate the proving intent from the research-local tests into maintained
`product/` acceptance/conformance tests with **new post-correction oracles**.
Do not transplant the original *negative* expectations that asserted the defect;
do not use research artifact paths/SHAs/ephemeral coordinates in product code or tests.

**R1: no invisible marker budget.**
Recreate the two-run, same-supported-state setup where one old runtime had a
trailing `OrderCompleted` marker and the other did not. Prove that `maxEvents=1`
is spent on the next real scheduled completion and that both runs now show
the same supported changes, command-time behavior and outcome. Include
`QUIESCENT`/new submission behavior as applicable. Never assert that every
pair of equal supported observations necessarily has identical future state
when their **authoritative** scheduled inputs legitimately differ.

**R2: no silent time split.**
Under bounded `advance()` and `advanceUntil()`, hand-derive and assert
the relevant frontier/next-command-time relationship across: queue dispatch,
shared multi-eligible backlog, availability recovery, equal-time completions,
interleaved commands, time guard beyond/before an event, count exhaustion,
no pending work, reset and a fault cascade. Preserve bounded fault evidence.
A research-local emulated `Turn` is not the product API to implement.

**D2: eliminate result-affecting marker-only progress.**
Recreate the previously divergent accepted-time / lead-time sequence.
The test should assert the adopted, corrected result for the same supported
decision boundary; do not assert that a genuinely different authoritative
advancement script must magically have the same result.
Include a Game-like pacing example where the consumer issues a command at an
identical **supported** state and changing frame/event budget alone cannot
make hidden markers change the production outcome. Current static/start-fixed
presentation remains valid.

**Preserve and adjust existing positive fixtures.**
Specifically rework `HeadlessClosureAcceptanceTest` marker-presence assertions
and any `SessionControlAcceptanceTest` count/returned-list expectations
that relied on `TaskStart` and `OrderCompleted` being scheduler events.
Test the new **absence of inert marker turns** and retention of supported
order-completion and quiescence truth instead. Inspect:
`RuntimeEventDeliveryAcceptanceTest`,
`RuntimeObservationAcceptanceTest`, `EngineDerivedResultConformanceTest`,
`FactoryHandlerTest`, `RecordingSchedulerTest` and relevant Finance tests.
Preserve normal end-to-end state/event and derived-result conformance.
Use explicit hand-derived expectations and deterministic independent fresh runs;
do not weaken tests simply to reach green CI.

Maintain and test the distinction between **internal event processing** and
**supported event publication**. It remains valid in general that not every
internal event is a supported `RuntimeEvent`; this PR fixes the current
Factory session's redundant scheduled markers rather than claiming a generic
one-to-one ontology.

## Normative reconciliation required in the *same* PR

Update the owning current-state authorities alongside code/tests:

- `docs/architecture/engine-semantics.md` §§1.1, 1.2 and §4 rule 3:
  specify the result-affecting meaning, the unchanged one-event API,
  command-time handling after correction, and the rule excluding inert queued
  markers from bounded Factory stepping. State the interpretation change
  explicitly and keep the rule independent of research labels or temporary
  commit coordinates.
- `docs/architecture/runtime-contract.md` only as necessary to preserve
  a truthful supported-order-completion/evidence account; do not promote
  the internal scheduler clock to a new supported fact as part of R-remove.
- Inspect `docs/architecture/overview.md`, Engine readiness and
  Game consumer planning for semantic neighbors. Change only statements
  made inaccurate by the correction. A Game live-loop requirement is not
  being selected by this PR.
- Preserve deterministic ordering/dispatch and event identity semantics
  as specified; any change to ordered returned internal `Event` lists is
  an explicitly recognized compatibility effect, not evidence that
  supported event streams may be weakened.
- If a significant architectural decision deserves concise rationale,
  put the **rule** in the owning specification and use Git/PR history
  for the research chronology, rather than copying an investigation into
  the canonical document.

The current Engine readiness research boundary excludes **new** advancement
or session capabilities without a concrete consumer need. This PR is a
**correction to an already-owned, incomplete Engine interpretation**, not
delivery of a speculative advancement feature. Make that distinction clear
in the PR's stable rationale. The independent review specifically permits
a behavior-changing remedy now when selected by the owner as such.

## Explicit non-goals

Do **not**:

- implement `E-guard` authoritative-turn semantics or change
  `advance()` into a batch/turn API;
- add `D-clock`, scheduled player inputs, a simulated-time floor,
  interval seal, terminal closure, producer-finality witness or
  independently verifiable interval-close API;
- reopen or reconcile execution-account E1–E8, choose analytics formulas,
  create an execution-account module, event-source the Engine, or add
  a retained history service;
- implement a Game loop or change Game presentation speed/interaction policy;
- change dispatch/admission ranking, resource eligibility, scheduler
  deterministic tie-breaking, spatial execution, or the separate
  same-semantics performance task;
- import `workspace/research/` into this branch or merge the evidence branch;
- open a second research track to revisit an already chosen owner decision.

If a real current handler/consumer makes R-remove impossible within the
bounded contract without an unapproved semantic expansion, stop with the
concrete counterexample, minimum alternatives and proposed owner decision;
do not silently substitute E-guard or E-record.

## Validation, PR and stop conditions

1. Before changing code, identify all actual internal marker producers/consumers
   and the smallest semantic-closure set; record the specific behavior that will
   intentionally differ. Do not expand to adjacent cleanup merely because files
   are open.
2. Implement R-remove with the retained tests above, and update authoritative
   specification text in the same branch. Verify affected supported event
   counts, sequence, times, fault-path emission, order completion, reset and
   budgeted progression. Keep changes coherent if implementation needs several
   small commits.
3. Use the repo-owned test commands and supported Java >= 21 runtime;
   on Windows prefer devcontainer, then documented Docker/WSL workflow.
   Run the directly affected Factory/simulation/Finance tests and
   `cd product && ./gradlew test` plus the appropriate Checkstyle,
   architecture conformance and coverage checks. Run `./arcogine check`
   if the execution environment permits. Report failures honestly;
   never call an unrun suite passing.
4. Check `.github/CONTRIBUTING.md`'s bounded semantic-closure rule:
   code, tests, specification, current planning/provenance and consumers
   should tell one truthful story about the behavior that would land.
5. **Remove this handoff and all tracked `workspace/` content from
   the PR head** before creating/finalizing the PR; verify the
   `.github/scripts/check-transient-workspace.mjs` check and inspect
   branch-added files for misplaced temporary material. The original
   prompt commit remains reachable in this branch's history by exact SHA
   even after its file is removed from the net diff.
6. Open one normal PR from `fix/engine-remove-inert-markers` into
   current `main` using the repository PR template. In the PR body state
   stable rationale and semantic/compatibility impact (including
   budget-interleaving changes), plus non-goals. Immutable research
   report/review coordinates may be cited as evidence in the PR
   discussion/body when useful, but **no** maintained `product/`
   source or tests may refer to transient research paths.
   Do not add validation logs, current SHAs, status or CI claims to the
   PR description.
7. After PR publication, own exact-head CI convergence. Resolve the
   `CI` workflow for that exact head and continue synchronously until
   `CI / gate` reaches terminal green or a concrete blocker is reported.
   If `main` moves or conflicts emerge, reconcile the implementation
   branch non-destructively and repeat CI on the resulting head.
   Do not claim independent review authorization, and do not merge.
8. Hand the **current head** to the independent PR Reviewer when ready,
   preserving the difference between product validation, CI and reviewer
   disposition.

Return only the PR number/link, branch and final commit SHA, material
semantic/code/spec changes, exact R1/R2/D2 retained test names and their
results, compatibility implications, local validation commands/outcomes,
exact-head CI/gate result, base freshness/mergeability, and any remaining
independent review/owner blocker. No new research report and no pasting
this handoff back to the user.
