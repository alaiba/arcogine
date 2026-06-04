package com.arcogine.core.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Ported from crates/sim-core/src/handler.rs #[cfg(test)] module. */
class CompositeHandlerTest {

    /** Records the time of each event it handles. */
    private static final class TrackingHandler implements EventHandler {
        private final List<SimTime> calls;

        TrackingHandler(List<SimTime> calls) {
            this.calls = calls;
        }

        @Override
        public void handleEvent(Event event, Scheduler scheduler) {
            calls.add(event.time());
        }
    }

    /** Always fails. */
    private static final class FailingHandler implements EventHandler {
        @Override
        public void handleEvent(Event event, Scheduler scheduler) throws SimError {
            throw new SimError.Other("fail");
        }
    }

    private static Event makeEvent() {
        return Event.of(new SimTime(1), EventPayload.DemandEvaluation.INSTANCE);
    }

    @Test
    void compositeDispatchesToAllHandlers() throws SimError {
        List<SimTime> callsA = new ArrayList<>();
        List<SimTime> callsB = new ArrayList<>();
        CompositeHandler composite = new CompositeHandler(List.of(
                new TrackingHandler(callsA),
                new TrackingHandler(callsB)));
        Scheduler scheduler = new Scheduler();

        composite.handleEvent(makeEvent(), scheduler);

        assertEquals(1, callsA.size());
        assertEquals(1, callsB.size());
    }

    @Test
    void compositePropagatesFirstErrAndShortCircuits() {
        List<SimTime> calls = new ArrayList<>();
        CompositeHandler composite = new CompositeHandler(List.of(
                new FailingHandler(),
                new TrackingHandler(calls)));
        Scheduler scheduler = new Scheduler();

        assertThrows(SimError.class, () -> composite.handleEvent(makeEvent(), scheduler));
        assertTrue(calls.isEmpty(), "second handler should not be called");
    }
}
