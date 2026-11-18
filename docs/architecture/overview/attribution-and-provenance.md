# Attribution and decision boundaries

Part of the [architecture overview](../overview.md#routed-topics); it is mandatory whenever the overview routes your change here.


The [Events–State–Observations](events-state-observations.md#core-architecture-philosophy-events-state-observations) model says how a choice becomes a state change. It does not say **who is answerable** for that change, **what produced** the choice, or **what was acted on**. Those are separate questions, and they are easy to conflate because the implementation currently answers all of them with free-form strings.

This section records the durable semantic result of the [Agency and decision boundary investigation](../../research/investigations/agency-decision-boundary.md). Everything here is a **rule, not a type**. No Java type, module, persisted field, identity contract, or delivery track is introduced by it, and nothing here describes implemented capability: `Event` and `RuntimeEventEnvelope` carry no attribution today. Read these as constraints on future work.

The headline result: **no platform-level `Agent` abstraction is currently justified.** Designs that collapse or hard-bind the actor, decision-source, and subject roles below fail the investigation's proving cases, and no additional cross-case invariant was found that warrants a shared platform `Agent` concept now. This is a claim about current evidence and current consumers, not an impossibility claim — a future design that preserves the roles compositionally is not foreclosed. `agent` remains a useful application/domain label; it must not become a platform ontology, superclass, shared module, or durable identity category merely because that label already exists.

## Actor, decision source, and subject are distinct roles

```text
Actor            who or what bears responsibility for a participation
Decision source  the mechanism that produced the choice
Subject          what is acted on or represented
```

- **Role is a property of a participation, not an intrinsic permanent kind of an identity.** The roles must stay distinguishable; the identities occupying them need not be different. A human acting directly is both actor and decision source, and that is not a modelling failure.
- **One decision source may serve many actors**, and **one actor may change decision sources without changing its attributable identity**. Attribution that dissolves when a controller is replaced is not attribution.
- **A responsibility-bearing actor concept survives** across humans, organizations, software, planners, simulated participants, and external systems — as a concept, and not always present. An external data source is not automatically an actor.
- **The actor concept does not justify a shared actor type.** Arcogine has no `ActorId`, actor reference, actor-kind enum, actor namespace, or actor equality/lifecycle rule, and should not acquire one until multiple concrete consumers demonstrate the *same* equality, namespace, lifecycle, and interoperability contract strongly enough that domain-local representations would duplicate one semantic invariant. A committed implementation consumer is strong evidence for that; a consumer *count* is not a threshold. A closed actor taxonomy is specifically not justified — W3C PROV, the usual source cited for one, states its agent types do not cover all kinds of agent.
- **An acting party and a represented principal may differ**, and where they do, both must remain recoverable. Whether the represented principal retains responsibility is a **domain policy question**: delegation, impersonation, and transfer of responsibility are different relations, and Arcogine takes no general position that a delegator stays accountable.

## Four kinds of provenance must stay distinct

```text
attributable actor      who is answerable
recording provenance    what caused Arcogine to record something
decision provenance     which mechanism(s) produced the choice
external data source    where an observation came from
```

These must not collapse into one `source` or `actor` field merely because several current records happen to be strings. On current `main`, attribution-shaped names already mean different things, two of them are not pinned down by durable authority at all, and not all of them are even strings. **No field below may be renamed, reinterpreted, or mechanically migrated** into a future attribution capability:

- `RevisionRecorder` is, **as a whole**, persisted recording provenance identifying what caused Arcogine to record a controlled revision. Its internal `source` / `subject` decomposition is **underspecified by durable authority** — the [controlled revision contract](../controlled-revisions.md) permits representing the recorder with a small source/subject value without fixing which slot means what, and the implementation says only that the pair identifies "the source and subject that caused a revision to be recorded". Do not read `source` as a canonical mechanism/channel or `subject` as a canonical actor/principal. It is persisted and participates in idempotency equality in immutable governance history, so any future attribution must be **additive**.
- `ChangeProvenance.source` is *producer* provenance for a change set: its contract states plainly that **none of its fields are identity**. What the slot carries beyond that is **not established** — current call sites use both role-like and mechanism-like values — so it is neither a party identity nor, on current authority, a role label. It must not be migrated into a shared actor identity.
- `RequirementSource` is a requirement's *governing publication* provenance, and is a sealed domain type rather than a free-form string. Its sense of "authority" is **publishing body**, not authorization authority — a direct collision with the authorization sense of the word used elsewhere in this section.
- Historical decision-event descriptions were not attribution; that event vocabulary has been retired.

**Subject is distinct from actor, and there is no universal subject reference.** `AffectedEntityRef` (Factory-owned, sealed, transition-scoped) and `ChangedEntityRef` (Governance-owned) coexist deliberately, because their equality, namespace, ownership, and lifecycle contracts differ — shared appearance is not shared semantic identity. Neither is Arcogine's universal subject reference, and the word `subject` in `RevisionRecorder` does not establish Arcogine's meaning of "the subject of an operation": when the operation sense is meant, say so explicitly.
