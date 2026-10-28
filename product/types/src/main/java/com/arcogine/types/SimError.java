package com.arcogine.types;

import java.util.Optional;
import java.util.OptionalLong;

@SuppressWarnings("serial")
public sealed class SimError extends RuntimeException {

    protected SimError(String message) {
        super(message);
    }

    public static final class InvalidStateTransition extends SimError {
        private final String context;

        public InvalidStateTransition(String context) {
            super("invalid state transition: " + context);
            this.context = context;
        }

        public String context() { return context; }
    }

    public static final class UnknownId extends SimError {
        private final String kind;
        private final long id;

        public UnknownId(String kind, long id) {
            super("unknown " + kind + " id: " + id);
            this.kind = kind;
            this.id = id;
        }

        public String kind() { return kind; }
        public long id() { return id; }
    }

    /**
     * A schedule whose time is not at or after the current time. Either the attempted time is an
     * earlier, representable {@link SimTime} ({@link #actual()}), or it is {@code expectedMin} plus a
     * {@link #duration()} that is not a representable time at all, because the sum lies outside
     * {@code [0, Long.MAX_VALUE]} -- the time is refused before it is ever constructed.
     */
    public static final class EventOrderingViolation extends SimError {
        private final SimTime expectedMin;
        private final SimTime actual;
        private final Long duration;

        public EventOrderingViolation(SimTime expectedMin, SimTime actual) {
            super("event ordering violation: expected time >= " + expectedMin + ", got " + actual);
            this.expectedMin = expectedMin;
            this.actual = actual;
            this.duration = null;
        }

        /** The attempted time {@code currentTime + duration} is not a representable {@link SimTime}. */
        public EventOrderingViolation(SimTime currentTime, long duration) {
            super("event ordering violation: expected time >= " + currentTime + ", got " + currentTime
                    + " + " + duration + " ticks, which is outside [0, Long.MAX_VALUE]");
            this.expectedMin = currentTime;
            this.actual = null;
            this.duration = duration;
        }

        public SimTime expectedMin() { return expectedMin; }

        /** The attempted time, or empty when it is not representable (see {@link #duration()}). */
        public Optional<SimTime> actual() { return Optional.ofNullable(actual); }

        /** The duration whose sum with {@link #expectedMin()} is unrepresentable, if that is the cause. */
        public OptionalLong duration() { return duration == null ? OptionalLong.empty() : OptionalLong.of(duration); }
    }

    public static final class OutOfRange extends SimError {
        private final String field;

        public OutOfRange(String field, String message) {
            super("out of range (" + field + "): " + message);
            this.field = field;
        }

        public String field() { return field; }
    }

    /**
     * A signed 64-bit sequence counter has no value left to assign, so the arithmetic that would
     * wrap is refused instead. {@code counter} names which counter ran out.
     */
    public static final class SequenceExhausted extends SimError {
        private final String counter;

        public SequenceExhausted(String counter) {
            super("sequence exhausted (" + counter + "): no value above Long.MAX_VALUE");
            this.counter = counter;
        }

        public String counter() { return counter; }
    }

    public static final class Other extends SimError {
        public Other(String message) {
            super(message);
        }
    }
}
