# Walkthrough key (do not read before answering)

| Item | Fixture | Truth |
|---|---|---|
| W1 | G5 | Q2 truth: adding an inspector helps (67->45); the CUT pool is the most occupied but adding a cutter does nothing. |
| W2 | G1+cut | Controlled: one cutter added, no change. |
| W3 | G2 | Q2 truth: adding an assembler helps (56->45). Q3: Inspector 2 is idle with no work at tick 17, yet removing it costs 11 ticks; surplus is not decidable from the run. |
| W4 | G2+assemble | Controlled: one assembler added, 56->45 (constraint migration from INSPECT to ASSEMBLE). |
| W5 | G9-reorder | Only the resource order changed, 11->9. |
| W6 | G1 | Q2 truth: adding an inspector helps (67->56); a cutter or an assembler alone does not. Q4: the last unit waited longest for CUT (33), the release-at-once backlog; CUT is not the limiting step. |
| W7 | G3+cut | Controlled: one cutter added, 45->37 (migration to CUT). |
| W8 | G4 | Q2 truth: adding a cutter helps (45->37). At tick 9 both twin slots are in use while completed-step credit is 4 ticks; at completion the twin's credited ticks (48) exceed elapsed time (45). |
| W9 | G10 | Same run as G2 seen by a late joiner: current state is available, interval measurements are refused. |
| W10 | G8 | Q3: Assembler 2 is idle with no work at tick 11; removing it changes nothing (67): surplus for completion, but not decidable from the run. |
| W11 | G7 | Q2 truth: no single addition helps (68); only adding both an assembler and an inspector does (46). |
| W12 | G7+assemble+inspect | Confounded interaction: each single change gives 0; together -22. |
| W13 | G6 | Multi-eligible INSPECT waiting at tick 17 is not either eligible resource's queue. The completion chain ties at tick 33; resource order alone moves completion between 46 and 47. |
| W14 | G8-remove-Assembler-2 | Controlled removal, no change: Assembler 2 was not needed to finish at 67. |
| W15 | G1+assemble+inspect | Confounded: singles give 0 (assembler) and -11 (inspector); together -22. Attribution is order-dependent. |
| W16 | G1+inspect | Controlled: one inspector added, 67->56. |
