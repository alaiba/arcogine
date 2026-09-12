# Research brief template

> Reusable planning structure for an Arcogine research question. This is a convenience template, not a second research authority: [`docs/development/researching.md`](../development/researching.md) controls the normative research method and [`docs/research/README.md`](README.md) controls lifecycle and portfolio state.
>
> Use only the sections that materially help bound the question. Lower-risk local questions may need little failure-oriented analysis. Medium- and high-risk questions should make the important failure/adversarial cases explicit before the question is marked `READY`.

---

## Question

State the exact bounded uncertainty to resolve.

A good question is narrow enough that an independent researcher can tell what evidence would change the answer and when to stop.

## Decision at stake

State the Arcogine decision that would change depending on the answer.

If no product, architecture, ADR, contract, or implementation responsibility could change, this is probably not a research question.

## Scope and non-goals

State what is included and what is deliberately excluded.

Use non-goals to prevent a material uncertainty from silently expanding into an adjacent survey.

## Risk classification

Classify the question using `docs/development/researching.md` §7:

- **Lower** — local and reversible; no persisted/public semantic contract.
- **Medium** — shared semantics, ownership boundaries, reusable abstractions, or likely architectural influence.
- **High** — hard-to-reverse identity, lineage, persistence, replay/history, determinism, security/authority, safety/consequence, interoperability, compatibility, major ownership, or foundational architecture.

State why the classification applies and what extra scrutiny it requires. Risk changes research depth; it does not predetermine the answer.

## Intended functions / invariants

Before choosing a preferred answer, state what any acceptable answer must preserve.

Examples of useful forms:

```text
A requested external operation must not be represented as an observed physical transition.
```

```text
A durable identity must survive process restart without being inferred from deployment location.
```

```text
A reusable analytics layer must not re-decide Engine scheduling semantics.
```

These are research constraints to test, not implementation designs to assume.

## Candidate models / hypotheses

Describe the alternative answers under test before evaluating them.

Include the current/simple/no-new-abstraction candidate wherever it plausibly survives. Do not write the brief as though one candidate has already won.

## Failure-oriented analysis

For medium/high-risk questions, and for lower-risk questions where failure behavior is decision-relevant, identify the smallest set of credible ways a candidate or assumption could fail.

This is **failure-oriented research planning**, not a formal DFMEA/PFMEA artifact. Do not add numeric severity/occurrence/detection scores, RPNs, or standards-compliance claims unless a later, explicitly scoped research question actually requires that methodology.

Use a compact table when it helps:

| Function / invariant | Failure or adversarial mode | Consequence if true | Why it discriminates | Research treatment |
|---|---|---|---|---|
| What must remain true | How a candidate/assumption could break | Semantic, safety, compatibility, ownership, or product consequence | Which candidates or assumptions this separates | `PROVING CASE`, `EVIDENCE GAP`, `BOUND / DEFER`, or `NOT MATERIAL` |

Use the treatment column carefully:

- `PROVING CASE` — turn the failure mode into a concrete scenario every candidate must face.
- `EVIDENCE GAP` — the failure mode cannot yet be resolved without additional repository/external evidence.
- `BOUND / DEFER` — deliberately outside this question; state why, the consequence of deferral, and the trigger that should reopen or create follow-up research.
- `NOT MATERIAL` — investigated enough to establish that it cannot change the decision; state the reason rather than silently dropping it.

Do not use this table to make implementation/governance decisions such as "risk accepted" or "control implemented." Research identifies what must be understood and what survives evidence; later authority owns implementation, authorization, and operational risk treatment.

## Proving cases

List the concrete scenarios that discriminate between the candidate models.

Each material `PROVING CASE` failure mode above should map to at least one proving case. A proving case should state:

- the setup / relevant facts;
- the distinction or failure mode being tested;
- what each candidate predicts or permits;
- which candidate(s) survive and fail if the expected evidence is observed;
- what evidence would make the result decision-quality.

Do not include a happy path merely to demonstrate that the favored candidate can represent it. Happy paths belong only when they genuinely discriminate between candidates or establish a required invariant.

## Evidence expectations

State what evidence categories are needed and why:

- repository facts and semantic neighbors to inspect;
- implementation/tests/reference surfaces where applicable;
- external evidence only where it can discriminate, falsify, expose a missing failure mode, or establish a consequence;
- source/provenance expectations for any load-bearing standards, specifications, papers, or product documentation.

For absence claims, define the search scope needed before treating "not found" as useful evidence.

## Falsification conditions

State what evidence would falsify each load-bearing hypothesis or materially change the preferred candidate.

Prefer conditions that can actually be checked over broad statements such as "if a better design is found."

## Adversarial-review plan

For medium risk, state the proportionate adversarial examination expected.

For high risk, state explicitly that genuinely independent adversarial review is required before the conclusion becomes decision-quality evidence for an ADR or comparably durable architecture.

Name the likely attack surface the adversarial reviewer should reconstruct independently before reading the recommendation in depth: omitted candidate, hidden assumption, failure case, ownership inversion, stale authority, misleading analogy, over-generalization, or conclusion stronger than the evidence.

## Exit criteria

State what must be true for the investigation to stop.

A useful exit condition normally requires:

- all live candidates evaluated against the material proving cases;
- material failure modes either resolved, converted into explicit remaining unknowns/follow-up triggers, or demonstrated not material to the decision;
- load-bearing evidence verified to the required level;
- surviving invariants stated without generalizing beyond the evidence;
- confidence and remaining unknowns explicit;
- the durable consequence identified without promoting it from inside research;
- any required independent adversarial review completed before high-risk promotion.

## Expected durable destination

State the smallest likely destination if the question settles:

```text
no action
product clarification
architecture / ADR
reference contract
concrete implementation responsibility -> docs/planning/
```

Research does not perform that promotion itself.

## Follow-up / reopening triggers

State what later evidence, consumer, implementation seam, operational observation, or changed assumption should reopen this question or create a narrower successor question.

Where a material failure mode is deliberately deferred, its trigger belongs here.

## READY check

Before marking the question `READY`, confirm that an independent researcher can start from this brief without having to re-derive the decision boundary.

The brief should make clear: the question, decision at stake, scope/non-goals, risk, candidate set, intended invariants, material failure/adversarial modes where applicable, proving cases, evidence expectations, falsification conditions, exit criteria, and expected durable destination.
