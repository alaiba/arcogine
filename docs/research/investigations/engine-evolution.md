# Engine Evolution Research

> **Lifecycle:** See the maintained research register; this artifact contains concluded dispatch history, concluded first-release dispatch decisions, and CANDIDATE follow-up questions  
> **Scope:** Result-affecting Engine questions that still require evidence before current deterministic runtime semantics change  
> **Authority:** Research only; current Engine semantics remain fixed by accepted architecture and executable evidence until a separate reconciliation change says otherwise

## Purpose

The Engine implementation must not silently evolve result-affecting policy under an existing semantics identity. This document holds candidate extensions and bounded follow-up questions until evidence and an explicit architecture decision justify a different implementation contract.

`engine-semantics:v1` is already a normative design contract even though its implementation/conformance work is pending. Under current ADR-0015 and v1 authority, an intentional change that can alter outcomes for identical explicit inputs requires a new `EngineSemanticsVersion`; unreleased or implementation-pending status does not create an in-place mutation exception. Research may still decide whether v1 is acceptable for the first supported release or whether evidence justifies architecture work for a different semantics version before implementation. Neither outcome permits a silent implementation tweak.

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

The current one-local-job-per-trigger rule and `combinedQueueDepth` ranking are normative `engine-semantics:v1` rules and production currently conforms to them. Historical review found that the one-job rule was inherited implementation behavior later captured by the normative wording, not evidence of an originally evaluated scheduling trade-off.

That history is material motivation to re-evaluate the rule, but it does not weaken the existing semantics identity. Under current architecture:

- retaining either rule leaves `engine-semantics:v1` unchanged and may be pinned by its conformance fixtures;
- selecting an outcome-changing alternative is evidence for an architecture reconciliation that establishes a new `EngineSemanticsVersion`; it is not an ordinary bug fix or permission to rewrite v1 in place;
- implementation must not adopt an alternative until that versioned architecture/specification exists.

The two first-release dispatch questions below have now completed standard investigation and independent adversarial review. Both reviews ended **ACCEPT WITH QUALIFICATIONS**, both retain the existing v1 rule, and their durable consequences are reconciled jointly below because the ranking rule's exceptional magnitude-sensitive regime is created by the retained local-admission behavior at recovery.

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

## First-release dispatch decisions — jointly reconciled

The two bounded first-release questions were investigated separately so that one experiment could hold the other rule fixed. Their reviewed conclusions converge on one release decision: **retain `engine-semantics:v1` unchanged for both local admission and shared flexible-backlog ranking**.

This is a conservative compatibility decision, not a claim that either rule is globally optimal or historically well-motivated. No accepted Arcogine objective selects a better result-affecting alternative, while each tested alternative has valid workloads where it regresses another outcome. Because both retained rules are already normative v1 semantics, this reconciliation requires no ADR or specification rewrite and creates no new `EngineSemanticsVersion`.

The decisions must nevertheless be carried together. The retained one-local-job recovery behavior can leave a recovered concurrency>1 machine both accepting and locally queued. That is the principal healthy regime where the magnitude of `combinedQueueDepth` can matter rather than acting only as a binary handover signal. Changing local admission later would therefore change part of the structural support for the ranking conclusion even if the ranking formula itself were untouched.

The executable handoff belongs to the v1-conformance slice in [Spatial Runtime Consequences](../../planning/spatial-runtime-consequences.md). That planning surface owns characterization fixtures and the already-identified arithmetic corrections; this research document owns only the decision, qualifications, and reopening triggers.

## CONCLUDED — local admission semantics before first Engine release

### Question

Should the first supported Engine release retain `engine-semantics:v1`'s at-most-one-local-job-per-cascade-trigger rule, or does the evidence justify defining a different Engine semantics version with a precisely bounded rule that admits more local work when one trigger exposes several immediately usable concurrency slots?

### Decision at stake

Whether Arcogine deliberately releases v1 with the current local-admission rule or promotes an alternative through a separately versioned Engine-semantics architecture/specification before changing implementation behavior.

### Reconciled conclusion

Retain v1's existing rule: after the established continuation/next-step processing, a cascade admits **at most one** job from the affected machine's own FIFO queue before running the existing shared-pending fixpoint. No differently versioned admission rule is justified for the first supported release, and no in-place edit of v1 is authorized.

The reason is deliberately narrower than "more utilization is not always better." Recovery fill gives a real isolated utilization/latency improvement, but it also redistributes recovery opportunity away from compatible shared work and changes downstream FIFO arrival order. Reviewed routed cases include both a makespan regression and a makespan/mean-lead trade-off, while a downstream bottleneck can erase the gain entirely. No current product, architecture, or consumer authority selects a non-idling/fill objective over those trade-offs.

### Binding qualifications and reopening triggers

- **Inherited asymmetry, not a general cascade invariant.** Only the local-queue stage is budgeted at one. Initial order admission and the shared-pending fixpoint already fill available capacity. Durable wording must not describe v1 as a generally "trigger-budgeted" or globally non-work-conserving cascade.
- **Healthy discrimination is recovery-scoped.** In the current supported runtime, a normal completion exposes at most one slot. Genuine offline -> online recovery is the healthy transition that can expose several slots at once, and therefore the healthy seam where retain-one versus fill semantics materially diverge.
- **Retention is provisional, not optimality.** The supported statement is "no current evidence justifies changing v1," not "one-local-job admission is intrinsically correct." Empirical recovery behavior from a supported consumer can reopen the question if v1 materially misrepresents the plant being modelled.
- **Faulted histories remain separate.** A supported command can fault after mutation and leave an online machine with both free capacity and a non-empty local queue. A fill-on-every-local-stage rule can then diverge from recovery-only fill. That is a fault/recovery-contract observation, not evidence for a healthy scheduling policy, and must not be used to prefer an alternative without a separately selected fault-repair contract.
- **Re-evaluate when the runtime gains a new way to expose several healthy slots.** Capacity resizing, preemption/cancellation, or spatial admission/reservation behavior that changes the current enqueue/free-capacity invariant would invalidate part of the successful-history equivalence argument and requires a fresh bounded check.

### Investigated candidates

- **Retain v1:** one local queued job at most per cascade trigger, followed by the existing shared-pending fixpoint.
- **Recovery-specific fill:** after a genuine offline -> online transition, admit local FIFO work up to currently usable capacity before the shared-pending stage; ordinary completion retains one-local-job behavior because it normally releases one slot. Selecting this outcome would require a new `EngineSemanticsVersion` under current architecture.
- **Trigger-independent bounded non-idling:** whenever the local-queue stage runs, admit local FIFO work until no immediately usable slot remains, then run the existing shared-pending fixpoint. Selecting this outcome would require a new `EngineSemanticsVersion` under current architecture.

The reviewed successful-history invariant makes the two fill alternatives observationally equivalent on current healthy supported execution: after recovery-specific fill, an online machine with a non-empty local queue has no unused slot at a completed transition boundary, so an ordinary completion exposes only the single slot that both alternatives refill. Their difference remains reachable only through partial-fault continuation under current mechanics.

### Durable proving cases

Executable conformance work should preserve the semantic cases, not experiment labels:

- one offline concurrency-4 machine with eight queued 10-tick single-eligible jobs: retained v1 completes after 80 ticks from recovery, while recovery fill would complete after 20;
- mixed recovery with M1 concurrency 4, M2 offline, two local M1-only 10-tick jobs and four shared `{M1,M2}` 3-tick jobs: v1's local stage admits one and the shared fixpoint fills remaining capacity, proving local-before-shared order does not imply exclusive local priority;
- the routed regression with order completions `[14,29,29]` and makespan 29 under v1 versus `[14,40,21]` and makespan 40 under fill;
- the reviewed trade-off with makespan/mean-order completion `93 / 69.75` under v1 versus `91 / 71.25` under fill;
- eight jobs routed through `M1 concurrency 4, duration 10 -> M2 unary, duration 100`, whose terminal completion remains 810 under both policies;
- same-machine continuation precedence, local-before-shared ordering, FIFO local start order, repeated recovery, queue-smaller-than-capacity, and equal-time completions.

### Exit criteria — satisfied

The retained rule now has an explicit bounded rationale independent of utilization, exact ordering is already normative in v1, discriminating cases have been reproduced independently, and the high-risk conclusion received adversarial review with **ACCEPT WITH QUALIFICATIONS**. The durable consequence is no semantics change: preserve v1 and pin it executably, subject to the qualifications above.

## CONCLUDED — shared flexible-backlog ranking semantics before first Engine release

### Question

Should the first supported Engine release retain v1's exact `combinedQueueDepth` — local physical queue depth plus every compatible shared-pending entry — when one unbound flexible job contributes demand to several candidate machines simultaneously, or does the evidence justify a different Engine semantics version with another ranking term?

### Decision at stake

Whether Arcogine deliberately releases v1 with the current resource-selection ranking or promotes a different queue-depth/ranking term through a separately versioned Engine-semantics architecture/specification while preserving eligibility, online preference, immediate-acceptance ranking, unbound shared waiting/reselection, and final `MachineId` tie-breaking.

### Reconciled conclusion

Retain v1's exact `combinedQueueDepth` ranking unchanged. No tested alternative has a better-supported semantic invariant under Arcogine's current objective boundary, so this question does not justify a new `EngineSemanticsVersion`; it also does not authorize any in-place edit of v1.

This conclusion is not a claim that `combinedQueueDepth` is a principled physical-load estimate or an optimal dispatch rule. The shared contribution is best understood conditionally: when competing accepting machines have empty local queues, the non-zero shared contribution acts as a handover signal that tends to leave the just-freed machine available to compatible unbound work. Outside that sub-domain — most importantly the recovery state created by v1 local admission — the magnitude of the term can affect the choice.

### Binding qualifications and reopening triggers

- **Conservative retention, not optimality.** Generated searches can falsify claims that local-depth-only generally dominates, but their win ratios are properties of those search spaces and must not be treated as Arcogine workload probabilities.
- **The candidate space is not universally two-valued.** Positive reweightings collapse to v1 only on the sub-domain where competing accepting candidates do not themselves carry residual local queues. A recovered concurrency>1 machine can violate that condition under retained v1 local admission, and positive weights can then produce distinct assignments.
- **The handover interpretation is conditional on retained local admission.** If local admission later fills recovery capacity, the magnitude-sensitive recovery corner changes or disappears; ranking and admission must be reconsidered compositionally rather than as unrelated rules.
- **Information horizon must be stated precisely.** The mirror evidence proves that no function of the facts the **current comparator consults** can separate both outcomes. It does not prove that every fact available at that instant is unavailable to a future differently-versioned rule. Shared-entry duration/routing position, the ranked job's current-step duration/remaining authored route, free capacity, eligible-set structure, backlog order, `busyTicks`, and current time already exist but are unused. Remaining processing time of an active job is genuinely unavailable today because the runtime exposes neither per-step start time nor scheduler enumeration.
- **No richer candidate was justified by the current objective boundary.** Demand-conserving fair share, placeable-now exclusion, duration weighting, positive reweighting, and effective-backlog variants either collapse to an already-tested behavior, lack a supported decision objective, or regress reviewed workloads. This does not close the design space forever; it closes this first-release decision at the present authority boundary.
- **Reopen on a selecting objective or changed runtime facts.** A supported consumer objective/fairness contract, representative real workload evidence, a change to local admission, implemented spatial transfer/admission semantics, heterogeneous resource speeds/capabilities, or another change to what compatible shared demand means can justify a new bounded ranking investigation.

### Investigated candidates

- **Retain v1's exact `combinedQueueDepth`** as currently specified.
- **Local physical queue depth only**, leaving shared work unbound and reconsidered at dispatch time rather than projecting it into every candidate's depth. Selecting this outcome would require a new `EngineSemanticsVersion` under current architecture.
- **Other deterministic overlap-aware terms**, provided they preserve unbound reselection and use an explicit, justified information horizon. Reviewed examples included demand-conserving fair share, exclusion of immediately placeable backlog, duration weighting, positive scalar reweighting, and effective backlog.

### Durable proving cases

At minimum preserve these semantic cases for executable conformance and future reopening:

1. **Canonical overlap discriminator**

   ```text
   O1: M1:5 -> {M1,M2}:1 -> M1:1
   O2: M3:6
   O3: {M1,M3}:100
   ```

   with all machines unary. V1 produces `[106,6,105]`, mean 72.33, makespan 106. Local-depth-only produces `[7,6,106]`, mean 39.67, makespan 106. The waiting shared job must remain genuinely unbound and be reselected later.

2. **One-variable mirror.** Use the same construction but change only `O3`'s duration from 100 to 1. The decisive current-comparator state is unchanged, but v1's mean is 6.33 versus 6.67 under local-depth-only. Together with the canonical case, this proves that the canonical improvement does not select a replacement policy over the incumbent comparator's fact projection.

3. **Scarce-machine protection.** `O1: M1:5 -> {M1,M2}:60`; `O2: M3:500`; `O3,O4: {M1,M3}:20`. V1 completes `[65,500,25,45]`, mean 158.75; local-depth-only completes `[65,500,85,105]`, mean 188.75.

4. **Makespan non-neutrality.** Use the canonical routes with concurrency 2 on M1 and M2 and quantities `2 / 1 / 3`. V1 yields `[106,6,106]`, mean 72.67, makespan 106; local-depth-only yields `[7,6,107]`, mean 40.00, makespan 107.

5. **Overlap multiplicity without early binding.** Project one 100-tick shared job onto `{M1,M3,M4,M5}` while M3/M4/M5 are each held for 500 ticks. V1 and local-depth-only must preserve unbound waiting/reselection even though the shared contribution is counted against several candidates.

6. **Recovery magnitude-sensitive corner.** Construct a ranking call with a recovered concurrency-4 machine still accepting with local depth 3 under v1's one-local-job recovery stage, competing with a just-freed unary machine that has two compatible pending shared entries. Exact v1 keys are 3 versus 2 and select the unary machine; a positive weight of 2 produces 3 versus 4 and selects the recovered machine. This pins the qualification that positive-weight terms do not universally collapse to v1 and couples the ranking case to retained local admission.

Also retain `canAcceptJob` primacy, all-offline fallback/recovery, final `MachineId` tie-breaking, local FIFO, backlog arrival order, non-head-of-line-blocking, and the ability of a shared entry to choose a different machine when reconsidered.

### Exit criteria — satisfied

The retained ranking has a bounded interpretation and objective boundary, overlap/tie/reselection cases have been reproduced independently, omitted-candidate challenges did not produce a better-supported replacement, and the high-risk conclusion received adversarial review with **ACCEPT WITH QUALIFICATIONS**. The durable consequence is no semantics change: preserve exact v1 ranking and pin the reviewed boundary cases, subject to the qualifications above.

## CANDIDATE — queue sequencing and scheduling objective

Current per-machine local queues are FIFO. The investigation proves that FIFO is not universally optimal and that current-step SPT is representable with facts Arcogine already has, but it also proves that no simple sequencing rule is universally better across multi-stage routes, order aggregation, makespan, mean order lead time, and dynamic arrivals.

Do not research a replacement sequencing rule merely because one benchmark improves. Reopen this question when a concrete supported consumer identifies the scheduling objective, fairness/starvation requirements, information horizon, and authoritative input facts that define what "better" means. Due dates/weights, setup matrices, resource-dependent duration, or full-horizon optimization remain separate semantic prerequisites when the chosen policy family needs them.

## Same-semantics shared-backlog performance

The dispatch investigation established a concrete implementation-efficiency problem independent of policy choice: repeated scans of `pendingMultiEligible` make admission scale quadratically for a fixed eligible-set size, with additional scan-heavy dispatch costs.

This concern is ready for implementation planning only under an exact-semantics constraint. Optimize the ranking semantics of the specific `EngineSemanticsVersion` being executed without changing selected resource assignments, local/shared waiting order, shared-backlog reselection behavior, supported events/observations, deterministic replay, or exact ranking arithmetic. If an optimization requires different results, stop and return to research rather than hiding policy evolution inside a performance change.

## Session/advancement evolution

Current consumer-neutral bounded advancement is established. Revisit the command/advancement surface only when a concrete consumer proves that event-count/tick semantics, scheduling control, or ownership cannot be expressed through the existing boundary.

## Distribution/recovery research boundary

Transport versioning, retained supported-event history, reconnect/resynchronization, exact checkpoint/restore, and sidecar packaging become implementation work only after their external contract is explicitly selected. Performance or packaging prototypes may supply evidence, but research must not redefine simulation semantics.

## Promotion rule

Promote a question only when:

- the concrete consumer/problem or first-release semantic risk is identified;
- result-affecting semantics are explicit;
- compatibility/version consequences are understood;
- ownership between Factory, Engine, and consumers is settled; and
- deterministic acceptance evidence can be written before implementation.