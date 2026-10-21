# Independent adversarial re-review: core reliability Phase A, revised packet

## Verdict

**DESIGN ADEQUATE; BLOCKERS EXPLICIT. PHASE B AUTHORIZATION: NO.**

The revised packet resolves the earlier review's blocking omission (A1) and both material requests
(A2/U10, A3). It also corrects the G-CONFORMANCE shortcut wording faithfully to source. Its inventory,
four-campaign decomposition and U1–U10 classifications withstand re-examination. No new finding blocks
the design as a design.

The re-review adds two **material** prerequisites. The packet must carry them before the affected
families can close:

- **RR-1:** `EvidenceWindow.missingWithin` has a second successor wrap, `afterSequence + 1`. It makes an
  empty query report missing sequences on *ordinary* windows. It is independent of the U3 horizon. A
  natural fix of only the recorded U10 site passes every explicit row of the packet's §4 cursor table
  and the U10 witness, while this defect survives.
- **RR-2:** for a non-comparator evidence use, the evidence evaluator and the occurrence authority bind
  only the use's target *fingerprint* to the evaluated subject, not its target *revision*. After a
  rollback F1→F2→F1, a relied-on use targeted at the first occurrence yields PASS for the later one.
  That sits against Governance evidence §4 ("a rollback never copies one use's applicability"). The
  packet treats this behavior as a hypothetical mutant and words its truth table as if such a use refuses.

Three notes (RR-3 to RR-5) refine oracle controls and protocol wording. This verdict is a design
judgment only. It is not runtime correctness, executed coverage or reliability certification. It does
not discharge owner commissioning, and it does not resolve any domain decision in §6 of the packet.

## 1. Coordinates, independence and method

| Coordinate | Value |
| --- | --- |
| Repository | `alaiba/arcogine` |
| Revised packet branch | `codex/core-reliability-phase-a-rework` |
| Revised packet commit | `260a9c9ddf29eeddf10f56192cb06ffaf9120a4d` |
| Report path / blob | `workspace/assurance/core-reliability-phase-a.md`, `caecbacea9e8e01be8924998ce61499ebccd0d50` |
| Crosswalk path / blob | `workspace/assurance/core-reliability-phase-a-crosswalk.json`, `d1015aa98b398616981eaba46c9335359b09a6d8` |
| Original assessment R | `6bc1e1ff5f11f7aff525761a027ec3b71db73f7b`; report blob `aa7f469431ccf9c5f735d305baa96a651221dd33` |
| Prior independent review V | `e0d89fe0e369fa427712653b2ef87e5493091381`; review blob `5c640ed6005895264a2066a7256bd5bb6c6f2e3a` |
| Review mandate used as procedure | `handoff/core-reliability-phase-a-independent-review-20261022`, `acf1b8088d17df33651359ce3f080f7182a5643f` |
| Rework mandate (acceptance intent) | `handoff/core-reliability-phase-a-rework-20261022`, `aaf6b9f78e2456b803e869bc68130cdbc5971e02` |
| Assessed main T | `da70c929be95b98aba773a9225ec70ba13d50c31` |
| Live main M, start and final recheck | `da70c929be95b98aba773a9225ec70ba13d50c31` (M = T) |

The owner commissioned this re-review by supplying the revised packet's branch and commit. No separate
re-review handoff document exists, either among the remote handoff branches or within the packet.
The packet's §7 names the next action as an "independent adversarial re-review of this exact revised
packet by a new session". The procedure follows the earlier review mandate's passes and finding and
verdict contract, scoped to a re-review:

1. verify custody and inventory;
2. check every rework disposition against T's source and specifications;
3. attack the changed sections for survivors;
4. examine U1–U10;
5. search for novel gaps adjacent to the changed material.

This session authored none of R, V or the rework. Independence is session-based; no different model
family is claimed. The review was performed in a separate worktree on a branch from live `main`. The
packet branch was not modified.

**Custody and inventory (independent PowerShell check).** The packet commit adds exactly two files to
T. The crosswalk was checked against `git ls-tree -r T product`, filtered to `.java`:

- 300 crosswalk entries for 300 actual paths;
- zero duplicates, zero paths missing from the tree, zero absent from the crosswalk, zero blob
  mismatches and zero empty contract lists;
- 33 distinct contract IDs, identical to the 33 bold matrix IDs in §3;
- module and source-set counts as declared: 188 main, 111 test and 1 JMH file.

The crosswalk blob equals R's, and R and V resolve to the blobs the packet cites. All relative Markdown
link targets in the packet resolve at T.

**Live state.** PR 510 is open and unmerged at `c3d5b3f5a57f61d17a8f50b90b56a074c20036b1`, matching
the rework's read. Issue 514 is closed/completed. PR 517 and PR 513 are open docs/process changes that
have not landed on T. They do not alter any T-bound contract the packet relies on.

**Evidence labels.**

- **Static:** source or specification reasoning.
- **Probe:** a bounded execution authored for this re-review against unmodified T main sources; the
  appendix reproduces it.
- **Retained:** an inspected existing assertion, not re-executed.
- **Inference.**

Probes were built by compiling the 188 main sources with native Temurin 21.0.9 `javac --release 21`
into ignored `logs/`. No Gradle, JUnit, Checkstyle, JaCoCo, mutation, campaign or CI execution is
claimed. I did not re-run V's witnesses for U1, U2, U5, U7 or U8. Instead, I checked their source sites
statically and independently re-executed the U10 witness.

## 2. Disposition of the rework against the requested corrections

| Request | Re-review result |
| --- | --- |
| **A1**, independent Phase D verdict challenge | **Adequate.** §5 now specifies entry custody with an immutable evidence manifest, a per-family executed/blocked/residual map over all 33 groups, drift reconciliation that forbids transfer of stale, truncated or lost evidence, contract-specific claims with `QUALIFIED/INCOMPLETE` as the honest default, and a separate challenger who did not author the synthesis. That challenger publishes an immutable `ACCEPT`/`QUALIFY`/`REWORK` artifact. Blocking challenges loop through reconciliation and re-review, and a missing review stops the claim. Phase A review and code-PR review cannot substitute. A protocol-wording refinement is RR-5 (note). |
| **A2 / U10**, last-representable cursor | **Adequate in structure, incomplete in localization.** The matrix rows E-EVENTS, R-CAPTURE and R-DERIVE carry the family; §4 supplies an independent endpoint oracle with no `MAX+1` sentinel, a seven-row table and five controls; both affected campaigns and §6 name it. Every expected value in the table checks out by hand against the documented `(after,through]` semantics. The U10 witness was independently re-executed (appendix, probe row E). However, the packet localizes U10 to `runFinalSequence + 1` only. The second wrap site survives the explicit design (RR-1), and two named controls are masked on their designated rows by baseline defects (RR-3). |
| **A3**, reclassification and localization | **Adequate.** Each U-entry's cited site matches T; see §4 of this re-review. U2 and U8 are stated as confirmed; U4, U5 and U6 keep their limits; U7 is an exception, not a returned wrong total; U9 is an evidence limit. Owners, alternatives and per-slice entry conditions are bounded, and no unilateral policy is chosen. |
| **G-CONFORMANCE** shortcut wording | **Accurate to source, but one phrase is overbroad.** Both truth tables in §4 match `ConformanceEvaluator.evaluate` lines 119–142 and `EvidenceConformanceEvaluator.evaluate` lines 50–84. In each, pairing and revision checks precede the scope shortcut. In the evidence evaluator, the external-evidence kind check and `validateUses` follow the shortcut; supplied PASS/FAIL without a relied-on use becomes UNKNOWN; `EvidenceJudgment` rejects NOT_APPLICABLE. The phrase "malformed exact-definition/target/revision use refuses" implies a target-revision binding that the implementation does not perform (RR-2). |
| U4/U5/U6/U7/U9 qualifications | **Adequate.** The source order in `InMemoryEvaluationOccurrenceAuthority.accept` (lines 68–74) is verify, then canonicalize and register references, then read the clock, then insert. That supports U4 exactly as stated. |

## 3. Findings

### RR-1 — MATERIAL: second successor wrap in `missingWithin` survives the explicit U10 design

**Provenance:** novel; confirmed by probe and by static source reading. **Groups:** R-CAPTURE,
E-EVENTS consumer side, R-DERIVE. **Authority:**

- `EvidenceWindow.missingWithin` Javadoc (lines 72–76): "the sequences in `(afterSequence,
  throughSequence]` that were not retained … An empty result means the whole range is available";
- retained `EvidenceWindowTest` line 36: `missingWithin(5, 5)` returns empty, labelled "an empty
  interval is trivially complete";
- retained `OracleContractTest` line 149: `completeEvents(2, 2)` returns present-empty;
- the runtime contract's basis/frontier semantics, which the packet cites.

**Source:** `product/research-experiments/src/main/java/com/arcogine/research/experiment/EvidenceWindow.java`.
Line 82, `long from = afterSequence + 1;`, wraps to `Long.MIN_VALUE` when `afterSequence == MAX`, and
the method admits `afterSequence == throughSequence` (line 78). The two consequences are:

- every declared gap then intersects, because `max(gap.from, MIN) = gap.from`;
- the beyond-frontier clause adds `[f+1, MAX]`, because `max(MIN, f+1) = f+1`.

When `f == MAX`, the beyond-frontier clause instead constructs `SequenceRange(MIN, MAX)`, and the
`SequenceRange` constructor (line 10) throws. `DeclaredEvidence.completeEvents` (line 53) and
`missingEvents` (line 70) consume this result.

**Probe** (appendix, `RangeEndpointProbe`), on unmodified T:

| Window | Query | Observed | Independent expectation |
| --- | --- | --- | --- |
| f=0, start=end=0, no gaps (initial empty window) | `(MAX,MAX]` | `[1..MAX]` | empty |
| f=5, start=1, end=5, no gaps (`isComplete` true) | `(MAX,MAX]` | `[6..MAX]` | empty |
| f=5, gap `[2,3]` | `(MAX,MAX]` | `[2..3], [6..MAX]` | empty |
| f=5 | `(4,4]`, `(MAX-1,MAX-1]` | `[]`, `[]` | empty (control: only `after = MAX` fails) |
| f=MAX, no gaps | `(MAX,MAX]` | `IllegalArgumentException: invalid supported-event sequence range MIN..MAX` | empty |

The fault is in the caller-supplied query domain, which the producer's sequence horizon does not
bound. No in-tree oracle currently queries with `after = MAX`; they all use `(0, cursor]`. Exposure
therefore runs through the public `DeclaredEvidence` API, through future derivations and through
Phase B tests. By static inference from line 53, `completeEvents(MAX, MAX)` on an ordinary complete
window returns `Optional.empty()` rather than a present empty list. A derivation built on it would
refuse spuriously, and §4 of the packet already names that false-refusal consequence for U10.

**Why the revised design misses it:**

- §6 U10 states the demonstrated behavior and the closure only as "`final+1` wraps".
- The owner alternatives include reconciling the admitted horizon at the producer. A horizon below MAX
  cannot close a query-argument wrap.
- §6 records "Empty-MAX endpoint checks are an extension of U10's designed failure family, not a
  separately claimed executed finding", and control (5) is framed as a hypothetical mutant. The
  production code already has that fault.

All seven explicit table rows and control (5)'s designated `(MAX,MAX]` row use f=MAX, or f values
whose empty queries sit below MAX. On that row, the after-wrap is masked once the final-wrap is guarded.
A natural final-only fix guards the beyond-frontier clause with `if (f < through)`. The
`FinalOnlyFixSurvivorProbe` (appendix) executes exactly that fix on a copy of the method. It passes all
twelve explicit table cases and the U10 witness, yet still returns `[6..MAX]` for f=5 and `[1..MAX]` for
f=0 on `(MAX,MAX]`. The general sentence "Generate all ordered endpoint choices from {0,1,MAX-1,MAX}"
would expose the survivor only if the frontier f is crossed with the query endpoints, and the packet
does not say that it is.

**Minimal correction (design text; no fix here):**

1. Record the second site as a confirmed witness inside U10 (or as U10b). Its domain is the query
   argument, independent of U3.
2. U10 closure must cover both successor computations, and producer-horizon reconciliation cannot
   close the query-side wrap. The only alternative is an explicit, owner-documented narrowing of the
   helper's accepted `afterSequence` domain.
3. Add explicit rows `(MAX,MAX]` on f=0, on f=5 with no gaps and on f=5 with an interior gap, all
   expected empty.
4. State that f is enumerated jointly with query endpoints, and list "exception versus empty" as a
   distinct observed-outcome class for triage.
5. Reword control (5) as a current defect, not a mutant.

**Effect:** blocks closure of the research-capture extreme-cursor family and any Phase D claim of
total `missingWithin`/`completeEvents` correctness until resolved. It does not block ordinary
finite capture slices or Phase A design adequacy.

### RR-2 — MATERIAL: evidence-use target revision is not bound to the evaluated subject revision

**Provenance:** novel; confirmed by probe and by static source reading; owner decision required. It is
classified as a **plausible contract contradiction**, not a confirmed defect, because a retained test
endorses a neighbouring relaxation (below). **Groups:** G-CONFORMANCE, G-EVIDENCE, G-OCCURRENCE, with
G-REVISION lineage. **Authority:** `docs/architecture/governance-evidence.md`:

- §4, lines 100–110: a use records "the target subject (model fingerprint, optional verified controlled
  revision, semantic scope)"; each use has "its own independent subject/revision/claim/time/applicability
  determination. Equal fingerprints or a rollback never copy one use's applicability or acceptance to
  another target"; "A use may not synthesize an accepted controlled revision for a candidate that has
  none";
- §6, lines 167–169: wrong-subject evidence "never silently produces `PASS`";
- `EvidenceUse` Javadoc (lines 15–16): "The target is intentionally a point identity: model
  fingerprint, optional verified revision, and optional semantic scope."

**Source:** three checks bind a non-comparator use's target fingerprint to the subject fingerprint.
Where a target revision is present, they check only that it is bound to the use's own target
fingerprint. None compares it with the evaluated or occurrence revision:

- `EvidenceConformanceEvaluator.validateUses`, lines 93–107;
- `EvaluationOccurrenceDraft.validateUses`, lines 107–121;
- `InMemoryEvaluationOccurrenceAuthority.verifyUseRevisions`, lines 107–124.

**Probe** (appendix, `UseTargetRevisionProbe`). The lineage is R1(F1) → R2(F2) → R3(F1), the
F1→F2→F1 rollback shape that G-REVISION treats as distinct occurrences, held in a fixture revision
authority. An external-evidence assertion uses one relied-on use whose target is (F1, R1) with
APPLICABLE applicability:

- `EvidenceConformanceEvaluator.evaluate` for subject (F1, R3) returns **PASS** attributed to R3;
- for a revisionless candidate F1, it returns **PASS**;
- `InMemoryEvaluationOccurrenceAuthority.accept` accepts an occurrence for R3 whose relied-on use
  targets R1.

A retained test, `GovernanceEvidenceTest` lines 624–656, evaluates a revision-bound subject with a
*revisionless* use and asserts FAIL. Revisionless uses are therefore intentionally accepted for a
revisioned subject. No retained test covers a use whose present revision differs from the subject's
revision.

**Why the revised design misses or misclassifies it:**

- §3 G-CONFORMANCE says "exact definition/target/revision checks precede adequacy/outcome", and the
  §4 evidence table says "malformed exact-definition/target/revision use refuses". Both read as if the
  cross-revision use refuses.
- §3 G-EVIDENCE lists "auto-copy applicability on rollback" as a mutant to inject. The implementation
  has no such guard to remove.

A Phase B author could follow the text and expect refusal, then meet an undeclared mid-campaign owner
decision. Alternatively, the author could read "revision" as only the use-internal revision-to-
fingerprint binding the code checks, and encode the current PASS as the expected oracle. Either way the
decision is absent from §6, from the Governance slice entry conditions and from the Phase D blocked
claims.

**Smallest owner decision:** for a non-comparator use, is a present target revision that differs from
the evaluated subject's revision wrong-subject, so that it refuses or cannot count as an adequate
basis? Is a use revision present on a revisionless subject also wrong-subject? Or is use targeting
intentionally fingerprint-level, so that §4's rollback sentence needs scoping? The answer should be
consistent with the retained revisionless-use precedent.

**Minimal correction (design text; no fix here):**

1. Add a U11 row with owner, alternatives and closure evidence.
2. Narrow the §3 and §4 wording to the checks the implementation actually performs.
3. Add "use target revision: absent / equal / different with same fingerprint / present on
   revisionless subject" as an explicit dimension of the Governance truth table and the occurrence
   ledger.
4. Move "auto-copy applicability on rollback" from the mutant list to the decision-dependent cases.

**Effect:** blocks closure of the use-binding family in Governance slices 1–2 and any Phase D claim
that rollback or equal fingerprints never transfer applicability. Other Governance slices are
unaffected.

### RR-3 — NOTE: two named endpoint controls are masked by baseline defects on their designated rows

**Provenance:** static, confirmed by the RR-1 and U10 probe outputs. On unmodified T, every f=MAX row
already returns the whole query as missing, because `beyondFrom` collapses to `from`. Two of the §4
controls are affected:

- **Control (4)**, "ignoring a declared gap must disagree on the MAX singleton gap". On row
  `f=MAX, gap [MAX,MAX], (MAX-1,MAX]`, T returns a duplicate `[MAX..MAX], [MAX..MAX]` (probe row G).
  The ignore-gaps mutant returns a single `[MAX..MAX]`, which *equals* the expected set, so the mutant
  improves agreement.
- **Control (2)**, the exclusive-endpoint `>`→`>=` mutant. On f=MAX rows it is likewise
  indistinguishable before the fix. After the fix, it discriminates only on a gap-boundary row such as
  `f=MAX, gap [1,1], (1,MAX]`, not on the gapless singleton/empty rows the packet names.

Controls (1) and (5) are current production behavior, not mutants (RR-1).

**Correction:** designate discriminating rows with f < MAX, for example `f=5, gap [2,3]` with queries
`(0,5]`, `(2,5]` and `(3,5]`. Validate controls separately against the pre-fix baseline and a corrected
helper, and do not count a baseline discrepancy as a control kill. The packet's shared-execution rule 1
would probably surface this in Phase B; the note makes it explicit up front.

### RR-4 — NOTE: recorded read support is a covering hull, not a completeness claim

**Static.** `DeclaredEvidence.completeEvents` (lines 59–63) merges successive reads into
`[min from, max through]`. `EvidenceSupport` (lines 7–13) documents "the smallest interval … covering
every event range that was read". Take two complete reads `(0,2]` and `(5,7]` around a declared gap
`[3,4]`: the recorded support is `[1..7]`, which spans unretained sequences. This is documented
behavior, not a defect.

The R-CAPTURE row and the §4 recipe ask Phase B to compare "recorded read support". The oracle should
state hull semantics, and no downstream consumer may read a support interval as proof that its interior
was complete.

### RR-5 — NOTE: Phase D challenger independence from evidence production

§5 requires the challenger to have no authorship of the final synthesis, and by default it is not the
Phase A reviewer. It does not exclude, or require disclosure of, a challenger who executed the B
campaigns or C reruns whose evidence is under challenge. A challenger reviewing frontiers and survivors
it produced is not independent with respect to that evidence. **Correction:** require the challenger to
declare any campaign or rerun authorship, and to have no authorship of the evidence for the families it
adjudicates (or name a second challenger for those families).

### Hypotheses examined and dismissed

- The structural evaluator's caller-supplied `stateFingerprint` function runs before scope. The packet
  already treats caller adapters as trust boundaries, so no gap.
- The comparator role with a cross-model target and revision is rejected correctly (retained
  `GovernanceEvidenceTest` lines 664–676).
- `isComplete()` versus head gaps: the constructor does not cross-validate `missingSequences` against
  start, end and final, and the packet already excludes treating arbitrary record construction as
  trace validation.
- The unchecked `Scheduler.nextSequence` wrap inverts same-time FIFO after exhaustion. That is already
  inside U3's Scheduler horizon decision.
- The U8 fingerprint orders offers by `String` natural order. That is consistent and is not an
  additional alias beyond the lossy UTF-8 already recorded.

## 4. U1–U10 re-review disposition

| Entry | Re-review | Basis |
| --- | --- | --- |
| U1 | Agree: confirmed contradiction plus contract decision | `SimTime.minus` is `Math.max(0, value - other.value)` with "returns 0 if other > this" (static). |
| U2 | Agree: confirmed diagnostic nondeterminism | `OperationStepDefinition` stores `Set.copyOf(...)` and the validator iterates `step.eligibleResources()` (static; V's eight-JVM probe and the rework's replication). |
| U3 | Agree: contract decision, static induction | `FactoryRuntime` `eventSequence++` and `Scheduler` `nextSequence++` are unchecked (static). The horizon decision does not cover RR-1's query-side wrap. |
| U4 | Agree | Accept order: verify, then register references, then read the clock, then insert (static). |
| U5 | Agree: constructed-domain decision | Not re-executed; the limits are preserved correctly. |
| U6 | Agree: no invented guarantees | Not re-examined beyond V. |
| U7 | Agree: executed exception; decision required | `ProcessingIntervals` uses `Long::sum` (static). Its `completeEvents(0, cursor)` gate is also U10-dependent at cursor MAX. |
| U8 | Agree: confirmed accepted-content alias | `writeOffer` uses `getBytes(StandardCharsets.UTF_8)` on the item text (static). |
| U9 | Agree: evidence limit | No suite or coverage execution here either. |
| U10 | **Qualify**: confirmed; under-localized | Witness independently re-executed (probe row E). It must include the second site (RR-1). |
| U11 (new) | Contract decision; plausible contradiction (probe) | RR-2. |

## 5. Challenge coverage and unreviewed surfaces

**Re-attacked directly:**

- §1 dispositions;
- the matrix rows the rework touched: T-TIME, F-PUBLISH, E-EVENTS, G-CONFORMANCE, C-CATALOGUE,
  C-ATTEMPT, C-CONTENT, R-CAPTURE and R-DERIVE;
- the §4 extreme-cursor oracle, table and controls, and the Governance ordering tables;
- the §5 campaign entry conditions and the Phase D protocol;
- the §6 decision, provenance and reproduction tables, and §7.

G-EVIDENCE and G-OCCURRENCE were re-attacked through RR-2.

**Not re-attacked beyond V's design-level coverage, because they were unchanged by the rework:**
T-VALUE, F-CODEC, F-ASSEMBLE, F-CHANGE, E-SCHEDULE, E-DISPATCH, E-WORK, E-CONTROL, E-ARITH,
E-OBSERVE, G-REVISION, G-CHANGE, G-DEFINITION, all S-*, C-DEFINITION, C-ECONOMICS, C-ADMISSION,
C-EVALUATION and X-BOUNDARY. This is an explicit examination limit, not an endorsement by silence.

There was no executed campaign, mutation run, model exploration, test suite, coverage or CI. Tool and
campaign cost remain unmeasured, as the packet states.

## 6. Requested amendments and next owner action

The design is adequate with RR-1 and RR-2 carried as explicit prerequisites. This review now states
them. The recommended amendments are bounded:

1. Extend U10 with the query-side wrap, the f<MAX `(MAX,MAX]` rows, the joint enumeration of f with
   query endpoints, an exception outcome class and both-site closure (RR-1).
2. Add U11 and align the §3 and §4 Governance wording and dimensions (RR-2).
3. Re-designate the endpoint control rows (RR-3).
4. State support-hull semantics (RR-4).
5. Add the challenger's evidence-authorship exclusion (RR-5).

If the amendments stay within these passages, re-review only the amended passages against this
artifact; a further full re-review is not required. Folding them in is recommended, but this artifact
binds the affected slices either way.

**Next owner action:**

1. Decide whether to fold these amendments into the packet.
2. Resolve or explicitly scope the slice-specific owner decisions: packet §6 plus U10's second site and
   U11.
3. Commission specific Phase B slices explicitly.

Unrelated slices can proceed after commissioning, under the packet's existing entry conditions. This
review authorizes nothing.

## Appendix: reproduction

From a clean checkout of T, in PowerShell from the repository root:

```powershell
New-Item -ItemType Directory -Force logs/rereview/classes | Out-Null
git ls-tree -r --name-only da70c929be95b98aba773a9225ec70ba13d50c31 product |
  Where-Object { $_ -match '/src/main/.*\.java$' } | Set-Content logs/rereview/main-sources.txt
javac --release 21 -d logs/rereview/classes '@logs/rereview/main-sources.txt'
javac --release 21 -cp logs/rereview/classes -d logs/rereview/classes logs/rereview/RangeEndpointProbe.java logs/rereview/UseTargetRevisionProbe.java logs/rereview/FinalOnlyFixSurvivorProbe.java
java -cp logs/rereview/classes RangeEndpointProbe
java -cp logs/rereview/classes UseTargetRevisionProbe
java -cp logs/rereview/classes FinalOnlyFixSurvivorProbe
```

`logs/rereview/RangeEndpointProbe.java`:

```java
import com.arcogine.research.experiment.*;
import com.arcogine.types.RunId;
import java.util.List;

public class RangeEndpointProbe {
    static final long MAX = Long.MAX_VALUE;

    static String q(EvidenceWindow w, long a, long b) {
        try {
            return w.missingWithin(a, b).toString();
        } catch (RuntimeException ex) {
            return ex.getClass().getSimpleName() + ": " + ex.getMessage();
        }
    }

    static EvidenceWindow w(long start, long end, long fin, List<SequenceRange> gaps) {
        return new EvidenceWindow(RunId.create(), ExperimentFixture.WindowIntent.COMPLETE_RUN, start, end, fin, gaps, List.of());
    }

    public static void main(String[] args) {
        EvidenceWindow empty = w(0, 0, 0, List.of());
        EvidenceWindow small = w(1, 5, 5, List.of());
        EvidenceWindow smallGap = w(1, 5, 5, List.of(new SequenceRange(2, 3)));
        EvidenceWindow full = w(1, MAX, MAX, List.of());
        EvidenceWindow maxGap = w(1, MAX - 1, MAX, List.of(new SequenceRange(MAX, MAX)));
        EvidenceWindow preMax = w(1, MAX - 1, MAX - 1, List.of());
        System.out.println("A f=0 complete=" + empty.isComplete() + " (MAX,MAX] -> " + q(empty, MAX, MAX));
        System.out.println("B f=5 complete=" + small.isComplete() + " (MAX,MAX] -> " + q(small, MAX, MAX));
        System.out.println("C f=5 gap[2,3] (MAX,MAX] -> " + q(smallGap, MAX, MAX));
        System.out.println("D f=5 (4,4] -> " + q(small, 4, 4) + " ; (MAX-1,MAX-1] -> " + q(small, MAX - 1, MAX - 1));
        System.out.println("E f=MAX complete=" + full.isComplete() + " (0,MAX] -> " + q(full, 0, MAX));
        System.out.println("F f=MAX (MAX-1,MAX] -> " + q(full, MAX - 1, MAX) + " ; (MAX,MAX] -> " + q(full, MAX, MAX));
        System.out.println("G f=MAX gap[MAX,MAX] (MAX-1,MAX] -> " + q(maxGap, MAX - 1, MAX));
        System.out.println("H f=MAX-1 (MAX-1,MAX] -> " + q(preMax, MAX - 1, MAX));
        System.out.println("I f=0 (0,0] -> " + q(empty, 0, 0) + " ; (0,1] -> " + q(empty, 0, 1));
    }
}
```

Observed at T, where `MAX` is 9223372036854775807:

```text
A f=0 complete=true (MAX,MAX] -> [SequenceRange[from=1, through=MAX]]
B f=5 complete=true (MAX,MAX] -> [SequenceRange[from=6, through=MAX]]
C f=5 gap[2,3] (MAX,MAX] -> [SequenceRange[from=2, through=3], SequenceRange[from=6, through=MAX]]
D f=5 (4,4] -> [] ; (MAX-1,MAX-1] -> []
E f=MAX complete=true (0,MAX] -> [SequenceRange[from=1, through=MAX]]
F f=MAX (MAX-1,MAX] -> [SequenceRange[from=MAX, through=MAX]] ; (MAX,MAX] -> IllegalArgumentException: invalid supported-event sequence range -9223372036854775808..MAX
G f=MAX gap[MAX,MAX] (MAX-1,MAX] -> [SequenceRange[from=MAX, through=MAX], SequenceRange[from=MAX, through=MAX]]
H f=MAX-1 (MAX-1,MAX] -> [SequenceRange[from=MAX, through=MAX]]
I f=0 (0,0] -> [] ; (0,1] -> [SequenceRange[from=1, through=1]]
```

The independent expectations are: rows A, B, C and the second query of F empty; D empty; E empty;
the first query of F empty; G the singleton set {MAX}; H `[MAX..MAX]`; I empty, then `[1..1]`.

`logs/rereview/UseTargetRevisionProbe.java`:

```java
import com.arcogine.governance.*;
import com.arcogine.governance.assertion.*;
import com.arcogine.governance.conformance.*;
import com.arcogine.governance.evidence.*;
import com.arcogine.governance.requirement.*;
import com.arcogine.types.ControlledRevisionId;
import com.arcogine.types.ModelFingerprint;
import java.time.Instant;
import java.util.*;

public class UseTargetRevisionProbe {
    public static void main(String[] args) {
        ModelFingerprint f1 = new ModelFingerprint("test", "sha256", "a".repeat(64));
        ModelFingerprint f2 = new ModelFingerprint("test", "sha256", "b".repeat(64));
        RevisionProvenance prov = new RevisionProvenance(Instant.EPOCH, new RevisionRecorder("test", "probe"));
        ControlledRevision r1 = new ControlledRevision(ControlledRevisionId.generate(), f1, List.of(), prov);
        ControlledRevision r2 = new ControlledRevision(ControlledRevisionId.generate(), f2, List.of(r1.id()), prov);
        ControlledRevision r3 = new ControlledRevision(ControlledRevisionId.generate(), f1, List.of(r2.id()), prov);
        Map<ControlledRevisionId, ControlledRevision> byId = new LinkedHashMap<>();
        for (ControlledRevision r : List.of(r1, r2, r3)) byId.put(r.id(), r);
        ControlledRevisionAuthority authority = new ControlledRevisionAuthority() {
            public ControlledRevision accept(ControlledRevision c, SemanticArtifact a) { throw new UnsupportedOperationException(); }
            public Optional<ControlledRevision> findById(ControlledRevisionId id) { return Optional.ofNullable(byId.get(id)); }
            public HistoricalRevision resolve(ControlledRevisionId id) {
                ControlledRevision r = byId.get(id);
                if (r == null) throw new IllegalArgumentException("unknown " + id);
                return new HistoricalRevision(r, new SemanticArtifact(r.modelFingerprint(), new byte[0]));
            }
            public List<ControlledRevision> revisions() { return List.copyOf(byId.values()); }
        };

        Requirement requirement = new Requirement(new RequirementId("req"), new RequirementVersion(1), "w", "w",
                ArcogineNativeRequirementSource.of("probe"), RequirementScope.empty());
        Assertion<?> assertion = new Assertion<>(new AssertionId("as"), new AssertionVersion(1), requirement.id(),
                requirement.version(), "external", EvidenceRequirement.EXTERNAL_EVIDENCE_REQUIRED, null);
        EvaluationOccurrenceId occurrence = EvaluationOccurrenceId.generate();
        EvidenceUse useForR1 = new EvidenceUse(occurrence, 0,
                new EvidenceReference("source", "record", EvidenceProvenance.unknown("probe")),
                f1, r1.id(), null, requirement, assertion, EvidenceUseRole.RELIED_ON,
                TemporalFrame.atKnowledgeBoundary(Instant.EPOCH),
                new EvidenceApplicability(EvidenceApplicabilityStatus.APPLICABLE, "determined for R1", "probe rule"));

        ConformanceEvaluation forR3 = EvidenceConformanceEvaluator.evaluate(requirement, assertion, Optional.empty(), f1,
                Optional.of(r3.id()), authority, List.of(useForR1), new EvidenceJudgment(ConformanceResult.PASS, "supported"));
        System.out.println("evaluate subject=R3(F1), relied-on use target=R1(F1): " + forR3.result()
                + " revision=" + (r3.id().equals(forR3.controlledRevisionId()) ? "R3" : String.valueOf(forR3.controlledRevisionId())));

        ConformanceEvaluation forCandidate = EvidenceConformanceEvaluator.evaluate(requirement, assertion, Optional.empty(), f1,
                Optional.empty(), authority, List.of(useForR1), new EvidenceJudgment(ConformanceResult.PASS, "supported"));
        System.out.println("evaluate revisionless candidate(F1), relied-on use target=R1(F1): " + forCandidate.result());

        InMemoryEvaluationOccurrenceAuthority occurrences = new InMemoryEvaluationOccurrenceAuthority(authority);
        EvaluationOccurrence accepted = occurrences.accept(new EvaluationOccurrenceDraft(occurrence, requirement, assertion, f1,
                r3.id(), List.of(useForR1), List.of(), List.of(), TemporalFrame.atKnowledgeBoundary(Instant.EPOCH), "rules",
                forR3, "pass", null));
        System.out.println("occurrence accepted: subjectRevision=" + (accepted.controlledRevisionIdOptional().map(r3.id()::equals).orElse(false) ? "R3" : "?")
                + " reliedOnUseTargetRevision=" + (r1.id().equals(accepted.reliedOnUses().get(0).targetControlledRevision()) ? "R1" : "?")
                + " result=" + accepted.evaluation().result());
    }
}
```

Observed at T:

```text
evaluate subject=R3(F1), relied-on use target=R1(F1): PASS revision=R3
evaluate revisionless candidate(F1), relied-on use target=R1(F1): PASS
occurrence accepted: subjectRevision=R3 reliedOnUseTargetRevision=R1 result=PASS
```

The revision authority is a probe fixture holding the supported F1→F2→F1 lineage shape. The
evaluator, use, draft and occurrence authority are the public production types at T.

`logs/rereview/FinalOnlyFixSurvivorProbe.java` copies `missingWithin`'s logic at T, changing one thing:
the beyond-frontier clause is guarded by `if (f < b)`. That is a natural fix for the recorded U10 site
only. It is a hypothetical-implementation control and does not touch product code:

```java
static List<long[]> finalOnlyFix(long f, List<long[]> gaps, long a, long b) {
    long from = a + 1;
    List<long[]> missing = new ArrayList<>();
    for (long[] g : gaps) {
        long low = Math.max(g[0], from);
        long high = Math.min(g[1], b);
        if (low <= high) missing.add(new long[] {low, high});
    }
    if (f < b) {
        long beyondFrom = Math.max(from, f + 1);
        if (beyondFrom <= b) missing.add(new long[] {beyondFrom, b});
    }
    return missing;
}
```

Applied to all twelve explicit cases in the packet's §4 table, it matches every independent
expectation, including the U10 witness `f=MAX (0,MAX]`, which returns `[]`. It still returns
`[6..MAX]` for `f=5 (MAX,MAX]` and `[1..MAX]` for `f=0 (MAX,MAX]`, where the expectation is empty.

Human Git identity: Vasile Alaiba, `vasile@alaiba.ro`. This branch is a transient review artifact, not
a merge candidate, so no tracked-workspace merge-gate pass is expected. No PR was opened, no branch was
deleted, nothing was merged, the packet branch was not modified, and Phase B was not begun.
