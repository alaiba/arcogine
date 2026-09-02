package com.arcogine.governance.conformance;

import com.arcogine.governance.ControlledRevisionAuthority;
import com.arcogine.governance.HistoricalRevision;
import com.arcogine.governance.assertion.Assertion;
import com.arcogine.governance.assertion.EvidenceRequirement;
import com.arcogine.governance.assertion.StructuralAssertionOutcome;
import com.arcogine.governance.change.ChangedEntityRef;
import com.arcogine.governance.change.ImpactScope;
import com.arcogine.governance.requirement.Requirement;
import com.arcogine.types.ControlledRevisionId;
import com.arcogine.types.ModelFingerprint;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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
 *
 * <p><b>Provenance binding.</b> {@code modelFingerprint} and {@code authoritativeState} remain
 * independent caller-supplied values: the G4 assertion state {@code T} is arbitrary domain state,
 * not necessarily a {@code SemanticArtifact}'s canonical bytes, so no existing G1 authority can
 * recompute a fingerprint from it generically (unlike {@code ChangeSetFactory}, which can rely on
 * {@code SemanticArtifactVerifier} exactly because a {@code SemanticArtifact} is always
 * bytes-with-a-declared-fingerprint). What G4 <em>can</em> and does verify with an existing G1
 * authority is the one binding {@link ControlledRevisionAuthority} already establishes: when a
 * {@link ControlledRevisionId} is supplied, {@link #evaluate} resolves it through {@code
 * authority.resolve(...)} and requires the resolved revision's authoritative fingerprint to equal
 * {@code modelFingerprint}. A generated-but-never-accepted, or accepted-but-mismatched, revision
 * is therefore rejected rather than silently attributed to the evaluation; an unaccepted candidate
 * must be passed as {@code Optional.empty()} and remains revisionless.
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
     * @param authority the G1 {@link ControlledRevisionAuthority} used to verify, when {@code
     *     controlledRevisionId} is present, that it is an authoritative revision actually bound to
     *     {@code modelFingerprint}; ignored (but still required, non-null) when {@code
     *     controlledRevisionId} is empty
     * @throws com.arcogine.governance.GovernanceHistoryException if {@code controlledRevisionId}
     *     is present but was never accepted by {@code authority}
     * @throws IllegalArgumentException if {@code controlledRevisionId} is present but resolves to
     *     a fingerprint other than {@code modelFingerprint}
     */
    public static <T> ConformanceEvaluation evaluate(
            Requirement requirement,
            Assertion<T> assertion,
            Optional<ImpactScope> impactScope,
            Optional<T> authoritativeState,
            ModelFingerprint modelFingerprint,
            Optional<ControlledRevisionId> controlledRevisionId,
            ControlledRevisionAuthority authority) {
        Objects.requireNonNull(requirement, "requirement");
        Objects.requireNonNull(assertion, "assertion");
        Objects.requireNonNull(impactScope, "impactScope");
        Objects.requireNonNull(authoritativeState, "authoritativeState");
        Objects.requireNonNull(modelFingerprint, "modelFingerprint");
        Objects.requireNonNull(controlledRevisionId, "controlledRevisionId");
        Objects.requireNonNull(authority, "authority");
        requireMatchingAssertion(requirement, assertion);

        ControlledRevisionId revisionId =
                requireAuthoritativeBinding(controlledRevisionId, modelFingerprint, authority);

        if (impactScope.isPresent() && !requirement.scope().intersects(impactScope.get())) {
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
                        affectedEntities(requirement, impactScope),
                        outcome.explanation());
        return result(requirement, assertion, modelFingerprint, revisionId, ConformanceResult.FAIL, finding);
    }

    /**
     * Resolves and validates {@code controlledRevisionId} against the G1 {@link
     * ControlledRevisionAuthority}, when present. An unaccepted or generated revision fails
     * resolution; an accepted revision bound to a different fingerprint is rejected explicitly.
     */
    private static ControlledRevisionId requireAuthoritativeBinding(
            Optional<ControlledRevisionId> controlledRevisionId,
            ModelFingerprint modelFingerprint,
            ControlledRevisionAuthority authority) {
        if (controlledRevisionId.isEmpty()) {
            return null;
        }
        ControlledRevisionId revisionId = controlledRevisionId.get();
        HistoricalRevision resolved = authority.resolve(revisionId);
        ModelFingerprint boundFingerprint = resolved.artifact().fingerprint();
        if (!boundFingerprint.equals(modelFingerprint)) {
            throw new IllegalArgumentException(
                    "controlledRevisionId "
                            + revisionId
                            + " is authoritatively bound to fingerprint "
                            + boundFingerprint
                            + ", not the evaluated fingerprint "
                            + modelFingerprint);
        }
        return revisionId;
    }

    /**
     * The entities a {@link Finding} attributes as affected: the requirement's full scope when no
     * {@link ImpactScope} was supplied (an explicit unconditional-evaluation mode), otherwise only
     * the subset of the requirement's scope actually touched by the supplied {@link ImpactScope} --
     * never the requirement's full scope when only part of it was impacted.
     */
    private static List<ChangedEntityRef> affectedEntities(
            Requirement requirement, Optional<ImpactScope> impactScope) {
        List<ChangedEntityRef> scopeEntities = requirement.scope().entities();
        if (impactScope.isEmpty()) {
            return scopeEntities;
        }
        Set<ChangedEntityRef> impacted = new LinkedHashSet<>(impactScope.get().affectedEntities());
        List<ChangedEntityRef> intersection = new ArrayList<>();
        for (ChangedEntityRef entity : scopeEntities) {
            if (impacted.contains(entity)) {
                intersection.add(entity);
            }
        }
        return intersection;
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
