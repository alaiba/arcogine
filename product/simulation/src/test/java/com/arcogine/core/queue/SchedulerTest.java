package com.arcogine.core.queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import org.junit.jupiter.api.Test;

/** Ported from crates/sim-core/tests/event_ordering.rs. */
class SchedulerTest {

    @Test
    void eventsDequeuedInTimeOrder() {
        Scheduler scheduler = new Scheduler();

        scheduler.schedule(orderAt(30, 1));
        scheduler.schedule(orderAt(10, 1));
        scheduler.schedule(orderAt(20, 1));

        assertEquals(new SimTime(10), scheduler.nextEvent().orElseThrow().time());
        assertEquals(new SimTime(20), scheduler.nextEvent().orElseThrow().time());
        assertEquals(new SimTime(30), scheduler.nextEvent().orElseThrow().time());
        assertTrue(scheduler.nextEvent().isEmpty());
    }

    @Test
    void monotonicTimeProgression() {
        Scheduler scheduler = new Scheduler();

        scheduler.schedule(Event.of(
                new SimTime(5),
                new EventPayload.OrderCreation(new ProductId(1), 1, 10.0)));
        scheduler.schedule(orderAt(10, 1));

        Event e1 = scheduler.nextEvent().orElseThrow();
        assertEquals(new SimTime(5), e1.time());
        assertEquals(new SimTime(5), scheduler.currentTime());

        Event e2 = scheduler.nextEvent().orElseThrow();
        assertEquals(new SimTime(10), e2.time());
        assertTrue(scheduler.currentTime().compareTo(new SimTime(5)) >= 0);
    }

    @Test
    void rejectPastTimeEvents() {
        Scheduler scheduler = new Scheduler();

        // Advance time by dequeuing an event
        scheduler.schedule(orderAt(10, 1));
        scheduler.nextEvent().orElseThrow();

        // Now try to schedule an event in the past
        SimError.EventOrderingViolation err = assertThrows(
                SimError.EventOrderingViolation.class,
                () -> scheduler.schedule(orderAt(5, 1)));
        assertEquals(new SimTime(10), err.expectedMin());
        assertEquals(new SimTime(5), err.actual());
    }

    @Test
    void sameTimeEventsAreAccepted() {
        Scheduler scheduler = new Scheduler();

        scheduler.schedule(orderAt(10, 1));
        scheduler.schedule(orderAt(10, 2));

        Event e1 = scheduler.nextEvent().orElseThrow();
        assertEquals(new SimTime(10), e1.time());

        // After dequeuing time=10, scheduling another time=10 should work
        scheduler.schedule(orderAt(10, 3));

        assertEquals(new SimTime(10), scheduler.nextEvent().orElseThrow().time());
        assertEquals(new SimTime(10), scheduler.nextEvent().orElseThrow().time());
    }

    @Test
    void sameTickEventsDequeueInInsertionOrder() {
        // Distinguishable same-tick events (different quantities) let us assert FIFO order
        // deterministically, rather than relying on PriorityQueue's unspecified tie-breaking.
        Scheduler scheduler = new Scheduler();

        scheduler.schedule(Event.of(
                new SimTime(10), new EventPayload.OrderCreation(new ProductId(1), 1, 1.0)));
        scheduler.schedule(Event.of(
                new SimTime(10), new EventPayload.OrderCreation(new ProductId(1), 2, 1.0)));
        scheduler.schedule(Event.of(
                new SimTime(10), new EventPayload.OrderCreation(new ProductId(1), 3, 1.0)));

        assertEquals(1L, orderQuantity(scheduler.nextEvent().orElseThrow()));
        assertEquals(2L, orderQuantity(scheduler.nextEvent().orElseThrow()));
        assertEquals(3L, orderQuantity(scheduler.nextEvent().orElseThrow()));
    }

    @Test
    void sameTickOrderingIsStableAcrossRepeatedRuns() {
        // Determinism contract: identical schedules must dequeue in identical order every run.
        for (int run = 0; run < 20; run++) {
            Scheduler scheduler = new Scheduler();
            for (long i = 0; i < 10; i++) {
                scheduler.schedule(Event.of(
                        new SimTime(1), new EventPayload.OrderCreation(new ProductId(1), i, 1.0)));
            }
            for (long expected = 0; expected < 10; expected++) {
                assertEquals(expected, orderQuantity(scheduler.nextEvent().orElseThrow()));
            }
        }
    }

    private static long orderQuantity(Event event) {
        return ((EventPayload.OrderCreation) event.payload()).quantity();
    }

    private static Event orderAt(long tick, long quantity) {
        return Event.of(new SimTime(tick), new EventPayload.OrderCreation(new ProductId(1), quantity, 1.0));
    }

    @Test
    void emptySchedulerReturnsNone() {
        Scheduler scheduler = new Scheduler();
        assertTrue(scheduler.nextEvent().isEmpty());
        assertTrue(scheduler.isEmpty());
        assertEquals(0, scheduler.size());
    }
}
