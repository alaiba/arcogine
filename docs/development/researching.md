# Research operating model

This document is Arcogine's single normative authority for the **research operating model**: what qualifies as research, research lifecycle and portfolio-priority semantics, promotion/reconciliation, investigation and adversarial-review method, evidence custody, synthesis-seed handling, historical decision-rationale retention, and maintenance of the research state registers.

Current maintained state is separate from these rules:

- [`docs/research/research-register.md`](../research/research-register.md) records current admitted research questions and their portfolio state;
- [`docs/research/synthesis-seeds.md`](../research/synthesis-seeds.md) records current non-authoritative synthesis-seed state.

For AI execution of this policy, the repository-owned **Researcher** procedure lives in [`.github/agents/researcher.agent.md`](../../.github/agents/researcher.agent.md). This document remains the normative research policy; the agent file defines how the specialized researcher executes it — the same split `docs/development/reviewing.md` and `.github/agents/pr-reviewer.agent.md` already use.

This document must not become a second research backlog, state ledger, architecture authority, or implementation plan. It defines the operating model, not current portfolio content: it does not list Arcogine's open questions (the research register does), does not decide any Arcogine semantic question (the architecture, specification, or product document that owns it does), and does not sequence implementation (`docs/planning/` does).

## Lifecycle, priority, promotion, and maintained state

Arcogine research exists to resolve material uncertainty before product, architecture, or executable delivery planning silently commits to an answer.

The research-question lifecycle is:

| Status | Meaning |
|---|---|
| **CANDIDATE** | Material uncertainty exists, but the investigation is not yet sufficiently bounded or timely to start |
| **READY** | Question, scope, evidence expectations, and exit criteria are sufficiently clear to start |
| **ACTIVE** | Evidence gathering or synthesis is in progress |
| **CONCLUDED** | A decision-quality result exists and durable consequences have been reconciled, or the result was explicitly no action |
| **SUPERSEDED** | Later evidence, question, or decision replaced the investigation before normal conclusion |

These lifecycle values apply only to research questions in the maintained research register. Synthesis seeds have no research lifecycle, priority, owner, delivery commitment, or percentage completion. Do not use percentage completion for research questions; track evidence, falsified hypotheses, and exit criteria instead.

Research priority is **portfolio guidance, not delivery commitment**. A `READY` research question means an investigation can start; it does not mean implementation is ready or admitted.

The promotion path is:

```text
Research
   |
   v
Decision-quality evidence
   |
   +--> no action
   +--> product clarification
   +--> architecture / specification
   +--> concrete implementation responsibility
                       |
                       v
                  docs/planning/
```

A topic is ready for implementation planning only when semantic/product meaning, ownership, prerequisites, and acceptance evidence are sufficiently settled. A blocked implementation contract may live in planning; an unresolved question that still determines the contract stays in research. Research may recommend a durable consequence, but the conclusion becomes authoritative only through reconciliation into the appropriate product, architecture, specification, reference, research, or admitted planning surface.

`docs/research/` therefore tracks what Arcogine still needs to understand or decide; `docs/planning/` tracks executable work given what is already known or decided. Research documents never receive temporary delivery coordinates.

A synthesis seed is outside the promotion path. It preserves only recurrence-detection value under the rules in §10; if recurrence later justifies a bounded Arcogine research question, that question must be deliberately admitted to the normal research register and lifecycle.

### Research-register maintenance

Maintain [`docs/research/research-register.md`](../research/research-register.md) as state, not as another policy surface:

- add a material unknown instead of hiding it in an implementation plan;
- mark research `READY` only when an independent researcher can execute it from the stated evidence and exit criteria;
- when research concludes, record the verdict and durable destination in the register without duplicating the authoritative conclusion;
- during planning or consistency review, move unresolved exploratory content back to research rather than allowing planning to settle it implicitly;
- terminal questions (`CONCLUDED` or `SUPERSEDED`) normally remain for lifecycle and provenance history; compact or remove a terminal row only when its continued presence no longer helps explain a material conclusion, supersession chain, reopening trigger, or active decision context, all durable consequences have already been reconciled, and removal is not being used as a substitute for the knowledge-transfer audit in §10;
- prefer current architecture/reference for durable semantics; the register records portfolio state and provenance, not a duplicate semantic authority.

## 1. What research is for

Arcogine research exists to answer a **material bounded uncertainty that can change an Arcogine decision** — a question whose answer would plausibly change product direction, architecture, a specification, or an implementation contract, and whose current uncertainty is real enough that guessing would be worse than investigating.

A question that cannot change any decision is not a research question; it is either already answered by current architecture/product docs, or it is genuine curiosity that does not belong in `docs/research/`.

A researcher must not turn an insufficiently bounded question into an encyclopedia. If a brief is too broad to falsify or too vague to exit, the correct action is to narrow the brief (or return it to `CANDIDATE` under the lifecycle above), not to produce a sprawling survey.

### What a bounded research brief normally identifies

A `READY` brief should normally state:

- the exact question;
- the decision at stake if the question is answered;
- scope, and explicit non-goals;
- a current/simple/no-new-abstraction candidate, where one plausibly exists;
- alternative hypotheses or candidate models to be tested against each other;
- proving cases the candidates must survive;
- evidence expectations (what would make the answer decision-quality);
- falsification conditions, where the question is the kind that can be falsified;
- exit criteria;
- the expected durable destination if the question is settled (no action, product, architecture/specification, process policy, planning, or implementation responsibility).

A brief missing one of these because it genuinely does not apply to that question is fine. A brief missing several of these is not yet `READY`: an independent researcher should be able to execute it from the brief's stated evidence and exit criteria without first re-deriving the question.

External-domain material belongs in a report only when it changes or tests an Arcogine-specific hypothesis, proving case, boundary, inference, confidence assessment, or decision. A summary of an outside field that does not do one of those things does not belong in the report, however interesting.

## 2. Repository grounding and baseline discipline

Every substantial research run begins by grounding itself in current repository authority, not in prior chat context, remembered conclusions, or a report someone described in conversation.

A researcher must:

1. resolve live `main` and record its exact SHA as the research baseline;
2. distinguish live `main` from any feature/research branch being inspected — a branch under investigation, or a branch carrying a prior draft report, is evidence to read, never landed repository truth;
3. read `AGENTS.md`;
4. read the relevant research brief and the surrounding entry in [`docs/research/research-register.md`](../research/research-register.md);
5. inspect the current product, architecture, specification, planning, implementation, and test surfaces the question actually touches;
6. search semantic neighbors rather than reading only the files named in the brief — a question about identity, ownership, or lifecycle usually has cousins elsewhere in `docs/architecture/`, `docs/planning/`, and the codebase that the brief's author did not anticipate;
7. state any important surface that could not be inspected, rather than silently omitting it.

Do **not** include `docs/research/synthesis-seeds.md` in routine start-of-run grounding for an investigation that is meant to provide independent evidence. First derive the bounded question's candidates, evidence needs, proving/failure cases, and result from its own brief and repository/external evidence. Compare against synthesis seeds only after reaching that result. If a seed is deliberately used as input to the question or materially shapes candidates, proving cases, or reasoning, label any later similarity as **reuse**, not independent recurrence.

For a long-running investigation, perform a final current-state recheck before presenting repository-dependent conclusions. If `main` moved materially during the investigation, reconcile whether the conclusion still holds against the new head rather than silently reporting against a stale one.

A historical research baseline is evidence about what was examined at that point; it does not redefine current Arcogine state. Conversation memory, prior prompts, previous agent statements, and even earlier research reports are useful context, but none of them is repository authority — only the live repository is.

## 3. Evidence categories

A decision-quality report keeps at least these categories visibly distinct, so a reader (and later reconciliation) can tell what kind of claim they are looking at:

- **Repository fact** — something the current (or explicitly labeled historical) repository state actually says: code, tests, current architecture or specification prose, an executable check.
- **External evidence** — a claim sourced from outside the repository: a standard, a paper, product documentation, an established engineering pattern.
- **Inference** — a conclusion the researcher draws by combining repository facts and/or external evidence; not itself directly observed in either.
- **Recommendation / proposed decision** — what the researcher thinks Arcogine should do about the question.

A recommendation must never be phrased as though it is a repository fact. An analogy to an external system must never be phrased as though it establishes Arcogine semantics by itself — see §6 on analogy limits. Where evidence is uncertain or was not actually available, the report must say so rather than silently upgrading it into fact.

Labeling every sentence is not required when the report's structure already makes the category obvious (for example, an entire "Repository evidence" section). Labeling is required wherever a reader could otherwise mistake one category for another — most importantly, wherever a recommendation or an analogy sits next to established fact.

## 4. Internal evidence

Internal repository evidence is mandatory for Arcogine architectural/domain research; its depth should scale with the question (see §7 on risk proportionality). Relevant internal evidence may include, as applicable:

- the Product Charter;
- current-state architecture (`docs/architecture/overview.md` and the relevant domain architecture doc);
- current architecture and specifications (`docs/architecture/`), kept explicitly distinct from unresolved proposals in research, planning, or branch discussion;
- the [research register](../research/research-register.md) and the relevant brief(s);
- admitted implementation planning under `docs/planning/`;
- implementation and tests;
- reference contracts (`docs/reference/`) and examples;
- recent merged PRs;
- open PRs, where they materially affect the question;
- review findings, where they exposed a relevant semantic failure mode.

Do not assume code always overrides architecture, or the reverse — use the repository's existing authority-by-subject model (the same table structure `docs/development/reviewing.md`, `.github/agents/work-planner.agent.md`, and `.github/agents/consistency.agent.md` each use: Charter for product direction, `docs/architecture/overview.md` for current behavior, the owning architecture or specification for why a constraint exists, `docs/planning/` for what is admitted, live `main` for what exists).

In particular:

- a proposal in research, planning, or branch discussion is not established architecture — treat it as a candidate, not as ground truth the report can lean on;
- **research priority is not implementation commitment** — a `High`-priority `CANDIDATE` question is not evidence that implementation is admitted;
- an **open branch is not landed capability** — a branch under investigation (including a prior research branch) is evidence to read, not architecture to cite as current.

## 5. External evidence

Do not specify a minimum number of references, pages, word count, or a mandatory literature review for every question. A citation quota produces padding, not decision quality. Instead, use evidence sufficiency and risk-proportionate depth.

External research is warranted when it can materially:

- falsify or discriminate between candidate models;
- reveal a known semantic distinction Arcogine might otherwise reinvent incorrectly;
- test an interoperability/standards claim;
- provide real implementation precedent;
- expose failure modes a purely internal analysis would miss;
- test whether a supposedly universal abstraction is actually system-specific;
- establish the consequences of an identity, lineage, authority, persistence, temporal, safety, or compatibility choice.

Internal evidence alone may be sufficient for a local, reversible repository question — do not manufacture external research a local question does not need.

For consequential cross-domain or hard-to-reverse semantic questions, actively seek relevant external evidence. For genuinely high-risk questions (see §7), seek triangulation across genuinely different evidence traditions where materially available — for example standards/specifications, foundational/original literature, mature production-system behavior, industrial/domain practice, real product/platform semantics, and deliberately contrary analogues — rather than collecting many versions of the same argument. Do not require every one of these traditions when most are irrelevant to the question at hand.

### Source quality

Prefer load-bearing evidence in this order where applicable:

1. issuing authority / normative standard;
2. official specification;
3. original paper or primary research;
4. official product/platform documentation;
5. strong secondary technical material, only where primary evidence is genuinely unavailable.

For standards and evolving specifications, capture provenance precisely: issuing body, designation, part, edition/version, year/date, and a useful locator/section where feasible. A family label (for example "ISA-95") is never sufficient provenance by itself — see `docs/architecture/standards-alignment.md` and `docs/architecture/isa-95-semantic-mapping.md` for the level of precision Arcogine already expects from standards claims.

For product/platform documentation, capture version/date context when semantics may change across versions.

Do not let an unverified memory of a source carry a load-bearing conclusion. If a source is known from background knowledge but could not actually be checked during the investigation, label it explicitly as unverified background and say so rather than presenting it as confirmed. A prior Arcogine investigation used exactly this discipline productively: sources actually fetched were marked "Verified in session," and sources recalled from background knowledge were marked "Background, verify before citing" — this pattern generalizes well and is recommended.

Absence claims need an explicit search scope. Failing to find a feature or a precedent during a bounded search is not, by itself, evidence that it does not exist; state what was searched and how, so the limitation is visible rather than silently promoted into a negative fact.

### Analogy limits

When an external system is used as an analogue, capture both what transfers and where the analogy breaks. State this explicitly rather than leaving it implicit — a report that only states what transfers invites the analogy to be read as stronger than it is.

An outside system may be strong evidence for one identity rule while being completely unsuitable evidence for ownership, persistence, or lifecycle in the same investigation. Industry precedent establishes *possibility* and *known tradeoffs*; it does not, by itself, establish *necessity* for Arcogine. A standards family name alone never proves a required Arcogine ontology.

## 6. Proving cases

A proving case is a concrete scenario used to discriminate between candidate semantics — not merely to demonstrate that the preferred model can represent a happy path. A proving-case set that only shows the favored model working is not evidence; it is illustration.

A strong proving-case set deliberately includes cases likely to separate concepts that are easy to conflate. Depending on the question, useful distinctions to probe include (this is a style example, not a universal checklist — every research question must derive its own proving cases from its own candidate models):

- identity from equality;
- source from actor;
- model from runtime;
- subject from controller;
- logical identity from physical asset;
- continuation from restart;
- continuation from divergence/fork;
- authority from capability;
- request from realization/result;
- raw observation from Arcogine-owned interpretation;
- shared appearance from shared semantic identity;
- consumer-local semantics from genuinely cross-consumer invariants.

Do not treat this list as a fixed ontology to check off. It exists to illustrate the *style* of discrimination that has previously produced decision-quality Arcogine research; the actual proving cases for a given question come from that question's own candidate models and failure modes.

## 7. Risk-proportionate depth

Research depth should scale with semantic consequence, not with document size. This mirrors the risk language `docs/development/reviewing.md` already uses for PR review depth.

- **Lower risk** — local terminology clarification; a repository-local, reversible process choice; a question whose answer does not establish a persisted or public semantic contract. Usually sufficient: repository grounding, a bounded set of alternatives, enough evidence to settle the local question. No mandatory independent adversarial pass.

- **Medium risk** — shared cross-module/domain semantics; ownership boundaries; reusable domain abstractions; a conclusion likely to influence architecture. Expect broader semantic-neighbor inspection, explicit alternative/falsification analysis, relevant external evidence where it can actually discriminate between candidates, and adversarial examination proportionate to the decision (which may be self-administered — see §9).

- **High risk** — hard-to-reverse questions involving identity/equality; lineage/continuity/fork; persistence; replay/history; determinism; security/authority; safety/consequence; public or persisted compatibility; interoperability semantics; major domain ownership; foundational architecture. Before such a conclusion is treated as decision-quality evidence for reconciliation into canonical architecture or a specification, a genuinely independent adversarial research review is required — see §9–§11.

Risk changes how much scrutiny a question receives. It must never predetermine the answer.

## 8. Stopping rule

Research stops gathering more material when additional evidence is no longer materially:

- changing the candidate set;
- falsifying or supporting a live hypothesis;
- exposing a new proving/failure case;
- sharpening a required invariant;
- changing confidence;
- changing the durable consequence.

Do not equate "comprehensive" with "collect every available source." A decision-quality report answers the bounded question; it is not an encyclopedia entry, and padding it with additional sources that do not move any of the above is a defect, not diligence.

## 9. What "adversarial" means

Do not use "adversarial" as a synonym for "be critical" or "think of some objections." Arcogine defines it operationally:

> An **adversarial review** is an independent attempt to falsify the report's load-bearing conclusions, performed without responsibility for defending the original recommendation.

An adversarial reviewer specifically attempts to discover, as applicable:

- an omitted viable candidate;
- a proving case the proposed semantics cannot survive;
- a hidden implementation/infrastructure assumption;
- an unstated equality, identity, continuity, lifecycle, ordering, or authority assumption;
- an ownership inversion;
- conflict with repository authority;
- stale baseline evidence;
- a source that does not actually support the claim attributed to it;
- a misleading analogy;
- evidence of *possibility* being treated as evidence of *necessity*;
- an abstraction generalized beyond its actual consumers;
- an unresolved question silently declared settled;
- a conclusion stronger than its evidence.

When a report proposes a potentially transferable result, challenge that scope explicitly as part of the same adversarial pass: seek Arcogine-specific causes, contrary contexts, design choices presented as necessities, and narrower claims that survive where the broader formulation does not.

A clean adversarial review — one that finds nothing that survives scrutiny — is a valid and useful result. Do not optimize for producing findings, and do not establish a minimum finding count. An adversarial reviewer who manufactures a finding to justify the pass has failed the same way a PR reviewer who "optimizes for finding something wrong" has failed (`docs/development/reviewing.md`).

### Report input integrity

An adversarial review is a review of a specific report revision, not of a conversational summary of that report. Before substantive report-specific work, the reviewer must verify that the complete original report is available and record its evidence coordinate: temporary research-evidence workspace branch, exact report commit SHA, report path, and the report's stated research-baseline SHA. This check may confirm file identity and completeness without deeply consuming the recommendation, preserving the anchoring-control sequence below.

If the complete report cannot be resolved, stop the report-specific review and return `INPUT BLOCKED — ORIGINAL REPORT NOT AVAILABLE`, naming the missing coordinate or artifact. This is not one of the four adversarial-review dispositions because the report was not actually reviewed. Do not infer the report from the review prompt, reconstruct its claims from a summary, or issue `ACCEPT`, `ACCEPT WITH QUALIFICATIONS`, `MORE EVIDENCE REQUIRED`, or `REOPEN` against an unavailable report. Independent reconstruction performed in that situation may be useful new research evidence, but it must be labeled as such rather than presented as a disposition on the missing report.

### Independence and anchoring control

For a **high-risk** adversarial review (§7), the reviewer must not simply be the same research run continuing to defend its own report.

Prefer, in order:

1. a different researcher, person, or model family, where practical;
2. otherwise, at minimum, a fresh isolated session/run with no responsibility for preserving the original conclusion.

Before deeply consuming the report's recommendation, an independent adversarial reviewer should, in order:

1. read the research question/brief;
2. re-ground in current repository authorities (per §2);
3. independently reconstruct the major constraints;
4. identify plausible candidate answers;
5. identify likely failure/adversarial cases;

and only then evaluate the report's own reasoning and recommendation. This sequencing reduces anchoring on the author's conceptual framing — a reviewer who reads the recommendation first tends to evaluate it on its own terms rather than discovering what an independent pass would have found.

Do not claim "independent adversarial review" if the independence condition was not actually achieved. If a report's self-challenge is the only adversarial pass performed — because independent review was unavailable, deferred, or rate-limited — say so plainly rather than presenting it with the weight of an independent review. A prior Arcogine investigation encountered exactly this limitation and disclosed it directly rather than overstating the pass; that disclosure is the expected standard of honesty, not an exceptional one.

### Adversarial-review outcomes

Use this compact disposition model:

- **ACCEPT** — the load-bearing conclusion survives the adversarial pass.
- **ACCEPT WITH QUALIFICATIONS** — the conclusion survives, but the identified qualifications must be part of any durable reconciliation.
- **MORE EVIDENCE REQUIRED** — no falsification was established, but the evidence is insufficient for decision-quality promotion.
- **REOPEN** — a load-bearing conclusion was falsified, or a viable omitted model materially changes the question.

Individual challenged claims may be tracked more granularly when useful, but do not build a heavyweight issue/finding ledger for research unless later evidence demonstrates an actual need for one — the Consistency agent's GitHub-Issues ledger exists because recurring drift detection needed cross-session continuity at scale; a single adversarial review does not automatically need the same machinery.

The review itself should state: what was challenged; what evidence was considered; the result; the effect on the report's conclusion; and any qualifications that must survive reconciliation.

## 10. Research and reconciliation stay separate

The full lifecycle is:

```text
bounded research question / explicitly coupled research packet
        |
        v
temporary research-evidence workspace
        |
        +--> checkpoints / drafts / diagnostic notes (optional, non-authoritative)
        |
        v
completed report revision handed off by exact commit + path
        |
        v
independent adversarial review (when required by risk, §7/§9)
        |
        v
completed review revision handed off by exact commit + path
        |
        v
decision-quality evidence
        |
        v
separate durable reconciliation on the same workspace by default
        |
        +--> no action
        +--> product
        +--> architecture / specification
        +--> concrete implementation responsibility
                            |
                            v
                       docs/planning/
        |
        v
knowledge-transfer audit
        |
        +--> reusable evidence / know-how
        +--> historical decision rationale (non-normative, only when retained)
        +--> qualifying synthesis seed (non-authoritative)
        +--> explicit discard
        |
        v
reconciliation lands
        |
        v
workspace branch retired
```

A researcher may recommend durable consequences. A researcher does not make architecture authoritative merely by publishing a report; the promotion rule above requires deliberate reconciliation into the appropriate durable authority.

The report's author must not silently rewrite current architecture, specifications, product semantics, production code, or implementation planning while "doing research." A separate reconciliation pass deliberately translates surviving research conclusions into the appropriate authoritative surface — an architecture or specification PR, a planning admission — and that reconciliation change goes through normal independent PR review exactly as any other change would.

**Separate reconciliation means a separate phase and authority transition, not a separate Git branch.** By default, the same finite research-evidence workspace continues through durable reconciliation. Do not create a fresh reconciliation branch solely because evidence gathering has ended. Completed report/review artifacts remain immutable by exact commit SHA + path while later workspace commits carry reconciliation edits and the knowledge-transfer audit.

Research is never `CONCLUDED` merely because a report was written. `CONCLUDED` requires the durable consequence to actually be reconciled, or an explicit, recorded no-action result.

### Evidence workspaces, artifact identity, and retirement

A research artifact that matters beyond the current session must not exist only inside an agent session, local scratch space, or pasted conversation output. Use one temporary, semantically named **research-evidence workspace branch** per bounded research question by default. An explicitly coupled set of questions may share one workspace when they are intentionally intended to be reviewed and reconciled as one packet. Do not create one branch per artifact merely because a report and its adversarial review are different artifacts, do not create a new branch merely because the same workspace has entered reconciliation, and do not use one permanent repository-wide branch for unrelated investigations.

The repository location for this temporary custody is the unignored `workspace/research/` subtree. Put reports, report revisions, adversarial reviews, checkpoints, diagnostic notes, and research handoff artifacts that need persistence under a semantic path there (normally `workspace/research/investigations/`). The durable research briefs, register, synthesis seeds, and reconciled conclusions remain in their existing `docs/research/` destinations. `workspace/research/` is not a permanent archive: it is expected to be absent from the final merge candidate and from `main`, and the tracked-workspace check enforces that boundary.

A workspace may be opened before the investigation is complete and may contain checkpoints, drafts, diagnostic notes, completed reports, adversarial-review artifacts, and later reconciliation work. Checkpoint or draft commits are continuity aids only; they do not become decision-quality evidence merely because they are persisted.

Before presenting a standard investigation as complete, the researcher must commit the completed report in its workspace and return the workspace branch, exact report commit SHA, report path, and research-baseline SHA. Before presenting an adversarial review as complete, the reviewer must commit the completed review in the same workspace when practical, or in the same explicitly coupled packet workspace, and return the workspace branch, exact review commit SHA, review path, reviewed-report commit SHA, and live-main review baseline. A separate review branch is unnecessary unless isolation is operationally required; branch separation does not establish reviewer independence.

The exact commit SHA + path is the artifact identity. Branch tip is never a substitute. Once a completed report revision has been handed off for review, do not amend, rewrite, force-push away, or otherwise mutate that revision. If the report changes, create a new commit; any report-specific adversarial disposition binds only to the exact revision it reviewed. The same rule applies to a completed review artifact once it has been handed off for reconciliation.

When a workspace carrying handed-off evidence needs to incorporate a newer `main`, preserve those exact evidence commits. Do not rebase, force-push, or otherwise rewrite handed-off artifact history merely to satisfy branch freshness. A stale reconciliation PR may use the repository's normal history-preserving base-normalization protocol; semantic conflict resolution and unsupported structural cases remain outside that protocol. After synchronization, verify that the workspace is current with its base and that every handed-off SHA remains reachable. If no safe history-preserving update is available, stop and return `EVIDENCE PERSISTENCE BLOCKED`. The invariant is immutable artifact identity, not linear branch history.

Workspace sharing must not weaken anchoring control. An independent reviewer may use the same workspace for persistence but should begin from the research brief and live repository authorities, then reconstruct constraints/candidates/failure cases before deeply reading the handed-off report, as §9 requires. Unrelated investigations should not share a workspace because that increases accidental anchoring, couples retirement, and creates pressure for a parallel long-running research archive.

The final reconciliation PR may use the same workspace branch. Before merge, temporary report/review/handoff files that are not deliberately promoted into durable repository authority should be absent from the branch's final tree, so the PR's net content is the durable reconciliation rather than an archive dump.

Exact workspace `commit SHA + path` coordinates are **active-custody artifact identity**. They must remain resolvable while the workspace is active so an adversarial review can bind to the exact report revision and a reconciliation reviewer can inspect the exact report/review pair rather than a conversational summary. That requirement ends when reconciliation has landed, the knowledge-transfer audit is complete, and the workspace is retirement-eligible.

A copied coordinate is not preservation of its target. Recording a workspace SHA in a pull request, issue, commit message, or maintained document preserves the coordinate text, but it does not create a repository guarantee that the underlying Git object will remain fetchable after squash merge, branch deletion, mirroring/export, or Git-host retention changes. Do not copy temporary artifact coordinates into maintained state merely to make them appear durable.

After retirement, maintained research state must depend on the **durable knowledge destinations** produced by reconciliation: accepted product, architecture/specification, or reference authority, admitted planning authority, remaining research-register questions and reopening triggers, durable proving cases/tests/source maps/know-how, any retained historical decision-rationale records, qualifying synthesis seeds, and explicit discard decisions. A merged reconciliation pull request or equivalent delivery-history record may be cited as a **delivery-history provenance reference** showing where the transfer and independent review occurred, but it is not an archive for the temporary report/review artifacts and current semantics must not require those artifacts to remain fetchable.

If the exact report, adversarial review, source map, or other research artifact must itself remain readable after workspace retirement, deliberately promote that artifact to a durable repository location or another retention surface with an explicit persistence contract before retiring the workspace. Otherwise the knowledge-transfer audit is the preservation mechanism and the temporary evidence is intentionally expendable.

Temporary research evidence may be deleted only after every research question carried by the workspace is `CONCLUDED` or `SUPERSEDED` **and** a knowledge-transfer audit accounts for every material result that should survive the investigation. At minimum, classify and transfer:

- accepted conclusions and invariants into the appropriate product, architecture, specification, reference, or admitted planning authority;
- qualifications that constrain an accepted conclusion into the same durable destination as that conclusion;
- unresolved unknowns, reopening triggers, and newly exposed questions into the [research register](../research/research-register.md) when they remain material;
- reusable proving cases, counterexamples, failure modes, measurements, protocols, source maps, or implementation know-how into the durable surface that will need them, when retaining them changes future reasoning or validation;
- decision rationale worth retaining independently of the current contract — why the chosen option beat serious alternatives, which trade-offs were knowingly accepted, and what would justify revisiting it — into a historical decision-rationale record when it meets the retention test below; the audit states either where that rationale is preserved or that no separate rationale record is needed;
- qualifying cross-investigation signals into `docs/research/synthesis-seeds.md` when they satisfy the synthesis-seed rules below;
- any exact report/review/source artifact whose future readability is itself material into a deliberate durable repository location or another retention surface with an explicit persistence contract;
- findings that no longer merit retention as explicitly discarded rather than accidentally lost with branch deletion.

### Historical decision rationale

A **historical decision-rationale record** is a concise, dated, non-normative account of why a significant reconciled choice was made: the serious alternatives, the trade-offs that actually decided it, and the changed conditions that would justify revisiting it. It lets that reasoning be retrieved deliberately after the temporary evidence that produced it retires, without making canonical documents carry alternative-analysis narrative and without restoring a decision-record authority. Research reconciliation is the normal producer; a significant architectural change reconciled without a research investigation applies the same rules. Records live under `docs/history/decisions/` (see [Historical evidence](../history/README.md)). Git and pull-request history remain delivery-history evidence of what changed and how it was reviewed; a record adds deliberate retrieval for selected high-value rationale and may cite the reconciliation pull request as its provenance.

**Authority.** A historical decision-rationale record cannot introduce, extend, override, or repair a current requirement. Any constraint, qualification, behavior, identity rule, or support obligation that still governs Arcogine must be present in its owning canonical document or executable contract, and current architecture and specifications must remain sufficient without reading any record. Therefore:

- a record is never implementation, review, or reconciliation authority — "the newest record says so" establishes nothing;
- editing a record changes no current meaning, and a record that differs from current architecture is historical evidence, not documentation drift by itself;
- a canonical document keeps any explanation whose removal would make a current rule ambiguous, unsafe to implement, or easy to misapply, while a record holds explanation whose primary purpose is why one option was selected over another, which trade-off was knowingly accepted, which assumptions were contingent at the time, and what evidence would justify choosing differently later.

**Retention test.** Retain a record only when losing the rationale would materially increase the chance that a future maintainer would:

- repeat substantial investigation;
- re-open a deliberately rejected alternative without knowing the decisive trade-off;
- mistake a contingent choice for a universal truth;
- accidentally undo a constraint whose motivation is not apparent from the current contract; or
- misunderstand why a deliberately narrower solution was chosen over a more general one.

A record is most clearly warranted when several of these hold: the decision followed substantial research or independent adversarial review; two or more serious alternatives remained viable; the selected option knowingly accepted a non-obvious trade-off; the decision is foundational or hard to reverse; future evidence can reasonably trigger reconsideration; the canonical result is much shorter than the reasoning needed to understand the choice. A record is usually not warranted for ordinary local implementation choices, straightforward refactors, rationale that is obvious and fully captured by current code, tests, and documents, temporary delivery sequencing, research material with no surviving decision relevance, interesting but non-decisive analysis, or content already adequately preserved as a durable proving case, synthesis seed, or retained artifact. "No rationale record" is a valid and normal audit outcome; a record is never a prerequisite for reconciliation, review, or merge.

**Boundaries with neighboring surfaces.** Keep each piece of reasoning in the one surface whose purpose it serves rather than repeating the same narrative across several:

| Surface | Holds | Does not hold |
|---|---|---|
| Canonical architecture or specification | Current requirements and semantics, with the concise rationale needed to interpret and apply them | A chronological decision log or alternative-analysis narrative |
| Historical decision-rationale record | Project-specific reasoning behind one significant reconciled choice | Current requirements, lifecycle state, or a copied report |
| Retained research artifact under `docs/research/investigations/` | Analysis or evidence whose exact or substantial content stays independently valuable beyond a decision summary | A substitute for the canonical result |
| Synthesis seed | A compact, potentially transferable signal for recognizing recurrence across investigations | An account of why Arcogine chose one option |
| Research register | Current portfolio state, verdicts, and reopening triggers | A rationale archive |

**Record content.** Name the file `YYYY-MM-DD-<semantic-slug>.md`, dated by when the choice was reconciled; there is no global sequence number, index file, or registry. Keep the record short — it is not an archived research report. Use this shape, omitting any section that does not apply:

```markdown
# <Decision title>

> **Date:** YYYY-MM-DD
>
> **Authority:** Historical, non-normative evidence; current meaning lives only in the documents it was reconciled into
>
> **Reconciled into:** <canonical document(s) that received the decision>

## Decision
## Context
## Serious alternatives
## Decisive rationale
## Consequences and accepted trade-offs
## Reconsider when
## Provenance
```

- **Decision** — a short historical statement of what was chosen at the time.
- **Context** — only the constraints that materially shaped the choice.
- **Serious alternatives** — the credible alternatives and concisely why each was not selected, saying plainly whether it was falsified or merely not justified by the evidence then available.
- **Decisive rationale** — the small set of facts or trade-offs that actually discriminated the choice.
- **Consequences and accepted trade-offs** — what the choice made easier or harder and what it deliberately left unresolved.
- **Reconsider when** — concrete evidence or changed conditions that would materially weaken the original rationale. This explains the record; it neither reopens research nor creates work. A trigger that should actively reopen an Arcogine question also belongs in the research register.
- **Provenance** — the merged reconciliation pull request or another delivery-history provenance reference, plus links to any deliberately retained evidence.

A record must be understandable without reconstructing the cited pull request's discussion. It must not carry a status or acceptance field, approval lifecycle, sequence number, copied report or source dump, restated current specification text, temporary delivery coordinates, or workspace `commit SHA + path` coordinates presented as preserved evidence; a research artifact that must itself stay readable is promoted under the artifact-promotion rule above instead.

**Later change.** When a later decision changes an earlier one, normally add a new dated record and cross-link the two rather than rewriting the earlier rationale to match the present. Small factual or link corrections are fine when they do not falsify the historical account. Retention is prospective: do not reconstruct records for past decisions wholesale; capture an older decision only when doing so has concrete current value.

### Synthesis-seed custody

A **synthesis seed** is a compact, non-authoritative record that preserves the ability to recognize a potentially generalizable signal across otherwise independent investigations. It is not an Arcogine research question, accepted product/architecture semantics, implementation commitment, publication candidate, or work item.

The purpose of the seed mechanism is to preserve enough evidence-bearing connective tissue that independently recurring knowledge can be recognized after temporary workspaces are retired. Recurrence can justify later synthesis; it does not itself establish that a result is general beyond Arcogine.

Admit or extend a seed only during reconciliation or another explicit knowledge-transfer review, after the originating investigation has reached its own result. The candidate must satisfy all three conditions:

1. **Evidence-bearing** — it arose from substantive evidence, discriminating proving cases, experiment, counterexample, implementation experience, or another actual investigation result rather than free-form speculation.
2. **Potentially transferable** — after Arcogine-specific class names and implementation details are removed, an intelligible proposition, distinction, failure mode, method, or counterexample remains.
3. **Loss-sensitive** — retiring the workspace without the compact signal would materially reduce the chance that a later independent investigation could recognize recurrence.

Prefer preserving reusable proving cases, counterexamples, measurements, protocols, tests, or other research assets in the durable Arcogine surface that will actually use them. A seed is connective tissue: it points to those assets and, when useful, to reconciliation delivery history rather than duplicating whole reports.

Do not create a seed merely because a finding is interesting, publication is imaginable, or a researcher wants to keep notes. If the item is an unresolved Arcogine decision, track it in the research register. If it has an accepted Arcogine semantic consequence, reconcile that consequence into its authoritative destination. If it is situational and not worth future recovery, discard it explicitly.

Each maintained seed record in [`docs/research/synthesis-seeds.md`](../research/synthesis-seeds.md) must contain:

- **Signal** — the smallest potentially transferable observation, distinction, counterexample, method, or hypothesis;
- **Origin** — the originating investigation, the durable reconciliation destination when one exists, and optionally a delivery-history provenance reference for where reconciliation occurred;
- **Evidence posture** — what is actually established, without upgrading an Arcogine-specific result into a broader claim;
- **Boundaries / counterevidence** — known conditions where the signal may not hold, contrary examples, or material untested scope;
- **Reusable assets** — durable proving cases, counterexamples, measurements, protocols, tests, source maps, or other research assets;
- **Occurrences** — later independent-occurrence or reuse entries with the materially similar aspect, durable destination where one exists, and an optional delivery-history provenance reference;
- **Revisit when** — a concrete evidence or recurrence condition that would justify considering a bounded synthesis investigation.

A **delivery-history provenance reference** identifies where reconciliation, review, or another relevant delivery event occurred — normally a merged pull request or equivalent record. It may preserve useful historical metadata, but it is not a storage guarantee for temporary workspace commits. Maintained research state must remain intelligible and useful if those temporary report/review commits are no longer fetchable; anything whose exact future readability matters must have been promoted deliberately before retirement.

When reconciling a new candidate, search existing seeds for semantic neighbors **only after** the originating investigation has reached its own result. Extend an existing seed when the underlying signal is genuinely the same. Record a later result as an **independent occurrence** only if the seed was not used as a load-bearing premise or framing input to that investigation; otherwise record **reuse**. Recurrence may justify considering a bounded synthesis question, but it does not establish generality by itself.

Do not use the synthesis-seed state as routine start-of-run grounding for an independent Arcogine investigation. First derive that investigation's candidates, evidence needs, proving/failure cases, and result from its bounded question and current repository authorities. A deliberate seed-informed investigation is permitted, but any later similarity is reuse rather than independent recurrence.

Every seed must state a concrete `Revisit when` condition such as independent recurrence in another domain, materially comparable external evidence, implementation experience that confirms or falsifies the signal, or reuse of the same proving method across distinct questions. When the condition fires, surface that fact for an explicit decision about whether a bounded cross-investigation synthesis question should be admitted to the normal research register. Do not automatically create research work, change priority, or promote the broader claim.

The reconciliation must name the workspace(s) covered by its transfer audit and state whether each is retirement-eligible. If any material item still lacks a durable destination, qualifying synthesis-seed record, or explicit discard decision, keep the workspace available. Once the reconciliation has landed and its independent PR review has validated the transfer, deleting a retirement-eligible workspace branch is immediate post-merge cleanup. The reconciliation owns that retirement decision even though deleting the Git ref is an operational action after merge rather than part of repository content.

This retirement rule does not create another lifecycle state, permanent report archive, research issue ledger, publication backlog, or second research roadmap. [`docs/research/research-register.md`](../research/research-register.md) is the maintained research portfolio state; [`docs/research/synthesis-seeds.md`](../research/synthesis-seeds.md) is only maintained low-authority recurrence state. Neither state file defines the operating rules above or can make semantic or delivery decisions by itself. Historical decision-rationale records are dated history, not maintained state, and cannot make such decisions either.

## 11. What this operating model intentionally does not decide

This document defines Arcogine's normative research operating model. It does not:

- list Arcogine's current open research questions — see [`docs/research/research-register.md`](../research/research-register.md);
- decide any Arcogine semantic question (agency, operational identity, resource semantics, or otherwise) — each one's state is exactly what the maintained register and the applicable architecture or specification record, and this document neither settles nor reopens any of them;
- create a Research delivery track, a second research roadmap, research delivery coordinates, a research sprint system, a permanent report archive, publication lifecycle/backlog, or new issue ledger;
- create a decision-record authority, decision numbering, or decision status/approval lifecycle.

Research documents remain research evidence only. Synthesis seeds remain non-authoritative recurrence signals only. Historical decision-rationale records remain non-normative history only. None of them becomes accepted architecture simply because it exists.
