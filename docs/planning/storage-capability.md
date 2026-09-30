# Arcogine Storage Capability Plan

> **Status:** PLAN-STO-1 complete; stronger support questions remain in research
> **Scope:** Bounded built-in persistence for Governance-controlled revisions
> **Authority:** Planning only; [Storage](../architecture/storage.md) owns the current contract and supported behavior

## PLAN-STO-1 — Built-in controlled-revision storage

The admitted slice gives Arcogine Storage a product purpose and independent `:storage` module,
exposes `ArcogineStorage` and `BuiltInStorage.open`, keeps the filesystem implementation private,
and realizes Governance's existing `ControlledRevisionAuthority` without moving revision meaning.
Factory supplies artifact verification through the domain-neutral seam. Existing roots keep their
private representation and definition-bound refusal. The slice includes moved integration tests,
separate-JVM reopen evidence, module boundary rules, canonical documentation, and research
placement. Acceptance evidence includes the product gate, repository tooling, exact-head CI and
independent review. Stronger support remains outside this completed bounded slice.

Governance may consume the current public contract within its documented scope. A Governance use
requiring stronger retained history must state that requirement and wait for a support contract that
actually supplies it. Neither all Governance work nor all Storage work is blocked on the research
questions below.

## Unadmitted stronger behavior

[Failure-model research](../research/investigations/storage-failure-model.md) is `READY`.
[Historical-support research](../research/investigations/storage-historical-support.md) is
`CANDIDATE` until a concrete retained use and support horizon are named; neither is a selected
implementation slice. The [research register](../research/research-register.md) also preserves three
later `CANDIDATE` triggers: backup/restore when retained authoritative history
must survive loss or replacement of its storage location; performance/capacity when a named workload
objective or measured limit exists; and alternate-provider conformance when a concrete substitution
need appears. Those candidates have no dedicated brief or admitted implementation slice yet.

No WAL, database, migration, replication, backup mechanism, performance framework, alternate
backend, evidence-history storage, or universal provider contract is admitted by this plan. A later
slice needs a settled owning contract, dependencies, bounded responsibility, and acceptance cases
before it receives a delivery coordinate.
