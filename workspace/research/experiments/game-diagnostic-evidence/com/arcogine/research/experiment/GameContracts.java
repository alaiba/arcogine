package com.arcogine.research.experiment;

import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.process.PendingWorkObservation;
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
import com.arcogine.types.MachineState;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** The candidate evidence contracts the investigation compares, plus a deliberately naive control. */
final class GameContracts {

    private GameContracts() {}

    static List<Contract> candidates() {
        return List.of(
                new FactsOnly(),
                new FactsPlusNamedInterpretation(NamedMethod.POOL_OCCUPANCY),
                new FactsPlusNamedInterpretation(NamedMethod.COMPLETION_CHAIN),
                new ClaimEvidenceBundle(true),
                new ClaimEvidenceBundle(false),
                new GamePlainContract.PlainBundle());
    }

    static List<Contract> all() {
        List<Contract> all = new ArrayList<>(candidates());
        all.add(new NaiveDashboard());
        return List.copyOf(all);
    }

    /** The factual basis every candidate shares: no verdict, no aggregate, no refusal. */
    static List<Statement> facts(Attempt attempt) {
        List<Statement> statements = new ArrayList<>();
        attempt.midRunLabel().ifPresent(label -> {
            statements.addAll(GameStatements.waiting(attempt, label));
            statements.addAll(GameStatements.resourceActivity(attempt, label));
            statements.addAll(GameStatements.idleness(attempt, label));
            statements.add(GameStatements.progress(attempt, label));
        });
        statements.add(GameStatements.outcome(attempt));
        statements.addAll(GameStatements.idleness(attempt, ExperimentEvidence.CLOSING_LABEL));
        statements.add(GameStatements.completingUnit(attempt));
        statements.addAll(GameStatements.activityTimeline(attempt));
        return statements;
    }

    static List<Statement> plainComparison(Attempt first, Attempt second) {
        List<Change> changes = GameDiagnostics.changeSet(first.design(), second.design());
        Question question = changes.size() > 1 ? Question.Q6_CONFOUNDED_COMPARISON : Question.Q5_CONTROLLED_COMPARISON;
        return List.of(GameStatements.changeAndOutcome(first, second, changes, question));
    }

    /** Candidate 1: supported facts, with no synthesized verdict. */
    static final class FactsOnly implements Contract {

        @Override
        public String id() {
            return "C1-facts-only";
        }

        @Override
        public String summary() {
            return "Waiting by step, resource activity, progress, the last unit's times and attempt outcomes; no verdict.";
        }

        @Override
        public List<Statement> attempt(Attempt attempt) {
            return facts(attempt);
        }

        @Override
        public List<Statement> compare(Attempt first, Attempt second) {
            return plainComparison(first, second);
        }
    }

    enum NamedMethod {
        POOL_OCCUPANCY,
        COMPLETION_CHAIN
    }

    /** Candidate 2: the same facts plus one named diagnostic method and the numbers it rests on. */
    static final class FactsPlusNamedInterpretation implements Contract {

        private final NamedMethod method;

        FactsPlusNamedInterpretation(NamedMethod method) {
            this.method = method;
        }

        @Override
        public String id() {
            return method == NamedMethod.POOL_OCCUPANCY ? "C2a-named-pool-occupancy" : "C2b-named-completion-chain";
        }

        @Override
        public String summary() {
            return method == NamedMethod.POOL_OCCUPANCY
                    ? "Facts plus a 'most occupied eligibility pool' bottleneck verdict with its job-tick numbers."
                    : "Facts plus a 'completion chain' pacing-step verdict with the chain's evidence; the method itself"
                            + " reports a tie.";
        }

        @Override
        public List<Statement> attempt(Attempt attempt) {
            List<Statement> statements = new ArrayList<>(facts(attempt));
            statements.add(GameStatements.stepWaitTotals(attempt));
            statements.add(method == NamedMethod.POOL_OCCUPANCY
                    ? GameStatements.poolOccupancyBottleneck(attempt)
                    : GameStatements.completionChain(attempt));
            return statements;
        }

        @Override
        public List<Statement> compare(Attempt first, Attempt second) {
            return plainComparison(first, second);
        }
    }

    /**
     * Candidate 3: a concise answer per question with its evidence chain, and an explicit refusal
     * where the evidence does not license an answer. {@code withChain} adds the completion-chain
     * pacing step (and the run-total waits it is read beside); without it, the single-run limiting
     * step is always refused and only controlled comparisons speak to it.
     */
    static final class ClaimEvidenceBundle implements Contract {

        private final boolean withChain;

        ClaimEvidenceBundle(boolean withChain) {
            this.withChain = withChain;
        }

        @Override
        public String id() {
            return withChain ? "C3a-bundle-with-completion-chain" : "C3b-bundle-minimal";
        }

        @Override
        public String summary() {
            return withChain
                    ? "Facts, run-total waits and the completion-chain pacing step as claims with evidence; refusal for"
                            + " surplus, counterfactual, ties and multi-change attribution."
                    : "Facts and the last unit's times as claims with evidence; the single-run limiting step, surplus and"
                            + " multi-change attribution are refused; one-change comparisons speak only to change and outcome.";
        }

        @Override
        public List<Statement> attempt(Attempt attempt) {
            List<Statement> statements = new ArrayList<>(facts(attempt));
            attempt.midRunLabel().ifPresent(label -> statements.addAll(GameStatements.surplusRefusals(attempt, label)));
            if (withChain) {
                statements.add(GameStatements.stepWaitTotals(attempt));
                Statement chain = GameStatements.completionChain(attempt);
                statements.add(chain);
                chain.facet("verdictStep").ifPresent(step -> statements.add(GameStatements.counterfactualRefusal(step)));
            } else {
                statements.add(GameStatements.singleRunLimitingStepRefusal());
            }
            return statements;
        }

        @Override
        public List<Statement> compare(Attempt first, Attempt second) {
            List<Change> changes = GameDiagnostics.changeSet(first.design(), second.design());
            List<Statement> statements = new ArrayList<>();
            if (changes.size() > 1) {
                statements.add(GameStatements.changeAndOutcome(first, second, changes, Question.Q6_CONFOUNDED_COMPARISON));
                statements.add(GameStatements.attributionRefusal(
                        changes, GameStatements.completionTick(first), GameStatements.completionTick(second)));
            } else {
                statements.add(GameStatements.changeAndOutcome(first, second, changes, Question.Q5_CONTROLLED_COMPARISON));
                if (changes.stream().anyMatch(change -> change.type() == ChangeType.ORDER)) {
                    statements.add(GameStatements.tieBreakRule());
                }
                statements.add(GameStatements.mechanismRefusal());
            }
            return statements;
        }
    }

    /**
     * A deliberately naive dashboard used only as a control that the audits can fail: completion-
     * credited {@code busyTicks} over elapsed time as "utilization", own queue plus every pending entry
     * naming a resource as that resource's "queue", the highest "utilization" as the bottleneck, an idle
     * resource as "surplus", and a comparison that attributes the outcome to its first change (or to
     * "variation" when its offer-only change set is empty).
     */
    static final class NaiveDashboard implements Contract {

        @Override
        public String id() {
            return "N-naive-dashboard";
        }

        @Override
        public String summary() {
            return "Control: busyTicks utilization, combined per-machine queues, highest-utilization bottleneck, idle ="
                    + " surplus, first-change attribution.";
        }

        @Override
        public List<Statement> attempt(Attempt attempt) {
            List<Statement> statements = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            attempt.midRunLabel().ifPresent(labels::add);
            labels.add(ExperimentEvidence.CLOSING_LABEL);
            FactoryModel model = attempt.evidence().publishedModel();
            for (String label : labels) {
                RuntimeObservation observation = attempt.evidence().observation(label);
                long tick = observation.metadata().currentTime().value();
                ResourceObservation busiest = null;
                for (ResourceObservation resource : observation.resources()) {
                    long percent = tick == 0 ? 0 : Math.round(100.0 * resource.busyTicks() / tick);
                    statements.add(Statement.of(Question.ACTIVITY, Kind.DIRECT_FACT, "utilization@" + label + ":" + resource.name(),
                                    "Utilization at tick " + tick + ": " + resource.name() + " " + percent + "%.")
                            .support(Support.observation(label, "ResourceObservation.busyTicks", "RuntimeObservationMetadata.currentTime"))
                            .facet("usesBusyTicks", true)
                            .facet("resource", resource.name())
                            .build());
                    if (busiest == null || resource.busyTicks() > busiest.busyTicks()) {
                        busiest = resource;
                    }
                    long pending = observation.pendingWork().stream()
                            .map(PendingWorkObservation::eligibleMachineIds)
                            .filter(ids -> ids.contains(resource.machineId()))
                            .count();
                    statements.add(Statement.of(Question.Q1_WAITING, Kind.BOUNDARY_COUNT, "queue@" + label + ":" + resource.name(),
                                    "Queue at " + resource.name() + " (tick " + tick + "): " + (resource.queueDepth() + pending) + ".")
                            .support(Support.observation(label, "ResourceObservation.queueDepth", "PendingWorkObservation"))
                            .facet("perResourceWaiting", resource.queueDepth() + pending)
                            .facet("resource", resource.name())
                            .build());
                    if (label.equals(ExperimentEvidence.CLOSING_LABEL) || resource.state() != MachineState.Idle) {
                        continue;
                    }
                    statements.add(Statement.of(Question.Q3_IDLE_RESOURCE, Kind.NAMED_INTERPRETATION, "surplus:" + resource.name(),
                                    resource.name() + " is surplus (idle at tick " + tick + ").")
                            .support(Support.observation(label, "ResourceObservation.state"))
                            .facet("surplusVerdict", resource.name())
                            .build());
                }
                if (label.equals(ExperimentEvidence.CLOSING_LABEL) && busiest != null) {
                    statements.add(Statement.of(Question.Q2_LIMITING_STEP, Kind.NAMED_INTERPRETATION, "limiting-step",
                                    "Bottleneck: " + busiest.name() + " (highest utilization).")
                            .support(Support.observation(label, "ResourceObservation.busyTicks"))
                            .facet("verdictStep", String.join("+", GameStatements.stepsServedBy(model, busiest.machineId())))
                            .facet("verdictWord", "bottleneck")
                            .facet("usesBusyTicks", true)
                            .build());
                }
            }
            statements.add(GameStatements.outcome(attempt));
            return statements;
        }

        @Override
        public List<Statement> compare(Attempt first, Attempt second) {
            Map<String, Long> before = offers(first.design());
            Map<String, Long> after = offers(second.design());
            List<String> changes = new ArrayList<>();
            before.forEach((offer, count) -> {
                long now = after.getOrDefault(offer, 0L);
                if (now < count) {
                    changes.add("removed " + (count - now) + " x " + offer);
                }
            });
            after.forEach((offer, count) -> {
                long was = before.getOrDefault(offer, 0L);
                if (count > was) {
                    changes.add("added " + (count - was) + " x " + offer);
                }
            });
            long t1 = GameStatements.completionTick(first);
            long t2 = GameStatements.completionTick(second);
            String attributedTo = changes.isEmpty() ? "run-to-run variation" : changes.getFirst();
            String text = changes.isEmpty()
                    ? "Same equipment; completion changed by " + (t2 - t1) + " ticks (run-to-run variation)."
                    : "Completion " + (t2 <= t1 ? "improved" : "worsened") + " by " + Math.abs(t2 - t1) + " ticks due to "
                            + attributedTo + ".";
            Question question = changes.size() > 1 ? Question.Q6_CONFOUNDED_COMPARISON : Question.Q5_CONTROLLED_COMPARISON;
            return List.of(Statement.of(question, Kind.COMPARISON, "comparison", text)
                    .support(Support.observation(ExperimentEvidence.CLOSING_LABEL, "OrderObservation.completedAt"))
                    .facet("attributedTo", attributedTo)
                    .facet("changeCount", changes.size())
                    .facet("completionDelta", t2 - t1)
                    .build());
        }

        /** Offers as a naive game might see them: a multiset of (steps, slots), ignoring names and order. */
        private static Map<String, Long> offers(LinearRoutingFamily design) {
            return design.resources().stream()
                    .map(GameDiagnostics::describe)
                    .collect(Collectors.groupingBy(o -> o, TreeMap::new, Collectors.counting()));
        }
    }

    static String render(List<Statement> statements) {
        return statements.stream()
                .sorted(Comparator.comparing(Statement::question))
                .map(s -> "- [" + s.question() + " / " + s.kind() + (s.method().map(m -> " / " + m).orElse("")) + "] "
                        + s.text())
                .collect(Collectors.joining("\n"));
    }

    static boolean same(List<Statement> a, List<Statement> b) {
        return Objects.equals(a, b);
    }
}
