package com.arcogine.research.experiment;

import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeEventPayload;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

/**
 * Research-local derivation: how a run's dispatches were spread over resources and operation steps,
 * and how long dispatched work waited for each step, up to one supported observation boundary. Like
 * every derivation here, it is not an Engine fact, game-owned analytics, or public API.
 *
 * <p>It counts the {@code JOB_DISPATCHED} events the run emitted; it never reconstructs or re-decides
 * which resource a job should have been given. A dispatch's wait runs from the moment its job became
 * ready for the step: the completion of the job's previous step or, for its first step, the
 * acceptance of its order. Waits are reported per step as an exact sum and a count, so a mean is
 * their ratio and no rounding rule enters the derived value.
 */
public final class DispatchProfileOracle implements Oracle<DispatchProfileOracle.DispatchProfile> {

    public static final ResearchDefinition DEFINITION = new ResearchDefinition(
            "dispatch-profile-by-resource-and-step",
            "For the supported observation at a boundary, the supported events 1..latestEventSequence and the"
                    + " published model: resolve each JOB_DISPATCHED to the operation step it starts, through the"
                    + " product its order's ORDER_ACCEPTED names and that product's routing in the published model."
                    + " Count dispatches per resource and step. Per step, sum the ticks from each dispatched job's"
                    + " previous JOB_STEP_COMPLETED, or for its first step from its ORDER_ACCEPTED, to the dispatch,"
                    + " alongside the number of dispatches. Refuse when any of those events was not retained or a"
                    + " dispatch cannot be resolved that way.",
            Set.of(EvidenceInput.PUBLISHED_MODEL, EvidenceInput.OBSERVATIONS, EvidenceInput.SUPPORTED_EVENTS));

    /** How many dispatches one resource received for one step. */
    public record ResourceStepDispatches(MachineId machineId, OperationStep step, long dispatches) {

        public ResourceStepDispatches {
            Objects.requireNonNull(machineId, "machineId");
            Objects.requireNonNull(step, "step");
        }
    }

    /** The exact total wait of every dispatch for one step, and how many dispatches it covers. */
    public record StepWait(OperationStep step, long dispatches, long totalWaitTicks) {

        public StepWait {
            Objects.requireNonNull(step, "step");
        }
    }

    /**
     * The dispatch profile up to a boundary. Only steps and resource-step pairs that received a
     * dispatch are listed, in operation order and then routing order, and by resource identifier
     * within a step.
     */
    public record DispatchProfile(List<ResourceStepDispatches> dispatches, List<StepWait> waits) {

        public DispatchProfile {
            dispatches = List.copyOf(Objects.requireNonNull(dispatches, "dispatches"));
            waits = List.copyOf(Objects.requireNonNull(waits, "waits"));
        }
    }

    /** A step's position in the published model, which orders the profile. */
    private record StepPosition(int operationIndex, int stepIndex) implements Comparable<StepPosition> {

        @Override
        public int compareTo(StepPosition other) {
            int byOperation = Integer.compare(operationIndex, other.operationIndex);
            return byOperation != 0 ? byOperation : Integer.compare(stepIndex, other.stepIndex);
        }
    }

    private record JobStep(JobId jobId, int stepIndex) {}

    private final String boundaryLabel;

    public DispatchProfileOracle(String boundaryLabel) {
        this.boundaryLabel = Objects.requireNonNull(boundaryLabel, "boundaryLabel");
    }

    @Override
    public ResearchDefinition definition() {
        return DEFINITION;
    }

    @Override
    public OracleOutcome<DispatchProfile> evaluate(DeclaredEvidence evidence) {
        FactoryModel model = evidence.publishedModel();
        RuntimeObservation boundary = evidence.observation(boundaryLabel);
        long cursor = boundary.metadata().latestEventSequence();
        Optional<List<RuntimeEventEnvelope>> events = evidence.completeEvents(0, cursor);
        if (events.isEmpty()) {
            return evidence.underdetermined("supported events 1.." + cursor + " are not all retained; missing "
                    + evidence.missingEvents(0, cursor));
        }

        Map<JobId, ProductId> productOf = new HashMap<>();
        Map<JobId, Long> acceptedAt = new HashMap<>();
        Map<JobStep, Long> completedAt = new HashMap<>();
        Map<StepPosition, OperationStep> steps = new TreeMap<>();
        Map<StepPosition, Map<MachineId, Long>> dispatchCounts = new TreeMap<>();
        Map<StepPosition, Long> waitTotals = new TreeMap<>();
        for (RuntimeEventEnvelope event : events.get()) {
            long time = event.simulationTime().value();
            switch (event.payload()) {
                case RuntimeEventPayload.OrderAccepted accepted -> accepted.jobIds().forEach(jobId -> {
                    productOf.put(jobId, accepted.productId());
                    acceptedAt.put(jobId, time);
                });
                case RuntimeEventPayload.JobStepCompleted completed ->
                    completedAt.put(new JobStep(completed.jobId(), completed.stepIndex()), time);
                case RuntimeEventPayload.JobDispatched dispatched -> {
                    Optional<StepPosition> position = positionOf(model, productOf.get(dispatched.jobId()), dispatched);
                    Long readyAt = dispatched.stepIndex() == 0
                            ? acceptedAt.get(dispatched.jobId())
                            : completedAt.get(new JobStep(dispatched.jobId(), dispatched.stepIndex() - 1));
                    if (position.isEmpty() || readyAt == null) {
                        return evidence.underdetermined("the dispatch of " + dispatched.jobId() + " for step index "
                                + dispatched.stepIndex() + " (event " + event.sequence() + ") cannot be resolved to an"
                                + " accepted order, its product's routing, and the moment the job became ready");
                    }
                    StepPosition at = position.get();
                    steps.computeIfAbsent(at, ignored -> stepAt(model, at));
                    dispatchCounts.computeIfAbsent(at, ignored -> new TreeMap<>())
                            .merge(dispatched.machineId(), 1L, Long::sum);
                    waitTotals.merge(at, time - readyAt, Long::sum);
                }
                default -> { }
            }
        }

        List<ResourceStepDispatches> dispatches = new ArrayList<>();
        List<StepWait> waits = new ArrayList<>();
        dispatchCounts.forEach((at, byResource) -> {
            byResource.forEach((machineId, count) ->
                    dispatches.add(new ResourceStepDispatches(machineId, steps.get(at), count)));
            long total = byResource.values().stream().mapToLong(Long::longValue).sum();
            waits.add(new StepWait(steps.get(at), total, waitTotals.get(at)));
        });
        return evidence.derived(new DispatchProfile(dispatches, waits));
    }

    private static Optional<StepPosition> positionOf(
            FactoryModel model, ProductId productId, RuntimeEventPayload.JobDispatched dispatched) {
        if (productId == null) {
            return Optional.empty();
        }
        Optional<Long> operationId = model.products().stream()
                .filter(product -> product.id().equals(productId))
                .map(ProductDefinition::operationId)
                .findFirst();
        for (int operationIndex = 0; operationIndex < model.operations().size(); operationIndex++) {
            OperationDefinition operation = model.operations().get(operationIndex);
            if (operationId.isPresent()
                    && operation.id() == operationId.get()
                    && dispatched.stepIndex() >= 0
                    && dispatched.stepIndex() < operation.steps().size()) {
                return Optional.of(new StepPosition(operationIndex, dispatched.stepIndex()));
            }
        }
        return Optional.empty();
    }

    private static OperationStep stepAt(FactoryModel model, StepPosition position) {
        OperationDefinition operation = model.operations().get(position.operationIndex());
        OperationStepDefinition step = operation.steps().get(position.stepIndex());
        return new OperationStep(operation.id(), step.stepId(), step.name());
    }
}
