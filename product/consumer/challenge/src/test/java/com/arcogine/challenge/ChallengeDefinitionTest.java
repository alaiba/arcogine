package com.arcogine.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChallengeDefinitionTest {

    @Test
    void referenceChallengeConstructsWithNoActiveSimulationOrRuntime() {
        ChallengeDefinition definition = ChallengeFixtures.referenceChallenge();

        assertEquals("challenge.factory-basics", definition.identity().id());
        assertEquals(20, definition.workload().requiredQuantity());
    }

    @Test
    void challengeIdentityAndVersionAreIndependentlyRepresented() {
        ChallengeIdentity a = new ChallengeIdentity("challenge.factory-basics", "1");
        ChallengeIdentity b = new ChallengeIdentity("challenge.factory-basics", "2");

        assertEquals(a.id(), b.id());
        assertNotEquals(a.version(), b.version());
        assertNotEquals(a, b);
    }

    @Test
    void evaluationPolicyIdentityAndVersionAreIndependentlyRepresented() {
        EvaluationPolicyIdentity a = new EvaluationPolicyIdentity("policy.contract-completion", "1");
        EvaluationPolicyIdentity b = new EvaluationPolicyIdentity("policy.contract-completion", "2");

        assertEquals(a.id(), b.id());
        assertNotEquals(a.version(), b.version());
        assertNotEquals(a, b);
    }

    @Test
    void twoVersionsOfSameChallengeIdentityAreDistinguishable() {
        ChallengeDefinition v1 = ChallengeFixtures.referenceChallenge();
        ChallengeDefinition v2 = new ChallengeDefinition(
                new ChallengeIdentity(v1.identity().id(), "2"),
                v1.floor(),
                v1.startingBudget(),
                v1.workload(),
                v1.availableEquipment(),
                v1.deadline(),
                v1.evaluationPolicy());

        assertNotEquals(v1, v2);
        assertEquals(v1.identity().id(), v2.identity().id());
    }

    @Test
    void collectionArgumentMutationDoesNotAffectConstructedDefinition() {
        List<EquipmentCatalogueItemId> mutable = new ArrayList<>();
        mutable.add(new EquipmentCatalogueItemId("equipment.cutter"));

        ChallengeDefinition definition = new ChallengeDefinition(
                new ChallengeIdentity("id", "1"),
                new FactoryFloorConstraint(1, 1),
                0L,
                new ChallengeWorkload("product.a", 1),
                mutable,
                1L,
                new EvaluationPolicyIdentity("policy", "1"));

        mutable.add(new EquipmentCatalogueItemId("equipment.assembly-station"));

        assertEquals(1, definition.availableEquipment().size());
    }

    @Test
    void returnedCollectionIsImmutable() {
        ChallengeDefinition definition = ChallengeFixtures.referenceChallenge();

        assertThrows(
                UnsupportedOperationException.class,
                () -> definition.availableEquipment().add(new EquipmentCatalogueItemId("x")));
    }

    @Test
    void nullIdentityIsRejected() {
        assertThrows(
                NullPointerException.class,
                () -> new ChallengeDefinition(
                        null,
                        new FactoryFloorConstraint(1, 1),
                        0L,
                        new ChallengeWorkload("product.a", 1),
                        List.of(),
                        1L,
                        new EvaluationPolicyIdentity("policy", "1")));
    }
}
