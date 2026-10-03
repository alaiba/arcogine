# Strategy-space experiment output (pass 1)

Projectable designs per (profile, N): 2112

## Granularity control (1 TWIN_ASSEMBLER vs 2 ASSEMBLER)

- P1/N6: 1140 pairs, 0 completion mismatches
- P1/N12: 1140 pairs, 0 completion mismatches
- P1/N24: 1140 pairs, 0 completion mismatches
- P2/N6: 1140 pairs, 0 completion mismatches
- P2/N12: 1140 pairs, 0 completion mismatches
- P2/N24: 1140 pairs, 0 completion mismatches

## Cell map by family

| Family | cells | CC material | IC holds | migration | MVS-W (CP) | CP binding | MVS-S | monotone | incomparable-unexplained>0 | provision-distinct ties>0 | positive |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| D | 576 | 276 | 220 | 324 | 280 | 550 | 37 | 481 | 0 | 9 | 15 |
| F | 2304 | 1124 | 1028 | 1144 | 1269 | 2302 | 796 | 1370 | 374 | 308 | 338 |

## MVS-S and positive cells by (profile, N, family, rule)

| profile | N | family | rule | cells | MVS-W | MVS-S | positive |
|---|---:|---|---|---:|---:|---:|---:|
| P1 | 12 | D | UNIFORM | 48 | 18 | 0 | 0 |
| P1 | 12 | D | WORK | 48 | 25 | 11 | 9 |
| P1 | 12 | F | UNIFORM | 192 | 95 | 61 | 30 |
| P1 | 12 | F | WORK | 192 | 109 | 51 | 23 |
| P1 | 24 | D | UNIFORM | 48 | 18 | 0 | 0 |
| P1 | 24 | D | WORK | 48 | 27 | 5 | 0 |
| P1 | 24 | F | UNIFORM | 192 | 97 | 62 | 37 |
| P1 | 24 | F | WORK | 192 | 111 | 51 | 10 |
| P1 | 6 | D | UNIFORM | 48 | 17 | 0 | 0 |
| P1 | 6 | D | WORK | 48 | 25 | 9 | 6 |
| P1 | 6 | F | UNIFORM | 192 | 83 | 57 | 25 |
| P1 | 6 | F | WORK | 192 | 89 | 45 | 26 |
| P2 | 12 | D | UNIFORM | 48 | 24 | 0 | 0 |
| P2 | 12 | D | WORK | 48 | 27 | 0 | 0 |
| P2 | 12 | F | UNIFORM | 192 | 112 | 73 | 31 |
| P2 | 12 | F | WORK | 192 | 118 | 77 | 38 |
| P2 | 24 | D | UNIFORM | 48 | 24 | 2 | 0 |
| P2 | 24 | D | WORK | 48 | 27 | 6 | 0 |
| P2 | 24 | F | UNIFORM | 192 | 116 | 74 | 36 |
| P2 | 24 | F | WORK | 192 | 124 | 87 | 47 |
| P2 | 6 | D | UNIFORM | 48 | 24 | 4 | 0 |
| P2 | 6 | D | WORK | 48 | 24 | 0 | 0 |
| P2 | 6 | F | UNIFORM | 192 | 107 | 80 | 13 |
| P2 | 6 | F | WORK | 192 | 108 | 78 | 22 |

## MVS-S cells by flex price (family F)

| rule | phi | cells | MVS-S | positive |
|---|---:|---:|---:|---:|
| UNIFORM | 100 | 288 | 110 | 27 |
| UNIFORM | 125 | 288 | 155 | 81 |
| UNIFORM | 150 | 288 | 131 | 59 |
| UNIFORM | 200 | 288 | 11 | 5 |
| WORK | 100 | 288 | 144 | 48 |
| WORK | 125 | 288 | 148 | 70 |
| WORK | 150 | 288 | 70 | 33 |
| WORK | 200 | 288 | 27 | 15 |

## Reference cell P1|N12|D|WORK|d90|p0|f60|b25

Prices: CUTTER=100 ASSEMBLER=300 TWIN_ASSEMBLER=540 INSPECTOR=150 

starter T=77, floor T=17 at cost 3220, target=53, budget=987, designs=432

Complete global frontier (feasible rows marked):

| # | cost | T | feasible | members | provision (cut,asm,insp,flex) | active | pool occupancy | shared dual use |
|---:|---:|---:|---|---|---|---|---|---|
| 0 | 550 | 77 |  | C1A1T0I1F0 | (1,1,1,0) | ASSEMBLE | CUT=24/77;ASSEMBLE=72/77;INSPECT=36/77 | false |
| 1 | 790 | 44 | yes | C1A0T1I1F0 | (1,2,1,0) | ASSEMBLE|INSPECT | CUT=24/44;ASSEMBLE=72/88;INSPECT=36/44 | false |
| 2 | 940 | 43 | yes | C1A0T1I2F0 | (1,2,2,0) | ASSEMBLE | CUT=24/43;ASSEMBLE=72/86;INSPECT=36/86 | false |
| 3 | 1040 | 41 |  | C2A0T1I2F0 | (2,2,2,0) | ASSEMBLE | CUT=24/82;ASSEMBLE=72/82;INSPECT=36/82 | false |
| 4 | 1240 | 33 |  | C1A1T1I2F0 | (1,3,2,0) | CUT|ASSEMBLE | CUT=24/33;ASSEMBLE=72/99;INSPECT=36/66 | false |
| 5 | 1340 | 32 |  | C2A1T1I2F0 | (2,3,2,0) | ASSEMBLE | CUT=24/64;ASSEMBLE=72/96;INSPECT=36/64 | false |
| 6 | 1490 | 31 |  | C2A1T1I3F0 | (2,3,3,0) | ASSEMBLE | CUT=24/62;ASSEMBLE=72/93;INSPECT=36/93 | false |
| 7 | 1580 | 26 |  | C2A0T2I2F0 | (2,4,2,0) | ASSEMBLE|INSPECT | CUT=24/52;ASSEMBLE=72/104;INSPECT=36/52 | false |
| 8 | 1880 | 25 |  | C2A0T2I4F0 | (2,4,4,0) | ASSEMBLE | CUT=24/50;ASSEMBLE=72/100;INSPECT=36/100 | false |
| 9 | 2030 | 23 |  | C2A1T2I3F0 | (2,5,3,0) | ASSEMBLE | CUT=24/46;ASSEMBLE=72/115;INSPECT=36/69 | false |
| 10 | 2270 | 22 |  | C2A0T3I3F0 | (2,6,3,0) | CUT|ASSEMBLE|INSPECT | CUT=24/44;ASSEMBLE=72/132;INSPECT=36/66 | false |
| 11 | 2370 | 20 |  | C3A0T3I3F0 | (3,6,3,0) | ASSEMBLE|INSPECT | CUT=24/60;ASSEMBLE=72/120;INSPECT=36/60 | false |
| 12 | 3120 | 19 |  | C3A2T3I4F0 | (3,8,4,0) | ASSEMBLE|INSPECT | CUT=24/57;ASSEMBLE=72/152;INSPECT=36/76 | false |
| 13 | 3220 | 17 |  | C4A2T3I4F0 | (4,8,4,0) | ASSEMBLE|INSPECT | CUT=24/68;ASSEMBLE=72/136;INSPECT=36/68 | false |

MVS-W=true, MVS-S=false, incomparable-unexplained pairs=0, monotone=true, tie classes=0, capital binding=true, positive=false

Interventions (starter design and every feasible frontier design):

- base C1A1T0I1F0 T=77 active=ASSEMBLE; at-constraint [AtProbe[offer=ASSEMBLER, delta=33, outsideLimit=false]] best=ASSEMBLER dT=33 material=true; away [AwayProbe[step=0, offer=CUTTER, units=1, delta=0, outsideLimit=false], AwayProbe[step=0, offer=CUTTER, units=3, delta=0, outsideLimit=false], AwayProbe[step=2, offer=INSPECTOR, units=1, delta=0, outsideLimit=false], AwayProbe[step=2, offer=INSPECTOR, units=2, delta=0, outsideLimit=false]] irrelevant=true; migration ASSEMBLE@77 -> +1ASSEMBLER:ASSEMBLE|INSPECT@44 => units=1 to ASSEMBLE|INSPECT
- base C1A0T1I1F0 T=44 active=ASSEMBLE|INSPECT; at-constraint [AtProbe[offer=ASSEMBLER, delta=0, outsideLimit=false], AtProbe[offer=INSPECTOR, delta=1, outsideLimit=false]] best=INSPECTOR dT=1 material=false; away [AwayProbe[step=0, offer=CUTTER, units=1, delta=0, outsideLimit=false], AwayProbe[step=0, offer=CUTTER, units=2, delta=0, outsideLimit=false]] irrelevant=false; migration ASSEMBLE|INSPECT@44 -> +1INSPECTOR:ASSEMBLE@43 -> +2INSPECTOR:ASSEMBLE@43 -> +3INSPECTOR:ASSEMBLE@43 -> +4INSPECTOR:ASSEMBLE@43 => units=0 to 
- base C1A0T1I2F0 T=43 active=ASSEMBLE; at-constraint [AtProbe[offer=ASSEMBLER, delta=10, outsideLimit=false]] best=ASSEMBLER dT=10 material=true; away [AwayProbe[step=0, offer=CUTTER, units=1, delta=2, outsideLimit=false], AwayProbe[step=0, offer=CUTTER, units=3, delta=2, outsideLimit=false], AwayProbe[step=2, offer=INSPECTOR, units=1, delta=0, outsideLimit=false], AwayProbe[step=2, offer=INSPECTOR, units=2, delta=0, outsideLimit=false]] irrelevant=true; migration ASSEMBLE@43 -> +1ASSEMBLER:CUT|ASSEMBLE@33 => units=1 to CUT|ASSEMBLE

Projection-order control (reversed offer order):

- checked all 14 frontier designs

Mid-run waiting corroboration (landed WaitingWorkByStepOracle at floor(T/2)):

- C1A1T0I1F0 T=77 at 38: [WaitingAtStep[operationId=1, stepId=2, stepName=ASSEMBLE, waitingJobs=5, attribution=SingleResource[machineId=Machine(2)]]]
- C1A0T1I1F0 T=44 at 22: [WaitingAtStep[operationId=1, stepId=2, stepName=ASSEMBLE, waitingJobs=3, attribution=SingleResource[machineId=Machine(2)]], WaitingAtStep[operationId=1, stepId=3, stepName=INSPECT, waitingJobs=1, attribution=SingleResource[machineId=Machine(3)]]]
- C1A0T1I2F0 T=43 at 21: [WaitingAtStep[operationId=1, stepId=1, stepName=CUT, waitingJobs=1, attribution=SingleResource[machineId=Machine(1)]], WaitingAtStep[operationId=1, stepId=2, stepName=ASSEMBLE, waitingJobs=3, attribution=SingleResource[machineId=Machine(2)]]]

## Reference cell P1|N12|F|WORK|d90|p125|f60|b25

Prices: CUTTER=100 ASSEMBLER=300 TWIN_ASSEMBLER=540 INSPECTOR=150 FLEX_CELL=375 

starter T=77, floor T=16 at cost 4945, target=52, budget=987, designs=2112

Complete global frontier (feasible rows marked):

| # | cost | T | feasible | members | provision (cut,asm,insp,flex) | active | pool occupancy | shared dual use |
|---:|---:|---:|---|---|---|---|---|---|
| 0 | 475 | 110 |  | C1A0T0I0F1 | (1,0,0,1) | ASSEMBLE+INSPECT | CUT=24/110;ASSEMBLE+INSPECT=108/110 | true |
| 1 | 550 | 77 |  | C1A1T0I1F0 | (1,1,1,0) | ASSEMBLE | CUT=24/77;ASSEMBLE=72/77;INSPECT=36/77 | false |
| 2 | 775 | 59 |  | C1A1T0I0F1 | (1,1,0,1) | ASSEMBLE+INSPECT | CUT=24/59;ASSEMBLE+INSPECT=108/118 | true |
| 3 | 790 | 44 | yes | C1A0T1I1F0 | (1,2,1,0) | ASSEMBLE|INSPECT | CUT=24/44;ASSEMBLE=72/88;INSPECT=36/44 | false |
| 4 | 940 | 43 | yes | C1A0T1I2F0 | (1,2,2,0) | ASSEMBLE | CUT=24/43;ASSEMBLE=72/86;INSPECT=36/86 | false |
| 5 | 1040 | 41 |  | C2A0T1I2F0 | (2,2,2,0) | ASSEMBLE | CUT=24/82;ASSEMBLE=72/82;INSPECT=36/82 | false |
| 6 | 1165 | 35 |  | C1A0T1I1F1 | (1,2,1,1) | ASSEMBLE+INSPECT | CUT=24/35;ASSEMBLE+INSPECT=108/140 | true |
| 7 | 1240 | 33 |  | C1A1T1I2F0 | (1,3,2,0) | CUT|ASSEMBLE | CUT=24/33;ASSEMBLE=72/99;INSPECT=36/66 | false |
| 8 | 1340 | 32 |  | C2A1T1I2F0 | (2,3,2,0) | ASSEMBLE | CUT=24/64;ASSEMBLE=72/96;INSPECT=36/64 | false |
| 9 | 1490 | 31 |  | C2A1T1I3F0 | (2,3,3,0) | ASSEMBLE | CUT=24/62;ASSEMBLE=72/93;INSPECT=36/93 | false |
| 10 | 1580 | 26 |  | C2A0T2I2F0 | (2,4,2,0) | ASSEMBLE|INSPECT | CUT=24/52;ASSEMBLE=72/104;INSPECT=36/52 | false |
| 11 | 1880 | 25 |  | C2A0T2I4F0 | (2,4,4,0) | ASSEMBLE | CUT=24/50;ASSEMBLE=72/100;INSPECT=36/100 | false |
| 12 | 1955 | 24 |  | C2A0T2I2F1 | (2,4,2,1) | ASSEMBLE+INSPECT | CUT=24/48;ASSEMBLE+INSPECT=108/168 | true |
| 13 | 2030 | 23 |  | C2A1T2I3F0 | (2,5,3,0) | ASSEMBLE | CUT=24/46;ASSEMBLE=72/115;INSPECT=36/69 | false |
| 14 | 2270 | 22 |  | C2A0T3I3F0 | (2,6,3,0) | CUT|ASSEMBLE|INSPECT | CUT=24/44;ASSEMBLE=72/132;INSPECT=36/66 | false |
| 15 | 2370 | 20 |  | C3A0T3I3F0 | (3,6,3,0) | ASSEMBLE|INSPECT | CUT=24/60;ASSEMBLE=72/120;INSPECT=36/60 | false |
| 16 | 2880 | 19 |  | C3A1T2I3F2 | (3,5,3,2) | ASSEMBLE+INSPECT | CUT=24/57;ASSEMBLE+INSPECT=108/190 | true |
| 17 | 3205 | 17 |  | C4A1T2I2F3 | (4,5,2,3) | ASSEMBLE+INSPECT | CUT=24/68;ASSEMBLE+INSPECT=108/170 | true |
| 18 | 4945 | 16 |  | C4A4T3I4F3 | (4,10,4,3) | ASSEMBLE+INSPECT | CUT=24/64;ASSEMBLE+INSPECT=108/272 | true |

MVS-W=true, MVS-S=false, incomparable-unexplained pairs=0, monotone=true, tie classes=0, capital binding=true, positive=false

Interventions (starter design and every feasible frontier design):

- base C1A1T0I1F0 T=77 active=ASSEMBLE; at-constraint [AtProbe[offer=ASSEMBLER, delta=33, outsideLimit=false]] best=ASSEMBLER dT=33 material=true; away [AwayProbe[step=0, offer=CUTTER, units=1, delta=0, outsideLimit=false], AwayProbe[step=0, offer=CUTTER, units=3, delta=0, outsideLimit=false], AwayProbe[step=2, offer=INSPECTOR, units=1, delta=0, outsideLimit=false], AwayProbe[step=2, offer=INSPECTOR, units=2, delta=0, outsideLimit=false]] irrelevant=true; migration ASSEMBLE@77 -> +1ASSEMBLER:ASSEMBLE|INSPECT@44 => units=1 to ASSEMBLE|INSPECT
- base C1A0T1I1F0 T=44 active=ASSEMBLE|INSPECT; at-constraint [AtProbe[offer=ASSEMBLER, delta=0, outsideLimit=false], AtProbe[offer=INSPECTOR, delta=1, outsideLimit=false]] best=INSPECTOR dT=1 material=false; away [AwayProbe[step=0, offer=CUTTER, units=1, delta=0, outsideLimit=false], AwayProbe[step=0, offer=CUTTER, units=2, delta=0, outsideLimit=false]] irrelevant=false; migration ASSEMBLE|INSPECT@44 -> +1INSPECTOR:ASSEMBLE@43 -> +2INSPECTOR:ASSEMBLE@43 -> +3INSPECTOR:ASSEMBLE@43 -> +4INSPECTOR:ASSEMBLE@43 => units=0 to 
- base C1A0T1I2F0 T=43 active=ASSEMBLE; at-constraint [AtProbe[offer=ASSEMBLER, delta=10, outsideLimit=false]] best=ASSEMBLER dT=10 material=true; away [AwayProbe[step=0, offer=CUTTER, units=1, delta=2, outsideLimit=false], AwayProbe[step=0, offer=CUTTER, units=3, delta=2, outsideLimit=false], AwayProbe[step=2, offer=INSPECTOR, units=1, delta=0, outsideLimit=false], AwayProbe[step=2, offer=INSPECTOR, units=2, delta=0, outsideLimit=false]] irrelevant=true; migration ASSEMBLE@43 -> +1ASSEMBLER:CUT|ASSEMBLE@33 => units=1 to CUT|ASSEMBLE

Projection-order control (reversed offer order):

- DIFFERS C1A1T0I0F1: 59 vs reversed 62
- DIFFERS C1A0T1I1F1: 35 vs reversed 37
- DIFFERS C2A0T2I2F1: 24 vs reversed 25
- DIFFERS C3A1T2I3F2: 19 vs reversed 20
- DIFFERS C4A1T2I2F3: 17 vs reversed 20
- checked all 19 frontier designs

Mid-run waiting corroboration (landed WaitingWorkByStepOracle at floor(T/2)):

- C1A1T0I1F0 T=77 at 38: [WaitingAtStep[operationId=1, stepId=2, stepName=ASSEMBLE, waitingJobs=5, attribution=SingleResource[machineId=Machine(2)]]]
- C1A0T1I1F0 T=44 at 22: [WaitingAtStep[operationId=1, stepId=2, stepName=ASSEMBLE, waitingJobs=3, attribution=SingleResource[machineId=Machine(2)]], WaitingAtStep[operationId=1, stepId=3, stepName=INSPECT, waitingJobs=1, attribution=SingleResource[machineId=Machine(3)]]]
- C1A0T1I2F0 T=43 at 21: [WaitingAtStep[operationId=1, stepId=1, stepName=CUT, waitingJobs=1, attribution=SingleResource[machineId=Machine(1)]], WaitingAtStep[operationId=1, stepId=2, stepName=ASSEMBLE, waitingJobs=3, attribution=SingleResource[machineId=Machine(2)]]]

## Candidate selection and robustness

Reference cell (family F) positive: false; positive cells in window: 353 of 2880

Candidate: P2|N12|F|WORK|d90|p125|f80|b25 pair C1A1T0I2F0[850,56,ASSEMBLE] vs C1A1T0I1F1[913,47,CUT|ASSEMBLE+INSPECT]

| axis | neighbours | MVS-S | positive | collapsed to one design |
|---|---:|---:|---:|---:|
| target f | 1 | 1 | 0 | 0 |
| budget beta | 2 | 2 | 2 | 0 |
| quantity N | 2 | 2 | 2 | 0 |
| twin delta | 2 | 2 | 2 | 0 |
| flex phi | 2 | 2 | 2 | 0 |
| cost rule | 1 | 1 | 1 | 0 |

Robust under the pre-registered rule: true

## Execution

Runtime executions: 14921; replay-checked design keys: 2148
