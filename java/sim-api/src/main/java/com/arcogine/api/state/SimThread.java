package com.arcogine.api.state;

import com.arcogine.api.dto.SimSnapshot;
import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.log.EventLog;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.core.scenario.ScenarioLoader;
import com.arcogine.types.MachineId;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import com.arcogine.types.scenario.ScenarioConfig;
import jakarta.annotation.PreDestroy;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@Component
public class SimThread {

    private final LinkedBlockingQueue<SimCommand> commandQueue = new LinkedBlockingQueue<>();
    private final AtomicReference<SimSnapshot> snapshot = new AtomicReference<>(SimSnapshot.empty());
    private final AtomicReference<EventLog> eventLog = new AtomicReference<>(new EventLog());
    private final CopyOnWriteArrayList<Consumer<Event>> eventListeners = new CopyOnWriteArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "sim-thread");
        t.setDaemon(true);
        return t;
    });

    public SimThread() {
        executor.submit(this::simLoop);
    }

    public void sendCommand(SimCommand command) {
        commandQueue.offer(command);
    }

    public SimSnapshot getSnapshot() {
        return snapshot.get();
    }

    public EventLog getEventLog() {
        return eventLog.get();
    }

    public void addEventListener(Consumer<Event> listener) {
        eventListeners.add(listener);
    }

    public void removeEventListener(Consumer<Event> listener) {
        eventListeners.remove(listener);
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }

    private void simLoop() {
        ScenarioConfig config = null;
        IntegratedHandler handler = null;
        Scheduler scheduler = new Scheduler();
        EventLog log = new EventLog();
        SimRunState runState = SimRunState.Idle;
        long eventsProcessed = 0;
        boolean agentEnabled = false;
        String lastError = null;

        while (!Thread.currentThread().isInterrupted()) {
            SimCommand cmd;
            try {
                cmd = commandQueue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            switch (cmd) {
                case SimCommand.LoadScenario load -> {
                    try {
                        ScenarioConfig cfg = ScenarioLoader.loadScenario(load.toml());
                        IntegratedHandler h = HandlerFactory.buildFromConfig(cfg);
                        agentEnabled = h.agentEnabled();

                        scheduler = new Scheduler();
                        log = new EventLog();
                        eventsProcessed = 0;
                        lastError = seedScheduler(scheduler, cfg, agentEnabled);

                        runState = SimRunState.Paused;
                        handler = h;
                        config = cfg;

                        publishSnapshot(handler, log, runState, scheduler, eventsProcessed, config, lastError);
                        load.reply().complete(null);
                    } catch (SimError e) {
                        load.reply().complete(e.getMessage());
                    } catch (RuntimeException e) {
                        load.reply().complete(e.getMessage());
                    }
                }
                case SimCommand.Run run -> {
                    if (handler != null && config != null) {
                        runState = SimRunState.Running;
                        SimTime maxTime = SimTime.of(config.simulation().maxTicks());

                        while (true) {
                            Optional<Event> next = scheduler.nextEvent();
                            if (next.isEmpty()) {
                                break;
                            }
                            Event event = next.get();
                            if (event.time().compareTo(maxTime) > 0) {
                                break;
                            }

                            log.append(event);
                            notifyListeners(event);
                            try {
                                handler.handleEvent(event, scheduler);
                            } catch (SimError e) {
                                lastError = e.getMessage();
                            }
                            eventsProcessed++;

                            lastError = reschedulePeriodic(
                                    event,
                                    scheduler,
                                    maxTime,
                                    config.simulation().demandInterval(),
                                    config.simulation().agentInterval(),
                                    agentEnabled,
                                    lastError);

                            SimCommand pending = commandQueue.poll();
                            if (pending instanceof SimCommand.Pause) {
                                runState = SimRunState.Paused;
                                break;
                            } else if (pending != null) {
                                commandQueue.offer(pending);
                            }
                        }

                        if (runState == SimRunState.Running) {
                            runState = SimRunState.Completed;
                        }

                        publishSnapshot(handler, log, runState, scheduler, eventsProcessed, config, lastError);
                    }
                }
                case SimCommand.Pause pause -> {
                    if (runState == SimRunState.Running) {
                        runState = SimRunState.Paused;
                    }
                }
                case SimCommand.Step step -> {
                    if (handler != null && config != null) {
                        SimTime maxTime = SimTime.of(config.simulation().maxTicks());
                        Optional<Event> next = scheduler.nextEvent();
                        if (next.isPresent()) {
                            Event event = next.get();
                            if (event.time().compareTo(maxTime) <= 0) {
                                log.append(event);
                                notifyListeners(event);
                                try {
                                    handler.handleEvent(event, scheduler);
                                } catch (SimError e) {
                                    lastError = e.getMessage();
                                }
                                eventsProcessed++;

                                lastError = reschedulePeriodic(
                                        event,
                                        scheduler,
                                        maxTime,
                                        config.simulation().demandInterval(),
                                        config.simulation().agentInterval(),
                                        agentEnabled,
                                        lastError);
                            }

                            if (scheduler.isEmpty()
                                    || scheduler.peekTime().map(t -> t.compareTo(maxTime) > 0).orElse(false)) {
                                runState = SimRunState.Completed;
                            } else {
                                runState = SimRunState.Paused;
                            }
                        } else {
                            runState = SimRunState.Completed;
                        }

                        publishSnapshot(handler, log, runState, scheduler, eventsProcessed, config, lastError);
                    }
                }
                case SimCommand.Reset reset -> {
                    if (config != null) {
                        handler = HandlerFactory.buildFromConfig(config);
                        agentEnabled = handler.agentEnabled();
                        scheduler = new Scheduler();
                        log = new EventLog();
                        eventsProcessed = 0;
                        lastError = seedScheduler(scheduler, config, agentEnabled);

                        runState = SimRunState.Paused;
                        publishSnapshot(handler, log, runState, scheduler, eventsProcessed, config, lastError);
                    }
                }
                case SimCommand.ChangePrice change -> {
                    if (handler != null && config != null) {
                        SimTime currentTime = scheduler.currentTime();
                        Event event = Event.of(currentTime, new EventPayload.PriceChange(change.newPrice()));
                        log.append(event);
                        notifyListeners(event);
                        try {
                            handler.handleEvent(event, scheduler);
                        } catch (SimError e) {
                            lastError = e.getMessage();
                        }
                        eventsProcessed++;
                        publishSnapshot(handler, log, runState, scheduler, eventsProcessed, config, lastError);
                    }
                }
                case SimCommand.ChangeMachine change -> {
                    if (handler != null && config != null) {
                        SimTime currentTime = scheduler.currentTime();
                        Event event = Event.of(
                                currentTime,
                                new EventPayload.MachineAvailabilityChange(
                                        new MachineId(change.machineId()), change.online()));
                        log.append(event);
                        notifyListeners(event);
                        try {
                            handler.handleEvent(event, scheduler);
                        } catch (SimError e) {
                            lastError = e.getMessage();
                        }
                        eventsProcessed++;
                        publishSnapshot(handler, log, runState, scheduler, eventsProcessed, config, lastError);
                    }
                }
                case SimCommand.ToggleAgent toggle -> {
                    agentEnabled = toggle.enabled();
                    if (handler != null) {
                        handler.setAgentEnabled(agentEnabled);
                    }
                    if (handler != null && config != null) {
                        publishSnapshot(handler, log, runState, scheduler, eventsProcessed, config, lastError);
                    }
                }
            }
        }
    }

    private void publishSnapshot(
            IntegratedHandler handler,
            EventLog log,
            SimRunState runState,
            Scheduler scheduler,
            long eventsProcessed,
            ScenarioConfig config,
            String lastError) {
        SimSnapshot snap = SnapshotBuilder.buildSnapshot(
                handler, log, runState, scheduler.currentTime(), eventsProcessed, config, lastError);
        snapshot.set(snap);
        eventLog.set(log.snapshot());
    }

    private String seedScheduler(Scheduler scheduler, ScenarioConfig cfg, boolean agentEnabled) {
        String lastError = null;
        long demandInterval = cfg.simulation().demandInterval();
        if (demandInterval > 0) {
            try {
                scheduler.schedule(
                        Event.of(SimTime.of(demandInterval), EventPayload.DemandEvaluation.INSTANCE));
            } catch (SimError e) {
                lastError = e.getMessage();
            }
        }

        long agentInterval = cfg.simulation().agentInterval();
        if (agentInterval > 0 && agentEnabled) {
            try {
                scheduler.schedule(
                        Event.of(SimTime.of(agentInterval), EventPayload.AgentEvaluation.INSTANCE));
            } catch (SimError e) {
                lastError = e.getMessage();
            }
        }
        return lastError;
    }

    private String reschedulePeriodic(
            Event event,
            Scheduler scheduler,
            SimTime maxTime,
            long demandInterval,
            long agentInterval,
            boolean agentEnabled,
            String lastError) {
        switch (event.payload()) {
            case EventPayload.DemandEvaluation ignored -> {
                SimTime nextTime = event.time().plus(demandInterval);
                if (nextTime.compareTo(maxTime) <= 0) {
                    try {
                        scheduler.schedule(Event.of(nextTime, EventPayload.DemandEvaluation.INSTANCE));
                    } catch (SimError e) {
                        return e.getMessage();
                    }
                }
            }
            case EventPayload.AgentEvaluation ignored -> {
                if (agentEnabled) {
                    SimTime nextTime = event.time().plus(agentInterval);
                    if (nextTime.compareTo(maxTime) <= 0) {
                        try {
                            scheduler.schedule(Event.of(nextTime, EventPayload.AgentEvaluation.INSTANCE));
                        } catch (SimError e) {
                            return e.getMessage();
                        }
                    }
                }
            }
            default -> {}
        }
        return lastError;
    }

    private void notifyListeners(Event event) {
        for (Consumer<Event> listener : eventListeners) {
            try {
                listener.accept(event);
            } catch (RuntimeException ignored) {
            }
        }
    }
}
