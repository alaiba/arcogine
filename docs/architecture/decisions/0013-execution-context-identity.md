# ADR-0013: Execution context identity

Status: Proposed
Date: 2026-09-02

## Context

Operational Execution must establish execution-context identity before Arcogine makes that identity public, persists it in operational records, or uses it as an input to later authorization, deployment, command, reconciliation, or evidence relationships.

The architectural requirement is deliberately two-dimensional:

```text
ExecutionContextKind
    what consequence/environment semantics apply?

ExecutionContextId
    which concrete Arcogine operational context is this?

ExecutionContext
    immutable binding of kind + concrete identity
```

The core invariant is:

> **Classification is not identity.**

`PRODUCTION` can identify a class of operational consequence, but it cannot identify one unique production environment. Arcogine must be able to represent several independent contexts of the same kind at the same time and retain their history without conflating them.

Several neighboring identities are already established or reserved for different responsibilities:

```text
ModelFingerprint
    exact semantic content

ControlledRevisionId
    governed historical occurrence

RunId
    one simulation runtime epoch

ExecutionContextKind
    operational consequence/environment classification

ExecutionContextId
    one concrete Arcogine operational context

target identity
    external system/resource acted upon

actor identity
    who/what requests or performs an action
```

The execution-context identity contract must preserve these non-equivalences:

```text
ExecutionContextId != RunId
ExecutionContextId != ControlledRevisionId
ExecutionContextId != ModelFingerprint
ExecutionContextId != target identity
ExecutionContextId != actor identity
```

Those values may later be correlated by commands, deployments, reconciliation records, runtime associations, or evidence relationships. Correlation must never collapse the identities.

ADR-0011 makes `RunId` an opaque identity for one fresh simulation runtime epoch; reset creates another run identity. ADR-0004 and ADR-0008 separate semantic model identity from controlled historical revision identity. ADR-0012 makes Arcogine semantic contracts authoritative over their external projections. Execution-context identity must build on those boundaries rather than reinterpret them.

Raw external observations impose a further constraint. An incoming observation may know only source identity, source time, receipt time, and source-provided environment metadata. The execution-context contract must not force the source to invent an Arcogine context, controlled revision, or model fingerprint merely to be ingested later by the external-observation capability.

The first implementation also does not justify centralized context issuance, a context registry, lifecycle administration, aliases, discovery, or a generic operational persistence system. The decision therefore has to distinguish durable identity semantics from durable registry state.

## Decision

The following is the proposed execution-context identity contract. It becomes intended architecture only if this ADR is Accepted.

### 1. Operational Execution owns three separate semantic concepts

Operational Execution will introduce three concepts:

```text
ExecutionContextKind
ExecutionContextId
ExecutionContext
```

`ExecutionContextKind` classifies operational consequence/environment semantics. `ExecutionContextId` identifies one concrete Arcogine operational context. `ExecutionContext` is the immutable value that binds exactly one ID to exactly one kind.

The first implementation should introduce a dedicated `:operational` module rather than place these concepts in `:simulation`, `:governance`, `:factory`, or an interface/DTO module. The proposed dependency direction is:

```text
:types
   ↑
:operational
   ↑
future operational adapters / projections
```

`:operational` must not depend on `:simulation`, `:governance`, or `:factory` merely to define execution-context identity. In particular, it must not add conversion constructors or derived-ID helpers from `RunId`, `ControlledRevisionId`, or `ModelFingerprint`.

### 2. The initial `ExecutionContextKind` taxonomy is consequence-oriented

The initial semantic taxonomy is exactly:

```text
PRODUCTION
STAGING
SIMULATION
```

Their meanings are:

- `PRODUCTION` — activity is interpreted under real production-consequence semantics. The kind does not name a target, plant, tenant, cluster, namespace, or one globally unique production environment.
- `STAGING` — production-like operational integration is exercised without production consequence. It is distinct because later authority or policy may legitimately treat staging differently from production even when the same production semantics or target classes are exercised.
- `SIMULATION` — an Operational Execution artifact needs to classify or correlate activity as simulated. A simulation context can outlive or correlate multiple simulation runtime epochs; it never replaces the Engine `RunId` for one runtime epoch.

`REPLAY` is not an initial context kind. Replay is primarily a processing/history-interpretation mode: the same operational context can be examined or reconstructed through replay without thereby becoming a different consequence class. If a later concrete authority/consequence invariant proves that replay cannot be modeled separately while preserving the Product Charter's required distinction, that requires an explicit taxonomy decision rather than silently adding process modes to the execution-context taxonomy.

Generic `TEST` is also not a context kind. A test runner, build mode, Spring profile, test deployment, or fixture is a software/process concern. Tests that need execution-context semantics create explicit production/staging/simulation fixtures as appropriate rather than making the test mechanism itself an operational ontology.

The taxonomy has no `UNKNOWN`, `OTHER`, or free-form extension member. At the semantic boundary, an unsupported kind fails explicitly. A versioned external adapter may preserve an unknown external token as adapter-specific uninterpreted data when its own compatibility contract requires that behavior, but it must not manufacture an `ExecutionContext` until the token maps to a supported semantic kind.

### 3. `ExecutionContextId` is an opaque RFC 9562 UUID version 4

`ExecutionContextId` is a type-safe wrapper around an RFC 9562 UUID version 4.

UUIDv4 is chosen because it is standardized, cross-language, independently generatable, and intentionally carries no kind, target, model, revision, actor, namespace, URL, clock, or ordering semantics. UUID ordering has no execution-context domain meaning.

The canonical textual representation is the conventional 36-character lowercase hyphenated UUID form:

```text
xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx
```

where hexadecimal letters are lowercase and `y` represents the RFC UUID variant bits appropriate to UUIDv4.

The identifier is opaque. Human-facing labels, descriptions, deployment names, DNS names, namespaces, API URLs, and similar metadata must not be encoded into it or recovered from it.

Although `ControlledRevisionId` also uses UUIDv4, the two IDs are different semantic types. Shared representation does not permit assignment, conversion, comparison-by-string-as-a-domain-shortcut, or reuse of one as the other.

### 4. Identity meaning is Arcogine-owned; issuance is decentralized initially

The execution-context contract distinguishes **identity semantics** from **identity issuance infrastructure**.

Arcogine owns what an `ExecutionContextId` means, but this decision does not require every ID to be allocated by one central Arcogine service. A new UUIDv4 may be generated once by Arcogine itself or by an operator/deployment system that is establishing a concrete Arcogine context. An already-established UUIDv4 may likewise be supplied through configuration or deployment tooling.

In every case, operational code receives an `ExecutionContext` only through an explicit parse/validation/resolution boundary. Supplying a value is not equivalent to deriving its identity from the configuration mechanism that carried it.

Conceptually:

```text
external/configured context representation
              |
              v
execution-context parse / validation / resolution boundary
              |
              v
immutable ExecutionContext
              |
              v
downstream operational semantics
```

An environment variable, configuration file, secret/config map, deployment manifest, or operator input may carry the already-established ID and kind. None of those mechanisms is itself the semantic identity or semantic authority.

This decision does not decide which actor is authorized to create, configure, or use a context. Actor authentication, trust, capability, and authorization belong to the operational identity/trust/authority capability.

### 5. Context must be explicit and must not be inferred

Downstream operational code may rely on an `ExecutionContext` only after the execution-context boundary has established it explicitly.

The boundary must not infer an execution context from:

- Spring profile;
- process hostname;
- environment-variable name or value semantics alone;
- Kubernetes or other deployment namespace;
- URL or endpoint;
- target identity;
- actor identity;
- presence or absence of a `RunId`;
- presence or absence of a `ControlledRevisionId`;
- `ModelFingerprint`;
- build, test, replay, or process mode.

There is no implicit default context. Failure to establish a valid context is explicit failure rather than fallback to `PRODUCTION`, `STAGING`, `SIMULATION`, or a guessed non-production value.

### 6. `ExecutionContext` contains only ID and kind

The immutable execution-context value contains exactly:

```text
ExecutionContext
    id: ExecutionContextId
    kind: ExecutionContextKind
```

It does not contain target identity, deployment state, `ModelFingerprint`, `ControlledRevisionId`, `RunId`, actor identity, permissions, authorization, hostname, URL, protocol, namespace, transport configuration, or mutable presentation metadata.

Human-facing labels/descriptions may be introduced later as metadata associated with a context. They must not define identity equality or change the identity when renamed.

### 7. One `ExecutionContextId` is permanently bound to one kind

An `ExecutionContextId` is permanently associated with exactly one `ExecutionContextKind`.

The same ID must not move from `STAGING` to `PRODUCTION`, from `SIMULATION` to `STAGING`, or between any other consequence classes. A consequence-class change establishes a new context and therefore a new `ExecutionContextId`.

For example, the same external target may legitimately be associated over time with:

```text
staging context S1
later production context P1
```

without asserting:

```text
S1 == P1
```

Promotion of a deployment, target, namespace, or installation is therefore distinct from mutation of operational-context identity. This preserves the meaning of historical commands, observations, deployments, reconciliation records, and future authorization decisions.

A persistent registry is not required merely to state this invariant. The establishment boundary validates each supplied pair and produces an immutable binding. Any scope that already contains or correlates an established binding for an ID — for example a configured context set, reconstructed persisted records, or a later authoritative store — must reject a second pair that reuses that ID with a different kind. An isolated establishment boundary cannot prove that no conflicting pair exists in some unrelated process with which it has never exchanged state; that limitation is a consequence of deliberately not requiring a registry, not permission to reinterpret the ID when conflicting values later meet.

### 8. Semantic context comparison is checked, not ordinary pair inequality

`ExecutionContextId` equality is concrete context identity equality.

Two distinct IDs are distinct contexts even when both have the same kind:

```text
context A: id = A, kind = PRODUCTION
context B: id = B, kind = PRODUCTION

A != B
A.kind == B.kind
```

Comparing two established `ExecutionContext` values has three semantic outcomes:

```text
different IDs
    => DISTINCT contexts

same ID + same kind
    => SAME context

same ID + different kind
    => BINDING CONFLICT; fail explicitly
```

The third case is not ordinary inequality. It means at least one input violates the permanent ID-to-kind binding and must be treated as invalid/corrupt configuration or persisted data.

This checked relation is the execution-context semantic comparison contract. A language-level boolean `equals` method cannot represent the conflict outcome, so ordinary structural/value equality — if the Java implementation provides it — is not authoritative for deciding whether two operational contexts are semantically distinct. Operational maps, indexes, reconstruction code, and correlation boundaries that reason by context identity must key/compare by `ExecutionContextId` first and validate that any repeated ID carries the same bound kind before continuing.

Labels and other presentation metadata never participate in semantic identity or checked comparison.

### 9. Parsing and validation are strict and deterministic

When the UUID textual form is accepted at an execution-context semantic/configuration boundary:

- null and blank input are invalid;
- the input must be exactly the canonical lowercase 36-character hyphenated UUID spelling;
- the parsed value must be UUID version 4 with the RFC variant required for UUIDv4;
- uppercase UUIDs, non-hyphenated forms, braced forms, whitespace-padded values, truncated/expanded components, and other non-canonical equivalent spellings are rejected rather than normalized silently;
- malformed input fails explicitly and never produces a default/generated replacement ID.

If a boundary accepts textual kind names directly, the canonical semantic spellings are exactly `PRODUCTION`, `STAGING`, and `SIMULATION`. Null, blank, case variants, and unknown values fail explicitly unless a versioned adapter intentionally maps its own external representation to one of those semantic values before entering the execution-context boundary.

The conceptual contract is deterministic validation failure, not a particular Java exception type or third-party parser exception. An implementation may use standard UUID parsing internally, but library-specific exception behavior is not part of the semantic contract.

### 10. Restart preserves context identity without requiring registry persistence

Re-establishing the same durable `ExecutionContextId` with its permanently bound `ExecutionContextKind` after process restart represents the same concrete operational context.

Therefore execution-context identity survives process lifetime even though this decision does not require Arcogine to persist a registry of contexts.

The initial contract specifically does not require:

- a context-registry database;
- issuance-history persistence;
- lookup/discovery service;
- alias registry;
- retirement/lifecycle administration;
- metadata history;
- a centralized uniqueness service.

A deployment may preserve its established UUIDv4 and kind in configuration and provide the same pair after restart. Later commands, deployments, reconciliation records, or persisted projections may store the ID directly and retain its semantic meaning.

If a future capability requires authoritative durable registration, aliases, retirement, discovery, metadata history, or cross-installation administration, that is a later architecture decision. Governance `FileControlledRevisionAuthority` remains revision-specific and must not be reused as a generic context registry.

### 11. Public and persisted representations are projections of the execution-context contract

ADR-0012 applies directly:

```text
execution-context semantic contract
        |
        v
versioned adapter / projection
        |
        v
JSON / OpenAPI / environment configuration / protocol representation
```

The semantic `ExecutionContextId` remains UUIDv4 and the semantic kind remains one of the supported kinds regardless of how a versioned adapter names fields or packages the values.

HTTP, OpenAPI, JSON, MQTT, OPC UA, CloudEvents, Kubernetes, environment configuration, database columns, or serializer defaults do not define execution-context identity.

A stable public or persisted projection must define its own schema/profile version, field mapping, validation behavior, compatibility expectations, and fixtures as required by ADR-0012. This ADR does not design any of those transport schemas.

### 12. Future taxonomy and identity changes require explicit compatibility handling

Future evolution distinguishes several different changes:

- **Adding a new semantic `ExecutionContextKind`** is a semantic taxonomy expansion. It requires explicit review of persisted and public consumers; enum additions are not assumed to be backward compatible in every Java, database, schema, client, or protocol boundary.
- **Changing the meaning of an existing kind** is strongly discouraged. Materially different consequence/authority semantics should receive a new semantic kind rather than silently redefining historical `PRODUCTION`, `STAGING`, or `SIMULATION` records.
- **Renaming a wire token or field** is an adapter/projection change. It requires versioned compatibility or migration at that boundary and does not by itself rename or reinterpret the semantic kind.
- **Changing the identifier representation** away from UUIDv4 is an identity-contract migration, not a serializer refactor. Existing IDs must not be re-derived from kind, target, actor, model, revision, URL, namespace, or other metadata to make such a migration convenient.

Unknown future kinds continue to fail at the semantic boundary until support is deliberately added. Versioned adapters may preserve unknown external data without pretending it is a known execution context.

### 13. Raw external observations remain independently provenanced

The execution-context contract does not require incoming external observations to carry an Arcogine `ExecutionContextId`.

A raw observation may legitimately contain only facts such as:

```text
source identity
source time
receipt time
source-provided environment metadata
```

The observation must not invent an `ExecutionContextId`, `ControlledRevisionId`, or `ModelFingerprint` to satisfy the execution-context contract.

If Arcogine later interprets an observation in a concrete context, binds it to modeled intent, correlates it with a deployment/command, or uses it as Governance evidence, that relationship belongs to the later interpretation/reconciliation/evidence-use record that has authority to make the association.

## Alternatives considered

### One enum value per process mode: `SIMULATION / REPLAY / TEST / STAGING / PRODUCTION`

Rejected for the initial execution-context taxonomy. It mixes operational consequence/environment classes with processing and software-execution modes. Replay can be performed against data associated with different contexts without changing the context's consequence identity, and generic test execution says nothing reliable about operational authority. Keeping the initial taxonomy at `PRODUCTION / STAGING / SIMULATION` avoids freezing process mechanics into a public semantic enum.

### Use `ExecutionContextKind` itself as identity

For example, treating `ExecutionContext = PRODUCTION` would be simple but would make every production context equal to every other production context. Arcogine needs multiple independent production and staging contexts, so classification cannot serve as concrete identity.

### Reuse Engine `RunId`

Rejected because `RunId` identifies one simulation runtime epoch and changes when a fresh runtime/reset is created. An operational context is durable across process/runtime epochs and may correlate multiple runs. Reuse would also make production/staging identity incorrectly depend on simulation machinery.

### Reuse `ControlledRevisionId` or `ModelFingerprint`

Rejected because both answer different questions. `ControlledRevisionId` identifies one governed historical occurrence; `ModelFingerprint` identifies exact semantic content. Different contexts can use the same revision/model, and one context can legitimately see different revisions/models over time. Reuse would collapse configuration provenance into operational environment identity.

### Use target identity as context identity

Rejected because target and context have different lifecycle and meaning. The same external target may move from staging use to production-consequential use, while a context can also exist independently of one target or span relationships to several target resources. Target promotion must not rewrite historical context identity.

### Use actor identity as context identity

Rejected because the requester/performer and the operational environment are independent dimensions. Many actors can operate in one context, and one actor can act across several contexts subject to later operational authority rules.

### Use a human-readable identifier or unrestricted operator-defined key

A name such as `plant-1-prod` is convenient for operators and logs, but it creates rename, case/normalization, namespace, collision, portability, and long-term compatibility problems. Human labels may exist as metadata; they do not justify making mutable organizational naming the durable identity.

An unrestricted operator-defined string was rejected for the same reason and because it would force the execution-context contract to define escaping, normalization, length, namespace, and uniqueness semantics that UUIDv4 avoids.

### Use a composite or derived identifier from kind, target, namespace, URL, model, or revision metadata

Rejected because every candidate component can change independently of concrete context identity, and several are explicitly separate identities. A derived ID would make renaming, target migration, model revision, or environment reconfiguration look like context replacement even when it is not, while also preventing two contexts with otherwise similar metadata from remaining distinct.

### Use a time-ordered identifier such as UUIDv7 or ULID

Rejected because creation time and sort order have no domain meaning for context identity. Storage locality does not justify embedding time into a durable semantic identifier. Physical storage may add its own indexes later.

### Require a durable authoritative context registry in the initial contract

Rejected because the first required contract is stable identity, explicit classification, and a permanent ID-to-kind semantic binding. Deployment/configuration can preserve an established UUIDv4 across restart without a registry. Registration, discovery, aliases, retirement, metadata history, and centralized administration are different lifecycle/persistence responsibilities and should be introduced only when a concrete operational requirement needs them.

## Consequences

If Accepted, this decision will provide the first execution-context implementation with a small, durable identity contract that later operational artifacts can reference without reopening the meaning of context identity.

Positive consequences include:

- multiple `PRODUCTION`, `STAGING`, or `SIMULATION` contexts can coexist without collision by classification;
- the same context can be re-established after restart without making process lifetime or `RunId` its identity;
- promotion from staging consequence to production consequence leaves historical staging identity intact and creates a distinct production context;
- later authorization can reason over both consequence class and concrete context without deriving either from deployment heuristics;
- persisted/public adapters can carry a standardized opaque identifier while remaining downstream of the semantic model;
- simulation, Governance, model, target, actor, and external-observation identities remain independently interpretable;
- the first implementation can be built without a persistence service or centralized registry.

Costs and constraints include:

- operators need a separate human-facing label mechanism if UUIDs alone are inconvenient for display;
- configuration/deployment processes must preserve the established UUIDv4 and kind across restart rather than regenerate IDs accidentally;
- without a registry, the establishment boundary cannot globally detect conflicting bindings that never enter the same process or authoritative store; whenever values with the same ID do meet, checked context comparison/binding validation must reject a different kind rather than treating it as another context;
- strict canonical parsing rejects convenient but non-canonical UUID/kind spellings instead of normalizing them;
- new context kinds and any future identifier-representation change require deliberate compatibility work rather than being treated as harmless enum/serializer edits.

The first implementation remains limited to the minimum semantic values and explicit establishment boundary. It does not implement later operational identity/trust, command, deployment, observation, reconciliation, or recovery behavior.

## Non-goals

This ADR does not define or implement:

- actor identity, authentication, authorization, trust, certificates, secrets, or capability policy;
- command identity/lifecycle, idempotency, retries, or acknowledgement semantics;
- deployment records, target registration, applied-artifact provenance, promotion workflow, or rollback;
- external-observation schemas, reconciliation, drift, calibration, or Governance evidence use;
- adapter protocols or HTTP/OpenAPI/MQTT/OPC UA/CloudEvents/Kubernetes schemas;
- context discovery, aliases, retirement, mutable metadata history, or lifecycle administration;
- a durable context registry or generic operational persistence service;
- a general environment/configuration framework;
- changes to `FactoryRuntime`, `RunId`, Governance identity types, or factory/model identity;
- a rule that raw external observations must identify an Arcogine context/model/revision.

## Charter alignment

This proposal supports the Product Charter's continuity, provenance, and operational-trust direction by making consequence explicit without conflating simulation/runtime provenance, governed model history, external targets, actors, or transport configuration. It preserves one semantic production model across lifecycle contexts while keeping real operational consequence identifiable as its own durable dimension.
