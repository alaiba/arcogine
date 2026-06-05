package com.arcogine.types;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Ported from crates/sim-types/src/lib.rs simerror_display_* tests. */
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
    void scenarioLoadMessage() {
        assertEquals(
            "scenario load error: bad toml",
            new SimError.ScenarioLoadError("bad toml").getMessage());
    }

    @Test
    void invalidReferenceMessage() {
        assertEquals(
            "invalid reference: no such machine",
            new SimError.InvalidReference("no such machine").getMessage());
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
}
