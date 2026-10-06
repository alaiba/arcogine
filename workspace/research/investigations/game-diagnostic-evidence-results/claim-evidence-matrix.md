# Claim-to-evidence matrix

## C1-facts-only

Waiting by step, resource activity, progress, the last unit's times and attempt outcomes; no verdict.

### G1

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of ASSEMBLE / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 33, 3 units (units 9-11) are waiting to start ASSEMBLE on Assembler. |
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of INSPECT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 33, 1 unit (unit 7) is waiting to start INSPECT on Inspector. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 33, Cutter (CUT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 33, Assembler (ASSEMBLE) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 33, Inspector (INSPECT) has 1 of 1 slot in use. |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 33, 5 of 12 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 67. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 67, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,76,81,96,98,105-106 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 67 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 11 for ASSEMBLE, ASSEMBLE 4 on Assembler; waited 11 for INSPECT, INSPECT 5 on Inspector. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-18,23-24,31-32,37-38,42-43,48-49,56-57,64-65,67-68,75-76,81 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,22,26-27,34-35,45-46,53-54,61-62,70-71,78-79,85-86,90-91,95-96,98 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 12 ASSEMBLE steps; in use during ticks 3-51. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,29-30,40-41,51-52,59-60,73-74,83-84,88-89,93-94,100-106 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector processed 12 INSPECT steps; in use during ticks 7-67. |

### G2

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of CUT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 16, 6 units (units 7-12) are waiting to start CUT on Cutter. |
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of ASSEMBLE / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 16, 1 unit (unit 5) is waiting to start ASSEMBLE on Assembler. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Cutter (CUT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Assembler (ASSEMBLE) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Inspector 1 (INSPECT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Inspector 2 (INSPECT) has 0 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Inspector 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 16, Inspector 2 is idle, and no unit is waiting for a step it can serve (INSPECT). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 16, 2 of 12 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 56. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 56, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,72,78,90,92-93,95 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 56 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 11 for ASSEMBLE, ASSEMBLE 4 on Assembler; waited 0 for INSPECT, INSPECT 5 on Inspector 2. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-18,23-24,30-31,36-37,40-41,47-48,54-55,60-61,64-65,71-72,78 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,22,26,28,33,35,43,45,50,52,57,59,67,69,74,76,80,82,84,86,88,90,92 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 12 ASSEMBLE steps; in use during ticks 3-51. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,29,34,46,51,63,68,77,81,87,89,94 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 1 processed 6 INSPECT steps; in use during ticks 7-12, 15-20, 23-28, 31-36, 39-44, 47-52. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 27,39,44,53,58,70,75,83,85,91,93,95 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 2 processed 6 INSPECT steps; in use during ticks 11-16, 19-24, 27-32, 35-40, 43-48, 51-56. |

### G3

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 45. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 45, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,72,76-77,81-82,84 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 45 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 4 on Assembler 2; waited 0 for INSPECT, INSPECT 5 on Inspector 2. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-17,19,22,24,28,30,34,36,40,42,46,48,52,54,58,60,64,66,70,72,76 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,23,31,35,43,47,55,59,67,71,78 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 6 ASSEMBLE steps; in use during ticks 3-7, 9-13, 15-19, 21-25, 27-31, 33-37. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,27,32,39,44,51,56,63,68,75,79,83 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 1 processed 6 INSPECT steps; in use during ticks 7-12, 13-18, 19-24, 25-30, 31-36, 37-42. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 26,33,38,45,50,57,62,69,74,80,82,84 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 2 processed 6 INSPECT steps; in use during ticks 10-15, 16-21, 22-27, 28-33, 34-39, 40-45. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 18,25,29,37,41,49,53,61,65,73,77,81 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 2 processed 6 ASSEMBLE steps; in use during ticks 6-10, 12-16, 18-22, 24-28, 30-34, 36-40. |

### G4

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of CUT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 9, 8 units (units 5-12) are waiting to start CUT on Cutter. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 9, Cutter (CUT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 9, Twin Assembler (ASSEMBLE) has 2 of 2 slots in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 9, Inspector 1 (INSPECT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 9, Inspector 2 (INSPECT) has 0 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Inspector 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 9, Inspector 2 is idle, and no unit is waiting for a step it can serve (INSPECT). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 9, 0 of 12 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 45. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 45, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,72,76-77,81-82,84 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 45 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 4 on Twin Assembler; waited 0 for INSPECT, INSPECT 5 on Inspector 2. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-17,19,22,24,28,30,34,36,40,42,46,48,52,54,58,60,64,66,70,72,76 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,18,20,23,25,29,31,35,37,41,43,47,49,53,55,59,61,65,67,71,73,77-78,81 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Twin Assembler processed 12 ASSEMBLE steps; in use during ticks 3-40; at most 2 of 2 slots in use at once. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,27,32,39,44,51,56,63,68,75,79,83 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 1 processed 6 INSPECT steps; in use during ticks 7-12, 13-18, 19-24, 25-30, 31-36, 37-42. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 26,33,38,45,50,57,62,69,74,80,82,84 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 2 processed 6 INSPECT steps; in use during ticks 10-15, 16-21, 22-27, 28-33, 34-39, 40-45. |

### G5

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 67. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 67, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,74,77-78,83,94-95 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 67 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 4 on Assembler 2; waited 22 for INSPECT, INSPECT 5 on Shared. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-17,19,22,24,29,31,34,36,41,43,46,48,53,55,60,62,65,67,72,74,77 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,23,32,35,44,47,56,61,68,73,81 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 1 processed 6 ASSEMBLE steps; in use during ticks 3-7, 9-13, 15-19, 21-25, 27-31, 33-37. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 18,25,30,37,42,51,54,63,66,75,78,83 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 2 processed 6 ASSEMBLE steps; in use during ticks 6-10, 12-16, 18-22, 24-28, 30-34, 36-40. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 1,96 / fields JOB_DISPATCHED | Assembler 3 processed nothing in this run. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 1,96 / fields JOB_DISPATCHED | Assembler 4 processed nothing in this run. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 1,96 / fields JOB_DISPATCHED | Assembler 5 processed nothing in this run. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 1,96 / fields JOB_DISPATCHED | Assembler 6 processed nothing in this run. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,27-28,39-40,49-50,58-59,70-71,79-80,85-95 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Shared processed 12 INSPECT steps; in use during ticks 7-67. |

### G6

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of CUT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 17, 6 units (units 7-12) are waiting to start CUT on Cutter. |
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of INSPECT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 17, 1 unit (unit 4) is waiting to start INSPECT; each goes to whichever of Inspector or Shared can take it first and is not assigned to either yet. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 17, Cutter (CUT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 17, Assembler (ASSEMBLE) has 0 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 17, Inspector (INSPECT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 17, Shared (ASSEMBLE+INSPECT) has 1 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Assembler is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 17, Assembler is idle, and no unit is waiting for a step it can serve (ASSEMBLE). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 17, 2 of 12 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 47. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 47, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,79,82-83,88,91,93 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 47 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 4 on Assembler; waited 2 for INSPECT, INSPECT 5 on Shared. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-17,19,22,24,28-29,35,37,42,44,47-48,56,58,61,63,68-69,77,79,82 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,23,31,33,38,43,50-51,59,62,71-72,80,83,88 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 8 ASSEMBLE steps; in use during ticks 3-7, 9-17, 18-26, 27-35, 36-40. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,27,32,40-41,53-54,64-65,74-75,86-87,92 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector processed 7 INSPECT steps; in use during ticks 7-12, 13-43. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 18,25-26,34,36,45-46,55,57,66-67,76,78,84-85,90-91,93 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Shared processed 4 ASSEMBLE and 5 INSPECT steps; in use during ticks 6-47. |

### G7

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 68. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 68, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,68,70,91,93-95 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 68 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 22 for ASSEMBLE, ASSEMBLE 5 on Assembler; waited 0 for INSPECT, INSPECT 5 on Inspector. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-18,23-24,26-27,33-34,40-41,43-44,50-51,53-54,60-61,67-68,70 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,22,30,32,37,39,47,49,57,59,64,66,73,75,77,79,81,83,85,87,89,91,93 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 12 ASSEMBLE steps; in use during ticks 3-63. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,29,31,36,38,46,48,56,58,63,65,72,74,76,78,80,82,84,86,88,90,92,94-95 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector processed 12 INSPECT steps; in use during ticks 8-68. |

### G8

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of CUT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 10, 8 units (units 5-12) are waiting to start CUT on Cutter. |
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of INSPECT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 10, 1 unit (unit 2) is waiting to start INSPECT on Inspector. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 10, Cutter (CUT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 10, Assembler (ASSEMBLE) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 10, Inspector (INSPECT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 10, Assembler 2 (ASSEMBLE) has 0 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Assembler 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 10, Assembler 2 is idle, and no unit is waiting for a step it can serve (ASSEMBLE). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 10, 0 of 12 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 67. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 67, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,74,77-78,83,94-95 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 67 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 4 on Assembler 2; waited 22 for INSPECT, INSPECT 5 on Inspector. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-17,19,22,24,29,31,34,36,41,43,46,48,53,55,60,62,65,67,72,74,77 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,23,32,35,44,47,56,61,68,73,81 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 6 ASSEMBLE steps; in use during ticks 3-7, 9-13, 15-19, 21-25, 27-31, 33-37. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,27-28,39-40,49-50,58-59,70-71,79-80,85-95 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector processed 12 INSPECT steps; in use during ticks 7-67. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 18,25,30,37,42,51,54,63,66,75,78,83 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 2 processed 6 ASSEMBLE steps; in use during ticks 6-10, 12-16, 18-22, 24-28, 30-34, 36-40. |

### G9a

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 11. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 11, all 2 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1-2,4-5,9,14-15 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 1) took 11 ticks from order acceptance: waited 0 for CUT, CUT 2 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 3 on Assembler; waited 4 for INSPECT, INSPECT 2 on Shared. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,4,6-7 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 2 CUT steps; in use during ticks 0-4. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 5,9 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 1 ASSEMBLE step; in use during ticks 2-5. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 8,11-15 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Shared processed 1 ASSEMBLE and 2 INSPECT steps; in use during ticks 4-11. |

### G9b

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 9. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 9, all 2 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,6-8,11,14-15 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 2) took 9 ticks from order acceptance: waited 2 for CUT, CUT 2 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 3 on Assembler; waited 0 for INSPECT, INSPECT 2 on Shared. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 5,9-10,13-15 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Shared processed 1 ASSEMBLE and 2 INSPECT steps; in use during ticks 2-9. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 8,11 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 1 ASSEMBLE step; in use during ticks 4-7. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,4,6-7 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 2 CUT steps; in use during ticks 0-4. |

### G10

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of CUT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 16, 6 units (units 7-12) are waiting to start CUT on Cutter. |
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of ASSEMBLE / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 16, 1 unit (unit 5) is waiting to start ASSEMBLE on Assembler. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Cutter (CUT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Assembler (ASSEMBLE) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Inspector 1 (INSPECT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Inspector 2 (INSPECT) has 0 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Inspector 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 16, Inspector 2 is idle, and no unit is waiting for a step it can serve (INSPECT). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 16, 2 of 12 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 56. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 56, all 12 units are complete and every resource is idle. |
| Q4_DELAY | REFUSAL |  | obs closing | The last unit's waiting and processing times are not available: supported events 1..96 are not all retained; missing [1-39]. |
| ACTIVITY | REFUSAL |  | obs closing | Resource activity over time is not available: supported events 1..96 are not all retained; missing [1-39]. |

### G11a

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of ASSEMBLE / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 5, 3 units (units 3-5) are waiting to start ASSEMBLE; each goes to whichever of Assembler 1 or Assembler 2 can take it first and is not assigned to either yet. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 5, Cutter (CUT) has 0 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 5, Assembler 1 (ASSEMBLE) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 5, Assembler 2 (ASSEMBLE) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 5, Inspector (INSPECT) has 0 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Cutter is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 5, Cutter is idle, and no unit is waiting for a step it can serve (CUT). |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Inspector is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 5, Inspector is idle, and no unit is waiting for a step it can serve (INSPECT). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 5, 0 of 5 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 26. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 26, all 5 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,17,19,32,38-40 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 5) took 26 ticks from order acceptance: waited 4 for CUT, CUT 1 on Cutter; waited 12 for ASSEMBLE, ASSEMBLE 8 on Assembler 1; waited 0 for INSPECT, INSPECT 1 on Inspector. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,7,9-10,12-14,16-17,19 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 5 CUT steps; in use during ticks 0-5. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 8,21,23,30,32,38 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 1 processed 3 ASSEMBLE steps; in use during ticks 1-25. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 11,24-25,33 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 2 processed 2 ASSEMBLE steps; in use during ticks 2-18. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 22,27-29,31,35-37,39-40 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector processed 5 INSPECT steps; in use during ticks 9-11, 17-19, 25-26. |

### G11b

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of ASSEMBLE / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 3, 1 unit (unit 3) is waiting to start ASSEMBLE on Assembler. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 3, Cutter (CUT) has 0 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 3, Assembler (ASSEMBLE) has 2 of 2 slots in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 3, Inspector (INSPECT) has 0 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Cutter is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 3, Cutter is idle, and no unit is waiting for a step it can serve (CUT). |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Inspector is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 3, Inspector is idle, and no unit is waiting for a step it can serve (INSPECT). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 3, 0 of 3 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 26. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 26, all 3 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,10-11,15,21-23 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 3) took 26 ticks from order acceptance: waited 2 for CUT, CUT 1 on Cutter; waited 10 for ASSEMBLE, ASSEMBLE 12 on Assembler; waited 0 for INSPECT, INSPECT 1 on Inspector. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,5,7-8,10-11 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 3 CUT steps; in use during ticks 0-3. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 6,9,13,15-16,21 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 3 ASSEMBLE steps; in use during ticks 1-25; at most 2 of 2 slots in use at once. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 14,18-20,22-23 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector processed 3 INSPECT steps; in use during ticks 13-15, 25-26. |

### G1+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). Completion: tick 67 -> tick 67 (no change). |

### G1+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 2 (ASSEMBLE). Completion: tick 67 -> tick 67 (no change). |

### G1+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 2 (INSPECT). Completion: tick 67 -> tick 56 (11 ticks earlier). |

### G2+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). Completion: tick 56 -> tick 56 (no change). |

### G2+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 2 (ASSEMBLE). Completion: tick 56 -> tick 45 (11 ticks earlier). |

### G2+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 3 (INSPECT). Completion: tick 56 -> tick 56 (no change). |

### G3+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). Completion: tick 45 -> tick 37 (8 ticks earlier). |

### G3+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 3 (ASSEMBLE). Completion: tick 45 -> tick 45 (no change). |

### G3+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 3 (INSPECT). Completion: tick 45 -> tick 45 (no change). |

### G4+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). Completion: tick 45 -> tick 37 (8 ticks earlier). |

### G4+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 2 (ASSEMBLE). Completion: tick 45 -> tick 45 (no change). |

### G4+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 3 (INSPECT). Completion: tick 45 -> tick 45 (no change). |

### G5+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). Completion: tick 67 -> tick 67 (no change). |

### G5+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 7 (ASSEMBLE). Completion: tick 67 -> tick 67 (no change). |

### G5+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 2 (INSPECT). Completion: tick 67 -> tick 45 (22 ticks earlier). |

### G7+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). Completion: tick 68 -> tick 68 (no change). |

### G7+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 2 (ASSEMBLE). Completion: tick 68 -> tick 68 (no change). |

### G7+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 2 (INSPECT). Completion: tick 68 -> tick 68 (no change). |

### G2-remove-Inspector-2

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: removed Inspector 2 (INSPECT). Completion: tick 56 -> tick 67 (11 ticks later). Without Inspector 2 the order completed 11 ticks later. |

### G8-remove-Assembler-2

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: removed Assembler 2 (ASSEMBLE). Completion: tick 67 -> tick 67 (no change). Without Assembler 2 the order still completed at tick 67. |

### G9-reorder

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: resource order [Cutter, Assembler, Shared] -> [Shared, Assembler, Cutter]. Completion: tick 11 -> tick 9 (2 ticks earlier). |

### G1+assemble+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q6_CONFOUNDED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | 2 changes: added Assembler 2 (ASSEMBLE); added Inspector 2 (INSPECT). Completion: tick 67 -> tick 45 (22 ticks earlier). |

### G1+cut+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q6_CONFOUNDED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | 2 changes: added Cutter 2 (CUT); added Inspector 2 (INSPECT). Completion: tick 67 -> tick 56 (11 ticks earlier). |

### G7+assemble+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q6_CONFOUNDED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | 2 changes: added Assembler 2 (ASSEMBLE); added Inspector 2 (INSPECT). Completion: tick 68 -> tick 46 (22 ticks earlier). |

## C2a-named-pool-occupancy

Facts plus a 'most occupied eligibility pool' bottleneck verdict with its job-tick numbers.

### G1

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of ASSEMBLE / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 33, 3 units (units 9-11) are waiting to start ASSEMBLE on Assembler. |
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of INSPECT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 33, 1 unit (unit 7) is waiting to start INSPECT on Inspector. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 33, Cutter (CUT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 33, Assembler (ASSEMBLE) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 33, Inspector (INSPECT) has 1 of 1 slot in use. |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 33, 5 of 12 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 67. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 67, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,76,81,96,98,105-106 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 67 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 11 for ASSEMBLE, ASSEMBLE 4 on Assembler; waited 11 for INSPECT, INSPECT 5 on Inspector. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-18,23-24,31-32,37-38,42-43,48-49,56-57,64-65,67-68,75-76,81 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,22,26-27,34-35,45-46,53-54,61-62,70-71,78-79,85-86,90-91,95-96,98 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 12 ASSEMBLE steps; in use during ticks 3-51. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,29-30,40-41,51-52,59-60,73-74,83-84,88-89,93-94,100-106 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector processed 12 INSPECT steps; in use during ticks 7-67. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,107 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 198 ticks over 12 units; ASSEMBLE 66 ticks over 12 units; INSPECT 66 ticks over 12 units. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION | eligibility-pool-occupancy-of-continuously-online-resources | obs closing / events 1,107 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED,ResourceObservation.concurrency | Bottleneck (method: most occupied eligibility pool): INSPECT -- CUT 36 of 67 job-ticks; ASSEMBLE 48 of 67 job-ticks; INSPECT 60 of 67 job-ticks. |

### G2

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of CUT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 16, 6 units (units 7-12) are waiting to start CUT on Cutter. |
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of ASSEMBLE / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 16, 1 unit (unit 5) is waiting to start ASSEMBLE on Assembler. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Cutter (CUT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Assembler (ASSEMBLE) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Inspector 1 (INSPECT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Inspector 2 (INSPECT) has 0 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Inspector 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 16, Inspector 2 is idle, and no unit is waiting for a step it can serve (INSPECT). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 16, 2 of 12 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 56. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 56, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,72,78,90,92-93,95 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 56 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 11 for ASSEMBLE, ASSEMBLE 4 on Assembler; waited 0 for INSPECT, INSPECT 5 on Inspector 2. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-18,23-24,30-31,36-37,40-41,47-48,54-55,60-61,64-65,71-72,78 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,22,26,28,33,35,43,45,50,52,57,59,67,69,74,76,80,82,84,86,88,90,92 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 12 ASSEMBLE steps; in use during ticks 3-51. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,29,34,46,51,63,68,77,81,87,89,94 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 1 processed 6 INSPECT steps; in use during ticks 7-12, 15-20, 23-28, 31-36, 39-44, 47-52. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 27,39,44,53,58,70,75,83,85,91,93,95 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 2 processed 6 INSPECT steps; in use during ticks 11-16, 19-24, 27-32, 35-40, 43-48, 51-56. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,96 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 198 ticks over 12 units; ASSEMBLE 66 ticks over 12 units; INSPECT 0 ticks over 12 units. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION | eligibility-pool-occupancy-of-continuously-online-resources | obs closing / events 1,96 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED,ResourceObservation.concurrency | Bottleneck (method: most occupied eligibility pool): ASSEMBLE -- CUT 36 of 56 job-ticks; ASSEMBLE 48 of 56 job-ticks; INSPECT 60 of 112 job-ticks. |

### G3

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 45. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 45, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,72,76-77,81-82,84 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 45 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 4 on Assembler 2; waited 0 for INSPECT, INSPECT 5 on Inspector 2. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-17,19,22,24,28,30,34,36,40,42,46,48,52,54,58,60,64,66,70,72,76 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,23,31,35,43,47,55,59,67,71,78 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 6 ASSEMBLE steps; in use during ticks 3-7, 9-13, 15-19, 21-25, 27-31, 33-37. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,27,32,39,44,51,56,63,68,75,79,83 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 1 processed 6 INSPECT steps; in use during ticks 7-12, 13-18, 19-24, 25-30, 31-36, 37-42. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 26,33,38,45,50,57,62,69,74,80,82,84 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 2 processed 6 INSPECT steps; in use during ticks 10-15, 16-21, 22-27, 28-33, 34-39, 40-45. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 18,25,29,37,41,49,53,61,65,73,77,81 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 2 processed 6 ASSEMBLE steps; in use during ticks 6-10, 12-16, 18-22, 24-28, 30-34, 36-40. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,85 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 198 ticks over 12 units; ASSEMBLE 0 ticks over 12 units; INSPECT 0 ticks over 12 units. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION | eligibility-pool-occupancy-of-continuously-online-resources | obs closing / events 1,85 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED,ResourceObservation.concurrency | Bottleneck (method: most occupied eligibility pool): CUT -- CUT 36 of 45 job-ticks; ASSEMBLE 48 of 90 job-ticks; INSPECT 60 of 90 job-ticks. |

### G4

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of CUT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 9, 8 units (units 5-12) are waiting to start CUT on Cutter. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 9, Cutter (CUT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 9, Twin Assembler (ASSEMBLE) has 2 of 2 slots in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 9, Inspector 1 (INSPECT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 9, Inspector 2 (INSPECT) has 0 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Inspector 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 9, Inspector 2 is idle, and no unit is waiting for a step it can serve (INSPECT). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 9, 0 of 12 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 45. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 45, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,72,76-77,81-82,84 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 45 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 4 on Twin Assembler; waited 0 for INSPECT, INSPECT 5 on Inspector 2. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-17,19,22,24,28,30,34,36,40,42,46,48,52,54,58,60,64,66,70,72,76 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,18,20,23,25,29,31,35,37,41,43,47,49,53,55,59,61,65,67,71,73,77-78,81 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Twin Assembler processed 12 ASSEMBLE steps; in use during ticks 3-40; at most 2 of 2 slots in use at once. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,27,32,39,44,51,56,63,68,75,79,83 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 1 processed 6 INSPECT steps; in use during ticks 7-12, 13-18, 19-24, 25-30, 31-36, 37-42. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 26,33,38,45,50,57,62,69,74,80,82,84 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 2 processed 6 INSPECT steps; in use during ticks 10-15, 16-21, 22-27, 28-33, 34-39, 40-45. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,85 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 198 ticks over 12 units; ASSEMBLE 0 ticks over 12 units; INSPECT 0 ticks over 12 units. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION | eligibility-pool-occupancy-of-continuously-online-resources | obs closing / events 1,85 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED,ResourceObservation.concurrency | Bottleneck (method: most occupied eligibility pool): CUT -- CUT 36 of 45 job-ticks; ASSEMBLE 48 of 90 job-ticks; INSPECT 60 of 90 job-ticks. |

### G5

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 67. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 67, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,74,77-78,83,94-95 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 67 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 4 on Assembler 2; waited 22 for INSPECT, INSPECT 5 on Shared. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-17,19,22,24,29,31,34,36,41,43,46,48,53,55,60,62,65,67,72,74,77 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,23,32,35,44,47,56,61,68,73,81 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 1 processed 6 ASSEMBLE steps; in use during ticks 3-7, 9-13, 15-19, 21-25, 27-31, 33-37. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 18,25,30,37,42,51,54,63,66,75,78,83 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 2 processed 6 ASSEMBLE steps; in use during ticks 6-10, 12-16, 18-22, 24-28, 30-34, 36-40. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 1,96 / fields JOB_DISPATCHED | Assembler 3 processed nothing in this run. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 1,96 / fields JOB_DISPATCHED | Assembler 4 processed nothing in this run. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 1,96 / fields JOB_DISPATCHED | Assembler 5 processed nothing in this run. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 1,96 / fields JOB_DISPATCHED | Assembler 6 processed nothing in this run. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,27-28,39-40,49-50,58-59,70-71,79-80,85-95 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Shared processed 12 INSPECT steps; in use during ticks 7-67. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,96 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 198 ticks over 12 units; ASSEMBLE 0 ticks over 12 units; INSPECT 132 ticks over 12 units. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION | eligibility-pool-occupancy-of-continuously-online-resources | obs closing / events 1,96 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED,ResourceObservation.concurrency | Bottleneck (method: most occupied eligibility pool): CUT -- CUT 36 of 67 job-ticks; ASSEMBLE+INSPECT 108 of 469 job-ticks. |

### G6

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of CUT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 17, 6 units (units 7-12) are waiting to start CUT on Cutter. |
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of INSPECT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 17, 1 unit (unit 4) is waiting to start INSPECT; each goes to whichever of Inspector or Shared can take it first and is not assigned to either yet. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 17, Cutter (CUT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 17, Assembler (ASSEMBLE) has 0 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 17, Inspector (INSPECT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 17, Shared (ASSEMBLE+INSPECT) has 1 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Assembler is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 17, Assembler is idle, and no unit is waiting for a step it can serve (ASSEMBLE). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 17, 2 of 12 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 47. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 47, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,79,82-83,88,91,93 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 47 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 4 on Assembler; waited 2 for INSPECT, INSPECT 5 on Shared. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-17,19,22,24,28-29,35,37,42,44,47-48,56,58,61,63,68-69,77,79,82 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,23,31,33,38,43,50-51,59,62,71-72,80,83,88 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 8 ASSEMBLE steps; in use during ticks 3-7, 9-17, 18-26, 27-35, 36-40. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,27,32,40-41,53-54,64-65,74-75,86-87,92 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector processed 7 INSPECT steps; in use during ticks 7-12, 13-43. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 18,25-26,34,36,45-46,55,57,66-67,76,78,84-85,90-91,93 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Shared processed 4 ASSEMBLE and 5 INSPECT steps; in use during ticks 6-47. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,94 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 198 ticks over 12 units; ASSEMBLE 3 ticks over 12 units; INSPECT 11 ticks over 12 units. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION | eligibility-pool-occupancy-of-continuously-online-resources | obs closing / events 1,94 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED,ResourceObservation.concurrency | Bottleneck (method: most occupied eligibility pool): CUT and ASSEMBLE+INSPECT -- CUT 36 of 47 job-ticks; ASSEMBLE+INSPECT 108 of 141 job-ticks. |

### G7

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 68. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 68, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,68,70,91,93-95 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 68 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 22 for ASSEMBLE, ASSEMBLE 5 on Assembler; waited 0 for INSPECT, INSPECT 5 on Inspector. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-18,23-24,26-27,33-34,40-41,43-44,50-51,53-54,60-61,67-68,70 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,22,30,32,37,39,47,49,57,59,64,66,73,75,77,79,81,83,85,87,89,91,93 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 12 ASSEMBLE steps; in use during ticks 3-63. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,29,31,36,38,46,48,56,58,63,65,72,74,76,78,80,82,84,86,88,90,92,94-95 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector processed 12 INSPECT steps; in use during ticks 8-68. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,96 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 198 ticks over 12 units; ASSEMBLE 132 ticks over 12 units; INSPECT 0 ticks over 12 units. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION | eligibility-pool-occupancy-of-continuously-online-resources | obs closing / events 1,96 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED,ResourceObservation.concurrency | Bottleneck (method: most occupied eligibility pool): ASSEMBLE and INSPECT -- CUT 36 of 68 job-ticks; ASSEMBLE 60 of 68 job-ticks; INSPECT 60 of 68 job-ticks. |

### G8

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of CUT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 10, 8 units (units 5-12) are waiting to start CUT on Cutter. |
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of INSPECT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 10, 1 unit (unit 2) is waiting to start INSPECT on Inspector. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 10, Cutter (CUT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 10, Assembler (ASSEMBLE) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 10, Inspector (INSPECT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 10, Assembler 2 (ASSEMBLE) has 0 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Assembler 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 10, Assembler 2 is idle, and no unit is waiting for a step it can serve (ASSEMBLE). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 10, 0 of 12 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 67. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 67, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,74,77-78,83,94-95 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 67 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 4 on Assembler 2; waited 22 for INSPECT, INSPECT 5 on Inspector. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-17,19,22,24,29,31,34,36,41,43,46,48,53,55,60,62,65,67,72,74,77 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,23,32,35,44,47,56,61,68,73,81 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 6 ASSEMBLE steps; in use during ticks 3-7, 9-13, 15-19, 21-25, 27-31, 33-37. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,27-28,39-40,49-50,58-59,70-71,79-80,85-95 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector processed 12 INSPECT steps; in use during ticks 7-67. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 18,25,30,37,42,51,54,63,66,75,78,83 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 2 processed 6 ASSEMBLE steps; in use during ticks 6-10, 12-16, 18-22, 24-28, 30-34, 36-40. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,96 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 198 ticks over 12 units; ASSEMBLE 0 ticks over 12 units; INSPECT 132 ticks over 12 units. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION | eligibility-pool-occupancy-of-continuously-online-resources | obs closing / events 1,96 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED,ResourceObservation.concurrency | Bottleneck (method: most occupied eligibility pool): INSPECT -- CUT 36 of 67 job-ticks; ASSEMBLE 48 of 134 job-ticks; INSPECT 60 of 67 job-ticks. |

### G9a

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 11. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 11, all 2 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1-2,4-5,9,14-15 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 1) took 11 ticks from order acceptance: waited 0 for CUT, CUT 2 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 3 on Assembler; waited 4 for INSPECT, INSPECT 2 on Shared. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,4,6-7 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 2 CUT steps; in use during ticks 0-4. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 5,9 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 1 ASSEMBLE step; in use during ticks 2-5. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 8,11-15 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Shared processed 1 ASSEMBLE and 2 INSPECT steps; in use during ticks 4-11. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,16 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 2 ticks over 2 units; ASSEMBLE 0 ticks over 2 units; INSPECT 4 ticks over 2 units. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION | eligibility-pool-occupancy-of-continuously-online-resources | obs closing / events 1,16 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED,ResourceObservation.concurrency | Bottleneck (method: most occupied eligibility pool): ASSEMBLE+INSPECT -- CUT 4 of 11 job-ticks; ASSEMBLE+INSPECT 10 of 22 job-ticks. |

### G9b

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 9. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 9, all 2 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,6-8,11,14-15 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 2) took 9 ticks from order acceptance: waited 2 for CUT, CUT 2 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 3 on Assembler; waited 0 for INSPECT, INSPECT 2 on Shared. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 5,9-10,13-15 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Shared processed 1 ASSEMBLE and 2 INSPECT steps; in use during ticks 2-9. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 8,11 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 1 ASSEMBLE step; in use during ticks 4-7. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,4,6-7 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 2 CUT steps; in use during ticks 0-4. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,16 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 2 ticks over 2 units; ASSEMBLE 0 ticks over 2 units; INSPECT 0 ticks over 2 units. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION | eligibility-pool-occupancy-of-continuously-online-resources | obs closing / events 1,16 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED,ResourceObservation.concurrency | Bottleneck (method: most occupied eligibility pool): ASSEMBLE+INSPECT -- CUT 4 of 9 job-ticks; ASSEMBLE+INSPECT 10 of 18 job-ticks. |

### G10

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of CUT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 16, 6 units (units 7-12) are waiting to start CUT on Cutter. |
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of ASSEMBLE / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 16, 1 unit (unit 5) is waiting to start ASSEMBLE on Assembler. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Cutter (CUT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Assembler (ASSEMBLE) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Inspector 1 (INSPECT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Inspector 2 (INSPECT) has 0 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Inspector 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 16, Inspector 2 is idle, and no unit is waiting for a step it can serve (INSPECT). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 16, 2 of 12 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 56. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 56, all 12 units are complete and every resource is idle. |
| Q4_DELAY | REFUSAL |  | obs closing | The last unit's waiting and processing times are not available: supported events 1..96 are not all retained; missing [1-39]. |
| ACTIVITY | REFUSAL |  | obs closing | Resource activity over time is not available: supported events 1..96 are not all retained; missing [1-39]. |
| Q4_DELAY | REFUSAL |  | obs closing | Waiting summed over all units is not available: supported events 1..96 are not all retained; missing [1-39]. |
| Q2_LIMITING_STEP | REFUSAL |  | obs closing | Pool occupancy is not available: supported events 1..96 are not all retained; missing [1-39]. |

### G11a

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of ASSEMBLE / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 5, 3 units (units 3-5) are waiting to start ASSEMBLE; each goes to whichever of Assembler 1 or Assembler 2 can take it first and is not assigned to either yet. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 5, Cutter (CUT) has 0 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 5, Assembler 1 (ASSEMBLE) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 5, Assembler 2 (ASSEMBLE) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 5, Inspector (INSPECT) has 0 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Cutter is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 5, Cutter is idle, and no unit is waiting for a step it can serve (CUT). |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Inspector is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 5, Inspector is idle, and no unit is waiting for a step it can serve (INSPECT). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 5, 0 of 5 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 26. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 26, all 5 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,17,19,32,38-40 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 5) took 26 ticks from order acceptance: waited 4 for CUT, CUT 1 on Cutter; waited 12 for ASSEMBLE, ASSEMBLE 8 on Assembler 1; waited 0 for INSPECT, INSPECT 1 on Inspector. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,7,9-10,12-14,16-17,19 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 5 CUT steps; in use during ticks 0-5. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 8,21,23,30,32,38 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 1 processed 3 ASSEMBLE steps; in use during ticks 1-25. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 11,24-25,33 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 2 processed 2 ASSEMBLE steps; in use during ticks 2-18. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 22,27-29,31,35-37,39-40 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector processed 5 INSPECT steps; in use during ticks 9-11, 17-19, 25-26. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,41 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 10 ticks over 5 units; ASSEMBLE 24 ticks over 5 units; INSPECT 0 ticks over 5 units. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION | eligibility-pool-occupancy-of-continuously-online-resources | obs closing / events 1,41 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED,ResourceObservation.concurrency | Bottleneck (method: most occupied eligibility pool): ASSEMBLE -- CUT 5 of 26 job-ticks; ASSEMBLE 40 of 52 job-ticks; INSPECT 5 of 26 job-ticks. |

### G11b

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of ASSEMBLE / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 3, 1 unit (unit 3) is waiting to start ASSEMBLE on Assembler. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 3, Cutter (CUT) has 0 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 3, Assembler (ASSEMBLE) has 2 of 2 slots in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 3, Inspector (INSPECT) has 0 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Cutter is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 3, Cutter is idle, and no unit is waiting for a step it can serve (CUT). |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Inspector is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 3, Inspector is idle, and no unit is waiting for a step it can serve (INSPECT). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 3, 0 of 3 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 26. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 26, all 3 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,10-11,15,21-23 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 3) took 26 ticks from order acceptance: waited 2 for CUT, CUT 1 on Cutter; waited 10 for ASSEMBLE, ASSEMBLE 12 on Assembler; waited 0 for INSPECT, INSPECT 1 on Inspector. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,5,7-8,10-11 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 3 CUT steps; in use during ticks 0-3. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 6,9,13,15-16,21 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 3 ASSEMBLE steps; in use during ticks 1-25; at most 2 of 2 slots in use at once. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 14,18-20,22-23 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector processed 3 INSPECT steps; in use during ticks 13-15, 25-26. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,24 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 3 ticks over 3 units; ASSEMBLE 10 ticks over 3 units; INSPECT 0 ticks over 3 units. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION | eligibility-pool-occupancy-of-continuously-online-resources | obs closing / events 1,24 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED,ResourceObservation.concurrency | Bottleneck (method: most occupied eligibility pool): ASSEMBLE -- CUT 3 of 26 job-ticks; ASSEMBLE 36 of 52 job-ticks; INSPECT 3 of 26 job-ticks. |

### G1+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). Completion: tick 67 -> tick 67 (no change). |

### G1+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 2 (ASSEMBLE). Completion: tick 67 -> tick 67 (no change). |

### G1+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 2 (INSPECT). Completion: tick 67 -> tick 56 (11 ticks earlier). |

### G2+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). Completion: tick 56 -> tick 56 (no change). |

### G2+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 2 (ASSEMBLE). Completion: tick 56 -> tick 45 (11 ticks earlier). |

### G2+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 3 (INSPECT). Completion: tick 56 -> tick 56 (no change). |

### G3+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). Completion: tick 45 -> tick 37 (8 ticks earlier). |

### G3+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 3 (ASSEMBLE). Completion: tick 45 -> tick 45 (no change). |

### G3+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 3 (INSPECT). Completion: tick 45 -> tick 45 (no change). |

### G4+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). Completion: tick 45 -> tick 37 (8 ticks earlier). |

### G4+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 2 (ASSEMBLE). Completion: tick 45 -> tick 45 (no change). |

### G4+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 3 (INSPECT). Completion: tick 45 -> tick 45 (no change). |

### G5+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). Completion: tick 67 -> tick 67 (no change). |

### G5+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 7 (ASSEMBLE). Completion: tick 67 -> tick 67 (no change). |

### G5+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 2 (INSPECT). Completion: tick 67 -> tick 45 (22 ticks earlier). |

### G7+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). Completion: tick 68 -> tick 68 (no change). |

### G7+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 2 (ASSEMBLE). Completion: tick 68 -> tick 68 (no change). |

### G7+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 2 (INSPECT). Completion: tick 68 -> tick 68 (no change). |

### G2-remove-Inspector-2

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: removed Inspector 2 (INSPECT). Completion: tick 56 -> tick 67 (11 ticks later). Without Inspector 2 the order completed 11 ticks later. |

### G8-remove-Assembler-2

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: removed Assembler 2 (ASSEMBLE). Completion: tick 67 -> tick 67 (no change). Without Assembler 2 the order still completed at tick 67. |

### G9-reorder

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: resource order [Cutter, Assembler, Shared] -> [Shared, Assembler, Cutter]. Completion: tick 11 -> tick 9 (2 ticks earlier). |

### G1+assemble+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q6_CONFOUNDED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | 2 changes: added Assembler 2 (ASSEMBLE); added Inspector 2 (INSPECT). Completion: tick 67 -> tick 45 (22 ticks earlier). |

### G1+cut+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q6_CONFOUNDED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | 2 changes: added Cutter 2 (CUT); added Inspector 2 (INSPECT). Completion: tick 67 -> tick 56 (11 ticks earlier). |

### G7+assemble+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q6_CONFOUNDED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | 2 changes: added Assembler 2 (ASSEMBLE); added Inspector 2 (INSPECT). Completion: tick 68 -> tick 46 (22 ticks earlier). |

## C2b-named-completion-chain

Facts plus a 'completion chain' pacing-step verdict with the chain's evidence; the method itself reports a tie.

### G1

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of ASSEMBLE / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 33, 3 units (units 9-11) are waiting to start ASSEMBLE on Assembler. |
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of INSPECT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 33, 1 unit (unit 7) is waiting to start INSPECT on Inspector. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 33, Cutter (CUT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 33, Assembler (ASSEMBLE) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 33, Inspector (INSPECT) has 1 of 1 slot in use. |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 33, 5 of 12 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 67. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 67, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,76,81,96,98,105-106 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 67 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 11 for ASSEMBLE, ASSEMBLE 4 on Assembler; waited 11 for INSPECT, INSPECT 5 on Inspector. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-18,23-24,31-32,37-38,42-43,48-49,56-57,64-65,67-68,75-76,81 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,22,26-27,34-35,45-46,53-54,61-62,70-71,78-79,85-86,90-91,95-96,98 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 12 ASSEMBLE steps; in use during ticks 3-51. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,29-30,40-41,51-52,59-60,73-74,83-84,88-89,93-94,100-106 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector processed 12 INSPECT steps; in use during ticks 7-67. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,107 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 198 ticks over 12 units; ASSEMBLE 66 ticks over 12 units; INSPECT 66 ticks over 12 units. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION | completion-chain-from-supported-events | events 1-2,14-15,20-21,26,29-30,34,40-41,45,51-53,59-61,70,73-74,78,83-85,88-90,93-95,98,100-106 / fields ORDER_COMPLETED.jobId,JOB_DISPATCHED,JOB_STEP_COMPLETED | Pacing step (method: completion chain): INSPECT. Tracing the last unit back, every step that started later than its unit was ready waited for INSPECT on Inspector, which worked back to back from tick 7 to 67 (11 such waits). |

### G2

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of CUT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 16, 6 units (units 7-12) are waiting to start CUT on Cutter. |
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of ASSEMBLE / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 16, 1 unit (unit 5) is waiting to start ASSEMBLE on Assembler. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Cutter (CUT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Assembler (ASSEMBLE) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Inspector 1 (INSPECT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Inspector 2 (INSPECT) has 0 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Inspector 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 16, Inspector 2 is idle, and no unit is waiting for a step it can serve (INSPECT). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 16, 2 of 12 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 56. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 56, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,72,78,90,92-93,95 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 56 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 11 for ASSEMBLE, ASSEMBLE 4 on Assembler; waited 0 for INSPECT, INSPECT 5 on Inspector 2. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-18,23-24,30-31,36-37,40-41,47-48,54-55,60-61,64-65,71-72,78 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,22,26,28,33,35,43,45,50,52,57,59,67,69,74,76,80,82,84,86,88,90,92 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 12 ASSEMBLE steps; in use during ticks 3-51. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,29,34,46,51,63,68,77,81,87,89,94 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 1 processed 6 INSPECT steps; in use during ticks 7-12, 15-20, 23-28, 31-36, 39-44, 47-52. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 27,39,44,53,58,70,75,83,85,91,93,95 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 2 processed 6 INSPECT steps; in use during ticks 11-16, 19-24, 27-32, 35-40, 43-48, 51-56. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,96 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 198 ticks over 12 units; ASSEMBLE 66 ticks over 12 units; INSPECT 0 ticks over 12 units. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION | completion-chain-from-supported-events | events 1-2,14-15,17,20,22-23,26,28,30,33,35-36,40,43,45,47,50,52,54,57,59-60,64,67,69,71,74,76,78,80,82,84,86,88,90,92-93,95 / fields ORDER_COMPLETED.jobId,JOB_DISPATCHED,JOB_STEP_COMPLETED | Pacing step (method: completion chain): ASSEMBLE. Tracing the last unit back, every step that started later than its unit was ready waited for ASSEMBLE on Assembler, which worked back to back from tick 3 to 51 (11 such waits). |

### G3

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 45. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 45, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,72,76-77,81-82,84 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 45 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 4 on Assembler 2; waited 0 for INSPECT, INSPECT 5 on Inspector 2. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-17,19,22,24,28,30,34,36,40,42,46,48,52,54,58,60,64,66,70,72,76 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,23,31,35,43,47,55,59,67,71,78 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 6 ASSEMBLE steps; in use during ticks 3-7, 9-13, 15-19, 21-25, 27-31, 33-37. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,27,32,39,44,51,56,63,68,75,79,83 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 1 processed 6 INSPECT steps; in use during ticks 7-12, 13-18, 19-24, 25-30, 31-36, 37-42. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 26,33,38,45,50,57,62,69,74,80,82,84 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 2 processed 6 INSPECT steps; in use during ticks 10-15, 16-21, 22-27, 28-33, 34-39, 40-45. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 18,25,29,37,41,49,53,61,65,73,77,81 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 2 processed 6 ASSEMBLE steps; in use during ticks 6-10, 12-16, 18-22, 24-28, 30-34, 36-40. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,85 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 198 ticks over 12 units; ASSEMBLE 0 ticks over 12 units; INSPECT 0 ticks over 12 units. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION | completion-chain-from-supported-events | events 1-2,14,16-17,19,22,24,28,30,34,36,40,42,46,48,52,54,58,60,64,66,70,72,76-77,81-82,84 / fields ORDER_COMPLETED.jobId,JOB_DISPATCHED,JOB_STEP_COMPLETED | Pacing step (method: completion chain): CUT. Tracing the last unit back, every step that started later than its unit was ready waited for CUT on Cutter, which worked back to back from tick 0 to 36 (11 such waits). |

### G4

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of CUT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 9, 8 units (units 5-12) are waiting to start CUT on Cutter. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 9, Cutter (CUT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 9, Twin Assembler (ASSEMBLE) has 2 of 2 slots in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 9, Inspector 1 (INSPECT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 9, Inspector 2 (INSPECT) has 0 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Inspector 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 9, Inspector 2 is idle, and no unit is waiting for a step it can serve (INSPECT). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 9, 0 of 12 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 45. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 45, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,72,76-77,81-82,84 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 45 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 4 on Twin Assembler; waited 0 for INSPECT, INSPECT 5 on Inspector 2. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-17,19,22,24,28,30,34,36,40,42,46,48,52,54,58,60,64,66,70,72,76 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,18,20,23,25,29,31,35,37,41,43,47,49,53,55,59,61,65,67,71,73,77-78,81 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Twin Assembler processed 12 ASSEMBLE steps; in use during ticks 3-40; at most 2 of 2 slots in use at once. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,27,32,39,44,51,56,63,68,75,79,83 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 1 processed 6 INSPECT steps; in use during ticks 7-12, 13-18, 19-24, 25-30, 31-36, 37-42. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 26,33,38,45,50,57,62,69,74,80,82,84 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 2 processed 6 INSPECT steps; in use during ticks 10-15, 16-21, 22-27, 28-33, 34-39, 40-45. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,85 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 198 ticks over 12 units; ASSEMBLE 0 ticks over 12 units; INSPECT 0 ticks over 12 units. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION | completion-chain-from-supported-events | events 1-2,14,16-17,19,22,24,28,30,34,36,40,42,46,48,52,54,58,60,64,66,70,72,76-77,81-82,84 / fields ORDER_COMPLETED.jobId,JOB_DISPATCHED,JOB_STEP_COMPLETED | Pacing step (method: completion chain): CUT. Tracing the last unit back, every step that started later than its unit was ready waited for CUT on Cutter, which worked back to back from tick 0 to 36 (11 such waits). |

### G5

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 67. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 67, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,74,77-78,83,94-95 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 67 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 4 on Assembler 2; waited 22 for INSPECT, INSPECT 5 on Shared. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-17,19,22,24,29,31,34,36,41,43,46,48,53,55,60,62,65,67,72,74,77 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,23,32,35,44,47,56,61,68,73,81 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 1 processed 6 ASSEMBLE steps; in use during ticks 3-7, 9-13, 15-19, 21-25, 27-31, 33-37. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 18,25,30,37,42,51,54,63,66,75,78,83 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 2 processed 6 ASSEMBLE steps; in use during ticks 6-10, 12-16, 18-22, 24-28, 30-34, 36-40. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 1,96 / fields JOB_DISPATCHED | Assembler 3 processed nothing in this run. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 1,96 / fields JOB_DISPATCHED | Assembler 4 processed nothing in this run. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 1,96 / fields JOB_DISPATCHED | Assembler 5 processed nothing in this run. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 1,96 / fields JOB_DISPATCHED | Assembler 6 processed nothing in this run. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,27-28,39-40,49-50,58-59,70-71,79-80,85-95 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Shared processed 12 INSPECT steps; in use during ticks 7-67. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,96 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 198 ticks over 12 units; ASSEMBLE 0 ticks over 12 units; INSPECT 132 ticks over 12 units. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION | completion-chain-from-supported-events | events 1-2,14-15,20-21,25,27-28,32,37,39-40,44,49-51,56,58-59,63,68,70-71,75,79-81,83,85-95 / fields ORDER_COMPLETED.jobId,JOB_DISPATCHED,JOB_STEP_COMPLETED | Pacing step (method: completion chain): INSPECT. Tracing the last unit back, every step that started later than its unit was ready waited for INSPECT on Shared, which worked back to back from tick 7 to 67 (11 such waits). |

### G6

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of CUT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 17, 6 units (units 7-12) are waiting to start CUT on Cutter. |
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of INSPECT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 17, 1 unit (unit 4) is waiting to start INSPECT; each goes to whichever of Inspector or Shared can take it first and is not assigned to either yet. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 17, Cutter (CUT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 17, Assembler (ASSEMBLE) has 0 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 17, Inspector (INSPECT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 17, Shared (ASSEMBLE+INSPECT) has 1 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Assembler is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 17, Assembler is idle, and no unit is waiting for a step it can serve (ASSEMBLE). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 17, 2 of 12 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 47. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 47, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,79,82-83,88,91,93 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 47 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 4 on Assembler; waited 2 for INSPECT, INSPECT 5 on Shared. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-17,19,22,24,28-29,35,37,42,44,47-48,56,58,61,63,68-69,77,79,82 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,23,31,33,38,43,50-51,59,62,71-72,80,83,88 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 8 ASSEMBLE steps; in use during ticks 3-7, 9-17, 18-26, 27-35, 36-40. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,27,32,40-41,53-54,64-65,74-75,86-87,92 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector processed 7 INSPECT steps; in use during ticks 7-12, 13-43. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 18,25-26,34,36,45-46,55,57,66-67,76,78,84-85,90-91,93 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Shared processed 4 ASSEMBLE and 5 INSPECT steps; in use during ticks 6-47. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,94 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 198 ticks over 12 units; ASSEMBLE 3 ticks over 12 units; INSPECT 11 ticks over 12 units. |
| Q2_LIMITING_STEP | REFUSAL | completion-chain-from-supported-events | events 1-2,14,16-17,19,22,24,28-29,35,37,42,44,47-48,56,58,61,63,68-69,77-78,84-85,88,90-91,93 / fields ORDER_COMPLETED.jobId,JOB_DISPATCHED,JOB_STEP_COMPLETED | Pacing step (method: completion chain): not unique -- at tick 33, unit 11 became ready for ASSEMBLE at the same tick Shared was released by unit 8 (INSPECT), so both held it up. |

### G7

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 68. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 68, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,68,70,91,93-95 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 68 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 22 for ASSEMBLE, ASSEMBLE 5 on Assembler; waited 0 for INSPECT, INSPECT 5 on Inspector. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-18,23-24,26-27,33-34,40-41,43-44,50-51,53-54,60-61,67-68,70 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,22,30,32,37,39,47,49,57,59,64,66,73,75,77,79,81,83,85,87,89,91,93 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 12 ASSEMBLE steps; in use during ticks 3-63. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,29,31,36,38,46,48,56,58,63,65,72,74,76,78,80,82,84,86,88,90,92,94-95 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector processed 12 INSPECT steps; in use during ticks 8-68. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,96 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 198 ticks over 12 units; ASSEMBLE 132 ticks over 12 units; INSPECT 0 ticks over 12 units. |
| Q2_LIMITING_STEP | REFUSAL | completion-chain-from-supported-events | events 1-2,14-15,17,20,22-23,26,30,32-33,37,39-40,43,47,49-50,53,57,59-60,64,66-67,70,73,75,77,79,81,83,85,87,89,91,93-95 / fields ORDER_COMPLETED.jobId,JOB_DISPATCHED,JOB_STEP_COMPLETED | Pacing step (method: completion chain): not unique -- at tick 63, unit 12 became ready for INSPECT at the same tick Inspector was released by unit 11 (INSPECT), so both held it up. |

### G8

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of CUT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 10, 8 units (units 5-12) are waiting to start CUT on Cutter. |
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of INSPECT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 10, 1 unit (unit 2) is waiting to start INSPECT on Inspector. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 10, Cutter (CUT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 10, Assembler (ASSEMBLE) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 10, Inspector (INSPECT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 10, Assembler 2 (ASSEMBLE) has 0 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Assembler 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 10, Assembler 2 is idle, and no unit is waiting for a step it can serve (ASSEMBLE). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 10, 0 of 12 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 67. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 67, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,74,77-78,83,94-95 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 67 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 4 on Assembler 2; waited 22 for INSPECT, INSPECT 5 on Inspector. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-17,19,22,24,29,31,34,36,41,43,46,48,53,55,60,62,65,67,72,74,77 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,23,32,35,44,47,56,61,68,73,81 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 6 ASSEMBLE steps; in use during ticks 3-7, 9-13, 15-19, 21-25, 27-31, 33-37. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,27-28,39-40,49-50,58-59,70-71,79-80,85-95 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector processed 12 INSPECT steps; in use during ticks 7-67. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 18,25,30,37,42,51,54,63,66,75,78,83 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 2 processed 6 ASSEMBLE steps; in use during ticks 6-10, 12-16, 18-22, 24-28, 30-34, 36-40. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,96 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 198 ticks over 12 units; ASSEMBLE 0 ticks over 12 units; INSPECT 132 ticks over 12 units. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION | completion-chain-from-supported-events | events 1-2,14-15,20-21,25,27-28,32,37,39-40,44,49-51,56,58-59,63,68,70-71,75,79-81,83,85-95 / fields ORDER_COMPLETED.jobId,JOB_DISPATCHED,JOB_STEP_COMPLETED | Pacing step (method: completion chain): INSPECT. Tracing the last unit back, every step that started later than its unit was ready waited for INSPECT on Inspector, which worked back to back from tick 7 to 67 (11 such waits). |

### G9a

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 11. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 11, all 2 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1-2,4-5,9,14-15 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 1) took 11 ticks from order acceptance: waited 0 for CUT, CUT 2 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 3 on Assembler; waited 4 for INSPECT, INSPECT 2 on Shared. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,4,6-7 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 2 CUT steps; in use during ticks 0-4. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 5,9 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 1 ASSEMBLE step; in use during ticks 2-5. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 8,11-15 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Shared processed 1 ASSEMBLE and 2 INSPECT steps; in use during ticks 4-11. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,16 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 2 ticks over 2 units; ASSEMBLE 0 ticks over 2 units; INSPECT 4 ticks over 2 units. |
| Q2_LIMITING_STEP | REFUSAL | completion-chain-from-supported-events | events 1-2,4,6-9,11-15 / fields ORDER_COMPLETED.jobId,JOB_DISPATCHED,JOB_STEP_COMPLETED | Pacing step (method: completion chain): not unique -- steps on the chain waited for capacity at INSPECT and CUT. |

### G9b

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 9. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 9, all 2 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,6-8,11,14-15 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 2) took 9 ticks from order acceptance: waited 2 for CUT, CUT 2 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 3 on Assembler; waited 0 for INSPECT, INSPECT 2 on Shared. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 5,9-10,13-15 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Shared processed 1 ASSEMBLE and 2 INSPECT steps; in use during ticks 2-9. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 8,11 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 1 ASSEMBLE step; in use during ticks 4-7. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,4,6-7 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 2 CUT steps; in use during ticks 0-4. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,16 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 2 ticks over 2 units; ASSEMBLE 0 ticks over 2 units; INSPECT 0 ticks over 2 units. |
| Q2_LIMITING_STEP | REFUSAL | completion-chain-from-supported-events | events 1-2,4,6-8,11,14-15 / fields ORDER_COMPLETED.jobId,JOB_DISPATCHED,JOB_STEP_COMPLETED | Pacing step (method: completion chain): not unique -- at tick 7, unit 2 became ready for INSPECT at the same tick Shared was released by unit 1 (INSPECT), so both held it up. |

### G10

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of CUT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 16, 6 units (units 7-12) are waiting to start CUT on Cutter. |
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of ASSEMBLE / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 16, 1 unit (unit 5) is waiting to start ASSEMBLE on Assembler. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Cutter (CUT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Assembler (ASSEMBLE) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Inspector 1 (INSPECT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Inspector 2 (INSPECT) has 0 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Inspector 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 16, Inspector 2 is idle, and no unit is waiting for a step it can serve (INSPECT). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 16, 2 of 12 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 56. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 56, all 12 units are complete and every resource is idle. |
| Q4_DELAY | REFUSAL |  | obs closing | The last unit's waiting and processing times are not available: supported events 1..96 are not all retained; missing [1-39]. |
| ACTIVITY | REFUSAL |  | obs closing | Resource activity over time is not available: supported events 1..96 are not all retained; missing [1-39]. |
| Q4_DELAY | REFUSAL |  | obs closing | Waiting summed over all units is not available: supported events 1..96 are not all retained; missing [1-39]. |
| Q2_LIMITING_STEP | REFUSAL |  | obs closing | The completion chain is not available: supported events 1..96 are not all retained; missing [1-39]. |

### G11a

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of ASSEMBLE / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 5, 3 units (units 3-5) are waiting to start ASSEMBLE; each goes to whichever of Assembler 1 or Assembler 2 can take it first and is not assigned to either yet. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 5, Cutter (CUT) has 0 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 5, Assembler 1 (ASSEMBLE) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 5, Assembler 2 (ASSEMBLE) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 5, Inspector (INSPECT) has 0 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Cutter is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 5, Cutter is idle, and no unit is waiting for a step it can serve (CUT). |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Inspector is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 5, Inspector is idle, and no unit is waiting for a step it can serve (INSPECT). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 5, 0 of 5 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 26. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 26, all 5 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,17,19,32,38-40 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 5) took 26 ticks from order acceptance: waited 4 for CUT, CUT 1 on Cutter; waited 12 for ASSEMBLE, ASSEMBLE 8 on Assembler 1; waited 0 for INSPECT, INSPECT 1 on Inspector. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,7,9-10,12-14,16-17,19 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 5 CUT steps; in use during ticks 0-5. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 8,21,23,30,32,38 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 1 processed 3 ASSEMBLE steps; in use during ticks 1-25. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 11,24-25,33 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 2 processed 2 ASSEMBLE steps; in use during ticks 2-18. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 22,27-29,31,35-37,39-40 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector processed 5 INSPECT steps; in use during ticks 9-11, 17-19, 25-26. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,41 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 10 ticks over 5 units; ASSEMBLE 24 ticks over 5 units; INSPECT 0 ticks over 5 units. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION | completion-chain-from-supported-events | events 1-2,7-8,13,19,21,23,30,32,38-40 / fields ORDER_COMPLETED.jobId,JOB_DISPATCHED,JOB_STEP_COMPLETED | Pacing step (method: completion chain): ASSEMBLE. Tracing the last unit back, every step that started later than its unit was ready waited for ASSEMBLE on Assembler 1, which worked back to back from tick 1 to 25 (2 such waits). |

### G11b

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of ASSEMBLE / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 3, 1 unit (unit 3) is waiting to start ASSEMBLE on Assembler. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 3, Cutter (CUT) has 0 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 3, Assembler (ASSEMBLE) has 2 of 2 slots in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 3, Inspector (INSPECT) has 0 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Cutter is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 3, Cutter is idle, and no unit is waiting for a step it can serve (CUT). |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Inspector is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 3, Inspector is idle, and no unit is waiting for a step it can serve (INSPECT). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 3, 0 of 3 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 26. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 26, all 3 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,10-11,15,21-23 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 3) took 26 ticks from order acceptance: waited 2 for CUT, CUT 1 on Cutter; waited 10 for ASSEMBLE, ASSEMBLE 12 on Assembler; waited 0 for INSPECT, INSPECT 1 on Inspector. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,5,7-8,10-11 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 3 CUT steps; in use during ticks 0-3. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 6,9,13,15-16,21 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 3 ASSEMBLE steps; in use during ticks 1-25; at most 2 of 2 slots in use at once. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 14,18-20,22-23 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector processed 3 INSPECT steps; in use during ticks 13-15, 25-26. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,24 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 3 ticks over 3 units; ASSEMBLE 10 ticks over 3 units; INSPECT 0 ticks over 3 units. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION | completion-chain-from-supported-events | events 1-2,5-6,11,13,15,21-23 / fields ORDER_COMPLETED.jobId,JOB_DISPATCHED,JOB_STEP_COMPLETED | Pacing step (method: completion chain): ASSEMBLE. Tracing the last unit back, every step that started later than its unit was ready waited for ASSEMBLE on Assembler, which worked back to back from tick 1 to 25 (1 such waits). |

### G1+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). Completion: tick 67 -> tick 67 (no change). |

### G1+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 2 (ASSEMBLE). Completion: tick 67 -> tick 67 (no change). |

### G1+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 2 (INSPECT). Completion: tick 67 -> tick 56 (11 ticks earlier). |

### G2+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). Completion: tick 56 -> tick 56 (no change). |

### G2+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 2 (ASSEMBLE). Completion: tick 56 -> tick 45 (11 ticks earlier). |

### G2+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 3 (INSPECT). Completion: tick 56 -> tick 56 (no change). |

### G3+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). Completion: tick 45 -> tick 37 (8 ticks earlier). |

### G3+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 3 (ASSEMBLE). Completion: tick 45 -> tick 45 (no change). |

### G3+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 3 (INSPECT). Completion: tick 45 -> tick 45 (no change). |

### G4+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). Completion: tick 45 -> tick 37 (8 ticks earlier). |

### G4+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 2 (ASSEMBLE). Completion: tick 45 -> tick 45 (no change). |

### G4+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 3 (INSPECT). Completion: tick 45 -> tick 45 (no change). |

### G5+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). Completion: tick 67 -> tick 67 (no change). |

### G5+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 7 (ASSEMBLE). Completion: tick 67 -> tick 67 (no change). |

### G5+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 2 (INSPECT). Completion: tick 67 -> tick 45 (22 ticks earlier). |

### G7+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). Completion: tick 68 -> tick 68 (no change). |

### G7+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 2 (ASSEMBLE). Completion: tick 68 -> tick 68 (no change). |

### G7+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 2 (INSPECT). Completion: tick 68 -> tick 68 (no change). |

### G2-remove-Inspector-2

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: removed Inspector 2 (INSPECT). Completion: tick 56 -> tick 67 (11 ticks later). Without Inspector 2 the order completed 11 ticks later. |

### G8-remove-Assembler-2

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: removed Assembler 2 (ASSEMBLE). Completion: tick 67 -> tick 67 (no change). Without Assembler 2 the order still completed at tick 67. |

### G9-reorder

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: resource order [Cutter, Assembler, Shared] -> [Shared, Assembler, Cutter]. Completion: tick 11 -> tick 9 (2 ticks earlier). |

### G1+assemble+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q6_CONFOUNDED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | 2 changes: added Assembler 2 (ASSEMBLE); added Inspector 2 (INSPECT). Completion: tick 67 -> tick 45 (22 ticks earlier). |

### G1+cut+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q6_CONFOUNDED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | 2 changes: added Cutter 2 (CUT); added Inspector 2 (INSPECT). Completion: tick 67 -> tick 56 (11 ticks earlier). |

### G7+assemble+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q6_CONFOUNDED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | 2 changes: added Assembler 2 (ASSEMBLE); added Inspector 2 (INSPECT). Completion: tick 68 -> tick 46 (22 ticks earlier). |

## C3a-bundle-with-completion-chain

Facts, run-total waits and the completion-chain pacing step as claims with evidence; refusal for surplus, counterfactual, ties and multi-change attribution.

### G1

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of ASSEMBLE / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 33, 3 units (units 9-11) are waiting to start ASSEMBLE on Assembler. |
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of INSPECT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 33, 1 unit (unit 7) is waiting to start INSPECT on Inspector. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 33, Cutter (CUT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 33, Assembler (ASSEMBLE) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 33, Inspector (INSPECT) has 1 of 1 slot in use. |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 33, 5 of 12 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 67. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 67, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,76,81,96,98,105-106 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 67 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 11 for ASSEMBLE, ASSEMBLE 4 on Assembler; waited 11 for INSPECT, INSPECT 5 on Inspector. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-18,23-24,31-32,37-38,42-43,48-49,56-57,64-65,67-68,75-76,81 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,22,26-27,34-35,45-46,53-54,61-62,70-71,78-79,85-86,90-91,95-96,98 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 12 ASSEMBLE steps; in use during ticks 3-51. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,29-30,40-41,51-52,59-60,73-74,83-84,88-89,93-94,100-106 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector processed 12 INSPECT steps; in use during ticks 7-67. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,107 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 198 ticks over 12 units; ASSEMBLE 66 ticks over 12 units; INSPECT 66 ticks over 12 units. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION | completion-chain-from-supported-events | events 1-2,14-15,20-21,26,29-30,34,40-41,45,51-53,59-61,70,73-74,78,83-85,88-90,93-95,98,100-106 / fields ORDER_COMPLETED.jobId,JOB_DISPATCHED,JOB_STEP_COMPLETED | Pacing step (method: completion chain): INSPECT. Tracing the last unit back, every step that started later than its unit was ready waited for INSPECT on Inspector, which worked back to back from tick 7 to 67 (11 such waits). |
| Q2_LIMITING_STEP | REFUSAL |  |  | Whether adding INSPECT capacity would finish sooner is not decidable from this run; a retry that changes only that can show it. |

### G2

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of CUT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 16, 6 units (units 7-12) are waiting to start CUT on Cutter. |
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of ASSEMBLE / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 16, 1 unit (unit 5) is waiting to start ASSEMBLE on Assembler. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Cutter (CUT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Assembler (ASSEMBLE) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Inspector 1 (INSPECT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Inspector 2 (INSPECT) has 0 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Inspector 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 16, Inspector 2 is idle, and no unit is waiting for a step it can serve (INSPECT). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 16, 2 of 12 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 56. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 56, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,72,78,90,92-93,95 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 56 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 11 for ASSEMBLE, ASSEMBLE 4 on Assembler; waited 0 for INSPECT, INSPECT 5 on Inspector 2. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-18,23-24,30-31,36-37,40-41,47-48,54-55,60-61,64-65,71-72,78 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,22,26,28,33,35,43,45,50,52,57,59,67,69,74,76,80,82,84,86,88,90,92 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 12 ASSEMBLE steps; in use during ticks 3-51. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,29,34,46,51,63,68,77,81,87,89,94 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 1 processed 6 INSPECT steps; in use during ticks 7-12, 15-20, 23-28, 31-36, 39-44, 47-52. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 27,39,44,53,58,70,75,83,85,91,93,95 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 2 processed 6 INSPECT steps; in use during ticks 11-16, 19-24, 27-32, 35-40, 43-48, 51-56. |
| Q3_IDLE_RESOURCE | REFUSAL |  | obs mid-run / design steps Inspector 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | Whether Inspector 2 is surplus -- whether the order would finish as early without it -- is not decidable from this run; a retry without it can show that. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,96 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 198 ticks over 12 units; ASSEMBLE 66 ticks over 12 units; INSPECT 0 ticks over 12 units. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION | completion-chain-from-supported-events | events 1-2,14-15,17,20,22-23,26,28,30,33,35-36,40,43,45,47,50,52,54,57,59-60,64,67,69,71,74,76,78,80,82,84,86,88,90,92-93,95 / fields ORDER_COMPLETED.jobId,JOB_DISPATCHED,JOB_STEP_COMPLETED | Pacing step (method: completion chain): ASSEMBLE. Tracing the last unit back, every step that started later than its unit was ready waited for ASSEMBLE on Assembler, which worked back to back from tick 3 to 51 (11 such waits). |
| Q2_LIMITING_STEP | REFUSAL |  |  | Whether adding ASSEMBLE capacity would finish sooner is not decidable from this run; a retry that changes only that can show it. |

### G3

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 45. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 45, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,72,76-77,81-82,84 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 45 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 4 on Assembler 2; waited 0 for INSPECT, INSPECT 5 on Inspector 2. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-17,19,22,24,28,30,34,36,40,42,46,48,52,54,58,60,64,66,70,72,76 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,23,31,35,43,47,55,59,67,71,78 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 6 ASSEMBLE steps; in use during ticks 3-7, 9-13, 15-19, 21-25, 27-31, 33-37. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,27,32,39,44,51,56,63,68,75,79,83 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 1 processed 6 INSPECT steps; in use during ticks 7-12, 13-18, 19-24, 25-30, 31-36, 37-42. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 26,33,38,45,50,57,62,69,74,80,82,84 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 2 processed 6 INSPECT steps; in use during ticks 10-15, 16-21, 22-27, 28-33, 34-39, 40-45. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 18,25,29,37,41,49,53,61,65,73,77,81 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 2 processed 6 ASSEMBLE steps; in use during ticks 6-10, 12-16, 18-22, 24-28, 30-34, 36-40. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,85 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 198 ticks over 12 units; ASSEMBLE 0 ticks over 12 units; INSPECT 0 ticks over 12 units. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION | completion-chain-from-supported-events | events 1-2,14,16-17,19,22,24,28,30,34,36,40,42,46,48,52,54,58,60,64,66,70,72,76-77,81-82,84 / fields ORDER_COMPLETED.jobId,JOB_DISPATCHED,JOB_STEP_COMPLETED | Pacing step (method: completion chain): CUT. Tracing the last unit back, every step that started later than its unit was ready waited for CUT on Cutter, which worked back to back from tick 0 to 36 (11 such waits). |
| Q2_LIMITING_STEP | REFUSAL |  |  | Whether adding CUT capacity would finish sooner is not decidable from this run; a retry that changes only that can show it. |

### G4

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of CUT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 9, 8 units (units 5-12) are waiting to start CUT on Cutter. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 9, Cutter (CUT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 9, Twin Assembler (ASSEMBLE) has 2 of 2 slots in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 9, Inspector 1 (INSPECT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 9, Inspector 2 (INSPECT) has 0 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Inspector 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 9, Inspector 2 is idle, and no unit is waiting for a step it can serve (INSPECT). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 9, 0 of 12 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 45. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 45, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,72,76-77,81-82,84 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 45 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 4 on Twin Assembler; waited 0 for INSPECT, INSPECT 5 on Inspector 2. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-17,19,22,24,28,30,34,36,40,42,46,48,52,54,58,60,64,66,70,72,76 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,18,20,23,25,29,31,35,37,41,43,47,49,53,55,59,61,65,67,71,73,77-78,81 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Twin Assembler processed 12 ASSEMBLE steps; in use during ticks 3-40; at most 2 of 2 slots in use at once. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,27,32,39,44,51,56,63,68,75,79,83 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 1 processed 6 INSPECT steps; in use during ticks 7-12, 13-18, 19-24, 25-30, 31-36, 37-42. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 26,33,38,45,50,57,62,69,74,80,82,84 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 2 processed 6 INSPECT steps; in use during ticks 10-15, 16-21, 22-27, 28-33, 34-39, 40-45. |
| Q3_IDLE_RESOURCE | REFUSAL |  | obs mid-run / design steps Inspector 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | Whether Inspector 2 is surplus -- whether the order would finish as early without it -- is not decidable from this run; a retry without it can show that. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,85 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 198 ticks over 12 units; ASSEMBLE 0 ticks over 12 units; INSPECT 0 ticks over 12 units. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION | completion-chain-from-supported-events | events 1-2,14,16-17,19,22,24,28,30,34,36,40,42,46,48,52,54,58,60,64,66,70,72,76-77,81-82,84 / fields ORDER_COMPLETED.jobId,JOB_DISPATCHED,JOB_STEP_COMPLETED | Pacing step (method: completion chain): CUT. Tracing the last unit back, every step that started later than its unit was ready waited for CUT on Cutter, which worked back to back from tick 0 to 36 (11 such waits). |
| Q2_LIMITING_STEP | REFUSAL |  |  | Whether adding CUT capacity would finish sooner is not decidable from this run; a retry that changes only that can show it. |

### G5

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 67. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 67, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,74,77-78,83,94-95 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 67 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 4 on Assembler 2; waited 22 for INSPECT, INSPECT 5 on Shared. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-17,19,22,24,29,31,34,36,41,43,46,48,53,55,60,62,65,67,72,74,77 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,23,32,35,44,47,56,61,68,73,81 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 1 processed 6 ASSEMBLE steps; in use during ticks 3-7, 9-13, 15-19, 21-25, 27-31, 33-37. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 18,25,30,37,42,51,54,63,66,75,78,83 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 2 processed 6 ASSEMBLE steps; in use during ticks 6-10, 12-16, 18-22, 24-28, 30-34, 36-40. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 1,96 / fields JOB_DISPATCHED | Assembler 3 processed nothing in this run. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 1,96 / fields JOB_DISPATCHED | Assembler 4 processed nothing in this run. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 1,96 / fields JOB_DISPATCHED | Assembler 5 processed nothing in this run. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 1,96 / fields JOB_DISPATCHED | Assembler 6 processed nothing in this run. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,27-28,39-40,49-50,58-59,70-71,79-80,85-95 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Shared processed 12 INSPECT steps; in use during ticks 7-67. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,96 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 198 ticks over 12 units; ASSEMBLE 0 ticks over 12 units; INSPECT 132 ticks over 12 units. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION | completion-chain-from-supported-events | events 1-2,14-15,20-21,25,27-28,32,37,39-40,44,49-51,56,58-59,63,68,70-71,75,79-81,83,85-95 / fields ORDER_COMPLETED.jobId,JOB_DISPATCHED,JOB_STEP_COMPLETED | Pacing step (method: completion chain): INSPECT. Tracing the last unit back, every step that started later than its unit was ready waited for INSPECT on Shared, which worked back to back from tick 7 to 67 (11 such waits). |
| Q2_LIMITING_STEP | REFUSAL |  |  | Whether adding INSPECT capacity would finish sooner is not decidable from this run; a retry that changes only that can show it. |

### G6

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of CUT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 17, 6 units (units 7-12) are waiting to start CUT on Cutter. |
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of INSPECT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 17, 1 unit (unit 4) is waiting to start INSPECT; each goes to whichever of Inspector or Shared can take it first and is not assigned to either yet. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 17, Cutter (CUT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 17, Assembler (ASSEMBLE) has 0 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 17, Inspector (INSPECT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 17, Shared (ASSEMBLE+INSPECT) has 1 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Assembler is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 17, Assembler is idle, and no unit is waiting for a step it can serve (ASSEMBLE). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 17, 2 of 12 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 47. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 47, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,79,82-83,88,91,93 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 47 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 4 on Assembler; waited 2 for INSPECT, INSPECT 5 on Shared. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-17,19,22,24,28-29,35,37,42,44,47-48,56,58,61,63,68-69,77,79,82 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,23,31,33,38,43,50-51,59,62,71-72,80,83,88 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 8 ASSEMBLE steps; in use during ticks 3-7, 9-17, 18-26, 27-35, 36-40. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,27,32,40-41,53-54,64-65,74-75,86-87,92 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector processed 7 INSPECT steps; in use during ticks 7-12, 13-43. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 18,25-26,34,36,45-46,55,57,66-67,76,78,84-85,90-91,93 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Shared processed 4 ASSEMBLE and 5 INSPECT steps; in use during ticks 6-47. |
| Q3_IDLE_RESOURCE | REFUSAL |  | obs mid-run / design steps Assembler is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | Whether Assembler is surplus -- whether the order would finish as early without it -- is not decidable from this run; a retry without it can show that. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,94 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 198 ticks over 12 units; ASSEMBLE 3 ticks over 12 units; INSPECT 11 ticks over 12 units. |
| Q2_LIMITING_STEP | REFUSAL | completion-chain-from-supported-events | events 1-2,14,16-17,19,22,24,28-29,35,37,42,44,47-48,56,58,61,63,68-69,77-78,84-85,88,90-91,93 / fields ORDER_COMPLETED.jobId,JOB_DISPATCHED,JOB_STEP_COMPLETED | Pacing step (method: completion chain): not unique -- at tick 33, unit 11 became ready for ASSEMBLE at the same tick Shared was released by unit 8 (INSPECT), so both held it up. |

### G7

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 68. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 68, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,68,70,91,93-95 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 68 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 22 for ASSEMBLE, ASSEMBLE 5 on Assembler; waited 0 for INSPECT, INSPECT 5 on Inspector. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-18,23-24,26-27,33-34,40-41,43-44,50-51,53-54,60-61,67-68,70 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,22,30,32,37,39,47,49,57,59,64,66,73,75,77,79,81,83,85,87,89,91,93 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 12 ASSEMBLE steps; in use during ticks 3-63. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,29,31,36,38,46,48,56,58,63,65,72,74,76,78,80,82,84,86,88,90,92,94-95 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector processed 12 INSPECT steps; in use during ticks 8-68. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,96 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 198 ticks over 12 units; ASSEMBLE 132 ticks over 12 units; INSPECT 0 ticks over 12 units. |
| Q2_LIMITING_STEP | REFUSAL | completion-chain-from-supported-events | events 1-2,14-15,17,20,22-23,26,30,32-33,37,39-40,43,47,49-50,53,57,59-60,64,66-67,70,73,75,77,79,81,83,85,87,89,91,93-95 / fields ORDER_COMPLETED.jobId,JOB_DISPATCHED,JOB_STEP_COMPLETED | Pacing step (method: completion chain): not unique -- at tick 63, unit 12 became ready for INSPECT at the same tick Inspector was released by unit 11 (INSPECT), so both held it up. |

### G8

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of CUT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 10, 8 units (units 5-12) are waiting to start CUT on Cutter. |
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of INSPECT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 10, 1 unit (unit 2) is waiting to start INSPECT on Inspector. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 10, Cutter (CUT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 10, Assembler (ASSEMBLE) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 10, Inspector (INSPECT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 10, Assembler 2 (ASSEMBLE) has 0 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Assembler 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 10, Assembler 2 is idle, and no unit is waiting for a step it can serve (ASSEMBLE). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 10, 0 of 12 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 67. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 67, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,74,77-78,83,94-95 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 67 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 4 on Assembler 2; waited 22 for INSPECT, INSPECT 5 on Inspector. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-17,19,22,24,29,31,34,36,41,43,46,48,53,55,60,62,65,67,72,74,77 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,23,32,35,44,47,56,61,68,73,81 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 6 ASSEMBLE steps; in use during ticks 3-7, 9-13, 15-19, 21-25, 27-31, 33-37. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,27-28,39-40,49-50,58-59,70-71,79-80,85-95 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector processed 12 INSPECT steps; in use during ticks 7-67. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 18,25,30,37,42,51,54,63,66,75,78,83 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 2 processed 6 ASSEMBLE steps; in use during ticks 6-10, 12-16, 18-22, 24-28, 30-34, 36-40. |
| Q3_IDLE_RESOURCE | REFUSAL |  | obs mid-run / design steps Assembler 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | Whether Assembler 2 is surplus -- whether the order would finish as early without it -- is not decidable from this run; a retry without it can show that. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,96 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 198 ticks over 12 units; ASSEMBLE 0 ticks over 12 units; INSPECT 132 ticks over 12 units. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION | completion-chain-from-supported-events | events 1-2,14-15,20-21,25,27-28,32,37,39-40,44,49-51,56,58-59,63,68,70-71,75,79-81,83,85-95 / fields ORDER_COMPLETED.jobId,JOB_DISPATCHED,JOB_STEP_COMPLETED | Pacing step (method: completion chain): INSPECT. Tracing the last unit back, every step that started later than its unit was ready waited for INSPECT on Inspector, which worked back to back from tick 7 to 67 (11 such waits). |
| Q2_LIMITING_STEP | REFUSAL |  |  | Whether adding INSPECT capacity would finish sooner is not decidable from this run; a retry that changes only that can show it. |

### G9a

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 11. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 11, all 2 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1-2,4-5,9,14-15 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 1) took 11 ticks from order acceptance: waited 0 for CUT, CUT 2 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 3 on Assembler; waited 4 for INSPECT, INSPECT 2 on Shared. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,4,6-7 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 2 CUT steps; in use during ticks 0-4. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 5,9 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 1 ASSEMBLE step; in use during ticks 2-5. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 8,11-15 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Shared processed 1 ASSEMBLE and 2 INSPECT steps; in use during ticks 4-11. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,16 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 2 ticks over 2 units; ASSEMBLE 0 ticks over 2 units; INSPECT 4 ticks over 2 units. |
| Q2_LIMITING_STEP | REFUSAL | completion-chain-from-supported-events | events 1-2,4,6-9,11-15 / fields ORDER_COMPLETED.jobId,JOB_DISPATCHED,JOB_STEP_COMPLETED | Pacing step (method: completion chain): not unique -- steps on the chain waited for capacity at INSPECT and CUT. |

### G9b

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 9. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 9, all 2 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,6-8,11,14-15 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 2) took 9 ticks from order acceptance: waited 2 for CUT, CUT 2 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 3 on Assembler; waited 0 for INSPECT, INSPECT 2 on Shared. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 5,9-10,13-15 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Shared processed 1 ASSEMBLE and 2 INSPECT steps; in use during ticks 2-9. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 8,11 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 1 ASSEMBLE step; in use during ticks 4-7. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,4,6-7 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 2 CUT steps; in use during ticks 0-4. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,16 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 2 ticks over 2 units; ASSEMBLE 0 ticks over 2 units; INSPECT 0 ticks over 2 units. |
| Q2_LIMITING_STEP | REFUSAL | completion-chain-from-supported-events | events 1-2,4,6-8,11,14-15 / fields ORDER_COMPLETED.jobId,JOB_DISPATCHED,JOB_STEP_COMPLETED | Pacing step (method: completion chain): not unique -- at tick 7, unit 2 became ready for INSPECT at the same tick Shared was released by unit 1 (INSPECT), so both held it up. |

### G10

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of CUT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 16, 6 units (units 7-12) are waiting to start CUT on Cutter. |
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of ASSEMBLE / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 16, 1 unit (unit 5) is waiting to start ASSEMBLE on Assembler. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Cutter (CUT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Assembler (ASSEMBLE) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Inspector 1 (INSPECT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Inspector 2 (INSPECT) has 0 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Inspector 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 16, Inspector 2 is idle, and no unit is waiting for a step it can serve (INSPECT). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 16, 2 of 12 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 56. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 56, all 12 units are complete and every resource is idle. |
| Q4_DELAY | REFUSAL |  | obs closing | The last unit's waiting and processing times are not available: supported events 1..96 are not all retained; missing [1-39]. |
| ACTIVITY | REFUSAL |  | obs closing | Resource activity over time is not available: supported events 1..96 are not all retained; missing [1-39]. |
| Q3_IDLE_RESOURCE | REFUSAL |  | obs mid-run / design steps Inspector 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | Whether Inspector 2 is surplus -- whether the order would finish as early without it -- is not decidable from this run; a retry without it can show that. |
| Q4_DELAY | REFUSAL |  | obs closing | Waiting summed over all units is not available: supported events 1..96 are not all retained; missing [1-39]. |
| Q2_LIMITING_STEP | REFUSAL |  | obs closing | The completion chain is not available: supported events 1..96 are not all retained; missing [1-39]. |

### G11a

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of ASSEMBLE / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 5, 3 units (units 3-5) are waiting to start ASSEMBLE; each goes to whichever of Assembler 1 or Assembler 2 can take it first and is not assigned to either yet. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 5, Cutter (CUT) has 0 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 5, Assembler 1 (ASSEMBLE) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 5, Assembler 2 (ASSEMBLE) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 5, Inspector (INSPECT) has 0 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Cutter is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 5, Cutter is idle, and no unit is waiting for a step it can serve (CUT). |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Inspector is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 5, Inspector is idle, and no unit is waiting for a step it can serve (INSPECT). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 5, 0 of 5 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 26. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 26, all 5 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,17,19,32,38-40 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 5) took 26 ticks from order acceptance: waited 4 for CUT, CUT 1 on Cutter; waited 12 for ASSEMBLE, ASSEMBLE 8 on Assembler 1; waited 0 for INSPECT, INSPECT 1 on Inspector. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,7,9-10,12-14,16-17,19 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 5 CUT steps; in use during ticks 0-5. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 8,21,23,30,32,38 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 1 processed 3 ASSEMBLE steps; in use during ticks 1-25. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 11,24-25,33 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 2 processed 2 ASSEMBLE steps; in use during ticks 2-18. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 22,27-29,31,35-37,39-40 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector processed 5 INSPECT steps; in use during ticks 9-11, 17-19, 25-26. |
| Q3_IDLE_RESOURCE | REFUSAL |  | obs mid-run / design steps Cutter is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | Whether Cutter is surplus -- whether the order would finish as early without it -- is not decidable from this run; a retry without it can show that. |
| Q3_IDLE_RESOURCE | REFUSAL |  | obs mid-run / design steps Inspector is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | Whether Inspector is surplus -- whether the order would finish as early without it -- is not decidable from this run; a retry without it can show that. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,41 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 10 ticks over 5 units; ASSEMBLE 24 ticks over 5 units; INSPECT 0 ticks over 5 units. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION | completion-chain-from-supported-events | events 1-2,7-8,13,19,21,23,30,32,38-40 / fields ORDER_COMPLETED.jobId,JOB_DISPATCHED,JOB_STEP_COMPLETED | Pacing step (method: completion chain): ASSEMBLE. Tracing the last unit back, every step that started later than its unit was ready waited for ASSEMBLE on Assembler 1, which worked back to back from tick 1 to 25 (2 such waits). |
| Q2_LIMITING_STEP | REFUSAL |  |  | Whether adding ASSEMBLE capacity would finish sooner is not decidable from this run; a retry that changes only that can show it. |

### G11b

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of ASSEMBLE / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 3, 1 unit (unit 3) is waiting to start ASSEMBLE on Assembler. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 3, Cutter (CUT) has 0 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 3, Assembler (ASSEMBLE) has 2 of 2 slots in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 3, Inspector (INSPECT) has 0 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Cutter is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 3, Cutter is idle, and no unit is waiting for a step it can serve (CUT). |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Inspector is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 3, Inspector is idle, and no unit is waiting for a step it can serve (INSPECT). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 3, 0 of 3 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 26. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 26, all 3 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,10-11,15,21-23 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 3) took 26 ticks from order acceptance: waited 2 for CUT, CUT 1 on Cutter; waited 10 for ASSEMBLE, ASSEMBLE 12 on Assembler; waited 0 for INSPECT, INSPECT 1 on Inspector. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,5,7-8,10-11 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 3 CUT steps; in use during ticks 0-3. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 6,9,13,15-16,21 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 3 ASSEMBLE steps; in use during ticks 1-25; at most 2 of 2 slots in use at once. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 14,18-20,22-23 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector processed 3 INSPECT steps; in use during ticks 13-15, 25-26. |
| Q3_IDLE_RESOURCE | REFUSAL |  | obs mid-run / design steps Cutter is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | Whether Cutter is surplus -- whether the order would finish as early without it -- is not decidable from this run; a retry without it can show that. |
| Q3_IDLE_RESOURCE | REFUSAL |  | obs mid-run / design steps Inspector is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | Whether Inspector is surplus -- whether the order would finish as early without it -- is not decidable from this run; a retry without it can show that. |
| Q4_DELAY | AGGREGATE_MEASUREMENT | dispatch-profile-by-resource-and-step | obs closing / events 1,24 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED | Waiting before each step, summed over every unit of the completed order: CUT 3 ticks over 3 units; ASSEMBLE 10 ticks over 3 units; INSPECT 0 ticks over 3 units. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION | completion-chain-from-supported-events | events 1-2,5-6,11,13,15,21-23 / fields ORDER_COMPLETED.jobId,JOB_DISPATCHED,JOB_STEP_COMPLETED | Pacing step (method: completion chain): ASSEMBLE. Tracing the last unit back, every step that started later than its unit was ready waited for ASSEMBLE on Assembler, which worked back to back from tick 1 to 25 (1 such waits). |
| Q2_LIMITING_STEP | REFUSAL |  |  | Whether adding ASSEMBLE capacity would finish sooner is not decidable from this run; a retry that changes only that can show it. |

### G1+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). Completion: tick 67 -> tick 67 (no change). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G1+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 2 (ASSEMBLE). Completion: tick 67 -> tick 67 (no change). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G1+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 2 (INSPECT). Completion: tick 67 -> tick 56 (11 ticks earlier). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G2+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). Completion: tick 56 -> tick 56 (no change). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G2+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 2 (ASSEMBLE). Completion: tick 56 -> tick 45 (11 ticks earlier). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G2+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 3 (INSPECT). Completion: tick 56 -> tick 56 (no change). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G3+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). Completion: tick 45 -> tick 37 (8 ticks earlier). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G3+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 3 (ASSEMBLE). Completion: tick 45 -> tick 45 (no change). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G3+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 3 (INSPECT). Completion: tick 45 -> tick 45 (no change). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G4+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). Completion: tick 45 -> tick 37 (8 ticks earlier). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G4+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 2 (ASSEMBLE). Completion: tick 45 -> tick 45 (no change). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G4+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 3 (INSPECT). Completion: tick 45 -> tick 45 (no change). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G5+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). Completion: tick 67 -> tick 67 (no change). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G5+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 7 (ASSEMBLE). Completion: tick 67 -> tick 67 (no change). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G5+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 2 (INSPECT). Completion: tick 67 -> tick 45 (22 ticks earlier). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G7+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). Completion: tick 68 -> tick 68 (no change). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G7+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 2 (ASSEMBLE). Completion: tick 68 -> tick 68 (no change). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G7+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 2 (INSPECT). Completion: tick 68 -> tick 68 (no change). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G2-remove-Inspector-2

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: removed Inspector 2 (INSPECT). Completion: tick 56 -> tick 67 (11 ticks later). Without Inspector 2 the order completed 11 ticks later. |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G8-remove-Assembler-2

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: removed Assembler 2 (ASSEMBLE). Completion: tick 67 -> tick 67 (no change). Without Assembler 2 the order still completed at tick 67. |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G9-reorder

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: resource order [Cutter, Assembler, Shared] -> [Shared, Assembler, Cutter]. Completion: tick 11 -> tick 9 (2 ticks earlier). |
| Q5_CONTROLLED_COMPARISON | DIRECT_FACT |  | design Engine semantics section 2 rule 4: remaining ties broken by MachineId | When eligible resources are otherwise equally placed to take work, it goes to the one with the lower resource number (in these designs, the one listed earlier), so resource order alone can change the outcome. |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G1+assemble+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q6_CONFOUNDED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | 2 changes: added Assembler 2 (ASSEMBLE); added Inspector 2 (INSPECT). Completion: tick 67 -> tick 45 (22 ticks earlier). |
| Q6_CONFOUNDED_COMPARISON | REFUSAL |  |  | How much of the difference (22 ticks earlier) each of the 2 changes accounts for is not attributable from this pair; compare one change at a time. |

### G1+cut+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q6_CONFOUNDED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | 2 changes: added Cutter 2 (CUT); added Inspector 2 (INSPECT). Completion: tick 67 -> tick 56 (11 ticks earlier). |
| Q6_CONFOUNDED_COMPARISON | REFUSAL |  |  | How much of the difference (11 ticks earlier) each of the 2 changes accounts for is not attributable from this pair; compare one change at a time. |

### G7+assemble+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q6_CONFOUNDED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | 2 changes: added Assembler 2 (ASSEMBLE); added Inspector 2 (INSPECT). Completion: tick 68 -> tick 46 (22 ticks earlier). |
| Q6_CONFOUNDED_COMPARISON | REFUSAL |  |  | How much of the difference (22 ticks earlier) each of the 2 changes accounts for is not attributable from this pair; compare one change at a time. |

## C3b-bundle-minimal

Facts and the last unit's times as claims with evidence; the single-run limiting step, surplus and multi-change attribution are refused; one-change comparisons speak only to change and outcome.

### G1

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of ASSEMBLE / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 33, 3 units (units 9-11) are waiting to start ASSEMBLE on Assembler. |
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of INSPECT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 33, 1 unit (unit 7) is waiting to start INSPECT on Inspector. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 33, Cutter (CUT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 33, Assembler (ASSEMBLE) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 33, Inspector (INSPECT) has 1 of 1 slot in use. |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 33, 5 of 12 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 67. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 67, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,76,81,96,98,105-106 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 67 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 11 for ASSEMBLE, ASSEMBLE 4 on Assembler; waited 11 for INSPECT, INSPECT 5 on Inspector. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-18,23-24,31-32,37-38,42-43,48-49,56-57,64-65,67-68,75-76,81 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,22,26-27,34-35,45-46,53-54,61-62,70-71,78-79,85-86,90-91,95-96,98 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 12 ASSEMBLE steps; in use during ticks 3-51. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,29-30,40-41,51-52,59-60,73-74,83-84,88-89,93-94,100-106 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector processed 12 INSPECT steps; in use during ticks 7-67. |
| Q2_LIMITING_STEP | REFUSAL |  |  | Which step limits this design is not decidable from one run. The last unit's times and each resource's activity above are measurements, not that answer; a retry that changes one resource shows whether that change alters completion. |

### G2

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of CUT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 16, 6 units (units 7-12) are waiting to start CUT on Cutter. |
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of ASSEMBLE / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 16, 1 unit (unit 5) is waiting to start ASSEMBLE on Assembler. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Cutter (CUT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Assembler (ASSEMBLE) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Inspector 1 (INSPECT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Inspector 2 (INSPECT) has 0 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Inspector 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 16, Inspector 2 is idle, and no unit is waiting for a step it can serve (INSPECT). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 16, 2 of 12 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 56. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 56, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,72,78,90,92-93,95 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 56 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 11 for ASSEMBLE, ASSEMBLE 4 on Assembler; waited 0 for INSPECT, INSPECT 5 on Inspector 2. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-18,23-24,30-31,36-37,40-41,47-48,54-55,60-61,64-65,71-72,78 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,22,26,28,33,35,43,45,50,52,57,59,67,69,74,76,80,82,84,86,88,90,92 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 12 ASSEMBLE steps; in use during ticks 3-51. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,29,34,46,51,63,68,77,81,87,89,94 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 1 processed 6 INSPECT steps; in use during ticks 7-12, 15-20, 23-28, 31-36, 39-44, 47-52. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 27,39,44,53,58,70,75,83,85,91,93,95 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 2 processed 6 INSPECT steps; in use during ticks 11-16, 19-24, 27-32, 35-40, 43-48, 51-56. |
| Q3_IDLE_RESOURCE | REFUSAL |  | obs mid-run / design steps Inspector 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | Whether Inspector 2 is surplus -- whether the order would finish as early without it -- is not decidable from this run; a retry without it can show that. |
| Q2_LIMITING_STEP | REFUSAL |  |  | Which step limits this design is not decidable from one run. The last unit's times and each resource's activity above are measurements, not that answer; a retry that changes one resource shows whether that change alters completion. |

### G3

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 45. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 45, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,72,76-77,81-82,84 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 45 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 4 on Assembler 2; waited 0 for INSPECT, INSPECT 5 on Inspector 2. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-17,19,22,24,28,30,34,36,40,42,46,48,52,54,58,60,64,66,70,72,76 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,23,31,35,43,47,55,59,67,71,78 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 6 ASSEMBLE steps; in use during ticks 3-7, 9-13, 15-19, 21-25, 27-31, 33-37. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,27,32,39,44,51,56,63,68,75,79,83 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 1 processed 6 INSPECT steps; in use during ticks 7-12, 13-18, 19-24, 25-30, 31-36, 37-42. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 26,33,38,45,50,57,62,69,74,80,82,84 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 2 processed 6 INSPECT steps; in use during ticks 10-15, 16-21, 22-27, 28-33, 34-39, 40-45. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 18,25,29,37,41,49,53,61,65,73,77,81 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 2 processed 6 ASSEMBLE steps; in use during ticks 6-10, 12-16, 18-22, 24-28, 30-34, 36-40. |
| Q2_LIMITING_STEP | REFUSAL |  |  | Which step limits this design is not decidable from one run. The last unit's times and each resource's activity above are measurements, not that answer; a retry that changes one resource shows whether that change alters completion. |

### G4

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of CUT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 9, 8 units (units 5-12) are waiting to start CUT on Cutter. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 9, Cutter (CUT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 9, Twin Assembler (ASSEMBLE) has 2 of 2 slots in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 9, Inspector 1 (INSPECT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 9, Inspector 2 (INSPECT) has 0 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Inspector 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 9, Inspector 2 is idle, and no unit is waiting for a step it can serve (INSPECT). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 9, 0 of 12 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 45. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 45, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,72,76-77,81-82,84 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 45 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 4 on Twin Assembler; waited 0 for INSPECT, INSPECT 5 on Inspector 2. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-17,19,22,24,28,30,34,36,40,42,46,48,52,54,58,60,64,66,70,72,76 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,18,20,23,25,29,31,35,37,41,43,47,49,53,55,59,61,65,67,71,73,77-78,81 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Twin Assembler processed 12 ASSEMBLE steps; in use during ticks 3-40; at most 2 of 2 slots in use at once. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,27,32,39,44,51,56,63,68,75,79,83 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 1 processed 6 INSPECT steps; in use during ticks 7-12, 13-18, 19-24, 25-30, 31-36, 37-42. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 26,33,38,45,50,57,62,69,74,80,82,84 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 2 processed 6 INSPECT steps; in use during ticks 10-15, 16-21, 22-27, 28-33, 34-39, 40-45. |
| Q3_IDLE_RESOURCE | REFUSAL |  | obs mid-run / design steps Inspector 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | Whether Inspector 2 is surplus -- whether the order would finish as early without it -- is not decidable from this run; a retry without it can show that. |
| Q2_LIMITING_STEP | REFUSAL |  |  | Which step limits this design is not decidable from one run. The last unit's times and each resource's activity above are measurements, not that answer; a retry that changes one resource shows whether that change alters completion. |

### G5

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 67. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 67, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,74,77-78,83,94-95 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 67 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 4 on Assembler 2; waited 22 for INSPECT, INSPECT 5 on Shared. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-17,19,22,24,29,31,34,36,41,43,46,48,53,55,60,62,65,67,72,74,77 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,23,32,35,44,47,56,61,68,73,81 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 1 processed 6 ASSEMBLE steps; in use during ticks 3-7, 9-13, 15-19, 21-25, 27-31, 33-37. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 18,25,30,37,42,51,54,63,66,75,78,83 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 2 processed 6 ASSEMBLE steps; in use during ticks 6-10, 12-16, 18-22, 24-28, 30-34, 36-40. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 1,96 / fields JOB_DISPATCHED | Assembler 3 processed nothing in this run. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 1,96 / fields JOB_DISPATCHED | Assembler 4 processed nothing in this run. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 1,96 / fields JOB_DISPATCHED | Assembler 5 processed nothing in this run. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 1,96 / fields JOB_DISPATCHED | Assembler 6 processed nothing in this run. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,27-28,39-40,49-50,58-59,70-71,79-80,85-95 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Shared processed 12 INSPECT steps; in use during ticks 7-67. |
| Q2_LIMITING_STEP | REFUSAL |  |  | Which step limits this design is not decidable from one run. The last unit's times and each resource's activity above are measurements, not that answer; a retry that changes one resource shows whether that change alters completion. |

### G6

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of CUT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 17, 6 units (units 7-12) are waiting to start CUT on Cutter. |
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of INSPECT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 17, 1 unit (unit 4) is waiting to start INSPECT; each goes to whichever of Inspector or Shared can take it first and is not assigned to either yet. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 17, Cutter (CUT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 17, Assembler (ASSEMBLE) has 0 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 17, Inspector (INSPECT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 17, Shared (ASSEMBLE+INSPECT) has 1 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Assembler is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 17, Assembler is idle, and no unit is waiting for a step it can serve (ASSEMBLE). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 17, 2 of 12 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 47. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 47, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,79,82-83,88,91,93 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 47 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 4 on Assembler; waited 2 for INSPECT, INSPECT 5 on Shared. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-17,19,22,24,28-29,35,37,42,44,47-48,56,58,61,63,68-69,77,79,82 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,23,31,33,38,43,50-51,59,62,71-72,80,83,88 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 8 ASSEMBLE steps; in use during ticks 3-7, 9-17, 18-26, 27-35, 36-40. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,27,32,40-41,53-54,64-65,74-75,86-87,92 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector processed 7 INSPECT steps; in use during ticks 7-12, 13-43. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 18,25-26,34,36,45-46,55,57,66-67,76,78,84-85,90-91,93 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Shared processed 4 ASSEMBLE and 5 INSPECT steps; in use during ticks 6-47. |
| Q3_IDLE_RESOURCE | REFUSAL |  | obs mid-run / design steps Assembler is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | Whether Assembler is surplus -- whether the order would finish as early without it -- is not decidable from this run; a retry without it can show that. |
| Q2_LIMITING_STEP | REFUSAL |  |  | Which step limits this design is not decidable from one run. The last unit's times and each resource's activity above are measurements, not that answer; a retry that changes one resource shows whether that change alters completion. |

### G7

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 68. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 68, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,68,70,91,93-95 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 68 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 22 for ASSEMBLE, ASSEMBLE 5 on Assembler; waited 0 for INSPECT, INSPECT 5 on Inspector. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-18,23-24,26-27,33-34,40-41,43-44,50-51,53-54,60-61,67-68,70 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,22,30,32,37,39,47,49,57,59,64,66,73,75,77,79,81,83,85,87,89,91,93 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 12 ASSEMBLE steps; in use during ticks 3-63. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,29,31,36,38,46,48,56,58,63,65,72,74,76,78,80,82,84,86,88,90,92,94-95 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector processed 12 INSPECT steps; in use during ticks 8-68. |
| Q2_LIMITING_STEP | REFUSAL |  |  | Which step limits this design is not decidable from one run. The last unit's times and each resource's activity above are measurements, not that answer; a retry that changes one resource shows whether that change alters completion. |

### G8

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of CUT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 10, 8 units (units 5-12) are waiting to start CUT on Cutter. |
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of INSPECT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 10, 1 unit (unit 2) is waiting to start INSPECT on Inspector. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 10, Cutter (CUT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 10, Assembler (ASSEMBLE) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 10, Inspector (INSPECT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 10, Assembler 2 (ASSEMBLE) has 0 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Assembler 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 10, Assembler 2 is idle, and no unit is waiting for a step it can serve (ASSEMBLE). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 10, 0 of 12 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 67. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 67, all 12 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,74,77-78,83,94-95 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 67 ticks from order acceptance: waited 33 for CUT, CUT 3 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 4 on Assembler 2; waited 22 for INSPECT, INSPECT 5 on Inspector. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-17,19,22,24,29,31,34,36,41,43,46,48,53,55,60,62,65,67,72,74,77 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 12 CUT steps; in use during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,23,32,35,44,47,56,61,68,73,81 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 6 ASSEMBLE steps; in use during ticks 3-7, 9-13, 15-19, 21-25, 27-31, 33-37. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,27-28,39-40,49-50,58-59,70-71,79-80,85-95 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector processed 12 INSPECT steps; in use during ticks 7-67. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 18,25,30,37,42,51,54,63,66,75,78,83 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 2 processed 6 ASSEMBLE steps; in use during ticks 6-10, 12-16, 18-22, 24-28, 30-34, 36-40. |
| Q3_IDLE_RESOURCE | REFUSAL |  | obs mid-run / design steps Assembler 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | Whether Assembler 2 is surplus -- whether the order would finish as early without it -- is not decidable from this run; a retry without it can show that. |
| Q2_LIMITING_STEP | REFUSAL |  |  | Which step limits this design is not decidable from one run. The last unit's times and each resource's activity above are measurements, not that answer; a retry that changes one resource shows whether that change alters completion. |

### G9a

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 11. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 11, all 2 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1-2,4-5,9,14-15 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 1) took 11 ticks from order acceptance: waited 0 for CUT, CUT 2 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 3 on Assembler; waited 4 for INSPECT, INSPECT 2 on Shared. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,4,6-7 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 2 CUT steps; in use during ticks 0-4. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 5,9 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 1 ASSEMBLE step; in use during ticks 2-5. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 8,11-15 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Shared processed 1 ASSEMBLE and 2 INSPECT steps; in use during ticks 4-11. |
| Q2_LIMITING_STEP | REFUSAL |  |  | Which step limits this design is not decidable from one run. The last unit's times and each resource's activity above are measurements, not that answer; a retry that changes one resource shows whether that change alters completion. |

### G9b

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 9. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 9, all 2 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,6-8,11,14-15 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 2) took 9 ticks from order acceptance: waited 2 for CUT, CUT 2 on Cutter; waited 0 for ASSEMBLE, ASSEMBLE 3 on Assembler; waited 0 for INSPECT, INSPECT 2 on Shared. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 5,9-10,13-15 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Shared processed 1 ASSEMBLE and 2 INSPECT steps; in use during ticks 2-9. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 8,11 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 1 ASSEMBLE step; in use during ticks 4-7. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,4,6-7 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 2 CUT steps; in use during ticks 0-4. |
| Q2_LIMITING_STEP | REFUSAL |  |  | Which step limits this design is not decidable from one run. The last unit's times and each resource's activity above are measurements, not that answer; a retry that changes one resource shows whether that change alters completion. |

### G10

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of CUT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 16, 6 units (units 7-12) are waiting to start CUT on Cutter. |
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of ASSEMBLE / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 16, 1 unit (unit 5) is waiting to start ASSEMBLE on Assembler. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Cutter (CUT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Assembler (ASSEMBLE) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Inspector 1 (INSPECT) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Inspector 2 (INSPECT) has 0 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Inspector 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 16, Inspector 2 is idle, and no unit is waiting for a step it can serve (INSPECT). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 16, 2 of 12 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 56. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 56, all 12 units are complete and every resource is idle. |
| Q4_DELAY | REFUSAL |  | obs closing | The last unit's waiting and processing times are not available: supported events 1..96 are not all retained; missing [1-39]. |
| ACTIVITY | REFUSAL |  | obs closing | Resource activity over time is not available: supported events 1..96 are not all retained; missing [1-39]. |
| Q3_IDLE_RESOURCE | REFUSAL |  | obs mid-run / design steps Inspector 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | Whether Inspector 2 is surplus -- whether the order would finish as early without it -- is not decidable from this run; a retry without it can show that. |
| Q2_LIMITING_STEP | REFUSAL |  |  | Which step limits this design is not decidable from one run. The last unit's times and each resource's activity above are measurements, not that answer; a retry that changes one resource shows whether that change alters completion. |

### G11a

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of ASSEMBLE / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 5, 3 units (units 3-5) are waiting to start ASSEMBLE; each goes to whichever of Assembler 1 or Assembler 2 can take it first and is not assigned to either yet. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 5, Cutter (CUT) has 0 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 5, Assembler 1 (ASSEMBLE) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 5, Assembler 2 (ASSEMBLE) has 1 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 5, Inspector (INSPECT) has 0 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Cutter is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 5, Cutter is idle, and no unit is waiting for a step it can serve (CUT). |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Inspector is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 5, Inspector is idle, and no unit is waiting for a step it can serve (INSPECT). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 5, 0 of 5 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 26. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 26, all 5 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,17,19,32,38-40 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 5) took 26 ticks from order acceptance: waited 4 for CUT, CUT 1 on Cutter; waited 12 for ASSEMBLE, ASSEMBLE 8 on Assembler 1; waited 0 for INSPECT, INSPECT 1 on Inspector. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,7,9-10,12-14,16-17,19 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 5 CUT steps; in use during ticks 0-5. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 8,21,23,30,32,38 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 1 processed 3 ASSEMBLE steps; in use during ticks 1-25. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 11,24-25,33 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 2 processed 2 ASSEMBLE steps; in use during ticks 2-18. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 22,27-29,31,35-37,39-40 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector processed 5 INSPECT steps; in use during ticks 9-11, 17-19, 25-26. |
| Q3_IDLE_RESOURCE | REFUSAL |  | obs mid-run / design steps Cutter is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | Whether Cutter is surplus -- whether the order would finish as early without it -- is not decidable from this run; a retry without it can show that. |
| Q3_IDLE_RESOURCE | REFUSAL |  | obs mid-run / design steps Inspector is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | Whether Inspector is surplus -- whether the order would finish as early without it -- is not decidable from this run; a retry without it can show that. |
| Q2_LIMITING_STEP | REFUSAL |  |  | Which step limits this design is not decidable from one run. The last unit's times and each resource's activity above are measurements, not that answer; a retry that changes one resource shows whether that change alters completion. |

### G11b

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of ASSEMBLE / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 3, 1 unit (unit 3) is waiting to start ASSEMBLE on Assembler. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 3, Cutter (CUT) has 0 of 1 slot in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 3, Assembler (ASSEMBLE) has 2 of 2 slots in use. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 3, Inspector (INSPECT) has 0 of 1 slot in use. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Cutter is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 3, Cutter is idle, and no unit is waiting for a step it can serve (CUT). |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Inspector is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 3, Inspector is idle, and no unit is waiting for a step it can serve (INSPECT). |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 3, 0 of 3 units are complete. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 26. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs closing / fields OrderObservation.complete,ResourceObservation.activeJobIds | At tick 26, all 3 units are complete and every resource is idle. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,10-11,15,21-23 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 3) took 26 ticks from order acceptance: waited 2 for CUT, CUT 1 on Cutter; waited 10 for ASSEMBLE, ASSEMBLE 12 on Assembler; waited 0 for INSPECT, INSPECT 1 on Inspector. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,5,7-8,10-11 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter processed 3 CUT steps; in use during ticks 0-3. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 6,9,13,15-16,21 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler processed 3 ASSEMBLE steps; in use during ticks 1-25; at most 2 of 2 slots in use at once. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 14,18-20,22-23 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector processed 3 INSPECT steps; in use during ticks 13-15, 25-26. |
| Q3_IDLE_RESOURCE | REFUSAL |  | obs mid-run / design steps Cutter is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | Whether Cutter is surplus -- whether the order would finish as early without it -- is not decidable from this run; a retry without it can show that. |
| Q3_IDLE_RESOURCE | REFUSAL |  | obs mid-run / design steps Inspector is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | Whether Inspector is surplus -- whether the order would finish as early without it -- is not decidable from this run; a retry without it can show that. |
| Q2_LIMITING_STEP | REFUSAL |  |  | Which step limits this design is not decidable from one run. The last unit's times and each resource's activity above are measurements, not that answer; a retry that changes one resource shows whether that change alters completion. |

### G1+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). Completion: tick 67 -> tick 67 (no change). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G1+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 2 (ASSEMBLE). Completion: tick 67 -> tick 67 (no change). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G1+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 2 (INSPECT). Completion: tick 67 -> tick 56 (11 ticks earlier). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G2+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). Completion: tick 56 -> tick 56 (no change). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G2+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 2 (ASSEMBLE). Completion: tick 56 -> tick 45 (11 ticks earlier). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G2+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 3 (INSPECT). Completion: tick 56 -> tick 56 (no change). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G3+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). Completion: tick 45 -> tick 37 (8 ticks earlier). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G3+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 3 (ASSEMBLE). Completion: tick 45 -> tick 45 (no change). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G3+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 3 (INSPECT). Completion: tick 45 -> tick 45 (no change). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G4+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). Completion: tick 45 -> tick 37 (8 ticks earlier). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G4+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 2 (ASSEMBLE). Completion: tick 45 -> tick 45 (no change). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G4+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 3 (INSPECT). Completion: tick 45 -> tick 45 (no change). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G5+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). Completion: tick 67 -> tick 67 (no change). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G5+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 7 (ASSEMBLE). Completion: tick 67 -> tick 67 (no change). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G5+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 2 (INSPECT). Completion: tick 67 -> tick 45 (22 ticks earlier). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G7+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). Completion: tick 68 -> tick 68 (no change). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G7+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 2 (ASSEMBLE). Completion: tick 68 -> tick 68 (no change). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G7+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 2 (INSPECT). Completion: tick 68 -> tick 68 (no change). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G2-remove-Inspector-2

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: removed Inspector 2 (INSPECT). Completion: tick 56 -> tick 67 (11 ticks later). Without Inspector 2 the order completed 11 ticks later. |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G8-remove-Assembler-2

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: removed Assembler 2 (ASSEMBLE). Completion: tick 67 -> tick 67 (no change). Without Assembler 2 the order still completed at tick 67. |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G9-reorder

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: resource order [Cutter, Assembler, Shared] -> [Shared, Assembler, Cutter]. Completion: tick 11 -> tick 9 (2 ticks earlier). |
| Q5_CONTROLLED_COMPARISON | DIRECT_FACT |  | design Engine semantics section 2 rule 4: remaining ties broken by MachineId | When eligible resources are otherwise equally placed to take work, it goes to the one with the lower resource number (in these designs, the one listed earlier), so resource order alone can change the outcome. |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This pair shows the change and the outcome together; it does not show why. |

### G1+assemble+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q6_CONFOUNDED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | 2 changes: added Assembler 2 (ASSEMBLE); added Inspector 2 (INSPECT). Completion: tick 67 -> tick 45 (22 ticks earlier). |
| Q6_CONFOUNDED_COMPARISON | REFUSAL |  |  | How much of the difference (22 ticks earlier) each of the 2 changes accounts for is not attributable from this pair; compare one change at a time. |

### G1+cut+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q6_CONFOUNDED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | 2 changes: added Cutter 2 (CUT); added Inspector 2 (INSPECT). Completion: tick 67 -> tick 56 (11 ticks earlier). |
| Q6_CONFOUNDED_COMPARISON | REFUSAL |  |  | How much of the difference (11 ticks earlier) each of the 2 changes accounts for is not attributable from this pair; compare one change at a time. |

### G7+assemble+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q6_CONFOUNDED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | 2 changes: added Assembler 2 (ASSEMBLE); added Inspector 2 (INSPECT). Completion: tick 68 -> tick 46 (22 ticks earlier). |
| Q6_CONFOUNDED_COMPARISON | REFUSAL |  |  | How much of the difference (22 ticks earlier) each of the 2 changes accounts for is not attributable from this pair; compare one change at a time. |

## C3c-plain-bundle

C3b's evidence with revised wording: 'needed' instead of 'surplus', the only machine for a step stated as a design fact, a caveat on the longest wait, plain refusals grouped as 'what this run cannot tell you', and work timelines on request.

### G1

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of ASSEMBLE / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 33, 3 units (units 9-11) are waiting to start ASSEMBLE on Assembler. |
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of INSPECT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 33, 1 unit (unit 7) is waiting to start INSPECT on Inspector. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 33, Cutter (CUT) is busy. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 33, Assembler (ASSEMBLE) is busy. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 33, Inspector (INSPECT) is busy. |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 33, 5 of 12 units are finished. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order finished at tick 67. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,76,81,96,98,105-106 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 67 ticks from the order's acceptance at tick 0: waited 33 to start CUT, then CUT took 3 on Cutter; waited 11 to start ASSEMBLE, then ASSEMBLE took 4 on Assembler; waited 11 to start INSPECT, then INSPECT took 5 on Inspector. Its longest wait shows where it spent time; on its own it does not show which machine to add. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-18,23-24,31-32,37-38,42-43,48-49,56-57,64-65,67-68,75-76,81 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter worked on 12 CUT steps during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,22,26-27,34-35,45-46,53-54,61-62,70-71,78-79,85-86,90-91,95-96,98 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler worked on 12 ASSEMBLE steps during ticks 3-51. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,29-30,40-41,51-52,59-60,73-74,83-84,88-89,93-94,100-106 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector worked on 12 INSPECT steps during ticks 7-67. |
| Q2_LIMITING_STEP | REFUSAL |  |  | Which machine to add: this run does not tell you. Waits and work times describe what happened; only a try that adds one machine shows whether the order then finishes sooner. |

### G2

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of CUT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 16, 6 units (units 7-12) are waiting to start CUT on Cutter. |
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of ASSEMBLE / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 16, 1 unit (unit 5) is waiting to start ASSEMBLE on Assembler. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Cutter (CUT) is busy. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Assembler (ASSEMBLE) is busy. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Inspector 1 (INSPECT) is busy. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Inspector 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 16, Inspector 2 is idle and no unit is waiting for INSPECT. |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 16, 2 of 12 units are finished. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order finished at tick 56. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,72,78,90,92-93,95 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 56 ticks from the order's acceptance at tick 0: waited 33 to start CUT, then CUT took 3 on Cutter; waited 11 to start ASSEMBLE, then ASSEMBLE took 4 on Assembler; waited 0 to start INSPECT, then INSPECT took 5 on Inspector 2. Its longest wait shows where it spent time; on its own it does not show which machine to add. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-18,23-24,30-31,36-37,40-41,47-48,54-55,60-61,64-65,71-72,78 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter worked on 12 CUT steps during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,22,26,28,33,35,43,45,50,52,57,59,67,69,74,76,80,82,84,86,88,90,92 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler worked on 12 ASSEMBLE steps during ticks 3-51. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,29,34,46,51,63,68,77,81,87,89,94 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 1 worked on 6 INSPECT steps during ticks 7-12, 15-20, 23-28, 31-36, 39-44, 47-52. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 27,39,44,53,58,70,75,83,85,91,93,95 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 2 worked on 6 INSPECT steps during ticks 11-16, 19-24, 27-32, 35-40, 43-48, 51-56. |
| Q3_IDLE_RESOURCE | REFUSAL |  | obs mid-run / design steps Inspector 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | Whether Inspector 2 is needed (whether the order would finish later without it): this run does not tell you. Try removing it to find out. |
| Q2_LIMITING_STEP | REFUSAL |  |  | Which machine to add: this run does not tell you. Waits and work times describe what happened; only a try that adds one machine shows whether the order then finishes sooner. |

### G3

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order finished at tick 45. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,72,76-77,81-82,84 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 45 ticks from the order's acceptance at tick 0: waited 33 to start CUT, then CUT took 3 on Cutter; waited 0 to start ASSEMBLE, then ASSEMBLE took 4 on Assembler 2; waited 0 to start INSPECT, then INSPECT took 5 on Inspector 2. Its longest wait shows where it spent time; on its own it does not show which machine to add. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-17,19,22,24,28,30,34,36,40,42,46,48,52,54,58,60,64,66,70,72,76 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter worked on 12 CUT steps during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,23,31,35,43,47,55,59,67,71,78 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler worked on 6 ASSEMBLE steps during ticks 3-7, 9-13, 15-19, 21-25, 27-31, 33-37. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,27,32,39,44,51,56,63,68,75,79,83 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 1 worked on 6 INSPECT steps during ticks 7-12, 13-18, 19-24, 25-30, 31-36, 37-42. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 26,33,38,45,50,57,62,69,74,80,82,84 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 2 worked on 6 INSPECT steps during ticks 10-15, 16-21, 22-27, 28-33, 34-39, 40-45. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 18,25,29,37,41,49,53,61,65,73,77,81 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 2 worked on 6 ASSEMBLE steps during ticks 6-10, 12-16, 18-22, 24-28, 30-34, 36-40. |
| Q2_LIMITING_STEP | REFUSAL |  |  | Which machine to add: this run does not tell you. Waits and work times describe what happened; only a try that adds one machine shows whether the order then finishes sooner. |

### G4

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of CUT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 9, 8 units (units 5-12) are waiting to start CUT on Cutter. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 9, Cutter (CUT) is busy. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 9, Twin Assembler (ASSEMBLE) has 2 of 2 slots busy. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 9, Inspector 1 (INSPECT) is busy. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Inspector 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 9, Inspector 2 is idle and no unit is waiting for INSPECT. |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 9, 0 of 12 units are finished. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order finished at tick 45. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,72,76-77,81-82,84 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 45 ticks from the order's acceptance at tick 0: waited 33 to start CUT, then CUT took 3 on Cutter; waited 0 to start ASSEMBLE, then ASSEMBLE took 4 on Twin Assembler; waited 0 to start INSPECT, then INSPECT took 5 on Inspector 2. Its longest wait shows where it spent time; on its own it does not show which machine to add. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-17,19,22,24,28,30,34,36,40,42,46,48,52,54,58,60,64,66,70,72,76 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter worked on 12 CUT steps during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,18,20,23,25,29,31,35,37,41,43,47,49,53,55,59,61,65,67,71,73,77-78,81 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Twin Assembler worked on 12 ASSEMBLE steps during ticks 3-40; at most 2 of 2 slots at once. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,27,32,39,44,51,56,63,68,75,79,83 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 1 worked on 6 INSPECT steps during ticks 7-12, 13-18, 19-24, 25-30, 31-36, 37-42. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 26,33,38,45,50,57,62,69,74,80,82,84 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector 2 worked on 6 INSPECT steps during ticks 10-15, 16-21, 22-27, 28-33, 34-39, 40-45. |
| Q3_IDLE_RESOURCE | REFUSAL |  | obs mid-run / design steps Inspector 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | Whether Inspector 2 is needed (whether the order would finish later without it): this run does not tell you. Try removing it to find out. |
| Q2_LIMITING_STEP | REFUSAL |  |  | Which machine to add: this run does not tell you. Waits and work times describe what happened; only a try that adds one machine shows whether the order then finishes sooner. |

### G5

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order finished at tick 67. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,74,77-78,83,94-95 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 67 ticks from the order's acceptance at tick 0: waited 33 to start CUT, then CUT took 3 on Cutter; waited 0 to start ASSEMBLE, then ASSEMBLE took 4 on Assembler 2; waited 22 to start INSPECT, then INSPECT took 5 on Shared. Its longest wait shows where it spent time; on its own it does not show which machine to add. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-17,19,22,24,29,31,34,36,41,43,46,48,53,55,60,62,65,67,72,74,77 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter worked on 12 CUT steps during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,23,32,35,44,47,56,61,68,73,81 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 1 worked on 6 ASSEMBLE steps during ticks 3-7, 9-13, 15-19, 21-25, 27-31, 33-37. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 18,25,30,37,42,51,54,63,66,75,78,83 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 2 worked on 6 ASSEMBLE steps during ticks 6-10, 12-16, 18-22, 24-28, 30-34, 36-40. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 1,96 / fields JOB_DISPATCHED | Assembler 3 did no work in this run. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 1,96 / fields JOB_DISPATCHED | Assembler 4 did no work in this run. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 1,96 / fields JOB_DISPATCHED | Assembler 5 did no work in this run. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 1,96 / fields JOB_DISPATCHED | Assembler 6 did no work in this run. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,27-28,39-40,49-50,58-59,70-71,79-80,85-95 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Shared worked on 12 INSPECT steps during ticks 7-67. |
| Q2_LIMITING_STEP | REFUSAL |  |  | Which machine to add: this run does not tell you. Waits and work times describe what happened; only a try that adds one machine shows whether the order then finishes sooner. |

### G6

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of CUT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 17, 6 units (units 7-12) are waiting to start CUT on Cutter. |
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of INSPECT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 17, 1 unit (unit 4) is waiting to start INSPECT. They are not assigned to a machine yet; either Inspector or Shared may take them when it becomes free. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 17, Cutter (CUT) is busy. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 17, Inspector (INSPECT) is busy. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 17, Shared (ASSEMBLE+INSPECT) is busy. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Assembler is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 17, Assembler is idle and no unit is waiting for ASSEMBLE. |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 17, 2 of 12 units are finished. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order finished at tick 47. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,79,82-83,88,91,93 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 47 ticks from the order's acceptance at tick 0: waited 33 to start CUT, then CUT took 3 on Cutter; waited 0 to start ASSEMBLE, then ASSEMBLE took 4 on Assembler; waited 2 to start INSPECT, then INSPECT took 5 on Shared. Its longest wait shows where it spent time; on its own it does not show which machine to add. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-17,19,22,24,28-29,35,37,42,44,47-48,56,58,61,63,68-69,77,79,82 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter worked on 12 CUT steps during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,23,31,33,38,43,50-51,59,62,71-72,80,83,88 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler worked on 8 ASSEMBLE steps during ticks 3-7, 9-17, 18-26, 27-35, 36-40. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,27,32,40-41,53-54,64-65,74-75,86-87,92 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector worked on 7 INSPECT steps during ticks 7-12, 13-43. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 18,25-26,34,36,45-46,55,57,66-67,76,78,84-85,90-91,93 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Shared worked on 4 ASSEMBLE and 5 INSPECT steps during ticks 6-47. |
| Q3_IDLE_RESOURCE | REFUSAL |  | obs mid-run / design steps Assembler is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | Whether Assembler is needed (whether the order would finish later without it): this run does not tell you. Try removing it to find out. |
| Q2_LIMITING_STEP | REFUSAL |  |  | Which machine to add: this run does not tell you. Waits and work times describe what happened; only a try that adds one machine shows whether the order then finishes sooner. |

### G7

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order finished at tick 68. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,68,70,91,93-95 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 68 ticks from the order's acceptance at tick 0: waited 33 to start CUT, then CUT took 3 on Cutter; waited 22 to start ASSEMBLE, then ASSEMBLE took 5 on Assembler; waited 0 to start INSPECT, then INSPECT took 5 on Inspector. Its longest wait shows where it spent time; on its own it does not show which machine to add. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-18,23-24,26-27,33-34,40-41,43-44,50-51,53-54,60-61,67-68,70 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter worked on 12 CUT steps during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,22,30,32,37,39,47,49,57,59,64,66,73,75,77,79,81,83,85,87,89,91,93 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler worked on 12 ASSEMBLE steps during ticks 3-63. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,29,31,36,38,46,48,56,58,63,65,72,74,76,78,80,82,84,86,88,90,92,94-95 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector worked on 12 INSPECT steps during ticks 8-68. |
| Q2_LIMITING_STEP | REFUSAL |  |  | Which machine to add: this run does not tell you. Waits and work times describe what happened; only a try that adds one machine shows whether the order then finishes sooner. |

### G8

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of CUT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 10, 8 units (units 5-12) are waiting to start CUT on Cutter. |
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of INSPECT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 10, 1 unit (unit 2) is waiting to start INSPECT on Inspector. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 10, Cutter (CUT) is busy. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 10, Assembler (ASSEMBLE) is busy. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 10, Inspector (INSPECT) is busy. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Assembler 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 10, Assembler 2 is idle and no unit is waiting for ASSEMBLE. |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 10, 0 of 12 units are finished. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order finished at tick 67. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,74,77-78,83,94-95 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 12) took 67 ticks from the order's acceptance at tick 0: waited 33 to start CUT, then CUT took 3 on Cutter; waited 0 to start ASSEMBLE, then ASSEMBLE took 4 on Assembler 2; waited 22 to start INSPECT, then INSPECT took 5 on Inspector. Its longest wait shows where it spent time; on its own it does not show which machine to add. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,14,16-17,19,22,24,29,31,34,36,41,43,46,48,53,55,60,62,65,67,72,74,77 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter worked on 12 CUT steps during ticks 0-36. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 15,20,23,32,35,44,47,56,61,68,73,81 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler worked on 6 ASSEMBLE steps during ticks 3-7, 9-13, 15-19, 21-25, 27-31, 33-37. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 21,27-28,39-40,49-50,58-59,70-71,79-80,85-95 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector worked on 12 INSPECT steps during ticks 7-67. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 18,25,30,37,42,51,54,63,66,75,78,83 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 2 worked on 6 ASSEMBLE steps during ticks 6-10, 12-16, 18-22, 24-28, 30-34, 36-40. |
| Q3_IDLE_RESOURCE | REFUSAL |  | obs mid-run / design steps Assembler 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | Whether Assembler 2 is needed (whether the order would finish later without it): this run does not tell you. Try removing it to find out. |
| Q2_LIMITING_STEP | REFUSAL |  |  | Which machine to add: this run does not tell you. Waits and work times describe what happened; only a try that adds one machine shows whether the order then finishes sooner. |

### G9a

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order finished at tick 11. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1-2,4-5,9,14-15 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 1) took 11 ticks from the order's acceptance at tick 0: waited 0 to start CUT, then CUT took 2 on Cutter; waited 0 to start ASSEMBLE, then ASSEMBLE took 3 on Assembler; waited 4 to start INSPECT, then INSPECT took 2 on Shared. Its longest wait shows where it spent time; on its own it does not show which machine to add. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,4,6-7 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter worked on 2 CUT steps during ticks 0-4. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 5,9 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler worked on 1 ASSEMBLE step during ticks 2-5. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 8,11-15 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Shared worked on 1 ASSEMBLE and 2 INSPECT steps during ticks 4-11. |
| Q2_LIMITING_STEP | REFUSAL |  |  | Which machine to add: this run does not tell you. Waits and work times describe what happened; only a try that adds one machine shows whether the order then finishes sooner. |

### G9b

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order finished at tick 9. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,6-8,11,14-15 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 2) took 9 ticks from the order's acceptance at tick 0: waited 2 to start CUT, then CUT took 2 on Cutter; waited 0 to start ASSEMBLE, then ASSEMBLE took 3 on Assembler; waited 0 to start INSPECT, then INSPECT took 2 on Shared. Its longest wait shows where it spent time; on its own it does not show which machine to add. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 5,9-10,13-15 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Shared worked on 1 ASSEMBLE and 2 INSPECT steps during ticks 2-9. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 8,11 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler worked on 1 ASSEMBLE step during ticks 4-7. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,4,6-7 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter worked on 2 CUT steps during ticks 0-4. |
| Q2_LIMITING_STEP | REFUSAL |  |  | Which machine to add: this run does not tell you. Waits and work times describe what happened; only a try that adds one machine shows whether the order then finishes sooner. |

### G10

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of CUT / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 16, 6 units (units 7-12) are waiting to start CUT on Cutter. |
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of ASSEMBLE / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 16, 1 unit (unit 5) is waiting to start ASSEMBLE on Assembler. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Cutter (CUT) is busy. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Assembler (ASSEMBLE) is busy. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 16, Inspector 1 (INSPECT) is busy. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Inspector 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 16, Inspector 2 is idle and no unit is waiting for INSPECT. |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 16, 2 of 12 units are finished. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order finished at tick 56. |
| Q4_DELAY | REFUSAL |  | obs closing | Waiting and work times are not shown: this view started watching after the order began and missed the earlier events. |
| ACTIVITY | REFUSAL |  | obs closing | Each machine's work times are not shown: this view started watching after the order began and missed the earlier events. |
| Q3_IDLE_RESOURCE | REFUSAL |  | obs mid-run / design steps Inspector 2 is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | Whether Inspector 2 is needed (whether the order would finish later without it): this run does not tell you. Try removing it to find out. |
| Q2_LIMITING_STEP | REFUSAL |  |  | Which machine to add: this run does not tell you. Waits and work times describe what happened; only a try that adds one machine shows whether the order then finishes sooner. |

### G11a

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of ASSEMBLE / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 5, 3 units (units 3-5) are waiting to start ASSEMBLE. They are not assigned to a machine yet; either Assembler 1 or Assembler 2 may take them when it becomes free. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 5, Assembler 1 (ASSEMBLE) is busy. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 5, Assembler 2 (ASSEMBLE) is busy. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Cutter is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 5, Cutter is idle and no unit is waiting for CUT. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Inspector is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 5, Inspector is idle and no unit is waiting for INSPECT. |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 5, 0 of 5 units are finished. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order finished at tick 26. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,17,19,32,38-40 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 5) took 26 ticks from the order's acceptance at tick 0: waited 4 to start CUT, then CUT took 1 on Cutter; waited 12 to start ASSEMBLE, then ASSEMBLE took 8 on Assembler 1; waited 0 to start INSPECT, then INSPECT took 1 on Inspector. Its longest wait shows where it spent time; on its own it does not show which machine to add. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,7,9-10,12-14,16-17,19 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter worked on 5 CUT steps during ticks 0-5. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 8,21,23,30,32,38 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 1 worked on 3 ASSEMBLE steps during ticks 1-25. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 11,24-25,33 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler 2 worked on 2 ASSEMBLE steps during ticks 2-18. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 22,27-29,31,35-37,39-40 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector worked on 5 INSPECT steps during ticks 9-11, 17-19, 25-26. |
| Q3_IDLE_RESOURCE | DIRECT_FACT |  | design eligible resources of CUT | Cutter is the only machine that can do CUT; the order cannot finish without it. |
| Q3_IDLE_RESOURCE | DIRECT_FACT |  | design eligible resources of INSPECT | Inspector is the only machine that can do INSPECT; the order cannot finish without it. |
| Q2_LIMITING_STEP | REFUSAL |  |  | Which machine to add: this run does not tell you. Waits and work times describe what happened; only a try that adds one machine shows whether the order then finishes sooner. |

### G11b

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q1_WAITING | BOUNDARY_COUNT | waiting-work-by-operation-step | obs mid-run / design eligible resources of ASSEMBLE / fields JobObservation.status,JobObservation.currentStep,PendingWorkObservation,ResourceObservation.queueDepth | At tick 3, 1 unit (unit 3) is waiting to start ASSEMBLE on Assembler. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.state,ResourceObservation.activeJobIds,ResourceObservation.concurrency | At tick 3, Assembler (ASSEMBLE) has 2 of 2 slots busy. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Cutter is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 3, Cutter is idle and no unit is waiting for CUT. |
| Q3_IDLE_RESOURCE | BOUNDARY_COUNT | idle-resources-and-eligible-waiting | obs mid-run / design steps Inspector is eligible for / fields ResourceObservation.state,ResourceObservation.activeJobIds,JobObservation.status,JobObservation.currentStep | At tick 3, Inspector is idle and no unit is waiting for INSPECT. |
| PROGRESS | DIRECT_FACT |  | obs mid-run / fields OrderObservation.completedQuantity,OrderObservation.requestedQuantity | At tick 3, 0 of 3 units are finished. |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order finished at tick 26. |
| Q4_DELAY | EVENT_INTERVAL | completing-unit-lead-time-decomposition | events 1,10-11,15,21-23 / fields ORDER_ACCEPTED,JOB_DISPATCHED,JOB_STEP_COMPLETED,ORDER_COMPLETED.jobId | The last unit to finish (unit 3) took 26 ticks from the order's acceptance at tick 0: waited 2 to start CUT, then CUT took 1 on Cutter; waited 10 to start ASSEMBLE, then ASSEMBLE took 12 on Assembler; waited 0 to start INSPECT, then INSPECT took 1 on Inspector. Its longest wait shows where it spent time; on its own it does not show which machine to add. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 2,5,7-8,10-11 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Cutter worked on 3 CUT steps during ticks 0-3. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 6,9,13,15-16,21 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Assembler worked on 3 ASSEMBLE steps during ticks 1-25; at most 2 of 2 slots at once. |
| ACTIVITY | EVENT_INTERVAL | job-step-occurrences-from-supported-events | events 14,18-20,22-23 / fields JOB_DISPATCHED,JOB_STEP_COMPLETED | Inspector worked on 3 INSPECT steps during ticks 13-15, 25-26. |
| Q3_IDLE_RESOURCE | DIRECT_FACT |  | design eligible resources of CUT | Cutter is the only machine that can do CUT; the order cannot finish without it. |
| Q3_IDLE_RESOURCE | DIRECT_FACT |  | design eligible resources of INSPECT | Inspector is the only machine that can do INSPECT; the order cannot finish without it. |
| Q2_LIMITING_STEP | REFUSAL |  |  | Which machine to add: this run does not tell you. Waits and work times describe what happened; only a try that adds one machine shows whether the order then finishes sooner. |

### G1+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). The order finished at tick 67, the same as before. |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This shows what happened with this change, not why. |

### G1+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 2 (ASSEMBLE). The order finished at tick 67, the same as before. |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This shows what happened with this change, not why. |

### G1+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 2 (INSPECT). The order finished at tick 56 instead of 67 (11 ticks sooner). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This shows what happened with this change, not why. |

### G2+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). The order finished at tick 56, the same as before. |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This shows what happened with this change, not why. |

### G2+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 2 (ASSEMBLE). The order finished at tick 45 instead of 56 (11 ticks sooner). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This shows what happened with this change, not why. |

### G2+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 3 (INSPECT). The order finished at tick 56, the same as before. |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This shows what happened with this change, not why. |

### G3+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). The order finished at tick 37 instead of 45 (8 ticks sooner). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This shows what happened with this change, not why. |

### G3+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 3 (ASSEMBLE). The order finished at tick 45, the same as before. |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This shows what happened with this change, not why. |

### G3+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 3 (INSPECT). The order finished at tick 45, the same as before. |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This shows what happened with this change, not why. |

### G4+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). The order finished at tick 37 instead of 45 (8 ticks sooner). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This shows what happened with this change, not why. |

### G4+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 2 (ASSEMBLE). The order finished at tick 45, the same as before. |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This shows what happened with this change, not why. |

### G4+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 3 (INSPECT). The order finished at tick 45, the same as before. |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This shows what happened with this change, not why. |

### G5+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). The order finished at tick 67, the same as before. |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This shows what happened with this change, not why. |

### G5+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 7 (ASSEMBLE). The order finished at tick 67, the same as before. |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This shows what happened with this change, not why. |

### G5+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 2 (INSPECT). The order finished at tick 45 instead of 67 (22 ticks sooner). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This shows what happened with this change, not why. |

### G7+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Cutter 2 (CUT). The order finished at tick 68, the same as before. |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This shows what happened with this change, not why. |

### G7+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Assembler 2 (ASSEMBLE). The order finished at tick 68, the same as before. |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This shows what happened with this change, not why. |

### G7+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: added Inspector 2 (INSPECT). The order finished at tick 68, the same as before. |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This shows what happened with this change, not why. |

### G2-remove-Inspector-2

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: removed Inspector 2 (INSPECT). The order finished at tick 67 instead of 56 (11 ticks later). |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This shows what happened with this change, not why. |

### G8-remove-Assembler-2

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: removed Assembler 2 (ASSEMBLE). The order finished at tick 67, the same as before. |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This shows what happened with this change, not why. |

### G9-reorder

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | One change: the machine order changed from [Cutter, Assembler, Shared] to [Shared, Assembler, Cutter]. The order finished at tick 9 instead of 11 (2 ticks sooner). |
| Q5_CONTROLLED_COMPARISON | DIRECT_FACT |  | design Engine semantics section 2 rule 4: remaining ties broken by MachineId | When several machines could take the same work and are otherwise equal, the lower-numbered one (listed earlier) takes it, so the order of machines alone can change the result. |
| Q5_CONTROLLED_COMPARISON | REFUSAL |  |  | This shows what happened with this change, not why. |

### G1+assemble+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q6_CONFOUNDED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | 2 changes: added Assembler 2 (ASSEMBLE); added Inspector 2 (INSPECT). The order finished at tick 45 instead of 67 (22 ticks sooner). |
| Q6_CONFOUNDED_COMPARISON | REFUSAL |  |  | This pair cannot tell how much each change contributed. Try them one at a time. |

### G1+cut+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q6_CONFOUNDED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | 2 changes: added Cutter 2 (CUT); added Inspector 2 (INSPECT). The order finished at tick 56 instead of 67 (11 ticks sooner). |
| Q6_CONFOUNDED_COMPARISON | REFUSAL |  |  | This pair cannot tell how much each change contributed. Try them one at a time. |

### G7+assemble+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q6_CONFOUNDED_COMPARISON | COMPARISON |  | obs closing / design authored design of both attempts, including resource order / fields OrderObservation.completedAt | 2 changes: added Assembler 2 (ASSEMBLE); added Inspector 2 (INSPECT). The order finished at tick 46 instead of 68 (22 ticks sooner). |
| Q6_CONFOUNDED_COMPARISON | REFUSAL |  |  | This pair cannot tell how much each change contributed. Try them one at a time. |

## N-naive-dashboard

Control: busyTicks utilization, combined per-machine queues, highest-utilization bottleneck, idle = surplus, first-change attribution.

### G1

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 33: Cutter 100%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs mid-run / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Cutter (tick 33): 0. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 33: Assembler 85%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs mid-run / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Assembler (tick 33): 3. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 33: Inspector 76%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs mid-run / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Inspector (tick 33): 1. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 67: Cutter 54%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Cutter (tick 67): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 67: Assembler 72%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Assembler (tick 67): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 67: Inspector 90%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Inspector (tick 67): 0. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION |  | obs closing / fields ResourceObservation.busyTicks | Bottleneck: Inspector (highest utilization). |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 67. |

### G2

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 16: Cutter 94%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs mid-run / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Cutter (tick 16): 6. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 16: Assembler 75%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs mid-run / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Assembler (tick 16): 1. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 16: Inspector 1 31%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs mid-run / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Inspector 1 (tick 16): 0. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 16: Inspector 2 31%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs mid-run / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Inspector 2 (tick 16): 0. |
| Q3_IDLE_RESOURCE | NAMED_INTERPRETATION |  | obs mid-run / fields ResourceObservation.state | Inspector 2 is surplus (idle at tick 16). |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 56: Cutter 64%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Cutter (tick 56): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 56: Assembler 86%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Assembler (tick 56): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 56: Inspector 1 54%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Inspector 1 (tick 56): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 56: Inspector 2 54%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Inspector 2 (tick 56): 0. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION |  | obs closing / fields ResourceObservation.busyTicks | Bottleneck: Assembler (highest utilization). |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 56. |

### G3

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 45: Cutter 80%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Cutter (tick 45): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 45: Assembler 53%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Assembler (tick 45): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 45: Inspector 1 67%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Inspector 1 (tick 45): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 45: Inspector 2 67%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Inspector 2 (tick 45): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 45: Assembler 2 53%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Assembler 2 (tick 45): 0. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION |  | obs closing / fields ResourceObservation.busyTicks | Bottleneck: Cutter (highest utilization). |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 45. |

### G4

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 9: Cutter 100%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs mid-run / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Cutter (tick 9): 8. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 9: Twin Assembler 44%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs mid-run / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Twin Assembler (tick 9): 0. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 9: Inspector 1 0%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs mid-run / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Inspector 1 (tick 9): 0. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 9: Inspector 2 0%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs mid-run / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Inspector 2 (tick 9): 0. |
| Q3_IDLE_RESOURCE | NAMED_INTERPRETATION |  | obs mid-run / fields ResourceObservation.state | Inspector 2 is surplus (idle at tick 9). |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 45: Cutter 80%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Cutter (tick 45): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 45: Twin Assembler 107%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Twin Assembler (tick 45): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 45: Inspector 1 67%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Inspector 1 (tick 45): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 45: Inspector 2 67%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Inspector 2 (tick 45): 0. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION |  | obs closing / fields ResourceObservation.busyTicks | Bottleneck: Twin Assembler (highest utilization). |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 45. |

### G5

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 67: Cutter 54%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Cutter (tick 67): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 67: Assembler 1 36%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Assembler 1 (tick 67): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 67: Assembler 2 36%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Assembler 2 (tick 67): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 67: Assembler 3 0%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Assembler 3 (tick 67): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 67: Assembler 4 0%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Assembler 4 (tick 67): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 67: Assembler 5 0%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Assembler 5 (tick 67): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 67: Assembler 6 0%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Assembler 6 (tick 67): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 67: Shared 90%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Shared (tick 67): 0. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION |  | obs closing / fields ResourceObservation.busyTicks | Bottleneck: Shared (highest utilization). |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 67. |

### G6

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 17: Cutter 88%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs mid-run / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Cutter (tick 17): 6. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 17: Assembler 71%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs mid-run / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Assembler (tick 17): 0. |
| Q3_IDLE_RESOURCE | NAMED_INTERPRETATION |  | obs mid-run / fields ResourceObservation.state | Assembler is surplus (idle at tick 17). |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 17: Inspector 29%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs mid-run / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Inspector (tick 17): 1. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 17: Shared 53%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs mid-run / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Shared (tick 17): 1. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 47: Cutter 77%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Cutter (tick 47): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 47: Assembler 68%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Assembler (tick 47): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 47: Inspector 74%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Inspector (tick 47): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 47: Shared 87%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Shared (tick 47): 0. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION |  | obs closing / fields ResourceObservation.busyTicks | Bottleneck: Shared (highest utilization). |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 47. |

### G7

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 68: Cutter 53%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Cutter (tick 68): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 68: Assembler 88%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Assembler (tick 68): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 68: Inspector 88%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Inspector (tick 68): 0. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION |  | obs closing / fields ResourceObservation.busyTicks | Bottleneck: Assembler (highest utilization). |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 68. |

### G8

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 10: Cutter 90%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs mid-run / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Cutter (tick 10): 8. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 10: Assembler 40%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs mid-run / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Assembler (tick 10): 0. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 10: Inspector 0%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs mid-run / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Inspector (tick 10): 1. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 10: Assembler 2 40%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs mid-run / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Assembler 2 (tick 10): 0. |
| Q3_IDLE_RESOURCE | NAMED_INTERPRETATION |  | obs mid-run / fields ResourceObservation.state | Assembler 2 is surplus (idle at tick 10). |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 67: Cutter 54%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Cutter (tick 67): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 67: Assembler 36%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Assembler (tick 67): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 67: Inspector 90%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Inspector (tick 67): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 67: Assembler 2 36%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Assembler 2 (tick 67): 0. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION |  | obs closing / fields ResourceObservation.busyTicks | Bottleneck: Inspector (highest utilization). |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 67. |

### G9a

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 11: Cutter 36%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Cutter (tick 11): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 11: Assembler 27%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Assembler (tick 11): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 11: Shared 64%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Shared (tick 11): 0. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION |  | obs closing / fields ResourceObservation.busyTicks | Bottleneck: Shared (highest utilization). |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 11. |

### G9b

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 9: Shared 78%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Shared (tick 9): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 9: Assembler 33%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Assembler (tick 9): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 9: Cutter 44%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Cutter (tick 9): 0. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION |  | obs closing / fields ResourceObservation.busyTicks | Bottleneck: Shared (highest utilization). |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 9. |

### G10

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 16: Cutter 94%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs mid-run / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Cutter (tick 16): 6. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 16: Assembler 75%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs mid-run / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Assembler (tick 16): 1. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 16: Inspector 1 31%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs mid-run / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Inspector 1 (tick 16): 0. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 16: Inspector 2 31%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs mid-run / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Inspector 2 (tick 16): 0. |
| Q3_IDLE_RESOURCE | NAMED_INTERPRETATION |  | obs mid-run / fields ResourceObservation.state | Inspector 2 is surplus (idle at tick 16). |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 56: Cutter 64%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Cutter (tick 56): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 56: Assembler 86%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Assembler (tick 56): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 56: Inspector 1 54%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Inspector 1 (tick 56): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 56: Inspector 2 54%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Inspector 2 (tick 56): 0. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION |  | obs closing / fields ResourceObservation.busyTicks | Bottleneck: Assembler (highest utilization). |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 56. |

### G11a

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 5: Cutter 100%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs mid-run / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Cutter (tick 5): 0. |
| Q3_IDLE_RESOURCE | NAMED_INTERPRETATION |  | obs mid-run / fields ResourceObservation.state | Cutter is surplus (idle at tick 5). |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 5: Assembler 1 0%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs mid-run / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Assembler 1 (tick 5): 3. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 5: Assembler 2 0%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs mid-run / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Assembler 2 (tick 5): 3. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 5: Inspector 0%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs mid-run / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Inspector (tick 5): 0. |
| Q3_IDLE_RESOURCE | NAMED_INTERPRETATION |  | obs mid-run / fields ResourceObservation.state | Inspector is surplus (idle at tick 5). |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 26: Cutter 19%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Cutter (tick 26): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 26: Assembler 1 92%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Assembler 1 (tick 26): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 26: Assembler 2 62%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Assembler 2 (tick 26): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 26: Inspector 19%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Inspector (tick 26): 0. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION |  | obs closing / fields ResourceObservation.busyTicks | Bottleneck: Assembler 1 (highest utilization). |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 26. |

### G11b

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 3: Cutter 100%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs mid-run / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Cutter (tick 3): 0. |
| Q3_IDLE_RESOURCE | NAMED_INTERPRETATION |  | obs mid-run / fields ResourceObservation.state | Cutter is surplus (idle at tick 3). |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 3: Assembler 0%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs mid-run / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Assembler (tick 3): 1. |
| ACTIVITY | DIRECT_FACT |  | obs mid-run / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 3: Inspector 0%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs mid-run / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Inspector (tick 3): 0. |
| Q3_IDLE_RESOURCE | NAMED_INTERPRETATION |  | obs mid-run / fields ResourceObservation.state | Inspector is surplus (idle at tick 3). |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 26: Cutter 12%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Cutter (tick 26): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 26: Assembler 138%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Assembler (tick 26): 0. |
| ACTIVITY | DIRECT_FACT |  | obs closing / fields ResourceObservation.busyTicks,RuntimeObservationMetadata.currentTime | Utilization at tick 26: Inspector 12%. |
| Q1_WAITING | BOUNDARY_COUNT |  | obs closing / fields ResourceObservation.queueDepth,PendingWorkObservation | Queue at Inspector (tick 26): 0. |
| Q2_LIMITING_STEP | NAMED_INTERPRETATION |  | obs closing / fields ResourceObservation.busyTicks | Bottleneck: Assembler (highest utilization). |
| PROGRESS | DIRECT_FACT |  | obs closing / fields OrderObservation.completedAt | The order completed at tick 26. |

### G1+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / fields OrderObservation.completedAt | Completion improved by 0 ticks due to added 1 x CUT. |

### G1+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / fields OrderObservation.completedAt | Completion improved by 0 ticks due to added 1 x ASSEMBLE. |

### G1+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / fields OrderObservation.completedAt | Completion improved by 11 ticks due to added 1 x INSPECT. |

### G2+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / fields OrderObservation.completedAt | Completion improved by 0 ticks due to added 1 x CUT. |

### G2+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / fields OrderObservation.completedAt | Completion improved by 11 ticks due to added 1 x ASSEMBLE. |

### G2+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / fields OrderObservation.completedAt | Completion improved by 0 ticks due to added 1 x INSPECT. |

### G3+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / fields OrderObservation.completedAt | Completion improved by 8 ticks due to added 1 x CUT. |

### G3+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / fields OrderObservation.completedAt | Completion improved by 0 ticks due to added 1 x ASSEMBLE. |

### G3+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / fields OrderObservation.completedAt | Completion improved by 0 ticks due to added 1 x INSPECT. |

### G4+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / fields OrderObservation.completedAt | Completion improved by 8 ticks due to added 1 x CUT. |

### G4+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / fields OrderObservation.completedAt | Completion improved by 0 ticks due to added 1 x ASSEMBLE. |

### G4+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / fields OrderObservation.completedAt | Completion improved by 0 ticks due to added 1 x INSPECT. |

### G5+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / fields OrderObservation.completedAt | Completion improved by 0 ticks due to added 1 x CUT. |

### G5+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / fields OrderObservation.completedAt | Completion improved by 0 ticks due to added 1 x ASSEMBLE. |

### G5+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / fields OrderObservation.completedAt | Completion improved by 22 ticks due to added 1 x INSPECT. |

### G7+cut

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / fields OrderObservation.completedAt | Completion improved by 0 ticks due to added 1 x CUT. |

### G7+assemble

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / fields OrderObservation.completedAt | Completion improved by 0 ticks due to added 1 x ASSEMBLE. |

### G7+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / fields OrderObservation.completedAt | Completion improved by 0 ticks due to added 1 x INSPECT. |

### G2-remove-Inspector-2

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / fields OrderObservation.completedAt | Completion worsened by 11 ticks due to removed 1 x INSPECT. |

### G8-remove-Assembler-2

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / fields OrderObservation.completedAt | Completion improved by 0 ticks due to removed 1 x ASSEMBLE. |

### G9-reorder

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q5_CONTROLLED_COMPARISON | COMPARISON |  | obs closing / fields OrderObservation.completedAt | Same equipment; completion changed by -2 ticks (run-to-run variation). |

### G1+assemble+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q6_CONFOUNDED_COMPARISON | COMPARISON |  | obs closing / fields OrderObservation.completedAt | Completion improved by 22 ticks due to added 1 x ASSEMBLE. |

### G1+cut+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q6_CONFOUNDED_COMPARISON | COMPARISON |  | obs closing / fields OrderObservation.completedAt | Completion improved by 11 ticks due to added 1 x CUT. |

### G7+assemble+inspect

| Question | Kind | Method | Evidence | Statement |
|---|---|---|---|---|
| Q6_CONFOUNDED_COMPARISON | COMPARISON |  | obs closing / fields OrderObservation.completedAt | Completion improved by 22 ticks due to added 1 x ASSEMBLE. |
