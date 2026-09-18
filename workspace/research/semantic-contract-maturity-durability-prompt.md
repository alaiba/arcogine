# Research handoff: Semantic Contract Maturity and Durability

Operate as the Arcogine **Researcher** in standard-investigation mode for repository `alaiba/arcogine`.

Execute the maintained brief:

`docs/research/investigations/semantic-contract-maturity-durability.md`

The bounded question is:

> When should Arcogine promote a currently authoritative semantic contract into a durable immutable contract, what evidence must precede that promotion, and what compatibility/provenance obligations apply before versus after promotion?

At handoff creation time, `main` was `46389bc5d21e0c95ffa9366ce271f9d943d44b35`. Treat that only as historical context. Resolve live `main` at investigation start and record the exact research-baseline SHA.

## Required operating model

Read and follow, in order:

1. `AGENTS.md`
2. `.github/agents/researcher.agent.md`
3. `docs/development/researching.md`
4. `docs/research/research-register.md`
5. `docs/research/investigations/semantic-contract-maturity-durability.md`
6. `docs/research/report-template.md`

The register currently marks this question `Critical-path / READY` and the brief marks it **High risk**. Verify both on current `main`.

This run is research only. Do not change product/runtime code, Accepted ADRs, current architecture, or implementation planning. Do not promote the conclusion yourself.

Do not use `docs/research/synthesis-seeds.md` as routine initial grounding. Reach the investigation result independently first, per the research operating model.

## Initial repository search

After reading the required operating documents, perform a quick repository search across maintained docs, source, tests, and relevant history for the material concepts in the brief, including:

`semantic contract`, `durability`, `immutable`, `compatibility`, `provenance`, `factory-model:v1`, `factory-model:v2`, `engine-semantics:v1`, `ModelFingerprint`, `FactoryModelVersion`, `ControlledRevisionId`, `EngineSemanticsVersion`, `golden vectors`, `historical resolution`, `coexistence`, `outward compatibility`, `stabilization`.

Treat search results as discovery only. Read load-bearing repository evidence at the exact target revision.

## Investigation requirements

Follow the brief completely rather than replacing its structure.

In particular:

- preserve the distinction among artifact immutability, current/normative authority, semantic-contract durability, implementation release, external compatibility support, controlled historical identity, and semantic content identity;
- evaluate every serious candidate lifecycle model in the brief, including the current eager-durability model rather than assuming the motivating hypothesis wins;
- apply every candidate to every mandatory proving case in the brief;
- answer every question listed under **Questions the report must answer**;
- explicitly investigate whether each relevant contract has a real compatibility constituency beyond repository-controlled tests, fixtures, examples, and planned consumers;
- inspect current Factory, Engine, Governance, runtime-contract, outward-transport, and future Operational evidence required by the brief;
- inspect repository/history evidence sufficient to distinguish real compatibility obligations from internal proving artifacts;
- inspect at least one completed high-risk Arcogine prove -> independent adversarial review -> reconciliation precedent, using it as process evidence rather than semantic authority;
- use external evidence only when it materially discriminates among candidates, preferring primary or normative sources and stating where any analogy to Arcogine breaks;
- actively try to falsify both explicit-maturity and eager-durability models;
- keep Repository fact, External evidence, Inference, and Recommendation visibly distinct.

Do not decide Factory semantic composition here. The sibling Factory composition investigation may proceed independently on model shape, but any conclusion about permanent durability/version identity belongs to this investigation.

## Current-contract classification

The report must classify the current Factory and Engine version contracts under the surviving model without silently changing their present authority.

If the evidence indicates that `factory-model:v1`, V2 policy identity, or `engine-semantics:v1` were frozen prematurely, identify the smallest truthful correction, migration, or rebaselining path and the planning consequences, but do not execute them.

If the current eager-durability approach survives, explain what evidence justifies it rather than relying on the fact that it already exists.

## Report and completion criteria

Use `docs/research/report-template.md`.

The report must satisfy the brief's full exit criteria and must include:

- live-`main` research baseline;
- risk classification;
- executive conclusion and confidence;
- candidate comparison;
- candidate x proving-case evaluation;
- repository evidence;
- external evidence actually used;
- explicit self-challenge that is not mislabeled independent review;
- surviving invariants and failed candidates;
- limitations and unresolved unknowns;
- reopening/falsification triggers;
- smallest recommended durable destination, including `no change` if that is what survives;
- explicit statement that research itself performs no implementation or authority transition.

If live `main` moves materially before completion, re-ground before finalizing and state whether the change affects the result.

Stop gathering material when additional evidence no longer changes a candidate, proving case, invariant, confidence level, or durable consequence.

## Evidence custody

Continue using this finite workspace branch:

`research/semantic-contract-maturity-durability`

Persist the completed report at:

`workspace/research/investigations/semantic-contract-maturity-durability-report.md`

Use another semantic filename only if current repository state gives a concrete reason.

Commit and push the completed report. The exact commit SHA plus path is the report identity; branch tip is not sufficient.

Once that report revision is handed off, do not rewrite, amend, rebase away, or force-push away its commit. A changed report is a new evidence revision.

Because this is high risk, the report is not decision-quality evidence for durable architecture until a genuinely independent adversarial review examines the exact report revision. The report author's self-challenge does not satisfy that requirement.

Do not mark the research `CONCLUDED` merely because the report exists. Do not merge the workspace merely because research is complete.

If persistence cannot be completed, return `EVIDENCE PERSISTENCE BLOCKED` with the missing capability rather than presenting the handoff as complete.

## Final response

After persistence succeeds, return only a compact handoff containing:

- live-`main` research baseline SHA;
- 2-4 sentence conclusion;
- confidence;
- workspace branch;
- exact report commit SHA;
- report path;
- adversarial-review status;
- material inspection limitation, if any;
- next action: independent adversarial review of that exact report revision.

Do not paste the complete report into chat after it has been persisted.
