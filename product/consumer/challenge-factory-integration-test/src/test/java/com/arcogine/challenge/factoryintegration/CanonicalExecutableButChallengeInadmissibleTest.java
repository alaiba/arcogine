package com.arcogine.challenge.factoryintegration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.challenge.ChallengeDefinition;
import com.arcogine.challenge.ChallengeIdentity;
import com.arcogine.challenge.ChallengeWorkload;
import com.arcogine.challenge.EquipmentCatalogueItemId;
import com.arcogine.challenge.EvaluationPolicyIdentity;
import com.arcogine.challenge.FactoryFloorConstraint;
import com.arcogine.challenge.admissibility.CandidateAdmissibilityPolicy;
import com.arcogine.challenge.admissibility.CandidateAdmissibilityResult;
import com.arcogine.challenge.admissibility.CandidateDraftSnapshot;
import com.arcogine.challenge.admissibility.EquipmentOccurrenceId;
import com.arcogine.challenge.admissibility.GridPlacement;
import com.arcogine.challenge.admissibility.PlacedEquipment;
import com.arcogine.challenge.catalogue.EquipmentCatalogue;
import com.arcogine.challenge.catalogue.EquipmentCatalogueIdentity;
import com.arcogine.challenge.catalogue.EquipmentOffer;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.model.ResourceDefinition;
import com.arcogine.factory.model.validation.FactoryModelValidator;
import com.arcogine.factory.model.validation.ModelValidationResult;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * C5 acceptance criterion 6: canonical Factory-executability and challenge admissibility are
 * independent validation axes. This is the only module in the repository with a test dependency
 * on both {@code :factory} and {@code :challenge}; neither of those production modules depends on
 * the other, and this module carries no production sources of its own.
 *
 * <p>The proof: a design can pass {@link FactoryModelValidator#validate} — the structural,
 * game-rule-agnostic notion of "this factory graph is well-formed and could run" — while the
 * corresponding candidate draft in a challenge is rejected by {@link CandidateAdmissibilityPolicy}
 * for a purely game-rule reason (here: exceeding the challenge's starting credit budget) that has
 * nothing to do with structural validity. Nine cutters is a perfectly coherent, executable factory
 * shape; it is inadmissible only because this challenge's economy caps spend at 40,000 credits.
 */
class CanonicalExecutableButChallengeInadmissibleTest {

    @Test
    void factoryValidCandidateCanStillBeChallengeInadmissibleOnBudget() {
        FactoryModel factoryModel = new FactoryModel(
                List.of(mill()),
                List.of(routing()),
                List.of(new ProductDefinition(new ProductId(10), "Widget", 100)));

        ModelValidationResult factoryResult = FactoryModelValidator.validate(factoryModel);
        assertTrue(factoryResult.isValid(),
                () -> "expected a canonically well-formed, executable factory model: "
                        + factoryResult.errors());

        EquipmentCatalogueItemId cutter = new EquipmentCatalogueItemId("equipment.cutter");
        long cutterCostCredits = 5_000L;
        long startingBudget = 40_000L;
        int cutterCount = 9;
        long totalCost = cutterCostCredits * cutterCount;
        assertTrue(totalCost > startingBudget,
                "the fixture must actually exceed budget for this proof to hold");

        EquipmentCatalogue catalogue = new EquipmentCatalogue(
                new EquipmentCatalogueIdentity("catalogue.challenge.factory-basics", "1"),
                List.of(EquipmentOffer.of(cutter, cutterCostCredits)));

        ChallengeDefinition challenge = new ChallengeDefinition(
                new ChallengeIdentity("challenge.factory-basics", "1"),
                new FactoryFloorConstraint(12, 10),
                startingBudget,
                new ChallengeWorkload("product.product-a", 20),
                List.of(cutter),
                400L,
                new EvaluationPolicyIdentity("policy.contract-completion", "1"),
                catalogue.identity(),
                catalogue.semanticFingerprint());

        List<PlacedEquipment> occurrences = new ArrayList<>();
        for (int i = 0; i < cutterCount; i++) {
            occurrences.add(new PlacedEquipment(
                    new EquipmentOccurrenceId("cutter-" + i), cutter, new GridPlacement(i, 0)));
        }
        CandidateDraftSnapshot candidate = new CandidateDraftSnapshot(occurrences);

        CandidateAdmissibilityResult admissibility =
                CandidateAdmissibilityPolicy.assess(challenge, catalogue, candidate);

        assertEquals(false, admissibility.admitted(),
                () -> "expected a game-rule (budget) rejection, not structural invalidity");
        assertEquals("candidate.budget.exceeded", admissibility.issues().get(0).code(),
                "the rejection must be a challenge/economy game rule, not a factory structural "
                        + "rule -- FactoryModelValidator has no concept of credits or budgets at all");
    }

    private static ResourceDefinition mill() {
        return new ResourceDefinition(new MachineId(1), "Mill", 1, null, 0);
    }

    private static OperationDefinition routing() {
        return new OperationDefinition(
                100,
                "Widget routing",
                List.of(new OperationStepDefinition(1, "Rough milling", Set.of(new MachineId(1)), 5)));
    }
}
