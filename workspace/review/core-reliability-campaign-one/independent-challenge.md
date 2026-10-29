# Independent challenge: Core reliability Phase B Campaign 1

## Decision and custody

**MORE EVIDENCE REQUIRED before treating issue #541's Campaign 1 packet as complete.**
The reproduced, bounded positive results are useful. Three discriminating controls expose missing
observations in the publication, assembly and Governance/Storage experiment families. The byte
exploration and mutation claims also need narrower wording. **No confirmed production violation was
found.** These are evidence findings; they do not commission production remediation or settle new
semantics. No core-wide or Phase D verdict is issued.

Immutable reviewed input:

- Branch: `assurance/core-reliability-phase-b-campaign-1`.
- Report/evidence commit: `f69566784bde479cc86d27d353573d98e7c283f1`.
- Path: `workspace/assurance/phase-b-campaign-1/report.md` and its complete sibling evidence tree.
- Continuation instructions: `workspace/assurance/phase-b-campaign-1/independent-challenge-prompt.md`
  at `36155f9a6ed0dbd707b54deb1c8780aef72b6fd7`.
- Execution source/test/spec baseline: `a0feeffcdec4a932e3ddbb62ce2551cc2540b1e4`.
- Historical design and crosswalk: `baf2e2a94750a350cc533663d52c4c147f41d9ba`;
  Step 2 acceptance: `b9d60d3ba8aa7fdc425fc719061ea34e1ddbefc8`.

The original packet is unchanged. This report and its reproducing sources/results are transient
review evidence on `codex/issue-541-campaign-1-challenge`, not a merge candidate. Its exact published
commit is supplied by the issue locator; no branch tip is offered as immutable identity.

## Independence, authority and limits

The challenger is this Codex / GPT-6 session. It did not author, execute, select or triage the
original Campaign 1 evidence. The producer declares one Claude Code Opus 5.5 session for all those
roles. This is a different session and model family. No responsibility for defending the original
packet is held. The packet and its proposed conclusions were read before reconstructing every
individual oracle; possible anchoring is disclosed rather than claiming a blinded review.

For **every** scoped family, original author/executor/selector/triager = that declared Claude
session; review-probe author/executor/selector/triager and this bounded independent adjudicator =
this Codex session. Human Git authorship records the repository owner's commit identity, not who
independently produced evidence. Future Phase D must exclude both evidence producers, including this
session, from independently challenging their own families.

Grounding used the live issue and comment, current `AGENTS.md`, contributor and testing contracts,
Factory model §§2–6, Factory Design §§4–5/11–12, Engine semantics §5.1, controlled-revision identity
and acceptance, Storage's supported boundary, Governance semantic change, and their implementing
types/tests. The researcher role was consulted for independence/evidence discipline; this is a
commissioned assurance challenge, not a new research decision or a PR disposition. The accepted
Phase A design was checked for its scoped obligations and identity, not re-reviewed wholesale.

Risk is high for false assurance about identity and historical authority. Depth therefore includes
independent witnesses, actual reruns and falsifying controls, rather than coverage or CI inference.
No external standards interpretation was needed: UUID meaning was checked against the owning
repository contract. No web claim, cryptographic proof or new UUID requirement is introduced.

## Baseline, inventory and provenance

Live main at entry was `27b59eab19efd492d9d759cead4bfd8742fed856`. GitHub reported PR #542 merged,
with that merge commit and head `99defd43aa030f479c200c8b1d38ce5a084e1a38`. Its net change from the
execution baseline is exactly three retained test assertions, 65 added lines, in existing files.
During review main advanced to `140d9c08efa3de720ab148fa281e28911b2460f7`, adding only the two-line
external-result-locator instruction to `AGENTS.md`. No production or specification drift occurred.
The review branch was fast-forwarded to that main. These are observed drift facts, not a promise
about future main.

[The independent audit](audit_packet.py) re-derived the historical and execution trees directly from
Git blobs and verified the crosswalk against its assessed commit. It confirmed **300 → 313 Java
paths: 257 unchanged, 43 changed, 13 added, zero removed**, with 33 contract identifiers. The added
U2 helper/probes, scheduling/sequence tests, Governance revision binding and research arithmetic
tests are accounted for. The packet's proposed Campaign 1 mappings are consistent with their
responsibilities; non-Campaign-1 additions remain other campaigns' work. No accepted historical
mapping was overwritten. [Audit results](results/packet-audit.json).

Execution used a fresh `git archive` of the exact source baseline, with no inherited build outputs,
inside a dedicated `gradle:9-jdk21` Docker container. Image ID
`sha256:c0ce93e022ea2e705332dabe090019c749356576fc8fe39c38129b2aae9ed68f`, Temurin 21.0.12+8,
wrapper Gradle 9.8.2 and host CPython 3.14.4 agree with the packet's declared toolchain. No suitable
running devcontainer mounted this repository; the documented generic Docker workflow was used.
[Environment](results/environment.json).

The clean retained selection actually discovered **38 suites / 302 tests**, all passing. Installing
the six immutable experiment sources then discovered **five suites / 37 tests**, all passing.
All 153 compiled main-class hashes in the clean baseline matched the experiment run. Sources and
classfiles are hashed separately; compiled-file presence is not claimed to prove execution.
[Clean baseline](results/clean-baseline.json), [experiment run](results/producer-experiments.json).

The five regenerated result files are byte-identical to the packet: differential ledger,
differential mismatches, targeted codec ledger, definition-binding coverage and fresh-JVM
diagnostics. Python regeneration also exactly matches all three golden-vector JSON values.
This reproduces the reported 8,489 attempts/1,295 accepted results, 3,000 structured models,
4,096 predicate combinations, 720 eligible-order permutations/eight child JVMs, 500 assembly
models and 4,500 comparison pairs under their source-defined bounds. Counts describe loop
attempts, not distinct semantic cases or independent probability samples.

**BLOCKED — original exact-classfile/run provenance.** The immutable producer packet contains
suite summaries and selected output ledgers, but no original classfile digest manifest, full
per-test discovery records or raw failing-test payloads for all 44 mutation runs. A matching
toolchain and a successful new run cannot retroactively establish the producer's exact binaries or
every original failure cause. This review supplies new observed discovery and source/classfile
manifests; it does not relabel them as original-run evidence. The producer can supply missing
original records or publish a superseding, explicitly identified run.

## Findings and discriminating controls

### CH-1 — complete publication messages are not independently compared (medium)

In `Campaign1PublicationExperimentTest.kind`, operation, step and product name diagnostics are
recognized from the field alone. Their message text is discarded. `check` compares a multiset of
kinds, and compares the publisher exception payload with a second call to the same validator.
The compound fresh-JVM expected sequences are also obtained from that validator. The signed U2
witness does have a literal independent expected sequence and remains valid evidence.

Control: keep resource Unicode messages unchanged, but change every other Unicode-name message to
`CHALLENGE changed text`. **All seven producer publication tests pass.** An independent literal
nine-diagnostic field/message witness fails, showing the changed operation/step/product messages.
[Producer result](results/diagnostic-message-producer.json),
[independent result](results/diagnostic-message-independent.json), [control implementation](run_controls.py).

Classification: **experiment observation gap**, not a product violation. The specification does not
standardize those words or a universal cross-kind sort. The issue nevertheless commissions complete
field/message sequence comparison; vocabulary must be identified as observed where not normative.
The producer should add explicit expected field/message observations without inventing a new global
ordering contract, and narrow the existing claim until then.

### CH-2 — Storage refusal category is invisible to the seam corpus (medium)

The campaign's invalid-artifact and unknown-revision checks accept any `RuntimeException` at
Storage/history boundaries. They do not observe `GovernanceHistoryException.code()`.
Control: in `FileArcogineStorage.verifyArtifact`, return `STORAGE_INTEGRITY` instead of
`FINGERPRINT_MISMATCH` for an unaccepted malformed candidate. **All five producer seam tests pass.**
The independent empty-model/truncated-artifact witness fails with expected `FINGERPRINT_MISMATCH`,
observed `STORAGE_INTEGRITY`. It also checks the exact exception type, absence of the candidate ID,
unchanged revision enumeration and unchanged historical base bytes after each refusal.
[Producer](results/storage-category-producer.json), [independent](results/storage-category-independent.json).

Classification: **experiment observation gap** against the commissioned precise refusal/post-state
standard and the typed history failure boundary. The actual unmodified implementation passes the
independent checks. This does not establish arbitrary filesystem atomicity, durability or all Storage
error mappings. The producer should observe the precise categories and per-refusal historical state.

### CH-3 — unreferenced operations are outside the assembly observation (medium)

The 500-model assembly loop checks operation/routing facts only by iterating `model.products()` and
resolving each product's route. An operation without a referencing product is never inspected.
Control: skip assembly of exactly those operations. **All five producer change/assembly tests pass.**
A minimal model with one resource, one operation/step and **zero products** fails the independent
`getRouting(10)` witness with `SimError.UnknownId`. The unmodified implementation preserves it.
[Producer](results/assembly-unreferenced-producer.json),
[independent](results/assembly-unreferenced-independent.json), [reproducer](IndependentFactoryChallengeTest.java).

Classification: **experiment coverage gap** in authored-operation preservation under Factory Design
§§4–5 and the assembler's model-derivation contract. Add direct checks for every authored operation,
including zero-product and partially referenced models, before claiming all authored assembly facts.
No production fix is proposed.

### CH-4 — byte and mutation claims require explicit narrower boundaries (low/medium)

The named “exhaustive single-edit corpus” is exhaustive over its chosen positions and **selected
values**, not all one-byte edits. Substitution uses up to six values; insertion uses only `00/01/ff`.
For example, replacing representative offset 47's `4d` (`M`) with `41` (`A`) is absent. The residual
frontier includes **untried one-edit values**, as well as two-or-more edits and allocation frontiers.
These 8,489 attempts also contain duplicates. Rename/described bounds should match the generator.

An independent empty-artifact check enumerated all **11,985** nonidentity byte substitutions and
**12,288** single-byte insertions. All were refused with exactly `IllegalArgumentException`, as
the fixed empty grammar requires. A fourth independent literal vector checks signed extremes,
astral/decomposed names, NUL, signed zero and exact bytes/digest without calling producer helpers.
These checks extend only the stated small domain; they do not close the general byte frontier.
[Probe source](IndependentFactoryChallengeTest.java), [four-test run](results/assembly-frontier-baseline.json).

All 44 saved mutation summaries have the same selected suites and test counts as their clean
baseline, with 37 failing configurations and seven survivors. No summary-level discovery mismatch
was found. However, `mutation_ledger.py` accepts an **empty** `ALL-PASS` baseline, computes then
discards missing suites, and treats any suite error as a kill. The independent synthetic control
with a `com.example.DiscoveryFailure` error is incorrectly labeled `KILLED`. This establishes a
fail-closed-tooling gap, **not evidence that an original mutant was actually killed by discovery**.
The experiment wrapper also pipes Gradle output through `grep` without preserving Gradle's exit;
this review ran Gradle directly and checked real discovery/results.

Six original mutants were independently rerun: time positive-boundary guard, missing canonical
round-trip, missing trailing guard, negative count, raw NaN and unverified snapshot. Their observed
outcomes match the packet. In particular, `F-DEC-NEGATIVE-COUNT` fails **only** because expected
message `invalid factory-model canonical artifact` becomes `len < 0`; both remain the required
refusal class. Preserve the packet's explicit diagnostic-only qualification: 37 failing configurations
are not 37 demonstrated semantic violations. The seven survivor explanations are consistent with
the source at the stated refusal/value boundary; only the trailing-guard survivor was rerun here.

## Per-family disposition

`ACCEPTED` below concerns the named bounded evidence, not independent assurance of the whole
implementation. Qualifications/blockers cannot be removed by combining rows.

| Producer family / status | Independent disposition | Basis and surviving limits |
|---|---|---|
| T-VALUE numeric typed-ID laws / EXECUTED | ACCEPTED | Signed boundaries, type separation and retained tests inspected; 11-test value experiment rerun. |
| T-VALUE revision-ID version/variant/spelling / EXECUTED | ACCEPTED WITH QUALIFICATIONS | Character-level grammar and constructor bit partitions match the owning contract. UUID collision/randomness quality excluded; 1,000 generations are samples. |
| T-VALUE fingerprint grammar/equality / EXECUTED | ACCEPTED WITH QUALIFICATIONS | Namespace/algorithm/digest remain distinct. D5 rendering frontier persists; no parser contract invented. |
| T-VALUE structured errors and RunId / EXECUTED | ACCEPTED WITH QUALIFICATIONS | Structured facts rerun. Correlation identity's nonparticipation in decisions remains Campaign 2. |
| T-TIME value-level U1 / EXECUTED | ACCEPTED | BigInteger oracle, 804 addition and 144 subtraction/order pairs; boundary mutant detected. Current non-negative meaning used. |
| T-TIME Engine-site closure / BLOCKED | ACCEPTED WITH QUALIFICATIONS | The explicit blocker is correct. Retained overflow tests are bounded evidence; this challenge does not close every dispatch site. |
| F-PUBLISH individual predicates / EXECUTED | ACCEPTED WITH QUALIFICATIONS | All admitted/refused boundaries and 4,096 interactions rerun. Diagnostic granularity is an explicit observation assumption. |
| F-PUBLISH complete diagnostic messages / implicit EXECUTED claim | NOT ACCEPTED | CH-1: field/kind agreement and same-validator equality do not independently compare complete message sequences. |
| F-PUBLISH U2 order / EXECUTED | ACCEPTED | Literal signed witness, 720 permutations and full fresh-JVM repeatability reproduced; no universal error sort imposed. |
| F-PUBLISH copy isolation/refusal state / EXECUTED | ACCEPTED | Source/accessor nesting checks and corrected retry rerun. Construction refusals remain separate. |
| F-CODEC literal bytes/digests / EXECUTED | ACCEPTED | Three independently encoded vectors re-derived; fourth literal witness added. SHA-256 collision resistance remains an assumption. |
| F-CODEC identity relations / EXECUTED | ACCEPTED WITH QUALIFICATIONS | Eligible permutations, list order and named field changes rerun. Equal fingerprints do not prove historical provenance. |
| F-CODEC strictness and predicate refusal / EXECUTED | ACCEPTED WITH QUALIFICATIONS | Differential/targeted ledgers exactly reproduced. CH-4's selected alphabet, compound-invalid precedence and finite-size limits survive. |
| F-CODEC structured models / EXECUTED | ACCEPTED WITH QUALIFICATIONS | 3,000 models from the exact seed, implementation and finite pools rerun; normalization in neutral projection is not a separate proof of constructor preservation. |
| F-CODEC support/definition binding / EXECUTED | ACCEPTED WITH QUALIFICATIONS | Truth table, byte-digest recomputation and class-load probe rerun. Class loading is not proof of complete control-flow execution; the list comes from the product and was also inspected statically. Historical cross-build guarantees remain blocked. |
| F-ASSEMBLE authored facts / EXECUTED | ACCEPTED WITH QUALIFICATIONS | Resource/product-linked routing checks reproduced; all-authored-operation preservation is NOT ACCEPTED until CH-3 is covered. |
| F-ASSEMBLE isolation/metadata separation / EXECUTED | ACCEPTED WITH QUALIFICATIONS | Two sessions and the four-unit single-product trace rerun. No generalized workload equivalence or future setup semantics inferred. |
| F-CHANGE edit/membership/order expectations / EXECUTED | ACCEPTED WITH QUALIFICATIONS | 4,500 bounded pairs, swap/no-op and independent field/aspect ledger rerun. Shared generator and descriptive-detail parsing are correlated limits; no exhaustive per-field fault-model claim. |
| G-CHANGE real revision/Storage seam / EXECUTED bounded | ACCEPTED WITH QUALIFICATIONS | Published artifacts, distinct revision identities, historical/snapshot paths and same-build reopen reproduced. Precise refusal-category completeness is NOT ACCEPTED under CH-2. The two producer-missed Governance mutants remain honestly recorded. |
| Randomness/collision proof, larger byte/model domains / RESIDUAL-EXCLUDED | ACCEPTED WITH QUALIFICATIONS | Exclusions are correct; add untried single-edit values and unreferenced-operation observation to the declared frontier. |
| Original exact-run provenance / evidence claimed | NOT ACCEPTED (BLOCKED) | Original binary manifest and per-test/failure records are absent from the immutable packet. New independent manifests cannot impersonate them. |

## Discrepancy reassessment and retained regression checks

D1's observation ordering is a corrected experiment assumption: current resource observations sort
by signed ID, while the assembled store preserves authored order. D2 correctly separates a decoding
range failure from publication predicates. D3's invalid helper fixtures were oracle/test errors;
their corrected forms run successfully, but the intermediate original failures are not in immutable
custody and were not independently reproduced. D4's step-detail wording/delimiters are descriptive,
not a specified semantic parser. D5 is a real noninjective rendering possibility for colon-bearing
namespace/algorithm values, without a current parse contract. Neither D4 nor D5 becomes a new
product requirement in this review.

The three exact test files from merged main were overlaid separately on the baseline scratch tree;
they are explicitly a **different test baseline**, never aggregated into the 37-test campaign run.
Their three-class baseline passes **36 tests**. Each original target mutant is then detected by its
own retained class: nonpositive dangling references (13 tests, one failure), numeric capacity
comparison (20, one failure), first-operation routing (three, one failure). Restoring production
sources then passes **42 selected tests**: those 36 plus four independent Factory and two independent
Storage witnesses. [Retention and restoration records](results/final-restored.json).

No full `./arcogine check`, security scan or new exact-head CI gate was run for this transient
evidence-only branch. No maintained product source/test/specification changed, and no merge PR is
opened. These omissions do not change the previously merged PR's live history or substitute for CI.
All intentional control failures are labeled as such; no compile/discovery failure was counted as
a semantic kill in this review.

The existing `:factory:test --tests com.arcogine.factory.process.TaskEndTimeOverflowTest` selection
was also rerun after restoration: **four tests passed**, including structured refusal and
post-state assertions at its named sites. [Observed results](results/time-engine-boundary.json).
This does not expand the commissioned scope into a new Campaign 2 exploration.

## Reproduction and next owner action

The committed Java/Python sources here are the minimized fixtures and control definitions. Raw
console logs and diagnostic scratch remain in ignored `logs/`; the committed JSON captures actual
test names, counts, failure type/message, source hashes and compiled-class hashes for each run.
See [capture_run.py](capture_run.py), [run_controls.py](run_controls.py) and
[run_assembly_retention.py](run_assembly_retention.py). The fixed container name is a reproduction
parameter; do not reuse another actor's container. Main/test overlays are named explicitly in those
scripts. Initialize scratch Git's index as described below so mutation restoration is deterministic.

1. In a clean review checkout, extract the source SHA above to `logs/source` and the exact producer
   packet tree to `logs/packet`. Keep the original baseline's product files in scratch Git's index.
2. Mount source as `/repo`, producer packet as read-only `/packet`, and review checkout as `/review`
   in the named JDK 21 container, working directory `/repo/product`.
3. Run the packet's declared clean retained selection; capture with
   `python -I workspace/review/core-reliability-campaign-one/capture_run.py clean-baseline 0`.
4. Install experiments with `sh /packet/tools/install-experiments.sh`; run the three declared
   `Campaign1*` selections with `CAMPAIGN1_RESULTS_DIR=/review/logs/reproduced-results`, observing
   Gradle's actual exit. Capture `producer-experiments`.
5. Initially copy `IndependentFactoryChallengeInitial.java` as
   `.../factory/model/IndependentFactoryChallengeTest.java` and the Storage Java file into its
   scratch test package, and run their fully qualified class names. The initial file's hash is
   verified against the first five-test run; the fourth Factory witness was added subsequently.
   Then run `python -u -I workspace/review/core-reliability-campaign-one/run_controls.py` followed by
   `python -u -I workspace/review/core-reliability-campaign-one/run_assembly_retention.py`.
   The latter installs the final four-test Factory file. The scripts require the stated pass/fail outcomes,
   retain the test payloads and restore mutated production sources in `finally` blocks.
6. Run `python -I -X utf8 workspace/review/core-reliability-campaign-one/audit_packet.py` from a checkout containing the historical Git objects.
   Its output independently checks inventory, summaries, vectors and the synthetic discovery control.

The owner should delegate an **evidence revision** for CH-1–CH-4 and the provenance blocker to the
campaign producer, preserving the old packet and publishing new immutable coordinates. A targeted
independent follow-up should then challenge that revision. The existing three retained regressions
are landed and discriminating; no additional production change is authorized or needed by these
findings. Keep issue #541 open pending that evidence disposition. Campaigns 2–4, Storage durability,
cross-build reopen, U11, exact-future-main reconciliation and Phase D remain separate duties.
