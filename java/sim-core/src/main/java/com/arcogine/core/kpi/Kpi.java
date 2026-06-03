package com.arcogine.core.kpi;

import com.arcogine.core.log.EventLog;
import com.arcogine.types.SimTime;

public interface Kpi {
    String name();

    KpiValue compute(EventLog log, SimTime currentTime);
}
