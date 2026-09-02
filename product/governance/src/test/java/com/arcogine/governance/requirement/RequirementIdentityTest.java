package com.arcogine.governance.requirement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    void equalityIsIdAndVersionOnlyNotWordingOrSource() {
        Requirement requirement =
                new Requirement(
                        new RequirementId("arc.test.example"),
                        new RequirementVersion(1),
                        "title",
                        null,
                        ArcogineNativeRequirementSource.unspecified(),
                        RequirementScope.empty());
        Requirement sameIdAndVersionDifferentWording =
                new Requirement(
                        requirement.id(),
                        requirement.version(),
                        "a different title",
                        "a different description",
                        ArcogineNativeRequirementSource.of("different rationale"),
                        RequirementScope.empty());
        Requirement differentVersion =
                new Requirement(
                        requirement.id(),
                        new RequirementVersion(2),
                        "title",
                        "",
                        ArcogineNativeRequirementSource.unspecified(),
                        RequirementScope.empty());

        assertEquals(requirement, sameIdAndVersionDifferentWording);
        assertEquals(requirement.hashCode(), sameIdAndVersionDifferentWording.hashCode());
        assertNotEquals(requirement, differentVersion);
        assertNotEquals(requirement, "not a requirement");
        assertEquals("", requirement.description());
        assertTrue(requirement.isArcogineNative());
        assertFalse(requirement.isExternallySourced());
    }

    @Test
    void rejectsBlankTitle() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new Requirement(
                                new RequirementId("arc.test.blank-title"),
                                new RequirementVersion(1),
                                " ",
                                "",
                                ArcogineNativeRequirementSource.unspecified(),
                                RequirementScope.empty()));
    }

    private static ModelFingerprint fingerprint(String suffix) {
        return new ModelFingerprint("test-model", "v1", "sha256", "0".repeat(62) + suffix);
    }

    private static ControlledRevisionId revisionId(int suffix) {
        return ControlledRevisionId.parse("00000000-0000-4000-8000-" + String.format("%012d", suffix));
    }
}
