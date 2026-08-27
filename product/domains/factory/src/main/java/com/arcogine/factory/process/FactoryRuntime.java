package com.arcogine.factory.process;

import com.arcogine.core.event.Event;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.factory.jobs.JobView;
import com.arcogine.factory.machines.MachineView;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.model.FactoryRuntimeAssembler;
import com.arcogine.factory.orders.Order;
import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimError;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * The supported, consumer-neutral entry point for submitting explicit production workload,
 * independent of the economy/pricing/demand/agent loop.
 *
 * <p>{@link FactoryHandler#submitOrder} takes a caller-supplied {@link Scheduler} and simulation
 * time -- necessary plumbing for the event-driven path, but not something a workload-submission
 * caller should have to own or get right. {@link FactoryRuntime} owns that scheduler/time context
 * internally instead, so a caller only ever supplies commercial/product intent.
 *
 * <p>A {@link FactoryRuntime} always owns the exclusive {@link FactoryHandler}/{@link Scheduler}
 * pair it advances: it is only constructed from a published {@link FactoryModelVersion} via {@link
 * #forModel}, never wrapped around an already-live {@code FactoryHandler} that some other scheduler
 * might also be driving. Without that, two schedulers could race to mutate the same factory state
 * and event ordering would stop being globally authoritative. For the same reason, the mutable
 * {@code FactoryHandler} is not exposed directly; observation happens through this type's own
 * read-only projections.
 *
 * <p>This is deliberately narrow: it does not attempt a general simulation-session API (bounded
 * advancement, structured accept/reject results, reset) -- that is later Gate 3 work. {@link
 * #advance()} exposes only the one-event-at-a-time pump a headless caller needs to drive submitted
 * workload through to completion.
 */
public class FactoryRuntime {

    private final FactoryHandler factory;
    private final Scheduler scheduler;

    private FactoryRuntime(FactoryHandler factory) {
        this.factory = factory;
        this.scheduler = new Scheduler();
    }

    /**
     * Assembles a fresh factory runtime from a published model and returns a {@link FactoryRuntime}
     * that exclusively owns it, so its explicit-workload scheduler is the only authority over the
     * factory's event ordering.
     */
    public static FactoryRuntime forModel(FactoryModelVersion version) {
        FactoryRuntimeAssembler.Assembled assembled = FactoryRuntimeAssembler.assemble(version);
        return new FactoryRuntime(assembled.factory());
    }

    /**
     * Submits one explicit production order and creates its execution job, under the same
     * acceptance/routing/dispatch semantics as any other accepted order.
     */
    public OrderId submitWorkload(ProductId productId, long quantity, double unitPrice) {
        return factory.submitOrder(productId, quantity, unitPrice, scheduler.currentTime(), scheduler);
    }

    /**
     * Brings a machine online or takes it offline, under the same dispatch semantics as the
     * economy/scenario-driven {@link com.arcogine.core.event.EventPayload.MachineAvailabilityChange}
     * event. Taking a machine offline never affects work already active on it; bringing an eligible
     * machine back online can immediately pick up work that was waiting because no other eligible
     * machine was available.
     */
    public void setMachineAvailability(MachineId machineId, boolean online) {
        factory.handleMachineAvailability(machineId, online, scheduler, scheduler.currentTime());
    }

    /** Processes exactly one pending event, if any, and returns it. */
    public Optional<Event> advance() throws SimError {
        Optional<Event> next = scheduler.nextEvent();
        if (next.isPresent()) {
            factory.handleEvent(next.get(), scheduler);
        }
        return next;
    }

    /** Read-only view of every accepted order. */
    public Stream<Order> ordersView() {
        return factory.ordersView();
    }

    /** Read-only view of every execution job. */
    public Stream<JobView> jobsView() {
        return factory.jobsView();
    }

    /** Read-only lookup for a single job. */
    public JobView job(JobId id) {
        return factory.job(id);
    }

    /** Read-only view of every machine. */
    public List<MachineView> machinesView() {
        return factory.machinesView();
    }

    public long backlog() {
        return factory.backlog();
    }

    public double avgLeadTime() {
        return factory.avgLeadTime();
    }

    public double throughput(long elapsedTicks) {
        return factory.throughput(elapsedTicks);
    }

    public double completedSalesValue() {
        return factory.completedSalesValue();
    }

    public long completedSales() {
        return factory.completedSales();
    }
}
