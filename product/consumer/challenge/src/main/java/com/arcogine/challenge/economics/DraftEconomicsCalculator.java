package com.arcogine.challenge.economics;

import com.arcogine.challenge.ChallengeDefinition;
import com.arcogine.challenge.EquipmentCatalogueItemId;
import com.arcogine.challenge.catalogue.EquipmentCatalogue;
import com.arcogine.challenge.catalogue.EquipmentOffer;
import java.util.List;
import java.util.Optional;

/**
 * A pure, deterministic calculator that derives {@link DraftEconomics} from an exact challenge
 * budget, catalogue, and set of draft equipment occurrences.
 *
 * <p>{@code calculate} resolves each occurrence through the game catalogue, sums committed
 * construction cost, and derives remaining budget from the challenge's starting budget. It
 * mutates none of its inputs, consults no wall-clock time, environment, random value, or global
 * registry, and never calls Finance or factory validation. For identical inputs it always
 * produces an equal result, regardless of occurrence order.
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

        long committedConstructionCostCredits = 0L;
        for (DraftEquipmentOccurrence occurrence : occurrences) {
            EquipmentCatalogueItemId itemId = occurrence.itemId();
            Optional<EquipmentOffer> offer = catalogue.findByItemId(itemId);
            if (offer.isEmpty()) {
                return DraftEconomicsResult.failure(
                        DraftEconomicsFailure.unknownCatalogueItem(itemId));
            }

            try {
                committedConstructionCostCredits = Math.addExact(
                        committedConstructionCostCredits, offer.get().purchaseCostCredits());
            } catch (ArithmeticException overflow) {
                return DraftEconomicsResult.failure(DraftEconomicsFailure.costOverflow());
            }
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
