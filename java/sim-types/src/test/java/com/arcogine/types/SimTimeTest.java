package com.arcogine.types;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Ported from crates/sim-types/src/lib.rs simtime_* tests. */
class SimTimeTest {

    @Test
    void ticksReturnsInner() {
        assertEquals(42L, SimTime.of(42).ticks());
    }

    @Test
    void plusAddsDelta() {
        assertEquals(SimTime.of(15), SimTime.of(10).plus(5));
    }

    @Test
    void minusProducesDelta() {
        assertEquals(20L, SimTime.of(30).minus(SimTime.of(10)));
    }

    @Test
    void minusSaturatesAtZero() {
        assertEquals(0L, SimTime.of(5).minus(SimTime.of(10)));
    }

    @Test
    void zeroConstant() {
        assertEquals(new SimTime(0), SimTime.ZERO);
    }
}
