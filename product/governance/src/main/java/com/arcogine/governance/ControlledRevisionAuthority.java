package com.arcogine.governance;

import com.arcogine.types.ControlledRevisionId;
import java.util.List;
import java.util.Optional;

/** Authoritative acceptance and durable historical-resolution boundary for controlled revisions. */
public interface ControlledRevisionAuthority {

    void accept(ControlledRevision revision, SemanticArtifact artifact);

    Optional<ControlledRevision> findById(ControlledRevisionId id);

    HistoricalRevision resolve(ControlledRevisionId id);

    List<ControlledRevision> revisions();
}
