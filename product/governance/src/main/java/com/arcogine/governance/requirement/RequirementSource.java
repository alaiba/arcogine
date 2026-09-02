package com.arcogine.governance.requirement;

/**
 * Exact provenance of a {@link Requirement}'s governing authority.
 *
 * <p>A requirement is either {@link ArcogineNativeRequirementSource native} to Arcogine (an
 * internal architecture rule, technical policy, product/business constraint, or internally
 * represented contractual commitment) or {@link ExternalRequirementSource external}, in which
 * case the exact governing publication/adoption must be retained -- a standards-family label such
 * as "ISA-95 / IEC 62264" is not sufficient because the exact edition, national adoption, and
 * clause can change meaning.
 */
public sealed interface RequirementSource permits ArcogineNativeRequirementSource, ExternalRequirementSource {}
