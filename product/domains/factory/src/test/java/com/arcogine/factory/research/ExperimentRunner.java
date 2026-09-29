package com.arcogine.factory.research;

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
 * publishes the authored model through the existing publication boundary, submits commands and
 * advances through the supported session-control surface, and collects the supported observation
 * and the draining supported-event stream exactly where the script says to. It never inspects the
 * scheduler, the handler, or any store, and it does not use the internal events that advancement
 * and command results expose, so nothing it returns depends on implementation internals.
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
        FactoryRuntime runtime = FactoryRuntime.forModel(version);

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
                case ExperimentStep.SetMachineAvailability step ->
                    commands.add(recordOf(index, step, runtime.setMachineAvailability(step.machineId(), step.online())));
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

    /** The internal events this call processes are deliberately discarded: they are not evidence. */
    private static void advance(FactoryRuntime runtime, SimTime target) {
        runtime.advanceUntil(target, Long.MAX_VALUE);
    }

    private static long cursor(FactoryRuntime runtime) {
        return runtime.observe().metadata().latestEventSequence();
    }

    private static <T> ExperimentEvidence.CommandRecord recordOf(
            int index, ExperimentStep step, CommandResult<T> result) {
        return switch (result) {
            case CommandResult.Accepted<T> accepted -> new ExperimentEvidence.CommandRecord(
                    index,
                    step,
                    ExperimentEvidence.CommandRecord.Outcome.ACCEPTED,
                    accepted.code(),
                    accepted.diagnostic(),
                    orderIdOf(accepted.value()));
            case CommandResult.Rejected<T> rejected -> new ExperimentEvidence.CommandRecord(
                    index,
                    step,
                    ExperimentEvidence.CommandRecord.Outcome.REJECTED,
                    rejected.code(),
                    rejected.diagnostic(),
                    Optional.empty());
            case CommandResult.Faulted<T> faulted -> new ExperimentEvidence.CommandRecord(
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
