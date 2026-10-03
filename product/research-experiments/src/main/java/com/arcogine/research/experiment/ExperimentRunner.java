package com.arcogine.research.experiment;

import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.process.CommandResult;
import com.arcogine.factory.process.FactoryRuntime;
import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.factory.process.RuntimeRunState;
import com.arcogine.types.OrderId;
import com.arcogine.types.SimTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Runs an {@link ExperimentFixture} in a fresh {@link FactoryRuntime} and returns the supported
 * evidence it produced.
 *
 * <p>The runner is an embedded consumer of the supported runtime contract and nothing more: it
 * instantiates the runtime from the version the fixture published through the existing
 * publication boundary, submits commands and advances through the supported session-control
 * surface, and collects the supported observation and the draining supported-event stream exactly
 * where the script says to. It never inspects the scheduler, the handler, or any store, and it does
 * not use the internal events that advancement and command results expose, so nothing it returns
 * depends on implementation internals.
 *
 * <p>It always ends with a {@value ExperimentEvidence#CLOSING_LABEL} observation, which fixes the
 * run's final event cursor and therefore what "the complete run" means for the window check.
 */
public final class ExperimentRunner {

    private ExperimentRunner() {}

    /**
     * Runs {@code fixture} once.
     *
     * @throws IllegalStateException when a run that must quiesce does not, or when the captured
     *     window does not match the fixture's declared intent
     */
    public static ExperimentEvidence run(ExperimentFixture fixture) {
        Objects.requireNonNull(fixture, "fixture");
        FactoryModelVersion version = fixture.publishedModel();
        FactoryRuntime runtime = instantiate(fixture);

        List<ExperimentEvidence.CommandRecord> commands = new ArrayList<>();
        Map<String, RuntimeObservation> observations = new LinkedHashMap<>();
        List<EvidenceWindow.CollectionPoint> points = new ArrayList<>();
        List<RuntimeEventEnvelope> retained = new ArrayList<>();

        List<ExperimentStep> script = fixture.script();
        for (int index = 0; index < script.size(); index++) {
            switch (script.get(index)) {
                case ExperimentStep.SubmitWorkload step ->
                    commands.add(recordOf(
                            index, step, runtime.submitWorkload(step.productId(), step.quantity(), step.unitPrice())));
                case ExperimentStep.SetMachineAvailability step -> {
                    CommandResult<?> result = runtime.setMachineAvailability(step.machineId(), step.online());
                    commands.add(recordOf(index, step, result));
                }
                case ExperimentStep.AdvanceUntil step -> advance(runtime, step.target());
                case ExperimentStep.AdvanceToQuiescence step -> {
                    advance(runtime, step.deadline());
                    if (runtime.observe().metadata().runState() != RuntimeRunState.QUIESCENT) {
                        throw new IllegalStateException("fixture '" + fixture.id() + "' step " + index
                                + ": the run was still active at deadline " + step.deadline());
                    }
                }
                case ExperimentStep.CaptureObservation step -> {
                    RuntimeObservation observation = runtime.observe();
                    observations.put(step.label(), observation);
                    points.add(new EvidenceWindow.CollectionPoint(
                            index,
                            step.label(),
                            EvidenceWindow.CollectionPoint.Kind.OBSERVATION,
                            observation.metadata().latestEventSequence()));
                }
                case ExperimentStep.CaptureEvents step -> {
                    retained.addAll(runtime.drainSupportedEvents());
                    points.add(new EvidenceWindow.CollectionPoint(
                            index,
                            step.label(),
                            EvidenceWindow.CollectionPoint.Kind.EVENTS_RETAINED,
                            cursor(runtime)));
                }
                case ExperimentStep.DiscardEvents step -> {
                    runtime.drainSupportedEvents();
                    points.add(new EvidenceWindow.CollectionPoint(
                            index,
                            step.label(),
                            EvidenceWindow.CollectionPoint.Kind.EVENTS_DISCARDED,
                            cursor(runtime)));
                }
            }
        }

        RuntimeObservation closing = runtime.observe();
        observations.put(ExperimentEvidence.CLOSING_LABEL, closing);
        long runFinalSequence = closing.metadata().latestEventSequence();
        points.add(new EvidenceWindow.CollectionPoint(
                script.size(),
                ExperimentEvidence.CLOSING_LABEL,
                EvidenceWindow.CollectionPoint.Kind.CLOSING_OBSERVATION,
                runFinalSequence));

        EvidenceWindow window = new EvidenceWindow(
                runtime.runId(),
                fixture.windowIntent(),
                retained.isEmpty() ? 0 : retained.getFirst().sequence(),
                retained.isEmpty() ? 0 : retained.getLast().sequence(),
                runFinalSequence,
                missingSequences(retained, runFinalSequence),
                points);
        requireIntentMatches(fixture, window);

        return new ExperimentEvidence(
                fixture.id(),
                fixture.authoredModel(),
                version.fingerprint(),
                script,
                commands,
                observations,
                retained,
                window);
    }

    /**
     * Runs {@code fixture} twice, each time in a fresh runtime, and requires the two runs to produce
     * equivalent evidence: equal in everything once only their run identities are normalized
     * ({@link ExperimentEvidence#withNormalizedRunIdentity()}). Returns the first run's evidence,
     * with its run identity exactly as the runtime issued it.
     *
     * @throws IllegalStateException when the replay differs, naming the first difference; or for
     *     any reason {@link #run} would
     */
    public static ExperimentEvidence runAndReplay(ExperimentFixture fixture) {
        ExperimentEvidence first = run(fixture);
        return requireReplayEquivalent(first, run(fixture));
    }

    /**
     * Requires {@code replay} to be equivalent to {@code first} once each has only its run identity
     * normalized, and returns {@code first}.
     *
     * @throws IllegalStateException naming the first respect in which the two differ
     */
    public static ExperimentEvidence requireReplayEquivalent(ExperimentEvidence first, ExperimentEvidence replay) {
        ExperimentEvidence expected = first.withNormalizedRunIdentity();
        ExperimentEvidence actual = replay.withNormalizedRunIdentity();
        if (!expected.equals(actual)) {
            throw new IllegalStateException("fixture '" + first.fixtureId() + "' did not replay deterministically: "
                    + firstDifference(expected, actual));
        }
        return first;
    }

    /** A fresh runtime instantiated from the version {@code fixture} has already published. */
    static FactoryRuntime instantiate(ExperimentFixture fixture) {
        return FactoryRuntime.forModel(fixture.publishedModel());
    }

    private static String firstDifference(ExperimentEvidence expected, ExperimentEvidence actual) {
        if (!expected.fixtureId().equals(actual.fixtureId())) {
            return "fixture id " + expected.fixtureId() + " vs " + actual.fixtureId();
        }
        if (!expected.publishedModel().equals(actual.publishedModel())) {
            return "the published model differs";
        }
        if (!expected.modelFingerprint().equals(actual.modelFingerprint())) {
            return "model fingerprint " + expected.modelFingerprint() + " vs " + actual.modelFingerprint();
        }
        if (!expected.script().equals(actual.script())) {
            return "the script differs";
        }
        Optional<String> commands = firstDifference("command", expected.commands(), actual.commands());
        if (commands.isPresent()) {
            return commands.get();
        }
        if (!List.copyOf(expected.observations().keySet()).equals(List.copyOf(actual.observations().keySet()))) {
            return "observation labels " + expected.observations().keySet() + " vs " + actual.observations().keySet();
        }
        for (String label : expected.observations().keySet()) {
            Optional<String> observation = firstDifference(expected.observation(label), actual.observation(label));
            if (observation.isPresent()) {
                return "observation '" + label + "' " + observation.get();
            }
        }
        Optional<String> events = firstDifference("retained event", expected.retainedEvents(), actual.retainedEvents());
        return events.orElseGet(() -> "evidence window " + expected.window() + " vs " + actual.window());
    }

    private static Optional<String> firstDifference(RuntimeObservation expected, RuntimeObservation actual) {
        if (!expected.metadata().equals(actual.metadata())) {
            return Optional.of("metadata " + expected.metadata() + " vs " + actual.metadata());
        }
        return firstDifference("resource", expected.resources(), actual.resources())
                .or(() -> firstDifference("order", expected.orders(), actual.orders()))
                .or(() -> firstDifference("job", expected.jobs(), actual.jobs()))
                .or(() -> firstDifference("pending work", expected.pendingWork(), actual.pendingWork()))
                .or(() -> expected.performance().equals(actual.performance())
                        ? Optional.empty()
                        : Optional.of("performance " + expected.performance() + " vs " + actual.performance()));
    }

    private static <T> Optional<String> firstDifference(String kind, List<T> expected, List<T> actual) {
        for (int i = 0; i < Math.min(expected.size(), actual.size()); i++) {
            if (!expected.get(i).equals(actual.get(i))) {
                return Optional.of(kind + " " + i + ": " + expected.get(i) + " vs " + actual.get(i));
            }
        }
        return expected.size() == actual.size()
                ? Optional.empty()
                : Optional.of(kind + " count " + expected.size() + " vs " + actual.size());
    }

    /** The internal events this call processes are deliberately discarded: they are not evidence. */
    private static void advance(FactoryRuntime runtime, SimTime target) {
        runtime.advanceUntil(target, Long.MAX_VALUE);
    }

    private static long cursor(FactoryRuntime runtime) {
        return runtime.observe().metadata().latestEventSequence();
    }

    private static ExperimentEvidence.CommandRecord recordOf(
            int index, ExperimentStep step, CommandResult<?> result) {
        return switch (result) {
            case CommandResult.Accepted<?> accepted -> new ExperimentEvidence.CommandRecord(
                    index,
                    step,
                    ExperimentEvidence.CommandRecord.Outcome.ACCEPTED,
                    accepted.code(),
                    accepted.diagnostic(),
                    orderIdOf(accepted.value()));
            case CommandResult.Rejected<?> rejected -> new ExperimentEvidence.CommandRecord(
                    index,
                    step,
                    ExperimentEvidence.CommandRecord.Outcome.REJECTED,
                    rejected.code(),
                    rejected.diagnostic(),
                    Optional.empty());
            case CommandResult.Faulted<?> faulted -> new ExperimentEvidence.CommandRecord(
                    index,
                    step,
                    ExperimentEvidence.CommandRecord.Outcome.FAULTED,
                    faulted.code(),
                    faulted.diagnostic(),
                    orderIdOf(faulted.value()));
        };
    }

    private static Optional<OrderId> orderIdOf(Object acceptedValue) {
        return acceptedValue instanceof OrderId orderId ? Optional.of(orderId) : Optional.empty();
    }

    /** Every sequence in {@code 1..runFinalSequence} that no retained event covers. */
    private static List<SequenceRange> missingSequences(List<RuntimeEventEnvelope> retained, long runFinalSequence) {
        List<SequenceRange> missing = new ArrayList<>();
        long next = 1;
        for (RuntimeEventEnvelope event : retained) {
            if (event.sequence() > next) {
                missing.add(new SequenceRange(next, event.sequence() - 1));
            }
            next = event.sequence() + 1;
        }
        if (next <= runFinalSequence) {
            missing.add(new SequenceRange(next, runFinalSequence));
        }
        return missing;
    }

    private static void requireIntentMatches(ExperimentFixture fixture, EvidenceWindow window) {
        boolean intendedComplete = fixture.windowIntent() == ExperimentFixture.WindowIntent.COMPLETE_RUN;
        if (intendedComplete != window.isComplete()) {
            throw new IllegalStateException("fixture '" + fixture.id() + "' declares " + fixture.windowIntent()
                    + " but its captured window "
                    + (window.isComplete() ? "is complete" : "is missing supported events " + window.missingSequences()));
        }
    }
}
