package com.arcogine.factory.process;

import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.MachineState;
import java.util.List;

/** Immutable current operational projection of one runtime resource. */
public record ResourceObservation(
        MachineId machineId,
        String name,
        MachineState state,
        int concurrency,
        List<JobId> activeJobIds,
        int queueDepth,
        Double capacityLiters,
        long setupTime,
        long busyTicks) {

    public ResourceObservation {
        activeJobIds = List.copyOf(activeJobIds);
    }
}
