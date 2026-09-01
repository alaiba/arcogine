package com.arcogine.governance;

import com.arcogine.types.ControlledRevisionId;
import com.arcogine.types.ModelFingerprint;
import java.util.List;
import java.util.Objects;

/** Immutable historical revision identity, semantic content reference, lineage, and provenance. */
public record ControlledRevision(
        ControlledRevisionId id,
        ModelFingerprint modelFingerprint,
        List<ControlledRevisionId> parentRevisionIds,
        RevisionProvenance provenance) {

    public ControlledRevision {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(modelFingerprint, "modelFingerprint");
        Objects.requireNonNull(parentRevisionIds, "parentRevisionIds");
        if (parentRevisionIds.size() > 1) {
            throw new IllegalArgumentException("a controlled revision may have at most one parent");
        }
        List<ControlledRevisionId> copiedParents = List.copyOf(parentRevisionIds);
        if (copiedParents.contains(id)) {
            throw new IllegalArgumentException("a controlled revision cannot be its own parent");
        }
        parentRevisionIds = copiedParents;
        Objects.requireNonNull(provenance, "provenance");
    }
}
