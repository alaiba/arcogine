package com.arcogine.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;

class SimTimeTest {

    private static final long MAX = Long.MAX_VALUE;
    private static final BigInteger BIG_MAX = BigInteger.valueOf(MAX);

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

    @Test
    void negativeTimeIsRefusedAtConstruction() {
        assertThrows(IllegalArgumentException.class, () -> SimTime.of(-1));
        assertThrows(IllegalArgumentException.class, () -> SimTime.of(Long.MIN_VALUE));
        assertThrows(IllegalArgumentException.class, () -> new SimTime(-1));
    }

    @Test
    void domainBoundsConstructNormally() {
        assertEquals(0L, SimTime.of(0).ticks());
        assertEquals(MAX, SimTime.of(MAX).ticks());
        assertEquals(0L, SimTime.ZERO.ticks());
    }

    @Test
    void minusIsExactElapsedTicksOrZeroAcrossTheDomainGrid() {
        long[] grid = {0, 1, 2, MAX - 1, MAX};
        for (long a : grid) {
            for (long b : grid) {
                BigInteger expected = BigInteger.valueOf(a).subtract(BigInteger.valueOf(b)).max(BigInteger.ZERO);
                assertEquals(
                        expected.longValueExact(),
                        SimTime.of(a).minus(SimTime.of(b)),
                        a + " - " + b);
            }
        }
        assertEquals(MAX, SimTime.of(MAX).minus(SimTime.of(0)));
        assertEquals(0L, SimTime.of(0).minus(SimTime.of(MAX)));
        assertEquals(1L, SimTime.of(MAX).minus(SimTime.of(MAX - 1)));
    }

    @Test
    void plusIsExactInsideTheDomainAndRefusesEverySumAboveIt() {
        long[] grid = {0, 1, MAX - 1, MAX};
        for (long a : grid) {
            for (long delta : grid) {
                BigInteger sum = BigInteger.valueOf(a).add(BigInteger.valueOf(delta));
                SimTime time = SimTime.of(a);
                if (sum.compareTo(BIG_MAX) <= 0) {
                    assertTrue(time.canAdd(delta), a + " + " + delta);
                    assertEquals(sum.longValueExact(), time.plus(delta).ticks(), a + " + " + delta);
                } else {
                    assertFalse(time.canAdd(delta), a + " + " + delta);
                    assertThrows(ArithmeticException.class, () -> time.plus(delta), a + " + " + delta);
                }
            }
        }
        assertEquals(SimTime.of(MAX), SimTime.of(MAX - 1).plus(1));
        assertThrows(ArithmeticException.class, () -> SimTime.of(MAX).plus(1));
    }

    @Test
    void plusRefusesADeltaThatWouldMakeTheResultNegative() {
        long[][] cases = {{0, -1}, {1, -2}, {MAX, Long.MIN_VALUE}, {0, Long.MIN_VALUE}};
        for (long[] c : cases) {
            assertTrue(BigInteger.valueOf(c[0]).add(BigInteger.valueOf(c[1])).signum() < 0);
            SimTime time = SimTime.of(c[0]);
            assertFalse(time.canAdd(c[1]));
            assertThrows(ArithmeticException.class, () -> time.plus(c[1]), c[0] + " + " + c[1]);
        }
        assertEquals(SimTime.ZERO, SimTime.of(MAX).plus(-MAX));
    }
}
