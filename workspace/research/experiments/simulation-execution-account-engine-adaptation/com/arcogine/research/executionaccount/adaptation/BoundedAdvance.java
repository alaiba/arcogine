package com.arcogine.research.executionaccount.adaptation;

import com.arcogine.factory.process.FactoryRuntime;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.factory.process.RuntimeRunState;
import com.arcogine.types.RunId;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Research-local surrogate for an Engine-produced bounded-advancement result, built only from the
 * supported session-control surface. It exists to test whether such a result would carry any fact
 * that an exclusive driver cannot already derive; it is not a proposed production type.
 *
 * <p>The derivation: a call that processed fewer internal events than its positive budget stopped
 * because no pending event remained at or before the target. A call that used its whole budget
 * proves nothing about what remains. A {@link SimError} escaping advancement is a fault. Whether
 * authoritative work remains beyond the target comes from the supported run state.
 */
final class BoundedAdvance {

    private BoundedAdvance() {}

    enum Stop {
        /** Every pending internal event at or before the target was processed. */
        EXHAUSTED_THROUGH_TARGET,
        /** The budget was used up; nothing is known about what remains at or before the target. */
        BUDGET_EXHAUSTED,
        /** Advancement threw; partial authoritative changes may have been published. */
        FAULTED
    }

    /**
     * What one bounded advancement established, bound to the supported boundary observed right
     * after it by the same exclusive caller.
     *
     * @param processed internal events the call processed; empty when it faulted, because the
     *     runtime does not report how far a faulting call got
     */
    record Outcome(
            Stop stop,
            OptionalLong processed,
            RunId runId,
            long sequenceAfter,
            long supportedTimeAfter,
            boolean authoritativeWorkRemains,
            Optional<SimError> fault) {

        Outcome {
            Objects.requireNonNull(stop, "stop");
            Objects.requireNonNull(processed, "processed");
            Objects.requireNonNull(runId, "runId");
            Objects.requireNonNull(fault, "fault");
        }
    }

    /**
     * Advances {@code runtime} through {@code target} with at most {@code budget} internal events.
     *
     * @throws IllegalArgumentException for a budget below one, which could never prove exhaustion
     */
    static Outcome advanceThrough(FactoryRuntime runtime, long target, long budget) {
        if (budget < 1) {
            throw new IllegalArgumentException("a positive budget is required to prove exhaustion, got " + budget);
        }
        long processed;
        try {
            processed = runtime.advanceUntil(SimTime.of(target), budget).size();
        } catch (SimError fault) {
            return outcome(Stop.FAULTED, OptionalLong.empty(), runtime.observe(), Optional.of(fault));
        }
        Stop stop = processed < budget ? Stop.EXHAUSTED_THROUGH_TARGET : Stop.BUDGET_EXHAUSTED;
        return outcome(stop, OptionalLong.of(processed), runtime.observe(), Optional.empty());
    }

    private static Outcome outcome(
            Stop stop, OptionalLong processed, RuntimeObservation after, Optional<SimError> fault) {
        return new Outcome(
                stop,
                processed,
                after.metadata().runId(),
                after.metadata().latestEventSequence(),
                after.metadata().currentTime().value(),
                after.metadata().runState() == RuntimeRunState.ACTIVE,
                fault);
    }
}
