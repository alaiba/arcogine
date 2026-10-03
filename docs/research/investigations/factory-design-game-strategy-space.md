# Factory-Design Game Strategy-Space Research

> **Status:** CONCLUDED — see the maintained [research register](../research-register.md)  
> **Scope:** Whether current non-spatial production/capacity semantics plus game-owned capital constraints can produce a useful first design problem  
> **Authority:** Product research record only; it defines no Factory/Engine semantics, Challenge capability, scoring rule or implementation admission

## Conclusion

> Under current deterministic, non-spatial Factory/Engine semantics, a deliberately small serial fixed-workload challenge can exhibit mechanically non-nested capital/capacity trade-offs. A bounded reference family using shared multi-step-eligible capacity provides a robust constructive case: at least two materially separated, nondominated structures survive all tested resource-identity orderings and bounded economic perturbation. Shared eligibility is not established as necessary; the tested dedicated-only family usually collapses toward nested capacity purchase but contains smaller finite-batch exceptions.

This is **mechanical ground truth** for one bounded reference. It establishes non-identical structures, nondominance under the declared cost and completion dimensions, a material performance difference in the selected pair, actual shared-resource usage, controlled intervention effects, and a bounded capital/performance trade-off. It does not establish engagement, player comprehension, discoverability, fairness of presentation, learning efficacy, or any population-level interpretation; those stay with the diagnostic-evidence, equipment-value-discovery, scoring/level and external-player questions.

### Outcome against the original hypotheses

| Hypothesis | Outcome |
|---|---|
| **Null** — a small challenge collapses to monotone capacity purchase or to fragile, arbitrary distinctions | **Holds in general for the tested dedicated-only family**, whose frontier is usually a chain of bottleneck purchases, with smaller finite-batch exceptions. **Does not hold for the selected shared-capacity reference.** |
| **Alternative** — a bounded region exists where capacity at the constraint helps, capacity away from it does not, relieving one constraint exposes another, and at least two structurally different feasible designs survive without either dominating | **Supported for the selected reference**, within the bounds below. Not shown to be a general property of non-spatial challenges. |

None of the brief's falsification conditions is met by the selected reference: no design dominates the others on cost and completion, improvement is not merely monotone purchase, the multiplicity survives repricing and resource-identity order rather than depending on arbitrary price tuning, and every difference is explained from supported dispatch evidence.

## Bounded question

Can a deliberately small, fixed **non-spatial** production challenge produce several materially different, explainable viable designs using only current production/routing/resource-capacity semantics, one fixed quantity-bearing production requirement, a bounded game-owned equipment catalogue and construction cost, and a game-owned budget and completion target?

**Decision at stake.** Whether the first factory-design product kernel already has a meaningful capacity/capital design space before spatial layout is introduced, or depends on unresolved semantics to become interesting. The answer is a bounded positive for a constructive reference. It is not evidence that the current mechanics are interesting in general, and it is not a license to infer that spatial features are unnecessary.

**Out of scope** and untouched: spatial layout and transfer, player comprehension and presentation, score/rating formulas, level structure, changing Engine dispatch or scheduling semantics, and manufacturing several orders to create parallelism (Arcogine owns quantity decomposition).

## Evidence basis

Exhaustive deterministic enumeration of a pre-declared parameter window (two routing profiles, quantities 6, 12 and 24 submitted as one order, a five-offer catalogue with a dedicated-only variant) through supported Factory publication and the supported research runner, using only supported events and observations. The protocol was registered before the run and revised once, after the first pass, to add controls that could only make a positive harder to claim. That revision is disclosed exploratory refinement, not independent confirmation on a fresh population, and the reference was selected by a predeclared rule over the window rather than guessed in advance.

An independent adversarial review (`ACCEPT WITH QUALIFICATIONS`) reproduced every persisted result byte for byte, re-derived the frontiers with a different dominance calculation, and executed every distinct resource-type identity interleaving of every affordable reference design (519 executions across 36 designs). It was a fresh isolated session with disclosed exposure to the handoff's summary; it did not use a different model family. The question is medium risk: it shapes a consumer's product requirements and how that consumer relies on a normative Engine tie-break, but decides no identity, persistence, determinism, compatibility or ownership contract. No external evidence was used, because none would discriminate between the candidates beyond what exhaustive execution of current semantics established.

## Selected reference challenge

Every value below is a game-owned research parameter of one reference, not a Challenge catalogue, a Factory equipment type or a contract. Offer names are fixture vocabulary.

| Element | Value |
|---|---|
| Routing | Serial `CUT` 3 ticks → `ASSEMBLE` 4 → `INSPECT` 5. Duration belongs to the step, so every eligible resource processes a step in the same time. |
| Workload | One order of quantity 12, submitted once |
| Catalogue | `CUTTER` (`CUT`, concurrency 1, up to 4) 150 · `ASSEMBLER` (`ASSEMBLE`, 1, up to 6) 200 · `TWIN_ASSEMBLER` (`ASSEMBLE`, 2, up to 3) 360 · `INSPECTOR` (`INSPECT`, 1, up to 4) 250 · `FLEX_CELL` (`ASSEMBLE` and `INSPECT`, 1, up to 3) 313. `setupTime` is 0 and `capacityLiters` absent. |
| Budget | 1,062 credits, 25% above the 850-credit cheapest design that meets the target |
| Target | Completion within 57 ticks |
| Dimensions | Construction cost and completion tick only; Pareto dominance; no weighted score |

### Feasible frontier

| Design | Offers | Cost | Completion | Mechanism |
|---|---|---:|---:|---|
| S1 | cutter, assembler, two inspectors | 850 | 56 | The assembler paces the run; the two inspectors are occupied for 60 of 112 inspector job-ticks |
| S2 | cutter, assembler, inspector, flex cell | 913 | 46–47 | The flex cell is dispatched for both assembly and inspection in every tested identity order, turning spare inspection capacity into assembly capacity for 63 credits |
| S3 | cutter, twin assembler, two inspectors | 1,010 | 45 | Dedicated two-slot assembly relieves assembly, which makes cutting the limit |

A fourth design (cutter, assembler, two flex cells; 976 credits, 46 ticks) is on the frontier only under identity orders for which S2 completes at 47. S2's completion is 46 or 47 depending on resource-identity order; S1 and S3 do not vary. The catalogue's fastest design (20 ticks) costs 2,946–3,209 credits against the 1,062 budget, so capital binds: inside the budget every tick bought costs capital (S1→S2 buys 9–10 ticks for 63 credits; S2→S3 buys 1–2 ticks for 97).

**Materiality is claimed for the S1/S2 pair only.** It stays separated at a uniform completion-improvement threshold of 2%, 5%, 10% and 15% in every tested order (worst case 9 of 56 ticks) and fails at 20%. S3 is mechanically distinct and nondominated, but its separation from S2 is only 1–2 ticks, so the result is two strongly separated alternatives plus a third nondominated one, **not** three pairwise materially separated designs.

### Marginal interventions

Completion in ticks after adding one resource to the base, on the same routing, workload and always-online resources. Added resources are of comparable, not equal, price; two cutters, the closer cost match, give the same result as one in the first two rows.

| Base | Completion | + cutter | + assembler | + inspector |
|---|---:|---:|---:|---:|
| One dedicated resource per step | 67 | 67 | 67 | 56 |
| S1 (one cutter, one assembler, two inspectors) | 56 | 56 | 45 | 56 |
| One cutter, two assembly slots, two inspectors | 45 | 37 | 45 | 45 |

For this reference, inspection capacity matters first; after inspection is relieved, assembly capacity matters; after assembly is relieved, cutting capacity matters. This is the evidence for the proving cases the brief required — capacity at the constraint improves the outcome, similar-cost capacity away from it does not, and relieving one constraint exposes another — and it is a bounded proving result, not a universal scheduling theorem. Most single additions from a given base change nothing, so a controlled retry must choose its base deliberately.

### Demonstrated robustness

- **Resource identity.** Across every distinct resource-type identity interleaving of all 36 affordable designs, the worst tested outcome of each of S1, S2 and S3 is not dominated by any other affordable design's best tested outcome. The two block orderings (catalogue order and its reverse) are **not** performance extremes: some affordable shared-resource designs finish up to three ticks later under an interleaved order than under either block order.
- **Economics.** With budget 1,062 and target 57 held fixed (no budget or target rescue), S1 and S2 remain on the feasible frontier at flex-cell prices of 280, 300, 313, 330, 350, 375 and 400 under both block orderings. S2 is displaced by dedicated capacity at 450 and 500, and at 250 cheaper pooled alternatives displace S1. At the reference prices the pair is retained for any budget from 913 to 1,062 and any target of at least 56. A budget with no slack above the cheapest target-meeting design admits no costlier alternative, by construction.

## Qualifications that constrain the result

These are part of the accepted result, not commentary on it.

1. **Shared eligibility is constructive, not necessary.** Shared multi-step-eligible capacity produced the strongest and most robust family tested. The tested dedicated-only family usually behaves like nested capacity purchase, but finite-batch exceptions exist and met the original proving criteria (for example, a six-unit order on a `CUT` 2 / `ASSEMBLE` 6 / `INSPECT` 3 routing admits two non-nested dedicated designs that differ by two ticks). They are small and threshold-sensitive, yet a passing test cannot be discounted after the fact. A product that excludes shared offers is therefore **not** automatically a negative result; it needs its own revalidation.
2. **Occupancy is measurement, not a causal constraint definition.** The generalization that the connected pool with the highest occupancy is the active constraint is falsified. With one cutter, six dedicated assemblers, no dedicated inspector and one flex cell on the same routing and workload, authored in that order (the flex cell after the assemblers; a diagnostic base outside the reference budget), the run completes at 67 ticks and cutting has the highest pool occupancy (36 of 67 job-ticks, against 108 of 469 for the connected assembly/inspection pool). Adding a cutter leaves completion at 67; adding an inspector brings it to 45. Resource order matters here too (qualification 3): across all 56 distinct interleavings of resource types the base completes at 67 to 70 ticks, the authored order being the fastest because a single inspection slot cannot start before tick 7 and needs 60 ticks, a second cutter never beats 67 in any order, and a second inspector completes before the base design's best under every order. Occupancy stays a descriptive research-local measurement; controlled marginal interventions are the evidence for the reference's effective constraint. This investigation defines no repository-wide bottleneck or constraint algorithm and settles no analytics ownership.
3. **Resource identity/order is result-affecting.** Under current Engine semantics a remaining selection tie is broken by `MachineId`, so for a resource eligible for more than one step the order in which resources are authored can change completion. S2 finishes at 46 or 47 depending on it, which means a deadline of 46 would make S2's eligibility identity-dependent. Dedicated-only designs in the tested family are unaffected in aggregate completion. The supported consequence is the requirement below; this result does not promote the separate final-tie-break question or reopen `combinedQueueDepth`.
4. **Materiality is definition-dependent.** The original classifier treated pooling and dedicated mechanisms differently, and replacing it with a uniform separation threshold moves prevalence counts substantially. Aggregate prevalence counts are therefore not a measurement of "meaningful strategy space" and are deliberately not promoted. The selected reference's demonstrated S1/S2 separation is the evidence; no universal materiality percentage follows.
5. **Economics are bounded challenge content, not a law.** The reference's fixed budget and deadline survive the flex-cell price range above, so the positive is not a one-credit knife edge, but no universal equipment-price ratio was established.
6. **Mechanical strategy is not human comprehension.** Dominated or mechanically equivalent offers can still be useful content for bargain or opportunity discovery, presentation or player preference; conversely, perceived value cannot rescue a mechanically trivial kernel.
7. **Twin versus two singles is a narrow control.** At equal total concurrency a two-slot offer and two single-slot offers produced equal aggregate completion in the tested always-online family. That is not Factory content equivalence, equality of per-resource dispatch assignments, a ban on differently priced or presented offers, or a statement under availability changes or richer resource semantics.
8. **Pooling needs concurrent work.** For a single-unit order, the starter design, S1, S2 and S3 all finish in 12 ticks: the pooling advantage disappears when there is nothing concurrent to exploit.

### Claims that did not survive review

These appeared in the original report, were falsified or narrowed by the independent review, and must not be reintroduced: that useful strategy space exists **only** with multi-step-eligible resources, and that excluding them is an automatic negative; that the highest connected-pool occupancy identifies the constraint; that all three reference designs are pairwise materially separated; that the two block orderings bound performance across resource identities; that aggregate prevalence counts measure meaningful strategy space; that equipment price must sit in a universal 1.0–1.5× band; that twin versus single granularity is "a price choice only" in any sense beyond the tested mechanical dimensions; and that the projection order is established as Challenge-owned content.

## Promoted reference-challenge requirements

These bound a future reference non-spatial challenge. They authorize no scoring formula, spatial mechanic, UI technology, Engine or Factory semantic change, or implementation.

1. **Reference mechanism.** A first reference challenge may use the selected shared-capacity serial family, or an explicitly revalidated successor, as its proving case. The choice is a strong constructive case, not a semantic necessity, and no catalogue is required to contain a shared offer.
2. **Revalidation, not inheritance.** A successor basis (different routing, catalogue, prices, budget or target) must re-establish, for itself, at least two non-nested nondominated structures with a stated separation and stated robustness to resource-identity order and repricing, rather than inheriting this result. Prices, budget and target are game-owned content; no universal pricing ratio or materiality threshold is implied.
3. **Budget slack.** The budget must leave enough slack above the cheapest target-meeting design to permit real alternatives.
4. **Projection identity/order.** Under current Engine semantics, any future game/consumer projector must make result-affecting configured-resource identity and order explicit and deterministic. This is a requirement on the integration boundary, not an assignment of ownership. Where identity is allocated, how edits preserve it, and whether `MachineId` tie-breaking is an acceptable player-visible policy are separate questions.
5. **Truthful mechanical claims.** Under current semantics a challenge must not state or imply a mechanical performance difference from machine granularity, `setupTime` or `capacityLiters`. Offers may still differ in price, availability and presentation.
6. **Solution set versus offered set.** The reference's mechanical frontier is ground truth, not a prescription that every offered item be nondominated.

## What this does not establish

- Any result for branching or longer routings, several products or orders, spatial transfer, resource-dependent speed or performance, executable setup/changeover, resource characteristics or qualification, machine failures or repairs, stochastic arrivals, yield or duration, or a larger quantity range than 6–24 plus the single-unit control.
- A universal bottleneck or constraint definition, equipment-price band, materiality threshold, or scheduling rule.
- Industrial realism. Determinism establishes repeatability for complete fixed inputs, not realism.
- Any of the human-facing properties named in the Conclusion.

### Reopening conditions

Revalidate the reference when any of these occurs; none is admitted work by itself.

- An Engine selection, backlog or recovery-cascade rule changes, since the shared-capacity result depends on them.
- Resource-dependent performance, qualified applicability, executable setup/changeover, availability change, or stochastic behavior becomes supported semantics (each has its own research question in the register).
- A concrete product decision excludes shared offers, or wants branching or longer routings, several orders, or spatial layout to be consequential.
- A consumer objective, fairness contract or representative workload fires the reopening boundary of the Engine final tie-break question; that evaluation belongs to that question.

## Downstream consequence

- **Product programme and implementation gate.** Strategy space is satisfied for a minimal non-spatial slice. [Diagnostic evidence](factory-design-game-diagnostic-evidence.md) is the remaining product-evidence blocker; no playable slice is admitted ([programme](factory-design-game-vertical-slice.md), [implementation gate](../../planning/factory-design-game-vertical-slice.md)).
- **Diagnostic evidence** (READY) consumes this reference as mechanical ground truth and as a known misleading-constraint counterexample (qualification 2). It supplies no diagnostic method and no analytics ownership; [Simulation analytics ownership](simulation-analytics-consumer-boundary.md) keeps that question.
- **Controlled retry** (CANDIDATE) now has a challenge family to compare, with the base-choice caveat above.
- **Scoring/level structure** and **equipment-value discovery** (CANDIDATE) now have a bounded mechanical solution family. Any fixed scalarization selects one frontier design: the landed reference evaluation policy's additive score would favor S1 (214, against 160–161 for S2 and 65 for S3), a research-local side check and not a proposal.
- **Challenge.** The reference's economics (purchase cost, quantity limits, budget, deadline, one fixed quantity) are expressible with the facts the landed headless Challenge capability already carries; the steps and concurrency an offer projects to are deliberately not Challenge facts. No new Challenge implementation follows ([Challenge delivery plan](../../planning/factory-design-game-challenge-readiness.md)).
- **Consumer integration.** The projection identity/order requirement is recorded in the [consumer plan](../../planning/factory-design-game-consumer.md). The consumer is not admitted.
- **Factory and Engine.** No change. `MachineId` tie-breaking is already normative in [Engine semantics](../../architecture/engine-semantics.md). That step-level duration and inert setup/volume fields cannot express cheaper-but-slower versus costlier-but-faster equipment neither recommends nor rejects resource-dependent performance, qualification, setup, availability or stochastic semantics; each stays with its own register question.

## Durable proving assets and knowledge-transfer outcome

The report, the independent review and the experiment outputs were held in a temporary research-evidence workspace. Each material result has a destination below or an explicit discard, no exact artifact needs to stay readable, and the maintained conclusion does not depend on that workspace remaining fetchable. It is retirement-eligible once this reconciliation lands and its independent review validates the transfer. No separate historical decision-rationale record is kept: the decisive distinctions are current constraints on how to read the result, so they live here beside the result (qualifications and the list above), and the reconciliation is neither foundational nor hard to reverse. No synthesis seed is admitted: "Pareto non-dominance on cost and time is not evidence of a strategy space" and "the most occupied resource is not necessarily the binding constraint" are recoverable from first principles, and the Arcogine instances are retained here, so the loss-sensitivity test is not met.

The capacity corpus of the research-experiments substrate already preserves, as executable fixtures with hand-derived expectations, the parts of this result that cover its know-how: [`CapacityCorpus`](../../../product/research-experiments/src/test/java/com/arcogine/research/experiment/CapacityCorpus.java) and its [test](../../../product/research-experiments/src/test/java/com/arcogine/research/experiment/CapacityCorpusTest.java) pin S1 (56 ticks, each inspector inspecting six units) and S2 in authored order (47 ticks, the shared resource serving both steps), the merging of assembly and inspection into one occupancy pool, the contrast between waiting-work and occupancy rankings, resource-order sensitivity for a smaller shared design (and its absence for a dedicated one), and replay determinism. This reconciliation adds the occupancy counterexample to the corpus (three fixtures for the base, a second cutter and a second inspector, and two tests: the relation between occupancy and added capacity, and the completion range over every resource-type interleaving). The occupancy oracle's own Javadoc already states that occupancy is a measurement and not a constraint.

| Result | Fate |
|---|---|
| Accepted conclusion, reference, S1/S2/S3 frontier, mechanism | Promoted here and into the register, programme and planning gate; S1 and S2 (authored order) also preserved as corpus fixtures |
| Qualifications 1–8 and the claims that did not survive | Promoted here; qualification 3 also into the consumer plan |
| Occupancy counterexample (parameters in qualification 2) | Promoted here with its parameters and preserved as executable corpus fixtures with hand-derived expectations, plus an all-interleavings order test. It is available to the diagnostic-evidence study as its required known misleading-constraint case, which that study may reuse without owning the proof |
| Intervention sequence (inspection, then assembly, then cutting) | Promoted here as a bounded result; starter (67) and S1 (56) pinned by corpus fixtures; the remaining rows are re-derivable and belong to the diagnostic-evidence corpus's required constraint-migration pair |
| Identity-order robustness, block orders not an envelope | Promoted here; the phenomenon is pinned by the corpus resource-order tests; the sweep itself is discarded (reconstructable from the parameters above) |
| Dedicated-only finite-batch exception | Promoted here with one example; the other cells are discarded |
| Materiality-threshold sensitivity | Promoted here as S1/S2 thresholds plus the definition-dependence; the sensitivity tables and prevalence counts are discarded |
| Fixed-budget repricing | Promoted here with the tested price range |
| Single-unit control | Promoted here as qualification 8 |
| Twin-versus-singles control | Promoted here, narrowly; the pair counts are discarded |
| Projection ownership and identity allocation | Retained as unresolved integration/product questions in the consumer plan, with the final tie-break question left CANDIDATE |
| Harness, protocols, classifier, raw outputs, audit scripts | Discarded. The parameter rules above plus the research-experiments substrate reconstruct a rerun. The classifier's definition-dependence is exactly what the review qualified, and keeping it would invite re-reading prevalence counts as measurement |
