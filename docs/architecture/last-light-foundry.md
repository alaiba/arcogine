# Last Light Foundry: integration design

Last Light Foundry is Arcogine's one outward consumer: a local single-player browser game in `product/consumer/last-light-foundry` (play guide: [README](../../product/consumer/last-light-foundry/README.md)). This document owns how the game uses the supported Challenge, Factory and runtime APIs. It defines no engine, Factory or Challenge semantics; where it says what a result means it points to the owning document: [Challenge](challenge.md), [Factory model](factory-model.md), [Engine semantics](engine-semantics.md), [runtime contract](runtime-contract.md).

## Dependency and ownership boundary

- The game depends on `types`, `factory` and `challenge` through their public APIs. Nothing depends on the game: `ArchitectureTest` rejects any dependency on `com.arcogine.game..` from outside it, and rejects game dependencies on scheduler internals, mutable Factory stores, and `java.lang.reflect`.
- The game adds no engine behavior. It does not fork, copy or shadow Engine, Factory, Challenge, Governance or Storage code and holds no shadow simulator. The existing production sources of those modules are unchanged by the game's introduction.
- It has no third-party runtime dependencies: the HTTP adapter is the JDK built-in server and the browser client is build-free ES modules. Node and Playwright are test-only.
- This slice does not use Governance or Storage.

## Responsibilities

| Concern | Owner in the game | Rule |
|---|---|---|
| Missions | `content.MissionLibrary` | A mission is data: a Challenge definition, an equipment catalogue and a game wrapper mapping each catalogue item to a Factory resource template. Loading goes through `ChallengeContentLoader.loadChallengeWithCatalogue`, so schema, catalogue binding and policy resolution are the Challenge module's. |
| Draft and projection | `draft.*` | The draft is untrusted client input; see below. |
| Design check | `engine.DesignChecker` | Runs Challenge economics and admissibility and, independently, Factory validation and publication. |
| Run driving | `engine.RunDriver` | One serialized driver per run owns every runtime mutation and the single supported-event drain. |
| Transport | `web.GameServer`, `web.Views` | Projects supported observations and events to JSON; no Java scheduler event or `CommandResult.scheduledEvents()` is ever serialized. |
| Browser | `src/main/resources/web` | Input, view models and rendering only. |

## Deterministic draft projection

A draft is a list of installations `{number, itemId, x, y}`. Projection to a Factory model (`DraftProjector`) is a pure function of the mission and the installations:

- A resource's `MachineId` is the installation **number**. The number is an explicit identity chosen by the player's placement, separate from the cosmetic coordinates and from the order the client lists installations in.
- Resources, and each step's eligible set, are ordered by ascending installation number. The Factory canonical form and the engine's dispatch tie-break (ascending resource identity) therefore make results independent of JSON property order, list order and floor position.
- Operation, step and product identities are fixed by the mission; coordinates, prices and names beyond the resource template never reach the Factory model.
- Moving an installation changes nothing result-affecting. Changing an installation number is an explicit change of Factory identity: the same equipment numbered differently publishes a different model fingerprint and can complete at a different tick (the Relay's flex-cell design completes at tick 46 or 47 depending on its number).

The browser's editor allocates numbers from a monotonic counter that is never reused after a sale and survives undo and redo.

## Challenge admissibility and Factory publication are different checks

`DesignChecker` returns both verdicts and never derives one from the other. A design can be admissible but unpublishable (Wake the Foundry's starter draft has no machine for the inspect step), or publishable but inadmissible (over budget or over a quantity limit). A run starts only when both pass, and only from the immutable `FactoryModelVersion` returned by `FactoryModelPublisher`; the runtime's model fingerprint is retained on every observation and event.

## The run driver

`RunDriver` is the only code that calls the runtime's mutating methods and `drainSupportedEvents()`.

- Every public method is `synchronized`: concurrent HTTP requests are applied one at a time (tested).
- The mission's single primary workload is submitted once with its real quantity. The engine creates the child jobs; the game never splits an order into several orders to manufacture parallelism.
- Commands are `step(n)` (n times `advance()`, one event each) and `advance(maxEvents, untilTick)` (`advanceUntil`). A tick is a bound, never a request to move the clock; a quiescent run invents no event and no time. A run that reaches a terminal status accepts no further engine commands.
- Play, pause and speed live in the browser as a cadence of such commands; they cannot change a result. Tests prove that single-step, count-bounded and time-bounded advancement yield identical event streams.
- Completion is derived from the primary order's supported aggregate (`OrderObservation.complete()`), never from quiescence or from counting animation. A quiescent run whose primary order is incomplete is `BLOCKED`, not a success.
- The workload command's `Accepted`, zero-mutation `Rejected` and post-mutation `Faulted` outcomes map to `ACTIVE`, `REJECTED` and `FAULTED` and are shown to the player as such; an exception from advancement is `FAULTED`.
- Reset constructs a fresh runtime from the same published version (new run identity, event sequence restarting at 1); the previous run is untouched.

## Evaluation

When the primary order completes, the driver builds `AuthoritativeOutcomeFacts(completed, completedAt)` from the observation and evaluates with `ReferenceChallengeEvaluationPolicy`; the score is the reference policy's and is displayed as such. The game does not alter or reweigh it. Attempts are recorded as `ChallengeAttempt`s and compared with `ChallengeAttemptComparator` (the first of two finished attempts is the earlier). Attempt history lives only in server memory. The game defines no optional medals in this slice.

## HTTP transport

JSON over HTTP on the loopback interface (`LLF_HOST`, default `127.0.0.1`; `LLF_PORT`, default 8551). Unauthenticated; the unsupported posture for non-loopback binding is in [SECURITY.md](../../.github/SECURITY.md).

| Route | Purpose |
|---|---|
| `GET /api/missions`, `GET /api/missions/{id}` | Contract summaries and the full briefing, equipment, identity and fingerprints. |
| `POST /api/missions/{id}/check` | The design report (economics, admissibility, publication). |
| `POST /api/missions/{id}/runs` | Publish and start; `422 design.not-runnable` with the design report when either verdict fails. |
| `GET /api/runs/{id}` | Atomic observation of the run: status, time, resources, orders, jobs, shared pending work, KPIs, evaluation, attempts and comparison, command transcript. |
| `POST /api/runs/{id}/step`, `/advance`, `/reset` | Commands; each returns the new observation. |
| `GET /api/runs/{id}/events?after=N&limit=M` | Retained supported events after cursor `N`. |

Rules:

- **Lossless integers.** Every Java `long` (ticks, identities, quantities, sequences, credits, scores) is a decimal string; the server also accepts decimal strings for integer inputs. Only presentation KPIs are JSON numbers, and non-finite doubles are the strings `NaN`, `Infinity`, `-Infinity`. The client compares sequences as `BigInt`.
- **Event identity.** Each event carries run identity, strictly increasing sequence (+1 per event within a run), simulation time, model fingerprint, affected entity references and the semantic payload.
- **Join and gap detection.** A client reads the run, then reads events from a cursor and accepts a batch only if it continues the cursor exactly; the client drops events of another run, rejects another fingerprint, ignores duplicates and treats a hole as a gap (`events.js`, tested).
- **Retention.** The driver keeps the most recent 5,000 events per run. A cursor older than the retained window is refused with `410 events.gap` (and the oldest retained sequence); the client then resnapshots and states that earlier events are not available. A cursor beyond the latest sequence is `409 events.cursor-ahead`.
- **Single drain.** Browser reads never drain the runtime; only the driver does.
- **Input validation.** JSON bodies only (`415` otherwise), at most 64 KiB (`413`), strict parsing (duplicate keys, deep nesting and malformed escapes refused), at most 64 installations with bounded numbers and coordinates, run identifiers matched as canonical UUIDs, static assets restricted to a flat allowlist of bundled file names.
- **Errors** are `{"error": {"code", "message"}}` with a matching status; an internal failure is a generic `500` that leaks no detail.

There is no OpenAPI description of this surface yet; this document is its only description, which [external representations](external-representations.md) lists as an open gap.

## Tested proofs

`RelayMechanicsTest` derives, from real engine runs, the dedicated line (850 credits, tick 56), the flex design (913 credits, tick 47 or 46 by installation numbering), the twin-assembler design (1,010 credits, tick 45) and a losing cheap line (600 credits, tick 67, deadline missed), plus replay equality after run-identity normalization, step/bounded-advancement equivalence, position and list-order independence, and the clock-bound rule. `DesignCheckerTest` proves the two independent verdicts, `RunDriverTest` the status, reset and cursor rules, `GameServerTest` the HTTP contract, and the browser tests (see the [testing guide](../development/testing.md#12-game-browser-tests)) drive the real server from a real browser for a first shipment, a Relay failure followed by an improved win, a structurally different win, speed invariance and a disconnect.

## Limits

No engine checkpoint, material transport, placement-distance consequence, resource-dependent processing speed, preemption, repair or new analytics method exists or is claimed. Saved drafts, retained attempt history, Governance evidence, reserve (availability) scenarios and the remaining contracts are not part of this slice. Playtest-based claims about enjoyment or comprehension are not made.
