package com.arcogine.factory.process;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.handler.EventHandler;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.factory.jobs.Job;
import com.arcogine.factory.jobs.JobStore;
import com.arcogine.factory.jobs.JobView;
import com.arcogine.factory.machines.Machine;
import com.arcogine.factory.machines.MachineStore;
import com.arcogine.factory.machines.MachineView;
import com.arcogine.factory.orders.Order;
import com.arcogine.factory.orders.OrderStore;
import com.arcogine.factory.orders.OrderExecutionView;
import com.arcogine.factory.routing.Routing;
import com.arcogine.factory.routing.RoutingStep;
import com.arcogine.factory.routing.RoutingStore;
import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.MachineState;
import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class FactoryHandler implements EventHandler {

    final MachineStore machines;
    final OrderStore orders;
    final JobStore jobs;
    public final RoutingStore routings;
    public final List<ProductId> productIds;
    private double completedSalesValue;
    private long completedSales;

    /**
     * Work waiting for one of more than one eligible machine to free up. Unlike {@link
     * Machine#enqueueJob}, an entry here is not pinned to any single machine, so it is
     * reconsidered against its whole eligible set -- not just the machine that most recently
     * changed state -- every time any machine frees up or comes online. This is what lets a
     * machine that was offline when the job first waited still pick it up once it recovers,
     * instead of the job staying stranded on whichever specific machine happened to be selected
     * at enqueue time. A step with exactly one eligible machine keeps using {@link
     * Machine}'s own per-machine queue unchanged.
     */
    private final Deque<PendingDispatch> pendingMultiEligible = new ArrayDeque<>();

    private record PendingDispatch(
            JobId jobId, Set<MachineId> eligibleMachines, int routingIndex, long duration) {}

    public FactoryHandler(MachineStore machines, RoutingStore routings, List<ProductId> productIds) {
        this.machines = machines;
        this.orders = new OrderStore();
        this.jobs = new JobStore();
        this.routings = routings;
        this.productIds = List.copyOf(productIds);
        this.completedSalesValue = 0.0;
        this.completedSales = 0;
    }

    @Override
    public void handleEvent(Event event, Scheduler scheduler) throws SimError {
        switch (event.payload()) {
            case EventPayload.OrderCreation oc ->
                    submitOrder(oc.productId(), oc.quantity(), oc.unitPrice(), event.time(), scheduler);
            case EventPayload.TaskEnd te ->
                    handleTaskEnd(te.jobId(), te.machineId(), te.stepIndex(), scheduler, event.time());
            case EventPayload.MachineAvailabilityChange mac ->
                    handleMachineAvailability(mac.machineId(), mac.online(), scheduler, event.time());
            default -> {}
        }
    }

    public double completedSalesValue() {
        return completedSalesValue;
    }

    public long completedSales() {
        return completedSales;
    }

    /** Immutable lookup of accepted order intent. */
    public Order order(OrderId id) {
        return orders.get(id);
    }

    /** Immutable view of all accepted orders. */
    public Stream<Order> ordersView() {
        return orders.allOrders();
    }

    public OrderExecutionView orderExecution(OrderId id) { return orders.execution(id); }

    public Stream<OrderExecutionView> orderExecutionsView() { return orders.allOrders().map(order -> orders.execution(order.id())); }

    /**
     * Read-only lookup for a single job -- deliberately returns {@link JobView}, not {@link Job},
     * so external callers can't reach {@code start}/{@code completeStep} and bypass event-driven
     * mutation.
     */
    public JobView job(JobId id) {
        return jobs.get(id);
    }

    /** Read-only view of every job -- see {@link #job(JobId)}. */
    public Stream<JobView> jobsView() {
        return jobs.allJobs().map(JobView.class::cast);
    }

    /** Read-only view of every machine -- excludes machine mutators for the same reason as {@link #job(JobId)}. */
    public List<MachineView> machinesView() {
        return machines.machines().stream().map(MachineView.class::cast).toList();
    }

    /**
     * Read-only snapshot of {@link #pendingMultiEligible}: work waiting for any one of several
     * eligible machines to free up, not reflected in any single {@link Machine}'s queue depth. See
     * {@link PendingWorkView} for why this is a separate, necessary projection from {@link
     * #machinesView()}.
     */
    public List<PendingWorkView> pendingWorkView() {
        return pendingMultiEligible.stream()
                .map(p -> new PendingWorkView(p.jobId(), p.eligibleMachines()))
                .toList();
    }

    public long backlog() {
        return orders.allOrders().map(order -> orders.execution(order.id())).filter(view -> !view.complete()).count();
    }

    public double avgLeadTime() {
        List<OrderExecutionView> completed = orderExecutionsView().filter(OrderExecutionView::complete).toList();
        if (completed.isEmpty()) {
            return 0.0;
        }
        long total = completed.stream()
                .mapToLong(view -> view.completedAt().minus(orders.get(view.orderId()).createdAt()))
                .sum();
        return (double) total / completed.size();
    }

    public double throughput(long elapsedTicks) {
        if (elapsedTicks == 0) {
            return 0.0;
        }
        return (double) completedSales / elapsedTicks;
    }

    /**
     * Deterministically picks one machine from a step's eligible set to run the next unit of
     * work. Policy: prefer an online machine that can accept work immediately; among ties, prefer
     * the shallowest queue; a final tie (including "no eligible machine is online") breaks on the
     * lowest {@link MachineId}, so identical inputs always resolve the same way.
     */
    private MachineId selectMachine(Set<MachineId> eligibleMachines) {
        List<MachineId> online = eligibleMachines.stream()
                .filter(id -> machines.get(id).state() != MachineState.Offline)
                .toList();
        List<MachineId> candidates = online.isEmpty() ? List.copyOf(eligibleMachines) : online;

        return candidates.stream()
                .min(Comparator
                        .<MachineId>comparingInt(id -> machines.get(id).canAcceptJob() ? 0 : 1)
                        .thenComparingInt(this::combinedQueueDepth)
                        .thenComparing(MachineId::value))
                .orElseThrow();
    }

    /**
     * A machine's own physical queue depth plus how many {@link #pendingMultiEligible} entries
     * could also land on it, so the "shallowest queue" tie-break accounts for shared multi-machine
     * backlog, not just work already pinned to this one machine.
     */
    private int combinedQueueDepth(MachineId id) {
        return machines.get(id).queueDepth()
                + (int) pendingMultiEligible.stream()
                        .filter(pending -> pending.eligibleMachines().contains(id))
                        .count();
    }

    private void tryDispatchFromQueue(MachineId machineId, Scheduler scheduler, SimTime currentTime) {
        Machine machine = machines.getMut(machineId);
        if (!machine.canAcceptJob()) {
            return;
        }
        machine.dequeueJob().ifPresent(jobId -> {
            Job job = jobs.get(jobId);
            int stepIndex = job.currentStep();
            ProductId productId = job.productId();
            Routing routing = routings.getRoutingForProduct(productId);
            int routingIndex = stepIndex % routing.stepCount();
            RoutingStep step = routing.getStep(routingIndex)
                    .orElseThrow(() -> new SimError.Other(
                            "step index " + routingIndex + " out of range for job " + jobId));

            Machine m = machines.getMut(machineId);
            m.startJob(jobId);

            Job j = jobs.get(jobId);
            j.start(machineId);

            SimTime endTime = currentTime.plus(step.duration());
            scheduler.schedule(Event.of(endTime, new EventPayload.TaskStart(jobId, machineId, routingIndex)));
            scheduler.schedule(Event.of(endTime, new EventPayload.TaskEnd(jobId, machineId, routingIndex)));
        });
    }

    /**
     * Reconsiders multi-eligible pending work against its whole eligible set, not just whichever
     * machine most recently changed state -- so a machine that was offline (or busy) when a job
     * first waited can still pick it up once it becomes available, rather than the job staying
     * stranded on whichever machine happened to be selected when it was first attempted.
     *
     * <p>Scans for the earliest entry that can actually be placed right now, rather than only
     * ever looking at the front of the queue: two entries with disjoint eligible sets (e.g. one
     * waiting on {@code {M1, M2}}, another on {@code {M3, M4}}) must not head-of-line block each
     * other -- an undispatchable first entry must not stall a later entry whose own eligible
     * machine has just freed up. Among entries that are dispatchable in a given pass, the
     * earliest-queued one is dispatched first, so relative order is preserved wherever it
     * actually matters (competing entries that could land on the same machine).
     */
    private void tryDispatchPendingMultiEligible(Scheduler scheduler, SimTime currentTime) {
        boolean dispatchedOne = true;
        while (dispatchedOne) {
            dispatchedOne = false;
            for (Iterator<PendingDispatch> it = pendingMultiEligible.iterator(); it.hasNext(); ) {
                PendingDispatch candidate = it.next();
                MachineId machineId = selectMachine(candidate.eligibleMachines());
                Machine machine = machines.getMut(machineId);
                if (!machine.canAcceptJob()) {
                    continue;
                }
                it.remove();

                machine.startJob(candidate.jobId());
                Job job = jobs.get(candidate.jobId());
                job.start(machineId);

                SimTime endTime = currentTime.plus(candidate.duration());
                scheduler.schedule(Event.of(
                        endTime, new EventPayload.TaskStart(candidate.jobId(), machineId, candidate.routingIndex())));
                scheduler.schedule(Event.of(
                        endTime, new EventPayload.TaskEnd(candidate.jobId(), machineId, candidate.routingIndex())));

                dispatchedOne = true;
                break;
            }
        }
    }

    /**
     * Accepts an immutable {@link Order} and deterministically creates one unit execution {@link Job}
     * per requested unit under the same routing/dispatch semantics regardless of how the caller decided to produce it --
     * the economy-driven {@link EventPayload.OrderCreation} event and {@link FactoryRuntime}'s
     * explicit workload submission both resolve to this one acceptance operation.
     *
     * <p>Children are allocated and initially dispatched in ascending ordinal order. Every child
     * has {@code totalSteps = routing.stepCount()} and traverses the routing once; the existing
     * machine queue, pending multi-eligible queue, and selector remain authoritative.
     *
     * <p>Package-private: this method's {@link SimTime}/{@link Scheduler} parameters are
     * event-engine plumbing that a consumer-neutral workload boundary should not have to own or
     * supply. {@link FactoryRuntime} is the supported external entry point for explicit workload;
     * it owns the scheduler/time context and derives {@code currentTime} itself.
     */
    OrderId submitOrder(
            ProductId productId,
            long quantity,
            double unitPrice,
            SimTime currentTime,
            Scheduler scheduler) {
        if (quantity < 1) {
            throw new SimError.OutOfRange("quantity", "must be at least 1, got " + quantity);
        }
        Routing routing = routings.getRoutingForProduct(productId);
        int stepsPerUnit = routing.stepCount();
        // Guard by division, not multiplication: multiplying stepsPerUnit * quantity first (even
        // widened to long) can itself overflow for a large long quantity (e.g. Long.MAX_VALUE),
        // silently wrapping past this check. Dividing Integer.MAX_VALUE by stepsPerUnit instead
        // never overflows, so the comparison is exact for the full long range of quantity.
        final long materializationLimit = 100_000L;
        if (quantity > materializationLimit) {
            throw new SimError.OutOfRange(
                    "quantity",
                    "quantity " + quantity + " exceeds supported child materialization limit " + materializationLimit);
        }

        // Determine the immediate-dispatch outcome for step 0, if any, before mutating any store.
        // In particular, this validates that scheduling the resulting TaskEnd would actually
        // succeed (SimTime.plus can silently overflow for a pathologically large but validly
        // published step duration, which Scheduler.schedule would otherwise reject with
        // EventOrderingViolation) -- so that failure is caught here, before any Order/Job/Machine
        // exists, rather than after they do. A rejected submitOrder call must never leave partial
        // mutation; see FactoryRuntime#submitWorkload.
        Optional<RoutingStep> firstStepOpt = routing.getStep(0);
        MachineId selectedMachineId = null;
        Set<MachineId> eligible = null;
        SimTime immediateEndTime = null;
        if (firstStepOpt.isPresent()) {
            RoutingStep firstStep = firstStepOpt.get();
            eligible = firstStep.eligibleMachines();
            selectedMachineId = selectMachine(eligible);
            if (machines.getMut(selectedMachineId).canAcceptJob()) {
                SimTime endTime = currentTime.plus(firstStep.duration());
                if (endTime.compareTo(currentTime) < 0) {
                    throw new SimError.EventOrderingViolation(currentTime, endTime);
                }
                immediateEndTime = endTime;
            }
        }

        OrderId orderId = orders.createOrder(productId, quantity, currentTime, unitPrice);
        Order order = orders.get(orderId);
        for (long ordinal = 0; ordinal < quantity; ordinal++) {
            JobId jobId = jobs.createJob(order, ordinal, stepsPerUnit, currentTime);
            if (firstStepOpt.isPresent()) {
                // Re-select for every child: each preceding placement changes queue/active state.
                MachineId machineId = selectMachine(eligible);
                Machine machine = machines.getMut(machineId);
                if (machine.canAcceptJob()) {
                    machine.startJob(jobId);
                    jobs.get(jobId).start(machineId);
                    scheduler.schedule(Event.of(currentTime.plus(firstStepOpt.get().duration()), new EventPayload.TaskEnd(jobId, machineId, 0)));
                } else if (eligible.size() > 1) {
                    pendingMultiEligible.addLast(new PendingDispatch(jobId, eligible, 0, firstStepOpt.get().duration()));
                } else {
                    machine.enqueueJob(jobId);
                }
            }
        }

        return orderId;
    }

    private void handleTaskEnd(
            JobId jobId,
            MachineId machineId,
            int stepIndex,
            Scheduler scheduler,
            SimTime currentTime) {
        Machine machine = machines.getMut(machineId);
        machine.completeJob(jobId);

        Job job = jobs.get(jobId);

        // Identifying the active bottleneck from supported observations alone requires a
        // resource's cumulative busy time to be an authoritative fact -- which it is only if it is
        // actually accumulated. Credit the just-finished step's duration here -- the one point
        // where a machine is known to have occupied itself for exactly that long -- so
        // ResourceObservation.busyTicks() expresses real utilization rather than a constant zero.
        routings.getRoutingForProduct(job.productId())
                .getStep(stepIndex)
                .ifPresent(finished -> {
                    long updated = machine.busyTicks() + finished.duration();
                    // Durations are non-negative, so a negative sum can only be overflow; saturate
                    // rather than report a nonsensical utilization.
                    machine.setBusyTicks(updated < 0 ? Long.MAX_VALUE : updated);
                });

        job.completeStep(currentTime);

        if (job.isComplete()) {
            Order order = orders.get(job.orderId());
            if (orders.completeChild(order.id(), currentTime)) {
                completedSalesValue += order.orderValue();
                completedSales += 1;
                scheduler.schedule(Event.of(currentTime, new EventPayload.OrderCompleted(order.id(), job.id(), order.productId(), order.quantity(), order.unitPrice())));
            }
        } else {
            int nextStepIndex = job.currentStep();
            ProductId productId = job.productId();
            Routing routing = routings.getRoutingForProduct(productId);
            int routingIndex = nextStepIndex % routing.stepCount();

            routing.getStep(routingIndex).ifPresent(nextStep -> {
                Set<MachineId> eligible = nextStep.eligibleMachines();
                MachineId nextMachineId = selectMachine(eligible);
                Machine nextMachine = machines.getMut(nextMachineId);

                if (nextMachine.canAcceptJob()) {
                    long duration = nextStep.duration();
                    nextMachine.startJob(jobId);

                    Job j = jobs.get(jobId);
                    j.start(nextMachineId);

                    scheduler.schedule(Event.of(
                            currentTime.plus(duration),
                            new EventPayload.TaskEnd(jobId, nextMachineId, routingIndex)));
                } else if (eligible.size() > 1) {
                    pendingMultiEligible.addLast(
                            new PendingDispatch(jobId, eligible, routingIndex, nextStep.duration()));
                } else {
                    nextMachine.enqueueJob(jobId);
                }
            });
        }

        tryDispatchFromQueue(machineId, scheduler, currentTime);
        tryDispatchPendingMultiEligible(scheduler, currentTime);
    }

    /**
     * Package-private for the same reason as {@link #submitOrder}: {@link FactoryRuntime} is the
     * supported external entry point and owns the scheduler/time context, so it calls this
     * directly rather than a caller having to construct a {@link EventPayload.MachineAvailabilityChange}
     * event by hand.
     */
    void handleMachineAvailability(
            MachineId machineId, boolean online, Scheduler scheduler, SimTime currentTime) {
        Machine machine = machines.getMut(machineId);
        machine.setAvailability(online);

        if (online) {
            tryDispatchFromQueue(machineId, scheduler, currentTime);
            tryDispatchPendingMultiEligible(scheduler, currentTime);
        }
    }
}
