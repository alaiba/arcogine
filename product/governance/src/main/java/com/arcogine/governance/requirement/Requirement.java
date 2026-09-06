package com.arcogine.governance.requirement;

import java.util.Objects;

/**
 * "What obligation or rule exists?" -- a stable, versioned, human-meaningful Governance
 * requirement, independent of any executable {@code Assertion}, {@code ModelFingerprint},
 * {@code ControlledRevisionId}, or {@code ChangeSet}.
 *
 * <p>{@code id}/{@code version} are Arcogine-owned identity, never derived from wording, an
 * external source edition, or a framework/control mapping. Two {@code Requirement} values are
 * equal iff their {@code id} and {@code version} are equal -- {@code title}/{@code description}
 * are meaning, not identity, mirroring how {@link com.arcogine.governance.change.ChangedEntityRef}
 * already treats its own presentation-only {@code label}.
 *
 * <p>{@code source} distinguishes an Arcogine-native rule from an external-standard requirement;
 * only the latter carries exact external provenance. No framework-specific field (SOC 2, ISO
 * 27001, IEC, ISA, GDPR, ...) belongs on this core contract -- framework mappings are downstream
 * governance work.
 */
public record Requirement(
        RequirementId id,
        RequirementVersion version,
        String title,
        String description,
        RequirementSource source,
        RequirementScope scope) {

    public Requirement {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(version, "version");
        requireText(title, "title");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(scope, "scope");
        description = description == null ? "" : description;
    }

    private static void requireText(String value, String field) {
        if (Objects.requireNonNull(value, field).isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }

    public boolean isArcogineNative() {
        return source instanceof ArcogineNativeRequirementSource;
    }

    public boolean isExternallySourced() {
        return source instanceof ExternalRequirementSource;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Requirement requirement
                && id.equals(requirement.id)
                && version.equals(requirement.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version);
    }
}
