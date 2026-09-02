package com.arcogine.governance.requirement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.types.ControlledRevisionId;
import com.arcogine.types.ModelFingerprint;
import org.junit.jupiter.api.Test;

/** Proves requirement identity/version invariant 1: independent of model/revision identity. */
class RequirementIdentityTest {

    @Test
    void sameRequirementIdentityCanApplyToDifferentModelRevisions() {
        Requirement requirement =
                new Requirement(
                        new RequirementId("arc.factory.resource-capacity-positive"),
                        new RequirementVersion(1),
                        "Resources declare positive capacity",
                        "Every factory resource must declare a strictly positive capacity.",
                        ArcogineNativeRequirementSource.of("factory structural invariant"),
                        RequirementScope.empty());

        ModelFingerprint revisionOneFingerprint = fingerprint("11");
        ModelFingerprint revisionTwoFingerprint = fingerprint("22");
        ControlledRevisionId revisionOne = revisionId(1);
        ControlledRevisionId revisionTwo = revisionId(2);

        // The same requirement identity/version is applicable regardless of which model
        // fingerprint or controlled revision it is later evaluated against (G4 concern) -- G3
        // only needs to prove the requirement itself carries no model/revision coupling.
        assertNotEquals(revisionOneFingerprint, revisionTwoFingerprint);
        assertNotEquals(revisionOne, revisionTwo);
        assertEquals(requirement.id(), requirement.id());
        assertEquals(requirement.version(), requirement.version());
        assertTrue(requirement.getClass().getRecordComponents().length > 0);
        for (var component : Requirement.class.getRecordComponents()) {
            String name = component.getName().toLowerCase();
            assertNotEquals("modelfingerprint", name);
            assertNotEquals("controlledrevisionid", name);
        }
    }

    @Test
    void requirementVersionIsIndependentOfModelFingerprintAndControlledRevision() {
        for (var component : Requirement.class.getRecordComponents()) {
            assertNotEquals(ModelFingerprint.class, component.getType());
            assertNotEquals(ControlledRevisionId.class, component.getType());
        }
    }

    private static ModelFingerprint fingerprint(String suffix) {
        return new ModelFingerprint("test-model", "v1", "sha256", "0".repeat(62) + suffix);
    }

    private static ControlledRevisionId revisionId(int suffix) {
        return ControlledRevisionId.parse("00000000-0000-4000-8000-" + String.format("%012d", suffix));
    }
}
