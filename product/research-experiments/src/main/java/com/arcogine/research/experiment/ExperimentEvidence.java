package com.arcogine.research.experiment;

import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.factory.process.RuntimeObservationMetadata;
import com.arcogine.types.ModelFingerprint;
import com.arcogine.types.OrderId;
import com.arcogine.types.RunId;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * The read-only bundle of raw supported evidence one run produced: published-model facts,
 * supported observations, and the supported runtime events that were retained.
 *
 * <p>Nothing in here is a derivation. Oracle outputs, expectations and interpretations live
 * elsewhere ({@link Oracle}, {@link ExpectedClaim}), so evidence can be compared, replayed and
 * re-analysed without any analytical result leaking into it. Internal scheduler events, command
 * results' scheduled-event lists, and handler or store state are deliberately not captured.
 *
 * @param fixtureId the fixture that produced this evidence
 * @param publishedModel the authored model facts the run was instantiated from
 * @param modelFingerprint the fingerprint of the published model, as the runtime reported it
 * @param script the ordered script that was executed
 * @param commands the outcome of every command in the script, in script order
 * @param observations supported observations by label, including the runner's {@value
 *     #CLOSING_LABEL} observation
 * @param retainedEvents the supported events that were retained, in sequence order
 * @param window what the retained events cover
 */
public record ExperimentEvidence(
        String fixtureId,
        FactoryModel publishedModel,
        ModelFingerprint modelFingerprint,
        List<ExperimentStep> script,
        List<CommandRecord> commands,
        Map<String, RuntimeObservation> observations,
        List<RuntimeEventEnvelope> retainedEvents,
        EvidenceWindow window) {

    /** Label of the observation the runner always takes after the last script step. */
    public static final String CLOSING_LABEL = "closing";

    /**
     * The one identity substituted by {@link #withNormalizedRunIdentity()}. A run's {@code RunId}
     * is opaque correlation metadata that never participates in a deterministic outcome
     * ({@code docs/architecture/runtime-contract.md}), so independently created runs legitimately
     * differ in it and in nothing else.
     */
    public static final RunId NORMALIZED_RUN_ID = new RunId(new UUID(0L, 0L));

    /**
     * The definite result of one script command. A rejected or faulted command is evidence, not an
     * exception: what the supported boundary answered is part of the experiment.
     */
    public record CommandRecord(
            int stepIndex,
            ExperimentStep step,
            Outcome outcome,
            String code,
            String diagnostic,
            Optional<OrderId> acceptedOrder) {

        public enum Outcome {
            ACCEPTED,
            REJECTED,
            FAULTED
        }

        public CommandRecord {
            Objects.requireNonNull(step, "step");
            Objects.requireNonNull(outcome, "outcome");
            Objects.requireNonNull(code, "code");
            Objects.requireNonNull(diagnostic, "diagnostic");
            Objects.requireNonNull(acceptedOrder, "acceptedOrder");
        }

        public boolean accepted() {
            return outcome == Outcome.ACCEPTED;
        }
    }

    public ExperimentEvidence {
        if (Objects.requireNonNull(fixtureId, "fixtureId").isBlank()) {
            throw new IllegalArgumentException("fixtureId must not be blank");
        }
        Objects.requireNonNull(publishedModel, "publishedModel");
        Objects.requireNonNull(modelFingerprint, "modelFingerprint");
        Objects.requireNonNull(window, "window");
        script = List.copyOf(Objects.requireNonNull(script, "script"));
        commands = List.copyOf(Objects.requireNonNull(commands, "commands"));
        retainedEvents = List.copyOf(Objects.requireNonNull(retainedEvents, "retainedEvents"));
        observations = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(observations, "observations")));
    }

    /** The supported observation captured under {@code label}. */
    public RuntimeObservation observation(String label) {
        RuntimeObservation observation = observations.get(label);
        if (observation == null) {
            throw new IllegalArgumentException(
                    "no observation was captured under label '" + label + "'; captured: " + observations.keySet());
        }
        return observation;
    }

    public boolean allCommandsAccepted() {
        return commands.stream().allMatch(CommandRecord::accepted);
    }

    /**
     * This evidence with the run identity replaced by {@link #NORMALIZED_RUN_ID} everywhere it
     * appears, and with nothing else changed. It is the existing determinism convention applied in
     * one place: two runs of one fixture are equivalent exactly when everything except the run
     * identity is equal, so machine identities, work-item identities, authored ordering, event
     * sequence and order, times, the model fingerprint and outcome values all stay significant.
     */
    public ExperimentEvidence withNormalizedRunIdentity() {
        Map<String, RuntimeObservation> normalizedObservations = new LinkedHashMap<>();
        observations.forEach((label, observation) -> normalizedObservations.put(label, normalize(observation)));
        return new ExperimentEvidence(
                fixtureId,
                publishedModel,
                modelFingerprint,
                script,
                commands,
                normalizedObservations,
                retainedEvents.stream().map(ExperimentEvidence::normalize).toList(),
                window.withRunId(NORMALIZED_RUN_ID));
    }

    private static RuntimeObservation normalize(RuntimeObservation observation) {
        RuntimeObservationMetadata metadata = observation.metadata();
        return new RuntimeObservation(
                new RuntimeObservationMetadata(
                        NORMALIZED_RUN_ID,
                        metadata.modelFingerprint(),
                        metadata.currentTime(),
                        metadata.runState(),
                        metadata.latestEventSequence()),
                observation.resources(),
                observation.orders(),
                observation.jobs(),
                observation.pendingWork(),
                observation.performance());
    }

    private static RuntimeEventEnvelope normalize(RuntimeEventEnvelope event) {
        return new RuntimeEventEnvelope(
                NORMALIZED_RUN_ID,
                event.sequence(),
                event.simulationTime(),
                event.eventType(),
                event.modelFingerprint(),
                event.controlledRevisionId(),
                event.affectedEntityRefs(),
                event.payload());
    }
}
