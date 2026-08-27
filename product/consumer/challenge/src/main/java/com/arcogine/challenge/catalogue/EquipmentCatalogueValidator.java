package com.arcogine.challenge.catalogue;

import com.arcogine.challenge.ChallengeDefinition;
import com.arcogine.challenge.EquipmentCatalogueItemId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Deterministic validation of whether an {@link EquipmentCatalogue}'s content is internally valid,
 * and whether a {@link ChallengeDefinition}'s catalogue references resolve.
 *
 * <p>This is a distinct validation domain from {@code
 * com.arcogine.challenge.validation.ChallengeDefinitionValidator} (which validates a challenge's
 * own content in isolation) and from Arcogine's {@code FactoryModelValidator} (which validates
 * canonical production executability). It does not call, extend, or reuse either.
 *
 * <p>Validation never mutates its inputs, never consults wall-clock time, random state, or any
 * runtime/session lookup, and iterates declared collections in their existing order, so repeated
 * validation of identical inputs always produces an equal result with equal issue ordering.
 */
public final class EquipmentCatalogueValidator {

    private EquipmentCatalogueValidator() {}

    public static EquipmentCatalogueValidationResult validate(EquipmentCatalogue catalogue) {
        if (catalogue == null) {
            throw new NullPointerException("catalogue");
        }
        List<EquipmentCatalogueIssue> issues = new ArrayList<>();

        Set<EquipmentCatalogueItemId> seenItemIds = new HashSet<>();
        List<EquipmentOffer> offers = catalogue.offers();
        for (int i = 0; i < offers.size(); i++) {
            EquipmentOffer offer = offers.get(i);
            String path = "offers[" + i + "]";

            if (isBlank(offer.itemId().value())) {
                issues.add(new EquipmentCatalogueIssue(
                        "offers.itemId.blank", path + ".itemId", "must be present and non-blank"));
            } else if (!seenItemIds.add(offer.itemId())) {
                issues.add(new EquipmentCatalogueIssue(
                        "offers.itemId.duplicate",
                        path + ".itemId",
                        "duplicate catalogue item id: " + offer.itemId().value()));
            }

            if (offer.purchaseCostCredits() < 0) {
                issues.add(new EquipmentCatalogueIssue(
                        "offers.purchaseCostCredits.negative",
                        path + ".purchaseCostCredits",
                        "must be >= 0"));
            }

            if (offer.quantityLimit().isPresent() && offer.quantityLimit().getAsInt() <= 0) {
                issues.add(new EquipmentCatalogueIssue(
                        "offers.quantityLimit.not-positive",
                        path + ".quantityLimit",
                        "must be > 0 when present"));
            }
        }

        return new EquipmentCatalogueValidationResult(issues);
    }

    /**
     * Validates that every {@link ChallengeDefinition#availableEquipment()} identity resolves to
     * exactly one offer in {@code catalogue}.
     *
     * <p>This counts matching offers itself rather than delegating to {@link
     * EquipmentCatalogue#findByItemId}, which deliberately returns only the first match -- an
     * identity with zero or more than one matching offer is reported here even when the
     * catalogue itself has not been separately validated for internal duplicates via {@link
     * #validate(EquipmentCatalogue)}. It does not decide other catalogue-internal validity
     * (blank ids, negative costs, etc.) -- use {@link #validate(EquipmentCatalogue)} for that --
     * nor does it reject draft occurrences for using equipment outside the challenge's allowed
     * set; that is candidate admissibility, a later Challenge Readiness slice.
     */
    public static EquipmentCatalogueValidationResult validateChallengeResolution(
            ChallengeDefinition challenge, EquipmentCatalogue catalogue) {
        if (challenge == null) {
            throw new NullPointerException("challenge");
        }
        if (catalogue == null) {
            throw new NullPointerException("catalogue");
        }

        List<EquipmentCatalogueIssue> issues = new ArrayList<>();
        List<EquipmentOffer> offers = catalogue.offers();
        List<EquipmentCatalogueItemId> availableEquipment = challenge.availableEquipment();
        for (int i = 0; i < availableEquipment.size(); i++) {
            EquipmentCatalogueItemId itemId = availableEquipment.get(i);
            String path = "availableEquipment[" + i + "]";

            long matches = offers.stream().filter(offer -> offer.itemId().equals(itemId)).count();
            if (matches == 0) {
                issues.add(new EquipmentCatalogueIssue(
                        "challenge.availableEquipment.unresolved",
                        path,
                        "no catalogue offer for item id: " + itemId.value()));
            } else if (matches > 1) {
                issues.add(new EquipmentCatalogueIssue(
                        "challenge.availableEquipment.ambiguous",
                        path,
                        "multiple catalogue offers for item id: " + itemId.value()));
            }
        }

        return new EquipmentCatalogueValidationResult(issues);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
