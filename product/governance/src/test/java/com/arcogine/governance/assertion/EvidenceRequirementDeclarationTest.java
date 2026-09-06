package com.arcogine.governance.assertion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.governance.requirement.RequirementId;
import com.arcogine.governance.requirement.RequirementVersion;
import java.lang.reflect.Modifier;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Proves invariant 4 (structural vs. external-evidence declaration) and the explicit G3/G4/G5
 * boundary: production assertion types declare evidence need only, never a conformance result or
 * evidence record.
 */
class EvidenceRequirementDeclarationTest {

    private static final RequirementId REQUIREMENT_ID = new RequirementId("arc.test.requirement");
    private static final RequirementVersion REQUIREMENT_VERSION = new RequirementVersion(1);

    @Test
    void structuralAssertionDeclaresModelStateSufficient() {
        Assertion<Integer> assertion =
                new Assertion<>(
                        new AssertionId("arc.test.structural"),
                        new AssertionVersion(1),
                        REQUIREMENT_ID,
                        REQUIREMENT_VERSION,
                        "structural check",
                        EvidenceRequirement.MODEL_STATE_SUFFICIENT,
                        value -> StructuralAssertionOutcome.satisfied("model state only"));

        assertEquals(EvidenceRequirement.MODEL_STATE_SUFFICIENT, assertion.evidenceRequirement());
        assertFalse(assertion.requiresExternalEvidence());
        assertTrue(assertion.ruleOptional().isPresent());
    }

    @Test
    void observationDependentAssertionDeclaresExternalEvidenceRequired() {
        Assertion<Void> assertion =
                new Assertion<>(
                        new AssertionId("arc.test.observation-dependent"),
                        new AssertionVersion(1),
                        REQUIREMENT_ID,
                        REQUIREMENT_VERSION,
                        "requires an external observation (e.g. MFA login event) to decide",
                        EvidenceRequirement.EXTERNAL_EVIDENCE_REQUIRED,
                        null);

        assertEquals(EvidenceRequirement.EXTERNAL_EVIDENCE_REQUIRED, assertion.evidenceRequirement());
        assertTrue(assertion.requiresExternalEvidence());
        // G3 declares the need only -- no structural rule is required or expected for this case,
        // and G3 does not implement G5 evidence ingestion/persistence to satisfy it.
        assertTrue(assertion.ruleOptional().isEmpty());
    }

    @Test
    void structuralAssertionOutcomeNullExplanationDefaultsToBlankAndToStringIsReadable() {
        StructuralAssertionOutcome satisfiedNoExplanation = new StructuralAssertionOutcome(true, null);
        StructuralAssertionOutcome violatedWithExplanation = StructuralAssertionOutcome.violated("5 <= 0");

        assertEquals("", satisfiedNoExplanation.explanation());
        assertEquals("true", satisfiedNoExplanation.toString());
        assertTrue(violatedWithExplanation.toString().contains("5 <= 0"));
    }

    @Test
    void modelStateSufficientAssertionMustSupplyAStructuralRule() {
        // A MODEL_STATE_SUFFICIENT declaration is a claim that the assertion is
        // decidable from authoritative model state alone -- construction must reject the
        // contradiction of that claim with no executable rule, rather than deferring failure to
        // evaluate() time.
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new Assertion<Integer>(
                                new AssertionId("arc.test.rule-less-structural"),
                                new AssertionVersion(1),
                                REQUIREMENT_ID,
                                REQUIREMENT_VERSION,
                                "claims model-state-sufficient with no rule",
                                EvidenceRequirement.MODEL_STATE_SUFFICIENT,
                                null));
    }

    @Test
    void productionAssertionAndRequirementTypesContainNoG4OrG5Concepts() {
        Set<String> forbiddenConceptFragments =
                Set.of(
                        "conformanceresult",
                        "finding",
                        "evidenceuse",
                        "approval",
                        "authorization",
                        "deployment",
                        "exception",
                        "riskacceptance",
                        "frameworkcontrolmapping");
        // "evidence" itself is allowed (EvidenceRequirement is the G3 declaration seam); the
        // forbidden fragments above are the G4/G5/G6 concepts G3 must not pull forward.
        Class<?>[] productionTypes = {
            Assertion.class,
            AssertionId.class,
            AssertionVersion.class,
            EvidenceRequirement.class,
            StructuralAssertionOutcome.class,
            com.arcogine.governance.requirement.Requirement.class,
            com.arcogine.governance.requirement.RequirementId.class,
            com.arcogine.governance.requirement.RequirementVersion.class,
            com.arcogine.governance.requirement.RequirementScope.class,
            com.arcogine.governance.requirement.RequirementSource.class,
            com.arcogine.governance.catalogue.RequirementCatalogue.class
        };
        for (Class<?> type : productionTypes) {
            String typeName = type.getName().toLowerCase();
            for (String forbidden : forbiddenConceptFragments) {
                assertFalse(typeName.contains(forbidden), type.getName());
            }
            for (var field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                String fieldName = field.getName().toLowerCase();
                for (String forbidden : forbiddenConceptFragments) {
                    assertFalse(fieldName.contains(forbidden), type.getName() + "#" + field.getName());
                }
            }
        }
    }
}
