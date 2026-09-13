# Operational Execution and Digital-Twin Boundary Research

> **Status:** READY, with durable operational identity on the critical path  
> **Scope:** Resolve the semantic and safety boundaries required before Arcogine admits implementation that connects shared production semantics to independently existing operational systems  
> **Authority:** Research only; this document defines no implementation queue or Operational module contract

## Purpose

Arcogine's executable core is simulation-first. Future operational and digital-twin use creates requirements pressure around independently existing subjects, real consequence, trust, external observations, and reconciliation. Those concerns must be resolved without creating a second production ontology or a global simulation/production mode.

The target conceptual continuity is:

```text
Published production semantics
        |
        v
Governed semantic identity / revision when applicable
        |
        v
Subjects, operations, and state transitions
        |
        v
Synthetic and/or external realization
        |
        v
External observations with independent provenance
        |
        v
Authoritative subject correspondence
        |
        v
Reconciliation
        |
        v
Drift / calibration feedback
```

## Established constraints

Research must preserve these already-established boundaries:

1. **One semantic model.** Synthetic and externally grounded realization reuse shared domain semantics.
2. **No global reality kind.** Production, staging, simulation, historical, replay, or consequence are not mutually exclusive whole-execution classifications.
3. **Hybrid composition is normal.** Reality relationship, trust, authority, and consequence may differ by subject/relationship in one modeled system.
4. **Digital twin is relational.** Twin-ness comes from authoritative correspondence, observations, provenance, and reconciliation.
5. **Operation, realization, transition, observation, and reconciliation are distinct facts.** Transport acknowledgements do not prove physical state change.
6. **Raw external observations are independent facts.** They keep source/subject/time/quality/trust provenance and need not carry Arcogine model/revision/history identity at ingestion.
7. **Subject correspondence is explicit.** Names, endpoints, namespaces, connector topology, and configuration keys never prove identity.
8. **Generic actor/capability semantics are not inherently Operational.** Consequential use adds verified identity, trust, safety, and authority requirements.
9. **Seek, replay, checkpoint/restore, and fork are distinct capabilities.** None is an execution kind.

## Durable operational identity — READY / critical path

ADR-0013 must resolve:

> What independently continuing operational history or partition, if any, needs durable identity, and what makes two records belong to the same one versus different ones?

The candidate identity must be tested against restart, failover, disaster recovery, lifecycle changes, changing telemetry/control availability, hybrid composition, several independent twins/interpretations of one physical installation, historical inspection, stale restore, split brain, and deliberate divergent fork.

It must remain distinct from runtime/run identity, model fingerprint, controlled revision, actor, target, deployment, and external subject identity.

Do not introduce a renamed operational identifier until referent, equality, continuity, and divergence rules are explicit.

## Actor, trust, authority, and capability — CANDIDATE

Research the minimum shared semantics for who/what may perform which operation on which subject, while distinguishing generic participation from consequential operational assurance.

Test:

- claimed versus verified identity;
- delegation and accountable actor/principal;
- capability versus authorization/policy;
- source/peer/target authenticity;
- least privilege and loss/revocation of authority;
- physical-safety/fail-safe requirements;
- interaction with the Agency and Decision Boundary research.

The output must settle ownership so Operational does not duplicate a generic authorization model.

## External operation and command-result lifecycle — CANDIDATE

Determine the smallest durable semantics needed to preserve:

```text
requested semantic operation
!= submitted/accepted external command
!= actual transition
!= observation of transition
!= reconciled interpretation
```

Investigate correlation, idempotency, retry, timeout, partial outcome, cancellation/compensation, actor/authority provenance, target trust, requested/effective value, and relationship to evidence/reconciliation.

## Deployment semantics — CANDIDATE

Determine what a deployment/application record must prove about:

- source model/revision;
- external target identity;
- transformation/mapping/profile version;
- material tool version;
- effective rendered/applied artifact;
- authorization;
- application acknowledgement;
- verification and rollback/compensation.

Deployment identity must not be conflated with model, revision, target, or durable operational-history identity.

## External observation and subject correspondence — CANDIDATE

Research a raw-observation boundary that preserves independent provenance and an explicit correspondence responsibility between namespace-qualified external subjects and Arcogine semantic subjects.

Correspondence research must cover asserting authority, mapping/profile version, effective interval, historical preservation, unknown/unmapped state, and conflict/replacement/alias semantics where required.

Raw ingestion may precede mapping; reconciliation may not claim subject equivalence without an authoritative binding.

## Reconciliation and temporal truth — CANDIDATE

Determine how Arcogine produces historically attributable interpretations of observed reality while keeping modeled intent, observation, reconciled state, and predicted continuation distinct.

Research needs include observation selection, correspondence used, trust decisions, freshness/temporal alignment, policy/version, pending external operations, and explicit match/pending/stale/missing/conflict/divergence/unknown states.

Late/corrected evidence may justify valid/effective-time versus knowledge/recorded-time semantics, but no persistence technology should be selected before the required queries/history are concrete.

## Manufacturing traceability proving pressure

Manufacturing traceability is a cross-cutting proving case for the Operational boundaries above, not a separate Operational ontology. The material identity/genealogy question itself is owned by the [Manufacturing Traceability Research](manufacturing-traceability.md); Operational research must not invent lot, batch, serial, or material identity merely to make a trace query convenient.

The command/result, deployment, observation/correspondence, and reconciliation candidates should be able to explain a production-history query without collapsing their distinctions. In particular, test histories where:

- a production operation is requested, an external command is accepted, but the physical transition is not observed;
- a command times out and later evidence shows either success, failure, or an outcome that remains ambiguous;
- one production history spans a model/revision change or deployment/mapping/profile change;
- telemetry arrives late or is corrected after a production step was previously reconciled;
- an external material/asset identifier is unmapped at ingestion and becomes authoritatively correlated later;
- a previously accepted subject correspondence is replaced while historical interpretation must remain attributable to the correspondence that was effective at the time;
- restart, failover, or disaster recovery occurs without turning runtime identity into durable production-history identity; and
- a backward/forward material trace, when material genealogy exists, can identify which requested operation, external realization, observations, correspondence, and reconciled interpretation support each material transformation claim.

A successful Operational model must preserve uncertainty. Missing observation must not become implicit success; transport acknowledgement must not become physical completion; and later interpretation must not rewrite the provenance of the raw observation that originally arrived.

These cases are evidence pressure on existing Operational questions. If they require a new canonical material identity or lineage relation rather than a new Operational fact, route that uncertainty back to the manufacturing-traceability investigation instead of duplicating the concept here.

## Drift and calibration — CANDIDATE

Determine how reconciled discrepancies become governed candidate changes without mutating published semantics directly. Reuse Governance-owned semantic change, requirements, conformance/findings, and evidence-use contracts rather than duplicating them.

## Resilience and recovery — CANDIDATE

Only after command/observation/correspondence contracts are accepted, determine their failure, idempotency, retry, recovery, trust-loss, ambiguity, and physical-reversibility semantics.

Unknown outcomes must remain unknown; observation loss must not become implicit success.

## First live-system proving case — CANDIDATE

Select one narrow integration only after the preceding boundaries are sufficiently settled. The proving case should demonstrate explicit external identity/correspondence, observation provenance, operation realization, trust where consequence requires it, command/result versus observed/reconciled truth, provenance, and recovery without allowing a protocol to define Arcogine's ontology.

Manufacturing traceability may become such a proving case only if a concrete integration is bounded enough to exercise those contracts without requiring Arcogine to adopt a complete MES/MOM information model first.

## Exit and promotion

Operational implementation is admitted to `docs/planning/` only when the relevant research has produced:

1. decision-quality semantics and ownership;
2. required ADR/architecture updates;
3. a bounded implementation responsibility;
4. explicit prerequisites; and
5. executable safety/correctness acceptance evidence.

The first promotion should be the narrowest implementation slice whose semantic referent is no longer under investigation.
