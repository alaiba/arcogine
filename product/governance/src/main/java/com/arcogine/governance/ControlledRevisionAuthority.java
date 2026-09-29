package com.arcogine.governance;

import com.arcogine.types.ControlledRevisionId;
import java.util.List;
import java.util.Optional;

/**
 * Authoritative acceptance and historical-resolution boundary for controlled revisions.
 *
 * <p>Within one authority, acceptance fixes a revision's identity, fingerprint binding, lineage and
 * recording provenance, and resolution returns the exact accepted artifact. Storage realizes this
 * semantic port without owning the meaning of revision identity or promising that a development
 * definition remains interpretable after it changes.
 */
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
