package com.arcogine.factory.orders;

import com.arcogine.types.OrderId;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;

final class OrderExecution {
    private final OrderId orderId; private final long requested; private long completed; private SimTime completedAt;
    OrderExecution(Order order) { orderId = order.id(); requested = order.quantity(); }
    boolean completeChild(SimTime time) {
        if (completed >= requested) throw new SimError.InvalidStateTransition("order " + orderId + " already complete");
        completed++;
        if (completed == requested) { completedAt = time; return true; }
        return false;
    }
    OrderExecutionView view() { return new OrderExecutionView(orderId, requested, requested, completed, completedAt, completed == requested); }
}
