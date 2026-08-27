package com.arcogine.challenge.catalogue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.challenge.ChallengeDefinition;
import com.arcogine.challenge.ChallengeFixtures;
import com.arcogine.challenge.EquipmentCatalogueItemId;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class EquipmentCatalogueValidatorTest {

    @Test
    void referenceCatalogueOffersPassValidation() {
        EquipmentCatalogueValidationResult result =
                EquipmentCatalogueValidator.validate(EquipmentCatalogueFixtures.referenceCatalogue());

        assertTrue(result.isValid());
        assertEquals(List.of(), result.issues());
    }

    @Test
    void duplicateItemIdsAreRejectedDeterministically() {
        EquipmentCatalogue catalogue = new EquipmentCatalogue(List.of(
                EquipmentOffer.of(new EquipmentCatalogueItemId("equipment.cutter"), 1L),
                EquipmentOffer.of(new EquipmentCatalogueItemId("equipment.cutter"), 2L)));

        EquipmentCatalogueValidationResult result = EquipmentCatalogueValidator.validate(catalogue);

        assertTrue(containsIssue(result, "offers.itemId.duplicate", "offers[1].itemId"));
    }

    @Test
    void blankItemIdIsRejected() {
        EquipmentCatalogue catalogue =
                new EquipmentCatalogue(List.of(EquipmentOffer.of(new EquipmentCatalogueItemId(" "), 1L)));

        EquipmentCatalogueValidationResult result = EquipmentCatalogueValidator.validate(catalogue);

        assertTrue(containsIssue(result, "offers.itemId.blank", "offers[0].itemId"));
    }

    @Test
    void negativePurchaseCostIsRejected() {
        EquipmentCatalogue catalogue = new EquipmentCatalogue(
                List.of(EquipmentOffer.of(new EquipmentCatalogueItemId("equipment.cutter"), -1L)));

        EquipmentCatalogueValidationResult result = EquipmentCatalogueValidator.validate(catalogue);

        assertTrue(containsIssue(
                result, "offers.purchaseCostCredits.negative", "offers[0].purchaseCostCredits"));
    }

    @Test
    void nonPositiveQuantityLimitIsRejectedWhenPresent() {
        EquipmentCatalogue catalogue = new EquipmentCatalogue(List.of(
                EquipmentOffer.of(new EquipmentCatalogueItemId("equipment.cutter"), 1L, 0)));

        EquipmentCatalogueValidationResult result = EquipmentCatalogueValidator.validate(catalogue);

        assertTrue(containsIssue(
                result, "offers.quantityLimit.not-positive", "offers[0].quantityLimit"));
    }

    @Test
    void multipleIssuesHaveStableDeterministicOrdering() {
        EquipmentCatalogue catalogue = new EquipmentCatalogue(List.of(
                EquipmentOffer.of(new EquipmentCatalogueItemId(" "), -1L, 0),
                EquipmentOffer.of(new EquipmentCatalogueItemId("equipment.cutter"), 1L)));

        EquipmentCatalogueValidationResult result = EquipmentCatalogueValidator.validate(catalogue);

        List<String> codes =
                result.issues().stream().map(EquipmentCatalogueIssue::code).collect(Collectors.toList());

        assertEquals(
                List.of(
                        "offers.itemId.blank",
                        "offers.purchaseCostCredits.negative",
                        "offers.quantityLimit.not-positive"),
                codes);
    }

    @Test
    void repeatedValidationOfIdenticalCatalogueProducesEqualResults() {
        EquipmentCatalogue catalogue = EquipmentCatalogueFixtures.referenceCatalogue();

        assertEquals(
                EquipmentCatalogueValidator.validate(catalogue),
                EquipmentCatalogueValidator.validate(catalogue));
    }

    @Test
    void allReferenceChallengeAvailableEquipmentResolves() {
        ChallengeDefinition challenge = ChallengeFixtures.referenceChallenge();
        EquipmentCatalogue catalogue = EquipmentCatalogueFixtures.referenceCatalogue();

        EquipmentCatalogueValidationResult result =
                EquipmentCatalogueValidator.validateChallengeResolution(challenge, catalogue);

        assertTrue(result.isValid());
    }

    @Test
    void missingCatalogueItemProducesDeterministicStructuredIssue() {
        ChallengeDefinition challenge = ChallengeFixtures.referenceChallenge();
        EquipmentCatalogue catalogue = new EquipmentCatalogue(
                List.of(EquipmentOffer.of(new EquipmentCatalogueItemId("equipment.cutter"), 1L)));

        EquipmentCatalogueValidationResult result =
                EquipmentCatalogueValidator.validateChallengeResolution(challenge, catalogue);

        assertTrue(containsIssue(
                result,
                "challenge.availableEquipment.unresolved",
                "availableEquipment[1]"));
        assertTrue(containsIssue(
                result,
                "challenge.availableEquipment.unresolved",
                "availableEquipment[2]"));
    }

    private static boolean containsIssue(
            EquipmentCatalogueValidationResult result, String code, String path) {
        return result.issues().stream()
                .anyMatch(issue -> issue.code().equals(code) && issue.path().equals(path));
    }
}
