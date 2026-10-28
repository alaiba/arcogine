package com.arcogine.types;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.OptionalLong;
import org.junit.jupiter.api.Test;

class SimErrorTest {

    @Test
    void invalidStateTransitionMessage() {
        assertEquals(
            "invalid state transition: test",
            new SimError.InvalidStateTransition("test").getMessage());
    }

    @Test
    void unknownIdMessage() {
        assertEquals(
            "unknown machine id: 5",
            new SimError.UnknownId("machine", 5).getMessage());
    }

    @Test
    void eventOrderingMessage() {
        assertEquals(
            "event ordering violation: expected time >= t=10, got t=5",
            new SimError.EventOrderingViolation(SimTime.of(10), SimTime.of(5)).getMessage());
    }

    @Test
    void outOfRangeMessage() {
        assertEquals(
            "out of range (price): must be positive",
            new SimError.OutOfRange("price", "must be positive").getMessage());
    }

    @Test
    void otherMessage() {
        assertEquals("oops", new SimError.Other("oops").getMessage());
    }

    @Test
    void sequenceExhaustedMessageAndCounter() {
        SimError.SequenceExhausted error = new SimError.SequenceExhausted("event sequence");
        assertEquals(
            "sequence exhausted (event sequence): no value above Long.MAX_VALUE", error.getMessage());
        assertEquals("event sequence", error.counter());
    }

    @Test
    void invalidStateTransitionExposesContext() {
        assertEquals("test", new SimError.InvalidStateTransition("test").context());
    }

    @Test
    void unknownIdExposesKindAndId() {
        SimError.UnknownId error = new SimError.UnknownId("machine", 5);
        assertEquals("machine", error.kind());
        assertEquals(5L, error.id());
    }

    @Test
    void eventOrderingViolationExposesTimes() {
        SimError.EventOrderingViolation error =
            new SimError.EventOrderingViolation(SimTime.of(10), SimTime.of(5));
        assertEquals(SimTime.of(10), error.expectedMin());
        assertEquals(Optional.of(SimTime.of(5)), error.actual());
        assertEquals(OptionalLong.empty(), error.duration());
    }

    @Test
    void eventOrderingViolationDescribesAnUnrepresentableTime() {
        SimError.EventOrderingViolation error =
            new SimError.EventOrderingViolation(SimTime.of(10), Long.MAX_VALUE);
        assertEquals(
            "event ordering violation: expected time >= t=10, got t=10 + 9223372036854775807 ticks, "
                + "which is outside [0, Long.MAX_VALUE]",
            error.getMessage());
        assertEquals(SimTime.of(10), error.expectedMin());
        assertEquals(Optional.empty(), error.actual());
        assertEquals(OptionalLong.of(Long.MAX_VALUE), error.duration());
    }

    @Test
    void outOfRangeExposesField() {
        assertEquals("price", new SimError.OutOfRange("price", "must be positive").field());
    }
}
