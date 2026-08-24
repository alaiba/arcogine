package com.arcogine.core.event;

import com.arcogine.types.SimTime;

public record Event(SimTime time, EventType eventType, EventPayload payload) {

    public static Event of(SimTime time, EventPayload payload) {
        EventType eventType = switch (payload) {
            case EventPayload.OrderCreation ignored -> EventType.OrderCreation;
            case EventPayload.TaskStart ignored -> EventType.TaskStart;
            case EventPayload.TaskEnd ignored -> EventType.TaskEnd;
            case EventPayload.OrderCompleted ignored -> EventType.OrderCompleted;
            case EventPayload.MachineAvailabilityChange ignored -> EventType.MachineAvailabilityChange;
            case EventPayload.PriceChange ignored -> EventType.PriceChange;
            case EventPayload.AgentEnabledChanged ignored -> EventType.AgentEnabledChanged;
            case EventPayload.AgentDecision ignored -> EventType.AgentDecision;
            case EventPayload.DemandEvaluation ignored -> EventType.DemandEvaluation;
            case EventPayload.AgentEvaluation ignored -> EventType.AgentEvaluation;
        };
        return new Event(time, eventType, payload);
    }
}
