# Security ownership and assurance boundary — durable reconciliation handoff

This is a **temporary handoff prompt** for the durable reconciliation phase of the completed security ownership/readiness research packet. It is not itself security policy, accepted architecture, implementation planning, or research evidence. Remove it from the final reconciliation tree before merge.

## Mission

Perform the **separate durable reconciliation** required by `docs/development/researching.md` §10 for the security ownership and assurance investigation carried by workspace `alaiba/awesome-edison-y28lc5`.

Translate the decision-quality research result into the **smallest correct edits to existing authoritative repository surfaces**, plus only the narrow implementation/configuration changes that the evidence already justifies.

Do **not** create a Security track, Security module, Security ADR, new cross-cutting Security owner/role, new assurance document by default, or speculative implementation roadmap.

The surviving conclusion is:

> Arcogine does not need a standalone Security track and does not need a new cross-cutting security owner. Security semantics remain with their existing domain/product/process owners. The missing layer is recorded security requirements/readiness criteria and routed assurance evidence, carried by existing authoritative artifacts rather than by a new owner.

That conclusion is decision-quality **only with all adversarial-review qualifications Q1–Q8 carried into reconciliation**.

## Exact evidence packet — immutable coordinates

Reconciliation must bind to these exact completed artifacts, not summaries or branch-tip versions:

### Research report

- **Workspace:** `alaiba/awesome-edison-y28lc5`
- **Commit:** `ef4130a13fb68749563a642126a75ecd449dab7b`
- **Path:** `docs/research/investigations/security-ownership-and-assurance-boundary.md`
- **Research baseline:** `950a19742e811918a2312d8ef6fdf0ec129761d3`

### Independent adversarial review

- **Workspace:** `alaiba/awesome-edison-y28lc5`
- **Commit:** `f99555b649c72c8761205e3aecfe068dad7e7fbe`
- **Path:** `docs/research/investigations/security-ownership-and-assurance-boundary-adversarial-review.md`
- **Disposition:** `ACCEPT WITH QUALIFICATIONS`
- **Review baseline:** `ebab62a71de99d3d171be6291ce5ebf6807f5895`

Both evidence commits are handed-off artifact identities. **Never amend, rebase away, force-push away, squash away, or otherwise rewrite them.** Later commits may delete the temporary files from the workspace tree; the historical commit+path coordinates remain the evidence identities.

## Start-of-run grounding

Before editing anything:

1. Re-resolve **live `main`** and record its exact SHA. At prompt creation, `main` was `c0b4f8091be00f70f112d91d1172fbe975f8fe4e`; this is only a historical hint.
2. Read current `AGENTS.md`.
3. Read `docs/development/researching.md`, especially §10 research/reconciliation separation, evidence custody, transfer audit, and retirement.
4. Read the exact report and exact adversarial review above.
5. Read the current authoritative surfaces that the evidence points to, including at least:
   - `.github/SECURITY.md`
   - `docs/development/testing.md`
   - `docs/development/reviewing.md`
   - `.github/CODEOWNERS`
   - `.github/agents/pr-reviewer.agent.md`
   - `.github/agents/dependency-maintainer.agent.md`
   - `.github/workflows/ci.yml`
   - `.github/dependabot.yml`
   - relevant API implementation/tests around request-body limits and network exposure
   - `docs/architecture/operational-execution-digital-twin.md`
   - `docs/research/research-register.md`
6. Re-check current live GitHub-side repository settings where they are load-bearing: private vulnerability reporting, Dependabot alert state, active ruleset/required checks/Code Owner behavior, and any relevant Actions/security setting that can be verified.
7. Read open issue #313, **Verify Code Owner enforcement for protected `.github` changes**, and any landed work/comments that have changed its state. Do not duplicate work it already owns.
8. Search current docs/code for the main reconciliation terms: `security`, `vulnerability`, `network exposure`, `CORS`, `body size`, `SSE`, `§3.`, `security scan ownership`, `Dependabot`, `Code Owner`, `candidate-controlled`, `untrusted`, `READY TO MERGE`, `hosted`, `multi-user`, `authentication`, `authorization`, and `per-principal`.

If live `main` has changed materially since the review baseline, reconcile against current authority rather than mechanically applying stale wording. Preserve the evidence conclusion unless new landed evidence actually falsifies it; if it does, stop and explain rather than silently overruling the reviewed packet.

## Workspace freshness and history preservation

Use the same workspace branch: `alaiba/awesome-edison-y28lc5`.

If it is behind live `main`, update it only through Arcogine's **history-preserving merge-style Update branch** path from `AGENTS.md`. Verify after the update that:

- `ef4130a13fb68749563a642126a75ecd449dab7b` remains reachable;
- `f99555b649c72c8761205e3aecfe068dad7e7fbe` remains reachable;
- the pre-update workspace head remains an ancestor of the new head;
- the workspace is current with `main`;
- the intended reconciliation diff remains non-empty.

Never rebase or force-update this evidence workspace.

## Authority boundary to preserve

Reconciliation must encode the following durable ownership shape without inventing parallel authority:

- **Operational/Digital Twin** owns consequence-specific operational trust/authority/command/observation/fail-safe semantics when that boundary is actually in scope.
- **Governance** owns its existing conformance/provenance/governed-change semantics, not generic application security.
- **Interface/application/product owners** own security behavior at their actual exposure surfaces, including current API/network-input behavior and future hosted/non-actuating product semantics when such a consumer is admitted.
- **PR review / dependency maintenance / CI / repository control plane** own their current process and supply-chain security responsibilities.
- `.github/SECURITY.md`, `docs/development/testing.md`, `docs/development/reviewing.md`, and existing executable safeguards carry cross-cutting **assurance criteria/evidence** where appropriate.

Do not turn assurance criteria into a new semantic owner.

## Required reconciliation outcomes

Apply the report's durable consequences **as narrowed by adversarial-review Q1–Q8**. Re-check each item against live state first; do not reintroduce something already fixed.

### 1. Vulnerability reporting must be private for sensitive reports

Current research evidence found a public repository whose documented entry path was a public GitHub issue while private vulnerability reporting was disabled.

Reconcile this by:

- enabling GitHub private vulnerability reporting if current repository capability and permissions allow it; and
- updating `.github/SECURITY.md` so sensitive vulnerabilities are never instructed to be disclosed in a public issue;
- recording a **minimal**, project-scale triage/remediation/verification expectation for non-dependency vulnerability reports.

Do not invent enterprise incident-response bureaucracy. The durable requirement is a credible confidential intake and clear responsibility through verification/closure.

If the setting cannot be changed from the available execution surface, make the repository policy edit only if it can truthfully point to an actually available confidential path; otherwise stop that part as a concrete external-setting blocker rather than documenting a path that does not exist.

### 2. Make security requirements and executable verification point at each other

The report found dangling `§3.x` labels in `ApiSmokeTest` with no maintained requirement set. The review confirmed that the five labels remain unexplained at its baseline.

Choose the **smallest coherent** repair on current `main`:

- either record the maintained security verification criteria in the existing `docs/development/testing.md` **Security verification tests** authority and make executable tests refer to stable semantic criteria;
- or retire the unexplained numeric labels and replace them with semantic test naming/comments if the numbering has no durable value.

Do not create a new security-assurance document merely to preserve old numbering. Prefer semantic names over opaque coordinates.

The final state must not claim that a control is verified unless a test/check actually proves it.

### 3. Correct network-exposure security posture

Update `.github/SECURITY.md` so the current network-exposure section accurately distinguishes:

- implemented controls/limits that actually exist now (verify current code/tests before listing them), including relevant body-size, SSE, CORS, input-bound, browser/CSP, bind, and similar controls;
- structural limits that hardening cannot solve, such as shared/single-user state, lack of a user/principal model, missing scenario/resource-cost isolation, or any equivalent current limitation that live code still demonstrates;
- hardening guidance from actual product readiness criteria;
- current simulation exposure from future production-consequential Operational execution.

Do not imply that TLS/CORS/reverse-proxy hardening turns the current product into a safe multi-user or production-operational system.

### 4. Route unattended security findings inside the existing scan authority

Carry adversarial qualification Q3 exactly in spirit: **do not create a new surface**.

`.github/SECURITY.md` already has `### Security scan ownership`. Extend that existing section with the smallest clear rule for who responds when a scheduled/unattended security scan fails, how that differs from a finding arising inside an active PR, and the expected response posture.

Use the historical 2026-09-09 scheduled failure clearing only as evidence for why routing matters; do not preserve the date/PR as a durable semantic rule unless needed as a concise example.

### 5. Do not duplicate issue #313; reconcile the observed merge-gate truth

The adversarial review resolved the report's Code Owner uncertainty negatively at its baseline: configured `require_code_owner_review: true` was **not evidence of an effective independent-approval gate**, and merges changing protected `.github/**` content had occurred without native `APPROVED` reviews.

Open issue #313 already owns the controlled platform verification and repair. Re-resolve its current state.

- **If #313 is still open/unrepaired:** do not implement a competing ruleset experiment or parallel repair in this reconciliation. Make only the minimum maintained-document accuracy corrections needed so `.github/CODEOWNERS` and `docs/development/reviewing.md` do not claim or imply an independent Code Owner protection that current evidence does not establish. Leave the actual controlled repair to #313.
- **If #313 has landed a verified repair:** consume that landed result and reconcile wording to the actual enforced invariant; do not preserve the older negative observation as current truth.

In either case, remove or correct the stale `.github/CODEOWNERS` comment that says the entry merely waits for the setting to be enabled if the setting is already enabled.

Do not create a new research-register question for this; the review explicitly classified it as owned delivery/process repair.

### 6. Decide the Dependabot-alert posture explicitly

The adversarial review verified that Dependabot alerts were disabled at its baseline.

Re-check current state. Reconciliation must leave an explicit, truthful posture:

- either enable Dependabot alerts (and any closely related setting only if justified and intentionally chosen),
- or record the deliberate decision to rely on the existing Trivy SBOM/image scans, `npm audit`, scheduled re-scan, and dependency-maintainer workflow instead.

Do not silently leave "unknown" or imply Dependabot coverage when it is disabled.

Account for the review's specific trade-off: scanners using `--ignore-unfixed` can miss an unfixed critical shipped-runtime vulnerability that an alerting surface could still expose. Verify current CI flags before carrying that statement forward.

This is an assurance/control-plane decision, not a new Security owner.

### 7. Fix the current request-body enforcement gap, narrowly

Re-check the report/review finding around `MaxBodySizeFilter` and requests without `Content-Length` / chunked transfer.

If the gap still exists, implement the **smallest correct product fix** plus regression evidence. Do not broaden this into an API-security redesign.

The test must prove the relevant bypass/failure mode, not only the happy path. Preserve existing API semantics for legitimate requests.

If current `main` has already fixed it, do not duplicate the change; record that the consequence was already reconciled by landed code.

### 8. Record the hosted/multi-user trigger without inventing a Security owner

Carry Q5.

A hosted or multi-user Arcogine with no physical actuation is a discriminating future boundary:

- Operational is **not automatically** the semantic owner merely because security is involved;
- the existing actor-identity research may become relevant when a concrete consumer is admitted;
- authentication, authorization enforcement, per-principal isolation, audit expectations, and resource/cost isolation must become explicit readiness criteria before such a consumer is treated as safe;
- current absence is a **triggered future research/product boundary**, not proof of a current standalone Security gap.

Encode this trigger in the smallest existing authority that will actually be consulted before intentional hosted/multi-user exposure, preferably `.github/SECURITY.md` and/or the existing testing/readiness wording. Do not add a duplicate security research row merely to restate the trigger.

If the current actor-identity register entry is too narrow to carry the future question, do **not** widen it speculatively now unless a concrete admitted consumer makes the question live. Preserve the trigger instead.

### 9. Treat candidate-controlled reviewer input as a security boundary

Carry Q6.

The ordinary `disposition` authorization path depends on an agent reviewer consuming PR text and candidate-controlled diffs. Reconcile that as an **owned PR-review threat-model rule**, not as a new security function.

Update the narrowest existing authority — normally `.github/agents/pr-reviewer.agent.md` and, where durable contributor/review semantics belong, `docs/development/reviewing.md` — so reviewer judgment treats candidate-controlled content as untrusted evidence/data, not as instructions that can override repository authority, reviewer contracts, tool-safety constraints, or disposition rules.

Keep the rule concrete and testable/reviewable. Do not add generic prompt-injection boilerplate that does not change reviewer behavior.

### 10. Reduce `.github/SECURITY.md` authority drift while editing it

Carry Q7.

The current file mixes vulnerability disclosure, current posture, deployment hardening, scan ownership, and mature-product principles. That is tolerable at current scale, but the mature-product list overlaps Operational §5/§16 and can drift.

While editing the file, decide deliberately whether to:

- keep a concise cross-cutting principle statement and point to Operational for consequence-specific semantics; or
- retain the current list only where it adds security-policy value not already owned elsewhere.

Prefer pointers to the existing semantic authority over parallel restatement. Do not move generic product/interface security under Operational.

### 11. External frameworks remain corroborating lenses, not requirements

Carry Q8.

NIST SSDF, GitHub documentation, OWASP, SLSA/OpenSSF, CISA, IEC 62443, NIST SP 800-82, and similar material must not become Arcogine compliance requirements merely because they appeared in research.

Use current primary external documentation only where needed to perform a live repository-setting/configuration change or verify a platform behavior. The durable repository rules should be justified by Arcogine's own boundary and evidence.

## Explicit non-goals

Do **not**:

- create a standalone Security track, `PLAN-SEC-*` namespace, Security module, Security architecture layer, or Security ADR;
- create a new cross-cutting Security Assurance owner/role;
- create a new security-assurance document unless current authoritative surfaces demonstrably cannot carry a required rule, and if you believe one is necessary, stop and justify that against Q4 before adding it;
- reopen Accepted ADR-0013 or concluded Agency semantics;
- move generic actor/capability/identity semantics into Operational or Security by default;
- implement authentication, authorization, TLS termination, secrets management, multi-tenancy, live actuation safety, SAST/DAST/fuzzing programs, release provenance, or other future controls without a current admitted consumer/trigger;
- duplicate issue #313's controlled Code Owner/ruleset repair;
- add a new security research-register entry merely because this reconciliation exists;
- treat framework checklists as compliance mandates;
- merge temporary research evidence files to `main` as a permanent archive.

## Validation expectations

Run validation proportionate to the actual changes, following current `AGENTS.md` and `docs/development/testing.md`.

At minimum:

- execute the targeted API tests for any request-body enforcement change;
- run the relevant broader Java/API test surface if production code changes;
- run documentation/repository checks that cover changed `.github/**`, docs, agent-contract, workflow, or CODEOWNERS surfaces;
- run security-related checks only where the changed surface requires them; do not make unavailable scanners gate unrelated documentation edits;
- verify any changed GitHub repository setting by reading it back after mutation;
- re-check the final branch diff against live `main` for accidental temporary-artifact leakage or unrelated edits.

Do not claim a security property from prose alone when an executable or live-platform verification is available and material.

## Knowledge-transfer audit — mandatory before reconciliation is complete

Before opening/finalizing the reconciliation PR, explicitly account for every material result from the report and review.

At minimum classify each as **transferred**, **already landed**, **owned follow-up**, or **discarded with reason**:

- no standalone Security track / no new Security owner;
- semantic-owner vs assurance-evidence distinction;
- private vulnerability-reporting finding;
- network-exposure/current structural-limit map;
- security verification criteria / dangling `§3.x` example;
- unattended scan-response routing;
- merge-gate / Code Owner qualification and issue #313 ownership;
- Dependabot-alert posture;
- `MaxBodySizeFilter` chunked/no-length proving case;
- hosted/multi-user trigger and ownership seam;
- candidate-controlled reviewer-judgment threat boundary;
- `.github/SECURITY.md` authority-shape/drift qualification;
- negative knowledge that PR #294 supports distributed ownership rather than proving an ownerless gap;
- the report's six-context trust-boundary map insofar as retaining it changes future readiness decisions;
- Q8's boundary that external frameworks are corroborating lenses, not Arcogine mandates.

### Synthesis-seed decision

Only after reconciliation results are settled, inspect `docs/research/synthesis-seeds.md` for semantic neighbors as required by the research method's transfer phase.

Do **not** create a seed just because an observation is interesting. Add/extend one only if the candidate is evidence-bearing, potentially transferable beyond Arcogine-specific names, and loss-sensitive after workspace retirement. Otherwise explicitly discard the seed candidate in the transfer audit.

Any maintained seed must use a **durable evidence reference** such as the eventual reconciliation PR plus durable destination — never the temporary workspace commit SHA as its long-term evidence reference.

## Temporary evidence/handoff cleanup before merge

The final reconciliation PR's tree should contain durable reconciliation only, not the research archive.

Before the PR is considered ready to merge, remove these temporary workspace files from the branch's **current tree** unless one has been deliberately transformed into a durable authoritative artifact (normally none should be):

- `docs/research/investigations/security-ownership-and-assurance-boundary.md`
- `docs/research/investigations/security-ownership-and-assurance-boundary-adversarial-review.md`
- `docs/research/investigations/security-ownership-and-assurance-boundary-adversarial-review-prompt.md`
- `docs/research/investigations/security-ownership-and-assurance-boundary-reconciliation-prompt.md`

Deleting them in a later commit is correct: their historical `commit SHA + path` evidence identities remain intact.

Do not delete or retire the workspace branch before the reconciliation has landed and its independent PR review has validated the transfer audit.

## PR and authority transition

This reconciliation is an ordinary repository change and must go through the normal independent PR-review lifecycle.

The PR description should identify the decision-quality evidence packet by exact coordinates:

- report `ef4130a13fb68749563a642126a75ecd449dab7b` + report path;
- adversarial review `f99555b649c72c8761205e3aecfe068dad7e7fbe` + review path;
- disposition `ACCEPT WITH QUALIFICATIONS`;
- the live-main SHA used as the reconciliation baseline.

Do not represent the Researcher as having approved the implementation. The independent PR Reviewer must assess the actual reconciliation diff normally.

If the reconciliation produces a durable no-action result for any recommendation, record the reason in the appropriate maintained authority or PR transfer audit rather than silently dropping it.

## Completion criteria

The reconciliation slice is complete only when all of the following are true:

1. live `main` was re-grounded and the evidence workspace was updated without rewriting either handed-off evidence SHA;
2. every surviving report consequence and Q1–Q8 qualification is accounted for;
3. no new Security owner/track/module/ADR or parallel assurance authority was introduced without new evidence;
4. current security policy/testing/reviewing surfaces tell the truth about what is implemented, verified, deferred, and triggered;
5. any included product fix has executable regression evidence;
6. issue #313 is not duplicated and its current authority/state is reflected accurately;
7. live repository settings changed by this slice are read back and verified;
8. the knowledge-transfer audit is complete;
9. temporary report/review/handoff files are absent from the final PR tree;
10. the reconciliation PR names the exact research/review evidence coordinates and is ready for normal independent PR review;
11. the workspace is marked retirement-eligible **only if** every material result has a durable destination, qualifying seed, owned follow-up, or explicit discard decision.

Do not mark the research outcome `CONCLUDED` merely because reconciliation commits exist. It becomes concluded only when the durable reconciliation actually lands (or an explicit durable no-action result lands), the independent PR review validates the transfer, and the workspace-retirement conditions are satisfied.

## Expected final handoff

Return:

- reconciliation workspace branch;
- exact reconciliation head SHA;
- live-main baseline SHA used;
- concise list of durable surfaces changed;
- any GitHub-side settings changed and their verified post-change state;
- validation commands/results;
- issue #313 status and how duplication was avoided;
- knowledge-transfer audit summary;
- whether a synthesis seed was added/extended or explicitly not justified;
- list of temporary research/handoff files removed from the final tree;
- reconciliation PR number/URL if opened;
- whether the workspace is retirement-eligible after merge.

If any required external repository setting cannot be changed or verified with the available permissions, return that as a concrete reconciliation blocker rather than inventing a repository claim that the setting is active.