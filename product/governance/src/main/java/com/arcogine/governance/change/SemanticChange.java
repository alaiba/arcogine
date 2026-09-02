package com.arcogine.governance.change;

import java.util.Objects;

/**
 * One classified, domain-attributed semantic change against a stable entity.
 *
 * <p>{@code detail} is a human-readable explanation only (e.g. "capacityLiters: 10 -> 12"); it is
 * explicitly not the primary semantic model and must never be the only way to identify what
 * changed -- {@code kind} and {@code entity} carry that.
 */
public record SemanticChange(SemanticChangeKind kind, ChangedEntityRef entity, String detail) {

    public SemanticChange {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(entity, "entity");
        detail = detail == null ? "" : detail;
    }
}
