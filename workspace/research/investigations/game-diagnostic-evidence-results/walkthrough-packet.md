# Blinded product-owner walkthrough packet

Shuffled with a fixed seed; fixture identities, the oracle and the design notes are withheld. Each item is a
completed attempt (one design run on its fixed workload) or a comparison of two attempts. Each item states the
authored design; the bullets are exactly what the minimal claim-evidence bundle shows, in its order, with the
named derivation behind any derived statement in brackets.

Answer from the statements alone, before opening the key:

- **Attempt items:** (1) what is waiting at the mid-run tick, and for which step? (2) which step limits this
  design, if the statements let you say? (3) for any idle resource: is it starved, surplus, or can you not
  tell? (4) what is the largest measured delay? Then read the optional completion-chain line and say whether it
  clarifies, changes or misleads your answer to (2).
- **Comparison items:** (5) what changed, and what changed in the outcome? (6) can you say which change
  accounts for the difference?

Record any wording that was ambiguous or that suggested more than it states.

## W1 (attempt)

Design: routing CUT 3 -> ASSEMBLE 4 -> INSPECT 5 ticks; one order of 12 units at tick 0; resources in order: Cutter (CUT), Assembler 1 (ASSEMBLE), Assembler 2 (ASSEMBLE), Assembler 3 (ASSEMBLE), Assembler 4 (ASSEMBLE), Assembler 5 (ASSEMBLE), Assembler 6 (ASSEMBLE), Shared (ASSEMBLE+INSPECT).

- The order completed at tick 67.
- At tick 67, all 12 units are complete and every resource is idle. [idle-resources-and-eligible-waiting]
- The last unit to finish (unit 12) took 67 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 4 on Assembler 2; waited 22 for INSPECT, INSPECT 5 on Shared. [completing-unit-lead-time-decomposition]
- Cutter processed 12 CUT steps; in use during ticks 0-36. [job-step-occurrences-from-supported-events]
- Assembler 1 processed 6 ASSEMBLE steps; in use during ticks 3-7, 9-13, 15-19, 21-25, 27-31, 33-37. [job-step-occurrences-from-supported-events]
- Assembler 2 processed 6 ASSEMBLE steps; in use during ticks 6-10, 12-16, 18-22, 24-28, 30-34, 36-40. [job-step-occurrences-from-supported-events]
- Assembler 3 processed nothing in this run. [job-step-occurrences-from-supported-events]
- Assembler 4 processed nothing in this run. [job-step-occurrences-from-supported-events]
- Assembler 5 processed nothing in this run. [job-step-occurrences-from-supported-events]
- Assembler 6 processed nothing in this run. [job-step-occurrences-from-supported-events]
- Shared processed 12 INSPECT steps; in use during ticks 7-67. [job-step-occurrences-from-supported-events]
- Which step limits this design is not decidable from one run. The last unit's times and each resource's activity above are measurements, not that answer; a retry that changes one resource shows whether that change alters completion.

Optional completion-chain line: Pacing step (method: completion chain): INSPECT. Tracing the last unit back, every step that started later than its unit was ready waited for INSPECT on Shared, which worked back to back from tick 7 to 67 (11 such waits).

## W2 (comparison)

First design: routing CUT 3 -> ASSEMBLE 4 -> INSPECT 5 ticks; one order of 12 units at tick 0; resources in order: Cutter (CUT), Assembler (ASSEMBLE), Inspector (INSPECT).

Second design: routing CUT 3 -> ASSEMBLE 4 -> INSPECT 5 ticks; one order of 12 units at tick 0; resources in order: Cutter (CUT), Assembler (ASSEMBLE), Inspector (INSPECT), Cutter 2 (CUT).

- One change: added Cutter 2 (CUT). Completion: tick 67 -> tick 67 (no change).
- This pair shows the change and the outcome together; it does not show why.

## W3 (attempt)

Design: routing CUT 3 -> ASSEMBLE 4 -> INSPECT 5 ticks; one order of 12 units at tick 0; resources in order: Cutter (CUT), Assembler (ASSEMBLE), Inspector 1 (INSPECT), Inspector 2 (INSPECT).

- At tick 16, 6 units (units 7-12) are waiting to start CUT on Cutter. [waiting-work-by-operation-step]
- At tick 16, 1 unit (unit 5) is waiting to start ASSEMBLE on Assembler. [waiting-work-by-operation-step]
- At tick 16, Cutter (CUT) has 1 of 1 slot in use.
- At tick 16, Assembler (ASSEMBLE) has 1 of 1 slot in use.
- At tick 16, Inspector 1 (INSPECT) has 1 of 1 slot in use.
- At tick 16, Inspector 2 (INSPECT) has 0 of 1 slot in use.
- At tick 16, Inspector 2 is idle, and no unit is waiting for a step it can serve (INSPECT). [idle-resources-and-eligible-waiting]
- At tick 16, 2 of 12 units are complete.
- The order completed at tick 56.
- At tick 56, all 12 units are complete and every resource is idle. [idle-resources-and-eligible-waiting]
- The last unit to finish (unit 12) took 56 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 11 for ASSEMBLE, ASSEMBLE 4 on Assembler; waited 0 for INSPECT, INSPECT 5 on Inspector 2. [completing-unit-lead-time-decomposition]
- Cutter processed 12 CUT steps; in use during ticks 0-36. [job-step-occurrences-from-supported-events]
- Assembler processed 12 ASSEMBLE steps; in use during ticks 3-51. [job-step-occurrences-from-supported-events]
- Inspector 1 processed 6 INSPECT steps; in use during ticks 7-12, 15-20, 23-28, 31-36, 39-44, 47-52. [job-step-occurrences-from-supported-events]
- Inspector 2 processed 6 INSPECT steps; in use during ticks 11-16, 19-24, 27-32, 35-40, 43-48, 51-56. [job-step-occurrences-from-supported-events]
- Whether Inspector 2 is surplus -- whether the order would finish as early without it -- is not decidable from this run; a retry without it can show that.
- Which step limits this design is not decidable from one run. The last unit's times and each resource's activity above are measurements, not that answer; a retry that changes one resource shows whether that change alters completion.

Optional completion-chain line: Pacing step (method: completion chain): ASSEMBLE. Tracing the last unit back, every step that started later than its unit was ready waited for ASSEMBLE on Assembler, which worked back to back from tick 3 to 51 (11 such waits).

## W4 (comparison)

First design: routing CUT 3 -> ASSEMBLE 4 -> INSPECT 5 ticks; one order of 12 units at tick 0; resources in order: Cutter (CUT), Assembler (ASSEMBLE), Inspector 1 (INSPECT), Inspector 2 (INSPECT).

Second design: routing CUT 3 -> ASSEMBLE 4 -> INSPECT 5 ticks; one order of 12 units at tick 0; resources in order: Cutter (CUT), Assembler (ASSEMBLE), Inspector 1 (INSPECT), Inspector 2 (INSPECT), Assembler 2 (ASSEMBLE).

- One change: added Assembler 2 (ASSEMBLE). Completion: tick 56 -> tick 45 (11 ticks earlier).
- This pair shows the change and the outcome together; it does not show why.

## W5 (comparison)

First design: routing CUT 2 -> ASSEMBLE 3 -> INSPECT 2 ticks; one order of 2 units at tick 0; resources in order: Cutter (CUT), Assembler (ASSEMBLE), Shared (ASSEMBLE+INSPECT).

Second design: routing CUT 2 -> ASSEMBLE 3 -> INSPECT 2 ticks; one order of 2 units at tick 0; resources in order: Shared (ASSEMBLE+INSPECT), Assembler (ASSEMBLE), Cutter (CUT).

- One change: resource order [Cutter, Assembler, Shared] -> [Shared, Assembler, Cutter]. Completion: tick 11 -> tick 9 (2 ticks earlier).
- When eligible resources are otherwise equally placed to take work, it goes to the one with the lower resource number (in these designs, the one listed earlier), so resource order alone can change the outcome.
- This pair shows the change and the outcome together; it does not show why.

## W6 (attempt)

Design: routing CUT 3 -> ASSEMBLE 4 -> INSPECT 5 ticks; one order of 12 units at tick 0; resources in order: Cutter (CUT), Assembler (ASSEMBLE), Inspector (INSPECT).

- At tick 33, 3 units (units 9-11) are waiting to start ASSEMBLE on Assembler. [waiting-work-by-operation-step]
- At tick 33, 1 unit (unit 7) is waiting to start INSPECT on Inspector. [waiting-work-by-operation-step]
- At tick 33, Cutter (CUT) has 1 of 1 slot in use.
- At tick 33, Assembler (ASSEMBLE) has 1 of 1 slot in use.
- At tick 33, Inspector (INSPECT) has 1 of 1 slot in use.
- At tick 33, 5 of 12 units are complete.
- The order completed at tick 67.
- At tick 67, all 12 units are complete and every resource is idle. [idle-resources-and-eligible-waiting]
- The last unit to finish (unit 12) took 67 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 11 for ASSEMBLE, ASSEMBLE 4 on Assembler; waited 11 for INSPECT, INSPECT 5 on Inspector. [completing-unit-lead-time-decomposition]
- Cutter processed 12 CUT steps; in use during ticks 0-36. [job-step-occurrences-from-supported-events]
- Assembler processed 12 ASSEMBLE steps; in use during ticks 3-51. [job-step-occurrences-from-supported-events]
- Inspector processed 12 INSPECT steps; in use during ticks 7-67. [job-step-occurrences-from-supported-events]
- Which step limits this design is not decidable from one run. The last unit's times and each resource's activity above are measurements, not that answer; a retry that changes one resource shows whether that change alters completion.

Optional completion-chain line: Pacing step (method: completion chain): INSPECT. Tracing the last unit back, every step that started later than its unit was ready waited for INSPECT on Inspector, which worked back to back from tick 7 to 67 (11 such waits).

## W7 (comparison)

First design: routing CUT 3 -> ASSEMBLE 4 -> INSPECT 5 ticks; one order of 12 units at tick 0; resources in order: Cutter (CUT), Assembler (ASSEMBLE), Inspector 1 (INSPECT), Inspector 2 (INSPECT), Assembler 2 (ASSEMBLE).

Second design: routing CUT 3 -> ASSEMBLE 4 -> INSPECT 5 ticks; one order of 12 units at tick 0; resources in order: Cutter (CUT), Assembler (ASSEMBLE), Inspector 1 (INSPECT), Inspector 2 (INSPECT), Assembler 2 (ASSEMBLE), Cutter 2 (CUT).

- One change: added Cutter 2 (CUT). Completion: tick 45 -> tick 37 (8 ticks earlier).
- This pair shows the change and the outcome together; it does not show why.

## W8 (attempt)

Design: routing CUT 3 -> ASSEMBLE 4 -> INSPECT 5 ticks; one order of 12 units at tick 0; resources in order: Cutter (CUT), Twin Assembler (ASSEMBLE, 2 slots), Inspector 1 (INSPECT), Inspector 2 (INSPECT).

- At tick 9, 8 units (units 5-12) are waiting to start CUT on Cutter. [waiting-work-by-operation-step]
- At tick 9, Cutter (CUT) has 1 of 1 slot in use.
- At tick 9, Twin Assembler (ASSEMBLE) has 2 of 2 slots in use.
- At tick 9, Inspector 1 (INSPECT) has 1 of 1 slot in use.
- At tick 9, Inspector 2 (INSPECT) has 0 of 1 slot in use.
- At tick 9, Inspector 2 is idle, and no unit is waiting for a step it can serve (INSPECT). [idle-resources-and-eligible-waiting]
- At tick 9, 0 of 12 units are complete.
- The order completed at tick 45.
- At tick 45, all 12 units are complete and every resource is idle. [idle-resources-and-eligible-waiting]
- The last unit to finish (unit 12) took 45 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 4 on Twin Assembler; waited 0 for INSPECT, INSPECT 5 on Inspector 2. [completing-unit-lead-time-decomposition]
- Cutter processed 12 CUT steps; in use during ticks 0-36. [job-step-occurrences-from-supported-events]
- Twin Assembler processed 12 ASSEMBLE steps; in use during ticks 3-40; at most 2 of 2 slots in use at once. [job-step-occurrences-from-supported-events]
- Inspector 1 processed 6 INSPECT steps; in use during ticks 7-12, 13-18, 19-24, 25-30, 31-36, 37-42. [job-step-occurrences-from-supported-events]
- Inspector 2 processed 6 INSPECT steps; in use during ticks 10-15, 16-21, 22-27, 28-33, 34-39, 40-45. [job-step-occurrences-from-supported-events]
- Whether Inspector 2 is surplus -- whether the order would finish as early without it -- is not decidable from this run; a retry without it can show that.
- Which step limits this design is not decidable from one run. The last unit's times and each resource's activity above are measurements, not that answer; a retry that changes one resource shows whether that change alters completion.

Optional completion-chain line: Pacing step (method: completion chain): CUT. Tracing the last unit back, every step that started later than its unit was ready waited for CUT on Cutter, which worked back to back from tick 0 to 36 (11 such waits).

## W9 (attempt)

Design: routing CUT 3 -> ASSEMBLE 4 -> INSPECT 5 ticks; one order of 12 units at tick 0; resources in order: Cutter (CUT), Assembler (ASSEMBLE), Inspector 1 (INSPECT), Inspector 2 (INSPECT).

- At tick 16, 6 units (units 7-12) are waiting to start CUT on Cutter. [waiting-work-by-operation-step]
- At tick 16, 1 unit (unit 5) is waiting to start ASSEMBLE on Assembler. [waiting-work-by-operation-step]
- At tick 16, Cutter (CUT) has 1 of 1 slot in use.
- At tick 16, Assembler (ASSEMBLE) has 1 of 1 slot in use.
- At tick 16, Inspector 1 (INSPECT) has 1 of 1 slot in use.
- At tick 16, Inspector 2 (INSPECT) has 0 of 1 slot in use.
- At tick 16, Inspector 2 is idle, and no unit is waiting for a step it can serve (INSPECT). [idle-resources-and-eligible-waiting]
- At tick 16, 2 of 12 units are complete.
- The order completed at tick 56.
- At tick 56, all 12 units are complete and every resource is idle. [idle-resources-and-eligible-waiting]
- The last unit's waiting and processing times are not available: supported events 1..96 are not all retained; missing [1-39].
- Resource activity over time is not available: supported events 1..96 are not all retained; missing [1-39].
- Whether Inspector 2 is surplus -- whether the order would finish as early without it -- is not decidable from this run; a retry without it can show that.
- Which step limits this design is not decidable from one run. The last unit's times and each resource's activity above are measurements, not that answer; a retry that changes one resource shows whether that change alters completion.

## W10 (attempt)

Design: routing CUT 3 -> ASSEMBLE 4 -> INSPECT 5 ticks; one order of 12 units at tick 0; resources in order: Cutter (CUT), Assembler (ASSEMBLE), Inspector (INSPECT), Assembler 2 (ASSEMBLE).

- At tick 10, 8 units (units 5-12) are waiting to start CUT on Cutter. [waiting-work-by-operation-step]
- At tick 10, 1 unit (unit 2) is waiting to start INSPECT on Inspector. [waiting-work-by-operation-step]
- At tick 10, Cutter (CUT) has 1 of 1 slot in use.
- At tick 10, Assembler (ASSEMBLE) has 1 of 1 slot in use.
- At tick 10, Inspector (INSPECT) has 1 of 1 slot in use.
- At tick 10, Assembler 2 (ASSEMBLE) has 0 of 1 slot in use.
- At tick 10, Assembler 2 is idle, and no unit is waiting for a step it can serve (ASSEMBLE). [idle-resources-and-eligible-waiting]
- At tick 10, 0 of 12 units are complete.
- The order completed at tick 67.
- At tick 67, all 12 units are complete and every resource is idle. [idle-resources-and-eligible-waiting]
- The last unit to finish (unit 12) took 67 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 4 on Assembler 2; waited 22 for INSPECT, INSPECT 5 on Inspector. [completing-unit-lead-time-decomposition]
- Cutter processed 12 CUT steps; in use during ticks 0-36. [job-step-occurrences-from-supported-events]
- Assembler processed 6 ASSEMBLE steps; in use during ticks 3-7, 9-13, 15-19, 21-25, 27-31, 33-37. [job-step-occurrences-from-supported-events]
- Inspector processed 12 INSPECT steps; in use during ticks 7-67. [job-step-occurrences-from-supported-events]
- Assembler 2 processed 6 ASSEMBLE steps; in use during ticks 6-10, 12-16, 18-22, 24-28, 30-34, 36-40. [job-step-occurrences-from-supported-events]
- Whether Assembler 2 is surplus -- whether the order would finish as early without it -- is not decidable from this run; a retry without it can show that.
- Which step limits this design is not decidable from one run. The last unit's times and each resource's activity above are measurements, not that answer; a retry that changes one resource shows whether that change alters completion.

Optional completion-chain line: Pacing step (method: completion chain): INSPECT. Tracing the last unit back, every step that started later than its unit was ready waited for INSPECT on Inspector, which worked back to back from tick 7 to 67 (11 such waits).

## W11 (attempt)

Design: routing CUT 3 -> ASSEMBLE 5 -> INSPECT 5 ticks; one order of 12 units at tick 0; resources in order: Cutter (CUT), Assembler (ASSEMBLE), Inspector (INSPECT).

- The order completed at tick 68.
- At tick 68, all 12 units are complete and every resource is idle. [idle-resources-and-eligible-waiting]
- The last unit to finish (unit 12) took 68 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 22 for ASSEMBLE, ASSEMBLE 5 on Assembler; waited 0 for INSPECT, INSPECT 5 on Inspector. [completing-unit-lead-time-decomposition]
- Cutter processed 12 CUT steps; in use during ticks 0-36. [job-step-occurrences-from-supported-events]
- Assembler processed 12 ASSEMBLE steps; in use during ticks 3-63. [job-step-occurrences-from-supported-events]
- Inspector processed 12 INSPECT steps; in use during ticks 8-68. [job-step-occurrences-from-supported-events]
- Which step limits this design is not decidable from one run. The last unit's times and each resource's activity above are measurements, not that answer; a retry that changes one resource shows whether that change alters completion.

Optional completion-chain line: Pacing step (method: completion chain): not unique -- at tick 63, unit 12 became ready for INSPECT at the same tick Inspector was released by unit 11 (INSPECT), so both held it up.

## W12 (comparison)

First design: routing CUT 3 -> ASSEMBLE 5 -> INSPECT 5 ticks; one order of 12 units at tick 0; resources in order: Cutter (CUT), Assembler (ASSEMBLE), Inspector (INSPECT).

Second design: routing CUT 3 -> ASSEMBLE 5 -> INSPECT 5 ticks; one order of 12 units at tick 0; resources in order: Cutter (CUT), Assembler (ASSEMBLE), Inspector (INSPECT), Assembler 2 (ASSEMBLE), Inspector 2 (INSPECT).

- 2 changes: added Assembler 2 (ASSEMBLE); added Inspector 2 (INSPECT). Completion: tick 68 -> tick 46 (22 ticks earlier).
- How much of the difference (22 ticks earlier) each of the 2 changes accounts for is not attributable from this pair; compare one change at a time.

## W13 (attempt)

Design: routing CUT 3 -> ASSEMBLE 4 -> INSPECT 5 ticks; one order of 12 units at tick 0; resources in order: Cutter (CUT), Assembler (ASSEMBLE), Inspector (INSPECT), Shared (ASSEMBLE+INSPECT).

- At tick 17, 6 units (units 7-12) are waiting to start CUT on Cutter. [waiting-work-by-operation-step]
- At tick 17, 1 unit (unit 4) is waiting to start INSPECT; each goes to whichever of Inspector or Shared can take it first and is not assigned to either yet. [waiting-work-by-operation-step]
- At tick 17, Cutter (CUT) has 1 of 1 slot in use.
- At tick 17, Assembler (ASSEMBLE) has 0 of 1 slot in use.
- At tick 17, Inspector (INSPECT) has 1 of 1 slot in use.
- At tick 17, Shared (ASSEMBLE+INSPECT) has 1 of 1 slot in use.
- At tick 17, Assembler is idle, and no unit is waiting for a step it can serve (ASSEMBLE). [idle-resources-and-eligible-waiting]
- At tick 17, 2 of 12 units are complete.
- The order completed at tick 47.
- At tick 47, all 12 units are complete and every resource is idle. [idle-resources-and-eligible-waiting]
- The last unit to finish (unit 12) took 47 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 4 on Assembler; waited 2 for INSPECT, INSPECT 5 on Shared. [completing-unit-lead-time-decomposition]
- Cutter processed 12 CUT steps; in use during ticks 0-36. [job-step-occurrences-from-supported-events]
- Assembler processed 8 ASSEMBLE steps; in use during ticks 3-7, 9-17, 18-26, 27-35, 36-40. [job-step-occurrences-from-supported-events]
- Inspector processed 7 INSPECT steps; in use during ticks 7-12, 13-43. [job-step-occurrences-from-supported-events]
- Shared processed 4 ASSEMBLE and 5 INSPECT steps; in use during ticks 6-47. [job-step-occurrences-from-supported-events]
- Whether Assembler is surplus -- whether the order would finish as early without it -- is not decidable from this run; a retry without it can show that.
- Which step limits this design is not decidable from one run. The last unit's times and each resource's activity above are measurements, not that answer; a retry that changes one resource shows whether that change alters completion.

Optional completion-chain line: Pacing step (method: completion chain): not unique -- at tick 33, unit 11 became ready for ASSEMBLE at the same tick Shared was released by unit 8 (INSPECT), so both held it up.

## W14 (comparison)

First design: routing CUT 3 -> ASSEMBLE 4 -> INSPECT 5 ticks; one order of 12 units at tick 0; resources in order: Cutter (CUT), Assembler (ASSEMBLE), Inspector (INSPECT), Assembler 2 (ASSEMBLE).

Second design: routing CUT 3 -> ASSEMBLE 4 -> INSPECT 5 ticks; one order of 12 units at tick 0; resources in order: Cutter (CUT), Assembler (ASSEMBLE), Inspector (INSPECT).

- One change: removed Assembler 2 (ASSEMBLE). Completion: tick 67 -> tick 67 (no change). Without Assembler 2 the order still completed at tick 67.
- This pair shows the change and the outcome together; it does not show why.

## W15 (comparison)

First design: routing CUT 3 -> ASSEMBLE 4 -> INSPECT 5 ticks; one order of 12 units at tick 0; resources in order: Cutter (CUT), Assembler (ASSEMBLE), Inspector (INSPECT).

Second design: routing CUT 3 -> ASSEMBLE 4 -> INSPECT 5 ticks; one order of 12 units at tick 0; resources in order: Cutter (CUT), Assembler (ASSEMBLE), Inspector (INSPECT), Assembler 2 (ASSEMBLE), Inspector 2 (INSPECT).

- 2 changes: added Assembler 2 (ASSEMBLE); added Inspector 2 (INSPECT). Completion: tick 67 -> tick 45 (22 ticks earlier).
- How much of the difference (22 ticks earlier) each of the 2 changes accounts for is not attributable from this pair; compare one change at a time.

## W16 (comparison)

First design: routing CUT 3 -> ASSEMBLE 4 -> INSPECT 5 ticks; one order of 12 units at tick 0; resources in order: Cutter (CUT), Assembler (ASSEMBLE), Inspector (INSPECT).

Second design: routing CUT 3 -> ASSEMBLE 4 -> INSPECT 5 ticks; one order of 12 units at tick 0; resources in order: Cutter (CUT), Assembler (ASSEMBLE), Inspector (INSPECT), Inspector 2 (INSPECT).

- One change: added Inspector 2 (INSPECT). Completion: tick 67 -> tick 56 (11 ticks earlier).
- This pair shows the change and the outcome together; it does not show why.
