# Security ownership and assurance boundary — independent adversarial review

> **Artifact type:** Independent adversarial research review (`docs/development/researching.md` §9)
> **Disposition:** **`ACCEPT WITH QUALIFICATIONS`**
> **Reviewed report:** `docs/research/investigations/security-ownership-and-assurance-boundary.md` at commit `ef4130a13fb68749563a642126a75ecd449dab7b`
> **Report's stated research baseline:** `950a19742e811918a2312d8ef6fdf0ec129761d3`
> **Live-`main` review baseline:** `ebab62a71de99d3d171be6291ce5ebf6807f5895`
> **Research-evidence workspace:** `alaiba/awesome-edison-y28lc5`
> **Authority:** Research evidence only. This review neither promotes nor blocks anything by itself; it records whether the reviewed report's load-bearing conclusion survives an independent attempt to falsify it.

## Report input integrity

The complete report revision was resolved before substantive report-specific work: `git cat-file` confirms `ef4130a1` is a commit in this workspace, and `git show ef4130a1:docs/research/investigations/security-ownership-and-assurance-boundary.md` returns the full 364-line document. `INPUT BLOCKED — ORIGINAL REPORT NOT AVAILABLE` does not apply.

The reviewed revision was not amended, rebased, or force-pushed by this review. This review is a new commit in the same workspace.

## Independence condition actually achieved

**Condition 2 (`docs/development/researching.md` §9): a fresh isolated session/run with no responsibility for preserving the report's conclusion.**

Condition 1 (a different researcher, person, or model family) **cannot be asserted**. The repository records no authoring model for the report, and this reviewing run cannot establish one. This run is configured as `claude-opus-5`; the served model may differ and is not itself evidence about the report's author. If the report was authored by the same model family, only condition 2 was achieved.

`docs/development/researching.md` §9 accepts condition 2 as the minimum for a high-risk review. Sharing the workspace branch does not weaken independence; this run began from live `main` and the repository's own authorities, not from the report.

### Anchoring-control sequence actually followed

1. Resolved live `main` (`ebab62a7`) and recorded it as the review baseline.
2. Read `AGENTS.md`.
3. Read `docs/development/researching.md` in full and `.github/agents/researcher.agent.md` in full.
4. Read `docs/research/research-register.md` — **the register carries no security question at all**, at the report's baseline or now. The question arrived bounded by its handoff.
5. Re-grounded in `.github/SECURITY.md`, `.github/CODEOWNERS`, `.github/dependabot.yml`, `.github/workflows/ci.yml`, `docs/development/reviewing.md`, `docs/development/testing.md`, `docs/architecture/operational-execution-digital-twin.md` §16, `.github/agents/pr-reviewer.agent.md`, `.github/agents/dependency-maintainer.agent.md`, and `.github/workflows/continuous-improvement.yml`.
6. Read live GitHub-side controls directly from the REST API before reading the report's own reading of them.
7. Independently reconstructed candidate ownership models and proving cases (below).
8. Only then read the report's reasoning and recommendation in depth.

**Disclosed compromise of anchoring control.** Two exposures preceded the deep report read and could not be avoided:

- the handoff prompt states the conclusion to attack as an explicit "attack map";
- the report's own commit message, visible in `git log` while resolving the artifact, summarises the conclusion.

Both are properties of the handoff design, not of this run's sequencing. The candidate set in the next section was written down before reading the report's `## Candidate models / hypotheses`, and it contains one candidate the report does not state as such — which is some evidence that the reconstruction was not purely an echo.

`docs/research/synthesis-seeds.md` was **not** used as grounding and did not shape this review.

## Candidates independently reconstructed before deep report reading

- **R-A — distributed ownership only.** Security is a cross-cutting property of existing owners: consequential semantics with Operational, conformance with Governance, exposure surface with the interface/application owners, and process/supply-chain with PR review, the dependency maintainer, CI, and the repository control plane. `.github/SECURITY.md` is the only security-specific surface. No new structure.
- **R-B — a cross-cutting security assurance owner.** A durable responsibility-holder for threat modelling, readiness criteria by exposure class, and assurance evidence routing.
- **R-C — a standalone Security track/module/architecture surface**, potentially owning shared identity/authn/authz.
- **R-D — split control plane from product.** Name the repository/CI/supply-chain control plane as an explicitly owned surface (today implicitly the single maintainer), and leave product security semantics with Operational/interfaces.
- **R-E — R-A plus corrections to existing surfaces only.** No new owner *and* no new document: every gap is repaired inside `.github/SECURITY.md`, `docs/development/testing.md`, and `docs/development/reviewing.md`, which already exist and already have authority over their respective subjects.

Proving cases derived independently, before reading the report's:

1. A CRITICAL dependency CVE surfaces on the unattended daily scheduled scan with no open PR — who acts?
2. An external party submits a sensitive vulnerability report — what is the actual path from receipt to closure?
3. An operator follows `.github/SECURITY.md`'s hardening list and exposes the service — is the resulting posture what the document implies?
4. A candidate PR authors a workflow whose job is named `disposition` — does the documented CODEOWNERS mitigation actually stop it?
5. Arcogine is hosted and multi-user but performs no actuation — who owns authentication, authorisation, and per-principal isolation?
6. Untrusted or independently authoritative external data is ingested — who owns parser/resource safety as distinct from interchange semantics?
7. Candidate-controlled PR content is an input to the agent review that produces the `READY TO MERGE` authorisation token — who owns that?

Case 7 has no counterpart in the report's set; cases 1–6 correspond closely to the report's cases 1, 9, 3, (control-plane), 4, and 5. The convergence on 1–6 was independent and is itself mild corroboration that the report's proving-case set is not idiosyncratic.

## Live evidence gathered by this review

All GitHub-side values below were read during this review against live state, not taken from the report.

| Surface | Live value | Method |
|---|---|---|
| Repository | `alaiba/arcogine`, `visibility: public`, `allow_forking: true`, `has_issues: true` | `GET /repos/alaiba/arcogine` |
| `main` ruleset | `Protect main` (`14822987`), `enforcement: active`, updated 2026-09-07 | `GET /repos/…/rulesets/14822987` |
| Required checks | `gate`, `disposition` (both `integration_id: 15368`), `strict_required_status_checks_policy: true` | same |
| PR rule | `required_approving_review_count: 0`, `require_code_owner_review: true`, `require_extra_approval_for_unattributed_changes: true` | same |
| Bypass | no `bypass_actors` key present; `current_user_can_bypass: "never"` for this session's token | same |
| Private vulnerability reporting | **`{"enabled": false}`** | `GET /repos/…/private-vulnerability-reporting` |
| Published advisories | none | `GET /repos/…/security-advisories` |
| Dependabot alerts | **disabled** — `403 "Dependabot alerts are disabled for this repository."` | `GET /repos/…/dependabot/alerts` |
| Scheduled CI history | 10 most recent `schedule` runs: one failure, `2026-09-09` on head `d7b55b08`; next scheduled run green on `56a87620` (the PR #294 merge commit) | `GET /repos/…/actions/workflows/ci.yml/runs?event=schedule` |
| PR #328 (merged into current `main`) | files include `.github/agents/consistency.agent.md`, `.github/scripts/continuous-improvement.mjs`, `.github/scripts/continuous-improvement.test.mjs`; reviews `[(alaiba, COMMENTED), (alaiba, COMMENTED)]`; **no `APPROVED` review**; `merged: true` | `GET /repos/…/pulls/328`, `/reviews`, `/files` |
| PR #328 head checks | `disposition` success, `gate` success | `GET /repos/…/commits/224ea2e5…/check-runs` |
| Open issues | #313 *Verify Code Owner enforcement for protected `.github` changes* (open, created 2026-09-13), #322, #323, #325, #327, #295 | `GET /repos/…/issues?state=open` |

Endpoints that remained inaccessible, recorded rather than inferred: `/vulnerability-alerts`, `/automated-security-fixes`, `/actions/permissions`, `/environments` (all blocked by the agent proxy, not by GitHub), and `/branches/main/protection` (403 to this integration). `security_and_analysis` returns `null` on the repository object, which is not evidence that features are off — the `dependabot/alerts` result above is, and it is a GitHub answer rather than a proxy refusal.

## Current-state drift

Live `main` is `ebab62a7`; the report's baseline is `950a1974`. The diff is 7 files:

```
.github/agents/consistency.agent.md             |  37 +++--
.github/scripts/continuous-improvement.mjs      | 204 +++++++++++++++++++++---
.github/scripts/continuous-improvement.test.mjs | 170 +++++++++++++++++---
AGENTS.md                                       |  26 ++-
docs/development/consistency-review.md          |  35 ++--
docs/development/continuous-improvement.md      |  12 +-
docs/development/testing.md                     |   2 +-
```

`git diff --name-only 950a197 origin/main -- .github/workflows/ .github/CODEOWNERS .github/SECURITY.md .github/dependabot.yml` returns **nothing**: no workflow, code-owner, security-policy, or Dependabot-configuration change landed after the report's baseline.

Inspected, not assumed:

- **PR #326** (`53979ef`) changes `AGENTS.md` only, defining harness-neutral PR base normalization and the "never point an open PR branch at the base commit" invariant. It is delivery-process authority, not a security control. **Not material.**
- **PR #328** (`ebab62a`) reworks Consistency finding accounting in `continuous-improvement.mjs` and its test. It adds parsing of completion-evidence fields from issue-comment bodies. Authorisation for that evidence remains the pre-existing `TRUSTED_COMPLETION_ASSOCIATIONS` (`OWNER`/`MEMBER`/`COLLABORATOR`) check, which the diff does not weaken; the workflow's `repository_dispatch` trust boundary is untouched. **Not material.**
- `docs/development/testing.md`'s single changed line describes the same test suite in more detail. **Not material.**

**Drift verdict: no post-baseline change materially affects the report's claims, evidence, ownership map, or recommendations.** The report's ruleset, CODEOWNERS, SECURITY.md, CI, and Dependabot observations remain accurate at `ebab62a7`.

## Challenges performed, and what each returned

### 1. Is a standalone Security track justified? — **Not falsified. C stays rejected.**

I looked for a coherent body of security semantics, architecture, or implementation responsibility that cannot truthfully live with an existing owner, and did not find one.

Consequential identity/trust/authority/credential-lifecycle/fail-safe semantics are owned by `docs/architecture/operational-execution-digital-twin.md` §5/§6/§16, whose ten safety principles I read directly. Interchange semantics are ADR-0012's. Accountable operational continuation identity is ADR-0013's (Accepted). Generic actor/capability semantics are held open by the register on purpose, and Operational §5 explicitly refuses to absorb them. A Security track would have to take either the deferred generic semantics — inverting a boundary the repository has already reasoned about — or Operational's consequence semantics, which duplicates. Both are the report's cases 4 and 6, and both hold on independent re-derivation.

The two concerns I mapped that the report does not (agent-reviewer judgment, and the local developer/devcontainer supply chain) both route to existing owners (`.github/agents/pr-reviewer.agent.md` and the infrastructure owner of `infra/dev/`, `.devcontainer/`), so neither creates track-shaped work.

### 2. Is a new cross-cutting Security *owner* justified? — **Not falsified.**

The test that matters is whether any obligation requires durable cross-domain *authority* rather than shared criteria. I could not construct one. The strongest candidate is "someone must respond to an unattended scan failure", and that is a routing sentence, not an authority. The second strongest is "someone must decide the exposure class at which authentication stops being optional", and that is a recorded threshold whose semantic answer still belongs to the product/interface owner.

The report's own sharpest self-challenge — *is Candidate D a renamed Candidate B?* — survives in a narrowed form and is carried as qualification **Q4** below rather than as a falsification: D is only meaningfully different from B if its artifacts land in surfaces that already have owners.

### 3. Is any material current concern ownerless? — **Not falsified for current concerns. Narrowed for one deferred concern (Q5).**

I built the ownership map before reading the report's and reached the same "no ownerless row" result for every *current* concern. The hosted/multi-user case is classified inconsistently by the report and is qualified below; it is a triggered future destination, not an owned future responsibility, and that distinction is exactly the one the handoff asked to preserve.

### 4. Can security semantics and security assurance stay separate? — **Not falsified.**

I found no case where the proposed assurance surface must acquire semantic authority. Recommendation 7 is the nearest approach — it places "per-principal isolation" in an assurance destination — but what it records is the *trigger threshold*, not whether isolation exists, and the report's own invariant 3 already forbids the stronger reading. This holds only if Q4 and Q5 are carried into reconciliation.

### 5. Are the recommended corrections the smallest durable response? — **Partly falsified. Recommendation 5 duplicates an existing surface; recommendation 4's destination can be smaller.**

Re-evaluated from first principles against live state:

| Rec | Gap still live? | Verdict |
|---|---|---|
| 1 — private vulnerability reporting + minimal triage expectation | **Yes.** `private-vulnerability-reporting` returns `{"enabled": false}`; `.github/SECURITY.md` still instructs a public issue on a public repository, with an unaddressed "contact the maintainers directly" fallback and no address anywhere | **Correct, correctly sized, correctly destined.** The strongest of the five |
| 2 — record or retire the `§3.x` numbering | **Yes.** `git grep "§3\."` on `origin/main` returns exactly five hits, all in `ApiSmokeTest` (`§3.1`, `§3.2`, `§3.3`, `§3.9`, `§3.11`); no document anywhere defines the numbering | **Correct.** The "or retire" alternative keeps it minimal |
| 3 — correct the network-exposure section | **Yes.** The hardening list names bind/CORS/TLS/audit/logging and none of the implemented controls or structural limits | **Correct**, subject to Q7 on the document's shape |
| 4 — route unattended scan findings | **Yes**, and independently corroborated: the `2026-09-09` scheduled run failed and the next scheduled run was green on `56a87620`, the merge commit of unrelated PR #294. The finding was cleared as a side effect of someone else's work, exactly as the report's case 1 argues | **Correct, but the destination should be smaller — see Q3** |
| 5 — verify the four activation conditions and record the result | **Yes**, but **already owned**: open issue #313 states the same problem, the same `count: 0` question, the same author-is-code-owner question, the same ruleset id, and prescribes repair plus reconciliation into `reviewing.md` and `CODEOWNERS` | **Duplicates an existing surface — see Q1** |
| 6 — `MaxBodySizeFilter` chunked path | **Yes.** `WebConfig.java:49` reads `request.getContentLengthLong()` and compares to `MAX_BODY_BYTES`; a request without `Content-Length` yields `-1` and passes | **Correct**, ordinary product fix |

Nothing was preserved merely because it is cheap: recommendation 5 is narrowed and recommendation 4 is redirected.

### 6. Does `.github/SECURITY.md` have the right authority shape? — **Not examined by the report. Raised as Q7.**

The document currently carries disclosure policy, current posture and limitations, deployment hardening guidance, scan-execution ownership, and a "Mature-product security principles" list. That last section substantially restates Operational §5/§16 in different words — I compared them directly. The ambiguity is mitigated, because SECURITY.md explicitly says the Operational architecture and readiness plan "now own the concrete planning boundary". So the mixture is tolerable at the project's current scale, but recommendation 3 adds content to an already five-role document, and the duplicate principles list is a live drift risk.

### 7. Does the control-plane evidence prove what the report says? — **The load-bearing uncertainty is now resolved, negatively. See Q1.**

The report correctly refused to claim that activation condition 4 was effective, and recorded the `count: 0` question as an unresolved unknown. Live evidence now answers it in the direction the report allowed for:

> **PR #328 changed `.github/agents/consistency.agent.md`, `.github/scripts/continuous-improvement.mjs`, and `.github/scripts/continuous-improvement.test.mjs` — all matched by `/.github/ @alaiba` — and merged into `main` with two `COMMENTED` reviews and no `APPROVED` review, under an active ruleset with `require_code_owner_review: true` and no bypass actors.**

The same pattern holds for #320 and #326, and issue #313 records it for #312. Whether the cause is that `require_code_owner_review` does not bind at `required_approving_review_count: 0`, or that the sole code owner being the PR author leaves the requirement unsatisfiable-and-therefore-unenforced, is a `GitHub platform behaviour` question this review did not isolate — and it does not need to be isolated for the conclusion: **configuration presence is demonstrably not evidence of an active independent-approval gate here.** `docs/development/reviewing.md` line 371 already says CODEOWNERS enforcement is "necessary but not sufficient on its own"; what is new is that it is now observably not *effective* either.

This is precisely the third item on the report's own "what would change the conclusion" list, with the response the report pre-registered: the control-plane item **sharpens from assurance gap to implementation gap, and the ownership conclusion does not change.** A report that predicts the direction of its own falsification and states the consequence in advance is well calibrated; this is a qualification, not a defeat.

### 8. Is vulnerability handling credible? — **The report's finding is confirmed and is the strongest item in the set.**

Walking `report → confidential receipt → triage → remediation → verification → disclosure/closure` on live state: the documented entry point is a public issue on a public repository; private vulnerability reporting is off; no advisories exist; no security contact address appears anywhere in the repository; and no triage, verification, or disclosure expectation is defined for anything that is not a dependency update (`.github/agents/dependency-maintainer.agent.md` covers advisory-driven dependency remediation only). The first step of the path discloses the vulnerability. This does not require enterprise incident-response machinery to fix — a settings toggle and a policy edit close it — and it does not imply a security owner.

### 9. Does hosted/non-actuating operation falsify the ownership conclusion? — **No. But it exposes a scope seam. See Q5.**

Assuming hosted or multi-user Arcogine with no physical actuation: Operational is *not* the semantic owner, and §5 says so in terms. Interface/application ownership is sufficient for the exposure surface but not for a shared actor/authentication/authorisation boundary, which is the register's open actor-identity question. The concluded Agency boundary constrains the answer in the direction the report states: generic actor/capability semantics do not become Operational's, and equally do not become a Security owner's. This case remains the strongest argument against Candidate C and I could not make it argue for one. Hosted SaaS is not committed product scope and I did not treat it as such.

The seam: the register's actor-identity question is scoped to identity *referent, equality, namespace, lifecycle, rename/merge/retirement, federation, and external identity*. Authorisation enforcement and per-principal isolation are in neither that question nor the Operational-boundaries question, which is Operational-scoped.

### 10. Are missing controls being mistaken for ownership gaps? — **No. The report is disciplined here.**

The report's invariant 2 ("an absent control is a gap only when a trust boundary is actually crossed") is the correct discriminator, is applied consistently, and is what makes contexts 1 and 6 classifiable differently despite both having almost no implemented controls. I tried to find a row where an absence was silently upgraded into an ownership claim and did not find one. The classification I disagree with (context 4, `OFR` with owner "open" and gap "none") is an under-statement of a gap, not an over-statement.

### 11. Additional failure modes from `docs/development/researching.md` §9

- **Stale baseline** — re-checked independently; no material drift (above).
- **Source that does not support the claim** — the repository-side claims I re-verified all held: `WebConfig.java:49`, the five `§3.x` labels, the `TRUST BOUNDARY` comment block in `continuous-improvement.yml`, the `.github/CODEOWNERS` self-ownership rationale, `reviewing.md`'s four activation conditions, SHA-pinned actions and checksum-verified scanner installs in `ci.yml`, and the daily `0 5 * * *` schedule. I did **not** re-fetch NIST SP 800-218 or the GitHub Actions events documentation — see Q8.
- **Possibility treated as necessity** — the report's exclusion of SLSA, OpenSSF, CISA, OWASP, IEC 62443, and NIST SP 800-82 as non-load-bearing at the current boundary is correct and is the right discipline, not a gap.
- **Over-generalised abstraction** — not found. The conclusion is narrower than the evidence would have permitted.
- **Prematurely settled question** — not found; the report leaves four unknowns open and names them.
- **Conclusion stronger than its evidence** — not found. Confidence is stated as moderate-to-high on the negative result and moderate on the positive recommendations, and the recommendations are where my qualifications land.
- **Omitted candidate** — R-D (naming the control plane as an explicit owned surface) and R-E (corrections to existing surfaces only) are not stated as candidates by the report. R-D collapses into the existing answer, because the control plane already has owners (`CODEOWNERS`, the ruleset, `reviewing.md`, the dependency maintainer) and the missing part is verification, which issue #313 owns. R-E does not change the conclusion but does discipline it, and is carried as Q4.

## What survived

- **No standalone Security track is justified.** Candidate C remains rejected on independent re-derivation; cases 4, 6, and 7 hold, and no concern I mapped — including two the report did not map — produces track-shaped work.
- **No new cross-cutting Security owner is justified.** No obligation I could construct requires durable cross-domain authority rather than shared criteria and routing.
- **The distinction between a cross-cutting security *assurance* surface and a cross-cutting security *owner*** is the correct surviving formulation, subject to Q4.
- **No material *current* concern is ownerless.**
- **Security semantics and security assurance can remain separate.**
- **Recommendations 1, 2, 3, and 6** — every underlying gap re-verified live; destinations correct; sizes correct.
- **PR #294 is evidence *for* distributed ownership**, not against it. Independently confirmed: the accepted `repository_dispatch` fix and its reasoning are present in `continuous-improvement.yml`'s `TRUST BOUNDARY` comment block, and the argument it makes — that no in-file construct can constrain a ref-selected copy of the same workflow file — is sound on its own terms.
- **The report's honesty posture.** It declares its own adversarial-review status as `required, not yet performed`, labels its self-challenge as non-independent, distinguishes `Repository fact` from `Inference` at the points where that distinction carries weight, records every surface it could not inspect, and pre-registers what would change its conclusion. Two of its four unresolved unknowns have now been answered against it, and it had already stated what that would mean.

## What was falsified or narrowed

1. **"Nothing in the repository verifies [the four activation conditions]"** — narrowed. Open issue #313 owns the verification and the repair. What is absent is *executable or automated* verification, not tracking.
2. **Activation condition 4, "Present, effect unverified"** — resolved negatively (Q1). The intended independent-approval property is not achieved on live `main`.
3. **"Dependabot security alerts not verifiable"** — resolved negatively (Q2). They are disabled.
4. **Context 4 classified `OFR` / owner "open" / gap "none"** — internally inconsistent by the report's own key, and under-covered by recommendation 8 (Q5).
5. **The security-context map's completeness** — one material current context is unmapped (Q6).
6. **Recommendation 4's destination** — an existing section already covers the adjacent responsibility (Q3).

None of these touches the load-bearing conclusion. Every one of them is an artifact, routing, or classification correction — which is, itself, further evidence for the report's central claim that Arcogine's security shortfall is recorded requirement and routed evidence rather than assignment.

## Qualifications that must survive any reconciliation

**Q1 — Record the merge-gate result, do not re-open the investigation.** Recommendation 5 must be narrowed to "carry the result into `reviewing.md` and `CODEOWNERS`" and must not duplicate open issue #313, which already owns the controlled verification and repair. Two things must land regardless of who does the repair:
  - `.github/CODEOWNERS`'s comment — "This entry has no effect until a maintainer enables 'Require review from Code Owners' on the branch protection ruleset for `main`" — is **inaccurate on live `main`**: the setting is enabled and the entry still has no observed effect. It must not continue to imply that enabling the setting was the missing step.
  - `docs/development/reviewing.md`'s activation condition 4 should record the observed behaviour rather than describing a configuration state, so that the check-name-provenance and listener-narrowing mitigations are not read as active protections while they are not.

**Q2 — Dependabot alerts are disabled; say which way that was decided.** Reconciliation must either enable them or record the deliberate reliance on Trivy SBOM/image scanning plus `npm audit` plus the daily re-scan. The trade-off is concrete and belongs in the record: the CI scanners run `--ignore-unfixed`, so an unfixed CRITICAL in a shipped dependency is invisible to the gate, and Dependabot alerts are the surface that would otherwise show it. This narrows the report's `CN` classification for that row; it does not create an ownerless concern, because the dependency maintainer owns remediation either way.

**Q3 — Route unattended findings inside the section that already owns scans.** `.github/SECURITY.md` already has a `### Security scan ownership` section assigning scan *execution* ownership between the local command surface and CI. Recommendation 4 is one sentence about *response* ownership in that same section, not a new assurance surface. The 2026-09-09 scheduled failure clearing as a side effect of PR #294 is the worked example and should be what the sentence is written against.

**Q4 — "Assurance policy surface" must mean edits to existing authorities, not a new document by default.** This is the surviving form of the report's own sharpest self-challenge. Candidate D differs from Candidate B only because artifacts cannot drift into co-ownership the way a responsibility-holder can — and that is true only while the artifacts live in surfaces that already have owners. A new, separately-maintained security-assurance document with no named maintainer is the artifact-level form of the everybody's-job problem D exists to avoid. Reconciliation should prefer `.github/SECURITY.md`, `docs/development/testing.md` (§ *Security verification tests*, which recommendation 2's requirement set is the natural companion to), and `docs/development/reviewing.md`, and should justify any new file explicitly against `AGENTS.md`'s preference for improving an existing authoritative artifact.

**Q5 — Classify the hosted/multi-user case as a triggered research destination, not an owned future responsibility, and check its scope.** The `OFR` / owner "open" / gap "none" combination is inconsistent. Additionally, the register question recommendation 8 points at is scoped to actor *identity*; authorisation enforcement and per-principal isolation for a hosted non-actuating deployment fall between it and the Operational-boundaries question. Reconciliation must either widen the trigger note or state explicitly that those semantics land with the product/interface owner when a consumer is admitted. This does not justify a Security track — case 4 still rejects C — but it must not be recorded as "gap: none".

**Q6 — Add agent-reviewer judgment to whatever threat-model or criteria artifact lands.** The repository is public with forking enabled. The `disposition` gate's ordinary positive path is a canonical `READY TO MERGE` review whose author association is checked but whose *content* is produced by an agent reading candidate-controlled diff and PR text. `docs/development/reviewing.md` and `.github/agents/dependency-maintainer.agent.md` already apply exactly this candidate-controlled-signal discipline to the Dependabot provenance path; `.github/agents/pr-reviewer.agent.md` contains no equivalent treatment for its own reading — a grep for `untrust`, `inject`, `malicious`, `adversar`, `hostile`, and `attacker` across it returns nothing on point. **This is owned** (PR Reviewer, `reviewing.md`), so it is not an ownerless gap and does not disturb the conclusion; it is another instance of the report's own diagnosis, on the highest-value asset in context 3, and it belongs in the record for exactly that reason.

**Q7 — Decide `.github/SECURITY.md`'s shape while editing it.** Recommendation 3 adds material to a document already carrying five roles, whose *Mature-product security principles* section is a near-restatement of Operational §5/§16. Reconciliation should decide whether that section becomes a pointer to Operational rather than a parallel list. Not a blocker; a drift risk that recommendation 3 makes slightly worse if left unexamined.

**Q8 — External evidence was not re-verified by this review.** I did not re-fetch NIST SP 800-218 or the GitHub Actions events documentation, so the report's quotations and designations are carried forward on its own verification, not independently confirmed here. This is acceptable because neither is load-bearing: the conclusion rests on repository and live-GitHub evidence, which I did verify in full, and SSDF's PO.1/PO.4-versus-PO.2 structure is corroboration arrived at independently from the repository side. Reconciliation must not elevate SSDF from a corroborating lens into an Arcogine requirement.

## Is the report decision-quality evidence for durable promotion?

**Yes, with Q1–Q8 carried.**

Two points of scope. First, the report's own recommended consequences are policy, documentation, and configuration changes — not an ADR and not architecture. The high-risk independent-review prerequisite in `docs/development/researching.md` §7/§9 is what this review satisfies, and it is satisfied at the condition-2 level of independence stated above, which is what §9 requires as a minimum for a high-risk question. Second, this disposition binds to commit `ef4130a13fb68749563a642126a75ecd449dab7b` and to no other revision. A later report commit — including a metadata-only edit adding this review's status or a link to it — is a distinct revision, and `.github/agents/pr-reviewer.agent.md` line 204 requires the promoting change to bind to the exact reviewed SHA or obtain a new review.

The report should **not** be edited to record this disposition. This review artifact is the record.

## New research question exposed

**None that should be admitted.**

The report offered one candidate — *in a single-maintainer repository where coding agents commit as the human owner, what independent-approval property can the merge gate actually guarantee?* — as `CANDIDATE`, explicitly gated on verification preceding admission. That gate has now done its job in both directions: the verification's factual precondition is answered (the property is not currently achieved), and the resulting work is owned by open issue #313 as delivery/process repair, not by research. Admitting it to the register would manufacture a research question where a repair is what is needed.

Q6 (agent-reviewer judgment as an authorisation input) is likewise not a research question: it is a threat-model entry for an owner that already exists.

**The research register has not been modified by this review.** No entry was added, removed, or re-stated, and `docs/research/synthesis-seeds.md` was not touched.

## Stopping point

Further evidence stopped moving the candidate set, the material proving cases, the gap classifications, confidence, the required qualifications, and the disposition. The remaining unresolved item — which GitHub mechanism explains the observed code-owner behaviour — is a controlled-experiment question owned by issue #313, and isolating it would not change any of the above.

## Disposition

**`ACCEPT WITH QUALIFICATIONS`**

The load-bearing conclusion survives an independent attempt to falsify it: **Arcogine does not need a standalone Security track and does not need a new cross-cutting security owner; the missing layer is recorded security requirements and routed assurance evidence, and a cross-cutting security-assurance artifact surface — not a role — is the justified response.** Qualifications Q1–Q8 must be part of any durable reconciliation.
