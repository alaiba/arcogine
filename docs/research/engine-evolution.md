# Engine Evolution Research

> **Lifecycle:** See the maintained research register; this artifact contains concluded dispatch history plus READY/CANDIDATE follow-up questions  
> **Scope:** Result-affecting Engine questions that still require evidence before current deterministic runtime semantics change  
> **Authority:** Research only; current Engine semantics remain fixed by accepted architecture and executable evidence until a separate reconciliation change says otherwise

## Purpose

The Engine implementation must not silently evolve result-affecting policy under an existing semantics identity. This document holds candidate extensions and bounded follow-up questions until evidence and an explicit architecture decision justify a different implementation contract.

The first Engine semantics version is still a normative design contract with implementation/conformance work pending. That creates an important boundary:

- **before the first release**, evidence may justify an explicit pre-release reconsideration of a v1 rule without pretending the existing implementation was a bug or automatically creating `engine-semantics:v2`;
- **after a semantics version is released**, an outcome-changing interpretation for identical explicit inputs requires a new `EngineSemanticsVersion` under ADR-0015.

Neither case permits a silent implementation tweak.

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

## Dispatch-policy investigation — concluded umbrella question

The 2026-09 dispatch-policy investigation asked when Arcogine's current deterministic resource-selection and queue-dispatch behavior becomes materially inadequate and whether a richer policy family should replace it. The high-risk investigation received an independent adversarial pass whose final disposition was **ACCEPT WITH QUALIFICATIONS**.

### Durable conclusion

Do **not** add a selectable policy menu, global optimizer, or sophisticated scheduler now. The current deterministic policy remains the implementation baseline because Arcogine has no accepted consumer objective that makes makespan, order lead time, utilization, fairness, tardiness, or another scheduling objective authoritative.

The investigation did establish material weaknesses worth preserving as reopening evidence. Those weaknesses do not select one replacement policy; several apparently beneficial changes regress other valid workloads or objectives.

### Version and history qualification

The current one-local-job-per-trigger rule and `combinedQueueDepth` ranking are normative in the unreleased `engine-semantics:v1` design contract and production currently conforms to them. Historical review found that the one-job rule was inherited implementation behavior later captured by the normative wording, not evidence of an originally evaluated scheduling trade-off.

Therefore:

- changing either rule now would be an explicit **pre-release semantic decision**, not an ordinary bug fix;
- retaining either rule also deserves an explicit decision before v1 is released because the investigation exposed discriminating counterevidence;
- once v1 is released, an outcome-changing revision requires a new Engine semantics version.

### Durable discriminating evidence

Retain these cases as reusable research evidence:

1. **Local recovery underfill.** With one offline machine, concurrency 4, eight already-queued single-eligible jobs, and 10-tick processing, one recovery completes at tick 80 under the current one-local-job-per-trigger cascade. A recovery-only capacity-fill counterfactual completes at tick 20. This is specifically a **local-queue stage** effect: the following shared `pendingMultiEligible` fixpoint can fill otherwise free slots when compatible shared work exists.
2. **Capacity filling is not objective-independent.** A valid routed case produces makespan 29 under current behavior and 40 under recovery fill. Another case improves makespan 93 -> 91 while worsening aggregate-order mean lead time 69.75 -> 71.25. A downstream unary bottleneck can erase the local utilization gain entirely.
3. **`combinedQueueDepth` has a reachable overlap failure.** In a workload that preserves unbound shared waiting and normal reselection, counting one shared pending job against every compatible candidate drives aggregate-order mean completion from a 39.67 local-depth counterfactual to 72.33 under current `combinedQueueDepth`, with makespan 106 in both cases. This is a ranking question, not a permanent-binding strawman.
4. **FIFO is not universally optimal, but simple SPT is not adoption-ready.** On one all-ready unary machine with durations `[100,1,1]`, FIFO mean completion is 101 and current-step SPT is 35 at the same makespan. Multi-stage/order cases show SPT can worsen makespan or aggregate-order mean lead time, and unaged SPT can starve long work under repeated short arrivals.
5. **Spatial-aware selection remains analytical future evidence.** Under the accepted Factory V2 Manhattan-transfer facts, selecting a nearer eligible destination can sharply reduce one transfer, but a nearest-next-step rule can also create a much longer downstream route (for example 296 versus 100 total transfer ticks in the reviewed construction). Spatial runtime is not yet implemented, so this is not executable evidence for changing current dispatch. Preserve it as a future ranking proving case only when spatial execution exists and the scheduling horizon/objective is explicit.
6. **Shared-backlog scanning is an implementation-efficiency concern.** Actual Java admission measurements for 1,000/2,000/4,000/8,000/16,000 flexible waiting jobs were approximately 84/112/333/1,123/4,433 ms in the diagnostic environment. Source inspection explains the superlinear curve: per-submission candidate ranking repeatedly scans shared pending work, yielding quadratic admission for a fixed eligible-set size. Preserve exact semantics while optimizing; do not call an equivalent implementation change dispatch-policy evolution.
7. **Redundant availability was a separate conformance defect.** A redundant online command previously could trigger dispatch without a supported event; current `main` now short-circuits the no-op. Keep its regression coverage, but do not treat it as open dispatch-policy evidence.

The exact diagnostic timings are evidence of a scaling shape, not a performance contract or universal benchmark target.

## READY — pre-release local admission semantics

### Question

Before `engine-semantics:v1` is released, should a cascade continue to admit at most one job from a machine's own local queue per trigger, or should a precisely bounded rule admit more local work when one trigger exposes several immediately usable concurrency slots?

### Decision at stake

Whether v1 deliberately retains the inherited one-local-job rule or adopts a bounded non-idling/recovery rule before conformance fixtures make the first release immutable.

### Candidates

- **A — retain current rule:** one local queued job at most per cascade trigger, followed by the existing shared-pending fixpoint.
- **B — recovery-specific fill:** after a genuine offline -> online transition, admit local FIFO work up to currently usable capacity before the shared-pending stage; ordinary completion retains one-local-job behavior because it normally releases one slot.
- **C — trigger-independent bounded non-idling:** whenever the local-queue stage runs, admit local FIFO work until no immediately usable slot remains, then run the existing shared-pending fixpoint.

Do not expand this question into queue reordering, policy menus, due-date scheduling, setup optimization, or a global scheduler.

### Proving cases

Any conclusion must account for:

- the isolated concurrency/queue-size recovery family, including concurrency 4 with eight 10-tick jobs;
- mixed local plus shared backlog, where the shared fixpoint can fill slots left by the local stage;
- the routed makespan regression 29 -> 40 under aggressive recovery fill;
- the 93 -> 91 makespan / 69.75 -> 71.25 mean-order-lead trade-off;
- the unary downstream bottleneck whose final completion remains 810 under both current and recovery-fill behavior;
- same-machine continuation precedence and local-before-shared ordering.

### Exit criteria

Conclude only when the chosen rule has an explicit rationale independent of "more utilization is always better," exact deterministic ordering is specified, compatibility/version consequences are stated, and executable acceptance cases can distinguish the chosen rule from the rejected candidates. A result-affecting conclusion remains high risk and requires independent adversarial review before architecture promotion.

## READY — shared flexible-backlog ranking semantics

### Question

Before v1 release, is exact `combinedQueueDepth` — local physical queue depth plus every compatible shared-pending entry — the right deterministic ranking input when one unbound flexible job contributes demand to several candidate machines simultaneously?

### Decision at stake

Whether v1 deliberately keeps the current resource-selection ranking or replaces only its queue-depth term with a better justified deterministic metric while preserving eligibility, online preference, immediate-acceptance ranking, unbound shared waiting/reselection, and final `MachineId` tie-breaking.

### Candidates

- **A — retain exact `combinedQueueDepth`** as currently specified.
- **B — local physical queue depth only**, leaving shared work unbound and reconsidered at dispatch time rather than projecting it into every candidate's depth.
- **C — another deterministic overlap-aware demand term**, only if a concrete candidate can be defined without early binding or invented future knowledge and can beat both A and B on discriminating cases.

### Proving case

At minimum preserve the reachable overlap discriminator:

```text
A: M1:5 -> {M1,M2}:1 -> M1:1
D: M3:6
J: {M1,M3}:100
```

with all machines unary. Current `combinedQueueDepth` sends A's flexible middle step to M2 and later J to M1, producing `[A=106,D=6,J=105]`, mean 72.33, makespan 106. Local-depth-only sends A to M1 and later J to M3, producing `[7,6,106]`, mean 39.67, makespan 106. Any alternative must preserve the real shared-pending reselection semantics rather than relying on permanent early binding.

### Exit criteria

Conclude only when the selected ranking term has a stated invariant/objective boundary, survives overlap and tie cases, preserves deterministic reselection semantics, and can be pinned by executable fixtures. A result-affecting conclusion remains high risk and requires independent adversarial review before architecture promotion.

## CANDIDATE — queue sequencing and scheduling objective

Current per-machine local queues are FIFO. The investigation proves that FIFO is not universally optimal and that current-step SPT is representable with facts Arcogine already has, but it also proves that no simple sequencing rule is universally better across multi-stage routes, order aggregation, makespan, mean order lead time, and dynamic arrivals.

Do not research a replacement sequencing rule merely because one benchmark improves. Reopen this question when a concrete supported consumer identifies the scheduling objective, fairness/starvation requirements, information horizon, and authoritative input facts that define what "better" means. Due dates/weights, setup matrices, resource-dependent duration, or full-horizon optimization remain separate semantic prerequisites when the chosen policy family needs them.

## Same-semantics shared-backlog performance

The dispatch investigation established a concrete implementation-efficiency problem independent of policy choice: repeated scans of `pendingMultiEligible` make admission scale quadratically for a fixed eligible-set size, with additional scan-heavy dispatch costs.

This concern is ready for implementation planning **only under an exact-semantics constraint**. Optimization must preserve selected resource assignments, local/shared waiting order, shared-backlog reselection behavior, supported events/observations, deterministic replay, and the final ranking rule chosen by the pre-release `combinedQueueDepth` research above. If an optimization requires changing those results, stop and return to research rather than hiding policy evolution inside a performance change.

## Session/advancement evolution

Current consumer-neutral bounded advancement is established. Revisit the command/advancement surface only when a concrete consumer proves that event-count/tick semantics, scheduling control, or ownership cannot be expressed through the existing boundary.

## Distribution/recovery research boundary

Transport versioning, retained supported-event history, reconnect/resynchronization, exact checkpoint/restore, and sidecar packaging become implementation work only after their external contract is explicitly selected. Performance or packaging prototypes may supply evidence, but research must not redefine simulation semantics.

## Promotion rule

Promote a question only when:

- the concrete consumer/problem or pre-release semantic risk is identified;
- result-affecting semantics are explicit;
- compatibility/version consequences are understood;
- ownership between Factory, Engine, and consumers is settled; and
- deterministic acceptance evidence can be written before implementation.
