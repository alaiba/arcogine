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
  semantic decision remains a historical record and links to the ADR that
  replaced it.

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

## Durable vocabulary

An ADR is a durable semantic record, not a retained implementation plan. Its
filename, title, and prose must name the capability, contract, invariant,
identity, or behavior being decided directly. Initiative-local stage, gate,
or slice identifiers belong in `docs/planning/`, issues, pull requests, and
other delivery history; they must not be required to find or understand an
ADR after the originating plan has been completed, condensed, or removed.

A planning document may link to an ADR and may say which delivery slice
implements it. The ADR must remain understandable if that planning document
no longer exists.

## Changing a decision

Accepted and Superseded ADRs are **semantically immutable** historical
records. A change to the decision, its applicability, its constraints, its
consequences, or the meaning of an accepted alternative requires a new ADR:

1. write a new ADR describing the changed decision;
2. the replacement ADR must itself be `Status: Accepted` before it can
   supersede an existing Accepted ADR;
3. reference the old ADR from the new one (`Supersedes: ADR-NNNN`);
4. change the old ADR's supersession metadata: set `Status: Superseded` and
   add `Superseded by: ADR-NNNN`.

A Proposed replacement does not supersede established architecture. Keep the
old ADR Accepted until the replacement decision is actually Accepted.

### Semantics-preserving editorial amendments

An Accepted or Superseded ADR may be amended in place when the amendment does
**not** change its semantic decision or impact and instead improves the
historical record's legibility or self-containment. Appropriate examples
include replacing transient delivery terminology with stable semantic terms
in the filename/title/prose, correcting a typo that materially obscures the
existing decision, or clarifying wording that otherwise requires obsolete
planning context.

Every such amendment must add a header entry in this form:

```text
Amendment: YYYY-MM-DD — concise reason; no semantic change
```

Amendment entries are cumulative historical metadata and must not be removed.
The pull request must explain why the edit is semantics-preserving. Independent
review must verify that claim against the pre-amendment ADR; adding the
metadata is not permission to revise architecture without supersession.

A filename amendment keeps the ADR number unchanged and must update all
repository references in the same change. Renaming an ADR is therefore a
legibility/maintainability operation, never a way to create a new decision or
reuse an ADR number.

If a reviewer cannot establish semantic equivalence confidently, the change
must use the normal superseding-ADR process instead.

### CI enforcement

The repository enforces the mechanical part of this policy through
`.github/scripts/check-adr-immutability.py`, which runs in the always-required
`Classify changes` / `CI / gate` path.

For every ADR that was `Accepted` or `Superseded` on the PR base commit, CI:

- rejects deletion of the ADR number/history;
- rejects filename, title, or body changes that do not add a new `Amendment:` entry;
- permits a semantics-preserving filename clarification only when the ADR number is unchanged and
  the amendment is explicitly recorded;
- requires every new amendment entry to state `no semantic change` explicitly;
- prevents existing amendment metadata from being removed;
- permits an `Accepted` ADR only to remain `Accepted` or transition to
  `Superseded`;
- permits that transition only when the same change adds a new **Accepted**
  ADR whose `Supersedes: ADR-NNNN` metadata points back to the old record;
- rejects a Proposed replacement as insufficient to supersede established
  architecture;
- keeps an already `Superseded` ADR's status and supersession target immutable.

CI cannot determine whether prose or a filename change is truly semantically
equivalent. That is a mandatory review responsibility. The automated guard
verifies that an edit is explicitly presented as an editorial amendment; the
reviewer decides whether that claim is valid.

`Proposed` ADRs remain freely editable until accepted, subject to normal
review and the same durable-vocabulary rule.
