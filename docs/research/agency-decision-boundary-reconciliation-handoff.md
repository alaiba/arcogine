# Agency and Decision Boundary reconciliation — handoff prompt

> **Artifact type:** Temporary architecture-reconciliation handoff prompt
> **Workspace:** `docs/agency-and-operational-identity-research-reports`
> **Prompt baseline — live `main`:** `a75c819c7a3e90e452c13a3ba3315a9ce0b9fbde`
> **Workspace baseline before this file:** `55a8569933b8de57179b46f0a392664f7f1b699e`
> **Authority:** Delivery handoff only. Live repository authorities and exact research-evidence coordinates control over this prompt.
> **Cleanup:** This file is temporary and must be absent from the final reconciliation PR tree.

You are executing the **durable reconciliation of the Agency and Decision Boundary research into Arcogine's maintained architecture and research state** in the canonical repository `alaiba/arcogine`.

This is **not** a new research run, not a new adversarial review, and not an implementation slice. Decision-quality evidence already exists. Your job is to translate the surviving, corrected result into the narrowest durable repository authority, reconcile maintained research state, perform the required knowledge-transfer audit, and prepare the change for normal independent PR review.

The expected outcome is primarily **semantic rules and explicit non-introduction decisions**. Do not manufacture a platform subsystem, shared type, ADR, or implementation plan merely to make the reconciliation look substantial.

## 1. Repository and workspace rules

Read `AGENTS.md` first and follow it exactly.

Use the existing finite research-evidence workspace:

```text
docs/agency-and-operational-identity-research-reports
```

Do **not** create a fresh branch merely because the work has transitioned from research into durable reconciliation. `docs/development/researching.md` treats reconciliation as a separate phase/authority transition, not a separate branch by default.

This workspace carries handed-off evidence coordinates. Their exact commits must remain reachable. Never rebase, amend, force-push, or otherwise rewrite them.

Before editing:

1. resolve live `main` and record its exact SHA;
2. resolve the current workspace head separately;
3. verify the source report, independent adversarial review, and successor-correction commits listed below remain ancestors/reachable from the workspace;
4. if live `main` has advanced beyond the workspace, incorporate it with a **history-preserving merge-style update**; never use the ordinary rebase/reconcile path for this evidence workspace;
5. after any update, verify the previous workspace head remains an ancestor and the new live `main` is incorporated.

At prompt generation, live `main` was `a75c819c7a3e90e452c13a3ba3315a9ce0b9fbde`, which includes merged PR #314 accepting ADR-0013 Durable operational identity. The workspace was synchronized history-preservingly in `55a8569933b8de57179b46f0a392664f7f1b699e`, with the pre-sync reconciliation head and current `main` as parents. Re-check all of this rather than assuming it remains current.

At prompt generation there was no open pull request matching Agency / actor / attribution reconciliation. Re-check open PRs before editing.

## 2. Read these rules and current authorities first

Perform a quick repository search under `docs/` for at least:

```text
Agency
actor attribution
decision source
controller
subject
capability
operation realization transition
RevisionRecorder
AgentDecision
actor identity
platform Agent
provenance
replay
```

Then read, in this order.

### Operating and reconciliation rules

- `AGENTS.md`
- `.github/agents/work-planner.agent.md` for the handoff contract only; you are now the reconciliation/implementation owner, not running another planning pass
- `docs/development/researching.md`, especially lifecycle, promotion/reconciliation, evidence custody, knowledge-transfer audit, and workspace retirement
- `docs/development/reviewing.md`
- `docs/architecture/decisions/README.md`

### Maintained current state on live `main`

- `docs/research/research-register.md`
- `docs/research/investigations/agency-decision-boundary.md`
- `docs/architecture/overview.md`
- `docs/architecture/governance-conformance.md`
- `docs/architecture/operational-execution-digital-twin.md`
- `docs/architecture/decisions/0008-controlled-revision-identity-and-lineage.md`
- `docs/architecture/decisions/0010-semantic-operation-and-aggregate-command-boundary.md` or the current ADR-0010 path if renamed
- `docs/architecture/decisions/0011-runtime-observation-and-event-contract.md`
- `docs/architecture/decisions/0012-external-interchange-and-serialization-boundaries.md`
- `docs/architecture/decisions/0013-durable-operational-identity.md`
- `docs/architecture/decisions/0015-engine-semantics-identity-and-reproducibility.md`
- `docs/architecture/decisions/0016-governance-evidence-provenance.md`
- `docs/product/charter.md` where a reconciliation statement actually depends on a Charter principle

Keep statuses honest. In particular, ADR-0013 is now **Accepted** and explicitly keeps actor identity separate from accountable operational-continuation identity. Proposed architecture/ADRs remain proposed and must not be quoted as established authority merely because the research used them as candidates.

Inspect the current implementation evidence before promoting any repository-fact statement, including at least the current equivalents of:

- `SalesAgent` and `AgentObservation`;
- `EventPayload.AgentDecision` / event publication;
- `RuntimeEventEnvelope`;
- `RevisionRecorder`, `RevisionProvenance`, and their persistence/equality use;
- `ChangeProvenance`;
- Factory/Governance subject-reference types such as `AffectedEntityRef` and `ChangedEntityRef`;
- Challenge provenance/identity patterns and its current dependency boundary.

Do not assume the old reports' implementation inventory is still current.

## 3. Exact research-evidence coordinates

Read the complete artifacts from these exact historical coordinates. Do not reconstruct them from this prompt.

### Source report

- commit: `bf744af171d5a42a3c578d33836e8e45bd33b0fe`
- path: `docs/research/agency-decision-boundary-report.md`

### Independent adversarial review

- commit: `18018d3dbdee4fb9e5cb739585634563a5c3ec94`
- path: `docs/research/agency-decision-boundary-adversarial-review.md`
- disposition: `ACCEPT WITH QUALIFICATIONS`

### Successor correction note

- commit: `115fe4465bda04690d5bd27cca47a74438c24d0a`
- path: `docs/research/investigations/agency-decision-boundary-adversarial-review-corrections.md`
- disposition impact: the original `ACCEPT WITH QUALIFICATIONS` remains unchanged

### Coupled synthesis, context only

- commit: `bf744af171d5a42a3c578d33836e8e45bd33b0fe`
- path: `docs/research/agency-and-operational-identity-synthesis.md`

The successor correction controls only where it explicitly narrows the adversarial review. The original review remains immutable historical evidence. Do not edit either historical artifact to make the corrected wording appear original.

Operational Identity has already been durably reconciled and is `CONCLUDED`; ADR-0013 is Accepted. Use that landed result as a semantic neighbor, not as a shared actor-identity lifecycle/equality contract.

## 4. Current lifecycle and objective

At prompt generation, the maintained research register says the Agency/Decision Boundary question is **High / READY** even though its report, independent adversarial review, and successor correction are complete. The remaining work is durable reconciliation.

Reconcile the decision-quality result into the narrowest authoritative maintained surfaces so that:

- Arcogine has an explicit durable rule distinguishing attributable actor/responsibility from decision-production mechanism, subject, recording provenance, and external data provenance;
- the repository records that no platform-level `Agent` abstraction is justified by current evidence;
- the distinct-role and provenance rules survive without inventing shared value types;
- the current provenance fields are not silently reinterpreted;
- the current research question becomes `CONCLUDED` atomically with its durable consequence;
- material unresolved unknowns are transferred to the research register or a durable reopening/defer trigger instead of being buried in prose;
- no implementation work is admitted merely because the semantic reconciliation lands.

The default durable destination is **architecture prose plus an explicit no-new-abstraction/no-implementation result**, not a new ADR. If fresh repository grounding reveals a contradiction or a new hard-to-reverse commitment that cannot truthfully be captured this way, stop and re-evaluate the ADR threshold rather than smuggling an ADR-grade decision into prose.

## 5. Corrected semantic contract to preserve

Use the exact report/review/correction for full reasoning. The rules below are a closure checklist.

### No platform `Agent` abstraction under current evidence

Record this narrowly:

> **No platform-level `Agent` abstraction is currently justified.** Designs that collapse or hard-bind actor, controller/decision-source, and subject roles fail the proving cases, and no additional cross-case invariant has been found that warrants a shared platform `Agent` concept now.

Do **not** write that every possible generalized Agent concept is impossible or "unrepresentable." The successor correction deliberately narrows that claim from impossibility to current justification.

Keep `agent` as an application/domain label where useful; do not create a platform ontology, superclass, shared module, or durable identity category merely because `SalesAgent` exists.

### Roles are distinct even when identities coincide

Preserve these rules:

- actor/responsibility-bearing party, decision source/controller, and subject/body are distinct semantic **roles**;
- role is a property of a participation/relationship, not an intrinsic permanent kind of an identity;
- the roles must remain distinguishable, but their identities do not have to be different;
- one identity may occupy several roles in one case;
- one controller may serve multiple actors;
- one actor may change controllers without necessarily changing attributable identity.

A responsibility-bearing actor **concept** survives across humans, organizations, software/services, planners, NPCs, and external systems when a participation actually has an attributable party. This does **not** justify an `ActorId`, actor kind enum, shared actor reference, or universal actor presence. An external data source, for example, is not automatically an actor.

### Acting party and represented principal may differ

Where a service/party acts for another party, the acting party and represented principal may be different and both may need to remain recoverable.

Do **not** promote a universal rule that the delegator/principal always retains responsibility. Delegation, impersonation, transfer of responsibility, and domain policy are different questions. Preserve only the semantic ability to distinguish and recover the parties when the domain requires it.

### Keep provenance categories distinct

Preserve the distinction among at least:

- attributable actor/responsibility;
- recording provenance/mechanism;
- decision-production mechanism/source;
- external data/observation source.

Do not collapse them into one generic `source` or `actor` field merely because several current records use strings.

The correction to the adversarial review is critical here:

> `RevisionRecorder` as a whole is persisted **recording provenance** identifying what caused Arcogine to record a controlled revision. Its internal `source` / `subject` decomposition is **underspecified** by durable authority.

ADR-0008 says the recorder identifies the human, service, agent, import process, or other source that caused Arcogine to record the revision and may represent that recorder with `source` / `subject`; the implementation Javadoc says only "Identifies the source and subject that caused a revision to be recorded."

Therefore do **not** state or imply that:

- `RevisionRecorder.source` is canonically the recording mechanism/channel;
- `RevisionRecorder.subject` is canonically the actor/person/principal;
- either field can be migrated mechanically into a future actor type.

Likewise do not rename, reinterpret, or migrate `ChangeProvenance.source` or `AgentDecision` as part of this reconciliation. Existing persisted/equality-bearing Governance history must remain semantically stable. Any future attribution capability must be additive unless a separate compatibility decision explicitly governs migration.

Resolve the terminology collision around the word `subject` in prose before using it as shared Agency vocabulary: `RevisionRecorder.subject` does not establish Arcogine's universal meaning of "subject/body of an operation."

### Subject is distinct from actor, without a universal subject type

Record the semantic distinction only. Do **not** claim `AffectedEntityRef` or another current domain type is Arcogine's universal subject reference.

Current domain-owned subject/reference types may coexist because their equality, namespace, ownership, and lifecycle contracts differ. Shared appearance does not prove shared semantic identity.

### Decision-source internals remain private

Goals, beliefs, memory, plans, prompts, behavior-tree state, planner search state, model weights, hidden reasoning traces, and comparable decision-source internals do not become common Arcogine world semantics merely because a decision-maker uses them.

A hidden reasoning trace must not be promoted as authoritative causal provenance. A voluntarily recorded public rationale, declared policy identifier, commitment, or audit explanation may be a durable record of what a party **asserted**, but that is different from claiming it is the operative hidden cause.

### Decision-production provenance is consumer-specific and may be multi-source

Where materially relevant, Arcogine must be able to explain **which mechanism(s) were relied on** to produce a decision/request.

Do not require one universal `DecisionSourceRef`, one singular source, or one universal immutable-version field.

- An exact immutable source version is required only where the consumer's own reproducibility contract requires it.
- Never fabricate a version for an opaque external participant that cannot supply one.
- Stateful/online-learning sources remain an explicit unresolved case rather than being forced into an identity+version shape that does not describe them.
- Multi-source, recommender+human, ensemble, and policy-check provenance must remain expressible later.

### Decision and requested operation: preserve only the narrow rule

Do **not** state that every decision and the operation it requests are universally one durable fact.

The surviving rule is:

> An **attributed operation request** is the preferred first/common durable boundary when a decision results in a requested semantic change, and Arcogine does not currently need a universal shared `Decision` type.

A domain remains free to own a distinct durable recommendation, approval, denial, standing authorization, exception/risk acceptance, or other decision record when a real consumer requires it. Do not recast existing/future Governance approval or exception records as operation requests merely for vocabulary symmetry.

### Replay distinctions survive

Preserve the distinction:

```text
re-execute the decision source
!= replay the recorded decision
!= replay the requested operation
!= replay the resulting transition
```

For nondeterministic behavior, the general reproducibility move is to convert the relevant nondeterministic output/input boundary into durable recorded input where a consumer's contract requires replayability. Do not require hidden source internals to become replay state.

### Attribution and deterministic outcome: use the corrected two-part rule

Do not copy the source report's categorical "actor identity must never affect deterministic outcome" rule or cite ADR-0011's `RunId` rule as its precedent.

Use the adversarial review's two-part form:

1. **Incidental attribution metadata must never affect outcome.** Attribution attached for provenance must not leak implicitly into domain, dispatch, pricing, or simulation computation.
2. **Explicitly modelled authority may legitimately affect behavior.** Actor identity may affect behavior only through an authority determination that is explicitly modelled, whose outcome is recorded, and which is itself a reproducible input on replay — never through an implicit read of attribution by unrelated domain logic.

This does **not** settle the authorization model. It preserves the boundary between provenance and explicit policy input.

### Authority is a question shape, not a settled schema

The useful cross-cutting question is approximately:

```text
May this actor perform this operation on this subject under the applicable policy?
```

Treat that as a **question shape**. Do not convert it into a mandatory persisted/public input schema, and do not state the old external-analogy rule "authorization consults the current actor only" as Arcogine authority.

Generic actor/capability ownership remains open. Operational consequence may add identity verification, trust, credential, safety, and fail-safe requirements, but those do not make Operational the owner of all actor semantics.

### No shared capability/procedure type from this research

Do not introduce a shared temporally extended `Capability`, procedure, skill, or process type. Existing aggregate/child operation patterns are sufficient for the current proving cases. A future consumer may reopen this only if those patterns fail a concrete semantic need.

### No agent-communication ontology

Ordinary typed operations, events, observations, results, and explicit public commitments are sufficient under current evidence. Do not introduce a generic agent message bus, conversation ontology, or platform communication protocol without a later proving case.

### No Agency subsystem, module, or delivery track

Do not create an Agency module/subsystem/track. The research found cross-cutting semantic distinctions, not a coherent implementation owner requiring a new subsystem.

### W3C PROV and outside models remain references/projections

W3C PROV may inform vocabulary and projections; it is not Arcogine's canonical domain model. Do not import PROV's agent subclasses as an actor-kind taxonomy.

## 6. Actor identity remains a separate unresolved question

The research does **not** justify a shared actor value type today because actor referent/equality/lifecycle/namespace/federation semantics are unsettled and current consumers do not prove one common contract.

The successor correction explicitly decouples this from ADR-0013:

- actor identity answers **who/what is the attributable party**;
- ADR-0013's accountable-operational-continuation identity answers **which independently continuing body of Arcogine conduct/history a fact belongs to**.

They may correlate. They are not the same identity problem and must not inherit one another's equality/lifecycle rules by default.

During the reconciliation knowledge-transfer audit, decide whether the remaining actor-identity referent/equality/lifecycle/namespace/federation uncertainty is material enough to remain in the maintained research portfolio. If it is, admit it as a **separate `CANDIDATE` research question**, not as a child of ADR-0013 and not as implementation planning. Choose priority from current portfolio evidence rather than copying an old heuristic.

A useful bounded formulation, if admission is justified, is approximately:

> What durable actor identity, if any, must Arcogine share across concrete consumers, and what referent, equality, namespace, lifecycle, rename/merge/retirement, federation, and external-identity rules would make that identity truthful?

Do not mark it `READY` unless the current research method's bounding/evidence/exit criteria are actually satisfied.

Similarly, account explicitly for the stateful/online-learning decision-source provenance unknown. Admit a separate research question only if it can currently change a material Arcogine decision; otherwise preserve a concrete reopening trigger in the durable architecture rather than manufacturing backlog.

## 7. ADR threshold

The corrected evidence says:

> **Under current evidence and current consumers, a new Agency-specific ADR is not justified.**

That is the default reconciliation path. Architecture prose or an explicit no-action/non-introduction result is sufficient when the durable consequence is role/provenance discipline plus refusal to create unproven shared abstractions.

This is **not** a permanent exemption from ADR discipline. If your reconciliation would introduce any of the following, re-evaluate the ADR threshold before proceeding:

- persisted/public actor identity equality;
- a shared actor namespace or lifecycle contract;
- a permanent closed actor taxonomy;
- cross-module equality/interoperability semantics;
- a hard permanent prohibition that materially constrains future implementation beyond the evidence;
- another hard-to-reverse public/persisted semantic commitment.

Do not hide an ADR-grade decision inside `overview.md` merely to obey the old "no ADR" sentence.

ADR-0013 is now Accepted. Treat it as a semantic neighbor and do not edit it unless a genuinely separate semantic change is required. A semantic change to an Accepted ADR requires the repository's normal supersession policy; Agency reconciliation should not need one.

## 8. Reconcile the narrowest maintained documentation

Choose the smallest existing authoritative surfaces that can carry the rules truthfully.

### Cross-cutting architecture

Inspect `docs/architecture/overview.md` first. If its current scope can truthfully carry a compact cross-cutting attribution/decision-boundary section, prefer that over creating a new architecture document.

Durable cross-cutting prose should make clear that:

- today's `SalesAgent` implementation does not establish a platform Agent abstraction;
- actor/responsibility, controller/decision source, subject, recording provenance, and external data provenance are semantically distinct;
- shared roles do not imply shared value types;
- no shared actor identity/equality contract exists yet;
- decision-source internals are not common world semantics;
- attributed operation requests are a common boundary, not a universal Decision model;
- future attribution must be additive with respect to existing persisted provenance contracts.

Keep `overview.md` honest about current implementation versus future semantic constraint. Do not claim implementation that does not exist.

### Operational architecture

Reconcile `docs/architecture/operational-execution-digital-twin.md` only where its current actor/authority/capability or operation/realization prose would otherwise conflict with the Agency result.

Preserve that:

- generic actor/capability semantics are not intrinsically Operational;
- consequential external execution may require additional verification/trust/safety/fail-safe controls;
- requested operation, external realization/result, actual transition, observation, and reconciled interpretation remain distinct;
- Accepted ADR-0013's operational-continuation identity remains separate from actor identity.

Do not make the whole Proposed Operational architecture Accepted and do not admit implementation work.

### Governance architecture

Reconcile `docs/architecture/governance-conformance.md` only where current provenance/approval/authorization wording needs the shared distinctions.

Keep recording provenance, attribution/responsibility, evidence source, approval/decision records, and authorization semantically separable. Do not reinterpret historical `RevisionRecorder` fields.

ADR-0016 remains subject to its actual current status; do not promote Proposed provenance claims as Accepted simply because they are compatible with the Agency result.

### Research state

In the same durable reconciliation:

- update `docs/research/research-register.md` so the Agency/Decision Boundary question becomes `CONCLUDED`, with a concise verdict and durable destination rather than duplicating the architecture rules;
- update `docs/research/investigations/agency-decision-boundary.md` so it no longer presents the investigation as `READY`; retain it as provenance/brief history and point to the durable destination;
- inspect the broad Operational actor/trust/authority research entry and adjust only if needed to prevent scope duplication or the false implication that Operational owns generic actor semantics;
- add the separate actor-identity lifecycle/equality question as `CANDIDATE` only if the knowledge-transfer audit determines it remains a material unresolved decision;
- do not admit implementation planning or a delivery coordinate in this reconciliation.

Do not duplicate the full Agency conclusion into the research register. The register records lifecycle state, verdict, durable destination, and material reopening/follow-up question(s).

### Synthesis seeds

Only during the knowledge-transfer audit, inspect `docs/research/synthesis-seeds.md` for genuine cross-investigation transfer value. Do not manufacture a recurrence or seed merely because the research was large. If no qualifying signal survives independently, explicitly classify it as not retained in the PR's knowledge-transfer audit.

## 9. Shared actor-type trigger is semantic, not numeric

Do not preserve the adversarial review's old heuristic that a shared actor type appears after an exact consumer/module count.

The corrected trigger is:

> Introduce a shared actor identity/value type only when **multiple concrete consumers demonstrate the same equality, namespace, lifecycle, and interoperability contract strongly enough that domain-local representations would duplicate one semantic invariant**.

A committed implementation consumer is strong evidence; no magic consumer count or module count is architectural authority.

This reconciliation must not create that type because current evidence does not meet the trigger.

## 10. Explicit non-goals

Do **not** in this change:

- add or modify production code to implement Agency semantics;
- create `ActorId`, `ActorKind`, `ActorRef`, `Decision`, `DecisionSourceRef`, `SubjectRef`, `Capability`, delegation, role, or principal value types;
- add an actor-kind enum or taxonomy;
- migrate or reinterpret `RevisionRecorder.source`, `RevisionRecorder.subject`, `ChangeProvenance.source`, or `AgentDecision`;
- add actor attribution fields to runtime/event/governance records;
- create a platform `Agent` interface/base class/ontology;
- create an Agency module, subsystem, or delivery track;
- design authentication, identity-provider, credential, trust, authorization-policy, or permission technology;
- settle actor identity equality/lifecycle/federation;
- make Operational the owner of generic actor/capability semantics;
- create a universal subject reference or observation reference;
- create a universal Decision record or force domain-specific approvals/recommendations into operation-request shape;
- create a generic agent message bus or conversation protocol;
- introduce decision-source internals such as goals, beliefs, memory, prompts, model reasoning traces, or planner state into common world semantics;
- change ADR-0013's Accepted operational identity semantics;
- admit an Operational or Agency implementation slice.

If any of these becomes necessary to make the architecture prose truthful, stop and explain why the original reconciliation boundary is no longer sufficient rather than expanding silently.

## 11. Knowledge-transfer audit and retirement

This reconciliation is expected to close the second and final research question carried by this coupled workspace. Perform an explicit knowledge-transfer audit before declaring the workspace retirement-eligible.

Account for at least:

- **accepted conclusions/invariants** → exact durable architecture destination(s);
- **all adversarial qualifications** → show where each is preserved or why it does not belong in durable prose;
- **successor corrections** → explicitly preserve the corrected `RevisionRecorder` interpretation, current-justification wording for `Agent`, actor-identity decoupling from ADR-0013, conditional ADR threshold, and semantic rather than numeric shared-type trigger;
- **unresolved actor-identity equality/lifecycle/federation** → separate research-register candidate or explicit justified discard/defer trigger;
- **stateful/online-learning decision-source provenance** → research candidate or durable reopening trigger if material;
- **reusable proving cases** → preserve only those future readers need to avoid collapsing roles or provenance; do not archive the entire report into architecture;
- **external analogies/standards** → retain only load-bearing durable references, with their analogy limits; do not turn them into Arcogine ontology;
- **findings not retained** → explicitly classify/discard them in the PR audit rather than silently losing them;
- **synthesis signals** → seed only if they satisfy the current synthesis-seed criteria.

The Operational Identity question is already `CONCLUDED`. If this Agency question becomes `CONCLUDED` and the audit accounts for every material result carried by the workspace, the workspace should become **retirement-eligible after the reconciliation lands and independent PR review validates the transfer**.

Before merge, temporary research report/review/correction/handoff files that are not deliberately promoted must be absent from the branch's final tree. They are already absent from the post-Operational-reconciliation tip; this newly created handoff file must also be deleted before final merge. Historical evidence remains addressed by exact commit + path until workspace retirement.

After the reconciliation PR lands and retirement eligibility is confirmed, perform the repository's prescribed immediate post-merge cleanup for the finite research-evidence workspace. Also inspect any old isolated review refs carrying the same handed-off evidence and retire them if the research policy says their custody role is complete. Do not delete any evidence ref early merely to clean the branch list.

## 12. Validation

This should remain a documentation-only reconciliation unless fresh evidence proves the scope wrong.

Run the narrowest applicable repository validation from `AGENTS.md`. At minimum for a docs-only change, run the repository's always-required classify/document checks, including:

```text
bash .github/scripts/classify-changes.test.sh
```

Ensure the Markdown-link, durable-delivery-label, ADR-policy, and continuous-improvement checks executed by the classify path are green.

If an Accepted ADR is touched unexpectedly, run the applicable ADR immutability/supersession checks explicitly and justify why that edit is necessary. Prefer not to touch Accepted ADRs for this reconciliation.

Perform a final stale-claim sweep for at least:

```text
platform Agent
Agent is unrepresentable
ActorId
ActorKind
AffectedEntityRef universal subject
latestEventSequence observation reference
authorization consults the current actor
RevisionRecorder.source actor
RevisionRecorder.subject actor
decision and operation are one durable fact
Operational owns actor
actor identity ADR-0013
```

Surviving occurrences in historical/report coordinates are fine; durable current prose must reflect the corrected evidence.

## 13. PR and review lifecycle

Open the reconciliation PR from this same workspace branch to `main` once the durable change is coherent. The PR description must identify:

- live-main reconciliation baseline;
- exact source-report, adversarial-review, and successor-correction coordinates;
- adversarial disposition `ACCEPT WITH QUALIFICATIONS`;
- durable destinations and why no new type/module/ADR is introduced;
- which research register state changes are made;
- the full knowledge-transfer audit;
- workspace retirement eligibility after merge;
- validation performed.

Do not self-certify merge readiness. The PR requires normal independent review under `.github/agents/pr-reviewer.agent.md`.

The reviewer must verify, in particular, that:

- every material adversarial qualification survived;
- the successor corrections control the superseded review wording;
- architecture prose does not accidentally introduce a type/equality/lifecycle contract;
- no current provenance field was reinterpreted;
- no Proposed surface is presented as Accepted;
- no Operational ownership or implementation admission was smuggled in;
- the knowledge-transfer audit is sufficient to retire the workspace after merge.

If the PR branch falls behind `main`, update it **history-preservingly** using the repository's research-evidence workspace rule. Never rebase or force-push away evidence commits.

## 14. Final report checklist

When the reconciliation work is complete, report:

1. live-main baseline used;
2. final branch/PR head;
3. durable files changed and the semantic purpose of each;
4. exact evidence coordinates used;
5. how every adversarial qualification and successor correction was handled;
6. whether a separate actor-identity research candidate was admitted, and why;
7. any remaining material unknowns/reopening triggers;
8. validation commands/results;
9. PR number and independent-review state;
10. whether the workspace is retirement-eligible after merge, and what post-merge cleanup remains.

The target is a **small, durable semantic reconciliation**: preserve the distinctions the research actually proved, preserve its limitations just as carefully, conclude the research truthfully, and build nothing that current consumers have not justified.