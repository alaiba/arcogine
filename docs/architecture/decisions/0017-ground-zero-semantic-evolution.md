# ADR-0017: Ground-zero semantic evolution and scoped support

Status: Accepted
Date: 2026-09-19
Supersedes: ADR-0006

## Context

On 2026-09-19, repository owner Vasile Alaiba declared that the repository is the
complete pre-reset estate: no client, retained artifact, external store, release,
deployment, or external consumer requires pre-reset support. This is a dated
owner-supplied operational fact, not an inference from an empty GitHub search,
missing implementation, or unavailable deployment inventory.

The high-risk semantic-evolution investigation received independent adversarial
review with **ACCEPT WITH QUALIFICATIONS**. The surviving result separates an
identity's meaning from its support obligations, while preserving accountability
for accepted uses. The review rejected treating a support reset as permission to
reopen unrelated Accepted semantics. This decision records that qualified result;
research evidence and its transfer audit belong to reconciliation delivery history.

[ADR-0006](0006-durable-semantic-fingerprint-contract.md) committed to supporting
the specific Factory V1 canonicalization policy. Withdrawing that policy changes
its applicability and requires supersession. The policy-level invariants survive
below. [ADR-0018](0018-factory-semantics-after-support-reset.md) separately
supersedes ADR-0014's Factory evolution/support decision and owns the surviving
Factory-specific rules. The Product Charter requires no amendment.

## Decision

### 1. Reset boundary and withdrawal

This decision takes effect when its reconciliation lands on `main`. The estate
covered by the owner's declaration is the repository's pre-reset contracts and
implementation, including `factory-model:v1`, `factory-model:v2`, and
`engine-semantics:v1`. Pre-reset compatibility, decoding, execution, migration,
retention, historical-reader, and interoperability promises are withdrawn. No
pre-reset artifact migration is required. Git history and historical ADR bodies
are preserved; withdrawal does not rebind identifiers or rewrite earlier facts.

Existing code, fixtures, interfaces, and specification text do not themselves
establish post-reset support. A post-reset owner must explicitly establish the
applicable support/custody contract under §3 before admitting retained use.
This decision does not delete implementation or alter its observable behavior.

If an excluded pre-reset client, artifact, store, release, deployment, or consumer
surfaces, **stop the affected transition**, inventory that use, and explicitly
decide support, migration, or exclusion. Do not silently reinterpret the owner's
declaration or dispose of the discovered fact.

### 2. Identity and definition evolution

A post-reset semantic identity may reuse a pre-reset label only if its definition
is unchanged rule-for-rule, and byte-for-byte where bytes define identity.
Otherwise a fresh label, namespace, or equivalent unmistakable discriminator is
mandatory. Ending support never permits recycling an identity for changed meaning.

Factory policy-domain prefixes plus artifact verification provide structural
discrimination. Engine identities have no equivalent discriminator: a changed
post-reset interpretation **must not** use `engine-semantics:v1`. This requires no
platform epoch service or central semantic registry.

A normative semantic definition may be corrected in place only until the first
accepted or retained record is attributed to it. That first attribution freezes
the **whole definition**: later behavioral or identity-affecting changes require a
distinguishable new identity/definition. Neither unexercised sections nor expired
support make the definition mutable again. This pre-attribution allowance does not
override the cross-reset non-reuse rule or a stronger owning contract's freeze
rule, including ADR-0015's released-version immutability.

Every semantic identity stamped on an accepted or retained record requires its
exact identity-defining definition revision to remain resolvable for as long as
that record is retained. This floor does not automatically retain all original
content or require perpetual decoding, execution, or interoperability. Stronger
owning obligations remain binding: in particular ADR-0015 retains released Engine
definitions and conformance fixtures after execution retirement.

### 3. Where support obligations arise

An obligation arises from either:

1. reliance explicitly published in the owning contract's support declaration; or
2. acceptance by an authority that has declared its custody, at that authority's
   commit/acceptance boundary.

Tests, scratch stores, drained events, temporary local runs, and equivalent
disposable activity are not accepted uses merely because they exist. Transfer of
such material into retained authority is a new admission decision, not an implicit
extension of the disposable activity.

Acceptance into an undeclared retained authority/store is an **admission defect**,
not a waiver: account for the obligations already created, block new admission
until custody/support is declared, and never retroactively dispose of the accepted
fact to evade those obligations. No declaration or maturity label proves that the
implementation fulfils its obligations.

Retention, exact-definition resolution, decoding, execution, migration,
interoperability, and historical explanation have separately scoped obligations.
Their dependencies must still be satisfied; a digest alone cannot provide an
explanation whose basis is missing. Narrowing an in-scope promise requires an
explicit authorized transition consistent with the owning authority's rules.
The declaration/checklist mechanism belongs to
[Semantic contract support](../../development/semantic-contract-support.md), not
to a universal whole-contract `proving/promoted` lifecycle.

### 4. Factory policy invariants carried from ADR-0006

These remain binding independently of support for V1:

- A fingerprint-policy version identifies a semantic/canonicalization contract,
  not merely a hash algorithm. An identity-affecting change requires a new label,
  subject only to §2's pre-attribution definition-development allowance.
- Semantic digests are independent of ordinary serializer representation. When
  canonical bytes define identity, their encoding rules are semantic rules.
- Fingerprinting is total over the published model domain. Publication validation
  must exclude inputs for which the chosen canonicalization is undefined.
- Semantic-content identity is distinct from controlled revision/history identity
  under ADR-0004 and ADR-0008; equal content can recur in different occurrences.

The V1-specific field list, byte grammar, golden-vector support requirement, and
legacy-hash compatibility period are no longer post-reset support obligations.
Their exact historical definitions remain in ADR-0006. Factory-specific authored
facts, identity boundaries, and comparison constraints are owned by ADR-0018.

### 5. Current-contract classification at the reset

“Removable” below permits a later bounded implementation change; it does not
authorize deletion in this reconciliation or bypass surviving domain invariants.

| Contract | Withdrawn pre-reset support | Retained intrinsic semantics | Implementation material removable in later cleanup | Unresolved decisions |
| --- | --- | --- | --- | --- |
| Factory V1 | V1 reproduction/compatibility vectors as a support promise, permanent decoder/resolution, non-spatial executor support, migration and legacy-hash compatibility | §4; canonical model boundary (ADR-0003); content versus occurrence (ADR-0004/0008); ADR-0018's no-lift and explicit-comparison rules | V1 codec/artifacts, V1-specific fixtures and adapters once dependents are inventoried | Future Factory composition, field membership and fresh identity contract; no V1 migration selected |
| Factory V2 | Automatic V2 release/promotion and permanent V1/V2 coexistence; no V2 support is created by retaining its document | ADR-0018's authored-field/validation semantics and authored-fact versus Engine-interpretation ownership; identity-affecting changes stay distinguishable | Unused V2 implementation scaffolding/fixtures if the eventual bounded Factory decision replaces them | Factory composition and the shape of a fresh semantic contract; no component/profile model selected |
| Engine V1 | V1 as a supported post-reset executor/label; no release or complete conformance is inferred from its constant or partial implementation | ADR-0015 in full: complete interpretation identity, determinism, fixed per-run version, authored/interpretation ownership, provenance, released definitions/fixtures and explicit comparison; other Accepted dispatch/control decisions remain binding | V1-specific runtime selection, fixtures or adapters only through a later explicit implementation change satisfying retained decisions | New supported interpretation and its conformance; post-attribution same-label amendment requires separate research and a later owning decision |
| Controlled history / Governance evidence | Reading pre-reset stores and compatibility with the current file layout; no such retained estate exists under the declaration | ADR-0008 and ADR-0016 **in full**, including immutable occurrence/basis, provenance, correction, exact resolution and evidence-use rules | Current file-adapter format and old-policy wiring, subject to preserving the authority contract | Representation, persistence, retention mechanisms and consumer policies already deferred by their owning decisions |
| Runtime / outward interfaces | Current adapters, HTTP/SSE/CLI projections and internal formats as automatic post-reset compatibility commitments | ADR-0011 post-authoritative derivation/order and ADR-0012 semantic projection/explicit outward contract boundaries | Legacy adapters/DTO formats once affected consumers are inventoried | Concrete supported external information set, schema evolution and recovery; analytics provenance remains separate |
| Operational history | **None**: no Operational support estate exists to withdraw | ADR-0013 **in full**, including accountable continuation, monotonic acceptance, divergence evidence and declared loss; ADR-0016 does not turn accumulating identity into point identity | None admitted by this reset | Existing Operational closure/retirement, audit horizon, trust and correspondence questions; implementation remains not admitted |
| Legacy `contentHash()` | Compatibility period and old raw-hash readers; no migration obligation | A bare legacy hash cannot acquire a different semantic meaning by relabelling; fingerprint is not revision identity | `contentHash()` and `modelContentHash` dependencies/fixtures | Bounded cleanup dependency inventory and truthful replacement provenance, not a new semantic research question |

ADR-0011, ADR-0012, and ADR-0015 need no supersession: this reset removes the
pre-reset support estate, not their intrinsic decisions or future released-contract
obligations. ADR-0008, ADR-0013, and ADR-0016 are preserved in full. Reopening any of
them requires a separate evidence-bearing decision; no part of this reset admits it.

## Alternatives and consequences

Universal eager support would recreate the withdrawn estate merely by naming a
contract. Universal whole-contract `proving/promoted` would conflate independent
promises (for example retained attribution with retired execution). Both are
rejected as platform rules; scoped owning declarations can establish support at
introduction when justified. Publication and commitment need not be separate
ceremonies.

Exercised-section freezing is rejected: specification sections are editorial units,
rules interact, and rejection behavior can matter without a happy-path fixture
exercising a section. Whole-definition attribution supplies a reviewable boundary.
Same-label Engine amendment after attribution remains a separate research question.

A reset epoch service adds no required semantic capability beyond the recorded
withdrawal and non-reuse rule. The reset is not evidence against canonical Factory
identity, Governance history, or Operational continuation. Their retained rules
constrain all later cleanup.

The consequence is a smaller support estate, not demonstrated implementation
conformance. Future admission must make custody and support explicit. The
development policy retains discriminating cases for reviewing that admission.

## Non-goals and Charter alignment

No product code is removed, no existing identity value is migrated, no Factory
composition or analytics ownership is selected, and no Operational work is admitted.
The decision preserves the Charter's semantic continuity, ownership, provenance,
and consequence-sensitive accountability without requiring permanent execution of
every interpretation or one storage representation.
