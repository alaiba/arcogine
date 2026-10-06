# Second owner walkthrough: scenario oracle (pre-registered)

> **Custody:** temporary research evidence for the factory-design game diagnostic-evidence
> investigation. **The repository owner must not read this note before playing the second
> walkthrough. It is the answer key.**
>
> **Written before** the revised player-facing contract or the scenario generator was implemented or
> run. Every value is hand-derived from Engine semantics §2–§4: a per-machine FIFO queue for a
> single-eligible step; a shared backlog for a multi-eligible step before binding; the lower
> `MachineId` wins a remaining tie; equal-time events are processed in insertion order. The generator
> must assert that runs reproduce these values. A disagreement is investigated, never resolved by
> copying the run.

## Shared inputs

- **Routing** `PRESS` → `WELD` → `PAINT`, durations per scenario.
- **Workload:** one order, submitted at tick 0, run to quiescence.
- **Quantity:** 8 units, except the tutorial (3).
- **Resource identity:** the 1-based authored position. Added machines are appended. A machine is named
  without a number when it is the only one of its kind ("Press"), and numbered when there are several
  ("Welder 1", "Welder 2"). An appended machine takes the next free number ("Press 2", "Welder 3").
- **Snapshots:** each snapshot tick has events at that tick, so the observation's `currentTime` equals
  the requested tick.
- **Units:** `u_k` is the unit with ordinal `k-1`.

## Scenarios

### Tutorial — PRESS 1, WELD 3, PAINT 1; Press, Welder, Painter; quantity 3 (not scored)

- **Schedule.**
  - Press: `u_k` (k−1, k).
  - Weld: u1 1–4, u2 4–7, u3 7–10.
  - Paint: u1 4–5, u2 7–8, u3 10–11.
  - **Completion 11.**
- **Snapshot @ 3:**
  - waiting: 2 units (u2, u3) for WELD on Welder;
  - Press and Painter idle, each the only machine for its step;
  - 0 of 3 finished.
- **Last unit (u3):** waited 2 for PRESS, 1; waited 4 for WELD, 3; waited 0 for PAINT, 1.
- **Interventions:**
  - + Press 2: 11 (the demonstrated try).
  - + Welder 2: 8.
  - + Painter 2: 11.

### A — PRESS 4, WELD 2, PAINT 5; Press, Welder, Painter

- **Schedule.**
  - Press: `u_k` (4k−4, 4k).
  - Weld: (4k, 4k+2), never waiting.
  - Paint: start max(4k+2, 5k+1) = 5k+1, end 5k+6.
  - **Completion 46.**
- **Snapshot @ 16.** The paint end for u2 was inserted before the press end for u4, so it is processed
  first.
  - Press works on u5, Welder on u4, Painter on u3.
  - Waiting: **3 for PRESS** (u6–u8), 0 for WELD, 0 for PAINT.
  - No machine is idle.
  - 2 of 8 finished.
- **Last unit (u8):**
  - waited **28** for PRESS, PRESS 4;
  - waited 0 for WELD, WELD 2;
  - waited 7 for PAINT, PAINT 5;
  - longest wait: PRESS.
- **Interventions:**
  - + Press 2: 46. Presses finish pairs at 4m; paint still runs back to back from 6.
  - + Welder 2: 46. The new welder never takes work.
  - + Painter 2: **39**. Painters alternate and paint never waits; u8 is painted 34–39.
- **Truth:** only a painter helps. The longest wait (PRESS) is not where a machine helps.

### D — PRESS 1, WELD 6, PAINT 2; Press, Welder 1, Welder 2, Painter

- **Schedule.**
  - Press: `u_k` (k−1, k).
  - Weld: u1 Welder 1 1–7, u2 Welder 2 2–8. u3–u8 enter the shared WELD backlog, then u3 7–13 (W1),
    u4 8–14 (W2), u5 13–19 (W1), u6 14–20 (W2), u7 19–25 (W1), u8 20–26 (W2).
  - Paint: u1 7–9, u2 9–11, u3 13–15, u4 15–17, u5 19–21, u6 21–23, u7 25–27, u8 27–29.
  - **Completion 29.**
- **Snapshot @ 6:**
  - **4 units (u3–u6) wait for WELD, in the shared backlog, assigned to neither welder.** Every
    welder's own queue is 0.
  - 1 unit (u8) waits for PRESS; u7 is being pressed.
  - Painter idle, the only PAINT machine.
  - 0 of 8 finished.
- **Last unit (u8):**
  - waited 7 for PRESS, PRESS 1;
  - waited **12** for WELD, WELD 6 on Welder 2;
  - waited 1 for PAINT, PAINT 2;
  - longest wait: WELD.
- **Interventions:**
  - + Press 2: 29.
  - + Welder 3: **23**.
  - + Painter 2: **28**.
- **Truth:** a welder helps (6 ticks) and a painter helps (1 tick); a press does not.

### B — PRESS 3, WELD 1, PAINT 5; Press, Welder, Painter 1, Painter 2

- **Schedule.**
  - Press: `u_k` (3k−3, 3k).
  - Weld: (3k, 3k+1).
  - Paint: u1 P1 4–9, u2 P2 7–12, u3 P1 10–15, u4 P2 13–18, u5 P1 16–21, u6 P2 19–24, u7 P1 22–27,
    u8 P2 25–30.
  - **Completion 30.**
- **Snapshot @ 12.** Painter 2's end for u2 is processed before the press end for u4.
  - Painter 2 is **idle, and no unit waits for PAINT**. Painter 1 works on u3, Press on u5, Welder on
    u4.
  - Waiting: 3 for PRESS (u6–u8).
  - 2 of 8 finished.
- **Last unit (u8):** waited 21 for PRESS, 0 for WELD, 0 for PAINT.
- **Interventions:**
  - **− Painter 2: 44** (+14). Single-painter start 5k−1, end 5k+4.
  - + Press 2: **25** (see the derivation note below).
  - + Welder 2: 30.
  - + Painter 3: 30.
- **Truth:** Painter 2 **is needed**. A single run cannot show that; removing it does.
- **Derivation note for + Press 2.**
  - Press pairs finish at 3, 6, 9, 12. The single welder serializes each pair.
  - Paint readiness: u1 4, u2 5, u3 7, u4 8, u5 10, u6 11, u7 13, u8 14.
  - Painters: P1 u1 4–9, P2 u2 5–10. u3 and u4 enter the shared backlog. P1 u3 9–14, P2 u4 10–15
    (Painter 2's end at 10 is processed before u5 becomes ready). P1 u5 14–19, P2 u6 15–20, P1 u7
    19–24, P2 u8 20–25.

### C — PRESS 2, WELD 3, PAINT 4; Press, Welder 1, Welder 2, Painter

- **Schedule.**
  - Press: `u_k` (2k−2, 2k).
  - Weld alternates: W1 u1 2–5, W2 u2 4–7, W1 u3 6–9, W2 u4 8–11, W1 u5 10–13, W2 u6 12–15, W1 u7
    14–17, W2 u8 16–19. Weld end = 2k+3.
  - Paint back to back: (4k+1, 4k+5).
  - **Completion 37.**
- **Snapshot @ 7:**
  - Welder 2 is **idle, and no unit waits for WELD**. Welder 1 works on u3, Press on u4, Painter on u1.
  - Waiting: 4 for PRESS (u5–u8); 1 for PAINT (u2, on Painter).
  - 0 of 8 finished.
- **Last unit (u8):** waited 14 for PRESS, 0 for WELD, 14 for PAINT (a tie).
- **Interventions:**
  - **− Welder 2: 37** (no change). A single welder ends at 3k+2, and paint still starts at 4k+1.
  - + Press 2: 37.
  - + Welder 3: 37.
  - + Painter 2: **23**. Painters alternate from 5; u8 is painted 19–23.
- **Truth:** Welder 2 **is not needed** for this order, but a single run cannot show that. Its
  single-run evidence is of the same kind as B's, with the opposite truth.

### E — PRESS 2, WELD 4, PAINT 4; Press, Welder, Painter (co-binding)

- **Schedule.**
  - Press: `u_k` (2k−2, 2k).
  - Weld: (4k−2, 4k+2).
  - Paint: (4k+2, 4k+6). Every paint start after the first is a tie between readiness and the
    painter's release.
  - **Completion 38.**
- **No snapshot.**
- **Last unit (u8):** waited 14 for PRESS, 14 for WELD, 0 for PAINT.
- **Interventions:**
  - + Press 2: 38.
  - + Welder 2: 38.
  - + Painter 2: 38.
  - Context, not offered as a try: + Welder 2 + Painter 2 gives 24.
- **Truth:** **no single added machine helps**. One try that changes nothing does not show that
  another single machine would also change nothing.

### F — PRESS 3, WELD 5, PAINT 2; Press, Welder, Painter; late joiner

- **Late joiner:** events through the snapshot are drained and discarded.
- **Schedule.**
  - Press: `u_k` (3k−3, 3k).
  - Weld: (5k−2, 5k+3).
  - Paint: (5k+3, 5k+5).
  - **Completion 45.**
- **Snapshot @ 12:**
  - Waiting: **3 for PRESS** (u6–u8); **2 for WELD** (u3, u4, on Welder).
  - Painter idle, the only PAINT machine.
  - 1 of 8 finished.
- **Truth:** waiting and processing times of the last unit are **not available** in this view.

### K1 — D → D + Welder 3 (one change)

29 → **23** (6 ticks sooner).

### K2 — PRESS 2, WELD 3, PAINT 4 (Press, Welder, Painter) → + Press 2 + Painter 2 (two changes)

- **Base:** 37 (paint back to back from 5).
- **Both added:** **30** (weld-bound 2 + 24 + 4).
- **Singles:**
  - + Press 2: 37.
  - + Painter 2: 30.
- **Truth:** the pair alone cannot apportion the 7 ticks. The singles happen to show that the painter
  accounts for all of them, which the pair does not license.

## Scored answers

All scored questions are multiple choice. The keyed option for each is below.

| Q | Question | Keyed answer |
|---|---|---|
| A1 | What is waiting at tick 16? | 3 units for PRESS, nothing else |
| A2 | Would adding one machine finish sooner? | Can't tell from this run |
| A3 | Longest wait was PRESS: does adding a press finish sooner? | Can't tell from this run |
| A-try follow-up | Would one of the machines you did not try finish sooner? | Can't tell from these runs |
| D1 | WELD waiting at tick 6 | 4 units, not yet assigned to either welder |
| D2 | Could the order finish without the Painter? | No |
| D3 | Would adding one machine finish sooner? | Can't tell from this run |
| D-try follow-up | Would one of the machines you did not try finish sooner? | Can't tell from these runs |
| B1 | Is Painter 2 needed? | Can't tell from this run |
| B-try follow-up | Is Painter 2 needed? | After "remove Painter 2": yes. After any other try: can't tell |
| C1 | Is Welder 2 needed? | Can't tell from this run |
| C-try follow-up | Is Welder 2 needed? | After "remove Welder 2": no. After any other try: can't tell |
| E1 | Would adding one machine finish sooner? | Can't tell from this run |
| E-try follow-up | Would one of the machines you did not try finish sooner? | Can't tell from these runs |
| F1 | What is waiting at tick 12? | 3 for PRESS and 2 for WELD |
| F2 | How long did the last unit wait in total? | Not shown in this view |
| K1 | What changed and what happened? | Added Welder 3; finished 6 ticks sooner |
| K2 | How much of the 7 ticks came from the painter? | Can't tell from this pair |
