package com.arcogine.types;

public record SimTime(long value) implements Comparable<SimTime> {
    public static final SimTime ZERO = new SimTime(0);

    public static SimTime of(long ticks) {
        return new SimTime(ticks);
    }

    public long ticks() {
        return value;
    }

    public SimTime plus(long delta) {
        return new SimTime(value + delta);
    }

    /** Saturating subtraction: returns 0 if other > this. */
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
