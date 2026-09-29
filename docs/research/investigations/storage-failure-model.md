# Storage failure model and recovery

Status: READY brief; no conclusion

## Question and decision

Beyond the [initial local contract](../../architecture/storage.md), which process, filesystem,
host, and concurrency failures must Arcogine Storage survive, and what implementation changes and
evidence would satisfy that selected guarantee? This decides a bounded recovery/coordination
contract, not whether Arcogine should own Storage.

## Current-contract baseline

Start from a provider that conforms to the current [Storage](../../architecture/storage.md) and
[controlled-revision](../../architecture/controlled-revisions.md) contracts. A departure from an
already-owned guarantee (acceptance, immutable ID binding, exact-definition refusal, or faithful
resolution of an accepted record) is a correctness defect fixed independently of this question. It is
neither evidence for a stronger claim nor a reason to redefine the baseline, and it does not by itself
block this brief. Concretely, the baseline already requires:

- accepted recorder source and subject resolve exactly as accepted after reopen, and valid Unicode,
  supplementary characters included, survives unchanged;
- text the private UTF-8 record format cannot represent, such as an unpaired surrogate, is refused
  before anything is persisted rather than silently substituted;
- distinct opaque definition bindings never collapse to the same stored marker, so a root created
  under one is refused under any other;
- a repeated or rebound controlled-revision ID is still rejected, and an accepted revision's
  fingerprint, lineage and provenance never change.

These examples add no crash, power-loss, concurrency, backup or repair guarantee; stronger survival is
what this question investigates.

## Scope and candidates

Compare (1) retaining the current atomic-installation, forced-file and lock model with its limited
claim, (2) strengthening publication and recovery within the filesystem design, and (3) a
transactional persistence realization if concrete failure requirements defeat the simpler model.
Do not select a WAL, database, replication, or repair algorithm by analogy. Performance, backup,
disk loss, hostile mutation, and distributed operation are outside this question unless evidence
shows they are prerequisites of the selected local guarantee.

## Discriminating cases and evidence

- Process terminates before artifact installation, after artifact installation but before revision
  installation, and after installation but before the caller receives confirmation. Identify which
  states are accepted and how a caller resolves acknowledgment ambiguity without duplicate IDs.
- Initialization is interrupted after root creation and before its marker/lock/directory set is
  complete. Compare refusal, safe retry, and explicit recovery without adopting foreign content.
- Two independent JVMs accept conflicting IDs at once; distinguish lock serialization from
  filesystem-specific assumptions. Include unsupported atomic moves and lock loss.
- OS crash or power loss occurs after an acknowledged operation. Determine whether file forcing,
  atomic rename, and directory metadata synchronization support the proposed claim on each
  supported filesystem. Do not infer crash safety from an ordinary reopen test.
- Corrupt bytes, missing files, and hostile replacement test detection separately from repair or
  authenticity. State which are intentionally refused.

Inspect current source/tests and filesystem/runtime documentation for each load-bearing claim;
use isolated, bounded fault injection or independent-process tests where feasible. A candidate is
falsified if an admitted failure case can erase or reinterpret an acknowledged revision or if its
guarantee depends on an untested platform property. Record unavailable crash evidence as a limit.

## Exit, review and destination

Stop with a named failure model, acceptance and acknowledgment rules, required platform assumptions,
evidence by case, rejected alternatives, and explicit remaining gaps. The question is high risk
because it changes authoritative history guarantees; independent adversarial review is required
before a conclusion is promoted. Reconcile any accepted result into
[Storage](../../architecture/storage.md), then admit an implementable slice in
[Storage planning](../../planning/storage-capability.md) if needed. Reopen when a concrete use needs
stronger survival or supported filesystems change.
