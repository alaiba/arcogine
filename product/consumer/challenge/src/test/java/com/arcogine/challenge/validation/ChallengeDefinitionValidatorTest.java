package com.arcogine.challenge.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.challenge.ChallengeDefinition;
import com.arcogine.challenge.ChallengeFixtures;
import com.arcogine.challenge.ChallengeIdentity;
import com.arcogine.challenge.ChallengeWorkload;
import com.arcogine.challenge.EquipmentCatalogueItemId;
import com.arcogine.challenge.EvaluationPolicyIdentity;
import com.arcogine.challenge.FactoryFloorConstraint;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ChallengeDefinitionValidatorTest {

    @Test
    void referenceChallengePassesValidation() {
        ChallengeDefinitionValidationResult result =
                ChallengeDefinitionValidator.validate(ChallengeFixtures.referenceChallenge());

        assertTrue(result.isValid());
        assertEquals(List.of(), result.issues());
    }

    @Test
    void blankStableIdIsRejectedWithStableCodeAndPath() {
        ChallengeDefinition definition = withIdentity(new ChallengeIdentity(" ", "1"));

        ChallengeDefinitionValidationResult result = ChallengeDefinitionValidator.validate(definition);

        assertTrue(containsIssue(result, "identity.id.blank", "identity.id"));
    }

    @Test
    void blankVersionIsRejected() {
        ChallengeDefinition definition = withIdentity(new ChallengeIdentity("challenge.a", " "));

        ChallengeDefinitionValidationResult result = ChallengeDefinitionValidator.validate(definition);

        assertTrue(containsIssue(result, "identity.version.blank", "identity.version"));
    }

    @Test
    void nonPositiveFloorDimensionsAreRejected() {
        ChallengeDefinition base = ChallengeFixtures.referenceChallenge();
        ChallengeDefinition definition = new ChallengeDefinition(
                base.identity(),
                new FactoryFloorConstraint(0, -1),
                base.startingBudget(),
                base.workload(),
                base.availableEquipment(),
                base.deadline(),
                base.evaluationPolicy());

        ChallengeDefinitionValidationResult result = ChallengeDefinitionValidator.validate(definition);

        assertTrue(containsIssue(result, "floor.width.not-positive", "floor.width"));
        assertTrue(containsIssue(result, "floor.height.not-positive", "floor.height"));
    }

    @Test
    void negativeStartingBudgetIsRejected() {
        ChallengeDefinition base = ChallengeFixtures.referenceChallenge();
        ChallengeDefinition definition = new ChallengeDefinition(
                base.identity(),
                base.floor(),
                -1L,
                base.workload(),
                base.availableEquipment(),
                base.deadline(),
                base.evaluationPolicy());

        ChallengeDefinitionValidationResult result = ChallengeDefinitionValidator.validate(definition);

        assertTrue(containsIssue(result, "startingBudget.negative", "startingBudget"));
    }

    @Test
    void blankWorkloadReferenceAndNonPositiveQuantityAreRejected() {
        ChallengeDefinition base = ChallengeFixtures.referenceChallenge();
        ChallengeDefinition definition = new ChallengeDefinition(
                base.identity(),
                base.floor(),
                base.startingBudget(),
                new ChallengeWorkload("", 0),
                base.availableEquipment(),
                base.deadline(),
                base.evaluationPolicy());

        ChallengeDefinitionValidationResult result = ChallengeDefinitionValidator.validate(definition);

        assertTrue(containsIssue(result, "workload.productReference.blank", "workload.productReference"));
        assertTrue(containsIssue(
                result, "workload.requiredQuantity.not-positive", "workload.requiredQuantity"));
    }

    @Test
    void duplicateAvailableEquipmentIdsAreRejected() {
        ChallengeDefinition base = ChallengeFixtures.referenceChallenge();
        ChallengeDefinition definition = new ChallengeDefinition(
                base.identity(),
                base.floor(),
                base.startingBudget(),
                base.workload(),
                List.of(
                        new EquipmentCatalogueItemId("equipment.cutter"),
                        new EquipmentCatalogueItemId("equipment.cutter")),
                base.deadline(),
                base.evaluationPolicy());

        ChallengeDefinitionValidationResult result = ChallengeDefinitionValidator.validate(definition);

        assertTrue(containsIssue(
                result, "availableEquipment.id.duplicate", "availableEquipment[1]"));
    }

    @Test
    void blankAvailableEquipmentIdIsRejected() {
        ChallengeDefinition base = ChallengeFixtures.referenceChallenge();
        ChallengeDefinition definition = new ChallengeDefinition(
                base.identity(),
                base.floor(),
                base.startingBudget(),
                base.workload(),
                List.of(new EquipmentCatalogueItemId(" ")),
                base.deadline(),
                base.evaluationPolicy());

        ChallengeDefinitionValidationResult result = ChallengeDefinitionValidator.validate(definition);

        assertTrue(containsIssue(result, "availableEquipment.id.blank", "availableEquipment[0]"));
    }

    @Test
    void nullAvailableEquipmentDefaultsToEmptyAndPassesThatRule() {
        ChallengeDefinition base = ChallengeFixtures.referenceChallenge();
        ChallengeDefinition definition = new ChallengeDefinition(
                base.identity(),
                base.floor(),
                base.startingBudget(),
                base.workload(),
                null,
                base.deadline(),
                base.evaluationPolicy());

        assertEquals(List.of(), definition.availableEquipment());
    }

    @Test
    void nonPositiveDeadlineIsRejected() {
        ChallengeDefinition base = ChallengeFixtures.referenceChallenge();
        ChallengeDefinition definition = new ChallengeDefinition(
                base.identity(),
                base.floor(),
                base.startingBudget(),
                base.workload(),
                base.availableEquipment(),
                0L,
                base.evaluationPolicy());

        ChallengeDefinitionValidationResult result = ChallengeDefinitionValidator.validate(definition);

        assertTrue(containsIssue(result, "deadline.not-positive", "deadline"));
    }

    @Test
    void blankEvaluationPolicyIdAndVersionAreRejected() {
        ChallengeDefinition base = ChallengeFixtures.referenceChallenge();
        ChallengeDefinition definition = new ChallengeDefinition(
                base.identity(),
                base.floor(),
                base.startingBudget(),
                base.workload(),
                base.availableEquipment(),
                base.deadline(),
                new EvaluationPolicyIdentity(" ", " "));

        ChallengeDefinitionValidationResult result = ChallengeDefinitionValidator.validate(definition);

        assertTrue(containsIssue(result, "evaluationPolicy.id.blank", "evaluationPolicy.id"));
        assertTrue(containsIssue(result, "evaluationPolicy.version.blank", "evaluationPolicy.version"));
    }

    @Test
    void multipleSimultaneousErrorsAreReturnedInDeterministicOrder() {
        ChallengeDefinition definition = new ChallengeDefinition(
                new ChallengeIdentity("", ""),
                new FactoryFloorConstraint(0, 0),
                -1L,
                new ChallengeWorkload("", 0),
                List.of(),
                0L,
                new EvaluationPolicyIdentity("", ""));

        ChallengeDefinitionValidationResult result = ChallengeDefinitionValidator.validate(definition);

        List<String> codes = result.issues().stream()
                .map(ChallengeDefinitionIssue::code)
                .collect(Collectors.toList());

        assertEquals(
                List.of(
                        "identity.id.blank",
                        "identity.version.blank",
                        "floor.width.not-positive",
                        "floor.height.not-positive",
                        "startingBudget.negative",
                        "workload.productReference.blank",
                        "workload.requiredQuantity.not-positive",
                        "deadline.not-positive",
                        "evaluationPolicy.id.blank",
                        "evaluationPolicy.version.blank"),
                codes);
    }

    @Test
    void repeatedValidationOfIdenticalInputProducesEqualResults() {
        ChallengeDefinition definition = ChallengeFixtures.referenceChallenge();

        ChallengeDefinitionValidationResult first = ChallengeDefinitionValidator.validate(definition);
        ChallengeDefinitionValidationResult second = ChallengeDefinitionValidator.validate(definition);

        assertEquals(first, second);
    }

    @Test
    void validatingDoesNotMutateTheDefinition() {
        ChallengeDefinition definition = ChallengeFixtures.referenceChallenge();
        List<EquipmentCatalogueItemId> before = definition.availableEquipment();

        ChallengeDefinitionValidator.validate(definition);

        assertEquals(before, definition.availableEquipment());
    }

    private static ChallengeDefinition withIdentity(ChallengeIdentity identity) {
        ChallengeDefinition base = ChallengeFixtures.referenceChallenge();
        return new ChallengeDefinition(
                identity,
                base.floor(),
                base.startingBudget(),
                base.workload(),
                base.availableEquipment(),
                base.deadline(),
                base.evaluationPolicy());
    }

    private static boolean containsIssue(
            ChallengeDefinitionValidationResult result, String code, String path) {
        return result.issues().stream()
                .anyMatch(issue -> issue.code().equals(code) && issue.path().equals(path));
    }
}
