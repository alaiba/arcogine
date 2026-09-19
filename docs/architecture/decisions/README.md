# Architecture decision records

This directory holds the small current set of durable architectural decisions whose
rationale must constrain future changes to Arcogine. Each record explains why a
constraint exists, so that a later contributor does not undo it by accident or repeat
the analysis that produced it.

It is not an archive. Git history preserves every decision that was replaced or
removed, together with the reasoning of the change that removed it. A removed
decision needs no tombstone, status transition, or supersession chain here.

Every record on `main` is a current, adopted decision. There is no status field:
proposed or unresolved architecture belongs in `docs/research/`, `docs/planning/`,
or the branch and pull request where it is being discussed, not in this directory.

## Admission

Record a decision here only when all four hold:

1. it intentionally constrains future architecture;
2. reversing it would be expensive, dangerous, hard to undo, or semantically
   consequential;
3. the rationale is not already obvious from current architecture, code, tests, or
   specifications;
4. understanding the decision will still matter after current implementation details
   change.

If any answer is materially "no", the subject does not belong here. Prefer, in order:
an executable invariant or test; the owning architecture or specification document;
development or process guidance; planning or research state; and only then a record
in this directory. Current-state behavior, field lists, byte grammars, algorithms,
delivery sequencing, and implementation decomposition belong in their owning
specification or code, not in a decision record.

## Writing and changing a record

Copy [`template.md`](template.md) to a semantic filename that names the decided
constraint (for example `deterministic-simulation.md`), not a sequence number,
initiative, stage, or delivery coordinate. Keep the prose self-contained: Context,
Decision, Consequences, and only the alternatives whose rejection still explains the
constraint. Do not require the reader to reconstruct pull requests, research
coordinates, planning state, or who made the decision.

A reviewed architectural change updates or removes the affected record and reconciles
every specification, plan, and consumer that depended on it, in the same change. The
reviewer checks the semantic consequences of the edit, not metadata about it.

Records operate under the [Product Charter](../../product/charter.md) and the
[Architecture Overview](../overview.md). A record is not product direction; where a
decision bears on a Charter principle, say so in the record.

## Current decisions

| Decision | Constraint |
|---|---|
| [Semantic identity and evolution](semantic-identity-and-evolution.md) | Semantic identity never rebinds; historical meaning and continuing support are distinct, contract-scoped obligations |
| [Deterministic simulation](deterministic-simulation.md) | Simulation outcomes are a function of explicit inputs and an identified Engine interpretation; authored facts and interpretation have different owners |
