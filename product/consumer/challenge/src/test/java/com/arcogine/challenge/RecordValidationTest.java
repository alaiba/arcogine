package com.arcogine.challenge;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arcogine.challenge.catalogue.EquipmentCatalogueIdentity;
import org.junit.jupiter.api.Test;

class RecordValidationTest {

    @Test
    void challengeIdentityRejectsNullFields() {
        assertThrows(NullPointerException.class, () -> new ChallengeIdentity(null, "1"));
        assertThrows(NullPointerException.class, () -> new ChallengeIdentity("id", null));
    }

    @Test
    void evaluationPolicyIdentityRejectsNullFields() {
        assertThrows(NullPointerException.class, () -> new EvaluationPolicyIdentity(null, "1"));
        assertThrows(NullPointerException.class, () -> new EvaluationPolicyIdentity("id", null));
    }

    @Test
    void equipmentCatalogueItemIdRejectsNullValue() {
        assertThrows(NullPointerException.class, () -> new EquipmentCatalogueItemId(null));
    }

    @Test
    void catalogueIdentityRejectsNullFields() {
        assertThrows(NullPointerException.class, () -> new EquipmentCatalogueIdentity(null, "1"));
        assertThrows(NullPointerException.class, () -> new EquipmentCatalogueIdentity("id", null));
    }

    @Test
    void challengeWorkloadRejectsNullProductReference() {
        assertThrows(NullPointerException.class, () -> new ChallengeWorkload(null, 1));
    }

    @Test
    void challengeDefinitionRejectsNullFloorWorkloadAndEvaluationPolicy() {
        ChallengeDefinition base = ChallengeFixtures.referenceChallenge();

        assertThrows(
                NullPointerException.class,
                () -> new ChallengeDefinition(
                        base.identity(),
                        null,
                        base.startingBudget(),
                        base.workload(),
                        base.availableEquipment(),
                        base.deadline(),
                        base.evaluationPolicy()));
        assertThrows(
                NullPointerException.class,
                () -> new ChallengeDefinition(
                        base.identity(),
                        base.floor(),
                        base.startingBudget(),
                        null,
                        base.availableEquipment(),
                        base.deadline(),
                        base.evaluationPolicy()));
        assertThrows(
                NullPointerException.class,
                () -> new ChallengeDefinition(
                        base.identity(),
                        base.floor(),
                        base.startingBudget(),
                        base.workload(),
                        base.availableEquipment(),
                        base.deadline(),
                        null));
    }

    @Test
    void challengeDefinitionRejectsNullCatalogueIdentityAndBlankFingerprint() {
        ChallengeDefinition base = ChallengeFixtures.referenceChallenge();

        assertThrows(NullPointerException.class, () -> new ChallengeDefinition(
                base.identity(), base.floor(), base.startingBudget(), base.workload(),
                base.availableEquipment(), base.deadline(), base.evaluationPolicy(), null, null));
        assertThrows(IllegalArgumentException.class, () -> new ChallengeDefinition(
                base.identity(), base.floor(), base.startingBudget(), base.workload(),
                base.availableEquipment(), base.deadline(), base.evaluationPolicy(),
                base.catalogueIdentity(), " "));
    }
}
