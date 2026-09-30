# Protocol revision — pass 2 (order robustness and mechanism classification)

> **Artifact kind:** transient research checkpoint (protocol revision), not a report  
> **Revises:** [`factory-design-game-strategy-space-protocol.md`](factory-design-game-strategy-space-protocol.md) (pass 1), which is preserved unchanged  
> **Research baseline (live `main`):** `3479053f304e43ec57d7a5fd69799ae09b84b7fd`

This revision is committed after pass 1 was run and inspected and before pass 2 is run. It changes no
pass-1 parameter: routing profiles, quantities, catalogue, quantity limits, cost rules, dimensions,
target/budget window, proving-case rules and the reference cell are identical. Pass-1 results remain
the pass-1 record; pass 2 is reported separately.

## Why pass 1 is inadequate on its own

1. **Projection-order dependence.** The pass-1 projection-order control (§11) found that 5 of the 7
   reference-frontier designs containing a `FLEX_CELL` complete at a different tick when offers are
   projected in reverse order (for example 17 versus 20 ticks). Only designs with a resource eligible
   for more than one step differed. `MachineId` is the Engine's final selection tie-break
   (`docs/architecture/engine-semantics.md` §2 rule 4), so for those designs the outcome is a function
   of the design **and** of identity assignment, which the player-visible design does not fix. A
   strong-multiplicity result that exists under one arbitrary assignment only is not evidence about the
   design space. Pass 1 applied the control to the reference frontier only.
2. **Mechanism of strong multiplicity.** Expectation E2 (Family D monotone everywhere) was falsified:
   37 Family D cells show provision-incomparable frontier pairs. Inspection shows pairs such as
   `(2 cutters, 2 assembly slots, 2 inspectors)` versus `(1, 3, 2)`, where the cheaper design is on the
   frontier only because it adds capacity at a step that is **not** its active constraint and gains a
   small pipeline-timing improvement. Pass 1's irrelevant-capacity rule calls such a gain immaterial,
   yet pass 1's MVS-S rule counts the resulting incomparability as strong multiplicity. The two rules
   must be reconciled before MVS-S can discriminate the brief's null from its alternative.

## Pass-2 additions

### A. Reversed projection order

Order `R` projects offers as `FLEX_CELL, INSPECTOR, TWIN_ASSEMBLER, ASSEMBLER, CUTTER` (reverse of the
pass-1 order `C`), occurrences consecutively, `MachineId` from 1. Under `C` a flex cell has the highest
identities and loses selection ties to dedicated resources; under `R` it wins them. The two orders are
the extremes of flex tie-break priority. The full enumeration is re-run under `R` for every
`(profile, N)`, and every context and cell is recomputed with the pass-1 rules unchanged. Reported:
the number of designs whose completion differs between `C` and `R`, split by whether the design has a
multi-step resource; and the granularity control under `R`.

### B. Mechanism classification of every strong-multiplicity pair

For every cell and each order, **every** explained provision-incomparable pair `(A cheaper, B faster)`
of the feasible frontier (pass 1 reported only the first) is classified:

- **POOLING** — `flex(A) ≠ flex(B)`: the designs differ in how much capacity is shared across steps.
- **DEDICATED** — `flex(A) = flex(B)`. Let `A⁻` be the component-wise minimum of `A`'s and `B`'s
  provision vectors (removing `A`'s surplus where it exceeds `B`), realized as
  `twins = min(3, asmOnly / 2)`, `assemblers = asmOnly − 2 × twins`, and simulated under the same
  order. The **surplus effect** is `T(A⁻) − T(A)`.
  - **MATERIAL-DEDICATED** iff the surplus effect `≥ max(1, ceil(0.05 × T(A⁻)))`, the pass-1 CC
    materiality threshold;
  - **FINE-TUNING** otherwise: the pair is incomparable only because of a capacity addition whose
    effect is below the protocol's own materiality threshold; removing it leaves a nested (monotone)
    pair.

A cell has **material MVS** under an order iff it has at least one POOLING or MATERIAL-DEDICATED pair
under that order.

### C. Pass-2 cell verdicts

- **Order-robust material MVS:** material MVS under both `C` and `R` for the same cell identity
  (profile, N, family, cost parameters, `f`, `β`; the target and budget are re-derived per order by the
  pass-1 rules).
- **Pass-2 positive:** constraint capacity, irrelevant capacity, migration and capital pressure (pass-1
  rules) hold under both orders, and order-robust material MVS holds.
- **Pair-level survival:** for each POOLING or MATERIAL-DEDICATED pair under `C`, whether the same two
  designs form an explained incomparable feasible-frontier pair of the same cell under `R`.

Candidate selection and robustness reuse the pass-1 rules with pass-2 positive and order-robust
material MVS substituted for pass-1 positive and MVS-S.

### D. Load-bearing dispatch evidence

For the pass-2 candidate's load-bearing pair (or, if none, the pass-1 candidate's pair) and the
reference cell's frontier designs containing a flex cell, a research-local **dispatch profile** is
derived from supported events only: the count of `JOB_DISPATCHED` per `(resource, step)` and the
mean waiting interval per step (dispatch time minus the job's previous step completion, or minus order
acceptance for the first step). It is reported under both orders.

## Unchanged

No new dimension, weighted score, catalogue item, cost value, window value, or Engine rule is
introduced. Pass-1 files are not regenerated in place; the refactored harness must reproduce the
pass-1 outputs byte-for-byte before pass 2 is trusted.
