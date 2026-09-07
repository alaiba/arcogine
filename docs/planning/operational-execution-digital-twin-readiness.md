# Operational Execution and Digital Twin Readiness

> **Status:** Proposed; PLAN-OPS-1 is architecture-blocked pending revision of ADR-0013  
> **Scope:** Establish the semantic and safety boundaries required before Arcogine can connect its shared production semantics to independently existing operational systems  
> **Authority:** Planning only; this document defines readiness dependencies and future implementation sequencing, not current production capability  
> **Related:** [Operational Execution and Digital Twin Architecture](../architecture/operational-execution-digital-twin.md), [ADR-0013: Durable operational identity](../architecture/decisions/0013-execution-context-identity.md), [Product Charter](../product/charter.md), [Architecture Overview](../architecture/overview.md), [Factory Design Architecture](../architecture/factory-design.md), [Governance and Conformance Architecture](../architecture/governance-conformance.md), [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md), [Runtime Observation and Event Delivery](runtime-observation-event-delivery.md), [Governance and Conformance Capability](governance-conformance-capability.md)

## 1. Purpose

Arcogine's executable core is simulation-first. This track applies requirements pressure from future digital-twin and real-system use without creating a parallel operational ontology or prematurely building a production-control platform.

The target is continuity across synthetic, hybrid, and externally grounded realization of the same semantic model:

```text
Published production semantics
        ↓
Governed semantic identity / revision when applicable
        ↓
Subjects, operations, state transitions
        ↓
Synthetic and/or external realization
        ↓
External observations with independent provenance
        ↓
Authoritative subject correspondence
        ↓
Reconciliation
        ↓
Drift / calibration feedback
```

The earlier plan treated a global execution context (`PRODUCTION / STAGING / SIMULATION`) as the first foundation. Architecture review has rejected that premise. Hybrid execution is expected: one modeled system can simultaneously contain synthetic subjects, externally observed subjects, physically controllable subjects, real actors, synthetic agents, and historical inputs.

The track therefore proceeds from relational requirements — identity, correspondence, realization, trust, evidence, consequence, and reconciliation — rather than from one global environment kind.

## 2. Current repository grounding

The relevant landed baseline is:

- Factory Design provides the canonical shared production-system semantics.
- Governance durable fingerprint/revision identity and history are implemented and authoritative.
- Governance semantic change/impact, requirement/assertion, and initial conformance/finding contracts are implemented and should be consumed rather than duplicated.
- Governance evidence-use/authorization capabilities remain outstanding where required.
- Engine runtime observation/event core/headless closure is complete.
- `RunId` identifies one simulation runtime epoch and is not the future durable operational-history identity.
- `EngineSemanticsVersion` identifies result-affecting Engine interpretation and remains Engine-owned.
- Operational Execution remains unimplemented.
- ADR-0013 is Proposed and on architecture-review hold; its former `ExecutionContextKind` contract is not an implementation target.

## 3. Converged architecture constraints

The readiness sequence must preserve these constraints:

1. **One semantic model.** Synthetic and externally grounded execution reuse the same domain semantics rather than forking simulation-only and production-only models.
2. **No global simulation/production taxonomy.** `PRODUCTION / STAGING / SIMULATION` and a replacement `SIMULATION / OPERATIONAL` binary are not durable whole-execution kinds.
3. **Hybrid composition is normal.** Reality relationship, trust, authority, and consequence can differ by subject and relationship inside one execution.
4. **Digital twin is relational.** Twin-ness comes from authoritative relationships between modeled subjects and independently existing subjects, observations, provenance, and reconciliation.
5. **Operations and transitions are not transport concepts.** The same semantic operation can have synthetic or external realization. Requested operation, accepted command, actual transition, observation, and reconciled interpretation remain distinct.
6. **External observations are independent facts.** They retain their own source/subject/time/quality/trust provenance and need not invent Arcogine model, revision, or operational-history identity at ingestion.
7. **Subject correspondence is explicit.** Arcogine must never infer physical/external identity from matching names, endpoints, namespaces, configuration, or connector topology.
8. **Authorization semantics are not inherently operational.** Actor/action/capability semantics should be usable in synthetic execution; verified external identity, trust roots, physical safety, and consequence are additional real-world requirements.
9. **Replay is not a context kind.** Seek/reconstitution, replay, checkpoint/restore, and fork are distinct capabilities.
10. **No new higher-rank execution ID yet.** A concept above `RunId` requires a proven lifecycle/equality rule before introduction.

## 4. Readiness sequence under architecture review

The previous linear sequence remains useful as a list of concerns but is **not currently an implementation queue**:

```text
PLAN-OPS-1  Durable operational identity
PLAN-OPS-2  Actor, trust, authority, and capability boundary
PLAN-OPS-3  External operation / command-result lifecycle
PLAN-OPS-4  Deployment target and deployment-record semantics
PLAN-OPS-5  External observation ingestion and provenance
PLAN-OPS-6  Modeled-versus-observed reconciliation
PLAN-OPS-7  Divergence, drift, and calibration feedback
PLAN-OPS-8  Operational resilience / recovery semantics
PLAN-OPS-9  First live-system adapter proving ground
```

Do not interpret adjacency in this list as proof that every item is a strict prerequisite for the next. Architecture review may redistribute shared semantics across Factory, Engine, Governance, and Operational ownership before implementation begins.

The only immediate critical-path work for this track is resolving the durable identity question in ADR-0013 and reconciling downstream sequencing against the accepted result.

## 5. PLAN-OPS-1 — Durable operational identity

### Goal

Define the identity needed by future durable operational records to distinguish one independently continuing operational history/partition from another without embedding a global reality/consequence kind.

The identity must remain distinct from:

```text
RunId
ModelFingerprint
ControlledRevisionId
actor identity
target identity
external subject identity
```

It must survive ordinary continuity events such as process restart and infrastructure replacement. If Arcogine supports deliberate independent continuation/forking, that divergence must not silently share one history identity.

### Architecture blocker

ADR-0013 must answer this before PLAN-OPS-1 can be implemented:

> What exactly is the independently continuing operational history/partition that needs identity, and what makes two records belong to the same one versus different ones?

The answer must survive at least:

- restart;
- active/passive failover;
- disaster-recovery standby;
- commissioning-to-production lifecycle change;
- gaining or losing telemetry;
- gaining or losing external control capability;
- hybrid physical/synthetic composition;
- several independent interpretations of one physical installation;
- historical inspection without continuation;
- deliberate divergent fork.

Until that rule is explicit:

- do not create `ExecutionContextKind`;
- do not implement `PRODUCTION / STAGING / SIMULATION` context taxonomy;
- do not create a replacement SIMULATION/OPERATIONAL kind;
- do not create `OperationalScopeId`, `ExecutionId`, `RealizationId`, or another renamed identifier merely to unblock code;
- do not create the formerly proposed minimal `:operational` module just to materialize identity types.

### Retained constraints

A future accepted identity should remain opaque, Arcogine-owned, explicitly established, and not inferred from profile/hostname/URL/deployment namespace/actor/target/run/revision/model identity. No central registry is required merely to state durable identity semantics.

Raw external observations remain independent of this identity at ingestion.

## 6. PLAN-OPS-2 — Actor, trust, authority, and capability

### Goal

Establish reusable semantics for who/what may perform which operation on which subject, while adding the stronger trust/assurance requirements needed when external consequence exists.

Synthetic execution may legitimately require humans, autonomous agents, NPCs, adversaries, delegated roles, approvals, protected resources, and forbidden actions. Therefore PLAN-OPS-2 must not assume that actor/action/capability semantics are meaningful only in production operation.

Future real-system use additionally requires:

- claimed versus verified identity;
- peer/source/target authenticity and trust basis;
- credential/secret lifecycle;
- least privilege;
- revocation/expiry or equivalent loss of trust;
- physical safety enforcement;
- fail-safe behavior when verification or integrity fails.

The exact module/domain ownership of the reusable actor/capability contract remains an architecture question. Operational should not manufacture a duplicate generic authorization model merely because it is the first consequential consumer.

## 7. PLAN-OPS-3 — External operation / command-result lifecycle

### Goal

Represent external realization of semantic operations without confusing requests or adapter results with actual state transitions.

The required distinction is:

```text
requested operation
!= accepted command
!= actual transition
!= observation of transition
!= reconciled interpretation
```

A future external operation lifecycle may require stable correlation/identity, actor and authority provenance, target identity/trust, requested/effective values, semantic source provenance, submission/acknowledgement facts, timeout/retry/idempotency rules, partial outcomes, cancellation/compensation where meaningful, and links to resulting evidence/reconciliation.

The semantic operation/transition vocabulary should remain usable across synthetic and external realization where the meaning genuinely matches. Ownership of that shared contract must be decided before PLAN-OPS-3 introduces parallel operation semantics.

## 8. PLAN-OPS-4 — Deployment target and deployment-record semantics

### Goal

Apply governed semantic intent to an external target while preserving the effective representation actually applied.

Governance durable revision/history capability is already available and must be consumed.

A deployment record eventually needs provenance for:

- source model fingerprint and controlled revision when applicable;
- external target identity;
- transformation/mapping/profile identity/version;
- material tool version;
- rendered/applied artifact fingerprint or authoritative external applied reference;
- authorization;
- application acknowledgement;
- verification outcome;
- rollback/compensation reference where meaningful.

Deployment identity is not the unresolved durable operational-history identity, model/revision identity, or target identity.

## 9. PLAN-OPS-5 — External observation ingestion, provenance, and subject correspondence

### Goal

Ingest external facts with independent provenance and establish the authoritative correspondence needed before those facts can be interpreted as evidence about Arcogine semantic subjects.

### Raw observation boundary

A durable raw observation should be able to identify at least:

- observation identity;
- source system/identity;
- source trust/authenticity provenance where applicable;
- namespace-qualified external subject identity;
- observed value/fact and unit/dimension where applicable;
- source event/measurement time;
- ingestion/receipt time;
- quality/confidence metadata;
- genuine command/deployment/run correlation when known;
- raw-source reference where retention policy allows.

Raw ingestion does not require an Arcogine operational-history identity, `ModelFingerprint`, or `ControlledRevisionId`.

### Authoritative external-subject correspondence

Before reconciliation can claim that an observation about an external subject is evidence about an Arcogine semantic subject, the track must provide an explicit correspondence responsibility equivalent to:

```text
external identity namespace + external subject
                    ↕
        authoritative correspondence
                    ↕
          Arcogine semantic subject
```

The future contract must address at least:

- namespace-qualified external subject identity;
- Arcogine semantic subject reference;
- asserting/mapping authority;
- mapping/profile version;
- effective interval;
- historical preservation;
- explicit unknown/unmapped state;
- conflict/replacement/alias semantics where required.

Names, endpoints, configuration keys, namespace placement, and connector topology never prove identity.

Subject correspondence answers which subjects correspond. It does not itself say how observations arrive or how operations are realized. Do not model it as `direction = observe | actuate`.

Raw ingestion can exist before a subject is mapped, but reconciliation cannot assert modeled-versus-observed correspondence without an authoritative subject binding.

## 10. PLAN-OPS-6 — Modeled-versus-observed reconciliation

### Goal

Produce historically attributable Arcogine interpretations of independently observed reality without overwriting modeled intent or pretending prediction is observation.

PLAN-OPS-6 depends locally on:

- PLAN-OPS-5 raw observation provenance; and
- the authoritative subject-correspondence contract needed to state what an external observation is about in Arcogine terms.

Reconciliation must eventually account for:

- selected model fingerprint/controlled revision where applicable;
- observations considered;
- correspondence assertions used;
- source authority/trust decisions;
- freshness and temporal alignment;
- reconciliation policy/version;
- pending external operations/deployments;
- match/pending/stale/missing/conflict/divergence/unknown semantics;
- reproducibility/attribution of the interpretation.

Modeled intent, external reality, observation, reconciled interpretation, and simulated/predicted continuation remain distinct.

Late and corrected evidence creates requirements pressure for explicit valid/effective time versus knowledge/recorded time. Do not select a bitemporal persistence technology until the concrete history/query requirements justify it.

## 11. PLAN-OPS-7 — Divergence, drift, and calibration feedback

### Goal

Turn modeled-versus-observed differences into governed improvement without directly mutating published semantics.

Operational drift analysis should consume Governance-owned semantic `ChangeSet`/impact, requirement/assertion, conformance/finding, and later evidence-use contracts rather than duplicate them.

The feedback path remains:

```text
modeled expectation + reconciled operational behavior
        ↓
drift/discrepancy analysis
        ↓
candidate calibration or semantic change
        ↓
validation / simulation / conformance
        ↓
controlled revision
        ↓
optional deployment
```

## 12. PLAN-OPS-8 — Operational resilience / recovery

### Goal

Define failure, idempotency, retry, recovery, trust-loss, and ambiguity semantics for whichever durable command/observation/correspondence contracts have actually been accepted.

Do not design resilience against the withdrawn execution-context taxonomy.

At minimum future consequential paths must preserve:

- explicit idempotency/retry rules;
- ambiguous outcomes as ambiguous;
- fail-safe handling of unverifiable actors/sources/targets;
- external rejection/partial failure visibility;
- distinction between logical rollback and physical reversibility;
- observation loss as unknown/stale rather than implicit success;
- restart/recovery semantics consistent with the accepted durable operational identity.

## 13. PLAN-OPS-9 — First live-system adapter proving ground

### Goal

Use one narrow real-system integration to prove the accepted semantic boundaries rather than letting a protocol define Arcogine's ontology.

The proving case must demonstrate, as applicable:

- external subject identity kept distinct from Arcogine subject identity;
- authoritative subject correspondence;
- observation mapping/provenance;
- operation-realization mapping separately from subject correspondence;
- verified actor/source/target trust where consequence requires it;
- command/request/result separated from observed/reconciled reality;
- model/revision and transformation provenance;
- failure/idempotency/recovery behavior;
- no dependence on a global simulation/staging/production kind.

A protocol test server can prove adapter mechanics but does not by itself close the full track.

## 14. Replay, seek, checkpoint, and fork

These are intentionally not part of PLAN-OPS-1 context classification.

The architecture distinguishes:

- **seek/reconstitution** — obtain state at historical time;
- **replay** — reconstruct/derive by consuming retained historical inputs or trace;
- **checkpoint/restore** — resume runtime state;
- **fork** — create an independently evolving continuation from selected historical state.

The current Engine supported-event boundary does not promise unbounded retained history, and `EngineSemanticsVersion` does not promise permanent exact execution support for every historical version.

Do not introduce a higher-rank execution identity until checkpoint/recovery or another concrete capability proves the lifecycle rule it would identify.

## 15. Cross-track ownership cautions

### Factory Design / Engine

Factory/Engine remains authoritative for canonical production semantics and deterministic simulation runtime behavior. Operational requirements may reveal a reusable semantic operation/transition contract, but Operational must not create a duplicate merely to reach external systems.

`EngineSemanticsVersion` remains Engine-owned interpretation provenance, including future Engine-driven prediction or virtual-commissioning simulation where applicable.

### Governance

Operational owns acquisition/provenance of external facts and reconciliation. Governance owns durable semantic revision/history, semantic change, requirements/assertions, conformance/findings, evidence use, exceptions, and governed change.

External facts may become Governance evidence through explicit evidence-use relationships without changing their operational provenance.

## 16. Fixture rules

1. A fixture may stand in for a genuinely outstanding sibling-owned input; it does not define that sibling contract.
2. Implemented Governance fingerprint/revision, semantic change/impact, requirement/assertion, and conformance/finding contracts must be consumed rather than recreated as synthetic production types.
3. Synthetic evidence-use/authorization fixtures remain acceptable only while those Governance capabilities are genuinely outstanding and do not count as sibling completion.
4. Synthetic operational adapters do not satisfy Engine distribution-hardening requirements.
5. No fixture may reintroduce the withdrawn global execution-context taxonomy as if it were an accepted contract.

## 17. Immediate next action

Do **not** implement PLAN-OPS-1 yet.

The next architecture step is:

1. Resolve the exact referent and lifecycle/equality semantics of the durable operational-history identity.
2. Revise ADR-0013 in place while it remains Proposed.
3. Reconcile this readiness plan and the Operational architecture against the revised ADR.
4. Only then identify the narrowest safe implementation slice.

Other open architecture questions — shared operation/transition ownership, exact correspondence cardinality, and temporal twin history — should be investigated only where necessary to resolve or safely sequence the accepted contracts, not as open-ended framework design.

## 18. Track exit condition

Operational Execution / Digital Twin readiness is not complete until at least one proving path demonstrates that Arcogine can connect its semantic model to independently existing operational subjects without collapsing identities or truth sources.

A mature exit condition includes:

- accepted durable operational-history identity semantics;
- actor/authority/trust semantics appropriate to consequential use;
- external operation/command lifecycle separated from actual transitions and observations;
- deployment/applied-artifact provenance;
- raw external observations with independent source/subject/time provenance;
- authoritative external-subject ↔ Arcogine-subject correspondence;
- reconciliation that preserves modeled, observed, reconciled, and predicted distinctions;
- drift/calibration feeding governed change rather than direct model mutation;
- resilience/idempotency/recovery semantics;
- a live-adapter proving case that demonstrates subject correspondence and operation realization as separate responsibilities;
- no dependency on a global `PRODUCTION / STAGING / SIMULATION` execution kind.
