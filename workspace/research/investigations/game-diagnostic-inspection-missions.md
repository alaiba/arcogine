# Starvation and utilization: scenarios and missions (draft for owner review)

> **Custody:** temporary research evidence for the factory-design game diagnostic-evidence
> investigation. This note is **safe for the owner to read**: it defines the scenarios and missions,
> not the answers. The official answers and their hand derivations are in a separate oracle note that
> the owner must not open. This note supersedes sections 5 and 6 of the iteration plan.

## How a mission works

1. The viewer shows the current mission: a short goal plus its **answer format**.
2. The player explores with the inspection commands (`design`, `at`, `machine`, `util`, `unit`,
   `variants`, `explain`…). The commands show facts only, never the answer.
3. When the player thinks they know, they type `answer …`. The answer is recorded as given.
4. The viewer immediately shows the **official answer**, a short explanation of the concept, and where
   in the data it can be seen. The player learns in the moment.
5. An optional one-line comment is recorded, and then the next mission begins.

Rules:

- Missions come strictly in order. Several missions build on one scenario before the next scenario
  begins.
- The first missions are deliberately trivial: they test the interaction and build confidence.
- The player can type `explain <term>` at any time. Nothing is scored. The evidence is the record of
  answers, comments and which commands were used.

**Mission types:**
- **Inspect:** read the answer off one view.
- **Analyze:** combine views or periods.
- **Experiment:** compare pre-computed variants.

## Theme

A small bakery. Every loaf goes through **MIX → BAKE → PACK**. The machines are the Mixer, the Oven and
the Packer, numbered when there are several. An order of loaves is released all at once at tick 0.
Every machine is online throughout. The theme is new, so nothing carries over from the earlier passes'
factories.

## Scenarios, in increasing complexity

| Scenario | What is new | Design |
|---|---|---|
| **S1 First bake** | One machine per step; a tiny order. Working and idle; starved versus no work left; utilization | MIX 2, BAKE 4, PACK 1 ticks; Mixer, Oven, Packer; 4 loaves |
| **S2 The big oven** | One machine with **two slots**: per-slot state and utilization; the period matters | MIX 2, BAKE 4, PACK 1; Mixer, Oven (2 slots), Packer; 6 loaves |
| **S3 Two upgrade options** | **Variants:** two versions of one line, each with one extra machine. Idle and starved does not mean "not needed" | MIX 2, BAKE 3, PACK 4; 8 loaves. **Version A:** Mixer, Oven, Packer 1, Packer 2. **Version B:** Mixer, Oven 1, Oven 2, Packer |
| **S4 Two busy machines** | A system-level effect: the most utilized machines and where adding capacity helps | MIX 2, BAKE 4, PACK 4; Mixer, Oven, Packer; 8 loaves |

**Variants available:**

| Scenario | Variants |
|---|---|
| S1, S2 | None (no experiment missions) |
| S3 | Every single add/remove variant of both versions, including removing Packer 2 (A) and removing Oven 2 (B) |
| S4 | Every single add variant, plus one joint variant: add an oven **and** a packer |

## Missions

| # | Scenario | Type | Mission | Answer format | Learning objective |
|---|---|---|---|---|---|
| 1 | S1 | Inspect | Which machine is idle at tick 5? | a machine name | Read a machine's state at a moment: working or idle |
| 2 | S1 | Inspect | The Packer is idle at tick 8. Is it **starved**, or does it have **no work left**? | `starved` / `no work left` | "Starved": idle while work it will need is still upstream |
| 3 | S1 | Inspect | The Mixer is idle at tick 12. Starved, or no work left? | `starved` / `no work left` | Tell starvation apart from simply being finished |
| 4 | S1 | Analyze | Which machine has the highest **utilization** over the whole run, and how much is it? | machine name + percentage (or "x of y ticks") | Utilization = time working ÷ time available, over a stated period |
| 5 | S1 | Analyze | How much of the Packer's idle time was starvation? | ticks (or "all", "none", "part") | An idle-time breakdown: a machine can be idle mostly because it is starved |
| 6 | S2 | Inspect | At tick 9, how many of the Oven's slots are working? | a number | A machine can have several slots; state is per slot |
| 7 | S2 | Analyze | What is the Oven's utilization over the whole run? It is busy most of the time, so why is it not 100%? | percentage + a short reason | Per-slot utilization; start-up starvation and end-of-run idle lower it |
| 8 | S2 | Analyze | What is the Oven's utilization from tick 4 to tick 14? | percentage | The period changes the answer: always state the period |
| 9 | S3 | Inspect | In version A, Packer 2 is idle at tick 13. Starved, or no work left? | `starved` / `no work left` | Apply starvation in a design with parallel machines |
| 10 | S3 | Experiment | Is Packer 2 **needed**, meaning the order would finish later without it? | `yes` / `no` | "Needed" is answered by an experiment (remove it and compare), not by looking at one run |
| 11 | S3 | Experiment | In version B, Oven 2 is also idle and starved at times, just like Packer 2. Is Oven 2 needed? | `yes` / `no` | Idle and starved look the same for a needed and an unneeded machine. Only the experiment tells them apart |
| 12 | S4 | Analyze | Which machine or machines have the highest utilization? | machine name(s) | Read utilization across machines; ties happen |
| 13 | S4 | Experiment | Would a second oven make the order finish sooner? A second packer? What does? | `yes`/`no` for each + which variant helps | High utilization does not tell you where added capacity helps; two steps can limit together (a bridge to the bottleneck concept) |

**Shape of the set:**

- Missions 1–3 need only one inspection view each.
- Missions 4–8 introduce and test utilization and its period.
- Missions 9–11 introduce the experiment.
- Missions 12–13 close with a system-level surprise.

Missions 5, 7, 11 and 13 carry the teachable traps.

## Glossary entries the missions rely on

The viewer shows each in context and on `explain`:

- tick;
- step;
- slot;
- working;
- idle;
- starved;
- no work left;
- blocked (a future concept: not modeled yet);
- utilization (and its period);
- variant;
- needed.

## Open points for the owner

1. Are the number and pacing right (13 missions, four scenarios)? Any to drop, merge or add?
2. Answer formats: they are deliberately simple (a name, a choice, a number), so answers can be
   compared exactly, with an optional free-text "why". Is that too rigid?
3. After the official answer, should the viewer also show *how* to find it ("type `machine Packer`")?
4. The bakery theme: keep it, or return to a neutral factory?
