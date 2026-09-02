package com.arcogine.governance.catalogue;

import com.arcogine.governance.change.ImpactScope;
import com.arcogine.governance.requirement.Requirement;
import com.arcogine.governance.requirement.RequirementId;
import com.arcogine.governance.requirement.RequirementVersion;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable, headless, in-memory catalogue of known {@link Requirement} definitions.
 *
 * <p>This is the minimum registration/catalogue capability needed to make G2's documented
 * "registered requirements" seam real: deterministic construction, resolution by stable identity
 * and version, and selection of requirements whose {@link
 * com.arcogine.governance.requirement.RequirementScope} intersects a G2 {@link ImpactScope}.
 *
 * <p>It is not a database, a mutable workflow system, a plugin marketplace, framework ingestion,
 * or remote configuration. "Registered" here means only "known to this catalogue" -- it is not
 * "approved", "authorized", "active in production", or "compliant".
 */
public final class RequirementCatalogue {

    private final Map<RegistrationKey, Requirement> byKey;

    private RequirementCatalogue(Map<RegistrationKey, Requirement> byKey) {
        this.byKey = byKey;
    }

    public static RequirementCatalogue of(Requirement... requirements) {
        return of(List.of(requirements));
    }

    public static RequirementCatalogue of(List<Requirement> requirements) {
        Objects.requireNonNull(requirements, "requirements");
        Map<RegistrationKey, Requirement> byKey = new LinkedHashMap<>();
        for (Requirement requirement : requirements) {
            Objects.requireNonNull(requirement, "requirement");
            RegistrationKey key = new RegistrationKey(requirement.id(), requirement.version());
            if (byKey.putIfAbsent(key, requirement) != null) {
                throw new IllegalArgumentException(
                        "duplicate requirement registration: " + requirement.id() + " " + requirement.version());
            }
        }
        return new RequirementCatalogue(Map.copyOf(byKey));
    }

    public Optional<Requirement> resolve(RequirementId id, RequirementVersion version) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(version, "version");
        return Optional.ofNullable(byKey.get(new RegistrationKey(id, version)));
    }

    /** All registered requirements, deterministically ordered by identity then version. */
    public List<Requirement> all() {
        List<Requirement> ordered = new ArrayList<>(byKey.values());
        ordered.sort(
                Comparator.<Requirement, String>comparing(r -> r.id().value())
                        .thenComparing(r -> r.version().value()));
        return List.copyOf(ordered);
    }

    /**
     * Registered requirements whose explicit {@code RequirementScope} intersects the given G2
     * {@link ImpactScope}, deterministically ordered. An unrelated impact (no shared entity)
     * selects nothing.
     */
    public List<Requirement> potentiallyAffectedBy(ImpactScope impactScope) {
        Objects.requireNonNull(impactScope, "impactScope");
        List<Requirement> selected = new ArrayList<>();
        for (Requirement requirement : all()) {
            if (requirement.scope().intersects(impactScope)) {
                selected.add(requirement);
            }
        }
        return List.copyOf(selected);
    }

    public int size() {
        return byKey.size();
    }

    private record RegistrationKey(RequirementId id, RequirementVersion version) {}
}
