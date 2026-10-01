# Factory-design game strategy space — independent adversarial review

## Identity, authority and disposition

- **Disposition: ACCEPT WITH QUALIFICATIONS.** The constructive positive survives. The
  necessity claim, general causal interpretation of maximum pool occupancy, universal
  materiality language and projection-ownership recommendation do not survive unchanged.
- **Review date:** 2026-10-01.
- **Evidence workspace:** `research/factory-design-game-strategy-space`.
- **Exact reviewed report:** commit `df23807640d68512d3ac9bee1b189d462c9efe08`,
  `workspace/research/investigations/factory-design-game-strategy-space-report.md`.
- **Report's stated research baseline:** `3479053f304e43ec57d7a5fd69799ae09b84b7fd`;
  its final main recheck was `9fc890ac7895d67f890c82fa5130720824b62536`.
- **Review baseline and final live-main recheck:**
  `335908a154170655d4a6f694db8826980dee4547`. Main did not move during the review.
- **Input handoff:** commit `ec17e74f7e28f601d7784f54fc1013973854c54b`,
  `workspace/research/investigations/factory-design-game-strategy-space-adversarial-review-handoff.md`.
- **Review artifact:** `workspace/research/investigations/factory-design-game-strategy-space-adversarial-review.md`;
  its exact completed commit is supplied in the handoff accompanying this artifact. No self-referential
  commit identity is claimed inside the file.

This is research evidence, not architecture adoption, implementation admission or a PR disposition.
The original report, protocols and results remain unchanged. The maintained research question remains
READY on main; the report's branch-local ACTIVE label does not change the register. Reconciliation and
the knowledge-transfer audit remain outstanding; the workspace is not retirement-eligible.

## Independence and anchoring control

This review was performed in a fresh isolated session with no authorship or responsibility for
defending the original report. The complete report blob was verified (34,704 bytes, expected section
structure and baseline) before report-specific evaluation. The brief, current authorities and relevant
research substrate were inspected before reading the report in depth. Candidate answers and failure
cases were recorded in [the reconstruction](factory-design-game-strategy-space-review-reconstruction.md)
before that reading. No synthesis seeds were used.

The supplied handoff disclosed the qualified-positive summary and suggested attacks. This is therefore
an independent review with disclosed framing exposure, not a blind replication. Independence rests on
the fresh run, independent reconstruction, separate falsification code and no obligation to preserve
the author's answer; it does not claim a different model family.

Risk is **medium** for this bounded product-evidence conclusion. Identity/determinism and major
ownership would be high-risk if this were used to change Engine semantics or settle projector/analytics
ownership. This review does neither.

The reconstructed candidates were: monotone/fragile null; dedicated finite-batch alternatives; shared
capacity substitution; arbitrary identity-policy advantage; and economics/classification artifact.
The attacks were direct frontier reconstruction, resource-type interleavings, intervention effects
independent of occupancy, uniform outcome thresholds, fixed-budget repricing, low-quantity control,
replay, provenance, and ownership/scope challenges.

## Surviving conclusion

Under the reviewed current Engine semantics, a bounded non-spatial serial three-step challenge with
durations 3/4/5, one quantity-12 order, and dedicated plus assembly/inspection-shared capacity has at
least two non-nested, nondominated designs with a material cost/completion trade-off. The retained
reference uses prices 150/200/360/250/313 for cutter/assembler/twin/inspector/flex, budget 1,062 and
completion target 57.

| Design | Cost | Completion across all its resource-type identity orders |
|---|---:|---:|
| S1: cutter, assembler, two inspectors | 850 | 56 |
| S2: cutter, assembler, inspector, flex | 913 | 46–47 |
| S3: cutter, twin assembler, two inspectors | 1,010 | 45 |

The S1/S2 pair remains materially separated even at a uniform 15% completion-improvement threshold
(worst-case improvement 9/56). S2/S3's 1–2 ticks do **not** establish a second 5% improvement.
Thus retain at least two materially separated alternatives, not an unqualified claim that all three
are pairwise materially distinct. All three are mechanically distinct and nondominated here.

Supported dispatch events show S2's flex serving both steps under every tested identity permutation.
This establishes a concrete shared-capacity mechanism and bounded mechanical ground truth. It does
not establish human comprehension, engagement, industrial realism, or the necessity of flexibility
for every viable challenge.

## Evidence and reproducibility

**Repository facts.** Current-main grounding included the charter; overview and determinism contract;
Factory model/resource semantics; Engine selection/decomposition rules; runtime control/evidence;
the three game consumer/vertical-slice/challenge-readiness plans; strategy and diagnostic briefs;
analytics ownership; Factory evolution, setup/changeover, resource characteristics; Engine evolution
and final-tie-break research; and their register entries. Semantic-neighbor searches included shared
eligibility, MachineId, combinedQueueDepth, constraint/bottleneck, occupancy/waiting, resource-dependent
performance, setupTime, capacityLiters, dominated choices, projection and stochastic/deterministic
execution. Main's source was checked for duration/selection behavior and Challenge's lack of Factory
dependencies; the research runner, fixture projection, occupancy oracle and corpus/replay tests were
inspected. There were no open GitHub PRs at the live query, and no required surface was unavailable.

Main advanced since the report through the analytics split, reviewer-process changes and expanded
resource/game research portfolio. The only product-source diff is a RuntimeObservation Javadoc
correction, not executable behavior. The new product/research wording materially narrows ownership,
human-choice and future-semantic interpretations; it does not invalidate the executions.

**Experimental observations.** The original harness was executed against an isolated archive of the
exact review baseline inside the running development container (Temurin 25.0.4, repository Gradle
wrapper 9.7.1, Java release-21 compilation). All three original experiment methods passed. All **21
persisted result files**, including summaries and candidate replay, reproduced byte-for-byte; hashes
are in [reproduction.json](factory-design-game-strategy-space-review-experiment/results/reproduction.json).

The independently authored
[Java experiment](factory-design-game-strategy-space-review-experiment/StrategySpaceAdversarialTest.java)
uses Factory publication and the supported research runner/events/observations only. It does not call
the original experiment's projector, frontier/classification helpers, scheduler, handler or stores.
It checks one accepted workload, a complete evidence window, one order completion matched to the
closing observation, and absence of availability transitions. It enumerates all distinct type
interleavings of every reference-budget-affordable design: **519 executions across 36 designs**.
This is exhaustive relative MachineId ordering modulo identical offer occurrences, not enumeration
of all possible ID magnitudes, names or Factory models. The current selector uses relative ID order.
All 24 S2 permutations were also replayed as complete normalized evidence bundles; reversing only the
resource list while preserving IDs left completion unchanged for each. The latter checks completion,
not universal model or event-stream equivalence.

**Research-local derivations.** The independent [CSV audit](factory-design-game-strategy-space-review-experiment/audit.py)
reconstructed all **360 global frontier contexts** using a different dominance calculation, including
tie members, then verified targets, budgets and feasible-frontier cardinality in **5,760 order/cell
records**. No mismatch was found. It reproduced the aggregate D/F counts and tests alternative
thresholds and fixed-budget repricing. The auditable results are
[audit-summary.json](factory-design-game-strategy-space-review-experiment/results/audit-summary.json),
[identity-orders.csv](factory-design-game-strategy-space-review-experiment/results/identity-orders.csv)
and [interventions.csv](factory-design-game-strategy-space-review-experiment/results/interventions.csv).

**External evidence:** none used. This bounded review tests the exact repository's execution and
research arithmetic; external manufacturing/game analogies cannot establish those outcomes. No
external precedent, general industrial-performance claim or human study is inferred.

## Challenges and results

### Occupancy is not a causal constraint definition

**Qualified; a generalized identification claim is falsified.** The report explicitly declares its
method, but repeatedly moves from the highest connected-pool occupancy to language such as “the
constraint.” Connected eligibility does not make every slot in that component interchangeable.

A counterexample within its original catalogue bounds uses P2/N12 with one cutter, six dedicated
assemblers, no inspector and one flex. It completes at 67. The report's formula ranks CUT highest:
36/67 versus 108/(7×67) for the connected assembly/inspection pool. Adding one or two cutters leaves
completion at 67; adding an inspector changes it to 45, as does adding a flex. An all-pool average
hides the inspection capacity restriction. The base and decisive cutter/inspector additions are
within catalogue count bounds; this is a diagnostic counterexample, not a candidate within the
reference's 1,062 budget. The supplementary +assembler probe exceeds the original assembler limit
and is not used in this argument.

The positive family's migration claim nevertheless survives **without this formula**:

| Base (P2/N12) | Base T | +cutter | +assembler | +inspector |
|---|---:|---:|---:|---:|
| One dedicated resource per step | 67 | 67 | 67 | 56 |
| One cutter, one assembler, two inspectors | 56 | 56 | 45 | 56 |
| One cutter, two assembly slots, two inspectors | 45 | 37 | 45 | 45 |

These direct experiments show where the effective marginal intervention moves: inspection, then
assembly, then cutting. Two cutters as the closer cost match also give zero in the first two rows.
The prices are bounded and comparable, not exactly equal. The two-slot third row was tested both as
two singles and as one twin. Existing resource IDs were preserved by appending added resources.

All these fixtures remain online throughout. Elapsed time × concurrency is therefore a valid
available-capacity denominator here; no availability adjustment is missing in these results. It is
not a general effective-availability formula for future transition-bearing fixtures. Retain named
occupancy as descriptive research measurement; do not promote it into a universal bottleneck rule
or settled analytics ownership.

### Two block orders are not performance extremes

**Qualified; core existence strengthened.** The exhaustive affordable sweep exposes orders outside
the C/R completion envelope. With P2/N12, `C1A2T0I0F1` costs 863: CAAF and FAAC both finish at 67,
while CAFA finishes at 70. Likewise `C1A1T1I0F1` costs 1,023: CATF and FTAC finish at 67, CAFT at 70.
Letters describe types in increasing MachineId order; T has concurrency two. Flex-first/last are
priority endpoints, not guaranteed makespan endpoints.

Those two counterexamples do not meet target 57 under any order and do not destroy the positive.
Across **all** orders of all 36 affordable designs, even each S1/S2/S3 design's worst outcome is
undominated by every other design's best outcome. Thus the core trio survives a stronger adversary
than one common global ordering policy. S2 uses its flex for assembly and inspection in all 24 orders.

Identity changes can still change eligibility for a challenge: S2's 46 versus 47 would cross a
deadline of 46. That example changes the challenge target; it does not claim failure at the selected
57. Fixing a deterministic projector makes results reproducible, but does not establish fairness,
player-visible relevance, or invariance to player-irrelevant identity allocation.

The large window counts require additional precision. The pass-2 protocol re-derives target and
budget per order; **312 of 2,880 cell identities** change at least one of those numbers. Also **59**
order-robust-material cells have no identical surviving material pair. Its 735/271 figures describe
the declared two-order parameter-rule experiment, not a guarantee that 735 fixed challenges or pairs
survive every identity ordering. The selected reference keeps its actual budget/target fixed and
is independently secured by the exhaustive sweep.

### Projection-order ownership

**Qualified.** Current planning explicitly locates a future projector in consumer-side integration,
and current Challenge has no Factory dependency or production projector. “Challenge-owned content”
is a proposed allocation, not established authority. The supported requirement is narrower:
**a future projector must make result-affecting resource identity/order explicit and deterministic
under the Engine semantics it consumes**. Where that policy is represented, how edits preserve
identity, and how players encounter it remain unresolved integration/product decisions.

The final MachineId rule remains normative. Neither this review nor the report automatically promotes
the separate CANDIDATE final-tie-break question or reopens combinedQueueDepth.

### Economics, materiality and preregistration

**Qualified; bounded positive survives.** Git history preserves pass-1 protocol before harness/results,
then pass-2 protocol after pass-1 results and before pass-2 execution. That verifies persisted
chronology, not an unverifiable claim about all prior private thought. Revising the discriminator
after seeing small dedicated effects is disclosed exploratory refinement; it is not an independent
confirmation on a new randomly sampled population. Candidate selection is likewise a predeclared
search rule over the window, not an ex ante successful reference guess.

The pair classifier treats every structurally explained flex-count change as POOLING, with no 5%
magnitude threshold. Dedicated pairs instead face the surplus-removal threshold. Consequently
“material” is mechanism-dependent, and “no threshold involved” does not prove materiality. This is
not an implementation bug relative to the protocol; it is a limitation of that operational definition.

A different, uniformly applied sensitivity rule keeps an original explained pair only if its
completion gain is at least the stated percentage of the cheaper design's completion:

| Uniform pair separation | D cells with a qualifying pair in both orders | F cells with a qualifying pair in both orders |
|---|---:|---:|
| 2% | 37 | 754 |
| 5% | 32 | 661 |
| 10% | 23 | 517 |
| 20% | 8 | 289 |

These are a **replacement sensitivity metric over original explained pairs**, not the author's
surplus-removal metric, not counts passing every proving case, and not all-permutation robustness.
They show how reported prevalence depends on the definition. The narrower S1/S2 existence result
passes 2/5/10/15% uniformly across all its orders and fails 20%; no definition-free “meaningful”
threshold follows. A 5% criterion does not establish three pairwise-material reference options.

Independent repricing holds **budget 1,062 and deadline 57 fixed**, avoiding automatic budget/target
rescue. With other reference prices fixed, S1 and S2 remain on the C/R feasible frontier at flex prices
280, 300, 313, 330, 350, 375 and 400. At 450 and 500, S2 is displaced by dedicated capacity. At 250,
cheaper pooled alternatives displace S1. Thus pooling's existence is not a one-credit equality trick,
but the 1.0–1.5× band is no universal pricing law; even this fixed challenge has positives at 1.6×.
At price 313, any budget in [913, 1,062] and target at least 56 retains the verified S1/S2 pair.
Zero slack over the minimum meeting cost excludes a strictly more expensive pair by construction;
the zero-slack negative is partly an algebraic consequence, not an independent empirical discovery.

### Dedicated-only negative and the word “only”

**Reject necessity; retain a scoped tendency.** The original data already contain nine dedicated-only
cells passing every pass-2 proving case and nineteen with its material multiplicity. For example,
P1/N6/WORK/twin-75%/target-fraction-40%/slack-25% has target 24 and budget 1,187. The non-nested
dedicated designs (2,2,2,0) and (1,3,2,0) cost 950/1,150 and finish at 23/21. Their recorded surplus
effect is 2 ticks against a 2-tick threshold; both orders agree. Small, threshold-sensitive effects
may justify preferring the stronger flex family, but passing the declared test cannot be erased by
calling it fine tuning afterward.

Replace “exists only with multi-step-eligible offers” with “a robust constructive example exists with
shared eligibility; the tested dedicated family usually produces nested capacity purchases, with
small finite-batch exceptions.” No universal dedicated-only impossibility or flexibility necessity
was proved. In particular, the report's proposed automatic negative if a product excludes shared
offers must not be promoted unchanged.

This negative is limited to current semantics and the tested family. Step-level durations and
inert setup/volume fields cannot express cheap/slow versus expensive/fast machinery. That fact
neither recommends nor rejects future operation-resource performance, qualification, setup,
availability or stochastic semantics. Those are separately bounded reopening conditions.

### Mechanical ground truth, offered choices and human value

**Qualified.** Mechanical non-dominance, non-nested provision, supported dispatch and controlled
intervention effects are established. The report generally keeps player comprehension out of scope,
but “interesting,” “meaningful” and “explainable” must be read as these mechanical properties only.
No presentation, target-user comprehension, discoverability, engagement or learning evidence exists.

A dominated or execution-equivalent offer can still matter through price, catalogue limits,
availability, presentation, aesthetics, or a deliberate equipment-value discovery task. The optimizer's
frontier is ground truth, not a prescription to remove all other offers. Conversely, possible human
confusion or aesthetic preference cannot rescue a mechanical null within this investigation.

### Twin/singles, vocabulary and realism

**Unchanged within scope; broader readings rejected.** Original completion controls reproduce for
13,680 twin-versus-two-single pairs across C/R. They establish aggregate completion equivalence in
this always-online workload family at equal total concurrency. They do not establish Factory content
equivalence, equality of per-resource assignments, or general equivalence under availability changes,
recovery, other eligibility, prices or quantity limits. “Price choice only” should be restricted to
the tested mechanical dimensions, not all possible product value.

CUT/ASSEMBLE/INSPECT are fixture names. The transferable mechanism is a serial route with unequal
step durations, dedicated capacity and capacity eligible for multiple steps under a fixed workload
and capital constraint. No result for branching, longer routes, multiple products/orders or spatial
transfer is established. A supplementary one-unit control gives 12 ticks for starter, S1, S2 and S3:
the reference pooling advantage disappears when there is no concurrent work to exploit. This lies
inside the brief's semantic scope but outside the original N={6,12,24} window. It falsifies a universal
pooling-benefit claim, not the existential N12 result. N6 and N24 controls retain the same direction.

Determinism establishes repeatability for complete fixed inputs, not industrial realism. No random
resource choice is proposed. Future explicit random context, failures, repairs, yield or arrival
semantics could alter strategy robustness while preserving the repository's determinism contract.

## Required qualifications for reconciliation

1. Promote only bounded mechanical existence, with the reference workload, catalogue and economic
   basis or an explicitly revalidated successor. Do not claim shared eligibility is necessary.
2. Preserve the dedicated exceptions and the method-dependent nature of “material” and “strategy.”
   Prefer the independently demonstrated S1/S2 separation over prevalence counts as the main evidence.
3. Use controlled marginal interventions to support the reference constraint-migration explanation.
   Maximum connected-pool occupancy is descriptive and has a demonstrated causal counterexample.
4. Make result-affecting projection identity/order explicit; preserve its player-relevant sensitivity
   and unresolved ownership. C/R block orders do not bound all outcomes.
5. Preserve the distinction between re-derived parameter-rule cells and fixed challenge robustness,
   and between pair substitution across orders and survival of the same pair.
6. Keep mechanical value, offered choice structure, perceived value and player preference separate;
   granularity and inert-field controls do not ban product-level distinctions.
7. Keep current deterministic, non-spatial, always-online, serial-workload limitations. Future
   semantics are reopening conditions, not implicit defects or admitted work.
8. Reconcile evidence into the appropriate product/research/planning authority separately, preserving
   analytics and diagnostic research boundaries. No Engine change or playable admission follows here.

## Remaining unknowns and stopping decision

Unknowns include all-permutation behavior outside the affordable reference, larger/different routings,
independent target-user interpretation, fair identity allocation in a real editor, reusable diagnostic
ownership, and future setup/resource-performance/availability/stochastic semantics. No universal
bottleneck definition, universal equipment-price band or industrial optimization rule has been found.

**No more evidence is required before separate reconciliation of the narrow surviving result with
all the qualifications above.** Additional claims—especially a mandatory flex catalogue, a general
diagnostic algorithm, three materially separated alternatives, or a human-facing claim—need their own
evidence. The observed counterexamples qualify auxiliary/generalized claims without destroying the
load-bearing constructive positive, so REOPEN is not the disposition for this exact bounded result.

The reviewed packet should retain these counterexamples, algorithms and output custody until a
reconciliation transfer audit decides what is promoted and what is explicitly discarded. This review
does not perform that audit or promote synthesis seeds.

## Execution and custody notes

The maintained checkout contains only new transient research artifacts. No product code, canonical
architecture, research register or planning document was edited. Execution used a disposable exact-main
archive under `/tmp/arcogine-strategy-review-20261001` in the existing devcontainer; temporary Java test
copies there are execution copies of the workspace assets, not maintained product additions.

Validation commands, run from that archive's `product/`:

```text
bash ./gradlew :factory:test --tests com.arcogine.factory.research.StrategySpaceAdversarialTest --tests com.arcogine.factory.research.StrategySpaceExperimentTest --console=plain
bash ./gradlew :factory:test --tests com.arcogine.factory.research.StrategySpaceAdversarialTest --console=plain
python3 <review-experiment>/verify_reproduction.py <archive>/product/domains/factory/build
python3 <review-experiment>/audit.py
```

Outcomes: four experiment methods passed in the combined run; the independent test passed again after
adding the occupancy counterexample; 21 original files matched byte-for-byte; all independent audit
assertions passed. Initial execution mistakes were corrected: the module task is `:factory:test`,
not a task derived from the filesystem directory, and `completedAt()` returns SimTime directly.
No failed product behavior was observed. Full contribution/security gates and JDK21 runtime execution
were not run: this is transient research, not a product change or release certification. No
implementation PR or CI handoff was created; exact-head `CI / gate` is not applicable to this review.
The tracked-workspace final-tree check is deliberately not an acceptance check for an active evidence
branch, which must not be merged in its current form.

**Final disposition: ACCEPT WITH QUALIFICATIONS. This is an independent fresh-session adversarial
review with disclosed handoff framing exposure, not the original author's self-review.**
