# Independent adversarial review — Core Reliability Assurance Phase A

## Assignment and binding

You are an **independent, adversarial assurance-design reviewer** for Arcogine. Your job is to test whether the completed Phase A assessment is sufficiently **complete, faithful to current supported contracts, falsifiable, and executable** to underpin a future comprehensive core-reliability campaign. Your job is NOT to confirm the author's effort, implement tests, fix product bugs, rewrite architecture, independently review an implementation pull request, or declare the Java core reliable.

Review the following immutable assessment artifacts as a **single packet**:

- Assessment branch: `codex/core-reliability-phase-a-assessment`
- Assessment commit R: `6bc1e1ff5f11f7aff525761a027ec3b71db73f7b`
- Report: `workspace/assurance/core-reliability-phase-a.md`
- Machine-checkable crosswalk: `workspace/assurance/core-reliability-phase-a-crosswalk.json`
- Assessed main T: `da70c929be95b98aba773a9225ec70ba13d50c31`
- Original Phase A commission, for acceptance intent (not proof): branch `handoff/core-reliability-phase-a-20261022`, commit `3dcdcb4bf883bcf570cba1867e49792be9cb179d`, file `workspace/assurance/core-reliability-phase-a-handoff.md`.

At review-prompt creation, T was still live `main`, PR #510 was open/unmerged with initial exploratory assessment and branch-only Scheduler test improvements, and issue #514 had closed after the JSON Unicode-escape correction landed. These observations are dated context; recheck live state. Do not credit an unmerged change as existing on T, or a closed issue as proof that adjacent contract families are correct.

**Important known state:** the Phase A author explicitly returned `INCOMPLETE`, primarily because certain admitted input domains and acceptable arithmetic/oracle outcomes lack contract closure. Your review must distinguish (a) the quality/completeness of the *design as a design*, (b) unresolved specification or evidence prerequisites, and (c) actual readiness to authorize Phase B. A sound inventory does not turn `INCOMPLETE` into a pass. Conversely, do not reject the whole packet simply because it honestly records blockers; evaluate whether they are correctly localized and whether further serious gaps are missing.

## Start-of-run controls

1. Resolve **this handoff** from its supplied branch + full commit SHA + path before proceeding; fail with `INPUT BLOCKED — HANDOFF NOT AVAILABLE` if it cannot be fetched. Resolve assessment report and crosswalk at the **same exact R**; confirm both exist, check their content hashes if useful, and confirm R is the requested assessment branch identity. If either is unavailable or inconsistent, stop with `REVIEW INCOMPLETE — SOURCE UNAVAILABLE`. Do not reconstruct either from chat or from a mutable branch tip.
2. Resolve live `main` and record its SHA M; read `AGENTS.md`, `.github/CONTRIBUTING.md`, `docs/architecture/overview.md`, `docs/development/testing.md`, and the owning specifications **at T**, not silently from M. Search `docs/` for the material request terms (reliability, coverage, invariant, identity/canonicalization, failure/refusal, state, determinism, recovery, evidence, conformance, parser, test diagnostics). Read other needed domain architecture/specification and source/test files on T. Read `docs/development/researching.md` and the specialized research/reviewing guidance for useful independent adversarial method, but **do not assume this is a registered Research investigation or a PR Reviewer disposition**; neither lifecycle may be mutated or claimed.
3. Compare M and T before assigning conclusions. If M advanced, analyze whether changed contracts, source, tests or architecture make the T-bound design stale. Review the *historical T-bound assessment* honestly and list required refresh points; do not silently apply new code to old claims. If substantive drift prevents a meaningful verdict, return `REVIEW INCOMPLETE` with precise affected paths.
4. Review in an **independent execution context**, not by continuing the Phase A author's analytical session. Take a blind or author-agnostic first pass: reconstruct major semantic invariants from T's canonical contracts and implementation, and list potentially catastrophic failure modes **before** relying on the packet's risk ratings, completeness claims, proposed tools, or U1–U9 explanations. You may use the packet to navigate, but its prose is untrusted hypotheses, not normative authority.
5. This is **not** a mandate to run Phase B tests. You may use small independently authored, bounded static calculations, single counterexample experiments, source diffs or isolated controls when necessary to establish whether a material review concern is real. Label each observation as static reasoning, executed probe, existing retained test or unresolved inference; capture exact reproduction. Do not claim unexecuted campaigns or green CI. Any exploratory code/logs belong in ignored `logs/` or review-workspace custody, never product main sources.

## Core adversarial review passes

### Pass 1 — Independently verify inventory and authority

- Reconstruct the Java module/source-set list from `product/settings.gradle.kts` and `git ls-tree -r T product`. Check the claimed 300 Java files (188 main, 111 tests, one JMH), nine declared modules, exact file path-set and Git blob-hash equality against the JSON crosswalk. Check every file has applicable contract IDs, including the non-shipped research substrate, test-only modules, package descriptors, nested declarations and proof fixtures. Distinguish physical file completeness from semantic-contract completeness.
- Crosscheck **all 33 claimed contract groups** against actual owning spec sections and real code/test entry points. Search for consequential current contract sentences without a matching group/failure model, missing code that is materially exposed, duplicated or misassigned authority, and a false assumption that a test proves more than its assertions.
- Independently inspect high-impact producer/consumer seams, including: typed time and IDs into scheduler/Factory; Factory publication→canonical bytes/verification→Storage and governed revision; Scheduler→Engine→events/observations→research evidence; ChangeSet→Governance applicability/conformance/evidence occurrence; Challenge JSON→admission/economics/evaluation/comparison; Factory versus Challenge independent validity; architecture module boundaries.
- Verify that present implementation and historical/non-shipped/deferred capabilities are not conflated. Evaluate how the assessment treats Java source contracts, public adapters, exceptions, invalid drafts, internal state and unsupported caller behavior; do not invent promised exception or support semantics.

### Pass 2 — Break the failure-model matrix

For every **critical** boundary and consequential cross-boundary seam, attempt to identify at least one plausible contract-violating implementation fault that could pass the *planned* experiments or be classified out of scope. For **high/supporting** groups apply risk-proportionate examination, with explicit rationale for any thinner examination. Prefer concrete witnesses over generic demands for “more tests.”

Challenge, as applicable:
- Arbitrary long values, signs, overflow/underflow, precision, equality/identity aliases, UTF-8/UTF-16, Unicode scalar validity, binary framing and canonical rejection.
- Same-time ordering, local/shared dispatch fixpoints, queue side effects on refusal, overflow during cascades, advancement budgets, faulted transitions and post-fault event/observation coherence.
- Canonical-vs-equivalent representations, document vs published identity, duplicate/missing references, provenance, occurrence/revision non-rebinding and historical resolution.
- Validator error ordering, compound-invalid diagnostics, permission/refusal paths, accepted-but-partially-mutated state, correction/redelivery, controlled revision and evidence-acceptance transaction boundaries.
- Storage commit-point ambiguities, corruption/missing distinction, cleanup with shared artifacts, failed initialization and *documented* reopen/concurrency/failure behavior.
- Challenge policy arithmetic, inclusive limits, malformed schema/JSON, identity/cost/offer aliasing, downstream rejection and cross-domain misuse.
- Research oracle completeness/frontier/finality, unsupported evidence, capacity occupancy arithmetic, nonindependent oracle helpers and static guard bypasses.
- False metamorphic relationships (renaming, permutation, capacity increases, split advances) that alter meaning under the actual contract.

For each credible survivor, identify **contract section + source path/member + failure trace or precise mutant + which proposed technique/oracle misses it + smallest design correction**. If a counterexample needs a contract decision, distinguish that from an implementer bug. Inapplicability must be justified by supported semantics, not convenience or absence of a current test.

### Pass 3 — Audit oracle independence and method feasibility

- Identify exact relations and *independently derived expected results* for all chosen differential, model-based, finite-enumeration, metamorphic and property techniques. If an oracle shares the encoder, comparator, parser, helper, fixture producer, runtime path or arithmetic under test, describe the common-mode fault it misses and propose a genuinely independent gold/vector/model or complementary control.
- Verify every intended generator is constrained to **admitted valid inputs or deliberately invalid boundary classes**, with a shrinker/minimizer that preserves preconditions and reproduces the fault. Validate test invalidation/survivor interpretation: a mutant compile failure, missing test discovery, assertion failure only in setup, timeouts and environmental caps do **not** prove detection of a semantic fault.
- Critique bounded model-state coverage: what states/transitions are explored, what high-order interactions are pruned, whether visited-state equivalence or symmetry reduction might erase contract distinctions, and what evidence frontier remains after a budget. No unbounded state-space or universal reliability claim is permitted from finite exploration.
- Determine whether proposed techniques are operationally credible under current JDK 21/Gradle/source-set conventions and supported test seams. Flag tooling/resource prerequisites, unsafe assumptions about installed tools or performance budgets, and where a simpler test gives the same discrimination. Estimates must be stated as unknown if not measured.
- Require **exit criteria by contract/failure family**, not number of pilots, test count, coverage percentage, mutation score or fuzz quota. A case counts as closed only when the oracle is credible, controls discriminate plausible faults, consequential survivors are explained, observed discrepancies are triaged, and residual limitations are explicit.

### Pass 4 — Independently examine U1–U9 and missing blockers

For each documented U1–U9, classify it as `CONFIRMED CONTRADICTION`, `PLAUSIBLE / UNTESTED`, `CONTRACT DECISION REQUIRED`, `EVIDENCE UNAVAILABLE`, or `NOT A GAP`; categories can overlap with reasons. Verify the cited spec, Java input domain, code path and consumer before concluding. Do not simply endorse the table.

Explicitly examine:
- **U1** signed-domain `SimTime.minus`/saturation and supported Engine time.
- **U2** iteration order of invalid eligible-resource diagnostics vs claimed determinism and canonical sorting.
- **U3** Scheduler/Engine long sequence-counter exhaustion versus representable supported state horizon.
- **U4** Governance occurrence refusal versus side effects in the separate evidence-reference authority.
- **U5** Challenge comparison delta arithmetic vs broad constructed-record/admitted-attempt domains.
- **U6** JSON surrogate, duplicate-key and resource policy — *do not invent parser guarantees*.
- **U7** shared research `ProcessingIntervals` accumulation and occupancy representability.
- **U8** catalogue item-ID text encoding and possible loss of distinct accepted identities before fingerprinting; distinguish representational aliasing from SHA collision.
- **U9** lack of freshly executed tests/JaCoCo at T and the epistemic limit of a source-only design.

Search proactively for **novel U10+** gaps rather than treating the above as an exhaustive checklist. Verify whether apparently severe findings are actually reachable and within supported contracts. Where a resolution requires an owner decision, phrase the smallest decision and the exact boundary it affects; do not quietly rewrite the specification or claim a defect without repro/contract support.

### Pass 5 — Audit campaigns, dependencies and final-baseline transfer

Assess the proposed Phase B domain campaigns end to end. For each, verify completeness against all relevant contract IDs/failure families; source/entry points; independent oracle; negative controls; reproducible commands/seeds/bytes; issue triage; bounded resource/frontier policy; retained regression test rules; smallest reviewable implementation slices; and cross-campaign dependency edges.

Check that remediation in Phase C requires rerunning the failed family and adjacent consumer oracles, not only a new green unit test. Check that Phase D evaluates a **fresh exact main**, harmonizes only evidence valid for that revision, addresses drift and unresolved consequential findings, and obtains *independent challenge of the assurance verdict*. Temporary diagnostic tools should not become permanent gates or dashboards without separate justification. The reviewed design must **not** imply new support, architecture, semantics, product identity, filesystem guarantees, or a release-readiness certification.

## Findings and verdict contract

Produce a review report that is useful for correcting the Phase A design, not a congratulatory summary.

For each material finding include:
- unique review-local ID, affected contract group(s), source/spec/test locations, classification and consequence;
- reproducible evidence (exact lines/rule or minimal controlled witness) and why the existing Phase A design would miss or misclassify the issue;
- precise minimal action: correct a matrix row/campaign/oracle, add a missing contract/failure family, verify an empirical claim, or request a bounded owner decision;
- whether it blocks **Phase A design adequacy**, a later **Phase B campaign start**, or eventual **Phase D assurance**; distinguish severity from priority and do not invent a numeric confidence score.

Use `BLOCKING` for an omission/invalid oracle/unsupported claim that makes the scope unsuitable for sound review or execution, `MATERIAL` for significant uncertainty that needs explicit design/owner treatment, and `NOTE` for verified minor corrections or limitations. A known open domain blocker can be accepted as accurately identified **without** authorizing Phase B on that boundary.

Report:
1. source coordinates (R/T/M), independent reading/verification method, exact inventory path/blob result and any missing evidence;
2. the evidence-backed findings with explicit novel-vs-confirmed-vs-dismissed provenance, plus a disposition of U1–U9;
3. matrix/technique/campaign challenge coverage and any **unreviewed** high-risk boundary;
4. assessment of reproducibility, feasibility, independence and estimated remaining work;
5. a verdict from: `DESIGN ADEQUATE; BLOCKERS EXPLICIT` / `REWORK DESIGN REQUIRED` / `REVIEW INCOMPLETE`;
6. separately, `PHASE B AUTHORIZATION: NO` by default; a different recommendation only if the reviewed design and every blocking admitted-domain/oracle prerequisite is actually closed on the applicable current main and the owner explicitly authorizes it. The review itself never authorizes execution.
7. precise requested revisions/questions and the next owner action. Distinguish an accepted design *with explicit blockers* from a completed reliability baseline.

Be willing to conclude the design is sound but semantically blocked, or that it needs rework, or that the review could not be completed. No mandatory finding count; do not manufacture defects to appear adversarial.

## Custody, changes and return

- Create a **separate review-only branch from live main** (not the assessment author's branch) and commit one substantial but navigable report at `workspace/review/core-reliability-phase-a-independent-review.md`. This is a **transient review artifact**, not a PR/merge candidate or a permanent file under `docs/history/`. Keep any truly necessary reproduction notes under the same review workspace; prefer integrating concise evidence into the report.
- Leave the reviewed report and crosswalk at R **unaltered**. The assessment author or owner makes revisions separately after this review; do not self-amend the object being reviewed or claim independent validation of your own repairs. Do not edit Java product, tests, architecture, testing policy, research register, GitHub issues/PRs or the Continuity Improvement checkpoint as a side effect of this task.
- Follow `AGENTS.md` attribution rules: the human repository owner's Git identity, no model/provider trailers or AI attribution blocks. No auto-merging or branch deletion. Do not run the tracked-workspace merge-candidate check expecting success on this intentionally workspace-bearing review branch.
- At the end, recheck source availability and material `main` drift. Return exactly the **review branch + full immutable review commit SHA + review path**, the R/T/M coordinate, verdict, key blocking findings, gaps in examination, and the recommended next step. Do **not** begin Phase B.

Success means **independent discrimination of serious omissions or justified adequacy**, not a passing word count, a “thorough review” label or automatic agreement with Phase A.
