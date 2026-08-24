package com.arcogine.factory.model.validation;

import java.util.List;

/** Deterministic, structured result of validating a {@code FactoryModel}. */
public record ModelValidationResult(List<ModelValidationError> errors) {

    public ModelValidationResult {
        errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public static ModelValidationResult valid() {
        return new ModelValidationResult(List.of());
    }

    public boolean isValid() {
        return errors.isEmpty();
    }
}
