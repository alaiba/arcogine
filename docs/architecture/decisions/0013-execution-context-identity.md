# ADR-0013: Durable operational identity

Status: Proposed — architecture review hold
Date: 2026-09-02

## Context

Operational Execution needs durable attribution that is not reducible to an Engine runtime epoch, semantic model fingerprint, governed revision, actor identity, or external target identity.

The original proposal attempted to solve that problem with three concepts:

```text
ExecutionContextKind = PRODUCTION | STAGING | SIMULATION
ExecutionContextId
ExecutionContext = { id, kind }
```

and permanently bound one ID to one consequence/environment kind.

Subsequent architecture review tested that model against mixed synthetic/physical execution, digital twins, virtual commissioning, software-in-the-loop, hardware-in-the-loop, predictive forks, agentic/multi-user simulations, commissioning, and externally observed or controlled subjects.

Those scenarios invalidate the premise that one global consequence/environment kind is a durable property of an execution. A single Arcogine execution may contain synthetic subjects, externally observed subjects, physically controllable subjects, historical inputs, real actors, and synthetic agents at the same time. Lifecycle state, trust, authority, external consequence, historical processing mode, and relationship to reality vary independently.

The broader reasoning and current constraints are recorded in [Operational Execution and Digital Twin Architecture](../operational-execution-digital-twin.md).

## Architecture-review hold

This ADR remains **Proposed** and must not be accepted or implemented until the unresolved durable-identity question below has a precise lifecycle/equality rule.

In particular, the following parts of the original proposal are **withdrawn from the implementation contract**:

- a global `ExecutionContextKind`;
- the `PRODUCTION / STAGING / SIMULATION` taxonomy;
- immutable `{id, kind}` execution-context values;
- permanent ID-to-kind binding;
- same-ID/different-kind conflict semantics;
- using a global environment/consequence classification as an authorization or safety input.

No replacement enum or simulation/operational binary is introduced by this hold.

## Retained requirements

The review did preserve a narrower identity problem and several constraints that a later revision of this ADR should retain unless new evidence overturns them.

### 1. A durable Arcogine-owned identity is still required

Future durable operational records need to distinguish one independently continuing operational history/partition from another across changes such as:

- process restart;
- runtime replacement;
- storage migration;
- deployment changes;
- installation lifecycle changes;
- changes in external-subject correspondence;
- temporary loss or gain of physical bindings.

A deliberately divergent continuation must not silently share one history identity with its source; if independent continuation is supported, its lineage must remain explicit.

The exact referent of this identity is **not yet decided**. This ADR therefore does not currently define `ExecutionContextId`, `OperationalScopeId`, `ExecutionId`, or another replacement type.

### 2. Neighboring identities remain distinct

Whatever durable identity is accepted later must not be derived from or collapsed into:

```text
RunId
ModelFingerprint
ControlledRevisionId
actor identity
target identity
external subject identity
```

Those values may be correlated by later operational artifacts without becoming substitutes for one another.

### 3. Identity must be established explicitly, not inferred from infrastructure

The future identity must not be inferred from:

- Spring profile;
- hostname;
- environment-variable naming convention;
- deployment/Kubernetes namespace;
- URL or endpoint;
- target identity;
- actor identity;
- presence or absence of `RunId`;
- presence or absence of `ControlledRevisionId`;
- `ModelFingerprint`;
- build, test, replay, or process mode.

Configuration may carry an already-established identity. Configuration location or syntax is not semantic identity or authority.

### 4. Identity semantics do not require a central registry

A durable opaque identity may be independently issued and later re-established without requiring the first implementation to introduce a global registry, alias service, discovery service, lifecycle-administration database, or centralized uniqueness authority.

Whether later capabilities require authoritative registration is a separate decision.

### 5. Raw external observations remain independently provenanced

Incoming external observations must not be forced to invent an Arcogine operational identity, `ControlledRevisionId`, or `ModelFingerprint` merely to be ingested.

An observation can carry its own source identity, external subject identity, source/event time, receipt time, quality/trust metadata, and source-provided context. Later interpretation, subject correspondence, reconciliation, deployment correlation, or Governance evidence use may establish Arcogine-owned relationships when it has authority to do so.

### 6. External representations remain projections

ADR-0012 continues to apply. UUID spelling, JSON fields, environment configuration, OpenAPI, database columns, industrial protocols, or other transport representations do not define the eventual semantic identity contract.

If the accepted identity uses UUIDv4 or another representation, semantic value validity and adapter-level textual/canonicalization rules must remain distinct concerns.

## Unresolved decision

Before this ADR can be rewritten for acceptance, Arcogine must answer:

> **What exactly is the independently continuing operational history/partition that needs durable identity, and what makes two records belong to the same one versus different ones?**

The answer must survive at least these cases without relying on a global simulation/production kind:

1. process restart;
2. active/passive failover;
3. disaster-recovery standby;
4. commissioning-to-production lifecycle change;
5. gaining or losing external telemetry;
6. gaining or losing external control capability;
7. hybrid physical/synthetic subjects;
8. multiple independent interpretations/twins of one physical installation;
9. a snapshot used only for historical inspection;
10. a deliberately divergent synthetic fork.

Only after that equality/lifecycle rule is explicit should this ADR decide the type name, representation, issuance boundary, restart/fork semantics, parsing/canonicalization rules, and module ownership.

## Consequences while the hold is active

- The Operational readiness plan must not schedule implementation of the original `ExecutionContextKind`/`ExecutionContext` proposal.
- No `:operational` module or public/persisted schema should be introduced merely to materialize the withdrawn taxonomy.
- Downstream architecture should model reality, authority, consequence, observation, and operation realization as relationships rather than inferring them from a global kind.
- Work may continue on architecture analysis and requirements that do not depend on the unresolved identity referent.
- When the durable identity question is resolved, revise this Proposed ADR in place before acceptance; no compatibility migration is required because the original proposal was never Accepted or implemented.

## Related decisions

- [ADR-0004: Model identity, revision lineage, and external change control](0004-model-identity-revision-lineage-and-external-change-control.md)
- [ADR-0008: Controlled revision identity and lineage](0008-controlled-revision-identity-and-lineage.md)
- [ADR-0011: Runtime observation and event contract](0011-runtime-observation-and-event-contract.md)
- [ADR-0012: External interchange and serialization boundaries](0012-external-interchange-and-serialization-boundaries.md)
- [ADR-0015: Engine semantics identity and reproducibility](0015-engine-semantics-identity-and-reproducibility.md)
