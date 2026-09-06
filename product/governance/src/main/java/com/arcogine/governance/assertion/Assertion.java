package com.arcogine.governance.assertion;

import com.arcogine.governance.requirement.RequirementId;
import com.arcogine.governance.requirement.RequirementVersion;
import java.util.Objects;
import java.util.Optional;

/**
 * "How does Arcogine determine whether an obligation is satisfied?" -- a stable, independently
 * versioned Governance assertion associated with one requirement identity/version.
 *
 * <p>{@code id}/{@code version} are Arcogine-owned identity, independent of: the requirement's
 * own version (a requirement can gain a new assertion version, or an assertion can be
 * re-implemented, without the other changing); the model/revision evaluated; and -- critically --
 * the {@link AssertionRule} implementation class, which is never consulted for identity or
 * equality (see {@link #equals(Object)}).
 *
 * <p>{@code evidenceRequirement} declares only whether authoritative model state is sufficient or
 * external evidence is needed; it never ingests, persists, or evaluates that evidence (the external-evidence capability). A
 * {@link EvidenceRequirement#MODEL_STATE_SUFFICIENT} assertion must always supply a structural
 * {@code rule} -- the declaration would otherwise be a claim with no executable backing. {@code
 * rule} is optional only for {@link EvidenceRequirement#EXTERNAL_EVIDENCE_REQUIRED}, since the requirement-scope capability
 * does not implement external-evidence evaluation.
 */
public record Assertion<T>(
        AssertionId id,
        AssertionVersion version,
        RequirementId requirementId,
        RequirementVersion requirementVersion,
        String description,
        EvidenceRequirement evidenceRequirement,
        AssertionRule<T> rule) {

    public Assertion {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(requirementId, "requirementId");
        Objects.requireNonNull(requirementVersion, "requirementVersion");
        Objects.requireNonNull(evidenceRequirement, "evidenceRequirement");
        description = description == null ? "" : description;
        if (evidenceRequirement == EvidenceRequirement.MODEL_STATE_SUFFICIENT && rule == null) {
            throw new IllegalArgumentException(
                    "an assertion declaring MODEL_STATE_SUFFICIENT must supply a structural AssertionRule"
                            + " -- only EXTERNAL_EVIDENCE_REQUIRED may omit one");
        }
    }

    public Optional<AssertionRule<T>> ruleOptional() {
        return Optional.ofNullable(rule);
    }

    public boolean requiresExternalEvidence() {
        return evidenceRequirement == EvidenceRequirement.EXTERNAL_EVIDENCE_REQUIRED;
    }

    public StructuralAssertionOutcome evaluate(T authoritativeState) {
        if (rule == null) {
            throw new IllegalStateException("assertion " + id + " " + version + " has no structural rule");
        }
        return rule.evaluate(authoritativeState);
    }

    /**
     * Identity/equality is {@code id}+{@code version} only -- never the {@link AssertionRule}
     * implementation, which is not durable semantic identity.
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof Assertion<?> assertion
                && id.equals(assertion.id)
                && version.equals(assertion.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version);
    }
}
