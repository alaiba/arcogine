package com.arcogine.governance.change;

import java.util.Objects;

/**
 * Stable reference to one domain entity affected by a semantic change.
 *
 * <p>{@code entityId} must be the domain's own stable identity (e.g. a typed ID's string form),
 * never a collection index, display label, object identity, or transient runtime ID. {@code
 * entityType} names the domain entity kind (e.g. {@code "factory.resource"}); it is a coarse
 * classification, not a full type-system reference. {@code label} is presentation-only and never
 * participates in equality/ordering, so relabeling an entity does not change its identity.
 */
public record ChangedEntityRef(String entityType, String entityId, String label) {

    public ChangedEntityRef {
        Objects.requireNonNull(entityType, "entityType");
        Objects.requireNonNull(entityId, "entityId");
        if (entityType.isBlank()) {
            throw new IllegalArgumentException("entityType must not be blank");
        }
        if (entityId.isBlank()) {
            throw new IllegalArgumentException("entityId must not be blank");
        }
        label = label == null ? "" : label;
    }

    /** Deterministic sort/equality key derived from stable identity only, ignoring the label. */
    String identityKey() {
        return entityType + "#" + entityId;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ChangedEntityRef ref
                && entityType.equals(ref.entityType)
                && entityId.equals(ref.entityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityType, entityId);
    }
}
