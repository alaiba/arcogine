package com.arcogine.factory.process;

import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimTime;

/** Immutable aggregate progress projection for one accepted order. */
public record OrderObservation(
        OrderId orderId,
        ProductId productId,
        long requestedQuantity,
        long releasedQuantity,
        long completedQuantity,
        SimTime createdAt,
        SimTime completedAt,
        boolean complete) {}
