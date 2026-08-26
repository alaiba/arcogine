package com.arcogine.factory.orders;

import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/** Stores immutable accepted orders independently from mutable job execution state. */
public class OrderStore {

    private final List<Order> orders;
    private long nextId;

    public OrderStore() {
        this.orders = new ArrayList<>();
        this.nextId = 1;
    }

    public OrderId createOrder(
            ProductId productId, long quantity, SimTime createdAt, double unitPrice) {
        OrderId id = new OrderId(nextId++);
        orders.add(new Order(id, productId, quantity, createdAt, unitPrice));
        return id;
    }

    public Order get(OrderId id) {
        return orders.stream()
                .filter(order -> order.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new SimError.UnknownId("order", id.value()));
    }

    public Stream<Order> allOrders() {
        return orders.stream();
    }
}
