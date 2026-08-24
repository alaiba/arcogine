package com.arcogine.factory.model.validation;

/** Thrown when a {@code FactoryModel} fails structural validation before publication. */
public class FactoryModelValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final transient ModelValidationResult result;

    public FactoryModelValidationException(ModelValidationResult result) {
        super("factory model failed validation: " + result.errors());
        this.result = result;
    }

    public ModelValidationResult result() {
        return result;
    }
}
