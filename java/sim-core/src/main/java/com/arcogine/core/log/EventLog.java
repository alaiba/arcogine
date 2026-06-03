package com.arcogine.core.log;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class EventLog {

    private static final int DEFAULT_CAPACITY = 1_000_000;

    private final List<Event> events = new ArrayList<>();
    private final int maxCapacity;

    public EventLog() {
        this(DEFAULT_CAPACITY);
    }

    public EventLog(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    private EventLog(List<Event> events, int maxCapacity) {
        this.events.addAll(events);
        this.maxCapacity = maxCapacity;
    }

    public void append(Event event) {
        if (events.size() < maxCapacity) {
            events.add(event);
        }
    }

    public boolean isTruncated() {
        return events.size() >= maxCapacity;
    }

    public int count() {
        return events.size();
    }

    public List<Event> events() {
        return Collections.unmodifiableList(events);
    }

    public Stream<Event> filterByType(EventType eventType) {
        return events.stream().filter(event -> event.eventType() == eventType);
    }

    public EventLog snapshot() {
        return new EventLog(new ArrayList<>(events), maxCapacity);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EventLog other)) {
            return false;
        }
        return events.equals(other.events);
    }

    @Override
    public int hashCode() {
        return Objects.hash(events);
    }
}
