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
import java.util.function.Function;

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
 * <p><b>Provenance binding.</b> The G4 assertion state {@code T} is arbitrary domain state, not
 * necessarily a {@code SemanticArtifact}'s canonical bytes, so no existing G1 authority can
 * recompute a fingerprint from it generically the way {@code ChangeSetFactory} recomputes one from
 * {@code SemanticArtifact} canonical bytes via {@code SemanticArtifactVerifier}. Instead, {@link
 * #evaluate} requires the caller to supply {@code stateFingerprint}: a domain-owned {@link
 * Function} that independently derives the {@link ModelFingerprint} of a given {@code T} from its
 * own content (e.g. {@code FactoryModelVersion::fingerprint}, which recomputes a fingerprint from
 * the model's canonical bytes on every call, or another domain adapter analogous to {@code
 * SemanticArtifactVerifier}) rather than trusting a caller-asserted, unrelated fingerprint
 * parameter. {@code ChangeSet::candidateFingerprint} is not a valid {@code stateFingerprint}
 * adapter: it is a pass-through accessor of the {@code ChangeSet}'s own recorded value, not a
 * derivation from the evaluated subject's content, and {@code ChangeSet} itself drives
 * requirement/assertion *selection* through its {@code ImpactScope} rather than being the
 * semantic subject an assertion evaluates. When {@code authoritativeState} is present, {@link
 * #evaluate} applies {@code
 * stateFingerprint} to it and requires the result to equal {@code modelFingerprint} -- exactly
 * mirroring the {@code ChangeSetFactory#fromCandidateSnapshot} precedent of verifying a candidate's
 * declared fingerprint against an independent recomputation before trusting it -- so a caller can
 * no longer pass unrelated state under an unrelated-but-otherwise-valid fingerprint.
 *
 * <p>G4 also verifies, with the existing G1 {@link ControlledRevisionAuthority}, the one binding it
 * already establishes: when a {@link ControlledRevisionId} is supplied, {@link #evaluate} resolves
 * it through {@code authority.resolve(...)} and requires the resolved revision's authoritative
 * fingerprint to equal {@code modelFingerprint}. A generated-but-never-accepted, or
 * accepted-but-mismatched, revision is therefore rejected rather than silently attributed to the
 * evaluation; an unaccepted candidate must be passed as {@code Optional.empty()} and remains
 * revisionless.
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
     * @param stateFingerprint a domain-owned function that independently derives the {@link
     *     ModelFingerprint} of a given {@code T} (e.g. {@code ChangeSet::candidateFingerprint}),
     *     used to verify -- when {@code authoritativeState} is present -- that it actually is the
     *     artifact identified by {@code modelFingerprint}, rather than trusting the two as
     *     unrelated caller-supplied values
     * @param controlledRevisionId the accepted controlled revision, when the candidate has been
     *     persisted through the G1.3 authority boundary; empty for an unpersisted candidate
     * @param authority the G1 {@link ControlledRevisionAuthority} used to verify, when {@code
     *     controlledRevisionId} is present, that it is an authoritative revision actually bound to
     *     {@code modelFingerprint}; ignored (but still required, non-null) when {@code
     *     controlledRevisionId} is empty
     * @throws com.arcogine.governance.GovernanceHistoryException if {@code controlledRevisionId}
     *     is present but was never accepted by {@code authority}
     * @throws IllegalArgumentException if {@code controlledRevisionId} is present but resolves to
     *     a fingerprint other than {@code modelFingerprint}, or if {@code authoritativeState} is
     *     present but {@code stateFingerprint} derives a fingerprint other than {@code
     *     modelFingerprint} from it
     */
    public static <T> ConformanceEvaluation evaluate(
            Requirement requirement,
            Assertion<T> assertion,
            Optional<ImpactScope> impactScope,
            Optional<T> authoritativeState,
            ModelFingerprint modelFingerprint,
            Function<T, ModelFingerprint> stateFingerprint,
            Optional<ControlledRevisionId> controlledRevisionId,
            ControlledRevisionAuthority authority) {
        Objects.requireNonNull(requirement, "requirement");
        Objects.requireNonNull(assertion, "assertion");
        Objects.requireNonNull(impactScope, "impactScope");
        Objects.requireNonNull(authoritativeState, "authoritativeState");
        Objects.requireNonNull(modelFingerprint, "modelFingerprint");
        Objects.requireNonNull(stateFingerprint, "stateFingerprint");
        Objects.requireNonNull(controlledRevisionId, "controlledRevisionId");
        Objects.requireNonNull(authority, "authority");
        requireMatchingAssertion(requirement, assertion);
        requireVerifiedStateBinding(authoritativeState, modelFingerprint, stateFingerprint);

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
     * Verifies, when {@code authoritativeState} is present, that {@code stateFingerprint} derives
     * {@code modelFingerprint} from it -- i.e. that the state actually being evaluated is the
     * artifact the caller claims it is, rather than an unrelated object attributed to a valid but
     * unconnected fingerprint. Mirrors {@code ChangeSetFactory#fromCandidateSnapshot}'s
     * declared-vs-recomputed fingerprint verification, generalized to arbitrary {@code T} through a
     * caller-supplied domain adapter instead of {@code SemanticArtifactVerifier}'s canonical-bytes
     * recomputation.
     */
    private static <T> void requireVerifiedStateBinding(
            Optional<T> authoritativeState,
            ModelFingerprint modelFingerprint,
            Function<T, ModelFingerprint> stateFingerprint) {
        if (authoritativeState.isEmpty()) {
            return;
        }
        ModelFingerprint derived = stateFingerprint.apply(authoritativeState.get());
        if (!modelFingerprint.equals(derived)) {
            throw new IllegalArgumentException(
                    "authoritativeState is bound to fingerprint "
                            + derived
                            + ", not the evaluated fingerprint "
                            + modelFingerprint);
        }
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
