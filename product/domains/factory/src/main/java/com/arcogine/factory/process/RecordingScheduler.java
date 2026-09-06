package com.arcogine.factory.process;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.queue.Scheduler;
import java.util.List;
import java.util.Optional;

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
    private int pendingAuthoritativeEvents;

    @Override
    public void schedule(Event event) {
        super.schedule(event);
        if (changesAuthoritativeState(event)) {
            pendingAuthoritativeEvents++;
        }
        if (capture != null) {
            capture.add(event);
        }
    }

    @Override
    public Optional<Event> nextEvent() {
        Optional<Event> next = super.nextEvent();
        if (next.isPresent() && changesAuthoritativeState(next.get())) {
            pendingAuthoritativeEvents--;
        }
        return next;
    }

    /**
     * Whether any queued event can still authoritatively change factory state -- the sense in which
     * {@link RuntimeRunState#ACTIVE} means "pending authoritative work" (ADR-0011).
     *
     * <p>Deliberately not {@link #isEmpty()}: the queue can still hold internal markers ({@code
     * TaskStart}, the {@code OrderCompleted} a terminal {@code TaskEnd} schedules purely so other
     * internal handlers can observe completion) that {@link FactoryHandler#handleEvent} ignores.
     * Processing one of those changes nothing a consumer can observe and emits no supported event,
     * so reporting {@code ACTIVE} merely because one is still queued would let two observations at
     * the same {@code latestEventSequence} disagree.
     */
    boolean hasPendingAuthoritativeWork() {
        return pendingAuthoritativeEvents > 0;
    }

    /**
     * Whether processing {@code event} can authoritatively change factory state -- exactly the
     * payloads {@link FactoryHandler#handleEvent} acts on; every other payload falls through its
     * {@code default} branch as a no-op marker.
     */
    static boolean changesAuthoritativeState(Event event) {
        return switch (event.payload()) {
            case EventPayload.OrderCreation ignored -> true;
            case EventPayload.TaskEnd ignored -> true;
            case EventPayload.MachineAvailabilityChange ignored -> true;
            default -> false;
        };
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
