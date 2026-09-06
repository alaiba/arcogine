package com.arcogine.factory.orders;

import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimTime;

/**
 * Immutable accepted-order intent. Production execution is tracked separately by {@code Job};
 * multiple jobs may reference the same order (ADR-0010 intra-order execution/work-item identity)
 * without copying commercial or quantity facts into mutable execution state.
 */
public record Order(
        OrderId id,
        ProductId productId,
        long quantity,
        SimTime createdAt,
        double unitPrice) {

    /** OrderValue: quantity x the order's agreed unit price. */
    public double orderValue() {
        return quantity * unitPrice;
    }
}
