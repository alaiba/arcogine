package com.arcogine.governance.conformance;

import com.arcogine.governance.ControlledRevisionAuthority;
import com.arcogine.governance.HistoricalRevision;
import com.arcogine.governance.assertion.Assertion;
import com.arcogine.governance.assertion.EvidenceRequirement;
import com.arcogine.governance.change.ChangedEntityRef;
import com.arcogine.governance.change.ImpactScope;
import com.arcogine.governance.evidence.EvidenceUse;
import com.arcogine.governance.evidence.EvidenceUseRole;
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
 * Composes an explicit evidence basis with the existing conformance result taxonomy.
 *
 * <p>This class does not inspect external observations or perform analytical calculations. The
 * caller supplies a claim-specific {@link EvidenceJudgment}; this boundary only ensures that a
 * positive or negative outcome has an applicable relied-on use and that target bindings are
 * valid. Missing or unresolved evidence therefore remains {@link ConformanceResult#UNKNOWN}.
 */
public final class EvidenceConformanceEvaluator {

    private EvidenceConformanceEvaluator() {}

    public static ConformanceEvaluation evaluate(
            Requirement requirement,
            Assertion<?> assertion,
            Optional<ImpactScope> impactScope,
            ModelFingerprint modelFingerprint,
            Optional<ControlledRevisionId> controlledRevisionId,
            ControlledRevisionAuthority revisionAuthority,
            List<EvidenceUse> evidenceUses,
            EvidenceJudgment judgment) {
        Objects.requireNonNull(requirement, "requirement");
        Objects.requireNonNull(assertion, "assertion");
        Objects.requireNonNull(impactScope, "impactScope");
        Objects.requireNonNull(modelFingerprint, "modelFingerprint");
        Objects.requireNonNull(controlledRevisionId, "controlledRevisionId");
        Objects.requireNonNull(revisionAuthority, "revisionAuthority");
        evidenceUses = List.copyOf(Objects.requireNonNull(evidenceUses, "evidenceUses"));
        Objects.requireNonNull(judgment, "judgment");
        requireMatchingAssertion(requirement, assertion);
        ControlledRevisionId verifiedRevision = verifyRevision(
                controlledRevisionId, modelFingerprint, revisionAuthority);

        if (impactScope.isPresent() && !requirement.scope().intersects(impactScope.get())) {
            return result(requirement, assertion, modelFingerprint, verifiedRevision,
                    ConformanceResult.NOT_APPLICABLE, null);
        }
        if (assertion.evidenceRequirement() != EvidenceRequirement.EXTERNAL_EVIDENCE_REQUIRED) {
            throw new IllegalArgumentException("evidence composition requires an external-evidence assertion");
        }
        validateUses(requirement, assertion, modelFingerprint, evidenceUses, revisionAuthority);

        boolean adequateBasis = evidenceUses.stream()
                .anyMatch(use -> use.isReliedOn() && use.applicability().isAdequate());
        if ((judgment.result() == ConformanceResult.PASS || judgment.result() == ConformanceResult.FAIL)
                && !adequateBasis) {
            return result(requirement, assertion, modelFingerprint, verifiedRevision,
                    ConformanceResult.UNKNOWN, null);
        }
        if (judgment.result() == ConformanceResult.FAIL) {
            Finding finding = new Finding(
                    requirement.id(),
                    requirement.version(),
                    assertion.id(),
                    assertion.version(),
                    modelFingerprint,
                    verifiedRevision,
                    affectedEntities(requirement, impactScope),
                    judgment.explanation());
            return result(requirement, assertion, modelFingerprint, verifiedRevision,
                    ConformanceResult.FAIL, finding);
        }
        return result(requirement, assertion, modelFingerprint, verifiedRevision,
                judgment.result(), null);
    }

    private static void validateUses(
            Requirement requirement,
            Assertion<?> assertion,
            ModelFingerprint modelFingerprint,
            List<EvidenceUse> evidenceUses,
            ControlledRevisionAuthority revisionAuthority) {
        for (EvidenceUse use : evidenceUses) {
            if (use.requirement() != requirement || use.assertion() != assertion) {
                throw new IllegalArgumentException("evidence use does not retain the exact definitions");
            }
            if (use.role() != EvidenceUseRole.COMPARATOR
                    && !use.targetModelFingerprint().equals(modelFingerprint)) {
                throw new IllegalArgumentException("evidence use targets a different model without comparator role");
            }
            if (use.targetControlledRevision() != null) {
                HistoricalRevision historical = revisionAuthority.resolve(use.targetControlledRevision());
                if (!historical.artifact().fingerprint().equals(use.targetModelFingerprint())) {
                    throw new IllegalArgumentException("evidence use revision is bound to a different model fingerprint");
                }
            }
        }
    }

    private static ControlledRevisionId verifyRevision(
            Optional<ControlledRevisionId> revisionId,
            ModelFingerprint modelFingerprint,
            ControlledRevisionAuthority authority) {
        if (revisionId.isEmpty()) {
            return null;
        }
        HistoricalRevision historical = authority.resolve(revisionId.get());
        if (!historical.artifact().fingerprint().equals(modelFingerprint)) {
            throw new IllegalArgumentException("controlled revision is bound to a different model fingerprint");
        }
        return revisionId.get();
    }

    private static List<ChangedEntityRef> affectedEntities(
            Requirement requirement, Optional<ImpactScope> impactScope) {
        if (impactScope.isEmpty()) {
            return requirement.scope().entities();
        }
        Set<ChangedEntityRef> impacted = new LinkedHashSet<>(impactScope.get().affectedEntities());
        List<ChangedEntityRef> intersection = new ArrayList<>();
        for (ChangedEntityRef entity : requirement.scope().entities()) {
            if (impacted.contains(entity)) {
                intersection.add(entity);
            }
        }
        return intersection;
    }

    private static void requireMatchingAssertion(Requirement requirement, Assertion<?> assertion) {
        if (!requirement.id().equals(assertion.requirementId())
                || !requirement.version().equals(assertion.requirementVersion())) {
            throw new IllegalArgumentException("assertion does not target the supplied requirement");
        }
    }

    private static ConformanceEvaluation result(
            Requirement requirement,
            Assertion<?> assertion,
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
