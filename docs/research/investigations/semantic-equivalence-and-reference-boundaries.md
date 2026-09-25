# Semantic equivalence and reference boundaries

> **Status:** Maintained framing for separate candidate questions; lifecycle and priority live in the [research register](../research-register.md)
> **Scope:** Concrete equivalence, reference, conversion, and commitment choices left open by the canonical vocabulary
> **Authority:** Research framing only; not a report, adopted answer, implementation plan, or new semantic identity scheme

## Starting boundary

The [canonical definitions](../../architecture/overview.md#determinism-equivalence-identity-and-durability)
separate determinism, persistence, referential stability, traceability, historical interpretability,
reproducibility, compatibility, and stability/support commitments. They also separate semantic
equivalence, canonical representation, fingerprints, runtime executability, and optional human
labels. Those distinctions are the owner-directed architectural starting point, not conclusions
that these questions must rediscover.

The unresolved work is to determine which concrete relations, references, and promises a real
Arcogine use needs. A specification can remain normative without being made into an independently
versioned runtime object. Conversely, calling a technical token a label does not remove its current
codec, digest, support-check, or provenance use. The current [Factory definition](../../architecture/factory-model.md),
[Engine rules](../../architecture/engine-semantics.md), strict artifact checks, and
[proving-store policy](../../architecture/controlled-revisions.md) remain the implementation baseline.
No field membership, ordering, normalization, fingerprint, dispatch behavior, storage policy, or
supported input is changed by recording this framing.

The five questions below are independent candidates, not one investigation requiring an umbrella
verdict. Before starting one, select its concrete use, input/observation scope, and discriminating
cases. Their presence in the register creates no new gate for unrelated implementation. Any proposed
change to identity/equality, persisted compatibility, determinism, or foundational ownership is
high-risk and requires the independent adversarial review specified by the
[research operating model](../../development/researching.md) before research-based architecture
reconciliation. Recording these candidates performs neither that research nor its review.

## Factory semantic equivalence

**Question.** For which authoritative Factory purposes should two authored designs count as the
same semantic content, and does the current canonical form preserve exactly those distinctions?

**Decision at stake.** Retain the current content-equivalence boundary or deliberately revise a
specific distinction. Do not begin by assuming equal simulation results imply equal designs.

**Candidates and scope.** Include the current field membership and normalization as the no-change
candidate; compare purpose-qualified alternatives for names, allocated/domain IDs, ordered lists
versus sets, spatial placement, and absent versus explicitly present values. A consumer-specific
comparison may be a projection rather than a replacement for the aggregate Factory identity.
Preserve the distinction between content equality and historical occurrence identity.

**Discriminating cases.** Consider a rename with unchanged production behavior; iteration-order
changes in eligible-resource sets; resource/operation/step list reordering; two designs yielding the
same outcome for one workload but differing for another; a whole-layout translation; and absent
spatial content versus an authored zero. Check the validity domain before applying an equivalence
relation. Similarity or numeric tolerance is not an equivalence relation unless its properties hold.

**Evidence and exit.** Identify the consumers and authoritative facts that make each difference
consequential or inconsequential. Compare specification, canonicalizer, validator, semantic
comparator, and relevant tests. A surviving answer states the domain, equivalence relation,
normalization obligations, counterexamples, and effects on already recorded content, or explicitly
retains current behavior. Golden vectors alone do not choose the relation.

**Start condition and destination.** Select a concrete equality/comparison requirement or a
contradictory current distinction first. Any adopted consequence belongs in the Factory model and
Factory Design specifications, with implementation consequences admitted separately. The concluded
[composition question](factory-model-semantic-composition.md) is context, not permission to assume
an open envelope, per-concern fingerprints, or a replacement identity architecture.

## Engine behavioral equivalence

**Question.** Over which input domain and observable boundary can two Engine interpretations or
implementations be considered behaviorally equivalent, and what evidence can establish that claim?

**Decision at stake.** Determine the scope of a same-behavior claim without silently weakening the
current simulation contract or treating a build hash as a semantic proof.

**Candidates and scope.** Include conformance to the existing complete supported behavior as the
baseline. Compare full supported traces and terminal observations with narrower, explicitly
consumer-scoped projections where a real use needs them. Distinguish intended specification,
actual implementation behavior, implementation conformance, and compatibility. A failing
implementation is not made conforming by reporting the expected interpretation name.

**Discriminating cases.** Same final throughput with different event order or completion times;
identical outputs for one workload but different acceptance/refusal on another; a behavior-preserving
refactor or compiler change; correction of an implementation bug versus changing a rule; correlation
IDs excluded by contract versus consequential domain IDs; numerical edge cases; and spatial content
that is valid but not executable. Sample tests can refute universal equivalence but do not prove it
for every possible input.

**Evidence and exit.** State the comparison domain, result projection, equality, conformance basis,
limits of test evidence, and counterexamples to stronger claims. Do not require a computable complete
canonicalizer for arbitrary program behavior. The result may preserve current conformance rules
without creating a separate semantic object.

**Start condition and destination.** Select a concrete cross-implementation, cross-revision,
optimization, or result-comparison claim. Engine specifications own any adopted behavioral boundary.
The [simulation-analytics question](simulation-analytics-consumer-boundary.md) continues to own
Engine-versus-analytics responsibility; this question must not silently resolve that separate issue.

## Exact reference boundaries

**Question.** At which codec, runtime, exchange, or historical boundaries must the exact meaning or
execution basis become explicitly resolvable, and does any of them require an independently
identified semantic-definition object?

**Decision at stake.** Keep sufficient current context, or introduce only the references a named
use cannot do without. Determine the referent before selecting its representation or friendly name.

**Candidates and scope.** Compare current process/specification context; an exact content-addressed
schema or specification reference; an implementation/build manifest; and a retained package of the
relevant definition, executable, configuration, and dependencies. Include no new public identity as
a serious candidate. A document hash identifies that document, not automatically its transitive
meaning or conformance; a build digest identifies code, not behavioral equivalence. Human labels are
optional aliases or claims, not a preferred identification mechanism.

**Discriminating cases.** Identical model bytes under changed validation or interpretation;
same-marker development artifacts; behavior-preserving build changes; two independent conforming
implementations; an unavailable referenced schema; a specification referring to a moving dependency;
an old result with a known build but missing inputs; and a partially implemented specification.
Examine existing `ModelFingerprint`, `EngineSemantics`, artifact-prefix/support checks, evidence
provenance, and the proving store's definition-build binding separately rather than treating them
as one token to rename.

**Evidence and exit.** Inventory actual consumers and the information they must identify, resolve,
or compare. For each proposed reference, state its referent, scope, transitive basis, binding,
resolution, failure behavior, and why existing context is insufficient. A no-new-object result is
valid. Do not introduce a registry, version ladder, public schema, or retention commitment merely to
make a reference available.

**Start condition and destination.** Bound one concrete decoding, result-comparison, experiment
reproduction, cache-validity, exchange, or historical-explanation case. Exact provenance can be useful
during development without a stability promise. Reconcile only the surviving responsibility into its
Factory, Engine, representation, or Governance authority. This is not a reopening of the discarded
ordinal definitions' amendment-permission debate.

## Conversion and migration semantics

**Question.** When a real consumer needs to read or convert earlier or external material, which
transformations preserve the required distinctions, which intentionally change them, and how must
assumptions, losses, and source-to-target attribution be expressed?

**Decision at stake.** Support a bounded import/migration path, retain direct reading of a source
format, or explicitly refuse material that cannot be interpreted or converted truthfully.

**Candidates and scope.** Compare refusal/no converter, a supported source decoder followed by a
mapping, and a direct target importer with an explicit source interpretation. Automatic conversion
is not excluded when its meaning is well-defined. Strict verification of canonical artifacts and
normalization of import input are different operations. Successful data conversion is not proof
that different Engine rules reproduce an old run.

**Discriminating cases.** Parseable but noncanonical bytes; a missing source definition; an absent
field for which a target requires a value; zero versus unknown; normalization that collapses an
important distinction; a changed validity predicate; and preservation of the original accepted
record while a transformed artifact acquires its own provenance.

**Evidence and exit.** Name the actual source and target definitions and supported consumer, prove
or qualify the preserved relation, state validation and refusal behavior, expose losses/assumptions,
and identify any retained basis or rollback need. A warning alone cannot supply unknown source
meaning. Do not build adapters for hypothetical data solely because a discarded token once existed.

**Start condition and destination.** A concrete import, retained-artifact, or live-upgrade need is
required before investigation. Representation and owning domain contracts receive a surviving
conversion rule; Governance receives only the attribution/custody consequences it owns. The current
no-legacy-codec and development-reset policy remains in force until a separate reviewed change.

## Scoped commitments and attestations

**Question.** Which concrete uses need a stability/support commitment, what exactly must be promised
and retained for them, and is a separate declaration or attestation useful to communicate that scope?

**Decision at stake.** Make the minimum support commitment a selected use needs, or make no new
commitment. This is deliberately a later question, not a prerequisite for deterministic computation,
canonicalization, content identity, or recording development provenance.

**Candidates and scope.** Compare the existing owning-contract declaration with a consumer-scoped
support declaration and, only when justified, a separately identifiable claim grouping exact Factory,
Engine, software/configuration, or deployment references. A friendly label may name any such claim;
it need not exist. No universal release, maturity, certification, or attestation entity is presumed.

**Discriminating cases.** Historical audit/explanation; live-instance upgrades; prospective
interoperability between independently evolving producers and consumers; reproducible development
experiments with no external promise; decoding retained content after old execution support ends;
partial support; expiry or withdrawal; and reliance discovered outside declared custody. Distinguish
recording that a party made a claim from proving the claim true or fulfilling its obligations.

**Evidence and exit.** Identify beneficiaries, exact referenced basis, supported inputs and uses,
retention horizon and dependencies, separate decoding/execution/migration/interoperability promises,
known gaps, failure behavior, and authorized change/retirement. Preserve already accepted history
and accountability. Return a scoped declaration requirement or a no-action result, not a generic
lifecycle framework.

**Start condition and destination.** Wait for a concrete commitment-bearing use or a demonstrated
gap in the [support policy](../../development/semantic-contract-support.md). The owning contracts and
Governance/Operational authorities receive only their respective consequences. The historical
[maturity investigation](semantic-contract-maturity-durability.md) remains historical provenance;
these candidate questions neither erase its surviving distinctions nor treat it as an answer to the
new use-specific question.
