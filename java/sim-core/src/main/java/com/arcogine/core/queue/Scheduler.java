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
     * ordering is deterministic rather than left to PriorityQueue's unspecified tie-breaking.
     * This preserves the existing implicit expectation that, e.g., a TaskStart scheduled before
     * a TaskEnd at the same tick is processed first.
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
        queue.add(new Entry(event, nextSequence++));
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
