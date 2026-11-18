# Enduring architectural principles and domain authority

Part of the [architecture overview](../overview.md#routed-topics); it is mandatory whenever the overview routes your change here.

## Enduring architectural principles

These are expected to hold regardless of how the implementation evolves, because they follow directly from the Product Charter:

1. Repository must be reproducible, modular, testable, and collaboration-ready.
2. Deterministic acceptance tests and scenario-level validation are mandatory for simulation, replay, and verification contexts (see the [Determinism Contract](determinism-contract.md#determinism-contract) for scope).
3. Agents only use approved command interfaces and never mutate simulation state directly — this is the same governance boundary the Charter asks of human and autonomous decision-makers alike.

### Domain authority

Each kind of fact has exactly one authoritative owner, and exposing a fact never transfers its
ownership:

- **Factory** owns authored production/design facts: the canonical model, its validation,
  publication and content identity ([Factory Design](../factory-design.md)).
- **Engine** owns deterministic simulation interpretation and authoritative runtime state: dispatch,
  decomposition, scheduling and derived-result rules under one identified interpretation
  ([Engine semantics](../engine-semantics.md), [Determinism Contract](determinism-contract.md#determinism-contract)).
- **Governance** owns controlled history, requirements/assertions, evidence use, conformance
  findings and governed change ([Governance and Conformance](../governance-conformance.md)).
- **Operational** would own real-operation integration, accountable continuation and the
  relationship to external authority; none of it is implemented ([Operational
  execution](operational-execution.md#operational-execution--not-implemented)).
- Observations, events, projections, DTOs, serializations and outward views expose these facts
  without becoming domain truth, and consumer domains (Challenge/Game, analytics, UI) never
  redefine authoritative production semantics.

**Analytical-definition ownership and admission.** Engine/Factory retain authoritative runtime
state, result-affecting interpretation, supported observations/events, and the exact arithmetic
of existing Engine-declared derived results; changing such a definition is an Engine
specification/support change. A named analytical method instead has choices execution does not
fix: population, selected interval, denominator/capacity, availability treatment, grouping,
attribution, interpretation or method-specific diagnostic judgment. The capability fixing those
choices owns the method's meaning; a single consumer's method is consumer-local by default.

A **shared analytical-definition owner** is justified only by concrete admission evidence:
(1) two actual consumers need the same named meaning; (2) one consumer and a core verification
or Governance requirement with observable acceptance evidence need that meaning; (3) an admitted
outward contract requires shared semantics; or (4) divergent duplicated computations need one
agreed definition. Two call sites, co-located capture, fixture-only verification and illustrative
objectives do not by themselves establish a shared owner. Definition ownership does not transfer
physical evidence custody or create a definition registry: the [runtime
contract](../runtime-contract.md#captured-execution-evidence-and-interval-determinacy) owns the
controller/holder split, claim-scoped coverage, proof frontiers and interval finality.

An **Engine-produced measurement** requires a separate demonstrated producer responsibility:
execution/result-affecting behavior, a concrete Engine-wide contractual meaning, or a justified
execution-fixed run-to-date aggregate where the holder cannot capture from establishment. Compare
the latter with bounded retention/shared delivery and compatibility cost; it can relieve retention,
not establish arbitrary interval finality. No new measurement or shared analytics implementation
is admitted now. Analytical methods interpret published outcomes; they do not reconstruct hidden
scheduler choices or step starts, infer future input, or turn raw occupancy into authoritative
causal bottleneck truth. Controlled counterfactuals use Engine re-execution with explicit inputs.
