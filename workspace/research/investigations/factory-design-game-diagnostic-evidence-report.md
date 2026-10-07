# Factory-design game diagnostic evidence: investigation report (revision 3, consolidated)

> **Research status:** `READY`. The question stays admitted and unresolved until its consequence is
> reconciled; this report does not change the register.
>
> **Research baseline:** live `main` `9e9e4678b18286cea83387b5ffc38741640f0db5`.
>
> **Final repository recheck:** live `main` `306205c33ef93f0433a26c92f85cbd82eadd27f8`, unchanged since
> the first revision. That change was test coverage only, with no Engine, Factory or substrate behavior
> change. Every experiment was rerun on the JDK 21 compatibility floor at each step, with 0 failures
> each time:
>
> - 147 tests (first revision);
> - 152 tests (second-pass walkthrough);
> - 156 tests (bakery missions).
>
> **This revision:**
> - It supersedes revision 1 (`bb37e2598d2684f7dc47b6db75884b798a0ae040`) and revision 2
>   (`e5debf2584b2a3096115c2ed8b117e2ac653cb31`), which stay in the workspace history as evidence.
> - It consolidates two further owner-directed iterations and the owner's scope decisions. Several of
>   revision 1's conclusions are reversed here, and the reversals are stated explicitly.
> - Any adversarial review must bind to this revision.
>
> **Authority:** research evidence only. This report is not accepted architecture, product direction,
> a game requirement, or implementation commitment. It decides no analytics ownership.
>
> **Adversarial-review status:** required (High risk), **not yet performed**. The self-challenge below
> is self-administered.
>
> **Owner walkthroughs:** three sessions. The first failed in part, the second was paused for scope
> discussion, and the third succeeded with defects. The owner co-designed the third pass's scenarios
> and missions, which limits blindness to the answer key only (section 14).

## 1. Question, and how the owner sharpened it

The brief asks: what minimum player-facing diagnostic evidence contract can the game build from
supported non-spatial simulation facts, so that every displayed diagnosis is mechanically traceable,
explicit about its method, able to refuse unsupported causal attribution, and inspectable without
exposing internals?

During the investigation the owner established that the brief defines *truthful* but not *diagnostic*.
It names no player goals, and its six fixed diagnostic questions were traps for truthfulness, not
goals. The owner set the goals and the method (recorded in the discovery notes):

- **Goals.** A player should be able to:
  1. understand what happened in one recorded run;
  2. learn the industry concepts that run exhibits, with the jargon introduced in context and
     explained on demand (learning by experiment, not school);
  3. form and check hypotheses about design changes, by exploring alternatives.
- **Method.** "No UI" was too abstract for a human. A **text UI** that tests concepts is the right
  medium. Medium choices (animation, Gantt charts, dashboards) stay out.
- **Static only.** Use pre-computed recorded runs and variants. A live simulation loop is a separate,
  future research question.

**Proposed refined question** (for the owner to adopt in the brief at reconciliation):

> Given recorded runs of non-spatial factory designs, what must a player be able to inspect, and what
> may the game claim, so that the player can understand a run, learn the production concepts it
> exhibits, and check hypotheses against alternatives, with every claim traceable to supported facts
> or a stated definition, and no claim stronger than its evidence?

## 2. Decision at stake

1. Whether a first playable slice can expose a truthful, usable diagnostic surface over the current
   non-spatial engine.
2. Which requirements to promote into the game consumer plan.
3. Which concrete measurement requirements to hand to the reopened simulation-analytics ownership
   research, without choosing their owner.
4. Which platform gaps and follow-up questions must be tracked.

## 3. Scope and non-goals

**In scope:**

- truth constraints, mechanically audited;
- inspection and teaching presentation in a text prototype;
- starvation and utilization as worked concepts;
- concrete measurement requirements;
- platform requirements;
- the owner's decision to remove the engine's busy counter, and its reconciliation scope.

**Not done:**

- game, analytics or Engine implementation;
- analytics ownership;
- any live simulation loop;
- medium or visual decisions;
- population-level comprehension;
- register, planning or architecture edits (all deferred to reconciliation).

## 4. Executive conclusion

**Provisional resolution: positive, bounded. The answer is satisfactory for the decision at stake.**
A truthful, usable diagnostic surface over the current non-spatial engine is achievable as:

1. **Guardrails** (section 7), mechanically audited.
   - **Allowed:** direct facts, counts at a moment, event intervals, comparisons and stated
     definitions.
   - **Never** as fact, unless licensed by a stated method or experiment:
     - "busiest = bottleneck";
     - busy counter as utilization;
     - "longest wait = problem";
     - per-machine queues for shared work;
     - single-run surplus;
     - per-change attribution;
     - mechanism claims;
     - "order is irrelevant".
2. **Pull-based inspection** of direct facts, by command:
   - the design;
   - finish time;
   - what every machine slot is doing at any tick;
   - which units wait for which step;
   - each unit's journey;
   - each machine's timeline.
3. **Named characterizations with stated definitions:**
   - **machine slot state:** working or idle, a direct fact;
   - **flow characterization** of an idle slot: starved or no work left, derived from the whole
     system ("blocked" is reserved for when finite buffers exist);
   - **utilization over a stated period.**
4. **Experiments for counterfactual questions** ("is this machine needed?", "where does capacity
   help?"), answered by comparing pre-computed variants. This is never inferred from one run.
5. **A mission pattern for learning:** a goal with an explicit answer format → exploration → recorded
   answer → official answer, explanation, how to find it, and what the mission teaches.

Evidence:

- The guardrails passed six mechanical audits on a 13-design, 24-pair falsification corpus. A naive
  control fails five of them.
- The third owner session completed 13 missions over four scenarios, with 17 of 19 checked answer
  parts correct. The owner judged the presentation good and the design explorable. Learning in the
  moment was observed: after one official answer the owner adopted the experiment path unprompted.

**Reversals of revision 1:**

| Revision 1 said | Revision 3 says | Why |
|---|---|---|
| No concrete reusable-measurement demand | **A demand exists:** utilization over a period and the flow characterization (starvation), with exact basis choices (section 9); bottleneck by a named method is pending | Revision 1's "no demand" came from a contract designed to refuse. The owner's educational goal needs these measurements, and a truthful definition exists |
| "The game must keep" event history, identity and change sets | These are **platform requirements**: runtime, Factory Design, model comparison (section 10) | The game is a presentation client. These are not game concepts (owner) |
| Explicit refusals shown to the player | Refusals are **guardrails**, not UI. The player is offered the action that answers the question (a variant), or the claim is simply not made | A "what this run cannot tell you" section confused the owner twice |
| The minimum contract is a statement list | A statement list is not a usable presentation. Pull-based inspection plus missions is | First-pass partial failure; third-pass success |

**Challenge: is another concept iteration (bottleneck) needed before concluding? No** (section 13).
The presentation pattern, including experiment-based questions, is already exercised. Bottleneck's
truth constraints are already established. What remains for bottleneck is a **named-method
definition**, which belongs to the analytics research, and a **teaching design**, which belongs to a
follow-up curriculum or controlled-retry question.

**Confidence:**

- **High** for the guardrails and the falsifications.
- **Medium** for the presentation pattern: one owner, a text prototype, two concepts.
- **Medium** for the measurement definitions as *requirements*. Their final form is the analytics
  research's call.
- **Low** for anything beyond one product, linear routing, release at once, all machines online, and no
  setup or transfer.

**Now unblocked** (section 15):

1. the independent adversarial review of this revision;
2. the reopened analytics-ownership research, which now has a concrete bounded use;
3. reconciliation items: brief and register updates, busy-counter removal, and the consumer-plan
   requirement set.

## 5. How the investigation evolved

| Iteration | What was tested | Result |
|---|---|---|
| **1. Statement contract** (revision 1) | Five information contracts plus a naive control, with six mechanical audits; blinded owner reading of a statement list (16 items) | C3b (facts plus explicit refusals) passed all audits. Owner: comparisons 8 of 8; inferred the limiting step from the longest wait; "starved/surplus" not understood; a refused interval misread; the protocol was "very hard" |
| **2. Revised wording, scripted MC walkthrough** | C3c ("plain bundle", which also passes all six audits); fresh pre-registered scenarios; terminal session | Paused by the owner after the tutorial. It surfaced the scope problems: verbosity, push versus pull, refusals as UI, trap questions instead of goals, the need for UI, the vocabulary goal. One defect found in generation: removing a machine renumbered later machines and produced a false second change |
| **3. Bakery inspection missions** | Two-layer state model; utilization; pull-based viewer; 13 missions with learning objectives; pre-registered answers | 17 of 19 parts correct; presentation judged good; learning in the moment observed; defects queued (section 8) |

## 6. Repository evidence (condensed; full detail in revision 1)

**Supported surface:**

- `RuntimeObservation`: resources (state, concurrency, active jobs, own queue depth, `busyTicks`),
  orders, jobs (status, current step, ordinal, created/completed times), pending multi-eligible work,
  performance.
- Metadata: `currentTime`, `latestEventSequence`.
- Supported events: `ORDER_ACCEPTED`, `JOB_DISPATCHED`, `JOB_STEP_COMPLETED`, `ORDER_COMPLETED`.
- Events are delivered once and not retained. The runtime contract leaves retention ownership to be
  defined when a consumer needs it.

**Selection and waiting:**

- Engine semantics §2: acceptance, then `combinedQueueDepth` (internal, not observable), then a
  `MachineId` tie-break.
- Pre-binding multi-eligible work is never a per-machine queue (§2 rule 7).
- Machine availability is a runtime command at a moment, not part of the design.
- An order's units are all released when it is accepted (§3).

**The busy counter (`busyTicks`):**

- It is credited only when a step finishes.
- It was introduced in a 2026-09-02 headless-acceptance change to make "the active production
  bottleneck identifiable … by utilization".
- That change's acceptance test encodes the "most utilized = bottleneck" heuristic this corpus
  falsifies.
- It is specified as "`busyTicks` / utilization" in Engine semantics §10–§10.2, and listed as
  "utilization facts" in the runtime contract.
- No product consumer uses it.

**Product boundary:** presentation is game-owned. Reusable derived measurement (utilization or
occupancy intervals, diagnosis, run comparison) is **unresolved, and not game-owned by default**
(consumer plan §4–§5).

**Concluded strategy-space reference:** its qualifications are preserved here:

- occupancy is not a constraint definition;
- the most-occupied pool can be the wrong constraint;
- identity and order matter;
- mechanical truth is not comprehension.

## 7. Guardrails: the mechanically established truth constraints

**Corpus.** 13 designs and 24 pairs, all hand-derived before evaluation, from the strategy-space
reference plus tracked fixtures. They cover:

- a true constraint;
- capacity added at and away from the constraint;
- migration of the constraint;
- starvation versus surplus;
- multi-eligible waiting;
- a long unfinished step;
- concurrency greater than 1;
- confounded pairs, including a pure interaction.

**Audit results** (each candidate on all 37 items; reproduced at every rerun):

| Contract | Traceability | Reconstruction | Counterexample | Refusal | Controlled mutation | Terminology |
|---|---|---|---|---|---|---|
| C1 facts only | PASS | PASS | PASS | FAIL (5) | PASS | PASS |
| C2a named pool occupancy | PASS | PASS | FAIL (2) | FAIL (7) | PASS | FAIL (12) |
| C2b named completion chain | PASS | PASS | PASS | FAIL (5) | PASS | PASS |
| C3a bundle + completion chain | PASS | PASS | PASS | PASS | PASS | PASS |
| C3b minimal bundle | PASS | PASS | PASS | PASS | PASS | PASS |
| C3c plain bundle (revised wording) | PASS | PASS | PASS | PASS | PASS | PASS |
| N naive control | FAIL (104) | PASS | FAIL (22) | FAIL (13) | FAIL (15) | FAIL (305) |

**Never present as fact without a licensing method or experiment.** Each row was falsified on a
concrete case:

| Claim | Counterexample |
|---|---|
| Busiest step or machine = bottleneck | G5: the CUT pool is the most occupied, yet adding a cutter does nothing; G7: tied occupancy, and no single addition helps |
| Busy counter ÷ elapsed = utilization | G4: 4 credited ticks while 2 of 2 slots are busy; 48 against 45 elapsed at completion. G11b: 36 against 26 |
| Longest wait = the problem | Under release at once, the first step's backlog is the largest wait in every corpus design (it is the constraint only in G3/G4) |
| Shared waiting = one machine's queue | G6, G11a: per-machine counts double-count |
| An idle machine is surplus | G2 against G8: the same single-run evidence, but removal costs 11 ticks in one and 0 in the other |
| Two simultaneous changes: "this one did it" | G1 + A + I: attribution depends on order; G7 + A + I: pure interaction |
| "Because…" after one change | Determinism reproduces an association, not a mechanism |
| Machine order is irrelevant | G9: order alone 11 → 9 |

**Allowed:**

- direct facts;
- counts at one observation;
- intervals between supported events;
- one-change comparisons (change and outcome);
- multi-change comparisons with attribution withheld;
- stated definitions (section 9).

**Disclosed method refinements:**

- *Revision 1:* the idle derivation was given a name; the controlled-mutation sub-check was corrected,
  and C3b and C3c do not depend on it; wording fixes.
- *Second pass:* one scenario reauthored so a removal does not renumber another machine. No values
  changed; recorded as an amendment.

## 8. Presentation findings (owner sessions)

**What works** (third session):

- **Pull, not push.** A minimal design, plus commands for facts (`design`, `result`, `at <tick>`,
  `machine`, `util`, `loaf`, `variants`, `switch`, `compare`, `explain`). Nothing is pre-answered.
- **Missions with learning objectives.** Each mission has one goal with an explicit answer format, and
  missions grow in complexity on one scenario before the next. After answering, the player sees the
  official answer, why, how to find it, and what the mission teaches.
- **Experiments by variant.** In-the-moment learning was observed. In mission 10 the owner answered
  correctly through an untested utilization heuristic; the official answer pointed to `compare`, and
  the owner used it unprompted in missions 11 and 13.
- **Plain text, in context.** The owner judged it "fairly easy" and "good" for showing that a design
  can be explored. For a game it would need less given away.

**What fails** (all three sessions):

- **Statement lists** (a wall of facts plus refusals) need pen and paper.
- **Refusal sections as UI** ("what this run cannot tell you") confused the owner twice.
- **"This shows what happened … not why"** confused the owner twice.
- **Trap questions** test caution, not reasoning, and penalize forming a hypothesis.
- **Undefined terms** ("starved", "surplus"): the owner wants industry jargon, with each term taught.
- **"idle (no work left)"** was read as "idle = no work left". Lead with the characterization, and
  accept equivalent answers.
- **Combined changes** were missed: the owner compared every single variant but not the joint one.
- **Correct answers can hide invalid heuristics.** Record "how did you decide?" on reasoning missions.
- **Tick ranges** are half-open, like clock hours. The owner prefers teaching that gently in the first
  missions over annotating every range.
- **Interaction defects:** inline `answer <text>`, natural range syntax, typo suggestions, and prompt
  editing.

## 9. Concrete reusable-measurement demand (revised)

**Classification:**

| Requirement | Classification |
|---|---|
| Finish time; units finished; machine slot state (working or idle); units waiting by step (resource only for a single-eligible step); design change set | **Direct supported fact**, or game-owned design fact |
| Each unit's journey (wait and work per step); each machine's timeline | Exact readings of supported events or per-tick observations: **game-local presentation**, given a platform source of per-step timing (section 10) |
| **Utilization over a period** | **Concrete reusable measurement candidate** |
| **Flow characterization of idle slots** (starved / no work left; blocked later) and starved time over a period | **Concrete reusable measurement candidate** |
| Bottleneck / constraint | **Candidate pending a named method.** Single-run heuristics are falsified. Intervention by variant is valid. The completion chain held on every unique corpus case, but has no general guarantee |
| "Needed" / excess capacity | Answered by experiment: a comparison of direct finish facts, which needs no measurement |
| Busy counter, or any `busyTicks`-based figure | **Unsupported**; to be removed (section 11) |
| Per-machine queue for shared work; per-change attribution; mechanism | **Unsupported, must not be claimed** |

### Utilization over a period

| Item | Content |
|---|---|
| Player/product purpose | Let a player see and learn how much of its available time each machine spent working over a chosen period; compare machines; see start-up and wind-down effects |
| Measurement question | Working slot-ticks ÷ (slots × period length) for a machine over a half-open period [a, b) |
| Inputs | Published model (concurrency); machine slot occupancy at every tick of the period, from per-tick observations or from complete dispatch/completion intervals |
| Basis choices | Half-open period; per slot (concurrency in the denominator); work in progress counted up to the boundary; per machine by default, with a per-eligible-step pool as a separate figure (they can rank differently); offline time excluded from available time (specified, not exercised) |
| Completeness | Occupancy known for every tick of the period |
| Refusal | Period not fully covered; offline handling not specified for the consumer; never presented as a bottleneck verdict |
| Reuse need | The same meaning is needed for game teaching, any future dashboard or analysis, and Factory Design's "Maximum utilization" verification objective. Removing the busy counter leaves no supported utilization |
| Ownership | **Unresolved:** hand to the simulation-analytics ownership research |

### Flow characterization of idle slots (starvation)

| Item | Content |
|---|---|
| Player/product purpose | Explain *why* a machine is idle; teach starved versus no work left (blocked later); starved time over a period |
| Measurement question | For an idle slot at a tick: does some unfinished unit still need a step this machine can do (starved) or not (no work left)? |
| Inputs | Published model (routing, eligibility); one observation (job status and current step, machine active jobs) |
| Basis choices | System-wide at the tick. A queued unit counts from its current step; an in-progress unit counts from its next step. An idle slot with a queued unit waiting for it is a contradiction under the current Engine, and is rejected. Aggregation is starved slot-ticks over a period |
| Completeness | One observation per tick (or per state change) |
| Refusal | Offline machines (not specified); "blocked" until finite buffers exist; workloads with orders arriving later need an explicit rule (known work only) before "no work left" is claimed |
| Reuse need | Equipment idle-reason coding is general across consumers. (Background, not verified against a specific standard: industry equipment-state models attach reasons such as "no material" to idle time) |
| Ownership | **Unresolved:** hand to the analytics research |

The research-local definitions (`machine-slot-state`, `flow-characterization-of-idle-slots`,
`utilization-over-a-period`) are executable in the workspace experiment and prove feasibility only.

## 10. Platform requirements (corrected ownership)

| Need | Owner | State |
|---|---|---|
| Per-step timing for unit journeys, machine timelines and utilization | **Runtime / analytics** | Two routes exist. (a) The runtime retains supported events or adds timing to observations. (b) A consumer records an observation after every advance; the bakery generator did exactly this through bounded advancement. Which route, and who owns recorded history, is a runtime and analytics decision |
| Time labels from the simulation clock | Engine (an existing fact) | A usage rule only |
| Machine identity stable when machines are added or removed | **Factory Design** (model identity) and draft projection | Positional identity renumbers later machines, and an honest change set then reports an extra change (found in the second pass) |
| What changed between two designs, including order | **Factory Design / model comparison** | A related register question is a CANDIDATE |

## 11. The busy counter: owner decision

**Remove it** as part of this research's reconciliation. The code removal is deferred to the first
reconciliation commit, because removing it earlier would rewrite this research's own counterexample
evidence and detach experiments from `main`.

**Scope:**

- the `ResourceObservation.busyTicks` field and its accumulation;
- the Engine semantics §10.1 rule 1 and the §10.2 register row (the lead-time saturation rule stays);
- the bottleneck-by-utilization assertion in `HeadlessClosureAcceptanceTest`;
- the busy-counter part of `EngineDerivedResultConformanceTest`;
- the runtime contract's "utilization facts", the ISA-95 mapping row and the Governance evidence
  mention.

**Risk: high.** It changes a supported observation contract and an Engine rule, so it needs
independent review. Its replacement is section 9's utilization candidate, owned wherever the analytics
research decides.

## 12. Requirement set recommended for the game consumer plan

To be promoted only after independent review and reconciliation:

1. **Guardrails:** section 7, in full, as constraints on every claim the game makes.
2. **Inspection capabilities:** design; finish; slot state at any tick; waiting by step (step first;
   resource named only for a single-eligible step; never derived from `combinedQueueDepth`); unit
   journeys; machine timelines; comparisons of variants.
3. **Characterizations:** flow characterization and utilization *as defined by their eventual owner*,
   shown with their names, definitions and periods, never as verdicts.
4. **Experiments:**
   - counterfactual questions are answered by comparing variants;
   - combined changes are presented as a distinct kind of experiment, with attribution withheld;
   - change sets include machine order, and machine identity stays stable.
5. **Teaching pattern:**
   - missions with learning objectives, explicit answer formats and increasing complexity;
   - the official answer, why, how to find it and what it teaches, right after each answer;
   - jargon introduced in context, plus an on-demand reference;
   - conventions (tick ranges) taught gently in the first missions.
6. **Labels:** time labels from the simulation's own clock.
7. **Scope:** non-spatial, one product, linear routing, release at once, all machines online. Anything
   else needs revalidation.

## 13. Challenge: do we need to exercise the next concept (bottleneck) here?

**Arguments for:** bottleneck is the central diagnostic concept for this game. The brief's main
falsification target was constraint claims. A full presentation iteration has covered only starvation
and utilization.

**Arguments against, which prevail:**

- **The truth side is done.** Iteration 1 established bottleneck's guardrails mechanically, including
  intervention ground truth, migration, co-binding, and the falsified single-run heuristics.
- **The pattern is already exercised.** Experiment-based questions ran in the third session (S3, and
  S4's co-binding mission). Its failure mode, missing the joint variant, is already recorded.
- **What remains is not this question's.** It is (a) choosing a **named method** for a single-run
  bottleneck explanation, which is a measurement definition and belongs to the analytics research,
  with corpus evidence ready for it; and (b) a **teaching design** for bottleneck (curriculum,
  difficulty), which is game product design. A follow-up curriculum question or the existing
  controlled-retry candidate fits it.
- **Scope has already grown well past the brief.** Further iterations would turn this question into
  the game's curriculum programme.

**Verdict:** conclude here, provisionally. Hand bottleneck's named method to the analytics research,
and its teaching to a follow-up question.

**Reconsider** if the analytics research chooses a single-run method whose presentation raises new
truthfulness risks. That would be a short, targeted extension of the guardrails, not a new iteration.

## 14. Adversarial analysis (self-administered; not independent)

1. **Shared authorship.** One author built the candidates, audits, scenarios, generators, viewer and
   this report.
   - Mitigations: answers were pre-registered and committed before any code, in every iteration; the
     audits caught the naive control; the runs matched every pre-registered value.
   - Residual: an independent reviewer should try to craft a misleading statement that passes the
     audits, and a mission whose official answer is wrong.
2. **Owner as co-designer and sole subject.** In the third pass the owner co-designed the scenarios and
   missions (but not the key), and had seen the first pass's key. The second pass was paused before
   its reveal. Their success is weaker evidence than a
   blinded naive player's; their failures are still falsifications. The asymmetry stated in the brief
   holds.
3. **The viewer gives too much away.** Labeled states and breakdown tables make several missions
   lookups. The owner judged that acceptable for proving explorability, not for gameplay difficulty.
   So "usable" here means *explorable and understandable*, not *challenging*.
4. **Measurement definitions are research-local and convention-laden.** Examples: half-open periods,
   per slot, and "known work only" for no-work-left. Another owner may choose differently. These are
   requirements with stated choices, not settled formulas.
5. **Narrow scope.** One product, linear routing, release at once, no offline machines, no setup or
   transfer. Flow characterization in particular needs a rule for future arrivals.
6. **The reversal of revision 1's analytics conclusion follows the owner's goal, not new mechanical
   evidence.** The demand exists *given* the educational goal. If that goal changes, the demand should
   be re-examined.
7. **The decision not to iterate on bottleneck is a judgement** (section 13), open to challenge.

## 15. What this unblocks, and what stays blocked

**Unblocked now:**

1. **Independent adversarial review** of this revision. It is required (High risk) before any
   promotion.
2. **The simulation-analytics ownership research (REOPEN).** It asked for a concrete, bounded use;
   section 9 supplies two, with basis choices, plus bottleneck's pending named method and a corpus to
   test it.
3. **Reconciliation preparation:**
   - **Brief** (proposed, for the owner to decide): adopt the refined question and goals; redefine the
     owner gate as a smoke test of a presentation prototype rather than of a statement list.
   - **Register:** record the verdict when concluded, and the new candidate questions below.
   - **Busy-counter removal:** architecture and code, reviewed.
   - **Consumer plan:** the requirement set (section 12).
4. **The controlled-retry candidate question:** its precondition (an evidence contract) is now supplied,
   along with concrete comparison-presentation findings.

**Still blocked:** a playable implementation. It needs:

- the live interactive loop decision;
- the analytics ownership decision;
- the per-step timing route (section 10);
- the Engine prerequisites already listed in planning.

## 16. Concept coverage and tracking destinations

| Item | Destination at reconciliation |
|---|---|
| Starvation, utilization | Concluded here (requirement set and measurement candidates) |
| Bottleneck (named method), shifting bottleneck, excess capacity, co-binding | Named method: analytics research. Teaching: follow-up curriculum or controlled-retry question |
| Explorable now but not exercised: queue/WIP, lead time, makespan, step time, parallel/multi-slot capacity, pooling, tie-break effects, release-at-once backlog | New **concept-curriculum** research question (game product programme) |
| Engine-supported, needing new scenarios: throughput, Little's Law, arrival/release patterns, demand versus capacity, machine downtime, backlog, multiple products | Same new question, or a sibling |
| **Order release policy as a design concept** (a schedule, every N ticks, a work-in-process cap) | New register CANDIDATE: today, orders over time come only from commands |
| Blocking / finite buffers | New register CANDIDATE (buffers are deferred in Factory resource semantics) |
| Variability (processing time, failures, arrivals, yield) | Engine evolution's stochastic boundary; the game's educational need is a candidate trigger |
| Setup/changeover; transfer/layout; lots/batches | Already tracked (register / spatial planning) |
| Scrap/rework, priorities and other dispatch rules | Programme roadmap |
| **Live simulation loop / interactive harness** | New research or planning item. The owner has stated it is necessary soon |

The game product research programme
(`docs/research/investigations/factory-design-game-vertical-slice.md`) is the natural single home for
the full concept map.

## 17. Surviving invariants

- Every player-visible claim is a direct fact, a count at a moment, an event interval, a comparison, or
  a value of a stated definition. No claim is stronger than its evidence.
- Machine state is not flow characterization. State is read; characterization is derived from the
  whole system.
- Counterfactual questions are answered by experiment, never by a single-run label.
- Pre-binding shared waiting belongs to the step, not to a machine.
- A completion-credited counter is not utilization.
- Identity and order are part of a design change.

## 18. What did not survive

- The first report's "no reusable measurement demand".
- The first report's game-owned "must keep" list.
- Refusals as UI.
- The statement list as a presentation.
- Trap questions as walkthrough tasks.
- Avoiding industry vocabulary.
- The completion-chain line for players (confusing).
- "Surplus" from one run.
- Annotating every range with its length (owner correction).
- The busy counter.

Each is reusable negative knowledge for the game and analytics work.

## 19. Unresolved unknowns

- Bottleneck's named method, and how to teach it.
- How per-step timing reaches a consumer.
- Where the measurement definitions live.
- Behavior with orders arriving over time, offline machines and multiple products.
- Comprehension by real players (the deferred external-player question).
- Difficulty design for actual play.

## 20. Durable consequences (none performed here)

- **Brief and register:**
  - the proposed refined question and goals, and the proposed owner-gate redefinition, if the owner
    adopts them;
  - verdict on conclusion;
  - new candidates: concept curriculum, release policy, blocking/finite buffers, live interactive
    loop.
- **Architecture and code:** busy-counter removal (section 11), independently reviewed.
- **Planning:** the consumer-plan requirement set (section 12) and the platform requirements
  (section 10).
- **Analytics research:** the section 9 tables, the bottleneck corpus and the falsified heuristics.
- **Reusable assets worth preserving if a later slice implements diagnostics:**
  - the G-corpus counterexamples;
  - the bakery scenarios and missions;
  - the audit implementations;
  - the viewer pattern.

  Otherwise they are discarded explicitly at the knowledge-transfer audit.
- **This report:** it need not remain readable after reconciliation.

## 21. Implementation implication

No product implementation is admitted by this report. The only product change it recommends is the
owner-decided busy-counter removal, through reconciliation.

## 22. Sources

- Roser, C., Nakano, M., Tanaka, M. *Throughput Sensitivity Analysis Using a Single Simulation*.
  Proceedings of the 2002 Winter Simulation Conference, pp. 1087–1094, DOI 10.1109/WSC.2002.1166361.
  - The abstract's validity condition, prediction valid only "provided that the system change does not
    significantly change the bottleneck", was checked against the author's preprint page.
  - Its §2.2 active-period ambiguity is background; verify before citing.
  - **Analogy limit:** that method targets steady-state lines with finite buffers. This corpus is one
    finite order with unbounded queues.
- Industry equipment-state models (idle reasons such as "no material"): **background, not verified**
  in this investigation.

## 23. Evidence coordinates (active custody)

Workspace branch: `workspace/factory-design-game-diagnostic-evidence`.

| Artifact | Commit | Path |
|---|---|---|
| Handoff | `a6081fbac9d901a1d2eb2911ef78eb49ed5cff79` | `workspace/research/handoffs/factory-design-game-diagnostic-evidence.md` |
| First-pass oracle (pre-registered) | `010b84a4e38113766af2cd594135825296a0ef3a` | `workspace/research/investigations/factory-design-game-diagnostic-evidence-oracle.md` |
| First-pass experiment and results | `f8a2be5bd2d5dfe0e2e9ffb0bfd7e9d8dc950374` | `workspace/research/experiments/game-diagnostic-evidence/`, `workspace/research/investigations/game-diagnostic-evidence-results/` |
| Report revisions 1 and 2 | `bb37e2598d2684f7dc47b6db75884b798a0ae040`, `e5debf2584b2a3096115c2ed8b117e2ac653cb31` | this path |
| First walkthrough record | `e5debf2584b2a3096115c2ed8b117e2ac653cb31` | `workspace/research/investigations/factory-design-game-diagnostic-evidence-walkthrough-record.md` |
| Second-pass oracle and protocol (pre-registered) | `de2c28a86cd0c03f1f6ffe022bc5dde1d271b7f8` | `workspace/research/investigations/game-diagnostic-walkthrough-2-*.md` |
| C3c contract, second-pass pack and script; oracle amendment | `c4c944da99933ab86cab9482441b43514e145057` | `workspace/research/experiments/…/GamePlainContract.java`, `GameWalkthroughPack.java`, `workspace/research/experiments/game-diagnostic-walkthrough/` |
| Discovery notes | `c7106ab6f2370962a3d4ed30f706b0b229dc1ca2` and later commits | `workspace/research/investigations/factory-design-game-diagnostic-evidence-discovery-notes.md` |
| Missions and missions oracle (pre-registered) | `051110921b051e907781c76046d05df15274b070` | `workspace/research/investigations/game-diagnostic-inspection-missions*.md` |
| Bakery generator, data pack and viewer | `82786d94b3ae68426b07551589be455a7ed6b5e9` | `…/BakeryInspectionPack.java`, `workspace/research/investigations/game-inspection/`, `workspace/research/experiments/game-diagnostic-inspection/inspect.mjs` |
| Third-session record | `9922ead0c051fc26b126cc464211775d75d04a4a` (and the commit adding this revision) | `workspace/research/investigations/game-inspection-walkthrough-record.md` |
| This report (revision 3) | the commit that adds it | this path |

**Rerun:** from `product/` on JDK 21:

```text
./gradlew :research-experiments:test \
  -PresearchExperimentSources=<repo>/workspace/research/experiments/game-diagnostic-evidence
```

Copy `build/game-diagnostic-evidence/`, `build/game-diagnostic-walkthrough/` and `build/game-inspection/`
over their workspace counterparts.
