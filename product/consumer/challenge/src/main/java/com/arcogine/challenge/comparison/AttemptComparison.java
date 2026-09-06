package com.arcogine.challenge.comparison;

import java.math.BigInteger;

/**
 * Deterministic, explainable differences between two compatible completed attempts.
 *
 * <p>Every field here is derived directly from facts already owned by the candidate-admissibility capability (economics) or the evaluation capability
 * (evaluation result) -- this does not introduce a second scoring or evaluation policy. All deltas
 * are {@code second - first}, matching the order the two attempts were supplied in.
 */
public record AttemptComparison(
        boolean firstSuccessful,
        boolean secondSuccessful,
        BigInteger scoreDelta,
        Long deadlineMarginDeltaTicks,
        long unusedBudgetDeltaCredits,
        long constructionCostDeltaCredits,
        AttemptComparisonWinner winner) {

    public AttemptComparison {
        if (scoreDelta == null) {
            throw new NullPointerException("scoreDelta");
        }
        if (winner == null) {
            throw new NullPointerException("winner");
        }
    }
}
