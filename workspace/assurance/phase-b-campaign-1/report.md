# Core reliability Phase B Campaign 1 — evidence report

Values, publication, canonical identity and model comparison (`T-VALUE`, `T-TIME`, `F-PUBLISH`,
`F-CODEC`, `F-ASSEMBLE`, `F-CHANGE`, with `G-CHANGE` as the downstream seam), commissioned by
issue alaiba/arcogine#541. This packet is **branch-transient workspace evidence**: it is not a
merge candidate, it adjudicates nothing about itself, and it is handed to a separate independent
challenge. Issue completion is not assurance-family closure.

## 1. Custody, authorship and conflict disclosure

| Role | Actor |
|---|---|
| Commissioning | Repository owner, issue #541 |
| Experiment and oracle authorship | One Claude Code implementation session (model Opus 5.5), committing under the owner's Git identity |
| Execution | The same session, in the container described in §3 |
| Corpus/evidence selection | The same session |
| Mutation design and triage | The same session |
| Outcome adjudication | **None.** No family below is independently assured. Phase D/independent challenge must exclude this session and, preferably, the Claude model family |

Conflict disclosure. The oracles (Python §4 encoder, Java codec/predicate/edit-ledger oracle) were
written from the normative documents, not from product code, but by the same actor who executes and
triages them. The independent Step 2 design acceptance this campaign relies on was also produced on a
`claude/` branch; the Phase A design and its amendments were produced on a `codex/` branch. A
challenger should therefore be neither this session nor a Claude-family agent if that is available.

Every file in this packet was written in this session. Nothing here edits the immutable Phase A
artifacts (`baf2e2a94750a350cc533663d52c4c147f41d9ba`) or the Step 2 acceptance
(`b9d60d3ba8aa7fdc425fc719061ea34e1ddbefc8`); they were read only.

## 2. Execution baseline and drift

- Execution source, test and specification SHA: **`a0feeffcdec4a932e3ddbb62ce2551cc2540b1e4`**
  (live `main` resolved at execution start on 2026-10-29, equal to the SHA the issue names;
  committed 2026-10-28T21:29:47+02:00). No drift between commissioning and execution start.
- Phase A design baseline: `da70c929be95b98aba773a9225ec70ba13d50c31`.
- All results below come from one scratch tree produced by `git archive` of the execution SHA;
  no result is aggregated across source or classfile baselines. Re-check live `main` before relying
  on any of it: final-main reconciliation is a Phase D duty, not done here.

Contract drift since Phase A that changes Campaign 1 expectations (all landed, all reconciled into the
oracles below):

| Change since Phase A | Effect on Campaign 1 |
|---|---|
| U1 — `SimTime` is a non-negative tick count with exact non-wrapping `plus`, saturating-to-zero `minus` and `canAdd` | Phase A's signed-subtraction premise and its "U1 blocks T-TIME" status are obsolete. T-TIME is tested against the landed meaning only |
| U2 — eligible-machine sets iterate in ascending signed `MachineId` order (`EligibleMachines`) | The validator diagnostic-order family is now decidable for eligible-resource diagnostics; no universal order is imposed on other kinds |
| Sequence-exhaustion, catalogue, attempt-comparison, occupancy and Governance corrections | Outside Campaign 1 except where they touch `SimError` facts (checked in T-VALUE) |

## 3. Environment, tools and provenance

| Item | Value |
|---|---|
| Host | Windows 10 Pro 10.0.19045; Docker Engine 29.9.2 (WSL2 kernel 6.18.54.1) |
| Container image | `gradle:9-jdk21`, digest `sha256:c0ce93e022ea2e705332dabe090019c749356576fc8fe39c38129b2aae9ed68f`, Ubuntu 26.04, 8 CPUs, 7.9 GB |
| JDK | Eclipse Temurin 21.0.12+8 (container); the repository's Java 21 floor |
| Gradle | 9.8.2 through the repository wrapper (`./gradlew`), compiler flags `-Xlint:all -Werror --release 21` from the build |
| Python oracle | CPython 3.14.4, run with `-I -X utf8` |
| .NET digest cross-check | PowerShell 7.6.9 on .NET 10.0.14 (`System.Security.Cryptography.SHA256`) |
| Scratch tree | `git archive a0feeffc…` extracted, `git init` + `git add -A` (index only, no commit) so mutants can be reverted with `git checkout`; experiment sources copied in by `tools/install-experiments.sh`; never the real worktree |

Environment preference: no devcontainer was running; the documented generic Docker workflow was used
(AGENTS.md order 2). Canonical Gradle output stayed in the scratch tree's `product/**/build`.

Commands (from `/repo/product` in the container):

```text
sh ./gradlew --no-daemon :types:test :factory:test --tests 'com.arcogine.factory.model.*' \
    --tests 'com.arcogine.factory.change.*' :storage:test :governance:test          # clean retained baseline
CAMPAIGN1_RESULTS_DIR=/repo/results sh ./gradlew --continue \
    :types:test --tests 'com.arcogine.types.Campaign1*' \
    :factory:test --tests 'com.arcogine.factory.model.Campaign1*' \
    :storage:test --tests 'com.arcogine.governance.change.Campaign1*'                # experiments
sh /packet/tools/run-mutants.sh /repo/mutation-run                                  # baseline + mutants
```

## 4. Inventory reconciliation

`results/inventory-reconciliation.json` (from `tools/reconcile_inventory.py`) compares the Phase A
crosswalk (300 paths, every blob re-verified against `da70c929…`) with the execution tree by blob:

| | Count |
|---|---|
| Phase A paths | 300 |
| Execution paths | **313** (190 main, 122 test, 1 JMH) — matches the issue |
| Unchanged blobs | 257 |
| Changed blobs | 43 |
| Added paths | 13 |
| Removed paths | 0 |

Campaign 1–relevant drift. Changed, with their Phase A contracts: `SimTime` and `SimTimeTest`
(T-TIME), `SimError` and `SimErrorTest` (T-VALUE), `FactoryModelCanonicalForm` (F-CODEC),
`OperationStepDefinition` (F-PUBLISH), `RoutingStep` (E-DISPATCH, F-ASSEMBLE). Added paths have no
Phase A mapping; this packet **proposes**, for challenge, the following Campaign 1 mapping (other
added paths belong to other campaigns and are only listed):

| Added path | Proposed contracts |
|---|---|
| `factory/routing/EligibleMachines.java` (main) | F-PUBLISH, F-CODEC (definition binding), F-ASSEMBLE, E-DISPATCH |
| `factory/model/validation/MissingEligibleResourceOrderTest.java`, `…Probe.java` | F-PUBLISH (U2 diagnostic order) |
| `factory/process/EligibleMachineOrderTest.java` | F-ASSEMBLE, E-DISPATCH, E-OBSERVE |
| `factory/process/TaskEndTimeOverflowTest.java` | T-TIME (Engine outcome), E-ARITH |
| `factory/process/SequenceExhaustionTest.java`, `simulation/…/SchedulerSequenceExhaustionTest.java` | E-SCHEDULE, E-ARITH (Campaign 2) |
| `factory/process/SharedBacklog{AdmissionBenchmark,Equivalence}Test.java` | E-DISPATCH (Campaign 2) |
| `governance/conformance/EvidenceUseRevisionBinding.java`, `…/EvidenceUseRevisionBindingTest.java` | G-EVIDENCE, G-OCCURRENCE (Campaign 3) |
| `research-experiments/…/ExactOccupancyAreaTest.java`, `ExperimentStepTest.java` | R-DERIVE, R-CAPTURE (Campaign 4) |

The 33-contract identifier set is unchanged. The Phase A crosswalk remains the historical record; it
is not rewritten.

## 5. Per-family status ledger

Status vocabulary: `EXECUTED` (observed evidence exists for the family at the execution SHA),
`BLOCKED` (a closure claim depends on an unproven prerequisite, named), `RESIDUAL-EXCLUDED` (outside
practical or commissioned evidence, stated). `EXECUTED` is not "assured".

| Group | Family | Status | Evidence (this packet) | Honest limit |
|---|---|---|---|---|
| T-VALUE | numeric typed ID order/equality/domain separation | EXECUTED | 7 signed boundaries × 7, 4 domains cross-checked | — |
| T-VALUE | `ControlledRevisionId` version/variant/spelling | EXECUTED | 256 version×variant spellings, 128 constructor bit patterns, 18 non-canonical spellings, 1 000 generated IDs | randomness quality / collision freedom RESIDUAL-EXCLUDED |
| T-VALUE | `ModelFingerprint` grammar and equality | EXECUTED | 8 malformed sha256 digests, blank/null fields, namespace/algorithm inequality | rendering `ns:alg:digest` is not injective for a namespace or algorithm containing `:` (discrepancy D5, frontier only) |
| T-VALUE | `SimError` structured facts, `RunId` | EXECUTED | unrepresentable vs earlier-time `EventOrderingViolation`, `SequenceExhausted` | RunId-as-non-decision-input belongs to Campaign 2 |
| T-TIME | value-level U1 meaning | EXECUTED | 12 boundary times × 67 deltas (804 cases) against `BigInteger`; all 144 `minus` and order pairs | — |
| T-TIME | Engine outcome for unrepresentable scheduling time | BLOCKED (closure) | retained `TaskEndTimeOverflowTest` exercised (§9.2); no new Engine corpus here | every dispatch site is E-ARITH/Campaign 2 scope |
| F-PUBLISH | each predicate and admitted boundary neighbour | EXECUTED | 100 single-change cases, 44 refused | — |
| F-PUBLISH | multi-predicate interactions | EXECUTED | full factorial of 12 interacting toggles = 4 096 models | diagnostic granularity (per occurrence) is an oracle assumption the spec does not decide; agreement observed |
| F-PUBLISH | U2 diagnostic order | EXECUTED | 720 authored orders of a signed witness incl. `MIN`/`MAX`; a 112-line witness/compound-invalid diagnostic sequence identical across 8 fresh JVMs | only eligible-resource diagnostics are order-checked; no universal order imposed |
| F-PUBLISH | copy isolation and refusal post-state | EXECUTED | source mutation at all 4 nesting depths, accessor mutation at 5 surfaces, refusal/retry | — |
| F-CODEC | literal bytes and fingerprints | EXECUTED | 3 golden vectors (47/297/481 bytes) from an independent Python encoder, digests agreed by Python, .NET and Java; field-by-field derivation | SHA-256 collision resistance is an assumption, RESIDUAL-EXCLUDED |
| F-CODEC | identity relations | EXECUTED | 120 eligible permutations ⇒ one identity; 20 list-order/field variants ⇒ 21 distinct identities | — |
| F-CODEC | strict decode, canonicality, predicate refusal | EXECUTED | exhaustive single-edit corpus (truncate/substitute/delete/insert) differential against an independent decoder, 0 mismatches; 37 targeted grammar + 10 predicate byte strings with refusal class | two-or-more-edit and allocation-bound frontiers not explored |
| F-CODEC | structured generation | EXECUTED | 3 000 seeded valid models (seed `0xC1A55EED`), independent encoder byte equality and round trip | generator pools are listed in source; not exhaustive |
| F-CODEC | support and definition binding | EXECUTED | support truth table; binding recomputed from the documented algorithm; class-load probe: every product class executed by verification is bound | binding guards one compiled build; historical provenance is not claimable by design |
| F-ASSEMBLE | authored facts through assembly | EXECUTED | 500 seeded models: store order, ID-ordered observation, routings, product IDs; record-component inventory | — |
| F-ASSEMBLE | session isolation, metadata vs execution | EXECUTED | two sessions from one version; capacity/setup-only edit changes identity and change set, not the 4-unit trace | single-product workload only; wider execution equivalence is Campaign 2 |
| F-CHANGE | per-field, membership, order changes | EXECUTED | 4 500 pairs (independent, self, edited) against an independent edit ledger; identity relation and swap relation | detail-text vocabulary is observation, not contract (D4) |
| G-CHANGE | real revision/Storage/Governance seam | EXECUTED (bounded) | `BuiltInStorage`, both `ChangeSetFactory` paths, reopen, refused snapshots, fingerprint vs revision identity | durability/reopen-across-builds closure BLOCKED on the Storage campaign (S-*); U11 out of scope |

## 6. Slice 1 — values, time, publication, copy isolation

Sources: `experiment-sources/types/…/Campaign1ValueTimeExperimentTest.java`,
`experiment-sources/factory/…/Campaign1PublicationExperimentTest.java`, `Campaign1Oracle.java`.

- **Oracles.** `BigInteger` sums/differences for `SimTime`; character-level RFC 9562 reading for
  UUIDs (hex digit 14 = `4`, digit 19 ∈ `8,9,a,b`, 36 lowercase characters); a predicate oracle
  written from factory-model.md §2 over a neutral model; diagnostic text is mapped to oracle kinds by
  an explicit parser that fails on any unrecognised diagnostic. Validator text templates are *observed
  vocabulary*, not specified.
- **U1.** `plus`/`canAdd` agree with the exact sum over the full boundary cross product (including
  `delta = −value`, `−value−1`, `MAX−value`, `MAX−value+1`, `Long.MIN_VALUE`); every refusal is an
  `ArithmeticException` with the exact message; construction refuses exactly negative ticks;
  `minus` is the exact non-negative difference or zero.
- **Predicates.** No positivity is imposed on IDs, setup time or capacity: signed IDs incl. `MIN`/`MAX`,
  setup `MIN…MAX`, capacity `null/±0/±∞/NaN/MIN/MAX` all publish. Ill-formed names are refused in all
  four name surfaces (7 ill-formed and 7 well-formed spellings incl. U+0000, U+FFFF, U+FEFF, U+10FFFF).
  Duplicate step IDs are admitted (no predicate).
- **Interactions.** All 4 096 combinations agree with the oracle multiset, publication outcome,
  exception payload/message and immutability; e.g. a duplicate resource ID also makes the displaced
  resource's references dangle, and a duplicate operation ID also orphans its product.
- **U2.** For every authored order of `{MAX, −1, 0, MIN, 7, −7}` the diagnostics read
  `MIN, −7, −1, 0, 7, MAX`; the 112-line compound sequence (`results/fresh-jvm-diagnostics.txt`) is
  byte-identical in-process and in 8 fresh JVMs.
- **Isolation/post-state.** Mutating source collections after publication changes neither the model,
  its bytes nor its fingerprint; every accessor collection refuses mutation, including the eligible
  set's iterator; a refused publication returns an immutable error list equal to `validate`, leaves
  the source untouched, and a corrected retry succeeds. Construction-time `NullPointerException`s
  precede publication and are not publication predicates.

## 7. Slice 2 — independent canonical bytes, identity and strict codec

Sources: `oracles/canonical_vectors.py`, `experiment-sources/factory/…/Campaign1CodecExperimentTest.java`,
`Campaign1Oracle.java`; results `canonical-vectors.json`, `canonical-vector-derivation.txt`,
`codec-differential-ledger.txt`, `codec-targeted-ledger.txt`, `definition-binding-coverage.txt`.

- **Golden vectors.** `empty` (47 B, `d3ab6440…63d0`), `representative` (297 B, `7457e1ca…aa79`) and
  `signed-extremes` (481 B, `81f6150d…5938`). The Python encoder uses `struct`, Python's own strict
  UTF-8 codec and `hashlib`; digests re-derived with .NET's SHA-256; the Java oracle encodes integers by
  shifts and UTF-8 from code points. Product bytes, fingerprints, `decode`, re-encode,
  `FactoryModelArtifact.fingerprint` and the verifier all agree. `signed-extremes` covers `I64` extremes
  in every ID/integral field, `Integer.MAX_VALUE` concurrency, `−0.0`/`+0.0`/NaN/`±∞`, the empty name,
  U+0000 and delimiters, CJK, U+10FFFF, duplicate step IDs and an unsorted authored eligible set.
- **Identity relations.** One fingerprint for all 120 eligible-set authored orders; a distinct
  fingerprint for every list reorder (resources, operations, steps, products) and for each single
  canonical-field change (21 distinct, incl. NFC-relevant `Load́` and NBSP, proving no
  normalization).
- **Strictness.** The exhaustive single-edit corpus (8 489 inputs over the three vectors; 1 295 accepted, each decoding to the oracle's model) has no product/oracle disagreement on acceptance,
  decoded model, fingerprint-of-accepted-bytes or refusal class (`IllegalArgumentException` for
  grammar/canonicality, `FactoryModelValidationException` for predicates). Targeted refusals include
  discarded `:v1`/`:v2` prefixes, counts `−1`, `2^31`, `Long.MAX`, overrunning counts/lengths,
  concurrency `2^31`, `2^32+1` (which an unchecked `int` cast would turn into a valid 1) and
  `−2^31−1`, markers `02`/`ff`, non-canonical NaN payloads, unsorted and duplicate eligible IDs, and
  thirteen ill-formed UTF-8 spellings (overlong, encoded/CESU surrogates, > U+10FFFF, F5/F8/FE leads, lone
  continuation, truncated sequences).
- **Support and binding.** `supports` is true exactly for `factory-model` + `sha256`. The verifier's
  definition binding equals an independent recomputation of the documented digest over the listed
  classes. A child JVM that runs only verification (valid, predicate-invalid and malformed bytes) loads
  no product class outside the binding list except `SemanticArtifactVerifier` (Governance interface)
  and the probe's own classes. Present canonical validity is distinguished from historical write
  provenance: the raw decoder cannot and does not claim the latter.

## 8. Slice 3 — assembly, semantic comparison and the change seam

Sources: `experiment-sources/factory/…/Campaign1ChangeAssemblyExperimentTest.java`,
`experiment-sources/storage/…/Campaign1ChangeSeamExperimentTest.java`.

- **Assembly.** For 500 seeded models every authored resource fact survives in the machine store in
  list order; `observe()` presents the same facts ordered by ascending ID (D1); every product routes to
  its authored operation with step IDs, names, durations and ascending eligible sets intact. The record
  components of the five canonical records are exactly the specified authored facts, so no scenario,
  presentation or consumer fact can enter identity through them.
- **Isolation and metadata.** Two runtimes from one version are independent (distinct `RunId`, the
  second unchanged by the first's run, equal traces afterwards). Changing only `capacityLiters`/
  `setupTime` changes the fingerprint and yields exactly two `factory.resource` changes but the same
  4-unit event trace, as factory-model.md §2 states.
- **Semantic comparison.** For 4 500 pairs the comparator's `(kind, type, id)` set and per-entity
  aspects equal the independent ledger; no change is reported exactly when canonical bytes are equal;
  swapping base and candidate exchanges additions/removals and preserves modified identities.
  Step-level edits are reported at operation granularity (allowed by the Phase A design).
- **Seam.** With real `BuiltInStorage` and the real comparator: equivalent eligible permutations are one
  fingerprint, two distinct revision IDs and a semantic no-op; historical and snapshot change sets equal
  the direct comparison; a reopened store yields the same historical change set; a snapshot with
  trailing or truncated bytes, a foreign namespace or a wrong digest is refused by both
  `ChangeSetFactory` and Storage `accept` without recording a revision; an unknown revision is refused.

## 9. Mutation and control ledger

### 9.1 Targeted mutants

44 single-fragment mutants (mutation/mutants.txt, applied by 	ools/Mutator.java, run by
	ools/run-mutants.sh). The clean BASELINE run of every selected class (33 suites, retained and
experimental) passed first (mutation/run/BASELINE.result). A kill requires compilation, discovery
and a failing suite; no compile, discovery or timeout outcome occurred, so none is counted.
Selections: 	ypes = :types:test; actory = :factory:test --tests model.\*, change.\*,
EligibleMachineOrderTest, ConfiguredResourceMetadataTest; seam = :storage:test plus
:governance:test --tests change.\*. Per-suite results are in mutation/run/*.result.

| Mutant | Selection | Outcome | Killing retained classes | Killing experiment classes | Seconds |
|---|---|---|---|---|---|
| `T-CTOR-ADMITS-MINUS-ONE` | types | KILLED | SimTimeTest | Campaign1ValueTimeExperimentTest | 9 |
| `T-CANADD-NEG-OFF-BY-ONE` | types | KILLED | SimTimeTest | Campaign1ValueTimeExperimentTest | 9 |
| `T-CANADD-POS-OFF-BY-ONE` | types | KILLED | SimTimeTest | Campaign1ValueTimeExperimentTest | 11 |
| `T-PLUS-UNCHECKED` | types | KILLED | SimTimeTest | Campaign1ValueTimeExperimentTest | 9 |
| `T-MINUS-UNCLAMPED` | types | KILLED | SimTimeTest | Campaign1ValueTimeExperimentTest | 10 |
| `T-CRID-NO-VARIANT` | types | KILLED | ControlledRevisionIdTest | Campaign1ValueTimeExperimentTest | 11 |
| `T-CRID-NO-CANONICAL` | types | KILLED | ControlledRevisionIdTest | Campaign1ValueTimeExperimentTest | 9 |
| `T-FP-CASE-FOLD` | types | KILLED | ModelFingerprintTest | Campaign1ValueTimeExperimentTest | 9 |
| `T-MACHINEID-REVERSED` | types+factory | KILLED | FactoryModelCanonicalFormTest, MissingEligibleResourceOrderTest, EligibleMachineOrderTest, MachineIdTest | Campaign1CodecExperimentTest, Campaign1PublicationExperimentTest, Campaign1ValueTimeExperimentTest | 23 |
| `F-VAL-CONCURRENCY-ZERO` | factory | KILLED | FactoryModelArtifactTest, FactoryModelValidatorTest | Campaign1CodecExperimentTest, Campaign1PublicationExperimentTest | 30 |
| `F-VAL-DURATION-ZERO` | factory | KILLED | FactoryModelValidatorTest | Campaign1CodecExperimentTest, Campaign1PublicationExperimentTest | 30 |
| `F-VAL-NO-DUPLICATE-PRODUCT` | factory | KILLED | FactoryModelValidatorTest | Campaign1PublicationExperimentTest | 30 |
| `F-VAL-LONE-LOW-SURROGATE` | factory | KILLED | FactoryModelCanonicalFormTest | Campaign1PublicationExperimentTest | 30 |
| `F-VAL-HIGH-AT-END` | factory | KILLED | FactoryModelCanonicalFormTest | Campaign1PublicationExperimentTest | 25 |
| `F-VAL-EMPTY-ELIGIBLE-NO-CONTINUE` | factory | SURVIVED | - | - | 32 |
| `F-VAL-DANGLING-POSITIVE-ONLY` | factory | KILLED (experiment only) | - | Campaign1CodecExperimentTest, Campaign1PublicationExperimentTest | 24 |
| `F-VAL-NO-OPERATION-REFERENCE` | factory | KILLED | FactoryModelArtifactTest, FactoryModelDefinitionBindingTest, FactoryModelValidatorTest | Campaign1CodecExperimentTest, Campaign1PublicationExperimentTest | 28 |
| `F-U2-HASH-ORDER` | factory | KILLED | MissingEligibleResourceOrderTest, EligibleMachineOrderTest | Campaign1CodecExperimentTest, Campaign1PublicationExperimentTest | 24 |
| `F-ENC-NO-SORT` | factory | SURVIVED | - | - | 30 |
| `F-ENC-RAW-NAN` | factory | KILLED | FactoryModelCanonicalFormTest | Campaign1CodecExperimentTest | 28 |
| `F-ENC-LONGBITS` | factory | SURVIVED | - | - | 31 |
| `F-ENC-PREFIX` | factory | KILLED | FactoryModelCanonicalFormTest | Campaign1CodecExperimentTest, Campaign1PublicationExperimentTest | 27 |
| `F-ENC-LITTLE-ENDIAN` | factory | KILLED | FactoryModelSemanticComparatorTest, FactoryModelArtifactTest, FactoryModelCanonicalFormTest, FactoryModelDefinitionBindingTest | Campaign1ChangeAssemblyExperimentTest, Campaign1CodecExperimentTest, Campaign1PublicationExperimentTest | 26 |
| `F-DEC-NO-TRAILING-CHECK` | factory | SURVIVED | - | - | 29 |
| `F-DEC-NO-ROUND-TRIP` | factory | KILLED | FactoryModelArtifactTest | Campaign1CodecExperimentTest | 28 |
| `F-DEC-NO-CONCURRENCY-RANGE` | factory | SURVIVED | - | - | 29 |
| `F-DEC-NEGATIVE-COUNT` | factory | KILLED (experiment only) | - | Campaign1CodecExperimentTest | 29 |
| `F-DEC-UTF8-REPLACE` | factory | SURVIVED | - | - | 29 |
| `F-DEC-ANY-MARKER` | factory | SURVIVED | - | - | 29 |
| `F-SUPPORT-ANY-ALGORITHM` | factory | KILLED | FactoryModelSemanticComparatorTest, FactoryModelArtifactTest | Campaign1CodecExperimentTest | 29 |
| `F-BINDING-DROPS-ELIGIBLE` | factory | KILLED | FactoryModelDefinitionBindingTest | Campaign1CodecExperimentTest | 28 |
| `F-VERSION-NO-VALIDATION` | factory | KILLED | FactoryModelArtifactTest, FactoryModelDefinitionBindingTest, FactoryRuntimeAssemblerTest | Campaign1CodecExperimentTest, Campaign1PublicationExperimentTest | 29 |
| `F-MODEL-ALIAS-RESOURCES` | factory | KILLED | FactoryModelImmutabilityTest | Campaign1PublicationExperimentTest | 30 |
| `F-STEP-ALIAS-ELIGIBLE` | factory | KILLED | FactoryModelImmutabilityTest, MissingEligibleResourceOrderTest, EligibleMachineOrderTest | Campaign1ChangeAssemblyExperimentTest, Campaign1CodecExperimentTest, Campaign1PublicationExperimentTest | 24 |
| `F-CMP-NO-SETUP` | factory | KILLED | FactoryModelSemanticComparatorTest | Campaign1ChangeAssemblyExperimentTest | 29 |
| `F-CMP-UNBOXED-CAPACITY` | factory | KILLED (experiment only) | - | Campaign1ChangeAssemblyExperimentTest | 27 |
| `F-CMP-NO-ORDER` | factory | KILLED | FactoryModelSemanticComparatorTest | Campaign1ChangeAssemblyExperimentTest | 27 |
| `F-CMP-IGNORES-STEPS` | factory | KILLED | FactoryModelSemanticComparatorTest | Campaign1ChangeAssemblyExperimentTest | 27 |
| `F-CMP-NO-PRODUCT-OPERATION` | factory | KILLED | FactoryModelSemanticComparatorTest | Campaign1ChangeAssemblyExperimentTest | 28 |
| `F-ASM-DROPS-CAPACITY` | factory | KILLED | ConfiguredResourceMetadataTest | Campaign1ChangeAssemblyExperimentTest | 32 |
| `F-ASM-FIRST-ROUTING` | factory | KILLED (experiment only) | - | Campaign1ChangeAssemblyExperimentTest | 29 |
| `G-SNAPSHOT-UNVERIFIED` | seam | KILLED | ChangeSetFactoryTest | Campaign1ChangeSeamExperimentTest | 17 |
| `G-NO-EXTRACTOR-SUPPORT` | seam | KILLED | ChangeSetFactoryTest | - | 14 |
| `G-SNAPSHOT-BASE-FINGERPRINT` | seam | KILLED | ChangeSetFactoryTest | - | 14 |

Totals: KILLED 33, KILLED (experiment only) 4, SURVIVED 7

Triage of the 7 survivors:

| Mutant | Disposition | Reason |
|---|---|---|
| `F-VAL-EMPTY-ELIGIBLE-NO-CONTINUE` | EQUIVALENT (planned control) | the skipped loop iterates an empty set |
| `F-ENC-NO-SORT` | EQUIVALENT (planned control) | every eligible set is already an ascending `TreeSet` view (U2) |
| `F-ENC-LONGBITS` | EQUIVALENT (planned control) | `doubleToLongBits` canonicalizes NaN to the same `0x7ff8000000000000` |
| `F-DEC-NO-TRAILING-CHECK` | EQUIVALENT at the specified observable | the canonical round trip (§5.2) still refuses; only the exception cause differs |
| `F-DEC-NO-CONCURRENCY-RANGE` | EQUIVALENT at the specified observable | the wrapped `int` re-encodes differently, so the round trip refuses |
| `F-DEC-UTF8-REPLACE` | EQUIVALENT at the specified observable | U+FFFD re-encodes as `EF BF BD`, never the malformed input |
| `F-DEC-ANY-MARKER` | EQUIVALENT at the specified observable | a non-`01` marker re-encodes as `01` |

The last four show that decode strictness at the refusal boundary rests on the canonical round trip;
the individual guards only sharpen the diagnostic cause, which the specification does not define.
`F-DEC-NO-ROUND-TRIP` itself is killed by both retained and experiment tests. `F-DEC-NEGATIVE-COUNT`
is killed only through a message assertion (a negative text length escapes as a raw
`IllegalArgumentException` from `readNBytes` rather than the wrapped refusal) — the refusal class
is unchanged, so it is not a retention candidate.

Experiment-only kills that exposed a retained-test gap for a specified invariant, and the narrow
retained regression assertions proposed for them (§9.3):

| Mutant | Invariant | Retained assertion |
|---|---|---|
| `F-VAL-DANGLING-POSITIVE-ONLY` | resource IDs carry no positivity requirement (factory-model.md §2) | `FactoryModelValidatorTest.reportsDanglingEligibleResourcesWithNonPositiveIds` |
| `F-CMP-UNBOXED-CAPACITY` | ±0 distinct and NaN canonical (factory-model.md §4.2) agree between comparator and identity | `FactoryModelSemanticComparatorTest.capacityChangesFollowCanonicalIdentityForSignedZeroAndNaN` |
| `F-ASM-FIRST-ROUTING` | each product keeps its authored operation through assembly (factory-design.md, F-ASSEMBLE) | `FactoryRuntimeAssemblerTest.routesEveryProductToItsOwnAuthoredOperation` |

The experiments did **not** kill `G-NO-EXTRACTOR-SUPPORT` or `G-SNAPSHOT-BASE-FINGERPRINT`; the
retained `ChangeSetFactoryTest` does. That is a gap in this campaign's seam corpus, recorded rather
than hidden.

### 9.2 SimTime mutants against Engine-outcome tests

`tools/run-time-engine-mutants.sh` ran the five SimTime mutants against retained
`TaskEndTimeOverflowTest`, `SequenceExhaustionTest` and `:simulation:test` (results in
`mutation/time-engine-run`). Baseline passed. Only `T-CANADD-POS-OFF-BY-ONE` is observed by these
classes (`TaskEndTimeOverflowTest`); the other four survive there and are killed by the value-level
classes (§9.1). This characterizes the Engine's reliance on `canAdd` preflight rather than on
`plus`'s own guard; it is not a defect, and Engine-site closure stays with Campaign 2. An earlier
attempt of this run recorded no suites because of a harness glob bug; it was discarded and re-run.

### 9.3 Retained regression assertions

The three assertions above live in their owning test classes on a separate implementation branch and
pull request (not in this packet). With them installed in the scratch tree, the clean baseline passed
and each target mutant is killed by its retained class (`mutation/retained-check`,
`mutation/retained-check-ledger.md`). They are minimal, use only specified behaviour and reference
no transient artifact. Independent review of that pull request is still required.

## 10. Discrepancy register

| ID | Finding | Classification | Follow-up owner |
|---|---|---|---|
| D1 | `observe()` lists resources by ascending ID, not authored order; an early experiment expected list order | test/oracle error (corrected); behaviour is consistent with ID-ordered observations elsewhere in `FactoryRuntime` | none |
| D2 | Early oracle classified a 32-bit concurrency overflow as a publication predicate; factory-model.md §6 item 5 lists it as a decode rejection, matching the product | test/oracle error (corrected) | none |
| D3 | Two hand-built test models were invalid for reasons unrelated to their intent (helper kept a non-null default; an ID change left references dangling) | test/oracle error (corrected) | none |
| D4 | `FactoryModelSemanticComparator` detail text says `step routing/duration changed` for a step-name-only edit or a step reorder, and embeds unescaped names (a name containing `; ` or `: ` would make the text ambiguous) | undecided semantic question — detail text is not specified; impact scope uses `(type, id)` and is correct | Factory/Governance owner, only if detail text is meant to be relied on |
| D5 | `ModelFingerprint.toString()` (`namespace:algorithm:digest`) is not injective when a namespace or algorithm contains `:`; there is no parse operation, and equality is record equality | undecided semantic question / frontier — no current consumer parses the rendering | Types owner, only if a rendering parser is introduced |

No confirmed implementation violation was found for any Campaign 1 family at the execution SHA. No
production code or specification was changed by this campaign, and nothing is submitted for Phase C.
Three retained-test gaps found by mutation (§9.1) are closed by narrow regression assertions in a
separate implementation pull request (§9.3).

## 11. What cannot yet be claimed

- No family is independently assured; this packet awaits a separate challenge (§1).
- Frontiers not explored: two-or-more-edit byte corpora; allocation-bound inputs beyond
  `Integer.MAX_VALUE` counts against short streams; validator behaviour for models larger than the
  generator's pools; diagnostic granularity rules the specification does not decide.
- T-TIME Engine-site closure, all scheduler/dispatch/observation families, Storage durability and
  reopen-under-another-build closure, U11 and every Challenge/research family belong to Campaigns 2–4.
- SHA-256 collision resistance and UUID randomness remain assumptions.
- The seam corpus did not itself detect two Governance mutants (§9.1); retained tests did. Mutation
  covered 44 hand-chosen faults, not a complete fault model.
- The retained regression pull request is not yet independently reviewed or merged; until it is, the
  three gaps of §9.1 remain open on `main`.
- Results are bound to `a0feeffc…`; any later `main` requires the reconciliation the Phase D protocol
  describes. Mutation outcomes (§9) measure sensitivity of the named selections only, not correctness.

## 12. Reproduction

1. `git archive a0feeffcdec4a932e3ddbb62ce2551cc2540b1e4 | tar -x -C <scratch>`; `git -C <scratch> init`;
   `git -C <scratch> add -A`.
2. Start `gradle:9-jdk21` with `<scratch>` at `/repo` and this packet directory at `/packet`.
3. `sh /packet/tools/install-experiments.sh` (prints the SHA-256 of each installed source).
4. Run the experiment and mutation commands of §3; `python -I tools/mutation_ledger.py <run> mutation/mutants.txt`.
5. `python -I -X utf8 oracles/canonical_vectors.py` reproduces `results/canonical-vectors.json`.
