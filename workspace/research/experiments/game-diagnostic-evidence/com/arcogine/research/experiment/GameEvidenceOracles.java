package com.arcogine.research.experiment;

import com.arcogine.research.experiment.GameEvidence.Occurrence;
import com.arcogine.research.experiment.GameEvidence.Occurrences;
import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Research-local derivations used by the game diagnostic-evidence investigation. Research custody
 * only: none of them is an Engine fact, game-owned analytics, or public API, and none re-decides a
 * dispatch -- each reads which resource took which step from supported events.
 */
final class GameEvidenceOracles {

    private GameEvidenceOracles() {}

    /** Every job-step occurrence and open wait up to a boundary. */
    static final class StepOccurrences implements Oracle<Occurrences> {

        private final String boundaryLabel;

        StepOccurrences(String boundaryLabel) {
            this.boundaryLabel = boundaryLabel;
        }

        @Override
        public ResearchDefinition definition() {
            return GameEvidence.OCCURRENCES;
        }

        @Override
        public OracleOutcome<Occurrences> evaluate(DeclaredEvidence evidence) {
            return switch (GameEvidence.occurrences(evidence, boundaryLabel)) {
                case GameEvidence.Result.Refused refused -> evidence.underdetermined(refused.reason());
                case GameEvidence.Result.Measured measured -> evidence.derived(measured.occurrences());
            };
        }
    }

    /** An idle online resource, the steps it may serve, and which of those steps have Queued work. */
    record IdleResource(MachineId machineId, List<String> stepsServed, List<String> waitingStepsServed) {}

    record Idleness(boolean orderComplete, long requestedQuantity, List<IdleResource> idle) {}

    static final ResearchDefinition IDLENESS = new ResearchDefinition(
            "idle-resources-and-eligible-waiting",
            "At one supported observation: when the single order is complete, report that every unit is complete."
                    + " Otherwise, for every resource whose state is not Offline and that has no active job, list the steps"
                    + " the published model makes it eligible for, and those of them that the current step of some Queued"
                    + " job names. Refuse when a Queued job's current step cannot be resolved through the published model.",
            Set.of(EvidenceInput.PUBLISHED_MODEL, EvidenceInput.OBSERVATIONS));

    static final class IdleResources implements Oracle<Idleness> {

        private final String label;

        IdleResources(String label) {
            this.label = label;
        }

        @Override
        public ResearchDefinition definition() {
            return IDLENESS;
        }

        @Override
        public OracleOutcome<Idleness> evaluate(DeclaredEvidence evidence) {
            com.arcogine.factory.model.FactoryModel model = evidence.publishedModel();
            com.arcogine.factory.process.RuntimeObservation observation = evidence.observation(label);
            com.arcogine.factory.process.OrderObservation order = observation.orders().getFirst();
            if (order.complete()) {
                return evidence.derived(new Idleness(true, order.requestedQuantity(), List.of()));
            }
            List<com.arcogine.factory.model.OperationStepDefinition> routing = model.operations().getFirst().steps();
            Set<String> waitingSteps = new LinkedHashSet<>();
            for (com.arcogine.factory.process.JobObservation job : observation.jobs()) {
                if (job.status() == com.arcogine.types.JobStatus.Queued) {
                    if (job.currentStep() < 0 || job.currentStep() >= routing.size()) {
                        return evidence.underdetermined("a Queued job's current step is not in the published routing");
                    }
                    waitingSteps.add(routing.get(job.currentStep()).name());
                }
            }
            List<IdleResource> idle = new ArrayList<>();
            for (com.arcogine.factory.process.ResourceObservation resource : observation.resources()) {
                if (resource.state() == com.arcogine.types.MachineState.Offline || !resource.activeJobIds().isEmpty()) {
                    continue;
                }
                List<String> served = routing.stream()
                        .filter(step -> step.eligibleResources().contains(resource.machineId()))
                        .map(com.arcogine.factory.model.OperationStepDefinition::name)
                        .toList();
                idle.add(new IdleResource(resource.machineId(), served, served.stream().filter(waitingSteps::contains).toList()));
            }
            return evidence.derived(new Idleness(false, order.requestedQuantity(), List.copyOf(idle)));
        }
    }

    /** One step of the completing unit: how long it waited for the step, and how long the step ran. */
    record UnitSegment(Occurrence occurrence) {

        long waitTicks() {
            return occurrence.waitTicks();
        }

        long processTicks() {
            return occurrence.completionTick().getAsLong() - occurrence.dispatchTick();
        }
    }

    /** The unit whose completion completed the order, and its lead time decomposed step by step. */
    record CompletingUnit(JobId jobId, int unit, long acceptedTick, long completionTick, List<UnitSegment> segments) {

        long leadTime() {
            return completionTick - acceptedTick;
        }

        List<Long> sequences() {
            List<Long> sequences = new ArrayList<>();
            for (UnitSegment segment : segments) {
                sequences.add(segment.occurrence().readySequence());
                sequences.add(segment.occurrence().dispatchSequence());
                sequences.add(segment.occurrence().completionSequence().getAsLong());
            }
            return sequences.stream().distinct().sorted().toList();
        }
    }

    static final ResearchDefinition COMPLETING_UNIT = new ResearchDefinition(
            "completing-unit-lead-time-decomposition",
            "Apply job-step-occurrences-from-supported-events at the closing observation. Require an"
                    + " ORDER_COMPLETED; take the job it names. For each of that job's steps in routing order, its wait is"
                    + " the dispatch tick minus the tick the job became ready for the step, and its processing is the"
                    + " completion tick minus the dispatch tick. The waits and processing sum to the job's lead time from"
                    + " ORDER_ACCEPTED. Refuse when the occurrences refuse, the order is not complete, or the job has"
                    + " not completed every step.",
            Set.of(EvidenceInput.PUBLISHED_MODEL, EvidenceInput.OBSERVATIONS, EvidenceInput.SUPPORTED_EVENTS));

    static final class CompletingUnitTimeline implements Oracle<CompletingUnit> {

        @Override
        public ResearchDefinition definition() {
            return COMPLETING_UNIT;
        }

        @Override
        public OracleOutcome<CompletingUnit> evaluate(DeclaredEvidence evidence) {
            Occurrences occurrences;
            switch (GameEvidence.occurrences(evidence, ExperimentEvidence.CLOSING_LABEL)) {
                case GameEvidence.Result.Refused refused -> {
                    return evidence.underdetermined(refused.reason());
                }
                case GameEvidence.Result.Measured measured -> occurrences = measured.occurrences();
            }
            if (occurrences.completion().isEmpty()) {
                return evidence.underdetermined("the order has not completed at the closing observation");
            }
            JobId job = occurrences.completion().get().jobId();
            List<Occurrence> steps = occurrences.ofJob(job);
            if (steps.size() != occurrences.stepsPerUnit() || steps.stream().anyMatch(o -> !o.completed())) {
                return evidence.underdetermined("the completing job has not completed every step");
            }
            return evidence.derived(new CompletingUnit(
                    job,
                    steps.getFirst().unit(),
                    occurrences.acceptedTick(),
                    occurrences.completion().get().tick(),
                    steps.stream().map(UnitSegment::new).toList()));
        }
    }

    /** How one occurrence on the completion chain links to the occurrence before it. */
    enum LinkKind {
        /** The step started when its job became ready: the chain continues through the job's previous step. */
        JOB_READY,
        /** The step started after its job became ready: the chain continues through the step that released its resource. */
        RESOURCE_RELEASED,
        /** The job's first step started when the order was accepted: the chain ends. */
        ORDER_ACCEPTED
    }

    record ChainLink(Occurrence occurrence, LinkKind link, Optional<Occurrence> releasedBy) {}

    /** A step that started at the tick its job became ready and its resource was released by another job. */
    record ChainTie(Occurrence occurrence, Occurrence releasedBy) {}

    record CompletionChain(List<ChainLink> links, List<ChainTie> ties) {

        /** The steps whose occurrences on the chain waited for capacity, with how many did. */
        Map<OperationStep, Long> capacityWaitsByStep() {
            Map<OperationStep, Long> waits = new LinkedHashMap<>();
            for (ChainLink link : links) {
                if (link.link() == LinkKind.RESOURCE_RELEASED) {
                    waits.merge(link.occurrence().step(), 1L, Long::sum);
                }
            }
            return waits;
        }

        /** The resources that occurrences on the chain waited for at {@code step}. */
        Set<MachineId> capacityResources(OperationStep step) {
            Set<MachineId> machines = new LinkedHashSet<>();
            for (ChainLink link : links) {
                if (link.link() == LinkKind.RESOURCE_RELEASED && link.occurrence().step().equals(step)) {
                    machines.add(link.occurrence().machineId());
                }
            }
            return machines;
        }

        /** Present only when the chain has no tie and its capacity waits are all at one step. */
        Optional<OperationStep> uniquePacingStep() {
            Map<OperationStep, Long> waits = capacityWaitsByStep();
            return ties.isEmpty() && waits.size() == 1 ? Optional.of(waits.keySet().iterator().next()) : Optional.empty();
        }

        /**
         * The span of the chain's occurrences at {@code step} on the resources it waited for there:
         * first dispatch to last completion.
         */
        long[] span(OperationStep step) {
            Set<MachineId> machines = capacityResources(step);
            long first = Long.MAX_VALUE;
            long last = Long.MIN_VALUE;
            for (ChainLink link : links) {
                Occurrence occurrence = link.occurrence();
                if (occurrence.step().equals(step) && machines.contains(occurrence.machineId())) {
                    first = Math.min(first, occurrence.dispatchTick());
                    last = Math.max(last, occurrence.completionTick().getAsLong());
                }
            }
            return new long[] {first, last};
        }

        List<Long> sequences() {
            List<Long> sequences = new ArrayList<>();
            for (ChainLink link : links) {
                sequences.add(link.occurrence().readySequence());
                sequences.add(link.occurrence().dispatchSequence());
                link.occurrence().completionSequence().ifPresent(sequences::add);
            }
            return sequences.stream().distinct().sorted().toList();
        }
    }

    static final ResearchDefinition COMPLETION_CHAIN = new ResearchDefinition(
            "completion-chain-from-supported-events",
            "Apply job-step-occurrences-from-supported-events at the closing observation and start from the last step"
                    + " of the job ORDER_COMPLETED names. For each occurrence on the chain: if it was dispatched later than"
                    + " its job became ready, continue through the occurrence whose JOB_STEP_COMPLETED on the same resource"
                    + " at the dispatch tick released it (the latest such completion sequenced before the dispatch); if it"
                    + " was dispatched at the tick its job became ready, continue through the job's previous step, and"
                    + " record a tie when a different job's step completed on the same resource at that tick; stop at the"
                    + " job's first step dispatched at order acceptance. The chain names a pacing step only when it has no"
                    + " tie and every capacity wait on it is at one step. Refuse when the occurrences refuse, the order is"
                    + " not complete, or a later-than-ready dispatch has no such release.",
            Set.of(EvidenceInput.PUBLISHED_MODEL, EvidenceInput.OBSERVATIONS, EvidenceInput.SUPPORTED_EVENTS));

    static final class CompletionChainTrace implements Oracle<CompletionChain> {

        @Override
        public ResearchDefinition definition() {
            return COMPLETION_CHAIN;
        }

        @Override
        public OracleOutcome<CompletionChain> evaluate(DeclaredEvidence evidence) {
            Occurrences occurrences;
            switch (GameEvidence.occurrences(evidence, ExperimentEvidence.CLOSING_LABEL)) {
                case GameEvidence.Result.Refused refused -> {
                    return evidence.underdetermined(refused.reason());
                }
                case GameEvidence.Result.Measured measured -> occurrences = measured.occurrences();
            }
            if (occurrences.completion().isEmpty()) {
                return evidence.underdetermined("the order has not completed at the closing observation");
            }
            List<Occurrence> lastJob = occurrences.ofJob(occurrences.completion().get().jobId());
            Occurrence current = lastJob.getLast();
            List<ChainLink> links = new ArrayList<>();
            List<ChainTie> ties = new ArrayList<>();
            while (true) {
                Occurrence at = current;
                if (at.dispatchTick() > at.readyTick()) {
                    Optional<Occurrence> release = occurrences.onMachine(at.machineId()).stream()
                            .filter(other -> other != at
                                    && other.completionTick().isPresent()
                                    && other.completionTick().getAsLong() == at.dispatchTick()
                                    && other.completionSequence().getAsLong() < at.dispatchSequence())
                            .max(Comparator.comparingLong(other -> other.completionSequence().getAsLong()));
                    if (release.isEmpty()) {
                        return evidence.underdetermined("unit " + at.unit() + " started " + at.step().name() + " on "
                                + at.machineId() + " at tick " + at.dispatchTick() + ", later than it became ready, but no"
                                + " step completed on that resource at that tick");
                    }
                    links.add(new ChainLink(at, LinkKind.RESOURCE_RELEASED, release));
                    current = release.get();
                    continue;
                }
                occurrences.onMachine(at.machineId()).stream()
                        .filter(other -> !other.jobId().equals(at.jobId())
                                && other.completionTick().isPresent()
                                && other.completionTick().getAsLong() == at.dispatchTick())
                        .findFirst()
                        .ifPresent(other -> ties.add(new ChainTie(at, other)));
                if (at.stepIndex() == 0) {
                    links.add(new ChainLink(at, LinkKind.ORDER_ACCEPTED, Optional.empty()));
                    break;
                }
                links.add(new ChainLink(at, LinkKind.JOB_READY, Optional.empty()));
                current = occurrences.find(at.jobId(), at.stepIndex() - 1).orElseThrow();
            }
            return evidence.derived(new CompletionChain(List.copyOf(links), List.copyOf(ties)));
        }
    }
}
