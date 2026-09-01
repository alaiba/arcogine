package com.arcogine.governance.change;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The minimum useful G2 impact representation: the set of stable domain entities a {@link
 * ChangeSet} touched, in deterministic order.
 *
 * <p>This is deliberately not a requirement-registry match. It is the seam a future G3 requirement
 * scope can intersect against ({@link #intersects(Set)}) without redesigning {@code ChangeSet}. It
 * does not itself know about requirements, conformance, or evaluation.
 */
public final class ImpactScope {

    private final List<ChangedEntityRef> affectedEntities;

    private ImpactScope(List<ChangedEntityRef> affectedEntities) {
        this.affectedEntities = affectedEntities;
    }

    public static ImpactScope of(List<SemanticChange> semanticChanges) {
        Objects.requireNonNull(semanticChanges, "semanticChanges");
        Set<ChangedEntityRef> unique = new LinkedHashSet<>();
        for (SemanticChange change : semanticChanges) {
            unique.add(change.entity());
        }
        List<ChangedEntityRef> ordered = new ArrayList<>(unique);
        ordered.sort(ChangedEntityRef.identityComparator());
        return new ImpactScope(List.copyOf(ordered));
    }

    /** Stable domain entities affected by the owning {@link ChangeSet}, deterministically ordered. */
    public List<ChangedEntityRef> affectedEntities() {
        return affectedEntities;
    }

    /**
     * Whether any entity in this scope is also present in a candidate requirement/evaluation
     * scope. This is the entire seam a future requirement-registry match needs; it deliberately
     * does not know what a requirement is.
     */
    public boolean intersects(Set<ChangedEntityRef> candidateScope) {
        Objects.requireNonNull(candidateScope, "candidateScope");
        for (ChangedEntityRef entity : affectedEntities) {
            if (candidateScope.contains(entity)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return affectedEntities.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ImpactScope scope && affectedEntities.equals(scope.affectedEntities);
    }

    @Override
    public int hashCode() {
        return affectedEntities.hashCode();
    }

    @Override
    public String toString() {
        return "ImpactScope" + affectedEntities;
    }
}
