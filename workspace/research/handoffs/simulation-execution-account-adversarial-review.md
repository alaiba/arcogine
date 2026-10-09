# Independent adversarial-review handoff — Simulation execution account semantics

## Assignment and authority

Act as a **genuinely independent Researcher in adversarial-review mode**. Evaluate the existing,
completed simulation-execution-account investigation for falsification, not for polishing or defending
its recommendation.

This is **High-risk research**. Follow the live repository versions of `AGENTS.md`,
`.github/agents/researcher.agent.md`, and `docs/development/researching.md` (especially §2, §7,
§9, and §10). Use the four research-review outcomes, **not** PR-review dispositions:

- `ACCEPT`
- `ACCEPT WITH QUALIFICATIONS`
- `MORE EVIDENCE REQUIRED`
- `REOPEN`

A clean acceptance is legitimate. Do not manufacture a finding, and do not let a target of "find a
defect" become an assumption that the report must be wrong.

The independent reviewer should be a different researcher/person/model family where practical, or
at minimum a genuinely fresh isolated session with no obligation to preserve the original result.
The original investigator's self-challenge is **not** the independent review. State the actual
independence condition in the review artifact.

Do not execute this review in the same uninterrupted research run that authored the report.

## Exact review target and evidence custody

**Review this immutable report revision only:**

- workspace branch: `workspace/simulation-execution-account`
- report commit: `a53778145f3ee9941a4b46c7f04f1ec17e6bff86`
- report path:
  `workspace/research/investigations/simulation-execution-account-report.md`
- report's stated research baseline and final main recheck:
  `79b3399149499f2f337206c909a277f66a5befd5`

The complete report is mandatory input. Before report-specific work, confirm that the exact
commit+path resolves and that its full content is available. At this stage inspect only enough
metadata/header to verify identity and completeness, not its recommendation.

If it cannot be resolved, stop and return:
`INPUT BLOCKED — ORIGINAL REPORT NOT AVAILABLE`.
Do not infer the report from this handoff and do not issue a disposition on a missing report.

The live `main` SHA when this review handoff was prepared was
`912b2ac85cefab3907fc562a3aaa6507e51975a7`. **Resolve live main afresh** at review
start and again before completion; record the live-main review baseline and any material changes
from the report's baseline. The main SHA above is context, not permission to skip the fresh check.

Use the **existing** workspace for review custody, without overwriting or amending the handed-off
report or its original commits.

- persist the completed independent review at
  `workspace/research/investigations/simulation-execution-account-adversarial-review.md`
- put only genuinely needed review probes/notes under
  `workspace/research/`, with separate named paths

Do not create another branch unless operational isolation genuinely requires it. If the workspace
must be synchronized with main, preserve every handed-off evidence commit; no rebase/force-push that
rewrites those SHAs. The workspace is temporary evidence custody, **not** a merge candidate as-is.

## Anti-anchoring sequence — required

After the minimal report-input-integrity check, **before reading the report's recommendation in
depth**:

1. Read the landed registered question,
   `docs/research/investigations/simulation-execution-account.md`, and the corresponding row of
   `docs/research/research-register.md`.
2. Independently re-ground in the Product Charter and current Factory/Engine runtime, supported
   observation/event, Governance, and Operational authorities.
3. Reconstruct the current semantics of execution identity, scheduler time, commands, supported
   observations/events, completeness, quiescence and session control.
4. Independently identify serious candidate ownership models and at least three plausible
   falsification cases (including a closure case, but not only a closure case).
5. Write a short independent reconstruction with candidate models and hand-derived expected
   discriminators **before** deeply reading the report. Persist that short pre-review reconstruction
   separately in this workspace so its chronology is inspectable, or state why persistence was
   unavailable and clearly label the loss of anchoring evidence.
6. Only then read the exact report and the inherited analytics, game, pre-registration and
   experiment evidence.

The handoff identifies areas requiring scrutiny, not desired verdicts. The reviewer must not
substitute the handoff's framing or the original report's candidate set for its own reconstruction.

## Primary live-main sources to inspect

At minimum, read the live versions of:

- `AGENTS.md`
- `.github/agents/researcher.agent.md`
- `docs/development/researching.md`
- `docs/development/testing.md` (§10 research experiment fixtures)
- `docs/product/charter.md`
- `docs/architecture/overview.md`
- `docs/architecture/engine-semantics.md` (§1.2 and applicable scheduling rules)
- `docs/architecture/runtime-contract.md`
- `docs/architecture/operational-continuity.md`
- `docs/architecture/operational-execution-digital-twin.md`
- `docs/architecture/governance-evidence.md`
- `docs/architecture/governance-conformance.md`
- `docs/research/investigations/simulation-execution-account.md`
- `docs/research/investigations/simulation-analytics-consumer-boundary.md`
- `docs/research/investigations/simulation-analytics-evidence-provenance.md`
- `docs/research/investigations/engine-evolution.md` (§Session/advancement evolution)
- relevant current FactoryRuntime, Scheduler, runtime-event, observation/metadata,
  CommandResult, and experimental-evidence types
- current session-control, headless-closure, supported-event and determinism acceptance tests.

Perform the normal semantic-neighbor search under `docs/` and implementation/tests for:

`RunId`, `advance()`, `advanceUntil`, next-event time, internal current time, supported time,
quiescence, command interleaving, accepted/faulted/no-op commands, reset, supported event sequence,
frontier, basis observation, late join, evidence gap, interval closure, half-open intervals,
reproduction, retained supported events, history custody, and analytical result provenance.

The recently landed Engine Evolution session/advancement note documents a **different** deferred
question about driver-owned batching versus Engine-owned pre-execution time guards. Do not mistake
that note for an answer to execution-evidence temporal closure or treat a new advance API as a
prerequisite for reviewing the account semantics.

Search hits are discovery aids; cite content fetched from the exact live-main revision.

## Prior research material — only after independent reconstruction

### Original investigator's pre-registration

- commit: `6c1640b60297c1044c860a66752e5cca82969e80`
- path:
  `workspace/research/investigations/simulation-execution-account-independent-reconstruction.md`

Inspect to distinguish precommitted expectations from post hoc explanation. It does not by itself
establish independent verification.

### Experiment

- commit: `1fe4d43acb8ac902e8368a42aa4eeb58eedfb52c`
- location:
  `workspace/research/experiments/simulation-execution-account/com/arcogine/research/executionaccount/`
- relevant classes include `CapturedAccount`, `PlacementFold`, `IntervalReadings`, and
  `ExecutionAccountProvingCasesTest`.

The investigator reports 14 proving cases without failures and 151 research-experiment suite tests
passing under documented JDK-21 Docker execution. **Treat counts as the investigator's assertion
until independently checked.** Read the original expectations, inspect assertion quality and
counterfactual coverage, rerun the supported experiment workflow where available, and preferably
construct at least one new test that is not merely a variant of the investigator's expected
successful outcome. If Docker/JDK execution is unavailable, state the exact limitation and
separate code/static evidence from executed evidence.

### Paused analytics and Game evidence

For comparison, not for adoption:

- analytics Phase 1 current-boundary report:
  `7e72a46506bf63febcb57d7b7dcc9ce0cd484d64`
- original analytics Phase 2 report:
  `6a5c043563e3bd89fae6826f830b377b43643d46`
- independent original Phase 2 adversarial review:
  `2c85fe7c03c6256c9e8a2ce73fde5091b3357e3c` — `REOPEN`
- paused revised Phase 2 report:
  `75736dddfa3fe1ae89c5089720c702f4ca595ab5`
- accepted-with-qualifications Game diagnostic report:
  `4ad00136441fa29e6b462ac5431b60cf2de1e148`
- Game independent adversarial review:
  `6484795ff4c4ad971e06c70751b03805e54a55f5`

These artifacts are on their respective existing `workspace/` research branches; resolve exact
commit+path from the execution-account report's evidence table. They are not current architectural
authorities and do not excuse independently testing the new report.

## Primary review question

Does the report establish the **correct minimum consumer-neutral meaning of one execution and its
evidence**, placed with the correct existing authority, while avoiding an unjustified new entity,
terminal state, central custody owner, event sourcing, or analytical ownership inversion?

Challenge the whole report. In particular distinguish:

- what the current runtime actually guarantees;
- what the report proposes as new *normative* coverage rules;
- what a capture holder can verify from supported observations/events;
- what only the controller can attest;
- what remains analytical or consumer-defined.

Do not let successful execution of one research helper make a proposed public semantic rule
automatically correct.

## Mandatory adversarial attack A — interval closure / E6

This issue is **part of the existing execution-account question**, not a separate deferred
investigation.

Independently test the rule for a claim over a half-open simulated interval `[a,b)` under event-driven
time, including when `advanceUntil(b)` stops before observed time `b`.

The review must discriminate **all** of these propositions:

1. A controller has processed every *currently known* event at or before `b`.
2. The supported observation's simulated-time frontier has reached `b`.
3. The controller promises to introduce no more inputs that can affect `[a,b)`.
4. The controller has actually finished processing pending internal events at or before `b`.
5. The capture contains every supported change required for `[a,b)`.
6. The resulting interval claim is historically final, as opposed to only a provisional observation.

Do not assume that any one proposition implies the others.

**Must-try counterexamples:**

- `advanceUntil(100)` leaves a quiescent runtime at tick 13; then the controller submits
  further work at tick 13. A truthful `[0,100)` metric must not have been finalized.
- `advanceUntil(b, maxEvents)` stops due to **event-count exhaustion** while additional
  events at or before `b` remain. A controller's declaration of "no future commands" is not
  enough if already-scheduled work remains unprocessed.
- A controller declares closure before draining/recording all supported changes that were
  actually emitted through its frontier. Determine whether E5 gap/frontier accounting prevents the
  resulting false claim.
- A pending *internal no-op* marker versus a pending authoritative event at or before `b`.
  Does the distinction affect how the controller can attest closure?
- A command is accepted after the last supported observation time but before `b`, where
  internal scheduler time may exceed the latest supported time; check whether the current
  supported-time frontier can falsely license a conclusion or whether the report's qualification
  is sound.
- **Boundary convention:** compare an event at exactly `b` to one strictly before `b`.
  A `[a,b)` claim excludes the event at `b`; the research helper's declared
  `closedThrough` condition may be inclusive. Determine whether this is merely a deliberately
  stronger controller promise or a genuine off-by-one / contract mismatch; test equal-time
  changes at the boundary.
- Explicitly test a controller declaration made prematurely and then contradicted by a later
  accepted command, *including* a command that produces no supported change.

The review must decide whether "controller declaration" is an **externally trusted attestation**
requiring preconditions, a verifiable runtime observation fact, or insufficiently defined to serve as
a consumer-neutral closure rule. Challenge whether the report's research-local
`CapturedAccount.declareClosedThrough(...)` (which stores a horizon) can actually establish its
stated preconditions from supported evidence.

For any found defect: provide the smallest reproducible counterexample, its authority/evidence,
whether E6 needs a bounded qualification/revision or invalidates a load-bearing ownership claim,
and the precise reconciliation consequence. Do not insist on adding a public API or a new
Engine primitive unless a concrete falsifier makes it necessary.

## Mandatory adversarial attack B — completeness / E5 and live delivery

Test basis observation, ordered supported-event sequences, frontier and tail-gap detection against:

- late join: complete *current* view versus incomplete earlier history;
- a middle gap and a missing tail against a later frontier;
- two consumers independently draining one live run, potentially seeing individually contiguous
  but jointly partitioned histories;
- reset/new `RunId` and accidental cross-run sequence joining;
- same-simulation-time events whose sequence order is essential;
- a capture frozen and transferred between parties without preserving its completeness basis.

A run ID alone, a gap-free-looking local list, and an advancement target are all insufficient
shortcuts. Test whether the proposed common vocabulary is both necessary and sufficient for the
specific claims made — not for every imaginable future analytical use.

## Mandatory adversarial attack C — semantics and owner placement

Compare serious alternatives including:

1. current producer contract plus strictly specified caller-capture rules;
2. a distinct bounded execution-account semantic owner;
3. a consumer-neutral retained execution record/history owner;
4. analytics/inspection-owned evidence session;
5. a hybrid that separates semantic meaning, delivered custody, and method-specific sufficiency.

Look for an **actual semantic difference**, not a naming or packaging preference.

Challenge in both directions:

- Could all coverage rules be legitimate caller conventions without incompatible truth claims?
- Does defining coverage in the Engine-owned runtime contract wrongly conflate source facts with
  evidence-custody attestation?
- Are any proposed E1–E8 rules truly **new supported consumer semantics** needing architecture
  reconciliation rather than merely existing derived invariants?
- Does one semantic execution require its own entity/identity, or is `RunId` enough?
- Does quiescence mean an end, or only an absence of currently pending authoritative work?
- Does a cross-consumer retained record become necessary for a concrete present requirement?
  Distinguish a present need from a plausible future multi-client service.
- Does placing one definition in shared analytics force that definition owner to own history?
- Does information loss/ambiguity alone prove an architecture owner? Recall the prior analytics
  `REOPEN` review's warning: information sufficiency is **not** an ownership theorem.

Require a supported consumer/proving case for each asserted necessity.

## Mandatory adversarial attack D — provenance, authority, derivations

Independently check:

- What `RunId` identifies, and whether reset leaves the old session usable.
- Which requests, accepted/rejected/no-op/`Faulted` outcomes, authoritative mutations, supported
  events and current observations belong to an execution account versus controller provenance.
- Whether a non-controlling consumer can assert "complete execution account" while missing a fault
  or a controller's no-op/rejection; does this make the term misleading?
- Whether supported history is a reproduction script, including command interleavings that depend
  on internal event counts not represented in supported events.
- Whether a future exact Engine-definition basis is required by a current proving case or only a
  cross-build provenance/reproduction claim.
- Whether "processing interval" and "waiting interval" have uniquely Engine-determined primitive
  meanings in every current supported case. Try at least one scenario that could admit two
  legitimate readiness/eligibility/waiting interpretations. If so, separate primitive execution
  facts from method-specific analytical attribution.
- Whether utilization, occupancy, starved/idle characterization and bottleneck detection remain
  analytical definitions with legitimate alternatives; do not conflate a complete account with
  sufficiency for every analytical question.
- Whether the proposed account is a second current-state authority, event-sourced engine, Governance
  controlled history, or Operational accountable continuation.

Do not enlarge the investigation into operational execution implementation, a distribution
protocol, a general query engine, or formula selection.

## Mandatory adversarial attack E — downstream and robustness

Test the report's claimed consequences for the *paused* simulation-analytics Phase 2:

- coverage meaning versus definition-specific sufficiency versus physical custody;
- the Phase 2 "evidence custody follows definition owner" rule;
- what happens if a stateless shared definition consumes evidence held by a separate session driver;
- whether Game/verification/monitoring consumers can agree on common execution facts without
  a centralized record now;
- which evidence/provenance candidate concerns genuinely move upstream versus remain analytical;
- whether optional future retention and handover are correctly triggered rather than silently
  assumed available today.

Check spatial-transfer compatibility as a **bounded reasoned recheck** unless executable spatial
transfer currently exists; do not claim an unexecuted transfer case has executable proof.

Challenge the report's transferable generalization. A rule valid for this deterministic,
totally-ordered, event-driven producer need not apply unchanged to sampled telemetry,
set-ordered Operational records, or a durable shared log.

## Required evidence standard

For every material challenge, supply:

- the exact claim challenged and its source location in the reviewed report;
- current authority or independently executed evidence;
- the minimal counterexample, if one exists;
- whether the issue affects a load-bearing conclusion or is a qualification;
- a disposition on that individual challenge (`SURVIVES`, `QUALIFY`, `NEEDS EVIDENCE`,
  or `FALSIFIED` are acceptable local labels, not new research outcomes);
- what would need to change in a report revision or later reconciliation.

Do not list generic architectural preferences as findings.

Prefer hand-derived expectations and independently authored counterexamples. Use the documented
research-experiment JDK21 Docker or Gradle workflow when available; retain new probe sources in the
workspace. Do not silently modify the original investigator's experiments or tests.

If an existing finding is merely a known non-goal or already disclosed limitation, distinguish it
from a new falsification. If a test passes, explain which narrower proposition it establishes.

## Required review artifact structure

Persist a decision-quality review containing:

1. exact source report branch, commit, path and report research baseline;
2. live-main review baseline, final recheck and material drift analysis;
3. independence statement and anchoring-control chronology;
4. self-derived candidate set and counterexamples before report consumption;
5. original report's material conclusions and candidate-selection argument;
6. review of E1–E8 as needed for the ownership decision;
7. **explicit E6 interval-closure findings**, including pending-work, deadline/event-count, and
   half-open boundary semantics;
8. E5 coverage/delivery and late-join findings;
9. provenance, controller outcomes, and reproduction limits;
10. cross-consumer/analytics and Governance/Operational boundaries;
11. source and experiment verification, limitations and any new probe coordinates;
12. strongest surviving counterargument to the result (even if ultimately rejected);
13. what survives, what must be qualified, what needs more evidence and what is falsified;
14. exact minimal changes necessary before durable reconciliation, or why none are needed;
15. one and only one final disposition among the four permitted outcomes.

Preserve the distinction between "the conclusion is wrong" and "one local experiment helper
overclaims a half-open interval"; a localized defect need not invalidate the ownership model, but
it must not be silently reconciled as if E6 were proved.

## Stop boundary / handoff

This run is an **independent adversarial review only**.

Do **not**:

- edit the original completed report in place;
- adopt architecture or implement an execution-account type/service;
- edit `docs/architecture/`, `docs/planning/`, or the maintained research register;
- create a new closure research question merely because E6 needs qualification;
- independently review or resume the paused analytics Phase 2 report;
- change product/runtime behavior;
- merge the workspace;
- perform the later reconciliation or file its PR.

After reviewing and persisting the review artifact, stop. Return only:

- live-main review baseline SHA and final recheck;
- reviewed report commit SHA and exact path;
- workspace branch;
- exact independent-review commit SHA and path;
- any pre-review reconstruction/independent probe commits and paths;
- independence status;
- the four-valued disposition;
- concise material findings (especially whether E6 survives and whether owner placement survives);
- precise required consequences for a later report revision or durable reconciliation;
- whether a further review cycle is needed.

Do not paste the complete review artifact into chat. Its exact committed coordinates are the handoff.
