package com.arcogine.core.queue;

import com.arcogine.core.event.Event;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import java.util.Comparator;
import java.util.Optional;
import java.util.PriorityQueue;

public class Scheduler {

    /**
     * Events at the same SimTime are ordered by insertion sequence (FIFO) so that same-tick
     * ordering is deterministic rather than left to PriorityQueue's unspecified tie-breaking:
     * e.g. of two TaskEnd events due at the same tick, the one scheduled first is processed first.
     */
    private record Entry(Event event, long sequence) {}

    private static final Comparator<Entry> ORDER =
            Comparator.<Entry, SimTime>comparing(e -> e.event().time()).thenComparingLong(Entry::sequence);

    private final PriorityQueue<Entry> queue = new PriorityQueue<>(ORDER);
    private SimTime currentTime = SimTime.ZERO;
    private long nextSequence = 0;

    public void schedule(Event event) {
        if (event.time().compareTo(currentTime) < 0) {
            throw new SimError.EventOrderingViolation(currentTime, event.time());
        }
        // The insertion ordinal is the same-time FIFO key: wrapping it would invert that order, so
        // exhaustion fails closed before anything is queued.
        if (nextSequence == Long.MAX_VALUE) {
            throw new SimError.SequenceExhausted("scheduler insertion ordinal");
        }
        queue.add(new Entry(event, nextSequence++));
    }

    /** Test-only seam: starts the insertion ordinal at {@code nextOrdinal}. */
    void seedInsertionOrdinalForTesting(long nextOrdinal) {
        this.nextSequence = nextOrdinal;
    }

    public Optional<Event> nextEvent() {
        Entry entry = queue.poll();
        if (entry == null) {
            return Optional.empty();
        }
        currentTime = entry.event().time();
        return Optional.of(entry.event());
    }

    public Optional<SimTime> peekTime() {
        Entry head = queue.peek();
        return head == null ? Optional.empty() : Optional.of(head.event().time());
    }

    public SimTime currentTime() {
        return currentTime;
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
