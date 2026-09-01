package com.arcogine.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ControlledRevisionIdTest {

    @Test
    void generationProducesRfcUuidV4() {
        ControlledRevisionId id = ControlledRevisionId.generate();

        assertEquals(4, id.value().version());
        assertEquals(2, id.value().variant());
    }

    @Test
    void canonicalStringRoundTrips() {
        ControlledRevisionId original = ControlledRevisionId.generate();

        assertEquals(original, ControlledRevisionId.parse(original.toString()));
        assertEquals(original.toString(), ControlledRevisionId.parse(original.toString()).toString());
    }

    @Test
    void rejectsNonRfcV4AndNonCanonicalUuids() {
        UUID v1 = UUID.fromString("00000000-0000-1000-8000-000000000000");
        UUID nonRfcV4 = UUID.fromString("00000000-0000-4000-0000-000000000000");

        assertThrows(IllegalArgumentException.class, () -> new ControlledRevisionId(v1));
        assertThrows(IllegalArgumentException.class, () -> ControlledRevisionId.parse(v1.toString()));
        assertThrows(IllegalArgumentException.class, () -> new ControlledRevisionId(nonRfcV4));
        assertThrows(IllegalArgumentException.class, () -> ControlledRevisionId.parse(nonRfcV4.toString()));
        assertThrows(IllegalArgumentException.class, () -> ControlledRevisionId.parse(
            "abcdefab-cdef-4abc-8def-abcdefabcdef".toUpperCase()));
    }
}
