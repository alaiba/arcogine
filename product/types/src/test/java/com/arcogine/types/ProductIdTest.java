package com.arcogine.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ProductIdTest {

    @Test
    void compareToOrdersByValue() {
        assertTrue(new ProductId(1).compareTo(new ProductId(2)) < 0);
        assertTrue(new ProductId(2).compareTo(new ProductId(1)) > 0);
        assertEquals(0, new ProductId(1).compareTo(new ProductId(1)));
    }

    @Test
    void toStringIncludesValue() {
        assertEquals("Product(7)", new ProductId(7).toString());
    }
}
