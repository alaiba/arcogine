package com.arcogine.governance.conformance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.governance.ControlledRevision;
import com.arcogine.governance.ControlledRevisionAuthority;
import com.arcogine.governance.GovernanceHistoryException;
import com.arcogine.governance.HistoricalRevision;
import com.arcogine.governance.RevisionProvenance;
import com.arcogine.governance.RevisionRecorder;
import com.arcogine.governance.SemanticArtifact;
import com.arcogine.governance.assertion.Assertion;
import com.arcogine.governance.assertion.AssertionId;
import com.arcogine.governance.assertion.AssertionVersion;
import com.arcogine.governance.assertion.EvidenceRequirement;
import com.arcogine.governance.assertion.StructuralAssertionOutcome;
import com.arcogine.governance.change.ChangedEntityRef;
import com.arcogine.governance.change.ImpactScope;
import com.arcogine.governance.change.SemanticChange;
import com.arcogine.governance.change.SemanticChangeKind;
import com.arcogine.governance.requirement.ArcogineNativeRequirementSource;
import com.arcogine.governance.requirement.Requirement;
import com.arcogine.governance.requirement.RequirementId;
import com.arcogine.governance.requirement.RequirementScope;
import com.arcogine.governance.requirement.RequirementVersion;
import com.arcogine.types.ControlledRevisionId;
import com.arcogine.types.ModelFingerprint;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

/**
 * G4 acceptance tests for {@link ConformanceEvaluator} against the real G2/G3 contracts (no
 * evaluation-specific test doubles for {@code Requirement}/{@code Assertion}/{@code ImpactScope}).
 */
class ConformanceEvaluatorTest {

    private record DeclaredResource(String id, double capacityLiters) {}

    private static final ModelFingerprint FINGERPRINT =
            new ModelFingerprint("test-domain", "v1", "sha-256", "deadbeef");
    private static final ModelFingerprint OTHER_FINGERPRINT =
            new ModelFingerprint("test-domain", "v1", "sha-256", "c0ffee");

    /**
     * The G4 provenance-binding seam ({@code stateFingerprint}) exercised with a domain adapter
     * that independently derives {@code FINGERPRINT} from a {@code DeclaredResource} -- standing in
     * for a real domain fingerprinter (e.g. {@code ChangeSet::candidateFingerprint}) the way this
     * test suite's other real-G2/G3 objects stand in for a full deployment.
     */
    private static final Function<DeclaredResource, ModelFingerprint> RESOURCE_FINGERPRINT =
            resource -> FINGERPRINT;

    private static final RequirementId REQUIREMENT_ID =
            new RequirementId("arc.test.declared-capacity-must-be-positive");
    private static final RequirementVersion REQUIREMENT_VERSION = new RequirementVersion(1);
    private static final AssertionId ASSERTION_ID =
            new AssertionId("arc.test.declared-capacity-must-be-positive.rule");
    private static final AssertionVersion ASSERTION_VERSION = new AssertionVersion(1);

    /**
     * Minimal in-memory {@link ControlledRevisionAuthority} test double, used only to exercise the
     * real G1 authoritative-binding contract ({@code resolve} throwing for anything never {@code
     * accept}-ed) without depending on the filesystem-backed {@code FileControlledRevisionAuthority}.
     */
    private static final class InMemoryControlledRevisionAuthority implements ControlledRevisionAuthority {
        private final Map<ControlledRevisionId, HistoricalRevision> accepted = new LinkedHashMap<>();

        @Override
        public ControlledRevision accept(ControlledRevision candidate, SemanticArtifact artifact) {
            ControlledRevision recorded =
                    new ControlledRevision(
                            candidate.id(),
                            candidate.modelFingerprint(),
                            candidate.parentRevisionIds(),
                            new RevisionProvenance(Instant.EPOCH, candidate.provenance().recorder()));
            accepted.put(recorded.id(), new HistoricalRevision(recorded, artifact));
            return recorded;
        }

        @Override
        public Optional<ControlledRevision> findById(ControlledRevisionId id) {
            return Optional.ofNullable(accepted.get(id)).map(HistoricalRevision::revision);
        }

        @Override
        public HistoricalRevision resolve(ControlledRevisionId id) {
            HistoricalRevision resolved = accepted.get(id);
            if (resolved == null) {
                throw new GovernanceHistoryException(
                        GovernanceHistoryException.Code.MISSING_REVISION,
                        "controlled revision does not exist: " + id);
            }
            return resolved;
        }

        @Override
        public List<ControlledRevision> revisions() {
            return accepted.values().stream().map(HistoricalRevision::revision).toList();
        }

        ControlledRevisionId acceptFor(ModelFingerprint fingerprint) {
            ControlledRevisionId id = ControlledRevisionId.generate();
            ControlledRevision candidate =
                    new ControlledRevision(
                            id, fingerprint, List.of(), new RevisionProvenance(Instant.EPOCH, RECORDER));
            accept(candidate, new SemanticArtifact(fingerprint, new byte[] {1, 2, 3}));
            return id;
        }
    }

    private static final RevisionRecorder RECORDER = new RevisionRecorder("test", "operator");

    @Test
    void deterministicPassYieldsPassResult() {
        Requirement requirement = structuralRequirement(RequirementScope.empty());
        Assertion<DeclaredResource> assertion = structuralAssertion();
        DeclaredResource satisfying = new DeclaredResource("press-1", 12.5);
        ControlledRevisionAuthority authority = new InMemoryControlledRevisionAuthority();

        ConformanceEvaluation evaluation =
                ConformanceEvaluator.evaluate(
                        requirement,
                        assertion,
                        Optional.empty(),
                        Optional.of(satisfying),
                        FINGERPRINT,
                        RESOURCE_FINGERPRINT,
                        Optional.empty(),
                        authority);

        assertEquals(ConformanceResult.PASS, evaluation.result());
        // Re-evaluating the same inputs again must produce an equal result -- no hidden clock,
        // random source, or mutable state influences the outcome.
        ConformanceEvaluation repeated =
                ConformanceEvaluator.evaluate(
                        requirement,
                        assertion,
                        Optional.empty(),
                        Optional.of(satisfying),
                        FINGERPRINT,
                        RESOURCE_FINGERPRINT,
                        Optional.empty(),
                        authority);
        assertEquals(evaluation, repeated);
    }

    @Test
    void passProducesNoFinding() {
        Requirement requirement = structuralRequirement(RequirementScope.empty());
        Assertion<DeclaredResource> assertion = structuralAssertion();

        ConformanceEvaluation evaluation =
                ConformanceEvaluator.evaluate(
                        requirement,
                        assertion,
                        Optional.empty(),
                        Optional.of(new DeclaredResource("press-1", 12.5)),
                        FINGERPRINT,
                        RESOURCE_FINGERPRINT,
                        Optional.empty(),
                        new InMemoryControlledRevisionAuthority());

        assertEquals(ConformanceResult.PASS, evaluation.result());
        assertTrue(evaluation.findingOptional().isEmpty());
    }

    @Test
    void deterministicFailProducesFinding() {
        Requirement requirement = structuralRequirement(RequirementScope.empty());
        Assertion<DeclaredResource> assertion = structuralAssertion();
        DeclaredResource violating = new DeclaredResource("tank-1", -1);

        ConformanceEvaluation evaluation =
                ConformanceEvaluator.evaluate(
                        requirement,
                        assertion,
                        Optional.empty(),
                        Optional.of(violating),
                        FINGERPRINT,
                        RESOURCE_FINGERPRINT,
                        Optional.empty(),
                        new InMemoryControlledRevisionAuthority());

        assertEquals(ConformanceResult.FAIL, evaluation.result());
        Finding finding = evaluation.findingOptional().orElseThrow();
        assertEquals(REQUIREMENT_ID, finding.requirementId());
        assertEquals(REQUIREMENT_VERSION, finding.requirementVersion());
        assertEquals(ASSERTION_ID, finding.assertionId());
        assertEquals(ASSERTION_VERSION, finding.assertionVersion());
        assertEquals(FINGERPRINT, finding.modelFingerprint());
        assertTrue(finding.explanation().contains("tank-1"));
    }

    @Test
    void stateNotBoundToTheDeclaredFingerprintCannotBeAttributedToAnEvaluation() {
        // REV-001: modelFingerprint and authoritativeState must be verified as describing the same
        // artifact, not merely two independent caller-supplied values. A state whose independently
        // derived fingerprint disagrees with the declared modelFingerprint must be rejected before
        // any PASS/FAIL/Finding can be attributed to that fingerprint.
        Requirement requirement = structuralRequirement(RequirementScope.empty());
        Assertion<DeclaredResource> assertion = structuralAssertion();
        Function<DeclaredResource, ModelFingerprint> unrelatedFingerprint = resource -> OTHER_FINGERPRINT;

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        ConformanceEvaluator.evaluate(
                                requirement,
                                assertion,
                                Optional.empty(),
                                Optional.of(new DeclaredResource("tank-1", -1)),
                                FINGERPRINT,
                                unrelatedFingerprint,
                                Optional.empty(),
                                new InMemoryControlledRevisionAuthority()));
    }

    @Test
    void anUnsuppliedStateNeverInvokesTheFingerprintBindingCheck() {
        // No authoritativeState means nothing was actually evaluated against modelFingerprint --
        // the UNKNOWN path -- so a fingerprinter that would reject any state must never even run.
        Requirement requirement = structuralRequirement(RequirementScope.empty());
        Assertion<DeclaredResource> assertion = structuralAssertion();
        Function<DeclaredResource, ModelFingerprint> explodingFingerprint =
                resource -> {
                    throw new AssertionError("must not be invoked when authoritativeState is empty");
                };

        ConformanceEvaluation evaluation =
                ConformanceEvaluator.evaluate(
                        requirement,
                        assertion,
                        Optional.empty(),
                        Optional.empty(),
                        FINGERPRINT,
                        explodingFingerprint,
                        Optional.empty(),
                        new InMemoryControlledRevisionAuthority());

        assertEquals(ConformanceResult.UNKNOWN, evaluation.result());
    }

    @Test
    void failWithNoImpactScopeAttributesTheFullRequirementScopeAsAffected() {
        ChangedEntityRef entity = new ChangedEntityRef("factory.resource", "tank-1", "");
        Requirement requirement = structuralRequirement(RequirementScope.of(entity));
        Assertion<DeclaredResource> assertion = structuralAssertion();

        ConformanceEvaluation evaluation =
                ConformanceEvaluator.evaluate(
                        requirement,
                        assertion,
                        Optional.empty(),
                        Optional.of(new DeclaredResource("tank-1", -1)),
                        FINGERPRINT,
                        RESOURCE_FINGERPRINT,
                        Optional.empty(),
                        new InMemoryControlledRevisionAuthority());

        Finding finding = evaluation.findingOptional().orElseThrow();
        assertEquals(List.of(entity), finding.affectedEntities());
    }

    @Test
    void failWithPartialImpactScopeAttributesOnlyTheIntersectingEntities() {
        ChangedEntityRef impacted = new ChangedEntityRef("factory.resource", "tank-1", "");
        ChangedEntityRef notImpacted = new ChangedEntityRef("factory.resource", "tank-2", "");
        Requirement requirement =
                structuralRequirement(RequirementScope.of(impacted, notImpacted));
        // A structural rule over the whole multi-entity requirement; only tank-1 is asserted here,
        // but the requirement scope covers both -- the impact scope is what narrows attribution.
        Assertion<DeclaredResource> assertion = structuralAssertion();
        ImpactScope partialImpact =
                ImpactScope.of(
                        List.of(
                                new SemanticChange(
                                        SemanticChangeKind.ENTITY_MODIFIED, impacted, "capacity lowered")));

        ConformanceEvaluation evaluation =
                ConformanceEvaluator.evaluate(
                        requirement,
                        assertion,
                        Optional.of(partialImpact),
                        Optional.of(new DeclaredResource("tank-1", -1)),
                        FINGERPRINT,
                        RESOURCE_FINGERPRINT,
                        Optional.empty(),
                        new InMemoryControlledRevisionAuthority());

        assertEquals(ConformanceResult.FAIL, evaluation.result());
        Finding finding = evaluation.findingOptional().orElseThrow();
        assertEquals(List.of(impacted), finding.affectedEntities());
        assertFalse(finding.affectedEntities().contains(notImpacted));
    }

    @Test
    void missingEvidenceRequirementIsUnknownNotFailOrPass() {
        Requirement requirement = structuralRequirement(RequirementScope.empty());
        Assertion<DeclaredResource> externalEvidenceAssertion =
                new Assertion<>(
                        ASSERTION_ID,
                        ASSERTION_VERSION,
                        REQUIREMENT_ID,
                        REQUIREMENT_VERSION,
                        "requires an external observation G4 cannot supply",
                        EvidenceRequirement.EXTERNAL_EVIDENCE_REQUIRED,
                        null);

        ConformanceEvaluation evaluation =
                ConformanceEvaluator.evaluate(
                        requirement,
                        externalEvidenceAssertion,
                        Optional.empty(),
                        Optional.empty(),
                        FINGERPRINT,
                        RESOURCE_FINGERPRINT,
                        Optional.empty(),
                        new InMemoryControlledRevisionAuthority());

        assertEquals(ConformanceResult.UNKNOWN, evaluation.result());
        assertNotEquals(ConformanceResult.FAIL, evaluation.result());
        assertNotEquals(ConformanceResult.PASS, evaluation.result());
        assertTrue(evaluation.findingOptional().isEmpty());
    }

    @Test
    void unknownResultNeverCarriesAFindingAndIsDistinctFromFail() {
        Requirement requirement = structuralRequirement(RequirementScope.empty());
        Assertion<DeclaredResource> assertion = structuralAssertion();

        // MODEL_STATE_SUFFICIENT, but no authoritative state was supplied to decide it with.
        ConformanceEvaluation evaluation =
                ConformanceEvaluator.evaluate(
                        requirement,
                        assertion,
                        Optional.empty(),
                        Optional.empty(),
                        FINGERPRINT,
                        RESOURCE_FINGERPRINT,
                        Optional.empty(),
                        new InMemoryControlledRevisionAuthority());

        assertEquals(ConformanceResult.UNKNOWN, evaluation.result());
        assertTrue(evaluation.findingOptional().isEmpty());
    }

    @Test
    void notApplicableIsDistinctFromPassFailAndUnknown() {
        ChangedEntityRef inScope = new ChangedEntityRef("factory.resource", "press-1", "");
        ChangedEntityRef outOfScope = new ChangedEntityRef("factory.resource", "unrelated-99", "");
        Requirement requirement = structuralRequirement(RequirementScope.of(inScope));
        Assertion<DeclaredResource> assertion = structuralAssertion();
        ImpactScope unrelatedImpact =
                ImpactScope.of(
                        List.of(
                                new SemanticChange(
                                        SemanticChangeKind.ENTITY_MODIFIED, outOfScope, "unrelated change")));

        ConformanceEvaluation evaluation =
                ConformanceEvaluator.evaluate(
                        requirement,
                        assertion,
                        Optional.of(unrelatedImpact),
                        Optional.of(new DeclaredResource("press-1", -1)),
                        FINGERPRINT,
                        RESOURCE_FINGERPRINT,
                        Optional.empty(),
                        new InMemoryControlledRevisionAuthority());

        assertEquals(ConformanceResult.NOT_APPLICABLE, evaluation.result());
        assertTrue(evaluation.findingOptional().isEmpty());
        assertNotEquals(ConformanceResult.PASS, evaluation.result());
        assertNotEquals(ConformanceResult.FAIL, evaluation.result());
        assertNotEquals(ConformanceResult.UNKNOWN, evaluation.result());
    }

    @Test
    void emptyRequirementScopeIsNeverApplicableWhenAnImpactScopeIsSupplied() {
        // G3's invariant: an empty scope never matches any impact (RequirementScopeTest
        // .emptyScopeIsEmptyAndNeverIntersects). A supplied ImpactScope must preserve that exactly.
        ChangedEntityRef changed = new ChangedEntityRef("factory.resource", "press-1", "");
        Requirement requirement = structuralRequirement(RequirementScope.empty());
        Assertion<DeclaredResource> assertion = structuralAssertion();
        ImpactScope impact =
                ImpactScope.of(
                        List.of(
                                new SemanticChange(SemanticChangeKind.ENTITY_MODIFIED, changed, "changed")));

        ConformanceEvaluation evaluation =
                ConformanceEvaluator.evaluate(
                        requirement,
                        assertion,
                        Optional.of(impact),
                        Optional.of(new DeclaredResource("press-1", -1)),
                        FINGERPRINT,
                        RESOURCE_FINGERPRINT,
                        Optional.empty(),
                        new InMemoryControlledRevisionAuthority());

        assertEquals(ConformanceResult.NOT_APPLICABLE, evaluation.result());
        assertTrue(evaluation.findingOptional().isEmpty());
    }

    @Test
    void controlledRevisionIsAbsentAndNeverSynthesizedForAnUnpersistedCandidate() {
        Requirement requirement = structuralRequirement(RequirementScope.empty());
        Assertion<DeclaredResource> assertion = structuralAssertion();

        ConformanceEvaluation evaluation =
                ConformanceEvaluator.evaluate(
                        requirement,
                        assertion,
                        Optional.empty(),
                        Optional.of(new DeclaredResource("tank-1", -1)),
                        FINGERPRINT,
                        RESOURCE_FINGERPRINT,
                        Optional.empty(),
                        new InMemoryControlledRevisionAuthority());

        assertTrue(evaluation.controlledRevisionIdOptional().isEmpty());
        Finding finding = evaluation.findingOptional().orElseThrow();
        assertTrue(finding.controlledRevisionIdOptional().isEmpty());
    }

    @Test
    void controlledRevisionIsCarriedThroughWhenAuthoritativelyBoundToTheEvaluatedFingerprint() {
        Requirement requirement = structuralRequirement(RequirementScope.empty());
        Assertion<DeclaredResource> assertion = structuralAssertion();
        InMemoryControlledRevisionAuthority authority = new InMemoryControlledRevisionAuthority();
        ControlledRevisionId revisionId = authority.acceptFor(FINGERPRINT);

        ConformanceEvaluation evaluation =
                ConformanceEvaluator.evaluate(
                        requirement,
                        assertion,
                        Optional.empty(),
                        Optional.of(new DeclaredResource("tank-1", -1)),
                        FINGERPRINT,
                        RESOURCE_FINGERPRINT,
                        Optional.of(revisionId),
                        authority);

        assertEquals(revisionId, evaluation.controlledRevisionIdOptional().orElseThrow());
        Finding finding = evaluation.findingOptional().orElseThrow();
        assertEquals(revisionId, finding.controlledRevisionIdOptional().orElseThrow());
    }

    @Test
    void generatedButNeverAcceptedRevisionCannotBeAttributedToAnEvaluation() {
        Requirement requirement = structuralRequirement(RequirementScope.empty());
        Assertion<DeclaredResource> assertion = structuralAssertion();
        ControlledRevisionId neverAccepted = ControlledRevisionId.generate();
        ControlledRevisionAuthority authority = new InMemoryControlledRevisionAuthority();

        assertThrows(
                GovernanceHistoryException.class,
                () ->
                        ConformanceEvaluator.evaluate(
                                requirement,
                                assertion,
                                Optional.empty(),
                                Optional.of(new DeclaredResource("tank-1", -1)),
                                FINGERPRINT,
                                RESOURCE_FINGERPRINT,
                                Optional.of(neverAccepted),
                                authority));
    }

    @Test
    void revisionAuthoritativelyBoundToADifferentFingerprintCannotBeAttributedToAnEvaluation() {
        Requirement requirement = structuralRequirement(RequirementScope.empty());
        Assertion<DeclaredResource> assertion = structuralAssertion();
        InMemoryControlledRevisionAuthority authority = new InMemoryControlledRevisionAuthority();
        // Accepted for a different fingerprint than the one this evaluation claims to evaluate.
        ControlledRevisionId mismatchedRevision = authority.acceptFor(OTHER_FINGERPRINT);

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        ConformanceEvaluator.evaluate(
                                requirement,
                                assertion,
                                Optional.empty(),
                                Optional.of(new DeclaredResource("tank-1", -1)),
                                FINGERPRINT,
                                RESOURCE_FINGERPRINT,
                                Optional.of(mismatchedRevision),
                                authority));
    }

    @Test
    void evaluateRejectsAnAssertionThatDoesNotTargetTheRequirement() {
        Requirement requirement = structuralRequirement(RequirementScope.empty());
        Assertion<DeclaredResource> mismatched =
                new Assertion<>(
                        new AssertionId("some.other.assertion"),
                        ASSERTION_VERSION,
                        new RequirementId("some.other.requirement"),
                        REQUIREMENT_VERSION,
                        "unrelated",
                        EvidenceRequirement.MODEL_STATE_SUFFICIENT,
                        r -> StructuralAssertionOutcome.satisfied(""));

        assertFalse(
                requirement.id().equals(mismatched.requirementId())
                        && requirement.version().equals(mismatched.requirementVersion()));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        ConformanceEvaluator.evaluate(
                                requirement,
                                mismatched,
                                Optional.empty(),
                                Optional.of(new DeclaredResource("press-1", 12.5)),
                                FINGERPRINT,
                                RESOURCE_FINGERPRINT,
                                Optional.empty(),
                                new InMemoryControlledRevisionAuthority()));
    }

    private static Requirement structuralRequirement(RequirementScope scope) {
        return new Requirement(
                REQUIREMENT_ID,
                REQUIREMENT_VERSION,
                "Declared resource capacity must be positive",
                "Every declared resource must state a strictly positive capacity.",
                ArcogineNativeRequirementSource.of("minimum viable structural invariant, G4 proving case"),
                scope);
    }

    private static Assertion<DeclaredResource> structuralAssertion() {
        return new Assertion<>(
                ASSERTION_ID,
                ASSERTION_VERSION,
                REQUIREMENT_ID,
                REQUIREMENT_VERSION,
                "capacityLiters > 0",
                EvidenceRequirement.MODEL_STATE_SUFFICIENT,
                resource ->
                        resource.capacityLiters() > 0
                                ? StructuralAssertionOutcome.satisfied(
                                        resource.id() + " capacity " + resource.capacityLiters() + " > 0")
                                : StructuralAssertionOutcome.violated(
                                        resource.id() + " capacity " + resource.capacityLiters() + " <= 0"));
    }
}
