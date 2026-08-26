# ADR-0005: Gate 2 explicit-eligibility dispatch policy

Status: Accepted
Date: 2026-08-26

## Context

Gate 2 of Engine Readiness ([`../../planning/factory-simulation-engine-readiness.md`](../../planning/factory-simulation-engine-readiness.md), §6) requires that a single operation step be executable by more than one equivalent installed resource, with deterministic resource selection, while keeping resource capability, operational status, and queue state distinct.

Before this decision, `OperationStepDefinition.eligibleResources` was already modeled as `Set<MachineId>` in anticipation of this gate, but three things collapsed it back to a single machine: `FactoryModelValidator` rejected any step naming more than one eligible resource ("not yet supported by the runtime, this milestone"); the runtime routing type `RoutingStep` had only one `MachineId machineId` field; and `FactoryRuntimeAssembler` discarded every eligible resource but the first (`eligibleResources().iterator().next()`). `FactoryHandler` never compared or ranked candidate machines, because there was never more than one to choose from.

A repository-grounded audit (see the Gate 2 session preceding this ADR) confirmed the model layer already had the shape Gate 2 needs; only the validator restriction and the runtime's single-machine routing representation were blocking it. No capability taxonomy, resource pool/work-center type, or generalized scheduler exists or is required by any accepted architecture document to satisfy Gate 2's acceptance criteria.

## Decision

Gate 2's first slice is classified **A: preserve existing model eligibility into runtime, plus a deterministic selector** — not a new capability abstraction (classification C) and not a canonical-model extension (classification B), since the model already carries what is needed.

Concretely:

1. `FactoryModelValidator` no longer rejects a step with more than one eligible resource; it still rejects zero.
2. `RoutingStep` carries `Set<MachineId> eligibleMachines` instead of a single `MachineId`, preserving the model's full eligibility set into the runtime routing representation. A convenience single-machine constructor keeps every existing single-eligible call site unchanged.
3. `FactoryRuntimeAssembler` copies the full eligible set into `RoutingStep` rather than picking one arbitrarily.
4. `FactoryHandler` gains one deterministic selection policy, applied wherever a routing step is dispatched:

   ```text
   eligible
       -> online (excludes Offline machines)
       -> able to accept work immediately (Machine.canAcceptJob())
       -> shallowest queue
       -> lowest MachineId (final, always-decisive tie-break)
   ```

   If no eligible machine is online, selection falls back to the full eligible set under the same ranking, so a step never becomes undispatchable purely because every eligible machine happens to be offline (existing single-eligible-machine behavior is unchanged in that case).

Model-side eligibility (`OperationStepDefinition.eligibleResources`), runtime operational status (`MachineState`), and runtime queue state (`Machine`'s per-machine `ArrayDeque`) remain three distinct types, matching Gate 2's acceptance criterion 7; this decision does not merge them.

Explicitly out of scope for this slice: capability taxonomies/tags, resource pools or work centers as first-class types, projected-completion-time or other load-aware ranking beyond queue depth, multiple `Job`s per `Order`, and any failure/maintenance modeling for offline machines beyond simple exclusion from new dispatch.

## Alternatives considered

### Generalized capability model (classification C)

Have `ResourceDefinition` advertise a capability (e.g. `MILLING`) and have `OperationStepDefinition` require a capability rather than name explicit machine IDs, with the eligible set derived at model or runtime construction time.

Rejected for this slice: nothing in `docs/architecture/isa-95-semantic-mapping.md` or `factory-design.md` establishes this as current accepted architecture — both describe it as a future direction, not a requirement. Building it now would add a taxonomy, an inference step, and a second way to express eligibility before any accepted use case demands it, when the existing explicit `Set<MachineId>` already satisfies every Gate 2 acceptance criterion.

### Optimization-driven selection (projected completion time, due dates, etc.)

The planning document's illustrative Gate 2 ordering (§6.3) includes "lowest projected completion time" ahead of queue depth.

Rejected for this slice: it requires estimating in-flight completion, which is speculative optimization machinery not needed to prove any Gate 2 acceptance criterion. Queue depth plus availability is sufficient to make parallel capacity and deterministic tie-breaking observable, and is exhaustively testable in one short policy statement.

### Change `RoutingStep`'s single-machine field to a `List<MachineId>` instead of a `Set<MachineId>`

Rejected: eligibility is unordered and must not admit duplicate entries; a `Set` matches `OperationStepDefinition.eligibleResources`'s own shape and avoids inventing meaning for machine order that dispatch does not use (the deterministic order emerges from the selection policy itself, not from insertion order).

## Consequences

- A single operation step can now be published with, and dispatched against, more than one eligible machine, without any change to `FactoryModel`, `OperationStepDefinition`, `ResourceDefinition`, or `MachineId`.
- Removing or disabling one eligible instance no longer requires changing the product/operation definition — dispatch simply stops selecting an offline machine for new work among a real multi-candidate eligible set.
- `RoutingStep.machineId()` no longer exists; callers read `eligibleMachines()` and, where they need one concrete choice, call `FactoryHandler`'s private `selectMachine`. External consumers (`interfaces/api`, `interfaces/cli`) that construct single-machine `RoutingStep`s are unaffected by the added convenience constructor.
- `TaskStart`/`TaskEnd` event payloads continue to carry the one machine actually assigned; no externally visible event semantics change.
- This tie-break policy is now a recorded architectural decision rather than planning-document prose; a future slice that adds load-aware ranking (e.g. projected completion time) should update or supersede this ADR rather than silently reinterpreting the policy.

## Charter alignment

This decision keeps the **published model owns eligibility / runtime owns dispatch** boundary established by ADR-0003 intact: `FactoryModel` still owns capability/eligibility facts, and `FactoryHandler` still owns the run-time decision of which eligible instance actually executes a given unit of work.
