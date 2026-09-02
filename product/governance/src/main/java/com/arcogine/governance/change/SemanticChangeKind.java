package com.arcogine.governance.change;

/**
 * Small, deliberately extensible vocabulary of domain-neutral semantic change classifications.
 *
 * <p>This is not an exhaustive change ontology. It is the minimum classification needed to
 * distinguish structural transitions (entity added/removed) from content transitions (an entity's
 * semantics changed) without exposing arbitrary serialized property paths as the primary model.
 * Domains may express richer meaning in {@link SemanticChange#detail()}; the kind stays coarse and
 * stable so Governance can reason about it generically.
 */
public enum SemanticChangeKind {

    /** A stable domain entity present in the candidate state did not exist in the base state. */
    ENTITY_ADDED,

    /** A stable domain entity present in the base state no longer exists in the candidate state. */
    ENTITY_REMOVED,

    /** A stable domain entity exists in both states but its semantic content differs. */
    ENTITY_MODIFIED
}
