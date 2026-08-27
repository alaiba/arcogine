package com.arcogine.challenge.economics;

import com.arcogine.challenge.ChallengeDefinition;
import com.arcogine.challenge.EquipmentCatalogueItemId;
import com.arcogine.challenge.catalogue.EquipmentCatalogue;
import com.arcogine.challenge.catalogue.EquipmentCatalogueValidationResult;
import com.arcogine.challenge.catalogue.EquipmentCatalogueValidator;
import com.arcogine.challenge.catalogue.EquipmentOffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * A pure, deterministic calculator that derives {@link DraftEconomics} from an exact challenge
 * budget, catalogue, and set of draft equipment occurrences.
 *
 * <p>{@code calculate} first validates the catalogue via {@link EquipmentCatalogueValidator#validate}
 * -- an internally invalid catalogue (negative purchase costs, duplicate item ids, etc.) fails
 * deterministically rather than silently producing an order-dependent or otherwise incorrect
 * result. Once the catalogue is known valid, it resolves every occurrence through it before
 * aggregating cost, sums committed construction cost, and derives remaining budget from the
 * challenge's starting budget. It mutates none of its inputs, consults no wall-clock time,
 * environment, random value, or global registry, and never calls Finance or factory validation.
 * For identical inputs -- including a failure outcome -- it always produces an equal result,
 * regardless of occurrence order: unresolved item ids are reported via the lexicographically
 * smallest offending identity rather than the first one encountered, and cost overflow is checked
 * only once all occurrences have resolved, so which failure is reported never depends on
 * occurrence order.
 *
 * <p>This calculator does not enforce that occurrences use only the challenge's {@code
 * availableEquipment}, nor does it enforce catalogue quantity limits against occurrence counts --
 * those checks belong to candidate admissibility, a later Challenge Readiness slice.
 */
public final class DraftEconomicsCalculator {

    private DraftEconomicsCalculator() {}

    public static DraftEconomicsResult calculate(
            ChallengeDefinition challenge,
            EquipmentCatalogue catalogue,
            List<DraftEquipmentOccurrence> occurrences) {
        if (challenge == null) {
            throw new NullPointerException("challenge");
        }
        if (catalogue == null) {
            throw new NullPointerException("catalogue");
        }
        if (occurrences == null) {
            throw new NullPointerException("occurrences");
        }

        EquipmentCatalogueValidationResult catalogueValidation = EquipmentCatalogueValidator.validate(catalogue);
        if (!catalogueValidation.isValid()) {
            return DraftEconomicsResult.failure(DraftEconomicsFailure.invalidCatalogue(catalogueValidation));
        }

        List<EquipmentCatalogueItemId> unresolved = new ArrayList<>();
        List<EquipmentOffer> resolvedOffers = new ArrayList<>();
        for (DraftEquipmentOccurrence occurrence : occurrences) {
            EquipmentCatalogueItemId itemId = occurrence.itemId();
            Optional<EquipmentOffer> offer = catalogue.findByItemId(itemId);
            if (offer.isEmpty()) {
                unresolved.add(itemId);
            } else {
                resolvedOffers.add(offer.get());
            }
        }

        if (!unresolved.isEmpty()) {
            EquipmentCatalogueItemId failingItemId = unresolved.stream()
                    .min(Comparator.comparing(EquipmentCatalogueItemId::value))
                    .orElseThrow();
            return DraftEconomicsResult.failure(
                    DraftEconomicsFailure.unknownCatalogueItem(failingItemId));
        }

        long committedConstructionCostCredits = 0L;
        try {
            for (EquipmentOffer offer : resolvedOffers) {
                committedConstructionCostCredits =
                        Math.addExact(committedConstructionCostCredits, offer.purchaseCostCredits());
            }
        } catch (ArithmeticException overflow) {
            return DraftEconomicsResult.failure(DraftEconomicsFailure.costOverflow());
        }

        long startingBudgetCredits = challenge.startingBudget();
        long remainingBudgetCredits;
        try {
            remainingBudgetCredits =
                    Math.subtractExact(startingBudgetCredits, committedConstructionCostCredits);
        } catch (ArithmeticException overflow) {
            return DraftEconomicsResult.failure(DraftEconomicsFailure.costOverflow());
        }

        return DraftEconomicsResult.success(new DraftEconomics(
                startingBudgetCredits, committedConstructionCostCredits, remainingBudgetCredits));
    }
}
