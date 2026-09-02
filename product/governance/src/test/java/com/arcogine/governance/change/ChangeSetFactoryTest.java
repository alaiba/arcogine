package com.arcogine.governance.change;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
import com.arcogine.types.ControlledRevisionId;
import com.arcogine.types.MachineId;
import com.arcogine.types.ModelFingerprint;
import com.arcogine.types.ProductId;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * End-to-end G2 tests: {@link ChangeSetFactory} against the real G1.3 authoritative persistence
 * boundary ({@link FileControlledRevisionAuthority}) and the real factory-domain D5 comparator
 * ({@link FactoryModelSemanticComparator}) -- not test-only object injection.
 */
class ChangeSetFactoryTest {

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
    void comparesTwoAuthoritativeRevisionsThroughHistoricalResolution() {
        FileControlledRevisionAuthority authority = authority();
        ControlledRevision base = accept(authority, model(List.of(1)), List.of());
        ControlledRevision candidate = accept(authority, model(List.of(1, 2)), List.of(base.id()));

        ChangeSet changeSet =
                ChangeSetFactory.fromAuthoritativeRevisions(
                        authority,
                        base.id(),
                        candidate.id(),
                        COMPARATOR,
                        ChangeProvenance.of("engineer", "add second machine"));

        assertEquals(base.id(), changeSet.baseRevisionId());
        assertEquals(candidate.id(), changeSet.resultingRevisionId());
        assertEquals(1, changeSet.semanticChanges().size());
        assertEquals(SemanticChangeKind.ENTITY_ADDED, changeSet.semanticChanges().get(0).kind());
        assertEquals(1, changeSet.impactScope().affectedEntities().size());
    }

    @Test
    void equalSemanticFingerprintAcrossDistinctRevisionsYieldsNoSemanticChangesButDistinctIdentity() {
        FileControlledRevisionAuthority authority = authority();
        FactoryModelVersion f1 = model(List.of(1));
        FactoryModelVersion f2 = model(List.of(1, 2));
        ControlledRevision revisionA = accept(authority, f1, List.of());
        ControlledRevision revisionB = accept(authority, f2, List.of(revisionA.id()));
        // Rollback: revision C reuses f1's fingerprint but is a distinct historical occurrence.
        ControlledRevision revisionC = accept(authority, f1, List.of(revisionB.id()));

        ChangeSet rollbackChangeSet =
                ChangeSetFactory.fromAuthoritativeRevisions(
                        authority,
                        revisionA.id(),
                        revisionC.id(),
                        COMPARATOR,
                        ChangeProvenance.of("engineer", "rollback"));

        assertTrue(rollbackChangeSet.isSemanticNoOp());
        assertNotEquals(rollbackChangeSet.baseRevisionId(), rollbackChangeSet.resultingRevisionId());
        assertEquals(revisionA.modelFingerprint(), revisionC.modelFingerprint());
        assertNotEquals(revisionA.id(), revisionC.id());
    }

    @Test
    void candidateSnapshotIsComparedWithoutBecomingAControlledRevision() {
        FileControlledRevisionAuthority authority = authority();
        ControlledRevision base = accept(authority, model(List.of(1)), List.of());
        FactoryModelVersion candidateVersion = model(List.of(1, 2));
        SemanticArtifact candidateArtifact =
                new SemanticArtifact(
                        candidateVersion.fingerprint(), FactoryModelArtifactV1.encode(candidateVersion));

        ChangeSet changeSet =
                ChangeSetFactory.fromCandidateSnapshot(
                        authority,
                        base.id(),
                        candidateArtifact,
                        FACTORY_VERIFIER,
                        COMPARATOR,
                        ChangeProvenance.of("engineer", "proposed change under review"));

        assertTrue(changeSet.resultingRevisionIdOptional().isEmpty());
        assertEquals(candidateVersion.fingerprint(), changeSet.candidateFingerprint());
        assertEquals(1, changeSet.semanticChanges().size());
        // The candidate never touched the authority: no new revision was accepted.
        assertEquals(1, authority.revisions().size());
    }

    @Test
    void candidateSnapshotWithFingerprintNotMatchingItsBytesIsRejected() {
        // REV-002 regression: a caller must not be able to claim a fingerprint for canonical bytes
        // that don't actually produce it -- mirroring FileControlledRevisionAuthority's own
        // fingerprint-to-bytes verification precedent for authoritative artifacts.
        FileControlledRevisionAuthority authority = authority();
        ControlledRevision base = accept(authority, model(List.of(1)), List.of());
        FactoryModelVersion realCandidate = model(List.of(1, 2));
        FactoryModelVersion differentCandidate = model(List.of(1, 2, 3));
        // Declares realCandidate's fingerprint but carries differentCandidate's canonical bytes.
        SemanticArtifact mismatchedArtifact =
                new SemanticArtifact(
                        realCandidate.fingerprint(), FactoryModelArtifactV1.encode(differentCandidate));

        IllegalArgumentException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                ChangeSetFactory.fromCandidateSnapshot(
                                        authority,
                                        base.id(),
                                        mismatchedArtifact,
                                        FACTORY_VERIFIER,
                                        COMPARATOR,
                                        ChangeProvenance.of("engineer", "malicious or corrupt candidate")));

        assertTrue(exception.getMessage().contains("fingerprint"));
        // The rejected candidate never touched the authority.
        assertEquals(1, authority.revisions().size());
    }

    @Test
    void candidateSnapshotWithUnsupportedFingerprintPolicyIsRejected() {
        FileControlledRevisionAuthority authority = authority();
        ControlledRevision base = accept(authority, model(List.of(1)), List.of());
        FactoryModelVersion candidateVersion = model(List.of(1, 2));
        SemanticArtifact candidateArtifact =
                new SemanticArtifact(
                        candidateVersion.fingerprint(), FactoryModelArtifactV1.encode(candidateVersion));
        SemanticArtifactVerifier unsupportingVerifier = new SemanticArtifactVerifier() {
            @Override
            public boolean supports(ModelFingerprint fingerprint) {
                return false;
            }

            @Override
            public ModelFingerprint fingerprint(byte[] canonicalBytes) {
                throw new AssertionError("must not be reached when the policy is unsupported");
            }
        };

        IllegalArgumentException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                ChangeSetFactory.fromCandidateSnapshot(
                                        authority,
                                        base.id(),
                                        candidateArtifact,
                                        unsupportingVerifier,
                                        COMPARATOR,
                                        ChangeProvenance.of("engineer", "unsupported policy")));

        assertTrue(exception.getMessage().contains("does not support"));
    }

    @Test
    void candidateSnapshotWhoseFingerprintCannotBeComputedIsRejected() {
        FileControlledRevisionAuthority authority = authority();
        ControlledRevision base = accept(authority, model(List.of(1)), List.of());
        FactoryModelVersion candidateVersion = model(List.of(1, 2));
        SemanticArtifact candidateArtifact =
                new SemanticArtifact(
                        candidateVersion.fingerprint(), FactoryModelArtifactV1.encode(candidateVersion));
        SemanticArtifactVerifier failingVerifier = new SemanticArtifactVerifier() {
            @Override
            public boolean supports(ModelFingerprint fingerprint) {
                return true;
            }

            @Override
            public ModelFingerprint fingerprint(byte[] canonicalBytes) {
                throw new IllegalStateException("cannot decode candidate bytes");
            }
        };

        IllegalArgumentException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                ChangeSetFactory.fromCandidateSnapshot(
                                        authority,
                                        base.id(),
                                        candidateArtifact,
                                        failingVerifier,
                                        COMPARATOR,
                                        ChangeProvenance.of("engineer", "corrupt candidate bytes")));

        assertTrue(exception.getMessage().contains("cannot be verified"));
        assertEquals(IllegalStateException.class, exception.getCause().getClass());
    }

    @Test
    void externalChangeRequestReferenceSurvivesEndToEnd() {
        FileControlledRevisionAuthority authority = authority();
        ControlledRevision base = accept(authority, model(List.of(1)), List.of());
        ControlledRevision candidate = accept(authority, model(List.of(1, 2)), List.of(base.id()));
        ExternalChangeReference jiraRef = new ExternalChangeReference("jira", "ARC-42");

        ChangeSet changeSet =
                ChangeSetFactory.fromAuthoritativeRevisions(
                        authority,
                        base.id(),
                        candidate.id(),
                        COMPARATOR,
                        ChangeProvenance.of("engineer", "requested capacity increase", jiraRef));

        assertEquals(jiraRef, changeSet.provenance().externalReferenceOptional().orElseThrow());
    }

    @Test
    void impactScopeIsUsableForFutureRequirementScopeMatching() {
        FileControlledRevisionAuthority authority = authority();
        ControlledRevision base = accept(authority, model(List.of(1)), List.of());
        ControlledRevision candidate = accept(authority, model(List.of(1, 2)), List.of(base.id()));

        ChangeSet changeSet =
                ChangeSetFactory.fromAuthoritativeRevisions(
                        authority,
                        base.id(),
                        candidate.id(),
                        COMPARATOR,
                        ChangeProvenance.of("engineer", "add machine"));

        // A hypothetical future requirement's registered scope, matched against ImpactScope
        // without any ChangeSet redesign -- proving the seam without fabricating G3 infrastructure.
        Set<ChangedEntityRef> requirementScope = Set.of(new ChangedEntityRef("factory.resource", "2", ""));
        assertTrue(changeSet.impactScope().intersects(requirementScope));
        Set<ChangedEntityRef> unrelatedScope = Set.of(new ChangedEntityRef("factory.resource", "99", ""));
        assertTrue(!changeSet.impactScope().intersects(unrelatedScope));
    }

    private FileControlledRevisionAuthority authority() {
        return new FileControlledRevisionAuthority(tempDirectory, FACTORY_VERIFIER);
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
