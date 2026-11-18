# Evidence, finding format and lifecycle

Part of the [PR Reviewer contract](../pr-reviewer.md#review-procedure); every complete review, re-review and `./` continuation follows all of its routed topics.

## Evidence rules

Every actionable finding must establish:

1. what the proposed change does or claims;
2. the conflicting contract, invariant, requirement, consumer, or missing evidence;
3. why that matters to correctness or merge readiness;
4. the outcome/invariant remediation must restore.

Prefer exact paths, symbols, test names, acceptance criteria, specification sections, PR numbers, and commit/head SHAs.

Use confidence `HIGH`, `MEDIUM`, or `LOW`. Do not inflate confidence because CI is green.

Every actionable finding must use one precise category: `CORRECTNESS`, `ARCHITECTURE`, `DETERMINISM`, `OWNERSHIP_BOUNDARY`, `COMPATIBILITY`, `IDENTITY_PROVENANCE`, `DOCUMENTATION_ACCURACY`, `TEST_EVIDENCE`, `SCOPE`, `TOOLCHAIN_CI`, `SECURITY_AUTHORITY`, or `PR_RECONCILIATION`.

`PR_RECONCILIATION` is for an actual reconciliation defect, such as an incorrect synchronization result or broken history invariant. A merely behind-base branch is normalized before review and is not itself a finding.

## Finding format

Assign each genuinely new actionable finding the next unused `REV-<N>` number monotonically across that PR's review lifecycle. Never reuse an identifier for a different semantic defect. Carry the same identifier forward while reconciling that finding; a finding that has been resolved or found inapplicable remains represented with its final status rather than disappearing from the lifecycle record.

```text
REV-<N> - concise title

Category: <category>
Confidence: HIGH | MEDIUM | LOW
Head: <reviewed PR head SHA>
Subject: <semantic subject>

Problem:
<what is wrong>

Evidence:
<paths/symbols/tests/specifications/criteria and relevant facts>

Why it matters:
<correctness, architecture, compatibility, evidence, or merge-risk consequence>

Required invariant/outcome:
<what remediation must restore without unnecessarily prescribing implementation>

Status: OPEN
```

The status value is `OPEN`, `RESOLVED`, or `OBSOLETE`; `OPEN` is shown in the example. Do not manufacture a finding merely to populate the format. Optional polish or future work that does not need correction on this PR head belongs in review prose or a durable follow-up issue, not in a `REV-<N>` block.

Each block must contain exactly one value for every labeled field shown above. `Head` is the full SHA of the head reviewed for that review. The category, confidence, title, and semantic subject identify the finding and remain stable when its `REV-<N>` is carried forward; update only `Head` and `Status`. If a finding must be materially reclassified or the subject changes, close the old identity as `OBSOLETE` and assign a new unused identifier to the distinct finding.

## Finding lifecycle

Review continuity belongs in evidence-backed findings, not unverified conversational memory. On re-review, carry prior findings forward with their same identifiers, verify them against the new head, set each status to `OPEN`, `RESOLVED`, or `OBSOLETE`, describe regressions in review prose, and create new IDs only for genuinely new defects. A status change records lifecycle progress; it does not authorize ID reuse.

If prior review history cannot be inspected, say so rather than claiming all previous findings are resolved.
