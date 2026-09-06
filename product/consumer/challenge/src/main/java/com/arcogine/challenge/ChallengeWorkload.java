package com.arcogine.challenge;

/**
 * The fixed production contract/workload a challenge asks the player to satisfy.
 *
 * <p>{@code productReference} is a game-owned identity naming the fixed product or contract this
 * challenge is built around. It is not an executable routing model -- Arcogine's canonical
 * production model remains the sole authority for executable routing semantics. This type does not
 * validate that this reference resolves to anything.
 */
public record ChallengeWorkload(String productReference, int requiredQuantity) {

    public ChallengeWorkload {
        if (productReference == null) {
            throw new NullPointerException("productReference");
        }
    }
}
