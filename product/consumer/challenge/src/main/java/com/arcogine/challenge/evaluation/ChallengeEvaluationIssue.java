package com.arcogine.challenge.evaluation;

/** A stable, game-rule diagnostic explaining why an evaluation did not succeed. */
public record ChallengeEvaluationIssue(String code, String path, String message) {

    public ChallengeEvaluationIssue {
        if (code == null) {
            throw new NullPointerException("code");
        }
        if (path == null) {
            throw new NullPointerException("path");
        }
        if (message == null) {
            throw new NullPointerException("message");
        }
    }
}
