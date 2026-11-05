# Secret-scan failures across unrelated Arcogine revisions

## Authority and question

- Investigation date: 2026-11-05.
- Live-main baseline: `140d9c08efa3de720ab148fa281e28911b2460f7`; rechecked against the remote during investigation.
- Question: why does the secret scan fail across unrelated PRs and main, which process changes exposed the failure, and what short- and long-term remedies preserve security coverage and evidence custody?
- Scope: the linked CI run, related live runs, scanner behavior, Git reachability and evidence-publication process. No production changes, workflow edits, branch deletions or history rewriting.
- Register: no existing brief for this bounded incident was identified; the owner's request supplies its scope. No register lifecycle transition is asserted.
- Risk: medium for reversible CI/process recommendations; changes to security coverage deserve independent review. No independent adversarial review has occurred; checks here are self-administered. This is diagnostic evidence and proposed process policy, not adopted security architecture or an implementation handoff.

## Executive conclusion

High-confidence diagnosis: unrelated retained branch history contaminates the required revision-level secret scan. Gitleaks 8.30.1 defaults to scanning all fetched refs. CI checks out full history, fetching all remote branch heads and tags. A review-evidence commit outside main contains 46 false-positive matches: two compiled-class SHA-256 values repeated across 23 JSON result files. The resulting failure propagates through the correctly fail-closed aggregate gate.

The immediate trigger is commit `58bcd62e316113ccb55b91f3d0e08c551e2115c8`, authored and committed at 2026-10-29 22:00:55 UTC (October 30 00:00:55 in Bucharest). It is reachable from retained `issue/541-campaign-1-regressions`, whose current head is `d70df3be49c66e8618578554a234a0704fcecd6d`, but is not an ancestor of main or PR #546. It records independent challenge evidence after the regression PR had merged. Consolidation into that issue branch at 22:05:25 UTC retained the offending commit.

Two problems need separate treatment: the scan's accidental cross-branch scope, and false-positive adjudication for published evidence. Restricting the revision gate alone does not establish that all other published refs are safe.

## Verified repository evidence

1. The [reported job](https://github.com/alaiba/arcogine/actions/runs/40968936724/job/123784467601) fetched `+refs/heads/*:refs/remotes/origin/*`, tags and the PR merge ref. Gitleaks installed successfully, scanned 674 commits, found 46 matches, and exited 1. Every finding names the same evidence commit, the same `generic-api-key` rule and files under `workspace/review/core-reliability-campaign-one/results/`.
2. [Main push run](https://github.com/alaiba/arcogine/actions/runs/40960755447) at 2026-10-29 21:47:46 UTC passed its Secret scan. The subsequent PR #546 run was the first failed CI run in the inspected recent CI history. Scheduled main runs from October 30 through November 4 and later unrelated PR runs failed. This chronology identifies the observed onset, not a repository-wide claim about every historical run.
3. [November 4 main job](https://github.com/alaiba/arcogine/actions/runs/41832938463/job/126584446249) reports the same 46 matches and evidence commit, with 675 commits scanned. [November 4 unrelated devcontainer PR job](https://github.com/alaiba/arcogine/actions/runs/41892569671/job/126793564469) reports the same 46, with 676 commits scanned. Java and dependency-audit jobs passed in both runs. Main remained at the same SHA as the successful October 29 push.
4. At the baseline, `.github/workflows/ci.yml` always runs the Secret scan and uses `fetch-depth: 0`, then `gitleaks detect --source . --config .github/security/gitleaks.toml --verbose`. It sets no `--log-opts`. Its push trigger covers main only; a push to a closed issue branch has no dedicated CI trigger. The aggregate gate requires the scan to succeed.
5. `.github/security/gitleaks.toml` extends default rules and only adds a coverage-artifact path exception. There is no exception for the reported evidence. The pinned scanner version and default command were already present in commit `a1daf73` on August 26. This incident does not require a scanner upgrade to explain it.
6. [The evidence producer](https://github.com/alaiba/arcogine/blob/58bcd62e316113ccb55b91f3d0e08c551e2115c8/workspace/review/core-reliability-campaign-one/capture_run.py) explicitly computes `hashlib.sha256(file.read_bytes()).hexdigest()` for compiled classes and records them under `classesSha256`. The matched names are `RequirementCatalogue$RegistrationKey.class` and `StateReferenceAuthority$Key.class`. Their names contain `Key`; their high-entropy hexadecimal digest values satisfy the generic detector. The detection is of digest metadata, not evidence of an authentication credential. Original class bytes were not rebuilt to independently reproduce the two digest values.
7. PR #546 changes ten workflow-documentation files, not scanner configuration. It remains open at head `dee804e297c47aca4b82e6121ddd692f2df40ed2`. Its proposed retirement of workspace exchanges is not landed policy and cannot explain the earlier evidence commit or fix the scanner by itself.

## External implementation evidence, verified at the pinned version

- [Gitleaks 8.30.1 sources/git.go](https://github.com/gitleaks/gitleaks/blob/v8.30.1/sources/git.go#L66-L88): absent log options, invokes Git history traversal with `--full-history --all --diff-filter=tuxdb`. Explicit log options replace that default argument sequence. This directly explains cross-ref inclusion.
- [Gitleaks 8.30.1 cmd/detect.go](https://github.com/gitleaks/gitleaks/blob/v8.30.1/cmd/detect.go): legacy `detect` performs Git-history scanning unless explicitly told otherwise; the documented replacement is `gitleaks git`.
- [Default generic rule](https://github.com/gitleaks/gitleaks/blob/v8.30.1/config/gitleaks.toml#L562-L578): key-related assignment patterns and entropy threshold 3.5 explain why the two class-name/digest assignments match. Values in the job have entropy approximately 3.87.
- [Versioned README](https://github.com/gitleaks/gitleaks/blob/v8.30.1/README.md): documents fingerprint ignores, baselines, redaction and explicit Git-log options. Any proposed exemption still needs validation against the pinned binary and positive detection controls.

## Five whys

| Why | Evidence-backed answer |
| --- | --- |
| 1. Why does CI fail for unrelated changes? | Required Secret scan exits 1; the aggregate gate correctly propagates failure. |
| 2. Why does the scanner exit 1? | It finds 46 generic-key matches in review JSON: two class digests repeated in 23 files. |
| 3. Why does that review packet affect main and another PR? | Full checkout fetches all branch refs; unqualified Gitleaks defaults to `--all`. The packet need not belong to the checked-out revision. |
| 4. Why is this history published and still reachable? | The process deliberately commits transient evidence, preserves exact artifact commits and retains branches. This packet was published after its issue's implementation merged. Tree cleanliness on main does not remove other refs or history. |
| 5. Why did the process allow this to become a global delivery failure? | Evidence custody evolved without a corresponding explicit scanner-scope and evidence-publication validation contract. CI conflates revision acceptance with all-published-history inspection, while off-main evidence pushes can avoid the PR/main trigger paths. |

The fifth answer is a systems-level inference from the contracts and workflow, not an assertion about an individual's intent or whether a particular agent privately attempted local validation.

## Work-process changes that exposed the latent coupling

- September 18, [transient workspace handoffs](https://github.com/alaiba/arcogine/commit/3d07205faea8858a9479290a4aac9b1e26b1495c): introduced deliberate committed, branch-local evidence plus a final-tree prohibition on workspace files. The new checker validates the merge tree; it was never a history scrubber.
- October 15, [branch safety and retrospective evidence](https://github.com/alaiba/arcogine/commit/476e40738b5c63c68bea4b14fe788af64bc2f619): made preservation of branch history explicit and prohibited implicit branch deletion. This is a legitimate custody safeguard, but retained refs enlarge an implicit all-ref scan indefinitely.
- October 24, [workspace exchanges](https://github.com/alaiba/arcogine/commit/896a496): expanded append/commit/push coordination on implementation and research branches. This increased exposure to the latent mismatch; the particular trigger is a review-results packet, not exchange Markdown itself.
- October 29/30: the independent challenge captured class digests in JSON, published them after the regression merge and consolidated them into the retained issue branch. A post-merge evidence push did not require changing main to affect the next scan.
- The current contribution guidance makes the full local security scan conditional; its pre-commit contract concerns human identity. Required CI covers main pushes and open PR events, not every evidence-publication push. There is no demonstrated enforced scan immediately before this packet's publication.

Root causes are therefore: implicit cross-ref scope; legitimate digest data colliding with a generic heuristic; and a validation-ownership gap for evidence publication. Branch retention is an amplifier and intended constraint, not misconduct or a reason to destroy evidence. Merely reducing branch count or retiring exchanges would leave the underlying coupling intact.

## Reproduction and discriminating cases

Used the official `ghcr.io/gitleaks/gitleaks:v8.30.1` image, digest `sha256:c00b6bd0aeb3071cbcb79009cb16a60dd9e0a7c60e2be9ab65d25e6bc8abbb7f`, with the baseline configuration and a read-only repository mount. No suitable devcontainer was running, so an ad hoc Docker container was used. Native Git supplied exact objects and ancestry checks. Connector job logs and public GitHub REST supplied live run metadata; `gh` was unavailable locally.

Common invocation: `gitleaks detect --source /repo --config /repo/.github/security/gitleaks.toml --redact`.

| Case | Result | Implication |
| --- | --- | --- |
| Default history scope | Exit 1; 676 commits; 46 matches, 23 files, one evidence commit and rule | Reproduces the reported mechanism locally. |
| Add `--log-opts=origin/main` | Exit 0; 668 commits; no findings | The same configuration passes main's own reachable history. |
| Add `--log-opts=origin/issue/545-unify-issue-admission-workflow` | Exit 0; 669 commits; no findings | PR #546's branch history is not the source. |
| `git merge-base --is-ancestor 58bcd62 origin/main` | Exit 1 | The evidence commit has not contaminated main ancestry. |
| Live remote-head and containing-ref inspection | Evidence reachable through retained issue branch | Explains why fresh CI fetches still encounter it. |

The explicit-ref tests validate the causal distinction, not a complete proposed CI implementation. They do not test a synthetic credential, merged-result-only content or missing-ref fail-closed handling. Those are required acceptance cases for the eventual change. No Java tests were necessary for this diagnostic investigation.

Rejected hypotheses: PR #546 introduced the detections; a runner/install outage caused failure; a newly changed scanner rule is necessary; deleting files from the current tree clears history; a rerun alone cures the failure. The observed evidence contradicts each. Credential compromise is not established by these 46 findings; this does not certify the whole repository or every published object secret-free.

## Short-term recommendations

1. Preferred repair: make the required scan's revision boundary explicit. Preserve full history for the selected PR head/merge candidate or main revision; remove dependence on arbitrary fetched refs. For a minimal change, explicit `--log-opts` selecting the intended revision's history can do this while retaining history scanning. Select and test PR head plus merged-result semantics deliberately; do not substitute a shallow current-directory-only scan. Align the local canonical command with the intended mode.
2. Handle the known evidence false positives independently. If keeping all-ref scanning immediately, reviewed fingerprint-specific ignores for these 46 exact occurrences offer a narrow tactical unblock. A strictly scoped rule/path/line-shape exception for class-digest records may be more maintainable, but requires proof that nearby real credential patterns still fail. Prefer exact fingerprints for urgent recovery over excluding an entire commit or workspace tree. This tactical option was assessed, not implemented or binary-tested here.
3. Add redaction to CI output and retain machine-readable, access-controlled diagnostic output sufficient to identify commit, file, rule and fingerprint. The existing verbose command would print a real secret if one were detected.
4. Rerun affected PRs and the scheduled/main scan after the chosen fix reaches the appropriate revision. The incident closes only when representative PR/main checks pass and synthetic-secret controls still fail. No workflow was modified or rerun by this investigation.

Do not disable the gate, globally exclude `workspace/`, suppress all 64-character hexadecimal values, erase the evidence commit, or delete the branch as an operational workaround. Excluding unrelated refs from the revision gate must be paired with an explicit place to scan published evidence; otherwise it merely hides a separate security responsibility.

## Long-term recommendations

Separate three responsibilities using the same pinned scanner and shared invocation policy:

| Responsibility | Intended scope and owner |
| --- | --- |
| Required PR/main validation | Explicit candidate revision/history and merged-result coverage; implementation owns failures attributable to that candidate or its base. |
| Evidence publication | Staged content plus outgoing commit history on every publication path, including research/review/handoff branches and post-merge appends. Publisher owns triage before pushing. A directory or tip-only scan misses a secret added then removed within outgoing history. |
| Repository-wide security sweep | Explicit inventory of retained published refs; security findings routed to their source branch/custodian. Real credentials receive incident response regardless of whether their branch is merged. Do not silently attach unrelated all-ref failures to arbitrary PRs. |

Add executable scanner-scope tests: a clean candidate remains green when an unrelated evidence ref appears; a synthetic secret in candidate history fails even after tip deletion; a real-pattern credential inside workspace evidence fails; only vetted digest occurrences are ignored; missing required history or scan errors fail closed; local and CI semantics agree. Include actual scanner integration coverage, not only stubbed CLI invocation tests.

For future evidence, prefer structured records separating `path` from `sha256` rather than making a key-containing class path the JSON assignment key; test that representation under the pinned scanner. Preserve existing exact evidence commits. Apply artifact minimization: retain the evidence needed for reproducibility/custody, keep ad hoc logs local, and use another durable artifact surface only if its explicit retention/access contract satisfies the existing custody requirement. Changing evidence storage is optional and is not necessary to fix this incident.

Require process changes involving branch lifetime, handoffs or generated artifacts to evaluate CI scope, trigger coverage, publication validation and data exposure together. Keep the owner-only deletion rule. An explicit scope contract makes correctness independent of branch cleanup habits.

## Limitations, consequences and follow-up

High confidence in the mechanism, established by logs, pinned upstream source, producer code, ancestry and controlled scan comparisons. The original compiled bytes were not rebuilt. The investigation sampled the incident, latest main and latest unrelated PR logs rather than downloading every run. It did not enumerate inaccessible/deleted refs or certify all credentials absent.

The root-cause chain is ready to inform a bounded remediation issue. The proposed security-boundary change still needs independent review and the acceptance cases above; no claim of independent research approval is made. Durable destinations are the CI/security invocation and tests, testing/contribution guidance, and evidence-publication procedure. A separate implementation authorization is required under repository work admission. This report is not that authorization or a complete implementation prompt.

Preserve the tested fact that a retained ref can change default scanner results for an unchanged main SHA; it is the most reusable regression case. Future scanner upgrades, changes to checkout refspecs or evidence custody should rerun that case. Do not preserve this entire report in maintained documentation merely for historical completeness. Its findings and qualifications should transfer to the owning surfaces before workspace retirement.
