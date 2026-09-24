# Engine Semantics v2

Status: Normative interpretation contract; execution not implemented. No runtime establishes this
identity yet; publication of `factory-model:v2` and spatial-runtime activation remain separately
gated by their own implementation prerequisites (see [Spatial Runtime Consequences](../planning/spatial-runtime-consequences.md)).
Semantic identity: `engine-semantics:v2`
Rationale: [Determinism Contract](overview.md#determinism-contract); reconciles the
[Engine applicability to optional-record Factory policies](../research/investigations/engine-evolution.md#concluded--engine-applicability-to-optional-record-factory-policies)
research question.
Evolution rule: [Semantic evolution and support](overview.md#semantic-evolution-and-support)
Predecessor: [Engine Semantics v1](engine-semantics-v1.md) — unchanged; continues to execute only
`factory-model:v1`.
Model-side counterpart: [Factory Model v2 Canonicalization](factory-model-v2.md), both the spatial
record present and absent forms of that reconciled policy.

## 1. Purpose and applicability

`engine-semantics:v2` is the distinguishable Engine interpretation identity that applies to
`factory-model:v2` artifacts. It does not amend, extend, or same-label-correct
`engine-semantics:v1`; it is a new identity introduced because the affirmative burden of showing
that `engine-semantics:v1` already admits `factory-model:v2` — with the spatial record either
present or absent — was not met (see the reopening condition in §7).

Applicability is exact:

- **Factory policy:** `factory-model:v2` only. `factory-model:v1` artifacts are never executed
  under `engine-semantics:v2`; they continue under `engine-semantics:v1`.
- **Represented content:** both complete forms Factory's own validation admits — the spatial
  record absent, and the spatial record present and complete. `engine-semantics:v2` defines no
  third, partial, or geometry-only form, because [Factory Model v2](factory-model-v2.md) §1.1
  admits none.
- **Refuses:** any artifact whose Factory policy is not `factory-model:v2`; any `factory-model:v2`
  representation Factory itself rejects as invalid (incomplete layout, overlapping footprints,
  out-of-range placement, an unrepresentable maximum-transfer duration, or a production-predicate
  violation); and any future Factory policy not explicitly named in this specification. Refusal
  occurs before runtime mutation, on the same terms as an unsupported `EngineSemanticsVersion`
  request.

This specification defines a single closed applicability domain. It does not select `v2` as a
numbering convention that must track `factory-model` version numbers: a successor Engine identity
may in principle support more than one Factory policy, or a future Factory policy may need no new
Engine identity at all, and neither is decided by this document for any case beyond the one it
names.

## 2. Reused definitions

To avoid duplicating rules and risking silent divergence between two texts that must agree,
`engine-semantics:v2` reuses the following [Engine Semantics v1](engine-semantics-v1.md) sections
by explicit stable reference, with their meaning and behavior fully preserved and unchanged:

- §1.2 Session and control semantics;
- §2 Resource-selection and dispatch semantics;
- §3 Unit-work decomposition semantics;
- §4 Scheduler and dispatch-cascade ordering;
- §§5-9 spatial model/Engine ownership boundary, destination selection and binding, transfer
  timing, the transfer runtime state machine, and capacity/availability semantics — applied only
  when the spatial record is present (§3 below);
- §10 KPI and derived-result interpretation, including §10.1's derived-result arithmetic and
  §10.2's accumulator register.

A future change to any referenced v1 section changes both identities' behavior identically, because
both point to the same rule text. If a future need arises to change `engine-semantics:v2`'s
behavior for a rule it currently shares with v1 — without changing v1 — that divergence itself
requires copying the rule out of shared reference and giving `engine-semantics:v2` its own
distinguishable text, under the same whole-definition-fixation and non-rebinding obligations this
specification is itself subject to once it has attributed records.

## 3. Spatial-absent behavior

When the spatial record is absent, `engine-semantics:v2` executes exactly the reused non-spatial
rules of §2 above: current dispatch, decomposition, scheduling and derived-result arithmetic, with
no transfer state, no `TRANSFER_STARTED`/`TRANSFER_COMPLETED` events, no reservation of destination
admission capacity, and no synthesized floor, position, `ticksPerCell`, or `handlingTicks` value.

This produces a production result equivalent to executing the same production content under
`engine-semantics:v1` against a `factory-model:v1` artifact — same dispatch order, same completion
times, same derived KPIs — while retaining the artifact's actual `factory-model:v2` (spatial-absent
form) `ModelFingerprint`. The fingerprint is never rewritten, dropped, converted to a
`factory-model:v1` fingerprint, or otherwise substituted; equivalent production outcome under two
different Factory policies is a comparison a consumer may draw explicitly, never an identity
Arcogine collapses on the artifact's behalf.

## 4. Spatial-present behavior

When the spatial record is present and complete, `engine-semantics:v2` executes exactly the reused
spatial rules of §2 above against the artifact's own authored floor, position, footprint,
`ticksPerCell`, and `handlingTicks` values, including the zero-magnitude case: authored zero
`ticksPerCell`/`handlingTicks` still produces the scheduled `TRANSFER_STARTED`/`TRANSFER_COMPLETED`
turn that [Engine Semantics v1](engine-semantics-v1.md) §7 rule 9 defines, and must not be
collapsed into the absent-record behavior of §3. Absence and authored zero remain distinct
authored content with distinct transition semantics, exactly as Factory Model v2 §1.1 requires.

## 5. Provenance

- Every run established under `engine-semantics:v2` reports that exact identity in runtime
  provenance — never a silent fallback to `engine-semantics:v1` for an unsupported request, and
  never a synthesized identity when none was actually established. This is the same mandatory
  metadata obligation [Engine Semantics v1](engine-semantics-v1.md) §11-12 already carries, and
  currently has the same propagation gap the [runtime contract](runtime-contract.md) records for
  v1.
- The artifact's actual `factory-model:v2` `ModelFingerprint` — spatial-present or spatial-absent —
  is preserved unchanged and is the only fingerprint a run executed under `engine-semantics:v2` may
  report.
- A `ControlledRevisionId` is carried only when actually, authoritatively bound, on the same terms
  the [runtime contract](runtime-contract.md) already states; `engine-semantics:v2` synthesizes
  none.

## 6. Relationship to `engine-semantics:v1`

`engine-semantics:v1` is unchanged by this specification. Its already-fixed definition, including
its unexercised spatial sections, remains resolvable at its historical meaning and continues to
apply only to `factory-model:v1`. `engine-semantics:v2` is a distinguishable identity, not an
amendment, extension, or same-label correction of v1; nothing here rebinds any historical v1
attribution, reinterprets a past v1 result, or grants v1 a new applicability domain.

A present-only alternative — stating `engine-semantics:v1` itself applicable to spatial-present
`factory-model:v2` artifacts while leaving the spatial-absent case to a distinguishable identity —
was examined and left unproven rather than disproven: the reused spatial facts and their
interpretation survive at the fact level, but full policy/reference/refusal preservation for that
narrower claim was not established. This specification does not adopt that hybrid partition. It
remains a candidate a future correction could still attempt for `engine-semantics:v1` itself,
separately from anything `engine-semantics:v2` defines here; adopting it later would not change
`engine-semantics:v2`'s own applicability to the spatial-absent case, which is this specification's
load-bearing new admission regardless of how the spatial-present case is eventually attributed.

## 7. Reopening condition

Discovery of an Engine-owned, whole-definition-preserving admission rule that shows
`engine-semantics:v1` already entailed applicability to this exact `factory-model:v2` case —
spatial record present or absent — would justify re-examining whether `engine-semantics:v2` was the
correct route for that case. A new fixture or implementation that merely assumes such a rule is not
evidence that the rule was already part of v1's historical definition. This specification does not
itself reopen the Factory composition question; if a measured successor implementation or support
cost materially changes the relative merit of the optional-record `factory-model:v2` boundary, that
is the Factory composition reopening trigger recorded in
[Factory Model Semantic Composition](../research/investigations/factory-model-semantic-composition.md),
weighed separately from this Engine identity choice.

## 8. Support, conformance, and non-goals

This specification defines the identity, its applicability, and its reused/absent-case rules. It
does not itself:

- implement publication, canonical encoding, or verification of `factory-model:v2`, owned by
  [PLAN-ENG-5-A2](../planning/spatial-runtime-consequences.md#plan-eng-5-a2--factory-v2-canonical-identity);
- implement runtime establishment of `engine-semantics:v2`, observation/event provenance
  propagation, the admission-reservation substrate, or spatial transfer activation, owned by the
  still-gated PLAN-ENG-5 slices in [Spatial Runtime Consequences](../planning/spatial-runtime-consequences.md);
- declare that any of the above is currently supported. A support declaration for
  `engine-semantics:v2`, made under [Semantic contract support](../development/semantic-contract-support.md),
  follows when that implementation lands; this specification alone is not that declaration.

Before `engine-semantics:v2` is considered released, conformance fixtures must prove, at minimum:

1. a Factory-valid but Engine-unsupported policy or represented variant is refused before runtime
   mutation, distinctly from an invalid Factory artifact;
2. the spatial-absent and present-with-legal-zero cases produce distinct transitions, per §§3-4;
3. the reused non-spatial ordering, ranking, decomposition, and derived-result arithmetic of §2 are
   preserved exactly, with no result change from the equivalent `engine-semantics:v1` behavior;
4. the reused transfer, reservation, and offline-arrival interactions of §2 are preserved exactly;
5. the selected identity is established on the runtime and propagated through supported
   observations and events;
6. the selected identity is preserved through `reset()`;
7. the actual `factory-model:v2` fingerprint is preserved and never substituted; and
8. Factory-owned invalid-content, canonical-byte, strict-decoding, and publication-predicate
   rejection remain Factory's obligations, not duplicated or weakened here.

Factory publication/decoding fixtures, golden vectors, and cross-policy separation tests remain
owned by [Factory Model v2 Canonicalization](factory-model-v2.md) §11, not by this specification.
