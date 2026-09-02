package com.arcogine.governance.conformance;

import com.arcogine.governance.assertion.Assertion;
import com.arcogine.governance.assertion.EvidenceRequirement;
import com.arcogine.governance.assertion.StructuralAssertionOutcome;
import com.arcogine.governance.change.ImpactScope;
import com.arcogine.governance.requirement.Requirement;
import com.arcogine.types.ControlledRevisionId;
import com.arcogine.types.ModelFingerprint;
import java.util.Objects;
import java.util.Optional;

/**
 * The G4 conformance-evaluation engine: turns one G3 {@link Requirement}/{@link Assertion} pair
 * and a candidate's authoritative state into one {@link ConformanceEvaluation}, consuming the real
 * G2 {@link ImpactScope} and G3 {@link com.arcogine.governance.requirement.RequirementScope}
 * contracts rather than any new scope representation.
 *
 * <p>This is deliberately narrow: it decides {@link ConformanceResult#PASS}/{@link
 * ConformanceResult#FAIL}/{@link ConformanceResult#UNKNOWN}/{@link
 * ConformanceResult#NOT_APPLICABLE} for one assertion at a time. It does not implement G5
 * evidence, authorization, deployment, workflow, a severity taxonomy, persistence, or any
 * transport (REST/CLI/UI). It never calls {@code Instant.now()}, a random source, or any other
 * system clock -- every input (fingerprint, revision, scope, state) is supplied by the caller, so
 * evaluation stays deterministic and reproducible per the architecture's audit-snapshot invariant
 * (§12).
 *
 * <p>{@code controlledRevisionId} is optional and never synthesized: an unpersisted candidate
 * fingerprint is evaluated exactly like an accepted one, only without a {@link
 * ControlledRevisionId} attached to the result, mirroring the {@code ChangeSet}/G1.3 precedent
 * that a {@link ControlledRevisionId} exists only for an actually-accepted revision.
 */
public final class ConformanceEvaluator {

    private ConformanceEvaluator() {}

    /**
     * Evaluates one {@link Requirement}/{@link Assertion} pair.
     *
     * @param requirement the requirement whose {@code RequirementScope} determines applicability
     * @param assertion the executable (or evidence-pending) assertion for that requirement
     * @param impactScope the scope of what is being evaluated, when known; {@link
     *     Optional#empty()} means "evaluate unconditionally" (no applicability filtering)
     * @param authoritativeState the candidate's authoritative state for {@code T}, when available
     * @param modelFingerprint the fingerprint of the state being evaluated
     * @param controlledRevisionId the accepted controlled revision, when the candidate has been
     *     persisted through the G1.3 authority boundary; empty for an unpersisted candidate
     */
    public static <T> ConformanceEvaluation evaluate(
            Requirement requirement,
            Assertion<T> assertion,
            Optional<ImpactScope> impactScope,
            Optional<T> authoritativeState,
            ModelFingerprint modelFingerprint,
            Optional<ControlledRevisionId> controlledRevisionId) {
        Objects.requireNonNull(requirement, "requirement");
        Objects.requireNonNull(assertion, "assertion");
        Objects.requireNonNull(impactScope, "impactScope");
        Objects.requireNonNull(authoritativeState, "authoritativeState");
        Objects.requireNonNull(modelFingerprint, "modelFingerprint");
        Objects.requireNonNull(controlledRevisionId, "controlledRevisionId");
        requireMatchingAssertion(requirement, assertion);

        ControlledRevisionId revisionId = controlledRevisionId.orElse(null);

        if (impactScope.isPresent()
                && !requirement.scope().isEmpty()
                && !requirement.scope().intersects(impactScope.get())) {
            return result(
                    requirement, assertion, modelFingerprint, revisionId, ConformanceResult.NOT_APPLICABLE, null);
        }

        if (assertion.evidenceRequirement() == EvidenceRequirement.EXTERNAL_EVIDENCE_REQUIRED) {
            // G4 does not implement G5 evidence: an external-evidence assertion can never be
            // decided here. This is UNKNOWN, never a silent PASS or FAIL.
            return result(
                    requirement, assertion, modelFingerprint, revisionId, ConformanceResult.UNKNOWN, null);
        }

        if (authoritativeState.isEmpty()) {
            // MODEL_STATE_SUFFICIENT declares the fact is decidable from model state, but no
            // state was supplied to decide it with -- still UNKNOWN, not a fabricated result.
            return result(
                    requirement, assertion, modelFingerprint, revisionId, ConformanceResult.UNKNOWN, null);
        }

        StructuralAssertionOutcome outcome = assertion.evaluate(authoritativeState.get());
        if (outcome.satisfied()) {
            return result(requirement, assertion, modelFingerprint, revisionId, ConformanceResult.PASS, null);
        }

        Finding finding =
                new Finding(
                        requirement.id(),
                        requirement.version(),
                        assertion.id(),
                        assertion.version(),
                        modelFingerprint,
                        revisionId,
                        requirement.scope().entities(),
                        outcome.explanation());
        return result(requirement, assertion, modelFingerprint, revisionId, ConformanceResult.FAIL, finding);
    }

    private static <T> void requireMatchingAssertion(Requirement requirement, Assertion<T> assertion) {
        if (!requirement.id().equals(assertion.requirementId())
                || !requirement.version().equals(assertion.requirementVersion())) {
            throw new IllegalArgumentException(
                    "assertion "
                            + assertion.id()
                            + " "
                            + assertion.version()
                            + " does not target requirement "
                            + requirement.id()
                            + " "
                            + requirement.version());
        }
    }

    private static <T> ConformanceEvaluation result(
            Requirement requirement,
            Assertion<T> assertion,
            ModelFingerprint modelFingerprint,
            ControlledRevisionId revisionId,
            ConformanceResult result,
            Finding finding) {
        return new ConformanceEvaluation(
                requirement.id(),
                requirement.version(),
                assertion.id(),
                assertion.version(),
                modelFingerprint,
                revisionId,
                result,
                finding);
    }
}
