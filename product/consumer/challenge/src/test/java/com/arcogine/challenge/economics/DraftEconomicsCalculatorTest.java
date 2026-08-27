package com.arcogine.challenge.economics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.challenge.ChallengeDefinition;
import com.arcogine.challenge.ChallengeFixtures;
import com.arcogine.challenge.EquipmentCatalogueItemId;
import com.arcogine.challenge.catalogue.EquipmentCatalogue;
import com.arcogine.challenge.catalogue.EquipmentCatalogueFixtures;
import com.arcogine.challenge.catalogue.EquipmentOffer;
import java.util.List;
import org.junit.jupiter.api.Test;

class DraftEconomicsCalculatorTest {

    private static final EquipmentCatalogueItemId CUTTER =
            new EquipmentCatalogueItemId("equipment.cutter");
    private static final EquipmentCatalogueItemId ASSEMBLY_STATION =
            new EquipmentCatalogueItemId("equipment.assembly-station");
    private static final EquipmentCatalogueItemId INSPECTOR =
            new EquipmentCatalogueItemId("equipment.inspector");

    @Test
    void computesExactCommittedConstructionCost() {
        ChallengeDefinition challenge = ChallengeFixtures.referenceChallenge();
        EquipmentCatalogue catalogue = EquipmentCatalogueFixtures.referenceCatalogue();
        List<DraftEquipmentOccurrence> occurrences = List.of(
                new DraftEquipmentOccurrence(CUTTER),
                new DraftEquipmentOccurrence(CUTTER),
                new DraftEquipmentOccurrence(ASSEMBLY_STATION));

        DraftEconomicsResult result = DraftEconomicsCalculator.calculate(challenge, catalogue, occurrences);

        assertTrue(result.isSuccess());
        long expectedCost = 2 * EquipmentCatalogueFixtures.CUTTER_COST_CREDITS
                + EquipmentCatalogueFixtures.ASSEMBLY_STATION_COST_CREDITS;
        assertEquals(expectedCost, result.economics().committedConstructionCostCredits());
    }

    @Test
    void remainingBudgetEqualsStartingBudgetMinusCommittedCost() {
        ChallengeDefinition challenge = ChallengeFixtures.referenceChallenge();
        EquipmentCatalogue catalogue = EquipmentCatalogueFixtures.referenceCatalogue();
        List<DraftEquipmentOccurrence> occurrences =
                List.of(new DraftEquipmentOccurrence(CUTTER), new DraftEquipmentOccurrence(INSPECTOR));

        DraftEconomics economics =
                DraftEconomicsCalculator.calculate(challenge, catalogue, occurrences).economics();

        assertEquals(challenge.startingBudget(), economics.startingBudgetCredits());
        assertEquals(
                challenge.startingBudget() - economics.committedConstructionCostCredits(),
                economics.remainingBudgetCredits());
    }

    @Test
    void reorderingEquivalentOccurrencesDoesNotChangeTheResult() {
        ChallengeDefinition challenge = ChallengeFixtures.referenceChallenge();
        EquipmentCatalogue catalogue = EquipmentCatalogueFixtures.referenceCatalogue();
        List<DraftEquipmentOccurrence> ordered = List.of(
                new DraftEquipmentOccurrence(CUTTER),
                new DraftEquipmentOccurrence(ASSEMBLY_STATION),
                new DraftEquipmentOccurrence(INSPECTOR));
        List<DraftEquipmentOccurrence> reordered = List.of(
                new DraftEquipmentOccurrence(INSPECTOR),
                new DraftEquipmentOccurrence(CUTTER),
                new DraftEquipmentOccurrence(ASSEMBLY_STATION));

        DraftEconomicsResult first = DraftEconomicsCalculator.calculate(challenge, catalogue, ordered);
        DraftEconomicsResult second = DraftEconomicsCalculator.calculate(challenge, catalogue, reordered);

        assertEquals(first.economics(), second.economics());
    }

    @Test
    void removingAnOccurrenceChangesCostDeterministically() {
        ChallengeDefinition challenge = ChallengeFixtures.referenceChallenge();
        EquipmentCatalogue catalogue = EquipmentCatalogueFixtures.referenceCatalogue();
        List<DraftEquipmentOccurrence> withCutter =
                List.of(new DraftEquipmentOccurrence(CUTTER), new DraftEquipmentOccurrence(INSPECTOR));
        List<DraftEquipmentOccurrence> withoutCutter = List.of(new DraftEquipmentOccurrence(INSPECTOR));

        DraftEconomics withCutterEconomics =
                DraftEconomicsCalculator.calculate(challenge, catalogue, withCutter).economics();
        DraftEconomics withoutCutterEconomics =
                DraftEconomicsCalculator.calculate(challenge, catalogue, withoutCutter).economics();

        assertEquals(
                EquipmentCatalogueFixtures.CUTTER_COST_CREDITS,
                withCutterEconomics.committedConstructionCostCredits()
                        - withoutCutterEconomics.committedConstructionCostCredits());
    }

    @Test
    void unknownDraftCatalogueIdentityFailsExplicitly() {
        ChallengeDefinition challenge = ChallengeFixtures.referenceChallenge();
        EquipmentCatalogue catalogue = EquipmentCatalogueFixtures.referenceCatalogue();
        List<DraftEquipmentOccurrence> occurrences =
                List.of(new DraftEquipmentOccurrence(new EquipmentCatalogueItemId("equipment.unknown")));

        DraftEconomicsResult result = DraftEconomicsCalculator.calculate(challenge, catalogue, occurrences);

        assertFalse(result.isSuccess());
        assertEquals("draft.occurrence.unknown-catalogue-item", result.failure().code());
    }

    @Test
    void arithmeticOverflowFailsExplicitlyRatherThanWrapping() {
        ChallengeDefinition challenge = ChallengeFixtures.referenceChallenge();
        EquipmentCatalogue catalogue = new EquipmentCatalogue(
                List.of(EquipmentOffer.of(CUTTER, Long.MAX_VALUE)));
        List<DraftEquipmentOccurrence> occurrences =
                List.of(new DraftEquipmentOccurrence(CUTTER), new DraftEquipmentOccurrence(CUTTER));

        DraftEconomicsResult result = DraftEconomicsCalculator.calculate(challenge, catalogue, occurrences);

        assertFalse(result.isSuccess());
        assertEquals("draft.cost.overflow", result.failure().code());
    }

    @Test
    void repeatedCalculationFromIdenticalInputsReturnsEqualResults() {
        ChallengeDefinition challenge = ChallengeFixtures.referenceChallenge();
        EquipmentCatalogue catalogue = EquipmentCatalogueFixtures.referenceCatalogue();
        List<DraftEquipmentOccurrence> occurrences = List.of(new DraftEquipmentOccurrence(CUTTER));

        DraftEconomicsResult first = DraftEconomicsCalculator.calculate(challenge, catalogue, occurrences);
        DraftEconomicsResult second = DraftEconomicsCalculator.calculate(challenge, catalogue, occurrences);

        assertEquals(first, second);
    }

    @Test
    void calculationDoesNotMutateChallengeCatalogueOrDraft() {
        ChallengeDefinition challenge = ChallengeFixtures.referenceChallenge();
        EquipmentCatalogue catalogue = EquipmentCatalogueFixtures.referenceCatalogue();
        List<DraftEquipmentOccurrence> occurrences = List.of(new DraftEquipmentOccurrence(CUTTER));
        long budgetBefore = challenge.startingBudget();
        List<EquipmentOffer> offersBefore = List.copyOf(catalogue.offers());
        List<DraftEquipmentOccurrence> occurrencesBefore = List.copyOf(occurrences);

        DraftEconomicsCalculator.calculate(challenge, catalogue, occurrences);

        assertEquals(budgetBefore, challenge.startingBudget());
        assertEquals(offersBefore, catalogue.offers());
        assertEquals(occurrencesBefore, occurrences);
    }

    @Test
    void runsWithoutAnArcogineRuntimeOrActiveSimulationSession() {
        ChallengeDefinition challenge = ChallengeFixtures.referenceChallenge();
        EquipmentCatalogue catalogue = EquipmentCatalogueFixtures.referenceCatalogue();

        DraftEconomicsResult result =
                DraftEconomicsCalculator.calculate(challenge, catalogue, List.of());

        assertTrue(result.isSuccess());
        assertEquals(0L, result.economics().committedConstructionCostCredits());
    }
}
