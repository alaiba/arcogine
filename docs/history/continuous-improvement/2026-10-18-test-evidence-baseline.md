# One-time Java test-evidence baseline — 2026-10-18

Non-normative, point-in-time evidence. Assessed `main`:
`3b0747acdd1d807ce68e78d7adf1217b57cdb86a`, resolved on 2026-10-18.
This is neither correctness certification nor a release/support promotion. It is
a breadth-first inventory with selected deep reads and two executed diagnostic
pilots, not an audit of every statement. Unchanged modules were included.

## Inventory and assertion strength

Inventory authority: `product/settings.gradle.kts` and the tracked Java source/test
tree at the assessed revision. All nine declared modules are represented below.
Paths in the matrix are relative to `product/`; Java names resolve within the
named module's `src/main/java` or `src/test/java` tree. Contracts are under
`docs/architecture/`. The tests named here were inspected for their oracles, not
credited merely because they execute. Sampling within a row does not mean every
test in its module was deeply examined.

| Module and material boundary | Contract and consequence if wrong | Retained evidence and oracle | Limits / NOT ASSESSED |
| --- | --- | --- | --- |
| `types`: `SimTime`, typed IDs, `ModelFingerprint`, `ControlledRevisionId` | Overview time/identity distinctions; controlled-revisions UUIDv4 and immutable occurrence identity. Bad arithmetic/order or identity parsing propagates across modules. | `SimTimeTest` checks exact addition and zero-saturated subtraction; typed-ID tests compare ordering/value rendering; `ControlledRevisionIdTest` checks canonical round trips and rejects wrong UUID version/variant and noncanonical forms. | Extreme arithmetic combinations and exhaustive identity inputs NOT ASSESSED. Example coverage does not decide an unstated overflow policy. |
| `simulation`: `Scheduler`, `Event` | Engine semantics §4 and overview determinism: time order, equal-time insertion order, no past scheduling. Wrong ordering changes downstream dispatch. | `SchedulerTest` compares distinguishable payload order, current time and exception metadata. `SchedulerPropertyTest` checks monotonicity over seeds 0–255, counts dequeued events and permits current-time scheduling. `EventTest` checks payload identity/value. | Generated tests mostly prefill then drain; counts alone cannot detect equal-count replacement. Rejection post-state was not pinned by the focused suite; pilot below. Sequence-counter exhaustion NOT ASSESSED. |
| `governance`: revisions, changes and impact | Controlled revisions; governance-conformance. Content equality must not collapse occurrences; scope must not silently omit affected entities. | `ControlledRevisionTest`, `ChangeSetTest`, requirement/assertion tests: immutable identities and scopes; exact sorted change/impact lists, including delimiter-bearing identities that would collide under concatenation. Storage's `ChangeSetFactoryTest` and `PreChangeConformanceProvingCaseTest` contribute real Factory/history integration. | Full cross-product of identity, scope and change kinds NOT ASSESSED. |
| `governance`: `ConformanceEvaluator`, evidence-reference/evaluation-occurrence authorities | Governance evidence and conformance contracts. Incorrect attribution or unsupported PASS/FAIL would turn missing evidence into a claim. | `ConformanceEvaluatorTest` uses a content-dependent fingerprinter, checks result/finding attribution, deterministic repeated evaluation and rejects mismatched state. `GovernanceEvidenceTest` checks canonical earlier-reference identity, correction preserving earlier outcomes, inadequate external-evidence rejection and an unchanged empty authority after refusal. | Broad combinations of evidence roles, applicability, temporal frames and subject bindings NOT ASSESSED; no durable evidence-store claim. |
| `storage`: `BuiltInStorage.open`, `ArcogineStorage.controlledRevisions`, private filesystem provider | Storage opening/integrity and controlled revisions. Wrong acceptance/recovery can rebind or lose immutable history. | `BuiltInStorageTest` checks exact bytes/provenance after independent-JVM reopen, duplicate/conflicting acceptance, F1→F2→F1, foreign/incomplete roots, malformed records, strict text, injected atomic-install/cleanup failures, unchanged files and authoritative history. `StoragePublicContractTest` exercises the public consumer boundary. | Existing fault seams are substantially stronger than success-only tests. Process-kill/power-loss/network-filesystem behavior is NOT ASSESSED and outside established support; do not turn it into a missing required test. |
| `domains/factory`: publication, canonical artifact and semantic comparison | Factory model §§2–5; Factory design; controlled revisions. Wrong identity or decoding corrupts equivalence and recovered history. | `FactoryModelCanonicalFormTest` pins literal bytes and SHA-256 vectors, semantic-field changes, set ordering, list-order significance, signed zero/NaN and malformed Unicode. `FactoryModelArtifactTest` checks exact model/byte round trip, invalid UTF-8/counts/markers, noncanonical ordering and grammar-valid but unpublishable records. Validator, immutability, assembler and comparator tests cover their respective boundaries. | Golden vectors give an oracle independent of encoder/decoder agreement; round trip alone would not. Full binary grammar/input-size exploration NOT ASSESSED. |
| `domains/factory`: `FactoryRuntime`, handler dispatch, job/machine lifecycle and derived results | Engine semantics §§1–5; runtime contract. Wrong resource selection, cascade order, budget boundary or accumulation changes authoritative outcomes. | `EngineDispatchConformanceTest` checks selected IDs, local FIFO completion times, mixed local/shared recovery and exact pending state. `BoundedAdvancementConformanceTest`, `EngineDerivedResultConformanceTest`, lifecycle and property suites provide boundary and arithmetic fixtures. Runtime event/observation acceptance tests check cursors, immutable snapshots, authoritative changes before faults, no-op emission and semantic replay with only run identity normalized. | Arbitrary command-interleaving/state-space exploration NOT ASSESSED. Shared-backlog performance already has an owner; no new performance pilot here. |
| `consumer/challenge`: definitions/catalogues, economics, admissibility, evaluation and comparison | Challenge §§Definition–Attempts. Wrong order dependence, overflow or compatibility can change game results while Factory stays valid. | Validator tests assert structured codes/paths. `DraftEconomicsCalculatorTest` checks exact costs, occurrence permutations and explicit overflow. Admissibility tests check exact-limit/over-limit budgets and counts, floor edges and catalogue fingerprint mismatches. Evaluation tests pin policy outcomes; `ChallengeAttemptComparatorTest` checks exact deltas, ties and incompatible identities without reevaluation. | Large combinations of invalid rules and permutation spaces NOT ASSESSED. No production-fact inference is credited to Challenge. |
| `consumer/challenge`: `content/Json`, `ChallengeContentLoader` | Challenge §Content loading and decoder contract: malformed representations yield structured issues. Bad decoding can silently alter authored identity. | `JsonTest` asserts decoded trees/escapes and rejection of bad numbers, ASCII digit violations, raw controls and invalid/truncated escapes. Loader tests assert structured malformed-JSON failures, numeric precision/range boundaries, policy resolution and catalogue binding. | Escape character-class breadth was not covered by those examples; actual defect demonstrated below. Deep nesting/resource exhaustion and duplicate-key policy NOT ASSESSED. |
| `consumer/challenge-factory-integration-test` — test-only | Challenge/Factory independent validation axes. Passing publication must not imply game admissibility. | `FactoryPublicationValidButChallengeInadmissibleTest` uses the same nine-cutter candidate shape, asserts Factory validity and explicit budget rejection by Challenge. | One counterexample establishes non-implication; no projector or runtime integration claim. |
| `research-experiments` — executable, non-shipped | Testing guide research substrate; package contract; supported runtime evidence only. Incomplete evidence must not become a derived fact. | `ExperimentRunnerTest`, corpus tests compare replay after normalizing only RunId and deliberately altered evidence. `OracleContractTest` checks that a gap-spanning read refuses while an intact suffix derives with exact support; evidence-window and occupancy/waiting/completion oracle tests pin boundaries, closed-form results and refusal cases. | Research-local derivations are not shipped analytics or Engine facts. All possible gap arrangements, derivations and model families NOT ASSESSED. |
| `architecture-conformance-test` — test-only | Overview ownership, dependency and determinism boundaries. A dependency leak can bypass authoritative state or introduce ambient inputs. | `ArchitectureTest` scans main classes with ArchUnit. `DeterminismGuardTest` proves forbidden legacy clocks/background factories violate rules and sequential streams do not. | Static rules cover named mechanisms, not every possible source of nondeterminism/reflection; no behavioral equivalence proof. |

Observed baseline coverage came from freshly generated module
`build/reports/jacoco/test/jacocoTestReport.xml` files, rendered by
`node .github/scripts/report-java-coverage.mjs` after `./arcogine check` completed:

| Executable module | LINE | BRANCH |
| --- | ---: | ---: |
| challenge | 94.2% | 87.8% |
| factory | 100.0% | 99.7% |
| governance | 100.0% | 100.0% |
| research-experiments | 100.0% | 100.0% |
| simulation | 100.0% | 100.0% |
| storage | 100.0% | 100.0% |
| types | 100.0% | 100.0% |

Values are rounded to one decimal, not assertions of literally complete coverage.
The baseline run executed all 73 Gradle tasks in 5m 14s using wrapper 9.8.1 and
Temurin 21.0.12.1+1 in Docker. Its source/test compilation preceded the retained
scheduler assertion edit; diagnostic copies compiled only into ignored scratch
and did not change Gradle classes or JaCoCo data. The policy is a 90% LINE floor
per executable module, with combined execution data from all suites; the two
test-only modules have no coverage gate. BRANCH is diagnostic only. Neither
percentage measures assertion strength, and JaCoCo does not count exception
handling as branches. The parser defect and scheduler survivor despite these
percentages illustrate that limit directly.

## Risk-prioritized questions

Selection used consequence, assertion specificity, input/state-space breadth and
availability of a narrow oracle, not recency or a numeric confidence score.

| Priority / exact code and tests | Concrete uncertainty and consequence | Efficient diagnostic and legitimate failure oracle |
| --- | --- | --- |
| First: `consumer/challenge/src/main/java/com/arcogine/challenge/content/Json.java:parseUnicodeEscape`; `JsonTest`, `ChallengeContentLoaderTest` | Does every accepted four-character escape use ASCII hexadecimal digits? Existing invalid examples use truncation or letters outside hex; `Integer.parseInt` is a broader parser. Silent interpretation of malformed authored identifiers matters. | Bounded character substitution fuzzing with explicit ASCII-hex membership and exact decoded-character checks; malformed acceptance or valid-value mismatch is a failure. Executed. |
| Second: `simulation/src/main/java/com/arcogine/core/queue/Scheduler.java:schedule`; `SchedulerTest`, `SchedulerPropertyTest` | Would the focused tests detect a rejected past event still being queued? They assert the throw/metadata but did not inspect subsequent state. Such leakage can move time backward. | Three hand-selected compiled source mutants isolate FIFO, equality and rejection side effects. Known semantic faults must fail existing tests; inspect survivors rather than infer a bug. Executed. |
| Consider: `domains/factory/src/main/java/com/arcogine/factory/model/FactoryModelArtifact.java:decode`; `FactoryModelArtifactTest`, `FactoryModelCanonicalFormTest` | Do combinations of nested counts, truncation and canonicality checks reject malformed binary representations without accepting altered content? Current targeted mutations leave the combination space UNKNOWN. | Bounded generated canonical models plus grammar-aware truncation/count mutation; accepted valid bytes must re-encode identically, known-invalid forms must refuse. Preserve golden vectors as an independent oracle. Not executed. |
| Consider: `governance/src/main/java/com/arcogine/governance/conformance/InMemoryEvaluationOccurrenceAuthority.java:accept`; `GovernanceEvidenceTest` | Do overlapping role/applicability/subject mismatches ever permit an external-evidence PASS/FAIL? Individual refusals exist; combined predicate fault sensitivity is UNKNOWN. | Targeted predicate mutation against existing tests, then one deterministic combination only if a meaningful survivor appears. The oracle is acceptance refusal with no recorded occurrence, not a blanket rule that every evaluation must fail. Not executed. |

The already-owned shared-backlog performance work was checked through the open
issue titled **Same-semantics shared-backlog performance**. It owns result-equivalence
and benchmark evidence for an eventual optimization; this baseline does not duplicate
it. Other open issues concerned documentation/Consistency, and no open PR was present
at the ownership check. Storage failure-model and canonical-artifact-boundary research
remain separate questions; neither is a reason to extend supported semantics here.

## Pilot 1: escape grammar challenge

Environment: Linux Docker `gradle:9-jdk21`, Temurin 21.0.12.1+1, `javac --release 21`.
No third-party fuzzer. Deterministic enumeration, no random seed: substitute each
UTF-16 code unit 0–65535 into each of the four positions of `0041`. This gives
262,144 inputs, including 88 valid ASCII-hex cases (duplicates across positions
are intentional). The oracle is the decoder's JSON grammar, not another parser's
possibly permissive behavior. Valid cases must decode to their exact character;
invalid cases must throw `JsonSyntaxException`.

Reproduce from the assessed revision: put this small probe in ignored
`logs/EscapeProbe.java` (package access is intentional), then run the commands below.

```java
package com.arcogine.challenge.content;
public class EscapeProbe {
    public static void main(String[] args) throws Exception {
        int valid = 0, invalid = 0, acceptedInvalid = 0, wrongValue = 0;
        for (int p = 0; p < 4; p++) for (int c = 0; c <= 65535; c++) {
            char[] digits = "0041".toCharArray(); digits[p] = (char) c;
            String hex = new String(digits);
            boolean permitted = "0123456789abcdefABCDEF".indexOf(c) >= 0;
            if (permitted) valid++; else invalid++;
            try {
                Object value = Json.parse("\"" + "\\" + "u" + hex + "\"");
                if (!permitted) acceptedInvalid++;
                else if (!String.valueOf((char) Integer.parseInt(hex, 16)).equals(value)) wrongValue++;
            } catch (JsonSyntaxException e) {
                if (permitted) throw new AssertionError(hex, e);
            }
        }
        System.out.printf("valid=%d invalid=%d acceptedInvalid=%d wrongValue=%d%n",
                valid, invalid, acceptedInvalid, wrongValue);
    }
}
```

```bash
mkdir -p logs/escape-classes
javac --release 21 -d logs/escape-classes \
  product/consumer/challenge/src/main/java/com/arcogine/challenge/content/Json.java \
  product/consumer/challenge/src/main/java/com/arcogine/challenge/content/JsonSyntaxException.java \
  logs/EscapeProbe.java
time java -cp logs/escape-classes com.arcogine.challenge.content.EscapeProbe
```

Observed: `valid=88 invalid=262056 acceptedInvalid=1490 wrongValue=0`;
enumeration runtime approximately 1.7 seconds, plus compilation. Inspected examples:
`"\u+041"` and `"\u０041"` both decode to `A`; `"\u-041"` decodes to U+FFBF.
The first and third contain a sign; the second contains fullwidth zero U+FF10.
These are actual malformed-input acceptance defects, not equivalent fuzz cases.
`Integer.parseInt(hex, 16)` accepts signs and Unicode digits; casting its result
does not establish the required grammar.

Public-path confirmation used `ChallengeContentLoader.load` with this document,
substituting each of `0041`, `+041`, `-041`, `０041` for `XXXX`:

```json
{"schemaVersion":"challenge-content:v1","identity":{"id":"\uXXXX","version":"1"},
 "floor":{"width":10,"height":8},"startingBudget":5000,
 "workload":{"productReference":"widget","requiredQuantity":20},
 "availableEquipment":[],"deadline":400,
 "evaluationPolicy":{"id":"policy.contract-completion","version":"1"}}
```

All four returned `isSuccess() == true` with no issues; IDs were U+0041, U+0041,
U+FFBF, U+0041 respectively. The public probe was compiled with
`javac --release 21 -cp product/consumer/challenge/build/classes/java/main` and run
with that classpath plus its ignored output directory. This is a syntax-refusal
defect, not evidence of arbitrary execution or a general security severity claim.
The experiment excludes nesting, input-size exhaustion, surrogate-pair policy and
duplicate-key semantics. No production fix or failing retained test was added.

## Pilot 2: scheduler fault detection

Temurin/Javac as above; JUnit Platform/Jupiter 6.1.3 from the repository's resolved
test dependencies. No PIT/plugin or build configuration was added. In ignored
`logs/scheduler-<variant>/Scheduler.java`, copy the assessed Scheduler source and
apply exactly one replacement from this table. Compile the copy and the two
retained scheduler test classes into an isolated directory; put that directory
first on the runtime classpath so each run uses only its chosen Scheduler.

| Variant | Exact source replacement | Baseline focused suite (32 invocations) |
| --- | --- | --- |
| Control | No replacement | 32 passed |
| Reversed FIFO | `thenComparingLong(Entry::sequence)` → `thenComparingLong(e -> -e.sequence())` | 2 failures: both payload-sensitive same-tick ordering tests |
| Reject equality | `compareTo(currentTime) < 0` → `compareTo(currentTime) <= 0` | 2 failures: current-time acceptance and full-seed monotonicity fixture |
| Enqueue then reject | Insert `queue.add(new Entry(event, nextSequence++));` immediately before `throw new SimError.EventOrderingViolation` | 32 passed: meaningful survivor |

No random mutations; existing property seeds were unchanged. The survivor is a
missing assertion, not a production defect: the real implementation rejects before
enqueueing. It is not equivalent because a later dequeue would deliver the rejected
past event. The survivor statement is scoped to these two test classes, not all
downstream Factory tests.

The executed runner, saved as ignored `logs/RunSchedulerTests.java`, was:

```java
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
public class RunSchedulerTests {
    public static void main(String[] args) {
        var listener = new SummaryGeneratingListener();
        var request = LauncherDiscoveryRequestBuilder.request().selectors(
                DiscoverySelectors.selectClass("com.arcogine.core.queue.SchedulerTest"),
                DiscoverySelectors.selectClass("com.arcogine.core.queue.SchedulerPropertyTest")).build();
        LauncherFactory.create().execute(request, listener);
        listener.getSummary().printTo(new java.io.PrintWriter(System.out));
        listener.getSummary().printFailuresTo(new java.io.PrintWriter(System.out));
        if (listener.getSummary().getTestsFoundCount() == 0) throw new AssertionError("no tests");
        if (listener.getSummary().getTotalFailureCount() != 0) System.exit(1);
    }
}
```

Commands run for each `variant` (`baseline`, `reverse`, `equality`, `rejected`),
after normal Gradle compilation/dependency resolution; the cache location shown
is the diagnostic container's mounted cache:

```bash
jars=$(find /root/.gradle/caches/modules-2/files-2.1/org.junit* \
  /root/.gradle/caches/modules-2/files-2.1/org.opentest4j \
  /root/.gradle/caches/modules-2/files-2.1/org.apiguardian -name '*.jar' -printf '%p:')
classes="logs/scheduler-$variant/classes"
mkdir -p "$classes"
cp="product/types/build/classes/java/main:product/simulation/build/classes/java/main:$jars"
javac --release 21 -cp "$cp" -d "$classes" "logs/scheduler-$variant/Scheduler.java" \
  product/simulation/src/test/java/com/arcogine/core/queue/SchedulerTest.java \
  product/simulation/src/test/java/com/arcogine/core/queue/SchedulerPropertyTest.java \
  logs/RunSchedulerTests.java
java -cp "$classes:$cp" RunSchedulerTests
```

The small retained change strengthens `SchedulerTest.rejectPastTimeEvents`: keep
a future event pending, reject a past event, assert unchanged current time, size,
peek and exact next event, then assert the queue is empty and time advanced only
to the legitimate future event. With these assertions, the control still passes
all 32 invocations and the survivor fails at size (expected 1, actual 2). The
two earlier mutants were already killed; no equivalent/irrelevant mutant was
claimed. Compilation and execution of the four variants cost roughly one minute
while the full baseline build ran; strengthened control/survivor reruns took
roughly 15 seconds. This is a three-fault probe, not a mutation score or a claim
about all scheduling faults. Test strengthening belongs to the report's delivery
change and was not part of the assessed main revision.

## Findings, follow-through and limits

- **Separate bounded remediation proposed:** require exactly four ASCII hex digits
  before decoding a JSON escape. Add deterministic rejection cases for signs and
  Unicode digits in `JsonTest`, plus public-loader `content.malformed-json` cases;
  preserve valid upper/lowercase escapes and exact values. Run focused Challenge
  tests and the normal gate. This report preserves reproducing evidence; it does
  not fix the decoder or change what JSON means.
- **Retained test improvement:** scheduler rejection post-state is now explicitly
  checked in the delivery change. There was no demonstrated scheduler product bug.
- **Optional follow-up questions:** the canonical-binary and Governance predicate
  questions above remain unexecuted uncertainties, not discovered defects or
  obligations to add a diagnostic framework.
- **Current-state custody limitation:** no new issue or standing owner was created
  for these proposed follow-ups. The repository owner can choose the narrow decoder
  remediation next. A future checkpoint must consult a current owner request or
  issue before treating this historical report as unresolved work; it must not
  rescan the report as a status ledger.

The testing guide and Continuous Improvement role now permit an explicitly retained,
still-unresolved high-value uncertainty to be considered without a recent semantic
change. The bounded activity scan, ownership checks and recommendation-only boundary
remain; there is no repeating inventory, baseline rerun, schedule or new gate.

Breadth was module-wide; depth was sampled. No all-module mutation/fuzzing run,
cross-language implementation comparison, performance certification, unsupported
storage durability experiment or exhaustive command-state exploration was performed.
Passing ordinary tests did not prevent the demonstrated parser defect. Local raw
logs, compiled mutants and probes remain ignored scratch, not maintained product
infrastructure. Product meaning continues to belong to the owning specifications.
