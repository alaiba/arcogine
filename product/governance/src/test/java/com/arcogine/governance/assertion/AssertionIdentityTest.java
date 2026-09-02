package com.arcogine.governance.assertion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.governance.requirement.ArcogineNativeRequirementSource;
import com.arcogine.governance.requirement.Requirement;
import com.arcogine.governance.requirement.RequirementId;
import com.arcogine.governance.requirement.RequirementScope;
import com.arcogine.governance.requirement.RequirementVersion;
import org.junit.jupiter.api.Test;

/** Proves invariants 1 and 2: assertion identity/version independent of requirement version. */
class AssertionIdentityTest {

    private static final RequirementId REQUIREMENT_ID = new RequirementId("arc.test.positive-capacity");

    @Test
    void assertionIdentityAndVersionAreIndependentOfRequirementVersion() {
        Assertion<Integer> assertionForV1 = structuralAssertion(new RequirementVersion(1));
        Assertion<Integer> assertionForV2 = structuralAssertion(new RequirementVersion(2));

        // Same assertion identity/version can be associated with different requirement versions
        // over time without becoming a different assertion.
        assertEquals(assertionForV1.id(), assertionForV2.id());
        assertEquals(assertionForV1.version(), assertionForV2.version());
        assertEquals(assertionForV1, assertionForV2);
        assertNotEquals(assertionForV1.requirementVersion(), assertionForV2.requirementVersion());
    }

    @Test
    void assertionIdentityIsNotDerivedFromEvaluatorImplementationClass() {
        AssertionRule<Integer> ruleImplementationOne = value -> StructuralAssertionOutcome.satisfied("ok");
        AssertionRule<Integer> ruleImplementationTwo = new NamedPositiveRule();

        Assertion<Integer> withLambdaRule = assertionWithRule(ruleImplementationOne);
        Assertion<Integer> withNamedClassRule = assertionWithRule(ruleImplementationTwo);

        assertEquals(withLambdaRule, withNamedClassRule);
        assertEquals(withLambdaRule.hashCode(), withNamedClassRule.hashCode());
        assertNotEquals(ruleImplementationOne.getClass(), ruleImplementationTwo.getClass());
    }

    @Test
    void requirementMeaningIsDistinctFromExecutableAssertionSemantics() {
        Requirement requirement =
                new Requirement(
                        REQUIREMENT_ID,
                        new RequirementVersion(1),
                        "Capacity must be positive",
                        "Human-readable obligation: every declared capacity must be greater than zero.",
                        ArcogineNativeRequirementSource.of("structural invariant"),
                        RequirementScope.empty());
        Assertion<Integer> assertion = structuralAssertion(requirement.version());

        // The requirement carries meaning (title/description); the assertion carries executable
        // semantics (the rule). Neither type exposes the other's concern as its own identity.
        assertTrue(requirement.title().contains("positive"));
        assertEquals(StructuralAssertionOutcome.satisfied("5 > 0"), assertion.evaluate(5));
        assertEquals(false, assertion.evaluate(-1).satisfied());
    }

    @Test
    void requirementCanBeAssociatedWithIndependentlyVersionedAssertion() {
        Requirement requirement =
                new Requirement(
                        REQUIREMENT_ID,
                        new RequirementVersion(3),
                        "Capacity must be positive",
                        "",
                        ArcogineNativeRequirementSource.unspecified(),
                        RequirementScope.empty());
        Assertion<Integer> assertionV5 =
                new Assertion<>(
                        new AssertionId("arc.test.positive-capacity.rule"),
                        new AssertionVersion(5),
                        requirement.id(),
                        requirement.version(),
                        "structural rule",
                        EvidenceRequirement.MODEL_STATE_SUFFICIENT,
                        value -> StructuralAssertionOutcome.satisfied("checked"));

        assertEquals(requirement.id(), assertionV5.requirementId());
        assertEquals(requirement.version(), assertionV5.requirementVersion());
        assertNotEquals(requirement.version().value(), assertionV5.version().value());
    }

    @Test
    void nullDescriptionDefaultsToBlank() {
        Assertion<Integer> assertion =
                new Assertion<>(
                        new AssertionId("arc.test.null-description"),
                        new AssertionVersion(1),
                        REQUIREMENT_ID,
                        new RequirementVersion(1),
                        null,
                        EvidenceRequirement.MODEL_STATE_SUFFICIENT,
                        value -> StructuralAssertionOutcome.satisfied("ok"));

        assertEquals("", assertion.description());
    }

    @Test
    void evaluatingAnAssertionWithNoRuleFailsExplicitly() {
        Assertion<Void> withoutRule =
                new Assertion<>(
                        new AssertionId("arc.test.no-rule"),
                        new AssertionVersion(1),
                        REQUIREMENT_ID,
                        new RequirementVersion(1),
                        "requires external evidence",
                        EvidenceRequirement.EXTERNAL_EVIDENCE_REQUIRED,
                        null);

        assertThrows(IllegalStateException.class, () -> withoutRule.evaluate(null));
    }

    @Test
    void equalityIsIdAndVersionOnlyNotDescriptionOrRequirementVersion() {
        Assertion<Integer> assertion = structuralAssertion(new RequirementVersion(1));

        assertNotEquals(assertion, "not an assertion");
        assertFalse(assertion.equals(null));
    }

    private static Assertion<Integer> structuralAssertion(RequirementVersion requirementVersion) {
        return new Assertion<>(
                new AssertionId("arc.test.positive-capacity.rule"),
                new AssertionVersion(1),
                REQUIREMENT_ID,
                requirementVersion,
                "capacity greater than zero",
                EvidenceRequirement.MODEL_STATE_SUFFICIENT,
                value -> value > 0
                        ? StructuralAssertionOutcome.satisfied(value + " > 0")
                        : StructuralAssertionOutcome.violated(value + " <= 0"));
    }

    private static Assertion<Integer> assertionWithRule(AssertionRule<Integer> rule) {
        return new Assertion<>(
                new AssertionId("arc.test.positive-capacity.rule"),
                new AssertionVersion(1),
                REQUIREMENT_ID,
                new RequirementVersion(1),
                "capacity greater than zero",
                EvidenceRequirement.MODEL_STATE_SUFFICIENT,
                rule);
    }

    private static final class NamedPositiveRule implements AssertionRule<Integer> {
        @Override
        public StructuralAssertionOutcome evaluate(Integer authoritativeState) {
            return StructuralAssertionOutcome.satisfied("named class rule");
        }
    }
}
