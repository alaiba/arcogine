# Determinism, equivalence, identity, and durability

Part of the [architecture overview](../overview.md#routed-topics); it is mandatory whenever the overview routes your change here.


These are canonical cross-cutting definitions, not a universal data model, lifecycle, or identity
service. Owning domain specifications select their concrete equality, input, output, and support
contracts. A current normative specification states what must hold; being normative does not by
itself create a separately versioned semantic entity or a stability promise.

## Determinism

**Determinism** is a property of a defined computation: with the same definition and the same
complete result-affecting inputs, repeated evaluation produces the same specified observable
result. A claim must identify the admitted input domain, initial-state assumptions, and the output
boundary and equality being compared. Formally, for a fixed definition `D` and declared observation
boundary `P`, `O = P(evaluate(D, I))` is single-valued for each admitted complete input `I`.

Relevant inputs include ordered commands and any result-affecting random, time, numerical, or
external context. A seed alone is insufficient when the random algorithm or its consumption order
can differ. Incidental metadata may be excluded only by the owning contract, not after a comparison
fails; for example, the simulation contract excludes per-run correlation identity, not production
ordering or timing. Determinism neither requires identical implementation binaries nor establishes
equivalence between different definitions. It does not prove correctness against a specification,
accuracy about physical reality, persistence, or later reproducibility. The [Determinism
Contract](determinism-contract.md#determinism-contract) specializes this definition for the current simulation boundary.

## Durability and related properties

Qualify a durability claim with the property, subject, scope, and horizon it actually concerns.
Do not infer one row of this table from another:

| Term | Meaning |
|---|---|
| **Persistence / storage durability** | Persistence stores information beyond its producing operation or process. A storage-durability guarantee specifies which accepted information survives which failures, recovery conditions, and retention horizon. Writing a file alone proves no broader guarantee. |
| **Referential stability / non-rebinding** | A reference declared stable continues to denote the same referent under its owning equality rule. This does not ensure that the referent remains available. A mutable lookup alias is not such a reference. |
| **Traceability** | Evidence-backed links identify the relevant inputs, definitions, implementations, occurrences, or decisions behind a recorded fact. A link or stated rationale is not by itself proof of causation. |
| **Historical interpretability** | Sufficient original meaning and context remain resolvable to understand a historical fact without silently substituting today's definition. This need not include an executable implementation. |
| **Reproducibility** | The necessary basis and capabilities can be recovered and used to repeat a computation against a stated result boundary. Required inputs, definitions, implementation/environment constraints, and dependencies must be available; a deterministic computation whose basis was lost is not thereby reproducible. |
| **Compatibility** | A scoped, potentially directional relationship: a particular consumer can read, execute, exchange, or convert particular input under stated conditions. It is not semantic equivalence and need not be symmetric or transitive. |
| **Stability / support commitment** | An authorized promise to specified consumers or uses about preservation, interpretation, compatibility, execution, migration, or support over a stated scope and horizon. The promise and evidence that it is fulfilled are different facts. |

A stored result can survive a restart while its interpretation has been lost. An old definition can
remain interpretable after its execution support ends. A development experiment can be traceable and
reproducible without an external stability promise. Conversely, a promise may exist without adequate
fulfilment evidence. These distinctions do not waive current admission or retention policies.

Use **durable commitment** only for a stated stability/support promise, not as a synonym for all
these properties. Promotion concerns that commitment; it does not make a computation deterministic,
create semantic equivalence, or cause previously recorded facts to acquire identity.
