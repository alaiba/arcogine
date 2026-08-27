package com.arcogine.challenge.validation;

/**
 * A single deterministic diagnostic produced by {@link ChallengeDefinitionValidator}.
 *
 * @param code stable, machine-readable issue code (e.g. {@code "floor.width.not-positive"})
 * @param path field path the issue applies to (e.g. {@code "floor.width"})
 * @param message human-readable description of the issue
 */
public record ChallengeDefinitionIssue(String code, String path, String message) {

    public ChallengeDefinitionIssue {
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

    @Override
    public String toString() {
        return path + " [" + code + "]: " + message;
    }
}
