# Research brief template

> Reusable advisory planning structure for an Arcogine research question. This is a convenience template, not a second research authority: [`docs/development/researching.md`](../development/researching.md) alone controls the normative research operating model and `READY` criteria, while [`research-register.md`](research-register.md) records current question state.
>
> Copy this structure into a semantically named file under [`investigations/`](investigations/) when a new bounded investigation needs a dedicated brief. Use only the sections that materially help bound the question. The failure-oriented sections below are an optional planning aid: using, omitting, or adapting their suggested treatment vocabulary does not independently change whether a question is `READY`.

---

## Question

State the exact bounded uncertainty to resolve.

A good question is narrow enough that an independent researcher can tell what evidence would change the answer and when to stop.

## Decision at stake

State the Arcogine decision that would change depending on the answer.

If no product direction, architecture, specification, contract, process policy, or implementation responsibility could change, this is probably not a research question.

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

Before choosing a preferred answer, state what any acceptable answer must preserve when doing so helps sharpen the candidate set or proving cases.

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

Where it materially sharpens the investigation — especially for medium/high-risk questions or lower-risk questions whose decision turns on failure behavior — consider identifying the smallest useful set of credible ways a candidate or assumption could fail.

This is **failure-oriented research planning**, not a formal DFMEA/PFMEA artifact and not an additional lifecycle gate. Do not add numeric severity/occurrence/detection scores, RPNs, or standards-compliance claims unless a later, explicitly scoped research question actually requires that methodology.

A compact table can help:

| Function / invariant | Failure or adversarial mode | Consequence if true | Why it discriminates | Optional research treatment |
|---|---|---|---|---|
| What must remain true | How a candidate/assumption could break | Semantic, safety, compatibility, ownership, or product consequence | Which candidates or assumptions this separates | `PROVING CASE`, `EVIDENCE GAP`, `BOUND / DEFER`, or `NOT MATERIAL` |

One useful optional treatment vocabulary is:

- `PROVING CASE` — turn the failure mode into a concrete scenario that helps discriminate candidates.
- `EVIDENCE GAP` — note that additional repository/external evidence is needed before the mode can be resolved.
- `BOUND / DEFER` — deliberately keep the mode outside this question; record why and the trigger that should reopen or create follow-up research when that information is useful.
- `NOT MATERIAL` — record why the mode cannot change the decision when making that negative result explicit will prevent repeated analysis.

This vocabulary is a planning aid, not required research state. A researcher may use different structure when it better fits the bounded question, provided the normative method's actual readiness, evidence, proving-case, falsification, exit, and review requirements remain satisfied.

Do not use this table to make implementation/governance decisions such as "risk accepted" or "control implemented." Research identifies what must be understood and what survives evidence; later authority owns implementation, authorization, and operational risk treatment.

## Proving cases

List the concrete scenarios needed to discriminate between the candidate models under the normative research method.

When the optional failure-oriented analysis marks a mode `PROVING CASE`, map it to a concrete scenario so the label carries actual evidentiary value. A useful proving-case description states:

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

Use the risk-proportionate review requirements in `docs/development/researching.md`.

For medium risk, note the proportionate adversarial examination expected when it helps execution.

For high risk, the operating model requires genuinely independent adversarial review before the conclusion becomes decision-quality evidence for a durable architecture or specification change.

A brief may name likely attack surfaces the adversarial reviewer should reconstruct independently before reading the recommendation in depth: omitted candidate, hidden assumption, failure case, ownership inversion, stale authority, misleading analogy, over-generalization, or conclusion stronger than the evidence.

## Exit criteria

State what must be true for the investigation to stop, following the normative research method.

When the optional failure-oriented analysis is used, useful prompts for sharpening exit criteria include whether its material modes were resolved, converted into explicit remaining unknowns/follow-up triggers, or shown not to affect the bounded decision. These prompts do not replace or extend the operating model's actual exit and decision-quality requirements.

## Expected durable destination

State the smallest likely destination if the question settles:

```text
no action
product clarification
architecture / specification
process policy
reference contract
concrete implementation responsibility -> docs/planning/
```

Research does not perform that reconciliation itself.

## Follow-up / reopening triggers

State what later evidence, consumer, implementation seam, operational observation, or changed assumption should reopen this question or create a narrower successor question.

If the optional failure-oriented analysis deliberately defers a material mode, recording its trigger here can keep that follow-up discoverable without turning the treatment vocabulary into maintained lifecycle state.

## READY alignment check

Only [`docs/development/researching.md`](../development/researching.md) determines whether a question is `READY`. Before proposing that state in `research-register.md`, compare the brief against the operating model's bounded-brief criteria: the exact question; decision at stake; scope/non-goals; current/simple candidate where applicable; alternative candidates; proving cases; evidence expectations; falsification conditions where applicable; exit criteria; and expected durable destination.

The risk-classification, intended-invariant, failure-oriented, treatment-vocabulary, and adversarial-planning prompts in this template are aids for producing a better brief. They are not additional `READY` criteria unless the normative operating model is separately changed to make them so.
