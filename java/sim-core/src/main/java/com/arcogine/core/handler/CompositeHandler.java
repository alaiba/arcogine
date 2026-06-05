package com.arcogine.core.handler;

import com.arcogine.core.event.Event;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.types.SimError;
import java.util.List;

public class CompositeHandler implements EventHandler {

    private final List<EventHandler> handlers;

    public CompositeHandler(List<EventHandler> handlers) {
        this.handlers = List.copyOf(handlers);
    }

    @Override
    public void handleEvent(Event event, Scheduler scheduler) throws SimError {
        for (EventHandler handler : handlers) {
            handler.handleEvent(event, scheduler);
        }
    }
}
