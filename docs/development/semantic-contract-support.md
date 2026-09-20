# Semantic contract support

This policy is the review mechanism behind the
[semantic evolution and support](../architecture/overview.md#semantic-evolution-and-support) rules
in the Architecture Overview, which own the architectural constraint; owning specifications define
domain meaning. This document owns how support is
declared and reviewed, not a support registry or a research backlog.

## Declare support where the contract is owned

Before publishing reliance on a semantic contract, or admitting material attributed to it into a
retained authority, record a compact declaration in the owning specification or authority
contract. Use only the information the promised use needs; links to exact owned definitions and
existing evidence suffice. No universal maturity state or central registry is required, and a
contract may be introduced and committed to in the same change.

Record:

- the owner, the exact identity/definition revision and its fixed aspects — a mutable document
  title or branch tip alone does not select a definition;
- the supported uses, inputs, consumers and exclusions, and whether each obligation comes from a
  published promise or from acceptance at a named authority's commit boundary;
- custody: the accepting authority, retained versus disposable scope, horizon, exact-definition
  resolution, the transitive basis needed for the claimed explanation, and how transfer from
  disposable material becomes retained admission;
- separately, content retention, decoding, execution, migration, interoperability and
  historical-explanation promises, including dependencies and explicit gaps;
- failure/refusal behavior for unsupported identity or input, missing or corrupt basis,
  undeclared custody, incomplete admission, and exhausted retention;
- evidence of fulfilment, its scope, known limitations and unimplemented obligations;
- authorized change/retirement behavior: successor meaning, scope and compatibility evidence,
  effects on existing accepted uses, retained basis, and refusal after expiry.

An obligation and evidence that it is fulfilled are different facts. An unproved promise is a
validation gap to close, not a promise erased by missing evidence. Silence creates no new support
offer but waives no obligation of an actual accepted use. Discovering undeclared retained
acceptance blocks new admission until custody/support is declared and requires accounting for the
facts already accepted.

Tests, scratch stores, drained events and local runs are disposable only while no retained use has
been admitted against them; a path name, test label or later deletion is not proof of
disposability. An Engine that stamps records with a semantics identity must either use a frozen
exact definition or keep those records explicitly disposable; the first retained attribution
freezes the whole definition, and a changed definition needs a distinguishable identity after that
boundary.

## Review evidence proportionate to each promise

Use canonical-byte vectors and strict rejection cases for fingerprint reproduction;
reopen/corruption/old-definition cases for retained history; supported-input, interaction and
rejection fixtures for Engine semantics; boundary-only clients and unknown-version/upgrade cases
for interchange; and authority, failure and gap evidence proportionate to consequential use. A
serializer, version constant, declaration or green unrelated test is not that evidence. An injected
verifier seam also means the absence of an in-tree codec cannot prove the absence of every
possible producer.

No new research is needed merely to fill a declaration whose semantics are settled. Use the normal
research boundary only when equality, acceptance or consequential accountability itself remains
unresolved. Experiments and pending migrations should carry a bounded reassessment trigger; a
deliberately internal, continuously evolving interface needs no promotion/retirement ceremony.
Simplify this checklist if it adds process without changing decisions, while preserving the owning
semantic obligations.

## Discriminating review cases

These cases separate distinct support obligations; they are not a mandatory ontology for every
future question.

| Case | What review must distinguish |
| --- | --- |
| Fingerprint-definition correction | Before the first retained attribution a normative definition can be corrected; after it, an identity-affecting correction needs a new identity. An existing label always denotes its unchanged definition and bytes. |
| Historical artifact after evolution | Retain the exact revision → fingerprint → definition/artifact basis the accepted use requires; never resolve it against current state. Equal content can recur in a distinct revision. |
| Engine section never exercised | Editorial section boundaries and missing fixture coverage do not make part of an attributed definition mutable; rejection behavior and cross-rule interactions still matter. |
| Engine run already retained | Keep its attribution and exact definition. Repairing implementation nonconformance differs from changing normative behavior; a new interpretation cannot silently rewrite that result. |
| Disposable proving artifact | Scratch activity alone creates no retained use; transfer into an accepting authority must declare custody/support before admission. |
| Retained experimental artifact | Record the exact definition, the actual retained basis, horizon and failure behavior. If the authority cannot meet those obligations, refuse admission; if it already accepted, remediate the defect without un-accepting the fact. |
| External consumer | A concrete information set and scoped compatibility evidence create support; the existence of an API or serializer does not. |
| Operational consequence | The [Operational continuity contract](../architecture/operational-continuity.md) governs continuity, accepted compaction, loss and divergence. A generic expiry field cannot waive accountability or erase the only audit basis. |
| Representation change | An adapter or layout change may preserve semantic identity while breaking an independently promised wire contract. Canonical identity-defining bytes are semantic, unlike ordinary serializer bytes. |
| Retired execution | Exact definition and attribution survive for the required horizon; missing execution is explicit. Retired execution is neither missing meaning nor automatic invalidation of past results. |

Published attribution with bounded execution is coherent; one exclusive whole-contract
`proving`/`promoted` state cannot express it. Declarations are therefore scoped by promise, without
a platform-wide lifecycle type.
