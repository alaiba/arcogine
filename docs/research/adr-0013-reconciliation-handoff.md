# ADR-0013 durable operational identity reconciliation — handoff prompt

> **Artifact type:** Temporary implementation/ADR handoff prompt
> **Workspace:** `docs/agency-and-operational-identity-research-reports`
> **Prompt baseline — live `main`:** `cbfcd36fb8f72ac39cc2896de97cc0113a22e41b`
> **Prompt baseline — workspace head before this file:** `115fe4465bda04690d5bd27cca47a74438c24d0a`
> **Authority:** Delivery handoff only. Repository authorities and exact research evidence control over this prompt.
> **Cleanup:** This file is temporary and must be absent from the final reconciliation PR tree.

You are executing the **durable reconciliation of the Operational Identity research into ADR-0013 and its directly dependent maintained documentation** in the canonical Arcogine repository `alaiba/arcogine`.

This is **not** a new research run, not a new adversarial review, and not an Operational implementation slice. Decision-quality research evidence already exists. Your job is to translate the surviving, corrected research result into durable repository authority, reconcile dependent maintained state, and prepare the change for normal independent PR review.

## 1. Repository and branch rules

Read `AGENTS.md` first and follow it exactly.

Use the existing finite research-evidence workspace:

```text
docs/agency-and-operational-identity-research-reports
```

Do **not** create a fresh reconciliation branch merely because the work has moved from research into durable reconciliation. `docs/development/researching.md` defines reconciliation as a separate phase/authority transition, not a separate branch by default.

Before editing anything:

1. resolve live `main` and record its exact SHA;
2. resolve the current head of `docs/agency-and-operational-identity-research-reports` and record it separately;
3. confirm the handed-off evidence commits listed below remain reachable from the workspace;
4. if live `main` has advanced beyond the workspace's incorporated base, update the workspace **history-preservingly** using a merge-style update/platform-equivalent; do not rebase, force-push, amend, or otherwise rewrite handed-off evidence SHAs;
5. after any update, verify the previous workspace head remains an ancestor and the new live `main` is incorporated.

At prompt generation, live `main` was `cbfcd36fb8f72ac39cc2896de97cc0113a22e41b`, and there was no open PR matching ADR-0013 / durable Operational identity. Re-check both facts; do not assume they are still true.

## 2. Read these authorities and evidence first

Perform a quick repository search under `docs/` for at least:

```text
ADR-0013
durable operational identity
operational identity
accountable operational continuation
continuity
fork
split brain
divergence
acceptance
raw observation
operational execution digital twin
```

Then read, in this order:

### Repository operating / reconciliation rules

- `AGENTS.md`
- `.github/agents/work-planner.agent.md` for the handoff contract only; you are now the implementation/reconciliation owner, not running another planning pass
- `docs/development/researching.md`, especially the promotion/reconciliation, evidence-custody, knowledge-transfer-audit, and workspace-retirement rules
- `docs/development/reviewing.md`
- `docs/architecture/decisions/README.md`

### Current durable / maintained surfaces on live `main`

- `docs/architecture/decisions/0013-execution-context-identity.md`
- `docs/architecture/operational-execution-digital-twin.md`
- `docs/planning/operational-execution-digital-twin-readiness.md`
- `docs/research/research-register.md`
- `docs/research/investigations/operational-execution-digital-twin-boundaries.md`
- applicable Accepted neighbors, especially ADR-0008, ADR-0011, ADR-0012, and ADR-0015
- `docs/product/charter.md` only where a reconciliation statement relies on a Charter principle

Treat Proposed architecture and Proposed ADR material as proposed until this reconciliation deliberately changes that status. Do not use the research documents themselves as durable authority.

### Exact Operational Identity evidence coordinates

Read the complete artifacts, not summaries from this prompt:

1. **Source report**
   - commit: `bf744af171d5a42a3c578d33836e8e45bd33b0fe`
   - path: `docs/research/operational-execution-digital-twin-identity-report.md`

2. **Independent adversarial review**
   - commit: `f6a3b7148fadc9c29652f5bf269606333299496c`
   - path: `docs/research/operational-identity-adversarial-review.md`
   - disposition: `ACCEPT WITH QUALIFICATIONS`

3. **Successor correction note**
   - commit: `794b65b70c1849e13eae592d0bce053006382050`
   - path: `docs/research/investigations/operational-identity-adversarial-review-corrections.md`
   - disposition impact: the original `ACCEPT WITH QUALIFICATIONS` remains unchanged

The successor correction controls only where it explicitly narrows the original review. Do not rewrite the old review to make it look as though the corrected wording was always there.

The coupled synthesis at `bf744af171d5a42a3c578d33836e8e45bd33b0fe` may be consulted for cross-question context, but do not let the Agency question broaden this ADR reconciliation.

## 3. Objective

Reconcile the decision-quality Operational Identity evidence into **ADR-0013** and directly dependent maintained documentation.

The intended durable result is:

- ADR-0013 no longer carries an unresolved equality/lifecycle hold;
- the ADR defines the accountable operational continuation semantics that survived independent review and the successor corrections;
- the ADR becomes **Accepted** if fresh repository grounding reveals no new semantic blocker;
- the Operational architecture accurately reflects the Accepted identity decision while remaining honest about its other Proposed material;
- the Operational readiness plan no longer cites ADR-0013's identity semantics as unresolved, but remains **NOT ADMITTED** unless all separate promotion conditions for a concrete implementation slice are independently satisfied;
- the durable Operational Identity research question reaches `CONCLUDED` in the maintained register atomically with the durable reconciliation;
- no production type, module, persistence format, registry, coordination mechanism, or implementation slice is introduced in this change.

If fresh grounding reveals a real contradiction or a material new unresolved semantic question that prevents accepting ADR-0013, do not invent an answer merely to complete the handoff. Stop the acceptance transition, record the blocker precisely, and keep the research lifecycle truthful.

## 4. ADR-0013 decision contract

Revise the existing **Proposed** ADR in place; it has never been Accepted, so this is not a semantic mutation of an Accepted historical decision and does not require a superseding ADR.

The ADR must preserve the withdrawal of the old `ExecutionContextKind` / `PRODUCTION | STAGING | SIMULATION` model. Do not revive that taxonomy under a new name.

The reconciled decision must encode the surviving semantics below. Use the exact research artifacts for full reasoning and qualifications; this section is a closure checklist, not a substitute for reading them.

### Referent and separation

Define the durable identity as identifying one **accountable operational continuation**: one independently continuing body of Arcogine-owned conduct and conclusions maintained as one account/history.

Keep it distinct from at least:

- `RunId`;
- `ModelFingerprint`;
- `ControlledRevisionId`;
- `EngineSemanticsVersion`;
- actor identity;
- deployment identity/location;
- external target identity;
- external subject identity;
- a physical installation;
- a digital-twin object/category;
- a global simulation/staging/production classification.

Identity must be established explicitly and must not be inferred from infrastructure, endpoint, namespace, target, actor, run, revision, fingerprint, or execution mode.

### Acceptance and continuity

State the continuity rule **representation-independently**.

Identity is preserved only while the continuation remains able to answer for accountable facts it has already accepted. Storage migration, encoding changes, compaction, runtime replacement, deployment changes, actor changes, target changes, lifecycle changes, and similar representation/hosting changes do not themselves create a new identity when accountable continuity is preserved.

Encode the minimum semantics of acceptance that the review requires before durable records exist:

- acceptance is an authority act at a commit boundary;
- accepted accountable history is monotonic in the semantic sense relevant to answerability;
- correction, retraction, or supersession is represented by new accepted material rather than silently un-accepting historical material;
- non-accepted material is outside the accepted set;
- the accepted set/state/frontier must remain determinable enough for the continuity and divergence rules to be evaluated.

Do not over-specify the future concrete storage/arrival/commit representation.

### Divergence, lineage, and historical honesty

Use one coherent divergence rule for deliberate fork, stale restore, and accidental divergence:

- beginning from a selected earlier state or from a state that cannot establish the required accepted continuity creates a distinct continuation identity;
- an explicit declared fork is permitted even when continuity could otherwise have been maintained;
- fork lineage is mandatory and immutable;
- lineage records the parent continuation and a determinate/referenceable divergence boundary appropriate to the chosen semantic model, plus whether the parent continues when that distinction is meaningful;
- later convergence of state/content does not merge identities;
- already-written identity bindings are not retroactively relabelled after late divergence discovery;
- where continuity cannot be established, fail safe to a distinct continuation rather than pretending sameness.

Treat legitimate exclusive extension as a **normative obligation**, not a metaphysical truth-condition that would retroactively invalidate records written before an accidental split was discovered. A violation is recorded and remediated forward; it does not rewrite history.

### Corrected divergence-evidence obligation — do not regress to a storage shape

The successor correction deliberately replaces the original review's normative "every accepted record must carry" / "per-record divergence-evidence" language.

The ADR-level semantic obligation is:

> Every accepted fact must remain durably relatable to the accepted continuation state or frontier it extended, with sufficient surviving evidence to establish common ancestry and incompatible extension later, including the divergence boundary/material needed by the chosen semantic model.

The representation **may** be per-record, per-batch, per-segment, per-checkpoint, or an authoritative correlated lineage/frontier structure. ADR-0013 must constrain what remains provable, not where a field is physically stored.

Do not copy external-system storage layouts into the Arcogine semantic contract.

### Raw observations and fact ownership

Preserve the raw-observation boundary:

- externally sourced observations do not need an Arcogine operational-continuation identity at ingestion;
- they retain their own source/subject/time/trust/quality provenance;
- later Arcogine interpretation/reconciliation may create identity-bearing relationships to them.

Keep the request/result/reality distinctions intact. Arcogine-owned request/submission/interpretation/reconciliation facts may belong to the accountable continuation; externally produced acknowledgement/accept-reject facts, actual physical transition, and raw telemetry are independently provenanced facts related to those Arcogine records rather than silently reclassified as Arcogine conduct.

### Non-reuse, closure, and authority

Require identity non-reuse and historical binding immutability.

Do **not** claim that an accountable continuation can never close, retire, or become non-extendable. Closure/retirement semantics remain deferred until a concrete consumer needs them. Non-reuse does not imply endless lifecycle.

Possession of the identifier confers no authority. Evidence that a continuation has the required accepted history is continuity evidence; it is not by itself authorization or entitlement to act. Actor/trust/authority remains a separate semantic boundary.

### Corrected one-level / two-level position

Do not present ADR-0008 as authority that Operational identity must permanently be one-level.

For the current decision, define only the **accountable-continuation identity** because current evidence does not require a second grouping/account identity. Treat ADR-0008 as a useful lineage precedent only.

Record the extension boundary explicitly: a future stable higher-level grouping/account identity remains permissible if a real consumer later proves a distinct equality, lifecycle, lookup, policy, or aggregation contract that accountable-continuation identity cannot satisfy.

Candidate two-level identity was not falsified; it is simply not justified now.

### What to defer

Do not prematurely settle:

- the final production type name;
- concrete identifier representation/encoding;
- textual canonicalization/parsing;
- storage schema/layout;
- per-record versus correlated divergence-evidence representation;
- registry/discovery service;
- uniqueness-enforcement mechanism;
- distributed coordination, quorum, fencing, lease, or consensus mechanism;
- concrete multi-writer acceptance protocol;
- closure/retirement mechanics;
- module ownership of an implementation type;
- a second grouping/account identity;
- Operational implementation sequencing.

The ADR should make clear which semantics are Accepted now and which implementation/consumer decisions remain deliberately deferred.

## 5. ADR durable naming

The current file path is:

```text
docs/architecture/decisions/0013-execution-context-identity.md
```

but the withdrawn `ExecutionContext` concept is no longer the decision's durable semantic subject, while the title is already `Durable operational identity`.

Because ADR-0013 is still Proposed on the base, evaluate the durable-vocabulary rule in `docs/architecture/decisions/README.md` and **prefer renaming it to a semantic path such as**:

```text
docs/architecture/decisions/0013-durable-operational-identity.md
```

if live repository search confirms that this is the narrowest accurate durable name. Keep ADR number 0013 and update every repository link/reference in the same change. Do not add `Amendment:` metadata merely for this Proposed-ADR reconciliation; that metadata is for Accepted/Superseded editorial amendments.

Do not encode a future Java type name in the ADR filename merely to make the name more concrete.

## 6. Reconcile dependent maintained documentation

### Operational architecture

Update `docs/architecture/operational-execution-digital-twin.md` so it no longer says the durable Operational identity referent/equality/lifecycle rule is wholly open after ADR-0013 is Accepted.

At minimum inspect and reconcile:

- §2.2, which currently says the exact referent/lifecycle/equality/final name remain open;
- §2.3, preserving the no-inference rule;
- §10, whose current "No higher-rank execution identity above `RunId`..." wording predates the completed research and must not contradict the Accepted accountable-continuation decision.

Do not convert the whole Operational architecture document to Accepted merely because one contained identity question is resolved. Keep its status/authority truthful and clearly defer its other open boundaries.

### Operational readiness plan

Update `docs/planning/operational-execution-digital-twin-readiness.md` to remove ADR-0013's missing equality/lifecycle rule as a current blocker once the ADR is Accepted.

Do **not** admit an Operational implementation slice just because this critical-path semantic blocker is closed. The same plan currently identifies other separate boundaries — actor/capability ownership, external operation realization, authoritative subject correspondence, temporal reconciliation, slice-specific safety/failure semantics, prerequisites, and executable acceptance evidence. Preserve those admission gates.

Keep the status `NOT ADMITTED` unless fresh repository evidence independently proves all promotion criteria for a specific concrete slice. Do not create a speculative delivery coordinate in this reconciliation.

### Research state

Reconcile `docs/research/research-register.md` so the durable Operational Identity question becomes `CONCLUDED` in the same change that lands its durable consequence, with an accurate verdict/destination pointing to the Accepted ADR and reconciled architecture rather than duplicating all semantics in the register.

Keep the Agency question separate and truthful. This Operational reconciliation does not conclude Agency/Decision Boundary research.

Inspect `docs/research/investigations/operational-execution-digital-twin-boundaries.md` for stale lifecycle/status wording such as "durable operational identity on the critical path" or text that still treats the now-settled question as READY. Reconcile only the maintained state/provenance needed to make the brief truthful; do not turn the brief into a duplicate ADR.

## 7. Explicit non-goals

Do **not** in this change:

- add an Operational Java/Kotlin/TypeScript type;
- create an `:operational` module;
- define `ExecutionContextKind` or any equivalent global production/staging/simulation enum;
- implement persistence, replay, checkpoint, restore, fork, or split-brain detection;
- select UUID or another identifier representation;
- create a registry, alias service, discovery service, database, quorum, fencing, lease, or consensus mechanism;
- solve actor identity, authentication, authorization, trust, capability, or policy ownership;
- solve subject correspondence, external command realization, or temporal reconciliation beyond keeping their boundary distinct from operational-continuation identity;
- admit a first Operational implementation slice;
- reconcile the Agency research result beyond avoiding contradictions with it;
- rewrite, amend, or replace the handed-off research/review commits.

## 8. Knowledge-transfer audit and workspace disposition

This reconciliation must include an explicit knowledge-transfer audit, preferably in the PR description unless a specific item belongs in a maintained repository authority.

For the **Operational Identity** question, account for at least:

- accepted conclusions/invariants → ADR-0013 and the directly dependent Operational architecture;
- qualifications → the same durable destination, including representation-neutral divergence evidence, non-foreclosure of a future second identity level, open closure/retirement semantics, and separation of authority from continuity;
- remaining material unknowns → leave/register them only where they remain real research questions rather than implementation details;
- proving cases/failure modes that future readers need to understand the decision → retain the minimum useful cases in the ADR/architecture reasoning rather than copying the research report wholesale;
- explicitly discarded/overruled formulations → record that "per-record divergence evidence" as a semantic requirement and "ADR-0008 proves permanent one-level Operational identity" are superseded by the successor correction;
- synthesis seed → add or extend one only if `docs/development/researching.md`'s evidence-bearing, transferable, and loss-sensitive criteria are actually satisfied; do not create a seed merely to preserve interesting notes.

Name the covered workspace:

```text
docs/agency-and-operational-identity-research-reports
```

and state **NOT retirement-eligible after this Operational-only reconciliation**, because the same finite workspace still carries the Agency/Decision Boundary research packet and its successor correction, whose durable reconciliation remains outstanding.

Do not delete the workspace branch after this PR merges.

## 9. Remove temporary evidence/handoff files from the final PR tree

The reconciliation may continue to use the same workspace history, but the final PR net content must be durable reconciliation, not a research archive dump.

Before the PR is ready for independent review, remove temporary report/review/correction/handoff files from the branch's **final tree** unless a specific artifact has been deliberately promoted to durable authority under the research method.

At minimum account for the current branch-only temporary files:

```text
docs/research/agency-and-operational-identity-synthesis.md
docs/research/agency-decision-boundary-report.md
docs/research/operational-execution-digital-twin-identity-report.md
docs/research/operational-identity-adversarial-review.md
docs/research/agency-decision-boundary-adversarial-review.md
docs/research/investigations/operational-identity-adversarial-review-corrections.md
docs/research/investigations/agency-decision-boundary-adversarial-review-corrections.md
docs/research/adr-0013-reconciliation-handoff.md
```

Deleting these from the branch tip is **not** deleting their evidence identity. Exact historical `commit SHA + path` coordinates remain the evidence contract. Verify the handed-off report/review/correction commits remain reachable from the workspace history after cleanup.

Do not delete or force-rewrite the branch ref.

## 10. Validation

Run the narrowest applicable repository validation for a documentation/ADR reconciliation, including at least:

```bash
python3 .github/scripts/check-markdown-links.py .
python3 .github/scripts/check-delivery-labels.py
python3 .github/scripts/check-adr-immutability.py --ci
```

Also run the repository's normal docs/classification validation required by `AGENTS.md` / CI for the actual changed-file set. If the ADR file is renamed, verify all repository references to the old path are updated and no stale link remains.

Perform a final repository search for stale semantic claims, especially:

```text
0013-execution-context-identity
exact referent remains open
lifecycle/equality remain open
No higher-rank execution identity
per-record divergence evidence
every accepted record must carry
ExecutionContextKind
PRODUCTION / STAGING / SIMULATION
```

Not every occurrence is necessarily wrong: historical discussion may legitimately describe the rejected proposal, and temporary evidence history is immutable. Fix only current durable/maintained surfaces that would otherwise misstate the accepted decision.

## 11. PR and review lifecycle

Once the durable reconciliation and cleanup are coherent:

1. inspect the net diff against current live `main` and verify it contains only the intended durable reconciliation / maintained-state updates;
2. open a normal PR from `docs/agency-and-operational-identity-research-reports` to `main` if no equivalent PR already exists;
3. explain in the PR description:
   - exact research evidence coordinates used;
   - the successor correction coordinate;
   - why ADR-0013 can move from Proposed to Accepted;
   - what remains deliberately deferred;
   - why the Operational implementation plan remains or does not remain `NOT ADMITTED`;
   - the knowledge-transfer audit and `NOT retirement-eligible` workspace verdict;
   - validation run;
4. do not self-certify merge readiness; normal independent PR review belongs to `.github/agents/pr-reviewer.agent.md`;
5. monitor the PR under `AGENTS.md` after opening it;
6. because this branch carries handed-off research evidence, if it later falls behind `main`, use the repository's **history-preserving merge-style Update branch path**, never `pr-reconcile.mjs`, rebase, or force-push;
7. stop at `READY TO MERGE`; the repository owner merges manually.

## 12. Final report checklist

When the reconciliation session finishes, report:

- live `main` SHA used as reconciliation baseline;
- final workspace head SHA;
- ADR-0013 final path and status;
- concise list of durable semantic decisions reconciled;
- directly dependent architecture/planning/research-state files changed;
- confirmation that no production implementation/type/module was added;
- validation commands and outcomes;
- PR number/head if opened;
- confirmation that the exact source report, original adversarial review, and successor correction SHAs remain reachable;
- knowledge-transfer audit result;
- explicit statement that the shared workspace is **not retirement-eligible yet** because Agency reconciliation remains outstanding.

The governing principle for this slice is: **promote only the semantic contract the evidence actually supports; keep implementation representation and adjacent identity/authority questions deferred until real consumers prove them.**