# Step 2 — narrow independent acceptance of the final Phase A design amendments

## 0. Commission, independence and authority

Act as a **fresh, independently tasked adversarial assurance-design reviewer**, not the author of the original Phase A assessment, its rework, its final amendments or either independent review. Your bounded assignment is to decide whether **RR-1 through RR-5**, including the new **U11**, were incorporated *correctly and completely enough for their affected semantics and campaigns* in the Step 1 amended packet. Preserve the earlier substantial design-level review rather than repeating the nine-module, 300-file, 33-contract audit without a concrete reason.

**The owner commissioned an independent acceptance addendum, not a favourable verdict.** Falsify the amendments where possible. A wrong endpoint oracle, an unrecognized existing defect disguised as a mutant, a silent resolution of U11, a completeness implication from an evidence-read hull, a conflict-of-interest loophole in Phase D, or a material contradictory packet passage are all grounds for targeted rework. Do not approve merely because a paragraph or row exists. Equally, do not block a valid design because future code fixes, owner semantic choices or Phase B test execution remain outstanding **and are explicitly bounded**.

This review decides **Phase A DESIGN acceptance only**. No Phase B commissioning, implementation change, requirement/specification change, Research lifecycle transition, PR review/disposition, reliability certification or unqualified core-correctness claim is authorized. The mandatory disposition line in every outcome is **`PHASE B AUTHORIZATION: NO`**.

## 1. Immutable subjects and custody: resolve before substantive work

Repository: `alaiba/arcogine`.

**Primary reviewed packet A — Step 1 amendments**
- Branch `codex/core-reliability-phase-a-final-amendments-20261023`
- **A = `baf2e2a94750a350cc533663d52c4c147f41d9ba`**
- `workspace/assurance/core-reliability-phase-a.md` — Git blob **`a13361a2b067402191c01dc66bc5a34351b09644`**
- `workspace/assurance/core-reliability-phase-a-crosswalk.json` — Git blob **`d1015aa98b398616981eaba46c9335359b09a6d8`**.

**Immediately preceding subject Q — compare content, not branch tip**
- Branch `codex/core-reliability-phase-a-rework`
- **Q = `260a9c9ddf29eeddf10f56192cb06ffaf9120a4d`**
- Same two `workspace/assurance/` paths; report blob `caecbacea9e8e01be8924998ce61499ebccd0d50`, crosswalk blob `d1015aa98b398616981eaba46c9335359b09a6d8`.

**Controlling adversarial re-review W — findings to dispose individually**
- Branch `claude/core-reliability-phase-a-re-review`
- **W = `72da6152c93b6821d61f8f70a378e08c7232f74a`**
- `workspace/review/core-reliability-phase-a-independent-re-review.md` — Git blob `d43ab8adb95f34548629996260271234f8425577`.

**Commission for Step 1**
- Branch `handoff/core-reliability-phase-a-final-amendments-20261023`
- Commit `649a748db8bb5f39eb8b03cec5e297c6def18960`
- `workspace/assurance/core-reliability-phase-a-final-amendments-handoff.md`.

**Earlier acceptance-review commission and provenance**
- Branch `handoff/core-reliability-phase-a-acceptance-review-20261023`
- Commit `eb3d310c9b29a09e9543e013124d52805d47f810`
- `workspace/review/core-reliability-phase-a-acceptance-handoff.md`.
- W says that no distinct acceptance handoff existed and records using an earlier mandate instead. This separate handoff **does exist at the immutable coordinates above**. Independently verify its presence. Do not retroactively claim W followed it; distinguish W's substantive work from procedural compliance. In this Step 2 addendum, follow **this** exact commission and relevant parts of the earlier acceptance-review commission. Identify any acceptance criterion not supported by either W's evidence or your own work, but do not automatically redo W's whole review.

**Earlier source/provenance context**
- Original Phase A R = `6bc1e1ff5f11f7aff525761a027ec3b71db73f7b`, `workspace/assurance/core-reliability-phase-a.md` and `workspace/assurance/core-reliability-phase-a-crosswalk.json`.
- First independent review V = `e0d89fe0e369fa427712653b2ef87e5493091381`, `workspace/review/core-reliability-phase-a-independent-review.md`.
- Initial mandate = `3dcdcb4bf883bcf570cba1867e49792be9cb179d`, `workspace/assurance/core-reliability-phase-a-handoff.md`.
- Source/spec/code/test basis **T = `da70c929be95b98aba773a9225ec70ba13d50c31`**. At handoff creation, live main **M=T**; independently recheck live main and material drift. PR #510 remained open/unmerged at the last check; do not credit its assertions on T unless separately landed.

All of A, Q and W are branch-transient artifacts based on main; **GitHub's branch-to-branch compare can show sibling additions rather than a meaningful content diff**. Compare the exact fetched A report against Q report directly. Check crosswalk blob identity instead of mistaking an added copy on A's transient branch for a changed crosswalk.

**Fail closed:** First resolve this handoff by its branch + *full immutable handoff commit SHA* + exact path. If not available, return `INPUT BLOCKED — HANDOFF UNAVAILABLE`. If A's two files at exact SHA/blobs, Q or W are unavailable, report `REVIEW INCOMPLETE — MISSING SOURCE` and the exact failed coordinate. Do not replace absent exact sources with chat summaries, copied snippets or mutable branch content.

## 2. Repository grounding and review scope

1. Read `AGENTS.md`; search `docs/` for assurance, invariants, evidence, coverage, frontier, sequence, use target, applicability, rollback, conformance, oracle, independent review and testing diagnostics. Read `docs/development/testing.md`, `docs/development/reviewing.md`, `docs/architecture/runtime-contract.md`, `docs/architecture/governance-evidence.md`, relevant Governance, Factory and Research specifications, and precise implementation/test paths **at T**, with any changes on live M checked explicitly. Follow the repo's authority-by-subject rules: implementation is evidence of behaviour, not unilaterally the desired contract.
2. Read A, Q, W and the Step 1 commission **in full**; read the earlier acceptance mandate and enough R/V source to independently judge historical disposition. Do not simply reuse A's author-reported checks or W's probes as your own. Locate RR-1..RR-5 in all pertinent amended matrix, method, campaign, owner-decision, challenge-guide, provenance and verdict sections.
3. Record the independent context and review method. Before accepting W/A's explanation, independently derive the expected mathematics/subject semantics of the two consequential findings. Small purpose-built probes are allowed where indispensable; source inspection and bounded falsification are preferred to a duplicate full campaign.
4. Distinguish evidence types: *implemented/source-static*, *retained assertion (inspected, not rerun)*, *W's executed probe*, *new independent probe*, *mathematical/oracle reasoning*, *proposed future experiment*, *unresolved owner semantic decision*. No Java test suite, JaCoCo, mutation campaign, complete model exploration or CI pass can be inferred from report text.
5. Verify that A's report/crosswalk differ from Q only by intended report amendments, the crosswalk remains byte-identical and T-bound (300 Java paths, nine modules and 33 mapped groups), and no production/test/spec files were changed on the transient Step 1 branch. Prior independent checks already established overall mechanical mapping: a narrow blob/path identity check is sufficient unless a real contradiction appears. Escalate only when supported by a precise defect. Recheck `main` immediately before the final verdict.

**Examination scope:** RR-1..RR-5, U10/U11, their affected E-EVENTS/R-CAPTURE/R-DERIVE/G-CONFORMANCE/G-EVIDENCE/G-OCCURRENCE/G-REVISION seams, associated B/L/M and Governance oracle recipes, Phase B campaign-entry/exit rules, independent Phase D challenge, and textual consistency with current U1–U9 blockers. Do **not** redo every unchanged contract or campaign from scratch, do not run Phase B, and do not import hypothetical product interfaces not currently shipped. You may identify a new materially adjacent finding with a precise counterexample, but do not invent a finding quota.

## 3. Acceptance test RR-1 — both `EvidenceWindow.missingWithin` successor failures

**Known observations at T:** `afterSequence + 1` wraps when caller supplies `after = through = Long.MAX_VALUE`, including on a complete window with ordinary final cursor f=0 or 5, producing spurious missing or an exception. `runFinalSequence + 1` wraps at f=MAX and can also cause false missing sets/duplicates. W's appendix includes an executable `FinalOnlyFixSurvivorProbe` that guards only the final-cursor increment; that partial fix passes the previous explicit U10 table yet misses the caller-side defect.

Independently test that A's proposed relation correctly models the Javadoc: for `0 <= a <= b <= MAX`, missing membership is `{s | a < s <= b and (s > f or s is in a declared gap)}`, under legal-shaped metadata. An empty query must be empty even past frontier. Verify intersection/open-closed boundary math without ever computing a sentinel `MAX+1` or needlessly enumerating a huge range. Identify any assumption about gap validity, read identity, gap duplication or range normalization not actually promised by the record/API.

Try to **break** A's oracle and explicitly check:
- `(MAX,MAX]` on f=0 empty window; f=5 complete without gap; f=5 with gap [2,3]; and f=MAX complete. All mathematical missing sets must be empty even if current implementation returns spurious ranges or throws.
- f=MAX with `(0,MAX]` and `(MAX-1,MAX]`, f=MAX-1 with a post-frontier singleton, and f=5 with a gap on `(0,5]`, `(2,5]`, `(3,5]`.
- A's **joint** f × after × through design; separate successor-fault controls; reported endpoint/exact membership versus raw duplicates and thrown exception; `DeclaredEvidence.completeEvents` present-empty versus absent, `missingEvents`, and R-DERIVE use/refusal.
- The final-only-fix survivor must be detected by at least one explicitly enumerated case with **ordinary f** and `(MAX,MAX]`; no ambiguity from changes to U3 producer event-horizon policy can excuse it. A helper query argument is a separate domain from runtime-produced event count.
- Constructed legal-shaped `EvidenceWindow` instances and a true runtime-produced capture must remain distinct. Do not demand 2^63 emitted events or treat synthetic extreme metadata as proof of reachability.

**Pass:** both sites, correct independent relation, plausible future controls with precise expected outcomes, consumer consequences and a separate owner decision about query admitted domain are explicit in every affected family. **Fail:** the final-only survivor can pass A's controls, a control is mathematically wrong, or a relevant claim silently relies on U3 to solve caller overflow.

## 4. Acceptance test RR-2/U11 — rollback and evidence-use target binding

Review the exact governance-evidence §4/§6/§8–9 terms: independent use-target revision/claim/temporal applicability, source identity, controlled-revision history, role semantics and prohibited automatic transfer on rollback. Inspect `EvidenceConformanceEvaluator.validateUses`, `EvaluationOccurrenceDraft.validateUses`, `InMemoryEvaluationOccurrenceAuthority.verifyUseRevisions`, `EvidenceUse` and the retained `GovernanceEvidenceTest` tests at T.

W's bounded probe used **R1(F1) → R2(F2) → R3(F1)**; an **APPLICABLE relied-on use** targeted (F1,R1) can return PASS for subject (F1,R3), return PASS for revisionless F1 and appear in an accepted R3 occurrence. Code checks that each present target revision binds to the use's own fingerprint and non-comparator fingerprint matches subject; it does **not** check that the use revision ID equals the subject revision ID. The retained precedent intentionally allows a **revisionless use for a revision-bound subject**. This observation is not a blanket proof that every re-used use is false evidence, nor a settled "must reject" policy.

**Attempt to falsify A's U11 treatment:**
- Check A labels **observed behavior** and **plausible contract contradiction** separately; it must not silently equate matching model fingerprints to matching historical revisions or treat a cited speculative mutant as evidence of a guard the code already has.
- Verify the matrix and Governance truth tables discriminate target revision absent, equal, same-fingerprint but *different*, and present on revisionless subject; compare non-comparator and comparator roles, relied-on versus excluded, applicability sufficiency and actual occurrence retained use context. Preserve the revisionless-use precedent.
- Determine whether every expected outcome is **deferred to the owning semantic decision**, except source-pinned behavior and existing unambiguous contracts. The author cannot choose "refuse", "PASS" or "reusable by fingerprint" as the universal normative oracle.
- Examine the actual alternatives: strict binding for a **present** target revision versus more permissive fingerprint-level targeting with **independent per-target** correspondence/time/claim/applicability and traceable basis. Does either alternative specify what would make rollback non-transfer verifiable and how candidate absence of accepted revision remains distinct?
- Verify Governance slices 1–2 and the final claim table cannot close use-binding/non-transfer while U11 is unresolved, but adjacent Storage/revision/other Governance work is not blanket blocked.
- Check cross-authority occurrence U4 remains distinct from U11; do not manufacture a global rollback-transaction obligation.

**Pass:** U11 can be handed to the owning decision-maker without ambiguity, and the planned independent truth table/occurrence ledger is implementable **after** the owner chooses a rule. **Fail:** accepted source behavior is misrepresented, expected refusals are invented, no binding boundary is specified, or the affected campaign can silently declare U11 green.

## 5. Acceptance test RR-3 — sensitivity controls discriminate rather than mask

Inspect the baseline cases and A's new mutation/control table:
- For f=5 and gap [2,3], the expected sets for queries `(0,5]`, `(2,5]` and `(3,5]` are respectively [2,3], [3,3] and empty.
- On f=MAX, a current duplicate [MAX,MAX] may make an "ignore gaps" mutant appear **closer to correctness**. The earlier purported gap and exclusive-lower-bound controls could be masked by baseline overflow.
- A must separately classify **known existing defect observations**, **hypothetical final-only-fix negative control**, **post-remediation mutation checks**, and **valid ordinary-frontier controls**.
- Test each nominated mutation for an actual independently expected behavioral difference, respecting preconditions. A baseline failure, setup/compile/test-discovery failure, equivalence or timeout must never count as a semantic mutant kill. A corrected passing baseline is a prerequisite for *post-fix* fault-sensitivity claims.

**Pass:** the future campaign can demonstrate sensitivity to each intended fault independently, not merely count failing cases. **Fail:** a claimed control is killed by the wrong existing defect or lacks a semantic oracle.

## 6. Acceptance test RR-4 — read-support hull and sufficiency

Read `DeclaredEvidence.completeEvents` and `EvidenceSupport` at T. Check that two successful reads `(0,2]` and `(5,7]`, with unretained gap [3,4] and a suitable independent frontier/basis for *each* read, may yield recorded hull **[1,7]** without establishing interior [3,4] continuity. Validate A's instructions for empty reads, failed reads, separately sufficient reads, hull min/max and analytic derivation sufficiency.

**Pass:** R-CAPTURE/R-DERIVE and adjacent campaigns compare the hull as a support *record*, not an evidence-completeness proof. **Fail:** the hull is implicitly used as a contiguous event trace, accepted proof frontier, or evidence of missing events magically obtained.

## 7. Acceptance test RR-5 — independent challenge of Phase D verdict and evidence

Review A's final Phase D protocol and the Step 1 change rather than re-running Phase D. Adversarial scenario: the proposed final-verdict challenger previously executed/selected/triaged survivors for a B campaign or C remediation and then reviews the same family's frontier, declares its own survivors equivalent, and accepts the claim. Does A **require** disclosure, detect that conflict, prohibit adjudicating that family and reassign it to an independently qualified challenger without accepting a missing-review gap? Check the *combined* acceptance/qualify/rework verdict and responsibility map at immutable synthesis/evidence/review SHAs. A fresh session or different model family alone is not proof of independence.

Inspect secondary scenario: changed final main, altered oracle version or lost generator input must trigger drift/reexecution or a blocked claim rather than a green transfer; A's earlier protocol was adequate on that point, and Step 1 must not regress it.

**Pass:** every family with a confidence claim has an identifiable independent challenger who was not its evidence author, and conflicting authorship cannot be waived by a label. **Fail:** a self-review path remains or conflicted/unreviewed families can still receive unqualified acceptance.

## 8. Cross-passage coherence, narrowness and procedural custody

- Compare the substantive delta **A vs Q directly**. Search all affected sections for contradictory surviving promises or "suspected" misclassification: matrix rows (E-EVENTS, R-CAPTURE, R-DERIVE, G-CONFORMANCE, G-EVIDENCE, G-OCCURRENCE), §4 B/L/M/Truth Tables, §5 campaign entry/exits and Phase D, §6 U10/U11 and owner decisions, §7 readiness and exclusions.
- Ensure A is still a **design**: no claimed completed Java tests/JaCoCo/CI/Phase B; 300-file crosswalk and 33 groups still T-bound; original U1–U9 uncertainties and known U8 catalogue alias remain visible; no unsupported runtime history or global transactional claim. No new 34th group needed for U11.
- Read the actual earlier acceptance-review mandate at `eb3d310c...` and note what W did/did not follow. This Step 2 addendum should **explicitly identify itself as following the current Step 2 mandate and addressing the prior provenance discrepancy**, without inventing historical compliance. If an earlier essential acceptance expectation is still wholly untested because W skipped it and the present narrow scope cannot defensibly close it, name the *exact missing acceptance assertion* and state whether the final disposition must be incomplete. Do not restart the whole 33-group assessment as a reflex.
- Inspect A's provenance that no source/spec/issue/CI changes occurred on Step 1 branch and that `main` still represents the T-bound domain or has explicit drift. Distinguish observed implementation defects U1/U2/U7/U8/U10, plausible semantic conflict U11, static U3 horizon, optional U4/U6 policy extension, and no full-suite execution U9.
- Review procedures from `docs/development/reviewing.md` inform evidence quality and independence, but **this is not a formal PR Reviewer review** and must not post a merge `disposition` or claim PR readiness.

## 9. Reviewer report and verdict contract

For **each RR-1 through RR-5**, report `ACCEPTED`, `ACCEPTED WITH NARROW LIMIT`, or `OPEN`, with:
- exact A passages and authoritative T source/spec/test sites inspected;
- at least one *independent* attempted counterexample, mathematical check or explicit invariant per consequential change; cite W's prior probes as *prior* evidence;
- specific falsification outcome, whether the current packet would detect a wrong future implementation, and any unexamined limits;
- what remains *owner decision* vs *known implementation remediation* vs *future test campaign*;
- if open, actionable smallest patch to the **design**, not a new repo-wide mandate.

Specifically disposition U11's owner-decision independence and U10's two sites. Capture any new consequential finding as `S2-1`, `S2-2`, etc., severity `BLOCKING` / `MATERIAL` / `NOTE`, exact counterexample and source/contract, claim/campaign impact and smallest revision. Do not require a minimum number of findings.

Produce an immutable report containing: reviewed and historical exact branch/commit/blob/path identities; source T vs live M drift; independent session declaration and evidence-class method; narrow review coverage and what you *did not* re-check; RR-1..RR-5 disposition matrix; U10/U11 entry/closure judgment; original-handoff provenance reconciliation; any new actionable findings; and **one** final verdict:
- **`PHASE A DESIGN ACCEPTED — CONTRACT BLOCKERS EXPLICIT`**: the amendment controls are sound and complete as a **design**, while the named owner semantics, real defects, actual campaign evidence and Phase D final assurance remain open. This may be used to organize *later* owner decisions and independent B slice commissions, not to start B under this review.
- **`TARGETED DESIGN REWORK REQUIRED`**: a material flaw remains in RR-1..RR-5, U11, the dependent passages, or an essential earlier acceptance criterion. Name the smallest correction and whether the next review can remain narrow.
- **`REVIEW INCOMPLETE`**: exact required artifacts/source cannot be inspected or material main drift invalidates the T-bound review. Identify precisely what blocks a defensible judgment.

On a separate line always report **`PHASE B AUTHORIZATION: NO`**; no review result silently authorizes campaigns, selects open contract meanings, declares defects fixed, certifies reliability or promotes readiness. Include a concise *owner next-action* map: if accepted, prioritize specific owner-decision packets (including U8, U11, U3/U10 etc.) and possible independent B slices without starting them; if rework, identify exact offending paragraph/control and who should amend it.

Give the deletion/continuity verdict required by `AGENTS.md`: after your exact review report is committed, explicitly say whether all unique findings/reproduction evidence have durable-enough transient custody for another session, or what remains uncaptured.

## 10. Authorized output and Git custody

This review is **read-only toward all reviewed artifacts and product/policy repository surfaces**. On a **fresh, isolated review-only branch from live `main`**, commit a single substantive report at:

`workspace/review/core-reliability-phase-a-step2-acceptance.md`

If a tiny independent repro is essential, place exact literal commands/source and observations in a report appendix, not an uncommitted script required by the verdict. Ephemeral diagnostics go in ignored `logs/`. Never edit Q, A, W, V, R, existing handoff commits or the assessment crosswalk; do not touch product Java, tests, specifications, docs, CI, research registers, PRs or issues. No implementation PR for a workspace artifact; `workspace/` must not enter `main` or any merge candidate. Do not merge branches or delete branches. Use the repository owner's human Git author/committer identity and no agent/tool/session attribution footer.

At the end return **review branch + full immutable review commit SHA + exact report path**, source A/Q/W/T/M, headline RR findings, one design verdict, **`PHASE B AUTHORIZATION: NO`**, unresolved owner decisions/unknown evidence, exact next action and session-continuity verdict. You are authoring *the independent acceptance addendum*; no separate review of your own report and no implementation work is delegated by this handoff.
