package com.arcogine.governance.requirement;

import java.util.Objects;
import java.util.Optional;

/**
 * Exact governing external publication/adoption backing a {@link Requirement}.
 *
 * <p>Deliberately more granular than a standards-family label: {@code authority} identifies the
 * publishing body (e.g. {@code "IEC"}, {@code "ANSI/ISA"}, a national standards body); {@code
 * designation} is the exact publication/standard number (e.g. {@code "IEC 62264-1"}); {@code
 * edition} is the exact edition/version/year (e.g. {@code "2013"}); {@code locator} is the
 * specific clause/section/page within that edition; {@code adoptionProfile} is an optional
 * national adoption or applicable profile (e.g. {@code "ANSI/ISA-95.00.01-2010"}). Two sources
 * differing in any of these fields are distinct provenance, never collapsed into a family label.
 *
 * <p>This slice never imports actual copyrighted standards text or builds a standards catalogue;
 * only this metadata shape is implemented, proven with synthetic fixtures.
 */
public record ExternalRequirementSource(
        String authority, String designation, String edition, String locator, String adoptionProfile)
        implements RequirementSource {

    public ExternalRequirementSource {
        requireText(authority, "authority");
        requireText(designation, "designation");
        requireText(edition, "edition");
        requireText(locator, "locator");
        adoptionProfile = adoptionProfile == null ? "" : adoptionProfile;
    }

    public static ExternalRequirementSource of(
            String authority, String designation, String edition, String locator) {
        return new ExternalRequirementSource(authority, designation, edition, locator, "");
    }

    public Optional<String> adoptionProfileOptional() {
        return adoptionProfile.isBlank() ? Optional.empty() : Optional.of(adoptionProfile);
    }

    private static void requireText(String value, String field) {
        if (Objects.requireNonNull(value, field).isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
