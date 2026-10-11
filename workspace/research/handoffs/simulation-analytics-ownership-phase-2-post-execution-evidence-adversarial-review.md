# Adversarial research-review handoff — analytics ownership Phase 2 after execution-evidence reconciliation

## Assignment and fixed target

Act as a **genuinely independent Arcogine Researcher in adversarial-review mode** in a new session, following live `AGENTS.md`, `.github/agents/researcher.agent.md`, and `docs/development/researching.md` §9. Your task is to **try to falsify the load-bearing recommendations** of one exact, high-risk analytics ownership report; it is not to continue writing that report, implement analytics, choose Game mechanics, or reconcile architecture.

**Report under review (immutable target):**

- Repository: `alaiba/arcogine`.
- Workspace: `workspace/simulation-analytics-ownership-phase-1`.
- **Exact report SHA:** `d2125f39482de3306f2f56e9c7261a3dde160b33`.
- **Exact report path:** `workspace/research/investigations/simulation-analytics-ownership-phase-2-post-execution-evidence-revision.md`.
- Its research baseline and final recheck: `a7f9856617d99e796d528a44aa8c15677c485f55` (PR #469 included).
- Reviewer handoff path: `workspace/research/handoffs/simulation-analytics-ownership-phase-2-post-execution-evidence-adversarial-review.md`.
- Intended **independent reconstruction checkpoint**:
  `workspace/research/investigations/simulation-analytics-ownership-phase-2-post-execution-evidence-review-reconstruction.md`.
- Intended **review artifact**:
  `workspace/research/investigations/simulation-analytics-ownership-phase-2-post-execution-evidence-adversarial-review.md`.
- Research-local additional probes, **only if discriminating**:
  `workspace/research/experiments/simulation-analytics-ownership-post-execution-evidence-review/`.

**Stop immediately with `INPUT BLOCKED — ORIGINAL REPORT NOT AVAILABLE`** if you cannot resolve the report at that exact SHA and path. Never reconstruct an absent report from the handoff, its predecessor, or a chat summary; never issue a verdict on a substituted revision.

The research question is whether, under the *already adopted* execution-evidence and corrected Engine contracts, an independent analytics semantic owner is justified **now**, or whether an explicitly staged, kind-specific ownership rule is better supported. The report recommends **`RECOMMEND STAGED/NO SHARED OWNER NOW`**, with a consumer-local default, a concrete shared-owner admission test, and a separation of definition ownership from physical evidence custody. That recommendation is **not** adopted architecture until genuinely independently reviewed and later reconciled.

This is the existing analytics ownership question (`READY`) and the existing workspace. **Do not create a new investigation, PR, or research track by default.** Existing execution-account semantics are `CONCLUDED`; Game diagnostic remains separately `READY`; the analytics evidence/provenance question remains `CANDIDATE`.

## Evidence coordinates: maintain review identity and prior dispositions

The following are sources to verify at their exact SHAs, **not** presumed truths or earlier approvals:

| Input | Exact revision | Path and role |
|---|---|---|
| Reviewed Phase 2 report | `d2125f39482de3306f2f56e9c7261a3dde160b33` | `workspace/research/investigations/simulation-analytics-ownership-phase-2-post-execution-evidence-revision.md` — **the sole reviewed target** |
| Its independently pre-registered expectations | `07ec4020e39274e49f690db302376d20c9f0e06b` | `workspace/research/investigations/simulation-analytics-ownership-phase-2-post-execution-evidence-preregistration.md` — expectations committed before probe code |
| Its new probes and two post-removal ports | `ac0ac2aa603f82426d7d2c0ec88f3485b9927908` | `workspace/research/experiments/simulation-analytics-ownership-post-execution-evidence/` |
| Previous Phase 2 revision | `75736dddfa3fe1ae89c5089720c702f4ca595ab5` | `workspace/research/investigations/simulation-analytics-ownership-phase-2-admission-boundary-revision.md` — **never independently reviewed** |
| Original Phase 2 report | `6a5c043563e3bd89fae6826f830b377b43643d46` | `workspace/research/investigations/simulation-analytics-ownership-phase-2-admission-boundary.md` |
| Independent review of original Phase 2 | `2c85fe7c03c6256c9e8a2ce73fde5091b3357e3c` | `workspace/research/investigations/simulation-analytics-ownership-phase-2-adversarial-review.md` — **REOPEN only for original report** |
| Phase 1 current-ownership checkpoint | `7e72a46506bf63febcb57d7b7dcc9ce0cd484d64` | `workspace/research/investigations/simulation-analytics-ownership-phase-1-current-boundary.md` |
| Game diagnostic report | `4ad00136441fa29e6b462ac5431b60cf2de1e148` | `workspace/research/investigations/factory-design-game-diagnostic-evidence-report.md` on `workspace/factory-design-game-diagnostic-evidence` |
| Independent Game diagnostic review | `6484795ff4c4ad971e06c70751b03805e54a55f5` | `workspace/research/investigations/factory-design-game-diagnostic-evidence-adversarial-review.md` — **ACCEPT WITH QUALIFICATIONS** |
| Execution-account / Engine adaptation | Original and adversarial reviews referenced by the target report §1 | Background only; **live** `docs/architecture/runtime-contract.md` and `engine-semantics.md` now own the accepted semantics |

Do not conflate the `REOPEN` of `6a5c0435` with a disposition on `75736ddd` or `d2125f3`. Do not infer that the target inherits independent acceptance because its author addressed the previous review's challenges. Record which older findings still constrain the new revision.

At handoff preparation, live `main` and target report's final recheck matched:
`a7f9856617d99e796d528a44aa8c15677c485f55`. **Re-resolve live `main` yourself** at review start and finish, inspect changed files/PRs since that SHA, and report whether any new change undermines a current-state inference. Branch tips are mutable; commit-plus-path identifies review evidence.

**Preserve all original artifact history.** Append review and any new evidence on the same analytics workspace when practical; never amend, rebase, reset away handed-off evidence, force-push or overwrite any previous report/review. A separate reviewer branch is permissible only for a concrete isolation need; branch separation alone does not establish independence.

## Independence and anti-anchoring sequence — mandatory

First state whether you are genuinely independent: different researcher/session (prefer a different model family where practical), no responsibility to defend the report. If not, disclose **self-administered review**; do not label the result an independent adversarial acceptance.

Carry out these stages **in this order**:

1. **Verify identity only.** Fetch the target report at `d2125f3`; inspect opening metadata, question and completeness, but do **not** read its executive recommendation, candidate winner or self-challenge in depth yet.
2. **Ground from live authority.** Read `AGENTS.md`, researcher procedure, research method and the registered analytics brief. Quick-search `docs/` for the question's key terms, then read canonical runtime/Engine semantics and Game/Governance requirement surfaces before trusting report interpretations.
3. **Independently reconstruct.** Produce a short checkpoint of the question, current authority/consumer boundaries, real candidate ownership models, and hand-derived adversarial/failure cases **before reading the report's analysis deeply**. Persist it at the reconstruction path above, preferably as its own immutable commit. Include what evidence would support keeping definitions consumer-local versus Engine/shared ownership.
4. **Then read the entire report**, pre-registration, probes, original `REOPEN` review, prior revision and Game evidence/review. Compare what the new revision actually claims to your independently reconstructed candidates; don't merely score whether it followed its handoff.
5. **Falsify selectively.** Independently derive expected values/refusals from the accepted contract and test targeted cases that can *change* a load-bearing conclusion. Explain both successful challenges and challenged claims that survived.
6. **Recheck live `main` and evidence custody**, then commit a separate review artifact bound to **this exact report revision**.

The report author's self-challenge (§15) is a candidate attack list, not independent evidence and not a substitute for a fresh test.

## Canonical repository sources and a bounded absence search

At the *review's* live-main SHA, read:

- `docs/research/research-register.md`;
  `docs/research/investigations/simulation-analytics-consumer-boundary.md`;
  `docs/research/investigations/simulation-analytics-evidence-provenance.md`;
  `docs/research/investigations/simulation-execution-account.md`.
- `docs/architecture/runtime-contract.md` — especially "Captured execution evidence and interval determinacy", E5 claim-scoped coverage, E6 half-open finality, and "Responsibilities and limits".
- `docs/architecture/engine-semantics.md` — §§1.1–1.2, §4 rule 3, §10–10.2; `docs/architecture/overview.md` — domain authority, current headless observation and Determinism Contract.
- `docs/product/charter.md` §6; `docs/architecture/factory-design.md` §10; `docs/architecture/governance-evidence.md` §§5–7, 11–13; `docs/planning/factory-design-game-consumer.md` and `factory-design-game-vertical-slice.md`; the Game diagnostic research brief and independently reviewed evidence.
- Current `FactoryRuntime`, `FactoryHandler`, `RuntimeObservation`, `RuntimePerformanceObservation`, `ResourceObservation`, command/advancement implementation and relevant Factory/Research conformance tests, only where a load-bearing claim depends on them.
- `docs/development/testing.md` §10 for research-local probes and the test-run boundary.

Search **`docs/` first** for analytics admission/owner, utilization, bottleneck, method identity, evidence custody, E5/E6 frontier, command/fault provenance, `busyTicks`, R-remove, output clock, Game diagnostics, verification objective, comparability and cross-revision. Then inspect directly relevant product and test call sites. Do not treat a workspace research proposal as normative Engine behavior.

**Concrete consumer verification:** Confirm that the Game diagnostic evidence identifies two forcing questions (period utilization and idle-flow characterization) but no completed playable live-loop implementation; the illustrative "maximum utilization <= threshold" verification objective has not already become an evaluated, basis-specified core use; Governance analytical evaluation is not silently promoted from fixtures; no current outward adapter creates a second concrete contract. If anything has changed, name the specific path/source and reassess ownership accordingly. An *unimplemented* consumer is not equivalent to no consumer demand, but anticipated reuse or two code call sites is not a shared-method requirement.

## Essential attack surfaces (use after independent reconstruction)

Prioritize the following because they can change the ownership conclusion or a normative qualification. These are **questions to challenge**, not predecided findings.

### A. Separate semantic definition ownership from evidence custody — including promotion

The old Rule 7 ("evidence custody follows the definition owner") contradicts the landed runtime contract, which distinguishes controller, holder and analytical-definition owner. The new report replaces it with custody following capture opportunity and the use's horizon, while definition owners specify exact sufficiency.

- Can one exclusive driver/holder supply multiple independent named calculations without transferring custody or the meaning of either method? Verify that the two-call-site P6 example proves **possibility**, not a full cross-party capture/handover contract.
- Does keeping identical arithmetic and declared inputs suffice to preserve meaning on promotion, or must the **proof class, basis, exact proof frontier, definition identity, producing model/revision, no-future-input attestation, attestor/scope and trust restrictions** also travel? Which are always required versus claim-specific?
- Can a holder lie or omit fault/command provenance while preserving gap-free supported changes? Would a shared definition silently upgrade a controller's private commitment into producer evidence?
- Could evidence be held by a non-owning delegate and a definition remain pure/stateless? Conversely, is there any accepted requirement that makes custody intrinsic to the definition? An actual counterexample to one side matters more than class/module placement.
- Challenge any general **one-way/no-rework promotion** promise. If it depends on proof provenance supplied from the beginning, require the exact conditional, not an unconditional architectural benefit.
- Neither holding events nor sharing a formula automatically justifies a new Engine ledger, analytics module, persisted account, universal bundle type or reusable capture service.

### B. E5/E6 interval sufficiency: correct by proof, not by plausible numbers

Independently derive the accepted half-open interval `[a,b)` contract and its alternatives before relying on the probe's helper. Check its treatment of:

1. A basis observation at or before `a`, correct `RunId`, initial state and sequence basis.
2. Gap-free supported changes through the **relevant proof sequence**, checked against an independently known authoritative cursor, not simply local consecutiveness.
3. A first held supported change at time `>= b` (prefix through `K-1`, witness retained), a supported observation at time `>= b` with the required captured cursor, or an attributable controller closure bound to post-advancement `(RunId,S*,T*)` and clean work exhaustion/input exclusion. Distinguish those proofs; neither an arbitrary target `b`, `QUIESCENT`, nor a spent budget suffices.
4. Refusal over an invalid stale frontier (the five-tick job whose naïve captured tail becomes 100), with preservation of earlier proved `[0,5)` despite later gaps.
5. A state change exactly at `b`, equal-time sequence, jobs active across a boundary, and the late joiner's inability to recreate pre-join processing history after `busyTicks` removal.
6. Claims that require only a fresh observation, a fully captured interval, attested no-future-input, command/fault history, producing-definition provenance or an entire deterministic script. **Do not silently require all categories for every measurement.**

Try to falsify an **incorrect acceptance**, not only demonstrate conservative refusal. In particular test whether the research-local `prove()` can accept an insufficient claim when observations/basis/changes are mixed, inconsistent or tampered with, and whether its observation-frontier requirement is too strong for a narrow proof without creating a false positive. Call out whether a counterexample is to the **report's normative assertion** or only to a research-only helper.

Reconstruct one counterexample that separates an **honest controller** (who can attest to its own command/fault history) from what a passive party can verify from a supported change stream. A reported `Faulted` outcome or rejected/no-op command need not generate a supported change; complete supported changes are not a fault-free or all-commands certificate. Do not re-open the already-reviewed execution-account architecture unless you discover a genuine new falsifier that changes its established E5/E6 contract.

### C. Game demands: current-state classification versus future-horizon claims

Challenge whether the report accidentally makes **all live diagnostics** dependent on interval closure:

- "No currently known eligible work" based on **one current supported observation** is not the same claim as "no work will arrive before `b)" or "no work left for the rest of `[a,b)`". A future-horizon no-work-left conclusion may require scoped controller input exclusion; a current-state classification may not.
- The P4.3 closed/open pair intentionally has identical supported prefixes/observations before future input. Check whether its result truly discriminates the **claimed interval classification**, and avoid generalizing it to a snapshot-only statement.
- Examine whether the flow oracle's **empty initial jobs**, always-online/single-product/release-at-once assumptions and availability/known-work choices are stated as research-local scope rather than universal Game semantics.
- Confirm the Game's prior independent review qualifications: idle-with-eligible-work-queued third state (or refusal), capped versus uncapped starved slot-ticks, pool/eligibility basis, availability treatment, running work at boundaries, and limits on causal wording. Do not promote the research-local formulas into production Game definitions merely because tests pass.
- Does the Game actually fix the basis choices that would make a named measurement owner meaningful? Check whether Game-as-default semantic owner is a **recommendation** awaiting Game reconciliation, not a live product rule.
- T1 mid-run commands and current corrected command time are separate from T2 time-targeted idle inputs and T3 non-driver interval finality. Do not turn unchosen live-loop options into present Engine or analytics requirements.

### D. Engine alternatives versus consumer-local/shared definitions

Review all genuine contenders in the report — A (standardized Engine measure), A′ (execution-fixed cumulative aggregate), B (universal outside Engine), C (shared owner now), D (staged admission), E (kind-specific classification), F (custody-led semantic ownership), and G (decoupled holder/stateless definitions).

Require the same evidence threshold for each:

- **Implementability versus architectural suitability:** the Game can calculate a number from events; what, if anything, must the Engine guarantee? Can an Engine-owned execution-fixed aggregate avoid duplicated error-prone protocols and serve later consumers with reasonable compatibility, or would its chosen normalization/method restrict legitimate alternatives?
- **A′ versus retained/shared delivery:** an Engine running-occupancy accumulator could replace **historical retention for a defined fresh-observation use**, not interval finality, complete command provenance or arbitrary historical windows. It is proposed/specification-derived, **not** demonstrated by running product code. Require explicit arithmetic/overflow, observation and compatibility obligations; avoid rejection merely because `busyTicks` was just removed.
- **A/B limits:** one existing Engine rate at supported time (`throughputPerTick`) differs from a Game period rate; that is a *definition and time-basis* difference, not evidence the Engine rate is erroneous. A universal ban on Engine measurement may be unjustified, but whether it is **logically falsified** by a hypothetical future late join, versus merely overrestrictive on current evidence, must be stated correctly.
- **C/shared owner now:** one bounded **core verification/governance requirement with acceptance evidence** can justify a shared owner even without two shipped consumers; inspect whether any such requirement actually exists and fixes the same named method. Conversely, the mere existence of a possible utilization threshold or two call sites is not a shared-definition contract.
- **F/G:** code factoring and physical holder arrangement are not themselves semantic ownership verdicts. Distinguish candidate models that select a different responsibility from those that merely permit an implementation.
- Are the prior `REOPEN` review's strong alternative models and information-loss input-scope qualifications actually met, rather than recategorized to make the preferred staged answer win?

A fair finding may be **viable but not now**, **viable with cost**, **falsified**, **selected** or **more evidence required**. Do not turn current lack of a requirement into a permanent architectural prohibition.

### E. Current-code drift, accepted outputs and downstream research triggers

Verify PR #465's R-remove state, #466's execution-evidence contract, and #469's removal of `busyTicks` against **current main**. The new report claims:

- No current Engine-produced resource utilization, cumulative busy-time, occupancy or bottleneck measure; preserved `backlog`, `completedOrders`, `completedSalesValue`, `averageLeadTime`, `throughputPerTick`, plus `combinedQueueDepth` as result-affecting ranking. Confirm exact arithmetic/projection and avoid mistaking public method existence for a supported output contract.
- `throughputPerTick` is over **observed supported time**, not an arbitrary analyst-selected interval; a requested `advanceUntil(100)` does not force time 100. The report's P7 (0.2 versus completed-per-100 of 0.01) is not itself an Engine defect.
- The old `busyTicks`-dependent experiments no longer compile, while the ported tests intentionally exclude those reads; verify they preserve original discriminators without silently changing baselines or expected claims.
- A late joiner cannot necessarily recover machine processing residence before its basis from present observations; that is information loss accepted with #469, **not** proof that it must be recovered by one specific Engine method.

Challenge the **evidence/provenance `CANDIDATE` trigger** carefully. The maintained brief permits promotion on **concrete additional supported inputs, method-specific sufficiency, bounded accumulation, analytical-definition/result provenance or required retention**, not only when a result crosses from a driver/holder to a passive verifier. Is the report's reliance-by-a-nonholder formulation genuinely a sufficient restatement of those needs, or does it silently exclude a concrete same-holder use that the separate brief already admits? Distinguish a permissible precise refinement from an unjustified narrowing; do not trigger a new investigation on hypothetical reuse.

Similarly check adapter compatibility and cross-revision comparability remain conditionally open. PR #465 changed outcomes for specific budget-stopped scripts; PR #469 removed a field. They create **a concrete comparability risk**, not automatic proof that every result across revisions is incomparable.

## Testing and falsification discipline

The report records **154 research tests** (137 tracked + 17 research-local) and **46 selected Factory conformance tests**, all passing at `a7f9856`. Treat this as an **author claim to verify proportionately**, not as your own independent execution or as proof of the ownership recommendation.

- Verify the pre-registration existed **before** probe code (exact chronology `07ec4020` → `ac0ac2aa` → `d2125f3`), that expected values/refusals match actual assertions, and that the two post-removal ports did not silently rewrite a load-bearing older expectation.
- Independently hand-derive at least the important witness/refusal and Game distinctions. If practical, rerun only the relevant suites with the documented Java 21/Gradle `-PresearchExperimentSources` workflow, plus accepted Factory conformance necessary for a finding. State full commands, environment, counts and limitations; don't claim reruns that did not happen.
- If there is a material gap, create one or more **pre-registered, research-local** reviewer probes under the review-specific experiment path. Do not edit production code, accepted conformance, original report/pre-registration or the author's probe files. Commit any new expectations **before running** the new probe and identify exact SHA. Keep negative controls (refusal) and positive witness cases.
- An analytical helper may be deliberately conservative or deliberately trust a controller attestation. Falsify the **load-bearing claim** it supports; do not mistake a conservative false negative for a false positive, or a knowingly false controller promise for proof of an Engine-produced guarantee.
- Avoid full-suite reruns or a broad literature review unless a specific failing comparison demands it. External analogies do not decide Arcogine ownership.

## Review report and disposition

Commit a **separate, completed** independent adversarial-review document at:

`workspace/research/investigations/simulation-analytics-ownership-phase-2-post-execution-evidence-adversarial-review.md`

It must contain:

1. One of **`ACCEPT`, `ACCEPT WITH QUALIFICATIONS`, `MORE EVIDENCE REQUIRED`, `REOPEN`** prominently at the top, with exact reviewed report SHA/path, reviewer's live-main baseline and independence statement.
2. The **pre-report reconstruction** commit/path and what candidates/failure cases were derived *before* reading the report recommendation; disclose any anchoring that was unavoidable.
3. The report's decisive claims in the reviewer's own words — no rubber-stamped paraphrase.
4. A claim-by-claim **adversarial matrix**: challenged assertion, independently derived counterexample/test or source, observed fact, disposition of that assertion and effect on the overall recommendation.
5. Explicit rulings on custody versus definition, E5/E6 proof/refusal, Game current-versus-horizon semantics, A/A′ and shared-owner alternatives, current fields, and the separate evidence/provenance trigger.
6. For successful probes, the actual output and expected outcome; for blocked probes, the missing premise, unavailable fixture or specification limitation. Label executable current-code evidence versus a proposed Engine alternative or architectural preference.
7. What survives from Phase 1, which original `REOPEN` findings are genuinely repaired, and whether anything materially new forces a change in the D/E/G recommendation. No inherited acceptance from the unreviewed intermediate revision.
8. The **minimum reconciliation consequences and durable qualifications**, **only if** the conclusion survives. State where to place the owning policy (architecture/overview or runtime contract), what must be corrected in the Game consumer plan, whether any Engine semantics/Determinism Contract change is justified, and which analytical-provenance/adapter/comparability triggers remain genuinely conditional.
9. A final live-main recheck and custody map: immutable report, preregistration, probes, reconstruction, additional reviewer probes (if any), and review commit/path; any material drift, unsupported absence claim or test-suite limitation.
10. A precise **next action**: one bounded reconciliation if ACCEPT/qualified; an exact missing discriminator if MORE EVIDENCE REQUIRED; or a **focused** question/alternative requiring report revision if REOPEN. Do not commission another broad research programme merely because a design has future options.

A qualified acceptance is meaningful only if the conditions are actionable and preserve the report's essential decision. If a challenger disproves a load-bearing invariant or changes owner placement, choose REOPEN or MORE EVIDENCE REQUIRED rather than laundering it as an editorial qualification. A clean ACCEPT is valid: do **not** invent objections to avoid it. The review cannot approve unexecuted future Game contracts or an unimplemented A′ API.

**No implementation, canonical `docs/` edits, research-register state change, decision adoption, reconciliation PR or merge.** The registered analytics ownership question remains `READY` until a later independently reviewed durable reconciliation lands. Its dependent evidence/provenance question remains `CANDIDATE` unless separately promoted by a concrete accepted reason. The Game diagnostic remains separately owned and must not be silently marked `CONCLUDED`.

## Final handoff / stop boundary

Preserve original reviewed report and all past evidence commits. Commit the review (and any reviewer reconstruction or small research-local probes) to `workspace/simulation-analytics-ownership-phase-1`, verify exact paths and SHAs, and return:

- **Branch** and **exact independent-review commit SHA and path**;
- **exact report reviewed:** `d2125f39482de3306f2f56e9c7261a3dde160b33` plus its path;
- reviewer independence mode and live-main baseline/final recheck;
- independent reconstruction commit/path, any new test/pre-registration commits/paths and the actual results;
- the adversarial disposition and decisive falsifiers/qualifications, with their impact on ownership and downstream triggers;
- the narrow next action (reconciliation versus a named blocker).

Do not paste the full review or this handoff into chat. A chat conclusion is not a committed review. **Do not merge or modify the report being reviewed.**
