package com.arcogine.types;

/**
 * A point on the simulation clock: a non-negative tick count in {@code [0, Long.MAX_VALUE]}.
 *
 * <p>Construction refuses a negative value, so no negative {@code SimTime} exists. Arithmetic is
 * exact and never wraps: {@link #minus} yields the exact elapsed ticks or zero, and {@link #plus}
 * refuses a sum outside the domain instead of producing a time (docs/architecture/engine-semantics.md
 * section 5.1).
 */
public record SimTime(long value) implements Comparable<SimTime> {
    public static final SimTime ZERO = new SimTime(0);

    public SimTime {
        if (value < 0) {
            throw new IllegalArgumentException("simulated time must be non-negative, got " + value);
        }
    }

    public static SimTime of(long ticks) {
        return new SimTime(ticks);
    }

    public long ticks() {
        return value;
    }

    /**
     * Whether {@code this + delta} is a representable time, that is lies in {@code [0,
     * Long.MAX_VALUE]}. Exact for every {@code delta}; {@link #plus} succeeds exactly when this holds.
     */
    public boolean canAdd(long delta) {
        return delta >= 0 ? delta <= Long.MAX_VALUE - value : delta >= -value;
    }

    /**
     * Exact addition: the time {@code delta} ticks after this one.
     *
     * @throws ArithmeticException if the sum lies outside {@code [0, Long.MAX_VALUE]}; no time is
     *     produced
     */
    public SimTime plus(long delta) {
        if (!canAdd(delta)) {
            throw new ArithmeticException(
                    "simulated time " + value + " + " + delta + " is outside [0, Long.MAX_VALUE]");
        }
        return new SimTime(value + delta);
    }

    /**
     * Elapsed ticks from {@code other} to this time: exactly {@code this - other} when {@code
     * other <= this}, and 0 otherwise. Both operands are non-negative, so the difference always
     * fits.
     */
    public long minus(SimTime other) {
        return Math.max(0, value - other.value);
    }

    @Override
    public int compareTo(SimTime other) {
        return Long.compare(this.value, other.value);
    }

    @Override
    public String toString() {
        return "t=" + value;
    }
}
