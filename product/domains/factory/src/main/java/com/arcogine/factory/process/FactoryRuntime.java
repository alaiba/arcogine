package com.arcogine.factory.process;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
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
import com.arcogine.types.SimTime;
import java.util.ArrayList;
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
 * <p>This class implements Gate 3 (docs/planning/factory-simulation-engine-readiness.md §7):
 * consumer-neutral session/control semantics layered onto the exclusive {@link FactoryHandler}/
 * {@link Scheduler} pair from Gate 1/2. {@link #modelVersion()} identifies the published model the
 * session was instantiated from throughout its lifetime; {@link #advance()} and {@link
 * #advanceUntil} give a caller both one-event-at-a-time and bounded-simulated-time control; {@link
 * #reset()} gives a caller a fresh session over the same published model; {@link #submitWorkload}
 * and {@link #setMachineAvailability} return a definite {@link CommandResult} rather than throwing
 * or returning {@code void}; {@link #pendingWorkView()} exposes the cross-machine multi-eligible
 * backlog that no single machine's queue depth reflects. It deliberately does not attempt a generic
 * simulation-session framework, event envelopes/cursors, or a new session-identity type -- those
 * remain out of scope for this slice or belong to later gates (see the plan).
 */
public class FactoryRuntime {

    private final FactoryHandler factory;
    private final RecordingScheduler scheduler;
    private final FactoryModelVersion modelVersion;

    private FactoryRuntime(FactoryHandler factory, FactoryModelVersion modelVersion) {
        this.factory = factory;
        this.scheduler = new RecordingScheduler();
        this.modelVersion = modelVersion;
    }

    /**
     * Assembles a fresh factory runtime from a published model and returns a {@link FactoryRuntime}
     * that exclusively owns it, so its explicit-workload scheduler is the only authority over the
     * factory's event ordering.
     */
    public static FactoryRuntime forModel(FactoryModelVersion version) {
        FactoryRuntimeAssembler.Assembled assembled = FactoryRuntimeAssembler.assemble(version);
        return new FactoryRuntime(assembled.factory(), version);
    }

    /**
     * The published model version this session was instantiated from, retained for the lifetime of
     * the session so a caller can identify its source model without tracking it separately (Gate 3
     * acceptance criterion 7).
     */
    public FactoryModelVersion modelVersion() {
        return modelVersion;
    }

    /**
     * Constructs a fresh {@link FactoryRuntime} over the same {@link #modelVersion()}, with none of
     * this session's submitted workload or dispatch state carried over (Gate 3 acceptance criterion
     * 6: reset and reproduce the same result). This session itself is left untouched -- reset is
     * fresh construction, not in-place mutation, matching {@code FactoryRuntime}'s existing
     * immutable-identity/exclusive-ownership shape: replaying the same command sequence against the
     * returned session reproduces the same result as replaying it against any other fresh session
     * built from this same model version.
     */
    public FactoryRuntime reset() {
        return forModel(modelVersion);
    }

    /**
     * Submits one explicit production order and creates its execution job, under the same
     * acceptance/routing/dispatch semantics as any other accepted order, and returns a definite
     * {@link CommandResult} per docs/planning/factory-simulation-engine-readiness.md §7.2.
     *
     * <p>On acceptance, {@link CommandResult.Accepted#value()} is the new {@link OrderId}. On
     * rejection (e.g. {@link SimError.OutOfRange} for an invalid quantity, {@link
     * SimError.UnknownId} for a product with no published routing), {@link
     * CommandResult.Rejected#error()} is the original {@link SimError} -- a rejected call never
     * leaves partial mutation, since every current rejection path is checked before any {@code
     * Order}/{@code Job} is created.
     */
    public CommandResult<OrderId> submitWorkload(ProductId productId, long quantity, double unitPrice) {
        List<Event> scheduled = new ArrayList<>();
        scheduler.startCapturing(scheduled);
        try {
            OrderId orderId =
                    factory.submitOrder(productId, quantity, unitPrice, scheduler.currentTime(), scheduler);
            return new CommandResult.Accepted<>(orderId, modelVersion, scheduled);
        } catch (SimError e) {
            return new CommandResult.Rejected<>(e, modelVersion);
        } finally {
            scheduler.stopCapturing();
        }
    }

    /**
     * Brings a machine online or takes it offline, under the same dispatch semantics as the
     * economy/scenario-driven {@link EventPayload.MachineAvailabilityChange} event, and returns a
     * definite {@link CommandResult}. Taking a machine offline never affects work already active on
     * it; bringing an eligible machine back online can immediately pick up work that was waiting
     * because no other eligible machine was available -- any such immediately-dispatched {@code
     * TaskStart}/{@code TaskEnd} events are included in {@link CommandResult#scheduledEvents()}.
     *
     * <p>Both rejection paths this method can produce ({@link SimError.UnknownId} for an unknown
     * {@code machineId}; {@link SimError.InvalidStateTransition} for taking a machine with active
     * jobs offline) are verified from this method's own read-only {@link #machinesView()} before
     * calling into {@link FactoryHandler}, so a returned {@link CommandResult.Rejected} is always
     * genuinely pre-mutation. A machine coming online can trigger a cascade of dispatching
     * previously waiting work; a failure surfacing from deep in that cascade (a genuine engine
     * fault, not a rejectable input this method could have pre-verified) is reported as {@link
     * CommandResult.Faulted} rather than {@link CommandResult.Rejected} -- the requested {@link
     * EventPayload.MachineAvailabilityChange} genuinely was applied by that point (both pre-checks
     * above having passed guarantees {@code Machine#setAvailability} itself cannot fail), so {@link
     * CommandResult.Faulted#value()} still carries it, unlike a rejection, which never has one.
     * This method never lets such a failure propagate past its own boundary as a bare exception: it
     * always returns a definite {@link CommandResult}, per
     * docs/planning/factory-simulation-engine-readiness.md §7.2.
     */
    public CommandResult<EventPayload.MachineAvailabilityChange> setMachineAvailability(
            MachineId machineId, boolean online) {
        Optional<MachineView> machine =
                machinesView().stream().filter(m -> m.id().equals(machineId)).findFirst();
        if (machine.isEmpty()) {
            return new CommandResult.Rejected<>(
                    new SimError.UnknownId("machine", machineId.value()), modelVersion);
        }
        if (!online && !machine.get().activeJobs().isEmpty()) {
            return new CommandResult.Rejected<>(
                    new SimError.InvalidStateTransition("cannot take machine " + machineId + " offline while "
                            + machine.get().activeJobs().size() + " jobs are active"),
                    modelVersion);
        }

        List<Event> scheduled = new ArrayList<>();
        scheduler.startCapturing(scheduled);
        EventPayload.MachineAvailabilityChange requested = new EventPayload.MachineAvailabilityChange(machineId, online);
        try {
            factory.handleMachineAvailability(machineId, online, scheduler, scheduler.currentTime());
            return new CommandResult.Accepted<>(requested, modelVersion, scheduled);
        } catch (SimError e) {
            // The availability change itself (Machine#setAvailability) is the first thing
            // handleMachineAvailability does and always succeeds once the pre-checks above have
            // passed -- only the subsequent dispatch cascade can fault. So `requested` genuinely
            // was applied by the time any SimError reaches here, and belongs on Faulted too.
            return new CommandResult.Faulted<>(requested, e, modelVersion, scheduled);
        } finally {
            scheduler.stopCapturing();
        }
    }

    /**
     * Read-only view of work waiting for one of several eligible machines to free up (Gate 2's
     * cross-machine multi-eligible backlog) -- not reflected in any single machine's {@link
     * MachineView#queueDepth()}. See {@link PendingWorkView} for why this is a necessary, separate
     * projection (Gate 3 acceptance criterion 5).
     */
    public List<PendingWorkView> pendingWorkView() {
        return factory.pendingWorkView();
    }

    /** Processes exactly one pending event, if any, and returns it. */
    public Optional<Event> advance() throws SimError {
        Optional<Event> next = scheduler.nextEvent();
        if (next.isPresent()) {
            factory.handleEvent(next.get(), scheduler);
        }
        return next;
    }

    /**
     * Processes pending events one at a time, in the same order {@link #advance()} would, stopping
     * as soon as either bound is reached: the next pending event's {@link SimTime} would exceed
     * {@code targetTime}, or {@code maxEvents} events have already been processed by this call.
     * Returns every event actually processed, in processing order (empty if none were).
     *
     * <p>This is the bounded-advancement primitive Gate 3 requires (acceptance criterion 4):
     * interactive consumers use it for pause/resume and normal/accelerated presentation speeds
     * without wall-clock sleeping in the simulation core, and headless consumers use it as
     * protection against unbounded work monopolizing a call. It is provably equivalent to looping
     * {@link #advance()} one event at a time under the same two stopping conditions -- it does not
     * reorder, skip, merge, or otherwise reinterpret events -- so a caller can freely mix the two
     * without affecting determinism; {@code Gate3SessionControlAcceptanceTest} proves the two
     * approaches converge to identical ordered event streams and identical terminal state for the
     * same model/workload.
     *
     * @param targetTime the simulated time not to advance past; an event scheduled after this time
     *     is left pending rather than processed
     * @param maxEvents the maximum number of events this call may process; must not be negative
     */
    public List<Event> advanceUntil(SimTime targetTime, long maxEvents) throws SimError {
        if (maxEvents < 0) {
            throw new IllegalArgumentException("maxEvents must not be negative, got " + maxEvents);
        }
        List<Event> processed = new ArrayList<>();
        while (processed.size() < maxEvents) {
            Optional<SimTime> nextTime = scheduler.peekTime();
            if (nextTime.isEmpty() || nextTime.get().compareTo(targetTime) > 0) {
                break;
            }
            advance().ifPresent(processed::add);
        }
        return processed;
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
