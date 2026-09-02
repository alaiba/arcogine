package com.arcogine.challenge.comparison;

import java.util.List;

/** The deterministic outcome of checking whether two attempts may be meaningfully compared. */
public record AttemptCompatibility(boolean comparable, List<AttemptIncompatibilityReason> reasons) {

    public AttemptCompatibility {
        if (reasons == null) {
            throw new NullPointerException("reasons");
        }
        reasons = List.copyOf(reasons);
        if (comparable != reasons.isEmpty()) {
            throw new IllegalArgumentException("comparable attempts must have no reasons");
        }
    }

    public static AttemptCompatibility compatible() {
        return new AttemptCompatibility(true, List.of());
    }

    public static AttemptCompatibility incomparable(List<AttemptIncompatibilityReason> reasons) {
        if (reasons == null || reasons.isEmpty()) {
            throw new IllegalArgumentException("incomparable attempts must have reasons");
        }
        return new AttemptCompatibility(false, reasons);
    }
}
