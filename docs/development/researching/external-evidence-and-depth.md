# 5. External evidence

Part of the [research operating model](../researching.md); it is mandatory whenever the entry point routes your task here.


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

## Source quality

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

## Analogy limits

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
