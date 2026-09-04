# ADR-0016: Governance Evidence Provenance and Derived Analytical Results

Status: Proposed
Date: 2026-09-02

## Context

Governance requirements and assertions already distinguish whether an assertion can be evaluated
from model state alone or requires evidence beyond structural model state:

```text
MODEL_STATE_SUFFICIENT
EXTERNAL_EVIDENCE_REQUIRED
```

Governance conformance evaluation adds deterministic evaluation results and findings. The current
conformance-evaluation implementation deliberately keeps evidence lifecycle/provenance semantics out
of scope and returns `UNKNOWN` when a result cannot be proven from the supplied model state.

The broader architecture, however, needs a future evidence capability to consume several provenance
classes without lying about where facts came from. Examples include:

- facts read directly from canonical Arcogine semantic state;
- simulation/verification/analysis results produced by Arcogine;
- external operational observations such as telemetry or third-party records;
- later reconciliation-derived results that combine modeled intent and externally observed facts.

The current two-member `EvidenceRequirement` answers a different question: whether an assertion can
be decided from model state alone. It does not necessarily need to encode the provenance class of
every evidence artifact that a future evidence capability may later bind to an evaluation.

This distinction became important while reviewing conformance evaluation. Pulling analytical-result
provenance into that evaluator would prematurely couple conformance result semantics to a
not-yet-designed evidence model. Conversely, failing to make provenance explicit in the evidence
capability would risk treating Arcogine-derived analysis as either raw model fact or external
observation, obscuring how a historical conformance result was established.

## Decision

The proposed decision is:

1. **Keep assertion evidence requirement and evidence provenance as separate dimensions.**
   `EvidenceRequirement` continues to answer whether model state alone is sufficient to evaluate an
   assertion. Evidence records answer what fact/result is being supplied and where/how it was
   produced.

2. **Do not add a third `EvidenceRequirement` member merely to represent Arcogine-produced analysis.**
   A simulation or verification result is not model state merely because Arcogine produced it, and
   it is not an external observation merely because it is supplied to Governance from outside the
   conformance evaluator. Evidence provenance belongs on the evidence/evidence-use side of the
   boundary.

3. **The evidence capability must support provenance sufficient to distinguish at least these
   semantic origins:**
   - authoritative modeled/semantic-state fact, where the evidence use directly addresses immutable
     Arcogine semantic state;
   - Arcogine-derived analytical/verification result, including simulation or deterministic
     verification output produced from explicit inputs under attributable interpretation semantics;
   - externally observed fact, whose source is outside Arcogine's modeled state and whose source
     identity/authenticity provenance is retained independently;
   - reconciliation/interpretation-derived result when a later capability explicitly combines
     modeled intent and observations.

   The exact enum/type names remain open. The requirement is semantic distinguishability, not a
   particular Java taxonomy.

4. **Evidence is not the same as `EvidenceUse`.** An evidence record preserves the independently
   provenanced fact/result. `EvidenceUse` records how that evidence is interpreted for a particular
   requirement/assertion/evaluation context, including model/revision/context associations that are
   not intrinsic to the raw source fact.

5. **Raw external observations remain revision-independent at ingestion.** If Arcogine later binds
   an observation to a model fingerprint, controlled revision, execution context, deployment, or
   reconciliation result, that relationship belongs to the interpretation/reconciliation or
   `EvidenceUse` record rather than being fabricated as source provenance.

6. **Arcogine-derived analytical evidence retains the interpretation provenance needed to explain
   the result.** For Engine-produced simulation/verification output this includes the subject model
   fingerprint, the applicable `EngineSemanticsVersion` established by
   [ADR-0015](0015-engine-semantics-identity-and-reproducibility.md), and the explicit run
   inputs/result provenance required by the producing capability. Governance does not own or
   redefine Engine semantics.

7. **Historical evidence validity does not require permanent re-executability.** Evidence validity
   is based on integrity, provenance, and applicability. A historical Arcogine analytical result may
   remain valid evidence even when the old producing semantics version is no longer executable,
   provided its semantic provenance remains attributable and verifiable. Whether it is still
   appropriate for a new forward-looking claim is an `EvidenceUse`/applicability judgment.

8. **Cross-version analytical comparison is never silently assumed.** If two analytical results
   were produced under materially different result-affecting semantic policies, a consumer must
   explicitly determine compatibility/applicability before comparing or combining them as evidence.

9. **Verification-objective semantics remain domain-owned.** Governance owns generic
   requirement/assertion/evaluation/evidence/finding contracts. The owning domain or capability
   defines the meaning and evaluator for a specific objective such as throughput, lead-time,
   utilization, safety separation, or operational reconciliation. Governance consumes the resulting
   attributable evidence; it does not become a generic simulation/verification engine.

10. **Conformance evaluation remains narrow.** `PASS`, `FAIL`, `UNKNOWN`, `NOT_APPLICABLE`,
    immutable evaluation provenance, and `Finding` remain conformance-evaluation responsibilities
    without an evidence-source taxonomy. Evidence/evidence-use semantics compose with those results
    later rather than forcing the evaluator to freeze evidence implementation details now.

## Alternatives considered

### Add `ARCOGINE_DERIVED_EVIDENCE` to `EvidenceRequirement`

Rejected as the default design. It mixes two questions: whether model state alone is sufficient and
where a non-model-state fact came from. An assertion may require evidence while remaining agnostic
about whether an acceptable fact was produced analytically by Arcogine or observed externally.

### Treat Arcogine analytical results as `EXTERNAL_EVIDENCE_REQUIRED` and record no further origin

Rejected. While the requirement may still be evidence-requiring, provenance must distinguish a
simulation/verification result from a PLC reading, IdP log, or other external observation so
historical interpretation and applicability remain explainable.

### Make simulation/verification output direct conformance-evaluation input and skip evidence

Rejected. That would make the conformance evaluator own producer-specific analytical provenance and
would duplicate the future evidence/evidence-use lifecycle instead of composing with it.

### Create a generic cross-domain verification framework now

Rejected. Factory, Engine, Operational, Challenge, and other domains can own their concrete
verification/evaluation semantics. Governance needs a generic way to retain attributable evidence
and interpret its use, not a universal rule engine.

## Consequences

- Conformance evaluation can remain implemented without prematurely designing the evidence
  lifecycle.
- The evidence capability gets an explicit architecture question before implementation rather than
  inferring provenance semantics from whichever first producer integrates with it.
- Operational external observations can remain independently provenanced and later become evidence
  without being rebound to a revision at ingestion.
- Engine simulation/verification output can become Governance evidence while retaining
  `ModelFingerprint` and Engine interpretation provenance rather than pretending it is structural
  model state.
- Future reconciliation results can remain distinct lifecycle records rather than collapsing raw
  observation, analytical interpretation, finding, and candidate change into one object.
- A detailed evidence identity, persistence, integrity/signature, retention, or transport schema is
  not selected by this proposal and should receive its own decision only when a concrete evidence
  implementation requires it.

## Charter alignment

This proposal preserves Arcogine's distinction between modeled intent, observed reality, analytical
interpretation, and governance decisions. It supports explainable historical results without
centralizing domain-specific verification logic inside Governance or binding external facts to
Arcogine semantic identities before an explicit interpretation occurs.
