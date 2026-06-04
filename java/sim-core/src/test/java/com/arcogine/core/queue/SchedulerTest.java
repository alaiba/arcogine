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

        scheduler.schedule(Event.of(new SimTime(30), EventPayload.DemandEvaluation.INSTANCE));
        scheduler.schedule(Event.of(new SimTime(10), EventPayload.DemandEvaluation.INSTANCE));
        scheduler.schedule(Event.of(new SimTime(20), EventPayload.DemandEvaluation.INSTANCE));

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
                new EventPayload.OrderCreation(new ProductId(1), 1)));
        scheduler.schedule(Event.of(new SimTime(10), EventPayload.DemandEvaluation.INSTANCE));

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
        scheduler.schedule(Event.of(new SimTime(10), EventPayload.DemandEvaluation.INSTANCE));
        scheduler.nextEvent().orElseThrow();

        // Now try to schedule an event in the past
        SimError.EventOrderingViolation err = assertThrows(
                SimError.EventOrderingViolation.class,
                () -> scheduler.schedule(Event.of(new SimTime(5), EventPayload.DemandEvaluation.INSTANCE)));
        assertEquals(new SimTime(10), err.expectedMin());
        assertEquals(new SimTime(5), err.actual());
    }

    @Test
    void sameTimeEventsAreAccepted() {
        Scheduler scheduler = new Scheduler();

        scheduler.schedule(Event.of(new SimTime(10), EventPayload.DemandEvaluation.INSTANCE));
        scheduler.schedule(Event.of(new SimTime(10), EventPayload.AgentEvaluation.INSTANCE));

        Event e1 = scheduler.nextEvent().orElseThrow();
        assertEquals(new SimTime(10), e1.time());

        // After dequeuing time=10, scheduling another time=10 should work
        scheduler.schedule(Event.of(new SimTime(10), EventPayload.DemandEvaluation.INSTANCE));

        assertEquals(new SimTime(10), scheduler.nextEvent().orElseThrow().time());
        assertEquals(new SimTime(10), scheduler.nextEvent().orElseThrow().time());
    }

    @Test
    void emptySchedulerReturnsNone() {
        Scheduler scheduler = new Scheduler();
        assertTrue(scheduler.nextEvent().isEmpty());
        assertTrue(scheduler.isEmpty());
        assertEquals(0, scheduler.size());
    }
}
