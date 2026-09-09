# Research method

This document is Arcogine's normative method for *conducting* a research investigation. It complements [`docs/research/README.md`](../research/README.md), which owns the research portfolio, lifecycle, and promotion boundary. That boundary does not change here:

```text
research -> decision-quality evidence -> no action / product clarification / architecture or ADR / concrete implementation responsibility -> docs/planning/
```

This document exists because the portfolio register defines *what* research is open and *when* a topic is done; it does not define *how* one investigation is actually executed, what evidence a report must establish before it counts as decision-quality, or what "independent" and "adversarial" mean when a high-risk conclusion needs them. Those questions recurred across real Arcogine investigations without a shared answer, which is the gap this document closes.

For AI execution of this policy, the repository-owned **Researcher** procedure lives in [`.github/agents/researcher.agent.md`](../../.github/agents/researcher.agent.md). This document remains the normative research policy; the agent file defines how the specialized researcher executes it — the same split `docs/development/reviewing.md` and `.github/agents/pr-reviewer.agent.md` already use.

This document must not become a second research backlog, roadmap, architecture authority, or implementation plan. It defines method, not content: it does not list Arcogine's open questions (`docs/research/README.md` does), does not decide any Arcogine semantic question (an ADR, architecture doc, or product doc does), and does not sequence implementation (`docs/planning/` does).

## 1. What research is for

Arcogine research exists to answer a **material bounded uncertainty that can change an Arcogine decision** — a question whose answer would plausibly change product direction, architecture, an ADR, or an implementation contract, and whose current uncertainty is real enough that guessing would be worse than investigating.

A question that cannot change any decision is not a research question; it is either already answered by current architecture/product docs, or it is genuine curiosity that does not belong in `docs/research/`.

A researcher must not turn an insufficiently bounded question into an encyclopedia. If a brief is too broad to falsify or too vague to exit, the correct action is to narrow the brief (or return it to `CANDIDATE` per `docs/research/README.md`), not to produce a sprawling survey.

### What a bounded research brief normally identifies

A `READY` brief — see `docs/research/README.md`'s lifecycle — should normally state:

- the exact question;
- the decision at stake if the question is answered;
- scope, and explicit non-goals;
- a current/simple/no-new-abstraction candidate, where one plausibly exists;
- alternative hypotheses or candidate models to be tested against each other;
- proving cases the candidates must survive;
- evidence expectations (what would make the answer decision-quality);
- falsification conditions, where the question is the kind that can be falsified;
- exit criteria;
- the expected durable destination if the question is settled (no action, product, architecture/ADR, or implementation responsibility).

A brief missing one of these because it genuinely does not apply to that question is fine. A brief missing several of these is not yet `READY`: an independent researcher should be able to execute it from the brief's stated evidence and exit criteria without first re-deriving the question.

External-domain material belongs in a report only when it changes or tests an Arcogine-specific hypothesis, proving case, boundary, inference, confidence assessment, or decision. A summary of an outside field that does not do one of those things does not belong in the report, however interesting.

## 2. Repository grounding and baseline discipline

Every substantial research run begins by grounding itself in current repository authority, not in prior chat context, remembered conclusions, or a report someone described in conversation.

A researcher must:

1. resolve live `main` and record its exact SHA as the research baseline;
2. distinguish live `main` from any feature/research branch being inspected — a branch under investigation, or a branch carrying a prior draft report, is evidence to read, never landed repository truth;
3. read `AGENTS.md`;
4. read the relevant research brief and the surrounding entry in `docs/research/README.md`;
5. inspect the current product, architecture, ADR, planning, implementation, and test surfaces the question actually touches;
6. search semantic neighbors rather than reading only the files named in the brief — a question about identity, ownership, or lifecycle usually has cousins elsewhere in `docs/architecture/`, `docs/planning/`, and the codebase that the brief's author did not anticipate;
7. state any important surface that could not be inspected, rather than silently omitting it.

For a long-running investigation, perform a final current-state recheck before presenting repository-dependent conclusions. If `main` moved materially during the investigation, reconcile whether the conclusion still holds against the new head rather than silently reporting against a stale one.

A historical research baseline is evidence about what was examined at that point; it does not redefine current Arcogine state. Conversation memory, prior prompts, previous agent statements, and even earlier research reports are useful context, but none of them is repository authority — only the live repository is.

## 3. Evidence categories

A decision-quality report keeps at least these categories visibly distinct, so a reader (and later reconciliation) can tell what kind of claim they are looking at:

- **Repository fact** — something the current (or explicitly labeled historical) repository state actually says: code, tests, an Accepted ADR, current architecture prose, an executable check.
- **External evidence** — a claim sourced from outside the repository: a standard, a paper, product documentation, an established engineering pattern.
- **Inference** — a conclusion the researcher draws by combining repository facts and/or external evidence; not itself directly observed in either.
- **Recommendation / proposed decision** — what the researcher thinks Arcogine should do about the question.

A recommendation must never be phrased as though it is a repository fact. An analogy to an external system must never be phrased as though it establishes Arcogine semantics by itself — see §6 on analogy limits. Where evidence is uncertain or was not actually available, the report must say so rather than silently upgrading it into fact.

Labeling every sentence is not required when the report's structure already makes the category obvious (for example, an entire "Repository evidence" section). Labeling is required wherever a reader could otherwise mistake one category for another — most importantly, wherever a recommendation or an analogy sits next to established fact.

## 4. Internal evidence

Internal repository evidence is mandatory for Arcogine architectural/domain research; its depth should scale with the question (see §7 on risk proportionality). Relevant internal evidence may include, as applicable:

- the Product Charter;
- current-state architecture (`docs/architecture/overview.md` and the relevant domain architecture doc);
- Accepted and Proposed ADRs, with their status kept explicitly distinct — see `docs/architecture/decisions/README.md`;
- the research register and the relevant brief(s);
- admitted implementation planning under `docs/planning/`;
- implementation and tests;
- reference contracts (`docs/reference/`) and examples;
- recent merged PRs;
- open PRs, where they materially affect the question;
- review findings, where they exposed a relevant semantic failure mode.

Do not assume code always overrides architecture, or the reverse — use the repository's existing authority-by-subject model (the same table structure `docs/development/reviewing.md`, `.github/agents/work-planner.agent.md`, and `.github/agents/consistency.agent.md` each use: Charter for product direction, `docs/architecture/overview.md` for current behavior, Accepted ADRs for why a constraint exists, `docs/planning/` for what is admitted, live `main` for what exists).

In particular:

- a **Proposed** ADR is not established architecture — treat it as a candidate under discussion, not as ground truth the report can lean on;
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

- **High risk** — hard-to-reverse questions involving identity/equality; lineage/continuity/fork; persistence; replay/history; determinism; security/authority; safety/consequence; public or persisted compatibility; interoperability semantics; major domain ownership; foundational architecture. Before such a conclusion is treated as decision-quality evidence for promotion into an ADR or comparably durable architecture, a genuinely independent adversarial research review is required — see §9–§11.

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

A clean adversarial review — one that finds nothing that survives scrutiny — is a valid and useful result. Do not optimize for producing findings, and do not establish a minimum finding count. An adversarial reviewer who manufactures a finding to justify the pass has failed the same way a PR reviewer who "optimizes for finding something wrong" has failed (`docs/development/reviewing.md`).

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
bounded research question
        |
        v
research execution
        |
        v
research report
        |
        v
independent adversarial review (when required by risk, §7/§9)
        |
        v
decision-quality evidence
        |
        v
separate durable reconciliation
        |
        +--> no action
        +--> product
        +--> architecture / ADR
        +--> concrete implementation responsibility
                            |
                            v
                       docs/planning/
```

A researcher may recommend durable consequences. A researcher does not make architecture authoritative merely by publishing a report — the same principle `docs/research/README.md` already states ("a research conclusion becomes durable only after it is reconciled into the appropriate product, architecture, ADR, reference, or implementation plan").

The report's author must not silently rewrite ADRs, current architecture, product semantics, production code, or implementation planning while "doing research." A separate reconciliation pass deliberately translates surviving research conclusions into the appropriate authoritative surface — an ADR PR, an architecture-doc PR, a planning admission — and that reconciliation change goes through normal independent PR review exactly as any other change would.

Research is never CONCLUDED (per `docs/research/README.md`'s lifecycle) merely because a report was written. `CONCLUDED` still requires the durable consequence to actually be reconciled, or an explicit, recorded no-action result.

## 11. What this document intentionally does not decide

This document defines method. It does not:

- list Arcogine's current open research questions — see `docs/research/README.md`;
- decide any live Arcogine semantic question (agency, operational identity, resource semantics, or otherwise) — those remain open exactly as the research register and any in-flight ADR record them;
- create a Research delivery track, a second research roadmap, research delivery coordinates, a research sprint system, or a new issue ledger;
- change how `docs/research/README.md`'s lifecycle (`CANDIDATE`, `READY`, `ACTIVE`, `CONCLUDED`, `SUPERSEDED`) or promotion boundary work.

Research documents remain research evidence only. They do not become accepted architecture simply because a report exists.
