package com.arcogine.factory.process;

import com.arcogine.core.event.Event;
import com.arcogine.core.queue.Scheduler;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Scheduler} that additionally remembers every {@link Event} it has ever scheduled, in
 * scheduling order, so {@link FactoryRuntime} can report exactly which events a specific command
 * call scheduled (docs/planning/factory-simulation-engine-readiness.md §7.2's "events produced by
 * the accepted command" field on {@link CommandResult}) without changing {@link Scheduler}'s own
 * public contract or touching any other consumer of it. {@link #scheduledSince(int)} together with
 * {@link #scheduledCount()} gives a caller a mark/diff pair: record the count before a command,
 * call the command, then read what was scheduled since that mark.
 *
 * <p>Package-private: this is {@link FactoryRuntime}'s own internal instrumentation, not a shape
 * any external caller should construct or depend on.
 */
final class RecordingScheduler extends Scheduler {

    private final List<Event> scheduled = new ArrayList<>();

    @Override
    public void schedule(Event event) {
        super.schedule(event);
        scheduled.add(event);
    }

    int scheduledCount() {
        return scheduled.size();
    }

    List<Event> scheduledSince(int markIndex) {
        return List.copyOf(scheduled.subList(markIndex, scheduled.size()));
    }
}
