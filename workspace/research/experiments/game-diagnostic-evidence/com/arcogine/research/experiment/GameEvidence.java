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
import com.arcogine.types.OrderId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Research-local pairing of supported events into job-step occurrences, for the game
 * diagnostic-evidence investigation. Research custody only: not an Engine fact, game-owned
 * analytics, or public API.
 *
 * <p>An occurrence is one {@code JOB_DISPATCHED} paired with the event that made its job ready for
 * the step (the job's previous {@code JOB_STEP_COMPLETED}, or its {@code ORDER_ACCEPTED} for the
 * first step) and, when one exists by the boundary, its {@code JOB_STEP_COMPLETED}. Nothing is
 * re-decided: which resource took the step is read from the dispatch event.
 */
final class GameEvidence {

    static final ResearchDefinition OCCURRENCES = new ResearchDefinition(
            "job-step-occurrences-from-supported-events",
            "For the supported observation at a boundary, the supported events 1..latestEventSequence and the"
                    + " published model, with exactly one ORDER_ACCEPTED: number each accepted job by its position in"
                    + " ORDER_ACCEPTED.jobIds (unit 1 first). Pair every JOB_DISPATCHED with the event that made the job"
                    + " ready for that step (its previous JOB_STEP_COMPLETED, or ORDER_ACCEPTED for the first step) and"
                    + " with its own JOB_STEP_COMPLETED when one exists by the boundary; resolve the step through the"
                    + " accepted product's routing. A job that became ready for a step and has no dispatch for it by the"
                    + " boundary is an open wait. Refuse when any of those events was not retained, when other than one"
                    + " order was accepted, or when a dispatch or completion cannot be paired that way.",
            java.util.Set.of(EvidenceInput.PUBLISHED_MODEL, EvidenceInput.OBSERVATIONS, EvidenceInput.SUPPORTED_EVENTS));

    record Occurrence(
            JobId jobId,
            int unit,
            int stepIndex,
            OperationStep step,
            MachineId machineId,
            long readyTick,
            long readySequence,
            long dispatchTick,
            long dispatchSequence,
            OptionalLong completionTick,
            OptionalLong completionSequence) {

        long waitTicks() {
            return dispatchTick - readyTick;
        }

        boolean completed() {
            return completionTick.isPresent();
        }

        long endOr(long boundaryTick) {
            return completionTick.orElse(boundaryTick);
        }
    }

    record OpenWait(JobId jobId, int unit, int stepIndex, OperationStep step, long readyTick, long readySequence) {}

    record CompletedOrder(OrderId orderId, JobId jobId, long tick, long sequence) {}

    record Occurrences(
            long boundaryTick,
            long boundarySequence,
            long acceptedTick,
            long acceptedSequence,
            int units,
            int stepsPerUnit,
            List<Occurrence> occurrences,
            List<OpenWait> openWaits,
            Optional<CompletedOrder> completion) {

        Optional<Occurrence> find(JobId jobId, int stepIndex) {
            return occurrences.stream()
                    .filter(o -> o.jobId().equals(jobId) && o.stepIndex() == stepIndex)
                    .findFirst();
        }

        List<Occurrence> ofJob(JobId jobId) {
            return occurrences.stream()
                    .filter(o -> o.jobId().equals(jobId))
                    .sorted(Comparator.comparingInt(Occurrence::stepIndex))
                    .toList();
        }

        List<Occurrence> onMachine(MachineId machineId) {
            return occurrences.stream().filter(o -> o.machineId().equals(machineId)).toList();
        }
    }

    sealed interface Result {
        record Measured(Occurrences occurrences) implements Result {}

        record Refused(String reason) implements Result {}
    }

    private record JobStep(JobId jobId, int stepIndex) {}

    private record Ready(long tick, long sequence) {}

    private GameEvidence() {}

    static Result occurrences(DeclaredEvidence evidence, String boundaryLabel) {
        FactoryModel model = evidence.publishedModel();
        RuntimeObservation boundary = evidence.observation(boundaryLabel);
        long cursor = boundary.metadata().latestEventSequence();
        Optional<List<RuntimeEventEnvelope>> events = evidence.completeEvents(0, cursor);
        if (events.isEmpty()) {
            return new Result.Refused("supported events 1.." + cursor + " are not all retained; missing "
                    + evidence.missingEvents(0, cursor));
        }

        RuntimeEventPayload.OrderAccepted accepted = null;
        long acceptedTick = 0;
        long acceptedSequence = 0;
        List<OperationStepDefinition> routing = List.of();
        OperationDefinition operation = null;
        Map<JobId, Integer> unitOf = new HashMap<>();
        Map<JobStep, Ready> ready = new LinkedHashMap<>();
        Map<JobStep, Occurrence> dispatched = new LinkedHashMap<>();
        CompletedOrder completion = null;

        for (RuntimeEventEnvelope event : events.get()) {
            long tick = event.simulationTime().value();
            switch (event.payload()) {
                case RuntimeEventPayload.OrderAccepted order -> {
                    if (accepted != null) {
                        return new Result.Refused("more than one order was accepted; the game's fixed workload is one order");
                    }
                    accepted = order;
                    acceptedTick = tick;
                    acceptedSequence = event.sequence();
                    Optional<OperationDefinition> resolved = operationOf(model, order);
                    if (resolved.isEmpty()) {
                        return new Result.Refused("the accepted product has no routing in the published model");
                    }
                    operation = resolved.get();
                    routing = operation.steps();
                    for (int i = 0; i < order.jobIds().size(); i++) {
                        unitOf.put(order.jobIds().get(i), i + 1);
                        ready.put(new JobStep(order.jobIds().get(i), 0), new Ready(tick, event.sequence()));
                    }
                }
                case RuntimeEventPayload.JobDispatched dispatch -> {
                    JobStep key = new JobStep(dispatch.jobId(), dispatch.stepIndex());
                    Ready readiness = ready.get(key);
                    if (operation == null || readiness == null || dispatch.stepIndex() < 0
                            || dispatch.stepIndex() >= routing.size() || dispatched.containsKey(key)) {
                        return new Result.Refused("dispatch event " + event.sequence() + " cannot be paired with the"
                                + " event that made its job ready for step index " + dispatch.stepIndex());
                    }
                    OperationStepDefinition step = routing.get(dispatch.stepIndex());
                    dispatched.put(key, new Occurrence(
                            dispatch.jobId(),
                            unitOf.get(dispatch.jobId()),
                            dispatch.stepIndex(),
                            new OperationStep(operation.id(), step.stepId(), step.name()),
                            dispatch.machineId(),
                            readiness.tick(),
                            readiness.sequence(),
                            tick,
                            event.sequence(),
                            OptionalLong.empty(),
                            OptionalLong.empty()));
                }
                case RuntimeEventPayload.JobStepCompleted completed -> {
                    JobStep key = new JobStep(completed.jobId(), completed.stepIndex());
                    Occurrence open = dispatched.get(key);
                    if (open == null || open.completed() || !open.machineId().equals(completed.machineId())) {
                        return new Result.Refused("completion event " + event.sequence() + " has no matching dispatch");
                    }
                    dispatched.put(key, new Occurrence(open.jobId(), open.unit(), open.stepIndex(), open.step(),
                            open.machineId(), open.readyTick(), open.readySequence(), open.dispatchTick(),
                            open.dispatchSequence(), OptionalLong.of(tick), OptionalLong.of(event.sequence())));
                    if (!completed.jobComplete()) {
                        ready.put(new JobStep(completed.jobId(), completed.stepIndex() + 1), new Ready(tick, event.sequence()));
                    }
                }
                case RuntimeEventPayload.OrderCompleted done ->
                    completion = new CompletedOrder(done.orderId(), done.jobId(), tick, event.sequence());
                default -> { }
            }
        }
        if (accepted == null) {
            return new Result.Refused("no order was accepted by the boundary");
        }

        List<OpenWait> openWaits = new ArrayList<>();
        for (Map.Entry<JobStep, Ready> entry : ready.entrySet()) {
            JobStep key = entry.getKey();
            if (!dispatched.containsKey(key)) {
                openWaits.add(new OpenWait(key.jobId(), unitOf.get(key.jobId()), key.stepIndex(),
                        stepAt(operation, key.stepIndex()), entry.getValue().tick(), entry.getValue().sequence()));
            }
        }
        List<Occurrence> occurrences = new ArrayList<>(dispatched.values());
        occurrences.sort(Comparator.comparingLong(Occurrence::dispatchSequence));
        return new Result.Measured(new Occurrences(
                boundary.metadata().currentTime().value(),
                cursor,
                acceptedTick,
                acceptedSequence,
                unitOf.size(),
                routing.size(),
                List.copyOf(occurrences),
                List.copyOf(openWaits),
                Optional.ofNullable(completion)));
    }

    private static Optional<OperationDefinition> operationOf(FactoryModel model, RuntimeEventPayload.OrderAccepted order) {
        Optional<Long> operationId = model.products().stream()
                .filter(product -> product.id().equals(order.productId()))
                .map(ProductDefinition::operationId)
                .findFirst();
        return operationId.flatMap(id -> model.operations().stream().filter(op -> op.id() == id).findFirst());
    }

    private static OperationStep stepAt(OperationDefinition operation, int stepIndex) {
        OperationStepDefinition step = operation.steps().get(stepIndex);
        return new OperationStep(operation.id(), step.stepId(), step.name());
    }
}
