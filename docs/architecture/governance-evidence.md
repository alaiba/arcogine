# Governance evidence

Status: Adopted semantic contract; evidence identification, storage, reuse and explanation are not yet implemented
Owning architecture: [Governance and Conformance](governance-conformance.md)
Evolution rule: [Semantic identity and evolution](decisions/semantic-identity-and-evolution.md)

## Purpose

Governance requirements and assertions distinguish whether an assertion can be evaluated from model
state alone (`MODEL_STATE_SUFFICIENT`) or requires evidence beyond structural model state
(`EXTERNAL_EVIDENCE_REQUIRED`). The landed conformance evaluation keeps evidence out of scope:
`EvidenceRequirement` only declares need, `ConformanceEvaluation` carries requirement/assertion
identity and version, subject fingerprint, optional verified controlled revision, result, and
finding, and an applicable assertion whose evidence cannot be supplied evaluates to `UNKNOWN`.

A future evidence capability must consume several provenance classes without lying about where
facts came from: facts read from canonical Arcogine semantic state, simulation/verification/analysis
results produced by Arcogine, external operational observations, and reconciliation-derived
results. This contract fixes what an evidence reference identifies, what equality means, how
corrections and later reinterpretation relate to earlier use, which provenance is intrinsic to
evidence versus supplied by a use, how evidence applicability relates to requirement applicability
and outcome, and what a historical evaluation must preserve to remain explainable. It selects no
storage schema, identifier scheme, serialization, registry, transport, retention policy, signature
infrastructure, ingestion adapter, or Java type shape.

It builds on, and does not reopen, the neighbouring contracts: semantic content identity and
controlled historical occurrence are different identities and an accepted record is never rewritten
([controlled revisions](controlled-revisions.md)); a fingerprint identifies canonical content under
a named policy and does not establish the provenance of an occurrence
([Factory Model v1](factory-model-v1.md)); external formats are projections and serialization never
defines identity ([external representations](external-representations.md)); raw external
observations never acquire Arcogine identity at ingestion and corrections are new accepted records
about earlier ones ([Operational continuity](operational-continuity.md)); and Engine-produced
results carry `ModelFingerprint`, `EngineSemanticsVersion` and explicit result-affecting inputs,
with retirement removing executability, not provenance, and cross-version comparison explicit and
consumer-owned ([deterministic simulation](decisions/deterministic-simulation.md)).

## 1. Evidence need, evidence provenance, and evidence use stay separate dimensions

`EvidenceRequirement` continues to answer only whether model state alone suffices to evaluate an
assertion. No third member is added to represent Arcogine-produced analysis: a simulation or
verification result is not model state because Arcogine produced it, and it is not an external
observation because it reaches Governance from outside the evaluator. Where a fact or result came
from, and how it was relied on, belong to evidence and evidence use respectively.

The evidence capability must keep at least these origins semantically distinguishable: an
authoritative modelled/semantic-state fact; an Arcogine-derived analytical/verification result; an
externally observed fact with independently retained source provenance; and a
reconciliation/interpretation-derived result that explicitly combines modelled intent and
observations. This is a distinguishability obligation, not a closed origin enum or a Java taxonomy.

## 2. An evidence reference identifies one attributable recorded source/result revision

An evidence reference identifies **one particular attributable recorded assertion, observation,
artifact revision, or analytical result revision/occurrence**: the record of what a source asserted
or a producer produced. It does not identify the external subject, the numerical value, an artifact
name, the evaluation that used it, or the truth of the proposition it asserts.

- A producer-owned immutable or versioned handle **can** fulfil the evidence-reference role. Where
  the producer's identity is already semantically sufficient — an authoritative controlled revision
  and fingerprint for a structural fact, an exact result revision for an analytical result, a
  source-qualified capture for an observation — Governance must not allocate a second global evidence
  identifier merely to have one of its own.
- A source-owned stable logical identifier may participate only while the exact relied-on revision
  or capture remains identifiable through the complete reference. A logical identifier that persists
  across a source's revisions is not, by itself, an evidence reference.
- A bare content digest identifies content, not necessarily the attributable source occurrence: two
  productions with identical bytes, or an attestation whose author is not recorded, are not
  identified by their digest alone.
- If a source offers only a mutable reference, an exact attributable capture or source revision is
  required before durable historical reliance. The capture proves what was obtained; it does not
  establish that the source's claim was true.
- Enough source meaning must be preserved, by value or by an exact authoritative relationship, to
  interpret the record: source/producer, described subject, fact/result and relevant units,
  source/production time or its explicit uncertainty, and material intrinsic provenance. Unknown
  intrinsic facts stay explicit; they may prevent a use without preventing faithful recording.

## 3. Equality is same-revision identity, and a reference is never rebound

- Evidence identity equality means **the same referenced source/result revision**. It does not mean
  byte equality, logical equivalence, a common subject, current truth, independent corroboration,
  trust, or applicability.
- The same complete evidence reference must never later resolve to different identity-bearing
  content or different intrinsic provenance.
- A correction, retraction, supersession, or producer reinterpretation creates a **distinguishable
  attributable relationship or revision** that names what it concerns. It never rebinds an existing
  reference and never rewrites the basis of an evaluation that already used the earlier revision.
  Later annotations, trust judgements, or reassessments reference the original without mutating it.
- Redelivery of the same source record is the same evidence, not a second independent observation,
  however many importers or evaluations encountered it; its common source lineage must remain
  visible. Equal payloads from distinct producers or distinct occurrences may remain distinct
  evidence when the producer's contract distinguishes them.
- These are logical non-rebinding obligations. They do not require append-only physical storage,
  perpetual byte retention, or any particular history representation; a mutable source system is
  acceptable when the reference pins the exact version or capture relied on.

## 4. Evidence and contextual use are distinct roles, not necessarily distinct records

- **Evidence** preserves the independently attributable source/result and its intrinsic provenance.
- A **use** (`EvidenceUse` or an equivalent relationship) records how that exact material was
  considered for one evaluation occurrence and one claim or role: the target subject (model
  fingerprint, optional verified controlled revision, semantic scope), the exact requirement and
  assertion definitions, the role in which the material was considered (relied on, considered but
  not relied on, comparator, or another explicit role), the temporal frame where material, and the
  applicability determination in §6.
- One evidence item may have many uses, within and across evaluations, each with its own
  independent subject/revision/claim/time/applicability determination. Equal fingerprints or a
  rollback never copy one use's applicability or acceptance to another target.
- A use must not rewrite the evidence's source meaning or intrinsic provenance. A use may not
  synthesize an accepted controlled revision for a candidate that has none.
- A representation may embed evidence and use together, reference one from the other, or copy
  source material into an evaluation-local record. That is packaging: it is acceptable only when the
  independent source identity, the intrinsic provenance, and the use context remain distinguishable,
  and repeated contextual copies never masquerade as independent corroboration.
- For a fact intrinsic to immutable Arcogine semantic state, the model version is the evidence's
  own provenance and evidence and use may collapse into one record. That collapse is a representation
  choice; it cannot erase the evaluation occurrence, the exact definitions used, or later distinct
  uses of the same fact.
- No separately allocated global use identifier is required: a use is identified
  by its evaluation occurrence (§8) plus its position or role within that occurrence, unless a later
  concrete consumer proves a cross-occurrence use identity is needed.

## 5. Intrinsic producer provenance is distinct from use-target binding

Evidence does not categorically lack a model fingerprint or revision. The durable distinction is between provenance **intrinsic to how the evidence was produced** and the
**target of a later Governance use**.

- A raw external observation is independent of Arcogine model, revision, and operational-continuation
  identity at ingestion unless the source itself intrinsically owns such provenance. It retains its
  source identity, external subject, fact/unit, source/event time, receipt provenance, and known
  quality/authenticity information. Correspondence to an Arcogine subject or revision is a later
  use or reconciliation determination and must never be fabricated at ingestion; endpoint naming or
  coincident identifiers do not establish it. This restates the Operational continuity contract §11 and does not extend it.
- An Arcogine-derived analytical or verification result **may, and where the producer's contract
  establishes it must, retain producer-intrinsic provenance**: the subject `ModelFingerprint`, the
  controlled revision when the producer was bound to one, the `EngineSemanticsVersion` when Engine
  behaviour was involved, run/result or result-boundary identity, the explicit result-affecting
  inputs the deterministic simulation decision names, and the analytical definition/version needed to interpret the result. A run
  identifier alone does not identify a time-varying result; model plus Engine version alone omits
  workload, random inputs, ordered commands, and other material inputs.
- The target model/revision/claim of a later use is a separate relationship and may differ from the
  source model that produced the evidence. A result for design `baseline` may serve as a comparator
  for candidate `changed` only through an explicit use-owned role and the owning domain's comparison
  semantics; it is not direct proof about `changed`.
- Missing producer provenance is recorded as missing. It is never inferred from a build name,
  stamped from the current implementation onto an older result, or otherwise fabricated; an unknown
  semantics version, subject, or input remains explicit and may make a use unresolvable.
- Governance does not take ownership of Operational acquisition, correspondence, or trust semantics,
  nor of Engine or analytics calculation semantics, by consuming their attributable results. Source
  authenticity, recording provenance, producer semantics, actor participation where actually known,
  and evaluation authorization must not collapse into one ambiguous field.

## 6. Evidence applicability, requirement applicability, and outcome are three determinations

- **Evidence applicability/reliance** is a reasoned, attributable determination about one use:
  source and subject correspondence, period/freshness, provenance and trust adequacy, analytical
  meaning and units, and compatibility where required. The determination and the interpretation that
  governed it must remain recoverable. "Applicable", "inapplicable", and "not established" are
  semantic distinctions here, not a selected API enum.
- **Requirement/assertion applicability** is scope: whether the requirement addresses the evaluated
  subject at all. `NOT_APPLICABLE` arises only from that scope determination.
- **Conformance outcome** is the assertion's own judgement over an adequate basis. Only an applicable
  assertion proven by an adequate, applicable basis can `PASS`.
- Stale, out-of-period, wrong-subject, untrusted, semantically incompatible, incomplete, or otherwise
  unusable evidence never silently produces `PASS`. Unusable evidence alone never makes an applicable
  requirement `NOT_APPLICABLE`.
- Missing or unresolved evidence for an applicable assertion remains explainable and ordinarily
  supports `UNKNOWN`. It supports `FAIL` only where the assertion's own semantics establish a
  violation from adequate evidence of absence — a reliable record that a required artifact does not
  exist — and a failed search of an incomplete feed is not that evidence. The assertion owns this
  distinction.
- A single unusable item does not poison an otherwise sufficient basis; a sufficient alternative
  evidence set may still support `PASS`. Known considered-but-not-relied-on material, material
  contradictory evidence, exclusion reasons, and material evidence gaps may need to remain part of the
  historical evaluation basis (§7). A gap is recorded as part of the basis, not manufactured as an
  evidence item.
- Two conflicting measurements are not resolved by arrival order or identifier order unless the
  claim's recorded policy gives that rule authority; an unresolved conflict is preserved and does not
  support `PASS`.
- Malformed or falsely bound input may be rejected at the boundary rather than converted into a
  conformance result, preserving the evaluator's explicit fingerprint/revision errors. Infrastructure
  failure is not a real-world violation.
- Consumer-specific freshness, admissibility, coverage, conflict-resolution, and compatibility
  policies remain open. No universal policy, predicate, or enum is selected here.

## 7. Cross-version compatibility is explicit, claim-specific, and consumer-owned

Cross-version compatibility is a determination about a particular claim, metric, and set of
assumptions, made by the consuming use. Equal `EngineSemanticsVersion` values do not establish equal
workload, units, scope, or experimental conditions; different values do not by themselves make
results incomparable for a fact the changed semantics cannot affect. Without adequate compatibility
evidence, comparison stays unsupported — identity never authorizes guessing. This applies the deterministic simulation decision
decision 17 to Governance use and does not modify it.

## 8. A completed evaluation is an identifiable immutable occurrence with a fixed basis

The evidence-capable Governance contract **requires an identifiable, immutable evaluation
occurrence**; this is an explicit rule, not a side effect of the evidence contract.

- An evaluation occurrence identifies one completed act of evaluating one requirement/assertion pair
  against one subject under one fixed basis. Its equality is occurrence identity: two evaluations
  with equal inputs and equal outcomes remain distinct occurrences when historical attribution
  distinguishes them. Equal results never make two occurrences the same historical fact.
- The occurrence is distinct from the `Finding` it may produce, from the deterministic evaluation
  function that computed the result, from any audit projection over it, and from `RunId`,
  `ModelFingerprint`, `ControlledRevisionId`, and `EngineSemanticsVersion`.
- The occurrence's **basis is fixed** when the occurrence is recorded: the exact requirement and
  assertion definitions used; the subject fingerprint and optional verified revision; the relied-on
  uses; the relevant considered-but-excluded material and known material gaps; the temporal frame and
  knowledge boundary; the applicability and interpretation rules applied; and the outcome and its
  explanation. New knowledge, corrected evidence, changed trust, or changed policy produces a new
  occurrence that names its relationship to the old one; it never mutates the old basis.
- An occurrence becomes an authoritative historical fact when its record is accepted at an
  authority's commit boundary, mirroring the controlled revision contract and the Operational continuity contract §5. The authority may add
  authority-owned recording time; the deterministic domain result consumes only explicit inputs and
  is not changed by recording. A `ConformanceEvaluation` value that has not been accepted is a result,
  not an occurrence.
- The current `ConformanceEvaluation` value has no occurrence identity and no acceptance boundary.
  Occurrence identity is therefore an **additive** obligation on the first evidence-capable
  implementation; it does not change the existing value's equality contract. A capability that claims
  historical attribution, reuse across evaluations, or audit reconstruction must satisfy this
  obligation; it cannot defer it by calling itself headless.

## 9. Historical explanation requires exact definition resolution

- A completed occurrence must preserve, or authoritatively resolve, the exact requirement and
  assertion definitions it used — wording, governing external source identity/edition/locator where
  meaning depends on it, and the executable rule — not merely labels that could later resolve to
  something else.
- Current Java equality of `Requirement` and `Assertion` by identity plus version is a value
  contract, not proof that historical definitions are durably resolvable: equality deliberately
  excludes wording, source, and rule, and the in-memory catalogue is not a durable definition
  authority. A material change to wording, source, or rule must not reuse an existing semantic
  definition binding. This obligation is additive to the present identity/equality contracts.
- Later requirement/assertion changes, evidence corrections, trust changes, or policy changes create
  new historical facts and new occurrences; they never reinterpret the recorded basis. A historical
  `PASS` is the recorded outcome of that occurrence, not a current assurance that the claim remains
  true.
- Retention may be bounded, but the remaining explanation must be truthful. If necessary historical
  material is missing or corrupt, the system discloses the gap; it never substitutes a current
  definition, a current source revision, or re-execution under current semantics. A retained digest
  with missing content proves neither readability nor meaning.
- Retirement of a producer's executability does not erase attribution or automatically invalidate a
  historical result (the deterministic simulation decision decision 16). Whether the result still serves a new claim is a new
  use determination.

## 10. Use targets are point identities; accumulating continuations are out of scope

A use binds evidence to exact point-in-time semantic or evaluation subjects whose meaning can be made
immutable and attributable: a model fingerprint, a verified controlled revision, a semantic scope,
an evaluation occurrence. Evidence *about* an accountable operational continuation over time — the
accumulating identity the [Operational continuity contract](operational-continuity.md) §11 records as a known limit — is **outside** this contract. An
integration that needs it must use the owning Operational contract or obtain a separate explicit
decision; it must not reinterpret the point-identity rule as though a continuation were a point.

## 11. Authority boundaries

- **Governance** owns requirement/assertion definitions, evaluation occurrences and their basis,
  evidence use, applicability/reliance interpretation, findings, and later governance decisions.
- **Operational Execution** owns acquisition of external observations, their source/trust/authenticity
  provenance, subject correspondence, and reconciliation. Governance may consume a fixture-backed or
  Operational-supplied explicit correspondence assertion for a bounded case; doing so neither
  implements ingestion nor establishes that the correspondence authority is trusted.
- **Engine and analytics producers** own calculation semantics, result identity, `EngineSemanticsVersion`,
  and analytical definitions. Verification-objective semantics (throughput, lead time, utilization,
  safety separation, operational reconciliation, and the like) remain domain-owned; Governance
  consumes attributable results and does not become a generic simulation/verification engine.
- A reference-shaped value or provenance-shaped object alone establishes no authoritative acceptance,
  authenticity, or entitlement. The owner of a source, result, or interpretation establishes the
  guarantees it claims; where the retained basis cannot support a claimed integrity or attribution,
  the limitation is explicit.

## 12. Implementation bounds follow producer identities that actually exist

A headless evidence-capable implementation may prove the generic reference/use/occurrence
contract using producer identities and provenance that actually exist, or explicit fixtures at the
owning seam. Structural facts have a landed producer identity (fingerprint plus authoritative
controlled revision). The Engine runtime exposes a fixed `EngineSemanticsVersion`, but durable Engine
result identity and observation/event provenance propagation are not yet established; Operational
observation identity, correspondence, and trust are not implemented; analytical-definition provenance
ownership is unresolved; durable requirement-definition and evaluation-history storage do not exist.

An implementation must therefore state which provenance class it proves and must not claim
production support for a class whose producer provenance is not yet exposed. A fixture that carries
explicitly attributed analytical or external material proves the seam, not the integration.

## 13. What remains deliberately open

The following remain open and must not be read as settled by this contract:

- the concrete evidence-reference and use representation: identifier scheme, URI or key grammar,
  canonical serialization, capture format, and interchange projection (the [external representation policy](external-representations.md) governs projections);
- source alias, deduplication, and correlation mechanics beyond the semantic rules in §3;
- consumer-specific freshness, admissibility, coverage, conflict-resolution, and cross-version
  compatibility policies (§6–§7);
- the Operational correspondence/trust capability and Operational observation identity required by a
  real external-observation integration;
- Engine result identity, runtime provenance propagation, and analytical-definition provenance
  ownership, the last of which is the separate simulation-analytics boundary research question;
- the evaluation-occurrence identifier representation, the acceptance authority, and any
  requirement-definition registry or persistence mechanism that satisfies §8–§9;
- retention/compaction limits and how missing history is disclosed for a particular claim;
- signature, integrity, PKI, authentication, and transport infrastructure;
- any evidence store, evidence lake, document system, or generic verification framework;
- module placement and Java type shapes for evidence, use, and occurrence.
