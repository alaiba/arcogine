# Research handoff — Engine applicability to optional-record Factory policies

You are operating as Arcogine's **Researcher** for one bounded, high-risk investigation. Follow `AGENTS.md`, `.github/agents/researcher.agent.md`, and `docs/development/researching.md` exactly.

## Repository and baseline discipline

Repository: `alaiba/arcogine`.

This handoff was prepared from live `main` at:

`c6236c9bb02318ae602c59a221cfb12d1ffad685`

At the start of the investigation, resolve live `main` again and record its exact SHA as the research baseline. Do not assume this handoff baseline is still current. Treat this workspace branch as research custody, not landed repository truth.

Use this branch as the bounded research-evidence workspace unless a concrete repository constraint requires otherwise. Preserve handed-off commits; do not rebase or force-push evidence history.

## Task

Execute the READY research question **Engine applicability to optional-record Factory policies** from:

`docs/research/investigations/engine-evolution.md#engine-applicability-to-optional-record-factory-policies`

Produce a decision-quality research report. Do **not** implement the answer, change canonical architecture/specifications, alter planning state, or activate spatial runtime behavior.

### Bounded question

Determine which Engine semantics identity may execute artifacts of the reconciled `factory-model:v2` policy, whose grammar is:

- required production records; plus
- an optional, complete spatial record.

Determine specifically whether `engine-semantics:v1` can truthfully be stated as applicable to those V2 artifacts **with the spatial record present and with it absent** without changing the fixed definition of `engine-semantics:v1`.

### Decision at stake

The result determines whether spatial runtime activation can remain attributed to `engine-semantics:v1` or requires a distinguishable Engine semantics identity.

Keep these two predicates separate:

1. a Factory artifact is valid/publishable under a Factory policy;
2. an Engine interpretation is applicable to that policy and represented content.

The concluded Factory-composition research intentionally did not answer Engine applicability.

## Risk

Classify this investigation as **High risk**.

The answer affects semantic identity, compatibility, determinism, accepted input/refusal behavior, and attribution. A genuinely independent adversarial research review is required before the conclusion can serve as decision-quality evidence for Engine architecture/specification reconciliation.

Do not perform the independent adversarial review in the same research run and do not claim that it has occurred unless an actual independent review artifact for the exact report revision exists.

## Read first

Read these current-revision authorities in full or far enough to establish their load-bearing contracts:

1. `AGENTS.md`
2. `.github/agents/researcher.agent.md`
3. `docs/development/researching.md`
4. `docs/research/research-register.md`
5. `docs/research/investigations/engine-evolution.md`, especially the named applicability section
6. `docs/architecture/overview.md`, especially **Semantic evolution and support**
7. `docs/development/semantic-contract-support.md`
8. `docs/architecture/factory-design.md`, especially **Factory semantic evolution**
9. `docs/architecture/factory-model-v2.md`
10. `docs/architecture/engine-semantics-v1.md`
11. `docs/research/investigations/factory-model-semantic-composition.md`
12. `docs/planning/spatial-runtime-consequences.md`
13. `docs/planning/factory-simulation-engine-readiness.md`

Then inspect implementation and executable evidence that establish what is currently represented, published, executed, attributed, or refused. At minimum consider:

- `product/types/src/main/java/com/arcogine/types/EngineSemanticsVersion.java`
- `product/types/src/test/java/com/arcogine/types/EngineSemanticsVersionTest.java`
- `product/domains/factory/src/main/java/com/arcogine/factory/model/FactoryModelVersion.java`
- `product/domains/factory/src/main/java/com/arcogine/factory/model/FactoryModelPublisher.java`
- `product/domains/factory/src/main/java/com/arcogine/factory/model/FactoryRuntimeAssembler.java`
- `product/domains/factory/src/main/java/com/arcogine/factory/model/v2/FactoryModelV2.java`
- `product/domains/factory/src/main/java/com/arcogine/factory/model/v2/FactoryModelV2Validator.java`
- `product/domains/factory/src/test/java/com/arcogine/factory/model/v2/FactoryModelV2Test.java`
- `product/domains/factory/src/test/java/com/arcogine/factory/model/v2/FactoryModelV2ValidatorTest.java`
- `product/domains/factory/src/test/java/com/arcogine/factory/process/EngineSemanticsIdentityAcceptanceTest.java`
- the existing `EngineSemanticsV1*` conformance tests under `product/domains/factory/src/test/java/com/arcogine/factory/process/`

Do not treat this list as exhaustive. Search semantic neighbors and inspect any current path that materially bears on accepted inputs, refusals, Engine identity attribution, Factory publication identity, or spatial execution semantics.

## Required repository searches

Perform quick repository searches for at least these concepts and plausible old assumptions:

- `engine-semantics:v1`
- `factory-model:v2`
- `EngineSemanticsVersion`
- `FactoryModelVersion`
- `FactoryModelV2`
- `FactoryRuntimeAssembler`
- applicability / supported / unsupported / refusal / reject
- optional spatial record / spatial absent / spatial present
- `ticksPerCell`
- `handlingTicks`
- model fingerprint / published model / runtime provenance
- any wording that implies V1 Engine applicability to V2 artifacts, or that equates Factory publication validity with Engine support

Fetch any material search hit at the exact research-baseline commit before relying on it.

Use Git/PR history only where it materially explains what definition was fixed or whether a statement is a correction versus a semantic expansion. Do not let history override current authority.

## Fixed constraints from the brief

Treat these as constraints to test against evidence, not conclusions to evade:

- Whole-definition fixation includes rules no fixture has exercised and refusal behavior. The fact that spatial execution has not shipped does not itself authorize changing `engine-semantics:v1` in place.
- An in-place applicability statement is admissible only if it is demonstrably a **correction**: the complete already-defined contract is preserved, including accepted input domain, interpretation, outputs, refusals, and referenced semantic definitions.
- If applicability changes that fixed contract, use a distinguishable Engine semantics identity and leave `engine-semantics:v1` exactly as defined.
- This investigation applies the same-label amendment problem to this one concrete case only. Do not generalize the result into a universal amendment policy unless the evidence independently requires reopening that broader question.
- `factory-model:v2` publication identity release and spatial runtime activation remain blocked until this question is reconciled.
- If the required Engine identity transition materially changes the relative merit of the reconciled Factory composition boundary, surface that as the documented reopening trigger rather than silently changing Factory semantics.

## Candidate models

Start with at least these two candidates. You may add another candidate only if repository or external evidence exposes a genuinely distinct viable model.

### Candidate A — Preservation proof and correction

Demonstrate that explicitly stating `engine-semantics:v1` applicability to reconciled V2 is only a correction of an already-fixed contract:

- V2 + spatial record present: existing Engine transfer rules operate over the same defined five spatial facts;
- V2 + spatial record absent: determine whether the already-defined contract necessarily entails the relevant non-spatial/no-transfer behavior;
- accepted inputs, outputs, interpretation, rejection/refusal behavior, and referenced semantic definitions remain unchanged.

The burden is affirmative proof of preservation, not absence of a contradictory fixture.

### Candidate B — Distinguishable Engine identity

Keep `engine-semantics:v1` fixed and define a distinguishable Engine interpretation identity that explicitly names the Factory policies and represented content it supports.

Determine the minimum semantic difference that forces the new identity and what compatibility/provenance consequences follow.

## Required proving cases

Derive the final proving-case set from the candidates, but ensure it discriminates at least the following situations:

1. `factory-model:v1` artifact under current `engine-semantics:v1`.
2. Reconciled `factory-model:v2` with a complete spatial record.
3. Reconciled `factory-model:v2` with the spatial record absent.
4. Invalid/incomplete spatial representation that the Factory policy itself rejects.
5. A valid Factory artifact whose policy/content the Engine identity does not support.
6. Result attribution/provenance when two Factory policies can encode equivalent non-spatial production semantics.
7. Refusal behavior: identify where unsupported policy/content must be rejected rather than silently interpreted.
8. Interaction with the already-fixed transfer rules, including the five spatial facts named by Engine Semantics v1.
9. Historical interpretability: whether previously attributed `engine-semantics:v1` results keep exactly the same meaning under the candidate.
10. A counterfactual future Factory policy adding optional authored content irrelevant to Engine v1, to test whether the proposed applicability rule accidentally makes Engine support track whole-model version labels rather than represented content/declared support.

Do not assume the expected answer from these cases. Use them to falsify candidates.

## Evidence requirements

Before reaching a conclusion, produce:

1. **V1 attribution/support inventory**
   - every retained or accepted record type you can find that carries or depends on `engine-semantics:v1`;
   - current declared support boundaries;
   - implementation paths that instantiate, report, or enforce the identity;
   - any explicit refusal behavior and any important absence of refusal enforcement.

2. **Before/after accepted-input matrix**
   Compare, for each candidate:
   - Factory policy identity;
   - represented content required/optional;
   - whether the artifact is Factory-valid;
   - whether the Engine interpretation accepts it;
   - result-affecting rules invoked;
   - refusal/rejection behavior;
   - output/provenance identity.

   Include both V2 spatial-present and V2 spatial-absent cases.

3. **Definition-preservation analysis**
   For Candidate A, explicitly prove or fail to prove preservation of:
   - accepted input domain;
   - result-affecting interpretation;
   - outputs/observations that are part of the fixed contract;
   - refusal semantics;
   - referenced semantic definitions.

   “No existing test fails” is insufficient.

4. **Fixture requirements**
   Identify the supported-input, interaction, identity/provenance, and rejection fixtures the selected interpretation identity requires. Distinguish:
   - fixtures needed to prove the research conclusion;
   - fixtures that would belong to later implementation/reconciliation.

5. **Compatibility/provenance consequences**
   Explain what each candidate means for:
   - historical results attributed to `engine-semantics:v1`;
   - replay/interpretability;
   - consumer comparison claims;
   - support/retirement obligations;
   - V2 publication and spatial-runtime activation.

## External evidence

Do not collect external material merely to make the report look comprehensive.

Use external evidence only if it can materially discriminate between the candidates, expose a failure mode, or establish a compatibility/provenance consequence that the repository cannot answer itself. Verify provenance/version for any load-bearing external source and clearly label unverified background.

This is primarily a repository-semantic identity question; repository evidence may be sufficient.

## Research method

Follow `docs/development/researching.md` and the Researcher contract:

- distinguish **Repository fact**, **External evidence**, **Inference**, and **Recommendation/proposed decision**;
- include the simplest/current/no-new-abstraction candidate;
- attempt to falsify each live candidate;
- evaluate every candidate against every proving case;
- state confidence and what evidence would change the conclusion;
- stop when more evidence no longer changes the candidate set, a proving case, confidence, or durable consequence.

Do not confuse an implementation convenience with a semantic necessity.

## Non-goals

Do not:

- edit `docs/architecture/engine-semantics-v1.md` or any other canonical architecture/specification as part of this research run;
- change Factory V2 publication policy;
- activate spatial runtime paths;
- create a new Engine identity in production code;
- modify planning status;
- settle the universal same-label amendment question;
- broaden into scheduler, routing, transport-network, congestion, resource-orientation, or pathfinding design;
- redesign Factory semantic composition;
- perform PR review or Consistency review;
- treat this research report as accepted architecture merely because it exists.

## Expected result

The report must reach one of these substantive outcomes, with evidence:

- Candidate A survives: `engine-semantics:v1` applicability to the specified V2 cases can be recorded as a correction because the complete fixed definition is preserved; or
- Candidate B survives: V2 support changes the fixed Engine contract enough to require a distinguishable Engine semantics identity; or
- neither candidate is decision-quality yet, with the exact missing evidence stated.

Do not manufacture certainty.

The recommended durable destination is a later **Engine architecture/specification reconciliation** that names which Engine identity executes which Factory policies and represented content. The research run itself must not perform that reconciliation.

## Report and persistence

Use `docs/research/report-template.md`.

Persist the completed report under a semantically named path such as:

`workspace/research/investigations/engine-applicability-optional-record-factory-policies.md`

Commit the completed report in this bounded workspace and return:

- research baseline SHA;
- workspace branch;
- exact report commit SHA;
- report path;
- risk classification;
- conclusion and confidence;
- unresolved unknowns;
- whether independent adversarial review is required and whether it has occurred.

Because this is high risk, the normal next phase after a complete report is an **independent adversarial research review of the exact report commit**. Reuse the same workspace for that review when practical, but use a genuinely independent reviewer/session and bind the review to the exact report SHA.

Do not mark the research question `CONCLUDED` merely because the report exists. Conclusion requires the durable consequence to be reconciled, or an explicit durable no-action result, under the repository research operating model.
