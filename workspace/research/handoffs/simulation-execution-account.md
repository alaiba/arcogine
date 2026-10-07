# Handoff — Simulation execution account semantics research

Execute the landed **READY / High-risk** research question:

> What minimum consumer-neutral semantic account, if any, of one simulated execution must Arcogine
> define so independent capabilities can reason about the same execution without reconstructing
> Engine behavior, making the Engine event sourced, or independently assembling incompatible notions
> of execution history?

This is a new foundational research line. It is upstream of the prospective simulation-analytics
ownership conclusion, but it is **not** an analytics Phase 3, not a runtime-retention implementation
slice, and not an architecture reconciliation run.

## Workspace and custody

Use this temporary research-evidence workspace:

- branch: `workspace/simulation-execution-account`

Persist the completed report under:

`workspace/research/investigations/simulation-execution-account-report.md`

Put any new experiments, generated fixtures, reconstruction probes, or supporting notes under the
same `workspace/research/` tree.

The workspace is temporary custody and must not be merged to `main`.

## Landed admission baseline

This handoff was prepared after PR #461 landed.

- landed admission commit / current `main` at handoff creation:
  `79b3399149499f2f337206c909a277f66a5befd5`
- admitted brief:
  `docs/research/investigations/simulation-execution-account.md`
- maintained register:
  `docs/research/research-register.md`

Resolve live `main` again at the start of the research run and record that exact SHA as the research
baseline. The SHA above is context, not authority if `main` has moved.

## Start-of-run grounding

Read and follow current live-main versions of:

- `AGENTS.md`
- `.github/agents/researcher.agent.md`
- `docs/development/researching.md`
- `docs/development/testing.md`
- `docs/product/charter.md`
- `docs/research/research-register.md`
- `docs/research/investigations/simulation-execution-account.md`
- `docs/research/investigations/simulation-analytics-consumer-boundary.md`
- `docs/research/investigations/simulation-analytics-evidence-provenance.md`
- `docs/architecture/overview.md`
- `docs/architecture/runtime-contract.md`
- `docs/architecture/engine-semantics.md`
- `docs/architecture/operational-continuity.md`
- `docs/architecture/operational-execution-digital-twin.md`
- `docs/architecture/governance-evidence.md`
- `docs/architecture/governance-conformance.md`
- `docs/architecture/storage.md`
- `docs/product/concepts.md`
- the current `FactoryRuntime`, runtime observation/event types, command-result types, reset/session
  behavior, and directly relevant conformance tests;
- the current Challenge attempt/comparison surfaces;
- the current `product/research-experiments/` substrate and relevant temporal/event oracles.

Perform the normal repository/docs semantic-neighbor search. At minimum search for:

- execution / run / session
- `RunId`
- reset / fresh run
- runtime observation / runtime event
- `latestEventSequence`
- retained events / drained events / history
- late join / gap / completeness
- command result / accepted command / faulted command
- model fingerprint / controlled revision provenance
- replay / seek / checkpoint / restore / fork
- attempt comparison / rerun
- evidence / evidence use / provenance
- operational continuation
- utilization / occupancy / waiting / starvation
- transfer events / spatial runtime

Search results are discovery only. Fetch every load-bearing source at the exact research baseline
before relying on it.

## Exact inherited research evidence

Use these exact workspace artifacts as prior evidence after independently reconstructing the current
question from the landed brief and current architecture.

### Simulation analytics current-boundary evidence

- Phase 1 report:
  - commit: `7e72a46506bf63febcb57d7b7dcc9ce0cd484d64`
  - path:
    `workspace/research/investigations/simulation-analytics-ownership-phase-1-current-boundary.md`

Treat Phase 1's current Engine/runtime classification as inherited evidence. Do not reopen it unless
current-main evidence materially contradicts it.

### Original analytics Phase 2 and REOPEN review

- original Phase 2 report:
  - commit: `6a5c043563e3bd89fae6826f830b377b43643d46`
  - path:
    `workspace/research/investigations/simulation-analytics-ownership-phase-2-admission-boundary.md`
- independent adversarial review:
  - commit: `2c85fe7c03c6256c9e8a2ce73fde5091b3357e3c`
  - path:
    `workspace/research/investigations/simulation-analytics-ownership-phase-2-adversarial-review.md`
  - disposition: **REOPEN**

Use the review especially for its warning that information-loss or coupling observations are not
ownership theorems and that shared ownership must be justified by a real requirement.

### Latest paused analytics Phase 2 revision

- revised Phase 2 report:
  - commit: `75736dddfa3fe1ae89c5089720c702f4ca595ab5`
  - path:
    `workspace/research/investigations/simulation-analytics-ownership-phase-2-admission-boundary-revision.md`

This revision is **paused before independent review** because the execution-account question may
change its premise that the first consumer should own evidence-window capture/completeness.

Treat it as candidate analysis, not accepted evidence. In particular, test rather than assume its
Rule 7 proposition that evidence custody simply follows the analytical definition owner.

### Accepted game diagnostic evidence

- game report:
  - commit: `4ad00136441fa29e6b462ac5431b60cf2de1e148`
  - path:
    `workspace/research/investigations/factory-design-game-diagnostic-evidence-report.md`
- independent game review:
  - commit: `6484795ff4c4ad971e06c70751b03805e54a55f5`
  - path:
    `workspace/research/investigations/factory-design-game-diagnostic-evidence-adversarial-review.md`
  - disposition: **ACCEPT WITH QUALIFICATIONS**

The game evidence supplies concrete downstream reasoning needs, including:

- machine/slot timelines;
- waiting-by-step;
- utilization over a period;
- idle-flow/starvation characterization;
- controlled comparison;
- nearby bottleneck/shifting/co-binding diagnostic methods.

Use the independent review's qualifications as binding. Do not treat the research-local formulas as
production semantics.

## Research-order independence requirement

Before deeply reading the paused analytics Phase 2 recommendation, reconstruct this question from:

1. the landed execution-account brief;
2. the Product Charter;
3. current runtime/Engine architecture and implementation;
4. Governance and Operational boundaries;
5. accepted game requirements.

Only then use the analytics reports as prior candidate evidence.

The purpose is to avoid anchoring this investigation on either:

- "caller capture is obviously enough"; or
- "Arcogine obviously needs an Execution module".

Both are hypotheses under test.

## Core question decomposition

The report must separately decide:

1. **Referent:** What, if anything, is the semantic thing called "one simulated execution" beyond a
   live `FactoryRuntime` instance and its current projections?
2. **Identity:** Is current `RunId` sufficient for that referent? What does it identify exactly?
3. **Boundary:** What starts an execution? What does reset end/start? Is terminal completion
   meaningful?
4. **Basis:** Which model, Engine interpretation, explicit inputs, and other result-affecting context
   must be attributable to the execution?
5. **Record semantics:** Which accepted requests/results, observations, and supported runtime events
   belong to the account, and which remain separate facts merely related to it?
6. **Ordering:** Which order is semantic — command acceptance, supported event sequence, simulation
   time, or a combination?
7. **Completeness:** How can a consumer know whether it has a complete execution, a complete bounded
   interval, a late-joined partial window, a detected gap, or unknown completeness?
8. **Live/completed continuity:** Can one semantic execution be consumed incrementally while live and
   later as a completed record?
9. **Retention:** Does the semantic contract require central retained custody, only a common
   completeness contract, or neither?
10. **Authority:** How does historical execution evidence remain non-authoritative with respect to
    live mutable runtime state?
11. **Downstream reasoning:** Which parts of utilization/waiting/timeline/comparison evidence assembly
    should be execution-level versus analytical-definition-level?
12. **Cross-domain relation:** How does this differ from Governance history and Operational
    accountable continuation?

Do not collapse these into one "event log" question.

## Candidate models

Evaluate at least the landed brief's materially distinct candidates.

### Candidate 1 — current observation/event boundary plus caller capture is sufficient

The current runtime contract is the consumer-neutral execution boundary.

A consumer that needs history captures supported events/observations itself and declares its own
complete/partial window.

This candidate must explain how independent consumers avoid incompatible meanings for:

- whole-run completeness;
- late join;
- reset;
- execution start/end;
- accepted-input provenance;
- terminal completion;
- comparison basis.

Do not reject it merely because duplicated plumbing feels inelegant.

### Candidate 2 — bounded consumer-neutral execution-account semantics, representation deferred

Arcogine defines the semantic meaning of one execution — identity, basis, ordering, accepted
execution evidence, completeness, live/completed status — while leaving storage, transport, module,
and retention mechanism open.

Test whether this is a real semantic responsibility distinct from both Engine and analytics.

### Candidate 3 — retained consumer-neutral execution-record capability

A shared owner retains sufficient supported execution evidence for consumers.

This candidate must prove a concrete need for central custody/retention rather than merely for common
semantics. It must also show why that custody is not:

- Engine event sourcing;
- Governance controlled history;
- Storage authority by implication;
- an analytics-only concern.

### Candidate 4 — analysis/inspection-owned evidence session

An analysis or inspection capability captures one run and supplies completeness/provenance.

Test whether this is too narrow for:

- verification;
- non-analytical outward clients;
- future monitoring/improvement;
- game presentation that needs execution history before choosing an analytical method.

### Candidate 5 — hybrid

Admit a hybrid only if it changes a semantic decision, for example:

- one cross-consumer execution-account contract;
- caller-owned or shared physical custody depending use;
- analytics-specific accumulators layered above it.

Do not create a hybrid merely to avoid choosing.

## Mandatory semantic distinctions

### A. Live authoritative state versus historical execution evidence

Preserve:

> Engine/runtime domains own current authoritative simulation state.

A retained account of what happened must not become another mutable authority and must not be the
mechanism required to reconstruct current runtime state.

### B. Internal Engine events versus supported runtime events

Supported runtime events are the outward semantic change contract.

Do not make scheduler-private/internal events part of the execution account merely because they are
convenient to inspect.

If a proving case cannot be served from supported facts, record the missing supported fact
explicitly.

### C. Request, command result, transition, observation, and interpretation

Keep distinct:

- caller request;
- Engine acceptance/rejection/fault;
- accepted authoritative transition;
- supported runtime event;
- supported current observation;
- downstream analytical or consumer conclusion.

Determine which of these need membership/correlation in an execution account and which remain
separate related records.

### D. Event sequence versus simulation time

Use equal-time event cases to establish:

- supported sequence is an ordered-change coordinate;
- simulation time is a domain time coordinate;
- equal timestamps do not erase supported event order.

Do not infer more ordering guarantees than the contract supports.

### E. Complete current view versus complete history

The runtime contract already guarantees that a fresh supported observation is sufficient to build a
current consumer view without replaying history.

Do not confuse that guarantee with whole-run historical completeness.

The investigation must make the distinction executable.

### F. Execution completeness versus analytical sufficiency

A fully captured execution may still be insufficient for a particular analytical method if that
method requires facts the runtime does not expose.

Conversely, a partial execution capture may be sufficient for a bounded interval method when its
required interval is explicitly complete.

Do not make "execution complete" mean "supports every future analytical question".

## Mandatory proving cases

Use or add deterministic executable evidence for each.

### 1. Full-run capture from start

Start from a fresh run and capture the supported execution boundary from the beginning to a known
terminal/quiescent point.

Prove which facts establish:

- one run;
- no missing supported event sequence inside the captured range;
- final observation agreement;
- model basis.

Do not assume "terminally complete" exists as a current supported concept; investigate it.

### 2. Late join

Observe a run only after prior supported events have drained.

Show simultaneously:

- a fresh observation is sufficient for current-state consumption;
- the consumer cannot truthfully claim whole-run history;
- any later complete interval claim must have an explicit basis.

Test the candidate models against this case.

### 3. Explicit gap

Construct or model an evidence set with a missing supported event sequence between two observed
points.

Any candidate that silently treats the set as complete fails.

### 4. Reset / fresh run

Use `FactoryRuntime.reset()`.

Pin:

- new run identity;
- new sequence epoch;
- same model version when applicable;
- no silent carry-over of historical/analytical state.

Determine whether the old and new run have any execution-account relation beyond a consumer-owned
comparison.

### 5. Equal simulation time, ordered supported changes

Use a case with multiple supported events at the same simulation time.

Show why sequence order remains part of the account even when timestamps match.

### 6. Running work crossing a measurement interval boundary

Use a long-running processing step.

Test whether an execution account needs:

- an observation at the interval boundary;
- a prior dispatch/start event;
- a later completion event;
- or some combination.

Separate execution evidence sufficiency from the utilization formula itself.

### 7. Availability change and idle slot

Use the independently reviewed game case where a multi-slot machine returns online with eligible
queued work and can still expose an idle slot.

Test the account's ability to preserve the actual temporal evidence without encoding the game's
starvation characterization as execution truth.

### 8. Two legitimate analytical definitions over one execution

Use one complete execution evidence set and apply at least two legitimate analytical definitions or
basis choices that produce different results.

The execution account must remain identical while interpretations differ.

This is a discriminator against accidentally making analytical semantics part of execution
semantics.

### 9. Controlled comparison of two runs

Run two independent executions with one authored change.

The comparison must:

- keep the two run identities distinct;
- preserve each execution's own completeness/basis;
- identify the authored change through the owning Factory/Governance comparison semantics;
- avoid merging event streams or inventing one super-run.

### 10. Faulted command / partial mutation if current evidence permits

Inspect current `CommandResult.Faulted` semantics and any existing fault fixture.

Determine how an execution account should represent:

- the caller request;
- the fault result;
- any authoritative changes that already occurred;
- the fact that fault outcome may follow partial mutation.

Do not invent transactional rollback.

If current executable evidence cannot reach a stable fault fixture without depending on private
internals, document the limitation rather than manufacturing one.

### 11. Spatial compatibility check

Do not block this investigation on spatial runtime implementation.

Instead re-ground the landed spatial runtime plan and current transfer event contract, and show
whether the surviving execution-account rule naturally extends when new Engine-owned transfer facts
become supported.

If the rule would need semantic restructuring merely because transfer events are added, treat that as
an anti-rework failure.

## Charter-level multi-consumer pressure

Use the Product Charter carefully.

The charter normatively establishes lifecycle modes:

- design;
- understand;
- simulate;
- verify;
- operate;
- monitor;
- improve.

That is legitimate evidence that multiple purpose-built consumers/modes are intended.

It does **not** prove:

- one physical application;
- one transport;
- one database;
- one module;
- one retention policy;
- one analytical definition.

The report must state what cross-consumer execution semantics, if any, follow from semantic/lifecycle
continuity rather than from speculative reuse.

## CLI/web representation case

A CLI and web UI for the same Game/Challenge semantics may be two presentations of one semantic
consumer.

Use them to test representation independence and late-join/completeness behavior, not as sufficient
proof of cross-domain semantic reuse by themselves.

## Industry-standard analytical notions

Utilization, occupancy, throughput, lead time, WIP, waiting, bottleneck/constraint analysis, and
similar notions are legitimate production-system analysis concepts, not game inventions.

However, common industry terminology does not by itself fix one exact Arcogine formula or owner.

Use that distinction:

- concept family may be cross-consumer;
- named definition still requires exact basis/method semantics.

Do not import an external standard as Arcogine's canonical execution ontology without direct
evidence.

## Operational-continuity comparison

Perform a disciplined comparison with `docs/architecture/operational-continuity.md`.

At minimum compare:

| Dimension | Simulation execution | Operational continuation |
|---|---|---|
| lifetime | bounded run? | independently continuing operational account |
| authority | deterministic simulated execution | accountable conduct/conclusions amid external authority |
| raw external observations | not the normal basis | explicitly independent facts |
| reset | fresh run identity | not equivalent to ordinary simulation reset |
| fork/lineage | current meaning? | explicit settled continuation/fork rules |
| durability | currently not promised | semantic durability is load-bearing |
| replay | deterministic rerun is separate | physical world does not replay |

Do not infer shared identity merely from structural analogy.

A valid conclusion may identify a reusable conceptual pattern without sharing a type or lifecycle.

## Governance comparison

Test whether a simulation execution account is:

- evidence that Governance may consume;
- a governed occurrence;
- controlled history;
- or a separate simulation-domain record.

Do not move controlled revision, evidence-use, conformance, or governed-change ownership out of
Governance.

If an execution account later becomes Governance evidence, preserve the distinction between:

- producer-owned execution provenance;
- Governance-owned evidence applicability/use.

## Exact Engine-definition provenance

The current runtime has model fingerprint provenance but no dedicated exact Engine-definition
identifier.

Do not add one merely because the execution-account model looks cleaner with it.

The report must identify the **first concrete claim** that would become untruthful or unreproducible
without exact Engine-definition identity. If no current proving case needs it, leave the capability
unadmitted and state the reopening trigger.

## Retention and persistence discipline

This investigation may determine that some consumer needs retained execution evidence.

Still distinguish:

1. **semantic ownership** — what a complete execution account means;
2. **custody/retention responsibility** — who must retain which evidence for which support horizon;
3. **physical persistence** — how bytes are stored.

Do not jump directly from (1) to Storage, a database, event sourcing, or unbounded history.

If Candidate 2 wins, explicitly state whether the semantic contract is implementable by caller-owned
capture today.

If Candidate 3 wins, prove why caller-owned or use-owned capture is insufficient for the concrete
requirement.

## Analytics-boundary consequence

The final report must include a section:

### Consequence for simulation analytics

Classify each concern as one of:

- **Engine execution semantics**
- **Execution-account semantics**
- **Analytical definition/provenance**
- **Consumer interpretation/presentation**
- **Governance evidence/use**
- **Operational-only**

At minimum classify:

- `RunId`;
- model fingerprint;
- Engine definition identity, if any;
- supported event sequence;
- observation cursor;
- full-run completeness;
- bounded-interval completeness;
- late-join state;
- retained event custody;
- processing intervals;
- waiting intervals;
- occupancy;
- utilization;
- starvation characterization;
- bottleneck method identity;
- cross-run comparison;
- analytical-definition identity;
- consumer wording/pedagogy.

Then state exactly what the paused analytics Phase 2 must reconsider.

Do not finish the analytics ownership decision in this run.

## Evidence/provenance consequence

Include a separate section:

### Consequence for analytics evidence/provenance

Identify which current candidate concerns move upstream into execution-account semantics and which
remain genuinely analytical.

At minimum test:

- run/reset basis;
- event ordering;
- complete/partial evidence-window semantics;
- late join;
- retained-event custody;
- accumulator ownership;
- analytical-definition identity;
- result provenance;
- reproduction/compatibility.

The outcome may be:

- execution account absorbs several concerns and narrows the analytics question;
- caller capture wins and the analytics question remains responsible for method-specific completeness;
- a retained record is required and changes the promotion trigger;
- another bounded split, if evidence supports it.

## External evidence

Use external evidence only where it materially discriminates:

- simulation run/history semantics;
- trace/event record versus authoritative simulator state;
- observability/telemetry completeness;
- execution records used by multiple analytical consumers;
- manufacturing/simulation analysis conventions.

Do not turn the investigation into a survey of event sourcing, observability platforms, process
mining products, MES products, or generic data engineering.

External precedent shows possibility and vocabulary, not Arcogine necessity.

## Required report structure

The report must include at least:

1. exact research baseline and final live-main recheck;
2. authority/non-goal statement;
3. current runtime-contract reconstruction;
4. current implementation evidence;
5. exact prior workspace evidence consumed;
6. independent reconstruction before reading paused Phase 2;
7. candidate models;
8. semantic-dimension matrix;
9. proving-case results;
10. identity/boundary conclusion;
11. ordering/completeness conclusion;
12. live-versus-completed conclusion;
13. retention/custody conclusion;
14. Engine/Governance/Operational boundary analysis;
15. consequence for simulation analytics;
16. consequence for analytics evidence/provenance;
17. architecture/support changes that would be required **if accepted**;
18. unresolved unknowns and reopening triggers;
19. confidence by conclusion class;
20. adversarial self-challenge.

## Decision-quality outcomes

A valid positive result may conclude:

> Arcogine needs a bounded consumer-neutral semantic account of one simulation execution, separate
> from Engine runtime authority and analytical interpretation.

If so, state the exact minimum responsibility and why caller-local assembly is insufficient or
architecturally unsafe for the accepted use cases.

A valid conservative result may conclude:

> The current runtime observation/event contract plus explicit caller-capture/completeness rules is
> the correct consumer-neutral execution boundary; no additional semantic owner is justified.

If so, state the exact common rules that prevent incompatible execution histories and who owns them.

A valid stronger result may conclude:

> A retained execution-record capability is required now.

If so, identify the concrete use that makes central custody necessary and why bounded caller or
consumer custody cannot satisfy it.

Do not choose the result from architectural taste.

## High-risk review boundary

This registered question is **High risk**.

Follow the current independent-adversarial-review rules in
`docs/development/researching.md`.

After the completed report and any research-custody experiments are persisted, stop.

Do not perform the independent adversarial review in the same research run.

The later reviewer must bind to the exact report revision, record its live-main baseline, and use the
research-review disposition vocabulary:

- ACCEPT
- ACCEPT WITH QUALIFICATIONS
- MORE EVIDENCE REQUIRED
- REOPEN

## Non-goals

Do not:

- implement product code;
- add a production execution-account type;
- create an analytics module;
- choose a database;
- add Storage support;
- introduce event sourcing;
- promise unbounded retention;
- define public HTTP/SSE/WebSocket/CLI schemas;
- implement replay/seek/checkpoint/restore/fork;
- unify simulation `RunId` with Operational continuation identity;
- create a universal execution-context registry;
- choose final utilization, starvation, bottleneck, or comparison formulas;
- reconcile architecture/planning/register to a conclusion in the same run;
- resume or independently review the paused analytics Phase 2 report;
- merge any workspace branch.

## Stop boundary

Stop after:

1. the exact current-main baseline is recorded;
2. required current repository evidence is inspected;
3. the mandatory proving cases that are executable are run;
4. any new research-custody experiment is persisted;
5. the complete report is persisted;
6. final live-main recheck is recorded;
7. the report states the downstream consequence for analytics and analytics evidence/provenance;
8. the report states that independent adversarial review is required next.

## Expected handoff

Return only:

- research baseline SHA;
- final live-main recheck SHA;
- workspace branch;
- exact report commit SHA;
- report path;
- any experiment/fixture paths and exact containing commit;
- concise research result;
- whether an additional consumer-neutral execution-account responsibility was established;
- whether retained custody was established as required, optional, or unnecessary;
- exact consequences for paused analytics Phase 2;
- exact consequences for the analytics evidence/provenance candidate;
- whether any current architecture/runtime contract would need revision if accepted;
- confirmation that a genuinely independent adversarial review is required next.

Do not paste the full report into chat after persistence.
