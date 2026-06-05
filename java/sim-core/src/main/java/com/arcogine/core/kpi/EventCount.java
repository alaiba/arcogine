package com.arcogine.core.kpi;

import com.arcogine.core.log.EventLog;
import com.arcogine.types.SimTime;

public final class EventCount implements Kpi {

    @Override
    public String name() {
        return "event_count";
    }

    @Override
    public KpiValue compute(EventLog log, SimTime currentTime) {
        return new KpiValue(name(), log.count(), "events");
    }
}
