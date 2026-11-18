# Semantic evolution and support

Part of the [architecture overview](../overview.md#routed-topics); it is mandatory whenever the overview routes your change here.


The [definitions](determinism-and-durability.md#determinism-equivalence-identity-and-durability) separate semantic meaning,
exact reference, representation, execution, persistence, and support. The rules here apply that
vocabulary to the current Factory and Engine development policy. Their specifications remain
normative and correctable without a successor-per-correction identity ladder. Promotion establishes
a scoped stability/support commitment, not determinism, content equality, or the first possible
record of an exact basis:

1. **Development status is not semantic data.** The [Factory model](../factory-model.md) and [Engine
   interpretation](../engine-semantics.md) are current development definitions. Between repository
   revisions their records, grammar, canonical bytes, fingerprints, validation and interpretation
   may be corrected or extended; each change updates the owning specification, fixtures and
   dependents together. No maturity/status token is encoded in their technical contracts.
2. **Determinism holds within a definition.** For the same current definition and explicit inputs,
   canonical bytes, fingerprints and supported simulation outcomes are deterministic. A running
   session executes one fixed interpretation over one immutable published model snapshot; neither
   changes under it.
3. **Current technical values are not exact cross-revision definition references.** A Factory
   content fingerprint does not identify the exact definition revision that produced it, and the
   current Engine has no dedicated definition identifier. Current built-in Storage binds its contents
   separately to the definition build that wrote them and refuses a mismatching binding without
   reinterpretation or deletion. A raw decoder enforces its current grammar and predicates. No reader may
   claim historical meaning that its available basis does not establish.
4. **Nothing creates a support commitment by accident.** Tests, golden vectors, in-process
   publication, implementation landing, physical persistence, an internal file write, and a
   normative description of current behavior do not create a retained stability/support promise.
   Storage's product status and a payload definition's support status are separate; retained,
   commitment-bearing admission requires an explicit owner declaration.
5. **Promotion is an explicit owner decision tied to a concrete stability/support need.** It
   identifies the exact definition through an unambiguous stable reference and states the supported
   uses and consumers, retained basis, and obligations. Any human-readable label is optional and gets
   its meaning from that binding or declaration. The commitment is recorded in the owning contract,
   not a repository-wide maturity state, registry, or version ladder. Factory and Engine commitments
   need not be made together.
6. **The exact basis bound by a promotion never rebinds.** The stable reference recorded by the
   promotion continues to denote the same definition, including rules no fixture has exercised and
   rejection behavior no consumer has observed. A materially changed definition — field membership,
   canonical bytes, validation, result-affecting interpretation — must not replace the definition
   behind that reference; later development continues without a commitment or a later commitment
   records a distinct stable reference. Promotion does not require a new shared semantic entity or a
   human-readable name.
7. **Support is scoped separately from meaning.** For a promoted definition, retaining its exact
   definition, retaining content, decoding, executing, migrating and interoperating are separate
   obligations the owning contract declares for the uses it accepts; recording or naming a reference
   creates none of them. Withdrawing support removes a capability; it never permits the stable
   reference of an accepted commitment to be rebound to changed meaning and never silently
   discharges obligations accepted records created.
8. **Misuse is handled explicitly.** Retention of, or reliance on, material outside a declared custody
   is a defect to diagnose and account for: further admission stops, the accepted facts and the
   definition they used are preserved as far as the evidence allows, and the use is neither silently
   rewritten nor treated as having promoted the contracts it touched.

Historical *occurrence* identities are a different kind of identity: a `ControlledRevisionId`, an
evidence reference, an evaluation occurrence or an operational continuation never rebinds within the
authority that accepted it, under the equality rule its owning contract defines, whatever the
maturity of the content it refers to. The owning contracts apply these rules to the [Factory
model](../factory-model.md), the [Engine interpretation](../engine-semantics.md), [controlled
revisions](../controlled-revisions.md) and [evidence](../governance-evidence.md), and the decided but not
implemented [operational continuity](../../research/investigations/operational-continuity-contract.md) contract applies them to
accountable continuations; review guidance lives in [Semantic contract
support](../../development/semantic-contract-support.md). Why this boundary replaced an earlier
attribution-driven freeze is recorded, non-normatively, in the [provisional semantic-contract reset
rationale](/docs/history/decisions/2026-09-25-provisional-semantic-contract-reset.md).
