# Architecture Decision Records

This directory records significant architectural and domain-design decisions
for Arcogine, along with the reasoning behind them. Current-state
documentation such as [`architecture/overview.md`](../overview.md) and
[`product/concepts.md`](../../product/concepts.md) explains how Arcogine works
today; ADRs explain *why* it ended up that way and preserve the history of
that reasoning as the system evolves.

ADRs operate under the [Product Charter](../../product/charter.md) and the
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
- **Superseded** — a later Accepted ADR replaced this decision. The original
  record is preserved as-is and links to the ADR that replaced it.

A Proposed ADR is a useful way to structure an open design discussion, but
its existence never implies acceptance — readers should be able to tell at a
glance which parts are established fact versus open question or proposal.

## Creating an ADR

1. Copy [`0000-template.md`](0000-template.md) to `NNNN-short-title.md`,
   where `NNNN` is the next unused four-digit number (check the existing
   files in this directory and concurrent ADR work before allocating it).
2. Fill in the sections that apply; don't pad a section with content just to
   fill it in.
3. Set `Status: Proposed` (or `Accepted` if the decision is already made and
   simply being recorded).
4. Open a PR as usual.

Numbers are never reused, even if an ADR is later rejected or superseded.

## Changing a decision

Accepted ADRs are immutable historical records. Once an ADR is Accepted, its
decision/body text is not edited in place, even to make the old decision read
more like the architecture that exists later. If the decision changes:

1. write a new ADR describing the new decision;
2. the replacement ADR must itself be `Status: Accepted` before it can
   supersede an existing Accepted ADR;
3. reference the old ADR from the new one (`Supersedes: ADR-NNNN`);
4. change only the old ADR's supersession metadata: set `Status: Superseded`
   and add `Superseded by: ADR-NNNN`.

A Proposed replacement does not supersede established architecture. Keep the
old ADR Accepted until the replacement decision is actually Accepted.

Typos or explanatory improvements discovered after acceptance should normally
be corrected in current-state documentation, not by rewriting the accepted
ADR. If an error in the ADR itself is materially misleading, supersede it so
the historical record and the correction are both explicit.

### CI enforcement

The repository enforces this rule through
`.github/scripts/check-adr-immutability.py`, which runs in the always-required
`Classify changes` / `CI / gate` path.

For every ADR that was `Accepted` or `Superseded` on the PR base commit, CI:

- rejects deletion or rename of the ADR file;
- rejects any change to the ADR body/decision text;
- permits an `Accepted` ADR only to remain `Accepted` or transition to
  `Superseded`;
- permits that transition only when the same change adds a new **Accepted**
  ADR whose `Supersedes: ADR-NNNN` metadata points back to the old record;
- rejects a Proposed replacement as insufficient to supersede established
  architecture;
- keeps an already `Superseded` ADR fully immutable, including its
  supersession target.

`Proposed` ADRs remain editable until accepted. The guard is intentionally
stricter than human review: changing an accepted decision requires a new
Accepted ADR, not an exception flag or reviewer override.
