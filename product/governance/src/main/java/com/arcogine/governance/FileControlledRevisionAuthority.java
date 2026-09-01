package com.arcogine.governance;

import static com.arcogine.governance.GovernanceHistoryException.Code.DUPLICATE_REVISION_ID;
import static com.arcogine.governance.GovernanceHistoryException.Code.FINGERPRINT_MISMATCH;
import static com.arcogine.governance.GovernanceHistoryException.Code.MISSING_ARTIFACT;
import static com.arcogine.governance.GovernanceHistoryException.Code.MISSING_PARENT;
import static com.arcogine.governance.GovernanceHistoryException.Code.MISSING_REVISION;
import static com.arcogine.governance.GovernanceHistoryException.Code.STORAGE_INTEGRITY;
import static com.arcogine.governance.GovernanceHistoryException.Code.UNSUPPORTED_ARTIFACT_POLICY;

import com.arcogine.types.ControlledRevisionId;
import com.arcogine.types.ModelFingerprint;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Durable append-only controlled-revision authority backed by one filesystem directory.
 *
 * <p>Revision records and semantic artifacts use private versioned binary files. Semantic
 * artifacts are deduplicated by a physical key derived from the complete {@link ModelFingerprint};
 * the fingerprint remains the semantic identity and the key never escapes this adapter.
 */
public final class FileControlledRevisionAuthority implements ControlledRevisionAuthority {

    private static final byte[] REVISION_MAGIC =
            "arcogine-revision-store-v1\0".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] ARTIFACT_MAGIC =
            "arcogine-semantic-artifact-v1\0".getBytes(StandardCharsets.US_ASCII);
    private static final Object PROCESS_LOCK = new Object();

    private final Path revisionsDirectory;
    private final Path artifactsDirectory;
    private final Path lockFile;
    private final SemanticArtifactVerifier verifier;
    private final Clock clock;

    public FileControlledRevisionAuthority(Path root, SemanticArtifactVerifier verifier) {
        this(root, verifier, Clock.systemUTC());
    }

    FileControlledRevisionAuthority(Path root, SemanticArtifactVerifier verifier, Clock clock) {
        Path authorityRoot = Objects.requireNonNull(root, "root").toAbsolutePath().normalize();
        this.verifier = Objects.requireNonNull(verifier, "verifier");
        this.clock = Objects.requireNonNull(clock, "clock");
        revisionsDirectory = authorityRoot.resolve("revisions");
        artifactsDirectory = authorityRoot.resolve("artifacts");
        lockFile = authorityRoot.resolve("authority.lock");
        try {
            Files.createDirectories(revisionsDirectory);
            Files.createDirectories(artifactsDirectory);
        } catch (IOException e) {
            throw storageFailure("cannot initialize controlled revision authority", e);
        }
    }

    @Override
    public ControlledRevision accept(ControlledRevision candidate, SemanticArtifact artifact) {
        Objects.requireNonNull(candidate, "candidate");
        Objects.requireNonNull(artifact, "artifact");
        return withExclusiveLock(() -> acceptLocked(candidate, artifact));
    }

    @Override
    public Optional<ControlledRevision> findById(ControlledRevisionId id) {
        Objects.requireNonNull(id, "id");
        return withExclusiveLock(() -> {
            Path path = revisionPath(id);
            return Files.exists(path) ? Optional.of(readRevision(path, id)) : Optional.empty();
        });
    }

    @Override
    public HistoricalRevision resolve(ControlledRevisionId id) {
        Objects.requireNonNull(id, "id");
        return withExclusiveLock(() -> resolveLocked(id));
    }

    @Override
    public List<ControlledRevision> revisions() {
        return withExclusiveLock(this::revisionsLocked);
    }

    private List<ControlledRevision> revisionsLocked() {
        List<ControlledRevision> revisions = new ArrayList<>();
        try (var paths = Files.list(revisionsDirectory)) {
            for (Path path : paths.filter(Files::isRegularFile).sorted().toList()) {
                if (!path.getFileName().toString().endsWith(".revision")) {
                    continue;
                }
                String value = path.getFileName().toString().replaceFirst("\\.revision$", "");
                ControlledRevisionId id;
                try {
                    id = ControlledRevisionId.parse(value);
                } catch (RuntimeException e) {
                    throw storageFailure("invalid revision filename: " + path.getFileName(), e);
                }
                revisions.add(readRevision(path, id));
            }
        } catch (IOException e) {
            throw storageFailure("cannot enumerate revision history", e);
        }
        return revisions.stream()
                .sorted(Comparator.comparing(revision -> revision.id().toString()))
                .toList();
    }

    private ControlledRevision acceptLocked(
            ControlledRevision candidate, SemanticArtifact artifact) {
        Path revisionPath = revisionPath(candidate.id());
        if (Files.exists(revisionPath)) {
            ControlledRevision existing = readRevision(revisionPath, candidate.id());
            String qualifier =
                    sameCandidateBinding(existing, candidate)
                            ? "already accepted"
                            : "bound to different immutable content";
            throw new GovernanceHistoryException(
                    DUPLICATE_REVISION_ID,
                    "controlled revision ID " + candidate.id() + " is " + qualifier);
        }
        if (!candidate.modelFingerprint().equals(artifact.fingerprint())) {
            throw new GovernanceHistoryException(
                    FINGERPRINT_MISMATCH,
                    "revision fingerprint does not match supplied semantic artifact fingerprint");
        }
        verifyArtifact(artifact, false);
        for (ControlledRevisionId parentId : candidate.parentRevisionIds()) {
            if (!Files.exists(revisionPath(parentId))) {
                throw new GovernanceHistoryException(
                        MISSING_PARENT, "parent revision does not exist: " + parentId);
            }
            resolveLocked(parentId);
        }

        Path artifactPath = artifactPath(artifact.fingerprint());
        boolean artifactCreated = false;
        if (Files.exists(artifactPath)) {
            SemanticArtifact existingArtifact = readArtifact(artifactPath, artifact.fingerprint());
            if (!existingArtifact.equals(artifact)) {
                throw new GovernanceHistoryException(
                        STORAGE_INTEGRITY,
                        "existing artifact binding differs for " + artifact.fingerprint());
            }
        } else {
            ensureNoRevisionReferencesMissingArtifact(artifact.fingerprint());
            writeAtomic(artifactPath, encodeArtifact(artifact));
            artifactCreated = true;
        }

        try {
            ControlledRevision accepted = acceptedRevision(candidate);
            writeAtomic(revisionPath, encodeRevision(accepted));
            return accepted;
        } catch (RuntimeException e) {
            if (artifactCreated) {
                try {
                    Files.deleteIfExists(artifactPath);
                } catch (IOException cleanupFailure) {
                    e.addSuppressed(cleanupFailure);
                }
            }
            throw e;
        }
    }

    private ControlledRevision acceptedRevision(ControlledRevision candidate) {
        return new ControlledRevision(
                candidate.id(),
                candidate.modelFingerprint(),
                candidate.parentRevisionIds(),
                new RevisionProvenance(clock.instant(), candidate.provenance().recorder()));
    }

    private static boolean sameCandidateBinding(
            ControlledRevision existing, ControlledRevision candidate) {
        return existing.id().equals(candidate.id())
                && existing.modelFingerprint().equals(candidate.modelFingerprint())
                && existing.parentRevisionIds().equals(candidate.parentRevisionIds())
                && existing.provenance().recorder().equals(candidate.provenance().recorder());
    }

    private HistoricalRevision resolveLocked(ControlledRevisionId id) {
        Path path = revisionPath(id);
        if (!Files.exists(path)) {
            throw new GovernanceHistoryException(
                    MISSING_REVISION, "controlled revision does not exist: " + id);
        }
        ControlledRevision revision = readRevision(path, id);
        Path artifactPath = artifactPath(revision.modelFingerprint());
        if (!Files.exists(artifactPath)) {
            throw new GovernanceHistoryException(
                    MISSING_ARTIFACT, "semantic artifact is missing for revision " + id);
        }
        SemanticArtifact artifact = readArtifact(artifactPath, revision.modelFingerprint());
        verifyArtifact(artifact, true);
        return new HistoricalRevision(revision, artifact);
    }

    private void ensureNoRevisionReferencesMissingArtifact(ModelFingerprint fingerprint) {
        for (ControlledRevision revision : revisionsLocked()) {
            if (revision.modelFingerprint().equals(fingerprint)) {
                throw new GovernanceHistoryException(
                        MISSING_ARTIFACT,
                        "existing revision "
                                + revision.id()
                                + " references missing artifact "
                                + fingerprint);
            }
        }
    }

    private void verifyArtifact(SemanticArtifact artifact, boolean storedArtifact) {
        if (!verifier.supports(artifact.fingerprint())) {
            throw new GovernanceHistoryException(
                    UNSUPPORTED_ARTIFACT_POLICY,
                    "unsupported semantic artifact policy: " + artifact.fingerprint());
        }
        ModelFingerprint computed;
        try {
            computed = verifier.fingerprint(artifact.canonicalBytes());
        } catch (RuntimeException e) {
            GovernanceHistoryException.Code code =
                    storedArtifact ? STORAGE_INTEGRITY : FINGERPRINT_MISMATCH;
            throw new GovernanceHistoryException(code, "semantic artifact cannot be verified", e);
        }
        if (!artifact.fingerprint().equals(computed)) {
            throw new GovernanceHistoryException(
                    FINGERPRINT_MISMATCH,
                    "semantic artifact fingerprint mismatch: recorded "
                            + artifact.fingerprint()
                            + ", computed "
                            + computed);
        }
    }

    private ControlledRevision readRevision(Path path, ControlledRevisionId expectedId) {
        try {
            byte[] encoded = Files.readAllBytes(path);
            try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(encoded))) {
                requireMagic(input, REVISION_MAGIC);
                ControlledRevisionId id = ControlledRevisionId.parse(readString(input));
                if (!id.equals(expectedId)) {
                    throw new IOException("revision record ID does not match filename");
                }
                ModelFingerprint fingerprint = readFingerprint(input);
                int parentCount = input.readInt();
                if (parentCount < 0 || parentCount > 1) {
                    throw new IOException("invalid parent count: " + parentCount);
                }
                List<ControlledRevisionId> parents = new ArrayList<>(parentCount);
                for (int index = 0; index < parentCount; index++) {
                    parents.add(ControlledRevisionId.parse(readString(input)));
                }
                Instant recordedAt = Instant.ofEpochSecond(input.readLong(), input.readInt());
                RevisionRecorder recorder =
                        new RevisionRecorder(readString(input), readString(input));
                requireEnd(input);
                return new ControlledRevision(
                        id, fingerprint, parents, new RevisionProvenance(recordedAt, recorder));
            }
        } catch (IOException | RuntimeException e) {
            if (e instanceof GovernanceHistoryException governanceFailure) {
                throw governanceFailure;
            }
            throw storageFailure("cannot decode revision record " + expectedId, e);
        }
    }

    private SemanticArtifact readArtifact(Path path, ModelFingerprint expectedFingerprint) {
        try {
            byte[] encoded = Files.readAllBytes(path);
            try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(encoded))) {
                requireMagic(input, ARTIFACT_MAGIC);
                ModelFingerprint fingerprint = readFingerprint(input);
                if (!fingerprint.equals(expectedFingerprint)) {
                    throw new IOException(
                            "artifact fingerprint metadata does not match revision fingerprint");
                }
                long length = input.readLong();
                if (length < 0 || length > Integer.MAX_VALUE) {
                    throw new IOException("invalid semantic artifact length: " + length);
                }
                byte[] canonicalBytes = input.readNBytes((int) length);
                if (canonicalBytes.length != (int) length) {
                    throw new EOFException("truncated semantic artifact");
                }
                requireEnd(input);
                return new SemanticArtifact(fingerprint, canonicalBytes);
            }
        } catch (IOException | RuntimeException e) {
            if (e instanceof GovernanceHistoryException governanceFailure) {
                throw governanceFailure;
            }
            throw storageFailure("cannot decode semantic artifact " + expectedFingerprint, e);
        }
    }

    private byte[] encodeRevision(ControlledRevision revision) {
        return encode(output -> {
            output.write(REVISION_MAGIC);
            writeString(output, revision.id().toString());
            writeFingerprint(output, revision.modelFingerprint());
            output.writeInt(revision.parentRevisionIds().size());
            for (ControlledRevisionId parentId : revision.parentRevisionIds()) {
                writeString(output, parentId.toString());
            }
            output.writeLong(revision.provenance().recordedAt().getEpochSecond());
            output.writeInt(revision.provenance().recordedAt().getNano());
            writeString(output, revision.provenance().recorder().source());
            writeString(output, revision.provenance().recorder().subject());
        });
    }

    private byte[] encodeArtifact(SemanticArtifact artifact) {
        return encode(output -> {
            output.write(ARTIFACT_MAGIC);
            writeFingerprint(output, artifact.fingerprint());
            byte[] canonicalBytes = artifact.canonicalBytes();
            output.writeLong(canonicalBytes.length);
            output.write(canonicalBytes);
        });
    }

    private byte[] encode(Encoder encoder) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream output = new DataOutputStream(bytes)) {
                encoder.write(output);
            }
            return bytes.toByteArray();
        } catch (IOException e) {
            throw storageFailure("cannot encode controlled revision history", e);
        }
    }

    private void writeAtomic(Path target, byte[] bytes) {
        Path temporary = null;
        try {
            temporary = Files.createTempFile(target.getParent(), ".pending-", ".tmp");
            try (FileChannel channel = FileChannel.open(
                    temporary, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                while (buffer.hasRemaining()) {
                    channel.write(buffer);
                }
                channel.force(true);
            }
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
                temporary = null;
            } catch (AtomicMoveNotSupportedException e) {
                throw storageFailure("filesystem does not support atomic authority writes", e);
            }
        } catch (IOException e) {
            throw storageFailure("cannot atomically persist controlled revision history", e);
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException ignored) {
                    // A failed temporary-file cleanup never changes authoritative history.
                }
            }
        }
    }

    private Path revisionPath(ControlledRevisionId id) {
        return revisionsDirectory.resolve(id + ".revision");
    }

    private Path artifactPath(ModelFingerprint fingerprint) {
        return artifactsDirectory.resolve(artifactStorageKey(fingerprint) + ".artifact");
    }

    private String artifactStorageKey(ModelFingerprint fingerprint) {
        byte[] identity = encode(output -> writeFingerprint(output, fingerprint));
        try {
            return HexFormat.of()
                    .formatHex(MessageDigest.getInstance("SHA-256").digest(identity));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private <T> T withExclusiveLock(CheckedSupplier<T> action) {
        synchronized (PROCESS_LOCK) {
            try (FileChannel channel = FileChannel.open(
                            lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    FileLock lock = channel.lock()) {
                if (!lock.isValid()) {
                    throw new IllegalStateException("controlled revision authority lock is invalid");
                }
                return action.get();
            } catch (GovernanceHistoryException e) {
                throw e;
            } catch (Exception e) {
                throw storageFailure("controlled revision authority operation failed", e);
            }
        }
    }

    private static void writeFingerprint(DataOutputStream output, ModelFingerprint fingerprint)
            throws IOException {
        writeString(output, fingerprint.namespace());
        writeString(output, fingerprint.policyVersion());
        writeString(output, fingerprint.algorithm());
        writeString(output, fingerprint.digest());
    }

    private static ModelFingerprint readFingerprint(DataInputStream input) throws IOException {
        return new ModelFingerprint(
                readString(input), readString(input), readString(input), readString(input));
    }

    private static void writeString(DataOutputStream output, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        output.writeInt(bytes.length);
        output.write(bytes);
    }

    private static String readString(DataInputStream input) throws IOException {
        int length = input.readInt();
        if (length < 0) {
            throw new IOException("negative string length");
        }
        byte[] bytes = input.readNBytes(length);
        if (bytes.length != length) {
            throw new EOFException("truncated string");
        }
        try {
            CharBuffer decoded = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes));
            return decoded.toString();
        } catch (CharacterCodingException e) {
            throw new IOException("invalid UTF-8 string", e);
        }
    }

    private static void requireMagic(DataInputStream input, byte[] expected) throws IOException {
        byte[] actual = input.readNBytes(expected.length);
        if (!Arrays.equals(expected, actual)) {
            throw new IOException("unsupported or corrupt storage record");
        }
    }

    private static void requireEnd(DataInputStream input) throws IOException {
        if (input.read() != -1) {
            throw new IOException("trailing bytes in storage record");
        }
    }

    private static GovernanceHistoryException storageFailure(String message, Throwable cause) {
        return new GovernanceHistoryException(STORAGE_INTEGRITY, message, cause);
    }

    @FunctionalInterface
    private interface CheckedSupplier<T> {
        T get() throws Exception;
    }

    @FunctionalInterface
    private interface Encoder {
        void write(DataOutputStream output) throws IOException;
    }
}
