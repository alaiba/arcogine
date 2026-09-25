package com.arcogine.governance;

import com.arcogine.types.ControlledRevisionId;
import java.util.List;
import java.util.Optional;

/**
 * Authoritative acceptance and historical-resolution boundary for controlled revisions.
 *
 * <p>Within one authority, acceptance fixes a revision's identity, fingerprint binding, lineage and
 * recording provenance, and resolution returns the exact accepted artifact. Whether accepted
 * records are retained commitments depends on the authority's declared custody: the current
 * implementation, {@link FileControlledRevisionAuthority}, is a disposable proving store over
 * work-in-progress definitions. No retained authority exists while every semantic contract is work
 * in progress; one is introduced only with an explicit promotion and admits only promoted
 * definitions.
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
