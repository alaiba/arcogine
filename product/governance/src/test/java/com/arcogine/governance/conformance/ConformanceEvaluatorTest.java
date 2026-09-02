package com.arcogine.governance.conformance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * G4 acceptance tests for {@link ConformanceEvaluator} against the real G2/G3 contracts (no
 * evaluation-specific test doubles for {@code Requirement}/{@code Assertion}/{@code ImpactScope}).
 */
class ConformanceEvaluatorTest {

    private record DeclaredResource(String id, double capacityLiters) {}

    private static final ModelFingerprint FINGERPRINT =
            new ModelFingerprint("test-domain", "v1", "sha-256", "deadbeef");

    private static final RequirementId REQUIREMENT_ID =
            new RequirementId("arc.test.declared-capacity-must-be-positive");
    private static final RequirementVersion REQUIREMENT_VERSION = new RequirementVersion(1);
    private static final AssertionId ASSERTION_ID =
            new AssertionId("arc.test.declared-capacity-must-be-positive.rule");
    private static final AssertionVersion ASSERTION_VERSION = new AssertionVersion(1);

    @Test
    void deterministicPassYieldsPassResult() {
        Requirement requirement = structuralRequirement(RequirementScope.empty());
        Assertion<DeclaredResource> assertion = structuralAssertion();
        DeclaredResource satisfying = new DeclaredResource("press-1", 12.5);

        ConformanceEvaluation evaluation =
                ConformanceEvaluator.evaluate(
                        requirement,
                        assertion,
                        Optional.empty(),
                        Optional.of(satisfying),
                        FINGERPRINT,
                        Optional.empty());

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
                        Optional.empty());
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
                        Optional.empty());

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
                        Optional.empty());

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
                        Optional.empty());

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
                        requirement, assertion, Optional.empty(), Optional.empty(), FINGERPRINT, Optional.empty());

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
                        Optional.empty());

        assertEquals(ConformanceResult.NOT_APPLICABLE, evaluation.result());
        assertTrue(evaluation.findingOptional().isEmpty());
        assertNotEquals(ConformanceResult.PASS, evaluation.result());
        assertNotEquals(ConformanceResult.FAIL, evaluation.result());
        assertNotEquals(ConformanceResult.UNKNOWN, evaluation.result());
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
                        Optional.empty());

        assertTrue(evaluation.controlledRevisionIdOptional().isEmpty());
        Finding finding = evaluation.findingOptional().orElseThrow();
        assertTrue(finding.controlledRevisionIdOptional().isEmpty());
    }

    @Test
    void controlledRevisionIsCarriedThroughUnchangedWhenSupplied() {
        Requirement requirement = structuralRequirement(RequirementScope.empty());
        Assertion<DeclaredResource> assertion = structuralAssertion();
        ControlledRevisionId revisionId = ControlledRevisionId.generate();

        ConformanceEvaluation evaluation =
                ConformanceEvaluator.evaluate(
                        requirement,
                        assertion,
                        Optional.empty(),
                        Optional.of(new DeclaredResource("tank-1", -1)),
                        FINGERPRINT,
                        Optional.of(revisionId));

        assertEquals(revisionId, evaluation.controlledRevisionIdOptional().orElseThrow());
        Finding finding = evaluation.findingOptional().orElseThrow();
        assertEquals(revisionId, finding.controlledRevisionIdOptional().orElseThrow());
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
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () ->
                        ConformanceEvaluator.evaluate(
                                requirement,
                                mismatched,
                                Optional.empty(),
                                Optional.of(new DeclaredResource("press-1", 12.5)),
                                FINGERPRINT,
                                Optional.empty()));
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
