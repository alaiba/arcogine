package com.arcogine.types;

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

    public static final class EventOrderingViolation extends SimError {
        private final SimTime expectedMin;
        private final SimTime actual;

        public EventOrderingViolation(SimTime expectedMin, SimTime actual) {
            super("event ordering violation: expected time >= " + expectedMin + ", got " + actual);
            this.expectedMin = expectedMin;
            this.actual = actual;
        }

        public SimTime expectedMin() { return expectedMin; }
        public SimTime actual() { return actual; }
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
