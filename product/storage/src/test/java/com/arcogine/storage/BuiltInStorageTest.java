package com.arcogine.storage;

import static com.arcogine.governance.GovernanceHistoryException.Code.DUPLICATE_REVISION_ID;
import static com.arcogine.governance.GovernanceHistoryException.Code.FINGERPRINT_MISMATCH;
import static com.arcogine.governance.GovernanceHistoryException.Code.MISSING_ARTIFACT;
import static com.arcogine.governance.GovernanceHistoryException.Code.MISSING_PARENT;
import static com.arcogine.governance.GovernanceHistoryException.Code.STORAGE_INTEGRITY;
import static com.arcogine.governance.GovernanceHistoryException.Code.UNSUPPORTED_ARTIFACT_FINGERPRINT;
import static com.arcogine.governance.GovernanceHistoryException.Code.UNSUPPORTED_STORE;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.governance.ControlledRevision;
import com.arcogine.governance.ControlledRevisionAuthority;
import com.arcogine.governance.GovernanceHistoryException;
import com.arcogine.governance.HistoricalRevision;
import com.arcogine.governance.RevisionProvenance;
import com.arcogine.governance.RevisionRecorder;
import com.arcogine.governance.SemanticArtifact;
import com.arcogine.governance.SemanticArtifactVerifier;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelArtifact;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.types.ControlledRevisionId;
import com.arcogine.types.MachineId;
import com.arcogine.types.ModelFingerprint;
import com.arcogine.types.ProductId;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BuiltInStorageTest {

    private static final RevisionRecorder RECORDER =
            new RevisionRecorder("governance-test", "operator-17");
    private static final Instant ACCEPTED_AT = Instant.parse("2026-09-02T08:30:45.123456789Z");
    private static final SemanticArtifactVerifier FACTORY_VERIFIER = FactoryModelArtifact.verifier();
    private static final String STORE_MARKER_PREFIX = "arcogine-proving-revision-store/strict-utf8\0";
    private static final String LEGACY_STORE_MARKER_PREFIX = "arcogine-proving-revision-store\0";

    @TempDir
    Path tempDirectory;

    @Test
    void acceptedRevisionResolvesInAnIndependentJvm() throws Exception {
        FactoryModelVersion version = version("Widget", 5);
        ControlledRevision accepted = authority().accept(
                revision(id(25), version.fingerprint(), List.of(), ACCEPTED_AT), artifact(version));
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        Process process = new ProcessBuilder(
                        java,
                        "-cp",
                        System.getProperty("storage.test.classpath"),
                        StorageReopenProbe.class.getName(),
                        store().toString(),
                        accepted.id().toString())
                .redirectErrorStream(true)
                .start();
        if (!process.waitFor(10, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new AssertionError("child JVM timed out");
        }
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, process.exitValue(), output);
        assertTrue(output.contains("REOPEN_OK"), output);
    }

    @Test
    void publicOpenerExposesOnlyTheContractAndBackendIsNotPublic() throws Exception {
        assertEquals(ArcogineStorage.class,
                BuiltInStorage.class.getMethod("open", Path.class, SemanticArtifactVerifier.class)
                        .getReturnType());
        assertFalse(Modifier.isPublic(FileArcogineStorage.class.getModifiers()));
        assertTrue(BuiltInStorage.open(store(), FACTORY_VERIFIER)
                .controlledRevisions()
                .revisions()
                .isEmpty());
    }

    @Test
    void rejectionBeforeRecordInstallationRemovesNewOrphanArtifact() throws IOException {
        Clock failingClock = new Clock() {
            @Override
            public ZoneOffset getZone() {
                return ZoneOffset.UTC;
            }

            @Override
            public Clock withZone(java.time.ZoneId zone) {
                return this;
            }

            @Override
            public Instant instant() {
                throw new IllegalStateException("recording time unavailable");
            }
        };
        FactoryModelVersion version = version("Widget", 5);
        ControlledRevisionAuthority authority = BuiltInStorage
                .open(store(), FACTORY_VERIFIER, failingClock)
                .controlledRevisions();

        GovernanceHistoryException failure = assertThrows(GovernanceHistoryException.class,
                () -> authority.accept(
                        revision(id(26), version.fingerprint(), List.of(), ACCEPTED_AT),
                        artifact(version)));
        assertEquals(STORAGE_INTEGRITY, failure.code());
        assertTrue(authority.revisions().isEmpty());
        assertTrue(regularFiles(store().resolve("artifacts")).isEmpty());
    }

    @Test
    void failedAtomicRevisionWriteLeavesNoRevisionOrOrphanArtifact() throws IOException {
        FactoryModelVersion version = version("Widget", 5);
        for (boolean unsupportedAtomicMove : List.of(false, true)) {
            Path root = tempDirectory.resolve("atomic-revision-failure-" + unsupportedAtomicMove);
            ControlledRevisionAuthority failing = FileArcogineStorage.open(
                    root, FACTORY_VERIFIER, Clock.fixed(ACCEPTED_AT, ZoneOffset.UTC),
                    (source, target) -> {
                        if (target.getFileName().toString().endsWith(".revision")) {
                            if (unsupportedAtomicMove) {
                                throw new AtomicMoveNotSupportedException(
                                        source.toString(), target.toString(), "test filesystem");
                            }
                            throw new IOException("test write failure");
                        }
                        Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
                    });
            ControlledRevision candidate = revision(id(74), version.fingerprint(), List.of(), ACCEPTED_AT);

            GovernanceHistoryException failure = assertThrows(GovernanceHistoryException.class,
                    () -> failing.accept(candidate, artifact(version)));
            assertEquals(STORAGE_INTEGRITY, failure.code());
            assertTrue(failing.revisions().isEmpty());
            assertTrue(regularFiles(root.resolve("artifacts")).isEmpty());
            assertTrue(regularFiles(root.resolve("revisions")).isEmpty());
            assertTrue(contents(root).keySet().stream().noneMatch(path -> path.contains(".pending-")));

            ControlledRevisionAuthority reopened = BuiltInStorage.open(root, FACTORY_VERIFIER).controlledRevisions();
            assertEquals(candidate.id(), reopened.accept(candidate, artifact(version)).id());
            assertEquals(candidate.id(), reopened.resolve(candidate.id()).revision().id());
        }
    }

    @Test
    void cleanupFailureIsReportedWithoutInventingAnAcceptedRevision() throws IOException {
        Path root = tempDirectory.resolve("cleanup-failure");
        FileArcogineStorage.FileOperations cannotRemoveArtifact =
                new FileArcogineStorage.FileOperations() {
                    @Override
                    boolean deleteIfExists(Path path) throws IOException {
                        if (path.getFileName().toString().endsWith(".artifact")) {
                            throw new IOException("artifact cleanup unavailable");
                        }
                        return super.deleteIfExists(path);
                    }
                };
        ControlledRevisionAuthority authority = FileArcogineStorage.open(root, FACTORY_VERIFIER,
                Clock.fixed(ACCEPTED_AT, ZoneOffset.UTC), (source, target) -> {
                    if (target.getFileName().toString().endsWith(".revision")) {
                        throw new IOException("revision write unavailable");
                    }
                    Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
                }, cannotRemoveArtifact);
        FactoryModelVersion version = version("Widget", 5);
        ControlledRevision candidate = revision(id(82), version.fingerprint(), List.of(), ACCEPTED_AT);

        GovernanceHistoryException failure = assertThrows(GovernanceHistoryException.class,
                () -> authority.accept(candidate, artifact(version)));
        assertEquals(STORAGE_INTEGRITY, failure.code());
        assertEquals(1, failure.getSuppressed().length);
        assertTrue(authority.revisions().isEmpty());
        assertEquals(1, regularFiles(root.resolve("artifacts")).size());
        assertTrue(regularFiles(root.resolve("revisions")).isEmpty());
    }

    @Test
    void failedTemporaryCleanupDoesNotCreateHistory() throws IOException {
        Path root = tempDirectory.resolve("temporary-cleanup-failure");
        FileArcogineStorage.FileOperations cannotRemovePending =
                new FileArcogineStorage.FileOperations() {
                    @Override
                    boolean deleteIfExists(Path path) throws IOException {
                        if (path.getFileName().toString().endsWith(".tmp")) {
                            throw new IOException("temporary cleanup unavailable");
                        }
                        return super.deleteIfExists(path);
                    }
                };
        ControlledRevisionAuthority authority = FileArcogineStorage.open(root, FACTORY_VERIFIER,
                Clock.fixed(ACCEPTED_AT, ZoneOffset.UTC), (source, target) -> {
                    if (target.getFileName().toString().endsWith(".revision")) {
                        throw new IOException("revision write unavailable");
                    }
                    Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
                }, cannotRemovePending);
        FactoryModelVersion version = version("Widget", 5);
        ControlledRevision candidate = revision(id(83), version.fingerprint(), List.of(), ACCEPTED_AT);

        GovernanceHistoryException failure = assertThrows(GovernanceHistoryException.class,
                () -> authority.accept(candidate, artifact(version)));
        assertEquals(STORAGE_INTEGRITY, failure.code());
        assertTrue(authority.revisions().isEmpty());
        assertTrue(regularFiles(root.resolve("artifacts")).isEmpty());
        assertTrue(regularFiles(root.resolve("revisions")).stream()
                .anyMatch(path -> path.getFileName().toString().endsWith(".tmp")));
    }

    @Test
    void reportedFailureAfterRevisionInstallationKeepsItsArtifactBinding() throws IOException {
        Path root = tempDirectory.resolve("post-install-failure");
        ControlledRevisionAuthority authority = FileArcogineStorage.open(root, FACTORY_VERIFIER,
                Clock.fixed(ACCEPTED_AT, ZoneOffset.UTC), (source, target) -> {
                    Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
                    if (target.getFileName().toString().endsWith(".revision")) {
                        throw new IOException("acknowledgment unavailable after move");
                    }
                });
        FactoryModelVersion version = version("Widget", 5);
        ControlledRevision candidate = revision(id(84), version.fingerprint(), List.of(), ACCEPTED_AT);

        GovernanceHistoryException failure = assertThrows(GovernanceHistoryException.class,
                () -> authority.accept(candidate, artifact(version)));
        assertEquals(STORAGE_INTEGRITY, failure.code());
        HistoricalRevision resolved = BuiltInStorage.open(root, FACTORY_VERIFIER)
                .controlledRevisions().resolve(candidate.id());
        assertEquals(candidate.modelFingerprint(), resolved.artifact().fingerprint());
        assertEquals(1, regularFiles(root.resolve("artifacts")).size());
    }

    @Test
    void failedRevisionWritePreservesArtifactSharedWithAcceptedHistory() throws IOException {
        FactoryModelVersion version = version("Widget", 5);
        ControlledRevision first = authorityAt(ACCEPTED_AT).accept(
                revision(id(75), version.fingerprint(), List.of(), ACCEPTED_AT), artifact(version));
        Map<String, String> before = contents(store());
        ControlledRevision candidate = revision(id(76), version.fingerprint(), List.of(first.id()), ACCEPTED_AT);
        ControlledRevisionAuthority failing = FileArcogineStorage.open(
                store(), FACTORY_VERIFIER, Clock.fixed(ACCEPTED_AT, ZoneOffset.UTC),
                (source, target) -> {
                    if (target.getFileName().toString().endsWith(".revision")) {
                        throw new IOException("test revision write failure");
                    }
                    Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
                });

        GovernanceHistoryException failure = assertThrows(GovernanceHistoryException.class,
                () -> failing.accept(candidate, artifact(version)));
        assertEquals(STORAGE_INTEGRITY, failure.code());
        assertEquals(before, contents(store()));
        assertEquals(first, authority().resolve(first.id()).revision());
        assertTrue(authority().findById(candidate.id()).isEmpty());

        ControlledRevision retried = authorityAt(ACCEPTED_AT).accept(candidate, artifact(version));
        assertEquals(List.of(first.id()), retried.parentRevisionIds());
        assertEquals(1, regularFiles(store().resolve("artifacts")).size());
    }

    @Test
    void failedMarkerWriteLeavesAnUnownedRootThatCannotBeAdopted() throws IOException {
        Path root = tempDirectory.resolve("failed-marker-write");
        GovernanceHistoryException failure = assertThrows(GovernanceHistoryException.class,
                () -> FileArcogineStorage.open(
                        root, FACTORY_VERIFIER, Clock.fixed(ACCEPTED_AT, ZoneOffset.UTC),
                        (source, target) -> {
                            throw new IOException("test marker write failure");
                        }));
        assertEquals(STORAGE_INTEGRITY, failure.code());
        assertTrue(Files.isDirectory(root));
        assertFalse(Files.exists(root.resolve("proving-store")));
        assertTrue(contents(root).keySet().stream().noneMatch(path -> path.contains(".pending-")));
        Map<String, String> before = contents(root);

        GovernanceHistoryException reopen = assertThrows(GovernanceHistoryException.class,
                () -> BuiltInStorage.open(root, FACTORY_VERIFIER));
        assertEquals(UNSUPPORTED_STORE, reopen.code());
        assertEquals(before, contents(root));
    }

    @Test
    void parentAndRootCreationFailuresLeaveNoOwnedStore() {
        Path parentFailure = tempDirectory.resolve("parent-failure").resolve("store");
        FileArcogineStorage.FileOperations cannotCreateParent = new FileArcogineStorage.FileOperations() {
            @Override
            void createDirectories(Path path) throws IOException {
                throw new IOException("parent unavailable");
            }
        };
        GovernanceHistoryException parent = assertThrows(GovernanceHistoryException.class,
                () -> withFileOperations(parentFailure, cannotCreateParent));
        assertEquals(STORAGE_INTEGRITY, parent.code());
        assertFalse(Files.exists(parentFailure.getParent()));

        Path rootFailure = tempDirectory.resolve("root-failure");
        FileArcogineStorage.FileOperations cannotCreateRoot = new FileArcogineStorage.FileOperations() {
            @Override
            void createDirectory(Path path) throws IOException {
                throw new IOException("root unavailable");
            }
        };
        GovernanceHistoryException root = assertThrows(GovernanceHistoryException.class,
                () -> withFileOperations(rootFailure, cannotCreateRoot));
        assertEquals(STORAGE_INTEGRITY, root.code());
        assertFalse(Files.exists(rootFailure));
    }

    @Test
    void filesystemRootIsNeverAdoptedAsAStore() {
        Path root = tempDirectory.getRoot();
        GovernanceHistoryException refused = assertThrows(GovernanceHistoryException.class,
                () -> BuiltInStorage.open(root, FACTORY_VERIFIER));
        assertEquals(UNSUPPORTED_STORE, refused.code());
    }

    @Test
    void unreadableOwnershipMarkerAndRevisionListingFailClosed() throws IOException {
        authority();
        Map<String, String> before = contents(store());
        FileArcogineStorage.FileOperations unreadableMarker = new FileArcogineStorage.FileOperations() {
            @Override
            byte[] readAllBytes(Path path) throws IOException {
                if (path.getFileName().toString().equals("proving-store")) {
                    throw new IOException("marker unreadable");
                }
                return super.readAllBytes(path);
            }
        };
        GovernanceHistoryException open = assertThrows(GovernanceHistoryException.class,
                () -> withFileOperations(store(), unreadableMarker));
        assertEquals(STORAGE_INTEGRITY, open.code());
        assertEquals(before, contents(store()));

        FileArcogineStorage.FileOperations unreadableListing = new FileArcogineStorage.FileOperations() {
            @Override
            Stream<Path> list(Path path) throws IOException {
                throw new IOException("revision listing unavailable");
            }
        };
        ControlledRevisionAuthority opened = withFileOperations(store(), unreadableListing);
        GovernanceHistoryException list = assertThrows(GovernanceHistoryException.class,
                opened::revisions);
        assertEquals(STORAGE_INTEGRITY, list.code());
        assertEquals(before, contents(store()));
    }

    @Test
    void missingDigestImplementationLeavesHistoryUnchanged() throws IOException {
        Path root = tempDirectory.resolve("digest-unavailable");
        FileArcogineStorage.FileOperations noDigest = new FileArcogineStorage.FileOperations() {
            @Override
            MessageDigest digest(String algorithm) throws NoSuchAlgorithmException {
                throw new NoSuchAlgorithmException(algorithm);
            }
        };
        ControlledRevisionAuthority authority = withFileOperations(root, noDigest);
        Map<String, String> before = contents(root);
        FactoryModelVersion version = version("Widget", 5);
        GovernanceHistoryException rejected = assertThrows(GovernanceHistoryException.class,
                () -> authority.accept(revision(id(81), version.fingerprint(), List.of(), ACCEPTED_AT),
                        artifact(version)));
        assertEquals(STORAGE_INTEGRITY, rejected.code());
        assertEquals(before, contents(root));
    }

    @Test
    void absentRevisionAndMissingSharedArtifactAreDistinctFailures() throws IOException {
        ControlledRevisionAuthority authority = authority();
        GovernanceHistoryException absent = assertThrows(GovernanceHistoryException.class,
                () -> authority.resolve(id(27)));
        assertEquals(GovernanceHistoryException.Code.MISSING_REVISION, absent.code());

        FactoryModelVersion version = version("Widget", 5);
        authority.accept(revision(id(28), version.fingerprint(), List.of(), ACCEPTED_AT),
                artifact(version));
        Files.delete(onlyRegularFile(store().resolve("artifacts")));
        GovernanceHistoryException missing = assertThrows(GovernanceHistoryException.class,
                () -> authority.accept(
                        revision(id(29), version.fingerprint(), List.of(), ACCEPTED_AT),
                        artifact(version)));
        assertEquals(MISSING_ARTIFACT, missing.code());
        assertEquals(1, authority.revisions().size());
    }

    @Test
    void incompleteOwnedRootIsRefusedWithoutRecreatingMissingDirectories() throws IOException {
        ControlledRevisionAuthority opened = authority();
        Files.delete(store().resolve("artifacts"));
        Map<String, String> before = contents(store());

        GovernanceHistoryException reopen = assertThrows(GovernanceHistoryException.class,
                () -> BuiltInStorage.open(store(), FACTORY_VERIFIER));
        assertEquals(UNSUPPORTED_STORE, reopen.code());
        GovernanceHistoryException reuse = assertThrows(GovernanceHistoryException.class,
                opened::revisions);
        assertEquals(UNSUPPORTED_STORE, reuse.code());
        assertEquals(before, contents(store()));
    }

    @Test
    void missingRevisionDirectoryIsAlsoRefusedWithoutRepair() throws IOException {
        ControlledRevisionAuthority opened = authority();
        Files.delete(store().resolve("revisions"));
        Map<String, String> before = contents(store());

        GovernanceHistoryException reopen = assertThrows(GovernanceHistoryException.class,
                () -> BuiltInStorage.open(store(), FACTORY_VERIFIER));
        assertEquals(UNSUPPORTED_STORE, reopen.code());
        GovernanceHistoryException reuse = assertThrows(GovernanceHistoryException.class,
                opened::revisions);
        assertEquals(UNSUPPORTED_STORE, reuse.code());
        assertEquals(before, contents(store()));
    }

    @Test
    void unrelatedFilesInRevisionDirectoryDoNotBecomeHistoricalRevisions() throws IOException {
        ControlledRevisionAuthority authority = authority();
        Files.write(store().resolve("revisions").resolve("operator-note.txt"),
                "not history".getBytes(StandardCharsets.UTF_8));
        assertTrue(authority.revisions().isEmpty());
    }

    @Test
    void malformedArtifactRecordsFailWithoutRepairOrReplacement() throws IOException {
        byte[] prefix = "arcogine-proving-artifact\0".getBytes(StandardCharsets.US_ASCII);
        for (int variant = 0; variant < 7; variant++) {
            Path root = tempDirectory.resolve("artifact-malformed-" + variant);
            ControlledRevisionAuthority authority = BuiltInStorage
                    .open(root, FACTORY_VERIFIER)
                    .controlledRevisions();
            FactoryModelVersion version = version("Widget", 5);
            ControlledRevision accepted = authority.accept(
                    revision(id(30 + variant), version.fingerprint(), List.of(), ACCEPTED_AT),
                    artifact(version));
            Path artifactFile = onlyRegularFile(root.resolve("artifacts"));
            byte[] encoded = Files.readAllBytes(artifactFile);
            ByteBuffer buffer = ByteBuffer.wrap(encoded);
            int firstLength = buffer.getInt(prefix.length);
            int cursor = prefix.length + Integer.BYTES + firstLength;
            for (int field = 1; field < 3; field++) {
                cursor += Integer.BYTES + buffer.getInt(cursor);
            }
            switch (variant) {
                case 0 -> encoded[prefix.length + Integer.BYTES] ^= 1; // different fingerprint namespace
                case 1 -> ByteBuffer.wrap(encoded).putLong(cursor, -1); // invalid byte length
                case 2 -> ByteBuffer.wrap(encoded).putLong(cursor, encoded.length); // truncated body
                case 3 -> encoded = Arrays.copyOf(encoded, encoded.length + 1); // trailing bytes
                case 4 -> encoded[prefix.length + Integer.BYTES] = (byte) 0xff; // invalid UTF-8
                case 5 -> ByteBuffer.wrap(encoded).putInt(prefix.length, -1); // negative string length
                case 6 -> ByteBuffer.wrap(encoded).putLong(cursor, (long) Integer.MAX_VALUE + 1);
                default -> throw new AssertionError(variant);
            }
            Files.write(artifactFile, encoded);
            byte[] corrupt = Files.readAllBytes(artifactFile);

            GovernanceHistoryException failure = assertThrows(GovernanceHistoryException.class,
                    () -> authority.resolve(accepted.id()));
            assertEquals(STORAGE_INTEGRITY, failure.code(), "variant " + variant);
            assertArrayEquals(corrupt, Files.readAllBytes(artifactFile));
        }
    }

    @Test
    void changedStoredArtifactBytesAreRefusedWithoutRewritingHistory() throws IOException {
        FactoryModelVersion version = version("Widget", 5);
        ControlledRevisionAuthority authority = authority();
        ControlledRevision accepted = authority.accept(
                revision(id(50), version.fingerprint(), List.of(), ACCEPTED_AT), artifact(version));
        Path artifactFile = onlyRegularFile(store().resolve("artifacts"));
        byte[] changed = Files.readAllBytes(artifactFile);
        byte[] replacement = FactoryModelArtifact.encode(version("Widget", 6));
        assertEquals(FactoryModelArtifact.encode(version).length, replacement.length);
        System.arraycopy(replacement, 0, changed, changed.length - replacement.length, replacement.length);
        Files.write(artifactFile, changed);

        GovernanceHistoryException failure = assertThrows(GovernanceHistoryException.class,
                () -> authority.resolve(accepted.id()));

        assertEquals(FINGERPRINT_MISMATCH, failure.code());
        assertArrayEquals(changed, Files.readAllBytes(artifactFile));
        assertEquals(accepted, authority.findById(accepted.id()).orElseThrow());

        ControlledRevision later = revision(id(79), version.fingerprint(), List.of(), ACCEPTED_AT);
        GovernanceHistoryException noOverwrite = assertThrows(GovernanceHistoryException.class,
                () -> authority.accept(later, artifact(version)));
        assertEquals(STORAGE_INTEGRITY, noOverwrite.code());
        assertArrayEquals(changed, Files.readAllBytes(artifactFile));
        assertEquals(List.of(accepted), authority.revisions());
    }

    @Test
    void verifierFailureDuringResolutionFailsClosedWithoutChangingStoredBytes() throws IOException {
        AtomicBoolean failVerification = new AtomicBoolean();
        SemanticArtifactVerifier verifier = new SemanticArtifactVerifier() {
            @Override
            public boolean supports(ModelFingerprint fingerprint) {
                return FACTORY_VERIFIER.supports(fingerprint);
            }

            @Override
            public ModelFingerprint fingerprint(byte[] canonicalBytes) {
                if (failVerification.get()) {
                    throw new IllegalStateException("definition unavailable");
                }
                return FACTORY_VERIFIER.fingerprint(canonicalBytes);
            }

            @Override
            public String definitionBinding() {
                return FACTORY_VERIFIER.definitionBinding();
            }
        };
        Path root = tempDirectory.resolve("verifier-failure");
        ControlledRevisionAuthority authority = BuiltInStorage.open(root, verifier).controlledRevisions();
        FactoryModelVersion version = version("Widget", 5);
        ControlledRevision accepted = authority.accept(
                revision(id(51), version.fingerprint(), List.of(), ACCEPTED_AT), artifact(version));
        Map<String, String> before = contents(root);
        failVerification.set(true);

        GovernanceHistoryException failure = assertThrows(GovernanceHistoryException.class,
                () -> authority.resolve(accepted.id()));

        assertEquals(STORAGE_INTEGRITY, failure.code());
        assertEquals(before, contents(root));
    }

    @Test
    void malformedRevisionFilenameAndParentCountFailExplicitly() throws IOException {
        ControlledRevisionAuthority authority = authority();
        Path revisions = store().resolve("revisions");
        Path invalidName = revisions.resolve("not-a-uuid.revision");
        Files.write(invalidName, new byte[] {1});
        GovernanceHistoryException badName = assertThrows(GovernanceHistoryException.class,
                authority::revisions);
        assertEquals(STORAGE_INTEGRITY, badName.code());
        Files.delete(invalidName);

        FactoryModelVersion version = version("Widget", 5);
        ControlledRevision accepted = authority.accept(
                revision(id(36), version.fingerprint(), List.of(), ACCEPTED_AT), artifact(version));
        Path record = onlyRegularFile(revisions);
        byte[] encoded = Files.readAllBytes(record);
        byte[] magic = "arcogine-proving-revision\0".getBytes(StandardCharsets.US_ASCII);
        int cursor = magic.length;
        for (int field = 0; field < 4; field++) {
            cursor += Integer.BYTES + ByteBuffer.wrap(encoded).getInt(cursor);
        }
        ByteBuffer.wrap(encoded).putInt(cursor, 2);
        Files.write(record, encoded);
        GovernanceHistoryException badParentCount = assertThrows(GovernanceHistoryException.class,
                () -> authority.resolve(accepted.id()));
        assertEquals(STORAGE_INTEGRITY, badParentCount.code());

        ByteBuffer.wrap(encoded).putInt(cursor, -1);
        Files.write(record, encoded);
        GovernanceHistoryException negativeParentCount = assertThrows(GovernanceHistoryException.class,
                () -> authority.resolve(accepted.id()));
        assertEquals(STORAGE_INTEGRITY, negativeParentCount.code());
    }

    @Test
    void revisionRecordWithAnotherIdentityIsNotResolvedUnderItsFilename() throws IOException {
        ControlledRevisionAuthority authority = authority();
        FactoryModelVersion version = version("Widget", 5);
        ControlledRevision accepted = authority.accept(
                revision(id(52), version.fingerprint(), List.of(), ACCEPTED_AT), artifact(version));
        Path record = onlyRegularFile(store().resolve("revisions"));
        byte[] changed = Files.readAllBytes(record);
        int idStart = "arcogine-proving-revision\0".getBytes(StandardCharsets.US_ASCII).length
                + Integer.BYTES;
        byte[] anotherId = id(53).toString().getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(anotherId, 0, changed, idStart, anotherId.length);
        Files.write(record, changed);

        GovernanceHistoryException failure = assertThrows(GovernanceHistoryException.class,
                () -> authority.resolve(accepted.id()));

        assertEquals(STORAGE_INTEGRITY, failure.code());
        assertArrayEquals(changed, Files.readAllBytes(record));
    }

    @Test
    void truncatedRecorderTextIsNotAcceptedAsRecoveredHistory() throws IOException {
        ControlledRevisionAuthority authority = authority();
        FactoryModelVersion version = version("Widget", 5);
        ControlledRevision accepted = authority.accept(
                revision(id(54), version.fingerprint(), List.of(), ACCEPTED_AT), artifact(version));
        Path record = onlyRegularFile(store().resolve("revisions"));
        byte[] truncated = Arrays.copyOf(Files.readAllBytes(record), (int) Files.size(record) - 1);
        Files.write(record, truncated);

        GovernanceHistoryException failure = assertThrows(GovernanceHistoryException.class,
                () -> authority.resolve(accepted.id()));

        assertEquals(STORAGE_INTEGRITY, failure.code());
        assertArrayEquals(truncated, Files.readAllBytes(record));
    }

    @Test
    void rootRevisionSurvivesReopenWithAuthorityOwnedProvenanceAndExactArtifact() {
        FactoryModelVersion version = version("Widget", 5);
        ControlledRevision candidate = revision(
                id(1),
                version.fingerprint(),
                List.of(),
                Instant.parse("1999-01-01T00:00:00Z"));

        ControlledRevision accepted = authorityAt(ACCEPTED_AT).accept(candidate, artifact(version));

        assertEquals(candidate.id(), accepted.id());
        assertEquals(candidate.modelFingerprint(), accepted.modelFingerprint());
        assertEquals(candidate.parentRevisionIds(), accepted.parentRevisionIds());
        assertEquals(RECORDER, accepted.provenance().recorder());
        assertEquals(ACCEPTED_AT, accepted.provenance().recordedAt());
        assertNotEquals(candidate.provenance().recordedAt(), accepted.provenance().recordedAt());

        ControlledRevisionAuthority reopened = authority();
        assertEquals(accepted, reopened.findById(accepted.id()).orElseThrow());
        HistoricalRevision resolved = reopened.resolve(accepted.id());
        assertEquals(accepted, resolved.revision());
        assertEquals(accepted.provenance(), resolved.revision().provenance());
        assertArrayEquals(
                FactoryModelArtifact.encode(version), resolved.artifact().canonicalBytes());
        FactoryModelVersion reconstructed =
                FactoryModelArtifact.decode(resolved.artifact().canonicalBytes());
        assertEquals(version.model(), reconstructed.model());
        assertEquals(accepted.modelFingerprint(), reconstructed.fingerprint());
    }

    @Test
    void revisionIdCannotBeAcceptedTwiceOrRebound() {
        FactoryModelVersion firstVersion = version("Widget", 5);
        ControlledRevision first = revision(
                id(2),
                firstVersion.fingerprint(),
                List.of(),
                Instant.parse("2026-09-01T20:00:00Z"));
        ControlledRevisionAuthority authority = authorityAt(ACCEPTED_AT);
        ControlledRevision acceptedFirst = authority.accept(first, artifact(firstVersion));

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
        assertEquals(acceptedFirst, authority.resolve(first.id()).revision());
    }

    @Test
    void revisionIdAlsoBindsParentsAndRecorderButNotCandidateTimestamp() {
        FactoryModelVersion version = version("Widget", 5);
        ControlledRevisionAuthority authority = authorityAt(ACCEPTED_AT);
        ControlledRevision parent = authority.accept(
                revision(id(77), version.fingerprint(), List.of(), ACCEPTED_AT), artifact(version));
        ControlledRevision original = revision(
                id(78), version.fingerprint(), List.of(), ACCEPTED_AT, RECORDER);
        ControlledRevision accepted = authority.accept(original, artifact(version));
        ControlledRevision differentParent = revision(
                original.id(), version.fingerprint(), List.of(parent.id()), ACCEPTED_AT, RECORDER);
        ControlledRevision differentRecorder = revision(
                original.id(), version.fingerprint(), List.of(), ACCEPTED_AT,
                new RevisionRecorder("import-service", "operator-17"));

        for (ControlledRevision rebound : List.of(differentParent, differentRecorder)) {
            GovernanceHistoryException failure = assertThrows(GovernanceHistoryException.class,
                    () -> authority.accept(rebound, artifact(version)));
            assertEquals(DUPLICATE_REVISION_ID, failure.code());
            assertTrue(failure.getMessage().contains("bound to different immutable content"));
        }
        ControlledRevision timestampOnly = revision(
                original.id(), version.fingerprint(), List.of(),
                Instant.parse("1999-01-01T00:00:00Z"), RECORDER);
        GovernanceHistoryException duplicate = assertThrows(GovernanceHistoryException.class,
                () -> authority.accept(timestampOnly, artifact(version)));
        assertEquals(DUPLICATE_REVISION_ID, duplicate.code());
        assertTrue(duplicate.getMessage().contains("already accepted"));
        assertEquals(accepted, authority().resolve(original.id()).revision());
        assertEquals(List.of(parent, accepted), authority().revisions());
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
        ControlledRevisionAuthority authority = authorityAt(ACCEPTED_AT);

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
        ControlledRevision acceptedParent = authority.accept(parent, artifact(version));
        ControlledRevision acceptedChild = authority.accept(child, artifact(version));
        assertEquals(
                List.of(acceptedParent.id()), acceptedChild.parentRevisionIds());
        assertEquals(
                List.of(acceptedParent.id()),
                authority.resolve(acceptedChild.id()).revision().parentRevisionIds());

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
        ControlledRevisionAuthority authority = authorityAt(ACCEPTED_AT);

        ControlledRevision acceptedA = authority.accept(a, artifact(f1));
        ControlledRevision acceptedB = authority.accept(b, artifact(f2));
        ControlledRevision acceptedC = authority.accept(c, artifact(f1));

        assertNotEquals(acceptedA.id(), acceptedC.id());
        assertEquals(acceptedA.modelFingerprint(), acceptedC.modelFingerprint());
        assertEquals(3, authority.revisions().size());
        assertEquals(2, regularFiles(store().resolve("artifacts")).size());
        assertEquals(f1.model(), reconstructed(authority.resolve(acceptedA.id())).model());
        assertEquals(f2.model(), reconstructed(authority.resolve(acceptedB.id())).model());
        assertEquals(f1.model(), reconstructed(authority.resolve(acceptedC.id())).model());
    }

    @Test
    void historicalResolutionDoesNotDependOnCurrentModel() {
        FactoryModelVersion historical = version("Historical widget", 5);
        ControlledRevision candidate = revision(
                id(9),
                historical.fingerprint(),
                List.of(),
                Instant.parse("2026-09-01T18:00:00Z"));
        ControlledRevisionAuthority authority = authorityAt(ACCEPTED_AT);
        ControlledRevision accepted = authority.accept(candidate, artifact(historical));

        FactoryModelVersion current = version("Current widget", 99);
        assertNotEquals(current.fingerprint(), historical.fingerprint());

        HistoricalRevision resolved = authority.resolve(accepted.id());
        assertEquals(historical.model(), reconstructed(resolved).model());
        assertEquals(
                accepted.modelFingerprint(),
                FACTORY_VERIFIER.fingerprint(resolved.artifact().canonicalBytes()));
    }

    @Test
    void fingerprintMismatchIsRejectedWithoutPartialRevision() {
        FactoryModelVersion recorded = version("Widget", 5);
        FactoryModelVersion supplied = version("Widget", 6);
        ControlledRevision candidate = revision(
                id(10),
                recorded.fingerprint(),
                List.of(),
                Instant.parse("2026-09-01T18:00:00Z"));
        ControlledRevisionAuthority authority = authorityAt(ACCEPTED_AT);

        GovernanceHistoryException failure = assertThrows(
                GovernanceHistoryException.class,
                () -> authority.accept(candidate, artifact(supplied)));
        assertEquals(FINGERPRINT_MISMATCH, failure.code());
        assertTrue(authority.findById(candidate.id()).isEmpty());
        assertTrue(authority.revisions().isEmpty());
    }

    @Test
    void missingOrCorruptHistoricalArtifactFailsAndIsNotSilentlyRepaired()
            throws IOException {
        FactoryModelVersion version = version("Widget", 5);
        ControlledRevision candidate = revision(
                id(11),
                version.fingerprint(),
                List.of(),
                Instant.parse("2026-09-01T18:00:00Z"));
        ControlledRevisionAuthority authority = authorityAt(ACCEPTED_AT);
        ControlledRevision accepted = authority.accept(candidate, artifact(version));
        Path artifactFile = onlyRegularFile(store().resolve("artifacts"));

        Files.delete(artifactFile);
        GovernanceHistoryException missing = assertThrows(
                GovernanceHistoryException.class, () -> authority().resolve(accepted.id()));
        assertEquals(MISSING_ARTIFACT, missing.code());

        ControlledRevision later = revision(
                id(12),
                version.fingerprint(),
                List.of(accepted.id()),
                Instant.parse("2026-09-01T19:00:00Z"));
        GovernanceHistoryException noRepair = assertThrows(
                GovernanceHistoryException.class,
                () -> authority().accept(later, artifact(version)));
        assertEquals(MISSING_ARTIFACT, noRepair.code());
        assertTrue(authority().findById(later.id()).isEmpty());

        Files.write(artifactFile, new byte[] {1, 2, 3, 4});
        GovernanceHistoryException corrupt = assertThrows(
                GovernanceHistoryException.class, () -> authority().resolve(accepted.id()));
        assertEquals(STORAGE_INTEGRITY, corrupt.code());
    }

    @Test
    void corruptRevisionMetadataFailsAsStorageIntegrityFailure() throws IOException {
        FactoryModelVersion version = version("Widget", 5);
        ControlledRevision candidate = revision(
                id(13),
                version.fingerprint(),
                List.of(),
                Instant.parse("2026-09-01T18:00:00Z"));
        ControlledRevision accepted = authorityAt(ACCEPTED_AT).accept(candidate, artifact(version));

        Path revisionFile = onlyRegularFile(store().resolve("revisions"));
        Files.write(revisionFile, new byte[] {9, 8, 7});

        GovernanceHistoryException failure = assertThrows(
                GovernanceHistoryException.class, () -> authority().findById(accepted.id()));
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

        ControlledRevisionAuthority reopened = authority();
        ControlledRevision winner = reopened.findById(sharedId).orElseThrow();
        assertTrue(
                winner.modelFingerprint().equals(first.modelFingerprint())
                        || winner.modelFingerprint().equals(second.modelFingerprint()));
        assertEquals(ACCEPTED_AT, winner.provenance().recordedAt());
        assertEquals(RECORDER, winner.provenance().recorder());
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
        ControlledRevisionAuthority authority = authorityAt(ACCEPTED_AT);
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
        byte[] bytes = FactoryModelArtifact.encode(version);
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

    @Test
    void builtInStorageNeverAdoptsOrModifiesALocationItDidNotCreate() throws IOException {
        // A store written by an earlier layout, a foreign proving-store marker, other directory
        // content -- including names the store itself would use -- an empty directory it did not
        // create, or a regular file is refused as a whole: not reopened, partially reinterpreted,
        // or touched in any way, including by creating the store's lock file.
        Path reservedLock = tempDirectory.resolve("reserved-lock");
        Files.createDirectories(reservedLock);
        Files.write(reservedLock.resolve("authority.lock"), "foreign".getBytes(StandardCharsets.UTF_8));
        Path reservedPending = tempDirectory.resolve("reserved-pending");
        Files.createDirectories(reservedPending);
        Files.write(reservedPending.resolve(".pending-123.tmp"), "foreign".getBytes(StandardCharsets.UTF_8));
        Path emptyDirectory = tempDirectory.resolve("empty-directory");
        Files.createDirectories(emptyDirectory);
        Path earlierLayout = tempDirectory.resolve("earlier-layout");
        Files.createDirectories(earlierLayout.resolve("revisions"));
        Files.write(
                earlierLayout.resolve("revisions").resolve("stale.revision"),
                "arcogine-revision-store-v1\0stale".getBytes(StandardCharsets.US_ASCII));
        Path foreignMarker = tempDirectory.resolve("foreign-marker");
        Files.createDirectories(foreignMarker);
        Files.write(foreignMarker.resolve("proving-store"), new byte[] {1, 2, 3});
        Path foreignContent = tempDirectory.resolve("foreign-content");
        Files.createDirectories(foreignContent);
        Files.write(foreignContent.resolve("notes.txt"), "unrelated".getBytes(StandardCharsets.UTF_8));
        Path regularFile = tempDirectory.resolve("regular-file");
        Files.write(regularFile, "not a directory".getBytes(StandardCharsets.UTF_8));

        for (Path location : List.of(
                reservedLock, reservedPending, emptyDirectory, earlierLayout, foreignMarker, foreignContent, regularFile)) {
            Map<String, String> before = contents(tempDirectory);

            GovernanceHistoryException refused = assertThrows(
                    GovernanceHistoryException.class,
                    () -> BuiltInStorage.open(location, FACTORY_VERIFIER).controlledRevisions());
            assertEquals(UNSUPPORTED_STORE, refused.code(), location.toString());
            assertEquals(before, contents(tempDirectory), location.toString());
        }
        assertFalse(Files.exists(emptyDirectory.resolve("authority.lock")));
        assertFalse(Files.exists(foreignContent.resolve("authority.lock")));

        Path fresh = tempDirectory.resolve("fresh");
        BuiltInStorage.open(fresh, FACTORY_VERIFIER).controlledRevisions();
        assertTrue(Files.exists(fresh.resolve("proving-store")));
        assertTrue(BuiltInStorage.open(fresh, FACTORY_VERIFIER).controlledRevisions().revisions().isEmpty());
    }

    @Test
    void concurrentOpenersOfOneAbsentLocationShareOneInitializedStore() throws Exception {
        // Only the opener that creates the absent location initializes it; every other opener
        // reopens that store rather than adopting a partially initialized directory.
        Path fresh = tempDirectory.resolve("contended");
        int openers = 8;
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(openers);
        try {
            List<Future<ControlledRevisionAuthority>> opened = new ArrayList<>();
            for (int i = 0; i < openers; i++) {
                opened.add(executor.submit(() -> {
                    start.await();
                    return BuiltInStorage.open(fresh, FACTORY_VERIFIER).controlledRevisions();
                }));
            }
            start.countDown();
            for (Future<ControlledRevisionAuthority> authority : opened) {
                assertTrue(authority.get().revisions().isEmpty());
            }
        } finally {
            executor.shutdownNow();
        }

        assertEquals(
                Set.of("artifacts", "authority.lock", "proving-store", "revisions"),
                contents(fresh).keySet());
        String marker = new String(Files.readAllBytes(fresh.resolve("proving-store")), StandardCharsets.UTF_8);
        assertEquals(STORE_MARKER_PREFIX + FACTORY_VERIFIER.definitionBinding(), marker);
    }

    @Test
    void reopenLocksOnlyThroughTheLockFileItsInitializationCreated() throws IOException {
        // Even a location carrying this definition's marker is never given a new lock file: a
        // store whose lock has gone -- or a location swapped in after the marker was read -- is
        // refused unchanged rather than locked by creating content in it.
        BuiltInStorage.open(store(), FACTORY_VERIFIER).controlledRevisions();
        Files.delete(store().resolve("authority.lock"));
        Map<String, String> before = contents(tempDirectory);

        GovernanceHistoryException refused = assertThrows(
                GovernanceHistoryException.class,
                () -> BuiltInStorage.open(store(), FACTORY_VERIFIER).controlledRevisions());
        assertEquals(UNSUPPORTED_STORE, refused.code());
        assertEquals(before, contents(tempDirectory));
    }

    @Test
    void openedAuthorityNeverRecreatesLockOrUsesAReplacementLocation() throws IOException {
        ControlledRevisionAuthority opened =
                BuiltInStorage.open(store(), FACTORY_VERIFIER).controlledRevisions();
        Path lock = store().resolve("authority.lock");
        Files.delete(lock);
        Map<String, String> beforeMissingLock = contents(tempDirectory);

        GovernanceHistoryException missingLock = assertThrows(
                GovernanceHistoryException.class, opened::revisions);
        assertEquals(UNSUPPORTED_STORE, missingLock.code());
        assertEquals(beforeMissingLock, contents(tempDirectory));
        assertFalse(Files.exists(lock));

        Path replaceable = tempDirectory.resolve("replaceable-store");
        ControlledRevisionAuthority replaced =
                BuiltInStorage.open(replaceable, FACTORY_VERIFIER).controlledRevisions();
        Path original = tempDirectory.resolve("replaceable-store-original");
        Files.move(replaceable, original);
        Files.createDirectories(replaceable.resolve("revisions"));
        Files.createDirectories(replaceable.resolve("artifacts"));
        Files.write(
                replaceable.resolve("authority.lock"),
                "foreign-lock".getBytes(StandardCharsets.UTF_8));
        Files.write(
                replaceable.resolve("notes.txt"),
                "foreign".getBytes(StandardCharsets.UTF_8));
        Map<String, String> beforeReplacement = contents(tempDirectory);
        FactoryModelVersion version = version("Widget", 5);
        ControlledRevision candidate = revision(
                id(24),
                version.fingerprint(),
                List.of(),
                Instant.parse("2026-09-01T18:00:00Z"));

        GovernanceHistoryException replacement = assertThrows(
                GovernanceHistoryException.class,
                () -> replaced.accept(candidate, artifact(version)));
        assertEquals(UNSUPPORTED_STORE, replacement.code());
        assertEquals(beforeReplacement, contents(tempDirectory));
        assertFalse(Files.exists(replaceable.resolve("proving-store")));
    }

    @Test
    void storeWrittenUnderOneDefinitionBuildIsNeverReadUnderAnother() throws IOException {
        // The content fingerprint does not identify an exact development definition build. The
        // store binding does, and the store must fail closed before any earlier revision's records
        // or artifacts are read, resolved or changed.
        SemanticArtifactVerifier earlierDefinition = boundTo("earlier-definition-build");
        SemanticArtifactVerifier laterDefinition = boundTo("later-definition-build");
        FactoryModelVersion version = version("Widget", 5);
        ControlledRevision accepted = BuiltInStorage
                .open(store(), earlierDefinition, Clock.fixed(ACCEPTED_AT, ZoneOffset.UTC))
                .controlledRevisions()
                .accept(
                        revision(id(16), version.fingerprint(), List.of(), Instant.parse("2026-09-01T18:00:00Z")),
                        artifact(version));
        assertTrue(laterDefinition.supports(accepted.modelFingerprint()));
        Map<String, String> before = contents(store());

        GovernanceHistoryException refused = assertThrows(
                GovernanceHistoryException.class,
                () -> BuiltInStorage.open(store(), laterDefinition).controlledRevisions());
        assertEquals(UNSUPPORTED_STORE, refused.code());
        assertEquals(before, contents(store()));

        assertEquals(
                accepted,
                BuiltInStorage.open(store(), earlierDefinition).controlledRevisions()
                        .resolve(accepted.id())
                        .revision());
    }

    @Test
    void storeRecordsTheFactoryDefinitionBindingAndRequiresOne() throws IOException {
        Path fresh = tempDirectory.resolve("fresh-binding");
        BuiltInStorage.open(fresh, FACTORY_VERIFIER).controlledRevisions();

        String marker = new String(Files.readAllBytes(fresh.resolve("proving-store")), StandardCharsets.UTF_8);
        assertTrue(marker.endsWith(FACTORY_VERIFIER.definitionBinding()), marker);
        assertThrows(
                IllegalArgumentException.class,
                () -> BuiltInStorage.open(tempDirectory.resolve("unbound"), boundTo(" ")).controlledRevisions());
        assertThrows(IllegalArgumentException.class,
                () -> BuiltInStorage.open(tempDirectory.resolve("null-binding"), boundTo(null)));
    }

    @Test
    void malformedFingerprintTextCannotBeEncodedIntoHistory() throws IOException {
        FactoryModelVersion version = version("Widget", 5);
        ModelFingerprint malformed = new ModelFingerprint("factory-\uD800", "sha256",
                version.fingerprint().digest());
        SemanticArtifactVerifier verifier = new SemanticArtifactVerifier() {
            @Override
            public boolean supports(ModelFingerprint fingerprint) {
                return malformed.equals(fingerprint);
            }

            @Override
            public ModelFingerprint fingerprint(byte[] canonicalBytes) {
                return malformed;
            }

            @Override
            public String definitionBinding() {
                return "malformed-fingerprint-fixture";
            }
        };
        Path root = tempDirectory.resolve("malformed-fingerprint");
        ControlledRevisionAuthority authority = BuiltInStorage.open(root, verifier).controlledRevisions();
        Map<String, String> before = contents(root);
        GovernanceHistoryException rejected = assertThrows(GovernanceHistoryException.class,
                () -> authority.accept(revision(id(80), malformed, List.of(), ACCEPTED_AT),
                        new SemanticArtifact(malformed, FactoryModelArtifact.encode(version))));
        assertEquals(STORAGE_INTEGRITY, rejected.code());
        assertEquals(before, contents(root));
    }

    @Test
    void asciiRecorderTextResolvesUnchangedAfterReopen() {
        RevisionRecorder recorder = new RevisionRecorder("import-service", "operator-17");

        assertRecorderResolvesUnchangedAfterReopen(40, recorder);
    }

    @Test
    void validUnicodeRecorderTextResolvesUnchangedAfterReopen() throws IOException {
        // U+1F9EA is a supplementary character, stored as a surrogate pair in a Java string.
        RevisionRecorder recorder = new RevisionRecorder(
                "source-🧪-é", "subject-日本-🧪");

        assertRecorderResolvesUnchangedAfterReopen(41, recorder);

        // The private format is unchanged for valid Unicode: the text is stored as plain UTF-8.
        String record = new String(
                Files.readAllBytes(onlyRegularFile(store().resolve("revisions"))),
                StandardCharsets.ISO_8859_1);
        for (String text : List.of(recorder.source(), recorder.subject())) {
            assertTrue(record.contains(new String(
                    text.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1)));
        }
    }

    @Test
    void unpairedHighSurrogateInRecorderSourceIsRefusedWithoutMutation() throws IOException {
        assertRecorderRefusedWithoutMutation(
                42, new RevisionRecorder("\uD800", "operator-17"), "recorder source");
        assertRecorderRefusedWithoutMutation(
                43, new RevisionRecorder("trailing-high-\uD83E", "operator-17"), "recorder source");
    }

    @Test
    void unpairedLowSurrogateInRecorderSubjectIsRefusedWithoutMutation() throws IOException {
        assertRecorderRefusedWithoutMutation(
                44, new RevisionRecorder("governance-test", "\uDC00"), "recorder subject");
        assertRecorderRefusedWithoutMutation(
                45, new RevisionRecorder("governance-test", "reversed-\uDDEA\uD83E"), "recorder subject");
    }

    @Test
    void malformedDefinitionBindingIsRefusedBeforeAnyRootInitialization() throws IOException {
        List<String> malformedBindings = List.of("\uD800", "\uDC00", "build-\uD83E", "\uDDEA\uD83E-build");
        for (int index = 0; index < malformedBindings.size(); index++) {
            String malformed = malformedBindings.get(index);
            Path absent = tempDirectory.resolve("malformed-binding-" + index);
            Map<String, String> before = contents(tempDirectory);

            assertThrows(
                    IllegalArgumentException.class,
                    () -> BuiltInStorage.open(absent, boundTo(malformed)));
            assertFalse(Files.exists(absent));
            assertEquals(before, contents(tempDirectory));
        }
    }

    @Test
    void distinctDefinitionBindingsCannotAliasThroughLossyEncoding() throws IOException {
        // Each malformed binding used to encode to the same bytes as the substitute character, so a
        // store created under one silently reopened under the other.
        String[][] pairs = {{"?", "\uD800"}, {"?", "\uDC00"}, {"a?b", "a\uD800b"}, {"build-?", "build-\uD83E"}};
        for (int index = 0; index < pairs.length; index++) {
            String created = pairs[index][0];
            String lossy = pairs[index][1];
            Path root = tempDirectory.resolve("alias-" + index);
            BuiltInStorage.open(root, boundTo(created)).controlledRevisions();
            Map<String, String> before = contents(root);

            assertThrows(IllegalArgumentException.class, () -> BuiltInStorage.open(root, boundTo(lossy)));
            assertEquals(before, contents(root));
            assertTrue(BuiltInStorage.open(root, boundTo(created)).controlledRevisions().revisions().isEmpty());
        }
    }

    @Test
    void validUnicodeDefinitionBindingIsRecordedExactlyAndStaysExact() throws IOException {
        String binding = "definition-🧪-build";
        Path root = tempDirectory.resolve("unicode-binding");
        BuiltInStorage.open(root, boundTo(binding)).controlledRevisions();

        byte[] expected = (STORE_MARKER_PREFIX + binding).getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, Files.readAllBytes(root.resolve("proving-store")));
        assertTrue(BuiltInStorage.open(root, boundTo(binding)).controlledRevisions().revisions().isEmpty());
        Map<String, String> before = contents(root);
        GovernanceHistoryException different = assertThrows(
                GovernanceHistoryException.class,
                () -> BuiltInStorage.open(root, boundTo("definition-🧫-build")));
        assertEquals(UNSUPPORTED_STORE, different.code());
        assertEquals(before, contents(root));
    }

    @Test
    void rootWrittenBeforeStrictTextEncodingIsRefusedWithoutBeingReadOrChanged() throws IOException {
        // The earlier encoder stored an unpaired-surrogate binding or recorder as a literal "?", so
        // a marker written then cannot be told apart from one authored exactly -- "?" below is the
        // marker a "\uD800" binding used to leave. No verifier may adopt such a root, however its
        // binding compares, and nothing in it is read.
        List<String> bindings = List.of("?", "definition-build-1");
        for (int index = 0; index < bindings.size(); index++) {
            String binding = bindings.get(index);
            SemanticArtifactVerifier verifier = boundTo(binding);
            Path root = tempDirectory.resolve("earlier-encoding-" + index);
            FactoryModelVersion version = version("Widget", 5);
            ControlledRevisionAuthority opened = BuiltInStorage
                    .open(root, verifier, Clock.fixed(ACCEPTED_AT, ZoneOffset.UTC))
                    .controlledRevisions();
            opened.accept(revision(id(46 + index), version.fingerprint(), List.of(), ACCEPTED_AT), artifact(version));
            Files.write(
                    root.resolve("proving-store"),
                    (LEGACY_STORE_MARKER_PREFIX + binding).getBytes(StandardCharsets.UTF_8));
            Map<String, String> before = contents(root);

            GovernanceHistoryException refused = assertThrows(
                    GovernanceHistoryException.class, () -> BuiltInStorage.open(root, verifier));
            assertEquals(UNSUPPORTED_STORE, refused.code(), binding);
            assertTrue(refused.getMessage().contains("before strict text encoding"), refused.getMessage());
            assertEquals(before, contents(root), binding);

            // An authority opened earlier refuses the swapped-in root the same way.
            GovernanceHistoryException reuse = assertThrows(GovernanceHistoryException.class, opened::revisions);
            assertEquals(UNSUPPORTED_STORE, reuse.code(), binding);
            assertEquals(before, contents(root), binding);
        }
    }

    @Test
    void discardedPrefixArtifactsAreRefusedBeforeAnyStoreMutation() throws IOException {
        // Bytes laid out under a discarded ordinal prefix are never admitted as, or reinterpreted
        // into, current proving content.
        FactoryModelVersion version = version("Widget", 5);
        byte[] current = FactoryModelArtifact.encode(version);
        byte[] discardedPrefix = "arcogine.factory-model.v1\0".getBytes(StandardCharsets.US_ASCII);
        int currentPrefix = "arcogine.factory-model\0".getBytes(StandardCharsets.US_ASCII).length;
        byte[] discardedBytes = new byte[discardedPrefix.length + current.length - currentPrefix - 1];
        System.arraycopy(discardedPrefix, 0, discardedBytes, 0, discardedPrefix.length);
        System.arraycopy(
                current, currentPrefix + 1, discardedBytes, discardedPrefix.length, current.length - currentPrefix - 1);
        ModelFingerprint discardedFingerprint = new ModelFingerprint(
                "factory-model", "sha256", version.fingerprint().digest());
        ControlledRevision candidate = revision(
                id(15), discardedFingerprint, List.of(), Instant.parse("2026-09-01T18:00:00Z"));
        ControlledRevisionAuthority authority = authorityAt(ACCEPTED_AT);

        GovernanceHistoryException failure = assertThrows(
                GovernanceHistoryException.class,
                () -> authority.accept(candidate, new SemanticArtifact(discardedFingerprint, discardedBytes)));
        assertEquals(FINGERPRINT_MISMATCH, failure.code());
        assertTrue(authority.revisions().isEmpty());
        assertTrue(regularFiles(store().resolve("artifacts")).isEmpty());
    }

    @Test
    void unsupportedFingerprintNamespaceIsRefusedBeforeAnyStoreMutation() throws IOException {
        FactoryModelVersion version = version("Widget", 5);
        ModelFingerprint unsupported =
                new ModelFingerprint("other-model", "sha256", version.fingerprint().digest());
        ControlledRevision candidate = revision(
                id(17), unsupported, List.of(), Instant.parse("2026-09-01T18:00:00Z"));
        ControlledRevisionAuthority authority = authorityAt(ACCEPTED_AT);

        GovernanceHistoryException failure = assertThrows(
                GovernanceHistoryException.class,
                () -> authority.accept(candidate, new SemanticArtifact(
                        unsupported, FactoryModelArtifact.encode(version))));

        assertEquals(UNSUPPORTED_ARTIFACT_FINGERPRINT, failure.code());
        assertTrue(authority.revisions().isEmpty());
        assertTrue(regularFiles(store().resolve("artifacts")).isEmpty());
    }

    private GovernanceHistoryException.Code acceptAfterStart(
            CountDownLatch start, ControlledRevision revision, SemanticArtifact artifact)
            throws InterruptedException {
        start.await();
        try {
            authorityAt(ACCEPTED_AT).accept(revision, artifact);
            return null;
        } catch (GovernanceHistoryException e) {
            return e.code();
        }
    }

    /** The store location; absent until the first opener creates it. */
    private Path store() {
        return tempDirectory.resolve("store");
    }

    private ControlledRevisionAuthority authority() {
        return BuiltInStorage.open(store(), FACTORY_VERIFIER).controlledRevisions();
    }

    private ControlledRevisionAuthority authorityAt(Instant instant) {
        return BuiltInStorage.open(
                store(), FACTORY_VERIFIER, Clock.fixed(instant, ZoneOffset.UTC)).controlledRevisions();
    }

    private static FileArcogineStorage withFileOperations(
            Path root, FileArcogineStorage.FileOperations files) {
        return FileArcogineStorage.open(root, FACTORY_VERIFIER,
                Clock.fixed(ACCEPTED_AT, ZoneOffset.UTC),
                (source, target) -> Files.move(source, target, StandardCopyOption.ATOMIC_MOVE), files);
    }

    private void assertRecorderResolvesUnchangedAfterReopen(int suffix, RevisionRecorder recorder) {
        FactoryModelVersion version = version("Widget", 5);
        ControlledRevision accepted = authorityAt(ACCEPTED_AT).accept(
                revision(id(suffix), version.fingerprint(), List.of(), ACCEPTED_AT, recorder),
                artifact(version));

        assertEquals(recorder, accepted.provenance().recorder());
        ControlledRevisionAuthority reopened = authority();
        assertEquals(recorder, reopened.findById(accepted.id()).orElseThrow().provenance().recorder());
        HistoricalRevision resolved = reopened.resolve(accepted.id());
        assertEquals(accepted, resolved.revision());
        assertEquals(recorder, resolved.revision().provenance().recorder());
        assertEquals(List.of(accepted), reopened.revisions());
    }

    /** A malformed recorder is refused before any artifact or revision record is installed. */
    private void assertRecorderRefusedWithoutMutation(int suffix, RevisionRecorder recorder, String field)
            throws IOException {
        FactoryModelVersion version = version("Widget", 5);
        Path root = tempDirectory.resolve("refused-" + suffix);
        ControlledRevisionAuthority authority = BuiltInStorage
                .open(root, FACTORY_VERIFIER, Clock.fixed(ACCEPTED_AT, ZoneOffset.UTC))
                .controlledRevisions();
        Map<String, String> before = contents(root);
        ControlledRevision candidate =
                revision(id(suffix), version.fingerprint(), List.of(), ACCEPTED_AT, recorder);

        IllegalArgumentException refused = assertThrows(
                IllegalArgumentException.class, () -> authority.accept(candidate, artifact(version)));
        assertTrue(refused.getMessage().startsWith(field), refused.getMessage());
        assertEquals(before, contents(root));
        assertTrue(authority.findById(candidate.id()).isEmpty());
        assertTrue(authority.revisions().isEmpty());
        assertTrue(regularFiles(root.resolve("artifacts")).isEmpty());

        // No binding was left behind: the same revision ID and artifact are still acceptable.
        ControlledRevision retry = authority.accept(
                revision(candidate.id(), version.fingerprint(), List.of(), ACCEPTED_AT), artifact(version));
        assertEquals(RECORDER, retry.provenance().recorder());
        assertEquals(
                retry,
                BuiltInStorage.open(root, FACTORY_VERIFIER)
                        .controlledRevisions()
                        .resolve(candidate.id())
                        .revision());
    }

    private static ControlledRevision revision(
            ControlledRevisionId id,
            ModelFingerprint fingerprint,
            List<ControlledRevisionId> parents,
            Instant recordedAt) {
        return revision(id, fingerprint, parents, recordedAt, RECORDER);
    }

    private static ControlledRevision revision(
            ControlledRevisionId id,
            ModelFingerprint fingerprint,
            List<ControlledRevisionId> parents,
            Instant recordedAt,
            RevisionRecorder recorder) {
        return new ControlledRevision(
                id, fingerprint, parents, new RevisionProvenance(recordedAt, recorder));
    }

    private static ControlledRevisionId id(int suffix) {
        return ControlledRevisionId.parse(
                "00000000-0000-4000-8000-" + String.format("%012d", suffix));
    }

    private static SemanticArtifact artifact(FactoryModelVersion version) {
        return new SemanticArtifact(
                version.fingerprint(), FactoryModelArtifact.encode(version));
    }

    private static FactoryModelVersion reconstructed(HistoricalRevision revision) {
        return FactoryModelArtifact.decode(revision.artifact().canonicalBytes());
    }

    private static FactoryModelVersion version(String productName, long duration) {
        ConfiguredResource machine =
                new ConfiguredResource(new MachineId(1), "Mill", 1, 125.5, 2);
        OperationStepDefinition step = new OperationStepDefinition(
                1, "Machine", Set.of(new MachineId(1)), duration);
        OperationDefinition operation =
                new OperationDefinition(100, "Routing", List.of(step));
        ProductDefinition product =
                new ProductDefinition(new ProductId(10), productName, operation.id());
        return FactoryModelPublisher.publish(
                new FactoryModel(List.of(machine), List.of(operation), List.of(product)));
    }

    private static SemanticArtifactVerifier boundTo(String definitionBinding) {
        return new SemanticArtifactVerifier() {
            @Override
            public boolean supports(ModelFingerprint fingerprint) {
                return FACTORY_VERIFIER.supports(fingerprint);
            }

            @Override
            public ModelFingerprint fingerprint(byte[] canonicalBytes) {
                return FACTORY_VERIFIER.fingerprint(canonicalBytes);
            }

            @Override
            public String definitionBinding() {
                return definitionBinding;
            }
        };
    }

    private static Map<String, String> contents(Path root) throws IOException {
        Map<String, String> contents = new TreeMap<>();
        try (var paths = Files.walk(root)) {
            for (Path path : paths.filter(path -> !path.equals(root)).toList()) {
                contents.put(
                        root.relativize(path).toString(),
                        Files.isDirectory(path) ? "<directory>" : HexFormat.of().formatHex(Files.readAllBytes(path)));
            }
        }
        return contents;
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
