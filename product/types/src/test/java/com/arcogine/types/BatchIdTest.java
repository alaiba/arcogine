package com.arcogine.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BatchIdTest {

    @Test
    void compareToOrdersByValue() {
        assertTrue(new BatchId(1).compareTo(new BatchId(2)) < 0);
        assertTrue(new BatchId(2).compareTo(new BatchId(1)) > 0);
        assertEquals(0, new BatchId(1).compareTo(new BatchId(1)));
    }

    @Test
    void toStringIncludesValue() {
        assertEquals("Batch(7)", new BatchId(7).toString());
    }
}
