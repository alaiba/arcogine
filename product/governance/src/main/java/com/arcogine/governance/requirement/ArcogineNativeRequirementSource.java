package com.arcogine.governance.requirement;

/**
 * Marks a {@link Requirement} as internally governed by Arcogine itself (architecture rule,
 * internal technical policy, product/business constraint, or internally represented contractual
 * commitment) rather than by an external standard, regulation, or publication.
 *
 * <p>{@code rationale} is an optional human note; it never participates in identity/equality and
 * external-source metadata must never be required for a native requirement.
 */
public record ArcogineNativeRequirementSource(String rationale) implements RequirementSource {

    public ArcogineNativeRequirementSource {
        rationale = rationale == null ? "" : rationale;
    }

    public static ArcogineNativeRequirementSource of(String rationale) {
        return new ArcogineNativeRequirementSource(rationale);
    }

    public static ArcogineNativeRequirementSource unspecified() {
        return new ArcogineNativeRequirementSource("");
    }
}
