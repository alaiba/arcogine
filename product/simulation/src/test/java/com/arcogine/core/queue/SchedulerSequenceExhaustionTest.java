package com.arcogine.core.queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import org.junit.jupiter.api.Test;

/**
 * The insertion ordinal is the same-time FIFO key, so it must fail closed rather than wrap. The
 * counter is started near its limit through the test-only seam; reaching it by scheduling is an
 * arithmetic argument, not a test.
 */
class SchedulerSequenceExhaustionTest {

    private static Event orderAt(long time, long quantity) {
        return Event.of(new SimTime(time), new EventPayload.OrderCreation(new ProductId(1), quantity, 1.0));
    }

    private static class SeededScheduler extends Scheduler {
        SeededScheduler(long nextOrdinal) {
            seedInsertionOrdinalForTesting(nextOrdinal);
        }
    }

    @Test
    void sameTimeEventsStayFifoUpToTheBoundary() {
        Scheduler scheduler = new SeededScheduler(Long.MAX_VALUE - 3);

        scheduler.schedule(orderAt(5, 1));
        scheduler.schedule(orderAt(5, 2));
        scheduler.schedule(orderAt(5, 3));

        for (long expected = 1; expected <= 3; expected++) {
            EventPayload.OrderCreation payload =
                    (EventPayload.OrderCreation) scheduler.nextEvent().orElseThrow().payload();
            assertEquals(expected, payload.quantity());
        }
    }

    @Test
    void schedulingBeyondTheLastOrdinalIsRefusedWithoutQueueingAnything() {
        Scheduler scheduler = new SeededScheduler(Long.MAX_VALUE - 1);
        scheduler.schedule(orderAt(5, 1));

        SimError.SequenceExhausted error =
                assertThrows(SimError.SequenceExhausted.class, () -> scheduler.schedule(orderAt(5, 2)));

        assertEquals("scheduler insertion ordinal", error.counter());
        assertEquals(1, scheduler.size());
        assertEquals(1L, ((EventPayload.OrderCreation) scheduler.nextEvent().orElseThrow().payload()).quantity());
    }
}
