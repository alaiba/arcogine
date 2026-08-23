# Architecture Decision Records

This directory records significant architectural and domain-design decisions
for Arcogine, along with the reasoning behind them. Current-state
documentation (`architecture.md`, `concepts.md`, etc.) explains how Arcogine
works today; ADRs explain *why* it ended up that way and preserve the history
of that reasoning as the system evolves.

ADRs operate under [`PRODUCT_CHARTER.md`](..//docs/product/charter.md) and the
current architecture — an ADR is not itself product vision, and it must not
be written or read as though it sets Arcogine's product direction. Where a
decision is consequential enough to interact with the Charter's principles,
say so explicitly in the ADR (see the template's optional Charter-alignment
note).

See [ADR-0001](0001-use-architecture-decision-records.md) for the decision to
use this mechanism.

## When to write an ADR

Write one for decisions that meaningfully constrain future implementation,
for example:

- fundamental simulation semantics;
- determinism guarantees;
- event-model guarantees;
- major domain boundaries;
- resource/scheduling abstractions;
- persistence or replay semantics;
- public API compatibility policy;
- architectural technology choices.

Don't write one for routine refactoring, ordinary bug fixes, dependency
upgrades, local implementation details, or other easily reversible choices
without meaningful architectural consequences.

## Status

Each ADR has one of the following statuses:

- **Proposed** — the decision is still open. The ADR captures the problem,
  constraints, alternatives, and a proposed direction, but that direction is
  not yet established architecture and must not be presented as such.
- **Accepted** — the decision has been made and represents the intended
  design.
- **Rejected** — the proposal was explicitly considered and not adopted.
- **Superseded** — a later ADR replaced this decision. The original record is
  preserved as-is and links to the ADR that replaced it.

A Proposed ADR is a useful way to structure an open design discussion, but
its existence never implies acceptance — readers should be able to tell at a
glance which parts are established fact versus open question or proposal.

## Creating an ADR

1. Copy [`0000-template.md`](0000-template.md) to `NNNN-short-title.md`,
   where `NNNN` is the next unused four-digit number (check the existing
   files in this directory).
2. Fill in the sections that apply; don't pad a section with content just to
   fill it in.
3. Set `Status: Proposed` (or `Accepted` if the decision is already made and
   simply being recorded).
4. Open a PR as usual.

Numbers are never reused, even if an ADR is later rejected or superseded.

## Changing a decision

Accepted ADRs are not rewritten to make a past decision look different from
what was actually decided. Minor corrections or clarifications that don't
change the decision itself may be edited in place. If the decision itself
changes:

1. write a new ADR describing the new decision;
2. reference the old ADR from the new one (`Supersedes: ADR-NNNN`);
3. mark the old ADR's status `Superseded` and link forward to the new one
   (`Superseded by: ADR-NNNN`).
