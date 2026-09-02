package com.arcogine.governance.change;

import java.util.Objects;
import java.util.Optional;

/**
 * Why a {@link ChangeSet} exists: who/what produced it, the stated reason, and an optional
 * external change-request association. None of these fields are identity.
 */
public record ChangeProvenance(String source, String reason, ExternalChangeReference externalReference) {

    public ChangeProvenance {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(reason, "reason");
        if (source.isBlank()) {
            throw new IllegalArgumentException("source must not be blank");
        }
        if (reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be blank");
        }
    }

    public static ChangeProvenance of(String source, String reason) {
        return new ChangeProvenance(source, reason, null);
    }

    public static ChangeProvenance of(
            String source, String reason, ExternalChangeReference externalReference) {
        return new ChangeProvenance(source, reason, externalReference);
    }

    public Optional<ExternalChangeReference> externalReferenceOptional() {
        return Optional.ofNullable(externalReference);
    }
}
