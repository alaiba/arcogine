package com.arcogine.challenge.evaluation;

/**
 * Opaque, immutable references to the published design and run whose supplied facts are evaluated.
 *
 * <p>These are attribution values only. They do not grant the challenge module access to a model,
 * runtime, or execution internals.
 */
public record EvaluationProvenance(String publishedModelReference, String runReference) {

    public EvaluationProvenance {
        requireNonBlank(publishedModelReference, "publishedModelReference");
        requireNonBlank(runReference, "runReference");
    }

    private static void requireNonBlank(String value, String name) {
        if (value == null) {
            throw new NullPointerException(name);
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must be non-blank");
        }
    }
}
