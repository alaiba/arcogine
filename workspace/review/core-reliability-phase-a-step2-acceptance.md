# Step 2 independent acceptance addendum: Phase A final design amendments

## Verdict

**PHASE A DESIGN ACCEPTED — CONTRACT BLOCKERS EXPLICIT**

**PHASE B AUTHORIZATION: NO**

RR-1 through RR-5 and the new U11 are incorporated correctly into every affected passage of packet A. Each one was attacked independently, and none fell. The two consequential corrections were checked against real T classes with new probes:

- **RR-1.** The joint frontier × query design catches W's final-only-fix survivor on the ordinary-frontier `(MAX,MAX]` rows. It catches the opposite partial fix (after-only) on the f=MAX rows. A fully corrected helper copy matches the oracle on all 98 rows.
- **RR-2/U11.** The behavior is stated faithfully and the outcome is left to the owner. The truth-table dimensions discriminate every cell asked for.

Four NOTE-level refinements remain (S2-1 to S2-4). None is blocking or material. Each can ride into the relevant owner-decision packet or Phase B slice commission, and none requires another packet revision or review round.

This is a design acceptance only. It is not runtime correctness, executed coverage, a fixed defect or reliability certification. Every owner decision in packet §6 (U1, U2, U3, U5, U7, U8, U10, U11, plus optional U4/U6) remains open.

## 1. Coordinates and custody

| Role | Branch / commit / path / blob |
| --- | --- |
| This commission | `handoff/core-reliability-phase-a-step2-acceptance-20261024`, `5109d9a26e7f19ea00588c33f671d2225265d21a`, `workspace/review/core-reliability-phase-a-step2-acceptance-handoff.md` (resolved) |
| Reviewed packet A | `codex/core-reliability-phase-a-final-amendments-20261023`, `baf2e2a94750a350cc533663d52c4c147f41d9ba`; report blob `a13361a2b067402191c01dc66bc5a34351b09644`, crosswalk blob `d1015aa98b398616981eaba46c9335359b09a6d8` (both verified) |
| Preceding packet Q | `codex/core-reliability-phase-a-rework`, `260a9c9ddf29eeddf10f56192cb06ffaf9120a4d`; report blob `caecbacea9e8e01be8924998ce61499ebccd0d50`, crosswalk blob `d1015aa…` (verified) |
| Controlling re-review W | `claude/core-reliability-phase-a-re-review`, `72da6152c93b6821d61f8f70a378e08c7232f74a`, `workspace/review/core-reliability-phase-a-independent-re-review.md`, blob `d43ab8adb95f34548629996260271234f8425577` (verified) |
| Step 1 commission | `handoff/core-reliability-phase-a-final-amendments-20261023`, `649a748db8bb5f39eb8b03cec5e297c6def18960` (read in full) |
| Earlier acceptance commission | `handoff/core-reliability-phase-a-acceptance-review-20261023`, `eb3d310c9b29a09e9543e013124d52805d47f810`, `workspace/review/core-reliability-phase-a-acceptance-handoff.md` (exists; read in full) |
| R / V | `6bc1e1ff5f11f7aff525761a027ec3b71db73f7b` / `e0d89fe0e369fa427712653b2ef87e5493091381` (R report read for row comparison; V cited through A, Q and W) |
| T | `da70c929be95b98aba773a9225ec70ba13d50c31` |
| Live main M | `da70c929…` at start and at final recheck: **M = T**, empty diff. No drift. |

**Custody checks, all independent.**

- A's parent is T. A adds exactly the two packet files and changes no product, test, spec, doc, CI or tooling path. Q and W likewise add only their own workspace files.
- The crosswalk is byte-identical across Q and A. `assessedCommit` is T. Its 300 entries match the 300 `.java` paths in `git ls-tree -r T product` with zero blob mismatches. It covers nine modules and 33 distinct contract IDs. Per the commission, this is a narrow identity check, not a new semantic survey.
- Comparing the A report against the Q report directly (fetched blobs, not a branch compare), exactly six §3 matrix rows changed: E-EVENTS, G-CONFORMANCE, G-EVIDENCE, G-OCCURRENCE, R-CAPTURE and R-DERIVE. Those are the rows RR-1, RR-2 and RR-4 target. The other 27 rows are byte-identical to Q. The other edits are in §1 dispositions, §4 B/L/M and Governance tables, §5 campaigns and Phase D, §6 U10/U11/reproduction/challenge guide, and §7. Headings changed only in three titles.

## 2. Independence, method and evidence classes

This session authored none of R, V, Q, W or A, and none of their commissions. It had no prior involvement in this assessment line before reading A. Independence here is session-based. No different model family is claimed, and a different branch or worktree is not offered as proof.

Evidence labels used below:

- **[static]** — source, Javadoc or specification reasoning at T.
- **[retained]** — an inspected existing assertion, not rerun.
- **[W-probe]** — W's prior executed probe, cited as prior evidence.
- **[S2-probe]** — a new probe authored for this addendum and executed against the 188 unmodified T main sources (appendix).
- **[math]** — independent oracle reasoning.
- **[future]** — a proposed Phase B diagnostic.
- **[owner]** — an unresolved owner semantic decision.

Two new probes were run with native Temurin 21.0.9, `javac --release 21`, from a `git archive` of T into scratch space outside the worktree:

- `S2CursorProbe` contains an independently written endpoint oracle, self-checked against brute-force membership in 154,880 comparisons. It also contains the real `EvidenceWindow` and isolated copies of candidate partial fixes and single mutants.
- `S2UseTargetProbe` exercises the public Governance evaluator and occurrence authority over the U11 cells.

No Gradle, JUnit, Checkstyle, JaCoCo, mutation campaign, model exploration or CI was run. Nothing in A is read as such an execution.

**Narrow scope.** The commission's examination scope was attacked directly: RR-1..RR-5, U10/U11, the six changed rows, §4 B/L/M and the Governance tables, §5 campaign entry/exit, Phase D, §6, the challenge guide and §7. The 27 unchanged matrix rows, the J/P/I/A method families and the Storage/Factory/Challenge campaign bodies were **not** re-attacked. Only their consistency with U1–U9 and their Q→A identity were checked. This is an examination limit, not an endorsement by silence.

## 3. RR-1 to RR-5 disposition

| Finding | Disposition | Basis |
| --- | --- | --- |
| RR-1 both `missingWithin` successor sites | **ACCEPTED** | §3.1 |
| RR-2 / U11 use-target revision | **ACCEPTED WITH NARROW LIMIT** (S2-2, S2-3) | §3.2 |
| RR-3 non-masked controls | **ACCEPTED WITH NARROW LIMIT** (S2-4) | §3.3 |
| RR-4 read-support hull | **ACCEPTED** | §3.4 |
| RR-5 Phase D evidence-authorship exclusion | **ACCEPTED** | §3.5 |

### 3.1 RR-1 — ACCEPTED

**Passages inspected (A).**

- §3 rows E-EVENTS, R-CAPTURE and R-DERIVE.
- §4 "Extreme cursor oracle and controls": the relation, the endpoint-pair algorithm, the 14-row table, the joint-exploration paragraph, the outcome classes, completeEvents/missingEvents, and the sensitivity table.
- §5: runtime and research campaigns, and Phase D.
- §6: U10 row and reproduction rows U10a, U10b and the negative control.
- Challenge guide and §7.

**Sources at T [static].**

- `EvidenceWindow.missingWithin` has two successor sites: `long from = afterSequence + 1` and `Math.max(from, runFinalSequence + 1)`. The precondition `afterSequence >= 0 && throughSequence >= afterSequence` admits a=b=MAX.
- `SequenceRange` requires `from >= 1`.
- `DeclaredEvidence.completeEvents` returns `Optional.empty()` whenever `missingWithin` is non-empty, and `missingEvents` returns its list unchanged.

**Independent relation [math].** Derived from the `missingWithin` Javadoc ("sequences in `(after, through]` that were not retained … beyond the run's final cursor … counts as missing") and the record Javadoc (`missingSequences` lists unretained sequences in `1..runFinalSequence`):

> missing(a,b) = { s : a < s ≤ b ∧ (s > f ∨ s ∈ ∪G) }

That is exactly A's relation. It does not depend on gap normalization. Membership via "∨" is idempotent under overlapping, unsorted or beyond-frontier gaps. The record constructor does not validate any of those, and A correctly separates "legal-shaped", "consistent declared window" and "runtime-produced".

A's endpoint-pair algorithm needs no MAX+1 at any step:

1. Each gap [g₁,g₂] becomes the open/closed pair (g₁−1, g₂]. `g₁−1` is safe because `g₁ ≥ 1`.
2. Intersect it with (a,b] as (max(a,g₁−1), min(b,g₂)]. The pair is kept only if lo < hi.
3. The post-frontier pair is (max(a,f), b], kept only if max(a,f) < b.
4. Convert back to inclusive as lo+1. This is safe because lo < hi ≤ MAX.

The early `a=b → ∅` return is consistent with the relation, and the oracle would be correct without it. The oracle in `S2CursorProbe` implements exactly this, merges adjacent ranges with BigInteger and never iterates to MAX. It agreed with brute-force membership in all 154,880 checks. Those checks covered f=0..7, every subset of singleton gaps, 0≤a≤b≤9, s=0..10, plus an overlapping, beyond-frontier gap set [S2-probe].

**Falsification attempts and outcomes [S2-probe, real T classes].**

| Commission row | Oracle | T observed | Packet A row |
| --- | --- | --- | --- |
| f=0, start=end=0, (MAX,MAX] | ∅ | [1..MAX] spurious | present |
| f=5 complete, (MAX,MAX] | ∅ | [6..MAX] spurious | present |
| f=5 gap [2,3], (MAX,MAX] | ∅ | [2..3],[6..MAX] spurious | present |
| f=MAX complete, (MAX,MAX] | ∅ | IllegalArgumentException | present |
| f=MAX, (0,MAX] | ∅ | [1..MAX] spurious | present |
| f=MAX, (MAX-1,MAX] | ∅ | [MAX..MAX] spurious | present |
| f=MAX gap [MAX,MAX], (MAX-1,MAX] | {MAX} | [MAX..MAX] twice (membership correct, duplicate) | present |
| f=MAX−1, (MAX-1,MAX] | {MAX} | exact | present |
| f=5 gap [2,3]: (0,5], (2,5], (3,5] | [2,3], [3,3], ∅ | exact, exact, exact | present |

This reproduces W's observations independently and confirms every expected value in A's table. Over the joint grid (f ∈ {0,1,5,MAX−1,MAX} × every a≤b from {0,1,MAX−1,MAX}, gapless plus gap [2,3] where f≥5: 80 rows) together with A's 18 explicit row/query cells:

- **T (real):** 27 wrong (spurious, false-present, or exception); 2 duplicate-only.
- **W's final-only fix (guard f<b only):** 10 wrong, including A's explicit ordinary-frontier rows f=0, f=5 and f=5+gap at (MAX,MAX]. It passes both f=MAX rows. **The survivor is caught by explicitly enumerated ordinary-f rows, as the commission requires, and nothing in the detection depends on U3.**
- **After-only fix (guard a=b only):** 15 wrong, including A's explicit f=MAX (0,MAX], (MAX-1,MAX] and gap [1,1] (1,MAX] rows. **The two sites are separately discriminated.**
- **Corrected copy (both guards):** 0 wrong on all 98 rows. A's expectations are mutually consistent with a correct helper.
- I also checked a saturating `a==MAX ? MAX : a+1` fix and an `addExact` fix by hand [math]. On f=0/f=5 (MAX,MAX] they yield [MAX,MAX] and an exception respectively. A's explicit rows and "exception versus empty" class catch both.

**Consumer consequences [static].** Traced from `DeclaredEvidence`, A's statements hold:

- `completeEvents(MAX,MAX)` on an ordinary complete window returns `Optional.empty()` instead of present-empty.
- f=MAX empty-MAX throws before any result.
- `completeEvents(0,MAX)` at f=MAX refuses.
- `missingEvents` exposes the raw list.

The hull update in `completeEvents` has its own `afterSequence + 1`, but it runs only when `through > after`, so it cannot wrap. A correctly says R-DERIVE must not read a U10 refusal as genuinely insufficient evidence.

**Separation of domains.** A states the helper Javadoc's direct promise and the admitted a=b=MAX domain. It keeps the U3 producer horizon separate and states that a lower horizon cannot fix a (MAX,MAX] caller query unless the helper owner explicitly changes the query precondition. A chooses no precondition. Constructed metadata is never claimed as a MAX-event capture. A asks for 2^63 events nowhere.

**Limit.** On rows A7 and A8 (f=MAX with a gap covering the whole query), T returns the correct membership as a duplicated range. A says "preserve raw duplicate diagnostic" but does not say whether duplicate-with-correct-membership passes the family. Given that A deliberately declines to invent a normal-form guarantee, the reading is "membership pass plus recorded representation anomaly". A Phase B author should state that reading explicitly, and so should any consumer that sums `SequenceRange.size()` over `missingEvents`. This is not a design defect. It is noted for the U10 owner packet.

**What remains:**

- **[owner]:** the U10 helper query domain and the U3 horizon.
- **Known remediation:** both successor sites, which are confirmed defects against the current Javadoc.
- **[future]:** the joint grid, the controls and the consumer checks.

### 3.2 RR-2 / U11 — ACCEPTED WITH NARROW LIMIT

**Passages inspected (A).**

- §3 rows G-CONFORMANCE, G-EVIDENCE and G-OCCURRENCE.
- §4 "Governance shortcut oracle ordering" and the U11 dimension matrix.
- §4 L Governance model.
- §5 Governance campaign: inventory, evidence and exit.
- §5 Phase D per-family map.
- §6 U11 row and U11 localization, challenge guide and §7.

**Sources at T [static].**

- `EvidenceConformanceEvaluator.evaluate` verifies pairing and the optional subject revision against the subject fingerprint, then applies the scope shortcut. After that it applies the external-evidence kind check and `validateUses`. That method checks exact definition objects, the non-comparator target fingerprint against the subject fingerprint, and each present use revision against the use's own fingerprint.
- `EvaluationOccurrenceDraft.validateUses` checks occurrence ID, definitions, collection placement and the non-comparator fingerprint.
- `InMemoryEvaluationOccurrenceAuthority.verifyUseRevisions` resolves each present use revision against the use's own fingerprint, for relied-on and excluded uses alike.
- **None compares a use's target revision ID with the subject or occurrence revision.**
- `EvidenceUse` rejects RELIED_ON without adequate applicability.

A's control-flow description is exact. Its earlier overbroad phrase "malformed exact-definition/target/revision use refuses" is gone. A search for `target/revision` finds 0 occurrences in A against 2 in Q.

**Specification [static].**

- governance-evidence §4: each use has "its own independent subject/revision/claim/time/applicability determination. Equal fingerprints or a rollback never copy one use's applicability or acceptance to another target"; and "A use may not synthesize an accepted controlled revision for a candidate that has none."
- §6: wrong-subject evidence "never silently produces `PASS`", and falsely bound input "may be rejected at the boundary".
- §8: the occurrence basis fixes "the subject fingerprint and optional verified revision; the relied-on uses".
- §10: use targets are point identities.

These sentences make the cross-revision PASS a **plausible contradiction**, not a settled defect. §6's "may be rejected" leaves the refuse-versus-inadequate-basis choice open. §4 constrains *who determined* applicability, which code cannot observe from a reason string. A's classification ("confirmed observed behavior; plausible contract contradiction; owner decision required") is correct. It does not equate equal fingerprints with equal revisions, and it does not present the rollback auto-copy case as a guard to delete. Both §3 G-EVIDENCE and §4 M say so explicitly.

**Independent probe [S2-probe].** These results extend W's cells. All use a relied-on, non-comparator use with target fingerprint F1, lineage R1(F1)→R2(F2)→R3(F1), and a supplied PASS:

| Subject | Use target revision | Evaluate | Occurrence authority |
| --- | --- | --- | --- |
| F1/R3 | absent | PASS | accepted |
| F1/R3 | R3 (equal) | PASS | accepted |
| F1/R3 | R1 (same fingerprint, different) | PASS | accepted, R1 target retained |
| revisionless F1 | absent | PASS | accepted |
| revisionless F1 | R1 (present) | PASS | **accepted, subject revision none, use target R1** (not tested by W) |
| F1/R3 | excluded-only R1 | UNKNOWN | — |
| F1/R3 | comparator-only F2/R2 | UNKNOWN | — |
| F1/R3 | relied-on F1 bound to R2(F2) | refused: revision bound to a different fingerprint | — |
| F1/R3 | relied-on R3 + excluded R1 | PASS | accepted, excluded R1-targeted use retained |

Every row agrees with A's description of current control flow. The new revisionless-subject acceptance cell is consistent with A's "never synthesize a subject revision" boundary: the accepted occurrence carries no subject revision. A cites only evaluator PASS for that cell, though (S2-3).

**Matrix discrimination.** A's U11 table separates target revision absent, equal, same-fingerprint-different and present-on-revisionless. It also covers absent on revisionless. Each cell is crossed with role, relied-on versus excluded, adequate versus not-established applicability, source-intrinsic provenance versus use-target binding, subject shape and exact definitions. A preserves the revisionless-use precedent: retained `GovernanceEvidenceTest.evidenceFailureFindingContainsOnlyImpactedRequirementEntities` [retained] evaluates a revisionless use for a revision-bound subject and expects FAIL. Every expected outcome is deferred to the owner except source-pinned behavior: fingerprint-binding refusal and comparator/excluded → UNKNOWN. A asserts neither "refuse" nor "PASS" nor "reusable by fingerprint" as the oracle.

**Gating.** Governance slices 1–2 cannot close the use/evaluator/occurrence target-binding family until the owner rule and an oracle derived from that decision exist. Phase D cannot claim proven rollback non-transfer. Accepted-revision, Storage and other Governance work is not blanket-blocked. U4's cross-authority side effect stays separate, and no global rollback transaction is invented. U11 sits in existing G-* groups, not a 34th group.

**Alternatives.** A gives three alternatives:

- exact match for a present target revision;
- barring a present revision on a revisionless subject;
- fingerprint-level targeting with separately established per-target applicability.

It also gives their consequences and requires mixed policies to state both cases. For the fingerprint-level option, A correctly requires naming the surviving §4 obligation and its proof. **Limit (S2-2):** A does not say that exact match leaves the retained revisionless-use precedent as fingerprint-level targeting. Exact match alone therefore does not make §4 non-transfer verifiable for revisionless-target uses.

**What remains:** [owner] the U11 rule; known remediation only after that decision; [future] a decision-derived truth table and occurrence ledger.

### 3.3 RR-3 — ACCEPTED WITH NARROW LIMIT

**Passages inspected (A):** the §4 sensitivity table, the baseline/corrected/one-mutant paragraph, §4 M, §5 shared rule 1, the research campaign and §7.

**Independent checks [S2-probe, single faults on a T copy and on a corrected copy].**

- *Include lower endpoint.* On T this fails on A's f=5 gap [2,3] (2,5] → [2,3] and (3,5] → [3,3], as A states. On the corrected copy it fails on those rows plus f=MAX gap [1,1] (1,MAX]. That last row is correctly labelled post-remediation only, because T already returns [2..MAX] there.
- *Ignore declared gaps.* This fails on f=5 gap [2,3] (0,5] and (2,5], both at T and after correction. On f=MAX gap [MAX,MAX] (MAX-1,MAX], the mutant on T returns exactly {MAX} while T itself returns a duplicate. The mutant **improves** raw agreement and is equivalent on membership, which confirms W's masking observation and A's post-remediation label. After correction, the mutant fails there, returning ∅.
- *Omit post-frontier.* This fails at f=MAX−1 (MAX-1,MAX] on T and after correction. A calls that row a genuine ordinary control, and it is.
- *Reintroduced final+1 / after+1 after correction.* These equal the after-only and final-only partial fixes above. Each is detected only by its own site's rows: f=MAX (0,MAX] and f=0/5 (MAX,MAX] respectively. That confirms the post-remediation controls are independent.

A classifies each item correctly:

- U10a and U10b as existing baseline discrepancies, not mutants;
- the final-only fix as a hypothetical negative control;
- the MAX rows as post-remediation sensitivity controls;
- the f=5/[2,3] and f=MAX−1 rows as ordinary controls.

A also rules that baseline failures, compile errors, discovery failures, setup failures, equivalence and timeouts never count as kills. **Limit (S2-4):** one sentence of the execution order can be read as holding even the ordinary controls back for an owner-authorized correction.

### 3.4 RR-4 — ACCEPTED

**Sources at T [static].** `DeclaredEvidence.completeEvents` updates `eventsRead` only when `through > after`, and only after the read has succeeded. The update is `from = min(prev.from, after+1)` and `through = max(prev.through, through)`. `EvidenceSupport` documents "the smallest interval … covering every event range that was read".

**Counterexample [math, from source].** Take f=7, declared gap [3,4], and reads (0,2] and (5,7]:

- `missingWithin(0,2)` returns ∅. The gap starts at 3, and the beyond-frontier range starts at 8.
- `missingWithin(5,7)` returns ∅. The gap clipped to `from`=6 is empty.
- Both reads succeed, and the hull becomes [1,2] and then [1,7], although [3,4] is unretained.

Further properties, all from source:

- An empty read (a=b) adds no support.
- A refused read (Optional.empty) adds none.
- For any successful nonempty read, b ≤ f, so the hull bound is the requested (a,b] bound. Comparing it with the min and max of actual successful nonempty read intervals is therefore well-defined.

A's R-CAPTURE and R-DERIVE rows, §4 counterexample, research campaign evidence list, challenge guide and Phase D map all treat the hull as a support *record*. They require each read's own basis, inputs, frontier and gaps, and state that the hull never establishes continuity, sufficiency, event sourcing, retention or a coverage guarantee. No passage uses the hull as a contiguous trace or proof frontier.

### 3.5 RR-5 — ACCEPTED

**Passages inspected (A):** Phase D "Entry and custody" (family-level authorship, execution, selection, mutation-triage and adjudication record), "Per-family entry map", "Independent decision challenge (RR-5)", "Exit and retention", §5 shared rule 1, the challenge guide and §7.

**Adversarial scenario.** The challenger executed a B campaign, triaged its survivors, and now reviews that family and declares its own survivors equivalent. A blocks this at four points:

1. **Disclosure** is mandatory, per family, before adjudication. It explicitly covers "execution, selection, mutation triage or outcome adjudication".
2. **Self-judged evidence** "must go to a separately independent challenger". The rule "must not adjudicate a family whose evidence they authored" is stated as non-waivable: exceptions "cannot waive the evidence-author exclusion".
3. **Reassignment** to a second independent challenger is required, with a named responsibility map and a combined verdict. An uncovered or conflicted family cannot inherit another reviewer's verdict, and an unassigned or conflicted family blocks that claim.
4. **Labels** do not establish independence: "branch/worktree/agent label/model family alone does not establish independence".

The final immutable record must carry the independence declaration, disclosures, reassignments and per-family adjudicator. Exit requires independent re-review of RR-5 assignment conflicts.

**Secondary scenario (drift, oracle change, lost generator input).** The "Drift reconciliation" and "Claim draft" paragraphs are **byte-identical to Q**. "Lost inputs or uncertain dependencies block transfer" survives. "Entry and custody" is condensed but retains every manifest element and adds authorship. "Exit and retention" moves the REWORK/missing-review block from the whole verdict to per-claim, which is consistent with per-family verdicts. "Missing independence remains a blocker even with green code reviews" keeps Q's stop rule. **No regression.** A authorizes no reviewer infrastructure, gate or certification.

## 4. New findings

All four are **NOTE** severity. None changes the verdict. Each correction is a sentence in the owner-decision packet or slice commission, not a packet revision.

**S2-1 — NOTE: a third MAX successor site in runtime-produced window construction.**

- **Site [static]:** `ExperimentRunner.missingSequences` sets `next = event.sequence() + 1` and then appends `SequenceRange(next, runFinalSequence)` if `next <= runFinalSequence`. If a retained event has sequence MAX, `next` wraps to MIN. The test `MIN <= f` passes and `SequenceRange(MIN, f)` throws. A real capture whose last retained event is MAX cannot even build its `EvidenceWindow`.
- **Reachability:** this needs a runtime-emitted MAX event, so it belongs to the U3 producer-horizon domain, not U10's caller-query domain.
- **Why the packet misses it:** A requires U3 "last-cursor consumer validation" in general terms, but names only the two helper sites. The research campaign's "both overflow sites" could be read as exhaustive.
- **Smallest correction:** when U3/U10 owner packets are drafted, list this as a U3 last-cursor consumer. Do not fold it into U10.
- **Blocks:** nothing beyond the existing U3 extreme-capture gate.

**S2-2 — NOTE: the exact-match U11 alternative does not by itself make rollback non-transfer verifiable.**

- **Problem [static + math]:** the retained precedent (revisionless use on a revision-bound subject) is fingerprint-level targeting by construction. Under "present target revision must equal subject revision", an R3 occurrence may still rely on a revisionless F1 use whose applicability was actually determined in an R1 context, and nothing distinguishes the two.
- **Current packet text:** A says exact match "preserves absent-target precedent unless explicitly replaced". That is true, but it leaves this consequence for the owner to infer.
- **Smallest correction:** the U11 owner packet should state that every alternative which keeps the revisionless-use precedent must still specify what §4 non-transfer obligation applies to revisionless-target uses and how it is proven, exactly as A already demands for the fingerprint-level option.
- **Blocks:** only the U11 family, which is already gated.

**S2-3 — NOTE: the U11 present-revision/revisionless-subject cell also passes occurrence acceptance.**

- **Observation [S2-probe]:** `InMemoryEvaluationOccurrenceAuthority.accept` accepts a revisionless-subject occurrence relying on an R1-targeted use. The subject revision stays none and the use target R1 is retained.
- **Current packet text:** A's matrix row for that cell cites only W's evaluator PASS.
- **Smallest correction:** add the authority observation to the U11 owner packet and the Governance slice 2 ledger.

**S2-4 — NOTE: the execution-order wording could stall valid ordinary controls.**

- **Wording:** A §4 says future execution "runs the unmodified baseline, an owner-authorized corrected implementation, then one mutation at a time", and that "a semantic kill requires a corrected passing control".
- **Why that over-constrains:** the probe shows that the ordinary-f controls (f=5/[2,3] and f=MAX−1) pass on unmodified T and kill their single mutants applied to T. A's own table calls them valid "on T".
- **Smallest correction:** in the slice commission, clarify that a mutant applied to T is a valid kill on any row where T already matches the independent oracle. An owner-authorized correction is required only for the post-remediation rows.

**Hypotheses examined and dismissed:**

- *The oracle depends on gap normalization.* Membership is idempotent.
- *The hull `after+1` in `completeEvents` wraps.* It is guarded by `through > after`.
- *`SequenceRange.size()` overflows.* No: `from ≥ 1`, so the largest value is [1,MAX] with size MAX.
- *A silently dropped Q control.* Q's controls (1)–(5) all map to A rows with corrected preconditions.
- *A's Exit rewording weakened the missing-review stop.* It did not; see §3.5.

## 5. Reconciliation with the earlier acceptance commission

The commission at `eb3d310c…` exists at the stated coordinates. W did not follow it. W reports using the earlier independent-review mandate and states incorrectly that no separate handoff existed. W also uses a different verdict vocabulary ("DESIGN ADEQUATE; BLOCKERS EXPLICIT"). This addendum follows **the current Step 2 commission**, applies the relevant parts of the earlier one, and claims no retroactive compliance for W. W's substantive findings stand.

| Earlier acceptance criterion | Covered by | Status |
| --- | --- | --- |
| Pass A: blobs, 300 paths, nine modules, 33 IDs | W at Q; this addendum at A | covered |
| Pass A: stale-terminology search (`suspected`, `before scope`, `baseline complete`, `Phase B ready`) | this addendum at A: every remaining hit is legitimate, and `target/revision`, `final+1 wraps` and "extension of U10" are gone | covered |
| Pass A: change isolation and propagation | this addendum (§1, Q→A row identity) | covered |
| Pass B A1 falsification (frontier stop, successes only, promoted family) | W judged adequate. This addendum re-ran the scenario against A: the manifest must record unsuccessful runs and frontiers, truncated exploration never passes, lost inputs block, the challenger attacks frontiers | covered |
| Pass B A2 cursor oracle derivation and mutant attack | W; this addendum §3.1/§3.3 with executed probes | covered |
| Pass B A3 U1–U10 recheck | W statically for all entries; U5/U6 "not re-examined beyond V". This addendum statically confirmed U6's integer-vs-fraction claim at T: `Json.parseNumber` yields Long/BigInteger for integer tokens and Double otherwise, and loader `requireInt`, `requireLong` and `quantityLimit` reject Double. U5 rests on V's probe and Q's replication plus W's static agreement | covered, U5 by prior evidence only |
| Pass B G-CONFORMANCE truth tables | W; this addendum re-read the order in `evaluate` | covered |
| Pass C falsify each critical group's design | V at R (design level); W for the 9 Q-changed rows; this addendum for the 6 A-changed rows. The other 21 rows are byte-identical between R, Q and A and have only V's design-level attack | **named limit, not wholly untested** |
| Verdict vocabulary | W non-conforming; this addendum uses the Step 2 vocabulary | reconciled here |

No essential earlier acceptance assertion is wholly untested. The only residual is that 21 unchanged matrix rows have had a single independent design-level attack (V). That is a breadth limit, not grounds for **REVIEW INCOMPLETE**. The Step 2 commission explicitly scopes out repeating the 33-group audit absent a concrete reason, and none was found.

## 6. Owner next-action map

This review authorizes nothing. Advisory order:

1. **U11** — Governance evidence-use rule. This decision packet should include S2-2 and S2-3. It gates Governance slices 1–2 use binding.
2. **U8** — catalogue text admission versus exact encoding. Highest-consequence confirmed alias. It gates Challenge exact binding.
3. **U10 + U3** — as two linked but separate packets: helper query domain and both successor fixes; producer horizon and exhaustion post-state, including S2-1's runner consumer.
4. **U2** — validator diagnostic order. **U1** — signed subtraction. **U5** and **U7** — constructed-comparison and occupancy result domains.

Slices that could be commissioned independently, each with its residuals explicit:

- values/codec/assembly;
- ordinary finite Scheduler and dispatch models;
- Storage injection/reopen;
- Challenge arithmetic/admission/evaluation;
- finite research interval/pool oracles and architecture controls, using A's ordinary-f controls (with S2-4's clarification).

None may close an affected family above.

## 7. Continuity verdict

Once this single report is committed and pushed, every unique finding, the probe sources and their literal outputs are in committed custody (appendix), and the session is safe to delete. The scratch build outputs are reproducible from the appendix commands and are not needed. Owner decisions and Phase B commissioning remain outstanding work, not uncaptured knowledge. This branch is transient review custody: it must not merge, and no branch should be deleted.

## Appendix: reproduction

From any directory, in PowerShell, with JDK 21 on PATH, the commands below compile a `git archive` of T into a scratch directory `$d`. The worktree's `product/` is untouched. Save the two probe sources below as `$d\probe\S2CursorProbe.java` and `$d\probe\S2UseTargetProbe.java` before compiling them.

```powershell
$T = 'da70c929be95b98aba773a9225ec70ba13d50c31'; $d = '<scratch dir>'
New-Item -ItemType Directory -Force "$d\src","$d\classes","$d\probe" | Out-Null
git archive --format=tar -o "$d\t.tar" $T product; tar -xf "$d\t.tar" -C "$d\src"
git ls-tree -r --name-only $T product | Where-Object { $_ -match '/src/main/.*\.java$' } |
  ForEach-Object { "$d\src\$_" } | Set-Content "$d\main-sources.txt"   # 188 paths
javac --release 21 -nowarn -d "$d\classes" "@$d\main-sources.txt"
javac --release 21 -cp "$d\classes" -d "$d\classes" "$d\probe\S2CursorProbe.java" "$d\probe\S2UseTargetProbe.java"
java -cp "$d\classes" S2CursorProbe
java -cp "$d\classes" S2UseTargetProbe
```

Executed with Temurin/OpenJDK 21.0.9 on Windows 10 on 2026-10-24. `RunId`, revision and occurrence UUIDs are incidental.

### `S2CursorProbe.java`

```java
import com.arcogine.research.experiment.EvidenceWindow;
import com.arcogine.research.experiment.ExperimentFixture;
import com.arcogine.research.experiment.SequenceRange;
import com.arcogine.types.RunId;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Step 2 independent cursor probe. Expected sets come from an endpoint oracle written for this
 * review (open/closed pairs, no successor sentinel) and are cross-checked by brute-force membership
 * on small values. Observed sets come from the real EvidenceWindow at T and from isolated copies of
 * candidate partial fixes and mutants. Nothing here touches product code.
 */
public class S2CursorProbe {
    static final long MAX = Long.MAX_VALUE;

    // ---------- independent oracle ----------
    /** Missing set of (a,b] as normalized inclusive ranges, never computing x+1 unless x < high <= MAX. */
    static List<long[]> oracle(long f, List<long[]> gaps, long a, long b) {
        List<long[]> pairs = new ArrayList<>(); // open/closed (lo, hi]
        for (long[] g : gaps) {
            long lo = Math.max(a, g[0] - 1); // g[0] >= 1, so g[0]-1 >= 0
            long hi = Math.min(b, g[1]);
            if (lo < hi) pairs.add(new long[] {lo, hi});
        }
        long lo = Math.max(a, f);
        if (lo < b) pairs.add(new long[] {lo, b});
        List<long[]> inclusive = new ArrayList<>();
        for (long[] p : pairs) inclusive.add(new long[] {p[0] + 1, p[1]}); // safe: p[0] < p[1] <= MAX
        return normalize(inclusive);
    }

    /** Union of inclusive ranges; merge adjacency via BigInteger to avoid hi+1. */
    static List<long[]> normalize(List<long[]> ranges) {
        TreeMap<Long, Long> byLow = new TreeMap<>();
        for (long[] r : ranges) byLow.merge(r[0], r[1], Math::max);
        List<long[]> out = new ArrayList<>();
        for (var e : byLow.entrySet()) {
            long lo = e.getKey(), hi = e.getValue();
            if (!out.isEmpty()) {
                long[] last = out.get(out.size() - 1);
                if (BigInteger.valueOf(lo).compareTo(BigInteger.valueOf(last[1]).add(BigInteger.ONE)) <= 0) {
                    last[1] = Math.max(last[1], hi);
                    continue;
                }
            }
            out.add(new long[] {lo, hi});
        }
        return out;
    }

    static boolean bruteMember(long f, List<long[]> gaps, long a, long b, long s) {
        if (!(a < s && s <= b)) return false;
        if (s > f) return true;
        for (long[] g : gaps) if (g[0] <= s && s <= g[1]) return true;
        return false;
    }

    // ---------- implementations under observation ----------
    interface Impl { List<long[]> run(long f, List<long[]> gaps, long a, long b); }

    static List<long[]> real(long f, List<long[]> gaps, long a, long b) {
        long start, end;
        if (f == 0) { start = 0; end = 0; } else { start = 1; end = f; }
        // keep window bounds legal when the last sequence is a gap
        if (!gaps.isEmpty() && gaps.get(gaps.size() - 1)[1] == f && f > 1) end = gaps.get(gaps.size() - 1)[0] - 1;
        List<SequenceRange> g = new ArrayList<>();
        for (long[] x : gaps) g.add(new SequenceRange(x[0], x[1]));
        EvidenceWindow w = new EvidenceWindow(RunId.create(), ExperimentFixture.WindowIntent.COMPLETE_RUN,
                start, end, f, g, List.of());
        List<long[]> out = new ArrayList<>();
        for (SequenceRange r : w.missingWithin(a, b)) out.add(new long[] {r.from(), r.through()});
        return out;
    }

    /** Copy of T's arithmetic with switchable faults/fixes. */
    static Impl variant(boolean guardAfter, boolean guardFinal, boolean includeLower, boolean ignoreGaps,
            boolean omitBeyond) {
        return (f, gaps, a, b) -> {
            if (guardAfter && a == b) return List.of();
            long from = includeLower ? a : a + 1;
            List<long[]> m = new ArrayList<>();
            if (!ignoreGaps) {
                for (long[] g : gaps) {
                    long low = Math.max(g[0], from), high = Math.min(g[1], b);
                    if (low <= high) m.add(range(low, high));
                }
            }
            if (!omitBeyond && (!guardFinal || f < b)) {
                long beyondFrom = Math.max(from, f + 1);
                if (beyondFrom <= b) m.add(range(beyondFrom, b));
            }
            return m;
        };
    }

    static long[] range(long lo, long hi) {
        if (lo < 1 || hi < lo) throw new IllegalArgumentException("invalid supported-event sequence range " + lo + ".." + hi);
        return new long[] {lo, hi};
    }

    // ---------- classification ----------
    static String classify(List<long[]> expected, Impl impl, long f, List<long[]> gaps, long a, long b) {
        List<long[]> raw;
        try {
            raw = impl.run(f, gaps, a, b);
        } catch (RuntimeException ex) {
            return "EXCEPTION(" + ex.getClass().getSimpleName() + ")";
        }
        List<long[]> norm = normalize(raw);
        boolean same = equal(norm, expected);
        long rawSum = 0; boolean overlap = false;
        BigInteger sum = BigInteger.ZERO;
        for (long[] r : raw) sum = sum.add(BigInteger.valueOf(r[1]).subtract(BigInteger.valueOf(r[0])).add(BigInteger.ONE));
        BigInteger normSum = BigInteger.ZERO;
        for (long[] r : norm) normSum = normSum.add(BigInteger.valueOf(r[1]).subtract(BigInteger.valueOf(r[0])).add(BigInteger.ONE));
        overlap = sum.compareTo(normSum) > 0;
        String presence = (raw.isEmpty() == expected.isEmpty()) ? "" : (raw.isEmpty() ? "+FALSE-EMPTY" : "+FALSE-PRESENT");
        if (same) return overlap ? "MEMBERSHIP-OK+DUPLICATE" + show(raw) : "EXACT";
        return "WRONG" + presence + show(raw);
    }

    static boolean equal(List<long[]> x, List<long[]> y) {
        if (x.size() != y.size()) return false;
        for (int i = 0; i < x.size(); i++) if (x.get(i)[0] != y.get(i)[0] || x.get(i)[1] != y.get(i)[1]) return false;
        return true;
    }

    static String n(long v) {
        if (v == MAX) return "MAX";
        if (v == MAX - 1) return "MAX-1";
        if (v == Long.MIN_VALUE) return "MIN";
        return Long.toString(v);
    }

    static String show(List<long[]> rs) {
        StringBuilder sb = new StringBuilder("[");
        for (long[] r : rs) sb.append(sb.length() > 1 ? "," : "").append(n(r[0])).append("..").append(n(r[1]));
        return sb.append("]").toString();
    }

    static List<long[]> gs(long... ends) {
        List<long[]> out = new ArrayList<>();
        for (int i = 0; i < ends.length; i += 2) out.add(new long[] {ends[i], ends[i + 1]});
        return out;
    }

    record Row(String label, long f, List<long[]> gaps, long a, long b) {}

    public static void main(String[] args) {
        // 1. oracle self-check against brute membership on small exhaustive domain
        long checks = 0;
        for (long f = 0; f <= 7; f++) {
            for (int mask = 0; mask < (1 << f); mask++) {
                List<long[]> gaps = new ArrayList<>();
                for (int s = 1; s <= f; s++) if ((mask & (1 << (s - 1))) != 0) gaps.add(new long[] {s, s});
                for (long a = 0; a <= 9; a++) for (long b = a; b <= 9; b++) {
                    List<long[]> exp = oracle(f, gaps, a, b);
                    for (long s = 0; s <= 10; s++) {
                        boolean inOracle = false;
                        for (long[] r : exp) if (r[0] <= s && s <= r[1]) inOracle = true;
                        if (inOracle != bruteMember(f, gaps, a, b, s)) throw new AssertionError("oracle mismatch");
                        checks++;
                    }
                }
            }
        }
        // overlapping / beyond-frontier gaps (legal-shaped, not consistent windows)
        for (long a = 0; a <= 9; a++) for (long b = a; b <= 9; b++) {
            List<long[]> gaps = gs(2, 4, 3, 6);
            List<long[]> exp = oracle(5, gaps, a, b);
            for (long s = 0; s <= 10; s++) {
                boolean in = false;
                for (long[] r : exp) if (r[0] <= s && s <= r[1]) in = true;
                if (in != bruteMember(5, gaps, a, b, s)) throw new AssertionError("oracle mismatch (overlap)");
                checks++;
            }
        }
        System.out.println("oracle-vs-brute membership checks: " + checks + " all agree");

        // 2. rows: packet A explicit rows + handoff-named rows
        List<Row> rows = new ArrayList<>();
        rows.add(new Row("A1 f=0 none (MAX,MAX]", 0, gs(), MAX, MAX));
        rows.add(new Row("A2 f=5 none (MAX,MAX]", 5, gs(), MAX, MAX));
        rows.add(new Row("A3 f=5 [2,3] (MAX,MAX]", 5, gs(2, 3), MAX, MAX));
        rows.add(new Row("A4 f=MAX none (MAX,MAX]", MAX, gs(), MAX, MAX));
        rows.add(new Row("A5 f=MAX none (0,MAX]", MAX, gs(), 0, MAX));
        rows.add(new Row("A6 f=MAX none (MAX-1,MAX]", MAX, gs(), MAX - 1, MAX));
        rows.add(new Row("A7 f=MAX [MAX,MAX] (MAX-1,MAX]", MAX, gs(MAX, MAX), MAX - 1, MAX));
        rows.add(new Row("A8 f=MAX [1,1] (0,1]", MAX, gs(1, 1), 0, 1));
        rows.add(new Row("A9 f=MAX [1,1] (1,MAX]", MAX, gs(1, 1), 1, MAX));
        rows.add(new Row("A10 f=MAX-1 none (MAX-1,MAX]", MAX - 1, gs(), MAX - 1, MAX));
        rows.add(new Row("A11 f=1 none (0,1]", 1, gs(), 0, 1));
        rows.add(new Row("A12 f=1 none (1,1]", 1, gs(), 1, 1));
        rows.add(new Row("A13 f=1 none (1,MAX]", 1, gs(), 1, MAX));
        rows.add(new Row("A14 f=0 none (0,0]", 0, gs(), 0, 0));
        rows.add(new Row("A15 f=0 none (0,1]", 0, gs(), 0, 1));
        rows.add(new Row("A16 f=5 [2,3] (0,5]", 5, gs(2, 3), 0, 5));
        rows.add(new Row("A17 f=5 [2,3] (2,5]", 5, gs(2, 3), 2, 5));
        rows.add(new Row("A18 f=5 [2,3] (3,5]", 5, gs(2, 3), 3, 5));

        Impl t = S2CursorProbe::real;
        Impl corrected = variant(true, true, false, false, false);
        String[] names = {"T(real)", "finalOnlyFix", "afterOnlyFix", "corrected",
                "T+includeLower", "T+ignoreGaps", "T+omitBeyond",
                "fix+includeLower", "fix+ignoreGaps", "fix+omitBeyond"};
        Impl[] impls = {t, variant(false, true, false, false, false), variant(true, false, false, false, false), corrected,
                variant(false, false, true, false, false), variant(false, false, false, true, false), variant(false, false, false, false, true),
                variant(true, true, true, false, false), variant(true, true, false, true, false), variant(true, true, false, false, true)};
        System.out.println();
        System.out.println("== explicit rows: expected | T(real) observed");
        for (Row r : rows) {
            List<long[]> exp = oracle(r.f, r.gaps, r.a, r.b);
            System.out.println(r.label + " expected=" + show(exp) + " | T=" + classify(exp, t, r.f, r.gaps, r.a, r.b));
        }

        // 3. joint grid f x (a<=b) from {0,1,MAX-1,MAX}, gapless, plus gap [2,3] where f>=5
        long[] fs = {0, 1, 5, MAX - 1, MAX};
        long[] ends = {0, 1, MAX - 1, MAX};
        List<Row> grid = new ArrayList<>();
        for (long f : fs) for (long a : ends) for (long b : ends) if (a <= b) {
            grid.add(new Row("f=" + n(f) + " (" + n(a) + "," + n(b) + "]", f, gs(), a, b));
            if (f >= 5) grid.add(new Row("f=" + n(f) + " gap[2,3] (" + n(a) + "," + n(b) + "]", f, gs(2, 3), a, b));
        }
        System.out.println();
        System.out.println("== joint grid (" + grid.size() + " rows) and explicit rows: rows disagreeing with oracle, per implementation");
        List<Row> all = new ArrayList<>(rows);
        all.addAll(grid);
        for (int i = 0; i < impls.length; i++) {
            List<String> bad = new ArrayList<>();
            List<String> dup = new ArrayList<>();
            for (Row r : all) {
                List<long[]> exp = oracle(r.f, r.gaps, r.a, r.b);
                String c = classify(exp, impls[i], r.f, r.gaps, r.a, r.b);
                if (c.startsWith("MEMBERSHIP-OK+DUPLICATE")) dup.add(r.label);
                else if (!c.equals("EXACT")) bad.add(r.label + " -> " + c);
            }
            System.out.println("-- " + names[i] + ": " + bad.size() + " wrong, " + dup.size() + " duplicate-only");
            List<String> explicitBad = new ArrayList<>();
            for (String s : bad) if (s.startsWith("A")) explicitBad.add(s);
            for (String s : explicitBad) System.out.println("     explicit " + s);
            if (bad.size() > explicitBad.size()) System.out.println("     + " + (bad.size() - explicitBad.size()) + " grid rows, e.g. " + bad.get(explicitBad.size()));
            for (String s : dup) System.out.println("     duplicate-only " + s);
        }
    }
}
```

Observed output at T:

```text
oracle-vs-brute membership checks: 154880 all agree

== explicit rows: expected | T(real) observed
A1 f=0 none (MAX,MAX] expected=[] | T=WRONG+FALSE-PRESENT[1..MAX]
A2 f=5 none (MAX,MAX] expected=[] | T=WRONG+FALSE-PRESENT[6..MAX]
A3 f=5 [2,3] (MAX,MAX] expected=[] | T=WRONG+FALSE-PRESENT[2..3,6..MAX]
A4 f=MAX none (MAX,MAX] expected=[] | T=EXCEPTION(IllegalArgumentException)
A5 f=MAX none (0,MAX] expected=[] | T=WRONG+FALSE-PRESENT[1..MAX]
A6 f=MAX none (MAX-1,MAX] expected=[] | T=WRONG+FALSE-PRESENT[MAX..MAX]
A7 f=MAX [MAX,MAX] (MAX-1,MAX] expected=[MAX..MAX] | T=MEMBERSHIP-OK+DUPLICATE[MAX..MAX,MAX..MAX]
A8 f=MAX [1,1] (0,1] expected=[1..1] | T=MEMBERSHIP-OK+DUPLICATE[1..1,1..1]
A9 f=MAX [1,1] (1,MAX] expected=[] | T=WRONG+FALSE-PRESENT[2..MAX]
A10 f=MAX-1 none (MAX-1,MAX] expected=[MAX..MAX] | T=EXACT
A11 f=1 none (0,1] expected=[] | T=EXACT
A12 f=1 none (1,1] expected=[] | T=EXACT
A13 f=1 none (1,MAX] expected=[2..MAX] | T=EXACT
A14 f=0 none (0,0] expected=[] | T=EXACT
A15 f=0 none (0,1] expected=[1..1] | T=EXACT
A16 f=5 [2,3] (0,5] expected=[2..3] | T=EXACT
A17 f=5 [2,3] (2,5] expected=[3..3] | T=EXACT
A18 f=5 [2,3] (3,5] expected=[] | T=EXACT

== joint grid (80 rows) and explicit rows: rows disagreeing with oracle, per implementation
-- T(real): 27 wrong, 2 duplicate-only
     explicit A1 f=0 none (MAX,MAX] -> WRONG+FALSE-PRESENT[1..MAX]
     explicit A2 f=5 none (MAX,MAX] -> WRONG+FALSE-PRESENT[6..MAX]
     explicit A3 f=5 [2,3] (MAX,MAX] -> WRONG+FALSE-PRESENT[2..3,6..MAX]
     explicit A4 f=MAX none (MAX,MAX] -> EXCEPTION(IllegalArgumentException)
     explicit A5 f=MAX none (0,MAX] -> WRONG+FALSE-PRESENT[1..MAX]
     explicit A6 f=MAX none (MAX-1,MAX] -> WRONG+FALSE-PRESENT[MAX..MAX]
     explicit A9 f=MAX [1,1] (1,MAX] -> WRONG+FALSE-PRESENT[2..MAX]
     + 20 grid rows, e.g. f=0 (MAX,MAX] -> WRONG+FALSE-PRESENT[1..MAX]
     duplicate-only A7 f=MAX [MAX,MAX] (MAX-1,MAX]
     duplicate-only A8 f=MAX [1,1] (0,1]
-- finalOnlyFix: 10 wrong, 0 duplicate-only
     explicit A1 f=0 none (MAX,MAX] -> WRONG+FALSE-PRESENT[1..MAX]
     explicit A2 f=5 none (MAX,MAX] -> WRONG+FALSE-PRESENT[6..MAX]
     explicit A3 f=5 [2,3] (MAX,MAX] -> WRONG+FALSE-PRESENT[2..3,6..MAX]
     + 7 grid rows, e.g. f=0 (MAX,MAX] -> WRONG+FALSE-PRESENT[1..MAX]
-- afterOnlyFix: 15 wrong, 2 duplicate-only
     explicit A5 f=MAX none (0,MAX] -> WRONG+FALSE-PRESENT[1..MAX]
     explicit A6 f=MAX none (MAX-1,MAX] -> WRONG+FALSE-PRESENT[MAX..MAX]
     explicit A9 f=MAX [1,1] (1,MAX] -> WRONG+FALSE-PRESENT[2..MAX]
     + 12 grid rows, e.g. f=MAX (0,1] -> WRONG+FALSE-PRESENT[1..1]
     duplicate-only A7 f=MAX [MAX,MAX] (MAX-1,MAX]
     duplicate-only A8 f=MAX [1,1] (0,1]
-- corrected: 0 wrong, 0 duplicate-only
-- T+includeLower: 48 wrong, 0 duplicate-only
     explicit A1 f=0 none (MAX,MAX] -> WRONG+FALSE-PRESENT[MAX..MAX]
     explicit A2 f=5 none (MAX,MAX] -> WRONG+FALSE-PRESENT[MAX..MAX]
     explicit A3 f=5 [2,3] (MAX,MAX] -> WRONG+FALSE-PRESENT[MAX..MAX]
     explicit A4 f=MAX none (MAX,MAX] -> WRONG+FALSE-PRESENT[MAX..MAX]
     explicit A5 f=MAX none (0,MAX] -> EXCEPTION(IllegalArgumentException)
     explicit A6 f=MAX none (MAX-1,MAX] -> WRONG+FALSE-PRESENT[MAX-1..MAX]
     explicit A7 f=MAX [MAX,MAX] (MAX-1,MAX] -> WRONG[MAX..MAX,MAX-1..MAX]
     explicit A8 f=MAX [1,1] (0,1] -> EXCEPTION(IllegalArgumentException)
     explicit A9 f=MAX [1,1] (1,MAX] -> WRONG+FALSE-PRESENT[1..1,1..MAX]
     explicit A17 f=5 [2,3] (2,5] -> WRONG[2..3]
     explicit A18 f=5 [2,3] (3,5] -> WRONG+FALSE-PRESENT[3..3]
     + 37 grid rows, e.g. f=0 (1,1] -> WRONG+FALSE-PRESENT[1..1]
-- T+ignoreGaps: 37 wrong, 0 duplicate-only
     explicit A1 f=0 none (MAX,MAX] -> WRONG+FALSE-PRESENT[1..MAX]
     explicit A2 f=5 none (MAX,MAX] -> WRONG+FALSE-PRESENT[6..MAX]
     explicit A3 f=5 [2,3] (MAX,MAX] -> WRONG+FALSE-PRESENT[6..MAX]
     explicit A4 f=MAX none (MAX,MAX] -> EXCEPTION(IllegalArgumentException)
     explicit A5 f=MAX none (0,MAX] -> WRONG+FALSE-PRESENT[1..MAX]
     explicit A6 f=MAX none (MAX-1,MAX] -> WRONG+FALSE-PRESENT[MAX..MAX]
     explicit A9 f=MAX [1,1] (1,MAX] -> WRONG+FALSE-PRESENT[2..MAX]
     explicit A16 f=5 [2,3] (0,5] -> WRONG+FALSE-EMPTY[]
     explicit A17 f=5 [2,3] (2,5] -> WRONG+FALSE-EMPTY[]
     + 28 grid rows, e.g. f=0 (MAX,MAX] -> WRONG+FALSE-PRESENT[1..MAX]
-- T+omitBeyond: 34 wrong, 0 duplicate-only
     explicit A3 f=5 [2,3] (MAX,MAX] -> WRONG+FALSE-PRESENT[2..3]
     explicit A10 f=MAX-1 none (MAX-1,MAX] -> WRONG+FALSE-EMPTY[]
     explicit A13 f=1 none (1,MAX] -> WRONG+FALSE-EMPTY[]
     explicit A15 f=0 none (0,1] -> WRONG+FALSE-EMPTY[]
     + 30 grid rows, e.g. f=0 (0,1] -> WRONG+FALSE-EMPTY[]
-- fix+includeLower: 9 wrong, 0 duplicate-only
     explicit A9 f=MAX [1,1] (1,MAX] -> WRONG+FALSE-PRESENT[1..1]
     explicit A17 f=5 [2,3] (2,5] -> WRONG[2..3]
     explicit A18 f=5 [2,3] (3,5] -> WRONG+FALSE-PRESENT[3..3]
     + 6 grid rows, e.g. f=0 (1,MAX-1] -> WRONG[1..MAX-1]
-- fix+ignoreGaps: 16 wrong, 0 duplicate-only
     explicit A7 f=MAX [MAX,MAX] (MAX-1,MAX] -> WRONG+FALSE-EMPTY[]
     explicit A8 f=MAX [1,1] (0,1] -> WRONG+FALSE-EMPTY[]
     explicit A16 f=5 [2,3] (0,5] -> WRONG+FALSE-EMPTY[]
     explicit A17 f=5 [2,3] (2,5] -> WRONG+FALSE-EMPTY[]
     + 12 grid rows, e.g. f=5 gap[2,3] (0,MAX-1] -> WRONG[6..MAX-1]
-- fix+omitBeyond: 30 wrong, 0 duplicate-only
     explicit A10 f=MAX-1 none (MAX-1,MAX] -> WRONG+FALSE-EMPTY[]
     explicit A13 f=1 none (1,MAX] -> WRONG+FALSE-EMPTY[]
     explicit A15 f=0 none (0,1] -> WRONG+FALSE-EMPTY[]
     + 27 grid rows, e.g. f=0 (0,1] -> WRONG+FALSE-EMPTY[]
```

### `S2UseTargetProbe.java`

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
import java.util.function.Supplier;

/**
 * Step 2 independent U11 probe: crosses use target revision (absent / equal / same-fingerprint
 * different / false binding) with subject shape (R3-bound / revisionless) and role, through the
 * public evaluator and the in-memory occurrence authority at T. Observations only; no expected
 * semantic outcome is asserted.
 */
public class S2UseTargetProbe {
    static ModelFingerprint f1 = new ModelFingerprint("test", "sha256", "a".repeat(64));
    static ModelFingerprint f2 = new ModelFingerprint("test", "sha256", "b".repeat(64));
    static ControlledRevision r1, r2, r3;
    static ControlledRevisionAuthority authority;
    static Requirement requirement = new Requirement(new RequirementId("req"), new RequirementVersion(1), "w", "w",
            ArcogineNativeRequirementSource.of("s2"), RequirementScope.empty());
    static Assertion<?> assertion = new Assertion<>(new AssertionId("as"), new AssertionVersion(1), requirement.id(),
            requirement.version(), "external", EvidenceRequirement.EXTERNAL_EVIDENCE_REQUIRED, null);

    static String attempt(Supplier<String> s) {
        try {
            return s.get();
        } catch (RuntimeException ex) {
            return "REFUSED(" + ex.getClass().getSimpleName() + ": " + ex.getMessage() + ")";
        }
    }

    static EvidenceUse use(EvaluationOccurrenceId occ, int pos, ModelFingerprint fp, ControlledRevision rev,
            EvidenceUseRole role) {
        return new EvidenceUse(occ, pos, new EvidenceReference("source", "record-" + pos, EvidenceProvenance.unknown("s2")),
                fp, rev == null ? null : rev.id(), null, requirement, assertion, role,
                TemporalFrame.atKnowledgeBoundary(Instant.EPOCH),
                new EvidenceApplicability(EvidenceApplicabilityStatus.APPLICABLE, "determined for its own target", "s2 rule"));
    }

    static String name(ControlledRevision r) {
        return r == null ? "none" : r == r1 ? "R1" : r == r2 ? "R2" : "R3";
    }

    public static void main(String[] args) {
        RevisionProvenance prov = new RevisionProvenance(Instant.EPOCH, new RevisionRecorder("test", "s2"));
        r1 = new ControlledRevision(ControlledRevisionId.generate(), f1, List.of(), prov);
        r2 = new ControlledRevision(ControlledRevisionId.generate(), f2, List.of(r1.id()), prov);
        r3 = new ControlledRevision(ControlledRevisionId.generate(), f1, List.of(r2.id()), prov);
        Map<ControlledRevisionId, ControlledRevision> byId = new LinkedHashMap<>();
        for (ControlledRevision r : List.of(r1, r2, r3)) byId.put(r.id(), r);
        authority = new ControlledRevisionAuthority() {
            public ControlledRevision accept(ControlledRevision c, SemanticArtifact a) { throw new UnsupportedOperationException(); }
            public Optional<ControlledRevision> findById(ControlledRevisionId id) { return Optional.ofNullable(byId.get(id)); }
            public HistoricalRevision resolve(ControlledRevisionId id) {
                ControlledRevision r = byId.get(id);
                if (r == null) throw new IllegalArgumentException("unknown " + id);
                return new HistoricalRevision(r, new SemanticArtifact(r.modelFingerprint(), new byte[0]));
            }
            public List<ControlledRevision> revisions() { return List.copyOf(byId.values()); }
        };

        ControlledRevision[] subjects = {r3, null};
        ControlledRevision[] targets = {null, r3, r1};
        System.out.println("relied-on non-comparator use, target fingerprint F1, supplied PASS judgment");
        for (ControlledRevision subject : subjects) {
            for (ControlledRevision target : targets) {
                if (subject == null && target == r3) continue; // same as present-on-revisionless via R1
                EvaluationOccurrenceId occ = EvaluationOccurrenceId.generate();
                EvidenceUse u = use(occ, 0, f1, target, EvidenceUseRole.RELIED_ON);
                String cell = "subject=" + (subject == null ? "F1/revisionless" : "F1/" + name(subject)) + " target=F1/" + name(target);
                final ControlledRevision subj = subject;
                String[] evalHolder = new String[1];
                ConformanceEvaluation[] ev = new ConformanceEvaluation[1];
                String evaluation = attempt(() -> {
                    ev[0] = EvidenceConformanceEvaluator.evaluate(requirement, assertion, Optional.empty(), f1,
                            Optional.ofNullable(subj == null ? null : subj.id()), authority, List.of(u),
                            new EvidenceJudgment(ConformanceResult.PASS, "supported"));
                    return ev[0].result().toString();
                });
                String acceptance = ev[0] == null ? "n/a" : attempt(() -> {
                    EvaluationOccurrence acc = new InMemoryEvaluationOccurrenceAuthority(authority).accept(
                            new EvaluationOccurrenceDraft(occ, requirement, assertion, f1, subj == null ? null : subj.id(),
                                    List.of(u), List.of(), List.of(), TemporalFrame.atKnowledgeBoundary(Instant.EPOCH),
                                    "rules", ev[0], "pass", null));
                    return "ACCEPTED(subjectRevision=" + acc.controlledRevisionIdOptional().map(id -> id.equals(r3.id()) ? "R3" : "?").orElse("none")
                            + ", useTarget=" + name(target) + ")";
                });
                System.out.println("  " + cell + " -> evaluate " + evaluation + "; occurrence " + acceptance);
            }
        }

        System.out.println("other roles / bindings for subject F1/R3");
        EvaluationOccurrenceId occ = EvaluationOccurrenceId.generate();
        EvidenceUse excludedR1 = use(occ, 1, f1, r1, EvidenceUseRole.CONSIDERED_BUT_NOT_RELIED_ON);
        EvidenceUse comparatorR2 = use(occ, 2, f2, r2, EvidenceUseRole.COMPARATOR);
        EvidenceUse falseBinding = use(occ, 3, f1, r2, EvidenceUseRole.RELIED_ON);
        EvidenceUse reliedR3 = use(occ, 0, f1, r3, EvidenceUseRole.RELIED_ON);
        System.out.println("  excluded-only use target F1/R1 -> evaluate " + attempt(() -> EvidenceConformanceEvaluator.evaluate(
                requirement, assertion, Optional.empty(), f1, Optional.of(r3.id()), authority, List.of(excludedR1),
                new EvidenceJudgment(ConformanceResult.PASS, "supported")).result().toString()));
        System.out.println("  comparator-only use target F2/R2 -> evaluate " + attempt(() -> EvidenceConformanceEvaluator.evaluate(
                requirement, assertion, Optional.empty(), f1, Optional.of(r3.id()), authority, List.of(comparatorR2),
                new EvidenceJudgment(ConformanceResult.PASS, "supported")).result().toString()));
        System.out.println("  relied-on use F1 bound to R2(F2) -> evaluate " + attempt(() -> EvidenceConformanceEvaluator.evaluate(
                requirement, assertion, Optional.empty(), f1, Optional.of(r3.id()), authority, List.of(falseBinding),
                new EvidenceJudgment(ConformanceResult.PASS, "supported")).result().toString()));
        String mixed = attempt(() -> {
            ConformanceEvaluation e = EvidenceConformanceEvaluator.evaluate(requirement, assertion, Optional.empty(), f1,
                    Optional.of(r3.id()), authority, List.of(reliedR3, excludedR1),
                    new EvidenceJudgment(ConformanceResult.PASS, "supported"));
            EvaluationOccurrence acc = new InMemoryEvaluationOccurrenceAuthority(authority).accept(
                    new EvaluationOccurrenceDraft(occ, requirement, assertion, f1, r3.id(), List.of(reliedR3), List.of(excludedR1),
                            List.of(), TemporalFrame.atKnowledgeBoundary(Instant.EPOCH), "rules", e, "pass", null));
            return e.result() + "; occurrence ACCEPTED with excluded R1-targeted use retained="
                    + acc.consideredButExcludedUses().get(0).targetControlledRevision().equals(r1.id());
        });
        System.out.println("  relied-on F1/R3 + excluded F1/R1 -> evaluate " + mixed);
    }
}
```

Observed output at T:

```text
relied-on non-comparator use, target fingerprint F1, supplied PASS judgment
  subject=F1/R3 target=F1/none -> evaluate PASS; occurrence ACCEPTED(subjectRevision=R3, useTarget=none)
  subject=F1/R3 target=F1/R3 -> evaluate PASS; occurrence ACCEPTED(subjectRevision=R3, useTarget=R3)
  subject=F1/R3 target=F1/R1 -> evaluate PASS; occurrence ACCEPTED(subjectRevision=R3, useTarget=R1)
  subject=F1/revisionless target=F1/none -> evaluate PASS; occurrence ACCEPTED(subjectRevision=none, useTarget=none)
  subject=F1/revisionless target=F1/R1 -> evaluate PASS; occurrence ACCEPTED(subjectRevision=none, useTarget=R1)
other roles / bindings for subject F1/R3
  excluded-only use target F1/R1 -> evaluate UNKNOWN
  comparator-only use target F2/R2 -> evaluate UNKNOWN
  relied-on use F1 bound to R2(F2) -> evaluate REFUSED(IllegalArgumentException: evidence use revision is bound to a different model fingerprint)
  relied-on F1/R3 + excluded F1/R1 -> evaluate PASS; occurrence ACCEPTED with excluded R1-targeted use retained=true
```
