# Engine applicability to optional-record Factory policies — retained investigation

> **Status:** SUPERSEDED — completed predecessor investigation, replaced before architectural reconciliation
>
> **Risk:** High — accepted inputs, refusals, deterministic results, historical interpretation, and provenance
>
> **Authority:** Research evidence and history only. [Current architecture](../../architecture/overview.md), the owning specifications, and the [research register](../research-register.md) govern current meaning and lifecycle state.

## Question and historical result

The predecessor asked whether the fixed `engine-semantics:v1` definition could execute the then-reconciled `factory-model:v2` policy with the optional spatial record **present or absent** through a whole-definition-preserving correction, or whether a distinguishable Engine interpretation was needed. The decision at stake was the identity attributed to V2 execution. Factory validity, Engine applicability, and implemented execution support were separate predicates.

The report examined live `main` at `7e5e6ad2f8a2707d00fff3e2a4e3db96da85852a`. At that baseline, Factory V2 had required V1-shaped production records plus an optional, complete spatial record. Absence asserted no spatial or handling facts; present legal zero remained authored spatial content. V2 had shape and validation proving code, but no canonical V2 publisher, codec, verifier, or runtime execution path. Engine v1 had a fixed complete definition, including unimplemented spatial rules, while the runtime accepted only the currently supported v1 identity and V1 model type. Observation/event Engine-provenance propagation remained incomplete. The investigation did not establish a durable Engine-result store or inventory every external consumer.

**Conclusion reached at that time:** the report recommended a distinguishable complete Engine interpretation for both V2 forms, leaving v1 unchanged. It reasoned that admitting an actual spatial-absent V2 artifact, with its own fingerprint and successful production-only execution, was not affirmatively entailed by the inspected fixed v1 definition. The existing production behavior and spatial transfer arithmetic could be reused, but they did not alone define the new policy/content admission rule. This was a bounded recommendation, not an adopted `engine-semantics:v2` identity. A later independent adversarial review returned **ACCEPT WITH QUALIFICATIONS (Q1–Q5)**. Neither report nor review implemented V2 publication, runtime establishment, or successor conformance.

## Evidence and alternatives assessed

The report compared two principal candidates under its then-assumed interpretation of spatial absence:

| Candidate | Historical argument | Boundary exposed |
| --- | --- | --- |
| **A — v1-preserving correction** | Keep the v1 identity; apply its existing spatial rules to present V2 content and production-only behavior to absent V2 content. Preserve actual V2 fingerprint and all old refusals and outputs. | A correction needed affirmative proof that the **complete** v1 definition already admitted both exact V2 cases. Similar fields or outcomes did not supply that proof. |
| **B — distinguishable interpretation** | Keep v1 fixed; define a successor's exact policy/content admission and two behaviors, reusing unchanged rules where possible. It might explicitly include V1 too. | Feasible under the assumed absent-case meaning, but feasibility did not prove a unique or necessary distribution of identities. No identifier or support policy was thereby allocated. |

A restricted present-only v1 correction, with a different interpretation for absent V2, was also considered. Present spatial facts retained their meanings, so this partition was coherent in principle; its full policy/reference/refusal preservation was **unproven, not disproven**. It did not answer the report's combined both-form question. Continuing to refuse V2 was a safe interim support state, not a resolution of V2 applicability.

The whole-definition test covered more than equality of final time:

| Dimension | Reusable evidence from the predecessor |
| --- | --- |
| Accepted inputs and references | The Engine definition must declare which exact Factory policies, represented records, values, variants, and interactions it admits. A changed Factory grammar or fingerprint does not alone require a new Engine identity, but a live link to changed Factory text cannot silently expand a fixed Engine definition. |
| Interpretation and outputs | Existing non-spatial execution and five-fact transfer calculations can be reused where their meaning survives. Equal completion time does not establish equality of transfer state, events, reservation, observations, or same-time advancement turns. |
| Refusals | Factory-invalid content, a Factory-valid but Engine-inapplicable case, and an unsupported Engine identity are different failures. An applicable pair must be selected before runtime mutation. Current Java type exclusion of V2 is implementation evidence, not a permanent normative ban. |
| Authored content | Spatial absence supplies no floor, placement, footprint, rate, or overhead; present legal zero is authored content. Dropping a present record, inserting zeros for absence, or republishing a V2 projection as V1 destroys distinctions owned by Factory. |
| Attribution and history | Production equivalence across V1 and V2 does not equate fingerprints or controlled occurrences. Retain the actual Factory fingerprint and selected Engine identity. Historical v1 meaning includes unexercised rules and must remain exactly resolvable even if its executor retires; missing in-repository storage evidence cannot unfreeze it. |

The report's source map drew on the [semantic evolution rules](../../architecture/overview.md#semantic-evolution-and-support), [Factory semantic-evolution contract](../../architecture/factory-design.md#111-semantic-evolution), Factory V2 specification and Engine v1 specification (both since replaced by the work-in-progress [Factory model](../../architecture/factory-model.md) and [Engine semantics](../../architecture/engine-semantics.md)), [runtime contract](../../architecture/runtime-contract.md), and [semantic contract support](../../development/semantic-contract-support.md). It also inspected Factory V1 publication/assembly, V2 shape/validation, Engine identity/runtime/session and event/observation code, Governance evidence provenance, and their focused tests. Its historical comparison observed that the Factory-composition change made V2 spatial content optional without changing the Engine v1 specification. That comparison was evidence of a changed dependency, not authority to execute the former V2 grammar. The report author reported 103 passing tests in eight focused suites at the report baseline; the adversarial reviewer inspected source and tests but did **not** reproduce that run. No successor execution or conformance test was run.

Key implementation evidence at the historical baseline was:

| Boundary | Inspected source and executable evidence |
| --- | --- |
| V1 publication and assembly | `product/domains/factory/src/main/java/com/arcogine/factory/model/FactoryModelVersion.java`, `FactoryRuntimeAssembler.java`, and `FactoryModelPublisher.java` in the same directory |
| V2 representation and validation | `product/domains/factory/src/main/java/com/arcogine/factory/model/v2/FactoryModelV2.java` and `FactoryModelV2Validator.java`; corresponding `FactoryModelV2Test.java` and `FactoryModelV2ValidatorTest.java` under `src/test/java/` |
| Engine identity, session, and outward provenance | `product/types/src/main/java/com/arcogine/types/EngineSemanticsVersion.java`; `product/domains/factory/src/main/java/com/arcogine/factory/process/FactoryRuntime.java`, `FactoryHandler.java`, `RuntimeObservationMetadata.java`, `RuntimeEventEnvelope.java`, and `CommandResult.java`; focused `EngineSemanticsIdentityAcceptanceTest.java`, `EngineSemanticsV1DispatchConformanceTest.java`, `EngineSemanticsV1DerivedResultConformanceTest.java`, and `SessionControlAcceptanceTest.java` under the Factory test tree |
| Governance historical attribution | `product/governance/src/main/java/com/arcogine/governance/evidence/EvidenceProvenance.java` and `InMemoryEvidenceReferenceAuthority.java`; `GovernanceEvidenceTest.java` under the corresponding test tree |

These locators map the evidence inspected, not a claim that present code still has the same shape.

## Reusable proving cases

These are research discriminators for future investigation, not assertions that current code executes V2. Transfer outcomes in the historical report assumed spatial absence meant no transfer; the later [transfer-lifecycle investigation](transfer-semantics.md#concluded--transfer-lifecycle-independence) tested that premise and the [current boundary](../../architecture/transfer-applicability.md) states the bounded selected meaning.

| Case | Distinction to test |
| --- | --- |
| Factory V1 under Engine v1 | Preserve the actual supported baseline, identity, non-spatial ordering, refusal, and fresh-run behavior. |
| V2 spatial present with nonzero handling | Test applicability separately from the already specified five-fact arithmetic and its selection, binding, reservation, arrival, and recovery interactions. |
| V2 spatial present with legal zero | A transfer of zero duration can still have state, events, a separately scheduled completion, and different equal-time ordering from no transfer. |
| V2 spatial absent | Use the eventual transfer contract to decide whether there is no transfer, zero-duration transfer, or refusal; never invent spatial facts. Preserve the true V2 fingerprint. |
| Same-resource continuation | A control case whose outcome alone cannot prove full distinct-resource applicability. |
| Invalid present content | Incomplete coverage, unknown or duplicate placement, overlap, extent violation, negative handling, and arithmetic overflow stay Factory-invalid; do not repair by dropping content. |
| Valid but unsupported content or Engine identity | Refuse at the correct boundary before runtime mutation, with no silent fallback to `CURRENT` or production-field projection. |
| Equivalent production across V1 and absent V2 | Compare only stated semantics; equal production fields or final times do not authorize fingerprint equality or historical reattribution. |
| Future optional authored concern | No wildcard acceptance merely because current formulas ignore a field; determine support or irrelevance in the Engine definition. |
| Historical records and fault boundary | Resolve the exact attributed definition and source; distinguish pre-mutation refusal from a command fault that follows mutation. |

The historical numerical witness used two unary resources and a route `M1:2 -> M2:3`. With spatial distance 2, `ticksPerCell=4`, and `handlingTicks=1`, the specified transfer duration was 9 and the isolated completion time was 14. With both handling magnitudes zero, the isolated completion time could equal the no-transfer case (5) while transfer transitions still differed. The independent reviewer reconstructed a second witness (`M1:5 -> M2:7`, distance 5, rate 2, overhead 3): transfer duration 13 and isolated completion 25. These arithmetic checks did not establish the then-missing applicability rule or decide the spatial-absent transfer meaning later selected in the [current boundary](../../architecture/transfer-applicability.md). Competing-work and same-time ordering need separate executable fixtures.

## Independent review qualifications

The independent review accepted the bounded report **with qualifications**, all of which remain useful even though its chosen answer was superseded:

1. **Q1 — conditional proof.** Failure to prove this both-form v1 correction at the inspected baseline is not a theorem that every Factory-policy or byte difference requires a new Engine identity. A genuinely Engine-owned, whole-definition-preserving admission rule could change the assessment; a new fixture that merely assumes the rule could not establish historical entailment.
2. **Q2 — partition scope.** Present-only v1 preservation was unproven, not impossible. A hybrid support partition was coherent in principle but would require its own full preservation proof and explicit support/provenance duties. One successor covering both forms was a bounded recommendation, not a unique logical necessity.
3. **Q3 — attribution and dependencies.** Leaving v1 text untouched does not suffice if its meaning later resolves through an incompatible mutable Factory reference. Retain its exact dependency basis; preserve actual Factory fingerprints and conditional controlled-revision provenance. Do not fill missing historical Engine identity from `CURRENT`, silently execute an old request under a new identity, or manufacture a new Factory fingerprint to alter interpretation.
4. **Q4 — research versus release.** Acceptance of a report and inspected tests did not prove V2 publication, pair admission, spatial execution, provenance propagation, or successor conformance. Later implementation must prove supported inputs, refusal before mutation, absent/present-zero distinction, ordering, transfer interactions, arithmetic, identity propagation, and reset behavior at the owning boundaries.
5. **Q5 — bounded reopening and transfer.** Keep semantic meaning in its owning durable surface, not in temporary evidence coordinates. Concrete Factory/Engine boundary or consumer/cost evidence can reopen a conclusion or the optional-record composition choice; no universal optimality or measured cost was established by this report.

## Supersession and knowledge transfer

Merged PR #394 admitted a separate question: whether transfer lifecycle for distinct-resource continuation exists independently of spatial layout. The predecessor had inherited **spatial record absent implies no transfer lifecycle** as an input, rather than testing it. This is an actual exercise of Q5's reopening condition, not proof that every part of the report was wrong. Its both-form identity recommendation cannot answer the now-correctly-bounded question. The [post-transfer applicability brief](engine-applicability-after-transfer-boundary.md) was then admitted as **READY** under the reconciled [current transfer boundary](../../architecture/transfer-applicability.md); it was later superseded, uninvestigated, by the owner-directed provisional semantic-contract reset.

The predecessor does **not** establish adoption of `engine-semantics:v2`, one successor identity for both V2 forms, no transfer when spatial content is absent, concluded Engine applicability, or clearance of the V2 publication and spatial-runtime blockers. Closed PR #395 was failed delivery history; its proposed architecture, v1 restriction, planning clearance, and decision-rationale draft are not current authority.

**Knowledge-transfer audit:** this retained artifact promotes the bounded question, evidence categories and source map, historical conclusion, adversarial disposition and Q1–Q5, accepted-input/refusal/provenance analysis, proving cases, and exact supersession reason. The [successor brief](engine-applicability-after-transfer-boundary.md) held the open identity/support question until the reset superseded it. The [concluded transfer brief](transfer-semantics.md#concluded--transfer-lifecycle-independence), the [Factory model](../../architecture/factory-model.md), the [Engine semantics](../../architecture/engine-semantics.md), and the [spatial-runtime plan](../../planning/spatial-runtime-consequences.md) own their respective current state. Original handoffs, review prompts, the failed reconciliation draft and planning clearance, and branch-local diagnostics were disposable after that transfer landed. Future reasoning no longer requires exact temporary report/review files to remain fetchable.

No synthesis seed is warranted: this is a dependency and framing correction within one research chain, not independent cross-investigation recurrence. No historical decision-rationale record is warranted: no Engine successor choice was reconciled. The research register keeps the lifecycle history; architecture and planning remain unchanged by this retained evidence.
