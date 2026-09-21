package com.arcogine.governance.evidence;

import com.arcogine.governance.assertion.Assertion;
import com.arcogine.governance.conformance.EvaluationOccurrenceId;
import com.arcogine.governance.requirement.Requirement;
import com.arcogine.governance.requirement.RequirementScope;
import com.arcogine.types.ControlledRevisionId;
import com.arcogine.types.ModelFingerprint;
import java.util.Objects;
import java.util.Optional;

/**
 * One contextual use of one exact evidence reference in one evaluation occurrence.
 *
 * <p>The target is intentionally a point identity: model fingerprint, optional verified revision,
 * and optional semantic scope. There is no operational-continuation target in this contract.
 */
public record EvidenceUse(
        EvaluationOccurrenceId occurrenceId,
        int position,
        EvidenceReference evidence,
        ModelFingerprint targetModelFingerprint,
        ControlledRevisionId targetControlledRevision,
        RequirementScope targetScope,
        Requirement requirement,
        Assertion<?> assertion,
        EvidenceUseRole role,
        TemporalFrame temporalFrame,
        EvidenceApplicability applicability) {

    public EvidenceUse {
        Objects.requireNonNull(occurrenceId, "occurrenceId");
        if (position < 0) {
            throw new IllegalArgumentException("position must not be negative");
        }
        Objects.requireNonNull(evidence, "evidence");
        Objects.requireNonNull(targetModelFingerprint, "targetModelFingerprint");
        Objects.requireNonNull(requirement, "requirement");
        Objects.requireNonNull(assertion, "assertion");
        Objects.requireNonNull(role, "role");
        temporalFrame = Objects.requireNonNull(temporalFrame, "temporalFrame");
        applicability = Objects.requireNonNull(applicability, "applicability");
        if (!requirement.id().equals(assertion.requirementId())
                || !requirement.version().equals(assertion.requirementVersion())) {
            throw new IllegalArgumentException("assertion does not target the supplied requirement");
        }
        if (role == EvidenceUseRole.RELIED_ON && !applicability.isAdequate()) {
            throw new IllegalArgumentException("a relied-on use must have an applicable basis");
        }
    }

    public Optional<ControlledRevisionId> targetControlledRevisionOptional() {
        return Optional.ofNullable(targetControlledRevision);
    }

    public Optional<RequirementScope> targetScopeOptional() {
        return Optional.ofNullable(targetScope);
    }

    public boolean isReliedOn() {
        return role == EvidenceUseRole.RELIED_ON;
    }

    public boolean isConsideredButNotReliedOn() {
        return role == EvidenceUseRole.CONSIDERED_BUT_NOT_RELIED_ON;
    }
}
