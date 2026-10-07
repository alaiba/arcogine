# Starvation and utilization missions: oracle (draft, pre-registration)

> **Custody:** temporary research evidence for the factory-design game diagnostic-evidence
> investigation. **The repository owner must not read this note before playing the missions.** It is
> the answer key.
>
> **Draft:** written before any generator or viewer exists, while the mission list is under owner
> review. Every value is hand-derived from Engine semantics §2–§4. The generator must assert these
> values; a disagreement is investigated, never copied from a run.

## Definitions used

- **Unit** `u_k` is the loaf with ordinal `k−1`. The whole order is released at tick 0. Intervals are
  half-open, `[start, end)`.
- **Working:** a slot holds a job, from `JOB_DISPATCHED` to `JOB_STEP_COMPLETED`.
- **Idle:** the slot holds nothing.
- **Starved:** idle while some unfinished unit will still need a step this machine can do. With unbounded
  queues and immediate dispatch, no unit is ever waiting for an idle machine.
- **No work left:** idle when no unfinished unit will need the machine again.
- **Utilization over `[a, b)`:** working slot-ticks ÷ (slots × (b − a)).

## S1 — MIX 2, BAKE 4, PACK 1; Mixer, Oven, Packer; 4 loaves

- **Schedule.**
  - Mixer: `u_k` [2k−2, 2k), so it works 0–8.
  - Oven: u1 [2,6), u2 [6,10), u3 [10,14), u4 [14,18). It is back to back, because each loaf waits for
    the oven.
  - Packer: u1 [6,7), u2 [10,11), u3 [14,15), u4 [18,19).
  - **Completion 19.**
- **States:**
  - Mixer working [0,8), no work left [8,19).
  - Oven starved [0,2), working [2,18), no work left [18,19).
  - Packer starved [0,6), [7,10), [11,14), [15,18); working at the four pack intervals.
- **Mission 1:** at tick 5 the Mixer works u3 [4,6), the Oven works u1, and the Packer is idle.
  **Answer: Packer.**
- **Mission 2:** at tick 8 the Packer is idle; u2 is baking and u3–u4 wait for the oven.
  **Answer: starved.**
- **Mission 3:** at tick 12 all 4 loaves are mixed. **Answer: no work left.**
- **Mission 4:** over [0,19): Mixer 8/19 ≈ 42%, **Oven 16/19 ≈ 84%**, Packer 4/19 ≈ 21%.
  **Answer: Oven, ≈84% (16 of 19 ticks).**
- **Mission 5:** Packer idle 15 ticks, all starved (6+3+3+3); none with no work left before
  completion. **Answer: all of it, 15 of 15 idle ticks.**

## S2 — MIX 2, BAKE 4, PACK 1; Mixer, Oven (2 slots), Packer; 6 loaves

- **Schedule.**
  - Mixer: `u_k` [2k−2, 2k).
  - Oven: u1 [2,6), u2 [4,8), u3 [6,10), u4 [8,12), u5 [10,14), u6 [12,16).
  - At 6 and at every later even tick, the Oven's completion event was inserted before the Mixer's, so
    a slot is freed before the next loaf arrives.
  - Packer: u_k [2k+4, 2k+5).
  - **Completion 17.**
- **Oven slot occupancy:**
  - 1 slot [2,4);
  - 2 slots [4,14);
  - 1 slot [14,16);
  - 0 slots [16,17).
- **Mission 6:** at tick 9 the Oven holds u3 [6,10) and u4 [8,12). **Answer: 2.**
- **Mission 7:** 24 working slot-ticks of 2 × 17 = 34, so **≈71%**. Idle slot-ticks total 10:
  - starved 6: both slots [0,2), one slot [2,4);
  - no work left 4: one slot [14,16), both [16,17).

  **Answer: ≈71% (24 of 34 slot-ticks); starved slots at the start and no work left at the end.**
- **Mission 8:** [4,14) has both slots busy throughout. **Answer: 100% (20 of 20 slot-ticks).**

## S3 — MIX 2, BAKE 3, PACK 4; 8 loaves

- **Base line** (Mixer, Oven, Packer):
  - The Oven runs back to back, `u_k` [3k−1, 3k+2).
  - The Packer runs back to back from 5, `u_k` [4k+1, 4k+5).
  - **Completion 37.**
- **Version A** (base + a second packer; Packer 2 authored last):
  - Oven as in the base.
  - Packers alternate: P1 u1 [5,9), P2 u2 [8,12), P1 u3 [11,15), P2 u4 [14,18), P1 u5 [17,21), P2 u6
    [20,24), P1 u7 [23,27), P2 u8 [26,30).
  - **Completion 30.**
- **Version B** (base + a second oven; Oven 2 authored last):
  - The Mixer delivers every 2 ticks. The ovens alternate: O1 u1 [2,5), O2 u2 [4,7), O1 u3 [6,9), …;
    bake end = 2k+3.
  - The Packer runs back to back from 5: `u_k` [4k+1, 4k+5).
  - **Completion 37.**
- **Mission 9:** in version A, Packer 2 finished u2 at 12 and next works u4 from 14. u4 is in the Oven
  [11,14). **Answer: starved.**
- **Mission 10:** removing Packer 2 from version A gives the base line: **37 instead of 30. Answer: yes,
  needed** (7 ticks later without it).
- **Mission 11:** removing Oven 2 from version B gives the base line: **37 instead of 37. Answer: no,
  not needed.**
  - Oven 2 is idle and starved at times, for example [7,8) after finishing u2, with u4 still being
    mixed until 8.
- **Other single variants** (pre-computed for exploration, not asked):
  - **Version A:**
    - + Mixer 2: loaves reach the Oven in pairs, but the single Oven still runs back to back from 2:
      **30**.
    - + Oven 2: the ovens alternate, with bake end 2k+3 (loaves ready for packing every 2 ticks). The
      two packers alternate without waits, and u8 is packed [19,23): **23**.
    - + Packer 3: oven-bound: **30**.
  - **Version B:**
    - + Mixer 2: the ovens take pairs (end 5, 8, 11, 14). The single Packer still runs back to back from
      5: **37**.
    - + Oven 3: pack-bound: **37**.
    - + Packer 2: two ovens and two packers, as in A + Oven 2: **23**.
  - **Removal variants:** only the extra, last-authored machine of each version is offered (Packer 2 in
    A, Oven 2 in B). Removing the first of a pair would renumber the other machine, and the change set
    would honestly report a second change.

## S4 — MIX 2, BAKE 4, PACK 4; Mixer, Oven, Packer; 8 loaves (co-binding)

- **Schedule.**
  - Mixer: `u_k` [2k−2, 2k).
  - Oven: [4k−2, 4k+2).
  - Packer: [4k+2, 4k+6). Every pack start after the first is a tie between the loaf becoming ready and
    the Packer being released.
  - **Completion 38.**
- **Mission 12:** over [0,38): Oven 32/38 ≈ 84%, Packer 32/38 ≈ 84%, Mixer 16/38 ≈ 42%.
  **Answer: Oven and Packer, tied at ≈84%.**
- **Mission 13:**
  - + Oven 2: 38 (pack-bound).
  - + Packer 2: 38 (oven-bound).
  - + Mixer 2: 38.
  - **+ Oven 2 + Packer 2: 24.** Ovens alternate with bake end 2k+4. The two packers never make a
    loaf wait, so u8 is packed [20,24).
  - **Answer: no; no; only adding both an oven and a packer finishes sooner (38 → 24).**
