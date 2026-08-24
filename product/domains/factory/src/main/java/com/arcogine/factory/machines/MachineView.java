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

    Double capacityLiters();

    long setupTime();

    long busyTicks();
}
