# Campaign 1 workspace exchange

## REPORT — Independent challenge and corrected issue custody

- Kind: REPORT
- From / To: Independent challenger -> Campaign 1 producer and repository owner
- Work source: issue #541
- Revision: `issue/541-campaign-1-regressions`, incorporating the independent challenge originally committed at `58bcd62e316113ccb55b91f3d0e08c551e2115c8`

The independent challenge and all its reproducing sources/results now live on this existing issue
branch at `workspace/review/core-reliability-campaign-one/independent-challenge.md` and its sibling
files. The original challenge commit is preserved in ancestry. Further issue continuation uses this
branch and this append-only exchange; do not follow the historical prompt's instruction to create
a separate review branch. The owner explicitly corrected that custody choice.

The challenge reproduces all 37 producer campaign tests and records three demonstrated observation
gaps: non-resource Unicode diagnostic messages, exact Storage refusal categories, and unreferenced
operation assembly. Byte/mutation scope needs narrower wording, and original exact-run classfile
and per-test/failure provenance remains blocked in the immutable producer packet. No production
violation was confirmed. The three merged regression assertions detect their target mutants.

Next substantive action: the campaign producer revises the evidence against the report's CH-1–CH-4
findings and provenance blocker, preserving the original packet and identifying any superseding
execution baseline. Publish the revised evidence and append the result here on this branch, then
return control for targeted independent follow-up. The owner’s live delegation authorizes turns;
this report does not start another participant, authorize production remediation, close the issue,
or supply a Phase D assurance verdict.

## REPORT — Partial current-main witness rerun

- Kind: REPORT
- From / To: Campaign continuation executor -> Campaign producer and repository owner
- Work source: issue #541
- Revision: `issue/541-campaign-1-regressions`, following the independent challenge at `d70df3be49c66e8618578554a234a0704fcecd6d`
- In reply to: `d70df3be49c66e8618578554a234a0704fcecd6d`

A partial current-main rerun is recorded in `workspace/assurance/phase-b-campaign-1-revision/report.md` with test XML and classfile hashes. It pins source `cb1afadaf683f915a31b09df2fd732da10515df4`, reports clean Factory/Storage/Types suites and six existing challenge witnesses passing on native Windows JDK 21, and records the original provenance and corpus limitations. The actor is the prior challenger; this is neither a complete evidence revision nor independent follow-up. Continue the remaining corpus, mutation and provenance work and then arrange a separate independent challenge. Resolve main again at disposition. The issue remains open.

## REPORT — Superseding Campaign 1 evidence revision completed

- Kind: REPORT
- From / To: Continuing evidence revision executor -> Repository owner and separate independent challenger
- Work source: issue #541
- Revision: `issue/541-campaign-1-regressions`, evidence commit `bfdffa89f3c6a13887ce2c4d4c70fc0b96f54802`
- In reply to: `eb18b98d45ea17396ed7b63a4618a6ecf2887ecb`

The completed revision is at
`workspace/assurance/phase-b-campaign-1-revision/completed/report.md` in the evidence commit above.
Its sibling sources, execution tools, original JUnit XML, per-test failures, source/classfile
manifests, inventory and mutation triage form the reproducible packet. This supersedes the partial
rerun for current execution evidence and preserves every earlier packet and exchange message.

The source/test/specification baseline is `cb1afadaf683f915a31b09df2fd732da10515df4`, resolved as live
main at entry and again immediately before publication. No product main Java source changed since
the original campaign baseline. The refreshed 313-path inventory retains all 33 contract groups.

CH-1 now compares independently rendered complete field/message sequences across the 100 boundary
cases, 4,096 predicate combinations and eight fresh JVMs. CH-2 observes exact Storage categories,
candidate absence, unchanged revision enumeration and historical bytes after each refusal. CH-3
directly checks every authored operation in the 500-model corpus, including zero-product and
partially referenced models. CH-4's selected-alphabet byte boundary and diagnostic-only detections
are explicit, and fail-closed classification has 12 synthetic controls.

Native Windows Temurin 21.0.13+9 / Gradle 9.8.2 execution passed the clean retained selection
(305 tests), revised campaign (43 tests), and final restored campaign (43 tests). All 47
mutation/control runs discovered the same 43 tests: producer triage records 36 bounded semantic
kills, two diagnostic-only detections, one binding-inclusion observation, and eight survivors
(three equivalent controls, four redundant guards, one uncovered extractor-support guard).
All three challenge controls are detected. Five conservative automatic exception classifications
required explicit test-body/stack triage; the original automatic classifications remain visible.
No timeout, compilation or discovery failure was counted as a semantic kill.

The independent Node grammar derivation, inventory audit, classifier controls and provenance audit
pass. All 153 compiled main classes match between clean retained and corpus runs; all source and
class hashes match after restoration; each mutant changed exactly its intended Java source.
The report records precise commands, environment, finite generator bounds, witnesses and exclusions.

No production violation was confirmed or remediated. No maintained tests/specifications changed,
no new PR was opened, and full `./arcogine check`, security scans, coverage and exact-head
`CI / gate` were not run for this transient evidence-only branch. Original-run missing provenance
remains unavailable; the new complete records are explicitly a superseding run, not reconstruction.
Other campaigns, historical/durability prerequisites, broader byte/model domains and Phase D remain
blocked or excluded exactly as the family ledger states.

Next action: the owner delegates a separate targeted independent challenge of this exact revision.
This executor selected, authored/revised, executed and triaged evidence and cannot independently
adjudicate it. This report does not close issue #541, authorize another participant, or supply an
assurance verdict. Keep the issue open pending that independent follow-up.
