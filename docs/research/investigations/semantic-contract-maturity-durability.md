# Semantic contract maturity and durability

> **Status:** CONCLUDED; its lifecycle rule was later replaced by the owner-directed provisional semantic-contract reset
> **Scope:** When a currently authoritative semantic contract becomes durable, what evidence precedes that, and which compatibility/provenance obligations apply before and after
> **Authority:** Research provenance only. Current rules live in the [semantic evolution and support rules](../../architecture/overview.md#semantic-evolution-and-support) and the owning contracts they name; this brief decides nothing.

## Conclusion reached

The question was whether semantic-contract durability needs an explicit, evidence-gated promotion
lifecycle rather than arising automatically from introduction, decision acceptance, internal
publication, or the first need for a deterministic identifier. Neither eager durability nor a
universal `proving`/`promoted` lifecycle survived. The reconciled result was a separation of
obligations rather than a state machine:

- semantic identity is non-rebinding, and a definition with attributed retained records is fixed as
  a whole;
- historical meaning and continuing support are different obligations, and content retention,
  decoding, execution, migration and interoperability are separately scoped promises;
- support obligations arise from published reliance or acceptance at a declared authority's commit
  boundary, never from disposable proving activity, and support withdrawal never frees an identity
  for changed meaning;
- exercised-section freezing and a repository-wide maturity state were rejected.

## What replaced it

Applying "fixed at the first retained attribution" to the development-stage Factory and Engine
definitions produced a standing argument about whether and why those definitions had already been
fixed, and the follow-up investigation of that promotion boundary reached no accepted answer. On
2026-09-25 the owner replaced the premise directly: Factory and Engine semantics are work in progress
(`factory-model:wip`, `engine-semantics:wip`) until an explicit, owner-approved promotion tied to a
concrete durable-use need, and deterministic fingerprinting during development does not itself
promote or freeze a contract. The [reset rationale](../../history/decisions/2026-09-25-provisional-semantic-contract-reset.md)
records that decision and its knowledge transfer.

Distinctions from this investigation that survive in the current rules:

- support is scoped separately from meaning, and naming an identity creates no support obligation;
- a promoted identity never rebinds, and withdrawing support never frees it for changed meaning;
- disposable proving activity creates no obligation, and custody is declared rather than inferred;
- there is no repository-wide maturity state or registry — promotion is recorded per owning contract;
- a promoted definition is fixed as a whole, not section by section.

The attribution-driven freeze of development definitions, and the open question of same-label
amendment of an attributed Engine definition, are superseded. Concrete custody mechanics for proving
artifacts are now the declared proving-store boundary of the [controlled revision
contract](../../architecture/controlled-revisions.md).

## Reopening triggers

Reopen the promotion rule only if a concrete durable-use need cannot be met by explicit promotion of
an exact definition with scoped support, or a legitimate accumulating identity cannot preserve its
declared fixed aspects under the non-rebinding rule. Simplify the declaration mechanism if it
repeatedly adds ceremony without changing decisions or preventing contradictions.
