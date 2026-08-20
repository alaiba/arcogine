package com.arcogine.agents;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.handler.EventHandler;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.types.SimError;
import java.util.Optional;

public class SalesAgent implements EventHandler {

    public final SalesAgentConfig config;
    private AgentObservation observation;
    private long interventions;

    public SalesAgent(SalesAgentConfig config) {
        this.config = config;
        this.observation = AgentObservation.DEFAULT;
        this.interventions = 0;
    }

    public static SalesAgent withDefaultConfig() {
        return new SalesAgent(SalesAgentConfig.DEFAULT);
    }

    public void observe(AgentObservation obs) {
        this.observation = obs;
    }

    public AgentObservation observation() {
        return observation;
    }

    public long interventions() {
        return interventions;
    }

    public Optional<Double> decide() {
        double current = observation.offerPrice();

        if (observation.backlog() > config.backlogHigh()) {
            double newPrice = Math.min(current * (1.0 + config.adjustmentPct()), config.maxPrice());
            if (Math.abs(newPrice - current) > Double.MIN_VALUE) {
                return Optional.of(newPrice);
            }
        } else if (observation.backlog() < config.backlogLow()) {
            double newPrice = Math.max(current * (1.0 - config.adjustmentPct()), config.minPrice());
            if (Math.abs(newPrice - current) > Double.MIN_VALUE) {
                return Optional.of(newPrice);
            }
        }
        return Optional.empty();
    }

    @Override
    public void handleEvent(Event event, Scheduler scheduler) throws SimError {
        switch (event.payload()) {
            case EventPayload.AgentEvaluation ignored -> decide().ifPresent(newPrice -> {
                interventions++;
                scheduler.schedule(Event.of(event.time(), new EventPayload.PriceChange(newPrice)));
                scheduler.schedule(Event.of(
                        event.time(),
                        new EventPayload.AgentDecision(String.format(
                                "SalesAgent: backlog=%d, price %.2f -> %.2f",
                                observation.backlog(),
                                observation.offerPrice(),
                                newPrice))));
            });
            default -> {}
        }
    }
}
