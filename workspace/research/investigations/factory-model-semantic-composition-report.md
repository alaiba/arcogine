# Factory Model Semantic Composition — Research Report

> **Research status:** ACTIVE (report revision complete; not `CONCLUDED` — no durable consequence has been reconciled)  
> **Research baseline:** live `main` = `46389bc5d21e0c95ffa9366ce271f9d943d44b35` (resolved at investigation start; unchanged at final recheck before this revision was committed)  
> **Material under investigation, not landed truth:** workspace branch `research/factory-model-semantic-composition`; handoff prompt at commit `956be37b82513d07cc6930a081eda3d0b10e043b`  
> **Risk:** High (identity/equality, persistence, replay/history, public/persisted compatibility, foundational Factory architecture)  
> **Adversarial-review status:** required, not yet performed. The self-challenge in this report is the author's own and carries no weight of independent review.  
> **Authority statement:** This document is research evidence only. It is not accepted architecture, product direction, fingerprint policy, Engine applicability policy, or implementation commitment. ADR-0006 and ADR-0014 remain authoritative; current `factory-model:v1` semantics remain current; `FactoryModelV2`/`FactoryModelV2Validator` remain landed proving evidence; `factory-model:v2` canonical bytes, policy registration, and V1/V2 coexistence remain unreleased. This report performs no implementation and no authority transition.  
> **Sibling dependency:** [Semantic Contract Maturity and Durability](../../../docs/research/investigations/semantic-contract-maturity-durability.md) has **no decision-quality result** at this revision (its workspace branch `research/semantic-contract-maturity-durability` at `3120b14fd34754336c467471dc8173db9c2327b4` holds only a handoff prompt; no report, no review). Every durability, permanent-version, or historical-support statement below is therefore explicitly conditional.

## Question

> How should Arcogine represent independently applicable authored Factory semantics so that adding a new concern does not automatically turn feature accumulation into a linear whole-model `factory-model:vN` progression?

The brief's question needed no sharpening. One clarification was necessary during investigation and is applied throughout: the question is about **semantic concern structure and identity**, not about **contract durability**. Where the two touch (whether growing a recognized concern set is an amendment or a new policy version, and whether `factory-model:v1`'s label may be extended), this report states the structural requirement and defers the lifecycle answer to the sibling investigation.

## Decision at stake

Whether the next Factory model contract after `factory-model:v1` is:

- another closed whole-model policy (`factory-model:v2` as ADR-0014 defines it: V1 plus five mandatory spatial/handling facts, then `v3`, …), or
- one canonical aggregate whose required production substrate is composed with explicitly present, typed, optional authored concerns under one `ModelFingerprint`, or
- independently identified/versioned components, or
- named profiles.

Concretely this decides: canonical model shape; the canonical byte grammar and what a fingerprint-policy version identifies; whether adding hierarchy, storage, topology, qualification, or richer geometry forces every existing design to adopt unrelated facts to advance; how `engine-semantics:vN` states what it requires of a design; how Governance comparison and controlled-revision lineage cross concern-set changes; and what happens to the landed `FactoryModelV2` type and the unreleased `factory-model:v2` grammar.

## Scope and non-goals

In scope: the structural, identity, canonicalization, validation, applicability, and comparison consequences of composable authored concerns, tested against the brief's twelve proving cases.

Explicitly out of scope (per the brief): implementing any future concern; choosing Engine distance/routing/reservation/scheduling policy; a generic plugin/extension framework; deciding semantic-contract durability or permanent version identity (sibling); rewriting Accepted ADRs; deciding whether `factory-model:v1`/`v2` are preserved or discarded (this report states consequences, it does not perform them).

## Executive conclusion

**Surviving model: Candidate B — one canonical Factory aggregate = required production substrate + explicitly present, typed, optional authored concerns, under exactly one `ModelFingerprint`, canonicalized as a substrate stream followed by concern segments sorted by tag.** Confidence in the structural conclusion (B over A, C, and D) is **high**; confidence in the specific canonicalization variant and in every version/durability wording is **moderate and conditional** on the sibling maturity result.

The load-bearing findings:

1. **The current V1→V2 shape is not evidence of whole-model semantic evolution; it is evidence of a composed concern forced through a monolithic identity grammar.** The landed `FactoryModelV2` is literally V1 concepts reused unchanged plus one spatial concern (`FactoryFloor`, `ResourcePlacement`, `ResourceFootprint`, two plant scalars); its validator delegates to the V1 validator and adds spatial predicates; operations and products are byte-identical in the V2 grammar. Nothing about products, operations, or eligibility advanced a generation. The "generation" exists only because ADR-0006 binds field membership to a closed policy label and ADR-0014 makes the five additions mandatory.
2. **Linear whole-model policies (Candidate A) fail the orthogonal-concern proving cases structurally, not merely aesthetically.** Under ADR-0014 decisions 3, 10, and 12, a production-only design can never adopt hierarchy, storage, or qualification without also authoring spatial facts, because `v3 = v2 + X` inherits `v2`'s mandatory spatial additions, and there is no lift. A consumer wanting hierarchy without layout would need a parallel `v2'` line — which is the combinatorial matrix the brief warns about, arising inside Candidate A. A survives only for the substrate itself, and B incorporates that surviving fragment.
3. **One whole-model fingerprint is sufficient and required; independent component identities (Candidate C) are rejected.** No proving case exposed a statement that one aggregate fingerprint cannot preserve. ADR-0008 already fixes one `ModelFingerprint` per `ControlledRevision` and forbids multiple alternate fingerprints for one semantic state. Concern-scoped comparison is derivable from one artifact. C is reopened only by an independently owned/governed concern that designs *reference* rather than *contain*.
4. **Profiles (Candidate D) are not identity, canonicalization, or version lines.** The only surviving fragment is that an Engine semantics version, a consumer, or a validator may *declare a required concern set*; naming such a set is presentation. Profiles as contracts would reproduce Candidate A's closed bundles under names.
5. **Absence is a first-class authored fact and is already accepted architecture.** `engine-semantics:v1` §7 rule 9 makes a V2 design with zero handling magnitudes produce transfer events that a V1 design never produces; ADR-0014 decisions 9–10 forbid synthesized spatial defaults. Under B, absence is encoded as "no segment", is bytewise distinct from "present with zero", and no consumer may synthesize a value. This is confirmed as a known failure mode externally (Protocol Buffers' documented implicit-presence problem).
6. **Deterministic canonicalization of an optional, extensible concern set is a solved problem** (segments keyed by unique tag, sorted bytewise, length-framed, unknown tags rejected, duplicate tags rejected, decode→re-encode canonicality) — the same technique RFC 8949 §4.2.1 normatively specifies for deterministic maps. It produces no combinatorial version matrix: N concerns yield one grammar with N tag definitions, not 2^N policies.
7. **Engine applicability should be declared over represented concerns, not over a whole-model policy number.** `engine-semantics:v1` is already, in substance, "substrate required; spatial placement interpreted when present" — one Engine semantics version covers both V1 and V2 designs today. Making that disposition explicit (REQUIRED / CONDITIONAL / INERT per concern; any undeclared present concern ⇒ inapplicable, failing explicitly before runtime mutation) is more truthful than a policy number and no more complex than the existing `requireSupported` seam. A production-only design is a first-class valid design that is simply inapplicable to an interpretation requiring facts it never authored.
8. **The core structural invariant B must satisfy — and A cannot — is: adding a recognized concern never changes the canonical bytes or fingerprint of any design that does not represent it.** Whether the growth of the recognized set is labelled an amendment of one policy or a new policy version, and whether older verifiers must be retained, are the sibling's lifecycle questions; the byte-stability invariant is required under either answer.

**Smallest recommended durable destination:** supersede the whole-model-generation portions of ADR-0014 (decisions 2, 3, 11, 12) and narrow ADR-0006's "policy version = closed field membership" clause into a composition rule; rewrite the unreleased `factory-model-v2.md` grammar (amendable pre-ship per its own §10) as a substrate-plus-concern-segment grammar with spatial placement as the first concern; add a per-concern disposition table to `engine-semantics-v1.md` as a semantics-preserving amendment; retain the landed V2 value objects and validator predicates as the spatial-placement concern's implementation. **No change** is the honest answer for ADR-0003, ADR-0004, ADR-0008, ADR-0015, and Factory Resource Semantics. None of this is performed here.

## Repository evidence

All items are **Repository fact** at the research baseline unless labelled otherwise.

### Accepted decisions and normative specifications

- **ADR-0003** (Accepted): the canonical model owns "products, operations, resource definitions, resource instances, capability or eligibility requirements, policies/constraints, and semantic layout where those facts affect execution"; exact type decomposition is left to implementation. It does not prescribe a linear policy sequence.
- **ADR-0004** (Accepted): semantic identity is a deterministic content fingerprint; controlled revision identity is separate; human `vX` labels are not identity.
- **ADR-0006** (Accepted): `factory-model:v1` = namespace + policy version + algorithm + digest; *"The policy version identifies the semantic/canonicalization contract … Any change that can alter semantic field membership, ordering, normalization, binary encoding, or digest semantics requires a new policy version."* It also states that v1 field membership is *"not … the ideal permanent ontology"*, keeps list order semantic conservatively, and lists "schema evolution/migration policy" among its non-goals. The v1 verifier rejects trailing bytes.
- **ADR-0008** (Accepted): a `ControlledRevision` references exactly one `ModelFingerprint`; *"A revision does not contain multiple alternate fingerprints for the same semantic state"*; *"`ModelFingerprint.policyVersion` already versions the fingerprint semantics. A separate domain model or serialization schema version should be added only when a concrete cross-domain contract requires it"*; lineage is independent of policy version.
- **ADR-0014** (Accepted): v1 immutable permanently (1); V2 is "a complete canonical Factory semantic artifact, not a spatial sidecar" (2); V2 = V1 + five **mandatory** additions (3); the additions are model facts, not Engine policy (4); orientation/paths/topology excluded until behaviorally relevant (5); footprint canonical though unused by Engine v1 distance (6); revision identity distinct from policy version, lineage may cross policies (7); policy-aware permanent resolution (8); a V1 model *"is not a degraded mode; it is the truthful execution of a design that never authored spatial semantics"* (9); no automatic lift (10); cross-policy comparison explicit, requiring migration classification or common representation (11); *"when a future behaviorally relevant authored fact cannot be represented without changing the meaning of a released Factory model policy, Arcogine creates `factory-model:vN+1`"* (12). Alternatives: an independent spatial fingerprint was *"rejected for the current capability … Splitting identity before independently evolving semantic components are required would add compatibility complexity without benefit."*
- **ADR-0015** (Accepted): one `EngineSemanticsVersion` covers the complete result-affecting interpretation; authored facts and interpretation have different owners; an implementation *"explicitly declares which semantics versions it executes and fails on an unsupported version"* (15); cross-version comparison is consumer-owned (17); no independently versioned Engine sub-policies without a proven need (4).
- **`factory-model-v2.md`** (normative, *implementation pending*, amendable until shipped per its §10): the V2 stream is the V1 stream with exactly three differences (prefix, a four-scalar plant header, four `I64`s appended per resource); *"Operation, step, and product records are shape-identical to V1's."* Footprint is anchor-plus-extent, two `I64`s, with no shape discriminator.
- **`engine-semantics-v1.md`** (normative): §5 consumes "the V2 model facts … floor dimensions, resource position, resource footprint, `ticksPerCell`, and `handlingTicks`"; §7 rule 9: a V2 design with zero magnitudes *"preserves the completion timing that would occur without spatial transfer delay while still exposing the authoritative transfer start/completion transitions"*; §9.3 *"V1 invents no transport resource/capacity, conveyor scheduler, physical buffer, or congestion model"*; §13 defers topology, transport resources, congestion, orientation.
- **ADR-0016** (Accepted) and Governance architecture §9: applicability is an explicit, attributable determination; stale/incompatible evidence *"never silently produces `PASS`"*. **Inference:** this is the repository's existing pattern for "applicability is a declared determination over represented facts", not a version-number equality.

### Architecture references

- **`factory-design.md`** (Proposed reference): §4.1 lists pool/work-center membership, transfer/dispatch policies, executable constraints, and behaviorally relevant geometry (position, orientation, footprint, connection points, zones, transfer relationships) as canonical *when they change executable meaning*; §8 *"Spatial layout and resource hierarchy are independent"*; §9 semantic vs presentation geometry; §10.1 lists "footprint lies inside the floor", "footprints do not overlap", "required transfers are representable" as Arcogine executability constraints.
- **`factory-resource-semantics.md`** (maintained): configured identity, reusable specification, qualification, hierarchy, spatial placement, runtime state, and external asset are *"orthogonal distinctions"* not to be collapsed; storage/buffers and transport resources *"may eventually participate in shared requirement/allocation relations"*, promoted as typed concepts *"when their own lifecycle, qualification, occupancy, contention, or behavior becomes consequential."*
- **`isa-95-semantic-mapping.md`** §8: hierarchy scope and spatial layout are *"related but independent dimensions"*; *"Do not use hierarchy membership as a substitute for coordinates or distance"*; *"A future machine instance may reference both one hierarchy node and one spatial location."*
- **Charter**: "one model, many views"; "semantics survive deployment"; "causality and provenance".

### Implementation and tests

- `FactoryModel(resources, operations, products)`; `ConfiguredResource(id, name, concurrency, capacityLiters, setupTime)`; `OperationDefinition(id, name, steps)`; `OperationStepDefinition(stepId, name, eligibleResources, duration)`; `ProductDefinition(id, name, operationId)`.
- `FactoryRuntimeAssembler.assemble(FactoryModelVersion)` consumes only ids, names, concurrency, `capacityLiters`, `setupTime`, routing steps (id, name, eligible set, duration), and product→operation. `FactoryHandler` uses none of `capacityLiters`/`setupTime` in dispatch or timing; both are carried into `MachineView`/observations only. **Inference:** V1 already contains fingerprinted authored facts that the current Engine does not interpret — the admission criterion is "designed production-system fact", not "used by the current Engine". Footprint under ADR-0014 decision 6 is the same pattern.
- `FactoryModelFingerprintV1`/`FactoryModelArtifactV1`: prefix `arcogine.factory-model.v1\0`, strict decode with canonical re-encode, `supports(fingerprint)` keyed on namespace/policy/algorithm.
- `FactoryModelV2(floor, ticksPerCell, handlingTicks, List<SpatialConfiguredResource>, operations, products)`; `SpatialConfiguredResource(ConfiguredResource, ResourcePlacement, ResourceFootprint)` *composes rather than duplicates* V1; `FactoryModelV2Validator.validate` = `FactoryModelValidator.validate(model.baseModel())` **plus** floor, handling, placement/footprint containment, non-overlap, and max-transfer predicates. `FactoryModelV2` shares no supertype with `FactoryModel` (`V1V2IdentitySeparationTest`). No production code constructs a `FactoryModelV2`; its only consumers are its own tests.
- Governance: `SemanticArtifact(fingerprint, canonicalBytes)`; `SemanticArtifactVerifier.supports/fingerprint`; `ChangeSetFactory.compare` throws unless the extractor supports **both** artifacts' policies; `FactoryModelSemanticComparator.supports` = V1 only; comparison is by stable entity identity with top-level reorder reported as `ENTITY_MODIFIED`. Cross-policy comparison is currently an explicit failure, consistent with ADR-0014 decision 11.
- `EngineSemanticsVersion.CURRENT = engine-semantics:v1`; `FactoryRuntime` calls `requireSupported(CURRENT)` at establishment — the existing seam where an applicability check would live.

### Consumers (whether partial concern sets are real usage)

- Every real authoring path (`ScenarioFactoryModelAdapter` from TOML, API `HandlerFactory`/`IntegratedHandler`, CLI `HeadlessHandler`, Challenge `ChallengeDefinitionValidator`) produces a V1 `FactoryModel`. `docs/examples/*.toml` author no spatial facts.
- The web `FactoryFlow` component computes `positions` locally for presentation only.
- The Challenge consumer already owns `FactoryFloorConstraint(width, height)`, `GridPlacement(x, y)`, and `PlacedEquipment`, and `CandidateAdmissibilityPolicy` performs floor-bounds and overlap admissibility **consumer-locally** while projecting a production-only canonical model. The game consumer plan lists "deterministic spatial transfer consequences" as a required Engine capability before playable integration.
- **Inference:** two concern sets have real standing today — production-only (every landed consumer) and production+spatial (game plan, V2 proving slice). The Challenge consumer's local floor/overlap checks are direct evidence that spatial validation is a shared need currently duplicated outside the canonical model because the canonical model cannot carry it without forcing every design into a spatial generation. A third orthogonal concern with a landed consumer does **not** exist yet; the orthogonality pressure on Candidate A is established from accepted architecture direction (`factory-design.md` §4.1, resource semantics) and from the structure of ADR-0014, not from a landed consumer. This limitation is recorded in *Confidence and limitations*.

### History

- ADR-0014 and the V2 grammar were introduced by the spatial-transfer convergence decisions (PR #248, #252); the V2 shape/validation slice landed in PR #299 as proving evidence with the fingerprint policy deliberately withheld; PR #352 admitted this question and the sibling, placing V2 identity and coexistence under a research hold *"not an architectural supersession"*.

## Candidate models

- **A — linear whole-model policies.** `factory-model:v1, v2, v3, …`; every new behaviorally relevant authored fact that cannot fit the released grammar creates a complete new policy; each policy is a closed bundle; older policies remain resolvable; cross-policy comparison is explicit.
- **B — one canonical aggregate with typed optional semantic concerns.** One `FactoryModel` = required production substrate + a set of explicitly present authored concerns, each identified by a tag with its own grammar and validation predicates and declared prerequisite concerns. One `ModelFingerprint` over the complete aggregate; presence/absence is fingerprinted. Absence = no claim in that dimension.
- **C — independently identified/versioned semantic components.** Substrate, spatial, storage, hierarchy, … each with its own fingerprint/version; a Factory definition composes them by reference.
- **D — supported Factory profiles/capability sets.** A small set of named coherent combinations (production-only, spatial-production, …) as the supported contracts.
- **E — hybrid.** Considered in the form "one whole-model identity with internally versioned concern grammars only after a concern independently earns durability". Evaluated below; it collapses into B plus the sibling's lifecycle rule rather than being a distinct structure.

The simplest no-new-abstraction candidate is A (it is the accepted architecture). B introduces exactly one new structural notion — a tagged concern segment — and no new identity, lifecycle state, profile, or extension framework.

## External evidence

Used only where it discriminates; all three sources below were fetched and checked during this investigation.

1. **RFC 8949, *Concise Binary Object Representation (CBOR)*, IETF, December 2020, §4.2.1 "Core Deterministic Encoding Requirements"** — *Verified in session.* Normative rules for a deterministic encoding of an extensible keyed structure: integers and lengths in shortest form; no indefinite-length items; *"The keys in every map MUST be sorted in the bytewise lexicographic order of their deterministic encodings."* **What it establishes:** deterministic canonicalization of an optional, extensible, keyed set is achieved by a small fixed rule set (unique keys, bytewise sort of encoded keys, fixed primitive forms) — directly answering brief question 9 without inventing anything Arcogine-specific. **Where the analogy breaks:** CBOR is a data-item encoding, not a semantic identity contract; it says nothing about which keys are semantically admissible, about rejecting unknown keys, or about publication validation — those remain Arcogine rules. (Its treatment of absent-vs-null keys was *not* relied on.)
2. **Protocol Buffers documentation, "Field Presence" (protobuf.dev, current edition, fetched 2026-09-18)** — *Verified in session.* Under implicit presence *"the default value is synonymous with 'not present' for purposes of serialization"*; a parser cannot distinguish a field set to its default from one never set; this makes partial/patch merges *"effectively impossible"* and creates ambiguity where the default may mean explicitly set, cleared, or never set; the maintainers now recommend explicit presence (`optional`) for proto3 scalars. **What it establishes:** a mature, widely deployed serialization contract found that conflating "absent" with "default" is a real semantic defect and reversed course to restore explicit presence. This is external confirmation of the brief's requirement that concern absence must not become an invented zero, and of the accepted ADR-0014 decision 10 / `engine-semantics:v1` §7 rule 9 distinction. **Where it breaks:** protobuf presence is per scalar field inside a mutable message; Arcogine's unit is a whole authored concern inside an immutable published aggregate, and Arcogine additionally forbids *any* synthesized default rather than merely tracking presence.
3. **AutomationML (IEC 62714), automationml.org overview (fetched 2026-09-18)** — *Verified in session (association overview page; the IEC text itself was not fetched).* AutomationML composes *"object topologies including hierarchies, properties and relations of objects: CAEX according to IEC 62424"* with geometry/kinematics (COLLADA) and behavior/logic (PLCopen XML); *"Each CAEX object can contain properties and reference geometry, kinematics or logics information stored in third party XML files."* **What it establishes:** an established plant-engineering interchange standard separates topology/hierarchy, geometry, and behavior as distinct, independently authored aspects linked by reference from one top-level object model — evidence that these are real, recognized concern boundaries rather than an Arcogine invention (brief proving cases 5, 6, 9). **Where it breaks (important):** AutomationML's aspects are *separately stored files with their own identities* because different engineering tools own them. That is the independent-evolution boundary Candidate C would need; it arises from multi-tool ownership, which Arcogine does not have (one canonical publisher), and AutomationML has no content-derived semantic identity contract at all. It therefore supports B's concern boundaries and identifies the *trigger* for C; it does not support C for Arcogine today.

**Background, not verified, not load-bearing:** the Asset Administration Shell metamodel (IDTA-01001) is recalled as aggregating optional, typed submodels each carrying a `semanticId`, with administrative rather than content-derived versioning. The IDTA specification pages could not be fetched (HTTP 404 on two URLs). Nothing in this report depends on it.

No external source was needed for the identity question: the discriminating evidence there is repository-internal (ADR-0008, ADR-0014's rejected spatial-fingerprint alternative, the resource-semantics promotion rule).

## Proving cases

### Candidate × proving-case matrix

`✓` survives · `✓q` survives with qualification · `✗` fails

| # | Proving case | A linear | B composed | C components | D profiles |
|---|---|---|---|---|---|
| 1 | Core-only deterministic production | ✓ (v1 stays first-class per ADR-0014 d.9, but is frozen out of any later concern) | ✓ | ✓q (needs a substrate component identity plus an aggregate) | ✓ (production-only profile) |
| 2 | Current spatial transfer | ✓ (this is what v2 is) | ✓ (spatial-placement concern; ownership unchanged) | ✓q (second identity for no gain) | ✓ |
| 3 | Logical storage without geometry | ✗ (`v3 = v2 + storage` inherits mandatory spatial facts) | ✓ (storage concern, prerequisite substrate only) | ✓ | ✗ (profile explosion or a "storage-only" profile per combination) |
| 4 | Spatial storage zones | ✓q (only as a further monolithic generation) | ✓ (storage-zone concern with prerequisites storage + placement/regions) | ✓q (cross-component references need a third identity or an aggregate) | ✗ |
| 5 | Richer geometry | ✗ (rectangles→polygons forces products/operations into a new generation) | ✓q (additive tagged alternative inside the geometry grammar; requires the placement grammar to carry a shape discriminator from the start) | ✓ | ✗ |
| 6 | Explicit material-flow topology | ✗ (same as 3) | ✓ (topology concern; geometric consistency is a cross-concern predicate when placement is also present) | ✓ | ✗ |
| 7 | Contended transport resources | ✗ (would force every design to declare transport) | ✓ (substrate identity for the participant + transport-relation concern; absent ⇒ no transport contention, as Engine v1 already states) | ✓q | ✗ |
| 8 | Qualification / resource-dependent performance | ✗ (same as 3) | ✓q (qualification concern independent of spatial; an overriding relation such as resource-dependent duration must declare precedence over the substrate step duration explicitly) | ✓ | ✗ |
| 9 | Hierarchy / work-center / pool scope | ✗ (same as 3) | ✓ (grouping concern over resource ids; independent of placement) | ✓ | ✗ |
| 10 | Cross-consumer partial models | ✗ (each consumer subset needs its own policy line) | ✓ | ✓q (compatibility states multiply per component pair) | ✗ |
| 11 | Historical revision and comparison | ✓q (needs an explicit migration classification at **every** generation boundary) | ✓ (one policy; concern-added/removed is an explicit change kind; no invented defaults; lineage unaffected) | ✗q (a revision would need several fingerprints or an aggregate — ADR-0008 forbids multiple alternate fingerprints per state) | ✓q (profile transitions are the same migration problem as A) |
| 12 | Cross-concern validation | ✓ (monolith validates everything) | ✓ (prerequisite DAG + predicates owned by the referencing concern; the landed V2 validator already has this shape) | ✓q (cross-identity validation needs the aggregate anyway) | ✓ |

**Result:** A fails 3, 5, 6, 7, 8, 9, 10 and survives 1, 2, 4, 11, 12 only with qualification; its surviving fragment is "substrate meaning changes are whole-model changes", which B incorporates. C survives most cases but only by adding an aggregate identity on top of component identities — at which point the components preserve nothing the aggregate does not. D fails every partial-set case unless profiles multiply into the same matrix. B survives all twelve, with two qualifications (5 and 8) that are grammar-design obligations, not structural weaknesses.

### Case narratives (where the matrix needs justification)

**1. Core-only production.** Under every candidate the design is valid. The discriminator is *what it can become*. Under A (ADR-0014 d.3 + d.10) it can only become a v2 design by authoring all five spatial facts; if a later v3 adds hierarchy, hierarchy is unreachable without spatial facts. Under B it may add any concern whose prerequisites it satisfies, and remains bytewise unchanged until it does.

**2. Current spatial transfer.** Under B the five ADR-0014 facts become the `spatial-placement` concern: plant header (floor width/height, `ticksPerCell`, `handlingTicks`) plus per-resource (position, footprint) keyed by `MachineId`. Ownership is unchanged: authored facts in the concern, Manhattan distance/binding/reservation in `engine-semantics:v1`. The maximum-transfer predicate stays a publication predicate of the concern. Nothing about A's *content* is lost; only its identity framing changes.

**3/14. Storage without geometry; geometry without storage.** Logical storage (a buffer with capacity, admissible materials, blocking semantics) references resources/operations by id and needs no coordinates; spatial layout (V2 today) needs no storage. They are independent concerns. A *storage zone* is the cross-concern relation (storage + region), expressible only when both prerequisites are present — evidence that interaction between concerns does not require collapsing them. Engine v1 §9.3 confirms Arcogine currently invents no physical buffer; storage absence therefore has a defined meaning already.

**5. Richer geometry.** The current V2 grammar encodes footprint as two `I64`s with no shape discriminator. Under B, a later polygon/orientation representation must be an *additive tagged alternative* (existing rectangle bytes unchanged; old verifiers reject polygon artifacts explicitly; new verifiers accept both) rather than a reinterpretation of existing bytes. That is only possible if the placement concern's grammar carries a shape tag from its first release — a concrete design obligation for the reconciliation that rewrites `factory-model-v2.md`, and one that is still cheap because the grammar is unshipped and amendable (its §10).

**6. Topology.** Paths, connection points, adjacency, and reachability *facts* are authored Factory content (the design claims them); whether a route exists without coordinates (a logical adjacency graph) is a legitimate design — so topology is not merely "more spatial layout". Geometric consistency (a path inside the floor, clear of footprints) is a cross-concern predicate when placement is also present. Which route the Engine takes, congestion, and rerouting are Engine interpretation (Engine v1 §13 already places them there).

**7. Transport resources.** A forklift is a configured productive participant (substrate identity, concurrency) whose *role* in moving material is a relation between it and routes/handling — a transport concern. A design without it has no transport contention (Engine v1 §9.3), which is exactly the absence semantics B needs. A would require every design to declare a transport concern once `vN` includes it.

**8. Qualification and resource-dependent performance.** A qualification relation (operation requirements ↔ resource provisions) references only substrate ids. Resource-dependent duration is harder: the substrate already authors a step duration, so a concern that varies it by resource *overrides* a substrate fact. B survives only if the overriding concern's grammar states its precedence explicitly and the Engine disposition table declares how it is interpreted — a rule the concern owns, not a reason to make it mandatory for every design. Recorded as a qualification, and as the one place where "independent applicability" must be stated as "independently *present*, with declared precedence", not "independent of the substrate's meaning".

**9. Hierarchy.** A grouping concern over `MachineId`s; independent of placement per `factory-design.md` §8 and ISA-95 mapping §8. Absence ⇒ no scope semantics. Under A, hierarchy would be `vN` and inherit whatever `vN−1` made mandatory.

**10. Cross-consumer partial models.** Game/experiment (production-only) — real today. Optimizer (spatial/handling) — planned; V2 tests prove the shape. Industrial importer (hierarchy + geometry, possibly no handling magnitudes) — hypothetical but architecturally admitted (AutomationML design-for status). Engine execution requiring a declared subset — covered by the disposition table (next section). Governance comparison across sets — covered by concern-aware comparison. Under A each of these is a separate policy line or an impossible combination.

**11. Historical revision.** Under B a controlled revision moving from a core-only design to one that adds `spatial-placement` changes `ModelFingerprint` (presence is fingerprinted), keeps one fingerprint per revision (ADR-0008 unchanged), and its `ChangeSet` reports "concern represented: spatial-placement" plus the concern's content — never "position changed from (0,0)". Removing a concern is the symmetric explicit change. No migration classification is needed because there is no policy boundary. Under A, ADR-0014 d.11's explicit migration classification is required at every generation.

**12. Cross-concern validation.** Validation composes as a prerequisite DAG: substrate validation is concern-blind; each concern validates its own predicates plus those referencing its prerequisites (containment/overlap reference the floor; a zone references a region; a route endpoint references a connection point; a qualification references a resource/operation id). `FactoryModelV2Validator` is already this shape (`FactoryModelValidator.validate(baseModel())` + spatial predicates). The separation is not nominal: dependency direction is one way, and no substrate predicate needs to know any concern exists.

## Identity and fingerprint analysis

**Four identities kept distinct** (brief requirement):

```text
Factory design identity        ModelFingerprint over the complete aggregate
                               (substrate + every represented concern + their presence)

semantic concern structure     the set of concern tags present in that aggregate
                               (an authored fact, encoded in the bytes; not an identity)

semantic-contract maturity     how permanently Arcogine has committed to a grammar's meaning
                               (sibling investigation; NOT decided here)

controlled revision identity   ControlledRevisionId, one per historical occurrence,
                               referencing exactly one ModelFingerprint (ADR-0008, unchanged)
```

**Why one fingerprint (Q6).** Every consumer question the proving cases raised — "is this the same authored design?", "what changed?", "which design produced this run?", "does this design supply what the Engine needs?" — is answered from one aggregate identity plus decoding its bytes. Concern-scoped comparison is a decode-time projection, not a second identity. ADR-0008 already forbids alternate fingerprints per state, and ADR-0014's own alternatives analysis rejected a spatial sidecar identity as "compatibility complexity without benefit". Composition of *structure* therefore does not imply composition of *identity* — exactly the inference the brief warns against.

**When a component would merit independent identity (Q7/Q16).** Only when a concern's content is authored, governed, and revised *independently of the designs that use it* and designs must *reference* it (so that "these two designs conform to the same governed layout revision" becomes a necessary cross-consumer statement), or when a concern must be verified against an explicitly versioned external contract. This is the same promotion test `factory-resource-semantics.md` applies to reusable specifications, and it is the multi-tool ownership pattern AutomationML exhibits. No landed or planned consumer needs it. Until then an independent identity would add a compatibility state (aggregate × component) with no statement it uniquely preserves.

**One-fingerprint ≠ one ever-growing schema (the second inference the brief warns against).** Under B the fingerprint policy identifies the *composition grammar* (prefix, substrate stream, concern-segment framing) rather than a closed field set; the recognized concern set grows additively and never re-prefixes existing artifacts. That is what makes one fingerprint compatible with non-monotonic concern sets.

## Canonicalization and publication requirements (Q8/Q9)

Structural requirements sufficient to assess feasibility (not a normative grammar — that is reconciliation's job):

```text
prefix                      arcogine.factory-model.<policy>\0        (domain separation, as today)
substrate stream            resources / operations / products         (V1 stream shape; list order
                                                                       remains semantic, ADR-0006)
U64(concernCount)
for concern in concerns sorted by bytewise order of TEXT(tag):
    TEXT(tag)               unique; duplicate tag ⇒ reject
    U64(byteLength)         segment framing
    <concern bytes>         fixed by that tag's grammar, using only the existing primitives
                            (U64 / I64 / TEXT / OPTIONAL_F64), references by stable ids,
                            internal set-shaped data sorted, internal lists in authored order
                            where order is semantic
```

Determinism obligations: unique tags sorted bytewise (the RFC 8949 §4.2.1 technique); no concern encoded twice; each concern grammar total over its value domain with publication predicates applied at decode (as `factory-model-v2.md` §7.6/§9.3 already require); decode → re-encode byte equality (§9.2 today); **unknown tag ⇒ explicit rejection, never skip** (skipping would let an artifact carrying unvalidated behaviorally relevant facts acquire a well-formed identity from a verifier that could not validate it); presence/absence is exactly the segment's existence, so "absent" and "present with all-zero content" have different bytes and different fingerprints.

There is no combinatorial matrix: with N recognized concerns there is one grammar, N tag definitions, and validity is decided per concern by its own predicates plus declared prerequisites. Compatibility questions reduce to set inclusion ("does this verifier recognize every tag present?").

**Two structural variants exist for the first composition policy, and the choice is the sibling's to constrain:**

- **B-i — new prefix.** A new policy prefix; a concern-free design gets a different fingerprint from its `factory-model:v1` fingerprint (one-time discontinuity, the same kind A incurs at every generation); `factory-model:v1` stays a historical closed policy; one policy-coexistence seam is needed once.
- **B-ii — substrate-first, v1-byte-preserving.** Concern segments are appended after the v1 substrate stream; a design with zero concerns is byte-identical to its v1 artifact and keeps its `factory-model:v1` fingerprint. This is the more truthful outcome for every existing design, but it requires relaxing the released v1 verifier's trailing-byte rejection rule under the same label, which ADR-0006 as accepted classifies as a new policy version. Whether a released label may be extended additively is precisely the maturity question. **Inference:** the *invariant* — adding a recognized concern never changes the bytes of a design that does not represent it — holds under both variants from the first composition policy onward; B-ii additionally makes the transition itself byte-preserving.

## Engine applicability analysis (Q10/Q11)

**Current state, restated as a disposition.** `engine-semantics:v1` interprets both V1 and V2 designs: substrate rules always; transfer rules only when spatial facts are represented (ADR-0014 d.9, Engine v1 §5/§7). Footprint is present-but-inert for distance (ADR-0014 d.6, Engine v1 §7 rule 3). That is already "applicability by represented facts"; it is merely phrased in terms of the policy that carries them.

**Tested hypothesis.** An Engine semantics version declares, per recognized concern tag, one disposition:

```text
REQUIRED      absent ⇒ inapplicable
CONDITIONAL   present ⇒ these rules apply; absent ⇒ those rules are vacuous, nothing synthesized
INERT         present ⇒ validated at publication, does not affect this interpretation's results
(undeclared)  present ⇒ inapplicable
```

`engine-semantics:v1` today is `{substrate: REQUIRED; spatial-placement: CONDITIONAL}` with footprint inert inside the concern's rule text, and every other tag undeclared.

**Failure mode (Q11).** Applicability is checked where `FactoryRuntime` already calls `EngineSemanticsVersion.requireSupported` at establishment: an inapplicable design is rejected explicitly, before any runtime mutation (the same zero-mutation discipline ADR-0007/Engine v1 §3 rule 5 apply to rejected commands), naming the missing REQUIRED concerns or the undeclared present concerns. Arcogine never synthesizes the missing concern, never silently ignores a present one, and never executes in a degraded mode. A production-only design is inapplicable to an interpretation that REQUIRES topology; it is not "old".

**More truthful than a policy number?** Yes, on the orthogonal case that discriminates the candidates: under A, when `v3` adds hierarchy, a spatial-only Engine must either declare `v3` support (and thereby implicitly claim a disposition for hierarchy it never examined) or declare every `v3` design inapplicable (forcing hierarchy consumers to lose execution). Under B the declaration names the concern, so a design carrying a concern the interpretation has not examined fails explicitly rather than executing under an unexamined claim. Provenance is unchanged: `ModelFingerprint + EngineSemanticsVersion` still identify the pair, because the disposition table is part of the semantics specification and changing it is result-affecting under Engine v1 §1.1 (a new `EngineSemanticsVersion`).

**More complex?** One declarative table per Engine semantics version, checked at one existing seam. This is less machinery than A's per-generation verifier/comparator/Engine re-declaration.

**What it does not decide:** which facts inside a CONDITIONAL concern are inert (that stays rule text, as footprint is today); whether an INERT disposition is ever admissible for a concern that is result-affecting for *some* interpretation (Engine v1 §1.1's completeness rule already governs that per version).

## Cross-concern validation and controlled-revision comparison (Q5/Q12/Q15)

**Validity of a published aggregate** = substrate valid ∧ every present concern's prerequisites present ∧ every present concern's own predicates hold ∧ every cross-concern predicate owned by a present concern holds. Impossible combinations are rejected by prerequisite declaration (a storage zone without regions) or by a concern's own contradiction rule (two geometry representations for one footprint), never by a global combination table. Rules scale linearly with concerns.

**Controlled revisions** are unchanged: one `ControlledRevisionId`, one `ModelFingerprint`, lineage independent of concern set, rollback an ordinary new revision (ADR-0008). Concern-set change is not a lineage event and needs no special migration classification within one composition policy.

**Comparison** becomes concern-aware inside one extractor: substrate compared by entity identity as today; concern presence compared as an explicit change ("concern represented"/"concern withdrawn" — a distinct change kind or a `ChangedEntityRef` type per concern, chosen at reconciliation); within-concern content compared only when the concern is present on both sides. The rule that prevents invented defaults is: **a comparison never reports a value on a side where the concern is absent.** `ChangeSetFactory`'s requirement that the extractor support both artifacts' policies remains the cross-policy guard; under B-ii it is never triggered by concern changes, under B-i only once at the v1 boundary.

## Treatment of current V1/V2 and the landed V2 implementation (Q18)

- **`factory-model:v1`**: no change proposed by this report. Under B-ii its artifacts remain valid composition artifacts; under B-i it remains a closed historical policy. Either way no existing fingerprint is rewritten. Whether its label may be extended is the sibling's question.
- **`factory-model:v2` (unreleased grammar)**: not released as specified. Its content maps 1:1 onto the `spatial-placement` concern (plant header → concern header; per-resource suffix → per-resource concern records keyed by `MachineId`). `factory-model-v2.md` is amendable before shipping by its own §10, so this is a rewrite of a design contract, not a supersession of released identity. Its footprint grammar should gain a shape discriminator (proving case 5).
- **Landed `FactoryModelV2`, `SpatialConfiguredResource`, `FactoryFloor`, `ResourcePlacement`, `ResourceFootprint`, `FactoryModelV2Validator`**: retained as proving evidence and as the natural implementation of the spatial-placement concern's value objects and predicates. The monolithic `FactoryModelV2` aggregate type would not become the published shape; its validator's delegate-then-add structure is the pattern every concern validator should follow. The compile-time separation from `FactoryModel` remains valuable until the composed model exists. No historical cost: no artifact or revision exists under `factory-model:v2`.
- **`engine-semantics:v1`**: behavior unchanged; §5 wording ("V2 model facts") becomes concern wording plus an explicit disposition table — a semantics-preserving amendment under ADR-0015's own rules (recording a rule without changing behavior).

## Adversarial analysis — author's self-challenge (not independent review)

Attempted falsifications of the surviving model, with outcomes:

- **"Every behaviorally relevant concern changes one inseparable canonical meaning."** Tested against resource-dependent duration (case 8), the strongest coupling found: an overriding concern touches the substrate's meaning. Outcome: not falsifying — the coupling is expressible as declared precedence inside the concern, and the substrate's authored duration remains an authored fact. Narrowed B's claim from "independent" to "independently present, with declared prerequisites and precedence".
- **"Optional presence makes identity ambiguous."** Tested with present-with-zero vs absent, and with duplicate/unknown tags. Outcome: not falsifying given the framing rules; the risk moves to the verifier ("must reject unknown tags") and is recorded as a required invariant.
- **"Engine applicability becomes less truthful."** Tested the silent-ignore hazard: a design carrying a behaviorally relevant concern that the Engine does not examine. Outcome: this hazard is *worse* under A (policy-number support implicitly covers every fact in the bundle) and is closed under B by the undeclared-⇒-inapplicable rule. The residual risk is an Engine version declaring INERT for a concern that is in fact result-affecting — governed by Engine v1 §1.1, not created by B.
- **"Cross-concern validation makes the separation nominal."** Counted dependency directions in the landed V2 validator and the brief's examples: every cross-concern predicate is owned by the referencing concern and reads only ids/values of its prerequisites. Outcome: not falsifying; dependency is a DAG, not a mesh. Would be falsified by a cyclic prerequisite need.
- **"A better compatibility matrix exists under A."** Enumerated per-addition costs: A needs a verifier, comparator support, Engine declaration, *and* a cross-policy migration classification per generation, plus forced re-fingerprinting of unaffected designs; B needs a tag grammar, its validator, a comparator branch, and an Engine disposition. Outcome: A is strictly more, not less.
- **"No real consumer benefits from partial sets."** Outcome: partially sustained as a *limitation*: exactly two sets have real standing (production-only; production+spatial), and the third orthogonal concern is research-stage. The structural failure of A (mandatory accretion + no lift) does not depend on a third landed consumer, but the *urgency* claim would be stronger with one. Recorded in limitations and reopening triggers.
- **"B is just A with an extra byte-level trick; the version label still bumps."** Under a strict reading of ADR-0006 (any membership change ⇒ new policy version), growing the recognized concern set is a policy change. Outcome: this is the genuine dependency on the sibling. Even under the strict reading, B preserves the byte-stability invariant if the prefix is not re-minted per concern (label ≠ prefix); if the sibling concludes every addition must re-prefix, B degenerates to "A with one fingerprint and better internals" and the byte-stability invariant dies. That is the report's clearest reopening condition.
- **"The substrate boundary is arbitrary."** Tested by asking what any interpretation must have to execute a designed production system: products→operations→ordered steps→eligible resources→durations→concurrency. Everything else in V1 (`capacityLiters`, `setupTime`, names as identity, allocated ids, list-order significance) is a current identity fact but not needed for executability. Outcome: sustained; also confirms that composition makes no substrate change free (reclassifying any of those alters v1 identity and is a separate decision).
- **Transferability challenge.** The result "compose optional typed concerns under one aggregate hash keyed by sorted tags" is not Arcogine-specific; it depends on (a) one publisher owning the aggregate and (b) absence being a meaningful claim. It fails where aspects are owned by different tools with independent lifecycles (AutomationML's case) — there, component identities are correct. Narrowest surviving claim: *one-owner aggregates with optional dimensions should fingerprint the aggregate and encode presence; component identity is warranted by independent ownership, not by modularity.*

## Surviving invariants

1. A canonical Factory design is one aggregate with one `ModelFingerprint`; concern structure is content of that identity, not a second identity.
2. The irreducible substrate is: products (each bound to one operation), operations with ordered steps, configured productive resources with stable identity and concurrency, explicit per-step eligibility, and step duration. Every other authored dimension is a concern.
3. Absence of a concern is an authored fact meaning "no claim in this dimension"; it is bytewise distinct from any present value; Arcogine never synthesizes, lifts, or defaults it; consumers distinguish *not modeled* (segment absent) from *zero* (authored `0`) and never see *unknown* or *default* in a published design.
4. Adding a recognized concern never changes the canonical bytes or fingerprint of a design that does not represent it.
5. Canonical form is deterministic by construction: unique tags, bytewise tag order, length framing, per-tag total grammars, decode→re-encode equality; unknown or duplicate tags are rejected, never skipped.
6. Validity composes over a prerequisite DAG; cross-concern predicates are owned by the referencing concern; no global combination table exists.
7. An Engine semantics version declares a disposition (REQUIRED / CONDITIONAL / INERT) for every concern it recognizes; a present undeclared concern or an absent required one makes the design inapplicable, failing explicitly before runtime mutation. Applicability is a declared predicate over represented concerns, not policy-number equality.
8. Controlled revisions and lineage are unaffected by concern-set changes; comparison reports concern presence changes explicitly and never reports a value on a side where the concern is absent.
9. Identity-bearing grammars evolve by adding recognized tagged alternatives, never by reinterpreting existing bytes.
10. A concern earns independent identity only by an independent ownership/evolution boundary that designs must reference — never by code modularity.

## Transferability and reuse

- **Arcogine-specific dependency:** one canonical publisher; content-derived identity already mandated by ADR-0004/0006; absence-as-claim already mandated by ADR-0014.
- **Potentially transferable result:** the narrowed claim in the self-challenge above (aggregate fingerprint + encoded presence for one-owner optional dimensions; component identity follows ownership, not modularity).
- **Boundary/counter-context:** multi-tool, multi-owner engineering aspects (AutomationML pattern) where component identity is correct.
- **Evidence level:** established for Arcogine by this investigation's proving cases; supported but not established externally (RFC 8949 technique; protobuf presence failure mode; AutomationML boundary evidence).
- **Reusable research asset:** the candidate × proving-case matrix and the disposition vocabulary (REQUIRED / CONDITIONAL / INERT / undeclared) as an applicability-declaration pattern.
- **Synthesis-seed candidate (nomination only):** "structural composition does not imply identity composition; presence is content; component identity tracks ownership boundaries." *Revisit when* another Arcogine domain (Governance requirements, Operational correspondence) faces optional-dimension identity, or when an independently owned Factory concern appears.

## What did not survive

- **Candidate A as the general evolution rule.** Fails orthogonal-concern cases because mandatory accretion plus no-lift makes every later concern inherit every earlier one. Survives only as "substrate meaning changes are whole-model changes".
- **Candidate C.** No proving case produced a statement one aggregate fingerprint cannot preserve; ADR-0008 and ADR-0014's alternatives already point the same way. Negative knowledge worth keeping: *component fingerprints are a storage/deduplication or ownership mechanism, not a semantic necessity.*
- **Candidate D.** Profiles as contracts are closed bundles under names; as identity they reproduce A; as version lines they multiply. Only "a declared required concern set" survives, and it needs no name.
- **Candidate E as a distinct structure.** "One identity with internally versioned aspects after an aspect earns durability" is B plus the sibling's lifecycle rule; nothing structural is added.
- **"Spatial layout is one concern forever."** Today's five facts are one concern because the accepted publication predicates couple them; geometry alternatives, topology, and storage zones are distinct concerns with prerequisites. The negative knowledge: *do not pre-split or pre-merge concerns by intuition; split where absence has an independent meaning and predicates close over the concern plus declared prerequisites.*
- **"Engine applicability by policy number is untruthful."** Overstated. It is truthful under A's closed bundles and becomes untruthful only when a bundle carries a fact the interpretation never examined — which is exactly the orthogonal case. The precise claim survived; the slogan did not.
- **The framing "V1 is old / V2 is current."** Neither candidate supports it; ADR-0014 d.9 already rejects it.

## Confidence and limitations

- **High** that B is structurally superior to A, C, and D on the brief's proving cases, and that one fingerprint is sufficient.
- **Moderate** on the exact canonicalization variant (B-i vs B-ii) and on any version-label wording: both depend on the sibling maturity result, which does not exist.
- **Limitation — consumer breadth:** only two concern sets have real standing; the third orthogonal concern with a landed consumer does not exist. The case against A is structural (ADR-0014 d.3/d.10/d.12 + ADR-0006 immutability), not empirical accumulation.
- **Limitation — nothing spatial has executed through the canonical model.** V2 exists as type + validator + tests; Engine transfer slices are held. The applicability analysis is therefore against specification and landed seams, not against observed runtime behavior.
- **Limitation — external:** the AAS metamodel could not be fetched and is not relied on; the IEC 62714 text itself was not fetched (association overview only).
- **Not inspected:** no open PRs existed at baseline; no surface named by the brief was unavailable.

## Unresolved unknowns

1. Whether growing a policy's recognized concern set is an amendment or a new policy version, and whether the v1 label may be extended byte-compatibly (B-ii) — **sibling**.
2. Whether `handlingTicks` (a per-transfer overhead with no geometric dependency) should eventually be separable from `ticksPerCell`/placement as a logical-transfer concern; no consumer evidence today, so it stays inside spatial placement.
3. The exact comparator representation of concern presence changes (new `SemanticChangeKind` vs per-concern `ChangedEntityRef` type).
4. Whether any INERT disposition is admissible for a concern that some other interpretation treats as result-affecting (Engine v1 §1.1 governs; not tested against a second Engine version because none exists).
5. Precedence semantics for overriding concerns (resource-dependent duration) — deferred until the qualification research is admitted.

## Durable consequences (stated, not performed)

- **ADR-0014**: supersede decisions 2, 3, 11, 12 (V2 as a complete closed policy; five mandatory additions; per-generation migration classification; `vN+1` on every unrepresentable fact) with a composition rule; generalize decision 10 (no lift) and decision 9 (absence is truthful, not degraded) to every concern; retain 1 (subject to sibling), 4, 5 (as the concern admission criterion: behaviorally relevant authored semantics), 6, 7, 8.
- **ADR-0006**: narrow "policy version identifies … semantic field membership" to "policy version identifies the composition grammar and substrate grammar; the recognized concern set grows additively; the bytes and meaning of every existing artifact are immutable" — exact wording contingent on the sibling.
- **`factory-model-v2.md`**: rewrite (pre-ship amendment) as the composition canonicalization contract plus the spatial-placement concern grammar with a shape discriminator; do not release as a closed `v2`.
- **`engine-semantics-v1.md`**: semantics-preserving amendment — concern wording in §5 and an explicit disposition table; ADR-0015 unchanged.
- **`factory-design.md`** §4.1/§8/§9 and **`isa-95-semantic-mapping.md`** §8: reconcile vocabulary to "concern"; no semantic change.
- **No change**: ADR-0003, ADR-0004, ADR-0008, ADR-0015, ADR-0016, Factory Resource Semantics.
- **Planning** (Factory Design capability; Spatial Runtime Consequences): replace the held "V2 canonical identity" and "V1/V2 coexistence" slices with "first composition policy + spatial-placement concern" and (under B-i only) "one policy-coexistence seam"; add a small concern-aware comparison slice; re-resolve the transfer-activation dependency map. Only after adversarial review and the sibling's result.
- **Research register**: this question stays ACTIVE until reconciliation; the CANDIDATE spatial/material-flow question consumes the concern boundary; qualification/hierarchy/storage questions gain the prerequisite/precedence obligations above.
- **Negative knowledge to preserve** (narrowest surface: the reconciled ADR's alternatives section): component identity ≠ modularity; profiles ≠ contracts; presence is content.
- The exact report/review artifacts need not remain readable after reconciliation if the invariants, matrix, and negative knowledge above are transferred; no retention contract is requested.

## Implementation implication

**No implementation.** This report admits nothing. The landed V2 slice stays as is; the held slices stay held. Implementation may be planned only after independent adversarial review of this exact revision, the sibling's decision-quality result, and separately reviewed ADR/specification reconciliation.

## Follow-up and reopening triggers

Reopen or narrow this conclusion if:

1. the sibling concludes that every addition to a released policy's recognized set must re-prefix artifacts (byte-stability invariant becomes unattainable; B degenerates toward A);
2. a concern requires cyclic prerequisites or a global combination table to validate;
3. a real consumer needs a concern authored/governed independently of designs and referenced by them (Candidate C trigger);
4. an Engine version cannot express its needs as per-concern dispositions (e.g., needs an interpretation of *absence* that is neither vacuous nor inapplicable — evidence the "concern" is really substrate);
5. a consumer needs a design with no production substrate (pure layout), challenging the substrate definition;
6. a second landed consumer subset (hierarchy/storage/qualification without spatial) does **not** appear within the horizon in which the next Factory policy must ship — weakening the urgency, not the structure.

Track in `docs/research/research-register.md` at reconciliation.

## Question-to-answer map

| Brief question | Answered in |
|---|---|
| 1 minimum substrate | Surviving invariant 2; self-challenge "substrate boundary" |
| 2 V1 facts: substrate vs implementation detail | Repository evidence (implementation); invariant 2; self-challenge |
| 3 spatial layout one or several concerns | Case narratives 2, 5, 6; "What did not survive" |
| 4 absence semantics | Executive conclusion 5; invariant 3 |
| 5 valid/invalid combinations | Cross-concern validation section |
| 6 whole-model vs component identity | Identity analysis; invariant 1 |
| 7 invariant justifying component identity | Identity analysis; invariant 10 |
| 8 canonical bytes without matrix | Canonicalization section |
| 9 determinism with optional/extensible concerns | Canonicalization section; RFC 8949 evidence; invariant 5 |
| 10 how Engine states required facts | Engine applicability section; invariant 7 |
| 11 applicability failure | Engine applicability section |
| 12 not modeled vs zero/default/unknown | Invariant 3; protobuf evidence |
| 13 richer geometry | Case 5; invariant 9 |
| 14 storage vs spatial independence | Case 3/14 narrative |
| 15 revisions/comparison across concern changes | Cross-concern validation and comparison section; invariant 8 |
| 16 when a concern merits durable identity | Identity analysis (Q7/Q16); sibling-conditional |
| 17 sibling constraint | Header; canonicalization variants; unknown 1 |
| 18 landed V2/validator | Treatment section |
| 19 ADR/spec/plan consequences | Durable consequences |
| 20 falsification evidence | Reopening triggers; self-challenge |

## Sources

Repository (at baseline `46389bc5d21e0c95ffa9366ce271f9d943d44b35`): `docs/product/charter.md`; `docs/architecture/{overview,factory-design,factory-resource-semantics,factory-model-v2,engine-semantics-v1,governance-conformance,isa-95-semantic-mapping,standards-alignment}.md`; ADR-0003, 0004, 0006, 0008, 0012, 0014, 0015, 0016; `docs/planning/{factory-design-capability,spatial-runtime-consequences,factory-design-game-consumer,factory-design-game-challenge-readiness}.md`; `docs/research/investigations/{factory-design-evolution,semantic-contract-maturity-durability,factory-design-game-vertical-slice}.md`; `product/domains/factory/src/main/java/com/arcogine/factory/{model/**,change/**,process/FactoryRuntime.java,process/FactoryHandler.java}`; `product/governance/src/main/java/com/arcogine/governance/{SemanticArtifact,SemanticArtifactVerifier,ControlledRevision,change/**}.java`; `product/types/src/main/java/com/arcogine/types/{ModelFingerprint,EngineSemanticsVersion}.java`; `product/consumer/challenge/src/main/java/com/arcogine/challenge/{FactoryFloorConstraint,admissibility/**}.java`; `product/interfaces/web/src/components/dashboard/FactoryFlow.tsx`; merged PRs #248, #252, #299, #352.

External (verified in session, 2026-09-18): IETF RFC 8949, *CBOR*, Dec 2020, §4.2.1, <https://www.rfc-editor.org/rfc/rfc8949.html>; Protocol Buffers, *Field Presence*, <https://protobuf.dev/programming-guides/field_presence/>; AutomationML e.V., *AutomationML* overview (IEC 62714 / CAEX IEC 62424 / COLLADA ISO/PAS 17506 / PLCopen XML), <https://www.automationml.org/about-automationml/automationml/>.

Background, unverified, not load-bearing: IDTA-01001 *Asset Administration Shell Part 1: Metamodel* (fetch failed).

## Evidence coordinate (active custody)

- Workspace branch: `research/factory-model-semantic-composition`
- Report path: `workspace/research/investigations/factory-model-semantic-composition-report.md`
- Exact report commit SHA: recorded in the handoff after commit; branch tip is not the artifact identity.
