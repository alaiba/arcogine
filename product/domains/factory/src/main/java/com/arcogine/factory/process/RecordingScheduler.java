package com.arcogine.factory.process;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.queue.Scheduler;
import java.util.List;

/**
 * {@link FactoryRuntime}'s own {@link Scheduler}: it admits only the event kind a Factory session
 * schedules, and can, for the duration of one command call, additionally capture every {@link
 * Event} it schedules into a caller-supplied sink, so {@link FactoryRuntime} can report exactly
 * which events a specific command scheduled (the command-scoped event list required by {@link
 * CommandResult} and {@code docs/architecture/engine-semantics.md} §1.2) without changing
 * {@link Scheduler}'s own public contract or touching any other consumer of it.
 *
 * <p>Admission is the session's scheduling rule ({@code docs/architecture/engine-semantics.md}
 * §4): a Factory session schedules only step completions ({@link EventPayload.TaskEnd}), each of
 * which authoritatively completes a step and is published at its own time. Bounded advancement
 * counts processed scheduler events and a command applies at the scheduler's time, so a queued
 * event that changed nothing -- a marker -- would still consume an advancement budget and move the
 * time the next command applies, invisibly to every supported observation. Any other payload is
 * therefore refused rather than silently queued; scheduling a new event kind is a deliberate
 * Engine-definition change, not something this class may absorb.
 *
 * <p>Capture is a scoped window, not a permanent history: {@link #startCapturing(List)} begins
 * appending every subsequently scheduled event to the given list, and {@link #stopCapturing()}
 * turns that off again. Nothing is retained by this class itself once a window closes -- unlike an
 * always-append history, this cannot grow unboundedly over a long-lived {@link FactoryRuntime}
 * session merely because ordinary {@link FactoryRuntime#advance()}/{@link
 * FactoryRuntime#advanceUntil} processing (dispatch, queue drains, ...) keeps scheduling further
 * events outside any capture window.
 *
 * <p>Package-private: this is {@link FactoryRuntime}'s own internal instrumentation, not a shape
 * any external caller should construct or depend on.
 */
final class RecordingScheduler extends Scheduler {

    private List<Event> capture;

    /**
     * Schedules a step completion.
     *
     * @throws IllegalArgumentException if {@code event} is not a step completion; nothing is
     *     scheduled or captured
     */
    @Override
    public void schedule(Event event) {
        if (!(event.payload() instanceof EventPayload.TaskEnd)) {
            throw new IllegalArgumentException(
                    "a Factory session schedules only step completions, not " + event.payload());
        }
        super.schedule(event);
        if (capture != null) {
            capture.add(event);
        }
    }

    /**
     * Whether any queued event can still authoritatively change factory state -- the sense in which
     * {@link RuntimeRunState#ACTIVE} means "pending authoritative work"
     * (docs/architecture/runtime-contract.md). Every queued event is an admitted step completion, so
     * this is exactly a non-empty queue.
     */
    boolean hasPendingAuthoritativeWork() {
        return !isEmpty();
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
