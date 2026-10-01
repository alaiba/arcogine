package com.arcogine.research.experiment;

import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.types.MachineId;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Research-local derivation: how much of each eligibility pool's capacity was occupied, over the
 * interval from the start of the run to one supported observation boundary. Like every derivation
 * here, it is not an Engine fact, game-owned analytics, or public API.
 *
 * <p>An eligibility pool is a connected component of the graph that links each operation step to
 * the resources eligible for it: a resource and every step it may serve belong to one pool, so work
 * at any of a pool's steps can draw on capacity that its other steps also use. It is a grouping for
 * measurement only. It is not a Factory resource pool, work center or other canonical grouping
 * ({@code docs/architecture/factory-resource-semantics.md}), and it decides nothing about which
 * resource takes which work.
 *
 * <p>Occupancy is measured per pool, never per step: a resource that serves several steps cannot
 * truthfully attribute its capacity to one of them, so a pool reports its steps but only pool-level
 * occupied and capacity job-ticks. Occupied job-ticks follow the same rule as {@link
 * ProcessingOccupancyOracle}, and so does the availability rule behind the capacity: a pooled
 * resource that changed availability in the interval refuses the measurement.
 *
 * <p>Occupancy is a measurement, not an interpretation. {@link
 * PoolOccupancies#maximumOccupancyPools()} names the pools whose measured occupancy is highest; it
 * does not name a constraint or bottleneck of the design, and an investigation that treats it as one
 * makes that interpretation in its own artifacts.
 */
public final class EligibilityPoolOccupancyOracle
        implements Oracle<EligibilityPoolOccupancyOracle.PoolOccupancies> {

    public static final ResearchDefinition DEFINITION = new ResearchDefinition(
            "eligibility-pool-occupancy-of-continuously-online-resources",
            "Group the published model's operation steps into eligibility pools: the connected components of the"
                    + " graph linking each step to the resources eligible for it. For the supported observation at a"
                    + " boundary and the supported events 1..latestEventSequence, a step occupies its resource from its"
                    + " JOB_DISPATCHED time until its JOB_STEP_COMPLETED time, or until the boundary's current time"
                    + " while it has not completed; a pool's occupiedJobTicks is the sum of those durations over its"
                    + " resources, and its capacityJobTicks is the boundary's current time multiplied by the summed"
                    + " authored concurrency of its resources. Capacity is never attributed to an individual step."
                    + " Refuse when any of those events was not retained, when no supported time has elapsed at the"
                    + " boundary, when a pooled resource changed availability in those events or is offline at the"
                    + " boundary, or when a completion has no matching dispatch.",
            Set.of(EvidenceInput.PUBLISHED_MODEL, EvidenceInput.OBSERVATIONS, EvidenceInput.SUPPORTED_EVENTS));

    /**
     * A connected component of the step–resource eligibility graph: steps that can draw on the same
     * resources, and those resources.
     *
     * @param steps the pool's steps, in operation order and then routing order
     * @param resources the resources eligible for any of those steps
     */
    public record EligibilityPool(List<OperationStep> steps, Set<MachineId> resources) {

        public EligibilityPool {
            steps = List.copyOf(Objects.requireNonNull(steps, "steps"));
            resources = Set.copyOf(Objects.requireNonNull(resources, "resources"));
            if (steps.isEmpty() || resources.isEmpty()) {
                throw new IllegalArgumentException("a pool has at least one step and one resource");
            }
        }
    }

    /**
     * Exact integer occupancy for one pool. Their ratio is the pool's occupancy over the interval; it
     * is not computed here so that no rounding rule enters the derived value.
     */
    public record PoolOccupancy(EligibilityPool pool, long occupiedJobTicks, long capacityJobTicks) {

        public PoolOccupancy {
            Objects.requireNonNull(pool, "pool");
            if (occupiedJobTicks < 0 || capacityJobTicks < 1) {
                throw new IllegalArgumentException("occupancy needs non-negative occupied and positive capacity"
                        + " job-ticks, got " + occupiedJobTicks + " of " + capacityJobTicks);
            }
        }

        /** Compares this pool's occupancy ratio with {@code other}'s exactly, without rounding. */
        int compareOccupancy(PoolOccupancy other) {
            BigInteger mine = BigInteger.valueOf(occupiedJobTicks).multiply(BigInteger.valueOf(other.capacityJobTicks));
            BigInteger theirs = BigInteger.valueOf(other.occupiedJobTicks).multiply(BigInteger.valueOf(capacityJobTicks));
            return mine.compareTo(theirs);
        }
    }

    /** The occupancy of every eligibility pool of the published model, in pool order. */
    public record PoolOccupancies(List<PoolOccupancy> pools) {

        public PoolOccupancies {
            pools = List.copyOf(Objects.requireNonNull(pools, "pools"));
        }

        /**
         * The pools whose occupancy ratio is the maximum, compared exactly. Every pool that ties for
         * the maximum is returned: a tie is part of the measurement, not something to break.
         */
        public Set<EligibilityPool> maximumOccupancyPools() {
            Set<EligibilityPool> maximum = new LinkedHashSet<>();
            PoolOccupancy best = null;
            for (PoolOccupancy candidate : pools) {
                int comparison = best == null ? 1 : candidate.compareOccupancy(best);
                if (comparison > 0) {
                    maximum.clear();
                    best = candidate;
                }
                if (comparison >= 0) {
                    maximum.add(candidate.pool());
                }
            }
            return Set.copyOf(maximum);
        }
    }

    private final String boundaryLabel;

    public EligibilityPoolOccupancyOracle(String boundaryLabel) {
        this.boundaryLabel = Objects.requireNonNull(boundaryLabel, "boundaryLabel");
    }

    /**
     * The eligibility pools of {@code model}, ordered by their first step. A resource that is
     * eligible for no step belongs to no pool, since no work can be dispatched to it.
     */
    public static List<EligibilityPool> poolsOf(FactoryModel model) {
        List<OperationStep> steps = new ArrayList<>();
        List<Set<MachineId>> eligibility = new ArrayList<>();
        Map<MachineId, List<Integer>> stepsByResource = new HashMap<>();
        for (OperationDefinition operation : model.operations()) {
            for (OperationStepDefinition step : operation.steps()) {
                int index = steps.size();
                steps.add(new OperationStep(operation.id(), step.stepId(), step.name()));
                eligibility.add(step.eligibleResources());
                step.eligibleResources().forEach(
                        resource -> stepsByResource.computeIfAbsent(resource, ignored -> new ArrayList<>()).add(index));
            }
        }

        List<EligibilityPool> pools = new ArrayList<>();
        boolean[] pooled = new boolean[steps.size()];
        for (int first = 0; first < steps.size(); first++) {
            if (pooled[first]) {
                continue;
            }
            Set<Integer> members = new TreeSet<>();
            Set<MachineId> resources = new LinkedHashSet<>();
            Deque<Integer> frontier = new ArrayDeque<>(List.of(first));
            pooled[first] = true;
            while (!frontier.isEmpty()) {
                int current = frontier.removeFirst();
                members.add(current);
                for (MachineId resource : eligibility.get(current)) {
                    if (resources.add(resource)) {
                        for (int linked : stepsByResource.get(resource)) {
                            if (!pooled[linked]) {
                                pooled[linked] = true;
                                frontier.addLast(linked);
                            }
                        }
                    }
                }
            }
            pools.add(new EligibilityPool(members.stream().map(steps::get).toList(), resources));
        }
        return List.copyOf(pools);
    }

    @Override
    public ResearchDefinition definition() {
        return DEFINITION;
    }

    @Override
    public OracleOutcome<PoolOccupancies> evaluate(DeclaredEvidence evidence) {
        FactoryModel model = evidence.publishedModel();
        List<EligibilityPool> pools = poolsOf(model);
        Set<MachineId> pooledResources = new LinkedHashSet<>();
        pools.forEach(pool -> pooledResources.addAll(pool.resources()));
        Map<MachineId, Integer> concurrency = new HashMap<>();
        for (ConfiguredResource resource : model.resources()) {
            concurrency.put(resource.id(), resource.concurrency());
        }

        return switch (ProcessingIntervals.measure(evidence, boundaryLabel, pooledResources)) {
            case ProcessingIntervals.Measurement.Refused refused -> evidence.underdetermined(refused.reason());
            case ProcessingIntervals.Measurement.Measured measured -> evidence.derived(new PoolOccupancies(pools.stream()
                    .map(pool -> new PoolOccupancy(
                            pool,
                            pool.resources().stream()
                                    .mapToLong(resource -> measured.occupiedJobTicks().getOrDefault(resource, 0L))
                                    .sum(),
                            Math.multiplyExact(
                                    measured.boundaryTime(),
                                    pool.resources().stream().mapToLong(concurrency::get).sum())))
                    .toList()));
        };
    }
}
