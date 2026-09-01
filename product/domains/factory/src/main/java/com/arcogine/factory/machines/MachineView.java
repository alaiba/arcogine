package com.arcogine.factory.machines;

import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.MachineState;
import java.util.List;

/**
 * Read-only view of a {@link Machine} -- deliberately excludes {@code startJob}, {@code
 * completeJob}, {@code enqueueJob}/{@code dequeueJob}, {@code setAvailability}, and {@code
 * setBusyTicks}. Only {@code FactoryHandler} (via its own package-private {@code MachineStore})
 * drives machine state; everything else, including anything obtaining a reference via
 * {@code FactoryHandler.machinesView()}, is structurally limited to reading.
 */
public interface MachineView {

    MachineId id();

    String name();

    MachineState state();

    int concurrency();

    List<JobId> activeJobs();

    int queueDepth();

    /**
     * The jobs waiting in this machine's own single-eligible-machine queue, in dispatch order --
     * distinct from the cross-machine multi-eligible backlog ({@code
     * com.arcogine.factory.process.PendingWorkView}), which is not reflected here. Needed so a
     * runtime-event consumer can attribute a not-yet-dispatched job to the specific machine it is
     * waiting on (ADR-0011 REV-002).
     */
    List<JobId> queuedJobs();

    Double capacityLiters();

    long setupTime();

    long busyTicks();
}
