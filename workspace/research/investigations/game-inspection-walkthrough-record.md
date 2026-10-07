# Bakery inspection missions: owner session record

> **Custody:** temporary research evidence for the factory-design game diagnostic-evidence
> investigation. This is single-person qualitative evidence from the repository owner, not population
> evidence. Failure is useful falsification; success is only a smoke test.
>
> **Session:** played 2026-10-07 15:51–16:39 UTC.
> - **Data pack:** `game-inspection-starvation-utilization`, sha256 `027c3c8c…41bf14`.
> - **Viewer:** `inspect.mjs`, at the commit that added the bakery missions.
>
> **Blinding:** the owner reviewed the scenarios and missions beforehand, which is owner-safe, but not
> the answer key. Answers were recorded by the viewer from the owner's own session file; the
> facilitator did not intervene during play.

## Answers, comments and commands (verbatim from the session file)

| # | Owner's answer | Matched | Commands used | Owner's comment |
|---|---|---|---|---|
| 1 | packer | yes | help, design, at 5, *packer*, answer | — |
| 2 | starved | yes | at 8, machine packer, answer | — |
| 3 | idle | **no** (official: no work left) | explain starved, at 12, answer | "idle and no work left are the same, as it is displayed by the at command : Mixer (MIX): idle (no work left)" |
| 4 | oven; 84% | yes; yes | util, *answer oven* | — |
| 5 | 15; 15 | yes; yes | machine, machine packer, *answer 15* | — |
| 6 | 2 | yes | at 9, answer | — |
| 7 | 24 of 34; "it waits for the mixer to prepare the loafs one at a time" | yes; recorded | util oven, util, answer | — |
| 8 | 100% | yes | *util 4 to 14*, util 4 14, answer | — |
| 9 | starved | yes | hariants, variants, at 13, answer | "the mission could have been solved inspecting the machine itself to see if it is starved, right?" |
| 10 | yes | yes | machine packer 2, util, answer | "it didn't occur to me to check variants and compare with A-packer2, I answered using util and an heuristic: since both packers are working more than 53% of the time, it is obvious that it is needed" |
| 11 | no | yes | versions, variants, switch B, util, compare B-oven2, answer | "this time I did use compare B-oven2; this phrase is confusing: - This shows what happened with this change, not why." |
| 12 | oven and packer | yes | util, *oven and packer*, *answer oven and packer* | — |
| 13 | no; no; e | yes; yes; **no** (official: d, add an oven and a packer) | util, design, variants, compare S4+mixer, compare S4+oven, compare S4+packer, answer | "there was no point in asking both yes/no and a b c answers and the information is the same" |

Italic commands show the owner trying to answer inline (`answer oven`, `answer 15`, a bare `packer`, a
bare `oven and packer`) or to use natural syntax (`util 4 to 14`). The viewer ignored the inline text
and asked the questions again.

**Overall (verbatim):** "I didn't use all the commands available; overall it was fairly easy, but
overall the presentation is good; for a more challenging gaming experience less information could be
provided, but given the purpose was to prove that the design can be explored, it was good"

**Mechanical tally:** 17 of 19 checked parts matched. The two misses are mission 3 (answered "idle")
and mission 13c (answered "none of these").

## Additional owner feedback after the session (paraphrased; verbatim in chat)

1. **Redundant mission card.** For single-part missions, the question is printed twice: once as the
   prompt and once as the part.
2. **Inconsistent prompts.** Mission 2's prompt ("…idle. Why?") and its part ("Is the Packer starved,
   or does it have no work left?") differ, while mission 1's are identical. Why?
3. **Prompt display defect.** Typing and then using backspace shrinks the prompt from `[mission 4] >`
   to `>`.
4. **Tick counting is ambiguous.** "From tick 0 to 6": is that 6 ticks or 7? "From 11 to 14"? Is the
   whole run 19 ticks or 20?
5. **Inline answers.** Wanted: `answer oven and packer` should answer directly.
6. **Order release.** "Order: 8 loaves, all released at tick 0": does the engine support releasing
   orders in batches or on a schedule?

## Findings

**What worked:**

- **The interaction loop works, and in-the-moment learning was observed.**
  - In mission 10 the owner reached the right answer by an untested heuristic: "working more than 53%
    means needed".
  - The official answer then showed the experiment path ("compare A-packer2"), and in mission 11 the
    owner used `compare` unprompted.
  - In mission 13 the owner compared every single-machine variant on their own.
- **The design can be explored by inspection.** The owner's overall judgement is that the presentation
  is good and fairly easy, and that it succeeds at showing that a design can be explored.

**What failed or misled:**

- **The two layers are read as one label.** "idle (no work left)" was read as "idle = no work left",
  and the owner answered "idle". The concept was understood; the wording and the strict choice format
  produced the miss. The display should make the characterization the answer-bearing term, for example
  "Mixer: no work left (idle since tick 8)", and the checker should not reject an answer that
  identifies the same state.
- **Correct answers can hide invalid reasoning.** Mission 10's "yes" was right, but reached through a
  utilization threshold that this research has shown is not a reliable rule. Missions that matter for
  reasoning should record *how* the player decided, not only the result.
- **The explore-everything instinct stops one step short.** In mission 13 the owner compared every
  single variant, but not the joint one, and answered "none of these". The joint variant was listed
  under `variants`. Either combined changes need to be presented as a distinct kind of experiment, or
  the mission should point at them.
- **The mechanism line confuses again.** "This shows what happened with this change, not why" confused
  the owner a second time (it was already raised in the second pass). Drop it.
- **Redundant parts.** Mission 13's yes/no parts duplicate the multiple-choice part. Ask one question.
- **The interval convention is ambiguous** (see the answer to feedback 4). Every range should show its
  length explicitly.

**Too easy:** for a game, the viewer gives too much away. States are labeled outright, and `util` shows
the breakdown directly. The owner noted this, and that it was acceptable for this iteration's purpose.

## Answers to the owner's questions (for the record)

- **Prompt versus part (feedback 1–2).**
  - The prompt is the mission's goal; each part is the exact question with its answer format. For
    single-part missions that distinction is noise. They should be merged into one question, and the
    card should be identical in shape across missions.
  - Mission 2 differed only because its part names the two allowed answers.
- **Tick counting (feedback 4).**
  - Ranges are half-open, like clock hours: from 0:00 to 6:00 is six hours. "From tick 0 to 6" covers
    ticks 0, 1, 2, 3, 4 and 5: 6 ticks, ending as tick 6 begins. "From 11 to 14" is 3 ticks.
  - The whole run "from 0 to 19" is 19 ticks. "Finished at tick 19" names the moment the last packing
    ended (it occupied tick 18).
  - The viewer used this convention consistently but never showed it. Showing durations, for example
    "ticks 0–5 (6 ticks)", removes the ambiguity.
- **Order release (feedback 6).**
  - The engine releases every unit of an order as soon as the order is accepted: the unit-work
    decomposition, Engine semantics §3.
  - Several orders can be submitted at different ticks, because submitting a workload is a runtime
    command at the current tick. A sequence of orders over time is therefore supported when something
    outside the design issues those commands, as research fixture scripts do.
  - The factory design has no release schedule or release policy, such as one loaf every N ticks or a
    work-in-process cap. A scheduled or batched release is therefore a scenario concern today (the
    "arrival/release patterns" item in the coverage map). A release *policy* as a design concept would
    be a new capability question.

## Defects and changes for the next iteration (not applied)

| Change | Kind |
|---|---|
| One question per single-part mission; one card shape for all missions | presentation |
| Accept `answer <text>` inline (fills the first part; asks the rest) | interaction |
| Accept natural range syntax (`util 4 to 14`); suggest the nearest command on typos (`hariants`, `versions`) | interaction |
| Use the readline prompt, so editing keeps `[mission n] >` (fixes feedback 3) | defect |
| Show every range with its length, and state the convention in `explain tick` / `explain period` | presentation |
| Lead with the flow characterization ("no work left", "starved") rather than "idle (…)"; accept answers that name the same state | presentation / checking |
| Drop the "This shows what happened with this change, not why" line | presentation |
| Mission 13: one multiple-choice question instead of yes/no plus a choice | mission design |
| Add a "how did you decide?" free-text part to reasoning missions (10, 11, 13) | mission design |
| Present combined-change variants as a distinct kind of experiment | presentation / mission design |
| Consider difficulty levels for later play (fewer labels, more inference) | product question, out of scope here |
