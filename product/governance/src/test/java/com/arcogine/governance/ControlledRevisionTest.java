package com.arcogine.governance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arcogine.types.ControlledRevisionId;
import com.arcogine.types.ModelFingerprint;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ControlledRevisionTest {

    private static final ModelFingerprint FINGERPRINT_ONE =
            new ModelFingerprint("factory-model", "v1", "sha256", "a".repeat(64));
    private static final ModelFingerprint FINGERPRINT_TWO =
            new ModelFingerprint("factory-model", "v1", "sha256", "b".repeat(64));
    private static final RevisionProvenance PROVENANCE = new RevisionProvenance(
            Instant.parse("2026-08-28T12:00:00Z"), new RevisionRecorder("test", "governance-test"));

    @Test
    void acceptsRootAndSingleParentRevisions() {
        ControlledRevision root = revision(FINGERPRINT_ONE, List.of());
        ControlledRevision child = revision(FINGERPRINT_TWO, List.of(root.id()));

        assertEquals(List.of(), root.parentRevisionIds());
        assertEquals(List.of(root.id()), child.parentRevisionIds());
    }

    @Test
    void rejectsMultipleParentsAndSelfParent() {
        ControlledRevisionId first = ControlledRevisionId.generate();
        ControlledRevisionId second = ControlledRevisionId.generate();
        ControlledRevisionId revisionId = ControlledRevisionId.generate();

        assertThrows(IllegalArgumentException.class,
                () -> new ControlledRevision(revisionId, FINGERPRINT_ONE, List.of(first, second), PROVENANCE));
        assertThrows(IllegalArgumentException.class,
                () -> new ControlledRevision(revisionId, FINGERPRINT_ONE, List.of(revisionId), PROVENANCE));
    }

    @Test
    void parentCollectionIsDefensivelyCopiedAndUnmodifiable() {
        ControlledRevisionId parent = ControlledRevisionId.generate();
        List<ControlledRevisionId> parents = new ArrayList<>(List.of(parent));
        ControlledRevision revision = revision(FINGERPRINT_ONE, parents);

        parents.clear();

        assertEquals(List.of(parent), revision.parentRevisionIds());
        assertThrows(UnsupportedOperationException.class,
                () -> revision.parentRevisionIds().add(ControlledRevisionId.generate()));
    }

    @Test
    void requiresFingerprintProvenanceAndRecorderText() {
        ControlledRevisionId id = ControlledRevisionId.generate();

        assertThrows(NullPointerException.class, () -> new ControlledRevision(null, FINGERPRINT_ONE, List.of(), PROVENANCE));
        assertThrows(NullPointerException.class, () -> new ControlledRevision(id, null, List.of(), PROVENANCE));
        assertThrows(NullPointerException.class, () -> new ControlledRevision(id, FINGERPRINT_ONE, null, PROVENANCE));
        assertThrows(NullPointerException.class, () -> new ControlledRevision(id, FINGERPRINT_ONE, List.of(), null));
        assertThrows(NullPointerException.class, () -> new RevisionProvenance(Instant.now(), null));
        assertThrows(NullPointerException.class, () -> new RevisionProvenance(null, new RevisionRecorder("source", "subject")));
        assertThrows(NullPointerException.class, () -> new RevisionRecorder(null, "subject"));
        assertThrows(NullPointerException.class, () -> new RevisionRecorder("source", null));
        assertThrows(IllegalArgumentException.class, () -> new RevisionRecorder(" ", "subject"));
        assertThrows(IllegalArgumentException.class, () -> new RevisionRecorder("source", ""));
    }

    @Test
    void supportsRepeatedFingerprintAcrossDistinctHistoricalRevisions() {
        ControlledRevision revisionA = revision(FINGERPRINT_ONE, List.of());
        ControlledRevision revisionB = revision(FINGERPRINT_TWO, List.of(revisionA.id()));
        ControlledRevision revisionC = revision(FINGERPRINT_ONE, List.of(revisionB.id()));

        assertEquals(revisionA.modelFingerprint(), revisionC.modelFingerprint());
        assertNotEquals(revisionA.id(), revisionC.id());
        assertEquals(List.of(revisionB.id()), revisionC.parentRevisionIds());
    }

    @Test
    void minimumContractHasNoWorkflowOrArtifactFields() {
        assertEquals(List.of("id", "modelFingerprint", "parentRevisionIds", "provenance"),
                List.of(ControlledRevision.class.getRecordComponents()).stream()
                        .map(component -> component.getName())
                        .toList());
    }

    private static ControlledRevision revision(ModelFingerprint fingerprint, List<ControlledRevisionId> parents) {
        return new ControlledRevision(ControlledRevisionId.generate(), fingerprint, parents, PROVENANCE);
    }
}
