# Starvation and utilization inspection prototype: plan (draft for owner review, revision 2)

> **Custody:** temporary research evidence for the factory-design game diagnostic-evidence
> investigation. This is a plan, not a result. It contains no scenario answers. Pre-registered expected
> values will go in a separate oracle note before anything is built.
>
> **Revision 2 (owner feedback):**
> - machine state and flow characterization are separated;
> - offline time is out of scope;
> - the engine's completion-credited busy counter is dropped from this iteration entirely (its removal
>   is tracked in the discovery notes as a reconciliation item).

## 1. Purpose and boundaries

- **Goal of this iteration.** Learn how a player *inspects* one recorded factory run and comes to
  understand **starvation** and **utilization** correctly, with the industry terms introduced in
  context and explained on demand.
- **Static only:**
  - Every view comes from runs pre-computed by the research experiment and shown by the `.mjs`
    viewer.
  - "What if" means exploring pre-computed variants. There is no live simulation.
  - The viewer derives nothing; it only displays.
- **Pull, not push.** The player sees a minimal design and asks for facts by command. Nothing
  pre-answers unasked questions. Truth constraints act as invisible guardrails: the viewer never offers
  a figure or label the evidence cannot license.
- **No visuals decisions.** The text UI tests concepts, not the medium.
- **Scope guard:** every machine is online for the whole run in every scenario. Machine availability
  (offline/online) exists in the engine as a runtime command issued at a moment, not as part of the
  factory design. It stays out of this iteration and belongs with "machine downtime" in the coverage
  map.

## 2. Concepts in this iteration: two layers

The owner pointed out that "starved" is not a machine state. These are two different things.

### Layer 1: machine state

What a machine slot is doing, read directly from the simulation at any moment:

| State | Meaning |
|---|---|
| **Working** | the slot holds a job, from its start (`JOB_DISPATCHED`) to its finish (`JOB_STEP_COMPLETED`) |
| **Idle** | the slot holds nothing |

### Layer 2: flow characterization of an idle slot

*Why* is the slot idle? This is computed from the state of the **whole system**, not the machine
alone:

| Characterization | Meaning (research-local definition, to be pre-registered) |
|---|---|
| **Starved** | some unfinished unit will still need a step this machine can do, but none is waiting for it now: work exists upstream and has not reached it |
| **No work left** | no unfinished unit will need this machine again, or the order is complete |
| *Blocked* | *a finished unit cannot move on. Not modeled (the engine has unbounded queues), so it is a reference entry for a future concept, never a computed value* |

A third combination, **idle while a unit waits for this machine**, cannot occur: the engine starts work
immediately when a slot is free. The pre-registered checks will assert that it is never observed.

This split also follows ownership. Layer 1 is a direct engine fact. Layer 2 is a derived interpretation
of the whole system, which is exactly the kind of definition whose long-term home the analytics
research decides (section 3). It also matches how industry equipment-state models are commonly
organized: a small set of states, with idle time attributed to reasons such as "no material". That is
background knowledge, not verified against a specific standard in this investigation.

### Utilization over a period `[a, b]`

Utilization = working slot-ticks ÷ (concurrency × (b − a)), built on layer 1 only. The choices it
makes explicit:

- **Period:** the whole run by default, or any window the player picks.
- **In-progress work** counts up to the period boundary; the measure does not wait for a step to finish.
- **Multi-slot machines** count per slot: a two-slot machine with one busy slot is 50% for that tick.
- **Per machine** by default. Per step (pooled over the machines that can do it) is a variant to show,
  because the two can rank differently.

The **idle share** of a machine's time splits further by layer 2 into starved time and no-work-left
time. Over a period, working + starved + no-work-left = 100% of each machine's slot-time.

### Teachable traps, all already proven in the corpus

- **Utilization ≠ bottleneck:** a most-utilized machine where extra capacity does nothing.
- **Period matters.** With the order released all at once, whole-run utilization includes ramp-up and
  wind-down; a window can tell a different story.
- **Starved ≠ not needed.** In the first pass's idle pair, an idle machine was needed in one design and
  not in the other. Only the pre-computed removal variant shows which.
- **Starved ≠ no work left.** Idle at the end of a completed order is not starvation (the first
  walkthrough's W1 confusion).

## 3. What this research does now versus what it delegates to analytics

| Concern | This research (temporary, research custody) | Delegated to the simulation-analytics ownership research |
|---|---|---|
| Utilization and flow characterization **definitions** | Research-local named derivations in the experiment, computed from supported events and observations, with every choice above stated | Where the definitions live (Engine/runtime fact, consumer-neutral analytics, or game-local), naming, versioning, and reuse across consumers |
| **Requirement specification** | The handoff's measurement-candidate table, filled in for utilization and starvation (later for bottleneck by a named method): purpose, question, inputs, basis choices, completeness, refusal, reuse need | Accepting, rejecting or generalizing it; alignment with Factory Design's "Maximum utilization" verification objective and with Governance |
| **Experiments on alternatives** | Show candidate variants side by side (per machine or per step pool; whole run or window) and record which is truthful and understandable | Selecting the shared formula, if any |
| **Evidence needs** | Use the full recorded event history (available in research) | The runtime gap: per-step timing exists only in delivered-once events. Whether the runtime retains history or adds timing to observations is a runtime ownership decision |
| **Product code** | None in this iteration | None directly |

This reverses the first report's analytics conclusion. The report states "no concrete reusable
measurement demand". Under the owner's educational goal, a concrete demand exists for utilization
(defined as above) and starvation, and later bottleneck by a named method. A new report revision must
say so. It must also correct the "game must keep…" items to platform requirements (runtime: per-step
timing; Factory Design: identity stability and model comparison).

## 4. Prototype design (iteration 3)

**Pack contents.** The experiment pre-computes, per scenario and per variant:

- the design;
- the observation at **every tick where something changes** (state is constant in between, so this
  covers every tick);
- each unit's journey;
- each machine's working, starved and no-work-left breakdown;
- utilization for the whole run plus the windows the scenario needs.

**Variants:** every single add/remove variant, plus the base.

**Size estimate.** About 30 runs × up to about 50 change points ≈ 1,500 snapshots of about 1–2 KB,
roughly 2–3 MB of JSON.

**Viewer commands** (draft):

| Command | Shows |
|---|---|
| `design` | the minimal design: steps with times, machines, order |
| `result` | finish time; nothing else unless asked |
| `at <tick>` | each machine slot's state (working or idle) and, for idle slots, why (starved or no work left); who waits for which step; progress |
| `machine <name>` | that machine's timeline and working/starved/no-work-left breakdown |
| `util [<from> <to>]` | utilization table for a period (whole run by default) |
| `unit <n>` | one unit's journey: waits and work per step |
| `variants` / `switch <variant>` / `compare` | explore the pre-computed variants against the base |
| `explain <term>` / `glossary` | in-context reference for every term used, including blocked as a future concept |
| `note <text>` | the owner's free-form observations, recorded with context |

**Teaching.** Terms appear in context: an idle slot is shown as "idle (starved)", and `explain starved`
gives the definition, how the game decides it, and what it does *not* mean. No quizzes.

## 5. Owner session: missions

A **mission** is a short, open goal that the owner pursues by exploring with the commands, instead of
answering fixed quiz questions. The owner's notes on how it went, what was clear, what was missing or
misleading, and what had to be worked out by hand are the evidence. There is no score.

Draft missions (the owner may rewrite, add or drop any):

1. "In this factory, find a moment when a machine is starved. What is it waiting for, and where is that
   work now?"
2. "Which machine worked hardest over the whole run? Is it still the hardest-working over the middle of
   the run?"
3. "One machine looks underused. Is it needed? Use the variants to find out."

Failure to accomplish a mission with the given tools still falsifies the presentation. Success is only
a smoke test.

## 6. Scenarios

Existing designs are reused where they already carry a lesson. Hand-derived expectations will be
pre-registered for every displayed number before generation.

| Lesson | Candidate design |
|---|---|
| Starved between units downstream of a slower step | first-pass G1 or second-pass Factory 1 |
| Starved but needed, versus idle and not needed | the idle pair (removal variants show which) |
| Two-slot machine: per-slot utilization | G4 (twin assembler) or G11b |
| Highest utilization ≠ bottleneck | G5 (pooled-step view), G7 (co-binding) |
| Whole run versus window | any release-at-once line (ramp-up and wind-down) |

## 7. Evaluation

**Mechanical:**

- the pre-registered values are reproduced;
- every slot-tick is in exactly one layer-1 state;
- every idle slot-tick has exactly one layer-2 characterization;
- working + starved + no-work-left sum to the period;
- "idle while a unit waits for it" is never observed;
- "starved" never appears for a completed order;
- no utilization figure is labeled a bottleneck;
- every displayed derived number names its derivation.

**Qualitative:** the owner's mission notes.

## 8. Concept coverage map and tracking destinations

Nothing listed during discovery is dropped.

| Concept | Status | Destination for tracking |
|---|---|---|
| Starvation, utilization (with working/idle breakdown) | **This iteration** | This research |
| Bottleneck (by named method / experiment), shifting bottleneck, excess capacity, co-binding constraints | Candidate next iterations | This research (owner to confirm) |
| Queue/WIP, lead time, finish time (makespan), step time and routing, parallel and multi-slot capacity, pooling/flexible machines, tie-break order effects, release-at-once backlog | Explorable now; not yet scheduled | Carried in the report revision. Unaddressed ones move to a new research question at reconciliation |
| Throughput over longer horizons, Little's Law, arrival/release patterns, demand versus capacity, **machine downtime / availability**, backlog, multiple products sharing machines | Engine-supported, needs new scenarios | **New research question** (game product programme) at reconciliation |
| Blocking / finite buffers | Engine lacks it | Register CANDIDATE at reconciliation (buffers deferred in Factory resource semantics; Factory Design evolution names buffers and congestion as separate capability questions) |
| Variability: processing time, failure/repair, arrivals, yield | Engine is deterministic | Engine evolution's stochastic boundary: promote a bounded question only when a concrete consumer needs a specific phenomenon. The game's educational need is a candidate trigger |
| Setup/changeover | Field present, ignored by the engine | Already a READY register question |
| Transfer/layout | Pending spatial semantics | Already in spatial-runtime planning |
| Lots, batches | — | Already a register question |
| Scrap/rework/quality, priorities and other dispatch rules | Engine lacks them | Programme roadmap at reconciliation |
| Live simulation loop / interactive harness | Out of scope here | **New research or planning item** at reconciliation |

The game product research programme
(`docs/research/investigations/factory-design-game-vertical-slice.md`) is the natural single place
for the full concept map. Register rows cover the new questions. Both change only through
reconciliation with review.

## 9. Open decisions for the owner

1. Approve the two-layer model and the utilization choices in section 2 (per slot; per machine by
   default, with per step as a variant).
2. Approve, rewrite or drop the draft missions in section 5.
3. Whether the next iteration after this one is bottleneck, as section 8 suggests.
