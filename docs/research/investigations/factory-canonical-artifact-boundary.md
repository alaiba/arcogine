# Factory canonical artifact boundary

Status: CANDIDATE framing; no conclusion

## Question and decision

Which concrete consumers require Factory canonicalization to be a reversible public artifact
representation, and should the representation used for fingerprinting, the representation persisted by
Storage, and decoding/reconstruction remain one contract or be separated?

The current implementation uses one canonical byte grammar for all three purposes:
`FactoryModelVersion.fingerprint()` digests it, `FactoryModelArtifact` publicly encodes/decodes it,
and built-in Storage persists the same verified bytes for controlled-revision resolution. That is the
baseline to examine, not evidence that the three responsibilities must remain coupled.

The decision at stake is the ownership boundary, not a new format. A conclusion could retain the
current unified artifact contract, narrow Factory canonicalization to a one-way fingerprint projection,
keep a public Factory artifact while allowing Storage a private representation, or justify another
bounded split only if concrete consumers require it. No replacement wire format, schema registry,
migration framework, or exact-definition identity is preselected.

## Why this is still CANDIDATE

Built-in Storage is a real consumer of reconstruction today, but the repository has not yet shown that
Storage's need to resolve accepted semantic state requires the fingerprint input itself to be a public
bidirectional artifact contract. Nor has another exchange/interoperability consumer established that
requirement.

Start an investigation when a concrete consumer needs one of these boundaries independently, when the
current coupling creates a compatibility/ownership constraint that matters, or when historical-support
research needs to choose what representation must actually be retained.

## Candidate distinctions to test

- **Unified canonical artifact:** keep the current public canonical bytes as fingerprint input,
  Storage representation, and decoding contract.
- **Fingerprint projection separated from persistence:** Factory defines deterministic fingerprint
  input but does not promise that the same representation is a public reversible storage artifact;
  Storage owns its private persistent representation and obtains only the domain verification or
  reconstruction capability it actually needs.
- **Public artifact separated from Storage representation:** retain a reversible Factory artifact for
  an evidenced exchange/consumer boundary while allowing Storage to persist a private form whose
  integrity and reconstruction semantics are independently specified.
- **Scoped decoder/reconstruction boundary:** retain current canonical bytes for identity while moving
  reconstruction behind the narrow consumer/adapter that needs it rather than treating decoding as a
  general Factory public contract.

These are candidates, not adopted architecture. Any investigation must distinguish deterministic hash
input from persistence, interchange, reconstruction, historical interpretation, and exact-definition
provenance.

## Proving cases and evidence needed before promotion

A future investigation should at minimum inspect:

- all current callers of `FactoryModelArtifact.encode/decode/fingerprint/verifier` and whether each
  needs public reversible bytes or only identity/verification;
- controlled-revision resolution across ordinary process reopen, distinguishing the semantic
  requirement to recover accepted state from the particular bytes used to do so;
- a Storage-format-only change with unchanged Factory meaning;
- a Factory-definition change where old bytes happen to satisfy a later decoder;
- independent verification or exchange, if any concrete consumer requires it, without assuming Storage
  and interchange share a format;
- the cost of retaining versus separating byte-level golden compatibility while the Factory definition
  remains an unpromoted development contract.

A candidate fails if it makes fingerprint equality imply unavailable historical meaning, moves
Factory semantics into Storage, makes a private storage format semantic identity, or invents a public
codec/retention promise with no consumer.

## Destination

If evidence justifies a change, reconcile the result into the Factory model/representation contract and
Storage architecture, coordinate with
[historical storage support](storage-historical-support.md) and the
[exact-reference/conversion questions](semantic-equivalence-and-reference-boundaries.md), and only then
admit a bounded implementation slice. If no consumer justifies separation, retain the current unified
contract without treating its persistence use as an independent durability promise.
