# Engine applicability to optional-record Factory policies

> **Date:** 2026-09-24
>
> **Authority:** Historical, non-normative evidence; current meaning lives only in the documents it was reconciled into
>
> **Reconciled into:** [Engine Semantics v2](../../architecture/engine-semantics-v2.md), [Factory Model v2](../../architecture/factory-model-v2.md#12-publication-validity-is-not-engine-applicability), [Engine Evolution Research](../../research/investigations/engine-evolution.md#concluded--engine-applicability-to-optional-record-factory-policies)

## Decision

Arcogine chose a distinguishable Engine semantics identity, `engine-semantics:v2`, to execute
artifacts of the reconciled `factory-model:v2` policy — both with its optional spatial record
present and with it absent — rather than stating that the already-fixed `engine-semantics:v1`
definition already applies to them. `engine-semantics:v1` is left completely unchanged and
continues to execute only `factory-model:v1`.

## Context

- `factory-model:v2` had just been corrected from mandatory spatial facts to an optional, complete
  spatial record, but no V2 artifact had ever been published, executed, or attributed to any Engine
  identity.
- `engine-semantics:v1`'s specification already defined spatial reservation, transfer and
  observation rules against the same five spatial facts, written at a time when the Factory draft
  made them mandatory — but its implementation executes only `factory-model:v1`, and its text never
  states which Factory policy or represented content it admits.
- Whole-definition fixation covers unexercised rules and refusal behavior; that spatial execution
  had not shipped did not license changing `engine-semantics:v1` in place, only a genuinely
  preservation-proved correction or a distinguishable identity.
- Publication validity and Engine applicability are different predicates under the Factory
  semantic-evolution contract; the concluded Factory composition decision deliberately left this
  question open rather than answering it.

## Serious alternatives

- **Candidate A — state `engine-semantics:v1` applicable to both reconciled V2 forms as a
  correction.** Reuses the existing non-spatial and spatial algorithms without inventing new
  computation, and preserves the actual Factory fingerprint. Not selected because it fails its own
  affirmative burden: v1's fixed text admits no rule for the previously-unrepresentable
  spatial-absent case, and equal-formula reuse for the spatial-present case does not prove the whole
  interpretation contract — accepted input domain, refusal behavior, outputs, and referenced
  definitions together — was already preserved. An independent adversarial review reached the same
  conclusion by an independent reconstruction.
- **Present-only correction / hybrid partition.** Stating `engine-semantics:v1` applicable to
  spatial-present V2 only, paired with a distinguishable identity for spatial-absent V2. Left
  **unproven, not impossible**: the five spatial facts and their interpretation survive at the fact
  level, but full policy/reference/refusal preservation for that narrower claim was not
  established. Not selected as the adopted route because a bounded, conservative single successor
  for the whole requested domain did not require resolving that separate, harder question first.
- **Restricted alternative — retain v1-only execution and refuse all V2.** The safe interim
  operational state. Not selected as an answer because it does not provide the requested
  both-form applicability; it remains available as a fallback if implementation is deferred.

## Decisive rationale

- The decision rule was proof, not plausibility: Candidate A needed to show the admission was
  already entailed by v1's fixed text, not merely that a faithful implementation was possible.
- Absence is not a present record with default or zero values; a spatial-absent `factory-model:v2`
  artifact is a genuinely new input case no existing rule admitted, so admitting it is a new rule,
  not a recorded-but-unwritten one.
- A single successor for the whole reconciled V2 domain avoided depending on the separately
  harder, still-open present-only preservation question, while still letting the successor reuse
  v1's rules by explicit reference rather than duplicating them.

## Consequences and accepted trade-offs

- `engine-semantics:v1`'s historical meaning, including its unexercised spatial sections, remains
  exactly as defined and continues to apply only to `factory-model:v1`; no historical v1 result or
  attribution is reinterpreted.
- `engine-semantics:v2` must independently satisfy the same whole-definition-fixation,
  non-rebinding, and support-declaration obligations once it has attributed records; it is not a
  parallel-but-lighter identity.
- This is a bounded application of the open same-label Engine amendment question to one concrete
  case; it does not settle same-label amendment in general, and does not establish that every
  different Factory fingerprint or every future Factory policy requires a new Engine identity.
- `factory-model:v2` publication release and spatial-runtime activation remain blocked on their own
  unimplemented prerequisites (canonical codec/publication, runtime establishment of
  `engine-semantics:v2`, provenance propagation, and the admission-reservation substrate) rather
  than on this research question, which this decision does not itself implement.
- The Factory composition question's existing reopening trigger remains available if a measured
  successor implementation or support cost later changes the relative merit of the optional-record
  `factory-model:v2` boundary; no such cost was measured here.

## Reconsider when

- an Engine-owned, whole-definition-preserving admission rule is discovered that shows
  `engine-semantics:v1` already entailed applicability to the spatial-absent (or spatial-present)
  `factory-model:v2` case, as opposed to a new fixture merely assuming such a rule;
- a concrete present-only correction of `engine-semantics:v1` is separately proved complete
  (policy/reference/refusal preservation, not only fact-level reuse), which would narrow but not by
  itself invalidate `engine-semantics:v2`'s applicability to the spatial-absent case;
- measured `engine-semantics:v2` implementation or support cost materially changes the relative
  merit of the optional-record `factory-model:v2` composition boundary.

## Provenance

Reconciled in the pull request that reconstructed [Engine Semantics v2](../../architecture/engine-semantics-v2.md),
updated [Factory Model v2](../../architecture/factory-model-v2.md#12-publication-validity-is-not-engine-applicability)
and [Spatial Runtime Consequences](../../planning/spatial-runtime-consequences.md), and concluded
the research question in [Engine Evolution Research](../../research/investigations/engine-evolution.md#concluded--engine-applicability-to-optional-record-factory-policies).
The high-risk investigation's report and its independent adversarial review (`ACCEPT WITH
QUALIFICATIONS`, qualifications Q1-Q5) were temporary research evidence on the
`workspace/research-engine-v2-applicability` workspace and were not retained; the reconciled
documents above and this record are the durable destinations.
