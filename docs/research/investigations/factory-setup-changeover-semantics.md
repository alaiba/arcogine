# Factory Setup and Changeover Semantics Research

> **Status:** READY  
> **Scope:** Determine what setup/changeover means in Arcogine, whether the current scalar `ConfiguredResource.setupTime` truthfully represents that meaning, and which Factory/Engine facts would be required for executable setup behavior  
> **Authority:** Research only; current Factory and Engine specifications remain authoritative until any conclusion is reconciled

## Question

> What setup/changeover semantics, if any, should Arcogine represent, and does the current scalar `ConfiguredResource.setupTime` truthfully represent them?

## Decision at stake

Whether the current Factory grammar should:

- remove `setupTime` until a concrete setup concept is justified;
- retain it with a precisely bounded executable meaning;
- replace it with operation-resource setup facts; or
- replace it with state/sequence-dependent changeover semantics that explicitly separate authored rules from mutable runtime setup state.

The result may require no setup capability at all. Research must not preserve the current field merely because it already participates in canonical model content.

## Current evidence boundary

Current repository behavior establishes all of the following:

- `ConfiguredResource.setupTime` is an authored Factory field and participates in canonical content;
- runtime assembly copies the value onto the runtime machine and current resource observations expose it;
- current Engine processing duration comes from `OperationStepDefinition.duration`;
- current resource selection, queueing, job start/completion, and scheduling do not consume `setupTime`; and
- [Factory Resource Semantics](../../architecture/factory-resource-semantics.md) already distinguishes immutable design facts from mutable runtime setup state.

That combination makes setup a current semantic uncertainty rather than a speculative future feature.

## Candidate models

At minimum test these candidates:

1. **Remove/defer.** Arcogine represents no setup fact until a concrete consumer requires one.
2. **Fixed per-processing overhead.** One configured resource contributes a fixed setup duration whenever it starts admitted work.
3. **Operation-resource setup.** Setup depends on the selected resource and operation/step but not on prior resource state.
4. **State/sequence-dependent changeover.** Setup depends on a transition such as prior product/tool/material/setup state to the next required state.
5. **Another narrower model** only when repository or external evidence establishes a distinct invariant not covered above.

Do not assume that the current field name or scalar shape deserves preservation.

## Scope

In scope:

- authored setup/changeover facts;
- mutable setup state needed to execute those facts;
- when setup is incurred;
- units and legal value domain;
- interaction with processing duration, eligibility, resource selection and scheduling;
- observability/provenance required to explain setup effects;
- whether setup belongs on a resource, operation-resource relation, transition/state relation, or nowhere yet.

Out of scope:

- maintenance and breakdown/repair;
- human staffing;
- spatial transfer;
- generic scheduling-policy redesign except where a candidate setup model creates an unavoidable result-affecting dependency;
- reusable equipment-type identity unless a surviving setup contract independently proves that need.

## Proving cases

A decision-quality answer must discriminate at least these cases:

1. **Repeated same work.** Two consecutive jobs requiring no meaningful transition must establish whether setup repeats, is reused, or is absent.
2. **Changed work.** A transition between materially different products/operations/tooling states must reveal whether a scalar resource field can represent the required behavior.
3. **Asymmetric changeover.** A case where A -> B and B -> A would reasonably differ must test whether directional transition semantics are necessary.
4. **Parallel resources.** Two eligible resources with different setup state/history must establish how setup interacts with deterministic selection.
5. **Fresh start/reset.** The initial setup condition and reset semantics must be explicit rather than inferred from runtime object construction.
6. **No-setup control.** A model with no represented setup must remain behaviorally equivalent to current setup-free execution.

## Evidence expectations

Use current Factory grammar/code, Engine semantics and conformance tests as mandatory repository evidence. Seek external manufacturing/scheduling evidence where it materially distinguishes fixed setup, operation-dependent setup and sequence-dependent changeover semantics.

For each surviving candidate, state:

- exact authored facts;
- exact mutable runtime state;
- when simulated time is added;
- how deterministic replay is preserved;
- what supported evidence explains the result;
- whether current `setupTime` can represent the candidate without misleading semantics.

## Falsification and rejection conditions

Reject a candidate when it:

- cannot state when setup is incurred;
- conflates immutable setup rules with mutable current setup state;
- requires hidden scheduler state or ambient policy to determine results;
- makes materially different changeovers indistinguishable where the candidate claims to support them;
- introduces reusable resource-type identity merely to store setup defaults;
- cannot preserve deterministic interpretation from explicit inputs.

## Exit criteria

Conclude when one candidate is sufficiently specified and evidenced to reconcile into Factory/Engine architecture, or when the evidence supports removing/deferring executable setup semantics for now.

The durable destination is:

- [Factory model](../../architecture/factory-model.md) and [Factory Resource Semantics](../../architecture/factory-resource-semantics.md) for authored setup facts;
- [Engine semantics](../../architecture/engine-semantics.md) for result-affecting execution;
- current planning only after those meanings are settled.

A conclusion that changes result-affecting Engine behavior should receive independent adversarial review before architecture reconciliation.
