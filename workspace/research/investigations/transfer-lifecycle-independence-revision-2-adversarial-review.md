# Adversarial review — transfer lifecycle independence, revision 2

## Evidence identity and disposition

- **Disposition: ACCEPT WITH QUALIFICATIONS.**
- **Reviewed report workspace:** `research/transfer-lifecycle-independence`
- **Reviewed report commit:** `0bf2515c3207b33ad4be2c453df09cae52ea539a`
- **Reviewed report path:** `workspace/research/investigations/transfer-lifecycle-independence-report.md`
- **Report's research baseline:** `07bf95cb14cc446fe02a12c88c9526a7ff205647`
- **Live-main review baseline:** `812eff79ba017c6770633aeab74249cbe7a504d3`
- **Review date:** 2026-09-24
- **Risk:** High: result-affecting interpretation, historical attribution, and the unreleased Factory grammar.
- **Authority:** Research evidence only. This review adopts no architecture, changes no product behavior, admits no implementation, and does not conclude the maintained research question.

The complete report was retrieved at its exact commit. Its Git blob is `9760a6ba5ee4c10be02c9a210caa5a30c562b8a2`; the live research branch initially pointed to the supplied report commit. The earlier report, its REOPEN review, and the original handoff remain ancestors. This review binds only to revision 2. The earlier review remains evidence about revision 1.

### Independence and anchoring limitation

This is a different review session from the report's research run. It authored neither report revision, the earlier review, nor the research handoff, and has no responsibility for preserving their conclusions. It is an independent challenge of another run's report, not the author's self-challenge. No claim of a different model family is made.

The ideal anchoring sequence was **not fully achieved**: preliminary handoff verification in this session fetched the report and exposed its recommendation and parts of its analysis before the user requested the review. That exposure cannot be undone. For the substantive review, the reviewer returned to the maintained brief and live authorities, reconstructed the constraints and candidate families, and then evaluated the detailed report. The reconstruction below is independent of authorship but must not be represented as blind or wholly unanchored. No synthesis-seed material was used.

## Reconstructed constraints and attempted counterexamples

The [transfer brief](../../../docs/research/investigations/transfer-semantics.md), [Factory evolution contract](../../../docs/architecture/factory-design.md#111-semantic-evolution), [Factory V2 grammar](../../../docs/architecture/factory-model-v2.md), [Engine v1](../../../docs/architecture/engine-semantics-v1.md), and [Determinism Contract](../../../docs/architecture/overview.md#determinism-contract) establish the following constraints without selecting an absent-case meaning:

1. Factory records authored content; absence is not an authored zero. V2 currently has either no spatial record or one complete record, with no geometry-only or handling-only variant.
2. Engine owns interpretation, including lifecycle, binding, reservation, ordering, and refusal within its defined input domain. Factory validity does not imply Engine executability.
3. No-transfer, a separately scheduled zero-duration lifecycle, and artifact refusal must be evaluated separately. Equal isolated completion time does not establish equal behavior.
4. Accepted artifact support must be established before runtime mutation. An execution that happens to select the same resource cannot retroactively justify admission under an artifact domain that excluded the model.
5. Model identity, Engine identity, publication support, execution support, and historical attribution are distinct. A future interpretation may differ without inventing authored facts or rebinding an old identity.
6. Current V1 execution is no-transfer. That fact does not by itself settle whether a fixed whole Engine definition admits either V2 form.

The candidate families reconstructed from these constraints were explicit no-transfer on absence; Engine-defined zero-duration lifecycle on absence; artifact-domain refusal; and a separately authored non-spatial hand-off assertion, with an explicit rule for missing timing. The adversarial cases included a same-resource-only artifact rejected by a present-only Engine, a multi-eligible route whose actual destination depends on commands, an offline command between same-time turns, an event-count bound placed on the wrong side of step completion, a future geometry-only grammar, and withdrawal of V1 publication without withdrawal of V1 execution.

## Evidence and baseline audit

### Repository evidence

Live `main` differs from the report baseline only by the retrospective tooling/process change in commit `812eff79ba017c6770633aeab74249cbe7a504d3`. The exact diff touches ten files in retrospective tooling, its tests, repository-tooling invocation, and process/reviewer guidance. It does not alter the Factory, Engine, product, planning, or research semantics used here. Between the report baseline and report commit, only the three research workspace artifacts changed. The review therefore uses current authorities without treating the workspace branch as landed truth.

The inspected semantic neighborhood included:

- [Architecture Overview](../../../docs/architecture/overview.md), [Factory Design](../../../docs/architecture/factory-design.md), [Factory V2](../../../docs/architecture/factory-model-v2.md), [Engine v1](../../../docs/architecture/engine-semantics-v1.md), and [runtime observation/event contract](../../../docs/architecture/runtime-contract.md).
- [Product Charter](../../../docs/product/charter.md), [semantic support policy](../../../docs/development/semantic-contract-support.md), [research register](../../../docs/research/research-register.md), the transfer brief, and the [successor applicability brief](../../../docs/research/investigations/engine-applicability-after-transfer-boundary.md).
- Relevant transfer/publication sections of the Factory-design, Engine-readiness and [spatial-runtime plans](../../../docs/planning/spatial-runtime-consequences.md); the [game research programme](../../../docs/research/investigations/factory-design-game-vertical-slice.md); bounded searches of operational planning, operational continuity, ISA-95 mapping, and standards alignment.
- The [composition decision record](../../../docs/history/decisions/2026-09-23-factory-model-semantic-composition.md) as historical rationale, not normative authority.
- Factory runtime, handler, supported-event taxonomy, V2 model/validation, Engine identity, and focused conformance/session/model tests listed below.

Searches for transfer, hand-off, reservation, spatial absence, production-only support, transfer events, and the timing fields found no in-repository consumer requirement that changes the report's candidate ranking. The operational plan's actual path is `docs/planning/operational-execution-digital-twin-readiness.md`; it contains no transfer/spatial/reservation requirement. There is no `docs/reference/` directory at the examined baseline. Consumers outside the repository were not inspected. These are bounded search results, not proof of universal absence.

The implementation inventory is materially correct:

- `FactoryHandler.handleTaskEnd` releases the source and starts an admissible next step in the same handler turn; otherwise it enters the established waiting path. There is no transfer reservation or runtime phase.
- `FactoryRuntime.setMachineAvailability` rejects taking a resource with active jobs offline before mutation. `advanceUntil` processes a next event whose time **equals** its target; only a later event is excluded.
- `RuntimeEventType` has no transfer events. Engine v1's transfer state, reservation, arrival and event requirements are specified behavior awaiting implementation.
- `FactoryModelV2` is structurally separate from the V1 publication/runtime input. Its tests distinguish absent spatial content from present content with zero magnitudes and reject incomplete present content.
- `EngineSemanticsVersion` and runtime identity tests retain `engine-semantics:v1`; the dispatch fixtures pin the current V1 production behavior.

### External source check

Re-fetched [Simio and Simulation: Modeling, Analysis, Applications, 7th edition, chapter 4, Table 4.1](https://textbook.simio.com/SASMAA7/ch-first-model.html) on the review date. The table supports the narrow contrast between an explicitly modeled zero-time Connector and Path/TimePath movement. This supports the possibility of explicitly authoring hand-off structure. It establishes neither Arcogine's absent-case rule nor demand for C4.

The report's broader assertion that Simio has no no-transfer mode other than disconnecting objects is not established by that table and is not used here. No product-wide negative inference should survive from it. The transportation literature remains unverified background and carries no decision weight. Additional external citations would not decide the repository's support promise or the present consumer requirement.

## Challenge results

| Challenge | Evidence and result | Effect |
|---|---|---|
| C2 still secretly treated as synthesized Factory data | Revision 2 explicitly permits an Engine-defined zero on represented absence. Factory and determinism authorities support this distinction. | Earlier finding 1 is resolved at the main candidate-selection level. C2 remains viable. |
| Refusal still rejected for a valid artifact | The report now treats C3b as a support choice and finds no present requirement to execute V2-absent. | Earlier finding 2 is resolved; the support implications still need qualification below. |
| An authored hand-off fact without timing was omitted | C4-z and C4-t are now evaluated; the absent flag still requires its own meaning/support decision. No current demand was found. | Earlier finding 3 is resolved. Deferral is defensible, not a proof of redundancy. |
| Conservative ranking disguises necessity | The report openly makes a medium-confidence trade-off. Extra absent-case lifecycle behavior is a real semantic surface, although it does not prove more implementation code or greater measured runtime cost. | C3a survives as a bounded recommendation, subject to qualification 1. |
| No transfer is equivalent to zero-duration transfer | Engine v1 §§9.6, 12 and current command rejection support W1 and W3. The zero-transfer execution remains specification-derived. | Distinction survives. W2's setup must be corrected under qualification 4. |
| Same-resource continuation forces artifact acceptance | A present-only Engine can reject an absent-record artifact even when every routing is same-resource. | Lifecycle and support must stay separate in the matrix; qualification 3. |
| Future geometry necessarily gives the wrong ownership | A future Engine can explicitly interpret geometry-only presence without pretending timing was authored. | The universal elimination claim is too strong; qualification 1. |
| Ending V1 publication forces V2-absent execution | Publication and execution are separately scoped promises; no unconditional replacement-support obligation was found. | The proposed forced consequence does not follow; qualification 2. |
| Current code proves V2 applicability or authorizes rewriting fixed v1 | It does neither. The runtime accepts only the existing V1 publication type, and the whole-definition test remains open. | No identity allocation, amendment, or runtime activation is authorized. |
| Stale baseline changes the answer | Exact baseline diff has no relevant semantic change. | No baseline invalidation. |

The revised successor handoff resolves the earlier finding 4 in its central respect: C3a is recommended rather than falsely deduced, refusal/support is open, and the handoff is conditional on review and reconciliation. Its statements must still be bounded by the following qualifications.

## Qualifications required for durable reconciliation

### 1. Do not promote the preferred discriminator into a universal ownership law

**Challenged locations:** case 9; invariants I-B and I-E; “What did not survive”; durable consequences item 1.

The current V2 grammar cannot discriminate a rule keyed on the complete spatial record from a rule keyed on its transfer-timing inputs: they select the same represented artifacts. The proposed C3a interpretation is coherent, but current evidence does not prove that every future authored transfer lifecycle must have authored interval inputs, or that an Engine rule conditioned on geometry would invert ownership.

Counterexample: a future closed grammar admits geometry with no handling magnitudes; a distinguishable Engine explicitly treats its presence as a zero-duration hand-off while preserving the absence of those magnitudes. This can be a poor or unnecessary design, but it does not make Factory own the runtime lifecycle. The Engine still owns the condition and interpretation, exactly as C2 does for completely absent facts. C4-z is another reason not to identify authored lifecycle applicability with an authored duration representation.

**Required qualification:** reconcile C3a as the selected interpretation for the bounded current content cases. Treat the current implicit coupling's defect as missing explicit Engine meaning, not proof that geometry-conditioned interpretations are universally forbidden. Preserve future C2/C4/geometry-only alternatives as explicitly revisitable designs requiring their own need, support, grammar and identity decisions. A later timing record is not automatically admitted or assigned meaning by this report.

This narrows overgeneralized wording; it does not overturn the present no-new-abstraction choice. The report's own recognition that anti-synthesis does not select an absence meaning should govern these later sections too.

### 2. Keep publication withdrawal separate from execution and from new support promises

**Challenged locations:** case 8's “only while V1 publication continues”; the decision criterion's “some identity must execute V2-absent”; the proposed C3b consequences.

Stopping V1 publication does not itself stop executing already-published V1 artifacts. Nor does it, without an applicable consumer/support obligation, require a new Engine to execute V2-absent. The [semantic evolution rules](../../../docs/architecture/overview.md#semantic-evolution-and-support) and Factory §11.1 deliberately separate these promises.

A coherent counterexample is an explicitly authorized support transition that stops new V1 publication, retains execution of supported existing V1 artifacts, executes only V2-present, and declines new production-only execution. Whether that would be a good product decision is separate; the report found no promise that mechanically rules it out. An actual existing commitment to new production-only execution would have to be honored or explicitly transitioned, not silently erased.

**Required qualification:** say that withdrawing V1 publication removes that publication route for **new** production-only designs. Jointly reconsider Factory composition, admission needs and Engine support. V2-absent execution becomes necessary only if the chosen/owed support scope requires those designs to remain executable. Historical V1 execution has its own declaration. The composition reopening trigger is an obligation to reassess the trade-off, not a pre-decided answer to that reassessment.

### 3. Apply the support predicate before the runtime control cases

**Challenged locations:** case 1 (“All candidates”, “absent facts are irrelevant”); the case-by-candidate summary; I-D and the static-refusal handoff.

The no-transfer same-resource rule is a control case **for an admitted execution**. Under the report's own C3b option (i), a V2-absent artifact with only `M1 -> M1` routings is refused before a run exists. Option (ii), a narrower artifact predicate, may admit that same artifact. Thus C3b does not unconditionally produce case 1's no-transfer execution.

**Required qualification:** give refusal priority in the matrix. First apply the exact identity's artifact support predicate; only admitted artifacts receive the lifecycle cases. For them, same-resource continuation has no transfer under the investigated rules. A multi-eligible route's eventual same-resource outcome never rescues a previously unsupported artifact.

The report's static-predicate reasoning is sound **for the artifact-applicability boundary under discussion**: do not wait for a distinct-resource continuation to discover that the already-running artifact was unsupported. It is not a universal claim that all runtime refusal must depend only on model content. Workload/command preflight and their explicit reproducibility inputs remain separate, already supported refusal boundaries under Engine v1 §1.2.

### 4. Correct the bounded-stepping witness and narrow the transferability claim

**Challenged locations:** W2 and “Transferability and reuse”.

W2 says to call `advanceUntil(t=5, maxEvents=1)` **right after step 1 ends** and expects a zero-duration run to remain `TRANSFERRING`. The code's target-time comparison is inclusive: if `TRANSFER_COMPLETED` at t=5 is now the next event, that call processes it. With the stated otherwise-idle model, the job starts processing on M2. The claimed intermediate-state result does not follow from the written setup.

**Corrected reusable witness:** start two candidate runs with unary online M1 and M2, route `M1:5 -> M2:3`, and one submitted work item. Before processing step 1's pending `TaskEnd` at t=5, call `advanceUntil(SimTime.of(5), 1)`. The one-event budget consumes that task-end turn. Under no-transfer the item is processing on M2; under the specified zero-duration lifecycle it is `TRANSFERRING`, with completion pending at t=5. Observe or issue the offline command at that boundary. Do not first perform W1's `advance()` and then use that already-advanced state as W2's setup.

This is a repair of the fixture specification, not a falsification of the underlying distinction: W1 and W3 remain independent supporting witnesses, and the corrected W2 follows the existing bounded-step contract.

For reuse outside Arcogine, interleaving or bounded stepping can make a zero-duration phase observably different. Conversely, atomic processing of all same-time events is **not sufficient to prove equivalence**: a supported event stream may still expose extra transfer events, or internal ordering may still change other results. Outside the report's sufficient conditions, equivalence must be assessed over that engine's actual observations, events, commands and ordering. Do not preserve the current boundary sentence as a universal equivalence claim.

## Surviving decision and reconciliation boundary

The central recommendation survives: for currently represented absent content that an Engine deliberately executes, choose explicit no-transfer behavior as the conservative current interpretation; defer a new authored hand-off grammar until a concrete need justifies it. This is a medium-confidence selection among viable designs, not a deduction from the anti-synthesis rule. No current evidence forces a V2 grammar change.

The high-confidence results are narrower: no-transfer, the specified zero-duration lifecycle and refusal are distinct; Engine owns their meaning and support; Factory preserves authored absence versus zero; historical V1 execution under its attributed interpretation cannot silently change.

Durable reconciliation must carry all four qualifications. It must state what current transfer contract is actually being adopted before declaring the upstream question concluded or re-promoting the successor. The successor then assesses exact Engine identity/support and whole-definition preservation against that reconciled contract; it must not inherit an unresolved choice disguised as a settled invariant. C2 and C4 remain alternatives at the stated reopening triggers, not evidence that the current recommendation is necessary.

This review does not authorize changing the fixed `engine-semantics:v1` definition merely because current code has no transfer. Whether recording the V1 rule is a behavior-preserving correction, and whether v1 can admit any V2 case, remain the stated whole-definition questions. Neither V2 publication nor runtime transfer activation is released by this review. Reconciliation remains a separate change with normal independent PR review on the same evidence workspace by default.

## Validation and limits

The focused existing tests were run through the repository wrapper in the documented `gradle:9-jdk21` Docker environment, mounting this isolated worktree's `product/` at `/app`. The successful wrapper command, from `product/`, was:

```bash
./gradlew :factory:test \
  --tests com.arcogine.factory.process.SessionControlAcceptanceTest \
  --tests com.arcogine.factory.process.EngineSemanticsV1DispatchConformanceTest \
  --tests com.arcogine.factory.process.EngineSemanticsIdentityAcceptanceTest \
  --tests com.arcogine.factory.process.HeadlessClosureAcceptanceTest \
  --tests com.arcogine.factory.model.v2.FactoryModelV2Test \
  --tests com.arcogine.factory.model.v2.FactoryModelV2ValidatorTest \
  --no-daemon
```

**Result:** 82 tests, zero failures, zero errors, zero skips; all nine Gradle tasks executed. Counts were checked from the six generated JUnit XML reports. These tests exercise current V1 dispatch/session/provenance and V2 shape/validation; they do not execute transfer behavior.

The first attempt used the running devcontainer's main checkout after verifying its tracked product tree matched the review baseline. It failed before tests at `:types:compileJava`: Gradle could not replace existing class files while loading a build-cache entry. That attempt is an incomplete validation, not a pass. The same focused tests then passed on this separate worktree using the documented generic Docker fallback; no product files were modified and no shared build files were manually removed.

`node .github/scripts/check-markdown-links.mjs .` passed, including the new workspace review. Staged whitespace validation uses `git diff --cached --check`. Full Java style/coverage/security gates were not run: this change adds research prose only. Transfer execution is not implemented, so the transfer side of W1–W3 is specification-derived; no test run is represented as executable proof of that future behavior. No new mirrored-model test or substitute transfer implementation was introduced.

Immediately before persistence, GitHub still reported live `main` at `812eff79ba017c6770633aeab74249cbe7a504d3` and the research branch at the reviewed report commit. The research branch was not synchronized with the unrelated main change. This artifact is appended without amending or rewriting any earlier evidence.

## Final disposition

**ACCEPT WITH QUALIFICATIONS.** The revised conservative selection survives. The qualifications correct scope, support consequences and proving-case precision; they do not introduce a newly omitted current candidate or falsify the selected current absent-case meaning.
