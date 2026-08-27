package com.arcogine.challenge.economics;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class DraftEquipmentOccurrenceTest {

    @Test
    void nullItemIdIsRejected() {
        assertThrows(NullPointerException.class, () -> new DraftEquipmentOccurrence(null));
    }
}
