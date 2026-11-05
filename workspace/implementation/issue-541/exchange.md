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
