# Governance evidence identity and applicability

> **Status:** CONCLUDED
> **Risk:** **High** — this question fixed hard-to-reverse identity, history, applicability, provenance, and cross-version semantics for a shared Governance contract, so its conclusion was independently adversarially reviewed before promotion.
> **Scope:** The minimum semantic contract required for Governance `Evidence` and `EvidenceUse` before their first implementation can be admitted
> **Authority:** Research provenance only. The durable conclusion lives in the [Governance evidence contract](../../architecture/governance-evidence.md) and [Governance architecture](../../architecture/governance-conformance.md) §7, §9, and §12; this brief decides nothing and is retained as the investigation's framing.

## Conclusion

**A minimum shared contract survives: an immutable, source-qualified evidence reference plus contextual, attributable use.** The decision-quality report and its independent adversarial review (disposition `ACCEPT WITH QUALIFICATIONS`, fresh isolated-run independence tier) are reconciled into the [Governance evidence contract](../../architecture/governance-evidence.md) and summarized in [Governance architecture](../../architecture/governance-conformance.md) §7, §9, and §12. Those documents are the authority; this brief does not restate the decision. The exact report and review coordinates are recorded in the [research register](../research-register.md) row's durable evidence reference.

### Outcome against the candidate models

| Candidate | Outcome |
|---|---|
| Source-record identity with contextual uses | **Adopted.** The only candidate that preserved independent attribution across equal-fingerprint revisions, survived correction without rewriting earlier basis, and separated duplicate delivery from independent corroboration |
| Context-bound evidence identity | **Rejected in pure form; permitted as packaging.** Copies without a stable independent source reference lose same-source correlation; a linked form that retains source identity implements the same obligations |
| Artifact/fact identity with an interpretation layer | **Adopted only in the strong form.** An exact producer/source revision with attributable provenance can be the reference; a bare digest is content identity, not source-occurrence identity |
| No new shared contract yet | **Rejected as the answer, retained as the fallback.** Operationally safe, but it answers neither cross-revision reuse nor historical attribution; preferable only if the product drops that need |

### Qualifications carried into the decision

The report's three qualifications (producer-intrinsic versus use-target provenance; evidence applicability distinct from requirement applicability and outcome; identity-plus-version labels insufficient without exact definition resolution) and the review's three (evaluation-occurrence identity decided explicitly; use targets are point identities, accumulating operational continuations out of scope; first-implementation acceptance bounded by producer identities that actually exist) are all encoded in the Governance evidence contract §5–§6, §8–§10, and §12. None was deferred.

### What remains open

Representation, alias/deduplication mechanics, consumer-specific freshness/admissibility/conflict/compatibility policies, Operational correspondence/trust identity, Engine result identity and provenance propagation, analytical-definition ownership, the occurrence/definition persistence mechanism, and retention limits are deliberately deferred in the Governance evidence contract §13. The analytical-definition ownership half is the separate [simulation-analytics boundary](simulation-analytics-consumer-boundary.md) question; the Operational halves belong to the [Operational boundary research](operational-execution-digital-twin-boundaries.md). Reopening triggers are recorded in the Governance evidence contract's consequences.

The original brief follows as the investigation's framing.

## Question

> What minimum durable identity, provenance, applicability, and historical-attribution contract must Governance `Evidence` and `EvidenceUse` expose so that evidence can be reused and interpreted across exact semantic revisions without conflating raw source facts, producer-owned analytical results, or later Governance interpretation?

## Decision at stake

Whether the proposed Governance evidence capability has a sufficiently settled semantic contract to admit its first headless implementation, and which durable architecture surface must be accepted or revised before implementation begins.

The answer must distinguish semantic obligations from deferred mechanism choices. It must not treat a research conclusion, a design proposal, or a planning acceptance list as an implementation contract until the required reconciliation has landed.

## Scope and non-goals

In scope:

- the referent and stable identity of an evidence item;
- equality and reuse of one evidence item through multiple uses;
- immutability, correction, later interpretation, and historical attribution;
- the provenance intrinsic to a source fact/result versus the context supplied by a use;
- applicability across exact subjects, controlled revisions, requirements, assertions, evaluations, periods, and source versions;
- explicit handling of missing, stale, incompatible, or inapplicable evidence;
- producer-owned provenance for Arcogine-derived analytical results, including material model and engine semantic versions;
- revision-independent ingestion/ownership of future external observations; and
- the explicit compatibility question for cross-version analytical use.

Out of scope unless the semantic answer proves one is indispensable: storage schema and database selection, generic persistence infrastructure, signatures/PKI, retention and archival policy, transport, connector authentication, telemetry ingestion, document management, evidence-lake design, simulation analytics redesign, and a generic cross-domain verification framework.

## Risk classification

**High.** The question determines identity and historical meaning for a future public/shared Governance capability and crosses Governance, Engine, and Operational ownership boundaries. A wrong choice would be expensive to reverse after persisted evidence or evaluation history exists. The investigation therefore needs failure-oriented proving cases, precise repository grounding, and genuinely independent adversarial review before architecture promotion.

## Intended functions / invariants

Any surviving contract must preserve these constraints while leaving representation open:

- one source fact/result must remain independently attributable when several evaluations use it;
- a use must not rewrite the provenance or semantic meaning of the evidence it references;
- exact semantic subject/revision and evaluation context must remain reconstructible where they are material;
- missing, stale, incompatible, or inapplicable evidence must not silently produce `PASS`;
- external observations must retain source-owned provenance and must not be rebound to a model/revision at ingestion merely for later Governance use;
- producer-owned analytical semantics must remain attributable without Governance redefining the producer's calculation; and
- later evidence, requirement, or semantic-revision changes must not erase the historical meaning of an earlier evaluation.

## Candidate models / hypotheses

The investigation should compare, at minimum:

1. **Source-record identity with contextual uses.** An immutable evidence record identifies one attributable fact/result, while separate uses carry evaluation-specific subject/revision, requirement/assertion, applicability, and interpretation context.
2. **Context-bound evidence identity.** Evidence is identified primarily by the evaluation/revision context in which it is used, with reuse represented by repeated or linked context records.
3. **Artifact/fact identity with an interpretation layer.** Identity points to an immutable external artifact or semantic fact, while Governance uses and later interpretations provide the contextual binding.
4. **No new shared evidence contract yet.** Existing requirement/conformance records and producer/Operational facts remain separate until a concrete consumer demonstrates a narrower boundary, with the first Governance evidence implementation staying blocked.

The researcher must test whether these are genuinely distinct under the required proving cases and must retain the simplest model that satisfies them. The list is a starting candidate set, not a preferred design or Java shape.

## Failure-oriented analysis

| Function / invariant | Failure or adversarial mode | Consequence if true | Why it discriminates | Treatment |
|---|---|---|---|---|
| Reuse without mutation | One observation supports two exact revisions but the second use overwrites the first context | Historical evaluation becomes ambiguous | Separates source-record identity from context-bound identity | `PROVING CASE` |
| Stable historical identity | A corrected or later-interpreted fact changes the record previously used by an evaluation | Past conformance can be rewritten by present data | Tests immutable-record versus mutable-fact models | `PROVING CASE` |
| Applicability | Evidence is missing, stale, outside its effective period, or produced under incompatible semantics | Unusable evidence becomes an implicit pass or an unexplained failure | Tests whether applicability is explicit and use-owned where appropriate | `PROVING CASE` |
| Ownership | Governance labels an Engine result as an external observation or recomputes its meaning | Producer provenance and ownership are falsified | Tests provenance separation across domains | `PROVING CASE` |
| Ingestion independence | An external observation arrives before correspondence to an Arcogine revision exists | Ingestion must guess or duplicate source facts | Tests revision-independent observation ownership | `PROVING CASE` |
| Mechanism scope | A candidate requires a database/signature/retention system to state its semantics | Implementation detail becomes an accidental architectural commitment | Keeps mechanism choices deferred unless indispensable | `BOUND / DEFER` |

## Proving cases

Derive and evaluate concrete cases including:

1. One independently attributed evidence item is referenced by multiple uses for different exact semantic revisions without mutating the evidence.
2. A later correction or reinterpretation creates an attributable historical distinction rather than silently rewriting the evidence or the earlier evaluation.
3. Missing, stale, inapplicable, or cross-version-incompatible evidence cannot silently yield `PASS`, and the resulting use remains explainable.
4. An external observation can be fixture-backed while retaining source/subject/time/trust provenance and without Governance inventing Operational telemetry types or revision-binding at ingestion.
5. An Arcogine-derived analytical result preserves its producer-owned model, `EngineSemantics`, run/result, and interpretation provenance without Governance owning the calculation.
6. A historical evaluation remains attributable after the requirement/assertion version, evidence set, or semantic revision later changes.

For each case, state what evidence would make the result decision-quality and which candidate models fail or survive.

## Evidence expectations

Ground first in live `main`, the Governance architecture, the controlled revision contract, the Operational continuity contract, the Determinism Contract, the Governance evidence contract, the research operating model/register, the planning admission rule, and the current Governance implementation and boundary tests. Search semantic neighbors across Operational observations, runtime provenance, controlled revision history, conformance results, and existing research.

External evidence is required only if it discriminates a candidate, exposes a failure mode, or establishes a material interoperability/consequence constraint. Any such source must be verified and cited with precise provenance; unverified background must be labeled as such. Absence claims must state the repository search scope.

## Falsification conditions

- A concrete current consumer proves that context-bound identity is necessary to preserve a distinction that source-record identity with explicit uses cannot preserve.
- A current adopted contract already settles a load-bearing identity, equality, history, or applicability fact that the investigation would otherwise treat as open.
- A proving case shows that the surviving model cannot preserve historical attribution, explicit applicability, or ownership separation without importing a deferred mechanism as semantic contract.
- Evidence demonstrates that producer-owned analytical provenance cannot be carried through a generic Governance use without Governance taking ownership of producer semantics.

## Adversarial-review plan

This is a high-risk question. Before any durable architecture or specification change is accepted, an independent reviewer must reconstruct the constraints and candidate set before reading the report recommendation, then challenge omitted candidates, hidden assumptions, ownership inversion, stale repository authority, possibility-treated-as-necessity, over-generalization, and conclusions stronger than the evidence.

## Exit criteria

Stop only when the report identifies the surviving semantic contract (or concludes that no shared contract is yet justified), evaluates every candidate against the discriminating proving cases, records unresolved unknowns and reopening triggers, separates semantic obligations from mechanism choices, and recommends a durable destination. The conclusion is not decision-quality for architecture promotion until the required independent adversarial review is complete.

## Expected durable destination

Focused architecture/specification reconciliation if a minimum contract survives; otherwise an explicit no-action or narrower research destination. Only after that reconciliation may the Governance plan be promoted to `READY_NEXT` and admit a concrete implementation responsibility. *(Outcome: the Governance evidence contract was adopted; the plan was promoted in the same reconciliation.)*

## Follow-up / reopening triggers

Reopen or narrow the question if a concrete producer or consumer needs a provenance class not covered here, if Operational correspondence or Engine analytics changes a load-bearing ownership boundary, if cross-version compatibility becomes a concrete interoperability requirement, or if implementation evidence falsifies the accepted semantic contract.

## READY alignment check

This brief is bounded by one implementation-admission decision, names the high-risk semantic scope and explicit non-goals, includes a current/simple candidate and alternatives, derives failure modes and proving cases, defines evidence and falsification expectations, states exit criteria and a durable destination, and requires independent adversarial review before architecture promotion.
