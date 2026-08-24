package com.arcogine.factory.routing;

import java.util.List;
import java.util.Optional;

public class Routing {

    private final long id;
    private final String name;
    private final List<RoutingStep> steps;

    public Routing(long id, String name, List<RoutingStep> steps) {
        this.id = id;
        this.name = name;
        this.steps = List.copyOf(steps);
    }

    public Optional<RoutingStep> getStep(int index) {
        if (index < 0 || index >= steps.size()) {
            return Optional.empty();
        }
        return Optional.of(steps.get(index));
    }

    public int stepCount() {
        return steps.size();
    }

    public long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public List<RoutingStep> steps() {
        return steps;
    }
}
