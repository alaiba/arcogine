# Independent acceptance review — corrected Core Reliability Assurance Phase A design

## Mission, independence, and stopping rule

Act as a **fresh, independent assurance-design reviewer** for Arcogine. Evaluate whether the **corrected Phase A design** is sound, complete enough to serve as a future comprehensive core-reliability assurance plan, faithful to current supported contracts, and adequately responsive to the prior adversarial review. You did not author the original Phase A design, its first independent review, or the rework. Independence requires separate analytical execution; branch separation alone is not proof.

**This is an acceptance/rework decision about a test and assurance DESIGN, not an endorsement of implementation correctness.** The desired outcome is a defensible verdict, not automatic acceptance. Do not run Phase B, remediate product defects, decide unsettled contract semantics for the owner, assert certification, or alter an existing assessment/review artifact. An inadequate design must receive a reasoned rework verdict even though the owner calls this an "acceptance review."

The owner seeks the strongest *practically demonstrable* reliability evidence for the retained Java core after pruning/reconciliation. A satisfactory design must map materially consequential implemented semantics and their realistic failure classes to credible, preferably independent oracles, executable diagnostic families, bounded reproducibility and fault-disposition criteria; subsequent Phase B/C/D execution remains necessary. No arbitrary pilot count, mutation percentage, branch-coverage threshold or generated-case quota constitutes completion.

## Immutable sources — resolve these first

Repository: `alaiba/arcogine`. Read the **exact Git objects** below; do not use branch tips or conversation summaries as substitutes:

**Subject of this review — corrected Phase A packet Q**
- Branch: `codex/core-reliability-phase-a-rework`
- Commit Q: `260a9c9ddf29eeddf10f56192cb06ffaf9120a4d`
- Assessment: `workspace/assurance/core-reliability-phase-a.md` (Git blob `caecbacea9e8e01be8924998ce61499ebccd0d50`)
- Source/proof crosswalk: `workspace/assurance/core-reliability-phase-a-crosswalk.json` (Git blob `d1015aa98b398616981eaba46c9335359b09a6d8`)

**Original assessment R**
- Branch: `codex/core-reliability-phase-a-assessment`
- Commit R: `6bc1e1ff5f11f7aff525761a027ec3b71db73f7b`
- Same two `workspace/assurance/` paths; original assessment blob `aa7f469431ccf9c5f735d305baa96a651221dd33`, original crosswalk blob `d1015aa98b398616981eaba46c9335359b09a6d8`.

**Original independent adversarial review V**
- Branch: `codex/core-reliability-phase-a-independent-review`
- Commit V: `e0d89fe0e369fa427712653b2ef87e5493091381`
- Path: `workspace/review/core-reliability-phase-a-independent-review.md`
- Git blob `5c640ed6005895264a2066a7256bd5bb6c6f2e3a`.

**Original commission and rework authority**
- Initial mandate: `handoff/core-reliability-phase-a-20261022` @ `3dcdcb4bf883bcf570cba1867e49792be9cb179d`, `workspace/assurance/core-reliability-phase-a-handoff.md`.
- Rework mandate: `handoff/core-reliability-phase-a-rework-20261022` @ `aaf6b9f78e2456b803e869bc68130cdbc5971e02`, `workspace/assurance/core-reliability-phase-a-rework-handoff.md`.
- Prior review mandate (method/context): `handoff/core-reliability-phase-a-independent-review-20261022` @ `acf1b8088d17df33651359ce3f080f7182a5643f`, `workspace/review/core-reliability-phase-a-independent-review-handoff.md`.

**Code/spec assessment basis T**: `da70c929be95b98aba773a9225ec70ba13d50c31`. This was still live `main` when Q was authored and verified; re-resolve current `main` independently. At creation of this handoff PR #510 was still open/unmerged (branch-only exploratory baseline and Scheduler assertions), and issue #514 (Challenge JSON escape defect) was closed after its fix landed on T. Recheck live status rather than relying on these observations.

If this very handoff cannot be resolved by its **handoff branch + immutable commit + file path**, stop `INPUT BLOCKED — HANDOFF UNAVAILABLE`. If any subject packet member at Q is unavailable, has unexpected blob content or cannot be bound to T, stop `REVIEW INCOMPLETE — SOURCE MISMATCH`; report the exact missing coordinate. Historical R/V sources must be read for an acceptance judgment; disclose and stop if an essential source cannot be inspected. Do not reverse-engineer lost content from the chat.

## Repository grounding and reviewer posture

1. Resolve live `main` SHA M and compare it to T. Record every materially changed contract, Java source/test, tool or architecture path if M differs. Review what Q *actually claims about T*, not an unmarked mixture of T and M. Identify required future refresh before Phase B if M has advanced; a large consequential drift can make the review `INCOMPLETE`.
2. Read `AGENTS.md`, `.github/CONTRIBUTING.md`, `docs/development/testing.md`, `docs/architecture/overview.md`, and the relevant owning domain architecture/specifications at T (plus current versions where drift matters). Search `docs/` for core reliability, invariants, canonicalization, determinism, evidence, refusal, recovery, time/cursor, correctness and testing diagnostics. Source/code/tests, not the rework author's explanations, are evidence for implementation behavior. Current owning specs govern current semantics. An open issue/research hypothesis is not a change in supported meaning.
3. Use `docs/development/reviewing.md` and relevant independent-review/research guidance for adversarial method, but do **not** post a formal PR Reviewer disposition or start a registered Research/Consistency lifecycle. This review targets a transient assurance-design packet, not a merge candidate.
4. Read Q, R, V and both relevant mandates **in full**, including tables and review reproductions. Independently reconstruct the most consequential contracts/failure modes from T before accepting Q's claimed dispositions. The review must not be a checklist-only confirmation that new paragraphs exist. Propose a credible counterexample if a key test oracle, campaign closure rule or supported-scope claim is unsound.
5. Label each claim **static source/spec reasoning**, **independently executed bounded probe**, **retained existing test assertion**, **historical reported experiment**, **unresolved inference** or **planned future diagnostic**. A design review is not a Java-suite execution. Small isolated reproductions are permitted where decisive; do not let optional probes turn into Phase B. If execution is unavailable, say so and reason from inspectable evidence.

## Review pass A — verify provenance, completeness and change isolation

- Verify Q's two packet files at exact SHA/blobs and compare to R. Confirm the crosswalk remained byte-identical to R, or precisely explain any deviation.
- Independently verify the file-set/blob correspondence against `git ls-tree -r T product`, the nine modules from `product/settings.gradle.kts`, the asserted 300 Java paths (188 main, 111 tests, one JMH) and all 33 contract IDs with nonempty mappings. File custody is **necessary but not sufficient** for semantic coverage.
- Verify that the rework changes are confined to intended assessment-design content and actually propagate to affected matrix rows, experiment recipes, campaign dependencies, owner-decision table, review guide and verdict. Search *every relevant occurrence* of old terminology (`suspected`, `before scope`, `baseline complete`, `Phase B ready`) that could contradict corrected claims; preserve legitimate historical quotations.
- Check major producer/consumer seams: typed time/identity → Scheduler/Factory; Factory publication/canonical bytes → Storage/Governance history; Engine events/observations → research capture; Governance evidence uses → conformance/occurrence; Challenge catalogue/JSON/admission/evaluation/attempt; cross-domain publication versus admissibility. A working 33-ID map does not rule out a consequential missing semantic statement. If a new omission appears, identify its exact owning source and plausible missed fault rather than restarting the entire source survey.

## Review pass B — test closure of the original findings

**A1: final Phase D verdict independence — mandatory blocking criterion.**
- Verify that Q specifies *who* independently challenges the final assurance synthesis, *which immutable evidence* they must inspect, how every contract/failure-family closure, residual/exclusion, test discovery, mutant survivor, resource frontier and common-mode oracle is interrogated, and how drift from old campaign SHAs to final exact main is handled.
- Try this falsification: a campaign stops at a resource frontier, retains only successful cases, and promotes the family as fully covered after implementation PRs and all ordinary tests pass. Does Q's protocol demonstrably require a separate reviewer to detect and reject the unsupported claim? How are lost scratch files, generator changes, absent oracle definitions, failed controls, stale classfiles or branch-only passes classified?
- Acceptance needs explicit `ACCEPT / QUALIFY / REWORK` possibilities for the eventual Phase D review, review-artifact identity, blocking-finding reconciliation, owner-controlled qualified publication, and the distinction between historical record and retained executable proofs. No standing dashboard/gate or extra product support promise is required.

**A2 / U10: last-representable evidence cursor — mandatory semantic validity check.**
- Verify that E-EVENTS, R-CAPTURE, R-DERIVE, the B/L/M method families, both affected Phase B campaigns, and owner-prerequisite table cover cursors `0,1,MAX-1,MAX`, empty/post-frontier/partial/gapped windows, successor arithmetic and downstream `DeclaredEvidence.completeEvents`.
- Independently derive the missing-set relation for query `(after,through]` from `EvidenceWindow`'s current Javadoc, `SequenceRange`, package contract and supported runtime frontier; test the proposed oracle's assumptions about `startSequence`, `endSequence`, `runFinalSequence`, declared missing gaps and query bounds. Distinguish **valid-shaped constructor inputs**, **consistent declared windows**, and **actual runtime-produced evidence**. A constructed `MAX` metadata witness is not proof that `MAX` events can be emitted/captured.
- Attack the proposed endpoint-pair algorithm: check the difference between mathematical missing-set equality and incidental order of returned ranges, safety of conversion back to inclusive `long` endpoints without `MAX+1`, overlap/duplicate gaps, and how empty windows or unsupported query preconditions are handled. Try mutants that change one endpoint, delete post-frontier missing, ignore a gap or wrap the successor. Does a planned control actually discriminate each?
- Decide whether the linked U3 event-sequence horizon/prerequisite is stated without silently deciding unbounded runtime support or general evidence-authority semantics. If oracle or control is mathematically wrong, the design still needs rework.

**A3: confirmation, consequences and correct scope of U1–U10.**
For each entry independently recheck its evidence class, contract authority, input-domain admissibility, affected code, required owner decision (if any), campaign entry/exit and prohibited final assurance claim:

- **U1** `SimTime.minus`: confirmed signed-value Javadoc contradiction, but not a demonstrated negative-time Engine failure; total-value arithmetic and >MAX mathematical difference need authority.
- **U2** Factory invalid eligible-set diagnostics: fresh-JVM nondeterministic *diagnostic order*, not runtime dispatch or canonical identity; require a defined ordering and discriminating validation oracle.
- **U3** Scheduler insertion/public Engine event sequences: unchecked finite `long` horizon; reason about potentially unbounded public events with drain and no retained-work bound; do not imply trillions-of-commands run. Link U10 and post-state semantics at exhaustion.
- **U4** Governance occurrence rejection versus earlier registration in a distinct evidence-reference authority: avoid inventing global rollback/transactionality.
- **U5** Challenge comparison cost/margin deltas: concrete overflow on *constructed* records, not automatically admitted pipeline attempts; comparison input domain and exact/refuse representation need owner boundary.
- **U6** JSON legal escaped UTF-16, duplicate keys and resource caps: do not invent scalar rejection, duplicate-key errors, arbitrary depth or general interoperability. Verify integer-vs-fraction parsing claims and keep Challenge catalogue text separate.
- **U7** extreme supported-shaped occupancy: observed `ArithmeticException`, not silent returned wrong totals; shared `ProcessingIntervals` cannot independently corroborate the two occupancy consumers. Define admitted area/exact/refuse choices.
- **U8** accepted unequal item IDs collide *before hashing* through lossy UTF-8 replacement: verify accepted-content alias, consumer relevance and owners' text admission/encoding choice, without claiming all downstream swaps are admitted or a SHA-256 collision.
- **U9** no matching full test suite/JaCoCo measurement at T: an honest Phase A evidence limit, not a baseline pass.
- **U10** constructed complete `EvidenceWindow` at `MAX` has incorrect missing range; boundary helper contradiction and source-horizon decision are related but not identical.

Check the reviewer-disposition table in Q for every A1/A2/A3 finding and the G-CONFORMANCE correction. Confirm specifically that `ConformanceEvaluator` and `EvidenceConformanceEvaluator` evaluate matching subject/assertion/revision before the not-applicable scope shortcut where implemented, while `EvidenceConformanceEvaluator.validateUses` follows the shortcut. Refuse an unconditional malformed-use rejection rule that is not in the owning contract. Challenge a supplied `UNKNOWN` judgment, excluded evidence, comparator-only evidence, verified revision and mismatched subject in the proposed truth tables.

Distinguish **owner decision needed** from **separate confirmed defect remediation**, **bounded probe**, **specific missing oracle**, and **explicit non-support**. An owner decision must not be silently chosen by the reviewer or deferred by declaring the entire plan complete.

## Review pass C — try to falsify the revised *design*, not merely the edits

For every critical contract group/cross-boundary seam, sample a serious fault that could survive the *proposed* technique portfolio, especially where the chosen oracle shares helpers/fixtures with production or the planned state exploration prunes high-order interactions. Apply proportionate scrutiny to high and supporting groups; identify any unreviewed group explicitly.

Check that:
- planned generator domains are admitted or deliberately invalid, and exact success/rejection/**post-state** oracles exist;
- canonical byte vectors, interval algebra, model transitions and BigInteger results are independently derived rather than code-under-test restatements;
- meaningful mutants have compilable behavioral controls with nonzero test discovery, not setup-only failures; equivalent/unreachable mutants and timeouts never become kills;
- finite exhaustive exploration records transition/visited-state keys, pruning/frontier and interactions without collapsing semantic distinctions; larger seeded traces do not masquerade as exhaustive proof;
- temporal/identity/availability transformations are genuinely metamorphic under the owning contract (Factory list order, signed IDs and command timing may be semantic);
- Storage failure injection/reopen verifies the supported commit point and shared artifact preservation without importing power-loss guarantees;
- Governance attribution distinguishes content identity from occurrence/reference/definition; Challenge validation, policies, catalogue binding and research evidence distinguish their authorities;
- per-family exit requires diagnostic execution and credible oracle controls, not a pilot count/coverage score; phase C requires rerunning the failed family plus adjacent consumer oracles, and Phase D requires a new exact-main and separate verdict challenge;
- each important unresolved owner choice blocks *its affected assurance claim* without unnecessarily blocking independent unrelated slices. Any Phase B sequencing recommendation remains **advisory** until the owner commissions it.

Do not demand every conceivable method at every class or a new generic testing framework. Demand the most discriminating **practically feasible** complementary methods for material failures, justified exclusions and honest residual bounds.

## Findings, judgment and return contract

For each actionable remaining finding include: local ID, severity `BLOCKING` / `MATERIAL` / `NOTE`, affected contract or phase, source/spec/test/packet locations at T/Q, concrete counterexample or independently checkable reasoning, why existing Q controls fail, smallest correction or owner decision, and which later phase/family is blocked. Explicitly disposition *all* prior A1/A2/A3 plus G-CONFORMANCE and U1–U10, even if accepted as resolved. If a prior finding was narrowed or genuinely inapplicable, explain why. Proactively include any new U11+ threat with evidence; do not manufacture a minimum finding count.

Produce a concise but substantial review report with:
1. exact handoff/Q/R/V/T/M identities, current branch/PR dependency facts and any relevant drift;
2. independent inventory/blob check, critical-seam/contract and assertion/oracle inspection method, and specific **not reviewed** surfaces;
3. A1/A2/A3, U1–U10 and G-CONFORMANCE disposition matrix; significant novel findings;
4. verdict on final Phase D verdict protocol, mathematical cursor oracle, phase B/C campaign completeness, residual risks and adequacy of owner-decision scoping;
5. **ONE explicit design verdict**:
   - `DESIGN ACCEPTED WITH EXPLICIT CONTRACT BLOCKERS`: the Phase A design is sound enough to plan campaign-specific future work; all unclosed owner decisions, known implementation defects and evidence-frontier limits are visible. This verdict is not certification or blanket permission to run B.
   - `REWORK DESIGN REQUIRED`: any material omission, unsound independent oracle, improperly asserted guarantee, incorrectly disposed reviewer finding or unusable campaign exit condition remains. Name concrete fixes, not vague requests for more work.
   - `REVIEW INCOMPLETE`: essential evidence could not be inspected or current code/spec drift prevents a defensible verdict. Name exactly what is missing.
6. Separately write **`PHASE B AUTHORIZATION: NO`**, regardless of design acceptance, because no owner authorization/contract-resolution transition is delegated to this reviewer. If accepted, list bounded campaign-specific decisions/repairs, what could proceed independently upon subsequent owner commissioning, and the smallest next handoff. If rework is required, identify the next revision and need for another independent review. Never mark the *code* "core reliable" in Phase A.
7. Explicit deletion/continuity verdict under `AGENTS.md`: whether all unique review findings/probes have been committed and a new session can safely continue from immutable coordinates; otherwise name missing custody.

## Authorized write surface and provenance

Create a **new review-only branch from live main M**, not from Q, R, V or a PR branch. Persist a single coherent independent acceptance review to `workspace/review/core-reliability-phase-a-acceptance-review.md`. A second supporting file in the same workspace is allowed **only** for a necessary self-contained minimal probe; prefer report appendix with exact commands and literal inputs. Temporary scratch outputs belong in ignored `logs/`. Keep product source/tests, specifications, `docs/history/`, maintained development policy, CI/tooling, research status, GitHub issues and PRs unchanged.

Do **not** edit the reviewed Q assessment or its crosswalk. Do not open a merge PR for workspace material (the repository forbids tracked `workspace/` on main and merge candidates), merge anything, delete branches or post a formal PR-review disposition/checkpoint comment. Recheck M and target availability before conclusion; if M advanced materially, disclose the affected claims.

Commit report with the repository owner's human author/committer identity, with no AI/session attribution. Return **review branch + full immutable review commit SHA + report path**, Q/R/V/T/M coordinates, headline findings, explicit design verdict, `PHASE B AUTHORIZATION: NO`, known unexamined surfaces and the next requested owner action. This review itself is the decision-quality artifact; no implementation or assurance baseline is established by its existence.
