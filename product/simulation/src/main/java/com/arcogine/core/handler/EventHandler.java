package com.arcogine.core.handler;

import com.arcogine.core.event.Event;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.types.SimError;

@FunctionalInterface
public interface EventHandler {
    void handleEvent(Event event, Scheduler scheduler) throws SimError;
}
