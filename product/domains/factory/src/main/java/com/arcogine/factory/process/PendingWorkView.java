package com.arcogine.factory.process;

import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import java.util.Set;

/**
 * An immutable, read-only projection of one entry in {@link FactoryHandler}'s cross-machine
 * multi-eligible pending backlog: a job waiting for any one of several eligible machines to free
 * up, not pinned to (and therefore not reflected in the queue depth of) any single {@link
 * com.arcogine.factory.machines.MachineView}.
 *
 * <p>Gate 2 (docs/architecture/decisions/0005-gate-2-explicit-eligibility-dispatch-policy.md)
 * introduced this backlog as the runtime's second authoritative waiting-work structure alongside
 * each machine's own queue; Gate 3 acceptance criterion 5 (inspect order, work-item, queue, and
 * resource state) requires it be observable through the supported runtime boundary, not only
 * inferable from a machine's queue depth staying at zero. Resolve {@code jobId} through {@link
 * FactoryRuntime#job(JobId)} for the job's order/execution state.
 */
public record PendingWorkView(JobId jobId, Set<MachineId> eligibleMachines) {

    public PendingWorkView {
        eligibleMachines = Set.copyOf(eligibleMachines);
    }
}
