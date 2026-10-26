package com.arcogine.governance.evidence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.governance.ControlledRevision;
import com.arcogine.governance.ControlledRevisionAuthority;
import com.arcogine.governance.HistoricalRevision;
import com.arcogine.governance.RevisionProvenance;
import com.arcogine.governance.RevisionRecorder;
import com.arcogine.governance.SemanticArtifact;
import com.arcogine.governance.assertion.Assertion;
import com.arcogine.governance.assertion.AssertionId;
import com.arcogine.governance.assertion.AssertionVersion;
import com.arcogine.governance.assertion.EvidenceRequirement;
import com.arcogine.governance.change.ChangedEntityRef;
import com.arcogine.governance.change.ImpactScope;
import com.arcogine.governance.change.SemanticChange;
import com.arcogine.governance.change.SemanticChangeKind;
import com.arcogine.governance.conformance.ConformanceEvaluation;
import com.arcogine.governance.conformance.ConformanceResult;
import com.arcogine.governance.conformance.EvaluationOccurrence;
import com.arcogine.governance.conformance.EvaluationOccurrenceDraft;
import com.arcogine.governance.conformance.EvaluationOccurrenceId;
import com.arcogine.governance.conformance.EvidenceConformanceEvaluator;
import com.arcogine.governance.conformance.EvidenceJudgment;
import com.arcogine.governance.conformance.InMemoryEvaluationOccurrenceAuthority;
import com.arcogine.governance.requirement.ArcogineNativeRequirementSource;
import com.arcogine.governance.requirement.Requirement;
import com.arcogine.governance.requirement.RequirementId;
import com.arcogine.governance.requirement.RequirementScope;
import com.arcogine.governance.requirement.RequirementVersion;
import com.arcogine.types.ControlledRevisionId;
import com.arcogine.types.ModelFingerprint;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Binds evidence-use target revisions to the evaluated subject over the rollback lineage
 * R1(F1) -> R2(F2) -> R3(F1), through the public evaluator, the occurrence draft, and the occurrence
 * authority.
 */
class EvidenceUseRevisionBindingTest {

    private static final ModelFingerprint F1 = new ModelFingerprint("test", "sha256", "1".repeat(64));
    private static final ModelFingerprint F2 = new ModelFingerprint("test", "sha256", "2".repeat(64));
    private static final ControlledRevision R1 = revision(F1, List.of());
    private static final ControlledRevision R2 = revision(F2, List.of(R1.id()));
    private static final ControlledRevision R3 = revision(F1, List.of(R2.id()));
    private static final ControlledRevisionAuthority LINEAGE = authority(R1, R2, R3);
    private static final TemporalFrame FRAME = TemporalFrame.atKnowledgeBoundary(Instant.EPOCH);
    private static final EvidenceApplicability APPLICABLE =
            new EvidenceApplicability(EvidenceApplicabilityStatus.APPLICABLE, "usable", "rule");
    private static final EvidenceJudgment PASS = new EvidenceJudgment(ConformanceResult.PASS, "supported");
    private static final String SOURCE = "source";

    private static final String DIFFERENT_REVISION =
            "non-comparator evidence use names a different controlled revision than the evaluated subject";
    private static final String SUBJECT_WITHOUT_REVISION =
            "non-comparator evidence use names a controlled revision but the evaluated subject has none";
    private static final String FOREIGN_FINGERPRINT =
            "evidence use revision is bound to a different model fingerprint";

    @Test
    void nonComparatorUseRevisionMustNameExactlyTheEvaluatedSubjectRevision() {
        for (EvidenceUseRole role : List.of(EvidenceUseRole.RELIED_ON, EvidenceUseRole.CONSIDERED_BUT_NOT_RELIED_ON)) {
            ControlledRevisionId r3 = R3.id();

            assertAccepted(role, null, r3);
            assertAccepted(role, r3, r3);
            assertRefused(role, R1.id(), r3, DIFFERENT_REVISION, DIFFERENT_REVISION);
            assertRefused(role, R2.id(), r3, FOREIGN_FINGERPRINT, DIFFERENT_REVISION);

            assertAccepted(role, null, null);
            assertRefused(role, r3, null, SUBJECT_WITHOUT_REVISION, SUBJECT_WITHOUT_REVISION);
            assertRefused(role, R1.id(), null, SUBJECT_WITHOUT_REVISION, SUBJECT_WITHOUT_REVISION);
            assertRefused(role, R2.id(), null, FOREIGN_FINGERPRINT, SUBJECT_WITHOUT_REVISION);
        }
    }

    @Test
    void comparatorKeepsItsOwnRevisionTargetAndAloneYieldsUnknown() {
        Requirement requirement = requirement(RequirementScope.empty());
        Assertion<?> assertion = assertion(requirement);
        for (ControlledRevision target : List.of(R2, R1)) {
            for (ControlledRevisionId subject : Arrays.asList(R3.id(), null)) {
                EvaluationOccurrenceId id = EvaluationOccurrenceId.generate();
                EvidenceUse comparator = use(id, 0, "comparator", target.modelFingerprint(), target.id(),
                        requirement, assertion, EvidenceUseRole.COMPARATOR);
                for (ConformanceResult claimed : List.of(ConformanceResult.PASS, ConformanceResult.FAIL)) {
                    ConformanceEvaluation evaluation = EvidenceConformanceEvaluator.evaluate(
                            requirement, assertion, Optional.empty(), F1, Optional.ofNullable(subject),
                            LINEAGE, List.of(comparator), new EvidenceJudgment(claimed, "comparison only"));
                    assertEquals(ConformanceResult.UNKNOWN, evaluation.result());
                    assertTrue(evaluation.findingOptional().isEmpty());
                }
                ConformanceEvaluation evaluation = EvidenceConformanceEvaluator.evaluate(
                        requirement, assertion, Optional.empty(), F1, Optional.ofNullable(subject),
                        LINEAGE, List.of(comparator), PASS);

                EvaluationOccurrence accepted = new InMemoryEvaluationOccurrenceAuthority(LINEAGE).accept(
                        draft(id, requirement, assertion, subject, List.of(), List.of(comparator), evaluation));

                EvidenceUse retained = accepted.consideredButExcludedUses().get(0);
                assertEquals(target.modelFingerprint(), retained.targetModelFingerprint());
                assertEquals(Optional.of(target.id()), retained.targetControlledRevisionOptional());
                assertEquals(Optional.ofNullable(subject), accepted.controlledRevisionIdOptional());
                assertEquals(ConformanceResult.UNKNOWN, accepted.evaluation().result());
            }
        }
    }

    @Test
    void excludedUseForAnotherRevisionRefusesAnOtherwiseExactBasis() {
        Requirement requirement = requirement(RequirementScope.empty());
        Assertion<?> assertion = assertion(requirement);
        EvaluationOccurrenceId id = EvaluationOccurrenceId.generate();
        EvidenceUse reliedOn = use(id, 0, "relied", F1, R3.id(), requirement, assertion, EvidenceUseRole.RELIED_ON);
        EvidenceUse excluded = use(id, 1, "excluded", F1, R1.id(), requirement, assertion,
                EvidenceUseRole.CONSIDERED_BUT_NOT_RELIED_ON);

        IllegalArgumentException evaluatorFailure = assertThrows(IllegalArgumentException.class,
                () -> EvidenceConformanceEvaluator.evaluate(requirement, assertion, Optional.empty(), F1,
                        Optional.of(R3.id()), LINEAGE, List.of(reliedOn, excluded), PASS));
        assertEquals(DIFFERENT_REVISION, evaluatorFailure.getMessage());

        InMemoryEvidenceReferenceAuthority references = new InMemoryEvidenceReferenceAuthority();
        InMemoryEvaluationOccurrenceAuthority occurrences = new InMemoryEvaluationOccurrenceAuthority(LINEAGE, references);
        ConformanceEvaluation claimed = evaluation(requirement, assertion, R3.id(), ConformanceResult.PASS);
        IllegalArgumentException occurrenceFailure = assertThrows(IllegalArgumentException.class,
                () -> occurrences.accept(draft(id, requirement, assertion, R3.id(),
                        List.of(reliedOn), List.of(excluded), claimed)));
        assertEquals(DIFFERENT_REVISION, occurrenceFailure.getMessage());
        assertUnchanged(occurrences, references, "relied", "excluded");
    }

    @Test
    void scopeExcludedSubjectIsNotApplicableBeforeUseRevisionBinding() {
        ChangedEntityRef cutter = new ChangedEntityRef("factory.resource", "1", "cutter");
        ChangedEntityRef assembler = new ChangedEntityRef("factory.resource", "2", "assembler");
        Requirement requirement = requirement(RequirementScope.of(cutter));
        Assertion<?> assertion = assertion(requirement);
        ImpactScope unrelated = ImpactScope.of(List.of(
                new SemanticChange(SemanticChangeKind.ENTITY_MODIFIED, assembler, "changed")));
        EvidenceUse staleRevision = use(EvaluationOccurrenceId.generate(), 0, "record", F1, R1.id(),
                requirement, assertion, EvidenceUseRole.RELIED_ON);

        for (ControlledRevisionId subject : Arrays.asList(R3.id(), null)) {
            ConformanceEvaluation evaluation = EvidenceConformanceEvaluator.evaluate(
                    requirement, assertion, Optional.of(unrelated), F1, Optional.ofNullable(subject),
                    LINEAGE, List.of(staleRevision), PASS);
            assertEquals(ConformanceResult.NOT_APPLICABLE, evaluation.result());
            assertTrue(evaluation.findingOptional().isEmpty());
        }
    }

    private static void assertAccepted(
            EvidenceUseRole role, ControlledRevisionId useRevision, ControlledRevisionId subjectRevision) {
        Requirement requirement = requirement(RequirementScope.empty());
        Assertion<?> assertion = assertion(requirement);
        EvaluationOccurrenceId id = EvaluationOccurrenceId.generate();
        EvidenceUse use = use(id, 0, "record", F1, useRevision, requirement, assertion, role);
        boolean reliedOn = role == EvidenceUseRole.RELIED_ON;
        ConformanceResult expected = reliedOn ? ConformanceResult.PASS : ConformanceResult.UNKNOWN;

        ConformanceEvaluation evaluation = EvidenceConformanceEvaluator.evaluate(
                requirement, assertion, Optional.empty(), F1, Optional.ofNullable(subjectRevision),
                LINEAGE, List.of(use), PASS);
        assertEquals(expected, evaluation.result());

        EvaluationOccurrence accepted = new InMemoryEvaluationOccurrenceAuthority(LINEAGE).accept(draft(
                id, requirement, assertion, subjectRevision,
                reliedOn ? List.of(use) : List.of(), reliedOn ? List.of() : List.of(use), evaluation));
        EvidenceUse retained = reliedOn
                ? accepted.reliedOnUses().get(0)
                : accepted.consideredButExcludedUses().get(0);
        assertEquals(Optional.ofNullable(useRevision), retained.targetControlledRevisionOptional());
        assertEquals(Optional.ofNullable(subjectRevision), accepted.controlledRevisionIdOptional());
        assertEquals(expected, accepted.evaluation().result());
    }

    private static void assertRefused(
            EvidenceUseRole role,
            ControlledRevisionId useRevision,
            ControlledRevisionId subjectRevision,
            String evaluatorMessage,
            String occurrenceMessage) {
        Requirement requirement = requirement(RequirementScope.empty());
        Assertion<?> assertion = assertion(requirement);
        EvaluationOccurrenceId id = EvaluationOccurrenceId.generate();
        EvidenceUse use = use(id, 0, "record", F1, useRevision, requirement, assertion, role);
        boolean reliedOn = role == EvidenceUseRole.RELIED_ON;

        IllegalArgumentException evaluatorFailure = assertThrows(IllegalArgumentException.class,
                () -> EvidenceConformanceEvaluator.evaluate(requirement, assertion, Optional.empty(), F1,
                        Optional.ofNullable(subjectRevision), LINEAGE, List.of(use), PASS));
        assertEquals(evaluatorMessage, evaluatorFailure.getMessage());

        InMemoryEvidenceReferenceAuthority references = new InMemoryEvidenceReferenceAuthority();
        InMemoryEvaluationOccurrenceAuthority occurrences = new InMemoryEvaluationOccurrenceAuthority(LINEAGE, references);
        ConformanceEvaluation claimed = evaluation(requirement, assertion, subjectRevision,
                reliedOn ? ConformanceResult.PASS : ConformanceResult.UNKNOWN);
        IllegalArgumentException occurrenceFailure = assertThrows(IllegalArgumentException.class,
                () -> occurrences.accept(draft(id, requirement, assertion, subjectRevision,
                        reliedOn ? List.of(use) : List.of(), reliedOn ? List.of() : List.of(use), claimed)));
        assertEquals(occurrenceMessage, occurrenceFailure.getMessage());
        assertUnchanged(occurrences, references, "record");
    }

    private static void assertUnchanged(
            InMemoryEvaluationOccurrenceAuthority occurrences,
            InMemoryEvidenceReferenceAuthority references,
            String... revisionIdentities) {
        assertTrue(occurrences.occurrences().isEmpty());
        for (String revisionIdentity : revisionIdentities) {
            assertTrue(references.find(SOURCE, revisionIdentity).isEmpty());
        }
    }

    private static EvidenceUse use(
            EvaluationOccurrenceId occurrence,
            int position,
            String revisionIdentity,
            ModelFingerprint target,
            ControlledRevisionId targetRevision,
            Requirement requirement,
            Assertion<?> assertion,
            EvidenceUseRole role) {
        return new EvidenceUse(
                occurrence, position,
                new EvidenceReference(SOURCE, revisionIdentity, EvidenceProvenance.unknown("fixture")),
                target, targetRevision, null, requirement, assertion, role, FRAME, APPLICABLE);
    }

    private static EvaluationOccurrenceDraft draft(
            EvaluationOccurrenceId id,
            Requirement requirement,
            Assertion<?> assertion,
            ControlledRevisionId subjectRevision,
            List<EvidenceUse> reliedOnUses,
            List<EvidenceUse> excludedUses,
            ConformanceEvaluation evaluation) {
        return new EvaluationOccurrenceDraft(
                id, requirement, assertion, F1, subjectRevision, reliedOnUses, excludedUses, List.of(),
                FRAME, "fixture rules", evaluation, "revision binding", null);
    }

    private static ConformanceEvaluation evaluation(
            Requirement requirement,
            Assertion<?> assertion,
            ControlledRevisionId subjectRevision,
            ConformanceResult result) {
        return new ConformanceEvaluation(
                requirement.id(), requirement.version(), assertion.id(), assertion.version(),
                F1, subjectRevision, result, null);
    }

    private static Requirement requirement(RequirementScope scope) {
        return new Requirement(
                new RequirementId("requirement.revision-binding"), new RequirementVersion(1),
                "revision binding", "revision binding", ArcogineNativeRequirementSource.of("test"), scope);
    }

    private static Assertion<?> assertion(Requirement requirement) {
        return new Assertion<>(
                new AssertionId("assertion.revision-binding"), new AssertionVersion(1),
                requirement.id(), requirement.version(), "external basis",
                EvidenceRequirement.EXTERNAL_EVIDENCE_REQUIRED, null);
    }

    private static ControlledRevision revision(ModelFingerprint fingerprint, List<ControlledRevisionId> parents) {
        return new ControlledRevision(ControlledRevisionId.generate(), fingerprint, parents,
                new RevisionProvenance(Instant.EPOCH, new RevisionRecorder("test", "accepted model")));
    }

    private static ControlledRevisionAuthority authority(ControlledRevision... revisions) {
        Map<ControlledRevisionId, ControlledRevision> byId = new LinkedHashMap<>();
        for (ControlledRevision revision : revisions) {
            byId.put(revision.id(), revision);
        }
        return new ControlledRevisionAuthority() {
            @Override
            public ControlledRevision accept(ControlledRevision candidate, SemanticArtifact artifact) {
                throw new AssertionError("fixture is read-only");
            }

            @Override
            public Optional<ControlledRevision> findById(ControlledRevisionId id) {
                return Optional.ofNullable(byId.get(id));
            }

            @Override
            public HistoricalRevision resolve(ControlledRevisionId id) {
                ControlledRevision revision = findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("unknown revision: " + id));
                return new HistoricalRevision(revision, new SemanticArtifact(revision.modelFingerprint(), new byte[0]));
            }

            @Override
            public List<ControlledRevision> revisions() {
                return List.copyOf(byId.values());
            }
        };
    }
}
