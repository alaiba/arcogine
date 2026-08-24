package com.arcogine.core.log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.event.EventType;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Ported from crates/sim-core/src/log.rs #[cfg(test)] module. */
class EventLogTest {

    private static Event makeOrder(long t) {
        return Event.of(new SimTime(t), new EventPayload.OrderCreation(new ProductId(1), 1, 10.0));
    }

    @Test
    void newLogIsEmpty() {
        EventLog log = new EventLog();
        assertEquals(0, log.count());
    }

    @Test
    void appendIncreasesCount() {
        EventLog log = new EventLog();
        log.append(makeOrder(1));
        assertEquals(1, log.count());
        log.append(makeOrder(2));
        assertEquals(2, log.count());
    }

    @Test
    void filterByTypeReturnsMatching() {
        EventLog log = new EventLog();
        log.append(makeOrder(1));
        log.append(Event.of(new SimTime(2), EventPayload.DemandEvaluation.INSTANCE));
        log.append(makeOrder(3));

        assertEquals(2, log.filterByType(EventType.OrderCreation).count());
        assertEquals(1, log.filterByType(EventType.DemandEvaluation).count());
    }

    @Test
    void snapshotReturnsClone() {
        EventLog log = new EventLog();
        log.append(makeOrder(1));
        EventLog snap = log.snapshot();
        assertEquals(log, snap);
        assertEquals(1, snap.count());
    }

    @Test
    void iterYieldsInsertionOrder() {
        EventLog log = new EventLog();
        log.append(makeOrder(10));
        log.append(makeOrder(20));
        log.append(makeOrder(5));

        List<Long> times = log.events().stream().map(e -> e.time().ticks()).toList();
        assertEquals(List.of(10L, 20L, 5L), times);
    }

    @Test
    void eventLogCapsAtMaxCapacity() {
        EventLog log = new EventLog(5);
        for (long i = 0; i < 10; i++) {
            log.append(makeOrder(i));
        }
        assertEquals(5, log.count());
    }

    @Test
    void eventLogEqualityIgnoresCapacity() {
        EventLog logA = new EventLog(100);
        EventLog logB = new EventLog(200);
        for (long i = 0; i < 5; i++) {
            logA.append(makeOrder(i));
            logB.append(makeOrder(i));
        }
        assertEquals(logA, logB);
    }

    @Test
    void eventLogIsTruncated() {
        EventLog log = new EventLog(3);
        assertFalse(log.isTruncated());
        for (long i = 0; i < 3; i++) {
            log.append(makeOrder(i));
        }
        assertTrue(log.isTruncated());
        log.append(makeOrder(99));
        assertEquals(3, log.count());
    }

    @Test
    void defaultLogHasLargeCapacity() {
        EventLog log = new EventLog();
        assertEquals(0, log.count());
        assertFalse(log.isTruncated());
    }
}
