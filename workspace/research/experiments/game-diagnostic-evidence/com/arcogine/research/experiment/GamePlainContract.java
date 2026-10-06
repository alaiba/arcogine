package com.arcogine.research.experiment;

import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.process.OrderObservation;
import com.arcogine.factory.process.ResourceObservation;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.research.experiment.GameDiagnostics.Attempt;
import com.arcogine.research.experiment.GameDiagnostics.Change;
import com.arcogine.research.experiment.GameDiagnostics.ChangeType;
import com.arcogine.research.experiment.GameDiagnostics.Contract;
import com.arcogine.research.experiment.GameDiagnostics.Kind;
import com.arcogine.research.experiment.GameDiagnostics.Question;
import com.arcogine.research.experiment.GameDiagnostics.Statement;
import com.arcogine.research.experiment.GameDiagnostics.Support;
import com.arcogine.research.experiment.GameEvidence.Occurrence;
import com.arcogine.research.experiment.GameEvidence.Occurrences;
import com.arcogine.research.experiment.GameEvidenceOracles.CompletingUnit;
import com.arcogine.research.experiment.GameEvidenceOracles.UnitSegment;
import com.arcogine.research.experiment.WaitingWorkByStepOracle.Attribution;
import com.arcogine.research.experiment.WaitingWorkByStepOracle.WaitingAtStep;
import com.arcogine.types.JobStatus;
import com.arcogine.types.MachineId;
import com.arcogine.types.MachineState;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * C3c, the "plain bundle": C3b's evidence rules with the player-facing wording revised after the
 * first owner walkthrough. Every statement keeps C3b's kind, derivation, cited evidence and audit
 * facets; only its text, its grouping ({@code section}) and three content choices change: no closing
 * idleness line, a design fact instead of a refusal for a machine that is the only one for a step, and
 * a caveat on the last unit's longest wait. Research custody only; not a game API.
 */
final class GamePlainContract {

    static final String SNAPSHOT = "snapshot";
    static final String RESULT = "result";
    static final String MORE = "more";
    static final String CANNOT_TELL = "cannot-tell";
    static final String COMPARISON = "comparison";

    private GamePlainContract() {}

    static final class PlainBundle implements Contract {

        @Override
        public String id() {
            return "C3c-plain-bundle";
        }

        @Override
        public String summary() {
            return "C3b's evidence with revised wording: 'needed' instead of 'surplus', the only machine for a step"
                    + " stated as a design fact, a caveat on the longest wait, plain refusals grouped as 'what this run"
                    + " cannot tell you', and work timelines on request.";
        }

        @Override
        public List<Statement> attempt(Attempt attempt) {
            List<Statement> statements = new ArrayList<>();
            attempt.midRunLabel().ifPresent(label -> {
                statements.addAll(waiting(attempt, label));
                statements.addAll(machines(attempt, label));
                statements.addAll(idle(attempt, label));
                statements.add(progress(attempt, label));
            });
            statements.add(outcome(attempt));
            statements.add(lastUnit(attempt));
            statements.addAll(timeline(attempt));
            attempt.midRunLabel().ifPresent(label -> statements.addAll(needed(attempt, label)));
            statements.add(limitingRefusal());
            return statements;
        }

        @Override
        public List<Statement> compare(Attempt first, Attempt second) {
            List<Change> changes = GameDiagnostics.changeSet(first.design(), second.design());
            Question question = changes.size() > 1 ? Question.Q6_CONFOUNDED_COMPARISON : Question.Q5_CONTROLLED_COMPARISON;
            List<Statement> statements = new ArrayList<>();
            statements.add(changeAndOutcome(first, second, changes, question));
            if (changes.size() > 1) {
                statements.add(Statement.of(Question.Q6_CONFOUNDED_COMPARISON, Kind.REFUSAL, "attribution",
                                "This pair cannot tell how much each change contributed. Try them one at a time.")
                        .facet("refusalOf", "attribution")
                        .facet("section", COMPARISON)
                        .build());
                return statements;
            }
            if (changes.stream().anyMatch(change -> change.type() == ChangeType.ORDER)) {
                statements.add(Statement.of(Question.Q5_CONTROLLED_COMPARISON, Kind.DIRECT_FACT, "tie-break-rule",
                                "When several machines could take the same work and are otherwise equal, the"
                                        + " lower-numbered one (listed earlier) takes it, so the order of machines alone can"
                                        + " change the result.")
                        .support(Support.design("Engine semantics section 2 rule 4: remaining ties broken by MachineId"))
                        .facet("section", COMPARISON)
                        .build());
            }
            statements.add(Statement.of(Question.Q5_CONTROLLED_COMPARISON, Kind.REFUSAL, "mechanism",
                            "This shows what happened with this change, not why.")
                    .facet("refusalOf", "mechanism")
                    .facet("section", COMPARISON)
                    .build());
            return statements;
        }
    }

    // ---------------------------------------------------------------- snapshot

    static List<Statement> waiting(Attempt attempt, String label) {
        RuntimeObservation observation = attempt.evidence().observation(label);
        FactoryModel model = attempt.evidence().publishedModel();
        long tick = GameStatements.tickOf(attempt, label);
        OracleOutcome<List<WaitingAtStep>> outcome = new WaitingWorkByStepOracle(label).evaluateOn(attempt.evidence());
        List<Statement> statements = new ArrayList<>();
        switch (outcome) {
            case OracleOutcome.Underdetermined<List<WaitingAtStep>> refused -> statements.add(Statement.of(
                            Question.Q1_WAITING, Kind.REFUSAL, "waiting@" + label,
                            "At tick " + tick + ", waiting units cannot be counted by step: " + String.join("; ", refused.reasons()))
                    .method(WaitingWorkByStepOracle.DEFINITION.name())
                    .support(Support.observation(label))
                    .facet("refusalOf", "waiting")
                    .facet("section", SNAPSHOT)
                    .build());
            case OracleOutcome.Derived<List<WaitingAtStep>> derived -> {
                if (derived.value().isEmpty()) {
                    statements.add(Statement.of(Question.Q1_WAITING, Kind.BOUNDARY_COUNT, "waiting@" + label,
                                    "At tick " + tick + ", no unit is waiting to start any step.")
                            .method(WaitingWorkByStepOracle.DEFINITION.name())
                            .support(Support.observation(label, "JobObservation.status", "JobObservation.currentStep"))
                            .facet("waitingUnits", 0)
                            .facet("section", SNAPSHOT)
                            .build());
                }
                for (WaitingAtStep step : derived.value()) {
                    List<Integer> units = observation.jobs().stream()
                            .filter(job -> job.status() == JobStatus.Queued && job.currentStep() == step.stepId() - 1)
                            .map(job -> (int) job.ordinalWithinOrder() + 1)
                            .sorted()
                            .toList();
                    String counted = step.waitingJobs() == 1
                            ? "1 unit (" + GameStatements.units(units) + ") is"
                            : step.waitingJobs() + " units (" + GameStatements.units(units) + ") are";
                    Statement.Builder builder;
                    if (step.attribution() instanceof Attribution.SingleResource single) {
                        String resource = GameStatements.resourceName(model, single.machineId());
                        builder = Statement.of(Question.Q1_WAITING, Kind.BOUNDARY_COUNT, "waiting@" + label + ":" + step.stepName(),
                                        "At tick " + tick + ", " + counted + " waiting to start " + step.stepName() + " on "
                                                + resource + ".")
                                .facet("attributedResource", resource);
                    } else {
                        Attribution.SharedEligibleSet shared = (Attribution.SharedEligibleSet) step.attribution();
                        builder = Statement.of(Question.Q1_WAITING, Kind.BOUNDARY_COUNT, "waiting@" + label + ":" + step.stepName(),
                                "At tick " + tick + ", " + counted + " waiting to start " + step.stepName()
                                        + ". They are not assigned to a machine yet; " + anyOf(model, shared.eligibleMachines())
                                        + " may take them when it becomes free.");
                    }
                    statements.add(builder
                            .method(WaitingWorkByStepOracle.DEFINITION.name())
                            .support(Support.observation(label, "JobObservation.status", "JobObservation.currentStep",
                                            "PendingWorkObservation", "ResourceObservation.queueDepth")
                                    .plus(Support.design("eligible resources of " + step.stepName())))
                            .facet("step", step.stepName())
                            .facet("waitingUnits", step.waitingJobs())
                            .facet("section", SNAPSHOT)
                            .build());
                }
            }
        }
        return statements;
    }

    private static String anyOf(FactoryModel model, Set<MachineId> ids) {
        List<String> names = ids.stream().sorted().map(id -> GameStatements.resourceName(model, id)).toList();
        if (names.size() == 2) {
            return "either " + names.get(0) + " or " + names.get(1);
        }
        return "any of " + String.join(", ", names.subList(0, names.size() - 1)) + " or " + names.getLast();
    }

    /** Machines that are working or offline at the boundary; idle machines get their own line. */
    static List<Statement> machines(Attempt attempt, String label) {
        RuntimeObservation observation = attempt.evidence().observation(label);
        FactoryModel model = attempt.evidence().publishedModel();
        long tick = GameStatements.tickOf(attempt, label);
        List<Statement> statements = new ArrayList<>();
        for (ResourceObservation resource : observation.resources()) {
            boolean offline = resource.state() == MachineState.Offline;
            if (!offline && resource.activeJobIds().isEmpty()) {
                continue;
            }
            String steps = String.join("+", GameStatements.stepsServedBy(model, resource.machineId()));
            String text;
            if (offline) {
                text = "At tick " + tick + ", " + resource.name() + " (" + steps + ") is offline.";
            } else if (resource.concurrency() == 1) {
                text = "At tick " + tick + ", " + resource.name() + " (" + steps + ") is busy.";
            } else {
                text = "At tick " + tick + ", " + resource.name() + " (" + steps + ") has " + resource.activeJobIds().size()
                        + " of " + resource.concurrency() + " slots busy.";
            }
            statements.add(Statement.of(Question.ACTIVITY, Kind.DIRECT_FACT, "activity@" + label + ":" + resource.name(), text)
                    .support(Support.observation(label, "ResourceObservation.state", "ResourceObservation.activeJobIds",
                            "ResourceObservation.concurrency"))
                    .facet("resource", resource.name())
                    .facet("section", SNAPSHOT)
                    .build());
        }
        return statements;
    }

    static List<Statement> idle(Attempt attempt, String label) {
        FactoryModel model = attempt.evidence().publishedModel();
        long tick = GameStatements.tickOf(attempt, label);
        String method = GameEvidenceOracles.IDLENESS.name();
        OracleOutcome<GameEvidenceOracles.Idleness> outcome =
                new GameEvidenceOracles.IdleResources(label).evaluateOn(attempt.evidence());
        if (outcome instanceof OracleOutcome.Underdetermined<GameEvidenceOracles.Idleness> refused) {
            return List.of(Statement.of(Question.Q3_IDLE_RESOURCE, Kind.REFUSAL, "idle@" + label,
                            "At tick " + tick + ", idle machines cannot be related to waiting units: "
                                    + String.join("; ", refused.reasons()))
                    .method(method)
                    .support(Support.observation(label))
                    .facet("refusalOf", "idleness")
                    .facet("section", SNAPSHOT)
                    .build());
        }
        GameEvidenceOracles.Idleness idleness = ((OracleOutcome.Derived<GameEvidenceOracles.Idleness>) outcome).value();
        if (idleness.orderComplete()) {
            return List.of();
        }
        List<Statement> statements = new ArrayList<>();
        for (GameEvidenceOracles.IdleResource resource : idleness.idle()) {
            String name = GameStatements.resourceName(model, resource.machineId());
            List<String> usable = resource.waitingStepsServed();
            String text = usable.isEmpty()
                    ? "At tick " + tick + ", " + name + " is idle and no unit is waiting for "
                            + String.join(" or ", resource.stepsServed()) + "."
                    : "At tick " + tick + ", " + name + " is idle while units wait for " + String.join(" and ", usable) + ".";
            statements.add(Statement.of(Question.Q3_IDLE_RESOURCE, Kind.BOUNDARY_COUNT, "idle@" + label + ":" + name, text)
                    .method(method)
                    .support(Support.observation(label, "ResourceObservation.state", "ResourceObservation.activeJobIds",
                                    "JobObservation.status", "JobObservation.currentStep")
                            .plus(Support.design("steps " + name + " is eligible for")))
                    .facet("resource", name)
                    .facet("idleWithoutWork", usable.isEmpty())
                    .facet("section", SNAPSHOT)
                    .build());
        }
        return statements;
    }

    static Statement progress(Attempt attempt, String label) {
        OrderObservation order = attempt.evidence().observation(label).orders().getFirst();
        return Statement.of(Question.PROGRESS, Kind.DIRECT_FACT, "progress@" + label,
                        "At tick " + GameStatements.tickOf(attempt, label) + ", " + order.completedQuantity() + " of "
                                + order.requestedQuantity() + " units " + (order.completedQuantity() == 1 ? "is" : "are")
                                + " finished.")
                .support(Support.observation(label, "OrderObservation.completedQuantity", "OrderObservation.requestedQuantity"))
                .facet("section", SNAPSHOT)
                .build();
    }

    /**
     * For each idle machine with no usable waiting work: a design fact when it is the only machine
     * for some step it serves (removing it cannot be tried), otherwise an explicit refusal that names
     * the try that can settle whether it is needed.
     */
    static List<Statement> needed(Attempt attempt, String label) {
        FactoryModel model = attempt.evidence().publishedModel();
        List<Statement> statements = new ArrayList<>();
        for (Statement idle : idle(attempt, label)) {
            if (idle.facet("idleWithoutWork").filter("true"::equals).isEmpty()) {
                continue;
            }
            String resource = idle.facet("resource").orElseThrow();
            MachineId id = model.resources().stream()
                    .filter(r -> r.name().equals(resource))
                    .map(ConfiguredResource::id)
                    .findFirst()
                    .orElseThrow();
            List<String> soleSteps = model.operations().getFirst().steps().stream()
                    .filter(step -> step.eligibleResources().equals(Set.of(id)))
                    .map(OperationStepDefinition::name)
                    .toList();
            if (!soleSteps.isEmpty()) {
                statements.add(Statement.of(Question.Q3_IDLE_RESOURCE, Kind.DIRECT_FACT, "sole@" + label + ":" + resource,
                                resource + " is the only machine that can do " + String.join(" and ", soleSteps)
                                        + "; the order cannot finish without it.")
                        .support(Support.design("eligible resources of " + String.join(", ", soleSteps)))
                        .facet("resource", resource)
                        .facet("section", SNAPSHOT)
                        .build());
                continue;
            }
            statements.add(Statement.of(Question.Q3_IDLE_RESOURCE, Kind.REFUSAL, "surplus@" + label + ":" + resource,
                            "Whether " + resource + " is needed (whether the order would finish later without it): this run"
                                    + " does not tell you. Try removing it to find out.")
                    .support(idle.support())
                    .facet("refusalOf", "surplus")
                    .facet("resource", resource)
                    .facet("section", CANNOT_TELL)
                    .build());
        }
        return statements;
    }

    // ---------------------------------------------------------------- result

    static Statement outcome(Attempt attempt) {
        OrderObservation order = attempt.evidence().observation(ExperimentEvidence.CLOSING_LABEL).orders().getFirst();
        String text = order.complete()
                ? "The order finished at tick " + order.completedAt().value() + "."
                : "The order has not finished.";
        Statement.Builder builder = Statement.of(Question.PROGRESS, Kind.DIRECT_FACT, "outcome", text)
                .support(Support.observation(ExperimentEvidence.CLOSING_LABEL,
                        order.complete() ? "OrderObservation.completedAt" : "OrderObservation.complete"))
                .facet("section", RESULT);
        if (order.complete()) {
            builder.facet("completionTick", order.completedAt().value());
        }
        return builder.build();
    }

    static Statement lastUnit(Attempt attempt) {
        FactoryModel model = attempt.evidence().publishedModel();
        OracleOutcome<CompletingUnit> outcome = new GameEvidenceOracles.CompletingUnitTimeline().evaluateOn(attempt.evidence());
        if (outcome instanceof OracleOutcome.Underdetermined<CompletingUnit> refused) {
            return intervalRefusal(Question.Q4_DELAY, "completing-unit", "Waiting and work times are not shown", refused.reasons());
        }
        CompletingUnit unit = ((OracleOutcome.Derived<CompletingUnit>) outcome).value();
        List<String> parts = new ArrayList<>();
        UnitSegment longest = null;
        for (UnitSegment segment : unit.segments()) {
            String step = segment.occurrence().step().name();
            parts.add("waited " + segment.waitTicks() + " to start " + step + ", then " + step + " took "
                    + segment.processTicks() + " on " + GameStatements.resourceName(model, segment.occurrence().machineId()));
            if (longest == null || segment.waitTicks() > longest.waitTicks()) {
                longest = segment;
            }
        }
        return Statement.of(Question.Q4_DELAY, Kind.EVENT_INTERVAL, "completing-unit",
                        "The last unit to finish (unit " + unit.unit() + ") took " + unit.leadTime()
                                + " ticks from the order's acceptance at tick " + unit.acceptedTick() + ": "
                                + String.join("; ", parts) + ". Its longest wait shows where it spent time; on its own it"
                                + " does not show which machine to add.")
                .method(GameEvidenceOracles.COMPLETING_UNIT.name())
                .support(Support.events(unit.sequences(), "ORDER_ACCEPTED", "JOB_DISPATCHED", "JOB_STEP_COMPLETED",
                        "ORDER_COMPLETED.jobId"))
                .facet("leadTime", unit.leadTime())
                .facet("longestWaitStep", longest.occurrence().step().name())
                .facet("longestWaitTicks", longest.waitTicks())
                .facet("section", RESULT)
                .build();
    }

    static List<Statement> timeline(Attempt attempt) {
        FactoryModel model = attempt.evidence().publishedModel();
        OracleOutcome<Occurrences> outcome =
                new GameEvidenceOracles.StepOccurrences(ExperimentEvidence.CLOSING_LABEL).evaluateOn(attempt.evidence());
        if (outcome instanceof OracleOutcome.Underdetermined<Occurrences> refused) {
            return List.of(intervalRefusal(Question.ACTIVITY, "timeline", "Each machine's work times are not shown",
                    refused.reasons()));
        }
        Occurrences occurrences = ((OracleOutcome.Derived<Occurrences>) outcome).value();
        List<Statement> statements = new ArrayList<>();
        for (ConfiguredResource resource : model.resources()) {
            List<Occurrence> mine = occurrences.onMachine(resource.id());
            if (mine.isEmpty()) {
                statements.add(Statement.of(Question.ACTIVITY, Kind.EVENT_INTERVAL, "timeline:" + resource.name(),
                                resource.name() + " did no work in this run.")
                        .method(GameEvidence.OCCURRENCES.name())
                        .support(Support.events(List.of(occurrences.acceptedSequence(), occurrences.boundarySequence()),
                                "JOB_DISPATCHED"))
                        .facet("resource", resource.name())
                        .facet("section", MORE)
                        .build());
                continue;
            }
            Map<String, Long> byStep = new LinkedHashMap<>();
            mine.forEach(o -> byStep.merge(o.step().name(), 1L, Long::sum));
            String worked = byStep.entrySet().stream()
                    .map(e -> e.getValue() + " " + e.getKey())
                    .collect(Collectors.joining(" and "));
            String during = GameStatements.mergedIntervals(mine, occurrences.boundaryTick()).stream()
                    .map(i -> i[0] + "-" + i[1])
                    .collect(Collectors.joining(", "));
            String slots = resource.concurrency() > 1
                    ? "; at most " + GameStatements.maxActive(mine, occurrences.boundaryTick()) + " of "
                            + resource.concurrency() + " slots at once"
                    : "";
            List<Long> sequences = new ArrayList<>();
            mine.forEach(o -> {
                sequences.add(o.dispatchSequence());
                o.completionSequence().ifPresent(sequences::add);
            });
            statements.add(Statement.of(Question.ACTIVITY, Kind.EVENT_INTERVAL, "timeline:" + resource.name(),
                            resource.name() + " worked on " + worked + " step" + (mine.size() == 1 ? "" : "s")
                                    + " during ticks " + during + slots + ".")
                    .method(GameEvidence.OCCURRENCES.name())
                    .support(Support.events(sequences, "JOB_DISPATCHED", "JOB_STEP_COMPLETED"))
                    .facet("resource", resource.name())
                    .facet("section", MORE)
                    .build());
        }
        return statements;
    }

    static Statement intervalRefusal(Question question, String subject, String what, List<String> reasons) {
        boolean missedEvents = reasons.stream().anyMatch(reason -> reason.contains("not all retained"));
        String why = missedEvents
                ? "this view started watching after the order began and missed the earlier events"
                : String.join("; ", reasons);
        return Statement.of(question, Kind.REFUSAL, subject, what + ": " + why + ".")
                .support(Support.observation(ExperimentEvidence.CLOSING_LABEL))
                .facet("refusalOf", "interval-measurement")
                .facet("section", CANNOT_TELL)
                .build();
    }

    static Statement limitingRefusal() {
        return Statement.of(Question.Q2_LIMITING_STEP, Kind.REFUSAL, "limiting-step",
                        "Which machine to add: this run does not tell you. Waits and work times describe what happened;"
                                + " only a try that adds one machine shows whether the order then finishes sooner.")
                .facet("refusalOf", "limiting-step")
                .facet("section", CANNOT_TELL)
                .build();
    }

    // ---------------------------------------------------------------- comparisons

    static Statement changeAndOutcome(Attempt first, Attempt second, List<Change> changes, Question question) {
        long before = GameStatements.completionTick(first);
        long after = GameStatements.completionTick(second);
        String changeText = changes.isEmpty()
                ? "No change to the design."
                : (changes.size() == 1 ? "One change: " : changes.size() + " changes: ")
                        + changes.stream().map(GamePlainContract::describe).collect(Collectors.joining("; ")) + ".";
        String outcome = after == before
                ? "The order finished at tick " + after + ", the same as before."
                : "The order finished at tick " + after + " instead of " + before + " (" + Math.abs(after - before)
                        + (Math.abs(after - before) == 1 ? " tick " : " ticks ") + (after < before ? "sooner" : "later") + ").";
        return Statement.of(question, Kind.COMPARISON, "comparison", changeText + " " + outcome)
                .support(Support.observation(ExperimentEvidence.CLOSING_LABEL, "OrderObservation.completedAt")
                        .plus(Support.design("authored design of both attempts, including resource order")))
                .facet("changeCount", changes.size())
                .facet("completionDelta", after - before)
                .facet("changes", changes.stream().map(c -> c.type().name()).collect(Collectors.joining(",")))
                .facet("section", COMPARISON)
                .build();
    }

    static String describe(Change change) {
        return switch (change.type()) {
            case ADDED -> "added " + change.resource() + " (" + change.detail() + ")";
            case REMOVED -> "removed " + change.resource() + " (" + change.detail() + ")";
            case CHANGED -> "changed " + change.resource() + " (" + change.detail() + ")";
            case ORDER -> "the machine order changed from " + change.detail().replace(" -> ", " to ");
        };
    }
}
