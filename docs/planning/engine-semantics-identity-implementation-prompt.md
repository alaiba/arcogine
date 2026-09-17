# Implementation prompt — establish Engine semantics identity

Implement the next bounded Engine slice: **PLAN-ENG-5-B1 — Engine semantics identity and runtime establishment**.

This prompt is intentionally stored on a dedicated implementation branch. Work on the branch this file is already on. Per `AGENTS.md`, do not switch to another branch merely because a prompt names one; if the checkout branch does not match the branch carrying this prompt, report the mismatch instead of silently switching.

At prompt creation time, live `main` is `d36d9ec5b0591803da2e1803c6ae01467fc0e483`, and PLAN-ENG-5-0 is already implemented on `main`. Re-resolve live `main` and relevant open PR state before editing; do not treat this recorded SHA as permanently current.

## Read first

Before changing code, read:

1. `AGENTS.md`
2. `.github/agents/work-planner.agent.md` far enough to preserve the named-slice handoff boundary
3. `docs/planning/spatial-runtime-consequences.md`
   - especially `PLAN-ENG-5-B1 — Engine semantics identity and runtime establishment`
   - the dependency/parallelism map
4. `docs/planning/factory-simulation-engine-readiness.md`
5. `docs/architecture/decisions/0015-engine-semantics-identity-and-reproducibility.md`
6. `docs/architecture/engine-semantics-v1.md`
7. `docs/architecture/decisions/0011-runtime-observation-and-event-contract.md`
8. `docs/architecture/overview.md`
9. `docs/development/testing.md`
10. `docs/development/reviewing.md`

Then inspect the current implementation around:

- `product/domains/factory/src/main/java/com/arcogine/factory/process/FactoryRuntime.java`
- `product/domains/factory/src/test/java/com/arcogine/factory/process/`
- `product/types/src/main/java/com/arcogine/types/`
- `product/types/src/test/java/com/arcogine/types/`
- `product/domains/factory/build.gradle.kts`
- `product/settings.gradle.kts`

Do a quick repository search before designing the change. At minimum search for:

```text
FactoryRuntime
RunId
ModelFingerprint
ControlledRevisionId
EngineSemanticsVersion
RuntimeObservationMetadata
RuntimeEventEnvelope
engine-semantics:v1
reset()
forModel(
```

Use the search to verify current ownership and consumers rather than assuming the type belongs in a package from this prompt alone.

## Current landed baseline

The prerequisite slice PLAN-ENG-5-0 is landed. Its conformance suites now pin the authoritative `engine-semantics:v1` behavior, including dispatch/recovery/ranking and derived-result arithmetic. That means this slice may introduce the identity for those already-defined semantics without reopening policy selection.

ADR-0015 is Accepted. Its key contract is already decided:

- `EngineSemanticsVersion` is the semantic identity of the complete result-affecting Engine interpretation for a run;
- it is distinct from `ModelFingerprint`, `RunId`, and software/build identity;
- one semantics version is fixed for one run and cannot change mid-run;
- the initial implementation supports exactly one current version;
- callers do not select among historical versions yet;
- implementations explicitly declare supported versions and fail on unsupported identities;
- `FactoryRuntime` exposes its fixed semantics version directly;
- observation/event provenance propagation is mandatory architecture, but is deliberately the following PLAN-ENG-5-B2 implementation slice rather than this one.

No open implementation PR for PLAN-ENG-5-B1 was found when this prompt was created. Re-check before implementation and do not duplicate in-flight work if that has changed.

## Objective

Establish Engine semantics identity as a first-class runtime invariant without pulling runtime provenance propagation or multi-version execution into this slice.

The required result is:

```text
FactoryRuntime
  model identity      -> existing FactoryModelVersion / ModelFingerprint path
  run identity        -> existing RunId
  semantics identity  -> fixed EngineSemanticsVersion = engine-semantics:v1
```

A fresh runtime and a reset runtime may have different `RunId` values, but both must report the same fixed Engine semantics version while the implementation supports only `engine-semantics:v1`.

## Required responsibility

Implement exactly the B1 responsibilities from the maintained plan:

1. add first-class `EngineSemanticsVersion`;
2. establish the one supported current value `engine-semantics:v1`;
3. fix that identity when a `FactoryRuntime` is established;
4. expose it directly from `FactoryRuntime`;
5. provide a narrow support check that fails explicitly for an unsupported identity;
6. do **not** add caller-selectable historical versions or a multi-version resolver.

Prefer the smallest representation consistent with existing Arcogine identity types. Reuse repository conventions for immutable value types, validation, equality, string rendering, and tests rather than inventing a new framework.

## Semantic invariants

Preserve these distinctions strictly:

### Engine semantics identity is not model identity

`ModelFingerprint` answers which authored Factory design was executed.

`EngineSemanticsVersion` answers which complete result-affecting Engine interpretation was used.

Do not derive one from the other, embed one inside the other, or change Factory fingerprint semantics in this slice.

### Engine semantics identity is not run identity

`RunId` is opaque correlation identity for one runtime epoch. A reset/fresh runtime gets a fresh `RunId` but retains the same supported `EngineSemanticsVersion`.

Do not make semantics version part of `RunId` generation or equality.

### Engine semantics identity is not build identity

Do not use application version, package version, Git SHA, classpath metadata, or build information as the semantics identity. The durable value is the semantic identifier `engine-semantics:v1` established by ADR-0015 and the normative v1 specification.

### Version is immutable for the runtime lifetime

A `FactoryRuntime` must not be able to change semantics version after construction. Avoid setters, mutable registries, or session commands that can swap versions.

### One-version support only

The initial implementation supports exactly one executable semantics version. The support seam should make unsupported identity failure explicit, but it must not become a speculative resolver/registry/plugin framework.

If the cleanest implementation needs a support predicate or `requireSupported(...)`-style guard, keep it narrow and deterministic.

## Placement and dependency guidance

Determine the correct package/module from current dependency direction before coding.

`EngineSemanticsVersion` is Engine-owned semantic identity, but it will be consumed by runtime/provenance surfaces across modules in later slices. Follow existing Arcogine identity placement patterns and module dependency rules; do not introduce an upward or circular dependency merely to place the type near `FactoryRuntime`.

If current architecture evidence makes the expected placement ambiguous, stop and surface the boundary question rather than creating a new shared-identity abstraction or moving unrelated types.

Do not create a generic `Version`, `SemanticVersion`, policy registry, provenance framework, or cross-domain identity superclass.

## Runtime establishment

`FactoryRuntime.forModel(...)` currently creates a fresh runtime from a published model, and `reset()` creates another fresh runtime over the same model.

Integrate semantics establishment at that runtime-construction boundary so that:

- every supported `FactoryRuntime` has exactly one non-null semantics identity;
- the current implementation establishes `engine-semantics:v1` automatically;
- callers are not required to choose it;
- callers cannot change it after establishment;
- `reset()` preserves the semantics version while creating a fresh run identity;
- a headless caller can read the runtime's semantics version directly without calling `observe()` or draining events.

Do not alter dispatch, scheduling, work decomposition, transfer behavior, command semantics, or model assembly merely to attach the identity.

## Unsupported identity seam

ADR-0015 requires an implementation to declare what it executes and to fail explicitly on an unsupported semantics version.

Add the smallest useful support seam that proves this requirement without pretending the runtime supports historical selection.

Evidence must demonstrate that an unsupported `EngineSemanticsVersion` is rejected explicitly if the support seam is exercised.

Do not add:

- a list of historical implementations;
- dynamic lookup by string;
- service loading;
- plugin registration;
- version negotiation;
- fallback-to-current behavior;
- silent coercion of an unknown value to v1.

Unknown/unsupported must stay distinguishable from current-supported.

## Required executable evidence

Add or extend tests that prove the B1 acceptance evidence, not merely object construction.

At minimum prove:

1. one fresh runtime reports exactly one immutable semantics version;
2. that version is `engine-semantics:v1`;
3. a reset/fresh runtime receives a different `RunId` while retaining the same semantics version;
4. two fresh runtimes over the same model report the same supported semantics version independent of their run identities;
5. unsupported identity fails explicitly through the support seam;
6. no caller-selectable multi-version execution path has been introduced.

Use semantic test/class/file names. `PLAN-ENG-5-B1` is a planning coordinate and may appear in this planning prompt, PR text, and commit history, but it must not become durable Java type names, test names, code comments, or non-planning documentation terminology.

Do not duplicate the large PLAN-ENG-5-0 conformance suite. B1 depends on those fixtures; it does not need to restate them.

## Non-goals

Do not implement PLAN-ENG-5-B2 in this PR.

Specifically, do **not** yet add `EngineSemanticsVersion` to:

- `RuntimeObservationMetadata`;
- `RuntimeEventEnvelope`;
- API DTOs;
- SSE payloads;
- frontend types/projections.

Also do not implement:

- `ControlledRevisionId` observation propagation;
- Factory Model V2 canonical identity;
- Factory V1→V2 policy evolution;
- transfer arithmetic;
- admission reservation;
- runtime `TRANSFERRING` state;
- transfer events/observations;
- scheduling-policy changes;
- multi-version historical execution;
- semantics retirement procedures;
- cross-version comparison policy.

B2 is the deliberate provenance propagation slice after B1. Keep that boundary intact even though ADR-0015 describes the final combined architecture.

## ADR / decision rule

This implementation should not require a new ADR: ADR-0015 already decides the identity, lifecycle, and one-version initial support contract.

If implementation reveals a hard-to-reverse choice that ADR-0015 and current architecture do not decide — for example a new persistence format, public wire encoding, caller-controlled version negotiation, or a generic cross-domain version registry — do not choose it opportunistically. Stop and surface the decision boundary.

Do not rewrite ADR-0015 in place. Accepted ADR history is protected; a semantic change would require a new/superseding decision.

## Documentation reconciliation

If the implementation satisfies the complete B1 acceptance evidence, update planning/current-state documentation so the repository is truthful if the PR merges by itself.

At minimum inspect and reconcile:

- `docs/planning/spatial-runtime-consequences.md` — mark B1 implemented and update dependency/parallelism wording accordingly;
- `docs/planning/factory-simulation-engine-readiness.md` — reflect that Engine semantics identity is established and identify newly unblocked work without claiming B2 is complete;
- `docs/architecture/overview.md` — update only if its current-state description would otherwise become stale after the implementation lands.

Do not mark runtime provenance propagation complete until B2 lands.

Do not leak planning coordinates into durable architecture/code vocabulary. In architecture/current-state docs, describe the semantic capability itself: fixed Engine semantics identity, `EngineSemanticsVersion`, and `engine-semantics:v1`.

## Validation

This should be a Java/backend-only slice unless the implementation unexpectedly crosses a boundary that it should not.

Run focused tests first, including the exact module tests that cover the new identity and runtime establishment.

Then run the Java gates required by `AGENTS.md`:

```bash
cd product
./gradlew compileJava compileTestJava checkstyleMain checkstyleTest test jacocoTestReport jacocoTestCoverageVerification
```

If documentation changes, also run the repository-owned documentation checks relevant to the touched files, including:

```bash
python3 .github/scripts/check-delivery-labels.py
python3 .github/scripts/check-markdown-links.py .
```

Also run `git diff --check` before completion.

On Windows, follow the execution-environment order in `AGENTS.md`: devcontainer, generic Docker container, WSL/Git Bash, then supported native tooling. Backend validation requires JDK 21+ and the repository Gradle wrapper. Never substitute a global Gradle installation for `product/gradlew`.

Report every validation command actually run and its result. If a required validation cannot run, state exactly what was unavailable and why; do not summarize partial validation as a full pass.

## PR lifecycle

Keep implementation and independent review separate.

When implementation is complete:

1. inspect the full branch diff against current live `main` for scope drift;
2. commit with the human repository owner's configured Git identity;
3. push the current branch;
4. open or continue the implementation PR against `main`;
5. keep the PR description truthful about the exact current head, tests, scope, and non-goals;
6. hand the current head to the independent PR Reviewer workflow;
7. do not merge the PR — the repository owner performs the final manual merge.

Do not add AI/model/provider attribution, generated-by footers, session URLs, or bot trailers to commit messages or GitHub bodies.

A suitable PR title is:

`feat(factory): establish engine semantics identity`

The PR description should state clearly that this slice establishes the fixed runtime identity only; observation/event provenance propagation remains the next slice.

## Completion report

When finished, report:

- live `main` SHA used for final comparison;
- branch name;
- commit SHA(s);
- PR number/link;
- final placement and representation of `EngineSemanticsVersion`;
- how `engine-semantics:v1` is established;
- how `FactoryRuntime` fixes and exposes the identity;
- unsupported-version failure behavior;
- reset/fresh-runtime evidence showing fresh `RunId` with stable semantics version;
- production/test/docs files changed;
- exact validation commands and outcomes;
- anything unavailable or only partially validated;
- any architecture contradiction or unplanned decision encountered;
- current PR lifecycle state and next owner/action.

The slice is complete when one-version Engine semantics identity is explicit, fixed for every runtime, directly readable, explicitly support-checked, executably evidenced, and planning/current-state documentation is truthful — without pulling B2 provenance propagation or multi-version execution into the change.
