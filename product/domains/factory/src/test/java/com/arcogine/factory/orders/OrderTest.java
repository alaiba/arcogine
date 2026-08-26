package com.arcogine.factory.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import org.junit.jupiter.api.Test;

class OrderTest {

    @Test
    void orderKeepsAcceptedIntentAndDerivesValue() {
        Order order = new Order(new OrderId(7), new ProductId(3), 5, new SimTime(11), 12.0);

        assertEquals(new OrderId(7), order.id());
        assertEquals(new ProductId(3), order.productId());
        assertEquals(5L, order.quantity());
        assertEquals(new SimTime(11), order.createdAt());
        assertEquals(12.0, order.unitPrice());
        assertEquals(60.0, order.orderValue());
    }

    @Test
    void orderStoreAllocatesIndependentOrderIdentities() {
        OrderStore store = new OrderStore();
        OrderId first = store.createOrder(new ProductId(1), 1, new SimTime(0), 10.0);
        OrderId second = store.createOrder(new ProductId(1), 1, new SimTime(0), 10.0);

        assertNotEquals(first, second);
        assertEquals(first, store.get(first).id());
        assertEquals(second, store.get(second).id());
    }

    @Test
    void orderStoreUnknownIdReturnsError() {
        OrderStore store = new OrderStore();
        SimError.UnknownId error =
                assertThrows(SimError.UnknownId.class, () -> store.get(new OrderId(999)));

        assertEquals("order", error.kind());
        assertEquals(999L, error.id());
    }
}
