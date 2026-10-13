package com.arcogine.factory.machines;

import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.MachineState;
import com.arcogine.types.SimError;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.SequencedSet;

public class Machine implements MachineView {

    private final MachineId id;
    private final String name;
    private MachineState state;
    private final int concurrency;
    private final List<JobId> activeJobs;
    private final ArrayDeque<JobId> queue;
    /**
     * Jobs holding a unit of this machine's admission capacity while inbound to it, in reservation
     * order (docs/architecture/engine-semantics.md section 6 rule 5 and section 9 rules 2 and 4). A
     * reserved job is neither active nor queued: it counts toward {@link #canAcceptJob()} and the
     * {@link #startJob} capacity check, and toward nothing this machine reports through {@link
     * MachineView}. No runtime path currently makes a reservation.
     */
    private final SequencedSet<JobId> inboundReservations;
    private final Double capacityLiters;
    private final long setupTime;

    public Machine(MachineId id, String name, int concurrency, Double capacityLiters, long setupTime) {
        this.id = id;
        this.name = name;
        this.state = MachineState.Idle;
        this.concurrency = concurrency;
        this.activeJobs = new ArrayList<>();
        this.queue = new ArrayDeque<>();
        this.inboundReservations = new LinkedHashSet<>();
        this.capacityLiters = capacityLiters;
        this.setupTime = setupTime;
    }

    public boolean canAcceptJob() {
        return state != MachineState.Offline && admissionLoad() < concurrency;
    }

    public void startJob(JobId jobId) {
        if (state == MachineState.Offline) {
            throw new SimError.InvalidStateTransition(
                    "cannot start job on offline machine " + id);
        }
        if (admissionLoad() >= concurrency) {
            throw new SimError.InvalidStateTransition(
                    "machine " + id + " already at max concurrency (" + concurrency + ")"
                            + reservedCapacityNote());
        }
        activeJobs.add(jobId);
        state = MachineState.Busy;
    }

    /**
     * Reserves one unit of admission capacity for {@code jobId}, which is inbound to this machine.
     * The reservation is admissible only where {@link #canAcceptJob()} holds
     * (docs/architecture/engine-semantics.md section 2 rule 10 and section 6 rule 4). It leaves
     * {@link #activeJobs()}, {@link #queuedJobs()}, {@link #queueDepth()} and {@link #state()}
     * unchanged, and it ends only through {@link #startReservedJob} or {@link #enqueueReservedJob}.
     *
     * @throws SimError.InvalidStateTransition if {@code jobId} already holds a reservation here, or
     *     the machine is offline or has no free admission capacity; the machine is then unchanged
     */
    public void reserveInboundAdmission(JobId jobId) {
        if (inboundReservations.contains(jobId)) {
            throw new SimError.InvalidStateTransition(
                    "job " + jobId + " already holds an inbound reservation on machine " + id);
        }
        if (state == MachineState.Offline) {
            throw new SimError.InvalidStateTransition(
                    "cannot reserve inbound admission for job " + jobId + " on offline machine "
                            + id);
        }
        if (admissionLoad() >= concurrency) {
            throw new SimError.InvalidStateTransition(
                    "cannot reserve inbound admission for job " + jobId + ": machine " + id
                            + " already at max concurrency (" + concurrency + ")"
                            + reservedCapacityNote());
        }
        inboundReservations.add(jobId);
    }

    /**
     * Converts {@code jobId}'s inbound reservation into active processing, for a job arriving while
     * this machine is online (docs/architecture/engine-semantics.md section 8). The job's own
     * reservation is the capacity it starts in, so no other capacity is required and admission load
     * is unchanged; afterwards the machine is exactly as if the job had been started by {@link
     * #startJob}.
     *
     * @throws SimError.InvalidStateTransition if {@code jobId} holds no reservation here or the
     *     machine is offline; the machine is then unchanged
     */
    public void startReservedJob(JobId jobId) {
        requireInboundReservation(jobId);
        if (state == MachineState.Offline) {
            throw new SimError.InvalidStateTransition(
                    "cannot start reserved job " + jobId + " on offline machine " + id);
        }
        inboundReservations.remove(jobId);
        activeJobs.add(jobId);
        state = MachineState.Busy;
    }

    /**
     * Converts {@code jobId}'s inbound reservation into an entry at the tail of this machine's own
     * FIFO queue, for a job arriving while this machine is offline
     * (docs/architecture/engine-semantics.md section 6 rule 9 and section 9 rule 8). The job is then
     * ordinary queued work, counted by {@link #queueDepth()} and no longer reserved.
     *
     * @throws SimError.InvalidStateTransition if {@code jobId} holds no reservation here or the
     *     machine is online; the machine is then unchanged
     */
    public void enqueueReservedJob(JobId jobId) {
        requireInboundReservation(jobId);
        if (state != MachineState.Offline) {
            throw new SimError.InvalidStateTransition(
                    "cannot queue reserved job " + jobId + " on online machine " + id
                            + ": only an arrival at an offline machine waits in its queue");
        }
        inboundReservations.remove(jobId);
        queue.addLast(jobId);
    }

    /** The jobs holding an inbound admission reservation on this machine, in reservation order. */
    public List<JobId> inboundReservations() {
        return List.copyOf(inboundReservations);
    }

    private int admissionLoad() {
        return activeJobs.size() + inboundReservations.size();
    }

    private String reservedCapacityNote() {
        return inboundReservations.isEmpty()
                ? ""
                : " with " + inboundReservations.size() + " reserved for inbound jobs";
    }

    private void requireInboundReservation(JobId jobId) {
        if (!inboundReservations.contains(jobId)) {
            throw new SimError.InvalidStateTransition(
                    "job " + jobId + " holds no inbound reservation on machine " + id);
        }
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
}
