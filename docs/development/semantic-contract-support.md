# Semantic contract support

This policy implements [ADR-0017](../architecture/decisions/0017-ground-zero-semantic-evolution.md).
The [Architecture Overview](../architecture/overview.md#semantic-evolution-and-support)
states the enduring principles; owning ADRs/specifications define domain meaning.
This document owns the review mechanism, not a support registry or research backlog.

## Declare support where the contract is owned

Before publishing reliance or admitting material to retained authority, record a
compact declaration in the owning specification or authority contract. Use only
information needed for the promised use; links to exact owned definitions and
existing evidence suffice. No universal maturity state or central registry is
required. Introduction and support commitment may occur together.

Record:

- the owner, exact identity/definition revision and its fixed aspects; a mutable
  document title or branch tip alone does not select a historical definition;
- the supported uses, inputs, consumers and exclusions, and whether each obligation
  comes from a published promise or acceptance at a named authority's commit boundary;
- custody: the accepting authority, retained versus disposable scope, horizon,
  exact definition resolution, transitive basis needed for the claimed explanation,
  and how transfer from disposable material becomes retained admission;
- separately, content retention, decoding, execution, migration, interoperability
  and historical-explanation promises, including dependencies and explicit gaps;
- failure/refusal behavior for unsupported identity/input, missing or corrupt
  basis, undeclared custody, incomplete admission, and exhausted retention;
- evidence of fulfilment, its scope, known limitations and unimplemented obligations;
- authorized change/retirement behavior: successor meaning, scope and compatibility
  evidence, effects on existing accepted uses, retained basis, and refusal after expiry.

An obligation and evidence that it is fulfilled are different facts. An unproved
promise is a validation gap to address, not a promise erased by missing evidence.
Silence creates no new support offer, but does not waive obligations of an actual
accepted use. Discovering undeclared retained acceptance blocks new admission until
custody/support is declared and requires accounting for the facts already accepted.

Tests, scratch stores, drained events and local runs are disposable only while no
retained use has been admitted against them. Do not turn a path name, test label or
later deletion into proof of disposability. A post-reset Engine must either use a
frozen exact definition when stamping accepted/retained records, or keep the records
explicitly disposable/non-retained. The first retained attribution freezes the whole
definition; a new definition needs a distinguishable identity after that boundary.

## Review evidence proportionate to each promise

Use canonical-byte vectors and strict rejection cases for fingerprint reproduction;
reopen/corruption/old-definition cases for retained history; supported-input,
interaction and rejection fixtures for Engine semantics; boundary-only clients and
unknown-version/upgrade cases for interchange; and authority, failure and gap evidence
proportionate to consequential use. A serializer, version constant, declaration or
green unrelated test is not that evidence. An injected verifier seam also means
absence of an in-tree codec cannot establish absence of every possible producer.

No new research is needed merely to fill a declaration whose semantics are settled.
Use the normal research boundary when equality, acceptance or consequential
accountability itself remains unresolved. Experiments and migrations should have a
bounded reassessment trigger; an intentionally internal evolving interface does not
need a mandatory promotion/retirement ceremony. Simplify the checklist if it adds
process without changing decisions, while preserving the owning semantic obligations.

## Reusable discriminating cases

These cases preserve the knowledge needed to review the reset's consequences; they
are not a mandatory ontology for every future research question.

| Case | What review must distinguish |
| --- | --- |
| Fingerprint-definition correction | Before first accepted/retained attribution, a normative definition can be corrected; after it, an identity-affecting correction needs a new identity. Cross-reset label reuse additionally requires unchanged definition/bytes. |
| Historical artifact after evolution | Retain the exact revision → fingerprint → definition/artifact basis required by the accepted use; never resolve it against current state. Equal content can recur in a distinct revision. |
| Engine section never exercised | Editorial section boundaries and missing fixture coverage do not make part of an attributed definition mutable; rejection and cross-rule interactions can still matter. |
| Engine run already retained | Keep its attribution and exact definition. Repair of implementation nonconformance differs from changing normative behavior; a new interpretation cannot silently rewrite that result. |
| Disposable proving artifact | Scratch activity alone creates no retained use; transfer into an accepting authority must declare custody/support before admission. |
| Retained experimental artifact | Record exact definition and actual retained basis, horizon and failure behavior. If the authority cannot meet those obligations, refuse admission; if already accepted, remediate the defect without un-accepting the fact. |
| External consumer | A concrete information set and scoped compatibility evidence create support; existence of an API or serializer does not. |
| Operational consequence | ADR-0013 governs continuity, accepted compaction, loss and divergence. A generic expiry field cannot waive accountability or erase the only audit basis. |
| Representation change | An adapter/layout change may preserve semantic identity while breaking an independently promised wire contract. Canonical identity-defining bytes are semantic, unlike ordinary serializer bytes. |
| Retired execution | Exact definition and attribution survive for the required horizon; missing execution is explicit. Retired execution is not missing meaning or automatic invalidation of past results. |
| Resurfacing pre-reset use | Stop the affected reset transition and inventory it; support/migration/exclusion needs an explicit decision. Factory's prefix plus verification discriminates policies; Engine needs strict non-reuse of changed labels. |

Published attribution with bounded execution is coherent; one exclusive
whole-contract `proving/promoted` state cannot express it. This is why declarations
are scoped by promise, without introducing a platform-wide lifecycle type.
