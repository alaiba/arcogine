package com.arcogine.core.runner;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.handler.EventHandler;
import com.arcogine.core.log.EventLog;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import com.arcogine.types.scenario.AgentConfig;
import com.arcogine.types.scenario.ScenarioConfig;

public final class SimRunner {

    private SimRunner() {}

    public static SimResult runScenario(ScenarioConfig config, EventHandler handler) throws SimError {
        Scheduler scheduler = new Scheduler();
        EventLog eventLog = new EventLog();
        SimTime maxTime = SimTime.of(config.simulation().maxTicks());

        long demandInterval = config.simulation().demandInterval();
        if (demandInterval > 0) {
            scheduler.schedule(Event.of(SimTime.of(demandInterval), EventPayload.DemandEvaluation.INSTANCE));
        }

        long agentInterval = config.simulation().agentInterval();
        AgentConfig agent = config.agent();
        if (agentInterval > 0 && agent != null && agent.enabled()) {
            scheduler.schedule(Event.of(SimTime.of(agentInterval), EventPayload.AgentEvaluation.INSTANCE));
        }

        long eventsProcessed = 0;

        while (true) {
            Event event = scheduler.nextEvent().orElse(null);
            if (event == null) {
                break;
            }
            if (event.time().compareTo(maxTime) > 0) {
                break;
            }

            eventLog.append(event);
            handler.handleEvent(event, scheduler);
            eventsProcessed++;

            switch (event.payload()) {
                case EventPayload.DemandEvaluation ignored -> {
                    SimTime nextTime = event.time().plus(demandInterval);
                    if (nextTime.compareTo(maxTime) <= 0) {
                        scheduler.schedule(Event.of(nextTime, EventPayload.DemandEvaluation.INSTANCE));
                    }
                }
                case EventPayload.AgentEvaluation ignored -> {
                    SimTime nextTime = event.time().plus(agentInterval);
                    if (nextTime.compareTo(maxTime) <= 0) {
                        scheduler.schedule(Event.of(nextTime, EventPayload.AgentEvaluation.INSTANCE));
                    }
                }
                default -> {}
            }
        }

        return new SimResult(scheduler.currentTime(), eventLog, eventsProcessed);
    }
}
