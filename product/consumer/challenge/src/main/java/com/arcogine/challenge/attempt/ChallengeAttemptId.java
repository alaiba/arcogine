package com.arcogine.challenge.attempt;

import java.util.UUID;

/**
 * Stable, game-owned identity of one historical player attempt.
 *
 * <p>This identifies which attempt occurred -- it is independent of {@code ChallengeIdentity}
 * (which challenge/rules), {@code EvaluationPolicyIdentity} (which scoring rules), a published
 * model fingerprint (which canonical semantic factory content), a controlled revision id (which
 * governed historical model occurrence), and a run id (which simulation runtime epoch). None of
 * those identities are derived from this one, and this one is never derived from them.
 */
public record ChallengeAttemptId(String value) {

    public ChallengeAttemptId {
        if (value == null) {
            throw new NullPointerException("value");
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must be non-blank");
        }
    }

    /** Generates a fresh identity for a newly recorded attempt. */
    public static ChallengeAttemptId generate() {
        return new ChallengeAttemptId(UUID.randomUUID().toString());
    }
}
