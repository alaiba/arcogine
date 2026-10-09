# Implementation handoff — reconcile simulation execution-evidence semantics

## Role, authority, delivery vehicle

Act as a **coding/contributor agent executing a bounded, research-backed architectural reconciliation**.
This is **not** a new Researcher investigation, an adversarial research review, the R-remove implementation, or independent PR review.

Follow the live repository versions of `AGENTS.md`, `.github/CONTRIBUTING.md`,
`docs/development/researching.md` (§10, evidence transfer and retirement),
`docs/development/reviewing.md` (architectural reconciliation and independent review),
`docs/development/testing.md`, and the repository PR workflow.

- Repository: `alaiba/arcogine`.
- **Continue on the existing branch:** `workspace/simulation-execution-account`.
  Do not create a new research or reconciliation branch merely for a phase change.
- This handoff path: `workspace/implementation/simulation-execution-account-reconciliation.md`.
  It is **temporary custody**, not a file to merge into `main`.
- Live `main` at handoff preparation: `b9902d407e048f731e31e62e85726ed3b9ddbbff`.
  Re-resolve it at execution start and before final review.
- Separate, active Engine correction: `fix/engine-remove-inert-markers`, handoff-only head
  `3a0af85ae498bfc5aae9d9a25254bfe69b464565` when checked. Its owner has selected
  **R-remove**. Recheck the live implementation branch/PR and actual merged `main` state;
  this historical head does **not** imply code has landed.
- Original research/review commits must remain reachable in the workspace history.
  No rebase, force-push, amend, wholesale cherry-pick/merge of research into another branch,
  or rewrite of handed-off evidence commits.

**Mission:** turn the independently reviewed execution-account conclusions into concise,
current, consumer-neutral **runtime observation/event and execution-evidence semantics**,
carry all material qualifications, close the single registered execution-account research
question through an independently reviewed reconciliation, and unblock the already-paused
analytics ownership decision. No new Engine execution-account API or history owner.

This handoff **may be executed now** while R-remove is in progress. Prepare and validate the
settled rules immediately. Any final wording that depends on the scheduler/command-time
correction must be verified against the Engine contract that actually lands, not copied from
a feature branch or speculative API sketch.

## Immutable evidence inputs — exact revisions, not branch tips

Before making normative changes, inspect these artifacts in full where relevant and verify
each independent review expressly names its reviewed report SHA:

1. **Execution-account report** — `a53778145f3ee9941a4b46c7f04f1ec17e6bff86`,
   `workspace/research/investigations/simulation-execution-account-report.md`.
   Focus: proposed E1–E8 (§executive rule table), §§10–17, 19, 23.
2. **Its independent adversarial review** — `57d87fc4c8ba19927bf50efa4097321208811d5a`,
   `workspace/research/investigations/simulation-execution-account-adversarial-review.md`.
   Disposition: **ACCEPT WITH QUALIFICATIONS**. Focus: stale-frontier falsifier,
   claim-scoped coverage and finality, §§Closure, Coverage, Minimum consequences.
3. **Engine-adaptation follow-up report** — `9551c0cee42bfdeab07f4a55f931eb10ec5fba4e`,
   `workspace/research/investigations/simulation-execution-account-engine-adaptation-report.md`.
   Focus: §11 E6 proof-sequence sharpening, §12 present-versus-future Engine decision,
   §13 reconciliation consequences. Its D-clock and E-guard recommendations are **not**
   automatically adopted.
4. **Its independent adversarial review** — `e3a4f9a223b982b341a71c2acf7b74146620e9f2`,
   `workspace/research/investigations/simulation-execution-account-engine-adaptation-adversarial-review.md`.
   Disposition: **ACCEPT WITH QUALIFICATIONS**. Focus: §3 marker defect, §4 remedy
   alternatives, §§5–6 Game applicability and T1–T3, §8 reconciliation sequence.
5. **Coupled Game evidence, only as dependency context:** diagnostic report
   `4ad00136441fa29e6b462ac5431b60cf2de1e148`,
   `workspace/research/investigations/factory-design-game-diagnostic-evidence-report.md`;
   independent diagnostic review `6484795ff4c4ad971e06c70751b03805e54a55f5`,
   `workspace/research/investigations/factory-design-game-diagnostic-evidence-adversarial-review.md`.
   These live on the Game research workspace. Do not reconcile that question in this PR.

The prior research's passing probes, the review's new falsifiers, and research-local helpers are
evidence, **not** new runtime contracts or production types. Do not edit original report/review
contents to make them read as if reconciliation has already landed.

## Live grounding and semantic-neighbor search

Resolve `main`, the active workspace head, the R-remove branch and any PR.
Read the exact live-main versions of:

- `AGENTS.md`, `.github/CONTRIBUTING.md`, `docs/development/researching.md`,
  `docs/development/reviewing.md`, `docs/development/testing.md`;
- `docs/product/charter.md`, `docs/architecture/overview.md`,
  `docs/architecture/runtime-contract.md`,
  `docs/architecture/engine-semantics.md`,
  `docs/architecture/governance-evidence.md`,
  `docs/architecture/operational-continuity.md`;
- `docs/research/investigations/simulation-execution-account.md`,
  `docs/research/research-register.md`,
  `docs/research/investigations/simulation-analytics-consumer-boundary.md`,
  `docs/research/investigations/simulation-analytics-evidence-provenance.md`,
  `docs/research/investigations/engine-evolution.md`;
- `docs/planning/factory-design-game-consumer.md` (simulation control/admission) and
  `docs/planning/factory-design-game-vertical-slice.md` (product gates);
- `FactoryRuntime`, `RuntimeObservation`, `RuntimeEventEnvelope`,
  delivery and session-control acceptance tests, and source definitions of
  run/sequence/fingerprint/controlled-revision/fault/command outcomes.

Do a quick search under `docs/` first, then implementation/tests, for `RunId`,
`latestEventSequence`, `reset`, `QUIESCENT`, observed/supported time,
`advanceUntil`, `drainSupportedEvents`, accepted-no-op, rejected/faulted commands,
execution history, capture basis, incomplete/gap/late-join, half-open interval,
interval closure/finality, retention, replay, and cross-consumer provenance.
Read the exact files for any material hits. Do not assume the old workspace copies of
`docs/research/` are fresher than landed `main`.

**Synchronize safely before editing current-state authority.** This evidence branch may be
behind/diverged from `main`. Use the repository's history-preserving base-normalization
path, preserve every handed-off SHA, resolve genuine content conflicts deliberately, and
verify the resulting net diff against current `main`. If artifact reachability cannot be
preserved, stop with `EVIDENCE PERSISTENCE BLOCKED`; never use a rebase/force-push as a shortcut.

## One bounded reconciliation — normative result

**Primary destination:** `docs/architecture/runtime-contract.md`, in a tightly scoped
section on execution identity, captured supported evidence and interval determinacy.
Avoid a second execution-account document, new entity/type or repeated report narrative.
Translate E1–E8 into current contract terms, with the review's qualifications:

### Identity, boundaries, ordering and authority (E1–E4, E7)

- One simulation execution is the **runtime epoch** identified by its existing opaque
  `RunId`; reset creates another run/sequence epoch and does not render the former
  runtime terminal. An evidence account is evidence *about* a run, not a distinct runtime
  entity, run ID, persisted record or authoritative state.
- Establishment and bounded observations are distinct from any controller decision
  to stop. `QUIESCENT` means no currently pending authoritative work; it is not a
  terminal state or evidence that new commands cannot arrive.
- A supported state-change fact is identified by `(RunId, sequence)`, with strictly
  increasing per-run supported sequence and non-decreasing simulated time along sequence.
  Equal simulated times do not collapse sequence ordering.
- Bind capture claims to an observation basis and the published `ModelFingerprint`,
  plus `ControlledRevisionId` **only when authoritatively provided**. Neither is
  an execution-account identity. Do not create a placeholder Engine-definition ID:
  exact producing-definition provenance is a separate custody/support question.
- Successful authoritative changes are reported by supported events.
  Rejected requests, accepted no-ops, command outcomes and faults can require
  **controller-held provenance**; absence from the supported change stream is not
  proof that they did not occur. Reproduction requires the controller's ordered
  commands **and advancement interleavings/budgets**, model and producing definition,
  not just supported events.
- A fresh observation remains the authoritative **current** state.
  Folding retained supported changes provides a derived view, not an event-sourced
  state authority, model replay or an Engine history ledger.

### Claim-scoped supported-evidence completeness (E5)

Define a checkable vocabulary for a holder's statement: run, **basis observation**
`(S0,T0)`, relevant supported changes after that basis, observed/proved
**frontier** `(Sf,Tf)`, claimed time/sequence range, and any known missing ranges.

- A basis at sequence zero represents capture from execution establishment;
  late-joined capture cannot silently claim earlier history.
- Sequence continuity from the basis to the **relevant proof frontier** is needed.
  A locally contiguous list is not enough to rule out a lost tail without a
  separately known authoritative cursor/frontier. Name missing middle and tail
  ranges; otherwise report completeness unknown, not complete.
- Completeness is **per claim/prefix**. A later gap beyond the proof boundary
  does not revoke an earlier genuinely complete interval, nor must a holder
  possess all subsequent history to justify an earlier claim.
- Two independent destructive drainers cannot each assert full history from their
  partial delivery. One holder capturing and fanning out, or a separately admitted
  retained cursor-addressable facility, is required for shared history.
- A complete captured state-change interval does **not** imply fault-free success,
  complete command/outcome history, sufficient evidence for every analytical
  definition, or deterministic re-execution.

### Half-open interval determinacy and finality (E6)

For a claim about one run over **`[a,b)`**, require (i) a valid state basis at
or before `a`, (ii) gap-free held supported change coverage through the **exact
proof sequence** that establishes finality for that interval, and (iii) a truthful
basis that no later supported change with time **`< b`** can alter its history.

Make the two valid classes of proof and their limits explicit:

1. **Producer-supported temporal evidence.** A genuinely captured supported change
   with simulated time at or beyond `b` (using the prefix just before that first
   change for a half-open claim), or a supported observation with time at or
   beyond `b` and a captured, gap-free cursor through the required prefix,
   can establish temporal finality through the producer's monotonic ordering.
   This is about **supported** time and change order; a requested deadline or
   unobserved internal scheduler time is not such evidence.
2. **Attributable controller closure.** Where supported time remains below
   `b` (idle/quiescent tail), finality can depend on the exclusive driver's
   **scoped commitment** against later effective input inside the interval
   **plus** demonstrated successful exhaustion of relevant pending work,
   post-advancement observation bound to `(RunId, S*, T*)`, and complete
   captured supported changes through that bound sequence.
   The attestor and scope/trust assumption must survive any handover.
   Confinement of the runtime to a closed script can support the commitment.
   An inclusive promise `closedThrough(b)` is stronger than needed for
   `[a,b)`: changes exactly at `b` need not invalidate the earlier interval.

**Prohibitions / qualifications:**

- Neither `advanceUntil(b)` nor exhausting a **count budget** advances the
  simulated clock to `b`, seals the interval, or proves successful exhaustion;
  a call that faults cannot be treated as a clean completion.
- `QUIESCENT` alone does not prove no future commands; a scheduler-internal
  marker/no-op is not an interval-closure witness.
- A controller promise about future inputs is **not a producer-verifiable fact**.
  Absence of later delivered effects does not verify absent rejected/accepted-no-op
  requests; only actually delivered contradictory supported changes may be detected.
  Frozen evidence with a controller attestation remains final under **recorded trust**.
- Do not assume every interval claim is final, or that every accepted controller
  declaration is valid. Unknown/provisional/refused is a legitimate result.
- Do not claim `CapturedAccount`, `HeldAccount`, `ControllerClosure`, or any
  research-local method is a production verifier, canonical type or contract.
- Do not install a D-clock/clock-witness API, interval seal, input floor, special
  `advanceNextIfDueBy` primitive, new session state, or unbounded history owner.
  The Engine adaptation review accepts **no new closure API now** for current
  confined/static consumers; it does not declare such an API globally unsuitable.

**Important separation:** The marker defect has two components—hidden command
time and internal event-count consumption. It is being corrected through the
owner-selected **R-remove** slice. The second adaptation review states clearly that
this defect **does not invalidate conservative E6**; it blocks merely writing
the original report's unqualified “command applies at internal current time”
sentence as normative Engine semantics. This reconciliation must not re-decide
R-remove, endorse E-guard, or assert `T_internal == T_supported` universally.
Any Engine command-time wording must match the actual merged R-remove semantics,
and the R-remove PR itself owns changes to `engine-semantics.md` §§1.1, 1.2, §4
and their conformance tests.

### Delivery and ownership (E8, cross-domain)

- Supported delivery is currently a single destructive drainer; retention is by
  its capture holder or delegate for the horizon its use needs. Do not infer a
  central service, durable execution-record capability, event sourcing, new
  execution-account identifier or physical persistence obligation.
- The runtime contract owns the **shared meaning** of run, sequence, basis,
  evidence coverage and interval finality. Controller owns command admission,
  advancement protocol and attributable promises. Evidence holder owns
  captured coverage/retention and claim qualification. An analytical definition
  owner owns method-specific sufficiency and computation.
  These semantic responsibilities do **not** require one class/module per role.
- State-duration residence inferred from supported changes is not automatically
  one universal “waiting”, “starvation”, “utilization” or causal attribution
  formula. Preserve distinct possible eligibility/availability/readiness
  definitions and explicit analytical method selection.
- Governance owns evidence use, historical attribution and governed conformance,
  not the simulation run's scheduler or capture. Operational continuation and
  its closure/retirement semantics are a separate sibling boundary.
- A retained, shared or cursor-addressable event service stays conditional on a
  **concrete** independent live consumer, late join needing previous history,
  or long-horizon third-party verification—not presumed from E5/E6.

## Smallest durable change set

Prefer a **documentation/specification reconciliation**. Do not change production
Factory runtime behavior, publish new APIs, add a new module, or copy research-local
helpers into product. Use maintained conformance tests only when a concrete existing
runtime invariant needs durable coverage and the R-remove PR is not already
testing that invariant.

Likely owned files:

1. **`docs/architecture/runtime-contract.md`** — primary E1–E8 normative
   result, precise E5/E6 and ownership/retention boundaries. Integrate into existing
   observation/event wording rather than reproduce it in multiple sections.
2. **`docs/research/investigations/simulation-execution-account.md`** —
   update from an active question to the accepted, narrowly scoped decision
   **only together with a complete reconciliation**; preserve material
   reopen triggers as concise current research state, not copied report prose.
3. **`docs/research/research-register.md`** — set the exact question
   `CONCLUDED` and name the durable destination/verdict when that reconciled
   result will actually land. Do not mark `CONCLUDED` in an evidence-only
   checkpoint or a PR missing its canonical consequence.
4. **`docs/research/investigations/simulation-analytics-consumer-boundary.md`**
   — clear/replace the *upstream* execution-account evidence dependency and
   identify that prospective Phase 2 may now resume **after this reconciliation
   lands**. Do not decide analytics owner, formulas, methods or result provenance.
5. **`docs/research/investigations/simulation-analytics-evidence-provenance.md`**
   — narrow the remaining *conditional* analytical questions (definition
   identity, method-specific sufficiency, result provenance and retention when
   evidenced) without making it `READY` just because execution coverage settled.
6. **`docs/architecture/overview.md` / Determinism Contract** — only
   minimal cross-reference or clarification if its current statement otherwise
   misrepresents run/script/advancement provenance. Avoid duplicating E5/E6.
7. **`docs/research/investigations/engine-evolution.md`** — retain its
   existing session/advancement reopening boundary; at most a narrow
   cross-reference to the time-finality trigger if materially needed. The
   marker correction belongs to Engine semantics via the separate R-remove PR.

**Game coordination without scope growth:** The Game diagnostic reconciliation,
not this execution-account PR, owns updating
`docs/planning/factory-design-game-consumer.md` and the vertical-slice gate
for the following **requirement-selection** checkpoints:

- **T1:** promoted live-loop control includes **mid-run commands**; then demand
  a landed safe pacing/command-time rule (R-remove or a truthful documented
  bounded protocol), before playable implementation admission.
- **T2:** commands must take effect at a **selected simulated time while idle**;
  compare D-clock, scheduled time-stamped inputs and event-boundary interaction;
  do not designate D-clock in advance.
- **T3:** a **non-driving party** must establish past-interval finality;
  reassess producer evidence versus scoped controller trust/handover.

Supported concurrent access is an additional separately triggered boundary.
Precomputed recorded inspection or start-fixed runs at any visual playback speed
do not trigger T1–T3. Carry the relevant cross-reference into durable
execution-account reopening text, but do **not** finish Game diagnostic research,
select the live loop or edit Game UI/product code in this PR.

## Two-phase execution without unnecessary waiting

**Phase A — can proceed before R-remove lands.**

- Verify exact reviewed evidence, read current normative owners and identify the
  minimal net documentation change.
- Compose and review E1–E8, especially qualified E5/E6, identity, custody and
  ownership. Prepare the analytics-dependency and research-register edits as part
  of the same coherent final change, not as premature state flips.
- Identify whether any required runtime conformance evidence is already held
  by current tests or the R-remove slice; avoid conflicting test implementations.
- Identify and record **only** the command-time/stepping claims that must be
  checked against the R-remove result. Do not freeze old internal marker
  scheduling as a hidden normative assumption.

**Phase B — authoritative integration/closure.**

- Re-read live `main`, R-remove PR status, and the exact Engine semantics
  once R-remove lands, or establish that a standalone E5/E6 reconciliation is
  truly independent of it. Do not treat an unmerged feature branch as current
  Engine behavior.
- If R-remove is still in flight, a documentation-only E5/E6 PR may proceed
  **only** if its net contract is truthful against the current `main`,
  does not overlap the pending correction, and retains an explicit later
  integration check. Otherwise keep the reconciliation draft in this workspace
  and finish after the R-remove change lands. Avoid artificial waiting where
  the semantics are demonstrably independent, but do not merge inconsistent
  architecture.
- A merged R-remove code/spec correction may intentionally change processed
  internal event lists and budget-stopped interleavings. Ensure the E6
  wording is independent of those old marker details; do not reintroduce
  them through examples, Engine descriptions or assertions.
- Confirm the final net PR changes and register status represent **one**
  accepted execution-account conclusion, not hidden implementation or an
  undocumented new closure guarantee.

## Acceptance cases and review challenges

The independent PR reviewer should be able to trace each normative claim
to current source/tests or the two exact independent reviews. Verify at least
these cases by existing tests, hand-derived proof or a small **maintained**
fixture when warranted; label what was actually executed:

1. **Stale post-advancement frontier:** truthful controller exhaustion and
   no-future-input promise but holder missing the final completion; a
   `[0,100)` active-time claim must be refused until capture covers the
   bound sequence (original review's five-versus-100 falsifier).
2. **Count budget exhausted:** due work remains even though the budget is
   consumed; refusal until successful exhaustion, with faults separate.
3. **Early prefix preserved:** valid complete `[0,5)` remains valid after
   a later capture gap at time 10; broader claims may be refused.
4. **Boundary equality:** a change exactly at `b` is excluded from
   `[a,b)`; equal-time sequence still orders state at `b`.
5. **Late join and lost tail:** a basis after history begins cannot assert
   from establishment; a gap-free local list without a later authoritative
   frontier does not prove its own missing tail.
6. **Two drainers:** split stream histories are not independently complete;
   one holder/fan-out may preserve coverage.
7. **Run reset:** no supported event or closure proof combines old and
   new `RunId`; the old runtime's continuation remains possible.
8. **Contradictory no-op/rejected/faulted command:** a controller's promise
   can be false without any supported change detecting it; observed state
   completeness does not imply complete command/fault provenance.
9. **Idle beyond last supported event:** `advanceUntil(100)` ending at
   tick 13 does not by itself create a producer-final `[0,100)` witness.
10. **Future Game checkpoint:** start-fixed/unbounded recorded playback
    remains unaffected, while a selected mid-run command, idle-time
    command or passive-finality requirement fires T1/T2/T3 **before**
    implementation admission.

Do not paste a large exploratory test suite into product as an archive.
Retain small executable falsifiers where they constrain actual supported
Engine behavior; preserve E5/E6 caller-coverage limits normatively when no
production API exists to exercise them. Never promote the research helpers
unchanged or claim they certify controller honesty.

## Knowledge-transfer audit and independent delivery

Before any final PR review, perform the §10 **knowledge-transfer audit**:

- Map every accepted E1–E8 conclusion and **all qualifications** into its
  owning durable authority; explicitly check the first and second
  `ACCEPT WITH QUALIFICATIONS` reviews, not only the original reports.
- Preserve only useful counterexamples/positive conformance or minimum
  truth rules in maintained documentation/tests; classify experiment helpers,
  temporary source maps, draft alternatives and self-challenges as
  **promoted, intentionally retained, or explicitly discarded**.
- Assess the historical rationale retention test for the significant
  no-new-account/no-central-custody choice. If warranted, create one concise
  **non-normative** record under `docs/history/decisions/` with serious
  alternatives, accepted trade-offs and material reopening triggers.
  Otherwise record that no separate rationale record is needed.
- Assess any qualifying cross-investigation synthesis seed against
  `docs/development/researching.md`; do not invent a standing research track.
- The reconciliation must leave downstream analytics ownership `READY`
  until its **own** Phase 2 review and reconciliation. Game diagnostic remains
  separately owned; no premature `CONCLUDED` for that question.
- Current maintained files must not require continued access to original
  `workspace/` reports or exact temporary SHA+path pairs after retirement.
  The final PR body may cite evidence coordinates for *active* review.
- Remove **all tracked `workspace/` files** (including this handoff and
  original research artifacts) from the final PR tree without rewriting
  their commits in history. Run the tracked-workspace checker. The evidence
  branch remains active until the reviewed reconciliation lands and
  retirement conditions hold.

Use one coherent reconciliation PR from this workspace into current `main`,
unless an unavoidable, demonstrated independence warrants a prior documentation
PR. A final change may be documentation-only; that is not a failure to deliver.
Use the repository PR template and include summary, semantic/compatibility
implications, non-goals and remaining work, with no transient statuses.
Run applicable documentation/link/source-authority/workspace checks and any
tests for changed product evidence. Use the normal exact-head CI gate and
**independent PR Reviewer**. This implementation/reconciliation agent does
not issue its own disposition and **never merges**.

If a reviewer finds that a proposed normative guarantee is stronger than
the two qualified reviews support, **narrow it or stop**; do not silently
commission a new broad research investigation. A genuine newly required
load-bearing conclusion must be researched and independently reviewed
separately before promotion.

## Stop and return contract

After performing the assigned reconciliation, return only:

- reconciliation PR number/link, workspace branch and exact final head SHA;
- live-main baseline used, and whether the R-remove correction was merged
  or still in flight at the point of final validation;
- durable files changed and precise E1–E8 / E5–E6 rules reconciled;
- qualifications incorporated from each independent review, especially
  capture frontier, controller trust, prefix coverage and Game triggers;
- explicit lack of new Engine/API, history service, central custody and
  analytical-definition ownership;
- knowledge-transfer disposition and proof that no `workspace/` files
  remain in the PR's net merge tree;
- checks/tests and exact-head CI result; base freshness/mergeability;
- independent reviewer disposition or concrete remaining blocker;
- whether execution-account research can become `CONCLUDED` on merge,
  and the specific already-READY analytics Phase 2 continuation enabled.

Do not paste the full reconciliation report or prompt into chat. Deliver
the coherent durable PR and its factual status. **Do not merge.**
