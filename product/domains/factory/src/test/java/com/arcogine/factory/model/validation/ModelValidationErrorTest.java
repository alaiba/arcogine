package com.arcogine.factory.model.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ModelValidationErrorTest {

    @Test
    void rejectsNullField() {
        assertThrows(NullPointerException.class, () -> new ModelValidationError(null, "message"));
    }

    @Test
    void rejectsNullMessage() {
        assertThrows(NullPointerException.class, () -> new ModelValidationError("field", null));
    }

    @Test
    void toStringCombinesFieldAndMessage() {
        ModelValidationError error = new ModelValidationError("resources", "duplicate id");

        assertEquals("resources: duplicate id", error.toString());
    }
}
