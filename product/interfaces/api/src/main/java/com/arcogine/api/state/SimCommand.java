package com.arcogine.api.state;

import java.util.concurrent.CompletableFuture;

public sealed interface SimCommand permits
        SimCommand.LoadScenario,
        SimCommand.Run,
        SimCommand.Pause,
        SimCommand.Step,
        SimCommand.Reset,
        SimCommand.ChangePrice,
        SimCommand.ChangeMachine,
        SimCommand.ToggleAgent {

    record LoadScenario(String toml, CompletableFuture<String> reply) implements SimCommand {}

    record Run() implements SimCommand {
        public static final Run INSTANCE = new Run();
    }

    record Pause() implements SimCommand {
        public static final Pause INSTANCE = new Pause();
    }

    record Step() implements SimCommand {
        public static final Step INSTANCE = new Step();
    }

    record Reset() implements SimCommand {
        public static final Reset INSTANCE = new Reset();
    }

    record ChangePrice(double newPrice) implements SimCommand {}

    record ChangeMachine(long machineId, boolean online) implements SimCommand {}

    record ToggleAgent(boolean enabled) implements SimCommand {}
}
