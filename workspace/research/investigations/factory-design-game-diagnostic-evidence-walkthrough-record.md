# Game diagnostic evidence: owner walkthrough record

> **Custody:** temporary research evidence. This is single-person qualitative evidence from the
> repository owner, not population evidence. Under the brief, a failure is useful falsification and a
> success is only a smoke test.
>
> **Packet:** the blinded walkthrough packet persisted with the experiment results
> (`game-diagnostic-evidence-results/walkthrough-packet.md`, unchanged since the experiment commit).
>
> **Blinding:** the owner stated before starting that they had not read the key, the oracle, the
> checkpoint or the report, and had not touched the repository. The owner answered every item before
> the key was revealed.
>
> **Date:** 2026-10-06, facilitated in chat by the research agent.

## Facilitation disclosures

- Items were shown one at a time in packet order, W1 to W16.
- From W8 onward, the facilitator condensed some bullets: per-resource slot lines were merged and some
  refusal sentences shortened. No facts were added or changed, but those items were not word for word
  from the packet. Early items also dropped some derivation-name brackets.
- On W11 the facilitator mislabeled the largest-delay question as "3". The owner's "3 33" is
  confirmed by the owner as the answer to question 4.
- After W8 the owner said they did not understand question 3. The facilitator then defined
  "starved", "surplus" and "can't tell" without hinting at any answer. The owner chose not to revise
  any earlier answer.
- Unprompted feedback from the owner: "starved" and "surplus" should be included or explained in the
  game if they are important. For one-change comparisons, the owner said the answer is given in the
  design itself. After the session, the owner reported that **the walkthrough was very hard to do**:
  it was unclear what to do and what to apply. It needs to be much clearer and cleaner for a second
  pass.

## Answers (verbatim) and key

Questions: 1 = what is waiting; 2 = limiting step; 3 = state of an idle resource (starved, surplus or
can't tell); 4 = largest measured delay; 5 = what changed and what changed in the outcome; 6 = which
change accounts for the difference.

| Item | Fixture | Owner's answer (verbatim) | Key | Assessment |
|---|---|---|---|---|
| W1 | G5 | "2 CUT / 3 starved / 4 27 / No idea" (on the chain line) | 2: INSPECT (adding a cutter does nothing). 3: order complete, so not starvation. 4: 33 (CUT) | Wrong on 2, 3 and 4; chain line not understood |
| W2 | G1 + cutter | "5 added a cutter, no change 6 there was no change because the routing makes the cutter irrelevant to the process" | Cutter added, no change | 5 right; 6 asserts a mechanism the item disclaims |
| W3 | G2 | "1 - 1 unit for assembler / 2 - CUT / 3 - can't tell / 4 - 33 / I don't really understand the completion chain line" | 1: 6 units for CUT and 1 for ASSEMBLE. 2: ASSEMBLE. 3: not decidable. 4: 33 | 1 incomplete; 2 wrong; 3 and 4 right; chain line not understood |
| W4 | G2 + assembler | "5 added an assembler, 11 ticks faster 6 the assembler / the answer is given in the design itself" | 56 → 45 | Right |
| W5 | G9 reorder | "5 reordered resources, 2 ticks faster 6 the order" | 11 → 9, order only | Right |
| W6 | G1 | "1 3 units on assembler, 1 unit on inspector / 2 inspect / 3 no resource is idle / 4 33 / clarifies" | 2: INSPECT | Right; chain line clarified |
| W7 | G3 + cutter | "5 added a cutter, 8 ticks faster 6 the cutter / for this kind of question the answer is in the design itself explicitly" | 45 → 37 | Right |
| W8 | G4 | "1 8 units wait for cutter / 2 assembler / 3 I don't think I understand the question, even if I answered it before / 4 33 / changes my answer from assembler to CUT" | 2: CUT | 2 wrong until the chain line corrected it; question 3 not understood |
| W9 | G10 (G2 seen by a late joiner) | "1 6 units on the CUT, 1 unit in ASSEMBLE / 2 CUT / 3 starved / 4 16" | 2: ASSEMBLE. 4: refused (window incomplete) | 2 wrong; on 4, the snapshot tick was read as a delay; 3 descriptive only |
| W10 | G8 | "1 8 units on CUT, 1 on INSPECT / 2 INSPECT / 3 starving / 4 33" | 3: removing Assembler 2 changes nothing (not decidable from the run) | 1, 2 and 4 consistent with the evidence; 3 misses that the resource is not needed |
| W11 | G7 | "2 ASSEMBLE 3 33" (the "3" was question 4, confirmed) | 2: no single addition helps; only adding both does | 2 overclaims; 4 right |
| W12 | G7 + assembler + inspector | "5 added assembler and inspector, 22 ticks faster 6 both" | Pure interaction | Right; no split claimed |
| W13 | G6 | "1 6 for CUT, 1 for INSPECT / 2 CUT / 3 starved / 4 33 / it confuses me, I'm not sure how it is relevant" | Single additions all help (cutter 47 → 44 is the largest); the chain ties | 1, 2 and 4 consistent; chain line confused |
| W14 | G8 − Assembler 2 | "5 removed assembler 2, no change 6 the assembler" | No change | Right |
| W15 | G1 + assembler + inspector | "5 added assembler and inspector, 22 ticks faster 6 both" | Singles 0 and −11; split depends on order | Right; no split claimed |
| W16 | G1 + inspector | "5 added inspector, 11 ticks faster 6 the inspector" | 67 → 56 | Right |

## Findings

1. **Comparisons work.** All 8 comparisons were correct on change and outcome, and two-change
   attribution refusals were respected. One mechanism overreach (W2) passed the "does not show why"
   line.
2. **The single-run limiting-step refusal fails in use.** The owner inferred a limiting step anyway,
   usually the step with the largest wait: CUT in W1, W3 and W9. W11 also overclaimed. The report
   anticipated this risk; the walkthrough confirms it.
3. **The completion-chain line helps only when it names one step** (W6, W8). A tie or a long trace was
   not understood or confused the owner (W1, W3, W13).
4. **Idle-resource terms are unusable undefined.** "Starved" was applied at the end of a completed run
   (W1) and to a resource the order did not need (W10). Question 3 was not understood.
5. **A refused interval was misread** (W9: the snapshot tick was read as a delay).
6. **The protocol itself was too hard.** The owner did not understand what to do or what to apply. Part
   of the failure above may be protocol failure, not contract failure. The two cannot be separated from
   this pass.

## Consequence

Under the brief's falsification condition, the minimal bundle (C3b) **is not promotable unchanged**.
Its mechanical audit result stands. Its player-facing form needs a revision and a second blinded pass.

Candidate changes for the next contract revision (not yet designed or audited):

- define the idle-resource terms, or replace the question;
- state at the delay measurement that the largest wait is not the limiting step;
- make the retry path the visible answer to "what limits this design";
- show the completion-chain line only when it names a single step, or drop it.

Candidate changes for a cleaner second-pass protocol:

- a short worked example before the first item;
- one question per screen, with its definition next to it;
- remove derivation-name brackets from the player view;
- separate attempt and comparison sessions;
- present items verbatim from a regenerated packet;
- use fresh fixture variants, so this pass's exposure does not carry over.
