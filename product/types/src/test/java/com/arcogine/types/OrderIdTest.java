package com.arcogine.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OrderIdTest {

    @Test
    void compareToOrdersByValue() {
        assertTrue(new OrderId(1).compareTo(new OrderId(2)) < 0);
        assertTrue(new OrderId(2).compareTo(new OrderId(1)) > 0);
        assertEquals(0, new OrderId(1).compareTo(new OrderId(1)));
    }

    @Test
    void toStringIncludesValue() {
        assertEquals("Order(7)", new OrderId(7).toString());
    }
}
