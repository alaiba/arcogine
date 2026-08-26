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
import com.arcogine.factory.routing.Routing;
import com.arcogine.factory.routing.RoutingStep;
import com.arcogine.factory.routing.RoutingStore;
import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class FactoryHandler implements EventHandler {

    final MachineStore machines;
    final OrderStore orders;
    final JobStore jobs;
    public final RoutingStore routings;
    public final List<ProductId> productIds;
    private double completedSalesValue;
    private long completedSales;

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

    public long backlog() {
        return jobs.activeJobs().count();
    }

    public double avgLeadTime() {
        List<Job> completed = jobs.completedJobs().toList();
        if (completed.isEmpty()) {
            return 0.0;
        }
        long total = completed.stream()
                .map(Job::leadTime)
                .filter(Optional::isPresent)
                .mapToLong(Optional::get)
                .sum();
        return (double) total / completed.size();
    }

    public double throughput(long elapsedTicks) {
        if (elapsedTicks == 0) {
            return 0.0;
        }
        return (double) completedSales / elapsedTicks;
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
     * Accepts an immutable {@link Order} and creates the one execution {@link Job} for it under
     * the same routing/dispatch semantics regardless of how the caller decided to produce it --
     * the economy-driven {@link EventPayload.OrderCreation} event and {@link FactoryRuntime}'s
     * explicit workload submission both resolve to this one acceptance operation.
     *
     * <p>The job's routing repeats once per unit of {@code quantity}: {@code totalSteps =
     * routing.stepCount() * quantity}, so a quantity-10 order consumes ten times the routing/
     * machine work of an otherwise identical quantity-1 order, and {@link Job#currentStep()} is a
     * job-global counter across every repeated pass. Dispatch and completion resolve that
     * job-global step back to its underlying {@link RoutingStep} by {@code stepIndex %
     * routing.stepCount()}; the externally visible {@link EventPayload.TaskStart}/{@link
     * EventPayload.TaskEnd#stepIndex()} continues to carry that routing-local index (as it did
     * before quantity repeated the routing), not the job-global counter, so the event contract's
     * existing meaning is unchanged.
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
        long totalStepsExact = (long) routing.stepCount() * quantity;
        if (totalStepsExact > Integer.MAX_VALUE) {
            throw new SimError.OutOfRange(
                    "quantity",
                    "quantity " + quantity + " with routing step count " + routing.stepCount()
                            + " exceeds the maximum representable execution step count");
        }
        int totalSteps = (int) totalStepsExact;

        OrderId orderId = orders.createOrder(productId, quantity, currentTime, unitPrice);
        Order order = orders.get(orderId);
        JobId jobId = jobs.createJob(order, totalSteps, currentTime);

        routing.getStep(0).ifPresent(firstStep -> {
            MachineId machineId = firstStep.machineId();
            Machine machine = machines.getMut(machineId);

            if (machine.canAcceptJob()) {
                long duration = firstStep.duration();
                machine.startJob(jobId);

                Job job = jobs.get(jobId);
                job.start(machineId);

                scheduler.schedule(Event.of(
                        currentTime.plus(duration),
                        new EventPayload.TaskEnd(jobId, machineId, 0)));
            } else {
                machine.enqueueJob(jobId);
            }
        });

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
        job.completeStep(currentTime);

        if (job.isComplete()) {
            Order order = orders.get(job.orderId());
            completedSalesValue += order.orderValue();
            completedSales += 1;
            scheduler.schedule(Event.of(
                    currentTime,
                    new EventPayload.OrderCompleted(
                            job.id(), order.productId(), order.quantity(), order.unitPrice())));
        } else {
            int nextStepIndex = job.currentStep();
            ProductId productId = job.productId();
            Routing routing = routings.getRoutingForProduct(productId);
            int routingIndex = nextStepIndex % routing.stepCount();

            routing.getStep(routingIndex).ifPresent(nextStep -> {
                MachineId nextMachineId = nextStep.machineId();
                Machine nextMachine = machines.getMut(nextMachineId);

                if (nextMachine.canAcceptJob()) {
                    long duration = nextStep.duration();
                    nextMachine.startJob(jobId);

                    Job j = jobs.get(jobId);
                    j.start(nextMachineId);

                    scheduler.schedule(Event.of(
                            currentTime.plus(duration),
                            new EventPayload.TaskEnd(jobId, nextMachineId, routingIndex)));
                } else {
                    nextMachine.enqueueJob(jobId);
                }
            });
        }

        tryDispatchFromQueue(machineId, scheduler, currentTime);
    }

    private void handleMachineAvailability(
            MachineId machineId, boolean online, Scheduler scheduler, SimTime currentTime) {
        Machine machine = machines.getMut(machineId);
        machine.setAvailability(online);

        if (online) {
            tryDispatchFromQueue(machineId, scheduler, currentTime);
        }
    }
}
