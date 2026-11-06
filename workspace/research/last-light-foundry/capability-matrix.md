# Last Light Foundry - capability and acceptance matrix

> Proposed acceptance contract, not a claim that the game or these tests already exist.
> Baseline: `cb1afadaf683f915a31b09df2fd732da10515df4`.

## Meaning of all features

Cover every implemented, supported semantic capability of the present engine and its Factory, Challenge, Governance, Storage, and shared-type APIs. Gameplay and the Engineering panel exercise normal paths; deterministic tests exercise boundary/error paths that should not be inflicted on ordinary players. Merely importing a class, showing a static label, or listing an existing unit test is not proof that the game integrates the capability.

Inspect the actual execution checkout and extend this inventory for newly landed capabilities. Internal collections, implementation helpers, research-only oracles, benchmarks and every possible enum value are not separate mandatory game mechanics. Future architectural ambitions are not implemented features. Reuse existing proving tests for baseline semantics and add integration evidence for the game's use of those semantics.

Each maintained coverage row must name: actual API/contract, player action, observable consequence, implemented test, evidence artifact, and any qualification. No silent omission or unsupported 'covered' checkmark is acceptable.

| Capability | Reachable game use | Required proof |
|---|---|---|
| Typed identity/time/error boundaries | Inspect a selected resource, order and child job | IDs correlate end-to-end; ticks/longs survive wire round trips above JavaScript's safe integer range; errors stay structured |
| Canonical Factory model, validation and publication | Publish a draft | Invalid eligibility, references and values fail through real Factory validation; published snapshots remain immutable |
| Canonical artifact/fingerprint/verifier | Inspect and export an accepted blueprint | Exact same content round-trips; meaningful changes affect identity; malformed/tampered bytes fail rather than becoming a different silent model |
| Stable projection and authored ordering | Move, save/load, undo/redo and deliberately reassign an installation | Cosmetic positions and collection traversal do not alter simulation; explicit ID/list-order changes remain visible and tested |
| Resource metadata | Inspect name, concurrency, optional volume and setup metadata in Sandbox | Metadata is preserved and comparable; setup/volume never advertised as current performance levers |
| Routing, multiple products and step precedence | Inspect routes and submit primary plus declared independent competing work | Supported traces prove each child follows its route, with no invented split/merge/transfer behavior |
| Quantity decomposition and immutable order intent | Submit one mission order with quantity greater than one | One parent order, N engine-created child jobs and ordinals; one aggregate completion; no fabricated parallel orders |
| Concurrency and shared eligibility | Compare dedicated, twin and flex designs | Multiple slots and cross-step sharing actually execute; capacity is not a cosmetic stat |
| Single-resource FIFO versus shared backlog | Inspect waiting work in a constrained run | Separate visible counts; shared jobs counted once, not duplicated across eligible queues; assignment follows the engine |
| Deterministic dispatch and recovery ordering | Replay a flex design | Supported event order/terminal state agree after permitted RunId normalization; no client scheduling or hidden priority override |
| Scheduler ordering and same-time events | Step through simultaneous completions | Supported sequence ordering preserved at equal tick; internal scheduled events never used as UI truth |
| Single-step and bounded advancement | Step, play, pause and change speed | Different rendering frame rates/batch sizes preserve results for the same effective command transcript; target guards do not invent time |
| Availability and recovery | Commission reserve; attempt to disable a busy resource | Real accepted change and waiting recovery; busy-offline rejection has zero mutation; redundant command is a no-op |
| Accepted/rejected/faulted commands | Show actionable status and a fault detail view | Fault after mutation is not shown as rejection; duplicate request handling never repeats workload or masks unknown outcome |
| Reset and quiescence | Retry and inspect the previous attempt | Fresh RunId/sequence epoch, old runtime unchanged; no pending work is not automatically victory or terminal prohibition on future commands |
| Supported observations/events | Reconnect the browser and inspect the event timeline | Single drainer; atomic snapshot/cursor join; correct run/fingerprint; gap/duplicate/out-of-order detection and honest historical refusal |
| Engine-derived KPIs | View completion, backlog, value, lead time, throughput | Values agree with runtime observations; order/quantity units and numerical edge cases are not silently rewritten |
| Challenge definition/content/catalogue | Load missions and equipment | Real loader, validators and catalogue fingerprint binding; invalid JSON/content, unsupported policy and limits fail explicitly |
| Economics and admissibility | Buy/sell/place equipment and publish | Costs/limits/budget/overlap/bounds enforced; overflow refused; challenge admissibility remains independent of Factory validity |
| Challenge evaluation and attempt history | Finish, fail and compare two attempts | Real reference policy, exact score/differences, immutable recorded basis; failure not inferred from UI clock; incompatible attempts refused |
| Controlled revisions and artifact authority | Publish A, B, and a revert of A | Accepted occurrence identities and lineage; equal content does not reuse occurrence identity; fabricated or mismatched bindings rejected |
| Semantic change and impact scope | Review A versus candidate B and accepted B | Real comparator/ChangeSet orchestration; add/remove/modify/order effects and scope retained; candidate versus accepted provenance distinct |
| Requirements, assertions and structural conformance | Engineering checklist for an adopted mission requirement | Actual evaluator produces PASS, FAIL, UNKNOWN and NOT_APPLICABLE for appropriate cases, with independently derived state fingerprint |
| Evidence references, applicability, use and relations | Attach run evidence and inspect why it supports or cannot support a requirement | Real authority acceptance and provenance; correct revision/use binding; stale/missing/conflicting evidence never auto-passes; relation type is explicit |
| Evaluation occurrences | Inspect two separately performed evaluations | Separate occurrence identity even for equal judgments; revision/evidence basis preserved; in-memory lifetime disclosed |
| Built-in persistent revision history | Restart and reopen the blueprint library | Public Storage entry points only; same-binding separate-process reopen; unknown/corrupt/foreign/mismatched store fails without repair or deletion |
| Add-on save/reproducibility wrapper | Reload a draft/finished attempt and rerun a saved experiment | Preserved explicit inputs, scenario/build/projection binding; no checkpoint claim, no cross-build reinterpretation, no supported-event state replay |
| HTTP projection and external representation | Use the web UI through the shipped adapter | JSON projects supported semantics without defining fingerprints; establish generated/validated OpenAPI for the introduced HTTP surface per the existing external-representations contract, not a separately hand-maintained competing schema |
| Core immutability and module ownership | Launch the shipped game through its real entry point | Protected-source diff empty; architecture tests deny inward game/UI dependencies, internal access and production research-substrate imports |

## Negative and integration scenarios

Derive focused tests from current conformance fixtures, not from assumptions in this proposal. Read these existing families first:

- Factory: `FactoryModelPublisherTest`, `FactoryModelCanonicalFormTest`, `FactoryModelArtifactTest`, `FactoryRuntimeBoundaryAcceptanceTest`, `IntraOrderExecutionAcceptanceTest`, `MultiResourceDispatchAcceptanceTest`, `SessionControlAcceptanceTest`, `BoundedAdvancementConformanceTest`, `RuntimeEventDeliveryAcceptanceTest`, `RuntimeObservationAcceptanceTest`, `EngineDispatchConformanceTest`, `EngineDerivedResultConformanceTest`, and `OrderIntentSeparationTest`.
- Challenge: `ContentDrivenAttemptComparisonTest`, `CandidateAdmissibilityPolicyTest`, `DraftEconomicsCalculatorTest`, `ReferenceChallengeEvaluationPolicyTest`, `ChallengeAttemptComparatorTest`, and `FactoryPublicationValidButChallengeInadmissibleTest`.
- Governance/Storage: `ChangeSetFactoryTest`, `ConformanceEvaluatorTest`, `EvidenceUseRevisionBindingTest`, `GovernanceEvidenceTest`, `PreChangeConformanceProvingCaseTest`, `StoragePublicContractTest`, `BuiltInStorageTest`, and `StorageReopenProbe`.
- Research test-only reference: `CapacityCorpus`, its tests, and diagnostic negative cases. Do not add a production dependency or copy a research oracle into production as an unapproved analytical definition.

Prove at least these end-to-end paths with the real server: new browser profile -> tutorial shipment -> failed design -> meaningful improvement -> two completed attempts compared; reserve activation and rejection; save/restart/reopen; stale or missing evidence; disconnect/resnapshot; reset with old-run messages arriving; final mission win. Recompute server-side publication/admission/evaluation inputs; browser-supplied scores, arbitrary eligibility or completed flags are untrusted.

Exercise quantity and time boundary failures through suitable tests, without promising an interactive 100,000-token display or inducing integer exhaustion during normal play. Browser rendering may aggregate/cull work-item graphics but must preserve authoritative totals and drill-down access. Record benchmark workload, hardware and measurements for performance claims rather than inventing an FPS guarantee.

## Explicitly absent at this baseline

There is no shipped web adapter, production draft projector, simulation checkpoint/restore, spatial transfer, resource-dependent speed, executable setup/changeover, material lot split/merge, core utilization/bottleneck metric, real-operation integration, or mature authorization/certification workflow. The new web bridge and game projector are concrete add-ons; the other absences are non-goals, not permission to fake engine capabilities.

`capacityLiters` and `setupTime` may be authored metadata but are not present processing levers. The core Challenge comparator checks challenge/policy identity, not complete cross-build comparability. The core runtime has no authoritative controlled-revision binding or Engine-definition identifier. Built-in Storage persists accepted controlled-revision artifacts, not arbitrary game sessions or every Governance authority. Preserve these distinctions.

## Source map

Current contracts: [Factory model](../../../docs/architecture/factory-model.md), [Engine semantics](../../../docs/architecture/engine-semantics.md), [Runtime boundary](../../../docs/architecture/runtime-contract.md), [Challenge](../../../docs/architecture/challenge.md), [Controlled revisions](../../../docs/architecture/controlled-revisions.md), [Governance](../../../docs/architecture/governance-conformance.md), [Evidence](../../../docs/architecture/governance-evidence.md), [Storage](../../../docs/architecture/storage.md), and [External representations](../../../docs/architecture/external-representations.md).

Qualified supporting evidence: [Strategy-space reference](../../../docs/research/investigations/factory-design-game-strategy-space.md), [Diagnostic guardrails](../../../docs/research/investigations/factory-design-game-diagnostic-evidence.md), and [First-slice research status](../../../docs/research/investigations/factory-design-game-vertical-slice.md). These remain supporting evidence, not implementation authority.
