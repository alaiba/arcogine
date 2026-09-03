# ADR-0015: Engine Semantics Identity and Reproducibility

Status: Accepted
Date: 2026-09-03

## Context

Arcogine already distinguishes canonical Factory model identity from runtime identity.
`ModelFingerprint` identifies authored semantic content, while `RunId` identifies one simulation
runtime epoch. Existing resource-selection and dispatch rules, unit-work decomposition, scheduler
ordering, session advancement, and supported runtime transitions also establish deterministic
Engine-owned behavior.

Spatial transfer semantics expose the missing provenance dimension. Spatial outcomes depend partly
on authored Factory facts and partly on Arcogine's interpretation of those facts. More generally,
the same canonical design can legitimately produce a different semantic outcome under a later
Engine interpretation that intentionally changes dispatch, scheduling, decomposition, or transfer
behavior.

Forcing every such Engine change into `ModelFingerprint` would make Engine policy masquerade as
Factory design. Using ordinary build/release identity is also insufficient: many builds can preserve
one semantic contract, while a result-affecting policy change must remain attributable across builds.

The sibling Challenge capability already uses the analogous rule: result-affecting evaluation
changes require a new evaluation-policy version, while build identity remains diagnostic.

[ADR-0011](0011-runtime-observation-and-event-contract.md) is Accepted and establishes run/model
provenance but no identity for result-affecting Engine interpretation. This ADR therefore supplements
ADR-0011; it does not rewrite it.

## Decision

1. **Introduce one `EngineSemanticsVersion` as the semantic identity of Arcogine's complete
   result-affecting simulation interpretation for a run.** It is distinct from:
   - `ModelFingerprint` — which authored Factory design was executed;
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

   Additional explicit run inputs join this tuple whenever they can affect semantic outcome.
   `RunId` is correlation identity only and must not affect the result.

3. **Authored facts and Engine interpretation have different owners.**
   - A fact describing the production system the designer authored belongs to the canonical domain
     model and its `ModelFingerprint`.
   - A rule describing how Arcogine interprets any such design belongs to
     `EngineSemanticsVersion` when changing the rule can change semantic outcome for identical
     explicit inputs.
   - A replaceable algorithm or state-machine implementation that preserves those observable
     semantics is an implementation detail.

4. **One Engine semantics version covers the complete result-affecting Engine interpretation.**
   Dispatch ranking, decomposition, scheduler ordering, and transfer interpretation jointly shape
   one simulation outcome and can interact. Do not create independently versioned sub-policies
   unless a concrete future need proves that two concerns must evolve independently.

5. **Every released Engine semantics version has a normative specification.** The specification
   enumerates every result-affecting Engine rule in that version, including rules that predate the
   versioning mechanism and were previously implicit in code/tests. For the first version this
   includes at minimum:
   - resource eligibility, selection ranking, queue-depth interpretation, and final tie-breaking;
   - unit-work decomposition/release behavior that affects execution outcome;
   - scheduler/same-time ordering behavior;
   - spatial-transfer destination binding, timing/distance interpretation, capacity reservation,
     zero/same-resource behavior, and arrival/offline behavior;
   - any other Engine-owned rule whose change could alter assignment, event/transition ordering,
     simulated time, terminal execution state, or derived outcome facts for the same explicit
     inputs.

6. **The first normative specification is maintained outside the ADR.** The concrete transfer
   formula and other version-specific rules belong to the `engine-semantics:v1` specification,
   because a later semantics version is allowed to change them without superseding this ADR. The
   ADR freezes the identity/versioning contract; the semantics specification freezes one released
   interpretation.

7. **A semantics version is fixed for one run.** It is established with the runtime and cannot
   change mid-run. A mid-run change would make one event/observation stream internally inconsistent
   and unreproducible.

8. **The initial implementation supports exactly one current Engine semantics version.** The
   runtime reports it; callers do not select among versions until a second supported version makes
   selection/resolution a real capability. A future multi-version resolver is permitted but not
   required now.

9. **A new `EngineSemanticsVersion` does not itself imply a new `RunId`.** They are independent
   dimensions. Starting a new runtime still creates a fresh `RunId`; the semantics version says
   which interpretation that run uses.

10. **Runtime provenance must carry `EngineSemanticsVersion`.** It is mandatory on every supported
    `RuntimeObservationMetadata` and `RuntimeEventEnvelope` alongside `ModelFingerprint`.
    `FactoryRuntime` also exposes its fixed semantics version directly so headless callers can read
    it without first observing or draining events. Optional `ControlledRevisionId` remains present
    only when an authoritative revision binding exists.

11. **This supplements ADR-0011.** No accepted ADR-0011 body text is edited. The new mandatory
    provenance dimension is established by this decision and implemented additively in the current
    runtime observation/event contract.

12. **Released semantics versions are immutable and never reused.** Any intentional change that
    can alter semantic outcome for identical explicit inputs creates a new version, including a bug
    fix when the fix observably changes the interpretation. Performance improvements, dependency
    upgrades, refactors, logging changes, or projection-only changes retain the version only when
    they preserve normative semantic behavior.

13. **Every released version retains behavioral conformance fixtures.** Fixtures map
    representative explicit inputs to expected semantic outcomes and pin facts such as:
    - authoritative transition/event-type ordering;
    - selected/assigned entity references;
    - simulated times;
    - terminal execution state;
    - derived semantic results where they are part of the versioned behavior.

    Fixtures must not accidentally freeze transport serialization, DTO bytes, non-behavioral
    observation fields, UI projection, or other representation details that are not Engine semantic
    identity.

14. **The durability guarantee is attribution plus a verifiable definition, not permanent exact
    re-execution.** For every released version Arcogine retains its identifier, immutable normative
    specification, and conformance fixtures. Exact historical re-execution is available only where
    an implementation exists that supports that version.

15. **An implementation explicitly declares which semantics versions it executes and fails on an
    unsupported version.** It must never silently execute a historical record under different
    semantics. The initial one-version implementation trivially declares one supported version.

16. **Retirement removes executability, not provenance.** A retired version's identifier,
    specification, and fixtures remain durable, so historical results remain attributable and
    interpretable. Retirement procedure and simultaneous multi-version support are deferred until a
    second version exists.

17. **Cross-version result comparison is explicit.** Results under different
    `EngineSemanticsVersion` values must not be silently treated as comparable where the changed
    semantics can affect the compared fact. Compatibility/applicability belongs to the consumer
    (for example Challenge attempt comparison or Governance `EvidenceUse`), not to Engine identity.

## Alternatives considered

### Treat `ModelFingerprint` as sufficient execution provenance

Rejected. `ModelFingerprint` identifies the authored design, not Arcogine's interpretation of it.
The same design may legitimately be executed under different accepted Engine semantics.

### Use application/build version

Rejected. Build identity changes for many semantics-preserving reasons and therefore does not carry
stable semantic meaning. It remains diagnostic provenance only.

### Create separately versioned dispatch, scheduler, decomposition, and transfer policies

Rejected for the first contract. Those concerns interact to produce one simulation outcome; for
example dispatch selects the destination whose Engine-defined distance then determines transfer
completion time. Separate identities would multiply compatibility states without an independent
evolution requirement.

### Create one immutable run-spec identity containing every input

Deferred. Such a convenience artifact may later be useful, but the underlying design identity,
Engine semantics identity, and explicit run inputs must remain independently attributable.

### Guarantee permanent exact execution of every released semantics version

Rejected. Retaining an entire historical simulation implementation against future JVMs, build
systems, and dependencies creates an unbounded maintenance commitment. An immutable specification
plus behavioral fixtures gives durable verification without requiring every old binary to survive.

## Consequences

- `ModelFingerprint` remains a truthful identity for authored Factory design rather than a proxy for
  all possible runtime outcomes.
- Spatial execution can place authored spatial facts in `factory-model:v2` while keeping distance,
  destination binding, reservation, and other Arcogine interpretation rules Engine-owned.
- Runtime observation/event metadata gains one mandatory provenance field under a decision that
  supplements ADR-0011.
- The first Engine semantics release requires a normative `engine-semantics:v1` specification and
  behavioral conformance fixtures before later changes make implicit current behavior unrecoverable.
- Challenge, Governance, Operational analytics, and checkpoint/recovery can retain Engine semantic
  provenance when they consume Engine-produced results without conflating it with their own
  identities.
- No generic cross-domain policy/versioning framework is introduced.

## Charter alignment

The decision strengthens deterministic and explainable execution while preserving Arcogine's
separation between canonical modeled intent, runtime execution, and consumer interpretation. It
makes historical results attributable without turning implementation binaries into permanent
product artifacts or coupling Factory design identity to Engine release mechanics.
