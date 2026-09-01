package com.arcogine.challenge.comparison;

import com.arcogine.challenge.attempt.ChallengeAttemptId;

/**
 * The deterministic result of attempting to compare two completed attempts.
 *
 * <p>{@code comparison} is present only when {@code compatibility} is comparable; when the two
 * attempts were evaluated under materially different challenge or evaluation-policy versions,
 * {@code comparison} is absent and {@code compatibility} carries structured, stable reasons
 * instead of silently implying an equivalence that does not hold.
 */
public record AttemptComparisonResult(
        ChallengeAttemptId first,
        ChallengeAttemptId second,
        AttemptCompatibility compatibility,
        AttemptComparison comparison) {

    public AttemptComparisonResult {
        if (first == null) {
            throw new NullPointerException("first");
        }
        if (second == null) {
            throw new NullPointerException("second");
        }
        if (compatibility == null) {
            throw new NullPointerException("compatibility");
        }
        if (compatibility.comparable() != (comparison != null)) {
            throw new IllegalArgumentException(
                    "comparison must be present exactly when attempts are comparable");
        }
    }
}
