package com.arcogine.research.experiment;

import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeObservation;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A finished evidence bundle as one derivation is allowed to see it: only the inputs its {@link
 * ResearchDefinition} declares, and events only through ranges the window has confirmed complete.
 *
 * <p>Reading an undeclared input fails, so a derivation cannot quietly depend on evidence its
 * definition does not name. Every read is recorded, so the outcome a derivation builds carries the
 * observations it read and the event interval covering the events it read.
 */
public final class DeclaredEvidence {

    private final ExperimentEvidence evidence;
    private final ResearchDefinition definition;
    private final Set<String> observationsRead = new LinkedHashSet<>();
    private SequenceRange eventsRead;

    DeclaredEvidence(ExperimentEvidence evidence, ResearchDefinition definition) {
        this.evidence = Objects.requireNonNull(evidence, "evidence");
        this.definition = Objects.requireNonNull(definition, "definition");
    }

    /** The authored model facts the run was instantiated from. */
    public FactoryModel publishedModel() {
        require(EvidenceInput.PUBLISHED_MODEL);
        return evidence.publishedModel();
    }

    /** The supported observation captured under {@code label}. */
    public RuntimeObservation observation(String label) {
        require(EvidenceInput.OBSERVATIONS);
        RuntimeObservation observation = evidence.observation(label);
        observationsRead.add(label);
        return observation;
    }

    /**
     * The retained supported events in {@code (afterSequence, throughSequence]}, or empty when any
     * sequence in that range was not retained. There is no way to read a range with a gap in it as
     * though it were complete; a derivation that gets an empty result is expected to refuse.
     */
    public Optional<List<RuntimeEventEnvelope>> completeEvents(long afterSequence, long throughSequence) {
        require(EvidenceInput.SUPPORTED_EVENTS);
        if (!evidence.window().missingWithin(afterSequence, throughSequence).isEmpty()) {
            return Optional.empty();
        }
        List<RuntimeEventEnvelope> range = evidence.retainedEvents().stream()
                .filter(event -> event.sequence() > afterSequence && event.sequence() <= throughSequence)
                .toList();
        if (throughSequence > afterSequence) {
            long from = eventsRead == null ? afterSequence + 1 : Math.min(eventsRead.from(), afterSequence + 1);
            long through = eventsRead == null ? throughSequence : Math.max(eventsRead.through(), throughSequence);
            eventsRead = new SequenceRange(from, through);
        }
        return Optional.of(range);
    }

    /** The sequences in {@code (afterSequence, throughSequence]} that were not retained. */
    public List<SequenceRange> missingEvents(long afterSequence, long throughSequence) {
        require(EvidenceInput.SUPPORTED_EVENTS);
        return evidence.window().missingWithin(afterSequence, throughSequence);
    }

    /** A derived value, together with the exact evidence read so far. */
    public <T> OracleOutcome<T> derived(T value) {
        return new OracleOutcome.Derived<>(
                definition, value, new EvidenceSupport(List.copyOf(observationsRead), Optional.ofNullable(eventsRead)));
    }

    /** An explicit refusal: the supported evidence does not license the claim. */
    public <T> OracleOutcome<T> underdetermined(String... reasons) {
        return new OracleOutcome.Underdetermined<>(definition, List.of(reasons));
    }

    private void require(EvidenceInput input) {
        if (!definition.inputs().contains(input)) {
            throw new IllegalStateException("definition '" + definition.name() + "' did not declare input " + input
                    + "; declared: " + definition.inputs());
        }
    }
}
