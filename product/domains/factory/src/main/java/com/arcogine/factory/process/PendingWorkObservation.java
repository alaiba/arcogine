package com.arcogine.factory.process;

import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import java.util.List;

/** Immutable projection of child work waiting for any of several eligible resources. */
public record PendingWorkObservation(JobId jobId, List<MachineId> eligibleMachineIds) {

    public PendingWorkObservation {
        eligibleMachineIds = List.copyOf(eligibleMachineIds);
    }
}
