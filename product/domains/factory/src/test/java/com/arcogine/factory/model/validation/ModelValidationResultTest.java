package com.arcogine.factory.model.validation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ModelValidationResultTest {

    @Test
    void defaultsNullErrorsToEmpty() {
        ModelValidationResult result = new ModelValidationResult(null);

        assertTrue(result.isValid());
    }

    @Test
    void validFactoryProducesAnEmptyValidResult() {
        ModelValidationResult result = ModelValidationResult.valid();

        assertTrue(result.isValid());
        assertTrue(result.errors().isEmpty());
    }
}
