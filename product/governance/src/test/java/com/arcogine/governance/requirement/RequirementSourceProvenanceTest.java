package com.arcogine.governance.requirement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Proves invariants 5 and 6: exact external provenance versus first-class native requirements. */
class RequirementSourceProvenanceTest {

    @Test
    void arcogineNativeRequirementDoesNotRequireExternalSourceMetadata() {
        Requirement requirement =
                new Requirement(
                        new RequirementId("arc.architecture.no-engine-dependency-in-governance"),
                        new RequirementVersion(1),
                        "Governance stays domain-neutral",
                        "The :governance module must not depend on :factory, Spring, or the frontend.",
                        ArcogineNativeRequirementSource.unspecified(),
                        RequirementScope.empty());

        assertTrue(requirement.isArcogineNative());
        assertTrue(requirement.source() instanceof ArcogineNativeRequirementSource);
    }

    @Test
    void externalRequirementRetainsExactAuthorityDesignationEditionAndLocator() {
        ExternalRequirementSource source =
                ExternalRequirementSource.of("IEC", "IEC 62264-1", "2013", "clause 5.3.2");

        assertEquals("IEC", source.authority());
        assertEquals("IEC 62264-1", source.designation());
        assertEquals("2013", source.edition());
        assertEquals("clause 5.3.2", source.locator());
        assertTrue(source.adoptionProfileOptional().isEmpty());
    }

    @Test
    void differentExternalEditionsRemainDistinguishable() {
        ExternalRequirementSource edition2013 =
                ExternalRequirementSource.of("IEC", "IEC 62264-1", "2013", "clause 5.3.2");
        ExternalRequirementSource edition2003 =
                ExternalRequirementSource.of("IEC", "IEC 62264-1", "2003", "clause 5.3.2");

        assertNotEquals(edition2013, edition2003);
    }

    @Test
    void differentAdoptionsOrProfilesRemainDistinguishableWhenSpecified() {
        ExternalRequirementSource iecPublication =
                new ExternalRequirementSource("IEC", "IEC 62264-1", "2013", "clause 5.3.2", "");
        ExternalRequirementSource ansiIsaAdoption =
                new ExternalRequirementSource(
                        "ANSI/ISA", "ISA-95.00.01", "2010", "clause 5.3.2", "ANSI/ISA-95.00.01-2010");

        assertNotEquals(iecPublication, ansiIsaAdoption);
        assertTrue(iecPublication.adoptionProfileOptional().isEmpty());
        assertEquals("ANSI/ISA-95.00.01-2010", ansiIsaAdoption.adoptionProfileOptional().orElseThrow());
    }

    @Test
    void nullRationaleAndNullAdoptionProfileDefaultToBlank() {
        assertEquals("", new ArcogineNativeRequirementSource(null).rationale());
        ExternalRequirementSource source = new ExternalRequirementSource("IEC", "IEC 62264-1", "2013", "clause", null);
        assertEquals("", source.adoptionProfile());
        assertTrue(source.adoptionProfileOptional().isEmpty());
    }

    @Test
    void externalSourceRejectsBlankRequiredFields() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExternalRequirementSource("", "IEC 62264-1", "2013", "clause", ""));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExternalRequirementSource("IEC", " ", "2013", "clause", ""));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExternalRequirementSource("IEC", "IEC 62264-1", "", "clause", ""));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExternalRequirementSource("IEC", "IEC 62264-1", "2013", "", ""));
    }

    @Test
    void sameExternalClauseCanSupportDifferentArcogineRequirementVersions() {
        ExternalRequirementSource clause = ExternalRequirementSource.of("IEC", "IEC 62264-1", "2013", "clause 5.3.2");

        Requirement versionOne =
                new Requirement(
                        new RequirementId("arc.isa95.equipment-hierarchy"),
                        new RequirementVersion(1),
                        "Equipment hierarchy modeled",
                        "Initial Arcogine interpretation of the equipment hierarchy clause.",
                        clause,
                        RequirementScope.empty());
        Requirement versionTwo =
                new Requirement(
                        new RequirementId("arc.isa95.equipment-hierarchy"),
                        new RequirementVersion(2),
                        "Equipment hierarchy modeled, revised scope",
                        "Broadened Arcogine interpretation of the same clause after scope review.",
                        clause,
                        RequirementScope.empty());

        assertEquals(clause, versionOne.source());
        assertEquals(clause, versionTwo.source());
        assertNotEquals(versionOne, versionTwo);
        assertEquals(versionOne.id(), versionTwo.id());
    }
}
