# Factory/Engine Transfer Applicability

> **Status:** Adopted boundary for the currently represented Factory content; spatial execution is specified but not implemented
>
> **Scope:** Transfer lifecycle existence, authored absence versus zero, and model admission by the current Engine interpretation

## Current boundary

Factory records the production system a designer authored. Engine owns the result-affecting interpretation of those records, including whether an admitted execution enters transfer state, when a destination binds, reservation, timing, events, and refusal. Runtime determines whether a selected destination is the same configured resource as the source. The selected rule below is a **current design choice for the current Factory model**, not a universal requirement that transfer must always depend on authored interval inputs. A later Engine rule over different represented content needs its own explicit semantics and support decision.

The [Factory model](factory-model.md#22-optional-spatial-record) has either no spatial record or one complete record containing geometry and transfer-timing magnitudes. It has no geometry-only, handling-only, or non-spatial hand-off record. Absence makes no authored handling assertion. Authored zero is a present fact and remains distinct in the Factory fingerprint. No transfer facts are synthesized from an absent record.

The Engine interpretation declares the represented content it executes. The admission predicate is applied **before** establishing a run or processing any routing step. Factory-invalid content is refused at publication. A valid model outside the executed content is refused before runtime mutation, even if its actual workload would use only same-resource continuations; a later same-resource selection cannot make it admissible. This model-admission rule does not replace workload and command preflight or their explicit reproducibility inputs.

For an admitted execution the lifecycle meanings are:

| Represented content | Same-resource continuation | Distinct-resource continuation |
|---|---|---|
| Spatial record absent | No transfer | No transfer: continue through ordinary destination selection and dispatch, without separate transfer binding, reservation, `TRANSFERRING` state, or `TRANSFER_*` events |
| Spatial record present with legal zero timing magnitudes | No transfer | Full transfer lifecycle under the [Engine's spatial interpretation](engine-semantics.md#5-spatial-modelengine-ownership-boundary); duration may be zero, with binding, reservation, state and events still present |
| Spatial record present with positive derived duration | No transfer | Full transfer lifecycle under the Engine's spatial interpretation, with its derived positive interval |

Refusal has priority over every row. **The current Engine executes only the first row.** It refuses a present spatial record, legal zero included, before any runtime state exists rather than executing it as though the record were absent; the second and third rows are the specified meaning spatial execution must implement. Making them executable changes the Engine's admitted content, not the meaning of the rows.

A zero-duration transfer and no transfer can differ despite identical isolated completion times. The former has its own binding, reservation, state, event, command-interleaving and same-time ordering consequences. A bounded-step proving case compares two candidate Engine meanings for the same spatial-absent model: the selected no-transfer rule and the unadopted Engine-defined zero-duration lifecycle. Use online unary resources `M1` and `M2`, route `M1:5 -> M2:3`, and one work item. **Before** the pending step-one `TaskEnd` at time 5 is processed, `advanceUntil(SimTime.of(5), 1)` consumes that one turn. Under no transfer the work item is processing on M2; under the specified zero-duration lifecycle it is `TRANSFERRING` with completion pending at time 5. Calling the bounded advance after already processing `TaskEnd` would consume the pending transfer completion and is not this witness. The distinction also appears in the supported event stream and in commands allowed between same-time turns. In another engine, equivalence must be judged against its actual observations, events, commands and ordering; atomic same-time advancement alone does not establish it.

## Evolution

Factory validity and Engine executability are separate decisions. While both definitions are work in progress, a change to either is reconciled in its owning specification, with the other's dependent text and tests updated in the same change; neither requires a successor identity.

The current spatial grammar needs no lifecycle-driven correction. A future consumer may justify an explicit non-spatial hand-off fact, positive non-spatial timing, a geometry-only variant, or an Engine-defined zero-duration lifecycle on absent content. Those are viable alternatives, not interpretations silently admitted by this boundary. Each requires a concrete need and a separate Factory grammar and Engine meaning assessment as applicable. A new timing record does not automatically make transfer applicable. The rule here also does not forbid an explicit future Engine interpretation conditioned on geometry without authored timing magnitudes.
