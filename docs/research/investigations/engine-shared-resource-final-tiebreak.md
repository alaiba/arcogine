# Engine Shared-Resource Final Tie-Break Research

> **Status:** READY  
> **Scope:** Test whether final `MachineId` ordering remains an acceptable result-affecting tie-break when eligible resources include capacity shared across multiple operation steps  
> **Authority:** Research only; current [Engine semantics](../../architecture/engine-semantics.md) remain normative until any conclusion is reconciled

## Question

> When otherwise tied eligible resources differ in how broadly their capacity can serve future operation steps, is final `MachineId` ordering an acceptable result-affecting tie-break, or should Arcogine preserve a different explicit invariant?

## Decision at stake

Whether the Engine should:

- retain final `MachineId` ordering and explicitly accept identity-order sensitivity;
- use a separate authored priority distinct from resource identity;
- use an order-invariant deterministic local rule;
- preserve relatively scarce/flexible capacity under a precisely bounded objective; or
- adopt another narrowly evidenced final tie-break.

This question does **not** reopen the entire dispatch policy or the concluded `combinedQueueDepth` ranking.

## Current evidence boundary

Current Engine research deliberately retained:

- online and immediate-acceptance preference;
- exact `combinedQueueDepth` ranking;
- unbound shared waiting/reselection;
- final `MachineId` tie-breaking.

That conclusion was conservative retention, not a claim that identity ordering is physically meaningful or globally optimal. Its reopening triggers already include a supported consumer objective and changed heterogeneous resource capabilities.

Current Factory semantics also permit one configured resource to be eligible for more than one operation step. In that case, assigning present work to a flexible resource can consume capacity that another step could otherwise use later.

## Scope

In scope:

- only cases that reach the existing final tie-break after prior ranking keys are equal;
- deterministic resource-selection behavior for explicit multi-eligible/shared-capacity routings;
- semantic sensitivity to permutations of resource identity/order;
- candidate information horizons that use current supported state unless a candidate explicitly proves a missing fact is required;
- interaction with deterministic replay and Factory resource identity.

Out of scope:

- replacing `combinedQueueDepth`;
- global scheduling optimization;
- queue sequencing beyond FIFO;
- stochastic dispatch;
- spatial transfer policy unless a later concrete proving case requires it;
- resource-dependent speed unless separately admitted Factory semantics make it relevant.

## Candidate rules

At minimum test:

1. **Retain `MachineId`.** Identity order is an explicit result-affecting interpretation rule.
2. **Authored priority.** A separate Factory/consumer-authored priority decides otherwise tied resources.
3. **Order-invariant local rule.** A rule based only on semantically relevant current resource/work facts, such that relabeling otherwise equivalent resources does not change the result.
4. **Flexibility/scarcity preservation.** Prefer the resource whose future applicability is less broadly useful, or preserve the more flexible resource, under a precisely stated local objective.
5. **Another bounded deterministic rule** only when discriminating evidence supports it.

## Proving cases

A decision-quality investigation must include:

1. **Pure symmetry control.** Two genuinely interchangeable tied resources; identity permutation should reveal whether any result difference is semantically observable.
2. **Shared-versus-dedicated tie.** One dedicated resource and one resource eligible for the current plus a downstream step, with prior ranking keys equal.
3. **Mirror workload.** A one-variable change that reverses which immediate choice is beneficial, preventing adoption of a heuristic that only wins one construction.
4. **Scarce-resource protection.** A case where preserving a flexible or scarce resource benefits later work.
5. **No-benefit control.** A case where extra flexibility has no future value and a sophistication-biased rule must not claim universal improvement.
6. **Identity permutation sweep.** Hold all non-identity authored facts constant and permute resource IDs/order to measure which outcomes change under each candidate.
7. **Deterministic replay.** Every candidate must be reproducible from explicit inputs and a fixed interpretation.

## Evidence expectations

Use current Engine specification, dispatch conformance tests, Factory eligibility semantics and executable generated/parameterized cases. Report multiple objective dimensions where relevant rather than treating makespan as automatically authoritative.

A candidate replacement must identify:

- the selecting objective or invariant;
- its exact information horizon;
- cases where it improves and regresses;
- fairness/starvation implications where applicable;
- whether it requires new authored Factory facts.

## Falsification and rejection conditions

Reject a replacement when it:

- merely substitutes another arbitrary stable ordering without a stronger invariant;
- improves one selected workload while regressing a valid mirror with no stated objective to choose between them;
- relies on hidden future knowledge not present in its declared information horizon;
- smuggles a generic scheduler or optimizer into the final tie-break;
- weakens deterministic replay.

## Exit criteria

Conclude when either:

- current `MachineId` ordering is justified as the bounded final rule with explicit accepted sensitivity; or
- a better-supported deterministic invariant is selected with enough evidence to change Engine semantics and conformance fixtures.

Treat a recommendation that changes result-affecting Engine semantics as high-risk research and require independent adversarial review before canonical reconciliation.
