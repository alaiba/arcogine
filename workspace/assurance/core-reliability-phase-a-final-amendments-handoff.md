# Arcogine — Phase A final design amendments (Step 1)

## Mission

You are a **fresh, independent assessment-editing agent**, separate from the agents who authored the original Phase A packet, its rework, and its independent reviews. Make a **bounded amendment** to the accepted-in-principle Core Reliability Assurance Phase A design after the latest independent re-review. Your job is **to integrate RR-1 through RR-5 correctly** into the existing assessment, including a new U11 owner-decision entry, and commit a new immutable assessment packet ready for a **narrow Step 2 independent acceptance addendum**. This is *not* a Phase A restart, product remediation, architectural interpretation decision, Phase B execution, or final reliability assurance.

The latest reviewer gave **"DESIGN ADEQUATE; BLOCKERS EXPLICIT. PHASE B AUTHORIZATION: NO."** Keep that historical judgment intact. It does not mean the reported weaknesses are fixed, nor does the rework author get to self-approve this amended packet. The goal is to ensure a later campaign agent can identify the right failure classes, reproduce the right controls, know where meaning is unresolved, and distinguish the supported scope from unimplemented promises.

## Immutable sources and custody

Repository: `alaiba/arcogine`. Resolve these exact source objects *before* editing:

- **Subject to amend, Q**: branch `codex/core-reliability-phase-a-rework`, commit `260a9c9ddf29eeddf10f56192cb06ffaf9120a4d`; report `workspace/assurance/core-reliability-phase-a.md` (blob `caecbacea9e8e01be8924998ce61499ebccd0d50`); crosswalk `workspace/assurance/core-reliability-phase-a-crosswalk.json` (blob `d1015aa98b398616981eaba46c9335359b09a6d8`).
- **Controlling feedback, W**: branch `claude/core-reliability-phase-a-re-review`, commit `72da6152c93b6821d61f8f70a378e08c7232f74a`; `workspace/review/core-reliability-phase-a-independent-re-review.md` (blob `d43ab8adb95f34548629996260271234f8425577`). Read the whole review, including its appendix, not just verdict/summary.
- **Earlier independent review, V**: branch `codex/core-reliability-phase-a-independent-review`, commit `e0d89fe0e369fa427712653b2ef87e5493091381`; `workspace/review/core-reliability-phase-a-independent-review.md`.
- **Original assessment, R**: branch `codex/core-reliability-phase-a-assessment`, commit `6bc1e1ff5f11f7aff525761a027ec3b71db73f7b`; original report and crosswalk at the same `workspace/assurance/` paths.
- **Initial and rework mandates**: `handoff/core-reliability-phase-a-20261022` @ `3dcdcb4bf883bcf570cba1867e49792be9cb179d`, `workspace/assurance/core-reliability-phase-a-handoff.md`; and `handoff/core-reliability-phase-a-rework-20261022` @ `aaf6b9f78e2456b803e869bc68130cdbc5971e02`, `workspace/assurance/core-reliability-phase-a-rework-handoff.md`.
- **Commissioned acceptance-review procedure**: `handoff/core-reliability-phase-a-acceptance-review-20261023` @ `eb3d310c9b29a09e9543e013124d52805d47f810`, `workspace/review/core-reliability-phase-a-acceptance-handoff.md`. W mistakenly said no such separate handoff existed. This provenance discrepancy should be **acknowledged in your delivery report**, but does not undo W's technical findings or demand another full review. Do not silently treat W as having followed that exact procedure.
- **Code/spec assessment baseline T**: `da70c929be95b98aba773a9225ec70ba13d50c31`.

At creation of this handoff, `main` still equalled T. PR #510 remained open/unmerged; the JSON escape fix from issue #514 had already landed on T. These are mutable facts: resolve live `main` and relevant open work yourself; never credit branch-only work as landed. If M differs from T, compare M to T and record any material effects on the T-bound assessment. Keep the crosswalk's `assessedCommit` honest; do not quietly remap historical proof to M without a justified complete regeneration.

First resolve **this handoff** at its supplied branch + exact SHA + path. If it is unavailable, stop with `INPUT BLOCKED — HANDOFF NOT AVAILABLE`. If Q's packet or W cannot be resolved at exact SHA/blob, stop with `INPUT BLOCKED — SOURCE UNAVAILABLE`. Do not reconstruct a source from chat.

## Repository/role grounding

Read `AGENTS.md`, `.github/CONTRIBUTING.md`, `docs/development/testing.md`, `docs/architecture/overview.md`, `docs/architecture/runtime-contract.md`, `docs/architecture/governance-evidence.md` and the relevant current domain specifications at T. Search `docs/` for "evidence", "frontier", "sequence", "revision", "rollback", "applicability", "independent review", "oracle" and "assurance". Inspect affected implementation and retained assertion bodies at T; code is evidence of behavior, not automatic authority to revise a spec. Do not invoke the PR Reviewer `./` process or author a formal PR disposition: this is an assessment packet amendment in transient custody.

You may conduct **small, bounded** independent calculations or reproductions to validate a material correction; document whether evidence is static, a retained test, W's reproduced probe, or a new executed probe. This request does **not** authorize Gradle-wide tests, mutation/fuzz campaigns, CI changes, specification edits, issue filing, or a product patch.

## Task 1 — RR-1: two independent successor-overflow sites in research capture

Current `EvidenceWindow.missingWithin(afterSequence, throughSequence)` at T has two separate `long` arithmetic problems:
- `afterSequence + 1`: for an allowed empty query `(Long.MAX_VALUE, Long.MAX_VALUE]`, wraps even when the recorded run final cursor is an ordinary value.
- `runFinalSequence + 1`: for a run ending at `MAX`, can mark retained coverage as missing.

W reproduced the first with complete windows `f=0`, `f=5` (with and without a gap), query `(MAX,MAX]`, with expected **empty** results; the f=MAX variant can throw. W also demonstrated a *surviving fault*: a hypothetical fix that only guards `final+1` passes all explicit Q's original seven-row table and U10 witness while leaving the query-side wrap.

Correct the design at **all affected locations**: R-CAPTURE; E-EVENTS, R-DERIVE; §4 boundary (B), stateful (L) and fault-sensitivity (M) recipes; research and runtime campaigns in §5; §6 U10 findings/owner prerequisites/reproduction/claims; review challenge guide and Phase A verdict where relevant.

The replacement design must:
1. Record both observed sites within U10 (U10a/U10b is fine; no need for an entirely new contract group). Clearly separate query-argument overflow from runtime producer-sequence horizon U3. Reducing the producer horizon does **not** fix a `(MAX,MAX]` caller query unless the query domain is explicitly changed by its owner.
2. Specify an independent endpoint-set oracle for missing `(a,b]` without `MAX+1` sentinels, with correctly guarded conversion back into inclusive long ranges. Compare exact *missing membership*, preserve unintended overlaps/duplicates for diagnostic attribution, and do not impose unsupported incidental ordering guarantees.
3. Add **explicit** rows at `(MAX,MAX]` for `f=0`, `f=5` gapless, `f=5` with an interior gap and `f=MAX`, all expecting the empty missing set. Add f=MAX and f<MAX crossed with `after` and `through` selected from `0,1,MAX-1,MAX`; explicitly state this is a **joint** frontier × query-boundary exploration rather than only one range endpoint dimension.
4. Include observed result classes: exact correct range, spurious missing, duplicate/overlapping output, empty-vs-present result mismatch, and thrown exception. Treat empty range as trivially complete. Trace the impact on `DeclaredEvidence.completeEvents` and `missingEvents` without claiming an arbitrary constructed window is runtime-produced or that MAX events were observed.
5. Retain W's **final-only-fix survivor** as a required negative control. The future diagnostic must kill both successor-fault families separately, not simply pass the original U10 witness. Keep exact reproduction and shrinking instructions where the MAX endpoint cannot be reduced without losing the fault.
6. State which semantics are directly promised by the current helper Javadoc versus which are pending owner decisions about admitted cursor horizon/caller arguments. Do not implement the behavior or choose a new API precondition.

## Task 2 — RR-2: add U11 Governance evidence-use target-revision ambiguity

The independent re-review constructed lineage **R1(F1) → R2(F2) → R3(F1)** and a relied-on non-comparator `EvidenceUse` targeted at **(F1,R1)**. On T, `EvidenceConformanceEvaluator.evaluate` returned **PASS for subject (F1,R3)** and **PASS for revisionless F1**, and `InMemoryEvaluationOccurrenceAuthority.accept` accepted an R3 occurrence relying on the R1-targeted use. Source checks compare the use's target revision to the use's *own fingerprint*, and compare non-comparator use fingerprint to the evaluated fingerprint, but do **not** compare use target-revision ID to the evaluated or occurrence revision ID. A retained test intentionally allows the reverse shape (revision-bound subject, revisionless use), which must not be accidentally prohibited.

Governance evidence §4 requires each use to have an independent subject/revision/claim/applicability determination and says equal fingerprints or rollback **never copy one use's applicability or acceptance to another target**. This is a **confirmed observed behavior with a plausible contract contradiction and an owner decision required**, *not automatically a settled production bug*. The evaluator's consumed applicability may have been independently determined for a later target only if the owning contract supports that interpretation; do not decide that in this packet.

Add **U11** to the decision/prerequisite register. Narrow G-CONFORMANCE/G-EVIDENCE/G-OCCURRENCE text, any `§4` Governance truth tables and future Governance campaign exit rules that currently imply that mismatching target revision IDs are already rejected. Include dimension matrix:
- target revision absent / same as evaluated revision / different revision with same fingerprint (rollback) / present when subject revision is absent;
- non-comparator vs comparator role; relied on vs excluded, source/target binding and applicability independently determined;
- subject revision-bound versus revisionless; stored occurrence basis and exact use context;
- valid evidence use with revisionless target on revision-bound subject (retained-accepted precedent) versus present *different* revision target.

Require **independent expected outcome only after the owner settles meaning**, not by copying currently returned PASS or unilaterally asserting refusal. Name the smallest owner decision: should non-comparator use's *present* target revision require exact match to subject revision, be barred for a revisionless subject, or be allowed with separately established per-target applicability? If fingerprint-level targeting is chosen, what precise §4 rollback non-transfer obligation survives and what must be represented/proven? Capture alternatives, semantic consequences and per-slice gate.

Move any "auto-copy applicability on rollback" mutant from a hypothetical guard deletion to a **known behavior/decision-dependent challenge** until an actual guard and contract exist. Do not conflate the evidence source's intrinsic provenance, use-target relationship, or a revision's exact occurrence with a model fingerprint.

Governance slices involving evidence use/evaluator/occurrence binding **cannot close that family** until U11 is resolved and its diagnostic oracle becomes authoritative; unrelated accepted-revision, Storage and selected Governance work may proceed if later commissioned. Future Phase D cannot claim proven non-transfer across rollback on the present evidence.

## Task 3 — RR-3: discriminating and non-masked controls

Two named Q §4 controls are misleading in the presence of the known baseline bugs:
- The "ignore a declared gap" mutant on `f=MAX, gap [MAX,MAX], query (MAX-1,MAX]` can **improve** agreement when the existing `final+1` defect duplicates the singleton gap.
- Exclusive-lower-endpoint mutation on some gapless/f=MAX rows cannot be distinguished before the baseline is corrected; U10's current-site and `after+1` failures are baseline defects, not hypothetical mutants.

Redesign the expected controls with **valid distinguishing rows**, including ordinary `f=5`, `gap [2,3]` and queries `(0,5]`, `(2,5]`, `(3,5]` as appropriate. For each control state which independent expected set and assertion must differ. Where a control only becomes meaningful after correcting a baseline implementation flaw, label it a **post-remediation sensitivity control**. Future test execution must run baseline, corrected implementation and one mutant at a time and **must never count a baseline discrepancy, compile error or test-discovery failure as a killed semantic mutant**. Retain Q's other genuine controls with accurate preconditions.

No mutations or product fixes are executed in Step 1. The output is a correct fault-discrimination recipe.

## Task 4 — RR-4: read-support hull is not completeness

Inspect `DeclaredEvidence.completeEvents` and `EvidenceSupport` at T. Their recorded `EventRange` support is the **smallest covering hull** of the ranges actually read, even when there are unretained sequences between separate successful reads. A hull from reads `(0,2]` and `(5,7]` may be `[1,7]` despite missing `[3,4]`.

Clarify R-CAPTURE and R-DERIVE experimental oracles so:
- read-support hull **never** implies interior continuity, sufficient basis or verified completeness;
- each separate read's sufficiency/coverage must be supported by its basis, declared inputs, frontier and gaps;
- the aggregate recorded support hull is compared to the min/max of actual read intervals, independently of sequence completeness;
- no revision invents event-sourcing, history retention or a public coverage guarantee.

Add a counterexample to the capture/control matrix or experiment recipe; no product change.

## Task 5 — RR-5: final Phase D challenger independence includes evidence authorship

Q's Phase D protocol already requires independence from the **final synthesis author**. Strengthen it to require the challenger to disclose **any authorship, execution, selection, mutation triage or outcome adjudication** of the relevant B campaign and C rerun evidence. A reviewer **must not adjudicate a family whose evidence they authored**; if different people/agents own different evidence families, delegate those to a separately independent challenger and reconcile the combined verdict. Review separation is based on *who produced and judged the evidence*, not merely worktree, branch, agent label or model family.

Make the independence declaration and exceptions explicit in the immutable final review record. Preserve the existing exact-main evidence-transfer and per-family challenged-verdict protocol. No standing workflow gate, reviewer infrastructure, permanent audit database or certification process is authorized.

## Integration, evidence classes and limits

Edit the existing packet rather than append five disconnected errata. Ensure the affected §1 provenance/reviewer-disposition area, §3 matrix, §4 method recipes, §5 campaign/Phase D protocols, §6 U-register and owner-prerequisite table, review challenge guide and §7 verdict consistently identify RR-1 through RR-5. **Do not** restate U11 as accepted by the owner or mark its outcome PASS/REFUSED on semantic authority not yet chosen. Keep U1–U10's valid confirmed-vs-inferred distinctions and all original supported/deferred exclusions.

Add a compact **RR-1..RR-5 disposition table**, noting where each correction lives and whether it is merely designed, proven by W's probe, or waiting for owner decision. Optionally note the acceptance-handoff discovery/provenance mismatch in this table's context without converting it into a Java defect or triggering another broad review.

Avoid "design accepted" rhetoric that erases its unresolved meaningful blockers. The amended artifact can describe itself as **`READY FOR NARROW INDEPENDENT ACCEPTANCE ADDENDUM — BLOCKERS EXPLICIT`**, with **`PHASE B AUTHORIZATION: NO`**. Its historical earlier verdicts remain recorded at their immutable commits. You cannot independent-review or approve your own modifications.

Keep domain-by-domain assurance claims correctly bounded, including unresolved U1/U2/U3/U5/U7/U8/U10/U11, independent oracles, tool feasibility, mutation survivors and incomplete frontiers. Do not introduce a 34th group merely because U11 is a new finding: it belongs to existing G-* groups.

## Acceptance conditions for this Step 1 amendment

The amendment is complete only when:
1. It preserves Q's correct 300 Java path/blob/source-set mapping, 33 contract groups and four campaign design; the unchanged crosswalk matches T exactly and has the same blob `d1015aa98b398616981eaba46c9335359b09a6d8`. If a real inventory defect is discovered, report precisely; do not silently discard/rebuild mappings.
2. Both `missingWithin` overflow sites have explicit independent distinguishing rows. In particular, a **final-only-fix** cannot satisfy the future test design; `(MAX,MAX]` on ordinary f=0/f=5 windows is named. Unknown domain policy remains explicit.
3. U11 is in the affected Governance matrix/experiments, §6 owner decision and campaign closure dependencies; it does not quietly change §4 evidence semantics or ignore revisionless-use precedent.
4. RR-3's controls have valid distinguishable expected sets and a baseline-versus-post-fix distinction. RR-4's support hull is not mistaken for complete coverage. RR-5's reviewer authorship requirement is enforceable at the design/evidence-custody level.
5. All RR-1..RR-5 are addressed throughout their affected semantic neighbors; the packet contains no materially contradictory old claims that would mislead a Phase B agent.
6. All unresolved owner decisions remain owner-owned; no production defect is claimed fixed, no Phase B/JUnit/mutation/coverage/CI execution is inferred, and no general reliability guarantee is made.
7. The packet truthfully declares readiness for a **narrow, separate independent Step 2 acceptance addendum** against W and the earlier acceptance mandate; not a fresh 33-contract review unless the amendment actually widens semantics.

## Artifact custody, changes and handoff

Create a **fresh amendment-only branch from live `main`**, not Q's branch or W's review branch. Copy the exact Q report and crosswalk into this new branch under the same two paths:
- `workspace/assurance/core-reliability-phase-a.md`
- `workspace/assurance/core-reliability-phase-a-crosswalk.json`.

Verify original Q byte/blob equality **before** editing. Revise the report in place and leave the crosswalk byte-identical unless a demonstrated mapping defect requires a separately explained change. A small separate supporting scratch/reproduction file may be committed under the same `workspace/assurance/` area only if genuinely necessary; default is **exactly two packet files**. Ignore ephemeral `logs/`; do not create another large notebook.

Do not modify the original R, V, Q, W branches or their commits; do not edit Java source/tests, architecture/spec documents, maintained development docs, CI, tooling, research-register state, GitHub issues, PR #510 or any other live delivery surface. No handoff prompt to a new reviewer is authorized within this task: report that a **separate, later Step 2 handoff** should be commissioned after the amended artifact exists.

This is transient workspace custody, **not a PR/merge candidate**. Follow `AGENTS.md` human owner author/committer identity, no model/tool/session attribution blocks, no merge and no branch deletion. Do not expect the tracked-workspace merge-candidate check to pass on the intentionally workspace-bearing branch.

Commit the amended packet and return:
- amendment branch + **full immutable packet commit SHA** + report and crosswalk paths;
- assessed T and current live M, with any material drift and applicable scope;
- a concise exact RR-1–RR-5 disposition and the U11 classification;
- retained evidence, source checks and validation outcomes, plus any unresolved material concern;
- the exact next action **"Commission narrow Step 2 independent acceptance addendum on the amended packet"** and **`PHASE B AUTHORIZATION: NO`**;
- the session-continuity/deletion verdict required by `AGENTS.md` (all unique material must be committed or the remaining uncaptured item named).

Success is a coherent, independently reviewable correction to an already broadly adequate assurance design. It is *not* a new reliability baseline, defect-remediation PR or authority to start testing campaigns.
