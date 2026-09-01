package com.arcogine.types;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class RunIdTest {

    @Test
    void createsOpaqueDistinctIdentitiesAndRejectsNullValue() {
        assertNotEquals(RunId.create(), RunId.create());
        assertThrows(NullPointerException.class, () -> new RunId(null));
    }
}
