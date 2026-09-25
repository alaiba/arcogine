# Semantic contract support

This policy is the review mechanism behind the
[semantic evolution and support](../architecture/overview.md#semantic-evolution-and-support) rules
in the Architecture Overview, which own the architectural constraint; owning specifications define
domain meaning. This document owns how work-in-progress status, promotion and support are stated
and reviewed. It is not a support registry, a maturity tracker or a research backlog.

## Work in progress is the default

The Factory model and the Engine interpretation are work in progress (`factory-model:wip`,
`engine-semantics:wip`). Each owning specification states that status in its header. Review a change
to a WIP definition like any other definition change:

- the owning specification, its golden vectors or conformance fixtures, and every dependent
  consumer, plan and document change together;
- no successor identity, legacy decoder, alias, tombstone, dual publication or migration path is
  added for earlier development material — stale or foreign input fails explicitly instead;
- vectors and fixtures are regenerated deliberately from the specification, preferably through an
  independent implementation of it, and the change says why the old values no longer hold.

Name contracts, types, packages, files and tests by what they mean. An ordinal suffix, an
implementation milestone or a golden vector is not a maturity or support signal; maturity and
support are stated explicitly in the owning contract.

## Audit the referent behind a durability claim

Words such as *published*, *attributed*, *retained*, *released* or *fixed* are claims about something
concrete. Before a review accepts one, identify its referent and classify it:

- a **capability** — an API, a store implementation, a serializer, a codec;
- an **accepted use** — a record admitted into a declared custody, a consumer that relies on it;
- a **promise** — owning-contract text committing to a durable behavior or support scope;
- **fulfilment evidence** — vectors, fixtures and tests showing a promise is met;
- a **cost preference** — a judgement that one design is cheaper or simpler.

Only an explicit promotion creates durable obligations; the other categories are capabilities or
evidence. For example, an in-process `FactoryModelPublisher.publish` call and a revision store opened
over a temporary directory are capabilities, not accepted use, and golden vectors are fulfilment
evidence for the current definition, not a promise that it will not change. Custody is what an
authority declares, not where it happens to be used: a path name, test label or later deletion neither
proves disposability nor creates retention. When a claim's referent cannot be found, say what was
searched; a failed search is not proof of absence.

## Declaring a promotion

Promotion is an explicit owner decision tied to a concrete durable-use need, recorded in the owning
specification or authority contract in the same change that makes the durable use possible. Factory
and Engine promote independently. Use only the information the promised use needs:

- the owner approval and the durable-use need it serves;
- the exact frozen definition — grammar or rules, validation and refusal behavior, fixtures — and
  its durable semantic name, distinct from the WIP marker; a mutable document title or branch tip
  never selects a definition;
- the supported uses, inputs, consumers and exclusions;
- custody: the accepting authority, retained versus disposable scope, horizon, exact-definition
  resolution and the transitive basis the claimed explanation needs;
- separately, content retention, decoding, execution, migration, interoperability and
  historical-explanation promises, including dependencies and explicit gaps;
- failure/refusal behavior for unsupported identity or input, missing or corrupt basis, undeclared
  custody and exhausted retention;
- evidence of fulfilment, its scope, known limitations and unimplemented obligations;
- authorized change/retirement behavior: successor meaning, effects on existing accepted uses,
  retained basis, and refusal after expiry.

A retained authority admitting a promoted identity refuses WIP and other unpromoted definitions.
Promotion is a status in one owning contract, not a platform-wide lifecycle type; after it,
declarations stay scoped by promise, so a contract can carry enduring attribution while only part of
its behavior is executed or supported.

An obligation and evidence that it is fulfilled are different facts: an unproved promise of a
promoted contract is a validation gap to close, not a promise erased by missing evidence. Silence
creates no support offer. Discovering retention of, or reliance on, material outside a declared
custody blocks further admission until custody is declared or the use is withdrawn, and requires
accounting for what was already accepted; it never promotes the contracts involved by itself.

## Review evidence proportionate to each promise

Use canonical-byte vectors and strict rejection cases for fingerprint reproduction within a
definition; refusal cases for discarded or foreign input; custody-declaration and no-adoption cases for
proving stores; reopen/corruption/old-definition cases for retained history once it exists;
supported-input, interaction and rejection fixtures for Engine semantics; boundary-only clients and
unknown-version/upgrade cases for interchange; and authority, failure and gap evidence proportionate
to consequential use. A serializer, marker constant, declaration or green unrelated test is not that
evidence. An injected verifier seam also means the absence of an in-tree codec cannot prove the
absence of every possible producer.

No new research is needed merely to fill a declaration whose semantics are settled, or to implement an
explicit owner decision about a contract's status; use the normal research boundary only when
equality, acceptance or consequential accountability itself remains unresolved. Simplify this
checklist if it adds process without changing decisions, while preserving the owning semantic
obligations.

## Discriminating review cases

These cases separate distinct obligations; they are not a mandatory ontology for every future
question.

| Case | What review must distinguish |
| --- | --- |
| WIP definition correction | The specification, vectors or fixtures and dependents change together under the same marker. Earlier development artifacts are refused or reset, never migrated or reinterpreted. |
| Durability claim | Identify the referent — capability, accepted use, promise, fulfilment evidence or cost preference. Only an explicit promotion creates durable obligations. |
| Proving persistence | A proving store may survive reopen, but it declares disposable custody; persisting WIP content there creates no attribution or compatibility promise, and it never adopts a location it did not create. |
| Accidental retention | WIP material found retained or relied on outside a declared custody is a defect: stop further admission, preserve what was accepted and the definition it used as far as evidence allows, and decide explicitly. It is neither silently rewritten nor treated as a promotion. |
| Promoted definition correction | An identity-affecting correction needs a distinguishable identity; the promoted name always denotes its definition. |
| Historical artifact after evolution | For a promoted definition, retain the exact revision → fingerprint → definition/artifact basis the accepted use requires and never resolve it against current state. Equal content can recur in a distinct revision. |
| Engine rule never exercised | For a promoted interpretation, editorial section boundaries and missing fixture coverage do not make part of it mutable; rejection behavior and cross-rule interactions still matter. While WIP, an unexercised rule is corrected like any other rule. |
| External consumer | A concrete information set and scoped compatibility evidence create support; the existence of an API or serializer does not. |
| Operational consequence | The [Operational continuity contract](../architecture/operational-continuity.md) governs continuity, accepted compaction, loss and divergence. A generic expiry field cannot waive accountability or erase the only audit basis. |
| Representation change | An adapter or layout change may preserve semantic identity while breaking an independently promised wire contract. Canonical identity-defining bytes are semantic, unlike ordinary serializer bytes. |
| Retired execution | For a promoted interpretation, the exact definition and attribution survive for the required horizon; missing execution is explicit, neither missing meaning nor automatic invalidation of past results. |
