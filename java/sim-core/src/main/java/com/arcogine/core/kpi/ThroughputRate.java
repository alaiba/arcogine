package com.arcogine.core.kpi;

import com.arcogine.core.event.EventType;
import com.arcogine.core.log.EventLog;
import com.arcogine.types.SimTime;

public final class ThroughputRate implements Kpi {

    @Override
    public String name() {
        return "throughput_rate";
    }

    @Override
    public KpiValue compute(EventLog log, SimTime currentTime) {
        long completed = log.filterByType(EventType.TaskEnd).count();
        double elapsed = Math.max(1, currentTime.ticks());
        return new KpiValue(name(), completed / elapsed, "task_completions/tick");
    }
}
