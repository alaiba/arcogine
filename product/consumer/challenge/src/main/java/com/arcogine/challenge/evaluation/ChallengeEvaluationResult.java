package com.arcogine.challenge.evaluation;

import com.arcogine.challenge.ChallengeIdentity;
import com.arcogine.challenge.EvaluationPolicyIdentity;
import java.math.BigInteger;
import java.util.List;

/** The immutable, explainable result of evaluating one exact challenge input. */
public record ChallengeEvaluationResult(
        ChallengeIdentity challengeIdentity,
        EvaluationPolicyIdentity evaluationPolicy,
        EvaluationProvenance provenance,
        boolean successful,
        List<ChallengeEvaluationIssue> issues,
        Long deadlineMarginTicks,
        long unusedBudgetCredits,
        BigInteger score) {

    public ChallengeEvaluationResult {
        if (challengeIdentity == null) {
            throw new NullPointerException("challengeIdentity");
        }
        if (evaluationPolicy == null) {
            throw new NullPointerException("evaluationPolicy");
        }
        if (provenance == null) {
            throw new NullPointerException("provenance");
        }
        if (issues == null) {
            throw new NullPointerException("issues");
        }
        if (score == null) {
            throw new NullPointerException("score");
        }
        issues = List.copyOf(issues);
        if (successful != issues.isEmpty()) {
            throw new IllegalArgumentException("successful evaluations must have no issues");
        }
        if (successful && deadlineMarginTicks == null) {
            throw new IllegalArgumentException("successful evaluations require deadlineMarginTicks");
        }
        if (!successful && !score.equals(BigInteger.ZERO)) {
            throw new IllegalArgumentException("unsuccessful evaluations must have zero score");
        }
    }
}
