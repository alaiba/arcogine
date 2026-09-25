package com.arcogine.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EngineSemanticsVersionTest {

    @Test
    void currentVersionHasTheCanonicalSemanticIdentifier() {
        assertEquals("engine-semantics:v1", EngineSemanticsVersion.CURRENT.value());
        assertEquals("engine-semantics:v1", EngineSemanticsVersion.CURRENT.toString());
        assertTrue(EngineSemanticsVersion.isSupported(EngineSemanticsVersion.CURRENT));
        assertEquals(
                EngineSemanticsVersion.CURRENT,
                EngineSemanticsVersion.requireSupported(EngineSemanticsVersion.CURRENT));
    }

    @Test
    void unsupportedVersionIsDistinguishableAndFailsExplicitly() {
        EngineSemanticsVersion unsupported = new EngineSemanticsVersion("engine-semantics:v2");

        assertFalse(EngineSemanticsVersion.isSupported(unsupported));
        IllegalArgumentException failure = assertThrows(
                IllegalArgumentException.class, () -> EngineSemanticsVersion.requireSupported(unsupported));
        assertEquals("unsupported engine semantics version: engine-semantics:v2", failure.getMessage());
    }

    @Test
    void valueIsRequiredAndCannotBeBlank() {
        assertThrows(NullPointerException.class, () -> new EngineSemanticsVersion(null));
        assertThrows(IllegalArgumentException.class, () -> new EngineSemanticsVersion(" "));
        assertThrows(NullPointerException.class, () -> EngineSemanticsVersion.isSupported(null));
    }
}
