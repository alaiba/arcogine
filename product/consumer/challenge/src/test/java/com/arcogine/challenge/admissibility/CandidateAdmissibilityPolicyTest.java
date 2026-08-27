package com.arcogine.challenge.admissibility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.challenge.ChallengeDefinition;
import com.arcogine.challenge.ChallengeFixtures;
import com.arcogine.challenge.EquipmentCatalogueItemId;
import com.arcogine.challenge.catalogue.EquipmentCatalogue;
import com.arcogine.challenge.catalogue.EquipmentCatalogueFixtures;
import com.arcogine.challenge.catalogue.EquipmentCatalogueIdentity;
import com.arcogine.challenge.catalogue.EquipmentOffer;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CandidateAdmissibilityPolicyTest {

    private static final EquipmentCatalogueItemId CUTTER = item("equipment.cutter");
    private static final EquipmentCatalogueItemId ASSEMBLY = item("equipment.assembly-station");
    private static final EquipmentCatalogueItemId INSPECTOR = item("equipment.inspector");

    @Test
    void validCandidateIsAdmittedAndRepeatedEvaluationIsEqual() {
        CandidateDraftSnapshot snapshot = snapshot(
                placed("cutter-1", CUTTER, 0, 0), placed("assembly-1", ASSEMBLY, 11, 9));

        CandidateAdmissibilityResult first = assess(ChallengeFixtures.referenceChallenge(),
                EquipmentCatalogueFixtures.referenceCatalogue(), snapshot);
        CandidateAdmissibilityResult second = assess(ChallengeFixtures.referenceChallenge(),
                EquipmentCatalogueFixtures.referenceCatalogue(), snapshot);

        assertTrue(first.admitted());
        assertEquals(first, second);
    }

    @Test
    void reorderingOccurrencesDoesNotChangeResult() {
        CandidateDraftSnapshot first = snapshot(
                placed("cutter-1", CUTTER, 0, 0), placed("inspector-1", INSPECTOR, 1, 0));
        CandidateDraftSnapshot reordered = snapshot(
                placed("inspector-1", INSPECTOR, 1, 0), placed("cutter-1", CUTTER, 0, 0));

        assertEquals(assess(ChallengeFixtures.referenceChallenge(),
                        EquipmentCatalogueFixtures.referenceCatalogue(), first),
                assess(ChallengeFixtures.referenceChallenge(),
                        EquipmentCatalogueFixtures.referenceCatalogue(), reordered));
    }

    @Test
    void snapshotCopiesItsCollection() {
        List<PlacedEquipment> source = new ArrayList<>(List.of(placed("cutter-1", CUTTER, 0, 0)));
        CandidateDraftSnapshot snapshot = new CandidateDraftSnapshot(source);
        source.clear();

        assertEquals(1, snapshot.placedEquipment().size());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.placedEquipment().clear());
    }

    @Test
    void unknownAndUnavailableItemsProduceDeterministicReasons() {
        EquipmentCatalogueItemId unknown = item("equipment.unknown");
        EquipmentCatalogueItemId disallowed = item("equipment.disallowed");
        EquipmentCatalogue catalogue = catalogue(List.of(
                EquipmentOffer.of(CUTTER, 5_000L), EquipmentOffer.of(disallowed, 1_000L)));
        CandidateDraftSnapshot snapshot = snapshot(
                placed("unknown-1", unknown, 0, 0), placed("disallowed-1", disallowed, 1, 0));

        CandidateAdmissibilityResult result = assess(challengeFor(catalogue), catalogue, snapshot);

        assertFalse(result.admitted());
        assertEquals(List.of("candidate.occurrence.unknown-catalogue-item",
                        "candidate.occurrence.unavailable-equipment",
                        "candidate.occurrence.unavailable-equipment"),
                result.issues().stream().map(CandidateAdmissibilityIssue::code).toList());
    }

    @Test
    void quantityLimitAllowsExactCountAndRejectsAdditionalOccurrence() {
        EquipmentCatalogue catalogue = limitedCatalogue(2);
        CandidateDraftSnapshot allowed = snapshot(
                placed("cutter-1", CUTTER, 0, 0), placed("cutter-2", CUTTER, 1, 0));
        CandidateDraftSnapshot tooMany = snapshot(
                placed("cutter-1", CUTTER, 0, 0), placed("cutter-2", CUTTER, 1, 0),
                placed("cutter-3", CUTTER, 2, 0));

        assertTrue(assess(challengeFor(catalogue), catalogue, allowed).admitted());
        assertEquals("candidate.quantity-limit.exceeded",
                assess(challengeFor(catalogue), catalogue, tooMany).issues().get(0).code());
    }

    @Test
    void budgetAtLimitIsAllowedAndBudgetOverLimitIsRejected() {
        EquipmentCatalogue exactCatalogue = catalogue(List.of(
                EquipmentOffer.of(CUTTER, 40_000L)));
        EquipmentCatalogue overCatalogue = catalogue(List.of(
                EquipmentOffer.of(CUTTER, 40_001L)));
        CandidateDraftSnapshot snapshot = snapshot(placed("cutter-1", CUTTER, 0, 0));

        assertTrue(assess(challengeFor(exactCatalogue), exactCatalogue, snapshot).admitted());
        assertEquals("candidate.budget.exceeded",
                assess(challengeFor(overCatalogue), overCatalogue, snapshot).issues().get(0).code());
    }

    @Test
    void changedPriceWithSameCatalogueIdentityIsRejected() {
        EquipmentCatalogue changedCatalogue = catalogue(List.of(
                EquipmentOffer.of(CUTTER, 5_001L),
                EquipmentOffer.of(ASSEMBLY, EquipmentCatalogueFixtures.ASSEMBLY_STATION_COST_CREDITS),
                EquipmentOffer.of(INSPECTOR, EquipmentCatalogueFixtures.INSPECTOR_COST_CREDITS)));

        CandidateAdmissibilityResult result = assess(ChallengeFixtures.referenceChallenge(),
                changedCatalogue, snapshot(placed("cutter-1", CUTTER, 0, 0)));

        assertEquals("candidate.catalogue.semantic-fingerprint-mismatch",
                result.issues().get(0).code());
    }

    @Test
    void legacyUnboundChallengeRejectsAdmissibility() {
        ChallengeDefinition reference = ChallengeFixtures.referenceChallenge();
        ChallengeDefinition unbound = new ChallengeDefinition(reference.identity(), reference.floor(),
                reference.startingBudget(), reference.workload(), reference.availableEquipment(),
                reference.deadline(), reference.evaluationPolicy(), reference.catalogueIdentity());

        CandidateAdmissibilityResult result = assess(unbound,
                EquipmentCatalogueFixtures.referenceCatalogue(),
                snapshot(placed("cutter-1", CUTTER, 0, 0)));

        assertEquals("candidate.catalogue.semantic-fingerprint-unbound",
                result.issues().get(0).code());
    }

    @Test
    void economicsFailureIsSurfacedExplicitly() {
        EquipmentCatalogue invalidCatalogue = catalogue(List.of(
                EquipmentOffer.of(CUTTER, -1L)));

        CandidateAdmissibilityResult result = assess(challengeFor(invalidCatalogue),
                invalidCatalogue, snapshot(placed("cutter-1", CUTTER, 0, 0)));

        assertEquals("candidate.economics.catalogue.invalid", result.issues().get(0).code());
    }

    @Test
    void floorEdgesAreAllowedAndOutsideCellsAreRejected() {
        ChallengeDefinition challenge = ChallengeFixtures.referenceChallenge();
        EquipmentCatalogue catalogue = EquipmentCatalogueFixtures.referenceCatalogue();
        assertTrue(assess(challenge, catalogue, snapshot(
                placed("edge", CUTTER, 11, 9))).admitted());
        assertEquals("candidate.placement.out-of-bounds", assess(challenge, catalogue, snapshot(
                placed("negative", CUTTER, -1, 0))).issues().get(0).code());
        assertEquals("candidate.placement.out-of-bounds", assess(challenge, catalogue, snapshot(
                placed("width", CUTTER, 12, 0))).issues().get(0).code());
        assertEquals("candidate.placement.out-of-bounds", assess(challenge, catalogue, snapshot(
                placed("height", CUTTER, 0, 10))).issues().get(0).code());
    }

    @Test
    void overlappingCellsAndDuplicateOccurrenceIdsAreRejected() {
        CandidateDraftSnapshot snapshot = snapshot(
                placed("same", CUTTER, 0, 0), placed("same", ASSEMBLY, 0, 0));

        CandidateAdmissibilityResult result = assess(ChallengeFixtures.referenceChallenge(),
                EquipmentCatalogueFixtures.referenceCatalogue(), snapshot);

        assertEquals(List.of("candidate.occurrence.duplicate-identity", "candidate.placement.overlap"),
                result.issues().stream().map(CandidateAdmissibilityIssue::code).toList());
    }

    @Test
    void catalogueIdentityMustMatchChallengeVersion() {
        EquipmentCatalogue catalogue = new EquipmentCatalogue(
                new EquipmentCatalogueIdentity("catalogue.challenge.factory-basics", "2"),
                List.of(EquipmentOffer.of(CUTTER, 5_000L)));

        CandidateAdmissibilityResult result = assess(ChallengeFixtures.referenceChallenge(), catalogue,
                snapshot(placed("cutter-1", CUTTER, 0, 0)));

        assertEquals("candidate.catalogue.identity-mismatch", result.issues().get(0).code());
    }

    @Test
    void differentOccurrenceIdsMayUseTheSameCatalogueItem() {
        CandidateAdmissibilityResult result = assess(ChallengeFixtures.referenceChallenge(),
                EquipmentCatalogueFixtures.referenceCatalogue(), snapshot(
                        placed("cutter-1", CUTTER, 0, 0), placed("cutter-2", CUTTER, 1, 0)));

        assertTrue(result.admitted());
    }

    @Test
    void doesNotRequireFactoryRuntime() {
        assertThrows(ClassNotFoundException.class,
                () -> Class.forName("com.arcogine.factory.model.validation.FactoryModelValidator"));
    }

    private static CandidateAdmissibilityResult assess(ChallengeDefinition challenge,
            EquipmentCatalogue catalogue, CandidateDraftSnapshot snapshot) {
        return CandidateAdmissibilityPolicy.assess(challenge, catalogue, snapshot);
    }

    private static EquipmentCatalogue limitedCatalogue(int cutterLimit) {
        return catalogue(List.of(
                EquipmentOffer.of(CUTTER, EquipmentCatalogueFixtures.CUTTER_COST_CREDITS, cutterLimit),
                EquipmentOffer.of(ASSEMBLY, EquipmentCatalogueFixtures.ASSEMBLY_STATION_COST_CREDITS),
                EquipmentOffer.of(INSPECTOR, EquipmentCatalogueFixtures.INSPECTOR_COST_CREDITS)));
    }

        private static EquipmentCatalogue catalogue(List<EquipmentOffer> offers) {
                return new EquipmentCatalogue(
                                new EquipmentCatalogueIdentity("catalogue.challenge.factory-basics", "1"), offers);
        }

    private static CandidateDraftSnapshot snapshot(PlacedEquipment... equipment) {
        return new CandidateDraftSnapshot(List.of(equipment));
    }

        private static ChallengeDefinition challengeFor(EquipmentCatalogue catalogue) {
                ChallengeDefinition reference = ChallengeFixtures.referenceChallenge();
                return new ChallengeDefinition(reference.identity(), reference.floor(), reference.startingBudget(),
                                reference.workload(), reference.availableEquipment(), reference.deadline(),
                                reference.evaluationPolicy(), catalogue.identity(), catalogue.semanticFingerprint());
        }

    private static PlacedEquipment placed(String occurrenceId, EquipmentCatalogueItemId itemId,
            int x, int y) {
        return new PlacedEquipment(new EquipmentOccurrenceId(occurrenceId), itemId,
                new GridPlacement(x, y));
    }

    private static EquipmentCatalogueItemId item(String value) {
        return new EquipmentCatalogueItemId(value);
    }
}
