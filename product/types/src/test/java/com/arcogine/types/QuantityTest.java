package com.arcogine.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.OptionalLong;
import org.junit.jupiter.api.Test;

/** Ported from crates/sim-types/src/lib.rs quantity_* tests. */
class QuantityTest {

    @Test
    void unitsRoundtrip() {
        Quantity q = Quantity.units(7);
        assertEquals(OptionalLong.of(7), q.asUnits());
    }

    @Test
    void volumeIsNotUnits() {
        Quantity q = Quantity.volume(3.5);
        assertFalse(q.asUnits().isPresent());
    }

    @Test
    void defaultIsZeroUnits() {
        assertEquals(new Quantity.Units(0), Quantity.defaultQuantity());
    }
}
