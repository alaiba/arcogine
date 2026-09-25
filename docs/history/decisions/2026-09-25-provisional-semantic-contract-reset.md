# Provisional semantic-contract reset

> **Date:** 2026-09-25
>
> **Authority:** Historical, non-normative evidence; current meaning lives only in the documents it was reconciled into
>
> **Reconciled into:** [Semantic evolution and support](../../architecture/overview.md#semantic-evolution-and-support), [Determinism Contract](../../architecture/overview.md#determinism-contract), [Factory model](../../architecture/factory-model.md), [Factory semantic evolution](../../architecture/factory-design.md#111-semantic-evolution), [Engine semantics](../../architecture/engine-semantics.md), [Transfer applicability](../../architecture/transfer-applicability.md), [Controlled revisions](../../architecture/controlled-revisions.md), [Semantic contract support](../../development/semantic-contract-support.md)

## Decision

The repository owner replaced the pre-release Factory and Engine semantic estate instead of settling
what it already permitted. Factory and Engine semantics are work in progress, named by the mutable
markers `factory-model:wip` and `engine-semantics:wip`, until an explicit owner-approved promotion
establishes a real, scoped durable-use commitment; deterministic fingerprinting during development
does not itself promote or freeze a contract. `factory-model:v1`, `factory-model:v2`,
`engine-semantics:v1`, their canonical bytes, fingerprints, fixtures, compatibility assumptions and
dependent planning were removed from the active system. The two Factory policies became one model
with an optional spatial record under one canonical form; retained, commitment-bearing admission was
made unavailable while every contract is work in progress.

## Context

- **The original objective** was meaningful terminology and freedom to evolve the Factory and Engine
  definitions before any genuine promotion: calling the first implemented policy `v1` encouraged
  reading it as final and made every correction look like a `v2`.
- **The investigation drifted.** Asked what event had promoted the definitions, research turned into
  an interpretation of whether, and on which historical basis, the old labels were already fixed.
  - The first report found no retained or accepted attribution in scope and concluded the V1 freeze
    was premature. Its independent adversarial review returned **REOPEN**: missing attribution
    evidence did not show that no commitment had been published, its proposed rule dropped actual
    undeclared retained acceptance as an obligation-creating case, and its proving matrix counted
    policy-relative fingerprints and discretionary costs as semantic falsifications.
  - The revised report repaired those defects, showed that the V1 specification's "has attributed
    records" basis was false, and left the operative basis as an owner choice between two readings.
    Its independent review returned a scoped **REOPEN**: delivery history supported a third reading —
    labels carried through an earlier support withdrawal stay closed to changed meaning — which
    conflicted with the correction path the report kept open, and the Engine analysis missed its own
    release precondition.
  - Neither report was accepted. Each round made the permission question narrower and the history
    deeper without bringing the original objective closer.
- **Nothing durable relied on the estate.** No release, outward consumer, retained store or
  accepted use was found within the searched scope; the only durable-capable revision store was
  constructed solely over temporary test directories; an earlier owner declaration had already
  stated that no retained artifact or consumer relied on the pre-consolidation estate.

## Serious alternatives

These were choices not adopted, not designs shown impossible.

- **Continue the research and keep the estate.** Resolve the fixation basis and carry V1 and V2
  forward as fixed, distinguishable policies. Coherent — the second review found a well-evidenced
  basis for it — but it preserved a successor ladder, cross-policy comparison and continued-V1
  publication questions for contracts no durable use needed.
- **Keep the old labels closed forever.** Build a new provisional grammar under a fresh label while
  reserving or tombstoning the old ones. Not adopted: the owner authorized discarding them, and the
  discarded tokens are refused as obsolete input rather than grandfathered.
- **Migrate or dual-read.** Legacy codecs, aliases or migration for V1/V2 artifacts. Not adopted:
  no retained artifact existed to migrate.
- **An ordinal or draft sequence** (`v0`, `draft-1`, `draft-2`, or immutable experimental
  identities per change). It would give exact cross-revision provenance during development, but
  recreates a mandatory ladder; proving-only custody makes that provenance unnecessary until
  promotion.
- **Admission-time binding of an exact definition revision behind a mutable alias.** Not adopted:
  no retained use needs it yet.
- **A universal `proving`/`promoted` lifecycle or registry.** Not adopted: promotion is a status in
  one owning contract.
- **A retained-custody authority mode that refuses WIP now.** Considered for the Governance
  revision store. The smaller boundary was chosen: the only store is an explicitly disposable proving
  store and retained admission is unavailable, because no promoted definition exists to admit.

## Decisive rationale

- Durability exists to protect real reliance. With no durable use, an attribution-driven freeze
  protected nothing while making ordinary correction look like identity evolution.
- Determinism does not need durability. Canonical bytes, fingerprints, golden vectors and
  reproducible runs work within one current definition.
- The permission debate could not change what the owner wanted, so settling it was not worth a third
  report and review; an explicit owner decision is a decision input, not a research conclusion.
- Making promotion explicit puts the cost of immutability and support where a concrete need creates
  it.

## Consequences and accepted trade-offs

- A WIP marker or fingerprint is not cross-revision provenance: results and proving artifacts from
  an earlier development revision cannot be attributed to today's definition, and development stores
  are reset rather than migrated.
- Golden vectors are regenerated deliberately with the definition; compatibility with the discarded
  bytes is not maintained.
- Governance proves its revision and structural-evidence mechanics in a proving store only; no
  retained historical commitment exists until promotion.
- The spatial record is represented, validated and fingerprinted, but the Engine refuses it before
  runtime mutation until spatial execution lands, rather than silently ignoring it. The Engine
  applicability research that had blocked that work became moot.
- If WIP material is ever retained or relied on outside a declared custody, it is handled as an
  explicit defect, not as an accidental promotion.

## Knowledge transfer

| Finding or learning | Disposition | Destination |
| --- | --- | --- |
| An explicit owner-directed architecture change is a decision input, not a hypothesis to prove compatible with the rules it replaces, and differs from promoting a research conclusion | Baked in | [Research operating model](../../development/researching.md) (owner-decision route); [review guidance](../../development/reviewing.md#architectural-reconciliation-discipline) |
| Deterministic canonicalization and in-process publication do not establish a durable-use promise | Encoded | Overview semantic evolution rules 2–4; [Factory model §1](../../architecture/factory-model.md#1-purpose-and-status); proving-store and refusal tests in `FileControlledRevisionAuthorityTest` and `FactoryModelArtifactTest` |
| Names express semantics; maturity and support are stated explicitly, not inferred from `v1`, a milestone or a golden vector | Encoded | [Semantic contract support](../../development/semantic-contract-support.md#work-in-progress-is-the-default); semantic names for the model, codec, Engine type, specifications and tests |
| Audit the concrete referent behind *published*, *attributed*, *retained*: capability, accepted use, promise, fulfilment evidence, cost preference | Baked in | [Semantic contract support](../../development/semantic-contract-support.md#audit-the-referent-behind-a-durability-claim), with an example |
| Scope negative evidence; a test path is not a custody rule | Already encoded; custody sentence added | Research operating model §5 (absence claims); custody-is-declared rule in semantic contract support |
| When compressing architecture, keep each rule's real condition and basis; basis substitution creates standing ambiguity | Baked in | [Review guidance](../../development/reviewing.md#architectural-reconciliation-discipline). No synthesis seed: one occurrence, and the reusable rule now lives in its owner |
| Policy-relative identity, semantic comparison and support cost are different questions; a cost is not a falsification | Baked in | Research operating model §9 (adversarial discriminators) |
| Absence versus authored zero, model versus interpretation, content versus occurrence identity | Encoded | [Factory model §2.2](../../architecture/factory-model.md#22-optional-spatial-record), [transfer applicability](../../architecture/transfer-applicability.md), overview occurrence-identity rule, [controlled revisions](../../architecture/controlled-revisions.md); `FactoryModelCanonicalFormTest`, `SpatialRecordValidationTest`, `EngineSemanticsAcceptanceTest` |
| Undeclared retained acceptance creates obligations; prevention, discovery and remediation differ | Reframed | Overview rule 8 and the accidental-retention review case. "Unknown is not mutable" is discarded for development definitions, whose WIP status is now declared |
| Attribution audit of the pre-reset estate | Context only | This record; no current rule depends on it |
| Carried-label non-reuse, the V1 cross-implementation clause, the Engine release precondition, the audit of in-place Engine text expansion | Discarded with the estate | Non-rebinding and support-withdrawal rules survive for promoted identities (overview rules 6–7) |
| Identity-mechanism options (single token, provisional token, draft identities, reserved final name, admission-time binding) | Recorded as alternatives | This record |
| Proving-case matrix and external analogues (Semantic Versioning, Kubernetes API versioning, RFC 7595) | Discarded | Surviving distinctions are the review cases in semantic contract support; the analogues were not load-bearing |
| Factory composition result | Unchanged | [Factory semantic evolution](../../architecture/factory-design.md#111-semantic-evolution); [composition brief](../../research/investigations/factory-model-semantic-composition.md) |
| Engine-identity applicability question | Superseded | [Brief](../../research/investigations/engine-applicability-after-transfer-boundary.md) maps its behavioral proving dimensions to current homes |

No research report, review or handoff is retained; the destinations above preserve what should
survive them.

## Reconsider when

- a concrete durable-use need appears — a retained revision, evidence or result store that must
  explain records later, an outward consumer that persists fingerprints or Engine names, a release
  promising reproducibility, or externally relied-on conformance claims — which is the trigger for an
  explicit promotion rather than for revisiting this reset;
- development churn under one marker causes real harm, such as a proving artifact decoding silently
  under a changed grammar, suggesting admission-time definition binding or recorded build context;
- promoting Factory and Engine independently proves unworkable for a real consumer.

## Provenance

Reconciled in [pull request #405](https://github.com/alaiba/arcogine/pull/405), together with the research supersession, the
knowledge-transfer audit above and its independent PR review. The earlier choices it replaces are
recorded in the [Factory composition rationale](2026-09-23-factory-model-semantic-composition.md)
and the [transfer-applicability rationale](2026-09-24-transfer-applicability-for-optional-spatial-models.md),
which remain history rather than current rules.
