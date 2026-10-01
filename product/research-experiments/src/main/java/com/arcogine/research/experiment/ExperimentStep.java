package com.arcogine.research.experiment;

import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimTime;
import java.util.Objects;

/**
 * One operation in a fixture's explicit, ordered script.
 *
 * <p>Every step maps onto the supported session-control and observation surface of {@code
 * FactoryRuntime}: the two externally initiated commands ({@code submitWorkload}, {@code
 * setMachineAvailability}), bounded advancement ({@code advanceUntil}), the supported
 * observation, and the draining supported-event stream. There is deliberately no step that reaches
 * scheduler or handler internals.
 *
 * <p>Runtime event delivery drains, so where evidence is collected is part of the experiment. The
 * {@link CaptureEvents} and {@link DiscardEvents} steps make each drain an explicit, labeled
 * collection point.
 */
public sealed interface ExperimentStep {

    /** Submits explicit workload through the supported command boundary. */
    record SubmitWorkload(ProductId productId, long quantity, double unitPrice) implements ExperimentStep {
        public SubmitWorkload {
            Objects.requireNonNull(productId, "productId");
        }
    }

    /** Brings a machine online or takes it offline through the supported command boundary. */
    record SetMachineAvailability(MachineId machineId, boolean online) implements ExperimentStep {
        public SetMachineAvailability {
            Objects.requireNonNull(machineId, "machineId");
        }
    }

    /** Advances processing of events up to and including {@code target}; may leave work pending. */
    record AdvanceUntil(SimTime target) implements ExperimentStep {
        public AdvanceUntil {
            Objects.requireNonNull(target, "target");
        }
    }

    /**
     * Advances until the runtime reports it is quiescent, and requires that it is by {@code
     * deadline}. A run that is still active at the deadline is a malformed experiment, not a
     * partial result.
     */
    record AdvanceToQuiescence(SimTime deadline) implements ExperimentStep {
        public AdvanceToQuiescence {
            Objects.requireNonNull(deadline, "deadline");
        }
    }

    /** A step that produces a labeled collection point. */
    sealed interface Capture extends ExperimentStep {
        String label();
    }

    /** Captures the supported observation under {@code label}; does not drain events. */
    record CaptureObservation(String label) implements Capture {
        public CaptureObservation {
            requireLabel(label);
        }
    }

    /** Drains the supported events accumulated since the previous drain and retains them. */
    record CaptureEvents(String label) implements Capture {
        public CaptureEvents {
            requireLabel(label);
        }
    }

    /**
     * Drains the supported events accumulated since the previous drain without retaining them: a
     * consumer that joined late, or lost events. The resulting evidence is intentionally partial.
     */
    record DiscardEvents(String label) implements Capture {
        public DiscardEvents {
            requireLabel(label);
        }
    }

    static SubmitWorkload submit(ProductId productId, long quantity, double unitPrice) {
        return new SubmitWorkload(productId, quantity, unitPrice);
    }

    static SetMachineAvailability setAvailability(MachineId machineId, boolean online) {
        return new SetMachineAvailability(machineId, online);
    }

    static AdvanceUntil advanceUntil(long ticks) {
        return new AdvanceUntil(SimTime.of(ticks));
    }

    static AdvanceToQuiescence advanceToQuiescence(long deadlineTicks) {
        return new AdvanceToQuiescence(SimTime.of(deadlineTicks));
    }

    static CaptureObservation observe(String label) {
        return new CaptureObservation(label);
    }

    static CaptureEvents captureEvents(String label) {
        return new CaptureEvents(label);
    }

    static DiscardEvents discardEvents(String label) {
        return new DiscardEvents(label);
    }

    private static void requireLabel(String label) {
        if (Objects.requireNonNull(label, "label").isBlank()) {
            throw new IllegalArgumentException("label must not be blank");
        }
    }
}
