# Equivalence, identity, semantic responsibilities, and labels

Part of the [architecture overview](../overview.md#routed-topics); it is mandatory whenever the overview routes your change here.

## Equivalence, canonicalization, and fingerprints

The owning domain first states what objects are being compared and which distinctions matter for
the declared purpose. For a domain `X`, a semantic equivalence relation `~` partitions `X` into
equivalence classes. It must be reflexive, symmetric, and transitive. A tolerance, similarity score,
one-way compatibility test, or equality of one experiment's outcomes is not automatically that
relation. Different concerns can legitimately use different equivalence relations.

**Canonicalization** selects a deterministic representation of the distinctions the owning contract
retains. When `C` claims to represent the classes of a declared relation, its correctness condition
is `x ~ y` if and only if `C(x) = C(y)`: it neither distinguishes equivalent members nor collapses a
required distinction. This is a criterion for that claim, not a claim that every behavioral
equivalence has a computable complete canonicalizer. A concrete contract must state its scope and
justify any narrower representation rather than infer universal equivalence from a byte comparison.

A **hash function** maps representations to digests. A **fingerprint** is the resulting compact,
content-derived value together with whatever interpretation context its contract requires. For a
canonical-content fingerprint, the construction is `F(x) = H(C(x))`. The equivalence decision comes
before `C`, and `C` comes before `H`; hashing does not decide which differences are consequential.
Equal canonical bytes yield equal digests. Treating equal digests as equal canonical bytes relies on
the chosen hash's collision-resistance assumption, not mathematical injectivity. A collision is not
semantic equivalence, and hashing cannot recover information the canonicalizer discarded.

Always ask **a fingerprint of what, under which rules?** A model-content fingerprint does not thereby
identify its complete validation definition, the Engine interpretation, the executable build, or a
historical occurrence. A build digest can distinguish semantics-preserving refactors without proving
behavioral difference. A definition or schema reference helps only when its referent and resolution
are known. Reference spelling, equality, availability, and support are separate properties.

The [Factory model](../factory-model.md) continues to own the concrete content distinctions and
canonical bytes used by `ModelFingerprint`; this vocabulary changes none of its field membership,
ordering, normalization, or golden vectors. The [Engine specification](../engine-semantics.md) owns its
result-affecting rules. Equal results for one workload do not establish equal Factory designs or
behavioral equivalence for every admitted Engine input. Neither definition is a version of physical
reality, and an external observation does not become a simulation result by being compared with one.

## Separate semantic responsibilities

| Responsibility | Question and owning surface |
|---|---|
| Semantic definition | What do authored facts or interpretation rules mean, and which distinctions matter? Factory and Engine specifications own their respective answers. |
| Canonical representation / codec | Which representation encodes those distinctions, and which grammar does a decoder accept? The owning representation contract defines framing, normalization, and validation. |
| Runtime executability | Does this implementation execute the behavior required by the represented content and requested interpretation? Engine owns this test. |
| Exact reference / provenance | Which content, definition, implementation, inputs, or occurrence does the claim concern, and can that basis be resolved? The relevant domain or accepting authority owns the binding. |
| Compatibility / migration / support | Which consumers, conversions, preserved distinctions, losses, and obligations are supported? These are declared relationships, not consequences of a matching name. |
| Human naming / attestation | How does a party refer to or make a claim about identified things? The label's binding and any declaration supply its meaning; its spelling supplies no semantics. |

These responsibilities may share an artifact where a concrete requirement justifies that choice;
they must not be collapsed merely because a string is reused. A format discriminator does not define
an architectural generation, a specification title does not prove runtime support, and a support
label does not determine equivalence. Dependent work must name the represented-content or behavioral
prerequisite, rather than manufacture a dependency from a version label alone.

**Strict decoding** means enforcing the declared decoding contract: grammar/framing, canonicality
where required, and the applicable validity predicates. It does not establish historical provenance
that the input does not contain, or prohibit a separate importer or migration operation. Such an
operation must identify its source/target meanings and any preserved distinctions, assumptions, or
losses instead of silently pretending source data was authored under the target definition. No
legacy decoder or migration is introduced by this distinction; the current development reset policy
in [semantic evolution and support](semantic-evolution.md) and the [Factory artifact contract](../factory-model.md#5-canonicality-and-decoding) still apply.

## Human-readable labels and attestations

A human-friendly label is optional and orthogonal to content equality, determinism, encoding,
execution, and support. It may alias an exact reference or name a later declaration grouping several
references, such as a Factory definition, Engine definition, and build. A declaration or attestation
records what a party claims about that basis; a convenient name alone proves neither the claim nor
its fulfilment. No shared label, release-bundle, or attestation entity is introduced here.

Where a label selects a configuration or a declaration participates in an admission policy, resolve
and record the relevant basis as required by that use. The selected facts and explicit policy, not
the label's intrinsic spelling, supply its effect. Stable declaration bindings must not be silently
rebound; mutable aliases must not be passed off as exact historical references.

Development status is human-facing repository state only. It does not appear in canonical bytes,
fingerprints, decoder dispatch, runtime APIs, or provenance. The current Factory fingerprint contains
only its domain namespace, digest algorithm, and digest; the current Engine exposes no dedicated
definition identifier. Neither absence implies that an exact definition reference can never be
needed: the unresolved concrete boundary question remains in the [research
register](../../research/research-register.md), with [question-specific
framing](../../research/investigations/semantic-equivalence-and-reference-boundaries.md).
