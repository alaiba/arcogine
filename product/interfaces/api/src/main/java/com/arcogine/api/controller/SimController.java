package com.arcogine.api.controller;

import com.arcogine.api.dto.SimSnapshot;
import com.arcogine.api.dto.TopologySnapshot;
import com.arcogine.api.state.SimCommand;
import com.arcogine.api.state.SimRunState;
import com.arcogine.api.state.SimThread;
import com.arcogine.core.kpi.KpiValue;
import com.arcogine.core.log.EventLog;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimController {

    private static final double MAX_PRICE = 1_000_000.0;

    private final SimThread simThread;

    public SimController(SimThread simThread) {
        this.simThread = simThread;
    }

    @PostMapping("/api/scenario")
    public ResponseEntity<?> loadScenario(@RequestBody LoadScenarioRequest body) {
        CompletableFuture<String> reply = new CompletableFuture<>();
        simThread.sendCommand(new SimCommand.LoadScenario(body.toml(), reply));

        try {
            String error = reply.get(5, TimeUnit.SECONDS);
            if (error != null) {
                return ResponseEntity.badRequest().body(Map.of("error", error));
            }
            return ResponseEntity.ok(new LoadScenarioResponse(true, "Scenario loaded"));
        } catch (TimeoutException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Scenario load timed out"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to send command to simulation thread"));
        }
    }

    @PostMapping("/api/sim/run")
    public ResponseEntity<?> runSim() {
        SimSnapshot snap = simThread.getSnapshot();
        if (!snap.scenarioLoaded()) {
            return conflict("No scenario loaded");
        }
        if (snap.runState() == SimRunState.Completed) {
            return conflict("Simulation already completed; reset first");
        }

        simThread.sendCommand(SimCommand.Run.INSTANCE);
        sleep(100);
        return ResponseEntity.ok(simThread.getSnapshot());
    }

    @PostMapping("/api/sim/pause")
    public ResponseEntity<?> pauseSim() {
        simThread.sendCommand(SimCommand.Pause.INSTANCE);
        sleep(50);
        return ResponseEntity.ok(simThread.getSnapshot());
    }

    @PostMapping("/api/sim/step")
    public ResponseEntity<?> stepSim() {
        SimSnapshot snap = simThread.getSnapshot();
        if (!snap.scenarioLoaded()) {
            return conflict("No scenario loaded");
        }
        if (snap.runState() == SimRunState.Completed) {
            return conflict("Simulation already completed; reset first");
        }

        simThread.sendCommand(SimCommand.Step.INSTANCE);
        sleep(50);
        return ResponseEntity.ok(simThread.getSnapshot());
    }

    @PostMapping("/api/sim/reset")
    public ResponseEntity<?> resetSim() {
        SimSnapshot snap = simThread.getSnapshot();
        if (!snap.scenarioLoaded()) {
            return conflict("No scenario loaded; load a scenario first");
        }

        simThread.sendCommand(SimCommand.Reset.INSTANCE);
        sleep(50);
        return ResponseEntity.ok(simThread.getSnapshot());
    }

    @PostMapping("/api/price")
    public ResponseEntity<?> changePrice(@RequestBody ChangePriceRequest body) {
        if (body.price() < 0.0 || body.price() > MAX_PRICE) {
            return ResponseEntity.badRequest().body(Map.of("error", "Price must be between 0 and 1,000,000"));
        }

        SimSnapshot snap = simThread.getSnapshot();
        if (!snap.scenarioLoaded()) {
            return conflict("No scenario loaded");
        }

        simThread.sendCommand(new SimCommand.ChangePrice(body.price()));
        sleep(50);
        return ResponseEntity.ok(simThread.getSnapshot());
    }

    @PostMapping("/api/machines")
    public ResponseEntity<?> changeMachine(@RequestBody ChangeMachineRequest body) {
        SimSnapshot snap = simThread.getSnapshot();
        if (!snap.scenarioLoaded()) {
            return conflict("No scenario loaded");
        }

        simThread.sendCommand(new SimCommand.ChangeMachine(body.machineId(), body.online()));
        sleep(50);
        return ResponseEntity.ok(simThread.getSnapshot());
    }

    @PostMapping("/api/agent")
    public ResponseEntity<?> toggleAgent(@RequestBody ToggleAgentRequest body) {
        simThread.sendCommand(new SimCommand.ToggleAgent(body.enabled()));
        sleep(50);
        return ResponseEntity.ok(simThread.getSnapshot());
    }

    @GetMapping("/api/kpis")
    public List<KpiValue> queryKpis() {
        return simThread.getSnapshot().kpis();
    }

    @GetMapping("/api/snapshot")
    public SimSnapshot querySnapshot() {
        return simThread.getSnapshot();
    }

    @GetMapping("/api/factory/topology")
    public TopologySnapshot queryTopology() {
        return simThread.getSnapshot().topology();
    }

    @GetMapping("/api/jobs")
    public List<com.arcogine.api.dto.JobInfo> queryJobs() {
        return simThread.getSnapshot().jobs();
    }

    @GetMapping("/api/export/events")
    public EventLog exportEvents() {
        return simThread.getEventLog();
    }

    private static ResponseEntity<Map<String, String>> conflict(String message) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", message));
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public record LoadScenarioRequest(String toml) {}

    public record LoadScenarioResponse(boolean success, String message) {}

    public record ChangePriceRequest(double price) {}

    public record ChangeMachineRequest(@JsonProperty("machine_id") long machineId, boolean online) {}

    public record ToggleAgentRequest(boolean enabled) {}
}
