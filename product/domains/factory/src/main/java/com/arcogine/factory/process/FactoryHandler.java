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
            RoutingStep step = routing.getStep(stepIndex)
                    .orElseThrow(() -> new SimError.Other(
                            "step index " + stepIndex + " out of range for job " + jobId));

            Machine m = machines.getMut(machineId);
            m.startJob(jobId);

            Job j = jobs.get(jobId);
            j.start(machineId);

            SimTime endTime = currentTime.plus(step.duration());
            scheduler.schedule(Event.of(endTime, new EventPayload.TaskStart(jobId, machineId, stepIndex)));
            scheduler.schedule(Event.of(endTime, new EventPayload.TaskEnd(jobId, machineId, stepIndex)));
        });
    }

    /**
     * Submits explicit production workload: accepts an immutable {@link Order} and creates the one
     * execution {@link Job} for it under the same routing/dispatch semantics as any other accepted
     * order, independent of how the caller decided to produce it. This is the supported,
     * consumer-neutral entry point for production workload -- it depends on nothing outside the
     * factory runtime (no economy, pricing, demand, or agent involvement), and is the same code
     * path the economy-driven {@link EventPayload.OrderCreation} event uses.
     */
    public OrderId submitOrder(
            ProductId productId,
            long quantity,
            double unitPrice,
            SimTime currentTime,
            Scheduler scheduler) {
        Routing routing = routings.getRoutingForProduct(productId);
        int totalSteps = routing.stepCount();

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

            routing.getStep(nextStepIndex).ifPresent(nextStep -> {
                MachineId nextMachineId = nextStep.machineId();
                Machine nextMachine = machines.getMut(nextMachineId);

                if (nextMachine.canAcceptJob()) {
                    long duration = nextStep.duration();
                    nextMachine.startJob(jobId);

                    Job j = jobs.get(jobId);
                    j.start(nextMachineId);

                    scheduler.schedule(Event.of(
                            currentTime.plus(duration),
                            new EventPayload.TaskEnd(jobId, nextMachineId, nextStepIndex)));
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
