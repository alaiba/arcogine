package com.arcogine.governance;

import java.util.Objects;

/** One authoritative revision record together with its exact immutable semantic artifact. */
public record HistoricalRevision(ControlledRevision revision, SemanticArtifact artifact) {

    public HistoricalRevision {
        Objects.requireNonNull(revision, "revision");
        Objects.requireNonNull(artifact, "artifact");
        if (!revision.modelFingerprint().equals(artifact.fingerprint())) {
            throw new IllegalArgumentException("revision fingerprint must equal artifact fingerprint");
        }
    }
}
