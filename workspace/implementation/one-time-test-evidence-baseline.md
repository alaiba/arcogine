# One-time test-evidence baseline for Arcogine

## Role and bounded objective

Act as Arcogine's coding/implementation agent under AGENTS.md, .github/CONTRIBUTING.md, and docs/development/agent-roles/implementer.md.

The repository owner has requested a ONE-TIME, RISK-BASED TEST-EVIDENCE BASELINE now that specifications, architecture, and implementation have been reconciled and unnecessary artifacts have been removed. This is an assessment of how strongly retained executable tests constrain existing behavior, NOT certification of product correctness, a release/support promotion, or a repeatable new quality gate.

Carry out three stages, in order:
1. Inventory executable evidence for all existing Java modules and their critical contracts, including unchanged code.
2. Prioritize concrete residual uncertainty using contract consequence and test evidence, not coverage percentage alone.
3. Execute a small number of actual targeted, out-of-band diagnostic pilots and classify what they demonstrate.

The checkpoint-based Continuous Improvement agent is deliberately lighter: it currently selects diagnostics from recent merged changes and does not establish a historical evidence baseline. Do not repurpose that agent to perform this full inventory or assume unchanged code has already passed adversarial evaluation.

Repository: alaiba/arcogine.
Context at handoff creation: main at 3b0747acdd1d807ce68e78d7adf1217b57cdb86a on 2026-10-18. On execution, resolve LIVE main and record the exact code revision actually assessed. This coordinate is starting context only; it is not permission to treat later changes as absent or to overwrite the real baseline. Never claim a branch-only result is landed on main.

## Start-of-run grounding

- Resolve this handoff from the exact branch + commit SHA + path supplied by the owner. Stop with INPUT BLOCKED — HANDOFF NOT AVAILABLE if the exact artifact cannot be resolved; do not reconstruct it from conversation.
- Read current AGENTS.md, .github/CONTRIBUTING.md, docs/development/agent-roles/implementer.md, docs/development/testing.md, docs/development/continuous-improvement.md, and docs/development/agent-roles/continuous-improvement.md.
- Read docs/architecture/overview.md and the relevant owning architecture/specifications, executable source, and tests for the candidate boundaries. Search docs/ for test evidence, invariants, coverage, canonicalization, validation, dispatch, determinism, and any other material candidate concepts.
- Obtain the authoritative module inventory from product/settings.gradle.kts and the current tracked source/test tree, not from a remembered module count or old project snapshot. Include modules with Java main sources and the test-only architecture/integration proof modules as distinct evidence contributors. Include the non-shipped research-experiments substrate without treating it as shipped product behavior.
- Check current open issues and active work only enough to avoid duplicating an existing evidence-hardening owner. Do not start a repository-wide Consistency review or delivery-process retrospective.
- Branch from live main for any reviewed implementation/report change. Do not edit, merge, or delete the handoff's branch. Follow Arcogine's human Git author/committer identity, tracked-workspace, review, and PR requirements.

## Stage 1 — Existing evidence inventory

Survey the ENTIRE CURRENT Java module set at module level, including modules whose code has not changed recently. This is breadth-first classification, not a claim to have audited every statement in every class. For each executable module, account for its important public/semantic boundaries and test evidence; for test-only modules, identify which cross-boundary invariant they prove.

For each material boundary, capture compactly:
- Owning module, relevant types/entry points, and governing semantic contract or specification section.
- Consequence of a subtle incorrect implementation, including trust, ordering, identity, persistence, validation, failure, or determinism where relevant.
- Retained proving tests and their assertion/oracle strategy (example cases, invariant/property tests, equivalence comparisons, golden vectors, acceptance/conformance tests, failure tests). A test name or execution alone is not proof of its assertion strength.
- Existing LINE and BRANCH coverage evidence IF actually observed from current JaCoCo/CI reports, with provenance and limits. Do not fabricate percentages or require a new branch-coverage gate; JaCoCo misses exception handling as branches. Current policy has a per-executable-module 90% LINE floor, not a robustness certification.
- Meaningful areas not inspected deeply or with unavailable evidence, explicitly marked NOT ASSESSED rather than quietly labeled robust.

Look beyond obvious successful examples: exact boundary conditions, rejection/error paths, event ordering, state-machine transitions, null/malformed representations where applicable, deterministic repeatability, and independently observable consequences. Distinguish spec-defined behavior from optional implementation detail. Avoid making new correctness requirements merely because testing could be more exhaustive.

## Stage 2 — Prioritize residual uncertainty

Make a SMALL risk-prioritized shortlist (normally 3–6 semantic boundaries) from Stage 1. Selection must not rely on recency. Use qualitative, evidence-backed criteria: contractual consequence if wrong, branch/decision density or state-space complexity, current test assertions and missing plausible cases, exposure to malformed inputs, downstream blast radius, and whether a valid oracle or fault model is available. Do not invent a numerical confidence metric or repo-wide ranking.

For each shortlisted boundary, state:
- Exact code and test locations and authority.
- A falsifiable, concrete evidence question that current tests do not clearly settle.
- Why the question matters and the evidence supporting the uncertainty; say UNKNOWN when it cannot be determined.
- The most diagnostic-efficient technique, with rationale, selected from mutation testing, targeted property-based testing, differential testing, metamorphic testing, targeted fuzzing, or a more directly justified narrow failure-injection probe.
- Why the chosen comparison/oracle is legitimate and what a meaningful failure would look like.

Do not treat unchanged code as previously assessed. Do not infer an absence of gaps from high LINE coverage, green CI, matching documentation, or the absence of recently merged changes. Avoid duplicating already-owned work and avoid recommending a technique solely because it is on the menu.

## Stage 3 — Execute focused diagnostic pilots

Perform TWO or THREE bounded pilots on distinct high-value questions chosen from Stage 2. Prefer different diagnostic failure models where possible; do not preselect a tool or target before the inventory. A focused parser/validation challenge and a separate invariant/fault-detection challenge may be good candidates, but only if Stage 2 supports them. Run the smallest credible scope (specific packages/classes/tests, not all modules under mutation or fuzzing).

- Use deterministic seeds and repeatable commands when applicable. Capture exact main baseline, test target, tool/version/configuration, seed/corpus, meaningful oracle/assertion, execution command, observed result, exclusions/limitations, and rough runtime/cost.
- Mutation testing: inspect killed/surviving/equivalent or irrelevant mutants; a survivor is a question about evidence, not automatically a bug. PIT is an option, not a mandated installed tool. Do not permanently add Gradle/PIT plugin/configuration solely to support an occasional run.
- Property-based testing: state the invariant and exercise an appropriately varied space; do not use the same shallow examples with randomized order.
- Differential testing: compare genuinely independent implementations or contractually equivalent paths only, normalizing intentionally different identities where the contract requires.
- Metamorphic testing: document the allowed transformation and the exact invariant/relation expected to hold.
- Fuzzing or failure injection: specify permitted input domain and observable error/refusal/safety properties; absence of crashes alone is not proof of semantic correctness. Bound resource use and stop conditions.
- Inspect each interesting result. Classify a real missing proving case, an equivalent/irrelevant diagnostic case, an ambiguous contract needing owner input, or a blocked/unavailable experiment; do not imply the experiment proves the absence of defects.
- When an unequivocal existing-contract assertion is missing and a SMALL deterministic retained test fits the same coherent slice, add that test. Keep generated corpus, raw logs, heavyweight output, throwaway scaffolding, and ephemeral tool setup out of tracked product code. If a genuine product bug or unresolved semantic decision is discovered, preserve reproducing evidence and propose separate bounded remediation instead of silently changing production meaning.

If the first diagnostic approach is infeasible in the environment, select another appropriate one and say why. Do not claim Stage 3 complete without actual runs. If all meaningful pilots are blocked, report the exact blockers and partial completion explicitly; do not substitute design prose for performed diagnostics.

## Durable results and process reconciliation

Produce ONE concise, dated, non-normative historical evidence record under docs/history/continuous-improvement/ (for example YYYY-MM-DD-test-evidence-baseline.md). It must include the exact assessed main SHA and date, module-wide inventory coverage, critical boundary/evidence matrix, qualitative priority shortlist, executed pilot commands and results, assumptions/limitations, material gaps, already-owned work, and smallest practical follow-ups. Keep the record a point-in-time account, not a living quality scorecard, completion register, release readiness grade, or asserted correctness certification. Do not embed voluminous logs, mutation listings, corpus data, generated reports, or a new recurring task state. If necessary, make a minimal truthful edit to docs/history/README.md to describe the one-time record's purpose. All current product semantics remain in the owning architecture/specifications and tests, not in the historical report.

Review docs/development/agent-roles/continuous-improvement.md and docs/development/testing.md for one narrow follow-through: after this baseline, allow explicitly identified, still-unresolved high-value test-evidence uncertainties to be considered for focused diagnostics even without a recent semantic change. Keep the normal checkpoint's cheap bounded merged/closed scan, ownership checks, and no-automatic-execution boundary intact. Do not have the checkpoint re-inventory the codebase, re-run baseline diagnostics, or maintain a baseline status ledger. If the source of an unresolved uncertainty cannot be retained in an authoritative current owner/issue without new bureaucracy, document that limitation rather than inventing a tracker.

Do not add a new test agent, CI workflow, standing schedule, mutation/fuzzing score threshold, blanket branch threshold, mandatory diagnostic tool, repository-wide gate, general-purpose testing framework, unnecessary abstraction, or change to runtime/public/storage semantics. No broad refactoring, no repeated sweeping after the baseline, no speculative product code, and no bulk issue creation.

## Acceptance and delivery

1. Every currently declared Java module is represented in the Stage 1 inventory, with test-only modules distinguished. Critical semantic contracts receive deeper evidence examination and untouched/unexamined areas are identified honestly.
2. A bounded, evidence-backed risk shortlist ties each proposed diagnostic to an exact boundary, oracle, test weakness/uncertainty, and expected value. The shortlist is not a numeric quality score.
3. Two or three real, reproducible targeted pilots have been run, with their outcomes inspected and classified. If environment or semantic ambiguity blocks this, describe the concrete missing evidence and do not claim a full baseline.
4. Material demonstrated gaps result in the smallest retained tests or clearly scoped proposed follow-up; reported findings distinguish actual defects from proof limitations and equivalent diagnostic cases.
5. One restrained non-normative historical baseline report provides usable initial evidence, with no permanent tracking machinery. The Continuous Improvement wording is reconciled only as necessary to recognize explicitly unresolved baseline uncertainties without turning future checkpoints into deep audits.
6. Validate what changed with the narrowest relevant native tests and the canonical ./arcogine check when executable tests/configuration are modified. Use the documented JDK-21-compatible toolchain. Report skipped/unavailable diagnostics honestly. For any PR, achieve terminal green exact-head CI / gate or state the actual blocker; leave independent reviewer disposition to the PR Reviewer.
7. Follow normal branch and PR hygiene: remove tracked workspace artifacts from any merge candidate, retain this source handoff's custody branch, use the PR template for stable change intent only, do not merge, and hand back the exact report location, tested main SHA, diagnostics actually run, material findings, validation, and next owner/action.

A clean result, including no demonstrated gaps, is acceptable if grounded in genuinely executed diagnostics. Do not optimize for discovering defects, increasing mutation score, or creating process work.
