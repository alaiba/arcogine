package com.arcogine.challenge;

/**
 * Stable identity of a game-owned challenge, independent of Arcogine's canonical production
 * model.
 *
 * <p>{@code id} identifies the challenge itself; {@code version} identifies the content/rules
 * revision of that challenge. Both are game-owned strings -- they are never derived from a
 * factory model version, a published model fingerprint, a runtime/session identifier, or object
 * identity.
 */
public record ChallengeIdentity(String id, String version) {

    public ChallengeIdentity {
        if (id == null) {
            throw new NullPointerException("id");
        }
        if (version == null) {
            throw new NullPointerException("version");
        }
    }
}
