package com.arcogine.challenge.attempt;

import com.arcogine.challenge.ChallengeIdentity;
import com.arcogine.challenge.EvaluationPolicyIdentity;
import com.arcogine.challenge.admissibility.CandidateDraftSnapshot;
import com.arcogine.challenge.economics.DraftEconomics;
import com.arcogine.challenge.evaluation.ChallengeEvaluationResult;

/**
 * An immutable, reproducible record of one completed challenge attempt.
 *
 * <p>{@code ChallengeAttempt} is the attempt/comparison capability's historical aggregate: it retains the exact admitted
 * candidate snapshot, the exact construction economics used, and the exact evaluation result
 * (which itself already retains the exact challenge identity/version, evaluation-policy
 * identity/version, and opaque model/run provenance). It does not recompute or duplicate any of
 * those evaluation facts -- it only adds a stable game-owned identity for the historical occurrence and
 * guarantees the whole record stays immutable once constructed.
 *
 * <p>This is not an event-sourcing or workflow record: it captures one already-completed
 * attempt/evaluation, not a lifecycle of intermediate states.
 */
public record ChallengeAttempt(
        ChallengeAttemptId id,
        CandidateDraftSnapshot candidateSnapshot,
        DraftEconomics economics,
        ChallengeEvaluationResult evaluationResult) {

    public ChallengeAttempt {
        if (id == null) {
            throw new NullPointerException("id");
        }
        if (candidateSnapshot == null) {
            throw new NullPointerException("candidateSnapshot");
        }
        if (economics == null) {
            throw new NullPointerException("economics");
        }
        if (evaluationResult == null) {
            throw new NullPointerException("evaluationResult");
        }
    }

    /** Records a new completed attempt, generating a fresh {@link ChallengeAttemptId}. */
    public static ChallengeAttempt record(CandidateDraftSnapshot candidateSnapshot,
            DraftEconomics economics, ChallengeEvaluationResult evaluationResult) {
        return new ChallengeAttempt(ChallengeAttemptId.generate(), candidateSnapshot, economics,
                evaluationResult);
    }

    /** The exact challenge identity/version this attempt was evaluated against. */
    public ChallengeIdentity challengeIdentity() {
        return evaluationResult.challengeIdentity();
    }

    /** The exact evaluation-policy identity/version that produced {@link #evaluationResult()}. */
    public EvaluationPolicyIdentity evaluationPolicy() {
        return evaluationResult.evaluationPolicy();
    }
}
