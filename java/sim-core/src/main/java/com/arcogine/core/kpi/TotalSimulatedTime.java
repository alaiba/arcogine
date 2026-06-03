package com.arcogine.core.kpi;

import com.arcogine.core.log.EventLog;
import com.arcogine.types.SimTime;

public final class TotalSimulatedTime implements Kpi {

    @Override
    public String name() {
        return "total_simulated_time";
    }

    @Override
    public KpiValue compute(EventLog log, SimTime currentTime) {
        return new KpiValue(name(), currentTime.ticks(), "ticks");
    }
}
