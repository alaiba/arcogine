# Independent evidence challenge — core reliability Phase B Campaign 1

## Role, independence and authority

Act as an independent adversarial assurance challenger, not as the original implementer, experiment/oracle author, executor, triager or adjudicator. This is an owner-delegated independent challenge of the **existing Campaign 1 evidence**, commissioned by [issue #541](https://github.com/alaiba/arcogine/issues/541). The evidence author/executor/selector/triager was one Claude Code Opus 5.5 session. Prefer a different independent session and model family and disclose your identity, access limitations and any conflicting involvement. Do not self-certify or replace proof with a green CI check.

Re-ground from live `main`, `AGENTS.md`, relevant role instructions, `.github/CONTRIBUTING.md`, the live issue, `docs/development/testing.md`, and normative Factory/Types/Engine/Storage/Governance contracts. Resolve both historical design/review inputs and the evidence packet at exact revisions, not search snippets.

## Immutable input packet

- Branch: `assurance/core-reliability-phase-b-campaign-1`
- Exact commit: `f69566784bde479cc86d27d353573d98e7c283f1`
- Report: `workspace/assurance/phase-b-campaign-1/report.md`
- Evidence tree at that commit: `workspace/assurance/phase-b-campaign-1/`, including `experiment-sources/`, `oracles/`, `mutation/`, `tools/`, `results/`
- Tested source/test/spec baseline: `a0feeffcdec4a932e3ddbb62ce2551cc2540b1e4`. Do not silently substitute live `main`; reconcile changed contracts separately.
- Accepted Phase A design at `baf2e2a94750a350cc533663d52c4c147f41d9ba` and independent Step 2 acceptance at `b9d60d3ba8aa7fdc425fc719061ea34e1ddbefc8` are historical inputs, not executable proof.
- Retained test assertions were delivered through PR #542; inspect its **live** merged state and current exact-head/merge history rather than assuming a stale PR status.

## Bounded challenge

Challenge only `T-VALUE`, `T-TIME`, `F-PUBLISH`, `F-CODEC`, `F-ASSEMBLE`, `F-CHANGE`, with `G-CHANGE` as the bounded downstream seam. Do not execute Campaigns 2–4, perform Phase D's core-wide verdict, write production fixes, add new semantics, or merge branches. Avoid re-running the accepted Phase A design review. Independently examine:

1. **Provenance and exact baseline.** Validate the 300-to-313 inventory crosswalk (changed/added paths, 33-group mapping), source/test SHA consistency, toolchain and classfile provenance, clean baseline, discovered test names and counts, deterministic seeds, raw outcome retention and original/minimized witnesses.
2. **Independence of oracles.** Inspect oracle implementations and the normative specifications line-by-line for correlated assumptions, imports or derivation from product code, hidden reuse of algorithms under test, misleading pass conditions, missed refusal/post-state checks and false generalization from bounded generation.
3. **Slice 1.** Critically test independent `BigInteger` time boundaries, UUID version/variant/spelling and typed-ID domain separation; all Factory publication predicates, multi-predicate interaction and complete diagnostics; copy isolation, U1 scheduling limits and U2 signed-ID order across fresh JVMs.
4. **Slice 2.** Verify the literal binary grammar, independently derived golden bytes/digests, 64-bit lengths/counts, signed I64 order, nullable IEEE-754, NaN and signed zero, UTF-8 scalar rejection, canonical round-trip refusal, semantic identity, current-definition support and binding; distinguish present validity from historical provenance and collision resistance. Inspect 8,489 single-edit and 3,000 seeded-model claims against scripts/raw results and identify two-plus-edit and allocation frontiers honestly.
5. **Slice 3.** Challenge assembly of authored facts, routing, session isolation and metadata-vs-execution separation, 4,500-pair semantic edit oracle, real revision/Storage/Governance seam, exact controlled-revision vs Factory fingerprint identity and refusal/post-state behavior.
6. **Mutation sensitivity.** Independently inspect each of the 44 targeted mutants and the 37 killed/7 survivor ledger. Confirm clean compilation and test discovery were mandatory for semantic kills; challenge equivalence claims and the three selected retained regressions. Independently check that no product defect or test/oracle error is misclassified.
7. **Discrepancies and frontier.** Reassess D1–D5, especially undecided D4/D5 without inventing contracts, the two Governance mutants seen only by retained tests, Engine-site T-TIME and Storage durability prerequisites. Keep those dependencies blocked/qualified when they belong to later campaigns.

## Method and acceptance standard

Use risk-weighted, reproducible independent spot checks and discriminating counterexamples. Do **not** merely summarize or endorse the producer's report. Read the actual sources and raw results, reproduce meaningful controls on an isolated exact-source tree where feasible, and identify any environment limits. A test or oracle mismatch must include minimally reproducible inputs, commands, exact expected and observed output, owning contract and classification: implementation violation; oracle/test error; undecided semantics; unsupported domain; or environment limitation.

For every family, state `ACCEPTED`, `ACCEPTED WITH QUALIFICATIONS`, or `NOT ACCEPTED` as your independent challenge disposition, alongside the producer's separate `EXECUTED` / `BLOCKED` / `RESIDUAL-EXCLUDED` status. Never promote merely executed evidence to independently assured. Record author, executor, selector, triager and independent adjudicator; identify unresolved conflict, coverage, provenance or baseline concerns. Do not treat this family review as a core-wide verdict.

## Output, custody and stopping rule

Write a self-contained, evidence-linked challenge report under a semantic branch-transient `workspace/assurance/` or `workspace/review/` path **on a dedicated review branch**, keeping the source packet immutable. Include per-family findings, exact reviewed evidence SHA, reproduced commands/outcomes, challenged assumptions, risk-rated independent qualifications, and explicit next owner decision. Preserve original/minimized counterexamples in that branch's workspace, not on `main`. Follow AGENTS.md owner-Git-identity requirements, commit without rewriting and verify the published commit and file are retrievable. Publish an issue-visible, minimal locator comment on #541 identifying the review branch, exact full commit, path, findings/disposition summary and next action. Do not paste the whole report into GitHub or chat. Do not open a merge PR for transient evidence and do not merge anything.

Stop with an explicit `BLOCKED` finding where independent review cannot inspect or reproduce necessary material. Any confirmed product defect must be routed to separately owner-authorized Phase C remediation; do not fix it during this challenge. Return control to the owner after publication for issue #541's ultimate disposition and any later Phase D judgment.
