package com.arcogine.governance.requirement;

import java.util.Objects;

/**
 * Stable, Arcogine-owned identity for one {@link Requirement} lineage, independent of {@link
 * RequirementVersion}, {@code ModelFingerprint}, {@code ControlledRevisionId}, external source
 * identity, and any assertion identity.
 */
public record RequirementId(String value) {

    public RequirementId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
