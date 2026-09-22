package com.arcogine.governance.conformance;

import com.arcogine.governance.assertion.Assertion;
import com.arcogine.governance.evidence.EvidenceUse;
import com.arcogine.governance.evidence.TemporalFrame;
import com.arcogine.governance.requirement.Requirement;
import com.arcogine.types.ControlledRevisionId;
import com.arcogine.types.ModelFingerprint;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * An accepted immutable historical occurrence of one conformance evaluation.
 *
 * <p>The deterministic {@link ConformanceEvaluation} remains a separate value. Acceptance adds
 * occurrence identity and authority-owned recording metadata without changing that value.
 */
public final class EvaluationOccurrence {

    private final EvaluationOccurrenceId id;
    private final Requirement requirement;
    private final Assertion<?> assertion;
    private final ModelFingerprint modelFingerprint;
    private final ControlledRevisionId controlledRevisionId;
    private final List<EvidenceUse> reliedOnUses;
    private final List<EvidenceUse> consideredButExcludedUses;
    private final List<String> knownMaterialGaps;
    private final TemporalFrame temporalFrame;
    private final String interpretationRules;
    private final ConformanceEvaluation evaluation;
    private final String explanation;
    private final EvaluationOccurrenceId relatedOccurrenceId;
    private final Instant acceptedAt;

    EvaluationOccurrence(EvaluationOccurrenceDraft draft, Instant acceptedAt) {
        this.id = draft.id();
        this.requirement = draft.requirement();
        this.assertion = draft.assertion();
        this.modelFingerprint = draft.modelFingerprint();
        this.controlledRevisionId = draft.controlledRevisionId();
        this.reliedOnUses = List.copyOf(draft.reliedOnUses());
        this.consideredButExcludedUses = List.copyOf(draft.consideredButExcludedUses());
        this.knownMaterialGaps = List.copyOf(draft.knownMaterialGaps());
        this.temporalFrame = draft.temporalFrame();
        this.interpretationRules = draft.interpretationRules();
        this.evaluation = draft.evaluation();
        this.explanation = draft.explanation();
        this.relatedOccurrenceId = draft.relatedOccurrenceId();
        this.acceptedAt = Objects.requireNonNull(acceptedAt, "acceptedAt");
    }

    public EvaluationOccurrenceId id() {
        return id;
    }

    public Requirement requirement() {
        return requirement;
    }

    public Assertion<?> assertion() {
        return assertion;
    }

    public ModelFingerprint modelFingerprint() {
        return modelFingerprint;
    }

    public Optional<ControlledRevisionId> controlledRevisionIdOptional() {
        return Optional.ofNullable(controlledRevisionId);
    }

    public List<EvidenceUse> reliedOnUses() {
        return reliedOnUses;
    }

    public List<EvidenceUse> consideredButExcludedUses() {
        return consideredButExcludedUses;
    }

    public List<String> knownMaterialGaps() {
        return knownMaterialGaps;
    }

    public TemporalFrame temporalFrame() {
        return temporalFrame;
    }

    public String interpretationRules() {
        return interpretationRules;
    }

    public ConformanceEvaluation evaluation() {
        return evaluation;
    }

    public String explanation() {
        return explanation;
    }

    public Optional<EvaluationOccurrenceId> relatedOccurrenceIdOptional() {
        return Optional.ofNullable(relatedOccurrenceId);
    }

    public Instant acceptedAt() {
        return acceptedAt;
    }
}
