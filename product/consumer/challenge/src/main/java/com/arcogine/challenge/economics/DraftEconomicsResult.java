package com.arcogine.challenge.economics;

/**
 * The outcome of {@link DraftEconomicsCalculator#calculate}: either a derived {@link
 * DraftEconomics} snapshot, or a structured {@link DraftEconomicsFailure} explaining why one could
 * not be derived.
 *
 * <p>Exactly one of {@link #economics()} or {@link #failure()} is present. This is a narrow,
 * domain-specific result shape -- not a generic {@code Result<T, E>} framework.
 */
public record DraftEconomicsResult(DraftEconomics economics, DraftEconomicsFailure failure) {

    public DraftEconomicsResult {
        if ((economics == null) == (failure == null)) {
            throw new IllegalArgumentException(
                    "exactly one of economics or failure must be present");
        }
    }

    public static DraftEconomicsResult success(DraftEconomics economics) {
        return new DraftEconomicsResult(economics, null);
    }

    public static DraftEconomicsResult failure(DraftEconomicsFailure failure) {
        return new DraftEconomicsResult(null, failure);
    }

    public boolean isSuccess() {
        return economics != null;
    }
}
