# Second owner walkthrough: revised wording and protocol

> **Custody:** temporary research evidence for the factory-design game diagnostic-evidence
> investigation. This note is **safe for the owner to read**: it describes what changed and how the
> session runs, not the answers. The answer key is a separate oracle note that the owner must not
> open.

## Why a second pass

The first blinded walkthrough partly failed. Comparisons were read correctly. Four things were not:

- the single-run "limiting step" refusal was overridden, usually by reading the longest wait as the
  answer;
- "starved" and "surplus" were not understood;
- a refused interval was misread;
- the protocol itself was hard to follow.

The research brief does not allow promoting the contract unchanged. This pass tests a revised
player-facing form, **C3c ("plain bundle")**, through a deterministic terminal session.

## Revised player-facing wording (C3c)

C3c keeps C3b's evidence rules: every line is a direct fact, a count at one moment, a time interval
between recorded events, a comparison or an explicit "this does not tell you". Every derived line
names its derivation and evidence. Only the wording and grouping change:

| First-pass problem | C3b wording | C3c wording |
|---|---|---|
| Limiting step inferred anyway | "Which step limits this design is not decidable from one run…" | "Which machine to add: this run does not tell you. The waits and work times above describe what happened; only a try that adds one machine shows whether the order then finishes sooner." |
| Longest wait read as the cause | Last unit's waits listed, with no caveat | Same list, plus "Its longest wait shows where it spent time; on its own it does not show which machine to add." |
| "Surplus" not understood | "Whether R is surplus -- whether the order would finish as early without it -- is not decidable…" | "Whether R is needed (whether the order would finish later without it): this run does not tell you. Try removing it to find out." |
| A refusal pointed at an impossible retry | Surplus refusal also emitted for the only machine of a step | "R is the only machine that can do STEP; the order cannot finish without it." (a design fact, no refusal) |
| Refused interval misread as a number | "…supported events 1..96 are not all retained; missing [1-39]." | "Waiting and work times are not shown: this view started watching after the order began and missed the earlier events." |
| Idle at the end read as "starved" | "At tick 67, all 12 units are complete and every resource is idle." | Line removed; only the finish time is shown |
| Too much at once | Every line, plus derivation names in brackets | Grouped into **Snapshot**, **Result** and **What this run cannot tell you**. Per-machine work timelines are behind `more`; derivation and evidence behind `evidence` |
| Multi-eligible wording | "each goes to whichever of A or B can take it first…" | "They are not assigned to a machine yet; any of A or B may take them when it becomes free." |
| Completion-chain line confused | Optional pacing line | Removed |
| One-change comparison felt redundant | "This pair shows the change and the outcome together; it does not show why." | "This shows what happened with this change, not why." |

Time is labeled with the observation's own time, as before. No utilization, percentage, bottleneck,
constraint, starved, surplus, blocked, "because", "due to", "caused" or "variation" appears outside a
"cannot tell" line.

C3c must pass the same six mechanical audits as C3b on the original fixture corpus before the session
is offered.

## Session protocol

- **How to run:** one command in a terminal on the workspace branch. Only the scenario pack is read
  during play. The answer key is a separate file, opened only when the owner asks for the reveal at
  the end.
- **Fresh designs:** new routings (`PRESS` → `WELD` → `PAINT`, 8 units) that were not seen in the
  first pass. Every line shown, including every try's result, was generated from real Engine runs by
  the research experiment.
- **Fixed and deterministic:** the same scenarios, in the same order, with the same text every time.
  There is no randomness.
- **Game shape:**
  1. A short **tutorial** (not scored) shows how to read a factory and how a try works.
  2. **Six factories.** Each shows the design, an optional mid-run snapshot, the result, and a "What
     this run cannot tell you" section. Two to three multiple-choice questions follow. Then the owner
     gets **one try** per factory: add one machine (or remove the named idle one), and the new run's
     result is shown. One follow-up question comes after the try.
  3. **Two comparisons:** one one-change and one two-change.
- **At any prompt:**
  - `?` shows the glossary and commands;
  - `more` shows each machine's work timeline;
  - `evidence` shows which derivation and recorded events each line rests on;
  - `q` saves and quits.

  Each factory ends with an optional free-text comment ("anything unclear?").
- **Glossary shown in the session:**
  - **Tick:** one unit of simulated time.
  - **Waiting to start STEP:** the unit is ready for that step, but no machine has started it.
  - **Idle:** the machine has nothing in progress.
  - **Needed:** the order would finish later without this machine.
  - **Try:** change one thing, run the same order again, and compare finish times.
  - **Can't tell from this run:** what is shown does not settle the question; a try might.
- **Recording:** every answer is saved as it is given, so quitting and resuming loses nothing. The
  answers file is written under `logs/` (local only) and transcribed into the walkthrough record
  afterward.
- **Reveal:** after the last item the session offers to show the key next to each answer.
  Multiple-choice matches are marked mechanically. Judgement and free-text comments are assessed in
  the walkthrough record.

## Evidentiary limits

- Multiple choice with an explicit "can't tell" option is easier than open questions, and it may cue
  the right answer. A success is therefore a weaker smoke test than in the first pass. A failure still
  falsifies.
- The owner has seen the first pass's key. The fresh designs reduce carry-over but cannot remove it.
- The result is one person's qualitative evidence, not population evidence.
