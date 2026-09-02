package com.arcogine.factory.machines;

import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.MachineState;
import com.arcogine.types.SimError;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Machine implements MachineView {

    private final MachineId id;
    private final String name;
    private MachineState state;
    private final int concurrency;
    private final List<JobId> activeJobs;
    private final ArrayDeque<JobId> queue;
    private final Double capacityLiters;
    private final long setupTime;
    private long busyTicks;

    public Machine(MachineId id, String name, int concurrency, Double capacityLiters, long setupTime) {
        this.id = id;
        this.name = name;
        this.state = MachineState.Idle;
        this.concurrency = concurrency;
        this.activeJobs = new ArrayList<>();
        this.queue = new ArrayDeque<>();
        this.capacityLiters = capacityLiters;
        this.setupTime = setupTime;
        this.busyTicks = 0;
    }

    public boolean canAcceptJob() {
        return state != MachineState.Offline && activeJobs.size() < concurrency;
    }

    public void startJob(JobId jobId) {
        if (state == MachineState.Offline) {
            throw new SimError.InvalidStateTransition(
                    "cannot start job on offline machine " + id);
        }
        if (activeJobs.size() >= concurrency) {
            throw new SimError.InvalidStateTransition(
                    "machine " + id + " already at max concurrency (" + concurrency + ")");
        }
        activeJobs.add(jobId);
        state = MachineState.Busy;
    }

    public void completeJob(JobId jobId) {
        int pos = -1;
        for (int i = 0; i < activeJobs.size(); i++) {
            if (activeJobs.get(i).equals(jobId)) {
                pos = i;
                break;
            }
        }
        if (pos < 0) {
            throw new SimError.InvalidStateTransition(
                    "job " + jobId + " not active on machine " + id);
        }
        activeJobs.remove(pos);
        if (activeJobs.isEmpty()) {
            state = MachineState.Idle;
        }
    }

    public void enqueueJob(JobId jobId) {
        queue.addLast(jobId);
    }

    public Optional<JobId> dequeueJob() {
        return Optional.ofNullable(queue.pollFirst());
    }

    @Override
    public int queueDepth() {
        return queue.size();
    }

    public void setAvailability(boolean online) {
        if (online) {
            if (state == MachineState.Offline) {
                state = MachineState.Idle;
            }
        } else {
            if (!activeJobs.isEmpty()) {
                throw new SimError.InvalidStateTransition(
                        "cannot take machine " + id + " offline while "
                                + activeJobs.size() + " jobs are active");
            }
            state = MachineState.Offline;
        }
    }

    @Override
    public MachineId id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public MachineState state() {
        return state;
    }

    @Override
    public int concurrency() {
        return concurrency;
    }

    @Override
    public List<JobId> activeJobs() {
        return List.copyOf(activeJobs);
    }

    public ArrayDeque<JobId> queue() {
        return queue;
    }

    @Override
    public List<JobId> queuedJobs() {
        return List.copyOf(queue);
    }

    @Override
    public Double capacityLiters() {
        return capacityLiters;
    }

    @Override
    public long setupTime() {
        return setupTime;
    }

    @Override
    public long busyTicks() {
        return busyTicks;
    }

    public void setBusyTicks(long busyTicks) {
        this.busyTicks = busyTicks;
    }
}
