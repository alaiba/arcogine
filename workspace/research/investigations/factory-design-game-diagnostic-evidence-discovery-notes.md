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
