# Research handoff: Factory Model Semantic Composition

Operate as the Arcogine **Researcher** in standard-investigation mode for repository `alaiba/arcogine`.

Execute the maintained brief:

`docs/research/investigations/factory-model-semantic-composition.md`

The bounded question is:

> How should Arcogine represent independently applicable authored Factory semantics so that adding a new concern does not automatically turn feature accumulation into a linear whole-model `factory-model:vN` progression?

At handoff creation time, `main` was `46389bc5d21e0c95ffa9366ce271f9d943d44b35`. Treat that only as historical handoff context. Resolve live `main` at investigation start and record the exact research-baseline SHA.

## Required operating model

Read and follow, in order:

1. `AGENTS.md`
2. `.github/agents/researcher.agent.md`
3. `docs/development/researching.md`
4. `docs/research/research-register.md`
5. `docs/research/investigations/factory-model-semantic-composition.md`
6. `docs/research/report-template.md`

The register currently marks this question `Critical-path / READY`, and the brief marks it **High risk**. Verify both on current `main`.

This run is research only. Do not change Factory/Engine implementation, Accepted ADRs, current architecture, or implementation planning. Do not promote the conclusion yourself.

Do not use `docs/research/synthesis-seeds.md` as routine initial grounding. Reach the investigation result independently first, per the research operating model.

## Initial repository search

After reading the required operating documents, perform a quick repository search across maintained docs, source, tests, and relevant history for the main concepts, including:

- `FactoryModel`
- `FactoryModelV2`
- `factory-model:v1`
- `factory-model:v2`
- `ModelFingerprint`
- `canonical bytes`
- `semantic policy`
- `optional concern`
- `spatial`
- `storage`
- `material flow`
- `topology`
- `qualification`
- `capability`
- `hierarchy`
- `controlled revision`
- `Engine applicability`
- `V1/V2 coexistence`
- `spatial runtime consequences`

Treat repository search as discovery only. Read load-bearing evidence at the exact target revision before relying on it.

## Preserve the current authority boundary

The investigation starts from these repository facts and must not silently rewrite them:

- ADR-0006 and ADR-0014 remain authoritative until separately superseded.
- Current Factory v1 semantics remain current.
- `FactoryModelV2` and `FactoryModelV2Validator` are landed proving evidence.
- V2 canonical bytes, fingerprint policy registration, and V1/V2 coexistence are not yet released.
- Current planning explicitly holds V2 identity/coexistence and dependent spatial hardening while this question and the sibling maturity question remain unresolved.
- Engine spatial consequences are not yet fully implemented.

Treat implementation shape as evidence, not architectural truth.

## Investigation requirements

Follow the maintained brief completely rather than replacing its structure.

In particular:

- determine the irreducible semantic substrate for a canonical Arcogine Factory design;
- distinguish required substrate facts from optional authored semantic concerns;
- define what **absence** of an optional concern means without inventing zero/default/unknown facts;
- test whether one complete Factory design should retain one `ModelFingerprint`;
- reject independent component identity unless it preserves a necessary invariant or genuine independent evolution boundary that one aggregate identity cannot preserve;
- define deterministic canonicalization/publication requirements for optional concern sets without creating a combinatorial version matrix;
- test whether Engine applicability should depend on represented semantic facts/capabilities rather than directly on a whole-model policy number;
- define how applicability fails when required authored semantics are absent;
- test cross-concern validation without assuming all concerns must collapse into one monolith;
- explain controlled-revision comparison/history when represented concern sets change;
- distinguish semantic composition from code modularity and from permanent version/durability commitments.

Do not create a generic extension/plugin framework merely because future concerns are imaginable.

## Candidate discipline

Evaluate every serious candidate required by the brief:

- linear whole-model policies;
- one canonical aggregate with typed optional semantic concerns;
- independently identified/versioned semantic components;
- supported Factory profiles/capability sets;
- a materially justified hybrid only if evidence requires it.

Include the simplest model that satisfies the evidence. Do not manufacture extra lifecycle states, identities, profiles, or extension mechanisms for symmetry.

Build an explicit candidate x proving-case evaluation. Apply every serious candidate to every mandatory proving case in the brief, including:

- core-only deterministic production;
- current spatial transfer;
- logical storage without geometry;
- spatial storage zones;
- richer geometry;
- explicit material-flow topology;
- contended transport resources;
- qualification/resource-dependent performance;
- hierarchy/work-center/resource-pool scope;
- cross-consumer partial models;
- historical revision/comparison;
- cross-concern validation.

Record which candidates survive, fail, or survive only with qualifications, and why.

## Required semantic distinctions

Keep these distinct throughout the report:

```text
Factory design identity
    exact authored aggregate that was published

semantic concern structure
    which authored dimensions the aggregate represents

semantic-contract maturity
    how permanently Arcogine has committed to the meaning of those dimensions

controlled revision identity
    which governed historical occurrence referenced the design
```

Do not infer that structural composition requires multiple fingerprints.

Do not infer that one whole-model fingerprint requires one monolithic, ever-growing schema generation.

Also distinguish:

- authored Factory facts;
- Engine interpretation of those facts;
- optional semantic concern presence;
- runtime mutable state;
- reusable resource/specification concepts;
- configured productive-resource identity.

## Engine applicability

Explicitly test the brief's applicability hypothesis:

```text
Engine interpretation
    requires certain authored semantic facts

Factory design
    either supplies those facts or does not

applicable
    only when the required authored semantics are represented and valid
```

Determine whether this is more truthful and maintainable than binding Engine applicability directly to `factory-model:vN`.

A production-only Factory must be allowed to remain a first-class valid design if the evidence supports that conclusion; do not classify it as obsolete merely because a spatial Engine interpretation cannot consume it.

## Repository evidence

At minimum inspect and reconcile:

- `docs/product/charter.md`
- `docs/architecture/overview.md`
- `docs/architecture/factory-design.md`
- `docs/architecture/factory-resource-semantics.md`
- `docs/architecture/factory-model-v2.md`
- `docs/architecture/engine-semantics-v1.md`
- `docs/architecture/governance-conformance.md`
- `docs/architecture/isa-95-semantic-mapping.md`
- `docs/architecture/standards-alignment.md`
- ADR-0003, ADR-0004, ADR-0006, ADR-0014, ADR-0015;
- `docs/planning/factory-design-capability.md`
- `docs/planning/spatial-runtime-consequences.md`
- `docs/research/investigations/factory-design-evolution.md`
- current Factory model/canonicalization/fingerprint implementation and tests;
- current V2 type/validator implementation and tests;
- Governance semantic comparison and controlled-revision implementation/tests;
- current consumers/adapters sufficient to determine whether partial concern sets reflect real usage rather than hypothetical modularity.

Inspect relevant repository/GitHub history where needed to explain why V2 was introduced, what coexistence machinery remains unimplemented, or what current consumers actually depend on.

## External evidence

Use external manufacturing, schema, information-model, or digital-twin sources only where they materially discriminate among candidates.

Potentially useful evidence may show separation among process structure, spatial geometry, hierarchy, transport topology, storage, or qualification. Treat such models as evidence about possible boundaries, not authority to import an external ontology wholesale.

Prefer primary or normative sources. For each load-bearing external source, record provenance, what it establishes, and where the analogy to Arcogine breaks.

## Questions and exit criteria

Treat every item under **Questions the report must answer** and **Exit criteria** in the maintained brief as mandatory.

In particular, the report must answer:

- minimum Factory substrate;
- which V1 facts are substrate versus implementation detail;
- whether spatial layout is one concern or several;
- semantics of concern absence;
- valid/invalid concern combinations;
- whole-model versus component identity;
- canonicalization determinism;
- Engine-required authored semantics and applicability failure;
- not-modeled versus zero/default/unknown;
- geometry/storage/topology/qualification/hierarchy evolution;
- controlled revision/comparison across concern-set changes;
- whether any concern ever merits independent durable identity;
- treatment of already-landed `FactoryModelV2`/validator;
- exact ADR/spec/plan reconciliation consequences;
- falsification/reopening evidence.

A compact question-to-answer/evidence map is encouraged if it helps prove that none of the required questions was skipped.

## Coordination with Semantic Contract Maturity and Durability

The sibling investigation:

`docs/research/investigations/semantic-contract-maturity-durability.md`

may run in parallel.

This composition investigation may reach structural conclusions independently. However:

- do not declare any core/aspect/profile/component **durable**;
- do not assign permanent version identity;
- do not commit Arcogine to historical compatibility support;

unless the sibling maturity investigation has reached a decision-quality result and this report explicitly incorporates that result.

At investigation start and again before finalizing, check the current status of the sibling maturity research. A branch, prompt, draft, or unreviewed report is not decision-quality evidence. If no reviewed lifecycle conclusion exists yet, keep durability/version recommendations explicitly conditional rather than inventing the answer here.

## Falsification

Actively try to falsify the compositional hypothesis.

Reject or narrow it if evidence shows, for example, that optional concern presence creates ambiguous identity, canonicalization cannot remain deterministic, Engine applicability becomes less truthful or substantially more complex, cross-concern validation collapses the separation, independent concerns produce a worse compatibility matrix, or no real consumer benefits from partial concern sets.

Likewise try to falsify the linear-policy model. Narrow or reject it if real consumers repeatedly need independent concern subsets and orthogonal concerns force unrelated semantics into permanent whole-model generations.

Reject independent component identities unless they preserve an otherwise unrepresentable statement or evolution boundary.

Record negative evidence and failed candidates, not just the surviving model.

## Evidence discipline

Keep these categories visibly distinct:

- **Repository fact**
- **External evidence**
- **Inference**
- **Recommendation / proposed decision**

If evidence contradicts the brief's motivating composition hypothesis, follow the evidence. Retaining the current linear policy is a valid outcome.

## Report requirements

Use `docs/research/report-template.md`.

The completed report must include at least:

- exact live-`main` research baseline SHA;
- bounded question and decision at stake;
- `Risk: High`;
- scope and non-goals;
- executive conclusion and confidence;
- candidate comparison;
- candidate x proving-case evaluation;
- repository evidence;
- external evidence actually used;
- identity/fingerprint analysis;
- Engine applicability analysis;
- cross-concern validation and controlled-revision analysis;
- treatment of current V1/V2 and landed V2 implementation;
- explicit author's self-challenge, clearly not mislabeled independent review;
- surviving invariants;
- failed candidates and counterevidence;
- unresolved unknowns;
- reopening triggers;
- smallest recommended durable destination, including `no change` if that survives;
- explicit statement that the report performs no implementation or authority transition.

If live `main` moves materially during the investigation, re-ground before finalizing and state whether the movement changes the conclusion.

Stop gathering evidence when additional material no longer changes a candidate, proving case, invariant, confidence level, or durable consequence.

## Evidence custody

Continue using this finite research workspace:

`research/factory-model-semantic-composition`

Persist the completed report at:

`workspace/research/investigations/factory-model-semantic-composition-report.md`

Use another semantic filename only if current repository state provides a concrete reason.

Commit and push the completed report. The exact commit SHA plus path is the report artifact identity; branch tip is not sufficient.

Once a completed report revision is handed off:

- do not amend or rewrite it;
- do not rebase or force-push away its commit;
- preserve the handed-off SHA through later history-preserving synchronization;
- treat any changed report as a new evidence revision.

Do not merge the workspace merely because the report exists.

Do not mark the research `CONCLUDED` merely because a report exists. This is high risk: a genuinely independent adversarial review of the exact report revision is required before architecture, fingerprint policy, Engine applicability, or implementation planning can be promoted from the result.

If persistence cannot be completed, return `EVIDENCE PERSISTENCE BLOCKED` and identify the missing capability rather than presenting the handoff as complete.

## Final response

After persistence succeeds, return only a compact handoff containing:

- live-`main` research baseline SHA;
- 2-4 sentence conclusion;
- confidence;
- workspace branch;
- exact report commit SHA;
- report path;
- adversarial-review status;
- sibling maturity-research status relevant to durability/version conclusions;
- material inspection limitation, if any;
- next required action: independent adversarial review of that exact report revision.

Do not paste the complete report into chat after it has been persisted.
