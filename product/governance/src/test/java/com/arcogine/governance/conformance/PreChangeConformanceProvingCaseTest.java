package com.arcogine.governance.conformance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.change.FactoryModelSemanticComparator;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelArtifactV1;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.model.ResourceDefinition;
import com.arcogine.governance.ControlledRevision;
import com.arcogine.governance.FileControlledRevisionAuthority;
import com.arcogine.governance.RevisionProvenance;
import com.arcogine.governance.RevisionRecorder;
import com.arcogine.governance.SemanticArtifact;
import com.arcogine.governance.SemanticArtifactVerifier;
import com.arcogine.governance.assertion.Assertion;
import com.arcogine.governance.assertion.AssertionId;
import com.arcogine.governance.assertion.AssertionVersion;
import com.arcogine.governance.assertion.EvidenceRequirement;
import com.arcogine.governance.assertion.StructuralAssertionOutcome;
import com.arcogine.governance.catalogue.RequirementCatalogue;
import com.arcogine.governance.change.ChangeProvenance;
import com.arcogine.governance.change.ChangeSet;
import com.arcogine.governance.change.ChangeSetFactory;
import com.arcogine.governance.change.ChangedEntityRef;
import com.arcogine.governance.requirement.ArcogineNativeRequirementSource;
import com.arcogine.governance.requirement.Requirement;
import com.arcogine.governance.requirement.RequirementId;
import com.arcogine.governance.requirement.RequirementScope;
import com.arcogine.governance.requirement.RequirementVersion;
import com.arcogine.types.ControlledRevisionId;
import com.arcogine.types.MachineId;
import com.arcogine.types.ModelFingerprint;
import com.arcogine.types.ProductId;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * G4 pre-change conformance proving case (architecture §11): a proposed candidate is compared
 * against its authoritative base with the real G1.3/G2 boundary, the affected requirements are
 * selected from a real G3 {@link RequirementCatalogue} via the resulting {@link
 * com.arcogine.governance.change.ImpactScope}, and each is evaluated with the real G4 {@link
 * ConformanceEvaluator} -- entirely before the candidate is authorized or deployed.
 */
class PreChangeConformanceProvingCaseTest {

    private static final RevisionRecorder RECORDER = new RevisionRecorder("test", "operator");
    private static final SemanticArtifactVerifier FACTORY_VERIFIER = new SemanticArtifactVerifier() {
        @Override
        public boolean supports(ModelFingerprint fingerprint) {
            return FactoryModelArtifactV1.supports(fingerprint);
        }

        @Override
        public ModelFingerprint fingerprint(byte[] canonicalBytes) {
            return FactoryModelArtifactV1.fingerprint(canonicalBytes);
        }
    };
    private static final FactoryModelSemanticComparator COMPARATOR = new FactoryModelSemanticComparator();

    @TempDir
    Path tempDirectory;

    @Test
    void preChangeCandidateIsEvaluatedAgainstRequirementsAffectedByItsRealImpactScope() {
        FileControlledRevisionAuthority authority =
                new FileControlledRevisionAuthority(tempDirectory, FACTORY_VERIFIER);
        FactoryModelVersion baseModel = model(List.of(1));
        FactoryModelVersion candidateModel = model(List.of(1, 2));
        ControlledRevision base = accept(authority, baseModel, List.of());
        ControlledRevision candidate = accept(authority, candidateModel, List.of(base.id()));

        ChangeSet changeSet =
                ChangeSetFactory.fromAuthoritativeRevisions(
                        authority,
                        base.id(),
                        candidate.id(),
                        COMPARATOR,
                        ChangeProvenance.of("engineer", "add second machine before deployment"));

        RequirementId requirementId = new RequirementId("arc.test.new-machines-must-be-reviewed");
        RequirementVersion requirementVersion = new RequirementVersion(1);
        ChangedEntityRef newMachine = new ChangedEntityRef("factory.resource", "2", "");
        Requirement affectedRequirement =
                new Requirement(
                        requirementId,
                        requirementVersion,
                        "New machines must be reviewed before deployment",
                        "Any newly added factory resource requires explicit pre-change review.",
                        ArcogineNativeRequirementSource.of("G4 pre-change proving case"),
                        RequirementScope.of(newMachine));
        Requirement unaffectedRequirement =
                new Requirement(
                        new RequirementId("arc.test.unrelated-requirement"),
                        requirementVersion,
                        "Unrelated requirement",
                        "Applies to an entity this change never touches.",
                        ArcogineNativeRequirementSource.of("G4 pre-change proving case"),
                        RequirementScope.of(new ChangedEntityRef("factory.resource", "99", "")));
        RequirementCatalogue catalogue = RequirementCatalogue.of(affectedRequirement, unaffectedRequirement);

        List<Requirement> affected = catalogue.potentiallyAffectedBy(changeSet.impactScope());
        assertEquals(List.of(affectedRequirement), affected);

        // The assertion subject is the actual candidate semantic model state, not the ChangeSet:
        // ChangeSet is used above only to drive requirement selection through its ImpactScope. The
        // binding function is FactoryModelVersion::fingerprint, which recomputes the fingerprint
        // from the model's own canonical content (FactoryModelFingerprintV1.fingerprint(model)) on
        // every call -- a non-trivial, content-derived derivation, not a constant or a pass-through
        // accessor unrelated to the subject's own content. changeSet.candidateFingerprint() is used
        // only as the expected value being verified against, which is legitimate: it is the
        // ChangeSet's own recorded candidate fingerprint, compared against the real subject's
        // independently derived fingerprint, not used as the binding function itself.
        Assertion<FactoryModelVersion> newMachinesMustBeReviewed =
                new Assertion<>(
                        new AssertionId("arc.test.new-machines-must-be-reviewed.rule"),
                        new AssertionVersion(1),
                        requirementId,
                        requirementVersion,
                        "candidate model must declare the reviewed machine",
                        EvidenceRequirement.MODEL_STATE_SUFFICIENT,
                        candidateModelState ->
                                candidateModelState.model().resources().stream()
                                                .anyMatch(resource -> resource.id().equals(new MachineId(2)))
                                        ? StructuralAssertionOutcome.satisfied(
                                                "candidate model declares machine 2")
                                        : StructuralAssertionOutcome.violated(
                                                "candidate model does not declare machine 2"));

        ConformanceEvaluation evaluation =
                ConformanceEvaluator.evaluate(
                        affectedRequirement,
                        newMachinesMustBeReviewed,
                        Optional.of(changeSet.impactScope()),
                        Optional.of(candidateModel),
                        changeSet.candidateFingerprint(),
                        FactoryModelVersion::fingerprint,
                        changeSet.resultingRevisionIdOptional(),
                        authority);

        assertEquals(ConformanceResult.PASS, evaluation.result());
        assertTrue(evaluation.findingOptional().isEmpty());
        ControlledRevisionId resolvedRevision = evaluation.controlledRevisionIdOptional().orElseThrow();
        assertEquals(candidate.id(), resolvedRevision);
    }

    private ControlledRevision accept(
            FileControlledRevisionAuthority authority,
            FactoryModelVersion version,
            List<ControlledRevisionId> parents) {
        ControlledRevision candidate =
                new ControlledRevision(
                        ControlledRevisionId.generate(),
                        version.fingerprint(),
                        parents,
                        new RevisionProvenance(Instant.now(), RECORDER));
        return authority.accept(
                candidate,
                new SemanticArtifact(version.fingerprint(), FactoryModelArtifactV1.encode(version)));
    }

    private static FactoryModelVersion model(List<Integer> resourceIds) {
        List<ResourceDefinition> resources =
                resourceIds.stream()
                        .map(id -> new ResourceDefinition(new MachineId(id), "Machine " + id, 1, 10.0, 1))
                        .toList();
        OperationStepDefinition step =
                new OperationStepDefinition(1, "Step", Set.of(resources.get(0).id()), 1);
        OperationDefinition operation = new OperationDefinition(100, "Routing", List.of(step));
        ProductDefinition product = new ProductDefinition(new ProductId(10), "Widget", operation.id());
        return FactoryModelPublisher.publish(
                new FactoryModel(resources, List.of(operation), List.of(product)));
    }
}
