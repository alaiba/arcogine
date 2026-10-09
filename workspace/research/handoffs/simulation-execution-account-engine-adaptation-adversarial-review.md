# Independent adversarial-review handoff: Engine adaptation and Game admission

## Mission and review boundary

Act as a **fresh, genuinely independent Researcher in adversarial-review mode**. Follow the live repository's `AGENTS.md`, `.github/agents/researcher.agent.md` and `docs/development/researching.md` (in particular its high-risk research, anti-anchoring, review, and evidence-custody rules). Use **research-review** dispositions only: `ACCEPT`, `ACCEPT WITH QUALIFICATIONS`, `MORE EVIDENCE REQUIRED`, or `REOPEN`. Do not substitute the PR-review disposition vocabulary.

**Review exactly this report revision:**

- Repository: `alaiba/arcogine`.
- Workspace branch: `workspace/simulation-execution-account`.
- Report commit: `9551c0cee42bfdeab07f4a55f931eb10ec5fba4e`.
- Report path: `workspace/research/investigations/simulation-execution-account-engine-adaptation-report.md`.
- Report's main baseline and final recheck: `912b2ac85cefab3907fc562a3aaa6507e51975a7`.
- Report pre-registration: `f3975916c925fe6e465203e700e171d0d1a57c7a`, `workspace/research/investigations/simulation-execution-account-engine-adaptation-framing.md`.
- New experiments: `f74c4ad7d9e0aa54743a17bf575699a0a5eeab92`, `workspace/research/experiments/simulation-execution-account-engine-adaptation/com/arcogine/research/executionaccount/adaptation/`.

First resolve the complete report by exact SHA and path. Verify identity and completeness from its header without deeply reading its argument. If unavailable, stop as `INPUT BLOCKED - ORIGINAL REPORT NOT AVAILABLE` and do not issue a disposition inferred from this handoff.

Review this report, **not** the earlier execution-account report as a whole. Its original report (`a53778145f3ee9941a4b46c7f04f1ec17e6bff86`) already received a separate independent review (`57d87fc4c8ba19927bf50efa4097321208811d5a`, `ACCEPT WITH QUALIFICATIONS`). Preserve those immutable coordinates. Reopen an earlier settled finding only on a specific new counterexample that invalidates something load-bearing, not as a generic renewed debate over E1-E8, analytics formulas, event sourcing, or custody.

The objective is to decide whether the **new report's load-bearing conclusions survive**: (1) a marker/count-budget-induced command-time semantic defect; (2) an appropriate remedy, especially its preferred `E-guard`; (3) retention of controller-managed interval closure for current needs rather than Engine-enforced finality; and (4) a credible Game-facing admission/reopening trigger. The reviewer must be free to uphold, qualify, reject, or request more evidence for each. Neither "change the Engine now" nor "leave it alone" is the predetermined verdict.

## Independence and anti-anchoring

State the actual independence condition (fresh researcher/session/model family as known, previous exposure, responsibility for the source report). A self-review is not an independent review. Sharing the research workspace for artifact custody is permitted and does not by itself prove or disprove independence.

After the minimal report-availability check, and **before reading its recommendation in depth**:

1. Resolve live `main` and record its exact SHA, compare material drift against `912b2ac85cefab3907fc562a3aaa6507e51975a7`, and read the live rules in `AGENTS.md`, `docs/development/researching.md`, `.github/agents/researcher.agent.md` and `docs/development/testing.md`.
2. Read the maintained execution-account brief and row (`docs/research/investigations/simulation-execution-account.md`, `docs/research/research-register.md`), plus the current Engine session/advancement rules, runtime event/observation contract, and actual `FactoryRuntime`, `RecordingScheduler`, `FactoryHandler`, handler/scheduler markers, and session-control acceptance tests.
3. Independently reconstruct what can and cannot be observed about *internal scheduler time*, the time at which a subsequent command executes, internal event count, supported changes, run state and interval finality. Derive at least two marker/command-time counterexamples or boundary cases and compare plausible remedies **without** using the report's chosen solution as the organizing assumption.
4. Independently inspect the **Game context** below and separate the research prototype's actual exercised control from the admitted/intended product control requirements. Reconstruct whether there is a concrete present or near-admission consumer needing changed Engine semantics.
5. Persist a short **pre-review independent reconstruction** at `workspace/research/investigations/simulation-execution-account-engine-adaptation-review-reconstruction.md` (distinct from the original report, the original pre-registration, and the first independent review). Commit it before consuming the detailed recommendation and experiment conclusions; record its commit coordinate.
6. Only then read the report and its pre-registration/probes at their exact revisions, and attempt to falsify each load-bearing claim.

Ground source facts by exact live-main revision; search hits are only discovery. At completion recheck live main and report any material changes since the review began.

## Mandatory Game evidence: distinguish implemented, researched, planned, and future

The author discusses the static Game study. **Check it directly, rather than accepting either "no Game consumer" or "Game already needs a live Engine clock".** These are different propositions.

At minimum inspect:

- On live `main`: `docs/planning/factory-design-game-consumer.md` (especially simulation control, diagnostic presentation, and playable admission); `docs/planning/factory-design-game-vertical-slice.md`; `docs/planning/factory-design-game-challenge-readiness.md`; `docs/research/investigations/factory-design-game-vertical-slice.md`; `docs/research/investigations/factory-design-game-diagnostic-evidence.md`; the corresponding research-register rows; `docs/research/investigations/engine-evolution.md` (session/advancement note).
- At its immutable revision: Game diagnostic evidence report `4ad00136441fa29e6b462ac5431b60cf2de1e148`, `workspace/research/investigations/factory-design-game-diagnostic-evidence-report.md`.
- Game's independent review `6484795ff4c4ad971e06c70751b03805e54a55f5`, `workspace/research/investigations/factory-design-game-diagnostic-evidence-adversarial-review.md`.
- If required to discriminate, the actual text viewer/prototype and generated recorded-run evidence under branch `workspace/factory-design-game-diagnostic-evidence`, such as `workspace/research/experiments/game-diagnostic-inspection/inspect.mjs` and `workspace/research/experiments/game-diagnostic-walkthrough/walkthrough.mjs`.

The Game's diagnostic report deliberately tested **static recorded runs** and classified a live simulation loop as future work. The consumer plan nonetheless specifies bounded advancement/reset and presentation-speed control for the eventual playable integration. Do these statements coexist consistently? Does the planned presentation/control model actually require commands interleaved during a continuing run, a chosen simulated-time clock, or independently final interval readings? Or can a first slice show precomputed runs or live event stepping without either need? Be precise about what is *tested*, *admitted*, *planned*, and merely *possible*.

Evaluate whether the adaptation report's statement that the research runner is the only implemented driver makes its **present-tense no-Engine-change** conclusion defensible, and whether it mistakenly delays reconsideration until *after* Game implementation. A good reopening trigger fires when relevant playable **requirements are selected or implementation admitted**, before writing the consumer loop. Do not turn every future Game concept into an immediate Engine requirement.

## Focal attack 1: marker / command-time finding

Independently reproduce or falsify the report's D1/D2 mechanism:

- A positive finite `advanceUntil(target, maxEvents)` budget can consume an internal `TaskStart` / other marker alone. Supported observation/events remain unchanged.
- The scheduler's internal command time can nevertheless advance, so a subsequent accepted workload/availability command lands at a different simulated time and changes a supported result.
- The report claims this conflicts with the Engine specification's assertion that internal markers have no semantic significance. Distinguish a genuine specification contradiction from permitted internal timing, or from a supported but currently unobservable interpretation that ought to be documented.

Trace the exact logic in current source and the semantics of `advance()`, `advanceUntil()`, `RecordingScheduler.nextEvent()/peekTime()` and command acceptance. Verify which marker paths actually occur, what budgets are affected, whether a marker at a *different* time is necessary, whether the supported observations are equal at the critical instant (apart from RunId), and what reproducibility guarantee an identical **public** command/advancement script should have.

Demand a minimal independently reasoned counterexample, not only acceptance of the author's D2 assertion. Check the original tests, their expectations and any omitted boundary variants. If the defect stands, say exactly which authority paragraph and semantic invariant are inconsistent and whether it must be corrected before execution-account E6 reconciliation or could be addressed under a separate bounded owner decision.

## Focal attack 2: `E-guard` and credible competing corrections

Review the proposed **authoritative-turn counting** remedy as an *Engine semantics change*, not a preapproved implementation fix. Compare at least:

- **E-guard**, as the author sketches it: process leading non-authoritative markers then one authoritative event, counting authoritative turns; claim latest supported time equals command time at return.
- **E-record**, the behavior-preserving alternative that makes marker placement/counting and internal-time consequences part of the supported interpretation.
- Any more narrowly scoped fix you derive that genuinely preserves desired invariants (for example an explicitly different advancement operation or a command-time exposure/definition), without turning a style preference into a new abstraction.

Attack E-guard's *exact API consistency*: `advance()` currently advances **one scheduler Event** and returns `Optional<Event>`; `advanceUntil()` promises the same order and returns the internal events processed. Does the proposal actually preserve these contracts while changing the meaning of one budget unit? If it does not, identify the required signature/result/contract changes and migration consequences. Do not call an event-count-to-authoritative-turn change "no new public type" and thereby imply compatibility if observable return values or progression steps change.

Required edges: marker-only tail; leading marker at a different time; time guard with next authoritative event beyond target; same-tick events and deterministic order; budget zero/exact budget exhaustion; exhausted queue; dispatch/recovery/backlog and `OrderCompleted`; fault during a turn and surviving emitted changes; reset; equal-time command interleavings; conformance across both `advance()` loops and bounded calls; future spatial/transfer markers as a *bounded compatibility risk*, not an unexecuted factual assertion. Consider whether `T_int == T_obs` truly holds after **every** successful return, including fault/marker-only/no-op paths, and whether supported observed time is the right contract to expose command time.

If the current defect is real but E-guard is unproven or overly disruptive, separate **the defect finding** from **remedy selection**. The owner should not be forced to select E-guard merely because E-record is unattractive.

## Focal attack 3: no Engine closure API now, and the alternative's actual benefit

The report prefers controller-managed closure for implemented/closed-script consumers; its research-local holder/attestation library binds the proof sequence and avoids the stale-frontier mistake. Check that the evidence establishes this **bounded** result; do not redo the entire original E1-E8 investigation.

Probe whether B (advancement outcome), C (atomic batch/drain/observation), a protected cursor/witness, D-clock (explicit simulated-time advance plus supported event), or a smaller hybrid could eliminate a **material present** error or repeated coordination obligation that A cannot prevent. Distinguish:

- feasibility through existing APIs versus suitability of the architectural owner;
- a controller's closed-script *confinement* versus a live continuing game session;
- events processed through `b`, supported delivery captured through sequence `S`, monotone producer time, and trusted promises about future inputs;
- producer-authored finality evidence versus consumer-held evidence completeness;
- passive consumers needing an independently verifiable claim versus an exclusive driver supplying its own attestation.

Verify the limited positive claims of the library using original cases and the actual source; report any new counterexample. Do not infer an already delivered Engine requirement from planned Game work, and do not infer architectural optimality merely because a library can enforce the protocol. For D-clock, test whether its supported `CLOCK_ADVANCED` witness and command-timing consequence are coherent and whether the choice is too strongly preselected as "the future shape" given that the live Game interaction contract is not selected.

**Decision requirement:** say whether "no Engine change now" is fully supported, only supported for static/confined consumers, requires qualification by a near-term Game admission gate, or is invalidated by a presently admitted consumer. Do not collapse this decision into the separate marker-remedy question.

## Required proof and verification

Inspect the exact new probe commit and pre-registration (11 new discriminators reported, 171-test suite reported). Treat those counts as the investigator's claims until rerun or independently inspected. Use repository-supported JDK 21 / research-experiment procedure when executable; label inability to execute and any unimplemented Engine alternatives. A test that reproduces an existing defect is not a test of a proposed fix. At least one **new** targeted adversarial case or hand-derived discriminator should attempt to falsify E-guard's strongest invariant or the no-change decision in light of the Game evidence. Persist executable probes under a distinct `workspace/research/experiments/...review/` path if useful, without altering the original tests.

For every material challenge, identify: (1) exact report claim and source location, (2) controlling specification/source or executed proof, (3) a minimal falsifier or survivor, (4) consequence for marker remedy, closure decision or Game admission, and (5) whether the report's main recommendation survives. Where evidence is insufficient, provide the smallest missing discriminator rather than inventing a broad research track.

## Consolidation requirements: preserve decisions and precise triggers

The outcome should reduce **parallel research sprawl**, not create more tracks. If "no closure API now" survives, give one or more **exact, observable triggers** to reconcile into existing maintained destinations, such as:

- `docs/planning/factory-design-game-consumer.md` and `docs/planning/factory-design-game-vertical-slice.md`: decide the Engine session-control/closure requirement **before** a live step/pause/resume/player-command loop is admitted for implementation, when the Game's selected interactions actually make it material;
- `docs/research/investigations/engine-evolution.md`: reopen the relevant bounded advancement/clock alternative when the selected consumer needs a simulated-time horizon while idle, mid-run commands at selected ticks, or final interval evidence independent of controller trust;
- `docs/architecture/runtime-contract.md` / `docs/architecture/engine-semantics.md`: the durable owner of any accepted coverage/time/marker invariants;
- the existing execution-account/analytics research linkage: do not let Game-local or analytics-local code create an unreviewed competing finality or derived-measurement contract.

The review should determine **which** of these triggers are justified, whether any already fired, and the exact decision/checkpoint at which to apply them; it should *not* update them itself. Do not create a standing follow-up Engine or Game research question merely to remember this conclusion. A material actual unmet requirement may justify an explicit bounded reopening through the existing research register, but name the evidence first.

Keep explicit the distinction between **(a) a present Engine-specification inconsistency requiring an owner decision**, **(b) a currently workable closure protocol**, and **(c) a future product-triggered decision about a stronger Engine guarantee**. Avoid treating all three as one yes/no vote on changing the Engine.

## Review output and stop boundary

Persist a **single decision-quality independent review** in the existing branch (unless operational isolation is genuinely required):

`workspace/research/investigations/simulation-execution-account-engine-adaptation-adversarial-review.md`

Include:

1. Reviewed report exact commit, path, research baseline, live-main review baseline/final recheck, and independence/anchoring chronology.
2. Independently reconstructed candidate boundaries and Game consumer status, before reading the target recommendation.
3. A finding on the **existence and scope** of the marker/command-time semantic problem.
4. A **separate** finding on E-guard validity, conformance/compatibility burden, E-record and any credible smaller remedy.
5. A **separate** finding on the scope and strength of **no Engine closure adaptation now**, with genuine consideration of a producer-side simplification.
6. A concrete and bounded Game admission/reopening decision: current static prototype versus planned bounded advancement versus *selected* live interactive loop, naming whether each actually triggers re-evaluation.
7. Exact probes run, expected-versus-observed distinctions and limitations; new falsifiers, if any.
8. Qualifiers that must survive, minimal owner choices, and a **consolidated reconciliation sequence** without a new parallel research programme.
9. One of the four permitted research-review dispositions for this report revision only.

Do **not** rewrite the original or new investigation reports, alter product code or canonical docs, adjudicate Game analytics or interface design, implement E-guard/D-clock, update the research register, perform reconciliation, open an implementation PR, or merge the research workspace. Independent review is the sole deliverable.

Preserve handed-off commits and artifacts as immutable. After committing the review, return **branch + exact review commit SHA + path**, separately identifying any pre-review reconstruction/probe commits; the reviewed report SHA, live-main baseline/final SHA, independence condition, final disposition, and concise findings for the three separate decisions (marker defect, E-guard remedy, closure now/Game trigger). Do not paste the full review artifact into chat.
