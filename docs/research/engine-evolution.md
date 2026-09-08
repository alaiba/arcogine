# Engine Evolution Research

> **Status:** CANDIDATE  
> **Scope:** Result-affecting Engine questions intentionally outside the currently accepted deterministic runtime semantics  
> **Authority:** Research only; current Engine semantics remain fixed by accepted architecture and executable evidence

## Purpose

The Engine implementation must not silently evolve result-affecting policy under an existing semantics identity. This document holds candidate extensions until a concrete use case and architecture decision justify a new implementation contract.

## Lot, batch, and material-lot semantics

Current accepted quantity execution creates independently dispatchable unit work under one aggregate production request. That does not establish domain semantics for lots, batches, transfer batches, material lots, genealogy, or configurable work chunking.

Research these only when a concrete manufacturing or consumer requirement needs them. The result must distinguish physical/domain batch identity from an implementation optimization used merely to reduce object count.

## Capability requirements and resource pools

Current dispatch uses explicit eligible resource instances. A future capability requirement, work center, or resource pool may be justified when heterogeneous resources must satisfy the same operation through stable shared semantics or when scheduling/reporting requires a real group boundary.

Research must answer:

- what capability identity means;
- whether capability parameters are required;
- how eligibility is derived;
- whether a pool owns scheduling/capacity semantics or is merely grouping;
- how the model-side concept relates to deterministic Engine selection.

Do not introduce a capability taxonomy only to replace an explicit eligible-instance set that already works.

## Dispatch-policy evolution

The current result-affecting dispatch ordering and tie-breaking policy is versioned Engine semantics. Candidate policies such as projected completion time, richer load balancing, priorities, setup-aware dispatch, or optimization-based assignment require evidence that the existing policy is insufficient.

Any accepted change that can alter assignments or outcomes for identical explicit inputs requires an explicit Engine-semantics evolution decision, not a silent implementation tweak.

## Session/advancement evolution

Current consumer-neutral bounded advancement is established. Revisit the command/advancement surface only when a concrete consumer proves that event-count/tick semantics, scheduling control, or ownership cannot be expressed through the existing boundary.

## Distribution/recovery research boundary

Transport versioning, retained supported-event history, reconnect/resynchronization, exact checkpoint/restore, and sidecar packaging become implementation work only after their external contract is explicitly selected. Performance or packaging prototypes may supply evidence, but research must not redefine simulation semantics.

## Promotion rule

Promote a question only when:

- the concrete consumer/problem is identified;
- result-affecting semantics are explicit;
- compatibility/version consequences are understood;
- ownership between Factory, Engine, and consumers is settled; and
- deterministic acceptance evidence can be written before implementation.
