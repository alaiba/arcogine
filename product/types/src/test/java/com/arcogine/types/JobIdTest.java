package com.arcogine.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class JobIdTest {

    @Test
    void compareToOrdersByValue() {
        assertTrue(new JobId(1).compareTo(new JobId(2)) < 0);
        assertTrue(new JobId(2).compareTo(new JobId(1)) > 0);
        assertEquals(0, new JobId(1).compareTo(new JobId(1)));
    }

    @Test
    void toStringIncludesValue() {
        assertEquals("Job(7)", new JobId(7).toString());
    }
}
