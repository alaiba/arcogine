package com.arcogine.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MachineIdTest {

    @Test
    void compareToOrdersByValue() {
        assertTrue(new MachineId(1).compareTo(new MachineId(2)) < 0);
        assertTrue(new MachineId(2).compareTo(new MachineId(1)) > 0);
        assertEquals(0, new MachineId(1).compareTo(new MachineId(1)));
    }

    @Test
    void toStringIncludesValue() {
        assertEquals("Machine(7)", new MachineId(7).toString());
    }
}
