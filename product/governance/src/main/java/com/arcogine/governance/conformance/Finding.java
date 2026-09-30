package com.arcogine.governance.conformance;

import com.arcogine.governance.assertion.AssertionId;
import com.arcogine.governance.assertion.AssertionVersion;
import com.arcogine.governance.change.ChangedEntityRef;
import com.arcogine.governance.requirement.RequirementId;
import com.arcogine.governance.requirement.RequirementVersion;
import com.arcogine.types.ControlledRevisionId;
import com.arcogine.types.ModelFingerprint;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable conformance-evaluation record of one violated {@link ConformanceEvaluation}.
 *
 * <p>A {@code Finding} exists only for {@link ConformanceResult#FAIL}; {@link
 * ConformanceEvaluator} never produces one for {@code PASS}, {@code UNKNOWN}, or {@code
 * NOT_APPLICABLE} (see {@link ConformanceEvaluation#finding()}). This value records the failed
 * evaluation's requirement/assertion identity, evaluated semantic identity, affected entities, and
 * explanation. It does not model severity, remediation state, exception/risk-acceptance reference,
 * authorization, or workflow status.
 *
 * <p>{@code controlledRevisionId} is {@code null}/empty when the evaluated candidate has not been
 * accepted through the persistence-acceptance boundary. Per
 * docs/architecture/controlled-revisions.md and the {@code ChangeSet}
 * precedent, a {@code Finding} never synthesizes a {@link ControlledRevisionId} for an unpersisted
 * candidate -- it records the {@link ModelFingerprint} it evaluated either way.
 */
public record Finding(
        RequirementId requirementId,
        RequirementVersion requirementVersion,
        AssertionId assertionId,
        AssertionVersion assertionVersion,
        ModelFingerprint modelFingerprint,
        ControlledRevisionId controlledRevisionId,
        List<ChangedEntityRef> affectedEntities,
        String explanation) {

    public Finding {
        Objects.requireNonNull(requirementId, "requirementId");
        Objects.requireNonNull(requirementVersion, "requirementVersion");
        Objects.requireNonNull(assertionId, "assertionId");
        Objects.requireNonNull(assertionVersion, "assertionVersion");
        Objects.requireNonNull(modelFingerprint, "modelFingerprint");
        affectedEntities = affectedEntities == null ? List.of() : List.copyOf(affectedEntities);
        explanation = explanation == null ? "" : explanation;
    }

    /** The controlled revision this finding was raised against, when the candidate is persisted. */
    public Optional<ControlledRevisionId> controlledRevisionIdOptional() {
        return Optional.ofNullable(controlledRevisionId);
    }
}
