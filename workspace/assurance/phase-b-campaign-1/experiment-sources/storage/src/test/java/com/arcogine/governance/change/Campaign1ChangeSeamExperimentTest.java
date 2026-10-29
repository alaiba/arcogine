package com.arcogine.governance.change;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.change.FactoryModelSemanticComparator;
import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelArtifact;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.governance.ControlledRevision;
import com.arcogine.governance.ControlledRevisionAuthority;
import com.arcogine.governance.HistoricalRevision;
import com.arcogine.governance.RevisionProvenance;
import com.arcogine.governance.RevisionRecorder;
import com.arcogine.governance.SemanticArtifact;
import com.arcogine.storage.BuiltInStorage;
import com.arcogine.types.ControlledRevisionId;
import com.arcogine.types.MachineId;
import com.arcogine.types.ModelFingerprint;
import com.arcogine.types.ProductId;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Transient Campaign 1 slice 3 experiment (not a merge candidate): the G-CHANGE seam over real
 * published Factory artifacts, real built-in Storage and the real Factory comparator. Factory
 * fingerprints, controlled-revision identities and verification authority are kept distinct.
 */
class Campaign1ChangeSeamExperimentTest {

    private static final RevisionRecorder RECORDER = new RevisionRecorder("campaign1", "experiment");
    private static final FactoryModelSemanticComparator COMPARATOR = new FactoryModelSemanticComparator();

    @TempDir
    Path temp;

    static FactoryModelVersion model(List<Long> eligibleOrder, String stepName, int concurrency) {
        LinkedHashSet<MachineId> eligible = new LinkedHashSet<>();
        eligibleOrder.forEach(id -> eligible.add(new MachineId(id)));
        return FactoryModelPublisher.publish(new FactoryModel(
                List.of(new ConfiguredResource(new MachineId(Long.MIN_VALUE), "a", concurrency, null, 0),
                        new ConfiguredResource(new MachineId(-1), "b", 1, -0.0, 0),
                        new ConfiguredResource(new MachineId(Long.MAX_VALUE), "c", 1, Double.NaN, 0)),
                List.of(new OperationDefinition(7, "op", List.of(new OperationStepDefinition(1, stepName, eligible, 3)))),
                List.of(new ProductDefinition(new ProductId(9), "p", 7))));
    }

    static SemanticArtifact artifact(FactoryModelVersion version) {
        return new SemanticArtifact(version.fingerprint(), FactoryModelArtifact.encode(version));
    }

    static ControlledRevision accept(ControlledRevisionAuthority authority, FactoryModelVersion version, List<ControlledRevisionId> parents) {
        return authority.accept(new ControlledRevision(ControlledRevisionId.generate(), version.fingerprint(), parents,
                new RevisionProvenance(Instant.parse("2026-10-29T00:00:00Z"), RECORDER)), artifact(version));
    }

    ControlledRevisionAuthority open() {
        return BuiltInStorage.open(temp.resolve("store"), FactoryModelArtifact.verifier()).controlledRevisions();
    }

    @Test
    void equivalentEligiblePermutationsAreOneFingerprintButDistinctRevisionsAndASemanticNoOp() {
        ControlledRevisionAuthority authority = open();
        FactoryModelVersion ascending = model(List.of(Long.MIN_VALUE, -1L, Long.MAX_VALUE), "s", 1);
        FactoryModelVersion permuted = model(List.of(Long.MAX_VALUE, Long.MIN_VALUE, -1L), "s", 1);
        assertEquals(ascending.fingerprint(), permuted.fingerprint());
        ControlledRevision first = accept(authority, ascending, List.of());
        ControlledRevision second = accept(authority, permuted, List.of(first.id()));
        assertNotEquals(first.id(), second.id());
        assertEquals(first.modelFingerprint(), second.modelFingerprint());

        ChangeSet change = ChangeSetFactory.fromAuthoritativeRevisions(authority, first.id(), second.id(), COMPARATOR,
                ChangeProvenance.of("campaign1", "permutation"));
        assertTrue(change.isSemanticNoOp());
        assertEquals(first.id(), change.baseRevisionId());
        assertEquals(second.id(), change.resultingRevisionIdOptional().orElseThrow());
        assertEquals(change.baseFingerprint(), change.candidateFingerprint());
        HistoricalRevision resolved = authority.resolve(second.id());
        assertArrayEquals(FactoryModelArtifact.encode(ascending), resolved.artifact().canonicalBytes());
    }

    @Test
    void historicalAndSnapshotPathsReportTheSameChangesAsDirectComparison() {
        ControlledRevisionAuthority authority = open();
        FactoryModelVersion base = model(List.of(-1L), "s", 1);
        FactoryModelVersion candidate = model(List.of(-1L, Long.MAX_VALUE), "s2", 2);
        ControlledRevision baseRevision = accept(authority, base, List.of());
        ControlledRevision candidateRevision = accept(authority, candidate, List.of(baseRevision.id()));
        List<SemanticChange> direct = COMPARATOR.compare(artifact(base), artifact(candidate));
        ChangeSet historical = ChangeSetFactory.fromAuthoritativeRevisions(authority, baseRevision.id(),
                candidateRevision.id(), COMPARATOR, ChangeProvenance.of("campaign1", "historical"));
        ChangeSet snapshot = ChangeSetFactory.fromCandidateSnapshot(authority, baseRevision.id(), artifact(candidate),
                FactoryModelArtifact.verifier(), COMPARATOR, ChangeProvenance.of("campaign1", "snapshot"));
        assertEquals(new ChangeSet(baseRevision.id(), base.fingerprint(), candidate.fingerprint(), null, direct, null,
                ChangeProvenance.of("x", "y")).semanticChanges(), historical.semanticChanges());
        assertEquals(historical.semanticChanges(), snapshot.semanticChanges());
        assertEquals(historical.impactScope(), snapshot.impactScope());
        assertTrue(snapshot.resultingRevisionIdOptional().isEmpty());
        assertEquals(2, authority.revisions().size());
        // Resource MIN (concurrency) and operation 7 (eligible set and step name) are the changes.
        assertEquals(List.of("factory.operation#7", "factory.resource#" + Long.MIN_VALUE),
                historical.semanticChanges().stream().map(c -> c.entity().entityType() + "#" + c.entity().entityId()).toList());
    }

    @Test
    void reopenedStorageYieldsTheSameHistoricalChangeSet() {
        FactoryModelVersion base = model(List.of(-1L), "s", 1);
        FactoryModelVersion candidate = model(List.of(Long.MIN_VALUE), "s", 1);
        ControlledRevisionId baseId;
        ControlledRevisionId candidateId;
        List<SemanticChange> before;
        {
            ControlledRevisionAuthority authority = open();
            baseId = accept(authority, base, List.of()).id();
            candidateId = accept(authority, candidate, List.of(baseId)).id();
            before = ChangeSetFactory.fromAuthoritativeRevisions(authority, baseId, candidateId, COMPARATOR,
                    ChangeProvenance.of("campaign1", "before reopen")).semanticChanges();
        }
        ControlledRevisionAuthority reopened = open();
        assertEquals(before, ChangeSetFactory.fromAuthoritativeRevisions(reopened, baseId, candidateId, COMPARATOR,
                ChangeProvenance.of("campaign1", "after reopen")).semanticChanges());
        assertEquals(1, before.size());
    }

    @Test
    void snapshotsThatAreNotVerifiedCurrentArtifactsAreRefusedBeforeComparison() {
        ControlledRevisionAuthority authority = open();
        FactoryModelVersion base = model(List.of(-1L), "s", 1);
        ControlledRevision baseRevision = accept(authority, base, List.of());
        byte[] canonical = FactoryModelArtifact.encode(base);

        byte[] trailing = java.util.Arrays.copyOf(canonical, canonical.length + 1);
        byte[] truncated = java.util.Arrays.copyOf(canonical, canonical.length - 1);
        ModelFingerprint declared = base.fingerprint();
        ModelFingerprint foreign = new ModelFingerprint("other-model", "sha256", declared.digest());
        ModelFingerprint wrongDigest = new ModelFingerprint("factory-model", "sha256", "0".repeat(64));
        List<SemanticArtifact> refused = List.of(
                new SemanticArtifact(declared, trailing),
                new SemanticArtifact(declared, truncated),
                new SemanticArtifact(foreign, canonical),
                new SemanticArtifact(wrongDigest, canonical));
        for (SemanticArtifact candidate : refused) {
            assertThrows(IllegalArgumentException.class, () -> ChangeSetFactory.fromCandidateSnapshot(authority,
                    baseRevision.id(), candidate, FactoryModelArtifact.verifier(), COMPARATOR,
                    ChangeProvenance.of("campaign1", "refused")));
        }
        assertEquals(1, authority.revisions().size());
        // Storage refuses the same artifacts on accept, without recording a revision.
        for (SemanticArtifact candidate : refused) {
            assertThrows(RuntimeException.class, () -> authority.accept(new ControlledRevision(ControlledRevisionId.generate(),
                    candidate.fingerprint(), List.of(baseRevision.id()), new RevisionProvenance(Instant.EPOCH, RECORDER)), candidate));
        }
        assertEquals(1, authority.revisions().size());
    }

    @Test
    void factoryFingerprintsAreNotRevisionIdentitiesAndUnknownRevisionsAreRefused() {
        ControlledRevisionAuthority authority = open();
        FactoryModelVersion base = model(List.of(-1L), "s", 1);
        ControlledRevision revision = accept(authority, base, List.of());
        ControlledRevisionId unknown = ControlledRevisionId.generate();
        assertTrue(authority.findById(unknown).isEmpty());
        assertThrows(RuntimeException.class, () -> ChangeSetFactory.fromAuthoritativeRevisions(authority, revision.id(),
                unknown, COMPARATOR, ChangeProvenance.of("campaign1", "unknown")));
        assertNotEquals(revision.id().toString(), revision.modelFingerprint().digest());
        Set<String> kinds = Set.of(revision.id().value().toString(), revision.modelFingerprint().toString());
        assertEquals(2, kinds.size());
    }
}
