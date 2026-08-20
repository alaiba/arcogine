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

    public static final class ScenarioLoadError extends SimError {
        public ScenarioLoadError(String message) {
            super("scenario load error: " + message);
        }
    }

    public static final class InvalidReference extends SimError {
        public InvalidReference(String message) {
            super("invalid reference: " + message);
        }
    }

    public static final class OutOfRange extends SimError {
        private final String field;

        public OutOfRange(String field, String message) {
            super("out of range (" + field + "): " + message);
            this.field = field;
        }

        public String field() { return field; }
    }

    public static final class Other extends SimError {
        public Other(String message) {
            super(message);
        }
    }

    /**
     * A journal entry whose debit postings do not sum to its credit postings. The core financial
     * invariant -- sum(debits) == sum(credits) -- must hold before an entry can ever enter
     * financial state, so this is thrown from JournalEntry's own constructor, not caught and
     * recovered from downstream.
     */
    public static final class UnbalancedJournalEntry extends SimError {
        private final String debits;
        private final String credits;

        public UnbalancedJournalEntry(String debits, String credits, String description) {
            super("unbalanced journal entry \"" + description + "\": debits=" + debits + ", credits=" + credits);
            this.debits = debits;
            this.credits = credits;
        }

        public String debits() { return debits; }
        public String credits() { return credits; }
    }
}
