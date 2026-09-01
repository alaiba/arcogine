package com.arcogine.governance;

import com.arcogine.types.ControlledRevisionId;
import java.util.List;
import java.util.Optional;

/** Authoritative acceptance and durable historical-resolution boundary for controlled revisions. */
public interface ControlledRevisionAuthority {

    /**
     * Accepts candidate revision content and returns the immutable authoritative record.
     *
     * <p>The candidate's {@link RevisionProvenance#recordedAt()} value is not authoritative input.
     * The revision authority establishes the accepted record's {@code recordedAt} at its commit
     * boundary while preserving the candidate recorder, identity, fingerprint, and lineage.
     */
    ControlledRevision accept(ControlledRevision candidate, SemanticArtifact artifact);

    Optional<ControlledRevision> findById(ControlledRevisionId id);

    HistoricalRevision resolve(ControlledRevisionId id);

    List<ControlledRevision> revisions();
}
