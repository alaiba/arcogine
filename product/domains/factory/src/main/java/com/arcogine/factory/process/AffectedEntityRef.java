package com.arcogine.factory.process;

import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.OrderId;
import java.util.Objects;

/**
 * Typed, stable correlation to one authoritative entity affected by a {@link RuntimeEventEnvelope},
 * used in place of an ambiguous display string (ADR-0011, ADR-0010 child-job identity semantics). A single
 * supported event can carry more than one ref -- e.g. a job-step-completion event correlates both
 * the completing {@link JobId} and its parent {@link OrderId}.
 */
public sealed interface AffectedEntityRef {

    record OrderRef(OrderId orderId) implements AffectedEntityRef {
        public OrderRef {
            Objects.requireNonNull(orderId, "orderId");
        }
    }

    record JobRef(JobId jobId) implements AffectedEntityRef {
        public JobRef {
            Objects.requireNonNull(jobId, "jobId");
        }
    }

    record MachineRef(MachineId machineId) implements AffectedEntityRef {
        public MachineRef {
            Objects.requireNonNull(machineId, "machineId");
        }
    }
}
