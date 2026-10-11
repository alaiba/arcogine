# Implementation handoff — remove the Engine busy counter

## Mission and delivery boundary

Execute the **already-made repository-owner decision to remove \`busyTicks\`** from the supported Factory runtime, rather than reopening the analytics ownership question or selecting a replacement utilization formula. This is a narrowly scoped **high-risk Engine semantics + runtime observation contract** change and must receive the ordinary independent PR Reviewer review.

Repository: \`alaiba/arcogine\`. Work **on this new branch**, \`fix/remove-busy-ticks\`, originally cut from live \`main\` \`a80cd6bf83d478dedb7a782143f93674cb5019e0\`. Prompt artifact: \`workspace/implementation/remove-busy-ticks.md\` (transient; remove it before submitting the final review candidate). Re-resolve current \`main\`, this branch and active PRs before editing and at delivery; do not assume the recorded baseline remains live. Preserve unrelated current work.

Read and follow live \`AGENTS.md\`, \`.github/CONTRIBUTING.md\`, \`docs/development/reviewing.md\`, \`docs/development/testing.md\`, the authoritative Engine semantics and runtime contract, and the relevant architecture overview. Quick-search \`docs/\`, \`product/\` and architecture tests for \`busyTicks\`, \`setBusyTicks\`, \`busy ticks\`, \`cumulative utilization\`, \`utilization facts\`, \`bottleneck\`, and supported derived-result arithmetic. Search is for discovery: inspect exact current files and their callers before changes. Use supported Java 21+ validation.

**This is implementation/reconciliation, not new research.** The Game diagnostic report records the owner's explicit removal decision, and the independent review accepted it **with a corrected, expanded scope**. The Analytics Phase 2 revision subsequently treats this removal as a *separate owner-decision route*, not adoption of the unresolved prospective analytics model. No new analytics service, API, data-retention scheme or Game diagnostic methodology follows from this task.

## Binding evidence (exact historical coordinates)

1. Game diagnostic report: \`4ad00136441fa29e6b462ac5431b60cf2de1e148\`, \`workspace/research/investigations/factory-design-game-diagnostic-evidence-report.md\`, §11 ("The busy counter: owner decision"). It says "**Remove it**" during reconciliation; removal was deferred only to preserve the research's counterexample baseline.
2. Its independent adversarial review: \`6484795ff4c4ad971e06c70751b03805e54a55f5\`, \`workspace/research/investigations/factory-design-game-diagnostic-evidence-adversarial-review.md\`, §4.5 and qualification 8 (§6). Verdict **ACCEPT WITH QUALIFICATIONS**. This review's **expanded removal inventory**, exclusions, and adjacent carried-load correction are binding acceptance inputs.
3. Analytics Phase 2 revised report: \`75736dddfa3fe1ae89c5089720c702f4ca595ab5\`, \`workspace/research/investigations/simulation-analytics-ownership-phase-2-admission-boundary-revision.md\`, §§5, 12, 15 and 23. It explicitly calls \`busyTicks\` **"Owner decision: remove"**, without adopting a future analytics owner or authorizing this as research promotion.
4. Current authoritative text: \`docs/architecture/engine-semantics.md\`, \`docs/architecture/runtime-contract.md\`, \`docs/architecture/overview.md\` and the maintained semantic neighbors discovered below. They must be changed atomically with implementation.
5. R-remove inert scheduler marker change is **already merged** into the handoff baseline (#465). Its event-ordering and command-time guarantees survive this change. The execution-account reconciliation (#466) also landed. Do not accidentally undo either result.

Do not commit historical workspace research or experiment artifacts from another branch. They are evidence, not PR content. Cite the reviewed reasoning in the PR body where helpful, but durable product comments and canonical architecture must express the adopted semantic result without depending on temporary workspace paths or report SHAs.

## Why the change is deliberate, and what it does not prove

The current counter is an Engine-owned, per-machine cumulative sum of **completed-step processing job-ticks**, with saturating arithmetic. It is credited at step completion; a running step has not yet credited its time. With \`concurrency > 1\`, dividing by elapsed simulated ticks may exceed 100%. The counter is not an instantaneous occupancy or a generally valid utilization/bottleneck metric. **At quiescence, below saturation**, it truthfully equals total completed processing job-ticks. Accordingly, the counter is *misnamed and misused as utilization*, not mathematically wrong. The review found that removal **or renaming** is evidentially defensible; the repository owner explicitly selected **removal**. Do not claim the research mathematically required this choice.

The current reported \`busyTicks\` result is supported Engine semantics; removing it intentionally narrows the supported observation and changes the Engine interpretation, even when no production consumer uses it. It is **not** implementation-only cleanup or a backward-compatible transparent refactor. No replacement utilization producer is required as a prerequisite: the research substrate derives occupancy from supported events. Nothing in this decision settles future normalized utilization, starvation, bottleneck, or analytics definition ownership.

## Product change — exhaustive removal, no replacement

Remove the cumulative busy counter and all its maintained APIs/mutation paths, with appropriately scoped code edits:

- \`Machine\`: remove the \`busyTicks\` state and initialization, getter, \`setBusyTicks\` setter and supporting comments.
- \`MachineView\`: remove its \`busyTicks()\` method and any no-longer-true Javadoc.
- \`FactoryHandler\`: remove only the step-completion busy-duration crediting (and associated routing lookup used **solely** for this credit). Preserve authoritative completion, dispatch cascade, lead-time and order-value accounting, and step scheduling.
- \`ResourceObservation\`: remove the \`busyTicks\` field/record component. Reconcile **every** constructor call, copy fixture, pattern, field access and consumer that depended on the old record shape.
- \`FactoryRuntime.observe()\` and any other projection/helper: stop retrieving and publishing busy ticks. Do not alter the supported event schema or observation metadata/cursor to compensate.
- \`product/architecture-conformance-test/.../ArchitectureTest.java\`: remove only the now-obsolete \`Machine.setBusyTicks\` mutator permission and leave other mutation constraints intact.
- Sweep test and **research-experiments** sources, including \`ProcessingOccupancyOracle\`, \`ProcessingOccupancyOracleTest\`, \`StarterCorpus\`, and \`WaitingWorkByStepOracleTest\`. Refactor or replace obsolete assertions, constructor arity and explanatory comments with **event/observation-based** proofs that do not require the removed counter. Preserve the actual occupancy and completion evidence; do not discard valuable discrimination merely to make compilation pass.

Do not remove \`completedSalesValue\`, \`averageLeadTime\`, \`throughputPerTick\`, \`backlog\`, \`combinedQueueDepth\`, time, active job projections, supported dispatch/completion events, or the other Engine facts. Preserve their exact accumulation, saturation and determinism contracts. Remove unused local imports, helpers and unreachable test scaffolding without unrelated refactoring.

## Normative and semantic-neighbor reconciliation

This PR **must** reconcile semantics and affected maintained documentation with product changes. Re-ground actual locations/anchors at live \`main\` before editing.

1. **\`docs/architecture/engine-semantics.md\`**:
   - Remove the \`busyTicks\` / utilization processing-time bullet and false later claims that a resource accumulates busy ticks, including spatial transfer/bottleneck prose (§10);
   - delete §10.1's **busy-tick-only** saturation rule and §10.2's \`busyTicks\` accumulator row, **renumber cross-references carefully** and retain mean lead-time saturation and the rules for zero elapsed throughput, empty completed-set mean, overflow rejection, exact counts, and completed value;
   - update the matching Engine conformance fixture list (§14, especially item 15);
   - ensure no broad claim remains that mere resource load/unqualified utilization identifies a bottleneck. Do not choose a new analytical definition.
2. **\`docs/architecture/runtime-contract.md\`**:
   - remove \`busyTicks\` as a supported resource projection and resolve the Performance minimum's ambiguous "utilization facts" commitment; retain the presently supported backlog, completed-order, sales-value, lead-time and throughput fields;
   - keep the independent E5/E6 evidence-completeness and interval-finality contract intact.
3. **\`docs/architecture/overview.md\`**:
   - revise the long headless-acceptance paragraph so it still accurately states the demonstrated observation/event/cursor and deterministic dispatch guarantees, but no longer claims that the active bottleneck is generally identifiable **either** by \`busyTicks\` **or by carried load (active + queued)**. The latter heuristic is also falsified by the Game diagnostic guardrails. Do not preserve the carried-load half under a different unsupported label;
   - remove its \`FactoryHandler.handleTaskEnd\` counter-crediting sentence; keep unrelated R-remove and supported-event statements.
4. **\`docs/architecture/isa-95-semantic-mapping.md\`**: inspect **both** the runtime Machine row and utilization observations/derived-measure row. Reword faithfully to retained facts; retain the distinction between externally definable utilization and an Engine-produced metric.
5. **\`docs/planning/spatial-runtime-consequences.md\`**: reconcile the identified busy-counter/overflow and transfer/admission examples and the Engine KPI interpretation sections. Do not change future transfer execution scope or other arithmetic requirements.
6. **\`docs/research/investigations/simulation-analytics-consumer-boundary.md\`**: keep the registered analytics question \`READY\` and its Phase 1 historical facts *clearly labeled as historical*, but amend current-surface matrices, proving cases, field inventory and ownership premise so they do not mandate or assume \`busyTicks\` still exists. Record the owner-decided removal without declaring Phase 2 accepted or selecting an analytics module.
7. **\`docs/research/investigations/simulation-analytics-evidence-provenance.md\`**: revise the unfinished-step and concurrency proving cases to test evidence completeness and occupancy derivation **without** a current busy-counter input. Its \`CANDIDATE\` status and promotion trigger are unchanged.
8. **\`docs/research/investigations/factory-design-game-diagnostic-evidence.md\`**: update obsolete counter-based proving language to durable supported facts, while preserving historically meaningful negative knowledge as an explicit prior state if useful; do **not** promote or conclude this separately \`READY\` Game question.
9. **\`docs/research/investigations/engine-evolution.md\`**: update its dispatch information-horizon inventory to omit \`busyTicks\` as a *currently available* fact, leaving the actual researched selection rule and retained conclusions untouched.
10. **\`docs/planning/factory-simulation-engine-readiness.md\`**, Game consumer/planning surfaces, mapping and any other semantic neighbor identified by the final repository-wide search: update *only* now-false current claims, preserved status/targets and no invented analytics feature. **Do not delete** \`docs/architecture/governance-evidence.md\`'s general statement about domain ownership of utilization/verification-objective semantics: the Game reviewer specifically excluded it because it does not depend on the busy counter.
11. **\`docs/research/research-register.md\`**: research status changes are **not part of this task**; only a genuinely required current-text cross-reference may be updated. No analytics/diagnostic question is concluded by busy-counter removal.

Do not recast source-code placement as semantic ownership. Reconcile supported API removal honestly rather than silently replacing it with an unspecified generic "utilization". Keep maintained docs self-contained and avoid including transient SHA + \`workspace/\` path pairs as durable dependencies.

## Tests and acceptance evidence

Change and execute the narrowest meaningful maintained conformance/acceptance tests, then required broader checks:

- **\`EngineDerivedResultConformanceTest\`**: remove \`busyTicks\`-specific overflow and accumulation assertions and rename/split the tests so **lead-time** saturation, zero-window throughput, empty mean and elapsed floors remain tested.
- **\`HeadlessClosureAcceptanceTest\`**: remove the counter-based resource ranking/bottleneck verdict **and the unqualified carried-load-is-bottleneck verdict**. Preserve its headless closure guarantees: late-join \`observe()\` completeness, supported event sequence/delta and state agreement, dispatch cascade, cursor after drain, status/run/time. The replacement should assert raw resource facts or scoped fixture behavior **without** universal causal attribution.
- Maintain evidence that dispatch/completion events plus published model durations can support **method-specific occupied job-time** for a complete recorded period; do not promote research oracle formulas as canonical analytics or preserve counter-comparison assertions after the field disappears.
- Verify \`Machine\` / \`MachineView\` / \`ResourceObservation\` surface has no \`busyTicks\` member, no mutable crediting call, and no stale constructor args; preserve unrelated architecture immutability constraints.
- Protect \`concurrency > 1\` and unfinished-work discrimination in tests using supported events and current-state projections, not \`busyTicks / elapsed\`.
- Verify arithmetic/determinism tests for other current derived results remain green, supported event ordering unchanged, and R-remove's \`advance()\` / \`advanceUntil()\` behavior unaffected.
- Verify documentation no longer promises a current normalized utilization metric. A remaining \`busyTicks\` reference must be intentional **historical provenance**, not current supported code/spec/plan truth; identify and justify each such exception.

Use \`./arcogine\` and \`docs/development/testing.md\` for canonical commands. Prefer scoped Gradle test targets first, then the required full product and architecture-conformance gates; report exact executed commands and outcomes in the final session handoff. If toolchain/environment prevents a check, state the gap and do not claim it passed.

## Scope discipline and PR delivery

**Non-goals:** no replacement accumulator, alternative "busy duration" field, analytic formula/definition, history-retention service, event sourcing, Engine scheduling/queue/routing policy change, Game rendering or playable-loop implementation, changes to business-domain utilization objectives, prospective analytics owner decision, or speculative shared analytics API. Remove a declared, report-only supported field without removing essential Engine state needed by current consumers.

Create **one standalone PR from \`fix/remove-busy-ticks\` to live \`main\`** containing the coherent code/tests/spec/docs change; do not mix it with the analytics research workspace or Game research reconciliation. Use the repository PR template for **stable intent, semantic compatibility impact and non-goals**, with no \`## Validation\` or volatile CI status in its body. Before PR review, delete the transient \`workspace/implementation/remove-busy-ticks.md\` from the **tracked branch tree** (its committed history remains accessible). Run \`.github/scripts/check-transient-workspace.mjs\` and relevant source-authority/link/delivery-label checks.

The handoff agent owns implementation, branch normalization against live \`main\`, remediation and exact-head \`CI / gate\` convergence. It must leave the independent PR Reviewer to issue the review disposition \`READY TO MERGE\` or \`CHANGES REQUIRED\`. **Never self-review, self-authorize disposition or merge.**

## Return contract

Return the PR number and link, branch and exact final head SHA; current \`main\` SHA used for the final comparison; concise inventory of removed code/types/contracts and corrected adjacent bottleneck assertion; specifically list preserved metrics and E5/E6/R-remove guarantees; state method-specific occupancy/consumer consequences without inventing a replacement; report all executed validation and exact-head CI terminal result, base freshness, GitHub mergeability, independent-review status and blockers. Confirm the final PR tree contains **no** tracked \`workspace/\` files. Do not paste the full handoff in chat.

**Stop at reviewed PR handoff; do not merge.**
