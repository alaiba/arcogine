package com.arcogine.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EngineSemanticsTest {

    @Test
    void currentInterpretationIsNamedByTheDevelopmentMarker() {
        assertEquals("engine-semantics:wip", EngineSemantics.CURRENT.value());
        assertEquals("engine-semantics:wip", EngineSemantics.CURRENT.toString());
        assertTrue(EngineSemantics.isSupported(EngineSemantics.CURRENT));
        assertEquals(EngineSemantics.CURRENT, EngineSemantics.requireSupported(EngineSemantics.CURRENT));
    }

    @Test
    void discardedOrdinalNameIsRefusedRatherThanReadAsTheCurrentInterpretation() {
        EngineSemantics obsolete = new EngineSemantics("engine-semantics:v1");

        assertFalse(EngineSemantics.isSupported(obsolete));
        IllegalArgumentException failure = assertThrows(
                IllegalArgumentException.class, () -> EngineSemantics.requireSupported(obsolete));
        assertEquals("unsupported engine semantics: engine-semantics:v1", failure.getMessage());
    }

    @Test
    void valueIsRequiredAndCannotBeBlank() {
        assertThrows(NullPointerException.class, () -> new EngineSemantics(null));
        assertThrows(IllegalArgumentException.class, () -> new EngineSemantics(" "));
        assertThrows(NullPointerException.class, () -> EngineSemantics.isSupported(null));
    }
}
