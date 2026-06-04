package com.arcogine.core.kpi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.log.EventLog;
import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimTime;
import org.junit.jupiter.api.Test;

/** Ported from crates/sim-core/src/kpi.rs #[cfg(test)] module. */
class KpiTest {

    private static EventLog emptyLog() {
        return new EventLog();
    }

    private static EventLog populatedLog() {
        EventLog log = new EventLog();
        log.append(Event.of(new SimTime(1), new EventPayload.OrderCreation(new ProductId(1), 5)));
        log.append(Event.of(new SimTime(2), new EventPayload.TaskStart(new JobId(1), new MachineId(1), 0)));
        log.append(Event.of(new SimTime(5), new EventPayload.TaskEnd(new JobId(1), new MachineId(1), 0)));
        log.append(Event.of(new SimTime(6), new EventPayload.OrderCreation(new ProductId(2), 3)));
        return log;
    }

    @Test
    void totalSimulatedTimeOnEmptyLog() {
        KpiValue v = new TotalSimulatedTime().compute(emptyLog(), SimTime.ZERO);
        assertEquals(0.0, v.value());
    }

    @Test
    void totalSimulatedTimeEqualsTicks() {
        KpiValue v = new TotalSimulatedTime().compute(emptyLog(), new SimTime(100));
        assertEquals(100.0, v.value());
    }

    @Test
    void eventCountOnEmptyLog() {
        KpiValue v = new EventCount().compute(emptyLog(), SimTime.ZERO);
        assertEquals(0.0, v.value());
    }

    @Test
    void eventCountCountsAllEvents() {
        KpiValue v = new EventCount().compute(populatedLog(), new SimTime(10));
        assertEquals(4.0, v.value());
    }

    @Test
    void throughputRateOnEmptyLog() {
        KpiValue v = new ThroughputRate().compute(emptyLog(), SimTime.ZERO);
        assertEquals(0.0, v.value());
    }

    @Test
    void throughputRateComputesCorrectly() {
        KpiValue v = new ThroughputRate().compute(populatedLog(), new SimTime(10));
        assertEquals(1.0 / 10.0, v.value());
    }

    @Test
    void orderCountOnEmptyLog() {
        KpiValue v = new OrderCount().compute(emptyLog(), SimTime.ZERO);
        assertEquals(0.0, v.value());
    }

    @Test
    void orderCountCountsOrderCreationEvents() {
        KpiValue v = new OrderCount().compute(populatedLog(), new SimTime(10));
        assertEquals(2.0, v.value());
    }
}
