package com.arcogine.governance.conformance;

import com.arcogine.governance.assertion.AssertionId;
import com.arcogine.governance.assertion.AssertionVersion;
import com.arcogine.governance.requirement.RequirementId;
import com.arcogine.governance.requirement.RequirementVersion;
import com.arcogine.types.ControlledRevisionId;
import com.arcogine.types.ModelFingerprint;
import java.util.Objects;
import java.util.Optional;

/**
 * The G4 outcome of evaluating one {@code Assertion}, for one {@code Requirement} identity/
 * version, against one {@code ModelFingerprint} (and, when available, one {@code
 * ControlledRevisionId}).
 *
 * <p>This is the minimal shape from the architecture's generic conformance model (§7) that G4
 * actually implements: requirement/assertion identity and version, the fingerprint/revision
 * evaluated, the {@link ConformanceResult}, and -- only for {@link ConformanceResult#FAIL} -- the
 * associated {@link Finding}. It deliberately omits {@code observed-at}/applicable-period and
 * evidence-set fields from the architecture's full sketch: those require G5 {@code Evidence}/
 * {@code EvidenceUse}, which this slice does not implement.
 *
 * <p>{@code controlledRevisionId} is {@code null} when the evaluated state is an unpersisted
 * candidate; this record never synthesizes one, mirroring {@code ChangeSet#resultingRevisionId}.
 */
public record ConformanceEvaluation(
        RequirementId requirementId,
        RequirementVersion requirementVersion,
        AssertionId assertionId,
        AssertionVersion assertionVersion,
        ModelFingerprint modelFingerprint,
        ControlledRevisionId controlledRevisionId,
        ConformanceResult result,
        Finding finding) {

    public ConformanceEvaluation {
        Objects.requireNonNull(requirementId, "requirementId");
        Objects.requireNonNull(requirementVersion, "requirementVersion");
        Objects.requireNonNull(assertionId, "assertionId");
        Objects.requireNonNull(assertionVersion, "assertionVersion");
        Objects.requireNonNull(modelFingerprint, "modelFingerprint");
        Objects.requireNonNull(result, "result");
        if (result == ConformanceResult.FAIL && finding == null) {
            throw new IllegalArgumentException("a FAIL result must carry a Finding");
        }
        if (result != ConformanceResult.FAIL && finding != null) {
            throw new IllegalArgumentException(
                    "only a FAIL result may carry a Finding, was " + result);
        }
    }

    public Optional<ControlledRevisionId> controlledRevisionIdOptional() {
        return Optional.ofNullable(controlledRevisionId);
    }

    public Optional<Finding> findingOptional() {
        return Optional.ofNullable(finding);
    }
}
