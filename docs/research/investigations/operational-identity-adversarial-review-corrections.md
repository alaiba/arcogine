# Operational Identity adversarial review — successor corrections

> **Artifact type:** Successor correction note to handed-off adversarial-review evidence
> **Corrected review coordinate:** `f6a3b7148fadc9c29652f5bf269606333299496c` + `docs/research/operational-identity-adversarial-review.md`
> **Reviewed source report coordinate:** `bf744af171d5a42a3c578d33836e8e45bd33b0fe` + `docs/research/operational-execution-digital-twin-identity-report.md`
> **Correction baseline (live `main`):** `cbfcd36fb8f72ac39cc2896de97cc0113a22e41b`
> **Workspace baseline before this correction:** `98ce8fdc4442ed48677a9b715f88ec84329c4b7c`
> **Authority:** Research evidence only. This note does not amend ADR-0013 or accepted architecture.
> **Disposition impact:** The original `ACCEPT WITH QUALIFICATIONS` disposition remains unchanged.

This note does **not** replace, rewrite, or claim to be a new independent adversarial review. It preserves the exact handed-off review revision above and narrows two statements whose wording was stronger than the evidence supports. Any later reconciliation must read this note together with the original review; where the two conflict on the points below, this successor correction controls.

## 1. Divergence evidence must be representation-neutral

### Superseded wording

The original review repeatedly states that **every accepted record must carry** carried-forward evidence sufficient to locate it relative to the accepted material its writer extended. That wording appears most prominently in §7.4, surviving invariant 7, claim-table item 3, Q3, and the reconciliation handoff. The proving-case matrix also summarizes split-brain detectability as depending on "per-record carried-forward evidence."

That is too strong. The review correctly identified a semantic requirement — later evidence must be sufficient to establish common ancestry and incompatible extension — but it accidentally promoted one possible storage representation into the semantic contract.

### Corrected contract

> **Every accepted fact must remain durably relatable to the accepted continuation state or frontier it extended, with sufficient surviving evidence to establish common ancestry, incompatible extension, an identifiable divergence boundary where the model requires one, and the accepted material each continuation carried forward.**
>
> The evidence representation may be carried by each record, by a batch/segment/checkpoint envelope, by an authoritative correlated lineage/frontier structure, or by another representation that preserves the same proof obligation. ADR-0013 should constrain what evidence must remain establishable, not where bytes or fields must reside.

This correction preserves the load-bearing result of §7.4: ancestry links written only when a fork is declared are insufficient to detect an **undeclared** split brain. A record capability that throws away every durable relation between accepted facts and the continuation/frontier they extended can make later divergence unprovable. What does **not** follow is that every individual record must physically embed the relation.

The DRBD and SQL Server analogues remain useful as implementation precedents showing that carried-forward lineage/frontier evidence can make divergence observable. They do not establish a universal Arcogine storage shape. In particular, SQL Server's per-`backupset` fields demonstrate one workable representation, not a requirement that Arcogine duplicate that representation at record granularity.

### Required reading of the original review

For reconciliation, replace every normative occurrence of:

- "per-record divergence-evidence obligation";
- "per-record carried-forward evidence obligation";
- "every accepted record must carry evidence...";

with the representation-neutral contract above. References to per-record evidence may remain only as descriptions of specific external systems or as examples of a possible implementation.

Q3 therefore becomes:

> **Q3 — Preserve sufficient durable divergence evidence.** Every accepted fact must remain durably relatable to the accepted continuation state/frontier it extended, with sufficient surviving evidence to establish common ancestry and incompatible extension later. The evidence representation may be per-record or held in an authoritative correlated structure. The semantic obligation cannot be deferred merely because its representation can.

## 2. ADR-0008 does not settle one-level versus two-level Operational identity

### Superseded wording

The original review's §7.13 and Q9 say the single-identifier choice should be presented as an **Arcogine consistency decision grounded in ADR-0008**. That gives ADR-0008 more authority over Operational identity topology than it has.

ADR-0008 decides controlled-revision **occurrence identity and lineage**. It proves that Arcogine can successfully represent divergent historical occurrence lineage without a separate branch object, and it is therefore a valuable precedent. It does **not** decide how many durable identities Operational Execution may ultimately need, nor whether a future stable grouping/account identity above accountable continuations would be invalid.

### Corrected contract

> ADR-0013 may decide only the **accountable-continuation identity** needed by the current evidence and omit a second grouping identity because no current proving case or concrete consumer requires one. That is a scope-minimization decision for the present ADR, not a durable claim that Operational identity is permanently one-level.
>
> A future stable higher-level grouping/account identity remains permissible if a concrete consumer later proves a distinct equality, lifecycle, lookup, policy, or aggregation contract that accountable-continuation identity cannot satisfy.

Candidate `T` (stable account/group plus incarnation/continuation identity) therefore remains **unfalsified but currently unjustified**. Mature external systems using two levels remain contrary evidence against any claim that a one-level topology is universally superior. ADR-0008 supports the feasibility of a one-level current design; it does not foreclose future two-level Operational semantics.

Q9 therefore becomes:

> **Q9 — Keep the current identity decision minimal without foreclosing a second level.** ADR-0013 may define only accountable-continuation identity now and omit a second grouping ID because current evidence does not require one. Cite ADR-0008 as a useful lineage precedent, not as authority for Operational identity cardinality. Record explicitly that two-level identity remains a viable future extension if a real consumer proves a separate durable contract.

## 3. Effect on reconciliation handoff

The original review remains decision-quality evidence only with its qualifications, now including these two corrected formulations. The reconciliation handoff is therefore:

- preserve the accountable-operational-continuation referent and the other surviving invariants that were not changed by this note;
- state divergence detectability as a **surviving-evidence/provability** obligation, representation-neutral with respect to record layout and storage;
- define only the identity level current evidence requires, while explicitly avoiding a permanent prohibition on a later higher-level grouping/account identity;
- continue to defer representation, persistence layout, coordination mechanism, concrete acceptance storage model, and any second identity until a real consumer proves those choices necessary.

Nothing in this correction changes the original review's `ACCEPT WITH QUALIFICATIONS` disposition, its historical baseline, or the reviewed source-report coordinate.