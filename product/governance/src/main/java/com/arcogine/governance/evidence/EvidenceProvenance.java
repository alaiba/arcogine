package com.arcogine.governance.evidence;

import com.arcogine.types.ControlledRevisionId;
import com.arcogine.types.EngineSemantics;
import com.arcogine.types.ModelFingerprint;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable intrinsic meaning and provenance retained with an evidence reference.
 *
 * <p>Optional values are deliberately explicit. Governance does not infer a missing producer
 * model, Engine interpretation, subject, or result boundary from the later use. A producer model
 * fingerprint or Engine interpretation named by a work-in-progress marker identifies the definition
 * current when the evidence was produced only for that development revision; it is development
 * provenance, not a durable cross-revision identity.
 */
public record EvidenceProvenance(
        String sourceDescription,
        Optional<String> describedSubject,
        Optional<String> recordedStatement,
        Optional<String> units,
        Optional<Instant> producedAt,
        Optional<ModelFingerprint> producerModelFingerprint,
        Optional<ControlledRevisionId> producerControlledRevision,
        Optional<EngineSemantics> engineSemantics,
        Optional<String> producerOccurrence,
        Optional<String> analyticalDefinition,
        Map<String, String> materialInputs,
        Optional<Instant> receivedAt,
        Optional<String> trustOrQuality) {

    public EvidenceProvenance {
        requireText(sourceDescription, "sourceDescription");
        describedSubject = requireOptionalText(describedSubject, "describedSubject");
        recordedStatement = requireOptionalText(recordedStatement, "recordedStatement");
        units = requireOptionalText(units, "units");
        producedAt = Objects.requireNonNull(producedAt, "producedAt");
        producerModelFingerprint = Objects.requireNonNull(producerModelFingerprint, "producerModelFingerprint");
        producerControlledRevision = Objects.requireNonNull(producerControlledRevision, "producerControlledRevision");
        engineSemantics = Objects.requireNonNull(engineSemantics, "engineSemantics");
        producerOccurrence = requireOptionalText(producerOccurrence, "producerOccurrence");
        analyticalDefinition = requireOptionalText(analyticalDefinition, "analyticalDefinition");
        materialInputs = materialInputs == null ? Map.of() : Map.copyOf(materialInputs);
        receivedAt = Objects.requireNonNull(receivedAt, "receivedAt");
        trustOrQuality = requireOptionalText(trustOrQuality, "trustOrQuality");
    }

    /** Provenance for a structural fact produced by an accepted controlled revision. */
    public static EvidenceProvenance structural(
            ModelFingerprint modelFingerprint, ControlledRevisionId controlledRevisionId) {
        return new EvidenceProvenance(
                "Arcogine controlled semantic state",
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(Objects.requireNonNull(modelFingerprint, "modelFingerprint")),
                Optional.of(Objects.requireNonNull(controlledRevisionId, "controlledRevisionId")),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Map.of(),
                Optional.empty(),
                Optional.empty());
    }

    /** Explicit analytical provenance fixture; no missing Engine interpretation is synthesized. */
    public static EvidenceProvenance analytical(
            ModelFingerprint modelFingerprint,
            Optional<ControlledRevisionId> controlledRevisionId,
            Optional<EngineSemantics> engineSemantics,
            String resultIdentity,
            Map<String, String> materialInputs,
            Optional<String> analyticalDefinition) {
        return new EvidenceProvenance(
                "Arcogine-derived analytical result",
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(Objects.requireNonNull(modelFingerprint, "modelFingerprint")),
                Objects.requireNonNull(controlledRevisionId, "controlledRevisionId"),
                Objects.requireNonNull(engineSemantics, "engineSemantics"),
                Optional.of(requireText(resultIdentity, "resultIdentity")),
                Objects.requireNonNull(analyticalDefinition, "analyticalDefinition"),
                materialInputs,
                Optional.empty(),
                Optional.empty());
    }

    /** Explicit external-observation fixture with no fabricated Arcogine subject. */
    public static EvidenceProvenance externalObservation(
            String sourceDescription,
            Optional<String> externalSubject,
            Optional<Instant> observedAt,
            Optional<Instant> receivedAt,
            Optional<String> trustOrQuality) {
        return new EvidenceProvenance(
                sourceDescription,
                externalSubject,
                Optional.empty(),
                Optional.empty(),
                observedAt,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Map.of(),
                receivedAt,
                trustOrQuality);
    }

    /** Explicitly unknown intrinsic provenance. */
    public static EvidenceProvenance unknown(String explanation) {
        return new EvidenceProvenance(
                "Unknown provenance: " + requireText(explanation, "explanation"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Map.of(),
                Optional.empty(),
                Optional.empty());
    }

    private static String requireText(String value, String field) {
        if (Objects.requireNonNull(value, field).isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }

    private static Optional<String> requireOptionalText(Optional<String> value, String field) {
        Objects.requireNonNull(value, field);
        value.ifPresent(item -> requireText(item, field));
        return value;
    }
}
