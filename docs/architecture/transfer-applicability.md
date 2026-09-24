# Factory/Engine Transfer Applicability

> **Status:** Adopted boundary for the currently represented Factory content cases; Engine identity and execution support remain to be reconciled
>
> **Scope:** Transfer lifecycle existence, authored absence versus zero, and artifact admission for Factory V1 and the unreleased Factory V2 grammar

## Current boundary

Factory records the production system a designer authored. Engine owns the result-affecting interpretation of those records, including whether an admitted execution enters transfer state, when a destination binds, reservation, timing, events, and refusal. Runtime determines whether a selected destination is the same configured resource as the source. The selected rule below is a **current design choice for the closed V2 grammar**, not a universal requirement that transfer must always depend on authored interval inputs. A later Engine rule over different represented content needs its own explicit semantics, support and identity decision.

Factory V2 currently has either no spatial record or one complete record containing geometry and transfer-timing magnitudes. It has no geometry-only, handling-only, or non-spatial hand-off record. Absence makes no authored handling assertion. Authored zero is a present fact and remains distinct in the Factory fingerprint. No transfer facts are synthesized from an absent record.

An Engine identity must declare its exact supported Factory policy and represented-content domain. Apply that artifact support predicate **before** establishing a run or processing any routing step. Factory-invalid content is refused at publication. A valid artifact outside the Engine's declared domain is refused before runtime mutation, even if its actual workload would use only same-resource continuations. A later same-resource selection cannot make an unsupported artifact admissible. This artifact-applicability rule does not replace workload and command preflight or their explicit reproducibility inputs.

For an admitted execution under the selected current V2 boundary:

| Represented content | Same-resource continuation | Distinct-resource continuation |
|---|---|---|
| V2 spatial record absent | No transfer | No transfer: continue through ordinary destination selection and dispatch, without separate transfer binding, reservation, `TRANSFERRING` state, or `TRANSFER_*` events |
| V2 spatial record present with legal zero timing magnitudes | No transfer | Full transfer lifecycle under the executing Engine's declared spatial interpretation; duration may be zero, with binding, reservation, state and events still present |
| V2 spatial record present with positive derived duration | No transfer | Full transfer lifecycle under the executing Engine's declared spatial interpretation, with its derived positive interval |

These are lifecycle meanings **only for artifacts an Engine admits**. Refusal has priority over every row. A present record does not by itself establish that `engine-semantics:v1`, or any other particular identity, supports it. The exact Engine identity or identities, their accepted V2 cases, and the whole-definition test for fixed `engine-semantics:v1` remain the separate [Engine applicability question](../research/investigations/engine-applicability-after-transfer-boundary.md). The current runtime executes V1 production artifacts with no transfer behavior; this observation does not amend the fixed v1 definition or admit V2 under it.

A zero-duration transfer and no transfer can differ despite identical isolated completion times. The former has its own binding, reservation, state, event, command-interleaving and same-time ordering consequences. A bounded-step proving case compares two candidate Engine meanings for the same V2-absent artifact: the selected no-transfer rule and the unadopted Engine-defined zero-duration lifecycle. Use online unary resources `M1` and `M2`, route `M1:5 -> M2:3`, and one work item. **Before** the pending step-one `TaskEnd` at time 5 is processed, `advanceUntil(SimTime.of(5), 1)` consumes that one turn. Under no transfer the work item is processing on M2; under the specified zero-duration lifecycle it is `TRANSFERRING` with completion pending at time 5. Calling the bounded advance after already processing `TaskEnd` would consume the pending transfer completion and is not this witness. The distinction also appears in the supported event stream and in commands allowed between same-time turns. These are proving cases for a future executing Engine, not a claim that transfer behavior is implemented now. In another engine, equivalence must be judged against its actual observations, events, commands and ordering; atomic same-time advancement alone does not establish it.

## Support and evolution

Factory V2 publication validity, Factory policy publication support, and Engine execution support are separate decisions. Stopping new V1 publication would remove the V1 route for **new** production-only designs, but would not itself withdraw execution support for already-published V1 artifacts or require V2-absent execution. The Factory composition trade-off, new-design admission needs, and Engine support must then be considered together. V2-absent execution is required only if a chosen or existing support obligation requires those designs to remain executable; historical V1 execution has its own declaration. No support scope is created by this document alone.

The current V2 grammar needs no lifecycle-driven correction before attribution. A future consumer may justify an explicit non-spatial hand-off fact, positive non-spatial timing, a geometry-only variant, or an Engine-defined zero-duration lifecycle on absent content. Those are viable alternatives, not interpretations silently admitted by this boundary. Each requires a concrete need and a separate Factory grammar, Engine meaning, support and identity assessment as applicable. A new timing record does not automatically make transfer applicable. The rule here also does not forbid an explicit future Engine interpretation conditioned on geometry without authored timing magnitudes.
