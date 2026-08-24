package com.arcogine.factory.model.validation;

/** One structural violation found while validating a {@code FactoryModel}. */
public record ModelValidationError(String field, String message) {

    public ModelValidationError {
        if (field == null) {
            throw new NullPointerException("field");
        }
        if (message == null) {
            throw new NullPointerException("message");
        }
    }

    @Override
    public String toString() {
        return field + ": " + message;
    }
}
