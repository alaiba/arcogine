package com.arcogine.research.experiment;

import com.arcogine.factory.process.ResourceObservation;
import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeEventPayload;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.MachineState;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

/**
 * The processing a run's supported events record up to one observation boundary, measured the one
 * way every occupancy derivation here uses, so their occupied and capacity job-ticks cannot follow
 * different rules.
 *
 * <p>A dispatched step occupies its resource from its {@code JOB_DISPATCHED} time until its {@code
 * JOB_STEP_COMPLETED} time, or until the boundary's current time while it has not completed. A
 * resource's capacity over the interval is the boundary's current time multiplied by its
 * concurrency, which is truthful only for a resource that was online throughout. The measurement is
 * therefore refused when a resource it covers changed availability in the interval or is offline at
 * the boundary; and also when the events up to the boundary are not all retained, when no supported
 * time has elapsed, or when a completion has no matching dispatch.
 */
final class ProcessingIntervals {

    /** Occupied job-ticks per resource up to a boundary, or why the evidence does not license them. */
    sealed interface Measurement {

        /** Occupied job-ticks per resource that processed anything, over {@code [0, boundaryTime]}. */
        record Measured(long boundaryTime, Map<MachineId, Long> occupiedJobTicks) implements Measurement {}

        record Refused(String reason) implements Measurement {}
    }

    private record StepKey(JobId jobId, int stepIndex) {}

    private record Dispatch(MachineId machineId, long startTime) {}

    private ProcessingIntervals() {}

    /**
     * Measures processing up to the observation labeled {@code boundaryLabel}, requiring every
     * resource in {@code covered} to have been online throughout.
     */
    static Measurement measure(DeclaredEvidence evidence, String boundaryLabel, Set<MachineId> covered) {
        RuntimeObservation boundary = evidence.observation(boundaryLabel);
        long cursor = boundary.metadata().latestEventSequence();
        long boundaryTime = boundary.metadata().currentTime().value();

        Optional<List<RuntimeEventEnvelope>> events = evidence.completeEvents(0, cursor);
        if (events.isEmpty()) {
            return new Measurement.Refused("supported events 1.." + cursor + " are not all retained; missing "
                    + evidence.missingEvents(0, cursor));
        }
        if (boundaryTime == 0) {
            return new Measurement.Refused("no supported time has elapsed at boundary '" + boundaryLabel
                    + "', so occupancy over an interval is undefined");
        }
        for (ResourceObservation resource : boundary.resources()) {
            if (covered.contains(resource.machineId()) && resource.state() == MachineState.Offline) {
                return new Measurement.Refused("resource " + resource.machineId() + " is offline at boundary '"
                        + boundaryLabel + "', so it was not online throughout the interval");
            }
        }

        Map<StepKey, Dispatch> dispatches = new LinkedHashMap<>();
        Map<StepKey, Long> completions = new LinkedHashMap<>();
        for (RuntimeEventEnvelope event : events.get()) {
            switch (event.payload()) {
                case RuntimeEventPayload.JobDispatched dispatched -> dispatches.put(
                        new StepKey(dispatched.jobId(), dispatched.stepIndex()),
                        new Dispatch(dispatched.machineId(), event.simulationTime().value()));
                case RuntimeEventPayload.JobStepCompleted completed -> completions.put(
                        new StepKey(completed.jobId(), completed.stepIndex()), event.simulationTime().value());
                case RuntimeEventPayload.MachineAvailabilityChanged changed -> {
                    if (covered.contains(changed.machineId())) {
                        return new Measurement.Refused("resource " + changed.machineId() + " changed availability at tick "
                                + event.simulationTime().value() + " (event " + event.sequence() + "), so its capacity over"
                                + " the interval is not its concurrency times the elapsed time");
                    }
                }
                default -> { }
            }
        }
        for (StepKey completed : completions.keySet()) {
            if (!dispatches.containsKey(completed)) {
                return new Measurement.Refused("a completion for " + completed.jobId() + " step "
                        + completed.stepIndex() + " has no matching dispatch in events 1.." + cursor);
            }
        }

        Map<MachineId, Long> occupied = new TreeMap<>();
        dispatches.forEach((key, dispatch) -> occupied.merge(
                dispatch.machineId(), completions.getOrDefault(key, boundaryTime) - dispatch.startTime(), Long::sum));
        return new Measurement.Measured(boundaryTime, occupied);
    }
}
