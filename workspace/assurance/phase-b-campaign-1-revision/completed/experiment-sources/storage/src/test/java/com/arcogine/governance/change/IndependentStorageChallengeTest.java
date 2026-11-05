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
import com.arcogine.governance.ControlledRevision;
import com.arcogine.governance.ControlledRevisionAuthority;
import com.arcogine.governance.GovernanceHistoryException;
import com.arcogine.governance.RevisionProvenance;
import com.arcogine.governance.RevisionRecorder;
import com.arcogine.governance.SemanticArtifact;
import com.arcogine.storage.BuiltInStorage;
import com.arcogine.types.ControlledRevisionId;
import com.arcogine.types.MachineId;
import com.arcogine.types.ModelFingerprint;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class IndependentStorageChallengeTest {
    @TempDir Path temp;
    private static final ControlledRevisionId BASE = ControlledRevisionId.parse("00000000-0000-4000-8000-000000000001");
    private static final ControlledRevisionId CANDIDATE = ControlledRevisionId.parse("00000000-0000-4000-8000-000000000002");

    private static ControlledRevision revision(ControlledRevisionId id, ModelFingerprint fingerprint) {
        return new ControlledRevision(id, fingerprint, List.of(), new RevisionProvenance(Instant.EPOCH,
                new RevisionRecorder("independent-challenge", "fixture")));
    }

    @Test
    void refusalClassesCodesAndHistoricalPostStateAreExact() {
        FactoryModelVersion base = FactoryModelPublisher.publish(new FactoryModel(List.of(), List.of(), List.of()));
        byte[] bytes = FactoryModelArtifact.encode(base);
        ControlledRevisionAuthority authority = BuiltInStorage.open(temp.resolve("store"), FactoryModelArtifact.verifier()).controlledRevisions();
        authority.accept(revision(BASE, base.fingerprint()), new SemanticArtifact(base.fingerprint(), bytes));
        List<ControlledRevision> before = authority.revisions();
        List<SemanticArtifact> invalid = List.of(
                new SemanticArtifact(base.fingerprint(), java.util.Arrays.copyOf(bytes, bytes.length - 1)),
                new SemanticArtifact(base.fingerprint(), java.util.Arrays.copyOf(bytes, bytes.length + 1)),
                new SemanticArtifact(new ModelFingerprint("factory-model", "sha256", "0".repeat(64)), bytes),
                new SemanticArtifact(new ModelFingerprint("foreign", "sha256", base.fingerprint().digest()), bytes));
        for (int i = 0; i < invalid.size(); i++) {
            SemanticArtifact artifact = invalid.get(i);
            assertEquals(IllegalArgumentException.class, assertThrows(IllegalArgumentException.class,
                    () -> ChangeSetFactory.fromCandidateSnapshot(authority, BASE, artifact, FactoryModelArtifact.verifier(),
                            new FactoryModelSemanticComparator(), ChangeProvenance.of("challenge", "refusal"))).getClass());
            GovernanceHistoryException failure = assertThrows(GovernanceHistoryException.class,
                    () -> authority.accept(revision(CANDIDATE, artifact.fingerprint()), artifact));
            assertEquals(i == 3 ? GovernanceHistoryException.Code.UNSUPPORTED_ARTIFACT_FINGERPRINT
                    : GovernanceHistoryException.Code.FINGERPRINT_MISMATCH, failure.code());
            assertTrue(authority.findById(CANDIDATE).isEmpty());
            assertEquals(before, authority.revisions());
            assertArrayEquals(bytes, authority.resolve(BASE).artifact().canonicalBytes());
        }
        assertEquals(GovernanceHistoryException.Code.MISSING_REVISION,
                assertThrows(GovernanceHistoryException.class, () -> ChangeSetFactory.fromAuthoritativeRevisions(authority,
                        BASE, CANDIDATE, new FactoryModelSemanticComparator(), ChangeProvenance.of("challenge", "absent"))).code());
    }

    @Test
    void candidateSnapshotRetainsItsOwnFingerprintWithoutInventingRevisionIdentity() {
        FactoryModelVersion base = FactoryModelPublisher.publish(new FactoryModel(List.of(), List.of(), List.of()));
        FactoryModelVersion candidate = FactoryModelPublisher.publish(new FactoryModel(
                List.of(new ConfiguredResource(new MachineId(-7), "r", 1, null, 0)), List.of(), List.of()));
        ControlledRevisionAuthority authority = BuiltInStorage.open(temp.resolve("store"), FactoryModelArtifact.verifier()).controlledRevisions();
        authority.accept(revision(BASE, base.fingerprint()), new SemanticArtifact(base.fingerprint(), FactoryModelArtifact.encode(base)));
        ChangeSet change = ChangeSetFactory.fromCandidateSnapshot(authority, BASE,
                new SemanticArtifact(candidate.fingerprint(), FactoryModelArtifact.encode(candidate)), FactoryModelArtifact.verifier(),
                new FactoryModelSemanticComparator(), ChangeProvenance.of("challenge", "candidate"));
        assertNotEquals(base.fingerprint(), candidate.fingerprint());
        assertEquals(candidate.fingerprint(), change.candidateFingerprint());
        assertEquals(base.fingerprint(), change.baseFingerprint());
        assertTrue(change.resultingRevisionIdOptional().isEmpty());
        assertEquals(1, authority.revisions().size());
    }
}
