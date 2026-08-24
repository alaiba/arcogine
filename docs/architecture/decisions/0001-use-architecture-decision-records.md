# ADR-0001: Use Architecture Decision Records

Status: Accepted
Date: 2026-08-22

## Context

Arcogine has substantial current-state documentation under `docs/`
(`architecture.md`, `concepts.md`, `standards-alignment.md`, etc.) describing
how the system works today. That documentation is not well suited to
preserving *why* a given design was chosen: it gets rewritten as the system
evolves, and older reasoning is overwritten or dropped rather than kept.

Git history records *what* changed and *when*, but commit messages and PR
descriptions are not a durable, discoverable place to find the rationale
behind a significant architectural or domain-design choice, especially once
the original discussion and implementation context are gone.

As Arcogine grows, and as coding agents make more of the changes, it becomes
more important that significant decisions remain understandable on their own
— including their alternatives and consequences — without requiring someone
to reconstruct the reasoning from scratch.

## Decision

Arcogine will use lightweight Markdown Architecture Decision Records (ADRs),
stored under [`docs/decisions/`](README.md) and versioned alongside the
source code, to record significant architectural and domain-design
decisions.

Each ADR is a single Markdown file following the template in
[`0000-template.md`](0000-template.md), with one of four statuses
(`Proposed`, `Accepted`, `Rejected`, `Superseded`). The process for creating,
numbering, and superseding ADRs is documented in
[`docs/decisions/README.md`](README.md).

## Alternatives considered

- **Rely on Git history and PR descriptions alone.** Already the status quo;
  doesn't provide a concise, discoverable summary of why a decision was
  made, and is easy to lose track of once a PR ages out of recent history.
- **Fold rationale into current-state docs (`architecture.md`, etc.).**
  These documents describe the present state and get edited as the system
  changes; embedding rationale in them either bloats them or gets lost when
  the described state changes.
- **Use an external ADR tool or issue tracker.** Adds a dependency and
  moves decision history outside the repository, out of sync with the code
  it concerns. Not justified given Arcogine's current scale.

## Consequences

- Significant decisions get a short, dedicated record capturing context,
  the decision, alternatives, and consequences.
- Superseded decisions remain visible instead of being silently overwritten.
- Current-state docs can link to the relevant ADR for rationale instead of
  duplicating it.
- Contributors and agents proposing a significant architectural change are
  expected to add or update an ADR as part of that change.
