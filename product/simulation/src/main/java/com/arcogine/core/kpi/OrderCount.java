package com.arcogine.core.kpi;

import com.arcogine.core.event.EventType;
import com.arcogine.core.log.EventLog;
import com.arcogine.types.SimTime;

public final class OrderCount implements Kpi {

    @Override
    public String name() {
        return "order_count";
    }

    @Override
    public KpiValue compute(EventLog log, SimTime currentTime) {
        long orders = log.filterByType(EventType.OrderCreation).count();
        return new KpiValue(name(), orders, "orders");
    }
}
