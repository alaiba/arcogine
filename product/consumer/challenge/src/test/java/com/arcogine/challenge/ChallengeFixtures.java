package com.arcogine.challenge;

import java.util.List;

/**
 * The reference challenge used across tests: produce 20 units of Product A on a 12x10 floor with
 * a cutter/assembly/inspector catalogue, a 40,000 credit budget and a 400-tick deadline.
 */
public final class ChallengeFixtures {

    private ChallengeFixtures() {}

    public static ChallengeDefinition referenceChallenge() {
        return new ChallengeDefinition(
                new ChallengeIdentity("challenge.factory-basics", "1"),
                new FactoryFloorConstraint(12, 10),
                40_000L,
                new ChallengeWorkload("product.product-a", 20),
                List.of(
                        new EquipmentCatalogueItemId("equipment.cutter"),
                        new EquipmentCatalogueItemId("equipment.assembly-station"),
                        new EquipmentCatalogueItemId("equipment.inspector")),
                400L,
                new EvaluationPolicyIdentity("policy.contract-completion", "1"));
    }
}
