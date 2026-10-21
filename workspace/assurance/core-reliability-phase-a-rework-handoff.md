# Core Reliability Assurance Phase A — independent-agent rework handoff

## 1. Commission and immutable inputs

Act as a **fresh, independent coding/assessment agent** for Arcogine. You did not author the original Phase A assessment and did not author its adversarial review. Your bounded task is to **revise the Phase A assurance-design packet in response to a completed independent adversarial review**. Do not start Phase B, fix Java product bugs, alter supported semantics, certify reliability, or treat this work as a second original assessment or an independent re-review.

Repository: `alaiba/arcogine`.

Fetch and read **all** of these at their exact commits, not at a mutable branch tip:

1. Original Phase A assessment branch `codex/core-reliability-phase-a-assessment`, commit **R = `6bc1e1ff5f11f7aff525761a027ec3b71db73f7b`**:
   - `workspace/assurance/core-reliability-phase-a.md`
   - `workspace/assurance/core-reliability-phase-a-crosswalk.json`.
2. Independent review branch `codex/core-reliability-phase-a-independent-review`, commit **V = `e0d89fe0e369fa427712653b2ef87e5493091381`**:
   - `workspace/review/core-reliability-phase-a-independent-review.md`.
3. Original Phase A mandate, branch `handoff/core-reliability-phase-a-20261022`, commit `3dcdcb4bf883bcf570cba1867e49792be9cb179d`:
   - `workspace/assurance/core-reliability-phase-a-handoff.md`.
4. Independent review mandate, branch `handoff/core-reliability-phase-a-independent-review-20261022`, commit `acf1b8088d17df33651359ce3f080f7182a5643f`:
   - `workspace/review/core-reliability-phase-a-independent-review-handoff.md`.

The original assessment and review both bind source evidence to **T = `da70c929be95b98aba773a9225ec70ba13d50c31`**. At this handoff's creation, T was live `main`; PR #510 (initial exploratory diagnostics and Scheduler test-strengthening) was still open/unmerged, and issue #514 (JSON escape fix) was closed with the fix landed. Re-read live state. Never credit PR-only changes on main and never treat an old status as current.

The independent review verdict was **REWORK DESIGN REQUIRED; PHASE B AUTHORIZATION: NO**. It found the mechanical 300-file, nine-module, 33-contract inventory sound and the four campaigns broadly credible; it did **not** verify comprehensive runtime correctness. Its specifically requested corrections, including A1–A3 and U10, define this assignment. Read all of V rather than relying on this recap.

**Identity guard:** At task start resolve this rework handoff by its own exact branch + SHA + path. If it cannot be resolved, return `INPUT BLOCKED — HANDOFF NOT AVAILABLE`. If R, V or either packet member is unavailable or mismatched, return `INPUT BLOCKED — SOURCE NOT AVAILABLE`, naming precisely what failed. Do not reconstruct the missing source from chat.

## 2. Repository grounding and independence

- Resolve live `main` and record SHA M. Read `AGENTS.md`, `.github/CONTRIBUTING.md`, `docs/development/testing.md`, `docs/architecture/overview.md`, the owning architecture/specifications and the relevant code/tests **at T**, plus at M where actual code/doc drift matters. Search `docs/` for assurance, invariants, failure models, determinism, canonical identity, evidence/coverage, replay, governance, refusal, time/cursors and test oracles. Read applicable agent-role instructions; do not imply this is an existing formal Research, Consistency or PR-review lifecycle.
- Before editing, read both the complete Phase A assessment and the entire independent review, including its U1–U9 dispositions, 33-group coverage table, reproducibility appendix and requested revision.
- Verify R/V file and blob identity. Reproduce or sanity-check the exact crosswalk membership, all 300 tracked Java paths and their blob IDs **against T**. The original and reviewer already did this; do not repeat a full inventory merely to produce activity. Preserve all existing mappings unless evidence shows they are wrong.
- Compare M against T. Where main changed materially, distinguish (a) correction of a claim about T, (b) a new source/contract/test reality on M, and (c) a future refresh prerequisite. Do not silently swap the assessed baseline underneath the crosswalk. If M has materially diverged so much that a T-bound revised design is misleading, report the affected paths and prepare the smallest justified rebase/refresh proposal; do not fabricate coverage.
- Your analysis and revisions must be **independent intellectual work**. Treat V as review evidence, not an automatic replacement for source authority; check pivotal claims against source/spec/reproducer and preserve uncertainty where they cannot be confirmed. You are the rework author, not its approving independent reviewer.

## 3. Required rework — keep it bounded but complete

### A1 — Add independent Phase D assurance-verdict challenge (blocking design defect)

The prior design required a fresh exact-main final baseline and independent *Phase A* review and individual implementation PR reviews, but did not assign an independent challenge of the **final Phase D assurance verdict**.

Add an operationally precise Phase D protocol to the assessment and Phase B/C/D transfer design:

- **Entry:** final exact source/spec/test `main` SHA; provenance of each campaign's executed evidence (revision, tools/versions, corpus/generator source+seed and bounds, oracle/model versions, commands, discovered tests, environment); full per-contract/failure-family executed/blocked/residual map; issue/remediation status.
- **Reconciliation:** identify all drift since every pilot/campaign run, which results were independently rerun at final T_final and which cannot be transferred; never convert stale branch-only evidence, truncated campaigns, inconclusive timeouts, equivalent mutants or lost scratch artifacts into passes.
- **Claims:** concrete contract-specific supported-domain assertions; named failure models examined, successful distinguishing controls, minimized reproducible failures, justified exclusions and unresolved consequential faults. A blank or failed family prevents an unqualified result for that family; broad reliability claims require no material unknown silently marked green.
- **Independent decision challenge:** a **separate agent/context**, not the Phase A reviewer by default and not the author of the final Phase D synthesis, attacks the final verdict, per-family closure, oracle independence/common-mode bugs, survivors, unexplored state/corpus frontiers, and exact-main transfer. It publishes an immutable review artifact with explicit accept/qualify/rework outcome. Standard individual code-PR review is insufficient.
- **Exit:** only after any blocking review challenges are reconciled may the owner consider a strictly qualified assurance statement. An honest result with unresolved material risks is `QUALIFIED/INCOMPLETE` rather than “core reliable.” No support/release promotion, permanent dashboard, standing gate or automated certification follows.

Specify where finite campaign provenance lives during execution (temporary workspace with resolvable identity), how later Phase D cites valid immutable evidence, and what is deliberately retained in final historical non-normative summary rather than a giant maintained lab notebook. These are future duties, not artifacts to create now.

### A2 / U10 — Add extreme evidence-window cursor arithmetic (material omission)

Address the review's independently executed **`EvidenceWindow.missingWithin`** witness. At T, a valid-shaped `EvidenceWindow` with start=1, end=final=`Long.MAX_VALUE`, no missing sequences, reports `isComplete() == true` but wrongly lists `[1,MAX]` missing for query `(0,MAX]`, because `runFinalSequence + 1` wraps.

- Amend R-CAPTURE, E-EVENTS and downstream R-DERIVE failure-model, technique and campaign-closure descriptions, not merely the unresolved-findings table.
- Explicitly cover cursors `0, 1, MAX-1, MAX`; `(after,through]` interval algebra with inclusive/exclusive endpoints; empty and nonempty ranges; complete/partial/wrong-frontier windows; latest representable sequence; post-frontier queries; successor overflow and the effects on `DeclaredEvidence.completeEvents`.
- Demand an **independent interval-set/endpoint oracle with no `MAX+1` sentinel**, plus contract-licensed B/L/M controls that would detect the demonstrated failure.
- Connect the admitted source-sequence horizon and next-event overflow decision to U3. Distinguish a constructed legal-shaped helper witness from a proven real `MAX`-event capture or guaranteed runtime reachability. U10 is evidence of a helper boundary inconsistency, not permission to invent unbounded history support or to implement the fix now.

### A3 — Reclassify and localize confirmed findings (material)

Reconcile the original U1–U9 table with V's independent evidence, using a concise provenance field such as **static contradiction, executed probe, retained assertion, contractual ambiguity, untested hypothesis**. Preserve exact input domains, response surfaces and limits. At minimum:

- **U1:** signed `SimTime.minus` saturation Javadoc contradiction confirmed (MIN minus 1 returns MAX); this is not evidence of reachable negative Engine time. Define the owner decision for total signed-domain subtraction, positive difference overflow and value-vs-runtime domain.
- **U2:** repeated fresh JVM runs returned varying `FactoryModelValidator` diagnostic orders over the same invalid eligible set. Mark a **confirmed diagnostic determinism violation**, not a scheduling or canonical fingerprint defect. Specify owner decision/expected order before revised F-PUBLISH validation-oracle rules are closed.
- **U3:** runtime/scheduler sequence counters lack an established finite horizon even with a drained event stream; event-producing availability toggles permit indefinitely many public transitions without growing retained work. This is static/inductive evidence, not a `MAX`-operation run. Decide sequence horizon, overflow/refusal and post-state; connect U10 and distinguish scheduler insertion counters, runtime public events and bounded internal job stores.
- **U4:** occurrence non-acceptance is distinct from an unpromised all-authorities rollback. Do not demand a transaction redesign. Verify occurrence state and reference authority state separately; only a stronger promise requires an owner decision.
- **U5:** constructed `ChallengeAttempt`/economics records can overflow long deltas (MIN→MAX yields -1) while `BigInteger` score delta remains exact. Distinguish these from genuinely pipeline-admitted attempts. Record exact-vs-refuse owner decision for the supported comparison domain; do not report ordinary admitted game attempts defective without evidence.
- **U6:** do not invent JSON duplicate-key, Unicode-scalar rejection, resource depth/heap or broad interchange guarantees. Preserve current integer-vs-fraction parsing distinctions. Treat optional larger resource guarantees as separate owner choices, not default blockers.
- **U7:** review probe on complete supported-shaped extreme-duration evidence produced an `ArithmeticException: long overflow` in processing occupancy. Distinguish an **executed representability failure** from silently incorrect returned totals; two occupancy consumers share a helper and cannot corroborate each other. Define exact/refusal/admitted-domain owner decision and independent BigInteger oracle.
- **U8:** two **validator-accepted distinct surrogate-containing catalogue item IDs** yielded **identical semantic fingerprints** because lossy UTF-8 replacement preceded hashing. Mark the demonstrated accepted-content identity alias, **not** a SHA collision. Restrict the proposed decision to catalogue item text admission or exact encoding at its owner; do not impose a parser-wide surrogate rejection or assert every catalogue swap bypasses downstream item-availability checks. Give this high-consequence binding issue prominent Phase B prerequisite treatment.
- **U9:** no Java test, Gradle gate or matching JaCoCo run was executed at the assessed T by the original agent. Reviewer Javac/probes do not change that; this is a truthful evidence limit, not a false baseline or automatic requirement that Phase A run full campaigns.
- **U10:** add a separately identified entry, not subsumed under U3; preserve exact A2 witness, scope and intended follow-up.

The revised packet may link to V as review provenance inside transient workspace, but must remain usable from its own committed rework branch and must never treat branch-only evidence as landed product tests.

### Correct G-CONFORMANCE binding/shortcut wording

V verified that subject/assertion/revision checks precede the scope shortcut, but `EvidenceConformanceEvaluator.validateUses` runs **after** that shortcut. Narrow statements in G-CONFORMANCE and the oracle planning that could imply *every* evidence-use binding is checked before scope refusal. Determine the actual supported outcome contract by reading T's implementation and `governance-evidence.md`. Preserve the distinction between `NOT_APPLICABLE`, `UNKNOWN`, required-evidence refusal, supplied evaluation and binding validation. Do not install an unconditional rejection requirement absent authority. Make this a test-design correction, not a production change.

## 4. Decide the minimum prerequisites without silently changing meaning

Produce an **owner-decision and campaign-prerequisite table** that answers, for each U1–U10 and A1–A3:

1. What is *actually demonstrated*, by which type of evidence, at which SHA?
2. Which current semantic sentence, source implementation and admission boundary govern it?
3. Does it demand a **design text correction**, a **contract interpretation/owner decision**, a **bounded confirming experiment**, a **separate bugfix**, or merely an **explicit residual exclusion**?
4. Which Phase B campaign/slice is affected and what can proceed independently?
5. What exactly would close the prerequisite without assuming an implementation fix already exists?
6. Which unresolved high-consequence finding prevents a particular unqualified Phase D claim?

Distinguish proposed implementation fixes from Phase A corrections. If a genuine contradiction needs authority, present narrow alternatives, consequences and recommended evidence to choose, **not a unilateral choice**. Do not blanket-block all unrelated Phase B work behind one arithmetic domain question; equally do not call a domain campaign complete while its high-consequence input or oracle prerequisites remain unresolved. Preserve existing non-goals, supported/deferred scope and no-new-gate principle.

The independent reviewer endorsed the general four-campaign decomposition and 33-contract inventory. **Preserve that useful structure.** Touch affected matrix rows, method recipes, campaign dependencies, review challenge guide and the completion verdict; leave unrelated material intact. New findings justified by your own grounding should be added with clear classification, but do not restart a 300-file survey or manufacture defects.

## 5. Completion and independent re-review

A revised Phase A packet is **ready for independent re-review**, not automatically adequate, only when:

- A1's Phase D independent assurance-verdict protocol is explicit and falsifiable.
- A2/U10 is incorporated in every affected contract/campaign/technique and has a distinguishing extreme-cursor oracle, not just a footnote.
- U1–U10 classifications match the *supported evidence* and the confirmed U2/U8 outcomes are no longer merely “suspected.” U4/U5/U6 limits and G-CONFORMANCE nuance remain correct.
- All unresolved contract/representation/ordering/sequence-horizon decisions and consequential known failures have bounded owners, suggested next actions and **campaign-specific entry conditions**.
- No claims are upgraded to executed testing, coverage certification, new support, correctness proof or Phase B authorization. The exact T/M relationship remains explicit.
- The 300-file crosswalk remains internally valid and exact for its declared assessed SHA; link targets and contract IDs are checked; no new source or test changed on an assessment-only branch.
- A reader can understand which portions of the design were changed in response to A1/A2/A3 without reading the full review. Include a compact reviewer-finding disposition, including accepted, narrowed or disputed points and why.
- Declare a truthful revised design verdict: `READY FOR INDEPENDENT RE-REVIEW — CONTRACT BLOCKERS EXPLICIT` if the **design** is adequate but owner decisions remain; otherwise `INCOMPLETE — DESIGN REWORK STILL OPEN`. **Both** must say `PHASE B AUTHORIZATION: NO`. Only an independent re-review after the revisions, plus appropriate owner decisions, can justify moving to execution.

**Do not hold rework hostage to resolving domain decisions the owner has not made.** The purpose is to make the *design* correct and reviewable, with named blockers, not to certify the runtime or implement selected semantic policies. Avoid a fake `READY` that relies on the assertion that someone later will design the missing oracle.

## 6. Repository custody and allowed actions

- Create a **new, separate, rework-only branch from live `main`** (not a branch attached to either R's or V's session). Preserve the original assessment commit R and review commit V untouched; do not force-push either branch or delete any branch.
- Copy the **exact** R-bound assessment and crosswalk into the new branch at
  `workspace/assurance/core-reliability-phase-a.md` and
  `workspace/assurance/core-reliability-phase-a-crosswalk.json`.
  Verify file content/blob identity **before** editing. Revise the report in the new branch; update the crosswalk only if source correctness actually requires it. Preserve exact original provenance in the revised packet; do not masquerade as the original author.
- If necessary, use a short `workspace/assurance/core-reliability-phase-a-rework-notes.md` only for concise discrepancy/reproduction evidence that genuinely cannot fit in the assessment; the default is two packet files, not another notebook. Use ignored `logs/` for ephemeral probes. Do not generate a PR merging `workspace/` into `main` — repository checks intentionally prohibit it.
- Do **not** edit production Java, test suites, architecture/specifications, maintained development docs, historical reports, CI/tooling, research register, open issues or PR #510 as part of this handoff. Report proposed product/contract corrections as bounded future actions. No new Issue/PR is authorized simply because the assessment discovers a concern.
- Follow `AGENTS.md` human author/committer identity and no artificial agent attribution. Commit the revised packet on the distinct rework branch. No merge, branch deletion, or Phase B execution.
- Conclude with **rework branch + full immutable revised commit SHA + exact packet path(s)**; source R/V/T, live M, a compact A1–A3/U10 disposition, any new findings, unresolved owner decisions, verification commands/results, and the **explicit next action: independent adversarial re-review of this exact revised packet by a new session**. Do not present your self-check as that review.

This assignment succeeds by faithfully improving a sound but incomplete assurance design and preserving uncertainty, not by minimizing the number of findings, claiming no known defects, or producing a runtime reliability verdict.
