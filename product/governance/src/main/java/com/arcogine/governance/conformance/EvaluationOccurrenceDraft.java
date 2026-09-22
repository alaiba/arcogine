package com.arcogine.governance.conformance;

import com.arcogine.governance.assertion.Assertion;
import com.arcogine.governance.evidence.EvidenceUse;
import com.arcogine.governance.evidence.EvidenceUseRole;
import com.arcogine.governance.evidence.TemporalFrame;
import com.arcogine.governance.requirement.Requirement;
import com.arcogine.types.ControlledRevisionId;
import com.arcogine.types.ModelFingerprint;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Candidate basis for one evaluation occurrence. It becomes historical only after authority
 * acceptance through {@link EvaluationOccurrenceAuthority}.
 */
public record EvaluationOccurrenceDraft(
        EvaluationOccurrenceId id,
        Requirement requirement,
        Assertion<?> assertion,
        ModelFingerprint modelFingerprint,
        ControlledRevisionId controlledRevisionId,
        List<EvidenceUse> reliedOnUses,
        List<EvidenceUse> consideredButExcludedUses,
        List<String> knownMaterialGaps,
        TemporalFrame temporalFrame,
        String interpretationRules,
        ConformanceEvaluation evaluation,
        String explanation,
        EvaluationOccurrenceId relatedOccurrenceId) {

    public EvaluationOccurrenceDraft {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(requirement, "requirement");
        Objects.requireNonNull(assertion, "assertion");
        Objects.requireNonNull(modelFingerprint, "modelFingerprint");
        reliedOnUses = copyUses(reliedOnUses, "reliedOnUses");
        consideredButExcludedUses = copyUses(consideredButExcludedUses, "consideredButExcludedUses");
        knownMaterialGaps = copyText(knownMaterialGaps, "knownMaterialGaps");
        temporalFrame = Objects.requireNonNull(temporalFrame, "temporalFrame");
        interpretationRules = requireText(interpretationRules, "interpretationRules");
        Objects.requireNonNull(evaluation, "evaluation");
        explanation = requireText(explanation, "explanation");
        if (!requirement.id().equals(assertion.requirementId())
                || !requirement.version().equals(assertion.requirementVersion())) {
            throw new IllegalArgumentException("assertion does not target the supplied requirement");
        }
        if (!evaluation.requirementId().equals(requirement.id())
                || !evaluation.requirementVersion().equals(requirement.version())
                || !evaluation.assertionId().equals(assertion.id())
                || !evaluation.assertionVersion().equals(assertion.version())
                || !evaluation.modelFingerprint().equals(modelFingerprint)
                || !Objects.equals(evaluation.controlledRevisionId(), controlledRevisionId)) {
            throw new IllegalArgumentException("evaluation does not match the occurrence basis");
        }
        validateUses(id, requirement, assertion, modelFingerprint, reliedOnUses, true);
        validateUses(id, requirement, assertion, modelFingerprint, consideredButExcludedUses, false);
        validateUniqueUsePositions(reliedOnUses, consideredButExcludedUses);
    }

    public static EvaluationOccurrenceDraft create(
            Requirement requirement,
            Assertion<?> assertion,
            ModelFingerprint modelFingerprint,
            Optional<ControlledRevisionId> controlledRevisionId,
            List<EvidenceUse> reliedOnUses,
            List<EvidenceUse> consideredButExcludedUses,
            List<String> knownMaterialGaps,
            TemporalFrame temporalFrame,
            String interpretationRules,
            ConformanceEvaluation evaluation,
            String explanation) {
        return new EvaluationOccurrenceDraft(
                EvaluationOccurrenceId.generate(),
                requirement,
                assertion,
                modelFingerprint,
                Objects.requireNonNull(controlledRevisionId, "controlledRevisionId").orElse(null),
                reliedOnUses,
                consideredButExcludedUses,
                knownMaterialGaps,
                temporalFrame,
                interpretationRules,
                evaluation,
                explanation,
                null);
    }

    public Optional<ControlledRevisionId> controlledRevisionIdOptional() {
        return Optional.ofNullable(controlledRevisionId);
    }

    public Optional<EvaluationOccurrenceId> relatedOccurrenceIdOptional() {
        return Optional.ofNullable(relatedOccurrenceId);
    }

    private static void validateUses(
            EvaluationOccurrenceId id,
            Requirement requirement,
            Assertion<?> assertion,
            ModelFingerprint modelFingerprint,
            List<EvidenceUse> uses,
            boolean reliedOn) {
        for (EvidenceUse use : uses) {
            if (!use.occurrenceId().equals(id)) {
                throw new IllegalArgumentException("evidence use belongs to a different occurrence");
            }
            if (use.requirement() != requirement || use.assertion() != assertion) {
                throw new IllegalArgumentException("evidence use must retain the exact occurrence definitions");
            }
            if (reliedOn != use.isReliedOn()) {
                throw new IllegalArgumentException("evidence use is in the wrong occurrence collection");
            }
            if (use.role() != EvidenceUseRole.COMPARATOR
                    && !use.targetModelFingerprint().equals(modelFingerprint)) {
                throw new IllegalArgumentException("non-comparator use must target the evaluated model");
            }
        }
    }

    private static void validateUniqueUsePositions(
            List<EvidenceUse> reliedOnUses, List<EvidenceUse> consideredButExcludedUses) {
        Set<Integer> positions = new HashSet<>();
        for (EvidenceUse use : reliedOnUses) {
            if (!positions.add(use.position())) {
                throw new IllegalArgumentException(
                        "evidence use position must be unique within an occurrence: " + use.position());
            }
        }
        for (EvidenceUse use : consideredButExcludedUses) {
            if (!positions.add(use.position())) {
                throw new IllegalArgumentException(
                        "evidence use position must be unique within an occurrence: " + use.position());
            }
        }
    }

    private static List<EvidenceUse> copyUses(List<EvidenceUse> value, String field) {
        return List.copyOf(Objects.requireNonNull(value, field));
    }

    private static List<String> copyText(List<String> value, String field) {
        Objects.requireNonNull(value, field);
        for (String item : value) {
            requireText(item, field + " item");
        }
        return List.copyOf(value);
    }

    private static String requireText(String value, String field) {
        if (Objects.requireNonNull(value, field).isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
