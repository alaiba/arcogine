package com.arcogine.governance.evidence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
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
import com.arcogine.governance.conformance.ConformanceEvaluation;
import com.arcogine.governance.conformance.ConformanceResult;
import com.arcogine.governance.conformance.EvidenceConformanceEvaluator;
import com.arcogine.governance.conformance.EvidenceJudgment;
import com.arcogine.governance.conformance.EvaluationOccurrence;
import com.arcogine.governance.conformance.EvaluationOccurrenceDraft;
import com.arcogine.governance.conformance.EvaluationOccurrenceId;
import com.arcogine.governance.conformance.InMemoryEvaluationOccurrenceAuthority;
import com.arcogine.governance.requirement.ArcogineNativeRequirementSource;
import com.arcogine.governance.requirement.Requirement;
import com.arcogine.governance.requirement.RequirementId;
import com.arcogine.governance.requirement.RequirementScope;
import com.arcogine.governance.requirement.RequirementVersion;
import com.arcogine.types.ControlledRevisionId;
import com.arcogine.types.EngineSemanticsVersion;
import com.arcogine.types.ModelFingerprint;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class GovernanceEvidenceTest {

    private static final ModelFingerprint MODEL = fingerprint("a");
    private static final ModelFingerprint OTHER_MODEL = fingerprint("b");
    private static final RequirementId REQUIREMENT_ID = new RequirementId("requirement.evidence");
    private static final RequirementVersion REQUIREMENT_VERSION = new RequirementVersion(1);
    private static final AssertionId ASSERTION_ID = new AssertionId("assertion.evidence");
    private static final AssertionVersion ASSERTION_VERSION = new AssertionVersion(1);
    private static final EvidenceProvenance PROVENANCE = EvidenceProvenance.unknown("fixture");

    @Test
    void sameSourceRevisionIsEqualButDifferentProducersRemainDistinct() {
        EvidenceReference first = new EvidenceReference("source-a", "record-1", PROVENANCE);
        EvidenceReference redelivery = new EvidenceReference("source-a", "record-1", PROVENANCE);
        EvidenceReference independent = new EvidenceReference("source-b", "record-1", PROVENANCE);

        assertEquals(first, redelivery);
        assertNotEquals(first, independent);

        InMemoryEvidenceReferenceAuthority authority = new InMemoryEvidenceReferenceAuthority();
        assertSame(authority.record(first), authority.record(redelivery));
        assertEquals(1, authority.references().size());
        assertEquals(2, authority.record(independent).equals(first) ? 1 : 2);
    }

    @Test
    void referenceAuthorityRejectsRebindingAndCorrectionIsNewMaterial() {
        InMemoryEvidenceReferenceAuthority authority = new InMemoryEvidenceReferenceAuthority();
        EvidenceReference original = new EvidenceReference("source-a", "record-1", PROVENANCE);
        EvidenceReference rebound = new EvidenceReference(
                "source-a", "record-1", EvidenceProvenance.unknown("different intrinsic provenance"));
        EvidenceReference correction = EvidenceReference.relatedRevision(
                "source-a", "record-2", EvidenceProvenance.unknown("corrected"),
                EvidenceRelationKind.CORRECTION, original, "source correction");

        authority.record(original);
        assertThrows(IllegalArgumentException.class, () -> authority.record(rebound));
        assertNotEquals(original, correction);
        assertEquals(original, correction.relationOptional().orElseThrow().earlier());
    }

    @Test
    void laterCorrectionChangesOnlyTheLaterOccurrence() {
        Requirement requirement = requirement("correction");
        Assertion<?> assertion = assertion(requirement, "correction rule");
        EvidenceReference original = new EvidenceReference("source", "record-1", PROVENANCE);
        EvidenceReference correction = EvidenceReference.relatedRevision(
                "source", "record-2", EvidenceProvenance.unknown("corrected"),
                EvidenceRelationKind.CORRECTION, original, "late correction");
        EvaluationOccurrenceId firstId = EvaluationOccurrenceId.generate();
        EvaluationOccurrenceId secondId = EvaluationOccurrenceId.generate();
        EvidenceUse firstUse = use(firstId, 0, original, MODEL, requirement, assertion,
                EvidenceUseRole.RELIED_ON,
                new EvidenceApplicability(EvidenceApplicabilityStatus.APPLICABLE, "initial", "fixture"));
        EvidenceUse secondUse = use(secondId, 0, correction, MODEL, requirement, assertion,
                EvidenceUseRole.RELIED_ON,
                new EvidenceApplicability(EvidenceApplicabilityStatus.APPLICABLE, "corrected", "fixture"));
        InMemoryEvaluationOccurrenceAuthority authority = new InMemoryEvaluationOccurrenceAuthority();
        EvaluationOccurrence first = authority.accept(draft(requirement, assertion, firstId, List.of(firstUse), List.of()));
        EvaluationOccurrence second = authority.accept(draft(
                requirement, assertion, secondId, List.of(secondUse), List.of(), ConformanceResult.PASS, firstId));

        assertEquals(original, first.reliedOnUses().get(0).evidence());
        assertEquals(correction, second.reliedOnUses().get(0).evidence());
        assertNotEquals(first.reliedOnUses().get(0).evidence(), second.reliedOnUses().get(0).evidence());
        assertEquals(ConformanceResult.UNKNOWN, first.evaluation().result());
        assertEquals(ConformanceResult.PASS, second.evaluation().result());
        assertEquals(first.id(), second.relatedOccurrenceIdOptional().orElseThrow());
    }

    @Test
    void externalEvidenceOutcomeRequiresAnAdequateReliedOnUseAtAcceptance() {
        Requirement requirement = requirement("acceptance basis");
        Assertion<?> assertion = externalAssertion(requirement);
        EvaluationOccurrenceId occurrence = EvaluationOccurrenceId.generate();
        ConformanceEvaluation pass = new ConformanceEvaluation(
                requirement.id(), requirement.version(), assertion.id(), assertion.version(),
                MODEL, null, ConformanceResult.PASS, null);
        EvaluationOccurrenceDraft candidate = new EvaluationOccurrenceDraft(
                occurrence, requirement, assertion, MODEL, null, List.of(), List.of(), List.of(),
                TemporalFrame.atKnowledgeBoundary(Instant.EPOCH), "fixture rules", pass, "unsupported pass", null);

        assertThrows(IllegalArgumentException.class,
                () -> new InMemoryEvaluationOccurrenceAuthority().accept(candidate));
    }

    @Test
    void relatedOccurrenceMustAlreadyBeAcceptedAndCannotBeSelf() {
        Requirement requirement = requirement("lineage");
        Assertion<?> assertion = externalAssertion(requirement);
        InMemoryEvaluationOccurrenceAuthority authority = new InMemoryEvaluationOccurrenceAuthority();
        EvaluationOccurrenceId occurrence = EvaluationOccurrenceId.generate();

        assertThrows(IllegalArgumentException.class, () -> authority.accept(new EvaluationOccurrenceDraft(
                occurrence, requirement, assertion, MODEL, null, List.of(), List.of(), List.of(),
                TemporalFrame.atKnowledgeBoundary(Instant.EPOCH), "fixture rules",
                unknownEvaluation(requirement, assertion), "unknown", EvaluationOccurrenceId.generate())));
        assertThrows(IllegalArgumentException.class, () -> authority.accept(new EvaluationOccurrenceDraft(
                occurrence, requirement, assertion, MODEL, null, List.of(), List.of(), List.of(),
                TemporalFrame.atKnowledgeBoundary(Instant.EPOCH), "fixture rules",
                unknownEvaluation(requirement, assertion), "unknown", occurrence)));
    }

    @Test
    void evidenceReferencesShareAuthorityAndCannotBeReboundAcrossOccurrences() {
        Requirement requirement = requirement("reference authority");
        Assertion<?> assertion = externalAssertion(requirement);
        InMemoryEvidenceReferenceAuthority evidenceAuthority = new InMemoryEvidenceReferenceAuthority();
        InMemoryEvaluationOccurrenceAuthority occurrenceAuthority =
                new InMemoryEvaluationOccurrenceAuthority(evidenceAuthority);
        EvidenceReference firstReference = new EvidenceReference("source", "record", PROVENANCE);
        EvidenceReference rebound = new EvidenceReference(
                "source", "record", EvidenceProvenance.unknown("different provenance"));
        EvaluationOccurrenceId firstId = EvaluationOccurrenceId.generate();
        EvaluationOccurrenceId secondId = EvaluationOccurrenceId.generate();
        EvidenceUse firstUse = use(firstId, 0, firstReference, MODEL, requirement, assertion,
                EvidenceUseRole.RELIED_ON,
                new EvidenceApplicability(EvidenceApplicabilityStatus.APPLICABLE, "first", "fixture"));
        EvidenceUse secondUse = use(secondId, 0, rebound, MODEL, requirement, assertion,
                EvidenceUseRole.RELIED_ON,
                new EvidenceApplicability(EvidenceApplicabilityStatus.APPLICABLE, "second", "fixture"));

        occurrenceAuthority.accept(draft(requirement, assertion, firstId, List.of(firstUse), List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> occurrenceAuthority.accept(draft(requirement, assertion, secondId, List.of(secondUse), List.of())));
    }

    @Test
    void correctionRelationCannotRebindCanonicalEarlierProvenance() {
        Requirement requirement = requirement("correction provenance");
        Assertion<?> assertion = externalAssertion(requirement);
        InMemoryEvidenceReferenceAuthority evidenceAuthority = new InMemoryEvidenceReferenceAuthority();
        InMemoryEvaluationOccurrenceAuthority occurrenceAuthority =
                new InMemoryEvaluationOccurrenceAuthority(evidenceAuthority);
        EvidenceReference original = new EvidenceReference("source", "record-1", PROVENANCE);
        EvidenceReference conflictingEarlier = new EvidenceReference(
                "source", "record-1", EvidenceProvenance.unknown("conflicting earlier provenance"));
        EvidenceReference correction = EvidenceReference.relatedRevision(
                "source", "record-2", EvidenceProvenance.unknown("corrected"),
                EvidenceRelationKind.CORRECTION, conflictingEarlier, "late correction");
        EvaluationOccurrenceId firstId = EvaluationOccurrenceId.generate();
        EvaluationOccurrenceId secondId = EvaluationOccurrenceId.generate();
        EvidenceUse firstUse = use(firstId, 0, original, MODEL, requirement, assertion,
                EvidenceUseRole.RELIED_ON,
                new EvidenceApplicability(EvidenceApplicabilityStatus.APPLICABLE, "first", "fixture"));
        EvidenceUse correctionUse = use(secondId, 0, correction, MODEL, requirement, assertion,
                EvidenceUseRole.RELIED_ON,
                new EvidenceApplicability(EvidenceApplicabilityStatus.APPLICABLE, "corrected", "fixture"));

        occurrenceAuthority.accept(draft(requirement, assertion, firstId, List.of(firstUse), List.of()));
        assertThrows(IllegalArgumentException.class, () -> occurrenceAuthority.accept(
                draft(requirement, assertion, secondId, List.of(correctionUse), List.of())));
    }

    @Test
    void unacceptedStructuralProducerRevisionCannotEnterAcceptedOccurrenceHistory() {
        Requirement requirement = requirement("structural provenance");
        Assertion<?> assertion = externalAssertion(requirement);
        ControlledRevision candidateRevision = new ControlledRevision(
                ControlledRevisionId.generate(), MODEL, List.of(),
                new RevisionProvenance(Instant.EPOCH, new RevisionRecorder("fixture", "candidate")));
        EvidenceReference evidence = EvidenceReference.forControlledRevision(candidateRevision);
        EvaluationOccurrenceId occurrence = EvaluationOccurrenceId.generate();
        EvidenceUse use = use(occurrence, 0, evidence, MODEL, requirement, assertion,
                EvidenceUseRole.RELIED_ON,
                new EvidenceApplicability(EvidenceApplicabilityStatus.APPLICABLE, "structural", "fixture"));

        assertThrows(IllegalArgumentException.class, () -> new InMemoryEvaluationOccurrenceAuthority(
                new NoopRevisionAuthority()).accept(draft(requirement, assertion, occurrence, List.of(use), List.of())));
    }

    @Test
    void usesKeepApplicabilityIndependentAcrossRevisionsAndComparatorTargets() {
        Requirement requirement = requirement("original wording");
        Assertion<?> assertion = assertion(requirement, "original rule");
        EvidenceReference evidence = new EvidenceReference("source", "revision", PROVENANCE);
        EvaluationOccurrenceId occurrence = EvaluationOccurrenceId.generate();
        EvidenceUse first = use(occurrence, 0, evidence, MODEL, requirement, assertion,
                EvidenceUseRole.CONSIDERED_BUT_NOT_RELIED_ON,
                new EvidenceApplicability(EvidenceApplicabilityStatus.APPLICABLE, "usable", "fixture rule"));
        EvidenceUse rollback = use(occurrence, 1, evidence, MODEL, requirement, assertion,
                EvidenceUseRole.CONSIDERED_BUT_NOT_RELIED_ON,
                new EvidenceApplicability(EvidenceApplicabilityStatus.NOT_ESTABLISHED, "not verified here", "new target"));
        EvidenceUse comparator = use(occurrence, 2, evidence, OTHER_MODEL, requirement, assertion,
                EvidenceUseRole.COMPARATOR,
                new EvidenceApplicability(EvidenceApplicabilityStatus.APPLICABLE, "comparison only", "explicit comparator"));

        assertNotEquals(first.applicability(), rollback.applicability());
        assertEquals(OTHER_MODEL, comparator.targetModelFingerprint());
        assertTrue(comparator.role() == EvidenceUseRole.COMPARATOR);
    }

    @Test
    void evidenceUsePositionIsUniqueWithinAnOccurrence() {
        Requirement requirement = requirement("use identity");
        Assertion<?> assertion = externalAssertion(requirement);
        EvaluationOccurrenceId occurrence = EvaluationOccurrenceId.generate();
        EvidenceUse reliedOn = use(
                occurrence,
                0,
                new EvidenceReference("source", "record-1", PROVENANCE),
                MODEL,
                requirement,
                assertion,
                EvidenceUseRole.RELIED_ON,
                new EvidenceApplicability(EvidenceApplicabilityStatus.APPLICABLE, "relied on", "fixture"));
        EvidenceUse excluded = use(
                occurrence,
                0,
                new EvidenceReference("source", "record-2", PROVENANCE),
                MODEL,
                requirement,
                assertion,
                EvidenceUseRole.CONSIDERED_BUT_NOT_RELIED_ON,
                new EvidenceApplicability(EvidenceApplicabilityStatus.APPLICABLE, "excluded", "fixture"));

        assertThrows(
                IllegalArgumentException.class,
                () -> draft(requirement, assertion, occurrence, List.of(reliedOn), List.of(excluded)));
    }

    @Test
    void unusableEvidenceCannotPassButAnAdequateAlternativeCan() {
        Requirement requirement = requirement("external wording");
        Assertion<?> assertion = externalAssertion(requirement);
        EvaluationOccurrenceId occurrence = EvaluationOccurrenceId.generate();
        EvidenceUse unusable = use(occurrence, 0, new EvidenceReference("source", "stale", PROVENANCE), MODEL,
                requirement, assertion, EvidenceUseRole.CONSIDERED_BUT_NOT_RELIED_ON,
                new EvidenceApplicability(EvidenceApplicabilityStatus.INAPPLICABLE, "stale", "period policy"));
        ConformanceEvaluation unknown = EvidenceConformanceEvaluator.evaluate(
                requirement, assertion, Optional.empty(), MODEL, Optional.empty(), new NoopRevisionAuthority(),
                List.of(unusable), new EvidenceJudgment(ConformanceResult.PASS, "claimed pass"));
        assertEquals(ConformanceResult.UNKNOWN, unknown.result());

        EvidenceUse adequate = use(occurrence, 1, new EvidenceReference("source", "current", PROVENANCE), MODEL,
                requirement, assertion, EvidenceUseRole.RELIED_ON,
                new EvidenceApplicability(EvidenceApplicabilityStatus.APPLICABLE, "current", "period policy"));
        ConformanceEvaluation pass = EvidenceConformanceEvaluator.evaluate(
                requirement, assertion, Optional.empty(), MODEL, Optional.empty(), new NoopRevisionAuthority(),
                List.of(unusable, adequate), new EvidenceJudgment(ConformanceResult.PASS, "alternative basis"));
        assertEquals(ConformanceResult.PASS, pass.result());
    }

    @Test
    void missingEvidenceIsUnknownWhileAdequateEvidenceOfAbsenceMayFail() {
        Requirement requirement = requirement("absence wording");
        Assertion<?> assertion = externalAssertion(requirement);
        EvaluationOccurrenceId occurrence = EvaluationOccurrenceId.generate();
        ConformanceEvaluation missing = EvidenceConformanceEvaluator.evaluate(
                requirement, assertion, Optional.empty(), MODEL, Optional.empty(), new NoopRevisionAuthority(),
                List.of(), new EvidenceJudgment(ConformanceResult.PASS, "no material"));
        assertEquals(ConformanceResult.UNKNOWN, missing.result());
        EvidenceUse adequateAbsence = use(occurrence, 0, new EvidenceReference("inventory", "scan-1", PROVENANCE), MODEL,
                requirement, assertion, EvidenceUseRole.RELIED_ON,
                new EvidenceApplicability(EvidenceApplicabilityStatus.APPLICABLE, "complete scan", "absence rule"));
        ConformanceEvaluation fail = EvidenceConformanceEvaluator.evaluate(
                requirement, assertion, Optional.empty(), MODEL, Optional.empty(), new NoopRevisionAuthority(),
                List.of(adequateAbsence), new EvidenceJudgment(ConformanceResult.FAIL, "required record absent"));
        assertEquals(ConformanceResult.FAIL, fail.result());
        assertTrue(fail.findingOptional().isPresent());
    }

    @Test
    void externalObservationRetainsSourceSubjectUntilUseCorrespondence() {
        EvidenceProvenance observation = EvidenceProvenance.externalObservation(
                "external sensor", Optional.of("external-machine-7"),
                Optional.of(Instant.parse("2026-09-20T10:00:00Z")), Optional.empty(), Optional.empty());
        EvidenceReference evidence = new EvidenceReference("sensor-a", "event-7", observation);

        assertEquals("external-machine-7", evidence.provenance().describedSubject().orElseThrow());
        assertTrue(evidence.provenance().producerModelFingerprint().isEmpty());
    }

    @Test
    void analyticalFixtureDoesNotStampMissingEngineSemantics() {
        EvidenceProvenance result = EvidenceProvenance.analytical(
                MODEL, Optional.empty(), Optional.empty(), "result-1", Map.of("workload", "small"), Optional.empty());
        assertTrue(result.producerModelFingerprint().isPresent());
        assertTrue(result.producerOccurrence().isPresent());
        assertTrue(result.engineSemanticsVersion().isEmpty());
        EvidenceProvenance versioned = EvidenceProvenance.analytical(
                MODEL, Optional.empty(), Optional.of(EngineSemanticsVersion.CURRENT), "result-2", Map.of(), Optional.empty());
        assertEquals(EngineSemanticsVersion.CURRENT, versioned.engineSemanticsVersion().orElseThrow());
    }

    @Test
    void retiredProducerAttributionRemainsWhileMissingInterpretationIsAVisibleGap() {
        EvidenceProvenance analytical = EvidenceProvenance.analytical(
                MODEL, Optional.empty(), Optional.of(EngineSemanticsVersion.CURRENT),
                "retired-result", Map.of("input", "1"), Optional.of("analysis:v1"));
        EvidenceReference result = new EvidenceReference("retired-engine", "result-1", analytical);
        assertEquals("retired-engine", result.sourceIdentity());
        assertEquals("retired-result", result.provenance().producerOccurrence().orElseThrow());
        EvidenceApplicability unresolved = new EvidenceApplicability(
                EvidenceApplicabilityStatus.NOT_ESTABLISHED,
                "producer no longer executable and interpretation material is unavailable",
                "historical attribution only");
        assertFalse(unresolved.isAdequate());
    }

    @Test
    void contextualCopiesRemainOneSourceAndDifferentUses() {
        Requirement requirement = requirement("packaging");
        Assertion<?> assertion = assertion(requirement, "packaging rule");
        EvidenceReference source = new EvidenceReference("source", "record-1", PROVENANCE);
        EvaluationOccurrenceId firstId = EvaluationOccurrenceId.generate();
        EvaluationOccurrenceId secondId = EvaluationOccurrenceId.generate();
        EvidenceUse firstUse = use(firstId, 0, source, MODEL, requirement, assertion,
                EvidenceUseRole.RELIED_ON,
                new EvidenceApplicability(EvidenceApplicabilityStatus.APPLICABLE, "first", "fixture"));
        EvidenceUse secondUse = use(secondId, 0, source, MODEL, requirement, assertion,
                EvidenceUseRole.RELIED_ON,
                new EvidenceApplicability(EvidenceApplicabilityStatus.APPLICABLE, "second", "fixture"));
        assertEquals(firstUse.evidence(), secondUse.evidence());
        assertNotEquals(firstUse.occurrenceId(), secondUse.occurrenceId());
    }

    @Test
    void acceptedOccurrenceCapturesDefinitionsAndCannotBeRebound() {
        Requirement oldRequirement = requirement("old wording");
        Assertion<?> oldAssertion = assertion(oldRequirement, "old rule");
        ConformanceEvaluation evaluation = new ConformanceEvaluation(
                oldRequirement.id(), oldRequirement.version(), oldAssertion.id(), oldAssertion.version(),
                MODEL, null, ConformanceResult.UNKNOWN, null);
        EvaluationOccurrenceDraft draft = EvaluationOccurrenceDraft.create(
                oldRequirement, oldAssertion, MODEL, Optional.empty(), List.of(), List.of(),
                List.of("evidence unavailable"), TemporalFrame.atKnowledgeBoundary(Instant.EPOCH),
                "external evidence interpretation", evaluation, "unknown pending evidence");
        EvaluationOccurrence accepted = new InMemoryEvaluationOccurrenceAuthority().accept(draft);
        Requirement changedRequirement = requirement("new wording");
        Assertion<?> changedAssertion = assertion(changedRequirement, "new rule");

        assertEquals("old wording", accepted.requirement().title());
        assertEquals("old rule", accepted.assertion().description());
        assertNotEquals(oldRequirement.title(), changedRequirement.title());
        assertNotEquals(oldAssertion.description(), changedAssertion.description());
    }

    @Test
    void equalDeterministicResultsAreDistinctAcceptedOccurrences() {
        Requirement requirement = requirement("same");
        Assertion<?> assertion = assertion(requirement, "same");
        ConformanceEvaluation evaluation = new ConformanceEvaluation(
                requirement.id(), requirement.version(), assertion.id(), assertion.version(),
                MODEL, null, ConformanceResult.UNKNOWN, null);
        EvaluationOccurrenceDraft first = EvaluationOccurrenceDraft.create(
                requirement, assertion, MODEL, Optional.empty(), List.of(), List.of(), List.of("gap"),
                TemporalFrame.atKnowledgeBoundary(Instant.EPOCH), "rules", evaluation, "unknown");
        EvaluationOccurrenceDraft second = EvaluationOccurrenceDraft.create(
                requirement, assertion, MODEL, Optional.empty(), List.of(), List.of(), List.of("gap"),
                TemporalFrame.atKnowledgeBoundary(Instant.EPOCH), "rules", evaluation, "unknown");
        InMemoryEvaluationOccurrenceAuthority authority = new InMemoryEvaluationOccurrenceAuthority();
        EvaluationOccurrence firstAccepted = authority.accept(first);
        EvaluationOccurrence secondAccepted = authority.accept(second);

        assertNotEquals(firstAccepted.id(), secondAccepted.id());
        assertEquals(evaluation, firstAccepted.evaluation());
        assertEquals(evaluation, secondAccepted.evaluation());
        assertThrows(IllegalArgumentException.class, () -> authority.accept(first));
    }

    @Test
    void malformedDefinitionBindingIsRejectedAtUseBoundary() {
        Requirement requirement = requirement("one");
        Assertion<?> assertion = assertion(requirement, "one");
        Requirement otherRequirement = requirement("requirement.other", "other");
        Assertion<?> otherAssertion = assertion(otherRequirement, "other");
        EvaluationOccurrenceId occurrence = EvaluationOccurrenceId.generate();
        assertThrows(IllegalArgumentException.class, () -> new EvidenceUse(
                occurrence, 0, new EvidenceReference("source", "record", PROVENANCE), MODEL, null, null,
                requirement, otherAssertion, EvidenceUseRole.RELIED_ON, TemporalFrame.atKnowledgeBoundary(Instant.EPOCH),
                new EvidenceApplicability(EvidenceApplicabilityStatus.APPLICABLE, "ok", "rules")));
    }

    @Test
    void useTargetIsPointIdentityAndDoesNotExposeContinuationType() {
        assertTrue(List.of(EvidenceUse.class.getRecordComponents()).stream()
                .noneMatch(component -> component.getType().getSimpleName().contains("Continuation")));
    }

    private static EvidenceUse use(
            EvaluationOccurrenceId occurrence,
            int position,
            EvidenceReference evidence,
            ModelFingerprint target,
            Requirement requirement,
            Assertion<?> assertion,
            EvidenceUseRole role,
            EvidenceApplicability applicability) {
        return new EvidenceUse(
                occurrence, position, evidence, target, null, null, requirement, assertion, role,
                TemporalFrame.atKnowledgeBoundary(Instant.EPOCH), applicability);
    }

    private static Requirement requirement(String wording) {
        return requirement(REQUIREMENT_ID.value(), wording);
    }

    private static Requirement requirement(String id, String wording) {
        return new Requirement(
                new RequirementId(id), REQUIREMENT_VERSION, wording, wording,
                ArcogineNativeRequirementSource.of("test"), RequirementScope.empty());
    }

    private static Assertion<?> assertion(Requirement requirement, String wording) {
        return new Assertion<>(
                ASSERTION_ID, ASSERTION_VERSION, requirement.id(), requirement.version(), wording,
                EvidenceRequirement.EXTERNAL_EVIDENCE_REQUIRED, null);
    }

    private static Assertion<?> externalAssertion(Requirement requirement) {
        return assertion(requirement, "external basis");
    }

    private static EvaluationOccurrenceDraft draft(
            Requirement requirement,
            Assertion<?> assertion,
            EvaluationOccurrenceId occurrenceId,
            List<EvidenceUse> reliedOnUses,
            List<EvidenceUse> excludedUses) {
        return draft(requirement, assertion, occurrenceId, reliedOnUses, excludedUses,
                ConformanceResult.UNKNOWN, null);
    }

    private static EvaluationOccurrenceDraft draft(
            Requirement requirement,
            Assertion<?> assertion,
            EvaluationOccurrenceId occurrenceId,
            List<EvidenceUse> reliedOnUses,
            List<EvidenceUse> excludedUses,
            ConformanceResult result,
            EvaluationOccurrenceId relatedOccurrenceId) {
        ConformanceEvaluation evaluation = new ConformanceEvaluation(
                requirement.id(), requirement.version(), assertion.id(), assertion.version(),
                MODEL, null, result, null);
        return new EvaluationOccurrenceDraft(
                occurrenceId, requirement, assertion, MODEL, null, reliedOnUses, excludedUses,
                List.of(), TemporalFrame.atKnowledgeBoundary(Instant.EPOCH), "fixture rules",
                evaluation, result.name().toLowerCase(), relatedOccurrenceId);
    }

    private static ConformanceEvaluation unknownEvaluation(Requirement requirement, Assertion<?> assertion) {
        return new ConformanceEvaluation(
                requirement.id(), requirement.version(), assertion.id(), assertion.version(),
                MODEL, null, ConformanceResult.UNKNOWN, null);
    }

    private static ModelFingerprint fingerprint(String suffix) {
        return new ModelFingerprint("test", "v1", "sha256", suffix.repeat(64));
    }

    private static final class NoopRevisionAuthority implements ControlledRevisionAuthority {
        @Override
        public ControlledRevision accept(ControlledRevision candidate, SemanticArtifact artifact) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<ControlledRevision> findById(ControlledRevisionId id) {
            return Optional.empty();
        }

        @Override
        public HistoricalRevision resolve(ControlledRevisionId id) {
            throw new IllegalArgumentException("no revisions in this fixture");
        }

        @Override
        public List<ControlledRevision> revisions() {
            return List.of();
        }
    }
}
