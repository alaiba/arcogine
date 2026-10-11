# Simulation analytics ownership — Phase 2 revision against reconciled execution evidence

> **Research status:** `READY`. This is the admitted Phase 2 continuation of the single simulation
> analytics ownership question, revised against the reconciled captured-execution-evidence contract,
> the inert-marker correction and the landed removal of the per-resource busy counter. The register
> is not changed; the evidence/provenance question stays `CANDIDATE`.
>
> **Research baseline:** live `main` `a7f9856617d99e796d528a44aa8c15677c485f55` (#469), resolved
> 2026-10-11. The handoff was prepared against `a80cd6bf83d478dedb7a782143f93674cb5019e0` (#466);
> `main` has since gained #469, which removes `busyTicks`. That drift is material and is carried
> throughout (§2).
>
> **Final live-main recheck:** see §2.3.
>
> **Workspace branch:** `workspace/simulation-analytics-ownership-phase-1` (the existing analytics
> workspace, reused). Handoff: commit `0a82429d2605c59a653b32f831b1bd2abb27e7c7`,
> `workspace/research/handoffs/simulation-analytics-ownership-phase-2-post-execution-evidence-revision.md`.
>
> **Risk:** High — major ownership, supported observable semantics, temporal evidence sufficiency.
>
> **Authority:** research evidence only. Nothing here changes architecture, a specification, the
> runtime contract, the Determinism Contract, planning, the register, a brief or product code. The
> research-local bundle, closure and definition types in the probes are not proposals for a
> production evidence format, holder class, account identifier, analytics database or module.
>
> **Adversarial-review status:** required, not yet performed. Only the author's self-challenge (§15)
> applies. The earlier **REOPEN** (`2c85fe7c`) binds to the original Phase 2 report (`6a5c0435`)
> only. The subsequent revision (`75736ddd`) was **never independently reviewed**; this report
> inherits no acceptance from it. Nothing here is decision-quality evidence for reconciliation
> until a genuinely independent review of this exact revision exists.

## 1. Evidence custody

Every artifact below was resolved at its exact commit and read in full before use, except where a
row says otherwise. All remain fetchable (§2.3).

| Artifact | Commit | Path | Role here |
|---|---|---|---|
| Phase 1 qualified checkpoint | `7e72a46506bf63febcb57d7b7dcc9ce0cd484d64` | `workspace/research/investigations/simulation-analytics-ownership-phase-1-current-boundary.md` | Current-boundary classification and Q1–Q6 (§11) |
| Original Phase 2 report (immutable) | `6a5c043563e3bd89fae6826f830b377b43643d46` | `workspace/research/investigations/simulation-analytics-ownership-phase-2-admission-boundary.md` | R1–R5 package reviewed and reopened |
| Independent review of the original, **REOPEN** | `2c85fe7c03c6256c9e8a2ce73fde5091b3357e3c` | `workspace/research/investigations/simulation-analytics-ownership-phase-2-adversarial-review.md` | Binding challenges A–J; tracked in §5 |
| Subsequent revision, **not independently reviewed** | `75736dddfa3fe1ae89c5089720c702f4ca595ab5` | `workspace/research/investigations/simulation-analytics-ownership-phase-2-admission-boundary-revision.md` | Primary candidate stress-tested here; Rules 1–8, staged D-plus-E |
| Original custody experiment | `6a5c043563e3bd89fae6826f830b377b43643d46` | `workspace/research/experiments/simulation-analytics-ownership/…/PerformanceEvidenceSufficiencyExperiment.java` | No longer compiles at baseline (§9.1); ported |
| Revision custody experiment | `75736dddfa3fe1ae89c5089720c702f4ca595ab5` | `workspace/research/experiments/simulation-analytics-ownership-revision/…/AdmissionBoundaryDiscriminatorExperiment.java` | No longer compiles at baseline (§9.1); ported |
| Game diagnostic report, revision 3 | `4ad00136441fa29e6b462ac5431b60cf2de1e148` | `workspace/research/investigations/factory-design-game-diagnostic-evidence-report.md` (branch `workspace/factory-design-game-diagnostic-evidence`) | Concrete demand, research evidence only (§12) |
| Game diagnostic review, **ACCEPT WITH QUALIFICATIONS** | `6484795ff4c4ad971e06c70751b03805e54a55f5` | `workspace/research/investigations/factory-design-game-diagnostic-evidence-adversarial-review.md` | Qualifications 1–8 bind the Game evidence |
| Game research-local definitions | `4ad00136441fa29e6b462ac5431b60cf2de1e148` | `workspace/research/experiments/game-diagnostic-evidence/…/BakeryInspectionPack.java` | `utilization-over-a-period`, `flow-characterization-of-idle-slots` (read; does not compile at baseline, §9.1) |
| Execution-account report / first review | `a53778145f3ee9941a4b46c7f04f1ec17e6bff86` / `57d87fc4c8ba19927bf50efa4097321208811d5a` | branch `workspace/simulation-execution-account` (retired; commits still fetchable by SHA) | Provenance only; live canonical contracts own the semantics |
| Engine-adaptation report / review | `9551c0cee42bfdeab07f4a55f931eb10ec5fba4e` / `e3a4f9a223b982b341a71c2acf7b74146620e9f2` | same | Supplementary; source of the Game control checkpoints T1–T3 (§12.4). Searched for T1–T3, not read in full |
| **This revision's pre-registration** | `07ec4020e39274e49f690db302376d20c9f0e06b` | `workspace/research/investigations/simulation-analytics-ownership-phase-2-post-execution-evidence-preregistration.md` | Expected values and refusals, committed before any new probe source existed |
| **This revision's probes** | `ac0ac2aa603f82426d7d2c0ec88f3485b9927908` | `workspace/research/experiments/simulation-analytics-ownership-post-execution-evidence/com/arcogine/research/experiment/` | `PostExecutionEvidenceDiscriminatorExperiment`, plus the two `…PostRemovalRerun` ports |

No earlier report, review, pre-registration or experiment was modified. The workspace history was
appended to, never rewritten.

## 2. Baseline, grounding and drift

### 2.1 Grounding performed at `a7f9856`

Read in full at the baseline: `AGENTS.md`; `.github/agents/researcher.agent.md`;
`docs/development/researching.md` (including the now-normative implementability-versus-suitability
rule); `docs/development/testing.md` §10; the register; the analytics consumer-boundary,
evidence/provenance, execution-account and Game diagnostic-evidence briefs; the captured-evidence
decision record (`docs/history/decisions/2026-10-09-captured-simulation-evidence.md`);
`runtime-contract.md` (all of it); `engine-semantics.md` §1–§1.2, §4, §9, §10–§10.2; overview
Domain authority, headless-closure paragraph and Determinism Contract; Factory Design §10;
Governance evidence §5–§6 and §11–§13; Charter §6; the Game consumer plan §4–§6 and the
vertical-slice gate.

Read at source: `FactoryRuntime`, `FactoryHandler` (aggregates, dispatch, completion),
`ResourceObservation`, `JobObservation`, `RuntimePerformanceObservation`, `RuntimeObservation`,
`RuntimeObservationMetadata`, `RuntimeEventType`/`RuntimeEventPayload`, `MachineView`, `SimTime`,
`SimError`; `BoundedAdvancementConformanceTest`; the whole research-experiments main source set
relevant to evidence windows and occupancy; `ResearchPackageBoundaryTest`.

Quick search over `docs/` and `product/` for analytics owner, utilization, starvation/idle-flow,
occupancy, KPI, evidence completeness, capture/retention, basis/frontier, `busyTicks`, R-remove,
Game diagnostics and analytics provenance. Within that scope, at the baseline:

- no production producer computes utilization, occupancy, busy time, starvation or a bottleneck
  verdict; Engine semantics §10 and the runtime contract now say so normatively;
- no implemented consumer evaluates a utilization verification objective; Governance analytical
  evidence remains fixture-only (`governance-evidence.md` §12);
- the only executable utilization-like derivations are the research substrate's oracles, the Game
  workspace's definitions and this workspace's probes;
- the only `busyTicks` mentions are historical notes in research briefs.

This is a bounded absence claim, not proof that no future requirement exists. No outward adapter,
verification-objective consumer, executable transfer consumer or live Game loop exists to inspect;
these are capability absences, not skipped surfaces.

### 2.2 Drift since the unreviewed revision's baseline `306205c3`

| Change | Commit | Material? | Effect on this question |
|---|---|---|---|
| #461 admit execution-account investigation | `79b3399` | No (superseded by #466) | — |
| #462 guarded-advancement note in engine-evolution | `912b2ac` | Minor | Confirms that neither `advanceUntil` nor a driver loop moves time to the requested bound or closes an interval (§8) |
| #463 `combinedQueueDepth` exactness beyond 32 bits | `bff5bf4` | Minor | Closes Phase 1's noted fixture gap for the one result-affecting derived quantity (§11) |
| #464 implementability versus architectural suitability | `b9902d4` | **Yes** | Normative method: a consumer being able to compute a number is not evidence that it is the right guarantor (§10) |
| #465 no inert Factory scheduler markers | `4c4bcdb` | **Yes** | Every processed scheduled event is a step completion published at its own time; after every session call command time equals supported observed time under this scheduling rule (Engine §1.2, §4 rule 3). A definition change for budget-stopped scripts. Discriminator 7 (§8, P7) |
| #466 reconcile captured execution evidence | `a80cd6b` | **Yes** | Canonical basis/coverage/frontier, half-open finality, controller closure, and *"definition ownership does not imply physical custody"*. Falsifies the revision's Rule 7 as a universal rule (§7) |
| #469 remove `busyTicks` | `a7f9856` | **Yes** | Landed by owner decision. Removes the field, its accumulation, its §10.1/§10.2 rules, the "utilization facts" wording and both halves of the overview bottleneck claim. Engine §10 now states the Engine produces no utilization, busy-time, occupancy or bottleneck measure. Breaks compilation of both earlier custody experiments and the Game experiment (§9.1) |

Open PRs at the recheck: #467 and #468 (Dependabot, unrelated). No open branch or PR touches this
boundary.

The handoff stated that the `busyTicks` removal "was **not landed**". That was true at its
preparation baseline and is false at this one. This report therefore classifies `busyTicks` as a
landed removal (§11), not as an owner-selected pending change, and it finds no remaining unlanded
owner-directed field change.

### 2.3 Final recheck

Live `main` was re-resolved immediately before this report's commit: `a7f9856617d99e796d528a44aa8c15677c485f55`,
unchanged from the baseline. Every handed-off SHA in §1 resolved as a commit object at that time.

## 3. Question, decision, scope and risk

**Question (handoff).**

> Given the already-reconciled consumer-neutral execution-evidence contract and the corrected Engine
> session/marker semantics, which parts of the earlier staged, kind-specific analytics-ownership
> recommendation still survive? What must change about evidence custody, measurement sufficiency,
> current-field classification and Game-derived demand? Does any reusable consumer-neutral analytics
> responsibility become justified **now**, and what minimum Engine/definition/consumer boundary
> should be recommended for later independent review and durable reconciliation?

It is the brief's Phase 2 question, narrowed by three landed authorities.

**Decision at stake.**

1. Whether a consumer-neutral analytics owner exists now.
2. Where the Game's two concrete measurements belong.
3. Which party must satisfy which evidence obligation for each kind of claim.
4. Whether any Engine/runtime change follows from the ownership result.
5. Which downstream questions become promotable, and on what exact trigger.

**Non-goals.** Final utilization or flow formulas; an analytics API, module or evidence format;
reopening execution-evidence rules E1–E8, R-remove or closure-API design (no new falsifier was
found); Game implementation; architecture, planning or register edits; independent review.

**Risk:** High. It required live re-grounding, a full candidate re-evaluation including a real
Engine alternative, pre-registered discriminating probes, reruns of the affected prior evidence and
accepted conformance cases, and a mandatory independent review before promotion.

## 4. Executive conclusion

**Research recommendation: `RECOMMEND STAGED/NO SHARED OWNER NOW`.** Confidence: medium-high.

1. **No consumer-neutral analytics owner is justified now.** The Game remains the single concrete
   consumer. Factory Design's "Maximum utilization ≤ threshold" is an unevaluated illustration.
   Governance analytical evidence is fixture-only. No second requirement names the same definition.
   The staged, kind-specific rule (D refined by E) survives, with corrections.
2. **The revision's Rule 7 ("evidence custody follows the definition owner") does not survive.** The
   canonical runtime contract separates the holder (capture, retention, coverage qualification) from
   the analytical-definition owner (sufficiency and computation). Probe P6 shows one holder bundle
   feeding two call sites of one definition with identical results, so custody need not follow the
   definition. Revised Rule 7: **custody follows capture opportunity and the use's horizon;
   sufficiency is the definition owner's.** The Game is the natural holder for its own runs because
   it is their exclusive driver from establishment, not because it owns the definitions.
3. **Sufficiency is claim-specific, and the earlier "fresh observation or complete window" framing
   was too coarse.** At least six evidence classes now separate (§8):
   - one fresh observation (current state and the Engine's declared results);
   - basis plus gap-free coverage plus a producer witness (any interval ending at or before the last
     supported change);
   - controller closure (idle tails, period rates, "no work left for the rest of the period");
   - controller command/fault provenance (fault-free and closure claims);
   - capture from establishment (pre-join history);
   - the full ordered script (reproduction).

   Probes P1–P8 confirm each separation on current code, with every value as pre-registered.
4. **Current fields.** `busyTicks` is **removed**, landed (#469). The Engine-declared `backlog`,
   `completedOrders`, `completedSalesValue`, `averageLeadTime` and `throughputPerTick` stay
   Engine-owned with exact arithmetic. `averageLeadTime`, `throughputPerTick` and caller-windowed
   `FactoryRuntime.throughput(long)` remain a finite compatibility exception. A new qualification
   from P7: `throughputPerTick` is a rate over the time of the last supported change, not a period
   rate. After an idle tail to 100 it reads 0.2 where the period rate is 0.01.
5. **The Engine alternative was compared seriously and is not admitted now.**
   - **A** (Engine-defined standardized utilization) would fix a contested denominator. Without a
     clock witness it still could not certify a period ending in an idle tail.
   - **A′** (an Engine execution-fixed cumulative occupied-slot-ticks aggregate, counting running
     work) is the strongest Engine form. It would substitute for *retention*: late joiners could
     difference observations. It would not substitute for *finality*. It stays a triggered option
     for a concrete late-join or non-driving consumer, weighed against shared/retained delivery.
6. **No Engine, runtime-contract or Determinism Contract change is justified by the ownership
   result.** The landed #465/#466/#469 state already matches the recommended boundary.
7. **Downstream.**
   - **Evidence/provenance stays `CANDIDATE`.** Its trigger is restated in custody terms: a result
     relied on by a party that did not drive or hold its evidence (handover, passive verifier,
     Governance use), retention beyond the holder's horizon, or reproduction across sessions. Merely
     sharing a definition with a co-located holder is not a trigger.
   - Adapter compatibility and cross-revision comparability are unchanged; one new concrete
     cross-revision risk is noted (§13).
   - The Game's two measurements are classified as game-owned named definitions under a
     promotion-ready discipline expressed in canonical coverage vocabulary. This is classification,
     not product admission.

## 5. Prior challenges: what binds, and how each is disposed

The REOPEN review challenged the **original** report (`6a5c0435`). The revision (`75736ddd`) answered
those challenges but was never independently checked. Its answers are therefore treated here as
**claims to re-test**, not as closed findings.

| REOPEN challenge (review §) | Revision's answer at `75736ddd` | Disposition in this revision |
|---|---|---|
| A/C: staged-admission and standardized-Engine-measurement candidates omitted; sentinel and attribution overclaims | Candidates A, A′, D added; Rule 2 trigger replaces the ban | **Upheld and re-tested.** A, A′, B–E and two custody variants are re-evaluated against current guarantees (§10). New result: A′ substitutes for retention only, not finality. Sentinel and historical-attribution corrections carried |
| B: facts-only falsification overstated | Narrowed to unchanged-input scope | **Carried; partly superseded by #469.** The owner has since chosen facts-only for the busy counter. Port C now shows the two pinned runs' fresh observations entirely equal, with only captured events telling them apart — the loss the removal knowingly accepted. `completedSalesValue` B/C′ results reproduce unchanged |
| D/J: shared owner now, downstream promotion | Shared owner staged; evidence/provenance not promoted | **Upheld.** Re-tested against Charter §6's single-core-requirement route; still no concrete requirement (§12.3). Downstream trigger restated in custody terms (§13) |
| E: R1(c) attribution wording | Attribution distinction | **Carried unchanged.** |
| F: retained lead time and throughput | Finite compatibility exception | **Carried,** with the observation-clock qualification from P7 (§11) |
| G: no second engine; comparison authority | Interpret/recreate table | **Carried and extended.** Three more forbidden inferences, each falsified executably: step start from cascade rules (P3), idle tail from `QUIESCENT` (P1, P4.3), fault-free from complete changes (P8) |
| H: Q1–Q6 closure | Updated for the owner decision | **Re-done against landed state** (§11.2): Q1 resolved by #469; Q3 removal landed |
| I: truthful change categories | Separate accounting | **Re-done.** The revision's pending items (`busyTicks` removal, Engine §10 bottleneck sentences, overview bottleneck claim) are now landed; no outstanding current-semantic change remains (§13) |
| Retained qualifications: fresh-observation scope, finite arithmetic, utilization ambiguity, public operations, spatial rechecks, non-causal comparison | Carried | **Carried.** The finite-arithmetic qualification about a saturated `busyTicks` is moot after removal; any future A′ needs its own overflow rule |

## 6. Authority separation

The runtime contract fixes the roles; they may share one component without becoming one authority.

| Role | Owns | How a statement is demonstrated | Trust and provenance boundary | Must not be assumed to own |
|---|---|---|---|---|
| **Engine / Factory producer** | Authoritative run, state and ordered supported changes; run identity and model binding; observation coherence at one cursor; result-affecting interpretation (`combinedQueueDepth`); declared derived results with exact arithmetic (§10.1–§10.2) | Supported observation at `(S,T)`; supported change at `(RunId, sequence)`; conformance fixtures | Producer-verifiable; carries `RunId` and `ModelFingerprint`; no Engine-definition identifier | Retention; any utilization/occupancy/bottleneck meaning (Engine §10); future-input exclusion; what a holder captured |
| **Session controller (exclusive driver)** | Command admission and issuance; advancement calls, targets and budgets; `CommandResult` outcomes and faults; any no-future-input commitment | Its own call/outcome log; a closure statement bound to a post-advancement `(RunId,S*,T*)` | **Attributable assertion, not producer-verifiable.** P8: a fault-hiding closure yields 95 and nothing in the supported stream contradicts it | Definition meaning; other parties' capture |
| **Evidence holder / capture delegate** | Retained basis, changes and observations; gap detection against an independently known cursor; retention horizon; handover packaging | Sequence continuity from basis through a proof sequence, checked against a frontier it did not invent (P1, P2) | Its warranty covers what it holds; missing ranges stated against an authoritative cursor; a stripped bundle cannot inherit a closure (P6) | Definition choice; controller trust it did not receive |
| **Analytical-definition owner** | Name, method, basis and denominator, declared inputs, **accepted proof classes**, controller-provenance requirement, refusal rules, computation, interpretation limits, result provenance | Definition text plus fixtures; the same definition gives the same result over the same bundle at any call site (P6) | Result provenance names the definition, proof class and any attestor and scope it relied on | Physical capture or retention; the runtime |
| **Consumer / presenter** | Use, wording, period selection, teaching | Presentation over a named result | Must not present a result as an Engine fact | The definition, unless it is also its owner |
| **Governance verifier** | Evidence use, applicability/reliance, conformance outcome, findings | Attributable evaluation over attributed results | Consumes producer and definition provenance; does not become a calculation engine (Governance §11) | Capture, scheduling, calculation semantics |

**Inference.** Three implementation arrangements are all legitimate. A static Game generator is
driver, holder and definition owner at once. A shared definition can consume a Game holder's bundle
while the Game stays driver and holder. A passive verifier can hold nothing and receive a handed-over
bundle. Ownership must be argued per role.

## 7. Rules 1–8: old rule → current contract → revised rule

| # | Old rule at `75736ddd` | Current contract at `a7f9856` | Revised rule (proposed, not adopted) | Change |
|---|---|---|---|---|
| 1 | Engine owns execution truth "**and the evidence to measure it**" | Engine owns state, change, interpretation and supported projections; "Arcogine is not event sourced"; delivery drains; retention belongs to the holder | Engine owns authoritative state and change, result-affecting interpretation, the supported observations, events and cursor needed to *obtain* evidence, and the exact arithmetic of declared results while declared. It holds no history and declares no utilization, occupancy, busy-time or bottleneck measure | **Narrowed**: "evidence" here means supported facts, not custody |
| 2 | Engine measurement only by requirement: (a) participates in execution; (b) execution-fixed aggregate needed in the fresh observation; (c) a contractual Engine-wide definition | Engine §10 forbids none prospectively but declares none now; #469 removed the one aggregate of kind (b) | (a) and (c) unchanged. (b) is restated: an execution-fixed run-to-date aggregate is admissible when a concrete use's holder *cannot capture from establishment* (late join, non-driving party), and it is weighed against retained/shared delivery, the runtime contract's own reopening route. Such an aggregate replaces retention, never finality | **Refined** (§10, A′) |
| 3 | A value whose definition needs a choice execution does not fix belongs to whoever fixes it; default consumer-local | "Utilization, occupancy … require an explicit named analytical method"; ownership unassigned | Unchanged as a **definition-ownership** rule only. It says nothing about who captures | **Scoped** |
| 4 | Shared ownership by trigger: (a) two consumers, same definition; (b) one consumer plus a core verification/governance requirement; (c) outward contract; (d) divergent duplicates | Charter §6: one concrete core requirement suffices with acceptance evidence | Unchanged. Added: a shared *holder* or delivery arrangement is not a trigger for a shared *definition* owner, nor the reverse | **Clarified** |
| 5 | Every non-Engine definition is promotion-ready: named, basis-explicit, inputs declared, "completeness condition", refusal; "promotion transfers custody of an unchanged definition" | Coverage is per claim: basis, gap-free through the proof sequence, finality class; closure has attestor, scope and trust; complete changes do not imply complete commands or fault-free success | Additionally state, in canonical vocabulary: claim interval convention; basis requirement; accepted proof classes (producer witness / producer observation / controller closure); whether clean command/fault outcomes are required; any no-future-input scope; and the result provenance to carry (definition identity, proof class, attestor and scope). **Promotion transfers definition custodianship only.** Evidence custody stays wherever capture happens | **Rewritten** |
| 6 | Measure outcomes; never re-decide execution | Same (brief invariant) | Unchanged. Added: never infer history or finality from Engine rules — step start from the cascade (P3), idle tail from `QUIESCENT` (P1.6, P4.3), fault-free success from complete changes (P8) | **Extended** |
| 7 | **Evidence custody follows the definition owner** | **"The evidence holder owns capture, retention and coverage qualification … definition ownership does not imply physical custody"** | **Custody follows capture opportunity and the use's horizon; sufficiency follows the definition.** The holder — normally the exclusive driver or its delegate — captures what its uses need. A definition owner obtains evidence from a driver, a holder or a future accepted delivery contract. Engine retention is a separate runtime-contract change on its own triggers | **Replaced** |
| 8 | Placement and factoring never transfer ownership | Same | Unchanged, extended to custody placement: holding the evidence confers no definition ownership | **Extended** |

### 7.1 Why Rule 7 fails, and what that does to Rules 3–5

**Falsification.** The canonical sentence contradicts Rule 7 as a universal rule. P6 then
demonstrates the counter-arrangement on current code: one driver-held bundle feeds two call sites
(`gameLocal`, `sharedCandidate`) of one stateless definition, with equal outcomes — (16,32) for
`[0,16)` and (16,40) for `[0,20)` — and identical proof provenance. A demonstrated legitimate
arrangement suffices to falsify "must follow".

**Can consumer-local stay the default without making the Game a second Engine or the universal
evidence owner?** Yes. Two separate justifications coincide for the static Game: it fixes the
definitions' choices (Rule 3), and it is the exclusive driver from establishment, so it can capture
(revised Rule 7). Neither makes it the owner of anyone else's evidence. Nor does capturing make it a
second Engine: its folds read supported placement facts and published routing only (P4).

**Does meaning survive promotion when definition custodianship changes but the holder stays put?**
Yes, under four portability preconditions:

1. the definition's declared inputs are exactly the bundle's contents;
2. basis and claim conventions are stated;
3. the accepted proof classes are stated;
4. the result carries its proof provenance and does not widen trust. In P6 the shared call site's
   `[0,20)` result still rests on "controller closure by driver (closed script)". Strip the closure
   and the same definition refuses `[0,20)` while still giving `[0,16)`.

**Do the claimed "one-way/no-rework" and future-admission benefits still follow?**

- **Partly.** Promotion no longer implies moving capture, so it is *cheaper* than the revision
  assumed: a shared definition can consume existing holders' bundles.
- **But one rework risk was hidden by the conflation.** The Game's period measures rely on
  controller-attested evidence for idle stretches. Its recorded generator samples each tick after
  an unbounded `advanceUntil(t)` in a closed script (`BakeryInspectionPack` at `4ad0013`), and
  ticks past the last supported change have no producer witness. When a non-driving party consumes
  such a result, the attestation must travel with it (runtime contract: handover preserves the
  basis, changes, proof frontier and attestation).
- **Consequence.** The anti-rework claim holds only if proof provenance is carried from day one
  (revised Rule 5). The evidence/provenance trigger is restated accordingly (§13).

## 8. Claim-specific temporal completeness and sufficiency

Notation as in the runtime contract: basis `(S0,T0)`, claim `[a,b)`, proof sequence, finality class.
"Provenance" is the controller's command/fault and closure provenance, kept separate from supported
state-change coverage. Every entry marked P*n* was executed on current code at the baseline (§9).

| Claim kind | Basis | Coverage | Finality | Controller command/fault provenance | Fresh observation enough? | Evidence |
|---|---|---|---|---|---|---|
| Current slot state, queue, pending work; declared `backlog`, `completedOrders`, `completedSalesValue`, `averageLeadTime`, `throughputPerTick` | The observation itself | None | None (current state) | Not needed for the value; needed to claim a fault-free run | **Yes** | Port A (23 observations, 14 fixtures), port B, P6 slot state |
| Occupancy / per-slot utilization over `[a,b)` with `b` ≤ last supported change time | `T0 ≤ a` | Gap-free `S0+1..K-1` | Producer witness `K` (first held change at time ≥ `b`), or observation at time ≥ `b` with gap-free coverage to its cursor | Not needed | **No** | P1.7, P2.1, P2.3, P4.1 `[0,16)`, P5 |
| Same with `b` in an idle tail | `T0 ≤ a` | Gap-free through `S*` | **Controller closure**: demonstrated exhaustion through `≥ b`, input excluded through `≥ b`, bound to `(RunId,S*,T*)` | **Required**: clean calls; a spent budget (P1.9), a lower target (P1.10) or a fault (P8) invalidates it | **No** | P1.1, P4.1 `[0,20)`, P5 `[6,12)` |
| Stale-frontier holder | — | Gaps against the bound cursor | Must refuse | — | — | **P1: 5 truthful; naive fold 100; definition refuses** |
| Earlier interval after a later gap | `T0 ≤ a` | Through `K-1` only | Witness at `b` | — | — | **P2: `[0,5)` = 5 survives; `[0,10)` refuses (6..7 missing against cursor 7)** |
| Event exactly at `b` | — | — | Witness is the first change at time ≥ `b`; changes at `b` contribute nothing | — | — | P2.4 (dispatch at 5 not needed for `[0,5)`); P5 completion at 10 excluded from `[0,10)` |
| Same-time ordering | — | Sequence orders state at equal times | — | — | — | P4.1: offline (1) then online (7) at time 0 yields online; zero offline ticks |
| Late join, bounded from the basis | `T0` = join time | Gap-free after `S0` | Witness | — | — | **P3: `[5,10)` = 5 from a joined basis** |
| Late join, pre-join history | — | — | — | — | — | **P3: `[0,10)` refuses**; observation records carry no step start time and no processing total (component check) |
| Cross-boundary running work | `T0 ≤ a` with the step active | Through `K-1` | Witness after the interval (completion at 10 proves `[0,6)`) or closure | Closure route only | No | **P5: running occupancy `[0,6)` = 6 by witness and by closure; completion-credited 0** |
| Period rate (completed orders per period tick) | `T0 ≤ a` | As occupancy | As occupancy; idle tail needs closure | Closure route | No — `throughputPerTick` is a different quantity | **P7: (1,100) with closure, refused without; `throughputPerTick` 0.2** |
| Flow characterization over `[a,b)` | `T0 ≤ a` | As occupancy | As occupancy | Closure route | No | P4.1, P4.2 |
| "No work left for the rest of the period" (known-work horizon) | `T0 ≤ a` | As occupancy | **Closure with input exclusion through `b`**; `QUIESCENT` is not enough | **Required** | No | **P4.3: closed run 8 no-work-left with closure, refused without; open run 8 starved (4 capped); identical prefixes 1..6 and identical observations at 5** |
| Fault-free completion | — | Complete changes are not enough | — | **Required**: complete changes 1..4 show no fault; `RuntimeEventType` has no fault value | No | **P8** |
| Reproduction or comparison | Model, producing definition, ordered script including advancement interleavings and budgets | — | — | **Required** (script) | No | Runtime contract; #465 changed outcomes for budget-stopped scripts (§13) |

**Answers to the handoff's sufficiency questions.**

- **Does a "fresh observation" suffice?** For current-state claims and the Engine's declared results,
  yes. For any interval claim, no: an observation is at most a frontier, and its time is the last
  supported change, not a requested target (P5: after `advanceUntil(6)`, time 0).
- **Which claims need what?**
  - Command-outcome completeness and fault provenance: only closure-based, fault-free and
    reproduction claims.
  - A no-future-input attestation: idle-tail and known-work claims.
  - Supported state-change completeness to a producer frontier: everything else in the interval
    class.
- **Discriminator 7 (command/clock).** Under the current scheduling rule, commands apply at the
  observed time: the second submission is accepted at 5, not at the target 100 (P7). Hence:
  - a requested target is neither a clock advance nor a finality witness;
  - a Game period ending at a displayed tick in an idle tail needs a closure;
  - a live loop that wants input at a selected idle time needs the separate T2 decision (§12.4).

## 9. Executable evidence

**Environment:** container JDK OpenJDK 21.0.12.1; repository Gradle wrapper 9.8.0; a detached
worktree of `a7f9856`. Experiments ran through the documented `-PresearchExperimentSources`
mechanism (`docs/development/testing.md` §10). Dependency resolution first hit transient HTTP 429s
from Maven Central; once the cache was filled, the probe and port runs used `--offline` against those resolved artifacts, and the conformance run resolved online.

### 9.1 Prior evidence at the baseline

| Run | Result |
|---|---|
| Tracked `:research-experiments:test` at `a7f9856` | **137 tests, 0 failures, 0 errors, 0 skipped** (12 classes) |
| Original custody experiment (`6a5c0435`) | **Does not compile**: `PerformanceEvidenceSufficiencyExperiment.java:317, :319, :326` — the `ResourceObservation` constructor and `busyTicks()` reads |
| Revision custody experiment (`75736ddd`) | **Does not compile**: `AdmissionBoundaryDiscriminatorExperiment.java:96, :97, :104` — `busyTicks()` |
| Game experiment (`4ad0013`) | **Does not compile**: `GameContracts.java:228, :235`; `GameDiagnosticEvidenceExperiment.java:100, :102` — `busyTicks` |

These failures are themselves drift evidence: every one is a read of the removed field. None of the
three is edited.

### 9.2 Ports and new probes (commit `ac0ac2a`)

- **Ports.** Each port removes only the counter reads; every other assertion and expected value is
  as originally registered. Restated cases are pre-registered at `07ec402`.
- **New probes.** All expected values were pre-registered at `07ec402` before any probe source
  existed.
- **Runs.** First run: 21 tests including `ResearchPackageBoundaryTest`. Second run: the whole
  tracked suite plus all custody classes, with `--rerun-tasks`.

| Class / probe | Pre-registered | Observed |
|---|---|---|
| Port A | 23 observations, 14 fixtures | 23 (9 with incomplete orders), 14 — **match** |
| Port B | 40.0 vs 80.0; late join 20.0 vs 40.0 | **match** |
| Port C (restated) | Closing observations entirely equal; event occupancy `{A1:5,A2:0}` vs `{A1:0,A2:5}` | **match** |
| Port C′ (restated) | `completedSalesValue` reconstructs on every complete-window fixture | 14 fixtures — **match** |
| Ports D1, D3 | 34; `{CUT:3}`/`{ASSEMBLE:3}`; waits 48 vs 6+42; −16, +5, −5 | **match** |
| Admission ports 1–3 | Oven profile; 20/20, 21/22, 24/34, Busy 14/17, Mixer 12/17, `[0,9)` 12; 16/12/4 and oracle refusal; 8/4/1 | **match** |
| P1 | 5; refusals for gap, unbound and no closure; naive 100; `[0,5)` 5; spent budget and target 50 refuse; `[0,50)` 5 | **match** |
| P2 | 5; refuse; 10; 5; refuse / 5 | **match** |
| P3 | Component lists; `[5,10)` 5; `[0,10)` refuses | **match** |
| P4 | (16,32); refuse; (16,40); Flow(16,12,0,0,4) and (16,12,0,0,12); oracle refuses; Packer (1,0,8,4,1); `[5,9)` (0,0,0,0,8) / refuse / (0,0,8,4,0); prefix and observation equality | **match** |
| P5 | 6 by witness and by closure; credited 0 and 0; 10; 4; refuse / 4 | **match** |
| P6 | Equal outcomes at both call sites; trust named; stripped and observation-only refusals; slot state (0,2) | **match** |
| P7 | (4,5), 0.2, 5.0; (1,100) with closure, refuse without; accepted at 5; (8,10), 5.0, 0.2 | **match** |
| P8 | Changes 1..4; six event types; (4,5), Holder active, `QUIESCENT`; honest closure refuses; hiding closure 95; Cutter 5, Holder 0 | **match** |

Totals:

- **First run:** 21 tests, 0 failures, 0 errors, 0 skipped — `PostExecutionEvidenceDiscriminatorExperiment`
  8, `PerformanceEvidenceSufficiencyPostRemovalRerun` 6, `AdmissionBoundaryDiscriminatorPostRemovalRerun`
  3, `ResearchPackageBoundaryTest` 4.
- **Full run:** 15 classes, **154 tests, 0 failures, 0 errors, 0 skipped** (137 tracked, 17 custody).
  The boundary test therefore also held over the custody classes.

No assertion or expected value was changed after a run. The only edit between the first attempt and
the passing run fixed one compile error: the probe named the `CommandResult` type parameterized by an
internal scheduler payload, which is not on the research classpath. That result, and
`advanceUntil`'s list, are now held through `CommandResult<?>` and `List<?>`, as `ExperimentRunner`
does.

**Accepted conformance cases** at the baseline (`:factory:test`):

| Test class | Tests | Failures |
|---|---|---|
| `BoundedAdvancementConformanceTest` | 10 | 0 |
| `EngineDerivedResultConformanceTest` | 4 | 0 |
| `HeadlessClosureAcceptanceTest` | 5 | 0 |
| `RuntimeEventDeliveryAcceptanceTest` | 14 | 0 |
| `SessionControlAcceptanceTest` | 13 | 0 |
| **Total** | **46** | **0** |

They pin R-remove command time, pacing invariance and fault publication; the derived-result
arithmetic without a busy counter; and raw-fact headless closure without a bottleneck verdict.

**Limits.**

- These are constructive cases: linear routing, one product, release at once or one added
  submission, a single availability toggle, one overflow fault.
- They show that the separations exist on current code and that each refusal is reachable. They do
  not select any definition.
- The research-local proof function encodes the runtime contract's rules. Its observation-frontier
  branch requires coverage through the observation's whole cursor, which is conservative (§15).
- Everything said about A, A′ and clock witnesses is **specification-derived**, not executed: those
  mechanisms do not exist.

## 10. Candidate set, re-evaluated

Under the now-normative rule, a feasible consumer computation is not evidence that the consumer is
the right guarantor, and a possible Engine measurement is not a requirement. Each candidate is
judged on which invariant it can guarantee, its interface and support burden, duplication across
real consumers, room for common methods, and definition-change compatibility.

| | Shape |
|---|---|
| **A** | Engine-defined standardized measurement (for example normalized per-slot utilization) in the supported observation, when a contract needs one Engine-wide meaning |
| **A′** | Engine execution-fixed aggregate only: cumulative occupied slot-ticks to the observation time, including running work, and cumulative online slot-ticks. Periods, normalization, grouping and refusal stay outside |
| **B** | Universal outside-Engine rule: no new Engine measurement is ever admitted |
| **C** | Shared consumer-neutral owner now (a semantic role, not necessarily a module), on a single concrete core requirement if one exists |
| **D** | Staged, concrete-demand admission with explicit triggers |
| **E** | Kind-specific hybrid: facts and execution-fixed aggregates → Engine; one consumer's measurement or characterization → consumer; reusable named measurement → shared on trigger; inference and comparison → named methods |
| **F** (added) | Custody-led ownership: whoever holds the evidence owns the definitions it feeds — the inverse of the revision's conflation |
| **G** (added) | Decoupled custody: one capture holder supplies bundles to independent stateless definitions |

### 10.1 Candidate × proving case

"Viable" means the case is served truthfully; "cost" names the price; "fails" means a case
contradicts the candidate.

| Case | A | A′ | B | C | D | E | F | G |
|---|---|---|---|---|---|---|---|---|
| Game utilization over a selected period (P4) | **Cost**: must pick per-slot vs `Busy` share, half-open vs inclusive, pool grouping, offline treatment (ports 1–2: values and rankings differ) or expose a caller-parameterized query; an idle-tail period still needs a controller statement or clock witness | Viable for retention; finality still outside (spec-derived) | Viable | Viable, single consumer | Viable: game-named definition | Viable | **Fails**: a holder can feed two owners (P6) | Viable; arrangement only |
| Idle-flow characterization, known-work horizon (P4.2–P4.3) | **Cost**: Engine classifies its own idle slots with routing lookahead, cap and pool choices; "no work left" needs future-input knowledge the Engine does not have | Not applicable (a state classification, not an aggregate) | Viable | Viable | Viable | Viable | Fails as F | Viable |
| Stale frontier / idle tail (P1) | Same finality need: an Engine figure at the observation time is either a last-change-time rate (P7) or needs a closure input | Same: differencing ends at observation times | Viable | Viable | Viable | Viable | — | Viable: refusal is definition-owned |
| Late-join run-to-date occupancy (P3) | Viable at cost | **Viable — its natural home**; removes the capture need | **Fails at the current contract** for a concrete requirement; offers only retained delivery | Viable with a capture/delivery contract | Viable via Rule 2(b) trigger | Viable | — | Needs a holder from establishment |
| Cross-boundary running work (P5) | Viable if defined | Viable (includes running work, unlike the removed counter) | Viable | Viable | Viable | Viable | — | Viable |
| Custody separation / promotion (P6) | n/a | n/a | n/a | Viable | Viable | Viable | **Fails** | **Demonstrated** |
| Command/clock period rate (P7) | `throughputPerTick` is the counterexample: an Engine rate over the observation clock is not the Game's period rate | n/a | Viable | Viable | Viable | Viable | — | Viable |
| Fault provenance (P8) | Engine cannot certify absence of a fault it did not publish as a change | n/a | Requires controller provenance in every candidate | Same | Same | Same | Same | Same |
| A measurement that starts to drive dispatch (future) | Viable | Viable | Viable (result-affecting stays Engine) | Must move | Must move (Rule 2(a)) | Viable | — | — |

**Counterexamples to the simplistic positions.**

- **Against "Engine-rich is cheap and complete".** P7: the Engine's own declared `throughputPerTick`
  reads 0.2 after an idle tail where the period rate is 0.01. An Engine figure computed at the
  observation boundary cannot certify an idle-tail period without a controller statement or a
  clock-advance witness. That witness is itself an unadmitted Engine change (T2), which would alter
  command timing.
- **Against "always consumer-local".** P3: a concrete late-joining consumer that must report
  occupancy since run start cannot do so from current supported evidence. A truthful consumer-local
  answer is refusal. Only A′, retained/shared delivery or capture from establishment can serve it. A
  universal outside-Engine ban (B) would forbid the cheapest of those without comparison.

### 10.2 Verdicts

| Candidate | Verdict | Why |
|---|---|---|
| **A** | **Viable but not currently admitted** | Not falsified. No requirement demands one Engine-wide utilization. It would suppress definitions that legitimately differ (ports 1–2). It would not remove the finality need for idle-tail periods without a further Engine clock change. Current Engine §10 declares no such measure |
| **A′** | **Viable with a concrete compatibility/custody cost; not currently admitted** | The strongest Engine form. Execution fixes its meaning (two evidence routes agree in port 1), and it would substitute for **retention**, not finality (spec-derived). Cost: a new accumulator in the §10.2 register with its own overflow rule, a new observation field and conformance; it reverses the direction of the just-landed owner removal, even though the new aggregate would be truthful. No present consumer needs it — the Game captures from establishment |
| **B** | **Falsified as a universal rule**; survives as the default | Falsified by result-affecting measures (Rule 2(a)) and the late-join case. Its current successes are the default |
| **C** | **Viable but not currently admitted** | Not falsified. Charter §6 would permit a single concrete core requirement, but none exists: Factory Design §10.2 is illustrative and Governance analytical evidence is fixture-only. An owner created now would have to choose among definitions the second requirement has not specified |
| **D** | **Selected** as the admission mechanism, with revised triggers | — |
| **E** | **Selected** as the classification | — |
| **F** | **Falsified** | Canonical sentence and P6 |
| **G** | **Selected as the custody arrangement compatible with D/E; changes implementation custody, not owner placement** | Already the research substrate's shape (`ExperimentRunner` → `Oracle` over `DeclaredEvidence`). A Game holder can feed a later shared definition without moving custody |

The surviving package is **D refined by E, with custody arranged as in G**, plus revised Rules 1–8.

## 11. Current state

### 11.1 Field matrix

Three states are kept separate:

1. landed at `a7f9856`;
2. owner-directed but unlanded — **none remains**;
3. prospective reclassification — subject to this question's independent review and reconciliation.

| Item | (1) Landed owner, arithmetic, support, provenance | (2) Unlanded owner change | (3) Prospective under revised rules |
|---|---|---|---|
| `combinedQueueDepth` | Engine; result-affecting ranking key (Engine §2 rule 3); unexposed; exact beyond 32 bits (#463) | None | Rule 1; never presentable as a physical queue |
| Queue, state and work projections (`queueDepth`, `activeJobIds`, `state`, `concurrency`, `pendingWork`, order/job observations) | Engine projections at one cursor. `JobObservation` carries `createdAt`/`completedAt`, no step start time (P3) | None | Rule 1; late-join history needs capture or a triggered A′ |
| `backlog` | Engine; current-state count (§10.2) | None | Retained |
| `completedOrders` | Engine; exact counter (§10.2 rule 2) | None | Retained |
| `completedSalesValue` | Engine; running total in completion order (§10.2 rule 3); not in a fresh observation otherwise (port B) | None | Retained |
| `averageLeadTime` | Engine; saturating accumulator, empty-set 0 (§10.1 rule 3, §10.2 rule 1) | None | **Compatibility exception**, retained with exact arithmetic |
| `throughputPerTick` | Engine; completed ÷ observed time, zero-window 0 (§10.1 rule 2). Observed time is the last supported change, equal to command time after every call (§1.2) | None | **Compatibility exception**. New qualification: not a period rate (P7); a period rate is a named outside definition |
| `FactoryRuntime.throughput(long)` (and `backlog()`, `avgLeadTime()`, `completedSalesValue()`, `completedSales()`) | Public; Engine arithmetic over a caller window. Not in the research boundary test's supported session-control/observation set — a test-scoped judgment, not a normative support statement | None | Method-level support stays open (Q5) |
| `busyTicks` | **Removed** (#469): field, accumulation, `MachineView`/`Machine` accessors, §10.1 rule, §10.2 row, §14 fixture item, "utilization facts" wording | None (landed) | No re-admission; a truthful A′ only on its trigger |
| Observation time and cursor | `currentTime` = time of last supported change; `latestEventSequence` advances in lockstep with emission; a requested target does not move time (P5, P7) | None | Rule 1. Interval claims need a proof class beyond it (§8) |
| Engine bottleneck wording | **Removed** (#469): Engine §10 and the overview say neither resource load nor any occupancy derivation identifies a bottleneck; `HeadlessClosureAcceptanceTest` asserts raw facts only | None | Inference is a named outside method (Rule 3/6) |

### 11.2 Phase 1 Q1–Q6

| Q | Updated answer |
|---|---|
| Q1 utilization wording | **Resolved by #469**: no utilization fact is promised or produced. Prospectively, normalized utilization is not admitted to Engine now (A) — "not now", not "never" |
| Q2 four recomputable fields | Retained; port A reconfirms exact recomputation at the baseline. `throughputPerTick` gains the observation-clock qualification (P7) |
| Q3 history-carrying aggregates | `busyTicks` removal **landed**. Port C shows the accepted loss: fresh observations are now identical across the pinned pair. `completedSalesValue` retained (port B) |
| Q4 observation clock | Closed for the field. Strengthened by R-remove: command time equals observed time after every session call under the current scheduling rule |
| Q5 public accessors | Open; no current change requires a decision |
| Q6 rule tests | Unchanged: `combinedQueueDepth` Engine-owned; step-first waiting and occupancy derivable outside; attribution distinction carried; three forbidden inferences added (Rule 6) |

## 12. The Game's actual requirements

### 12.1 What the Game evidence supports, and its limits

Read as research evidence together with its review:

- **Concrete consumer.** The Game's static recorded prototype is one concrete consumer with two
  forcing questions: utilization over a selected period, and idle-flow/starvation characterization.
- **Definitions are research-local.** `BakeryInspectionPack` holds feasibility definitions, not
  production contracts (review qualification 5; promoting them directly would trigger the review's
  `MORE EVIDENCE REQUIRED` condition).
- **Reuse is anticipated, not demonstrated.** The verification-objective use is illustrative; flow
  reuse rests on unverified background; the counter's removal is not reuse evidence.
- **Corrections to carry.** The idle-with-eligible-work-queued third state, a declared starved-slot
  cap and a pool basis are required (review qualifications 3–4). All three are reproduced here
  (admission ports 2–3; P4.1–P4.2).
- **Status.** Diagnostics `READY`, playable slice `BLOCKED`.

### 12.2 Classification

Both are **game-owned, consumer-local named definitions** (Rule 3), not production semantics yet.

**Utilization over a period.** State:

- the period convention (half-open, as the Game chose);
- the per-slot denominator;
- the availability treatment — offline excluded, a declared choice; the continuously-online substrate
  oracle refuses instead (P4.1);
- work in progress counted to the boundary;
- machine versus pool grouping;
- the accepted proof classes — the producer witness for periods ending at or before the last change,
  and the Game driver's closure for idle tails (P4.1: (16,32) vs (16,40));
- the refusals;
- never a bottleneck verdict.

**Idle-flow characterization.** State:

- the three idle categories, or refusal;
- the starved cap (P4.2: 8 uncapped, 4 capped);
- the pool basis;
- step position from supported placement facts only;
- the known-work horizon as an **explicit controller no-future-input requirement** (P4.3): prefixes
  1..6 and the observations at 5 are identical between a closed and an open run, yet `[5,9)` is 8
  no-work-left in one and 8 starved (4 capped) in the other;
- the always-online scope as a stated scope.

**Custody.** In the static prototype the generator is exclusive driver and holder, by capture
opportunity (revised Rule 7). Its per-tick samples after an unbounded `advanceUntil(t)` in a closed
script are controller-attested wherever no later captured change witnesses them.

### 12.3 Shared-owner test

| Trigger | Utilization | Flow |
|---|---|---|
| (a) Two concrete consumers, same named definition | Not met | Not met |
| (b) One consumer plus a concrete core verification/governance requirement (Charter §6 route; no shipped-consumer count needed) | **Not met.** Factory Design §10.2 is an illustrative classification. Governance §11 keeps verification semantics domain-owned and §12 says analytical integration is fixture-only. No objective names a basis | Not met |
| (c) Outward contract | Not met — no adapter | Not met |
| (d) Divergent duplicates | Not met | Not met |

Single-consumer status is **not** a permanent prohibition. Each trigger is concrete and observable.

### 12.4 Later live control and the anti-second-Engine rule

The supplementary Engine-adaptation review (`e3a4f9a2`) names checkpoints T1–T3 for a selected
live-loop requirement set.

- **T1, mid-run commands.** The command-time remedy has landed as behavior (#465). Commands apply at
  the observed time; pacing does not change supported outcomes for start-fixed inputs
  (`presentationPacingOfAStartFixedRunChangesNothingSupported`). Analytics consequence: a period
  measure crossing a mid-run command reads the accepted order as a supported change. Rejected or
  no-op requests change no state, but they matter to closure and reproduction claims.
- **T2, input at a selected simulated time while idle.** Undecided (clock witness, scheduled inputs
  or event-boundary interaction). Analytics must not assume a target is a clock advance (P5, P7).
- **T3, non-driving finality.** Undecided. This is where the evidence/provenance question would
  activate (§13).

None of these is grounds to grow analytics or the Engine now. They belong to Game playable-admission
evaluation.

Anti-second-Engine: the folds in P4 read only `ORDER_ACCEPTED`/`JOB_DISPATCHED`/`JOB_WAITING`/
`JOB_STEP_COMPLETED` facts and published routing. Forbidden, and shown to give wrong answers:

- inferring a running step's start time from the one-dispatch-per-trigger cascade (P3: refuse
  instead);
- reading `QUIESCENT` as finality (P1.6, P4.3);
- reading complete changes as fault-free success (P8).

Counterfactuals use Engine re-execution under changed explicit inputs; no definition here simulates
forward.

## 13. Downstream decision

| Question | Decision | Exact condition |
|---|---|---|
| Consumer-neutral analytics owner | **Not now** | Admit on Rule 4 (a)–(d). The most plausible first trigger is an evaluated utilization verification objective (Factory Design §10.2 with Governance §11) that names the same definition, with acceptance evidence |
| Engine / runtime / Determinism Contract change from the ownership result | **None** | A′ only on a concrete late-join or non-driving consumer needing run-to-date occupancy, compared against retained/shared delivery (execution-account reopening triggers). Any clock witness only at T2, through Engine semantics |
| Evidence/provenance question | **Stays `CANDIDATE`** | Promote when an admitted named definition's result must be relied on by a party that did not drive or hold its evidence (handover, passive verifier, Governance evidence use), so proof class, attestor, scope and producing basis must cross a custody boundary; or when retention beyond the holder's horizon, or reproduction across sessions, is required. Sharing a definition while the same holder supplies the evidence is **not** a trigger |
| Adapter compatibility | Stays `CANDIDATE` | Unchanged: a shared analytics contract plus a second supported consumption boundary |
| Cross-revision comparability | Stays `CANDIDATE` | Unchanged trigger. New concrete risk: #465 is a definition change for scripts that stopped a bounded call on a positive budget, and #469 removed a field. Game recorded runs retained across revisions need producing-revision provenance before being compared with fresh runs |
| Game diagnostic question | Remains `READY`, separately owned | Its reconciliation may promote the two game-owned definitions stated per §12.2 in canonical coverage vocabulary, with T1–T3 evaluated before playable admission. This report admits nothing |

## 14. What changed, what survived, what is open (versus `75736ddd`)

| Conclusion | At `75736ddd` | Now | Confidence | Falsifier / reopening |
|---|---|---|---|---|
| No shared owner now; staged D refined by E | Yes | **Survives**, with custody arrangement G | Medium-high | An evaluated core requirement or second consumer naming the same definition |
| Rule 7: custody follows definition owner | Proposed | **Did not survive**; replaced | High (canonical text plus P6) | — |
| Rule 5: promotion-ready discipline | "Completeness condition" | **Rewritten** in proof-class and trust vocabulary; promotion moves definition custodianship only | High for the requirement; medium that it suffices for every future method | A method whose sufficiency cannot be expressed by these proof classes |
| Rule 2 Engine-admission triggers | (a)–(c) | **Survives**; (b) restated in capture-opportunity terms; A′ shown to replace retention, not finality | Medium-high (spec-derived for A′) | A concrete late-join consumer; an owner preference for Engine-provided measures |
| One-way / no-rework promotion | Claimed | **Conditional**: holds only with proof provenance from day one | Medium | A promoted result whose attestation cannot travel |
| Evidence/provenance trigger | Shared definition, or non-capturing party | **Restated**: non-driving/non-holding reliance, retention or reproduction; sharing alone is not a trigger | Medium-high | — |
| `busyTicks` | "Owner decision: remove" (unlanded) | **Removed, landed (#469)** | High (code, docs, compile failures) | — |
| Engine §10 / overview bottleneck wording | To correct | **Corrected, landed (#469)** | High | — |
| Compatibility exception (`averageLeadTime`, `throughputPerTick`, `throughput(long)`) | Yes | **Survives**; observation-clock qualification added (P7) | Medium (owner preference) | A proposal to change formula or sentinel; a consumer misreading it as a period rate |
| Fresh observation versus window | Two classes | **Six classes** (§8) | High for current code | A claim kind not expressible in them |
| Anti-second-Engine boundary | Interpret / recreate | **Extended** with three falsified inferences | High | — |
| Determinism Contract | No change | No change | High | — |

**Still open:** the final Game definitions; whether and how a utilization verification objective
will be specified; where a triggered shared owner would live; method-level support of the
`FactoryRuntime` aggregate accessors (Q5); how a live loop would capture across T1–T3; whether a
truthful A′ would ever be preferred to shared delivery.

## 15. Self-challenge (self-administered only — not an independent review)

| Challenge | Result |
|---|---|
| P6 passes by construction, because both call sites call one function | **Partly upheld.** It cannot show that every shared method keeps meaning. It shows that the arrangement is coherent, that sufficiency and refusal depend on the bundle's contents and trust scope rather than on who calls, and that stripping a closure changes the outcome. That suffices to falsify "must follow" and to state the portability preconditions |
| The observation-frontier branch is too strict: it demands coverage through the whole cursor even when missing changes may lie at or after `b` | **Accepted as conservative.** Missing changes' times are unknown, so a truthful definition cannot assume they lie after `b`. A stricter-than-necessary refusal is truthful, never wrong in value. A reviewer may argue a looser rule; it would not change any ownership verdict |
| P8 uses an artificial overflow | **Rejected as a weakness.** It is the accepted conformance fault (`aFaultingStepCompletionKeepsTheNextCommandAtTheObservedTime`); any post-mutation fault has the same shape (Engine §1.2 `Faulted`) |
| A′ is dismissed only because the owner just removed a counter | **Rejected.** A′ is kept as a triggered option, and is shown to differ from the removed counter (it would count running work). It is not admitted because no present consumer lacks capture from establishment |
| The recommendation is the same as the unreviewed revision's, so nothing changed | **Rejected.** Rule 7 is replaced; Rules 1, 2, 5, 6 and 8 change; the evidence/provenance trigger changes; the sufficiency framework changes from two classes to six; three items moved from pending to landed; the Game's per-tick route is reclassified as controller-attested |
| Stale baseline | Rechecked at commit time (§2.3); handoff drift (#469) incorporated throughout |
| Synthesis seeds | **Not consulted**, per `researching.md` §2 |

**Transferability (narrow).** "Who owns a measurement's meaning and who holds the evidence it needs
are separate questions; a measurement over captured history needs a proof frontier, and an
idle-tail claim needs an attributable no-future-input statement no producer stream can supply." It
is recoverable from the canonical runtime contract and is preserved there if reconciled, so no
synthesis seed is nominated.

## 16. Surviving invariants and negative knowledge

**Surviving invariants**

1. Engine owns execution truth, result-affecting interpretation and the supported facts and cursor
   from which evidence is obtained. It holds no history and currently declares no utilization,
   occupancy, busy-time or bottleneck measure.
2. A report-only measure enters Engine only on a concrete requirement: execution participation, an
   Engine-wide contractual definition, or — for an execution-fixed aggregate — a use that cannot
   capture from establishment.
3. A measurement's meaning belongs to whoever fixes the choices execution does not fix; the default
   is the consumer that needs it.
4. Evidence custody belongs to whoever can capture it for the needed horizon; definition ownership
   neither implies nor requires it.
5. Every interval claim names its basis, proof class and any controller trust it relies on, and
   refuses otherwise. A later gap never revokes an earlier proved interval.
6. Measurement reads recorded outcomes and never infers history, finality or fault-freedom from
   Engine rules.

**Negative knowledge to keep**

- A naive fold over a stale holder reports 100 where the truth is 5 (P1).
- Identical supported prefixes and quiescent observations can precede opposite "no work left"
  answers (P4.3).
- A complete supported change stream can describe a faulted run (P8).
- `throughputPerTick` is not a period rate (P7).
- After #469, fresh observations no longer distinguish where processing happened (port C).
- Rule 7's conflation, and the inverse custody-led rule F, both fail.

## 17. Suggested durable destinations (not edited here)

1. **Architecture / runtime contract or overview domain authority.** The revised Rules 1–8 and the
   role table as the simulation-measurement ownership boundary. This is a policy decision needing
   its own independent PR review. The runtime contract's responsibilities paragraph already carries
   the custody half.
2. **Game consumer plan §4–§5, vertical-slice criterion 4.** Game-owned named definitions under
   revised Rules 5 and 7; requirements stated per §12.2; shared on Rule 4; never presented as
   Engine facts.
3. **Evidence/provenance brief and register row.** The restated trigger (§13).
4. **Governance evidence §12–§13.** Analytical-definition provenance follows the definition owner,
   proof provenance follows the holder and attestor; Governance responsibilities unchanged.
5. **Research corpus or future Game acceptance tests.** P1 (stale frontier), P4.3 (known-work
   horizon) and P8 (fault invisibility) are the highest-value reusable proving cases. The prior
   availability third-state and several-definitions cases remain worth keeping. Port A and P2 are
   recoverable from the runtime contract and may be discarded.
6. **Historical decision-rationale record:** recommended at reconciliation. Several serious
   alternatives stayed viable (A, A′, C, G). The choice knowingly trades late-join convenience for
   no Engine coupling, it follows a REOPEN, and concrete triggers exist. It should record the
   rejected Rule 7, the rejected custody-led inverse, the A′ trigger and the "retention, not
   finality" distinction.
7. This report need not remain readable after reconciliation.

## 18. Unresolved unknowns

- The concrete basis choices a future utilization verification objective would make.
- Whether a passive or Governance consumer of Game results will exist, and when (the T3 activation
  of evidence/provenance).
- Method-level support of the `FactoryRuntime` aggregate accessors (Q5).
- Whether shared delivery or a truthful A′ would be preferred if a late-join consumer appears.
- Whether one-dispatch-per-trigger after an availability return is intended behavior for Game
  curricula (engine-evolution research owns this).
- Transfer/spatial residence rechecks once executable transfer lands.

## 19. Implementation implication

**No implementation.** No analytics module, API, holder type, evidence format or Engine change is
admitted. No Game consumer is admitted.

## 20. Recommendation

**`RECOMMEND STAGED/NO SHARED OWNER NOW`** — a research recommendation, not a lifecycle or review
disposition.

**Next action: a genuinely independent adversarial review of this exact revision.** It should
attack:

- whether custody and method ownership are truly separated;
- whether any E5/E6 sufficiency claim is made without a valid proof frontier;
- whether current `busyTicks` and Engine-clock results are classified correctly;
- whether the Game's concrete demand justifies the selected owner and promotion triggers;
- whether the Engine alternatives (A, A′) were compared seriously.

## 21. Sources

**Repository at `a7f9856`:**

- `docs/architecture/runtime-contract.md` (supported observations; internal scheduler machinery;
  captured execution evidence: basis, coverage and frontier; finality; responsibilities and limits);
- `docs/architecture/engine-semantics.md` §1–§1.2, §2, §4, §9, §10–§10.2;
- `docs/architecture/overview.md` (Domain authority, headless closure, Determinism Contract);
- `docs/architecture/isa-95-semantic-mapping.md` (KPI row);
- `docs/architecture/factory-design.md` §10;
- `docs/architecture/governance-evidence.md` §5, §11–§13;
- `docs/product/charter.md` §6;
- `docs/planning/factory-design-game-consumer.md` §4–§6;
- `docs/planning/factory-design-game-vertical-slice.md`;
- `docs/research/research-register.md`;
- `docs/research/investigations/{simulation-analytics-consumer-boundary,simulation-analytics-evidence-provenance,simulation-execution-account,factory-design-game-diagnostic-evidence,engine-evolution}.md`;
- `docs/history/decisions/2026-10-09-captured-simulation-evidence.md`;
- `docs/development/{researching,testing}.md`;
- `product/domains/factory/src/main/java/com/arcogine/factory/process/*` (as listed in §2.1);
- `BoundedAdvancementConformanceTest`;
- `product/research-experiments/src/**` (`ProcessingIntervals`, `ProcessingOccupancyOracle`,
  `EvidenceWindow`, `DeclaredEvidence`, `ExperimentRunner`, `ResearchPackageBoundaryTest`,
  `StarterCorpus`).

**Merged history:** #461–#466 and #469, at the commits in §2.2.

**Prior evidence:** §1 coordinates.

**External:** none newly used. The prior artifacts' verified Roser–Nakano–Tanaka (WSC 2002) point
about named temporal bottleneck methods is not load-bearing here, and was not re-fetched.
