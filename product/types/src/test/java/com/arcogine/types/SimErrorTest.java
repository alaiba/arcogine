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
        assertEquals(SimTime.of(5), error.actual());
    }

    @Test
    void outOfRangeExposesField() {
        assertEquals("price", new SimError.OutOfRange("price", "must be positive").field());
    }

    @Test
    void unbalancedJournalEntryMessageAndAccessors() {
        SimError.UnbalancedJournalEntry error =
            new SimError.UnbalancedJournalEntry("10", "5", "misc entry");
        assertEquals(
            "unbalanced journal entry \"misc entry\": debits=10, credits=5",
            error.getMessage());
        assertEquals("10", error.debits());
        assertEquals("5", error.credits());
    }
}
