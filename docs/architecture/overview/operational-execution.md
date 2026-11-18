# Operational execution — not implemented

Part of the [architecture overview](../overview.md#routed-topics); it is mandatory whenever the overview routes your change here.


Arcogine has no Operational capability: no module, type, adapter, external command, observation
ingestion, correspondence, reconciliation, deployment or operational history. These boundaries hold
today because they describe what Arcogine does not do:

- `FactoryRuntime` executes simulated workload only. It is not a production-control runtime, and
  Arcogine issues no command to a real system.
- An external observation keeps its own source and time provenance. It is never stamped with an
  Arcogine model, revision or continuation identity at ingestion; Governance evidence can refer to
  such a source without any Operational capability ([Governance evidence](../governance-evidence.md)).
- Governance evidence targets point identities; it does not provide an identity that accumulates
  conduct over time.
- If Operational execution is built, it owns acquisition, trust, correspondence, reconciliation and
  applying a revision to an execution target. Factory, Engine and Governance keep their meanings,
  and Operational never redefines production semantics, revision identity or conformance.

The intended design — hybrid realization, the external operation lifecycle, adapter trust and
safety, deployment, reconciliation, drift and operational history — is kept in the [Operational
execution design](../../research/investigations/operational-execution-design.md). The decided [Operational continuity
contract](../../research/investigations/operational-continuity-contract.md) and the open [Operational boundary
research](../../research/investigations/operational-execution-digital-twin-boundaries.md) hold the
rest.
