# Independent adversarial review handoff: Semantic Contract Maturity and Durability

Operate as the Arcogine **Researcher** in **independent adversarial-review mode** for repository `alaiba/arcogine`.

This review is for exactly this completed research report revision:

- Workspace branch: `research/semantic-contract-maturity-durability`
- Reviewed report commit SHA: `d0b2c5f127d340ecb4816d6316f5028190c23a13`
- Reviewed report path: `workspace/research/investigations/semantic-contract-maturity-durability-report.md`
- Report-stated research baseline: `46389bc5d21e0c95ffa9366ce271f9d943d44b35`
- Known limitation stated by the report: external deployment/consumer inventory unavailable

Do not review branch tip as a substitute for the exact report revision. Your disposition must bind explicitly to:

`d0b2c5f127d340ecb4816d6316f5028190c23a13:workspace/research/investigations/semantic-contract-maturity-durability-report.md`

This review must be performed in a genuinely independent session/model family with no responsibility for defending the original report. If that independence condition is not actually satisfied, state that plainly and do not represent the result as an independent adversarial review.

## 1. Read the operating contract first

Read and follow, in order:

1. `AGENTS.md`
2. `.github/agents/researcher.agent.md`
3. `docs/development/researching.md`, especially the adversarial-review procedure in §9 and evidence-custody rules in §10
4. `docs/research/research-register.md`
5. `docs/research/investigations/semantic-contract-maturity-durability.md`
6. `docs/research/report-template.md` only where useful for checking report completeness and evidence discipline

Also perform the repository-required quick search across docs, implementation, tests, and relevant history for at least:

- `semantic contract`
- `durability`
- `durable`
- `immutable`
- `provisional`
- `compatibility`
- `provenance`
- `factory-model:v1`
- `factory-model:v2`
- `engine-semantics:v1`
- `ModelFingerprint`
- `ControlledRevisionId`
- `EngineSemanticsVersion`
- `golden vectors`
- `historical resolution`
- `outward compatibility`
- `release`
- `stabilization`
- `support`
- `retention`

Treat search results as discovery only. Read load-bearing evidence at the exact revision you rely on.

## 2. Confirm the exact review input before substantive work

Resolve the complete original report at the exact coordinates above.

Confirm:

- exact report commit SHA;
- report path;
- workspace branch;
- report-stated research baseline;
- that the report is complete and readable.

If the exact report cannot be resolved, stop with:

`INPUT BLOCKED — ORIGINAL REPORT NOT AVAILABLE`

Do not infer the report from a prompt, summary, later branch tip, later report revision, or chat transcript.

Confirming identity/completeness before deep recommendation reading is allowed and does not violate anchoring control.

## 3. Enforce brief-first anchoring control

Before deeply reading the report's recommendation:

1. resolve **live `main`** and record the exact review-baseline SHA;
2. read the maintained research brief;
3. re-ground in current repository authorities relevant to the question;
4. independently reconstruct:
   - the major semantic distinctions the question requires;
   - plausible lifecycle candidates, including eager durability, explicit maturity promotion, boundary-specific triggers, release-bound durability, and the simplest narrower alternative;
   - the strongest case for immediate/eager durability;
   - the strongest case for explicit later promotion;
   - the strongest case for boundary-specific promises;
   - the strongest case for release-bound promotion;
   - likely failure/adversarial cases;
   - evidence needed to distinguish provenance obligations from compatibility/support commitments;
5. write a compact pre-report reconstruction into the review artifact before evaluating the report's conclusion.

Do not use the report's recommendation, terminology refinements, matrices, or classifications as the framing source for this reconstruction.

The maintained brief is the authority for required candidates, proving cases, questions, falsification criteria, and exit criteria.

## 4. Re-ground the current repository authorities

At minimum inspect the current versions of the surfaces named by the brief, including:

- Product Charter;
- Architecture Overview;
- Factory Design;
- Factory Model V2;
- Engine Semantics v1;
- Governance Conformance;
- Operational Execution / Digital Twin architecture;
- ADR-0003;
- ADR-0004;
- ADR-0006;
- ADR-0011;
- ADR-0012;
- ADR-0014;
- ADR-0015;
- ADR-0016;
- relevant Factory/Engine/Governance implementation and tests;
- current planning that would deepen V1/V2 coexistence, semantics-version propagation, spatial activation, or outward-contract stabilization;
- current releases/tags/history/consumer evidence relevant to real compatibility constituencies;
- at least one completed high-risk Arcogine research -> independent adversarial review -> reconciliation precedent.

If live `main` differs materially from the report baseline, distinguish:

- what was true at the report baseline;
- what is true at the review baseline;
- whether drift weakens, strengthens, or invalidates a report conclusion.

Do not silently judge historical claims against changed current facts.

## 5. Then deeply read and attack the exact report

Only after the independent reconstruction above, deeply evaluate the exact report revision.

The report's load-bearing conclusion is:

> Candidate C: boundary-specific evidence-gated commitments with shared minimum rules.

It further argues that:

- current authority should not automatically imply perpetual compatibility;
- exact historical referents must remain non-rebinding immediately;
- a contract revision plus a specified support promise should be explicitly promoted after evidence appropriate to that promise;
- introduction and support promotion must be distinguishable but need not always occur in separate PRs/dates;
- existing V1/Engine meanings should not be retroactively relabeled provisional;
- future policy should improve promotion criteria rather than recycle identifiers or discard history.

Try to falsify these claims.

At minimum challenge the following.

### A. Candidate C versus Candidate A — eager durability

Test the strongest defense of eager durability.

Ask:

- Does exact provenance actually require stronger immediate semantic-contract preservation than the report allows?
- Are the costs of multiple durable versions materially demonstrated, or merely anticipated?
- Is the current Factory/Engine model simpler and safer because every named semantic contract is immediately immutable?
- Does boundary-specific support create ambiguity about what must remain interpretable?
- Would a mature system be easier to reason about if "named version" always implied a fixed preserved meaning?

Do not reject A merely because future evolution could become expensive.

### B. Candidate C versus Candidate B — explicit maturity promotion

The report rejects Candidate B's universal requirement for a separate later ceremony.

Challenge that rejection.

Ask:

- Does allowing introduction and support promotion in the same event undermine the practical distinction the research is trying to create?
- What evidence proves a contract can be mature enough at introduction?
- Would a mandatory separate promotion decision create a valuable forcing function even when the evidence already exists?
- Does avoiding ceremony risk reintroducing automatic durability under another name?
- Is the report underestimating governance benefits of a mechanically separate transition?

Conversely, do not favor B merely because it is procedurally neat.

### C. "Durability is a scoped promise"

The report sharpens durability into distinct promises: definition preservation, historical decoding, execution support, outward compatibility, physical retention, etc.

Challenge whether this distinction is:

- semantically necessary;
- operationally enforceable;
- understandable to consumers;
- sufficiently bounded to avoid hidden permanent obligations.

Look for cases where these promises cannot be cleanly separated.

For example:

- historical decoding may require retaining executable interpretation;
- provenance may require exact schema/codec support;
- outward compatibility may depend on internal semantic behavior;
- retention promises may imply support promises indirectly.

Determine whether "scoped promise" is a useful semantic decomposition or whether it fragments one compatibility contract into confusing partial guarantees.

### D. Immediate non-rebinding during proving

The report requires exact accepted historical referents to remain non-rebinding even before durability.

Challenge:

- what exactly qualifies as an "accepted historical occurrence";
- whether repository-controlled proving artifacts can be invalidated/rebaselined without violating truth;
- whether every persisted artifact needs retained definition resolution;
- whether implementation checkpoints and temporary fixtures accidentally become historical commitments under this rule;
- whether the distinction between discardable proving artifact and attributable historical artifact is mechanically clear.

The brief explicitly asks how provisional persisted artifacts may be invalidated, migrated, rebaselined, retained, or otherwise treated. Verify that the report gives a usable boundary rather than a principle only.

### E. Evidence gates

The report proposes boundary-specific evidence gates rather than one universal checklist.

Attack their sufficiency and minimality.

Ask whether the report actually defines enough to decide promotion for:

- Factory fingerprint policy;
- Engine interpretation;
- outward transport compatibility;
- Governance evidence/provenance;
- future Operational semantics.

Look for vague terms such as:

- "enough consumers";
- "real reliance";
- "failure cases";
- "mature enough";
- "appropriate evidence";
- "specified support promise".

Determine whether the proposed rule would let two reasonable reviewers reach opposite outcomes with no mechanical disagreement to resolve.

If so, identify the smallest missing criterion.

### F. Consumer evidence and independent-consumer requirement

The brief asks whether one originating consumer is sufficient and whether an independent consumer is required/preferred/irrelevant for different contract classes.

Challenge the report's answer directly.

Ask:

- Which contract classes require a second/independent consumer?
- Which can mature from one consumer plus strong conformance evidence?
- Can Governance/Operational semantics mature without an external consumer?
- Can a codec/fingerprint contract mature through language-independent vectors alone?
- Does consumer independence matter more for API shape than for identity/equality invariants?

The answer must be boundary-specific, not a generic "it depends."

### G. Current Factory v1 classification

The report preserves `factory-model:v1` commitments and rejects retroactive provisional labeling.

Challenge:

- what exact obligation already exists;
- whether permanent historical resolution means the codec must remain executable forever;
- whether golden vectors create a compatibility constituency;
- whether V1 can ever be retired from active support;
- whether the report conflates preserving meaning with preserving implementation;
- whether the repository history actually supports "permanent" in every dimension claimed.

Test the smallest truthful support/retirement model.

### H. Current Factory V2 classification

The report says V2 has not shipped a fingerprint policy and is amendable before shipment.

Verify:

- no released V2 codec/policy exists;
- no controlled revision has persisted under V2 identity;
- no consumer already relies on V2 meaning as a support commitment;
- the specification's own amendment rule truly permits correction;
- planning/test artifacts do not create stronger obligations.

Challenge whether "unreleased" is sufficient to treat the grammar as freely correctable, especially if current code/tests and sibling research already depend on it.

### I. Engine v1 classification

The report says `engine-semantics:v1` meaning is already attributed, while complete capability release remains under-proven.

Challenge that split.

Ask:

- Can a semantic version be "fixed in meaning" but not fully released?
- Which exact subset of behavior is already promised?
- Do conformance fixtures define the version more strongly than the report admits?
- Are missing spatial/propagation fixtures incompleteness in implementation only, or incompleteness in the semantic contract itself?
- Does the report risk creating a partial-version concept not supported by ADR-0015?
- What would an external consumer be entitled to infer from receiving `engine-semantics:v1` today?

### J. ADR acceptance versus durability

The report argues an Accepted ADR does not necessarily imply permanent compatibility for every represented concrete contract.

Challenge this against ADR-0016 and ADR-0013.

Ask:

- Which accepted semantic invariants are durable immediately because they define identity/equality/history?
- Which implementation/API shapes remain provisional?
- Can "Accepted logical invariant, provisional representation" be consistently maintained?
- Are there examples where Accepted architecture later changes without superseding a semantic identity?
- Does the report define a reusable rule, or merely classify each current ADR ad hoc?

### K. Outward compatibility precedent

The report uses ADR-0012's stabilization-before-outward-compatibility posture as supportive evidence.

Challenge the analogy.

Ask:

- Is transport/API compatibility fundamentally different from semantic identity?
- Can semantic identity be used internally before stabilization in a way external APIs cannot?
- Does exact fingerprinting require earlier immutability than HTTP/OpenAPI compatibility?
- Does the analogy establish possibility only, not necessity?

Do not let ADR-0012 carry more argumentative weight than it supports.

### L. Operational consequence

The brief requires testing stronger Operational audit/trust/continuity stakes.

Challenge whether the report actually changes promotion requirements for real-world consequence.

Ask:

- Does real external consequence require immediate durability at first accepted act?
- Can an Operational contract be provisional if an act has legal/safety/economic consequence?
- Which facts must be frozen before the act versus which support obligations may mature later?
- Is bounded retention enough for accountability if representations evolve?
- Does future Operational implementation need a stricter gate than Factory/Engine simulation contracts?

### M. Changing-contract proving case

The brief requires a realistic case where implementation and multiple clients reveal a missing field, wrong equality rule, or wrongly owned responsibility before external release.

Verify that the report actually demonstrates a truthful correction path.

Challenge:

- whether migration/rebaselining preserves historical facts;
- whether old artifacts must remain readable;
- when an identifier must change;
- how to distinguish a corrected pre-durable contract from a new durable version;
- whether controlled revisions can refer to artifacts whose defining provisional contract later changes.

If the report's rules are not operationally precise enough for this case, identify the gap.

### N. External deployment/consumer inventory limitation

The report explicitly states:

> No customer/deployment inventory, private consumers, external retained stores, usage telemetry or independent downstream package audit was available.

Treat this as a load-bearing limitation.

Do **not** convert "no constituency identified" into "no constituency exists."

Challenge:

- which conclusions depend on knowing whether unseen consumers exist;
- whether any recommendation could withdraw or narrow an existing commitment without that inventory;
- whether the missing inventory requires `MORE EVIDENCE REQUIRED` for some current-contract classification;
- whether the report properly limits itself to prospective policy while grandfathering existing commitments;
- what minimum additional evidence would be necessary before any support-removal or migration decision.

A missing external inventory does not automatically invalidate the prospective lifecycle rule. Distinguish the generic rule from claims about existing deployed obligations.

### O. Grandfathering and correction path

The report recommends prospective policy improvement while preserving existing V1/Engine meanings.

Challenge whether this is the smallest truthful correction.

Ask:

- Is grandfathering all current contracts too conservative?
- Could some current commitments be reclassified more narrowly without breaking truth?
- Does grandfathering preserve unnecessary machinery forever?
- Does selective correction create more ambiguity than it removes?
- Which exact clauses in ADR-0006/0014/0015 would require supersession versus clarification?

### P. Reopening/falsification criteria

Verify the report provides concrete enough triggers to reopen.

Try to produce a counterexample for each major claim.

In particular search for:

- a case where exact provenance forces immediate full durability;
- a client that cannot safely consume a provisional authoritative contract;
- a case where scoped promises cannot be separated;
- a case where the proposed evidence-gated lifecycle adds ceremony without changing a real decision;
- a contract whose maturity is correctly determined solely by release;
- a real dependency missed by the constituency search.

## 6. Verify every required brief question and proving case

Check every question under **Questions the report must answer** and every required proving case in the maintained brief.

Do not accept any report matrix or question map at face value. Follow the evidence.

Explicitly verify answers for:

1. meaning of normative/current before durability;
2. durability trigger;
3. whether introduction and promotion must be separate;
4. minimum implementation evidence;
5. minimum consumer evidence;
6. role of independent consumers;
7. conformance/fixture evidence;
8. required research/review evidence before freezing;
9. treatment of provisional persisted artifacts;
10. preserving history without unnecessary compatibility promises;
11. relationship among ADR status, maturity, release, outward support;
12. identifier naming before durability;
13. post-durability immutability;
14. whether current contracts were promoted prematurely;
15. correction path for current Factory/Engine contracts;
16. reopening/falsification evidence.

Required proving cases:

- Factory semantic fingerprint policy;
- Engine semantics;
- supported runtime versus outward transport;
- Governance evidence plus another accepted identity/provenance concept;
- future Operational execution;
- a contract changed during proving.

If a mandatory question is answered only by principle and not by an actionable boundary, call that out.

## 7. External evidence challenge

Re-verify any external source the report uses materially.

For each load-bearing external source:

- verify current/appropriate version and locator;
- state exactly what it establishes;
- state where the analogy to Arcogine breaks;
- distinguish evidence of possibility from evidence of necessity.

Specifically challenge any use of:

- standards with draft/provisional/stable states;
- Kubernetes lifecycle/stability rules;
- Rust stabilization;
- RFC process precedent.

Do not import an external lifecycle taxonomy into Arcogine without independent Arcogine evidence.

## 8. Omitted-model search

Actively search for a materially different candidate that the report did not adequately test.

Examples to consider:

- immediate immutable historical identity plus optional support-grade labels;
- per-boundary maturity with no explicit promotion event, only evidence-bound support declarations;
- support contracts attached to consumer/release channels rather than semantic definitions;
- durable core identity with provisional extensions;
- automatic durability for identity/equality invariants but evidence-gated durability for behavioral compatibility.

Do not invent an extra model merely to force `REOPEN`. It must materially change the answer.

## 9. Evidence discipline

In the review artifact keep these distinct:

- **Repository fact**
- **External evidence**
- **Inference**
- **Challenge**
- **Disposition / qualification**

For every material challenge record:

1. what was challenged;
2. evidence considered;
3. whether the claim survived, failed, or requires qualification;
4. effect on the report's overall conclusion.

Do not substitute reviewer preference for evidence.

## 10. Required disposition

Reach exactly one of:

- `ACCEPT`
- `ACCEPT WITH QUALIFICATIONS`
- `MORE EVIDENCE REQUIRED`
- `REOPEN`

Use the repository definitions in `docs/development/researching.md`.

If `ACCEPT WITH QUALIFICATIONS`, state the exact qualifications that **must survive durable reconciliation**.

If `MORE EVIDENCE REQUIRED`, state:

- the minimum discriminating evidence still required;
- whether the gap blocks only current-contract reclassification/support changes or also blocks the prospective lifecycle rule;
- which conclusions remain unpromotable.

If `REOPEN`, identify the falsified load-bearing conclusion or omitted viable model that materially changes the question.

The review must state explicitly:

- independence condition achieved or not;
- reviewed report commit SHA;
- reviewed report path;
- report research baseline;
- live-main review baseline;
- material repository drift, if any;
- what was challenged;
- evidence considered;
- disposition;
- effect on the report's conclusion;
- mandatory reconciliation qualifications, if any;
- impact of the missing external deployment/consumer inventory.

## 11. Persistence

Use the **same** finite research-evidence workspace:

`research/semantic-contract-maturity-durability`

Do not create a new review branch merely for phase separation.

Persist the completed review at:

`workspace/research/investigations/semantic-contract-maturity-durability-adversarial-review.md`

Use another semantic filename only if current repository state provides a concrete reason.

Commit and push the completed review.

The review artifact identity is:

`exact review commit SHA + review path`

The review must explicitly identify the reviewed report as:

`d0b2c5f127d340ecb4816d6316f5028190c23a13:workspace/research/investigations/semantic-contract-maturity-durability-report.md`

Do not amend, rewrite, rebase away, or force-push away the handed-off report commit or completed review commit.

If `main` advances and the workspace must be synchronized, preserve the exact handed-off report SHA through history-preserving synchronization. If that cannot be done safely, return:

`EVIDENCE PERSISTENCE BLOCKED`

Do not mark the research `CONCLUDED` merely because the review is complete. Durable reconciliation is a separate authority transition.

## 12. Final handoff

After persistence succeeds, return only a compact handoff containing:

- independence status;
- disposition;
- live-main review baseline SHA;
- workspace branch;
- exact review commit SHA;
- review path;
- reviewed report commit SHA;
- mandatory qualifications or evidence gaps;
- explicit treatment of the external deployment/consumer-inventory limitation;
- next required action:
  - durable reconciliation if `ACCEPT` or `ACCEPT WITH QUALIFICATIONS`;
  - evidence gathering/report revision if `MORE EVIDENCE REQUIRED`;
  - reopened research if `REOPEN`.

Do not paste the complete review into chat after it has been persisted.
