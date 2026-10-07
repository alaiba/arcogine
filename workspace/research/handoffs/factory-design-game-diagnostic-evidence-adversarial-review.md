# Handoff: independent adversarial review of the game diagnostic-evidence report

Perform an **independent adversarial research review** of a completed research report, operating as
the repository's **Researcher** in adversarial-review mode.

## Operating contract

Read and follow the live-`main` versions of:

- `AGENTS.md`;
- `.github/agents/researcher.agent.md` (mode: *Adversarial research review*);
- `docs/development/researching.md`, especially §2 (grounding), §5 (external evidence), §7 (risk),
  §9 (what "adversarial" means, report input integrity, independence and anchoring control, and the
  dispositions) and §10 (custody).

Where this handoff and those documents could be read to disagree, those documents control.

## Independence

- This review must not be performed by the session that wrote the report.
- Prefer a different model family or person. At minimum, run it as a fresh, isolated session with no
  responsibility for preserving the report's conclusions.
- State plainly in the review which independence condition was actually achieved.
- Sharing the workspace branch neither establishes nor weakens independence.

## Report under review (resolve before substantive work)

| Field | Value |
|---|---|
| Workspace branch | `workspace/factory-design-game-diagnostic-evidence` |
| Report commit | `4ad00136441fa29e6b462ac5431b60cf2de1e148` |
| Report path | `workspace/research/investigations/factory-design-game-diagnostic-evidence-report.md` (revision 3, consolidated) |
| Report's stated research baseline | `9e9e4678b18286cea83387b5ffc38741640f0db5` |
| Report's final recheck | `306205c33ef93f0433a26c92f85cbd82eadd27f8` |

Review the report **at that exact commit**, not the branch tip. If you cannot resolve the complete
report at that commit, stop with `INPUT BLOCKED — ORIGINAL REPORT NOT AVAILABLE`. Revisions 1
(`bb37e2598d2684f7dc47b6db75884b798a0ae040`) and 2 (`e5debf2584b2a3096115c2ed8b117e2ac653cb31`) are
superseded history: read them only as evidence of what changed.

Resolve **live `main` anew** and record its SHA as your review baseline. If `main` has moved
materially since `306205c33ef93f0433a26c92f85cbd82eadd27f8`, assess whether the report's conclusions
still hold.

## Anchoring control: the order of work

Before reading the report's executive conclusion or recommendations in depth:

1. **Read the question framing:**
   - the brief at live `main`: `docs/research/investigations/factory-design-game-diagnostic-evidence.md`;
   - its research-register row;
   - the owner decisions in the box below.
2. **Re-ground in current repository authorities:**
   - `docs/architecture/overview.md`, `engine-semantics.md`, `runtime-contract.md`;
   - the game consumer and vertical-slice planning documents;
   - `docs/research/investigations/factory-design-game-strategy-space.md`;
   - the simulation-analytics consumer-boundary brief;
   - the supported runtime observation and event types, and the `product/research-experiments`
     substrate.
3. **Independently reconstruct** the major constraints, the plausible candidate answers (including
   negative and no-new-abstraction ones), and the likely failure cases.
4. **Only then** read the report, and attempt to falsify its load-bearing conclusions.

> **Owner decisions** (question framing, recorded in the workspace discovery notes; not conclusions to
> defend):
>
> - **Goals.** The game's diagnostic purpose: a player can understand a recorded run, learn the
>   production concepts it exhibits (industry jargon introduced in context and explained on demand;
>   learning by experiment), and check hypotheses against pre-computed alternatives.
> - **Static only.** A live simulation loop is out of scope and belongs to a separate future question.
> - **Text UI is acceptable** for testing concepts. Medium choices (animation, Gantt charts,
>   dashboards) are out of scope.
> - **Refined question adopted** for later reconciliation into the brief: given recorded runs of
>   non-spatial factory designs, what must a player be able to inspect, and what may the game claim, so
>   that the player can understand a run, learn the production concepts it exhibits, and check
>   hypotheses against alternatives, with every claim traceable to supported facts or a stated
>   definition, and no claim stronger than its evidence?
> - **Owner gate redefinition adopted.** The owner smoke test runs on a presentation prototype, not a
>   statement list.
> - **Busy counter.** The owner decided to remove the engine's busy counter (`busyTicks`) as part of
>   this research's reconciliation.
> - **Verdict accepted.** The owner accepted the report's provisional verdict and stopped iterating.
>   Owner acceptance is not adversarial review and does not substitute for it.

## Load-bearing claims to challenge

Challenge at least these. Use the failure modes in researching.md §9: omitted candidate, a proving case
that breaks the model, hidden assumption, ownership inversion, stale baseline, misattributed source,
misleading analogy, possibility treated as necessity, over-generalization, a question prematurely
settled, a conclusion stronger than its evidence.

1. **Provisional positive resolution.** Is the surface it describes truthful *and* usable, and is the
   evidence sufficient for the decision at stake? The surface is guardrails, pull-based inspection,
   named characterizations, experiments by variant, and a mission teaching pattern.
2. **Guardrails.** Do the "never present as fact" counterexamples hold? Can you craft a misleading
   statement that the six audits would pass, or find an allowed claim that overstates?
3. **The reversed analytics conclusion.** Utilization over a period, and the flow characterization of
   idle slots (starved / no work left), are classified as *concrete reusable measurement candidates*.
   Is the reuse need demonstrated, or is it possibility treated as necessity? Could they be game-local
   presentation instead? Are the basis choices (half-open periods, per slot, in-progress work to the
   boundary, per machine versus per pool, "known work only") material and complete?
4. **Platform requirements and ownership.** Are per-step timing (retained events versus a consumer
   recording observations after each advance), machine identity stability and design comparison
   correctly assigned to runtime and Factory Design, rather than the game, and not inverted?
5. **Busy-counter removal.** Is the evidence against `busyTicks` as utilization sound? Is the removal
   scope complete and correctly risk-classified? Must a replacement exist before removal?
6. **The two-layer model.** Machine slot state (a direct fact) versus flow characterization (derived).
   Are the definitions correct under current Engine semantics, including multi-slot machines and
   multi-eligible steps, and the rejection of "idle while a unit waits for it"?
7. **The decision not to iterate on bottleneck.** Is handing the named method to the analytics research
   and the teaching to a follow-up question sound, or is the central diagnostic concept left
   prematurely settled?
8. **Owner-walkthrough evidence weight.** Single person; co-designed scenarios and missions; had seen
   the first pass's key. Is the report's asymmetry (failure falsifies, success only smoke-tests)
   respected throughout?
9. **Mission and oracle correctness.** Verify independently, by hand or by rerun, that the official
   answers and the pre-registered values are right, and that no mission's official answer is wrong or
   ambiguous.
10. **Scope boundaries.** Consider especially costs: "needed" is time-only, and "worth it" belongs to the
    Challenge layer's evaluation. Also the non-spatial, release-at-once, single-product, always-online
    scope, and orders arriving over time. Are they stated where they bound a conclusion?

## Evidence you may use

These are all on the workspace branch. Read them as evidence after step 3 of the anchoring sequence.

- **Pre-registered oracles:**
  - `…-oracle.md` (first pass, commit `010b84a4e38113766af2cd594135825296a0ef3a`);
  - `game-diagnostic-walkthrough-2-oracle.md`, with its disclosed amendment;
  - `game-diagnostic-inspection-missions-oracle.md` (commit `051110921b051e907781c76046d05df15274b070`).
- **Experiment sources:** `workspace/research/experiments/game-diagnostic-evidence/`. This includes
  `GameContracts`, `GamePlainContract`, `GameWalkthroughPack` and `BakeryInspectionPack`.
- **Generated results:** `game-diagnostic-evidence-results/`, `game-diagnostic-walkthrough-2/` and
  `game-inspection/`.
- **Viewers:** `workspace/research/experiments/game-diagnostic-walkthrough/walkthrough.mjs` and
  `workspace/research/experiments/game-diagnostic-inspection/inspect.mjs`.
- **Owner-session records:**
  - `factory-design-game-diagnostic-evidence-walkthrough-record.md` (first pass);
  - `factory-design-game-diagnostic-evidence-discovery-notes.md` (owner direction, the paused second
    pass, the busy-counter history, the cost boundary, closure decisions);
  - `game-inspection-walkthrough-record.md` (the bakery session).

**Rerun** (JDK 21 is the compatibility floor), from `product/`:

```text
./gradlew :research-experiments:test \
  -PresearchExperimentSources=<repo>/workspace/research/experiments/game-diagnostic-evidence
```

- The expected outcome at the report commit is 156 tests and 0 failures.
- Outputs go to `product/research-experiments/build/{game-diagnostic-evidence,game-diagnostic-walkthrough,game-inspection}/`.
  Compare them byte for byte with the committed counterparts.
- On a Windows host without JDK 21, follow `docs/development/testing.md` (the documented Docker
  workflow). Run it against a scratch export of the commit (`git archive`) rather than the live
  worktree, so root-owned build output does not break the devcontainer.

You may run the viewers yourself, for example with
`node …/inspect.mjs --answers=<scratch path>`. Never write to the owner's `logs/` answers files.

## Output

Write the review to:

`workspace/research/investigations/factory-design-game-diagnostic-evidence-adversarial-review.md`

It must state:

- your live-`main` review baseline;
- the reviewed report's exact commit and path;
- the independence condition actually achieved;
- for each challenged claim: what was challenged, the evidence considered, the result, and the effect
  on the conclusion;
- any qualifications that must survive reconciliation;
- exactly one disposition: `ACCEPT`, `ACCEPT WITH QUALIFICATIONS`, `MORE EVIDENCE REQUIRED` or
  `REOPEN`.

A clean `ACCEPT` is a valid result. Do not manufacture findings, and do not suppress real ones.

Commit the review alone, in the same workspace branch, using the repository owner's human Git identity
(verify `git config user.name` and `user.email`). Do not add tool or model attribution trailers. Push
it, and return:

- workspace branch;
- review commit SHA;
- review path;
- reviewed report commit SHA;
- live-`main` review baseline.

## Do not

- modify the report, the oracles, the experiment sources, product code, or any `docs/` file;
- perform reconciliation (brief, register, planning, architecture, busy-counter removal);
- rebase or force-push the workspace branch, or rewrite any handed-off commit;
- merge the workspace;
- paste the full review into chat after it is persisted.
