# ADR-0015: Engine Semantics Identity and Reproducibility

Status: Proposed
Date: 2026-09-02

## Context

Arcogine already distinguishes canonical Factory model identity from runtime identity.
`ModelFingerprint` identifies authored semantic content, while `RunId` identifies one simulation
runtime epoch. Gate 2, W1, Gate 3, and Gate 4 also establish deterministic Engine-owned behavior
such as resource-selection ranking, child-work decomposition, scheduler/event ordering, session
advancement, and supported runtime observations/events.

Gate 5 makes a latent gap explicit. Spatial transfer outcomes depend partly on authored Factory
facts and partly on Arcogine's interpretation of those facts. More generally, the same canonical
factory design could produce a different semantic outcome if a future Engine release intentionally
changes a result-affecting dispatch, scheduling, decomposition, or transfer rule. Requiring every
such Engine-policy change to mint a new Factory model fingerprint would conflate the design being
executed with the Engine semantics used to interpret it.

A build/release identifier is not sufficient semantic provenance: unrelated implementation or
packaging changes may alter a build without changing observable execution semantics, while an
intentional semantic-policy change must remain attributable even if implemented by several builds.
The sibling Challenge capability already follows the analogous rule that result-affecting
evaluation changes create a new evaluation-policy version, while application build identity is
only diagnostic provenance.

The runtime provenance established by
[ADR-0011](0011-runtime-observation-and-event-contract.md) currently carries `RunId` and
`ModelFingerprint` but no explicit identity for result-affecting Engine interpretation semantics.
Because ADR-0011 is Accepted, any extension is recorded by a new decision rather than editing that
historical ADR in place.

## Decision

The proposed decision is:

1. **Introduce one `EngineSemanticsVersion` as the semantic identity of Arcogine's complete
   result-affecting simulation interpretation for a run.** It is distinct from:
   - `ModelFingerprint` — which authored design was executed;
   - `RunId` — which runtime epoch produced the facts;
   - software/build/release identity — which implementation artifact happened to execute them.

2. **The durable reproducibility inputs for a simulation outcome are conceptually:**

   ```text
   ModelFingerprint
   + EngineSemanticsVersion
   + explicit workload
   + seed/random inputs
   + ordered external commands
   ```

   Additional explicit run inputs belong in this tuple when they can affect semantic outcome;
   `RunId` itself is correlation identity, not a result-affecting input.

3. **Authored production-system facts and Engine interpretation rules have different owners.**
   - A fact describing what production system was authored belongs to the canonical domain model
     and its `ModelFingerprint`.
   - A rule describing how Arcogine interprets any such design belongs to
     `EngineSemanticsVersion` when changing the rule can change semantic outcome for identical
     explicit inputs.
   - An implementation algorithm that can be replaced without changing observable semantic
     outcome is not part of Engine semantic identity.

4. **One Engine semantics version initially covers the complete result-affecting Engine
   interpretation.** Do not introduce independently versioned dispatch, scheduler, decomposition,
   transfer, or rounding policy identities unless concrete independent-evolution requirements
   later justify them.

5. **The first semantics version must normatively fix all current result-affecting Engine rules.**
   At minimum this includes the accepted behavior of:
   - deterministic dispatch ranking/tie-breaking;
   - W1 decomposition/release semantics that affect execution outcome;
   - authoritative scheduler/same-time ordering semantics;
   - Gate 5 distance/rounding/zero-distance/destination-binding rules once they are accepted;
   - any other Engine-owned interpretation rule whose change could alter semantic event ordering,
     simulated times, assignments, terminal execution state, or derived outcome facts for the same
     explicit inputs.

6. **A semantics version is selected at run establishment and cannot change mid-run.** The
   initial implementation may support exactly one current version rather than building a
   multi-version resolver. A run performed by an implementation that does not support the
   requested/recorded version must fail explicitly instead of silently executing under different
   semantics.

7. **Runtime provenance must carry `EngineSemanticsVersion`.** Supported observations and events
   that already expose durable run/model provenance must also make the Engine semantics version
   attributable. This supplements ADR-0011; it does not replace `RunId`, `ModelFingerprint`, or
   optional controlled-revision provenance.

8. **Released Engine semantics versions are immutable and never reused.** Any intentional change
   that can alter semantic outcome for the same explicit inputs creates a new version. A
   semantics-preserving refactor, performance optimization, dependency upgrade, or build change
   retains the version only when it preserves the normative observable behavior.

9. **The durability guarantee is attribution plus a verifiable definition, not permanent exact
   re-execution.** For every released version Arcogine retains:
   - an immutable written specification of the result-affecting rules; and
   - pinned behavioral conformance fixtures mapping representative explicit inputs to expected
     semantic outcomes.

   Fixtures pin behavior such as authoritative transition ordering, dispatch/assignment choices,
   simulated times, terminal execution state, and derived outcomes. They must not accidentally
   make unrelated transport, serialization, projection, or non-behavioral observation-schema
   details part of Engine semantic identity.

10. **Exact historical re-execution is supported only where a compatible implementation exists;
    it is not promised forever.** A future implementation may add a resolver capable of executing
    older semantics versions and can prove compatibility against the retained specification and
    fixtures, but retaining every old Engine binary/dependency stack indefinitely is not part of
    the architecture contract.

11. **Retirement removes executability, not provenance.** Even if a historical semantics version
    is no longer executable by current software, its identifier, normative specification, and
    conformance fixtures remain durable. Historical results remain attributable and interpretable.
    The detailed retirement process and any multi-version execution mechanism are deferred until a
    second semantics version makes them concrete concerns.

12. **Cross-version result comparison is explicit.** A result produced under one
    `EngineSemanticsVersion` must not be silently treated as directly comparable to a result under
    another version where the changed semantics can affect the compared facts. Consumer-specific
    compatibility/applicability policy belongs to the consumer (for example Challenge comparison
    or future Governance `EvidenceUse`), not to Engine identity itself.

## Alternatives considered

### Treat `ModelFingerprint` as sufficient execution-semantics provenance

Rejected. The same authored design can legitimately be interpreted by different accepted Engine
semantic policies over Arcogine's lifetime. Forcing such changes into the model fingerprint would
make Engine policy masquerade as authored Factory design.

### Use ordinary application/build version

Rejected. Build identity is too broad and unstable to be semantic identity. Many builds can
preserve one semantic contract, and a semantic-policy version must remain meaningful independent of
packaging or diagnostic build metadata.

### Create one independently versioned policy per Engine concern

Rejected for the first contract. Dispatch, decomposition, scheduling, and transfer semantics all
contribute to one simulation outcome. Independent policy identities would multiply compatibility
states before any concrete independent-evolution requirement exists.

### Create one immutable run-specification identity that combines every input

Deferred. A composite run-spec artifact may later be useful, but it is not required to establish
the missing semantic distinction. The underlying model identity, Engine semantics identity, and
explicit run inputs should remain individually meaningful and attributable.

### Guarantee permanent exact execution of every released semantics version

Rejected. Keeping an entire historical simulation implementation buildable and runnable against
future JVMs, build tools, and dependencies creates an unbounded maintenance commitment. Retained
normative semantics plus behavioral conformance fixtures provide durable verification without that
liability.

## Consequences

- Gate 5 can place authored spatial facts in `factory-model:v2` while keeping distance/rounding and
  other Arcogine interpretation rules Engine-owned.
- A model fingerprint no longer needs to pretend to identify every possible deterministic outcome;
  it identifies the authored semantic design.
- Runtime observation/event provenance needs an additive semantics-version field under a new
  accepted decision that supplements ADR-0011.
- Historical Challenge, Governance analytical evidence, checkpoint/recovery, and Operational
  analytics can retain Engine interpretation provenance when they begin consuming real Engine
  results, without conflating it with their own policy/context identities.
- No generic cross-domain policy framework is created. Challenge evaluation-policy identity,
  Governance requirement/assertion/evidence semantics, and Operational execution-context identity
  remain separate concepts with separate ownership.
- The first Engine semantics release must include a normative specification and behavioral
  conformance fixtures before later semantic evolution makes those facts unrecoverable.

## Charter alignment

This proposal strengthens deterministic and explainable execution while preserving Arcogine's
separation between canonical modeled intent, runtime execution, and consumer interpretation. It
makes historical results attributable without turning implementation binaries into permanent
product artifacts or coupling Factory design identity to Engine release mechanics.
