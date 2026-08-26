package com.arcogine.factory.process;

import com.arcogine.core.event.Event;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimError;
import java.util.Optional;

/**
 * The supported, consumer-neutral entry point for submitting explicit production workload to a
 * {@link FactoryHandler}, independent of the economy/pricing/demand/agent loop.
 *
 * <p>{@link FactoryHandler#submitOrder} takes a caller-supplied {@link Scheduler} and simulation
 * time -- necessary plumbing for the event-driven path, but not something a workload-submission
 * caller should have to own or get right. {@link FactoryRuntime} owns that scheduler/time context
 * internally instead, so a caller only ever supplies commercial/product intent.
 *
 * <p>This is deliberately narrow: it does not attempt a general simulation-session API (bounded
 * advancement, structured accept/reject results, reset) -- that is later Gate 3 work. {@link
 * #advance()} exposes only the one-event-at-a-time pump a headless caller needs to drive submitted
 * workload through to completion.
 */
public class FactoryRuntime {

    private final FactoryHandler factory;
    private final Scheduler scheduler;

    public FactoryRuntime(FactoryHandler factory) {
        this.factory = factory;
        this.scheduler = new Scheduler();
    }

    /**
     * Submits one explicit production order and creates its execution job, under the same
     * acceptance/routing/dispatch semantics as any other accepted order.
     */
    public OrderId submitWorkload(ProductId productId, long quantity, double unitPrice) {
        return factory.submitOrder(productId, quantity, unitPrice, scheduler.currentTime(), scheduler);
    }

    /** Processes exactly one pending event, if any, and returns it. */
    public Optional<Event> advance() throws SimError {
        Optional<Event> next = scheduler.nextEvent();
        if (next.isPresent()) {
            factory.handleEvent(next.get(), scheduler);
        }
        return next;
    }

    /** The underlying factory runtime this instance submits workload to and advances. */
    public FactoryHandler factory() {
        return factory;
    }
}
