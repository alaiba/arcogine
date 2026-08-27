package com.arcogine.challenge;

import com.arcogine.challenge.catalogue.EquipmentCatalogueIdentity;
import java.util.List;

/**
 * An immutable, explicitly versioned, game-owned definition of a Challenge Readiness challenge.
 *
 * <p>{@code ChallengeDefinition} answers game-owned questions -- what the player must build,
 * within what constraints, to satisfy a fixed contract -- and is entirely independent of
 * Arcogine's canonical factory runtime. It carries no challenge, score, medal, or evaluation
 * outcome; those belong to later Challenge Readiness slices.
 *
 * <p>Construction only establishes an immutable value; it does not decide whether that value
 * represents valid challenge content. Use {@link
 * com.arcogine.challenge.validation.ChallengeDefinitionValidator} to obtain deterministic
 * structured diagnostics.
 */
public record ChallengeDefinition(
        ChallengeIdentity identity,
        FactoryFloorConstraint floor,
        long startingBudget,
        ChallengeWorkload workload,
        List<EquipmentCatalogueItemId> availableEquipment,
        long deadline,
        EvaluationPolicyIdentity evaluationPolicy,
        EquipmentCatalogueIdentity catalogueIdentity) {

        public ChallengeDefinition(
            ChallengeIdentity identity,
            FactoryFloorConstraint floor,
            long startingBudget,
            ChallengeWorkload workload,
            List<EquipmentCatalogueItemId> availableEquipment,
            long deadline,
            EvaluationPolicyIdentity evaluationPolicy) {
        this(identity, floor, startingBudget, workload, availableEquipment, deadline,
            evaluationPolicy, new EquipmentCatalogueIdentity("catalogue." + identity.id(),
                identity.version()));
        }

    public ChallengeDefinition {
        if (identity == null) {
            throw new NullPointerException("identity");
        }
        if (floor == null) {
            throw new NullPointerException("floor");
        }
        if (workload == null) {
            throw new NullPointerException("workload");
        }
        if (evaluationPolicy == null) {
            throw new NullPointerException("evaluationPolicy");
        }
        if (catalogueIdentity == null) {
            throw new NullPointerException("catalogueIdentity");
        }
        availableEquipment = availableEquipment == null ? List.of() : List.copyOf(availableEquipment);
    }
}
