package com.arcogine.research.executionaccount;

import com.arcogine.factory.process.JobObservation;
import com.arcogine.factory.process.ResourceObservation;
import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeEventPayload;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.types.JobId;
import com.arcogine.types.JobStatus;
import com.arcogine.types.MachineId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Research-local reading: the placement state a consumer reaches by applying supported events, in
 * the order given, to a supported observation. It is a consumer view, never runtime authority, and
 * it is strict: a transition the supported contract cannot produce (a step completing for work that
 * is not running there, more running work than a resource's concurrency) fails.
 */
final class PlacementFold {

    /** One job's placement: status, routing step, and the resource running it while in progress. */
    record JobPlacement(JobStatus status, int step, MachineId machine) {}

    private final Map<MachineId, Integer> concurrency = new TreeMap<>();
    private final Map<MachineId, TreeSet<JobId>> active = new TreeMap<>();
    private final Map<JobId, JobPlacement> jobs = new LinkedHashMap<>();
    private long time;

    private PlacementFold(RuntimeObservation basis) {
        for (ResourceObservation resource : basis.resources()) {
            concurrency.put(resource.machineId(), resource.concurrency());
            active.put(resource.machineId(), new TreeSet<>(resource.activeJobIds()));
        }
        for (JobObservation job : basis.jobs()) {
            jobs.put(job.jobId(), new JobPlacement(
                    job.status(), job.currentStep(), job.status() == JobStatus.InProgress ? job.currentMachineId() : null));
        }
        time = basis.metadata().currentTime().value();
    }

    static PlacementFold from(RuntimeObservation basis) {
        return new PlacementFold(basis);
    }

    long time() {
        return time;
    }

    int activeCount(MachineId machine) {
        return active.get(machine).size();
    }

    int concurrency(MachineId machine) {
        return concurrency.get(machine);
    }

    PlacementFold applyAll(List<RuntimeEventEnvelope> events) {
        events.forEach(this::apply);
        return this;
    }

    void apply(RuntimeEventEnvelope event) {
        time = event.simulationTime().value();
        switch (event.payload()) {
            case RuntimeEventPayload.OrderAccepted accepted ->
                accepted.jobIds().forEach(job -> jobs.put(job, new JobPlacement(JobStatus.Queued, 0, null)));
            case RuntimeEventPayload.JobDispatched dispatched -> {
                JobPlacement before = require(dispatched.jobId(), event.sequence());
                if (before.status() != JobStatus.Queued || before.step() != dispatched.stepIndex()) {
                    throw new IllegalStateException("event " + event.sequence() + ": " + dispatched.jobId()
                            + " dispatched for step " + dispatched.stepIndex() + " while " + before);
                }
                TreeSet<JobId> running = active.get(dispatched.machineId());
                running.add(dispatched.jobId());
                if (running.size() > concurrency.get(dispatched.machineId())) {
                    throw new IllegalStateException("event " + event.sequence() + ": " + dispatched.machineId()
                            + " would run " + running + " with concurrency " + concurrency.get(dispatched.machineId()));
                }
                jobs.put(dispatched.jobId(),
                        new JobPlacement(JobStatus.InProgress, dispatched.stepIndex(), dispatched.machineId()));
            }
            case RuntimeEventPayload.JobWaiting waiting -> {
                JobPlacement before = require(waiting.jobId(), event.sequence());
                if (before.status() != JobStatus.Queued) {
                    throw new IllegalStateException("event " + event.sequence() + ": " + waiting.jobId()
                            + " reported waiting while " + before);
                }
            }
            case RuntimeEventPayload.JobStepCompleted completed -> {
                JobPlacement before = require(completed.jobId(), event.sequence());
                if (before.status() != JobStatus.InProgress
                        || !completed.machineId().equals(before.machine())
                        || before.step() != completed.stepIndex()) {
                    throw new IllegalStateException("event " + event.sequence() + ": " + completed.jobId()
                            + " completed step " + completed.stepIndex() + " on " + completed.machineId()
                            + " while " + before);
                }
                active.get(completed.machineId()).remove(completed.jobId());
                jobs.put(completed.jobId(), new JobPlacement(
                        completed.jobComplete() ? JobStatus.Completed : JobStatus.Queued,
                        completed.stepIndex() + 1,
                        null));
            }
            case RuntimeEventPayload.OrderCompleted ignored -> { }
            case RuntimeEventPayload.MachineAvailabilityChanged ignored -> { }
        }
    }

    /** Whether this fold agrees with {@code observation} on every job's placement and every resource's running work. */
    boolean agreesWith(RuntimeObservation observation) {
        for (ResourceObservation resource : observation.resources()) {
            if (!active.get(resource.machineId()).equals(new TreeSet<>(resource.activeJobIds()))) {
                return false;
            }
        }
        if (observation.jobs().size() != jobs.size()) {
            return false;
        }
        for (JobObservation job : observation.jobs()) {
            JobPlacement expected = new JobPlacement(
                    job.status(), job.currentStep(), job.status() == JobStatus.InProgress ? job.currentMachineId() : null);
            if (!expected.equals(jobs.get(job.jobId()))) {
                return false;
            }
        }
        return true;
    }

    /** Jobs currently waiting to start their next step. */
    long queuedCount() {
        return jobs.values().stream().filter(job -> job.status() == JobStatus.Queued).count();
    }

    private JobPlacement require(JobId job, long sequence) {
        JobPlacement placement = jobs.get(job);
        if (placement == null) {
            throw new IllegalStateException("event " + sequence + ": unknown job " + job);
        }
        return placement;
    }
}
