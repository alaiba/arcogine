package com.arcogine.core.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Ported from crates/sim-core/src/event.rs #[cfg(test)] module. */
class EventTest {

    @Test
    void eventNewSetsTimeAndPayload() {
        Event e = Event.of(
                new SimTime(42),
                new EventPayload.OrderCreation(new ProductId(1), 5, 10.0));
        assertEquals(new SimTime(42), e.time());
        EventPayload.OrderCreation payload =
                assertInstanceOf(EventPayload.OrderCreation.class, e.payload());
        assertEquals(new ProductId(1), payload.productId());
        assertEquals(5L, payload.quantity());
        assertEquals(10.0, payload.unitPrice());
    }

    @Test
    void eventTypeDerivedFromEachPayloadVariant() {
        record Case(EventPayload payload, EventType expectedType) {}

        List<Case> cases = List.of(
                new Case(new EventPayload.OrderCreation(new ProductId(1), 1, 10.0), EventType.OrderCreation),
                new Case(new EventPayload.TaskStart(new JobId(1), new MachineId(1), 0), EventType.TaskStart),
                new Case(new EventPayload.TaskEnd(new JobId(1), new MachineId(1), 0), EventType.TaskEnd),
                new Case(new EventPayload.MachineAvailabilityChange(new MachineId(1), true),
                        EventType.MachineAvailabilityChange),
                new Case(new EventPayload.PriceChange(1.0), EventType.PriceChange),
                new Case(new EventPayload.AgentDecision("test"), EventType.AgentDecision),
                new Case(EventPayload.DemandEvaluation.INSTANCE, EventType.DemandEvaluation),
                new Case(EventPayload.AgentEvaluation.INSTANCE, EventType.AgentEvaluation));

        for (Case c : cases) {
            Event event = Event.of(SimTime.ZERO, c.payload());
            assertEquals(c.expectedType(), event.eventType());
        }
    }
}
