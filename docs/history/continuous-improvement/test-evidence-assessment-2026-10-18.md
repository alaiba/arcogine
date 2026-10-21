# Initial Java test-evidence assessment — 2026-10-18

> Dated, non-normative exploratory evidence. Assessed \`main\` at \`3b0747acdd1d807ce68e78d7adf1217b57cdb86a\` on 2026-10-18. This is a breadth-first inventory and two executed diagnostic pilots, **not** a comprehensive reliability baseline, correctness certification, supported-behavior change, or ongoing status ledger. Current architecture, specifications and testing guidance remain authoritative.

## Result

- The inventory accounted for all nine declared Java modules (seven with executable main sources; two test-only proof modules), sampling the assertions that constrain important contracts. It did not scrutinize every class, path, or state transition.
- A deterministic character-substitution experiment found that the Challenge JSON decoder accepts malformed Unicode escapes. The public Challenge content loader also accepted representative malformed content. **This is a demonstrated product defect** requiring separate remediation.
- A focused Scheduler mutation experiment showed that the retained tests detected two ordering/boundary faults but missed an injected enqueue-before-rejection side effect. The proposed change strengthens \`SchedulerTest.rejectPastTimeEvents\` so that mutant is detected. **This is a test-strengthening result, not a Scheduler implementation defect.**
- Factory binary-decoder combinations and Governance evidence-acceptance predicate combinations remain unexecuted diagnostic questions, not established defects.
- These findings demonstrate that high LINE/BRANCH coverage and passing retained tests do not, by themselves, establish assertion sensitivity or complete behavioral reliability. **Systematic, contract-wide core assurance remains to be performed.**

## Survey and evidence

Inventory derived from \`product/settings.gradle.kts\` and the tracked source/test tree at the assessed commit. Oracle descriptions below summarize inspected representative tests; they are not comprehensive test-suite certifications.

| Module / semantic boundaries | Representative proving evidence | Not established by this survey |
| --- | --- | --- |
| \`types\`: time arithmetic, typed IDs, revision identity | \`SimTimeTest\`, \`ControlledRevisionIdTest\`; exact examples, canonical round trips and invalid identity refusal | Extreme arithmetic combinations and full identifier-input exploration |
| \`simulation\`: scheduler ordering and rejection | \`SchedulerTest\`, \`SchedulerPropertyTest\`; distinguishable payload order and monotonicity across fixed seeds | General interleavings, counter exhaustion; rejected-event post-state was a discovered assertion gap |
| \`governance\`: revisions, scope, change identity | \`ControlledRevisionTest\`, \`ChangeSetTest\`; distinct occurrences and precisely scoped change lists | Full identity/scope/change-kind combinations |
| \`governance\`: evidence and conformance | \`ConformanceEvaluatorTest\`, \`GovernanceEvidenceTest\`; attribution, deterministic evaluation, rejection | Combined evidence roles, subject bindings and applicability predicates |
| \`storage\`: acceptance, reopen and recovery | \`BuiltInStorageTest\`, \`StoragePublicContractTest\`; exact bytes, independent-JVM reopen and injected failures | Process termination and unsupported filesystem failure models |
| \`domains/factory\`: canonical bytes, identity and decoding | \`FactoryModelCanonicalFormTest\`, \`FactoryModelArtifactTest\`; literal vectors, ordering, malformed records | Broader binary grammar combinations and nested count/truncation space |
| \`domains/factory\`: dispatch, runtime and replay | \`EngineDispatchConformanceTest\`, \`BoundedAdvancementConformanceTest\`, runtime acceptance suites; exact event outcomes and replay | Arbitrary command-interleaving/state-space exploration |
| \`consumer/challenge\`: validation, economics, admission, comparison | Validator, economics, admissibility and attempt-comparator tests; policy outcomes and boundary fixtures | Large combinations of invalid rules and permutations |
| \`consumer/challenge\`: JSON content loading | \`JsonTest\`, \`ChallengeContentLoaderTest\`; success and structured refusal | Unicode escape character-class gap demonstrated below; nesting limits and duplicate-key policy not assessed |
| \`consumer/challenge-factory-integration-test\` (test-only) | \`FactoryPublicationValidButChallengeInadmissibleTest\`; one counterexample to conflated validity axes | General runtime integration |
| \`research-experiments\` (non-shipped) | \`ExperimentRunnerTest\`, \`OracleContractTest\`; replay, evidence gaps and explicit refusal | All corpus families or evidence-gap arrangements |
| \`architecture-conformance-test\` (test-only) | ArchUnit \`ArchitectureTest\` and \`DeterminismGuardTest\`; selected static dependency and ambient-input prohibitions | Behavioral equivalence or every nondeterminism mechanism |

Observed combined JaCoCo counters were generated with \`./arcogine check\` and \`node .github/scripts/report-java-coverage.mjs\` from module \`jacocoTestReport.xml\` files on the assessed revision, **before** the branch-only Scheduler test change:

| Executable module | LINE | BRANCH |
| --- | ---: | ---: |
| Challenge | 94.2% | 87.8% |
| Factory | 100.0% | 99.7% |
| Governance | 100.0% | 100.0% |
| Research experiments | 100.0% | 100.0% |
| Simulation | 100.0% | 100.0% |
| Storage | 100.0% | 100.0% |
| Types | 100.0% | 100.0% |

Rounded values do not assert literal full coverage. The existing requirement is 90% LINE per executable module; BRANCH is informational, and JaCoCo does not count exception handling as branches. The full check reportedly completed 73 Gradle tasks in 5m 14s using Gradle 9.8.1 and Temurin 21.0.12.1+1 in Docker.

## Executed diagnostics

### Challenge JSON escape grammar — actual defect

A deterministic probe replaced each of four positions in \`0041\` with every UTF-16 code unit (0–65535): **262,144** cases, including 88 legal ASCII-hex substitutions and 262,056 illegal ones. The oracle was the JSON grammar: each position must be \`[0-9A-Fa-f]\`. Legal escapes must decode to the exact expected character; illegal escapes must raise \`JsonSyntaxException\`. No random seed or external fuzzer was used.

**Observed:** 1,490 illegal inputs were accepted; no wrong values were observed for legal inputs. Examples: \`"\u+041"\` and \`"\u０041"\` (fullwidth zero) decoded to \`A\`, and \`"\u-041"\` decoded to U+FFBF. Representative inputs also returned success from \`ChallengeContentLoader.load\` rather than \`content.malformed-json\`. The failure mechanism is \`Json.parseUnicodeEscape()\` applying \`Integer.parseInt(hex, 16)\` without first enforcing four ASCII hexadecimal digits.

The experiment challenged this escape character class, not general parser security, surrogate handling, nesting, input exhaustion or duplicate-key semantics. A focused separate fix needs parser and public-loader regression cases. The original detailed experiment recipe, including a standalone probe and public-loader fixture, is preserved as [PR evidence at the original assessment commit](https://github.com/alaiba/arcogine/blob/fc8bc36feb4349a448185098ce771d0dbc6d1688/docs/history/continuous-improvement/2026-10-18-test-evidence-baseline.md#pilot-1-escape-grammar-challenge).

### Scheduler rejection and ordering — test sensitivity

Against \`SchedulerTest\` and \`SchedulerPropertyTest\` (32 focused JUnit invocations), isolated Java source mutants gave the following results:

| Deliberate change | Before test-strengthening |
| --- | --- |
| Reverse same-time FIFO comparator | Detected (2 test failures) |
| Reject equal-to-current-time scheduling | Detected (2 test failures) |
| Add rejected past event to queue immediately before throwing | **Not detected** (32 invocations passed) |

The surviving fault is contract-relevant but **not present in the real Scheduler**. The proposed retained test change verifies unchanged time/size/peek and the exact subsequent event after refusal. The normal Scheduler continued to pass, while the previously surviving mutant then failed (expected queue size 1; observed 2). Mutants were deliberately hand-selected, not an all-operator mutation run or score. The complete isolated compilation/run recipe remains in [the original assessment commit](https://github.com/alaiba/arcogine/blob/fc8bc36feb4349a448185098ce771d0dbc6d1688/docs/history/continuous-improvement/2026-10-18-test-evidence-baseline.md#pilot-2-scheduler-fault-detection).

## Unexecuted questions and limits

- **Factory canonical binary decoder:** generated valid models and grammar-aware malformed count/truncation/canonicality cases could challenge the decoding contract. Not run; no defect established.
- **Governance evidence acceptance:** selected predicate mutants and combinations of role, applicability and subject mismatch could test refusal without a recorded occurrence. Not run; no defect established.
- Other core boundaries were inventoried but not systematically challenged with complementary adversarial, stateful, differential, metamorphic, fuzzing or fault-injection methods.
- The JSON defect and Scheduler assertion gap are the *only* demonstrated pilot findings. Neither a 100% coverage figure nor two pilots establishes that the retained core is comprehensively reliable.

This record is historical evidence only. Actual remediation ownership belongs to current issues/owner requests, and the testing guide governs future diagnostics. Do not reread this record as a current defect ledger or treat this exploratory assessment as completion of the broader core-reliability assurance objective.
