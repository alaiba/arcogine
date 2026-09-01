package com.arcogine.challenge.comparison;

import com.arcogine.challenge.attempt.ChallengeAttempt;
import com.arcogine.challenge.evaluation.ChallengeEvaluationResult;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic, headless design-to-design comparison of two completed {@link ChallengeAttempt}s.
 *
 * <p>Comparison never re-evaluates either attempt: it only reads already-owned C2 economics and
 * C3 evaluation facts. It first checks compatibility -- attempts must share the exact same
 * challenge identity/version and evaluation-policy identity/version -- and only ever explains
 * differences that already-supported facts contain. It introduces no new scoring dimension.
 */
public final class ChallengeAttemptComparator {

    private ChallengeAttemptComparator() {}

    /** Compares two attempts, returning structured incompatibility reasons if not comparable. */
    public static AttemptComparisonResult compare(ChallengeAttempt first, ChallengeAttempt second) {
        if (first == null) {
            throw new NullPointerException("first");
        }
        if (second == null) {
            throw new NullPointerException("second");
        }

        AttemptCompatibility compatibility = checkCompatibility(first, second);
        if (!compatibility.comparable()) {
            return new AttemptComparisonResult(first.id(), second.id(), compatibility, null);
        }
        return new AttemptComparisonResult(first.id(), second.id(), compatibility,
                buildComparison(first, second));
    }

    private static AttemptCompatibility checkCompatibility(ChallengeAttempt first,
            ChallengeAttempt second) {
        List<AttemptIncompatibilityReason> reasons = new ArrayList<>();
        if (!first.challengeIdentity().equals(second.challengeIdentity())) {
            reasons.add(new AttemptIncompatibilityReason("attempt.challenge.mismatch",
                    "attempts were evaluated against different challenge identities/versions"));
        }
        if (!first.evaluationPolicy().equals(second.evaluationPolicy())) {
            reasons.add(new AttemptIncompatibilityReason("attempt.evaluationPolicy.mismatch",
                    "attempts were evaluated under different evaluation-policy identities/versions"));
        }
        return reasons.isEmpty() ? AttemptCompatibility.compatible()
                : AttemptCompatibility.incomparable(reasons);
    }

    private static AttemptComparison buildComparison(ChallengeAttempt first, ChallengeAttempt second) {
        ChallengeEvaluationResult firstResult = first.evaluationResult();
        ChallengeEvaluationResult secondResult = second.evaluationResult();

        BigInteger scoreDelta = secondResult.score().subtract(firstResult.score());
        Long deadlineMarginDelta = firstResult.deadlineMarginTicks() != null
                        && secondResult.deadlineMarginTicks() != null
                ? secondResult.deadlineMarginTicks() - firstResult.deadlineMarginTicks()
                : null;
        long unusedBudgetDelta =
                secondResult.unusedBudgetCredits() - firstResult.unusedBudgetCredits();
        long constructionCostDelta = second.economics().committedConstructionCostCredits()
                - first.economics().committedConstructionCostCredits();

        int comparison = scoreDelta.signum();
        AttemptComparisonWinner winner = comparison == 0 ? AttemptComparisonWinner.TIE
                : comparison > 0 ? AttemptComparisonWinner.SECOND : AttemptComparisonWinner.FIRST;

        return new AttemptComparison(firstResult.successful(), secondResult.successful(), scoreDelta,
                deadlineMarginDelta, unusedBudgetDelta, constructionCostDelta, winner);
    }
}
