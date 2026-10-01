package com.arcogine.research.experiment;

import com.arcogine.factory.process.ResourceObservation;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.types.MachineId;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Research-local derivation: how much processing time each resource was occupied, over the interval
 * from the start of the run to one supported observation boundary. Like every derivation here, it
 * is not an Engine fact, game-owned analytics, or public API.
 *
 * <p>It measures outcomes from supported dispatch and completion events; it never reconstructs or
 * re-decides which resource a job was given. It is deliberately not derived from the cumulative
 * {@code busyTicks} a resource observation reports: that field is credited only when a step
 * completes, so it does not count a step that is still running, and it sums processing ticks across
 * concurrent jobs, so it cannot be read as an instantaneous occupancy and, divided by elapsed time,
 * exceeds 1 when concurrency does. Capacity over the interval is truthful only for a resource that
 * was online throughout, so a resource that changed availability refuses the whole measurement.
 *
 * <p>A resource's occupancy is a measurement, not an interpretation. A resource that is highly
 * occupied is not thereby the design's constraint or bottleneck, and a resource that several steps
 * share cannot attribute its capacity to any one of them; {@link EligibilityPoolOccupancyOracle}
 * measures such shared capacity per eligibility pool.
 */
public final class ProcessingOccupancyOracle implements Oracle<List<ProcessingOccupancyOracle.ResourceOccupancy>> {

    public static final ResearchDefinition DEFINITION = new ResearchDefinition(
            "processing-occupancy-of-continuously-online-resources",
            "For the supported observation at a boundary and the supported events 1..latestEventSequence, a step"
                    + " occupies its resource from its JOB_DISPATCHED time until its JOB_STEP_COMPLETED time, or until"
                    + " the boundary's current time while it has not completed. occupiedJobTicks is the sum of those"
                    + " durations per resource; capacityJobTicks is the boundary's current time multiplied by the"
                    + " resource's concurrency. Refuse when any of those events was not retained, when no supported"
                    + " time has elapsed at the boundary, when an observed resource changed availability in those"
                    + " events or is offline at the boundary, or when a completion has no matching dispatch.",
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
        Set<MachineId> observed = boundary.resources().stream()
                .map(ResourceObservation::machineId)
                .collect(Collectors.toSet());
        return switch (ProcessingIntervals.measure(evidence, boundaryLabel, observed)) {
            case ProcessingIntervals.Measurement.Refused refused -> evidence.underdetermined(refused.reason());
            case ProcessingIntervals.Measurement.Measured measured -> evidence.derived(boundary.resources().stream()
                    .map(resource -> new ResourceOccupancy(
                            resource.machineId(),
                            measured.occupiedJobTicks().getOrDefault(resource.machineId(), 0L),
                            Math.multiplyExact(measured.boundaryTime(), (long) resource.concurrency())))
                    .toList());
        };
    }
}
