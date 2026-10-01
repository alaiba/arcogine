package com.arcogine.research.experiment;

import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.process.ResourceObservation;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.types.JobStatus;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Research-local derivation: which work is waiting, and for which operation step, at one supported
 * observation.
 *
 * <p>Waiting belongs to the operation step first. A step's waiting work is attributed to one
 * resource only when the step's authored eligible set has exactly one member; otherwise it is
 * attributed to the whole eligible set, because work waiting for any of several resources is
 * held in a shared backlog that no single resource's queue depth reflects
 * ({@code docs/architecture/engine-semantics.md} section 2).
 */
public final class WaitingWorkByStepOracle implements Oracle<List<WaitingWorkByStepOracle.WaitingAtStep>> {

    public static final ResearchDefinition DEFINITION = new ResearchDefinition(
            "waiting-work-by-operation-step",
            "Group the jobs whose status is Queued at one supported observation by the operation step they are"
                    + " waiting to start, using each job's current routing step and the published model. Attribute a"
                    + " step's waiting work to a single resource only when the step's authored eligible set has exactly"
                    + " one member; otherwise attribute it to the whole eligible set. Refuse when the observation's"
                    + " queued jobs are not accounted for by per-resource queues plus pending multi-eligible work, or"
                    + " cannot all be resolved through the published model.",
            Set.of(EvidenceInput.PUBLISHED_MODEL, EvidenceInput.OBSERVATIONS));

    /** Which resource(s) a step's waiting work may truthfully be attributed to. */
    public sealed interface Attribution {

        /** The step's eligible set has one member, so waiting work is that resource's queue. */
        record SingleResource(MachineId machineId) implements Attribution {}

        /** Several resources are eligible; the waiting work is not any one resource's queue. */
        record SharedEligibleSet(Set<MachineId> eligibleMachines) implements Attribution {
            public SharedEligibleSet {
                eligibleMachines = Set.copyOf(eligibleMachines);
            }
        }
    }

    /** Waiting work at one operation step. Only steps with waiting work are reported. */
    public record WaitingAtStep(
            long operationId, long stepId, String stepName, long waitingJobs, Attribution attribution) {

        public WaitingAtStep {
            Objects.requireNonNull(stepName, "stepName");
            Objects.requireNonNull(attribution, "attribution");
        }
    }

    private final String observationLabel;

    public WaitingWorkByStepOracle(String observationLabel) {
        this.observationLabel = Objects.requireNonNull(observationLabel, "observationLabel");
    }

    @Override
    public ResearchDefinition definition() {
        return DEFINITION;
    }

    @Override
    public OracleOutcome<List<WaitingAtStep>> evaluate(DeclaredEvidence evidence) {
        FactoryModel model = evidence.publishedModel();
        RuntimeObservation observation = evidence.observation(observationLabel);

        long queued = observation.jobs().stream()
                .filter(job -> job.status() == JobStatus.Queued)
                .count();
        long accountedFor = observation.resources().stream()
                        .mapToLong(ResourceObservation::queueDepth)
                        .sum()
                + observation.pendingWork().size();
        if (queued != accountedFor) {
            return evidence.underdetermined(queued + " jobs are Queued, but per-resource queues plus pending"
                    + " multi-eligible work account for " + accountedFor);
        }

        List<WaitingAtStep> waiting = new ArrayList<>();
        for (OperationDefinition operation : model.operations()) {
            Set<ProductId> products = model.products().stream()
                    .filter(product -> product.operationId() == operation.id())
                    .map(ProductDefinition::id)
                    .collect(Collectors.toSet());
            for (int stepIndex = 0; stepIndex < operation.steps().size(); stepIndex++) {
                OperationStepDefinition step = operation.steps().get(stepIndex);
                int index = stepIndex;
                long jobs = observation.jobs().stream()
                        .filter(job -> job.status() == JobStatus.Queued
                                && job.currentStep() == index
                                && products.contains(job.productId()))
                        .count();
                if (jobs > 0) {
                    waiting.add(new WaitingAtStep(operation.id(), step.stepId(), step.name(), jobs, attributionFor(step)));
                }
            }
        }
        if (waiting.stream().mapToLong(WaitingAtStep::waitingJobs).sum() != queued) {
            return evidence.underdetermined(
                    "some Queued jobs cannot be resolved to an operation step through the published model");
        }
        return evidence.derived(List.copyOf(waiting));
    }

    private static Attribution attributionFor(OperationStepDefinition step) {
        Set<MachineId> eligible = step.eligibleResources();
        return eligible.size() == 1
                ? new Attribution.SingleResource(eligible.iterator().next())
                : new Attribution.SharedEligibleSet(eligible);
    }
}
