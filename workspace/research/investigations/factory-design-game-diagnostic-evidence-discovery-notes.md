# Game diagnostic evidence: discovery notes (owner session, 2026-10-07)

> **Custody:** temporary research evidence and continuity for the factory-design game
> diagnostic-evidence investigation. Not a report, a key or maintained state. It records owner
> feedback and decisions in progress, so they are not lost with the session.

## Second walkthrough status

- **Paused by the owner after the tutorial and A1**, to discuss scope before more prototyping or
  walkthroughs.
- Partial answers are in `logs/game-walkthrough-2-answers.json` (local only): T1, A1. The owner used
  `?`, `evidence`, `show` and `more`.
- Owner feedback on the second-pass prototype (paraphrased; the owner's own words are in chat):
  - **Redundant facts.** The sole-machine lines ("Press is the only machine that can do PRESS…") repeat
    what the design shows. They are answers to a question the player never asked. Agreed fix: do not
    raise "is it needed?" for a machine that is obviously required. More generally, the output is
    still too verbose.
  - **Pull, not push.** The UI should show the information needed to answer a question, and no more.
    A "What this run cannot tell you" section is confusing: why tell the player what the game cannot
    say?
  - **Industry vocabulary.** The game should be educational: use industry jargon as much as possible
    and have the game explain every concept it uses. This reverses the first-pass response of
    replacing "starved/surplus".
  - **Audits stay internal.** The mechanical audits validate correctness and need not be visible to the
    player.
  - **Cognitive load.** The first pass needed pen and paper to follow the text. That defeats the
    purpose: some UI is required for the experiment to work. A text UI is fine and tests concepts;
    graphics would only add to a text UI that already works.
  - **Not a live prototype.** Tries are a fixed, pre-generated menu. The player cannot freely add or
    remove machines or inspect other ticks.
  - **Tutorial defects:**
    - after `evidence` or `show`, the pending question is not printed again;
    - the practice question asks for something stated verbatim;
    - option numbers differ from the counts they stand for, and the owner answered with the count.
  - **"Can't tell" questions.** The owner was unsure whether the expected answer was "can't tell" or a
    machine they could reasonably infer. The question tests epistemic caution rather than the
    player's reasoning, and penalizes forming a hypothesis.

## Scope discussion (in progress)

- **Owner view:** the second walkthrough went wrong in setting expectations. The questions seemed
  useless. The display format is exactly what was being searched for.
- **"Truthful diagnostic surface" is under-defined.** The brief defines *truthful* (traceable, explicit
  about method, refuses unsupported attribution, inspectable). It does not define *diagnostic*: which
  goals or questions the player needs answered, and for what decision. The six fixed questions were
  designed as traps for truthfulness, not derived from player goals.
- **"Evidence requirements into the game consumer plan" is immaterial** while the plan has no concrete
  player task to attach them to.
- **Owner position:** this is not a different question from the research. "No UI" held it back; the
  research is about what to present, and how, from a scenario to achieve goals, and **the goals are
  the missing piece**. The research is not done.
- **Discovery so far:** the walkthrough felt like coaching. Understanding industry concepts by
  inspecting an existing design feels valuable. Medium questions (animation, Gantt, dashboards) are to
  be avoided for now.
- **Core activity:** not yet established. The walkthroughs were about understanding one engine
  execution, not designing a factory.

## Owner decisions and direction (2026-10-07, second exchange)

- **Next prototype iteration.**
  - Present a design in its most minimal form.
  - Let the player *inspect*, by command, the facts the game can always state truthfully: finish time
    and progress; what each machine is doing at a moment; who waits for which step; exact waiting and
    working times; what changed between two designs and the outcome.
  - Purpose: discover what needs to be shown in each context. The model is pull, not push.
- **Misleading claims as teaching material.** The falsified claims (busiest = bottleneck, busy-counter
  utilization, longest wait = problem, per-machine queues for shared work, single-run "surplus",
  per-change attribution, "because", order irrelevance) are lessons in themselves. They should be
  preserved and used to generate interesting, challenging, educational scenarios: teachable moments.
- **Teaching model.** Learning by experiment, not school. Introduce industry jargon in context, and
  explain every concept, both indirectly and through an in-game reference the player can open on
  demand.
- **Static only.** Conclude as much as possible in this research with static, pre-computed scenarios
  in the `.mjs` prototype (or a new version on the same principles). Hypothesis testing is in scope
  only as exploring pre-computed alternatives. A live simulation loop or harness is **out of scope**:
  it is a separate future research question that must be planned.
- **Concepts.** Build a list of all concepts the existing engine can support, pick one or two first,
  and see how they land in the static prototype.
- **Owner challenges to the first report's conclusions:**
  - **Ownership of the "game must keep" items.** Full event history, simulation-clock labels, stable
    machine identity and change sets are simulation/runtime or model concerns, not game concepts. The
    game is a presentation client of the engine.
  - **Analytics demand.** "No reusable measurement needed" may be avoidance. Utilization, bottleneck and
    other measurements seem necessary for any meaningful experience, in a game or otherwise.
- **The busy counter (`busyTicks`) looks like a design smell.** The owner asked for it to be
  investigated.

### Busy-counter investigation (research agent, from repository history)

- **Origin.**
  - `Machine.busyTicks` predates the runtime observation contract. `ResourceObservation.busyTicks`
    was exposed by the runtime-observation PR, but nothing accumulated it, so it always read 0.
  - The Gate 4 headless-acceptance PR (2026-09-02) added crediting of a finished step's duration at
    task end. Its stated purpose was to make "the active production bottleneck identifiable from
    ResourceObservation facts alone, by carried load and by utilization".
- **Encoded heuristic.** That PR's acceptance test,
  `HeadlessClosureAcceptanceTest.supportedObservationIdentifiesTheActiveBottleneckWithoutInternalAccess`,
  encodes the heuristic this investigation falsified: most load, or highest `busyTicks` ÷ elapsed,
  equals the bottleneck. It holds on that test's two-stage fixture but not in general (G4, G5, G7).
- **Codified as "utilization".** Engine semantics §10, §10.1 and §10.2 codify "`busyTicks` /
  utilization" with saturation rules, pinned by `EngineDerivedResultConformanceTest`. The runtime
  contract lists "utilization facts".
- **No product consumer.** There is no application. Its only users are those tests. The research
  substrate deliberately does not use it: `ProcessingOccupancyOracle` documents why it measures
  occupancy from events instead.
- **Assessment:** a semantic smell. A completion-credited cumulative sum is exposed and specified under
  the name "utilization", and justified by a bottleneck heuristic that the corpus falsifies. It is a
  candidate architecture/consistency reconciliation item, not decided here.

### Owner decisions (2026-10-07, third exchange)

- **The busy counter is to be deleted.** It is out of the plan and the prototypes entirely.
- **Removal scope** (for reconciliation, independently reviewed):
  - `ResourceObservation.busyTicks` and its accumulation in `FactoryHandler`/`Machine`/`MachineView`;
  - Engine semantics §10.1 rule 1 and the §10.2 register row (the lead-time saturation rule stays);
  - the bottleneck-by-utilization assertion in `HeadlessClosureAcceptanceTest`, and the `busyTicks`
    part of `EngineDerivedResultConformanceTest`;
  - the runtime contract's "utilization facts", the ISA-95 mapping row and the Governance evidence
    mention.
- **Timing.** The owner allowed removing it on this branch now if that helps focus the research. The
  research agent recommended deferring it to the first reconciliation commit:
  - The new prototype does not use the counter at all, so removing it earlier does not sharpen this
    iteration.
  - Removing it now would force rewriting the committed first-pass experiment (its naive control and
    the divergence test use it as a counterexample).
  - Later experiments would no longer run against `main`'s supported runtime.

  The deferral is awaiting owner confirmation.
- **Machine state is not flow characterization.** Machine slot state is a direct fact (working, idle,
  offline). Flow characterization is computed from the whole system: starved, no work left, and
  blocked in future. Adopted in revision 2 of the iteration plan.
- **Offline time is out of this iteration.** Availability is an engine runtime command issued at a
  moment, not a design property, and no scenario exercises it. It belongs with "machine downtime" in
  the engine-supported-needs-scenarios bucket.

### Bakery missions played (2026-10-07)

See `game-inspection-walkthrough-record.md`.

- The interaction loop works, and in-the-moment learning was observed.
- 17 of 19 checked parts matched.
- The misses came from the label format ("idle (no work left)") and from not trying the joint variant.
- Correct answers can hide invalid heuristics.
- The interval convention needs explicit durations.
- New coverage item: **order release patterns and policies**. Multiple orders over time are supported
  by commands; a release schedule or policy as a design concept is not.

## Items to track (not yet in any maintained surface)

1. **"Blocked" / finite buffers.** The owner wants this planned, probably as a research question first.
   - Current repository state: Factory resource semantics defers buffer/storage "until
     occupancy/material/blocking semantics matter".
   - The runtime has unbounded queues and no blocked state.
   - No research-register question exists.
   - Candidate for a register entry at reconciliation.
2. **Live interactive harness or prototype decision.**
   - The engine can advance to any tick, observe and rerun changed designs.
   - Nothing interactive exists in front of it. The product concepts doc says there is no interactive
     experiment loop, and the vertical-slice planning gate admits no playable implementation.
   - A live research harness, or an admitted prototype, needs an explicit owner decision. Not tracked
     anywhere yet.
3. **Machine identity stability under removal** (from the second-pass generation). With positional
   identity, removing a machine renumbers later machines, so a truthful change set reports an extra
   change. A game projection should keep unchanged machines' identities stable. Carry into the
   requirement set.
