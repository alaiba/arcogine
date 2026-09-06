package com.arcogine.governance.catalogue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.governance.change.ChangeProvenance;
import com.arcogine.governance.change.ChangeSet;
import com.arcogine.governance.change.ChangedEntityRef;
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
import org.junit.jupiter.api.Test;

/** Proves the catalogue makes the change-set/impact-scope capability's "registered requirements" seam real, deterministically. */
class RequirementCatalogueTest {

    private static final ChangedEntityRef PRESS_A = new ChangedEntityRef("factory.resource", "1", "Press A");
    private static final ChangedEntityRef WIDGET = new ChangedEntityRef("factory.product", "9", "Widget");

    @Test
    void impactAnalysisSelectsOnlyRequirementsWithIntersectingScope() {
        Requirement resourceRequirement =
                new Requirement(
                        new RequirementId("arc.factory.resource-capacity-positive"),
                        new RequirementVersion(1),
                        "Resources declare positive capacity",
                        "",
                        ArcogineNativeRequirementSource.unspecified(),
                        RequirementScope.of(PRESS_A));
        Requirement unrelatedRequirement =
                new Requirement(
                        new RequirementId("arc.factory.product-naming"),
                        new RequirementVersion(1),
                        "Products have stable names",
                        "",
                        ArcogineNativeRequirementSource.unspecified(),
                        RequirementScope.of(new ChangedEntityRef("factory.product", "42", "Other")));
        RequirementCatalogue catalogue = RequirementCatalogue.of(resourceRequirement, unrelatedRequirement);

        SemanticChange change = new SemanticChange(SemanticChangeKind.ENTITY_MODIFIED, PRESS_A, "capacity changed");
        ChangeSet changeSet =
                new ChangeSet(
                        revisionId(1),
                        fingerprint("aa"),
                        fingerprint("bb"),
                        revisionId(2),
                        List.of(change),
                        null,
                        ChangeProvenance.of("test", "catalogue selection"));

        List<Requirement> affected = catalogue.potentiallyAffectedBy(changeSet.impactScope());

        assertEquals(List.of(resourceRequirement), affected);
    }

    @Test
    void resolveFindsByExactIdentityAndVersion() {
        Requirement v1 =
                new Requirement(
                        new RequirementId("arc.factory.resource-capacity-positive"),
                        new RequirementVersion(1),
                        "title v1",
                        "",
                        ArcogineNativeRequirementSource.unspecified(),
                        RequirementScope.empty());
        Requirement v2 =
                new Requirement(
                        v1.id(),
                        new RequirementVersion(2),
                        "title v2",
                        "",
                        ArcogineNativeRequirementSource.unspecified(),
                        RequirementScope.empty());
        RequirementCatalogue catalogue = RequirementCatalogue.of(v1, v2);

        assertEquals(v1, catalogue.resolve(v1.id(), v1.version()).orElseThrow());
        assertEquals(v2, catalogue.resolve(v1.id(), v2.version()).orElseThrow());
        assertTrue(catalogue.resolve(v1.id(), new RequirementVersion(3)).isEmpty());
        assertEquals(List.of(v1, v2), catalogue.all());
        assertEquals(2, catalogue.size());
    }

    @Test
    void rejectsDuplicateRequirementRegistration() {
        Requirement requirement =
                new Requirement(
                        new RequirementId("arc.factory.duplicate"),
                        new RequirementVersion(1),
                        "title",
                        "",
                        ArcogineNativeRequirementSource.unspecified(),
                        RequirementScope.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> RequirementCatalogue.of(requirement, requirement));
    }

    private static ModelFingerprint fingerprint(String suffix) {
        return new ModelFingerprint("test-model", "v1", "sha256", "0".repeat(62) + suffix);
    }

    private static ControlledRevisionId revisionId(int suffix) {
        return ControlledRevisionId.parse("00000000-0000-4000-8000-" + String.format("%012d", suffix));
    }
}
