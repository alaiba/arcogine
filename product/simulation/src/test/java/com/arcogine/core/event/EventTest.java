package com.arcogine.core.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimTime;
import java.util.List;
import org.junit.jupiter.api.Test;

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
    void everyPayloadVariantIsCarriedUnchangedWithValueEquality() {
        List<EventPayload> payloads = List.of(
                new EventPayload.OrderCreation(new ProductId(1), 1, 10.0),
                new EventPayload.TaskStart(new JobId(1), new MachineId(1), 0),
                new EventPayload.TaskEnd(new JobId(1), new MachineId(1), 0),
                new EventPayload.OrderCompleted(new OrderId(1), new JobId(2), new ProductId(1), 5, 10.0),
                new EventPayload.MachineAvailabilityChange(new MachineId(1), true));

        for (EventPayload payload : payloads) {
            Event event = Event.of(SimTime.of(7), payload);
            assertSame(payload, event.payload());
            assertEquals(Event.of(SimTime.of(7), payload), event);
        }
    }
}
