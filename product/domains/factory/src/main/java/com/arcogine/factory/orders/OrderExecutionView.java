package com.arcogine.factory.orders;

import com.arcogine.types.OrderId;
import com.arcogine.types.SimTime;

/** Authoritative mutable execution progress for one immutable accepted order. */
public record OrderExecutionView(OrderId orderId, long requestedQuantity, long releasedQuantity,
        long completedQuantity, SimTime completedAt, boolean complete) {}
