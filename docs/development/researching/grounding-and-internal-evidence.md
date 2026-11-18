# 2. Repository grounding and baseline discipline

Part of the [research operating model](../researching.md); it is mandatory whenever the entry point routes your task here.


Every substantial research run begins by grounding itself in current repository authority, not in prior chat context, remembered conclusions, or a report someone described in conversation.

A researcher must:

1. resolve live `main` and record its exact SHA as the research baseline;
2. distinguish live `main` from any feature/research branch being inspected — a branch under investigation, or a branch carrying a prior draft report, is evidence to read, never landed repository truth;
3. read `AGENTS.md`;
4. read the relevant research brief and the surrounding entry in [`docs/research/research-register.md`](../../research/research-register.md);
5. inspect the current product, architecture, specification, implementation, and test surfaces the question actually touches;
6. search semantic neighbors rather than reading only the files named in the brief — a question about identity, ownership, or lifecycle usually has cousins elsewhere in `docs/architecture/`, the research register, and the codebase that the brief's author did not anticipate;
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
- current architecture and specifications (`docs/architecture/`), kept explicitly distinct from unresolved proposals in research, issues, or branch discussion;
- the [research register](../../research/research-register.md) and the relevant brief(s);
- known work in open GitHub issues;
- implementation and tests;
- relevant supported architecture contracts (including the [runtime contract](../../architecture/runtime-contract.md)) and consumer examples;
- recent merged PRs;
- open PRs, where they materially affect the question;
- review findings, where they exposed a relevant semantic failure mode.

Do not assume code always overrides architecture, or the reverse — use the repository's existing authority-by-subject model (the same table structure `docs/development/reviewing.md`, `docs/development/agent-roles/researcher.md`, and `docs/development/agent-roles/consistency.md` each use: Charter for product direction, `docs/architecture/overview.md` for current behavior, the owning architecture or specification for why a constraint exists, GitHub issues for known work, live `main` for what exists).

In particular:

- a proposal in research, an issue, or branch discussion is not established architecture — treat it as a candidate, not as ground truth the report can lean on;
- **research priority is not implementation commitment** — a `High`-priority `CANDIDATE` question is not evidence that implementation is admitted;
- an **open branch is not landed capability** — a branch under investigation (including a prior research branch) is evidence to read, not architecture to cite as current.

## Executable research substrate

Deterministic experiments over Factory/Engine behavior use the `product/research-experiments`
module, the standard place for them rather than a per-investigation harness. It runs fixtures —
an authored model, an explicit ordered command script, the evidence window the experiment means to
capture, and expected claims — in fresh runtimes and returns the supported evidence each run
produced. Its boundary is fixed:

- it is an embedded consumer of the [supported runtime contract](../../architecture/runtime-contract.md)
  only: published model facts, supported observations and supported events, never scheduler,
  handler or store internals;
- a derivation it computes is research-local. Reaching a value through the substrate does not
  make it an Engine fact, game-owned analytics, a Factory concept or a public API; promoting a
  measurement or an interpretation of one is a reconciliation decision, not an implementation
  consequence;
- it is never a production dependency, and executable checks enforce that direction.

Fixture expectations are derived by hand from the current specifications, never copied from a run.
An experiment whose sources stay in research custody can run against the module without being
copied into the tracked tree; [testing.md](../testing.md#10-research-experiment-fixtures) documents
the mechanics. A fixture carries no source revision; the revision a run executed on belongs to the
research custody that ran it (§2 and §10).
