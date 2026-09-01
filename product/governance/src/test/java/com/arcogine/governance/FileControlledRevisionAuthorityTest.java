package com.arcogine.governance;

import static com.arcogine.governance.GovernanceHistoryException.Code.DUPLICATE_REVISION_ID;
import static com.arcogine.governance.GovernanceHistoryException.Code.FINGERPRINT_MISMATCH;
import static com.arcogine.governance.GovernanceHistoryException.Code.MISSING_ARTIFACT;
import static com.arcogine.governance.GovernanceHistoryException.Code.MISSING_PARENT;
import static com.arcogine.governance.GovernanceHistoryException.Code.STORAGE_INTEGRITY;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelArtifactV1;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.model.ResourceDefinition;
import com.arcogine.types.ControlledRevisionId;
import com.arcogine.types.MachineId;
import com.arcogine.types.ModelFingerprint;
import com.arcogine.types.ProductId;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileControlledRevisionAuthorityTest {

    private static final RevisionRecorder RECORDER =
            new RevisionRecorder("governance-test", "operator-17");
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

    @TempDir
    Path tempDirectory;

    @Test
    void rootRevisionSurvivesReopenWithExactRevisionArtifactAndProvenance() {
        FactoryModelVersion version = version("Widget", 5);
        ControlledRevision revision = revision(
                id(1),
                version.fingerprint(),
                List.of(),
                Instant.parse("2026-09-01T20:15:30.123456789Z"));

        authority().accept(revision, artifact(version));

        FileControlledRevisionAuthority reopened = authority();
        assertEquals(revision, reopened.findById(revision.id()).orElseThrow());
        HistoricalRevision resolved = reopened.resolve(revision.id());
        assertEquals(revision, resolved.revision());
        assertEquals(revision.provenance(), resolved.revision().provenance());
        assertArrayEquals(
                FactoryModelArtifactV1.encode(version), resolved.artifact().canonicalBytes());
        FactoryModelVersion reconstructed =
                FactoryModelArtifactV1.decode(resolved.artifact().canonicalBytes());
        assertEquals(version.model(), reconstructed.model());
        assertEquals(revision.modelFingerprint(), reconstructed.fingerprint());
    }

    @Test
    void revisionIdCannotBeAcceptedTwiceOrRebound() {
        FactoryModelVersion firstVersion = version("Widget", 5);
        ControlledRevision first = revision(
                id(2),
                firstVersion.fingerprint(),
                List.of(),
                Instant.parse("2026-09-01T20:00:00Z"));
        FileControlledRevisionAuthority authority = authority();
        authority.accept(first, artifact(firstVersion));

        GovernanceHistoryException sameFailure = assertThrows(
                GovernanceHistoryException.class,
                () -> authority.accept(first, artifact(firstVersion)));
        assertEquals(DUPLICATE_REVISION_ID, sameFailure.code());

        FactoryModelVersion secondVersion = version("Widget", 6);
        ControlledRevision rebound = revision(
                first.id(),
                secondVersion.fingerprint(),
                List.of(),
                Instant.parse("2026-09-01T21:00:00Z"));
        GovernanceHistoryException reboundFailure = assertThrows(
                GovernanceHistoryException.class,
                () -> authority.accept(rebound, artifact(secondVersion)));
        assertEquals(DUPLICATE_REVISION_ID, reboundFailure.code());
        assertEquals(first, authority.resolve(first.id()).revision());
    }

    @Test
    void parentMustAlreadyBeAuthoritativeAndSelfParentingRemainsInvalid() {
        FactoryModelVersion version = version("Widget", 5);
        ControlledRevisionId missingParent = id(3);
        ControlledRevision child = revision(
                id(4),
                version.fingerprint(),
                List.of(missingParent),
                Instant.parse("2026-09-01T20:00:00Z"));
        FileControlledRevisionAuthority authority = authority();

        GovernanceHistoryException missingFailure = assertThrows(
                GovernanceHistoryException.class,
                () -> authority.accept(child, artifact(version)));
        assertEquals(MISSING_PARENT, missingFailure.code());
        assertTrue(authority.findById(child.id()).isEmpty());

        ControlledRevision parent = revision(
                missingParent,
                version.fingerprint(),
                List.of(),
                Instant.parse("2026-09-01T19:00:00Z"));
        authority.accept(parent, artifact(version));
        authority.accept(child, artifact(version));
        assertEquals(
                List.of(parent.id()), authority.resolve(child.id()).revision().parentRevisionIds());

        ControlledRevisionId self = id(5);
        assertThrows(
                IllegalArgumentException.class,
                () -> revision(
                        self,
                        version.fingerprint(),
                        List.of(self),
                        Instant.parse("2026-09-01T22:00:00Z")));
    }

    @Test
    void f1ToF2ToF1KeepsThreeHistoricalOccurrencesAndDeduplicatesArtifact()
            throws IOException {
        FactoryModelVersion f1 = version("Widget", 5);
        FactoryModelVersion f2 = version("Widget", 6);
        ControlledRevision a = revision(
                id(6), f1.fingerprint(), List.of(), Instant.parse("2026-09-01T18:00:00Z"));
        ControlledRevision b = revision(
                id(7),
                f2.fingerprint(),
                List.of(a.id()),
                Instant.parse("2026-09-01T19:00:00Z"));
        ControlledRevision c = revision(
                id(8),
                f1.fingerprint(),
                List.of(b.id()),
                Instant.parse("2026-09-01T20:00:00Z"));
        FileControlledRevisionAuthority authority = authority();

        authority.accept(a, artifact(f1));
        authority.accept(b, artifact(f2));
        authority.accept(c, artifact(f1));

        assertNotEquals(a.id(), c.id());
        assertEquals(a.modelFingerprint(), c.modelFingerprint());
        assertEquals(3, authority.revisions().size());
        assertEquals(2, regularFiles(tempDirectory.resolve("artifacts")).size());
        assertEquals(f1.model(), reconstructed(authority.resolve(a.id())).model());
        assertEquals(f2.model(), reconstructed(authority.resolve(b.id())).model());
        assertEquals(f1.model(), reconstructed(authority.resolve(c.id())).model());
    }

    @Test
    void historicalResolutionDoesNotDependOnCurrentModel() {
        FactoryModelVersion historical = version("Historical widget", 5);
        ControlledRevision revision = revision(
                id(9),
                historical.fingerprint(),
                List.of(),
                Instant.parse("2026-09-01T18:00:00Z"));
        FileControlledRevisionAuthority authority = authority();
        authority.accept(revision, artifact(historical));

        FactoryModelVersion current = version("Current widget", 99);
        assertNotEquals(current.fingerprint(), historical.fingerprint());

        HistoricalRevision resolved = authority.resolve(revision.id());
        assertEquals(historical.model(), reconstructed(resolved).model());
        assertEquals(
                revision.modelFingerprint(),
                FACTORY_VERIFIER.fingerprint(resolved.artifact().canonicalBytes()));
    }

    @Test
    void fingerprintMismatchIsRejectedWithoutPartialRevision() {
        FactoryModelVersion recorded = version("Widget", 5);
        FactoryModelVersion supplied = version("Widget", 6);
        ControlledRevision revision = revision(
                id(10),
                recorded.fingerprint(),
                List.of(),
                Instant.parse("2026-09-01T18:00:00Z"));
        FileControlledRevisionAuthority authority = authority();

        GovernanceHistoryException failure = assertThrows(
                GovernanceHistoryException.class,
                () -> authority.accept(revision, artifact(supplied)));
        assertEquals(FINGERPRINT_MISMATCH, failure.code());
        assertTrue(authority.findById(revision.id()).isEmpty());
        assertTrue(authority.revisions().isEmpty());
    }

    @Test
    void missingOrCorruptHistoricalArtifactFailsAndIsNotSilentlyRepaired()
            throws IOException {
        FactoryModelVersion version = version("Widget", 5);
        ControlledRevision revision = revision(
                id(11),
                version.fingerprint(),
                List.of(),
                Instant.parse("2026-09-01T18:00:00Z"));
        FileControlledRevisionAuthority authority = authority();
        authority.accept(revision, artifact(version));
        Path artifactFile = onlyRegularFile(tempDirectory.resolve("artifacts"));

        Files.delete(artifactFile);
        GovernanceHistoryException missing = assertThrows(
                GovernanceHistoryException.class, () -> authority().resolve(revision.id()));
        assertEquals(MISSING_ARTIFACT, missing.code());

        ControlledRevision later = revision(
                id(12),
                version.fingerprint(),
                List.of(revision.id()),
                Instant.parse("2026-09-01T19:00:00Z"));
        GovernanceHistoryException noRepair = assertThrows(
                GovernanceHistoryException.class,
                () -> authority().accept(later, artifact(version)));
        assertEquals(MISSING_ARTIFACT, noRepair.code());
        assertTrue(authority().findById(later.id()).isEmpty());

        Files.write(artifactFile, new byte[] {1, 2, 3, 4});
        GovernanceHistoryException corrupt = assertThrows(
                GovernanceHistoryException.class, () -> authority().resolve(revision.id()));
        assertEquals(STORAGE_INTEGRITY, corrupt.code());
    }

    @Test
    void corruptRevisionMetadataFailsAsStorageIntegrityFailure() throws IOException {
        FactoryModelVersion version = version("Widget", 5);
        ControlledRevision revision = revision(
                id(13),
                version.fingerprint(),
                List.of(),
                Instant.parse("2026-09-01T18:00:00Z"));
        authority().accept(revision, artifact(version));

        Path revisionFile = onlyRegularFile(tempDirectory.resolve("revisions"));
        Files.write(revisionFile, new byte[] {9, 8, 7});

        GovernanceHistoryException failure = assertThrows(
                GovernanceHistoryException.class, () -> authority().findById(revision.id()));
        assertEquals(STORAGE_INTEGRITY, failure.code());
    }

    @Test
    void concurrentConflictingAcceptanceProducesOneImmutableWinner() throws Exception {
        FactoryModelVersion firstVersion = version("Widget", 5);
        FactoryModelVersion secondVersion = version("Widget", 6);
        ControlledRevisionId sharedId = id(14);
        ControlledRevision first = revision(
                sharedId,
                firstVersion.fingerprint(),
                List.of(),
                Instant.parse("2026-09-01T18:00:00Z"));
        ControlledRevision second = revision(
                sharedId,
                secondVersion.fingerprint(),
                List.of(),
                Instant.parse("2026-09-01T19:00:00Z"));
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<GovernanceHistoryException.Code> firstResult = executor.submit(
                    () -> acceptAfterStart(start, first, artifact(firstVersion)));
            Future<GovernanceHistoryException.Code> secondResult = executor.submit(
                    () -> acceptAfterStart(start, second, artifact(secondVersion)));
            start.countDown();

            List<GovernanceHistoryException.Code> results =
                    Arrays.asList(firstResult.get(), secondResult.get());
            assertEquals(1, results.stream().filter(result -> result == null).count());
            assertEquals(
                    1,
                    results.stream().filter(DUPLICATE_REVISION_ID::equals).count());
        }

        FileControlledRevisionAuthority reopened = authority();
        ControlledRevision winner = reopened.findById(sharedId).orElseThrow();
        assertTrue(winner.equals(first) || winner.equals(second));
        assertEquals(
                winner.modelFingerprint(), reopened.resolve(sharedId).artifact().fingerprint());
        assertEquals(1, reopened.revisions().size());
    }

    @Test
    void iterationOrderingIsDeterministicAcrossReopen() {
        FactoryModelVersion version = version("Widget", 5);
        ControlledRevision third = revision(
                id(23),
                version.fingerprint(),
                List.of(),
                Instant.parse("2026-09-01T18:00:00Z"));
        ControlledRevision first = revision(
                id(21),
                version.fingerprint(),
                List.of(),
                Instant.parse("2026-09-01T19:00:00Z"));
        ControlledRevision second = revision(
                id(22),
                version.fingerprint(),
                List.of(),
                Instant.parse("2026-09-01T20:00:00Z"));
        FileControlledRevisionAuthority authority = authority();
        authority.accept(third, artifact(version));
        authority.accept(first, artifact(version));
        authority.accept(second, artifact(version));

        List<ControlledRevisionId> expected = List.of(first.id(), second.id(), third.id());
        assertEquals(
                expected,
                authority.revisions().stream().map(ControlledRevision::id).toList());
        assertEquals(
                expected,
                authority().revisions().stream().map(ControlledRevision::id).toList());
    }

    @Test
    void semanticArtifactDefensivelyCopiesCanonicalBytes() {
        FactoryModelVersion version = version("Widget", 5);
        byte[] bytes = FactoryModelArtifactV1.encode(version);
        SemanticArtifact artifact = new SemanticArtifact(version.fingerprint(), bytes);
        bytes[0] ^= 1;
        assertEquals(
                version.fingerprint(), FACTORY_VERIFIER.fingerprint(artifact.canonicalBytes()));

        byte[] exposed = artifact.canonicalBytes();
        exposed[0] ^= 1;
        assertEquals(
                version.fingerprint(), FACTORY_VERIFIER.fingerprint(artifact.canonicalBytes()));
        assertFalse(Arrays.equals(bytes, artifact.canonicalBytes()));
    }

    private GovernanceHistoryException.Code acceptAfterStart(
            CountDownLatch start, ControlledRevision revision, SemanticArtifact artifact)
            throws InterruptedException {
        start.await();
        try {
            authority().accept(revision, artifact);
            return null;
        } catch (GovernanceHistoryException e) {
            return e.code();
        }
    }

    private FileControlledRevisionAuthority authority() {
        return new FileControlledRevisionAuthority(tempDirectory, FACTORY_VERIFIER);
    }

    private static ControlledRevision revision(
            ControlledRevisionId id,
            ModelFingerprint fingerprint,
            List<ControlledRevisionId> parents,
            Instant recordedAt) {
        return new ControlledRevision(
                id, fingerprint, parents, new RevisionProvenance(recordedAt, RECORDER));
    }

    private static ControlledRevisionId id(int suffix) {
        return ControlledRevisionId.parse(
                "00000000-0000-4000-8000-" + String.format("%012d", suffix));
    }

    private static SemanticArtifact artifact(FactoryModelVersion version) {
        return new SemanticArtifact(
                version.fingerprint(), FactoryModelArtifactV1.encode(version));
    }

    private static FactoryModelVersion reconstructed(HistoricalRevision revision) {
        return FactoryModelArtifactV1.decode(revision.artifact().canonicalBytes());
    }

    private static FactoryModelVersion version(String productName, long duration) {
        ResourceDefinition machine =
                new ResourceDefinition(new MachineId(1), "Mill", 1, 125.5, 2);
        OperationStepDefinition step = new OperationStepDefinition(
                1, "Machine", Set.of(new MachineId(1)), duration);
        OperationDefinition operation =
                new OperationDefinition(100, "Routing", List.of(step));
        ProductDefinition product =
                new ProductDefinition(new ProductId(10), productName, operation.id());
        return FactoryModelPublisher.publish(
                new FactoryModel(List.of(machine), List.of(operation), List.of(product)));
    }

    private static Path onlyRegularFile(Path directory) throws IOException {
        List<Path> files = regularFiles(directory);
        assertEquals(1, files.size());
        return files.getFirst();
    }

    private static List<Path> regularFiles(Path directory) throws IOException {
        try (var paths = Files.list(directory)) {
            return paths.filter(Files::isRegularFile).toList();
        }
    }
}
