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
 * <p>The dispatch semantics
 * (docs/architecture/engine-semantics.md section 2)
 * established this backlog as the runtime's second authoritative waiting-work structure alongside
 * each machine's own queue. The consumer-neutral runtime inspection contract requires it to be
 * observable through the supported runtime boundary, not only inferable from a machine's queue
 * depth staying at zero. Resolve {@code jobId} through {@link FactoryRuntime#job(JobId)} for the
 * job's order/execution state.
 */
public record PendingWorkView(JobId jobId, Set<MachineId> eligibleMachines) {

    public PendingWorkView {
        eligibleMachines = Set.copyOf(eligibleMachines);
    }
}
