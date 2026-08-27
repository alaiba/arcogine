package com.arcogine.factory.process;

import com.arcogine.core.event.Event;
import com.arcogine.core.queue.Scheduler;
import java.util.List;

/**
 * A {@link Scheduler} that can, for the duration of one command call, additionally capture every
 * {@link Event} it schedules into a caller-supplied sink, so {@link FactoryRuntime} can report
 * exactly which events a specific command scheduled (docs/planning/factory-simulation-engine-readiness.md
 * §7.2's "events produced by the accepted command" field on {@link CommandResult}) without changing
 * {@link Scheduler}'s own public contract or touching any other consumer of it.
 *
 * <p>Capture is a scoped window, not a permanent history: {@link #startCapturing(List)} begins
 * appending every subsequently scheduled event to the given list, and {@link #stopCapturing()}
 * turns that off again. Nothing is retained by this class itself once a window closes -- unlike an
 * always-append history, this cannot grow unboundedly over a long-lived {@link FactoryRuntime}
 * session merely because ordinary {@link FactoryRuntime#advance()}/{@link
 * FactoryRuntime#advanceUntil} processing (dispatch, queue drains, order completion, ...) keeps
 * scheduling further events outside any capture window.
 *
 * <p>Package-private: this is {@link FactoryRuntime}'s own internal instrumentation, not a shape
 * any external caller should construct or depend on.
 */
final class RecordingScheduler extends Scheduler {

    private List<Event> capture;

    @Override
    public void schedule(Event event) {
        super.schedule(event);
        if (capture != null) {
            capture.add(event);
        }
    }

    /** Begins appending every subsequently scheduled event to {@code sink}, until {@link #stopCapturing()}. */
    void startCapturing(List<Event> sink) {
        this.capture = sink;
    }

    /** Stops appending scheduled events anywhere; this scheduler retains nothing from the closed window. */
    void stopCapturing() {
        this.capture = null;
    }
}
