# Starvation and utilization inspection prototype: plan (draft for owner review)

> **Custody:** temporary research evidence for the factory-design game diagnostic-evidence
> investigation. This is a plan, not a result. It contains no scenario answers. Pre-registered expected
> values will go in a separate oracle note before anything is built.

## 1. Purpose and boundaries

- **Goal of this iteration.** Learn how a player *inspects* one recorded factory run and comes to
  understand **starvation** and **utilization** correctly, with the industry terms introduced in
  context and explained on demand.
- **Static only** (owner decision):
  - Every view comes from runs pre-computed by the research experiment and shown by the `.mjs`
    viewer.
  - "What if" means exploring pre-computed variants. There is no live simulation.
  - The viewer derives nothing; it only displays.
- **Pull, not push.** The player sees a minimal design and asks for facts by command. Nothing
  pre-answers unasked questions, and no "what this run cannot tell you" section appears. Truth
  constraints act as invisible guardrails: the viewer never offers a figure or label the evidence
  cannot license.
- **No visuals decisions.** The text UI tests concepts, not the medium.

## 2. Concepts in this iteration

**Machine time breakdown**, an industry-standard way to answer "where did each machine's time go?".
Every slot of every machine, at every tick, is in exactly one state:

| State | Meaning (research-local definition, to be pre-registered) |
|---|---|
| **Working** | the slot holds a job: from its start (`JOB_DISPATCHED`) to its finish (`JOB_STEP_COMPLETED`) |
| **Starved** | online and empty, while some unfinished unit will still need a step this machine can do, but none is waiting for it now: work exists upstream and has not reached it |
| **Idle, no work left** | online and empty, and no unfinished unit will ever need it again, or the order is complete |
| **Offline** | the machine is unavailable (not exercised by the current scenarios; the definition must still hold) |
| *Blocked* | *not modeled: the engine has unbounded queues. It is shown as a known future concept in the reference, never as a state* |

**Waiting while a slot is empty cannot happen** for an online machine: the engine dispatches
immediately. The pre-registered checks will assert that this state is never observed.

**Utilization over a period `[a, b]`** = working slot-ticks ÷ available slot-ticks, where available
slot-ticks = concurrency × (b − a), minus offline time. The choices it makes explicit:

- **Period:** the whole run by default, or any window the player picks.
- **In-progress work** counts up to the period boundary; the measure does not wait for steps to finish.
- **Multi-slot machines** count per slot, so a two-slot machine with one busy slot is 50% for that
  tick.
- **Offline time** is excluded from available time. (Whether to report it separately is an open
  choice.)
- **Per machine** by default. Per step (pooled over the machines that can do it) is a variant to show,
  because the two can rank differently.

**Teachable traps for this iteration**, all already proven in the corpus:

- **Utilization ≠ bottleneck:** a most-utilized machine where extra capacity does nothing.
- **Busy counter ≠ utilization.** The engine's completion-credited `busyTicks` reads 0 while a machine
  is working, and exceeds 100% for a two-slot machine. It appears only as a deliberate lesson, labeled
  "completed-work credit", and is scheduled for removal (section 4).
- **Period matters.** With the order released all at once, whole-run utilization includes ramp-up and
  wind-down; a window can tell a different story.
- **Starved ≠ excess capacity.** A starved machine may still be needed: the idle pair from the first
  pass, where an idle machine was needed in one design and not in the other. Excess capacity is
  shown only by the pre-computed removal variant.
- **Starved ≠ done.** Idle at the end of a completed order is not starvation (the first walkthrough's
  W1 confusion).

## 3. What this research does now versus what it delegates to analytics

| Concern | This research (temporary, research custody) | Delegated to the simulation-analytics ownership research |
|---|---|---|
| Utilization and starvation **definitions** | Research-local named derivations in the experiment, computed from supported events and observations, with every choice above stated | Where the definitions live (Engine/runtime fact, consumer-neutral analytics, or game-local), naming, versioning, and reuse across consumers |
| **Requirement specification** | The handoff's measurement-candidate table, filled in for utilization and starvation (later for bottleneck by a named method): purpose, question, inputs, basis choices, completeness, refusal, reuse need | Accepting, rejecting or generalizing it; alignment with Factory Design's "Maximum utilization" verification objective and with Governance |
| **Experiments on alternatives** | Show candidate variants side by side in the prototype (interval occupancy per machine or per step pool; whole run or window; completion credit as a trap) and record which is truthful and understandable | Selecting the shared formula, if any |
| **Evidence needs** | Use the full recorded event history (available in research) | The runtime gap: per-step timing exists only in delivered-once events. Whether the runtime retains history or adds timing to observations is a runtime ownership decision |
| **Product code** | None | None directly; it feeds the replacement decision for `busyTicks` |

This reverses the first report's analytics conclusion. The report states "no concrete reusable
measurement demand". Under the owner's educational goal, a concrete demand exists for utilization
(defined as above) and starvation, and later bottleneck by a named method. A new report revision must
say so. It must also correct the "game must keep…" items to platform requirements (runtime: per-step
timing; Factory Design: identity stability and model comparison).

## 4. The busy counter (`busyTicks`): planned removal

- **Owner decision:** remove it as part of this research's reconciliation, and experiment here on what
  takes its place.
- **Reconciliation scope.** This is a separate, independently reviewed change, not done in research:
  - remove the field from `ResourceObservation` and the accumulation in `FactoryHandler`/`Machine`;
  - remove the Engine semantics §10.1 rule 1 and the §10.2 register row (keep the saturation rule for
    lead time);
  - retire or rewrite `HeadlessClosureAcceptanceTest`'s bottleneck-by-utilization assertion and the
    `busyTicks` part of `EngineDerivedResultConformanceTest`;
  - update the runtime contract's "utilization facts", the ISA-95 mapping row and the Governance
    evidence mention.
- **Risk: high.** It changes a supported observation contract and an Engine semantics rule, so it
  needs independent review. There is no outward consumer today.
- **What replaces it** depends on section 3. Nothing in product replaces it until the analytics
  ownership decision. Research computes interval utilization from events meanwhile.

## 5. Prototype design (iteration 3)

**Pack contents.** The experiment pre-computes, per scenario and per variant:

- the design;
- the supported observation at **every tick where something changes** (state is constant in between,
  so this covers every tick);
- each unit's journey;
- each machine's time breakdown;
- utilization tables for the whole run plus the windows the scenario needs.

**Variants:** every single add/remove variant, plus the base.

**Size estimate.**

- Runs last at most about 70 ticks, with at most about 50 change points each.
- About 6 scenarios × about 5 runs ≈ 30 runs, roughly 1,500 snapshots of about 1–2 KB each.
- That is about 2–3 MB of JSON before deduplication: small for a generated, deterministic file.

**Viewer commands** (draft):

| Command | Shows |
|---|---|
| `design` | the minimal design: steps with times, machines, order |
| `result` | finish time; nothing else unless asked |
| `at <tick>` | each machine's state (working k/n, starved, idle with no work left, offline); who waits for which step; progress |
| `machine <name>` | that machine's timeline and time breakdown |
| `util [<from> <to>]` | utilization table for a period (whole run by default) |
| `unit <n>` | one unit's journey: waits and work per step |
| `variants` / `switch <variant>` / `compare` | explore the pre-computed variants against the base |
| `explain <term>` / `glossary` | in-context reference for every term used |
| `note <text>` | the owner's free-form observations, recorded with context |

**Teaching.** Terms appear in context: a state is named "starved", and `explain starved` gives the
definition, how the game decides it, and what it does *not* mean. No quizzes.

**Owner session.** An open exploration with two or three goal-oriented *missions*, for example "find
when a machine was starved and say what it was waiting for", and "which machine worked hardest, over
which period?". Notes are captured verbatim, and the result is qualitative. (The mission wording is
open for owner review.)

## 6. Scenarios

Existing designs are reused where they already carry a lesson. Hand-derived expectations will be
pre-registered for every displayed number before generation.

| Lesson | Candidate design |
|---|---|
| Starved between units downstream of a slower step | first-pass G1 or second-pass Factory 1 |
| Starved but needed, versus idle and not needed | the idle pair (removal variants show which is excess capacity) |
| Two-slot machine: per-slot utilization; the busy counter exceeds 100% | G4 (twin assembler), G11b |
| Highest utilization ≠ bottleneck | G5 (pooled-step view), G7 (co-binding) |
| Whole run versus window | any release-at-once line (ramp-up and wind-down) |

## 7. Evaluation

**Mechanical:**

- the pre-registered values are reproduced;
- every machine-tick is in exactly one state, and the states sum to the period;
- "waiting while a slot is empty" is never observed;
- "starved" never appears for a completed order or a machine with no work left;
- the busy counter is never labeled utilization;
- no utilization figure is labeled a bottleneck;
- every displayed derived number names its derivation.

**Qualitative:** the owner's exploration notes against the missions. Failure to understand still
falsifies the presentation; success is only a smoke test.

## 8. Concept coverage map and tracking destinations

Nothing listed during discovery is dropped.

| Concept | Status | Destination for tracking |
|---|---|---|
| Starvation, utilization (with machine time breakdown) | **This iteration** | This research |
| Bottleneck (by named method / experiment), shifting bottleneck, excess capacity, co-binding constraints | Candidate next iterations of this research | This research (owner to confirm) |
| Queue/WIP, lead time, finish time (makespan), step time and routing, parallel and multi-slot capacity, pooling/flexible machines, tie-break order effects, release-at-once backlog | Explorable now; not yet scheduled | This research's concept list, carried in the report revision. Unaddressed ones move to a new research question at reconciliation |
| Throughput over longer horizons, Little's Law, arrival/release patterns, demand versus capacity, machine downtime, backlog, multiple products sharing machines | Engine-supported, needs new scenarios | **New research question** (game product programme) at reconciliation |
| Blocking / finite buffers | Engine lacks it | Register CANDIDATE at reconciliation (buffers are deferred in Factory resource semantics; Factory Design evolution names buffers and congestion as separate capability questions) |
| Variability: processing time, failure/repair, arrivals, yield | Engine is deterministic | Engine evolution's stochastic boundary: promote a bounded question only when a concrete consumer needs a specific phenomenon. The game's educational need is a candidate trigger |
| Setup/changeover | Field present, ignored by the engine | Already a READY register question |
| Transfer/layout | Pending spatial semantics | Already in spatial-runtime planning |
| Lots, batches | — | Already a register question |
| Scrap/rework/quality, priorities and other dispatch rules | Engine lacks them | Programme roadmap at reconciliation (dispatch policy evolution is a concluded Engine question; quality/yield is under the stochastic boundary) |
| Live simulation loop / interactive harness | Out of scope here | **New research or planning item** at reconciliation |

The game product research programme
(`docs/research/investigations/factory-design-game-vertical-slice.md`) is the natural single place
for the full concept map, recording what the game wants to teach and which engine capability or
question each concept depends on. Register rows cover the new questions. Both are maintained
surfaces, changed only through reconciliation with review.

## 9. Open decisions for the owner

1. Approve the state definitions and the utilization choices in section 2, or adjust them (especially
   per slot, offline exclusion, and per machine versus per step).
2. Is showing the busy counter as a labeled trap useful, or should the prototype omit it entirely?
3. Mission wording for the exploration session.
4. Whether the next iteration after this one is bottleneck, as section 8 suggests.
