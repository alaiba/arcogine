package com.arcogine.challenge.comparison;

import java.math.BigInteger;

/**
 * Deterministic, explainable differences between two compatible completed attempts.
 *
 * <p>Every field here is derived directly from facts already owned by the candidate-admissibility capability (economics) or the evaluation capability
 * (evaluation result) -- this does not introduce a second scoring or evaluation policy. All deltas
 * are the exact mathematical value of {@code second - first}, matching the order the two attempts
 * were supplied in; none wraps, whatever {@code long} values the compatible attempts carry. The
 * deadline-margin difference is {@code null} unless both attempts report a deadline margin.
 */
public record AttemptComparison(
        boolean firstSuccessful,
        boolean secondSuccessful,
        BigInteger scoreDelta,
        BigInteger deadlineMarginDeltaTicks,
        BigInteger unusedBudgetDeltaCredits,
        BigInteger constructionCostDeltaCredits,
        AttemptComparisonWinner winner) {

    public AttemptComparison {
        if (scoreDelta == null) {
            throw new NullPointerException("scoreDelta");
        }
        if (unusedBudgetDeltaCredits == null) {
            throw new NullPointerException("unusedBudgetDeltaCredits");
        }
        if (constructionCostDeltaCredits == null) {
            throw new NullPointerException("constructionCostDeltaCredits");
        }
        if (winner == null) {
            throw new NullPointerException("winner");
        }
    }
}
