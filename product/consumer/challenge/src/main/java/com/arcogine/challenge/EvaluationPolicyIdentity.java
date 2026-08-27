package com.arcogine.challenge;

/**
 * Stable identity of a game-owned evaluation policy.
 *
 * <p>{@code id} identifies the policy itself; {@code version} identifies its semantic revision.
 * This is distinct from, and independently versioned from, {@link ChallengeIdentity}. C1 does not
 * validate that an evaluator implementation exists for a given policy version -- it only requires
 * that the identity be present.
 */
public record EvaluationPolicyIdentity(String id, String version) {

    public EvaluationPolicyIdentity {
        if (id == null) {
            throw new NullPointerException("id");
        }
        if (version == null) {
            throw new NullPointerException("version");
        }
    }
}
