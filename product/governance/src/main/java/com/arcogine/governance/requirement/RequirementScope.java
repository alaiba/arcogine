package com.arcogine.governance.requirement;

import com.arcogine.governance.change.ChangedEntityRef;
import com.arcogine.governance.change.ImpactScope;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Deterministic, explicit business/entity applicability of one {@link Requirement}.
 *
 * <p>This is the G3 half of the seam G2 deliberately left open: {@code ChangeSet ->
 * ImpactScope -> ChangedEntityRef} on one side, {@code Requirement -> RequirementScope ->
 * ChangedEntityRef} on the other. {@link #intersects(ImpactScope)} reuses {@link
 * ImpactScope#intersects(Set)} directly rather than duplicating an entity-reference abstraction.
 *
 * <p>Scope is a plain, deterministically ordered set of stable {@link ChangedEntityRef} values --
 * intentionally not a query language or expression DSL, per G3 non-goals. An empty scope never
 * matches any impact.
 */
public final class RequirementScope {

    private static final RequirementScope EMPTY = new RequirementScope(List.of());

    private final List<ChangedEntityRef> entities;

    private RequirementScope(List<ChangedEntityRef> entities) {
        this.entities = entities;
    }

    public static RequirementScope of(ChangedEntityRef... entities) {
        return of(List.of(entities));
    }

    public static RequirementScope of(Collection<ChangedEntityRef> entities) {
        Objects.requireNonNull(entities, "entities");
        if (entities.isEmpty()) {
            return EMPTY;
        }
        Set<ChangedEntityRef> unique = new LinkedHashSet<>(entities);
        List<ChangedEntityRef> ordered = new ArrayList<>(unique);
        ordered.sort(
                Comparator.comparing(ChangedEntityRef::entityType).thenComparing(ChangedEntityRef::entityId));
        return new RequirementScope(List.copyOf(ordered));
    }

    public static RequirementScope empty() {
        return EMPTY;
    }

    /** Stable entities this requirement applies to, deterministically ordered. */
    public List<ChangedEntityRef> entities() {
        return entities;
    }

    public boolean isEmpty() {
        return entities.isEmpty();
    }

    /** Whether this scope shares any entity with a {@link ImpactScope} produced by a G2 {@code ChangeSet}. */
    public boolean intersects(ImpactScope impactScope) {
        Objects.requireNonNull(impactScope, "impactScope");
        if (entities.isEmpty()) {
            return false;
        }
        return impactScope.intersects(new LinkedHashSet<>(entities));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof RequirementScope scope && entities.equals(scope.entities);
    }

    @Override
    public int hashCode() {
        return entities.hashCode();
    }

    @Override
    public String toString() {
        return "RequirementScope" + entities;
    }
}
