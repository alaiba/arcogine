# Arcogine core reliability assurance — Phase A handoff

## Mission and authority

Act as a coding agent commissioned by the repository owner to perform **Phase A: comprehensive assurance scope and adversarial test-design** for Arcogine's *retained Java core*. This is a read-only, evidence-design assignment. It is **not** another two-pilot test-evidence assessment, a product implementation task, a formal Consistency review, or a declaration that the core is reliable.

The owner intends to establish the strongest **practically demonstrable** confidence that existing implementations satisfy their *current, supported semantic contracts* after specification/architecture/code reconciliation and pruning. The ultimate four-phase sequence is: (A) establish and independently challenge a complete assurance scope; (B) execute contract-wide adversarial test campaigns; (C) remediate proven weaknesses and repeat relevant tests; (D) independently assess a final exact-main evidence baseline. **Your assignment stops at Phase A.** Design a rigorous evidence strategy with explicit completion/falsification conditions; do not substitute executing a couple of pilots for systematic coverage.

Repository: `alaiba/arcogine`. Context when this handoff was authored: `main` = `ff1d6aeecf599246c3829542111fb78b905ca193`; PR #510 (initial exploratory test-evidence assessment and two pilots) was **open and unmerged** at head `10c906efb49bef69a3712bf5365f0f66cd85fc25`; issue #514 (Challenge JSON Unicode-escape grammar defect) was **open**. Treat these as contextual evidence only. Resolve *live* state at execution time and never credit branch-only tests or reports as already present on `main`.

## Grounding and role discipline

1. Resolve this handoff from the supplied **branch + exact commit SHA + path** first. If any coordinate does not resolve, stop `INPUT BLOCKED — HANDOFF NOT AVAILABLE` rather than reconstructing from chat.
2. Resolve live `main` and record exact SHA `T`. Read `AGENTS.md`, `.github/CONTRIBUTING.md`, `docs/development/testing.md`, `docs/architecture/overview.md`, and `docs/development/continuous-improvement.md` on `T`. Read the applicable agent-role procedures for any operations you actually perform; this evidence-design assignment does **not** authorize you to change product semantics or claim a formal specialized review.
3. Search `docs/` for the material assurance concepts: invariants, canonicalization, identity, compatibility, failure/refusal, replay, determinism, recovery, provenance, evidence, scheduling, publication, dispatch, validation, content decoding, conformance, correctness, coverage and test diagnostics. Fetch owning architecture/specification files and match their exact current text to code/tests at `T`.
4. Discover the complete module list from `product/settings.gradle.kts` and the current tracked source/test tree, not a remembered nine-module count. Map all Java main-source files and all cross-module/test-only proving suites. Treat `research-experiments` as non-shipped; distinguish test-only modules from implementation modules and shipped code from deferred/unimplemented capabilities.
5. Consult the initial evidence in PR #510 (and its prior report/reproducer commit, if available) and live issue #514 **as leads and falsifications**, not as proof that any other boundary has passed assurance. Consult current open issues/PRs only as needed to avoid duplicate work, attribute verified gaps, or identify work that will change the assessed code.
6. Read canonical documents for each concrete semantic surface, as applicable: `docs/architecture/engine-semantics.md`, `runtime-contract.md`, `factory-design.md`, `factory-model.md`, `factory-resource-semantics.md`, `controlled-revisions.md`, `governance-conformance.md`, `governance-evidence.md`, `storage.md`, `challenge.md`, `external-representations.md`. Explicitly separate currently implemented/supported behavior from open research, proposed features and externally unsupported fault models.

Use repository-owned semantic authority rather than assuming code always wins or every narrative description is implemented. If source and current contract disagree, record the discrepancy as an **open semantic/implementation blocker**; do not silently choose an interpretation or edit the contract. Likewise, do not enlarge a public support commitment through the assurance exercise.

## Phase A work — establish the whole assurance surface

### 1. Build a comprehensive contract-to-code-to-test inventory

Enumerate **every tracked Java production/main-source type** in every declared module and account for it. Group types into meaningful semantic boundaries, citing exact owning contract sections, public entry points, implementation paths and proving test paths. Every type must appear in the inventory or be covered by a named parent boundary with a verifiable path rule; no large "other" bucket. Account separately for test-only modules, architecture/static checks, research substrate, integration/cross-boundary proofs, and any non-Java repository tooling only when it materially controls the Java assurance claim.

For **every material contract/invariant** within each boundary, capture:
- Stable semantic identifier and exact authoritative source section (do not encode PR or obsolete planning IDs into a durable identifier).
- Admitted input/state domain, preconditions, output/observable boundary, equality rules and the consequences of violating it. Include success **and** rejection/no-side-effect/refusal semantics.
- Implementing entry points and dependent consumers, including cross-module interactions.
- Existing retained tests and the **actual strength and independence of their assertions/oracles**: illustrative examples, golden vectors, invariants, model-based results, deterministic replay, faults, conformance, negative cases, etc. Merely referencing a passing test name is not an assertion analysis.
- Existing LINE/BRANCH coverage only with verified provenance (test run, report and assessed SHA), and limitations; never convert coverage into a reliability claim. Where tests/code/coverage cannot be inspected, mark `NOT ASSESSED` with a reason.
- Known defects, exclusions, ambiguous semantics, and evidence gaps; distinguish a demonstrated violation from a test weakness, a suspected risk, and an untested question.

This is a *completeness exercise at the contract/boundary level*, not a mandate to paraphrase every line or attach a huge generated file list to a permanent document. Use compact tables with precise links and a machine-checkable inventory/crosswalk when feasible, so an independent reviewer can detect omissions.

### 2. Perform an explicit failure-model decomposition

For each consequential boundary, enumerate realistic classes of faults that could violate supported contracts. Do not select only convenient techniques or code changed recently. Evaluate, where applicable:
- Boundary values, overflow/underflow, rounding, Java numeric behavior, null/empty/invalid representations, malformed encodings, grammar/canonicality and exact identities.
- Ordering/ties, clock/tick authority, monotonicity, queue and dispatch invariants, state machine transitions, aborted operations and their **post-state**, cross-event causal ordering, deterministic replay/seed/completeness.
- Ownership/authority, mutable aliasing, historical revision identity, provenance/evidence applicability, accepted-vs-refused transitions and cross-module invariant preservation.
- Persistence atomicity and recovery **within documented support**, corruption/truncation, reopen/definition binding and controlled partial failure; do not claim power-loss or unsupported-filesystem guarantees without an owning contract.
- Domain-specific validation, policy gates, compatibility, publication vs admissibility, external-content refusal, resource exhaustion where specified, and explicitly bounded concurrency/interleavings only where relevant.
- Common-mode/oracle defects: two paths sharing one buggy helper are not independent verification; round trips can agree on the same wrong encoding.

Attach meaningful fault models to each contract. If a method or failure model is inapplicable, record a **semantic reason**, not just "not needed." Use a qualitative consequence/exposure/complexity/observability rubric; do not invent numeric reliability, confidence or mutation thresholds.

### 3. Choose a method portfolio for each boundary

For every material failure model, identify the **most discriminating practicable** test or reasoning technique. Explicitly consider, with justified applied/planned/not-applicable decisions:

- deterministic boundary/exhaustive finite-domain enumeration and combinatorial pairwise or higher-order interactions when constraints warrant;
- targeted mutation testing for assertion fault sensitivity (survivors inspected for equivalence/contract relevance);
- property-based and stateful/model-based sequence testing with shrinking/minimization;
- bounded exhaustive state-space exploration or model checking for high-consequence finite-state logic when feasible;
- differential testing only where legitimately independent implementations or contractually equivalent entry paths exist;
- metamorphic testing where supported input transformations imply precise observable relations;
- structured/grammar-aware fuzzing of strict decoders, formats, untrusted inputs and refusal/robustness behavior;
- deterministic failure injection and recovery/reopen experiments tied to supported durability/acceptance assumptions;
- static analysis, existing ArchUnit/Checkstyle/compiler guards and controlled integration/contract tests;
- performance/resource and concurrency-sensitive tests **only** for explicitly supported resource or concurrency semantics, not arbitrary aspirational guarantees.

Do **not** require every method for every class; do **not** let a cheap single technique substitute for independently challenging important failure models. For each critical boundary use *complementary oracles/fault models* when they add substantive discrimination. State the independence limitations of the oracle(s): independent goldens/models vs implementation-derived values, correlated test fixtures, normalization of permissible differences, expected false positives/equivalent mutants, and relevant seeds/corpus construction.

For each proposed experimental family specify: concrete entry point, admitted cases to generate, fault/mutation family, oracle and pass/fail meaning, deterministic replay mechanism, shrink/reproducer strategy, applicable tool or minimal no-new-tool option, estimated order-of-magnitude cost when supportable, expected runtime/compute constraints, and what result would change the subsequent assurance verdict. State unknown feasibility rather than guessing or implying a tool was installed.

### 4. Design the execution campaigns and exit conditions

Partition the future **Phase B** into small independently reviewable, domain-owned campaigns. A useful starting decomposition (adjust on evidence, not just to fit this outline):
- shared types/identity/time and Factory canonical representation;
- Simulation/Factory Engine dispatch, bounded advancement and deterministic replay;
- Governance, accepted evidence, controlled revisions and Storage/recovery;
- Challenge content, policy/economics/admissibility, Factory–Challenge integration, research oracles and architecture conformance.

For each campaign specify:
- scope and exact contract IDs, dependency ordering and shared oracles;
- full inventory of consequential failure-model families it must cover (not an arbitrary 2–3 pilot count);
- techniques to execute with executable entry points or straightforward reproduction designs;
- evidence artifacts and criteria for substantive completion;
- bounded slices suitable for implementation PRs **after** reviewed Phase A, with rules for reverting/retaining experiment code;
- expected remediation and independent recheck loop (Phase C) and what may be transferred to later Phase D;
- prerequisites, blockers, overlap with already-owned issue(s), and explicit non-goals.

Do not create long-lived score dashboards, standing jobs, new gates, recurring audit obligations, wholesale new frameworks, or a permanent cross-cutting abstraction merely to execute Phase A. Proposed diagnostics may use temporary tools in Phase B where justified; their maintenance requires separate evidence/decision.

## Minimum acceptance bar for the Phase A artifact

The Phase A result is a **reviewable assurance design**, not merely an inventory or recommendation list. It is complete only when ALL of the following are verifiable:

1. All current Java main-source types and test-only proof surfaces are accounted for via an explicit inventory/crosswalk. The matrix covers every materially consequential current semantic contract in their supported scope; nothing is omitted because it was unchanged or previously passed CI.
2. Each critical contract has a linked owner/specification, code surface, existing proving evidence, explicit realistic failure models, suitable independent oracle(s), proposed executed-in-Phase-B techniques, and a semantic rationale for exclusions. If full enumeration of a semantic contract is infeasible, identify the unresolved coverage limit precisely and declare `INCOMPLETE` rather than claiming full coverage.
3. The assessment tests the adequacy of planned **oracles and failure models**, not just planned inputs, tools or case counts. A reviewer can ask "what plausible serious bug would survive this plan?" and locate how that bug class is challenged or why it is explicitly residual risk.
4. High-consequence cross-boundary seams are traced across their real producers and consumers. Unsupported/deferred semantics are separated and not silently promoted to guarantees.
5. A feasible domain-sliced Phase B execution map exists, with evidence/reproduction standards, fault triage, validation expectations and a Phase C remediation/retest loop; no arbitrary number of pilot executions is an exit criterion.
6. Every known material discrepancy, uncovered contract, unreviewable oracle or infeasible technique is visible with the smallest concrete proposed owner decision or blocker. **Unknown is not passed.**
7. The output plainly says **Phase A does not establish runtime correctness, comprehensive coverage of executed adversarial diagnostics, or reliability certification.** The next transition is an **independent adversarial review of the Phase A design** before Phase B starts.

## Deliverable format and custody

Create **one coherent, comprehensive but navigable transient assessment packet** under `workspace/assurance/core-reliability-phase-a.md` on a dedicated assessment branch from live `main`. This is an assessment-only branch, **not a PR to merge to main**, because tracked `workspace/` is forbidden in merge candidates. You may place a supporting crosswalk in the same `workspace/assurance/` subtree only if the main packet cannot remain navigable without it; avoid a sprawling set of generated reports.

Organize the packet as:
1. exact assessed baseline, scope, evidence methods and omissions;
2. module/type-to-contract coverage and cross-boundary dependency inventory;
3. contract/failure-model/oracle/technique matrix with acceptance conditions;
4. domain campaigns and their dependency order, resource/feasibility notes;
5. explicit unresolved unknowns, exclusions and contradictions;
6. **review challenge guide**: most likely omissions, oracle-correlations, high-consequence surviving-fault scenarios and questions an independent assessor must try to falsify;
7. Phase A completion verdict: `READY FOR INDEPENDENT REVIEW` or `INCOMPLETE`, never a core-reliability verdict.

Keep scratch outputs in ignored `logs/`, not the repository root; do not alter `docs/history/`, `docs/architecture/`, product source/tests, CI/tooling, research-register status, PR #510 or issue #514 as a side effect of Phase A. If discovering a real new issue requires owner action, report the bounded evidence and proposed issue rather than making unsanctioned code/spec decisions. No retrospective or Continuous Improvement checkpoint is part of this assignment.

Commit the packet with the human repository owner's Git author/committer identity; provide the resulting **branch + immutable packet commit SHA + path**, the exact assessed `main` SHA, an inventory/coverage summary, principal threats, unresolved blockers and a recommendation for independent review. Independent review must be performed by a separate review execution context; do not self-authorize. If the live `main` advances materially during inventory/design, reconcile affected surfaces or mark your assessment incomplete, never silently certify an obsolete tree.

The owner may then request a separate committed fresh-session handoff for the independent Phase A reviewer, followed by Phase B campaign handoffs once the design survives scrutiny. **Do not start Phase B, open test-remediation PRs, or declare the final assurance baseline within this task.**
