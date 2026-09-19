# Semantic identity and evolution

## Context

Arcogine attributes durable facts to named semantic definitions: a published model
carries a fingerprint under a named canonicalization policy, a controlled revision
binds one historical occurrence to one fingerprint, a simulation run is attributed to
one Engine interpretation, evidence references one exact source revision, and an
operational continuation answers for the facts it accepted. Every downstream use —
replay, historical explanation, conformance evaluation, comparison, audit — depends
on those references keeping the meaning they had when they were made.

Two forces pull against that. Definitions are corrected and extended as
implementation and consumers expose gaps, and continuing to decode, execute, migrate
or interoperate with every definition ever named is an open-ended cost that no
consumer has asked Arcogine to pay. Without a common rule, each contract would either
freeze prematurely, treating every named definition as a permanent support estate, or
evolve silently, letting an existing identity acquire changed meaning.

## Decision

1. **Semantic identity is non-rebinding.** An identity denotes exactly one definition
   and one intrinsic provenance. A materially changed definition — changed field
   membership, canonical bytes, result-affecting interpretation, or the referent of a
   historical record — requires a distinguishable identity. Corrections, changed
   interpretations and later occurrences are new distinguishable things; they never
   rewrite what an existing identity denotes. This holds for content identities,
   historical occurrence identities, evidence references and accountable
   continuations alike, each under the equality rule its owning contract defines.
2. **Attribution fixes the whole definition.** A definition may be corrected in place
   only until the first retained or accepted record is attributed to it. From that
   point the whole definition is fixed, including rules no fixture has exercised and
   rejection behavior no consumer has yet observed. Arcogine does not use
   exercised-section freezing, unreleased status, or expired support as grounds for
   amending an attributed definition under its existing identity.
3. **Historical meaning and continuing support are different obligations.**
   Retaining the exact definition an identity denotes, retaining content, decoding,
   executing, migrating, and interoperating are separately scoped promises with their
   own dependencies. Naming an identity creates none of them automatically, and a
   digest or identifier alone never substitutes for an explanation whose basis was not
   retained.
4. **Retained attribution requires resolvable definitions.** Every identity stamped
   on a retained or accepted record keeps its exact identity-defining definition
   resolvable for as long as that record is retained. Stronger owning contracts may
   add more, for example retained conformance fixtures for released Engine
   interpretations.
5. **Support obligations arise from accepted use and are scoped by the owning
   contract.** An obligation exists when the owning contract publishes reliance, or
   when an authority that has declared its custody accepts a record at its commit
   boundary. Disposable activity — tests, scratch stores, drained events, local runs —
   creates no obligation by existing; admitting such material into retained authority
   is a new decision. Acceptance by an authority that never declared custody is a
   defect to account for, not a waiver: the accepted fact is never disposed of to
   escape the obligation it created.
6. **Withdrawing support never frees an identity for changed meaning.** Retiring
   execution, decoding or compatibility for a definition removes a capability, not the
   meaning of the records attributed to it. Narrowing an in-scope promise requires an
   explicit, authorized transition under the owning contract, and that transition must
   neither rewrite history nor silently discharge obligations already created.
7. **There is no universal lifecycle.** Arcogine does not adopt a repository-wide
   maturity state such as `proving`/`promoted` for whole contracts, one retention
   horizon, or one version scheme. A contract can carry enduring attribution
   obligations while only part of its behavior is executed or supported; the owning
   contract states which promises it makes, and the review mechanism in
   [Semantic contract support](../../development/semantic-contract-support.md)
   checks the evidence proportionate to each promise.

## Consequences

- Every owning contract states its identity's equality rule, its fixed aspects, and
  the support it actually promises; silence creates no support offer but waives no
  obligation of an actual accepted use.
- Evolving a fingerprint policy, Engine interpretation, evidence reference scheme or
  continuation rule means introducing a distinguishable identity and reconciling the
  consumers in scope, never editing the attributed definition in place.
- Historical reconstruction can be truthful without permanent executors or eternal
  readers, because attribution and definition retention are separated from execution
  and decoding support.
- Same-identity amendment of an attributed Engine definition, concrete custody
  mechanics for retained proving artifacts, and closure of an operational
  continuation remain open questions for their owning contracts; this decision does
  not settle them.
- A change to this decision must reconcile the Factory, Engine, Governance and
  Operational contracts that apply it and the review guidance that checks it.

## Alternatives

- **Eager durability**: every named definition becomes permanently supported at
  introduction. Rejected because it manufactures a compatibility estate no consumer
  requires and makes ordinary correction impossible.
- **Universal maturity lifecycle**: one `proving`/`promoted` state per contract.
  Rejected because independent promises (retained attribution, executed behavior,
  outward compatibility) do not share one state, and the label would either over- or
  under-claim for every contract.
- **Exercised-section freezing**: only fixture-covered parts of a definition become
  fixed. Rejected because specification sections are editorial units, rules interact,
  and rejection behavior matters without a happy-path fixture exercising it.
