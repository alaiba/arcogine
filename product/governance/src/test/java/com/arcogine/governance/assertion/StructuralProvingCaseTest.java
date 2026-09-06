package com.arcogine.governance.assertion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.governance.requirement.ArcogineNativeRequirementSource;
import com.arcogine.governance.requirement.Requirement;
import com.arcogine.governance.requirement.RequirementId;
import com.arcogine.governance.requirement.RequirementScope;
import com.arcogine.governance.requirement.RequirementVersion;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * First structural proving case for the requirement-scope capability: an Arcogine-native requirement backed by a deterministic,
 * headless structural assertion, evaluated purely from authoritative semantic facts.
 *
 * <p>Per the requirement-scope acceptance criteria, this uses a minimal test-domain fixture ({@link
 * DeclaredResource}) rather than manufacturing a permanent factory policy or duplicating {@code
 * FactoryModelValidator} as a competing executability authority. It proves the contract is
 * headlessly operable -- no Spring, no HTTP/API DTOs, no frontend code, no mutable runtime state,
 * no external evidence, no compliance framework -- while leaving the choice of a real permanent
 * factory-owned invariant to a future conformance-evaluation slice with actual conformance-evaluation semantics.
 */
class StructuralProvingCaseTest {

    private record DeclaredResource(String id, double capacityLiters) {}

    @Test
    void arcogineNativeStructuralRequirementIsHeadlesslyEvaluableFromAuthoritativeState() {
        Requirement requirement =
                new Requirement(
                        new RequirementId("arc.test.declared-capacity-must-be-positive"),
                        new RequirementVersion(1),
                        "Declared resource capacity must be positive",
                        "Every declared resource must state a strictly positive capacity; this is an "
                                + "Arcogine-native structural invariant, not an external-standard requirement.",
                        ArcogineNativeRequirementSource.of("minimum viable structural invariant, requirement-scope proving case"),
                        RequirementScope.empty());
        Assertion<DeclaredResource> assertion =
                new Assertion<>(
                        new AssertionId("arc.test.declared-capacity-must-be-positive.rule"),
                        new AssertionVersion(1),
                        requirement.id(),
                        requirement.version(),
                        "capacityLiters > 0",
                        EvidenceRequirement.MODEL_STATE_SUFFICIENT,
                        resource ->
                                resource.capacityLiters() > 0
                                        ? StructuralAssertionOutcome.satisfied(
                                                resource.id() + " capacity " + resource.capacityLiters() + " > 0")
                                        : StructuralAssertionOutcome.violated(
                                                resource.id() + " capacity " + resource.capacityLiters() + " <= 0"));

        List<DeclaredResource> authoritativeState =
                List.of(new DeclaredResource("press-1", 12.5), new DeclaredResource("tank-1", -1));

        assertEquals(EvidenceRequirement.MODEL_STATE_SUFFICIENT, assertion.evidenceRequirement());
        assertFalse(assertion.requiresExternalEvidence());
        assertTrue(assertion.evaluate(authoritativeState.get(0)).satisfied());
        assertFalse(assertion.evaluate(authoritativeState.get(1)).satisfied());
        // Requirement wording and assertion executable semantics remain independently accessible.
        assertTrue(requirement.description().contains("Arcogine-native"));
        assertEquals("capacityLiters > 0", assertion.description());
    }
}
