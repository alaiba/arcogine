package com.arcogine.challenge.evaluation;

import com.arcogine.challenge.ChallengeDefinition;

/** Explicit, immutable inputs to a deterministic challenge evaluation. */
public record ChallengeEvaluationInput(
        ChallengeDefinition challenge,
        EvaluationProvenance provenance,
        AuthoritativeOutcomeFacts outcomeFacts,
        long committedConstructionCostCredits) {

    public ChallengeEvaluationInput {
        if (challenge == null) {
            throw new NullPointerException("challenge");
        }
        if (provenance == null) {
            throw new NullPointerException("provenance");
        }
        if (outcomeFacts == null) {
            throw new NullPointerException("outcomeFacts");
        }
        if (committedConstructionCostCredits < 0) {
            throw new IllegalArgumentException("committedConstructionCostCredits must be non-negative");
        }
    }
}
