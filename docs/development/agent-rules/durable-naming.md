# Temporary delivery coordinates and durable documentation

Arcogine tracks open questions in the [research register](../../research/research-register.md) and known work in GitHub issues. An issue exists only for work defined well enough to implement; it waits until someone starts it, and in the rare case something blocks it, its body says what. Issues carry no workflow-state labels. Issue and pull-request numbers are the delivery coordinates. PR-local review/finding identifiers use the reserved `REV-<N>` namespace: ordinary variable-width decimal numbering, with no fixed-width zero-padding required or preferred. The former `PLAN-<TRACK>-<LOCAL-ID>` planning namespace is retired together with the planning area, and no tracked file may contain one.

These are temporary delivery coordinates. They may be used in issues, pull requests, PR descriptions/comments, reviews, branch names, commit messages, handoff prompts, and other active/delivery-history context where the coordinate helps track work — see [`.github/CONTRIBUTING.md`](../../../.github/CONTRIBUTING.md)'s commit message guidance, which this section does not change.

Durable repository assets must not depend on transient coordinates. Durable assets include
architecture, product, reference, and development documents; code comments; workflow definitions;
and test/class/file names introduced alongside a change. A transient coordinate may remain in
active delivery context or durable delivery-history provenance (such as a commit message
or merged PR reference), but maintained semantic state must survive independently through the
durable capability, contract, identity, invariant, behavior, deliberately promoted artifact, or
provenance it represents. Exact `commit SHA + workspace/... path` pairs identify artifacts only
while those artifacts are in active workspace custody; copying the pair into durable state does
not preserve the artifact. Working/process material may discuss coordinate syntax when the syntax
itself is the subject. When delivery outcomes move into durable semantic naming, translate them
into what they represent rather than naming them after the coordinate that tracked them.

Product source and tests have a stricter lifetime boundary: maintained files under `product/`
must not reference `docs/research/` or transient research-workspace artifacts. Research may cite
implementation as evidence, but product source must describe current behavior, invariants, and
limitations in durable semantic terms and, when a textual authority is useful, point to the current
architecture, specification, reference, or development contract that owns them. The dependency
direction is one-way: transient research material may point to durable implementation; durable
product source must not point back to it.

The mechanical checkers enforce recognizable cases deterministically by scanning tracked
repository text (`git ls-files`, so generated/untracked/build output is never in scope):
`.github/scripts/check-delivery-labels.mjs` rejects any `PLAN-*` or `REV-<N>` token in a tracked
file or file path; `.github/scripts/check-transient-coordinates.mjs` rejects an exact full commit
SHA paired with a concrete `workspace/...` artifact path in durable files; and
`.github/scripts/check-source-authority-links.mjs` rejects product-source references to
`docs/research/` or `workspace/research/`. These checkers do not attempt to infer semantic
dependence from prose without a safe syntax signal. Catching semantic dependencies without a safe
literal signal (for example, prose like "the next stage") remains a human-review responsibility.

`docs/architecture/` holds Arcogine's current architecture — see
[`docs/architecture/overview.md`](../../architecture/overview.md) for cross-cutting principles and
domain boundaries, and [`.github/CONTRIBUTING.md`](../../../.github/CONTRIBUTING.md) for the rule that a
significant architectural change is reconciled into the owning architecture/specification document
rather than left in delivery history, plus the historical decision-rationale exception.
