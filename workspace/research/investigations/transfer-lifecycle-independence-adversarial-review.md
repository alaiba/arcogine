# Independent adversarial review — transfer lifecycle independence

## Header / evidence identity

- **Disposition:** **REOPEN**
- **Live-main review baseline:** `07bf95cb14cc446fe02a12c88c9526a7ff205647`
- **Reviewed report workspace:** `research/transfer-lifecycle-independence`
- **Reviewed report commit:** `f36f7f535d42245b38b3db0ca8ad0333963edf79`
- **Reviewed report path:** `workspace/research/investigations/transfer-lifecycle-independence-report.md`
- **Reviewed report stated baseline:** `07bf95cb14cc446fe02a12c88c9526a7ff205647`
- **Input integrity:** the exact report revision was resolved in full. At review time the workspace branch contained live `main` and the report commit was its head before this review artifact was added.
- **Authority:** this review is research evidence only. It does not itself reconcile Factory or Engine architecture.

### Independence statement

This review did not author the reviewed report and has no responsibility for preserving its recommendation. The reviewer did author the earlier bounded handoff prompt for this question, so this is not independence from the repository's question framing. Anchoring control was therefore applied deliberately: the review re-read the maintained brief and current repository authorities, reconstructed the constraints, candidate families and likely failure cases, and only then read the report recommendation in depth.

The independence condition is satisfied with respect to the report's conclusion and reasoning: this is a separate adversarial pass over a report produced by another research run, not the report author's self-challenge.

## Independent reconstruction before report-specific evaluation

The current repository establishes these constraints before choosing an absence rule:

1. **Factory absence is represented content, not a missing Java value to repair.** Under the Factory semantic-evolution contract, an absent optional record means the design makes no assertion in that semantic dimension, and nothing is synthesized from a default.
2. **Engine owns transfer lifecycle interpretation.** Factory owns authored facts; Engine owns result-affecting rules such as destination binding, reservation and transfer lifecycle.
3. **Publication validity and Engine applicability are separate.** A valid Factory artifact may be unsupported by a particular Engine definition and refused before mutation.
4. **An Engine interpretation may branch on exact policy and represented content.** Its support domain is defined by the Engine itself; recognition of absence is not synthesis of authored content.
5. **Changing result-affecting behavior may require a distinguishable Engine identity, but that is an evolution mechanism, not a semantic falsifier.**
6. **Historical V1 behavior cannot silently change under an attributed identity.** A future Engine identity may nevertheless give a valid artifact different semantics if support and attribution are explicit.
7. **No-transfer, zero-duration transfer and refusal are observably distinct.** They must not be collapsed or left ambient.
8. **The maintained brief explicitly permits three broad absence outcomes:** no transfer, zero-duration lifecycle, or refusal, and asks who owns the discriminator.

From those constraints, the independently plausible candidate families were:

- explicit no-transfer when no transfer-specific authored facts are present;
- distinct-resource lifecycle with an Engine-defined zero-duration absence rule, without pretending zero timing fields were authored;
- valid-model / Engine-inapplicable refusal when required transfer facts are absent;
- an explicit non-spatial transfer-applicability fact, separate from timing, if evidence justifies adding one to the Factory grammar.

Likely adversarial cases were: V1 versus V2-absent support, exact Engine-domain definition, zero-duration same-time ordering, reservation/command interleaving, whether policy identity may legitimately discriminate behavior, and whether an applicability fact adds information that authored spatial zero does not.

## What survives the adversarial pass

Several important parts of the report survive and should be retained in a reopened investigation:

### 1. No transfer and zero-duration transfer are different semantics

The report's W1–W3 are a sound Arcogine-specific discriminator:

- with no transfer, next-step processing can begin in the same task-end turn;
- with a zero-duration transfer, binding/reservation and a separately scheduled same-time completion turn create an observable intermediate state;
- command acceptance, bounded advancement, supported events and observations can therefore differ even when isolated completion time is equal.

That is a strong result. Any eventual reconciliation must state which represented-content cases receive which behavior.

### 2. Transfer lifecycle is Engine-owned

The report correctly preserves the Factory/Engine ownership split. A model may carry facts relevant to transfer, but the rule deciding binding, reservation, runtime state and events belongs to the identified Engine interpretation.

### 3. Same-resource continuation is a useful control case

Under the current Engine contract, same-resource continuation has no transfer lifecycle. It remains a valid control case and should not be used as evidence for the distinct-resource absence rule.

### 4. Current implementation inventory is materially accurate

Transfer runtime state, reservation and supported transfer events are still specification/planning behavior rather than landed runtime behavior. The current V1 runtime proceeds directly into next-step processing or waiting. The report does not falsely claim transfer execution is already implemented.

## Findings

### Finding 1 — load-bearing: Candidate 2 is not falsified by the anti-synthesis rule

The report says the zero-duration fallback candidate is forbidden because it would "default the missing transfer inputs to zero" and thereby synthesize absent content. That conclusion is stronger than the cited authority.

The Factory semantic-evolution contract says:

- absence means no authored assertion in that semantic dimension;
- authored facts are not synthesized;
- an Engine refuses an artifact **outside its own support domain** rather than ignoring represented content or supplying absent content.

The Determinism Contract says authored facts and Engine interpretation have different owners and that an Engine definition decides which exact Factory policies and represented content it supports.

Those rules do **not** prohibit an Engine definition from explicitly interpreting the represented state "transfer-timing record absent" as:

`distinct destination => transfer lifecycle; duration = 0`

provided that the Engine does not claim that `ticksPerCell=0`, `handlingTicks=0`, placement, or any other authored fact exists. That is an Engine rule over represented absence, not synthesis of authored Factory content.

The report itself recognizes this objection in A2, but then treats Candidate 2 as "dominated" because it may require a new Engine identity, may distinguish V1 from V2-absent by policy/support, and offers a uniform lifecycle. None of those is a repository falsification:

- a new Engine identity is the prescribed mechanism for intentional result-affecting change;
- Engine applicability is explicitly allowed to depend on exact Factory policy and represented content;
- the report's own W1–W3 show that a uniform lifecycle is not merely presentation — it changes binding, reservation, command acceptance, advancement and supported state.

**Effect:** Candidate 2 remains semantically viable. It may still be *unjustified* or *undesirable*, but the report has not established that it is invalid. The recommendation of Candidate 3a therefore cannot be promoted on the basis that Candidate 2 was falsified.

### Finding 2 — load-bearing: Candidate 3b/refusal is rejected by reasoning that contradicts the applicability boundary

The report rejects refusal on absence because a production-only V1 or V2-absent design would need to author floor, placement and zero handling facts to execute.

That is not a falsification of refusal. The Factory semantic-evolution contract explicitly states that publication validity does not establish Engine applicability and that a valid artifact outside an Engine's support domain is refused before mutation.

A future Engine may therefore legitimately say:

- V2 spatial-present is in scope;
- V2 spatial-absent is valid Factory content but not executable under this Engine definition.

That does **not** force the model to invent facts. It means the Engine does not support that represented-content case. Historical V1 execution may remain supported under the existing Engine identity independently.

The maintained transfer brief explicitly lists refusal as one of the meanings that must be evaluated for absent transfer facts. The report's invariant I-C — "refusal applies only to partial or incomplete facts" — is also structurally weak: partial V2 spatial content is Factory-invalid/draft and never becomes a published artifact for Engine applicability to refuse. Engine-level refusal is most meaningful for a **valid published artifact outside the Engine support domain**, exactly the case the report removes.

**Effect:** refusal remains a viable absence meaning unless a concrete support/product requirement establishes that V2-absent artifacts must execute. The report did not establish such a requirement.

### Finding 3 — material omitted refinement: explicit applicability separate from timing was dismissed too narrowly

The report added C4 as a boolean "hand-off modeled" fact, but then rejected it as redundant with authored zero spatial magnitudes and left flag-present / timing-absent behavior undefined.

That does not test the strongest form of the candidate.

An explicit non-spatial transfer-applicability fact can add information that a present-zero spatial record does not:

- it can state that a hand-off lifecycle exists **without authoring floor, placement or handling magnitudes**;
- timing can remain a separate concern;
- it directly exercises the brief's model-versus-Engine ownership question instead of using timing-data presence as the lifecycle switch.

A coherent variant could pair:

- an authored applicability fact;
- an Engine-defined zero-duration rule when no timing contract is present; or
- an applicability fact whose execution requires a separately present timing contract.

Whether such a Factory grammar addition is worth its cost is a separate question. Current evidence may well justify **not adding it**. But it is not redundant with "spatial present and both magnitudes zero", because that latter representation authors geometry and handling content that the non-spatial applicability fact deliberately would not.

**Effect:** the report's conclusion that no V2 grammar correction is needed is premature. The reopened investigation must either evaluate this candidate properly or state a concrete reason that no consumer/semantic requirement justifies adding such an applicability fact.

### Finding 4 — successor handoff overstates what is settled

Because Findings 1–3 leave multiple absence meanings viable, the report's successor handoff cannot yet promote these statements as settled:

- V1 and V2-absent necessarily have no transfer phase;
- refusal is excluded for absence;
- no V2 grammar correction is required for lifecycle reasons.

The successor Engine-applicability investigation still needs an upstream transfer result, but this report revision has not yet supplied one that survives adversarial review.

The facts that **can** survive into the reopened work are narrower:

- no-transfer, zero-duration transfer and refusal are distinct semantics;
- transfer lifecycle is Engine-owned;
- authored absence must remain distinguishable from authored zero;
- current V1 execution is no-transfer;
- changing that behavior under an attributed identity would require an appropriate semantic-evolution treatment.

## Why the disposition is REOPEN rather than MORE EVIDENCE REQUIRED

This is not only a case where the selected conclusion lacks one more supporting source.

Two alternatives that materially change the downstream Factory/Engine contract were rejected using rules that do not reject them:

1. an explicit Engine zero-duration interpretation for represented absence; and
2. valid-model / Engine-inapplicable refusal.

A third candidate family — explicit transfer applicability independent of timing — was tested in a weaker form and dismissed as redundant even though its semantic purpose is precisely to avoid authoring spatial/timing facts merely to express lifecycle existence.

Those are load-bearing defects in the candidate elimination and successor handoff. The report's strongest invariant survives, but its selected absence rule does not yet qualify as decision-quality evidence for reconciliation.

**Disposition: REOPEN.**

## Required next pass

Reopen the bounded transfer-lifecycle investigation rather than starting successor Engine-applicability research.

The next report revision should:

1. **Reframe Candidate 2 as an Engine interpretation of represented absence**, not as synthesized Factory zero fields. Evaluate whether such a rule is acceptable, useful and supportable; do not reject it merely because it would require a distinguishable Engine identity.
2. **Reframe refusal as an Engine support-domain choice** over a valid published V2-absent artifact. Establish whether any current product/support requirement requires that case to execute. If none does, say so explicitly rather than claiming refusal forces invented facts.
3. **Evaluate explicit non-spatial lifecycle applicability separately from timing.** If no current consumer justifies a Factory grammar addition, reject it as unjustified scope/cost, not as semantically redundant with a spatial zero record.
4. **Use a clear decision criterion among still-viable candidates.** A conservative/no-new-abstraction recommendation is legitimate if supported: e.g. preserve the current no-transfer outcome because no current consumer needs binding/reservation without transfer facts, it minimizes semantic/support surface, and changed conditions that would justify C2/C4 are recorded. But that is a deliberate trade-off, not a proof that the alternatives are forbidden.
5. **Retain W1–W3** as discriminating conformance/proving cases.
6. **Do not hand facts to the successor applicability question as settled** until the revised transfer result receives another independent adversarial review if the load-bearing recommendation materially changes.

## Final repository recheck

Immediately before this review was persisted, live `main` remained:

`07bf95cb14cc446fe02a12c88c9526a7ff205647`

The reviewed report's repository baseline therefore remained current for all material Factory/Engine/research authorities considered here.
