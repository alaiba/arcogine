package com.arcogine.factory.orders;

import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;

/** Stores immutable accepted orders independently from mutable job execution state. */
public class OrderStore {

    private final List<Order> orders;
    private long nextId;
    private final java.util.Map<OrderId, OrderExecution> executions;

    public OrderStore() {
        this.orders = new ArrayList<>();
        this.nextId = 1;
        this.executions = new LinkedHashMap<>();
    }

    public OrderId createOrder(
            ProductId productId, long quantity, SimTime createdAt, double unitPrice) {
        OrderId id = new OrderId(nextId++);
        Order order = new Order(id, productId, quantity, createdAt, unitPrice);
        orders.add(order);
        executions.put(id, new OrderExecution(order));
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

    public OrderExecutionView execution(OrderId id) {
        OrderExecution execution = executions.get(id);
        if (execution == null) throw new SimError.UnknownId("order", id.value());
        return execution.view();
    }

    /** @return true only for the final child completion transition. */
    public boolean completeChild(OrderId id, SimTime time) {
        OrderExecution execution = executions.get(id);
        if (execution == null) throw new SimError.UnknownId("order", id.value());
        return execution.completeChild(time);
    }
}
