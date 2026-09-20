# Semantic contract maturity and durability

> **Status:** CONCLUDED
> **Scope:** When a currently authoritative semantic contract becomes durable, what evidence precedes that, and which compatibility/provenance obligations apply before and after
> **Authority:** Research provenance only. The durable conclusion lives in the [semantic evolution and support rules](../../architecture/overview.md#semantic-evolution-and-support) and the owning contracts they name; this brief decides nothing.

## Conclusion

The question was whether semantic-contract durability needs an explicit, evidence-gated promotion
lifecycle rather than arising automatically from introduction, decision acceptance, internal
publication, or the first need for a deterministic identifier. Neither eager durability nor a
universal `proving`/`promoted` lifecycle survived. The durable result is a separation of
obligations rather than a state machine:

- semantic identity is non-rebinding: a materially changed definition needs a distinguishable
  identity, and a definition with attributed retained records is fixed as a whole;
- historical meaning and continuing support are different obligations — retained attribution
  requires the exact defining semantics to stay resolvable while the record is retained, whereas
  content retention, decoding, execution, migration and interoperability are separately scoped
  promises of the owning contract;
- support obligations arise from published reliance or from acceptance at a declared authority's
  commit boundary, never from disposable proving activity, and support withdrawal never frees an
  identity for changed meaning;
- exercised-section freezing and a repository-wide maturity state were rejected.

The [Architecture Overview](../../architecture/overview.md#semantic-evolution-and-support) locates
the rule; [Semantic contract support](../../development/semantic-contract-support.md) owns the
declaration and review mechanism; the [Factory semantic-evolution contract](../../architecture/factory-design.md#111-semantic-evolution)
and [Engine Semantics v1](../../architecture/engine-semantics-v1.md) apply it without confusing
specification, implementation and support.

## What remains open

- Factory model composition — the sibling
  [Factory Model semantic composition](factory-model-semantic-composition.md) investigation; this
  result constrains it (no rebinding, explicit comparison, scoped support) without selecting a
  composition.
- Same-label amendment of an attributed Engine definition — a separate bounded Engine question;
  a particular amendment would need its own attribution and supported-input evidence and would
  not establish universal section freezing.
- Concrete custody mechanics for retained proving artifacts — ordinary bounded design/validation
  unless equality, acceptance or accountability semantics prove unsettled.
- Operational closure/retirement and audit horizon — remain with the open Operational questions.
- Simulation-analytics provenance ownership — its own register question.

## Reopening triggers

Reopen the common rule only if a concrete accepted use cannot preserve truthful attribution under
any bounded support scope short of permanent support, or a legitimate accumulating identity cannot
preserve its declared fixed aspects under the non-rebinding rule. Simplify the declaration
mechanism if it repeatedly adds ceremony without changing decisions or preventing contradictions.
