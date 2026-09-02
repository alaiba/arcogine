package com.arcogine.challenge.content;

/**
 * A single deterministic diagnostic produced while decoding an untrusted external representation
 * (e.g. loaded JSON) into a {@code ChallengeDefinition}.
 *
 * <p>This is a distinct failure domain from {@code
 * com.arcogine.challenge.validation.ChallengeDefinitionIssue}: a {@code ChallengeContentIssue}
 * means the external representation itself was absent, malformed, or mistyped and a {@code
 * ChallengeDefinition} could not even be constructed. It never overlaps with, extends, or shares a
 * result type with {@code ChallengeDefinitionIssue}, which instead diagnoses the scalar/structural
 * content of an already-constructed definition.
 *
 * @param code stable, machine-readable issue code (e.g. {@code "content.field.missing"})
 * @param path field path the issue applies to (e.g. {@code "identity.id"})
 * @param message human-readable description of the issue
 */
public record ChallengeContentIssue(String code, String path, String message) {

    public ChallengeContentIssue {
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
