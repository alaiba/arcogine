package com.arcogine.research.experiment;

import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.process.JobObservation;
import com.arcogine.factory.process.OrderObservation;
import com.arcogine.factory.process.ResourceObservation;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.research.experiment.EligibilityPoolOccupancyOracle.EligibilityPool;
import com.arcogine.research.experiment.EligibilityPoolOccupancyOracle.PoolOccupancies;
import com.arcogine.research.experiment.EligibilityPoolOccupancyOracle.PoolOccupancy;
import com.arcogine.research.experiment.GameDiagnostics.Attempt;
import com.arcogine.research.experiment.GameDiagnostics.Change;
import com.arcogine.research.experiment.GameDiagnostics.ChangeType;
import com.arcogine.research.experiment.GameDiagnostics.Kind;
import com.arcogine.research.experiment.GameDiagnostics.Question;
import com.arcogine.research.experiment.GameDiagnostics.Statement;
import com.arcogine.research.experiment.GameDiagnostics.Support;
import com.arcogine.research.experiment.GameEvidence.Occurrence;
import com.arcogine.research.experiment.GameEvidence.Occurrences;
import com.arcogine.research.experiment.GameEvidenceOracles.ChainTie;
import com.arcogine.research.experiment.GameEvidenceOracles.CompletingUnit;
import com.arcogine.research.experiment.GameEvidenceOracles.CompletionChain;
import com.arcogine.research.experiment.GameEvidenceOracles.UnitSegment;
import com.arcogine.research.experiment.WaitingWorkByStepOracle.Attribution;
import com.arcogine.research.experiment.WaitingWorkByStepOracle.WaitingAtStep;
import com.arcogine.types.JobStatus;
import com.arcogine.types.MachineId;
import com.arcogine.types.MachineState;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Statement builders shared by the candidate contracts. Each builder reads only the attempt's
 * authored design, its supported observations, and research-local derivations evaluated through
 * {@link DeclaredEvidence}; each statement cites the evidence it rests on.
 */
final class GameStatements {

    private GameStatements() {}

    // ---------------------------------------------------------------- naming (published-model facts)

    static String resourceName(FactoryModel model, MachineId id) {
        return model.resources().stream()
                .filter(resource -> resource.id().equals(id))
                .map(ConfiguredResource::name)
                .findFirst()
                .orElse(id.toString());
    }

    static List<String> stepsServedBy(FactoryModel model, MachineId id) {
        List<String> steps = new ArrayList<>();
        for (OperationStepDefinition step : model.operations().getFirst().steps()) {
            if (step.eligibleResources().contains(id)) {
                steps.add(step.name());
            }
        }
        return steps;
    }

    static String names(FactoryModel model, Set<MachineId> ids) {
        List<String> sorted = ids.stream().sorted().map(id -> resourceName(model, id)).toList();
        if (sorted.size() == 2) {
            return sorted.get(0) + " or " + sorted.get(1);
        }
        return String.join(", ", sorted.subList(0, sorted.size() - 1)) + " or " + sorted.getLast();
    }

    static String units(List<Integer> units) {
        List<Long> asLongs = units.stream().map(Integer::longValue).toList();
        return (units.size() == 1 ? "unit " : "units ") + GameDiagnostics.compactRanges(asLongs).replace(",", ", ");
    }

    static long tickOf(Attempt attempt, String label) {
        return attempt.evidence().observation(label).metadata().currentTime().value();
    }

    // ---------------------------------------------------------------- Q1: waiting work, step first

    static List<Statement> waiting(Attempt attempt, String label) {
        RuntimeObservation observation = attempt.evidence().observation(label);
        FactoryModel model = attempt.evidence().publishedModel();
        long tick = tickOf(attempt, label);
        OracleOutcome<List<WaitingAtStep>> outcome = new WaitingWorkByStepOracle(label).evaluateOn(attempt.evidence());
        List<Statement> statements = new ArrayList<>();
        switch (outcome) {
            case OracleOutcome.Underdetermined<List<WaitingAtStep>> refused -> statements.add(Statement.of(
                            Question.Q1_WAITING, Kind.REFUSAL, "waiting@" + label,
                            "At tick " + tick + ", waiting work cannot be grouped by step: " + refused.reasons())
                    .method(WaitingWorkByStepOracle.DEFINITION.name())
                    .support(Support.observation(label))
                    .facet("refusalOf", "waiting")
                    .build());
            case OracleOutcome.Derived<List<WaitingAtStep>> derived -> {
                if (derived.value().isEmpty()) {
                    statements.add(Statement.of(Question.Q1_WAITING, Kind.BOUNDARY_COUNT, "waiting@" + label,
                                    "At tick " + tick + ", no unit is waiting to start any step.")
                            .method(WaitingWorkByStepOracle.DEFINITION.name())
                            .support(Support.observation(label, "JobObservation.status", "JobObservation.currentStep"))
                            .facet("waitingUnits", 0)
                            .build());
                }
                for (WaitingAtStep step : derived.value()) {
                    List<Integer> waitingUnits = observation.jobs().stream()
                            .filter(job -> job.status() == JobStatus.Queued && job.currentStep() == step.stepId() - 1)
                            .map(job -> (int) job.ordinalWithinOrder() + 1)
                            .sorted()
                            .toList();
                    Statement.Builder builder;
                    if (step.attribution() instanceof Attribution.SingleResource single) {
                        String resource = resourceName(model, single.machineId());
                        builder = Statement.of(Question.Q1_WAITING, Kind.BOUNDARY_COUNT,
                                        "waiting@" + label + ":" + step.stepName(),
                                        "At tick " + tick + ", " + count(step.waitingJobs(), waitingUnits)
                                                + " waiting to start " + step.stepName() + " on " + resource + ".")
                                .facet("attributedResource", resource);
                    } else {
                        Attribution.SharedEligibleSet shared = (Attribution.SharedEligibleSet) step.attribution();
                        builder = Statement.of(Question.Q1_WAITING, Kind.BOUNDARY_COUNT,
                                "waiting@" + label + ":" + step.stepName(),
                                "At tick " + tick + ", " + count(step.waitingJobs(), waitingUnits)
                                        + " waiting to start " + step.stepName() + "; each goes to whichever of "
                                        + names(model, shared.eligibleMachines())
                                        + " can take it first and is not assigned to either yet.");
                    }
                    statements.add(builder
                            .method(WaitingWorkByStepOracle.DEFINITION.name())
                            .support(Support.observation(label, "JobObservation.status", "JobObservation.currentStep",
                                    "PendingWorkObservation", "ResourceObservation.queueDepth")
                                    .plus(Support.design("eligible resources of " + step.stepName())))
                            .facet("step", step.stepName())
                            .facet("waitingUnits", step.waitingJobs())
                            .build());
                }
            }
        }
        return statements;
    }

    private static String count(long count, List<Integer> waitingUnits) {
        return (count == 1 ? "1 unit (" : count + " units (") + units(waitingUnits) + (count == 1 ? ") is" : ") are");
    }

    // ---------------------------------------------------------------- resource activity and idleness

    static List<Statement> resourceActivity(Attempt attempt, String label) {
        RuntimeObservation observation = attempt.evidence().observation(label);
        FactoryModel model = attempt.evidence().publishedModel();
        long tick = tickOf(attempt, label);
        List<Statement> statements = new ArrayList<>();
        for (ResourceObservation resource : observation.resources()) {
            String steps = String.join("+", stepsServedBy(model, resource.machineId()));
            String text = resource.state() == MachineState.Offline
                    ? "At tick " + tick + ", " + resource.name() + " (" + steps + ") is offline."
                    : "At tick " + tick + ", " + resource.name() + " (" + steps + ") has " + resource.activeJobIds().size()
                            + " of " + resource.concurrency() + (resource.concurrency() == 1 ? " slot" : " slots") + " in use.";
            statements.add(Statement.of(Question.ACTIVITY, Kind.DIRECT_FACT, "activity@" + label + ":" + resource.name(), text)
                    .support(Support.observation(label, "ResourceObservation.state", "ResourceObservation.activeJobIds",
                            "ResourceObservation.concurrency"))
                    .facet("resource", resource.name())
                    .build());
        }
        return statements;
    }

    /** Idle resources and whether any waiting work could use them: facts, never a verdict. */
    static List<Statement> idleness(Attempt attempt, String label) {
        FactoryModel model = attempt.evidence().publishedModel();
        long tick = tickOf(attempt, label);
        String method = GameEvidenceOracles.IDLENESS.name();
        OracleOutcome<GameEvidenceOracles.Idleness> outcome =
                new GameEvidenceOracles.IdleResources(label).evaluateOn(attempt.evidence());
        if (outcome instanceof OracleOutcome.Underdetermined<GameEvidenceOracles.Idleness> refused) {
            return List.of(Statement.of(Question.Q3_IDLE_RESOURCE, Kind.REFUSAL, "idle@" + label,
                            "At tick " + tick + ", idle resources cannot be related to waiting work: " + refused.reasons())
                    .method(method)
                    .support(Support.observation(label))
                    .facet("refusalOf", "idleness")
                    .build());
        }
        GameEvidenceOracles.Idleness idleness = ((OracleOutcome.Derived<GameEvidenceOracles.Idleness>) outcome).value();
        if (idleness.orderComplete()) {
            return List.of(Statement.of(Question.Q3_IDLE_RESOURCE, Kind.BOUNDARY_COUNT, "idle@" + label,
                            "At tick " + tick + ", all " + idleness.requestedQuantity()
                                    + " units are complete and every resource is idle.")
                    .method(method)
                    .support(Support.observation(label, "OrderObservation.complete", "ResourceObservation.activeJobIds"))
                    .build());
        }
        List<Statement> statements = new ArrayList<>();
        for (GameEvidenceOracles.IdleResource resource : idleness.idle()) {
            String name = resourceName(model, resource.machineId());
            List<String> usable = resource.waitingStepsServed();
            String text = usable.isEmpty()
                    ? "At tick " + tick + ", " + name + " is idle, and no unit is waiting for a step it can serve ("
                            + String.join(", ", resource.stepsServed()) + ")."
                    : "At tick " + tick + ", " + name + " is idle while units wait for " + String.join(", ", usable) + ".";
            statements.add(Statement.of(Question.Q3_IDLE_RESOURCE, Kind.BOUNDARY_COUNT, "idle@" + label + ":" + name, text)
                    .method(method)
                    .support(Support.observation(label, "ResourceObservation.state", "ResourceObservation.activeJobIds",
                                    "JobObservation.status", "JobObservation.currentStep")
                            .plus(Support.design("steps " + name + " is eligible for")))
                    .facet("resource", name)
                    .facet("idleWithoutWork", usable.isEmpty())
                    .build());
        }
        return statements;
    }

    /** Explicit refusal of a surplus verdict for every idle resource a single run cannot settle. */
    static List<Statement> surplusRefusals(Attempt attempt, String label) {
        List<Statement> refusals = new ArrayList<>();
        for (Statement idle : idleness(attempt, label)) {
            if (idle.facet("idleWithoutWork").filter("true"::equals).isPresent()) {
                String resource = idle.facet("resource").orElseThrow();
                refusals.add(Statement.of(Question.Q3_IDLE_RESOURCE, Kind.REFUSAL, "surplus@" + label + ":" + resource,
                                "Whether " + resource + " is surplus -- whether the order would finish as early without it --"
                                        + " is not decidable from this run; a retry without it can show that.")
                        .support(idle.support())
                        .facet("refusalOf", "surplus")
                        .facet("resource", resource)
                        .build());
            }
        }
        return refusals;
    }

    // ---------------------------------------------------------------- progress and outcome (direct facts)

    static Statement progress(Attempt attempt, String label) {
        OrderObservation order = attempt.evidence().observation(label).orders().getFirst();
        return Statement.of(Question.PROGRESS, Kind.DIRECT_FACT, "progress@" + label,
                        "At tick " + tickOf(attempt, label) + ", " + order.completedQuantity() + " of " + order.requestedQuantity()
                                + " units are complete.")
                .support(Support.observation(label, "OrderObservation.completedQuantity", "OrderObservation.requestedQuantity"))
                .build();
    }

    static Statement outcome(Attempt attempt) {
        OrderObservation order = attempt.evidence().observation(ExperimentEvidence.CLOSING_LABEL).orders().getFirst();
        if (!order.complete()) {
            return Statement.of(Question.PROGRESS, Kind.DIRECT_FACT, "outcome", "The order has not completed.")
                    .support(Support.observation(ExperimentEvidence.CLOSING_LABEL, "OrderObservation.complete"))
                    .build();
        }
        return Statement.of(Question.PROGRESS, Kind.DIRECT_FACT, "outcome",
                        "The order completed at tick " + order.completedAt().value() + ".")
                .support(Support.observation(ExperimentEvidence.CLOSING_LABEL, "OrderObservation.completedAt"))
                .facet("completionTick", order.completedAt().value())
                .build();
    }

    static long completionTick(Attempt attempt) {
        return attempt.evidence().observation(ExperimentEvidence.CLOSING_LABEL).orders().getFirst().completedAt().value();
    }

    // ---------------------------------------------------------------- Q4: event intervals

    static Statement completingUnit(Attempt attempt) {
        FactoryModel model = attempt.evidence().publishedModel();
        OracleOutcome<CompletingUnit> outcome = new GameEvidenceOracles.CompletingUnitTimeline().evaluateOn(attempt.evidence());
        return switch (outcome) {
            case OracleOutcome.Underdetermined<CompletingUnit> refused -> intervalRefusal(Question.Q4_DELAY,
                    "completing-unit", "The last unit's waiting and processing times are not available", refused.reasons());
            case OracleOutcome.Derived<CompletingUnit> derived -> {
                CompletingUnit unit = derived.value();
                List<String> parts = new ArrayList<>();
                UnitSegment longestWait = null;
                for (UnitSegment segment : unit.segments()) {
                    String step = segment.occurrence().step().name();
                    parts.add("waited " + segment.waitTicks() + " for " + step + ", " + step + " " + segment.processTicks()
                            + " on " + resourceName(model, segment.occurrence().machineId()));
                    if (longestWait == null || segment.waitTicks() > longestWait.waitTicks()) {
                        longestWait = segment;
                    }
                }
                yield Statement.of(Question.Q4_DELAY, Kind.EVENT_INTERVAL, "completing-unit",
                                "The last unit to finish (unit " + unit.unit() + ") took " + unit.leadTime()
                                        + " ticks from order acceptance: " + String.join("; ", parts) + ".")
                        .method(GameEvidenceOracles.COMPLETING_UNIT.name())
                        .support(Support.events(unit.sequences(), "ORDER_ACCEPTED", "JOB_DISPATCHED", "JOB_STEP_COMPLETED",
                                "ORDER_COMPLETED.jobId"))
                        .facet("leadTime", unit.leadTime())
                        .facet("longestWaitStep", longestWait.occurrence().step().name())
                        .facet("longestWaitTicks", longestWait.waitTicks())
                        .build();
            }
        };
    }

    static List<Statement> activityTimeline(Attempt attempt) {
        FactoryModel model = attempt.evidence().publishedModel();
        OracleOutcome<Occurrences> outcome =
                new GameEvidenceOracles.StepOccurrences(ExperimentEvidence.CLOSING_LABEL).evaluateOn(attempt.evidence());
        if (outcome instanceof OracleOutcome.Underdetermined<Occurrences> refused) {
            return List.of(intervalRefusal(Question.ACTIVITY, "timeline", "Resource activity over time is not available",
                    refused.reasons()));
        }
        Occurrences occurrences = ((OracleOutcome.Derived<Occurrences>) outcome).value();
        List<Statement> statements = new ArrayList<>();
        for (ConfiguredResource resource : model.resources()) {
            List<Occurrence> mine = occurrences.onMachine(resource.id());
            if (mine.isEmpty()) {
                statements.add(Statement.of(Question.ACTIVITY, Kind.EVENT_INTERVAL, "timeline:" + resource.name(),
                                resource.name() + " processed nothing in this run.")
                        .method(GameEvidence.OCCURRENCES.name())
                        .support(Support.events(List.of(occurrences.acceptedSequence(), occurrences.boundarySequence()),
                                "JOB_DISPATCHED"))
                        .facet("resource", resource.name())
                        .build());
                continue;
            }
            Map<String, Long> byStep = new LinkedHashMap<>();
            mine.forEach(o -> byStep.merge(o.step().name(), 1L, Long::sum));
            List<long[]> merged = mergedIntervals(mine, occurrences.boundaryTick());
            int maxActive = maxActive(mine, occurrences.boundaryTick());
            String processed = byStep.entrySet().stream()
                    .map(e -> e.getValue() + " " + e.getKey())
                    .collect(Collectors.joining(" and "));
            String busy = merged.stream().map(i -> i[0] + "-" + i[1]).collect(Collectors.joining(", "));
            String slots = resource.concurrency() > 1
                    ? "; at most " + maxActive + " of " + resource.concurrency() + " slots in use at once"
                    : "";
            List<Long> sequences = new ArrayList<>();
            mine.forEach(o -> {
                sequences.add(o.dispatchSequence());
                o.completionSequence().ifPresent(sequences::add);
            });
            statements.add(Statement.of(Question.ACTIVITY, Kind.EVENT_INTERVAL, "timeline:" + resource.name(),
                            resource.name() + " processed " + processed + " step" + (mine.size() == 1 ? "" : "s")
                                    + "; in use during ticks " + busy + slots + ".")
                    .method(GameEvidence.OCCURRENCES.name())
                    .support(Support.events(sequences, "JOB_DISPATCHED", "JOB_STEP_COMPLETED"))
                    .facet("resource", resource.name())
                    .build());
        }
        return statements;
    }

    static List<long[]> mergedIntervals(List<Occurrence> occurrences, long boundaryTick) {
        List<long[]> intervals = occurrences.stream()
                .map(o -> new long[] {o.dispatchTick(), o.endOr(boundaryTick)})
                .sorted(Comparator.comparingLong(i -> i[0]))
                .toList();
        List<long[]> merged = new ArrayList<>();
        for (long[] interval : intervals) {
            if (!merged.isEmpty() && interval[0] <= merged.getLast()[1]) {
                merged.getLast()[1] = Math.max(merged.getLast()[1], interval[1]);
            } else {
                merged.add(new long[] {interval[0], interval[1]});
            }
        }
        return merged;
    }

    static int maxActive(List<Occurrence> occurrences, long boundaryTick) {
        TreeMap<Long, Integer> delta = new TreeMap<>();
        for (Occurrence o : occurrences) {
            delta.merge(o.dispatchTick(), 1, Integer::sum);
            delta.merge(o.endOr(boundaryTick), -1, Integer::sum);
        }
        int active = 0;
        int max = 0;
        for (int change : delta.values()) {
            active += change;
            max = Math.max(max, active);
        }
        return max;
    }

    static Statement stepWaitTotals(Attempt attempt) {
        OracleOutcome<DispatchProfileOracle.DispatchProfile> outcome =
                new DispatchProfileOracle(ExperimentEvidence.CLOSING_LABEL).evaluateOn(attempt.evidence());
        return switch (outcome) {
            case OracleOutcome.Underdetermined<DispatchProfileOracle.DispatchProfile> refused -> intervalRefusal(
                    Question.Q4_DELAY, "step-wait-totals", "Waiting summed over all units is not available", refused.reasons());
            case OracleOutcome.Derived<DispatchProfileOracle.DispatchProfile> derived -> {
                List<String> parts = derived.value().waits().stream()
                        .map(w -> w.step().name() + " " + w.totalWaitTicks() + " ticks over " + w.dispatches() + " units")
                        .toList();
                DispatchProfileOracle.StepWait largest = derived.value().waits().stream()
                        .max(Comparator.comparingLong(DispatchProfileOracle.StepWait::totalWaitTicks))
                        .orElseThrow();
                EvidenceSupport support = derived.support();
                List<Long> range = support.events().map(r -> List.of(r.from(), r.through())).orElse(List.of());
                yield Statement.of(Question.Q4_DELAY, Kind.AGGREGATE_MEASUREMENT, "step-wait-totals",
                                "Waiting before each step, summed over every unit of the completed order: "
                                        + String.join("; ", parts) + ".")
                        .method(DispatchProfileOracle.DEFINITION.name())
                        .support(Support.events(range, "ORDER_ACCEPTED", "JOB_DISPATCHED", "JOB_STEP_COMPLETED")
                                .plus(Support.observation(ExperimentEvidence.CLOSING_LABEL)))
                        .facet("largestWaitStep", largest.step().name())
                        .facet("eventRange", "true")
                        .build();
            }
        };
    }

    static Statement intervalRefusal(Question question, String subject, String what, List<String> reasons) {
        String readable = String.join("; ", reasons)
                .replaceAll("SequenceRange\\[from=(\\d+), through=(\\d+)\\]", "$1-$2");
        return Statement.of(question, Kind.REFUSAL, subject, what + ": " + readable + ".")
                .support(Support.observation(ExperimentEvidence.CLOSING_LABEL))
                .facet("refusalOf", "interval-measurement")
                .build();
    }

    // ---------------------------------------------------------------- Q2: named methods

    static Statement poolOccupancyBottleneck(Attempt attempt) {
        OracleOutcome<PoolOccupancies> outcome =
                new EligibilityPoolOccupancyOracle(ExperimentEvidence.CLOSING_LABEL).evaluateOn(attempt.evidence());
        if (outcome instanceof OracleOutcome.Underdetermined<PoolOccupancies> refused) {
            return intervalRefusal(Question.Q2_LIMITING_STEP, "limiting-step", "Pool occupancy is not available",
                    refused.reasons());
        }
        PoolOccupancies pools = ((OracleOutcome.Derived<PoolOccupancies>) outcome).value();
        Set<EligibilityPool> maximum = pools.maximumOccupancyPools();
        List<String> verdict = new ArrayList<>();
        List<String> numbers = new ArrayList<>();
        for (PoolOccupancy pool : pools.pools()) {
            String steps = pool.pool().steps().stream().map(OperationStep::name).collect(Collectors.joining("+"));
            numbers.add(steps + " " + pool.occupiedJobTicks() + " of " + pool.capacityJobTicks() + " job-ticks");
            if (maximum.contains(pool.pool())) {
                verdict.add(steps);
            }
        }
        EvidenceSupport support = ((OracleOutcome.Derived<PoolOccupancies>) outcome).support();
        List<Long> range = support.events().map(r -> List.of(r.from(), r.through())).orElse(List.of());
        return Statement.of(Question.Q2_LIMITING_STEP, Kind.NAMED_INTERPRETATION, "limiting-step",
                        "Bottleneck (method: most occupied eligibility pool): " + String.join(" and ", verdict) + " -- "
                                + String.join("; ", numbers) + ".")
                .method(EligibilityPoolOccupancyOracle.DEFINITION.name())
                .support(Support.events(range, "JOB_DISPATCHED", "JOB_STEP_COMPLETED")
                        .plus(Support.observation(ExperimentEvidence.CLOSING_LABEL, "ResourceObservation.concurrency")))
                .facet("verdictStep", String.join("|", verdict))
                .facet("verdictWord", "bottleneck")
                .build();
    }

    /** The completion-chain method: a descriptive pacing step, or an explicit refusal at a tie. */
    static Statement completionChain(Attempt attempt) {
        FactoryModel model = attempt.evidence().publishedModel();
        OracleOutcome<CompletionChain> outcome = new GameEvidenceOracles.CompletionChainTrace().evaluateOn(attempt.evidence());
        if (outcome instanceof OracleOutcome.Underdetermined<CompletionChain> refused) {
            return intervalRefusal(Question.Q2_LIMITING_STEP, "limiting-step", "The completion chain is not available",
                    refused.reasons());
        }
        CompletionChain chain = ((OracleOutcome.Derived<CompletionChain>) outcome).value();
        Support support = Support.events(chain.sequences(), "ORDER_COMPLETED.jobId", "JOB_DISPATCHED", "JOB_STEP_COMPLETED");
        Optional<OperationStep> pacing = chain.uniquePacingStep();
        if (pacing.isPresent()) {
            OperationStep step = pacing.get();
            long[] span = chain.span(step);
            Set<MachineId> machines = chain.capacityResources(step);
            String resources = machines.stream().sorted().map(id -> resourceName(model, id)).collect(Collectors.joining(", "));
            return Statement.of(Question.Q2_LIMITING_STEP, Kind.NAMED_INTERPRETATION, "limiting-step",
                            "Pacing step (method: completion chain): " + step.name() + ". Tracing the last unit back, every"
                                    + " step that started later than its unit was ready waited for " + step.name() + " on "
                                    + resources + ", which worked back to back from tick " + span[0] + " to " + span[1]
                                    + " (" + chain.capacityWaitsByStep().get(step) + " such waits).")
                    .method(GameEvidenceOracles.COMPLETION_CHAIN.name())
                    .support(support)
                    .facet("verdictStep", step.name())
                    .facet("verdictWord", "pacing")
                    .build();
        }
        String reason;
        if (!chain.ties().isEmpty()) {
            ChainTie tie = chain.ties().getFirst();
            reason = "at tick " + tie.occurrence().dispatchTick() + ", unit " + tie.occurrence().unit() + " became ready for "
                    + tie.occurrence().step().name() + " at the same tick " + resourceName(model, tie.occurrence().machineId())
                    + " was released by unit " + tie.releasedBy().unit() + " (" + tie.releasedBy().step().name()
                    + "), so both held it up";
            if (chain.ties().size() > 1) {
                reason += "; " + (chain.ties().size() - 1) + " more such tie" + (chain.ties().size() > 2 ? "s" : "")
                        + " on the chain";
            }
        } else if (chain.capacityWaitsByStep().isEmpty()) {
            reason = "no step on the chain waited for capacity";
        } else {
            reason = "steps on the chain waited for capacity at " + chain.capacityWaitsByStep().keySet().stream()
                    .map(OperationStep::name).collect(Collectors.joining(" and "));
        }
        return Statement.of(Question.Q2_LIMITING_STEP, Kind.REFUSAL, "limiting-step",
                        "Pacing step (method: completion chain): not unique -- " + reason + ".")
                .method(GameEvidenceOracles.COMPLETION_CHAIN.name())
                .support(support)
                .facet("refusalOf", "limiting-step")
                .build();
    }

    static Statement singleRunLimitingStepRefusal() {
        return Statement.of(Question.Q2_LIMITING_STEP, Kind.REFUSAL, "limiting-step",
                        "Which step limits this design is not decidable from one run. The last unit's times and each"
                                + " resource's activity above are measurements, not that answer; a retry that changes one"
                                + " resource shows whether that change alters completion.")
                .facet("refusalOf", "limiting-step")
                .build();
    }

    static Statement counterfactualRefusal(String step) {
        return Statement.of(Question.Q2_LIMITING_STEP, Kind.REFUSAL, "limiting-step-counterfactual",
                        "Whether adding " + step + " capacity would finish sooner is not decidable from this run; a retry"
                                + " that changes only that can show it.")
                .facet("refusalOf", "counterfactual")
                .build();
    }

    // ---------------------------------------------------------------- Q5/Q6: comparisons

    static Statement changeAndOutcome(Attempt first, Attempt second, List<Change> changes, Question question) {
        long before = completionTick(first);
        long after = completionTick(second);
        String outcome = "Completion: tick " + before + " -> tick " + after + " (" + delta(before, after) + ").";
        String changeText = changes.isEmpty()
                ? "No authored change."
                : (changes.size() == 1 ? "One change: " : changes.size() + " changes: ")
                        + changes.stream().map(Change::describe).collect(Collectors.joining("; ")) + ".";
        String surplus = "";
        if (changes.size() == 1 && changes.getFirst().type() == ChangeType.REMOVED) {
            surplus = after == before
                    ? " Without " + changes.getFirst().resource() + " the order still completed at tick " + after + "."
                    : " Without " + changes.getFirst().resource() + " the order completed " + Math.abs(after - before)
                            + " ticks " + (after > before ? "later" : "earlier") + ".";
        }
        return Statement.of(question, Kind.COMPARISON, "comparison", changeText + " " + outcome + surplus)
                .support(Support.observation(ExperimentEvidence.CLOSING_LABEL, "OrderObservation.completedAt")
                        .plus(Support.design("authored design of both attempts, including resource order")))
                .facet("changeCount", changes.size())
                .facet("completionDelta", after - before)
                .facet("changes", changes.stream().map(c -> c.type().name()).collect(Collectors.joining(",")))
                .build();
    }

    static String delta(long before, long after) {
        if (after == before) {
            return "no change";
        }
        return Math.abs(after - before) + (after < before ? " ticks earlier" : " ticks later");
    }

    static Statement tieBreakRule() {
        return Statement.of(Question.Q5_CONTROLLED_COMPARISON, Kind.DIRECT_FACT, "tie-break-rule",
                        "When eligible resources are otherwise equally placed to take work, it goes to the one with the"
                                + " lower resource number (in these designs, the one listed earlier), so resource order alone"
                                + " can change the outcome.")
                .support(Support.design("Engine semantics section 2 rule 4: remaining ties broken by MachineId"))
                .build();
    }

    static Statement attributionRefusal(List<Change> changes, long before, long after) {
        return Statement.of(Question.Q6_CONFOUNDED_COMPARISON, Kind.REFUSAL, "attribution",
                        "How much of the difference (" + delta(before, after) + ") each of the " + changes.size()
                                + " changes accounts for is not attributable from this pair; compare one change at a time.")
                .facet("refusalOf", "attribution")
                .build();
    }

    static Statement mechanismRefusal() {
        return Statement.of(Question.Q5_CONTROLLED_COMPARISON, Kind.REFUSAL, "mechanism",
                        "This pair shows the change and the outcome together; it does not show why.")
                .facet("refusalOf", "mechanism")
                .build();
    }

    // ---------------------------------------------------------------- the naive anti-candidate's sources

    static List<JobObservation> queued(RuntimeObservation observation) {
        return observation.jobs().stream().filter(job -> job.status() == JobStatus.Queued).toList();
    }
}
