package com.arcogine.research.experiment;

import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeEventPayload;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

/**
 * Research-local derivation: how much processing time each resource was occupied, over the interval
 * from the start of the run to one supported observation boundary.
 *
 * <p>It measures outcomes from supported dispatch and completion events; it never reconstructs or
 * re-decides which resource a job was given. It is deliberately not derived from the cumulative
 * {@code busyTicks} a resource observation reports: that field is credited only when a step
 * completes, so it does not count a step that is still running, and it sums processing ticks across
 * concurrent jobs, so it cannot be read as an instantaneous occupancy and, divided by elapsed time,
 * exceeds 1 when concurrency does.
 */
public final class ProcessingOccupancyOracle implements Oracle<List<ProcessingOccupancyOracle.ResourceOccupancy>> {

    public static final ResearchDefinition DEFINITION = new ResearchDefinition(
            "processing-occupancy-from-supported-events",
            "For the supported observation at a boundary and the supported events 1..latestEventSequence, a step"
                    + " occupies its resource from its JOB_DISPATCHED time until its JOB_STEP_COMPLETED time, or until"
                    + " the boundary's current time while it has not completed. occupiedJobTicks is the sum of those"
                    + " durations per resource; capacityJobTicks is the boundary's current time multiplied by the"
                    + " resource's concurrency. Refuse when any of those events was not retained, when no supported"
                    + " time has elapsed at the boundary, or when a completion has no matching dispatch.",
            Set.of(EvidenceInput.OBSERVATIONS, EvidenceInput.SUPPORTED_EVENTS));

    /**
     * Exact integer occupancy for one resource. Their ratio is the occupancy over the interval; it
     * is not computed here so that no rounding rule enters the derived value.
     */
    public record ResourceOccupancy(MachineId machineId, long occupiedJobTicks, long capacityJobTicks) {

        public ResourceOccupancy {
            Objects.requireNonNull(machineId, "machineId");
        }
    }

    private record StepKey(JobId jobId, int stepIndex) {}

    private record Dispatch(MachineId machineId, long startTime) {}

    private final String boundaryLabel;

    public ProcessingOccupancyOracle(String boundaryLabel) {
        this.boundaryLabel = Objects.requireNonNull(boundaryLabel, "boundaryLabel");
    }

    @Override
    public ResearchDefinition definition() {
        return DEFINITION;
    }

    @Override
    public OracleOutcome<List<ResourceOccupancy>> evaluate(DeclaredEvidence evidence) {
        RuntimeObservation boundary = evidence.observation(boundaryLabel);
        long cursor = boundary.metadata().latestEventSequence();
        long boundaryTime = boundary.metadata().currentTime().value();

        Optional<List<RuntimeEventEnvelope>> events = evidence.completeEvents(0, cursor);
        if (events.isEmpty()) {
            return evidence.underdetermined("supported events 1.." + cursor + " are not all retained; missing "
                    + evidence.missingEvents(0, cursor));
        }
        if (boundaryTime == 0) {
            return evidence.underdetermined("no supported time has elapsed at boundary '" + boundaryLabel
                    + "', so occupancy over an interval is undefined");
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
                default -> { }
            }
        }
        for (StepKey completed : completions.keySet()) {
            if (!dispatches.containsKey(completed)) {
                return evidence.underdetermined("a completion for " + completed.jobId() + " step "
                        + completed.stepIndex() + " has no matching dispatch in events 1.." + cursor);
            }
        }

        Map<MachineId, Long> occupied = new TreeMap<>();
        dispatches.forEach((key, dispatch) -> occupied.merge(
                dispatch.machineId(), completions.getOrDefault(key, boundaryTime) - dispatch.startTime(), Long::sum));
        List<ResourceOccupancy> occupancy = boundary.resources().stream()
                .map(resource -> new ResourceOccupancy(
                        resource.machineId(),
                        occupied.getOrDefault(resource.machineId(), 0L),
                        Math.multiplyExact(boundaryTime, (long) resource.concurrency())))
                .toList();
        return evidence.derived(occupancy);
    }
}
