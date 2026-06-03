package com.arcogine.core.queue;

import com.arcogine.core.event.Event;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import java.util.Comparator;
import java.util.Optional;
import java.util.PriorityQueue;

public class Scheduler {

    private final PriorityQueue<Event> queue = new PriorityQueue<>(Comparator.comparing(Event::time));
    private SimTime currentTime = SimTime.ZERO;

    public void schedule(Event event) {
        if (event.time().compareTo(currentTime) < 0) {
            throw new SimError.EventOrderingViolation(currentTime, event.time());
        }
        queue.add(event);
    }

    public Optional<Event> nextEvent() {
        Event event = queue.poll();
        if (event == null) {
            return Optional.empty();
        }
        currentTime = event.time();
        return Optional.of(event);
    }

    public Optional<SimTime> peekTime() {
        Event head = queue.peek();
        return head == null ? Optional.empty() : Optional.of(head.time());
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
