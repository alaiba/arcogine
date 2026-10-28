package com.arcogine.challenge.catalogue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void unpairedSurrogateItemIdsAreRejectedAtTheItemPath() {
        for (String illFormed : List.of("\uD800", "\uDC00", "\uDC00\uD800", "a\uD800", "\uD800a")) {
            EquipmentCatalogue catalogue = new EquipmentCatalogue(
                    List.of(EquipmentOffer.of(new EquipmentCatalogueItemId(illFormed), 1L)));

            List<EquipmentCatalogueIssue> issues =
                    EquipmentCatalogueValidator.validate(catalogue).issues();

            assertEquals(
                    List.of(new EquipmentCatalogueIssue(
                            "offers.itemId.invalid-unicode",
                            "offers[0].itemId",
                            "must contain only valid Unicode scalar values")),
                    issues,
                    () -> "item id " + illFormed.chars().boxed().toList());
        }
    }

    @Test
    void wellFormedItemIdsProduceNoUnicodeIssue() {
        for (String wellFormed : List.of("equipment.cutter", "😀", "?", "équipment", "é")) {
            EquipmentCatalogue catalogue = new EquipmentCatalogue(
                    List.of(EquipmentOffer.of(new EquipmentCatalogueItemId(wellFormed), 1L)));

            assertEquals(List.of(), EquipmentCatalogueValidator.validate(catalogue).issues());
        }
    }

    @Test
    void illFormedItemIdIsReportedWithOtherIssuesInDeterministicOrder() {
        EquipmentCatalogue catalogue = new EquipmentCatalogue(List.of(
                EquipmentOffer.of(new EquipmentCatalogueItemId("\uD800"), -1L, 0),
                EquipmentOffer.of(new EquipmentCatalogueItemId(" "), 1L),
                EquipmentOffer.of(new EquipmentCatalogueItemId("\uD800"), 1L)));

        List<String> codeAndPath = EquipmentCatalogueValidator.validate(catalogue).issues().stream()
                .map(issue -> issue.code() + "@" + issue.path())
                .collect(Collectors.toList());

        assertEquals(
                List.of(
                        "offers.itemId.invalid-unicode@offers[0].itemId",
                        "offers.purchaseCostCredits.negative@offers[0].purchaseCostCredits",
                        "offers.quantityLimit.not-positive@offers[0].quantityLimit",
                        "offers.itemId.blank@offers[1].itemId",
                        "offers.itemId.invalid-unicode@offers[2].itemId",
                        "offers.itemId.duplicate@offers[2].itemId"),
                codeAndPath);
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

    @Test
    void duplicateMatchingOffersMakeResolutionAmbiguousRatherThanValid() {
        ChallengeDefinition challenge = ChallengeFixtures.referenceChallenge();
        EquipmentCatalogue catalogue = new EquipmentCatalogue(List.of(
                EquipmentOffer.of(new EquipmentCatalogueItemId("equipment.cutter"), 1L),
                EquipmentOffer.of(new EquipmentCatalogueItemId("equipment.cutter"), 2L),
                EquipmentOffer.of(new EquipmentCatalogueItemId("equipment.assembly-station"), 3L),
                EquipmentOffer.of(new EquipmentCatalogueItemId("equipment.inspector"), 4L)));

        EquipmentCatalogueValidationResult result =
                EquipmentCatalogueValidator.validateChallengeResolution(challenge, catalogue);

        assertTrue(containsIssue(
                result, "challenge.availableEquipment.ambiguous", "availableEquipment[0]"));
    }

    @Test
    void validateRejectsNullCatalogue() {
        assertThrows(NullPointerException.class, () -> EquipmentCatalogueValidator.validate(null));
    }

    @Test
    void validateChallengeResolutionRejectsNullChallenge() {
        assertThrows(
                NullPointerException.class,
                () -> EquipmentCatalogueValidator.validateChallengeResolution(
                        null, EquipmentCatalogueFixtures.referenceCatalogue()));
    }

    @Test
    void validateChallengeResolutionRejectsNullCatalogue() {
        assertThrows(
                NullPointerException.class,
                () -> EquipmentCatalogueValidator.validateChallengeResolution(
                        ChallengeFixtures.referenceChallenge(), null));
    }

    private static boolean containsIssue(
            EquipmentCatalogueValidationResult result, String code, String path) {
        return result.issues().stream()
                .anyMatch(issue -> issue.code().equals(code) && issue.path().equals(path));
    }
}
