package com.arcogine.core.event;

import com.arcogine.types.SimTime;

public record Event(SimTime time, EventPayload payload) {

    public static Event of(SimTime time, EventPayload payload) {
        return new Event(time, payload);
    }
}
