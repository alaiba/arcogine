package com.arcogine.governance;

import com.arcogine.types.ModelFingerprint;
import java.util.Arrays;
import java.util.Objects;

/** Immutable canonical semantic artifact retained for exact historical reconstruction. */
public record SemanticArtifact(ModelFingerprint fingerprint, byte[] canonicalBytes) {

    public SemanticArtifact {
        Objects.requireNonNull(fingerprint, "fingerprint");
        Objects.requireNonNull(canonicalBytes, "canonicalBytes");
        canonicalBytes = canonicalBytes.clone();
    }

    @Override
    public byte[] canonicalBytes() {
        return canonicalBytes.clone();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SemanticArtifact artifact
                && fingerprint.equals(artifact.fingerprint)
                && Arrays.equals(canonicalBytes, artifact.canonicalBytes);
    }

    @Override
    public int hashCode() {
        return 31 * fingerprint.hashCode() + Arrays.hashCode(canonicalBytes);
    }
}
