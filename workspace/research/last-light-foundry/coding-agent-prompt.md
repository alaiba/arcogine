# Last Light Foundry - coding-agent prompt

> Status: owner-requested design proposal and conditional execution prompt, not an admitted implementation issue or a concluded research result.
> Grounded against `alaiba/arcogine` main at `cb1afadaf683f915a31b09df2fd732da10515df4` on 2026-11-06.
> Read this with [the capability matrix](capability-matrix.md) and [the admission brief](admission-brief.md). They form one proposal packet.

## Mission

Build **Last Light Foundry**, a complete, local-first, single-player factory-optimization game with a polished browser interface, powered by the actual Arcogine Java implementation. The last orbital foundry must manufacture a rescue beacon before its station goes dark. The player buys equipment, designs production capacity, runs experiments, reads the resulting evidence, and improves the design across six increasingly demanding contracts.

The emotional arc is rescue through engineering: an initially dim station becomes visibly alive as real contracts complete. The intellectual pleasure is finding that a cheap dedicated line, a flexible shared cell, and a parallel line make different trade-offs. Deliver a game, not an engine dashboard with a fictional score pasted on top.

The outcome must be runnable from a clean checkout with documented prerequisites and one launch command, playable without editing JSON or issuing terminal requests, and demonstrably connected to the engine. No mock simulator, prerecorded successful run, client-computed production outcome, or engine source modification is acceptable.

## Admission and execution boundary

Before implementation, read the live issue supplied by the owner, its comments, `AGENTS.md`, `.github/CONTRIBUTING.md`, and `docs/development/agent-roles/implementer.md`. Resolve the exact packet coordinates cited by that issue. An issue must explicitly adopt the relevant proposed decisions and acceptance criteria below. This packet alone does not admit work; no issue is supplied with it. Without an admitting issue and owner delegation, report the admission requirement and do not create production changes, invent an issue, or self-start implementation.

Reuse that issue's single `issue/<number>-<slug>` branch. Never implement on this proposal workspace branch. Each separately delivered slice requires its own owner-opened issue; the proposed sequence in the admission brief is not authorization to create those issues. Coordinate questions and results through issue comments, not exchange files. Do not merge a PR, delete branches, self-review, or claim independent review.

After admission, execute the adopted slice rather than stopping at scaffolding or a design document. Make ordinary reversible technical choices yourself. If an actual core semantic contradiction is found, stop only the affected work and report the precise missing contract on the issue; do not patch around it or broaden the issue.

## Grounding before coding

Resolve live main and the issue branch; record both exact commits. Read the embedded `project_instructions` of an available repository snapshot before using its contents, follow its live-delta protocol, and re-ground changed paths at the exact target commit. Read the current owning contracts and their implementation tests, not only this packet.

Start with `docs/architecture/overview.md`, `engine-semantics.md`, `factory-model.md`, `factory-design.md`, `factory-resource-semantics.md`, `runtime-contract.md`, `challenge.md`, `controlled-revisions.md`, `governance-conformance.md`, `governance-evidence.md`, and `storage.md`; also read `docs/development/testing.md`. Search docs, code, and tests for `game`, `challenge`, `projector`, `bounded advancement`, `pendingMultiEligible`, `availability`, `fingerprint`, `evidence`, `workspace`, and `cross-revision`.

Read the game strategy-space, diagnostic-evidence, and vertical-slice investigations as qualified supporting research, not as implementation admission. The first-slice requirement question remains a candidate at this baseline. This packet proposes a concrete answer; it does not mark that question concluded or claim independent player validation.

Inspect `product/settings.gradle.kts`, the existing build, `FactoryModelPublisher`, `FactoryRuntimeAssembler`, `FactoryRuntime`, the supported observation/event records, the Challenge public APIs, Governance public APIs, and `BuiltInStorage`. The accompanying matrix names relevant proving tests. Reconcile the inventory with the execution checkout: add any newly implemented capability to the matrix, and report any removed or materially changed prerequisite instead of silently assuming this baseline still applies.

## Freeze the existing implementation

Treat every pre-existing production source file in these modules as read-only:

```text
product/types/src/main/
product/simulation/src/main/
product/domains/factory/src/main/
product/governance/src/main/
product/storage/src/main/
product/consumer/challenge/src/main/
```

Do not fork, copy, shadow, instrument, reflect into, or rewrite these implementations. Do not alter their semantics through replacement classes or private-state access. Record a baseline manifest and prove the protected paths are byte-identical at delivery. Preserve existing tests and quality gates; do not delete or weaken them to make the game pass.

All required new behavior belongs in outward-facing game/add-on modules. Minimal build registration, new-module dependencies, additional tests, frontend CI, documentation, and a local launcher are allowed only within the admitting issue. These integration changes are not permission to edit engine algorithms. If a necessary mechanic cannot be expressed through supported APIs, change the game design within the admitted alternatives or report the gap; never ship a parallel production simulator.

Use one-way dependencies: the game may depend on the existing APIs; existing engine/domain/Challenge modules must not depend on the game, transport, browser, or its assets. The research-experiments substrate may be used by tests, never by a shipped source set.

## Product and gameplay

### The player loop

Briefing -> construct an editable candidate -> inspect construction cost and validity -> publish an immutable design -> submit the actual workload -> run/pause/step -> inspect outcomes and evidence -> compare an alternative -> retry -> complete the contract.

A run always has a visible goal, deadline in simulation ticks, budget, progress, and an explicit success/failure explanation. Pause and thinking time are free. Difficulty must come from constrained design and shared capacity, not wall-clock reflexes, concealed rules, random punishment, or fighting the interface.

The player may buy, sell, select, move, and inspect equipment, undo/redo draft edits, save/load a draft, publish, start, pause, single-step, change presentation speed, reset to a new run, inspect jobs/orders/resources, compare completed attempts, and return to editing. Editing must never mutate the model behind an active or historical run. Provide both a guided start and an empty-draft path.

The factory floor is a readable schematic construction surface. Positions support placement and collision rules, but distance, animated flow paths, and apparent conveyors do not affect processing. Say this once clearly in the tutorial and inspector. Movement alone must not change the projected Factory model or run outcome. Do not imply implemented spatial transfer, buffers, travel times, setup penalties, material consumption, variable machine speed, or physical power simulation.

### Six authored contracts

1. **Wake the Foundry.** A short guided contract teaches placement, independent validations, publication, one quantity-bearing order, stepping, and reading completion. The player completes the first real shipment through browser controls. Supply a useful starting draft, but do not auto-complete it.
2. **The Relay.** Reproduce the grounded non-spatial reference: `CUT` 3 ticks -> `ASSEMBLE` 4 -> `INSPECT` 5; one order of quantity 12; 1,062 credits; deadline 57 ticks. Offers: cutter, concurrency 1, price 150, limit 4; assembler, 1, 200, limit 6; twin assembler, 2, 360, limit 3; inspector, 1, 250, limit 4; flex cell eligible for assembly and inspection, 1, 313, limit 3. Keep setup metadata zero and volume absent here. This is the initial mechanical calibration, not a claim that the research proved fun.
3. **Crossed Wires.** A larger authored workload and constrained catalogue make parallel versus shared capacity consequential. The final content must have at least two structurally different viable solutions and a tempting affordable losing design. Calibrate budget/deadline with real engine runs; do not invent expected times.
4. **Cold Reserve.** Begin with a declared reserve resource offline. Commission it through the supported availability command and show queued/shared waiting and recovery. An optional action demonstrates a rejected attempt to take a busy resource offline without changing state. No forced mid-job failure, preemption, drain-to-offline policy, or pretend repair simulation. Any game-owned intervention allowance is an explicit rule, not an Engine fact.
5. **Proof Before Departure.** A useful design must also carry correctly bound engineering evidence. Publish A, inspect a candidate B change set, run B, evaluate requirements, and compare attempts. Missing or mismatched evidence must not produce a green clearance. Reverting to A's content creates a new accepted revision occurrence with the original fingerprint, not the original historical identity. Departure clearance is a fictional game rule, never real certification or an Engine authorization state.
6. **Last Light.** A constrained final contract combines shared eligibility, concurrency, a declared reserve, a primary production target, and openly declared competing work. Keep the Challenge definition's one primary fixed workload intact: any independent background orders belong to the game scenario wrapper, are submitted as real separate order intents, and are included in the scenario identity and reproducibility record. Never split the primary quantity into separate orders to manufacture parallelism. Win only from authoritative primary-order completion and the mission's explicit evidence rules.

Also provide a small **Engineering Sandbox** exposing multi-product/multi-order workloads, availability, replay, and the feature inspector without campaign unlock restrictions. It is not a replacement for the six contracts.

Unlocks, mission fiction, currency, tutorial progress, commissioning allowances, camera, effects, and sound are game-owned. The terminal victory screen must be caused by real evaluated results. Standard contracts can be retried indefinitely; optional medals reward different solutions without misrepresenting the official Challenge score.

### Challenge quality

For every non-tutorial contract retain a known winning candidate, a plausible losing candidate, and their engine-derived expected results in tests. For at least two contracts retain two non-nested viable structures, with a material cost/completion trade-off and disclosed identity-order sensitivity. For The Relay, verify the 850-credit dedicated solution at 56 ticks and the 913-credit flex solution at 46 or 47 ticks under the chosen explicit identity rule; the 1,010-credit twin solution completes at 45 ticks. Reproduce rather than merely transcribe these expectations.

Include a counterexample where buying more capacity at the most occupied-looking station does not improve completion. Explain the measured before/change/after result; do not declare a causal bottleneck from one utilization or idle observation. Let the player request hints progressively. Automated balance checks establish solvability and mechanical trade-offs; only an actual playtest supports claims about player comprehension or enjoyment.

## Web experience and art direction

Use an original orbital workshop aesthetic: dark technical surfaces, warm hazard lights, luminous process traces, crisp equipment silhouettes, readable typography, and a station that visibly recovers. Prioritize legibility over particle density. Build original SVG/canvas assets or generated artwork with a coherent palette, equipment states, and icon vocabulary. Record authorship/source/license and generation provenance where relevant. Bundle assets locally; no required paid service, account, API key, or runtime CDN. Asset generation is optional; a coherent procedural/vector set is sufficient.

The main screen should have a mission/budget header, a compact equipment palette, a large pan/zoom schematic floor, a contextual inspector, and a bottom simulation timeline. A clear Engineering panel holds comparisons, revision history, requirements, evidence, and the capability inspector without overwhelming the first mission.

Animate only supported changes: dispatch, processing, completion, waiting, availability. Smooth interpolation is presentation, never authority. Use shapes/icons/text as well as color. Provide keyboard navigation, visible focus, keyboard placement/movement alternatives, readable contrast, reduced motion, mute, tooltips, and a concise control reference. Fit common laptop screens without clipped controls. Distinguish loading, invalid draft, disconnected server, rejected command, faulted run, incomplete evidence, and mission failure. Never substitute a happy animation for a missing server result.

## Add-on architecture

A suggested location is `product/consumer/last-light-foundry/`, with a Java game/server module and a `web/` client. This is a proposal, not an existing module. Register the Java module in the current build and preserve Java 21 compatibility. A TypeScript UI with SVG/canvas and a small Java HTTP adapter is a sensible default; choose the smallest maintainable stack that passes the real tests. Lock dependencies and document new frontend toolchain requirements. Do not introduce a generic plugin platform, distributed event bus, microservices, or new shared analytics domain.

Keep these responsibilities explicit even if they share one deployable:

- **Game content and draft:** mission wrappers, equipment-to-production templates, editor, history, progression, and deterministic projection.
- **Engine bridge/session driver:** public publication/runtime APIs, serialized commands, advancement, single event drainer, snapshots and transport projection.
- **Game rules and evidence:** existing Challenge operations; Governance/Storage integration; bounded game-owned comparison and presentation logic.
- **Browser:** inputs, immutable view models, animation and rendering only.

### Deterministic projection

Map every result-affecting draft field explicitly to Factory facts. Derive resources, operations, products and eligibility from versioned game templates, not from arbitrary browser data. Keep prices, floor positions, sprite names and unlocks out of canonical Factory content.

Use explicit stable numbered installation identities separate from floor coordinates. The draft records and displays the installation number used to derive `MachineId`; moving a machine or rearranging UI arrays must not change it. Preserve identities through save/load and undo/redo. Make any intentional reassignment an explicit design change. Sort projected lists by a declared rule and test independence from JSON property/map/set iteration and incidental frontend traversal. Do not hide a result-affecting tie-break inside random UUIDs, click arrival order, or rendering order.

Keep challenge admissibility, Factory validity/publication, and runtime execution results distinct. An affordable design with a missing eligible resource may pass Challenge rules but fail Factory publication. Display both reports accurately. Instantiate the runtime only from the published `FactoryModelVersion` and retain its fingerprint.

### Driving and observing the runtime

All mutation of one run must pass through one serialized Java driver. Accept explicit commands; never mutate machines, jobs, scheduler, or handler directly. Submit the primary workload once with its real quantity, preserving parent order and child job correlation. Query supported observations and drain supported events through exactly one capture owner, then fan out to browser views and evidence consumers.

Provide single-event stepping and bounded advancement. Play/pause/speed change how the driver invokes those primitives, not processing durations or the simulation clock. A time target is a bound, not a command to move the clock to that time. Do not invent an event or busy-wait when a run is quiescent or blocked. A blocked unfinished contract is not a successful completion.

When a mission contains scripted interventions, evaluate them only at declared supported command boundaries, including after each atomic advancement where needed. Store the actual ordered command/advancement transcript, trigger rule, and any seed/algorithm. Batch size or frame rate must not skip an intervention or change its effective time. No wall-clock scoring or unseeded gameplay randomness.

Preserve `Accepted`, zero-mutation `Rejected`, and post-mutation `Faulted` outcomes. Do not serialize internal `Event`, `EventPayload`, `advance()` return objects, or `CommandResult.scheduledEvents()` as the outward event contract. Project only supported semantic observations/events and supported error information.

For HTTP plus SSE, polling, or another selected transport, define commands, observations, errors, and reconnect behavior in the add-on. Preserve run identity, supported-event sequence, simulation time, model fingerprint, affected entities, and semantic payload. On reconnect obtain an atomic observation/cursor and subsequent deltas. Detect duplicate, out-of-order, missing, old-run, and wrong-fingerprint events. Bounded retention may repair a gap; otherwise resnapshot and explicitly invalidate historical claims whose evidence was lost. Never let a second consumer destructively drain the runtime.

Encode Java long values and arbitrary-precision scores/deltas losslessly, such as decimal strings, across JSON and TypeScript. Define non-finite numeric presentation explicitly instead of emitting invalid JSON or silently changing an engine value. Validate untrusted input, cap request size, bound event batches, serialize duplicate command handling, and avoid path traversal in import/export. Bind locally by default; no public deployment or authentication claim is required.

### Scoring, diagnostics, provenance, and persistence

Use the actual `ChallengeContentLoader`, validators, catalogue binding, `DraftEconomicsCalculator`, `CandidateAdmissibilityPolicy`, `ReferenceChallengeEvaluationPolicy`, `ChallengeAttempt`, and comparator. The existing reference policy is the official score; do not silently replace it with a different weighted formula. Its score trades budget against deadline margin, so faster need not mean a larger score. Label optional game medals separately.

Derive completion from the primary order's supported aggregate, not from counting animated tokens or inferring completion from quiescence. Preserve Challenge, policy, catalogue, model, run, and attempt identities as distinct values. The game wrapper must additionally bind scenario content, projection rule, ordered commands, and exact build provenance. Refuse cross-build/scenario comparisons that this prototype cannot establish, even when the core comparator's challenge/policy check would pass. A build reference is conservative provenance, not a newly invented Engine-definition identifier.

Show authoritative KPIs with their actual meanings: orders are not units; engine throughput is not automatically a user-selected interval rate. Show per-resource queues separately from shared pending work. A timeline may display captured dispatch/completion facts. Do not add utilization/starvation aggregates or causal bottleneck labels by default. A later named analytical method requires its own adopted meaning, interval/population/denominator/availability rules, evidence coverage/finality and refusal tests. Counterfactual improvement is proved by a real rerun of an explicit variant, not inferred by a client formula.

Use `BuiltInStorage.open(...).controlledRevisions()` and the existing verifier for accepted immutable blueprint history. Resolve historical artifacts through that public boundary. Exercise semantic comparison, requirement scopes, structural/evidence evaluation, and authoritative evidence/evaluation occurrence identities in the Engineering panel. Verify any game-owned link from run to accepted revision against the authority and fingerprint; runtime envelopes themselves do not carry revision identity. Never turn missing, stale, corrupt, unsupported or mismatched evidence into PASS.

Game-owned persistence stores drafts, progression, finished attempt wrappers, content/build provenance, and optional command transcripts separately from Storage's private files. Preserve identity and exact input on reload. Mandatory persistence covers drafts, campaign progress, finished attempts, and accepted blueprint history across an ordinary restart. In-memory-only proof authorities may become unavailable after restart; show that honestly and require a new evaluation rather than fabricating acceptance history.

There is no current engine checkpoint/restore contract. Reset/retry makes a fresh runtime and RunId and leaves the old run untouched. An optional resume-from-script must actually rerun the exact saved inputs/interleavings under a supported same-build basis and be labeled deterministic rerun, not checkpoint restoration or event-stream state replay. Do not serialize runtime internals, reverse-engineer Storage files, silently migrate a mismatched storage binding, or erase an unreadable save.

## Delivery and proof

Implement the relevant issue as a reachable vertical slice; the full programme ends only when the entire capability matrix and all six contracts are satisfied. Do not trade away required engine integration to finish visual polish. Keep unit, integration, and real-browser end-to-end tests in their maintained homes. Test the browser against the real Java server; isolated UI mocks cannot establish game completion.

The first slice must already launch and play a real contract. Subsequent slices deepen gameplay, operational controls, persistence/evidence, and polish rather than landing unused abstractions. Provide a self-contained local launcher, deterministic fixtures, content validation, a clear play guide, and a troubleshooting path. Run without network access after prerequisites/dependencies/assets have been installed.

At minimum validate:

```text
./arcogine check
./arcogine check --full                         when the required security tools are available
bash .github/scripts/check-repository-tooling.sh
(cd product && ./gradlew :<actual-game-module>:test)
<documented frontend install/typecheck/lint/unit/build commands>
<documented real-server browser end-to-end command>
```

Replace proposed command placeholders in maintained documentation with commands that really exist. Wire new frontend/integration tests into required CI so a green existing Java gate alone cannot certify an untested browser. Keep Java coverage at the repository's per-module 90% LINE floor; do not lower it or exclude new business logic. Report unavailable checks as unavailable, not passed.

Acceptance includes a browser recording or screenshots from the running implementation, two distinct winning designs, one explained failure, a replay comparison, availability rejection/recovery, restart persistence, proof binding/refusal, and a zero-diff check of protected source paths. Put local captures/logs under ignored `logs/` and canonical build outputs in their normal locations. Do not claim these artifacts exist before producing them.

Maintain durable game documentation and the capability/test mapping under `docs/` or the new maintained module, using semantic names. Reconcile current-state statements that no application server or web consumer exists when the implementing slice actually makes them false. Do not change Engine/Factory semantics or mark open research concluded merely because the game runs. Product source/tests must not cite this workspace or research documents as authority.

Before independent review remove tracked transient `workspace/` material from the issue branch, retain committed evidence history, and run the tracked-workspace check. Keep PR bodies to stable intent using the repository template. Converge exact-head required CI and live-base freshness under the Implementer contract, then report actual validation, current unresolved gates, how to launch/play, and limitations in the implementation completion report. Independent review and owner merge remain separate.
